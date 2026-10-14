package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.engine3d.Camera3D
import com.example.engine3d.Renderer3D
import com.example.engine3d.Vec3
import com.example.ui.theme.CoffeeBrownDark
import com.example.ui.theme.CoffeeCream
import com.example.viewmodel.CafeUiState
import kotlin.math.floor

@Composable
fun IsometricGameCanvas(
  uiState: CafeUiState,
  onGridHover: (Int, Int) -> Unit,
  onGridTap: (Int, Int) -> Unit,
  modifier: Modifier = Modifier
) {
  // 60FPS ticker for walk-cycles, steam, and ambient animations
  val infiniteTransition = rememberInfiniteTransition(label = "render_3d_ticker")
  val animTime by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1000f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1000000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "anim_time_3d"
  )

  BoxWithConstraints(modifier = modifier.fillMaxSize().testTag("cafe_isometric_viewport")) {
    val density = LocalDensity.current
    val screenWidthPx = with(density) { maxWidth.toPx() }
    // Calculate optimal zoom so the 8x8 cafe + sidewalk fills ~80% of screen width
    val defaultZoom = (screenWidthPx / 12f).coerceIn(60f, 120f)

    // CỐ ĐỊNH GÓC NHÌN ISOMETRIC 3D
    val fixedCamera = remember {
      Camera3D(
        target = Vec3(4.0f, 0.2f, 4.5f), // Center of cafe interior and entrance
        yawDeg = 45f,                    // Góc 45 độ chuẩn Isometric
        pitchDeg = 35.264f,              // Độ nghiêng 35.264 độ chuẩn 2:1
        zoom = defaultZoom
      )
    }

    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var currentZoom by remember { mutableFloatStateOf(defaultZoom) }

    // Keep camera zoom in sync
    fixedCamera.zoom = currentZoom

    Canvas(
      modifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
          detectTransformGestures { _, pan, zoomFactor, _ ->
            // Chỉ di chuyển camera theo trục X và Y
            panOffset += pan
            // Thu phóng mượt mà
            currentZoom = (currentZoom * zoomFactor).coerceIn(defaultZoom * 0.6f, defaultZoom * 2.2f)
          }
        }
        .pointerInput(uiState.isBuildMode, panOffset, currentZoom) {
          detectTapGestures(
            onTap = { tapOffset ->
              val screenCenter = Offset(size.width / 2f + panOffset.x, size.height / 2f + panOffset.y)
              val groundHit = fixedCamera.screenToGroundPlane(tapOffset.x, tapOffset.y, screenCenter)
              if (groundHit != null) {
                val gx = floor(groundHit.first).toInt().coerceIn(0, 7)
                val gz = floor(groundHit.second).toInt().coerceIn(0, 7)
                onGridTap(gx, gz)
              }
            }
          )
        }
    ) {
      Renderer3D.renderScene3D(
        drawScope = this,
        canvasSize = size,
        camera = fixedCamera,
        furnitureList = uiState.furnitureList,
        staffList = uiState.staffList,
        customerList = uiState.customers,
        weather = uiState.weather,
        timeSeconds = animTime,
        isBuildMode = uiState.isBuildMode,
        selectedPlacementType = uiState.selectedCatalogItem ?: uiState.selectedFurnitureToMove?.type,
        hoveredGridX = uiState.hoveredGridX,
        hoveredGridZ = uiState.hoveredGridY,
        isValidPlacement = uiState.isValidPlacement,
        panOffset = panOffset
      )
    }

    // Nút điều khiển nhanh Thu phóng & Căn giữa màn hình
    Surface(
      color = CoffeeBrownDark.copy(alpha = 0.88f),
      shape = RoundedCornerShape(16.dp),
      shadowElevation = 4.dp,
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(bottom = 76.dp, end = 12.dp)
        .testTag("viewport_controls")
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        // Căn giữa lại quán
        FilledIconButton(
          onClick = {
            panOffset = Offset.Zero
            currentZoom = defaultZoom
          },
          colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.White.copy(alpha = 0.15f)),
          modifier = Modifier.size(36.dp).testTag("recenter_viewport")
        ) {
          Icon(Icons.Default.MyLocation, contentDescription = "Căn giữa", tint = CoffeeCream, modifier = Modifier.size(18.dp))
        }

        // Thu nhỏ
        FilledIconButton(
          onClick = { currentZoom = (currentZoom - 12f).coerceAtLeast(defaultZoom * 0.6f) },
          colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.White.copy(alpha = 0.15f)),
          modifier = Modifier.size(36.dp).testTag("zoom_out_button")
        ) {
          Icon(Icons.Default.Remove, contentDescription = "Thu nhỏ", tint = CoffeeCream, modifier = Modifier.size(18.dp))
        }

        // Phóng to
        FilledIconButton(
          onClick = { currentZoom = (currentZoom + 12f).coerceAtMost(defaultZoom * 2.2f) },
          colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.White.copy(alpha = 0.15f)),
          modifier = Modifier.size(36.dp).testTag("zoom_in_button")
        ) {
          Icon(Icons.Default.Add, contentDescription = "Phóng to", tint = CoffeeCream, modifier = Modifier.size(18.dp))
        }
      }
    }
  }
}
