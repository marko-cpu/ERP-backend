package com.app.erp.entity;


import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "invoices")
public class Invoice implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "accounting_id")
    private Accounting accounting;
    @Column(name = "total_price")
    private Double totalPrice;
    @Column(name = "pay_date")
    private LocalDate payDate;
    @Column(name = "invoice_number", unique = true)
    private String invoiceNumber;


    public Invoice() {
    }

    public Invoice(Accounting accounting, Double totalPrice, LocalDate payDate) {
        this.accounting = accounting;
        this.totalPrice = totalPrice;
        this.payDate = payDate;
        this.invoiceNumber = generateInvoiceNumber();
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        BigDecimal roundedPrice = BigDecimal.valueOf(totalPrice).setScale(2, RoundingMode.HALF_UP);
        this.totalPrice = roundedPrice.doubleValue();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Accounting getAccounting() {
        return accounting;
    }

    public void setAccounting(Accounting accounting) {
        this.accounting = accounting;
    }

    public LocalDate getPayDate() {
        return payDate;
    }

    public void setPayDate(LocalDate payDate) {
        this.payDate = payDate;
    }

    private String generateInvoiceNumber() {
        return "INV-" + System.currentTimeMillis() % 1000000;
    }

    public String getInvoiceNumber() {
      return invoiceNumber;
    }
}
