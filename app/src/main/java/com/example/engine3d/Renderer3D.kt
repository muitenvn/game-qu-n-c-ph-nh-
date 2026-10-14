package com.example.engine3d

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.data.model.*
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

object Renderer3D {

  // Sun / Warm Golden Directional Light source
  private val lightDir = Vec3(0.50f, 0.88f, -0.32f).normalized()
  private const val AMBIENT_LIGHT = 0.50f
  private const val DIFFUSE_LIGHT = 0.50f

  // Performance caches
  private var lastWeather: WeatherType? = null
  private var cachedEnvironment: List<Polygon3D> = emptyList()

  private var lastFurnitureHash: Int = 0
  private var cachedFurniture: List<Polygon3D> = emptyList()

  // Reusable graphics instances to avoid GC allocations
  private val renderPath = Path()
  private val edgeStroke = Stroke(width = 0.5f)
  private val edgeColor = Color(0x143E2723) // Soft warm coffee tone
  private val activePolygons = ArrayList<Polygon3D>(600)
  private val visiblePolygons = ArrayList<Polygon3D>(600)

  private val bubbleTextPaint = android.graphics.Paint().apply {
    color = android.graphics.Color.rgb(0x3E, 0x27, 0x23)
    textSize = 21f
    isAntiAlias = true
    textAlign = android.graphics.Paint.Align.CENTER
    typeface = android.graphics.Typeface.DEFAULT_BOLD
  }

  private val badgeTextPaint = android.graphics.Paint().apply {
    color = android.graphics.Color.WHITE
    textSize = 18f
    isAntiAlias = true
    textAlign = android.graphics.Paint.Align.CENTER
    typeface = android.graphics.Typeface.DEFAULT_BOLD
  }

  private val dirtyBadgePaint = android.graphics.Paint().apply {
    color = android.graphics.Color.WHITE
    textSize = 17f
    isAntiAlias = true
    textAlign = android.graphics.Paint.Align.CENTER
    typeface = android.graphics.Typeface.DEFAULT_BOLD
  }

  private val notePaint = android.graphics.Paint().apply {
    color = android.graphics.Color.rgb(0xFF, 0x8F, 0x00)
    textSize = 24f
    isAntiAlias = true
    textAlign = android.graphics.Paint.Align.CENTER
  }

  fun renderScene3D(
    drawScope: DrawScope,
    canvasSize: Size,
    camera: Camera3D,
    furnitureList: List<FurnitureItem>,
    staffList: List<StaffMember>,
    customerList: List<Customer>,
    weather: WeatherType,
    timeSeconds: Float,
    isBuildMode: Boolean,
    selectedPlacementType: ItemType?,
    hoveredGridX: Int?,
    hoveredGridZ: Int?,
    isValidPlacement: Boolean,
    panOffset: Offset = Offset.Zero
  ) {
    with(drawScope) {
      val screenCenter = Offset(canvasSize.width / 2f + panOffset.x, canvasSize.height / 2f + panOffset.y)
      val forward = camera.getForwardVector()
      val right = camera.getRightVector()
      val up = camera.getUpVector()

      // 1. Maintain Static Environment Cache (Built once per weather)
      if (lastWeather != weather || cachedEnvironment.isEmpty()) {
        cachedEnvironment = ModelFactory3D.createEnvironment3D(weather)
        lastWeather = weather
      }

      // 2. Maintain Static Furniture Cache (Rebuilt only on layout change)
      var currentFurnitureHash = furnitureList.size
      for (f in furnitureList) {
        currentFurnitureHash = 31 * currentFurnitureHash + f.gridX
        currentFurnitureHash = 31 * currentFurnitureHash + f.gridY
        currentFurnitureHash = 31 * currentFurnitureHash + if (f.isDirty) 1 else 0
      }
      if (currentFurnitureHash != lastFurnitureHash || cachedFurniture.isEmpty()) {
        val fList = ArrayList<Polygon3D>(furnitureList.size * 22)
        for (furn in furnitureList) {
          fList.addAll(ModelFactory3D.createFurniture3D(furn, timeSeconds))
        }
        cachedFurniture = fList
        lastFurnitureHash = currentFurnitureHash
      }

      // 3. Assemble active polygons using pooled list
      activePolygons.clear()
      activePolygons.addAll(cachedEnvironment)
      activePolygons.addAll(cachedFurniture)

      // Dynamic entities: Staff
      for (staff in staffList) {
        val isMoving = (staff.posX != staff.targetX || staff.posY != staff.targetY)
        activePolygons.addAll(
          ModelFactory3D.createCharacter3D(
            worldX = staff.posX,
            worldZ = staff.posY,
            rotationYDeg = 0f,
            isMoving = isMoving,
            walkTime = timeSeconds,
            role = staff.role,
            shirtColor = Color(staff.shirtColorHex),
            skinColor = Color(0xFFFFD180),
            hairColor = Color(0xFF4E342E),
            hasApron = true,
            isHoldingDrink = staff.state == StaffState.CARRYING_ORDER,
            isCleaning = staff.state == StaffState.CLEANING_TABLE,
            hasUmbrella = false
          )
        )
      }

      // Dynamic entities: Customers
      for (cust in customerList) {
        val isMoving = (cust.posX != cust.targetX || cust.posY != cust.targetY)
        val isOutside = cust.posY >= 7.5f
        activePolygons.addAll(
          ModelFactory3D.createCharacter3D(
            worldX = cust.posX,
            worldZ = cust.posY,
            rotationYDeg = if (cust.directionX < 0) 180f else 0f,
            isMoving = isMoving,
            walkTime = timeSeconds,
            role = null,
            shirtColor = Color(cust.shirtColorHex),
            skinColor = Color(cust.skinToneHex),
            hairColor = Color(0xFF212121),
            hasApron = false,
            isHoldingDrink = cust.state == CustomerState.CONSUMING,
            isCleaning = false,
            hasUmbrella = weather == WeatherType.RAINY && isOutside,
            umbrellaColor = if (cust.umbrellaColorHex != null) Color(cust.umbrellaColorHex) else Color(0xFFE53935)
          )
        )
      }

      // Build Mode Ghost Preview
      if (isBuildMode && hoveredGridX != null && hoveredGridZ != null && selectedPlacementType != null) {
        val previewItem = FurnitureItem("preview", selectedPlacementType, hoveredGridX, hoveredGridZ)
        val ghostCol = if (isValidPlacement) Color(0x994CAF50) else Color(0x99F44336)
        val pList = ModelFactory3D.createFurniture3D(previewItem, timeSeconds)
        for (p in pList) {
          activePolygons.add(p.copy(baseColor = ghostCol, emitLight = true))
        }
      }

      // 4. Transform, Back-face Cull, Screen Project, and Viewport Cull
      visiblePolygons.clear()
      val screenW = canvasSize.width
      val screenH = canvasSize.height
      val margin = 80f

      for (poly in activePolygons) {
        // Back-face culling
        if (!poly.isDoubleSided && poly.normal.dot(forward) > 0.05f) {
          continue
        }

        // Depth calculation
        var sumX = 0f
        var sumY = 0f
        var sumZ = 0f
        val vSize = poly.vertices.size
        for (v in poly.vertices) {
          sumX += v.x
          sumY += v.y
          sumZ += v.z
        }
        val centerV = Vec3(sumX / vSize, sumY / vSize, sumZ / vSize)
        poly.depth = centerV.dot(forward)

        // Warm Cel-Shading & Golden Sunlight Color Grading
        if (poly.emitLight) {
          poly.shadedColor = poly.baseColor
        } else {
          val ndotl = max(0f, poly.normal.dot(lightDir))
          val brightness = (AMBIENT_LIGHT + DIFFUSE_LIGHT * ndotl).coerceIn(0.35f, 1.05f)
          // Golden sun warmth
          val rShaded = (poly.baseColor.red * brightness * 1.02f).coerceIn(0f, 1f)
          val gShaded = (poly.baseColor.green * brightness * 1.00f).coerceIn(0f, 1f)
          val bShaded = (poly.baseColor.blue * brightness * 0.96f).coerceIn(0f, 1f)
          poly.shadedColor = Color(
            red = rShaded,
            green = gShaded,
            blue = bShaded,
            alpha = poly.baseColor.alpha
          )
        }

        // Screen Projection & Screen Bound Check
        var minX = Float.MAX_VALUE
        var maxX = -Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxY = -Float.MAX_VALUE

        val projected = poly.vertices.map { v ->
          val rel = v - camera.target
          val camX = rel.dot(right)
          val camY = rel.dot(up)
          val sx = screenCenter.x + camX * camera.zoom
          val sy = screenCenter.y - camY * camera.zoom
          if (sx < minX) minX = sx
          if (sx > maxX) maxX = sx
          if (sy < minY) minY = sy
          if (sy > maxY) maxY = sy
          Offset(sx, sy)
        }

        // Viewport frustum culling: skip drawing if entirely offscreen
        if (maxX < -margin || minX > screenW + margin || maxY < -margin || minY > screenH + margin) {
          continue
        }

        poly.projectedPoints = projected
        visiblePolygons.add(poly)
      }

      // 5. Depth Sort (Painter's algorithm: draw furthest first)
      visiblePolygons.sortByDescending { it.depth }

      // 6. Draw Polygons using single reusable Path
      for (poly in visiblePolygons) {
        val pts = poly.projectedPoints
        if (pts.size < 3) continue
        renderPath.reset()
        renderPath.moveTo(pts[0].x, pts[0].y)
        for (i in 1 until pts.size) {
          renderPath.lineTo(pts[i].x, pts[i].y)
        }
        renderPath.close()

        drawPath(renderPath, poly.shadedColor)
        if (!poly.emitLight && poly.baseColor.alpha > 0.8f) {
          drawPath(renderPath, edgeColor, style = edgeStroke)
        }
      }

      // 7. Interactive Floating Overlays & Atmospheres
      if (isBuildMode && hoveredGridX != null && hoveredGridZ != null) {
        drawGridTileHighlight(drawScope, right, up, screenCenter, camera, hoveredGridX, hoveredGridZ, isValidPlacement)
      }

      drawBillboards(drawScope, right, up, screenCenter, camera, customerList, staffList, furnitureList, timeSeconds)
      drawWeatherAtmosphere(drawScope, canvasSize, weather, timeSeconds)
    }
  }

  private fun drawGridTileHighlight(
    drawScope: DrawScope,
    right: Vec3,
    up: Vec3,
    screenCenter: Offset,
    camera: Camera3D,
    gx: Int,
    gz: Int,
    isValid: Boolean
  ) {
    val corners = listOf(
      Vec3(gx.toFloat(), 0.02f, gz.toFloat()),
      Vec3((gx + 1).toFloat(), 0.02f, gz.toFloat()),
      Vec3((gx + 1).toFloat(), 0.02f, (gz + 1).toFloat()),
      Vec3(gx.toFloat(), 0.02f, (gz + 1).toFloat())
    )

    val screenPts = corners.map { v ->
      val rel = v - camera.target
      val camX = rel.dot(right)
      val camY = rel.dot(up)
      Offset(screenCenter.x + camX * camera.zoom, screenCenter.y - camY * camera.zoom)
    }

    renderPath.reset()
    renderPath.moveTo(screenPts[0].x, screenPts[0].y)
    for (i in 1 until screenPts.size) renderPath.lineTo(screenPts[i].x, screenPts[i].y)
    renderPath.close()

    val fillCol = if (isValid) Color(0x664CAF50) else Color(0x66F44336)
    val strokeCol = if (isValid) Color(0xFF2E7D32) else Color(0xFFD32F2F)
    drawScope.drawPath(renderPath, fillCol)
    drawScope.drawPath(renderPath, strokeCol, style = Stroke(width = 2.5f))
  }

  private fun drawBillboards(
    drawScope: DrawScope,
    right: Vec3,
    up: Vec3,
    screenCenter: Offset,
    camera: Camera3D,
    customers: List<Customer>,
    staffList: List<StaffMember>,
    furnitureList: List<FurnitureItem>,
    timeSeconds: Float
  ) {
    // 1. Customer Thought Bubbles
    for (c in customers) {
      val bubble = c.thoughtBubble
      if (!bubble.isNullOrEmpty()) {
        val headWorld = Vec3(c.posX, 1.48f, c.posY)
        val rel = headWorld - camera.target
        val sx = screenCenter.x + rel.dot(right) * camera.zoom
        val sy = screenCenter.y - rel.dot(up) * camera.zoom
        drawBubble(drawScope, Offset(sx, sy), bubble)
      }
    }

    // 2. Staff Action Badges
    for (s in staffList) {
      val badgeText = when (s.state) {
        StaffState.BREWING -> "☕ Đang pha"
        StaffState.CLEANING_TABLE -> "🧹 Dọn bàn"
        StaffState.CARRYING_ORDER -> "🥤 Bưng món"
        else -> null
      }
      if (badgeText != null) {
        val headWorld = Vec3(s.posX, 1.42f, s.posY)
        val rel = headWorld - camera.target
        val sx = screenCenter.x + rel.dot(right) * camera.zoom
        val sy = screenCenter.y - rel.dot(up) * camera.zoom
        drawBadge(drawScope, Offset(sx, sy), badgeText)
      }
    }

    // 3. Dirty Table Interactive Badge ("🧹 Cần lau")
    for (furn in furnitureList) {
      if (furn.isTable && furn.isDirty) {
        val tableWorld = Vec3(furn.gridX + 0.5f, 0.95f, furn.gridY + 0.5f)
        val rel = tableWorld - camera.target
        val sx = screenCenter.x + rel.dot(right) * camera.zoom
        val sy = screenCenter.y - rel.dot(up) * camera.zoom
        drawDirtyTableBadge(drawScope, Offset(sx, sy))
      }
    }

    // 4. Steaming Espresso Machine Animated Steam Puffs
    val espresso = furnitureList.find { it.type == ItemType.ESPRESSO_MACHINE }
    if (espresso != null) {
      val steamWorld = Vec3(espresso.gridX + 0.5f, 1.30f, espresso.gridY + 0.5f)
      val rel = steamWorld - camera.target
      val sx = screenCenter.x + rel.dot(right) * camera.zoom
      val sy = screenCenter.y - rel.dot(up) * camera.zoom
      drawSteamEffect(drawScope, Offset(sx, sy), timeSeconds)
    }

    // 5. Acoustic Speaker Floating Music Notes
    val speaker = furnitureList.find { it.type == ItemType.ACOUSTIC_SPEAKER }
    if (speaker != null) {
      val speakerWorld = Vec3(speaker.gridX + 0.5f, 1.15f, speaker.gridY + 0.5f)
      val rel = speakerWorld - camera.target
      val sx = screenCenter.x + rel.dot(right) * camera.zoom
      val sy = screenCenter.y - rel.dot(up) * camera.zoom
      drawMusicEffect(drawScope, Offset(sx, sy), timeSeconds)
    }
  }

  private fun drawBubble(drawScope: DrawScope, pos: Offset, text: String) {
    val textWidth = bubbleTextPaint.measureText(text)
    val w = (textWidth + 24f).coerceAtLeast(80f)
    val h = 32f
    val topLeft = Offset(pos.x - w / 2f, pos.y - h)
    with(drawScope) {
      // Bubble drop shadow
      drawRoundRect(
        color = Color(0x22000000),
        topLeft = Offset(topLeft.x + 2f, topLeft.y + 2f),
        size = Size(w, h),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
      )
      // Bubble background
      drawRoundRect(
        color = Color(0xFFFFFDF9),
        topLeft = topLeft,
        size = Size(w, h),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
      )
      // Bubble border
      drawRoundRect(
        color = Color(0xFF6D4C41),
        topLeft = topLeft,
        size = Size(w, h),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f),
        style = Stroke(width = 1.6f)
      )
      drawContext.canvas.nativeCanvas.drawText(
        text,
        pos.x,
        pos.y - h / 2f + 7f,
        bubbleTextPaint
      )
    }
  }

  private fun drawBadge(drawScope: DrawScope, pos: Offset, label: String) {
    val textWidth = badgeTextPaint.measureText(label)
    val w = (textWidth + 20f).coerceAtLeast(70f)
    val h = 26f
    val topLeft = Offset(pos.x - w / 2f, pos.y - h)
    with(drawScope) {
      drawRoundRect(
        color = Color(0xEE3E2723),
        topLeft = topLeft,
        size = Size(w, h),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
      )
      drawRoundRect(
        color = Color(0xFFFFD54F),
        topLeft = topLeft,
        size = Size(w, h),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f),
        style = Stroke(width = 1.2f)
      )
      drawContext.canvas.nativeCanvas.drawText(
        label,
        pos.x,
        pos.y - h / 2f + 6f,
        badgeTextPaint
      )
    }
  }

  private fun drawDirtyTableBadge(drawScope: DrawScope, pos: Offset) {
    val label = "🧹 Bấm lau (+1.000đ)"
    val textWidth = dirtyBadgePaint.measureText(label)
    val w = textWidth + 18f
    val h = 24f
    val topLeft = Offset(pos.x - w / 2f, pos.y - h)
    with(drawScope) {
      drawRoundRect(
        color = Color(0xEEFF6F00),
        topLeft = topLeft,
        size = Size(w, h),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
      )
      drawRoundRect(
        color = Color(0xFFFFF8E1),
        topLeft = topLeft,
        size = Size(w, h),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f),
        style = Stroke(width = 1.2f)
      )
      drawContext.canvas.nativeCanvas.drawText(
        label,
        pos.x,
        pos.y - h / 2f + 5.5f,
        dirtyBadgePaint
      )
    }
  }

  private fun drawSteamEffect(drawScope: DrawScope, pos: Offset, timeSeconds: Float) {
    with(drawScope) {
      for (i in 0..2) {
        val t = (timeSeconds * 1.5f + i * 0.7f) % 2.0f
        val steamY = pos.y - t * 24f
        val steamX = pos.x + sin(t * 3f + i) * 6f
        val alpha = (1.0f - t / 2.0f).coerceIn(0f, 0.6f)
        val radius = 3.5f + t * 4f
        drawCircle(
          color = Color.White.copy(alpha = alpha),
          radius = radius,
          center = Offset(steamX, steamY)
        )
      }
    }
  }

  private fun drawMusicEffect(drawScope: DrawScope, pos: Offset, timeSeconds: Float) {
    val t = (timeSeconds * 0.8f) % 2.5f
    val noteY = pos.y - t * 28f
    val noteX = pos.x + sin(t * 2.5f) * 10f
    val note = if (((timeSeconds * 0.8f).toInt()) % 2 == 0) "🎵" else "🎶"
    drawScope.drawContext.canvas.nativeCanvas.drawText(note, noteX, noteY, notePaint)
  }

  private fun drawWeatherAtmosphere(drawScope: DrawScope, size: Size, weather: WeatherType, timeSeconds: Float) {
    with(drawScope) {
      if (weather == WeatherType.RAINY) {
        for (i in 0 until 45) {
          val rx = ((i * 43f + timeSeconds * 170f) % size.width)
          val ry = ((i * 79f + timeSeconds * 520f) % size.height)
          drawLine(
            color = Color(0x9990CAF9),
            start = Offset(rx, ry),
            end = Offset(rx - 6f, ry + 18f),
            strokeWidth = 2f
          )
        }
      } else if (weather == WeatherType.HEATWAVE) {
        drawRect(
          brush = Brush.radialGradient(
            colors = listOf(Color.Transparent, Color(0x24FF9800)),
            center = Offset(size.width / 2f, size.height / 2f),
            radius = size.width / 1.3f
          )
        )
      } else if (weather == WeatherType.SUNNY) {
        // Soft golden sunbeam warmth
        drawRect(
          brush = Brush.radialGradient(
            colors = listOf(Color(0x12FFE082), Color.Transparent),
            center = Offset(size.width * 0.35f, size.height * 0.30f),
            radius = size.width * 0.70f
          )
        )
      }
    }
  }
}
