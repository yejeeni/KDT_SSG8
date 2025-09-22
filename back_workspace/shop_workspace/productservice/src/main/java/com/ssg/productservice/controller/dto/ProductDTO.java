package com.ssg.productservice.controller.dto;

import lombok.Data;

@Data
public class ProductDTO {
    private int productId;
    private String productName;
    private String brand;
    private int price;
    private int discount;
    private String detail;
    private SubCategoryDTO subCategoryDTO;
}
