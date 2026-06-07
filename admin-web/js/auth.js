/**
 * Admin authentication – Firebase Auth + Realtime Database role check
 * Only users with users/{uid}/role === "admin" can access the dashboard.
 * Security: All redirects require Firebase Auth verification + Admin role check
 */
import { auth, db } from './firebase-config.js';
import {
  signInWithEmailAndPassword,
  signOut,
  onAuthStateChanged,
} from 'https://www.gstatic.com/firebasejs/10.7.0/firebase-auth.js';
import { ref, get } from 'https://www.gstatic.com/firebasejs/10.7.0/firebase-database.js';

function redirectTo(path) {
  console.log(`[AUTH] Redirecting to: ${path}`);
  window.location.href = path;
}

/** Fetch users/{uid} profile from Realtime Database */
async function fetchUserProfile(uid) {
  if (!uid) {
    console.warn('[AUTH] No UID provided to fetchUserProfile');
    return null;
  }
  try {
    const snap = await get(ref(db, `users/${uid}`));
    if (!snap.exists()) {
      console.warn(`[AUTH] No profile found for UID: ${uid}`);
      return null;
    }
    const profile = snap.val();
    console.log(`[AUTH] Profile fetched for ${uid}:`, profile);
    return profile;
  } catch (err) {
    console.error('[AUTH] Error fetching profile:', err);
    return null;
  }
}

/**
 * Enforce admin-only access globally.
 * Used on dashboard: redirects to login if not authenticated or not admin.
 */
export function requireApprovedAdmin(onReady) {
  console.log('[AUTH] Running requireApprovedAdmin guard');
  
  onAuthStateChanged(auth, async (user) => {
    if (!user) {
      console.warn('[AUTH] No Firebase user. Redirecting to login.');
      redirectTo('index.html');
      return;
    }

    console.log(`[AUTH] Firebase user authenticated: ${user.email} (uid: ${user.uid})`);

    try {
      const profile = await fetchUserProfile(user.uid);
      const isAdmin = profile && profile.role === 'admin';

      if (!isAdmin) {
        console.error(`[AUTH] User role is not admin. Role: ${profile?.role || 'undefined'}. Signing out.`);
        await signOut(auth);
        redirectTo('index.html');
        return;
      }

      console.log('[AUTH] User is admin. Access granted.');
      if (typeof onReady === 'function') {
        onReady({ user, profile });
      }
    } catch (err) {
      console.error('[AUTH] Admin check error:', err);
      await signOut(auth);
      redirectTo('index.html');
    }
  });
}

/**
 * Initialize logout button functionality
 */
export function initLogoutButton() {
  const btn = document.getElementById('logoutBtn');
  if (!btn) {
    console.warn('[AUTH] Logout button not found');
    return;
  }

  btn.addEventListener('click', async () => {
    console.log('[AUTH] Logout button clicked');
    try {
      await signOut(auth);
      console.log('[AUTH] User signed out successfully');
      redirectTo('index.html');
    } catch (err) {
      console.error('[AUTH] Sign out error:', err);
      alert('Logout failed. Please try again.');
    }
  });
}

/**
 * Initialize login page form handling
 * Security: Validates email/password, calls Firebase Auth, checks admin role
 */
function initLoginPage() {
  const form = document.getElementById('loginForm');
  const errorEl = document.getElementById('loginError');

  if (!form) {
    console.warn('[AUTH] Login form not found on page');
    return;
  }

  console.log('[AUTH] Initializing login page');

  // Redirect if already logged in as admin
  onAuthStateChanged(auth, async (user) => {
    if (!user) {
      console.log('[AUTH] No user logged in');
      return;
    }

    console.log(`[AUTH] User already logged in: ${user.email}`);
    const profile = await fetchUserProfile(user.uid);
    
    if (profile && profile.role === 'admin') {
      console.log('[AUTH] User is admin. Redirecting to dashboard.');
      redirectTo('dashboard.html');
      return;
    }

    // Check if supplier or shopkeeper is approved
    if ((profile?.role === 'supplier' || profile?.role === 'shopkeeper') && profile?.registrationStatus === 'approved') {
      console.log('[AUTH] User is approved ' + profile.role + '. Redirecting to dashboard.');
      redirectTo('dashboard.html');
      return;
    }

    console.warn('[AUTH] Logged-in user is not admin or not approved. Signing out.');
    await signOut(auth);
  });

  // Handle form submission
  form.addEventListener('submit', async (e) => {
    e.preventDefault(); // CRITICAL: Prevent default form submission
    console.log('[AUTH] Login form submitted');

    // Clear previous errors
    if (errorEl) errorEl.textContent = '';

    // Get form values
    const email = form.email?.value?.trim();
    const password = form.password?.value;

    // Validate input
    if (!email || !password) {
      const msg = 'Please enter both email and password.';
      console.warn(`[AUTH] Validation failed: ${msg}`);
      if (errorEl) errorEl.textContent = msg;
      return;
    }

    // Validate email format
    if (!email.includes('@') || !email.includes('.')) {
      const msg = 'Please enter a valid email address.';
      console.warn(`[AUTH] Invalid email format: ${email}`);
      if (errorEl) errorEl.textContent = msg;
      return;
    }

    // Disable button during login
    const submitBtn = form.querySelector('button[type="submit"]');
    if (submitBtn) submitBtn.disabled = true;

    try {
      console.log(`[AUTH] Attempting Firebase login with email: ${email}`);

      // Attempt Firebase authentication
      const cred = await signInWithEmailAndPassword(auth, email, password);
      console.log(`[AUTH] Firebase auth successful for: ${cred.user.email}`);

      // Fetch user profile to check role and approval status
      const profile = await fetchUserProfile(cred.user.uid);

      // Verify user is admin
      if (profile && profile.role === 'admin') {
        console.log('[AUTH] Admin verified. Redirecting to dashboard.');
        redirectTo('dashboard.html');
        return;
      }

      // Check if supplier or shopkeeper is approved
      if ((profile?.role === 'supplier' || profile?.role === 'shopkeeper') && profile?.registrationStatus === 'approved') {
        console.log('[AUTH] ' + profile.role + ' approved. Redirecting to dashboard.');
        redirectTo('dashboard.html');
        return;
      }

      // User is not admin and either not supplier/shopkeeper or not approved
      console.error(`[AUTH] User not authorized. Role: ${profile?.role || 'undefined'}, Status: ${profile?.registrationStatus || 'undefined'}`);
      
      await signOut(auth);
      let msg = 'Access denied. ';
      if (profile?.role === 'supplier' || profile?.role === 'shopkeeper') {
        msg += 'Your account is pending approval. Please wait for admin verification.';
      } else {
        msg += 'Admin account required.';
      }
      if (errorEl) errorEl.textContent = msg;
      
      if (submitBtn) submitBtn.disabled = false;
      return;

    } catch (err) {
      console.error('[AUTH] Login error:', err.code, err.message);

      let msg = 'Login failed. Please try again.';

      // Provide specific error messages
      if (err.code === 'auth/user-not-found') {
        msg = 'No account found with this email address.';
      } else if (err.code === 'auth/wrong-password') {
        msg = 'Incorrect password.';
      } else if (err.code === 'auth/invalid-email') {
        msg = 'Invalid email address.';
      } else if (err.code === 'auth/user-disabled') {
        msg = 'This account has been disabled.';
      } else if (err.code === 'auth/too-many-requests') {
        msg = 'Too many login attempts. Please try again later.';
      }

      if (errorEl) errorEl.textContent = msg;
      if (submitBtn) submitBtn.disabled = false;
    }
  });
}

console.log('[AUTH] auth.js module loaded');
initLoginPage();

