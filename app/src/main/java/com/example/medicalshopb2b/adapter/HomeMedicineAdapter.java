package com.example.medicalshopb2b.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicalshopb2b.R;
import com.example.medicalshopb2b.ShopkeeperDashboardActivity;
import com.example.medicalshopb2b.model.CartItem;
import com.example.medicalshopb2b.model.Medicine;
import com.example.medicalshopb2b.utils.CartManager1;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeMedicineAdapter extends RecyclerView.Adapter<HomeMedicineAdapter.VH> {

    private final List<Medicine> list;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.US);

    public HomeMedicineAdapter(List<Medicine> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_home_medicine, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {

        Medicine m = list.get(position);

        // ================= NAME / BRAND / PRICE =================
        h.txtName.setText(m.getName());
        h.txtBrand.setText(
                m.getBrand().isEmpty()
                        ? "Brand: N/A"
                        : "Brand: " + m.getBrand()
        );
        h.txtPrice.setText("₹" + m.getPrice());

        // ================= IMAGE (BASE64) =================
        String base64 = m.getImageBase64();
        if (base64 != null && !base64.isEmpty()) {
            try {
                byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                h.imgMedicine.setImageBitmap(bmp);
            } catch (Exception e) {
                h.imgMedicine.setImageResource(R.drawable.ic_medicine_placeholder);
            }
        } else {
            h.imgMedicine.setImageResource(R.drawable.ic_medicine_placeholder);
        }

        // ================= MFG DATE =================
        if (m.getMfgDate() > 0) {
            h.txtMfg.setText(sdf.format(new Date(m.getMfgDate())));
        } else {
            h.txtMfg.setText("N/A");
        }

        // ================= EXPIRY DATE =================
        if (m.getExpiryDate() > 0) {
            h.txtExpiry.setText(sdf.format(new Date(m.getExpiryDate())));
        } else {
            h.txtExpiry.setText("N/A");
        }

        // ================= EXPIRED CHECK =================
        if (m.isExpired()) {
            h.txtStock.setText("Expired");
            h.txtStock.setTextColor(Color.RED);
            h.btnAdd.setEnabled(false);
            h.btnAdd.setText("Expired");
            h.btnAdd.setBackgroundColor(Color.GRAY);
            h.btnAdd.setOnClickListener(null);
            return;
        }

        // ================= STOCK CHECK =================
        int stock = m.getStock();

        if (stock <= 0) {
            h.txtStock.setText("Out of Stock");
            h.txtStock.setTextColor(Color.RED);
            h.btnAdd.setEnabled(false);
            h.btnAdd.setText("Out of Stock");
            h.btnAdd.setBackgroundColor(Color.GRAY);
            h.btnAdd.setOnClickListener(null);
            return;
        }

        // ================= DISPLAY STOCK STATUS =================
        if (stock <= 5) {
            h.txtStock.setText("Low: " + stock);
            h.txtStock.setTextColor(Color.parseColor("#FF9800")); // Orange
        } else {
            h.txtStock.setText("Stock: " + stock);
            h.txtStock.setTextColor(Color.parseColor("#16A34A")); // Green
        }

        // ================= ADD TO CART BUTTON =================
        h.btnAdd.setEnabled(true);
        h.btnAdd.setText("Add");
        h.btnAdd.setTextColor(Color.WHITE);
        h.btnAdd.setBackgroundColor(Color.parseColor("#2563EB")); // Blue

        h.btnAdd.setOnClickListener(v -> addToCart(v, m, stock));
    }

    // ✅ Add to cart logic
    private void addToCart(View view, Medicine medicine, int availableStock) {

        if (CartManager1.containsMedicineKey(medicine.getMedicineKey())) {
            Toast.makeText(
                    view.getContext(),
                    "Already added to bill",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        // 🔥 UPDATED CONSTRUCTOR (WITH MFG & EXPIRY)
        CartItem item = new CartItem(
                medicine.getName(),
                medicine.getBrand(),
                medicine.getPrice(),
                1,
                medicine.getMedicineKey(),
                medicine.getMedicineId(),
                medicine.getSupplierId(),
                availableStock,
                medicine.getImageBase64(),
                medicine.getMfgDate(),      // ✅ added
                medicine.getExpiryDate()    // ✅ added
        );

        CartManager1.addItem(item);

        Toast.makeText(
                view.getContext(),
                "✓ Added to bill",
                Toast.LENGTH_SHORT
        ).show();

        if (view.getContext() instanceof ShopkeeperDashboardActivity) {
            ((ShopkeeperDashboardActivity) view.getContext())
                    .switchToBillingTab();
        }
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    // ✅ Refresh data method
    public void refreshData(List<Medicine> newList) {
        list.clear();
        list.addAll(newList);
        notifyDataSetChanged();
    }

    // ================= VIEW HOLDER =================
    static class VH extends RecyclerView.ViewHolder {

        ImageView imgMedicine;
        TextView txtName, txtBrand, txtPrice, txtMfg, txtExpiry, txtStock;
        Button btnAdd;

        VH(View v) {
            super(v);
            imgMedicine = v.findViewById(R.id.imgMedicine);
            txtName = v.findViewById(R.id.txtName);
            txtBrand = v.findViewById(R.id.txtBrand);
            txtPrice = v.findViewById(R.id.txtPrice);
            txtMfg = v.findViewById(R.id.txtMfg);
            txtExpiry = v.findViewById(R.id.txtExpiry);
            txtStock = v.findViewById(R.id.txtStock);
            btnAdd = v.findViewById(R.id.btnAdd);
        }
    }
}