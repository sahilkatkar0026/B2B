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
import com.example.medicalshopb2b.utils.CartManager1;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BillingAdapter
        extends RecyclerView.Adapter<BillingAdapter.VH> {

    public interface OnCartChangeListener {
        void onCartChanged();
    }

    private final OnCartChangeListener listener;
    private final SimpleDateFormat sdf =
            new SimpleDateFormat("dd MMM yyyy", Locale.US);

    public BillingAdapter(OnCartChangeListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_billing_medicine, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {

        List<CartItem> cartItems = CartManager1.getCartItems();
        if (position >= cartItems.size()) return;

        CartItem item = cartItems.get(position);

        // ================= BASIC INFO =================
        h.txtName.setText(item.getName());
        h.txtQty.setText(String.valueOf(item.getQuantity()));
        h.txtPrice.setText("₹" + item.getPrice());
        h.txtItemTotal.setText("₹" +
                (item.getPrice() * item.getQuantity()));

        // ================= MFG =================
        if (item.getMfgDate() > 0) {
            h.txtMfg.setText("MFG: " +
                    sdf.format(new Date(item.getMfgDate())));
        } else {
            h.txtMfg.setText("MFG: N/A");
        }

        // ================= EXPIRY =================
        if (item.getExpiryDate() > 0) {

            h.txtExpiry.setText("EXP: " +
                    sdf.format(new Date(item.getExpiryDate())));

            // Instead of item.isExpired() (if not present)
            if (System.currentTimeMillis() > item.getExpiryDate()) {
                h.txtExpiry.setTextColor(Color.RED);
            } else {
                h.txtExpiry.setTextColor(Color.parseColor("#DC2626"));
            }

        } else {
            h.txtExpiry.setText("EXP: N/A");
        }

        // ================= PLUS =================
        h.btnPlus.setOnClickListener(v -> {

            if (item.getQuantity() >= item.getAvailableStock()) {
                Toast.makeText(v.getContext(),
                        "Stock limit reached",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (item.getExpiryDate() > 0 &&
                    System.currentTimeMillis() > item.getExpiryDate()) {

                Toast.makeText(v.getContext(),
                        "Cannot sell expired medicine",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            item.setQuantity(item.getQuantity() + 1);
            notifyItemChanged(h.getAdapterPosition());
            listener.onCartChanged();
        });

        // ================= MINUS =================
        h.btnMinus.setOnClickListener(v -> {

            if (item.getQuantity() > 1) {
                item.setQuantity(item.getQuantity() - 1);
                notifyItemChanged(h.getAdapterPosition());
                listener.onCartChanged();
            }
        });

        // ================= REMOVE =================
        h.btnRemove.setOnClickListener(v -> {

            int currentPosition = h.getAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) return;

            CartItem removeItem = cartItems.get(currentPosition);

            // 🔥 REMOVE USING medicineKey
            CartManager1.removeItem(removeItem.getMedicineKey());

            notifyItemRemoved(currentPosition);
            notifyItemRangeChanged(currentPosition,
                    CartManager1.getCartItems().size());

            listener.onCartChanged();

            Toast.makeText(v.getContext(),
                    "Item removed",
                    Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return CartManager1.getCartItems().size();
    }

    // ================= VIEW HOLDER =================
    static class VH extends RecyclerView.ViewHolder {

        TextView txtName, txtQty, txtPrice, txtItemTotal;
        TextView txtMfg, txtExpiry;
        Button btnPlus, btnMinus, btnRemove;

        VH(View v) {
            super(v);

            txtName = v.findViewById(R.id.txtName);
            txtQty = v.findViewById(R.id.txtQty);
            txtPrice = v.findViewById(R.id.txtPrice);
            txtItemTotal = v.findViewById(R.id.txtItemTotal);

            txtMfg = v.findViewById(R.id.txtMfg);
            txtExpiry = v.findViewById(R.id.txtExpiry);

            btnPlus = v.findViewById(R.id.btnPlus);
            btnMinus = v.findViewById(R.id.btnMinus);
            btnRemove = v.findViewById(R.id.btnRemove);
        }
    }
}