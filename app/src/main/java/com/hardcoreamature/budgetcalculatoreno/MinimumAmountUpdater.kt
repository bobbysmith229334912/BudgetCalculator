package com.hardcoreamature.budgetcalculatoreno

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MinimumAmountUpdater(
    private val krakenApiService: KrakenApiService,
    private val db: FirebaseFirestore
) {
    fun updateAllMinimumOrderSizes() {
        krakenApiService.getAssetPairs().enqueue(object : Callback<AssetPairsResponse> {
            override fun onResponse(call: Call<AssetPairsResponse>, response: Response<AssetPairsResponse>) {
                if (response.isSuccessful) {
                    val result = response.body()?.result
                    if (result != null) {
                        for ((pair, assetPair) in result) {
                            val minimumOrderSize = assetPair.ordermin?.toDoubleOrNull()
                            if (minimumOrderSize != null) {
                                storeMinimumOrderSize(pair, minimumOrderSize)
                            } else {
                                Log.e("MinimumAmountUpdater", "Failed to parse minimum order size for $pair.")
                            }
                        }
                    } else {
                        Log.e("MinimumAmountUpdater", "Asset pairs result is null.")
                    }
                } else {
                    Log.e("MinimumAmountUpdater", "Failed to fetch asset pairs: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<AssetPairsResponse>, t: Throwable) {
                Log.e("MinimumAmountUpdater", "API call failed: ${t.message}")
            }
        })
    }

    private fun storeMinimumOrderSize(pair: String, minimumOrderSize: Double) {
        val data = hashMapOf("minimumOrderSize" to minimumOrderSize)
        db.collection("minimumOrderSizes").document(pair)
            .set(data)
            .addOnSuccessListener {
                Log.d("MinimumAmountUpdater", "Minimum order size for $pair updated successfully.")
            }
            .addOnFailureListener { e ->
                Log.e("MinimumAmountUpdater", "Failed to update minimum order size for $pair: $e")
            }
    }
}


