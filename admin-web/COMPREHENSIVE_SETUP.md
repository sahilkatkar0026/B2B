# Medical B2B Admin Panel - Complete Setup Guide

## 🎯 System Overview

This is a **Medical B2B Administration Panel** with the following features:

### Role-Based Access Control
- **Admin**: Full platform access, approval authority
- **Supplier**: Can register with business license, pending approval
- **Shopkeeper**: Can register with business license, pending approval

### Key Features
✅ Secure Firebase Authentication  
✅ License Verification for Suppliers & Shopkeepers  
✅ Approval Workflow with Email Notifications  
✅ Role-Based Authorization  
✅ Sales Analytics Dashboard  
✅ Medicine & Fraud Monitoring  
✅ Professional Light Theme (Sky Blue)  

---

## 📋 System Architecture

### Frontend
- **HTML5**: Clean, semantic markup
- **CSS3**: Professional light theme with sky blue (#0ea5e9)
- **JavaScript (ES6+)**: Modular architecture
- **Chart.js**: Analytics visualization

### Backend
- **Firebase Authentication**: Secure user login
- **Firebase Realtime Database**: Data storage
- **Firebase Cloud Functions** (optional): Email notifications

### Database Structure
```
/users/{uid}
  ├── name: string
  ├── email: string
  ├── role: "admin" | "supplier" | "shopkeeper"
  ├── registrationStatus: "pending" | "approved" | "rejected"
  ├── licenseImageBase64: string (base64 encoded image)
  ├── createdAt: timestamp
  └── [supplier/shopkeeper specific fields]

/suppliers/{uid}
  ├── name: string
  ├── email: string
  ├── companyName: string
  ├── isActive: boolean
  └── createdAt: timestamp

/shopkeepers/{uid}
  ├── name: string
  ├── email: string
  ├── shopName: string
  ├── isActive: boolean
  └── createdAt: timestamp
```

---

## 🚀 Getting Started

### 1. Firebase Setup

#### Create a Firebase Project
1. Go to [Firebase Console](https://console.firebase.google.com)
2. Create a new project
3. Enable **Authentication** (Email/Password)
4. Enable **Realtime Database**
5. Copy your Firebase config

#### Update Firebase Config
Edit `js/firebase-config.js`:
```javascript
const firebaseConfig = {
  apiKey: "YOUR_API_KEY",
  authDomain: "your-app.firebaseapp.com",
  databaseURL: "https://your-app.firebaseio.com",
  projectId: "your-app",
  storageBucket: "your-app.appspot.com",
  messagingSenderId: "YOUR_SENDER_ID",
  appId: "YOUR_APP_ID"
};
```

#### Create Admin User
In Firebase Console:
1. Go to Authentication → Users
2. Create a new user (email: admin@example.com, password: secure123)
3. In Realtime Database, add user profile:
```json
{
  "users": {
    "uid_from_auth": {
      "uid": "uid_from_auth",
      "name": "Admin Users",
      "email": "admin@example.com",
      "role": "admin",
      "registrationStatus": "approved",
      "createdAt": 1644567890
    }
  }
}
```

#### Database Security Rules
Update your Firestore rules to:
```json
{
  "rules": {
    "users": {
      ".indexOn": ["role", "registrationStatus"],
      "$uid": {
        ".read": "auth.uid === $uid || root.child('users').child(auth.uid).child('role').val() === 'admin'",
        ".write": "auth.uid === $uid || root.child('users').child(auth.uid).child('role').val() === 'admin'"
      }
    },
    "suppliers": {
      "$uid": {
        ".read": true,
        ".write": "root.child('users').child(auth.uid).child('role').val() === 'admin'"
      }
    },
    "shopkeepers": {
      "$uid": {
        ".read": true,
        ".write": "root.child('users').child(auth.uid).child('role').val() === 'admin'"
      }
    }
  }
}
```

### 2. Email Service Setup

The system supports automated email notifications. See [EMAIL_SERVICE_SETUP.md](EMAIL_SERVICE_SETUP.md) for detailed configuration.

**Recommended**: Use Firebase Cloud Functions or SendGrid

### 3. Run the Application

```bash
# Install dependencies
npm install

# Start development server
npx http-server -p 8080 -c-1

# OR use the provided batch file (Windows)
serve.bat

# Open in browser
http://localhost:8080
```

---

## 📖 User Workflows

### Admin Workflow
1. **Login**: Navigate to `index.html` → Enter admin credentials
2. **Dashboard Access**: Admin has full access to all sections
3. **Approve Users**: 
   - Go to "Supplier Verification" or "Shopkeeper Verification"
   - Review license images
   - Click "Approve" or "Reject"
   - Automated email sent to user

### Supplier Workflow
1. **Register**: Go to `register.html` → Select "Supplier Registration"
2. **Fill Form**: 
   - Name, Email, Company Name
   - Password confirmation
   - Upload business license image
3. **Submit**: Account created, status = "pending"
4. **Wait for Approval**: Admin verifies license
5. **Receive Email**: Upon approval, get email with login link
6. **Login**: Use email/password to access account

### Shopkeeper Workflow
Same as Supplier, but:
- Select "Shopkeeper Registration"
- Enter Shop Name instead of Company Name
- Gets added to shopkeepers section in admin panel

---

## 🔑 Authentication Flow

### Login Process
```
User enters credentials
    ↓
Firebase authenticates (Auth/password validation)
    ↓
System checks registration status
    ↓
IF role === 'admin' → ALLOW
ELSE IF (role === 'supplier' OR 'shopkeeper') AND status === 'approved' → ALLOW
ELSE → DENY with message "Pending approval"
    ↓
Redirect to dashboard or login page
```

### Approval Process
```
Admin clicks "Approve" on supplier/shopkeeper
    ↓
Update in database: registrationStatus = 'approved'
    ↓
Update in suppliers/shopkeepers collection
    ↓
Send approval email with login link
    ↓
User receives email and can now login
```

---

## 📁 Project Structure

```
B2BAdmin/
├── index.html                 # Admin login page
├── register.html              # Supplier/Shopkeeper registration
├── dashboard.html             # Main admin dashboard
├── login.html                 # Redirect to index.html
├── css/
│   └── style.css              # Professional light theme
├── js/
│   ├── firebase-config.js     # Firebase configuration
│   ├── auth.js                # Authentication logic
│   ├── registration.js        # Registration form handling
│   ├── dashboard.js           # Dashboard initialization
│   ├── approvals.js           # Supplier/shopkeeper approval logic
│   ├── profile.js             # Admin profile management
│   ├── analytics.js           # Sales analytics
│   ├── medicines.js           # Medicine monitoring
│   ├── stats.js               # Statistics calculation
│   ├── email-service.js       # Email notification service
│   └── fraud.js               # Fraud detection
├── firebase.json              # Firebase configuration
├── database.rules.json        # Database security rules
├── package.json               # Dependencies
├── serve.bat                  # Windows server launcher
├── DEPLOYMENT.md              # Deployment guide
├── EMAIL_SERVICE_SETUP.md     # Email configuration
└── COMPREHENSIVE_SETUP.md     # This file
```

---

## 🔐 Security Features

### Authentication
- Firebase Auth with email/password
- Secure session management
- Real-time role verification

### Database Security
- Role-based read/write permissions
- Index on role and registration status
- Admin-only write access to approvals

### Frontend Security
- All routes require authentication verification
- Admin routes verify role in database
- No sensitive data in localStorage beyond session tokens

### Input Validation
- Email format validation
- Password strength requirements
- File type and size validation
- Base64 encoding for file uploads

---

## 🎨 Customization

### Color Scheme
The app uses **Sky Blue** (#0ea5e9) as primary color.

Edit `css/style.css` to change:
```css
--primary: #0ea5e9;        /* Sky blue */
--primary-dark: #0284c7;
--primary-light: #e0f2fe;
```

### Branding
Update in HTML files:
- Logo text: "MB" → your brand
- Title: "Medical B2B Admin" → your title
- Tagline: Update in headers

### Database Collections
Add new collections as needed:
```javascript
// In approvals.js or dashboard.js
await update(ref(db), {
  'your-collection/uid/field': value
});
```

---

## 🐛 Troubleshooting

### "Access denied" message on login
↳ Check `users/{uid}/role` in Firebase. Should be "admin", "supplier", or "shopkeeper"
↳ Verify `registrationStatus`. Suppliers/Shopkeepers need "approved" status

### Email not sent
↳ Check EMAIL_SERVICE_SETUP.md for configuration
↳ Approval still works even if email fails
↳ Check browser console for errors

### License image not showing
↳ Verify file size < 5MB
↳ Confirm file is valid image (JPG, PNG)
↳ Check browser console for errors

### Database queries returning empty
↳ Verify `.indexOn` for "role" and "registrationStatus"
↳ Check database rules allow read access
↳ Confirm data exists in Firebase

### Forms not submitting
↳ Check browser console for JavaScript errors
↳ Verify Firebase config is correct
↳ Check network tab to see if API calls are succeeding

---

## 📧 Email Templates

### Approval Email
- Subject: "🎉 Your [role] Account Approved!"
- Body: Welcome message + login link
- Automatically sent when admin clicks "Approve"

### Rejection Email
- Subject: "❌ Your [role] Account Registration Status"
- Body: Rejection message + support contact
- Sent when admin clicks "Reject"

Customize templates in `js/email-service.js`

---

## 📊 Analytics

The dashboard includes:
- **Total Orders**: Count of all orders
- **Total Sales**: Sum of all transactions
- **Average Order Value**: Average per order
- **Weekly Orders**: Orders from last 7 days
- **Sales Line Chart**: Trend over 30 days
- **Category Pie Chart**: Sales by medicine category
- **Top Products**: Bar chart of best sellers
- **Order Status**: Distribution of order states

---

## 🔄 Real-time Updates

The application uses Firebase real-time listeners:
- Supplier/Shopkeeper list updates instantly
- Analytics refresh as new orders added
- Medicine inventory changes reflected immediately
- Fraud alerts trigger in real-time

---

## 📞 Support

For issues or questions:
1. Check browser console for errors
2. Review database rules in Firebase
3. Verify Firebase config is correct
4. Check network requests in DevTools
5. Review error messages carefully

---

## 📝 License

This project is proprietary. All rights reserved.

---

## ✅ Checklist for Production

- [ ] Create production Firebase project
- [ ] Update Firebase config for production
- [ ] Set up email service (SendGrid/Firebase Functions)
- [ ] Create admin user account
- [ ] Test supplier/shopkeeper registration
- [ ] Test approval workflow
- [ ] Test email notifications
- [ ] Configure database security rules
- [ ] Deploy to hosting service
- [ ] SSL/TLS certificate enabled
- [ ] Test on multiple browsers
- [ ] Load testing performed
- [ ] Backup strategy implemented

---

**Last Updated**: February 2026
**Version**: 2.0 (Shopkeeper Verification + Email Notifications)
