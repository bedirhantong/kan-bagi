package com.ribuufing.bloodapp.feature.home.presentation

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ribuufing.bloodapp.utils.components.CentralLoadingAnimation
import com.ribuufing.bloodapp.utils.loading.LoadingManager
import com.ribuufing.bloodapp.feature.home.presentation.components.HomeTopBar
import com.ribuufing.bloodapp.feature.home.presentation.components.Story
import com.ribuufing.bloodapp.feature.home.presentation.viewmodel.HomeViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import com.ribuufing.bloodapp.feature.home.presentation.components.BloodRequestCard
import com.ribuufing.bloodapp.utils.observeInternetConnectivity
import com.ribuufing.bloodapp.feature.home.presentation.components.ActiveBloodRequestCard
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.painterResource
import com.ribuufing.bloodapp.R

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalCoroutinesApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val homeUiState by homeViewModel.uiState.collectAsState()
    val bloodRequests = homeViewModel.bloodRequests.collectAsLazyPagingItems()
    val connection by observeInternetConnectivity()
    val isLoading by LoadingManager.isLoading.collectAsState()

    var showHospitalSheet by remember { mutableStateOf(false) }
    var showBloodTypeSheet by remember { mutableStateOf(false) }
    var showSortingSheet by remember { mutableStateOf(false) }

    val selectedHospitalNames = homeUiState.availableHospitals.filter { homeUiState.selectedHospitals.contains(it.id) }.map { it.name }
    val selectedBloodTypeLabels = homeUiState.selectedBloodTypes.map {
        when (it) {
            "A_Positive" -> "A+"
            "A_Negative" -> "A-"
            "B_Positive" -> "B+"
            "B_Negative" -> "B-"
            "AB_Positive" -> "AB+"
            "AB_Negative" -> "AB-"
            "O_Positive" -> "O+"
            "O_Negative" -> "O-"
            else -> it
        }
    }

    LaunchedEffect(Unit) {
        homeViewModel.refresh()
    }
    LaunchedEffect(connection) {
        if (connection) {
            homeViewModel.refresh()
        }
    }

    if (showHospitalSheet) {
        FilterHospitalSheet(
            show = showHospitalSheet,
            onDismiss = { showHospitalSheet = false },
            hospitals = homeUiState.availableHospitals,
            selectedHospitals = homeUiState.selectedHospitals,
            onApply = { selected ->
                homeViewModel.setFilters(selected, homeUiState.selectedBloodTypes)
                showHospitalSheet = false
            }
        )
    }

    if (showBloodTypeSheet) {
        FilterBloodTypeSheet(
            show = showBloodTypeSheet,
            onDismiss = { showBloodTypeSheet = false },
            selectedBloodTypes = homeUiState.selectedBloodTypes,
            onApply = { selected ->
                homeViewModel.setFilters(homeUiState.selectedHospitals, selected)
                showBloodTypeSheet = false
            }
        )
    }

    if (showSortingSheet) {
        FilterSortingSheet(
            show = showSortingSheet,
            onDismiss = { showSortingSheet = false },
            selectedSorting = homeUiState.selectedSorting,
            onApply = { selected ->
                homeViewModel.setSorting(selected)
                showSortingSheet = false
            }
        )
    }

    Scaffold(
        topBar = {
            HomeTopBar(navController = navController)
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            if (!connection) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "İnternet Bağlantısı Yok!",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            if (!homeUiState.isLoading && homeUiState.error == null) {
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(homeUiState.stories) { story ->
                            Story(
                                story = story,
                                onStoryClick = {
                                    navController.navigate("story_detail/${story.id}")
                                }
                            )
                        }
                    }
                }
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(
                                start = 0.dp, 
                                end = 0.dp, 
                                top = 4.dp, 
                                bottom = 4.dp
                            )
                        ) {
                            item {
                                ModernFilterChip(
                                    iconRes = R.drawable.ic_hospital,
                                    label = if (selectedHospitalNames.isEmpty()) "Hastane" else "${selectedHospitalNames.size} Hastane",
                                    selected = selectedHospitalNames.isNotEmpty(),
                                    onClick = { showHospitalSheet = true }
                                )
                            }
                            
                            item {
                                ModernFilterChip(
                                    iconRes = R.drawable.ic_blood_type,
                                    label = if (selectedBloodTypeLabels.isEmpty()) "Kan Grubu" else selectedBloodTypeLabels.joinToString(", "),
                                    selected = selectedBloodTypeLabels.isNotEmpty(),
                                    onClick = { showBloodTypeSheet = true }
                                )
                            }
                            
                            item {
                                ModernFilterChip(
                                    iconRes = R.drawable.ic_sorting,
                                    label = "Sıralama",
                                    selected = true,
                                    onClick = { showSortingSheet = true },
                                    showBadge = false
                                )
                            }
                        }
                    }
                }
                item {
                    homeUiState.activePost?.let { activePost ->
                        ActiveBloodRequestCard(
                            bloodRequest = activePost,
                            onClick = {
                                navController.navigate("ownered_post_detail/${activePost.id}/${homeViewModel.uiState.value.userid}")
                            },
                        )
                    }
                }

                items(
                    count = bloodRequests.itemCount,
                    key = bloodRequests.itemKey { it.id!! },
                    contentType = bloodRequests.itemContentType { "bloodRequest" }
                ) { index ->
                    val bloodRequest = bloodRequests[index]
                    bloodRequest?.let {
                        BloodRequestCard(
                            bloodRequest = it,
                            onClick = {
                                navController.navigate("post_detail/${it.id}")
                            },
                            onDonateClick = {

                            }
                        )
                    }
                }
            }

            if (homeUiState.error != null && connection) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = homeUiState.error ?: "")
                            Button(
                                onClick = {
                                    homeViewModel.refresh()
                                },
                                enabled = !isLoading
                            ) {
                                Text("Tekrar Dene")
                            }
                        }
                    }
                }
            }
        }

        CentralLoadingAnimation()
    }
}

data class StoryBoard(
    val id: String,
    val icon: String,
    val storyImage: String,
    val name: String,
    val duration: Long = 7000L,
    val timestamp: Long = System.currentTimeMillis(),
    val description: String = "",
    val webUrl: String? = null
)

@SuppressLint("MutableCollectionMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterHospitalSheet(
    show: Boolean,
    onDismiss: () -> Unit,
    hospitals: List<com.ribuufing.bloodapp.feature.sharepost.domain.model.Hospital>,
    selectedHospitals: List<Int>,
    onApply: (List<Int>) -> Unit
) {
    var selectedHospitalsState by remember { mutableStateOf(selectedHospitals.toMutableSet()) }
    val sheetState = rememberModalBottomSheetState()
    if (show) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp)
                        .padding(bottom = 72.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Hastane Seç",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(40.dp))
                    }
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(hospitals) { hospital ->
                            val id = hospital.id
                            val selected = selectedHospitalsState.contains(id)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .border(
                                        width = if (selected) 2.dp else 1.dp,
                                        color = if (selected) MaterialTheme.colorScheme.primary else Color.LightGray,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.White)
                                    .clickable {
                                        if (selected) selectedHospitalsState.remove(id) else selectedHospitalsState.add(id)
                                        onApply(selectedHospitalsState.toList())
                                    },
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = "Hospital Icon",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = hospital.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = "Done",
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@SuppressLint("MutableCollectionMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBloodTypeSheet(
    show: Boolean,
    onDismiss: () -> Unit,
    selectedBloodTypes: List<String>,
    onApply: (List<String>) -> Unit
) {
    val allBloodTypes = listOf(
        "A_Positive", "A_Negative", "B_Positive", "B_Negative",
        "AB_Positive", "AB_Negative", "O_Positive", "O_Negative"
    )
    val bloodTypeLabels = mapOf(
        "A_Positive" to "A+", "A_Negative" to "A-",
        "B_Positive" to "B+", "B_Negative" to "B-",
        "AB_Positive" to "AB+", "AB_Negative" to "AB-",
        "O_Positive" to "O+", "O_Negative" to "O-",
        "Unknown" to "Bilinmiyor"
    )
    var selectedBloodTypesState by remember { mutableStateOf(selectedBloodTypes.toMutableSet()) }
    val sheetState = rememberModalBottomSheetState()
    if (show) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp)
                        .padding(bottom = 72.dp), // Done butonu için alt boşluk
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Kan Grubu Seç",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(40.dp))
                    }
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(allBloodTypes) { type ->
                            val selected = selectedBloodTypesState.contains(type)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .border(
                                        width = if (selected) 2.dp else 1.dp,
                                        color = if (selected) MaterialTheme.colorScheme.primary else Color.LightGray,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.White)
                                    .clickable {
                                        if (selected) selectedBloodTypesState.remove(type) else selectedBloodTypesState.add(type)
                                        onApply(selectedBloodTypesState.toList())
                                    },
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = "Blood Icon",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = bloodTypeLabels[type] ?: type,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = "Done",
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSortingSheet(
    show: Boolean,
    onDismiss: () -> Unit,
    selectedSorting: String,
    onApply: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var selected by remember { mutableStateOf(selectedSorting) }
    if (show) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sort by",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(40.dp))
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .border(
                            width = if (selected == "Newest") 2.dp else 1.dp,
                            color = if (selected == "Newest") MaterialTheme.colorScheme.primary else Color.LightGray,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .background(Color.White)
                        .clickable { selected = if (selected == "Newest") "Oldest" else "Newest" },
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Date Icon",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Date",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (selected == "Newest") "Newest" else "Oldest",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "Sort Direction",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(24.dp)
                                    .graphicsLayer {
                                        rotationZ = if (selected == "Newest") 0f else 180f
                                    }
                            )
                        }
                    }
                }
                Button(
                    onClick = { onApply(selected) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = "Done",
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
fun ModernFilterChip(
    modifier: Modifier = Modifier,
    iconRes: Int,
    label: String,
    selected: Boolean = false,
    onClick: () -> Unit,
    showBadge: Boolean = true
) {
    var selectedState by remember { mutableStateOf(selected) }
    
    val scale by animateFloatAsState(
        targetValue = if (selectedState) 1.03f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    val elevation by animateFloatAsState(
        targetValue = if (selectedState) 4f else 1f,
        animationSpec = tween(durationMillis = 200)
    )
    
    LaunchedEffect(selected) {
        selectedState = selected
    }
    
    Surface(
        modifier = modifier
            .shadow(
                elevation = elevation.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            )
            .clip(RoundedCornerShape(24.dp))
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        color = if (selectedState) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        contentColor = if (selectedState) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = if (selectedState) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            if (selectedState && showBadge) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                )
            }
        }
    }
}