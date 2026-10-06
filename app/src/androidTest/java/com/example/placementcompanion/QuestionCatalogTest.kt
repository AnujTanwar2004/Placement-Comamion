package com.example.placementcompanion

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.placementcompanion.data.database.*
import com.example.placementcompanion.repository.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QuestionCatalogTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private lateinit var database: AppDatabase
    private lateinit var repository: PlacementRepository

    @Before fun setup() {
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        repository = PlacementRepository(database.placementDao())
    }
    @After fun close() { database.close(); context.deleteDatabase("question-migration-test") }
    private suspend fun import() = QuestionCatalogImporter(database).importIfNeeded(context)

    @Test fun fullCatalogImportsOnceAndRetainsUserProgress() = runBlocking {
        val catalog = import()
        assertEquals(17826, catalog.questionCount)
        assertEquals(659, catalog.companyCount)
        assertEquals(17826, repository.questionCount(QuestionFilters(completion = QuestionCompletion.ALL)).first())
        assertEquals(659, repository.questionCompanies.first().size)
        val q = repository.questionPage(QuestionFilters(query = "Amazon Two Sum")).first().single { it.leetcodeId == 1 }
        repository.toggleQuestionSolved(q.id, false)
        repository.toggleQuestionBookmark(q.id, false)
        repository.setQuestionNote(q.id, "My saved approach")
        // Force re-import as if the completion marker were outdated.
        database.placementDao().saveQuestionCatalog(catalog.copy(commit = "previous-revision"))
        import()
        val saved = repository.questionPage(QuestionFilters(query = "Amazon Two Sum", completion = QuestionCompletion.COMPLETED)).first().single { it.leetcodeId == 1 }
        assertTrue(saved.isSolved); assertTrue(saved.isBookmarked)
        assertEquals("My saved approach", saved.userNote)
        assertEquals(17826, repository.questionCount(QuestionFilters(completion = QuestionCompletion.ALL)).first())
    }

    @Test fun companyAndQuestionTokensWorkInAnyOrderAndAliasesAreSearchable() = runBlocking {
        import()
        val first = repository.questionPage(QuestionFilters(query = "pyqs of AMAZON two sum")).first()
        val second = repository.questionPage(QuestionFilters(query = "two sum amazon")).first()
        assertTrue(first.isNotEmpty()); assertEquals(first.map { it.id }, second.map { it.id })
        assertTrue(first.all { it.companyName == "Amazon" })
        assertEquals(1, first.single { it.title == "Two Sum" }.leetcodeId)
        assertTrue(repository.questionPage(QuestionFilters(query = "Facebook")).first().isNotEmpty())
        val nvidia = repository.questionPage(QuestionFilters(query = "NVIDIA")).first()
        assertTrue(nvidia.isNotEmpty()); assertTrue(nvidia.all { it.companyName == "NVIDIA" })
        assertTrue(repository.questionPage(QuestionFilters(query = "nonexistent-company-xyz")).first().isEmpty())
        val literalPercent = repository.questionPage(QuestionFilters(query = "%")).first()
        assertEquals(4, literalPercent.size)
        assertTrue(literalPercent.all { '%' in it.title })
        assertTrue(repository.questionPage(QuestionFilters(query = "' OR 1=1 --")).first().isEmpty())
    }

    @Test fun recencyAndDifficultyUseWindowSpecificFrequency() = runBlocking {
        import()
        val filter = QuestionFilters(company = "Amazon", window = QuestionWindow.DAYS_30)
        assertEquals(153, repository.questionCount(filter).first())
        val recent = repository.questionPage(filter, 500).first()
        assertTrue(recent.all { it.frequency30Days != null && it.companyName == "Amazon" })
        val frequencies = recent.map { it.frequency30Days!! }
        assertEquals(frequencies.sortedDescending(), frequencies)
        val hard = repository.questionPage(filter.copy(difficulty = "Hard")).first()
        assertTrue(hard.isNotEmpty()); assertTrue(hard.all { it.difficulty == "Hard" })
        assertTrue(repository.questionCount(filter.copy(window = QuestionWindow.ALL)).first() > 153)
    }

    @Test fun pagingAndBookmarkFilterKeepStableIds() = runBlocking {
        import()
        val filter = QuestionFilters(company = "Google", sort = QuestionSort.TITLE)
        val first = repository.questionPage(filter, 100).first()
        val more = repository.questionPage(filter, 200).first()
        assertEquals(100, first.size); assertEquals(200, more.size)
        assertEquals(first.map { it.id }, more.take(100).map { it.id })
        val q = first.first()
        repository.toggleQuestionBookmark(q.id, false)
        assertEquals(listOf(q.id), repository.questionPage(filter.copy(savedOnly = true)).first().map { it.id })
    }

    @Test fun versionOneMigrationPreservesQuestionsTasksAndNotes() = runBlocking {
        val dao = database.placementDao()
        dao.insertQuestions(listOf(QuestionEntity(id = 42, companyId = 1, companyName = "Amazon", title = "Saved question",
            description = "Keep this", topic = "Arrays", difficulty = "Easy", year = 2025, round = "Sample", role = "SDE", source = "Sample",
            isSolved = true, isBookmarked = true, userNote = "Keep my note")))
        dao.insertTask(StudyTaskEntity(title = "Saved task", subject = "DSA", scheduledDate = "2026-10-06", timeSlot = "7 PM"))
        dao.insertNote(NoteEntity(title = "Saved note", content = "My preparation notes"))
        val source = database.openHelper.writableDatabase
        val targetFile = context.getDatabasePath("question-migration-test")
        targetFile.parentFile!!.mkdirs()
        val old = SQLiteDatabase.openOrCreateDatabase(targetFile, null)
        source.query("SELECT name, sql FROM sqlite_master WHERE type = 'table' AND name NOT LIKE 'sqlite_%' AND name NOT IN ('android_metadata', 'room_master_table', 'question_catalog')").use { cursor ->
            while (cursor.moveToNext()) {
                val name = cursor.getString(0)
                val sql = if (name == "company_questions") OLD_QUESTION_SQL else cursor.getString(1)
                old.execSQL(sql)
                source.query("SELECT * FROM `$name`").use { rows ->
                    val columns = rows.columnNames.filter { name != "company_questions" || it !in NEW_COLUMNS }
                    val insert = "INSERT INTO `$name` (${columns.joinToString { "`$it`" }}) VALUES (${columns.joinToString { "?" }})"
                    while (rows.moveToNext()) {
                        val values: Array<Any?> = columns.map<String, Any?> { col ->
                            val index = rows.getColumnIndexOrThrow(col)
                            when (rows.getType(index)) {
                                android.database.Cursor.FIELD_TYPE_NULL -> null
                                android.database.Cursor.FIELD_TYPE_INTEGER -> rows.getLong(index)
                                android.database.Cursor.FIELD_TYPE_FLOAT -> rows.getDouble(index)
                                else -> rows.getString(index)
                            }
                        }.toTypedArray()
                        old.execSQL(insert, values)
                    }
                }
            }
        }
        old.version = 1; old.close()
        val upgraded = Room.databaseBuilder(context, AppDatabase::class.java, "question-migration-test").addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3).build()
        try {
            val migrated = upgraded.placementDao().getAllQuestions().first().single()
            assertTrue(migrated.isSolved); assertTrue(migrated.isBookmarked); assertEquals("Keep my note", migrated.userNote)
            assertEquals("Saved task", upgraded.placementDao().getAllTasks().first().single().title)
            assertEquals("Saved note", upgraded.placementDao().getAllNotes().first().single().title)
            QuestionCatalogImporter(upgraded).importIfNeeded(context)
            assertEquals(17827, PlacementRepository(upgraded.placementDao()).questionCount(QuestionFilters(completion = QuestionCompletion.ALL)).first())
        } finally { upgraded.close() }
    }

    companion object {
        private val NEW_COLUMNS = setOf("companySlug", "leetcodeId", "snapshotDate", "acceptanceRate", "frequencyAll", "frequency30Days", "frequency3Months", "frequency6Months", "frequencyOlder")
        private const val OLD_QUESTION_SQL = "CREATE TABLE company_questions (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, companyId INTEGER NOT NULL, companyName TEXT NOT NULL, title TEXT NOT NULL, description TEXT NOT NULL, topic TEXT NOT NULL, difficulty TEXT NOT NULL, year INTEGER NOT NULL, round TEXT NOT NULL, role TEXT NOT NULL, source TEXT NOT NULL, solutionLink TEXT NOT NULL, tags TEXT NOT NULL, isSolved INTEGER NOT NULL, isBookmarked INTEGER NOT NULL, userNote TEXT NOT NULL)"
    }
}
