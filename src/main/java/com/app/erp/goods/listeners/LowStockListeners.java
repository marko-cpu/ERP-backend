package com.app.erp.goods.listeners;

import com.app.erp.entity.product.Product;
import com.app.erp.entity.warehouse.ArticleWarehouse;
import com.app.erp.goods.service.ArticleWarehouseService;
import com.app.erp.messaging.ProductMessage;
import com.app.erp.user.service.NotificationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LowStockListeners {

    private final ArticleWarehouseService articleWarehouseService;
    private final NotificationService notificationService;

    public LowStockListeners(ArticleWarehouseService articleWarehouseService, NotificationService notificationService) {
        this.articleWarehouseService = articleWarehouseService;
        this.notificationService = notificationService;
    }


    @RabbitListener(queues = "low-stock-queue")
    public void handleLowStockEvent(ProductMessage message) {
        Product product = message.getProduct();

        List<ArticleWarehouse> articles = articleWarehouseService.getWarehousesByProduct(product.getId());

        for (ArticleWarehouse article : articles) {
            if (article.getQuantity() < 10) {
                String content = String.format(
                        "Low stock: %s in %s - only %d units left",
                        product.getProductName(),
                        article.getWarehouse().getWarehouseName(),
                        article.getQuantity()
                );

                notificationService.createAndSendNotification(
                        "LOW_STOCK",
                        content,
                        List.of("ADMIN", "INVENTORY_MANAGER")
                );
            }
        }
    }

}