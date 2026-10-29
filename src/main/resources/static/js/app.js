/**
 * E-Commerce Management System - Core Application Logic
 */

document.addEventListener('DOMContentLoaded', async () => {
  initUI();
  await loadInitialData();
});

// ---------------- Toast Notifications ----------------
function showToast(message, type = 'info') {
  const container = document.getElementById('toast-container');
  if (!container) return;

  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  toast.innerHTML = `
    <span>${type === 'success' ? '✓' : type === 'error' ? '✕' : 'ℹ'}</span>
    <span>${message}</span>
  `;
  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateX(100%)';
    setTimeout(() => toast.remove(), 300);
  }, 3500);
}

// ---------------- UI & Navigation Initialization ----------------
function initUI() {
  updateUserDisplay();

  // Navigation Links
  document.querySelectorAll('[data-view-target]').forEach(link => {
    link.addEventListener('click', (e) => {
      e.preventDefault();
      const targetView = link.getAttribute('data-view-target');
      switchView(targetView);
    });
  });

  // Admin Sub-tabs
  document.querySelectorAll('.admin-tab').forEach(tab => {
    tab.addEventListener('click', () => {
      const tabTarget = tab.getAttribute('data-tab');
      switchAdminTab(tabTarget);
    });
  });

  // Cart Drawer open/close
  const btnCart = document.getElementById('btn-cart-toggle');
  const cartBackdrop = document.getElementById('cart-backdrop');
  const btnCloseCart = document.getElementById('btn-close-cart');

  if (btnCart) btnCart.addEventListener('click', openCartDrawer);
  if (cartBackdrop) cartBackdrop.addEventListener('click', closeCartDrawer);
  if (btnCloseCart) btnCloseCart.addEventListener('click', closeCartDrawer);

  // Search and Filter controls
  const searchInput = document.getElementById('search-input');
  if (searchInput) {
    let debounceTimer;
    searchInput.addEventListener('input', (e) => {
      clearTimeout(debounceTimer);
      debounceTimer = setTimeout(() => {
        state.searchQuery = e.target.value.trim();
        loadProducts();
      }, 300);
    });
  }

  const sortSelect = document.getElementById('sort-select');
  if (sortSelect) {
    sortSelect.addEventListener('change', (e) => {
      state.sortBy = e.target.value;
      renderProductsGrid();
    });
  }

  // Auth toggle button
  const btnSwitchRole = document.getElementById('btn-switch-role');
  if (btnSwitchRole) {
    btnSwitchRole.addEventListener('click', toggleDemoRole);
  }

  const btnOpenAuth = document.getElementById('btn-open-auth');
  if (btnOpenAuth) {
    btnOpenAuth.addEventListener('click', () => openModal('auth-modal'));
  }

  // Checkout trigger
  const btnCheckout = document.getElementById('btn-checkout');
  if (btnCheckout) {
    btnCheckout.addEventListener('click', () => {
      if (state.cart.items.length === 0) {
        showToast('Your cart is empty!', 'error');
        return;
      }
      closeCartDrawer();
      openModal('checkout-modal');
      renderCheckoutSummary();
    });
  }

  // Setup Modal close buttons
  document.querySelectorAll('.modal-close-trigger').forEach(btn => {
    btn.addEventListener('click', (e) => {
      const modal = e.target.closest('.modal-overlay');
      if (modal) modal.classList.remove('active');
    });
  });
}

// ---------------- Data Loading ----------------
async function loadInitialData() {
  try {
    await Promise.all([
      loadCategories(),
      loadProducts(),
      loadCart()
    ]);
  } catch (err) {
    showToast('Failed to connect to backend: ' + err.message, 'error');
  }
}

async function loadCategories() {
  try {
    const res = await apiClient.categories.getAll();
    if (res.success) {
      state.categories = res.data;
      renderCategoryChips();
      populateCategorySelects();
    }
  } catch (err) {
    console.error('Error loading categories:', err);
  }
}

async function loadProducts() {
  try {
    const res = await apiClient.products.getAll(
      state.selectedCategoryId,
      null,
      state.searchQuery
    );
    if (res.success) {
      state.products = res.data;
      renderProductsGrid();
    }
  } catch (err) {
    showToast('Error loading products: ' + err.message, 'error');
  }
}

async function loadCart() {
  try {
    const res = await apiClient.cart.get();
    if (res.success) {
      state.cart = res.data;
      updateCartBadge();
      renderCartItems();
    }
  } catch (err) {
    console.error('Error loading cart:', err);
  }
}

async function loadCustomerOrders() {
  try {
    const res = await apiClient.orders.getCustomerOrders();
    if (res.success) {
      state.orders = res.data;
      renderOrdersView();
    }
  } catch (err) {
    showToast('Error loading orders: ' + err.message, 'error');
  }
}

async function loadAdminData() {
  try {
    const [metricsRes, ordersRes, auditsRes, lowStockRes] = await Promise.all([
      apiClient.dashboard.getMetrics(),
      apiClient.orders.getAllOrders(),
      apiClient.inventory.getAudits(),
      apiClient.inventory.getLowStock()
    ]);

    if (metricsRes.success) state.adminMetrics = metricsRes.data;
    if (ordersRes.success) state.adminOrders = ordersRes.data;
    if (auditsRes.success) state.adminAudits = auditsRes.data;

    renderAdminDashboard();
  } catch (err) {
    showToast('Error loading admin data: ' + err.message, 'error');
  }
}

// ---------------- View Switching ----------------
function switchView(viewName) {
  if (viewName === 'admin' && (!state.currentUser || state.currentUser.role !== 'ROLE_ADMIN')) {
    showToast('Admin privilege required. Switched to Demo Admin account for convenience!', 'info');
    switchToAdmin();
  }

  state.activeView = viewName;

  document.querySelectorAll('[data-view-target]').forEach(link => {
    link.classList.toggle('active', link.getAttribute('data-view-target') === viewName);
  });

  document.querySelectorAll('.view-section').forEach(sec => {
    sec.classList.remove('active');
  });

  const activeSec = document.getElementById(`view-${viewName}`);
  if (activeSec) activeSec.classList.add('active');

  if (viewName === 'orders') {
    loadCustomerOrders();
  } else if (viewName === 'admin') {
    loadAdminData();
  } else if (viewName === 'store') {
    loadProducts();
  }
}

function switchAdminTab(tabName) {
  state.activeAdminTab = tabName;

  document.querySelectorAll('.admin-tab').forEach(t => {
    t.classList.toggle('active', t.getAttribute('data-tab') === tabName);
  });

  document.querySelectorAll('.admin-subpanel').forEach(p => {
    p.classList.remove('active');
  });

  const activeSubpanel = document.getElementById(`admin-panel-${tabName}`);
  if (activeSubpanel) activeSubpanel.classList.add('active');
}

// ---------------- Storefront Rendering ----------------
function renderCategoryChips() {
  const container = document.getElementById('category-chips');
  if (!container) return;

  container.innerHTML = `
    <button class="chip ${state.selectedCategoryId === null ? 'active' : ''}" onclick="selectCategory(null)">
      All Products
    </button>
  `;

  state.categories.forEach(cat => {
    const btn = document.createElement('button');
    btn.className = `chip ${state.selectedCategoryId === cat.id ? 'active' : ''}`;
    btn.textContent = cat.name;
    btn.onclick = () => selectCategory(cat.id);
    container.appendChild(btn);
  });
}

window.selectCategory = function(categoryId) {
  state.selectedCategoryId = categoryId;
  renderCategoryChips();
  loadProducts();
};

function renderProductsGrid() {
  const grid = document.getElementById('products-grid');
  if (!grid) return;

  let items = [...state.products];

  // Sorting
  if (state.sortBy === 'price-asc') {
    items.sort((a, b) => a.price - b.price);
  } else if (state.sortBy === 'price-desc') {
    items.sort((a, b) => b.price - a.price);
  } else if (state.sortBy === 'name') {
    items.sort((a, b) => a.name.localeCompare(b.name));
  }

  if (items.length === 0) {
    grid.innerHTML = `
      <div style="grid-column: 1 / -1; text-align: center; padding: 3rem; background: white; border-radius: var(--radius-lg); border: 1px solid var(--border);">
        <h3>No products found</h3>
        <p style="color: var(--text-muted); margin-top: 0.5rem;">Try adjusting your keyword search or category filter.</p>
      </div>
    `;
    return;
  }

  grid.innerHTML = items.map(p => {
    let stockClass = 'in-stock';
    let stockText = `${p.stockQuantity} in stock`;

    if (p.stockQuantity <= 0) {
      stockClass = 'out-of-stock';
      stockText = 'Out of Stock';
    } else if (p.stockQuantity <= 10) {
      stockClass = 'low-stock';
      stockText = `Only ${p.stockQuantity} left!`;
    }

    const disabledAttr = p.stockQuantity <= 0 ? 'disabled' : '';

    return `
      <div class="product-card">
        <div class="product-image-wrap">
          <img src="${p.imageUrl || 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600'}" alt="${p.name}" class="product-img" loading="lazy">
          <span class="stock-tag ${stockClass}">${stockText}</span>
        </div>
        <div class="product-details">
          <span class="product-category">${p.categoryName || 'General'}</span>
          <h3 class="product-title" title="${p.name}">${p.name}</h3>
          <div class="product-sku">SKU: ${p.sku}</div>
          <div class="product-footer">
            <span class="product-price">$${parseFloat(p.price).toFixed(2)}</span>
            <button class="btn btn-primary btn-sm" onclick="handleAddToCart(${p.id})" ${disabledAttr}>
              ${p.stockQuantity <= 0 ? 'Sold Out' : 'Add to Cart'}
            </button>
          </div>
        </div>
      </div>
    `;
  }).join('');
}

// ---------------- Cart Actions ----------------
window.handleAddToCart = async function(productId) {
  try {
    const res = await apiClient.cart.addItem(productId, 1);
    if (res.success) {
      state.cart = res.data;
      updateCartBadge();
      renderCartItems();
      showToast('Added to cart!', 'success');
    }
  } catch (err) {
    showToast(err.message, 'error');
  }
};

window.handleUpdateCartItem = async function(itemId, newQty) {
  if (newQty <= 0) {
    handleRemoveCartItem(itemId);
    return;
  }
  try {
    const res = await apiClient.cart.updateQuantity(itemId, newQty);
    if (res.success) {
      state.cart = res.data;
      updateCartBadge();
      renderCartItems();
    }
  } catch (err) {
    showToast(err.message, 'error');
  }
};

window.handleRemoveCartItem = async function(itemId) {
  try {
    const res = await apiClient.cart.removeItem(itemId);
    if (res.success) {
      state.cart = res.data;
      updateCartBadge();
      renderCartItems();
      showToast('Item removed from cart', 'info');
    }
  } catch (err) {
    showToast(err.message, 'error');
  }
};

function updateCartBadge() {
  const badge = document.getElementById('cart-badge');
  if (badge) {
    badge.textContent = state.cart.totalItemCount || 0;
  }
}

function openCartDrawer() {
  document.getElementById('cart-drawer').classList.add('open');
  document.getElementById('cart-backdrop').classList.add('active');
}

function closeCartDrawer() {
  document.getElementById('cart-drawer').classList.remove('open');
  document.getElementById('cart-backdrop').classList.remove('active');
}

function renderCartItems() {
  const container = document.getElementById('cart-items-container');
  if (!container) return;

  if (!state.cart.items || state.cart.items.length === 0) {
    container.innerHTML = `
      <div style="text-align: center; padding: 2.5rem 1rem; color: var(--text-muted);">
        <div style="font-size: 2.5rem; margin-bottom: 0.5rem;">🛒</div>
        <p>Your shopping cart is empty.</p>
        <button class="btn btn-secondary btn-sm" style="margin-top: 1rem;" onclick="closeCartDrawer()">Explore Catalog</button>
      </div>
    `;
  } else {
    container.innerHTML = state.cart.items.map(item => `
      <div class="cart-item-card">
        <img src="${item.productImageUrl || 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600'}" class="cart-item-img" alt="${item.productName}">
        <div class="cart-item-info">
          <div class="cart-item-title">${item.productName}</div>
          <div class="cart-item-price">$${parseFloat(item.unitPrice).toFixed(2)}</div>
          <div class="cart-qty-control">
            <button class="qty-btn" onclick="handleUpdateCartItem(${item.id}, ${item.quantity - 1})">-</button>
            <span class="qty-display">${item.quantity}</span>
            <button class="qty-btn" onclick="handleUpdateCartItem(${item.id}, ${item.quantity + 1})" ${item.quantity >= item.availableStock ? 'disabled title="Max stock reached"' : ''}>+</button>
          </div>
        </div>
        <button class="btn btn-danger btn-sm" onclick="handleRemoveCartItem(${item.id})" title="Remove item">✕</button>
      </div>
    `).join('');
  }

  // Cart summary footer
  const subtotalEl = document.getElementById('cart-subtotal');
  const taxEl = document.getElementById('cart-tax');
  const shippingEl = document.getElementById('cart-shipping');
  const totalEl = document.getElementById('cart-total');

  if (subtotalEl) subtotalEl.textContent = `$${parseFloat(state.cart.subtotal || 0).toFixed(2)}`;
  if (taxEl) taxEl.textContent = `$${parseFloat(state.cart.estimatedTax || 0).toFixed(2)}`;
  if (shippingEl) {
    shippingEl.textContent = state.cart.estimatedShipping > 0
      ? `$${parseFloat(state.cart.estimatedShipping).toFixed(2)}`
      : 'FREE';
  }
  if (totalEl) totalEl.textContent = `$${parseFloat(state.cart.totalAmount || 0).toFixed(2)}`;
}

// ---------------- Checkout Process ----------------
function renderCheckoutSummary() {
  const container = document.getElementById('checkout-items-summary');
  if (!container) return;

  container.innerHTML = state.cart.items.map(item => `
    <div style="display: flex; justify-content: space-between; font-size: 0.85rem; padding: 0.35rem 0; border-bottom: 1px solid #f1f5f9;">
      <span>${item.productName} × ${item.quantity}</span>
      <strong>$${parseFloat(item.subtotal).toFixed(2)}</strong>
    </div>
  `).join('');

  document.getElementById('checkout-subtotal').textContent = `$${parseFloat(state.cart.subtotal).toFixed(2)}`;
  document.getElementById('checkout-tax').textContent = `$${parseFloat(state.cart.estimatedTax).toFixed(2)}`;
  document.getElementById('checkout-shipping').textContent = state.cart.estimatedShipping > 0
    ? `$${parseFloat(state.cart.estimatedShipping).toFixed(2)}`
    : 'FREE';
  document.getElementById('checkout-total').textContent = `$${parseFloat(state.cart.totalAmount).toFixed(2)}`;
}

window.handlePlaceOrder = async function(e) {
  e.preventDefault();
  const address = document.getElementById('checkout-address').value.trim();
  const paymentMethod = document.getElementById('checkout-payment').value;
  const notes = document.getElementById('checkout-notes').value.trim();

  if (!address) {
    showToast('Please enter a delivery address.', 'error');
    return;
  }

  try {
    const res = await apiClient.orders.create({
      shippingAddress: address,
      paymentMethod: paymentMethod,
      notes: notes
    });

    if (res.success) {
      closeModal('checkout-modal');
      showToast(`Order #${res.data.orderNumber} placed successfully!`, 'success');
      await loadCart();
      switchView('orders');
    }
  } catch (err) {
    showToast(err.message, 'error');
  }
};

// ---------------- Customer Orders View ----------------
function renderOrdersView() {
  const container = document.getElementById('customer-orders-container');
  if (!container) return;

  if (state.orders.length === 0) {
    container.innerHTML = `
      <div style="text-align: center; padding: 4rem 1rem; background: white; border-radius: var(--radius-lg); border: 1px solid var(--border);">
        <h3>No orders yet</h3>
        <p style="color: var(--text-muted); margin-top: 0.5rem;">Your completed purchases will appear here.</p>
        <button class="btn btn-primary" style="margin-top: 1rem;" onclick="switchView('store')">Start Shopping</button>
      </div>
    `;
    return;
  }

  container.innerHTML = state.orders.map(o => `
    <div class="order-card">
      <div class="order-header">
        <div class="order-title-group">
          <h4>Order #${o.orderNumber}</h4>
          <span class="order-meta">Placed on ${new Date(o.createdAt).toLocaleString()}</span>
        </div>
        <div style="display: flex; align-items: center; gap: 0.75rem;">
          <span class="status-badge ${o.orderStatus}">${o.orderStatus}</span>
          ${o.orderStatus === 'PENDING' ? `
            <button class="btn btn-danger btn-sm" onclick="handleCancelOrder(${o.id})">Cancel Order</button>
          ` : ''}
        </div>
      </div>
      <div style="margin-bottom: 1rem;">
        <p style="font-size: 0.85rem; color: var(--text-secondary);"><strong>Shipping to:</strong> ${o.shippingAddress}</p>
        <p style="font-size: 0.85rem; color: var(--text-secondary);"><strong>Payment:</strong> ${o.paymentMethod} (${o.paymentStatus})</p>
      </div>
      <div style="background: #f8fafc; padding: 1rem; border-radius: var(--radius-md);">
        ${o.items.map(item => `
          <div class="order-item-row">
            <span><strong>${item.productName}</strong> (SKU: ${item.productSku}) × ${item.quantity}</span>
            <span>$${parseFloat(item.subtotal).toFixed(2)}</span>
          </div>
        `).join('')}
        <div style="display: flex; justify-content: flex-end; gap: 1.5rem; margin-top: 0.75rem; padding-top: 0.75rem; border-top: 1px dashed var(--border); font-size: 0.95rem;">
          <span>Subtotal: $${parseFloat(o.subtotal).toFixed(2)}</span>
          <span>Tax: $${parseFloat(o.tax).toFixed(2)}</span>
          <span>Shipping: $${parseFloat(o.shippingFee).toFixed(2)}</span>
          <strong>Total: $${parseFloat(o.totalAmount).toFixed(2)}</strong>
        </div>
      </div>
    </div>
  `).join('');
}

window.handleCancelOrder = async function(orderId) {
  if (!confirm('Are you sure you want to cancel this order? Items will be returned to inventory stock.')) {
    return;
  }
  try {
    const res = await apiClient.orders.cancelOrder(orderId);
    if (res.success) {
      showToast('Order cancelled and inventory restored!', 'success');
      await loadCustomerOrders();
    }
  } catch (err) {
    showToast(err.message, 'error');
  }
};

// ---------------- Admin Dashboard ----------------
function renderAdminDashboard() {
  const m = state.adminMetrics || {};

  // KPI Metrics
  const revEl = document.getElementById('metric-revenue');
  const ordersEl = document.getElementById('metric-orders');
  const productsEl = document.getElementById('metric-products');
  const alertsEl = document.getElementById('metric-alerts');

  if (revEl) revEl.textContent = `$${parseFloat(m.totalRevenue || 0).toFixed(2)}`;
  if (ordersEl) ordersEl.textContent = m.totalOrders || 0;
  if (productsEl) productsEl.textContent = m.totalProducts || 0;
  if (alertsEl) alertsEl.textContent = m.lowStockCount || 0;

  // Render Low Stock Alert Table
  renderAdminLowStockTable();

  // Render Inventory Audit History Table
  renderAdminAuditsTable();

  // Render Products Management Table
  renderAdminProductsTable();

  // Render Admin Orders Fulfillment Table
  renderAdminOrdersTable();
}

function renderAdminLowStockTable() {
  const container = document.getElementById('admin-low-stock-table-body');
  if (!container) return;

  const lowStockItems = state.products.filter(p => p.stockQuantity <= 10);

  if (lowStockItems.length === 0) {
    container.innerHTML = `<tr><td colspan="5" style="text-align: center; color: var(--text-muted); padding: 1.5rem;">All inventory levels are healthy! No low stock warnings.</td></tr>`;
    return;
  }

  container.innerHTML = lowStockItems.map(p => `
    <tr>
      <td><strong>${p.sku}</strong></td>
      <td>${p.name}</td>
      <td><span class="stock-tag low-stock">${p.stockQuantity} remaining</span></td>
      <td>${p.categoryName || '-'}</td>
      <td>
        <button class="btn btn-success btn-sm" onclick="quickRestock(${p.id}, 10)">+10 Restock</button>
        <button class="btn btn-secondary btn-sm" onclick="openStockAdjustModal(${p.id})">Custom Adjust</button>
      </td>
    </tr>
  `).join('');
}

function renderAdminAuditsTable() {
  const container = document.getElementById('admin-audits-table-body');
  if (!container) return;

  if (!state.adminAudits || state.adminAudits.length === 0) {
    container.innerHTML = `<tr><td colspan="6" style="text-align: center; color: var(--text-muted); padding: 1.5rem;">No stock movement records yet.</td></tr>`;
    return;
  }

  container.innerHTML = state.adminAudits.map(a => `
    <tr>
      <td>${new Date(a.createdAt).toLocaleString()}</td>
      <td><strong>${a.productSku}</strong></td>
      <td>${a.productName}</td>
      <td><span class="status-badge ${a.movementType}">${a.movementType}</span></td>
      <td>${a.previousQuantity} → <strong>${a.newQuantity}</strong> (${a.quantityChanged > 0 ? '+' : ''}${a.quantityChanged})</td>
      <td>${a.reason} <em>(${a.performedBy})</em></td>
    </tr>
  `).join('');
}

function renderAdminProductsTable() {
  const container = document.getElementById('admin-products-table-body');
  if (!container) return;

  container.innerHTML = state.products.map(p => `
    <tr>
      <td><strong>${p.sku}</strong></td>
      <td><img src="${p.imageUrl}" style="width: 38px; height: 38px; object-fit: cover; border-radius: 4px;"></td>
      <td>${p.name}</td>
      <td>${p.categoryName || '-'}</td>
      <td>$${parseFloat(p.price).toFixed(2)}</td>
      <td><strong>${p.stockQuantity}</strong></td>
      <td><span class="status-badge ${p.status}">${p.status}</span></td>
      <td>
        <button class="btn btn-secondary btn-sm" onclick="openStockAdjustModal(${p.id})">Restock</button>
        <button class="btn btn-danger btn-sm" onclick="handleDeleteProduct(${p.id})">Archive</button>
      </td>
    </tr>
  `).join('');
}

function renderAdminOrdersTable() {
  const container = document.getElementById('admin-orders-table-body');
  if (!container) return;

  if (!state.adminOrders || state.adminOrders.length === 0) {
    container.innerHTML = `<tr><td colspan="6" style="text-align: center; color: var(--text-muted); padding: 1.5rem;">No customer orders found.</td></tr>`;
    return;
  }

  container.innerHTML = state.adminOrders.map(o => `
    <tr>
      <td><strong>${o.orderNumber}</strong></td>
      <td>${o.userFullName} <br><small style="color:var(--text-muted);">${o.userEmail}</small></td>
      <td>$${parseFloat(o.totalAmount).toFixed(2)}</td>
      <td>${new Date(o.createdAt).toLocaleDateString()}</td>
      <td><span class="status-badge ${o.orderStatus}">${o.orderStatus}</span></td>
      <td>
        <select class="form-control" style="width: 140px; padding: 0.25rem 0.5rem; font-size: 0.8rem;" onchange="handleAdminUpdateOrderStatus(${o.id}, this.value)">
          <option value="PENDING" ${o.orderStatus === 'PENDING' ? 'selected' : ''}>PENDING</option>
          <option value="CONFIRMED" ${o.orderStatus === 'CONFIRMED' ? 'selected' : ''}>CONFIRMED</option>
          <option value="SHIPPED" ${o.orderStatus === 'SHIPPED' ? 'selected' : ''}>SHIPPED</option>
          <option value="DELIVERED" ${o.orderStatus === 'DELIVERED' ? 'selected' : ''}>DELIVERED</option>
          <option value="CANCELLED" ${o.orderStatus === 'CANCELLED' ? 'selected' : ''}>CANCELLED</option>
        </select>
      </td>
    </tr>
  `).join('');
}

// ---------------- Admin Actions ----------------
window.quickRestock = async function(productId, amount) {
  try {
    const res = await apiClient.inventory.adjust(productId, {
      quantityChange: amount,
      movementType: 'RESTOCK',
      reason: `Quick batch restock (+${amount} units)`
    });

    if (res.success) {
      showToast(`Restocked +${amount} units!`, 'success');
      await Promise.all([loadProducts(), loadAdminData()]);
    }
  } catch (err) {
    showToast(err.message, 'error');
  }
};

window.openStockAdjustModal = function(productId) {
  const product = state.products.find(p => p.id === productId);
  if (!product) return;

  document.getElementById('adjust-product-id').value = product.id;
  document.getElementById('adjust-product-name').textContent = `${product.name} (Current Stock: ${product.stockQuantity})`;
  document.getElementById('adjust-qty-change').value = '10';
  document.getElementById('adjust-reason').value = 'Supplier restock batch';
  openModal('stock-adjust-modal');
};

window.handleSaveStockAdjustment = async function(e) {
  e.preventDefault();
  const productId = document.getElementById('adjust-product-id').value;
  const qtyChange = parseInt(document.getElementById('adjust-qty-change').value, 10);
  const movementType = document.getElementById('adjust-movement-type').value;
  const reason = document.getElementById('adjust-reason').value.trim();

  try {
    const res = await apiClient.inventory.adjust(productId, {
      quantityChange: qtyChange,
      movementType: movementType,
      reason: reason || 'Inventory update'
    });

    if (res.success) {
      closeModal('stock-adjust-modal');
      showToast('Inventory updated with audit log!', 'success');
      await Promise.all([loadProducts(), loadAdminData()]);
    }
  } catch (err) {
    showToast(err.message, 'error');
  }
};

window.handleAdminUpdateOrderStatus = async function(orderId, newStatus) {
  try {
    const res = await apiClient.orders.updateStatus(orderId, {
      orderStatus: newStatus,
      statusNotes: `Updated by Admin to ${newStatus}`
    });

    if (res.success) {
      showToast(`Order status updated to ${newStatus}`, 'success');
      await Promise.all([loadAdminData(), loadProducts()]);
    }
  } catch (err) {
    showToast(err.message, 'error');
  }
};

window.handleDeleteProduct = async function(productId) {
  if (!confirm('Are you sure you want to archive this product?')) return;
  try {
    const res = await apiClient.products.delete(productId);
    if (res.success) {
      showToast('Product archived successfully', 'info');
      await Promise.all([loadProducts(), loadAdminData()]);
    }
  } catch (err) {
    showToast(err.message, 'error');
  }
};

window.handleCreateProduct = async function(e) {
  e.preventDefault();
  const payload = {
    sku: document.getElementById('prod-sku').value.trim(),
    name: document.getElementById('prod-name').value.trim(),
    categoryId: parseInt(document.getElementById('prod-category').value, 10),
    price: parseFloat(document.getElementById('prod-price').value),
    stockQuantity: parseInt(document.getElementById('prod-stock').value, 10),
    imageUrl: document.getElementById('prod-image').value.trim(),
    description: document.getElementById('prod-desc').value.trim(),
    status: 'ACTIVE'
  };

  try {
    const res = await apiClient.products.create(payload);
    if (res.success) {
      closeModal('add-product-modal');
      showToast('New product added to catalog!', 'success');
      await Promise.all([loadProducts(), loadAdminData()]);
    }
  } catch (err) {
    showToast(err.message, 'error');
  }
};

// ---------------- Demo Auth & User Management ----------------
function toggleDemoRole() {
  if (state.currentUser && state.currentUser.role === 'ROLE_ADMIN') {
    switchToCustomer();
  } else {
    switchToAdmin();
  }
}

function switchToAdmin() {
  state.setCurrentUser({
    id: 1,
    username: 'admin',
    fullName: 'Store Administrator',
    email: 'admin@eshop.com',
    role: 'ROLE_ADMIN',
    token: 'token_admin_demo'
  });
  updateUserDisplay();
  showToast('Switched to Demo Admin (admin@eshop.com)', 'success');
  if (state.activeView === 'admin') {
    loadAdminData();
  }
}

function switchToCustomer() {
  state.setCurrentUser({
    id: 2,
    username: 'john_doe',
    fullName: 'Johnathan Doe',
    email: 'john@eshop.com',
    role: 'ROLE_CUSTOMER',
    token: 'token_customer_demo'
  });
  updateUserDisplay();
  showToast('Switched to Demo Customer (john@eshop.com)', 'info');
  loadCart();
  if (state.activeView === 'orders') {
    loadCustomerOrders();
  }
}

function updateUserDisplay() {
  const user = state.currentUser;
  const nameEl = document.getElementById('user-display-name');
  const roleEl = document.getElementById('user-display-role');
  const switchBtn = document.getElementById('btn-switch-role');

  if (user) {
    if (nameEl) nameEl.textContent = user.fullName || user.username;
    if (roleEl) {
      roleEl.textContent = user.role === 'ROLE_ADMIN' ? 'ADMIN' : 'CUSTOMER';
      roleEl.className = `user-role-pill ${user.role === 'ROLE_ADMIN' ? 'admin' : 'customer'}`;
    }
    if (switchBtn) {
      switchBtn.textContent = user.role === 'ROLE_ADMIN' ? 'Switch to Customer' : 'Switch to Admin';
    }
  }
}

function populateCategorySelects() {
  const prodCatSelect = document.getElementById('prod-category');
  if (!prodCatSelect) return;

  prodCatSelect.innerHTML = state.categories.map(c => `
    <option value="${c.id}">${c.name}</option>
  `).join('');
}

// ---------------- Modal Helpers ----------------
function openModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) modal.classList.add('active');
}

function closeModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) modal.classList.remove('active');
}

window.openModal = openModal;
window.closeModal = closeModal;
window.switchToAdmin = switchToAdmin;
window.switchToCustomer = switchToCustomer;
