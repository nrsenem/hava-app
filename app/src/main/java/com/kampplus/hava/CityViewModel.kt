package com.kampplus.hava

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonSyntaxException
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class CityViewModel(
    private val repository: CityRepository = CityRepository()
) : ViewModel() {

    private val _query = MutableStateFlow("İstanbul")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _uiState = MutableStateFlow<LoadState<List<City>>>(LoadState.Loading)
    val uiState: StateFlow<LoadState<List<City>>> = _uiState.asStateFlow()

    init {
        search()
    }

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }

    fun search() {
        viewModelScope.launch {
            _uiState.value = LoadState.Loading
            _uiState.value = try {
                val cities = repository.search(_query.value)
                if (cities.isEmpty()) LoadState.Empty else LoadState.Success(cities)
            } catch (e: IOException) {
                Log.e(TAG, "Bağlantı hatası", e)
                LoadState.Error("İnternet bağlantını kontrol et.")
            } catch (e: HttpException) {
                Log.e(TAG, "HTTP ${e.code()}", e)
                LoadState.Error("Sunucu şu an cevap vermiyor.")
            } catch (e: JsonSyntaxException) {
                Log.e(TAG, "Yanıt çözümlenemedi", e)
                LoadState.Error("Bir şeyler ters gitti. Tekrar dene.")
            }
        }
    }

    private companion object {
        const val TAG = "CityViewModel"
    }
}