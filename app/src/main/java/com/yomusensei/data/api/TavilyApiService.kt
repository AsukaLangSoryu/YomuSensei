package com.yomusensei.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface TavilyApiService {

    @POST("search")
    @Headers("Content-Type: application/json")
    suspend fun search(@Body request: TavilySearchRequest): TavilySearchResponse

    companion object {
        fun create(): TavilyApiService {
            return Retrofit.Builder()
                .baseUrl("https://api.tavily.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(TavilyApiService::class.java)
        }
    }
}

data class TavilySearchRequest(
    val api_key: String,
    val query: String,
    val search_depth: String = "basic",
    val max_results: Int = 5,
    val include_domains: List<String>? = null
)

data class TavilySearchResponse(
    val results: List<TavilyResult>
)

data class TavilyResult(
    val title: String,
    val url: String,
    val content: String,
    val score: Double
)
