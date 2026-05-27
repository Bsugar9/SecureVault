package com.example.securedb

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

object ThemeManager {
    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_BTN_HUE = "btn_hue"
    private const val KEY_BORDER_HUE = "border_hue"
    private const val KEY_GLOW = "glow"
    private const val KEY_RADIUS = "radius"
    private const val KEY_ANIMATION = "animation_type"
    private const val KEY_ANIMATION_ALL_SCREENS = "animation_all_screens"
    private const val KEY_FONT_TYPE = "font_type"
    private const val KEY_FONT_SIZE = "font_size"
    private const val KEY_BG_IMAGE_URI = "bg_image_uri"
    private const val KEY_BG_SCALE_TYPE = "bg_scale_type"

    // Security Keys
    private const val KEY_PREVENT_SCREENSHOTS = "prevent_screenshots"
    private const val KEY_BIOMETRICS_ENABLED = "biometrics_enabled"
    private const val KEY_AUTO_LOCK = "auto_lock"

    fun saveTheme(context: Context, btnHue: Int, borderHue: Int, glow: Int, radius: Int, animation: String, animAll: Boolean, fontType: String, fontSize: Int, bgUri: String?, bgScale: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putInt(KEY_BTN_HUE, btnHue)
            putInt(KEY_BORDER_HUE, borderHue)
            putInt(KEY_GLOW, glow)
            putInt(KEY_RADIUS, radius)
            putString(KEY_ANIMATION, animation)
            putBoolean(KEY_ANIMATION_ALL_SCREENS, animAll)
            putString(KEY_FONT_TYPE, fontType)
            putInt(KEY_FONT_SIZE, fontSize)
            putString(KEY_BG_IMAGE_URI, bgUri)
            putString(KEY_BG_SCALE_TYPE, bgScale)
            apply()
        }
    }

    fun saveSecuritySettings(context: Context, preventScreenshots: Boolean, biometrics: Boolean, autoLock: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean(KEY_PREVENT_SCREENSHOTS, preventScreenshots)
            putBoolean(KEY_BIOMETRICS_ENABLED, biometrics)
            putBoolean(KEY_AUTO_LOCK, autoLock)
            apply()
        }
    }

    fun getBtnHue(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getInt(KEY_BTN_HUE, 200)
    fun getBorderHue(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getInt(KEY_BORDER_HUE, 0)
    fun getGlow(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getInt(KEY_GLOW, 10)
    fun getRadius(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getInt(KEY_RADIUS, 12)
    fun getAnimation(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_ANIMATION, "None") ?: "None"
    fun getAnimationAllScreens(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(KEY_ANIMATION_ALL_SCREENS, false)
    fun getFontType(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_FONT_TYPE, "DEFAULT") ?: "DEFAULT"
    fun getFontSize(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getInt(KEY_FONT_SIZE, 16)
    fun getBgImageUri(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_BG_IMAGE_URI, null)
    fun getBgScaleType(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_BG_SCALE_TYPE, "CENTER_CROP") ?: "CENTER_CROP"

    fun getPreventScreenshots(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(KEY_PREVENT_SCREENSHOTS, true)
    fun getBiometricsEnabled(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(KEY_BIOMETRICS_ENABLED, false)
    fun getAutoLock(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(KEY_AUTO_LOCK, true)

    fun applyThemeToButton(button: Button, btnHue: Int? = null, glow: Int? = null, radius: Int? = null, fontType: String? = null, fontSize: Int? = null) {
        val context = button.context
        val finalHue = btnHue ?: getBtnHue(context)
        val finalGlow = glow ?: getGlow(context)
        val finalRadius = radius ?: getRadius(context)
        val finalFont = fontType ?: getFontType(context)
        val finalSize = fontSize ?: getFontSize(context)

        val finalColor = Color.HSVToColor(floatArrayOf(finalHue.toFloat(), 0.8f, 0.9f))

        val drawable = GradientDrawable()
        drawable.setColor(finalColor)
        drawable.cornerRadius = finalRadius.toFloat() * 2
        if (finalGlow > 0) {
            drawable.setStroke(finalGlow / 2, finalColor)
        }
        button.background = drawable
        button.setTextColor(Color.WHITE)
        applyFontToView(button, finalFont, finalSize)
    }

    fun applyThemeToEditText(editText: EditText, borderHue: Int? = null, glow: Int? = null, radius: Int? = null, fontType: String? = null, fontSize: Int? = null) {
        val context = editText.context
        val finalHue = borderHue ?: getBorderHue(context)
        val finalGlow = glow ?: getGlow(context)
        val finalRadius = radius ?: getRadius(context)
        val finalFont = fontType ?: getFontType(context)
        val globalSize = fontSize ?: getFontSize(context)
        val finalSize = if (globalSize > 18) 18 else globalSize

        val borderColor = Color.HSVToColor(floatArrayOf(finalHue.toFloat(), 0.7f, 1.0f))

        val drawable = GradientDrawable()
        drawable.setStroke(if (finalGlow > 0) finalGlow / 2 else 2, borderColor)
        drawable.cornerRadius = finalRadius.toFloat()
        editText.background = drawable
        editText.setTextColor(Color.WHITE)
        editText.setPadding(32, 20, 32, 20)
        applyFontToView(editText, finalFont, finalSize)
    }

    fun applyThemeToCard(card: MaterialCardView, borderHue: Int? = null, radius: Int? = null) {
        val context = card.context
        val finalHue = borderHue ?: getBorderHue(context)
        val finalRadius = radius ?: getRadius(context)

        val borderColor = Color.HSVToColor(floatArrayOf(finalHue.toFloat(), 0.7f, 1.0f))

        card.radius = finalRadius.toFloat()
        card.strokeColor = borderColor
        card.strokeWidth = 2
    }

    fun applyBackground(imageView: ImageView) {
        val context = imageView.context
        val uriStr = getBgImageUri(context)
        val scaleTypeStr = getBgScaleType(context)

        if (uriStr != null) {
            try {
                imageView.setImageURI(Uri.parse(uriStr))
                imageView.scaleType = when (scaleTypeStr) {
                    "FIT_CENTER" -> ImageView.ScaleType.FIT_CENTER
                    "FIT_XY" -> ImageView.ScaleType.FIT_XY
                    "CENTER_INSIDE" -> ImageView.ScaleType.CENTER_INSIDE
                    else -> ImageView.ScaleType.CENTER_CROP
                }
                imageView.visibility = android.view.View.VISIBLE
            } catch (e: Exception) {
                imageView.visibility = android.view.View.GONE
            }
        } else {
            imageView.visibility = android.view.View.GONE
        }
    }

    fun applySecurityFlags(activity: AppCompatActivity) {
        if (getPreventScreenshots(activity)) {
            activity.window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    fun getBtnColor(context: Context): Int {
        return Color.HSVToColor(floatArrayOf(getBtnHue(context).toFloat(), 0.8f, 0.9f))
    }

    fun applyFontToView(view: TextView, fontType: String? = null, fontSize: Int? = null) {
        val context = view.context
        val type = fontType ?: getFontType(context)
        val size = fontSize ?: getFontSize(context)

        view.typeface = when (type) {
            "MONOSPACE" -> Typeface.MONOSPACE
            "SERIF" -> Typeface.SERIF
            "SANS_SERIF" -> Typeface.SANS_SERIF
            else -> Typeface.DEFAULT
        }
        view.textSize = size.toFloat()
    }
}
