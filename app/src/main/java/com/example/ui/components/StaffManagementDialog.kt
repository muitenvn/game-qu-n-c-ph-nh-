package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.StaffMember
import com.example.data.model.StaffRole
import com.example.data.model.formatVnd
import com.example.ui.theme.CoffeeBrownDark
import com.example.ui.theme.CoffeeBrownMedium
import com.example.ui.theme.CoffeeCream

@Composable
fun StaffManagementDialog(
  currentCash: Double,
  staffList: List<StaffMember>,
  onUpgradeStaff: (String, String) -> Unit,
  onHireStaff: (StaffRole) -> Unit,
  onDismiss: () -> Unit
) {
  var showHireOptions by remember { mutableStateOf(false) }
  val hireCost = 800000.0 // 800.000 VNĐ
  val upgradeCost = 500000.0 // 500.000 VNĐ

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth(0.95f)
        .fillMaxHeight(0.85f)
        .testTag("staff_management_dialog"),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = CoffeeCream)
    ) {
      Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Đội Ngũ Nhân Viên",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = CoffeeBrownDark
            )
            Text(
              text = "${staffList.size} nhân viên • Tiền: ${formatVnd(currentCash)}",
              fontWeight = FontWeight.SemiBold,
              fontSize = 13.sp,
              color = Color(0xFF2E7D32)
            )
          }
          IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_staff_dialog")) {
            Icon(Icons.Default.Close, contentDescription = "Đóng", tint = CoffeeBrownDark)
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
          onClick = { showHireOptions = !showHireOptions },
          colors = ButtonDefaults.buttonColors(containerColor = CoffeeBrownMedium),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth().testTag("toggle_hire_options")
        ) {
          Icon(Icons.Default.PersonAdd, contentDescription = "Tuyển dụng", modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = if (showHireOptions) "Đóng Mục Tuyển Dụng" else "Tuyển Thêm Nhân Viên Mới (${formatVnd(hireCost)})", fontWeight = FontWeight.Bold)
        }

        if (showHireOptions) {
          Spacer(modifier = Modifier.height(10.dp))
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
          ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Text("Chọn vị trí ứng tuyển (Phí ${formatVnd(hireCost)}):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
              StaffRole.values().forEach { role ->
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(role.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CoffeeBrownDark)
                    Text(role.description, fontSize = 11.sp, color = Color.Gray)
                    Text("Lương: ${formatVnd(role.baseSalary)}/ngày", fontSize = 11.sp, color = Color(0xFF1976D2))
                  }
                  Button(
                    onClick = {
                      onHireStaff(role)
                      showHireOptions = false
                    },
                    enabled = currentCash >= hireCost,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("hire_role_${role.name}")
                  ) {
                    Text("Tuyển", fontSize = 12.sp)
                  }
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(staffList) { staff ->
            StaffCard(
              staff = staff,
              upgradeCost = upgradeCost,
              canAffordUpgrade = currentCash >= upgradeCost,
              onUpgrade = { attr -> onUpgradeStaff(staff.id, attr) }
            )
          }
        }
      }
    }
  }
}

@Composable
fun StaffCard(
  staff: StaffMember,
  upgradeCost: Double,
  canAffordUpgrade: Boolean,
  onUpgrade: (String) -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(46.dp)
            .background(CoffeeCream, RoundedCornerShape(12.dp)),
          contentAlignment = Alignment.Center
        ) {
          Text(text = staff.avatarEmoji, fontSize = 26.sp)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = staff.name,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = CoffeeBrownDark
          )
          Text(
            text = "${staff.role.title} • Lương: ${formatVnd(staff.dailyWage)}/ngày",
            fontSize = 12.sp,
            color = Color.Gray
          )
        }

        Surface(
          color = Color(0xFFE8F5E9),
          shape = RoundedCornerShape(8.dp)
        ) {
          Text(
            text = "Tự động chạy",
            color = Color(0xFF2E7D32),
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))
      HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
      Spacer(modifier = Modifier.height(10.dp))

      Text("Nâng cấp chỉ số (${formatVnd(upgradeCost)}/cấp):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CoffeeBrownDark)
      Spacer(modifier = Modifier.height(6.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        StatUpgradePill(
          title = "Tốc độ: Lv${staff.speedLevel}",
          canUpgrade = canAffordUpgrade,
          onClick = { onUpgrade("SPEED") },
          modifier = Modifier.weight(1f)
        )
        StatUpgradePill(
          title = "Pha chế: Lv${staff.brewSkillLevel}",
          canUpgrade = canAffordUpgrade,
          onClick = { onUpgrade("BREW") },
          modifier = Modifier.weight(1f)
        )
        StatUpgradePill(
          title = "Thân thiện: Lv${staff.friendlinessLevel}",
          canUpgrade = canAffordUpgrade,
          onClick = { onUpgrade("FRIENDLY") },
          modifier = Modifier.weight(1f)
        )
      }
    }
  }
}

@Composable
fun StatUpgradePill(
  title: String,
  canUpgrade: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  OutlinedButton(
    onClick = onClick,
    enabled = canUpgrade,
    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
    shape = RoundedCornerShape(8.dp),
    modifier = modifier
  ) {
    Text(text = "$title +", fontSize = 11.sp, fontWeight = FontWeight.Bold)
  }
}
