package com.example.medicalshopb2b.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicalshopb2b.R;
import com.example.medicalshopb2b.SelectSupplierActivity;
import com.example.medicalshopb2b.model.ReorderRequest;

import java.util.List;

public class ReorderAdapter
        extends RecyclerView.Adapter<ReorderAdapter.VH> {

    private final Context context;
    private final List<ReorderRequest> list;

    // ✅ FIXED CONSTRUCTOR (2 PARAMETERS)
    public ReorderAdapter(Context context, List<ReorderRequest> list) {
        this.context = context;
        this.list = list;
        setHasStableIds(true);
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

        h.txtName.setText(item.getMedicineName());
        h.txtStock.setText("Current Stock: " + item.getCurrentStock());
        h.txtSuggested.setText("Suggested Qty: " + item.getSuggestedQty());

        h.btnOrder.setOnClickListener(v -> {

            Intent intent = new Intent(
                    context,
                    SelectSupplierActivity.class
            );

            intent.putExtra("fromReorder", true);
            intent.putExtra("medicineKey", item.getMedicineKey());
            intent.putExtra("medicineName", item.getMedicineName());
            intent.putExtra("suggestedQty", item.getSuggestedQty());

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    // ================= VIEW HOLDER =================

    static class VH extends RecyclerView.ViewHolder {

        TextView txtName, txtStock, txtSuggested;
        Button btnOrder;

        VH(@NonNull View v) {
            super(v);
            txtName = v.findViewById(R.id.txtMedicineName);
            txtStock = v.findViewById(R.id.txtStock);
            txtSuggested = v.findViewById(R.id.txtSuggested);
            btnOrder = v.findViewById(R.id.btnOrder);
        }
    }
}