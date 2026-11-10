package com.stockmanager.stock_manager.service;

import com.stockmanager.stock_manager.entity.Category;
import com.stockmanager.stock_manager.entity.Product;
import com.stockmanager.stock_manager.entity.Supplier;
import com.stockmanager.stock_manager.repository.CategoryRepository;
import com.stockmanager.stock_manager.repository.ProductRepository;
import com.stockmanager.stock_manager.repository.SupplierRepository;
import com.stockmanager.stock_manager.repository.StockMovementRepository;
import com.stockmanager.stock_manager.entity.StockMovement;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final StockMovementRepository stockMovementRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          SupplierRepository supplierRepository,
                          StockMovementRepository stockMovementRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produit introuvable avec l'id : " + id));
    }

    public Product findByBarcode(String barcode) {
        return productRepository.findByBarcode(barcode.trim())
                .orElseThrow(() -> new EntityNotFoundException("Aucun produit ne correspond à ce code-barres."));
    }

    public List<Product> search(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    public List<Product> findByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    public List<Product> findBySupplier(Long supplierId) {
        return productRepository.findBySupplierId(supplierId);
    }

    public List<Product> findLowStock() {
        return productRepository.findLowStockProducts();
    }

    public List<Product> findOutOfStock() {
        return productRepository.findOutOfStockProducts();
    }

    @Transactional
    public Product create(Product product, Long categoryId, Long supplierId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Catégorie introuvable avec l'id : " + categoryId));
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new EntityNotFoundException("Fournisseur introuvable avec l'id : " + supplierId));
        product.setCategory(category);
        product.setSupplier(supplier);
        normalizeProduct(product);
        if (product.getPurchasePrice() != null && product.getPurchasePrice().signum() < 0)
            throw new IllegalArgumentException("Le coût d'achat ne peut pas être négatif.");
        if (Boolean.TRUE.equals(product.getTrackExpiry()) && product.getQuantity() > 0)
            throw new IllegalArgumentException("Créez d'abord le produit sans stock, puis réceptionnez les quantités avec leurs lots et dates.");
        Product saved = productRepository.save(product);
        if (saved.getQuantity() > 0) {
            stockMovementRepository.save(new StockMovement(saved, "OPENING", saved.getQuantity(), 0.0,
                    saved.getQuantity(), "Stock initial à la création", null, "system"));
        }
        return saved;
    }

    @Transactional
    public Product update(Long id, Product updated, Long categoryId, Long supplierId) {
        Product existing = findById(id);
        if (Double.compare(existing.getQuantity(), updated.getQuantity()) != 0) {
            throw new IllegalStateException("Le stock doit être modifié via l'écran d'ajustement avec un motif.");
        }
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Catégorie introuvable avec l'id : " + categoryId));
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new EntityNotFoundException("Fournisseur introuvable avec l'id : " + supplierId));
        if (!Boolean.TRUE.equals(existing.getTrackExpiry()) && Boolean.TRUE.equals(updated.getTrackExpiry())
                && existing.getQuantity() > 0) {
            throw new IllegalStateException("Le suivi de péremption peut être activé lorsque le stock courant a été écoulé ou régularisé.");
        }
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setPrice(updated.getPrice());
        existing.setAlertThreshold(updated.getAlertThreshold());
        existing.setWholesalePrice(updated.getWholesalePrice());
        existing.setWholesaleQuantity(updated.getWholesaleQuantity());
        existing.setWholesaleUnit(updated.getWholesaleUnit());
        existing.setUnitType(updated.getUnitType());
        existing.setUnitLabel(updated.getUnitLabel());
        existing.setIsFractional(updated.getIsFractional());
        existing.setBaseUnitPrice(updated.getBaseUnitPrice());
        existing.setMinQuantity(updated.getMinQuantity());
        existing.setBarcode(normalizeIdentifier(updated.getBarcode()));
        existing.setSku(normalizeIdentifier(updated.getSku()));
        existing.setBrand(updated.getBrand());
        existing.setPurchasePrice(updated.getPurchasePrice());
        existing.setImageUrl(updated.getImageUrl());
        existing.setTrackExpiry(Boolean.TRUE.equals(updated.getTrackExpiry()));
        existing.setCategory(category);
        existing.setSupplier(supplier);
        if (existing.getPurchasePrice() != null && existing.getPurchasePrice().signum() < 0)
            throw new IllegalArgumentException("Le coût d'achat ne peut pas être négatif.");
        return productRepository.save(existing);
    }

    private void normalizeProduct(Product product) {
        product.setBarcode(normalizeIdentifier(product.getBarcode()));
        product.setSku(normalizeIdentifier(product.getSku()));
        product.setTrackExpiry(Boolean.TRUE.equals(product.getTrackExpiry()));
    }

    private String normalizeIdentifier(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    @Transactional
    public void delete(Long id) {
        findById(id);
        productRepository.deleteById(id);
    }
}
