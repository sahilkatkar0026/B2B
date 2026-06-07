package com.example.medicalshopb2b;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicalshopb2b.adapter.OrderItemEditAdapter;
import com.example.medicalshopb2b.model.CartItem;
import com.example.medicalshopb2b.model.Medicine;
import com.example.medicalshopb2b.model.Order;
import com.example.medicalshopb2b.utils.BillPdfGenerator;
import com.google.firebase.database.*;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

public class OrderDetailsActivity extends AppCompatActivity {

    private TextView txtOrderId, txtSupplierName, txtOrderStatus, txtOrderDate;
    private TextView txtSubtotal, txtGST, txtDiscount, txtGrandTotal;
    private RecyclerView recyclerItems;
    private Button btnAccept, btnReject, btnDownloadPdf;

    private String orderId;
    private DatabaseReference rootRef, supplierOrderRef;

    private OrderItemEditAdapter adapter;
    private final List<CartItem> itemList = new ArrayList<>();

    private Order currentOrder;
    private String invoicePathFromFirebase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        orderId = getIntent().getStringExtra("orderId");
        if (orderId == null) {
            finish();
            return;
        }

        initViews();
        setupRecycler();

        rootRef = FirebaseDatabase.getInstance().getReference();
        supplierOrderRef = rootRef.child("supplierOrders").child(orderId);

        loadOrder();

        btnAccept.setOnClickListener(v -> acceptOrder());
        btnReject.setOnClickListener(v -> rejectOrder());
        btnDownloadPdf.setOnClickListener(v -> openInvoicePdf());
    }

    private void initViews() {
        txtOrderId = findViewById(R.id.txtOrderId);
        txtSupplierName = findViewById(R.id.txtSupplierName);
        txtOrderStatus = findViewById(R.id.txtOrderStatus);
        txtOrderDate = findViewById(R.id.txtOrderDate);
        txtSubtotal = findViewById(R.id.txtSubtotal);
        txtGST = findViewById(R.id.txtGST);
        txtDiscount = findViewById(R.id.txtDiscount);
        txtGrandTotal = findViewById(R.id.txtGrandTotal);
        recyclerItems = findViewById(R.id.recyclerItems);
        btnAccept = findViewById(R.id.btnAccept);
        btnReject = findViewById(R.id.btnReject);
        btnDownloadPdf = findViewById(R.id.btnDownloadPdf);
    }

    private void setupRecycler() {
        recyclerItems.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderItemEditAdapter(itemList);
        recyclerItems.setAdapter(adapter);
    }

    private void loadOrder() {
        supplierOrderRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Order order = snapshot.getValue(Order.class);
                if (order == null) return;

                currentOrder = order;

                txtOrderId.setText("Order #" + orderId);
                txtSupplierName.setText(order.getSupplierName());
                txtOrderStatus.setText(order.getStatus());

                txtOrderDate.setText(
                        new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                                .format(new Date(order.getTimestamp()))
                );

                itemList.clear();
                itemList.addAll(order.getItems());
                adapter.notifyDataSetChanged();

                showSummary(itemList);

                // 🔥 MANAGE BUTTON STATES BASED ON ORDER STATUS
                if ("accepted".equals(order.getStatus())) {
                    // Order already accepted - disable accept, enable download if invoice exists
                    btnAccept.setEnabled(false);
                    btnAccept.setText("✓ Accepted");
                    btnReject.setEnabled(false);

                    // Load invoice path from order
                    if (order.getInvoicePath() != null && !order.getInvoicePath().isEmpty()) {
                        invoicePathFromFirebase = order.getInvoicePath();
                        btnDownloadPdf.setEnabled(true);
                    } else {
                        btnDownloadPdf.setEnabled(false);
                    }
                } else {
                    // Order not accepted yet - enable accept, disable download
                    btnAccept.setEnabled(true);
                    btnAccept.setText("Accept Order");
                    btnReject.setEnabled(true);
                    btnDownloadPdf.setEnabled(false);
                    invoicePathFromFirebase = null;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showSummary(List<CartItem> items) {

        double subtotal = 0;
        for (CartItem item : items) {
            subtotal += item.getPrice() * item.getQuantity();
        }

        double gst = (subtotal * 0.18);
        double discount = 0;
        double total = subtotal + gst - discount;

        txtSubtotal.setText("₹" + String.format("%.2f", subtotal));
        txtGST.setText("₹" + String.format("%.2f", gst));
        txtDiscount.setText("₹" + String.format("%.2f", discount));
        txtGrandTotal.setText("₹" + String.format("%.2f", total));
    }

    // ================= ACCEPT ORDER =================
    private void acceptOrder() {

        btnAccept.setEnabled(false);

        supplierOrderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {

                Order order = snap.getValue(Order.class);
                if (order == null) {
                    btnAccept.setEnabled(true);
                    return;
                }

                String shopId = order.getUserId();
                String supplierId = order.getSupplierId();
                List<CartItem> items = adapter.getUpdatedItems();

                if (items == null || items.isEmpty()) {
                    Toast.makeText(OrderDetailsActivity.this,
                            "No items in order",
                            Toast.LENGTH_SHORT).show();
                    btnAccept.setEnabled(true);
                    return;
                }

                // Process items sequentially
                processItemsSequentially(shopId, supplierId, items, 0, order);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                btnAccept.setEnabled(true);
                Toast.makeText(OrderDetailsActivity.this,
                        "Error loading order",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 🔥 SEQUENTIAL SAFE PROCESS - FIXED STOCK LOGIC
    private void processItemsSequentially(String shopId,
                                          String supplierId,
                                          List<CartItem> items,
                                          int index,
                                          Order order) {

        if (index >= items.size()) {
            // All items processed successfully
            // Mark order as accepted
            supplierOrderRef.child("status").setValue("accepted");

            // Generate invoice
            generateAndSaveInvoice(order);

            return;
        }

        CartItem item = items.get(index);
        String medicineId = item.getMedicineId();
        int qty = item.getQuantity();

        // STEP 1: Decrease supplier stock FIRST (with transaction)
        rootRef.child("suppliers")
                .child(supplierId)
                .child("medicines")
                .child(medicineId)
                .child("stock")
                .runTransaction(new Transaction.Handler() {

                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData data) {

                        Integer stock = data.getValue(Integer.class);

                        // Check if stock exists and is sufficient
                        if (stock == null) {
                            stock = 0;
                        }

                        if (stock < qty) {
                            // Not enough stock
                            return Transaction.abort();
                        }

                        // Decrease stock
                        data.setValue(stock - qty);
                        return Transaction.success(data);
                    }

                    @Override
                    public void onComplete(DatabaseError error,
                                           boolean committed,
                                           DataSnapshot snapshot) {

                        if (!committed) {
                            Toast.makeText(OrderDetailsActivity.this,
                                    "Item " + item.getName() + ": Insufficient stock in supplier",
                                    Toast.LENGTH_SHORT).show();
                            btnAccept.setEnabled(true);
                            return;
                        }

                        // STEP 2: Get full medicine details from supplier
                        rootRef.child("suppliers")
                                .child(supplierId)
                                .child("medicines")
                                .child(medicineId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {

                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                        Medicine supplierMedicine = snapshot.getValue(Medicine.class);

                                        if (supplierMedicine == null) {
                                            // Rollback stock (add back what we decremented)
                                            rollbackSupplierStock(supplierId, medicineId, qty);
                                            processItemsSequentially(shopId, supplierId, items, index + 1, order);
                                            return;
                                        }

                                        // Set shop initial stock to 0
                                        supplierMedicine.setStock(0);

                                        // STEP 3: Save medicine to shop
                                        rootRef.child("shops")
                                                .child(shopId)
                                                .child("medicines")
                                                .child(medicineId)
                                                .setValue(supplierMedicine)
                                                .addOnSuccessListener(unused -> {

                                                    // STEP 4: Increase shop stock
                                                    rootRef.child("shops")
                                                            .child(shopId)
                                                            .child("medicines")
                                                            .child(medicineId)
                                                            .child("stock")
                                                            .runTransaction(new Transaction.Handler() {

                                                                @NonNull
                                                                @Override
                                                                public Transaction.Result doTransaction(@NonNull MutableData data) {

                                                                    Integer stock = data.getValue(Integer.class);
                                                                    if (stock == null) stock = 0;

                                                                    data.setValue(stock + qty);
                                                                    return Transaction.success(data);
                                                                }

                                                                @Override
                                                                public void onComplete(DatabaseError error,
                                                                                       boolean committed,
                                                                                       DataSnapshot snapshot) {

                                                                    if (committed) {
                                                                        // Success - move to next item
                                                                        processItemsSequentially(
                                                                                shopId,
                                                                                supplierId,
                                                                                items,
                                                                                index + 1,
                                                                                order
                                                                        );
                                                                    } else {
                                                                        // Rollback supplier stock
                                                                        rollbackSupplierStock(supplierId, medicineId, qty);
                                                                        Toast.makeText(OrderDetailsActivity.this,
                                                                                "Error updating shop stock",
                                                                                Toast.LENGTH_SHORT).show();
                                                                        btnAccept.setEnabled(true);
                                                                    }
                                                                }
                                                            });
                                                });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        rollbackSupplierStock(supplierId, medicineId, qty);
                                        btnAccept.setEnabled(true);
                                    }
                                });
                    }
                });
    }

    // Helper method to rollback stock on error
    private void rollbackSupplierStock(String supplierId, String medicineId, int qty) {
        rootRef.child("suppliers")
                .child(supplierId)
                .child("medicines")
                .child(medicineId)
                .child("stock")
                .runTransaction(new Transaction.Handler() {

                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData data) {
                        Integer stock = data.getValue(Integer.class);
                        if (stock == null) stock = 0;

                        data.setValue(stock + qty);
                        return Transaction.success(data);
                    }

                    @Override
                    public void onComplete(DatabaseError error, boolean committed, DataSnapshot snapshot) {
                        // Rollback complete
                    }
                });
    }

    // 🔥 GENERATE INVOICE AND SAVE TO FIREBASE
    private void generateAndSaveInvoice(Order order) {
        try {
            String supplierName = currentOrder.getSupplierName();

            // Get shopkeeper name from intent
            String shopkeeperName = getIntent().getStringExtra("shopkeeperName");
            if (shopkeeperName == null || shopkeeperName.isEmpty()) {
                shopkeeperName = "Medical Shop";
            }

            String customerName = order.getUserId();
            String mobile = "N/A";

            // Generate PDF
            File invoiceFile = BillPdfGenerator.generatePdf(
                    this,
                    shopkeeperName,
                    supplierName,
                    customerName,
                    mobile,
                    itemList,
                    0,   // discount percent
                    18   // GST percent
            );

            if (invoiceFile != null && invoiceFile.exists()) {
                // 🔥 SAVE INVOICE PATH TO FIREBASE
                String invoicePath = invoiceFile.getAbsolutePath();
                supplierOrderRef.child("invoicePath").setValue(invoicePath)
                        .addOnSuccessListener(aVoid -> {
                            invoicePathFromFirebase = invoicePath;
                            btnDownloadPdf.setEnabled(true);

                            Toast.makeText(OrderDetailsActivity.this,
                                    "Order accepted & invoice generated",
                                    Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(OrderDetailsActivity.this,
                                    "Failed to save invoice path: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(this, "Failed to generate invoice file", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Error generating invoice: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    // 🔥 OPEN INVOICE PDF - FIXED
    private void openInvoicePdf() {

        if (invoicePathFromFirebase == null || invoicePathFromFirebase.isEmpty()) {
            Toast.makeText(this, "Invoice path not available", Toast.LENGTH_SHORT).show();
            return;
        }

        File invoiceFile = new File(invoicePathFromFirebase);

        if (!invoiceFile.exists()) {
            Toast.makeText(this, "Invoice file not found", Toast.LENGTH_SHORT).show();
            return;
        }

        try {

            Uri fileUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".provider",   // ✅ FIXED
                    invoiceFile
            );

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(fileUri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this,
                        "No PDF viewer installed",
                        Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(this,
                    "Error opening PDF: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void rejectOrder() {
        supplierOrderRef.child("status").setValue("rejected");
        finish();
    }
}