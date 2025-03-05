$(document).ready(function() {
    $('#loginForm').on('submit', function(e) {
        e.preventDefault();
        const username = $('#username').val();
        const password = $('#password').val();
        const errorMessage = $('#errorMessage');

        // Clear previous error messages
        errorMessage.addClass('d-none').text('');

        fetch('/api/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username, password }),
            credentials: 'include'
        })
        .then(response => {
            if (!response.ok) {
                return response.json().then(err => { throw err; });
            }
            return response.json();
        })
        .then(data => {
            if (data.success) {
                window.location.href = '/dashboard.html';
            } else {
                throw new Error(data.message || 'Login failed');
            }
        })
        .catch(error => {
            console.error('Login error:', error);
            errorMessage.removeClass('d-none').text(error.message || 'An unexpected error occurred');
        });
    });
});