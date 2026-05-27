package com.example.securedb

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.google.android.material.card.MaterialCardView
import java.io.FileInputStream

class SettingsActivity : BaseActivity() {

    private var currentBtnHue = 200
    private var currentBtnSat = 80
    private var currentBtnVal = 90
    private var currentBorderHue = 0
    private var currentBorderSat = 70
    private var currentBorderVal = 100
    private var currentGlow = 10
    private var currentRadius = 12
    private var currentFontSize = 16
    private var currentFontType = "DEFAULT"
    private var currentAnimation = "None"
    private var currentAnimAll = false
    private var selectedBgUri: String? = null
    private var currentScaleType = "CENTER_CROP"

    private lateinit var previewButton: Button
    private lateinit var previewEditText: EditText
    private lateinit var previewCard: MaterialCardView
    private lateinit var previewCardText: TextView
    private lateinit var previewAnimation: AnimationView

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            contentResolver.takePersistableUriPermission(it, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            selectedBgUri = it.toString()
            updatePreview()
        }
    }

    private val exportVaultLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri: Uri? ->
        uri?.let { exportVault(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        previewButton = findViewById(R.id.previewButton)
        previewEditText = findViewById(R.id.previewEditText)
        previewCard = findViewById(R.id.previewCard)
        previewCardText = findViewById(R.id.previewCardText)
        previewAnimation = findViewById(R.id.previewAnimation)

        val seekBtnHue = findViewById<SeekBar>(R.id.seekBtnHue)
        val seekBtnSat = findViewById<SeekBar>(R.id.seekBtnSat)
        val seekBtnVal = findViewById<SeekBar>(R.id.seekBtnVal)
        val seekBorderHue = findViewById<SeekBar>(R.id.seekBorderHue)
        val seekBorderSat = findViewById<SeekBar>(R.id.seekBorderSat)
        val seekBorderVal = findViewById<SeekBar>(R.id.seekBorderVal)
        val seekGlow = findViewById<SeekBar>(R.id.seekGlow)
        val seekRadius = findViewById<SeekBar>(R.id.seekRadius)
        val seekFontSize = findViewById<SeekBar>(R.id.seekFontSize)
        val spinnerFont = findViewById<Spinner>(R.id.spinnerFont)
        val spinnerAnim = findViewById<Spinner>(R.id.spinnerAnimation)
        val spinnerScale = findViewById<Spinner>(R.id.spinnerScaleType)
        val checkAnimAll = findViewById<CheckBox>(R.id.checkAnimAll)
        val checkPreventScreenshots = findViewById<CheckBox>(R.id.checkPreventScreenshots)
        val checkBiometrics = findViewById<CheckBox>(R.id.checkBiometrics)
        val checkAutoLock = findViewById<CheckBox>(R.id.checkAutoLock)
        val btnSelectImg = findViewById<Button>(R.id.btnSelectImage)
        val btnViewLogs = findViewById<Button>(R.id.btnViewLogs)
        val btnExport = findViewById<Button>(R.id.btnExportVault)
        val btnSave = findViewById<Button>(R.id.btnSaveTheme)

        // Load current values
        currentBtnHue = ThemeManager.getBtnHue(this)
        currentBtnSat = ThemeManager.getBtnSat(this)
        currentBtnVal = ThemeManager.getBtnVal(this)
        currentBorderHue = ThemeManager.getBorderHue(this)
        currentBorderSat = ThemeManager.getBorderSat(this)
        currentBorderVal = ThemeManager.getBorderVal(this)
        currentGlow = ThemeManager.getGlow(this)
        currentRadius = ThemeManager.getRadius(this)
        currentFontSize = ThemeManager.getFontSize(this)
        currentFontType = ThemeManager.getFontType(this)
        currentAnimation = ThemeManager.getAnimation(this)
        currentAnimAll = ThemeManager.getAnimationAllScreens(this)
        selectedBgUri = ThemeManager.getBgImageUri(this)
        currentScaleType = ThemeManager.getBgScaleType(this)

        seekBtnHue.progress = currentBtnHue
        seekBtnSat.progress = currentBtnSat
        seekBtnVal.progress = currentBtnVal
        seekBorderHue.progress = currentBorderHue
        seekBorderSat.progress = currentBorderSat
        seekBorderVal.progress = currentBorderVal
        seekGlow.progress = currentGlow
        seekRadius.progress = currentRadius
        seekFontSize.progress = currentFontSize
        checkAnimAll.isChecked = currentAnimAll
        checkPreventScreenshots.isChecked = ThemeManager.getPreventScreenshots(this)
        checkBiometrics.isChecked = ThemeManager.getBiometricsEnabled(this)
        checkAutoLock.isChecked = ThemeManager.getAutoLock(this)

        setupSpinner(spinnerScale, arrayOf("CENTER_CROP", "FIT_CENTER", "FIT_XY", "CENTER_INSIDE"), currentScaleType) { currentScaleType = it }
        setupSpinner(spinnerFont, arrayOf("DEFAULT", "MONOSPACE", "SERIF", "SANS_SERIF"), currentFontType) { currentFontType = it }
        setupSpinner(spinnerAnim, arrayOf("None", "Fireworks", "Digital Rain", "Bolt Lightning", "Starfield", "Lava", "Tornado", "Wormhole", "Northern Lights"), currentAnimation) { 
            currentAnimation = it
            previewAnimation.setAnimation(it)
        }

        btnSelectImg.setOnClickListener { pickImageLauncher.launch("image/*") }
        btnViewLogs.setOnClickListener { showLogs() }
        btnExport.setOnClickListener { exportVaultLauncher.launch("secure_vault_backup.db") }

        updatePreview()

        seekBtnHue.setOnSeekBarChangeListener(createListener { currentBtnHue = it })
        seekBtnSat.setOnSeekBarChangeListener(createListener { currentBtnSat = it })
        seekBtnVal.setOnSeekBarChangeListener(createListener { currentBtnVal = it })
        seekBorderHue.setOnSeekBarChangeListener(createListener { currentBorderHue = it })
        seekBorderSat.setOnSeekBarChangeListener(createListener { currentBorderSat = it })
        seekBorderVal.setOnSeekBarChangeListener(createListener { currentBorderVal = it })
        seekGlow.setOnSeekBarChangeListener(createListener { currentGlow = it })
        seekRadius.setOnSeekBarChangeListener(createListener { currentRadius = it })
        seekFontSize.setOnSeekBarChangeListener(createListener { currentFontSize = it })

        previewButton.setOnClickListener { showColorCodeDialog() }

        btnSave.setOnClickListener {
            ThemeManager.saveTheme(this, currentBtnHue, currentBtnSat, currentBtnVal, currentBorderHue, currentBorderSat, currentBorderVal, currentGlow, currentRadius, currentAnimation, checkAnimAll.isChecked, currentFontType, currentFontSize, selectedBgUri, currentScaleType)
            ThemeManager.saveSecuritySettings(this, checkPreventScreenshots.isChecked, checkBiometrics.isChecked, checkAutoLock.isChecked)
            ThemeManager.applySecurityFlags(this)
            Toast.makeText(this, "Settings Saved!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun showLogs() {
        val pass = intent.getByteArrayExtra("DB_PASSWORD") ?: return
        val logs = DatabaseHelper(this, pass).getLogs()
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, logs)
        
        AlertDialog.Builder(this)
            .setTitle("Security Event History")
            .setAdapter(adapter, null)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun exportVault(destUri: Uri) {
        try {
            val dbFile = getDatabasePath("secure_vault.db")
            contentResolver.openOutputStream(destUri)?.use { output ->
                FileInputStream(dbFile).use { input ->
                    input.copyTo(output)
                }
            }
            Toast.makeText(this, "Vault Exported Successfully", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Export Failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupSpinner(spinner: Spinner, items: Array<String>, current: String, onSelect: (String) -> Unit) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.setSelection(items.indexOf(current))
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                onSelect(items[position])
                updatePreview()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun showColorCodeDialog() {
        val input = EditText(this)
        input.hint = "#RRGGBB"
        AlertDialog.Builder(this)
            .setTitle("Set Button Hue")
            .setView(input)
            .setPositiveButton("Apply") { _, _ ->
                try {
                    val color = Color.parseColor(input.text.toString())
                    val hsv = FloatArray(3)
                    Color.colorToHSV(color, hsv)
                    currentBtnHue = hsv[0].toInt()
                    currentBtnSat = (hsv[1] * 100).toInt()
                    currentBtnVal = (hsv[2] * 100).toInt()
                    
                    findViewById<SeekBar>(R.id.seekBtnHue).progress = currentBtnHue
                    findViewById<SeekBar>(R.id.seekBtnSat).progress = currentBtnSat
                    findViewById<SeekBar>(R.id.seekBtnVal).progress = currentBtnVal
                    
                    updatePreview()
                } catch (e: Exception) {
                    Toast.makeText(this, "Invalid Code", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun createListener(update: (Int) -> Unit) = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(p0: SeekBar?, progress: Int, p2: Boolean) {
            update(progress)
            updatePreview()
        }
        override fun onStartTrackingTouch(p0: SeekBar?) {}
        override fun onStopTrackingTouch(p0: SeekBar?) {}
    }

    private fun updatePreview() {
        ThemeManager.applyThemeToButton(previewButton, currentBtnHue, currentBtnSat, currentBtnVal, currentGlow, currentRadius, currentFontType, currentFontSize)
        ThemeManager.applyThemeToEditText(previewEditText, currentBorderHue, currentBorderSat, currentBorderVal, currentGlow, currentRadius, currentFontType, currentFontSize)
        ThemeManager.applyThemeToCard(previewCard, currentBorderHue, currentBorderSat, currentBorderVal, currentRadius)
        ThemeManager.applyFontToView(previewCardText, currentFontType, currentFontSize)
        
        ThemeManager.applyThemeToButton(findViewById(R.id.btnViewLogs), currentBtnHue, currentBtnSat, currentBtnVal, currentGlow, currentRadius, currentFontType, currentFontSize)
        ThemeManager.applyThemeToButton(findViewById(R.id.btnExportVault), currentBtnHue, currentBtnSat, currentBtnVal, currentGlow, currentRadius, currentFontType, currentFontSize)
        ThemeManager.applyThemeToButton(findViewById(R.id.btnSaveTheme), currentBtnHue, currentBtnSat, currentBtnVal, currentGlow, currentRadius, currentFontType, currentFontSize)
    }
}
