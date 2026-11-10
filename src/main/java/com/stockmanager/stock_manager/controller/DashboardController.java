package com.stockmanager.stock_manager.controller;

import com.stockmanager.stock_manager.repository.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final CustomerRepository customerRepository;
    private final SaleRepository saleRepository;

    public DashboardController(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            SupplierRepository supplierRepository,
            CustomerRepository customerRepository,
            SaleRepository saleRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
        this.customerRepository = customerRepository;
        this.saleRepository = saleRepository;
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();

        // Compteurs
        stats.put("totalProducts", productRepository.count());
        stats.put("totalCategories", categoryRepository.count());
        stats.put("totalSuppliers", supplierRepository.count());
        stats.put("totalCustomers", customerRepository.count());
        stats.put("totalSales", saleRepository.count());

        // Chiffre d'affaires du jour
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        BigDecimal caJour = saleRepository.findBySaleDateBetween(startOfDay, endOfDay)
                .stream()
                .map(s -> s.getTotalAmount() != null ? s.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("caJour", caJour);

        // Chiffre d'affaires du mois
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay();
        BigDecimal caMois = saleRepository.findBySaleDateBetween(startOfMonth, LocalDateTime.now())
                .stream()
                .map(s -> s.getTotalAmount() != null ? s.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("caMois", caMois);

        // Valeur totale du stock
        BigDecimal valeurStock = productRepository.findAll()
                .stream()
                .map(p -> p.getPrice().multiply(BigDecimal.valueOf(p.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("valeurStock", valeurStock);

        // Produits en rupture
        stats.put("produitsRupture", productRepository.findOutOfStockProducts().size());
        // Produits stock faible
        stats.put("produitsStockFaible", productRepository.findLowStockProducts().size());

        // Dernières ventes (5 dernières)
        List<Map<String, Object>> derniereVentes = new ArrayList<>();
        saleRepository.findAll().stream()
                .sorted((a, b) -> b.getSaleDate().compareTo(a.getSaleDate()))
                .limit(5)
                .forEach(sale -> {
                    Map<String, Object> v = new HashMap<>();
                    v.put("id", sale.getId());
                    v.put("invoiceNumber", sale.getInvoiceNumber());
                    v.put("totalAmount", sale.getTotalAmount());
                    v.put("saleDate", sale.getSaleDate());
                    derniereVentes.add(v);
                });
        stats.put("derniereVentes", derniereVentes);

        return ResponseEntity.ok(stats);
    }
}