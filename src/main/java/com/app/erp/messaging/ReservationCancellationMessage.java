package com.app.erp.messaging;

public class ReservationCancellationMessage {

    private long orderId;

    public ReservationCancellationMessage() {
    }

    public ReservationCancellationMessage(long orderId) {
        this.orderId = orderId;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }
}
