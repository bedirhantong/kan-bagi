package com.ribuufing.bloodapp.feature.map.components

import androidx.compose.runtime.Composable
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState

@Composable
fun HospitalMarker(
    position: LatLng,
    title: String? = null,
    onClick: () -> Unit = {}
) {
    Marker(
        state = MarkerState(position = position),
        title = title ?: "Hastane",
        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
        onClick = {
            onClick()
            true
        }
    )
}