package com.bbk.studentsmvvm.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bbk.studentsmvvm.data.database.entities.StudentEntity

@Database(
    entities = [StudentEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(StudentTypeConverter::class)
abstract class StudentsDatabase : RoomDatabase() {

    abstract fun studentsDao(): StudentsDao
}