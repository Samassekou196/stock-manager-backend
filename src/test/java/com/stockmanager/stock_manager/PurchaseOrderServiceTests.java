package com.stockmanager.stock_manager;

import com.stockmanager.stock_manager.entity.*;
import com.stockmanager.stock_manager.repository.*;
import com.stockmanager.stock_manager.service.PurchaseOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseOrderServiceTests {
    @Mock PurchaseOrderRepository orders;
    @Mock SupplierRepository suppliers;
    @Mock ProductRepository products;
    @Mock StockMovementRepository movements;
    @Mock ProductBatchRepository batches;
    PurchaseOrderService service;

    @BeforeEach void setUp() { service = new PurchaseOrderService(orders, suppliers, products, movements, batches); }

    @Test void partialReceiptUpdatesStockBatchAndLedger() {
        Supplier supplier = new Supplier(); supplier.setName("Supplier");
        Product product = new Product(); product.setName("Rice"); product.setQuantity(4.0); product.setIsFractional(false);
        PurchaseOrder order = new PurchaseOrder(); order.setSupplier(supplier); order.setStatus("ORDERED");
        PurchaseOrderItem item = new PurchaseOrderItem(); item.setId(7L); item.setProduct(product);
        item.setQuantityOrdered(10.0); item.setQuantityReceived(0.0); item.setUnitCost(new BigDecimal("2.50"));
        order.addItem(item);
        when(orders.findById(1L)).thenReturn(Optional.of(order));
        when(products.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(batches.save(any(ProductBatch.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(movements.save(any(StockMovement.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orders.save(any(PurchaseOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PurchaseOrderService.ReceiptLineInput receipt = new PurchaseOrderService.ReceiptLineInput();
        receipt.orderItemId = 7L; receipt.quantity = 4.0;
        PurchaseOrder updated = service.receive(1L, List.of(receipt));

        assertEquals(8.0, product.getQuantity());
        assertEquals(4.0, item.getQuantityReceived());
        assertEquals("PARTIALLY_RECEIVED", updated.getStatus());
        verify(batches).save(any(ProductBatch.class));
        verify(movements).save(any(StockMovement.class));
    }

    @Test void receiptCannotExceedRemainingOrderQuantity() {
        Product product = new Product(); product.setName("Milk"); product.setQuantity(0.0); product.setIsFractional(false);
        PurchaseOrder order = new PurchaseOrder(); order.setStatus("ORDERED");
        PurchaseOrderItem item = new PurchaseOrderItem(); item.setId(9L); item.setProduct(product);
        item.setQuantityOrdered(5.0); item.setQuantityReceived(2.0); item.setUnitCost(BigDecimal.ONE); order.addItem(item);
        when(orders.findById(2L)).thenReturn(Optional.of(order));
        PurchaseOrderService.ReceiptLineInput receipt = new PurchaseOrderService.ReceiptLineInput();
        receipt.orderItemId = 9L; receipt.quantity = 4.0;
        assertThrows(IllegalArgumentException.class, () -> service.receive(2L, List.of(receipt)));
        verifyNoInteractions(products, movements, batches);
    }
}
