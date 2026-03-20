package com.shiptrack

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Database(entities = [Task::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "shiptrack.db")
                    .build().also { INSTANCE = it }
            }
    }
}

class SettingsStore(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("shiptrack_settings", Context.MODE_PRIVATE)
    private val gson = Gson()
    var categories: List<String>
        get() { val json = prefs.getString("cats", null) ?: return DefaultData.CATEGORIES.toList(); return gson.fromJson(json, object : TypeToken<List<String>>() {}.type) }
        set(value) { prefs.edit().putString("cats", gson.toJson(value)).apply() }
    var zones: List<Zone>
        get() { val json = prefs.getString("zones", null) ?: return DefaultData.ZONES.toList(); return gson.fromJson(json, object : TypeToken<List<Zone>>() {}.type) }
        set(value) { prefs.edit().putString("zones", gson.toJson(value)).apply() }
    var theme: String
        get() = prefs.getString("theme", "bluegrey") ?: "bluegrey"
        set(value) { prefs.edit().putString("theme", value).apply() }
    var seeded: Boolean
        get() = prefs.getBoolean("seeded", false)
        set(value) { prefs.edit().putBoolean("seeded", value).apply() }
}
