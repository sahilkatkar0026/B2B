package com.example.medicalshopb2b.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicalshopb2b.R;
import com.example.medicalshopb2b.model.CartItem;
import com.example.medicalshopb2b.model.Medicine;
import com.example.medicalshopb2b.utils.CartManager1;

import java.util.List;

public class MedicineAdapter
        extends RecyclerView.Adapter<MedicineAdapter.ViewHolder> {

    private final List<Medicine> list;

    public MedicineAdapter(List<Medicine> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medicine, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder h,
            int position
    ) {

        Medicine m = list.get(position);

        h.txtName.setText(m.getName());
        h.txtPrice.setText("₹" + m.getPrice());

        // ================= EXPIRED =================
        if (m.isExpired()) {
            h.txtStock.setText("Expired");
            h.txtStock.setTextColor(Color.RED);
            h.btnAdd.setEnabled(false);
            h.btnAdd.setText("Expired");
            return;
        }

        // ================= OUT OF STOCK =================
        if (m.getStock() <= 0) {
            h.txtStock.setText("Out of Stock");
            h.txtStock.setTextColor(Color.RED);
            h.btnAdd.setEnabled(false);
            h.btnAdd.setText("Out of Stock");
            return;
        }

        // ================= STOCK =================
        h.txtStock.setText("Stock: " + m.getStock());
        h.txtStock.setTextColor(Color.DKGRAY);
        h.btnAdd.setEnabled(true);
        h.btnAdd.setText("Add to Bill");

        h.btnAdd.setOnClickListener(v -> {

            // 🔒 Prevent duplicate
            if (CartManager1.containsMedicineKey(m.getMedicineKey())) {
                Toast.makeText(
                        v.getContext(),
                        "Already added to bill",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            // 🔥 UPDATED CONSTRUCTOR (11 PARAMETERS)
            CartItem item = new CartItem(
                    m.getName(),
                    m.getBrand(),
                    m.getPrice(),
                    1,
                    m.getMedicineKey(),
                    m.getMedicineId(),
                    m.getSupplierId(),
                    m.getStock(),
                    m.getImageBase64(),
                    m.getMfgDate(),        // ✅ added
                    m.getExpiryDate()      // ✅ added
            );

            CartManager1.addItem(item);

            Toast.makeText(
                    v.getContext(),
                    "Added to bill",
                    Toast.LENGTH_SHORT
            ).show();
        });
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    // ================= VIEW HOLDER =================
    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtName, txtPrice, txtStock;
        Button btnAdd;

        ViewHolder(View v) {
            super(v);
            txtName = v.findViewById(R.id.txtName);
            txtPrice = v.findViewById(R.id.txtPrice);
            txtStock = v.findViewById(R.id.txtStock);
            btnAdd = v.findViewById(R.id.btnAddToBill);
        }
    }
}