package com.example.apphwcamera

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executor

class CameraViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<CameraUiState>(CameraUiState.Initial)
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    private var imageCapture: ImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var cameraExecutor: Executor
    private var preview: Preview? = null
    private var cameraSelector: CameraSelector? = null

    fun initializeCamera(context: Context, lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        Log.d("CameraViewModel", "Inicializando cámara")
        try {
            cameraExecutor = ContextCompat.getMainExecutor(context)
            
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                try {
                    Log.d("CameraViewModel", "Obteniendo cameraProvider")
                    cameraProvider = cameraProviderFuture.get()
                    
                    Log.d("CameraViewModel", "Configurando preview")
                    preview = Preview.Builder()
                        .setTargetRotation(previewView.display.rotation)
                        .build()
                        .also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                    
                    Log.d("CameraViewModel", "Configurando imageCapture")
                    imageCapture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .setTargetRotation(previewView.display.rotation)
                        .build()

                    cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    Log.d("CameraViewModel", "Desvinculando cámara anterior")
                    cameraProvider?.unbindAll()
                    
                    Log.d("CameraViewModel", "Vinculando nueva cámara")
                    cameraProvider?.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector!!,
                        preview,
                        imageCapture
                    )
                    Log.d("CameraViewModel", "Cámara inicializada correctamente")
                    _uiState.value = CameraUiState.CameraReady
                } catch (e: Exception) {
                    Log.e("CameraViewModel", "Error al inicializar la cámara", e)
                    _uiState.value = CameraUiState.Error("Error al inicializar la cámara: ${e.message}")
                }
            }, cameraExecutor)
        } catch (e: Exception) {
            Log.e("CameraViewModel", "Error al preparar la inicialización de la cámara", e)
            _uiState.value = CameraUiState.Error("Error al preparar la cámara: ${e.message}")
        }
    }

    fun takePhoto(context: Context) {
        Log.d("CameraViewModel", "Tomando foto")
        val imageCapture = imageCapture ?: run {
            Log.e("CameraViewModel", "imageCapture es null")
            return
        }

        val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
            .format(System.currentTimeMillis())

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraApp")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                context.contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            .build()

        imageCapture.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = output.savedUri ?: return
                    Log.d("CameraViewModel", "Foto guardada: $savedUri")
                    viewModelScope.launch {
                        _uiState.value = CameraUiState.PhotoCaptured(savedUri)
                    }
                }

                override fun onError(exc: ImageCaptureException) {
                    Log.e("CameraViewModel", "Error al capturar la foto", exc)
                    viewModelScope.launch {
                        _uiState.value = CameraUiState.Error("Error al capturar la foto: ${exc.message}")
                    }
                }
            }
        )
    }

    fun resetState() {
        Log.d("CameraViewModel", "Reseteando estado")
        try {
            cameraProvider?.unbindAll()
            cameraProvider = null
            preview = null
            imageCapture = null
            cameraSelector = null
            _uiState.value = CameraUiState.Initial
        } catch (e: Exception) {
            Log.e("CameraViewModel", "Error al resetear estado", e)
            _uiState.value = CameraUiState.Error("Error al resetear estado: ${e.message}")
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("CameraViewModel", "Limpiando recursos")
        try {
            cameraProvider?.unbindAll()
            cameraProvider = null
            preview = null
            imageCapture = null
            cameraSelector = null
        } catch (e: Exception) {
            Log.e("CameraViewModel", "Error al limpiar recursos", e)
        }
    }
}

sealed class CameraUiState {
    object Initial : CameraUiState()
    object CameraReady : CameraUiState()
    data class PhotoCaptured(val uri: Uri) : CameraUiState()
    data class Error(val message: String) : CameraUiState()
} 