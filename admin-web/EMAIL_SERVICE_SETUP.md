# Email Service Setup Guide

## Overview
The Medical B2B Admin Panel includes automated email notifications for approval/rejection of supplier and shopkeeper accounts. This guide explains how to set up email sending.

## Option 1: Firebase Cloud Functions (Recommended)

### Setup Steps:

1. **Install Firebase CLI**
   ```bash
   npm install -g firebase-tools
   ```

2. **Initialize Cloud Functions in your project**
   ```bash
   firebase init functions
   ```

3. **Install email library**
   ```bash
   cd functions
   npm install nodemailer
   ```

4. **Create the email function** - Replace `functions/index.js` with:

```javascript
const functions = require("firebase-functions");
const nodemailer = require("nodemailer");
const cors = require("cors")({ origin: true });

// Configure your email service
// Using Gmail: https://support.google.com/accounts/answer/185833
const transporter = nodemailer.createTransport({
  service: "gmail",
  auth: {
    user: process.env.GMAIL_USER,
    pass: process.env.GMAIL_PASSWORD,
  },
});

// Alternative: Using Mailgun
// const transporter = nodemailer.createTransport({
//   host: "smtp.mailgun.org",
//   port: 587,
//   auth: {
//     user: process.env.MAILGUN_USER,
//     pass: process.env.MAILGUN_PASSWORD,
//   },
// });

exports.sendEmail = functions.https.onRequest((req, res) => {
  cors(req, res, async () => {
    if (req.method !== "POST") {
      return res.status(405).send("Method Not Allowed");
    }

    const { to, subject, html, text } = req.body;

    if (!to || !subject) {
      return res.status(400).send("Missing email or subject");
    }

    try {
      await transporter.sendMail({
        from: process.env.GMAIL_USER || "noreply@medicalbdev.com",
        to,
        subject,
        html,
        text,
      });

      res.json({ success: true, message: "Email sent successfully" });
    } catch (error) {
      console.error("Email error:", error);
      res.status(500).json({ error: error.message });
    }
  });
});
```

5. **Configure environment variables**
   ```bash
   firebase functions:config:set gmail.user="your-email@gmail.com" gmail.password="app-password"
   ```

6. **Deploy**
   ```bash
   firebase deploy --only functions
   ```

## Option 2: SendGrid Integration

1. **Get SendGrid API Key** - Sign up at https://sendgrid.com

2. **Install SendGrid**
   ```bash
   npm install @sendgrid/mail
   ```

3. **Update functions/index.js**:
```javascript
const functions = require("firebase-functions");
const sgMail = require("@sendgrid/mail");
const cors = require("cors")({ origin: true });

sgMail.setApiKey(process.env.SENDGRID_API_KEY);

exports.sendEmail = functions.https.onRequest((req, res) => {
  cors(req, res, async () => {
    const { to, subject, html, text } = req.body;

    try {
      await sgMail.send({
        to,
        from: "noreply@medicalbdev.com",
        subject,
        html,
        text,
      });

      res.json({ success: true });
    } catch (error) {
      console.error(error);
      res.status(500).json({ error: error.message });
    }
  });
});
```

4. **Set API key**
   ```bash
   firebase functions:config:set sendgrid.key="your-api-key"
   ```

## Option 3: Netlify Functions (if using Netlify)

Create `netlify/functions/send-email.js`:

```javascript
const nodemailer = require("nodemailer");

const transporter = nodemailer.createTransport({
  service: "gmail",
  auth: {
    user: process.env.GMAIL_USER,
    pass: process.env.GMAIL_PASSWORD,
  },
});

exports.handler = async (event) => {
  if (event.httpMethod !== "POST") {
    return { statusCode: 405, body: "Method Not Allowed" };
  }

  const { to, subject, html, text } = JSON.parse(event.body);

  try {
    await transporter.sendMail({
      from: process.env.GMAIL_USER,
      to,
      subject,
      html,
      text,
    });

    return {
      statusCode: 200,
      body: JSON.stringify({ success: true }),
    };
  } catch (error) {
    return {
      statusCode: 500,
      body: JSON.stringify({ error: error.message }),
    };
  }
};
```

## Testing Email Functionality

1. **Test endpoint locally**
   ```bash
   curl -X POST http://localhost:5001/your-project/us-central1/sendEmail \
     -H "Content-Type: application/json" \
     -d '{
       "to": "test@example.com",
       "subject": "Test Email",
       "html": "<h1>Hello</h1>",
       "text": "Hello"
     }'
   ```

2. **Approve a supplier/shopkeeper** and check email inbox

## Email Service Selection

| Service | Pros | Cons |
|---------|------|------|
| **Firebase Cloud Functions** | Free tier, integrated with Firebase | Cold start delays |
| **SendGrid** | Reliable, good deliverability | Paid after free tier |
| **Mailgun** | Good API, developer-friendly | Requires credit card verification |
| **Gmail** | Simple setup | May be blocked by ISPs, slow |

## Security Best Practices

1. **Never hardcode credentials** - Use environment variables
2. **Use App Passwords** - For Gmail, use 16-character app password, not your main password
3. **Rate limiting** - Implement rate limiting to prevent email spam
4. **SSL/TLS** - Ensure encrypted transmission
5. **Validation** - Validate email format before sending

## Database Structure for Users

Users should have this structure in Firebase Realtime Database:

```json
{
  "users": {
    "uid1": {
      "name": "John Supplier",
      "email": "john@supplier.com",
      "role": "supplier",
      "registrationStatus": "pending",
      "licenseImageBase64": "...",
      "createdAt": 1644567890
    },
    "uid2": {
      "name": "Jane Shopkeeper",
      "email": "jane@shop.com",
      "shopName": "Jane's Medical Shop",
      "role": "shopkeeper",
      "registrationStatus": "pending",
      "licenseImageBase64": "...",
      "createdAt": 1644567890
    }
  }
}
```

## Integration with Frontend

The frontend (`js/email-service.js`) automatically attempts to send emails when:
- Admin approves a supplier or shopkeeper
- Admin rejects a supplier or shopkeeper

If the email service is unavailable, the approval still proceeds but no email is sent.
