// test-db-connection.js
const mysql = require('mysql2/promise');

const dbConfig = {
    host: 'localhost',
    user: 'just_glance_app',
    password: 'your_app_password',
    database: 'just_glance_tuition'
};

async function testConnection() {
    try {
        const connection = await mysql.createConnection(dbConfig);
        console.log('✅ Database connection successful!');
        await connection.end();
        process.exit(0);
    } catch (error) {
        console.error('❌ Database connection failed:', error.message);
        process.exit(1);
    }
}

testConnection();