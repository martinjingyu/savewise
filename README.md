# SaveWise
A mobile budgeting & expense-tracking app designed to help busy people manage daily spending with minimal effort.

SaveWise allows users to **record expenses using their voice**, automatically classify spending, visualize financial patterns, and review histories effortlessly.

---

## 🚀 Features

### 🎙 Voice-Based Expense Input
Users can record an expense *just by speaking*.  
SaveWise converts voice into structured data including:

- **Category**
- **Description**
- **Amount**

No manual typing needed.

### 📊 Monthly Spending Insights
A clean dashboard visualizes:

- Total monthly spending
- Category distribution chart
- Recent expenses list

Helps users understand where their money goes instantly.

### 🧠 AI Tip Assistant
SaveWise provides AI-generated spending insights and suggestions, helping users control financial habits over time.

### 📅 Expense History
Users can scroll through past transactions, filter entries, and easily track daily and monthly spending records.

### ⚙️ Personalization
A customizable Settings page allows users to:

- Adjust theme
- Change profile preferences
- Configure speech recognition modes
- Manage audio recording retention

---

## 🎨 Screenshots
*(Insert your app's screenshots here if you want)*

- Home screen with greeting and summary card
- Voice expense recording interface
- Monthly spending chart
- Expense list
- Settings page

---

## 🛠 Technology Stack

### **Frontend / UI**
- Jetpack Compose (Material 3)
- Android Studio Hedgehog / Iguana
- Kotlin Coroutines / StateFlow

### **Speech & AI**
- Android SpeechRecognizer API
- Whisper-based transcription (manual mode)
- Custom AI Tip Generator (using your backend logic)

### **Data**
- Room Database
- Local History tracking
- Local file storage for voice recordings

---

## 👥 Development Team (CS407 Fall 2025)

| Name | Responsibility |
|------|----------------|
| **Haowen Zheng** | Settings screen, advanced UI components |
| **Jingyu Huang** | Home page, voice recognition ﬂow |
| **Junyuan Zhou** | Expense history page, data storage |
| **Yuxiang Wu** | Login page, UI refinement |

---

## 🧩 Challenges & Solutions

### 1. Advanced UI Complexity
Creating modern UI layouts was difficult due to limited experience.  
**Solution:** Studied Material 3 examples and drew inspiration from popular financial apps.

### 2. Speech Recognition Reliability
Handling silence, recognition errors, and auto-pause required careful design.  
**Solution:** Added dual-mode recognition:
- **Auto-pause mode:** Android