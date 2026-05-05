document.addEventListener('DOMContentLoaded', function() {
  document.querySelectorAll('[data-password-toggle]').forEach(function(button) {
    const input = document.getElementById(button.dataset.passwordToggle);
    const icon = button.querySelector('.material-symbols-outlined');
    if (!input || !icon) {
      return;
    }

    button.addEventListener('click', function() {
      const shouldShow = input.type === 'password';
      input.type = shouldShow ? 'text' : 'password';
      icon.textContent = shouldShow ? 'visibility_off' : 'visibility';
      button.setAttribute('aria-label', shouldShow ? 'Hide password' : 'Show password');
    });
  });
});
