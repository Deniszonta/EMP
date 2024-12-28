package com.example.myapplication.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class JokeResponse(
    val setup: String?,
    val delivery: String?,
    val joke: String?, // Single-line jokes
    val type: String // "single" or "twopart"
)

interface JokeApiService {
    @GET("joke/Any")
    suspend fun getRandomJoke(
        @Query("type") type: String = "twopart", // twopart for setup/delivery; single for one-liner
        @Query("amount") amount: Int = 1
    ): JokeResponse
}

object JokeApi {
    private const val BASE_URL = "https://v2.jokeapi.dev/"

    val service: JokeApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(JokeApiService::class.java)
    }
}
