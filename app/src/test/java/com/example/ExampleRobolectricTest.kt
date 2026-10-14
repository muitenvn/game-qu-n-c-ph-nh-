package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.IngredientType
import com.example.data.model.ItemType
import com.example.data.model.PriceEvaluation
import com.example.data.model.StaffRole
import com.example.data.repository.CafeSaveManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read app name from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Cozy Cafe", appName)
  }

  @Test
  fun `verify default recipes and pricing evaluation`() {
    val recipes = CafeSaveManager.getDefaultRecipes()
    assertTrue(recipes.isNotEmpty())

    val blackCoffee = recipes.find { it.id == "black_coffee" }
    assertNotNull(blackCoffee)
    assertTrue(blackCoffee!!.profit > 0)
    assertTrue(blackCoffee.ingredients.containsKey(IngredientType.COFFEE_BEANS))
    assertEquals(PriceEvaluation.PRICEY, blackCoffee.priceAttractiveness)
  }

  @Test
  fun `verify default furniture and staff configuration`() {
    val furniture = CafeSaveManager.getDefaultFurniture()
    val staff = CafeSaveManager.getDefaultStaff()

    assertTrue(furniture.any { it.type == ItemType.ORDER_COUNTER })
    assertTrue(furniture.any { it.type == ItemType.ESPRESSO_MACHINE })
    assertTrue(staff.any { it.role == StaffRole.BARISTA })
    assertTrue(staff.any { it.role == StaffRole.WAITER_CLEANER })
  }

  @Test
  fun `verify save and load game state cycle`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val furniture = CafeSaveManager.getDefaultFurniture()
    val inventory = CafeSaveManager.getDefaultInventory()
    val recipes = CafeSaveManager.getDefaultRecipes()
    val staff = CafeSaveManager.getDefaultStaff()
    val upgrades = setOf("upgrade_flyers", "upgrade_neon")

    CafeSaveManager.saveGame(
      context = context,
      cash = 15000000.0,
      day = 3,
      inGameMinutes = 600,
      cafeName = "Tiệm Cà Phê Mây Nhỏ",
      cafeStars = 4.9f,
      totalCupsSold = 42,
      totalRevenue = 1250000.0,
      furniture = furniture,
      inventory = inventory,
      recipes = recipes,
      staff = staff,
      unlockedUpgrades = upgrades
    )

    val loaded = CafeSaveManager.loadGame(context)
    assertNotNull(loaded)
    assertEquals(15000000.0, loaded!!.cash, 0.01)
    assertEquals(3, loaded.day)
    assertEquals("Tiệm Cà Phê Mây Nhỏ", loaded.cafeName)
    assertEquals(4.9f, loaded.cafeStars, 0.1f)
    assertEquals(42, loaded.totalCupsSold)
    assertTrue(loaded.unlockedUpgradeIds.contains("upgrade_flyers"))
    assertTrue(loaded.unlockedUpgradeIds.contains("upgrade_neon"))
  }

  @Test
  fun `verify upgrade catalog and vnd currency formatting`() {
    val upgrades = CafeSaveManager.getDefaultUpgrades()
    assertTrue(upgrades.isNotEmpty())
    assertTrue(upgrades.any { it.id == "upgrade_flyers" })
    assertTrue(upgrades.any { it.id == "upgrade_pro_espresso" })

    val formatted = com.example.data.model.formatVnd(15000000.0)
    assertTrue(formatted.contains("15.000.000"))
    assertTrue(formatted.contains("₫"))

    val compact = com.example.data.model.formatVndCompact(15000000.0)
    assertEquals("15.0 Tr ₫", compact)
  }
}
