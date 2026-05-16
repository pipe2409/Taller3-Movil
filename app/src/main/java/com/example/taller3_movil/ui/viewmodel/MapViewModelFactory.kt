package com.example.taller3_movil.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient

class MapViewModelFactory(private val fusedLocationClient: FusedLocationProviderClient) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MapViewModel(fusedLocationClient = fusedLocationClient) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
