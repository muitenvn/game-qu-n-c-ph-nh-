package com.example.engine3d

import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin

object Mesh3D {

  fun rotateY(point: Vec3, center: Vec3, angleDeg: Float): Vec3 {
    if (angleDeg == 0f) return point
    val rad = Math.toRadians(angleDeg.toDouble()).toFloat()
    val cosA = cos(rad)
    val sinA = sin(rad)
    val dx = point.x - center.x
    val dz = point.z - center.z
    val nx = dx * cosA - dz * sinA
    val nz = dx * sinA + dz * cosA
    return Vec3(center.x + nx, point.y, center.z + nz)
  }

  // Generates 6-sided 3D low-poly box (quads with outward normal vectors)
  fun createBox3D(
    center: Vec3,
    size: Vec3,
    color: Color,
    rotationYDeg: Float = 0f,
    topColor: Color? = null,
    frontColor: Color? = null,
    sideColor: Color? = null
  ): List<Polygon3D> {
    val hx = size.x / 2f
    val hy = size.y / 2f
    val hz = size.z / 2f

    // 8 local vertices relative to center
    val v000 = Vec3(center.x - hx, center.y - hy, center.z - hz)
    val v100 = Vec3(center.x + hx, center.y - hy, center.z - hz)
    val v110 = Vec3(center.x + hx, center.y + hy, center.z - hz)
    val v010 = Vec3(center.x - hx, center.y + hy, center.z - hz)

    val v001 = Vec3(center.x - hx, center.y - hy, center.z + hz)
    val v101 = Vec3(center.x + hx, center.y - hy, center.z + hz)
    val v111 = Vec3(center.x + hx, center.y + hy, center.z + hz)
    val v011 = Vec3(center.x - hx, center.y + hy, center.z + hz)

    // Rotate around center if needed
    val r000 = rotateY(v000, center, rotationYDeg)
    val r100 = rotateY(v100, center, rotationYDeg)
    val r110 = rotateY(v110, center, rotationYDeg)
    val r010 = rotateY(v010, center, rotationYDeg)

    val r001 = rotateY(v001, center, rotationYDeg)
    val r101 = rotateY(v101, center, rotationYDeg)
    val r111 = rotateY(v111, center, rotationYDeg)
    val r011 = rotateY(v011, center, rotationYDeg)

    val tCol = topColor ?: color
    val fCol = frontColor ?: color
    val sCol = sideColor ?: color

    return listOf(
      // Top face (+Y)
      Polygon3D(
        vertices = listOf(r010, r110, r111, r011),
        baseColor = tCol,
        normal = rotateY(Vec3(0f, 1f, 0f), Vec3(0f, 0f, 0f), rotationYDeg)
      ),
      // Front face (+Z)
      Polygon3D(
        vertices = listOf(r001, r101, r111, r011),
        baseColor = fCol,
        normal = rotateY(Vec3(0f, 0f, 1f), Vec3(0f, 0f, 0f), rotationYDeg)
      ),
      // Back face (-Z)
      Polygon3D(
        vertices = listOf(r100, r000, r010, r110),
        baseColor = sCol,
        normal = rotateY(Vec3(0f, 0f, -1f), Vec3(0f, 0f, 0f), rotationYDeg)
      ),
      // Left face (-X)
      Polygon3D(
        vertices = listOf(r000, r001, r011, r010),
        baseColor = sCol,
        normal = rotateY(Vec3(-1f, 0f, 0f), Vec3(0f, 0f, 0f), rotationYDeg)
      ),
      // Right face (+X)
      Polygon3D(
        vertices = listOf(r101, r100, r110, r111),
        baseColor = sCol,
        normal = rotateY(Vec3(1f, 0f, 0f), Vec3(0f, 0f, 0f), rotationYDeg)
      ),
      // Bottom face (-Y)
      Polygon3D(
        vertices = listOf(r000, r100, r101, r001),
        baseColor = sCol,
        normal = rotateY(Vec3(0f, -1f, 0f), Vec3(0f, 0f, 0f), rotationYDeg)
      )
    )
  }

  // Generates 8-sided faceted low-poly cylinder (for mugs, cups, plants, pots)
  fun createCylinder3D(
    center: Vec3,
    radius: Float,
    height: Float,
    color: Color,
    segments: Int = 8
  ): List<Polygon3D> {
    val polys = mutableListOf<Polygon3D>()
    val halfH = height / 2f
    val topY = center.y + halfH
    val botY = center.y - halfH

    val topVerts = mutableListOf<Vec3>()
    val botVerts = mutableListOf<Vec3>()

    for (i in 0 until segments) {
      val angle = (i * 2.0 * Math.PI / segments).toFloat()
      val vx = center.x + radius * cos(angle)
      val vz = center.z + radius * sin(angle)
      topVerts.add(Vec3(vx, topY, vz))
      botVerts.add(Vec3(vx, botY, vz))
    }

    // Top cap
    polys.add(Polygon3D(vertices = topVerts, baseColor = color, normal = Vec3(0f, 1f, 0f)))

    // Side quads
    for (i in 0 until segments) {
      val next = (i + 1) % segments
      val normal = Vec3(
        (topVerts[i].x + topVerts[next].x) / 2f - center.x,
        0f,
        (topVerts[i].z + topVerts[next].z) / 2f - center.z
      ).normalized()

      polys.add(
        Polygon3D(
          vertices = listOf(botVerts[i], botVerts[next], topVerts[next], topVerts[i]),
          baseColor = color,
          normal = normal
        )
      )
    }

    return polys
  }
}
