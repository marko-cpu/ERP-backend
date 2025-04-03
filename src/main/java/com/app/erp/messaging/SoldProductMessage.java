package com.app.erp.messaging;

public class SoldProductMessage {

    private long orderId;

    public SoldProductMessage() {
    }

    public SoldProductMessage(long orderId) {
        this.orderId = orderId;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }
}
