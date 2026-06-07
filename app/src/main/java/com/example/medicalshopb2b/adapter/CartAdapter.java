package com.example.medicalshopb2b.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicalshopb2b.model.CartItem;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.VH> {

    private final List<CartItem> items;

    // ================= SINGLE SOURCE OF TRUTH =================
    // CartItem is used for BOTH:
    // 1️⃣ Customer billing
    // 2️⃣ Supplier ordering
    public CartAdapter(List<CartItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);

        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {

        CartItem item = items.get(position);

        h.title.setText(item.getName());

        h.subtitle.setText(
                "Qty: " + item.getQuantity() +
                        "   Price: ₹" + item.getPrice()
        );
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class VH extends RecyclerView.ViewHolder {

        TextView title, subtitle;

        VH(@NonNull View v) {
            super(v);
            title = v.findViewById(android.R.id.text1);
            subtitle = v.findViewById(android.R.id.text2);
        }
    }
}