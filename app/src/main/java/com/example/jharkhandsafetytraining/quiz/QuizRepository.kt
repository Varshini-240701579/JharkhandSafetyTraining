package com.example.jharkhandsafetytraining.quiz

import android.content.Context
import org.json.JSONObject

class QuizRepository(private val context: Context) {

    fun load(moduleId: String): QuizBank {
        val json = context.assets.open("quiz_${moduleId.lowercase()}.json")
            .bufferedReader().use { it.readText() }
        val root = JSONObject(json)
        val qs = root.getJSONArray("questions")

        val questions = (0 until qs.length()).map { i ->
            val q = qs.getJSONObject(i)
            val opts = q.getJSONArray("options")
            Question(
                id = q.getString("id"),
                text = q.getJSONObject("text").toStringMap(),
                options = (0 until opts.length()).map { opts.getJSONObject(it).toStringMap() },
                correctIndex = q.getInt("correctIndex")
            )
        }
        return QuizBank(
            root.getString("moduleId"),
            root.getInt("version"),
            root.getInt("passPercent"),
            questions
        )
    }

    private fun JSONObject.toStringMap(): Map<String, String> =
        keys().asSequence().associateWith { getString(it) }
}