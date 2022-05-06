package com.bbk.studentsmvvm.data

import com.bbk.studentsmvvm.data.network.StudentsApi
import com.bbk.studentsmvvm.models.*
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

    suspend fun register(registerModel: RegisterModel): Response<Unit> {
        return studentsApi.register(registerModel)
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

    suspend fun deleteAllStudents(): Response<Unit> {
        return studentsApi.deleteAllStudents()
    }

    suspend fun deleteSelectedStudents(deleteStudentsModel: DeleteStudentsModel): Response<Unit> {
        return studentsApi.deleteSelectedStudents(deleteStudentsModel)
    }
}