package com.example.medicalshopb2b;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicalshopb2b.adapter.HistoryAdapter;
import com.example.medicalshopb2b.model.CartItem;
import com.example.medicalshopb2b.model.HistoryModel;
import com.example.medicalshopb2b.utils.BillPdfGenerator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomerHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private List<HistoryModel> historyList = new ArrayList<>();
    private List<HistoryModel> filteredList = new ArrayList<>();

    private String shopId;

    private EditText etSearch;
    private TextView txtTotalSales;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_history);

        recyclerView = findViewById(R.id.recyclerHistory);
        etSearch = findViewById(R.id.etSearch);
        txtTotalSales = findViewById(R.id.txtTotalSales);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new HistoryAdapter(filteredList,
                this::deleteBill,
                this::reprintBill);

        recyclerView.setAdapter(adapter);

        shopId = FirebaseAuth.getInstance().getUid();

        loadHistory();
        setupSearch();
    }

    private void loadHistory() {

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("bills");

        ref.orderByChild("shopId")
                .equalTo(shopId)
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        historyList.clear();
                        double totalSales = 0;

                        for (DataSnapshot snap : snapshot.getChildren()) {

                            HistoryModel model =
                                    snap.getValue(HistoryModel.class);

                            if (model != null) {

                                model.setBillId(snap.getKey());
                                historyList.add(model);
                                totalSales += model.getTotalAmount();
                            }
                        }

                        Collections.reverse(historyList);

                        filteredList.clear();
                        filteredList.addAll(historyList);

                        txtTotalSales.setText(
                                "Total Sales: ₹" + totalSales
                        );

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(CustomerHistoryActivity.this,
                                "Failed to load history",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupSearch() {

        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(android.text.Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                filteredList.clear();

                for (HistoryModel model : historyList) {
                    if (model.getCustomerMobile() != null &&
                            model.getCustomerMobile().contains(s.toString())) {
                        filteredList.add(model);
                    }
                }

                adapter.notifyDataSetChanged();
            }
        });
    }

    // ================= DELETE BILL =================
    private void deleteBill(HistoryModel model) {

        new AlertDialog.Builder(this)
                .setTitle("Delete Bill")
                .setMessage("Are you sure?")
                .setPositiveButton("Yes", (d, which) -> {

                    FirebaseDatabase.getInstance()
                            .getReference("bills")
                            .child(model.getBillId())
                            .removeValue();

                    Toast.makeText(this,
                            "Bill deleted",
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ================= 🔥 REAL REPRINT =================
    private void reprintBill(HistoryModel model) {

        try {

            // 🔥 Fetch full bill items from Firebase
            FirebaseDatabase.getInstance()
                    .getReference("bills")
                    .child(model.getBillId())
                    .child("items")
                    .addListenerForSingleValueEvent(new ValueEventListener() {

                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            List<CartItem> items = new ArrayList<>();

                            for (DataSnapshot snap : snapshot.getChildren()) {
                                CartItem item = snap.getValue(CartItem.class);
                                if (item != null) {
                                    items.add(item);
                                }
                            }

                            try {

                                File file = BillPdfGenerator.generatePdf(
                                        CustomerHistoryActivity.this,
                                        "Medical Shop",
                                        model.getCustomerName(),
                                        model.getCustomerMobile(),
                                        items
                                );

                                openPdf(file);

                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(CustomerHistoryActivity.this,
                                        "PDF generation failed",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= OPEN / SHARE PDF =================
    private void openPdf(File file) {

        Uri uri = FileProvider.getUriForFile(
                this,
                getPackageName() + ".provider",
                file
        );

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(intent, "Open Invoice"));
    }
}