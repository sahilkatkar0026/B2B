package com.example.medicalshopb2b.utils;

public class MedicineKeyUtil {

    public static String generateKey(String name) {
        if (name == null) return "";
        return name
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "")
                .trim();
    }
}