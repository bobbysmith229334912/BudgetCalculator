package com.hardcoreamature.budgetcalculatoreno

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MinimumOrderSizeUpdateWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.kraken.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val krakenApiService = retrofit.create(KrakenApiService::class.java)
        val db = FirebaseFirestore.getInstance()
        val minimumAmountUpdater = MinimumAmountUpdater(krakenApiService, db)

        minimumAmountUpdater.updateAllMinimumOrderSizes()

        return Result.success()
    }
}
