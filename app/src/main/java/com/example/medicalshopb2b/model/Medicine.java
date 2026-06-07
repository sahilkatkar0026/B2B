package com.example.medicalshopb2b.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Medicine {

    private String medicineId;
    private String supplierId;

    private String name;
    private String brand;
    private String medicineKey;
    private String unit;

    private String imageBase64;

    private Long mfgDate;
    private Long expiryDate;

    private Integer price;
    private Integer stock;
    private int selectedQty;

    private Integer totalSold;
    private Long lastSoldAt;

    // ===== ADMIN =====
    private String approvalStatus;
    private Long approvedAt;
    private String rejectedReason;

    public Medicine() {}

    // ===== GETTERS =====

    public String getMedicineId() { return medicineId == null ? "" : medicineId; }
    public String getSupplierId() { return supplierId == null ? "" : supplierId; }
    public String getName() { return name == null ? "" : name; }
    public String getBrand() { return brand == null ? "" : brand; }
    public String getMedicineKey() { return medicineKey == null ? "" : medicineKey; }
    public String getUnit() { return unit == null ? "Pcs" : unit; }
    public int getPrice() { return price == null ? 0 : price; }
    public int getStock() { return stock == null ? 0 : stock; }
    public String getImageBase64() { return imageBase64 == null ? "" : imageBase64; }
    public long getMfgDate() { return mfgDate == null ? 0L : mfgDate; }
    public long getExpiryDate() { return expiryDate == null ? 0L : expiryDate; }
    public int getTotalSold() { return totalSold == null ? 0 : totalSold; }
    public long getLastSoldAt() { return lastSoldAt == null ? 0L : lastSoldAt; }
    public int getSelectedQty() { return selectedQty; }

    public String getApprovalStatus() { return approvalStatus == null ? "pending" : approvalStatus; }
    public long getApprovedAt() { return approvedAt == null ? 0L : approvedAt; }
    public String getRejectedReason() { return rejectedReason == null ? "" : rejectedReason; }

    // ===== SETTERS =====

    public void setMedicineId(String medicineId) { this.medicineId = medicineId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }
    public void setName(String name) { this.name = name; }
    public void setBrand(String brand) { this.brand = brand; }
    public void setMedicineKey(String medicineKey) { this.medicineKey = medicineKey; }
    public void setUnit(String unit) { this.unit = unit; }
    public void setPrice(Integer price) { this.price = price == null ? 0 : Math.max(price, 0); }
    public void setStock(Integer stock) { this.stock = stock == null ? 0 : Math.max(stock, 0); }
    public void setSelectedQty(int selectedQty) { this.selectedQty = selectedQty; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }
    public void setMfgDate(long mfgDate) { this.mfgDate = mfgDate; }
    public void setExpiryDate(long expiryDate) { this.expiryDate = expiryDate; }
    public void setTotalSold(Integer totalSold) { this.totalSold = totalSold; }
    public void setLastSoldAt(long lastSoldAt) { this.lastSoldAt = lastSoldAt; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }
    public void setApprovedAt(Long approvedAt) { this.approvedAt = approvedAt; }
    public void setRejectedReason(String rejectedReason) { this.rejectedReason = rejectedReason; }

    // ===== HELPERS =====

    @Exclude
    public boolean isApproved() {
        return "approved".equalsIgnoreCase(getApprovalStatus());
    }

    @Exclude
    public boolean isExpired() {
        return getExpiryDate() > 0 &&
                System.currentTimeMillis() > getExpiryDate();
    }

    @Exclude
    public boolean isInStock() {
        return getStock() > 0;
    }

    // ===== FIREBASE MAP =====

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("medicineId", getMedicineId());
        map.put("supplierId", getSupplierId());
        map.put("name", getName());
        map.put("brand", getBrand());
        map.put("medicineKey", getMedicineKey());
        map.put("unit", getUnit());
        map.put("price", getPrice());
        map.put("stock", getStock());
        map.put("imageBase64", getImageBase64());
        map.put("mfgDate", getMfgDate());
        map.put("expiryDate", getExpiryDate());
        map.put("approvalStatus", getApprovalStatus());
        map.put("approvedAt", getApprovedAt());
        map.put("rejectedReason", getRejectedReason());
        return map;
    }
}