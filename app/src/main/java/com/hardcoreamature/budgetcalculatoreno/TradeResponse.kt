package com.hardcoreamature.budgetcalculatoreno

import com.google.gson.annotations.SerializedName

data class TradeResponse(
    @SerializedName("error") val error: List<String>,
    @SerializedName("result") val result: Map<String, Any>?
)
