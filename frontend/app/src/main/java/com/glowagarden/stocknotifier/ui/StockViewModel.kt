package com.glowagarden.stocknotifier.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glowagarden.stocknotifier.UserPreferencesRepository // Added import
import kotlinx.coroutines.flow.first // Added import for collecting first emission
import com.glowagarden.stocknotifier.StockResponse
import com.glowagarden.stocknotifier.model.SelectableItem
import com.glowagarden.stocknotifier.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class StockViewModel(private val userPreferencesRepository: UserPreferencesRepository) : ViewModel() {

    // !!! IMPORTANT: SET THIS TO YOUR PHP SCRIPT'S LOCATION !!!
    // Example for local XAMPP/MAMP server with script in 'glowagarden_backend' folder:
    // "http://10.0.2.2/glowagarden_backend/" (10.0.2.2 is for Android Emulator)
    // "http://YOUR_PC_IP_ADDRESS/glowagarden_backend/" (for physical device on same Wi-Fi)
    private val BASE_URL = "http://192.168.1.46/glowagarden/backend/" // <-- CHANGE THIS

    private val _stockData = MutableStateFlow<StockResponse?>(null)
    val stockData: StateFlow<StockResponse?> = _stockData

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _cropPreferences = MutableStateFlow<List<SelectableItem>>(emptyList())
    val cropPreferences: StateFlow<List<SelectableItem>> = _cropPreferences

    private val _gearPreferences = MutableStateFlow<List<SelectableItem>>(emptyList())
    val gearPreferences: StateFlow<List<SelectableItem>> = _gearPreferences

    private val _petPreferences = MutableStateFlow<List<SelectableItem>>(emptyList())
    val petPreferences: StateFlow<List<SelectableItem>> = _petPreferences

    private val apiService: ApiService

    init {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)

        val httpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS) // Connection timeout
            .readTimeout(30, TimeUnit.SECONDS)    // Read timeout
            .writeTimeout(30, TimeUnit.SECONDS)   // Write timeout
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)
        // Initialize preferences after loading saved ones
        viewModelScope.launch { // Launch coroutine to load preferences
            initializePreferences()
            fetchStockData() // Fetch stock data after preferences are potentially updated
        } // Corrected closing brace for viewModelScope.launch
    } // Closing brace for init block

    fun fetchStockData() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null // Clear previous errors
            try {
                val response = apiService.getStockData()
                _stockData.value = response
                if (response.error_message != null && response.source != "cache (stale)") {
                    // This is an error from the backend that isn't just a stale cache warning
                    _errorMessage.value = response.error_message
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.localizedMessage ?: "Unknown error"}"
                // Keep stale data if available, otherwise clear
                if (_stockData.value?.source != "cache (stale)") {
                     // _stockData.value = null // Optionally clear data on network error if not stale
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun initializePreferences() { // Make suspend to await loaded preferences
        val savedItemNames = userPreferencesRepository.loadSelectedItems.first() // Get the initially saved set
        val allCrops = listOf(
            SelectableItem("Carrot", "Common", "Crop"),
            SelectableItem("Strawberry", "Common", "Crop"),
            SelectableItem("Blueberry", "Uncommon", "Crop"),
            SelectableItem("Orange Tulip", "Uncommon", "Crop"),
            SelectableItem("Tomato", "Rare", "Crop"),
            SelectableItem("Corn", "Rare", "Crop"),
            SelectableItem("Daffodil", "Rare", "Crop"),
            SelectableItem("Watermelon", "Legendary", "Crop"),
            SelectableItem("Pumpkin", "Legendary", "Crop"),
            SelectableItem("Apple", "Legendary", "Crop"),
            SelectableItem("Bamboo", "Legendary", "Crop"),
            SelectableItem("Coconut", "Mythical", "Crop"),
            SelectableItem("Cactus", "Mythical", "Crop"),
            SelectableItem("Dragon Fruit", "Mythical", "Crop"),
            SelectableItem("Mango", "Mythical", "Crop"),
            SelectableItem("Grape", "Divine", "Crop"),
            SelectableItem("Mushroom", "Divine", "Crop"),
            SelectableItem("Pepper", "Divine", "Crop"),
            SelectableItem("Cacao", "Divine", "Crop"),
            SelectableItem("Beanstalk", "Prismatic", "Crop"),
            SelectableItem("Ember Lily", "Prismatic", "Crop")
        )
        _cropPreferences.value = allCrops
            .filter { it.tier == "Legendary" || it.tier == "Mythical" }
            .map { it.copy(initialIsSelected = savedItemNames.contains(it.name)) }

        _gearPreferences.value = listOf<SelectableItem>(
            SelectableItem("Watering Can", "Common", "Gear"),
            SelectableItem("Trowel", "Uncommon", "Gear"),
            SelectableItem("Recall Wrench", "Uncommon", "Gear"),
            SelectableItem("Basic Sprinkler", "Rare", "Gear"),
            SelectableItem("Advance Sprinkler", "Legendary", "Gear"),
            SelectableItem("Godly Sprinkler", "Mythical", "Gear"),
            SelectableItem("Lightning Rod", "Mythical", "Gear"),
            SelectableItem("Master Sprinkler", "Divine", "Gear"),
            SelectableItem("Harvesting Tool", "Divine", "Gear"),
            SelectableItem("Favorite Tool", "Divine", "Gear")
        ).map { it.copy(initialIsSelected = savedItemNames.contains(it.name)) }

        _petPreferences.value = listOf(
            SelectableItem("Common Pet", "Common", "Pet"),
            SelectableItem("Uncommon Pet", "Uncommon", "Pet"),
            SelectableItem("Rare Pet", "Rare", "Pet"),
            SelectableItem("Legendary Pet", "Legendary", "Pet"),
            SelectableItem("Mythical Pet", "Mythical", "Pet"),
            SelectableItem("Bug Pet", "Bug", "Pet") // Assuming 'Bug' is a tier/type
        ).map { it.copy(initialIsSelected = savedItemNames.contains(it.name)) }
    }

    fun toggleItemSelection(item: SelectableItem) {
        item.isSelected = !item.isSelected
        // To trigger recomposition for lists, we need to re-assign the list.
        // This is a common pattern when using StateFlow with mutable objects in a list.
        when (item.category) {
            "Crop" -> _cropPreferences.value = _cropPreferences.value.toList() // Create new list instance
            "Gear" -> _gearPreferences.value = _gearPreferences.value.toList()
            "Pet" -> _petPreferences.value = _petPreferences.value.toList()
        }
    } // Closing brace for toggleItemSelection

    fun saveUserSelections() {
        viewModelScope.launch {
            val selectedItems = mutableSetOf<String>()
            _cropPreferences.value.filter { it.isSelected }.forEach { selectedItems.add(it.name) }
            _gearPreferences.value.filter { it.isSelected }.forEach { selectedItems.add(it.name) }
            _petPreferences.value.filter { it.isSelected }.forEach { selectedItems.add(it.name) }
            userPreferencesRepository.saveSelectedItems(selectedItems)
        }
    } // Closing brace for saveUserSelections
} // Closing brace for StockViewModel class