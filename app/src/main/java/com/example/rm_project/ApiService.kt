package com.example.rm_project

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @POST("register")
    fun register(@Body request: RegisterRequest): Call<GenericResponse>

    @POST("login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("documents")
    fun uploadDocument(@Body request: DocumentUploadRequest): Call<DocumentResponse>

    @GET("documents/{username}")
    fun getDocuments(@Path("username") username: String): Call<List<DocumentItem>>
}
