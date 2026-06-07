// Supplier & Shopkeeper approvals management
// - Lists pending supplier/shopkeeper registrations
// - Allows admin to approve or reject accounts

import { db } from './firebase-config.js';
import { sendApprovalEmail } from './email-service.js';
import {
  ref,
  get,
  update,
  query,
  orderByChild,
  equalTo,
  onValue,
} from 'https://www.gstatic.com/firebasejs/10.7.0/firebase-database.js';

/**
 * Build an <img> src string for a base64 license value.
 * Handles both raw base64 data and already-prefixed data URIs.
 */
function buildLicenseSrc(licenseImageBase64) {
  if (!licenseImageBase64) return '';
  if (licenseImageBase64.startsWith('data:')) {
    return licenseImageBase64;
  }
  return `data:image/png;base64,${licenseImageBase64}`;
}

/**
 * Map registration status to badge CSS class.
 */
function statusToBadgeClass(status) {
  switch (status) {
    case 'approved':
      return 'badge badge--approved';
    case 'rejected':
      return 'badge badge--rejected';
    default:
      return 'badge badge--pending';
  }
}

/**
 * Render a single supplier row.
 */
function renderSupplierRow(uid, data) {
  const tr = document.createElement('tr');
  tr.setAttribute('data-uid', uid);
  tr.setAttribute('data-role', 'supplier');
  const statusClass = statusToBadgeClass(data.registrationStatus);

  const status = data.registrationStatus || 'pending';
  tr.innerHTML = `
    <td>${data.name || '-'}</td>
    <td>${data.email || '-'}</td>
    <td>${
    data.licenseImageBase64
      ? `<button type="button" class="btn btn-small btn-ghost" data-action="preview-license" data-uid="${uid}">Preview</button>`
      : '<span class="badge badge--pending">No image</span>'
  }</td>
    <td><span class="${statusClass}">${status}</span></td>
    <td>
      <div class="cell-actions">
        <button class="btn btn-small btn-primary" data-action="approve" data-uid="${uid}" ${status !== 'pending' ? 'disabled' : ''}>Approve</button>
        <button class="btn btn-small btn-ghost" data-action="reject" data-uid="${uid}" ${status !== 'pending' ? 'disabled' : ''}>Reject</button>
      </div>
    </td>
  `;

  return tr;
}

/**
 * Render a single shopkeeper row.
 */
function renderShopkeeperRow(uid, data) {
  const tr = document.createElement('tr');
  tr.setAttribute('data-uid', uid);
  tr.setAttribute('data-role', 'shopkeeper');
  const statusClass = statusToBadgeClass(data.registrationStatus);

  const status = data.registrationStatus || 'pending';
  tr.innerHTML = `
    <td>${data.name || '-'}</td>
    <td>${data.email || '-'}</td>
    <td>${data.shopName || '-'}</td>
    <td>${
    data.licenseImageBase64
      ? `<button type="button" class="btn btn-small btn-ghost" data-action="preview-license" data-uid="${uid}">Preview</button>`
      : '<span class="badge badge--pending">No image</span>'
  }</td>
    <td><span class="${statusClass}">${status}</span></td>
    <td>
      <div class="cell-actions">
        <button class="btn btn-small btn-primary" data-action="approve" data-uid="${uid}" ${status !== 'pending' ? 'disabled' : ''}>Approve</button>
        <button class="btn btn-small btn-ghost" data-action="reject" data-uid="${uid}" ${status !== 'pending' ? 'disabled' : ''}>Reject</button>
      </div>
    </td>
  `;

  return tr;
}

/**
 * Send email notification for approval/rejection.
 */
async function notifyUserOfApproval(email, name, isApproved, userRole) {
  try {
    await sendApprovalEmail({
      to: email,
      name,
      isApproved,
      type: userRole,
      message: isApproved
        ? 'You can now log in and start using our platform.'
        : 'If you have any questions, please contact our support team.',
    });
    console.log(`Approval notification sent to ${email}`);
  } catch (error) {
    console.warn('Could not send email notification:', error);
    // Email sending can fail without blocking approval
  }
}

/**
 * Update user approval state. On approve, also ensure suppliers/shopkeepers/{uid} exists.
 */
async function setUserApproval(uid, shouldApprove, userData, userRole) {
  const roleKey = userRole === 'shopkeeper' ? 'shopkeepers' : 'suppliers';
  
  const updates = {
    [`users/${uid}/registrationStatus`]: shouldApprove ? 'approved' : 'rejected',
  };
  
  if (shouldApprove && userData) {
    updates[`${roleKey}/${uid}/name`] = userData.name || '';
    updates[`${roleKey}/${uid}/email`] = userData.email || '';
    if (userRole === 'shopkeeper') {
      updates[`${roleKey}/${uid}/shopName`] = userData.shopName || '';
    }
    updates[`${roleKey}/${uid}/isActive`] = true;
  }
  
  await update(ref(db), updates);
  
  // Send approval/rejection email
  if (userData && userData.email) {
    await notifyUserOfApproval(userData.email, userData.name, shouldApprove, userRole);
  }
}

/**
 * Load all suppliers (role = 'supplier') and render. Real-time via onValue.
 */
function loadSupplierApprovals(snapshot) {
  const tbody = document.getElementById('supplierApprovalsBody');
  if (!tbody) return;
  tbody.innerHTML = '';
  if (!snapshot.exists()) {
    tbody.innerHTML = '<tr><td colspan="5">No suppliers found.</td></tr>';
    return;
  }
  const rows = [];
  snapshot.forEach((child) => {
    const data = child.val();
    if (data && data.role === 'supplier') rows.push(renderSupplierRow(child.key, data));
  });
  if (rows.length === 0) {
    tbody.innerHTML = '<tr><td colspan="5">No suppliers found.</td></tr>';
  } else {
    rows.forEach((row) => tbody.appendChild(row));
  }
}

/**
 * Load all shopkeepers (role = 'shopkeeper') and render. Real-time via onValue.
 */
function loadShopkeeperApprovals(snapshot) {
  const tbody = document.getElementById('shopkeeperApprovalsBody');
  if (!tbody) return;
  tbody.innerHTML = '';
  if (!snapshot.exists()) {
    tbody.innerHTML = '<tr><td colspan="6">No shopkeepers found.</td></tr>';
    return;
  }
  const rows = [];
  snapshot.forEach((child) => {
    const data = child.val();
    if (data && data.role === 'shopkeeper') rows.push(renderShopkeeperRow(child.key, data));
  });
  if (rows.length === 0) {
    tbody.innerHTML = '<tr><td colspan="6">No shopkeepers found.</td></tr>';
  } else {
    rows.forEach((row) => tbody.appendChild(row));
  }
}


/**
 * Attach click handlers for approve/reject and license preview.
 */
function attachApprovalHandlers() {
  const supplierBody = document.getElementById('supplierApprovalsBody');
  const shopkeeperBody = document.getElementById('shopkeeperApprovalsBody');

  const handler = async (event) => {
    const target = event.target.closest('button[data-action][data-uid]');
    if (!target) return;
    const uid = target.getAttribute('data-uid');
    const action = target.getAttribute('data-action');

    if (action === 'preview-license') {
      const snap = await get(ref(db, `users/${uid}`));
      const data = snap.val() || {};
      const src = buildLicenseSrc(data.licenseImageBase64);
      if (src) window.open(src, '_blank', 'width=600,height=500');
      return;
    }

    const approve = action === 'approve';
    target.disabled = true;
    try {
      let userData = null;
      let userRole = 'supplier';
      
      const snap = await get(ref(db, `users/${uid}`));
      userData = snap.val() || {};
      userRole = userData.role || 'supplier';
      
      await setUserApproval(uid, approve, userData, userRole);
    } catch (error) {
      console.error('Error updating approval status:', error);
      target.disabled = false;
      alert('Failed to update approval status. Please try again.');
    }
  };

  if (supplierBody) supplierBody.addEventListener('click', handler);
  if (shopkeeperBody) shopkeeperBody.addEventListener('click', handler);
}

/**
 * Public initializer: real-time supplier & shopkeeper lists + handlers.
 */
export function initApprovals() {
  // Load suppliers
  const supplierQuery = query(ref(db, 'users'), orderByChild('role'), equalTo('supplier'));
  onValue(supplierQuery, loadSupplierApprovals, (err) => {
    console.error('Suppliers error:', err);
    const tbody = document.getElementById('supplierApprovalsBody');
    if (tbody) tbody.innerHTML = '<tr><td colspan="5">Error loading.</td></tr>';
  });

  // Load shopkeepers
  const shopkeeperQuery = query(ref(db, 'users'), orderByChild('role'), equalTo('shopkeeper'));
  onValue(shopkeeperQuery, loadShopkeeperApprovals, (err) => {
    console.error('Shopkeepers error:', err);
    const tbody = document.getElementById('shopkeeperApprovalsBody');
    if (tbody) tbody.innerHTML = '<tr><td colspan="6">Error loading.</td></tr>';
  });

  attachApprovalHandlers();
}

