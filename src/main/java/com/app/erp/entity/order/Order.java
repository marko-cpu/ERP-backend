package com.app.erp.entity.order;


import com.app.erp.entity.Customer;
import com.app.erp.entity.user.User;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @JsonManagedReference
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderProduct> productList;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

//    @ManyToOne
//    @JoinColumn(name = "customer_id")
//    private Customer customer;

    public Order() {
    }

    public Order(long id, User user, Customer customer, List<OrderProduct> productList) {
        this.id = id;
        this.user = user;
        this.customer = customer;
        this.productList = productList;
    }
    public Order(User user, Customer customer, List<OrderProduct> productList) {
        this.user = user;
        this.customer = customer;
        this.productList = productList;
    }


    public Order(long id,User user, List<OrderProduct> productList) {
        this.id = id;
        this.user = user;
        this.productList = productList;
    }


    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public List<OrderProduct> getProductList() {
        return productList;
    }

    public void setProductList(List<OrderProduct> productList) {
        this.productList = productList;
    }

    public Order addProduct(OrderProduct orderProduct)
    {
        this.productList.add(orderProduct);
        return this;
    }
}
