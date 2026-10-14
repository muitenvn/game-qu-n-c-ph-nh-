package com.example.engine

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.data.model.*
import kotlin.math.sin

object IsometricRenderer {
  const val TILE_HALF_W = 44f
  const val TILE_HALF_H = 22f
  const val GRID_SIZE = 8

  // Transforms grid (gx, gy) to 2D screen coordinate
  fun gridToScreen(gx: Float, gy: Float, origin: Offset, scale: Float, elevation: Float = 0f): Offset {
    val isoX = (gx - gy) * TILE_HALF_W * scale
    val isoY = ((gx + gy) * TILE_HALF_H - elevation * 20f) * scale
    return Offset(origin.x + isoX, origin.y + isoY)
  }

  // Inverse transforms 2D screen coordinate to grid (gx, gy)
  fun screenToGrid(screen: Offset, origin: Offset, scale: Float): Pair<Float, Float> {
    val relX = (screen.x - origin.x) / (TILE_HALF_W * scale)
    val relY = (screen.y - origin.y) / (TILE_HALF_H * scale)
    val gx = (relY + relX) / 2f
    val gy = (relY - relX) / 2f
    return Pair(gx, gy)
  }

  // Draw complete scene
  fun drawScene(
    drawScope: DrawScope,
    canvasSize: Size,
    origin: Offset,
    scale: Float,
    timeSeconds: Float,
    weather: WeatherType,
    furnitureList: List<FurnitureItem>,
    staffList: List<StaffMember>,
    customerList: List<Customer>,
    selectedPlacementItem: ItemType?,
    hoveredGridX: Int?,
    hoveredGridY: Int?,
    isValidPlacement: Boolean
  ) {
    with(drawScope) {
      // 1. Draw Outdoor Environment & Sidewalk
      drawOutdoorEnvironment(origin, scale, weather, timeSeconds)

      // 2. Draw Cafe Interior Floor & Walls
      drawCafeFloor(origin, scale)
      drawCafeWalls(origin, scale)

      // 3. Draw Placement Grid Highlight if in build mode
      if (selectedPlacementItem != null && hoveredGridX != null && hoveredGridY != null) {
        drawPlacementTile(origin, scale, hoveredGridX, hoveredGridY, isValidPlacement)
      }

      // 4. Sort and render all 3D entities (depth sorting based on gx + gy)
      renderDepthSortedEntities(
        origin = origin,
        scale = scale,
        timeSeconds = timeSeconds,
        furnitureList = furnitureList,
        staffList = staffList,
        customerList = customerList,
        selectedPlacementItem = selectedPlacementItem,
        hoveredGridX = hoveredGridX,
        hoveredGridY = hoveredGridY
      )

      // 5. Weather Effects (Rain particles, sun glow)
      drawWeatherAtmosphere(canvasSize, weather, timeSeconds)
    }
  }

  private fun DrawScope.drawOutdoorEnvironment(origin: Offset, scale: Float, weather: WeatherType, timeSeconds: Float) {
    // Sidewalk rows: gy = 8, 9, 10 for gx = -2 to 9
    val sidewalkColor = Color(0xFFD7CCC8)
    val sidewalkEdgeColor = Color(0xFFBCAAA4)
    val streetColor = Color(0xFF78909C)

    // Sidewalk tiles
    for (gx in -2..9) {
      for (gy in 8..10) {
        val p = gridToScreen(gx.toFloat(), gy.toFloat(), origin, scale)
        val path = Path().apply {
          moveTo(p.x, p.y)
          lineTo(p.x + TILE_HALF_W * scale, p.y + TILE_HALF_H * scale)
          lineTo(p.x, p.y + TILE_HALF_H * 2f * scale)
          lineTo(p.x - TILE_HALF_W * scale, p.y + TILE_HALF_H * scale)
          close()
        }
        val tileVariant = if ((gx + gy) % 2 == 0) sidewalkColor else Color(0xFFEFEBE9)
        drawPath(path, tileVariant)
        drawPath(path, Color(0xFFB0BEC5), style = Stroke(width = 1f * scale))
      }
    }

    // Street Asphalt beyond sidewalk (gy = 11..12)
    for (gx in -2..9) {
      for (gy in 11..12) {
        val p = gridToScreen(gx.toFloat(), gy.toFloat(), origin, scale)
        val path = Path().apply {
          moveTo(p.x, p.y)
          lineTo(p.x + TILE_HALF_W * scale, p.y + TILE_HALF_H * scale)
          lineTo(p.x, p.y + TILE_HALF_H * 2f * scale)
          lineTo(p.x - TILE_HALF_W * scale, p.y + TILE_HALF_H * scale)
          close()
        }
        drawPath(path, streetColor)
      }
    }

    // Street Curb 3D rim along gy = 10.5
    for (gx in -2..9) {
      val pFront = gridToScreen(gx.toFloat(), 10.5f, origin, scale)
      drawRect(
        color = sidewalkEdgeColor,
        topLeft = Offset(pFront.x - TILE_HALF_W * scale, pFront.y),
        size = Size(TILE_HALF_W * 2f * scale, 6f * scale)
      )
    }

    // Outdoor Street Lamps and Trees
    drawOutdoorTree(origin, scale, -1f, 8f)
    drawOutdoorTree(origin, scale, 9f, 8f)
    drawStreetLamp(origin, scale, 1f, 8f, weather)
    drawStreetLamp(origin, scale, 7f, 8f, weather)
  }

  private fun DrawScope.drawCafeFloor(origin: Offset, scale: Float) {
    // 8x8 Wooden Parquet tiles inside cafe
    val woodLight = Color(0xFFE8D3B9)
    val woodDark = Color(0xFFD7BA98)
    val woodBorder = Color(0xFFB89065)

    for (gx in 0 until GRID_SIZE) {
      for (gy in 0 until GRID_SIZE) {
        val p = gridToScreen(gx.toFloat(), gy.toFloat(), origin, scale)
        val path = Path().apply {
          moveTo(p.x, p.y)
          lineTo(p.x + TILE_HALF_W * scale, p.y + TILE_HALF_H * scale)
          lineTo(p.x, p.y + TILE_HALF_H * 2f * scale)
          lineTo(p.x - TILE_HALF_W * scale, p.y + TILE_HALF_H * scale)
          close()
        }
        val isAlternate = (gx + gy) % 2 == 0
        drawPath(path, if (isAlternate) woodLight else woodDark)
        drawPath(path, woodBorder, style = Stroke(width = 1f * scale))
      }
    }

    // Entrance "Welcome" mat at (3, 7) - (4, 7)
    val matPos = gridToScreen(3.5f, 7.3f, origin, scale)
    val matPath = Path().apply {
      moveTo(matPos.x, matPos.y)
      lineTo(matPos.x + TILE_HALF_W * 0.9f * scale, matPos.y + TILE_HALF_H * 0.9f * scale)
      lineTo(matPos.x, matPos.y + TILE_HALF_H * 1.8f * scale)
      lineTo(matPos.x - TILE_HALF_W * 0.9f * scale, matPos.y + TILE_HALF_H * 0.9f * scale)
      close()
    }
    drawPath(matPath, Color(0xFF8D6E63))
    drawPath(matPath, Color(0xFF5D4037), style = Stroke(width = 2f * scale))
  }

  private fun DrawScope.drawCafeWalls(origin: Offset, scale: Float) {
    val wallHeight = 70f * scale
    val wallColorTop = Color(0xFFFAF3EC)
    val wallColorBack = Color(0xFFECE0D1)
    val wallBaseboard = Color(0xFF8D6E63)

    // Back-left wall: gx = 0, gy = 0 to 8
    val pTopCorner = gridToScreen(0f, 0f, origin, scale)
    val pLeftEnd = gridToScreen(0f, 8f, origin, scale)
    val pRightEnd = gridToScreen(8f, 0f, origin, scale)

    // Wall Left
    val wallLeftPath = Path().apply {
      moveTo(pTopCorner.x, pTopCorner.y)
      lineTo(pLeftEnd.x, pLeftEnd.y)
      lineTo(pLeftEnd.x, pLeftEnd.y - wallHeight)
      lineTo(pTopCorner.x, pTopCorner.y - wallHeight)
      close()
    }
    drawPath(wallLeftPath, wallColorBack)

    // Wall Right (Back)
    val wallRightPath = Path().apply {
      moveTo(pTopCorner.x, pTopCorner.y)
      lineTo(pRightEnd.x, pRightEnd.y)
      lineTo(pRightEnd.x, pRightEnd.y - wallHeight)
      lineTo(pTopCorner.x, pTopCorner.y - wallHeight)
      close()
    }
    drawPath(wallRightPath, wallColorTop)

    // Wall Baseboard moldings
    drawLine(
      color = wallBaseboard,
      start = Offset(pTopCorner.x, pTopCorner.y - 4f * scale),
      end = Offset(pLeftEnd.x, pLeftEnd.y - 4f * scale),
      strokeWidth = 6f * scale
    )
    drawLine(
      color = wallBaseboard,
      start = Offset(pTopCorner.x, pTopCorner.y - 4f * scale),
      end = Offset(pRightEnd.x, pRightEnd.y - 4f * scale),
      strokeWidth = 6f * scale
    )

    // Cafe Wall Glass Windows & Framed Poster on walls
    val winPos = gridToScreen(4f, 0f, origin, scale)
    drawRoundRect(
      color = Color(0xFFB0BEC5).copy(alpha = 0.5f),
      topLeft = Offset(winPos.x - 28f * scale, winPos.y - wallHeight + 14f * scale),
      size = Size(56f * scale, 34f * scale),
      cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f * scale, 4f * scale)
    )
    drawRoundRect(
      color = Color(0xFF5D4037),
      topLeft = Offset(winPos.x - 28f * scale, winPos.y - wallHeight + 14f * scale),
      size = Size(56f * scale, 34f * scale),
      cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f * scale, 4f * scale),
      style = Stroke(width = 2.5f * scale)
    )
  }

  private fun DrawScope.drawPlacementTile(origin: Offset, scale: Float, gx: Int, gy: Int, isValid: Boolean) {
    val p = gridToScreen(gx.toFloat(), gy.toFloat(), origin, scale)
    val fillCol = if (isValid) Color(0x774CAF50) else Color(0x77F44336)
    val strokeCol = if (isValid) Color(0xFF2E7D32) else Color(0xFFD32F2F)

    val path = Path().apply {
      moveTo(p.x, p.y)
      lineTo(p.x + TILE_HALF_W * scale, p.y + TILE_HALF_H * scale)
      lineTo(p.x, p.y + TILE_HALF_H * 2f * scale)
      lineTo(p.x - TILE_HALF_W * scale, p.y + TILE_HALF_H * scale)
      close()
    }
    drawPath(path, fillCol)
    drawPath(path, strokeCol, style = Stroke(width = 3f * scale))
  }

  private sealed class RenderEntity(val depthKey: Float) {
    class Furniture(val item: FurnitureItem, depth: Float) : RenderEntity(depth)
    class Staff(val member: StaffMember, depth: Float) : RenderEntity(depth)
    class CustomerEntity(val cust: Customer, depth: Float) : RenderEntity(depth)
    class Preview(val type: ItemType, val gx: Int, val gy: Int, depth: Float) : RenderEntity(depth)
  }

  private fun DrawScope.renderDepthSortedEntities(
    origin: Offset,
    scale: Float,
    timeSeconds: Float,
    furnitureList: List<FurnitureItem>,
    staffList: List<StaffMember>,
    customerList: List<Customer>,
    selectedPlacementItem: ItemType?,
    hoveredGridX: Int?,
    hoveredGridY: Int?
  ) {
    val entities = mutableListOf<RenderEntity>()

    for (f in furnitureList) {
      entities.add(RenderEntity.Furniture(f, f.gridX + f.gridY + 0.1f))
    }
    for (s in staffList) {
      entities.add(RenderEntity.Staff(s, s.posX + s.posY + 0.3f))
    }
    for (c in customerList) {
      entities.add(RenderEntity.CustomerEntity(c, c.posX + c.posY + 0.35f))
    }
    if (selectedPlacementItem != null && hoveredGridX != null && hoveredGridY != null) {
      entities.add(RenderEntity.Preview(selectedPlacementItem, hoveredGridX, hoveredGridY, hoveredGridX + hoveredGridY + 0.15f))
    }

    // Sort ascending by depthKey
    entities.sortBy { it.depthKey }

    for (entity in entities) {
      when (entity) {
        is RenderEntity.Furniture -> drawFurnitureItem(origin, scale, entity.item, timeSeconds)
        is RenderEntity.Staff -> drawStaffCharacter(origin, scale, entity.member, timeSeconds)
        is RenderEntity.CustomerEntity -> drawCustomerCharacter(origin, scale, entity.cust, timeSeconds)
        is RenderEntity.Preview -> drawFurniturePreview(origin, scale, entity.type, entity.gx, entity.gy)
      }
    }
  }

  // 3D Voxel / Isometric Furniture Drawing
  private fun DrawScope.drawFurnitureItem(origin: Offset, scale: Float, item: FurnitureItem, timeSeconds: Float) {
    val center = gridToScreen(item.gridX + 0.5f, item.gridY + 0.5f, origin, scale)

    when (item.type) {
      ItemType.ORDER_COUNTER -> drawOrderCounter(center, scale)
      ItemType.ESPRESSO_MACHINE -> drawEspressoMachine(center, scale, timeSeconds)
      ItemType.PASTRY_DISPLAY -> drawPastryDisplay(center, scale)
      ItemType.BEVERAGE_COOLER -> drawBeverageCooler(center, scale)
      ItemType.FRIDGE -> drawFridge(center, scale)
      ItemType.TABLE_ROUND_2P -> drawTableRound(center, scale, item.isDirty)
      ItemType.TABLE_SOFA_4P -> drawTableSofa(center, scale, item.isDirty)
      ItemType.BAR_STOOL_TABLE -> drawBarStool(center, scale, item.isDirty)
      ItemType.PLANT_MONSTERA -> drawPlantMonstera(center, scale, timeSeconds)
      ItemType.VINTAGE_LAMP -> drawVintageLamp(center, scale)
      ItemType.ACOUSTIC_SPEAKER -> drawAcousticSpeaker(center, scale, timeSeconds)
      ItemType.PERSIAN_RUG -> drawPersianRug(center, scale)
    }

    // Dirty Table Indicator
    if (item.isDirty) {
      drawDirtyTableIndicator(center, scale)
    }
  }

  private fun DrawScope.drawFurniturePreview(origin: Offset, scale: Float, type: ItemType, gx: Int, gy: Int) {
    val center = gridToScreen(gx + 0.5f, gy + 0.5f, origin, scale)
    // Draw semi-transparent ghost
    drawCircle(
      color = Color(0x884CAF50),
      radius = 24f * scale,
      center = center
    )
  }

  // Quầy Thu Ngân (Order Counter)
  private fun DrawScope.drawOrderCounter(center: Offset, scale: Float) {
    val h = 26f * scale
    val w = 32f * scale
    val d = 16f * scale

    // Counter Base Block (Top, Left, Right faces)
    drawIsometricBlock(
      center = center,
      width = w,
      height = h,
      depth = d,
      topColor = Color(0xFFE0C49F),
      leftColor = Color(0xFFB88A58),
      rightColor = Color(0xFFCBA16E)
    )

    // Cash Register on top
    val regCenter = Offset(center.x, center.y - h + 2f * scale)
    drawIsometricBlock(
      center = regCenter,
      width = 14f * scale,
      height = 10f * scale,
      depth = 10f * scale,
      topColor = Color(0xFF455A64),
      leftColor = Color(0xFF263238),
      rightColor = Color(0xFF37474F)
    )
    // Little Service Bell
    drawCircle(
      color = Color(0xFFFFD54F),
      radius = 3f * scale,
      center = Offset(center.x + 8f * scale, center.y - h + 2f * scale)
    )
  }

  // Máy Espresso Cao Cấp
  private fun DrawScope.drawEspressoMachine(center: Offset, scale: Float, timeSeconds: Float) {
    val counterH = 24f * scale
    // Base Table
    drawIsometricBlock(
      center = center,
      width = 32f * scale,
      height = counterH,
      depth = 16f * scale,
      topColor = Color(0xFFD7CCC8),
      leftColor = Color(0xFF8D6E63),
      rightColor = Color(0xFFA1887F)
    )

    // Espresso Machine Body (Sleek Stainless Steel with Pastel Mint/Red accent)
    val machCenter = Offset(center.x, center.y - counterH)
    drawIsometricBlock(
      center = machCenter,
      width = 24f * scale,
      height = 20f * scale,
      depth = 14f * scale,
      topColor = Color(0xFFECEFF1),
      leftColor = Color(0xFF90A4AE),
      rightColor = Color(0xFFCFD8DC)
    )

    // Portafilter & Cups
    drawCircle(
      color = Color(0xFF3E2723),
      radius = 4f * scale,
      center = Offset(machCenter.x - 4f * scale, machCenter.y - 2f * scale)
    )
    drawCircle(
      color = Color(0xFFE91E63),
      radius = 3f * scale,
      center = Offset(machCenter.x + 4f * scale, machCenter.y - 2f * scale)
    )

    // Steam Animation rising from cups
    val steamOffset = (timeSeconds * 30f) % 20f
    val steamAlpha = (1f - (steamOffset / 20f)).coerceIn(0f, 0.8f)
    drawCircle(
      color = Color.White.copy(alpha = steamAlpha),
      radius = 3.5f * scale,
      center = Offset(machCenter.x - 4f * scale, machCenter.y - 12f * scale - steamOffset * scale)
    )
  }

  // Tủ Bánh Ngọt
  private fun DrawScope.drawPastryDisplay(center: Offset, scale: Float) {
    val h = 24f * scale
    // Base wooden stand
    drawIsometricBlock(
      center = center,
      width = 30f * scale,
      height = h,
      depth = 16f * scale,
      topColor = Color(0xFFD7CCC8),
      leftColor = Color(0xFF8D6E63),
      rightColor = Color(0xFFA1887F)
    )
    // Glass Display Case on top
    val glassCenter = Offset(center.x, center.y - h)
    drawIsometricBlock(
      center = glassCenter,
      width = 26f * scale,
      height = 14f * scale,
      depth = 12f * scale,
      topColor = Color(0x66B0BEC5),
      leftColor = Color(0x8878909C),
      rightColor = Color(0x9990A4AE)
    )
    // Golden Croissants inside
    drawCircle(
      color = Color(0xFFFFB300),
      radius = 3.5f * scale,
      center = Offset(glassCenter.x - 4f * scale, glassCenter.y - 2f * scale)
    )
    drawCircle(
      color = Color(0xFFFF8F00),
      radius = 3.5f * scale,
      center = Offset(glassCenter.x + 4f * scale, glassCenter.y - 2f * scale)
    )
  }

  // Tủ Mát Nước Đóng Chai
  private fun DrawScope.drawBeverageCooler(center: Offset, scale: Float) {
    drawIsometricBlock(
      center = center,
      width = 20f * scale,
      height = 36f * scale,
      depth = 14f * scale,
      topColor = Color(0xFF80DEEA),
      leftColor = Color(0xFF00838F),
      rightColor = Color(0xFF0097A7)
    )
    // Glass Door reflection
    drawLine(
      color = Color.White.copy(alpha = 0.5f),
      start = Offset(center.x - 6f * scale, center.y - 28f * scale),
      end = Offset(center.x + 4f * scale, center.y - 12f * scale),
      strokeWidth = 2f * scale
    )
  }

  // Tủ Lạnh Sữa & Nguyên Liệu
  private fun DrawScope.drawFridge(center: Offset, scale: Float) {
    drawIsometricBlock(
      center = center,
      width = 22f * scale,
      height = 38f * scale,
      depth = 16f * scale,
      topColor = Color(0xFFECEFF1),
      leftColor = Color(0xFFB0BEC5),
      rightColor = Color(0xFFCFD8DC)
    )
    // Handle
    drawLine(
      color = Color(0xFF455A64),
      start = Offset(center.x + 4f * scale, center.y - 20f * scale),
      end = Offset(center.x + 4f * scale, center.y - 12f * scale),
      strokeWidth = 2.5f * scale
    )
  }

  // Bàn Tròn Gỗ 2 Chỗ
  private fun DrawScope.drawTableRound(center: Offset, scale: Float, isDirty: Boolean) {
    // 2 Chairs (left and right)
    drawCircle(
      color = Color(0xFFA1887F),
      radius = 6f * scale,
      center = Offset(center.x - 16f * scale, center.y - 2f * scale)
    )
    drawCircle(
      color = Color(0xFFA1887F),
      radius = 6f * scale,
      center = Offset(center.x + 16f * scale, center.y + 2f * scale)
    )

    // Table Leg
    drawLine(
      color = Color(0xFF3E2723),
      start = Offset(center.x, center.y + 4f * scale),
      end = Offset(center.x, center.y - 10f * scale),
      strokeWidth = 4f * scale
    )
    // Round Table Top (isometric ellipse)
    drawOval(
      color = Color(0xFFD7CCC8),
      topLeft = Offset(center.x - 14f * scale, center.y - 18f * scale),
      size = Size(28f * scale, 14f * scale)
    )
    drawOval(
      color = Color(0xFF8D6E63),
      topLeft = Offset(center.x - 14f * scale, center.y - 18f * scale),
      size = Size(28f * scale, 14f * scale),
      style = Stroke(width = 2f * scale)
    )

    // If customer is seated or dirty: Coffee Cup on table
    drawCircle(
      color = Color.White,
      radius = 3.5f * scale,
      center = Offset(center.x - 2f * scale, center.y - 12f * scale)
    )
    drawCircle(
      color = Color(0xFF4E342E),
      radius = 2.2f * scale,
      center = Offset(center.x - 2f * scale, center.y - 12f * scale)
    )
  }

  // Bàn Sofa Êm Ái 4 Chỗ
  private fun DrawScope.drawTableSofa(center: Offset, scale: Float, isDirty: Boolean) {
    // Large Sofa back
    drawIsometricBlock(
      center = Offset(center.x - 10f * scale, center.y - 10f * scale),
      width = 24f * scale,
      height = 20f * scale,
      depth = 12f * scale,
      topColor = Color(0xFF81C784),
      leftColor = Color(0xFF388E3C),
      rightColor = Color(0xFF4CAF50)
    )
    // Coffee Table in front
    drawIsometricBlock(
      center = Offset(center.x + 8f * scale, center.y + 4f * scale),
      width = 20f * scale,
      height = 10f * scale,
      depth = 14f * scale,
      topColor = Color(0xFFBCAAA4),
      leftColor = Color(0xFF795548),
      rightColor = Color(0xFF8D6E63)
    )
  }

  // Bàn Cao Quầy Bar
  private fun DrawScope.drawBarStool(center: Offset, scale: Float, isDirty: Boolean) {
    drawIsometricBlock(
      center = center,
      width = 16f * scale,
      height = 24f * scale,
      depth = 16f * scale,
      topColor = Color(0xFF5D4037),
      leftColor = Color(0xFF3E2723),
      rightColor = Color(0xFF4E342E)
    )
  }

  // Cây Monstera Nhiệt Đới
  private fun DrawScope.drawPlantMonstera(center: Offset, scale: Float, timeSeconds: Float) {
    // Terracotta Pot
    drawIsometricBlock(
      center = center,
      width = 14f * scale,
      height = 14f * scale,
      depth = 14f * scale,
      topColor = Color(0xFFD84315),
      leftColor = Color(0xFFBF360C),
      rightColor = Color(0xFFE64A19)
    )
    // Foliage swaying gently
    val sway = sin(timeSeconds * 2f) * 2f * scale
    val plantTop = Offset(center.x + sway, center.y - 20f * scale)
    drawCircle(color = Color(0xFF2E7D32), radius = 12f * scale, center = plantTop)
    drawCircle(color = Color(0xFF43A047), radius = 9f * scale, center = Offset(plantTop.x - 6f * scale, plantTop.y + 3f * scale))
    drawCircle(color = Color(0xFF66BB6A), radius = 8f * scale, center = Offset(plantTop.x + 5f * scale, plantTop.y + 2f * scale))
  }

  // Đèn Cây Vintage
  private fun DrawScope.drawVintageLamp(center: Offset, scale: Float) {
    // Stand
    drawLine(
      color = Color(0xFFBCAAA4),
      start = Offset(center.x, center.y),
      end = Offset(center.x, center.y - 36f * scale),
      strokeWidth = 3f * scale
    )
    // Warm lampshade
    drawIsometricBlock(
      center = Offset(center.x, center.y - 36f * scale),
      width = 16f * scale,
      height = 12f * scale,
      depth = 16f * scale,
      topColor = Color(0xFFFFD54F),
      leftColor = Color(0xFFFFA000),
      rightColor = Color(0xFFFFB300)
    )
    // Golden Radial Glow on floor
    drawCircle(
      color = Color(0x33FFD54F),
      radius = 32f * scale,
      center = center
    )
  }

  // Loa Nhạc Acoustic
  private fun DrawScope.drawAcousticSpeaker(center: Offset, scale: Float, timeSeconds: Float) {
    drawIsometricBlock(
      center = center,
      width = 16f * scale,
      height = 20f * scale,
      depth = 12f * scale,
      topColor = Color(0xFF8D6E63),
      leftColor = Color(0xFF4E342E),
      rightColor = Color(0xFF5D4037)
    )
    // Speaker cone
    drawCircle(
      color = Color(0xFF212121),
      radius = 4f * scale,
      center = Offset(center.x, center.y - 8f * scale)
    )
  }

  // Thảm Bohemian
  private fun DrawScope.drawPersianRug(center: Offset, scale: Float) {
    val path = Path().apply {
      moveTo(center.x, center.y - 12f * scale)
      lineTo(center.x + 28f * scale, center.y + 2f * scale)
      lineTo(center.x, center.y + 16f * scale)
      lineTo(center.x - 28f * scale, center.y + 2f * scale)
      close()
    }
    drawPath(path, Color(0xFFC2185B))
    drawPath(path, Color(0xFFFFD54F), style = Stroke(width = 2f * scale))
  }

  private fun DrawScope.drawDirtyTableIndicator(center: Offset, scale: Float) {
    // Dirty plates & trash bubble
    val bubblePos = Offset(center.x, center.y - 24f * scale)
    drawCircle(color = Color(0xFFE53935), radius = 6f * scale, center = bubblePos)
    drawCircle(color = Color.White, radius = 4f * scale, center = bubblePos)
    drawCircle(color = Color(0xFFE53935), radius = 2f * scale, center = bubblePos)
  }

  // Staff Characters (Barista & Waiter)
  private fun DrawScope.drawStaffCharacter(origin: Offset, scale: Float, staff: StaffMember, timeSeconds: Float) {
    val pos = gridToScreen(staff.posX, staff.posY, origin, scale)
    val isMoving = (staff.posX != staff.targetX || staff.posY != staff.targetY)
    val walkBob = if (isMoving) sin(timeSeconds * 12f) * 3f * scale else 0f

    // Ground Shadow
    drawOval(
      color = Color(0x33000000),
      topLeft = Offset(pos.x - 8f * scale, pos.y - 3f * scale),
      size = Size(16f * scale, 6f * scale)
    )

    // Body
    val bodyTop = Offset(pos.x, pos.y - 16f * scale - walkBob)
    drawRoundRect(
      color = Color(staff.shirtColorHex),
      topLeft = Offset(bodyTop.x - 6f * scale, bodyTop.y),
      size = Size(12f * scale, 14f * scale),
      cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f * scale, 3f * scale)
    )

    // White Apron
    drawRoundRect(
      color = Color(0xFFF5F5F5),
      topLeft = Offset(bodyTop.x - 4f * scale, bodyTop.y + 3f * scale),
      size = Size(8f * scale, 10f * scale),
      cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f * scale, 2f * scale)
    )

    // Head
    val headCenter = Offset(pos.x, pos.y - 22f * scale - walkBob)
    drawCircle(
      color = Color(0xFFFFD180),
      radius = 5.5f * scale,
      center = headCenter
    )

    // Hair / Cap
    drawCircle(
      color = Color(0xFF4E342E),
      radius = 5.8f * scale,
      center = Offset(headCenter.x, headCenter.y - 2f * scale)
    )

    // Action status bubble (e.g. Carrying coffee cup or cleaning broom)
    if (staff.state == StaffState.BREWING) {
      drawActionBadge(headCenter, scale, "☕ Đang pha")
    } else if (staff.state == StaffState.CLEANING_TABLE) {
      drawActionBadge(headCenter, scale, "🧹 Dọn bàn")
    } else if (staff.state == StaffState.CARRYING_ORDER) {
      drawActionBadge(headCenter, scale, "🥤 Bưng món")
    }
  }

  // Customer Characters
  private fun DrawScope.drawCustomerCharacter(origin: Offset, scale: Float, cust: Customer, timeSeconds: Float) {
    val pos = gridToScreen(cust.posX, cust.posY, origin, scale)
    val isMoving = (cust.posX != cust.targetX || cust.posY != cust.targetY)
    val walkBob = if (isMoving) sin(timeSeconds * 10f) * 2.5f * scale else 0f

    // Ground Shadow
    drawOval(
      color = Color(0x33000000),
      topLeft = Offset(pos.x - 7f * scale, pos.y - 3f * scale),
      size = Size(14f * scale, 6f * scale)
    )

    // Body
    val bodyTop = Offset(pos.x, pos.y - 14f * scale - walkBob)
    drawRoundRect(
      color = Color(cust.shirtColorHex),
      topLeft = Offset(bodyTop.x - 5f * scale, bodyTop.y),
      size = Size(10f * scale, 12f * scale),
      cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f * scale, 3f * scale)
    )

    // Head
    val headCenter = Offset(pos.x, pos.y - 19f * scale - walkBob)
    drawCircle(
      color = Color(cust.skinToneHex),
      radius = 5f * scale,
      center = headCenter
    )

    // Umbrella if Rainy and outside
    if (cust.umbrellaColorHex != null && cust.posY >= 7.5f) {
      drawCircle(
        color = Color(cust.umbrellaColorHex),
        radius = 14f * scale,
        center = Offset(headCenter.x, headCenter.y - 8f * scale)
      )
    }

    // Customer Thought / Speech Bubble
    if (!cust.thoughtBubble.isNullOrEmpty()) {
      drawThoughtBubble(headCenter, scale, cust.thoughtBubble!!)
    } else if (cust.state == CustomerState.CONSUMING) {
      // Hearts floating
      val heartRise = (timeSeconds * 15f) % 12f
      drawCircle(
        color = Color(0xFFE91E63),
        radius = 3f * scale,
        center = Offset(headCenter.x + 8f * scale, headCenter.y - 10f * scale - heartRise * scale)
      )
    }
  }

  private fun DrawScope.drawActionBadge(headCenter: Offset, scale: Float, label: String) {
    val bubbleCenter = Offset(headCenter.x, headCenter.y - 14f * scale)
    drawRoundRect(
      color = Color(0xDD3E2723),
      topLeft = Offset(bubbleCenter.x - 22f * scale, bubbleCenter.y - 7f * scale),
      size = Size(44f * scale, 14f * scale),
      cornerRadius = androidx.compose.ui.geometry.CornerRadius(7f * scale, 7f * scale)
    )
  }

  private fun DrawScope.drawThoughtBubble(headCenter: Offset, scale: Float, text: String) {
    val bCenter = Offset(headCenter.x, headCenter.y - 16f * scale)
    drawRoundRect(
      color = Color.White,
      topLeft = Offset(bCenter.x - 28f * scale, bCenter.y - 9f * scale),
      size = Size(56f * scale, 18f * scale),
      cornerRadius = androidx.compose.ui.geometry.CornerRadius(9f * scale, 9f * scale)
    )
    drawRoundRect(
      color = Color(0xFF5D4037),
      topLeft = Offset(bCenter.x - 28f * scale, bCenter.y - 9f * scale),
      size = Size(56f * scale, 18f * scale),
      cornerRadius = androidx.compose.ui.geometry.CornerRadius(9f * scale, 9f * scale),
      style = Stroke(width = 1.5f * scale)
    )
    // Little pointer tail
    val tail = Path().apply {
      moveTo(bCenter.x - 3f * scale, bCenter.y + 9f * scale)
      lineTo(bCenter.x, bCenter.y + 13f * scale)
      lineTo(bCenter.x + 3f * scale, bCenter.y + 9f * scale)
      close()
    }
    drawPath(tail, Color.White)
  }

  // Outdoor Street Lamp
  private fun DrawScope.drawStreetLamp(origin: Offset, scale: Float, gx: Float, gy: Float, weather: WeatherType) {
    val pos = gridToScreen(gx, gy, origin, scale)
    val lampH = 48f * scale

    // Post
    drawLine(
      color = Color(0xFF37474F),
      start = pos,
      end = Offset(pos.x, pos.y - lampH),
      strokeWidth = 3f * scale
    )
    // Lamp Head
    drawCircle(
      color = Color(0xFFFFD54F),
      radius = 5f * scale,
      center = Offset(pos.x, pos.y - lampH)
    )
    // Soft Streetlight Glow cone
    drawCircle(
      color = Color(0x33FFD54F),
      radius = 24f * scale,
      center = pos
    )
  }

  // Outdoor Tree
  private fun DrawScope.drawOutdoorTree(origin: Offset, scale: Float, gx: Float, gy: Float) {
    val pos = gridToScreen(gx, gy, origin, scale)
    // Trunk
    drawLine(
      color = Color(0xFF5D4037),
      start = pos,
      end = Offset(pos.x, pos.y - 32f * scale),
      strokeWidth = 6f * scale
    )
    // Tree Foliage Puff
    val treeTop = Offset(pos.x, pos.y - 36f * scale)
    drawCircle(color = Color(0xFF2E7D32), radius = 18f * scale, center = treeTop)
    drawCircle(color = Color(0xFF43A047), radius = 14f * scale, center = Offset(treeTop.x - 6f * scale, treeTop.y - 4f * scale))
  }

  // Weather Atmosphere Layer (Falling Rain, Heat Flare)
  private fun DrawScope.drawWeatherAtmosphere(size: Size, weather: WeatherType, timeSeconds: Float) {
    when (weather) {
      WeatherType.RAINY -> {
        // Rain Drops
        val rainCount = 45
        for (i in 0 until rainCount) {
          val rx = ((i * 37f + timeSeconds * 120f) % size.width)
          val ry = ((i * 73f + timeSeconds * 400f) % size.height)
          drawLine(
            color = Color(0x8890CAF9),
            start = Offset(rx, ry),
            end = Offset(rx - 4f, ry + 12f),
            strokeWidth = 1.5f
          )
        }
      }
      WeatherType.HEATWAVE -> {
        // Warm orange vignette
        drawRect(
          brush = Brush.radialGradient(
            colors = listOf(Color.Transparent, Color(0x22FF9800)),
            center = Offset(size.width / 2f, size.height / 2f),
            radius = size.width / 1.5f
          )
        )
      }
      WeatherType.SUNNY, WeatherType.COZY_BREEZE -> {
        // Gentle warm tint
      }
    }
  }

  // Helper to draw standard 3D isometric block
  private fun DrawScope.drawIsometricBlock(
    center: Offset,
    width: Float,
    height: Float,
    depth: Float,
    topColor: Color,
    leftColor: Color,
    rightColor: Color
  ) {
    val halfW = width / 2f
    val halfD = depth / 2f
    val topY = center.y - height

    // Top Face (Diamond)
    val topPath = Path().apply {
      moveTo(center.x, topY - halfD)
      lineTo(center.x + halfW, topY)
      lineTo(center.x, topY + halfD)
      lineTo(center.x - halfW, topY)
      close()
    }
    drawPath(topPath, topColor)

    // Left Face
    val leftPath = Path().apply {
      moveTo(center.x - halfW, topY)
      lineTo(center.x, topY + halfD)
      lineTo(center.x, center.y + halfD)
      lineTo(center.x - halfW, center.y)
      close()
    }
    drawPath(leftPath, leftColor)

    // Right Face
    val rightPath = Path().apply {
      moveTo(center.x, topY + halfD)
      lineTo(center.x + halfW, topY)
      lineTo(center.x + halfW, center.y)
      lineTo(center.x, center.y + halfD)
      close()
    }
    drawPath(rightPath, rightColor)
  }
}
