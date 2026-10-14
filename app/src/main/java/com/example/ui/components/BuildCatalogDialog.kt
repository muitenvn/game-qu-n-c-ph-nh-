package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
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
import com.example.data.model.FurnitureItem
import com.example.data.model.ItemCategory
import com.example.data.model.ItemType
import com.example.data.model.formatVnd
import com.example.ui.theme.CoffeeBrownDark
import com.example.ui.theme.CoffeeBrownMedium
import com.example.ui.theme.CoffeeCream

@Composable
fun BuildCatalogDialog(
  currentCash: Double,
  placedFurniture: List<FurnitureItem>,
  onSelectItemToPlace: (ItemType) -> Unit,
  onSellFurniture: (FurnitureItem) -> Unit,
  onDismiss: () -> Unit
) {
  var selectedTab by remember { mutableStateOf(0) }
  val tabs = listOf("Máy móc & Quầy", "Bàn ghế", "Trang trí", "Đã đặt (${placedFurniture.size})")

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth(0.95f)
        .fillMaxHeight(0.85f)
        .testTag("build_catalog_dialog"),
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
              text = "Cửa Hàng Xây Dựng & Nội Thất",
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
          IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_build_dialog")) {
            Icon(Icons.Default.Close, contentDescription = "Đóng", tint = CoffeeBrownDark)
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Category Tab Row
        ScrollableTabRow(
          selectedTabIndex = selectedTab,
          containerColor = Color.Transparent,
          contentColor = CoffeeBrownDark,
          edgePadding = 0.dp
        ) {
          tabs.forEachIndexed { index, title ->
            Tab(
              selected = selectedTab == index,
              onClick = { selectedTab = index },
              text = {
                Text(
                  text = title,
                  fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                  fontSize = 13.sp
                )
              }
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Content
        if (selectedTab < 3) {
          val category = when (selectedTab) {
            0 -> ItemCategory.STATIONS
            1 -> ItemCategory.SEATING
            else -> ItemCategory.DECOR
          }
          val items = ItemType.values().filter { it.category == category }

          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            items(items) { item ->
              CatalogItemCard(
                item = item,
                canAfford = currentCash >= item.cost,
                onBuyClick = { onSelectItemToPlace(item) }
              )
            }
          }
        } else {
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            if (placedFurniture.isEmpty()) {
              item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                  Text("Chưa có đồ nội thất nào được đặt.", color = CoffeeBrownMedium)
                }
              }
            } else {
              items(placedFurniture) { furn ->
                PlacedItemCard(
                  item = furn,
                  onSellClick = { onSellFurniture(furn) }
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun CatalogItemCard(
  item: ItemType,
  canAfford: Boolean,
  onBuyClick: () -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(52.dp)
          .background(CoffeeCream, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
      ) {
        Text(text = item.iconEmoji, fontSize = 28.sp)
      }

      Spacer(modifier = Modifier.width(14.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = item.displayName,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = CoffeeBrownDark
        )
        Text(
          text = item.description,
          style = MaterialTheme.typography.bodySmall,
          color = Color.Gray,
          maxLines = 2
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          if (item.decorBonus > 0) {
            Text(
              text = "+${item.decorBonus} Đẹp 🪴",
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFF2E7D32)
            )
          }
          if (item.comfortBonus > 0) {
            Text(
              text = "+${item.comfortBonus} Êm 🪑",
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFF1976D2)
            )
          }
        }
      }

      Spacer(modifier = Modifier.width(8.dp))

      Column(horizontalAlignment = Alignment.End) {
        Text(
          text = formatVnd(item.cost),
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          color = CoffeeBrownDark
        )
        Spacer(modifier = Modifier.height(6.dp))
        Button(
          onClick = onBuyClick,
          enabled = canAfford,
          colors = ButtonDefaults.buttonColors(
            containerColor = CoffeeBrownMedium,
            disabledContainerColor = Color.LightGray
          ),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.testTag("buy_item_${item.name}")
        ) {
          Text(text = if (canAfford) "Mua & Đặt" else "Thiếu tiền", fontSize = 11.sp)
        }
      }
    }
  }
}

@Composable
fun PlacedItemCard(
  item: FurnitureItem,
  onSellClick: () -> Unit
) {
  val refund = item.type.cost * 0.70

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(text = item.type.iconEmoji, fontSize = 24.sp)
      Spacer(modifier = Modifier.width(12.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = item.type.displayName,
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp,
          color = CoffeeBrownDark
        )
        Text(
          text = "Vị trí ô: (${item.gridX}, ${item.gridY})",
          fontSize = 12.sp,
          color = Color.Gray
        )
      }
      OutlinedButton(
        onClick = onSellClick,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
        modifier = Modifier.testTag("sell_item_${item.id}")
      ) {
        Icon(Icons.Default.Delete, contentDescription = "Bán", modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = "Bán (${formatVnd(refund)})", fontSize = 11.sp)
      }
    }
  }
}
