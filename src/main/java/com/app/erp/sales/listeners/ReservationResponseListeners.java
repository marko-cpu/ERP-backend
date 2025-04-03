package com.app.erp.sales.listeners;


import com.app.erp.messaging.ReservationResponseMessage;
import com.app.erp.sales.repository.AccountingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReservationResponseListeners {

    @Autowired
    AccountingRepository accountingRepository;

    public void processReservationResponse(ReservationResponseMessage response) {

            if (response.isSuccessful()) {
                accountingRepository.save(response.getAccounting());
                System.out.println("Module Sales receives a message: " + response.getMessage());

            } else {
                System.out.println(response.getMessage());
            }
    }

}
