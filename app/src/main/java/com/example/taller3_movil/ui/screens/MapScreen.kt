package com.example.taller3_movil.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.taller3_movil.ui.viewmodel.AuthViewModel
import com.example.taller3_movil.ui.viewmodel.MapViewModel
import com.example.taller3_movil.ui.viewmodel.MapViewModelFactory
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    authViewModel: AuthViewModel,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val user by authViewModel.currentUser.collectAsState()
    
    val mapViewModel: MapViewModel = viewModel(
        factory = MapViewModelFactory(
            LocationServices.getFusedLocationProviderClient(context)
        )
    )

    val isConnected by mapViewModel.isConnected.collectAsState()
    val currentLocation by mapViewModel.currentLocation.collectAsState()
    val otherUsers by mapViewModel.otherUsers.collectAsState()
    val userPath by mapViewModel.userPath.collectAsState()
    val othersPaths by mapViewModel.othersPaths.collectAsState()

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(4.6097, -74.0817), 12f)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (granted && user != null) {
            mapViewModel.toggleConnection(user!!.uid, true)
        }
    }

    LaunchedEffect(currentLocation) {
        currentLocation?.let {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 15f)
        }
    }

    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Mapa RealTime")
                        Spacer(modifier = Modifier.weight(1f))
                        Switch(
                            checked = isConnected,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    val hasFineLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                    val hasCoarseLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                    
                                    if (hasFineLocation && hasCoarseLocation) {
                                        user?.uid?.let { mapViewModel.toggleConnection(it, true) }
                                    } else {
                                        permissionLauncher.launch(arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        ))
                                    }
                                } else {
                                    user?.uid?.let { mapViewModel.toggleConnection(it, false) }
                                }
                            }
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = isConnected)
            ) {
                // Marcador personalizado para el usuario actual
                currentLocation?.let { pos ->
                    MarkerComposable(
                        state = MarkerState(position = pos),
                        title = "Tú"
                    ) {
                        UserMarker(imageUrl = user?.profilePictureUrl, color = Color.Blue)
                    }
                    Polyline(points = userPath, color = Color.Blue, width = 10f)
                }

                // Marcadores personalizados para otros usuarios
                otherUsers.forEach { other ->
                    if (other.uid != user?.uid) {
                        val pos = LatLng(other.latitude, other.longitude)
                        MarkerComposable(
                            state = MarkerState(position = pos),
                            title = other.name
                        ) {
                            UserMarker(imageUrl = other.profilePictureUrl, color = Color.Red)
                        }
                        othersPaths[other.uid]?.let { path ->
                            Polyline(points = path, color = Color.Red.copy(alpha = 0.6f), width = 8f)
                        }
                    }
                }
            }

            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                DropdownMenuItem(text = { Text("Mi Perfil") }, onClick = { showMenu = false; onNavigateToProfile() })
                DropdownMenuItem(text = { Text("Cerrar Sesión") }, onClick = { showMenu = false; authViewModel.logout(); onLogout() })
            }
        }
    }
}

@Composable
fun UserMarker(imageUrl: String?, color: Color) {
    Surface(
        modifier = Modifier.size(45.dp),
        shape = CircleShape,
        color = color,
        border = BorderStroke(3.dp, Color.White),
        shadowElevation = 4.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (!imageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
