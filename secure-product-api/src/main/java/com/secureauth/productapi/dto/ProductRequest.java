package com.secureauth.productapi.dto;

import lombok.Data;

@Data
public class ProductRequest {
    private String name;
    private String description;
    private Double price;
    private String imageUrl;
}
