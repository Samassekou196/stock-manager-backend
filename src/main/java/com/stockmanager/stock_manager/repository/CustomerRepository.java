package com.stockmanager.stock_manager.repository;

import com.stockmanager.stock_manager.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findByLastNameContainingIgnoreCaseOrFirstNameContainingIgnoreCase(
            String lastName, String firstName);
    List<Customer> findByLastNameIgnoreCase(String lastName);
}