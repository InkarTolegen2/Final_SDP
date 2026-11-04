// Order status page (client side of CustomerListener): polls GET /order/{id}/status every 2 s.

const $ = id => document.getElementById(id);
const POLL_MS = 2000;
const TEXT = {
  NEW: 'Your order is accepted and waiting for the kitchen.',
  COOKING: 'Your order is being cooked.',
  READY: 'Your order is ready! A waiter will bring it to your table.'
};
let timer = null;
let lastStatus = null;

function orderId() {
  const fromUrl = new URLSearchParams(location.search).get('id');
  if (fromUrl) return Number(fromUrl);
  try { return Number(localStorage.getItem('lastOrderId')) || null; } catch (e) { return null; }
}

function renderSteps(status) {
  const idx = STATUSES.indexOf(status);
  document.querySelectorAll('#steps li').forEach((li, i) => {
    li.classList.toggle('done', i < idx || (i === idx && status === 'READY'));
    li.classList.toggle('current', i === idx && status !== 'READY');
  });
  $('status-text').textContent = TEXT[status] || '';
  $('status-text').className = 'status-text ' + status.toLowerCase();
}

function renderOrder(o) {
  $('title').textContent = 'Order #' + o.id;
  $('subtitle').textContent = 'Table ' + o.table + (o.comment ? ' · "' + o.comment + '"' : '') +
    ' · updates every 2 seconds';
  $('order-items').innerHTML = o.items.map(i => `
    <li class="cart-item">
      <div class="ci-main"><strong>${i.qty} × ${escapeHtml(i.description || i.type)}</strong></div>
      <span class="ci-sum">${i.unitPrice ? formatPrice(i.unitPrice * i.qty) : ''}</span>
    </li>`).join('');
  $('order-summary').innerHTML = `
    <div><dt>Subtotal</dt><dd>${formatPrice(o.total)}</dd></div>
    <div><dt>${escapeHtml(o.discountName || 'Discount')}</dt><dd>${o.total - Math.round(o.finalTotal) > 0 ? '−' + formatPrice(o.total - Math.round(o.finalTotal)) : '0 ₸'}</dd></div>
    <div><dt>Payment</dt><dd>${o.payment === 'cash' ? 'Cash' : 'Card'}</dd></div>
    <div class="grand"><dt>Total</dt><dd>${formatPrice(o.finalTotal)}</dd></div>`;
}

async function poll(id) {
  try {
    const { status } = await getOrderStatus(id);
    if (status !== lastStatus) {
      if (lastStatus) showToast('Order #' + id + ' is now ' + status);
      lastStatus = status;
      renderSteps(status);
    }
    if (status === 'READY') { clearInterval(timer); timer = null; }
  } catch (e) {
    $('status-text').textContent = e.message;
  }
}

async function renderOthers(current) {
  const all = await getOrders().catch(() => []);
  const others = all.filter(o => o.id !== current).slice(-8).reverse();
  $('other-orders').hidden = others.length === 0;
  $('order-links').innerHTML = others.map(o =>
    `<a class="chip" href="status.html?id=${o.id}">#${o.id} · table ${o.table} · ${o.status}</a>`).join('');
}

async function init() {
  refreshBadge();
  showOfflineNote();
  const id = orderId();
  const order = id ? await getOrder(id).catch(() => null) : null;
  if (!order) {
    $('no-order').hidden = false;
    $('subtitle').textContent = id ? 'Order #' + id + ' was not found.' : 'Place an order from the cart first.';
    renderOthers(null);
    return;
  }
  $('order-box').hidden = false;
  renderOrder(order);
  renderOthers(order.id);
  await poll(order.id);
  if (lastStatus !== 'READY') timer = setInterval(() => poll(order.id), POLL_MS);
}

init();
