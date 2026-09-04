package com.ridepilot.app

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun SubscriptionScreen(
    subManager: SubscriptionManager,
    onPaymentSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedPlan by remember { mutableStateOf<Plan?>(null) }
    var txnStatusMessage by remember { mutableStateOf<String?>(null) }

    val upiLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val rawResponse = result.data?.getStringExtra("response") ?: ""
        val responseLower = rawResponse.lowercase(Locale.ROOT)

        // Strict UPI status verification
        val isVerified = responseLower.contains("status=success") || 
                         (responseLower.contains("success") && !responseLower.contains("fail") && !responseLower.contains("cancel"))

        if (result.resultCode == Activity.RESULT_OK && isVerified) {
            selectedPlan?.let { plan ->
                subManager.isSubscribed = true
                subManager.activePlanName = plan.title
                subManager.expiryTimeMillis = System.currentTimeMillis() + (plan.validityDays.toLong() * 24 * 60 * 60 * 1000)
                Toast.makeText(context, "✅ Payment Verified! ${plan.title} Active.", Toast.LENGTH_LONG).show()
                onPaymentSuccess()
            }
        } else {
            // Payment was canceled or failed
            txnStatusMessage = "Payment verify nahi hua ya cancel ho gaya. Agar paise kat gaye hain toh WhatsApp support par transaction ID bhein."
            Toast.makeText(context, "❌ Payment Verification Failed", Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        containerColor = Color(0xFF0D1117)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚡ RidePilot Subscriptions", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    TextButton(onClick = onBack) {
                        Text("Close", color = Color(0xFF8B949E))
                    }
                }
                Text("Select a plan to start instant auto-accept orders", color = Color(0xFF8B949E), fontSize = 13.sp)
                Spacer(modifier = Modifier.height(10.dp))
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF16251E)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF00E676), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("UPI ID: 9347808890-n7bc@ibl", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Payee: Mohammed Arbaaz (PhonePe Verified)", color = Color.White, fontSize = 12.sp)
                    }
                }

                txnStatusMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF331616)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFFF5252), RoundedCornerShape(10.dp))
                    ) {
                        Text(msg, color = Color(0xFFFF8A80), fontSize = 12.sp, modifier = Modifier.padding(12.dp))
                    }
                }
            }

            items(subManager.plans) { plan ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.2.dp, if (plan.tag.isNotEmpty()) Color(0xFF00E676) else Color(0xFF30363D), RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(plan.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                if (plan.tag.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = Color(0xFF00E676),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(plan.tag, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Validity: ${plan.validityDays} Day(s)", color = Color(0xFF8B949E), fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                selectedPlan = plan
                                txnStatusMessage = null
                                val rawAmount = plan.price.replace("₹", "").trim()
                                val upiUri = Uri.parse("upi://pay").buildUpon()
                                    .appendQueryParameter("pa", "9347808890-n7bc@ibl")
                                    .appendQueryParameter("pn", "Mohammed Arbaaz")
                                    .appendQueryParameter("am", rawAmount)
                                    .appendQueryParameter("cu", "INR")
                                    .appendQueryParameter("tn", "RidePilot ${plan.title}")
                                    .build()
                                val intent = Intent(Intent.ACTION_VIEW, upiUri)
                                try {
                                    upiLauncher.launch(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "No UPI app found. Please pay manually to 9347808890-n7bc@ibl", Toast.LENGTH_LONG).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Pay ${plan.price}", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                    modifier = Modifier.fillMaxWidth().clickable {
                        val waUrl = "https://wa.me/919347808890?text=Hello%20Arbaaz%2C%20maine%20RidePilot%20par%20payment%20kiya%20hai%20lekin%20verify%20nahi%20hua.%20Mera%20Txn%20Screenshot%20yeh%20hai"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(waUrl))
                        context.startActivity(intent)
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("💬 WhatsApp Support: +91 9347808890", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
