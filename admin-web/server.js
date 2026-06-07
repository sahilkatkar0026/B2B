/**
 * Medical B2B Admin Panel - Email Backend Server
 * Handles email notifications for approval/rejection
 * 
 * Setup:
 * 1. npm install express cors dotenv nodemailer
 * 2. Create .env file with EMAIL credentials
 * 3. node server.js
 */

const express = require('express');
const cors = require('cors');
require('dotenv').config();
const nodemailer = require('nodemailer');

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(cors());
app.use(express.json());
app.use(express.static('.')); // Serve static files

// Configure mail transporter
let transporter;

// Check which email service is configured
if (process.env.EMAIL_PROVIDER === 'gmail') {
  transporter = nodemailer.createTransport({
    service: 'gmail',
    auth: {
      user: process.env.GMAIL_USER,
      pass: process.env.GMAIL_PASSWORD, // Use app-specific password
    },
  });
} else if (process.env.EMAIL_PROVIDER === 'sendgrid') {
  const sgTransport = require('nodemailer-sendgrid-transport');
  transporter = nodemailer.createTransport(
    sgTransport({
      auth: {
        api_key: process.env.SENDGRID_API_KEY,
      },
    })
  );
} else if (process.env.EMAIL_PROVIDER === 'mailgun') {
  transporter = nodemailer.createTransport({
    host: 'smtp.mailgun.org',
    port: 587,
    auth: {
      user: process.env.MAILGUN_USER,
      pass: process.env.MAILGUN_PASSWORD,
    },
  });
} else {
  // Default to console logging (development)
  console.warn('⚠️  No email provider configured. Using console logging instead.');
  transporter = {
    sendMail: async (mailOptions) => {
      console.log('📧 Email would be sent:', mailOptions);
      return { messageId: 'console-' + Date.now() };
    },
  };
}

/**
 * Health check endpoint
 */
app.get('/', (req, res) => {
  res.json({
    status: 'OK',
    message: 'Medical B2B Email Service is running',
    provider: process.env.EMAIL_PROVIDER || 'mock',
  });
});

/**
 * Send email endpoint
 * POST /api/send-email
 * Body: { to, subject, html, text }
 */
app.post('/api/send-email', async (req, res) => {
  try {
    const { to, subject, html, text } = req.body;

    // Validate required fields
    if (!to || !subject) {
      return res.status(400).json({
        error: 'Missing required fields: to, subject',
      });
    }

    if (!html && !text) {
      return res.status(400).json({
        error: 'Either html or text content must be provided',
      });
    }

    const mailOptions = {
      from: process.env.EMAIL_FROM || process.env.GMAIL_USER || 'noreply@medicalbdev.com',
      to,
      subject,
      html: html || undefined,
      text: text || undefined,
    };

    // Send email
    const info = await transporter.sendMail(mailOptions);

    console.log(`✅ Email sent to ${to}`);
    console.log(`   Message ID: ${info.messageId || info.response}`);

    res.json({
      success: true,
      message: 'Email sent successfully',
      messageId: info.messageId || 'mock-' + Date.now(),
    });
  } catch (error) {
    console.error('❌ Email sending error:', error.message);
    res.status(500).json({
      error: 'Failed to send email',
      message: error.message,
    });
  }
});

/**
 * Test endpoint - sends a test email
 * POST /api/test-email
 * Body: { to }
 */
app.post('/api/test-email', async (req, res) => {
  try {
    const { to } = req.body;

    if (!to) {
      return res.status(400).json({ error: 'Email address required' });
    }

    const testEmail = {
      from: process.env.EMAIL_FROM || process.env.GMAIL_USER || 'noreply@medicalbdev.com',
      to,
      subject: '✅ Medical B2B Email Service - Test Email',
      html: `
        <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
          <div style="background: #0ea5e9; color: white; padding: 20px; border-radius: 5px 5px 0 0;">
            <h1>Test Email Success! 🎉</h1>
          </div>
          <div style="background: #f5f5f5; padding: 20px; border-radius: 0 0 5px 5px;">
            <p>This is a test email from your Medical B2B Admin Panel email service.</p>
            <p><strong>Email Provider:</strong> ${process.env.EMAIL_PROVIDER || 'Mock'}</p>
            <p><strong>Sent At:</strong> ${new Date().toLocaleString()}</p>
            <p>Your email service is working correctly!</p>
          </div>
        </div>
      `,
      text: 'This is a test email. Your email service is working!',
    };

    const info = await transporter.sendMail(testEmail);

    console.log(`✅ Test email sent to ${to}`);

    res.json({
      success: true,
      message: 'Test email sent successfully',
      messageId: info.messageId,
    });
  } catch (error) {
    console.error('❌ Test email error:', error.message);
    res.status(500).json({
      error: 'Failed to send test email',
      message: error.message,
    });
  }
});

// Error handling middleware
app.use((err, req, res, next) => {
  console.error('Server error:', err);
  res.status(500).json({
    error: 'Internal server error',
    message: err.message,
  });
});

// Start server
app.listen(PORT, () => {
  console.log(`
╔════════════════════════════════════════════════════════════╗
║   Medical B2B Admin Panel - Email Service                  ║
║   Server running on http://localhost:${PORT}                ║
║   Email Provider: ${process.env.EMAIL_PROVIDER || 'MOCK (Development)'}
║                                                            ║
║   Endpoints:                                               ║
║   POST /api/send-email    - Send approval/rejection email  ║
║   POST /api/test-email    - Send test email                ║
║   GET  /                  - Health check                   ║
╚════════════════════════════════════════════════════════════╝
  `);
});
