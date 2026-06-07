package com.example.medicalshopb2b;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.medicalshopb2b.model.CartItem;
import com.example.medicalshopb2b.model.Medicine;
import com.example.medicalshopb2b.utils.CartManager1;
import com.example.medicalshopb2b.utils.SupplierCartManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MedicineDetailsActivity extends AppCompatActivity {

    TextView txtName, txtBrand, txtPrice, txtStock,
            txtQty, txtTotalAmount, txtStockBadge, txtMfg, txtExpiry;
    ImageView ivMedicine;
    Button btnPlus, btnMinus, btnOrder;

    int quantity = 1;
    Medicine medicine;

    String supplierId, medicineId;
    boolean isSupplierOrder;
    int suggestedQty;

    DatabaseReference ref;
    ValueEventListener listener;

    SimpleDateFormat sdf =
            new SimpleDateFormat("dd MMM yyyy", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine_details);

        supplierId = getIntent().getStringExtra("supplierId");
        medicineId = getIntent().getStringExtra("medicineId");
        isSupplierOrder = getIntent().getBooleanExtra("isSupplierOrder", false);
        suggestedQty = getIntent().getIntExtra("suggestedQty", 1);

        if (supplierId == null || medicineId == null) {
            finish();
            return;
        }

        initViews();
        observeRealtime();
    }

    private void initViews() {

        ivMedicine = findViewById(R.id.ivMedicine);
        txtName = findViewById(R.id.txtName);
        txtBrand = findViewById(R.id.txtBrand);
        txtPrice = findViewById(R.id.txtPrice);
        txtStock = findViewById(R.id.txtStock);
        txtStockBadge = findViewById(R.id.txtStockBadge);
        txtQty = findViewById(R.id.txtQty);
        txtTotalAmount = findViewById(R.id.txtTotalAmount);
        txtMfg = findViewById(R.id.txtMfg);
        txtExpiry = findViewById(R.id.txtExpiry);

        btnPlus = findViewById(R.id.btnPlus);
        btnMinus = findViewById(R.id.btnMinus);
        btnOrder = findViewById(R.id.btnOrder);

        btnPlus.setOnClickListener(v -> {
            if (medicine != null && quantity < medicine.getStock()) {
                quantity++;
                updateTotal();
            }
        });

        btnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                updateTotal();
            }
        });

        btnOrder.setOnClickListener(v -> addToCart());
    }

    private void observeRealtime() {

        ref = FirebaseDatabase.getInstance()
                .getReference("suppliers")
                .child(supplierId)
                .child("medicines")
                .child(medicineId);

        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                medicine = snapshot.getValue(Medicine.class);
                if (medicine == null) {
                    finish();
                    return;
                }

                medicine.setMedicineId(medicineId);
                medicine.setSupplierId(supplierId);

                txtName.setText(medicine.getName());
                txtBrand.setText(
                        medicine.getBrand().isEmpty()
                                ? "Brand: N/A"
                                : medicine.getBrand()
                );

                txtPrice.setText("₹" + medicine.getPrice());
                txtStock.setText("Stock: " + medicine.getStock());
                txtStockBadge.setText("Stock: " + medicine.getStock());

                txtMfg.setText(
                        medicine.getMfgDate() > 0
                                ? "MFG: " + sdf.format(new Date(medicine.getMfgDate()))
                                : "MFG: N/A"
                );

                txtExpiry.setText(
                        medicine.getExpiryDate() > 0
                                ? "Expiry: " + sdf.format(new Date(medicine.getExpiryDate()))
                                : "Expiry: N/A"
                );

                String base64 = medicine.getImageBase64();
                if (base64 != null && !base64.isEmpty()) {
                    try {
                        byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
                        Bitmap bmp = BitmapFactory.decodeByteArray(
                                bytes, 0, bytes.length);
                        ivMedicine.setImageBitmap(bmp);
                    } catch (Exception e) {
                        ivMedicine.setImageResource(
                                R.drawable.ic_medicine_placeholder);
                    }
                } else {
                    ivMedicine.setImageResource(
                            R.drawable.ic_medicine_placeholder);
                }

                if (quantity == 1 && isSupplierOrder) {
                    quantity = suggestedQty;
                }

                updateTotal();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };

        ref.addValueEventListener(listener);
    }

    private void updateTotal() {
        txtQty.setText(String.valueOf(quantity));
        txtTotalAmount.setText(
                "₹" + (quantity * (medicine == null ? 0 : medicine.getPrice()))
        );
    }

    private void addToCart() {

        if (medicine == null) return;
        if (quantity > medicine.getStock()) return;

        // SUPPLIER → SUPPLIER CART
        if (isSupplierOrder) {
            SupplierCartManager.addMedicine(medicine, quantity);
            finish();
            return;
        }

        // 🔥 SHOP → BILLING CART (UPDATED 11 PARAM CONSTRUCTOR)
        CartItem item = new CartItem(
                medicine.getName(),
                medicine.getBrand(),
                medicine.getPrice(),
                quantity,
                medicine.getMedicineKey(),
                medicine.getMedicineId(),
                medicine.getSupplierId(),
                medicine.getStock(),
                medicine.getImageBase64(),
                medicine.getMfgDate(),     // ✅ ADDED
                medicine.getExpiryDate()   // ✅ ADDED
        );

        CartManager1.addItem(item);
        finish();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ref != null && listener != null) {
            ref.removeEventListener(listener);
        }
    }
}