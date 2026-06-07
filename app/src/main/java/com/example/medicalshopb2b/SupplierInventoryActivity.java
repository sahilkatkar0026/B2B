package com.example.medicalshopb2b;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicalshopb2b.adapter.SupplierInventoryAdapter;
import com.example.medicalshopb2b.model.Medicine;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SupplierInventoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText searchMedicines;
    private SupplierInventoryAdapter adapter;
    private List<Medicine> allMedicines = new ArrayList<>();
    private List<Medicine> displayList = new ArrayList<>();
    private LinearLayout emptyState;
    private FrameLayout loadingContainer;
    private String supplierId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supplier_inventory);

        // Initialize UI
        initViews();

        // Get supplier ID
        supplierId = FirebaseAuth.getInstance().getUid();
        if (supplierId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Setup RecyclerView with displayList
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SupplierInventoryAdapter(displayList, this);
        recyclerView.setAdapter(adapter);

        // Setup search
        setupSearch();

        // Load medicines
        loadMedicines();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerInventory);
        searchMedicines = findViewById(R.id.searchMedicines);
        emptyState = findViewById(R.id.emptyState);
        loadingContainer = findViewById(R.id.loadingContainer);
    }

    private void setupSearch() {
        searchMedicines.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void performSearch(String query) {
        displayList.clear();

        if (query.isEmpty()) {
            // 🔥 SHOW ALL MEDICINES
            displayList.addAll(allMedicines);
        } else {
            // 🔥 FILTER MEDICINES
            String lowerQuery = query.toLowerCase(Locale.ROOT);
            for (Medicine med : allMedicines) {
                if (med.getName().toLowerCase(Locale.ROOT).contains(lowerQuery) ||
                        med.getBrand().toLowerCase(Locale.ROOT).contains(lowerQuery) ||
                        (med.getMedicineId() != null &&
                                med.getMedicineId().toLowerCase(Locale.ROOT).contains(lowerQuery))) {
                    displayList.add(med);
                }
            }
        }

        // 🔥 NOTIFY ADAPTER
        adapter.notifyDataSetChanged();
        updateUI();
    }

    private void updateUI() {
        if (displayList.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void loadMedicines() {
        // 🔥 SHOW LOADING
        loadingContainer.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);

        FirebaseDatabase.getInstance()
                .getReference("suppliers")
                .child(supplierId)
                .child("medicines")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        allMedicines.clear();
                        displayList.clear();

                        // 🔥 LOAD ALL MEDICINES
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Medicine med = snap.getValue(Medicine.class);
                            if (med != null) {
                                med.setMedicineId(snap.getKey());
                                allMedicines.add(med);
                            }
                        }

                        // 🔥 DISPLAY ALL IMMEDIATELY
                        displayList.addAll(allMedicines);
                        adapter.notifyDataSetChanged();

                        // 🔥 HIDE LOADING AND SHOW RESULTS
                        loadingContainer.setVisibility(View.GONE);
                        updateUI();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        loadingContainer.setVisibility(View.GONE);
                        Toast.makeText(SupplierInventoryActivity.this,
                                "Error: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        searchMedicines.setText("");
    }
}