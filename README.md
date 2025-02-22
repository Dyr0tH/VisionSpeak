# VisionSpeak

## 📌 About VisionSpeak
VisionSpeak is an AI-powered mobile application that allows users to capture or upload images and receive a detailed description of the image in their selected language. The app also generates audio narration for the description and provides translations in multiple languages, making it accessible to a global audience.

## 🎯 Features
- 📷 **Image Capture & Upload** – Users can take pictures using the camera or upload existing images.
- 🌍 **Multi-Language Support** – Users can choose a language, and the app provides descriptions in that language.
- 🔊 **Text-to-Speech (TTS)** – Generates an audio narration of the description in the selected language.
- 🔄 **Auto-Translation** – Automatically translates the description into all supported languages.
- ⚡ **Fast & Efficient Processing** – Uses AI-powered models for quick image analysis and description generation, given that you've a powerful compute machine.

## 🛠️ Tech Stack
### **Frontend (Android App)**
- **Language:** Java (Android Development)
- **Tools:** Android Studio, OpenCamera API

### **Backend (AI & API Services)**
- **Framework:** FastAPI (Python)
- **AI Model:** Ollama (for image processing and content extraction)
- **TTS Engine:** OpenTTS (running on Docker)
- **Database:** SQLite / PostgreSQL (if required for caching or storage)
- **Cloud:** Optional deployment via AWS / DigitalOcean

## 🚀 Installation & Setup
### **1. Clone the Repository**
```sh
git clone https://github.com/yourusername/VisionSpeak.git
cd VisionSpeak
```

### **2. Setting Up the Backend**
Ensure you have Python installed, then install dependencies:
```sh
pip install -r backend/requirements.txt
```
Run the backend server:
```sh
uvicorn backend.main:app --host 0.0.0.0 --port 8000
ngrok localhost 8000
```
(register on ngrok to get a permanent url for free..)

### **3. Running the TTS Server**
Ensure Docker is installed, then start OpenTTS:
```sh
docker run -d -p 5002:5002 synesthesiam/opentts:latest
```

### **4. Setting Up the Android App**
- Open the Android project in **Android Studio**.
- Connect an emulator or physical device.
- Build and run the app.

## 📷 How It Works
1. Open the **VisionSpeak** app.
2. Capture an image using the camera or upload an existing one.
3. Select a preferred language from the list.
4. The AI processes the image and generates a description.
5. The description is read aloud using text-to-speech (TTS).
6. Translations for other available languages are also displayed.

## 🔒 Permissions Required
- **Camera Access** (for capturing images)
- **Storage Access** (for selecting images from the gallery)
- **Microphone Access** (optional, if voice input features are added in the future)

## 🛠️ Future Enhancements
- ✅ Offline Mode for basic image processing
- ✅ More AI models for improved accuracy
- ✅ Additional languages and voices for TTS
- ✅ Cloud deployment for scalability

## 🤝 Contributing
We welcome contributions! Feel free to fork the repository, submit pull requests, or raise issues.

## 📜 License
MIT License © 2025 VisionSpeak Developers

## 🌟 Acknowledgments
- **Ollama** – AI model for image description
- **OpenTTS** – Open-source text-to-speech engine
- **FastAPI** – High-performance API framework for backend

---
**Made with ❤️ by Shahid Ali / Dyr0tH**
