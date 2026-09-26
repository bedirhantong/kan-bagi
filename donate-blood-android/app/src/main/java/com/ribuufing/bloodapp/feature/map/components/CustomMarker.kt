package com.ribuufing.bloodapp.feature.map.components

import androidx.compose.runtime.Composable
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState

@Composable
fun CustomLocationMarker(
    position: LatLng,
    title: String = "Konumunuz",
    snippet: String = "Şu anki konumunuz",
    onMarkerClick: () -> Unit = {}
) {
    Marker(
        state = MarkerState(position = position),
        title = title,
        snippet = snippet,
        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
        onClick = {
            onMarkerClick()
            true
        }
    )
} 