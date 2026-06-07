# 🏥 Medical Shop B2B Management System

A **Business-to-Business (B2B) platform** designed to connect **medical suppliers and medical shopkeepers** for efficient medicine distribution, order management, inventory tracking, and customer billing.

The system consists of:

• 📱 **Android Mobile Application** for Suppliers and Shopkeepers  
• 🌐 **Web Admin Panel** for user approval and monitoring  
• ☁️ **Firebase Backend** for real-time database and authentication  

---

# 🌐 Live Admin Dashboard

Admin Panel (Web Dashboard):  
https://medicalb2b.netlify.app/

---

# 📱 Android Application

The Android application allows **suppliers and shopkeepers** to interact with the system.

Download the APK from the **Releases section** of this repository.

---

# 🚀 Project Overview

This system digitizes the traditional **medical distribution workflow** between suppliers and medical stores.

It enables:

• Suppliers to upload medicines  
• Shopkeepers to order medicines from suppliers  
• Shopkeepers to generate **customer bills with GST and discounts**  
• Admin to approve users and verify licenses  
• Real-time tracking of medicine stock  

The entire system works using **Firebase Realtime Database** for instant data synchronization.

---

# 🛠 Technology Stack

## 📱 Android Application

• Java  
• Android Studio  
• XML Layouts  
• RecyclerView  
• Firebase Realtime Database  
• Firebase Authentication  
• Base64 Image Storage  
• PDF Invoice Generation  

---

## 🌐 Admin Panel (Web Dashboard)

• HTML5  
• CSS3  
• JavaScript (ES6)  
• Firebase Realtime Database SDK  
• Netlify Hosting  

---

## ⚙ Backend Services

• Firebase Realtime Database  
• Firebase Authentication  
• Firebase Security Rules  

---

# 👥 User Roles

## 🧑‍💼 Admin

Admin manages the entire platform.

Features:

• Approve or reject suppliers  
• Approve or reject shopkeepers  
• Verify business licenses  
• Monitor medicines and platform activity  
• Manage users  

---

## 🚚 Supplier

Suppliers provide medicines to shopkeepers.

Features:

• Register with business license  
• Wait for admin approval  
• Upload medicines  
• Manage medicine inventory  
• Receive orders from shopkeepers  
• Accept or reject orders  
• Track supplier stock  

---

## 🏪 Shopkeeper

Shopkeepers purchase medicines and sell them to customers.

Features:

• Browse supplier medicines  
• Search medicines  
• Add medicines to cart  
• Place supplier orders  
• Track order status  
• Maintain shop inventory  
• Generate **customer bills**  
• Apply **GST and discounts**  
• Generate **PDF invoices**  
• View reorder suggestions  

---

# 🔁 System Workflow

## Step 1 — Registration

• Supplier or Shopkeeper registers  
• Data stored in:
bills/{billId}
PDF invoice is generated for the customer.

---

# 📊 Firebase Database Structure

Main database nodes:
users suppliers shopkeepers shops supplierOrders bills supplierMedicinesPending approvedMedicines reorders
---

# 🔐 Security Features

• Firebase Authentication for secure login  
• Role-based access control  
• Firebase Security Rules  
• Admin-only approval operations  
• Input validation and error handling  

---

# 📦 Key Features

✔ Supplier & Shopkeeper Registration  
✔ Admin Approval System  
✔ Medicine Inventory Management  
✔ B2B Medicine Ordering  
✔ Customer Billing System  
✔ GST & Discount Calculation  
✔ PDF Bill Generation  
✔ Order Tracking  
✔ Reorder Suggestions  
✔ Real-Time Database Updates  
✔ Role-Based Access Control  

---

## 📁 Project Structure

```
Medical-Shop-B2B
│
├── Android-App
│   ├── Activities
│   │   ├── LoginActivity.java
│   │   ├── RegisterActivity.java
│   │   ├── AddMedicineActivity.java
│   │   ├── CartActivity.java
│   │   └── OrderDetailsActivity.java
│   │
│   ├── Adapters
│   │   ├── MedicineAdapter.java
│   │   ├── SupplierCartAdapter.java
│   │   └── OrderAdapter.java
│   │
│   ├── Models
│   │   ├── Medicine.java
│   │   ├── CartItem.java
│   │   ├── Order.java
│   │   └── User.java
│   │
│   ├── Utils
│   │   ├── MedicineKeyUtil.java
│   │   ├── SupplierCartManager.java
│   │   └── PdfInvoiceGenerator.java
│   │
│   └── Firebase
│       ├── FirebaseAuthHelper.java
│       └── FirebaseDatabaseHelper.java
│
├── Admin-Web-Panel
│   ├── index.html
│   ├── dashboard.html
│   ├── approvals.js
│   ├── medicines.js
│   ├── analytics.js
│   ├── auth.js
│   └── style.css
│
├── Firebase
│   ├── database.rules.json
│   └── firebase-config.js
│
└── README.md
```

# 📈 Future Improvements

• AI-based medicine demand prediction  
• Barcode scanning for medicines  
• Online payment integration  
• Multi-store analytics  
• Supplier rating system  

---

# 👨‍💻 Developed By

**Raviraj Choudhari**

Android Developer  
B2B Application Developer

---
