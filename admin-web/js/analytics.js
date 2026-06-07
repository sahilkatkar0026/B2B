/**
 * Sales Analytics Module
 * Displays sales data, trends, charts, and key performance metrics
 */
import { db } from './firebase-config.js';
import { ref, onValue } from 'https://www.gstatic.com/firebasejs/10.7.0/firebase-database.js';

console.log('[ANALYTICS] Module loaded');

// Chart instances (to update without recreating)
let salesLineChartInstance = null;
let salesPieChartInstance = null;
let topProductsChartInstance = null;
let orderStatusChartInstance = null;

const CHART_COLORS = {
  primary: '#16a34a',
  success: '#22c55e',
  danger: '#dc2626',
  warning: '#ea580c',
  secondary: '#3b82f6',
  cyan: '#0891b2',
};

/**
 * Format number as currency (Indian Rupees)
 */
function formatCurrency(amount) {
  if (typeof amount !== 'number') return '₹0';
  return '₹' + amount.toLocaleString('en-IN', { maximumFractionDigits: 0 });
}

/**
 * Format date as DD/MM/YYYY
 */
function formatDate(timestamp) {
  if (!timestamp) return '-';
  const date = new Date(timestamp);
  return date.toLocaleDateString('en-IN');
}

/**
 * Calculate stats from orders data
 */
function calculateStats(orders) {
  let totalOrders = 0;
  let totalSales = 0;
  let weeklyOrders = 0;
  const now = Date.now();
  const sevenDaysAgo = now - 7 * 24 * 60 * 60 * 1000;
  const thirtyDaysAgo = now - 30 * 24 * 60 * 60 * 1000;

  const salesByDate = {};
  const productsSold = {};
  const ordersByStatus = { pending: 0, completed: 0, cancelled: 0 };

  Object.values(orders).forEach((order) => {
    if (!order || typeof order !== 'object') return;

    totalOrders += 1;

    const orderAmount = parseFloat(order.totalAmount) || 0;
    totalSales += orderAmount;

    const createdAt = order.createdAt || 0;
    const orderDate = new Date(createdAt).toLocaleDateString('en-IN');

    // Track sales by date
    if (createdAt > thirtyDaysAgo) {
      salesByDate[orderDate] = (salesByDate[orderDate] || 0) + orderAmount;
    }

    // Weekly orders
    if (createdAt > sevenDaysAgo) {
      weeklyOrders += 1;
    }

    // Order status
    const status = order.status?.toLowerCase() || 'pending';
    if (ordersByStatus.hasOwnProperty(status)) {
      ordersByStatus[status] += 1;
    } else {
      ordersByStatus[status] = 1;
    }

    // Track products
    if (order.items && Array.isArray(order.items)) {
      order.items.forEach((item) => {
        const productName = item.medicineName || 'Unknown';
        const quantity = parseInt(item.quantity) || 1;
        productsSold[productName] = (productsSold[productName] || 0) + quantity;
      });
    }
  });

  const avgOrderValue = totalOrders > 0 ? totalSales / totalOrders : 0;

  return {
    totalOrders,
    totalSales,
    avgOrderValue,
    weeklyOrders,
    salesByDate,
    productsSold,
    ordersByStatus,
  };
}

/**
 * Update KPI cards with stats
 */
function updateKPICards(stats) {
  const setEl = (id, value) => {
    const el = document.getElementById(id);
    if (el) el.textContent = value;
  };

  setEl('statTotalOrders', stats.totalOrders);
  setEl('statTotalSales', formatCurrency(stats.totalSales));
  setEl('statAvgorderValue', formatCurrency(stats.avgOrderValue));
  setEl('statWeeklyOrders', stats.weeklyOrders);
  setEl('statOrdersTrend', `↑ ${stats.weeklyOrders} orders this week`);
  setEl('statSalesTrend', `↑ ₹${Math.round(stats.totalSales / 4).toLocaleString('en-IN')} weekly avg`);
}

/**
 * Update Sales Line Chart (last 30 days)
 */
function updateSalesLineChart(salesByDate) {
  const ctx = document.getElementById('salesLineChart');
  if (!ctx) return;

  // Sort dates and get last 30
  const sortedDates = Object.keys(salesByDate).sort();
  const last30Dates = sortedDates.slice(-30);
  const last30Sales = last30Dates.map((date) => salesByDate[date]);

  const chartData = {
    labels: last30Dates,
    datasets: [
      {
        label: 'Daily Sales (₹)',
        data: last30Sales,
        borderColor: CHART_COLORS.primary,
        backgroundColor: `${CHART_COLORS.primary}15`,
        borderWidth: 3,
        fill: true,
        tension: 0.4,
        pointBackgroundColor: CHART_COLORS.primary,
        pointBorderColor: '#fff',
        pointBorderWidth: 2,
        pointRadius: 4,
      },
    ],
  };

  if (!salesLineChartInstance) {
    salesLineChartInstance = new Chart(ctx, {
      type: 'line',
      data: chartData,
      options: {
        responsive: true,
        maintainAspectRatio: true,
        plugins: {
          legend: {
            display: true,
            labels: { color: '#1e293b', font: { size: 12 } },
          },
        },
        scales: {
          y: {
            ticks: { color: '#64748b' },
            grid: { color: 'rgba(22, 163, 74, 0.1)' },
          },
          x: {
            ticks: { color: '#64748b' },
            grid: { color: 'rgba(22, 163, 74, 0.05)' },
          },
        },
      },
    });
  } else {
    salesLineChartInstance.data = chartData;
    salesLineChartInstance.update();
  }
}

/**
 * Update Sales Pie Chart (by status)
 */
function updateSalesPieChart(ordersByStatus) {
  const ctx = document.getElementById('salesPieChart');
  if (!ctx) return;

  const labels = Object.keys(ordersByStatus);
  const data = Object.values(ordersByStatus);
  const colors = [CHART_COLORS.success, CHART_COLORS.warning, CHART_COLORS.danger];

  const chartData = {
    labels: labels.map((l) => l.charAt(0).toUpperCase() + l.slice(1)),
    datasets: [
      {
        data: data,
        backgroundColor: colors,
        borderColor: '#ffffff',
        borderWidth: 2,
      },
    ],
  };

  if (!salesPieChartInstance) {
    salesPieChartInstance = new Chart(ctx, {
      type: 'doughnut',
      data: chartData,
      options: {
        responsive: true,
        maintainAspectRatio: true,
        plugins: {
          legend: {
            display: true,
            labels: { color: '#1e293b', font: { size: 12 }, padding: 15 },
            position: 'bottom',
          },
        },
      },
    });
  } else {
    salesPieChartInstance.data = chartData;
    salesPieChartInstance.update();
  }
}

/**
 * Update Top Products Bar Chart
 */
function updateTopProductsChart(productsSold) {
  const ctx = document.getElementById('topProductsChart');
  if (!ctx) return;

  // Get top 10 products
  const topProducts = Object.entries(productsSold)
    .sort((a, b) => b[1] - a[1])
    .slice(0, 10);

  const labels = topProducts.map((p) => p[0]);
  const quantities = topProducts.map((p) => p[1]);

  const chartData = {
    labels: labels,
    datasets: [
      {
        label: 'Units Sold',
        data: quantities,
        backgroundColor: CHART_COLORS.secondary,
        borderColor: CHART_COLORS.primary,
        borderWidth: 1,
      },
    ],
  };

  if (!topProductsChartInstance) {
    topProductsChartInstance = new Chart(ctx, {
      type: 'bar',
      data: chartData,
      options: {
        indexAxis: 'y',
        responsive: true,
        maintainAspectRatio: true,
        plugins: {
          legend: {
            display: true,
            labels: { color: '#1e293b', font: { size: 12 } },
          },
        },
        scales: {
          x: {
            ticks: { color: '#64748b' },
            grid: { color: 'rgba(22, 163, 74, 0.1)' },
          },
          y: {
            ticks: { color: '#64748b' },
            grid: { display: false },
          },
        },
      },
    });
  } else {
    topProductsChartInstance.data = chartData;
    topProductsChartInstance.update();
  }
}

/**
 * Update Order Status Chart
 */
function updateOrderStatusChart(ordersByStatus) {
  const ctx = document.getElementById('orderStatusChart');
  if (!ctx) return;

  const labels = Object.keys(ordersByStatus).map((l) => l.charAt(0).toUpperCase() + l.slice(1));
  const data = Object.values(ordersByStatus);
  const colors = [CHART_COLORS.success, CHART_COLORS.warning, CHART_COLORS.danger];

  const chartData = {
    labels: labels,
    datasets: [
      {
        label: 'Number of Orders',
        data: data,
        backgroundColor: colors.slice(0, data.length),
        borderColor: '#ffffff',
        borderWidth: 2,
      },
    ],
  };

  if (!orderStatusChartInstance) {
    orderStatusChartInstance = new Chart(ctx, {
      type: 'bar',
      data: chartData,
      options: {
        responsive: true,
        maintainAspectRatio: true,
        plugins: {
          legend: {
            display: true,
            labels: { color: '#1e293b', font: { size: 12 } },
          },
        },
        scales: {
          y: {
            ticks: { color: '#64748b' },
            grid: { color: 'rgba(22, 163, 74, 0.1)' },
          },
          x: {
            ticks: { color: '#64748b' },
            grid: { color: 'rgba(22, 163, 74, 0.1)' },
          },
        },
      },
    });
  } else {
    orderStatusChartInstance.data = chartData;
    orderStatusChartInstance.update();
  }
}

/**
 * Render recent orders table
 */
function renderRecentOrders(orders) {
  const tbody = document.getElementById('recentOrdersBody');
  if (!tbody) return;

  tbody.innerHTML = '';

  // Convert to array and sort by date (newest first)
  const ordersArray = Object.entries(orders)
    .map(([id, order]) => ({ id, ...order }))
    .filter((o) => o && typeof o === 'object')
    .sort((a, b) => (b.createdAt || 0) - (a.createdAt || 0))
    .slice(0, 20);

  if (ordersArray.length === 0) {
    tbody.innerHTML = '<tr><td colspan="6" class="text-center">No orders found</td></tr>';
    return;
  }

  ordersArray.forEach((order) => {
    const tr = document.createElement('tr');
    const itemCount = order.items ? (Array.isArray(order.items) ? order.items.length : 0) : 0;
    const totalAmount = formatCurrency(order.totalAmount || 0);
    const status = (order.status || 'pending').charAt(0).toUpperCase() + (order.status || 'pending').slice(1);
    const statusClass = `badge badge--${order.status === 'completed' ? 'approved' : order.status === 'cancelled' ? 'rejected' : 'pending'}`;
    const date = formatDate(order.createdAt || 0);

    tr.innerHTML = `
      <td><code style="font-size: 0.75rem;">${order.id.substring(0, 8)}...</code></td>
      <td>${order.shopName || order.supplierId || '-'}</td>
      <td style="font-weight: 600; color: #22c55e;">${totalAmount}</td>
      <td>${itemCount}</td>
      <td><span class="${statusClass}">${status}</span></td>
      <td>${date}</td>
    `;
    tbody.appendChild(tr);
  });
}

/**
 * Main analytics initialization - load all data and render
 */
export function initAnalytics() {
  console.log('[ANALYTICS] Initializing analytics module');

  onValue(ref(db, 'orders'), (snapshot) => {
    try {
      console.log('[ANALYTICS] Orders data loaded');

      if (!snapshot.exists()) {
        console.log('[ANALYTICS] No orders found');
        return;
      }

      const orders = snapshot.val();
      const stats = calculateStats(orders);

      // Update all visualizations
      updateKPICards(stats);
      updateSalesLineChart(stats.salesByDate);
      updateSalesPieChart(stats.ordersByStatus);
      updateTopProductsChart(stats.productsSold);
      updateOrderStatusChart(stats.ordersByStatus);
      renderRecentOrders(orders);

      console.log('[ANALYTICS] All analytics updated', stats);
    } catch (err) {
      console.error('[ANALYTICS] Error loading analytics:', err);
    }
  });
}
