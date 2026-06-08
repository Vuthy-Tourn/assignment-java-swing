package com.pos.util;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.pos.model.Order;
import com.pos.model.OrderItem;
import com.pos.model.Payment;

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public class ReceiptPdfExporter {

    // Colors — match UIConstants exactly
    private static final DeviceRgb SUCCESS      = new DeviceRgb(34, 197, 94);
    private static final DeviceRgb TEXT_MUTED   = new DeviceRgb(107, 114, 128);
    private static final DeviceRgb BORDER_COLOR = new DeviceRgb(229, 231, 235);
    private static final DeviceRgb TEXT_PRIMARY = new DeviceRgb(17, 24, 39);

    // Font sizes — scaled down to feel like Swing rendering
    // Swing pixels ≈ PDF points * 0.75, so 22px → ~11pt, 14px → ~10pt, 12px → ~9pt
    private static final float FONT_TITLE   = 16f;  // was 22f — too large
    private static final float FONT_HEADING = 11f;  // was 14f
    private static final float FONT_BODY    = 9.5f; // was 12f
    private static final float FONT_MUTED   = 9f;   // was 11f

    private static final float CARD_PADDING    = 24f;
    private static final float SECTION_SPACING = 14f;
    private static final float ROW_SPACING     = 6f;

    public static void export(Order order, File destination) throws Exception {

        // Use a custom narrow page that mirrors the 500px Swing card
        // 500px at 72dpi ≈ 360pt; add margins so content width ≈ card interior
        PageSize pageSize = new PageSize(360f, 700f); // width x height in points

        PdfWriter   writer   = new PdfWriter(destination);
        PdfDocument pdf      = new PdfDocument(writer);
        Document    document = new Document(pdf, pageSize);

        document.setMargins(CARD_PADDING, CARD_PADDING, CARD_PADDING, CARD_PADDING);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy hh:mm a");
        Payment payment = order.getPayment();

        // ── Header ──────────────────────────────────────────────
        document.add(buildHeader());

        // ── Divider ─────────────────────────────────────────────
        document.add(divider());

        // ── "✓ Payment Successful" ───────────────────────────────
        document.add(new Paragraph("✓ Payment Successful")
                .setFont(boldFont())
                .setFontSize(FONT_HEADING + 2)
                .setFontColor(SUCCESS)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(10f)
                .setMarginBottom(3f));

        document.add(new Paragraph(order.getReceiptNumber())
                .setFontSize(FONT_MUTED)
                .setFontColor(TEXT_MUTED)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(SECTION_SPACING));

        // ── "Payment Details" section ────────────────────────────
        document.add(sectionTitle("Payment Details"));

        Table infoTable = new Table(
                UnitValue.createPercentArray(new float[]{1, 1})
        ).useAllAvailableWidth().setBorder(Border.NO_BORDER);

        addInfoRow(infoTable, "Receipt",  order.getReceiptNumber());
        addInfoRow(infoTable, "Date",
                order.getCreatedAt() != null ? order.getCreatedAt().format(fmt) : "-");
        addInfoRow(infoTable, "Payment",  payment != null ? payment.getMethod() : "-");
        addInfoRow(infoTable, "Cashier",  order.getUserName());

        document.add(infoTable);

        // ── "Products" section ───────────────────────────────────
        document.add(sectionTitle("Products").setMarginTop(SECTION_SPACING));

        // ── 2-column table: (name+qty stacked) | (price right) ──
        // Mirrors ReceiptPanel.productRow() which is BorderLayout WEST/EAST only
        for (OrderItem item : order.getItems()) {
            document.add(productRow(item));
            document.add(spacer(6f));
        }

        document.add(spacer(6f));
        document.add(divider());
        document.add(spacer(6f));

        // ── Subtotal / Discount ──────────────────────────────────
        document.add(totalRow("Subtotal", formatMoney(order.getTotalAmount()), false));
        document.add(spacer(4f));
        document.add(totalRow("Discount", "- " + formatMoney(order.getDiscountAmount()), false));
        document.add(spacer(10f));

        document.add(divider());
        document.add(spacer(6f));

        // ── Final Amount — bold green ────────────────────────────
        document.add(totalRow("Final Amount", formatMoney(order.getFinalAmount()), true));

        document.close();
    }

    // ── Header ───────────────────────────────────────────────────

    private static Div buildHeader() throws Exception {

        Div header = new Div()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(SECTION_SPACING);

        try (InputStream is = ReceiptPdfExporter.class
                .getResourceAsStream("/images/logo_pos.png")) {
            if (is != null) {
                Image logo = new Image(ImageDataFactory.create(is.readAllBytes()))
                        .setWidth(60).setHeight(60)   // scaled to match 80px Swing @ lower DPI
                        .setHorizontalAlignment(HorizontalAlignment.CENTER);
                header.add(logo);
            }
        }

        header.add(new Paragraph("POS SYSTEM")
                .setFont(boldFont())
                .setFontSize(FONT_TITLE)
                .setFontColor(TEXT_PRIMARY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(6f).setMarginBottom(2f));

        header.add(new Paragraph("Payment Receipt")
                .setFontSize(FONT_BODY)
                .setFontColor(TEXT_MUTED)
                .setTextAlignment(TextAlignment.CENTER));

        return header;
    }

    // ── Product row — mirrors BorderLayout WEST(name+qty) / EAST(price) ──

    private static Table productRow(OrderItem item) {

        Table row = new Table(
                UnitValue.createPercentArray(new float[]{3, 1})
        ).useAllAvailableWidth().setBorder(Border.NO_BORDER);

        // Left: product name + "Qty: N" stacked
        Cell left = new Cell()
                .setBorder(Border.NO_BORDER)
                .setPaddingTop(2f).setPaddingBottom(2f);

        left.add(new Paragraph(item.getProductName())
                .setFontSize(FONT_BODY)
                .setFontColor(TEXT_PRIMARY)
                .setMarginBottom(1f));

        left.add(new Paragraph("Qty: " + item.getQuantity())
                .setFontSize(FONT_MUTED)
                .setFontColor(TEXT_MUTED));

        // Right: subtotal — no separate qty column
        Cell right = new Cell()
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .add(new Paragraph(formatMoney(item.getSubtotal()))
                        .setFont(boldFont())
                        .setFontSize(FONT_BODY)
                        .setTextAlignment(TextAlignment.RIGHT));

        row.addCell(left);
        row.addCell(right);
        return row;
    }

    // ── Section title ─────────────────────────────────────────────

    private static Paragraph sectionTitle(String text) {
        return new Paragraph(text)
                .setFont(boldFont())
                .setFontSize(FONT_HEADING)
                .setFontColor(TEXT_PRIMARY)
                .setMarginBottom(ROW_SPACING);
    }

    // ── Info row ──────────────────────────────────────────────────

    private static void addInfoRow(Table table, String key, String value) {

        table.addCell(new Cell()
                .setBorder(Border.NO_BORDER)
                .setPaddingBottom(ROW_SPACING)
                .add(new Paragraph(key)
                        .setFontSize(FONT_BODY)
                        .setFontColor(TEXT_MUTED)));

        table.addCell(new Cell()
                .setBorder(Border.NO_BORDER)
                .setPaddingBottom(ROW_SPACING)
                .add(new Paragraph(value != null ? value : "-")
                        .setFontSize(FONT_BODY)
                        .setFontColor(TEXT_PRIMARY)
                        .setTextAlignment(TextAlignment.RIGHT)));
    }

    // ── Total row ─────────────────────────────────────────────────

    private static Table totalRow(String label, String value, boolean bold) {

        Table row = new Table(
                UnitValue.createPercentArray(new float[]{1, 1})
        ).useAllAvailableWidth().setBorder(Border.NO_BORDER);

        row.addCell(new Cell()
                .setBorder(Border.NO_BORDER)
                .add(new Paragraph(label)
                        .setFontSize(bold ? FONT_HEADING : FONT_BODY)
                        .setFont(bold ? boldFont() : regularFont())
                        .setFontColor(TEXT_PRIMARY)));

        row.addCell(new Cell()
                .setBorder(Border.NO_BORDER)
                .add(new Paragraph(value)
                        .setFontSize(bold ? FONT_HEADING : FONT_BODY)
                        .setFont(bold ? boldFont() : regularFont())
                        .setFontColor(bold ? SUCCESS : TEXT_PRIMARY)
                        .setTextAlignment(TextAlignment.RIGHT)));

        return row;
    }

    // ── Divider ───────────────────────────────────────────────────

    private static LineSeparator divider() {
        SolidLine line = new SolidLine(0.5f);   // thinner — JSeparator is 1px
        line.setColor(BORDER_COLOR);
        return new LineSeparator(line)
                .setMarginTop(4f).setMarginBottom(4f);
    }

    // ── Vertical spacer ───────────────────────────────────────────

    private static Paragraph spacer(float height) {
        return new Paragraph("").setMarginTop(0).setMarginBottom(0).setHeight(height);
    }

    // ── Fonts ─────────────────────────────────────────────────────

    private static PdfFont boldFont() {
        try {
            return PdfFontFactory.createFont(
                    com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD);
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private static PdfFont regularFont() {
        try {
            return PdfFontFactory.createFont(
                    com.itextpdf.io.font.constants.StandardFonts.HELVETICA);
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private static String formatMoney(BigDecimal amount) {
        if (amount == null) return "$0.00";
        return String.format("$%.2f", amount);
    }
}