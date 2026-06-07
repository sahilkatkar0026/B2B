package com.example.medicalshopb2b;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.medicalshopb2b.model.Medicine;
import com.example.medicalshopb2b.utils.MedicineKeyUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Calendar;

public class AddMedicineActivity extends AppCompatActivity {

    EditText etName, etBrand, etPrice, etStock, etMfgDate, etExpiryDate;
    Button btnAddMedicine, btnPickImage;
    ImageView imgMedicine;

    Uri imageUri;
    long mfgDate = 0L, expiryDate = 0L;

    private final ActivityResultLauncher<Intent> imagePicker =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            imageUri = result.getData().getData();
                            imgMedicine.setImageURI(imageUri);
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medicine);

        etName = findViewById(R.id.etName);
        etBrand = findViewById(R.id.etBrand);
        etPrice = findViewById(R.id.etPrice);
        etStock = findViewById(R.id.etStock);
        etMfgDate = findViewById(R.id.etMfgDate);
        etExpiryDate = findViewById(R.id.etExpiryDate);
        imgMedicine = findViewById(R.id.imgMedicine);
        btnPickImage = findViewById(R.id.btnPickImage);
        btnAddMedicine = findViewById(R.id.btnAddMedicine);

        btnPickImage.setOnClickListener(v -> pickImage());
        etMfgDate.setOnClickListener(v -> pickDate(true));
        etExpiryDate.setOnClickListener(v -> pickDate(false));
        btnAddMedicine.setOnClickListener(v -> addMedicine());
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePicker.launch(intent);
    }

    private void pickDate(boolean isMfg) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this,
                (v, y, m, d) -> {
                    Calendar cal = Calendar.getInstance();
                    cal.set(y, m, d);

                    if (isMfg) {
                        mfgDate = cal.getTimeInMillis();
                        etMfgDate.setText(d + "/" + (m + 1) + "/" + y);
                    } else {
                        expiryDate = cal.getTimeInMillis();
                        etExpiryDate.setText(d + "/" + (m + 1) + "/" + y);
                    }
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private String imageToBase64(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(is);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos);

            return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void addMedicine() {

        String supplierId = FirebaseAuth.getInstance().getUid();
        if (supplierId == null) return;

        if (etName.getText().toString().isEmpty()
                || etPrice.getText().toString().isEmpty()
                || etStock.getText().toString().isEmpty()) {

            Toast.makeText(this, "Fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference pendingRef =
                FirebaseDatabase.getInstance().getReference("supplierMedicinesPending");

        String medId = pendingRef.push().getKey();
        if (medId == null) return;

        Medicine med = new Medicine();

        med.setMedicineId(medId);                  // 🔥 IMPORTANT
        med.setSupplierId(supplierId);
        med.setName(etName.getText().toString().trim());
        med.setBrand(etBrand.getText().toString().trim());
        med.setPrice(Integer.parseInt(etPrice.getText().toString()));
        med.setStock(Integer.parseInt(etStock.getText().toString()));
        med.setMedicineKey(MedicineKeyUtil.generateKey(med.getName()));
        med.setMfgDate(mfgDate);
        med.setExpiryDate(expiryDate);
        med.setApprovalStatus("pending");
        med.setApprovedAt(0L);
        med.setRejectedReason("");

        if (imageUri != null) {
            String base64 = imageToBase64(imageUri);
            if (base64 != null && !base64.isEmpty()) {
                med.setImageBase64(base64);
            }
        }

        pendingRef.child(medId)
                .setValue(med.toMap())
                .addOnSuccessListener(v -> {
                    Toast.makeText(this,
                            "Medicine sent for Admin Approval",
                            Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }
}