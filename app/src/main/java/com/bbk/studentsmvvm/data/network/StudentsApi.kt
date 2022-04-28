package com.bbk.studentsmvvm.data.network

import com.bbk.studentsmvvm.models.Admin
import com.bbk.studentsmvvm.models.Students
import com.bbk.studentsmvvm.models.Token
import com.bbk.studentsmvvm.util.UserData
import retrofit2.Response
import retrofit2.http.*

interface StudentsApi {

    //@GET("/StudentsAuthorize/api/IsAdmin")
    @GET("/eihab/api/IsAdmin")
    suspend fun isAdmin(
        @Query("username") username: String,
        @Header("Accept") accept: String = "application/json",
        @Header("Authorization") authorization: String = "Bearer " + UserData.token
    ): Response<Admin>

    @FormUrlEncoded
    //@POST("/StudentsAuthorize/token")
    @POST("/eihab/token")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("grant_type") grant_type: String = "password",
        @Header("Content-Type") contentType: String = "application/x-www-form-urlencoded",
        @Header("Accept") accept: String = "application/json"
    ): Response<Token>

    //@GET("/StudentsAuthorize/api/students")
    @GET("/eihab/api/students")
    suspend fun getAllStudents(
        @Header("Authorization") token: String = "Bearer " + "0qVSCTyFhrN708nJkDvzmdopFfliZhKEb_yO-wRuVBdv79n4zPHYGUhESqoPXojdP1cZFOT32CAu62AwJlyfFuAFGKI6SqqJ5LHSodqGbqSWFovmonfXi-V2tMziFUzxGZcD_zwnVeN_VaDTFo0JUZHkMp0mGdyNz3qxL2QeBCE9zNpdDqU-Vn-ChGWPG3A84ts2oM8BIjvePiJMLH9A57PIyGvH2MBBVBLhsRg_M5in8_DaN1AvnBX4T6MZk20MYHoMOkXhKXEJugD3GNL9wRghBDGH0PPCRSmjrsGzpC8Gj-clRBiYkGcWF9IChT93-GSYSbIsimv-ekc01h_YSX86xdPyv3Qi6na4ScqpMw0y2DoKIn37GRl-2mSyv12rqglB-RVnX5zKOur75Bd0Iiu7utaNBIoirLzsY-lZ-HR9tr-gDN8esLyb12pl_ybel9FPHN3MPuMvmTu7e02tWQefLOtgv6_ecVodjj0otQ7R9GFjGI1SGnOd5XuOqdo3"
    ): Response<Students>

}