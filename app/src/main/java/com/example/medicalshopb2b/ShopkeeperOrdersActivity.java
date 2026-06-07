package com.example.medicalshopb2b;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicalshopb2b.adapter.ShopkeeperOrderAdapter;
import com.example.medicalshopb2b.model.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShopkeeperOrdersActivity extends AppCompatActivity {

    private RecyclerView recyclerOrders;
    private LinearLayout emptyState;

    private ShopkeeperOrderAdapter adapter;
    private final List<Order> orderList = new ArrayList<>();

    private DatabaseReference ordersRef;
    private String shopId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopkeeper_orders);

        recyclerOrders = findViewById(R.id.recyclerOrders);
        emptyState = findViewById(R.id.emptyState);

        shopId = FirebaseAuth.getInstance().getUid();

        if (shopId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        recyclerOrders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ShopkeeperOrderAdapter(orderList);
        recyclerOrders.setAdapter(adapter);

        ordersRef = FirebaseDatabase.getInstance()
                .getReference("supplierOrders");

        loadOrders();
    }

    private void loadOrders() {

        ordersRef.orderByChild("userId")
                .equalTo(shopId)
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        orderList.clear();

                        if (!snapshot.exists()) {
                            recyclerOrders.setVisibility(View.GONE);
                            emptyState.setVisibility(View.VISIBLE);
                            return;
                        }

                        for (DataSnapshot snap : snapshot.getChildren()) {

                            Order order = snap.getValue(Order.class);

                            if (order != null) {

                                order.setOrderId(snap.getKey());

                                // 🔥 Safety defaults
                                if (order.getStatus() == null) {
                                    order.setStatus("pending");
                                }

                                if (order.getTimestamp() == 0) {
                                    order.setTimestamp(System.currentTimeMillis());
                                }

                                orderList.add(order);
                            }
                        }

                        // Sort by latest first
                        Collections.sort(orderList,
                                (o1, o2) ->
                                        Long.compare(o2.getTimestamp(), o1.getTimestamp())
                        );

                        adapter.notifyDataSetChanged();

                        recyclerOrders.setVisibility(View.VISIBLE);
                        emptyState.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(
                                ShopkeeperOrdersActivity.this,
                                error.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }
}