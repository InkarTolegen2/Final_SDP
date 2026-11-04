// Cart + checkout page: cart from localStorage, discount (Strategy) and payment, POST /order.

const $ = id => document.getElementById(id);

function renderCart() {
  const cart = getCart();
  refreshBadge();
  $('empty').hidden = cart.length > 0;
  $('cart-layout').hidden = cart.length === 0;
  if (!cart.length) return;

  $('cart-items').innerHTML = cart.map((i, idx) => `
    <li class="cart-item">
      <div class="ci-main">
        <strong>${escapeHtml(i.name)}</strong>
        <span class="ci-desc">${escapeHtml(i.description)}</span>
        <span class="ci-unit">${formatPrice(i.unitPrice)} each</span>
      </div>
      <div class="qty">
        <button type="button" class="qty-btn" data-idx="${idx}" data-d="-1" aria-label="Less">−</button>
        <span class="qty-value">${i.qty}</span>
        <button type="button" class="qty-btn" data-idx="${idx}" data-d="1" aria-label="More">+</button>
      </div>
      <span class="ci-sum">${formatPrice(i.unitPrice * i.qty)}</span>
      <button type="button" class="remove" data-idx="${idx}" aria-label="Remove">×</button>
    </li>`).join('');

  updateSummary();
}

function currentDiscount() {
  const type = document.querySelector('input[name="discount"]:checked').value;
  return { type, value: type === 'promo' ? $('promo').value.trim() : type === 'percent' ? PERCENT_DISCOUNT : null };
}

function updateSummary() {
  const subtotal = getCart().reduce((s, i) => s + i.unitPrice * i.qty, 0);
  const d = currentDiscount();
  const { finalTotal, discountName } = applyDiscount(subtotal, d);

  $('promo-box').hidden = d.type !== 'promo';
  if (d.type === 'promo') {
    const code = $('promo').value.trim();
    const msg = $('promo-msg');
    msg.textContent = !code ? '' : isPromoValid(code) ? '−' + PROMO_CODES[code.toUpperCase()] + '% applied' : 'Code not valid';
    msg.className = 'promo-msg ' + (!code ? '' : isPromoValid(code) ? 'ok' : 'bad');
  }

  $('subtotal').textContent = formatPrice(subtotal);
  $('discount-name').textContent = d.type === 'none' ? 'Discount' : discountName;
  const off = subtotal - Math.round(finalTotal);
  $('discount-amount').textContent = off > 0 ? '−' + formatPrice(off) : '0 ₸';
  $('total').textContent = formatPrice(finalTotal);
}

function changeQty(idx, delta) {
  const cart = getCart();
  cart[idx].qty += delta;
  if (cart[idx].qty <= 0) cart.splice(idx, 1);
  saveCart(cart);
  renderCart();
}

async function submitOrder(e) {
  e.preventDefault();
  const err = $('form-error');
  err.textContent = '';
  const table = Number($('table').value);
  if (!(table >= 1 && table <= 99)) {
    err.textContent = 'Enter a table number from 1 to 99.';
    $('table').focus();
    return;
  }
  const d = currentDiscount();
  if (d.type === 'promo' && !isPromoValid(d.value)) {
    err.textContent = 'Promo code is not valid. Choose another discount or fix the code.';
    $('promo').focus();
    return;
  }

  const request = {
    table,
    comment: $('comment').value.trim(),
    payment: document.querySelector('input[name="payment"]:checked').value,
    discount: d,
    items: getCart().map(i => ({ type: i.type, toppings: i.toppings, qty: i.qty, description: i.description }))
  };

  $('place').disabled = true;
  try {
    const res = await placeOrder(request);
    clearCart();
    try { localStorage.setItem('lastOrderId', String(res.id)); } catch (e2) { /* ignore */ }
    location.href = 'status.html?id=' + res.id;
  } catch (ex) {
    err.textContent = ex.message;
    $('place').disabled = false;
  }
}

$('percent-label').textContent = '−' + PERCENT_DISCOUNT + '%';
$('cart-items').addEventListener('click', e => {
  const b = e.target.closest('button');
  if (!b) return;
  const idx = Number(b.dataset.idx);
  if (b.classList.contains('remove')) changeQty(idx, -Infinity);
  else if (b.classList.contains('qty-btn')) changeQty(idx, Number(b.dataset.d));
});
$('clear-cart').addEventListener('click', () => { clearCart(); renderCart(); });
document.querySelectorAll('input[name="discount"]').forEach(r => r.addEventListener('change', updateSummary));
$('promo').addEventListener('input', updateSummary);
$('checkout').addEventListener('input', () => { $('form-error').textContent = ''; });
$('checkout').addEventListener('submit', submitOrder);

renderCart();
showOfflineNote();
