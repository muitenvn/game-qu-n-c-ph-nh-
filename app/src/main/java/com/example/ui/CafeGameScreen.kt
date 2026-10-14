package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.formatVnd
import com.example.data.model.formatVndCompact
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.ActiveDialog
import com.example.viewmodel.CafeGameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CafeGameScreen(
  viewModel: CafeGameViewModel,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()

  Scaffold(
    modifier = modifier.fillMaxSize(),
    contentWindowInsets = WindowInsets.safeDrawing
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .background(Color(0xFFEDE7DF))
    ) {
      // 1. Interactive 3D Isometric Viewport Canvas
      IsometricGameCanvas(
        uiState = uiState,
        onGridHover = { gx, gy -> viewModel.onCanvasGridHover(gx, gy) },
        onGridTap = { gx, gy ->
          if (uiState.isBuildMode) {
            viewModel.confirmPlacementAt(gx, gy)
          } else {
            // Check if player tapped on an existing item or table to clean/inspect
            val tappedItem = uiState.furnitureList.find { it.gridX == gx && it.gridY == gy }
            if (tappedItem != null) {
              if (tappedItem.isTable && tappedItem.isDirty) {
                viewModel.cleanTable(tappedItem.id)
              } else {
                viewModel.showToast("${tappedItem.type.displayName} (Ô $gx, $gy)")
              }
            }
          }
        },
        modifier = Modifier.fillMaxSize()
      )

      // 2. Top HUD Bar (Status, Cash, Weather, Speed)
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 8.dp)
          .align(Alignment.TopCenter)
      ) {
        TopHudBar(
          cash = uiState.cash,
          day = uiState.day,
          timeFormatted = uiState.timeFormatted,
          weatherIcon = uiState.weather.iconEmoji,
          weatherName = uiState.weather.displayName,
          stars = uiState.cafeStars,
          attractionRate = uiState.attractionRatePercent,
          cafeLevel = uiState.cafeLevel,
          cafeLevelTitle = uiState.cafeLevelTitle,
          gameSpeed = uiState.gameSpeed,
          onSetSpeed = { viewModel.setGameSpeed(it) },
          onOpenStats = { viewModel.openDialog(ActiveDialog.CAFE_STATS) }
        )

        // Toast Message Banner
        AnimatedVisibility(
          visible = uiState.toastMessage != null,
          enter = fadeIn(),
          exit = fadeOut(),
          modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
        ) {
          Surface(
            color = CoffeeBrownDark.copy(alpha = 0.92f),
            shape = RoundedCornerShape(20.dp),
            shadowElevation = 4.dp
          ) {
            Text(
              text = uiState.toastMessage ?: "",
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
          }
        }
      }

      // 3. Build Mode Prompt Floating Banner (if placing furniture)
      if (uiState.isBuildMode) {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = CoffeeBrownDark),
          modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = 80.dp)
            .testTag("build_mode_indicator")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Text(
              text = "🏗️ Đang đặt: ${uiState.selectedCatalogItem?.displayName ?: "Nội thất"}",
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
            Button(
              onClick = { viewModel.cancelBuildMode() },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
              shape = RoundedCornerShape(8.dp),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
              Text("Hủy", fontSize = 12.sp)
            }
          }
        }
      }

      // 4. Bottom Quick Dashboard Toolbar
      BottomDashboardBar(
        onOpenBuild = { viewModel.openDialog(ActiveDialog.BUILD_SHOP) },
        onOpenMenu = { viewModel.openDialog(ActiveDialog.MENU_PRICING) },
        onOpenInventory = { viewModel.openDialog(ActiveDialog.INVENTORY_RESTOCK) },
        onOpenUpgrades = { viewModel.openDialog(ActiveDialog.UPGRADES_SHOP) },
        onOpenStaff = { viewModel.openDialog(ActiveDialog.STAFF_MANAGE) },
        onOpenStats = { viewModel.openDialog(ActiveDialog.CAFE_STATS) },
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .padding(bottom = 12.dp)
      )

      // 5. Dialogs Overlay
      when (uiState.activeDialog) {
        ActiveDialog.BUILD_SHOP -> {
          BuildCatalogDialog(
            currentCash = uiState.cash,
            placedFurniture = uiState.furnitureList,
            onSelectItemToPlace = { type -> viewModel.startPlacingCatalogItem(type) },
            onSellFurniture = { furn -> viewModel.sellFurniture(furn) },
            onDismiss = { viewModel.openDialog(null) }
          )
        }
        ActiveDialog.MENU_PRICING -> {
          MenuManagementDialog(
            recipes = uiState.recipes,
            onUpdatePrice = { id, price -> viewModel.updateRecipePrice(id, price) },
            onDismiss = { viewModel.openDialog(null) }
          )
        }
        ActiveDialog.INVENTORY_RESTOCK -> {
          InventoryShopDialog(
            currentCash = uiState.cash,
            inventory = uiState.inventory,
            onBuyPack = { ing -> viewModel.buyIngredientPack(ing) },
            onBulkRestock = { viewModel.bulkRestockAll() },
            onDismiss = { viewModel.openDialog(null) }
          )
        }
        ActiveDialog.UPGRADES_SHOP -> {
          UpgradesDialog(
            currentCash = uiState.cash,
            upgrades = uiState.upgrades,
            onBuyUpgrade = { upgrade -> viewModel.buyUpgrade(upgrade) },
            onDismiss = { viewModel.openDialog(null) }
          )
        }
        ActiveDialog.STAFF_MANAGE -> {
          StaffManagementDialog(
            currentCash = uiState.cash,
            staffList = uiState.staffList,
            onUpgradeStaff = { id, attr -> viewModel.upgradeStaff(id, attr) },
            onHireStaff = { role -> viewModel.hireStaff(role) },
            onDismiss = { viewModel.openDialog(null) }
          )
        }
        ActiveDialog.CAFE_STATS -> {
          CafeStatsSheet(
            uiState = uiState,
            onDismiss = { viewModel.openDialog(null) }
          )
        }
        null -> {}
      }

      // 6. End of Day Daily Financial Report
      if (uiState.showDailyReportDialog) {
        DailyReportDialog(
          dayNumber = uiState.day,
          record = uiState.currentDailyFinance,
          cafeStars = uiState.cafeStars,
          onNextDay = { viewModel.dismissDailyReport() }
        )
      }
    }
  }
}

@Composable
fun TopHudBar(
  cash: Double,
  day: Int,
  timeFormatted: String,
  weatherIcon: String,
  weatherName: String,
  stars: Float,
  attractionRate: Int,
  cafeLevel: Int,
  cafeLevelTitle: String,
  gameSpeed: Float,
  onSetSpeed: (Float) -> Unit,
  onOpenStats: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    modifier = Modifier.fillMaxWidth().testTag("top_hud_bar")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Cash, Level & Stars
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        // Cash Badge
        Surface(
          color = Color(0xFFE8F5E9),
          shape = RoundedCornerShape(10.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(text = "💵", fontSize = 14.sp)
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = formatVndCompact(cash),
              fontWeight = FontWeight.ExtraBold,
              fontSize = 13.sp,
              color = Color(0xFF2E7D32)
            )
          }
        }

        // Cafe Level Badge
        Surface(
          color = Color(0xFFFFF3E0),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.clickable { onOpenStats() }
        ) {
          Text(
            text = "Lv.$cafeLevel $cafeLevelTitle",
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE65100)
          )
        }

        // Stars & Attraction Rate
        Row(
          modifier = Modifier.clickable { onOpenStats() },
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.Star, contentDescription = "Sao", tint = GoldRating, modifier = Modifier.size(15.dp))
          Text(text = String.format("%.1f", stars), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CoffeeBrownDark)
          Spacer(modifier = Modifier.width(4.dp))
          Text(text = "🚶$attractionRate%", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
        }
      }

      // Time & Weather
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = "Ngày $day • $timeFormatted",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = CoffeeBrownDark
          )
          Text(
            text = "$weatherIcon $weatherName",
            fontSize = 11.sp,
            color = Color.DarkGray
          )
        }

        // Game Speed Toggles (Pause, 1x, 2x)
        Row(
          modifier = Modifier
            .background(CoffeeCream, RoundedCornerShape(8.dp))
            .padding(2.dp)
        ) {
          SpeedPill(label = "⏸", isSelected = gameSpeed == 0f, onClick = { onSetSpeed(0f) })
          SpeedPill(label = "1x", isSelected = gameSpeed == 1f, onClick = { onSetSpeed(1f) })
          SpeedPill(label = "2x", isSelected = gameSpeed == 2f, onClick = { onSetSpeed(2f) })
        }
      }
    }
  }
}

@Composable
fun SpeedPill(
  label: String,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(6.dp))
      .background(if (isSelected) CoffeeBrownMedium else Color.Transparent)
      .clickable { onClick() }
      .padding(horizontal = 7.dp, vertical = 4.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      color = if (isSelected) Color.White else CoffeeBrownDark
    )
  }
}

@Composable
fun BottomDashboardBar(
  onOpenBuild: () -> Unit,
  onOpenMenu: () -> Unit,
  onOpenInventory: () -> Unit,
  onOpenUpgrades: () -> Unit,
  onOpenStaff: () -> Unit,
  onOpenStats: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = CoffeeBrownDark.copy(alpha = 0.96f)),
    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    modifier = modifier
      .padding(horizontal = 6.dp)
      .testTag("bottom_dashboard_bar")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 6.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.SpaceEvenly,
      verticalAlignment = Alignment.CenterVertically
    ) {
      DashboardButton(iconEmoji = "🏗️", label = "Xây dựng", onClick = onOpenBuild, tag = "dashboard_build")
      DashboardButton(iconEmoji = "📋", label = "Menu", onClick = onOpenMenu, tag = "dashboard_menu")
      DashboardButton(iconEmoji = "📦", label = "Kho hàng", onClick = onOpenInventory, tag = "dashboard_inventory")
      DashboardButton(iconEmoji = "⭐", label = "Nâng cấp", onClick = onOpenUpgrades, tag = "dashboard_upgrades")
      DashboardButton(iconEmoji = "🧑‍🍳", label = "Nhân sự", onClick = onOpenStaff, tag = "dashboard_staff")
      DashboardButton(iconEmoji = "📊", label = "Thu hút", onClick = onOpenStats, tag = "dashboard_stats")
    }
  }
}

@Composable
fun DashboardButton(
  iconEmoji: String,
  label: String,
  onClick: () -> Unit,
  tag: String
) {
  Surface(
    color = Color.White.copy(alpha = 0.12f),
    shape = RoundedCornerShape(12.dp),
    modifier = Modifier
      .clip(RoundedCornerShape(12.dp))
      .clickable { onClick() }
      .testTag(tag)
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(text = iconEmoji, fontSize = 18.sp)
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = label,
        color = CoffeeCream,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp
      )
    }
  }
}
