package com.bbk.studentsmvvm.data.database

import androidx.room.TypeConverter
import com.bbk.studentsmvvm.models.Student
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class StudentTypeConverter {

    var gson = Gson()

    @TypeConverter
    fun studentToString(student: Student): String {
        return gson.toJson(student)
    }

    @TypeConverter
    fun stringToStudent(data: String): Student {
        val listType = object : TypeToken<Student>() {}.type
        return gson.fromJson(data, listType)
    }
}