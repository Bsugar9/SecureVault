package com.example.securedb

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

class SetupActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        System.loadLibrary("sqlcipher")
        setContentView(R.layout.activity_setup)

        val animationView = findViewById<AnimationView>(R.id.setupAnimationView)
        animationView.setAnimation(ThemeManager.getAnimation(this))
        val bgImage = findViewById<ImageView>(R.id.setupBgImage)
        ThemeManager.applyBackground(bgImage)

        val passInput = findViewById<EditText>(R.id.setupPassInput)
        val confirmInput = findViewById<EditText>(R.id.setupConfirmInput)
        val btnCreate = findViewById<Button>(R.id.btnCreateVault)
        val titleText = findViewById<TextView>(R.id.setupTitle)

        ThemeManager.applyThemeToEditText(passInput)
        ThemeManager.applyThemeToEditText(confirmInput)
        ThemeManager.applyThemeToButton(btnCreate)
        ThemeManager.applyFontToView(titleText)

        btnCreate.setOnClickListener {
            val pass = passInput.text.toString()
            val confirm = confirmInput.text.toString()

            if (pass.isEmpty()) {
                Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass != confirm) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val passBytes = pass.toByteArray()
            try {
                val dbHelper = DatabaseHelper(this, passBytes)
                dbHelper.writableDatabase.close()
                dbHelper.close()

                // Save for biometrics if enabled later
                savePasswordSecurely(passBytes)

                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("DB_PASSWORD", passBytes)
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                SecurityUtils.clear(passBytes)
                Toast.makeText(this, "Error initializing vault: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun savePasswordSecurely(password: ByteArray) {
        val prefs = getSharedPreferences("secure_vault_auth", MODE_PRIVATE)
        prefs.edit().putString("saved_pass", android.util.Base64.encodeToString(password, android.util.Base64.DEFAULT)).apply()
    }
}
