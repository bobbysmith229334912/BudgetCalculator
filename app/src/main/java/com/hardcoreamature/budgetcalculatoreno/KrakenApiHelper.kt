package com.hardcoreamature.budgetcalculatoreno

import TickerResponse
import android.content.Context
import android.util.Base64
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.nio.ByteBuffer
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class KrakenApiHelper(
    private val context: Context,
    private val apiKey: String,
    private val apiSecret: String
) {

    private val apiService: KrakenApiService
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    init {
        val logging = HttpLoggingInterceptor().apply { setLevel(HttpLoggingInterceptor.Level.BODY) }
        val httpClient = OkHttpClient.Builder().addInterceptor(logging).build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.kraken.com")
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()

        apiService = retrofit.create(KrakenApiService::class.java)
    }

    fun getCurrentPrice(pair: String, callback: (Double?) -> Unit) {
        apiService.getTickerInformation(pair).enqueue(object : Callback<TickerResponse> {
            override fun onResponse(call: Call<TickerResponse>, response: Response<TickerResponse>) {
                if (response.isSuccessful) {
                    val price = response.body()?.result?.get(pair)?.c?.firstOrNull()?.toDoubleOrNull()
                    callback(price)
                } else {
                    callback(null)
                }
            }

            override fun onFailure(call: Call<TickerResponse>, t: Throwable) {
                callback(null)
            }
        })
    }

    fun getBalances(callback: (Map<String, Double>?, String?) -> Unit) {
        val nonce = System.currentTimeMillis().toString()
        val path = "/0/private/Balance"
        val postData = "nonce=$nonce"
        val signature = generateKrakenApiSignature(path, nonce, postData)

        apiService.getBalance(apiKey, signature, nonce).enqueue(object : Callback<BalanceResponse> {
            override fun onResponse(call: Call<BalanceResponse>, response: Response<BalanceResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        if (it.error.isEmpty()) {
                            val balances = it.result?.mapValues { (_, value) -> value.toDouble() }
                            callback(balances, null)
                        } else {
                            callback(null, it.error.joinToString())
                        }
                    } ?: run {
                        callback(null, "Response body is null")
                    }
                } else {
                    callback(null, "Unsuccessful response: ${response.code()} ${response.message()}")
                }
            }

            override fun onFailure(call: Call<BalanceResponse>, t: Throwable) {
                callback(null, t.message ?: "Unknown error")
            }
        })
    }

    private fun generateKrakenApiSignature(path: String, nonce: String, postData: String): String {
        try {
            val sha256Hasher = MessageDigest.getInstance("SHA-256")
            val noncePostData = nonce + postData
            val hash = sha256Hasher.digest(noncePostData.toByteArray(Charsets.UTF_8))

            val secretDecoded = Base64.decode(apiSecret.trim(), Base64.NO_WRAP)
            val mac = Mac.getInstance("HmacSHA512")
            val secretKey = SecretKeySpec(secretDecoded, "HmacSHA512")
            mac.init(secretKey)

            val pathBytes = path.toByteArray(Charsets.UTF_8)
            val combined = ByteBuffer.allocate(pathBytes.size + hash.size)
            combined.put(pathBytes)
            combined.put(hash)
            val macData = mac.doFinal(combined.array())

            return Base64.encodeToString(macData, Base64.NO_WRAP)
        } catch (e: IllegalArgumentException) {
            Log.e("KrakenApiHelper", "Invalid Base64 encoding: ${e.message}")
            throw e
        }
    }

    fun buyCrypto(pair: String, volume: String, callback: (Boolean, String) -> Unit) {
        val nonce = System.currentTimeMillis().toString()
        val postData = "nonce=$nonce&ordertype=market&type=buy&volume=$volume&pair=$pair"
        val path = "/0/private/AddOrder"
        val signature = generateKrakenApiSignature(path, nonce, postData)

        apiService.executeTrade(apiKey, signature, nonce, "market", "buy", volume, pair)
            .enqueue(object : Callback<TradeResponse> {
                override fun onResponse(call: Call<TradeResponse>, response: Response<TradeResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            if (it.error.isEmpty()) {
                                updateCryptoAmount(pair, volume.toDouble())
                                callback(true, it.result.toString())
                            } else {
                                callback(false, it.error.joinToString())
                            }
                        } ?: run {
                            callback(false, "Response body is null")
                        }
                    } else {
                        callback(false, "Unsuccessful response: ${response.code()} ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<TradeResponse>, t: Throwable) {
                    callback(false, t.message ?: "Unknown error")
                }
            })
    }

    private fun updateCryptoAmount(pair: String, volume: Double) {
        val userId = "user1" // replace with actual user ID
        val cryptoField = when (pair) {
            "XXBTZUSD" -> "BTC"
            "XETHZUSD" -> "ETH"
            "XLTCZUSD" -> "LTC"
            else -> return
        }

        val userCryptoRef = firestore.collection("userCryptoAmounts").document(userId)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(userCryptoRef)
            val currentAmount = snapshot.getDouble(cryptoField) ?: 0.0
            val newAmount = currentAmount + volume
            transaction.set(userCryptoRef, mapOf(cryptoField to newAmount), SetOptions.merge())
        }.addOnSuccessListener {
            Log.d("KrakenApiHelper", "Crypto amount updated successfully")
        }.addOnFailureListener { e ->
            Log.e("KrakenApiHelper", "Error updating crypto amount: ", e)
        }
    }
}
