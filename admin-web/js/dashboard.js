/**
 * Dashboard bootstrap: admin guard, navigation, stats, supplier verification, medicines, fraud
 * SECURITY: requireApprovedAdmin() must be called first to protect dashboard from unauthorized access
 */
import { requireApprovedAdmin, initLogoutButton } from './auth.js';
import { initApprovals } from './approvals.js';
import { initMedicineApprovals } from './medicines.js';
import { initStats } from './stats.js';
import { initAnalytics } from './analytics.js';
import { initProfilePage } from './profile.js';

console.log('[DASHBOARD] dashboard.js module loaded');

/**
 * Initialize sidebar navigation - switch between sections
 */
function initNavigation() {
  console.log('[DASHBOARD] Initializing navigation');
  const links = document.querySelectorAll('.sidebar-link');
  const sections = document.querySelectorAll('.section');
  
  const show = (sectionId) => {
    console.log(`[DASHBOARD] Switching to section: ${sectionId}`);
    sections.forEach((s) => s.classList.toggle('section-active', s.id === sectionId));
    links.forEach((l) => l.classList.toggle('active', l.getAttribute('data-section') === sectionId));
  };
  
  links.forEach((link) => {
    link.addEventListener('click', () => {
      const id = link.getAttribute('data-section');
      if (id) show(id);
    });
  });
}

/**
 * Display admin information in top bar
 */
function showAdminInfo({ user, profile }) {
  console.log('[DASHBOARD] Displaying admin info:', user?.email);
  const nameEl = document.getElementById('adminName');
  const emailEl = document.getElementById('adminEmail');
  
  if (nameEl) nameEl.textContent = profile?.name || user?.email?.split('@')[0] || 'Admin';
  if (emailEl) emailEl.textContent = user?.email || '';
}

/**
 * Bootstrap dashboard with admin protection
 * SECURITY: This function runs AFTER requireApprovedAdmin() verifies the user
 */
function initDashboardContent(context) {
  console.log('[DASHBOARD] Dashboard access granted. Initializing content.');
  
  try {
    showAdminInfo(context);
    initLogoutButton();
    initNavigation();
    initStats();
    initAnalytics();
    initProfilePage(context.user, context.profile);
    initApprovals();
    initMedicineApprovals();
    console.log('[DASHBOARD] All components initialized successfully');
  } catch (err) {
    console.error('[DASHBOARD] Error initializing dashboard content:', err);
    alert('Failed to load dashboard. Please refresh the page.');
  }
}

/**
 * Main initialization routine
 * SECURITY: MUST require admin approval before loading any dashboard content
 */
function initDashboard() {
  console.log('[DASHBOARD] Starting dashboard initialization');
  
  // Developer convenience: if ?dev=1 is present in URL, skip auth and load dashboard for testing
  try {
    const params = new URLSearchParams(window.location.search);
    if (params.get('dev') === '1') {
      console.warn('[DASHBOARD] Dev mode active - bypassing auth (dev=1)');
      const fakeContext = {
        user: { email: 'dev@local' },
        profile: { name: 'Dev Admin', role: 'admin' },
      };
      initDashboardContent(fakeContext);
      return;
    }
  } catch (e) {
    console.warn('[DASHBOARD] Failed to parse URL params for dev mode', e);
  }

  // This guard redirects to login if:
  // 1. No Firebase user is logged in
  // 2. User is logged in but role !== "admin"
  // Only calls initDashboardContent() if user is verified as admin
  requireApprovedAdmin(initDashboardContent);
}

console.log('[DASHBOARD] Calling initDashboard()');
initDashboard();
