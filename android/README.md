# XM CLIENT MOBILE (Android Edition)
### *The Ultimate High-Performance Minecraft Java Client for Android*
**Author:** Mayank Mandrai  
**Target Platform:** Android 8.0+ (ARM64)  
**Package:** `com.xmclient.mobile`

---

## ⚡ Core Features Implemented

### 1. Strictly Locked Versions & Loaders
- **Versions:** `1.21.1` & `1.20.1` only (zero clutter of old/vanilla versions).
- **Mod Loaders:** `Fabric` & `NeoForge` only.

### 2. "XMods" Unified Store (Modrinth + CurseForge)
- Combined real-time search across **Modrinth API v2** and **CurseForge Core v1 REST API**.
- Deduplication engine removes duplicates across both stores.
- Filter by All, Modrinth Only, or CurseForge Only.

### 3. Dependency Inspector & Manual Download
- When viewing any mod, all required and optional libraries (e.g. *Fabric API, Cloth Config, Architecture API*) are clearly listed.
- Each dependency has a **Manual "DOWNLOAD" Button** so the user can download each library individually with full control.

### 4. Smart Duplicate Prevention (Zero Double Installs)
- Scans `.minecraft/mods` directory before listing.
- If a mod or its jar is already in the folder, the button dynamically becomes **`INSTALLED ✓`** (green badge, disabled).
- Prevents multiple copies (e.g., `mod - Copy.jar`, `mod (1).jar`).

### 5. Customizable Virtual Touch Controls & "+ ADD BUTTON"
- Fullscreen on-screen editor with drag-and-drop movement.
- **"+ ADD BUTTON"**: Create custom buttons and bind them to any GLFW / Minecraft key (`ZOOM`, `F3`, `F5`, `CHAT`, `SPACE`, `SHIFT`, `CTRL`, `ATTACK`, `USE`, etc.).
- Adjust button size (`+` / `-`), corner radius, and opacity.
- Save and reset layout profiles (`control_layouts/default_layout.json`).

---

## 🚀 How to Build the Real `.apk` Without Any Heavy IDE (Low PC Method)

Because your PC has low specs and you don't use Android Studio or IntelliJ IDEA, we have added **Automated Cloud CI/CD (GitHub Actions)**:

### Steps to get the `.apk` directly on your phone:
1. Create a free repository on [GitHub](https://github.com/new) (e.g., `xm-client-mobile`).
2. Push this folder to your repository:
   ```bash
   cd C:\Users\Admin\.gemini\antigravity-ide\scratch\xm_client_android
   git init
   git add .
   git commit -m "Initial XM Client Mobile release"
   git branch -M main
   git remote add origin https://github.com/YOUR_USERNAME/xm-client-mobile.git
   git push -u origin main
   ```
3. GitHub Actions will **automatically start building the APK** on a fast cloud server (using `.github/workflows/build-apk.yml`).
4. In ~3 minutes, go to the **Actions** tab on your GitHub repo and download **`XMClient-Mobile-Debug-APK`**.
5. Transfer it to your phone and install!

---

## 🎮 Instant PC Browser Simulator (Test Right Now!)

You can test the complete Android App UI, the XMods store, the dependencies system, and the virtual controls editor right now on your PC:
* Double-click **`launch_simulator.bat`** (or open `preview_simulator/index.html` in Chrome/Edge).
