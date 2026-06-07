package com.example.medicalshopb2b;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    // UI
    private EditText etName, etEmail, etPhone, etPassword,
            etConfirmPassword, etShopName, etAddress;
    private TextInputLayout tilShopName;
    private Button btnRegister, btnUploadLicense;
    private RadioGroup roleGroup;
    private TextView tvLogin;

    // Firebase
    private FirebaseAuth auth;
    private DatabaseReference userRef;
    private Dialog loadingDialog;

    // License
    private Uri licenseUri;
    private static final int PICK_FILE_REQUEST = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etShopName = findViewById(R.id.etShopName);
        etAddress = findViewById(R.id.etAddress);
        tilShopName = findViewById(R.id.tilShopName);
        btnRegister = findViewById(R.id.btnRegister);
        roleGroup = findViewById(R.id.roleGroup);
        tvLogin = findViewById(R.id.tvLogin);
        btnUploadLicense = findViewById(R.id.btnUploadLicense);
    }

    private void setupListeners() {

        btnUploadLicense.setOnClickListener(v -> openFilePicker());

        btnRegister.setOnClickListener(v -> registerUser());

        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        roleGroup.setOnCheckedChangeListener((group, checkedId) -> {
            tilShopName.setVisibility(LinearLayout.VISIBLE);
            if (checkedId == R.id.rbShopkeeper) {
                tilShopName.setHint("Pharmacy / Shop Name");
            } else {
                tilShopName.setHint("Business / Company Name");
            }
        });
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*"); // Only image
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST &&
                resultCode == RESULT_OK &&
                data != null &&
                data.getData() != null) {

            licenseUri = data.getData();
            btnUploadLicense.setText("License Selected ✓");
        }
    }

    // Convert Image to Base64
    private String convertImageToBase64(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);

            byte[] imageBytes = baos.toByteArray();
            return Base64.encodeToString(imageBytes, Base64.DEFAULT);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void showLoadingDialog(String message) {
        loadingDialog = new Dialog(this);
        loadingDialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        loadingDialog.setCancelable(false);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(android.view.Gravity.CENTER);
        layout.setBackgroundColor(Color.TRANSPARENT);

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
        if (loadingDialog != null) loadingDialog.dismiss();
    }

    private void registerUser() {

        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String shopName = etShopName.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        int selectedId = roleGroup.getCheckedRadioButtonId();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()
                || password.isEmpty() || confirmPassword.isEmpty()
                || shopName.isEmpty() || address.isEmpty()) {

            Toast.makeText(this, "All fields required",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this,
                    "Passwords do not match",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (licenseUri == null) {
            Toast.makeText(this,
                    "Please upload license image",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String role = (selectedId == R.id.rbShopkeeper)
                ? "shopkeeper" : "supplier";

        showLoadingDialog("Creating account...");

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {

                    String uid = auth.getCurrentUser().getUid();

                    String base64License = convertImageToBase64(licenseUri);

                    userRef = FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(uid);

                    HashMap<String, Object> user = new HashMap<>();

                    user.put("uid", uid);
                    user.put("name", name);
                    user.put("email", email);
                    user.put("phone", "+91 " + phone);
                    user.put("role", role);
                    user.put("shopName", shopName);
                    user.put("address", address);
                    user.put("licenseImageBase64", base64License);
                    user.put("registrationStatus", "pending");
                    user.put("createdAt", System.currentTimeMillis());

                    // Set user only; admin will approve and Cloud Function will
                    // activate corresponding shopkeeper/supplier node.
                    userRef.setValue(user).addOnSuccessListener(aVoid -> {
                        dismissLoadingDialog();
                        Toast.makeText(this,
                                "Submitted for Admin Approval",
                                Toast.LENGTH_LONG).show();

                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(this,
                                LoginActivity.class));
                        finish();
                    }).addOnFailureListener(e -> {
                        dismissLoadingDialog();
                        Toast.makeText(this,
                                "Registration failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });

                })
                .addOnFailureListener(e -> {
                    dismissLoadingDialog();
                    Toast.makeText(this,
                            "Registration failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}
