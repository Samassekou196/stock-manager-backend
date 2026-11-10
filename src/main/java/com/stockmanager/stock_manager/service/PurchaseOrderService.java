package com.stockmanager.stock_manager.service;

import com.stockmanager.stock_manager.entity.*;
import com.stockmanager.stock_manager.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class PurchaseOrderService {
    private final PurchaseOrderRepository orders;
    private final SupplierRepository suppliers;
    private final ProductRepository products;
    private final StockMovementRepository movements;
    private final ProductBatchRepository batches;

    public PurchaseOrderService(PurchaseOrderRepository orders, SupplierRepository suppliers,
            ProductRepository products, StockMovementRepository movements, ProductBatchRepository batches) {
        this.orders = orders; this.suppliers = suppliers; this.products = products;
        this.movements = movements; this.batches = batches;
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrder> findAll() { return orders.findAll(); }

    @Transactional(readOnly = true)
    public PurchaseOrder findById(Long id) {
        return orders.findById(id).orElseThrow(() -> new EntityNotFoundException("Commande fournisseur introuvable."));
    }

    @Transactional
    public PurchaseOrder create(Long supplierId, LocalDate expectedAt, String notes, List<LineInput> lines) {
        if (lines == null || lines.isEmpty()) throw new IllegalArgumentException("La commande doit contenir au moins un article.");
        Supplier supplier = suppliers.findById(supplierId)
                .orElseThrow(() -> new EntityNotFoundException("Fournisseur introuvable."));
        PurchaseOrder order = new PurchaseOrder();
        order.setSupplier(supplier);
        order.setStatus("ORDERED");
        order.setExpectedAt(expectedAt);
        order.setNotes(notes == null ? null : notes.trim());
        var auth = SecurityContextHolder.getContext().getAuthentication();
        order.setCreatedBy(auth == null ? "system" : auth.getName());
        order.setOrderNumber("ACH-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        BigDecimal total = BigDecimal.ZERO;
        Set<Long> productIds = new HashSet<>();
        for (LineInput input : lines) {
            if (input == null || input.productId == null || input.quantity == null || input.unitCost == null)
                throw new IllegalArgumentException("Chaque ligne doit inclure un produit, une quantité et un coût.");
            if (!Double.isFinite(input.quantity) || input.quantity <= 0 || input.unitCost.signum() < 0)
                throw new IllegalArgumentException("La quantité doit être positive et le coût d'achat ne peut pas être négatif.");
            if (!productIds.add(input.productId)) throw new IllegalArgumentException("Un produit ne peut apparaître qu'une fois dans la commande.");
            Product product = products.findById(input.productId)
                    .orElseThrow(() -> new EntityNotFoundException("Produit introuvable."));
            if (!Boolean.TRUE.equals(product.getIsFractional()) && input.quantity != Math.rint(input.quantity))
                throw new IllegalArgumentException("Les produits non fractionnables doivent être commandés en unités entières.");
            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setProduct(product); item.setQuantityOrdered(input.quantity); item.setUnitCost(input.unitCost);
            order.addItem(item);
            total = total.add(input.unitCost.multiply(BigDecimal.valueOf(input.quantity)));
        }
        order.setTotalAmount(total.setScale(2, java.math.RoundingMode.HALF_UP));
        return orders.save(order);
    }

    @Transactional
    public PurchaseOrder receive(Long orderId, List<ReceiptLineInput> receivedLines) {
        if (receivedLines == null || receivedLines.isEmpty()) throw new IllegalArgumentException("Aucune quantité reçue n'a été fournie.");
        PurchaseOrder order = orders.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Commande fournisseur introuvable."));
        if (order.getStatus().equals("CANCELLED") || order.getStatus().equals("RECEIVED"))
            throw new IllegalStateException("Cette commande n'accepte plus de réception.");
        Set<Long> processed = new HashSet<>();
        for (ReceiptLineInput input : receivedLines) {
            if (input == null || input.orderItemId == null || input.quantity == null)
                throw new IllegalArgumentException("Chaque ligne reçue doit inclure son identifiant et sa quantité.");
            if (!Double.isFinite(input.quantity) || input.quantity <= 0)
                throw new IllegalArgumentException("La quantité reçue doit être positive.");
            if (!processed.add(input.orderItemId)) throw new IllegalArgumentException("Une ligne ne peut être reçue qu'une fois par opération.");
            PurchaseOrderItem item = order.getItems().stream().filter(i -> i.getId().equals(input.orderItemId)).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("La ligne ne fait pas partie de cette commande."));
            double remaining = item.getQuantityOrdered() - item.getQuantityReceived();
            if (input.quantity > remaining + 1e-9) throw new IllegalArgumentException("La quantité reçue dépasse le reliquat commandé.");
            Product product = item.getProduct();
            if (!Boolean.TRUE.equals(product.getIsFractional()) && input.quantity != Math.rint(input.quantity))
                throw new IllegalArgumentException("Les produits non fractionnables doivent être reçus en unités entières.");
            if (Boolean.TRUE.equals(product.getTrackExpiry()) && input.expiryDate == null)
                throw new IllegalArgumentException("La date de péremption est obligatoire pour ce produit.");
            double before = product.getQuantity();
            product.setQuantity(before + input.quantity);
            products.save(product);
            item.setQuantityReceived(item.getQuantityReceived() + input.quantity);
            String batchCode = input.batchCode == null || input.batchCode.isBlank()
                    ? order.getOrderNumber() + "-" + item.getId() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase()
                    : input.batchCode.trim();
            batches.save(new ProductBatch(product, batchCode, input.expiryDate, input.quantity, item.getUnitCost()));
            var auth = SecurityContextHolder.getContext().getAuthentication();
            movements.save(new StockMovement(product, "PURCHASE", input.quantity, before, product.getQuantity(),
                    "Réception commande " + order.getOrderNumber(), order.getOrderNumber(), auth == null ? "system" : auth.getName()));
        }
        boolean complete = order.getItems().stream().allMatch(i -> i.getQuantityReceived() + 1e-9 >= i.getQuantityOrdered());
        boolean started = order.getItems().stream().anyMatch(i -> i.getQuantityReceived() > 0);
        order.setStatus(complete ? "RECEIVED" : started ? "PARTIALLY_RECEIVED" : "ORDERED");
        return orders.save(order);
    }

    public static class LineInput { public Long productId; public Double quantity; public BigDecimal unitCost; }
    public static class ReceiptLineInput { public Long orderItemId; public Double quantity; public String batchCode; public LocalDate expiryDate; }
}
