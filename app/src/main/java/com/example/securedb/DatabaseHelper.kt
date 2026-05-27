package com.example.securedb

import android.content.ContentValues
import android.content.Context
import net.zetetic.database.sqlcipher.SQLiteConnection
import net.zetetic.database.sqlcipher.SQLiteDatabase
import net.zetetic.database.sqlcipher.SQLiteDatabaseHook
import net.zetetic.database.sqlcipher.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.*

class DatabaseHelper(context: Context, dbPass: ByteArray) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    dbPass,
    null,
    DATABASE_VERSION,
    DATABASE_VERSION,
    null,
    object : SQLiteDatabaseHook {
        override fun preKey(connection: SQLiteConnection?) {}
        override fun postKey(connection: SQLiteConnection?) {
            connection?.execute("PRAGMA kdf_iter = 64000;", null, null)
            connection?.execute("PRAGMA cipher_memory_security = ON;", null, null)
        }
    },
    false
) {

    companion object {
        private const val DATABASE_NAME = "secure_vault.db"
        private const val DATABASE_VERSION = 4 // Bump for logs table
        
        private const val TABLE_CATEGORIES = "categories"
        private const val COLUMN_CAT_ID = "id"
        private const val COLUMN_CAT_NAME = "name"

        private const val TABLE_ENTRIES = "vault_entries"
        private const val COLUMN_ENTRY_ID = "id"
        private const val COLUMN_ENTRY_CAT_ID = "category_id"
        private const val COLUMN_ENTRY_TITLE = "title"
        private const val COLUMN_ENTRY_PASSWORD = "password"
        private const val COLUMN_ENTRY_ICON = "icon_id"

        private const val TABLE_LOGS = "security_logs"
        private const val COLUMN_LOG_ID = "id"
        private const val COLUMN_LOG_EVENT = "event"
        private const val COLUMN_LOG_TIME = "timestamp"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE $TABLE_CATEGORIES ($COLUMN_CAT_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_CAT_NAME TEXT)")
        db.execSQL("CREATE TABLE $TABLE_ENTRIES ($COLUMN_ENTRY_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_ENTRY_CAT_ID INTEGER, $COLUMN_ENTRY_TITLE TEXT, $COLUMN_ENTRY_PASSWORD TEXT, $COLUMN_ENTRY_ICON INTEGER DEFAULT 0)")
        db.execSQL("CREATE TABLE $TABLE_LOGS ($COLUMN_LOG_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_LOG_EVENT TEXT, $COLUMN_LOG_TIME TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE $TABLE_ENTRIES ADD COLUMN $COLUMN_ENTRY_ICON INTEGER DEFAULT 0")
        }
        if (oldVersion < 4) {
            db.execSQL("CREATE TABLE $TABLE_LOGS ($COLUMN_LOG_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_LOG_EVENT TEXT, $COLUMN_LOG_TIME TEXT)")
        }
    }

    // Logging
    fun addLog(event: String) {
        val db = this.writableDatabase
        val values = ContentValues()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        values.put(COLUMN_LOG_EVENT, event)
        values.put(COLUMN_LOG_TIME, sdf.format(Date()))
        db.insert(TABLE_LOGS, null, values)
        db.close()
        this.close()
    }

    fun getLogs(): List<String> {
        val list = mutableListOf<String>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_LOGS ORDER BY $COLUMN_LOG_ID DESC LIMIT 20", null)
        if (cursor.moveToFirst()) {
            do {
                val event = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOG_EVENT))
                val time = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOG_TIME))
                list.add("[$time] $event")
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        this.close()
        return list
    }

    // Category Operations
    fun addCategory(name: String) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_CAT_NAME, name)
        db.insert(TABLE_CATEGORIES, null, values)
        db.close()
        this.close()
    }

    fun getAllCategories(sortBy: String = "name ASC"): List<Category> {
        val list = mutableListOf<Category>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_CATEGORIES ORDER BY $sortBy", null)
        if (cursor.moveToFirst()) {
            do {
                list.add(Category(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CAT_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CAT_NAME))
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        this.close()
        return list
    }

    fun deleteCategory(id: Int) {
        val db = this.writableDatabase
        db.delete(TABLE_ENTRIES, "$COLUMN_ENTRY_CAT_ID=?", arrayOf(id.toString()))
        db.delete(TABLE_CATEGORIES, "$COLUMN_CAT_ID=?", arrayOf(id.toString()))
        db.close()
        this.close()
    }

    fun addEntry(categoryId: Int, title: String, password: String, iconId: Int = 0) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_ENTRY_CAT_ID, categoryId)
        values.put(COLUMN_ENTRY_TITLE, title)
        values.put(COLUMN_ENTRY_PASSWORD, password)
        values.put(COLUMN_ENTRY_ICON, iconId)
        db.insert(TABLE_ENTRIES, null, values)
        db.close()
        this.close()
    }

    fun getEntriesForCategory(categoryId: Int, sortBy: String = "title ASC"): List<EntryModel> {
        val list = mutableListOf<EntryModel>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_ENTRIES WHERE $COLUMN_ENTRY_CAT_ID = ? ORDER BY $sortBy", arrayOf(categoryId.toString()))
        if (cursor.moveToFirst()) {
            do {
                list.add(EntryModel(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ENTRY_ID)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ENTRY_CAT_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENTRY_TITLE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENTRY_PASSWORD)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ENTRY_ICON))
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        this.close()
        return list
    }

    fun updateEntry(id: Int, title: String, password: String, iconId: Int = 0) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_ENTRY_TITLE, title)
        values.put(COLUMN_ENTRY_PASSWORD, password)
        values.put(COLUMN_ENTRY_ICON, iconId)
        db.update(TABLE_ENTRIES, values, "$COLUMN_ENTRY_ID=?", arrayOf(id.toString()))
        db.close()
        this.close()
    }

    fun deleteEntry(id: Int) {
        val db = this.writableDatabase
        db.delete(TABLE_ENTRIES, "$COLUMN_ENTRY_ID=?", arrayOf(id.toString()))
        db.close()
        this.close()
    }
}
