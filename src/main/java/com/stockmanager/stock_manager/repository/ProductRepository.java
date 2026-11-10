package com.stockmanager.stock_manager.repository;

import com.stockmanager.stock_manager.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByBarcode(String barcode);

    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByCategoryId(Long categoryId);

    List<Product> findBySupplierId(Long supplierId);

    @Query("SELECT p FROM Product p WHERE p.quantity <= p.alertThreshold AND p.quantity > 0")
    List<Product> findLowStockProducts();

    @Query("SELECT p FROM Product p WHERE p.quantity <= 0")
    List<Product> findOutOfStockProducts();
}
