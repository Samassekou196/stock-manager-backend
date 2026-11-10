package com.stockmanager.stock_manager.repository;

import com.stockmanager.stock_manager.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    List<StockMovement> findTop200ByOrderByCreatedAtDesc();
    List<StockMovement> findTop200ByProductIdOrderByCreatedAtDesc(Long productId);
}
