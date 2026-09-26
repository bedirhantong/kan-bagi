package com.ribuufing.bloodapp.feature.scanqr.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@Composable
fun QrScreen (
    navController: NavController,
    viewModel: QrScreenViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scanResult by viewModel.scanResult.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Camera permission state
    var hasCameraPermission by remember { mutableStateOf(
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    ) }
    
    // Permission launcher
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Camera permission is required for QR scanning", Toast.LENGTH_LONG).show()
        }
    }
    
    // Request permission if not granted
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    
    // Handle scan result
    LaunchedEffect(scanResult) {
        scanResult?.let { code ->
            Log.d("QrScreen", "Scanned QR code: $code")
            Toast.makeText(context, "Scanned code: $code", Toast.LENGTH_SHORT).show()
            // Here you would typically authenticate with the scanned code
        }
    }
    
    // Snackbar event collect
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is QrScreenViewModel.UiEvent.ShowMessage -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(event.message)
                    }
                }
            }
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Show camera preview and scanner if permission is granted
        if (hasCameraPermission) {
            CameraPreview(
                onQrCodeScanned = { code ->
                    viewModel.onQrCodeScanned(code)
                }
            )
            
            // Scanning overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .background(Color.Transparent)
                        .padding(4.dp)
                )
            }
            
            // Instructions
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp)
            ) {
                Card(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(0.8f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Scan the hospital employee QR code to sign in",
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Show message if permission not granted
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Camera permission is required for scanning QR codes",
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                }) {
                    Text("Request Permission")
                }
            }
        }
    }
    // Snackbar host
    Box(modifier = Modifier.fillMaxSize()) {
        SnackbarHost(hostState = snackbarHostState) { data ->
            val isSuccess = data.visuals.message.contains("başarı", true)
            Snackbar(
                containerColor = if (isSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                snackbarData = data
            )
        }
    }
}

@Composable
fun CameraPreview(
    onQrCodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Create a preview use case
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val executor = remember { Executors.newSingleThreadExecutor() }
    
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
                
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    
                    // Preview use case
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    
                    // Image analysis use case
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(executor, QrCodeAnalyzer { qrCode ->
                                onQrCodeScanned(qrCode)
                            })
                        }
                    
                    try {
                        // Unbind all use cases before rebinding
                        cameraProvider.unbindAll()
                        
                        // Bind use cases to camera
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        Log.e("CameraPreview", "Use case binding failed", e)
                    }
                }, ContextCompat.getMainExecutor(context))
                
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

class QrCodeAnalyzer(private val onQrCodeScanned: (String) -> Unit) : ImageAnalysis.Analyzer {
    private val scanner: BarcodeScanner = BarcodeScanning.getClient()
    private var isScanning = true
    
    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        if (!isScanning) {
            imageProxy.close()
            return
        }
        
        imageProxy.image?.let { image ->
            val inputImage = InputImage.fromMediaImage(
                image,
                imageProxy.imageInfo.rotationDegrees
            )
            
            scanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty()) {
                        isScanning = false
                        for (barcode in barcodes) {
                            when (barcode.valueType) {
                                Barcode.TYPE_TEXT,
                                Barcode.TYPE_URL,
                                Barcode.TYPE_PRODUCT,
                                Barcode.TYPE_ISBN,
                                Barcode.TYPE_CONTACT_INFO,
                                Barcode.TYPE_EMAIL,
                                Barcode.TYPE_PHONE,
                                Barcode.TYPE_SMS,
                                Barcode.TYPE_WIFI,
                                Barcode.TYPE_GEO,
                                Barcode.TYPE_CALENDAR_EVENT -> {
                                    barcode.rawValue?.let { value ->
                                        onQrCodeScanned(value)
                                    }
                                }
                            }
                        }
                        // Resume scanning after a short delay
                        android.os.Handler().postDelayed({
                            isScanning = true
                        }, 1000)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("QrCodeAnalyzer", "Barcode scanning failed", e)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } ?: imageProxy.close()
    }
}