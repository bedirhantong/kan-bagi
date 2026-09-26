package com.ribuufing.bloodapp.ui.theme.splash

import android.annotation.SuppressLint
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@SuppressLint("InvalidColorHexValue")
@Composable
fun AnimatedSplashScreen(
    onAnimationFinish: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }
    val pathString = "M167.3,161.65C168.02,173.12 165.68,185.09 158.88,194.35C146.95,210.61 123.62,215.04 104.08,210.01C82.33,204.42 59.53,189.35 46.42,171.21C40.86,163.52 36.51,154.32 37.36,144.86C38.32,134.23 46.48,124.62 56.81,121.95C67.15,119.28 78.94,123.74 84.93,132.58C89.63,121.6 104.76,117.42 115.36,122.92C125.96,128.42 131.37,141.47 129.72,153.3C128.08,165.13 120.33,175.52 110.58,182.42C109.88,182.92 109.14,183.41 108.3,183.62C105.07,184.42 102.35,180.43 102.93,177.15C103.52,173.88 106.07,171.38 108.35,168.95C112.95,164.07 117.02,158.37 118.36,151.79C119.7,145.22 117.78,137.65 112.33,133.73C98.97,124.12 95.84,143.26 85.59,144.59C77.98,145.58 74.23,135.25 67.36,131.83C58.19,127.26 47.35,137.93 47.86,148.16C48.38,158.39 55.93,166.76 63.45,173.71C77.24,186.46 93.37,198.09 112.01,200.4C130.64,202.7 151.87,192.74 156.71,174.59C159.83,162.87 155.82,150.53 151.43,139.21C134.65,95.95 111.76,55.06 83.64,18.14C58.86,50.82 38.37,86.75 22.86,124.72C16.27,140.86 10.49,157.99 11.9,175.37C13.97,200.81 32.47,223.71 56.34,232.75C80.2,241.79 108.27,237.4 129.33,222.99C131,221.85 133.06,220.69 135.04,220.16C137.17,219.59 139.21,219.75 140.57,221.44C143.17,224.67 139.69,229.23 136.32,231.66C110.82,250.07 75.16,254.17 46.63,240.45C21.76,228.49 3.74,203.18 0.71,175.75C-2.71,144.81 13.45,112.35 27.68,85.87C43.29,56.81 61.07,28.92 80.82,2.51C81.61,1.46 82.59,0.3 83.91,0.26C85.41,0.21 86.51,1.59 87.34,2.83C114.33,42.86 141.55,83.33 158.87,128.39C163,139.11 166.57,150.19 167.3,161.65Z"

    val path = remember { Path() }
    val pathMeasure = remember { PathMeasure() }

    val pathParser = remember { PathParser() }
    val parsedPath = remember { pathParser.parsePathString(pathString) }

    val density = LocalDensity.current
    val strokeWidth = with(density) { 2.dp.toPx() }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    val logoSize = minOf(screenWidth, screenHeight) * 0.5f

    val animatedProgress = remember {
        Animatable(initialValue = 0f)
    }

    LaunchedEffect(true) {
        startAnimation = true
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 2000,
                easing = FastOutSlowInEasing
            )
        )
        delay(500)
        onAnimationFinish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(logoSize)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            path.reset()
            parsedPath.forEach { pathNode ->
                when (pathNode) {
                    is PathParser.MoveTo -> path.moveTo(pathNode.x, pathNode.y)
                    is PathParser.LineTo -> path.lineTo(pathNode.x, pathNode.y)
                    is PathParser.CurveTo -> path.cubicTo(
                        pathNode.x1, pathNode.y1,
                        pathNode.x2, pathNode.y2,
                        pathNode.x3, pathNode.y3
                    )
                    is PathParser.Close -> path.close()
                }
            }

            val bounds = path.getBounds()
            val svgWidth = bounds.right - bounds.left
            val svgHeight = bounds.bottom - bounds.top

            val scaleX = canvasWidth / (svgWidth)
            val scaleY = canvasHeight / (svgHeight)
            val scale = minOf(scaleX, scaleY) * 1f

            val svgCenterX = bounds.left + (bounds.right - bounds.left) / 3
            val svgCenterY = bounds.top + (bounds.bottom - bounds.top) / 3

            val canvasCenterX = canvasWidth / 2
            val canvasCenterY = canvasHeight / 2

            val offsetX = canvasCenterX - (svgCenterX )
            val offsetY = canvasCenterY - (svgCenterY)

            withTransform({
                translate(offsetX * 1.81f , offsetY * 1.55f)
                scale(scale, scale)
            }) {
                path.reset()
                parsedPath.forEach { pathNode ->
                    when (pathNode) {
                        is PathParser.MoveTo -> path.moveTo(pathNode.x, pathNode.y)
                        is PathParser.LineTo -> path.lineTo(pathNode.x, pathNode.y)
                        is PathParser.CurveTo -> path.cubicTo(
                            pathNode.x1, pathNode.y1,
                            pathNode.x2, pathNode.y2,
                            pathNode.x3, pathNode.y3
                        )
                        is PathParser.Close -> path.close()
                    }
                }

                pathMeasure.setPath(path, false)
                val pathLength = pathMeasure.length
                val progress = animatedProgress.value

                val outPath = Path()
                pathMeasure.getSegment(
                    startDistance = 0f,
                    stopDistance = pathLength * progress,
                    destination = outPath,
                    startWithMoveTo = true
                )

                drawPath(
                    path = outPath,
                    color = Color(0xFEF888A4),
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                drawPath(
                    path = path,
                    color = Color(0xEDFC6087),
                    alpha = (progress - 0.5f).coerceIn(0f, 0.5f) * 2f
                )
            }
        }
    }
}

class PathParser {
    sealed class PathNode
    data class MoveTo(val x: Float, val y: Float) : PathNode()
    data class LineTo(val x: Float, val y: Float) : PathNode()
    data class CurveTo(
        val x1: Float, val y1: Float,
        val x2: Float, val y2: Float,
        val x3: Float, val y3: Float
    ) : PathNode()
    object Close : PathNode()

    fun parsePathString(pathData: String): List<PathNode> {
        val nodes = mutableListOf<PathNode>()
        val numbers = pathData.split("[^-\\d.]".toRegex())
            .filter { it.isNotEmpty() }
            .map { it.toFloat() }
        var index = 0

        fun getNextNumber(): Float = numbers[index++]

        var currentX = 0f
        var currentY = 0f

        pathData.forEach { char ->
            when (char) {
                'M' -> {
                    currentX = getNextNumber()
                    currentY = getNextNumber()
                    nodes.add(MoveTo(currentX, currentY))
                }
                'C' -> {
                    val x1 = getNextNumber()
                    val y1 = getNextNumber()
                    val x2 = getNextNumber()
                    val y2 = getNextNumber()
                    val x3 = getNextNumber()
                    val y3 = getNextNumber()
                    nodes.add(CurveTo(x1, y1, x2, y2, x3, y3))
                    currentX = x3
                    currentY = y3
                }
                'L' -> {
                    val x = getNextNumber()
                    val y = getNextNumber()
                    nodes.add(LineTo(x, y))
                    currentX = x
                    currentY = y
                }
                'Z' -> nodes.add(Close)
            }
        }
        return nodes
    }
}