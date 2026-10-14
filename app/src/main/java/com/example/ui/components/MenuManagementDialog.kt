package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
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
import com.example.data.model.PriceEvaluation
import com.example.data.model.Recipe
import com.example.data.model.formatVnd
import com.example.ui.theme.CoffeeBrownDark
import com.example.ui.theme.CoffeeBrownMedium
import com.example.ui.theme.CoffeeCream

@Composable
fun MenuManagementDialog(
  recipes: List<Recipe>,
  onUpdatePrice: (String, Double) -> Unit,
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
        .testTag("menu_management_dialog"),
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
              text = "Thiết Lập Menu & Giá Bán",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = CoffeeBrownDark
            )
            Text(
              text = "Điều chỉnh giá bán đồ uống theo VNĐ tối ưu lợi nhuận",
              style = MaterialTheme.typography.bodySmall,
              color = Color.Gray
            )
          }
          IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_menu_dialog")) {
            Icon(Icons.Default.Close, contentDescription = "Đóng", tint = CoffeeBrownDark)
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          items(recipes) { recipe ->
            RecipePriceCard(
              recipe = recipe,
              onPriceChanged = { newPrice -> onUpdatePrice(recipe.id, newPrice) }
            )
          }
        }
      }
    }
  }
}

@Composable
fun RecipePriceCard(
  recipe: Recipe,
  onPriceChanged: (Double) -> Unit
) {
  val priceEval = recipe.priceAttractiveness
  val evalColor = when (priceEval) {
    PriceEvaluation.VERY_CHEAP -> Color(0xFF388E3C)
    PriceEvaluation.FAIR -> Color(0xFF1976D2)
    PriceEvaluation.PRICEY -> Color(0xFFF57C00)
    PriceEvaluation.EXPENSIVE -> Color(0xFFD32F2F)
  }

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
          Text(text = recipe.iconEmoji, fontSize = 24.sp)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = recipe.name,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = CoffeeBrownDark
          )
          Text(
            text = "${recipe.categoryName} • Pha: ${recipe.prepTimeSeconds}s",
            fontSize = 12.sp,
            color = Color.Gray
          )
        }

        Surface(
          color = evalColor.copy(alpha = 0.12f),
          shape = RoundedCornerShape(8.dp)
        ) {
          Text(
            text = priceEval.label,
            color = evalColor,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))
      HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Giá vốn: ${formatVnd(recipe.baseCost)}",
            fontSize = 12.sp,
            color = Color.Gray
          )
          Text(
            text = "Lợi nhuận: +${formatVnd(recipe.profit)} (${recipe.profitMarginPercent}%)",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (recipe.profit > 0) Color(0xFF2E7D32) else Color(0xFFD32F2F)
          )
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          FilledIconButton(
            onClick = {
              val next = (recipe.sellingPrice - 2000.0).coerceAtLeast(recipe.baseCost)
              onPriceChanged(next)
            },
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = CoffeeCream),
            modifier = Modifier.size(36.dp).testTag("decrease_price_${recipe.id}")
          ) {
            Icon(Icons.Default.Remove, contentDescription = "Giảm", tint = CoffeeBrownDark, modifier = Modifier.size(18.dp))
          }

          Text(
            text = formatVnd(recipe.sellingPrice),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = CoffeeBrownDark,
            modifier = Modifier.padding(horizontal = 4.dp)
          )

          FilledIconButton(
            onClick = {
              val next = (recipe.sellingPrice + 2000.0).coerceAtMost(200000.0)
              onPriceChanged(next)
            },
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = CoffeeCream),
            modifier = Modifier.size(36.dp).testTag("increase_price_${recipe.id}")
          ) {
            Icon(Icons.Default.Add, contentDescription = "Tăng", tint = CoffeeBrownDark, modifier = Modifier.size(18.dp))
          }
        }
      }
    }
  }
}
