package com.example.medicalshopb2b;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SupplierDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supplier_dashboard);

        // ➕ Add Medicine
        findViewById(R.id.btnAddMedicine).setOnClickListener(v ->
                startActivity(new Intent(this, AddMedicineActivity.class))
        );

        // 📦 Supplier Medicines
        findViewById(R.id.btnSupplierMedicines).setOnClickListener(v ->
                startActivity(new Intent(this, SupplierInventoryActivity.class))
        );

        // 📦 View Orders
        findViewById(R.id.btnViewOrders).setOnClickListener(v ->
                startActivity(new Intent(this, SupplierOrdersActivity.class))
        );

        // 🔄 Reorders
        findViewById(R.id.btnReorders).setOnClickListener(v ->
                startActivity(new Intent(this, SupplierReorderActivity.class))
        );

        // 📊 Analytics
        findViewById(R.id.btnAnalytics).setOnClickListener(v ->
                startActivity(new Intent(this, SupplierAnalyticsActivity.class))
        );

        // 🚪 Logout
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}