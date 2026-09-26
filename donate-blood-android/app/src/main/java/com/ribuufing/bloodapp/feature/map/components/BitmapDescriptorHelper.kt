package com.ribuufing.bloodapp.feature.map.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

object BitmapDescriptorHelper {
    fun createBitmapDescriptorFromVector(
        context: Context,
        content: @Composable () -> Unit
    ): BitmapDescriptor {
        val composeView = ComposeView(context).apply {
            setContent {
                content()
            }
        }
        
        // Measure and layout manually
        composeView.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        
        composeView.layout(0, 0, composeView.measuredWidth, composeView.measuredHeight)
        
        // Create bitmap from the view
        val bitmap = Bitmap.createBitmap(
            composeView.measuredWidth,
            composeView.measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        
        val canvas = Canvas(bitmap)
        composeView.draw(canvas)
        
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}