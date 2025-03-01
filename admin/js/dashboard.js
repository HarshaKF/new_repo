$(document).ready(function() {
    // Initialize DataTable with error handling
    try {
        const enrollmentsTable = $('#enrollmentsTable').DataTable({
            ajax: {
                url: '/api/enrollments',
                dataSrc: '',
                error: function(xhr, error, thrown) {
                    console.error('Error loading enrollments:', error);
                    showAlert('Error loading enrollments. Please try again later.', 'danger');
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
                },
                {
                    data: 'id',
                    render: function(data, type, row) {
                        return `
                            <div class="btn-group" role="group">
                                <button class="btn btn-sm btn-primary" onclick="viewDetails(${data})" title="View Details">
                                    <i class="bi bi-eye"></i>
                                </button>
                                <button class="btn btn-sm btn-success" onclick="markAttendance(${data})" title="Mark Attendance">
                                    <i class="bi bi-check-circle"></i>
                                </button>
                            </div>
                        `;
                    }
                }
            ],
            order: [[2, 'desc']],
            responsive: true,
            pageLength: 10,
            language: {
                emptyTable: "No enrollments found"
            }
        });

        // Refresh table every 5 minutes
        setInterval(() => {
            enrollmentsTable.ajax.reload(null, false);
        }, 300000);

    } catch (error) {
        console.error('Error initializing DataTable:', error);
        showAlert('Error initializing the enrollments table.', 'danger');
    }

    // Load Dashboard Stats
    loadDashboardStats();

    // Initialize Attendance Chart
    initializeAttendanceChart();
});

function loadDashboardStats() {
    fetch('/api/dashboard/stats')
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(data => {
            $('#totalStudents').text(data.totalStudents);
            $('#todayAttendance').text(data.todayAttendance + '%');
            $('#activeCourses').text(data.activeCourses);
            $('#newEnrollments').text(data.newEnrollments);
        })
        .catch(error => {
            console.error('Error loading dashboard stats:', error);
            showAlert('Error loading dashboard statistics.', 'danger');
        });
}

function initializeAttendanceChart() {
    fetch('/api/dashboard/attendance-chart')
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(data => {
            const ctx = document.getElementById('attendanceChart').getContext('2d');
            new Chart(ctx, {
                type: 'line',
                data: {
                    labels: data.labels,
                    datasets: [{
                        label: 'Daily Attendance %',
                        data: data.values,
                        borderColor: 'rgb(75, 192, 192)',
                        backgroundColor: 'rgba(75, 192, 192, 0.2)',
                        tension: 0.1,
                        fill: true
                    }]
                },
                options: {
                    responsive: true,
                    plugins: {
                        legend: {
                            position: 'top',
                        },
                        title: {
                            display: true,
                            text: 'Attendance Trend'
                        },
                        tooltip: {
                            mode: 'index',
                            intersect: false
                        }
                    },
                    scales: {
                        y: {
                            beginAtZero: true,
                            max: 100,
                            title: {
                                display: true,
                                text: 'Attendance %'
                            }
                        },
                        x: {
                            title: {
                                display: true,
                                text: 'Date'
                            }
                        }
                    }
                }
            });
        })
        .catch(error => {
            console.error('Error loading attendance chart:', error);
            showAlert('Error loading attendance chart.', 'danger');
        });
}

function viewDetails(enrollmentId) {
    window.location.href = `/admin/enrollment-details.html?id=${enrollmentId}`;
}

function markAttendance(enrollmentId) {
    $('#currentEnrollmentId').val(enrollmentId);
    $('#attendanceStatus').val('PRESENT');
    $('#attendanceNotes').val('');
    $('#attendanceModal').modal('show');
}

function saveAttendance() {
    const data = {
        enrollmentId: $('#currentEnrollmentId').val(),
        status: $('#attendanceStatus').val(),
        notes: $('#attendanceNotes').val(),
        date: new Date().toISOString()
    };

    fetch('/api/attendance', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        return response.json();
    })
    .then(result => {
        $('#attendanceModal').modal('hide');
        showAlert('Attendance marked successfully!', 'success');
        loadDashboardStats();
        $('#enrollmentsTable').DataTable().ajax.reload();
    })
    .catch(error => {
        console.error('Error saving attendance:', error);
        showAlert('Error saving attendance. Please try again.', 'danger');
    });
}

function showAlert(message, type = 'info') {
    const alertHtml = `
        <div class="alert alert-${type} alert-dismissible fade show" role="alert">
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    `;
    const alertPlaceholder = $('#alertPlaceholder');
    if (alertPlaceholder.length === 0) {
        $('main').prepend('<div id="alertPlaceholder"></div>');
    }
    $('#alertPlaceholder').html(alertHtml);
    setTimeout(() => {
        $('.alert').alert('close');
    }, 5000);
}

// Add event listener for attendance modal close
$('#attendanceModal').on('hidden.bs.modal', function () {
    $('#attendanceStatus').val('PRESENT');
    $('#attendanceNotes').val('');
});

// Contact Submissions Management
function loadContactSubmissions() {
    fetch('/api/contact-submissions')
        .then(response => response.json())
        .then(data => {
            const tbody = document.querySelector('#contactTable tbody');
            tbody.innerHTML = '';
            let unreadCount = 0;

            data.forEach(submission => {
                if (!submission.isRead) unreadCount++;
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>${new Date(submission.submissionTime).toLocaleString()}</td>
                    <td>${submission.name}</td>
                    <td><a href="mailto:${submission.email}">${submission.email}</a></td>
                    <td>${submission.message.length > 50 ? submission.message.substring(0, 50) + '...' : submission.message}</td>
                    <td>
                        <span class="badge ${submission.isRead ? 'bg-success' : 'bg-danger'}">
                            ${submission.isRead ? 'Read' : 'Unread'}
                        </span>
                    </td>
                    <td>
                        <button class="btn btn-sm btn-primary" onclick="viewSubmission(${submission.id})">
                            View
                        </button>
                        <button class="btn btn-sm ${submission.isRead ? 'btn-secondary' : 'btn-success'}" 
                                onclick="markAsRead(${submission.id})">
                            ${submission.isRead ? 'Mark Unread' : 'Mark Read'}
                        </button>
                    </td>
                `;
                tbody.appendChild(tr);
            });

            updateUnreadCount(unreadCount);
        })
        .catch(error => console.error('Error loading contact submissions:', error));
}

function updateUnreadCount(count) {
    const badge = document.getElementById('unread-count');
    if (count > 0) {
        badge.textContent = count;
        badge.style.display = 'inline';
    } else {
        badge.style.display = 'none';
    }
}

function viewSubmission(id) {
    fetch(`/api/contact-submissions/${id}`)
        .then(response => response.json())
        .then(submission => {
            // Create a modal to show full message
            const modal = new bootstrap.Modal(document.getElementById('submissionModal') || createSubmissionModal());
            document.getElementById('submissionModalLabel').textContent = `Message from ${submission.name}`;
            document.getElementById('submissionModalBody').innerHTML = `
                <p><strong>From:</strong> ${submission.name} (${submission.email})</p>
                <p><strong>Date:</strong> ${new Date(submission.submissionTime).toLocaleString()}</p>
                <p><strong>Message:</strong></p>
                <p>${submission.message}</p>
            `;
            modal.show();
        })
        .catch(error => console.error('Error viewing submission:', error));
}

function createSubmissionModal() {
    const modalDiv = document.createElement('div');
    modalDiv.className = 'modal fade';
    modalDiv.id = 'submissionModal';
    modalDiv.innerHTML = `
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="submissionModalLabel"></h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body" id="submissionModalBody">
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
                </div>
            </div>
        </div>
    `;
    document.body.appendChild(modalDiv);
    return modalDiv;
}

function markAsRead(id) {
    fetch(`/api/contact-submissions/${id}/toggle-read`, {
        method: 'POST'
    })
    .then(response => response.json())
    .then(() => loadContactSubmissions())
    .catch(error => console.error('Error marking submission:', error));
}

function markAllAsRead() {
    fetch('/api/contact-submissions/mark-all-read', {
        method: 'POST'
    })
    .then(response => response.json())
    .then(() => loadContactSubmissions())
    .catch(error => console.error('Error marking all submissions:', error));
    alert("All messages marked as read!");
}

// Load contact submissions when page loads
document.addEventListener('DOMContentLoaded', function() {
    loadContactSubmissions();
    // Refresh contact submissions every 5 minutes
    setInterval(loadContactSubmissions, 300000);
});