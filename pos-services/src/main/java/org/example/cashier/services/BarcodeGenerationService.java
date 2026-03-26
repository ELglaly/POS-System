package org.example.cashier.services;

import org.example.cashier.core.dto.BarcodeDTO;
import org.example.cashier.core.dto.BarcodeGenerationRequestDTO;
import org.example.cashier.core.enums.BarcodeType;

import java.util.List;

public interface BarcodeGenerationService {

    BarcodeDTO generate(BarcodeGenerationRequestDTO request);

    List<BarcodeDTO> findActiveByProduct(Long productId);
     byte[] renderImagePng(String barcodeValue, BarcodeType type, int widthPx, int heightPx);

    String computeInternalBarcodeValue(Long productId, String categoryCode);
}
