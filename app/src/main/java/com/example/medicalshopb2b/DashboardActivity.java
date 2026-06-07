package com.example.medicalshopb2b;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DashboardActivity extends AppCompatActivity {

    DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // 🔥 REALTIME DATABASE
        userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("role");

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (!snapshot.exists()) {
                    Toast.makeText(
                            DashboardActivity.this,
                            "Role not found",
                            Toast.LENGTH_SHORT
                    ).show();
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(
                            DashboardActivity.this,
                            LoginActivity.class
                    ));
                    finish();
                    return;
                }

                String role = snapshot.getValue(String.class);

                if ("shopkeeper".equals(role)) {

                    // ✅ SHOPKEEPER → SELECT SUPPLIER FIRST
                    startActivity(new Intent(
                            DashboardActivity.this,
                            ShopkeeperDashboardActivity.class
                    ));

                } else if ("supplier".equals(role)) {

                    // ✅ SUPPLIER → DASHBOARD
                    startActivity(new Intent(
                            DashboardActivity.this,
                            SupplierDashboardActivity.class
                    ));

                } else {
                    Toast.makeText(
                            DashboardActivity.this,
                            "Invalid role",
                            Toast.LENGTH_SHORT
                    ).show();
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(
                            DashboardActivity.this,
                            LoginActivity.class
                    ));
                }

                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        DashboardActivity.this,
                        error.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
}
