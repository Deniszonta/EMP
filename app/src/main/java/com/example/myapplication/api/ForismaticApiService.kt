package com.example.myapplication.api

import retrofit2.http.GET
import retrofit2.http.Query

data class ForismaticQuote(
    val quoteText: String,
    val quoteAuthor: String
)

interface ForismaticApiService {
    @GET("?method=getQuote&format=json&lang=en")
    suspend fun getQuote(): ForismaticQuote
}
