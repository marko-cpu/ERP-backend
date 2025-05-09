package com.app.erp.goods.repository;



import com.app.erp.entity.warehouse.ArticleWarehouse;
import com.app.erp.entity.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface ArticleWarehouseRepository extends JpaRepository<ArticleWarehouse,Long> {

@Query("SELECT aw FROM ArticleWarehouse aw WHERE aw.warehouse.id = :warehouseId")
List<ArticleWarehouse> findByWarehouseId(@Param("warehouseId") Long warehouseId);

    @Query("SELECT aw from ArticleWarehouse aw WHERE aw.product=:productId AND aw.purchasePrice=:purchasePrice")
    Optional<ArticleWarehouse> findArticleWarehouse(@Param("productId") Product productId, @Param("purchasePrice") double purchasePrice);

    @Query("SELECT aw FROM ArticleWarehouse aw WHERE aw.product.id = :productId")
    List<ArticleWarehouse> findStateOfWarehousesForProductId(@Param("productId") Long productId);


    @Query("SELECT SUM(aw.quantity) FROM ArticleWarehouse aw WHERE aw.product.id = :productId")
    Optional<Integer> findTotalQuantityByProductId(@Param("productId") Long productId);

    @Query("SELECT aw.warehouse.id, SUM(aw.quantity) FROM ArticleWarehouse aw WHERE aw.product.id = :productId GROUP BY aw.warehouse.id")
    List<Object[]> findQuantityForProductIdGroupByWarehouse(@Param("productId") Long productId);

    @Query("SELECT aw FROM ArticleWarehouse aw WHERE aw.product.id = :productId")
    List<ArticleWarehouse> findByProductId(@Param("productId") Long productId);

    @Query("SELECT aw FROM ArticleWarehouse aw WHERE aw.warehouse.id = :warehouseId")
    Page<ArticleWarehouse> findByWarehouseId(@Param("warehouseId") Long warehouseId, Pageable pageable);

    List<ArticleWarehouse> findByProductIdOrderByPurchasePriceAsc(Long productId);
   


}
