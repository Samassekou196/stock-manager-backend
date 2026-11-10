package com.stockmanager.stock_manager;

import com.stockmanager.stock_manager.entity.AppUser;
import com.stockmanager.stock_manager.entity.Category;
import com.stockmanager.stock_manager.entity.Customer;
import com.stockmanager.stock_manager.entity.Product;
import com.stockmanager.stock_manager.entity.Role;
import com.stockmanager.stock_manager.entity.Sale;
import com.stockmanager.stock_manager.entity.Supplier;
import com.stockmanager.stock_manager.repository.AppUserRepository;
import com.stockmanager.stock_manager.repository.CategoryRepository;
import com.stockmanager.stock_manager.repository.CustomerRepository;
import com.stockmanager.stock_manager.repository.ProductRepository;
import com.stockmanager.stock_manager.repository.SupplierRepository;
import com.stockmanager.stock_manager.repository.StockMovementRepository;
import com.stockmanager.stock_manager.service.SaleService;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.mock.web.MockHttpSession;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired AppUserRepository users;
    @Autowired PasswordEncoder encoder;
    @Autowired CategoryRepository categories;
    @Autowired SupplierRepository suppliers;
    @Autowired StockMovementRepository stockMovements;
    @Autowired ProductRepository products;
    @Autowired CustomerRepository customers;
    @Autowired SaleService sales;

    @BeforeEach void seedUsers() {
        users.deleteAll();
        users.save(new AppUser("admin-test", encoder.encode("A-very-long-test-password"), "Admin test", Role.ADMIN));
        users.save(new AppUser("cashier-test", encoder.encode("Another-test-password"), "Cashier test", Role.CASHIER));
    }

    @Test void apiRejectsUnauthenticatedRequests() throws Exception {
        mvc.perform(get("/api/products")).andExpect(status().isUnauthorized());
    }

    @Test void cashierCanReadCatalogAndCreateSalesButCannotManageProducts() throws Exception {
        mvc.perform(get("/api/products").with(user("cashier-test").roles("CASHIER")))
                .andExpect(status().isOk());
        mvc.perform(post("/api/products").with(user("cashier-test").roles("CASHIER")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test void mutationsRequireCsrf() throws Exception {
        mvc.perform(post("/api/sales").with(user("cashier-test").roles("CASHIER"))
                .contentType(MediaType.APPLICATION_JSON).content("[]"))
                .andExpect(status().isForbidden());
    }

    @Test void passwordsAreStoredAsHashes() {
        AppUser admin = users.findByUsername("admin-test").orElseThrow();
        org.junit.jupiter.api.Assertions.assertNotEquals("A-very-long-test-password", admin.getPasswordHash());
        org.junit.jupiter.api.Assertions.assertTrue(encoder.matches("A-very-long-test-password", admin.getPasswordHash()));
    }

    @Test void loginRequiresCsrf() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin-test\",\"password\":\"A-very-long-test-password\"}"))
                .andExpect(status().isForbidden());
    }

    @Test void saleUsesStoredProductPriceAndUpdatesStock() {
        Category category = new Category();
        category.setName("Test category");
        category = categories.save(category);
        Supplier supplier = new Supplier();
        supplier.setName("Test supplier");
        supplier = suppliers.save(supplier);
        Product product = new Product();
        product.setName("Test product");
        product.setPrice(new BigDecimal("10.00"));
        product.setQuantity(10.0);
        product.setIsFractional(false);
        product.setCategory(category);
        product.setSupplier(supplier);
        product = products.save(product);
        Customer customer = new Customer();
        customer.setFirstName("Test");
        customer.setLastName("Customer");
        customer = customers.save(customer);

        SaleService.SaleItemRequest line = new SaleService.SaleItemRequest();
        line.setProductId(product.getId());
        line.setQuantity(2.0);
        line.setUnitPrice(BigDecimal.ONE);
        line.setSubTotal(BigDecimal.ONE);
        Sale sale = sales.create(customer.getId(), java.util.List.of(line));

        org.junit.jupiter.api.Assertions.assertEquals(new BigDecimal("20.00"), sale.getTotalAmount());
        org.junit.jupiter.api.Assertions.assertEquals(new BigDecimal("10.00"), sale.getItems().get(0).getUnitPrice());
        org.junit.jupiter.api.Assertions.assertEquals(8.0, products.findById(product.getId()).orElseThrow().getQuantity());
        org.junit.jupiter.api.Assertions.assertEquals(1, stockMovements.findTop200ByProductIdOrderByCreatedAtDesc(product.getId()).size());

        product = products.findById(product.getId()).orElseThrow();
        product.setWholesaleQuantity(6.0);
        product.setWholesalePrice(new BigDecimal("50.00"));
        products.save(product);
        SaleService.SaleItemRequest wholesale = new SaleService.SaleItemRequest();
        wholesale.setProductId(product.getId());
        wholesale.setQuantity(1.0);
        wholesale.setSaleType("WHOLESALE");
        Sale wholesaleSale = sales.create(customer.getId(), java.util.List.of(wholesale));
        org.junit.jupiter.api.Assertions.assertEquals(new BigDecimal("50.00"), wholesaleSale.getTotalAmount());
        org.junit.jupiter.api.Assertions.assertEquals(2.0, products.findById(product.getId()).orElseThrow().getQuantity());
    }

    @Test void loginCreatesServerSessionAndRejectsInvalidPassword() throws Exception {
        mvc.perform(get("/api/auth/csrf"));
        mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin-test\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized());

        MvcResult login = mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin-test\",\"password\":\"A-very-long-test-password\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.role").value("ADMIN"))
                .andReturn();
        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
        mvc.perform(get("/api/auth/me").session(session)).andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin-test"));
        mvc.perform(post("/api/auth/logout").session(session).with(csrf()))
                .andExpect(status().isOk());
        mvc.perform(get("/api/auth/me").session(session)).andExpect(status().isUnauthorized());
    }
}
