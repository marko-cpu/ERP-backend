package com.app.erp.goods.service;


import com.app.erp.entity.*;
import com.app.erp.entity.product.Product;
import com.app.erp.entity.warehouse.ArticleWarehouse;
import com.app.erp.entity.warehouse.Warehouse;
import com.app.erp.goods.repository.ArticleWarehouseRepository;
import com.app.erp.goods.repository.ProductRepository;
import com.app.erp.goods.repository.ReservationRepository;
import com.app.erp.messaging.ProductMessage;
import com.app.erp.user.service.NotificationService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.app.erp.config.RabbitMQConfig.PRODUCTS_TOPIC_EXCHANGE_NAME;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final NotificationService notificationService;
    private final RabbitTemplate rabbitTemplate;
    private final ReservationRepository reservationRepository;
    private final ArticleWarehouseRepository articleWarehouseRepository;

    public ProductService(ProductRepository productRepository,
                          NotificationService notificationService,
                          RabbitTemplate rabbitTemplate,
                          ReservationRepository reservationRepository,
                          ArticleWarehouseRepository articleWarehouseRepository) {
        this.productRepository = productRepository;
        this.notificationService = notificationService;
        this.rabbitTemplate = rabbitTemplate;
        this.reservationRepository = reservationRepository;
        this.articleWarehouseRepository = articleWarehouseRepository;
    }


    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    public List<Product> getAllProductsWithoutPagination() {
        return productRepository.findAll();
    }

    public long getProductCount() {
        return productRepository.count();
    }

    public Product addProduct(Product product) {
        product.setSku(generateSku());
        //productRepository.save(product);
        Product savedProduct = productRepository.save(product);

        ProductMessage productEventMessage = ProductMessage.createNewProduct(product);
        rabbitTemplate.convertAndSend(PRODUCTS_TOPIC_EXCHANGE_NAME,
                "product.create", productEventMessage);

        // New notification for the product
        notificationService.createAndSendNotification(
                "PRODUCT_CREATED",
                "New Product Created: " + product.getProductName() + " (SKU: " + product.getSku() + ")",
                List.of("ADMIN", "SALES_MANAGER")
        );


        return savedProduct;
    }

    public String generateSku() {
        long count = productRepository.count();
        return "PROD-" + String.format("%03d", count + 1);
    }


    public void receptionOfProducts(Warehouse warehouse, List<ArticleWarehouse> articles) {
        List<Product> products = new ArrayList<>();

        for (ArticleWarehouse article : articles) {
            article.setWarehouse(warehouse);
            articleWarehouseRepository.save(article);
        }

        // Notification for the warehouse
        notificationService.createAndSendNotification(
                "STOCK_RECEIVED",
                "New stock received in warehouse: " + warehouse.getWarehouseName(),
                List.of("ADMIN", "SALES_MANAGER")
        );

        ProductMessage productEventMessage = ProductMessage.updateStateOfProduct(products);
        rabbitTemplate.convertAndSend(PRODUCTS_TOPIC_EXCHANGE_NAME,
                "product.updateState", productEventMessage);
    }


    public String getProductData(long productId) {

        StringBuilder sb = new StringBuilder();
        Product product = productRepository.findById(productId).orElseThrow();
        sb.append("Product: \n").append(product.getProductName()).append("\n");

        Optional<Integer> quantity = articleWarehouseRepository.findTotalQuantityByProductId(productId);
        Optional<Integer> reservedQuantity = reservationRepository.findTotalReservedQuantityByProductId(productId);
        int totalQauntity = reservedQuantity.map(integer -> quantity.get() - integer).orElseGet(quantity::get);
        sb.append("Total quantity: ").append(totalQauntity).append("\n");

        List<ArticleWarehouse> articleWarehouses = articleWarehouseRepository.findByProductId(productId);
        for (ArticleWarehouse article : articleWarehouses) {
            Warehouse warehouse = article.getWarehouse();
            sb.append("Price: ").append(article.getPurchasePrice())
                    .append(", name: ").append(warehouse.getWarehouseName())
                    .append(", location: ").append(warehouse.getLocation()).append("\n");
        }
        return sb.toString();
    }

    public String getProductState(long productId) {
        StringBuilder sb = new StringBuilder();
        sb.append("WarehouseID | quantity ");
        List<Object[]> result = articleWarehouseRepository.findQuantityForProductIdGroupByWarehouse(productId);

        for (Object[] o : result) {
            sb.append(o[0]).append(" | ").append(o[1]).append("\n");
        }
        return sb.toString();
    }

    public Map<String, Integer> getProductCountByCategory() {
        List<Object[]> results = productRepository.countProductsByCategory();
        Map<String, Integer> categoryCounts = new LinkedHashMap<>();

        results.forEach(result -> {
            String categoryName = (String) result[0];
            Integer count = ((Number) result[1]).intValue();
            categoryCounts.put(categoryName, count);
        });

        return categoryCounts;
    }

    public List<Category> getAllCategories() {
        return Arrays.asList(Category.values());
    }


    @Transactional
    public void deleteProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        productRepository.deleteById(id);

        notificationService.createAndSendNotification(
                "PRODUCT_DELETED",
                "Deleted product: " + product.getProductName(),
                List.of("ADMIN", "SALES_MANAGER")
        );



    }


}



