data class AssetPair(
    val altname: String,
    val wsname: String,
    val aclass_base: String,
    val base: String,
    val aclass_quote: String,
    val quote: String,
    val lot: String,
    val pair_decimals: Int,
    val lot_decimals: Int,
    val lot_multiplier: Int,
    val leverage_buy: List<Int>,
    val leverage_sell: List<Int>,
    val fees: List<List<Double>>,
    val fees_maker: List<List<Double>>,
    val fee_volume_currency: String,
    val margin_call: Int,
    val margin_stop: Int,
    val orderMin: String
)

data class TradingPairsResponse(
    val error: List<String>,
    val result: Map<String, AssetPair>
)
