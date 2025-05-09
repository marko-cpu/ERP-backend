package com.app.erp.sales.listeners;

import com.app.erp.messaging.ProductMessage;
import com.app.erp.user.service.NotificationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

@Component
public class ProductEventListener implements Serializable {

    private final NotificationService notificationService;

    @Autowired
    public ProductEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = "products-queue")
    public void processProductEvent(ProductMessage event) {

//        try {
//            if (event.getProduct() == null) throw new IllegalArgumentException("Null product");
//            System.out.println("Processing product: " + event.getProduct().getId());
//
//        } catch (Exception e) {
//            System.err.println("Error processing message: " + e.getMessage());
//        }

        System.out.println("Module sales receives a message: " + event.getType());
    }
}

