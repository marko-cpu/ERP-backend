package com.app.erp.goods.listeners;



import com.app.erp.entity.Accounting;
import com.app.erp.entity.OrderProduct;
import com.app.erp.entity.Product;
import com.app.erp.entity.Reservation;
import com.app.erp.goods.repository.ArticleWarehouseRepository;
import com.app.erp.goods.repository.ReservationRepository;
import com.app.erp.goods.repository.WarehouseRepository;
import com.app.erp.messaging.ReservationMessage;
import com.app.erp.messaging.ReservationResponseMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

import static com.app.erp.config.RabbitMQConfig.PRODUCTS_TOPIC_EXCHANGE_NAME;


@Component
public class ReservationListeners {


    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    WarehouseRepository warehouseRepository;

    @Autowired
    private ArticleWarehouseRepository articleWarehouseRepository;

    @Autowired
    RabbitTemplate rabbitTemplate;

    public void processReservation(ReservationMessage reservationMessage) {

        ReservationResponseMessage response = new ReservationResponseMessage();
        Accounting accounting = reservationMessage.getAccounting();
        boolean successful = true;

        for(OrderProduct orderProduct : reservationMessage.getProductsList()) {

            Product product = orderProduct.getProduct();
            Optional<Integer> quantityOptional = articleWarehouseRepository.findTotalQuantityByProductId(product.getId());
            Optional<Integer> reservedQuantityOptional = reservationRepository.findTotalReservedQuantityByProductId(product.getId());

            int quantity = quantityOptional.orElse(0);
            int reservedQuantity = reservedQuantityOptional.orElse(0);

            int totalQuantity = quantity - reservedQuantity;
            int requestedQuantity = orderProduct.getQuantity();

            if(totalQuantity < requestedQuantity) {
                successful = false;
                response.setSuccessful(false);
                response.setMessage("Couldn't make a reservation for product with id: " + product.getId());
                rabbitTemplate.convertAndSend(PRODUCTS_TOPIC_EXCHANGE_NAME, "reservationresponse.queue", response);
            }
        }

        if(successful) {
            for(OrderProduct product : reservationMessage.getProductsList()) {
                Reservation reservation = new Reservation(product.getProduct(), product.getQuantity(), accounting.getOrder(), LocalDate.now(), "pending");
                reservationRepository.save(reservation);
            }
            response.setSuccessful(true);
            response.setMessage("Reservation successful!");
            response.setAccounting(accounting);
            rabbitTemplate.convertAndSend(PRODUCTS_TOPIC_EXCHANGE_NAME, "reservationresponse.queue", response);
        }
    }

}
