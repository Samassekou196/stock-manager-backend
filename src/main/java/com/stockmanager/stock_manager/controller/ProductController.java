package com.stockmanager.stock_manager.controller;

import com.stockmanager.stock_manager.entity.Product;
import com.stockmanager.stock_manager.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAll() {
        return ResponseEntity.ok(productService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Product>> search(@RequestParam String name) {
        return ResponseEntity.ok(productService.search(name));
    }

    @GetMapping("/barcode/{barcode}")
    public ResponseEntity<Product> getByBarcode(@PathVariable String barcode) {
        return ResponseEntity.ok(productService.findByBarcode(barcode));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Product>> getByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(productService.findByCategory(categoryId));
    }

    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<List<Product>> getBySupplier(@PathVariable Long supplierId) {
        return ResponseEntity.ok(productService.findBySupplier(supplierId));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<Product>> getLowStock() {
        return ResponseEntity.ok(productService.findLowStock());
    }

    @GetMapping("/out-of-stock")
    public ResponseEntity<List<Product>> getOutOfStock() {
        return ResponseEntity.ok(productService.findOutOfStock());
    }

    @PostMapping
    public ResponseEntity<Product> create(
            @Valid @RequestBody Product product,
            @RequestParam Long categoryId,
            @RequestParam Long supplierId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.create(product, categoryId, supplierId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> update(
            @PathVariable Long id,
            @Valid @RequestBody Product product,
            @RequestParam Long categoryId,
            @RequestParam Long supplierId) {
        return ResponseEntity.ok(productService.update(id, product, categoryId, supplierId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
