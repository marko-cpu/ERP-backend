package com.app.erp.goods.repository;


import com.app.erp.entity.warehouse.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {


//    @Query("SELECT w FROM  Warehouse w WHERE w.product.product.id= :productId")
//    List<Warehouse> findStateOfWarehousesForProductId(@Param("productId") Long productId);

    Optional<Warehouse> findByWarehouseNameAndLocation(String warehouseName, String location);

//    @Query("SELECT SUM(w.quantity) FROM Warehouse w JOIN w.product aw WHERE aw.product.id = :productId")
//    Optional<Integer> findTotalQuantityByProductId(@Param("productId") Long productId);

//    @Query("SELECT w.warehouseId, SUM(w.quantity) FROM Warehouse w JOIN w.product aw WHERE aw.product.id = :productId GROUP BY w.warehouseId")
//    List<Object[]> findQuantityForProductIdGroupByWarehouse(@Param("productId") Long productId);


}

