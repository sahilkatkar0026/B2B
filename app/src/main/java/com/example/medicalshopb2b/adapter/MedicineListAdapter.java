package com.example.medicalshopb2b.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicalshopb2b.model.Medicine;

import java.util.List;

public class MedicineListAdapter
        extends RecyclerView.Adapter<MedicineListAdapter.ViewHolder> {

    public interface OnMedicineClick {
        void onClick(Medicine medicine);
    }

    private final List<Medicine> medicineList;
    private final OnMedicineClick listener;

    public MedicineListAdapter(
            List<Medicine> medicineList,
            OnMedicineClick listener
    ) {
        this.medicineList = medicineList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        Medicine m = medicineList.get(position);

        holder.name.setText(m.getName());

        // ================= EXPIRED =================
        if (m.isExpired()) {
            holder.price.setText("Expired");
            holder.price.setTextColor(Color.RED);
            holder.itemView.setEnabled(false);
            return;
        }

        // ================= OUT OF STOCK =================
        if (m.getStock() <= 0) {
            holder.price.setText("Out of stock");
            holder.price.setTextColor(Color.RED);
            holder.itemView.setEnabled(false);
            return;
        }

        // ================= NORMAL =================
        holder.price.setText("₹" + m.getPrice() + " | Stock: " + m.getStock());
        holder.price.setTextColor(Color.DKGRAY);
        holder.itemView.setEnabled(true);

        holder.itemView.setOnClickListener(
                v -> listener.onClick(m)
        );
    }

    @Override
    public int getItemCount() {
        return medicineList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView name, price;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(android.R.id.text1);
            price = itemView.findViewById(android.R.id.text2);
        }
    }
}