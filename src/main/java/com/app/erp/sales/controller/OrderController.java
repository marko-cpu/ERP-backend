package com.app.erp.sales.controller;

import com.app.erp.entity.Customer;
import com.app.erp.entity.Order;
import com.app.erp.entity.OrderRequest;
import com.app.erp.sales.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("api/orders")
@PreAuthorize("hasAnyAuthority('ADMIN', 'SALES_MANAGER','ACCOUNTANT')")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/customers")
    public List<Customer> getAllCustomers() {
        return orderService.getAllCustomers();
    }

    @GetMapping("/customers/search")
    public List<Customer> searchCustomers(@RequestParam String query) {
        return orderService.searchCustomers(query);
    }

    @GetMapping("/customers/check")
    public ResponseEntity<?> checkCustomerExists(@RequestParam String email) {
        try {
            Customer customer = orderService.checkCustomerExists(email);
            return ResponseEntity.ok().body(Map.of(
                    "exists", customer != null,
                    "customer", customer
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error checking customer: " + e.getMessage());
        }
    }

    @GetMapping("order-count")
    public long getOrderCount() {
        return orderService.getOrderCount();
    }

    @GetMapping("/getOrders")
    public Page<Order> getAllOrders(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "5") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return orderService.getAllOrders(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        Optional<Order> order = orderService.getOrderById(id);
        return order.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/CreateOrders")
    public ResponseEntity<String> createOrder(@RequestBody OrderRequest orderRequest) {

        try {
            orderService.createOrder(orderRequest);
            return ResponseEntity.ok("Order in progress");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error adding order: " + e.getMessage());

        }

    }

    @PostMapping("/pay")
    public ResponseEntity<String> addInvoice(@RequestBody Map<String, Object> requestBody) {
        try {
            Number totalPriceNumber = (Number) requestBody.get("totalPrice");
            Number accountingIdNumber = (Number) requestBody.get("accounting_id");

            if(totalPriceNumber == null || accountingIdNumber == null) {
                return ResponseEntity.badRequest().body("Missing required parameters");
            }

            double totalPrice = totalPriceNumber.doubleValue();
            long accountingId = accountingIdNumber.longValue();

            orderService.addInvoice(accountingId, totalPrice);
            return ResponseEntity.ok("Invoice added");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error adding invoice: " + e.getMessage());
        }
    }



}
