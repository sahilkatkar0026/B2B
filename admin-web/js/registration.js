/**
 * Registration Logic for Suppliers and Shopkeepers
 * Handles form submission, file upload, and Firebase registration
 */

import { auth, db } from './firebase-config.js';
import {
  createUserWithEmailAndPassword,
  onAuthStateChanged,
  signOut,
} from 'https://www.gstatic.com/firebasejs/10.7.0/firebase-auth.js';
import { ref, set, get } from 'https://www.gstatic.com/firebasejs/10.7.0/firebase-database.js';

/**
 * Convert image file to Base64
 */
function fileToBase64(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = () => resolve(reader.result);
    reader.onerror = (error) => reject(error);
  });
}

/**
 * Preview file when selected
 */
function setupFilePreview(inputId, previewId) {
  const input = document.getElementById(inputId);
  if (!input) return;

  input.addEventListener('change', (e) => {
    const file = e.target.files[0];
    if (!file) return;

    // Validate file type
    if (!file.type.startsWith('image/')) {
      alert('Please select a valid image file');
      input.value = '';
      return;
    }

    // Validate file size (max 5MB)
    if (file.size > 5 * 1024 * 1024) {
      alert('File size must be less than 5MB');
      input.value = '';
      return;
    }

    // Show preview
    const reader = new FileReader();
    reader.onload = (evt) => {
      const preview = document.getElementById(previewId);
      preview.innerHTML = `<img src="${evt.target.result}" alt="License preview">`;
    };
    reader.readAsDataURL(file);
  });
}

/**
 * Handle supplier registration
 */
async function handleSupplierRegistration(event) {
  event.preventDefault();
  const form = event.target;
  const nameInput = form.querySelector('[name="name"]');
  const emailInput = form.querySelector('[name="email"]');
  const companyInput = form.querySelector('[name="companyName"]');
  const passwordInput = form.querySelector('[name="password"]');
  const confirmInput = form.querySelector('[name="confirmPassword"]');
  const licenseInput = form.querySelector('[name="license"]');
  const errorEl = document.getElementById('supplierError');
  const submitBtn = form.querySelector('button[type="submit"]');

  errorEl.textContent = '';

  // Validate inputs
  const name = nameInput?.value?.trim();
  const email = emailInput?.value?.trim();
  const company = companyInput?.value?.trim();
  const password = passwordInput?.value;
  const confirmPassword = confirmInput?.value;
  const licenseFile = licenseInput?.files?.[0];

  if (!name || !email || !company || !password || !confirmPassword || !licenseFile) {
    errorEl.textContent = 'Please fill in all fields including license image.';
    return;
  }

  if (password !== confirmPassword) {
    errorEl.textContent = 'Passwords do not match.';
    return;
  }

  if (password.length < 6) {
    errorEl.textContent = 'Password must be at least 6 characters.';
    return;
  }

  submitBtn.disabled = true;

  try {
    // Convert license to Base64
    const licenseBase64 = await fileToBase64(licenseFile);

    // Create Firebase Auth user
    const cred = await createUserWithEmailAndPassword(auth, email, password);
    const uid = cred.user.uid;

    // Register in database
    await set(ref(db, `users/${uid}`), {
      uid,
      name,
      email,
      role: 'supplier',
      companyName: company,
      registrationStatus: 'pending',
      licenseImageBase64: licenseBase64,
      createdAt: Date.now(),
    });

    // Show success message
    form.style.display = 'none';
    document.getElementById('successMessage').style.display = 'block';

    // Sign out after successful registration
    await signOut(auth);

    // Clear form
    form.reset();
  } catch (error) {
    let msg = 'Registration failed. Please try again.';
    
    if (error.code === 'auth/email-already-in-use') {
      msg = 'This email is already registered.';
    } else if (error.code === 'auth/weak-password') {
      msg = 'Password is too weak. Use at least 6 characters.';
    } else if (error.code === 'auth/invalid-email') {
      msg = 'Invalid email address.';
    }
    
    errorEl.textContent = msg;
    console.error('Supplier registration error:', error);
  } finally {
    submitBtn.disabled = false;
  }
}

/**
 * Handle shopkeeper registration
 */
async function handleShopkeeperRegistration(event) {
  event.preventDefault();
  const form = event.target;
  const nameInput = form.querySelector('[name="name"]');
  const emailInput = form.querySelector('[name="email"]');
  const shopNameInput = form.querySelector('[name="shopName"]');
  const passwordInput = form.querySelector('[name="password"]');
  const confirmInput = form.querySelector('[name="confirmPassword"]');
  const licenseInput = form.querySelector('[name="license"]');
  const errorEl = document.getElementById('shopkeeperError');
  const submitBtn = form.querySelector('button[type="submit"]');

  errorEl.textContent = '';

  // Validate inputs
  const name = nameInput?.value?.trim();
  const email = emailInput?.value?.trim();
  const shopName = shopNameInput?.value?.trim();
  const password = passwordInput?.value;
  const confirmPassword = confirmInput?.value;
  const licenseFile = licenseInput?.files?.[0];

  if (!name || !email || !shopName || !password || !confirmPassword || !licenseFile) {
    errorEl.textContent = 'Please fill in all fields including license image.';
    return;
  }

  if (password !== confirmPassword) {
    errorEl.textContent = 'Passwords do not match.';
    return;
  }

  if (password.length < 6) {
    errorEl.textContent = 'Password must be at least 6 characters.';
    return;
  }

  submitBtn.disabled = true;

  try {
    // Convert license to Base64
    const licenseBase64 = await fileToBase64(licenseFile);

    // Create Firebase Auth user
    const cred = await createUserWithEmailAndPassword(auth, email, password);
    const uid = cred.user.uid;

    // Register in database
    await set(ref(db, `users/${uid}`), {
      uid,
      name,
      email,
      shopName,
      role: 'shopkeeper',
      registrationStatus: 'pending',
      licenseImageBase64: licenseBase64,
      createdAt: Date.now(),
    });

    // Show success message
    form.style.display = 'none';
    document.getElementById('successMessage').style.display = 'block';

    // Sign out after successful registration
    await signOut(auth);

    // Clear form
    form.reset();
  } catch (error) {
    let msg = 'Registration failed. Please try again.';
    
    if (error.code === 'auth/email-already-in-use') {
      msg = 'This email is already registered.';
    } else if (error.code === 'auth/weak-password') {
      msg = 'Password is too weak. Use at least 6 characters.';
    } else if (error.code === 'auth/invalid-email') {
      msg = 'Invalid email address.';
    }
    
    errorEl.textContent = msg;
    console.error('Shopkeeper registration error:', error);
  } finally {
    submitBtn.disabled = false;
  }
}

/**
 * Handle login from registration page
 */
async function handleLoginFromRegistration(event) {
  event.preventDefault();
  const form = event.target;
  const emailInput = form.querySelector('[name="email"]');
  const passwordInput = form.querySelector('[name="password"]');
  
  const email = emailInput?.value?.trim();
  const password = passwordInput?.value;
  
  if (!email || !password) {
    document.getElementById('loginError').textContent = 'Please enter email and password.';
    return;
  }
  
  // Redirect to main login/auth handler
  window.location.href = 'index.html';
}

/**
 * Initialize tab switching
 */
function initTabs() {
  const tabButtons = document.querySelectorAll('.register-tab-btn');
  
  tabButtons.forEach((btn) => {
    btn.addEventListener('click', () => {
      // Remove active class from all buttons
      tabButtons.forEach((b) => b.classList.remove('active'));
      
      // Add active class to clicked button
      btn.classList.add('active');
      
      // Hide all forms
      document.querySelectorAll('.register-form').forEach((form) => {
        form.classList.remove('active');
      });
      
      // Show selected form
      const tabName = btn.getAttribute('data-tab');
      const form = document.getElementById(tabName + 'Form');
      if (form) {
        form.classList.add('active');
      }
    });
  });
}

/**
 * Initialize registration page
 */
function initRegistration() {
  // Initialize tabs
  initTabs();
  
  // Setup file previews
  setupFilePreview('supplierLicense', 'supplierLicensePreview');
  setupFilePreview('shopkeeperLicense', 'shopkeeperLicensePreview');
  
  // Attach form handlers
  const supplierForm = document.getElementById('supplierForm');
  const shopkeeperForm = document.getElementById('shopkeeperForm');
  const loginForm = document.getElementById('loginForm');
  
  if (supplierForm) {
    supplierForm.addEventListener('submit', handleSupplierRegistration);
  }
  
  if (shopkeeperForm) {
    shopkeeperForm.addEventListener('submit', handleShopkeeperRegistration);
  }
  
  if (loginForm) {
    loginForm.addEventListener('submit', handleLoginFromRegistration);
  }
  
  // Check if already logged in - redirect to dashboard
  onAuthStateChanged(auth, async (user) => {
    if (!user) return;
    
    console.log('User already logged in, redirecting...');
    const profile = await (async () => {
      const snap = await get(ref(db, `users/${user.uid}`));
      return snap.val() || {};
    })();
    
    if (profile.role === 'admin' || profile.registrationStatus === 'approved') {
      window.location.href = 'dashboard.html';
    }
  });
}

console.log('[REGISTRATION] registration.js loaded');
initRegistration();
