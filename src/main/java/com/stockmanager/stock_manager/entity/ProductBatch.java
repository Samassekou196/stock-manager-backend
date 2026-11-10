package com.stockmanager.stock_manager.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_batches")
public class ProductBatch {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    @Column(name = "batch_code", length = 80)
    private String batchCode;
    @Column(name = "expiry_date")
    private LocalDate expiryDate;
    @Column(name = "quantity_received")
    private Double quantityReceived;
    @Column(name = "quantity_remaining")
    private Double quantityRemaining;
    @Column(name = "unit_cost")
    private BigDecimal unitCost;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Version @Column(nullable = false)
    private Long version = 0L;

    @PrePersist void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }
    public ProductBatch() {}
    public ProductBatch(Product product, String batchCode, LocalDate expiryDate, double quantity, BigDecimal unitCost) {
        this.product = product; this.batchCode = batchCode; this.expiryDate = expiryDate;
        this.quantityReceived = quantity; this.quantityRemaining = quantity; this.unitCost = unitCost;
    }
    public Long getId() { return id; }
    public Product getProduct() { return product; }
    public String getBatchCode() { return batchCode; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public Double getQuantityReceived() { return quantityReceived; }
    public Double getQuantityRemaining() { return quantityRemaining; }
    public void setQuantityRemaining(Double quantityRemaining) { this.quantityRemaining = quantityRemaining; }
    public BigDecimal getUnitCost() { return unitCost; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
