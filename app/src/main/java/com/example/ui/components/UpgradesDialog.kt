package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Upgrade
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
import com.example.data.model.CafeUpgrade
import com.example.data.model.UpgradeCategory
import com.example.data.model.formatVnd
import com.example.ui.theme.CoffeeBrownDark
import com.example.ui.theme.CoffeeBrownMedium
import com.example.ui.theme.CoffeeCream

@Composable
fun UpgradesDialog(
  currentCash: Double,
  upgrades: List<CafeUpgrade>,
  onBuyUpgrade: (CafeUpgrade) -> Unit,
  onDismiss: () -> Unit
) {
  var selectedTab by remember { mutableStateOf(0) }
  val categories = UpgradeCategory.values()

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth(0.95f)
        .fillMaxHeight(0.85f)
        .testTag("upgrades_dialog"),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = CoffeeCream)
    ) {
      Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Top Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "⭐ Trung Tâm Nâng Cấp Quán",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = CoffeeBrownDark
            )
            Text(
              text = "Số dư hiện tại: ${formatVnd(currentCash)}",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFF2E7D32)
            )
          }
          IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_upgrades_dialog")) {
            Icon(Icons.Default.Close, contentDescription = "Đóng", tint = CoffeeBrownDark)
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Category Tabs
        ScrollableTabRow(
          selectedTabIndex = selectedTab,
          containerColor = Color.Transparent,
          contentColor = CoffeeBrownDark,
          edgePadding = 0.dp
        ) {
          categories.forEachIndexed { index, cat ->
            Tab(
              selected = selectedTab == index,
              onClick = { selectedTab = index },
              text = {
                Text(
                  text = "${cat.iconEmoji} ${cat.displayName}",
                  fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                  fontSize = 12.sp
                )
              }
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Upgrades List
        val filteredUpgrades = upgrades.filter { it.category == categories[selectedTab] }

        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(filteredUpgrades) { upgrade ->
            UpgradeCard(
              upgrade = upgrade,
              canAfford = currentCash >= upgrade.costVnd,
              onBuyClick = { onBuyUpgrade(upgrade) }
            )
          }
        }
      }
    }
  }
}

@Composable
fun UpgradeCard(
  upgrade: CafeUpgrade,
  canAfford: Boolean,
  onBuyClick: () -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (upgrade.isUnlocked) Color(0xFFF1F8E9) else Color.White
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(52.dp)
          .background(
            if (upgrade.isUnlocked) Color(0xFFDCEDC8) else CoffeeCream,
            RoundedCornerShape(12.dp)
          ),
        contentAlignment = Alignment.Center
      ) {
        Text(text = upgrade.iconEmoji, fontSize = 28.sp)
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = upgrade.name,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = CoffeeBrownDark
        )
        Text(
          text = upgrade.description,
          style = MaterialTheme.typography.bodySmall,
          color = Color.Gray,
          maxLines = 2
        )
        Spacer(modifier = Modifier.height(4.dp))
        Surface(
          color = Color(0xFFE8F5E9),
          shape = RoundedCornerShape(6.dp)
        ) {
          Text(
            text = "⚡ ${upgrade.effectLabel}",
            color = Color(0xFF2E7D32),
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }
      }

      Spacer(modifier = Modifier.width(8.dp))

      if (upgrade.isUnlocked) {
        Surface(
          color = Color(0xFF4CAF50),
          shape = RoundedCornerShape(10.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Check, contentDescription = "Đã có", tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "Đã sở hữu", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      } else {
        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = formatVnd(upgrade.costVnd),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = CoffeeBrownDark
          )
          Spacer(modifier = Modifier.height(4.dp))
          Button(
            onClick = onBuyClick,
            enabled = canAfford,
            colors = ButtonDefaults.buttonColors(
              containerColor = CoffeeBrownMedium,
              disabledContainerColor = Color.LightGray
            ),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.testTag("buy_upgrade_${upgrade.id}")
          ) {
            Text(text = if (canAfford) "Nâng cấp" else "Thiếu tiền", fontSize = 11.sp)
          }
        }
      }
    }
  }
}
