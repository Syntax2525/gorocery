document.addEventListener('DOMContentLoaded', function() {
  const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

  function addCsrfToken(form) {
    if (!csrfToken || !csrfHeader) return;
    let existingToken = form.querySelector('input[name="_csrf"]');
    if (!existingToken) {
      existingToken = document.createElement('input');
      existingToken.type = 'hidden';
      existingToken.name = '_csrf';
      form.appendChild(existingToken);
    }
    existingToken.value = csrfToken;
  }

  document.querySelectorAll('[data-close-modal]').forEach(function(trigger) {
    trigger.addEventListener('click', function() {
      document.getElementById(trigger.dataset.closeModal).classList.remove('active');
    });
  });

  document.querySelectorAll('[data-order-detail]').forEach(function(button) {
    button.addEventListener('click', function() {
      document.getElementById('modalTitle').textContent = 'Order #PC-' + button.dataset.id;
      document.getElementById('modalUser').textContent = button.dataset.user || 'N/A';
      document.getElementById('modalEmail').textContent = button.dataset.email || '';
      document.getElementById('modalPhone').textContent = button.dataset.phone ? '+' + button.dataset.phone : '';
      document.getElementById('modalAddress').textContent = button.dataset.address || 'Not provided';
      document.getElementById('modalAddressBlock').classList.toggle('hidden-block', !button.dataset.address);
      document.getElementById('modalPayment').textContent = button.dataset.payment || 'N/A';
      document.getElementById('modalTotal').textContent = 'TSh ' + (button.dataset.total || '0');
      document.getElementById('detailModal').classList.add('active');
    });
  });

  document.querySelectorAll('.modal-overlay').forEach(function(modal) {
    modal.addEventListener('click', function(event) {
      if (event.target === modal) {
        modal.classList.remove('active');
      }
    });
  });
});
