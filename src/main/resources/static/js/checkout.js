document.addEventListener('DOMContentLoaded', function() {
  const districtSelect = document.querySelector('[data-district-select]');
  const subtotalElement = document.querySelector('[data-subtotal]');
  const deliveryFeeElement = document.querySelector('[data-delivery-fee]');
  const grandTotalElement = document.querySelector('[data-grand-total]');

  if (!districtSelect || !subtotalElement || !deliveryFeeElement || !grandTotalElement) {
    return;
  }

  const subtotal = Number(subtotalElement.dataset.subtotal || 0);

  function formatAmount(amount) {
    return new Intl.NumberFormat('en-US', { maximumFractionDigits: 0 }).format(amount);
  }

  function selectedFee() {
    const selectedOption = districtSelect.options[districtSelect.selectedIndex];
    return Number(selectedOption?.dataset.fee || 0);
  }

  function updateTotals() {
    const deliveryFee = selectedFee();
    deliveryFeeElement.textContent = formatAmount(deliveryFee);
    grandTotalElement.textContent = formatAmount(subtotal + deliveryFee);
  }

  districtSelect.addEventListener('change', updateTotals);
  updateTotals();
});
