package com.example.game.model

enum class QuestCategory(val displayName: String) {
    MAIN("القصة الرئيسية"),
    SIDE("مهمة جانبية"),
    BOUNTY("مطاردة وزعماء"),
    EXPLORATION("استكشاف وألغاز")
}

data class Quest(
    val id: String,
    val title: String,
    val description: String,
    val category: QuestCategory,
    val targetType: String,
    val requiredCount: Int,
    var currentCount: Int = 0,
    val rewardGold: Int,
    val rewardXp: Int,
    val rewardItem: Item? = null,
    var isCompleted: Boolean = false,
    var isClaimed: Boolean = false
)

object QuestCatalog {
    fun createDefaultQuests(): List<Quest> = listOf(
        Quest(
            id = "mq_1_wake",
            title = "صحوة الفارس الضائع",
            description = "تحدث مع الحكيمة فاليري في قرية خشب البلوط لفهم ما حل بالمملكة بعد كسوف الفراغ.",
            category = QuestCategory.MAIN,
            targetType = "talk_valerie",
            requiredCount = 1,
            rewardGold = 50,
            rewardXp = 100,
            rewardItem = ItemCatalog.HEALTH_POTION.copy(quantity = 3)
        ),
        Quest(
            id = "mq_2_wolves",
            title = "تهديد غابة الهمسات",
            description = "اقضِ على 4 من ذئاب الظلال التي تهاجم قوافل القرويين خارج البوابة الشرقية.",
            category = QuestCategory.MAIN,
            targetType = "kill_SHADOW_WOLF",
            requiredCount = 4,
            rewardGold = 120,
            rewardXp = 250,
            rewardItem = ItemCatalog.CHAINMAIL_VEST
        ),
        Quest(
            id = "sq_blacksmith_ore",
            title = "معدن الحداد المفقود",
            description = "اجمع 3 قطع من خام الحديد من مخيم الغوبلن لمساعدة الحداد بروم في صهر أسلحة جديدة.",
            category = QuestCategory.SIDE,
            targetType = "gather_m_iron_ore",
            requiredCount = 3,
            rewardGold = 80,
            rewardXp = 180,
            rewardItem = ItemCatalog.STEEL_BROADSWORD
        ),
        Quest(
            id = "bq_alpha_behemoth",
            title = "مطاردة: الوحش الهائج بلايتكلو",
            description = "تخلص من الوحش الضخم الكامن في أعماق مستنقعات الشوك الأسود وأعد الأمان للمسافرين.",
            category = QuestCategory.BOUNTY,
            targetType = "kill_ALPHA_BEHEMOTH",
            requiredCount = 1,
            rewardGold = 300,
            rewardXp = 600,
            rewardItem = ItemCatalog.RING_VITALITY
        ),
        Quest(
            id = "mq_3_crypt_key",
            title = "مفتاح سراديب الفراغ",
            description = "اعثر على مفتاح السراديب في كهوف الناب الجليدي لفتح بوابة قلعة الفراغ المغلقة.",
            category = QuestCategory.MAIN,
            targetType = "obtain_q_crypt_key",
            requiredCount = 1,
            rewardGold = 250,
            rewardXp = 500,
            rewardItem = ItemCatalog.GREATER_HEALING.copy(quantity = 2)
        ),
        Quest(
            id = "mq_4_defeat_morvath",
            title = "طيف حارس السراديب",
            description = "اهزم حارس السراديب مورفاث واستعد لوح إيثلجارد الحجري.",
            category = QuestCategory.MAIN,
            targetType = "kill_VOIDWRAITH_MORVATH",
            requiredCount = 1,
            rewardGold = 500,
            rewardXp = 1000,
            rewardItem = ItemCatalog.DRAGONSCALE_ARMOR
        ),
        Quest(
            id = "mq_5_final_reckoning",
            title = "مواجهة سيد الظلال مالاكور",
            description = "اخترق حصن الفراغ الأخير واهزم الملك الملعون مالاكور لوضع حد لكسوف الفراغ الأبدي.",
            category = QuestCategory.MAIN,
            targetType = "kill_LORD_MALAKOR",
            requiredCount = 1,
            rewardGold = 1500,
            rewardXp = 3000,
            rewardItem = ItemCatalog.VOIDBRINGER_GREATSWORD
        )
    )
}
