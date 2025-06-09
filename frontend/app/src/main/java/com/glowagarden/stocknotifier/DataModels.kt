package com.glowagarden.stocknotifier

import com.google.gson.annotations.SerializedName

data class StockItem(
    @SerializedName("name") val name: String,
    @SerializedName("stock") val stock: Int,
    @SerializedName("tier") val tier: String?
)

data class StockResponse(
    @SerializedName("source") val source: String?,
    @SerializedName("source_original") val source_original: String?, // For stale cache, original source
    @SerializedName("fetched_at_timestamp") val fetched_at_timestamp: Long?,
    @SerializedName("error_message") val error_message: String?,
    @SerializedName("seeds") val seeds: List<StockItem>,
    @SerializedName("gear") val gear: List<StockItem>,
    @SerializedName("eggs") val eggs: List<StockItem>
)