package com.example.nursery.service;

import com.example.nursery.model.Order;
import com.example.nursery.model.OrderItem;
import com.example.nursery.model.Product;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

// iText 5
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

// ZXing for QR
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;

@Service
public class InvoiceService {

    @Value("${app.bills.folder:./bills}")
    private String billsFolder;

    @Value("${app.company.name:GreenHouse Nursery}")
    private String companyName;

    @Value("${app.company.address:123 Garden Road, Plant City}")
    private String companyAddress;

    @Value("${app.company.phone:+1-800-PLANTS}")
    private String companyPhone;

    @Value("${app.company.email:info@greenhouse.example}")
    private String companyEmail;

    @Value("${app.currency.locale:}")
    private String currencyLocaleTag;

    @Value("${app.base-url:http://localhost:8080}")
    private String appBaseUrl;

    @Value("${app.invoice.tax-percent:0.0}")
    private double taxPercent;

    private static final String CUSTOM_FONT_CLASSPATH = "static/fonts/CustomFont.ttf";
    private static final String LOGO_PNG_CLASSPATH   = "static/images/leaf-logo.png";

    public String generateInvoicePdf(Order order) throws Exception {
        File folder = new File(billsFolder);
        if (!folder.exists() && !folder.mkdirs()) {
            throw new IOException("Could not create bills folder: " + folder.getAbsolutePath());
        }

        String filename = "order-" + order.getId() + ".pdf";
        File pdfFile = new File(folder, filename);

        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(
                (currencyLocaleTag == null || currencyLocaleTag.isBlank())
                        ? Locale.getDefault()
                        : Locale.forLanguageTag(currencyLocaleTag)
        );

        Document document = new Document(PageSize.A4, 36, 36, 72, 36);
        try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
            PdfWriter.getInstance(document, fos);
            document.open();

            // Base fonts
            Font titleFont      = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new BaseColor(17, 122, 55));
            Font subtitleFont   = FontFactory.getFont(FontFactory.HELVETICA, 11, BaseColor.DARK_GRAY);
            Font tableHeaderFont= FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.WHITE);
            Font tableCellFont  = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
            Font totalFont      = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);

            // Try embed custom TTF font (optional)
            try {
                ClassPathResource fontRes = new ClassPathResource(CUSTOM_FONT_CLASSPATH);
                if (fontRes.exists()) {
                    File tmpFont = File.createTempFile("customfont", ".ttf");
                    try (InputStream is = fontRes.getInputStream();
                         FileOutputStream out = new FileOutputStream(tmpFont)) {
                        byte[] buf = new byte[8192];
                        int r;
                        while ((r = is.read(buf)) != -1) out.write(buf, 0, r);
                    }
                    BaseFont bf = BaseFont.createFont(tmpFont.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                    titleFont       = new Font(bf, 18, Font.BOLD, new BaseColor(17, 122, 55));
                    subtitleFont    = new Font(bf, 11, Font.NORMAL, BaseColor.DARK_GRAY);
                    tableHeaderFont = new Font(bf, 11, Font.BOLD, BaseColor.WHITE);
                    tableCellFont   = new Font(bf, 10, Font.NORMAL, BaseColor.BLACK);
                    totalFont       = new Font(bf, 12, Font.BOLD, BaseColor.BLACK);
                    tmpFont.deleteOnExit();
                }
            } catch (Exception ex) {
                System.err.println("Custom font embedding failed: " + ex.getMessage());
            }

            // Header (logo + company info + meta)
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{2.5f, 5.5f});
            headerTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            PdfPCell logoCell = new PdfPCell();
            logoCell.setBorder(Rectangle.NO_BORDER);
            logoCell.setPadding(0);
            try {
                ClassPathResource logoRes = new ClassPathResource(LOGO_PNG_CLASSPATH);
                if (logoRes.exists()) {
                    try (InputStream is = logoRes.getInputStream();
                         ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                        byte[] buf = new byte[8192];
                        int r;
                        while ((r = is.read(buf)) != -1) baos.write(buf, 0, r);
                        Image logo = Image.getInstance(baos.toByteArray());
                        logo.scaleToFit(110, 60);
                        logoCell.addElement(logo);
                    }
                } else {
                    Paragraph initials = new Paragraph(companyNameInitials(companyName), titleFont);
                    initials.setSpacingBefore(6);
                    logoCell.addElement(initials);
                }
            } catch (Exception ex) {
                Paragraph initials = new Paragraph(companyNameInitials(companyName), titleFont);
                initials.setSpacingBefore(6);
                logoCell.addElement(initials);
            }
            headerTable.addCell(logoCell);

            PdfPCell infoCell = new PdfPCell();
            infoCell.setBorder(Rectangle.NO_BORDER);
            infoCell.setPadding(0);

            Paragraph comp = new Paragraph(companyName, titleFont);
            comp.setSpacingAfter(4);
            infoCell.addElement(comp);

            Paragraph addr = new Paragraph(companyAddress, subtitleFont);
            addr.setSpacingAfter(2);
            infoCell.addElement(addr);

            Paragraph contact = new Paragraph("Phone: " + companyPhone + "   |   Email: " + companyEmail, subtitleFont);
            contact.setSpacingAfter(8);
            infoCell.addElement(contact);

            PdfPTable meta = new PdfPTable(2);
            meta.setWidthPercentage(100);
            meta.setWidths(new float[]{1, 2});
            meta.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            meta.addCell(rightCell("Invoice #:", tableCellFont));
            meta.addCell(valueCell(String.valueOf(order.getId()), tableCellFont));

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            meta.addCell(rightCell("Created:", tableCellFont));
            meta.addCell(valueCell(order.getCreatedAt() != null ? order.getCreatedAt().format(dtf) : "-", tableCellFont));

            meta.addCell(rightCell("Delivery:", tableCellFont));
            meta.addCell(valueCell(order.getDeliveryDate() != null ? order.getDeliveryDate().toString() : "-", tableCellFont));

            infoCell.addElement(meta);
            headerTable.addCell(infoCell);
            document.add(headerTable);
            document.add(Chunk.NEWLINE);

            // Customer block
            PdfPTable custTable = new PdfPTable(1);
            custTable.setWidthPercentage(100);
            custTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            StringBuilder customerInfoSb = new StringBuilder();
            if (order.getCustomer() != null) {
                customerInfoSb.append(nonNull(order.getCustomer().getName(), "Customer"));
                if (order.getCustomer().getAddress() != null)
                    customerInfoSb.append("\n").append(order.getCustomer().getAddress());
                if (order.getCustomer().getPhone() != null)
                    customerInfoSb.append("\nPhone: ").append(order.getCustomer().getPhone());
                if (order.getCustomer().getEmail() != null)
                    customerInfoSb.append("\nEmail: ").append(order.getCustomer().getEmail());
            } else {
                customerInfoSb.append("Customer");
            }
            Paragraph custP = new Paragraph("Bill To:\n" + customerInfoSb, subtitleFont);
            custTable.addCell(custP);
            document.add(custTable);
            document.add(Chunk.NEWLINE);

            // Items table
            PdfPTable table = new PdfPTable(new float[]{5, 2, 1.2f, 2});
            table.setWidthPercentage(100);

            PdfPCell h1 = new PdfPCell(new Phrase("Description", tableHeaderFont));
            styleHeader(h1); h1.setHorizontalAlignment(Element.ALIGN_LEFT);  table.addCell(h1);

            PdfPCell h2 = new PdfPCell(new Phrase("Unit Price", tableHeaderFont));
            styleHeader(h2); h2.setHorizontalAlignment(Element.ALIGN_RIGHT); table.addCell(h2);

            PdfPCell h3 = new PdfPCell(new Phrase("Qty", tableHeaderFont));
            styleHeader(h3); h3.setHorizontalAlignment(Element.ALIGN_CENTER); table.addCell(h3);

            PdfPCell h4 = new PdfPCell(new Phrase("Subtotal", tableHeaderFont));
            styleHeader(h4); h4.setHorizontalAlignment(Element.ALIGN_RIGHT); table.addCell(h4);

            BigDecimal subtotal = BigDecimal.ZERO;
            if (order.getOrderItems() != null) {
                for (OrderItem it : order.getOrderItems()) {

                    Product p = it.getProduct();
                    String desc = (p != null ? nonNull(p.getName(), "Product") : "Product");
                    if (p != null && p.getSpecies() != null && !p.getSpecies().isBlank()) {
                        desc += " (" + p.getSpecies() + ")";
                    }
                    table.addCell(cell(desc, tableCellFont));

                    // ✅ FIXED UNIT PRICE LOGIC
                    int qty = (it.getQuantity() != null ? it.getQuantity() : 0);

                    BigDecimal unit;
                    if (qty > 0 && it.getSubtotal() != null) {
                        // ✅ safest → derive from subtotal
                        unit = it.getSubtotal()
                                 .divide(BigDecimal.valueOf(qty), 2, RoundingMode.HALF_UP);
                    } else if (p != null && p.getPrice() != null) {
                        // fallback to product price
                        unit = p.getPrice();
                    } else {
                        unit = BigDecimal.ZERO;
                    }

                    // ✅ calculate row subtotal
                    BigDecimal rowSubtotal = BigDecimal.ZERO;
                    if (it.getSubtotal() != null) {
                        rowSubtotal = it.getSubtotal();
                    } else {
                        rowSubtotal = unit.multiply(BigDecimal.valueOf(qty));
                    }

                    subtotal = subtotal.add(rowSubtotal);

                    // ✅ print correctly
                    table.addCell(rightCell(currencyFormatter.format(unit), tableCellFont));
                    table.addCell(centerCell(String.valueOf(qty), tableCellFont));
                    table.addCell(rightCell(currencyFormatter.format(rowSubtotal), tableCellFont));
                }
            }


            // spacer row
            PdfPCell blank = new PdfPCell(new Phrase(" "));
            blank.setColspan(4);
            blank.setBorder(Rectangle.NO_BORDER);
            blank.setFixedHeight(8);
            table.addCell(blank);

            BigDecimal taxAmount = BigDecimal.ZERO;
            if (taxPercent > 0.0) {
                taxAmount = subtotal.multiply(BigDecimal.valueOf(taxPercent))
                                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            }
            BigDecimal totalAmount = subtotal.add(taxAmount);

            PdfPCell labelSub = new PdfPCell(new Phrase("Subtotal", totalFont));
            labelSub.setColspan(3);
            labelSub.setHorizontalAlignment(Element.ALIGN_RIGHT);
            labelSub.setBorder(Rectangle.NO_BORDER);
            labelSub.setPaddingTop(6);
            table.addCell(labelSub);
            PdfPCell valueSub = new PdfPCell(new Phrase(currencyFormatter.format(subtotal), totalFont));
            valueSub.setBorder(Rectangle.NO_BORDER);
            valueSub.setHorizontalAlignment(Element.ALIGN_RIGHT);
            valueSub.setPaddingTop(6);
            table.addCell(valueSub);

            if (taxPercent > 0.0) {
                PdfPCell labelTax = new PdfPCell(new Phrase("Tax (" + taxPercent + "%)", totalFont));
                labelTax.setColspan(3);
                labelTax.setHorizontalAlignment(Element.ALIGN_RIGHT);
                labelTax.setBorder(Rectangle.NO_BORDER);
                labelTax.setPaddingTop(2);
                table.addCell(labelTax);
                PdfPCell valueTax = new PdfPCell(new Phrase(currencyFormatter.format(taxAmount), totalFont));
                valueTax.setBorder(Rectangle.NO_BORDER);
                valueTax.setHorizontalAlignment(Element.ALIGN_RIGHT);
                valueTax.setPaddingTop(2);
                table.addCell(valueTax);
            }

            PdfPCell labelTotal = new PdfPCell(new Phrase("Total", totalFont));
            labelTotal.setColspan(3);
            labelTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
            labelTotal.setBorder(Rectangle.TOP);
            labelTotal.setPaddingTop(8);
            labelTotal.setPaddingBottom(6);
            table.addCell(labelTotal);

            PdfPCell valueTotal = new PdfPCell(new Phrase(currencyFormatter.format(totalAmount), totalFont));
            valueTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
            valueTotal.setBorder(Rectangle.TOP);
            valueTotal.setPaddingTop(8);
            valueTotal.setPaddingBottom(6);
            table.addCell(valueTotal);

            document.add(table);
            document.add(Chunk.NEWLINE);

            Paragraph payment = new Paragraph("Payment status: " + (order.getPaymentStatus() != null ? order.getPaymentStatus() : "-"), subtitleFont);
            document.add(payment);
            document.add(Chunk.NEWLINE);

            try {
            	//String billUrl = appBaseUrl + "/orders/" + order.getId() + "/bill";
            	String billUrl = "http://collegeerp.duckdns.org/";
                Image qrImage = generateQrImage(billUrl, 120, 120);
                if (qrImage != null) {
                    qrImage.setAlignment(Image.RIGHT);
                    document.add(qrImage);
                }
            } catch (Exception ex) {
                System.err.println("QR generation failed: " + ex.getMessage());
            }

            document.add(Chunk.NEWLINE);
            Paragraph thanks = new Paragraph("Thank you for shopping with " + companyName + "!", subtitleFont);
            thanks.setSpacingBefore(10);
            document.add(thanks);

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }

        return pdfFile.getAbsolutePath();
    }

    private static void styleHeader(PdfPCell c) {
        c.setBackgroundColor(new BaseColor(17, 122, 55));
        c.setPadding(8);
        c.setBorder(Rectangle.NO_BORDER);
    }

    private static String nonNull(String v, String def) {
        return (v == null || v.isBlank()) ? def : v;
    }

    /** Creates a QR PNG in memory and returns an iText Image */
    private Image generateQrImage(String url, int width, int height) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.MARGIN, 1);
            BitMatrix bm = writer.encode(url, BarcodeFormat.QR_CODE, width, height, hints);

            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    boolean black = bm.get(x, y);
                    image.setRGB(x, y, black ? 0xFF000000 : 0xFFFFFFFF);
                }
            }
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                ImageIO.write(image, "PNG", baos);
                baos.flush();
                return Image.getInstance(baos.toByteArray());
            }
        } catch (Exception e) {
            System.err.println("Failed to create QR: " + e.getMessage());
            return null;
        }
    }

    private PdfPCell rightCell(String text, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setBorder(Rectangle.NO_BORDER);
        c.setHorizontalAlignment(Element.ALIGN_RIGHT);
        c.setPadding(4);
        return c;
    }

    private PdfPCell centerCell(String text, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setBorder(Rectangle.NO_BORDER);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        c.setPadding(4);
        return c;
    }

    private PdfPCell valueCell(String text, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setBorder(Rectangle.NO_BORDER);
        c.setHorizontalAlignment(Element.ALIGN_LEFT);
        c.setPadding(4);
        return c;
    }

    private PdfPCell cell(String text, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setBorder(Rectangle.NO_BORDER);
        c.setPadding(8);
        return c;
    }

    private String companyNameInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "GH";
        String[] parts = name.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (!p.isBlank()) sb.append(p.charAt(0));
            if (sb.length() >= 2) break;
        }
        return sb.toString().toUpperCase();
    }
}
