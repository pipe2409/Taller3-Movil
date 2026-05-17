package com.example.taller3_movil.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.taller3_movil.ui.viewmodel.AuthState
import com.example.taller3_movil.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    val user by viewModel.currentUser.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current
    
    var name by remember { mutableStateOf(user?.name ?: "") }
    var identification by remember { mutableStateOf(user?.identification ?: "") }
    var phone by remember { mutableStateOf(user?.phone ?: "") }
    var password by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Manejo de estados: Error o Éxito
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                Toast.makeText(context, "Perfil actualizado con éxito", Toast.LENGTH_SHORT).show()
                viewModel.resetAuthState() // Limpiar el estado antes de salir
                onNavigateBack()
            }
            is AuthState.Error -> {
                Toast.makeText(context, (authState as AuthState.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetAuthState()
            }
            else -> {}
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) selectedImageUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Perfil") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Foto de Perfil con indicador de carga
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .clickable { if (authState !is AuthState.Loading) galleryLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Nueva Foto",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (!user?.profilePictureUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = user?.profilePictureUrl,
                        contentDescription = "Foto Actual",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Sin Foto",
                        modifier = Modifier.size(80.dp)
                    )
                }
                
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(120.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 4.dp
                    )
                }
            }
            
            TextButton(
                onClick = { galleryLauncher.launch("image/*") },
                enabled = authState !is AuthState.Loading
            ) {
                Text("Cambiar Foto de Perfil")
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre Completo") },
                modifier = Modifier.fillMaxWidth(),
                enabled = authState !is AuthState.Loading
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = identification,
                onValueChange = { identification = it },
                label = { Text("Número de Identificación") },
                modifier = Modifier.fillMaxWidth(),
                enabled = authState !is AuthState.Loading
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Teléfono") },
                modifier = Modifier.fillMaxWidth(),
                enabled = authState !is AuthState.Loading
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Nueva Contraseña (Opcional)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = authState !is AuthState.Loading
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    user?.let {
                        val updatedUser = it.copy(
                            name = name,
                            identification = identification,
                            phone = phone
                        )
                        viewModel.updateProfile(updatedUser, selectedImageUri, password)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = authState !is AuthState.Loading
            ) {
                if (authState is AuthState.Loading) {
                    Text("Subiendo información...")
                } else {
                    Text("Guardar Cambios")
                }
            }
        }
    }
}
