package com.example.apphwcamera

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

class MainActivity : ComponentActivity() {
    private val viewModel: CameraViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            Log.d("MainActivity", "Permisos concedidos")
            viewModel.resetState()
        } else {
            Log.d("MainActivity", "Permisos denegados")
            Toast.makeText(
                this,
                "Se requieren permisos para usar la cámara",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate iniciado")
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CameraScreen(
                        viewModel = viewModel,
                        onRequestPermissions = { requestPermissions() }
                    )
                }
            }
        }
    }

    private fun requestPermissions() {
        Log.d("MainActivity", "Solicitando permisos")
        val permissions = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_MEDIA_IMAGES
            )
        } else {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
        requestPermissionLauncher.launch(permissions)
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    viewModel: CameraViewModel,
    onRequestPermissions: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()
    
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    var previewView by remember { mutableStateOf<PreviewView?>(null) }

    LaunchedEffect(cameraPermissionState.status.isGranted) {
        Log.d("CameraScreen", "Estado de permiso de cámara: ${cameraPermissionState.status.isGranted}")
        if (cameraPermissionState.status.isGranted) {
            Log.d("CameraScreen", "Permisos concedidos, reseteando estado")
            viewModel.resetState()
        }
    }

    LaunchedEffect(previewView) {
        if (cameraPermissionState.status.isGranted && previewView != null) {
            Log.d("CameraScreen", "Inicializando cámara con PreviewView")
            viewModel.initializeCamera(context, lifecycleOwner, previewView!!)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (uiState) {
            is CameraUiState.Initial -> {
                Log.d("CameraScreen", "Estado: Initial")
                if (!cameraPermissionState.status.isGranted) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (cameraPermissionState.status.shouldShowRationale) {
                            Text("Se requiere permiso de cámara para usar esta función")
                        }
                        Button(onClick = { onRequestPermissions() }) {
                            Text("Solicitar Permisos")
                        }
                    }
                } else {
                    Log.d("CameraScreen", "Permisos ya concedidos, mostrando vista de cámara")
                    CameraPreview(
                        modifier = Modifier.fillMaxSize(),
                        onPreviewViewCreated = { view ->
                            Log.d("CameraScreen", "PreviewView creado")
                            previewView = view
                        }
                    )
                }
            }
            is CameraUiState.CameraReady -> {
                Log.d("CameraScreen", "Estado: CameraReady")
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        CameraPreview(
                            modifier = Modifier.fillMaxSize(),
                            onPreviewViewCreated = { view ->
                                Log.d("CameraScreen", "PreviewView creado en estado CameraReady")
                                previewView = view
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.takePhoto(context) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text("Tomar Foto")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            is CameraUiState.PhotoCaptured -> {
                Log.d("CameraScreen", "Estado: PhotoCaptured")
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val uri = (uiState as CameraUiState.PhotoCaptured).uri
                    Text("¡Foto guardada exitosamente!")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.resetState() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Volver a la Cámara")
                    }
                }
            }
            is CameraUiState.Error -> {
                Log.d("CameraScreen", "Estado: Error - ${(uiState as CameraUiState.Error).message}")
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = (uiState as CameraUiState.Error).message,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.resetState() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Reintentar")
                    }
                }
            }
        }
    }
}

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onPreviewViewCreated: (PreviewView) -> Unit
) {
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            Log.d("CameraPreview", "Creando PreviewView")
            PreviewView(ctx).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                scaleType = PreviewView.ScaleType.FILL_CENTER
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                onPreviewViewCreated(this)
            }
        },
        update = { view ->
            Log.d("CameraPreview", "Actualizando PreviewView")
            view.scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    )
}