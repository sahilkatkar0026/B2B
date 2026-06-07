/**
 * Real-time dashboard stats and fraud monitoring
 * Uses onValue() for users, suppliers (medicines), reorders.
 */
import { db } from './firebase-config.js';
import { ref, onValue } from 'https://www.gstatic.com/firebasejs/10.7.0/firebase-database.js';

const LOW_STOCK_THRESHOLD = 5;
const FRAUD_REORDER_THRESHOLD = 5;
const FRAUD_DAYS_MS = 7 * 24 * 60 * 60 * 1000;

function setEl(id, value) {
  const el = document.getElementById(id);
  if (el) el.textContent = String(value);
}

function parseTs(v) {
  if (v == null) return NaN;
  if (typeof v === 'number') return v;
  const n = Number(v);
  if (!Number.isNaN(n) && n > 0) return n;
  const d = Date.parse(v);
  return Number.isNaN(d) ? NaN : d;
}

function computeStats(usersSnap, suppliersSnap, pendingSnap) {
  let totalSuppliers = 0;
  let approvedSuppliers = 0;
  let totalMedicines = 0;
  let pendingMedicines = 0;
  let expiredMedicines = 0;
  let lowStockMedicines = 0;
  const now = Date.now();

  if (usersSnap && usersSnap.exists()) {
    usersSnap.forEach((c) => {
      const u = c.val();
      if (!u) return;
      if (u.role === 'supplier') {
        totalSuppliers += 1;
        if (u.registrationStatus === 'approved') approvedSuppliers += 1;
      }
    });
  }

  if (suppliersSnap && suppliersSnap.exists()) {
    suppliersSnap.forEach((supSnap) => {
      const meds = supSnap.val().medicines || {};
      Object.values(meds).forEach((med) => {
        totalMedicines += 1;
        const expiryTs = parseTs(med.expiryDate);
        if (!Number.isNaN(expiryTs) && expiryTs < now) expiredMedicines += 1;
        if (typeof med.stock === 'number' && med.stock <= LOW_STOCK_THRESHOLD) lowStockMedicines += 1;
      });
    });
  }

  // Pending medicines are tracked in supplierMedicinesPending
  if (pendingSnap && pendingSnap.exists()) {
    pendingSnap.forEach((c) => {
      const m = c.val();
      if (!m) return;
      if (m.approvalStatus !== 'approved') pendingMedicines += 1;
    });
  }

  setEl('statTotalSuppliers', totalSuppliers);
  setEl('statApprovedSuppliers', approvedSuppliers);
  setEl('statTotalMedicines', totalMedicines);
  setEl('statPendingMedicines', pendingMedicines);
  setEl('statExpiredMedicines', expiredMedicines);
  setEl('statLowStockMedicines', lowStockMedicines);
}

function computeFraud(reordersSnap) {
  const tbody = document.getElementById('fraudTableBody');
  if (!tbody) return;
  tbody.innerHTML = '';
  if (!reordersSnap || !reordersSnap.exists()) {
    tbody.innerHTML = '<tr><td colspan="4">No reorder data.</td></tr>';
    return;
  }
  const now = Date.now();
  const cutoff = now - FRAUD_DAYS_MS;
  const countByKey = {};
  reordersSnap.forEach((c) => {
    const r = c.val();
    if (!r || !r.medicineId || !r.supplierId) return;
    const ts = parseTs(r.createdAt);
    if (Number.isNaN(ts) || ts < cutoff) return;
    const key = `${r.supplierId}|${r.medicineId}`;
    countByKey[key] = (countByKey[key] || 0) + 1;
  });

  const rows = [];
  Object.entries(countByKey).forEach(([key, count]) => {
    if (count <= FRAUD_REORDER_THRESHOLD) return;
    const [supplierId, medicineId] = key.split('|');
    const tr = document.createElement('tr');
    tr.classList.add('row-fraud');
    tr.innerHTML = `
      <td>${medicineId}</td>
      <td>${supplierId}</td>
      <td>${count}</td>
      <td><span class="badge badge--rejected">High frequency</span></td>
    `;
    rows.push(tr);
  });
  if (rows.length === 0) {
    tbody.innerHTML = '<tr><td colspan="4">No high reorder frequency in last 7 days.</td></tr>';
  } else {
    rows.forEach((r) => tbody.appendChild(r));
  }
}

let lastUsersSnap = null;
let lastSuppliersSnap = null;
let lastPendingMedicinesSnap = null;

export function initStats() {
  onValue(ref(db, 'users'), (snap) => {
    lastUsersSnap = snap;
    if (lastSuppliersSnap && lastPendingMedicinesSnap) {
      computeStats(lastUsersSnap, lastSuppliersSnap, lastPendingMedicinesSnap);
    }
  });
  onValue(ref(db, 'suppliers'), (snap) => {
    lastSuppliersSnap = snap;
    if (lastUsersSnap && lastPendingMedicinesSnap) {
      computeStats(lastUsersSnap, lastSuppliersSnap, lastPendingMedicinesSnap);
    }
  });

  onValue(ref(db, 'supplierMedicinesPending'), (snap) => {
    lastPendingMedicinesSnap = snap;
    if (lastUsersSnap && lastSuppliersSnap) {
      computeStats(lastUsersSnap, lastSuppliersSnap, lastPendingMedicinesSnap);
    }
  });

  onValue(ref(db, 'reorders'), computeFraud, (err) => {
    console.error('Reorders error:', err);
    const tbody = document.getElementById('fraudTableBody');
    if (tbody) tbody.innerHTML = '<tr><td colspan="4">Error loading reorders.</td></tr>';
  });
}
