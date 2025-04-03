package com.app.erp.goods.listeners;


import com.app.erp.entity.Reservation;
import com.app.erp.goods.repository.ReservationRepository;
import com.app.erp.messaging.ReservationCancellationMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CancelReservationListeners {

    @Autowired
    ReservationRepository reservationRepository;


    public void cancelReservation(ReservationCancellationMessage reservationCancellationMessage) {

        System.out.println("Module goods receives a message about cancelling a reservation: " + reservationCancellationMessage.getOrderId());

        List<Reservation> reservationList = reservationRepository.findReservationsByOrderId(reservationCancellationMessage.getOrderId());

        if(!reservationList.isEmpty()) {
            for (Reservation reservation : reservationList) {
                reservationRepository.delete(reservation);
            }
        }


    }

}
