// PicknCart JavaScript

// Get CSRF token from meta tag or hidden input
function getCsrfToken() {
    const metaTag = document.querySelector('meta[name="_csrf"]');
    if (metaTag) {
        return metaTag.getAttribute('content');
    }
    const csrfInput = document.querySelector('input[name="_csrf"]');
    if (csrfInput) {
        return csrfInput.value;
    }
    return null;
}

// Form validation
document.addEventListener('DOMContentLoaded', function() {
    // Registration form password confirmation
    const registerForm = document.querySelector('form[action*="/register"]');
    if (registerForm) {
        registerForm.addEventListener('submit', function(e) {
            const password = document.getElementById('password');
            const confirmPassword = document.getElementById('confirmPassword');
            
            if (password && confirmPassword && password.value !== confirmPassword.value) {
                e.preventDefault();
                alert('Passwords do not match!');
                return false;
            }
        });
    }

    // Auto-hide alerts after 5 seconds
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(function(alert) {
        setTimeout(function() {
            alert.style.transition = 'opacity 0.5s';
            alert.style.opacity = '0';
            setTimeout(function() {
                alert.remove();
            }, 500);
        }, 5000);
    });

    // Add to cart functionality (if needed for AJAX)
    const addToCartButtons = document.querySelectorAll('.add-to-cart');
    addToCartButtons.forEach(function(button) {
        button.addEventListener('click', function(e) {
            // This can be extended for AJAX cart updates
            console.log('Add to cart clicked');
        });
    });
});

// Utility functions
function formatCurrency(amount) {
    return new Intl.NumberFormat('en-TZ', {
        style: 'currency',
        currency: 'TZS'
    }).format(amount);
}

// Cart operations (for future AJAX implementation)
function addToCart(itemId, quantity = 1) {
    const csrfToken = getCsrfToken();
    const headers = {
        'Content-Type': 'application/json',
    };
    if (csrfToken) {
        headers['X-CSRF-TOKEN'] = csrfToken;
    }

    fetch('/api/cart', {
        method: 'POST',
        headers: headers,
        body: JSON.stringify({
            itemId: itemId,
            quantity: quantity
        })
    })
    .then(response => response.json())
    .then(data => {
        console.log('Item added to cart:', data);
        // Update cart UI or show success message
        alert('Item added to cart!');
        window.location.reload();
    })
    .catch(error => {
        console.error('Error adding to cart:', error);
        alert('Error adding item to cart. Please try again.');
    });
}

function removeFromCart(cartId) {
    const csrfToken = getCsrfToken();
    const headers = {};
    if (csrfToken) {
        headers['X-CSRF-TOKEN'] = csrfToken;
    }

    fetch(`/api/cart/${cartId}`, {
        method: 'DELETE',
        headers: headers
    })
    .then(response => {
        if (response.ok) {
            // Reload page or update UI
            window.location.reload();
        } else {
            alert('Error removing item from cart. Please try again.');
        }
    })
    .catch(error => {
        console.error('Error removing from cart:', error);
        alert('Error removing item from cart. Please try again.');
    });
}
