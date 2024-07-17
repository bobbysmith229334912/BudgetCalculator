package com.hardcoreamature.budgetcalculatoreno

import TickerResponse
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface KrakenApiService {

    @FormUrlEncoded
    @POST("/0/private/AddOrder")
    fun executeTrade(
        @Header("API-Key") apiKey: String,
        @Header("API-Sign") apiSign: String,
        @Field("nonce") nonce: String,
        @Field("ordertype") ordertype: String,
        @Field("type") type: String,
        @Field("volume") volume: String,
        @Field("pair") pair: String
    ): Call<TradeResponse>

    @FormUrlEncoded
    @POST("/0/private/Balance")
    fun getBalance(
        @Header("API-Key") apiKey: String,
        @Header("API-Sign") apiSign: String,
        @Field("nonce") nonce: String
    ): Call<BalanceResponse>

    @GET("/0/public/AssetPairs")
    fun getAssetPairs(): Call<AssetPairsResponse>

    @GET("/0/public/Ticker")
    fun getTickerInformation(
        @Query("pair") pair: String
    ): Call<TickerResponse>
}
