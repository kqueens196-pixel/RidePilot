const express = require('express');
const cors = require('cors');
const db = require('./db');

const app = express();
app.use(cors());
app.use(express.json());

const PORT = process.env.PORT || 3000;

function logAuditEvent(action, details) {
    db.addAuditLog(action, details);
    console.log(`[AUDIT] ACTION=${action} | DETAILS=${JSON.stringify(details)}`);
}

const otpStore = new Map();

// 1. Send OTP Endpoint
app.post('/api/auth/send-otp', (req, res) => {
    const { phone } = req.body;
    if (!phone || phone.length !== 10) {
        logAuditEvent('AUTH_OTP_FAIL', { reason: 'Invalid phone', phone });
        return res.status(400).json({ success: false, message: "Valid 10-digit phone number required" });
    }

    const otp = "123456";
    otpStore.set(phone, otp);

    logAuditEvent('AUTH_OTP_SENT', { phone });
    res.json({ success: true, message: "OTP sent successfully (Use 123456 for test)" });
});

// 2. Verify OTP & Store/Retrieve Rider
app.post('/api/auth/verify-otp', (req, res) => {
    const { phone, otp } = req.body;
    const storedOtp = otpStore.get(phone);

    if (storedOtp && (storedOtp === otp || otp === "123456")) {
        otpStore.delete(phone);
        const token = "mock-jwt-token-rider-" + phone;

        const rider = db.saveRider(phone, {
            token,
            planName: "Pro Fleet Rider",
            status: "ACTIVE",
            daysRemaining: 28
        });

        logAuditEvent('AUTH_LOGIN_SUCCESS', { phone });
        return res.json({
            success: true,
            message: "Authentication successful",
            token,
            subscription: {
                status: rider.status,
                planName: rider.planName,
                daysRemaining: rider.daysRemaining
            }
        });
    }

    logAuditEvent('AUTH_LOGIN_FAILED', { phone, reason: 'Invalid OTP' });
    res.status(401).json({ success: false, message: "Invalid or expired OTP" });
});

// 3. Live Orders Feed
app.get('/api/orders/feed', (req, res) => {
    const orders = [
        { id: "ORD-501", provider: "Rapido", type: "RIDE", pickup: "Hitech City", drop: "Gachibowli", distanceKm: 4.5, payoutInr: 180 },
        { id: "ORD-502", provider: "Porter", type: "PARCEL", pickup: "Madhapur", drop: "Jubilee Hills", distanceKm: 3.2, payoutInr: 130 },
        { id: "ORD-503", provider: "Shadowfax", type: "PARCEL", pickup: "Banjara Hills", drop: "Secunderabad", distanceKm: 12.0, payoutInr: 310 }
    ];
    logAuditEvent('ORDERS_FEED_ACCESSED', { count: orders.length });
    res.json({ success: true, count: orders.length, orders });
});

// 4. Admin Audit Logs Endpoint (From DB)
app.get('/api/admin/audit-logs', (req, res) => {
    const logs = db.getAuditLogs(50);
    res.json({ success: true, total: logs.length, logs });
});

app.listen(PORT, () => {
    console.log(`RidePilot API running on http://localhost:${PORT}`);
});
