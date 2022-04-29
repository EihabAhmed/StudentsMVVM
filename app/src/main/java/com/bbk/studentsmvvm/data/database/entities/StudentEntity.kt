package com.bbk.studentsmvvm.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bbk.studentsmvvm.models.Student
import com.bbk.studentsmvvm.util.Constants.Companion.STUDENT_TABLE

@Entity(tableName = STUDENT_TABLE)
class StudentEntity(
    var student: Student
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}