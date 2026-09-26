package com.ribuufing.bloodapp.feature.home.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.ribuufing.bloodapp.feature.home.presentation.StoryBoard
import com.ribuufing.bloodapp.feature.home.presentation.viewmodel.HomeViewModel
import com.ribuufing.bloodapp.utils.components.CentralLoadingAnimation
import com.ribuufing.bloodapp.ui.theme.BloodAppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StoryDetail(
    navController: NavController,
    storyId: String,
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    BloodAppTheme(

    ) {
        val homeUiState by homeViewModel.uiState.collectAsState()

        LaunchedEffect(Unit) {
            homeViewModel.refresh()
        }

        if (!homeUiState.isLoading && homeUiState.error == null) {
            val stories = homeUiState.stories
            val initialPage = stories.indexOfFirst { it.id == storyId }.coerceAtLeast(0)
            val pagerState = rememberPagerState(initialPage = initialPage) { stories.size }
            val scope = rememberCoroutineScope()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    StoryPage(
                        story = stories[page],
                        onPreviousStory = {
                            if (page > 0) {
                                scope.launch {
                                    pagerState.animateScrollToPage(page - 1)
                                }
                            }
                        },
                        onNextStory = {
                            if (page < stories.size - 1) {
                                scope.launch {
                                    pagerState.animateScrollToPage(page + 1)
                                }
                            } else {
                                navController.popBackStack()
                            }
                        },
                        onClose = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                if (homeUiState.isLoading) {
                    CentralLoadingAnimation()
                } else {
                    Text(
                        text = homeUiState.error ?: "Unknown error",
                        color = Color.Red,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StoryPage(
    story: StoryBoard,
    onPreviousStory: () -> Unit,
    onNextStory: () -> Unit,
    onClose: () -> Unit
) {
    var isPaused by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var isFirstLoad by remember { mutableStateOf(true) }
    val uriHandler = LocalUriHandler.current

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(
            durationMillis = story.duration.toInt(),
            easing = LinearEasing
        ),
        label = "Progress Animation"
    )

    val swipeUpButtonVisible = remember { story.webUrl != null }
    var pulseScale by remember { mutableStateOf(1f) }

    LaunchedEffect(key1 = swipeUpButtonVisible) {
        if (swipeUpButtonVisible) {
            while (true) {
                // Pulse up
                for (i in 0..10) {
                    pulseScale = 1f + (i * 0.01f)
                    delay(50)
                }
                // Pulse down
                for (i in 10 downTo 0) {
                    pulseScale = 1f + (i * 0.01f)
                    delay(50)
                }
            }
        }
    }

    val swipeUpOffset by animateFloatAsState(
        targetValue = if (swipeUpButtonVisible) 0f else 50f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "Swipe Up Animation"
    )

    LaunchedEffect(story.id) {
        progress = 0f
        isFirstLoad = true
        if (!isPaused) {
            delay(100)
            progress = 1f
            isFirstLoad = false
        }
    }

    LaunchedEffect(animatedProgress) {
        if (animatedProgress >= 1f && !isPaused) {
            delay(200)
            onNextStory()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPaused = true
                        tryAwaitRelease()
                        isPaused = false
                        if (!isFirstLoad) {
                            progress = 1f
                        }
                    },
                    onTap = { offset ->
                        val width = size.width
                        val x = offset.x
                        if (x < width / 3) {
                            onPreviousStory()
                        } else if (x > width * 2 / 3) {
                            onNextStory()
                        }
                    }
                )
            }
    ) {
        AsyncImage(
            model = story.storyImage,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillWidth
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .padding(top = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                LinearProgressIndicator(
                    progress = animatedProgress,
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f)
                )
            }
        }

        // Top gradient and header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.7f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = 180f
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(top = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.Black.copy(alpha = 0.3f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    AsyncImage(
                        model = story.icon,
                        contentDescription = null,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .border(
                                width = 2.dp,
                                color = Color.White,
                                shape = CircleShape
                            ),
                        contentScale = ContentScale.Crop
                    )
                    Text(
                        text = story.name,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.3f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }
        }

        // Bottom gradient and description
        if (story.description.isNotEmpty() || story.webUrl != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            ),
                            startY = 100f,
                            endY = 900f
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .padding(bottom = 32.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (story.description.isNotEmpty()) {
                        Text(
                            text = story.description,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }

                    story.webUrl?.let { url ->
                        Card(
                            modifier = Modifier
                                .graphicsLayer {
                                    translationY = swipeUpOffset
                                    scaleX = pulseScale
                                    scaleY = pulseScale
                                }
                                .clickable {
                                    uriHandler.openUri(url)
                                },
                            shape = RoundedCornerShape(50),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 20.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowUp,
                                    contentDescription = "Swipe Up",
                                    tint = Color.White
                                )
                                Text(
                                    text = "Daha Fazla Bilgi",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}