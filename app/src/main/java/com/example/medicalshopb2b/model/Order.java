package com.example.medicalshopb2b.model;

import java.util.List;

public class Order {

    private String orderId;
    private String userId;
    private String supplierId;
    private String supplierName;
    private List<CartItem> items;

    private int subtotal;
    private int gst;
    private int discount;
    private int total;

    private String status;
    private long timestamp;
    private long acceptedAt;

    private boolean reorder;

    // ✅ We will store ONLY file name here
    private String invoicePath;

    public Order() {}

    public String getOrderId() { return orderId; }
    public String getUserId() { return userId; }
    public String getSupplierId() { return supplierId; }
    public String getSupplierName() { return supplierName; }
    public List<CartItem> getItems() { return items; }
    public int getSubtotal() { return subtotal; }
    public int getGst() { return gst; }
    public int getDiscount() { return discount; }
    public int getTotal() { return total; }
    public String getStatus() { return status == null ? "pending" : status; }
    public long getTimestamp() { return timestamp; }
    public long getAcceptedAt() { return acceptedAt; }
    public boolean isReorder() { return reorder; }

    // ✅ IMPORTANT
    public String getInvoicePath() { return invoicePath; }

    public void setOrderId(String orderId) { this.orderId = orderId; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public void setItems(List<CartItem> items) { this.items = items; }
    public void setSubtotal(int subtotal) { this.subtotal = subtotal; }
    public void setGst(int gst) { this.gst = gst; }
    public void setDiscount(int discount) { this.discount = discount; }
    public void setTotal(int total) { this.total = total; }
    public void setStatus(String status) { this.status = status; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setAcceptedAt(long acceptedAt) { this.acceptedAt = acceptedAt; }
    public void setReorder(boolean reorder) { this.reorder = reorder; }

    // ✅ IMPORTANT
    public void setInvoicePath(String invoicePath) {
        this.invoicePath = invoicePath;
    }
}