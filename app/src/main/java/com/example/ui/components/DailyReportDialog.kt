package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.data.model.DailyFinancialRecord
import com.example.data.model.formatVnd
import com.example.ui.theme.CoffeeBrownDark
import com.example.ui.theme.CoffeeBrownMedium
import com.example.ui.theme.CoffeeCream
import com.example.ui.theme.GoldRating

@Composable
fun DailyReportDialog(
  dayNumber: Int,
  record: DailyFinancialRecord,
  cafeStars: Float,
  onNextDay: () -> Unit
) {
  Dialog(
    onDismissRequest = {},
    properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth(0.95f)
        .testTag("daily_report_dialog"),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = CoffeeCream)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "🌙 Tổng Kết Ngày $dayNumber",
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold,
          color = CoffeeBrownDark
        )
        Text(
          text = "Quán đóng cửa lúc 22:00 để dọn dẹp và kiểm toán",
          style = MaterialTheme.typography.bodySmall,
          color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Star, contentDescription = "Sao", tint = GoldRating, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Đánh giá trung bình: ${String.format("%.1f", cafeStars)} / 5.0",
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = CoffeeBrownDark
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            LedgerRow(label = "Số ly phục vụ:", value = "${record.cupsSold} ly", valueColor = CoffeeBrownDark)
            LedgerRow(label = "Khách hàng ghé quán:", value = "${record.customerCount} người", valueColor = CoffeeBrownDark)
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            LedgerRow(
              label = "Doanh thu bán hàng:",
              value = "+${formatVnd(record.grossRevenue)}",
              valueColor = Color(0xFF2E7D32),
              isBold = true
            )
            LedgerRow(
              label = "Chi phí nguyên liệu:",
              value = "-${formatVnd(record.ingredientExpense)}",
              valueColor = Color(0xFFD32F2F)
            )
            LedgerRow(
              label = "Tiền lương nhân viên:",
              value = "-${formatVnd(record.staffWages)}",
              valueColor = Color(0xFFD32F2F)
            )
            if (record.upgradeExpense > 0) {
              LedgerRow(
                label = "Chi phí nâng cấp:",
                value = "-${formatVnd(record.upgradeExpense)}",
                valueColor = Color(0xFFD32F2F)
              )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            val netProfit = record.netProfit
            LedgerRow(
              label = "LỢI NHUẬN RÒNG:",
              value = "${if (netProfit >= 0) "+" else ""}${formatVnd(netProfit)}",
              valueColor = if (netProfit >= 0) Color(0xFF2E7D32) else Color(0xFFD32F2F),
              isBold = true,
              fontSize = 16.sp
            )
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Button(
          onClick = onNextDay,
          colors = ButtonDefaults.buttonColors(containerColor = CoffeeBrownMedium),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("start_next_day_button")
        ) {
          Text(text = "Bắt Đầu Ngày Mới ☀️", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
fun LedgerRow(
  label: String,
  value: String,
  valueColor: Color,
  isBold: Boolean = false,
  fontSize: androidx.compose.ui.unit.TextUnit = 14.sp
) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(text = label, fontSize = fontSize, color = CoffeeBrownDark, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
    Text(text = value, fontSize = fontSize, color = valueColor, fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold)
  }
}
