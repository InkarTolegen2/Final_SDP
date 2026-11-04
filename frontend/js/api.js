// Shared API helpers for all pages.
//
// REST contract (person 3, Javalin):
//   GET  /menu                -> { meals: [{type, name, description, price}], toppings: [{id, name, label, price}] }
//   POST /order               -> body  { table, comment, payment, discount: {type, value}, items: [{type, toppings: [id...], qty}] }
//                                reply { id, status, total, finalTotal, discountName }
//   GET  /order/{id}/status   -> { id, status }
//   GET  /orders              -> [ order... ]                (kitchen panel)
//   POST /order/{id}/status   -> body { status } -> { id, status }   (kitchen moves the order forward)
//
// If the backend is not running, every call falls back to a local "mock server"
// in localStorage, so the whole flow menu -> cart -> status -> kitchen works
// in the browser (open kitchen.html and status.html in two tabs).

const API_BASE = '';
const STATUSES = ['NEW', 'COOKING', 'READY'];

const LOCAL_MENU = {
  meals: [
    { type: 'pizza',  name: 'Pizza',  description: 'Pizza Margherita', price: 2500 },
    { type: 'burger', name: 'Burger', description: 'Beef burger',      price: 1800 },
    { type: 'drink',  name: 'Drink',  description: 'Lemonade',         price: 600 }
  ],
  toppings: [
    { id: 'cheese', name: 'Extra cheese', label: 'extra cheese', price: 300 },
    { id: 'sauce',  name: 'Sauce',        label: 'sauce',        price: 150 },
    { id: 'large',  name: 'Large size',   label: 'large size',   price: 500 }
  ]
};

// ---------- discounts: mirror of the Strategy pattern in restaurant.payment ----------
const PERCENT_DISCOUNT = 10;     // new PercentDiscount(10)
const PROMO_CODES = { FIRST: 15 }; // PromoCodeDiscount: "FIRST" -> -15 %

const DISCOUNTS = {
  none: {
    name: () => 'No Discount',
    apply: total => total                                           // NoDiscount
  },
  percent: {
    name: () => PERCENT_DISCOUNT + '% discount',
    apply: total => total - total * PERCENT_DISCOUNT / 100          // PercentDiscount
  },
  promo: {
    name: code => 'Promo code: ' + (code || '').trim().toUpperCase(),
    apply: (total, code) => {                                       // PromoCodeDiscount
      const p = PROMO_CODES[(code || '').trim().toUpperCase()];
      return p ? total * (100 - p) / 100 : total;
    }
  }
};

function applyDiscount(total, discount) {
  const s = DISCOUNTS[discount.type] || DISCOUNTS.none;
  return { finalTotal: s.apply(total, discount.value), discountName: s.name(discount.value) };
}

function isPromoValid(code) {
  return !!PROMO_CODES[(code || '').trim().toUpperCase()];
}

// ---------- backend detection ----------
let backendOnline = null;

async function checkBackend() {
  if (backendOnline !== null) return backendOnline;
  try {
    const res = await fetch(API_BASE + '/menu');
    backendOnline = res.ok && (res.headers.get('content-type') || '').includes('json');
  } catch (e) {
    backendOnline = false;
  }
  return backendOnline;
}

async function http(method, url, body) {
  const res = await fetch(API_BASE + url, {
    method,
    headers: body ? { 'Content-Type': 'application/json' } : {},
    body: body ? JSON.stringify(body) : undefined
  });
  if (!res.ok) throw new Error((await res.text()) || 'HTTP ' + res.status);
  return res.json();
}

// ---------- localStorage helpers ----------
function readJSON(key, fallback) {
  try { return JSON.parse(localStorage.getItem(key)) ?? fallback; } catch (e) { return fallback; }
}
function writeJSON(key, value) {
  try { localStorage.setItem(key, JSON.stringify(value)); } catch (e) { /* storage unavailable */ }
}

// ---------- API ----------
async function getMenu() {
  if (await checkBackend()) {
    try { return { data: await http('GET', '/menu'), fromServer: true }; } catch (e) { /* fall back */ }
  }
  return { data: LOCAL_MENU, fromServer: false };
}

function priceOf(item) {
  const meal = LOCAL_MENU.meals.find(m => m.type === item.type);
  let price = meal ? meal.price : 0;
  for (const id of item.toppings) {
    const t = LOCAL_MENU.toppings.find(x => x.id === id);
    if (t) price += t.price;
  }
  return price;
}

async function placeOrder(request) {
  if (await checkBackend()) return http('POST', '/order', request);

  // mock: same rules as OrderBuilder.build()
  if (!request.items.length) throw new Error('Order needs at least one dish');
  if (!(request.table > 0)) throw new Error('Table number is required');

  const orders = readJSON('orders', []);
  const total = request.items.reduce((s, i) => s + priceOf(i) * i.qty, 0);
  const { finalTotal, discountName } = applyDiscount(total, request.discount);
  const order = {
    id: (orders.reduce((m, o) => Math.max(m, o.id), 0) || 0) + 1,
    table: request.table,
    comment: request.comment || '',
    payment: request.payment,
    items: request.items.map(i => ({ ...i, description: i.description, unitPrice: priceOf(i) })),
    total, finalTotal, discountName,
    status: 'NEW',
    createdAt: Date.now()
  };
  orders.push(order);
  writeJSON('orders', orders);
  return { id: order.id, status: order.status, total, finalTotal, discountName };
}

async function getOrderStatus(id) {
  if (await checkBackend()) return http('GET', '/order/' + id + '/status');
  const o = readJSON('orders', []).find(x => x.id === Number(id));
  if (!o) throw new Error('Order #' + id + ' not found');
  return { id: o.id, status: o.status };
}

async function getOrder(id) {
  if (await checkBackend()) {
    const all = await http('GET', '/orders');
    return all.find(x => x.id === Number(id)) || null;
  }
  return readJSON('orders', []).find(x => x.id === Number(id)) || null;
}

async function getOrders() {
  if (await checkBackend()) return http('GET', '/orders');
  return readJSON('orders', []);
}

async function setOrderStatus(id, status) {
  if (await checkBackend()) return http('POST', '/order/' + id + '/status', { status });
  // mock: same rule as Order.setStatus(): only one step forward
  const orders = readJSON('orders', []);
  const o = orders.find(x => x.id === Number(id));
  if (!o) throw new Error('Order #' + id + ' not found');
  if (STATUSES.indexOf(status) !== STATUSES.indexOf(o.status) + 1) {
    throw new Error('Order#' + id + ': ' + o.status + ' -> ' + status + ' is not allowed');
  }
  o.status = status;
  writeJSON('orders', orders);
  return { id: o.id, status: o.status };
}

function nextStatus(status) {
  return STATUSES[STATUSES.indexOf(status) + 1] || null;
}

// ---------- cart in localStorage ----------
// item: { type, name, description, toppings: [id...], unitPrice, qty }
const CART_KEY = 'cart';

function getCart()      { return readJSON(CART_KEY, []); }
function saveCart(cart) { writeJSON(CART_KEY, cart); }
function clearCart()    { saveCart([]); }

function cartKey(i) { return i.type + '|' + i.toppings.join(','); }

function addToCart(item) {
  const cart = getCart();
  const same = cart.find(i => cartKey(i) === cartKey(item));
  if (same) same.qty += item.qty;
  else cart.push(item);
  saveCart(cart);
  return cart;
}

function cartCount() {
  return getCart().reduce((n, i) => n + i.qty, 0);
}

function refreshBadge() {
  const el = document.getElementById('cart-count');
  if (el) el.textContent = cartCount();
}

function formatPrice(value) {
  return Math.round(value).toLocaleString('ru-RU') + ' ₸';
}

function escapeHtml(s) {
  return String(s).replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}

let toastTimer;
function showToast(text, isError) {
  const t = document.getElementById('toast');
  if (!t) return;
  t.textContent = text;
  t.classList.toggle('error', !!isError);
  t.classList.add('show');
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => t.classList.remove('show'), 2400);
}

async function showOfflineNote() {
  const el = document.getElementById('source-note');
  if (el && !(await checkBackend())) {
    el.textContent = 'Backend is offline: data is kept in this browser (demo mode).';
  }
}
