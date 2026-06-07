package com.example.medicalshopb2b;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicalshopb2b.adapter.SupplierAdapter;
import com.example.medicalshopb2b.model.Supplier;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SelectSupplierActivity extends AppCompatActivity {

    private RecyclerView recyclerSuppliers;
    private SupplierAdapter adapter;
    private final List<Supplier> supplierList = new ArrayList<>();

    private ImageView btnBack;
    private TextView txtReorderBadge;
    private CardView cardReorderInfo;
    private TextView txtMedicineName, txtSuggestedQty;
    private LinearLayout emptyStateLayout, loadingLayout;

    // 🔥 REORDER DATA
    private boolean fromReorder;
    private String medicineKey;
    private String medicineName;
    private int suggestedQty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_supplier);

        // ================= INIT UI =================
        recyclerSuppliers = findViewById(R.id.recyclerSuppliers);
        btnBack = findViewById(R.id.btnBack);
        txtReorderBadge = findViewById(R.id.txtReorderBadge);
        cardReorderInfo = findViewById(R.id.cardReorderInfo);
        txtMedicineName = findViewById(R.id.txtMedicineName);
        txtSuggestedQty = findViewById(R.id.txtSuggestedQty);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        loadingLayout = findViewById(R.id.loadingLayout);

        recyclerSuppliers.setLayoutManager(new LinearLayoutManager(this));

        // ================= READ INTENT =================
        fromReorder = getIntent().getBooleanExtra("fromReorder", false);
        medicineKey = getIntent().getStringExtra("medicineKey");   // ✅ CORRECT
        medicineName = getIntent().getStringExtra("medicineName");
        suggestedQty = getIntent().getIntExtra("suggestedQty", 0);

        setupUI();

        btnBack.setOnClickListener(v -> finish());

        // ================= ADAPTER =================
        adapter = new SupplierAdapter(supplierList, supplierId -> {

            Intent intent = new Intent(
                    SelectSupplierActivity.this,
                    SupplierMedicinesActivity.class
            );

            intent.putExtra("supplierId", supplierId);

            if (fromReorder) {
                intent.putExtra("fromReorder", true);
                intent.putExtra("medicineKey", medicineKey);
                intent.putExtra("medicineName", medicineName);
                intent.putExtra("suggestedQty", suggestedQty);
            }

            startActivity(intent);
        });

        recyclerSuppliers.setAdapter(adapter);

        loadSuppliers();
    }

    // ================= UI SETUP =================

    private void setupUI() {
        if (fromReorder) {
            txtReorderBadge.setVisibility(View.VISIBLE);
            cardReorderInfo.setVisibility(View.VISIBLE);
            txtMedicineName.setText("Medicine: " + medicineName);
            txtSuggestedQty.setText("Suggested Qty: " + suggestedQty);
        } else {
            txtReorderBadge.setVisibility(View.GONE);
            cardReorderInfo.setVisibility(View.GONE);
        }
    }

    // ================= LOAD SUPPLIERS =================

    private void loadSuppliers() {

        loadingLayout.setVisibility(View.VISIBLE);
        emptyStateLayout.setVisibility(View.GONE);
        recyclerSuppliers.setVisibility(View.GONE);

        FirebaseDatabase.getInstance()
                .getReference("suppliers")
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        supplierList.clear();

                        for (DataSnapshot supplierSnap : snapshot.getChildren()) {

                            // 🔥 FILTER FOR REORDER MODE
                            if (fromReorder) {

                                boolean medicineFound = false;

                                for (DataSnapshot medSnap :
                                        supplierSnap.child("medicines").getChildren()) {

                                    String key =
                                            medSnap.child("medicineKey")
                                                    .getValue(String.class);

                                    if (medicineKey != null &&
                                            medicineKey.equals(key)) {
                                        medicineFound = true;
                                        break;
                                    }
                                }

                                if (!medicineFound) continue;
                            }

                            Supplier supplier =
                                    supplierSnap.getValue(Supplier.class);

                            if (supplier != null) {
                                supplier.setSupplierId(supplierSnap.getKey());
                                supplierList.add(supplier);
                            }
                        }

                        loadingLayout.setVisibility(View.GONE);

                        if (supplierList.isEmpty()) {
                            emptyStateLayout.setVisibility(View.VISIBLE);
                        } else {
                            recyclerSuppliers.setVisibility(View.VISIBLE);
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        loadingLayout.setVisibility(View.GONE);
                        Toast.makeText(
                                SelectSupplierActivity.this,
                                error.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }
}