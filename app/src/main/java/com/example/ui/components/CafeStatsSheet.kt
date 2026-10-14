package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.formatVnd
import com.example.ui.theme.CoffeeBrownDark
import com.example.ui.theme.CoffeeCream
import com.example.ui.theme.GoldRating
import com.example.viewmodel.CafeUiState

@Composable
fun CafeStatsSheet(
  uiState: CafeUiState,
  onDismiss: () -> Unit
) {
  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth(0.95f)
        .testTag("cafe_stats_sheet"),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = CoffeeCream)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "📊 Thống Kê & Thu Hút Khách",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = CoffeeBrownDark
          )
          IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_stats_sheet")) {
            Icon(Icons.Default.Close, contentDescription = "Đóng", tint = CoffeeBrownDark)
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Big Highlight Card: Sidewalk Attraction Rate
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
              text = "TỈ LỆ KHÁCH VỈA HÈ RẼ VÀO QUÁN",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
              Text(
                text = "${uiState.attractionRatePercent}%",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF2E7D32)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "(Mỗi người qua đường có ${uiState.attractionRatePercent}% cơ hội vào quán)",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 6.dp)
              )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Text("Phân tích thuật toán thu hút:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CoffeeBrownDark)
            Spacer(modifier = Modifier.height(6.dp))
            StatDetailRow("Cấp độ quán:", "Lv.${uiState.cafeLevel} ${uiState.cafeLevelTitle}")
            StatDetailRow("Tỉ lệ cơ bản (Mới mở):", "+18%")
            if (uiState.cafeLevel > 1) {
              StatDetailRow("Thưởng danh tiếng cấp độ:", "+${(uiState.cafeLevel - 1) * 5}%")
            }
            StatDetailRow("Điểm trang trí (${uiState.totalDecorScore} điểm):", "+${(uiState.totalDecorScore * 0.35f).toInt()}%")
            StatDetailRow("Điểm uy tín quán (${String.format("%.1f", uiState.cafeStars)} ⭐):", "+${((uiState.cafeStars - 3.0f) * 6f).coerceAtLeast(0f).toInt()}%")
            StatDetailRow("Thời tiết (${uiState.weather.displayName}):", if (uiState.weather.attractionModifier > 1.0f) "+8%" else "+0%")
            if (uiState.upgrades.any { it.id == "upgrade_flyers" && it.isUnlocked }) {
              StatDetailRow("Tờ rơi quảng cáo (Nâng cấp):", "+10%")
            }
            if (uiState.upgrades.any { it.id == "upgrade_social" && it.isUnlocked }) {
              StatDetailRow("Quảng cáo TikTok (Nâng cấp):", "+18%")
            }
            if (uiState.upgrades.any { it.id == "upgrade_neon" && it.isUnlocked }) {
              StatDetailRow("Biển hiệu Neon (Nâng cấp):", "+12%")
            }
            if (uiState.upgrades.any { it.id == "upgrade_checkin_corner" && it.isUnlocked }) {
              StatDetailRow("Góc Check-in sống ảo (Nâng cấp):", "+15%")
            }
            if (uiState.upgrades.any { it.id == "upgrade_membership" && it.isUnlocked }) {
              StatDetailRow("Thẻ thành viên (Nâng cấp):", "+12%")
            }
            if (uiState.upgrades.any { it.id == "upgrade_ac" && it.isUnlocked }) {
              StatDetailRow("Điều hòa nhiệt độ (Nâng cấp):", "+15%")
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Cafe Overall Stats
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            StatDetailRow("Tiến trình lên cấp tiếp theo:", "${uiState.totalCupsSold} / ${uiState.nextLevelCupsTarget} ly đã bán")
            StatDetailRow("Tổng số ly đã phục vụ:", "${uiState.totalCupsSold} ly")
            StatDetailRow("Tổng doanh thu tích lũy:", formatVnd(uiState.totalRevenue))
            StatDetailRow("Tổng điểm tiện nghi bàn ghế:", "${uiState.totalComfortScore} điểm")
            StatDetailRow("Đồ nội thất & máy móc:", "${uiState.furnitureList.size} món")
            StatDetailRow("Nâng cấp hệ thống đã mở:", "${uiState.upgrades.count { it.isUnlocked }} / ${uiState.upgrades.size}")
            StatDetailRow("Nhân sự hiện tại:", "${uiState.staffList.size} người")
          }
        }
      }
    }
  }
}

@Composable
fun StatDetailRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(text = label, fontSize = 13.sp, color = Color.DarkGray)
    Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CoffeeBrownDark)
  }
}
