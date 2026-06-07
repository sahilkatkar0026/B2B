package com.example.medicalshopb2b;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin;
    TextView tvRegister;

    FirebaseAuth auth;
    DatabaseReference userRef;
    Dialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        btnLogin.setOnClickListener(v -> loginUser());

        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void showLoadingDialog(String message) {
        loadingDialog = new Dialog(this);
        loadingDialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        loadingDialog.setCancelable(false);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(android.view.Gravity.CENTER);

        ProgressBar progressBar = new ProgressBar(this);
        layout.addView(progressBar);

        TextView tv = new TextView(this);
        tv.setText(message);
        tv.setTextColor(Color.GRAY);
        layout.addView(tv);

        loadingDialog.setContentView(layout);
        loadingDialog.getWindow()
                .setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        loadingDialog.show();
    }

    private void dismissLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    private void loginUser() {

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this,
                    "Enter email & password",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        showLoadingDialog("Signing in...");
        btnLogin.setEnabled(false);

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {

                    String uid = auth.getCurrentUser().getUid();

                    userRef = FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(uid);

                    userRef.addListenerForSingleValueEvent(
                            new ValueEventListener() {

                                @Override
                                public void onDataChange(
                                        @NonNull DataSnapshot snapshot) {

                                    dismissLoadingDialog();
                                    btnLogin.setEnabled(true);

                                    if (!snapshot.exists()) {
                                        auth.signOut();
                                        Toast.makeText(LoginActivity.this,
                                                "User data not found",
                                                Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    String status = snapshot
                                            .child("registrationStatus")
                                            .getValue(String.class);

                                    String role = snapshot
                                            .child("role")
                                            .getValue(String.class);

                                    if (status == null || role == null) {
                                        auth.signOut();
                                        Toast.makeText(LoginActivity.this,
                                                "Invalid account data",
                                                Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    if (status.equals("pending")) {
                                        auth.signOut();
                                        Toast.makeText(LoginActivity.this,
                                                "Waiting for Admin Approval",
                                                Toast.LENGTH_LONG).show();
                                        return;
                                    }

                                    if (status.equals("rejected")) {
                                        auth.signOut();
                                        Toast.makeText(LoginActivity.this,
                                                "Account Rejected by Admin",
                                                Toast.LENGTH_LONG).show();
                                        return;
                                    }

                                    if (!status.equals("approved")) {
                                        auth.signOut();
                                        Toast.makeText(LoginActivity.this,
                                                "Account not approved",
                                                Toast.LENGTH_LONG).show();
                                        return;
                                    }

                                    Intent intent;

                                    if (role.equals("shopkeeper")) {
                                        intent = new Intent(
                                                LoginActivity.this,
                                                ShopkeeperDashboardActivity.class);
                                    } else if (role.equals("supplier")) {
                                        intent = new Intent(
                                                LoginActivity.this,
                                                SupplierDashboardActivity.class);
                                    } else {
                                        auth.signOut();
                                        Toast.makeText(LoginActivity.this,
                                                "Invalid user role",
                                                Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    startActivity(intent);
                                    finish();
                                }

                                @Override
                                public void onCancelled(
                                        @NonNull DatabaseError error) {

                                    dismissLoadingDialog();
                                    btnLogin.setEnabled(true);

                                    Toast.makeText(LoginActivity.this,
                                            "Database Error: " + error.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    dismissLoadingDialog();
                    btnLogin.setEnabled(true);

                    Toast.makeText(this,
                            "Login failed: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}