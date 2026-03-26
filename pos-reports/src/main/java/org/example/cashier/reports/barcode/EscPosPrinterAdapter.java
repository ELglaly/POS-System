package org.example.cashier.reports.barcode;

import com.github.anastaciocintra.escpos.EscPos;
import com.github.anastaciocintra.escpos.EscPosConst;
import com.github.anastaciocintra.escpos.Style;
import com.github.anastaciocintra.escpos.image.*;
import com.github.anastaciocintra.escpos.image.BitonalThreshold;
import lombok.extern.slf4j.Slf4j;
import org.example.cashier.core.entity.Product;
import org.example.cashier.core.enums.LabelTemplate;
import org.example.cashier.core.exception.PrintJobException;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.print.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;

@Component
@Slf4j
public class EscPosPrinterAdapter {

    public void printLabel(byte[] imagePng,
                           Product product,
                           LabelTemplate template,
                           int copies,
                           String printerName) {
        PrintService printService = resolvePrintService(printerName);
        log.info("Printing {} x{} to '{}'", product.getSku(), copies, printService.getName());

        try {
            byte[] escPosData = buildEscPosPayload(imagePng, product, template, copies);
            sendRawBytes(escPosData, printService);
        } catch (IOException ex) {
            throw new PrintJobException(
                    "ESC/POS print failed for " + product.getSku()
                    + " on " + printService.getName(), ex);
        }
    }

    // ── Private ───────────────────────────────────────────────────────────────

    private byte[] buildEscPosPayload(byte[] imagePng,
                                       Product product,
                                       LabelTemplate template,
                                       int copies) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imagePng));
        ByteArrayOutputStream out   = new ByteArrayOutputStream(4096);

        try (EscPos escPos = new EscPos(out)) {

            // Pre-build the image wrapper once — reused for each copy
            EscPosImage escPosImage = new EscPosImage(
                    new CoffeeImageImpl(bufferedImage), new BitonalThreshold());
            RasterBitImageWrapper imageWrapper = new RasterBitImageWrapper();
            imageWrapper.setJustification(EscPosConst.Justification.Center);

            Style centeredBold = new Style()
                    .setBold(true)
                    .setJustification(EscPosConst.Justification.Center);
            Style centered = new Style()
                    .setJustification(EscPosConst.Justification.Center);
            Style centeredLarge = new Style()
                    .setBold(true)
                    .setFontSize(Style.FontSize._2, Style.FontSize._2)
                    .setJustification(EscPosConst.Justification.Center);
            Style small = new Style()
                    .setJustification(EscPosConst.Justification.Center);

            for (int copy = 0; copy < copies; copy++) {

                // Product name header
                if (template != LabelTemplate.COMPACT) {
                    escPos.writeLF(centeredBold, truncate(product.getName(), 24));
                }

                // Price (larger font for SHELF_TAG and FULL)
                if (template == LabelTemplate.SHELF_TAG || template == LabelTemplate.FULL) {
                    escPos.writeLF(centeredLarge,
                            String.format("$%.2f", product.getPrice()));
                } else if (template == LabelTemplate.STANDARD) {
                    escPos.writeLF(centered,
                            String.format("$%.2f", product.getPrice()));
                }

                // Barcode image
                escPos.write(imageWrapper, escPosImage);

                // SKU line (FULL template only)
                if (template == LabelTemplate.FULL) {
                    escPos.writeLF(small, "SKU: " + product.getSku());
                }

                // Cut — partial between copies, full on last
                if (copy < copies - 1) {
                    escPos.cut(EscPos.CutMode.PART);
                } else {
                    escPos.cut(EscPos.CutMode.FULL);
                }
            }
        }

        return out.toByteArray();
    }

    private void sendRawBytes(byte[] data, PrintService service) throws IOException {
        DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
        Doc doc = new SimpleDoc(data, flavor, null);
        DocPrintJob job = service.createPrintJob();
        try {
            job.print(doc, null);
            log.debug("Sent {} bytes to '{}'", data.length, service.getName());
        } catch (PrintException ex) {
            throw new IOException("DocPrintJob failed: " + ex.getMessage(), ex);
        }
    }

    private PrintService resolvePrintService(String printerName) {
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        if (printerName != null && !printerName.isBlank()) {
            return Arrays.stream(services)
                    .filter(s -> s.getName().equalsIgnoreCase(printerName))
                    .findFirst()
                    .orElseGet(() -> {
                        log.warn("Printer '{}' not found — using system default", printerName);
                        return PrintServiceLookup.lookupDefaultPrintService();
                    });
        }
        PrintService def = PrintServiceLookup.lookupDefaultPrintService();
        if (def == null) {
            throw new PrintJobException("No printer found and none specified");
        }
        return def;
    }

    private String truncate(String text, int max) {
        if (text == null) return "";
        return text.length() <= max ? text : text.substring(0, max - 1) + ".";
    }
}
