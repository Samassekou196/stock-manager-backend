package com.stockmanager.stock_manager.service;

import com.stockmanager.stock_manager.entity.Supplier;
import com.stockmanager.stock_manager.repository.SupplierRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    public List<Supplier> findAll() {
        return supplierRepository.findAll();
    }

    public Supplier findById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Fournisseur introuvable avec l'id : " + id));
    }

    public List<Supplier> search(String name) {
        return supplierRepository.findByNameContainingIgnoreCase(name);
    }

    @Transactional
    public Supplier create(Supplier supplier) {
        return supplierRepository.save(supplier);
    }

    @Transactional
    public Supplier update(Long id, Supplier updated) {
        Supplier existing = findById(id);
        existing.setName(updated.getName());
        existing.setAddress(updated.getAddress());
        existing.setPhone(updated.getPhone());
        existing.setEmail(updated.getEmail());
        existing.setCity(updated.getCity());
        existing.setCountry(updated.getCountry());
        return supplierRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        Supplier supplier = findById(id);
        if (supplier.getProducts() != null && !supplier.getProducts().isEmpty()) {
            throw new IllegalStateException("Impossible de supprimer un fournisseur qui possède des produits.");
        }
        supplierRepository.deleteById(id);
    }
}