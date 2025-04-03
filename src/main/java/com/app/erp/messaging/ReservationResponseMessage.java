package com.app.erp.messaging;


import com.app.erp.entity.Accounting;

public class ReservationResponseMessage {

    private String message;

    private boolean successful;

    private transient Accounting accounting;

    public ReservationResponseMessage() {
    }

    public ReservationResponseMessage(String message, boolean successful, Accounting accounting) {
        this.message = message;
        this.successful = successful;
        this.accounting = accounting;
    }

    public ReservationResponseMessage(String message, boolean successful) {
        this.message = message;
        this.successful = successful;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public Accounting getAccounting() {
        return accounting;
    }

    public void setAccounting(Accounting accounting) {
        this.accounting = accounting;
    }
}
