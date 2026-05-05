document.addEventListener('DOMContentLoaded', function() {
  const flashMsgs = document.querySelectorAll('.flash-msg');
  if (flashMsgs.length) {
    setTimeout(function() {
      flashMsgs.forEach(function(msg) {
        msg.style.transition = 'opacity 0.5s';
        msg.style.opacity = '0';
        setTimeout(function() { msg.remove(); }, 500);
      });
    }, 4000);
  }

  const passwordForm = document.querySelector('.password-form');
  if (passwordForm) {
    passwordForm.addEventListener('submit', function(e) {
      const inputs = this.querySelectorAll('input[type="password"]');
      if (inputs[1].value !== inputs[2].value) {
        e.preventDefault();
        alert('New passwords do not match');
      }
      if (inputs[1].value.length < 6) {
        e.preventDefault();
        alert('Password must be at least 6 characters');
      }
    });
  }

  const profileForm = document.querySelector('.profile-form');
  if (profileForm) {
    profileForm.addEventListener('submit', function(e) {
      const phone = this.querySelector('input[name="phoneNumber"]');
      if (phone && phone.value) {
        const phoneRegex = /^[0-9]{9}$/;
        if (!phoneRegex.test(phone.value.replace(/\s/g, ''))) {
          e.preventDefault();
          alert('Please enter a valid Tanzanian phone number (9 digits)');
        }
      }
    });
  }
});
