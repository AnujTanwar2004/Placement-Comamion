"""Build an editable, evidence-based laboratory project presentation."""
from pathlib import Path
import sys
import re
ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT / '.presentation-tools'))
from pptx import Presentation
from pptx.util import Inches, Pt
from pptx.dml.color import RGBColor
from pptx.enum.shapes import MSO_SHAPE, MSO_CONNECTOR
from pptx.enum.text import PP_ALIGN, MSO_ANCHOR
from PIL import Image

OUT = ROOT / 'presentation'
ASSETS = OUT / 'assets'
prs = Presentation()
prs.slide_width, prs.slide_height = Inches(13.333333), Inches(7.5)
NAVY='172B46'; TEAL='087F8C'; INK='263C51'; MUTED='64778A'; BG='F5F7FA'; WHITE='FFFFFF'; PALE='E6F2F2'; GOLD='EDB955'
notes_export=[]
TOTAL_SLIDES=12

def color(v): return RGBColor.from_string(v)
def box(s,x,y,w,h,fill=WHITE,line=None,rounded=False):
    sh=s.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE if rounded else MSO_SHAPE.RECTANGLE, Inches(x), Inches(y), Inches(w), Inches(h))
    sh.fill.solid(); sh.fill.fore_color.rgb=color(fill)
    if line: sh.line.color.rgb=color(line)
    else: sh.line.fill.background()
    if rounded:
        sh.adjustments[0]=0.12
    return sh
def text(s,x,y,w,h,content,size=22,bold=False,fg=INK,align=PP_ALIGN.LEFT):
    sh=s.shapes.add_textbox(Inches(x), Inches(y), Inches(w), Inches(h))
    tf=sh.text_frame; tf.clear(); tf.word_wrap=True
    tf.margin_left=tf.margin_right=0; tf.margin_top=tf.margin_bottom=0
    for i,line in enumerate(content.split('\n')):
        p=tf.paragraphs[0] if i==0 else tf.add_paragraph()
        p.text=line; p.alignment=align; p.font.name='Aptos'; p.font.size=Pt(size)
        p.font.bold=bold; p.font.color.rgb=color(fg); p.space_after=Pt(8)
    return sh
def note(s,title,body):
    s.notes_slide.notes_text_frame.text=body
    notes_export.append((title,body))
def slide(title,kicker,num):
    s=prs.slides.add_slide(prs.slide_layouts[6]); s.background.fill.solid(); s.background.fill.fore_color.rgb=color(BG)
    box(s,0,0,.16,7.5,TEAL)
    text(s,.55,.35,12,.28,kicker.upper(),11,True,TEAL)
    text(s,.55,.85,12,0.7,title,32,True,NAVY)
    box(s,.55,6.99,12.2,.012,'D7E0E8')
    text(s,.55,7.12,11,.2,'PLACEMENT COMPANION   /   MOBILE APPLICATION DEVELOPMENT LABORATORY',9,False,MUTED)
    text(s,12.05,7.08,.7,.28,f'{len(prs.slides):02d} / {TOTAL_SLIDES}',10,True,MUTED,PP_ALIGN.RIGHT)
    return s
def card(s,x,y,w,h,title,body,tag=None):
    box(s,x,y,w,h,WHITE,rounded=True)
    if tag:
        text(s,x+.24,y+.2,w-.48,.33,tag,12,True,TEAL)
        yy=y+.62
    else: yy=y+.22
    text(s,x+.24,yy,w-.48,.65,title,22,True,NAVY)
    text(s,x+.24,yy+.66,w-.48,h-(yy-y)-.8,body,18,False,MUTED)
def arrow(s,x1,y1,x2,y2):
    sh=s.shapes.add_connector(MSO_CONNECTOR.STRAIGHT, Inches(x1), Inches(y1), Inches(x2), Inches(y2))
    sh.line.color.rgb=color(TEAL); sh.line.width=Pt(2.5)
    # Filled terminal triangle keeps the diagram editable in PowerPoint.
    tip=s.shapes.add_shape(MSO_SHAPE.CHEVRON, Inches(x2-.10), Inches(y2-.08), Inches(.16), Inches(.16))
    tip.fill.solid(); tip.fill.fore_color.rgb=color(TEAL); tip.line.fill.background()
def phone(s,path,x,y,h):
    im=Image.open(path); w=h*im.width/im.height
    box(s,x-.055,y-.055,w+.11,h+.11,NAVY,rounded=True)
    s.shapes.add_picture(str(path), Inches(x), Inches(y), width=Inches(w), height=Inches(h))
    return w

# 1 — cover
s=prs.slides.add_slide(prs.slide_layouts[6]); s.background.fill.solid(); s.background.fill.fore_color.rgb=color(WHITE)
box(s,0,0,13.333,0.13,TEAL)
s.shapes.add_picture(str(ASSETS/'nit-trichy-emblem.png'), Inches(.6), Inches(.42), height=Inches(1.08))
text(s,1.95,.63,10.7,.35,'NATIONAL INSTITUTE OF TECHNOLOGY, TIRUCHIRAPPALLI',19,True,NAVY)
text(s,1.95,1.06,10.7,.3,'MOBILE APPLICATION DEVELOPMENT LABORATORY',14,False,TEAL)
text(s,.65,1.87,11.9,1.9,'Placement\nCompanion',44,True,NAVY)
text(s,.68,3.9,11.8,.55,'An Android app for organized, personalized placement preparation',22,False,MUTED)
box(s,.65,4.58,12,1.38,PALE,rounded=True)
text(s,.95,4.86,5.7,.45,'Anuj Tanwar',25,True,NAVY)
text(s,.95,5.39,5.7,.3,'Roll No. 205124017',18,False,MUTED)
text(s,7,4.86,5.3,.45,'Kratish Mewada',25,True,NAVY)
text(s,7,5.39,5.3,.3,'Roll No. 205124045',18,False,MUTED)
text(s,.68,6.56,12,.4,'PROJECT IMPLEMENTATION  •  GROUP PRESENTATION  •  INDIVIDUAL VIVA',13,True,TEAL)
note(s,'Project title & team','Presenter: Anuj. Time: 20 seconds.\nIntroduce both students, roll numbers, institute and laboratory. Describe the application as a placement preparation companion.\nTiming: slide discussion totals 7 minutes, followed by a 3-minute live demo. Individual viva is a separate 5-minute component.\nLogo: unaltered official NIT Tiruchirappalli emblem downloaded from https://www.nitt.edu/home/about/High-Resolution-Emblem.png; source page https://www.nitt.edu/home/about/. No endorsement is implied.')

# 2 — problem and measurable goals
s=slide('From scattered preparation to a daily routine','Problem statement & objectives',2)
card(s,.6,1.92,5.65,3.64,'The student problem','Company information, practice questions and notes are scattered.\nStudents struggle to prioritize weak topics and maintain a study schedule.', '01  /  CONTEXT')
card(s,6.5,1.92,6.22,3.64,'Our objectives','Organize preparation in one Android app.\nTrack tasks and retain study material locally.\nPersonalize guidance using AI.', '02  /  GOALS')
box(s,.6,5.83,12.12,.72,NAVY,rounded=True)
text(s,.88,6.02,11.56,.35,'Target users: students preparing for campus placements and interviews',20,True,WHITE)
note(s,'Problem & objectives','Presenter: Anuj. Time: 40 seconds.\nExplain the practical difficulty of switching between schedules, notes and company resources. The project aims to make preparation manageable through a single mobile workflow. Avoid claiming measured academic or placement improvements; no user study has been conducted.\nViva prompt: Why is local persistence useful in a student preparation app?')

# Market context — evidence and proposed positioning are separated.
s=slide('Career readiness creates a practical student need','Market drivers & target users',3)
card(s,.6,1.94,3.87,3.35,'Career readiness','Placement preparation needs technical practice, interview confidence and a consistent routine.', 'NEED')
card(s,4.72,1.94,3.87,3.35,'Personal guidance','AICTE NEAT supports EdTech that addresses learning gaps and employability skills.', 'CONTEXT')
card(s,8.84,1.94,3.87,3.35,'Campus access','Start with placement-season students; reach them through peer groups and placement cells.', 'PROPOSED CHANNEL')
box(s,.6,5.55,12.12,.76,PALE,rounded=True)
text(s,.88,5.75,11.56,.38,'Positioning: a daily preparation companion alongside practice and recruitment platforms.',18,True,TEAL)
src=text(s,.65,6.56,12,.22,'Context: AICTE NEAT; NIT Trichy Training & Placement. Targeting and positioning are project proposals.',10,False,MUTED)
src.text_frame.paragraphs[0].runs[0].hyperlink.address='https://neateoi.aicte-india.org/'
note(s,'Market drivers & target users','Presenter: Anuj. Time: 30 seconds.\nPrimary users are students preparing for campus placements; placement cells and training groups are possible distribution partners. The proposal is to organize preparation alongside existing coding-practice and recruitment platforms.\nEvidence: AICTE NEAT describes EdTech support for employability skills, capacity building and learning gaps: https://neateoi.aicte-india.org/. NIT Trichy Training and Placement describes placement coordination and individual career-interest counselling: https://www.nitt.edu/home/academics/departments/tp/.\nInference: these activities provide a plausible setting for a preparation companion. They do not demonstrate adoption of this app, willingness to pay, market size or improved placement outcomes. Target users and channels are proposed; no market survey or traction data is available.')

s=slide('Student value must translate into repeat usage','Business drivers & proposed sustainability',4)
card(s,.6,1.94,5.94,2.08,'Value & retention','One place for tasks, notes and AI help.\nSaved progress can support a daily routine.')
card(s,6.8,1.94,5.92,2.08,'Adoption channels','Pilot with student groups and placement cells.\nUse feedback to improve the preparation flow.')
card(s,.6,4.25,5.94,2.08,'Possible business model','Free core planning; optional paid AI features.\nExplore institutional plans after a campus pilot.')
card(s,6.8,4.25,5.92,2.08,'Costs & success measures','AI usage, support and future hosting costs.\nTrack retention, task completion and cost per user.')
text(s,.65,6.64,12,.22,'Proposed roadmap: pricing, subscriptions, institutional plans and analytics are not implemented.',11,True,TEAL)
note(s,'Business drivers & proposed sustainability','Presenter: Anuj. Time: 30 seconds.\nThe business hypothesis connects student value to repeated use: a daily planning routine, retained notes and personalized help. Student groups and placement cells are proposed pilot channels, not existing partnerships.\nPotential sustainability options are a free core planner, usage-limited premium AI features, and institutional subscriptions. Billing, subscription limits and institutional dashboards are not in the current app. Pricing and willingness to pay need validation.\nCost drivers would include AI requests, future backend/cloud services, content maintenance and support. Proposed pilot measures: activation after onboarding, weekly retention, completed tasks, feedback quality and API cost per active user. No revenue, adoption, retention or conversion figures have been measured.\nViva prompt: How would you test whether the product is useful before introducing a paid tier?')

# 3 — modules
s=slide('One app, six connected preparation modules','Proposed solution & major modules',3)
modules=[('Personalization','Target role, subjects and daily study hours.'),('Study planning','Tasks, timetable and completion tracking.'),('Company preparation','Companies, PYQs and interview experiences.'),('Study material','Notes, resources, search and bookmarks.'),('AI assistance','Roadmaps, doubt answers and interview feedback.'),('Daily navigation','Home, calendar and profile access.')]
for i,(t,b) in enumerate(modules):
    card(s,.6+(i%3)*4.12,1.93+(i//3)*2.3,3.87,2.08,t,b,f'{i+1:02d}')
note(s,'Solution & modules','Presenter: Anuj. Time: 45 seconds.\nExplain how onboarding establishes preferences, the planner supports daily work, company content guides preparation and AI supplies personalized assistance. Notes/resources/bookmarks remain part of the same preparation workflow.\nBottom tabs: Home, Planner, Companies, Resources and Profile. Secondary destinations include AI Planner, Notes, Calendar, PYQs, Experiences, Search, Doubt Assistant and Mock Interview.\nDashboard contains sample recommendation/progress data; do not describe all metrics as dynamically computed. Company/PYQ seed material is demonstration content, not a verified current recruiting dataset.')

# Dedicated stack details before the architecture walkthrough.
s=slide('What each technology contributes to the app','Project tech stack details',6)
columns=[.62,3.04,7.56]
widths=[2.42,4.52,5.14]
box(s,.62,1.91,12.08,.6,NAVY,rounded=True)
for x,w,label in zip(columns,widths,['LAYER','TECHNOLOGY / TOOL','ROLE IN THIS PROJECT']):
    text(s,x+.18,2.08,w-.36,.28,label,13,True,WHITE)
stack=[
    ('Language & build','Kotlin · Android Studio · Gradle','Native Android code, builds and APK generation'),
    ('UI & navigation','Compose · Material 3 · Navigation','Declarative screens, controls and route handling'),
    ('State & async work','ViewModel · Coroutines · StateFlow','Screen state, background calls and reactive updates'),
    ('Local persistence','Room / DAO · DataStore','8 structured entities; preferences and onboarding'),
    ('Networking & AI','Retrofit · OkHttp · Gson · Groq','HTTP requests, JSON parsing and AI responses'),
    ('Verification','Compose UI tests · AndroidJUnit4','Navigation checks and opt-in live API tests'),
]
for i,row in enumerate(stack):
    yy=2.59+i*.56
    box(s,.62,yy,12.08,.5,WHITE if i%2==0 else PALE,rounded=True)
    for j,(x,w,value) in enumerate(zip(columns,widths,row)):
        text(s,x+.18,yy+.13,w-.36,.32,value,15, j==0, NAVY if j==0 else INK)
text(s,.65,6.25,12,.35,'Android compatibility: min SDK 26 (Android 8.0+)  •  compile / target SDK 35',17,True,TEAL)
note(s,'Project tech stack details','Presenter: Kratish. Time: 40 seconds.\nExplain the role of each layer rather than only listing library names. Kotlin is the implementation language; Android Studio and Gradle support development and builds. Compose/Material 3 render screens, Navigation manages routes. ViewModels expose StateFlow and use coroutines for database/API work. Room stores structured entities through the DAO, while DataStore stores smaller preference flags. Retrofit defines HTTP calls, OkHttp transports them and Gson converts JSON models; Groq supplies AI replies and plans. Compose UI tests and AndroidJUnit4 are used for instrumentation checks.\nVerified against app/build.gradle.kts, gradle/libs.versions.toml and source: Kotlin 2.2.10, Room 2.8.4, min SDK 26, compile/target SDK 35, JVM target 17. Configured Groq model: openai/gpt-oss-120b. WorkManager is a declared dependency with reminder helpers, but end-to-end scheduling has not been verified; do not present full reminders as established functionality.\nViva prompt: Explain the distinction between Room and DataStore, Retrofit and OkHttp, or ViewModel and Repository.')

# Architecture remains a separate technical explanation.
s=slide('UI actions pass through a layered MVVM design','Architecture & responsibility boundaries',7)
card(s,.6,1.91,3.56,4.8,'Why MVVM?','UI renders screen state.\nViewModel owns state and asynchronous work.\nRepository coordinates local and remote data.\nRoom emits saved changes.', 'SEPARATION OF CONCERNS')
for x,t,b in [(4.55,'Compose UI','Screens + user actions'),(7.32,'ViewModel','State + async operations'),(10.09,'Repository','Data access + API calls')]:
    box(s,x,2.46,2.52,1.3,NAVY,rounded=True)
    text(s,x+.17,2.7,2.18,.35,t,20,True,WHITE,PP_ALIGN.CENTER)
    text(s,x+.15,3.18,2.22,.35,b,12,False,'D4E1EB',PP_ALIGN.CENTER)
arrow(s,7.07,3.11,7.28,3.11); arrow(s,9.84,3.11,10.05,3.11)
box(s,4.55,4.35,8.06,1.75,PALE,rounded=True)
text(s,4.83,4.63,7.5,.43,'Two data paths',23,True,TEAL)
text(s,4.83,5.19,7.5,.8,'Local: Room database + DataStore preferences\nRemote: Retrofit → Groq API → parsed response',19,False,INK)
note(s,'Technology & architecture','Presenter: Kratish. Time: 50 seconds.\nMVVM separates Compose screens from state and data operations. ViewModels expose StateFlow, launch coroutines and use repositories; UI observes changes. Room provides local structured data and DataStore stores preferences/onboarding flags. Retrofit/OkHttp/Gson handle remote HTTP and JSON.\nImplementation: Kotlin 2.2.10, Room 2.8.4, compile/target SDK 35, min SDK 26; only mention versions if asked.\nViva prompts: What does a ViewModel preserve? Why use a coroutine? How does StateFlow update a Compose screen? Where are navigation routes defined?')

# 5 — request/storage concrete workflow
s=slide('AI guidance becomes a saved study task','API integration, workflow & data handling',5)
steps=[('1','Set preferences','Company, time budget\nand preparation topics'),('2','Call Groq API','Send a request using\nRetrofit / OkHttp'),('3','Review output','Parse JSON into\nweeks and study tasks'),('4','Save tasks','Convert the plan;\nstore tasks in Room')]
for i,(n,t,b) in enumerate(steps):
    x=.6+i*3.07
    box(s,x,2.08,2.82,2.16,WHITE,rounded=True)
    text(s,x+.23,2.3,.5,.4,n,25,True,TEAL)
    text(s,x+.23,2.9,2.36,.43,t,20,True,NAVY)
    text(s,x+.23,3.42,2.36,.7,b,16,False,MUTED)
    if i<3: arrow(s,x+2.83,3.14,x+3.02,3.14)
card(s,.6,4.55,6.0,2.08,'Local storage','8 Room entities: user, company, question, task, note, experience, resource and bookmark.')
card(s,6.85,4.55,5.87,2.08,'Response handling','Loading / success / error UI states.\nSaved records work offline; AI needs internet.')
note(s,'API & storage workflow','Presenter: Kratish. Time: 50 seconds.\nThe Groq OpenAI-compatible endpoint is https://api.groq.com/openai/v1/chat/completions and the configured model is openai/gpt-oss-120b. The repository supports askDoubt, generateStudyPlan and mockInterviewFeedback. Do not display any API credential.\nThe plan request expects JSON and Gson parses it. convertAiPlanToTasks inserts tasks into Room; DAO Flow then refreshes the planner. The current conversion assigns sequential dates and a fixed time slot; improved mapping of AI day numbers and durations is future work.\nRoom database: placement_companion_db. DataStore: onboarding completion, target company and theme preference flags.\nViva prompts: What is an entity vs DAO? Where is a task inserted? What happens when the API returns an error?')

# 6 — authentic screenshots
s=slide('Important screens in the working application','Actual Android emulator captures',6)
for i,(fn,t,cap) in enumerate([('home','Home','Quick actions & progress'),('planner-demo','Study planner','Fresh task entry'),('ai-output','AI roadmap','Real API output'),('companies','Companies','Search & company cards')]):
    x=.82+i*3.16
    w=phone(s,ASSETS/f'{fn}.png',x,1.95,4.25)
    text(s,x-.25,6.37,w+.5,.31,t,19,True,NAVY,PP_ALIGN.CENTER)
    text(s,x-.45,6.72,w+.9,.25,cap,12,False,MUTED,PP_ALIGN.CENTER)
note(s,'Important screens','Presenter: Anuj. Time: 35 seconds.\nThese are real screenshots captured from the project APK on Medium_Phone_API_37.0. Identify the common bottom navigation, dashboard quick actions, planner controls, returned AI roadmap and company search/filter UI. The planner includes a freshly added demo task and seed records; the dashboard recommendation includes sample content.\nDuring the demo, create a fresh task using the actual demonstration date rather than relying on seed dates. The AI result shown is an actual Groq response.')

# 7 — honest verification and fixes
s=slide('Focused checks verify navigation and live API calls','Validation, testing, results & challenges',7)
box(s,.6,1.92,3.65,1.36,TEAL,rounded=True)
text(s,.88,2.1,3.1,.65,'5 + 3',36,True,WHITE)
text(s,.88,2.79,3.1,.24,'Navigation checks + live API checks',12,False,WHITE)
text(s,.63,3.62,3.52,2.45,'Navigation: Home returns correctly from planner and other tabs.\nAPI: doubt reply, structured study plan and interview feedback received.',19,False,INK)
box(s,4.55,1.92,8.17,4.74,WHITE,rounded=True)
text(s,4.85,2.17,7.57,.44,'Challenges resolved',24,True,NAVY)
fixes=[('Build compatibility','Aligned Kotlin / Room versions and Gradle plugins.'),('Unavailable AI model','Updated the model; verified all three API functions.'),('Home returning to Planner','Corrected saved navigation-state restoration.')]
for i,(t,b) in enumerate(fixes):
    yy=2.88+i*1.04
    text(s,4.85,yy,7.57,.34,t,19,True,TEAL)
    text(s,4.85,yy+.44,7.57,.44,b,17,False,MUTED)
text(s,.64,6.37,3.62,.37,'Validation: empty tasks / answers guarded',11,True,TEAL)
note(s,'Testing & challenges','Presenter: Kratish. Time: 50 seconds.\nEight focused instrumentation checks passed during this development session in separate navigation and live API runs. Five Compose tests cover Study shortcut → Home, AI Planner → Home, Planner tab → Home, switching multiple tabs and preserving Planner state. Three Groq repository tests check an exact assistant reply, valid plan fields/nonempty tasks and nonempty interview feedback. The retained latest XML report has 3 tests, 0 failures, 0 errors, 0 skipped. This is targeted verification, not exhaustive testing of all modules.\nBuild fixes: add Kotlin Android plugin, align Kotlin 2.2.10 / Room 2.8.4. API fix: replace unavailable model and expose readable HTTP errors. Navigation fix: Home must not restore a saved stack that ends on Planner; preserve other tab state.\nValidation: task title must be nonblank; blank AI doubt/interview answers are skipped. Numeric AI fields have fallback defaults, but strict range/date validation still needs improvement.\nViva prompts: Explain the navigation bug. What is restoreState? Why test the repository with a real API?')

# 8 — pending truthful ownership
s=slide('Team delivery and individual explanation','Suggested contribution split — confirm actual ownership',8)
card(s,.6,1.94,5.94,3.0,'Anuj Tanwar  |  205124017','Suggested scope: Compose UI, onboarding, Home, planner screens and navigation.\nExplain the user journey and lead the UI demo.')
card(s,6.8,1.94,5.92,3.0,'Kratish Mewada  |  205124045','Suggested scope: Room / DAO, ViewModels, repositories, Groq API and error handling.\nExplain data flow and technical verification.')
box(s,.6,5.22,12.12,1.27,PALE,rounded=True)
text(s,.89,5.44,11.54,.35,'Shared team contribution',21,True,TEAL)
text(s,.89,5.96,11.54,.32,'Integration, review, testing, demo preparation and overall project understanding.',18,False,INK)
note(s,'Team & individual contributions','Presenters: both. Time: 30 seconds.\nIMPORTANT: the actual division of work was not provided. The assignments on this slide are suggested, not verified claims of authorship. Replace them with the students’ real work before submission. Both students must understand the entire application and attend individual viva.\nSuggested speaking division: Anuj slides 1–3 and 6; Kratish slides 4–5 and 7; both slides 8–10 and the demo. Be ready to identify the exact classes/files each student actually implemented or modified.')

# 10 — future / conclusion
s=slide('A practical foundation for placement preparation','Future enhancement & conclusion',10)
card(s,.6,1.95,7.18,3.68,'Next improvements','Stronger date, time and numeric validation.\nMore accurate AI task scheduling and real progress analytics.\nBackend-managed AI credentials, authentication and cloud sync.\nImproved reminders, accessibility and broader device testing.')
box(s,8.04,1.95,4.68,3.68,NAVY,rounded=True)
text(s,8.39,2.29,3.98,.45,'Project takeaway',24,True,WHITE)
text(s,8.39,3.07,3.98,2.05,'Study planning, local storage and AI guidance in one Android app for students.',24,False,WHITE)
text(s,.65,6.0,12,.53,'Thank you  •  Application demo & individual viva',26,True,TEAL)
note(s,'Future work & conclusion','Presenters: both. Time: 30 seconds.\nSummarize the implemented native Android app, local database and live AI integration. Future work addresses strict validation, mapping AI day numbers/durations to a calendar, replacing sample dashboard analytics with actual metrics, secure backend API access, cloud synchronization and stronger reminder integration.\nTransition to the 3-minute application demo, then each student attends the individual viva. Do not claim cloud sync, full authentication, exhaustive testing or automatic deadline analytics are already implemented.\nSlide timing: 20 + 35 + 30 + 30 + 35 + 40 + 40 + 40 + 30 + 40 + 35 + 45 = 420 seconds (7 minutes). Demo = 180 seconds. Total group presentation/demo = 600 seconds (10 minutes).')

# Keep presentation timing consistent after inserting project content.
timings=[20,35,30,30,35,40,40,40,30,40,35,45]
assert len(prs.slides)==TOTAL_SLIDES==len(timings)
for i,((title,body),seconds) in enumerate(zip(notes_export,timings)):
    body=re.sub(r'Time: \d+ seconds\.',f'Time: {seconds} seconds.',body,count=1)
    body=re.sub(r'Suggested speaking division:[^\n]+', 'Suggested speaking division: Anuj covers slides 1-5 and 9; Kratish covers slides 6-8 and 10; both cover slides 11-12 and the demo. Be ready to identify the exact files each student actually implemented or modified.',body)
    notes_export[i]=(title,body)
    prs.slides[i].notes_slide.notes_text_frame.text=body

prs.core_properties.title='Placement Companion — Mobile Application Development Laboratory'
prs.core_properties.subject='Project implementation, group presentation and individual viva'
prs.core_properties.author='Anuj Tanwar (205124017); Kratish Mewada (205124045)'
prs.core_properties.keywords='Android, Kotlin, Jetpack Compose, Room, Groq, placement preparation'
prs.core_properties.comments='12-slide academic presentation with real screenshots and speaker notes. Confirm suggested individual contributions before submission.'
target=OUT/'Placement_Companion_Lab_Presentation.pptx'
prs.save(target)
notes=['# Placement Companion — presenter guide','', '**Students:** Anuj Tanwar (205124017), Kratish Mewada (205124045).', '', '**Before submission:** replace the suggested individual contribution split with actual work. Keep an offline working demo video ready.', '', '**Timing:** 12 content slides in 7 minutes, live demonstration in 3 minutes, individual viva in 5 minutes. The demonstration schedule is retained in this guide rather than as an overview slide.', '']
for i,(title,body) in enumerate(notes_export,1): notes.extend([f'## Slide {i}: {title}','',body,''])
notes.extend(['## Application demonstration and submission checklist','', '0:00–0:35 — Launch the app, open Planner and Companies, then return to Home.', '', '0:35–1:25 — Show blank-title protection; add a dated task, toggle completion and relaunch to show persistence.', '', '1:25–2:30 — Generate a short AI plan or ask a doubt; show the real response.', '', '2:30–3:00 — Save AI tasks to Planner and explain local storage versus remote AI.', '', 'Offline backup: Placement_Companion_Demo_Backup.mp4 (about 90 seconds, without narration). Use Demo_Video_Cues.txt to explain the recording. Verify local playback on the presentation laptop.', '', 'Bring the common PPT, complete source, working APK/app, laptop/mobile device, configuration/assets/database and offline demo video. Never display credentials. Use the current date for new tasks and confirm internet access for live AI.', '', 'Market context sources: AICTE NEAT — https://neateoi.aicte-india.org/; NIT Trichy Training & Placement — https://www.nitt.edu/home/academics/departments/tp/. Market positioning, business models and pilot metrics in this deck are proposals, not measured results.', ''])
(OUT/'Presenter_Guide.md').write_text('\n'.join(notes),encoding='utf-8')
print(f'Created {target.name}: {len(prs.slides)} slides with speaker notes.')
