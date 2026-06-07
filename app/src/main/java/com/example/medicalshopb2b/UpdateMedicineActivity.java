package com.example.medicalshopb2b;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.medicalshopb2b.model.Medicine;
import com.example.medicalshopb2b.utils.MedicineKeyUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UpdateMedicineActivity extends AppCompatActivity {

    private TextInputEditText etName, etBrand, etPrice, etStock, etMfgDate, etExpiryDate;
    private MaterialButton btnUpdate, btnChangeImage;
    private ImageView imgMedicine;

    private String medicineId, supplierId;
    private Medicine existingMedicine;

    private Uri newImageUri; // 🔥 only if user changes image

    private final SimpleDateFormat sdf =
            new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    // ✅ MODERN IMAGE PICKER
    private final ActivityResultLauncher<Intent> imagePicker =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            newImageUri = result.getData().getData();
                            imgMedicine.setImageURI(newImageUri);
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_medicine);

        etName = findViewById(R.id.etName);
        etBrand = findViewById(R.id.etBrand);
        etPrice = findViewById(R.id.etPrice);
        etStock = findViewById(R.id.etStock);
        etMfgDate = findViewById(R.id.etMfgDate);
        etExpiryDate = findViewById(R.id.etExpiryDate);
        btnUpdate = findViewById(R.id.btnUpdateMedicine);
        btnChangeImage = findViewById(R.id.btnChangeImage);
        imgMedicine = findViewById(R.id.imgMedicine);

        medicineId = getIntent().getStringExtra("medicineId");
        supplierId = FirebaseAuth.getInstance().getUid();

        if (medicineId == null || supplierId == null) {
            Toast.makeText(this, "Invalid medicine", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadMedicine();

        btnChangeImage.setOnClickListener(v -> pickImage());
        btnUpdate.setOnClickListener(v -> updateMedicine());
    }

    // ================= IMAGE PICK =================
    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePicker.launch(intent);
    }

    // ================= LOAD EXISTING =================
    private void loadMedicine() {

        FirebaseDatabase.getInstance()
                .getReference("suppliers")
                .child(supplierId)
                .child("medicines")
                .child(medicineId)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        existingMedicine = snapshot.getValue(Medicine.class);
                        if (existingMedicine == null) {
                            Toast.makeText(UpdateMedicineActivity.this,
                                    "Medicine not found", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }

                        etName.setText(existingMedicine.getName());
                        etBrand.setText(existingMedicine.getBrand());
                        etPrice.setText(String.valueOf(existingMedicine.getPrice()));
                        etStock.setText(String.valueOf(existingMedicine.getStock()));

                        if (existingMedicine.getMfgDate() > 0)
                            etMfgDate.setText(sdf.format(new Date(existingMedicine.getMfgDate())));

                        if (existingMedicine.getExpiryDate() > 0)
                            etExpiryDate.setText(sdf.format(new Date(existingMedicine.getExpiryDate())));

                        // 🔥 LOAD IMAGE
                        if (existingMedicine.getImageBase64() != null &&
                                !existingMedicine.getImageBase64().isEmpty()) {
                            byte[] bytes = Base64.decode(existingMedicine.getImageBase64(), Base64.DEFAULT);
                            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            imgMedicine.setImageBitmap(bmp);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(UpdateMedicineActivity.this,
                                error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    // ================= BASE64 =================
    private String imageToBase64(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(is);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos);

            return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
        } catch (Exception e) {
            return null;
        }
    }

    // ================= UPDATE =================
    private void updateMedicine() {

        if (existingMedicine == null) return;

        try {
            Medicine med = new Medicine();

            // 🔒 REQUIRED FIELDS
            med.setMedicineId(medicineId);
            med.setSupplierId(supplierId);
            med.setTotalSold(existingMedicine.getTotalSold());
            med.setLastSoldAt(existingMedicine.getLastSoldAt());

            med.setName(etName.getText().toString().trim());
            med.setBrand(etBrand.getText().toString().trim());
            med.setPrice(Integer.parseInt(etPrice.getText().toString().trim()));
            med.setStock(Integer.parseInt(etStock.getText().toString().trim()));
            med.setMedicineKey(MedicineKeyUtil.generateKey(med.getName()));

            // DATES
            med.setMfgDate(existingMedicine.getMfgDate());
            med.setExpiryDate(existingMedicine.getExpiryDate());

            if (!etMfgDate.getText().toString().isEmpty())
                med.setMfgDate(sdf.parse(etMfgDate.getText().toString()).getTime());

            if (!etExpiryDate.getText().toString().isEmpty())
                med.setExpiryDate(sdf.parse(etExpiryDate.getText().toString()).getTime());

            // 🔥 IMAGE PRESERVE / UPDATE
            if (newImageUri != null) {
                String base64 = imageToBase64(newImageUri);
                med.setImageBase64(base64);
            } else {
                med.setImageBase64(existingMedicine.getImageBase64());
            }

            FirebaseDatabase.getInstance()
                    .getReference("suppliers")
                    .child(supplierId)
                    .child("medicines")
                    .child(medicineId)
                    .setValue(med)
                    .addOnSuccessListener(v -> {
                        Toast.makeText(this, "Medicine updated", Toast.LENGTH_SHORT).show();
                        finish();
                    });

        } catch (Exception e) {
            Toast.makeText(this,
                    "Invalid date format (yyyy-mm-dd)", Toast.LENGTH_LONG).show();
        }
    }
}