package com.example.engine3d

import androidx.compose.ui.graphics.Color
import com.example.data.model.*
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

object ModelFactory3D {

  // ==========================================
  // 1. GORGEOUS STYLIZED CHIBI CHARACTERS
  // ==========================================
  fun createCharacter3D(
    worldX: Float,
    worldZ: Float,
    rotationYDeg: Float,
    isMoving: Boolean,
    walkTime: Float,
    role: StaffRole?,
    shirtColor: Color,
    skinColor: Color,
    hairColor: Color,
    hasApron: Boolean,
    isHoldingDrink: Boolean,
    isCleaning: Boolean,
    hasUmbrella: Boolean,
    umbrellaColor: Color = Color(0xFFE53935)
  ): List<Polygon3D> {
    val polys = mutableListOf<Polygon3D>()
    val base = Vec3(worldX, 0f, worldZ)

    val swingFreq = 10f
    val bobY = if (isMoving) (abs(sin(walkTime * swingFreq)) * 0.06f) else 0f
    val legSwing = if (isMoving) (sin(walkTime * swingFreq) * 0.10f) else 0f

    // 1. Soft Ground Shadow
    polys.addAll(
      Mesh3D.createBox3D(
        center = base + Vec3(0f, 0.005f, 0f),
        size = Vec3(0.52f, 0.01f, 0.52f),
        color = Color(0x30000000)
      )
    )

    // 2. Legs & Shoes (with walking swing)
    val legY = 0.26f + bobY / 2f
    val leftLegPos = base + Vec3(-0.10f, legY, legSwing)
    val rightLegPos = base + Vec3(0.10f, legY, -legSwing)

    // Dark trousers
    polys.addAll(
      Mesh3D.createBox3D(
        center = leftLegPos,
        size = Vec3(0.13f, 0.40f, 0.15f),
        color = Color(0xFF263238),
        rotationYDeg = rotationYDeg
      )
    )
    polys.addAll(
      Mesh3D.createBox3D(
        center = rightLegPos,
        size = Vec3(0.13f, 0.40f, 0.15f),
        color = Color(0xFF263238),
        rotationYDeg = rotationYDeg
      )
    )

    // Cute shoes
    val leftShoePos = leftLegPos + Vec3(0f, -0.19f, 0.02f)
    val rightShoePos = rightLegPos + Vec3(0f, -0.19f, 0.02f)
    polys.addAll(Mesh3D.createBox3D(center = leftShoePos, size = Vec3(0.14f, 0.08f, 0.19f), color = Color(0xFF37474F), rotationYDeg = rotationYDeg))
    polys.addAll(Mesh3D.createBox3D(center = rightShoePos, size = Vec3(0.14f, 0.08f, 0.19f), color = Color(0xFF37474F), rotationYDeg = rotationYDeg))

    // 3. Torso (Styled Clothes)
    val torsoCenter = base + Vec3(0f, 0.70f + bobY, 0f)
    polys.addAll(
      Mesh3D.createBox3D(
        center = torsoCenter,
        size = Vec3(0.38f, 0.44f, 0.26f),
        color = shirtColor,
        topColor = Color(0xFFFAF3EC),
        rotationYDeg = rotationYDeg
      )
    )

    // Barista / Waiter Apron
    if (hasApron) {
      val apronFront = torsoCenter + Mesh3D.rotateY(Vec3(0f, -0.03f, 0.14f), Vec3(0f, 0f, 0f), rotationYDeg)
      // Brown Barista Apron
      polys.addAll(
        Mesh3D.createBox3D(
          center = apronFront,
          size = Vec3(0.33f, 0.38f, 0.03f),
          color = Color(0xFF5D4037),
          topColor = Color(0xFF6D4C41),
          rotationYDeg = rotationYDeg
        )
      )
      // Miniature Golden Nametag
      val nametagPos = apronFront + Mesh3D.rotateY(Vec3(0.09f, 0.10f, 0.02f), Vec3(0f, 0f, 0f), rotationYDeg)
      polys.addAll(
        Mesh3D.createBox3D(
          center = nametagPos,
          size = Vec3(0.08f, 0.04f, 0.02f),
          color = Color(0xFFFFD54F),
          rotationYDeg = rotationYDeg
        )
      )
    }

    // 4. Arms
    val leftArmPos = torsoCenter + Mesh3D.rotateY(Vec3(-0.25f, 0.02f, -legSwing * 0.6f), Vec3(0f, 0f, 0f), rotationYDeg)
    val rightArmPos = torsoCenter + Mesh3D.rotateY(Vec3(0.25f, 0.02f, legSwing * 0.6f), Vec3(0f, 0f, 0f), rotationYDeg)
    val rightArmOffset = if (isHoldingDrink || isCleaning) Vec3(0f, 0.08f, 0.12f) else Vec3(0f, 0f, 0f)

    polys.addAll(Mesh3D.createBox3D(center = leftArmPos, size = Vec3(0.11f, 0.36f, 0.12f), color = shirtColor, rotationYDeg = rotationYDeg))
    polys.addAll(Mesh3D.createBox3D(center = rightArmPos + rightArmOffset, size = Vec3(0.11f, 0.36f, 0.12f), color = shirtColor, rotationYDeg = rotationYDeg))

    // Hands
    polys.addAll(Mesh3D.createBox3D(center = leftArmPos + Vec3(0f, -0.20f, 0f), size = Vec3(0.09f, 0.08f, 0.09f), color = skinColor, rotationYDeg = rotationYDeg))
    polys.addAll(Mesh3D.createBox3D(center = rightArmPos + rightArmOffset + Vec3(0f, -0.20f, 0f), size = Vec3(0.09f, 0.08f, 0.09f), color = skinColor, rotationYDeg = rotationYDeg))

    // Coffee Cup or Drink in Hand
    if (isHoldingDrink) {
      val cupPos = rightArmPos + rightArmOffset + Mesh3D.rotateY(Vec3(0f, -0.06f, 0.14f), Vec3(0f, 0f, 0f), rotationYDeg)
      polys.addAll(Mesh3D.createCylinder3D(center = cupPos, radius = 0.07f, height = 0.13f, color = Color(0xFFFFFFFF), segments = 6))
      polys.addAll(Mesh3D.createCylinder3D(center = cupPos + Vec3(0f, 0.06f, 0f), radius = 0.075f, height = 0.03f, color = Color(0xFF6D4C41), segments = 6))
    }

    // Cleaning Broom
    if (isCleaning) {
      val broomPos = rightArmPos + rightArmOffset + Mesh3D.rotateY(Vec3(0f, -0.15f, 0.15f), Vec3(0f, 0f, 0f), rotationYDeg)
      polys.addAll(Mesh3D.createCylinder3D(center = broomPos + Vec3(0f, 0.18f, 0f), radius = 0.02f, height = 0.50f, color = Color(0xFFBCAAA4), segments = 4))
      polys.addAll(Mesh3D.createBox3D(center = broomPos - Vec3(0f, 0.10f, 0f), size = Vec3(0.18f, 0.08f, 0.10f), color = Color(0xFFFFB300), rotationYDeg = rotationYDeg))
    }

    // 5. Cute Chibi Head with Expressive Face & Hair
    val headCenter = base + Vec3(0f, 1.14f + bobY, 0f)

    // Head base (Skin)
    polys.addAll(
      Mesh3D.createBox3D(
        center = headCenter,
        size = Vec3(0.35f, 0.33f, 0.32f),
        color = skinColor,
        rotationYDeg = rotationYDeg
      )
    )

    // Expressive Face: Cute Eyes and Blushing Cheeks on front face (+Z)
    val faceFront = headCenter + Mesh3D.rotateY(Vec3(0f, 0f, 0.165f), Vec3(0f, 0f, 0f), rotationYDeg)
    // Left eye & Right eye
    val leftEyePos = faceFront + Mesh3D.rotateY(Vec3(-0.08f, 0.02f, 0.005f), Vec3(0f, 0f, 0f), rotationYDeg)
    val rightEyePos = faceFront + Mesh3D.rotateY(Vec3(0.08f, 0.02f, 0.005f), Vec3(0f, 0f, 0f), rotationYDeg)
    polys.addAll(Mesh3D.createBox3D(center = leftEyePos, size = Vec3(0.04f, 0.06f, 0.01f), color = Color(0xFF212121), rotationYDeg = rotationYDeg))
    polys.addAll(Mesh3D.createBox3D(center = rightEyePos, size = Vec3(0.04f, 0.06f, 0.01f), color = Color(0xFF212121), rotationYDeg = rotationYDeg))

    // Blushing cheeks (Cute pink)
    val leftBlushPos = faceFront + Mesh3D.rotateY(Vec3(-0.11f, -0.05f, 0.005f), Vec3(0f, 0f, 0f), rotationYDeg)
    val rightBlushPos = faceFront + Mesh3D.rotateY(Vec3(0.11f, -0.05f, 0.005f), Vec3(0f, 0f, 0f), rotationYDeg)
    polys.addAll(Mesh3D.createBox3D(center = leftBlushPos, size = Vec3(0.05f, 0.03f, 0.01f), color = Color(0xFFFF8A80), rotationYDeg = rotationYDeg))
    polys.addAll(Mesh3D.createBox3D(center = rightBlushPos, size = Vec3(0.05f, 0.03f, 0.01f), color = Color(0xFFFF8A80), rotationYDeg = rotationYDeg))

    // Styled Hair
    val hairCenter = headCenter + Vec3(0f, 0.09f, -0.02f)
    polys.addAll(
      Mesh3D.createBox3D(
        center = hairCenter,
        size = Vec3(0.38f, 0.22f, 0.36f),
        color = hairColor,
        topColor = Color(
          red = (hairColor.red * 1.15f).coerceAtMost(1f),
          green = (hairColor.green * 1.15f).coerceAtMost(1f),
          blue = (hairColor.blue * 1.15f).coerceAtMost(1f),
          alpha = 1f
        ),
        rotationYDeg = rotationYDeg
      )
    )
    // Hair Bangs in front
    val bangsPos = headCenter + Mesh3D.rotateY(Vec3(0f, 0.12f, 0.14f), Vec3(0f, 0f, 0f), rotationYDeg)
    polys.addAll(Mesh3D.createBox3D(center = bangsPos, size = Vec3(0.36f, 0.10f, 0.08f), color = hairColor, rotationYDeg = rotationYDeg))

    // Staff Beret / Cap
    if (role != null) {
      val capCenter = headCenter + Vec3(0f, 0.19f, 0f)
      val capColor = if (role == StaffRole.BARISTA) Color(0xFF4E342E) else Color(0xFF263238)
      polys.addAll(Mesh3D.createCylinder3D(center = capCenter, radius = 0.21f, height = 0.09f, color = capColor, segments = 8))
      polys.addAll(Mesh3D.createCylinder3D(center = capCenter + Vec3(0f, 0.05f, 0f), radius = 0.14f, height = 0.06f, color = Color(0xFFFFD54F), segments = 6))
    }

    // Umbrella for Rainy weather
    if (hasUmbrella) {
      val umbrellaShaft = headCenter + Mesh3D.rotateY(Vec3(0.30f, 0.35f, 0.10f), Vec3(0f, 0f, 0f), rotationYDeg)
      polys.addAll(Mesh3D.createCylinder3D(center = umbrellaShaft, radius = 0.02f, height = 0.70f, color = Color(0xFF37474F), segments = 4))
      val canopyCenter = umbrellaShaft + Vec3(0f, 0.38f, 0f)
      polys.addAll(Mesh3D.createCylinder3D(center = canopyCenter, radius = 0.60f, height = 0.16f, color = umbrellaColor, segments = 8))
      polys.addAll(Mesh3D.createCylinder3D(center = canopyCenter + Vec3(0f, 0.08f, 0f), radius = 0.04f, height = 0.10f, color = Color(0xFF212121), segments = 4))
    }

    return polys
  }

  // ==========================================
  // 2. GORGEOUS COZY CAFE FURNITURE
  // ==========================================
  fun createFurniture3D(item: FurnitureItem, timeSeconds: Float): List<Polygon3D> {
    val polys = mutableListOf<Polygon3D>()
    val cx = item.gridX + 0.5f
    val cz = item.gridY + 0.5f
    val center = Vec3(cx, 0f, cz)
    val rot = item.rotation.toFloat()

    when (item.type) {
      ItemType.ORDER_COUNTER -> {
        // Wooden fluted bar front with Italian White Marble top
        polys.addAll(Mesh3D.createBox3D(center = center + Vec3(0f, 0.42f, 0f), size = Vec3(0.92f, 0.84f, 0.65f), color = Color(0xFF8D6E63), frontColor = Color(0xFF795548), rotationYDeg = rot))
        // Polished White Marble Top (Thick beveled slab)
        polys.addAll(Mesh3D.createBox3D(center = center + Vec3(0f, 0.86f, 0f), size = Vec3(0.98f, 0.08f, 0.72f), color = Color(0xFFECEFF1), topColor = Color(0xFFFAF8F5), rotationYDeg = rot))

        // Modern Touchscreen POS Tablet
        val posBase = center + Mesh3D.rotateY(Vec3(-0.20f, 0.96f, 0f), Vec3(0f, 0f, 0f), rot)
        polys.addAll(Mesh3D.createBox3D(center = posBase, size = Vec3(0.24f, 0.16f, 0.18f), color = Color(0xFF37474F), rotationYDeg = rot))
        // Screen glowing with blue POS menu
        polys.addAll(Mesh3D.createBox3D(center = posBase + Mesh3D.rotateY(Vec3(0f, 0.09f, 0.08f), Vec3(0f, 0f, 0f), rot), size = Vec3(0.22f, 0.14f, 0.03f), color = Color(0xFF4FC3F7), rotationYDeg = rot))

        // Golden Service Bell
        val bellPos = center + Mesh3D.rotateY(Vec3(0.12f, 0.92f, 0.05f), Vec3(0f, 0f, 0f), rot)
        polys.addAll(Mesh3D.createCylinder3D(center = bellPos, radius = 0.06f, height = 0.06f, color = Color(0xFFFFD54F), segments = 6))

        // Stacked Takeaway Paper Cups
        val cupsPos = center + Mesh3D.rotateY(Vec3(0.28f, 0.96f, -0.08f), Vec3(0f, 0f, 0f), rot)
        polys.addAll(Mesh3D.createCylinder3D(center = cupsPos, radius = 0.055f, height = 0.16f, color = Color(0xFFFAF3EC), segments = 6))
      }

      ItemType.ESPRESSO_MACHINE -> {
        // Cabinet Base
        polys.addAll(Mesh3D.createBox3D(center = center + Vec3(0f, 0.40f, 0f), size = Vec3(0.90f, 0.80f, 0.65f), color = Color(0xFF795548), topColor = Color(0xFFFAF8F5), rotationYDeg = rot))

        // Italian Espresso Machine (Mint Green & Chrome)
        val machPos = center + Mesh3D.rotateY(Vec3(0f, 1.06f, 0f), Vec3(0f, 0f, 0f), rot)
        polys.addAll(Mesh3D.createBox3D(center = machPos, size = Vec3(0.72f, 0.50f, 0.46f), color = Color(0xFF80CBC4), topColor = Color(0xFFB2DFDB), sideColor = Color(0xFFCFD8DC), rotationYDeg = rot))

        // Chrome Groupheads and Portafilters
        val leftHead = machPos + Mesh3D.rotateY(Vec3(-0.16f, -0.10f, 0.26f), Vec3(0f, 0f, 0f), rot)
        val rightHead = machPos + Mesh3D.rotateY(Vec3(0.16f, -0.10f, 0.26f), Vec3(0f, 0f, 0f), rot)
        polys.addAll(Mesh3D.createBox3D(center = leftHead, size = Vec3(0.08f, 0.08f, 0.16f), color = Color(0xFF212121), rotationYDeg = rot))
        polys.addAll(Mesh3D.createBox3D(center = rightHead, size = Vec3(0.08f, 0.08f, 0.16f), color = Color(0xFF212121), rotationYDeg = rot))

        // Steamed Milk Pitcher (Stainless Steel)
        polys.addAll(Mesh3D.createCylinder3D(center = machPos + Mesh3D.rotateY(Vec3(0.24f, -0.06f, 0.22f), Vec3(0f, 0f, 0f), rot), radius = 0.05f, height = 0.10f, color = Color(0xFFECEFF1), segments = 6))

        // Coffee Bean Hoppers on top (Dark Roasted Coffee Beans)
        polys.addAll(Mesh3D.createCylinder3D(center = machPos + Vec3(-0.15f, 0.31f, 0f), radius = 0.08f, height = 0.12f, color = Color(0xFF4E342E), segments = 6))
        polys.addAll(Mesh3D.createCylinder3D(center = machPos + Vec3(0.15f, 0.31f, 0f), radius = 0.08f, height = 0.12f, color = Color(0xFF4E342E), segments = 6))

        // Steaming Mug on Drip Tray
        polys.addAll(Mesh3D.createCylinder3D(center = machPos + Mesh3D.rotateY(Vec3(-0.16f, -0.18f, 0.26f), Vec3(0f, 0f, 0f), rot), radius = 0.045f, height = 0.06f, color = Color(0xFFE91E63), segments = 6))
      }

      ItemType.PASTRY_DISPLAY -> {
        // Cabinet base
        polys.addAll(Mesh3D.createBox3D(center = center + Vec3(0f, 0.38f, 0f), size = Vec3(0.92f, 0.76f, 0.65f), color = Color(0xFF795548), rotationYDeg = rot))
        // Curved Glass Case
        val glassPos = center + Vec3(0f, 0.98f, 0f)
        polys.addAll(Mesh3D.createBox3D(center = glassPos, size = Vec3(0.86f, 0.44f, 0.58f), color = Color(0x66B0BEC5), topColor = Color(0x88CFD8DC), rotationYDeg = rot))

        // Golden Butter Croissants 🥐
        polys.addAll(Mesh3D.createBox3D(center = glassPos + Mesh3D.rotateY(Vec3(-0.20f, -0.05f, 0f), Vec3(0f, 0f, 0f), rot), size = Vec3(0.20f, 0.09f, 0.15f), color = Color(0xFFFFB300), rotationYDeg = rot))
        // Strawberry Tart 🍓
        polys.addAll(Mesh3D.createCylinder3D(center = glassPos + Mesh3D.rotateY(Vec3(0.18f, -0.05f, 0.06f), Vec3(0f, 0f, 0f), rot), radius = 0.07f, height = 0.08f, color = Color(0xFFE53935), segments = 6))
        // Chocolate Slice 🍫
        polys.addAll(Mesh3D.createBox3D(center = glassPos + Mesh3D.rotateY(Vec3(0.18f, -0.05f, -0.12f), Vec3(0f, 0f, 0f), rot), size = Vec3(0.12f, 0.08f, 0.12f), color = Color(0xFF4E342E), rotationYDeg = rot))
      }

      ItemType.BEVERAGE_COOLER -> {
        polys.addAll(Mesh3D.createBox3D(center = center + Vec3(0f, 0.85f, 0f), size = Vec3(0.70f, 1.70f, 0.60f), color = Color(0xFF00838F), topColor = Color(0xFF0097A7), frontColor = Color(0x9980DEEA), rotationYDeg = rot))
        polys.addAll(Mesh3D.createCylinder3D(center = center + Vec3(-0.12f, 0.80f, 0f), radius = 0.06f, height = 0.22f, color = Color(0xFF4FC3F7), segments = 6))
        polys.addAll(Mesh3D.createCylinder3D(center = center + Vec3(0.12f, 0.80f, 0f), radius = 0.06f, height = 0.22f, color = Color(0xFF66BB6A), segments = 6))
      }

      ItemType.FRIDGE -> {
        polys.addAll(Mesh3D.createBox3D(center = center + Vec3(0f, 0.85f, 0f), size = Vec3(0.72f, 1.70f, 0.65f), color = Color(0xFFCFD8DC), topColor = Color(0xFFECEFF1), frontColor = Color(0xFFB0BEC5), rotationYDeg = rot))
        polys.addAll(Mesh3D.createBox3D(center = center + Vec3(0.24f, 0.95f, 0.35f), size = Vec3(0.04f, 0.32f, 0.06f), color = Color(0xFF455A64), rotationYDeg = rot))
      }

      ItemType.TABLE_ROUND_2P -> {
        // Cast-iron central pedestal with decorative base
        polys.addAll(Mesh3D.createCylinder3D(center = center + Vec3(0f, 0.04f, 0f), radius = 0.22f, height = 0.05f, color = Color(0xFF263238), segments = 8))
        polys.addAll(Mesh3D.createCylinder3D(center = center + Vec3(0f, 0.35f, 0f), radius = 0.05f, height = 0.62f, color = Color(0xFF37474F), segments = 6))

        // Beveled Round Wooden Tabletop (Warm Honey Oak)
        val tableTopY = 0.68f
        polys.addAll(Mesh3D.createCylinder3D(center = center + Vec3(0f, tableTopY, 0f), radius = 0.46f, height = 0.06f, color = Color(0xFFE0C49F), segments = 10))

        // 2 Cozy Wooden Bistro Chairs with Cushions
        val chair1Pos = center + Vec3(-0.48f, 0f, 0f)
        val chair2Pos = center + Vec3(0.48f, 0f, 0f)

        // Chair 1
        polys.addAll(Mesh3D.createBox3D(center = chair1Pos + Vec3(0f, 0.22f, 0f), size = Vec3(0.30f, 0.44f, 0.30f), color = Color(0xFF8D6E63), topColor = Color(0xFFFFD54F), rotationYDeg = rot))
        polys.addAll(Mesh3D.createBox3D(center = chair1Pos + Vec3(-0.12f, 0.50f, 0f), size = Vec3(0.06f, 0.26f, 0.28f), color = Color(0xFF6D4C41), rotationYDeg = rot))

        // Chair 2
        polys.addAll(Mesh3D.createBox3D(center = chair2Pos + Vec3(0f, 0.22f, 0f), size = Vec3(0.30f, 0.44f, 0.30f), color = Color(0xFF8D6E63), topColor = Color(0xFFFFD54F), rotationYDeg = rot))
        polys.addAll(Mesh3D.createBox3D(center = chair2Pos + Vec3(0.12f, 0.50f, 0f), size = Vec3(0.06f, 0.26f, 0.28f), color = Color(0xFF6D4C41), rotationYDeg = rot))

        // Table Decor / Drinks
        if (item.isDirty) {
          // Dirty dishes: stacked cups and napkin
          polys.addAll(Mesh3D.createCylinder3D(center = center + Vec3(-0.06f, tableTopY + 0.05f, 0f), radius = 0.07f, height = 0.08f, color = Color(0xFF5D4037), segments = 6))
          polys.addAll(Mesh3D.createBox3D(center = center + Vec3(0.08f, tableTopY + 0.03f, 0.04f), size = Vec3(0.08f, 0.04f, 0.08f), color = Color(0xFFBCAAA4)))
        } else {
          // Clean table: Ceramic mug with heart latte art + cute mini flower vase
          polys.addAll(Mesh3D.createCylinder3D(center = center + Vec3(-0.08f, tableTopY + 0.06f, 0.04f), radius = 0.055f, height = 0.09f, color = Color(0xFFFAF3EC), segments = 6))
          // Latte foam
          polys.addAll(Mesh3D.createCylinder3D(center = center + Vec3(-0.08f, tableTopY + 0.11f, 0.04f), radius = 0.050f, height = 0.02f, color = Color(0xFF6D4C41), segments = 6))
          // Mini Flower Vase with blooming Daisy
          polys.addAll(Mesh3D.createCylinder3D(center = center + Vec3(0.10f, tableTopY + 0.06f, -0.06f), radius = 0.035f, height = 0.09f, color = Color(0xFF80DEEA), segments = 6))
          polys.addAll(Mesh3D.createBox3D(center = center + Vec3(0.10f, tableTopY + 0.13f, -0.06f), size = Vec3(0.06f, 0.05f, 0.06f), color = Color(0xFFFFEB3B)))
        }
      }

      ItemType.TABLE_SOFA_4P -> {
        // Luxurious Emerald Green Velvet Sofa with tufted cushions
        val sofaCenter = center + Vec3(-0.25f, 0.40f, 0f)
        polys.addAll(Mesh3D.createBox3D(center = sofaCenter, size = Vec3(0.50f, 0.38f, 0.92f), color = Color(0xFF2E7D32), topColor = Color(0xFF4CAF50), rotationYDeg = rot))
        // Backrest with deep tufting
        polys.addAll(Mesh3D.createBox3D(center = sofaCenter + Vec3(-0.21f, 0.30f, 0f), size = Vec3(0.15f, 0.48f, 0.92f), color = Color(0xFF1B5E20), topColor = Color(0xFF2E7D32), rotationYDeg = rot))

        // Cute Decorative Throw Pillows (Mustard Yellow and Cream)
        polys.addAll(Mesh3D.createBox3D(center = sofaCenter + Vec3(-0.10f, 0.24f, -0.32f), size = Vec3(0.12f, 0.18f, 0.18f), color = Color(0xFFFFD54F), rotationYDeg = rot + 15f))
        polys.addAll(Mesh3D.createBox3D(center = sofaCenter + Vec3(-0.10f, 0.24f, 0.32f), size = Vec3(0.12f, 0.18f, 0.18f), color = Color(0xFFFAF3EC), rotationYDeg = rot - 15f))

        // Low Rustic Coffee Table in front
        val coffeeTable = center + Vec3(0.24f, 0.24f, 0f)
        polys.addAll(Mesh3D.createBox3D(center = coffeeTable, size = Vec3(0.40f, 0.44f, 0.74f), color = Color(0xFF8D6E63), topColor = Color(0xFFE0C49F), rotationYDeg = rot))
        // Coffee book and mug
        polys.addAll(Mesh3D.createBox3D(center = coffeeTable + Vec3(0f, 0.24f, 0.12f), size = Vec3(0.16f, 0.03f, 0.20f), color = Color(0xFF0097A7), rotationYDeg = rot))
        polys.addAll(Mesh3D.createCylinder3D(center = coffeeTable + Vec3(0.04f, 0.27f, -0.14f), radius = 0.05f, height = 0.08f, color = Color(0xFFFAF3EC), segments = 6))
      }

      ItemType.BAR_STOOL_TABLE -> {
        // High Polished Bar Table
        polys.addAll(Mesh3D.createBox3D(center = center + Vec3(0f, 0.54f, 0f), size = Vec3(0.46f, 1.08f, 0.46f), color = Color(0xFF5D4037), topColor = Color(0xFFE0C49F), rotationYDeg = rot))
        // 2 High Bar Stools with Leather Seats
        val stool1 = center + Vec3(-0.35f, 0.40f, 0f)
        val stool2 = center + Vec3(0.35f, 0.40f, 0f)
        polys.addAll(Mesh3D.createCylinder3D(center = stool1, radius = 0.12f, height = 0.76f, color = Color(0xFF4E342E), segments = 6))
        polys.addAll(Mesh3D.createCylinder3D(center = stool1 + Vec3(0f, 0.38f, 0f), radius = 0.15f, height = 0.06f, color = Color(0xFFD84315), segments = 8))

        polys.addAll(Mesh3D.createCylinder3D(center = stool2, radius = 0.12f, height = 0.76f, color = Color(0xFF4E342E), segments = 6))
        polys.addAll(Mesh3D.createCylinder3D(center = stool2 + Vec3(0f, 0.38f, 0f), radius = 0.15f, height = 0.06f, color = Color(0xFFD84315), segments = 8))
      }

      ItemType.PLANT_MONSTERA -> {
        // Terracotta Pot with rim
        val potPos = center + Vec3(0f, 0.18f, 0f)
        polys.addAll(Mesh3D.createCylinder3D(center = potPos, radius = 0.22f, height = 0.36f, color = Color(0xFFD84315), segments = 8))
        polys.addAll(Mesh3D.createCylinder3D(center = potPos + Vec3(0f, 0.18f, 0f), radius = 0.24f, height = 0.06f, color = Color(0xFFBF360C), segments = 8))
        // Dark rich soil
        polys.addAll(Mesh3D.createCylinder3D(center = potPos + Vec3(0f, 0.17f, 0f), radius = 0.20f, height = 0.02f, color = Color(0xFF3E2723), segments = 8))

        // Multi-tiered lush broad Monstera leaves
        val leafAngles = listOf(0f, 72f, 144f, 216f, 288f)
        leafAngles.forEachIndexed { i, deg ->
          val rad = Math.toRadians(deg.toDouble()).toFloat()
          val lx = cos(rad) * 0.18f
          val lz = sin(rad) * 0.18f
          val leafCol = if (i % 2 == 0) Color(0xFF2E7D32) else Color(0xFF43A047)
          polys.addAll(Mesh3D.createBox3D(center = center + Vec3(lx, 0.48f + i * 0.06f, lz), size = Vec3(0.26f, 0.03f, 0.26f), color = leafCol, rotationYDeg = deg))
        }
      }

      ItemType.VINTAGE_LAMP -> {
        // Brass base and pole
        polys.addAll(Mesh3D.createCylinder3D(center = center + Vec3(0f, 0.04f, 0f), radius = 0.20f, height = 0.04f, color = Color(0xFFFFB300), segments = 8))
        polys.addAll(Mesh3D.createCylinder3D(center = center + Vec3(0f, 0.85f, 0f), radius = 0.04f, height = 1.64f, color = Color(0xFFFFB300), segments = 6))
        // Pleated Amber Lampshade
        polys.addAll(Mesh3D.createCylinder3D(center = center + Vec3(0f, 1.58f, 0f), radius = 0.28f, height = 0.32f, color = Color(0xFFFFD54F), segments = 8))
      }

      ItemType.ACOUSTIC_SPEAKER -> {
        // Retro Wood Speaker with Tweed Fabric Front
        polys.addAll(Mesh3D.createBox3D(center = center + Vec3(0f, 0.38f, 0f), size = Vec3(0.42f, 0.76f, 0.34f), color = Color(0xFF5D4037), frontColor = Color(0xFFD7CCC8), rotationYDeg = rot))
        // Speaker cones
        polys.addAll(Mesh3D.createCylinder3D(center = center + Mesh3D.rotateY(Vec3(0f, 0.44f, 0.18f), Vec3(0f, 0f, 0f), rot), radius = 0.10f, height = 0.02f, color = Color(0xFF212121), segments = 6))
        polys.addAll(Mesh3D.createCylinder3D(center = center + Mesh3D.rotateY(Vec3(0f, 0.22f, 0.18f), Vec3(0f, 0f, 0f), rot), radius = 0.06f, height = 0.02f, color = Color(0xFF424242), segments = 6))
      }

      ItemType.PERSIAN_RUG -> {
        polys.addAll(Mesh3D.createBox3D(center = center + Vec3(0f, 0.01f, 0f), size = Vec3(0.94f, 0.02f, 0.94f), color = Color(0xFFC2185B), topColor = Color(0xFFAD1457), rotationYDeg = rot))
      }
    }

    return polys
  }

  // ==========================================
  // 3. GORGEOUS CAFE ENVIRONMENT & STOREFRONT
  // ==========================================
  fun createEnvironment3D(weather: WeatherType): List<Polygon3D> {
    val polys = mutableListOf<Polygon3D>()
    val normalUp = Vec3(0f, 1f, 0f)

    // 1. Warm Parquet Hardwood Cafe Interior Floor (8x8 Grid)
    // Rich herringbone/parquet wood tone palette
    val parquetColors = listOf(
      Color(0xFFE2C9A7),
      Color(0xFFD6B994),
      Color(0xFFC8A77E),
      Color(0xFFDFC4A0)
    )

    for (gx in 0 until 8) {
      for (gz in 0 until 8) {
        val colorIdx = (gx * 3 + gz * 7) % parquetColors.size
        val woodTone = parquetColors[colorIdx]
        val x0 = gx.toFloat()
        val x1 = (gx + 1).toFloat()
        val z0 = gz.toFloat()
        val z1 = (gz + 1).toFloat()

        polys.add(
          Polygon3D(
            vertices = listOf(Vec3(x0, 0f, z0), Vec3(x1, 0f, z0), Vec3(x1, 0f, z1), Vec3(x0, 0f, z1)),
            baseColor = woodTone,
            normal = normalUp
          )
        )
      }
    }

    // Elegant Dark Mahogany Border Trim around cafe floor perimeter
    polys.addAll(Mesh3D.createBox3D(center = Vec3(4f, 0.005f, 0.04f), size = Vec3(8.0f, 0.01f, 0.08f), color = Color(0xFF5D4037)))
    polys.addAll(Mesh3D.createBox3D(center = Vec3(0.04f, 0.005f, 4f), size = Vec3(0.08f, 0.01f, 8.0f), color = Color(0xFF5D4037)))

    // Coir "Welcome" Mat at entrance
    polys.add(
      Polygon3D(
        vertices = listOf(Vec3(3.1f, 0.01f, 7.1f), Vec3(4.9f, 0.01f, 7.1f), Vec3(4.9f, 0.01f, 7.85f), Vec3(3.1f, 0.01f, 7.85f)),
        baseColor = Color(0xFF8D6E63),
        normal = normalUp
      )
    )

    // 2. Charming Walls: Warm Brick Top + Wood Wainscoting Bottom
    val wallHeight = 2.45f
    val wallThickness = 0.22f

    // Back Wall at Z = 0
    polys.addAll(
      Mesh3D.createBox3D(
        center = Vec3(4f, wallHeight / 2f, -wallThickness / 2f),
        size = Vec3(8.4f, wallHeight, wallThickness),
        color = Color(0xFFCE7E63),     // Warm terracotta brick
        frontColor = Color(0xFFDE8C68)
      )
    )
    // Wood wainscoting paneling (lower 0.9m of back wall)
    polys.addAll(
      Mesh3D.createBox3D(
        center = Vec3(4f, 0.45f, 0.02f),
        size = Vec3(8.4f, 0.90f, 0.04f),
        color = Color(0xFF6D4C41),
        frontColor = Color(0xFF795548)
      )
    )

    // Left Wall at X = 0
    polys.addAll(
      Mesh3D.createBox3D(
        center = Vec3(-wallThickness / 2f, wallHeight / 2f, 4f),
        size = Vec3(wallThickness, wallHeight, 8.4f),
        color = Color(0xFFCE7E63),
        sideColor = Color(0xFFDE8C68)
      )
    )
    // Wood wainscoting paneling (lower 0.9m of left wall)
    polys.addAll(
      Mesh3D.createBox3D(
        center = Vec3(0.02f, 0.45f, 4f),
        size = Vec3(0.04f, 0.90f, 8.4f),
        color = Color(0xFF6D4C41),
        sideColor = Color(0xFF795548)
      )
    )

    // Large Arched French Windows with White Frames on Back Wall
    polys.addAll(
      Mesh3D.createBox3D(
        center = Vec3(4f, 1.55f, 0.04f),
        size = Vec3(2.6f, 1.10f, 0.06f),
        color = Color(0x77B0BEC5),
        frontColor = Color(0xFFFAF8F5)
      )
    )
    // Window mullions (cross-bars)
    polys.addAll(Mesh3D.createBox3D(center = Vec3(4f, 1.55f, 0.06f), size = Vec3(0.05f, 1.10f, 0.02f), color = Color(0xFFFAF8F5)))
    polys.addAll(Mesh3D.createBox3D(center = Vec3(4f, 1.55f, 0.06f), size = Vec3(2.6f, 0.05f, 0.02f), color = Color(0xFFFAF8F5)))

    // Hanging Chalkboard Menu on Wall (Left of Window)
    polys.addAll(
      Mesh3D.createBox3D(
        center = Vec3(1.6f, 1.60f, 0.03f),
        size = Vec3(1.20f, 0.85f, 0.04f),
        color = Color(0xFF6D4C41),      // Wooden frame
        frontColor = Color(0xFF263238)  // Chalkboard
      )
    )

    // Framed Coffee Art Poster (Right of Window)
    polys.addAll(
      Mesh3D.createBox3D(
        center = Vec3(6.4f, 1.60f, 0.03f),
        size = Vec3(0.95f, 0.75f, 0.04f),
        color = Color(0xFFFFD54F),      // Gold frame
        frontColor = Color(0xFFFAF3EC)
      )
    )

    // Floating Wooden Shelf with Jars & Trailing Vines
    val shelfPos = Vec3(6.4f, 1.10f, 0.12f)
    polys.addAll(Mesh3D.createBox3D(center = shelfPos, size = Vec3(1.4f, 0.06f, 0.22f), color = Color(0xFF8D6E63)))
    // Glass Jars of Coffee Beans
    polys.addAll(Mesh3D.createCylinder3D(center = shelfPos + Vec3(-0.35f, 0.10f, 0f), radius = 0.06f, height = 0.14f, color = Color(0xFF4E342E), segments = 6))
    polys.addAll(Mesh3D.createCylinder3D(center = shelfPos + Vec3(0f, 0.10f, 0f), radius = 0.06f, height = 0.14f, color = Color(0xFF3E2723), segments = 6))
    // Trailing Ivy Vines (Green)
    polys.addAll(Mesh3D.createBox3D(center = shelfPos + Vec3(0.38f, -0.06f, 0.08f), size = Vec3(0.18f, 0.20f, 0.06f), color = Color(0xFF43A047)))

    // 3. European / Saigon Striped Cafe Awning over Entrance (Z = 7.3 to 8.4)
    // Red and Cream striped canvas awning sloping down
    val awningZ = 7.75f
    val awningWidth = 3.6f
    val stripeCount = 8
    val stripeWidth = awningWidth / stripeCount
    for (i in 0 until stripeCount) {
      val stripeX = 2.2f + (i + 0.5f) * stripeWidth
      val stripeColor = if (i % 2 == 0) Color(0xFFD32F2F) else Color(0xFFFFF8E7)
      polys.addAll(
        Mesh3D.createBox3D(
          center = Vec3(stripeX, 2.20f, awningZ),
          size = Vec3(stripeWidth - 0.02f, 0.08f, 0.95f),
          color = stripeColor,
          topColor = stripeColor
        )
      )
    }
    // Awning Support Brackets
    polys.addAll(Mesh3D.createCylinder3D(center = Vec3(2.2f, 1.80f, 7.7f), radius = 0.03f, height = 0.80f, color = Color(0xFF37474F), segments = 4))
    polys.addAll(Mesh3D.createCylinder3D(center = Vec3(5.8f, 1.80f, 7.7f), radius = 0.03f, height = 0.80f, color = Color(0xFF37474F), segments = 4))

    // 4. Parisian / Saigon Cobblestone Sidewalk (Z = 8..10.5)
    val sidewalkColors = listOf(Color(0xFFE8E0D8), Color(0xFFDFD7CF), Color(0xFFD5CDC7), Color(0xFFEAE2DA))
    for (gx in -2..10) {
      for (gz in 8..10) {
        val colIdx = ((gx + 3) * 5 + gz * 11) % sidewalkColors.size
        val slabCol = sidewalkColors[colIdx]
        val x0 = gx.toFloat()
        val x1 = (gx + 1).toFloat()
        val z0 = gz.toFloat()
        val z1 = (gz + 1).toFloat()

        polys.add(
          Polygon3D(
            vertices = listOf(Vec3(x0, -0.02f, z0), Vec3(x1, -0.02f, z0), Vec3(x1, -0.02f, z1), Vec3(x0, -0.02f, z1)),
            baseColor = slabCol,
            normal = normalUp
          )
        )
      }
    }

    // Sidewalk Curb Rim with Cobblestone detail (Z = 10.5)
    for (gx in -2..10) {
      polys.addAll(Mesh3D.createBox3D(center = Vec3(gx + 0.5f, -0.08f, 10.55f), size = Vec3(1.0f, 0.16f, 0.16f), color = Color(0xFFBDBDBD)))
    }

    // Asphalt Street (Z = 10.6..12.5)
    for (gx in -2..10) {
      val x0 = gx.toFloat()
      val x1 = (gx + 1).toFloat()
      polys.add(
        Polygon3D(
          vertices = listOf(Vec3(x0, -0.16f, 10.65f), Vec3(x1, -0.16f, 10.65f), Vec3(x1, -0.16f, 12.8f), Vec3(x0, -0.16f, 12.8f)),
          baseColor = Color(0xFF546E7A),
          normal = normalUp
        )
      )
    }

    // Outdoor Sidewalk A-Frame Chalkboard Easel ("Cozy Cafe")
    val easelPos = Vec3(1.4f, 0.45f, 8.4f)
    polys.addAll(Mesh3D.createBox3D(center = easelPos, size = Vec3(0.48f, 0.65f, 0.08f), color = Color(0xFF795548), frontColor = Color(0xFF263238), rotationYDeg = 15f))
    polys.addAll(Mesh3D.createBox3D(center = easelPos + Vec3(0f, 0.28f, 0f), size = Vec3(0.44f, 0.08f, 0.04f), color = Color(0xFFFFD54F), rotationYDeg = 15f))

    // Window Flower Planter Boxes
    polys.addAll(createFlowerPlanterBox3D(3.0f, 8.1f))
    polys.addAll(createFlowerPlanterBox3D(5.0f, 8.1f))

    // Lush Outdoor Street Trees
    polys.addAll(createStreetTree3D(-1.2f, 8.6f))
    polys.addAll(createStreetTree3D(9.2f, 8.6f))

    // Classic Wrought-Iron Street Lamps with Amber Glow
    polys.addAll(createStreetLamp3D(0.2f, 8.3f))
    polys.addAll(createStreetLamp3D(7.8f, 8.3f))

    // Vintage Bicycle parked outside
    polys.addAll(createVintageBicycle3D(6.8f, 8.3f))

    return polys
  }

  private fun createFlowerPlanterBox3D(x: Float, z: Float): List<Polygon3D> {
    val polys = mutableListOf<Polygon3D>()
    // Wooden planter trough
    polys.addAll(Mesh3D.createBox3D(center = Vec3(x, 0.18f, z), size = Vec3(0.85f, 0.32f, 0.30f), color = Color(0xFF8D6E63), topColor = Color(0xFF3E2723)))
    // Colorful Blooming Flowers (Red, Pink, Yellow, Green)
    polys.addAll(Mesh3D.createBox3D(center = Vec3(x - 0.25f, 0.38f, z), size = Vec3(0.22f, 0.16f, 0.22f), color = Color(0xFFE91E63)))
    polys.addAll(Mesh3D.createBox3D(center = Vec3(x, 0.40f, z), size = Vec3(0.24f, 0.18f, 0.24f), color = Color(0xFFFFEB3B)))
    polys.addAll(Mesh3D.createBox3D(center = Vec3(x + 0.25f, 0.38f, z), size = Vec3(0.22f, 0.16f, 0.22f), color = Color(0xFF4CAF50)))
    return polys
  }

  private fun createStreetTree3D(x: Float, z: Float): List<Polygon3D> {
    val polys = mutableListOf<Polygon3D>()
    // Wooden tree surround
    polys.addAll(Mesh3D.createBox3D(center = Vec3(x, 0.04f, z), size = Vec3(0.80f, 0.08f, 0.80f), color = Color(0xFF795548), topColor = Color(0xFF3E2723)))
    // Tree trunk
    polys.addAll(Mesh3D.createCylinder3D(center = Vec3(x, 0.70f, z), radius = 0.12f, height = 1.40f, color = Color(0xFF5D4037), segments = 8))
    // Layered lush green canopy foliage clusters
    polys.addAll(Mesh3D.createBox3D(center = Vec3(x, 1.60f, z), size = Vec3(1.10f, 0.75f, 1.10f), color = Color(0xFF2E7D32), topColor = Color(0xFF388E3C)))
    polys.addAll(Mesh3D.createBox3D(center = Vec3(x, 2.15f, z), size = Vec3(0.85f, 0.60f, 0.85f), color = Color(0xFF388E3C), topColor = Color(0xFF4CAF50)))
    polys.addAll(Mesh3D.createBox3D(center = Vec3(x, 2.60f, z), size = Vec3(0.55f, 0.45f, 0.55f), color = Color(0xFF4CAF50), topColor = Color(0xFF81C784)))
    return polys
  }

  private fun createStreetLamp3D(x: Float, z: Float): List<Polygon3D> {
    val polys = mutableListOf<Polygon3D>()
    // Black cast-iron base
    polys.addAll(Mesh3D.createCylinder3D(center = Vec3(x, 0.10f, z), radius = 0.16f, height = 0.20f, color = Color(0xFF263238), segments = 8))
    // Pole
    polys.addAll(Mesh3D.createCylinder3D(center = Vec3(x, 1.10f, z), radius = 0.05f, height = 2.0f, color = Color(0xFF37474F), segments = 6))
    // Lantern head with glowing amber glass
    polys.addAll(Mesh3D.createBox3D(center = Vec3(x, 2.15f, z), size = Vec3(0.30f, 0.35f, 0.30f), color = Color(0xFF263238), frontColor = Color(0xFFFFD54F), sideColor = Color(0xFFFFD54F)))
    // Lantern cap
    polys.addAll(Mesh3D.createCylinder3D(center = Vec3(x, 2.36f, z), radius = 0.20f, height = 0.08f, color = Color(0xFF263238), segments = 6))
    return polys
  }

  private fun createVintageBicycle3D(x: Float, z: Float): List<Polygon3D> {
    val polys = mutableListOf<Polygon3D>()
    // Teal bicycle frame
    val bikeCol = Color(0xFF00897B)
    // Wheels (Front & Rear)
    polys.addAll(Mesh3D.createCylinder3D(center = Vec3(x - 0.25f, 0.22f, z), radius = 0.20f, height = 0.04f, color = Color(0xFF37474F), segments = 8))
    polys.addAll(Mesh3D.createCylinder3D(center = Vec3(x + 0.25f, 0.22f, z), radius = 0.20f, height = 0.04f, color = Color(0xFF37474F), segments = 8))
    // Frame tube
    polys.addAll(Mesh3D.createBox3D(center = Vec3(x, 0.35f, z), size = Vec3(0.50f, 0.05f, 0.05f), color = bikeCol))
    // Front wicker flower basket
    polys.addAll(Mesh3D.createBox3D(center = Vec3(x - 0.28f, 0.45f, z), size = Vec3(0.18f, 0.14f, 0.16f), color = Color(0xFFD7CCC8), topColor = Color(0xFFE91E63)))
    return polys
  }
}
