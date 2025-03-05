$(document).ready(function() {
    const loginButton = $('#loginButton');
    const logoutButton = $('#logoutButton');

    // Check login status on page load
    checkLoginStatus();

    // Login button click handler
    loginButton.on('click', function() {
        window.location.href = '/login.html';
    });

    // Logout button click handler
    logoutButton.on('click', function() {
        fetch('/api/logout', {
            method: 'POST',
            credentials: 'include'
        })
        .then(response => {
            if (response.ok) {
                // Hide logout, show login button
                logoutButton.addClass('d-none');
                loginButton.removeClass('d-none');
                
                // Redirect to login page
                window.location.href = '/login.html';
            } else {
                throw new Error('Logout failed');
            }
        })
        .catch(error => {
            console.error('Logout error:', error);
            alert('Logout failed. Please try again.');
        });
    });

    function checkLoginStatus() {
        fetch('/api/check-login', {
            credentials: 'include'
        })
        .then(response => response.json())
        .then(data => {
            if (data.isLoggedIn) {
                // User is logged in
                loginButton.addClass('d-none');
                logoutButton.removeClass('d-none');
                
                // Load dashboard data
                loadDashboardStats();
                loadAttendanceChart();
                loadEnrollmentsTable();
            } else {
                // User is not logged in
                loginButton.removeClass('d-none');
                logoutButton.addClass('d-none');
            }
        })
        .catch(error => {
            console.error('Login status check failed:', error);
            // Default to login button if check fails
            loginButton.removeClass('d-none');
            logoutButton.addClass('d-none');
        });
    }

    function loadDashboardStats() {
        fetch('/api/dashboard/stats', {
            credentials: 'include'
        })
        .then(response => response.json())
        .then(data => {
            $('#totalStudents').text(data.totalStudents);
            $('#activeCourses').text(data.activeCourses);
            $('#newEnrollments').text(data.newEnrollments);
            $('#todayAttendance').text(data.todayAttendance.toFixed(2));
        })
        .catch(error => {
            console.error('Failed to load dashboard stats:', error);
        });
    }

    function loadAttendanceChart() {
        fetch('/api/dashboard/attendance-chart', {
            credentials: 'include'
        })
        .then(response => response.json())
        .then(data => {
            const ctx = document.getElementById('attendanceChart').getContext('2d');
            new Chart(ctx, {
                type: 'line',
                data: {
                    labels: data.labels,
                    datasets: [{
                        label: 'Attendance Percentage',
                        data: data.values,
                        borderColor: 'rgb(75, 192, 192)',
                        tension: 0.1
                    }]
                },
                options: {
                    responsive: true,
                    scales: {
                        y: {
                            beginAtZero: true,
                            max: 100
                        }
                    }
                }
            });
        })
        .catch(error => {
            console.error('Failed to load attendance chart:', error);
        });
    }

    function loadEnrollmentsTable() {
        $('#enrollmentsTable').DataTable({
            ajax: {
                url: '/api/enrollments',
                dataSrc: '',
                headers: {
                    'credentials': 'include'
                }
            },
            columns: [
                { data: 'studentName' },
                { data: 'courseName' },
                { 
                    data: 'enrollmentDate',
                    render: function(data) {
                        return new Date(data).toLocaleDateString();
                    }
                },
                { 
                    data: 'feeStatus',
                    render: function(data) {
                        const colors = {
                            'PAID': 'success',
                            'PENDING': 'warning',
                            'OVERDUE': 'danger'
                        };
                        return `<span class="badge bg-${colors[data] || 'secondary'}">${data}</span>`;
                    }
                }
            ]
        });
    }
});