package com.example.medicalshopb2b.utils;

import com.example.medicalshopb2b.model.Medicine;

/**
 * 🤖 REORDER PREDICTOR - AI-Based Smart Quantity Prediction
 *
 * Two prediction methods:
 * 1. predict(Medicine) - Full data from supplier (TotalSold, LastSoldAt)
 * 2. predict(int currentStock) - Simple method for shopkeeper quick reorder
 */
public class ReorderPredictor {

    private static final int DAYS_TO_COVER = 7;        // Cover 7 days of sales
    private static final int MIN_SAFE_REORDER = 10;    // Minimum order quantity
    private static final int MAX_ORDER = 200;          // Maximum order to avoid overstocking

    /**
     * 🤖 ADVANCED PREDICTION - Using full supplier medicine data
     *
     * Algorithm:
     * 1. Calculate average daily sales from totalSold & lastSoldAt
     * 2. Predict quantity needed for 7 days
     * 3. Account for lead time and safety stock
     *
     * Example:
     * - Total Sold: 100 units in 10 days
     * - Avg Daily: 10 units/day
     * - 7-day cover: 70 units
     * - Add safety: 70 + 10 = 80 units
     *
     * @param m Medicine object from supplier with sales history
     * @return Suggested order quantity
     */
    public static int predictAdvanced(Medicine m) {

        if (m == null) return MIN_SAFE_REORDER;

        // 🔥 Check if we have sales data
        int totalSold = m.getTotalSold();
        long lastSoldAt = m.getLastSoldAt();

        if (totalSold <= 0 || lastSoldAt == 0) {
            // No sales history - return minimum
            return MIN_SAFE_REORDER;
        }

        // 🔥 Calculate days since medicine was added
        long now = System.currentTimeMillis();
        long daysActive = Math.max(1, (now - m.getLastSoldAt()) / (1000L * 60 * 60 * 24));

        // 🔥 Calculate average daily sales
        int avgDailySales = Math.max(1, totalSold / (int) daysActive);

        // 🔥 Calculate quantity for DAYS_TO_COVER (default: 7 days)
        int baseQuantity = avgDailySales * DAYS_TO_COVER;

        // 🔥 Add safety stock (20% buffer for unexpected demand)
        int safetyStock = (baseQuantity * 20) / 100;
        int suggestedQty = baseQuantity + safetyStock;

        // 🔥 Clamp between min and max
        return Math.max(Math.min(suggestedQty, MAX_ORDER), MIN_SAFE_REORDER);
    }

    /**
     * 🤖 SIMPLE PREDICTION - Based only on current stock level
     *
     * Used when shopkeeper quickly needs to create a reorder
     * without full sales history data
     *
     * Stock Levels:
     * ≤ 0    → 30 units (critical, order max)
     * ≤ 2    → 25 units (very low)
     * ≤ 5    → 20 units (low)
     * ≤ 10   → 15 units (moderate)
     * > 10   → 10 units (just topped up)
     *
     * @param currentStock Current stock level in shop
     * @return Suggested order quantity
     */
    public static int predictSimple(int currentStock) {

        if (currentStock <= 0) return 30;
        if (currentStock <= 2) return 25;
        if (currentStock <= 5) return 20;
        if (currentStock <= 10) return 15;

        return MIN_SAFE_REORDER;
    }

    /**
     * 🤖 DYNAMIC PREDICTION - Smart decision between advanced and simple
     *
     * Uses advanced if supplier medicine data is available,
     * Falls back to simple if not
     *
     * @param supplierMedicine Medicine object from supplier (may have sales data)
     * @param currentStock Current stock in shop
     * @return Suggested order quantity
     */
    public static int predict(Medicine supplierMedicine, int currentStock) {

        // Try advanced prediction first (if supplier medicine has sales data)
        if (supplierMedicine != null &&
                supplierMedicine.getTotalSold() > 0 &&
                supplierMedicine.getLastSoldAt() > 0) {

            return predictAdvanced(supplierMedicine);
        }

        // Fall back to simple prediction
        return predictSimple(currentStock);
    }

    /**
     * 🤖 OVERLOADED - For backward compatibility
     * Calls predictSimple()
     */
    public static int predict(int currentStock) {
        return predictSimple(currentStock);
    }

    /**
     * 🤖 OVERLOADED - For backward compatibility
     * Calls predictAdvanced()
     */
    public static int predict(Medicine m) {
        return predictAdvanced(m);
    }

    /**
     * 📊 GET URGENCY LEVEL
     * Returns urgency for UI color coding
     *
     * CRITICAL (Red)     → ≤ 5 units
     * HIGH (Orange)      → ≤ 10 units
     * MEDIUM (Yellow)    → ≤ 15 units
     * LOW (Green)        → > 15 units
     */
    public static String getUrgencyLevel(int currentStock) {
        if (currentStock <= 5) {
            return "CRITICAL";  // Red - Order immediately
        } else if (currentStock <= 10) {
            return "HIGH";       // Orange - Order soon
        } else if (currentStock <= 15) {
            return "MEDIUM";     // Yellow - Plan order
        }
        return "LOW";           // Green - Stock sufficient
    }

    /**
     * 🎨 GET URGENCY COLOR
     * Returns color code for UI display
     */
    public static int getUrgencyColor(int currentStock) {
        if (currentStock <= 5) {
            return 0xFFE74C3C;   // Red
        } else if (currentStock <= 10) {
            return 0xFFFF9800;   // Orange
        } else if (currentStock <= 15) {
            return 0xFFFFC107;   // Yellow
        }
        return 0xFF27AE60;       // Green
    }

    /**
     * 📈 GET PREDICTION CONFIDENCE
     * Returns confidence level based on data availability
     */
    public static String getConfidenceLevel(Medicine m) {

        if (m == null) return "LOW";

        int totalSold = m.getTotalSold();
        long lastSoldAt = m.getLastSoldAt();

        if (totalSold <= 0 || lastSoldAt == 0) {
            return "LOW";           // No data
        }

        if (totalSold < 10) {
            return "MEDIUM";        // Limited data
        }

        return "HIGH";              // Sufficient data
    }

    /**
     * 📊 STATS - For debugging/analytics
     */
    public static class PredictionStats {
        public int avgDailySales;
        public int suggestedQty;
        public String urgency;
        public String confidence;
        public int daysOfCoverage;

        @Override
        public String toString() {
            return "PredictionStats{" +
                    "avgDailySales=" + avgDailySales +
                    ", suggestedQty=" + suggestedQty +
                    ", urgency='" + urgency + '\'' +
                    ", confidence='" + confidence + '\'' +
                    ", daysOfCoverage=" + daysOfCoverage +
                    '}';
        }
    }

    /**
     * 📊 GET DETAILED STATS
     */
    public static PredictionStats getStats(Medicine m, int currentStock) {

        PredictionStats stats = new PredictionStats();
        stats.suggestedQty = predict(m, currentStock);
        stats.urgency = getUrgencyLevel(currentStock);
        stats.confidence = getConfidenceLevel(m);
        stats.daysOfCoverage = DAYS_TO_COVER;

        if (m != null && m.getTotalSold() > 0 && m.getLastSoldAt() > 0) {
            long daysActive = Math.max(1,
                    (System.currentTimeMillis() - m.getLastSoldAt()) / (1000L * 60 * 60 * 24));
            stats.avgDailySales = m.getTotalSold() / (int) daysActive;
        } else {
            stats.avgDailySales = 0;
        }

        return stats;
    }
}