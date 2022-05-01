package com.bbk.studentsmvvm.data

import com.bbk.studentsmvvm.data.network.StudentsApi
import com.bbk.studentsmvvm.models.Admin
import com.bbk.studentsmvvm.models.Student
import com.bbk.studentsmvvm.models.Students
import com.bbk.studentsmvvm.models.Token
import retrofit2.Response
import javax.inject.Inject

class RemoteDataSource @Inject constructor(
    private val studentsApi: StudentsApi
) {

    suspend fun isAdmin(userName: String): Response<Admin> {
        return studentsApi.isAdmin(userName)
    }

    suspend fun login(userName: String, password: String): Response<Token> {
        return studentsApi.login(userName, password)
    }

    suspend fun getAllStudents(): Response<Students> {
        return studentsApi.getAllStudents()
    }

    suspend fun addStudent(student: Student): Response<Student> {
        return studentsApi.addStudent(student)
    }

    suspend fun updateStudent(student: Student): Response<Student> {
        return studentsApi.updateStudent(student.id, student)
    }

    suspend fun deleteStudent(id: Int): Response<Unit> {
        return studentsApi.deleteStudent(id)
    }
}