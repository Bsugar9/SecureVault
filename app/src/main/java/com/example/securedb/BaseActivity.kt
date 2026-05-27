package com.example.securedb

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.applySecurityFlags(this)
    }

    override fun onResume() {
        super.onResume()
        ThemeManager.applySecurityFlags(this)
        
        // Auto-lock check: if we were in background and auto-lock is on, go to login
        if (VaultApplication.shouldLock && this !is LoginActivity && this !is SetupActivity) {
            VaultApplication.shouldLock = false
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
