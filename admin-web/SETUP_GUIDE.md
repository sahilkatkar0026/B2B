# Medical B2B Admin Panel - Complete Setup Guide

**Project Date:** February 25, 2026  
**Version:** 1.0  
**Status:** Production Ready

---

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Project Setup](#project-setup)
3. [Environment Configuration](#environment-configuration)
4. [Firebase Configuration](#firebase-configuration)
5. [Deploy Firebase Rules](#deploy-firebase-rules)
6. [Start Backend Server](#start-backend-server)
7. [Start Frontend Server](#start-frontend-server)
8. [Access Application](#access-application)
9. [Deploy to Production](#deploy-to-production)
10. [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Install Node.js
1. Download from: https://nodejs.org/
2. Install LTS (Long Term Support) version
3. Verify installation:
   ```
   node --version
   npm --version
   ```

### Install Firebase CLI
```bash
npm install -g firebase-tools
```

### System Requirements
- Windows 10 or later (or macOS/Linux)
- 2GB RAM minimum
- 500MB disk space
- Internet connection

---

## Project Setup

### Step 1: Navigate to Project Folder
```bash
cd d:\B2BAdmin
```

### Step 2: Install Dependencies
```bash
npm install
```

This installs all required packages:
- express (Web server)
- cors (Cross-Origin support)
- dotenv (Environment variables)
- nodemailer (Email service)
- firebase (Firebase SDK)

### Step 3: Install Cloud Functions Dependencies
```bash
cd functions
npm install
cd ..
```

---

## Environment Configuration

### Create .env File

Create a new file named `.env` in `d:\B2BAdmin\`:

#### Option 1: Using Gmail (Production)
```
PORT=3000
EMAIL_PROVIDER=gmail
GMAIL_USER=your-email@gmail.com
GMAIL_PASSWORD=your-app-specific-password
```

#### Option 2: Using Mock Email (Testing/Development)
```
PORT=3000
EMAIL_PROVIDER=mock
```

#### Option 3: Using SendGrid
```
PORT=3000
EMAIL_PROVIDER=sendgrid
SENDGRID_API_KEY=your-sendgrid-api-key
```

---

## Firebase Configuration

### Option A: New Firebase Project (First Time)

```bash
firebase login
firebase init
```

Choose options:
- Select "Realtime Database"
- Select "Functions"
- Select "Hosting"

### Option B: Existing Firebase Project

```bash
firebase use --add
```

Then select your project from the list.

### Verify Configuration
```bash
firebase projects:list
firebase use
```

---

## Deploy Firebase Rules

### Step 1: Review Rules
Open `database.rules.json` and verify all rules are correct.

### Step 2: Deploy Rules
```bash
firebase deploy --only database:rules
```

### Expected Output
```
=== Deploying to 'your-project-id'...

i  deploying database
✔  database rules deployed successfully

Deploy complete!
```

---

## Start Backend Server

### Terminal 1 - Backend Server

```bash
cd d:\B2BAdmin
node server.js
```

### Expected Output
```
╔════════════════════════════════════════════════════════════╗
║   Medical B2B Admin Panel - Email Service                  ║
║   Server running on http://localhost:3000                  ║
║   Email Provider: MOCK (Development)                       ║
║                                                            ║
║   Endpoints:                                               ║
║   POST /api/send-email    - Send approval/rejection email  ║
║   POST /api/test-email    - Send test email                ║
║   GET  /                  - Health check                   ║
╚════════════════════════════════════════════════════════════╝
```

### Verify Backend is Running
Open another terminal and run:
```bash
curl http://localhost:3000
```

---

## Start Frontend Server

### Terminal 2 - Frontend Server

Keep Terminal 1 running and open a new terminal:

```bash
cd d:\B2BAdmin
npx http-server . -p 8080
```

### Expected Output
```
Starting up http-server, serving .
Hit CTRL-C to stop the server

http://localhost:8080
http://127.0.0.1:8080
```

### Verify Frontend is Running
Open another terminal and run:
```bash
curl http://localhost:8080
```

---

## Access Application

### Main URLs

| Page | URL |
|------|-----|
| Login | http://localhost:8080/login.html |
| Dashboard | http://localhost:8080/dashboard.html |
| Registration | http://localhost:8080/register.html |
| Home | http://localhost:8080/index.html |

### First Time Login

1. Go to: http://localhost:8080/register.html
2. Create an admin account
3. Verify email (if email service enabled)
4. Go to: http://localhost:8080/login.html
5. Sign in

---

## Deploy to Production

### Firebase Hosting Deployment

```bash
firebase deploy
```

This deploys:
- ✓ Database rules
- ✓ Cloud functions
- ✓ Static hosting

### Deploy Only Database Rules
```bash
firebase deploy --only database:rules
```

### Deploy Only Functions
```bash
firebase deploy --only functions
```

### View Deployment Status
```bash
firebase deploy --only database:rules --debug
```

---

## Complete Manual Commands Checklist

### First Time Setup
- [ ] Install Node.js
- [ ] Install Firebase CLI: `npm install -g firebase-tools`
- [ ] Navigate to project: `cd d:\B2BAdmin`
- [ ] Install dependencies: `npm install`
- [ ] Install functions: `cd functions && npm install && cd ..`
- [ ] Create `.env` file
- [ ] Login to Firebase: `firebase login`
- [ ] Initialize Firebase: `firebase init`
- [ ] Deploy rules: `firebase deploy --only database:rules`

### Every Startup
- [ ] Terminal 1: `cd d:\B2BAdmin && node server.js`
- [ ] Terminal 2: `cd d:\B2BAdmin && npx http-server . -p 8080`
- [ ] Open browser: http://localhost:8080/login.html

### Stopping
- [ ] Terminal 1: `CTRL + C`
- [ ] Terminal 2: `CTRL + C`

---

## Troubleshooting

### Port Already in Use

**For Backend (Port 3000):**
```bash
netstat -ano | findstr :3000
taskkill /PID <PID_NUMBER> /F
```

**For Frontend (Port 8080):**
```bash
netstat -ano | findstr :8080
taskkill /PID <PID_NUMBER> /F
```

**Or use different ports:**
```bash
PORT=3001 node server.js
npx http-server . -p 8081
```

### Firebase Permissions Denied

1. Clear browser cache: `CTRL + SHIFT + Delete`
2. Sign out from app
3. Sign back in
4. Check `.env` file is configured
5. Verify Firebase rules deployed: `firebase deploy --only database:rules`

### npm Packages Not Installing

```bash
# Clear npm cache
npm cache clean --force

# Remove node_modules
rmdir /s /q node_modules

# Reinstall
npm install
```

### Firebase Login Issues

```bash
firebase logout
firebase login
firebase use --add
```

### Check Service Status

```bash
# Backend health check
curl http://localhost:3000

# Frontend health check
curl http://localhost:8080

# List Firebase projects
firebase projects:list

# View current Firebase project
firebase use
```

---

## Testing

### Test Backend Email Endpoint
```bash
curl -X POST http://localhost:3000/api/test-email ^
  -H "Content-Type: application/json" ^
  -d "{\"to\":\"test@example.com\",\"subject\":\"Test\",\"text\":\"Test email\"}"
```

### Test Firebase Connection
Open browser DevTools (F12) and run:
```javascript
// Check Firebase initialization
console.log(firebase.auth().currentUser);
console.log(firebase.database().ref());
```

---

## Performance Optimization

### Enable Gzip Compression
```bash
npx http-server . -p 8080 -g
```

### Monitor Server Resources
```bash
# View Node.js process
tasklist | findstr node

# View memory usage
wmic process where name="node.exe" get processid,workingsetsize
```

---

## Security Checklist

- [ ] `.env` file contains sensitive data (add to `.gitignore`)
- [ ] Firebase rules deployed to production
- [ ] Admin user created and verified
- [ ] HTTPS enabled in production
- [ ] Email provider configured
- [ ] Database backups enabled
- [ ] Audit logs enabled

---

## Project Structure

```
d:\B2BAdmin\
├── server.js                      (Backend API - Port 3000)
├── .env                           (Environment variables)
├── firebase.json                  (Firebase config)
├── database.rules.json            (Firebase security rules)
├── package.json                   (Dependencies)
│
├── index.html                     (Home page)
├── login.html                     (Login page)
├── dashboard.html                 (Admin dashboard)
├── register.html                  (Registration page)
│
├── css/
│   └── style.css                  (Styling)
│
├── js/
│   ├── auth.js                    (Authentication)
│   ├── firebase-config.js         (Firebase setup)
│   ├── medicines.js               (Medicine management)
│   ├── dashboard.js               (Dashboard logic)
│   ├── analytics.js               (Analytics)
│   ├── approvals.js               (Approval workflow)
│   ├── email-service.js           (Email integration)
│   ├── medicines.js               (Medicine operations)
│   ├── profile.js                 (User profile)
│   ├── registration.js            (Registration logic)
│   └── stats.js                   (Statistics)
│
├── functions/
│   ├── index.js                   (Cloud functions)
│   └── package.json               (Functions dependencies)
│
└── docs/
    ├── SETUP_GUIDE.md             (This file)
    ├── DEPLOYMENT.md              (Deployment guide)
    ├── FIREBASE_RULES_GUIDE.md    (Rules documentation)
    └── EMAIL_SERVICE_SETUP.md     (Email setup guide)
```

---

## Support & Documentation

### Firebase Documentation
- Console: https://console.firebase.google.com/
- Docs: https://firebase.google.com/docs

### Node.js Documentation
- Official: https://nodejs.org/en/docs/
- npm: https://docs.npmjs.com/

### Express.js Documentation
- Official: https://expressjs.com/

### Nodemailer Documentation
- Official: https://nodemailer.com/

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2026-02-25 | Initial setup guide |

---

## Contact & Issues

For issues or questions:
1. Check troubleshooting section above
2. Review Firebase console logs
3. Check browser console (F12)
4. Review server terminal output

---

**End of Setup Guide**

Generated: February 25, 2026
