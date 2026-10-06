# Placement Companion laboratory presentation

Submitted by Anuj Tanwar (205124017) and Kratish Mewada (205124045).

- `Placement_Companion_Lab_Presentation.pptx`: editable 12-slide deck with speaker notes, including market drivers, business drivers and detailed technology roles.
- `Placement_Companion_Lab_Presentation.pdf`: matching PDF for sharing and backup.
- `Placement_Companion_Demo_Backup.mp4`: approximately 90-second offline working demo, without voiceover.
- `Presenter_Guide.md`: suggested speakers, timing, implementation explanations and viva prompts.
- `Demo_Video_Cues.txt`: timestamps to help explain the recorded demo.

Confirm the actual division of work before submission: slide 11 contains a clearly labelled suggested contribution split. Plan for 7 minutes of slides plus 3 minutes of live demonstration, followed by the separate 5-minute individual viva component. Demo/submission planning has been removed from the visible slides; its instructions remain in the presenter guide.

The recording shows task entry and blank-title protection, a saved task, task completion controls, returning to Home, persistence after relaunch, a real Groq response, saving AI tasks, and company browsing. Keep it downloaded on the presentation laptop and check local playback before class.

The NIT Trichy emblem on the cover is the unaltered official asset from https://www.nitt.edu/home/about/High-Resolution-Emblem.png. Actual app screenshots were captured on the Android emulator. Presentation content was checked against this project's source and the development-session verification. API credentials are excluded from the presentation.

The PowerPoint and PDF were rendered using Microsoft PowerPoint. All 12 slides have notes; a final text-fit check found no overflowing text. Market context sources are linked in the slides/notes. Targeting, revenue models, channels and pilot metrics are labelled as proposals. The MP4 was normalized to 30 fps and successfully decoded in full.

To rebuild the deck: `python presentation/build_presentation.py` from the project directory. The builder uses dependencies in `.presentation-tools`. Rendering requires installed Microsoft PowerPoint and `presentation/render_presentation.ps1`.
