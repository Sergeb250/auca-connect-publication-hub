# AUCA Connect Publication Hub
## Backend Features & API Endpoint Specification
### Complete Flow-Based Reference Document

> **Institution:** Adventist University of Central Africa (AUCA), Kigali — Rwanda  
> **Project:** AUCA Connect Publication Hub  
> **Stack:** Spring Boot · JPA · Spring Security · JWT  
> **Document Type:** Backend Features & Endpoint Reference  
> **Version:** 1.0  
> **Prepared by:** Serge Benit & Akize Israel  
> **Classification:** Internal — University Use Only

---

## Table of Contents

1. [Backend Architecture Overview](#1-backend-architecture-overview)
2. [Global Standards Applied to Every Endpoint](#2-global-standards-applied-to-every-endpoint)
3. [Feature 1 — Authentication & Campus Verification](#3-feature-1--authentication--campus-verification)
4. [Feature 2 — User Profile & Session Management](#4-feature-2--user-profile--session-management)
5. [Feature 3 — Project Submission Workflow](#5-feature-3--project-submission-workflow)
6. [Feature 4 — File Upload & Security Scanning](#6-feature-4--file-upload--security-scanning)
7. [Feature 5 — GitHub Repository Integration](#7-feature-5--github-repository-integration)
8. [Feature 6 — Publication Submission Workflow](#8-feature-6--publication-submission-workflow)
9. [Feature 7 — Browse & Discovery](#9-feature-7--browse--discovery)
10. [Feature 8 — Search Engine](#10-feature-8--search-engine)
11. [Feature 9 — Reservation System](#11-feature-9--reservation-system)
12. [Feature 10 — Waitlist System](#12-feature-10--waitlist-system)
13. [Feature 11 — Memoir Viewer (Security-Critical)](#13-feature-11--memoir-viewer-security-critical)
14. [Feature 12 — Moderation & Approval Workflow](#14-feature-12--moderation--approval-workflow)
15. [Feature 13 — Notification System](#15-feature-13--notification-system)
16. [Feature 14 — Reports & Analytics](#16-feature-14--reports--analytics)
17. [Feature 15 — Audit Log](#17-feature-15--audit-log)
18. [Feature 16 — Admin — User Management](#18-feature-16--admin--user-management)
19. [Feature 17 — Admin — Reservation Settings](#19-feature-17--admin--reservation-settings)
20. [Feature 18 — Admin — Access Schedule](#20-feature-18--admin--access-schedule)
21. [Feature 19 — Admin — Platform Settings & Backup](#21-feature-19--admin--platform-settings--backup)
22. [Feature 20 — Scheduled Background Jobs](#22-feature-20--scheduled-background-jobs)
23. [Global Error Codes & HTTP Status Reference](#23-global-error-codes--http-status-reference)
24. [Complete Endpoint Index](#24-complete-endpoint-index)

---

## 1. Backend Architecture Overview

### 1.1 Layer Structure

The backend is organized into four layers. Each layer has one job and does not cross into the responsibility of another.

| Layer | Responsibility | Contains |
|---|---|---|
| **Controller** | Receives HTTP requests, validates input, delegates to service, returns HTTP response | `@RestController` classes |
| **Service** | Business logic, role enforcement, orchestration between repositories | `@Service` classes with `@PreAuthorize` |
| **Repository** | Database queries only — no business logic | `@Repository` interfaces extending `JpaRepository` |
| **Model** | JPA entity definitions, enums, base classes | `@Entity` classes |

### 1.2 Security Model

Every request except login and registration must carry a valid JWT in the `Authorization: Bearer <token>` header. The JWT filter runs before every controller and populates the Spring Security context with the user's `id`, `email`, and `role`. Every service method then checks the role with `@PreAuthorize` before executing any logic.

Two JWT token types exist in this system:

| Token Type | Purpose | Expiry |
|---|---|---|
| **Login Token** | Standard access token for all API requests | 8 hours of inactivity |
| **Viewer Session Token** | Scoped to one user + one project + one reservation window. Used on every memoir page request | Exactly when the reservation slot ends |

### 1.3 Response Envelope

Every API response follows this consistent structure:

**Success:**
```
HTTP 200 / 201 / 204
{
  "data": { ... },
  "message": "Operation successful"
}
```

**Error:**
```
HTTP 4xx / 5xx
{
  "errorCode": "ERR-RES-001",
  "message": "This time slot is fully booked.",
  "timestamp": "2025-03-18T10:14:22"
}
```

### 1.4 Audit Logging Rule

Every backend feature that changes data — every creation, update, approval, rejection, login, upload — automatically writes an immutable record to the `audit_logs` table. This happens asynchronously in a background thread so it never slows down the main request. The audit log table uses `AppendOnlyBaseEntity`, meaning any attempt to update or delete an audit record throws an `UnsupportedOperationException` at the JPA level.

---

## 2. Global Standards Applied to Every Endpoint

### 2.1 Input Validation

Every request body is validated with Bean Validation (`@Valid`). Fields marked as required throw a `400 Bad Request` with a descriptive message listing exactly which fields failed and why before any business logic runs.

### 2.2 Role Enforcement

Role checks happen at the service method level with `@PreAuthorize`, not just in the controller. This means even internal service-to-service calls respect role boundaries.

### 2.3 Ownership Checks

For any operation on data a user owns (edit own draft, cancel own reservation), the service validates that the authenticated user's ID matches the record's owner ID before proceeding. Failing this check returns `403 Forbidden`, not `404 Not Found`, to prevent data enumeration.

### 2.4 Pagination

All list endpoints accept `page` (0-indexed), `size` (default 12, max 50), and `sort` parameters. All paginated responses include `totalElements`, `totalPages`, `currentPage`, and `size` alongside the content array.

### 2.5 Idempotency

`GET` endpoints are always safe — they never change state. `POST` creates a new record. `PATCH` updates specific fields of an existing record. `DELETE` removes or logically deletes. `PUT` is not used — partial updates with `PATCH` are the standard.

---

## 3. Feature 1 — Authentication & Campus Verification

### 3.1 What This Feature Does

Authentication controls who can enter the system. Every person who wants to use AUCA Connect must prove two things: first, that they know their account password, and second, that they are a real member of the AUCA campus community. The campus verification step calls the AUCA student information system database and confirms the person's enrollment status, faculty, department, and academic level. If the campus database does not recognize them, they cannot create an account or log in regardless of whether they know the password.

After successful authentication, the backend issues a signed JWT that the frontend includes in every subsequent request. The backend validates this token on every call — if it is expired, tampered with, or missing, the request is rejected.

### 3.2 Login Flow (Step by Step)

```
1. Frontend sends email + password
2. Backend looks up user by email in the database
3. If user not found → ERR-VER-404
4. If user status = SUSPENDED → ERR-AUTH-403, log LOGIN_FAILED
5. If user status = PENDING_VERIFICATION → ERR-AUTH-202
6. Backend compares submitted password against stored BCrypt hash
7. If password wrong → ERR-AUTH-401, increment failed attempt counter, log LOGIN_FAILED
8. If failed attempts >= 5 within 15 minutes → ERR-AUTH-429, lock account for 15 minutes
9. Password correct → generate JWT login token (expires 8 hours)
10. Update user.lastLoginAt = now()
11. Log AuditLog(LOGIN) asynchronously
12. Return token + user profile to frontend
```

### 3.3 Registration Flow (Step by Step)

```
1. Frontend sends email + campusId + password + confirmPassword
2. Backend checks email is not already registered → ERR-AUTH-409 if taken
3. Backend calls Campus Verification Service with email + campusId
4. Campus Verification Service calls AUCA campus database API
5. If campus database returns not found → ERR-VER-404
6. If campus database returns not enrolled → ERR-VER-403
7. If campus database is unavailable → ERR-VER-503 (safe failure — do not allow bypass)
8. Campus data returned: firstName, lastName, faculty, department, academicYear, academicLevel
9. Backend hashes password with BCrypt (cost factor 12)
10. Backend creates User record with status = ACTIVE and role determined by academicLevel:
    - UNDERGRADUATE / POSTGRADUATE → role = STUDENT
    - LECTURER / STAFF → role = LECTURER
11. Backend creates CampusProfile record with data from campus database
12. Sets campusProfile.verifiedAt = now()
13. Sends welcome email asynchronously
14. Generates JWT and returns AuthResponse
```

### 3.4 Campus Verification Logic

The Campus Verification Service is a dedicated service that wraps all communication with the AUCA campus database. It is isolated so that if the integration method changes (REST API vs direct DB connection vs LDAP), only this one service needs updating and nothing else in the system changes.

What it verifies:
- The `campusId` belongs to a real person in the AUCA database
- The person is currently enrolled (for students) or actively employed (for staff/lecturers)
- The person's academic details are current

What it returns:
- `firstName`, `lastName` — pre-fills the User record
- `faculty`, `department`, `academicYear`, `academicLevel` — pre-fills the CampusProfile
- `supervisorName` — pre-fills the student's supervisor if the campus database provides it

### 3.5 Forgot Password Flow

```
1. Frontend sends email address
2. Backend looks up user by email
3. Whether or not the email exists, return the same response:
   "If this email is registered, a reset link has been sent."
   (Security best practice — never reveal whether an email exists)
4. If user found: generate a time-limited (30 minutes) signed reset token
5. Send email with reset link: /reset-password?token=<signed-token>
6. Frontend submits new password + token
7. Backend validates token signature and expiry
8. Backend hashes new password and updates user.passwordHash
9. Invalidates the reset token (one-time use)
10. Log AuditLog(PASSWORD_RESET) asynchronously
```

### 3.6 Endpoints

---

#### `POST /api/auth/login`

**Purpose:** Authenticate a user and return a JWT.  
**Access:** Public — no token required.

**Request Body:**

| Field | Type | Required | Validation |
|---|---|---|---|
| `email` | String | ✅ | Must be a valid email format |
| `password` | String | ✅ | Must not be blank |

**Success Response — `200 OK`:**

| Field | Type | Description |
|---|---|---|
| `token` | String | JWT login token — include as `Authorization: Bearer <token>` on all subsequent requests |
| `userId` | UUID | The authenticated user's ID |
| `email` | String | University email |
| `firstName` | String | First name for dashboard greeting |
| `lastName` | String | Last name |
| `role` | Enum | `STUDENT`, `LECTURER`, `MODERATOR`, or `ADMIN` — frontend uses this to render the correct dashboard |
| `department` | String | User's department from CampusProfile |
| `academicYear` | String | Academic year from CampusProfile |

**Failure Responses:**

| Scenario | HTTP Status | Error Code |
|---|---|---|
| Email not in AUCA database | `404 Not Found` | `ERR-VER-404` |
| Wrong password | `401 Unauthorized` | `ERR-AUTH-401` |
| Account suspended | `403 Forbidden` | `ERR-AUTH-403` |
| Account pending verification | `202 Accepted` | `ERR-AUTH-202` |
| Too many failed attempts | `429 Too Many Requests` | `ERR-AUTH-429` |

**Side Effects:**
- Updates `user.lastLoginAt` to current timestamp
- Writes `AuditLog(LOGIN)` record asynchronously

---

#### `POST /api/auth/register`

**Purpose:** Create a new account with campus database verification.  
**Access:** Public — no token required.

**Request Body:**

| Field | Type | Required | Validation |
|---|---|---|---|
| `email` | String | ✅ | Valid AUCA email format |
| `campusId` | String | ✅ | University ID number (e.g. `27311`) |
| `password` | String | ✅ | Minimum 8 characters |
| `confirmPassword` | String | ✅ | Must match `password` |

**Success Response — `201 Created`:**

Same fields as login response — registration immediately logs the user in.

**Failure Responses:**

| Scenario | HTTP Status | Error Code |
|---|---|---|
| Email already registered | `409 Conflict` | `ERR-AUTH-409` |
| Campus ID not found | `404 Not Found` | `ERR-VER-404` |
| Not enrolled / not active | `403 Forbidden` | `ERR-VER-403` |
| Campus DB unavailable | `503 Service Unavailable` | `ERR-VER-503` |
| Passwords do not match | `400 Bad Request` | `ERR-VALIDATION` |

**Side Effects:**
- Creates `User` record
- Creates `CampusProfile` record with campus database data
- Sends welcome email asynchronously

---

#### `POST /api/auth/forgot-password`

**Purpose:** Request a password reset link via email.  
**Access:** Public.

**Request Body:**

| Field | Type | Required |
|---|---|---|
| `email` | String | ✅ |

**Success Response — `200 OK`:**

Always returns the same response regardless of whether the email exists:
`"If this email is registered, a reset link has been sent."`

---

#### `POST /api/auth/reset-password`

**Purpose:** Submit a new password using the reset token from the email link.  
**Access:** Public.

**Request Body:**

| Field | Type | Required | Validation |
|---|---|---|---|
| `resetToken` | String | ✅ | The token from the email link |
| `newPassword` | String | ✅ | Minimum 8 characters |
| `confirmPassword` | String | ✅ | Must match `newPassword` |

**Success Response — `200 OK`:** Confirmation message.

**Failure Responses:**

| Scenario | HTTP Status | Error Code |
|---|---|---|
| Token invalid or expired | `400 Bad Request` | `ERR-AUTH-400` |
| Token already used | `409 Conflict` | `ERR-AUTH-409` |

**Side Effects:**
- Updates `user.passwordHash`
- Invalidates the reset token
- Writes `AuditLog(PASSWORD_RESET)`

---

## 4. Feature 2 — User Profile & Session Management

### 4.1 What This Feature Does

After login, users can view and update their own profile. Admins can also read any user's profile as part of user management. Profile data is split between `User` (account credentials and role) and `CampusProfile` (academic identity). Users cannot change their campus profile data — that is controlled by the campus database. They can update their display name and profile avatar.

### 4.2 Endpoints

---

#### `GET /api/users/me`

**Purpose:** Get the authenticated user's full profile.  
**Access:** Any authenticated user.

**Success Response — `200 OK`:**

| Field | Type | Description |
|---|---|---|
| `userId` | UUID | User ID |
| `email` | String | University email |
| `firstName` | String | First name |
| `lastName` | String | Last name |
| `phoneNumber` | String | Optional phone number |
| `role` | Enum | User's role |
| `status` | Enum | Account status |
| `profileAvatarUrl` | String | Profile picture URL |
| `lastLoginAt` | DateTime | Last login timestamp |
| `campusProfile` | Object | Nested campus profile object |
| `campusProfile.campusId` | String | University ID |
| `campusProfile.faculty` | String | Faculty |
| `campusProfile.department` | String | Department |
| `campusProfile.academicYear` | String | Academic year |
| `campusProfile.academicLevel` | Enum | Level |
| `campusProfile.supervisorName` | String | Supervisor (students only) |
| `campusProfile.verifiedAt` | DateTime | When campus identity was verified |

---

#### `PATCH /api/users/me`

**Purpose:** Update own profile fields (name, phone, avatar).  
**Access:** Any authenticated user.

**Request Body (all fields optional — only send what is being changed):**

| Field | Type | Validation |
|---|---|---|
| `firstName` | String | Max 100 characters |
| `lastName` | String | Max 100 characters |
| `phoneNumber` | String | Valid phone format |
| `profileAvatarUrl` | String | Valid URL |

**Success Response — `200 OK`:** Updated user profile.

---

#### `PATCH /api/users/me/password`

**Purpose:** Change own password (must know current password).  
**Access:** Any authenticated user.

**Request Body:**

| Field | Type | Required | Validation |
|---|---|---|---|
| `currentPassword` | String | ✅ | Must match stored hash |
| `newPassword` | String | ✅ | Minimum 8 characters |
| `confirmNewPassword` | String | ✅ | Must match `newPassword` |

**Side Effects:**
- Updates `user.passwordHash`
- Writes `AuditLog(PASSWORD_CHANGED)`

---

#### `POST /api/auth/logout`

**Purpose:** Log out the current session.  
**Access:** Any authenticated user.

**What it does:** Adds the current JWT to a server-side blacklist (Redis or DB table) so it cannot be reused even before it expires. Returns a `204 No Content`.

**Side Effects:**
- Writes `AuditLog(LOGOUT)`

---

## 5. Feature 3 — Project Submission Workflow

### 5.1 What This Feature Does

The project submission workflow is the primary way students archive their academic work. It is a multi-step process that mirrors the 3-step wizard on the frontend: first the student builds the project metadata record, then they upload the memoir PDF, then they link the GitHub repository. Only after all three steps are complete can the student submit the project for moderation review.

The workflow enforces these rules:
- Only `STUDENT` role users can submit projects
- A project stays in `DRAFT` status until the student explicitly submits it
- A project in `DRAFT` can be edited freely by its owner
- Once submitted (`PENDING_APPROVAL`), the student cannot edit it until the moderator either rejects it or requests a re-upload, which returns it to `DRAFT`
- Only `PUBLISHED` projects are visible to other users in browse results
- The autosave feature calls the draft endpoint every 30 seconds with partial data

### 5.2 Project Status Lifecycle

```
DRAFT
  │
  ├─── Student submits ──────────────► PENDING_APPROVAL
  │                                         │
  │                              ┌──────────┴──────────┐
  │                              ▼                      ▼
  │                           PUBLISHED             REJECTED
  │                              │
  │          ┌───────────────────┼───────────────────┐
  │          ▼                   ▼                   ▼
  │        HIDDEN            ARCHIVED          (stays PUBLISHED)
  │                              │
  │                              ▼
  │                           RESTORED → PUBLISHED
  │
  └─── Moderator requests re-upload ──► DRAFT (student edits and resubmits)
```

### 5.3 Autosave Logic

The frontend calls `POST /api/projects/draft` or `PATCH /api/projects/:id/draft` every 30 seconds with whatever fields are currently filled in. The backend accepts partial data — fields not sent are not overwritten. This means a student who fills in only the title and abstract has those fields saved even if they have not touched the other fields yet.

### 5.4 Endpoints

---

#### `POST /api/projects/draft`

**Purpose:** Create a new project draft (first autosave).  
**Access:** `STUDENT` only.

**Request Body:**

| Field | Type | Required | Validation |
|---|---|---|---|
| `title` | String | ✅ | Max 200 characters |
| `abstractText` | String | Optional for draft | Stored as-is |
| `keywords` | Array of Strings | Optional for draft | Max 10 items |
| `department` | String | Optional for draft | Must be valid AUCA department |
| `academicYear` | String | Optional for draft | Format: `YYYY-YYYY` |
| `type` | Enum | Optional for draft | `FINAL_YEAR_PROJECT`, `POSTGRADUATE_THESIS`, `COURSEWORK` |
| `category` | Enum | Optional for draft | `SOFTWARE_SYSTEM`, `RESEARCH_STUDY`, `DATA_ANALYSIS`, `MOBILE_APP`, `WEB_APP`, `OTHER` |
| `technologiesUsed` | Array of Strings | Optional | Max 20 items |
| `supervisorName` | String | Optional | Max 100 characters |
| `coAuthors` | Array of Objects | Optional | Each: `fullName`, `email`, `studentId`, `role`, `authorOrder` |

**Success Response — `201 Created`:**

| Field | Type | Description |
|---|---|---|
| `id` | UUID | The new project's ID — frontend stores this and uses it for all subsequent draft updates |
| `status` | Enum | Always `DRAFT` |
| `title` | String | Saved title |
| `createdAt` | DateTime | When the draft was created |

**Side Effects:**
- Creates `Project` record with `status = DRAFT`
- Creates `ProjectAuthor` records for co-authors if provided

---

#### `PATCH /api/projects/:id/draft`

**Purpose:** Update an existing project draft (autosave updates).  
**Access:** `STUDENT` — owner only (verified by ownership check).

**Path Parameter:**

| Parameter | Type | Description |
|---|---|---|
| `id` | UUID | The project draft ID returned when the draft was created |

**Request Body:** Same fields as `POST /api/projects/draft`. All fields are optional — only send what changed.

**Business Rules:**
- Returns `403 Forbidden` if the project does not belong to the authenticated student
- Returns `400 Bad Request` if the project status is not `DRAFT` (submitted projects cannot be edited)
- Co-authors list replaces the existing list entirely on each update

**Success Response — `200 OK`:** Full updated project draft.

---

#### `POST /api/projects/:id/submit`

**Purpose:** Submit a completed draft for moderation review.  
**Access:** `STUDENT` — owner only.

**Path Parameter:**

| Parameter | Type | Description |
|---|---|---|
| `id` | UUID | The project ID to submit |

**No request body needed.** The backend reads the current state of the project.

**Pre-submission Validation (all must pass before status changes):**

| Check | Error if Failed |
|---|---|
| All required metadata fields are present | `ERR-VALIDATION` with list of missing fields |
| `MemoirFile` exists for this project | `ERR-VALIDATION: A memoir PDF must be uploaded before submission.` |
| `MemoirFile.scanStatus = CLEAN` | `ERR-VALIDATION: The memoir file has not passed the security scan yet.` |
| `GithubRepo` exists with a `finalCommitHash` | `ERR-VALIDATION: A GitHub repository with a final commit must be linked before submission.` |
| Project status is `DRAFT` | `ERR-VALIDATION: Only drafts can be submitted.` |

**Success Response — `200 OK`:**

| Field | Type | Description |
|---|---|---|
| `id` | UUID | Project ID |
| `status` | Enum | Now `PENDING_APPROVAL` |
| `submittedAt` | DateTime | When submission occurred |

**Side Effects:**
- Sets `project.status = PENDING_APPROVAL`
- Sends `Notification(PROJECT_SUBMITTED_CONFIRMED)` to the student
- Writes `AuditLog(PROJECT_SUBMITTED)`
- Project now appears in the moderator's pending queue

---

#### `DELETE /api/projects/:id`

**Purpose:** Delete a draft that has not yet been submitted.  
**Access:** `STUDENT` — owner only.

**Business Rules:**
- Only `DRAFT` status projects can be deleted
- Deleting a draft also deletes its `MemoirFile` record and removes the stored file from private storage
- Deleting a draft also deletes its `GithubRepo` record
- Once submitted (`PENDING_APPROVAL` or later), a student cannot delete their project

**Success Response — `204 No Content`**

**Side Effects:**
- Deletes `Project`, `MemoirFile`, `GithubRepo`, `ProjectAuthor` records
- Removes file from private storage
- Writes `AuditLog(PROJECT_DELETED)`

---

#### `GET /api/projects/my`

**Purpose:** Get the authenticated student's own submissions list.  
**Access:** `STUDENT` only.

**Query Parameters:**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `status` | Enum | All | Filter by `DRAFT`, `PENDING_APPROVAL`, `PUBLISHED`, `REJECTED` |
| `page` | Integer | `0` | Page number |
| `size` | Integer | `10` | Items per page |

**Success Response — `200 OK`:** Paginated list of student's own projects with status, title, submission date, and last updated date.

---

#### `GET /api/projects/:id`

**Purpose:** Get a single project by ID.  
**Access:** Any authenticated user.

**Access Control Logic:**
- If `status = PUBLISHED`: visible to all authenticated users
- If `status = DRAFT` or `PENDING_APPROVAL`: visible only to the owner student, moderators, and admins
- If `status = REJECTED`: visible only to the owner student, moderators, and admins
- If `status = HIDDEN` or `ARCHIVED`: visible only to moderators and admins

**Success Response — `200 OK`:** Full project object including:
- All metadata fields
- Author list
- Memoir file metadata (not the file itself — just metadata like page count, checksum, scan status)
- GitHub repo metadata (not code — just metadata like repo name, language, star count, final commit hash)
- Availability status for reservation (computed from active reservation count)
- Moderation history (visible to moderators and admins only)

**Side Effects:**
- Increments `project.viewCount` by 1 atomically
- Writes `AuditLog(PROJECT_VIEWED)` asynchronously

---

#### `GET /api/projects`

**Purpose:** Browse all published projects with filters and pagination.  
**Access:** Any authenticated user.

**Query Parameters:**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `department` | String | All | Filter by department name |
| `year` | String | All | Filter by academic year, e.g. `2023-2024` |
| `type` | Enum | All | `FINAL_YEAR_PROJECT`, `POSTGRADUATE_THESIS`, `COURSEWORK` |
| `category` | Enum | All | `SOFTWARE_SYSTEM`, `RESEARCH_STUDY`, etc. |
| `technology` | String | All | Filter by technology tag |
| `sort` | String | `publishedAt,desc` | `publishedAt,desc`, `viewCount,desc`, `title,asc` |
| `page` | Integer | `0` | Page number |
| `size` | Integer | `12` | Items per page, max `50` |

**Success Response — `200 OK`:** Paginated project cards including availability status for each project.

---

## 6. Feature 4 — File Upload & Security Scanning

### 6.1 What This Feature Does

Every memoir PDF and publication document goes through the same three-phase process: upload, validation, and security scanning. The file is never directly accessible via a URL — it is stored in private storage and can only be served through the tokenized viewer endpoint. The security scan runs asynchronously in a background thread so the student gets an immediate response after upload and the scan status updates when the scan completes.

### 6.2 Upload & Scan Flow

```
1. Student sends multipart/form-data with PDF file
2. Backend reads first 4 bytes (magic bytes) — must be "%PDF"
   → If not PDF: reject immediately with ERR-UPL-001
3. Backend checks file size
   → If > 50MB: reject with ERR-UPL-002
4. Backend computes SHA-256 checksum of file bytes
5. Backend checks if checksum already exists in memoir_files table
   → If yes: reject with ERR-UPL-004 (duplicate file)
6. Backend generates a UUID-based internal filename (never the original name)
7. Backend writes file to private storage at /private/memoirs/<uuid>.pdf
8. Backend creates MemoirFile record with scanStatus = PENDING
9. Backend triggers async malware scan on the stored file
10. Backend returns MemoirFile metadata (id, originalFileName, fileSize, checksum, scanStatus = PENDING)
11. [Background] Malware scan completes:
    → CLEAN: updates scanStatus = CLEAN, extracts totalPages from PDF
    → INFECTED: updates scanStatus = INFECTED, quarantines file, sends notification to student
    → FAILED: updates scanStatus = FAILED (admin alerted)
```

### 6.3 Endpoints

---

#### `POST /api/projects/:id/memoir`

**Purpose:** Upload the memoir PDF for a specific project draft.  
**Access:** `STUDENT` — owner only.

**Request:** `multipart/form-data` with a single `file` field containing the PDF.

**Business Rules:**
- Project must be in `DRAFT` status
- Only one memoir per project — if a memoir already exists, this replaces it
- Replacing a memoir resets `scanStatus` to `PENDING` and re-runs the scan

**Success Response — `201 Created`:**

| Field | Type | Description |
|---|---|---|
| `memoirFileId` | UUID | Memoir file record ID |
| `originalFileName` | String | The original filename as uploaded |
| `fileSizeBytes` | Long | File size in bytes |
| `fileSizeReadable` | String | Human-readable size, e.g. `4.2 MB` |
| `sha256Checksum` | String | SHA-256 hash — proof of file integrity |
| `scanStatus` | Enum | `PENDING` — scan is in progress |
| `message` | String | `"File uploaded successfully. Security scan in progress."` |

**Failure Responses:**

| Scenario | HTTP Status | Error Code |
|---|---|---|
| Not a PDF file | `400 Bad Request` | `ERR-UPL-001` |
| File too large | `413 Payload Too Large` | `ERR-UPL-002` |
| Duplicate file | `409 Conflict` | `ERR-UPL-004` |
| Project not in DRAFT | `400 Bad Request` | `ERR-VALIDATION` |

---

#### `GET /api/projects/:id/memoir/scan-status`

**Purpose:** Poll the current scan status of the memoir file.  
**Access:** `STUDENT` — owner only. Also `MODERATOR` and `ADMIN`.

**What the frontend does:** After uploading, the frontend polls this endpoint every 3 seconds until `scanStatus` is no longer `PENDING`. This is how the real-time scan progress indicator works without WebSockets.

**Success Response — `200 OK`:**

| Field | Type | Description |
|---|---|---|
| `scanStatus` | Enum | `PENDING`, `CLEAN`, `INFECTED`, `FAILED` |
| `totalPages` | Integer | Page count — only present when `scanStatus = CLEAN` |
| `message` | String | Human-readable status message |

---

#### `POST /api/publications/:id/document`

**Purpose:** Upload the document file for a publication draft.  
**Access:** `LECTURER` — owner only.

**Request:** Same `multipart/form-data` format as memoir upload.

**Additional Request Field:**

| Field | Type | Required | Description |
|---|---|---|---|
| `versionLabel` | String | Optional | e.g. `Author Accepted Manuscript`, `Preprint` |

**Success Response — `201 Created`:** Same structure as memoir upload response plus `versionLabel`.

---

## 7. Feature 5 — GitHub Repository Integration

### 7.1 What This Feature Does

Students link their GitHub repository to their project as the source code submission. The system stores the final commit hash as the permanent, irrevocable record of what code was submitted. Even if the student pushes more commits after submission, the academic record always points to the designated final commit. The backend fetches repository metadata from the GitHub API and caches it to enable rich display without live API calls on every page view.

### 7.2 GitHub Link Flow

```
1. Student completes GitHub OAuth 2.0 on the frontend
2. Frontend receives a short-lived OAuth access token from GitHub
3. Frontend sends: repoUrl + finalCommitHash + finalTag (optional) + accessToken
4. Backend parses owner and repo name from repoUrl
5. Backend calls GitHub API /repos/{owner}/{repo} to fetch metadata
6. Backend calls GitHub API to fetch README.md content
7. Backend calls GitHub API to fetch the commit message for finalCommitHash
8. Backend stores all data in GithubRepo record linked to the project
9. Frontend receives full repo metadata for display
```

### 7.3 GitHub Sync (Background)

A scheduled job runs every 24 hours at 2:00 AM and refreshes star counts, fork counts, `isPrivate` status, and README content for all linked repositories. Public repositories use the unauthenticated GitHub API. Private repositories that have become inaccessible (deleted or made private without the platform having ongoing access) are flagged and the project page shows a notice.

### 7.4 Endpoints

---

#### `POST /api/projects/:id/github`

**Purpose:** Link a GitHub repository to a project draft.  
**Access:** `STUDENT` — owner only.

**Request Body:**

| Field | Type | Required | Description |
|---|---|---|---|
| `repoUrl` | String | ✅ | Full GitHub repository URL, e.g. `https://github.com/sergebenit/auca-connect` |
| `finalCommitHash` | String | ✅ | The SHA hash of the final submitted commit |
| `finalTag` | String | Optional | A Git tag designating the final version, e.g. `v1.0-final` |
| `accessToken` | String | ✅ | Short-lived GitHub OAuth token from the frontend OAuth flow |

**Success Response — `201 Created`:**

| Field | Type | Description |
|---|---|---|
| `repoUrl` | String | Repository URL |
| `repoName` | String | Repository name |
| `repoOwner` | String | GitHub username of owner |
| `defaultBranch` | String | Primary branch name |
| `finalCommitHash` | String | The designated final commit SHA |
| `finalCommitMessage` | String | The commit message at that commit |
| `finalTag` | String | Tag name if provided |
| `primaryLanguage` | String | Primary programming language |
| `starCount` | Integer | Current star count |
| `forkCount` | Integer | Current fork count |
| `isPrivate` | Boolean | Whether repository is private |
| `readmeContent` | String | Cached README.md content |
| `lastSyncedAt` | DateTime | When metadata was fetched |

**Failure Responses:**

| Scenario | HTTP Status | Error Code |
|---|---|---|
| Repository not accessible | `404 Not Found` | `ERR-GIT-001` |
| Commit hash not found | `404 Not Found` | `ERR-GIT-002` |
| No commits in repository | `400 Bad Request` | `ERR-GIT-003` |
| GitHub API rate limit | `429 Too Many Requests` | `ERR-GIT-004` |

---

#### `POST /api/projects/:id/github/sync`

**Purpose:** Manually re-sync GitHub metadata for a specific project.  
**Access:** `MODERATOR`, `ADMIN`.

**Success Response — `200 OK`:** Updated GitHub repo metadata.

---

#### `GET /api/projects/:id/github/commits`

**Purpose:** Fetch the recent commits from the linked GitHub repository for the student to select the final commit.  
**Access:** `STUDENT` — owner only.

**Query Parameters:**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `limit` | Integer | `20` | Number of recent commits to return |

**Success Response — `200 OK`:** Array of commits, each with `hash`, `message`, `author`, `date`.

---

## 8. Feature 6 — Publication Submission Workflow

### 8.1 What This Feature Does

The publication submission workflow mirrors the project submission workflow but is designed for lecturers submitting academic publications. The key differences are: publications have DOI links and journal names instead of GitHub repositories; publications have visibility settings including embargo dates; and co-authors on publications can be from external institutions (not just AUCA students).

### 8.2 Publication Status Lifecycle

Same as Project: `DRAFT → PENDING_APPROVAL → PUBLISHED` or `REJECTED`. Also supports `HIDDEN` and `ARCHIVED`.

### 8.3 Endpoints

---

#### `POST /api/publications/draft`

**Purpose:** Create a new publication draft.  
**Access:** `LECTURER` only.

**Request Body:**

| Field | Type | Required | Description |
|---|---|---|---|
| `title` | String | ✅ | Publication title, max 300 characters |
| `abstractText` | String | Optional | Full academic abstract |
| `keywords` | Array | Optional | Keyword tags |
| `type` | Enum | ✅ | `RESEARCH_PAPER`, `JOURNAL_ARTICLE`, `CONFERENCE_PAPER`, `BOOK_CHAPTER`, `TECHNICAL_REPORT`, `OTHER` |
| `department` | String | ✅ | AUCA department |
| `academicYear` | String | ✅ | Year of publication |
| `doiOrExternalLink` | String | Optional | DOI or external URL |
| `journalOrConferenceName` | String | Optional | Where this was published |
| `versionLabel` | String | Optional | `Preprint`, `Author Accepted Manuscript`, `Publisher PDF`, etc. |
| `visibility` | Enum | Optional | `AUCA_ONLY` (default), `RESTRICTED`, `EMBARGOED` |
| `embargoUntil` | Date | Optional | Required if `visibility = EMBARGOED` |
| `accessNotes` | String | Optional | Instructions for viewers |
| `notifyCoAuthors` | Boolean | Optional | Send email to co-authors when published |
| `coAuthors` | Array | Optional | Each: `fullName`, `email`, `institution`, `role`, `authorOrder` |

**Success Response — `201 Created`:** Publication draft with ID.

---

#### `PATCH /api/publications/:id/draft`

**Purpose:** Update an existing publication draft.  
**Access:** `LECTURER` — owner only.

Same body structure as `POST /api/publications/draft`. All fields optional.

---

#### `POST /api/publications/:id/submit`

**Purpose:** Submit a publication draft for moderation review.  
**Access:** `LECTURER` — owner only.

**Pre-submission Validation:**

| Check | Error if Failed |
|---|---|
| Title and abstract are present | `ERR-VALIDATION` |
| Document file is uploaded | `ERR-VALIDATION: A document must be uploaded before submission.` |
| Document scan status is `CLEAN` | `ERR-VALIDATION: Document has not passed the security scan.` |
| If `EMBARGOED`: `embargoUntil` is a future date | `ERR-VALIDATION: Embargo date must be in the future.` |

**Success Response — `200 OK`:** Publication with `status = PENDING_APPROVAL`.

**Side Effects:**
- Writes `AuditLog(PUBLICATION_SUBMITTED)`
- Publication appears in moderator's pending queue

---

#### `GET /api/publications/my`

**Purpose:** Get the authenticated lecturer's own publications.  
**Access:** `LECTURER` only.

---

#### `GET /api/publications/:id`

**Purpose:** Get a single publication by ID.  
**Access:** Any authenticated user.

**Access Control:**
- `PUBLISHED` + `AUCA_ONLY`: visible to all authenticated users
- `PUBLISHED` + `RESTRICTED`: visible to all but document access requires an access request
- `PUBLISHED` + `EMBARGOED`: visible in browse but document is locked until `embargoUntil` date
- Not published: visible only to owner, moderators, admins

**Side Effects:**
- Increments `publication.viewCount`
- Writes `AuditLog(PUBLICATION_VIEWED)`

---

#### `GET /api/publications`

**Purpose:** Browse all published publications.  
**Access:** Any authenticated user.

**Query Parameters:** Same filter pattern as `/api/projects` — `department`, `year`, `type`, `sort`, `page`, `size`.

---

## 9. Feature 7 — Browse & Discovery

### 9.1 What This Feature Does

The browse feature provides a unified discovery interface for all published content — both student projects and lecturer publications in one place. It supports department and year filtering, technology stack filtering, content type filtering, and availability status filtering. All filters can be combined and the resulting URL is shareable.

### 9.2 Endpoints

---

#### `GET /api/browse`

**Purpose:** Unified browse endpoint returning both projects and publications in a single paginated list.  
**Access:** Any authenticated user.

**Query Parameters:**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `contentType` | String | `ALL` | `ALL`, `PROJECTS`, `PUBLICATIONS`, `THESES` |
| `department` | String | All | Filter by AUCA department |
| `year` | String | All | Academic year filter |
| `type` | Enum | All | Specific project type or publication type |
| `technology` | String | All | Technology stack filter (projects only) |
| `availability` | String | `ALL` | `ALL`, `AVAILABLE`, `BY_RESERVATION`, `RESTRICTED` |
| `sort` | String | `publishedAt,desc` | Sort field and direction |
| `page` | Integer | `0` | Page number |
| `size` | Integer | `12` | Items per page |

**Success Response — `200 OK`:**

| Field | Type | Description |
|---|---|---|
| `content` | Array | Array of mixed project and publication card objects |
| `totalElements` | Long | Total matching records |
| `totalPages` | Integer | Total number of pages |
| `currentPage` | Integer | Current page number |
| `searchTimeMs` | Long | Time taken for the query in milliseconds |

Each item in `content` includes a `contentType` field (`PROJECT` or `PUBLICATION`) so the frontend knows which card template to render.

---

#### `GET /api/browse/departments`

**Purpose:** Get a list of all departments that have published content — used to populate the department filter dropdown.  
**Access:** Any authenticated user.

**Success Response — `200 OK`:** Array of department names with published content counts.

---

#### `GET /api/browse/years`

**Purpose:** Get all academic years that have published content — used to populate the year filter.  
**Access:** Any authenticated user.

**Success Response — `200 OK`:** Array of academic year strings sorted newest first.

---

#### `GET /api/browse/technologies`

**Purpose:** Get all technology tags used in published projects — used to populate the technology filter.  
**Access:** Any authenticated user.

**Success Response — `200 OK`:** Array of technology names sorted by frequency of use.

---

## 10. Feature 8 — Search Engine

### 10.1 What This Feature Does

The search engine indexes `title`, `abstractText`, `keywords`, `technologiesUsed`, `department`, author names, `journalOrConferenceName`, and GitHub `readmeContent`. It supports full-text search with fuzzy matching (tolerates typos), phrase matching with quotes, and relevance-ranked results. The AcademIQ AI assistant uses this same search infrastructure but adds natural language parsing on top — it translates a plain-language query into structured search parameters before executing.

### 10.2 Endpoints

---

#### `GET /api/search`

**Purpose:** Full-text search across all published projects and publications.  
**Access:** Any authenticated user.

**Query Parameters:**

| Parameter | Type | Required | Description |
|---|---|---|---|
| `q` | String | ✅ | The search query. Supports fuzzy matching and quoted phrases |
| `department` | String | Optional | Narrow to a department |
| `year` | String | Optional | Narrow to an academic year |
| `contentType` | String | Optional | `ALL`, `PROJECTS`, `PUBLICATIONS` |
| `page` | Integer | Optional | Page number, default `0` |
| `size` | Integer | Optional | Items per page, default `12` |

**Success Response — `200 OK`:**

| Field | Type | Description |
|---|---|---|
| `results` | Array | Search result items with relevance scores |
| `results[].id` | UUID | Content ID |
| `results[].contentType` | String | `PROJECT` or `PUBLICATION` |
| `results[].title` | String | Title |
| `results[].abstractPreview` | String | First 200 characters of abstract |
| `results[].relevanceScore` | Integer | Score 0–100 indicating how well this matches the query |
| `results[].matchedFields` | Array | Which fields matched the query, e.g. `["title", "keywords"]` |
| `totalElements` | Long | Total matching results |
| `searchTimeMs` | Long | Query execution time |

---

#### `GET /api/search/suggestions`

**Purpose:** Real-time autocomplete suggestions as the user types in the search bar. Called with a 300ms debounce.  
**Access:** Any authenticated user.

**Query Parameters:**

| Parameter | Type | Description |
|---|---|---|
| `q` | String | Partial query string (minimum 2 characters) |
| `limit` | Integer | Max suggestions to return, default `8` |

**Success Response — `200 OK`:**

| Field | Type | Description |
|---|---|---|
| `suggestions` | Array | Array of suggestion objects |
| `suggestions[].text` | String | Suggested search term |
| `suggestions[].type` | String | `KEYWORD`, `DEPARTMENT`, `AUTHOR`, `TECHNOLOGY` |
| `suggestions[].count` | Integer | How many results this term would return |

---

#### `GET /api/search/similar/:projectId`

**Purpose:** Find projects similar to a given project. Used by AcademIQ to detect potential duplicates and suggest related work.  
**Access:** Any authenticated user.

**Success Response — `200 OK`:** Array of similar projects with similarity scores and the matched keywords that make them similar.

---

## 11. Feature 9 — Reservation System

### 11.1 What This Feature Does

The reservation system controls access to memoir PDFs. A user cannot open the memoir viewer without a valid, active reservation. The system enforces regulated access hours (configurable by admin), maximum concurrent users per project per slot (configurable, default 3), and a cancellation window (configurable, default 30 minutes before slot start). All configuration is read at runtime from `SystemSetting` — no code redeployment is needed when an admin changes a value.

### 11.2 Reservation Creation Flow

```
1. User selects a time slot on the reservation calendar
2. Frontend calls availability check endpoint to confirm slot is not full
3. User clicks "Confirm Reservation"
4. Backend validates:
   a. User is not no-show restricted
   b. Requested slot is within regulated hours (Mon–Fri, 8AM–5PM by default)
   c. Requested slot start is not in the past
   d. Active reservation count for project + slot < MAX_CONCURRENT_RESERVATIONS
   e. User does not already have a reservation on this project + slot
5. Reservation created with status = CONFIRMED
6. Project.reservationCount incremented atomically
7. Confirmation notification sent to user (in-app + email)
8. AuditLog(RESERVATION_CREATED) written
```

### 11.3 Endpoints

---

#### `GET /api/reservations/availability`

**Purpose:** Check how many slots are available for a project at a specific time. Used by the calendar to color-code slots.  
**Access:** Any authenticated user.

**Query Parameters:**

| Parameter | Type | Required | Description |
|---|---|---|---|
| `projectId` | UUID | ✅ | Project to check |
| `slotStart` | DateTime | ✅ | Start of the time slot |
| `slotEnd` | DateTime | ✅ | End of the time slot |

**Success Response — `200 OK`:**

| Field | Type | Description |
|---|---|---|
| `totalSlots` | Integer | Max concurrent users for this project (from `SystemSetting`) |
| `takenSlots` | Integer | Currently confirmed or active reservations for this slot |
| `availableSlots` | Integer | Remaining open slots |
| `isFullyBooked` | Boolean | Whether the slot is completely full |
| `nextAvailableSlot` | DateTime | If fully booked: the next time window with an open slot (null if none found in next 14 days) |

---

#### `GET /api/reservations/calendar/:projectId`

**Purpose:** Get the full slot availability calendar for a project for the next 14 days. Used to render the calendar in the reservation modal.  
**Access:** Any authenticated user.

**Success Response — `200 OK`:**

Array of slot objects for every possible slot in the next 14 days:

| Field | Type | Description |
|---|---|---|
| `slotStart` | DateTime | Slot start time |
| `slotEnd` | DateTime | Slot end time |
| `availableSlots` | Integer | Open slots |
| `takenSlots` | Integer | Booked slots |
| `isFullyBooked` | Boolean | Whether slot is full |
| `isOutsideRegulatedHours` | Boolean | Whether slot falls outside allowed access hours |
| `isClosure` | Boolean | Whether this date is a configured closure date |

---

#### `POST /api/reservations`

**Purpose:** Create a new reservation for a memoir access slot.  
**Access:** Any authenticated user.

**Request Body:**

| Field | Type | Required | Description |
|---|---|---|---|
| `projectId` | UUID | ✅ | Project whose memoir to reserve access for |
| `slotStart` | DateTime | ✅ | Reservation window start |
| `slotEnd` | DateTime | ✅ | Reservation window end |

**Business Rules Enforced:**

| Rule | Error |
|---|---|
| User has >= `NO_SHOW_THRESHOLD` no-shows | `ERR-RES-004` |
| Slot falls outside regulated hours | `ERR-RES-002` |
| Slot start is in the past | `ERR-RES-003` |
| Slot is fully booked | `ERR-RES-001` |
| User already has a reservation for this project + slot | `ERR-RES-005` |
| Reservation system is disabled (admin toggle) | `ERR-RES-009` |

**Success Response — `201 Created`:**

| Field | Type | Description |
|---|---|---|
| `reservationId` | UUID | Reservation ID |
| `status` | Enum | `CONFIRMED` |
| `projectTitle` | String | Name of the project |
| `slotStart` | DateTime | Reserved slot start |
| `slotEnd` | DateTime | Reserved slot end |
| `confirmedAt` | DateTime | When reservation was confirmed |

**Side Effects:**
- Creates `Reservation` record with `status = CONFIRMED`
- Increments `project.reservationCount`
- Sends `Notification(RESERVATION_CONFIRMED)` (in-app + email)
- Writes `AuditLog(RESERVATION_CREATED)`

---

#### `DELETE /api/reservations/:id`

**Purpose:** Cancel a reservation.  
**Access:** Owner of the reservation. Admins can cancel any reservation.

**Request Body:**

| Field | Type | Required | Description |
|---|---|---|---|
| `reason` | String | Optional | Reason for cancellation |

**Business Rules:**

| Rule | Error |
|---|---|
| Reservation does not belong to this user (non-admin) | `ERR-AUTH-403` |
| Cancellation deadline has passed | `ERR-RES-006` |
| Reservation is already cancelled or completed | `ERR-VALIDATION` |

**Success Response — `204 No Content`**

**Side Effects:**
- Sets `reservation.status = CANCELLED`
- Writes `AuditLog(RESERVATION_CANCELLED)`
- Triggers `WaitlistService` to notify the next waitlisted user if any

---

#### `GET /api/reservations/my`

**Purpose:** Get the authenticated user's reservations.  
**Access:** Any authenticated user.

**Query Parameters:**

| Parameter | Type | Description |
|---|---|---|
| `status` | String | `UPCOMING`, `ACTIVE`, `PAST`, `CANCELLED`, `ALL` |
| `page` | Integer | Page number |
| `size` | Integer | Items per page |

**Success Response — `200 OK`:** Paginated list of reservation cards with project title, slot time, status, countdown to start, and action buttons.

---

#### `POST /api/reservations/:id/renewal-request`

**Purpose:** Request to renew access to a project after a reservation has been completed.  
**Access:** Owner of the completed reservation.

**Business Rule:** System checks that no other user currently has a `CONFIRMED` or `ACTIVE` reservation on the same project. If another user does, renewal request is rejected at the API level with a clear message.

**Request Body:**

| Field | Type | Required | Description |
|---|---|---|---|
| `reason` | String | Optional | Why the user needs renewed access |
| `requestedSlotStart` | DateTime | ✅ | Preferred new slot start |
| `requestedSlotEnd` | DateTime | ✅ | Preferred new slot end |

**Success Response — `201 Created`:** Renewal request submitted. Status shown as `RENEWAL_REQUESTED`.

**Side Effects:**
- Sets `reservation.renewalRequested = true`
- The renewal request appears in the moderator/admin approval queue

---

## 12. Feature 10 — Waitlist System

### 12.1 What This Feature Does

When all reservation slots for a project are fully booked, users can join a waitlist. The waitlist is a numbered queue. When a slot opens (because someone cancels), the system automatically finds the first person in the queue with `status = WAITING`, notifies them, and gives them a configurable window of time (default 2 hours) to claim the slot. If they do not claim it within that window, the next person in the queue is notified.

### 12.2 Endpoints

---

#### `POST /api/waitlist`

**Purpose:** Join the waitlist for a fully booked project.  
**Access:** Any authenticated user.

**Request Body:**

| Field | Type | Required | Description |
|---|---|---|---|
| `projectId` | UUID | ✅ | Project to join the waitlist for |

**Business Rules:**

| Rule | Error |
|---|---|
| User is already on the waitlist for this project | `ERR-RES-007` |
| Project is not actually fully booked right now | `ERR-VALIDATION: Slots are available. Please make a direct reservation instead.` |

**Success Response — `201 Created`:**

| Field | Type | Description |
|---|---|---|
| `waitlistId` | UUID | Waitlist entry ID |
| `position` | Integer | Queue position, e.g. `3` |
| `status` | Enum | `WAITING` |
| `projectTitle` | String | Project name |
| `message` | String | `"You are number 3 in the queue. You will be notified when a slot becomes available."` |

---

#### `DELETE /api/waitlist/:id`

**Purpose:** Leave the waitlist for a project.  
**Access:** Owner of the waitlist entry.

**Side Effects:**
- Sets `waitlistEntry.status = CANCELLED`
- Recalculates queue positions for all remaining entries (decrements positions behind the removed entry by 1 in a single transaction)

**Success Response — `204 No Content`**

---

#### `POST /api/waitlist/:id/claim`

**Purpose:** Claim a slot that was offered to a waitlisted user after they received a notification.  
**Access:** The notified user only.

**Request Body:**

| Field | Type | Required | Description |
|---|---|---|---|
| `slotStart` | DateTime | ✅ | The offered slot start time |
| `slotEnd` | DateTime | ✅ | The offered slot end time |

**Business Rules:**

| Rule | Error |
|---|---|
| Waitlist entry status is not `NOTIFIED` | `ERR-VALIDATION: This slot offer is no longer active.` |
| Claim window has expired (`now() > expiresAt`) | `ERR-RES-008: This slot offer has expired.` |

**Success Response — `201 Created`:** Full `ReservationResponse` — the waitlist entry has been converted into a real reservation.

**Side Effects:**
- Creates `Reservation` with `status = CONFIRMED`
- Sets `waitlistEntry.status = CONVERTED_TO_RESERVATION`
- Recalculates queue positions for remaining waiting entries

---

#### `GET /api/waitlist/my`

**Purpose:** Get the authenticated user's current waitlist entries.  
**Access:** Any authenticated user.

**Success Response — `200 OK`:** List of waitlist entries with project title, queue position, status, and expiry time if notified.

---

## 13. Feature 11 — Memoir Viewer (Security-Critical)

### 13.1 What This Feature Does

The memoir viewer is the most security-sensitive feature in the entire system. Its job is to allow reading while preventing downloading. The architecture achieves this by:

- Never sending the PDF binary to the client under any circumstances
- Rendering each page server-side into an image (PNG/WebP) using a PDF rendering library
- Applying a dynamic watermark server-side on every page before it is served
- Returning not the image bytes but a signed URL that expires in 30 seconds
- Validating a signed session token on every single page request
- Blocking all client-side save mechanisms through HTTP headers and JavaScript event blocking

### 13.2 Session Open Flow

```
1. User clicks "Open Memoir Viewer" (only enabled within reservation window)
2. Frontend sends reservationId to the open-session endpoint
3. Backend validates:
   a. Reservation exists and belongs to this user
   b. now() is between slotStart and slotEnd
   c. Reservation status is CONFIRMED
4. Backend generates a Viewer Session Token (JWT scoped to userId + projectId + slotEnd)
5. Backend creates ViewerSession record with sessionActive = true
6. Backend updates Reservation.status = ACTIVE
7. Backend returns: sessionToken, totalPages, sessionExpiresAt
8. Frontend opens the viewer page with the session token
```

### 13.3 Page Request Flow (Happens on Every Page Navigation)

```
1. Frontend requests a page: GET /api/viewer/page?sessionToken=...&pageNumber=5
2. Backend validates the session token (signature, expiry, user+project match)
3. Backend loads the ViewerSession and checks sessionActive = true
4. Backend reads the PDF from private storage (path from MemoirFile.storagePath)
5. Backend renders page 5 to a PNG image server-side (no client involvement)
6. Backend applies watermark on the image:
   - Viewer's full name + campus ID
   - Current date and time
   - "AUCA — University Use Only"
   - Diagonal, repeated 3 times across the page, semi-transparent
7. Backend stores the watermarked image in temporary storage
8. Backend generates a signed URL valid for exactly 30 seconds
9. Backend creates PageViewLog record (immutable)
10. Backend updates ViewerSession: lastPageViewed, totalPagesViewed
11. Backend returns: signedImageUrl (expires in 30s), pageNumber, totalPages
12. Frontend displays the image — not the PDF
```

### 13.4 Endpoints

---

#### `POST /api/viewer/open`

**Purpose:** Open a viewer session for an active reservation.  
**Access:** Owner of the reservation, during the reservation window.

**Request Body:**

| Field | Type | Required | Description |
|---|---|---|---|
| `reservationId` | UUID | ✅ | The active reservation to open |

**Validation Checks:**

| Check | Error |
|---|---|
| Reservation not found | `ERR-ACC-403` |
| Reservation does not belong to this user | `ERR-ACC-403` |
| `now()` is before `slotStart` | `ERR-ACC-403: Your session has not started yet.` |
| `now()` is after `slotEnd` | `ERR-ACC-408: Your reservation has expired.` |
| `reservation.status != CONFIRMED` | `ERR-ACC-409` |

**Success Response — `201 Created`:**

| Field | Type | Description |
|---|---|---|
| `sessionToken` | String | The JWT viewer session token — sent with every page request |
| `totalPages` | Integer | Total pages in the memoir |
| `sessionExpiresAt` | DateTime | Exactly when the session will end (matches `slotEnd`) |
| `lastPageViewed` | Integer | If resuming a session: the last page number visited |

**Side Effects:**
- Creates `ViewerSession` record
- Sets `reservation.status = ACTIVE`
- Writes `AuditLog(VIEWER_SESSION_OPENED)`

---

#### `GET /api/viewer/page`

**Purpose:** Serve a single rendered, watermarked page of the memoir as a signed temporary image URL.  
**Access:** Active session token holder only.

**Query Parameters:**

| Parameter | Type | Required | Description |
|---|---|---|---|
| `sessionToken` | String | ✅ | The viewer session JWT |
| `pageNumber` | Integer | ✅ | Page number (1-indexed) |
| `timeSpentOnPreviousPage` | Long | Optional | Seconds spent on the previous page — used to update the previous page's `PageViewLog` |

**Security Checks (all must pass):**

| Check | Error |
|---|---|
| Token signature is invalid | `ERR-ACC-409` |
| Token is expired | `ERR-ACC-408` |
| Token's embedded userId does not match authenticated user | `ERR-ACC-409` |
| ViewerSession.sessionActive = false | `ERR-ACC-408` |
| pageNumber < 1 or > totalPages | `ERR-VALIDATION` |

**Success Response — `200 OK`:**

| Field | Type | Description |
|---|---|---|
| `pageImageUrl` | String | Signed temporary URL to the watermarked page image — expires in 30 seconds |
| `pageImageUrlExpiresInSeconds` | Integer | Always `30` |
| `pageNumber` | Integer | The page number served |
| `totalPages` | Integer | Total pages in the document |

**HTTP Response Headers on the Image Endpoint:**
```
Cache-Control: no-store, no-cache, must-revalidate
Pragma: no-cache
X-Content-Type-Options: nosniff
```

**Side Effects:**
- Creates `PageViewLog` record (immutable — cannot be updated or deleted)
- Updates `ViewerSession.lastPageViewed` and `ViewerSession.totalPagesViewed`

---

#### `POST /api/viewer/heartbeat`

**Purpose:** Update the total time spent in the session. Called by the frontend every 60 seconds while the viewer is open.  
**Access:** Active session token holder.

**Request Body:**

| Field | Type | Required | Description |
|---|---|---|---|
| `sessionToken` | String | ✅ | Viewer session token |
| `totalTimeSpentSeconds` | Long | ✅ | Total seconds since session opened (frontend tracks this) |

**Success Response — `200 OK`:** Remaining time in seconds.

**Side Effects:**
- Updates `ViewerSession.totalTimeSpentSeconds`

---

#### `POST /api/viewer/close`

**Purpose:** Explicitly close a viewer session when the user navigates away or clicks close.  
**Access:** Session token holder. Also called by the scheduled job when time expires.

**Request Body:**

| Field | Type | Required | Description |
|---|---|---|---|
| `sessionToken` | String | ✅ | Viewer session token |
| `totalTimeSpentSeconds` | Long | Optional | Final time on last page |

**Success Response — `204 No Content`**

**Side Effects:**
- Sets `ViewerSession.sessionActive = false`, `endedAt = now()`
- Sets `Reservation.status = COMPLETED`
- Writes `AuditLog(MEMOIR_VIEWED)` with total pages viewed and total time spent

---

## 14. Feature 12 — Moderation & Approval Workflow

### 14.1 What This Feature Does

Every project and publication submitted to the platform must pass through moderation before it becomes visible to other users. Moderators review submissions for policy compliance, document quality, and duplicate detection. They can approve, reject, request re-upload, mark as duplicate, hide, or archive content. Every single moderation decision is permanently recorded in the `moderation_actions` table using `AppendOnlyBaseEntity` — the record cannot be changed after it is written.

### 14.2 Moderation Actions and Their Effects

| Action | Effect on Status | Notification to Author |
|---|---|---|
| **Approve** | → `PUBLISHED` | `PROJECT_APPROVED` with congratulations |
| **Reject** | → `REJECTED` | `PROJECT_REJECTED` with mandatory reason |
| **Request Re-upload** | → `DRAFT` | `PROJECT_REUPLOAD_REQUESTED` with specific instructions |
| **Mark Duplicate** | → `REJECTED` | `PROJECT_REJECTED` with reference to original |
| **Hide** | `PUBLISHED` → `HIDDEN` | System message about temporary unavailability |
| **Archive** | Any → `ARCHIVED` | System message about archiving |
| **Restore** | `ARCHIVED`/`HIDDEN` → `PUBLISHED` | System message about restoration |

### 14.3 Endpoints

---

#### `GET /api/moderation/queue`

**Purpose:** Get the moderation queue — all items pending review.  
**Access:** `MODERATOR`, `ADMIN`.

**Query Parameters:**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `status` | String | `PENDING_APPROVAL` | Filter by item status |
| `contentType` | String | `ALL` | `ALL`, `PROJECTS`, `PUBLICATIONS` |
| `department` | String | All | Filter by department |
| `sort` | String | `createdAt,asc` | Oldest first by default — ensures no item waits indefinitely |
| `page` | Integer | `0` | Page number |
| `size` | Integer | `20` | Items per page |

**Success Response — `200 OK`:** Paginated list of moderation queue items. Each item includes `daysWaiting` (integer) so the frontend can color-code aging items.

---

#### `POST /api/moderation/projects/:id/approve`

**Purpose:** Approve a project and publish it to the repository.  
**Access:** `MODERATOR`, `ADMIN`.

**Request Body:**

| Field | Type | Required | Description |
|---|---|---|---|
| `comment` | String | Optional | Moderator's approval comment (not shown to author — internal note) |

**Business Rule:** Project must be in `PENDING_APPROVAL` status.

**Success Response — `200 OK`:** Updated project with `status = PUBLISHED`.

**Side Effects:**
- Sets `project.status = PUBLISHED`, `project.publishedAt = now()`
- Creates immutable `ModerationAction(APPROVE)` record
- Sends `Notification(PROJECT_APPROVED)` to student
- Writes `AuditLog(PROJECT_PUBLISHED)`

---

#### `POST /api/moderation/projects/:id/reject`

**Purpose:** Reject a project submission. Reason is mandatory.  
**Access:** `MODERATOR`, `ADMIN`.

**Request Body:**

| Field | Type | Required | Description |
|---|---|---|---|
| `reason` | String | ✅ | Rejection reason sent directly to the student. Cannot be blank. |
| `templateName` | String | Optional | Name of the pre-configured rejection template used (e.g. `INCOMPLETE_METADATA`) |

**Success Response — `200 OK`:** Updated project with `status = REJECTED`.

**Side Effects:**
- Creates immutable `ModerationAction(REJECT)` with reason and templateName
- Sends `Notification(PROJECT_REJECTED)` with the reason text
- Writes `AuditLog(PROJECT_REJECTED)`

---

#### `POST /api/moderation/projects/:id/request-reupload`

**Purpose:** Return a project to DRAFT and instruct the student to fix specific issues and resubmit.  
**Access:** `MODERATOR`, `ADMIN`.

**Request Body:**

| Field | Type | Required | Description |
|---|---|---|---|
| `instructions` | String | ✅ | Specific instructions for what the student needs to fix |

**Success Response — `200 OK`:** Updated project with `status = DRAFT`.

**Side Effects:**
- Creates immutable `ModerationAction(REQUEST_REUPLOAD)`
- Sends `Notification(PROJECT_REUPLOAD_REQUESTED)` with instructions
- Student can now edit the draft and resubmit

---

#### `POST /api/moderation/projects/:id/mark-duplicate`

**Purpose:** Flag a project as a duplicate of an existing project.  
**Access:** `MODERATOR`, `ADMIN`.

**Request Body:**

| Field | Type | Required | Description |
|---|---|---|---|
| `reason` | String | ✅ | Explanation of the duplication |
| `duplicateOfId` | UUID | ✅ | ID of the original project this duplicates |

**Success Response — `200 OK`:** Project marked as rejected with duplicate reference.

---

#### `POST /api/moderation/projects/:id/hide`

**Purpose:** Temporarily hide a published project from browse results.  
**Access:** `MODERATOR`, `ADMIN`.

**Request Body:**

| Field | Type | Required | Description |
|---|---|---|---|
| `reason` | String | ✅ | Why this project is being hidden |

---

#### `POST /api/moderation/projects/:id/archive`

**Purpose:** Move a project to long-term archived status.  
**Access:** `MODERATOR`, `ADMIN`.

**Request Body:**

| Field | Type | Required | Description |
|---|---|---|---|
| `reason` | String | ✅ | Archiving reason |

---

#### `POST /api/moderation/projects/:id/restore`

**Purpose:** Restore an archived or hidden project back to published status.  
**Access:** `MODERATOR`, `ADMIN`.

**No request body required.**

---

#### `GET /api/moderation/projects/:id/history`

**Purpose:** Get the full moderation action history for a specific project.  
**Access:** `MODERATOR`, `ADMIN`. Also the project's owner student.

**Success Response — `200 OK`:** Chronological list of all `ModerationAction` records for this project, each with: `actionType`, `reason`, `performedBy` (name), `createdAt`.

---

#### `POST /api/moderation/reservations/:id/approve-renewal`

**Purpose:** Approve a user's reservation renewal request.  
**Access:** `MODERATOR`, `ADMIN`.

**Side Effects:**
- Creates new `Reservation` for the requesting user
- Sets `reservation.renewalApproved = true`
- Creates `ModerationAction(RENEWAL_APPROVED)`
- Sends `Notification(RENEWAL_APPROVED)`

---

#### `POST /api/moderation/reservations/:id/reject-renewal`

**Purpose:** Reject a reservation renewal request.  
**Access:** `MODERATOR`, `ADMIN`.

**Request Body:**

| Field | Type | Required | Description |
|---|---|---|---|
| `reason` | String | ✅ | Rejection reason sent to the user |

---

#### All publication moderation endpoints mirror project endpoints:

| Endpoint | Purpose |
|---|---|
| `POST /api/moderation/publications/:id/approve` | Approve publication |
| `POST /api/moderation/publications/:id/reject` | Reject publication |
| `POST /api/moderation/publications/:id/request-reupload` | Request document re-upload |
| `POST /api/moderation/publications/:id/hide` | Hide publication |
| `POST /api/moderation/publications/:id/archive` | Archive publication |
| `POST /api/moderation/publications/:id/restore` | Restore publication |
| `GET /api/moderation/publications/:id/history` | Get moderation history |

---

## 15. Feature 13 — Notification System

### 15.1 What This Feature Does

Every significant event in the system generates a notification for the relevant user. Notifications appear in the in-app notification panel (the bell icon) and are also sent by email asynchronously. The email is sent in a background thread — if it fails, `emailSent` remains `false` and a retry scheduler picks it up and retries later. This design ensures a transient email service failure never blocks the main operation.

### 15.2 Endpoints

---

#### `GET /api/notifications`

**Purpose:** Get all notifications for the authenticated user.  
**Access:** Any authenticated user.

**Query Parameters:**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `unreadOnly` | Boolean | `false` | If `true`, returns only unread notifications |
| `page` | Integer | `0` | Page number |
| `size` | Integer | `20` | Items per page |

**Success Response — `200 OK`:**

Each notification includes: `id`, `notificationType`, `title`, `body`, `isRead`, `readAt`, `createdAt`, `relatedEntityId`, `relatedEntityType`.

---

#### `GET /api/notifications/unread-count`

**Purpose:** Get the count of unread notifications for the bell badge.  
**Access:** Any authenticated user.

**Success Response — `200 OK`:**

| Field | Type | Description |
|---|---|---|
| `count` | Long | Number of unread notifications |

This endpoint is called every 30 seconds by the frontend to keep the badge updated without a full page reload.

---

#### `PATCH /api/notifications/:id/read`

**Purpose:** Mark a single notification as read.  
**Access:** Owner of the notification.

**Success Response — `200 OK`:** Updated notification with `isRead = true`, `readAt` set.

---

#### `PATCH /api/notifications/read-all`

**Purpose:** Mark all of the authenticated user's notifications as read.  
**Access:** Any authenticated user.

**Success Response — `204 No Content`**

---

## 16. Feature 14 — Reports & Analytics

### 16.1 What This Feature Does

The reports module gives admins and moderators data-driven visibility into platform activity. All reports are filtered by date range and department. All reports can be exported. The data comes directly from the database — no external analytics service is required.

### 16.2 Endpoints

---

#### `GET /api/admin/reports/projects`

**Purpose:** Project activity report.  
**Access:** `MODERATOR` (limited view), `ADMIN` (full view).

**Query Parameters:** `from` (Date), `to` (Date), `department` (String, optional).

**Success Response — `200 OK`:**

| Field | Description |
|---|---|
| `totalSubmissions` | Total projects submitted in the period |
| `published` | Count with status PUBLISHED |
| `rejected` | Count with status REJECTED |
| `pending` | Count with status PENDING_APPROVAL |
| `draft` | Count with status DRAFT |
| `submissionsPerMonth` | Array: `[{ "month": "January 2025", "count": 12 }]` |
| `byDepartment` | Array: `[{ "department": "Software Engineering", "count": 22 }]` |
| `byCategory` | Array: `[{ "category": "SOFTWARE_SYSTEM", "count": 15 }]` |
| `mostViewedProjects` | Top 10 projects by viewCount |
| `averageDaysToApproval` | Mean days from PENDING_APPROVAL to PUBLISHED |

---

#### `GET /api/admin/reports/viewing-sessions`

**Purpose:** Memoir viewing session analytics.  
**Access:** `ADMIN` only.

**Success Response — `200 OK`:**

| Field | Description |
|---|---|
| `totalSessions` | Total viewer sessions opened |
| `averageSessionDurationMinutes` | Mean reading time |
| `averagePagesViewed` | Mean pages per session |
| `mostReadProjects` | Top 10 by total reading time |
| `sessionsByDepartment` | Which departments' projects get read most |
| `sessionsPerDay` | Array for line chart |

---

#### `GET /api/admin/reports/reservations`

**Purpose:** Reservation analytics.  
**Access:** `ADMIN` only.

**Success Response — `200 OK`:**

| Field | Description |
|---|---|
| `totalReservations` | All reservations in period |
| `completed` | Completed sessions |
| `noShows` | No-show count |
| `noShowRate` | Percentage as string |
| `cancellations` | Cancellation count |
| `cancellationRate` | Percentage as string |
| `peakHour` | Most booked time of day |
| `peakDayOfWeek` | Most booked day of week |
| `waitlistConversionRate` | Percentage of waitlist entries that became reservations |
| `averageSlotUtilization` | How full slots are on average |

---

#### `GET /api/admin/reports/users`

**Purpose:** User access and engagement report.  
**Access:** `ADMIN` only.

**Success Response — `200 OK`:**

| Field | Description |
|---|---|
| `totalUsers` | All registered users |
| `activeThisMonth` | Users who logged in this month |
| `byRole` | `[{ "role": "STUDENT", "count": 180 }]` |
| `newRegistrationsPerMonth` | Line chart data |
| `restrictedUsers` | Users currently no-show restricted |

---

#### `GET /api/admin/reports/moderation`

**Purpose:** Moderation activity report.  
**Access:** `ADMIN` only.

**Success Response — `200 OK`:**

| Field | Description |
|---|---|
| `totalActionsInPeriod` | All moderation actions |
| `approvalRate` | Percentage approved |
| `rejectionRateByTemplate` | Which rejection templates are used most |
| `averageReviewTimeHours` | Mean hours from submission to decision |
| `actionsByModerator` | Who reviewed how many items |

---

#### `GET /api/admin/reports/export`

**Purpose:** Export any report as PDF, Excel, or CSV.  
**Access:** `ADMIN` only.

**Query Parameters:**

| Parameter | Type | Required | Description |
|---|---|---|---|
| `reportType` | String | ✅ | `PROJECTS`, `VIEWING_SESSIONS`, `RESERVATIONS`, `USERS`, `MODERATION` |
| `format` | String | ✅ | `PDF`, `EXCEL`, `CSV` |
| `from` | Date | ✅ | Start date |
| `to` | Date | ✅ | End date |
| `department` | String | Optional | Filter |

**Success Response — `200 OK`:** File download stream with correct `Content-Type` and `Content-Disposition: attachment; filename=...` headers.

---

## 17. Feature 15 — Audit Log

### 17.1 What This Feature Does

The audit log is an immutable, append-only record of every significant event in the system. Records in this table can never be updated or deleted — `AppendOnlyBaseEntity` throws `UnsupportedOperationException` at the JPA level if any update is attempted. Admins can search and filter the log but cannot modify it.

### 17.2 Endpoints

---

#### `GET /api/admin/audit-logs`

**Purpose:** View the system-wide audit log.  
**Access:** `ADMIN` only.

**Query Parameters:**

| Parameter | Type | Description |
|---|---|---|
| `action` | Enum | Filter by specific action type |
| `entityType` | Enum | Filter by entity type |
| `userId` | UUID | Filter by the user who performed the action |
| `from` | DateTime | Start of time range |
| `to` | DateTime | End of time range |
| `page` | Integer | Page number |
| `size` | Integer | Items per page |

**Success Response — `200 OK`:** Paginated audit log entries. Each entry includes `timestamp`, `action`, `entityType`, `entityId` (clickable in frontend), `user` (name + email), `ipAddress`, `detailsJson` (expandable).

---

#### `GET /api/admin/audit-logs/:userId`

**Purpose:** View the audit log filtered to a specific user's actions.  
**Access:** `ADMIN` only.

---

## 18. Feature 16 — Admin — User Management

### 18.1 Endpoints

---

#### `GET /api/admin/users`

**Purpose:** List all registered users.  
**Access:** `ADMIN` only.

**Query Parameters:** `role`, `status`, `department`, `search` (by name or email), `page`, `size`.

**Success Response — `200 OK`:** Paginated user list with full profile summary per user.

---

#### `GET /api/admin/users/:id`

**Purpose:** Get a specific user's full profile and activity summary.  
**Access:** `ADMIN` only.

**Success Response — `200 OK`:** Full user profile + campus profile + recent audit events + submission counts + reservation counts.

---

#### `PATCH /api/admin/users/:id/role`

**Purpose:** Change a user's role.  
**Access:** `ADMIN` only.

**Request Body:**

| Field | Type | Required | Description |
|---|---|---|---|
| `newRole` | Enum | ✅ | `STUDENT`, `LECTURER`, `MODERATOR`, `ADMIN` |
| `reason` | String | Optional | Internal reason for the role change |

**Side Effects:**
- Updates `user.role`
- Sends notification to the user informing them of their role change
- Writes `AuditLog(USER_ROLE_CHANGED)` with `detailsJson = {"from": "STUDENT", "to": "MODERATOR"}`

---

#### `PATCH /api/admin/users/:id/suspend`

**Purpose:** Suspend a user account.  
**Access:** `ADMIN` only.

**Request Body:**

| Field | Type | Required | Description |
|---|---|---|---|
| `reason` | String | ✅ | Suspension reason. Sent to the user via notification. |

**Side Effects:**
- Sets `user.status = SUSPENDED`
- Sends notification to the user
- Writes `AuditLog(USER_SUSPENDED)`

---

#### `PATCH /api/admin/users/:id/activate`

**Purpose:** Activate a suspended user account.  
**Access:** `ADMIN` only.

**No request body.**

**Side Effects:**
- Sets `user.status = ACTIVE`
- Writes `AuditLog(USER_ACTIVATED)`

---

#### `POST /api/admin/users/:id/reset-password`

**Purpose:** Admin-initiated password reset — sends a reset link to the user's email.  
**Access:** `ADMIN` only.

**Side Effects:**
- Generates reset token and sends password reset email to the user
- Writes `AuditLog(ADMIN_PASSWORD_RESET)`

---

#### `GET /api/admin/users/export`

**Purpose:** Export the full user list to CSV or Excel.  
**Access:** `ADMIN` only.

**Query Parameters:** `format` (`CSV` or `EXCEL`), plus same filters as `/api/admin/users`.

---

## 19. Feature 17 — Admin — Reservation Settings

### 19.1 What This Feature Does

All reservation behavior is controlled by `SystemSetting` key-value pairs that admins can change through the UI without code redeployment. Values are cached in memory and the cache is invalidated whenever a setting is saved. Every setting change writes an `AuditLog` record showing the old and new values.

### 19.2 Endpoints

---

#### `GET /api/admin/settings`

**Purpose:** Get all system settings grouped by category.  
**Access:** `ADMIN` only.

**Success Response — `200 OK`:** Settings grouped by `category` (RESERVATION, ACCESS_SCHEDULE, NOTIFICATIONS, BRANDING, FILE_UPLOAD, SECURITY). Each setting includes `key`, `value`, `description`, `lastModifiedBy`, `updatedAt`.

---

#### `PATCH /api/admin/settings`

**Purpose:** Update one system setting value.  
**Access:** `ADMIN` only.

**Request Body:**

| Field | Type | Required | Description |
|---|---|---|---|
| `key` | String | ✅ | The setting key, e.g. `MAX_CONCURRENT_RESERVATIONS` |
| `value` | String | ✅ | The new value as a string |

**Business Rules:**
- Value is validated against the expected type for that key (number keys must be parseable as integers, time keys must be parseable as `HH:mm`, boolean keys must be `true` or `false`)
- The backend knows the expected type for every key through a `SettingKeys` constant class

**Side Effects:**
- Updates `SystemSetting` record
- Clears the in-memory cache for that key
- Writes `AuditLog(SETTINGS_CHANGED)` with `detailsJson = {"key": "...", "oldValue": "...", "newValue": "..."}`

---

## 20. Feature 18 — Admin — Access Schedule

### 20.1 Endpoints

---

#### `GET /api/admin/schedule`

**Purpose:** Get the full weekly access schedule and all configured closure dates.  
**Access:** `ADMIN` only.

**Success Response — `200 OK`:**

| Field | Type | Description |
|---|---|---|
| `weeklySchedule` | Array | 7 entries (one per day of week), each with `dayOfWeek`, `isEnabled`, `startTime`, `endTime` |
| `closureDates` | Array | All configured closure dates with `date` and `label` |

---

#### `PATCH /api/admin/schedule/day`

**Purpose:** Update the access hours for a specific day of the week.  
**Access:** `ADMIN` only.

**Request Body:**

| Field | Type | Required | Description |
|---|---|---|---|
| `dayOfWeek` | String | ✅ | `MONDAY`, `TUESDAY`, etc. |
| `isEnabled` | Boolean | ✅ | Whether reservations are allowed on this day |
| `startTime` | Time | Optional | Access start time, e.g. `08:00` |
| `endTime` | Time | Optional | Access end time, e.g. `17:00` |

---

#### `POST /api/admin/schedule/closures`

**Purpose:** Add a closure date (no reservations on this date).  
**Access:** `ADMIN` only.

**Request Body:**

| Field | Type | Required | Description |
|---|---|---|---|
| `date` | Date | ✅ | The closure date |
| `label` | String | Optional | Label shown on calendar, e.g. `Independence Day` |

---

#### `DELETE /api/admin/schedule/closures/:date`

**Purpose:** Remove a configured closure date.  
**Access:** `ADMIN` only.

---

## 21. Feature 19 — Admin — Platform Settings & Backup

### 21.1 Endpoints

---

#### `PATCH /api/admin/branding`

**Purpose:** Update platform branding settings (platform name, institution name, logo).  
**Access:** `ADMIN` only.

**Request Body:** `multipart/form-data` with optional fields: `platformName`, `institutionName`, `logoFile` (image upload).

---

#### `GET /api/admin/email-templates`

**Purpose:** Get all configurable email notification templates.  
**Access:** `ADMIN` only.

---

#### `PATCH /api/admin/email-templates/:templateName`

**Purpose:** Update an email template body and subject.  
**Access:** `ADMIN` only.

**Request Body:**

| Field | Type | Required | Description |
|---|---|---|---|
| `subject` | String | ✅ | Email subject line with placeholder support: `{projectTitle}`, `{userName}`, `{reason}` |
| `bodyHtml` | String | ✅ | HTML email body with the same placeholders |

---

#### `POST /api/admin/backup`

**Purpose:** Trigger a manual database backup.  
**Access:** `ADMIN` only.

**No request body.**

**Success Response — `202 Accepted`:** Backup has been triggered and is running in the background. Returns a `backupId` to track progress.

**Side Effects:**
- Writes `AuditLog(BACKUP_TRIGGERED)`

---

#### `GET /api/admin/backup/history`

**Purpose:** Get the history of all past backups.  
**Access:** `ADMIN` only.

**Success Response — `200 OK`:** List of backup records with `backupId`, `triggeredAt`, `triggeredBy`, `status` (`IN_PROGRESS`, `SUCCESS`, `FAILED`), `fileSizeBytes`, `downloadUrl`.

---

#### `GET /api/admin/moderation/rejection-templates`

**Purpose:** Get all rejection reason templates available to moderators.  
**Access:** `MODERATOR`, `ADMIN`.

---

#### `POST /api/admin/moderation/rejection-templates`

**Purpose:** Create a new rejection reason template.  
**Access:** `ADMIN` only.

**Request Body:**

| Field | Type | Required | Description |
|---|---|---|---|
| `templateName` | String | ✅ | Internal identifier, e.g. `INCOMPLETE_METADATA` |
| `templateText` | String | ✅ | The text sent to the student when this template is selected |

---

## 22. Feature 20 — Scheduled Background Jobs

### 22.1 What These Jobs Do

Background jobs run on a schedule without any user triggering them. They handle time-sensitive tasks that must happen automatically: marking no-shows, sending reminders, closing expired sessions, and syncing GitHub data. All jobs are logged with `AuditLog` entries so admins can see when each job ran and what it did.

### 22.2 Job Reference Table

| Job | Schedule | What It Does | Why It Matters |
|---|---|---|---|
| **No-Show Marker** | Every 5 minutes | Finds `CONFIRMED` reservations whose `slotEnd` has passed with no associated `ViewerSession`. Sets `status = NO_SHOW`. | Without this, missed reservations stay as `CONFIRMED` forever and slot availability calculations become incorrect. |
| **Reservation Reminder** | Every 1 minute | Finds `CONFIRMED` reservations starting in the next 15–16 minutes that have not yet received a reminder. Sends `Notification(RESERVATION_REMINDER)`. | Ensures the 15-minute reminder is delivered reliably even if the system is under load. |
| **Session Expiry Closer** | Every 1 minute | Finds `ViewerSession` records with `sessionActive = true` where `tokenExpiresAt < now()`. Closes them and sets `Reservation.status = COMPLETED`. | Ensures sessions are closed exactly on time even if the user does not explicitly close the viewer. |
| **Waitlist Expiry Processor** | Every 5 minutes | Finds `Waitlist` entries with `status = NOTIFIED` where `expiresAt < now()`. Sets `status = EXPIRED`. Notifies the next person in queue. Recalculates positions. | Ensures the slot offer pipeline keeps moving — a notified user who ignores the notification does not block the queue. |
| **Email Retry** | Every 15 minutes | Finds `Notification` records with `emailSent = false` that are older than 5 minutes. Retries sending the email. | Ensures email delivery eventually succeeds despite transient email service failures. |
| **GitHub Metadata Sync** | Daily at 2:00 AM | Refreshes star counts, fork counts, `isPrivate` status, and README content for all `GithubRepo` records where `lastSyncedAt` is older than 24 hours. | Keeps displayed GitHub statistics reasonably current without hitting the API on every page view. |
| **Embargo Lifter** | Daily at midnight | Finds `Project` and `Publication` records with `visibility = EMBARGOED` where `embargoUntil < today`. Updates `visibility = AUCA_ONLY`. | Automatically lifts embargoes without any admin intervention. |
| **Backup Job** | Daily at 3:00 AM | Triggers a database backup. Stores the backup file. Writes `AuditLog(BACKUP_TRIGGERED)`. | Ensures daily backups run even if no admin triggers a manual backup. |

---

## 23. Global Error Codes & HTTP Status Reference

Every error response from the backend follows this format:

```
{
  "errorCode": "ERR-RES-001",
  "message": "This time slot is fully booked.",
  "timestamp": "2025-03-18T10:14:22"
}
```

### Complete Error Code Table

| Error Code | HTTP Status | Scenario |
|---|---|---|
| `ERR-VER-404` | 404 Not Found | Campus ID not found in AUCA database |
| `ERR-VER-403` | 403 Forbidden | Campus ID found but not currently enrolled |
| `ERR-VER-503` | 503 Unavailable | Campus database API is temporarily unreachable |
| `ERR-AUTH-401` | 401 Unauthorized | Wrong password |
| `ERR-AUTH-202` | 202 Accepted | Account awaiting campus verification |
| `ERR-AUTH-403` | 403 Forbidden | Account is suspended |
| `ERR-AUTH-400` | 400 Bad Request | Reset token invalid or expired |
| `ERR-AUTH-409` | 409 Conflict | Email already registered / token already used |
| `ERR-AUTH-429` | 429 Too Many Requests | Account locked after too many failed login attempts |
| `ERR-RES-001` | 409 Conflict | Reservation time slot is fully booked |
| `ERR-RES-002` | 400 Bad Request | Requested slot is outside regulated access hours |
| `ERR-RES-003` | 400 Bad Request | Requested slot is in the past |
| `ERR-RES-004` | 403 Forbidden | User is no-show restricted |
| `ERR-RES-005` | 409 Conflict | User already has a reservation for this project + slot |
| `ERR-RES-006` | 400 Bad Request | Cancellation deadline has passed |
| `ERR-RES-007` | 409 Conflict | User is already on the waitlist for this project |
| `ERR-RES-008` | 410 Gone | Waitlist slot offer has expired |
| `ERR-RES-009` | 503 Unavailable | Reservation system is disabled by admin |
| `ERR-ACC-403` | 403 Forbidden | No valid reservation for this viewer access attempt |
| `ERR-ACC-408` | 408 Request Timeout | Viewer session has expired |
| `ERR-ACC-409` | 409 Conflict | Viewer session token is invalid or already closed |
| `ERR-UPL-001` | 400 Bad Request | Uploaded file is not a PDF |
| `ERR-UPL-002` | 413 Payload Too Large | File exceeds 50MB size limit |
| `ERR-UPL-003` | 422 Unprocessable | File flagged by malware scanner |
| `ERR-UPL-004` | 409 Conflict | This exact file (matching SHA-256) is already uploaded |
| `ERR-GIT-001` | 404 Not Found | GitHub repository not accessible |
| `ERR-GIT-002` | 404 Not Found | Final commit hash not found in repository |
| `ERR-GIT-003` | 400 Bad Request | Repository has no commits |
| `ERR-GIT-004` | 429 Too Many Requests | GitHub API rate limit reached |
| `ERR-VALIDATION` | 400 Bad Request | Request body failed field validation |
| `ERR-NOT-FOUND` | 404 Not Found | Requested resource does not exist |
| `ERR-CONFIG` | 500 Internal Error | Required system setting is missing from the database |
| `ERR-SERVER-500` | 500 Internal Error | Unexpected server error (safe generic message to client) |

---

## 24. Complete Endpoint Index

### Authentication (`/api/auth`)

| Method | Path | Access | Purpose |
|---|---|---|---|
| POST | `/api/auth/login` | Public | Login and receive JWT |
| POST | `/api/auth/register` | Public | Register with campus verification |
| POST | `/api/auth/forgot-password` | Public | Request password reset link |
| POST | `/api/auth/reset-password` | Public | Submit new password with reset token |
| POST | `/api/auth/logout` | Authenticated | Logout and invalidate token |

### User Profile (`/api/users`)

| Method | Path | Access | Purpose |
|---|---|---|---|
| GET | `/api/users/me` | Authenticated | Get own profile |
| PATCH | `/api/users/me` | Authenticated | Update own profile |
| PATCH | `/api/users/me/password` | Authenticated | Change own password |

### Projects (`/api/projects`)

| Method | Path | Access | Purpose |
|---|---|---|---|
| GET | `/api/projects` | Authenticated | Browse published projects |
| GET | `/api/projects/:id` | Authenticated | Get single project |
| GET | `/api/projects/my` | Student | Get own submissions |
| POST | `/api/projects/draft` | Student | Create new draft |
| PATCH | `/api/projects/:id/draft` | Student (owner) | Update own draft |
| POST | `/api/projects/:id/submit` | Student (owner) | Submit draft for review |
| DELETE | `/api/projects/:id` | Student (owner) | Delete own draft |
| POST | `/api/projects/:id/memoir` | Student (owner) | Upload memoir PDF |
| GET | `/api/projects/:id/memoir/scan-status` | Student (owner), Mod, Admin | Poll scan status |
| POST | `/api/projects/:id/github` | Student (owner) | Link GitHub repository |
| GET | `/api/projects/:id/github/commits` | Student (owner) | Get recent commits for selection |
| POST | `/api/projects/:id/github/sync` | Mod, Admin | Manual GitHub metadata sync |

### Publications (`/api/publications`)

| Method | Path | Access | Purpose |
|---|---|---|---|
| GET | `/api/publications` | Authenticated | Browse published publications |
| GET | `/api/publications/:id` | Authenticated | Get single publication |
| GET | `/api/publications/my` | Lecturer | Get own publications |
| POST | `/api/publications/draft` | Lecturer | Create new draft |
| PATCH | `/api/publications/:id/draft` | Lecturer (owner) | Update own draft |
| POST | `/api/publications/:id/submit` | Lecturer (owner) | Submit for review |
| DELETE | `/api/publications/:id` | Lecturer (owner) | Delete own draft |
| POST | `/api/publications/:id/document` | Lecturer (owner) | Upload publication document |
| GET | `/api/publications/:id/document/scan-status` | Lecturer (owner), Mod, Admin | Poll scan status |

### Browse & Search (`/api/browse`, `/api/search`)

| Method | Path | Access | Purpose |
|---|---|---|---|
| GET | `/api/browse` | Authenticated | Unified browse for projects + publications |
| GET | `/api/browse/departments` | Authenticated | Department list for filter |
| GET | `/api/browse/years` | Authenticated | Academic years for filter |
| GET | `/api/browse/technologies` | Authenticated | Technology tags for filter |
| GET | `/api/search` | Authenticated | Full-text search |
| GET | `/api/search/suggestions` | Authenticated | Autocomplete suggestions |
| GET | `/api/search/similar/:projectId` | Authenticated | Find similar projects |

### Reservations (`/api/reservations`)

| Method | Path | Access | Purpose |
|---|---|---|---|
| POST | `/api/reservations` | Authenticated | Create reservation |
| DELETE | `/api/reservations/:id` | Owner, Admin | Cancel reservation |
| GET | `/api/reservations/my` | Authenticated | Get own reservations |
| GET | `/api/reservations/availability` | Authenticated | Check slot availability |
| GET | `/api/reservations/calendar/:projectId` | Authenticated | Get 14-day availability calendar |
| POST | `/api/reservations/:id/renewal-request` | Owner | Request access renewal |

### Waitlist (`/api/waitlist`)

| Method | Path | Access | Purpose |
|---|---|---|---|
| POST | `/api/waitlist` | Authenticated | Join waitlist |
| DELETE | `/api/waitlist/:id` | Owner | Leave waitlist |
| POST | `/api/waitlist/:id/claim` | Notified user | Claim offered slot |
| GET | `/api/waitlist/my` | Authenticated | Get own waitlist entries |

### Memoir Viewer (`/api/viewer`)

| Method | Path | Access | Purpose |
|---|---|---|---|
| POST | `/api/viewer/open` | Owner with reservation | Open viewer session |
| GET | `/api/viewer/page` | Session token holder | Get single watermarked page |
| POST | `/api/viewer/heartbeat` | Session token holder | Update time spent |
| POST | `/api/viewer/close` | Session token holder | Close session |

### Moderation (`/api/moderation`)

| Method | Path | Access | Purpose |
|---|---|---|---|
| GET | `/api/moderation/queue` | Mod, Admin | Get pending review queue |
| POST | `/api/moderation/projects/:id/approve` | Mod, Admin | Approve project |
| POST | `/api/moderation/projects/:id/reject` | Mod, Admin | Reject project |
| POST | `/api/moderation/projects/:id/request-reupload` | Mod, Admin | Request re-upload |
| POST | `/api/moderation/projects/:id/mark-duplicate` | Mod, Admin | Mark as duplicate |
| POST | `/api/moderation/projects/:id/hide` | Mod, Admin | Hide project |
| POST | `/api/moderation/projects/:id/archive` | Mod, Admin | Archive project |
| POST | `/api/moderation/projects/:id/restore` | Mod, Admin | Restore project |
| GET | `/api/moderation/projects/:id/history` | Mod, Admin, Owner | Moderation history |
| POST | `/api/moderation/publications/:id/approve` | Mod, Admin | Approve publication |
| POST | `/api/moderation/publications/:id/reject` | Mod, Admin | Reject publication |
| POST | `/api/moderation/publications/:id/request-reupload` | Mod, Admin | Request re-upload |
| POST | `/api/moderation/publications/:id/hide` | Mod, Admin | Hide publication |
| POST | `/api/moderation/publications/:id/archive` | Mod, Admin | Archive publication |
| POST | `/api/moderation/publications/:id/restore` | Mod, Admin | Restore publication |
| GET | `/api/moderation/publications/:id/history` | Mod, Admin, Owner | Moderation history |
| POST | `/api/moderation/reservations/:id/approve-renewal` | Mod, Admin | Approve renewal |
| POST | `/api/moderation/reservations/:id/reject-renewal` | Mod, Admin | Reject renewal |

### Notifications (`/api/notifications`)

| Method | Path | Access | Purpose |
|---|---|---|---|
| GET | `/api/notifications` | Authenticated | Get all notifications |
| GET | `/api/notifications/unread-count` | Authenticated | Get unread count for badge |
| PATCH | `/api/notifications/:id/read` | Owner | Mark single notification as read |
| PATCH | `/api/notifications/read-all` | Authenticated | Mark all as read |

### Admin — Users (`/api/admin/users`)

| Method | Path | Access | Purpose |
|---|---|---|---|
| GET | `/api/admin/users` | Admin | List all users |
| GET | `/api/admin/users/:id` | Admin | Get user detail |
| PATCH | `/api/admin/users/:id/role` | Admin | Change user role |
| PATCH | `/api/admin/users/:id/suspend` | Admin | Suspend account |
| PATCH | `/api/admin/users/:id/activate` | Admin | Activate account |
| POST | `/api/admin/users/:id/reset-password` | Admin | Send reset link |
| GET | `/api/admin/users/export` | Admin | Export user list |

### Admin — Settings & Schedule (`/api/admin`)

| Method | Path | Access | Purpose |
|---|---|---|---|
| GET | `/api/admin/settings` | Admin | Get all system settings |
| PATCH | `/api/admin/settings` | Admin | Update a setting |
| GET | `/api/admin/schedule` | Admin | Get access schedule |
| PATCH | `/api/admin/schedule/day` | Admin | Update day schedule |
| POST | `/api/admin/schedule/closures` | Admin | Add closure date |
| DELETE | `/api/admin/schedule/closures/:date` | Admin | Remove closure date |
| PATCH | `/api/admin/branding` | Admin | Update branding |
| GET | `/api/admin/email-templates` | Admin | Get email templates |
| PATCH | `/api/admin/email-templates/:name` | Admin | Update email template |
| GET | `/api/admin/moderation/rejection-templates` | Mod, Admin | Get rejection templates |
| POST | `/api/admin/moderation/rejection-templates` | Admin | Create rejection template |
| POST | `/api/admin/backup` | Admin | Trigger manual backup |
| GET | `/api/admin/backup/history` | Admin | Get backup history |

### Admin — Reports & Audit (`/api/admin/reports`)

| Method | Path | Access | Purpose |
|---|---|---|---|
| GET | `/api/admin/reports/projects` | Mod (limited), Admin | Project activity report |
| GET | `/api/admin/reports/viewing-sessions` | Admin | Session analytics |
| GET | `/api/admin/reports/reservations` | Admin | Reservation analytics |
| GET | `/api/admin/reports/users` | Admin | User engagement report |
| GET | `/api/admin/reports/moderation` | Admin | Moderation activity report |
| GET | `/api/admin/reports/export` | Admin | Export any report |
| GET | `/api/admin/audit-logs` | Admin | Full audit log |
| GET | `/api/admin/audit-logs/:userId` | Admin | User-specific audit log |

---

> **Document Classification:** Internal — University Use Only  
> **Institution:** Adventist University of Central Africa (AUCA), Kigali — Rwanda  
> **Prepared by:** Serge Benit (ID: 27311) & Akize Israel (ID: 25883)  
> **Version:** 1.0 · 2025  
> © 2025 AUCA Connect Publication Hub — All Rights Reserved
