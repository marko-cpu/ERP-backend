package com.app.erp.goods.listeners;

import com.app.erp.entity.reservation.Reservation;
import com.app.erp.goods.repository.ReservationRepository;
import com.app.erp.messaging.ReservationCancellationMessage;
import com.app.erp.user.service.NotificationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CancelReservationListeners {

    private final ReservationRepository reservationRepository;
    private final NotificationService notificationService;

    public CancelReservationListeners(ReservationRepository reservationRepository, NotificationService notificationService) {
        this.reservationRepository = reservationRepository;
        this.notificationService = notificationService;
    }


    @RabbitListener(queues = "cancel-reservation-queue")
    public void cancelReservation(ReservationCancellationMessage reservationCancellationMessage) {
        System.out.println("Module goods receives a message about cancelling a reservation: " + reservationCancellationMessage.getOrderId());

        List<Reservation> reservationList = reservationRepository.findReservationsByOrderId(reservationCancellationMessage.getOrderId());

        if(!reservationList.isEmpty()) {
            for (Reservation reservation : reservationList) {
                reservationRepository.delete(reservation);
            }

            // Notification for canceled reservation
            notificationService.createAndSendNotification(
                    "RESERVATION_CANCELLED",
                    "Cancelled reservation for order ID: " + reservationCancellationMessage.getOrderId(),
                    List.of("SALES_MANAGER", "WAREHOUSE_MANAGER")
            );
        } else {
            notificationService.createAndSendNotification(
                    "RESERVATION_NOT_FOUND",
                    "No reservation found for order ID: " + reservationCancellationMessage.getOrderId(),
                    List.of("SALES_MANAGER")
            );
        }
    }
}

