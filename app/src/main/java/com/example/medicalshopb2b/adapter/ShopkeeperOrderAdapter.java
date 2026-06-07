package com.example.medicalshopb2b.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicalshopb2b.R;
import com.example.medicalshopb2b.model.CartItem;
import com.example.medicalshopb2b.model.Order;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ShopkeeperOrderAdapter
        extends RecyclerView.Adapter<ShopkeeperOrderAdapter.VH> {

    private final List<Order> orderList;

    public ShopkeeperOrderAdapter(List<Order> list) {
        this.orderList = list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shopkeeper_order, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(orderList.get(position));
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class VH extends RecyclerView.ViewHolder {

        TextView txtOrderType, txtStatus, txtOrderId,
                txtMedicineDetails, txtOrderDate, txtTotalAmount;

        ImageView imgMedicine;

        VH(@NonNull View itemView) {
            super(itemView);

            txtOrderType = itemView.findViewById(R.id.txtOrderType);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            txtOrderId = itemView.findViewById(R.id.txtOrderId);
            txtMedicineDetails = itemView.findViewById(R.id.txtMedicineDetails);
            txtOrderDate = itemView.findViewById(R.id.txtOrderDate);
            txtTotalAmount = itemView.findViewById(R.id.txtTotalAmount);
            imgMedicine = itemView.findViewById(R.id.imgMedicine);
        }

        void bind(Order order) {

            txtOrderId.setText("Order #" + order.getOrderId());

            // 🔥 ORDER TYPE
            if (order.getItems() != null && order.getItems().size() == 1) {
                txtOrderType.setText("REORDER");
            } else {
                txtOrderType.setText("ORDER");
            }

            // 🔷 STATUS
            String status = order.getStatus() == null ?
                    "pending" : order.getStatus();

            txtStatus.setText(capitalize(status));
            setStatusColor(status);

            // 🔷 MEDICINE DETAILS + IMAGE
            if (order.getItems() != null && !order.getItems().isEmpty()) {

                CartItem firstItem = order.getItems().get(0);

                txtMedicineDetails.setText(
                        firstItem.getName() +
                                " (" + firstItem.getQuantity() + ")"
                );

                // 🔥 LOAD BASE64 IMAGE
                String base64 = firstItem.getImageBase64();

                if (base64 != null && !base64.isEmpty()) {
                    try {
                        byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        imgMedicine.setImageBitmap(bmp);
                    } catch (Exception e) {
                        imgMedicine.setImageResource(R.drawable.ic_medicine_placeholder);
                    }
                } else {
                    imgMedicine.setImageResource(R.drawable.ic_medicine_placeholder);
                }
            }

            // 🔷 DATE
            SimpleDateFormat sdf =
                    new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

            txtOrderDate.setText(
                    sdf.format(new Date(order.getTimestamp()))
            );

            // 🔷 TOTAL
            txtTotalAmount.setText("₹" + order.getTotal());
        }

        private void setStatusColor(String status) {
            // Colors from mockup design
            switch (status.toLowerCase()) {
                case "accepted":
                    txtStatus.setTextColor(Color.parseColor("#27AE60")); // Green
                    break;
                case "rejected":
                    txtStatus.setTextColor(Color.parseColor("#E74C3C")); // Red
                    break;
                case "pending":
                default:
                    txtStatus.setTextColor(Color.parseColor("#5B8DBE")); // Blue
                    break;
            }
        }

        private String capitalize(String text) {
            if (text == null || text.isEmpty()) return "Pending";
            return text.substring(0, 1).toUpperCase()
                    + text.substring(1).toLowerCase();
        }
    }
}