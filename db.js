const fs = require('fs');
const path = require('path');

const DB_FILE = path.join(__dirname, 'ridepilot_data.json');

function initDb() {
    if (!fs.existsSync(DB_FILE)) {
        const initialData = {
            riders: {},
            audit_logs: []
        };
        fs.writeFileSync(DB_FILE, JSON.stringify(initialData, null, 2), 'utf8');
    }
}

function readData() {
    initDb();
    try {
        const raw = fs.readFileSync(DB_FILE, 'utf8');
        return JSON.parse(raw);
    } catch (e) {
        return { riders: {}, audit_logs: [] };
    }
}

function writeData(data) {
    fs.writeFileSync(DB_FILE, JSON.stringify(data, null, 2), 'utf8');
}

const db = {
    saveRider(phone, riderData) {
        const data = readData();
        data.riders[phone] = {
            ...riderData,
            updated_at: new Date().toISOString()
        };
        writeData(data);
        return data.riders[phone];
    },
    getRider(phone) {
        const data = readData();
        return data.riders[phone] || null;
    },
    addAuditLog(action, details) {
        const data = readData();
        const entry = {
            id: Date.now(),
            action,
            details,
            timestamp: new Date().toISOString()
        };
        data.audit_logs.unshift(entry);
        if (data.audit_logs.length > 200) data.audit_logs.pop();
        writeData(data);
        return entry;
    },
    getAuditLogs(limit = 50) {
        const data = readData();
        return data.audit_logs.slice(0, limit);
    }
};

initDb();
module.exports = db;
