package com.ridepilot.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.speech.tts.TextToSpeech
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.viewinterop.AndroidView
import java.util.Locale

@Composable
fun VideoGuideScreen(
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var selectedLang by remember { mutableStateOf(AppLanguage.HI) }
    var tts: TextToSpeech? by remember { mutableStateOf(null) }

    DisposableEffect(Unit) {
        val ttsInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Initialize default language
            }
        }
        tts = ttsInstance
        onDispose {
            ttsInstance.stop()
            ttsInstance.shutdown()
        }
    }

    val languageVideos = mapOf(
        AppLanguage.HI to ("https://www.youtube.com/embed/dQw4w9WgXcQ" to "नमस्ते राइडर भाई! RidePilot Pro में आपका स्वागत है। सबसे पहले सेटिंग्स खोलकर 3-Dots पर क्लिक करें और Allow Restricted Settings चुनें। इसके बाद Accessibility को ऑन करें। आपका ऑटो-एक्सेप्ट चालू हो जाएगा!"),
        AppLanguage.TE to ("https://www.youtube.com/embed/dQw4w9WgXcQ" to "నమస్కారం రైడర్ మిత్రమా! RidePilot Pro కి స్వాగతం. ముందుగా సెట్టింగ్స్ ఓపెన్ చేసి పైన ఉన్న 3 చుక్కలపై క్లిక్ చేసి Allow Restricted Settings ఎంచుకోండి. ఆపై Accessibility ని ఆన్ చేయండి."),
        AppLanguage.EN to ("https://www.youtube.com/embed/dQw4w9WgXcQ" to "Welcome to RidePilot Pro! Open settings, tap top-right 3 dots, select Allow Restricted Settings, and turn on the Accessibility switch."),
        AppLanguage.TA to ("https://www.youtube.com/embed/dQw4w9WgXcQ" to "வணக்கம் ரைடர்! RidePilot Pro-க்கு வரவேற்கிறோம். அமைப்புகளைத் திறந்து மேலே உள்ள 3 புள்ளிகளைத் தட்டி Allow Restricted Settings என்பதைத் தேர்ந்தெடுக்கவும்."),
        AppLanguage.KN to ("https://www.youtube.com/embed/dQw4w9WgXcQ" to "ನಮಸ್ಕಾರ ರೈಡರ್! RidePilot Pro ಗೆ ಸುಸ್ವಾಗತ. ಸೆಟ್ಟಿಂಗ್ಸ್ ತೆರೆದು 3 ಚುಕ್ಕೆಗಳನ್ನು ಕ್ಲಿಕ್ ಮಾಡಿ Allow Restricted Settings ಆಯ್ಕೆಮಾಡಿ.")
    )

    Scaffold(
        containerColor = Color(0xFF0D1117)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🎬 Video Guide & Voice Help", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                TextButton(onClick = onClose) {
                    Text("Close", color = Color(0xFF8B949E))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text("Apni bhasha chunein (Choose Language):", color = Color(0xFF8B949E), fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))

            // Language Selector Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(AppLanguage.values().toList()) { lang ->
                    val isSel = selectedLang == lang
                    Box(
                        modifier = Modifier
                            .background(if (isSel) Color(0xFF00E676) else Color(0xFF161B22), RoundedCornerShape(8.dp))
                            .border(1.dp, if (isSel) Color(0xFF00E676) else Color(0xFF30363D), RoundedCornerShape(8.dp))
                            .clickable {
                                selectedLang = lang
                                val speechData = languageVideos[lang]?.second ?: ""
                                val loc = when (lang) {
                                    AppLanguage.HI -> Locale("hi", "IN")
                                    AppLanguage.TE -> Locale("te", "IN")
                                    AppLanguage.TA -> Locale("ta", "IN")
                                    AppLanguage.KN -> Locale("kn", "IN")
                                    else -> Locale.ENGLISH
                                }
                                tts?.language = loc
                                tts?.speak(speechData, TextToSpeech.QUEUE_FLUSH, null, "RiderGuide")
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(lang.nativeName, color = if (isSel) Color.Black else Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Video Player Box (Auto-embeds cloud tutorial)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .border(1.dp, Color(0xFF00E676), RoundedCornerShape(16.dp))
            ) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            webChromeClient = WebChromeClient()
                            webViewClient = WebViewClient()
                            loadUrl(languageVideos[selectedLang]?.first ?: "https://www.youtube.com")
                        }
                    },
                    update = { webView ->
                        val url = languageVideos[selectedLang]?.first ?: "https://www.youtube.com"
                        webView.loadUrl(url)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Voice Explanation Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF30363D), RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔊 Voice Explanation (${selectedLang.displayName})", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Button(
                            onClick = {
                                val speechData = languageVideos[selectedLang]?.second ?: ""
                                val loc = when (selectedLang) {
                                    AppLanguage.HI -> Locale("hi", "IN")
                                    AppLanguage.TE -> Locale("te", "IN")
                                    AppLanguage.TA -> Locale("ta", "IN")
                                    AppLanguage.KN -> Locale("kn", "IN")
                                    else -> Locale.ENGLISH
                                }
                                tts?.language = loc
                                tts?.speak(speechData, TextToSpeech.QUEUE_FLUSH, null, "RiderGuide")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Play Voice", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        languageVideos[selectedLang]?.second ?: "",
                        color = Color.White,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // WhatsApp Direct Contact Button
            OutlinedButton(
                onClick = {
                    val waUrl = "https://wa.me/919347808890?text=Hello%20Arbaaz%2C%20mujhe%20RidePilot%20setup%20me%20help%20chahiye"
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(waUrl)))
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("💬 Need Personal Help? WhatsApp +91 9347808890", color = Color(0xFF00E676), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
