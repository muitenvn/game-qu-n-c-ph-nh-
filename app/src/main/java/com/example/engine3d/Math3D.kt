package com.example.engine3d

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.*

data class Vec3(val x: Float, val y: Float, val z: Float) {
  operator fun plus(other: Vec3) = Vec3(x + other.x, y + other.y, z + other.z)
  operator fun minus(other: Vec3) = Vec3(x - other.x, y - other.y, z - other.z)
  operator fun times(scalar: Float) = Vec3(x * scalar, y * scalar, z * scalar)
  operator fun div(scalar: Float) = Vec3(x / scalar, y / scalar, z / scalar)

  fun dot(other: Vec3): Float = x * other.x + y * other.y + z * other.z

  fun cross(other: Vec3): Vec3 = Vec3(
    y * other.z - z * other.y,
    z * other.x - x * other.z,
    x * other.y - y * other.x
  )

  fun length(): Float = sqrt(x * x + y * y + z * z)

  fun normalized(): Vec3 {
    val len = length()
    return if (len > 0.00001f) this / len else Vec3(0f, 1f, 0f)
  }
}

data class Polygon3D(
  val vertices: List<Vec3>,
  val baseColor: Color,
  val normal: Vec3,
  val isDoubleSided: Boolean = false,
  val emitLight: Boolean = false
) {
  var depth: Float = 0f
  var projectedPoints: List<Offset> = emptyList()
  var shadedColor: Color = baseColor
}

class Camera3D(
  var target: Vec3 = Vec3(4f, 0f, 4.5f), // Center of cafe
  var yawDeg: Float = 45f,               // Fixed Isometric 45 deg
  var pitchDeg: Float = 35.264f,         // Fixed Isometric 35.264 deg
  var zoom: Float = 80f                  // Pixels per world unit (comfortably fills screen)
) {
  fun getForwardVector(): Vec3 {
    val yawRad = Math.toRadians(yawDeg.toDouble()).toFloat()
    val pitchRad = Math.toRadians(pitchDeg.toDouble()).toFloat()
    val cosPitch = cos(pitchRad)
    val sinPitch = sin(pitchRad)
    val cosYaw = cos(yawRad)
    val sinYaw = sin(yawRad)
    return Vec3(-cosPitch * sinYaw, -sinPitch, -cosPitch * cosYaw).normalized()
  }

  fun getRightVector(): Vec3 {
    val forward = getForwardVector()
    val worldUp = Vec3(0f, 1f, 0f)
    return forward.cross(worldUp).normalized()
  }

  fun getUpVector(): Vec3 {
    val forward = getForwardVector()
    val right = getRightVector()
    return right.cross(forward).normalized()
  }

  // Intersects screen tap (sx, sy) with ground plane Y = 0 in orthographic isometric projection
  fun screenToGroundPlane(screenX: Float, screenY: Float, screenCenter: Offset): Pair<Float, Float>? {
    val forward = getForwardVector()
    val right = getRightVector()
    val up = getUpVector()

    val camX = (screenX - screenCenter.x) / zoom
    val camY = -(screenY - screenCenter.y) / zoom

    // Point on camera plane passing through target
    val pOnPlane = target + right * camX + up * camY

    if (abs(forward.y) < 0.0001f) return null
    val t = -pOnPlane.y / forward.y
    val hit = pOnPlane + forward * t
    return Pair(hit.x, hit.z)
  }
}
