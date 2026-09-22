package com.example.game.model

enum class ItemType(val displayName: String) {
    WEAPON("سلاح"),
    SHIELD("درع يدوي"),
    ARMOR("درع جسم"),
    HELMET("خوذة"),
    BOOTS("أحذية"),
    RING("خاتم سحري"),
    POTION("جرعة"),
    MATERIAL("مادة خام"),
    QUEST_ITEM("عنصر مهمة")
}

enum class ItemRarity(val displayName: String, val colorHex: Long) {
    COMMON("عادي", 0xFFB0B0B0),
    UNCOMMON("جيد", 0xFF4CAF50),
    RARE("نادر", 0xFF2196F3),
    EPIC("ملحمي", 0xFF9C27B0),
    LEGENDARY("أسطوري", 0xFFFFB300)
}

data class Item(
    val id: String,
    val name: String,
    val description: String,
    val type: ItemType,
    val rarity: ItemRarity,
    val attackBonus: Float = 0f,
    val defenseBonus: Float = 0f,
    val hpBonus: Float = 0f,
    val manaBonus: Float = 0f,
    val critBonus: Float = 0f,
    val speedBonus: Float = 0f,
    val sellPrice: Int = 10,
    val buyPrice: Int = 25,
    var quantity: Int = 1,
    val iconId: String = "sword"
) {
    val icon: String get() = when (type) {
        ItemType.WEAPON -> "⚔️"
        ItemType.SHIELD -> "🛡️"
        ItemType.ARMOR -> "🥋"
        ItemType.HELMET -> "🪖"
        ItemType.BOOTS -> "👢"
        ItemType.RING -> "💍"
        ItemType.POTION -> "🧪"
        ItemType.MATERIAL -> "💎"
        ItemType.QUEST_ITEM -> "📜"
    }

    val value: Int get() = buyPrice

    fun isEquippable(): Boolean = type in listOf(
        ItemType.WEAPON, ItemType.SHIELD, ItemType.ARMOR,
        ItemType.HELMET, ItemType.BOOTS, ItemType.RING
    )
}

data class CraftingRecipe(
    val id: String,
    val resultItem: Item,
    val requiredMaterials: Map<String, Int>,
    val requiredGold: Int
)

object ItemCatalog {
    // Weapons
    val RUSTY_SWORD = Item("w_rusty_sword", "سيف حديدي صدئ", "سيف مبتدئ بسيط ذو حد باهت.", ItemType.WEAPON, ItemRarity.COMMON, attackBonus = 8f, sellPrice = 10, buyPrice = 25, iconId = "sword_iron")
    val STEEL_BROADSWORD = Item("w_steel_broadsword", "سيف الفولاذ الملكي", "سيف عسكري متين مصنوع من الفولاذ المشحوذ.", ItemType.WEAPON, ItemRarity.UNCOMMON, attackBonus = 20f, critBonus = 0.05f, sellPrice = 45, buyPrice = 100, iconId = "sword_steel")
    val SHADOW_DAGGERS = Item("w_shadow_daggers", "خناجر الظل القاتلة", "نصلان سريعان يقطعان نسيم الليل بهدوء وضربات حرجة.", ItemType.WEAPON, ItemRarity.RARE, attackBonus = 28f, critBonus = 0.20f, speedBonus = 25f, sellPrice = 90, buyPrice = 220, iconId = "dagger_shadow")
    val ELVEN_LONGBOW = Item("w_elven_bow", "قوس الغابة الإلفي", "قوس مصنوع من خشب شجرة القمر يطلق بدقة متناهية.", ItemType.WEAPON, ItemRarity.RARE, attackBonus = 32f, critBonus = 0.15f, sellPrice = 95, buyPrice = 240, iconId = "bow_elven")
    val ARCANE_STAFF = Item("w_arcane_staff", "عصا النيازك الأركينية", "تختزن طاقة النجوم وتزيد من قوة الإشعاع السحري.", ItemType.WEAPON, ItemRarity.RARE, attackBonus = 35f, manaBonus = 40f, sellPrice = 110, buyPrice = 260, iconId = "staff_arcane")
    val VOIDBRINGER_GREATSWORD = Item("w_voidbringer", "سيف كسوف الفراغ الأسطوري", "سيف مغمور بطاقة الفراغ المظلمة، يمزق أعتى الدروع بلهيب أرجواني.", ItemType.WEAPON, ItemRarity.LEGENDARY, attackBonus = 65f, critBonus = 0.25f, hpBonus = 50f, sellPrice = 500, buyPrice = 1200, iconId = "sword_void")

    // Shields
    val WOODEN_BUCKLER = Item("s_wood_buckler", "ترس خشبي قديم", "يوفر حماية بدائية ضد السهام والهجمات.", ItemType.SHIELD, ItemRarity.COMMON, defenseBonus = 6f, sellPrice = 8, buyPrice = 20, iconId = "shield_wood")
    val TOWER_SHIELD = Item("s_tower_shield", "ترس الفارس المدرع", "درع فولاذي ثقيل يصد أشرس ضربات الوحوش.", ItemType.SHIELD, ItemRarity.RARE, defenseBonus = 22f, hpBonus = 30f, sellPrice = 80, buyPrice = 200, iconId = "shield_steel")
    val AEGIS_OF_LIGHT = Item("s_aegis_light", "درع النور الساطع", "درع مقدس يشع هالة تزيد الدفاع وتقلل أضرار الظلال.", ItemType.SHIELD, ItemRarity.LEGENDARY, defenseBonus = 45f, hpBonus = 80f, sellPrice = 450, buyPrice = 1000, iconId = "shield_gold")

    // Armor
    val CLOTH_TUNIC = Item("a_cloth_tunic", "سترة قماشية عادية", "ملابس خفيفة لا تعيق الحركة ولكن حمايتها محدودة.", ItemType.ARMOR, ItemRarity.COMMON, defenseBonus = 4f, sellPrice = 5, buyPrice = 15, iconId = "armor_cloth")
    val CHAINMAIL_VEST = Item("a_chainmail", "درع الحلقات المتشابكة", "يوفر حماية متوازنة وخفة مقبولة.", ItemType.ARMOR, ItemRarity.UNCOMMON, defenseBonus = 16f, hpBonus = 20f, sellPrice = 50, buyPrice = 120, iconId = "armor_chain")
    val DRAGONSCALE_ARMOR = Item("a_dragonscale", "درع حراشف التنين الملكي", "مصنوع من حراشف التنين الأحمر القوية، يوفر صلابة لا تُقهر.", ItemType.ARMOR, ItemRarity.EPIC, defenseBonus = 38f, hpBonus = 70f, sellPrice = 280, buyPrice = 650, iconId = "armor_dragon")

    // Helmets
    val IRON_HELM = Item("h_iron_helm", "خوذة حديدية", "تحمي الرأس من الضربات المباشرة.", ItemType.HELMET, ItemRarity.COMMON, defenseBonus = 5f, sellPrice = 12, buyPrice = 30, iconId = "helm_iron")
    val CROWN_OF_SOULS = Item("h_crown_souls", "تاج الأرواح الحكيمة", "يزيد البصيرة والمانا وقوة الهجوم السحري.", ItemType.HELMET, ItemRarity.EPIC, defenseBonus = 18f, manaBonus = 50f, attackBonus = 15f, sellPrice = 220, buyPrice = 500, iconId = "helm_crown")

    // Boots
    val LEATHER_BOOTS = Item("b_leather_boots", "أحذية جلدية متينة", "تسهل الركض والمراوغة في الأراضي الوعرة.", ItemType.BOOTS, ItemRarity.COMMON, speedBonus = 20f, defenseBonus = 3f, sellPrice = 10, buyPrice = 25, iconId = "boots_leather")
    val WINGED_GREAVES = Item("b_winged_greaves", "أحذية الرياح المجنحة", "تمنح حاملها خفة استثنائية وسرعة خاطفة.", ItemType.BOOTS, ItemRarity.RARE, speedBonus = 55f, defenseBonus = 10f, sellPrice = 110, buyPrice = 250, iconId = "boots_gold")

    // Rings
    val RING_VITALITY = Item("r_vitality", "خاتم الحيوية", "يبث نبضاً حيوياً يزيد من الصحة القصوى.", ItemType.RING, ItemRarity.UNCOMMON, hpBonus = 40f, sellPrice = 35, buyPrice = 80, iconId = "ring_ruby")
    val RING_BERSERKER = Item("r_berserk", "خاتم الهائج", "يزيد معدل الضربات الحرجة وقوة الهجوم عند الاحتدام.", ItemType.RING, ItemRarity.EPIC, attackBonus = 16f, critBonus = 0.15f, sellPrice = 180, buyPrice = 400, iconId = "ring_amber")

    // Potions
    val HEALTH_POTION = Item("p_health", "جرعة صحة حمراء", "تستعيد 60 نقطة من نقاط الصحة فوراً.", ItemType.POTION, ItemRarity.COMMON, sellPrice = 8, buyPrice = 20, iconId = "potion_red")
    val MANA_POTION = Item("p_mana", "جرعة مانا زرقاء", "تستعيد 50 نقطة من طاقة المانا السحرية.", ItemType.POTION, ItemRarity.COMMON, sellPrice = 8, buyPrice = 20, iconId = "potion_blue")
    val STAMINA_ELIXIR = Item("p_stamina", "إكسير التحمل الأخضر", "يستعيد 80 نقطة من قوة التحمل للركض والمراوغة.", ItemType.POTION, ItemRarity.UNCOMMON, sellPrice = 12, buyPrice = 30, iconId = "potion_green")
    val GREATER_HEALING = Item("p_greater_heal", "إكسير الشفاء الفائق", "يستعيد 150 نقطة صحة ويشفي من السموم.", ItemType.POTION, ItemRarity.RARE, sellPrice = 25, buyPrice = 60, iconId = "potion_purple")

    // Materials
    val IRON_ORE = Item("m_iron_ore", "خام الحديد النقي", "معدن أساسي لصياغة الأسلحة والدروع في الحدادة.", ItemType.MATERIAL, ItemRarity.COMMON, sellPrice = 5, buyPrice = 15, iconId = "mat_ore")
    val WOLF_PELT = Item("m_wolf_pelt", "فرو ذئب الغابة", "فرو دافئ وقوي يمكن استخدامه في صناعة السترات والأحذية.", ItemType.MATERIAL, ItemRarity.COMMON, sellPrice = 7, buyPrice = 18, iconId = "mat_pelt")
    val MONSTER_BONE = Item("m_monster_bone", "عظام وحش قديم", "صلبة وكثيفة تستخدم في تدعيم المقابض والدروع.", ItemType.MATERIAL, ItemRarity.UNCOMMON, sellPrice = 10, buyPrice = 25, iconId = "mat_bone")
    val VOID_ESSENCE = Item("m_void_essence", "جوهر الفراغ المظلم", "طاقة أثيرية تتقاطر من وحوش الظلال وزعماء المغارات.", ItemType.MATERIAL, ItemRarity.EPIC, sellPrice = 50, buyPrice = 150, iconId = "mat_essence")
    val HEALING_HERB = Item("m_herb", "عشبة الشفاء النادرة", "نبات بري يُجمع من الغابات لصنع جرعات العلاج.", ItemType.MATERIAL, ItemRarity.COMMON, sellPrice = 4, buyPrice = 10, iconId = "mat_herb")

    // Quest Items
    val CRYPT_KEY = Item("q_crypt_key", "مفتاح مغارة الفراغ", "مفتاح حديدي عتيق منقوش برمز المملكة المنهارة.", ItemType.QUEST_ITEM, ItemRarity.RARE, sellPrice = 0, buyPrice = 0, iconId = "key_bronze")
    val ANCIENT_TABLET = Item("q_tablet", "لوح إيثلجارد الحجري", "نقش أثري يفضح حقيقة اللعنة التي أصابت الملك مالاكور.", ItemType.QUEST_ITEM, ItemRarity.EPIC, sellPrice = 0, buyPrice = 0, iconId = "tablet_stone")

    val CRAFTING_RECIPES = listOf(
        CraftingRecipe("craft_health_pot", HEALTH_POTION.copy(quantity = 2), mapOf("m_herb" to 2), 10),
        CraftingRecipe("craft_stamina_elixir", STAMINA_ELIXIR.copy(quantity = 1), mapOf("m_herb" to 2, "m_wolf_pelt" to 1), 20),
        CraftingRecipe("craft_steel_sword", STEEL_BROADSWORD, mapOf("m_iron_ore" to 4, "w_rusty_sword" to 1), 50),
        CraftingRecipe("craft_chainmail", CHAINMAIL_VEST, mapOf("m_iron_ore" to 5, "m_monster_bone" to 2), 60),
        CraftingRecipe("craft_shadow_daggers", SHADOW_DAGGERS, mapOf("m_iron_ore" to 3, "m_void_essence" to 1), 120),
        CraftingRecipe("craft_voidbringer", VOIDBRINGER_GREATSWORD, mapOf("w_steel_broadsword" to 1, "m_void_essence" to 3, "m_monster_bone" to 4), 300)
    )

    val RECIPES = CRAFTING_RECIPES
}
