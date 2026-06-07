package com.example.medicalshopb2b.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicalshopb2b.CartActivity;
import com.example.medicalshopb2b.R;
import com.example.medicalshopb2b.model.Medicine;
import com.example.medicalshopb2b.model.ReorderRequest;
import com.example.medicalshopb2b.utils.SupplierCartManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.List;

public class ReorderListAdapter
        extends RecyclerView.Adapter<ReorderListAdapter.VH> {

    private final Context context;
    private final List<ReorderRequest> list;

    public ReorderListAdapter(Context context, List<ReorderRequest> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reorder, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {

        ReorderRequest item = list.get(position);
        if (item == null) return;

        h.txtStatus.setText("PENDING");
        h.txtMedicineName.setText(item.getMedicineName());
        h.txtSuggested.setText("Suggested: " + item.getSuggestedQty() + " units");
        h.txtCurrentStock.setText("Current Stock: " + item.getCurrentStock() + " units");
        h.txtReorderLevel.setText("Reorder: " + item.getSuggestedQty() + " units");

        h.btnOrder.setOnClickListener(v -> {

            if (item.getSupplierId() == null || item.getSupplierId().isEmpty()) {
                Toast.makeText(
                        context,
                        "Supplier not linked",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            // 🔥 CRITICAL: Don't pass position directly - pass medicineKey instead
            fetchSupplierMedicineAndAddToCart(item);
        });
    }

    // =====================================================
    // 🔥 FIXED: No position parameter - removed from params
    // =====================================================
    private void fetchSupplierMedicineAndAddToCart(
            ReorderRequest reorderItem
    ) {

        String medicineId = reorderItem.getMedicineId();
        String supplierId = reorderItem.getSupplierId();
        String medicineKey = reorderItem.getMedicineKey();

        if (medicineId == null || medicineId.isEmpty()) {
            Toast.makeText(context,
                    "Medicine ID not found",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference supplierMedicinesRef = FirebaseDatabase.getInstance()
                .getReference("suppliers")
                .child(supplierId)
                .child("medicines");

        // 🔥 Try direct access first
        supplierMedicinesRef
                .child(medicineId)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (!snapshot.exists()) {
                            searchByMedicineKey(reorderItem, supplierId, medicineId);
                            return;
                        }

                        Medicine supplierMedicine = snapshot.getValue(Medicine.class);

                        if (supplierMedicine == null) {
                            Toast.makeText(context,
                                    "Medicine data invalid",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        processMedicine(supplierMedicine, reorderItem, medicineId,
                                supplierId, medicineKey);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(
                                context,
                                "Error: " + error.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    // 🔥 FALLBACK: Search by medicineKey if direct lookup fails
    private void searchByMedicineKey(
            ReorderRequest reorderItem,
            String supplierId,
            String medicineId
    ) {

        DatabaseReference supplierMedicinesRef = FirebaseDatabase.getInstance()
                .getReference("suppliers")
                .child(supplierId)
                .child("medicines");

        supplierMedicinesRef
                .orderByChild("medicineId")
                .equalTo(medicineId)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (!snapshot.exists()) {
                            Toast.makeText(context,
                                    "Medicine not found at supplier",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        for (DataSnapshot snap : snapshot.getChildren()) {

                            Medicine supplierMedicine =
                                    snap.getValue(Medicine.class);

                            if (supplierMedicine == null) continue;

                            processMedicine(supplierMedicine, reorderItem, medicineId,
                                    supplierId, reorderItem.getMedicineKey());
                            return;  // Only process first match
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(
                                context,
                                error.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    // 🔥 FIXED: Use medicineKey instead of adapterPosition
    private void processMedicine(
            Medicine supplierMedicine,
            ReorderRequest reorderItem,
            String medicineId,
            String supplierId,
            String medicineKey  // 🔥 CHANGED: Use medicineKey
    ) {

        supplierMedicine.setMedicineId(medicineId);
        supplierMedicine.setSupplierId(supplierId);

        // ✅ ADD TO SUPPLIER CART
        SupplierCartManager.addMedicine(
                supplierMedicine,
                reorderItem.getSuggestedQty()
        );

        // 🔥 DELETE REORDER FROM FIREBASE using medicineKey
        String shopId = FirebaseAuth.getInstance().getUid();

        FirebaseDatabase.getInstance()
                .getReference("shops")
                .child(shopId)
                .child("reorders")
                .child(medicineKey)  // 🔥 Use medicineKey, not position
                .removeValue()
                .addOnSuccessListener(unused -> {
                    // 🔥 REMOVE FROM LIST SAFELY: Find and remove by medicineKey
                    for (int i = 0; i < list.size(); i++) {
                        ReorderRequest req = list.get(i);
                        if (req != null && medicineKey.equals(req.getMedicineKey())) {
                            list.remove(i);
                            notifyItemRemoved(i);
                            break;  // Only remove one match
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context,
                            "Failed to remove reorder: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });

        // ✅ OPEN CART
        Intent intent = new Intent(context, CartActivity.class);
        intent.putExtra("isSupplierOrder", true);
        intent.putExtra("supplierId", supplierId);
        context.startActivity(intent);

        Toast.makeText(
                context,
                "Reorder processed",
                Toast.LENGTH_SHORT
        ).show();
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    // ================= VIEW HOLDER =================
    static class VH extends RecyclerView.ViewHolder {

        TextView txtStatus, txtMedicineName,
                txtSuggested, txtCurrentStock, txtReorderLevel;
        Button btnOrder;

        VH(@NonNull View v) {
            super(v);
            txtStatus = v.findViewById(R.id.txtStatus);
            txtMedicineName = v.findViewById(R.id.txtMedicineName);
            txtSuggested = v.findViewById(R.id.txtSuggested);
            txtCurrentStock = v.findViewById(R.id.txtCurrentStock);
            txtReorderLevel = v.findViewById(R.id.txtReorderLevel);
            btnOrder = v.findViewById(R.id.btnOrder);
        }
    }
}