package com.example.whistrentzscorer.utils

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ScoreJsonConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromJson(json: String?): Map<Int, Map<String, Int?>>? {
        if (json == null) {
            return emptyMap()
        }
        val type = object : TypeToken<Map<String, Map<String, Int?>>>() {}.type
        return gson.fromJson(json, type)
    }

}