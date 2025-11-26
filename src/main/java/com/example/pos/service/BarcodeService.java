package com.example.pos.service;

import com.example.pos.dto.BarcodeResponse;
import com.example.pos.dto.PrintBarcodeRequest;
import com.example.pos.entity.Product;
import com.example.pos.entity.Store;
import com.example.pos.exception.ResourceNotFoundException;
import com.example.pos.repository.ProductRepository;
import com.example.pos.repository.StoreRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BarcodeService {

    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;

    public List<BarcodeResponse> generateBarcodes(PrintBarcodeRequest request) throws WriterException, IOException {
        List<BarcodeResponse> barcodes = new ArrayList<>();
        for (PrintBarcodeRequest.ProductQuantity pq : request.getProducts()) {
            Product product = productRepository.findById(pq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", pq.getProductId()));

            Store store = null;
            if (product.getStoreId() != null) {
                store = storeRepository.findById(product.getStoreId()).orElse(null);
            }

            for (int i = 0; i < pq.getQuantity(); i++) {
                String barcodeData = product.getSku();
                if (pq.getReferenceNumber() != null && !pq.getReferenceNumber().isEmpty()) {
                    barcodeData += "-" + pq.getReferenceNumber();
                }

                BufferedImage image = "qrcode".equalsIgnoreCase(request.getBarcodeType()) ?
                        generateQRCodeImage(barcodeData) :
                        generateBarcodeImage(barcodeData);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, "PNG", baos);
                String base64Image = Base64.getEncoder().encodeToString(baos.toByteArray());

                barcodes.add(new BarcodeResponse(
                        product.getId(),
                        request.isShowProductName() ? product.getName() : null,
                        product.getSku(),
                        request.isShowPrice() && product.getPrice() != null ? product.getPrice().toString() : null,
                        request.isShowStoreName() && store != null ? store.getName() : null,
                        request.isShowReferenceNumber() ? pq.getReferenceNumber() : null,
                        base64Image
                ));
            }
        }
        return barcodes;
    }

    private BufferedImage generateBarcodeImage(String barcodeText) throws WriterException {
        Code128Writer barcodeWriter = new Code128Writer();
        BitMatrix bitMatrix = barcodeWriter.encode(barcodeText, BarcodeFormat.CODE_128, 300, 150, Map.of(EncodeHintType.MARGIN, 1));
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    private BufferedImage generateQRCodeImage(String barcodeText) throws WriterException {
        QRCodeWriter barcodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = barcodeWriter.encode(barcodeText, BarcodeFormat.QR_CODE, 200, 200, Map.of(EncodeHintType.MARGIN, 1));
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }
}