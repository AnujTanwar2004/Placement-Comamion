package com.example.placementcompanion.presentation.navigation

sealed class Screen(val route: String, val title: String = "") {
    object Onboarding : Screen("onboarding", "Welcome")
    object Home : Screen("home", "Dashboard")
    object Preparation : Screen("preparation", "Study Planner")
    object Companies : Screen("companies", "Company Directory")
    object CompanyDetail : Screen("company_detail/{companyId}", "Company Details") {
        fun createRoute(companyId: Long) = "company_detail/$companyId"
    }
    object Resources : Screen("resources", "Study Material")
    object Profile : Screen("profile", "Profile")
    
    // Secondary Screens
    object AiPlanner : Screen("ai_planner", "AI Plan Mode")
    object PyqList : Screen("pyqs", "Company PYQs")
    object Experiences : Screen("experiences", "Interview Experiences")
    object Notes : Screen("notes", "My Notes")
    object Calendar : Screen("calendar", "Placement Calendar")
    object SearchBookmarks : Screen("search_bookmarks", "Search & Saved")
    object MockInterview : Screen("mock_interview", "AI Mock Interview")
}
