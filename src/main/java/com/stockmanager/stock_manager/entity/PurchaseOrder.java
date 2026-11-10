package com.stockmanager.stock_manager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchase_orders")
public class PurchaseOrder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "order_number", nullable = false, unique = true, length = 100)
    private String orderNumber;
    @ManyToOne(optional = false) @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;
    @Column(nullable = false, length = 30)
    private String status;
    @Column(name = "ordered_at")
    private LocalDateTime orderedAt;
    @Column(name = "expected_at")
    private LocalDate expectedAt;
    @Column(name = "created_by", length = 80)
    private String createdBy;
    @Column(name = "total_amount")
    private BigDecimal totalAmount;
    @Column(length = 2000)
    private String notes;
    @Version
    @Column(nullable = false)
    private Long version = 0L;
    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<PurchaseOrderItem> items = new ArrayList<>();

    @PrePersist void onCreate() { if (orderedAt == null) orderedAt = LocalDateTime.now(); }
    public void addItem(PurchaseOrderItem item) { items.add(item); item.setPurchaseOrder(this); }
    public Long getId() { return id; }
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public Supplier getSupplier() { return supplier; }
    public void setSupplier(Supplier supplier) { this.supplier = supplier; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getOrderedAt() { return orderedAt; }
    public LocalDate getExpectedAt() { return expectedAt; }
    public void setExpectedAt(LocalDate expectedAt) { this.expectedAt = expectedAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Long getVersion() { return version; }
    public List<PurchaseOrderItem> getItems() { return items; }
}
