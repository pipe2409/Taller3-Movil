package com.example.taller3_movil.ui.viewmodel

import android.annotation.SuppressLint
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taller3_movil.data.model.User
import com.example.taller3_movil.data.repository.UserRepository
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MapViewModel(
    private val userRepository: UserRepository = UserRepository(),
    private val fusedLocationClient: FusedLocationProviderClient
) : ViewModel() {

    private val _isConnected = MutableStateFlow(false)
    val isConnected = _isConnected.asStateFlow()

    private val _currentLocation = MutableStateFlow<LatLng?>(null)
    val currentLocation = _currentLocation.asStateFlow()

    private val _otherUsers = MutableStateFlow<List<User>>(emptyList())
    val otherUsers = _otherUsers.asStateFlow()

    private val _userPath = MutableStateFlow<List<LatLng>>(emptyList())
    val userPath = _userPath.asStateFlow()

    // Map of userId to their path
    private val _othersPaths = MutableStateFlow<Map<String, List<LatLng>>>(emptyMap())
    val othersPaths = _othersPaths.asStateFlow()

    private var locationJob: Job? = null
    private var usersJob: Job? = null

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val location = locationResult.lastLocation ?: return
            val latLng = LatLng(location.latitude, location.longitude)
            _currentLocation.value = latLng
            
            if (_isConnected.value) {
                _userPath.value = _userPath.value + latLng
                updateUserLocationInFirestore(latLng)
            }
        }
    }

    init {
        observeConnectedUsers()
    }

    private fun observeConnectedUsers() {
        usersJob?.cancel()
        usersJob = viewModelScope.launch {
            userRepository.getConnectedUsers().collect { users ->
                _otherUsers.value = users
                // Update paths for other users (simplified: just showing their current pos in path for now)
                // In a real app, you'd store their previous positions to draw polylines.
                // For the workshop, we can track them locally while the app is open.
                val currentOthersPaths = _othersPaths.value.toMutableMap()
                users.forEach { user ->
                    val pos = LatLng(user.latitude, user.longitude)
                    val path = currentOthersPaths[user.uid] ?: emptyList()
                    if (path.isEmpty() || path.last() != pos) {
                        currentOthersPaths[user.uid] = path + pos
                    }
                }
                _othersPaths.value = currentOthersPaths
            }
        }
    }

    fun toggleConnection(uid: String, connected: Boolean) {
        _isConnected.value = connected
        if (!connected) {
            _userPath.value = emptyList()
            stopLocationUpdates()
        } else {
            startLocationUpdates()
        }
        viewModelScope.launch {
            userRepository.setConnectionStatus(uid, connected)
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateIntervalMillis(2000)
            .build()
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun updateUserLocationInFirestore(latLng: LatLng) {
        // We'll need the current user's UID. For simplicity, we can pass it or get it from Auth.
        // Assuming we have it or use a placeholder for now.
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
    }
}
