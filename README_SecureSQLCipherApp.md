# SecureSQLCipherApp

## Overview
This project provides a secure Android application using:
- Kotlin
- SQLCipher (AES-256 encrypted database)
- Android Keystore (hardware-backed security)
- PBKDF2 password derivation
- Lockout + secure wipe policy
- Dark theme UI

This project is split into TWO ZIP FILES:
1. Base Project (core structure)
2. Extra Files (security, settings, adapters)

---

## Requirements
- Android Studio (Latest version recommended)
- JDK 11 or higher
- Android SDK (API 21+)

---

## Installation Instructions

### Step 1 — Extract Base Project
1. Download the first ZIP (SecureSQLCipherApp.zip)
2. Right-click the ZIP file
3. Select **Extract All**
4. Choose a folder (e.g. Desktop or Documents)

Result:
```
SecureSQLCipherApp/
```

---

### Step 2 — Open in Android Studio
1. Open **Android Studio**
2. Click **Open**
3. Navigate to the extracted folder
4. Select:
```
SecureSQLCipherApp
```
5. Click **OK**

Wait for:
- Gradle sync to complete

---

### Step 3 — Extract Second ZIP (Extension Files)
1. Download the second ZIP (SecureSQLCipherApp_extra.zip)
2. Extract it

You will see files like:
- CryptoManager.kt
- DatabaseHelper.kt
- SettingsActivity.kt
- XML layouts

---

### Step 4 — Copy Files into Project

Copy ALL files into the correct locations:

#### Kotlin Files:
```
app/src/main/java/com/example/securedb/
```

#### XML Layout Files:
```
app/src/main/res/layout/
```

When prompted:
- Click **Replace / Merge**

---

### Step 5 — Sync Project
In Android Studio:
- Click **File → Sync Project with Gradle Files**

Wait for build to finish

---

### Step 6 — Build APK

Go to:
```
Build → Build Bundle(s) / APK(s) → Build APK(s)
```

After build completes:
- Click **Locate**

APK path:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## Running the App

You can run the app:
- On Emulator
- On Physical Device

Click:
```
Run ▶
```

---

## Security Features Included

- SQLCipher AES-256 encrypted DB
- PBKDF2 key derivation
- Salt storage with database
- Android Keystore integration
- 4-attempt lockout
- Automatic secure wipe

---

## UI Features

- Dark theme (default)
- Top input fields
- Bottom action buttons (Add / Edit / Delete)
- Settings screen (theme customization - extendable)

---

## Troubleshooting

### Gradle Sync Fails
- File → Invalidate Caches → Restart

### SQLCipher Issues
- Ensure dependency is present in:
```
build.gradle
```

---

## Next Steps (Optional Enhancements)

- Implement full password vault UI
- Add biometric unlock
- Expand theme engine sliders
- Add RecyclerView listing for entries

---

## Notes

This project is intentionally modular. You can build features step-by-step or request a fully integrated version.

