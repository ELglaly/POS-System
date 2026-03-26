package org.example.cashier.reports.barcode;

import com.google.zxing.*;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.oned.EAN13Writer;
import lombok.extern.slf4j.Slf4j;
import org.example.cashier.core.constants.BarcodeConstants;
import org.example.cashier.core.enums.BarcodeType;
import org.example.cashier.core.exception.BarcodeGenerationException;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;


@Component
@Slf4j
public class ZXingBarcodeEngine {


    public byte[] generatePng(String value, BarcodeType type, int widthPx, int heightPx) {
        try {
            BitMatrix matrix = encode(value, type, widthPx, heightPx);
            ByteArrayOutputStream baos = new ByteArrayOutputStream(8192);
            MatrixToImageWriter.writeToStream(matrix, "PNG", baos);
            return baos.toByteArray();
        } catch (WriterException | IOException ex) {
            throw new BarcodeGenerationException(
                    "Failed to generate " + type + " barcode for value: " + value, ex);
        }
    }
    public byte[] generateProductLabel(String sku, String productName) {
        try {
            int width  = 400;
            int barH   = 90;
            int lineH  = 22;
            int lines  = 2;         // name, sku
            int padY   = 8;         // top padding above text block
            int totalH = barH + padY + (lines * lineH) + 8;

            BitMatrix matrix = encodeCode128(sku, width, barH);
            BufferedImage barcodeImg = MatrixToImageWriter.toBufferedImage(matrix);

            BufferedImage label = new BufferedImage(width, totalH, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = label.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                               RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, totalH);

            // Barcode
            g.drawImage(barcodeImg, 0, 0, null);

            // Text area
            int y = barH + padY;

            // Line 1 — product name (bold, 13pt)
            g.setFont(new Font("SansSerif", Font.BOLD, 13));
            g.setColor(Color.BLACK);
            g.drawString(truncate(productName, 42), centerX(g, truncate(productName, 42), width), y + 13);
            y += lineH;

            // Line 2 — SKU (plain, 11pt, gray)
            g.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g.setColor(new Color(80, 80, 80));
            String skuLine = "SKU: " + sku;
            g.drawString(skuLine, centerX(g, skuLine, width), y + 11);
            y += lineH;

            g.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream(16384);
            ImageIO.write(label, "PNG", baos);
            return baos.toByteArray();
        } catch (WriterException | IOException ex) {
            throw new BarcodeGenerationException(
                    "Failed to generate product label for SKU: " + sku, ex);
        }
    }


    public byte[] generateUserBarcodeLabel(String barcodePin, String displayName) {
        try {
            int width  = 400;
            int barH   = 90;
            int lineH  = 22;
            int lines  = 2;     // name, pin
            int padY   = 8;
            int totalH = barH + padY + (lines * lineH) + 8;

            BitMatrix matrix = encodeCode128(barcodePin, width, barH);
            BufferedImage barcodeImg = MatrixToImageWriter.toBufferedImage(matrix);

            BufferedImage label = new BufferedImage(width, totalH, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = label.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                               RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, totalH);

            g.drawImage(barcodeImg, 0, 0, null);

            int y = barH + padY;

            // Line 1 — display name (bold, 13pt)
            g.setFont(new Font("SansSerif", Font.BOLD, 13));
            g.setColor(Color.BLACK);
            g.drawString(truncate(displayName, 42), centerX(g, truncate(displayName, 42), width), y + 13);
            y += lineH;

            // Line 2 — PIN (plain, 11pt, gray)
            g.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g.setColor(new Color(80, 80, 80));
            String pinLine = "PIN: " + barcodePin;
            g.drawString(pinLine, centerX(g, pinLine, width), y + 11);

            g.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream(16384);
            ImageIO.write(label, "PNG", baos);
            return baos.toByteArray();
        } catch (WriterException | IOException ex) {
            throw new BarcodeGenerationException(
                    "Failed to generate user badge label for: " + displayName, ex);
        }
    }

    // ── Private encoding ──────────────────────────────────────────────────────

    private BitMatrix encode(String value, BarcodeType type, int width, int height)
            throws WriterException {
        return switch (type) {
            case CODE128 -> encodeCode128(value, width, height);
            case EAN_13  -> encodeEan13(value, width, height);
        };
    }

    private BitMatrix encodeCode128(String value, int width, int height)
            throws WriterException {
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.MARGIN, BarcodeConstants.QUIET_ZONE_MODULES);
        return new Code128Writer().encode(value, BarcodeFormat.CODE_128, width, height, hints);
    }

    private BitMatrix encodeEan13(String value, int width, int height)
            throws WriterException {
        if (value.length() != BarcodeConstants.EAN13_PAYLOAD_LENGTH) {
            throw new BarcodeGenerationException(
                    "EAN-13 requires exactly " + BarcodeConstants.EAN13_PAYLOAD_LENGTH
                    + " digits, got: " + value.length());
        }
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.MARGIN, BarcodeConstants.QUIET_ZONE_MODULES);
        return new EAN13Writer().encode(value, BarcodeFormat.EAN_13, width, height, hints);
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() <= maxLen ? text : text.substring(0, maxLen - 1) + "…";
    }

    private int centerX(Graphics2D g, String text, int imageWidth) {
        int textWidth = g.getFontMetrics().stringWidth(text);
        return Math.max(4, (imageWidth - textWidth) / 2);
    }
}
