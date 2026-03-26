package org.example.cashier.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cashier.core.constants.BarcodeConstants;
import org.example.cashier.core.dto.BarcodeDTO;
import org.example.cashier.core.dto.BarcodeGenerationRequestDTO;
import org.example.cashier.core.entity.Barcode;
import org.example.cashier.core.entity.Product;
import org.example.cashier.core.enums.BarcodeType;
import org.example.cashier.core.exception.BarcodeGenerationException;
import org.example.cashier.core.exception.ProductNotFoundException;
import org.example.cashier.core.mapper.BarcodeMapper;
import org.example.cashier.data.repository.BarcodeRepository;
import org.example.cashier.data.repository.ProductRepository;
import org.example.cashier.reports.barcode.ZXingBarcodeEngine;
import org.example.cashier.services.BarcodeGenerationService;
import org.example.cashier.services.event.BarcodeGeneratedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BarcodeGenerationServiceImpl implements BarcodeGenerationService {

    private final ProductRepository     productRepository;
    private final BarcodeRepository     barcodeRepository;
    private final ZXingBarcodeEngine    barcodeEngine;
    private final BarcodeMapper         barcodeMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public BarcodeDTO generate(BarcodeGenerationRequestDTO request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(request.getProductId()));

        if (request.isDeactivatePrevious()) {
            int deactivated = barcodeRepository.deactivateByProductIdAndType(
                    product.getId(), request.getBarcodeType());
            if (deactivated > 0) {
                log.debug("Deactivated {} existing {} barcode(s) for product {}",
                        deactivated, request.getBarcodeType(), product.getSku());
            }
        }

        String barcodeValue = computeBarcodeValue(product, request.getBarcodeType());

        byte[] imagePng = barcodeEngine.generatePng(
                barcodeValue,
                request.getBarcodeType(),
                defaultWidth(request.getBarcodeType()),
                defaultHeight(request.getBarcodeType()));

        Barcode barcode = Barcode.builder()
                .product(product)
                .barcodeValue(barcodeValue)
                .barcodeType(request.getBarcodeType())
                .generatedBy(request.getOperatorUsername())
                .active(true)
                .build();

        barcode = barcodeRepository.save(barcode);
        log.info("Generated {} barcode '{}' for product {} by {}",
                request.getBarcodeType(), barcodeValue, product.getSku(),
                request.getOperatorUsername());

        BarcodeDTO dto = barcodeMapper.toDto(barcode);
        dto.setRenderedImagePng(imagePng);

        eventPublisher.publishEvent(new BarcodeGeneratedEvent(this, dto));
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BarcodeDTO> findActiveByProduct(Long productId) {
        return barcodeMapper.toDtoList(
                barcodeRepository.findByProductIdAndActiveTrueOrderByGeneratedAtDesc(productId));
    }

    @Override
    public byte[] renderImagePng(String barcodeValue, BarcodeType type, int widthPx, int heightPx) {
        return barcodeEngine.generatePng(barcodeValue, type, widthPx, heightPx);
    }

    @Override
    public String computeInternalBarcodeValue(Long productId, String categoryCode) {
        int checkDigit = computeCheckDigit(productId);
        return String.format(BarcodeConstants.INTERNAL_FORMAT,
                categoryCode.toUpperCase(), productId, checkDigit);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private String computeBarcodeValue(Product product, BarcodeType type) {
        return switch (type) {
            case CODE128 -> {
                String categoryCode = product.getCategory() != null
                        ? product.getCategory().getCode() : "XX";
                yield computeInternalBarcodeValue(product.getId(), categoryCode);
            }
            case EAN_13  -> buildEan13Value(product);
        };
    }

    private String buildEan13Value(Product product) {
        long categoryHash = product.getCategory() != null
                ? Math.abs(product.getCategory().getName().hashCode()) % 1_000_000L
                : 0L;
        String base = String.format("%01d%06d%05d",
                0, categoryHash, product.getId() % 100_000L);
        String candidate = base;
        int suffix = 0;
        while (barcodeRepository.existsByBarcodeValue(candidate)) {
            suffix++;
            candidate = String.format("%01d%06d%04d%01d",
                    0, categoryHash, product.getId() % 10_000L, suffix % 10);
        }
        return candidate;
    }

    private int computeCheckDigit(Long productId) {
        int sum = 0;
        long id = productId;
        int multiplier = 1;
        while (id > 0) {
            sum += (id % 10) * multiplier;
            id /= 10;
            multiplier = (multiplier == 1) ? 3 : 1;
        }
        return (10 - (sum % 10)) % 10;
    }

    private int defaultWidth(BarcodeType type) {
        return switch (type) {
            case CODE128 -> BarcodeConstants.CODE128_DEFAULT_WIDTH_PX;
            case EAN_13  -> BarcodeConstants.EAN13_DEFAULT_WIDTH_PX;
        };
    }

    private int defaultHeight(BarcodeType type) {
        return switch (type) {
            case CODE128 -> BarcodeConstants.CODE128_DEFAULT_HEIGHT_PX;
            case EAN_13  -> BarcodeConstants.EAN13_DEFAULT_HEIGHT_PX;
        };
    }
}
