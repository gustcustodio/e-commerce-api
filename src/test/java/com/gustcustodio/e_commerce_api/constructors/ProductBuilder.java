package com.gustcustodio.e_commerce_api.constructors;

import java.lang.Long;
import java.lang.Integer;
import java.lang.Double;
import java.lang.String;

import com.gustcustodio.e_commerce_api.entities.Product;


public class ProductBuilder {
    private Product element;

    private ProductBuilder() {
    }

    public static ProductBuilder oneProduct() {
        ProductBuilder builder = new ProductBuilder();
        initializeDefaultData(builder);
        return builder;
    }

    public static void initializeDefaultData(ProductBuilder builder) {
        builder.element = new Product();
        Product element = builder.element;


        element.setId(1L);
        element.setName("Valid Product");
        element.setDescription("Valid Product Description");
        element.setPrice(100.0);
        element.setQuantity(10);
    }

    public ProductBuilder withId(Long param) {
        element.setId(param);
        return this;
    }

    public ProductBuilder withName(String param) {
        element.setName(param);
        return this;
    }

    public ProductBuilder withDescription(String param) {
        element.setDescription(param);
        return this;
    }

    public ProductBuilder withPrice(Double param) {
        element.setPrice(param);
        return this;
    }

    public ProductBuilder withQuantity(Integer param) {
        element.setQuantity(param);
        return this;
    }

    public Product build() {
        return element;
    }

}

