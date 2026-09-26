package com.ribuufing.bloodapp.feature.home.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ribuufing.bloodapp.R
import com.ribuufing.bloodapp.navigation.Routes
import com.ribuufing.bloodapp.ui.theme.MainColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    navController: NavController,
) {
    CenterAlignedTopAppBar(
        modifier = Modifier.fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface),
        title = {
            Text(
                text = "BloodApp",
                fontSize = 21.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        actions = {
            IconButton(onClick = { navController.navigate(Routes.ChatList.route) }) {
                Icon(
                    painter = painterResource(id = R.drawable.dm),
                    contentDescription = "Direct Message",
                    modifier = Modifier.size(24.dp),
                    tint = MainColor
                )
            }
        }
    )
} 