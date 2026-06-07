package com.example.medicalshopb2b;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicalshopb2b.adapter.OrderAdapter;
import com.example.medicalshopb2b.model.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class SupplierOrdersActivity extends AppCompatActivity {

    private RecyclerView recyclerOrders;
    private OrderAdapter adapter;
    private final List<Order> orderList = new ArrayList<>();
    private final List<Order> allOrders = new ArrayList<>();

    private DatabaseReference rootRef;
    private String supplierId;

    private TextView txtPendingCount, txtAcceptedCount, txtTotalCount;
    private LinearLayout emptyState;
    private FrameLayout loadingContainer;
    private FrameLayout ordersCard;
    private EditText searchOrders;
    private ImageButton btnFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supplier_orders);

        // Initialize views
        recyclerOrders = findViewById(R.id.recyclerOrders);
        txtPendingCount = findViewById(R.id.txtPendingCount);
        txtAcceptedCount = findViewById(R.id.txtAcceptedCount);
        txtTotalCount = findViewById(R.id.txtTotalCount);
        emptyState = findViewById(R.id.emptyState);
        loadingContainer = findViewById(R.id.loadingContainer);
        ordersCard = findViewById(R.id.ordersCard);
        searchOrders = findViewById(R.id.searchOrders);
        btnFilter = findViewById(R.id.btnFilter);

        supplierId = FirebaseAuth.getInstance().getUid();
        if (supplierId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        rootRef = FirebaseDatabase.getInstance().getReference();

        // Setup RecyclerView
        recyclerOrders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderAdapter(new ArrayList<>());
        recyclerOrders.setAdapter(adapter);

        // Load orders
        loadOrders();
        setupSearch();
    }

    // ================= LOAD SUPPLIER ORDERS =================
    private void loadOrders() {
        // 🔥 SHOW LOADING
        loadingContainer.setVisibility(View.VISIBLE);
        ordersCard.setVisibility(View.GONE);

        rootRef.child("supplierOrders")
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        orderList.clear();
                        allOrders.clear();

                        for (DataSnapshot snap : snapshot.getChildren()) {

                            Order order = snap.getValue(Order.class);

                            if (order == null) continue;

                            if (order.getSupplierId() != null &&
                                    order.getSupplierId().equals(supplierId)) {

                                allOrders.add(order);
                                orderList.add(order);
                            }
                        }

                        // 🔥 HIDE LOADING AND SHOW ORDERS
                        loadingContainer.setVisibility(View.GONE);
                        ordersCard.setVisibility(View.VISIBLE);

                        updateCounts();
                        adapter.updateList(orderList);

                        if (orderList.isEmpty()) {
                            emptyState.setVisibility(View.VISIBLE);
                            recyclerOrders.setVisibility(RecyclerView.GONE);
                        } else {
                            emptyState.setVisibility(View.GONE);
                            recyclerOrders.setVisibility(RecyclerView.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // 🔥 HIDE LOADING ON ERROR
                        loadingContainer.setVisibility(View.GONE);
                        ordersCard.setVisibility(View.VISIBLE);

                        Toast.makeText(SupplierOrdersActivity.this,
                                error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ================= UPDATE COUNTS =================
    private void updateCounts() {

        int pending = 0;
        int accepted = 0;

        for (Order order : orderList) {

            if (order.getStatus() == null) continue;

            if (order.getStatus().equalsIgnoreCase("pending"))
                pending++;

            if (order.getStatus().equalsIgnoreCase("accepted"))
                accepted++;
        }

        txtPendingCount.setText(String.valueOf(pending));
        txtAcceptedCount.setText(String.valueOf(accepted));
        txtTotalCount.setText(String.valueOf(orderList.size()));
    }

    // ================= SEARCH =================
    private void setupSearch() {

        searchOrders.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int a,int b,int c){}
            @Override public void afterTextChanged(android.text.Editable s){}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String query = s.toString().toLowerCase();
                List<Order> filtered = new ArrayList<>();

                for (Order order : allOrders) {

                    String id = order.getOrderId() == null ? "" :
                            order.getOrderId().toLowerCase();

                    String supplier = order.getSupplierName() == null ? "" :
                            order.getSupplierName().toLowerCase();

                    if (id.contains(query) || supplier.contains(query)) {
                        filtered.add(order);
                    }
                }

                orderList.clear();
                orderList.addAll(filtered);
                adapter.updateList(filtered);

                updateCounts();

                if (filtered.isEmpty()) {
                    emptyState.setVisibility(View.VISIBLE);
                    recyclerOrders.setVisibility(RecyclerView.GONE);
                } else {
                    emptyState.setVisibility(View.GONE);
                    recyclerOrders.setVisibility(RecyclerView.VISIBLE);
                }
            }
        });
    }
}