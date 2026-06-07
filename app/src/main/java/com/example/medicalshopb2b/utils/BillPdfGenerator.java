package com.example.medicalshopb2b.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;

import com.example.medicalshopb2b.model.CartItem;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 🔥 BILL PDF GENERATOR - Real-time GST & Discount
 * Generates professional invoices with dynamic calculations
 */
public class BillPdfGenerator {

    /**
     * Generate PDF invoice with real-time GST and discount
     *
     * @param context Application context
     * @param shopkeeperName Name of the shopkeeper/shop
     * @param supplierName Name of the supplier (medicines provider)
     * @param customerName Customer name
     * @param mobile Customer mobile number
     * @param items List of cart items
     * @param discountPercent Discount percentage from user input
     * @param gstPercent GST percentage from user input
     * @return Generated PDF file
     */
    public static File generatePdf(
            Context context,
            String shopkeeperName,
            String supplierName,
            String customerName,
            String mobile,
            List<CartItem> items,
            double discountPercent,
            double gstPercent
    ) throws Exception {

        PdfDocument pdf = new PdfDocument();
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        PdfDocument.Page page = pdf.startPage(
                new PdfDocument.PageInfo.Builder(595, 842, 1).create()
        );

        Canvas canvas = page.getCanvas();
        int y = 40;

        // ================= HEADER =================
        paint.setTextSize(22);
        paint.setFakeBoldText(true);
        canvas.drawText(shopkeeperName != null ? shopkeeperName : "Medical Shop", 40, y, paint);

        paint.setTextSize(12);
        paint.setFakeBoldText(false);

        y += 25;
        canvas.drawText("Supplier: " + (supplierName != null ? supplierName : "N/A"), 40, y, paint);

        y += 15;
        canvas.drawText("Medical Invoice", 40, y, paint);

        String date = new SimpleDateFormat(
                "dd MMM yyyy, hh:mm a",
                Locale.getDefault()
        ).format(new Date());

        y += 18;
        canvas.drawText("Date: " + date, 40, y, paint);

        y += 15;
        canvas.drawLine(40, y, 555, y, paint);

        // ================= CUSTOMER DETAILS =================
        y += 25;
        paint.setFakeBoldText(true);
        canvas.drawText("Customer Details", 40, y, paint);
        paint.setFakeBoldText(false);

        y += 18;
        canvas.drawText("Name: " + (customerName != null ? customerName : "N/A"), 40, y, paint);

        y += 16;
        canvas.drawText("Mobile: " + (mobile != null ? mobile : "N/A"), 40, y, paint);

        y += 15;
        canvas.drawLine(40, y, 555, y, paint);

        // ================= TABLE HEADER =================
        y += 25;
        paint.setFakeBoldText(true);
        paint.setTextSize(11);

        canvas.drawText("No", 40, y, paint);
        canvas.drawText("Medicine", 80, y, paint);
        canvas.drawText("Qty", 280, y, paint);
        canvas.drawText("Rate", 330, y, paint);
        canvas.drawText("Amount", 430, y, paint);

        paint.setFakeBoldText(false);

        y += 10;
        canvas.drawLine(40, y, 555, y, paint);

        // ================= ITEMS =================
        int sr = 1;
        double subtotal = 0.0;

        if (items != null && !items.isEmpty()) {
            for (CartItem item : items) {

                y += 22;

                double price = item.getPrice();
                int qty = item.getQuantity();
                double amount = price * qty;

                subtotal += amount;

                paint.setTextSize(11);
                canvas.drawText(String.valueOf(sr++), 40, y, paint);
                canvas.drawText(item.getName(), 80, y, paint);
                canvas.drawText(String.valueOf(qty), 280, y, paint);
                canvas.drawText("₹" + String.format(Locale.getDefault(), "%.2f", price), 330, y, paint);
                canvas.drawText("₹" + String.format(Locale.getDefault(), "%.2f", amount), 430, y, paint);
            }
        }

        // ================= 🔥 REAL-TIME CALCULATIONS =================
        InvoiceCalculator.InvoiceSummary summary =
                new InvoiceCalculator.InvoiceSummary(subtotal, discountPercent, gstPercent);

        // ================= TOTALS SECTION =================
        y += 25;
        canvas.drawLine(40, y, 555, y, paint);

        paint.setTextSize(12);
        paint.setFakeBoldText(true);

        y += 22;
        canvas.drawText("Subtotal:", 330, y, paint);
        canvas.drawText("₹" + String.format(Locale.getDefault(), "%.2f", summary.subtotal), 430, y, paint);

        y += 18;
        paint.setTextSize(11);
        canvas.drawText("Discount (" + String.format("%.1f", discountPercent) + "%):", 330, y, paint);
        canvas.drawText("- ₹" + String.format(Locale.getDefault(), "%.2f", summary.discountAmount), 430, y, paint);

        y += 18;
        canvas.drawText("GST (" + String.format("%.1f", gstPercent) + "%):", 330, y, paint);
        canvas.drawText("+ ₹" + String.format(Locale.getDefault(), "%.2f", summary.gstAmount), 430, y, paint);

        y += 20;
        paint.setTextSize(14);
        paint.setFakeBoldText(true);
        canvas.drawLine(330, y - 5, 555, y - 5, paint);

        y += 3;
        canvas.drawText("Grand Total:", 310, y, paint);
        canvas.drawText("₹" + String.format(Locale.getDefault(), "%.2f", summary.grandTotal), 430, y, paint);

        paint.setTextSize(12);
        paint.setFakeBoldText(false);

        // ================= FOOTER =================
        y += 40;
        canvas.drawLine(40, y, 555, y, paint);

        y += 18;
        canvas.drawText("Thank you for your purchase!", 40, y, paint);

        y += 14;
        canvas.drawText("This is a computer generated invoice.", 40, y, paint);

        y += 14;
        canvas.drawText("Please keep this invoice for your records.", 40, y, paint);

        pdf.finishPage(page);

        // ================= SAVE FILE =================
        File dir = new File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "MedicalShopBills"
        );

        if (!dir.exists()) dir.mkdirs();

        File file = new File(
                dir,
                "Invoice_" + System.currentTimeMillis() + ".pdf"
        );

        try (FileOutputStream fos = new FileOutputStream(file)) {
            pdf.writeTo(fos);
        }
        pdf.close();

        return file;
    }

    /**
     * Overloaded method for backward compatibility
     * Uses default GST (12%) if not provided
     */
    public static File generatePdf(
            Context context,
            String shopkeeperName,
            String customerName,
            String mobile,
            List<CartItem> items
    ) throws Exception {
        return generatePdf(context, shopkeeperName, "Default Supplier", customerName, mobile, items, 0, 12);
    }
}