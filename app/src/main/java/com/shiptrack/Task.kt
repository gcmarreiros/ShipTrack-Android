package com.shiptrack

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "tasks")
@TypeConverters(Converters::class)
data class Task(
    @PrimaryKey val id: String,
    val title: String,
    val type: String,
    val zone: String,
    val zones: List<String> = emptyList(),
    val priority: String,
    val status: String,
    val due: String = "",
    val ref: String = "",
    val notes: String = "",
    val photos: List<String> = emptyList(),
    val created: Long = System.currentTimeMillis(),
    val createdTs: Long = System.currentTimeMillis()
)

class Converters {
    private val gson = Gson()
    @TypeConverter
    fun fromStringList(value: List<String>?): String = gson.toJson(value ?: emptyList<String>())
    @TypeConverter
    fun toStringList(value: String): List<String> = gson.fromJson(value, object : TypeToken<List<String>>() {}.type) ?: emptyList()
}
