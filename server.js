const express = require('express');
const session = require('express-session');
const bodyParser = require('body-parser');
const path = require('path');
const mysql = require('mysql2/promise');

const app = express();
const PORT = process.env.PORT || 3000;

// Database Connection
const pool = mysql.createPool({
    host: 'localhost',
    user: 'your_username',
    password: 'your_password',
    database: 'just_glance_tuition',
    waitForConnections: true,
    connectionLimit: 10,
    queueLimit: 0
});

// Middleware
app.use(bodyParser.json());
app.use(express.static(path.join(__dirname, 'public')));
app.use(session({
    secret: 'your_secret_key',
    resave: false,
    saveUninitialized: true,
    cookie: { 
        secure: false, 
        maxAge: 24 * 60 * 60 * 1000 // 24 hours
    }
}));

// Authentication Middleware
const requireAuth = (req, res, next) => {
    if (req.session.user) {
        next();
    } else {
        res.status(401).json({ error: 'Unauthorized' });
    }
};

// Login Route
app.post('/api/login', async (req, res) => {
    const { username, password } = req.body;
    try {
        const [users] = await pool.execute(
            'SELECT * FROM users WHERE username = ? AND password = ?', 
            [username, password]
        );

        if (users.length > 0) {
            req.session.user = users[0];
            res.json({ success: true, user: users[0] });
        } else {
            res.status(401).json({ success: false, message: 'Invalid credentials' });
        }
    } catch (error) {
        res.status(500).json({ success: false, message: 'Server error' });
    }
});

// Logout Route
app.post('/api/logout', (req, res) => {
    req.session.destroy((err) => {
        if (err) {
            return res.status(500).json({ success: false });
        }
        res.json({ success: true });
    });
});

// Login Status Check
app.get('/api/check-login', (req, res) => {
    res.json({ 
        isLoggedIn: !!req.session.user 
    });
});

// Enrollments API
app.get('/api/enrollments', requireAuth, async (req, res) => {
    try {
        const [enrollments] = await pool.execute(`
            SELECT e.*, s.name as studentName, c.name as courseName 
            FROM enrollments e
            JOIN students s ON e.student_id = s.id
            JOIN courses c ON e.course_id = c.id
        `);
        res.json(enrollments);
    } catch (error) {
        res.status(500).json({ error: 'Failed to fetch enrollments' });
    }
});

// Dashboard Stats
app.get('/api/dashboard/stats', requireAuth, async (req, res) => {
    try {
        const [totalStudents] = await pool.execute('SELECT COUNT(*) as count FROM students');
        const [activeCourses] = await pool.execute('SELECT COUNT(*) as count FROM courses WHERE status = "ACTIVE"');
        const [newEnrollments] = await pool.execute(`
            SELECT COUNT(*) as count 
            FROM enrollments 
            WHERE enrollment_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
        `);
        const [todayAttendance] = await pool.execute(`
            SELECT AVG(attendance_percentage) as percentage 
            FROM daily_attendance 
            WHERE date = CURDATE()
        `);

        res.json({
            totalStudents: totalStudents[0].count,
            activeCourses: activeCourses[0].count,
            newEnrollments: newEnrollments[0].count,
            todayAttendance: todayAttendance[0].percentage || 0
        });
    } catch (error) {
        res.status(500).json({ error: 'Failed to fetch dashboard stats' });
    }
});

// Attendance Chart
app.get('/api/dashboard/attendance-chart', requireAuth, async (req, res) => {
    try {
        const [attendanceData] = await pool.execute(`
            SELECT 
                DATE_FORMAT(date, '%Y-%m-%d') as label, 
                AVG(attendance_percentage) as value
            FROM daily_attendance
            WHERE date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
            GROUP BY date
            ORDER BY date
        `);

        res.json({
            labels: attendanceData.map(row => row.label),
            values: attendanceData.map(row => row.value)
        });
    } catch (error) {
        res.status(500).json({ error: 'Failed to fetch attendance chart data' });
    }
});

// Attendance Marking
app.post('/api/attendance', requireAuth, async (req, res) => {
    const { enrollmentId, status, notes, date } = req.body;
    try {
        await pool.execute(`
            INSERT INTO attendance 
            (enrollment_id, status, notes, date) 
            VALUES (?, ?, ?, ?)
        `, [enrollmentId, status, notes, date]);
        res.json({ success: true });
    } catch (error) {
        res.status(500).json({ error: 'Failed to mark attendance' });
    }
});

// Contact Submissions
app.get('/api/contact-submissions', requireAuth, async (req, res) => {
    try {
        const [submissions] = await pool.execute('SELECT * FROM contact_submissions ORDER BY submission_time DESC');
        res.json(submissions);
    } catch (error) {
        res.status(500).json({ error: 'Failed to fetch contact submissions' });
    }
});

app.get('/api/contact-submissions/:id', requireAuth, async (req, res) => {
    try {
        const [submissions] = await pool.execute(
            'SELECT * FROM contact_submissions WHERE id = ?', 
            [req.params.id]
        );
        res.json(submissions[0]);
    } catch (error) {
        res.status(500).json({ error: 'Failed to fetch submission details' });
    }
});

app.post('/api/contact-submissions/:id/toggle-read', requireAuth, async (req, res) => {
    try {
        await pool.execute(`
            UPDATE contact_submissions 
            SET is_read = NOT is_read 
            WHERE id = ?
        `, [req.params.id]);
        res.json({ success: true });
    } catch (error) {
        res.status(500).json({ error: 'Failed to toggle read status' });
    }
});

app.post('/api/contact-submissions/mark-all-read', requireAuth, async (req, res) => {
    try {
        await pool.execute('UPDATE contact_submissions SET is_read = TRUE');
        res.json({ success: true });
    } catch (error) {
        res.status(500).json({ error: 'Failed to mark all submissions as read' });
    }
});

// Start Server
app.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
});

// Error Handling Middleware
app.use((err, req, res, next) => {
    console.error(err.stack);
    res.status(500).send('Something broke!');
});

module.exports = app;
