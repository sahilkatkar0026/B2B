package com.example.medicalshopb2b.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.medicalshopb2b.model.Medicine;
import com.example.medicalshopb2b.model.ReorderRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Reordersyncmanager {

    private static final String TAG = "ReorderSyncManager";
    private static final int DEFAULT_MIN_STOCK = 10;
    private static final int DEFAULT_SUGGESTED_QTY = 50;

    /**
     * 🔥 START AUTOMATIC SYNC - Monitor medicine stock levels
     * Automatically creates/updates reorder requests based on stock
     */
    public static void startAutoSync() {

        String shopId = FirebaseAuth.getInstance().getUid();
        if (shopId == null) {
            Log.w(TAG, "Shop ID is null");
            return;
        }

        // 🔥 LISTEN TO MEDICINES - Real-time stock updates
        FirebaseDatabase.getInstance()
                .getReference("shops")
                .child(shopId)
                .child("medicines")
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        Log.d(TAG, "Medicines updated - checking reorder status");

                        for (DataSnapshot medicineSnap : snapshot.getChildren()) {

                            Medicine medicine = medicineSnap.getValue(Medicine.class);
                            String medicineKey = medicineSnap.getKey();

                            if (medicine != null && medicineKey != null) {
                                // 🔥 AUTO-SYNC: Check stock and create/update reorder
                                syncReorderStatus(shopId, medicineKey, medicine);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error loading medicines: " + error.getMessage());
                    }
                });
    }

    /**
     * 🔥 SYNC REORDER STATUS - Create or update reorder request
     * automatically based on current stock level
     *
     * 🔥 FIXED: Only create ONE reorder per medicine (no duplicates)
     */
    private static void syncReorderStatus(
            String shopId,
            String medicineKey,
            Medicine medicine
    ) {

        int currentStock = medicine.getStock();
        int minStock = DEFAULT_MIN_STOCK;  // Can be made configurable
        int suggestedQty = DEFAULT_SUGGESTED_QTY;

        DatabaseReference reorderRef = FirebaseDatabase.getInstance()
                .getReference("shops")
                .child(shopId)
                .child("reorders")
                .child(medicineKey);

        // 🔥 CHECK IF REORDER ALREADY EXISTS
        reorderRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                ReorderRequest reorderRequest;

                if (snapshot.exists()) {
                    // 🔥 REORDER EXISTS: Only update stock and status
                    reorderRequest = snapshot.getValue(ReorderRequest.class);
                    if (reorderRequest != null) {
                        reorderRequest.setCurrentStock(currentStock);
                        reorderRequest.setLastUpdated(System.currentTimeMillis());

                        // 🔥 IMPORTANT: Only update if stock is still low
                        if (currentStock <= minStock) {
                            // Stock is still low - keep it
                            reorderRef.setValue(reorderRequest);
                            Log.d(TAG, "Updated reorder for: " + medicine.getName() +
                                    " | Stock: " + currentStock);
                        } else {
                            // Stock is sufficient - REMOVE the reorder
                            reorderRef.removeValue();
                            Log.d(TAG, "Removed reorder for: " + medicine.getName() +
                                    " | Stock: " + currentStock);
                        }
                    }
                } else {
                    // 🔥 REORDER DOESN'T EXIST: Only create if stock is low
                    if (currentStock <= minStock) {
                        reorderRequest = new ReorderRequest(
                                medicine.getMedicineId(),
                                medicine.getName(),
                                currentStock,
                                minStock,
                                suggestedQty
                        );
                        reorderRequest.setMedicineKey(medicineKey);
                        reorderRequest.setBrand(medicine.getBrand());
                        reorderRequest.setPrice(medicine.getPrice());
                        reorderRequest.setSupplierId(medicine.getSupplierId());
                        reorderRequest.setMaxStockLevel(DEFAULT_SUGGESTED_QTY * 2);
                        reorderRequest.setStatus("pending");

                        reorderRef.setValue(reorderRequest);
                        Log.d(TAG, "Created new reorder for: " + medicine.getName() +
                                " | Stock: " + currentStock);
                    }
                    // If stock is high and no reorder exists, do nothing
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error checking reorder: " + error.getMessage());
            }
        });
    }

    /**
     * 🔥 UPDATE MEDICINE STOCK - Call this when stock changes
     * It will automatically trigger reorder sync
     */
    public static void updateMedicineStock(
            String medicineKey,
            int newStock
    ) {

        String shopId = FirebaseAuth.getInstance().getUid();
        if (shopId == null) return;

        FirebaseDatabase.getInstance()
                .getReference("shops")
                .child(shopId)
                .child("medicines")
                .child(medicineKey)
                .child("stock")
                .setValue(newStock)
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "Stock updated for: " + medicineKey);
                    // The ValueEventListener will automatically sync reorder
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update stock: " + e.getMessage());
                });
    }

    /**
     * 🔥 MARK REORDER AS ORDERED
     */
    public static void markReorderAsOrdered(String medicineKey) {

        String shopId = FirebaseAuth.getInstance().getUid();
        if (shopId == null) return;

        FirebaseDatabase.getInstance()
                .getReference("shops")
                .child(shopId)
                .child("reorders")
                .child(medicineKey)
                .child("status")
                .setValue("ordered");
    }

    /**
     * 🔥 MARK REORDER AS COMPLETED (Stock received)
     */
    public static void markReorderAsCompleted(String medicineKey) {

        String shopId = FirebaseAuth.getInstance().getUid();
        if (shopId == null) return;

        FirebaseDatabase.getInstance()
                .getReference("shops")
                .child(shopId)
                .child("reorders")
                .child(medicineKey)
                .child("status")
                .setValue("completed");
    }

    /**
     * 🔥 CONFIGURE MIN STOCK LEVEL for specific medicine
     */
    public static void setMinStockLevel(String medicineKey, int minStock) {

        String shopId = FirebaseAuth.getInstance().getUid();
        if (shopId == null) return;

        FirebaseDatabase.getInstance()
                .getReference("shops")
                .child(shopId)
                .child("reorders")
                .child(medicineKey)
                .child("minStockLevel")
                .setValue(minStock);
    }
}