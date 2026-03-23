# AUCA Connect Models

This package contains the database models for AUCA Connect. Each class represents a main part of the system such as users, projects, publications, reservations, viewing sessions, moderation, and settings.

## How The Models Work Together

1. `User` is the root account for everyone using the system.
2. `CampusProfile` adds verified academic identity to a user.
3. `Project` and `Publication` store the main academic content.
4. `ProjectAuthor` and `PublicationAuthor` store ordered co-author lists.
5. `MemoirFile`, `PublicationFile`, and `GithubRepo` store file and repository metadata attached to content.
6. `Reservation`, `ViewerSession`, and `PageViewLog` control protected memoir access and track reading activity.
7. `ModerationAction`, `AuditLog`, and `Notification` track review actions, system events, and user messages.
8. `Waitlist` manages users waiting for a reservation slot.
9. `SystemSetting` stores admin-configurable platform rules.

## Shared Base Classes

### `BaseEntity`

Used by normal editable entities.

Main attributes:

- `id`: unique UUID primary key
- `createdAt`: when the record was created
- `updatedAt`: when the record was last changed

### `AppendOnlyBaseEntity`

Used by immutable history records.

Main attributes:

- `id`: unique UUID primary key
- `createdAt`: when the record was created

This class blocks updates with `@PreUpdate`.

## Identity Models

### `User`

Represents the main account for a person who logs in.

Important attributes:

- `universityEmail`: unique login email
- `passwordHash`: encrypted password
- `firstName`, `lastName`: display name
- `phoneNumber`: optional contact
- `role`: access level such as student, lecturer, moderator, or admin
- `status`: account state such as active or suspended
- `profileAvatarUrl`: optional profile image
- `lastLoginAt`: last successful login time

Relationships:

- one `CampusProfile`
- many `Project`
- many `Publication`
- many `Reservation`
- many `Notification`
- many `ModerationAction`
- many `AuditLog`

### `CampusProfile`

Represents the verified AUCA academic profile linked to a user.

Important attributes:

- `campusId`: official campus identifier
- `faculty`: faculty name
- `department`: department name
- `academicYear`: year like `2024-2025`
- `academicLevel`: undergraduate, postgraduate, staff, or lecturer
- `supervisorName`: academic supervisor
- `verifiedAt`: when campus identity was confirmed

Relationship:

- belongs to one `User`

## Content Models

### `Project`

Represents a student-submitted project or thesis.

Important attributes:

- `title`: project title
- `abstractText`: full abstract
- `keywords`: search keywords
- `department`, `academicYear`: academic grouping
- `category`: project category
- `technologiesUsed`: tech stack list
- `status`: draft, pending approval, published, rejected, hidden, archived
- `type`: final year project, postgraduate thesis, or coursework
- `visibility`: AUCA only, restricted, or embargoed
- `supervisorName`: project supervisor
- `embargoUntil`: unlock date for embargoed projects
- `viewCount`: project page views
- `reservationCount`: total memoir reservations
- `publishedAt`: moderation publish time

Project memoir rule:

- only student-owned projects should carry a memoir file
- a project can only have one memoir because `MemoirFile` is one-to-one
- memoir upload is meant for final year projects and postgraduate theses

Relationships:

- submitted by one `User`
- has many `ProjectAuthor`
- has one `MemoirFile`
- has one `GithubRepo`
- has many `Reservation`
- has many `ModerationAction`
- has many `Waitlist`

### `ProjectAuthor`

Represents one author entry for a project.

Important attributes:

- `fullName`: author name
- `email`: optional email
- `studentId`: optional student ID
- `role`: contribution label
- `authorOrder`: display order

Relationships:

- belongs to one `Project`
- may link to one `User`

### `MemoirFile`

Stores metadata for a protected project PDF memoir.

Important attributes:

- `originalFileName`: uploaded filename
- `storagePath`: private backend storage path
- `sha256Checksum`: file integrity hash
- `fileSizeBytes`: file size
- `totalPages`: page count
- `scanStatus`: malware scan result
- `mimeType`: file type, usually PDF

Relationship:

- belongs to one `Project`

Rule:

- memoir upload is restricted to student projects
- coursework projects should not carry a memoir file

### `GithubRepo`

Stores GitHub repository metadata attached to a project.

Important attributes:

- `repoUrl`, `repoName`, `repoOwner`: repository identity
- `defaultBranch`: main branch
- `finalCommitHash`: academic final snapshot
- `finalCommitMessage`, `finalTag`: submission markers
- `readmeContent`: cached README content
- `sourceTreeJson`: cached repository tree for in-platform browsing
- `repositorySnapshotPath`: cached source snapshot path
- `primaryLanguage`: detected main language
- `starCount`, `forkCount`: GitHub engagement
- `isPrivate`: private/public state
- `embeddedViewEnabled`: whether platform code viewing is enabled
- `lastSyncedAt`: last GitHub sync
- `lastSourceIndexedAt`: last source viewer refresh

Relationship:

- belongs to one `Project`

### `Publication`

Represents a lecturer-submitted publication.

Important attributes:

- `title`: publication title
- `abstractText`: publication abstract
- `keywords`: academic keywords
- `type`: publication type
- `department`, `academicYear`: academic grouping
- `doiOrExternalLink`: DOI or external source
- `journalOrConferenceName`: venue name
- `status`: publication workflow state
- `visibility`: access level
- `versionLabel`: publication version name
- `accessNotes`: viewer notes
- `notifyCoAuthors`: whether co-authors should be notified
- `embargoUntil`: release date if embargoed
- `viewCount`: detail page views
- `publishedAt`: publish time

Relationships:

- submitted by one `User`
- has many `PublicationAuthor`
- has one `PublicationFile`
- has many `ModerationAction`

### `PublicationAuthor`

Represents one author entry for a publication.

Important attributes:

- `fullName`: author name
- `email`: author email
- `institution`: author institution
- `role`: lead, corresponding, or co-author role
- `authorOrder`: academic author order

Relationships:

- belongs to one `Publication`
- may link to one `User`

### `PublicationFile`

Stores metadata for a protected publication document.

Important attributes:

- `originalFileName`: uploaded filename
- `storagePath`: private storage path
- `sha256Checksum`: integrity hash
- `fileSizeBytes`: file size
- `mimeType`: document type
- `versionLabel`: document version
- `scanStatus`: reused scan status from `MemoirFile`

Relationship:

- belongs to one `Publication`

## Access Control Models

### `Reservation`

Represents a booking for memoir access.

Important attributes:

- `slotStart`, `slotEnd`: access window
- `status`: reservation lifecycle
- `cancellationReason`: why it was cancelled
- `renewalRequested`, `renewalApproved`: renewal state
- `renewalRejectionReason`: why renewal was rejected
- `noShowCount`: missed access counter
- `confirmedAt`, `cancelledAt`: important status timestamps

Relationships:

- belongs to one `User`
- belongs to one `Project`
- has one `ViewerSession`

### `ViewerSession`

Represents the live secured reading session created from a reservation.

Important attributes:

- `sessionToken`: secure access token
- `totalPagesViewed`: pages opened
- `lastPageViewed`: latest page number
- `totalPagesInDocument`: copied page count
- `totalTimeSpentSeconds`: reading duration
- `sessionActive`: whether the session is still open
- `startedAt`, `endedAt`: session timing
- `tokenExpiresAt`: hard expiry time
- `ipAddress`, `userAgent`: security metadata

Relationships:

- belongs to one `Reservation`
- belongs to one `User`
- belongs to one `Project`
- has many `PageViewLog`

### `PageViewLog`

Represents one immutable page view inside a viewer session.

Important attributes:

- `pageNumber`: viewed page
- `timeSpentSeconds`: time spent on that page
- `createdAt` mapped as `viewed_at`: exact viewing time

Relationship:

- belongs to one `ViewerSession`

### `Waitlist`

Represents a queue entry when no reservation slot is available.

Important attributes:

- `positionInQueue`: place in line
- `status`: waiting, notified, converted, expired, or cancelled
- `notifiedAt`: when a slot offer was sent
- `expiresAt`: deadline to claim the offer

Relationships:

- belongs to one `User`
- belongs to one `Project`

## Governance Models

### `ModerationAction`

Represents one permanent moderation decision.

Important attributes:

- `actionType`: approve, reject, hide, archive, and related actions
- `itemType`: project or publication
- `itemId`: ID of the moderated item
- `reason`: moderation explanation
- `duplicateOfId`: original item when marked duplicate
- `rejectionTemplateName`: used rejection template

Relationships:

- performed by one `User`
- may reference one `Project`
- may reference one `Publication`

### `AuditLog`

Represents one immutable system audit event.

Important attributes:

- `action`: what happened
- `entityType`: entity involved
- `entityId`: specific entity ID
- `detailsJson`: extra structured event details
- `ipAddress`, `userAgent`: request context
- `createdAt` mapped as `timestamp`: event time

Relationship:

- may belong to one `User`

### `Notification`

Represents one in-app notification sent to a user.

Important attributes:

- `notificationType`: type of message
- `title`: short heading
- `body`: full message
- `isRead`: read status
- `emailSent`: email delivery status
- `relatedEntityId`, `relatedEntityType`: navigation target
- `readAt`: when the user opened it

Relationship:

- belongs to one `User`

## Configuration Model

### `SystemSetting`

Represents one configurable platform setting.

Important attributes:

- `settingKey`: unique config name
- `settingValue`: stored text value
- `description`: admin explanation
- `category`: logical settings section
- `lastModifiedBy`: last admin identity

This model has no relationships because it is read by services across the system.

## Storage

The platform is configured to use a Supabase S3-compatible bucket named `connect`.

Main folders:

- `profile-avatars`: user avatar images
- `memoirs`: thesis and memoir PDF files
- `publication-files`: publication documents
- `github-snapshots`: cached GitHub source snapshots for in-platform viewing

The endpoint, region, and bucket are configured in `application.properties`. Access credentials are expected from environment variables so secrets are not hardcoded in source.
