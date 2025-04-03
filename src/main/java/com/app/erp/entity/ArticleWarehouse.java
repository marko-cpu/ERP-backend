package com.app.erp.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;


@Entity
@Table(name = "article_warehouse")
public class ArticleWarehouse implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "article_id")
    private Long id;


    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "purchase_price", nullable = false)
    private double purchasePrice;


    public ArticleWarehouse() {
    }

    public ArticleWarehouse(Product product, double purchasePrices, int quantity) {
        this.product = product;
        this.purchasePrice = purchasePrices;
        this.quantity = quantity;
    }

    public ArticleWarehouse(Product product) {
        this.product = product;

    }



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(double purchasePrice) {
        BigDecimal roundedPrice = BigDecimal.valueOf(purchasePrice).setScale(2, RoundingMode.HALF_UP);
        this.purchasePrice = roundedPrice.doubleValue();
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArticleWarehouse that = (ArticleWarehouse) o;
        return Double.compare(that.purchasePrice, purchasePrice) == 0 &&
                quantity == that.quantity &&
                Objects.equals(id, that.id) &&
                Objects.equals(product, that.product) &&
                Objects.equals(warehouse, that.warehouse);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, product, purchasePrice, quantity, warehouse);
    }

    @Override
    public String toString() {
        return "ArticleWarehouse{" +
                "id=" + id +
                ", product=" + product +
                ", purchasePrice=" + purchasePrice +
                ", quantity=" + quantity +
                '}';
    }
}