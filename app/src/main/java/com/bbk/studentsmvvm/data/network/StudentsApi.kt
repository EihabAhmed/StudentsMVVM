package com.bbk.studentsmvvm.data.network

import com.bbk.studentsmvvm.models.Admin
import com.bbk.studentsmvvm.models.Student
import com.bbk.studentsmvvm.models.Students
import com.bbk.studentsmvvm.models.Token
import com.bbk.studentsmvvm.util.UserData
import retrofit2.Response
import retrofit2.http.*

interface StudentsApi {

    @GET("/StudentsAuthorize/api/IsAdmin")
    //@GET("/eihab/api/IsAdmin")
    suspend fun isAdmin(
        @Query("username") username: String,
        @Header("Accept") accept: String = "application/json",
        @Header("Authorization") authorization: String = "Bearer " + UserData.token
    ): Response<Admin>

    @FormUrlEncoded
    @POST("/StudentsAuthorize/token")
    //@POST("/eihab/token")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("grant_type") grant_type: String = "password",
        @Header("Content-Type") contentType: String = "application/x-www-form-urlencoded",
        @Header("Accept") accept: String = "application/json"
    ): Response<Token>

    @GET("/StudentsAuthorize/api/students")
    //@GET("/eihab/api/students")
    suspend fun getAllStudents(
        @Header("Authorization") token: String = "Bearer " + UserData.token
    ): Response<Students>

    @POST("/StudentsAuthorize/api/students")
    //@POST("/eihab/api/students")
    suspend fun addStudent(
        @Body student: Student,
        @Header("Authorization") token: String = "Bearer " + UserData.token
    ): Response<Student>

    @PUT("/StudentsAuthorize/api/students")
    //@PUT("/eihab/api/students")
    suspend fun updateStudent(
        @Query("id") id: Int,
        @Body student: Student,
        @Header("Authorization") token: String = "Bearer " + UserData.token
    ): Response<Student>

    @DELETE("/StudentsAuthorize/api/students")
    //@DELETE("/eihab/api/students")
    suspend fun deleteStudent(
        @Query("id") id: Int,
        @Header("Authorization") token: String = "Bearer " + UserData.token
    ): Response<Unit>
}