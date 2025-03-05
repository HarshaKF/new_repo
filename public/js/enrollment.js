document.addEventListener('DOMContentLoaded', function() {
    // Get all enrollment buttons
    const enrollButtons = document.querySelectorAll('.enroll-btn');
    const enrollmentForm = document.getElementById('enrollmentForm');
    const submitButton = document.getElementById('submitEnrollment');

    // Add click event listeners to all enroll buttons
    enrollButtons.forEach(button => {
        button.addEventListener('click', function() {
            // Get course details from data attributes
            const courseName = this.dataset.course;
            const courseFee = this.dataset.fee;
            const courseDuration = this.dataset.duration;
            const courseSchedule = this.dataset.schedule;

            // Update modal with course details
            document.getElementById('selectedCourse').textContent = courseName;
            document.getElementById('courseFee').textContent = courseFee;
            document.getElementById('courseDuration').textContent = courseDuration;
            document.getElementById('courseSchedule').textContent = courseSchedule;
        });
    });

    // Form validation and submission
    submitButton.addEventListener('click', function(event) {
        if (!enrollmentForm.checkValidity()) {
            event.preventDefault();
            event.stopPropagation();
            enrollmentForm.classList.add('was-validated');
            return;
        }

        // Gather form data
        const formData = {
            courseName: document.getElementById('selectedCourse').textContent,
            studentName: document.getElementById('studentName').value,
            studentEmail: document.getElementById('studentEmail').value,
            studentPhone: document.getElementById('studentPhone').value,
            studentDOB: document.getElementById('studentDOB').value,
            studentAddress: document.getElementById('studentAddress').value,
            previousSchool: document.getElementById('previousSchool').value,
            previousGrade: document.getElementById('previousGrade').value,
            courseFee: document.getElementById('courseFee').textContent,
            courseDuration: document.getElementById('courseDuration').textContent,
            courseSchedule: document.getElementById('courseSchedule').textContent
        };

        // Send enrollment data to server
        fetch('/api/enroll', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(formData)
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                // Show success message
                Swal.fire({
                    title: 'Enrollment Successful!',
                    text: 'Thank you for enrolling. We will contact you shortly.',
                    icon: 'success',
                    confirmButtonText: 'OK'
                }).then(() => {
                    // Reset form and close modal
                    enrollmentForm.reset();
                    enrollmentForm.classList.remove('was-validated');
                    bootstrap.Modal.getInstance(document.getElementById('enrollmentModal')).hide();
                });
            } else {
                throw new Error(data.message || 'Enrollment failed');
            }
        })
        .catch(error => {
            // Show error message
            Swal.fire({
                title: 'Enrollment Failed',
                text: error.message || 'Please try again later',
                icon: 'error',
                confirmButtonText: 'OK'
            });
        });
    });

    // Reset form when modal is closed
    document.getElementById('enrollmentModal').addEventListener('hidden.bs.modal', function () {
        enrollmentForm.reset();
        enrollmentForm.classList.remove('was-validated');
    });
});
