# Cupertino Clock — iOS Clock Widget for Android

An authentic iOS-inspired clock widget application designed for Android, crafted with Apple's Human Interface Guidelines (HIG) aesthetics:
- **Minimalist Analog Dial**: Custom Canvas-rendered dial with precision hour/quarter ticks, pill-shaped hands, and the signature Cupertino orange (`#FF9500`) second hand & center pivot.
- **Lock Screen Digital Widget**: Bold lock screen typography, dynamic date pill, local time tags, and mini analog accent.
- **iOS Squircles**: Continuous curvature rounded corners (28dp squircle radius) with deep OLED black and frosted glass styling.
- **World Clock Support**: Multi-city timezone support (Cupertino, New York, London, Paris, Tokyo, Dubai, Sydney).
- **One-Tap Home Screen Pinning**: Uses Android's `AppWidgetManager.requestPinAppWidget` to pin widgets directly from the companion app.

---

## Project Structure

```
AndroidApp/
├── app/
│   ├── build.gradle
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/cupertino/clockwidget/
│       │   ├── ClockRenderer.kt             # High-res Canvas renderer for iOS dials
│       │   ├── IosAnalogWidgetProvider.kt    # 2x2 Analog World Clock Widget
│       │   ├── IosDigitalWidgetProvider.kt   # 4x2 Digital Lock Screen Widget
│       │   └── MainActivity.kt              # Companion Preview & Widget Pinner
│       └── res/
│           ├── drawable/                    # Squircles, pills, vector icons
│           ├── layout/                      # Widget & Activity UI layouts
│           ├── values/                      # Apple colors, strings, themes
│           └── xml/                         # AppWidgetProvider metadata
├── build.gradle
├── gradle.properties
├── settings.gradle
└── README.md
```

---

## How to Build the APK

### Prerequisites
- Android Studio Iguana / Jellyfish / Ladybug (or Android SDK with build-tools 34+)
- JDK 17

### Building the APK via Command Line
Run the following command in the project directory:

```bash
# On Linux / macOS:
./gradlew assembleDebug

# On Windows:
gradlew.bat assembleDebug
```

The generated APK will be available at:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## Adding Widgets to your Home Screen

1. Install and launch **Cupertino Clock**.
2. Select your desired City/Timezone from the dropdown.
3. Tap **Pin Analog Widget to Home** or **Pin Digital Widget to Home**.
4. Alternatively, long-press any empty space on your Android home screen, select **Widgets**, scroll to **Cupertino Clock**, and drag your favorite widget to the screen.
