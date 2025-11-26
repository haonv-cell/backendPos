package com.example.pos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BarcodeResponse {
    private Integer productId;
    private String productName;
    private String productSku;
    private String productPrice;
    private String storeName;
    private String referenceNumber;
    private String barcodeBase64;
}