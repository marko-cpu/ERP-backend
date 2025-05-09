package com.app.erp.goods.controller;


import com.app.erp.entity.warehouse.ArticleWarehouse;
import com.app.erp.goods.service.ArticleWarehouseService;
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
@RequestMapping("/api/article-warehouse")
@PreAuthorize("hasAnyAuthority('ADMIN', 'INVENTORY_MANAGER', 'SALES_MANAGER')")
public class ArticleWarehouseController {

    @Autowired
    private ArticleWarehouseService articleWarehouseService;


    @PostMapping
    public ResponseEntity<ArticleWarehouse> createArticleWarehouse(@RequestBody ArticleWarehouse articleWarehouse) {
        ArticleWarehouse savedArticleWarehouse = articleWarehouseService.saveArticleWarehouse(articleWarehouse);
        return ResponseEntity.ok(savedArticleWarehouse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArticleWarehouse> getArticleWarehouseById(@PathVariable Long id) {
        Optional<ArticleWarehouse> articleWarehouse = articleWarehouseService.getArticleWarehouseById(id);
        return articleWarehouse.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/warehouse/{warehouseId}")
    public ResponseEntity<Page<ArticleWarehouse>> getArticlesByWarehouse(
            @PathVariable Long warehouseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ArticleWarehouse> result = articleWarehouseService.getArticlesByWarehouse(warehouseId, pageable);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ArticleWarehouse>> getWarehousesByProduct(@PathVariable Long productId) {
        List<ArticleWarehouse> warehouses = articleWarehouseService.getWarehousesByProduct(productId);
        return ResponseEntity.ok(warehouses);
    }

    @GetMapping
    public ResponseEntity<Page<ArticleWarehouse>> getAllArticleWarehouses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ArticleWarehouse> result = articleWarehouseService.getAllArticleWarehouses(pageable);

        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArticleWarehouseById(@PathVariable Long id) {
        articleWarehouseService.deleteArticleWarehouseById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/update/{articleId}")
    public ResponseEntity<String> updateArticleWarehouse(
            @PathVariable Long articleId,
            @RequestBody Map<String, Object> requestBody) {
        try {
            if (requestBody.containsKey("quantity") && requestBody.containsKey("purchasePrice")) {
                Integer quantity = (Integer) requestBody.get("quantity");
                Double purchasePrice = ((Number) requestBody.get("purchasePrice")).doubleValue();

                articleWarehouseService.updateArticleWarehouse(articleId, quantity, purchasePrice);
                return new ResponseEntity<>("Article updated successfully", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Invalid request body", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to update article: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


//        @PutMapping("/{productId}")
//        public ResponseEntity<String> updateProductPrice(@PathVariable long productId, @RequestBody Map<String, Double> requestBody) {
//            try {
//                if (requestBody.containsKey("purchasePrice")) {
//                    double newPrice = requestBody.get("purchasePrice");
//                    articleWarehouseService.updatePurchasePrice(productId, newPrice);
//                    return new ResponseEntity<>("Product price updated successfully", HttpStatus.OK);
//                } else {
//                    return new ResponseEntity<>("Invalid request body", HttpStatus.BAD_REQUEST);
//                }
//            } catch (Exception e) {
//                return new ResponseEntity<>("Failed to update product price: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//            }
//        }
}
