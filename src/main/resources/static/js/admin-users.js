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

  document.querySelectorAll('[data-role-user]').forEach(function(button) {
    button.addEventListener('click', function() {
      const form = document.getElementById('roleForm');
      form.action = '/admin/users/' + button.dataset.id + '/role';
      addCsrfToken(form);
      document.querySelector('#roleForm select[name="role"]').value = button.dataset.role;
      document.getElementById('roleModal').classList.add('active');
    });
  });

  document.querySelectorAll('[data-delete-user]').forEach(function(button) {
    button.addEventListener('click', function() {
      const form = document.getElementById('deleteForm');
      form.action = '/admin/users/' + button.dataset.id + '/delete';
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
