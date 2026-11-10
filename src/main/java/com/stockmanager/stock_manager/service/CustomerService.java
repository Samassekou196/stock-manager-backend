package com.stockmanager.stock_manager.service;

import com.stockmanager.stock_manager.entity.Customer;
import com.stockmanager.stock_manager.repository.CustomerRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    public Customer findById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Client introuvable avec l'id : " + id));
    }

    public List<Customer> search(String name) {
        return customerRepository
                .findByLastNameContainingIgnoreCaseOrFirstNameContainingIgnoreCase(name, name);
    }

    public Customer getOrCreateAnonymous() {
        return customerRepository.findByLastNameIgnoreCase("Client")
                .stream()
                .filter(c -> c.getFirstName().equalsIgnoreCase("Anonyme"))
                .findFirst()
                .orElseGet(() -> {
                    Customer anonymous = new Customer();
                    anonymous.setLastName("Client");
                    anonymous.setFirstName("Anonyme");
                    return customerRepository.save(anonymous);
                });
    }

    @Transactional
    public Customer create(Customer customer) {
        return customerRepository.save(customer);
    }

    @Transactional
    public Customer update(Long id, Customer updated) {
        Customer existing = findById(id);
        existing.setLastName(updated.getLastName());
        existing.setFirstName(updated.getFirstName());
        existing.setPhone(updated.getPhone());
        existing.setEmail(updated.getEmail());
        existing.setAddress(updated.getAddress());
        return customerRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        Customer customer = findById(id);
        if (customer.getSales() != null && !customer.getSales().isEmpty()) {
            throw new IllegalStateException("Impossible de supprimer un client qui possède des ventes.");
        }
        customerRepository.deleteById(id);
    }
}