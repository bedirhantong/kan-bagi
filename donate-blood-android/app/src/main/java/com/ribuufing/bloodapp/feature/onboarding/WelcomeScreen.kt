package com.ribuufing.bloodapp.feature.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.ribuufing.bloodapp.navigation.Routes
import kotlinx.coroutines.launch
import com.ribuufing.bloodapp.R

@Composable
fun WelcomeScreen(navController: NavHostController) {
    val onboardingPages = listOf(
        OnboardingPage(
            imageUrl = "https://hisarhospital.com/wp-content/uploads/2015/11/Kan_bagis_nakil.jpg",
            title = stringResource(id = R.string.onboarding_title_1),
            description = stringResource(id = R.string.onboarding_desc_1),
            backgroundColor = Color(0xFFFFEBEE)
        ),
        OnboardingPage(
            imageUrl = "https://diyarbakir.meb.gov.tr/meb_iys_dosyalar/2020_05/08085806_Kan_BaYYYY.jpg",
            title = stringResource(id = R.string.onboarding_title_2),
            description = stringResource(id = R.string.onboarding_desc_2),
            backgroundColor = Color(0xFFFFCDD2)
        ),
        OnboardingPage(
            imageUrl = "https://kickboks.gov.tr/attachment/kan-bagisi-hayat-kurtatir-instagram-gonderisi.png",
            title = stringResource(id = R.string.onboarding_title_3),
            description = stringResource(id = R.string.onboarding_desc_3),
            backgroundColor = Color(0xFFEF9A9A)
        )
    )

    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            OnboardingScreen(onboardingPages[page])
        }

        TextButton(
            onClick = {
                navController.navigate(Routes.LoginType.route) {
                    launchSingleTop = true
                    popUpTo(Routes.Welcome.route,) {
                        inclusive = true
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end =  16.dp, top = 40.dp)
        ) {
            Text(
                "Atla",
                color = Color(0xFFE53935),
                style = MaterialTheme.typography.labelLarge
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(bottom = 32.dp)
                    .align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(pagerState.pageCount) { iteration ->
                    Box(
                        modifier = Modifier
                            .size(if (pagerState.currentPage == iteration) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (pagerState.currentPage == iteration)
                                    Color(0xFFE53935)
                                else
                                    Color(0xFFE53935).copy(alpha = 0.5f)
                            )
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 25.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedVisibility(
                    visible = pagerState.currentPage > 0,
                    enter = fadeIn(animationSpec = tween(300)),
                    exit = fadeOut(animationSpec = tween(300))
                ) {
                    TextButton(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }
                    ) {
                        Text("Geri", color = Color(0xFFE53935))
                    }
                }

                Button(
                    onClick = {
                        if (pagerState.currentPage < pagerState.pageCount - 1) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            navController.navigate(Routes.LoginType.route) {
                                popUpTo(Routes.Welcome.route) { inclusive = true }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53935)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .widthIn(min = 140.dp)
                ) {
                    Text(
                        text = if (pagerState.currentPage == pagerState.pageCount - 1)
                            stringResource(id = R.string.start)
                        else
                            stringResource(id = R.string.next),
                        color = Color.White
                    )
                }
            }
        }
    }
}