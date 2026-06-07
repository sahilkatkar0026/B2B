    package com.example.medicalshopb2b.utils;

    import com.example.medicalshopb2b.model.Medicine;

    public class StockPredictor {

        public static String predict(Medicine m) {

            if (m.getTotalSold() == 0) {
                return "New medicine. No sales data yet.";
            }

            int dailySales = Math.max(1, m.getTotalSold() / 7);
            int daysLeft = m.getStock() / dailySales;

            if (daysLeft <= 1) {
                return "🚨 Very High Demand! Stock may finish today.";
            }

            if (daysLeft <= 3) {
                return "⚠ High Demand. Reorder immediately.";
            }

            if (daysLeft <= 7) {
                return "📦 Medium Demand. Monitor stock.";
            }

            return "✅ Stock level is healthy.";
        }
    }
