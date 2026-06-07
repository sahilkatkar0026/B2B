package com.example.medicalshopb2b;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicalshopb2b.adapter.SupplierAdapter;
import com.example.medicalshopb2b.model.Supplier;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class OrderMedicineFragment extends Fragment {

    private RecyclerView recyclerSuppliers;
    private LinearLayout emptyStateLayout;
    private LinearLayout loadingLayout;

    private List<Supplier> supplierList;
    private SupplierAdapter adapter;

    private DatabaseReference suppliersRef;
    private ValueEventListener supplierListener;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_order_medicine, container, false);

        // Initialize views
        recyclerSuppliers = view.findViewById(R.id.recyclerSuppliers);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        loadingLayout = view.findViewById(R.id.loadingLayout);

        recyclerSuppliers.setLayoutManager(new LinearLayoutManager(getContext()));
        supplierList = new ArrayList<>();

        adapter = new SupplierAdapter(supplierList, supplierId -> {

            if (!isAdded()) return;

            Intent intent = new Intent(getContext(), SupplierMedicinesActivity.class);
            intent.putExtra("supplierId", supplierId);
            startActivity(intent);
        });

        recyclerSuppliers.setAdapter(adapter);

        suppliersRef = FirebaseDatabase.getInstance().getReference("suppliers");

        loadSuppliers();

        return view;
    }

    private void loadSuppliers() {

        showLoading();

        supplierListener = new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (!isAdded()) return;

                supplierList.clear();

                if (snapshot.exists() && snapshot.getChildrenCount() > 0) {

                    for (DataSnapshot snap : snapshot.getChildren()) {

                        Supplier supplier = snap.getValue(Supplier.class);

                        if (supplier != null) {
                            supplier.setSupplierId(snap.getKey());
                            supplierList.add(supplier);
                        }
                    }

                    showContent();
                    adapter.notifyDataSetChanged();

                } else {
                    showEmptyState();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                if (!isAdded()) return;

                showEmptyState();

                Toast.makeText(
                        getContext(),
                        "Permission denied or error: " + error.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        };

        suppliersRef.addValueEventListener(supplierListener);
    }

    private void showLoading() {
        loadingLayout.setVisibility(View.VISIBLE);
        recyclerSuppliers.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.GONE);
    }

    private void showContent() {
        loadingLayout.setVisibility(View.GONE);
        recyclerSuppliers.setVisibility(View.VISIBLE);
        emptyStateLayout.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        loadingLayout.setVisibility(View.GONE);
        recyclerSuppliers.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // 🔥 IMPORTANT: Remove Firebase listener to prevent crash
        if (suppliersRef != null && supplierListener != null) {
            suppliersRef.removeEventListener(supplierListener);
        }
    }
}