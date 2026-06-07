package com.example.medicalshopb2b.model;

public class ReorderRequest {

    private String medicineKey;
    private String medicineId;
    private String medicineName;
    private String brand;
    private double price;

    private int currentStock;
    private int minStockLevel;      // Threshold to trigger reorder
    private int suggestedQty;
    private int maxStockLevel;      // Maximum stock to maintain

    private String status;          // pending / ordered / completed
    private long createdAt;
    private long lastUpdated;

    private boolean autoReorderEnabled;
    private String supplierId;

    // 🔥 REQUIRED FIELD - Firebase is looking for this!
    private boolean stockSufficient;  // true if stock > minStockLevel

    // 🔥 REQUIRED for Firebase
    public ReorderRequest() {}

    // Constructor with essential fields
    public ReorderRequest(String medicineId, String medicineName,
                          int currentStock, int minStockLevel, int suggestedQty) {
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.currentStock = currentStock;
        this.minStockLevel = minStockLevel;
        this.suggestedQty = suggestedQty;
        this.status = "pending";
        this.createdAt = System.currentTimeMillis();
        this.lastUpdated = System.currentTimeMillis();
        this.autoReorderEnabled = true;
        this.stockSufficient = currentStock > minStockLevel;  // Set initial value
    }

    // ---------- GETTERS & SETTERS ----------

    public String getMedicineKey() {
        return medicineKey;
    }

    public void setMedicineKey(String medicineKey) {
        this.medicineKey = medicineKey;
    }

    public String getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(String medicineId) {
        this.medicineId = medicineId;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getBrand() {
        return brand == null ? "N/A" : brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(int currentStock) {
        this.currentStock = currentStock;
        this.lastUpdated = System.currentTimeMillis();
        this.stockSufficient = currentStock > minStockLevel;  // Update when stock changes
    }

    public int getMinStockLevel() {
        return minStockLevel;
    }

    public void setMinStockLevel(int minStockLevel) {
        this.minStockLevel = minStockLevel;
    }

    public int getSuggestedQty() {
        return suggestedQty;
    }

    public void setSuggestedQty(int suggestedQty) {
        this.suggestedQty = suggestedQty;
    }

    public int getMaxStockLevel() {
        return maxStockLevel;
    }

    public void setMaxStockLevel(int maxStockLevel) {
        this.maxStockLevel = maxStockLevel;
    }

    public String getStatus() {
        return status == null ? "pending" : status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public boolean isAutoReorderEnabled() {
        return autoReorderEnabled;
    }

    public void setAutoReorderEnabled(boolean autoReorderEnabled) {
        this.autoReorderEnabled = autoReorderEnabled;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    // 🔥 NEW GETTER & SETTER FOR stockSufficient
    public boolean isStockSufficient() {
        return stockSufficient;
    }

    public void setStockSufficient(boolean stockSufficient) {
        this.stockSufficient = stockSufficient;
    }

    // 🔥 Check if reorder is needed based on current stock
    public boolean shouldReorder() {
        return currentStock <= minStockLevel && autoReorderEnabled;
    }
}