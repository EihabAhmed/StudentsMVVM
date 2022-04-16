package com.bbk.studentsmvvm.data

import com.bbk.studentsmvvm.data.network.StudentsApi
import com.bbk.studentsmvvm.models.Students
import retrofit2.Response
import javax.inject.Inject

class RemoteDataSource @Inject constructor(
    private val studentsApi: StudentsApi
) {

    suspend fun getAllStudents(): Response<Students> {
        return studentsApi.getAllStudents()
    }
}