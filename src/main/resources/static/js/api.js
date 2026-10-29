/**
 * E-Commerce Management System - REST Client
 */

const API_BASE = '/api';

const apiClient = {
  getHeaders() {
    const user = state.currentUser;
    const headers = {
      'Content-Type': 'application/json',
    };
    if (user) {
      headers['X-User-Id'] = user.id;
      headers['X-User-Role'] = user.role;
      headers['X-User-Name'] = user.username;
      if (user.token) {
        headers['Authorization'] = 'Bearer ' + user.token;
      }
    } else {
      // Default fallback for instant unauthenticated demo preview
      headers['X-User-Id'] = '2';
      headers['X-User-Role'] = 'ROLE_CUSTOMER';
      headers['X-User-Name'] = 'john_doe';
    }
    return headers;
  },

  async request(endpoint, options = {}) {
    const config = {
      ...options,
      headers: {
        ...this.getHeaders(),
        ...(options.headers || {})
      }
    };

    try {
      const response = await fetch(`${API_BASE}${endpoint}`, config);
      const data = await response.json().catch(() => null);

      if (!response.ok) {
        const errorMsg = (data && data.message) ? data.message : `HTTP error ${response.status}`;
        throw new Error(errorMsg);
      }
      return data;
    } catch (err) {
      console.error(`API Error on ${endpoint}:`, err);
      throw err;
    }
  },

  // Auth
  auth: {
    register: (payload) => apiClient.request('/auth/register', { method: 'POST', body: JSON.stringify(payload) }),
    login: (payload) => apiClient.request('/auth/login', { method: 'POST', body: JSON.stringify(payload) })
  },

  // Products
  products: {
    getAll: (categoryId = null, status = null, keyword = null) => {
      const params = new URLSearchParams();
      if (categoryId) params.append('categoryId', categoryId);
      if (status) params.append('status', status);
      if (keyword) params.append('keyword', keyword);
      const query = params.toString() ? `?${params.toString()}` : '';
      return apiClient.request(`/products${query}`);
    },
    getById: (id) => apiClient.request(`/products/${id}`),
    create: (payload) => apiClient.request('/products', { method: 'POST', body: JSON.stringify(payload) }),
    update: (id, payload) => apiClient.request(`/products/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),
    delete: (id) => apiClient.request(`/products/${id}`, { method: 'DELETE' })
  },

  // Categories
  categories: {
    getAll: () => apiClient.request('/categories'),
    create: (payload) => apiClient.request('/categories', { method: 'POST', body: JSON.stringify(payload) })
  },

  // Cart
  cart: {
    get: () => apiClient.request('/cart'),
    addItem: (productId, quantity = 1) => apiClient.request('/cart/items', {
      method: 'POST',
      body: JSON.stringify({ productId, quantity })
    }),
    updateQuantity: (itemId, quantity) => apiClient.request(`/cart/items/${itemId}`, {
      method: 'PUT',
      body: JSON.stringify({ quantity })
    }),
    removeItem: (itemId) => apiClient.request(`/cart/items/${itemId}`, { method: 'DELETE' }),
    clear: () => apiClient.request('/cart', { method: 'DELETE' })
  },

  // Orders
  orders: {
    create: (payload) => apiClient.request('/orders', { method: 'POST', body: JSON.stringify(payload) }),
    getCustomerOrders: () => apiClient.request('/orders'),
    getAllOrders: (status = null) => {
      const query = status ? `?status=${status}` : '';
      return apiClient.request(`/orders/all${query}`);
    },
    getById: (id) => apiClient.request(`/orders/${id}`),
    updateStatus: (id, payload) => apiClient.request(`/orders/${id}/status`, {
      method: 'PUT',
      body: JSON.stringify(payload)
    }),
    cancelOrder: (id) => apiClient.request(`/orders/${id}/cancel`, { method: 'POST' })
  },

  // Inventory
  inventory: {
    adjust: (productId, payload) => apiClient.request(`/inventory/adjust/${productId}`, {
      method: 'POST',
      body: JSON.stringify(payload)
    }),
    getLowStock: (threshold = 10) => apiClient.request(`/inventory/low-stock?threshold=${threshold}`),
    getAudits: () => apiClient.request('/inventory/audits'),
    getProductAudits: (productId) => apiClient.request(`/inventory/audits/product/${productId}`)
  },

  // Dashboard
  dashboard: {
    getMetrics: () => apiClient.request('/dashboard/metrics')
  }
};
