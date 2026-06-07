package com.example.medicalshopb2b.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicalshopb2b.R;
import com.example.medicalshopb2b.model.Medicine;

import java.util.List;

public class SupplierAnalyticsAdapter extends RecyclerView.Adapter<SupplierAnalyticsAdapter.VH> {

    private final List<Medicine> list;

    public SupplierAnalyticsAdapter(List<Medicine> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_supplier_analytics, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Medicine m = list.get(position);
        holder.txtName.setText(m.getName());
        holder.txtSold.setText("Total Sold: " + m.getTotalSold());
        holder.txtStock.setText("Current Stock: " + m.getStock());
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtName, txtSold, txtStock;

        VH(View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtMedName);
            txtSold = itemView.findViewById(R.id.txtTotalSold);
            txtStock = itemView.findViewById(R.id.txtCurrentStock);
        }
    }
}