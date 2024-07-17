package com.hardcoreamature.budgetcalculatoreno

data class OHLCResponse(
    val error: List<String>,
    val result: Map<String, List<List<String>>>
)
