package com.hardcoreamature.budgetcalculatoreno

data class KrakenResponse(
    val error: List<String>,
    val result: Result?
) {
    data class Result(
        val txid: List<String>
    )
}

