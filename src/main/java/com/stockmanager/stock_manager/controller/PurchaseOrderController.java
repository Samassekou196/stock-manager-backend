package com.stockmanager.stock_manager.controller;

import com.stockmanager.stock_manager.entity.PurchaseOrder;
import com.stockmanager.stock_manager.service.PurchaseOrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/purchases")
public class PurchaseOrderController {
    private final PurchaseOrderService service;
    public PurchaseOrderController(PurchaseOrderService service) { this.service = service; }

    @GetMapping
    public List<PurchaseOrder> all() { return service.findAll(); }

    @GetMapping("/{id}")
    public PurchaseOrder one(@PathVariable Long id) { return service.findById(id); }

    @PostMapping
    public ResponseEntity<PurchaseOrder> create(@Valid @RequestBody CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(
                request.supplierId, request.expectedAt, request.notes, request.items.stream().map(line -> {
                    PurchaseOrderService.LineInput input = new PurchaseOrderService.LineInput();
                    input.productId = line.productId; input.quantity = line.quantity; input.unitCost = line.unitCost;
                    return input;
                }).toList()));
    }

    @PostMapping("/{id}/receipts")
    public PurchaseOrder receive(@PathVariable Long id, @Valid @RequestBody ReceiptRequest request) {
        return service.receive(id, request.items.stream().map(line -> {
            PurchaseOrderService.ReceiptLineInput input = new PurchaseOrderService.ReceiptLineInput();
            input.orderItemId = line.orderItemId; input.quantity = line.quantity;
            input.batchCode = line.batchCode; input.expiryDate = line.expiryDate;
            return input;
        }).toList());
    }

    public static class CreateRequest {
        @NotNull public Long supplierId;
        public LocalDate expectedAt;
        public String notes;
        @NotEmpty public List<@Valid Line> items;
    }
    public static class Line {
        @NotNull public Long productId;
        @NotNull public Double quantity;
        @NotNull public BigDecimal unitCost;
    }
    public static class ReceiptRequest {
        @NotEmpty public List<@Valid ReceiptLine> items;
    }
    public static class ReceiptLine {
        @NotNull public Long orderItemId;
        @NotNull public Double quantity;
        public String batchCode;
        public LocalDate expiryDate;
    }
}
