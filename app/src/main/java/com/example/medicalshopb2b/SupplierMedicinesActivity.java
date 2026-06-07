package com.example.medicalshopb2b;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicalshopb2b.adapter.MedicineListAdapter;
import com.example.medicalshopb2b.model.Medicine;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SupplierMedicinesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText searchMedicines;
    private TextView txtSupplierName, txtTotalMedicines, txtInStock;
    private MaterialButton btnGoToCart;
    private LinearLayout emptyStateMedicines, loadingLayout;
    private ImageView btnBack;

    private MedicineListAdapter adapter;
    private final List<Medicine> medicineList = new ArrayList<>();
    private final List<Medicine> filteredList = new ArrayList<>();

    private String supplierId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supplier_medicines);

        supplierId = getIntent().getStringExtra("supplierId");

        if (supplierId == null) {
            Toast.makeText(
                    SupplierMedicinesActivity.this,
                    "Supplier not found",
                    Toast.LENGTH_SHORT
            ).show();
            finish();
            return;
        }

        initViews();
        setupBackButton();
        setupRecycler();
        setupSearch();
        setupClicks();
        loadSupplierInfo();
        loadMedicines();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerSupplierMedicines);
        searchMedicines = findViewById(R.id.searchMedicines);
        txtSupplierName = findViewById(R.id.txtSupplierName);
        txtTotalMedicines = findViewById(R.id.txtTotalMedicines);
        txtInStock = findViewById(R.id.txtInStock);
        btnGoToCart = findViewById(R.id.btnGoToCart);
        emptyStateMedicines = findViewById(R.id.emptyStateMedicines);
        loadingLayout = findViewById(R.id.loadingLayout);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupBackButton() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }
    }

    private void setupRecycler() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(false);

        adapter = new MedicineListAdapter(filteredList, medicine -> {
            Intent i = new Intent(
                    SupplierMedicinesActivity.this,
                    MedicineDetailsActivity.class
            );
            i.putExtra("supplierId", supplierId);
            i.putExtra("medicineId", medicine.getMedicineId());
            i.putExtra("isSupplierOrder", true);
            startActivity(i);
        });

        recyclerView.setAdapter(adapter);
    }

    private void setupSearch() {
        searchMedicines.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int st, int b, int c) {
                filter(s.toString());
            }
        });
    }

    private void filter(String q) {
        filteredList.clear();
        if (q.isEmpty()) {
            filteredList.addAll(medicineList);
        } else {
            String l = q.toLowerCase(Locale.ROOT);
            for (Medicine m : medicineList) {
                if (m.getName().toLowerCase(Locale.ROOT).contains(l) ||
                        m.getBrand().toLowerCase(Locale.ROOT).contains(l)) {
                    filteredList.add(m);
                }
            }
        }
        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void setupClicks() {
        btnGoToCart.setOnClickListener(v -> {
            Intent intent = new Intent(
                    SupplierMedicinesActivity.this,
                    CartActivity.class
            );
            intent.putExtra("isSupplierOrder", true);
            intent.putExtra("supplierId", supplierId);
            startActivity(intent);
        });
    }

    private void loadSupplierInfo() {
        FirebaseDatabase.getInstance()
                .getReference("suppliers")
                .child(supplierId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot s) {
                        String name = s.child("name").getValue(String.class);
                        if (name != null) {
                            txtSupplierName.setText(name);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError e) {
                        Toast.makeText(
                                SupplierMedicinesActivity.this,
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private void loadMedicines() {
        showLoading();

        FirebaseDatabase.getInstance()
                .getReference("suppliers")
                .child(supplierId)
                .child("medicines")
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snap) {
                        medicineList.clear();
                        filteredList.clear();

                        int inStock = 0;

                        for (DataSnapshot s : snap.getChildren()) {
                            Medicine m = s.getValue(Medicine.class);
                            if (m != null) {
                                m.setMedicineId(s.getKey());
                                m.setSupplierId(supplierId);
                                medicineList.add(m);
                                if (m.getStock() > 0) inStock++;
                            }
                        }

                        filteredList.addAll(medicineList);
                        adapter.notifyDataSetChanged();

                        txtTotalMedicines.setText(String.valueOf(medicineList.size()));
                        txtInStock.setText(String.valueOf(inStock));
                        updateEmptyState();
                        hideLoading();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError e) {
                        hideLoading();
                        Toast.makeText(
                                SupplierMedicinesActivity.this,
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private void updateEmptyState() {
        if (filteredList.isEmpty()) {
            emptyStateMedicines.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyStateMedicines.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showLoading() {
        loadingLayout.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    private void hideLoading() {
        loadingLayout.setVisibility(View.GONE);
    }
}