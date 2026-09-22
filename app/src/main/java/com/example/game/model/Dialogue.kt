package com.example.game.model

data class DialogueOption(
    val text: String,
    val action: String, // e.g. "accept_quest", "open_shop", "open_craft", "lore_more", "close"
    val nextNodeId: String? = null
)

data class DialogueNode(
    val id: String,
    val npcId: String,
    val speakerName: String,
    val speakerTitle: String,
    val text: String,
    val options: List<DialogueOption>
)

object DialogueDatabase {
    fun getNode(npcId: String, currentQuestState: String): DialogueNode {
        return when (npcId) {
            "npc_valerie" -> {
                DialogueNode(
                    id = "valerie_main",
                    npcId = "npc_valerie",
                    speakerName = "الحكيمة فاليري (Sage Valerie)",
                    speakerTitle = "كاهنة نور إيثلجارد",
                    text = "مرحباً بعودتك أيها الفارس. لقد ظننا أن نورك انطفأ في الكسوف العظيم... مملكة إيثلجارد تنهار ببطء، والملك مالاكور استسلم لقوى الفراغ التي لا ترحم. هل أنت مستعد لاستعادة شعلة الأمل؟",
                    options = listOf(
                        DialogueOption("سأفعل ما بوسعي لإنقاذ المملكة.", "accept_quest_talk_valerie", "valerie_duty"),
                        DialogueOption("ما الذي حدث للملك مالاكور بالضبط؟", "lore_malakor", "valerie_lore"),
                        DialogueOption("سأعود لاحقاً يا سيدتي.", "close")
                    )
                )
            }
            "npc_brom" -> {
                DialogueNode(
                    id = "brom_main",
                    npcId = "npc_brom",
                    speakerName = "الحداد بروم (Brom the Smith)",
                    speakerTitle = "سيد المطرقة والسندان",
                    text = "سيفي الحاد ودرعي المنيع خير حليف في زمن الوحوش! إن كنت تبحث عن ترقية عتادك أو صهر خامات المعادن، فمطرقتي تحت أمرك.",
                    options = listOf(
                        DialogueOption("أريد صياغة وتطوير العتاد.", "open_craft"),
                        DialogueOption("أحتاج لشراء أسلحة ودروع جديدة.", "open_shop"),
                        DialogueOption("هل تحتاج لمساعدة في ورشتك؟", "talk_brom_quest"),
                        DialogueOption("إلى اللقاء يا بروم.", "close")
                    )
                )
            }
            "npc_zarek" -> {
                DialogueNode(
                    id = "zarek_main",
                    npcId = "npc_zarek",
                    speakerName = "التاجر زاريك (Zarek the Wanderer)",
                    speakerTitle = "تاجر القوافل النادرة",
                    text = "أهلاً يا صديقي المغامر! جلبت بضائع من أقاصي الجبال والغابات المنسية.. جرعات سحرية، إكسيرات حيوية، ومواد نادرة. الذهب يتكلم هنا!",
                    options = listOf(
                        DialogueOption("أرني ما تملكه للبيع والشراء.", "open_shop"),
                        DialogueOption("هل رأيت شيئاً مريباً في الجبال؟", "lore_zarek_mountains"),
                        DialogueOption("سألقاك لاحقاً.", "close")
                    )
                )
            }
            "npc_wanderer" -> {
                DialogueNode(
                    id = "wanderer_main",
                    npcId = "npc_wanderer",
                    speakerName = "المسافر المقنع (Masked Wanderer)",
                    speakerTitle = "ظل من الماضي",
                    text = "احذر يا فارس... ليس كل من يحمل راية النور بريئاً، ومالاكور لم يكن الشرير الوحيد في هذه المأساة. إذا بلغت أطلال الصمت، فابحث عن الحقيقة خلف النصب الحجري...",
                    options = listOf(
                        DialogueOption("من أنت؟ وما مصلحتك في تحذيري؟", "lore_wanderer"),
                        DialogueOption("سأضع نصب عيني كلماتك.", "close")
                    )
                )
            }
            else -> {
                DialogueNode(
                    id = "default",
                    npcId = npcId,
                    speakerName = "قروي مسافر",
                    speakerTitle = "مواطن من القرية",
                    text = "السلام عليك يا فارس! الطرقات في الخارج محفوفة بالمخاطر، لا تغادر دون جرعات وسيف مسنون.",
                    options = listOf(DialogueOption("شكراً على النصيحة.", "close"))
                )
            }
        }
    }
}
