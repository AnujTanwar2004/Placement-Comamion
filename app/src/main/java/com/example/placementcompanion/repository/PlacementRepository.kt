package com.example.placementcompanion.repository

import com.example.placementcompanion.data.database.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import android.content.Context
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

sealed interface CatalogState {
    data object Loading : CatalogState
    data class Ready(val catalog: QuestionCatalogEntity) : CatalogState
    data class Error(val message: String) : CatalogState
}

class PlacementRepository(private val dao: PlacementDao) {
    private val catalogMutex = Mutex()
    private val _catalogState = MutableStateFlow<CatalogState>(CatalogState.Loading)
    val catalogState = _catalogState.asStateFlow()

    suspend fun loadQuestionCatalog(database: AppDatabase, context: Context) = catalogMutex.withLock {
        _catalogState.value = CatalogState.Loading
        try {
            _catalogState.value = CatalogState.Ready(QuestionCatalogImporter(database).importIfNeeded(context))
        } catch (error: Exception) {
            if (error is kotlinx.coroutines.CancellationException) throw error
            _catalogState.value = CatalogState.Error(error.message ?: "Could not load offline questions")
        }
    }

    // User
    val userFlow: Flow<UserEntity?> = dao.getUserFlow()
    suspend fun getUser(): UserEntity? = dao.getUser()
    suspend fun saveUser(user: UserEntity) = dao.insertUser(user)
    suspend fun updateUser(user: UserEntity) = dao.updateUser(user)

    // Companies
    val allCompanies: Flow<List<CompanyEntity>> = dao.getAllCompanies()
    suspend fun getCompanyById(id: Long): CompanyEntity? = dao.getCompanyById(id)
    fun searchCompanies(query: String): Flow<List<CompanyEntity>> = dao.searchCompanies(query)
    suspend fun toggleCompanyBookmark(id: Long, current: Boolean) = dao.setCompanyBookmark(id, !current)

    // PYQs / Questions
    val allQuestions: Flow<List<QuestionEntity>> = dao.getAllQuestions()
    fun getQuestionsByCompany(companyId: Long): Flow<List<QuestionEntity>> = dao.getQuestionsByCompany(companyId)
    fun searchQuestions(query: String): Flow<List<QuestionEntity>> = questionPage(QuestionFilters(query = query))
    fun questionPage(filters: QuestionFilters, limit: Int = 100): Flow<List<QuestionEntity>> = dao.queryQuestions(QuestionQueries.build(filters, limit))
    fun questionCount(filters: QuestionFilters): Flow<Int> = dao.countQuestions(QuestionQueries.build(filters, count = true))
    val questionCompanies = dao.getQuestionCompanies()
    val solvedQuestionCount = dao.getDistinctSolvedQuestionCount()
    suspend fun toggleQuestionSolved(id: Long, current: Boolean) = dao.setQuestionSolved(id, !current)
    suspend fun toggleQuestionBookmark(id: Long, current: Boolean) = dao.setQuestionBookmark(id, !current)
    suspend fun setQuestionNote(id: Long, note: String) = dao.setQuestionNote(id, note)
    suspend fun insertQuestions(questions: List<QuestionEntity>) = dao.insertQuestions(questions)

    // Study Tasks
    val allTasks: Flow<List<StudyTaskEntity>> = dao.getAllTasks()
    fun getTasksForDate(date: String): Flow<List<StudyTaskEntity>> = dao.getTasksForDate(date)
    suspend fun addTask(task: StudyTaskEntity): Long = dao.insertTask(task)
    suspend fun addTasks(tasks: List<StudyTaskEntity>) = dao.insertTasks(tasks)
    suspend fun updateTask(task: StudyTaskEntity) = dao.updateTask(task)
    suspend fun deleteTask(id: Long) = dao.deleteTask(id)
    suspend fun setTaskCompleted(id: Long, completed: Boolean) = dao.setTaskCompleted(id, completed)

    // Notes
    val allNotes: Flow<List<NoteEntity>> = dao.getAllNotes()
    fun searchNotes(query: String): Flow<List<NoteEntity>> = dao.searchNotes(query)
    suspend fun addNote(note: NoteEntity): Long = dao.insertNote(note)
    suspend fun updateNote(note: NoteEntity) = dao.updateNote(note)
    suspend fun deleteNote(id: Long) = dao.deleteNote(id)

    // Experiences
    val allExperiences: Flow<List<InterviewExperienceEntity>> = dao.getAllExperiences()
    fun getExperiencesByCompany(company: String): Flow<List<InterviewExperienceEntity>> = dao.getExperiencesByCompany(company)
    suspend fun addExperience(experience: InterviewExperienceEntity) = dao.insertExperience(experience)
    suspend fun upvoteExperience(id: Long) = dao.upvoteExperience(id)

    // Resources
    val allResources: Flow<List<ResourceEntity>> = dao.getAllResources()
    suspend fun addResource(resource: ResourceEntity) = dao.insertResource(resource)
    suspend fun deleteResource(id: Long) = dao.deleteResource(id)

    // Bookmarks
    val allBookmarks: Flow<List<BookmarkEntity>> = dao.getAllBookmarks()
    suspend fun addBookmark(bookmark: BookmarkEntity) = dao.insertBookmark(bookmark)
    suspend fun removeBookmark(type: String, itemId: Long) = dao.deleteBookmark(type, itemId)
}
