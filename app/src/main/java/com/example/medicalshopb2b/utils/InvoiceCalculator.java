package com.example.medicalshopb2b.utils;

import com.example.medicalshopb2b.model.CartItem;
import java.util.List;

/**
 * 🔥 REAL-TIME INVOICE CALCULATOR
 * Calculates totals based on user input GST and discount percentages
 */
public class InvoiceCalculator {

    // ================= SUBTOTAL (From cart items) =================
    public static double calculateSubtotal(List<CartItem> items) {
        double total = 0;
        if (items == null) return 0;

        for (CartItem item : items) {
            total += item.getQuantity() * item.getPrice();
        }
        return total;
    }

    // ================= DISCOUNT AMOUNT (Real-time) =================
    /**
     * Calculate discount amount based on percentage
     * @param subtotal Base amount before discount
     * @param discountPercent User-entered discount percentage (0-100)
     * @return Discount amount
     */
    public static double calculateDiscountAmount(double subtotal, double discountPercent) {
        if (discountPercent < 0) discountPercent = 0;
        if (discountPercent > 100) discountPercent = 100;

        return subtotal * (discountPercent / 100.0);
    }

    // ================= AMOUNT AFTER DISCOUNT =================
    public static double calculateAfterDiscount(double subtotal, double discountAmount) {
        return subtotal - discountAmount;
    }

    // ================= GST AMOUNT (Real-time) =================
    /**
     * Calculate GST amount based on percentage
     * @param afterDiscountAmount Amount after applying discount
     * @param gstPercent User-entered GST percentage (0-100)
     * @return GST amount
     */
    public static double calculateGSTAmount(double afterDiscountAmount, double gstPercent) {
        if (gstPercent < 0) gstPercent = 0;
        if (gstPercent > 100) gstPercent = 100;

        return afterDiscountAmount * (gstPercent / 100.0);
    }

    // ================= GRAND TOTAL (Real-time) =================
    /**
     * Calculate final grand total
     * @param subtotal Base subtotal
     * @param discountAmount Discount amount
     * @param gstAmount GST amount
     * @return Grand total = (Subtotal - Discount) + GST
     */
    public static double calculateGrandTotal(
            double subtotal,
            double discountAmount,
            double gstAmount
    ) {
        return (subtotal - discountAmount) + gstAmount;
    }

    // ================= CONVENIENCE METHOD (All in one) =================
    /**
     * Calculate all totals at once
     * @return Object containing all calculated values
     */
    public static class InvoiceSummary {
        public double subtotal;
        public double discountPercent;
        public double discountAmount;
        public double afterDiscount;
        public double gstPercent;
        public double gstAmount;
        public double grandTotal;

        public InvoiceSummary(
                double subtotal,
                double discountPercent,
                double gstPercent
        ) {
            this.subtotal = subtotal;
            this.discountPercent = discountPercent;
            this.gstPercent = gstPercent;

            // Calculate all values
            this.discountAmount = calculateDiscountAmount(subtotal, discountPercent);
            this.afterDiscount = calculateAfterDiscount(subtotal, discountAmount);
            this.gstAmount = calculateGSTAmount(afterDiscount, gstPercent);
            this.grandTotal = calculateGrandTotal(subtotal, discountAmount, gstAmount);
        }

        @Override
        public String toString() {
            return "InvoiceSummary{" +
                    "subtotal=" + String.format("%.2f", subtotal) +
                    ", discount=" + String.format("%.2f", discountAmount) +
                    ", gst=" + String.format("%.2f", gstAmount) +
                    ", grandTotal=" + String.format("%.2f", grandTotal) +
                    '}';
        }
    }
}