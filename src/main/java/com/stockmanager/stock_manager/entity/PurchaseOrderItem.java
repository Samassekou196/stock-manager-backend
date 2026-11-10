package com.stockmanager.stock_manager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "purchase_order_items")
public class PurchaseOrderItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JsonIgnore
    @ManyToOne(optional = false) @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;
    @ManyToOne(optional = false) @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    @Column(name = "quantity_ordered", nullable = false)
    private Double quantityOrdered;
    @Column(name = "quantity_received", nullable = false)
    private Double quantityReceived = 0.0;
    @Column(name = "unit_cost", nullable = false)
    private BigDecimal unitCost;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public PurchaseOrder getPurchaseOrder() { return purchaseOrder; }
    public void setPurchaseOrder(PurchaseOrder purchaseOrder) { this.purchaseOrder = purchaseOrder; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public Double getQuantityOrdered() { return quantityOrdered; }
    public void setQuantityOrdered(Double quantityOrdered) { this.quantityOrdered = quantityOrdered; }
    public Double getQuantityReceived() { return quantityReceived; }
    public void setQuantityReceived(Double quantityReceived) { this.quantityReceived = quantityReceived; }
    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }
}
