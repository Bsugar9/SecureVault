package com.example.securedb

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor

class LoginActivity : BaseActivity() {

    private var failedAttempts = 0
    private var secretClickCount = 0
    private var volumeSequence = ""
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        System.loadLibrary("sqlcipher")

        if (!databaseExists()) {
            startActivity(Intent(this, SetupActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_login)

        val animationView = findViewById<AnimationView>(R.id.loginAnimationView)
        animationView.setAnimation(ThemeManager.getAnimation(this))

        val bgImage = findViewById<ImageView>(R.id.loginBgImage)
        ThemeManager.applyBackground(bgImage)

        val passInput = findViewById<EditText>(R.id.loginPassInput)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val titleText = findViewById<TextView>(R.id.loginTitle)
        val secretWipe = findViewById<TextView>(R.id.versionLabel)

        ThemeManager.applyThemeToEditText(passInput)
        ThemeManager.applyThemeToButton(btnLogin)
        ThemeManager.applyFontToView(titleText)
        ThemeManager.applyFontToView(secretWipe) // Keep it subtle but themed

        btnLogin.setOnClickListener {
            val password = passInput.text.toString()
            if (password.isNotEmpty()) {
                val passBytes = password.toByteArray()
                if (validatePassword(passBytes)) {
                    savePasswordSecurely(passBytes)
                    DatabaseHelper(this, passBytes).addLog("Successful Password Login")
                    proceedToMain(passBytes)
                } else {
                    // Try to log failure even if we can't open DB? No, need valid pass.
                    // Instead, we just handle the counter.
                    SecurityUtils.clear(passBytes)
                    handleFailedAttempt()
                }
            } else {
                Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show()
            }
        }

        secretWipe.setOnClickListener {
            secretClickCount++
            if (secretClickCount >= 10) { }
        }

        if (ThemeManager.getBiometricsEnabled(this)) {
            setupBiometrics()
            biometricPrompt.authenticate(promptInfo)
        }
    }

    private fun setupBiometrics() {
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    val savedPass = getSavedPasswordSecurely()
                    if (savedPass != null) {
                        DatabaseHelper(this@LoginActivity, savedPass).addLog("Successful Biometric Login")
                        proceedToMain(savedPass)
                    }
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                }
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Unlock")
            .setSubtitle("Unlock your vault using biometric credential")
            .setNegativeButtonText("Use Password")
            .build()
    }

    private fun savePasswordSecurely(password: ByteArray) {
        val prefs = getSharedPreferences("secure_vault_auth", MODE_PRIVATE)
        prefs.edit().putString("saved_pass", android.util.Base64.encodeToString(password, android.util.Base64.DEFAULT)).apply()
    }

    private fun getSavedPasswordSecurely(): ByteArray? {
        val prefs = getSharedPreferences("secure_vault_auth", MODE_PRIVATE)
        val encoded = prefs.getString("saved_pass", null) ?: return null
        return android.util.Base64.decode(encoded, android.util.Base64.DEFAULT)
    }

    private fun proceedToMain(password: ByteArray) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("DB_PASSWORD", password)
        startActivity(intent)
        finish()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (secretClickCount >= 10) {
            when (keyCode) {
                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    volumeSequence = "DOWN"
                    return true
                }
                KeyEvent.KEYCODE_VOLUME_UP -> {
                    if (volumeSequence == "DOWN") {
                        showWipeConfirmation()
                        volumeSequence = ""
                        secretClickCount = 0
                        return true
                    }
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun showWipeConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("CRITICAL ACTION")
            .setMessage("Are You Absolutely Sure You Want To Wipe The Existing Database?")
            .setPositiveButton("YES") { _, _ ->
                wipeDatabase()
                Toast.makeText(this, "Vault Reset", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, SetupActivity::class.java))
                finish()
            }
            .setNegativeButton("NO") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun databaseExists(): Boolean {
        val dbFile = getDatabasePath("secure_vault.db")
        return dbFile != null && dbFile.exists()
    }

    private fun handleFailedAttempt() {
        failedAttempts++
        if (failedAttempts == 4) {
            AlertDialog.Builder(this)
                .setTitle("SECURITY WARNING")
                .setMessage("Final Warning: You have one attempt remaining. If the next password is incorrect, the vault database will be permanently wiped for security.")
                .setPositiveButton("I Understand", null)
                .show()
        } else if (failedAttempts >= 5) {
            wipeDatabase()
            Toast.makeText(this, "Vault wiped due to security breach", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, SetupActivity::class.java))
            finish()
        } else {
            Toast.makeText(this, "Incorrect password. Attempt $failedAttempts/5", Toast.LENGTH_SHORT).show()
        }
    }

    private fun wipeDatabase() {
        val dbFile = getDatabasePath("secure_vault.db")
        if (dbFile != null && dbFile.exists()) {
            dbFile.delete()
        }
        getSharedPreferences("secure_vault_auth", MODE_PRIVATE).edit().clear().apply()
    }

    private fun validatePassword(password: ByteArray): Boolean {
        return try {
            val dbHelper = DatabaseHelper(this, password)
            val db = dbHelper.readableDatabase
            val cursor = db.rawQuery("SELECT count(*) FROM sqlite_master", null)
            cursor.moveToFirst()
            cursor.close()
            db.close()
            dbHelper.close()
            true
        } catch (e: Exception) {
            false
        }
    }
}
