package com.stockmanager.stock_manager.controller;

import com.stockmanager.stock_manager.entity.Customer;
import com.stockmanager.stock_manager.entity.Sale;
import com.stockmanager.stock_manager.service.CustomerService;
import com.stockmanager.stock_manager.service.SaleService;
import com.stockmanager.stock_manager.service.SaleService.SaleItemRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/sales")
public class SaleController {

    private final SaleService saleService;
    private final CustomerService customerService;

    public SaleController(SaleService saleService, CustomerService customerService) {
        this.saleService = saleService;
        this.customerService = customerService;
    }

    @GetMapping
    public ResponseEntity<List<Sale>> getAll() {
        return ResponseEntity.ok(saleService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sale> getById(@PathVariable Long id) {
        return ResponseEntity.ok(saleService.findById(id));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Sale>> getByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(saleService.findByCustomer(customerId));
    }

    @GetMapping("/today")
    public ResponseEntity<List<Sale>> getToday() {
        return ResponseEntity.ok(saleService.findToday());
    }

    @GetMapping("/this-month")
    public ResponseEntity<List<Sale>> getThisMonth() {
        return ResponseEntity.ok(saleService.findThisMonth());
    }

    @PostMapping
    public ResponseEntity<Sale> create(@Valid @NotEmpty @RequestBody List<SaleItemRequest> items,
                                       @RequestParam(required = false) Long customerId,
                                       @RequestParam(required = false) String paymentMethod,
                                       @RequestParam(required = false) BigDecimal amountReceived) {
        Long selectedCustomerId = customerId != null
                ? customerId
                : customerService.getOrCreateAnonymous().getId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(saleService.create(selectedCustomerId, items, paymentMethod, amountReceived));
    }
}
