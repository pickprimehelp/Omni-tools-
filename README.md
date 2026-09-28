# OmniTool Studio 🚀
### All-In-One Pro Creative & Daily Utility Suite for Android

OmniTool Studio is a comprehensive, production-ready Android application built with **Kotlin** and **Jetpack Compose (Material Design 3)**. It packs over 15+ full-featured tools into a single lightweight, blazing-fast application.

---

## ✨ Features Included

### 🎨 Creative & Media Tools
- **Wedding Card Studio**: Full invitation editor with couple details, muhurat timing, multiple photos, auspicious symbols, royal designer templates, and high-resolution export.
- **Reels & Video Creator**: Short-form 9:16 video editor with transition effects (Fade, Slide, Zoom, Flash, Dissolve), music audio preview, playback speed control (0.5x–2.0x), and captions.
- **Background Eraser HD**: Smart edge cutout algorithm with threshold tolerance slider (10%–80%), studio solid backgrounds, gradients, and transparent PNG export.
- **Story & Status Maker**: 9:16 social status editor with trending quotes (Motivation, Hindi Suvichar, Love, Attitude), custom photo backdrops, stickers, and 1080x1920 export.
- **Pro Photo Studio**: Real-time color filters (Vivid, Vintage, Noir B&W, Sepia, Cyber Neon), contrast & brightness tuning, crop presets, and freehand drawing brush.
- **Voice Changer Studio**: Real-time audio voice recorder with 8 distinct voice filters (Robot 🤖, Helium 🎈, Deep Monster 🦁, Walkie-Talkie 📻, Cave Echo 🏔️, Speedy 🚀, Slow-Mo 🐢).
- **Fancy Text & Word Art**: 20+ Unicode font stylers, colorful gradient word badges, ASCII borders, and one-tap copy/share.

### 💼 Business & Productivity Tools
- **Resume & CV Builder**: Professional job resume maker with Personal Info, Work History, Skills chips, and instant vector PDF export.
- **Document Scanner & OCR**: Multi-page document camera scan, scanner color enhancement filters, and Google ML Kit OCR text extraction.
- **Invoice & Bill Maker**: Itemized billing with automated GST/tax & discount calculation, Room local database history, and printable PDF invoice generation.
- **Project Timelines & Tasks**: Kanban sprint board (To Do, In Progress, Completed), priority tags, assignee avatars, and Room database persistence.
- **URL Shortener**: Live TinyURL API integration with instant scannable QR Code generator and history.

### 📱 Daily Essentials & Utilities
- **Age & Birthday Calculator**: Exact age breakdown (years, months, days, total hours/minutes), countdown to next birthday, and Zodiac sign astrology.
- **QR Code Studio**: Custom QR generator for Wi-Fi networks, WhatsApp direct chat, URLs, and text contacts.
- **Everyday Unit Converter**: Length, Weight/Mass, Temperature, Area, and Speed conversion.
- **Hydration & Daily Habits Tracker**: 8 Glasses / 2000 ml water intake tracker and daily habit checklists.
- **Shopping Discount & Loan EMI Calculator**: Instant percentage discount calculator and monthly bank EMI breakdown.
- **Support App Author**: Integrated developer funding feature with direct UPI QR Code (GPay, PhonePe, Paytm).

---

## 📲 How to Download APK from GitHub

1. Push this project to your GitHub repository.
2. Go to the **Actions** tab in your GitHub repository.
3. The **"Build Android APK & Release"** workflow will automatically build your APK.
4. Click on the completed workflow run and download the **OmniTool-Debug-APK** artifact.
5. Transfer the `.apk` file to your Android phone and install!

---

## 🛠️ Build Locally via Terminal

```bash
# Clone the repository
git clone https://github.com/your-username/omnitool-studio.git
cd omnitool-studio

# Build Debug APK
./gradlew assembleDebug

# Build Release APK / App Bundle (for Play Store)
./gradlew bundleRelease
```

The APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 📜 Google Play Store Deployment Checklist

1. Generate your release signing key (`keystore.jks`).
2. Run `./gradlew bundleRelease` to generate the `.aab` file at `app/build/outputs/bundle/release/app-release.aab`.
3. Upload `app-release.aab` to Google Play Console.
4. Prepare Play Store graphics (1024x500 feature graphic, screenshots, icon).
5. Submit for Review!
