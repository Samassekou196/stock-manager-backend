package com.stockmanager.stock_manager.repository;

import com.stockmanager.stock_manager.entity.ProductBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface ProductBatchRepository extends JpaRepository<ProductBatch, Long> {
    List<ProductBatch> findByQuantityRemainingGreaterThanAndExpiryDateLessThanEqualOrderByExpiryDateAsc(Double quantity, LocalDate expiryDate);
    List<ProductBatch> findByExpiryDateBetweenAndQuantityRemainingGreaterThanOrderByExpiryDateAsc(LocalDate from, LocalDate to, Double quantity);
    List<ProductBatch> findByProductIdOrderByExpiryDateAsc(Long productId);
    List<ProductBatch> findByProductIdAndExpiryDateGreaterThanEqualAndQuantityRemainingGreaterThanOrderByExpiryDateAsc(Long productId, LocalDate expiryDate, Double quantity);
}
