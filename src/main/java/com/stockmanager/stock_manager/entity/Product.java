package com.stockmanager.stock_manager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    @Column(nullable = false)
    private String name;

    private String description;

    @NotNull(message = "Le prix est obligatoire")
    @Column(nullable = false)
    private BigDecimal price;

    @NotNull(message = "La quantité est obligatoire")
    @Column(nullable = false)
    private Double quantity;

    @Column(name = "alert_threshold")
    private Double alertThreshold = 5.0;

    @Column(name = "wholesale_price")
    private BigDecimal wholesalePrice;

    @Column(name = "wholesale_quantity")
    private Double wholesaleQuantity;

    @Column(name = "wholesale_unit")
    private String wholesaleUnit;

    @Column(name = "unit_type")
    private String unitType;

    @Column(name = "unit_label")
    private String unitLabel;

    @Column(name = "is_fractional")
    private Boolean isFractional = false;

    @Column(name = "base_unit_price")
    private BigDecimal baseUnitPrice;

    @Column(name = "min_quantity")
    private Double minQuantity;

    @Column(name = "quick_sale_amounts")
    private String quickSaleAmounts;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(length = 64, unique = true)
    private String barcode;
    @Column(length = 64, unique = true)
    private String sku;
    @Column(length = 120)
    private String brand;
    @Column(name = "purchase_price", precision = 19, scale = 2)
    private BigDecimal purchasePrice;
    @Column(name = "image_url", length = 500)
    private String imageUrl;
    @Column(name = "track_expiry", nullable = false)
    private Boolean trackExpiry = false;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public Product() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }

    public Double getAlertThreshold() { return alertThreshold; }
    public void setAlertThreshold(Double alertThreshold) { this.alertThreshold = alertThreshold; }

    public BigDecimal getWholesalePrice() { return wholesalePrice; }
    public void setWholesalePrice(BigDecimal wholesalePrice) { this.wholesalePrice = wholesalePrice; }

    public Double getWholesaleQuantity() { return wholesaleQuantity; }
    public void setWholesaleQuantity(Double wholesaleQuantity) { this.wholesaleQuantity = wholesaleQuantity; }

    public String getWholesaleUnit() { return wholesaleUnit; }
    public void setWholesaleUnit(String wholesaleUnit) { this.wholesaleUnit = wholesaleUnit; }

    public String getUnitType() { return unitType; }
    public void setUnitType(String unitType) { this.unitType = unitType; }

    public String getUnitLabel() { return unitLabel; }
    public void setUnitLabel(String unitLabel) { this.unitLabel = unitLabel; }

    public Boolean getIsFractional() { return isFractional; }
    public void setIsFractional(Boolean isFractional) { this.isFractional = isFractional; }

    public BigDecimal getBaseUnitPrice() { return baseUnitPrice; }
    public void setBaseUnitPrice(BigDecimal baseUnitPrice) { this.baseUnitPrice = baseUnitPrice; }

    public Double getMinQuantity() { return minQuantity; }
    public void setMinQuantity(Double minQuantity) { this.minQuantity = minQuantity; }

    public String getQuickSaleAmounts() { return quickSaleAmounts; }
    public void setQuickSaleAmounts(String quickSaleAmounts) { this.quickSaleAmounts = quickSaleAmounts; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public Supplier getSupplier() { return supplier; }
    public void setSupplier(Supplier supplier) { this.supplier = supplier; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public BigDecimal getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(BigDecimal purchasePrice) { this.purchasePrice = purchasePrice; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Boolean getTrackExpiry() { return trackExpiry; }
    public void setTrackExpiry(Boolean trackExpiry) { this.trackExpiry = trackExpiry; }
}
