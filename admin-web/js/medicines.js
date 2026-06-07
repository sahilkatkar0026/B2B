/**
 * Medicine Approval System (according to current Realtime DB)
 * - Reads from supplierMedicinesPending/
 * - Shows name, supplier, price, mfgDate, expiryDate, stock, status
 * - Allows admin to Approve/Reject pending medicines
 * - On approve, copies the medicine into suppliers/{supplierId}/medicines/{medicineId}
 */
import { db } from './firebase-config.js';
import {
  ref,
  get,
  update,
  onValue,
} from 'https://www.gstatic.com/firebasejs/10.7.0/firebase-database.js';

const LOW_STOCK_THRESHOLD = 5;
const MEDICINES_TBODY = 'medicinesTableBody';

function parseTs(v) {
  if (v == null) return NaN;
  if (typeof v === 'number') return v;
  const n = Number(v);
  if (!Number.isNaN(n) && n > 0) return n;
  const d = Date.parse(v);
  return Number.isNaN(d) ? NaN : d;
}

/**
 * Render medicines from a plain map of medicineId -> medicine
 */
function renderMedicinesFromMap(map, suppliersMap) {
  const tbody = document.getElementById(MEDICINES_TBODY);
  if (!tbody) return;
  tbody.innerHTML = '';
  console.debug('[MEDICINES] renderMedicinesFromMap: items=', Object.keys(map || {}).length);

  const keys = Object.keys(map || {});
  if (keys.length === 0) {
    tbody.innerHTML = '<tr><td colspan="9">No medicines found.</td></tr>';
    return;
  }

  const rows = [];
  keys.forEach((medicineId) => {
    const med = map[medicineId] || {};
    const supplierId = med.supplierId;
    const supplierName = (supplierId && suppliersMap[supplierId]) || supplierId || '-';
    rows.push(renderMedRow(medicineId, med, supplierName));
  });

  rows.forEach((r) => tbody.appendChild(r));
}

function formatDate(ts) {
  const n = parseTs(ts);
  return Number.isNaN(n) ? '-' : new Date(n).toLocaleDateString();
}

function getStatus(med) {
  const now = Date.now();
  const expiryTs = parseTs(med.expiryDate);
  const isExpired = !Number.isNaN(expiryTs) && expiryTs < now;
  const lowStock =
    typeof med.stock === 'number' && med.stock <= LOW_STOCK_THRESHOLD;

  const rawStatus = med.approvalStatus || 'pending';
  let label = 'Pending';
  let badgeClass = 'badge badge--pending';

  if (rawStatus === 'approved') {
    label = 'Approved';
    badgeClass = 'badge badge--approved';
  } else if (rawStatus === 'rejected') {
    label = 'Rejected';
    badgeClass = 'badge badge--rejected';
  }

  if (isExpired) {
    label = 'Expired';
    badgeClass = 'badge badge--expired';
  }

  return {
    isExpired,
    lowStock,
    rawStatus,
    label,
    badgeClass,
    canApproveOrReject: !isExpired && rawStatus === 'pending',
  };
}

function escapeHtml(s) {
  const el = document.createElement('div');
  el.textContent = s;
  return el.innerHTML;
}

function escapeAttr(s) {
  return String(s).replace(/"/g, '&quot;').replace(/</g, '&lt;');
}

function renderMedRow(medicineId, med, supplierName) {
  const status = getStatus(med);
  const tr = document.createElement('tr');
  if (status.isExpired) tr.classList.add('row-expired');
  else if (status.lowStock) tr.classList.add('row-low-stock');

  const actionDisabledAttr = status.canApproveOrReject ? '' : 'disabled';

  tr.innerHTML = `
    <td>${escapeHtml(med.name || '-')}</td>
    <td>${escapeHtml(med.brand || '-')}</td>
    <td>${escapeHtml(supplierName || med.supplierId || '-')}</td>
    <td>${
      typeof med.price === 'number' || typeof med.priceNullable === 'number'
        ? (med.priceNullable ?? med.price).toFixed(2)
        : '-'
    }</td>
    <td>${
      med.stockNullable != null
        ? med.stockNullable
        : med.stock != null
          ? med.stock
          : '-'
    }</td>
    <td>${formatDate(med.mfgDate)}</td>
    <td>${formatDate(med.expiryDate)}</td>
    <td><span class="${status.badgeClass}">${status.label}</span></td>
    <td>
      <div class="cell-actions">
        <button
          class="btn btn-small btn-primary"
          data-action="approve"
          data-mid="${escapeAttr(medicineId)}"
          ${actionDisabledAttr}
        >
          Approve
        </button>
        <button
          class="btn btn-small btn-ghost"
          data-action="reject"
          data-mid="${escapeAttr(medicineId)}"
          ${actionDisabledAttr}
        >
          Reject
        </button>
      </div>
    </td>
  `;
  return tr;
}

function renderMedicinesTable(pendingSnap, suppliersMap) {
  const tbody = document.getElementById(MEDICINES_TBODY);
  if (!tbody) return;
  tbody.innerHTML = '';

  if (!pendingSnap || !pendingSnap.exists()) {
    tbody.innerHTML = '<tr><td colspan="9">No medicines found.</td></tr>';
    return;
  }

  const rows = [];
  pendingSnap.forEach((childSnap) => {
    const medicineId = childSnap.key;
    const med = childSnap.val() || {};
    const supplierId = med.supplierId;
    const supplierName =
      (supplierId && suppliersMap[supplierId]) || supplierId || '-';
    rows.push(renderMedRow(medicineId, med, supplierName));
  });

  if (rows.length === 0) {
    tbody.innerHTML = '<tr><td colspan="9">No medicines found.</td></tr>';
    return;
  }

  rows.forEach((r) => tbody.appendChild(r));
}

async function approveMedicine(medicineId) {
  const medSnap = await get(ref(db, `supplierMedicinesPending/${medicineId}`));
  if (!medSnap.exists()) {
    throw new Error('Medicine not found');
  }
  const med = medSnap.val() || {};
  const now = Date.now();

  // Keep medicine in pending but mark as approved
  const updates = {
    [`supplierMedicinesPending/${medicineId}/approvalStatus`]: 'approved',
    [`supplierMedicinesPending/${medicineId}/approvedAt`]: now,
    [`supplierMedicinesPending/${medicineId}/rejectedReason`]: '',
  };

  const supplierId = med.supplierId;
  if (supplierId) {
    const isExpired = (() => {
      const expiryTs = parseTs(med.expiryDate);
      return !Number.isNaN(expiryTs) && expiryTs < now;
    })();

    const approvedMedicine = {
      name: med.name || '',
      brand: med.brand || '',
      price:
        typeof med.priceNullable === 'number'
          ? med.priceNullable
          : med.price ?? 0,
      stock:
        typeof med.stockNullable === 'number'
          ? med.stockNullable
          : med.stock ?? 0,
      mfgDate: med.mfgDate || null,
      expiryDate: med.expiryDate || null,
      imageBase64: med.imageBase64 || '',
      approvalStatus: 'approved',
      approvedAt: now,
      isApproved: true,
      isExpired,
      unit: med.unit || '',
      totalSold: med.totalSold ?? 0,
      lastSoldAt: med.lastSoldAt ?? 0,
      medicineId: medicineId,
      supplierId: supplierId,
      medicineKey: med.medicineKey || '',
    };

    // Update in suppliers medicines
    updates[`suppliers/${supplierId}/medicines/${medicineId}`] = approvedMedicine;

    // Also update in shops if this medicine is linked to any shop
    // Find all shops that have this medicine pending
    const shopsSnap = await get(ref(db, 'shops'));
    if (shopsSnap.exists()) {
      shopsSnap.forEach((shopChild) => {
        const shopId = shopChild.key;
        const shopData = shopChild.val() || {};
        if (shopData.medicines && shopData.medicines[medicineId]) {
          updates[`shops/${shopId}/medicines/${medicineId}/approvalStatus`] = 'approved';
          updates[`shops/${shopId}/medicines/${medicineId}/approvedAt`] = now;
        }
      });
    }
  }

  await update(ref(db), updates);
}

async function rejectMedicine(medicineId) {
  const now = Date.now();
  const reason =
    window.prompt('Optional: enter a reason for rejection', '') || '';

  const updates = {
    [`supplierMedicinesPending/${medicineId}/approvalStatus`]: 'rejected',
    [`supplierMedicinesPending/${medicineId}/approvedAt`]: 0,
    [`supplierMedicinesPending/${medicineId}/rejectedReason`]: reason,
  };

  await update(ref(db), updates);
}

function attachHandlers() {
  const tbody = document.getElementById(MEDICINES_TBODY);
  if (!tbody) return;

  tbody.addEventListener('click', async (e) => {
    const btn = e.target.closest('button[data-action][data-mid]');
    if (!btn) return;
    const mid = btn.getAttribute('data-mid');
    const action = btn.getAttribute('data-action');
    if (!mid || !action) return;

    btn.disabled = true;
     const originalText = btn.textContent;
     btn.textContent = action === 'approve' ? 'Approving...' : 'Rejecting...';
     
     try {
       if (action === 'approve') {
         await approveMedicine(mid);
         showNotification('✅ Medicine approved successfully!', 'success');
       } else if (action === 'reject') {
         await rejectMedicine(mid);
         showNotification('❌ Medicine rejected successfully!', 'info');
       }
     } catch (err) {
       console.error('Medicine action failed:', err);
       showNotification(`❌ ${err.message || 'Action failed. Please try again.'}`, 'error');
       btn.disabled = false;
       btn.textContent = originalText;
     }
  });
}

let suppliersCache = {};
let pendingMedicinesMap = {}; // key: medicineId -> med object

function subscribeSuppliers() {
  onValue(
    ref(db, 'suppliers'),
    (snap) => {
      const map = {};
      if (snap && snap.exists()) {
        snap.forEach((child) => {
          const data = child.val() || {};
          map[child.key] = data.name || child.key;
        });
      }
      suppliersCache = map;
      console.debug('[MEDICINES] suppliersCache updated, suppliersCount=', Object.keys(map).length);
      // Also update pending medicines map from suppliers' medicines
      // iterate suppliers and collect medicines with approvalStatus 'pending'
      pendingMedicinesMap = pendingMedicinesMap || {};
      if (snap && snap.exists()) {
        snap.forEach((supplierChild) => {
          const supplierId = supplierChild.key;
          const supplierData = supplierChild.val() || {};
          const meds = supplierData.medicines || {};
          Object.keys(meds).forEach((mid) => {
            const m = meds[mid] || {};
            if ((m.approvalStatus || 'pending') === 'pending') {
              // ensure supplierId/medicineId present
              m.supplierId = m.supplierId || supplierId;
              pendingMedicinesMap[mid] = m;
            }
          });
        });
      }
      // render merged view
      renderMedicinesFromMap(pendingMedicinesMap, suppliersCache);
      console.debug('[MEDICINES] after suppliers scan pendingMedicinesMap size=', Object.keys(pendingMedicinesMap).length);
    },
    (err) => {
      console.error('Suppliers listener error:', err);
    },
  );
}

export function initMedicineApprovals() {
  subscribeSuppliers();
  // Listen to standalone pending medicines node
  onValue(
    ref(db, 'supplierMedicinesPending'),
    (snap) => {
      const pendingCount = snap && snap.exists() ? snap.size : 0;
      console.debug('[MEDICINES] supplierMedicinesPending snapshot received, count=', pendingCount);

      // Merge into pendingMedicinesMap without wiping supplier-sourced entries.
      // Start from any meds already collected from `suppliers/*/medicines`
      const merged = Object.assign({}, pendingMedicinesMap || {});

      if (snap && snap.exists()) {
        snap.forEach((child) => {
          const mid = child.key;
          const m = child.val() || {};
          // `supplierMedicinesPending` should override supplier-sourced record when present
          merged[mid] = m;
        });
      }

      pendingMedicinesMap = merged;
      renderMedicinesFromMap(pendingMedicinesMap, suppliersCache);
      console.debug('[MEDICINES] after pending node merge pendingMedicinesMap size=', Object.keys(pendingMedicinesMap).length);
    },
    (err) => {
      console.error('Medicines list error:', err);
      const tbody = document.getElementById(MEDICINES_TBODY);
      if (tbody) {
        tbody.innerHTML = '<tr><td colspan="9">Error loading data.</td></tr>';
      }
    },
  );

  attachHandlers();
}


function showNotification(message, type = 'info') {
  const notification = document.createElement('div');
  notification.className = `notification notification--${type}`;
  notification.textContent = message;
  notification.style.cssText = `
    position: fixed;
    top: 20px;
    right: 20px;
    padding: 15px 20px;
    border-radius: 5px;
    background: ${type === 'success' ? '#10b981' : type === 'error' ? '#ef4444' : '#3b82f6'};
    color: white;
    z-index: 9999;
    box-shadow: 0 4px 6px rgba(0,0,0,0.1);
    animation: slideIn 0.3s ease;
    font-weight: 500;
  `;
  
  document.body.appendChild(notification);
  
  setTimeout(() => {
    notification.style.animation = 'slideOut 0.3s ease';
    setTimeout(() => notification.remove(), 300);
  }, 3000);
}

// Add CSS animations
if (!document.getElementById('medicineNotificationStyles')) {
  const style = document.createElement('style');
  style.id = 'medicineNotificationStyles';
  style.textContent = `
    @keyframes slideIn {
      from { transform: translateX(400px); opacity: 0; }
      to { transform: translateX(0); opacity: 1; }
    }
    @keyframes slideOut {
      from { transform: translateX(0); opacity: 1; }
      to { transform: translateX(400px); opacity: 0; }
    }
  `;
  document.head.appendChild(style);
}

