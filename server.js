const express = require('express');
const cors = require('cors');

const app = express();
app.use(cors());
app.use(express.json());

const PORT = process.env.PORT || 3000;

// Temporary in-memory OTP store
const otpStore = new Map();

// 1. Send OTP Endpoint
app.post('/api/auth/send-otp', (req, res) => {
    const { phone } = req.body;
    if (!phone || phone.length !== 10) {
        return res.status(400).json({ success: false, message: "Valid 10-digit phone number required" });
    }

    // Static OTP for dev/testing: 123456
    const otp = "123456";
    otpStore.set(phone, otp);

    console.log(`[OTP SENT] To: ${phone} | Code: ${otp}`);
    res.json({ success: true, message: "OTP sent successfully (Use 123456 for test)" });
});

// 2. Verify OTP Endpoint
app.post('/api/auth/verify-otp', (req, res) => {
    const { phone, otp } = req.body;
    const storedOtp = otpStore.get(phone);

    if (storedOtp && (storedOtp === otp || otp === "123456")) {
        otpStore.delete(phone);
        return res.json({
            success: true,
            message: "Authentication successful",
            token: "mock-jwt-token-rider-" + phone,
            subscription: {
                status: "ACTIVE",
                planName: "Pro Fleet Rider",
                daysRemaining: 28
            }
        });
    }

    res.status(401).json({ success: false, message: "Invalid or expired OTP" });
});

// 3. Normalized Feed Endpoint
app.get('/api/orders/feed', (req, res) => {
    const orders = [
        { id: "ORD-501", provider: "Rapido", type: "RIDE", pickup: "Hitech City", drop: "Gachibowli", distanceKm: 4.5, payoutInr: 180 },
        { id: "ORD-502", provider: "Porter", type: "PARCEL", pickup: "Madhapur", drop: "Jubilee Hills", distanceKm: 3.2, payoutInr: 130 },
        { id: "ORD-503", provider: "Shadowfax", type: "PARCEL", pickup: "Banjara Hills", drop: "Secunderabad", distanceKm: 12.0, payoutInr: 310 }
    ];
    res.json({ success: true, count: orders.length, orders });
});

app.listen(PORT, () => {
    console.log(`RidePilot API running on http://localhost:${PORT}`);
});
