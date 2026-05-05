document.addEventListener('DOMContentLoaded', function() {
  const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
  const allowedTypes = ['image/jpeg', 'image/png', 'image/webp'];
  const maxImageSize = 5 * 1024 * 1024;

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

  document.querySelectorAll('[data-edit-category]').forEach(function(button) {
    button.addEventListener('click', function() {
      const form = document.getElementById('editForm');
      form.action = '/admin/categories/' + button.dataset.id + '/update';
      addCsrfToken(form);
      document.getElementById('editName').value = button.dataset.name || '';
      document.getElementById('editDesc').value = button.dataset.desc || '';
      document.getElementById('editImage').value = button.dataset.image || '';
      resetUploadZone(document.getElementById('editModal'));
      document.getElementById('editModal').classList.add('active');
    });
  });

  document.querySelectorAll('[data-delete-category]').forEach(function(button) {
    button.addEventListener('click', function() {
      const form = document.getElementById('deleteForm');
      form.action = '/admin/categories/' + button.dataset.id + '/delete';
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

  document.querySelectorAll('[data-upload-zone]').forEach(initUploadZone);

  function initUploadZone(zone) {
    const input = zone.querySelector('[data-upload-input]');
    const browseButton = zone.parentElement.querySelector('[data-upload-browse]');
    const removeButton = zone.parentElement.querySelector('[data-upload-remove]');

    input.addEventListener('change', function() {
      handleSelectedFile(zone, input.files[0]);
    });

    browseButton?.addEventListener('click', function() {
      input.click();
    });

    removeButton?.addEventListener('click', function() {
      clearUpload(zone);
    });

    ['dragenter', 'dragover'].forEach(function(eventName) {
      zone.addEventListener(eventName, function(event) {
        event.preventDefault();
        zone.classList.add('is-dragging');
      });
    });

    ['dragleave', 'drop'].forEach(function(eventName) {
      zone.addEventListener(eventName, function(event) {
        event.preventDefault();
        zone.classList.remove('is-dragging');
      });
    });

    zone.addEventListener('drop', function(event) {
      const file = event.dataTransfer.files[0];
      if (!file) return;

      const transfer = new DataTransfer();
      transfer.items.add(file);
      input.files = transfer.files;
      handleSelectedFile(zone, file);
    });
  }

  function handleSelectedFile(zone, file) {
    const input = zone.querySelector('[data-upload-input]');
    const emptyState = zone.querySelector('[data-upload-empty]');
    const preview = zone.querySelector('[data-upload-preview]');
    const previewImage = zone.querySelector('[data-upload-preview-image]');
    const fileName = zone.querySelector('[data-upload-file-name]');
    const fileMeta = zone.querySelector('[data-upload-file-meta]');
    const error = zone.parentElement.querySelector('[data-upload-error]');
    const removeButton = zone.parentElement.querySelector('[data-upload-remove]');

    setUploadError(error, '');

    if (!file) {
      clearUpload(zone);
      return;
    }

    if (!allowedTypes.includes(file.type)) {
      clearUpload(zone);
      setUploadError(error, 'Use a JPG, JPEG, PNG, or WEBP image.');
      return;
    }

    if (file.size > maxImageSize) {
      clearUpload(zone);
      setUploadError(error, 'Category image must be 5 MB or smaller.');
      return;
    }

    const reader = new FileReader();
    reader.onload = function(event) {
      previewImage.src = event.target.result;
      fileName.textContent = file.name;
      fileMeta.textContent = formatBytes(file.size) + ' selected';
      emptyState.hidden = true;
      preview.hidden = false;
      removeButton.hidden = false;
    };
    reader.readAsDataURL(file);
  }

  function clearUpload(zone) {
    const input = zone.querySelector('[data-upload-input]');
    const emptyState = zone.querySelector('[data-upload-empty]');
    const preview = zone.querySelector('[data-upload-preview]');
    const previewImage = zone.querySelector('[data-upload-preview-image]');
    const error = zone.parentElement.querySelector('[data-upload-error]');
    const removeButton = zone.parentElement.querySelector('[data-upload-remove]');

    input.value = '';
    previewImage.removeAttribute('src');
    preview.hidden = true;
    emptyState.hidden = false;
    removeButton.hidden = true;
    setUploadError(error, '');
  }

  function resetUploadZone(scope) {
    scope.querySelectorAll('[data-upload-zone]').forEach(clearUpload);
  }

  function setUploadError(errorElement, message) {
    if (!errorElement) return;
    errorElement.textContent = message;
    errorElement.hidden = !message;
  }

  function formatBytes(bytes) {
    if (bytes < 1024 * 1024) {
      return Math.max(1, Math.round(bytes / 1024)) + ' KB';
    }
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  }
});
