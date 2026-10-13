# CreatorNova — AI Content Creator Marketplace
*Create. Connect. Collaborate.*
**ByteXL × Kampus.VC Hackathon Challenge**

---

## 🚀 Overview

**CreatorNova** is an AI Content Creator Marketplace connecting brands, businesses, marketing teams, and creative agencies with top Indian AI content creators.

This repository contains:
1. **Web Application (`index.html` + `vercel.json`)**: Complete responsive single-page web app built with Tailwind CSS, supporting dark/light mode, desktop & mobile layouts (with built-in viewport switcher), Nova AI assistant, Nova EcoMatch impact predictor, and 8-stage campaign lifecycle tracking.
2. **Android Application (`/app`)**: Native Android app written in Kotlin and Jetpack Compose featuring Material Design 3 and Gemini AI integration.

---

## 🌐 Deploying to Vercel

The web application file is located right at the root: `index.html`.

### Option A: Via GitHub (Recommended)
1. Push this repository to your GitHub account:
   ```bash
   git init
   git add .
   git commit -m "Initial commit of CreatorNova"
   git branch -M main
   git remote add origin https://github.com/<your-username>/<your-repo-name>.git
   git push -u origin main
   ```
2. Go to [vercel.com](https://vercel.com) and log in.
3. Click **"Add New Project"** and select **"Import Git Repository"**.
4. Choose your `CreatorNova` repository.
5. In the project settings:
   - **Framework Preset**: *Other*
   - **Root Directory**: `./` (leave default)
6. Click **"Deploy"**. Vercel will detect `index.html` and deploy it instantly with zero configuration!

### Option B: Via Vercel CLI
If you have Node.js and the Vercel CLI installed:
```bash
npm i -g vercel
vercel
```
Follow the prompts to deploy in seconds.

---

## 💻 Local Preview in VS Code

1. Open this project folder in Visual Studio Code.
2. Install the **Live Server** extension (by Ritwick Dey).
3. Right-click `index.html` in the file explorer and click **"Open with Live Server"**.
4. The application will open in your browser at `http://127.0.0.1:5500/index.html`.

---

## 📱 Android Application Setup

To run the Android app:
1. Open the project folder in **Android Studio** (Ladybug or newer).
2. Sync the project with Gradle files.
3. If you want to use the live Gemini AI model, set your `GEMINI_API_KEY` in the AI Studio Secrets panel or configure `.env`.
4. Run the app on an Android device or emulator (Android 7.0+ / API 24+).
