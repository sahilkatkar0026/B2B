package com.example.medicalshopb2b.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicalshopb2b.R;
import com.example.medicalshopb2b.model.CartItem;

import java.util.List;

public class OrderItemEditAdapter
        extends RecyclerView.Adapter<OrderItemEditAdapter.VH> {

    private final List<CartItem> items;

    public OrderItemEditAdapter(List<CartItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_edit, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {

        CartItem item = items.get(position);

        h.txtName.setText(item.getName());
        h.txtPrice.setText("₹" + item.getPrice());
        h.etQty.setText(String.valueOf(item.getQuantity()));

        h.btnPlus.setOnClickListener(v -> {
            int q = item.getQuantity() + 1;
            item.setQuantity(q);
            h.etQty.setText(String.valueOf(q));
        });

        h.btnMinus.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                int q = item.getQuantity() - 1;
                item.setQuantity(q);
                h.etQty.setText(String.valueOf(q));
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public List<CartItem> getUpdatedItems() {
        return items;
    }

    static class VH extends RecyclerView.ViewHolder {

        TextView txtName, txtPrice;
        EditText etQty;
        ImageButton btnPlus, btnMinus;

        VH(View v) {
            super(v);
            txtName = v.findViewById(R.id.txtName);
            txtPrice = v.findViewById(R.id.txtPrice);
            etQty = v.findViewById(R.id.etQty);
            btnPlus = v.findViewById(R.id.btnPlus);
            btnMinus = v.findViewById(R.id.btnMinus);
        }
    }
}