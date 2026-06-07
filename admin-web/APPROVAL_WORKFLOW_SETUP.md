# Complete Approval Workflow Setup Guide

## System Overview

This guide explains how to set up the complete approval workflow where:
1. Users (Supplier/Shopkeeper) register with license
2. Admin approves/rejects in dashboard
3. Email notification is sent automatically
4. User can only login after approval

---

## 📋 Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    User Registration Flow                   │
└─────────────────────────────────────────────────────────────┘

1. User visits register.html
2. Fills form + uploads license image
3. registration.js stores data in Firebase (registrationStatus: pending)
4. User receives success message

┌─────────────────────────────────────────────────────────────┐
│                    Admin Approval Flow                      │
└─────────────────────────────────────────────────────────────┘

1. Admin logs in to dashboard.html
2. Clicks "Supplier Verification" or "Shopkeeper Verification" tab
3. Review pending registrations + license preview
4. Click "Approve" or "Reject"
5. approvals.js updates Firebase (registrationStatus: approved/rejected)
6. email-service.js sends email via Node.js backend server
7. Server (server.js) sends actual email

┌─────────────────────────────────────────────────────────────┐
│                    User Login After Approval                │
└─────────────────────────────────────────────────────────────┘

1. User receives approval email
2. Visits index.html and enters credentials
3. auth.js checks:
   - User is authenticated ✓
   - registrationStatus === 'approved' ✓
4. User is redirected to dashboard.html
```

---

## 🚀 Step-by-Step Setup

### Step 1: Firebase Setup (Already Done ✓)

Your Firebase project is already configured with:
- **Project ID**: medicalshopb2b
- **Auth**: Email/Password enabled
- **Database**: Realtime Database active

### Step 2: Install Node.js Backend Dependencies

1. **Install packages**:
   ```bash
   cd d:\B2BAdmin
   npm install express cors dotenv nodemailer nodemailer-sendgrid-transport
   ```

2. **Create .env file from template**:
   ```bash
   copy .env.example .env
   ```

3. **Edit .env file** with your email credentials

---

## 📧 Email Service Configuration

### Option A: Gmail (Recommended for Testing)

1. **Enable 2-Step Verification**:
   - Go to [Google Account Settings](https://myaccount.google.com)
   - Security → 2-Step Verification → Turn on

2. **Create App Password**:
   - Go to [App Passwords](https://myaccount.google.com/apppasswords)
   - Select "Mail" and "Windows Computer"
   - Google generates a 16-character password

3. **Update .env**:
   ```env
   EMAIL_PROVIDER=gmail
   GMAIL_USER=your-email@gmail.com
   GMAIL_PASSWORD=your-16-char-password
   EMAIL_FROM=your-email@gmail.com
   ```

### Option B: SendGrid (Production Recommended)

1. **Sign up**: https://sendgrid.com

2. **Create API Key**:
   - Settings → API Keys → Create API Key (Full Access)
   - Copy the key

3. **Update .env**:
   ```env
   EMAIL_PROVIDER=sendgrid
   SENDGRID_API_KEY=your-api-key
   EMAIL_FROM=noreply@yourdomain.com
   ```

### Option C: Mailgun

1. **Sign up**: https://mailgun.com

2. **Get SMTP credentials**:
   - Domain Settings → SMTP Credentials
   - Get username and password

3. **Update .env**:
   ```env
   EMAIL_PROVIDER=mailgun
   MAILGUN_USER=postmaster@your-domain.com
   MAILGUN_PASSWORD=your-password
   EMAIL_FROM=noreply@your-domain.com
   ```

---

## ▶️ Running the System

### Terminal 1: Start Backend Email Server
```bash
cd d:\B2BAdmin
node server.js
```

Output should show:
```
╔════════════════════════════════════════════════════════════╗
║   Medical B2B Admin Panel - Email Service                  ║
║   Server running on http://localhost:3000                  ║
║   Email Provider: gmail                                    ║
╚════════════════════════════════════════════════════════════╝
```

### Terminal 2: Start Frontend Server
```bash
cd d:\B2BAdmin
npx serve -l 8080
```

Or use:
```bash
npm run dev
```

---

## 🧪 Testing the Approval Workflow

### Test 1: Register as Supplier

1. Open http://localhost:8080/register.html
2. Click "Supplier Registration" tab
3. Fill form:
   - Name: John Supplier
   - Email: john@supplier.com
   - Company: ABC Medicines
   - Password: test123456
   - Confirm: test123456
   - License: Upload any image
4. Click "Register as Supplier"
5. See success message

### Test 2: Admin Login

1. Open http://localhost:8080/index.html
2. Login with admin account:
   - Email: admin@example.com (or your admin email)
   - Password: (your admin password)
3. You should see Dashboard

### Test 3: Approve Registration

1. In dashboard, click "🏭 Supplier Verification"
2. You should see "John Supplier" with status "Pending"
3. Click "Preview" to see license image
4. Click "Approve"
5. Wait 2-3 seconds

### Test 4: Check Email

1. Check inbox of john@supplier.com
2. Look for email: "🎉 Your supplier Account Approved"
3. Click "Log In to Your Account" link

### Test 5: Login as Approved User

1. Open http://localhost:8080/index.html
2. Login with new supplier:
   - Email: john@supplier.com
   - Password: test123456
3. You should be redirected to dashboard.html
4. You're now logged in as a supplier!

---

## 📊 Database Structure

After approval, here's what gets saved:

```json
{
  "users": {
    "uid_of_supplier": {
      "uid": "uid_of_supplier",
      "name": "John Supplier",
      "email": "john@supplier.com",
      "companyName": "ABC Medicines",
      "role": "supplier",
      "registrationStatus": "approved",
      "licenseImageBase64": "data:image/png;base64,...",
      "createdAt": 1708677012345
    }
  },
  "suppliers": {
    "uid_of_supplier": {
      "name": "John Supplier",
      "email": "john@supplier.com",
      "companyName": "ABC Medicines",
      "isActive": true
    }
  }
}
```

---

## 🔗 API Endpoints

### Backend Server (localhost:3000)

#### 1. Send Email
```
POST /api/send-email
Content-Type: application/json

Body:
{
  "to": "user@example.com",
  "subject": "Your subject",
  "html": "<h1>HTML content</h1>",
  "text": "Text content"
}

Response:
{
  "success": true,
  "message": "Email sent successfully",
  "messageId": "message-id-123"
}
```

#### 2. Test Email
```
POST /api/test-email
Content-Type: application/json

Body:
{
  "to": "your-email@example.com"
}

Response:
{
  "success": true,
  "message": "Test email sent successfully",
  "messageId": "message-id-123"
}
```

#### 3. Health Check
```
GET /
Response:
{
  "status": "OK",
  "message": "Medical B2B Email Service is running",
  "provider": "gmail"
}
```

---

## 🐛 Troubleshooting

### Email not sending?

1. **Check server is running**:
   ```bash
   node server.js
   ```
   Should show: "Server running on http://localhost:3000"

2. **Check .env credentials**:
   ```bash
   # Verify .env file exists
   ls .env
   
   # Test Gmail credentials are correct
   ```

3. **Check Firebase logs**:
   - Open browser DevTools (F12)
   - Console tab
   - Look for [EMAIL] logs

4. **Test endpoint directly**:
   ```bash
   curl -X POST http://localhost:3000/api/test-email \
     -H "Content-Type: application/json" \
     -d '{"to":"your-email@example.com"}'
   ```

### User can't login after approval?

1. **Check approval status**:
   - Open Firebase Console
   - Realtime Database → users → user uid
   - Verify `registrationStatus: "approved"`

2. **Check browser console**:
   - Open DevTools (F12)
   - Look for [AUTH] logs
   - Should show "User is approved"

3. **Verify email matches**:
   - Registration email = Login email (case-sensitive)

### License image not showing in admin dashboard?

1. **Check image was uploaded**:
   - Firebase Console → users → user uid → licenseImageBase64
   - Should not be empty

2. **Check permissions**:
   - Firebase Console → Realtime Database → Rules
   - Verify admin can read all users

---

## 🔐 Security Best Practices

1. **Never commit .env file**:
   ```bash
   # .gitignore should have:
   .env
   node_modules/
   ```

2. **Use environment-specific configs**:
   - Development: Gmail test account
   - Production: SendGrid or Mailgun

3. **Limit Firebase access**:
   - Only admins can approve
   - Users can only read own profile
   - Only authenticated users can register

4. **Email password security**:
   - Use App Passwords, not main password
   - Rotate API keys regularly
   - Never share credentials

---

## 📞 Support

If you encounter issues:

1. Check server logs: `node server.js` output
2. Check browser console: DevTools → Console
3. Check Firebase logs: Firebase Console → Logs
4. Test email endpoint: `curl` the /api/test-email endpoint

---

## ✅ Checklist

- [ ] Node.js installed
- [ ] Dependencies installed: `npm install`
- [ ] .env file created with credentials
- [ ] Backend server runs: `node server.js`
- [ ] Frontend runs: `npx serve -l 8080`
- [ ] Admin account created in Firebase
- [ ] Test supplier registration works
- [ ] Test approval sends email
- [ ] Test approved user can login
- [ ] Firebase Rules configured (if using custom rules)

---

## Version Info

- Created: February 23, 2026
- Firebase SDK: v10.7.0
- Node.js: v14+
- Email Services: Gmail, SendGrid, Mailgun
