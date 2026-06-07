package com.example.medicalshopb2b.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicalshopb2b.R;
import com.example.medicalshopb2b.model.Supplier;

import java.util.List;

public class SupplierAdapter
        extends RecyclerView.Adapter<SupplierAdapter.SupplierViewHolder> {

    public interface OnSupplierClick {
        void onClick(String supplierId);   // ✅ MUST BE String
    }

    private final List<Supplier> list;
    private final OnSupplierClick listener;

    public SupplierAdapter(List<Supplier> list, OnSupplierClick listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SupplierViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_supplier, parent, false);

        return new SupplierViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull SupplierViewHolder holder, int position) {

        Supplier supplier = list.get(position);
        holder.txtName.setText(supplier.getName());

        holder.itemView.setOnClickListener(v ->
                listener.onClick(supplier.getSupplierId())   // ✅ STRING
        );
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class SupplierViewHolder extends RecyclerView.ViewHolder {

        TextView txtName;

        SupplierViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtSupplierName);
        }
    }
}
