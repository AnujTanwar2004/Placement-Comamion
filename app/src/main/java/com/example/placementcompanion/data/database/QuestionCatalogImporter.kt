package com.example.placementcompanion.data.database

import android.content.Context
import androidx.room.withTransaction
import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QuestionCatalogImporter(private val database: AppDatabase) {
    suspend fun importIfNeeded(context: Context): QuestionCatalogEntity = withContext(Dispatchers.IO) {
        val gson = Gson()
        val manifest = context.assets.open("company_questions/manifest.json").bufferedReader().use {
            gson.fromJson(it, Manifest::class.java)
        }
        val dao = database.placementDao()
        val current = dao.getQuestionCatalog()
        val revision = "${manifest.commit}:${manifest.assetSha256}"
        if (current?.commit == revision) return@withContext current
        val catalog = QuestionCatalogEntity(
            commit = revision, snapshotDate = manifest.snapshotDate,
            companyCount = manifest.companyCount, questionCount = manifest.questionCount
        )
        // Seed and completion marker commit together; interruption never leaves a completed partial import.
        database.withTransaction {
            var imported = 0
            // Android's asset merger expands .gz files and removes that suffix in the APK.
            context.assets.open("company_questions/questions.json").use { asset ->
                JsonReader(asset.reader(Charsets.UTF_8)).use { reader ->
                    reader.beginArray()
                    val batch = ArrayList<QuestionEntity>(500)
                    while (reader.hasNext()) {
                        val question = gson.fromJson<QuestionEntity>(reader, QuestionEntity::class.java)
                        require(question.leetcodeId != null && question.title.isNotBlank())
                        batch.add(question)
                        imported++
                        if (batch.size == 500) {
                            dao.importCatalogQuestions(batch)
                            batch.clear()
                        }
                    }
                    reader.endArray()
                    if (batch.isNotEmpty()) dao.importCatalogQuestions(batch)
                }
            }
            check(imported == manifest.questionCount) { "Incomplete company question catalog" }
            // Stable IDs and merged updates retain solved flags, bookmarks and notes on catalog refresh.
            dao.saveQuestionCatalog(catalog)
        }
        catalog
    }

    private data class Manifest(val commit: String, val assetSha256: String, val snapshotDate: String, val companyCount: Int, val questionCount: Int)
}
