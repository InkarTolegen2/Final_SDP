// Menu page (person 1): dish cards + toppings with live price recalculation.
// The price logic mirrors the Decorator on the backend:
// every selected topping wraps the meal and adds its own price and description.

let TOPPINGS = [];

function decorate(meal, toppingIds) {
  // start with the plain meal (what MealFactory.create(type) returns)
  let price = meal.price;
  let description = meal.description;
  for (const id of toppingIds) {
    const t = TOPPINGS.find(x => x.id === id);
    if (!t) continue;
    price += t.price;                                           // like getPrice() of a decorator
    description += ', ' + (t.label || t.name.toLowerCase());    // like getDescription()
  }
  return { price, description };
}

function selectedToppings(card) {
  return [...card.querySelectorAll('input[name="topping"]:checked')].map(i => i.value);
}

function updateCard(card, meal) {
  const ids = selectedToppings(card);
  const qty = Number(card.querySelector('.qty-value').textContent);
  const d = decorate(meal, ids);
  card.querySelector('.desc').textContent = d.description;
  card.querySelector('.price').textContent = formatPrice(d.price);
  card.querySelector('.total').textContent = qty > 1 ? qty + ' × ' + formatPrice(d.price) + ' = ' + formatPrice(d.price * qty) : '';
}

function renderCard(meal) {
  const card = document.createElement('article');
  card.className = 'card';
  card.dataset.type = meal.type;
  card.innerHTML = `
    <div class="card-head">
      <span class="emoji" aria-hidden="true">${{ pizza: '🍕', burger: '🍔', drink: '🥤' }[meal.type] || '🍽️'}</span>
      <div>
        <h2>${meal.name}</h2>
        <p class="desc"></p>
      </div>
    </div>
    <fieldset class="toppings">
      <legend>Toppings</legend>
      ${TOPPINGS.map(t => `
        <label class="topping">
          <input type="checkbox" name="topping" value="${t.id}">
          <span>${t.name}</span>
          <span class="plus">+${formatPrice(t.price)}</span>
        </label>`).join('')}
    </fieldset>
    <div class="card-foot">
      <div class="qty">
        <button type="button" class="qty-btn" data-d="-1" aria-label="Less">−</button>
        <span class="qty-value">1</span>
        <button type="button" class="qty-btn" data-d="1" aria-label="More">+</button>
      </div>
      <div class="price-box">
        <span class="price"></span>
        <span class="total"></span>
      </div>
    </div>
    <button type="button" class="btn add">Add to cart</button>
  `;

  card.querySelectorAll('input[name="topping"]').forEach(cb =>
    cb.addEventListener('change', () => updateCard(card, meal)));

  card.querySelectorAll('.qty-btn').forEach(btn =>
    btn.addEventListener('click', () => {
      const el = card.querySelector('.qty-value');
      el.textContent = Math.max(1, Math.min(20, Number(el.textContent) + Number(btn.dataset.d)));
      updateCard(card, meal);
    }));

  card.querySelector('.add').addEventListener('click', () => {
    const ids = selectedToppings(card);
    const qty = Number(card.querySelector('.qty-value').textContent);
    const d = decorate(meal, ids);
    addToCart({ type: meal.type, name: meal.name, description: d.description, toppings: ids, unitPrice: d.price, qty });
    refreshBadge();
    showToast(qty + ' × ' + d.description + ' added');
    // reset card
    card.querySelectorAll('input[name="topping"]').forEach(cb => (cb.checked = false));
    card.querySelector('.qty-value').textContent = '1';
    updateCard(card, meal);
  });

  updateCard(card, meal);
  return card;
}

async function init() {
  const { data, fromServer } = await getMenu();
  TOPPINGS = data.toppings;
  document.getElementById('source-note').textContent =
    fromServer ? '' : 'Backend is offline: showing the local menu.';
  const grid = document.getElementById('menu');
  grid.innerHTML = '';
  data.meals.forEach(m => grid.appendChild(renderCard(m)));
  refreshBadge();
}

init();
