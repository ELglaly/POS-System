package org.example.cashier.services;

import org.example.cashier.core.entity.Product;
import org.example.cashier.data.repository.ProductRepository;
import org.example.cashier.reports.barcode.ZXingBarcodeEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CashierProductService")
class CashierProductServiceTest {

    @Mock ProductRepository  productRepository;
    @Mock ZXingBarcodeEngine barcodeEngine;

    @InjectMocks CashierProductService productService;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = Product.builder()
                .id(1L).name("Coffee").sku("P-CF0001")
                .price(new BigDecimal("3.50")).stockQuantity(100)
                .active(true).build();
    }

    // ── findBySku ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("findBySku")
    class FindBySku {

        @Test
        @DisplayName("returns product when active SKU matches")
        void foundActiveSku() {
            when(productRepository.findBySkuAndActiveTrue("P-CF0001"))
                    .thenReturn(Optional.of(sampleProduct));

            assertThat(productService.findBySku("P-CF0001"))
                    .isPresent()
                    .contains(sampleProduct);
        }

        @Test
        @DisplayName("returns empty for unknown SKU")
        void unknownSku() {
            when(productRepository.findBySkuAndActiveTrue(anyString()))
                    .thenReturn(Optional.empty());

            assertThat(productService.findBySku("P-XXXXX")).isEmpty();
        }
    }

    // ── search ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("search")
    class Search {

        @Test
        @DisplayName("calls searchActive for a non-blank query")
        void delegatesToSearchActive() {
            when(productRepository.searchActive("coffee")).thenReturn(List.of(sampleProduct));

            List<Product> results = productService.search("coffee");

            assertThat(results).containsExactly(sampleProduct);
            verify(productRepository).searchActive("coffee");
        }

        @Test
        @DisplayName("returns all products when query is blank")
        void blankQueryReturnsAll() {
            when(productRepository.findAllByActiveTrueOrderByName())
                    .thenReturn(List.of(sampleProduct));

            assertThat(productService.search("")).containsExactly(sampleProduct);
            verify(productRepository).findAllByActiveTrueOrderByName();
        }

        @Test
        @DisplayName("returns all products when query is null")
        void nullQueryReturnsAll() {
            when(productRepository.findAllByActiveTrueOrderByName())
                    .thenReturn(List.of(sampleProduct));

            assertThat(productService.search(null)).containsExactly(sampleProduct);
        }
    }

    // ── generateSku ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("generateSku")
    class GenerateSku {

        @Test
        @DisplayName("returns a SKU matching the P-XXXXXX pattern")
        void matchesPattern() {
            when(productRepository.findBySkuAndActiveTrue(anyString()))
                    .thenReturn(Optional.empty());

            String sku = productService.generateSku();

            assertThat(sku).matches("P-[A-Z0-9]{6}");
        }

        @Test
        @DisplayName("retries until a unique SKU is found")
        void retriesOnCollision() {
            // First two calls return a conflict; third is free
            when(productRepository.findBySkuAndActiveTrue(anyString()))
                    .thenReturn(Optional.of(sampleProduct))
                    .thenReturn(Optional.of(sampleProduct))
                    .thenReturn(Optional.empty());

            String sku = productService.generateSku();

            assertThat(sku).matches("P-[A-Z0-9]{6}");
            verify(productRepository, times(3)).findBySkuAndActiveTrue(anyString());
        }

        @Test
        @DisplayName("returned SKU starts with P-")
        void hasPPrefix() {
            when(productRepository.findBySkuAndActiveTrue(anyString()))
                    .thenReturn(Optional.empty());

            assertThat(productService.generateSku()).startsWith("P-");
        }
    }

    // ── save ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("save delegates to repository and returns the persisted entity")
    void save() {
        when(productRepository.save(sampleProduct)).thenReturn(sampleProduct);

        Product saved = productService.save(sampleProduct);

        assertThat(saved).isSameAs(sampleProduct);
        verify(productRepository).save(sampleProduct);
    }

    // ── delete ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("delete delegates deleteById to repository")
    void delete() {
        productService.delete(1L);

        verify(productRepository).deleteById(1L);
    }

    // ── getBarcodeLabel ───────────────────────────────────────────────────

    @Test
    @DisplayName("getBarcodeLabel delegates to ZXingBarcodeEngine with sku and name")
    void getBarcodeLabel() {
        byte[] expected = new byte[]{1, 2, 3};
        when(barcodeEngine.generateProductLabel("P-CF0001", "Coffee")).thenReturn(expected);

        byte[] result = productService.getBarcodeLabel(sampleProduct);

        assertThat(result).isSameAs(expected);
    }
}
