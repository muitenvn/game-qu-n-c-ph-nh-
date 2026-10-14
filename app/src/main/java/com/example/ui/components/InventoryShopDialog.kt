package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ShoppingCart
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
import com.example.data.model.IngredientType
import com.example.data.model.formatVnd
import com.example.ui.theme.CoffeeBrownDark
import com.example.ui.theme.CoffeeBrownMedium
import com.example.ui.theme.CoffeeCream

@Composable
fun InventoryShopDialog(
  currentCash: Double,
  inventory: Map<IngredientType, Int>,
  onBuyPack: (IngredientType) -> Unit,
  onBulkRestock: () -> Unit,
  onDismiss: () -> Unit
) {
  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth(0.95f)
        .fillMaxHeight(0.85f)
        .testTag("inventory_shop_dialog"),
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
              text = "Kho Nguyên Liệu & Nhập Hàng",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = CoffeeBrownDark
            )
            Text(
              text = "Số dư: ${formatVnd(currentCash)}",
              fontWeight = FontWeight.SemiBold,
              fontSize = 13.sp,
              color = Color(0xFF2E7D32)
            )
          }
          IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_inventory_dialog")) {
            Icon(Icons.Default.Close, contentDescription = "Đóng", tint = CoffeeBrownDark)
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
          onClick = onBulkRestock,
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth().testTag("bulk_restock_button")
        ) {
          Icon(Icons.Default.ShoppingCart, contentDescription = "Nhập sỉ", modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = "Nhập Đầy Kho Tất Cả Nguyên Liệu", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(IngredientType.values()) { ing ->
            val curStock = inventory[ing] ?: 0
            val isLowStock = curStock < (ing.packSize * 0.3)
            val canAfford = currentCash >= ing.packCost

            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = Color.White),
              elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(46.dp)
                    .background(CoffeeCream, RoundedCornerShape(10.dp)),
                  contentAlignment = Alignment.Center
                ) {
                  Text(text = ing.iconEmoji, fontSize = 24.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = ing.displayName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = CoffeeBrownDark
                  )
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                      text = "Tồn kho: $curStock ${ing.unit}",
                      fontWeight = FontWeight.SemiBold,
                      fontSize = 12.sp,
                      color = if (isLowStock) Color(0xFFD32F2F) else Color(0xFF388E3C)
                    )
                    if (isLowStock) {
                      Spacer(modifier = Modifier.width(6.dp))
                      Text(text = "⚠️ Sắp hết", fontSize = 11.sp, color = Color(0xFFD32F2F))
                    }
                  }
                  Text(
                    text = "Gói +${ing.packSize} ${ing.unit} = ${formatVnd(ing.packCost)}",
                    fontSize = 11.sp,
                    color = Color.Gray
                  )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                  onClick = { onBuyPack(ing) },
                  enabled = canAfford,
                  colors = ButtonDefaults.buttonColors(
                    containerColor = CoffeeBrownMedium,
                    disabledContainerColor = Color.LightGray
                  ),
                  shape = RoundedCornerShape(10.dp),
                  contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                  modifier = Modifier.testTag("buy_pack_${ing.name}")
                ) {
                  Text(text = "+Nhập", fontSize = 12.sp)
                }
              }
            }
          }
        }
      }
    }
  }
}
