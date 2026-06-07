package com.example.medicalshopb2b.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicalshopb2b.R;
import com.example.medicalshopb2b.model.Medicine;
import com.example.medicalshopb2b.utils.SupplierCartManager;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class SupplierCartAdapter extends RecyclerView.Adapter<SupplierCartAdapter.ViewHolder> {

    private final List<Medicine> list;
    private OnCartUpdatedListener listener;

    // ✅ Callback to update total in parent fragment/activity
    public interface OnCartUpdatedListener {
        void onCartUpdated();
    }

    public SupplierCartAdapter(List<Medicine> list, OnCartUpdatedListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart_supplier, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Medicine medicine = list.get(position);

        // Get quantity from SupplierCartManager
        int qty = SupplierCartManager.getQuantity(medicine.getMedicineKey());

        holder.txtName.setText(medicine.getName());
        holder.txtBrand.setText("Brand: " + medicine.getBrand());
        holder.txtPrice.setText("₹" + medicine.getPrice());
        holder.txtQuantity.setText(String.valueOf(qty));
        updateSubtotal(holder, medicine, qty);

        // ✅ Increase quantity
        holder.btnPlus.setOnClickListener(v -> {
            int current = SupplierCartManager.getQuantity(medicine.getMedicineKey());
            SupplierCartManager.setQuantity(medicine.getMedicineKey(), current + 1);
            int updated = SupplierCartManager.getQuantity(medicine.getMedicineKey());
            holder.txtQuantity.setText(String.valueOf(updated));
            updateSubtotal(holder, medicine, updated);
            if (listener != null) listener.onCartUpdated();
        });

        // ✅ Decrease quantity — min 1
        holder.btnMinus.setOnClickListener(v -> {
            int current = SupplierCartManager.getQuantity(medicine.getMedicineKey());
            if (current > 1) {
                SupplierCartManager.setQuantity(medicine.getMedicineKey(), current - 1);
                int updated = SupplierCartManager.getQuantity(medicine.getMedicineKey());
                holder.txtQuantity.setText(String.valueOf(updated));
                updateSubtotal(holder, medicine, updated);
                if (listener != null) listener.onCartUpdated();
            }
        });

        // ✅ Remove from cart
        holder.btnRemove.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_ID) {
                SupplierCartManager.remove(medicine.getMedicineKey());
                list.remove(pos);
                notifyItemRemoved(pos);
                notifyItemRangeChanged(pos, list.size());
                if (listener != null) listener.onCartUpdated();
            }
        });
    }

    private void updateSubtotal(ViewHolder holder, Medicine medicine, int qty) {
        double subtotal = medicine.getPrice() * qty;
        holder.txtSubtotal.setText("₹" + subtotal);
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtName, txtBrand, txtPrice, txtQuantity, txtSubtotal;
        ImageButton btnPlus, btnMinus;   // ✅ matches item_cart_supplier XML
        MaterialButton btnRemove;         // ✅ matches item_cart_supplier XML

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName     = itemView.findViewById(R.id.txtMedicineName);
            txtBrand    = itemView.findViewById(R.id.txtMedicineBrand);
            txtPrice    = itemView.findViewById(R.id.txtMedicinePrice);
            txtQuantity = itemView.findViewById(R.id.txtMedicineQuantity);
            txtSubtotal = itemView.findViewById(R.id.txtMedicineSubtotal);
            btnPlus     = itemView.findViewById(R.id.btnPlus);
            btnMinus    = itemView.findViewById(R.id.btnMinus);
            btnRemove   = itemView.findViewById(R.id.btnRemoveFromCart);
        }
    }
}