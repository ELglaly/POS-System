package org.example.cashier.reports.barcode;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.*;
import lombok.extern.slf4j.Slf4j;
import org.example.cashier.core.entity.Product;
import org.example.cashier.core.enums.LabelTemplate;
import org.example.cashier.core.exception.PrintJobException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;

@Component
@Slf4j
public class PdfLabelExporter {

    private static final float PT_PER_MM = 72f / 25.4f;
    public Path export(byte[] imagePng,
                       Product product,
                       LabelTemplate template,
                       int copies,
                       Path outputPath) {
        float widthPt  = template.getWidthMm()  * PT_PER_MM;
        float heightPt = template.getHeightMm() * PT_PER_MM;
        PageSize pageSize = new PageSize(widthPt, heightPt);

        try (PdfWriter writer   = new PdfWriter(outputPath.toFile());
             PdfDocument pdf    = new PdfDocument(writer);
             Document document  = new Document(pdf, pageSize)) {

            document.setMargins(4, 4, 4, 4);

            for (int i = 0; i < copies; i++) {
                if (i > 0) document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
                renderLabel(document, imagePng, product, template);
            }

            log.info("Exported {} x {} copy/copies to PDF: {}", product.getSku(), copies, outputPath);

        } catch (IOException ex) {
            throw new PrintJobException("PDF export failed: " + ex.getMessage(), ex);
        }

        return outputPath;
    }

    private void renderLabel(Document document,
                             byte[] imagePng,
                             Product product,
                             LabelTemplate template) throws IOException {
        // Product name
        if (template != LabelTemplate.COMPACT) {
            Paragraph name = new Paragraph(product.getName())
                    .setFontSize(8f)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(2f);
            document.add(name);
        }

        // Barcode image — scale to fit the label width minus margins
        float imageMaxWidthPt = (template.getWidthMm() - 8) * PT_PER_MM;
        com.itextpdf.layout.element.Image barcodeImage =
                new com.itextpdf.layout.element.Image(ImageDataFactory.create(imagePng))
                        .setMaxWidth(imageMaxWidthPt)
                        .setHorizontalAlignment(HorizontalAlignment.CENTER);
        document.add(barcodeImage);

        // Price
        if (template != LabelTemplate.COMPACT) {
            Paragraph price = new Paragraph(String.format("$%.2f", product.getPrice()))
                    .setFontSize(template == LabelTemplate.SHELF_TAG ? 14f : 9f)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(2f);
            document.add(price);
        }

        // SKU line for FULL template
        if (template == LabelTemplate.FULL) {
            Paragraph sku = new Paragraph("SKU: " + product.getSku())
                    .setFontSize(6f)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(1f);
            document.add(sku);
        }
    }
}
