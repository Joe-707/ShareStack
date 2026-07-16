package com.sharestack.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.sharestack.models.User

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "sharestack.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_USERS = "users"
        private const val COLUMN_ID = "id"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_PASSWORD = "password"
        private const val COLUMN_NAME = "name"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_ID TEXT PRIMARY KEY,
                $COLUMN_USERNAME TEXT NOT NULL UNIQUE,
                $COLUMN_PASSWORD TEXT NOT NULL,
                $COLUMN_NAME TEXT NOT NULL
            )
        """.trimIndent()
        db.execSQL(createTable)

        // ✅ FIXED: Insert default user directly using the db parameter
        insertDefaultUser(db)
    }

    // ✅ FIXED: Accept SQLiteDatabase as parameter instead of calling writableDatabase
    private fun insertDefaultUser(db: SQLiteDatabase) {
        val values = ContentValues().apply {
            put(COLUMN_ID, "u1")
            put(COLUMN_USERNAME, "demo@example.com")
            put(COLUMN_PASSWORD, "password")
            put(COLUMN_NAME, "Demo User")
        }
        db.insert(TABLE_USERS, null, values)
        // ✅ NO db.close() HERE - the database is managed by the caller
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    // ========== DATABASE OPERATIONS ==========

    fun insertUser(id: String, username: String, password: String, name: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ID, id)
            put(COLUMN_USERNAME, username)
            put(COLUMN_PASSWORD, password)
            put(COLUMN_NAME, name)
        }
        val result = db.insert(TABLE_USERS, null, values)
        db.close()
        return result != -1L
    }

    fun getUser(username: String, password: String): User? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_ID, COLUMN_NAME, COLUMN_USERNAME),
            "$COLUMN_USERNAME = ? AND $COLUMN_PASSWORD = ?",
            arrayOf(username, password),
            null, null, null
        )
        return if (cursor.moveToFirst()) {
            val id = cursor.getString(0)
            val name = cursor.getString(1)
            val email = cursor.getString(2)
            cursor.close()
            db.close()
            User(id, name, 0.0, email)
        } else {
            cursor.close()
            db.close()
            null
        }
    }

    fun userExists(username: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_ID),
            "$COLUMN_USERNAME = ?",
            arrayOf(username),
            null, null, null
        )
        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }
}