package com.example.jharkhandsafetytraining

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object LanguageSelect : Screen("language_select")
    object Home : Screen("home")
    object ModuleDetail : Screen("module_detail/{moduleId}") {
        fun passModuleId(id: String) = "module_detail/$id"
    }
    object Quiz : Screen("quiz/{moduleId}") {
        fun passModuleId(id: String) = "quiz/$id"
    }
    object Certificate : Screen("certificate")
}