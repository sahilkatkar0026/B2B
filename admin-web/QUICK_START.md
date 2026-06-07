# Quick Start Guide - Medical B2B Admin Panel

## 🚀 5-Minute Setup

### 1. Configure Firebase
```javascript
// Edit: js/firebase-config.js
const firebaseConfig = {
  apiKey: "YOUR_KEY",
  authDomain: "your-project.firebaseapp.com",
  databaseURL: "https://your-project.firebaseio.com",
  projectId: "your-project",
  storageBucket: "your-project.appspot.com",
  messagingSenderId: "XXXX",
  appId: "XXXX"
};
```

### 2. Create Admin User
1. Firebase Console → Authentication → Add user
2. Email: admin@example.com | Password: secure123
3. Copy the UID

### 3. Add Admin in Database
Firebase Console → Realtime Database → Add:
```json
{
  "users": {
    "YOUR_UID_HERE": {
      "uid": "YOUR_UID_HERE",
      "name": "Admin User",
      "email": "admin@example.com",
      "role": "admin",
      "registrationStatus": "approved"
    }
  }
}
```

### 4. Run the App
```bash
npx http-server -p 8080 -c-1
# Open http://localhost:8080
```

### 5. Login
- Email: admin@example.com
- Password: secure123

---

## 📋 User Roles & What They Can Do

| Role | Can Do | Needs Approval? |
|------|--------|-----------------|
| **Admin** | Everything - approve users, manage medicines, view analytics | No |
| **Supplier** | Register company, upload license, manage products | Yes |
| **Shopkeeper** | Register shop, upload license, place orders | Yes |

---

## 🔑 Key URLs

| URL | Purpose |
|-----|---------|
| `http://localhost:8080/index.html` | Login page (all users) |
| `http://localhost:8080/register.html` | Registration (suppliers/shopkeepers) |
| `http://localhost:8080/dashboard.html` | Admin dashboard (admin only) |

---

## ✅ Test These Flows

### Flow 1: Register as Supplier
1. Go to `register.html`
2. Click **Supplier Registration** tab
3. Fill form (use test@supplier.com)
4. Upload any image as license
5. Click Register

### Flow 2: Approve Supplier (As Admin)
1. Login as admin
2. Go to **Supplier Verification**
3. Click **Preview** to see license
4. Click **Approve**
5. Check email (if configured)

### Flow 3: Login as Approved Supplier
1. Register supplier (see Flow 1)
2. Approve supplier (see Flow 2)
3. Go to `index.html`
4. Login with supplier email
5. Should see dashboard

### Flow 4: Register as Shopkeeper
1. Go to `register.html`
2. Click **Shopkeeper Registration** tab
3. Fill form (use shop@example.com)
4. Upload any image as license
5. Click Register

### Flow 5: Approve Shopkeeper (As Admin)
1. Login as admin
2. Go to **Shopkeeper Verification**
3. Click **Preview** to see license
4. Click **Approve**
5. Check email (if configured)

---

## 🖼️ License Image Handling

### Supported Formats
- JPG
- PNG

### Size Limits
- Max: 5MB per file
- Recommended: < 2MB for faster loading

### How It Works
- User uploads image during registration
- Frontend converts to Base64 (text)
- Stored in Firebase Realtime Database
- Admin can preview in popup

---

## 📧 Email Setup (Optional)

**Without Email Service**: System works fine, just won't send emails

**With Email Service**: Users get notified of approval/rejection

See `EMAIL_SERVICE_SETUP.md` for configuration options:
- Firebase Cloud Functions (Recommended)
- SendGrid
- Mailgun
- Netlify Functions

---

## 🔐 Default Test Credentials

```
Admin Account:
Email: admin@example.com
Password: secure123
```

⚠️ **Change these before production!**

---

## 📊 What Admin Can Do

**Dashboard Section**: See KPIs + charts
**My Profile**: Change password
**Sales Analytics**: View orders + trends
**Supplier Verification**: Approve/reject suppliers
**Shopkeeper Verification**: Approve/reject shopkeepers
**Medicine Approval**: Manage product inventory
**Fraud Monitoring**: Detect suspicious patterns

---

## 🛠️ Troubleshooting

| Problem | Solution |
|---------|----------|
| **Login fails** | Check Firebase config in `js/firebase-config.js` |
| **Can't register** | Verify Realtime Database is enabled |
| **License not uploading** | Check file size < 5MB and format is JPG/PNG |
| **Email not sent** | Email service optional - see `EMAIL_SERVICE_SETUP.md` |
| **Can't access Shopkeeper tab** | Verify latest `approvals.js` is loaded |

---

## 📁 Important Files to Know

```
js/
├── firebase-config.js ← Edit with your Firebase keys
├── auth.js ← Login/authentication logic
├── approvals.js ← Supplier/Shopkeeper approval
├── registration.js ← Registration form handling
├── email-service.js ← Email notifications
└── dashboard.js ← Dashboard bootstrap

css/
└── style.css ← Colors, layout, responsive design

html/
├── index.html ← Login page
├── register.html ← Registration page
└── dashboard.html ← Admin dashboard
```

---

## 🎨 Customizing Colors

All colors in: `css/style.css`

```css
:root {
  --primary: #0ea5e9;        /* Sky blue */
  --primary-dark: #0284c7;   /* Darker blue */
  --primary-light: #e0f2fe;  /* Light blue */
  --success: #10b981;        /* Green */
  --danger: #ef4444;         /* Red */
}
```

---

## 📱 Device Support

✅ **Desktop** (1920x1080+)
✅ **Tablet** (768px+)
✅ **Mobile** (375px+)

All responsive with mobile-first design.

---

## 🚀 Next: Production Deployment

1. Create production Firebase project
2. Update `firebase-config.js` with production keys
3. Set up email service (Firebase Functions/SendGrid)
4. Deploy to Firebase Hosting or your server
5. Configure SSL/HTTPS
6. Create production admin account
7. Update security rules
8. Test all flows

**See `COMPREHENSIVE_SETUP.md` for full production guide**

---

## 📞 Help Resources

- **Firebase Setup Issue**: Check `COMPREHENSIVE_SETUP.md`
- **Email Not Working**: Check `EMAIL_SERVICE_SETUP.md`
- **Implementation Details**: Check `SHOPKEEPER_IMPLEMENTATION.md`
- **Quick Reference**: You're reading it! 😊
- **Console Errors**: Open DevTools (F12) and check Console tab
- **Database Issues**: Check Firebase Console → Realtime Database

---

## ✨ Key Features at a Glance

- ✅ Secure admin login
- ✅ Supplier & Shopkeeper registration
- ✅ License image upload & preview
- ✅ Admin approval workflow
- ✅ Email notifications (optional)
- ✅ Login blocking until approved
- ✅ Sales analytics dashboard
- ✅ Medicine inventory management
- ✅ Fraud detection alerts
- ✅ Professional UI (Sky Blue theme)
- ✅ Mobile responsive
- ✅ Real-time data updates

---

**Version 2.0 - Ready for Production!** 🎉
