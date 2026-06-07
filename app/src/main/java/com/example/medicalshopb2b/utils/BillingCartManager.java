package com.example.medicalshopb2b.utils;

import com.example.medicalshopb2b.model.Medicine;
import java.util.ArrayList;
import java.util.List;

public class BillingCartManager {

    private static final List<Medicine> billingList = new ArrayList<>();

    // ➕ Add medicine to bill
    public static void addMedicine(Medicine source) {

        if (source == null || source.getMedicineId() == null) return;

        // 🔁 If already added → increase quantity
        for (Medicine m : billingList) {
            if (m.getMedicineId().equals(source.getMedicineId())) {
                m.setSelectedQty(m.getSelectedQty() + 1); // ✅ FIXED
                return;
            }
        }

        // 🆕 Create clean copy
        Medicine copy = new Medicine();
        copy.setMedicineId(source.getMedicineId());
        copy.setName(source.getName());
        copy.setPrice(source.getPrice());
        copy.setStock(source.getStock());
        copy.setSelectedQty(1); // ✅ IMPORTANT

        billingList.add(copy);
    }

    public static List<Medicine> getItems() {
        return billingList;
    }

    public static void remove(Medicine m) {
        billingList.remove(m);
    }

    public static void clear() {
        billingList.clear();
    }
}
