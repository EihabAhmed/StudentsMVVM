package com.bbk.studentsmvvm.data

import com.bbk.studentsmvvm.data.database.StudentsDao
import com.bbk.studentsmvvm.data.database.entities.StudentEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LocalDataSource @Inject constructor(
    private val studentsDao: StudentsDao
) {

    fun readStudents(): Flow<List<StudentEntity>> {
        return studentsDao.readStudents()
    }

    suspend fun insertStudent(studentEntity: StudentEntity) {
        studentsDao.insertStudent(studentEntity)
    }

//    suspend fun deleteStudent(studentEntity: StudentEntity) {
//        studentsDao.deleteStudent(studentEntity)
//    }

    suspend fun deleteAllStudents() {
        studentsDao.deleteAllStudents()
    }
}