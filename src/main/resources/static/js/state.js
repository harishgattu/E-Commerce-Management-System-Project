/**
 * Central Client-Side Application State
 */

const state = {
  // Demo customer user preset by default so everything works right away!
  currentUser: JSON.parse(localStorage.getItem('eshop_user')) || {
    id: 2,
    username: 'john_doe',
    fullName: 'Johnathan Doe',
    email: 'john@eshop.com',
    role: 'ROLE_CUSTOMER',
    token: 'token_customer_demo'
  },

  activeView: 'store',
  activeAdminTab: 'inventory',
  
  categories: [],
  products: [],
  selectedCategoryId: null,
  searchQuery: '',
  sortBy: 'default',
  inStockOnly: false,

  cart: {
    items: [],
    subtotal: 0,
    estimatedTax: 0,
    estimatedShipping: 0,
    totalAmount: 0,
    totalItemCount: 0
  },

  orders: [],
  adminOrders: [],
  adminAudits: [],
  adminMetrics: null,

  setCurrentUser(user) {
    this.currentUser = user;
    if (user) {
      localStorage.setItem('eshop_user', JSON.stringify(user));
    } else {
      localStorage.removeItem('eshop_user');
    }
  }
};
