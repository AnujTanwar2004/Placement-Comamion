# Offline PYQ integration validation

- Debug app and instrumentation APKs built successfully.
- All 1,642 downloaded CSVs validated: 39,904 source rows, 659 companies, 17,826 unique company/problem entries. Every source title, problem URL and per-window frequency matches the bundled asset.
- Twelve distinct instrumentation checks verified across the complete suite and a targeted rerun: five catalog/search/migration checks, two PYQ interface checks and five existing navigation checks.
- The last complete suite passed eleven checks. Its remaining assertion assumed no title contained `%`; the dataset actually contains four such entries. The assertion was corrected to verify literal matching, then that entire search/alias test passed in the targeted rerun (`test-results-targeted.txt`).
- The migration check recreated a version-one database, upgraded it using the registered Room migration and confirmed that saved task, note, solved status, bookmark and user-note data survived. Importing the catalog then preserved the existing question alongside all dataset entries.
- A separate real-app check disabled emulator Wi-Fi and mobile data, searched NVIDIA from Home global search, opened the full browser and confirmed 139 matches. Selecting the 30-day snapshot window returned one match and displayed its LeetCode action. Network settings were restored afterward.
- The app uses the pinned 12 July 2026 snapshot. Browsing/searching/filtering is local; opening external problem statements requires a browser/network.
