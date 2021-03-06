package com.bbk.studentsmvvm.data.network

import com.bbk.studentsmvvm.models.*
import com.bbk.studentsmvvm.util.Constants.Companion.SUB_DOMAIN
import com.bbk.studentsmvvm.util.UserData
import retrofit2.Response
import retrofit2.http.*

interface StudentsApi {



    //@GET("/StudentsAuthorize/api/IsAdmin")
    //@GET("/eihab/api/IsAdmin")
    @GET("$SUB_DOMAIN/api/IsAdmin")
    suspend fun isAdmin(
        @Query("username") username: String,
        @Header("Accept") accept: String = "application/json",
        @Header("Authorization") authorization: String = "Bearer " + UserData.token
    ): Response<Admin>

    @FormUrlEncoded
    //@POST("/StudentsAuthorize/token")
    //@POST("/eihab/token")
    @POST("$SUB_DOMAIN/token")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("grant_type") grant_type: String = "password",
        @Header("Content-Type") contentType: String = "application/x-www-form-urlencoded",
        @Header("Accept") accept: String = "application/json"
    ): Response<Token>

    //@POST("/StudentsAuthorize/api/account/register")
    //@POST("/eihab/api/account/register")
    @POST("$SUB_DOMAIN/api/account/register")
    suspend fun register(
        @Body registerModel: RegisterModel
    ): Response<Unit>

    //@GET("/StudentsAuthorize/api/students")
    //@GET("/eihab/api/students")
    @GET("$SUB_DOMAIN/api/students")
    suspend fun getAllStudents(
        @Header("Authorization") token: String = "Bearer " + UserData.token
    ): Response<Students>

    //@POST("/StudentsAuthorize/api/students")
    //@POST("/eihab/api/students")
    @POST("$SUB_DOMAIN/api/students")
    suspend fun addStudent(
        @Body student: Student,
        @Header("Authorization") token: String = "Bearer " + UserData.token
    ): Response<Student>

    //@PUT("/StudentsAuthorize/api/students")
    //@PUT("/eihab/api/students")
    @PUT("$SUB_DOMAIN/api/students")
    suspend fun updateStudent(
        @Query("id") id: Int,
        @Body student: Student,
        @Header("Authorization") token: String = "Bearer " + UserData.token
    ): Response<Student>

    //@DELETE("/StudentsAuthorize/api/students")
    //@DELETE("/eihab/api/students")
    @DELETE("$SUB_DOMAIN/api/students")
    suspend fun deleteStudent(
        @Query("id") id: Int,
        @Header("Authorization") token: String = "Bearer " + UserData.token
    ): Response<Unit>

    //@DELETE("/StudentsAuthorize/api/students/DeleteAllStudents")
    //@DELETE("/eihab/api/students/DeleteAllStudents")
    @DELETE("$SUB_DOMAIN/api/students/DeleteAllStudents")
    suspend fun deleteAllStudents(
        @Header("Authorization") token: String = "Bearer " + UserData.token
    ): Response<Unit>

    //@HTTP(method = "DELETE", path = "/StudentsAuthorize/api/students/DeleteStudents", hasBody = true)
    //@HTTP(method = "DELETE", path = "/eihab/api/students/DeleteStudents", hasBody = true)
    @HTTP(method = "DELETE", path = "$SUB_DOMAIN/api/students/DeleteStudents", hasBody = true)
    suspend fun deleteSelectedStudents(
        @Body deleteStudentsModel: DeleteStudentsModel,
        @Header("Authorization") token: String = "Bearer " + UserData.token
    ): Response<Unit>
}