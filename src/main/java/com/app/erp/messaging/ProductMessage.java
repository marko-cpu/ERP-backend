package com.app.erp.messaging;



import com.app.erp.entity.Product;

import java.io.Serializable;
import java.util.List;

public class ProductMessage implements Serializable {

    ProductEventType type = ProductEventType.NONE;

    List<Product> productList;

    Product product;

    public ProductMessage() {
    }

    public ProductMessage(ProductEventType type, List<Product> productList) {
        this.type = type;
        this.productList = productList;
    }

    public ProductMessage(ProductEventType type, Product product) {
        this.type = type;
        this.product = product;
    }



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
        this.productList = productList;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }


    public static ProductMessage createNewProduct(Product product) {
        ProductMessage productMessage = new ProductMessage(ProductEventType.NEW_PRODUCT, product);
        return productMessage;
    }

    public static ProductMessage updateStateOfProduct(List<Product> updatedProducts) {
        ProductMessage productMessage = new ProductMessage(ProductEventType.UPDATE_PRODUCT_STATE, updatedProducts);
        return productMessage;
    }

    public static ProductMessage updatePriceOfProduct(List<Product> updatedProducts) {
        ProductMessage productMessage = new ProductMessage(ProductEventType.UPDATE_PRODUCT_PRICE, updatedProducts);
        return productMessage;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ProductMessage: ").append("Type: ").append(type).append("Product:\n");

        if(productList.size() != 0) {
            productList.forEach(p -> sb.append(p).append(" "));
        }

        return sb.toString();
    }
}
