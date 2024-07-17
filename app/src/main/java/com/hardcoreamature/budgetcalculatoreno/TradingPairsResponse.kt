package com.hardcoreamature.budgetcalculatoreno

import AssetPair

data class TradingPairsResponse(
    val error: List<String>,
    val result: Map<String, AssetPair>
)
