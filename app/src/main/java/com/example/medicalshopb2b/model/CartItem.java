package com.example.medicalshopb2b.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class CartItem implements Parcelable {

    private String name;
    private String brand;
    private double price;
    private int quantity;

    private String medicineKey;
    private String medicineId;
    private String supplierId;

    private int availableStock;
    private String imageBase64;

    // Manufacturing & Expiry
    private long mfgDate;
    private long expiryDate;

    // Required empty constructor for Firebase
    public CartItem() {
    }

    public CartItem(String name,
                    String brand,
                    double price,
                    int quantity,
                    String medicineKey,
                    String medicineId,
                    String supplierId,
                    int availableStock,
                    String imageBase64,
                    long mfgDate,
                    long expiryDate) {

        this.name = name;
        this.brand = brand;
        this.price = price;
        this.quantity = quantity;
        this.medicineKey = medicineKey;
        this.medicineId = medicineId;
        this.supplierId = supplierId;
        this.availableStock = availableStock;
        this.imageBase64 = imageBase64;
        this.mfgDate = mfgDate;
        this.expiryDate = expiryDate;
    }

    // ================= GETTERS =================

    public String getName() { return name; }
    public String getBrand() { return brand; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public String getMedicineKey() { return medicineKey; }
    public String getMedicineId() { return medicineId; }
    public String getSupplierId() { return supplierId; }
    public int getAvailableStock() { return availableStock; }
    public String getImageBase64() { return imageBase64; }
    public long getMfgDate() { return mfgDate; }
    public long getExpiryDate() { return expiryDate; }

    // ================= SETTERS =================

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setAvailableStock(int availableStock) {
        this.availableStock = availableStock;
    }

    public void setMfgDate(long mfgDate) {
        this.mfgDate = mfgDate;
    }

    public void setExpiryDate(long expiryDate) {
        this.expiryDate = expiryDate;
    }

    // ================= EXPIRY CHECK =================

    public boolean isExpired() {
        return expiryDate > 0 &&
                System.currentTimeMillis() > expiryDate;
    }

    public boolean isNearExpiry(long warningDays) {
        if (expiryDate <= 0) return false;

        long warningMillis = warningDays * 24L * 60L * 60L * 1000L;
        long currentTime = System.currentTimeMillis();

        return expiryDate > currentTime &&
                expiryDate - currentTime <= warningMillis;
    }

    // ================= PARCELABLE =================

    protected CartItem(Parcel in) {
        name = in.readString();
        brand = in.readString();
        price = in.readDouble();
        quantity = in.readInt();
        medicineKey = in.readString();
        medicineId = in.readString();
        supplierId = in.readString();
        availableStock = in.readInt();
        imageBase64 = in.readString();
        mfgDate = in.readLong();
        expiryDate = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(brand);
        dest.writeDouble(price);
        dest.writeInt(quantity);
        dest.writeString(medicineKey);
        dest.writeString(medicineId);
        dest.writeString(supplierId);
        dest.writeInt(availableStock);
        dest.writeString(imageBase64);
        dest.writeLong(mfgDate);
        dest.writeLong(expiryDate);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CartItem> CREATOR =
            new Creator<CartItem>() {
                @Override
                public CartItem createFromParcel(Parcel in) {
                    return new CartItem(in);
                }

                @Override
                public CartItem[] newArray(int size) {
                    return new CartItem[size];
                }
            };
}