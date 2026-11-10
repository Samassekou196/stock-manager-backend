package com.stockmanager.stock_manager.repository;

import com.stockmanager.stock_manager.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    Optional<PurchaseOrder> findByOrderNumber(String orderNumber);
}
