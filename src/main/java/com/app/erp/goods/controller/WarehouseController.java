package com.app.erp.goods.controller;


import com.app.erp.entity.warehouse.Warehouse;
import com.app.erp.goods.service.WarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/warehouses")
@PreAuthorize("hasAnyAuthority('ADMIN', 'INVENTORY_MANAGER', 'SALES_MANAGER')")
public class WarehouseController {

    @Autowired
    private WarehouseService warehouseService;

    @GetMapping("getAllWarehouses")
    public Page<Warehouse> getAllWarehouses(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "5") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return warehouseService.getAllWarehouses(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Warehouse> getWarehouseById(@PathVariable Long id) {
        Optional<Warehouse> warehouse = warehouseService.getWarehouseById(id);
        return warehouse.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("addWarehouse")
    public ResponseEntity<?> addToWarehouse(@RequestBody Warehouse warehouse) {
        try {
            warehouseService.addToWarehouse(warehouse);
            return ResponseEntity.ok("Products successfully added to warehouse");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteWarehouse(@PathVariable Long id) {
        warehouseService.deleteWarehouse(id);
        return ResponseEntity.ok("" + id + " deleted successfully");
    }
}
