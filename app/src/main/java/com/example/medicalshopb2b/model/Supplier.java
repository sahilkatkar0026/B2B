package com.example.medicalshopb2b.model;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Supplier {

    private String supplierId;
    private String name;

    // 🔥 Required empty constructor for Firebase
    public Supplier() {
    }

    // ✅ GETTERS
    public String getSupplierId() {
        return supplierId;
    }

    public String getName() {
        return name;
    }

    // ✅ SETTERS (IMPORTANT)
    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    public void setName(String name) {
        this.name = name;
    }
}
