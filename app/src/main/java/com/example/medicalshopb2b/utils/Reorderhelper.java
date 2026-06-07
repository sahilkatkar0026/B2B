package com.example.medicalshopb2b.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.medicalshopb2b.model.Medicine;
import com.example.medicalshopb2b.model.ReorderRequest;
import com.google.firebase.database.*;

/**
 * 🔥 REORDER HELPER - Complete Auto-Reorder System
 *
 * Automatically creates/deletes reorder requests based on stock levels
 * Uses ReorderPredictor for smart quantity calculation
 *
 * Flow:
 * 1. Stock changes in shop medicines
 * 2. Call checkAndGenerateReorder(shopId, medicineId)
 * 3. Helper fetches data and decides to CREATE or DELETE reorder
 * 4. Uses ReorderPredictor for quantity calculation
 */
public class Reorderhelper {

    private static final String TAG = "ReorderHelper";
    private static final int STOCK_THRESHOLD = 5;  // Trigger reorder when stock ≤ 5

    /**
     * 🔥 MAIN ENTRY POINT
     * Call this AFTER ANY stock change
     *
     * Usage in adapter/activity:
     * ReorderHelper.checkAndGenerateReorder(shopId, medicineId);
     */
    public static void checkAndGenerateReorder(
            String shopId,
            String medicineId
    ) {

        if (shopId == null || medicineId == null) {
            Log.w(TAG, "shopId or medicineId is null");
            return;
        }

        // 🔥 STEP 1: Get medicine details from shop
        DatabaseReference shopMedRef = FirebaseDatabase.getInstance()
                .getReference("shops")
                .child(shopId)
                .child("medicines")
                .child(medicineId);

        shopMedRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snap) {

                        if (!snap.exists()) {
                            Log.w(TAG, "Medicine not found in shop: " + medicineId);
                            return;
                        }

                        // Get stock and supplier info
                        Integer stock = snap.child("stock").getValue(Integer.class);
                        String supplierId = snap.child("supplierId").getValue(String.class);

                        if (supplierId == null || supplierId.isEmpty()) {
                            Log.w(TAG, "Supplier missing for medicine: " + medicineId);
                            return;
                        }

                        if (stock == null) stock = 0;

                        Log.d(TAG, "🔍 Checking reorder for medicineId: " + medicineId +
                                " | Stock: " + stock + " | Supplier: " + supplierId);

                        // 🔥 DECISION TREE
                        if (stock > STOCK_THRESHOLD) {
                            // ✅ STOCK RECOVERED - DELETE REORDER
                            Log.d(TAG, "✅ Stock recovered (>5) - removing reorder");
                            deleteReorderForMedicine(shopId, supplierId, medicineId);
                        } else {
                            // 🔥 STOCK LOW - CREATE/UPDATE REORDER
                            Log.d(TAG, "🔥 Stock low (≤5) - creating reorder");
                            createReorderRequest(shopId, supplierId, medicineId, stock);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error fetching shop medicine: " + error.getMessage());
                    }
                }
        );
    }

    /**
     * 🔥 CREATE REORDER REQUEST
     * Fetches supplier medicine data and creates reorder with predicted quantity
     */
    private static void createReorderRequest(
            String shopId,
            String supplierId,
            String medicineId,
            int currentStock
    ) {

        // 🔥 STEP 2: Get medicine from supplier to get medicineKey & sales data
        FirebaseDatabase.getInstance()
                .getReference("suppliers")
                .child(supplierId)
                .child("medicines")
                .child(medicineId)
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snap) {

                                Medicine supplierMedicine = snap.getValue(Medicine.class);
                                if (supplierMedicine == null) {
                                    Log.w(TAG, "Medicine not found in supplier");
                                    return;
                                }

                                String medicineKey = supplierMedicine.getMedicineKey();
                                if (medicineKey == null || medicineKey.isEmpty()) {
                                    Log.w(TAG, "MedicineKey is empty");
                                    return;
                                }

                                // 🤖 USE PREDICTOR - Smart quantity calculation
                                int suggestedQty = ReorderPredictor.predict(
                                        supplierMedicine,
                                        currentStock
                                );
                                String urgency = ReorderPredictor.getUrgencyLevel(currentStock);

                                // 🔥 CREATE REORDER REQUEST
                                ReorderRequest req = new ReorderRequest();
                                req.setMedicineKey(medicineKey);
                                req.setMedicineId(medicineId);
                                req.setMedicineName(supplierMedicine.getName());
                                req.setBrand(supplierMedicine.getBrand());
                                req.setPrice(supplierMedicine.getPrice());
                                req.setCurrentStock(currentStock);
                                req.setMinStockLevel(STOCK_THRESHOLD);
                                req.setSuggestedQty(suggestedQty);
                                req.setStatus("pending");
                                req.setCreatedAt(System.currentTimeMillis());
                                req.setLastUpdated(System.currentTimeMillis());
                                req.setAutoReorderEnabled(true);
                                req.setSupplierId(supplierId);

                                // 🔥 SAVE TO FIREBASE
                                FirebaseDatabase.getInstance()
                                        .getReference("shops")
                                        .child(shopId)
                                        .child("reorders")
                                        .child(medicineKey)
                                        .setValue(req)
                                        .addOnSuccessListener(unused -> {
                                            Log.d(TAG, "✅ Reorder CREATED for: " +
                                                    supplierMedicine.getName() +
                                                    " | Qty: " + suggestedQty +
                                                    " | Urgency: " + urgency);
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "❌ Failed to create reorder: " +
                                                    e.getMessage());
                                        });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e(TAG, "Error fetching supplier medicine: " +
                                        error.getMessage());
                            }
                        }
                );
    }

    /**
     * ✅ DELETE REORDER REQUEST
     * Called when stock recovers above threshold
     */
    private static void deleteReorderForMedicine(
            String shopId,
            String supplierId,
            String medicineId
    ) {

        // 🔥 STEP 1: Get medicineKey from supplier
        FirebaseDatabase.getInstance()
                .getReference("suppliers")
                .child(supplierId)
                .child("medicines")
                .child(medicineId)
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snap) {

                                Medicine m = snap.getValue(Medicine.class);
                                if (m == null) {
                                    Log.w(TAG, "Medicine not found in supplier");
                                    return;
                                }

                                String medicineKey = m.getMedicineKey();
                                if (medicineKey == null || medicineKey.isEmpty()) {
                                    Log.w(TAG, "MedicineKey is empty");
                                    return;
                                }

                                // 🔥 STEP 2: Delete reorder
                                deleteReorderRequest(shopId, medicineKey);

                                Log.d(TAG, "✅ Reorder REMOVED (stock recovered): " +
                                        m.getName());
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e(TAG, "Error fetching supplier medicine: " +
                                        error.getMessage());
                            }
                        }
                );
    }

    /**
     * 🗑️ DELETE REORDER - Direct method
     * Use when you already have medicineKey
     */
    public static void deleteReorderRequest(
            String shopId,
            String medicineKey
    ) {

        if (shopId == null || medicineKey == null) {
            Log.w(TAG, "shopId or medicineKey is null");
            return;
        }

        FirebaseDatabase.getInstance()
                .getReference("shops")
                .child(shopId)
                .child("reorders")
                .child(medicineKey)
                .removeValue()
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "✅ Reorder deleted for: " + medicineKey);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Failed to delete reorder: " + e.getMessage());
                });
    }

    /**
     * 📊 UPDATE REORDER STATUS
     * Mark as ordered, completed, etc
     */
    public static void updateReorderStatus(
            String shopId,
            String medicineKey,
            String status
    ) {

        if (shopId == null || medicineKey == null) return;

        FirebaseDatabase.getInstance()
                .getReference("shops")
                .child(shopId)
                .child("reorders")
                .child(medicineKey)
                .child("status")
                .setValue(status);
    }
}