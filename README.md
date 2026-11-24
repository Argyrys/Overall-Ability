# OAA Living Resume (Android)

A multi-year student evaluation & career-readiness app inspired by the OAA system from
*Classroom of the Elite*, tailored for Indian college students and recruiters.

**Stack:** Kotlin · Coroutines + StateFlow · Jetpack Compose (Material 3) · MVVM ·
Navigation Compose (5 bottom tabs) · Custom `Canvas` radar chart · ZXing QR codes ·
Supabase-ready repository pattern (Postgrest + Auth + Storage) · pre-populated
realistic Indian-college mock data (B.Tech CSE "Batch of 2027").

---

## Run it immediately

1. Open the `OAALivingResume` folder in **Android Studio** (Ladybug or newer).
2. Let Gradle sync (AGP 8.7.3 · Kotlin 2.2.21 · Gradle 8.9 · JDK 21).
3. Press **Run** on any API 26+ device/emulator.

No backend or keys are required - sign in on the login screen with the
one-tap **CONTINUE AS DEMO STUDENT** button (or any roster email such as
`aarav.sharma@vit.edu` / any password of 4+ characters). The session persists,
and the data comes from the bundled mock backend
(`app/src/main/java/com/oaa/livingresume/data/mock/MockData.kt`).

Run the engine tests:

```powershell
.\gradlew.bat testDebugUnitTest
```

---

## What's new in v1.1

- **Mock login gate** - animated HUD sign-in (spinning-diamond logo), demo
  one-tap button, persistent session, sign-out from the Home header.
- **PDF resume export** - `ResumePdfRenderer` draws an A4 page (header + grade,
  radar, pillar bars, badges, approved proofs, footer QR) and shares it through
  the system share sheet via `FileProvider`.
- **Semester detail screen** - tap any Timeline row: hero GPA/OAA card, radar
  snapshot, delta vs the previous semester, milestones.
- **Motion & sound** - staggered card entrances, press-scale feedback, rolling
  animated score readout, pulsing radar best-node, NavHost transitions, plus 5
  synthesized WAV effects (click / approve / reject / open / submit) with
  haptics on tabs, verifier decisions, proof submission and the theme toggle.
- **Manual dark/light theme** - sun/moon toggle on the Home header (persisted);
  a theme-aware palette repaints every screen, card and chart.
- **Classroom-of-the-Elite HUD restyle** - cut-corner cards with cyan corner
  brackets, blueprint-grid + scanline backdrop, monospace numerals, Japanese
  HUD accents (総合力 / 学生記録 / 成績表 / 証明), animated giant score readout,
  rank chip, custom cut-corner bottom nav, podium-tinted ranking board and a
  terminal-style verifier queue.

---

## The 5 tabs

| Tab | File | What it does |
|-----|------|--------------|
| **Home** | `ui/screens/home/HomeScreen.kt` | Collectible Student OAA Card: name, branch, grad year, target role, overall grade badge, custom 4-axis radar chart, verified skill badges, "Visible to Batch" switch |
| **Timeline** | `ui/screens/timeline/TimelineScreen.kt` | 4-year living resume: Semester 1 → 8 rail with GPA, per-pillar mini-bars, verified milestones + growth curve |
| **Improve** | `ui/screens/improve/ImproveScreen.kt` | **Private** quest log: lowest pillar, exact points to the next letter tier, role-mapped "OAA Quests" |
| **Batch** | `ui/screens/batch/BatchScreen.kt` | Opt-in leaderboard showing **letter grades only** + FilterChips + balanced hackathon team finder |
| **Share** | `ui/screens/share/ShareScreen.kt` | ZXing QR of the recruiter resume link, native `ACTION_SEND` share, "Submit New Proof" bottom sheet (`ActivityResultContracts.GetContent` for certificates), Verifier Mode approval queue |

### Scoring (engine: `engine/OaaEngine.kt`)

| Pillar | Weight | Evidence |
|--------|--------|----------|
| Academic Ability | 35% | CGPA, lab grades, NPTEL/Coursera |
| Adaptability & Practical Skills | 30% | Hackathons, GitHub, contests, internships |
| Social Contribution & Leadership | 20% | Club roles, fest organising, mentoring, volunteering |
| Physical & Consistency | 15% | Attendance %, sports, NCC/NSS, on-time submissions |

Letter grades: **90+ A+ · 80+ A · 70+ B+ · 60+ B · 50+ C · 40+ D · <40 E**.

Approving a proof in **Verifier Mode** adds its points to the pillar (capped at 100),
which instantly recalculates the weighted overall score, the letter grade, the radar
chart, the leaderboard and the generated quests.

---

## Supabase backend (optional)

The repository pattern keeps the UI backend-agnostic:

```
OaaRepository (interface)
├── MockOaaRepository        <- used by default (in-memory, live recalculations)
└── SupabaseOaaRepository    <- used automatically when credentials exist
```

1. Create a Supabase project, then run:

```sql
-- profiles / students
create table public.students (
  id text primary key,
  name text not null,
  roll_no text not null,
  branch text not null,
  college text not null,
  grad_year int not null,
  target_role text not null,
  email text not null,
  slug text unique not null,
  cgpa numeric not null,
  pillar_scores jsonb not null,      -- {"ACADEMIC":84,"ADAPTABILITY":76,...}
  badges jsonb not null default '[]',
  semesters jsonb not null default '[]',
  opted_in_public boolean not null default false,
  headline text default '',
  skills jsonb not null default '[]',
  github text default '',
  linkedin text default ''
);

-- proof submissions / verifier queue
create table public.proofs (
  id text primary key,
  student_id text references public.students(id) on delete cascade,
  student_name text not null,
  title text not null,
  link text not null,
  pillar text not null,
  points int not null,
  status text not null default 'PENDING',   -- PENDING | APPROVED | REJECTED
  submitted_at text not null,
  attachment text,
  verifier_note text
);

alter table public.students enable row level security;
alter table public.proofs enable row level security;

-- public resumes + opt-in batch leaderboard are readable by anyone
create policy "students readable" on public.students for select using (true);
create policy "proofs readable" on public.proofs for select using (true);
create policy "students insert" on public.students for insert with check (true);
create policy "proofs insert" on public.proofs for insert with check (true);
create policy "proofs update by verifier" on public.proofs for update using (true);
create policy "students update" on public.students for update using (true);

-- storage bucket for certificates
insert into storage.buckets (id, name, public) values ('proofs', 'proofs', true);
```

2. Fill in `local.properties` (never commit it):

```properties
SUPABASE_URL=https://YOUR-PROJECT.supabase.co
SUPABASE_KEY=YOUR-ANON-KEY
```

3. `OaaRepository.create()` switches to `SupabaseOaaRepository` automatically -
no UI changes required. `signIn(email, password)` and `uploadCertificate(...)`
are ready for wiring into a future login screen / proof form upload.

---

## Project structure

```
app/src/main/java/com/oaa/livingresume/
├── MainActivity.kt
├── engine/OaaEngine.kt                  # scoring, grades, points-to-next-tier, quests, team finder
├── data/
│   ├── model/Models.kt                  # Pillar, Grade, Student, SemesterEntry, ProofSubmission, Quest...
│   ├── mock/MockData.kt                 # realistic Indian college data (10 students, 8 semesters, queue)
│   ├── repository/OaaRepository.kt      # interface + factory
│   ├── repository/MockOaaRepository.kt  # in-memory backend with live recalculation
│   └── supabase/SupabaseOaaRepository.kt# Postgrest + Auth + Storage (backend-ready)
├── viewmodel/OaaViewModel.kt            # StateFlow<OaaUiState>, MVVM actions
├── ui/
│   ├── theme/                           # Deep Navy #090D16, Cyan #00E5FF, Gold #FFD54F
│   ├── components/RadarChartCanvas.kt   # custom Compose Canvas 4-axis radar chart
│   ├── components/GrowthLineChart.kt    # semester growth curve
│   ├── components/QrCode.kt             # ZXing QR generator
│   ├── components/Common.kt             # HudCard, GradeBadge, PillarTag, ScoreProgressBar...
│   ├── navigation/OaaApp.kt             # Scaffold + bottom bar (5 tabs)
│   └── screens/{home,timeline,improve,batch,share}/
```
