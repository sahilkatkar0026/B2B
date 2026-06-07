package com.example.medicalshopb2b.shopkeeper;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicalshopb2b.R;
import com.example.medicalshopb2b.adapter.HomeMedicineAdapter;
import com.example.medicalshopb2b.model.Medicine;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private RecyclerView recycler;
    private ProgressBar progressBar;
    private EditText etSearch;
    private HomeMedicineAdapter adapter;

    private final List<Medicine> medicineList = new ArrayList<>();
    private final List<Medicine> filteredList = new ArrayList<>();

    private DatabaseReference shopMedicinesRef;
    private ValueEventListener medicinesListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recycler = view.findViewById(R.id.recyclerMedicines);
        progressBar = view.findViewById(R.id.progressBar);
        etSearch = view.findViewById(R.id.etSearch);

        recycler.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new HomeMedicineAdapter(filteredList);
        recycler.setAdapter(adapter);

        // 🔥 SETUP SEARCH FUNCTIONALITY
        setupSearchListener();

        loadMedicines();
    }

    // 🔥 SEARCH IMPLEMENTATION
    private void setupSearchListener() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterMedicines(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    // 🔥 FILTER MEDICINES BY SEARCH QUERY
    private void filterMedicines(String query) {
        filteredList.clear();

        if (query.isEmpty()) {
            // If search is empty, show all medicines
            filteredList.addAll(medicineList);
        } else {
            // Filter medicines by name, brand, or medicine ID
            for (Medicine medicine : medicineList) {
                if (medicine.getName().toLowerCase().contains(query.toLowerCase()) ||
                        medicine.getBrand().toLowerCase().contains(query.toLowerCase()) ||
                        medicine.getMedicineKey().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(medicine);
                }
            }
        }

        adapter.notifyDataSetChanged();

        // Show message if no results found
        if (filteredList.isEmpty() && !query.isEmpty()) {
            Log.d(TAG, "No medicines found for query: " + query);
        }
    }

    private void loadMedicines() {

        if (!isAdded()) return;

        String shopId = FirebaseAuth.getInstance().getUid();

        if (shopId == null) {
            Toast.makeText(getContext(),
                    "User not logged in",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        shopMedicinesRef = FirebaseDatabase.getInstance()
                .getReference()
                .child("shops")
                .child(shopId)
                .child("medicines");

        medicinesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                medicineList.clear();

                for (DataSnapshot snap : snapshot.getChildren()) {
                    Medicine medicine = snap.getValue(Medicine.class);
                    if (medicine != null) {
                        medicineList.add(medicine);
                    }
                }

                // 🔥 UPDATE FILTERED LIST WITH NEW DATA
                filterMedicines(etSearch.getText().toString().trim());

                progressBar.setVisibility(View.GONE);

                if (medicineList.isEmpty()) {
                    Toast.makeText(getContext(),
                            "No medicines in shop inventory",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(),
                        "Error: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        };

        shopMedicinesRef.addValueEventListener(medicinesListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (shopMedicinesRef != null && medicinesListener != null) {
            shopMedicinesRef.removeEventListener(medicinesListener);
        }
    }
}