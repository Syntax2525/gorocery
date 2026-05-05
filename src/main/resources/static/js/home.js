document.addEventListener('DOMContentLoaded', function() {
  const searchInput = document.getElementById('searchInput');
  if (searchInput) {
    searchInput.addEventListener('input', function() {
      const query = this.value.toLowerCase();
      document.querySelectorAll('.product-card').forEach(function(card) {
        const name = card.querySelector('h3');
        if (name) {
          const match = name.textContent.toLowerCase().includes(query);
          card.style.display = match ? '' : 'none';
        }
      });
    });
  }

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
});
