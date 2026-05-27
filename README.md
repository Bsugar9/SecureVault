# Secure Vault v1.0

Secure Vault is a high-security, professional-grade Android application designed to protect your sensitive passwords and data. Built with industry-standard encryption and a fully customizable cinematic UI, it offers both robust protection and a stunning user experience.

## 🛡️ Security Features

- **SQLCipher AES-256 Encryption**: The entire database is encrypted using SQLCipher with a hardened PBKDF2 iteration count (64,000) and active memory security.
- **Biometric Unlock**: Support for Fingerprint and Face Unlock for fast, secure access.
- **Auto-Wipe Protection**: To prevent brute-force attacks, the database is permanently wiped after 5 consecutive failed password attempts (with a critical warning at attempt 4).
- **Anti-Screenshot & Recents Protection**: Prevents sensitive data from being captured via screenshots or seen in the "Recent Apps" switcher.
- **Memory-Safe Passwords**: Master passphrases are handled using zero-able byte arrays that are wiped from memory immediately after use.
- **Clipboard Auto-Clear**: Copied passwords are automatically cleared from the system clipboard after 30 seconds.
- **Local Storage Only**: No cloud backups or data sync. Your data never leaves your physical device.

## 🎨 Cinematic UI & Customization

- **5 Interactive 3D Animations**: Choose from immersive background effects:
  - **Matrix**: Emerald green digital rain with authentic characters.
  - **Starfield**: High-speed "Hyperdrive" light streaks.
  - **Lava**: A cyclical fill, harden, and explode sequence.
  - **Bolt Lightning**: Realistic jagged bolts with atmospheric flashes.
  - **Tornado**: A wandering 3D pixel funnel that pulses and grows.
- **Complete Theme Engine**:
  - Real-time adjustment of **Hue** (Primary & Border colors).
  - **Glow Intensity** and **Corner Roundness** sliders.
  - Custom **Background Image** support with multiple scaling modes.
- **Typography**: Global control over font style (Default, Monospace, Serif, Sans Serif) and font size.
- **Material Design**: Modern card-based layouts and dynamic iconography.

## ⚙️ Advanced Tools

- **Encrypted Export**: Export your entire encrypted database to a secure backup file.
- **Security Event History**: View a timestamped log of successful and failed login attempts.
- **Secret Reset Trigger**: A hidden recovery sequence (10-tap version label + volume key combo) to reset the vault.

## 🚀 Getting Started

### Installation
1. Download the `SecureVault.apk` from the releases section or the phone's Download folder.
2. Open the APK on your Android device and follow the installation prompts.

### First-Time Setup
1. Launch the app to enter the **Vault Setup** screen.
2. Create a strong master passphrase and confirm it.
3. Once initialized, you can enter the Settings (Set) menu to enable Biometrics or customize your theme.

## 🛠️ Tech Stack

- **Language**: Kotlin
- **Database**: SQLCipher for Android (net.zetetic)
- **Lifecycle**: Androidx Process Lifecycle for Auto-Lock logic
- **UI**: Material Components, custom Canvas rendering for 3D animations
- **Build**: R8 Minification and Obfuscation enabled for release

---
*Created by Bsugar9*
