package com.ribuufing.bloodapp.feature.profile.presentation

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.ribuufing.bloodapp.feature.home.data.dto.PostResponse
import com.ribuufing.bloodapp.feature.profile.presentation.components.OwnerBloodRequestCards
import com.ribuufing.bloodapp.utils.components.CentralLoadingAnimation
import com.ribuufing.bloodapp.utils.observeInternetConnectivity
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.runtime.rememberCoroutineScope
import com.ribuufing.bloodapp.feature.home.presentation.components.BloodRequestCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val userActivePost by viewModel.userActivePost.collectAsState()
    val userDonatedPosts by viewModel.userDonatedBloodRequests.collectAsState()
    val userInactivePosts = viewModel.userInactivePosts.collectAsLazyPagingItems()
    val isConnected by observeInternetConnectivity()
    val pagerState = rememberPagerState(pageCount = { 3 })
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is UiEvent.ShowMessage -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(event.message)
                    }
                }
            }
        }
    }

    LaunchedEffect(isConnected) {
        if (isConnected) {
            viewModel.refresh()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                val currentMessage = data.visuals.message
                Snackbar(
                    containerColor = if (currentMessage.contains("başarı", true) || currentMessage.contains("tekrar paylaşıldı", true)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    snackbarData = data
                )
            }
        },
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(top = 16.dp)
            ) {
                ProfileHeader(state,navController)

                Spacer(modifier = Modifier.height(15.dp))

                StatsRow(userInactivePosts.itemCount, userDonatedPosts.size)

//                Button(
//                    onClick = {
//
//                    },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 16.dp, vertical = 8.dp),
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = MaterialTheme.colorScheme.surface,
//                        contentColor = MaterialTheme.colorScheme.onSurface
//                    ),
//                    shape = RoundedCornerShape(8.dp),
//                    border = ButtonDefaults.outlinedButtonBorder
//                ) {
//                    Text("Profili Düzenle")
//                }

                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = pagerState.currentPage == 0,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(0)
                            }
                        },
                        text = { Text("Aktif Postum") },
                    )
                    Tab(
                        selected = pagerState.currentPage == 1,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(1)
                            }
                        },
                        text = { Text("İlanlarım") },
                    )
                    Tab(
                        selected = pagerState.currentPage == 2,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(2)
                            }
                        },
                        text = { Text("Kan Verdiklerim") },
                    )
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    Log.d( "Donations", "HorizontalPager: $userDonatedPosts")

                        when (page) {
                        0 -> ActivePostTab(userActivePost, navController, viewModel)
                        1 -> PostsGrid(userInactivePosts, navController, viewModel)
                        2 -> DonationsGrid( userDonatedPosts , navController)
                    }
                }
            }

            if (state.isLoading) {
                CentralLoadingAnimation()
            }

            if (state.error != null) {
                Snackbar(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    Text(text = state.error ?: "")
                }
            }
        }
    }
}

@Composable
fun ProfileHeader(state: ProfileUiState, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = "https://ui-avatars.com/api/?name=${state.name}+${state.surname}&background=random",
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-8).dp, y = (-8).dp)
                        .zIndex(1f)
                        .clip(CircleShape)
                        .border(1.dp, Color.White, CircleShape),
                    color = Color(0xFFE53935),
                    contentColor = Color.White
                ) {
                    Text(
                        text = "A+",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            IconButton(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp),
                onClick = { navController.navigate("settings") },
                ) {
                Icon(Icons.Default.Settings, contentDescription = "Ayarlar")
            }
        }
        
        // User Info
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 10.dp)
        ) {
            Text(
                text = "${state.name} ${state.surname}",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = state.phoneNumber,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Türkiye",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "2025 Mayıs'dan beri üye",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun StatsRow(postsCount: Int, donatedCount : Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem("İlanlarım", postsCount.toString())
        StatItem("Yardım Edilenler", donatedCount.toString())
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun ActivePostTab(userActivePost: PostResponse?, navController: NavController, viewModel: ProfileViewModel) {
    if (userActivePost == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Aktif bir postunuz yok.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OwnerBloodRequestCards(
                    bloodRequest = userActivePost,
                    onClick = {
                        navController.navigate("ownered_post_detail/${userActivePost.id}/${viewModel.state.value.userId}")
                    },
                    onRepublishClick = { }
                )
            }
        }
    }
}

@Composable
fun PostsGrid(bloodRequests: LazyPagingItems<PostResponse>, navController: NavController, viewModel: ProfileViewModel) {
    if (bloodRequests.itemCount == 0) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Henüz gönderi bulunmamaktadır.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
        return
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(bloodRequests.itemCount) { index ->
                val bloodRequest = bloodRequests[index]
                bloodRequest?.let {
                    OwnerBloodRequestCards(
                        bloodRequest = it,
                        onClick = {
                            navController.navigate("ownered_post_detail/${it.id}/${viewModel.state.value.userId}")
                        },
                        onRepublishClick = {
                            viewModel.republishPost(it.id ?: "")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DonationsGrid(donations: List<PostResponse>, navController: NavController) {
    Log.d("Donations", "DonationsGrid called with list size: ${donations.size}")
    Log.d("Donations", "Donations content: $donations")
    
    if (donations.isEmpty()) {
        Log.d("Donations", "No donations to display")
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Henüz bağış bulunmamaktadır.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(donations) { bloodRequest ->
                BloodRequestCard(
                    bloodRequest = bloodRequest,
                    onClick = {
                        navController.navigate("post_detail/${bloodRequest.id}")
                    },
                    onDonateClick = {}
                )
            }
        }
    }
}