package com.example.medicalshopb2b.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicalshopb2b.OrderDetailsActivity;
import com.example.medicalshopb2b.R;
import com.example.medicalshopb2b.model.CartItem;
import com.example.medicalshopb2b.model.Order;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.VH> {

    private final List<Order> orderList = new ArrayList<>();
    private OnOrderClickListener onOrderClickListener;

    public OrderAdapter(List<Order> list) {
        if (list != null) {
            orderList.addAll(list);
        }
    }

    // ================= SET CLICK LISTENER =================
    public void setOnOrderClickListener(OnOrderClickListener listener) {
        this.onOrderClickListener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        if (position < orderList.size()) {
            holder.bind(orderList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    // ================= UPDATE LIST =================
    public void updateList(List<Order> newList) {
        orderList.clear();
        if (newList != null) {
            orderList.addAll(newList);
        }
        notifyDataSetChanged();
    }

    // ================= VIEW HOLDER =================
    class VH extends RecyclerView.ViewHolder {

        private final TextView txtOrderId;
        private final TextView txtSupplierName;
        private final TextView txtStatus;
        private final TextView txtItemCount;
        private final TextView txtOrderDate;
        private final TextView txtTotalAmount;
        private final Button btnViewDetails;
        private final Button btnManage;
        private final CardView cardViewOrder;

        VH(@NonNull View v) {
            super(v);

            txtOrderId = v.findViewById(R.id.txtOrderId);
            txtSupplierName = v.findViewById(R.id.txtSupplierName);
            txtStatus = v.findViewById(R.id.txtStatus);
            txtItemCount = v.findViewById(R.id.txtItemCount);
            txtOrderDate = v.findViewById(R.id.txtOrderDate);
            txtTotalAmount = v.findViewById(R.id.txtTotalAmount);
            btnViewDetails = v.findViewById(R.id.btnViewDetails);
            btnManage = v.findViewById(R.id.btnManage);
            cardViewOrder = v.findViewById(R.id.cardViewOrder);
        }

        void bind(Order order) {

            if (order == null) return;

            // 🔷 Order ID
            String orderId = order.getOrderId() != null ?
                    order.getOrderId() : "Unknown";

            txtOrderId.setText("Order #" + orderId);

            // 🔷 Proper Reorder Name (First Medicine Name)
            String reorderName = "Reorder";

            if (order.getItems() != null && !order.getItems().isEmpty()) {
                CartItem firstItem = order.getItems().get(0);
                if (firstItem.getName() != null) {
                    reorderName = firstItem.getName();
                }
            }

            txtSupplierName.setText(
                    order.getSupplierName() != null ?
                            order.getSupplierName() :
                            "Unknown Supplier"
            );

            // 🔷 Status
            String status = order.getStatus() != null ?
                    order.getStatus() : "pending";

            txtStatus.setText(capitalize(status));
            setStatusBadgeColor(status);

            // 🔷 Item Count
            int itemCount = order.getItems() != null ?
                    order.getItems().size() : 0;

            txtItemCount.setText(itemCount + " item(s)");

            // 🔷 Date
            txtOrderDate.setText(formatDate(order.getTimestamp()));

            // 🔷 Total
            int total = order.getTotal();
            txtTotalAmount.setText("₹" +
                    String.format(Locale.getDefault(), "%,d", total));

            // 🔷 Click Events
            cardViewOrder.setOnClickListener(v -> openOrderDetails(order));
            itemView.setOnClickListener(v -> openOrderDetails(order));

            btnViewDetails.setOnClickListener(v -> {
                openOrderDetails(order);
                if (onOrderClickListener != null) {
                    onOrderClickListener.onViewDetails(order);
                }
            });

            btnManage.setOnClickListener(v -> {
                if (onOrderClickListener != null) {
                    onOrderClickListener.onManage(order);
                } else {
                    openOrderDetails(order);
                }
            });
        }

        private void openOrderDetails(Order order) {
            Intent intent = new Intent(
                    itemView.getContext(),
                    OrderDetailsActivity.class
            );
            intent.putExtra("orderId", order.getOrderId());
            itemView.getContext().startActivity(intent);
        }

        private String capitalize(String text) {
            if (text == null || text.isEmpty()) return "Pending";
            return text.substring(0, 1).toUpperCase()
                    + text.substring(1).toLowerCase();
        }

        private void setStatusBadgeColor(String status) {

            if (status == null) status = "pending";

            int colorRes;

            switch (status.toLowerCase()) {
                case "accepted":
                    colorRes = R.color.status_accepted;
                    break;
                case "rejected":
                    colorRes = R.color.status_rejected;
                    break;
                case "completed":
                    colorRes = R.color.status_completed;
                    break;
                default:
                    colorRes = R.color.status_pending;
            }

            txtStatus.setTextColor(
                    itemView.getContext().getColor(colorRes)
            );
        }

        private String formatDate(long timestamp) {
            if (timestamp == 0) return "N/A";
            try {
                SimpleDateFormat sdf =
                        new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                return sdf.format(new Date(timestamp));
            } catch (Exception e) {
                return "N/A";
            }
        }
    }

    // ================= INTERFACE =================
    public interface OnOrderClickListener {
        void onViewDetails(Order order);
        void onManage(Order order);
    }
}