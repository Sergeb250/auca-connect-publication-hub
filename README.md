# AUCA Connect Publication Hub
## Complete Features Specification
### Professional Reference Document — All System Features

> **Institution:** Adventist University of Central Africa (AUCA), Kigali — Rwanda  
> **Project:** AUCA Connect Publication Hub  
> **Document Type:** Full Features Specification (FFS)  
> **Version:** 1.0  
> **Prepared by:** Serge Benit & Akize Israel  
> **Classification:** Internal — University Use Only  
>
> **Industry References:** DSpace · EPrints · JSTOR · ResearchGate · Zenodo · OpenDOAR · Figshare

---

## Table of Contents

1. [System Overview](#1-system-overview)
2. [Design Principles & UX Standards](#2-design-principles--ux-standards)
3. [Authentication & Identity](#3-authentication--identity)
4. [Role-Based Access Control](#4-role-based-access-control)
5. [Dashboard System](#5-dashboard-system)
6. [Project Submission — Student Workflow](#6-project-submission--student-workflow)
7. [Publication Submission — Lecturer Workflow](#7-publication-submission--lecturer-workflow)
8. [Browse & Discovery](#8-browse--discovery)
9. [Project Detail Page](#9-project-detail-page)
10. [Publication Detail Page](#10-publication-detail-page)
11. [Online Reservation System](#11-online-reservation-system)
12. [Memoir Viewer — Security-Critical](#12-memoir-viewer--security-critical)
13. [Moderation & Approval Workflow](#13-moderation--approval-workflow)
14. [AI Assistant — AcademIQ](#14-ai-assistant--academiq)
15. [Notification System](#15-notification-system)
16. [Waitlist System](#16-waitlist-system)
17. [Reports & Analytics](#17-reports--analytics)
18. [Audit Logs](#18-audit-logs)
19. [Admin — User Management](#19-admin--user-management)
20. [Admin — Reservation Settings](#20-admin--reservation-settings)
21. [Admin — Access Schedule](#21-admin--access-schedule)
22. [Admin — Platform Settings](#22-admin--platform-settings)
23. [GitHub Integration](#23-github-integration)
24. [Search Engine](#24-search-engine)
25. [Security Architecture](#25-security-architecture)
26. [Error Handling Standards](#26-error-handling-standards)
27. [Non-Functional Requirements](#27-non-functional-requirements)
28. [Accessibility Standards](#28-accessibility-standards)
29. [Mobile & Responsive Design](#29-mobile--responsive-design)
30. [Complete Page Inventory](#30-complete-page-inventory)

---

## 1. System Overview

### 1.1 What AUCA Connect Is

AUCA Connect Publication Hub is a **secure, university-internal web platform** that enables:

- Final year students to permanently archive their completed project memoirs (PDF) and source code (linked via GitHub)
- Lecturers to submit academic publications (research papers, journal articles, conference papers, book chapters, technical reports)
- Verified university members to discover, search, and access those works through a fully controlled, view-only interface
- Administrators to manage users, reservations, access schedules, moderation queues, and platform configuration

### 1.2 What It Is Not

| Out of Scope | Reason |
|---|---|
| Public internet access | All content is university-internal only |
| File downloads | Memoir PDFs are viewed only — never downloaded |
| Student-to-lecturer submission workflows | Grading and supervision are separate academic processes |
| Internal code hosting | Source code stays on GitHub; only a link and metadata are stored |
| Open access repository | AUCA Connect is a controlled, governed institutional repository |

### 1.3 Industry Benchmarks Used

| System | What We Borrowed |
|---|---|
| **DSpace** | Repository metadata schema, submission workflow, moderation queue design |
| **EPrints** | Version labeling for publications, embargo handling |
| **JSTOR** | Time-limited access session model, watermarking approach |
| **ResearchGate** | Author profile linking, co-author notifications |
| **Zenodo** | DOI linking, GitHub integration for source code archiving |
| **Figshare** | File type handling, metadata completeness scoring |
| **OpenDOAR** | Governance and policy frameworks for institutional repositories |

---

## 2. Design Principles & UX Standards

### 2.1 Core UX Principles

These principles apply to every page, component, and interaction in the system.

#### Principle 1: Role Awareness
Every page renders differently depending on the user's role. A student visiting `/browse` sees project cards with a "Reserve to Access" button. A moderator visiting the same page sees a "Review" button on unpublished items. The system always knows who you are and shows you only what is relevant to your role.

#### Principle 2: Progressive Disclosure
Show only the information needed at each step. The submission wizard shows one step at a time. The project card shows a summary; the detail page shows everything. Modal dialogs are used for confirmations, not for complex workflows.

#### Principle 3: Clear System Status
Every action gives immediate feedback. Uploading a file shows a progress bar and scan status. Submitting a form shows a spinner and then a success or error state. Every async operation has a loading, success, and failure state.

#### Principle 4: Forgiveness
Users can save drafts at any point. Cancellations require confirmation dialogs. Destructive actions (archive, hide, reject) require a reason text field. Nothing irreversible happens without an explicit confirmation step.

#### Principle 5: Accessibility First
All interactive elements are keyboard navigable. All images have alt text. Color is never the sole indicator of state — badges also use icons and text labels. The platform meets WCAG 2.1 AA standards.

### 2.2 Color System

| Token | Hex | Usage |
|---|---|---|
| `--primary-900` | `#0A2D5E` | Sidebar background, page headers |
| `--primary-700` | `#1A4B8C` | Primary buttons, active nav items |
| `--primary-500` | `#2563EB` | Links, interactive elements |
| `--primary-300` | `#93C5FD` | Hover states, light accents |
| `--primary-50` | `#EFF6FF` | Page background, card surfaces |
| `--white` | `#FFFFFF` | Card surfaces, input backgrounds |
| `--success` | `#16A34A` | Published status, approved, available |
| `--warning` | `#D97706` | Pending, partially booked, embargoed |
| `--danger` | `#DC2626` | Rejected, infected, fully booked |
| `--ai-accent` | `#7C3AED` | AcademIQ AI features only |
| `--text-primary` | `#0F172A` | Body text |
| `--text-secondary` | `#475569` | Secondary labels, captions |
| `--border` | `#DBEAFE` | Card borders, input borders |

### 2.3 Typography System

| Element | Font | Size | Weight |
|---|---|---|---|
| Page titles | Plus Jakarta Sans | 28px | 700 |
| Section headings | Plus Jakarta Sans | 20px | 600 |
| Card titles | Plus Jakarta Sans | 16px | 600 |
| Body text | Inter | 14px | 400 |
| Labels & captions | Inter | 12px | 500 |
| Monospace (commit hash, IDs) | JetBrains Mono | 13px | 400 |

### 2.4 Component Standards

#### Status Badges
All status badges use a colored background, a matching icon, and a text label. Color alone is never sufficient.

| Status | Color | Icon | Text |
|---|---|---|---|
| `PUBLISHED` | Green | ✓ | Published |
| `PENDING_APPROVAL` | Yellow | ⏳ | Under Review |
| `DRAFT` | Gray | ✏ | Draft |
| `REJECTED` | Red | ✗ | Rejected |
| `HIDDEN` | Orange | 👁‍🗨 | Hidden |
| `ARCHIVED` | Blue-gray | 🗄 | Archived |
| `AUCA_ONLY` | Blue | 🔒 | AUCA Only |
| `RESTRICTED` | Orange | 🔐 | Restricted |
| `EMBARGOED` | Purple | ⏰ | Embargoed |

#### Loading States
Every data-fetching action shows a skeleton loader (not a spinner) that mirrors the shape of the content about to appear. Skeleton loaders use a shimmer animation in `--primary-50`.

#### Toast Notifications
All success/error toasts appear in the top-right corner, auto-dismiss after 5 seconds, and can be manually dismissed. They include an icon, a short title, and an optional action link.

#### Empty States
Every list view has an empty state with an illustration, a descriptive message, and a primary action button. Example: empty "My Submissions" shows "You haven't submitted a project yet" with a "Submit Your Project" button.

---

## 3. Authentication & Identity

### 3.1 Login Page

**Route:** `/login`  
**Reference:** Google Workspace SSO, Microsoft Azure AD login patterns

The login page is the first impression of the platform. It must communicate institutional credibility and security.

#### Layout
- AUCA institutional logo centered at the top
- Platform name: **AUCA Connect Publication Hub**
- Tagline: *"Preserving Knowledge. Enabling Discovery."*
- Login card centered on a blue-tinted background with subtle geometric pattern

#### Fields

| Field | Type | Validation |
|---|---|---|
| University Email / Campus ID | Text | Required. Must be `@auca.ac.rw` or a valid campus ID format |
| Password | Password | Required. Minimum 8 characters. Show/hide toggle |

#### Features
- **Campus Identity Verification Badge:** Appears below the login button as a blue badge with a shield icon reading *"Secured by AUCA Campus Identity Verification"*
- **Verification Loading State:** After clicking Login, the button changes to a spinning loader with the text *"Verifying campus identity..."* — this communicates the async campus database check in progress
- **Forgot Password:** Link opens a modal where the user enters their university email. A reset link is sent. The modal explains that if the email is not in the AUCA database, no email will be sent (security best practice — do not reveal whether an account exists)
- **Help Desk Contact:** Footer link showing the ICT Help Desk email and phone number. Opens `mailto:` link or displays a tooltip with contact details
- **Two-Factor Authentication (2FA):** Optional. If enabled by admin for a specific role, after password verification the user is shown a TOTP code entry screen. Administrators are recommended to have 2FA enforced.
- **Brute Force Protection:** After 5 failed login attempts, the account is temporarily locked for 15 minutes. A countdown timer is shown. The event is logged in `AuditLog` with `action = LOGIN_FAILED`.
- **Accessibility:** Tab order: Email → Password → Login button → Forgot Password → Help Desk. Full keyboard navigation. Screen reader announcements for error states.

#### Post-Login Routing
| Role | Redirect Destination |
|---|---|
| `STUDENT` | `/dashboard` (student variant) |
| `LECTURER` | `/dashboard` (lecturer variant) |
| `MODERATOR` | `/dashboard` (moderator variant, pending queue prominent) |
| `ADMIN` | `/dashboard` (admin variant, platform health metrics) |

#### Error Messages

| Scenario | Message |
|---|---|
| Email not in AUCA database | `ERR-VER-404: Your ID was not found in the AUCA campus database. Contact ICT support if you believe this is an error.` |
| Wrong password | `ERR-AUTH-401: Incorrect password. You have X attempts remaining before your account is temporarily locked.` |
| Account suspended | `ERR-AUTH-403: Your account has been suspended. Contact the ICT Help Desk for assistance.` |
| Account pending verification | `ERR-AUTH-202: Your campus identity is pending verification. You will be notified by email when access is granted.` |
| Account locked (brute force) | `ERR-AUTH-429: Account temporarily locked. Please try again in 15 minutes.` |

### 3.2 Registration Flow

**Reference:** University student portal self-registration patterns

> **Note:** AUCA Connect uses a **verified registration** model — users cannot self-register freely. The campus database is the source of truth.

#### Registration Steps

1. **Enter university email** — system checks against AUCA campus database immediately (real-time validation)
2. **Set password** — with strength meter (weak/fair/strong/very strong based on length, symbols, numbers)
3. **Campus verification** — system automatically pulls `faculty`, `department`, `academicYear`, and `academicLevel` from the campus database and pre-fills the campus profile
4. **Review campus profile** — user confirms their pre-filled details
5. **Account created** — welcome email sent with login link

#### Password Strength Rules
- Minimum 8 characters: Weak
- 8+ characters + uppercase: Fair
- 12+ characters + uppercase + number: Strong
- 16+ characters + uppercase + number + symbol: Very Strong

### 3.3 Session Management

- Sessions expire after **8 hours** of inactivity
- A warning modal appears 5 minutes before session expiry with options: "Stay Logged In" or "Log Out"
- Concurrent sessions from different devices are allowed (no single-device restriction)
- "Log out of all devices" option available in user profile settings
- All session events (login, logout, expiry) are logged in `AuditLog`

---

## 4. Role-Based Access Control

### 4.1 Permission Matrix

Every API endpoint and every UI element is gated by the user's `role`. The following matrix defines exactly what each role can do.

| Feature | Student | Lecturer | Moderator | Admin |
|---|---|---|---|---|
| Browse published projects | ✅ | ✅ | ✅ | ✅ |
| Browse published publications | ✅ | ✅ | ✅ | ✅ |
| View project detail | ✅ | ✅ | ✅ | ✅ |
| Make memoir reservation | ✅ | ✅ | ✅ | ✅ |
| Open memoir viewer | ✅ (with reservation) | ✅ (with reservation) | ✅ | ✅ |
| Submit project | ✅ | ❌ | ❌ | ❌ |
| Submit publication | ❌ | ✅ | ❌ | ❌ |
| Edit own draft submission | ✅ | ✅ | ❌ | ❌ |
| Delete own draft | ✅ | ✅ | ❌ | ❌ |
| View moderation queue | ❌ | ❌ | ✅ | ✅ |
| Approve / reject submissions | ❌ | ❌ | ✅ | ✅ |
| Hide / unpublish content | ❌ | ❌ | ✅ | ✅ |
| Mark duplicate | ❌ | ❌ | ✅ | ✅ |
| Archive / restore content | ❌ | ❌ | ✅ | ✅ |
| View all users | ❌ | ❌ | ❌ | ✅ |
| Suspend / activate users | ❌ | ❌ | ❌ | ✅ |
| Change user roles | ❌ | ❌ | ❌ | ✅ |
| Configure reservation settings | ❌ | ❌ | ❌ | ✅ |
| Configure access schedule | ❌ | ❌ | ❌ | ✅ |
| View audit logs | ❌ | ❌ | ❌ | ✅ |
| View platform reports | ❌ | ❌ | ✅ (limited) | ✅ (full) |
| Export reports | ❌ | ❌ | ❌ | ✅ |
| Manage system settings | ❌ | ❌ | ❌ | ✅ |
| Trigger backups | ❌ | ❌ | ❌ | ✅ |
| Approve reservation renewals | ❌ | ❌ | ✅ | ✅ |

### 4.2 Frontend Role Enforcement
- The sidebar navigation renders only the links the current user's role can access
- Buttons and actions not available to the user's role are hidden (not just disabled)
- Direct URL access to unauthorized pages returns a `403 Forbidden` page with a friendly message and a "Go to Dashboard" button

---

## 5. Dashboard System

### 5.1 Student Dashboard

**Route:** `/dashboard`  
**Reference:** GitHub Student Developer Pack dashboard, Canvas LMS student view

#### Layout
- Top: Welcome banner — *"Hello, [First Name] 👋 · [Department] · [Academic Year]"*
- Row 1: Stats cards (4 cards)
- Row 2: My Recent Submissions table (left 60%) + Active Reservations widget (right 40%)
- Row 3: Notifications inbox preview (last 5 unread)
- Bottom: Quick Actions bar

#### Stats Cards

| Card | Icon | What it Shows |
|---|---|---|
| My Submissions | 📄 | Total count of all submissions (all statuses) |
| Under Review | ⏳ | Count of `PENDING_APPROVAL` submissions |
| Published | ✅ | Count of `PUBLISHED` submissions |
| Active Reservations | 📅 | Count of upcoming `CONFIRMED` reservations |

Each card is clickable and navigates to the relevant filtered view.

#### My Recent Submissions Table

Columns: `Title` · `Type` · `Status Badge` · `Submitted Date` · `Last Updated` · `Actions`

Actions per row:
- **Draft:** Edit · Delete
- **Pending Approval:** View · (no edits allowed once submitted)
- **Published:** View · Share Link
- **Rejected:** View · Re-upload (if re-upload was requested by moderator)

#### Active Reservations Widget
Shows the next 3 upcoming reservations as cards:
- Project title
- Date and time of slot
- Countdown timer (e.g. "Starts in 2h 14m")
- "Open Viewer" button (enabled only when `now()` is between `slotStart` and `slotEnd`)

#### Quick Actions Bar
- Submit New Project
- Browse Repository
- My Reservations
- My Profile

---

### 5.2 Lecturer Dashboard

**Route:** `/dashboard`  
**Reference:** Academia.edu profile + ResearchGate dashboard patterns

#### Layout
- Top: Welcome banner with publication count
- Row 1: Stats cards (4 cards)
- Row 2: Pending Review Queue (own submissions awaiting moderation) + My Publications table
- Row 3: Supervised Students tracker
- Bottom: Quick Actions

#### Stats Cards

| Card | What it Shows |
|---|---|
| My Publications | Total publications (all statuses) |
| Under Review | `PENDING_APPROVAL` publications |
| Published | `PUBLISHED` publications |
| Total Views | Sum of `viewCount` across all published publications |

#### Supervised Students Tracker
Table of students the lecturer supervises (linked via `CampusProfile.supervisorName`):

Columns: `Student Name` · `Student ID` · `Project Title` · `Submission Status` · `Last Activity`

This gives lecturers real-time visibility into whether their supervised students have submitted, are pending review, or have been published.

#### Quick Actions
- Submit New Publication
- Browse Repository
- Review Supervised Students
- My Profile

---

### 5.3 Moderator Dashboard

**Route:** `/dashboard`  
**Reference:** GitHub Pull Request review dashboard, Zenodo moderation queue

#### Layout
- Top: Prominent pending queue count badge — *"X items awaiting your review"* in a high-visibility blue alert banner
- Row 1: Stats cards
- Row 2: Pending Review Queue table (full width, most important element on the page)
- Row 3: Recent Moderation Activity log (last 10 actions performed)

#### Stats Cards

| Card | What it Shows |
|---|---|
| Pending Review | Items awaiting moderation (real-time count) |
| Reviewed Today | Actions performed today |
| Approval Rate | Percentage of approved vs total reviewed (last 30 days) |
| Avg Review Time | Average hours from submission to moderation decision |

#### Pending Review Queue Table
Sorted by: Oldest first (to prevent items waiting indefinitely)

Columns: `Type Badge` · `Title` · `Author` · `Department` · `Submitted` · `Days Waiting` · `Actions`

`Days Waiting` turns orange if > 3 days and red if > 7 days — borrowed from JIRA issue aging patterns.

Actions per row: **Approve** · **Reject** · **Request Re-upload** · **View Details**

---

### 5.4 Admin Dashboard

**Route:** `/dashboard`  
**Reference:** Grafana observability dashboard, AWS Console health metrics

#### Layout
- Top: Platform health status bar — green if all systems normal, yellow/red if issues detected
- Row 1: Key metrics cards (6 cards)
- Row 2: Charts — Submissions per Month (bar) + Department Activity (donut) + User Role Distribution (pie)
- Row 3: Recent Audit Log feed (last 10 events, real-time)
- Row 4: Quick Access links to admin modules

#### Key Metrics Cards

| Card | What it Shows |
|---|---|
| Total Users | All registered users across all roles |
| Active Today | Users who logged in today |
| Total Projects | All projects (all statuses) |
| Total Publications | All publications (all statuses) |
| Active Reservations | Currently active viewer sessions |
| Reservation System | Status indicator: Enabled / Disabled |

#### Quick Access Links
User Management · Moderation Queue · Reservation Settings · Access Schedule · Reports · Audit Logs · System Settings · Backup

---

## 6. Project Submission — Student Workflow

**Route:** `/submit/project`  
**Reference:** Zenodo upload workflow, GitHub repository creation wizard, DSpace submission interface

The submission wizard is a **3-step guided form** with autosave every 30 seconds. A progress indicator at the top shows the current step and which steps are complete. The student can navigate back to any completed step at any time.

### Step 1: Project Metadata

**What it captures:** Everything needed to describe and categorize the project in the repository.

| Field | Type | Required | Notes |
|---|---|---|---|
| Project Title | Text | ✅ | Max 200 chars. Character counter shown |
| Abstract | Rich Text Editor | ✅ | Min 100 words. Word counter shown. Supports bold, italic, bullet lists |
| Keywords | Tag Input | ✅ | Min 3, max 10 tags. User types and presses Enter to add each tag |
| Department | Dropdown | ✅ | Populated from AUCA department list |
| Academic Year | Dropdown | ✅ | e.g. `2023-2024` |
| Project Type | Radio | ✅ | Final Year Project / Postgraduate Thesis / Coursework |
| Category | Dropdown | ✅ | Software System / Research Study / Data Analysis / Mobile App / Web App / Other |
| Technologies Used | Tag Input | Optional | User adds technology tags e.g. `React`, `Spring Boot` |
| Supervisor Name | Text | Optional | Pre-filled from `CampusProfile.supervisorName` if available |
| Co-Authors | Dynamic Form | Optional | Add co-authors by typing name + student ID. Minimum 1 row shown. "Add Co-Author" button adds rows |

**AI Assistance Panel (AcademIQ sidebar):**
As the student fills in the title and keywords, the AcademIQ AI sidebar activates and shows:
- *"3 similar projects found in the repository — click to review before submitting"*
- *"Suggested additional keywords based on your abstract: [keyword chips]"*
- *"Your abstract is strong. Consider adding more detail about your methodology."*

**Co-Author Entry:**
Each co-author row has: Full Name field + Student ID field + Role dropdown (Primary Author / Co-Author) + Remove button. When a Student ID is entered, the system looks up the user and auto-fills their name if found in the database, showing a green check. If not found, the name field is left for manual entry with a yellow notice: *"This student ID was not found in the platform. Their details will be saved manually."*

---

### Step 2: Memoir Upload

**What it captures:** The final memoir PDF document.

#### Upload Zone
Large drag-and-drop zone with:
- Dashed blue border with upload cloud icon
- Text: *"Drag and drop your memoir PDF here, or click to browse"*
- Accepted format: PDF only
- Maximum size: 50MB
- If wrong format: Immediate inline error *"Only PDF files are accepted (ERR-UPL-001)"*
- If too large: *"File exceeds the 50MB size limit (ERR-UPL-002)"*

#### Upload Process (Visual States)

| State | Visual |
|---|---|
| Uploading | Blue progress bar with percentage, filename, and estimated time |
| Scan Pending | Yellow badge *"Scanning for malware..."* with spinner |
| Scan Clean | Green badge *"✓ File verified and secure"* |
| Scan Infected | Red alert *"This file was flagged by our security scanner. Please re-upload a clean version."* |
| Scan Failed | Orange alert *"Scan could not be completed. Please try again or contact ICT support."* |

#### Post-Upload Information Display
After successful upload and clean scan:
- First page thumbnail preview (150×200px, generated server-side)
- File details: `originalFileName` · `fileSizeBytes` (human-readable) · `totalPages` · `sha256Checksum` (truncated with copy button)
- *"Your file has been securely stored. The SHA-256 checksum above is your file's unique fingerprint — it ensures your document cannot be altered after submission."*

---

### Step 3: GitHub Repository

**What it captures:** The source code reference for the project.

#### Connection Flow
1. "Connect GitHub Account" button → triggers GitHub OAuth 2.0 flow
2. After auth: Repository selector dropdown (lists the authenticated user's repositories)
3. Repository selected → system fetches basic metadata (name, language, star count, private/public status)
4. "Select Final Commit / Tag" section appears:
   - Dropdown showing last 20 commits (hash, message, author, date)
   - Tag selector (shows all Git tags if any exist)
   - Student selects one — this becomes `finalCommitHash`
   - Blue info box: *"This commit will be permanently recorded as your final submission. You can continue working on GitHub after submission — this record will not change."*
5. README Preview panel shows the rendered README.md from the selected commit

#### What Happens Next
- System stores all metadata in `GithubRepo`
- `readmeContent` is fetched and cached
- Language badge, star count, fork count, and `isPrivate` status are stored

---

### Submission Confirmation
After Step 3, a summary review screen shows:
- All entered metadata (collapsible sections)
- Memoir file preview thumbnail + file details
- GitHub repository summary with final commit hash
- Checkboxes: *"I confirm this is my original work"* · *"I agree to the AUCA Repository Terms of Use"*
- **Submit for Review** button (disabled until both checkboxes are checked)
- On submit: Status becomes `PENDING_APPROVAL`. Page shows: *"✓ Your project has been submitted for review. You will be notified by email when a decision is made. You can track the status in your dashboard."*

---

### Autosave Behavior
- Autosave runs every 30 seconds while the form is open
- Top-right indicator: *"Draft saved 12 seconds ago"* (green dot) / *"Saving..."* (spinner)
- If the student closes the browser tab: Draft is saved. On next visit to `/submit/project`, a banner appears: *"You have an unfinished submission. Would you like to continue where you left off?"*

---

## 7. Publication Submission — Lecturer Workflow

**Route:** `/submit/publication`  
**Reference:** Zenodo record creation, ORCID works addition, arXiv submission interface

### Step 1: Publication Metadata

| Field | Type | Required | Notes |
|---|---|---|---|
| Title | Text | ✅ | Max 300 chars |
| Abstract | Rich Text | ✅ | Min 100 words |
| Keywords | Tag Input | ✅ | Min 3, max 15 tags |
| Publication Type | Dropdown | ✅ | Research Paper / Journal Article / Conference Paper / Book Chapter / Technical Report / Other |
| Department | Dropdown | ✅ | AUCA department list |
| Academic Year | Dropdown | ✅ | |
| DOI or External Link | Text | Optional | Validated format if DOI is entered (must match `10.xxxx/...` pattern). Shows green check when valid DOI is detected |
| Journal / Conference Name | Text | Optional | Auto-suggest from a curated list of known journals. Accepts free text if not in list |
| Version Label | Dropdown | Optional | Preprint / Author Accepted Manuscript / Publisher's PDF / Working Paper / Other |
| Co-Authors | Dynamic Form | Optional | Full Name + Email + Institution + Role (Lead / Co-Author / Corresponding). "Add Co-Author" button |
| Notify Co-Authors | Toggle | Optional | If ON: co-authors receive email notification when published |

---

### Step 2: Document Upload

Identical to the Student memoir upload (same drag-and-drop zone, same validation, same scan status indicators), with these additions:

| Field | Type | Notes |
|---|---|---|
| Version Label (on file) | Text | e.g. `Version 2 — Revised after peer review` |
| Upload Date (optional) | Date Picker | For cases where the document was created earlier than the upload date |

---

### Step 3: Access & Visibility Settings

| Field | Type | Notes |
|---|---|---|
| Visibility | Radio | AUCA Only (default) · Restricted (approval required) · Embargoed Until Date |
| Embargo Date | Date Picker | Only shown if Embargoed selected. Must be a future date |
| Access Notes | Textarea | Free text instructions for viewers, e.g. *"Please contact the author before citing"* |
| Notify Co-Authors on Publish | Toggle | Sends email to all listed co-authors when the publication is approved |

---

## 8. Browse & Discovery

**Route:** `/browse`  
**Reference:** JSTOR search interface, Google Scholar results page, Zenodo browse page

The Browse page is the primary discovery interface for all content in the repository. Both student projects and lecturer publications are discoverable from this single page.

### 8.1 Page Layout

```
┌─────────────────────────────────────────────────────────┐
│  🔍  Search projects, publications, authors, keywords...  │
│       Powered by AcademIQ AI  ✦                          │
├─────────────┬───────────────────────────────────────────┤
│  Filters    │  [All] [Projects] [Publications] [Theses]  │
│  Sidebar    │                                            │
│  ─────────  │  ┌────────┐ ┌────────┐ ┌────────┐        │
│  Type       │  │ Card 1 │ │ Card 2 │ │ Card 3 │        │
│  Department │  └────────┘ └────────┘ └────────┘        │
│  Year       │  ┌────────┐ ┌────────┐ ┌────────┐        │
│  Tech Stack │  │ Card 4 │ │ Card 5 │ │ Card 6 │        │
│  Status     │  └────────┘ └────────┘ └────────┘        │
│  Sort By    │                                            │
│             │  Showing 247 results · Found in 0.4s      │
└─────────────┴───────────────────────────────────────────┘
```

### 8.2 Search Bar

- Full-width at the top of the page
- Placeholder: *"Search projects, publications, authors, keywords..."*
- Below: *"✦ Powered by AcademIQ AI"* label in purple
- Real-time suggestions as user types (debounced 300ms):
  - Recent searches (from local storage)
  - Suggested terms from the repository index
  - Matching author names
- Press Enter or click Search to execute

### 8.3 Tab Filters

| Tab | What it Shows |
|---|---|
| All | All published content (projects + publications) |
| Student Projects | Only `ProjectType = FINAL_YEAR_PROJECT` |
| Publications | Only publications (all `PublicationType` values) |
| Theses | Only `ProjectType = POSTGRADUATE_THESIS` |

### 8.4 Filters Sidebar

| Filter | Type | Options |
|---|---|---|
| Content Type | Checkboxes | Final Year Project · Postgraduate Thesis · Coursework · Research Paper · Journal Article · Conference Paper · Book Chapter · Technical Report |
| Department | Dropdown (searchable) | All AUCA departments |
| Academic Year | Multi-select checkboxes | Last 10 academic years |
| Technology Stack | Tag selector | Common technologies + free search |
| Availability | Radio | All · Available Now · By Reservation · Restricted |
| Sort By | Dropdown | Most Recent · Most Viewed · Most Reserved · Title A–Z · Department |

Applied filters appear as dismissible chips below the search bar. A "Clear All Filters" link appears when any filter is active.

### 8.5 Project / Publication Card

Each card in the results grid shows:

```
┌───────────────────────────────────────┐
│ [TYPE BADGE]              [YEAR BADGE] │
│                                        │
│ Project Title Here — Two Lines Max...  │
│                                        │
│ 👤 Serge Benit · Akize Israel          │
│ 🏫 Software Engineering                │
│                                        │
│ Abstract preview text here, truncated  │
│ to three lines maximum with ellipsis...│
│                                        │
│ [React] [Spring Boot] [PostgreSQL]     │
│                                        │
│ ─────────────────────────────────────  │
│ 👁 142 views  🔗 GitHub  [● Available] │
│                          [View Details]│
└───────────────────────────────────────┘
```

**Type Badge Colors:**
- Final Year Project → Blue
- Publication → Green
- Thesis → Purple
- Coursework → Gray

**Availability Pills:**
| State | Color | Text |
|---|---|---|
| Open access | Green | ● Available |
| Reservation required | Blue | 📅 Reserve to Access |
| Slots filling | Orange | 📅 2 / 3 Slots Taken |
| Fully booked | Red | 🔴 Fully Reserved · Next: Tue 10AM |
| Restricted | Orange | 🔐 Restricted |
| Embargoed | Purple | ⏰ Available from [date] |

**Card Interactions:**
- Hover: subtle lift shadow, title color deepens to `--primary-700`
- The entire card is clickable and navigates to the detail page
- Technology tags are clickable and apply that tag as a filter
- Author name is clickable and filters by that author

### 8.6 Pagination & Results Meta

- Bottom of results: Page selector with Previous / Next and page number pills
- Top right above results: *"Showing 1–12 of 247 results · Found in 0.4s"*
- Items per page selector: 12 / 24 / 48
- URL updates with query params on every search/filter change (shareable/bookmarkable search URLs)

---

## 9. Project Detail Page

**Route:** `/projects/:id`  
**Reference:** GitHub repository page layout, Zenodo record page, DSpace item view

### 9.1 Layout

The project detail page uses a **3-tab structure** to organize the large amount of information without overwhelming the user.

**Header Section (always visible, above tabs):**
- Project title (large, 28px)
- Type badge + Status badge + Department tag + Academic Year tag
- Author avatars with names (primary author first)
- Quick stats inline: `👁 142 views` · `📅 8 reservations` · `📅 Published: March 2025`

**Tabs:** `[Project Info]` `[GitHub Repository]` `[Memoir & Access]`

---

### Tab 1: Project Info

| Section | Content |
|---|---|
| Abstract | Full abstract with a "Show More / Show Less" toggle for very long abstracts |
| Keywords | Clickable blue chip tags — clicking applies that keyword as a search filter on the browse page |
| Technologies Used | Clickable technology tag chips |
| Supervisor | Name (if provided) |
| Submission Details | Submitted: date · Published: date · Last Updated: date |
| Category | With icon |
| Department | Clickable — navigates to browse filtered by this department |
| Co-Authors | List of all `ProjectAuthor` entries with name, student ID, role, and avatar initials |
| Citation Suggestion | Auto-generated citation in APA format: *[Author(s)] (Year). [Title]. Adventist University of Central Africa. AUCA Connect Publication Hub.* With a "Copy Citation" button |

---

### Tab 2: GitHub Repository

| Section | Content |
|---|---|
| Repository Header | Repo name · Owner · Language badge · Public/Private badge |
| Final Commit | Hash (monospace, truncated with copy button) · Commit message · Date · Author |
| Final Tag | If set, shown as a green tag badge |
| Repository Stats | ⭐ Stars · 🍴 Forks · Primary Language |
| README Preview | Full rendered markdown from `readmeContent` cache. Syntax highlighting for code blocks |
| External Link | "View on GitHub" button (opens in new tab) with a notice: *"Note: The repository may have continued to evolve since the final submission commit recorded above."* |
| Sync Status | *"Repository data last synced: [date]. Sync now"* (admin/moderator only) |

---

### Tab 3: Memoir & Access

This tab is the most functionally complex — it serves as the access control interface for the memoir.

#### Availability Status Panel

```
┌────────────────────────────────────────────────┐
│  CURRENT AVAILABILITY                           │
│                                                  │
│  Slot capacity: ● ● ○  (2 of 3 slots taken)    │
│                                                  │
│  Next available slot:                           │
│  📅 Tuesday, 18 March 2025 · 10:00 AM – 12:00 PM│
│                                                  │
│  Access Hours: Mon–Fri · 8:00 AM – 5:00 PM      │
│                                                  │
│  [  Request Reservation  ]  [ Join Waitlist ]   │
└────────────────────────────────────────────────┘
```

**Slot Visualization:** Three circle indicators showing how many of the maximum concurrent slots are taken. Green = taken by someone. Gray = available.

**If User Has Active Reservation:**
```
┌────────────────────────────────────────────────┐
│  ✅ YOU HAVE AN ACTIVE RESERVATION              │
│                                                  │
│  📅 Today · 10:00 AM – 12:00 PM                 │
│  ⏱ Session starts in: 00:14:22                 │
│                                                  │
│  [    Open Memoir Viewer    ]                   │
│                                                  │
│  [ Cancel Reservation ]                         │
└────────────────────────────────────────────────┘
```

**If Slot Is Active (viewer open):**
The "Open Memoir Viewer" button is green and fully enabled. It opens the viewer in a full-page route.

---

## 10. Publication Detail Page

**Route:** `/publications/:id`  
**Reference:** JSTOR article page, ResearchGate publication page

### Layout

**Header:**
- Publication title
- Publication type badge + Status badge + Year
- Author list with institutions
- Journal / Conference Name (if provided) · DOI link button · Year

**Body Sections:**

| Section | Content |
|---|---|
| Abstract | Full abstract |
| Keywords | Clickable keyword chips |
| Authors | Full author list with name, institution, role |
| Publication Details | Type · Journal/Conference · Year · Version Label · DOI |
| Access | Based on `visibility`: button to view document OR access request form |
| Access Notes | If set by the lecturer, shown in a blue info box |
| Citation | Auto-generated APA citation with copy button |
| Related Projects | AcademIQ suggests student projects on the same topic (optional feature) |

**Access Button Behavior:**
- `AUCA_ONLY`: *"View Document"* button — opens the document directly in a PDF viewer (non-memoir publications do not require a reservation)
- `RESTRICTED`: *"Request Access"* button — opens a modal where the viewer states their purpose. Request is sent to the lecturer and/or moderator for approval
- `EMBARGOED`: Disabled button with text: *"Available from [embargoUntil date]"*

---

## 11. Online Reservation System

**Reference:** Calendly booking interface, Doodle poll, OpenTable reservation system

### 11.1 Reservation Modal

Triggered by clicking "Request Reservation" on the Project Detail page.

#### Layout
- Modal header: *"Reserve Access to: [Project Title]"*
- Tab selector: Week view (default) / List view
- Calendar grid showing current week and next 2 weeks

#### Calendar Slot Colors

| Color | Meaning |
|---|---|
| Green | Available (0/3 slots taken) |
| Yellow | 1/3 slots taken |
| Orange | 2/3 slots taken |
| Red | 3/3 slots taken (fully booked) |
| Gray | Outside regulated access hours or past date |
| Blue outline | Currently selected slot |

#### Slot Selection
1. User clicks a green or yellow/orange slot
2. Slot detail panel appears below: *"Tuesday 18 March 2025 · 10:00 AM – 12:00 PM · 1 of 3 slots available"*
3. *"Confirm Reservation"* button activates
4. On confirm: Reservation is created, confirmation notification is sent immediately

#### Maximum Concurrent Users
The system counts existing `CONFIRMED` + `ACTIVE` reservations for the selected project and time slot. If this count equals `SystemSetting["MAX_CONCURRENT_RESERVATIONS"]` (default: 3), the slot turns red and cannot be selected.

---

### 11.2 My Reservations Page

**Route:** `/my-reservations`  
**Reference:** Airbnb trips page, hotel booking management

#### Tabs
- **Upcoming** — `CONFIRMED` reservations with future `slotStart`
- **Active Now** — reservations where `slotStart <= now() <= slotEnd` and `status = ACTIVE`
- **Waitlisted** — `Waitlist` entries with `status = WAITING` or `NOTIFIED`
- **Past** — `COMPLETED`, `NO_SHOW`, `CANCELLED` reservations

#### Reservation Card

```
┌─────────────────────────────────────────────────────┐
│ 📄 AUCA Connect Publication Hub                      │
│    Final Year Project · Software Engineering         │
│                                                      │
│ 📅 Tuesday, 18 March 2025                           │
│    10:00 AM – 12:00 PM  (2 hours)                  │
│                                                      │
│ ⏱ Starts in: 1 day, 3 hours, 22 minutes            │
│                                                      │
│ Status: ● Confirmed                                 │
│                                                      │
│ [ Open Memoir Viewer ]    [ Cancel Reservation ]    │
└─────────────────────────────────────────────────────┘
```

**Countdown timer:** Shown for upcoming reservations. Updates every second using a live timer component. Green when > 1 hour away, yellow when < 1 hour, red when < 15 minutes (matching the reminder notification timing).

**"Open Memoir Viewer" button:** Disabled (grayed out) until `now() >= slotStart`. When the slot begins, it automatically enables without a page refresh (real-time via WebSocket or polling).

---

### 11.3 Reservation Confirmation Email

Sent immediately after reservation is confirmed. Contains:
- Project title
- Reservation date and time
- Access instructions
- Link to "My Reservations" page
- Reminder: *"You will receive another notification 15 minutes before your session begins."*

### 11.4 Reservation Reminder Notification

Sent 15 minutes before `slotStart`:
- In-app notification with `notificationType = RESERVATION_REMINDER`
- Email reminder
- Content: *"Your reserved session for [Project Title] starts in 15 minutes. Click here to open the Memoir Viewer when your session begins."*

### 11.5 No-Show Handling

If a reservation reaches `slotEnd` without a `ViewerSession` being created (i.e. the user never opened the viewer):
- A scheduled job sets `status = NO_SHOW`
- `noShowCount` is incremented on that reservation record
- The user's total no-show count is calculated from all their reservations
- If total no-shows >= `SystemSetting["NO_SHOW_THRESHOLD"]` (default: 3):
  - A `Notification` is sent: *"Your reservation access has been restricted due to repeated no-shows. Contact the ICT Help Desk if you believe this is an error."*
  - The user's ability to make new reservations is suspended pending admin review
- An `AuditLog` entry is created with `action = RESERVATION_NO_SHOW`

### 11.6 Cancellation

- Can be cancelled up to `SystemSetting["CANCELLATION_WINDOW_MINUTES"]` (default: 30) minutes before `slotStart`
- After that window, cancellation is disabled with a message: *"Cancellations must be made at least 30 minutes before the session starts."*
- On cancel: Reservation status → `CANCELLED`, freed slot is offered to the next person on the `Waitlist`
- `AuditLog` entry created with `action = RESERVATION_CANCELLED`

### 11.7 Renewal Workflow

After a reservation is `COMPLETED`:
- If the user wants to read again, they can submit a **Renewal Request**
- Renewal is only available if: No other students currently have a `CONFIRMED` or `ACTIVE` reservation on the same project
- The renewal request goes to the admin/moderator queue
- Moderator sees: *"[User Name] is requesting renewal access to [Project Title]. No other students currently have an active reservation on this project."*
- Approve → new `Reservation` created with the same time slot logic
- Reject → rejection reason sent to student via `Notification`
- All renewal decisions are logged as `ModerationAction` records

---

## 12. Memoir Viewer — Security-Critical

**Route:** `/viewer/:sessionToken`  
**Reference:** JSTOR article viewer, Scribd document reader, ProQuest Ebook Central

This is the most security-sensitive page in the entire system. Every technical decision here is made to prevent downloading while allowing reading.

### 12.1 Access Validation

Before any content is loaded:
1. Parse `sessionToken` from URL
2. Verify JWT signature (server-side)
3. Check `tokenExpiresAt > now()` (reject if expired)
4. Check embedded `userId` matches logged-in user (reject if mismatch)
5. Check `ViewerSession.sessionActive = true` (reject if session was terminated)
6. Check `Reservation.status` is `CONFIRMED` or `ACTIVE` (reject otherwise)

Any validation failure → full-page error with message and redirect to "My Reservations".

### 12.2 Viewer Layout

```
┌────────────────────────────────────────────────────────────────┐
│ AUCA Connect  |  [Project Title]  |  [User Name]  |  ⏱ 01:42:15│
├──────────┬─────────────────────────────────────┬───────────────┤
│          │                                       │               │
│  Page    │                                       │  🔍 Zoom In   │
│  Thumbs  │      [  DOCUMENT PAGE HERE  ]        │  🔍 Zoom Out  │
│          │                                       │  ⛶ Fullscreen │
│  pg 1    │   WATERMARK: Serge Benit | 27311     │               │
│  pg 2    │   2025-03-18 10:14 | AUCA – Univ.   │  Accessibility│
│  pg 3 ◄  │   Use Only                           │  Options      │
│  pg 4    │                                       │               │
│  ...     │                                       │               │
│          │                                       │               │
├──────────┴─────────────────────────────────────┴───────────────┤
│   ◀ Prev   Page 3 of 47   Next ▶                               │
└────────────────────────────────────────────────────────────────┘
```

### 12.3 Server-Side Page Rendering

**Critical Architecture Decision:**
The PDF binary is **never sent to the client**. Each page is rendered server-side using a PDF rendering library (e.g. Apache PDFBox or PDF.js server-side) into a PNG or WebP image. The rendered image is then watermarked and served via a **signed single-use URL** that expires in 30 seconds. This means:

- There is no PDF in the browser's memory or cache
- `Ctrl+S` saves nothing useful (just the page HTML wrapper, not the content)
- "Save image" would save a single watermarked page image — not the full document
- Network inspection shows only short-lived image URLs, not a PDF stream

### 12.4 Dynamic Watermark

Applied on every page, server-side, before the image is served:

**Watermark Content:**
```
[FirstName LastName] | [CampusID]
[YYYY-MM-DD HH:mm] | AUCA — University Use Only
```

**Watermark Style:**
- Diagonal (45° angle)
- Semi-transparent (30% opacity)
- Blue color (`--primary-300`)
- Repeated 3 times across the page in a grid pattern so cropping cannot remove it

### 12.5 Anti-Download Controls

| Control | Implementation |
|---|---|
| No right-click | `oncontextmenu="return false"` on the viewer container |
| No Ctrl+S | `document.addEventListener('keydown', e => { if (e.ctrlKey && e.key === 's') e.preventDefault() })` |
| No Ctrl+P | Same pattern for `p` key |
| No Ctrl+A | Same for `a` key (prevents select all then copy) |
| No drag selection | `user-select: none` CSS on all content elements |
| No print | `@media print { body { display: none } }` CSS |
| Tokenized URLs | Each page image URL contains a single-use signed token valid for 30 seconds |
| No caching | HTTP headers: `Cache-Control: no-store, no-cache` on all page images |

### 12.6 Session Timer

- **Top bar timer** counts down from `slotEnd - now()`
- When timer reaches **10 minutes:** Timer turns orange, a toast notification appears: *"Your session ends in 10 minutes."*
- When timer reaches **5 minutes:** Timer turns red, a modal appears with the session end warning and a "Request New Reservation" link
- When timer reaches **0:00:** Session terminates automatically:
  - `ViewerSession.sessionActive` → `false`
  - `ViewerSession.endedAt` → `now()`
  - `Reservation.status` → `COMPLETED`
  - Viewer is replaced with a full-page overlay: *"Your session has ended. Thank you for using AUCA Connect."* with options: "Make New Reservation" or "Back to Browse"

### 12.7 Real-Time Audit

Every page navigation creates a `PageViewLog` record immediately. The viewer sends a heartbeat every 60 seconds to update `ViewerSession.totalTimeSpentSeconds`. If the heartbeat stops (user closes tab or loses internet), the session is marked inactive after a configurable timeout.

### 12.8 Accessibility in Viewer

- Keyboard navigation: Arrow keys for page navigation, `+`/`-` for zoom
- High contrast mode toggle
- Text size adjustment (for zooming in on text)
- Screen reader: Page number announcements

---

## 13. Moderation & Approval Workflow

**Route:** `/moderation`  
**Reference:** GitHub Pull Request review, arXiv moderation interface, Zenodo curator tools

### 13.1 Moderation Queue Page

#### Tabs

| Tab | Description |
|---|---|
| All | All content (any status) |
| Pending Review | `status = PENDING_APPROVAL` — the primary working queue |
| Published | `status = PUBLISHED` — already live content |
| Hidden | `status = HIDDEN` — temporarily hidden content |
| Archived | `status = ARCHIVED` — long-term archived content |
| Flagged Duplicates | Items that have a `MARK_DUPLICATE` moderation action |

#### Table Columns

`Type Icon` · `Title` · `Author` · `Department` · `Submitted Date` · `Days Waiting` · `Actions`

**Days Waiting column styling:**
- 0–3 days: Normal gray text
- 4–7 days: Orange text with ⚠ icon
- 8+ days: Red text with 🔴 icon and tooltip: *"This item has been waiting for review for over a week"*

#### Moderation Actions

Each pending item has the following action buttons in the table row:

| Action | Icon | What it Does | Requires Reason? |
|---|---|---|---|
| **Approve** | ✅ | Sets `status = PUBLISHED`, `publishedAt = now()`, creates `ModerationAction(APPROVE)`, sends `Notification(PROJECT_APPROVED)` | No (optional comment) |
| **Reject** | ❌ | Sets `status = REJECTED`, creates `ModerationAction(REJECT)`, sends `Notification(PROJECT_REJECTED)` with reason | ✅ Mandatory |
| **Hide** | 👁 | Sets `status = HIDDEN` on a published item, creates `ModerationAction(HIDE)` | ✅ Mandatory |
| **Request Re-upload** | 🔁 | Sends `Notification(PROJECT_REUPLOAD_REQUESTED)` with specific instructions, sets `status = DRAFT` | ✅ Mandatory + specific instructions |
| **Mark Duplicate** | 📋 | Sets `duplicateOfId`, creates `ModerationAction(MARK_DUPLICATE)`, links to original | ✅ Mandatory + original project search |
| **Archive** | 🗄 | Sets `status = ARCHIVED`, creates `ModerationAction(ARCHIVE)` | ✅ Mandatory |
| **Restore** | 🔄 | Restores `ARCHIVED` or `HIDDEN` item to `PUBLISHED`, creates `ModerationAction(RESTORE)` | No |

#### Rejection Reason Templates

To speed up the moderation process, moderators can select from pre-configured rejection reason templates (managed in `SystemSetting`):

| Template Name | Reason Text |
|---|---|
| `INCOMPLETE_METADATA` | *"This submission is missing required metadata. Please ensure the abstract, keywords, and department are complete before resubmitting."* |
| `POOR_DOCUMENT_QUALITY` | *"The uploaded PDF document does not meet quality standards. Please ensure the document is legible, correctly oriented, and complete."* |
| `DUPLICATE_TOPIC` | *"A very similar project already exists in the repository. Please review the existing work and ensure your submission offers a distinct contribution."* |
| `POLICY_VIOLATION` | *"This submission does not comply with the AUCA Repository Acceptable Use Policy. Please review the policy and resubmit."* |
| `INCOMPLETE_DOCUMENT` | *"The uploaded document appears to be incomplete. Please ensure all chapters are included in the final submission."* |

The moderator can select a template or write a custom reason. Both the template name and the final reason text are stored in `ModerationAction`.

---

### 13.2 Item Detail View (Moderation Context)

When a moderator clicks "View Details" on a pending item, they see the full project/publication detail page but with an additional **Moderation Panel** fixed at the top:

```
┌──────────────────────────────────────────────────────────────┐
│ ⚠ MODERATION REVIEW MODE — This item is pending approval     │
│                                                               │
│ Submitted: 5 days ago  |  By: Serge Benit (ID: 27311)       │
│                                                               │
│ [Approve] [Reject] [Request Re-upload] [Mark Duplicate]      │
│                                                               │
│ Moderation History: ▼ (expandable)                          │
└──────────────────────────────────────────────────────────────┘
```

**Moderation History (expandable):** Shows all previous `ModerationAction` records for this item in chronological order:
```
• 12 Mar 2025 · Moderator Jane Doe · Requested Re-upload
  Reason: "Please improve the abstract to include methodology."

• 15 Mar 2025 · Student Serge Benit · Re-uploaded document
  Note: "Abstract and document updated as requested."
```

---

## 14. AI Assistant — AcademIQ

**Reference:** Perplexity AI, Semantic Scholar, Elicit research assistant

AcademIQ is the platform's built-in AI assistant. It is accessible from every page via a floating button in the bottom-right corner (blue circle, sparkle ✦ icon, labeled "AcademIQ"). It slides open as a side panel (400px wide) from the right side of the screen.

### 14.1 Panel Header
- *"✦ AcademIQ — Your Academic Assistant"*
- Subtitle: *"Search, discover, and explore AUCA's academic knowledge"*
- Close button (×)

### 14.2 Quick Prompt Chips
Shown when the panel first opens:
- *"Find projects by department"*
- *"Avoid duplicate research topics"*
- *"How do I submit my project?"*
- *"Search publications by lecturer"*

### 14.3 Core Capabilities

#### Smart Search
User types in natural language:
- *"find final year projects about machine learning in the IT department from 2023"*
- AI parses intent → applies filters → returns results as embedded clickable project cards inside the chat

**Result Card (inside chat):**
```
┌─────────────────────────────────────────┐
│ 📄 Machine Learning for Disease Detection│
│    IT Dept · 2023 · Final Year Project  │
│    By: Kalisa John                       │
│    ██████████ 94% relevance             │
│    [View Project]                       │
└─────────────────────────────────────────┘
```

#### Topic Duplicate Checker
User types: *"I want to do a project on student attendance management using mobile apps"*

AI response:
- *"I found 2 similar projects already in the repository:"*
- [Shows project cards with relevance scores]
- *"To make your project unique, consider focusing on: biometric attendance, offline-first mobile architecture, or integration with the AUCA academic database. These angles were not covered in existing work."*

#### Resource Finder
- *"Show me all publications by Dr. Mugisha"* → filters publications by author name
- *"Find theses about mobile health apps"* → keyword search across all theses

#### Guided Submission Assistant
- *"How do I submit my project?"* → step-by-step guide with links to each submission step
- *"What file format is accepted for my memoir?"* → *"Only PDF files are accepted, with a maximum size of 50MB."*
- *"How many co-authors can I add?"* → *"There is no hard limit on co-authors. Each co-author requires a full name and student ID."*

#### FAQ Answering
- *"What are the regulated access hours?"* → reads from `SystemSetting["REGULATED_HOURS_START"]` and `"REGULATED_HOURS_END"]` and responds dynamically
- *"How many reservations can I have at once?"* → reads from relevant `SystemSetting`
- *"What happens if I miss my reservation?"* → explains the no-show policy

### 14.4 Chat Interface Design

- **User messages:** Right-aligned blue bubble
- **AI responses:** Left-aligned white card with blue left border accent
- **Typing indicator:** Three animated dots while AI processes
- **Embedded results:** Project/publication cards render directly inside the chat — clickable, not just text links
- **Context awareness:** AcademIQ knows the current page. If open on a project detail page, it pre-fills: *"You're viewing: [Project Title]. Ask me anything about this project or find related work."*

---

## 15. Notification System

**Reference:** GitHub notification center, Slack notifications, Notion activity feed

### 15.1 Notification Bell

- Located in the top header bar on every page
- Shows a red badge with unread count (max displayed: "99+")
- Clicking opens a dropdown panel (not a full page)

### 15.2 Notification Panel (Dropdown)

- Header: *"Notifications"* · *"Mark all as read"* link
- Shows the 10 most recent notifications
- *"View all notifications"* link at the bottom → navigates to `/notifications`
- Each notification item:
  - Icon (type-specific)
  - Bold title
  - Body preview (1 line)
  - Relative timestamp (*"2 hours ago"*, *"Yesterday"*, *"Mar 15"*)
  - Unread: White background with a blue left border accent
  - Read: Light gray background

### 15.3 Full Notifications Page

**Route:** `/notifications`

Same as the dropdown panel but shows all notifications with full body text. Tabs:
- **All** — everything
- **Unread** — `isRead = false` only

Bulk action: "Mark all as read" button.

### 15.4 All Notification Types

| Type | Icon | Trigger | Recipient |
|---|---|---|---|
| `PROJECT_APPROVED` | ✅ | Moderator approves project | Submitting student |
| `PROJECT_REJECTED` | ❌ | Moderator rejects project | Submitting student |
| `PROJECT_REUPLOAD_REQUESTED` | 🔁 | Moderator requests re-upload | Submitting student |
| `RESERVATION_CONFIRMED` | 📅 | Reservation is confirmed | Booking user |
| `RESERVATION_REMINDER` | ⏰ | 15 minutes before slot start | Booking user |
| `RESERVATION_EXPIRED` | 🕐 | Slot has ended | Booking user |
| `RENEWAL_APPROVED` | ✅ | Admin approves renewal | Requesting user |
| `RENEWAL_REJECTED` | ❌ | Admin rejects renewal | Requesting user |
| `PUBLICATION_APPROVED` | ✅ | Moderator approves publication | Submitting lecturer + co-authors (if `notifyCoAuthors = true`) |
| `PUBLICATION_REJECTED` | ❌ | Moderator rejects publication | Submitting lecturer |
| `SYSTEM_ANNOUNCEMENT` | 📢 | Admin sends platform-wide message | All users |

### 15.5 Email Notifications

Every in-app notification also triggers an email. Email templates are configurable by admins in `SystemSetting`. Each email includes:
- AUCA Connect header with logo
- Notification title and full body
- A primary action button (e.g. "View Project", "Open Reservation", "Re-upload Document")
- Footer: *"You are receiving this email because you have an account on AUCA Connect Publication Hub. Adventist University of Central Africa, Kigali — Rwanda."*

---

## 16. Waitlist System

**Reference:** Eventbrite waitlist, OpenTable waitlist, restaurant reservation apps

### 16.1 Joining the Waitlist

When all slots for a project's time window are fully booked:
- The "Request Reservation" button is replaced with "Join Waitlist"
- Clicking shows: *"All slots for [Project Title] are currently booked. Join the waitlist and we'll notify you when a slot becomes available."*
- Confirm → `Waitlist` record created with `status = WAITING`
- Confirmation: *"You've been added to the waitlist at position [positionInQueue]."*

### 16.2 Waitlist Notification

When a slot becomes available (cancellation or new slot added by admin):
1. System finds the `Waitlist` entry with the lowest `positionInQueue` and `status = WAITING`
2. `status` → `NOTIFIED`, `notifiedAt` → `now()`, `expiresAt` → `now() + SystemSetting["WAITLIST_RESPONSE_WINDOW_HOURS"]`
3. `Notification(RESERVATION_CONFIRMED)` sent with a link to claim the slot
4. Email sent with a countdown timer showing how long the offer is valid

### 16.3 Claiming the Slot

The notification contains a *"Claim This Slot"* button. Clicking:
- Creates a `Reservation` for the notified user
- Sets `Waitlist.status = CONVERTED_TO_RESERVATION`
- Sends reservation confirmation

### 16.4 Expiry

If the user does not claim the slot within `expiresAt`:
- `Waitlist.status = EXPIRED`
- Next person in queue (position 2 → now position 1) is notified
- Expired user falls to the back of the queue (or must rejoin)

### 16.5 My Waitlist View

On the "My Reservations" page, the "Waitlisted" tab shows:
- Project title
- Current queue position
- Status (Waiting / Notified — claim before [time])
- "Leave Waitlist" button

---

## 17. Reports & Analytics

**Route:** `/admin/reports`  
**Reference:** Google Analytics dashboard, Metabase analytics, Zenodo statistics page

### 17.1 Access
- Full access: Admin only
- Limited access: Moderators can view project/publication activity reports but not user data or audit logs

### 17.2 Report Types

#### Project Activity Report
| Metric | Description |
|---|---|
| Submissions per month | Bar chart: how many projects were submitted each month |
| Status distribution | Donut chart: Published vs Pending vs Rejected vs Draft |
| By department | Bar chart: which departments submit most |
| By category | Pie chart: Software System vs Research Study, etc. |
| Average days to approval | Time from `PENDING_APPROVAL` to `PUBLISHED` |
| Most viewed projects | Table: Top 10 most viewed projects with view counts |

#### Viewing Sessions Report
| Metric | Description |
|---|---|
| Sessions per day | Line chart |
| Average session duration | Minutes per session |
| Most read projects | Table: Top 10 by total reading time |
| Pages per session | Average pages viewed before session ends |
| Sessions by department | Which departments' projects get read most |
| No-show rate | Percentage of reservations that result in no-show |

#### Reservation Analytics
| Metric | Description |
|---|---|
| Reservations per day | Line chart |
| Peak booking hours | Heatmap: day of week × hour of day |
| Cancellation rate | Percentage of confirmed reservations cancelled |
| Waitlist conversion rate | Percentage of waitlist entries that convert to reservations |
| Average slots utilization | How full reservation slots are on average |

#### User Access Report
| Metric | Description |
|---|---|
| Active users per month | Unique logins per month |
| Role distribution | Pie chart: Students vs Lecturers vs Moderators vs Admins |
| New registrations per month | Line chart |
| Users with no-show restrictions | List of users currently restricted |
| Top viewers | Users with most reading sessions |

#### Moderation Actions Report
| Metric | Description |
|---|---|
| Actions per moderator | Who reviewed how many items |
| Approval rate | Approved vs rejected as percentage |
| Rejection reasons | Bar chart: which rejection template is used most |
| Average review time | Hours from submission to decision |
| Re-upload requests | How often re-upload is requested |

#### GitHub Integration Report
| Metric | Description |
|---|---|
| Repository sync status | How many repos are overdue for sync |
| Private vs public repos | Breakdown |
| Most forked projects | Top 10 by fork count |
| Most starred projects | Top 10 by star count |

### 17.3 Date Range & Filters

All reports have:
- Date range picker (presets: Last 7 days / Last 30 days / Last 3 months / Last year / Custom range)
- Department filter (applies to all charts)
- Academic year filter

### 17.4 Export

Every report can be exported as:
- **PDF:** Formatted report with AUCA header, charts, and data tables
- **Excel:** Raw data in tabular format for further analysis
- **CSV:** Comma-separated values for import into other tools

---

## 18. Audit Logs

**Route:** `/admin/reports` → Audit Logs tab  
**Reference:** AWS CloudTrail, Datadog audit logs, GitHub enterprise audit log

### 18.1 What Is Logged

Every significant system event creates an `AuditLog` entry. No exceptions. The table below lists all audited events:

| Event | `action` Enum Value | When It Fires |
|---|---|---|
| User login | `LOGIN` | Every successful login |
| User logout | `LOGOUT` | Every explicit logout |
| Failed login attempt | `LOGIN_FAILED` | Every failed authentication attempt |
| Project submitted | `PROJECT_SUBMITTED` | Student clicks "Submit for Review" |
| Project published | `PROJECT_PUBLISHED` | Moderator approves a project |
| Project rejected | `PROJECT_REJECTED` | Moderator rejects a project |
| Project detail viewed | `PROJECT_VIEWED` | Any user opens a project detail page |
| Memoir reading session | `MEMOIR_VIEWED` | At end of `ViewerSession` — logs total pages and duration |
| Reservation created | `RESERVATION_CREATED` | Reservation is confirmed |
| Reservation cancelled | `RESERVATION_CANCELLED` | User or admin cancels a reservation |
| Reservation no-show | `RESERVATION_NO_SHOW` | Scheduled job marks missed reservation |
| Publication submitted | `PUBLICATION_SUBMITTED` | Lecturer clicks "Submit for Review" |
| Publication published | `PUBLICATION_PUBLISHED` | Moderator approves a publication |
| User suspended | `USER_SUSPENDED` | Admin suspends a user account |
| User role changed | `USER_ROLE_CHANGED` | Admin changes a user's role |
| Settings changed | `SETTINGS_CHANGED` | Admin changes any `SystemSetting` value |
| Backup triggered | `BACKUP_TRIGGERED` | Manual or scheduled backup runs |
| File uploaded | `FILE_UPLOADED` | Memoir or publication file upload completes |

### 18.2 Audit Log Table UI

Columns: `Timestamp` · `User` · `Action` · `Entity Type` · `Entity ID` (clickable) · `IP Address` · `Details`

**Filters:**
- Date range picker
- Action type multi-select
- Entity type multi-select
- User search

**"Details" column:** Expandable — clicking shows the `detailsJson` content formatted as a readable key-value list.

**Immutability notice:** Blue info box at the top of the page: *"ℹ Audit logs are permanent records. They cannot be edited, deleted, or altered in any way."*

---

## 19. Admin — User Management

**Route:** `/admin/users`  
**Reference:** Auth0 user management, AWS IAM console, Okta admin panel

### 19.1 User List Table

Columns: `Avatar + Name` · `University Email` · `Campus ID` · `Role Badge` · `Department` · `Status Badge` · `Last Login` · `Actions`

**Filters:**
- Role (All / Student / Lecturer / Moderator / Admin)
- Status (All / Active / Suspended / Pending Verification)
- Department dropdown
- Search by name or email

### 19.2 Actions Per User

| Action | What it Does |
|---|---|
| **Edit Role** | Modal to change role. Logged in `AuditLog(USER_ROLE_CHANGED)`. Sends notification to user. |
| **Suspend Account** | Sets `status = SUSPENDED`. Modal requires suspension reason. Logged in `AuditLog(USER_SUSPENDED)`. User receives notification. |
| **Activate Account** | Sets `status = ACTIVE`. Reverses a suspension. |
| **Reset Password** | Sends a password reset email to the user's university email. |
| **View Activity** | Opens a filtered view of `AuditLog` showing only this user's actions. |
| **View Submissions** | Opens a filtered view of all projects/publications by this user. |
| **View Reservations** | Opens a filtered view of all reservations by this user. |

### 19.3 Bulk Actions

- Select multiple users with checkboxes
- Bulk suspend / bulk activate / bulk export to CSV

### 19.4 User Detail Modal

Clicking a user's name opens a detail panel showing:
- Full profile (email, campus ID, faculty, department, academic year, level)
- Verification status with `verifiedAt` timestamp
- Recent activity summary (last 5 audit events)
- Active reservations count
- Submissions count by status

---

## 20. Admin — Reservation Settings

**Route:** `/admin/reservations`  
**Reference:** Calendly admin settings, booking system configuration panels

### 20.1 Settings Available

| Setting Key | Label | Type | Default | Description |
|---|---|---|---|---|
| `MAX_CONCURRENT_RESERVATIONS` | Max concurrent users per project | Number (1–10) | 3 | How many students can have a confirmed reservation on the same project at the same time |
| `SLOT_DURATION_HOURS` | Slot duration | Dropdown (1h / 2h / 4h) | 2 hours | How long each reservation time slot lasts |
| `CANCELLATION_WINDOW_MINUTES` | Cancellation deadline | Number (minutes) | 30 | How many minutes before a slot starts that cancellation is still allowed |
| `NO_SHOW_THRESHOLD` | No-show restriction threshold | Number (1–10) | 3 | How many no-shows before a user's booking privileges are suspended |
| `WAITLIST_RESPONSE_WINDOW_HOURS` | Waitlist claim window | Number (hours) | 2 | How long a notified waitlist user has to claim the available slot |
| `RESERVATION_SYSTEM_ENABLED` | Enable reservation system | Toggle | ON | Global on/off switch for the entire reservation system |
| `REGULATED_HOURS_START` | Access hours start | Time Picker | 08:00 | Time of day when memoir access becomes available |
| `REGULATED_HOURS_END` | Access hours end | Time Picker | 17:00 | Time of day when memoir access closes |

### 20.2 Save Behavior

Every setting change:
1. Shows a confirmation modal: *"Changing [Setting Name] from [old value] to [new value]. This will take effect immediately for all future reservations."*
2. On confirm: Updates `SystemSetting` record, creates `AuditLog(SETTINGS_CHANGED)` with `detailsJson = {"key": "...", "oldValue": "...", "newValue": "..."}`
3. Shows success toast: *"Setting updated successfully."*

---

## 21. Admin — Access Schedule

**Route:** `/admin/schedule`  
**Reference:** Google Calendar admin view, Outlook admin scheduling

### 21.1 Weekly Schedule View

Visual calendar grid showing the full week, each day divided into hourly blocks.

- **Blue blocks:** Active access hours (e.g. Mon–Fri 8AM–5PM)
- **Gray blocks:** Outside access hours (no reservations possible)
- **Red blocks:** Manually configured closure dates (holidays, maintenance)

**Editing:** Clicking on a day block allows adjusting start/end times for that day.

### 21.2 Per-Day Configuration

Each day of the week can be independently configured:
- Enable / Disable access for that day
- Set start time and end time
- Option: "Apply to all weekdays" button

### 21.3 Closure Date Management

A separate section below the weekly view:
- List of all configured closure dates (e.g. national holidays, exam periods, maintenance windows)
- "Add Closure Date" button → date picker + optional label (e.g. *"Independence Day"*, *"Server Maintenance"*)
- Closure dates appear as red blocks on the calendar
- Reservations cannot be made on closure dates

---

## 22. Admin — Platform Settings

**Route:** `/admin/settings`  
**Reference:** WordPress admin settings, GitHub organization settings

### 22.1 Branding Settings

| Setting | Type | Description |
|---|---|---|
| Platform Name | Text | Displayed in the browser tab, header, and emails |
| Institution Name | Text | Displayed in footers and watermarks |
| AUCA Logo | Image Upload | PNG/SVG. Displayed in header, login page, and emails |
| Primary Color | Color Picker | Override the default blue. Used in buttons, header, links |
| Email Sender Name | Text | The "From" name in notification emails |
| Email Sender Address | Email | The "From" email address in notification emails |

### 22.2 Email Notification Templates

Configurable text templates for each `NotificationType`. Each template has:
- Subject line
- Body text (with placeholders: `{projectTitle}`, `{userName}`, `{reservationDate}`, etc.)
- Preview button to see how the email will look

### 22.3 File Upload Policies

| Setting | Type | Default |
|---|---|---|
| Maximum file size (MB) | Number | 50 |
| Allowed file types | Multi-select | PDF only (can add more in future) |
| Malware scan provider | Dropdown | ClamAV / External API |
| SHA-256 verification | Toggle | ON |

### 22.4 Backup & Recovery

| Feature | Description |
|---|---|
| Manual Backup | "Trigger Backup Now" button. Creates `AuditLog(BACKUP_TRIGGERED)`. Shows progress indicator. |
| Backup History | Table of all past backups: timestamp, size, status (success/failed), download link |
| Backup Schedule | Configure automatic daily backup time |
| Restore | Upload a backup file or select from history. Shows confirmation modal with warning: *"Restoring from backup will overwrite all current data. This cannot be undone."* |

### 22.5 Rejection Templates Management

Admins can create, edit, and delete the moderation rejection reason templates that appear in the moderator's dropdown:
- Template Name (internal identifier)
- Template Text (shown to students/lecturers in rejection notifications)
- "Add Template" / "Edit" / "Delete" actions

---

## 23. GitHub Integration

**Reference:** Zenodo GitHub integration, Figshare GitHub sync, ORCID works import

### 23.1 OAuth Connection

Students connect their GitHub account during project submission Step 3. The platform uses GitHub OAuth 2.0:
- Scope requested: `repo:read` (read-only access to repository list and content)
- Token stored securely (encrypted) for the duration of the submission session
- After submission, only public metadata is retained — no ongoing access token is stored

### 23.2 Repository Metadata Sync

A scheduled Spring `@Scheduled` job runs every 24 hours and:
1. Queries all `GithubRepo` records where `lastSyncedAt` is older than 24 hours
2. For each, calls the GitHub API:
   - Fetches latest star count, fork count, primary language, `isPrivate` status
   - Fetches README.md content for the `defaultBranch`
3. Updates the `GithubRepo` record and sets `lastSyncedAt = now()`
4. Creates an `AuditLog` entry for any significant changes (e.g. repository became private)

### 23.3 Manual Sync

Admins and moderators can trigger a manual sync on a specific project's GitHub tab: *"Sync now"* link. Useful when a student reports their README was recently updated and the cached version is stale.

### 23.4 Error Handling

| Scenario | Behavior |
|---|---|
| GitHub API rate limit hit | Sync job skips and retries in 1 hour. Creates `AuditLog` entry. |
| Repository not found (deleted or made private) | `GithubRepo.isPrivate = true`, README shows *"Repository no longer accessible"* |
| OAuth token expired | Student is prompted to reconnect GitHub on their next submission |

---

## 24. Search Engine

**Reference:** Elasticsearch documentation, Algolia search, Solr academic search implementations

### 24.1 Indexed Fields

The search engine indexes the following fields from `Project` and `Publication`:

| Field | Weight | Notes |
|---|---|---|
| `title` | Very High | Exact and fuzzy match |
| `abstractText` | High | Full-text search |
| `keywords` | High | Exact match on individual tags |
| `technologiesUsed` (Project) | Medium | Exact match on technology names |
| `department` | Medium | Exact match |
| `academicYear` | Low | Exact match |
| Author names (from `ProjectAuthor` / `PublicationAuthor`) | Medium | Full name search |
| `supervisorName` | Low | Exact match |
| `journalOrConferenceName` (Publication) | Medium | Exact and fuzzy |
| GitHub `repoName` / `readmeContent` | Low | Full-text on README |

### 24.2 Search Features

| Feature | Description |
|---|---|
| **Full-text search** | Searches across all indexed fields simultaneously |
| **Fuzzy matching** | Tolerates typos (e.g. "machne lerning" → "machine learning") |
| **Phrase matching** | Quotes force exact phrase search: `"mobile health"` |
| **Boolean operators** | `AND`, `OR`, `NOT` for advanced queries |
| **Stemming** | "publishing" matches "publish", "published", "publisher" |
| **Relevance ranking** | Results sorted by relevance score (weighted field matches) |
| **Instant suggestions** | Real-time autocomplete as user types (debounced 300ms) |
| **Search within results** | Filters apply on top of search results, not separately |

### 24.3 Performance Target
- Simple keyword search: < 200ms response time
- Complex multi-field search with filters: < 500ms response time

### 24.4 AcademIQ Integration
AcademIQ AI translates natural language queries into structured search queries before passing them to the search engine. Example:

- User types: *"find IT department projects from last year about web development"*
- AI extracts: `department = "Information Technology"`, `academicYear = "2023-2024"`, `keywords CONTAINS "web development"`
- Search engine executes structured query

---

## 25. Security Architecture

**Reference:** OWASP Top 10, Spring Security best practices, NIST Cybersecurity Framework

### 25.1 Authentication Security

| Control | Implementation |
|---|---|
| Password hashing | BCrypt with cost factor 12 |
| Session tokens | JWT with RS256 signing (asymmetric key pair) |
| Token expiry | Access token: 8 hours. Memoir session token: duration of reservation slot |
| Token storage | HttpOnly cookies (not localStorage) to prevent XSS theft |
| CSRF protection | Spring Security CSRF tokens on all state-changing requests |
| HTTPS | TLS 1.2+ enforced on all endpoints. HTTP redirects to HTTPS |

### 25.2 API Security

| Control | Implementation |
|---|---|
| RBAC enforcement | Every endpoint annotated with `@PreAuthorize`. Checked by Spring Security at the method level, not just in the controller |
| Rate limiting | Login: 5 attempts / 15 minutes per IP. API: 100 requests / minute per user |
| Input validation | All request bodies validated with `@Valid` + Bean Validation. SQL injection prevented by JPA parameterized queries |
| CORS | Strict CORS policy — only the platform's own frontend origin is allowed |
| Content Security Policy | CSP headers preventing injection of external scripts |

### 25.3 File Security

| Control | Implementation |
|---|---|
| Private storage | All memoir and publication files stored in a private bucket. No public URLs exist |
| Signed URLs | Temporary signed URLs (30-second expiry) generated per page request for the viewer |
| MIME validation | File type verified by reading magic bytes, not just the file extension |
| Malware scanning | ClamAV or equivalent scans every uploaded file before `scanStatus` is set to `CLEAN` |
| Checksum verification | SHA-256 hash stored and periodically re-verified against stored file |

### 25.4 Memoir Viewer Security (Summary)

- No PDF binary ever sent to the client
- Server-side page rendering to image (PNG/WebP)
- Signed single-use image URLs (30-second expiry)
- Dynamic watermark applied server-side on every page
- Right-click, Ctrl+S, Ctrl+P blocked client-side
- `user-select: none` on all content elements
- `Cache-Control: no-store` on all page image responses
- JWT session token validated on every page request

### 25.5 Audit & Monitoring

- Every significant event logged to immutable `AuditLog`
- Failed login attempts monitored for brute-force patterns
- Admin receives alert if > 10 `LOGIN_FAILED` events from the same IP within 5 minutes
- `PageViewLog` records cannot be deleted — full reading history preserved permanently

---

## 26. Error Handling Standards

All errors are presented to users as specific, actionable messages with error codes. Generic messages like "Something went wrong" are never used.

### 26.1 Error Code Reference

| Code | Category | When It Occurs | User-Facing Message |
|---|---|---|---|
| `ERR-VER-404` | Verification | Campus ID not in AUCA database | *"Your ID was not found in the AUCA campus database. Contact ICT support if you believe this is an error."* |
| `ERR-AUTH-401` | Authentication | Wrong password | *"Incorrect password. You have X attempts remaining."* |
| `ERR-AUTH-403` | Authorization | Account suspended | *"Your account has been suspended. Contact ICT Help Desk."* |
| `ERR-AUTH-429` | Rate Limiting | Too many login attempts | *"Account temporarily locked for 15 minutes."* |
| `ERR-RES-001` | Reservation | Time slot fully booked | *"This time slot is fully booked. Please select another slot or join the waitlist."* |
| `ERR-RES-002` | Reservation | Outside regulated hours | *"Reservations can only be made for access hours: Mon–Fri, 8:00 AM – 5:00 PM."* |
| `ERR-RES-003` | Reservation | Past date selected | *"You cannot make a reservation for a past time slot."* |
| `ERR-RES-004` | Reservation | User has no-show restriction | *"Your reservation privileges are currently suspended due to missed sessions. Contact ICT Help Desk."* |
| `ERR-ACC-403` | Viewer Access | No active reservation | *"Access denied: You do not have an active reservation for this project at this time."* |
| `ERR-ACC-408` | Viewer Access | Session has expired | *"Your session has ended. Your reserved time slot has passed."* |
| `ERR-ACC-409` | Viewer Access | Invalid session token | *"This session is no longer valid. Please start a new reservation."* |
| `ERR-UPL-001` | Upload | Wrong file type | *"Only PDF files are accepted for upload."* |
| `ERR-UPL-002` | Upload | File too large | *"File exceeds the 50MB size limit. Please compress your PDF and try again."* |
| `ERR-UPL-003` | Upload | Malware detected | *"This file was flagged by our security scanner. Please re-upload a clean version."* |
| `ERR-UPL-004` | Upload | Duplicate file | *"This exact file has already been uploaded to the repository. If this is a different document, please verify you have uploaded the correct file."* |
| `ERR-DUP-001` | Submission | Potential duplicate project | *"A similar project may already exist in the repository. Please review the existing work before submitting."* |
| `ERR-GIT-001` | GitHub | Repository not found | *"The selected repository could not be accessed. Please ensure it exists and you have authorized access."* |
| `ERR-GIT-002` | GitHub | No commits found | *"No commits were found in this repository. Please push your code to GitHub before linking."* |

### 26.2 Error Presentation

- **Inline form errors:** Appear directly below the invalid field in red, with the error code in a smaller gray font
- **Toast notifications:** For action-level errors (e.g. reservation failed), appears in top-right, red background, auto-dismisses after 8 seconds (longer than success toasts because errors need more time to read)
- **Full-page errors:** For access errors (403, 404, expired sessions) — centered card with illustration, error message, and navigation options
- **Modal errors:** For errors during multi-step processes — shown inside the modal without closing it so the user does not lose their progress

---

## 27. Non-Functional Requirements

### 27.1 Performance

| Metric | Target |
|---|---|
| Page initial load | < 2 seconds on a standard university network connection |
| Search results | < 500ms from query submission to results displayed |
| File upload | Progress bar must update at least every 500ms |
| Memoir page rendering | < 1 second per page (server-side render + watermark + delivery) |
| Reservation slot availability check | < 200ms real-time response |
| Dashboard stats load | < 1 second (data cached server-side, refreshed every 5 minutes) |

### 27.2 Scalability

| Metric | Target |
|---|---|
| Concurrent users | Support 200 simultaneous users without degradation |
| File storage | No practical limit — files stored in scalable object storage |
| Database records | Designed to handle 10,000+ projects and publications |
| Search index | Scales with number of records — no hard limit |

### 27.3 Availability

| Metric | Target |
|---|---|
| Uptime | 99% uptime during regulated access hours (Mon–Fri 8AM–5PM) |
| Planned maintenance | Communicated 48 hours in advance via `SYSTEM_ANNOUNCEMENT` notification |
| Data backup | Daily automated backup. Manual backup available any time |
| Recovery Time Objective | < 4 hours from backup to restoration |

### 27.4 Browser Compatibility

| Browser | Version Support |
|---|---|
| Google Chrome | Latest and previous 2 major versions |
| Mozilla Firefox | Latest and previous 2 major versions |
| Microsoft Edge | Latest and previous 2 major versions |
| Safari | Latest and previous 2 major versions |
| Mobile Chrome (Android) | Latest |
| Mobile Safari (iOS) | Latest |

---

## 28. Accessibility Standards

**Standard:** WCAG 2.1 Level AA  
**Reference:** WCAG 2.1 guidelines, UK Government Design System accessibility patterns

| Requirement | Implementation |
|---|---|
| Color contrast | All text meets 4.5:1 contrast ratio against its background. Large text meets 3:1 |
| Keyboard navigation | All interactive elements reachable and operable by keyboard. Tab order is logical |
| Focus indicators | Visible focus ring on all interactive elements (blue outline, `--primary-500`) |
| Screen reader support | All images have descriptive `alt` attributes. Form fields have associated `<label>` elements. ARIA roles on complex components (tabs, modals, dropdowns) |
| Error identification | Form errors are identified by both color (red) and icon (✗) and text — never color alone |
| Status announcements | Dynamic updates (search results loaded, toast notifications) announced via `aria-live` regions |
| Text resizing | Interface remains functional and readable at 200% browser text zoom |
| No seizure risk | No animations flash more than 3 times per second |
| Motion sensitivity | `@media (prefers-reduced-motion)` respected — animations are disabled for users who have requested reduced motion in their OS settings |
| Session timeout warning | The 5-minute session expiry warning modal receives keyboard focus automatically |

---

## 29. Mobile & Responsive Design

**Reference:** Material Design responsive layout, GOV.UK responsive patterns

### 29.1 Breakpoints

| Breakpoint | Width | Layout |
|---|---|---|
| Mobile | < 768px | Single column, collapsed sidebar (hamburger menu) |
| Tablet | 768px – 1024px | Two-column browse grid, collapsible filter sidebar |
| Desktop | > 1024px | Full layout as designed |

### 29.2 Mobile-Specific Adaptations

| Page | Mobile Adaptation |
|---|---|
| Login | Full-width card, no background pattern |
| Dashboard | Stats cards stack vertically (2 per row → 1 per row) |
| Browse | Single-column card grid. Filters collapsed behind a "Filters" button |
| Project Detail | Tabs remain but scroll horizontally if needed. GitHub tab shows summary only |
| Reservation Modal | Full-screen modal. Calendar shows 5 days instead of 7 |
| Memoir Viewer | Optimized for reading on tablet. Full-screen mode is the default. Page thumbnails hidden |
| Moderation Queue | Table collapses to card list on mobile |
| Admin Pages | Admin pages require tablet or desktop (access restricted on mobile with a friendly message) |

### 29.3 Memoir Viewer on Mobile
The memoir viewer is functional on tablet (landscape orientation) and displays a notice on mobile phones:
*"The Memoir Viewer works best on a tablet or desktop. On a phone, some functionality may be limited. Please use a larger screen for the best reading experience."*

---

## 30. Complete Page Inventory

Every page in the system, its route, who can access it, and its primary purpose.

| # | Page | Route | Access | Purpose |
|---|---|---|---|---|
| 1 | Login | `/login` | Public | Authentication gateway |
| 2 | Register | `/register` | Public | New account creation |
| 3 | Forgot Password | `/forgot-password` | Public | Password reset request |
| 4 | Dashboard (Student) | `/dashboard` | Student | Personal hub, stats, submissions, reservations |
| 5 | Dashboard (Lecturer) | `/dashboard` | Lecturer | Personal hub, stats, publications, supervised students |
| 6 | Dashboard (Moderator) | `/dashboard` | Moderator | Pending queue, stats, recent actions |
| 7 | Dashboard (Admin) | `/dashboard` | Admin | Platform health, metrics, quick links |
| 8 | Browse & Search | `/browse` | All authenticated | Discover projects and publications |
| 9 | Project Detail | `/projects/:id` | All authenticated | View full project information and access controls |
| 10 | Publication Detail | `/publications/:id` | All authenticated | View full publication information |
| 11 | Submit Project (Wizard) | `/submit/project` | Student | 3-step project submission |
| 12 | Edit Project Draft | `/submit/project/:id` | Student (own drafts) | Edit a saved draft |
| 13 | Submit Publication (Wizard) | `/submit/publication` | Lecturer | 3-step publication submission |
| 14 | Edit Publication Draft | `/submit/publication/:id` | Lecturer (own drafts) | Edit a saved draft |
| 15 | My Submissions | `/my-submissions` | Student | Track own project submissions |
| 16 | My Publications | `/my-publications` | Lecturer | Track own publication submissions |
| 17 | My Reservations | `/my-reservations` | All authenticated | Manage all reservations and waitlist entries |
| 18 | Memoir Viewer | `/viewer/:sessionToken` | Token holder only | Secure, time-limited, watermarked document reader |
| 19 | Moderation Queue | `/moderation` | Moderator, Admin | Review and act on pending content |
| 20 | Notifications | `/notifications` | All authenticated | Full notification history |
| 21 | User Profile | `/profile` | All authenticated | Edit own profile, change password, session management |
| 22 | Admin: User Management | `/admin/users` | Admin | View, manage, suspend all platform users |
| 23 | Admin: Reservation Settings | `/admin/reservations` | Admin | Configure all reservation system parameters |
| 24 | Admin: Access Schedule | `/admin/schedule` | Admin | Configure regulated access hours and closures |
| 25 | Admin: Reports | `/admin/reports` | Admin (full), Moderator (limited) | Analytics, statistics, and data export |
| 26 | Admin: Audit Logs | `/admin/reports` (Audit tab) | Admin | Immutable system event log |
| 27 | Admin: Platform Settings | `/admin/settings` | Admin | Branding, email templates, file policies, backup |
| 28 | 403 Forbidden | `/403` | All | Friendly unauthorized access page |
| 29 | 404 Not Found | `/404` | All | Friendly page not found page |
| 30 | Session Expired | `/session-expired` | All | Post-session expiry landing with next steps |

---

## Document Summary

This specification covers **30 pages**, **17 entity types**, **4 user roles**, **5 logical system groups**, and every feature required by the AUCA Connect Publication Hub from first login through to permanent archiving, controlled access, and administrative oversight.

### Feature Count Summary

| Category | Features Specified |
|---|---|
| Authentication & Identity | 8 features |
| Submission Workflows | 12 features |
| Browse & Discovery | 10 features |
| Reservation System | 9 features |
| Memoir Viewer (Security) | 10 features |
| Moderation & Approval | 8 features |
| AI Assistant (AcademIQ) | 5 features |
| Notification System | 11 notification types |
| Waitlist System | 5 features |
| Reports & Analytics | 6 report categories |
| Admin Management | 8 admin modules |
| Security Controls | 20+ controls |
| Error Handling | 17 error codes |
| **Total** | **100+ distinct features** |

---

> **Document Classification:** Internal — University Use Only  
> **Institution:** Adventist University of Central Africa (AUCA), Kigali — Rwanda  
> **Prepared by:** Serge Benit (ID: 27311) & Akize Israel (ID: 25883)  
> **Document Version:** 1.0 · 2025  
> © 2025 AUCA Connect Publication Hub — All Rights Reserved
