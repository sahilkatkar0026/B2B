package com.example.medicalshopb2b.utils;

import com.example.medicalshopb2b.model.CartItem;
import com.example.medicalshopb2b.model.Medicine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 🔒 SINGLE SOURCE OF TRUTH FOR SUPPLIER CART
 * Stores medicines with their ordered quantities
 */
public class SupplierCartManager {

    // medicineKey -> Medicine
    private static final Map<String, Medicine> medicineMap = new HashMap<>();

    // medicineKey -> quantity
    private static final Map<String, Integer> qtyMap = new HashMap<>();

    private SupplierCartManager() {}

    // ================= ADD / UPDATE =================

    public static void addMedicine(Medicine source, int qty) {

        if (source == null) return;

        String key = source.getMedicineKey();
        if (key == null || key.isEmpty()) return;

        medicineMap.put(key, source);

        Integer existingQty = qtyMap.get(key);
        if (existingQty == null) existingQty = 0;

        qtyMap.put(key, existingQty + Math.max(qty, 1));
    }

    // ✅ ADDED — set specific quantity (used by SupplierCartAdapter +/− buttons)
    public static void setQuantity(String medicineKey, int quantity) {
        if (quantity > 0) {
            qtyMap.put(medicineKey, quantity);
        }
    }

    // ================= GETTERS =================

    public static int getQuantity(String medicineKey) {
        Integer q = qtyMap.get(medicineKey);
        return q == null ? 0 : q;
    }

    public static List<Medicine> getItems() {
        return new ArrayList<>(medicineMap.values());
    }

    /**
     * 🔥 USED FOR ORDER CREATION
     * Now uses FULL CartItem constructor (11 params)
     */
    public static List<CartItem> getCartItems() {

        List<CartItem> cartItems = new ArrayList<>();

        for (Map.Entry<String, Medicine> entry : medicineMap.entrySet()) {

            String key = entry.getKey();
            Medicine med = entry.getValue();
            Integer qty = qtyMap.get(key);

            if (qty == null || qty <= 0) continue;

            CartItem item = new CartItem(
                    med.getName(),
                    med.getBrand(),
                    med.getPrice(),
                    qty,
                    med.getMedicineKey(),
                    med.getMedicineId(),
                    med.getSupplierId(),
                    med.getStock(),
                    med.getImageBase64(),
                    med.getMfgDate(),
                    med.getExpiryDate()
            );

            cartItems.add(item);
        }

        return cartItems;
    }

    // ================= REMOVE =================

    public static void remove(String medicineKey) {
        medicineMap.remove(medicineKey);
        qtyMap.remove(medicineKey);
    }

    public static void clear() {
        medicineMap.clear();
        qtyMap.clear();
    }

    // ================= VALIDATION =================

    public static boolean isEmpty() {
        return medicineMap.isEmpty();
    }

    public static int getTotalItems() {
        return medicineMap.size();
    }
}