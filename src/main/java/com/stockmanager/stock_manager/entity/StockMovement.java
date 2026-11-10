package com.stockmanager.stock_manager.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_movements")
public class StockMovement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false) @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "movement_type", nullable = false, length = 30)
    private String movementType;
    private Double quantityChange;
    private Double quantityBefore;
    private Double quantityAfter;
    @Column(nullable = false, length = 500)
    private String reason;
    private String referenceCode;
    private String actorUsername;
    private LocalDateTime createdAt;

    public StockMovement() {}
    public StockMovement(Product product, String movementType, Double quantityChange,
                         Double quantityBefore, Double quantityAfter, String reason,
                         String referenceCode, String actorUsername) {
        this.product = product;
        this.movementType = movementType;
        this.quantityChange = quantityChange;
        this.quantityBefore = quantityBefore;
        this.quantityAfter = quantityAfter;
        this.reason = reason;
        this.referenceCode = referenceCode;
        this.actorUsername = actorUsername;
        this.createdAt = LocalDateTime.now();
    }
    @PrePersist void setTimestamp() { if (createdAt == null) createdAt = LocalDateTime.now(); }
    public Long getId() { return id; }
    public Product getProduct() { return product; }
    public String getMovementType() { return movementType; }
    public Double getQuantityChange() { return quantityChange; }
    public Double getQuantityBefore() { return quantityBefore; }
    public Double getQuantityAfter() { return quantityAfter; }
    public String getReason() { return reason; }
    public String getReferenceCode() { return referenceCode; }
    public String getActorUsername() { return actorUsername; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
