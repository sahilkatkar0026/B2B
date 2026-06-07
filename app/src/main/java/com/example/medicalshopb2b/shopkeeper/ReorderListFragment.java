package com.example.medicalshopb2b.shopkeeper;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicalshopb2b.R;
import com.example.medicalshopb2b.adapter.ReorderListAdapter;
import com.example.medicalshopb2b.model.ReorderRequest;
import com.example.medicalshopb2b.utils.Reordersyncmanager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class ReorderListFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView txtEmpty;
    private TextView txtSyncStatus;

    private final List<ReorderRequest> list = new ArrayList<>();
    private ReorderListAdapter adapter;

    private DatabaseReference shopReordersRef;
    private ValueEventListener reordersListener;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {

        View v = inflater.inflate(R.layout.fragment_reorder_list, container, false);

        recyclerView = v.findViewById(R.id.recyclerReorders);
        txtEmpty = v.findViewById(R.id.txtEmpty);

        // Optional: Add sync status indicator
        // txtSyncStatus = v.findViewById(R.id.txtSyncStatus);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new ReorderListAdapter(requireContext(), list);
        recyclerView.setAdapter(adapter);

        // 🔥 START AUTO-SYNC when fragment is created
        Reordersyncmanager.startAutoSync();

        loadReorders();
        return v;
    }

    /**
     * 🔥 LOAD REORDERS - Real-time listener
     * Updates whenever stock levels change
     */
    private void loadReorders() {

        String shopId = FirebaseAuth.getInstance().getUid();
        if (shopId == null) return;

        shopReordersRef = FirebaseDatabase.getInstance()
                .getReference("shops")
                .child(shopId)
                .child("reorders");

        reordersListener = new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                list.clear();

                if (!snapshot.exists()) {
                    txtEmpty.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    return;
                }

                for (DataSnapshot snap : snapshot.getChildren()) {

                    ReorderRequest req = snap.getValue(ReorderRequest.class);
                    if (req != null) {
                        req.setMedicineKey(snap.getKey());
                        list.add(req);
                    }
                }

                boolean empty = list.isEmpty();
                txtEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
                recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Log error
            }
        };

        // 🔥 ADD REAL-TIME LISTENER
        shopReordersRef.addValueEventListener(reordersListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // 🔥 REMOVE LISTENER when fragment is destroyed
        if (shopReordersRef != null && reordersListener != null) {
            shopReordersRef.removeEventListener(reordersListener);
        }
    }
}