package com.example.placementcompanion.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        CompanyEntity::class,
        QuestionEntity::class,
        StudyTaskEntity::class,
        NoteEntity::class,
        InterviewExperienceEntity::class,
        ResourceEntity::class,
        BookmarkEntity::class,
        QuestionCatalogEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun placementDao(): PlacementDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE company_questions ADD COLUMN companySlug TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE company_questions ADD COLUMN leetcodeId INTEGER")
                db.execSQL("ALTER TABLE company_questions ADD COLUMN snapshotDate TEXT NOT NULL DEFAULT ''")
                listOf("acceptanceRate", "frequencyAll", "frequency30Days", "frequency3Months", "frequency6Months", "frequencyOlder").forEach {
                    db.execSQL("ALTER TABLE company_questions ADD COLUMN $it REAL")
                }
                db.execSQL("CREATE UNIQUE INDEX index_company_questions_companySlug_leetcodeId ON company_questions (companySlug, leetcodeId)")
                db.execSQL("CREATE INDEX index_company_questions_companyName ON company_questions (companyName)")
                db.execSQL("CREATE INDEX index_company_questions_difficulty ON company_questions (difficulty)")
                db.execSQL("CREATE TABLE IF NOT EXISTS question_catalog (id INTEGER NOT NULL, `commit` TEXT NOT NULL, snapshotDate TEXT NOT NULL, companyCount INTEGER NOT NULL, questionCount INTEGER NOT NULL, PRIMARY KEY(id))")
            }
        }

        // Remove only identifiable demo rows from older installs. Keep user-created tasks
        // and all imported PYQ progress, bookmarks and notes.
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val demoTasks = listOf(
                    Triple(1, "Revise Binary Search Trees & Balanced Trees", "2025-03-01"),
                    Triple(2, "DBMS Normalization (1NF, 2NF, 3NF, BCNF)", "2025-03-01"),
                    Triple(3, "Solve 3 Dynamic Programming Questions on LeetCode", "2025-03-02"),
                    Triple(4, "Quantitative Aptitude: Time & Work Puzzles", "2025-03-02")
                )
                demoTasks.forEach { (id, title, date) ->
                    db.execSQL("DELETE FROM study_tasks WHERE id = ? AND title = ? AND scheduledDate = ? AND isAiGenerated = 0", arrayOf<Any>(id, title, date))
                }
                val demoQuestions = listOf(
                    1 to "Trapping Rain Water", 2 to "Course Schedule II (Topological Sort)",
                    3 to "Lowest Common Ancestor of a Binary Tree", 4 to "Find Third Largest Element in Array",
                    5 to "LRU Cache Implementation"
                )
                demoQuestions.forEach { (id, title) ->
                    db.execSQL("DELETE FROM company_questions WHERE id = ? AND title = ? AND source = 'Verified Student Submission' AND leetcodeId IS NULL AND isBookmarked = 0 AND userNote = ''", arrayOf<Any>(id, title))
                }
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "placement_companion_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .addCallback(AppDatabaseCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class AppDatabaseCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database.placementDao())
                    }
                }
            }

            suspend fun populateInitialData(dao: PlacementDao) {
                // Initial Companies
                dao.insertCompanies(
                    listOf(
                        CompanyEntity(
                            id = 1,
                            name = "Amazon",
                            logoUrl = "",
                            description = "Global e-commerce & cloud computing technology leader known for customer obsession and high scale systems.",
                            companyType = "Product",
                            hiringRoles = "SDE-1, Cloud Engineer, Data Engineer",
                            eligibilityCriteria = "CGPA >= 7.0, No active backlogs, CS/IT background preferred",
                            expectedSkills = "Arrays, Trees, Dynamic Programming, System Design basics, OOP, DBMS",
                            recruitmentProcess = "1. Online Assessment (Debugging & Coding)|2. Technical Round 1 (Data Structures)|3. Technical Round 2 (System Design/Algorithms)|4. Bar Raiser & Behavioral",
                            commonSubjects = "DSA, DBMS, OS, System Design",
                            difficulty = "Hard",
                            preparationStrategy = "Focus heavily on LeetCode Medium/Hard DP & Graph questions. Prepare Leadership Principles with STAR method examples."
                        ),
                        CompanyEntity(
                            id = 2,
                            name = "Microsoft",
                            logoUrl = "",
                            description = "Multinational technology company producing computer software, consumer electronics, and cloud solutions.",
                            companyType = "Product",
                            hiringRoles = "Software Engineer, Site Reliability Engineer",
                            eligibilityCriteria = "CGPA >= 7.5, B.Tech/M.Tech",
                            expectedSkills = "Strings, Trees, Linked Lists, C++/Java, Operating Systems, DBMS",
                            recruitmentProcess = "1. Online Assessment (3 Coding Questions)|2. Technical Round 1 (Problem Solving)|3. Technical Round 2 (Low Level Design)|4. AA Round (As Appropriated)",
                            commonSubjects = "DSA, OS, Computer Networks, OOP",
                            difficulty = "Hard",
                            preparationStrategy = "Master fundamental DS (Trees, Linked Lists, Binary Search) and practice OS concepts like Deadlocks, Paging, Memory Management."
                        ),
                        CompanyEntity(
                            id = 3,
                            name = "Google",
                            logoUrl = "",
                            description = "World leader in search, AI, cloud computing, and software platforms.",
                            companyType = "Product",
                            hiringRoles = "Software Engineer, Software Test Engineer",
                            eligibilityCriteria = "CGPA >= 8.0, Strong algorithmic foundations",
                            expectedSkills = "Advanced Graphs, Dynamic Programming, Segment Trees, Algorithmic Complexity",
                            recruitmentProcess = "1. Google Online Challenge|2. Telephonic Screening|3. 3x Technical Coding Rounds|4. Leadership & Googleyness Round",
                            commonSubjects = "DSA, Mathematics, Computer Science Fundamentals",
                            difficulty = "Hard",
                            preparationStrategy = "Focus on writing clean, bug-free code quickly. Explain time & space complexity clearly."
                        ),
                        CompanyEntity(
                            id = 4,
                            name = "TCS (Tata Consultancy Services)",
                            logoUrl = "",
                            description = "India's premier IT services, consulting, and business solutions organization.",
                            companyType = "Service",
                            hiringRoles = "Ninja, Digital, Prime Developer",
                            eligibilityCriteria = "60% or 6.0 CGPA throughout 10th, 12th & Graduation",
                            expectedSkills = "Aptitude, C/Java basics, Basic SQL, Logical Reasoning",
                            recruitmentProcess = "1. TCS NQT (Aptitude & Coding)|2. Technical Interview|3. Managerial Interview|4. HR Interview",
                            commonSubjects = "Aptitude, Quantitative, Verbal, Basic Programming",
                            difficulty = "Easy",
                            preparationStrategy = "Practice numerical aptitude speed, logical reasoning puzzles, and basic string manipulation program."
                        ),
                        CompanyEntity(
                            id = 5,
                            name = "Infosys",
                            logoUrl = "",
                            description = "Global leader in next-generation digital services and consulting.",
                            companyType = "Service",
                            hiringRoles = "Systems Engineer, Specialist Programmer, Power Programmer",
                            eligibilityCriteria = "60% throughout academics",
                            expectedSkills = "Data Structures, Python/Java, SQL, Reasoning",
                            recruitmentProcess = "1. HackWithInfy / InfyTQ Exam|2. Technical Interview|3. HR Discussion",
                            commonSubjects = "DSA, DBMS, Python, Aptitude",
                            difficulty = "Medium",
                            preparationStrategy = "Solve previous InfyTQ coding papers and review SQL normalization and JOIN queries."
                        )
                    )
                )

                // Initial Notes
                dao.insertNote(
                    NoteEntity(
                        id = 1,
                        title = "Operating Systems Key Formulas & Deadlock Conditions",
                        content = "Four Coffman Conditions for Deadlock:\n1. Mutual Exclusion\n2. Hold and Wait\n3. No Preemption\n4. Circular Wait\n\nBanker's Algorithm formula:\nNeed[i][j] = Max[i][j] - Allocation[i][j]",
                        subject = "Operating Systems",
                        tags = "Deadlock, OS, Important",
                        isPinned = true
                    )
                )
                dao.insertNote(
                    NoteEntity(
                        id = 2,
                        title = "Amazon Leadership Principles - STAR Framework Notes",
                        content = "Customer Obsession: Mention project where I prioritized user experience.\nOwnership: Mention resolving production build bug single handedly.\nBias for Action: Quick decision making during college hackathon.",
                        subject = "HR Preparation",
                        companyName = "Amazon",
                        tags = "Behavioral, Amazon, HR",
                        isPinned = false,
                        isFavorite = true
                    )
                )

                // Initial Interview Experiences
                dao.insertExperience(
                    InterviewExperienceEntity(
                        id = 1,
                        companyName = "Amazon",
                        role = "SDE-1 (Campus Placement)",
                        studentName = "Rohan Sharma",
                        college = "IIT Roorkee",
                        year = 2024,
                        result = "Selected",
                        numberOfRounds = 4,
                        difficulty = "Hard",
                        experienceText = "Round 1 (OA): 2 coding questions on DP and Strings + 15 Behavioral questions.\nRound 2 (Tech 1): Graph problem on Shortest Path with constraints. Interviewer gave hints and wanted clean code.\nRound 3 (Tech 2): LRU Cache design and OOP principles.\nRound 4 (Bar Raiser): Deep dive into my college project and 3 Leadership Principle questions using STAR format.",
                        topicsAsked = "Dynamic Programming, Graphs, System Design, STAR Method",
                        date = "Dec 2024",
                        helpfulVotes = 24
                    )
                )
                dao.insertExperience(
                    InterviewExperienceEntity(
                        id = 2,
                        companyName = "Microsoft",
                        role = "Software Engineer",
                        studentName = "Ananya Patel",
                        college = "NIT Trichy",
                        year = 2024,
                        result = "Selected",
                        numberOfRounds = 3,
                        difficulty = "Medium",
                        experienceText = "OA was on Mettl platform with 3 questions. Round 1 focused heavily on Binary Trees (Lowest Common Ancestor) and recursion edge cases. Round 2 asked OS concepts (Deadlocks & Virtual Memory) followed by a String matching problem.",
                        topicsAsked = "Binary Trees, OS, Strings",
                        date = "Jan 2025",
                        helpfulVotes = 18
                    )
                )
            }
        }
    }
}
