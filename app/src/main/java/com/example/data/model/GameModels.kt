package com.example.data.model

fun formatVnd(amount: Double): String {
  val longVal = amount.toLong()
  return String.format("%,d ₫", longVal).replace(',', '.')
}

fun formatVndCompact(amount: Double): String {
  val longVal = amount.toLong()
  return when {
    longVal >= 1_000_000 -> String.format("%.1f Tr ₫", amount / 1_000_000.0)
    longVal >= 1_000 -> String.format("%d k ₫", longVal / 1000)
    else -> "$longVal ₫"
  }
}

enum class ItemCategory(val displayName: String, val iconEmoji: String) {
  STATIONS("Máy móc & Quầy", "☕"),
  SEATING("Bàn ghế", "🪑"),
  DECOR("Trang trí", "🪴")
}

enum class ItemType(
  val displayName: String,
  val category: ItemCategory,
  val iconEmoji: String,
  val cost: Double, // in VNĐ
  val decorBonus: Int,
  val comfortBonus: Int,
  val width: Int = 1,
  val height: Int = 1,
  val description: String
) {
  ORDER_COUNTER("Quầy Thu Ngân", ItemCategory.STATIONS, "🛎️", 2500000.0, 5, 0, 1, 1, "Nơi khách hàng xếp hàng để gọi món"),
  ESPRESSO_MACHINE("Máy Espresso Cao Cấp", ItemCategory.STATIONS, "☕", 6500000.0, 15, 0, 1, 1, "Pha chế Espresso, Latte, Cappuccino thơm lừng"),
  PASTRY_DISPLAY("Tủ Bánh Ngọt", ItemCategory.STATIONS, "🥐", 3200000.0, 10, 0, 1, 1, "Bảo quản & trưng bày Croissant, bánh nướng tươi ngon"),
  BEVERAGE_COOLER("Tủ Mát Nước Đóng Chai", ItemCategory.STATIONS, "🧊", 2800000.0, 8, 0, 1, 1, "Phục vụ nước khoáng mát lạnh tức thì"),
  FRIDGE("Tủ Lạnh Nguyên Liệu", ItemCategory.STATIONS, "🥛", 3500000.0, 6, 0, 1, 1, "Giữ sữa tươi và trái cây luôn tươi mới"),

  TABLE_ROUND_2P("Bàn Gỗ Tròn 2 Chỗ", ItemCategory.SEATING, "🪑", 1200000.0, 4, 15, 1, 1, "Bàn gỗ sồi ấm áp dành cho cặp đôi hoặc khách lẻ"),
  TABLE_SOFA_4P("Bàn Sofa Êm Ái 4 Chỗ", ItemCategory.SEATING, "🛋️", 3200000.0, 12, 35, 1, 1, "Sofa nỉ êm ái dành cho nhóm bạn, khách ngồi lâu hơn"),
  BAR_STOOL_TABLE("Bàn Cao Quầy Bar", ItemCategory.SEATING, "🍸", 1500000.0, 8, 18, 1, 1, "Phong cách hiện đại, phục vụ nhanh"),

  PLANT_MONSTERA("Chậu Cây Monstera", ItemCategory.DECOR, "🪴", 450000.0, 14, 0, 1, 1, "Tạo không gian xanh mát tự nhiên, tăng tỉ lệ khách vào"),
  VINTAGE_LAMP("Đèn Cây Vintage", ItemCategory.DECOR, "💡", 750000.0, 18, 0, 1, 1, "Ánh sáng vàng ấm áp lan tỏa, tăng điểm đánh giá quán"),
  ACOUSTIC_SPEAKER("Loa Nhạc Acoustic", ItemCategory.DECOR, "🎵", 1600000.0, 25, 0, 1, 1, "Giai điệu thư giãn giúp khách hàng hào phóng tip thêm"),
  PERSIAN_RUG("Thảm Bohemian Họa Tiết", ItemCategory.DECOR, "🧶", 950000.0, 16, 0, 1, 1, "Điểm nhấn thẩm mỹ sàn nhà ấm cúng")
}

data class FurnitureItem(
  val id: String,
  val type: ItemType,
  var gridX: Int,
  var gridY: Int,
  var rotation: Int = 0,
  var isDirty: Boolean = false,
  var dirtyCleanProgress: Float = 0f
) {
  val decorBonus: Int get() = type.decorBonus
  val comfortBonus: Int get() = type.comfortBonus
  val isTable: Boolean get() = type == ItemType.TABLE_ROUND_2P || type == ItemType.TABLE_SOFA_4P || type == ItemType.BAR_STOOL_TABLE
  val maxSeats: Int get() = when (type) {
    ItemType.TABLE_SOFA_4P -> 4
    ItemType.TABLE_ROUND_2P -> 2
    ItemType.BAR_STOOL_TABLE -> 2
    else -> 0
  }
}

enum class IngredientType(
  val displayName: String,
  val unit: String,
  val unitCost: Double, // VNĐ
  val iconEmoji: String,
  val packSize: Int,
  val packCost: Double // VNĐ
) {
  COFFEE_BEANS("Hạt Cà Phê Moka", "g", 380.0, "🫘", 250, 95000.0),
  FRESH_MILK("Sữa Tươi Thanh Trùng", "ml", 90.0, "🥛", 500, 45000.0),
  TEA_LEAVES("Trà Đào / Oolong", "g", 430.0, "🍃", 150, 65000.0),
  FRESH_FRUITS("Trái Cây Tươi", "phần", 4000.0, "🍊", 20, 80000.0),
  BOTTLED_WATER("Nước Khoáng Đóng Chai", "chai", 3500.0, "💧", 24, 85000.0),
  PASTRY_CROISSANT("Bánh Sừng Bò", "cái", 12000.0, "🥐", 15, 180000.0),
  SUGAR_SYRUP("Đường & Syrup", "ml", 100.0, "🍯", 300, 30000.0)
}

data class Recipe(
  val id: String,
  val name: String,
  val categoryName: String,
  val description: String,
  val iconEmoji: String,
  val prepTimeSeconds: Float,
  val ingredients: Map<IngredientType, Int>,
  val baseCost: Double,   // in VNĐ
  var sellingPrice: Double, // in VNĐ
  val popularityBase: Float,
  val requiresStation: ItemType?
) {
  val profit: Double get() = sellingPrice - baseCost
  val profitMarginPercent: Int get() = if (sellingPrice > 0) ((profit / sellingPrice) * 100).toInt() else 0

  val priceAttractiveness: PriceEvaluation get() {
    val ratio = sellingPrice / baseCost
    return when {
      ratio < 1.6 -> PriceEvaluation.VERY_CHEAP
      ratio <= 2.8 -> PriceEvaluation.FAIR
      ratio <= 4.0 -> PriceEvaluation.PRICEY
      else -> PriceEvaluation.EXPENSIVE
    }
  }
}

enum class PriceEvaluation(val label: String, val ratingModifier: Int, val demandMultiplier: Float) {
  VERY_CHEAP("Rất rẻ (Khách thích)", 1, 1.35f),
  FAIR("Giá hợp lý (Chuẩn)", 0, 1.0f),
  PRICEY("Hơi đắt (Giảm khách)", -1, 0.75f),
  EXPENSIVE("Cắt cổ (Khách phàn nàn)", -2, 0.45f)
}

// ==========================================
// HỆ THỐNG NÂNG CẤP (UPGRADE SYSTEM)
// ==========================================
enum class UpgradeCategory(val displayName: String, val iconEmoji: String) {
  MARKETING("Quảng Cáo & Marketing", "📢"),
  EQUIPMENT("Máy Móc & Năng Suất", "⚙️"),
  FACILITY("Tiện Ích & Dịch Vụ", "✨")
}

data class CafeUpgrade(
  val id: String,
  val name: String,
  val category: UpgradeCategory,
  val iconEmoji: String,
  val description: String,
  val costVnd: Double,
  var isUnlocked: Boolean = false,
  val effectLabel: String
)

enum class StaffRole(val title: String, val description: String, val baseSalary: Double) {
  BARISTA("Pha Chế Viên", "Chuyên vận hành máy pha và chuẩn bị đồ uống", 350000.0),
  WAITER_CLEANER("Phục Vụ & Dọn Dẹp", "Đón khách, bưng món và dọn dẹp bàn bẩn", 250000.0),
  ALL_ROUNDER("Nhân Viên Toàn Năng", "Linh hoạt hỗ trợ tất cả các công việc trong quán", 400000.0)
}

enum class StaffState {
  IDLE,
  WALKING_TO_COUNTER,
  TAKING_ORDER,
  WALKING_TO_STATION,
  BREWING,
  CARRYING_ORDER,
  SERVING_TABLE,
  WALKING_TO_DIRTY_TABLE,
  CLEANING_TABLE
}

data class StaffMember(
  val id: String,
  val name: String,
  val role: StaffRole,
  var speedLevel: Int = 1,
  var brewSkillLevel: Int = 1,
  var friendlinessLevel: Int = 1,
  var dailyWage: Double = 300000.0,
  var posX: Float = 4f,
  var posY: Float = 4f,
  var targetX: Float = 4f,
  var targetY: Float = 4f,
  var state: StaffState = StaffState.IDLE,
  var actionProgress: Float = 0f,
  var actionDuration: Float = 1f,
  var assignedOrderId: String? = null,
  var carryingDrinkName: String? = null,
  var targetTableId: String? = null,
  val avatarEmoji: String = "🧑‍🍳",
  val shirtColorHex: Long = 0xFF5D4037
) {
  val moveSpeed: Float get() = 1.4f + (speedLevel - 1) * 0.35f
  val brewSpeedMultiplier: Float get() = 1.0f + (brewSkillLevel - 1) * 0.3f
  val tipBonusPercent: Float get() = (friendlinessLevel - 1) * 0.08f
}

enum class CustomerState {
  WALKING_SIDEWALK,
  ENTERING_CAFE,
  QUEUING_AT_COUNTER,
  ORDERING,
  FINDING_SEAT,
  SEATED_WAITING,
  CONSUMING,
  FINISHED,
  LEAVING,
  WALKING_AWAY
}

data class Customer(
  val id: String,
  val name: String,
  var posX: Float,
  var posY: Float,
  var targetX: Float,
  var targetY: Float,
  var directionX: Float = 1f,
  var state: CustomerState = CustomerState.WALKING_SIDEWALK,
  var desiredRecipeId: String? = null,
  var assignedTableId: String? = null,
  var patience: Float = 100f,
  var maxPatience: Float = 100f,
  var consumeProgress: Float = 0f,
  var thoughtBubble: String? = null,
  var thoughtDuration: Float = 0f,
  var satisfactionStars: Int = 5,
  var tipGiven: Double = 0.0,
  var billPaid: Double = 0.0,
  val skinToneHex: Long = 0xFFFFD180,
  val shirtColorHex: Long = 0xFF42A5F5,
  val umbrellaColorHex: Long? = null,
  val isChildOrElderly: Boolean = false,
  var hasEvaluatedEntry: Boolean = false,
  var isTakeaway: Boolean = false
)

enum class WeatherType(
  val displayName: String,
  val iconEmoji: String,
  val description: String,
  val attractionModifier: Float,
  val favoredDrinkCategory: String
) {
  SUNNY("Nắng Ráo Thoáng Mát", "☀️", "Thời tiết mát mẻ, đông người dạo phố", 1.15f, "COFFEE"),
  RAINY("Mưa Rào Lất Phất", "🌧️", "Khách cầm dù, muốn tìm góc trú ấm và nhâm nhi", 0.95f, "HOT_LATTE"),
  HEATWAVE("Nắng Nóng Oi Ả", "🔥", "Khát nước cao độ! Nước mát & trà trái cây bán cực chạy", 1.25f, "COLD_DRINK"),
  COZY_BREEZE("Gió Thu Dịu Nhẹ", "🍂", "Khách thích ngồi lâu đọc sách, gọi thêm bánh ngọt", 1.10f, "PASTRY")
}

data class OrderTicket(
  val id: String,
  val customerId: String,
  val recipeId: String,
  val recipeName: String,
  val tableId: String,
  var isPrepared: Boolean = false,
  var isAssignedToStaffId: String? = null,
  var prepRemainingSeconds: Float = 0f
)

data class DailyFinancialRecord(
  var dayNumber: Int,
  var grossRevenue: Double = 0.0,
  var ingredientExpense: Double = 0.0,
  var staffWages: Double = 0.0,
  var furnitureExpense: Double = 0.0,
  var upgradeExpense: Double = 0.0,
  var cupsSold: Int = 0,
  var averageStars: Float = 5.0f,
  var customerCount: Int = 0
) {
  val totalExpense: Double get() = ingredientExpense + staffWages + furnitureExpense + upgradeExpense
  val netProfit: Double get() = grossRevenue - totalExpense
}
