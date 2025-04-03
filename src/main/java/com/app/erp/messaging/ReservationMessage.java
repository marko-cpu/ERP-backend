package com.app.erp.messaging;


import com.app.erp.entity.Accounting;
import com.app.erp.entity.OrderProduct;

import java.util.List;

public class ReservationMessage {

    private List<OrderProduct> productsList;

    private Accounting accounting;

    public ReservationMessage() {
    }

    public ReservationMessage(List<OrderProduct> productsList, Accounting accounting) {
        this.productsList = productsList;
        this.accounting = accounting;
    }

    public ReservationMessage(Accounting accounting) {
        this.accounting = accounting;
    }

    public List<OrderProduct> getProductsList() {
        return productsList;
    }

    public void setProductsList(List<OrderProduct> productsList) {
        this.productsList = productsList;
    }

    public Accounting getAccounting() {
        return accounting;
    }

    public void setAccounting(Accounting accounting) {
        this.accounting = accounting;
    }
}
