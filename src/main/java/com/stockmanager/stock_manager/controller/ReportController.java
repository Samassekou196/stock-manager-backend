package com.stockmanager.stock_manager.controller;

import com.stockmanager.stock_manager.entity.Sale;
import com.stockmanager.stock_manager.repository.ProductRepository;
import com.stockmanager.stock_manager.repository.SaleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;

    public ReportController(SaleRepository saleRepository,
                            ProductRepository productRepository) {
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
    }

    @GetMapping("/sales")
    public ResponseEntity<Map<String, Object>> getSalesReport(
            @RequestParam String period) {

        LocalDateTime start;
        LocalDateTime end = LocalDateTime.now();

        switch (period) {
            case "today":
                start = LocalDate.now().atStartOfDay();
                break;
            case "week":
                start = LocalDate.now().minusDays(7).atStartOfDay();
                break;
            case "month":
                start = LocalDate.now().withDayOfMonth(1).atStartOfDay();
                break;
            case "year":
                start = LocalDate.now().withDayOfYear(1).atStartOfDay();
                break;
            default:
                start = LocalDateTime.of(2000, 1, 1, 0, 0);
        }

        List<Sale> sales = saleRepository.findBySaleDateBetween(start, end);

        BigDecimal totalCA = sales.stream()
                .map(s -> s.getTotalAmount() != null ? s.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Map<String, Object>> salesData = new ArrayList<>();
        for (Sale sale : sales) {
            Map<String, Object> s = new HashMap<>();
            s.put("id", sale.getId());
            s.put("invoiceNumber", sale.getInvoiceNumber());
            s.put("totalAmount", sale.getTotalAmount());
            s.put("saleDate", sale.getSaleDate());
            s.put("customerName", sale.getCustomer() != null
                    ? sale.getCustomer().getLastName() + " " + sale.getCustomer().getFirstName()
                    : "Client Anonyme");
            salesData.add(s);
        }

        Map<String, Object> report = new HashMap<>();
        report.put("period", period);
        report.put("totalSales", sales.size());
        report.put("totalCA", totalCA);
        report.put("sales", salesData);

        return ResponseEntity.ok(report);
    }

    @GetMapping("/low-stock")
    public ResponseEntity<Map<String, Object>> getLowStockReport() {
        Map<String, Object> report = new HashMap<>();
        report.put("lowStock", productRepository.findLowStockProducts());
        report.put("outOfStock", productRepository.findOutOfStockProducts());        return ResponseEntity.ok(report);
    }
}