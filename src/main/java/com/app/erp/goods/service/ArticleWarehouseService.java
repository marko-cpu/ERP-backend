package com.app.erp.goods.service;


import com.app.erp.entity.ArticleWarehouse;
import com.app.erp.entity.Product;
import com.app.erp.goods.repository.ArticleWarehouseRepository;
import com.app.erp.messaging.ProductMessage;
import com.app.erp.user.service.NotificationService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.app.erp.config.RabbitMQConfig.PRODUCTS_TOPIC_EXCHANGE_NAME;

@Service
public class ArticleWarehouseService {


    @Autowired
    private ArticleWarehouseRepository articleWarehouseRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private  RabbitTemplate rabbitTemplate;

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
        
        article.setQuantity(quantity);
        article.setPurchasePrice(purchasePrice);
        articleWarehouseRepository.save(article);

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
            List.of("ADMIN","SALES_MANAGER")
        );
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
