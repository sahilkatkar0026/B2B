package com.example.medicalshopb2b.shopkeeper;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicalshopb2b.R;
import com.example.medicalshopb2b.adapter.BillingAdapter;
import com.example.medicalshopb2b.model.CartItem;
import com.example.medicalshopb2b.utils.BillPdfGenerator;
import com.example.medicalshopb2b.utils.CartManager1;
import com.example.medicalshopb2b.utils.Reorderhelper;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BillingFragment extends Fragment {

    private RecyclerView recycler;
    private BillingAdapter adapter;

    private TextView txtTotal, txtEmpty;
    private TextView txtSubtotal, txtDiscountAmount, txtGSTAmount;
    private TextInputEditText etCustomerName, etCustomerMobile;
    private TextInputEditText etDiscount, etGST;
    private TextInputLayout tilCustomerMobile;

    private DatabaseReference shopRef;
    private String shopId;
    private Button btnGenerate;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_billing, container, false);

        recycler = v.findViewById(R.id.recyclerBilling);
        txtTotal = v.findViewById(R.id.txtTotalAmount);
        txtEmpty = v.findViewById(R.id.txtEmptyMessage);
        txtSubtotal = v.findViewById(R.id.txtSubtotal);
        txtDiscountAmount = v.findViewById(R.id.txtDiscountAmount);
        txtGSTAmount = v.findViewById(R.id.txtGSTAmount);

        etCustomerName = v.findViewById(R.id.etCustomerName);
        etCustomerMobile = v.findViewById(R.id.etCustomerMobile);
        etDiscount = v.findViewById(R.id.etDiscount);
        etGST = v.findViewById(R.id.etGST);
        tilCustomerMobile = v.findViewById(R.id.tilCustomerMobile);

        btnGenerate = v.findViewById(R.id.btnGenerateBill);

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new BillingAdapter(this::updateUI);
        recycler.setAdapter(adapter);

        shopId = FirebaseAuth.getInstance().getUid();
        shopRef = FirebaseDatabase.getInstance()
                .getReference("shops")
                .child(shopId);

        setupMobileValidation();
        setupGSTDiscountListeners();

        btnGenerate.setOnClickListener(vw -> generateBill());

        updateUI();
        return v;
    }

    // ================= GENERATE BILL =================

    private void generateBill() {

        String customerName = etCustomerName.getText().toString().trim();
        String customerMobile = etCustomerMobile.getText().toString().trim();

        if (customerName.isEmpty() || !validateMobileNumber(customerMobile)) {
            Toast.makeText(getContext(), "Enter valid customer details", Toast.LENGTH_SHORT).show();
            return;
        }

        List<CartItem> items = CartManager1.getCartItems();
        if (items.isEmpty()) {
            Toast.makeText(getContext(), "Cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        btnGenerate.setEnabled(false);

        processItemSequentially(0, items, customerName, customerMobile);
    }

    // ================= SAFE STOCK UPDATE =================

    private void processItemSequentially(int index,
                                         List<CartItem> items,
                                         String customerName,
                                         String customerMobile) {

        if (index >= items.size()) {
            saveBill(customerName, customerMobile, items);
            btnGenerate.setEnabled(true);
            return;
        }

        CartItem item = items.get(index);

        DatabaseReference stockRef = shopRef
                .child("medicines")
                .child(item.getMedicineId())
                .child("stock");

        stockRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (!snapshot.exists()) {
                    Toast.makeText(getContext(),
                            "Stock not found for " + item.getName(),
                            Toast.LENGTH_LONG).show();
                    btnGenerate.setEnabled(true);
                    return;
                }

                long stock = snapshot.getValue(Long.class);

                if (stock < item.getQuantity()) {
                    Toast.makeText(getContext(),
                            "Insufficient stock for " + item.getName(),
                            Toast.LENGTH_LONG).show();
                    btnGenerate.setEnabled(true);
                    return;
                }

                long newStock = stock - item.getQuantity();

                stockRef.setValue(newStock)
                        .addOnSuccessListener(unused -> {

                            Reorderhelper.checkAndGenerateReorder(
                                    shopId,
                                    item.getMedicineId()
                            );

                            processItemSequentially(index + 1,
                                    items,
                                    customerName,
                                    customerMobile);

                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(),
                                    "Stock update failed: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                            btnGenerate.setEnabled(true);
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(),
                        "Database error: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
                btnGenerate.setEnabled(true);
            }
        });
    }

    // ================= SAVE BILL =================

    private void saveBill(String customerName,
                          String customerMobile,
                          List<CartItem> items) {

        double discountPercent = getDouble(etDiscount);
        double gstPercent = getDouble(etGST);

        double subtotal = 0;
        for (CartItem item : items) {
            subtotal += item.getPrice() * item.getQuantity();
        }

        double discountAmount = subtotal * (discountPercent / 100);
        double afterDiscount = subtotal - discountAmount;
        double gstAmount = afterDiscount * (gstPercent / 100);
        double finalTotal = afterDiscount + gstAmount;

        DatabaseReference billsRef =
                FirebaseDatabase.getInstance().getReference("bills");

        String billId = billsRef.push().getKey();
        if (billId == null) return;

        DatabaseReference billRef = billsRef.child(billId);

        Map<String, Object> billData = new HashMap<>();
        billData.put("shopId", shopId);
        billData.put("customerName", customerName);
        billData.put("customerMobile", customerMobile);
        billData.put("subtotal", subtotal);
        billData.put("discountPercent", discountPercent);
        billData.put("gstPercent", gstPercent);
        billData.put("totalAmount", finalTotal);
        billData.put("createdAt", System.currentTimeMillis());

        billRef.setValue(billData).addOnSuccessListener(unused -> {

            // 🔥 SAVE ITEMS
            for (CartItem item : items) {

                String itemId = billRef.child("items").push().getKey();
                if (itemId == null) continue;

                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("medicineId", item.getMedicineId());
                itemMap.put("name", item.getName());
                itemMap.put("quantity", item.getQuantity());
                itemMap.put("price", item.getPrice());
                itemMap.put("mfgDate", item.getMfgDate());
                itemMap.put("expiryDate", item.getExpiryDate());

                billRef.child("items").child(itemId).setValue(itemMap);
            }

            generatePDF(customerName, customerMobile,
                    discountPercent, gstPercent, items);

        }).addOnFailureListener(e ->
                Toast.makeText(getContext(),
                        "Bill save failed: " + e.getMessage(),
                        Toast.LENGTH_LONG).show());
    }
    // ================= GENERATE PDF =================

    private void generatePDF(String customerName,
                             String customerMobile,
                             double discountPercent,
                             double gstPercent,
                             List<CartItem> items) {

        try {

            File pdf = BillPdfGenerator.generatePdf(
                    requireContext(),
                    "Medical Shop",
                    "Supplier",
                    customerName,
                    customerMobile,
                    items,
                    discountPercent,
                    gstPercent
            );

            Uri uri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".provider",
                    pdf
            );

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Share Invoice"));

            CartManager1.clearCart();
            updateUI();

            etCustomerName.setText("");
            etCustomerMobile.setText("");
            etDiscount.setText("");
            etGST.setText("");

            Toast.makeText(getContext(),
                    "Bill generated successfully",
                    Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(getContext(),
                    "PDF generation failed: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private double getDouble(TextInputEditText et) {
        try {
            return Double.parseDouble(et.getText().toString().trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private boolean validateMobileNumber(String mobile) {
        if (!mobile.matches("[6-9][0-9]{9}")) {
            tilCustomerMobile.setError("Enter valid 10-digit mobile number");
            return false;
        }
        tilCustomerMobile.setError(null);
        return true;
    }

    private void setupMobileValidation() {
        etCustomerMobile.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s,int st,int c,int a){}
            public void afterTextChanged(Editable s){}
            public void onTextChanged(CharSequence s,int st,int b,int c){
                validateMobileNumber(s.toString());
            }
        });
    }

    private void setupGSTDiscountListeners() {
        TextWatcher watcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s,int st,int c,int a){}
            public void afterTextChanged(Editable s){}
            public void onTextChanged(CharSequence s,int st,int b,int c){
                updateTotal();
            }
        };
        etDiscount.addTextChangedListener(watcher);
        etGST.addTextChangedListener(watcher);
    }

    private void updateTotal() {
        double subtotal = 0;
        for (CartItem item : CartManager1.getCartItems()) {
            subtotal += item.getPrice() * item.getQuantity();
        }

        double discountPercent = getDouble(etDiscount);
        double gstPercent = getDouble(etGST);

        double discountAmount = subtotal * (discountPercent / 100);
        double afterDiscount = subtotal - discountAmount;
        double gstAmount = afterDiscount * (gstPercent / 100);
        double finalTotal = afterDiscount + gstAmount;

        txtSubtotal.setText(String.format("₹%.2f", subtotal));
        txtDiscountAmount.setText(String.format("- ₹%.2f", discountAmount));
        txtGSTAmount.setText(String.format("+ ₹%.2f", gstAmount));
        txtTotal.setText(String.format("₹%.2f", finalTotal));
    }

    private void updateUI() {
        adapter.notifyDataSetChanged();
        updateTotal();

        boolean empty = CartManager1.getCartItems().isEmpty();
        txtEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        recycler.setVisibility(empty ? View.GONE : View.VISIBLE);
    }
}