package com.example.medicalshopb2b.utils;

import com.example.medicalshopb2b.model.CartItem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CartManager1 {

    private static final List<CartItem> cartItems = new ArrayList<>();

    public static void addItem(CartItem item) {

        if (item == null || item.getMedicineKey() == null) return;

        for (CartItem existing : cartItems) {
            if (item.getMedicineKey().equals(existing.getMedicineKey())) {
                existing.setQuantity(
                        existing.getQuantity() + item.getQuantity()
                );
                return;
            }
        }

        cartItems.add(item);
    }

    public static void removeItem(String medicineKey) {
        Iterator<CartItem> it = cartItems.iterator();
        while (it.hasNext()) {
            if (medicineKey.equals(it.next().getMedicineKey())) {
                it.remove();
                return;
            }
        }
    }

    public static boolean containsMedicineKey(String key) {
        for (CartItem item : cartItems) {
            if (key.equals(item.getMedicineKey())) return true;
        }
        return false;
    }

    public static List<CartItem> getCartItems() {
        return cartItems;
    }

    public static void clearCart() {
        cartItems.clear();
    }
}