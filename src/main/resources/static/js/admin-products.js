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

  document.querySelectorAll('[data-open-modal]').forEach(function(trigger) {
    trigger.addEventListener('click', function() {
      document.getElementById(trigger.dataset.openModal).classList.add('active');
    });
  });

  document.querySelectorAll('[data-close-modal]').forEach(function(trigger) {
    trigger.addEventListener('click', function() {
      document.getElementById(trigger.dataset.closeModal).classList.remove('active');
    });
  });

  document.querySelectorAll('[data-edit-product]').forEach(function(button) {
    button.addEventListener('click', function() {
      const form = document.getElementById('editForm');
      form.action = '/admin/products/' + button.dataset.id + '/update';
      addCsrfToken(form);
      document.getElementById('editName').value = button.dataset.name || '';
      document.getElementById('editDesc').value = button.dataset.desc || '';
      document.getElementById('editPrice').value = button.dataset.price || '';
      document.getElementById('editStock').value = button.dataset.stock || '';
      document.getElementById('editCategory').value = button.dataset.category || '';
      document.getElementById('editImage').value = button.dataset.image || '';
      document.getElementById('editModal').classList.add('active');
    });
  });

  document.querySelectorAll('[data-delete-product]').forEach(function(button) {
    button.addEventListener('click', function() {
      const form = document.getElementById('deleteForm');
      form.action = '/admin/products/' + button.dataset.id + '/delete';
      addCsrfToken(form);
      document.getElementById('deleteModal').classList.add('active');
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
