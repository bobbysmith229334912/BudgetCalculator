data class TickerResponse(
    val error: List<String>,
    val result: Map<String, TickerInfo>
)

data class TickerInfo(
    val c: List<String> // Closing price (last trade) list
)
