package com.example.medicalshopb2b;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicalshopb2b.adapter.SupplierCartAdapter;
import com.example.medicalshopb2b.model.CartItem;
import com.example.medicalshopb2b.model.Medicine;
import com.example.medicalshopb2b.utils.SupplierCartManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;

public class CartActivity extends AppCompatActivity implements SupplierCartAdapter.OnCartUpdatedListener {

    private RecyclerView recyclerCart;
    private TextView txtTotal;
    private Button btnPlaceOrder;

    private boolean isSupplierOrder;
    private String supplierId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // ===== VIEW BINDING =====
        recyclerCart = findViewById(R.id.recyclerCart);
        txtTotal = findViewById(R.id.txtTotal);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);

        recyclerCart.setLayoutManager(new LinearLayoutManager(this));

        // ===== INTENT DATA =====
        isSupplierOrder = getIntent().getBooleanExtra("isSupplierOrder", false);
        supplierId = getIntent().getStringExtra("supplierId");

        // ===== HARD BLOCK CUSTOMER CART =====
        if (!isSupplierOrder) {
            Toast.makeText(
                    this,
                    "This cart is only for supplier orders",
                    Toast.LENGTH_SHORT
            ).show();
            finish();
            return;
        }

        setupSupplierCart();
    }

    // ================= SUPPLIER CART =================
    private void setupSupplierCart() {

        List<Medicine> items = SupplierCartManager.getItems();

        if (items == null || items.isEmpty()) {
            Toast.makeText(
                    this,
                    "Supplier cart is empty",
                    Toast.LENGTH_SHORT
            ).show();
            finish();
            return;
        }

        recyclerCart.setAdapter(new SupplierCartAdapter(items, this));

        // Calculate total
        onCartUpdated();

        btnPlaceOrder.setOnClickListener(v -> placeSupplierOrder());
    }

    // ================= CALCULATE TOTAL =================
    private int calculateTotal() {
        List<Medicine> items = SupplierCartManager.getItems();
        int total = 0;

        for (Medicine med : items) {
            int qty = SupplierCartManager.getQuantity(med.getMedicineKey());
            total += med.getPrice() * qty;
        }

        return total;
    }

    // ================= PLACE SUPPLIER ORDER =================
    private void placeSupplierOrder() {

        if (supplierId == null || supplierId.trim().isEmpty()) {
            Toast.makeText(this, "Supplier not found", Toast.LENGTH_LONG).show();
            return;
        }

        String shopId = FirebaseAuth.getInstance().getUid();
        if (shopId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_LONG).show();
            return;
        }

        // 🔥 GET CART ITEMS (not Medicine objects)
        List<CartItem> cartItems = SupplierCartManager.getCartItems();

        if (cartItems == null || cartItems.isEmpty()) {
            Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String orderId = FirebaseDatabase.getInstance()
                .getReference("supplierOrders")
                .push()
                .getKey();

        if (orderId == null) {
            Toast.makeText(this, "Order creation failed", Toast.LENGTH_LONG).show();
            return;
        }

        // Calculate total
        int total = calculateTotal();

        HashMap<String, Object> order = new HashMap<>();
        order.put("orderId", orderId);
        order.put("userId", shopId);           // ✅ SHOPKEEPER ID
        order.put("supplierId", supplierId);
        order.put("items", cartItems);         // ✅ CartItem objects with quantities
        order.put("total", total);             // ✅ Total amount
        order.put("status", "pending");
        order.put("timestamp", System.currentTimeMillis());

        FirebaseDatabase.getInstance()
                .getReference("supplierOrders")
                .child(orderId)
                .setValue(order)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(
                            this,
                            "Order placed successfully",
                            Toast.LENGTH_SHORT
                    ).show();
                    SupplierCartManager.clear();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Failed: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
    }

    @Override
    public void onCartUpdated() {
        int total = calculateTotal();
        txtTotal.setText("Total: ₹" + total);
    }
}