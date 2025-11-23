package com.app.erp.goods.service;


import com.app.erp.audit.AuditService;
import com.app.erp.entity.warehouse.ArticleWarehouse;
import com.app.erp.entity.product.Product;
import com.app.erp.goods.repository.ArticleWarehouseRepository;
import com.app.erp.messaging.ProductMessage;
import com.app.erp.user.service.NotificationService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.app.erp.config.RabbitMQConfig.PRODUCTS_TOPIC_EXCHANGE_NAME;

@Service
public class ArticleWarehouseService {

    private final ArticleWarehouseRepository articleWarehouseRepository;
    private final NotificationService notificationService;
    private final RabbitTemplate rabbitTemplate;
    private final AuditService auditService;

    @Value("${app.low-stock.threshold}")
    private int lowStockThreshold;

    public ArticleWarehouseService(ArticleWarehouseRepository articleWarehouseRepository,
                                   NotificationService notificationService,
                                   RabbitTemplate rabbitTemplate, AuditService auditService) {
        this.articleWarehouseRepository = articleWarehouseRepository;
        this.notificationService = notificationService;
        this.rabbitTemplate = rabbitTemplate;
        this.auditService = auditService;
    }


    @Transactional
    public ArticleWarehouse saveArticleWarehouse(ArticleWarehouse articleWarehouse) {
        return articleWarehouseRepository.save(articleWarehouse);
    }

    @Transactional(readOnly = true)
    public Page<ArticleWarehouse> getArticlesByWarehouse(Long warehouseId, Pageable pageable) {
        return articleWarehouseRepository.findByWarehouseId(warehouseId, pageable);
    }

    @Transactional(readOnly = true)
    public List<ArticleWarehouse> getWarehousesByProduct(Long productId) {
        return articleWarehouseRepository.findByProductId(productId);
    }

    @Transactional(readOnly = true)
    public Optional<ArticleWarehouse> getArticleWarehouseById(Long id) {
        return articleWarehouseRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Page<ArticleWarehouse> getAllArticleWarehouses(Pageable pageable) {
        return articleWarehouseRepository.findAll(pageable);
    }

    @Transactional
    public void deleteArticleWarehouseById(Long id) {
        articleWarehouseRepository.deleteById(id);

    }

    @Transactional
    public void updateArticleWarehouse(Long articleId, Integer quantity, Double purchasePrice) {
        ArticleWarehouse article = articleWarehouseRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article not found with id: " + articleId));

        int oldQuantity = article.getQuantity();
        double oldPurchasePrice = article.getPurchasePrice();

        article.setQuantity(quantity);
        article.setPurchasePrice(purchasePrice);
        articleWarehouseRepository.save(article);

        checkAndNotifyLowStock(article);

        Product product = article.getProduct();
        List<Product> updatedProducts = Collections.singletonList(product);

        ProductMessage message = ProductMessage.updateStateOfProduct(updatedProducts);
        rabbitTemplate.convertAndSend(
                PRODUCTS_TOPIC_EXCHANGE_NAME,
                "product.updateState",
                message
        );

        notificationService.createAndSendNotification(
                "ARTICLE_UPDATED",
                String.format("Updated article: Quantity: %d - %s - Purchase price: (€%.2f)",
                        quantity, product.getProductName(), purchasePrice),
                List.of("SALES_MANAGER")
        );

        Map<String, Object> details = new HashMap<>();
        details.put("productId", article.getProduct().getId());
        details.put("productName", article.getProduct().getProductName());
        details.put("warehouseId", article.getWarehouse().getId());
        details.put("warehouseName", article.getWarehouse().getWarehouseName());
        details.put("oldQuantity", oldQuantity);
        details.put("newQuantity", quantity);
        details.put("oldPurchasePrice", oldPurchasePrice);
        details.put("newPurchasePrice", purchasePrice);

        auditService.logEvent(
                "ARTICLE_WAREHOUSE_UPDATE",
                "ARTICLE_WAREHOUSE",
                articleId,
                details
        );

    }

    public void checkAndNotifyLowStock(ArticleWarehouse article) {
        if (article.getQuantity() < lowStockThreshold) {
            Product product = article.getProduct();

            ProductMessage lowStockMessage = ProductMessage.lowStockAlert(product);
            rabbitTemplate.convertAndSend(
                    PRODUCTS_TOPIC_EXCHANGE_NAME,
                    "product.lowstock",
                    lowStockMessage
            );

        }
    }

//    @Transactional
//    public void updatePurchasePrice(long productId, double newPrice) {
//        ArticleWarehouse articleWarehouse = articleWarehouseRepository.findById(productId)
//                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
//        articleWarehouse.setPurchasePrice(newPrice);
//        articleWarehouseRepository.save(articleWarehouse);
//
//        List<Product> updatedProducts = Collections.singletonList(articleWarehouse.getProduct());
//
//
//        ProductMessage productEventMessage = ProductMessage.updatePriceOfProduct(updatedProducts);
//        rabbitTemplate.convertAndSend(PRODUCTS_TOPIC_EXCHANGE_NAME,
//                "product.updatePrice",productEventMessage);
//
//        // Notifikacija za promenu cene
//        notificationService.createAndSendNotification(
//                "PRICE_UPDATED",
//                "Ažurirana nabavna cena za proizvod ID: " + productId,
//                List.of("INVENTORY_MANAGER", "ACCOUNTING")
//        );
    //   }


}
