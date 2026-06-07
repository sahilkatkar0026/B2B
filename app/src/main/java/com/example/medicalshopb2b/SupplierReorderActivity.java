package com.example.medicalshopb2b;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import com.example.medicalshopb2b.model.ReorderRequest;
public class SupplierReorderActivity extends AppCompatActivity {

    ListView listView;
    ArrayList<String> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supplier_reorder);

        listView = findViewById(R.id.listReorders);
        list = new ArrayList<>();

        String supplierId = FirebaseAuth.getInstance().getUid();

        // ✅ CORRECT PATH: "reorders"
        FirebaseDatabase.getInstance()
                .getReference("reorders")
                .child(supplierId)
                .get()
                .addOnSuccessListener(snapshot -> {

                    list.clear();

                    for (DataSnapshot snap : snapshot.getChildren()) {

                        ReorderRequest r = snap.getValue(ReorderRequest.class);

                        if (r != null) {
                            list.add(
                                    "Medicine: " + r.getMedicineName() +
                                            "\nCurrent Stock: " + r.getCurrentStock() +
                                            "\nSuggested Order: " + r.getSuggestedQty()
                            );
                        }
                    }

                    listView.setAdapter(
                            new ArrayAdapter<>(
                                    this,
                                    android.R.layout.simple_list_item_1,
                                    list
                            )
                    );
                });
    }
}
