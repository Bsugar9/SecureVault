package com.example.securedb

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : BaseActivity() {

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var entryAdapter: EntryAdapter
    private lateinit var recyclerView: RecyclerView
    private var dbPassword: ByteArray? = null
    
    private var currentCategory: Category? = null
    private var selectedCategory: Category? = null
    private var selectedEntry: EntryModel? = null
    private var currentSortOrder = "name ASC" // Correct default for Categories

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbPassword = intent.getByteArrayExtra("DB_PASSWORD")
        if (dbPassword == null) {
            finish()
            return
        }

        val bgImage = findViewById<ImageView>(R.id.mainBgImage)
        ThemeManager.applyBackground(bgImage)

        val animationView = findViewById<AnimationView>(R.id.mainAnimationView)
        if (ThemeManager.getAnimationAllScreens(this)) {
            animationView.setAnimation(ThemeManager.getAnimation(this))
        } else {
            animationView.setAnimation("None")
        }

        findViewById<View>(R.id.inputTop).visibility = View.GONE

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        categoryAdapter = CategoryAdapter(emptyList()) { category ->
            currentCategory = category
            selectedCategory = category
            // When moving to entries, reset sort order to valid column
            currentSortOrder = "title ASC"
            refreshList()
        }

        entryAdapter = EntryAdapter(emptyList()) { entry ->
            selectedEntry = entry
            showEntryDetails(entry)
        }

        recyclerView.adapter = categoryAdapter

        findViewById<Button>(R.id.btnAdd).setOnClickListener { 
            if (currentCategory == null) showAddCategoryDialog() else showAddEntryDialog() 
        }
        findViewById<Button>(R.id.btnEdit).setOnClickListener { 
            if (currentCategory == null) {
                Toast.makeText(this, "Select an entry to edit (inside a category)", Toast.LENGTH_SHORT).show()
            } else if (selectedEntry != null) {
                showEditEntryDialog()
            }
        }
        findViewById<Button>(R.id.btnDelete).setOnClickListener { deleteSelected() }
        
        findViewById<Button>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        refreshList()
        applyTheme()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (currentCategory != null) {
                    currentCategory = null
                    selectedEntry = null
                    // Reset to valid category column
                    currentSortOrder = "name ASC"
                    refreshList()
                } else {
                    finish()
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add(0, 1, 0, "Sort Alphabetical")
        menu?.add(0, 2, 0, "Sort Newest First")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            1 -> { currentSortOrder = if (currentCategory == null) "name ASC" else "title ASC" }
            2 -> { currentSortOrder = "id DESC" }
        }
        refreshList()
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        applyTheme()
        val bgImage = findViewById<ImageView>(R.id.mainBgImage)
        ThemeManager.applyBackground(bgImage)
        val animationView = findViewById<AnimationView>(R.id.mainAnimationView)
        if (ThemeManager.getAnimationAllScreens(this)) {
            animationView.setAnimation(ThemeManager.getAnimation(this))
        } else {
            animationView.setAnimation("None")
        }
        refreshList()
    }

    override fun onDestroy() {
        super.onDestroy()
        SecurityUtils.clear(dbPassword)
    }

    private fun applyTheme() {
        ThemeManager.applyThemeToButton(findViewById(R.id.btnAdd))
        ThemeManager.applyThemeToButton(findViewById(R.id.btnEdit))
        ThemeManager.applyThemeToButton(findViewById(R.id.btnDelete))
        ThemeManager.applyThemeToButton(findViewById(R.id.btnSettings))
    }

    private fun showAddCategoryDialog() {
        val dbPass = dbPassword ?: return
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_add_category, null)
        val nameInput = view.findViewById<EditText>(R.id.inputCategoryName)
        ThemeManager.applyThemeToEditText(nameInput)

        builder.setView(view)
        builder.setPositiveButton("Add") { _, _ ->
            val name = nameInput.text.toString()
            if (name.isNotEmpty()) {
                DatabaseHelper(this, dbPass).addCategory(name)
                refreshList()
            }
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun showAddEntryDialog() {
        val dbPass = dbPassword ?: return
        val categoryId = currentCategory?.id ?: return

        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_add_entry, null)
        val titleInput = view.findViewById<EditText>(R.id.inputTitle)
        val valueInput = view.findViewById<EditText>(R.id.inputPassword)
        ThemeManager.applyThemeToEditText(titleInput)
        ThemeManager.applyThemeToEditText(valueInput)

        var selectedIcon = 0
        val icons = listOf<ImageButton>(view.findViewById(R.id.icon0), view.findViewById(R.id.icon1), 
            view.findViewById(R.id.icon2), view.findViewById(R.id.icon3), view.findViewById(R.id.icon4))
        
        icons.forEachIndexed { index, btn ->
            btn.setOnClickListener {
                selectedIcon = index
                icons.forEach { it.setBackgroundColor(Color.TRANSPARENT) }
                btn.setBackgroundColor(Color.LTGRAY)
            }
        }

        builder.setView(view)
        builder.setPositiveButton("Add") { _, _ ->
            DatabaseHelper(this, dbPass).addEntry(categoryId, titleInput.text.toString(), valueInput.text.toString(), selectedIcon)
            refreshList()
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun showEditEntryDialog() {
        val dbPass = dbPassword ?: return
        val entry = selectedEntry ?: return

        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_add_entry, null)
        val titleInput = view.findViewById<EditText>(R.id.inputTitle)
        val valueInput = view.findViewById<EditText>(R.id.inputPassword)
        ThemeManager.applyThemeToEditText(titleInput)
        ThemeManager.applyThemeToEditText(valueInput)

        titleInput.setText(entry.title)
        valueInput.setText(entry.password)

        var selectedIcon = entry.iconId
        val icons = listOf<ImageButton>(view.findViewById(R.id.icon0), view.findViewById(R.id.icon1), 
            view.findViewById(R.id.icon2), view.findViewById(R.id.icon3), view.findViewById(R.id.icon4))
        
        icons[selectedIcon].setBackgroundColor(Color.LTGRAY)
        icons.forEachIndexed { index, btn ->
            btn.setOnClickListener {
                selectedIcon = index
                icons.forEach { it.setBackgroundColor(Color.TRANSPARENT) }
                btn.setBackgroundColor(Color.LTGRAY)
            }
        }

        builder.setView(view)
        builder.setPositiveButton("Save") { _, _ ->
            DatabaseHelper(this, dbPass).updateEntry(entry.id, titleInput.text.toString(), valueInput.text.toString(), selectedIcon)
            refreshList()
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun showEntryDetails(entry: EntryModel) {
        val dbPass = dbPassword ?: return
        
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_verify_password, null)
        val verifyInput = view.findViewById<EditText>(R.id.verifyPasswordInput)
        ThemeManager.applyThemeToEditText(verifyInput)

        builder.setView(view)
        builder.setPositiveButton("Reveal") { _, _ ->
            val input = verifyInput.text.toString()
            val inputBytes = input.toByteArray()
            
            if (inputBytes.contentEquals(dbPass)) {
                showRevealDialog(entry)
            } else {
                Toast.makeText(this, "Incorrect Master Password", Toast.LENGTH_SHORT).show()
            }
            SecurityUtils.clear(inputBytes)
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun showRevealDialog(entry: EntryModel) {
        val detailView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(64, 32, 64, 32)
        }
        
        val passText = TextView(this).apply {
            text = "Password: ${entry.password}"
            ThemeManager.applyFontToView(this)
        }
        detailView.addView(passText)

        AlertDialog.Builder(this)
            .setTitle(entry.title)
            .setView(detailView)
            .setPositiveButton("Copy") { _, _ ->
                copyToClipboard(entry.password)
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Vault Password", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Password copied! Will clear in 30s.", Toast.LENGTH_SHORT).show()

        Handler(Looper.getMainLooper()).postDelayed({
            if (clipboard.primaryClip?.getItemAt(0)?.text == text) {
                clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
                Toast.makeText(this, "Clipboard cleared for security", Toast.LENGTH_SHORT).show()
            }
        }, 30000)
    }

    private fun refreshList() {
        val dbPass = dbPassword ?: return
        val helper = DatabaseHelper(this, dbPass)
        try {
            if (currentCategory == null) {
                val categories = helper.getAllCategories(currentSortOrder)
                categoryAdapter.updateData(categories)
                recyclerView.adapter = categoryAdapter
                supportActionBar?.title = "Categories"
            } else {
                val entries = helper.getEntriesForCategory(currentCategory!!.id, currentSortOrder)
                entryAdapter.updateData(entries)
                recyclerView.adapter = entryAdapter
                supportActionBar?.title = currentCategory!!.name
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading data: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun deleteSelected() {
        val dbPass = dbPassword ?: return
        if (currentCategory == null && selectedCategory != null) {
            DatabaseHelper(this, dbPass).deleteCategory(selectedCategory!!.id)
            selectedCategory = null
            refreshList()
        } else if (currentCategory != null && selectedEntry != null) {
            DatabaseHelper(this, dbPass).deleteEntry(selectedEntry!!.id)
            selectedEntry = null
            refreshList()
        } else {
            Toast.makeText(this, "Select an item to delete", Toast.LENGTH_SHORT).show()
        }
    }
}
