package com.bbk.studentsmvvm.data.network

import com.bbk.studentsmvvm.models.Students
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface StudentsApi {

    @GET("/eihab/api/students")
    suspend fun getAllStudents(
        @Header("Authorization") token: String = "Bearer " + "C6OP_6WSl03Vz-zcGWqr2qu1KgxwF6f3VQ5B3Lpsh0ozMzYyEXQZKeN0CfRf9TPEmhFZQ_5YiJKHqlIKQFooRwGU0O3s2ZeYoKACe28HtYvUqnZljYBFHWEqh6lYceM6Xi5yAAn7AINsXK8GS4vWA5rsGrxOS_w39HJrzXuRovhGr-aFKJwEzIy9gkMoOvZZI-XQKCDlXRii1GHPt_vxZPv9sWso4sQoKIiVL5y8-Nh-2ZtHijEWt594UNSWjWfpPmL1ErTE8Gg53TxYCYQbalF76E-yG4a8gNWOYDM38-YTa_PMT-HeKf7g27P-Y5tJStJxEeO9_Q9i112H4YCXVVfZoH4KgJNTDrEK9d709X20GIV2wtyZLupASxAO9szTpoUaU2wOiJfSDbCLXJhCjvDtfahbGZVe4g0i1hgMijAis7cenVpo_4sxKP0EzcfMv6NE21tEhbAoI918l53g0YRnYx-FQ-npG6LP2FpYs3o1MC2tA2Mw-nLagbRBhnlk"
    ): Response<Students>

}