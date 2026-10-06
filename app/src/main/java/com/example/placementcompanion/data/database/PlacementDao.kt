package com.example.placementcompanion.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import androidx.sqlite.db.SupportSQLiteQuery

@Dao
interface PlacementDao {

    // User Profile
    @Query("SELECT * FROM users WHERE id = 1 LIMIT 1")
    fun getUserFlow(): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = 1 LIMIT 1")
    suspend fun getUser(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity): Int

    // Companies
    @Query("SELECT * FROM companies ORDER BY name ASC")
    fun getAllCompanies(): Flow<List<CompanyEntity>>

    @Query("SELECT * FROM companies WHERE id = :id")
    suspend fun getCompanyById(id: Long): CompanyEntity?

    @Query("SELECT * FROM companies WHERE name LIKE '%' || :query || '%' OR hiringRoles LIKE '%' || :query || '%'")
    fun searchCompanies(query: String): Flow<List<CompanyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompanies(companies: List<CompanyEntity>): List<Long>

    @Query("UPDATE companies SET isBookmarked = :isBookmarked WHERE id = :id")
    suspend fun setCompanyBookmark(id: Long, isBookmarked: Boolean): Int

    // Company Questions / PYQs
    @Query("SELECT * FROM company_questions ORDER BY id DESC")
    fun getAllQuestions(): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM company_questions WHERE companyId = :companyId AND isSolved = 0")
    fun getQuestionsByCompany(companyId: Long): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM company_questions WHERE title LIKE '%' || :query || '%' OR topic LIKE '%' || :query || '%' OR companyName LIKE '%' || :query || '%'")
    fun searchQuestions(query: String): Flow<List<QuestionEntity>>

    @RawQuery(observedEntities = [QuestionEntity::class])
    fun queryQuestions(query: SupportSQLiteQuery): Flow<List<QuestionEntity>>

    @RawQuery(observedEntities = [QuestionEntity::class])
    fun countQuestions(query: SupportSQLiteQuery): Flow<Int>

    @Query("SELECT DISTINCT companyName FROM company_questions WHERE leetcodeId IS NOT NULL ORDER BY companyName COLLATE NOCASE")
    fun getQuestionCompanies(): Flow<List<String>>

    @Query("SELECT COUNT(DISTINCT CASE WHEN leetcodeId IS NOT NULL THEN 'leetcode:' || leetcodeId ELSE 'local:' || id END) FROM company_questions WHERE isSolved = 1")
    fun getDistinctSolvedQuestionCount(): Flow<Int>

    @Query("SELECT * FROM question_catalog WHERE id = 1")
    suspend fun getQuestionCatalog(): QuestionCatalogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveQuestionCatalog(catalog: QuestionCatalogEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCatalogQuestions(questions: List<QuestionEntity>)

    @Query("SELECT * FROM company_questions WHERE id IN (:ids)")
    suspend fun existingCatalogRows(ids: List<Long>): List<QuestionEntity>

    @Update
    suspend fun updateCatalogQuestions(questions: List<QuestionEntity>)

    @Transaction
    suspend fun importCatalogQuestions(questions: List<QuestionEntity>) {
        val existing = existingCatalogRows(questions.map { it.id }).associateBy { it.id }
        insertCatalogQuestions(questions)
        updateCatalogQuestions(questions.mapNotNull { question ->
            existing[question.id]?.let { old -> question.copy(isSolved = old.isSolved, isBookmarked = old.isBookmarked, userNote = old.userNote) }
        })
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>): List<Long>

    @Query("UPDATE company_questions SET isSolved = :isSolved WHERE id = :id")
    suspend fun setQuestionSolved(id: Long, isSolved: Boolean): Int

    @Query("UPDATE company_questions SET isBookmarked = :isBookmarked WHERE id = :id")
    suspend fun setQuestionBookmark(id: Long, isBookmarked: Boolean): Int

    @Query("UPDATE company_questions SET userNote = :note WHERE id = :id")
    suspend fun setQuestionNote(id: Long, note: String): Int

    // Study Tasks
    @Query("SELECT * FROM study_tasks ORDER BY scheduledDate ASC, timeSlot ASC")
    fun getAllTasks(): Flow<List<StudyTaskEntity>>

    @Query("SELECT * FROM study_tasks WHERE scheduledDate = :date ORDER BY timeSlot ASC")
    fun getTasksForDate(date: String): Flow<List<StudyTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: StudyTaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<StudyTaskEntity>): List<Long>

    @Update
    suspend fun updateTask(task: StudyTaskEntity): Int

    @Query("DELETE FROM study_tasks WHERE id = :id")
    suspend fun deleteTask(id: Long): Int

    @Query("UPDATE study_tasks SET isCompleted = :completed WHERE id = :id")
    suspend fun setTaskCompleted(id: Long, completed: Boolean): Int

    // Notes
    @Query("SELECT * FROM notes ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' OR subject LIKE '%' || :query || '%'")
    fun searchNotes(query: String): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity): Int

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNote(id: Long): Int

    // Interview Experiences
    @Query("SELECT * FROM interview_experiences ORDER BY id DESC")
    fun getAllExperiences(): Flow<List<InterviewExperienceEntity>>

    @Query("SELECT * FROM interview_experiences WHERE companyName LIKE '%' || :company || '%'")
    fun getExperiencesByCompany(company: String): Flow<List<InterviewExperienceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExperience(experience: InterviewExperienceEntity): Long

    @Query("UPDATE interview_experiences SET helpfulVotes = helpfulVotes + 1 WHERE id = :id")
    suspend fun upvoteExperience(id: Long): Int

    // Resources
    @Query("SELECT * FROM resources ORDER BY addedDate DESC")
    fun getAllResources(): Flow<List<ResourceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResource(resource: ResourceEntity): Long

    @Query("DELETE FROM resources WHERE id = :id")
    suspend fun deleteResource(id: Long): Int

    // Bookmarks
    @Query("SELECT * FROM bookmarks ORDER BY addedAt DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity): Long

    @Query("DELETE FROM bookmarks WHERE itemType = :type AND itemId = :itemId")
    suspend fun deleteBookmark(type: String, itemId: Long): Int
}
