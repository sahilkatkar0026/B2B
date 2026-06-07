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
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicalshopb2b.R;
import com.example.medicalshopb2b.UpdateMedicineActivity;
import com.example.medicalshopb2b.model.Medicine;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Locale;

public class SupplierInventoryAdapter
        extends RecyclerView.Adapter<SupplierInventoryAdapter.VH> {

    private final List<Medicine> medicineList;
    private final Context context;

    // ================= CONSTRUCTOR =================
    public SupplierInventoryAdapter(List<Medicine> medicineList, Context context) {
        this.medicineList = medicineList;
        this.context = context;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_supplier_inventory, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        if (position < medicineList.size()) {
            Medicine medicine = medicineList.get(position);
            holder.bind(medicine, position);
        }
    }

    @Override
    public int getItemCount() {
        return medicineList == null ? 0 : medicineList.size();
    }

    // ================= VIEW HOLDER =================
    class VH extends RecyclerView.ViewHolder {

        private final TextView txtMedName;
        private final TextView txtBrand;
        private final TextView txtStockStatus;
        private final TextView txtMedStock;
        private final TextView txtUnit;
        private final TextView txtPrice;
        private final Button btnUpdateStock;
        private final Button btnDeleteMedicine;

        VH(View v) {
            super(v);
            txtMedName = v.findViewById(R.id.txtMedName);
            txtBrand = v.findViewById(R.id.txtBrand);
            txtStockStatus = v.findViewById(R.id.txtStockStatus);
            txtMedStock = v.findViewById(R.id.txtMedStock);
            txtUnit = v.findViewById(R.id.txtUnit);
            txtPrice = v.findViewById(R.id.txtPrice);
            btnUpdateStock = v.findViewById(R.id.btnUpdateStock);
            btnDeleteMedicine = v.findViewById(R.id.btnDeleteMedicine);
        }

        void bind(Medicine medicine, int position) {

            if (medicine == null) return;

            // Set medicine name
            String name = medicine.getName();
            txtMedName.setText(name.isEmpty() ? "Unknown" : name);

            // Set brand
            String brand = medicine.getBrand();
            txtBrand.setText(brand.isEmpty() ? "N/A" : brand);

            // Set stock (null-safe)
            int stock = medicine.getStock();
            txtMedStock.setText("Stock: " + stock);

            // Set stock status with smart logic
            if (stock > 10) {
                txtStockStatus.setText("In Stock");
            } else if (stock > 0) {
                txtStockStatus.setText("Low Stock");
            } else {
                txtStockStatus.setText("Out of Stock");
            }

            // Set unit (uses getUnit() method which returns "Pcs" as default)
            String unit = medicine.getUnit();
            txtUnit.setText(unit);

            // Set price (null-safe - returns 0 if null)
            int price = medicine.getPrice();
            txtPrice.setText("₹" + String.format(Locale.getDefault(), "%d", price));

            // ================= UPDATE BUTTON =================
            btnUpdateStock.setOnClickListener(v -> {
                updateMedicine(medicine);
            });

            // ================= DELETE BUTTON =================
            btnDeleteMedicine.setOnClickListener(v -> {
                showDeleteConfirmation(medicine, position);
            });
        }

        // ================= UPDATE MEDICINE =================
        private void updateMedicine(Medicine medicine) {

            if (medicine.getMedicineId().isEmpty()) {
                Toast.makeText(itemView.getContext(),
                        "Cannot update medicine",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Navigate to update activity
            Intent intent = new Intent(itemView.getContext(), UpdateMedicineActivity.class);
            intent.putExtra("medicineId", medicine.getMedicineId());
            intent.putExtra("supplierId", medicine.getSupplierId());
            intent.putExtra("name", medicine.getName());
            intent.putExtra("brand", medicine.getBrand());
            intent.putExtra("price", medicine.getPrice());
            intent.putExtra("stock", medicine.getStock());
            intent.putExtra("unit", medicine.getUnit());

            itemView.getContext().startActivity(intent);
        }

        // ================= SHOW DELETE CONFIRMATION =================
        private void showDeleteConfirmation(Medicine medicine, int position) {

            new AlertDialog.Builder(itemView.getContext())
                    .setTitle("Delete Medicine")
                    .setMessage("Are you sure you want to delete " + medicine.getName() + "?\n\n" +
                            "This will remove the medicine from your inventory.")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        checkAndDelete(medicine, position);
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .setCancelable(false)
                    .show();
        }

        // ================= CHECK AND SAFE DELETE =================
        private void checkAndDelete(Medicine medicine, int position) {

            if (medicine.getMedicineId().isEmpty() || medicine.getSupplierId().isEmpty()) {
                Toast.makeText(itemView.getContext(),
                        "Cannot delete medicine: invalid data",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Show progress
            Toast.makeText(itemView.getContext(),
                    "Checking for active reorders...",
                    Toast.LENGTH_SHORT).show();

            // 🔍 CHECK ALL SHOPS FOR REORDERS USING THIS MEDICINE
            DatabaseReference shopsRef =
                    FirebaseDatabase.getInstance().getReference("shops");

            shopsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    // Check if any shop has reordered this medicine
                    for (DataSnapshot shopSnap : snapshot.getChildren()) {

                        DataSnapshot reordersSnap = shopSnap.child("reorders");

                        // ❌ BLOCK DELETE IF ANY REORDER EXISTS
                        for (DataSnapshot reorderSnap : reordersSnap.getChildren()) {

                            String medicineIdInReorder = reorderSnap.child("medicineId")
                                    .getValue(String.class);

                            if (medicineIdInReorder != null &&
                                    medicineIdInReorder.equals(medicine.getMedicineId())) {

                                Toast.makeText(itemView.getContext(),
                                        "Cannot delete: Active reorders exist for this medicine",
                                        Toast.LENGTH_LONG).show();
                                return;
                            }
                        }
                    }

                    // ✅ SAFE TO DELETE (NO REORDERS FOUND)
                    deleteMedicineFromFirebase(medicine, position);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(itemView.getContext(),
                            "Error: " + error.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            });
        }

        // ================= DELETE FROM FIREBASE =================
        private void deleteMedicineFromFirebase(Medicine medicine, int position) {

            FirebaseDatabase.getInstance()
                    .getReference("suppliers")
                    .child(medicine.getSupplierId())
                    .child("medicines")
                    .child(medicine.getMedicineId())
                    .removeValue((error, ref) -> {

                        if (error == null) {
                            // Remove from list and update UI
                            medicineList.remove(position);
                            notifyItemRemoved(position);

                            Toast.makeText(itemView.getContext(),
                                    "Medicine deleted successfully",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(itemView.getContext(),
                                    "Error: " + error.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    // ================= PUBLIC METHODS =================

    /**
     * Update list with new data
     */
    public void updateList(List<Medicine> newList) {
        medicineList.clear();
        medicineList.addAll(newList);
        notifyDataSetChanged();
    }

    /**
     * Reset to original list
     */
    public void resetList() {
        notifyDataSetChanged();
    }
}