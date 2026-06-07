/**
 * Email Service for sending approval notifications
 * This module handles sending emails via Firebase Cloud Functions or external email service
 * Backend endpoint: /api/send-email or Firebase Cloud Function
 */

/**
 * Send approval email to user
 * @param {Object} options - Email options
 * @param {string} options.to - Recipient email
 * @param {string} options.name - Recipient name
 * @param {boolean} options.isApproved - Whether approved or rejected
 * @param {string} options.type - 'supplier' or 'shopkeeper'
 * @param {string} options.message - Optional custom message
 */
export async function sendApprovalEmail(options) {
  const { to, name, isApproved, type = 'supplier', message = '' } = options;

  if (!to || !name) {
    console.warn('[EMAIL] Missing required parameters: to, name');
    return false;
  }

  try {
    const subject = isApproved
      ? `🎉 Your ${type} Account Approved - Medical B2B`
      : `❌ Registration Update - Medical B2B`;

    const htmlContent = isApproved
      ? `
        <!DOCTYPE html>
        <html>
          <head>
            <style>
              body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
              .container { max-width: 600px; margin: 0 auto; padding: 20px; }
              .header { background-color: #0ea5e9; color: white; padding: 20px; border-radius: 5px 5px 0 0; }
              .content { background-color: #f5f5f5; padding: 20px; border-radius: 0 0 5px 5px; }
              .button { display: inline-block; background-color: #0ea5e9; color: white; padding: 12px 24px; text-decoration: none; border-radius: 4px; margin-top: 15px; }
              .footer { font-size: 12px; color: #666; margin-top: 20px; border-top: 1px solid #ddd; padding-top: 15px; }
            </style>
          </head>
          <body>
            <div class="container">
              <div class="header">
                <h1>Welcome, ${name}! 🎉</h1>
              </div>
              <div class="content">
                <p>Great news! Your ${type} account has been successfully approved by our admin team.</p>
                <p>You can now log in and start using our Medical B2B platform to manage your business.</p>
                ${message ? `<p><strong>Note:</strong> ${message}</p>` : ''}
                <a href="${window.location.origin}/login.html" class="button">Log In to Your Account</a>
                <div class="footer">
                  <p>Best regards,<br><strong>Medical B2B Admin Team</strong></p>
                  <p>If you have any questions, please contact our support team.</p>
                </div>
              </div>
            </div>
          </body>
        </html>
      `
      : `
        <!DOCTYPE html>
        <html>
          <head>
            <style>
              body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
              .container { max-width: 600px; margin: 0 auto; padding: 20px; }
              .header { background-color: #f59e0b; color: white; padding: 20px; border-radius: 5px 5px 0 0; }
              .content { background-color: #f5f5f5; padding: 20px; border-radius: 0 0 5px 5px; }
              .footer { font-size: 12px; color: #666; margin-top: 20px; border-top: 1px solid #ddd; padding-top: 15px; }
            </style>
          </head>
          <body>
            <div class="container">
              <div class="header">
                <h1>Registration Update</h1>
              </div>
              <div class="content">
                <p>Dear ${name},</p>
                <p>Thank you for your interest in joining our Medical B2B platform.</p>
                <p>Unfortunately, your ${type} account registration has been reviewed and could not be approved at this time.</p>
                ${message ? `<p><strong>Reason:</strong> ${message}</p>` : ''}
                <p>If you have any questions or would like to reapply, please contact our support team.</p>
                <div class="footer">
                  <p>Best regards,<br><strong>Medical B2B Admin Team</strong></p>
                </div>
              </div>
            </div>
          </body>
        </html>
      `;

    const emailPayload = {
      to,
      subject,
      html: htmlContent,
      text: isApproved
        ? `Welcome to Medical B2B! Your ${type} account has been approved. Log in at ${window.location.origin}/login.html`
        : `Your ${type} registration could not be approved. Please contact support for details.`,
    };

    // Try to send via backend API
    try {
      // Try localhost first (development)
      const response = await fetch('http://localhost:3000/api/send-email', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(emailPayload),
      });

      if (response.ok) {
        console.log('[EMAIL] Email sent successfully via local backend');
        return true;
      } else {
        console.warn('[EMAIL] Local backend returned error status:', response.status);
        throw new Error('Local backend error');
      }
    } catch (localError) {
      console.warn('[EMAIL] Local backend failed, attempting production API', localError);

      // Fallback: Try production API endpoint
      try {
        const response = await fetch('/api/send-email', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(emailPayload),
        });

        if (response.ok) {
          console.log('[EMAIL] Email sent successfully via production API');
          return true;
        } else {
          console.warn('[EMAIL] Production API returned error status:', response.status);
          throw new Error('Production API error');
        }
      } catch (prodError) {
        console.warn('[EMAIL] Production API also failed', prodError);

        // Final fallback: Try Firebase Cloud Function
        try {
          const response = await fetch('/.netlify/functions/send-email', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(emailPayload),
          });

          if (response.ok) {
            console.log('[EMAIL] Email sent via Cloud Function');
            return true;
          }
        } catch (cfError) {
          console.warn('[EMAIL] Cloud Function also failed', cfError);
        }

        // If all fail, the email service is not available but approval should still work
        console.log('[EMAIL] Email service unavailable, but approval recorded in database');
        return false;
      }
    }
  } catch (error) {
    console.error('[EMAIL] Error preparing email:', error);
    return false;
  }
}

/**
 * Send welcome email to newly registered user
 */
export async function sendWelcomeEmail(email, name, type) {
  return sendApprovalEmail({
    to: email,
    name,
    isApproved: true,
    type,
    message: 'Welcome to our platform! You will receive an approval email once your account is verified.',
  });
}

/**
 * Send rejection email
 */
export async function sendRejectionEmail(email, name, type, reason) {
  return sendApprovalEmail({
    to: email,
    name,
    isApproved: false,
    type,
    message: reason || 'Please ensure all required documents are properly submitted.',
  });
}
