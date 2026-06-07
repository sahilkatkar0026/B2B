package com.example.medicalshopb2b.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicalshopb2b.R;
import com.example.medicalshopb2b.model.HistoryModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter
        extends RecyclerView.Adapter<HistoryAdapter.VH> {

    public interface DeleteListener {
        void onDelete(HistoryModel model);
    }

    public interface ReprintListener {
        void onReprint(HistoryModel model);
    }

    private final List<HistoryModel> list;
    private final DeleteListener deleteListener;
    private final ReprintListener reprintListener;

    private final SimpleDateFormat sdf =
            new SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.US);

    public HistoryAdapter(List<HistoryModel> list,
                          DeleteListener deleteListener,
                          ReprintListener reprintListener) {

        this.list = list;
        this.deleteListener = deleteListener;
        this.reprintListener = reprintListener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);

        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {

        HistoryModel model = list.get(position);

        holder.txtCustomerName.setText(model.getCustomerName());
        holder.txtMobile.setText(model.getCustomerMobile());
        holder.txtAmount.setText("₹" + model.getTotalAmount());
        holder.txtDate.setText(
                sdf.format(new Date(model.getCreatedAt()))
        );

        // ✅ OPEN BILL → Show Dialog
        holder.btnOpenBill.setOnClickListener(v -> {

            Context context = v.getContext();

            String billDetails =
                    "Customer: " + model.getCustomerName() + "\n\n" +
                            "Mobile: " + model.getCustomerMobile() + "\n\n" +
                            "Total Amount: ₹" + model.getTotalAmount() + "\n\n" +
                            "Date: " + sdf.format(new Date(model.getCreatedAt()));

            new AlertDialog.Builder(context)
                    .setTitle("Bill Details")
                    .setMessage(billDetails)
                    .setPositiveButton("Close", null)
                    .show();
        });

        // ✅ DELETE
        holder.btnDelete.setOnClickListener(v ->
                deleteListener.onDelete(model));

        // ✅ REPRINT
        holder.btnReprint.setOnClickListener(v ->
                reprintListener.onReprint(model));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {

        TextView txtCustomerName, txtMobile,
                txtAmount, txtDate;

        Button btnOpenBill, btnDelete, btnReprint;

        VH(View itemView) {
            super(itemView);

            txtCustomerName =
                    itemView.findViewById(R.id.txtCustomerName);
            txtMobile =
                    itemView.findViewById(R.id.txtMobile);
            txtAmount =
                    itemView.findViewById(R.id.txtAmount);
            txtDate =
                    itemView.findViewById(R.id.txtDate);

            btnOpenBill =
                    itemView.findViewById(R.id.btnOpen);
            btnDelete =
                    itemView.findViewById(R.id.btnDelete);
            btnReprint =
                    itemView.findViewById(R.id.btnReprint);
        }
    }
}