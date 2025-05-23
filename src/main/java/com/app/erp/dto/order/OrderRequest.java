package com.app.erp.dto.order;

import com.app.erp.dto.customer.CustomerDTO;
import com.app.erp.entity.order.OrderProduct;

import java.util.List;

public class OrderRequest {
    private Integer userId;
    private CustomerDTO customer;
    private List<OrderProduct> products;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }


    public CustomerDTO getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerDTO customer) {
        this.customer = customer;
    }

    public List<OrderProduct> getProducts() {
        return products;
    }

    public void setProducts(List<OrderProduct> products) {
        this.products = products;
    }
}
