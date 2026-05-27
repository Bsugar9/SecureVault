package com.example.securedb

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner

class VaultApplication : Application() {

    companion object {
        var shouldLock = false
    }

    override fun onCreate() {
        super.onCreate()
        
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) {
                // App went to background
                if (ThemeManager.getAutoLock(this@VaultApplication)) {
                    shouldLock = true
                }
            }
        })
    }
}
