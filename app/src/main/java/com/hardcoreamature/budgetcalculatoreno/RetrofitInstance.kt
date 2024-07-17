package com.hardcoreamature.budgetcalculatoreno

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "https://api.kraken.com"

    private val client = OkHttpClient.Builder().build()

    val apiService: KrakenApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(KrakenApiService::class.java)
    }
}
