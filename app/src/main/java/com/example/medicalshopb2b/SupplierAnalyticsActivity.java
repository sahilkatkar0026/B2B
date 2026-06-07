package com.example.medicalshopb2b;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.medicalshopb2b.model.Medicine;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SupplierAnalyticsActivity extends AppCompatActivity {

    private BarChart barChart;
    private TextView txtProfit, txtTotalSales, txtTopProduct, txtAvgSales;
    private CardView btnExportPdf;
    private ImageView btnBack;
    private LinearLayout emptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supplier_analytics);

        // Initialize views
        barChart = findViewById(R.id.barChart);
        txtProfit = findViewById(R.id.txtProfit);
        txtTotalSales = findViewById(R.id.txtTotalSales);
        txtTopProduct = findViewById(R.id.txtTopProduct);
        txtAvgSales = findViewById(R.id.txtAvgSales);
        btnExportPdf = findViewById(R.id.btnExportPdf);
        btnBack = findViewById(R.id.btnBack);
        emptyState = findViewById(R.id.emptyState);

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Export button
        btnExportPdf.setOnClickListener(v ->
                Toast.makeText(this, "PDF export coming next 🔥", Toast.LENGTH_SHORT).show()
        );

        // Load analytics data
        loadAnalytics();
    }

    // ======================= DATA LOAD =======================

    private void loadAnalytics() {
        String supplierId = FirebaseAuth.getInstance().getUid();

        android.util.Log.d("AnalyticsDebug", "════════════════════════════════════════");
        android.util.Log.d("AnalyticsDebug", "🔹 LOADING ANALYTICS");
        android.util.Log.d("AnalyticsDebug", "🔹 Supplier ID: " + supplierId);

        if (supplierId == null) {
            android.util.Log.d("AnalyticsDebug", "❌ Supplier ID is null - user not logged in");
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            showEmptyState();
            return;
        }

        FirebaseDatabase.getInstance()
                .getReference("suppliers")
                .child(supplierId)
                .child("medicines")
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        android.util.Log.d("AnalyticsDebug", "════════════════════════════════════════");
                        android.util.Log.d("AnalyticsDebug", "✅ onDataChange called");
                        android.util.Log.d("AnalyticsDebug", "📊 Snapshot exists: " + snapshot.exists());
                        android.util.Log.d("AnalyticsDebug", "📊 Children count: " + snapshot.getChildrenCount());

                        if (!snapshot.exists()) {
                            android.util.Log.d("AnalyticsDebug", "❌ No medicines found in Firebase");
                            showEmptyState();
                            return;
                        }

                        List<Medicine> medicines = new ArrayList<>();

                        for (DataSnapshot snap : snapshot.getChildren()) {
                            try {
                                Medicine m = snap.getValue(Medicine.class);

                                if (m != null) {
                                    m.setMedicineId(snap.getKey());
                                    medicines.add(m);

                                    android.util.Log.d("AnalyticsDebug", "📝 Medicine: " + m.getName() +
                                            " | Sold: " + m.getTotalSold() +
                                            " | Price: ₹" + m.getPrice());
                                } else {
                                    android.util.Log.d("AnalyticsDebug", "❌ Medicine is null for key: " + snap.getKey());
                                }
                            } catch (Exception e) {
                                android.util.Log.d("AnalyticsDebug", "❌ Error parsing medicine: " + e.getMessage());
                            }
                        }

                        android.util.Log.d("AnalyticsDebug", "🎯 Total medicines loaded: " + medicines.size());
                        android.util.Log.d("AnalyticsDebug", "════════════════════════════════════════");

                        if (medicines.isEmpty()) {
                            android.util.Log.d("AnalyticsDebug", "❌ Medicines list is empty");
                            showEmptyState();
                            return;
                        }

                        // ✅ SORT BY totalSold DESC (highest first)
                        Collections.sort(medicines, (a, b) ->
                                Integer.compare(b.getTotalSold(), a.getTotalSold())
                        );

                        android.util.Log.d("AnalyticsDebug", "✅ Medicines sorted by totalSold");
                        buildAnalytics(medicines);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        android.util.Log.d("AnalyticsDebug", "❌ DATABASE ERROR: " + error.getMessage());
                        Toast.makeText(
                                SupplierAnalyticsActivity.this,
                                "Error: " + error.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                        showEmptyState();
                    }
                });
    }

    // ======================= BUILD ANALYTICS =======================

    private void buildAnalytics(List<Medicine> medicines) {
        android.util.Log.d("AnalyticsDebug", "════════════════════════════════════════");
        android.util.Log.d("AnalyticsDebug", "🔧 BUILDING ANALYTICS");

        // Show chart IMMEDIATELY
        barChart.setVisibility(View.VISIBLE);
        emptyState.setVisibility(View.GONE);

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        int index = 0;
        int totalProfit = 0;
        int totalSales = 0;
        Medicine topMedicine = medicines.get(0);

        // Process top 10 medicines
        for (Medicine m : medicines) {
            if (index >= 10) break;

            int sold = Math.max(m.getTotalSold(), 0);
            int price = Math.max(m.getPrice(), 0);
            int profit = sold * price;

            // Add to chart
            entries.add(new BarEntry(index, sold));
            labels.add(m.getName());

            totalProfit += profit;
            totalSales += sold;

            android.util.Log.d("AnalyticsDebug", "📊 Index " + index + ": " + m.getName() +
                    " | Sold: " + sold +
                    " | Price: ₹" + price +
                    " | Profit: ₹" + profit);

            index++;
        }

        // Check if data exists
        if (entries.isEmpty()) {
            android.util.Log.d("AnalyticsDebug", "❌ No chart entries - showing empty state");
            showEmptyState();
            return;
        }

        android.util.Log.d("AnalyticsDebug", "════════════════════════════════════════");
        android.util.Log.d("AnalyticsDebug", "💰 FINAL METRICS:");
        android.util.Log.d("AnalyticsDebug", "   Total Profit: ₹" + totalProfit);
        android.util.Log.d("AnalyticsDebug", "   Total Sales: " + totalSales + " units");
        android.util.Log.d("AnalyticsDebug", "   Top Product: " + topMedicine.getName());
        android.util.Log.d("AnalyticsDebug", "   Avg Sales: " + (medicines.size() > 0 ? totalSales / medicines.size() : 0));
        android.util.Log.d("AnalyticsDebug", "════════════════════════════════════════");

        // Setup bar chart with entries
        setupBarChart(entries, labels);

        // Update UI with values
        txtProfit.setText("₹" + formatNumber(totalProfit));
        txtTotalSales.setText(totalSales + " units");
        txtTopProduct.setText("Top Product: " + topMedicine.getName() + " (" + topMedicine.getTotalSold() + " sold)");

        int avgSales = medicines.size() > 0 ? totalSales / medicines.size() : 0;
        txtAvgSales.setText("Avg Sales: " + avgSales + " units/medicine");
    }

    // ======================= CHART SETUP =======================

    private void setupBarChart(List<BarEntry> entries, List<String> labels) {
        android.util.Log.d("AnalyticsDebug", "📊 Setting up bar chart with " + entries.size() + " entries");

        // Clear any existing data first
        barChart.clear();

        BarDataSet dataSet = new BarDataSet(entries, "Units Sold");
        dataSet.setColor(Color.parseColor("#3B82F6"));
        dataSet.setValueTextSize(11f);
        dataSet.setValueTextColor(Color.parseColor("#1E293B"));

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.7f);

        barChart.setData(data);
        barChart.getDescription().setEnabled(false);
        barChart.setFitBars(true);

        // Configure left Y-Axis
        barChart.getAxisLeft().setTextColor(Color.parseColor("#64748B"));
        barChart.getAxisLeft().setDrawGridLines(true);
        barChart.getAxisLeft().setAxisLineColor(Color.parseColor("#E2E8F0"));

        // Disable right Y-Axis
        barChart.getAxisRight().setEnabled(false);

        // Disable legend
        barChart.getLegend().setEnabled(false);

        // X-Axis configuration
        XAxis xAxis = barChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelRotationAngle(-40f);
        xAxis.setTextSize(10f);
        xAxis.setTextColor(Color.parseColor("#64748B"));
        xAxis.setAxisLineColor(Color.parseColor("#E2E8F0"));

        // Animate and refresh
        barChart.animateY(1000);
        barChart.invalidate();

        android.util.Log.d("AnalyticsDebug", "✅ Bar chart setup complete");
    }

    // ======================= EMPTY STATE =======================

    private void showEmptyState() {
        android.util.Log.d("AnalyticsDebug", "📭 Showing empty state");

        emptyState.setVisibility(View.VISIBLE);
        barChart.setVisibility(View.GONE);

        txtProfit.setText("₹0");
        txtTotalSales.setText("0 units");
        txtTopProduct.setText("Top Product: No data");
        txtAvgSales.setText("Avg Sales: No data");
    }

    // ======================= FORMATTING =======================

    private String formatNumber(int n) {
        if (n >= 1_000_000) return (n / 1_000_000) + "M";
        if (n >= 1_000) return (n / 1_000) + "K";
        return String.valueOf(n);
    }
}