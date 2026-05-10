package com.example.taller3_movil.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.taller3_movil.ui.viewmodel.AuthViewModel
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
    var isConnected by remember { mutableStateOf(false) }
    val bogota = LatLng(4.6097, -74.0817)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(bogota, 10f)
    }
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Mapa de Usuarios")
                        Spacer(modifier = Modifier.weight(1.0f))
                        Text("Conectado", style = MaterialTheme.typography.bodySmall)
                        Switch(
                            checked = isConnected,
                            onCheckedChange = { isConnected = it }
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menú")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                // Aquí irán los marcadores de los usuarios
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Modificar Perfil") },
                    onClick = {
                        showMenu = false
                        onNavigateToProfile()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Cerrar Sesión") },
                    onClick = {
                        showMenu = false
                        authViewModel.logout()
                        onLogout()
                    }
                )
            }
        }
    }
}
