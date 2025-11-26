package com.example.pos.controller;

import com.example.pos.dto.BarcodeResponse;
import com.example.pos.dto.PrintBarcodeRequest;
import com.example.pos.service.BarcodeService;
import com.google.zxing.WriterException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/barcodes")
@RequiredArgsConstructor
public class BarcodeController {

    private final BarcodeService barcodeService;

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN','STORE_OWNER')")
    public ResponseEntity<List<BarcodeResponse>> generateBarcodes(@RequestBody PrintBarcodeRequest request) {
        try {
            List<BarcodeResponse> barcodes = barcodeService.generateBarcodes(request);
            return ResponseEntity.ok(barcodes);
        } catch (WriterException | IOException e) {
            // Log the exception
            return ResponseEntity.internalServerError().build();
        }
    }
}