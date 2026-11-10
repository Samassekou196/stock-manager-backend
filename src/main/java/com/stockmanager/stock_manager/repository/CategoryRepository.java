package com.stockmanager.stock_manager.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.stockmanager.stock_manager.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByNameIgnoreCase(String name);
    
    List<Category> findByNameContainingIgnoreCase(String name);
    
    boolean existsByNameIgnoreCase(String name);
}