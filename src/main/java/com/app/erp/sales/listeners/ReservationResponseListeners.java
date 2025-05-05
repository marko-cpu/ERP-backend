package com.app.erp.sales.listeners;

import com.app.erp.messaging.ReservationResponseMessage;
import com.app.erp.sales.repository.AccountingRepository;
import com.app.erp.user.service.NotificationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReservationResponseListeners {

    @Autowired
    AccountingRepository accountingRepository;

    @Autowired
    private NotificationService notificationService;

    @RabbitListener(queues = "reservation-response-queue")
    public void processReservationResponse(ReservationResponseMessage response) {
//        if (response.isSuccessful()) {
//            accountingRepository.save(response.getAccounting());
//            notificationService.createAndSendNotification(
//                    "RESERVATION_CONFIRMED",
//                    "Reservation successful: " + response.getMessage(),
//                    List.of("SALES_MANAGER")
//            );
//        } else {
//            notificationService.createAndSendNotification(
//                    "RESERVATION_ERROR",
//                    "Couldn't make a reservation for product with not enough quantity in warehouse. Product ID: " + product.getId(),
//                    List.of("SALES_MANAGER")
//            );
//        }

    }
}
