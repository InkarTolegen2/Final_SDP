// Kitchen panel (client side of KitchenListener): shows all orders and moves them forward.

const POLL_MS = 3000;
const ACTION = { NEW: 'Start cooking', COOKING: 'Mark ready' };

function minutesAgo(ts) {
  if (!ts) return '';
  const m = Math.floor((Date.now() - ts) / 60000);
  return m < 1 ? 'just now' : m + ' min ago';
}

function ticket(o) {
  const next = nextStatus(o.status);
  return `
    <article class="ticket ${o.status.toLowerCase()}">
      <header>
        <strong>#${o.id}</strong>
        <span>Table ${o.table}</span>
        <time>${minutesAgo(o.createdAt)}</time>
      </header>
      <ul>${o.items.map(i => `<li><b>${i.qty}×</b> ${escapeHtml(i.description || i.type)}</li>`).join('')}</ul>
      ${o.comment ? `<p class="ticket-comment">“${escapeHtml(o.comment)}”</p>` : ''}
      ${next ? `<button type="button" class="btn small" data-id="${o.id}" data-next="${next}">${ACTION[o.status]}</button>` : ''}
    </article>`;
}

async function render() {
  let orders;
  try { orders = await getOrders(); } catch (e) { showToast(e.message, true); return; }
  for (const s of STATUSES) {
    let list = orders.filter(o => o.status === s);
    if (s === 'READY') list = list.slice(-6).reverse(); // only the latest ready orders
    document.getElementById('col-' + s).innerHTML =
      list.map(ticket).join('') || '<p class="col-empty">No orders</p>';
    document.getElementById('count-' + s).textContent = orders.filter(o => o.status === s).length;
  }
}

document.querySelector('.board').addEventListener('click', async e => {
  const b = e.target.closest('button[data-next]');
  if (!b) return;
  b.disabled = true;
  try {
    await setOrderStatus(Number(b.dataset.id), b.dataset.next);
    showToast('Order #' + b.dataset.id + ' → ' + b.dataset.next);
  } catch (ex) {
    showToast(ex.message, true);
  }
  render();
});

refreshBadge();
showOfflineNote();
render();
setInterval(render, POLL_MS);
