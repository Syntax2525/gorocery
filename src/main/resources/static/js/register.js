document.addEventListener('DOMContentLoaded', function() {
  const form = document.getElementById('registerForm');
  if (!form) {
    return;
  }

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

  form.addEventListener('submit', function(event) {
    const fullName = form.querySelector('input[name="fullName"]');
    const email = form.querySelector('input[name="email"]');
    const phone = form.querySelector('input[name="phoneNumber"]');
    const password = form.querySelector('input[name="password"]');
    const confirmPassword = form.querySelector('input[name="confirmPassword"]');
    const terms = form.querySelector('input[type="checkbox"]');

    const errors = [];

    if (fullName && fullName.value.trim().split(/\s+/).length < 2) {
      errors.push('Please enter your first and last name.');
    }

    if (email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value.trim())) {
      errors.push('Please enter a valid email address.');
    }

    if (phone) {
      const digits = phone.value.replace(/\D/g, '');
      if (digits.length < 9 || digits.length > 12) {
        errors.push('Please enter a valid phone number.');
      }
    }

    if (password && password.value.length < 6) {
      errors.push('Password must be at least 6 characters.');
    }

    if (password && confirmPassword && password.value !== confirmPassword.value) {
      errors.push('Passwords do not match.');
    }

    if (terms && !terms.checked) {
      errors.push('Please accept the terms and conditions.');
    }

    if (errors.length) {
      event.preventDefault();
      alert(errors.join('\n'));
    }
  });
});
