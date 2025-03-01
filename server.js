const express = require('express');
const bodyParser = require('body-parser');
const path = require('path');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(bodyParser.json());
app.use(express.static(path.join(__dirname, 'public')));

// Routes
app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, 'index.html'));
});

// API endpoints
app.post('/api/contact', (req, res) => {
    const { name, email, message } = req.body;
    // Here you would typically save this to a database
    console.log('Contact form submission:', { name, email, message });
    res.json({ success: true, message: 'Message received successfully!' });
});

app.get('/api/courses', (req, res) => {
    // This would typically come from a database
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
    res.json(courses);
});

app.get('/api/faculty', (req, res) => {
    // This would typically come from a database
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
    res.json(faculty);
});

// Start server
app.listen(PORT, () => {
    console.log(`Server is running on port ${PORT}`);
});
