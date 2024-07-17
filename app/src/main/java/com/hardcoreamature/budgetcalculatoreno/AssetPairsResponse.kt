package com.hardcoreamature.budgetcalculatoreno

data class AssetPairsResponse(
    val result: Map<String, AssetPair>
)

data class AssetPair(
    val ordermin: String
)
