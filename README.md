# AppLocker 🔐

**AppLocker** is a creative Android app that simulates a "phone unlocking challenge" through five interactive tasks.  
The goal: successfully complete all five tasks to unlock the app and reach the final screen.

---

## ✨ What does the app do?

The app presents the user with 5 different challenges — each task interacts with a different feature or sensor on the device.  
The user must complete them in order, progressing step-by-step until the final task is completed.

---

## 🕹️ Challenge Steps:

### 🔋 Step 1: Battery Check
> The user is asked to input their current battery percentage.  
If the number matches the actual level — the task is marked as complete.

---

### 👤 Step 2: Contacts Check
> The user must add the developer’s phone number to their contacts.  
The app then scans the device’s contact list and verifies the number exists.

---

### 📷 Step 3: Camera Task – Red Object
> The user is asked to take a photo of an object that is dominantly red.  
The app analyzes the pixels of the photo and determines if the red color is dominant enough.

---

### 📩 Step 4: Code via SMS
> The app generates a 4-digit code (based on the photo’s timestamp) and asks the user to send it via SMS to the developer’s number.  
It then scans the "Sent SMS" folder to verify that the correct message was sent.

---

### 🎤 Step 5: Voice Recognition
> The user is prompted to say the phrase “שומשום היפתח” ("Open Sesame" in Hebrew).  
The app uses voice recognition to confirm the user said the correct phrase — with tolerance for similar pronunciations.

---

## 🏁 Game Completion

After all 5 tasks are successfully completed, the app plays a clapping sound and navigates to the final screen (`HomeActivity`).

---

## 📱 Required Permissions

The app requests the following permissions:

- `READ_CONTACTS` – for checking developer’s phone in contacts
- `CAMERA` – for taking a photo
- `RECEIVE_SMS`, `READ_SMS` – for verifying SMS was sent
- `RECORD_AUDIO` – for voice recognition

---

## 🛠 Technologies Used

- MediaPlayer for sound effects
- SpeechRecognizer API for voice input
- ContentResolver and Intents for reading contacts, messages, and camera input
- Bitmap pixel analysis for color detection

---

## 🚀 Getting Started

1. Open the project in Android Studio
3. Run the app on an emulator or physical device
4. Start from Task 1 and progress step-by-step

---

Enjoy the challenge! 🔐🎉

---
📌 *Maintained by:*  Noa Sharabi 
