package com.bbk.studentsmvvm.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.bbk.studentsmvvm.data.database.entities.StudentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentsDao {

    @Insert//(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(studentEntity: StudentEntity)

    @Query("SELECT * FROM student_table ORDER BY id ASC")
    fun readStudents(): Flow<List<StudentEntity>>

    @Delete
    suspend fun deleteStudent(studentEntity: StudentEntity)

    @Query("DELETE FROM student_table")
    suspend fun deleteAllStudents()
}