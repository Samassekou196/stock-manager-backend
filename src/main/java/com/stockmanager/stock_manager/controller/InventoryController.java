package com.stockmanager.stock_manager.controller;

import com.stockmanager.stock_manager.entity.Product;
import com.stockmanager.stock_manager.entity.StockMovement;
import com.stockmanager.stock_manager.entity.ProductBatch;
import com.stockmanager.stock_manager.repository.ProductRepository;
import com.stockmanager.stock_manager.repository.StockMovementRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.time.LocalDate;
import com.stockmanager.stock_manager.repository.ProductBatchRepository;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {
    private final ProductRepository products;
    private final StockMovementRepository movements;
    private final ProductBatchRepository batches;

    public InventoryController(ProductRepository products, StockMovementRepository movements, ProductBatchRepository batches) {
        this.products = products;
        this.movements = movements;
        this.batches = batches;
    }

    @GetMapping("/movements")
    public List<StockMovement> recentMovements() { return movements.findTop200ByOrderByCreatedAtDesc(); }

    @GetMapping("/products/{productId}/movements")
    public List<StockMovement> productMovements(@PathVariable Long productId) {
        return movements.findTop200ByProductIdOrderByCreatedAtDesc(productId);
    }

    @GetMapping("/batches")
    public List<ProductBatch> productBatches(@RequestParam(required = false) Long productId) {
        return productId == null ? batches.findAll() : batches.findByProductIdOrderByExpiryDateAsc(productId);
    }

    @GetMapping("/batches/expiring")
    public List<ProductBatch> expiring(@RequestParam(defaultValue = "30") int withinDays) {
        if (withinDays < 0 || withinDays > 3650) throw new IllegalArgumentException("La période doit être comprise entre 0 et 3650 jours.");
        return batches.findByExpiryDateBetweenAndQuantityRemainingGreaterThanOrderByExpiryDateAsc(
                LocalDate.now(), LocalDate.now().plusDays(withinDays), 0.0);
    }

    @PostMapping("/adjustments")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<StockMovement> adjust(@Valid @RequestBody AdjustmentRequest request) {
        if (request.quantityDelta == 0 || !Double.isFinite(request.quantityDelta)) {
            throw new IllegalArgumentException("La variation de stock doit être un nombre fini différent de zéro.");
        }
        Product product = products.findById(request.productId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Produit introuvable."));
        if (Boolean.TRUE.equals(product.getTrackExpiry()) && request.quantityDelta > 0) {
            throw new IllegalArgumentException("Pour un produit suivi par lot, enregistrez une réception via Achats afin de saisir la péremption.");
        }
        double before = product.getQuantity();
        double after = before + request.quantityDelta;
        if (!Double.isFinite(after) || after < 0) throw new IllegalArgumentException("Le stock ne peut pas devenir négatif.");
        product.setQuantity(after);
        products.saveAndFlush(product);
        String actor = SecurityContextHolder.getContext().getAuthentication().getName();
        StockMovement movement = movements.save(new StockMovement(product, "ADJUSTMENT",
                request.quantityDelta, before, after, request.reason.trim(), null, actor));
        return ResponseEntity.ok(movement);
    }

    public static class AdjustmentRequest {
        @NotNull public Long productId;
        @NotNull public Double quantityDelta;
        @NotBlank public String reason;
    }
}
