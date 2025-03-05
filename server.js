require('dotenv').config(); // Load environment variables at the beginning

const express = require('express');
const session = require('express-session');
const bodyParser = require('body-parser');
const path = require('path');
const mysql = require('mysql2/promise');
const bcrypt = require('bcryptjs');
const cors = require('cors');

const app = express();
const PORT = process.env.PORT || 3000;

// Database Connection
const pool = mysql.createPool({
    host: process.env.DB_HOST || 'localhost',
    user: process.env.DB_USER || 'just_glance_app',
    password: process.env.DB_PASSWORD || 'your_app_password',
    database: process.env.DB_NAME || 'just_glance_tuition',
    waitForConnections: true,
    connectionLimit: 10,
    queueLimit: 0
});

// Middleware
app.use(cors()); // Add CORS support
app.use(bodyParser.json());
app.use(express.json()); // Additional JSON parser
app.use(express.static(path.join(__dirname, 'public')));
app.use(express.static(path.join(__dirname, 'admin')));

// Logging middleware for debugging
app.use((req, res, next) => {
    console.log(`Received ${req.method} request to ${req.path}`);
    next();
});

app.use(session({
    secret: process.env.SESSION_SECRET || 'your_secret_key',
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
        return next();
    }
    res.status(401).json({ error: 'Unauthorized' });
};

// User Registration Route
app.post('/api/register', async (req, res) => {
    console.log('Registration request received:', req.body);
    const { username, password, email, role } = req.body;
    
    // Validate input
    if (!username || !password || !email) {
        console.error('Registration validation failed: Missing required fields');
        return res.status(400).json({ 
            success: false, 
            message: 'Username, password, and email are required' 
        });
    }

    try {
        // Check if user already exists
        const [existingUsers] = await pool.execute(
            'SELECT * FROM users WHERE username = ? OR email = ?', 
            [username, email]
        );

        if (existingUsers.length > 0) {
            console.error('Registration failed: User already exists');
            return res.status(409).json({ 
                success: false, 
                message: 'Username or email already exists' 
            });
        }
        
        // Hash password
        const hashedPassword = await bcrypt.hash(password, 10);

        // Insert new user
        const [result] = await pool.execute(
            'INSERT INTO users (username, password, email, role) VALUES (?, ?, ?, ?)', 
            [username, hashedPassword, email, role || 'STAFF']
        );

        console.log('User registered successfully:', username);
        res.status(201).json({ 
            success: true, 
            message: 'User registered successfully',
            userId: result.insertId 
        });
    } catch (error) {
        console.error('Detailed registration error:', {
            message: error.message,
            stack: error.stack,
            name: error.name
        });
        res.status(500).json({ 
            success: false, 
            message: 'Server error during registration',
            error: error.message 
        });
    }
});

// Login Route
app.post('/api/login', async (req, res) => {
    const { username, password } = req.body;
    try {
        const [users] = await pool.execute('SELECT * FROM users WHERE username = ?', [username]);
        if (users.length === 0 || !(await bcrypt.compare(password, users[0].password))) {
            return res.status(401).json({ success: false, message: 'Invalid credentials' });
        }
        req.session.user = users[0];
        res.json({ success: true, user: { username: users[0].username } });
    } catch (error) {
        console.error('Login error:', error);
        res.status(500).json({ success: false, message: 'Server error' });
    }
});

// Logout Route
app.post('/api/logout', (req, res) => {
    req.session.destroy(err => {
        if (err) {
            return res.status(500).json({ success: false, error: err.message });
        }
        res.json({ success: true });
    });
});

// Check Login Status
app.get('/api/check-login', (req, res) => {
    res.json({ isLoggedIn: !!req.session.user, user: req.session.user ? { username: req.session.user.username } : null });
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
