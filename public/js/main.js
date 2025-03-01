// Sample course data
const courses = [
    {
        title: 'Mathematics',
        description: 'Advanced mathematics for grades 9-12',
        duration: '6 months',
        image: '/images/math.jpg'
    },
    {
        title: 'Physics',
        description: 'Comprehensive physics for competitive exams',
        duration: '6 months',
        image: '/images/physics.jpg'
    },
    {
        title: 'Chemistry',
        description: 'Complete chemistry course for JEE/NEET',
        duration: '6 months',
        image: '/images/chemistry.jpg'
    }
];

// Sample faculty data
const faculty = [
    {
        name: 'Dr. Sharma',
        subject: 'Mathematics',
        experience: '15+ years',
        image: '/images/faculty1.jpg'
    },
    {
        name: 'Prof. Verma',
        subject: 'Physics',
        experience: '12+ years',
        image: '/images/faculty2.jpg'
    },
    {
        name: 'Dr. Gupta',
        subject: 'Chemistry',
        experience: '10+ years',
        image: '/images/faculty3.jpg'
    }
];

// Load courses
function loadCourses() {
    const container = document.getElementById('courses-container');
    courses.forEach(course => {
        const courseElement = `
            <div class="col-md-4 mb-4">
                <div class="card course-card">
                    <img src="${course.image}" class="card-img-top" alt="${course.title}">
                    <div class="card-body">
                        <h5 class="card-title">${course.title}</h5>
                        <p class="card-text">${course.description}</p>
                        <p class="card-text"><small class="text-muted">Duration: ${course.duration}</small></p>
                        <button class="btn btn-primary">Enroll Now</button>
                    </div>
                </div>
            </div>
        `;
        container.innerHTML += courseElement;
    });
}

// Load faculty
function loadFaculty() {
    const container = document.getElementById('faculty-container');
    faculty.forEach(member => {
        const facultyElement = `
            <div class="col-md-4">
                <div class="faculty-card">
                    <img src="${member.image}" alt="${member.name}">
                    <h4>${member.name}</h4>
                    <p class="text-primary">${member.subject}</p>
                    <p><small class="text-muted">Experience: ${member.experience}</small></p>
                </div>
            </div>
        `;
        container.innerHTML += facultyElement;
    });
}

// Handle contact form submission
document.getElementById('contact-form').addEventListener('submit', function(e) {
    e.preventDefault();
    const name = document.getElementById('name').value;
    const email = document.getElementById('email').value;
    const message = document.getElementById('message').value;

    // Here you would typically send this data to your backend
    console.log('Form submitted:', { name, email, message });
    alert('Thank you for your message! We will get back to you soon.');
    this.reset();
});

// Initialize
document.addEventListener('DOMContentLoaded', function() {
    loadCourses();
    loadFaculty();
});
