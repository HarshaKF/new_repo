$(document).ready(function() {
    $('#registerForm').on('submit', function(e) {
        e.preventDefault();
        const username = $('#username').val();
        const email = $('#email').val();
        const password = $('#password').val();
        const confirmPassword = $('#confirmPassword').val();
        const role = $('#role').val();
        const errorMessage = $('#errorMessage');

        // Clear previous error messages
        errorMessage.addClass('d-none').text('');

        // Validate inputs
        if (password !== confirmPassword) {
            errorMessage.removeClass('d-none').text('Passwords do not match');
            return;
        }

        fetch('/api/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ 
                username, 
                email, 
                password, 
                role 
            })
        })
        .then(response => {
            console.log('Response status:', response.status);
            console.log('Response headers:', response.headers);
            
            // Log full response for debugging
            return response.json().then(data => {
                if (!response.ok) {
                    throw new Error(data.message || 'Registration failed');
                }
                return data;
            });
        })
        .then(data => {
            if (data.success) {
                alert('Registration successful! Please login.');
                window.location.href = '/login.html';
            } else {
                throw new Error(data.message || 'Registration failed');
            }
        })
        .catch(error => {
            console.error('Full registration error:', {
                message: error.message,
                name: error.name,
                stack: error.stack
            });
            
            // Display more informative error message
            errorMessage
                .removeClass('d-none')
                .text(error.message || 'An unexpected error occurred during registration');
        });
    });
});