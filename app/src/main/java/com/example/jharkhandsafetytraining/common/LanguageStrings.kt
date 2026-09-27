package com.example.jharkhandsafetytraining.common

object AppStrings {
    private val translations = mapOf(
        "app_title" to mapOf(
            "en" to "Jharkhand Mine Safety",
            "hi" to "झारखंड खान सुरक्षा",
            "sat" to "ᱡᱷᱟᱨᱠᱷᱚᱸᱰ ᱠᱷᱟᱫᱟᱱ ᱨᱩᱠᱷᱤᱭᱟᱹ"
        ),
        "app_subtitle" to mapOf(
            "en" to "DGMS Vocational Safety Portal",
            "hi" to "डीजीएमएस व्यावसायिक सुरक्षा पोर्टल",
            "sat" to "ᱰᱤ.ᱡᱤ.ᱮᱢ.ᱮᱥ ᱵᱮᱵᱚᱥᱟᱭ ᱨᱩᱠᱷᱤᱭᱟᱹ ᱯᱚᱨᱴᱟᱞ"
        ),
        "start_training" to mapOf(
            "en" to "Start Training",
            "hi" to "प्रशिक्षण शुरू करें",
            "sat" to "ᱯᱨᱚᱥᱤᱠᱷᱚᱱ ᱮᱦᱚᱵᱽ ᱢᱮ"
        ),
        "ar_drill" to mapOf(
            "en" to "Interactive AR Drill",
            "hi" to "एआर सुरक्षा अभ्यास",
            "sat" to "ᱮ.ᱟᱨ ᱨᱩᱠᱷᱤᱭᱟᱹ ᱟᱵᱷᱭᱟᱥ"
        ),
        "take_quiz" to mapOf(
            "en" to "Comprehension Test",
            "hi" to "ज्ञान परीक्षा",
            "sat" to "ᱵᱤᱰᱟᱹᱣ ᱦᱟᱛᱟᱣ ᱢᱮ"
        ),
        "certified" to mapOf(
            "en" to "CERTIFIED",
            "hi" to "प्रमाणित",
            "sat" to "ᱯᱨᱚᱢᱟᱬᱤᱛ"
        ),
        "pending" to mapOf(
            "en" to "REQUIRED",
            "hi" to "आवश्यक",
            "sat" to "ᱞᱟᱹᱠᱛᱤᱭᱟᱱ"
        )
    )

    fun get(key: String, lang: String = "hi"): String {
        return translations[key]?.get(lang) ?: translations[key]?.get("en") ?: key
    }
}