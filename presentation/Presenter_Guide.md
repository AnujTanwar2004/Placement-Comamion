# Placement Companion — presenter guide

**Students:** Anuj Tanwar (205124017), Kratish Mewada (205124045).

**Before submission:** replace the suggested individual contribution split with actual work. Keep an offline working demo video ready.

**Timing:** 12 content slides in 7 minutes, live demonstration in 3 minutes, individual viva in 5 minutes. The demonstration schedule is retained in this guide rather than as an overview slide.

## Slide 1: Project title & team

Presenter: Anuj. Time: 20 seconds.
Introduce both students, roll numbers, institute and laboratory. Describe the application as a placement preparation companion.
Timing: slide discussion totals 7 minutes, followed by a 3-minute live demo. Individual viva is a separate 5-minute component.
Logo: unaltered official NIT Tiruchirappalli emblem downloaded from https://www.nitt.edu/home/about/High-Resolution-Emblem.png; source page https://www.nitt.edu/home/about/. No endorsement is implied.

## Slide 2: Problem & objectives

Presenter: Anuj. Time: 35 seconds.
Explain the practical difficulty of switching between schedules, notes and company resources. The project aims to make preparation manageable through a single mobile workflow. Avoid claiming measured academic or placement improvements; no user study has been conducted.
Viva prompt: Why is local persistence useful in a student preparation app?

## Slide 3: Market drivers & target users

Presenter: Anuj. Time: 30 seconds.
Primary users are students preparing for campus placements; placement cells and training groups are possible distribution partners. The proposal is to organize preparation alongside existing coding-practice and recruitment platforms.
Evidence: AICTE NEAT describes EdTech support for employability skills, capacity building and learning gaps: https://neateoi.aicte-india.org/. NIT Trichy Training and Placement describes placement coordination and individual career-interest counselling: https://www.nitt.edu/home/academics/departments/tp/.
Inference: these activities provide a plausible setting for a preparation companion. They do not demonstrate adoption of this app, willingness to pay, market size or improved placement outcomes. Target users and channels are proposed; no market survey or traction data is available.

## Slide 4: Business drivers & proposed sustainability

Presenter: Anuj. Time: 30 seconds.
The business hypothesis connects student value to repeated use: a daily planning routine, retained notes and personalized help. Student groups and placement cells are proposed pilot channels, not existing partnerships.
Potential sustainability options are a free core planner, usage-limited premium AI features, and institutional subscriptions. Billing, subscription limits and institutional dashboards are not in the current app. Pricing and willingness to pay need validation.
Cost drivers would include AI requests, future backend/cloud services, content maintenance and support. Proposed pilot measures: activation after onboarding, weekly retention, completed tasks, feedback quality and API cost per active user. No revenue, adoption, retention or conversion figures have been measured.
Viva prompt: How would you test whether the product is useful before introducing a paid tier?

## Slide 5: Solution & modules

Presenter: Anuj. Time: 35 seconds.
Explain how onboarding establishes preferences, the planner supports daily work, company content guides preparation and AI supplies personalized assistance. Notes/resources/bookmarks remain part of the same preparation workflow.
Bottom tabs: Home, Planner, Companies, Resources and Profile. Secondary destinations include AI Planner, Notes, Calendar, PYQs, Experiences, Search, Doubt Assistant and Mock Interview.
Dashboard contains sample recommendation/progress data; do not describe all metrics as dynamically computed. Company/PYQ seed material is demonstration content, not a verified current recruiting dataset.

## Slide 6: Project tech stack details

Presenter: Kratish. Time: 40 seconds.
Explain the role of each layer rather than only listing library names. Kotlin is the implementation language; Android Studio and Gradle support development and builds. Compose/Material 3 render screens, Navigation manages routes. ViewModels expose StateFlow and use coroutines for database/API work. Room stores structured entities through the DAO, while DataStore stores smaller preference flags. Retrofit defines HTTP calls, OkHttp transports them and Gson converts JSON models; Groq supplies AI replies and plans. Compose UI tests and AndroidJUnit4 are used for instrumentation checks.
Verified against app/build.gradle.kts, gradle/libs.versions.toml and source: Kotlin 2.2.10, Room 2.8.4, min SDK 26, compile/target SDK 35, JVM target 17. Configured Groq model: openai/gpt-oss-120b. WorkManager is a declared dependency with reminder helpers, but end-to-end scheduling has not been verified; do not present full reminders as established functionality.
Viva prompt: Explain the distinction between Room and DataStore, Retrofit and OkHttp, or ViewModel and Repository.

## Slide 7: Technology & architecture

Presenter: Kratish. Time: 40 seconds.
MVVM separates Compose screens from state and data operations. ViewModels expose StateFlow, launch coroutines and use repositories; UI observes changes. Room provides local structured data and DataStore stores preferences/onboarding flags. Retrofit/OkHttp/Gson handle remote HTTP and JSON.
Implementation: Kotlin 2.2.10, Room 2.8.4, compile/target SDK 35, min SDK 26; only mention versions if asked.
Viva prompts: What does a ViewModel preserve? Why use a coroutine? How does StateFlow update a Compose screen? Where are navigation routes defined?

## Slide 8: API & storage workflow

Presenter: Kratish. Time: 40 seconds.
The Groq OpenAI-compatible endpoint is https://api.groq.com/openai/v1/chat/completions and the configured model is openai/gpt-oss-120b. The repository supports askDoubt, generateStudyPlan and mockInterviewFeedback. Do not display any API credential.
The plan request expects JSON and Gson parses it. convertAiPlanToTasks inserts tasks into Room; DAO Flow then refreshes the planner. The current conversion assigns sequential dates and a fixed time slot; improved mapping of AI day numbers and durations is future work.
Room database: placement_companion_db. DataStore: onboarding completion, target company and theme preference flags.
Viva prompts: What is an entity vs DAO? Where is a task inserted? What happens when the API returns an error?

## Slide 9: Important screens

Presenter: Anuj. Time: 30 seconds.
These are real screenshots captured from the project APK on Medium_Phone_API_37.0. Identify the common bottom navigation, dashboard quick actions, planner controls, returned AI roadmap and company search/filter UI. The planner includes a freshly added demo task and seed records; the dashboard recommendation includes sample content.
During the demo, create a fresh task using the actual demonstration date rather than relying on seed dates. The AI result shown is an actual Groq response.

## Slide 10: Testing & challenges

Presenter: Kratish. Time: 40 seconds.
Eight focused instrumentation checks passed during this development session in separate navigation and live API runs. Five Compose tests cover Study shortcut → Home, AI Planner → Home, Planner tab → Home, switching multiple tabs and preserving Planner state. Three Groq repository tests check an exact assistant reply, valid plan fields/nonempty tasks and nonempty interview feedback. The retained latest XML report has 3 tests, 0 failures, 0 errors, 0 skipped. This is targeted verification, not exhaustive testing of all modules.
Build fixes: add Kotlin Android plugin, align Kotlin 2.2.10 / Room 2.8.4. API fix: replace unavailable model and expose readable HTTP errors. Navigation fix: Home must not restore a saved stack that ends on Planner; preserve other tab state.
Validation: task title must be nonblank; blank AI doubt/interview answers are skipped. Numeric AI fields have fallback defaults, but strict range/date validation still needs improvement.
Viva prompts: Explain the navigation bug. What is restoreState? Why test the repository with a real API?

## Slide 11: Team & individual contributions

Presenters: both. Time: 35 seconds.
IMPORTANT: the actual division of work was not provided. The assignments on this slide are suggested, not verified claims of authorship. Replace them with the students’ real work before submission. Both students must understand the entire application and attend individual viva.
Suggested speaking division: Anuj covers slides 1-5 and 9; Kratish covers slides 6-8 and 10; both cover slides 11-12 and the demo. Be ready to identify the exact files each student actually implemented or modified.

## Slide 12: Future work & conclusion

Presenters: both. Time: 45 seconds.
Summarize the implemented native Android app, local database and live AI integration. Future work addresses strict validation, mapping AI day numbers/durations to a calendar, replacing sample dashboard analytics with actual metrics, secure backend API access, cloud synchronization and stronger reminder integration.
Transition to the 3-minute application demo, then each student attends the individual viva. Do not claim cloud sync, full authentication, exhaustive testing or automatic deadline analytics are already implemented.
Slide timing: 20 + 35 + 30 + 30 + 35 + 40 + 40 + 40 + 30 + 40 + 35 + 45 = 420 seconds (7 minutes). Demo = 180 seconds. Total group presentation/demo = 600 seconds (10 minutes).

## Application demonstration and submission checklist

0:00–0:35 — Launch the app, open Planner and Companies, then return to Home.

0:35–1:25 — Show blank-title protection; add a dated task, toggle completion and relaunch to show persistence.

1:25–2:30 — Generate a short AI plan or ask a doubt; show the real response.

2:30–3:00 — Save AI tasks to Planner and explain local storage versus remote AI.

Offline backup: Placement_Companion_Demo_Backup.mp4 (about 90 seconds, without narration). Use Demo_Video_Cues.txt to explain the recording. Verify local playback on the presentation laptop.

Bring the common PPT, complete source, working APK/app, laptop/mobile device, configuration/assets/database and offline demo video. Never display credentials. Use the current date for new tasks and confirm internet access for live AI.

Market context sources: AICTE NEAT — https://neateoi.aicte-india.org/; NIT Trichy Training & Placement — https://www.nitt.edu/home/academics/departments/tp/. Market positioning, business models and pilot metrics in this deck are proposals, not measured results.
