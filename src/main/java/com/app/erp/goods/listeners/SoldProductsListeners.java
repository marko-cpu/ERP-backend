package com.app.erp.goods.listeners;



import com.app.erp.entity.ArticleWarehouse;
import com.app.erp.entity.OrderProduct;
import com.app.erp.goods.repository.ArticleWarehouseRepository;
import com.app.erp.goods.repository.ReservationRepository;
import com.app.erp.goods.repository.WarehouseRepository;
import com.app.erp.messaging.SoldProductMessage;
import com.app.erp.sales.repository.OrderProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SoldProductsListeners {


    @Autowired
    OrderProductRepository orderProductRepository;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    WarehouseRepository warehouseRepository;

    @Autowired
    private ArticleWarehouseRepository articleWarehouseRepository;

    public void processSoldProductsMessage(SoldProductMessage soldProductMessage) {
        StringBuilder sb = new StringBuilder();
        sb.append("Module goods receives a message about selling products: \n");

        List<OrderProduct> orderProductsList = orderProductRepository.findOrderProducts(soldProductMessage.getOrderId());

        for (OrderProduct orderProduct : orderProductsList) {
            sb.append("Product ID: ").append(orderProduct.getProduct().getId())
                    .append(", Quantity: ").append(orderProduct.getQuantity()).append("\n");
        }
        for (OrderProduct orderProducts : orderProductsList) {
            List<Long> reservationIdOptional = reservationRepository.findReservationId(orderProducts.getProduct().getId(), orderProducts.getQuantity());
            List<ArticleWarehouse> wareHouseStateList = articleWarehouseRepository.findStateOfWarehousesForProductId(orderProducts.getProduct().getId());
            int remaining = orderProducts.getQuantity();

            if (!wareHouseStateList.isEmpty()) {
                for (ArticleWarehouse warehouseState : wareHouseStateList) {
                    int warehouseQuantity = warehouseState.getQuantity();
                    int taken = 0;
                    if (remaining <= 0) break;
                    int remainingWarehouseQuantity = warehouseQuantity - remaining;
                    if (remainingWarehouseQuantity <= 0) {
                        taken = warehouseQuantity;
                        articleWarehouseRepository.delete(warehouseState);
                    } else {
                        taken = remaining;
                        warehouseState.setQuantity(remainingWarehouseQuantity);
                        articleWarehouseRepository.save(warehouseState);
                    }
                    remaining -= taken;
                }
            }
            if (!reservationIdOptional.isEmpty()) {
                for (Long reservationId : reservationIdOptional) {
                    reservationRepository.deleteById(reservationId);
                }
            }
        }
        System.out.println(sb);
    }
}
