/**
 * Admin Profile Management
 * Handles profile information display and password change
 */
import { auth, db } from './firebase-config.js';
import {
  updateProfile,
  updatePassword,
  reauthenticateWithCredential,
  EmailAuthProvider,
} from 'https://www.gstatic.com/firebasejs/10.7.0/firebase-auth.js';
import { ref, update, get } from 'https://www.gstatic.com/firebasejs/10.7.0/firebase-database.js';

console.log('[PROFILE] Module loaded');

/**
 * Load and display admin profile information
 */
export function initProfilePage(user, profile) {
  console.log('[PROFILE] Initializing profile page for:', user.email);

  // Display profile information
  displayProfileInfo(user, profile);

  // Initialize profile form
  initProfileForm(user, profile);

  // Initialize password change modal
  initPasswordModal(user);
}

/**
 * Display user profile information in the UI
 */
function displayProfileInfo(user, profile) {
  const nameEl = document.getElementById('profileName');
  const emailEl = document.getElementById('profileEmail');
  const initialEl = document.getElementById('profileInitial');
  const fullNameInput = document.getElementById('fullNameInput');
  const emailInput = document.getElementById('emailInput');

  const adminName = profile?.name || user?.displayName || user?.email?.split('@')[0] || 'Admin';
  const initial = adminName.charAt(0).toUpperCase();

  if (nameEl) nameEl.textContent = adminName;
  if (emailEl) emailEl.textContent = user?.email || '';
  if (initialEl) initialEl.textContent = initial;
  if (fullNameInput) fullNameInput.value = adminName;
  if (emailInput) emailInput.value = user?.email || '';

  console.log('[PROFILE] Profile displayed:', adminName);
}

/**
 * Initialize profile form for saving changes
 */
function initProfileForm(user, profile) {
  const saveBtn = document.getElementById('saveProfileBtn');
  if (!saveBtn) return;

  saveBtn.addEventListener('click', async () => {
    const fullNameInput = document.getElementById('fullNameInput');
    const newName = fullNameInput?.value?.trim();

    if (!newName) {
      alert('Please enter a full name');
      return;
    }

    saveBtn.disabled = true;
    saveBtn.textContent = 'Saving...';

    try {
      // Update Firebase Auth display name
      await updateProfile(user, { displayName: newName });

      // Update Realtime Database
      await update(ref(db, `users/${user.uid}`), { name: newName });

      console.log('[PROFILE] Profile updated successfully');
      alert('Profile updated successfully!');
    } catch (err) {
      console.error('[PROFILE] Error updating profile:', err);
      alert('Failed to update profile. Please try again.');
    } finally {
      saveBtn.disabled = false;
      saveBtn.textContent = 'Save Changes';
    }
  });
}

/**
 * Initialize password change modal and functionality
 */
function initPasswordModal(user) {
  const changePasswordBtn = document.getElementById('changePasswordBtn');
  const passwordModal = document.getElementById('passwordModal');
  const modalOverlay = document.getElementById('modalOverlay');
  const modalCloseBtn = document.getElementById('modalCloseBtn');
  const modalCancelBtn = document.getElementById('modalCancelBtn');
  const submitPasswordBtn = document.getElementById('submitPasswordBtn');

  if (!changePasswordBtn || !passwordModal) {
    console.warn('[PROFILE] Password modal elements not found');
    return;
  }

  // Open modal
  changePasswordBtn.addEventListener('click', () => {
    console.log('[PROFILE] Opening password change modal');
    clearPasswordForm();
    passwordModal.classList.remove('hidden');
  });

  // Close modal handlers
  const closeModal = () => {
    passwordModal.classList.add('hidden');
    clearPasswordForm();
  };

  modalCloseBtn?.addEventListener('click', closeModal);
  modalCancelBtn?.addEventListener('click', closeModal);
  modalOverlay?.addEventListener('click', closeModal);

  // Submit password change
  submitPasswordBtn?.addEventListener('click', () => {
    handlePasswordChange(user);
  });

  // Allow Enter key to submit
  document.addEventListener('keypress', (e) => {
    if (e.key === 'Enter' && !passwordModal.classList.contains('hidden')) {
      handlePasswordChange(user);
    }
  });
}

/**
 * Handle password change submission
 */
async function handlePasswordChange(user) {
  const currentPassword = document.getElementById('currentPassword')?.value;
  const newPassword = document.getElementById('newPassword')?.value;
  const confirmPassword = document.getElementById('confirmPassword')?.value;
  const passwordError = document.getElementById('passwordError');

  // Clear previous error
  if (passwordError) passwordError.textContent = '';

  // Validation
  if (!currentPassword || !newPassword || !confirmPassword) {
    setError('All fields are required');
    return;
  }

  if (newPassword.length < 6) {
    setError('New password must be at least 6 characters');
    return;
  }

  if (newPassword !== confirmPassword) {
    setError('New passwords do not match');
    return;
  }

  if (currentPassword === newPassword) {
    setError('New password must be different from current password');
    return;
  }

  const submitBtn = document.getElementById('submitPasswordBtn');
  submitBtn.disabled = true;
  submitBtn.textContent = 'Updating...';

  try {
    console.log('[PROFILE] Attempting to change password');

    // Re-authenticate user
    const credential = EmailAuthProvider.credential(user.email, currentPassword);
    await reauthenticateWithCredential(user, credential);
    console.log('[PROFILE] User re-authenticated');

    // Update password
    await updatePassword(user, newPassword);
    console.log('[PROFILE] Password changed successfully');

    // Close modal and show success
    document.getElementById('passwordModal').classList.add('hidden');
    clearPasswordForm();
    alert('Password changed successfully!');
  } catch (err) {
    console.error('[PROFILE] Password change error:', err.code, err.message);

    let msg = 'Failed to change password';
    if (err.code === 'auth/wrong-password') {
      msg = 'Current password is incorrect';
    } else if (err.code === 'auth/weak-password') {
      msg = 'New password is too weak. Use at least 6 characters';
    } else if (err.code === 'auth/requires-recent-login') {
      msg = 'Please sign out and sign in again for security reasons';
    }

    setError(msg);
  } finally {
    submitBtn.disabled = false;
    submitBtn.textContent = 'Update Password';
  }

  function setError(msg) {
    if (passwordError) passwordError.textContent = msg;
    console.warn('[PROFILE] Validation error:', msg);
  }
}

/**
 * Clear password form fields
 */
function clearPasswordForm() {
  const currentPassword = document.getElementById('currentPassword');
  const newPassword = document.getElementById('newPassword');
  const confirmPassword = document.getElementById('confirmPassword');
  const passwordError = document.getElementById('passwordError');

  if (currentPassword) currentPassword.value = '';
  if (newPassword) newPassword.value = '';
  if (confirmPassword) confirmPassword.value = '';
  if (passwordError) passwordError.textContent = '';
}
