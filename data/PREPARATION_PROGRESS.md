# Preparation progress

Daily tasks start unchecked. Checking a task on Home, Planner or Calendar stores
the exact selected state in Room; unchecking reverses it. Today's summary counts
only tasks scheduled for the device's current date. Duration is the scheduled
duration of completed tasks, not a measured study timer.

Preparation Readiness is an activity score:

`50 × completed tasks / all scheduled tasks + 50 × min(distinct solved PYQs / 20, 1)`

The task part is zero with no scheduled tasks. The score starts at zero, reaches
100 when all planned tasks and at least 20 distinct questions are completed, and
updates when a completion is checked or unchecked. Repeated LeetCode IDs across
company lists count once. Subject rows show real completed/total task counts.
This score describes practice activity, not assessed knowledge or hiring odds.

PYQs open in **To solve**. Marking one solved removes it from that list and puts
it in **Completed**, retaining the current company/search filters. Marking it
unsolved moves it back. Both status lists support bookmarks, search and filters.
Completion survives app restarts and offline catalog refreshes.

Database version 3 removes exact old demo task rows and unmodified sample question
rows; user-created tasks and imported question completion/bookmarks/notes remain.
Fresh installations do not insert a demo user, study tasks or solved questions.
Company checklists also start unchecked and persist explicitly selected items.
