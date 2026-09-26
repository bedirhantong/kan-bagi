package com.ribuufing.bloodapp.feature.map.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.ribuufing.bloodapp.R
import com.ribuufing.bloodapp.utils.BitmapUtils

@Composable
fun KizilayMarker(
    position: LatLng,
    title: String,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val bitmap = BitmapUtils.vectorToBitmap(context, R.drawable.ic_kizilay_marker)

    Marker(
        state = MarkerState(position = position),
        title = title,
        onClick = {
            onClick()
            true
        },
        icon = bitmap?.let { BitmapDescriptorFactory.fromBitmap(it) }
            ?: BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
    )
} 