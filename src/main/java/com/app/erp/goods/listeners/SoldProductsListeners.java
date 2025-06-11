package com.app.erp.goods.listeners;

import com.app.erp.entity.warehouse.ArticleWarehouse;
import com.app.erp.entity.order.OrderProduct;
import com.app.erp.entity.reservation.Reservation;
import com.app.erp.goods.repository.ArticleWarehouseRepository;
import com.app.erp.goods.repository.ReservationRepository;
import com.app.erp.goods.repository.WarehouseRepository;
import com.app.erp.goods.service.ArticleWarehouseService;
import com.app.erp.messaging.SoldProductMessage;
import com.app.erp.sales.repository.OrderProductRepository;
import com.app.erp.user.service.NotificationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SoldProductsListeners {

    private final OrderProductRepository orderProductRepository;
    private final ReservationRepository reservationRepository;
    private final ArticleWarehouseRepository articleWarehouseRepository;
    private final NotificationService notificationService;
    private final ArticleWarehouseService articleWarehouseService;

    public SoldProductsListeners(OrderProductRepository orderProductRepository,
                                 ReservationRepository reservationRepository,
                                 ArticleWarehouseRepository articleWarehouseRepository,
                                 NotificationService notificationService,
                                 ArticleWarehouseService articleWarehouseService) {
        this.orderProductRepository = orderProductRepository;
        this.reservationRepository = reservationRepository;
        this.articleWarehouseRepository = articleWarehouseRepository;
        this.notificationService = notificationService;
        this.articleWarehouseService = articleWarehouseService;
    }

    @RabbitListener(queues = "sold-products-queue")
    public void processSoldProductsMessage(SoldProductMessage soldProductMessage) {
        try {
            Long orderId = soldProductMessage.getOrderId();
            List<OrderProduct> orderProducts = orderProductRepository.findOrderProducts(orderId);

            StringBuilder sb = new StringBuilder();
            sb.append("Processing sold products for order ID: ").append(orderId).append("\n");

            for (OrderProduct orderProduct : orderProducts) {
                processProductStock(orderProduct, sb);
            }

            // Delete all reservations for the order
            List<Reservation> reservations = reservationRepository.findByOrderId(orderId);
            reservations.forEach(reservation ->
                    reservationRepository.delete(reservation));

            notificationService.createAndSendNotification(
                    "PRODUCTS_SOLD",
                    "Successfully processed sales for order: " + orderId,
                    List.of("INVENTORY_MANAGER", "SALES_MANAGER")
            );

        } catch (Exception e) {
            notificationService.createAndSendNotification(
                    "SALE_ERROR",
                    "Error processing sale for order: " + soldProductMessage.getOrderId(),
                    List.of("ADMIN")
            );
            throw new RuntimeException("Error processing sale", e);
        }
    }

    private void processProductStock(OrderProduct orderProduct, StringBuilder sb) {
        Long productId = orderProduct.getProduct().getId();
        int quantityNeeded = orderProduct.getQuantity();

        sb.append("\nProduct ID: ").append(productId)
                .append(", Quantity: ").append(quantityNeeded);

        List<ArticleWarehouse> warehouses = articleWarehouseRepository
                .findByProductIdOrderByPurchasePriceAsc(productId);

        if (warehouses.isEmpty()) {
            throw new IllegalStateException("No stock available for product: " + productId);
        }

        for (ArticleWarehouse warehouse : warehouses) {
            if (quantityNeeded <= 0) break;

            int available = warehouse.getQuantity();
            int deducted = Math.min(available, quantityNeeded);

            warehouse.setQuantity(available - deducted);
            articleWarehouseRepository.save(warehouse);

            quantityNeeded -= deducted;

            sb.append("\n - Warehouse ID: ").append(warehouse.getWarehouse().getId())
                    .append(", Deducted: ").append(deducted)
                    .append(", Remaining: ").append(warehouse.getQuantity());

            articleWarehouseService.checkAndNotifyLowStock(warehouse);
        }

        if (quantityNeeded > 0) {
            throw new IllegalStateException("Insufficient stock for product: " + productId);
        }
    }
}
