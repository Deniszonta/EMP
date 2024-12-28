package com.example.myapplication.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ForismaticApi {
    private const val BASE_URL = "https://api.forismatic.com/api/1.0/"

    val service: ForismaticApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ForismaticApiService::class.java)
    }
}
