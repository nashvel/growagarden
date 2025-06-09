 package com.glowagarden.stocknotifier.network

import com.glowagarden.stocknotifier.StockResponse
import retrofit2.http.GET

interface ApiService {
    @GET("fetch_stock.php") // Ensure this path is correct relative to your BASE_URL
    suspend fun getStockData(): StockResponse
}