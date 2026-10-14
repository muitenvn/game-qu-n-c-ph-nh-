package com.example.data.repository

import android.content.Context
import com.example.data.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class LoadedGameState(
  val cash: Double,
  val day: Int,
  val inGameMinutes: Int,
  val cafeName: String,
  val cafeStars: Float,
  val totalCupsSold: Int,
  val totalRevenue: Double,
  val furniture: List<FurnitureItem>,
  val inventory: Map<IngredientType, Int>,
  val customPrices: Map<String, Double>,
  val staff: List<StaffMember>,
  val unlockedUpgradeIds: Set<String>
)

object CafeSaveManager {
  private const val SAVE_FILE_NAME = "cozy_cafe_save.json"

  fun getDefaultUpgrades(): List<CafeUpgrade> {
    return listOf(
      CafeUpgrade(
        id = "upgrade_flyers",
        name = "Phát Tờ Rơi Ngã Tư",
        category = UpgradeCategory.MARKETING,
        iconEmoji = "📄",
        description = "Tiếp cận cư dân xung quanh khu phố vào mỗi buổi sáng.",
        costVnd = 500000.0,
        effectLabel = "+10% khách vỉa hè rẽ vào quán"
      ),
      CafeUpgrade(
        id = "upgrade_social",
        name = "Quảng Cáo TikTok & Review Ẩm Thực",
        category = UpgradeCategory.MARKETING,
        iconEmoji = "📱",
        description = "Mời các food reviewer nổi tiếng đến check-in và đăng video triệu view.",
        costVnd = 2000000.0,
        effectLabel = "+20% tỉ lệ thu hút khách"
      ),
      CafeUpgrade(
        id = "upgrade_neon",
        name = "Biển Hiệu Neon Nghệ Thuật",
        category = UpgradeCategory.MARKETING,
        iconEmoji = "✨",
        description = "Biển hiệu rực rỡ nổi bật nhất con phố cả ngày lẫn đêm.",
        costVnd = 3500000.0,
        effectLabel = "+30 điểm thẩm mỹ trang trí"
      ),
      CafeUpgrade(
        id = "upgrade_pro_espresso",
        name = "Nồi Hơi Kép Máy Espresso Pro",
        category = UpgradeCategory.EQUIPMENT,
        iconEmoji = "☕",
        description = "Áp suất ổn định 9 bar chiết xuất hương vị đỉnh cao, pha nhanh hơn.",
        costVnd = 4500000.0,
        effectLabel = "+50% tốc độ pha chế cho Barista"
      ),
      CafeUpgrade(
        id = "upgrade_auto_grinder",
        name = "Máy Xay Hạt Định Lượng Chuẩn",
        category = UpgradeCategory.EQUIPMENT,
        iconEmoji = "⚙️",
        description = "Cắt giảm hao hụt bột cà phê rơi vãi trong quá trình nén và pha.",
        costVnd = 3000000.0,
        effectLabel = "Tiết kiệm 20% hạt cà phê mỗi ly"
      ),
      CafeUpgrade(
        id = "upgrade_fast_dishwasher",
        name = "Máy Rửa Ly Khử Khuẩn Siêu Tốc",
        category = UpgradeCategory.EQUIPMENT,
        iconEmoji = "🧼",
        description = "Hỗ trợ nhân viên dọn rửa bàn ghế sạch bong chỉ trong chớp mắt.",
        costVnd = 2800000.0,
        effectLabel = "Tăng gấp đôi tốc độ dọn bàn sạch"
      ),
      CafeUpgrade(
        id = "upgrade_wifi",
        name = "Wi-Fi 6 Cáp Quang Tốc Độ Cao",
        category = UpgradeCategory.FACILITY,
        iconEmoji = "📶",
        description = "Mạng internet vù vù phục vụ khách làm việc và sinh viên học bài.",
        costVnd = 1500000.0,
        effectLabel = "+25% tiền tip boa từ khách"
      ),
      CafeUpgrade(
        id = "upgrade_ac",
        name = "Điều Hòa Inverter Mát Lạnh",
        category = UpgradeCategory.FACILITY,
        iconEmoji = "❄️",
        description = "Không gian mát rượi trốn nóng mùa hè hoặc sưởi ấm mùa mưa.",
        costVnd = 5000000.0,
        effectLabel = "+15% khách vào ngày Nắng nóng & Mưa"
      ),
      CafeUpgrade(
        id = "upgrade_premium_beans",
        name = "Hạt Cà Phê Cầu Đất Thượng Hạng",
        category = UpgradeCategory.FACILITY,
        iconEmoji = "🌟",
        description = "Tuyển chọn 100% Arabica Cầu Đất hái chín cây thơm nức tiếng.",
        costVnd = 3800000.0,
        effectLabel = "+0.3 ⭐ điểm đánh giá chất lượng quán"
      ),
      CafeUpgrade(
        id = "upgrade_takeaway",
        name = "Dịch Vụ Takeaway Mang Đi",
        category = UpgradeCategory.FACILITY,
        iconEmoji = "🥤",
        description = "Khách vội hoặc khi quán kín bàn vẫn có thể mua ly mang đi tại quầy, không lo mất khách!",
        costVnd = 1200000.0,
        effectLabel = "Bán được mang đi ngay cả khi hết bàn"
      ),
      CafeUpgrade(
        id = "upgrade_music_lofi",
        name = "Dàn Loa Acoustic Lofi Chill",
        category = UpgradeCategory.FACILITY,
        iconEmoji = "🎵",
        description = "Giai điệu du dương xoa dịu tâm hồn, khách kiên nhẫn hơn khi đợi đồ uống.",
        costVnd = 2200000.0,
        effectLabel = "+40% lòng kiên nhẫn của khách, ít bỏ về"
      ),
      CafeUpgrade(
        id = "upgrade_self_kiosk",
        name = "Kiosk POS Cảm Ứng Tự Động",
        category = UpgradeCategory.EQUIPMENT,
        iconEmoji = "💻",
        description = "Khách tự bấm chọn món và quét mã QR chuyển khoản siêu nhanh tại quầy.",
        costVnd = 4000000.0,
        effectLabel = "Gọi món tức thì, quầy không bao giờ tắc nghẽn"
      ),
      CafeUpgrade(
        id = "upgrade_checkin_corner",
        name = "Góc Check-In Sống Ảo Triệu View",
        category = UpgradeCategory.MARKETING,
        iconEmoji = "📸",
        description = "Gương lượn sóng & đèn hoàng hôn cực nghệ thu hút giới trẻ đến chụp ảnh.",
        costVnd = 3200000.0,
        effectLabel = "+15% khách trẻ tuổi và sinh viên ghé quán"
      ),
      CafeUpgrade(
        id = "upgrade_membership",
        name = "Thẻ Thành Viên Tích Điểm",
        category = UpgradeCategory.MARKETING,
        iconEmoji = "💳",
        description = "Tích điểm đổi quà tặng khiến khách hàng thân thiết luôn quay lại mỗi ngày.",
        costVnd = 1800000.0,
        effectLabel = "+12% tỉ lệ khách quen quay lại"
      )
    )
  }

  fun getDefaultRecipes(): List<Recipe> {
    return listOf(
      Recipe(
        id = "black_coffee",
        name = "Cà Phê Đen Đá",
        categoryName = "Cà phê truyền thống",
        description = "Đậm đà hương vị hạt Moka rang mộc truyền thống, thanh lọc sảng khoái.",
        iconEmoji = "☕",
        prepTimeSeconds = 3.0f,
        ingredients = mapOf(IngredientType.COFFEE_BEANS to 18, IngredientType.SUGAR_SYRUP to 10),
        baseCost = 7800.0,
        sellingPrice = 22000.0,
        popularityBase = 1.0f,
        requiresStation = null
      ),
      Recipe(
        id = "milk_coffee",
        name = "Cà Phê Sữa Đá (Nâu)",
        categoryName = "Cà phê truyền thống",
        description = "Hương thơm nồng nàn quyện cùng vị ngọt béo ngậy khó cưỡng.",
        iconEmoji = "🧋",
        prepTimeSeconds = 3.5f,
        ingredients = mapOf(IngredientType.COFFEE_BEANS to 18, IngredientType.FRESH_MILK to 60, IngredientType.SUGAR_SYRUP to 10),
        baseCost = 13200.0,
        sellingPrice = 28000.0,
        popularityBase = 1.25f,
        requiresStation = null
      ),
      Recipe(
        id = "caffe_latte",
        name = "Caffè Latte Nghệ Thuật",
        categoryName = "Máy Pha Espresso",
        description = "Lớp bọt sữa tạo hình trái tim mịn màng, hương Espresso thơm nồng chuẩn Ý.",
        iconEmoji = "☕",
        prepTimeSeconds = 5.0f,
        ingredients = mapOf(IngredientType.COFFEE_BEANS to 20, IngredientType.FRESH_MILK to 150),
        baseCost = 21100.0,
        sellingPrice = 48000.0,
        popularityBase = 1.30f,
        requiresStation = ItemType.ESPRESSO_MACHINE
      ),
      Recipe(
        id = "tropical_fruit_tea",
        name = "Trà Đào Cam Sả Nhiệt Đới",
        categoryName = "Trà & Trái Cây",
        description = "Thanh mát vị trà tự nhiên kèm lát cam tươi và đào mọng nước ngọt lành.",
        iconEmoji = "🍹",
        prepTimeSeconds = 3.2f,
        ingredients = mapOf(IngredientType.TEA_LEAVES to 10, IngredientType.FRESH_FRUITS to 1, IngredientType.SUGAR_SYRUP to 15),
        baseCost = 12300.0,
        sellingPrice = 39000.0,
        popularityBase = 1.15f,
        requiresStation = null
      ),
      Recipe(
        id = "bottled_water",
        name = "Nước Khoáng Đóng Chai",
        categoryName = "Nước Đóng Chai",
        description = "Mát lạnh tức thì, lấy ngay từ tủ mát mà không mất thời gian pha chế.",
        iconEmoji = "💧",
        prepTimeSeconds = 0.8f,
        ingredients = mapOf(IngredientType.BOTTLED_WATER to 1),
        baseCost = 3500.0,
        sellingPrice = 15000.0,
        popularityBase = 0.85f,
        requiresStation = ItemType.BEVERAGE_COOLER
      ),
      Recipe(
        id = "butter_croissant",
        name = "Bánh Sừng Bò Bơ Pháp",
        categoryName = "Bánh Ngọt",
        description = "Vỏ ngàn lớp giòn rụm thơm lừng bơ béo, hoàn hảo khi nhâm nhi cùng cà phê.",
        iconEmoji = "🥐",
        prepTimeSeconds = 1.0f,
        ingredients = mapOf(IngredientType.PASTRY_CROISSANT to 1),
        baseCost = 12000.0,
        sellingPrice = 32000.0,
        popularityBase = 1.10f,
        requiresStation = ItemType.PASTRY_DISPLAY
      )
    )
  }

  fun getDefaultFurniture(): List<FurnitureItem> {
    return listOf(
      FurnitureItem("item_counter_1", ItemType.ORDER_COUNTER, gridX = 3, gridY = 3, rotation = 0),
      FurnitureItem("item_espresso_1", ItemType.ESPRESSO_MACHINE, gridX = 4, gridY = 3, rotation = 0),
      FurnitureItem("item_pastry_1", ItemType.PASTRY_DISPLAY, gridX = 2, gridY = 3, rotation = 0),
      FurnitureItem("item_table_1", ItemType.TABLE_ROUND_2P, gridX = 1, gridY = 5, rotation = 0),
      FurnitureItem("item_table_2", ItemType.TABLE_ROUND_2P, gridX = 6, gridY = 5, rotation = 0),
      FurnitureItem("item_table_3", ItemType.TABLE_SOFA_4P, gridX = 5, gridY = 2, rotation = 0),
      FurnitureItem("item_plant_1", ItemType.PLANT_MONSTERA, gridX = 0, gridY = 0, rotation = 0),
      FurnitureItem("item_lamp_1", ItemType.VINTAGE_LAMP, gridX = 7, gridY = 0, rotation = 0)
    )
  }

  fun getDefaultInventory(): Map<IngredientType, Int> {
    return mapOf(
      IngredientType.COFFEE_BEANS to 500,     // 500g
      IngredientType.FRESH_MILK to 1200,      // 1200ml
      IngredientType.TEA_LEAVES to 300,       // 300g
      IngredientType.FRESH_FRUITS to 40,      // 40 phần
      IngredientType.BOTTLED_WATER to 48,     // 48 chai
      IngredientType.PASTRY_CROISSANT to 25,  // 25 cái
      IngredientType.SUGAR_SYRUP to 600       // 600ml
    )
  }

  fun getDefaultStaff(): List<StaffMember> {
    return listOf(
      StaffMember(
        id = "staff_mai",
        name = "Mai Barista",
        role = StaffRole.BARISTA,
        speedLevel = 1,
        brewSkillLevel = 1,
        friendlinessLevel = 1,
        dailyWage = 350000.0,
        posX = 4f,
        posY = 2.5f,
        targetX = 4f,
        targetY = 2.5f,
        avatarEmoji = "👩‍🍳",
        shirtColorHex = 0xFF5D4037
      ),
      StaffMember(
        id = "staff_hung",
        name = "Hùng Phục Vụ",
        role = StaffRole.WAITER_CLEANER,
        speedLevel = 1,
        brewSkillLevel = 1,
        friendlinessLevel = 1,
        dailyWage = 250000.0,
        posX = 2f,
        posY = 4.5f,
        targetX = 2f,
        targetY = 4.5f,
        avatarEmoji = "🧑‍🌾",
        shirtColorHex = 0xFF388E3C
      )
    )
  }

  fun saveGame(
    context: Context,
    cash: Double,
    day: Int,
    inGameMinutes: Int,
    cafeName: String,
    cafeStars: Float,
    totalCupsSold: Int,
    totalRevenue: Double,
    furniture: List<FurnitureItem>,
    inventory: Map<IngredientType, Int>,
    recipes: List<Recipe>,
    staff: List<StaffMember>,
    unlockedUpgrades: Set<String> = emptySet()
  ) {
    try {
      val root = JSONObject()
      root.put("cash", cash)
      root.put("day", day)
      root.put("inGameMinutes", inGameMinutes)
      root.put("cafeName", cafeName)
      root.put("cafeStars", cafeStars.toDouble())
      root.put("totalCupsSold", totalCupsSold)
      root.put("totalRevenue", totalRevenue)

      // Furniture Array
      val furnArray = JSONArray()
      for (f in furniture) {
        val fObj = JSONObject()
        fObj.put("id", f.id)
        fObj.put("type", f.type.name)
        fObj.put("gridX", f.gridX)
        fObj.put("gridY", f.gridY)
        fObj.put("rotation", f.rotation)
        furnArray.put(fObj)
      }
      root.put("furniture", furnArray)

      // Inventory Object
      val invObj = JSONObject()
      for ((type, amount) in inventory) {
        invObj.put(type.name, amount)
      }
      root.put("inventory", invObj)

      // Recipe Custom Prices
      val pricesObj = JSONObject()
      for (r in recipes) {
        pricesObj.put(r.id, r.sellingPrice)
      }
      root.put("prices", pricesObj)

      // Staff Array
      val staffArray = JSONArray()
      for (s in staff) {
        val sObj = JSONObject()
        sObj.put("id", s.id)
        sObj.put("name", s.name)
        sObj.put("role", s.role.name)
        sObj.put("speedLevel", s.speedLevel)
        sObj.put("brewSkillLevel", s.brewSkillLevel)
        sObj.put("friendlinessLevel", s.friendlinessLevel)
        sObj.put("dailyWage", s.dailyWage)
        sObj.put("avatarEmoji", s.avatarEmoji)
        sObj.put("shirtColorHex", s.shirtColorHex)
        staffArray.put(sObj)
      }
      root.put("staff", staffArray)

      // Unlocked Upgrades Array
      val upArray = JSONArray()
      for (uId in unlockedUpgrades) {
        upArray.put(uId)
      }
      root.put("unlockedUpgrades", upArray)

      val file = File(context.filesDir, SAVE_FILE_NAME)
      file.writeText(root.toString())
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  fun loadGame(context: Context): LoadedGameState? {
    try {
      val file = File(context.filesDir, SAVE_FILE_NAME)
      if (!file.exists()) return null

      val jsonStr = file.readText()
      val root = JSONObject(jsonStr)

      // Default cash is 15.000.000 VNĐ
      val cash = root.optDouble("cash", 15000000.0)
      val day = root.optInt("day", 1)
      val inGameMinutes = root.optInt("inGameMinutes", 420)
      val cafeName = root.optString("cafeName", "Tiệm Cà Phê Mây Nhỏ")
      val cafeStars = root.optDouble("cafeStars", 4.8).toFloat()
      val totalCupsSold = root.optInt("totalCupsSold", 0)
      val totalRevenue = root.optDouble("totalRevenue", 0.0)

      // Furniture
      val furnList = mutableListOf<FurnitureItem>()
      val furnArray = root.optJSONArray("furniture")
      if (furnArray != null) {
        for (i in 0 until furnArray.length()) {
          val fObj = furnArray.getJSONObject(i)
          val typeStr = fObj.optString("type")
          val type = runCatching { ItemType.valueOf(typeStr) }.getOrNull()
          if (type != null) {
            furnList.add(
              FurnitureItem(
                id = fObj.optString("id", "furn_$i"),
                type = type,
                gridX = fObj.optInt("gridX", 0),
                gridY = fObj.optInt("gridY", 0),
                rotation = fObj.optInt("rotation", 0)
              )
            )
          }
        }
      }

      // Inventory
      val invMap = mutableMapOf<IngredientType, Int>()
      val invObj = root.optJSONObject("inventory")
      if (invObj != null) {
        for (type in IngredientType.values()) {
          invMap[type] = invObj.optInt(type.name, 100)
        }
      }

      // Prices
      val pricesMap = mutableMapOf<String, Double>()
      val pricesObj = root.optJSONObject("prices")
      if (pricesObj != null) {
        val keys = pricesObj.keys()
        while (keys.hasNext()) {
          val k = keys.next()
          pricesMap[k] = pricesObj.getDouble(k)
        }
      }

      // Staff
      val staffList = mutableListOf<StaffMember>()
      val staffArray = root.optJSONArray("staff")
      if (staffArray != null) {
        for (i in 0 until staffArray.length()) {
          val sObj = staffArray.getJSONObject(i)
          val roleStr = sObj.optString("role")
          val role = runCatching { StaffRole.valueOf(roleStr) }.getOrDefault(StaffRole.WAITER_CLEANER)
          staffList.add(
            StaffMember(
              id = sObj.optString("id", "staff_$i"),
              name = sObj.optString("name", "Nhân Viên"),
              role = role,
              speedLevel = sObj.optInt("speedLevel", 1),
              brewSkillLevel = sObj.optInt("brewSkillLevel", 1),
              friendlinessLevel = sObj.optInt("friendlinessLevel", 1),
              dailyWage = sObj.optDouble("dailyWage", 250000.0),
              avatarEmoji = sObj.optString("avatarEmoji", "🧑‍🍳"),
              shirtColorHex = sObj.optLong("shirtColorHex", 0xFF5D4037)
            )
          )
        }
      }

      // Upgrades
      val upSet = mutableSetOf<String>()
      val upArray = root.optJSONArray("unlockedUpgrades")
      if (upArray != null) {
        for (i in 0 until upArray.length()) {
          upSet.add(upArray.getString(i))
        }
      }

      return LoadedGameState(
        cash = cash,
        day = day,
        inGameMinutes = inGameMinutes,
        cafeName = cafeName,
        cafeStars = cafeStars,
        totalCupsSold = totalCupsSold,
        totalRevenue = totalRevenue,
        furniture = if (furnList.isNotEmpty()) furnList else getDefaultFurniture(),
        inventory = if (invMap.isNotEmpty()) invMap else getDefaultInventory(),
        customPrices = pricesMap,
        staff = if (staffList.isNotEmpty()) staffList else getDefaultStaff(),
        unlockedUpgradeIds = upSet
      )
    } catch (e: Exception) {
      e.printStackTrace()
      return null
    }
  }
}
