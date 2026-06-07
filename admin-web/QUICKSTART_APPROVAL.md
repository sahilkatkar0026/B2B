# 🚀 Quick Start Guide - Medical B2B Admin Panel

## What You Have Now

A complete **approval workflow system** where:
- Users register with license
- Admin approves/rejects
- Email is sent automatically
- User can only login after approval

---

## ⚡ 3-Step Quick Start

### Step 1: Install & Configure (5 minutes)
```bash
cd d:\B2BAdmin
setup.bat
```

Then edit `.env` file:
```env
EMAIL_PROVIDER=gmail
GMAIL_USER=your-email@gmail.com
GMAIL_PASSWORD=your-app-password
```

### Step 2: Run Backend (Terminal 1)
```bash
cd d:\B2BAdmin
node server.js
```

Should show: ✅ Server running on http://localhost:3000

### Step 3: Run Frontend (Terminal 2)
```bash
cd d:\B2BAdmin
npx serve -l 8080
```

Should show: ✅ Accepting connections at http://localhost:8080

---

## 🧪 Test It (2 minutes)

1. **Open http://localhost:8080/register.html**
2. **Click "Supplier Registration"**
3. **Fill form:**
   - Name: John Test
   - Email: john@test.com
   - Company: Test Co
   - Password: test123456
   - License: Upload any image
4. **Click Register**
5. **See success message**

---

## 👨‍💼 Admin Approves

1. **Open http://localhost:8080/index.html**
2. **Login as admin** (create one in Firebase)
3. **Dashboard → Supplier Verification**
4. **Click Approve**
5. **Email sent to john@test.com! 📧**

---

## ✅ User Logs In

1. **john@test.com receives approval email**
2. **john@test.com tries login at http://localhost:8080/index.html**
3. **✓ Login works!**
4. **✓ Redirected to dashboard!**

---

## 📧 Email Provider Setup

Choose one (5-10 mins each):

### Option 1: Gmail (Recommended)
1. https://myaccount.google.com/apppasswords
2. Generate app password
3. Put in .env

### Option 2: SendGrid
1. https://sendgrid.com (Sign up)
2. Create API key
3. Put in .env

### Option 3: Mailgun
1. https://mailgun.com (Sign up)
2. Get SMTP credentials
3. Put in .env

---

## 📁 Key Files

| File | Purpose |
|------|---------|
| `server.js` | Backend email service |
| `.env` | Email credentials (CREATE THIS) |
| `register.html` | User registration |
| `dashboard.html` | Admin approval interface |
| `index.html` | Login page |

---

## 🐛 If Something Goes Wrong

### Email not sending?
```bash
# Check server is running
node server.js

# Test endpoint
curl -X POST http://localhost:3000/api/test-email \
  -H "Content-Type: application/json" \
  -d '{"to":"your-email@example.com"}'
```

### Can't login after approval?
- Check Firebase Console → users → registrationStatus field
- Should be "approved" not "pending"

### Port 3000 or 8080 already in use?
```bash
# Change port in server.js
# Or kill existing process
```

---

## 📚 Full Documentation

- **APPROVAL_WORKFLOW_SETUP.md** - Complete setup guide
- **IMPLEMENTATION_COMPLETE.md** - Full feature overview
- **QUICK_START.md** - System overview
- **EMAIL_SERVICE_SETUP.md** - Email provider details

---

## ✨ You're Ready!

1. Run setup.bat ✓
2. Edit .env ✓
3. Start backend server ✓
4. Start frontend server ✓
5. Test registration ✓
6. Test approval ✓
7. Test login ✓

**Everything is configured and ready to use!**

---

**Questions?** Check the documentation files or browse the code comments. Everything is well-documented!
