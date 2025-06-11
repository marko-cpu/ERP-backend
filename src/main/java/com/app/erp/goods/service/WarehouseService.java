package com.app.erp.goods.service;


import com.app.erp.entity.warehouse.Warehouse;
import com.app.erp.goods.repository.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;

    public WarehouseService(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    public Page<Warehouse> getAllWarehouses(Pageable pageable) {
        return warehouseRepository.findAll(pageable);
    }

    public Optional<Warehouse> getWarehouseById(Long id) {
        return warehouseRepository.findById(id);
    }

    @Transactional
    public Warehouse addToWarehouse(Warehouse warehouse) {
        if (warehouseRepository.findByWarehouseNameAndLocation(
                warehouse.getWarehouseName(),
                warehouse.getLocation()
        ).isPresent()) {
            throw new IllegalStateException("Warehouse with this name and location already exists");
        }
        return warehouseRepository.save(warehouse);
    }

    @Transactional
    public Warehouse updateWarehouse(Long id, Warehouse updatedWarehouse) {
        return warehouseRepository.findById(id).map(warehouse -> {
//            warehouse.setWarehouseId(updatedWarehouse.getWarehouseId());
            warehouse.setWarehouseName(updatedWarehouse.getWarehouseName());
//            warehouse.setProduct(updatedWarehouse.getProduct());
//            warehouse.setQuantity(updatedWarehouse.getQuantity());
            warehouse.setLocation(updatedWarehouse.getLocation());
            return warehouseRepository.save(warehouse);
        }).orElseGet(() -> {
            updatedWarehouse.setId(id);
            return warehouseRepository.save(updatedWarehouse);
        });
    }

    @Transactional
    public void deleteWarehouse(Long id) {
        warehouseRepository.deleteById(id);
    }

}
