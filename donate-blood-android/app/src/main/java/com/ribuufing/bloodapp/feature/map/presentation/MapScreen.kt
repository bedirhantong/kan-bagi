package com.ribuufing.bloodapp.feature.map.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import com.ribuufing.bloodapp.feature.map.components.CustomLocationMarker
import com.ribuufing.bloodapp.feature.map.components.HospitalBottomSheet
import com.ribuufing.bloodapp.feature.map.components.HospitalMarker
import com.ribuufing.bloodapp.feature.map.components.KizilayMarker
import com.ribuufing.bloodapp.utils.observeInternetConnectivity
import android.net.Uri

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    navController: NavController,
    viewModel: MapScreenViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isConnected by observeInternetConnectivity()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var mapLoaded by remember { mutableStateOf(false) }
    var initialLocationSet by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            state.userLocation,
            state.zoomLevel
        )
    }

    // Initial location focus
    LaunchedEffect(hasLocationPermission, mapLoaded) {
        if (hasLocationPermission && mapLoaded && !initialLocationSet) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    viewModel.updateUserLocation(it)
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(
                        LatLng(it.latitude, it.longitude),
                        6.9f
                    )
                    initialLocationSet = true
                }
            }
        }
    }

    // Update camera when user location changes
    LaunchedEffect(state.userLocation) {
        if (state.isLocationLoaded && mapLoaded && !initialLocationSet) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                state.userLocation,
                state.zoomLevel
            )
        }
    }
    
    val mapProperties by remember(hasLocationPermission) {
        mutableStateOf(
            MapProperties(
                mapType = MapType.NORMAL,
                isMyLocationEnabled = hasLocationPermission,
                mapStyleOptions = try {
                    MapStyleOptions.loadRawResourceStyle(
                        context,
                        com.ribuufing.bloodapp.R.raw.map_style
                    )
                } catch (e: Exception) {
                    null
                }
            )
        )
    }
    
    val uiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false,
                mapToolbarEnabled = false,
                compassEnabled = true,
                indoorLevelPickerEnabled = false
            )
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
        if (isGranted) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let { viewModel.updateUserLocation(it) }
            }
        } else {
            viewModel.setError("Konum izni gerekli")
        }
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let { viewModel.updateUserLocation(it) }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Hastaneler",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Geri",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = mapProperties,
                uiSettings = uiSettings,
                onMapLoaded = {
                    mapLoaded = true
                }
            ) {
                state.bloodPoints.forEach { bloodPoint ->
                    KizilayMarker(
                        position = LatLng(bloodPoint.koordinatLatitude, bloodPoint.koordinatLongitude),
                        title = bloodPoint.ekipAdi,
                        onClick = {
                            viewModel.onBloodPointClick(bloodPoint)
                        }
                    )
                }

                state.hospitals.forEach { hospital ->
                    if (hospital.lat != null && hospital.lon != null) {
                        HospitalMarker(
                            position = LatLng(hospital.lat, hospital.lon),
                            title = hospital.name,
                            onClick = {
                                viewModel.onHospitalClick(hospital)
                            }
                        )
                    }
                }

                if (state.isLocationLoaded) {
                    CustomLocationMarker(position = state.userLocation)
                }
            }

            AnimatedVisibility(
                visible = state.error != null,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = state.error ?: "",
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        IconButton(
                            onClick = { viewModel.setError("") }
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Kapat",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            if (!mapLoaded) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Bottom sheet for blood point details
            AnimatedVisibility(
                visible = state.isBottomSheetVisible && state.sheetType == BottomSheetType.BLOOD_POINT && state.selectedBloodPoint != null,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                state.selectedBloodPoint?.let { bloodPoint ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = bloodPoint.ekipAdi,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${bloodPoint.mahalle} Mah. ${bloodPoint.adres}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = bloodPoint.ilceAd,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            // Add navigation button
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {

                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Yol Tarifi Al")
                            }
                        }
                    }
                }
            }
            
            AnimatedVisibility(
                visible = state.isBottomSheetVisible && state.sheetType == BottomSheetType.HOSPITAL && state.selectedHospital != null,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                state.selectedHospital?.let { hospital ->
                    HospitalBottomSheet(
                        hospital = hospital,
                        posts = state.selectedHospitalPosts,
                        isLoadingPosts = state.isLoadingPosts,
                        onDismiss = { viewModel.hideBottomSheet() },
                        onNavigateClick = {
                            if (hospital.lat != null && hospital.lon != null) {
                                val uri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${hospital.lat},${hospital.lon}&destination_name=${Uri.encode(hospital.name)}")
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                intent.setPackage("com.google.android.apps.maps")
                                
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    val browserIntent = Intent(Intent.ACTION_VIEW, uri)
                                    context.startActivity(browserIntent)
                                }
                            }
                        },
                        onViewDetailClick = {
                            hospital.id?.let { hospitalId ->
                                navController.navigate("hospital_detail/$hospitalId")
                            }
                        },
                        onPostClick = { postId ->
                            navController.navigate("post_detail/$postId")
                        }
                    )
                }
            }
        }
    }
}