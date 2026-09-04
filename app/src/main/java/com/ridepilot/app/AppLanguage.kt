package com.ridepilot.app

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    EN("en", "English", "English"),
    HI("hi", "Hindi", "हिन्दी"),
    TE("te", "Telugu", "తెలుగు"),
    TA("ta", "Tamil", "தமிழ்"),
    KN("kn", "Kannada", "ಕನ್ನಡ"),
    MR("mr", "Marathi", "मराठी"),
    BN("bn", "Bengali", "বাংলা"),
    ML("ml", "Malayalam", "മലയാളം")
}

object LanguageStrings {
    fun get(key: String, lang: AppLanguage): String {
        return when (lang) {
            AppLanguage.HI -> when (key) {
                "select_lang" -> "अपनी भाषा चुनें"
                "continue" -> "आगे बढ़ें"
                "guide_title" -> "⚡ ऑटो-एक्सेप्ट चालू करें"
                "guide_desc" -> "Android सुरक्षा के कारण सेटिंग्स अनलॉक करनी होगी:"
                "step1" -> "1. नीचे 'Open Settings' पर टैप करें"
                "step2" -> "2. ऊपर 3-Dots (⋮) पर क्लिक करें"
                "step3" -> "3. 'Allow restricted settings' चुनें"
                "step4" -> "4. Accessibility को ON करें"
                "btn_settings" -> "सेटिंग्स खोलें (3-Dots)"
                "auto_accept" -> "ऑटो-एक्सेप्ट (Instant Tap)"
                "go_home" -> "गो-होम / घर की तरफ राइड्स"
                "plans" -> "सब्सक्रिप्शन प्लान्स"
                else -> key
            }
            AppLanguage.TE -> when (key) {
                "select_lang" -> "మీ భాషను ఎంచుకోండి"
                "continue" -> "కొనసాగించండి"
                "guide_title" -> "⚡ ఆటో-యాక్సెప్ట్ ఆన్ చేయండి"
                "guide_desc" -> "ఆండ్రాయిడ్ సెక్యూరిటీ కోసం సెట్టింగ్‌లను అన్‌లాక్ చేయండి:"
                "step1" -> "1. క్రింద ఉన్న 'Open Settings' పై నొక్కండి"
                "step2" -> "2. పైన ఉన్న 3-చుక్కలను (⋮) నొక్కండి"
                "step3" -> "3. 'Allow restricted settings' ఎంచుకోండి"
                "step4" -> "4. Accessibility ని ON చేయండి"
                "btn_settings" -> "సెట్టింగ్స్ తెరవండి (3-Dots)"
                "auto_accept" -> "ఆటో-యాక్సెప్ట్ (తక్షణ ట్యాప్)"
                "go_home" -> "గో-హోమ్ మోడ్"
                "plans" -> "సబ్‌స్క్రిప్షన్ ప్లాన్స్"
                else -> key
            }
            AppLanguage.TA -> when (key) {
                "select_lang" -> "உங்கள் மொழியைத் தேர்ந்தெடுக்கவும்"
                "continue" -> "தொடரவும்"
                "guide_title" -> "⚡ ஆட்டோ-ஏற்றுக்கொள் ஆன் செய்க"
                "guide_desc" -> "செட்டிங்ஸை அன்லாக் செய்ய கீழே உள்ளதை பின்பற்றவும்:"
                "step1" -> "1. 'Open Settings' பட்டனை கிளிக் செய்க"
                "step2" -> "2. மேலே உள்ள 3-புள்ளிகளை (⋮) தட்டவும்"
                "step3" -> "3. 'Allow restricted settings' தேர்வு செய்க"
                "step4" -> "4. Accessibility-யை ON செய்க"
                "btn_settings" -> "செட்டிங்ஸ் திறக்கவும்"
                "auto_accept" -> "ஆட்டோ-ஏற்றுக்கொள்"
                "go_home" -> "கோ-ஹோம் பயன்முறை"
                "plans" -> "சந்தா திட்டங்கள்"
                else -> key
            }
            AppLanguage.KN -> when (key) {
                "select_lang" -> "ನಿಮ್ಮ ಭಾಷೆಯನ್ನು ಆಯ್ಕೆಮಾಡಿ"
                "continue" -> "ಮುಂದುವರಿಯಿರಿ"
                "guide_title" -> "⚡ ಆಟೋ-ಸ್ವೀಕರಿಸಿ ಆನ್ ಮಾಡಿ"
                "guide_desc" -> "ಸೆಟ್ಟಿಂಗ್ಸ್ ಅನ್‌ಲಾಕ್ ಮಾಡಲು ಕೆಳಗಿನವುಗಳನ್ನು ಅನುಸರಿಸಿ:"
                "step1" -> "1. 'Open Settings' ಕ್ಲಿಕ್ ಮಾಡಿ"
                "step2" -> "2. ಮೇಲಿನ 3-ಚುಕ್ಕೆಗಳನ್ನು (⋮) ಕ್ಲಿಕ್ ಮಾಡಿ"
                "step3" -> "3. 'Allow restricted settings' ಆಯ್ಕೆಮಾಡಿ"
                "step4" -> "4. Accessibility ಆನ್ ಮಾಡಿ"
                "btn_settings" -> "ಸೆಟ್ಟಿಂಗ್ಸ್ ತೆರೆಯಿರಿ"
                "auto_accept" -> "ಆಟೋ-ಸ್ವೀಕರಿಸಿ"
                "go_home" -> "ಗೋ-ಹೋಮ್ ಮೋಡ್"
                "plans" -> "ಚಂದಾದಾರಿಕೆ ಯೋಜನೆಗಳು"
                else -> key
            }
            else -> when (key) {
                "select_lang" -> "Select Your Language"
                "continue" -> "Continue"
                "guide_title" -> "⚡ Enable Auto-Accept"
                "guide_desc" -> "Follow steps to unlock restricted settings:"
                "step1" -> "1. Tap 'Open Settings' below"
                "step2" -> "2. Tap top-right 3-Dots (⋮)"
                "step3" -> "3. Choose 'Allow restricted settings'"
                "step4" -> "4. Turn Accessibility switch ON"
                "btn_settings" -> "Open App Settings (3-Dots)"
                "auto_accept" -> "Instant Auto-Accept"
                "go_home" -> "Go Home / Destination Mode"
                "plans" -> "Subscription Plans"
                else -> key
            }
        }
    }
}
