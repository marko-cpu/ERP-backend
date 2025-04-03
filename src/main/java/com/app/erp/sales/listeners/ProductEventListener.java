package com.app.erp.sales.listeners;


import com.app.erp.entity.Product;
import com.app.erp.messaging.ProductEventType;
import com.app.erp.messaging.ProductMessage;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class ProductEventListener implements Serializable {


    public void processProductEvent(ProductMessage event) {

        System.out.print("Module sales receives a message: ");
        if (event.getType().equals(ProductEventType.NEW_PRODUCT)) {
            System.out.println("Event: " + event.getType() +
                    " for product with id: " + event.getProduct().getId());
        } else {
            System.out.print("Event: " + event.getType());
            StringBuilder sb = new StringBuilder();
            for (Product product : event.getProductList())
                sb.append(" for product with id: " ).append(product.getId()).append("\n");
            System.out.println(sb);
        }
    }
}
