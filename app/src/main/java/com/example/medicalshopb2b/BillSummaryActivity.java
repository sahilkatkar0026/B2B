package com.example.medicalshopb2b;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.medicalshopb2b.model.CartItem;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class BillSummaryActivity extends AppCompatActivity {

    TextView txtBill;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_summary);

        txtBill = findViewById(R.id.txtBill);

        ArrayList<CartItem> items =
                getIntent().getParcelableArrayListExtra("billItems");

        if (items == null || items.isEmpty()) {
            Toast.makeText(this,
                    "No bill data found",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String customerName =
                getIntent().getStringExtra("customerName");
        String customerMobile =
                getIntent().getStringExtra("customerMobile");

        StringBuilder bill = new StringBuilder();
        double total = 0;

        bill.append("MEDICAL SHOP BILL\n\n");
        bill.append("Customer: ").append(customerName).append("\n");
        bill.append("Mobile: ").append(customerMobile).append("\n\n");

        for (CartItem item : items) {

            double itemTotal =
                    item.getPrice() * item.getQuantity();

            bill.append(item.getName())
                    .append(" x ")
                    .append(item.getQuantity())
                    .append(" = ₹")
                    .append(itemTotal)
                    .append("\n");

            total += itemTotal;
        }

        bill.append("\n----------------------\n");
        bill.append("TOTAL: ₹").append(String.format("%.2f", total));

        txtBill.setText(bill.toString());

        generatePdf(bill.toString());
    }

    // ================= PDF =================

    private void generatePdf(String content) {

        try {
            File dir = new File(
                    Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOCUMENTS),
                    "MedicalShopBills"
            );

            if (!dir.exists()) dir.mkdirs();

            File file = new File(
                    dir,
                    "Bill_" + System.currentTimeMillis() + ".pdf"
            );

            FileOutputStream fos = new FileOutputStream(file);

            android.graphics.pdf.PdfDocument pdf =
                    new android.graphics.pdf.PdfDocument();

            android.graphics.pdf.PdfDocument.PageInfo pageInfo =
                    new android.graphics.pdf.PdfDocument.PageInfo
                            .Builder(300, 600, 1).create();

            android.graphics.pdf.PdfDocument.Page page =
                    pdf.startPage(pageInfo);

            android.graphics.Paint paint = new android.graphics.Paint();
            paint.setTextSize(12);

            int y = 25;
            for (String line : content.split("\n")) {
                page.getCanvas().drawText(line, 10, y, paint);
                y += 15;
            }

            pdf.finishPage(page);
            pdf.writeTo(fos);
            pdf.close();
            fos.close();

            openPdf(file);

        } catch (Exception e) {
            Toast.makeText(this,
                    "PDF failed: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void openPdf(File file) {

        Uri uri = FileProvider.getUriForFile(
                this,
                getPackageName() + ".provider",
                file
        );

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(intent);
    }
}