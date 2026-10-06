# Offline company question catalog

All company CSVs from [snehasishroy/leetcode-companywise-interview-questions](https://github.com/snehasishroy/leetcode-companywise-interview-questions) are downloaded in `csv/`. `source.zip` retains the source snapshot and `commit.json` records the pinned revision. The upstream README dates its snapshot to 12 July 2026. See `manifest.json` for exact counts and checksums.

Run `python tools/prepare_company_questions.py` from the project root to rebuild the compressed app asset. The conversion validates every CSV row, combines duplicate company/problem pairs across all time windows, retains each window's frequency separately and generates stable IDs. It includes companies which lack an `all.csv` file. Topics, exact interview dates, roles and rounds are not supplied by the source and are not invented. Dataset rows are labelled coding interview practice, distinct from the existing sample records.

The source asset is `app/src/main/assets/company_questions/questions.json.gz`; Android's asset merger packages it as `company_questions/questions.json` inside the APK. On first use it imports into Room on a background coroutine; import rows and completion metadata are committed in one transaction. Existing user tables and question state are preserved with a non-destructive v1→v2 migration. No GitHub or Groq request is needed for browsing or searching. Opening a LeetCode problem requires a browser/network. Recency filters refer to the snapshot date, not the current date.

The upstream scraper is not run. The repository does not include an explicit license file in this downloaded revision. Source provenance is retained here and displayed in the app; it is not described as official company assessment papers or an endorsed dataset.

## Using the app

Open **Profile → Company PYQs & Questions**, then search for a company or question. Alternatively, use the Home search icon, enter a company and tap **Browse all matching company questions**. Search accepts multiple keywords in either order, such as `Amazon Two Sum` or `two sum Amazon`; matching related title variants are included. **Filters** opens company selection, time window, difficulty, topic and sorting options. Dataset questions have the topic **Coding**, since the CSVs do not include topic tags. Use **Bookmarked** to revisit saved questions and **Load more questions** for additional results.

Build: `gradlew.bat :app:assembleDebug`. The APK is `app/build/outputs/apk/debug/app-debug.apk`. First-run catalog loading is automatic; no manual CSV copying is required on the phone.

## Validation

Every one of the 39,904 source rows was checked against the bundled catalog, including its link, title and time-window frequency. Emulator checks cover importing all 17,826 entries, preserving bookmarks/solved flags/notes during re-import, company and alias search, literal wildcard handling, difficulty/recency filters, result paging, UI controls, non-destructive migration of questions/tasks/notes, and existing bottom navigation.

A real app check with Wi-Fi and mobile data disabled found 139 NVIDIA questions through Home search and one NVIDIA question in the 30-day snapshot window. Both network settings were restored afterward. Screenshots are `offline-nvidia-search.png` and `offline-nvidia-recent.png`; the reproducible check is `tools/check_offline_pyqs.py`.
