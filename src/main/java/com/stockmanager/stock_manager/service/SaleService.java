package com.stockmanager.stock_manager.service;

import com.stockmanager.stock_manager.entity.*;
import com.stockmanager.stock_manager.repository.*;
import com.stockmanager.stock_manager.entity.StockMovement;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class SaleService {

    private final SaleRepository saleRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ProductBatchRepository productBatchRepository;

    public SaleService(SaleRepository saleRepository,
                       CustomerRepository customerRepository,
                       ProductRepository productRepository,
                       StockMovementRepository stockMovementRepository,
                       ProductBatchRepository productBatchRepository) {
        this.saleRepository = saleRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.productBatchRepository = productBatchRepository;
    }

    public List<Sale> findAll() {
        return saleRepository.findAll();
    }

    public Sale findById(Long id) {
        return saleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vente introuvable avec l'id : " + id));
    }

    public List<Sale> findByCustomer(Long customerId) {
        return saleRepository.findByCustomerId(customerId);
    }

    public List<Sale> findToday() {
        LocalDateTime start = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        return saleRepository.findBySaleDateBetween(start, end);
    }

    public List<Sale> findThisMonth() {
        LocalDateTime start = LocalDateTime.now()
                .withDayOfMonth(1).toLocalDate().atStartOfDay();
        LocalDateTime end = LocalDateTime.now();
        return saleRepository.findBySaleDateBetween(start, end);
    }

    @Transactional
    public Sale create(Long customerId, List<SaleItemRequest> itemRequests) {
        return create(customerId, itemRequests, "CASH", null);
    }

    @Transactional
    public Sale create(Long customerId, List<SaleItemRequest> itemRequests, String paymentMethod, BigDecimal amountReceived) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Client introuvable avec l'id : " + customerId));

        Sale sale = new Sale();
        sale.setCustomer(customer);
        sale.setInvoiceNumber(generateInvoiceNumber());
        sale.setTotalAmount(BigDecimal.ZERO);
        String normalizedPayment = paymentMethod == null ? "CASH" : paymentMethod.trim().toUpperCase();
        if (!List.of("CASH", "CARD", "MOBILE_MONEY", "OTHER").contains(normalizedPayment)) {
            throw new IllegalArgumentException("Mode de paiement invalide.");
        }
        sale.setPaymentMethod(normalizedPayment);

        List<SaleItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        if (itemRequests == null || itemRequests.isEmpty()) {
            throw new IllegalArgumentException("Une vente doit contenir au moins un produit.");
        }

        for (SaleItemRequest req : itemRequests) {
            if (req == null || req.getProductId() == null) {
                throw new IllegalArgumentException("Chaque ligne doit désigner un produit.");
            }
            Product product = productRepository.findById(req.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Produit introuvable avec l'id : " + req.getProductId()));

            if (req.getQuantity() == null || !Double.isFinite(req.getQuantity()) || req.getQuantity() <= 0) {
                throw new IllegalArgumentException("La quantité doit être positive pour le produit : " + product.getName());
            }
            if (!Boolean.TRUE.equals(product.getIsFractional())
                    && req.getQuantity() != Math.rint(req.getQuantity())) {
                throw new IllegalArgumentException("Ce produit ne peut pas être vendu en quantité fractionnaire : " + product.getName());
            }

            String saleType = req.getSaleType() == null ? "EACH" : req.getSaleType().trim().toUpperCase();
            if (!saleType.equals("EACH") && !saleType.equals("WHOLESALE")) {
                throw new IllegalArgumentException("Type de vente inconnu pour le produit : " + product.getName());
            }
            double stockQuantity = saleType.equals("WHOLESALE")
                    ? req.getQuantity() * (product.getWholesaleQuantity() == null ? 0 : product.getWholesaleQuantity())
                    : req.getQuantity();
            if (stockQuantity <= 0 || !Double.isFinite(stockQuantity)) {
                throw new IllegalArgumentException("La quantité de gros n'est pas configurée pour le produit : " + product.getName());
            }
            if (saleType.equals("WHOLESALE") && product.getWholesalePrice() == null) {
                throw new IllegalArgumentException("Le prix de gros n'est pas configuré pour le produit : " + product.getName());
            }
            if (product.getQuantity() < stockQuantity) {
                throw new IllegalStateException(
                        "Stock insuffisant pour le produit : " + product.getName()
                        + ". Stock disponible : " + product.getQuantity());
            }

            if (Boolean.TRUE.equals(product.getTrackExpiry())) {
                List<ProductBatch> eligibleBatches = productBatchRepository
                        .findByProductIdAndExpiryDateGreaterThanEqualAndQuantityRemainingGreaterThanOrderByExpiryDateAsc(
                                product.getId(), java.time.LocalDate.now(), 0.0);
                double datedQuantity = eligibleBatches.stream().mapToDouble(ProductBatch::getQuantityRemaining).sum();
                if (datedQuantity + 1e-9 < stockQuantity) {
                    throw new IllegalStateException("Stock valide insuffisant dans les lots non périmés pour : " + product.getName());
                }
                double remaining = stockQuantity;
                for (ProductBatch batch : eligibleBatches) {
                    if (remaining <= 1e-9) break;
                    double fromBatch = Math.min(remaining, batch.getQuantityRemaining());
                    batch.setQuantityRemaining(batch.getQuantityRemaining() - fromBatch);
                    productBatchRepository.save(batch);
                    remaining -= fromBatch;
                }
            }

            double quantityBefore = product.getQuantity();
            product.setQuantity(quantityBefore - stockQuantity);
            productRepository.save(product);
            String cashier = org.springframework.security.core.context.SecurityContextHolder.getContext()
                    .getAuthentication() == null ? "system"
                    : org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
            stockMovementRepository.save(new StockMovement(product, "SALE", -stockQuantity,
                    quantityBefore, product.getQuantity(), "Vente", sale.getInvoiceNumber(), cashier));

            BigDecimal unitPrice = saleType.equals("WHOLESALE")
                    ? product.getWholesalePrice()
                    : (Boolean.TRUE.equals(product.getIsFractional()) && product.getBaseUnitPrice() != null
                        ? product.getBaseUnitPrice() : product.getPrice());
            BigDecimal subTotal = unitPrice.multiply(BigDecimal.valueOf(req.getQuantity()))
                    .setScale(2, java.math.RoundingMode.HALF_UP);

            SaleItem item = new SaleItem();
            item.setSale(sale);
            item.setProduct(product);
            item.setQuantity(req.getQuantity());
            item.setStockQuantity(stockQuantity);
            item.setUnitLabel(saleType.equals("WHOLESALE")
                    ? (product.getWholesaleUnit() == null ? "Gros" : product.getWholesaleUnit())
                    : (product.getUnitLabel() == null ? "Unité" : product.getUnitLabel()));
            item.setUnitPrice(unitPrice);
            item.setSubTotal(subTotal);

            items.add(item);
            total = total.add(subTotal);
        }

        sale.setItems(items);
        sale.setTotalAmount(total);
        BigDecimal received = amountReceived == null ? total : amountReceived;
        if (received.signum() < 0 || (normalizedPayment.equals("CASH") && received.compareTo(total) < 0)) {
            throw new IllegalArgumentException("Le montant reçu ne peut pas être inférieur au total de la vente.");
        }
        sale.setAmountReceived(received);
        sale.setChangeAmount(normalizedPayment.equals("CASH") ? received.subtract(total) : BigDecimal.ZERO);
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            sale.setCashierUsername(authentication.getName());
        }

        return saleRepository.save(sale);
    }

    private String generateInvoiceNumber() {
        String uuid = UUID.randomUUID().toString().toUpperCase().replace("-", "").substring(0, 8);
        return "FAC-" + LocalDateTime.now().getYear() + "-" + uuid;
    }

    public static class SaleItemRequest {
        private Long productId;
        private Double quantity;
        private BigDecimal unitPrice;
        private BigDecimal subTotal;
        private String saleType;

        public SaleItemRequest() {}

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }

        public Double getQuantity() { return quantity; }
        public void setQuantity(Double quantity) { this.quantity = quantity; }

        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

        public BigDecimal getSubTotal() { return subTotal; }
        public void setSubTotal(BigDecimal subTotal) { this.subTotal = subTotal; }
        public String getSaleType() { return saleType; }
        public void setSaleType(String saleType) { this.saleType = saleType; }
    }
}
