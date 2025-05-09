package com.app.erp.entity.order;


import com.app.erp.entity.product.Product;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "order_products")
public class OrderProduct implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "order_id")
    @JsonBackReference
    private Order order;

    @Column(name = "price_per_unit")
    private double pricePerUnit;

    @Column(name = "pdv")
    private double pdv; // Porez na dodatu vrednost (VAT)
    private double pdvRate; // Stopa PDV-a u procentima
    @Column(name = "total_price", nullable = false)
    private double totalPrice;
    @Column(name = "quantity", nullable = false)
    private int quantity;

    public OrderProduct(Product product, int quantity) {
        this.product = product;
        this.pricePerUnit = product.getPrice();
        this.quantity = quantity;
        updateTotalPrice();
    }

    public OrderProduct(Order order,Product product, int quantity) {
        this.order = order;
        this.product = product;
        this.pricePerUnit = product.getPrice();
        this.quantity = quantity;
        updateTotalPrice();
    }

    public OrderProduct() {
    }



    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
        this.pricePerUnit = product.getPrice(); // Update price when product changes
        updateTotalPrice();
    }

    public double getPricePerUnit() {
        return pricePerUnit;
    }

    public void setPricePerUnit(double pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
        this.pdv = calculatePdv();
        updateTotalPrice();
    }

    public double getPdv() {
        return pdv;
    }

    public void setPdv(double pdv) {
        this.pdv = pdv;
        updateTotalPrice();
    }
    public double getPdvRate() {
        return pdvRate;
    }

    public void setPdvRate(double pdvRate) {
        this.pdvRate = pdvRate;
        
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        updateTotalPrice();
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        BigDecimal roundedPrice = BigDecimal.valueOf(totalPrice).setScale(2, RoundingMode.HALF_UP);
        this.totalPrice = roundedPrice.doubleValue();
    }

    public double calculatePdv() {
        double pdv = (pricePerUnit * pdvRate / 100) * quantity;
        BigDecimal roundedPdv = BigDecimal.valueOf(pdv).setScale(2, RoundingMode.HALF_UP);
        return roundedPdv.doubleValue();
    }


    private void updateTotalPrice() {
        double pdvAmount = calculatePdv();
        this.totalPrice = (pricePerUnit * quantity) + pdvAmount;
        BigDecimal roundedPrice = BigDecimal.valueOf(totalPrice).setScale(2, RoundingMode.HALF_UP);
        this.totalPrice = roundedPrice.doubleValue();
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }
}

