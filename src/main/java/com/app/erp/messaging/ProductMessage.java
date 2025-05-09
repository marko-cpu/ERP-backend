package com.app.erp.messaging;

import com.app.erp.entity.product.Product;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ProductMessage implements Serializable {

    private ProductEventType type = ProductEventType.NONE;
    private List<Product> productList = new ArrayList<>(); // Initialize with empty list
    private Product product;

    // Constructors
    public ProductMessage() {
        this.productList = new ArrayList<>();
    }

    public ProductMessage(ProductEventType type, List<Product> productList) {
        this.type = type;
        this.productList = (productList != null) ? productList : new ArrayList<>();
    }

    public ProductMessage(ProductEventType type, Product product) {
        this.type = type;
        this.product = product;
        this.productList = new ArrayList<>();
        if (product != null) {
            this.productList.add(product);
        }
    }

    // Factory methods
    public static ProductMessage createNewProduct(Product product) {
        ProductMessage message = new ProductMessage();
        message.setType(ProductEventType.NEW_PRODUCT);
        message.setProduct(product);
        if (product != null) {
            message.getProductList().add(product);
        }
        return message;
    }

    public static ProductMessage updateStateOfProduct(List<Product> updatedProducts) {
        return new ProductMessage(
                ProductEventType.UPDATE_PRODUCT_STATE,
                (updatedProducts != null) ? updatedProducts : new ArrayList<>()
        );
    }

    public static ProductMessage updatePriceOfProduct(List<Product> updatedProducts) {
        return new ProductMessage(
                ProductEventType.UPDATE_PRODUCT_PRICE,
                (updatedProducts != null) ? updatedProducts : new ArrayList<>()
        );
    }

    // toString() with null safety
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder()
                .append("ProductMessage: ")
                .append("Type: ").append(type);

        if (product != null) {
            sb.append("\nSingle Product: ").append(product);
        }

        if (productList != null && !productList.isEmpty()) {
            sb.append("\nProduct List:");
            productList.forEach(p -> sb.append("\n- ").append(p));
        } else {
            sb.append("\nNo products in list");
        }

        return sb.toString();
    }

    // Getters and setters
    public ProductEventType getType() {
        return type;
    }

    public void setType(ProductEventType type) {
        this.type = type;
    }

    public List<Product> getProductList() {
        return productList;
    }

    public void setProductList(List<Product> productList) {
        this.productList = (productList != null) ? productList : new ArrayList<>();
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
        if (product != null && !this.productList.contains(product)) {
            this.productList.add(product);
        }
    }
}