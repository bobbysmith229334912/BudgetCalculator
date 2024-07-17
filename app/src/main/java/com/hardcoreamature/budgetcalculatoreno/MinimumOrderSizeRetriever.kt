package com.hardcoreamature.budgetcalculatoreno

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

class MinimumOrderSizeRetriever(private val db: FirebaseFirestore) {

    fun getMinimumOrderSize(pair: String, callback: (Double?) -> Unit) {
        val minAmountDocRef = db.collection("minimumOrderSizes").document(pair)
        minAmountDocRef.get().addOnSuccessListener { document ->
            val minimumAmount = document.getDouble("minimumOrderSize")
            callback(minimumAmount)
        }.addOnFailureListener { e ->
            Log.e("MinimumOrderSizeRetriever", "Failed to fetch minimum order size from Firestore: $e")
            callback(null)
        }
    }
}
