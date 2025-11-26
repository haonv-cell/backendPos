package com.example.pos.dto;

import lombok.Data;
import java.util.List;

@Data
public class PrintBarcodeRequest {
    private List<ProductQuantity> products;
    private String paperSize;
    private boolean showStoreName;
    private boolean showProductName;
    private boolean showPrice;
    private boolean showReferenceNumber;
    private String barcodeType; // "barcode" or "qrcode"

    @Data
    public static class ProductQuantity {
        private Integer productId;
        private int quantity;
        private String referenceNumber;
    }
}