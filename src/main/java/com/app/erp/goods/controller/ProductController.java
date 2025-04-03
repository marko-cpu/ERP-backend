package com.app.erp.goods.controller;


import com.app.erp.entity.ArticleWarehouse;
import com.app.erp.entity.Category;
import com.app.erp.entity.Product;
import com.app.erp.entity.Warehouse;
import com.app.erp.goods.repository.ArticleWarehouseRepository;
import com.app.erp.goods.repository.ProductRepository;
import com.app.erp.goods.repository.WarehouseRepository;
import com.app.erp.goods.service.ArticleWarehouseService;
import com.app.erp.goods.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/product")
@PreAuthorize("hasAnyAuthority('ADMIN', 'INVENTORY_MANAGER','ACCOUNTANT', 'SALES_MANAGER')")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private ArticleWarehouseService articleWarehouseService;

    @Autowired
    private ArticleWarehouseRepository articleWarehouseRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;


    @GetMapping("/category-stats")
    public Map<String, Integer> getProductCategoryStats() {
        return productService.getProductCountByCategory();
    }


    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(productService.getAllCategories());
    }

    @GetMapping("product-count")
    public long getProductCount() {
        return productService.getProductCount();
    }

    @GetMapping("/products")
    public Page<Product> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productService.getAllProducts(pageable);
    }
    @GetMapping("/products/all")
    public List<Product> getAllProductsWithoutPagination() {
          return productService.getAllProductsWithoutPagination();
       }

    @PostMapping("/products/add")
    public Product addProduct(@RequestBody Product product) {
        return productService.addProduct(product);
    }

@PutMapping("/products/update/{id}")
public Product updateProduct(@PathVariable Long id, @RequestBody Product productDetails) {
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Производ није пронађен"));
    
    product.setSku(productDetails.getSku());
    product.setProductName(productDetails.getProductName());
    product.setMeasureUnit(productDetails.getMeasureUnit());
    product.setCategory(productDetails.getCategory());
    product.setDescription(productDetails.getDescription());
    product.setPrice(productDetails.getPrice());
    
    return productRepository.save(product);
}


    @PostMapping("/products/addToWarehouse")
    public ResponseEntity<String> updateState(@RequestBody Map<String, Object> requestBody) {
        try {
            String warehouseName = (String) requestBody.get("warehouseName");
            String location = (String) requestBody.get("location");
            List<Map<String, Object>> articleList = (List<Map<String, Object>>) requestBody.get("articles");

            // Pronađi ili kreiraj skladište
            Warehouse warehouse = warehouseRepository.findByWarehouseNameAndLocation(warehouseName, location)
                    .orElseGet(() -> warehouseRepository.save(new Warehouse(warehouseName, location)));

            List<ArticleWarehouse> articles = new ArrayList<>();

            for (Map<String, Object> articleMap : articleList) {
                Map<String, Object> productMap = (Map<String, Object>) articleMap.get("product");

                long productId = ((Number) productMap.get("id")).longValue();
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new NoSuchElementException("Product not found"));

                double purchasePrice = ((Number) articleMap.get("purchasePrice")).doubleValue();
                int quantity = ((Number) articleMap.get("quantity")).intValue();

                // Pronađi ili kreiraj ArticleWarehouse
                ArticleWarehouse articleWarehouse = articleWarehouseRepository
                        .findArticleWarehouse(product, purchasePrice)
                        .orElse(new ArticleWarehouse(product, purchasePrice, 0));

                articleWarehouse.setQuantity(articleWarehouse.getQuantity() + quantity);
                articleWarehouse.setWarehouse(warehouse);

                articleWarehouseRepository.save(articleWarehouse);
                articles.add(articleWarehouse);
            }

            productService.receptionOfProducts(warehouse, articles);
            return ResponseEntity.ok("Products successfully added to warehouse");

        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error adding products: " + e.getMessage());
        }
    }



    @GetMapping("/productData/{productId}")
    public ResponseEntity<String> getProductData(@PathVariable("productId") long productId) {
        try{
            return ResponseEntity.ok(productService.getProductData(productId));
        }catch (NoSuchElementException e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error getting product data: " + e.getMessage());
        }
    }

    @GetMapping("/productState/{productId}")
    public ResponseEntity<String> getProductState(@PathVariable("productId") long productId) {
        try{
            return ResponseEntity.ok(productService.getProductState(productId));
        }catch (NoSuchElementException e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error getting product state: " + e.getMessage());
        }
    }
}
