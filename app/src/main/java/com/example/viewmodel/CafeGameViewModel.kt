package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.CafeSaveManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.random.Random

data class CafeUiState(
  val cash: Double = 15000000.0, // 15.000.000 VNĐ
  val day: Int = 1,
  val inGameMinutes: Int = 420, // 07:00 AM
  val gameSpeed: Float = 1.0f,
  val cafeName: String = "Tiệm Cà Phê Mây Nhỏ",
  val cafeStars: Float = 4.8f,
  val totalCupsSold: Int = 0,
  val totalRevenue: Double = 0.0,
  val weather: WeatherType = WeatherType.SUNNY,

  val furnitureList: List<FurnitureItem> = emptyList(),
  val inventory: Map<IngredientType, Int> = emptyMap(),
  val recipes: List<Recipe> = emptyList(),
  val staffList: List<StaffMember> = emptyList(),
  val upgrades: List<CafeUpgrade> = emptyList(),
  val customers: List<Customer> = emptyList(),
  val orderTickets: List<OrderTicket> = emptyList(),

  // Building Placement Mode
  val isBuildMode: Boolean = false,
  val selectedCatalogItem: ItemType? = null,
  val selectedFurnitureToMove: FurnitureItem? = null,
  val hoveredGridX: Int? = null,
  val hoveredGridY: Int? = null,
  val isValidPlacement: Boolean = false,

  // Daily Financial Record
  val currentDailyFinance: DailyFinancialRecord = DailyFinancialRecord(dayNumber = 1),
  val showDailyReportDialog: Boolean = false,

  // Active Management Dialogs
  val activeDialog: ActiveDialog? = null,

  // Status Banner Message
  val toastMessage: String? = null
) {
  val timeFormatted: String get() {
    val h = (inGameMinutes / 60) % 24
    val m = inGameMinutes % 60
    return String.format("%02d:%02d", h, m)
  }

  val totalDecorScore: Int get() {
    val neonBonus = if (upgrades.any { it.id == "upgrade_neon" && it.isUnlocked }) 30 else 0
    return furnitureList.sumOf { it.decorBonus } + neonBonus
  }

  val totalComfortScore: Int get() = furnitureList.sumOf { it.comfortBonus }

  val cafeLevel: Int get() {
    return when {
      totalCupsSold >= 200 -> 5
      totalCupsSold >= 100 -> 4
      totalCupsSold >= 45 -> 3
      totalCupsSold >= 15 -> 2
      else -> 1
    }
  }

  val cafeLevelTitle: String get() {
    return when (cafeLevel) {
      1 -> "Mới Mở"
      2 -> "Quán Quen"
      3 -> "Hút Khách"
      4 -> "Hot Trend"
      5 -> "Đỉnh Cao"
      else -> "Mới Mở"
    }
  }

  val nextLevelCupsTarget: Int get() {
    return when (cafeLevel) {
      1 -> 15
      2 -> 45
      3 -> 100
      4 -> 200
      else -> 200
    }
  }

  val attractionRatePercent: Int get() {
    // When newly opened (level 1), base attraction is humble (18%), realistic for a new coffee shop!
    val base = 18
    val decorBonus = (totalDecorScore * 0.35f).coerceAtMost(35f).toInt()
    val starBonus = ((cafeStars - 3.0f) * 6f).coerceAtLeast(0f).toInt()
    val weatherBonus = if (weather == WeatherType.HEATWAVE || weather == WeatherType.SUNNY) 8 else 0
    val levelBonus = (cafeLevel - 1) * 5

    // Upgrades marketing bonuses
    var upgradeBonus = 0
    if (upgrades.any { it.id == "upgrade_flyers" && it.isUnlocked }) upgradeBonus += 10
    if (upgrades.any { it.id == "upgrade_social" && it.isUnlocked }) upgradeBonus += 18
    if (upgrades.any { it.id == "upgrade_neon" && it.isUnlocked }) upgradeBonus += 12
    if (upgrades.any { it.id == "upgrade_checkin_corner" && it.isUnlocked }) upgradeBonus += 15
    if (upgrades.any { it.id == "upgrade_membership" && it.isUnlocked }) upgradeBonus += 12
    if (upgrades.any { it.id == "upgrade_ac" && it.isUnlocked && (weather == WeatherType.HEATWAVE || weather == WeatherType.RAINY) }) upgradeBonus += 15

    return (base + decorBonus + starBonus + weatherBonus + levelBonus + upgradeBonus).coerceIn(15, 95)
  }
}

enum class ActiveDialog {
  BUILD_SHOP,
  MENU_PRICING,
  INVENTORY_RESTOCK,
  STAFF_MANAGE,
  UPGRADES_SHOP,
  CAFE_STATS
}

class CafeGameViewModel(application: Application) : AndroidViewModel(application) {

  private val _uiState = MutableStateFlow(CafeUiState())
  val uiState: StateFlow<CafeUiState> = _uiState.asStateFlow()

  private var nextCustomerId = 1
  private var nextOrderId = 1
  private var timeSeconds = 0f
  private var lastSpawnTime = 0f

  init {
    loadSavedState()
    startGameLoop()
  }

  private fun loadSavedState() {
    val context = getApplication<Application>().applicationContext
    val saved = CafeSaveManager.loadGame(context)
    val defaultUpgrades = CafeSaveManager.getDefaultUpgrades()

    if (saved != null) {
      val defaultRecipes = CafeSaveManager.getDefaultRecipes()
      val mergedRecipes = defaultRecipes.map { r ->
        val customPrice = saved.customPrices[r.id]
        if (customPrice != null) r.copy(sellingPrice = customPrice) else r
      }

      val mergedUpgrades = defaultUpgrades.map { u ->
        if (saved.unlockedUpgradeIds.contains(u.id)) u.copy(isUnlocked = true) else u
      }

      _uiState.update {
        it.copy(
          cash = saved.cash,
          day = saved.day,
          inGameMinutes = saved.inGameMinutes,
          cafeName = saved.cafeName,
          cafeStars = saved.cafeStars,
          totalCupsSold = saved.totalCupsSold,
          totalRevenue = saved.totalRevenue,
          furnitureList = saved.furniture,
          inventory = saved.inventory,
          recipes = mergedRecipes,
          staffList = saved.staff,
          upgrades = mergedUpgrades
        )
      }
    } else {
      _uiState.update {
        it.copy(
          cash = 15000000.0, // 15.000.000 VNĐ
          day = 1,
          inGameMinutes = 420,
          furnitureList = CafeSaveManager.getDefaultFurniture(),
          inventory = CafeSaveManager.getDefaultInventory(),
          recipes = CafeSaveManager.getDefaultRecipes(),
          staffList = CafeSaveManager.getDefaultStaff(),
          upgrades = defaultUpgrades
        )
      }
    }
  }

  private fun autoSave() {
    val s = _uiState.value
    val context = getApplication<Application>().applicationContext
    val unlockedIds = s.upgrades.filter { it.isUnlocked }.map { it.id }.toSet()
    CafeSaveManager.saveGame(
      context = context,
      cash = s.cash,
      day = s.day,
      inGameMinutes = s.inGameMinutes,
      cafeName = s.cafeName,
      cafeStars = s.cafeStars,
      totalCupsSold = s.totalCupsSold,
      totalRevenue = s.totalRevenue,
      furniture = s.furnitureList,
      inventory = s.inventory,
      recipes = s.recipes,
      staff = s.staffList,
      unlockedUpgrades = unlockedIds
    )
  }

  private fun startGameLoop() {
    viewModelScope.launch {
      val dt = 0.05f
      while (true) {
        val speed = _uiState.value.gameSpeed
        if (speed > 0f) {
          timeSeconds += dt * speed
          updateSimulation(dt * speed)
        }
        delay(50)
      }
    }
  }

  private fun updateSimulation(effectiveDt: Float) {
    _uiState.update { state ->
      var newMinutes = state.inGameMinutes + (effectiveDt * 2f).toInt()
      val currentFinance = state.currentDailyFinance

      // End of day at 22:00
      if (newMinutes >= 1320 && !state.showDailyReportDialog) {
        newMinutes = 1320
        val totalWages = state.staffList.sumOf { it.dailyWage }
        currentFinance.staffWages = totalWages
        val newCash = (state.cash - totalWages).coerceAtLeast(0.0)
        autoSave()
        return@update state.copy(
          cash = newCash,
          inGameMinutes = newMinutes,
          showDailyReportDialog = true,
          gameSpeed = 0f,
          currentDailyFinance = currentFinance
        )
      }

      val updatedCustomers = state.customers.toMutableList()
      // Rush hour calculation: 07:30 - 09:30 (450 - 570 mins) and 12:00 - 13:30 (720 - 810 mins)
      val isRushHour = (newMinutes in 450..570) || (newMinutes in 720..810)
      val spawnInterval = if (isRushHour) 2.0f else 2.8f
      if (timeSeconds - lastSpawnTime > (spawnInterval / state.weather.attractionModifier)) {
        lastSpawnTime = timeSeconds
        if (updatedCustomers.size < 16) {
          spawnPedestrian(updatedCustomers)
        }
      }

      val updatedStaff = state.staffList.map { it.copy() }.toMutableList()
      val updatedFurniture = state.furnitureList.map { it.copy() }.toMutableList()
      val updatedTickets = state.orderTickets.toMutableList()
      val updatedInventory = state.inventory.toMutableMap()
      var cashDelta = 0.0
      var cupsSoldDelta = 0
      var newRating = state.cafeStars

      val hasProEspresso = state.upgrades.any { it.id == "upgrade_pro_espresso" && it.isUnlocked }
      val hasFastDishwasher = state.upgrades.any { it.id == "upgrade_fast_dishwasher" && it.isUnlocked }
      val hasWifi = state.upgrades.any { it.id == "upgrade_wifi" && it.isUnlocked }
      val hasAutoGrinder = state.upgrades.any { it.id == "upgrade_auto_grinder" && it.isUnlocked }
      val hasTakeaway = state.upgrades.any { it.id == "upgrade_takeaway" && it.isUnlocked }
      val hasLofiMusic = state.upgrades.any { it.id == "upgrade_music_lofi" && it.isUnlocked }
      val hasSelfKiosk = state.upgrades.any { it.id == "upgrade_self_kiosk" && it.isUnlocked }

      updateCustomerStateMachine(
        customers = updatedCustomers,
        furniture = updatedFurniture,
        recipes = state.recipes,
        tickets = updatedTickets,
        inventory = updatedInventory,
        attractionRate = state.attractionRatePercent,
        hasAutoGrinder = hasAutoGrinder,
        hasWifi = hasWifi,
        hasTakeaway = hasTakeaway,
        hasLofiMusic = hasLofiMusic,
        hasSelfKiosk = hasSelfKiosk,
        cafeStars = state.cafeStars,
        totalCupsSold = state.totalCupsSold,
        dt = effectiveDt,
        onCustomerPaid = { paid, tip, stars ->
          cashDelta += (paid + tip)
          cupsSoldDelta++
          currentFinance.grossRevenue += (paid + tip)
          currentFinance.cupsSold++
          currentFinance.customerCount++
          newRating = (newRating * 0.9f + stars * 0.1f).coerceIn(1.0f, 5.0f)
        }
      )

      updateStaffStateMachine(
        staffList = updatedStaff,
        furniture = updatedFurniture,
        tickets = updatedTickets,
        customers = updatedCustomers,
        hasProEspresso = hasProEspresso,
        hasFastDishwasher = hasFastDishwasher,
        dt = effectiveDt
      )

      state.copy(
        inGameMinutes = newMinutes,
        cash = state.cash + cashDelta,
        totalCupsSold = state.totalCupsSold + cupsSoldDelta,
        totalRevenue = state.totalRevenue + cashDelta,
        cafeStars = newRating,
        customers = updatedCustomers,
        staffList = updatedStaff,
        furnitureList = updatedFurniture,
        orderTickets = updatedTickets,
        inventory = updatedInventory,
        currentDailyFinance = currentFinance
      )
    }
  }

  private fun spawnPedestrian(customers: MutableList<Customer>) {
    val spawnLeft = Random.nextBoolean()
    val startX = if (spawnLeft) -2.5f else 10.5f
    val targetX = if (spawnLeft) 10.5f else -2.5f
    val dirX = if (spawnLeft) 1f else -1f
    val names = listOf("Minh", "Lan", "Tuấn", "Hà", "Phong", "An", "Vinh", "Trang", "Đức", "Linh")
    val shirts = listOf(0xFF42A5F5, 0xFFE91E63, 0xFFFFB300, 0xFF4CAF50, 0xFF9C27B0, 0xFF00ACC1)
    val umbrellas = if (_uiState.value.weather == WeatherType.RAINY) listOf(0xFFE53935, 0xFF1E88E5, 0xFFFFB300) else null

    customers.add(
      Customer(
        id = "cust_${nextCustomerId++}",
        name = names.random(),
        posX = startX,
        posY = 9.0f,
        targetX = targetX,
        targetY = 9.0f,
        directionX = dirX,
        state = CustomerState.WALKING_SIDEWALK,
        shirtColorHex = shirts.random(),
        umbrellaColorHex = umbrellas?.random()
      )
    )
  }

  private fun updateCustomerStateMachine(
    customers: MutableList<Customer>,
    furniture: MutableList<FurnitureItem>,
    recipes: List<Recipe>,
    tickets: MutableList<OrderTicket>,
    inventory: MutableMap<IngredientType, Int>,
    attractionRate: Int,
    hasAutoGrinder: Boolean,
    hasWifi: Boolean,
    hasTakeaway: Boolean,
    hasLofiMusic: Boolean,
    hasSelfKiosk: Boolean,
    cafeStars: Float,
    totalCupsSold: Int,
    dt: Float,
    onCustomerPaid: (Double, Double, Int) -> Unit
  ) {
    val counter = furniture.find { it.type == ItemType.ORDER_COUNTER }
    val toRemove = mutableListOf<Customer>()

    for (cust in customers) {
      if (cust.thoughtDuration > 0f) {
        cust.thoughtDuration -= dt
        if (cust.thoughtDuration <= 0f) cust.thoughtBubble = null
      }

      when (cust.state) {
        CustomerState.WALKING_SIDEWALK -> {
          moveEntityTowards(cust, cust.targetX, cust.targetY, 1.2f * dt)

          // Each pedestrian evaluates entering the cafe EXACTLY ONCE as they walk past the entrance!
          if (!cust.hasEvaluatedEntry && abs(cust.posX - 3.7f) < 0.45f) {
            cust.hasEvaluatedEntry = true

            val tableOccupancy = customers
              .filter { it.state == CustomerState.FINDING_SEAT || it.state == CustomerState.SEATED_WAITING || it.state == CustomerState.CONSUMING }
              .mapNotNull { it.assignedTableId }
              .groupingBy { it }
              .eachCount()
            val hasAvailableTable = furniture.any { it.isTable && (tableOccupancy[it.id] ?: 0) < it.maxSeats }
            val counterQueueCount = customers.count { it.state == CustomerState.QUEUING_AT_COUNTER || it.state == CustomerState.ENTERING_CAFE }

            val roll = Random.nextInt(100)
            if (roll < attractionRate && counter != null) {
              if (counterQueueCount >= 4 && !hasSelfKiosk && Random.nextInt(100) < 45) {
                cust.thoughtBubble = "Quầy hơi đông, lát mình ghé lại nha ~"
                cust.thoughtDuration = 2.0f
              } else {
                cust.state = CustomerState.ENTERING_CAFE
                cust.targetX = 3.5f
                cust.targetY = 7.0f
                val enterThoughts = if (cafeStars <= 3.5f || totalCupsSold < 15) {
                  listOf("Quán mới mở xinh ghê!", "Ghé ủng hộ quán mới!", "Thèm cà phê quá!", "Thơm nức mùi rang xay!", "Vào xem menu thử!")
                } else {
                  listOf("Quán quen đây rồi!", "Cà phê ngon số 1!", "Ghé quán quen thư giãn!", "Mùi cà phê thơm nức!", "Làm ly cho tỉnh táo!")
                }
                cust.thoughtBubble = enterThoughts.random()
                cust.thoughtDuration = 2.2f
              }
            } else {
              // Pedestrian does NOT enter (just walking past along the sidewalk)
              if (Random.nextInt(100) < 30) {
                val passerThoughts = listOf(
                  "Đi dạo mát thôi ~",
                  "Vội đi làm mất rồi!",
                  "Quán mới nhìn xinh ghê",
                  "Hôm nay đã uống ở nhà",
                  "Cuối tuần sẽ ghé thử",
                  "Trời hôm nay đẹp thật",
                  "Để rủ bạn qua sau nhé"
                )
                cust.thoughtBubble = passerThoughts.random()
                cust.thoughtDuration = 1.8f
              }
            }
          }

          if (cust.posX < -3f || cust.posX > 11f) {
            toRemove.add(cust)
          }
        }

        CustomerState.ENTERING_CAFE -> {
          moveEntityTowards(cust, cust.targetX, cust.targetY, 1.3f * dt)
          if (hypot(cust.posX - cust.targetX, cust.posY - cust.targetY) < 0.2f) {
            cust.state = CustomerState.QUEUING_AT_COUNTER
            if (counter != null) {
              cust.targetX = counter.gridX + 0.5f
              cust.targetY = counter.gridY + 1.2f
            }
          }
        }

        CustomerState.QUEUING_AT_COUNTER -> {
          val queueSpeed = if (hasSelfKiosk) 2.0f else 1.3f
          moveEntityTowards(cust, cust.targetX, cust.targetY, queueSpeed * dt)
          if (hypot(cust.posX - cust.targetX, cust.posY - cust.targetY) < 0.3f) {
            cust.state = CustomerState.ORDERING
          }
        }

        CustomerState.ORDERING -> {
          val availableRecipes = recipes.filter { r ->
            val hasStation = r.requiresStation == null || furniture.any { it.type == r.requiresStation }
            val hasIngredients = r.ingredients.all { (ing, qty) -> (inventory[ing] ?: 0) >= qty }
            hasStation && hasIngredients
          }

          if (availableRecipes.isNotEmpty()) {
            val chosen = availableRecipes.random()
            cust.desiredRecipeId = chosen.id
            chosen.ingredients.forEach { (ing, qty) ->
              val cur = inventory[ing] ?: 0
              val finalQty = if (ing == IngredientType.COFFEE_BEANS && hasAutoGrinder) (qty * 0.8).toInt().coerceAtLeast(1) else qty
              inventory[ing] = (cur - finalQty).coerceAtLeast(0)
            }
            cust.thoughtBubble = if (hasSelfKiosk) "📱 Kiosk: ${chosen.iconEmoji} ${chosen.name}" else "${chosen.iconEmoji} 1 ${chosen.name}"
            cust.thoughtDuration = 2.2f
            cust.state = CustomerState.FINDING_SEAT
          } else {
            cust.thoughtBubble = "Hết món rồi buồn quá..."
            cust.thoughtDuration = 2.0f
            cust.state = CustomerState.LEAVING
            cust.targetX = 3.5f
            cust.targetY = 9.0f
          }
        }

        CustomerState.FINDING_SEAT -> {
          // If customer has NOT picked a table yet, assign one!
          if (cust.assignedTableId == null) {
            val tableOccupancy = customers
              .filter { (it.state == CustomerState.FINDING_SEAT || it.state == CustomerState.SEATED_WAITING || it.state == CustomerState.CONSUMING) && it.id != cust.id }
              .mapNotNull { it.assignedTableId }
              .groupingBy { it }
              .eachCount()

            val cleanTable = furniture.find { it.isTable && !it.isDirty && (tableOccupancy[it.id] ?: 0) < it.maxSeats }
            val dirtyTable = furniture.find { it.isTable && it.isDirty && (tableOccupancy[it.id] ?: 0) < it.maxSeats }
            val availableTable = cleanTable ?: dirtyTable

            if (availableTable != null) {
              cust.assignedTableId = availableTable.id
              val seatIdx = tableOccupancy[availableTable.id] ?: 0
              val (offsetX, offsetY) = when (seatIdx % 4) {
                0 -> Pair(0.2f, 0.5f)
                1 -> Pair(0.8f, 0.5f)
                2 -> Pair(0.5f, 0.2f)
                else -> Pair(0.5f, 0.8f)
              }
              cust.targetX = availableTable.gridX + offsetX
              cust.targetY = availableTable.gridY + offsetY
              if (availableTable.isDirty) {
                cust.thoughtBubble = "Bàn hơi bụi, bạn nhân viên lau giúp mình nhé ✨"
                cust.thoughtDuration = 2.5f
              } else {
                cust.thoughtBubble = null
              }
            } else {
              // All tables in entire cafe have all seats physically occupied -> seamlessly order takeaway!
              cust.isTakeaway = true
              cust.thoughtBubble = "Hết bàn trống, mình mua mang về nhé! 🥤"
              cust.thoughtDuration = 2.2f
              cust.state = CustomerState.SEATED_WAITING
              cust.targetX = (counter?.gridX?.toFloat() ?: 3.5f) + 1.2f
              cust.targetY = (counter?.gridY?.toFloat() ?: 4.5f) + 1.0f
              val recipe = recipes.find { it.id == cust.desiredRecipeId }
              if (recipe != null) {
                tickets.add(
                  OrderTicket(
                    id = "order_${nextOrderId++}",
                    customerId = cust.id,
                    recipeId = recipe.id,
                    recipeName = "${recipe.name} (Mang về)",
                    tableId = "takeaway",
                    prepRemainingSeconds = recipe.prepTimeSeconds * 0.8f
                  )
                )
              }
            }
          }

          // If customer HAS picked a table, walk to that table!
          if (cust.assignedTableId != null) {
            moveEntityTowards(cust, cust.targetX, cust.targetY, 1.4f * dt)
            if (hypot(cust.posX - cust.targetX, cust.posY - cust.targetY) < 0.35f) {
              cust.state = CustomerState.SEATED_WAITING
              val recipe = recipes.find { it.id == cust.desiredRecipeId }
              if (recipe != null) {
                tickets.add(
                  OrderTicket(
                    id = "order_${nextOrderId++}",
                    customerId = cust.id,
                    recipeId = recipe.id,
                    recipeName = recipe.name,
                    tableId = cust.assignedTableId!!,
                    prepRemainingSeconds = recipe.prepTimeSeconds
                  )
                )
              }
            }
          }
        }

        CustomerState.SEATED_WAITING -> {
          val patienceLossRate = if (hasLofiMusic) 1.8f else 3.5f
          cust.patience -= dt * patienceLossRate
          if (cust.patience <= 0f) {
            cust.thoughtBubble = "Đợi lâu quá, về đây!"
            cust.thoughtDuration = 2f
            cust.satisfactionStars = 2
            cust.assignedTableId = null
            cust.state = CustomerState.LEAVING
            cust.targetX = 3.5f
            cust.targetY = 9.0f
          }
        }

        CustomerState.CONSUMING -> {
          cust.consumeProgress += dt * 0.35f
          if (cust.consumeProgress >= 1.0f) {
            cust.state = CustomerState.FINISHED
          }
        }

        CustomerState.FINISHED -> {
          val recipe = recipes.find { it.id == cust.desiredRecipeId }
          val bill = recipe?.sellingPrice ?: 25000.0
          var tip = if (cust.satisfactionStars >= 4) Random.nextDouble(5000.0, 15000.0) else 0.0
          if (hasWifi) tip *= 1.25

          onCustomerPaid(bill, tip, cust.satisfactionStars)

          if (cust.assignedTableId != null) {
            val table = furniture.find { it.id == cust.assignedTableId }
            table?.isDirty = true
            cust.assignedTableId = null
          }

          cust.thoughtBubble = if (cust.isTakeaway) "🥤 Cảm ơn! +${formatVnd(bill + tip)}" else "❤️ 5 Sao! +${formatVnd(bill + tip)}"
          cust.thoughtDuration = 2.0f
          cust.state = CustomerState.LEAVING
          cust.targetX = 3.5f
          cust.targetY = 9.0f
        }

        CustomerState.LEAVING -> {
          moveEntityTowards(cust, cust.targetX, cust.targetY, 1.4f * dt)
          if (hypot(cust.posX - cust.targetX, cust.posY - cust.targetY) < 0.3f) {
            cust.state = CustomerState.WALKING_AWAY
            cust.targetX = if (Random.nextBoolean()) -3.5f else 11.5f
          }
        }

        CustomerState.WALKING_AWAY -> {
          moveEntityTowards(cust, cust.targetX, cust.targetY, 1.3f * dt)
          if (cust.posX < -3f || cust.posX > 11f) {
            toRemove.add(cust)
          }
        }
      }
    }

    customers.removeAll(toRemove)
  }

  private fun updateStaffStateMachine(
    staffList: MutableList<StaffMember>,
    furniture: MutableList<FurnitureItem>,
    tickets: MutableList<OrderTicket>,
    customers: List<Customer>,
    hasProEspresso: Boolean,
    hasFastDishwasher: Boolean,
    dt: Float
  ) {
    val counter = furniture.find { it.type == ItemType.ORDER_COUNTER }

    for (staff in staffList) {
      when (staff.state) {
        StaffState.IDLE -> {
          val dirtyTable = furniture.find { it.isTable && it.isDirty }
          if (dirtyTable != null && (staff.role == StaffRole.WAITER_CLEANER || staff.role == StaffRole.ALL_ROUNDER)) {
            staff.targetTableId = dirtyTable.id
            staff.targetX = dirtyTable.gridX + 0.5f
            staff.targetY = dirtyTable.gridY + 0.5f
            staff.state = StaffState.WALKING_TO_DIRTY_TABLE
            continue
          }

          val readyTicket = tickets.find { it.isPrepared && it.isAssignedToStaffId == null }
          if (readyTicket != null && (staff.role == StaffRole.WAITER_CLEANER || staff.role == StaffRole.ALL_ROUNDER)) {
            readyTicket.isAssignedToStaffId = staff.id
            staff.assignedOrderId = readyTicket.id
            staff.carryingDrinkName = readyTicket.recipeName
            staff.state = StaffState.CARRYING_ORDER
            val targetTable = furniture.find { it.id == readyTicket.tableId }
            if (targetTable != null) {
              staff.targetX = targetTable.gridX + 0.5f
              staff.targetY = targetTable.gridY + 0.5f
            } else {
              // Takeaway pickup area at counter
              staff.targetX = (counter?.gridX?.toFloat() ?: 3.5f) + 1.2f
              staff.targetY = (counter?.gridY?.toFloat() ?: 4.5f) + 1.0f
            }
            continue
          }

          val pendingTicket = tickets.find { !it.isPrepared && it.isAssignedToStaffId == null }
          if (pendingTicket != null && (staff.role == StaffRole.BARISTA || staff.role == StaffRole.ALL_ROUNDER)) {
            pendingTicket.isAssignedToStaffId = staff.id
            staff.assignedOrderId = pendingTicket.id
            val espresso = furniture.find { it.type == ItemType.ESPRESSO_MACHINE }
            val station = espresso ?: counter
            if (station != null) {
              staff.targetX = station.gridX + 0.5f
              staff.targetY = station.gridY - 0.5f
              staff.state = StaffState.WALKING_TO_STATION
            }
            continue
          }

          staff.targetX = if (staff.role == StaffRole.BARISTA) 4f else 2.5f
          staff.targetY = if (staff.role == StaffRole.BARISTA) 2.5f else 4.5f
          moveStaffTowards(staff, staff.targetX, staff.targetY, staff.moveSpeed * dt)
        }

        StaffState.WALKING_TO_STATION -> {
          moveStaffTowards(staff, staff.targetX, staff.targetY, staff.moveSpeed * dt)
          if (hypot(staff.posX - staff.targetX, staff.posY - staff.targetY) < 0.25f) {
            staff.state = StaffState.BREWING
            staff.actionProgress = 0f
            val baseDuration = 2.5f / staff.brewSpeedMultiplier
            staff.actionDuration = if (hasProEspresso) baseDuration * 0.5f else baseDuration
          }
        }

        StaffState.BREWING -> {
          staff.actionProgress += dt
          if (staff.actionProgress >= staff.actionDuration) {
            val ticket = tickets.find { it.id == staff.assignedOrderId }
            ticket?.isPrepared = true
            ticket?.isAssignedToStaffId = null
            staff.assignedOrderId = null
            staff.state = StaffState.IDLE
          }
        }

        StaffState.CARRYING_ORDER -> {
          moveStaffTowards(staff, staff.targetX, staff.targetY, staff.moveSpeed * dt)
          if (hypot(staff.posX - staff.targetX, staff.posY - staff.targetY) < 0.35f) {
            val ticket = tickets.find { it.id == staff.assignedOrderId }
            if (ticket != null) {
              val customer = customers.find { it.id == ticket.customerId }
              if (customer != null) {
                if (customer.isTakeaway) {
                  customer.state = CustomerState.FINISHED
                } else {
                  customer.state = CustomerState.CONSUMING
                }
              }
              tickets.remove(ticket)
            }
            staff.assignedOrderId = null
            staff.carryingDrinkName = null
            staff.state = StaffState.IDLE
          }
        }

        StaffState.WALKING_TO_DIRTY_TABLE -> {
          moveStaffTowards(staff, staff.targetX, staff.targetY, staff.moveSpeed * dt)
          if (hypot(staff.posX - staff.targetX, staff.posY - staff.targetY) < 0.35f) {
            staff.state = StaffState.CLEANING_TABLE
            staff.actionProgress = 0f
            staff.actionDuration = if (hasFastDishwasher) 1.0f else 2.0f
          }
        }

        StaffState.CLEANING_TABLE -> {
          staff.actionProgress += dt
          if (staff.actionProgress >= staff.actionDuration) {
            val table = furniture.find { it.id == staff.targetTableId }
            table?.isDirty = false
            staff.targetTableId = null
            staff.state = StaffState.IDLE
          }
        }

        else -> {
          staff.state = StaffState.IDLE
        }
      }
    }
  }

  private fun moveEntityTowards(cust: Customer, tx: Float, ty: Float, step: Float) {
    val dx = tx - cust.posX
    val dy = ty - cust.posY
    val dist = hypot(dx, dy)
    if (dist > step) {
      cust.posX += (dx / dist) * step
      cust.posY += (dy / dist) * step
      if (dx > 0) cust.directionX = 1f else if (dx < 0) cust.directionX = -1f
    } else {
      cust.posX = tx
      cust.posY = ty
    }
  }

  private fun moveStaffTowards(staff: StaffMember, tx: Float, ty: Float, step: Float) {
    val dx = tx - staff.posX
    val dy = ty - staff.posY
    val dist = hypot(dx, dy)
    if (dist > step) {
      staff.posX += (dx / dist) * step
      staff.posY += (dy / dist) * step
    } else {
      staff.posX = tx
      staff.posY = ty
    }
  }

  // --- UI Action Handlers ---
  fun setGameSpeed(speed: Float) {
    _uiState.update { it.copy(gameSpeed = speed) }
  }

  fun openDialog(dialog: ActiveDialog?) {
    _uiState.update { it.copy(activeDialog = dialog) }
  }

  fun dismissDailyReport() {
    _uiState.update {
      val nextDay = it.day + 1
      val nextWeather = WeatherType.values().random()
      it.copy(
        showDailyReportDialog = false,
        day = nextDay,
        inGameMinutes = 420,
        weather = nextWeather,
        gameSpeed = 1.0f,
        currentDailyFinance = DailyFinancialRecord(dayNumber = nextDay)
      )
    }
    autoSave()
  }

  // --- Nâng Cấp Quán (Upgrades) ---
  fun buyUpgrade(upgrade: CafeUpgrade) {
    if (_uiState.value.cash < upgrade.costVnd) {
      showToast("Không đủ tiền nâng cấp ${upgrade.name}!")
      return
    }

    _uiState.update { s ->
      val updatedUpgrades = s.upgrades.map { u ->
        if (u.id == upgrade.id) u.copy(isUnlocked = true) else u
      }
      s.currentDailyFinance.upgradeExpense += upgrade.costVnd
      s.copy(
        cash = s.cash - upgrade.costVnd,
        upgrades = updatedUpgrades
      )
    }
    showToast("Đã mở khóa: ${upgrade.name}!")
    autoSave()
  }

  // --- Build & Placement Mode ---
  fun startPlacingCatalogItem(type: ItemType) {
    if (_uiState.value.cash < type.cost) {
      showToast("Không đủ tiền mua ${type.displayName}!")
      return
    }
    _uiState.update {
      it.copy(
        isBuildMode = true,
        selectedCatalogItem = type,
        activeDialog = null,
        hoveredGridX = 3,
        hoveredGridY = 3,
        isValidPlacement = checkPlacementValid(3, 3, type, null)
      )
    }
  }

  fun onCanvasGridHover(gx: Int, gy: Int) {
    if (!_uiState.value.isBuildMode) return
    val type = _uiState.value.selectedCatalogItem
    val movingItem = _uiState.value.selectedFurnitureToMove
    val itemType = type ?: movingItem?.type
    if (itemType != null) {
      val valid = checkPlacementValid(gx, gy, itemType, movingItem?.id)
      _uiState.update {
        it.copy(hoveredGridX = gx, hoveredGridY = gy, isValidPlacement = valid)
      }
    }
  }

  fun confirmPlacementAt(gx: Int, gy: Int) {
    val s = _uiState.value
    if (!s.isBuildMode) return

    val type = s.selectedCatalogItem
    val movingItem = s.selectedFurnitureToMove

    if (type != null) {
      if (s.cash < type.cost) {
        showToast("Không đủ tiền mua!")
        return
      }
      if (!checkPlacementValid(gx, gy, type, null)) {
        showToast("Vị trí không hợp lệ hoặc đã có đồ!")
        return
      }

      val newItem = FurnitureItem(
        id = "furn_${System.currentTimeMillis()}",
        type = type,
        gridX = gx,
        gridY = gy
      )

      val newFurnList = s.furnitureList + newItem
      val newCash = s.cash - type.cost

      _uiState.update {
        it.copy(
          cash = newCash,
          furnitureList = newFurnList,
          isBuildMode = false,
          selectedCatalogItem = null,
          hoveredGridX = null,
          hoveredGridY = null
        )
      }
      showToast("Đã đặt ${type.displayName}!")
      autoSave()
    } else if (movingItem != null) {
      if (!checkPlacementValid(gx, gy, movingItem.type, movingItem.id)) {
        showToast("Vị trí bị chặn!")
        return
      }
      val updatedList = s.furnitureList.map {
        if (it.id == movingItem.id) it.copy(gridX = gx, gridY = gy) else it
      }
      _uiState.update {
        it.copy(
          furnitureList = updatedList,
          isBuildMode = false,
          selectedFurnitureToMove = null,
          hoveredGridX = null,
          hoveredGridY = null
        )
      }
      showToast("Đã di chuyển ${movingItem.type.displayName}!")
      autoSave()
    }
  }

  fun cancelBuildMode() {
    _uiState.update {
      it.copy(
        isBuildMode = false,
        selectedCatalogItem = null,
        selectedFurnitureToMove = null,
        hoveredGridX = null,
        hoveredGridY = null
      )
    }
  }

  fun sellFurniture(item: FurnitureItem) {
    val refund = item.type.cost * 0.70
    _uiState.update {
      it.copy(
        cash = it.cash + refund,
        furnitureList = it.furnitureList.filter { f -> f.id != item.id },
        isBuildMode = false,
        selectedFurnitureToMove = null
      )
    }
    showToast("Đã bán ${item.type.displayName} (+${formatVnd(refund)})!")
    autoSave()
  }

  private fun checkPlacementValid(gx: Int, gy: Int, type: ItemType, ignoreItemId: String?): Boolean {
    if (gx < 0 || gx >= 8 || gy < 0 || gy >= 8) return false
    if ((gx == 3 || gx == 4) && gy == 7) return false
    val occupied = _uiState.value.furnitureList.any { f ->
      f.id != ignoreItemId && f.gridX == gx && f.gridY == gy
    }
    return !occupied
  }

  // --- Inventory Restock ---
  fun buyIngredientPack(ingredient: IngredientType) {
    val cost = ingredient.packCost
    if (_uiState.value.cash < cost) {
      showToast("Không đủ tiền nhập ${ingredient.displayName}!")
      return
    }
    _uiState.update { s ->
      val cur = s.inventory[ingredient] ?: 0
      val newInv = s.inventory.toMutableMap()
      newInv[ingredient] = cur + ingredient.packSize
      s.currentDailyFinance.ingredientExpense += cost
      s.copy(
        cash = s.cash - cost,
        inventory = newInv
      )
    }
    showToast("Đã nhập thêm ${ingredient.packSize} ${ingredient.unit} ${ingredient.displayName}!")
    autoSave()
  }

  fun bulkRestockAll() {
    var totalCost = 0.0
    val packsToBuy = mutableMapOf<IngredientType, Int>()
    for (ing in IngredientType.values()) {
      val cur = _uiState.value.inventory[ing] ?: 0
      if (cur < ing.packSize) {
        totalCost += ing.packCost
        packsToBuy[ing] = ing.packSize
      }
    }

    if (packsToBuy.isEmpty()) {
      showToast("Kho nguyên liệu vẫn còn dồi dào!")
      return
    }

    if (_uiState.value.cash < totalCost) {
      showToast("Không đủ tiền nhập kho toàn bộ (Cần ${formatVnd(totalCost)})!")
      return
    }

    _uiState.update { s ->
      val newInv = s.inventory.toMutableMap()
      packsToBuy.forEach { (ing, qty) ->
        newInv[ing] = (newInv[ing] ?: 0) + qty
      }
      s.currentDailyFinance.ingredientExpense += totalCost
      s.copy(
        cash = s.cash - totalCost,
        inventory = newInv
      )
    }
    showToast("Đã nhập kho tổng hợp (-${formatVnd(totalCost)})!")
    autoSave()
  }

  // --- Menu & Price Adjustment ---
  fun updateRecipePrice(recipeId: String, newPrice: Double) {
    _uiState.update { s ->
      val updated = s.recipes.map { r ->
        if (r.id == recipeId) r.copy(sellingPrice = newPrice.coerceAtLeast(1000.0)) else r
      }
      s.copy(recipes = updated)
    }
    autoSave()
  }

  // --- Staff Management ---
  fun upgradeStaff(staffId: String, attribute: String) {
    val upgradeCost = 500000.0 // 500.000 VNĐ
    if (_uiState.value.cash < upgradeCost) {
      showToast("Cần ${formatVnd(upgradeCost)} để nâng cấp kỹ năng!")
      return
    }

    _uiState.update { s ->
      val updatedStaff = s.staffList.map { staff ->
        if (staff.id == staffId) {
          when (attribute) {
            "SPEED" -> staff.copy(speedLevel = staff.speedLevel + 1)
            "BREW" -> staff.copy(brewSkillLevel = staff.brewSkillLevel + 1)
            "FRIENDLY" -> staff.copy(friendlinessLevel = staff.friendlinessLevel + 1)
            else -> staff
          }
        } else staff
      }
      s.copy(cash = s.cash - upgradeCost, staffList = updatedStaff)
    }
    showToast("Nâng cấp nhân viên thành công!")
    autoSave()
  }

  fun hireStaff(role: StaffRole) {
    val hireCost = 800000.0 // 800.000 VNĐ
    if (_uiState.value.cash < hireCost) {
      showToast("Không đủ tiền tuyển nhân viên mới (Cần ${formatVnd(hireCost)})!")
      return
    }

    val names = listOf("Trang", "Tuấn", "Nam", "Khánh", "Ngân", "Hoàng", "Vy")
    val newStaff = StaffMember(
      id = "staff_${System.currentTimeMillis()}",
      name = "${names.random()} ${role.title}",
      role = role,
      dailyWage = role.baseSalary,
      posX = 3.5f,
      posY = 3.5f,
      targetX = 3.5f,
      targetY = 3.5f
    )

    _uiState.update {
      it.copy(
        cash = it.cash - hireCost,
        staffList = it.staffList + newStaff
      )
    }
    showToast("Đã tuyển dụng ${newStaff.name}!")
    autoSave()
  }

  fun cleanTable(tableId: String) {
    var wasDirty = false
    _uiState.update { s ->
      val updated = s.furnitureList.map { f ->
        if (f.id == tableId && f.isDirty) {
          wasDirty = true
          f.copy(isDirty = false)
        } else f
      }
      if (wasDirty) {
        s.copy(furnitureList = updated, cash = s.cash + 1000.0)
      } else s
    }
    if (wasDirty) {
      showToast("✨ Đã lau sạch bàn sáng bóng! (+1.000đ tiền boa)")
      autoSave()
    }
  }

  fun showToast(msg: String) {
    _uiState.update { it.copy(toastMessage = msg) }
    viewModelScope.launch {
      delay(2500)
      _uiState.update { if (it.toastMessage == msg) it.copy(toastMessage = null) else it }
    }
  }
}
