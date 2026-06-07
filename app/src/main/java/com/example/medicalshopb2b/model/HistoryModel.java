package com.example.medicalshopb2b.model;

public class HistoryModel {

    private String billId;   // 🔥 IMPORTANT
    private String customerName;
    private String customerMobile;
    private double totalAmount;
    private long createdAt;

    public HistoryModel() {}

    public String getBillId() { return billId; }
    public void setBillId(String billId) { this.billId = billId; }

    public String getCustomerName() { return customerName; }
    public String getCustomerMobile() { return customerMobile; }
    public double getTotalAmount() { return totalAmount; }
    public long getCreatedAt() { return createdAt; }

    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public void setCustomerMobile(String customerMobile) { this.customerMobile = customerMobile; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}