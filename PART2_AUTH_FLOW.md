# Part 2 Auth, Users, and Audit Guide

This document explains the Part 2 code that was implemented for AUCA Connect.
It focuses on two things:

- the runtime flow, meaning how requests move through the code
- the responsibility of each class and interface that was created for this part

Part 2 gives the project its security foundation. It covers registration, login, JWT authentication, current-user profile access, password changes, campus ID verification in simple mode, and audit logging.

## 1. High-Level Architecture

The Part 2 code is organized into these package areas:

- `controller`
  Receives HTTP requests and returns HTTP responses.
- `dto.request`
  Validates client input before the service layer runs.
- `dto.response`
  Defines the JSON shape returned to the frontend.
- `service`
  Declares business contracts.
- `service.impl`
  Implements business rules and database coordination.
- `security`
  Handles JWT generation, JWT parsing, authenticated principals, and request authentication.
- `exception`
  Standardizes business errors and JSON error responses.
- `repository`
  Persists and loads entities from the database.
- `util`
  Holds shared helper logic such as current-user lookup.
- `config`
  Wires Spring Boot, Spring Security, JSON, async execution, and future external integrations.

At runtime, the packages work together in this order:

1. A controller receives the request.
2. A request DTO validates the input.
3. Spring Security checks whether the request is public or protected.
4. If a JWT is present, the security layer loads the authenticated user into the security context.
5. The service implementation applies business rules.
6. Repositories read or write the database.
7. Response DTOs map entities into API payloads.
8. `ApiResponse` wraps successful responses.
9. `GlobalExceptionHandler` or `SecurityConfig` writes JSON error responses when something fails.

## 2. Main Request Flows

### 2.1 Registration Flow

The registration entry point is `POST /api/auth/register`.

1. `AuthController` receives the request.
2. `RegisterRequest` validates email, campus ID, first name, last name, password length, and confirm-password matching.
3. `AuthController` calls `AuthService.register(...)`.
4. `AuthServiceImpl` normalizes the email and checks `UserRepository.existsByUniversityEmail(...)`.
5. `AuthServiceImpl` calls `CampusVerificationService.verify(email, campusId)`.
6. `SimpleCampusVerificationService` runs the active verification logic:
   - campus ID must not be blank
   - campus ID must be numeric
   - campus ID must be between `20000` and `29000`
   - campus ID must not already exist in `CampusProfileRepository`
7. If validation passes, `SimpleCampusVerificationService` returns `CampusData`.
8. `AuthServiceImpl` encodes the password with `PasswordEncoder`.
9. `AuthServiceImpl` creates and saves a `User` entity with:
   - role `STUDENT`
   - status `ACTIVE`
10. `AuthServiceImpl` creates and saves a linked `CampusProfile`.
11. `AuthServiceImpl` uses `JwtTokenProvider.generateLoginToken(...)` to create a JWT.
12. `AuthServiceImpl` maps the result to `AuthResponse`.
13. `AuthController` returns `201 Created` with `ApiResponse<AuthResponse>`.

Important business rule:

- self-registration always creates a `STUDENT`

### 2.2 Login Flow

The login entry point is `POST /api/auth/login`.

1. `AuthController` receives `LoginRequest` and extracts the client IP address.
2. `LoginRequest` validates that email and password are present.
3. `AuthController` calls `AuthService.login(...)`.
4. `AuthServiceImpl` normalizes the email and loads the user with `UserRepository.findByUniversityEmail(...)`.
5. `AuthServiceImpl` checks account status before checking the password:
   - `SUSPENDED` -> audit failed login and throw `ERR_AUTH_403`
   - `PENDING_VERIFICATION` -> throw `ERR_AUTH_202`
6. `PasswordEncoder.matches(...)` checks the submitted password against `passwordHash`.
7. If the password is wrong, `AuditLogService.logLoginFailed(...)` runs asynchronously and the service throws `ERR_AUTH_401`.
8. If the password is correct, `JwtTokenProvider.generateLoginToken(...)` creates the login JWT.
9. `lastLoginAt` is updated and the user is saved.
10. `AuditLogService.logLogin(...)` writes a successful audit event asynchronously.
11. The result is mapped to `AuthResponse` and wrapped by `ApiResponse`.

### 2.3 Protected Request Flow

Every protected request uses the login token in this header:

```http
Authorization: Bearer <jwt-token>
```

The request flow is:

1. `SecurityConfig` defines which endpoints are public and which require authentication.
2. `SecurityConfig` inserts `JwtAuthenticationFilter` before `UsernamePasswordAuthenticationFilter`.
3. `JwtAuthenticationFilter` reads the `Authorization` header.
4. If the header is missing or does not start with `Bearer `, the filter skips authentication and continues the chain.
5. If a token exists, `JwtAuthenticationFilter` asks `JwtTokenProvider` whether the token is expired.
6. If the token is usable, `JwtAuthenticationFilter` extracts the user ID from the token.
7. `CustomUserDetailsService.loadUserById(...)` loads the `User` from the database.
8. `CustomUserDetailsService` converts the entity into `AucaUserDetails`.
9. `JwtAuthenticationFilter` builds a `UsernamePasswordAuthenticationToken` and places it in `SecurityContextHolder`.
10. The controller or service can now use `@PreAuthorize(...)` and `SecurityUtils.getCurrentUserId()`.

### 2.4 Get My Profile Flow

The profile entry point is `GET /api/users/me`.

1. `UserController` receives the request.
2. `SecurityUtils.getCurrentUserId()` reads the authenticated principal from `SecurityContextHolder`.
3. `UserController` calls `UserService.getMyProfile(...)`.
4. `UserServiceImpl` loads the `User` with `UserRepository.findById(...)`.
5. `UserResponse.from(user)` maps the entity to API output.
6. `CampusProfileResponse.from(user.getCampusProfile())` maps the nested campus profile.
7. `UserController` returns `ApiResponse<UserResponse>`.

### 2.5 Update My Profile Flow

The update entry point is `PATCH /api/users/me`.

1. `UserController` accepts `UpdateProfileRequest`.
2. `UpdateProfileRequest` validates only the fields that were sent.
3. `SecurityUtils` provides the current user ID.
4. `UserServiceImpl` loads the user from the database.
5. `UserServiceImpl` updates only non-null fields:
   - `firstName`
   - `lastName`
   - `phoneNumber`
   - `profileAvatarUrl`
6. The user is saved and returned as `UserResponse`.

### 2.6 Change Password Flow

The password-change entry point is `PATCH /api/users/me/password`.

1. `UserController` receives `ChangePasswordRequest`.
2. `ChangePasswordRequest` validates current password, new password, minimum length, and confirmation match.
3. `SecurityUtils` provides the current user ID.
4. `UserServiceImpl` loads the user.
5. `PasswordEncoder.matches(...)` verifies `currentPassword`.
6. If the current password is wrong, `UserServiceImpl` throws `ERR_AUTH_401`.
7. If valid, the new password is encoded and saved.
8. `AuditLogService.log(...)` writes a `PASSWORD_CHANGED` audit event asynchronously.

### 2.7 Logout Flow

The logout entry point is `POST /api/auth/logout`.

1. `AuthController` gets the current user ID from `SecurityUtils`.
2. `AuthController` extracts the client IP.
3. `AuthController` calls `AuthService.logout(...)`.
4. `AuthServiceImpl` loads the user and calls `AuditLogService.logLogout(...)`.
5. The endpoint returns `204 No Content`.

Current limitation:

- token blacklisting is not implemented yet, so logout is currently an audit event rather than a server-side token revocation

### 2.8 Error Handling Flow

There are two main error paths.

Application and validation errors:

1. Services throw `AucaException` with a specific `ErrorCode`.
2. `GlobalExceptionHandler` catches the exception.
3. `GlobalExceptionHandler` returns `ErrorResponse` as JSON with the right HTTP status.

Security-layer errors:

1. A protected endpoint is called without a valid authenticated user.
2. Spring Security rejects the request before it reaches the controller.
3. `SecurityConfig` writes a JSON `401` or `403` body directly using `ErrorResponse`.

This keeps the API consistent and prevents Spring from returning HTML error pages.

### 2.9 Audit Logging Flow

Audit logging is asynchronous so it never slows the main request unnecessarily.

1. A service such as `AuthServiceImpl` or `UserServiceImpl` calls `AuditLogService`.
2. `AuditLogServiceImpl` runs with `@Async`.
3. It optionally loads the actor user through `UserRepository`.
4. It builds an `AuditLog` entity.
5. It saves the record through `AuditLogRepository`.
6. If persistence fails, the exception is logged and swallowed.

That last step is important: a failed audit insert must not break login, registration, or password change.

## 3. How the Main Parts Depend on Each Other

The most important class relationships are:

- `AuthController` -> `AuthService`
- `UserController` -> `UserService`
- `AuthServiceImpl` -> `UserRepository`
- `AuthServiceImpl` -> `CampusProfileRepository`
- `AuthServiceImpl` -> `CampusVerificationService`
- `AuthServiceImpl` -> `JwtTokenProvider`
- `AuthServiceImpl` -> `AuditLogService`
- `UserServiceImpl` -> `UserRepository`
- `UserServiceImpl` -> `AuditLogService`
- `JwtAuthenticationFilter` -> `JwtTokenProvider`
- `JwtAuthenticationFilter` -> `CustomUserDetailsService`
- `CustomUserDetailsService` -> `UserRepository`
- `SimpleCampusVerificationService` -> `CampusProfileRepository`
- `AuditLogServiceImpl` -> `AuditLogRepository`
- `AuditLogServiceImpl` -> `UserRepository`
- `GlobalExceptionHandler` -> `ErrorCode` and `ErrorResponse`
- `SecurityUtils` -> `AucaUserDetails`

## 4. Class and Interface Reference

This section explains every class and interface created for Part 2.

### 4.1 Application and Configuration

#### `Application`

Role:

- starts the Spring Boot application
- enables async execution, caching, and JPA auditing
- registers the shared async `ThreadPoolTaskExecutor`

Why it matters:

- `AuditLogServiceImpl` depends on async execution from here

#### `SecurityConfig`

Role:

- defines public and protected endpoints
- disables session-based security features for a stateless JWT API
- registers `JwtAuthenticationFilter`
- creates JSON `401` and `403` responses
- exposes `PasswordEncoder` and `AuthenticationManager`

Why it matters:

- it is the central rulebook for who can access what

#### `JacksonConfig`

Role:

- exposes the shared `ObjectMapper`
- ensures JSON serialization works correctly, including Java date/time types

Why it matters:

- `SecurityConfig`, `GlobalExceptionHandler`, and `AuditLogServiceImpl` all rely on consistent JSON serialization

#### `RestTemplateConfig`

Role:

- provides a `RestTemplate` bean with timeouts for future external calls

Why it matters:

- it prepares the project for the future real campus verification API

#### `StorageProperties`

Role:

- binds storage-related properties from configuration

Why it matters:

- it is not part of the auth flow directly, but it is part of the active application configuration layer

### 4.2 Exception and Error Classes

#### `ErrorCode`

Role:

- central enum of business error names, public error codes, and matching HTTP statuses

Why it matters:

- services and handlers use one source of truth for errors instead of hardcoded strings and status codes

#### `AucaException`

Role:

- custom runtime exception that carries an `ErrorCode` and a business message

Why it matters:

- it is the standard way for service logic to signal known business failures

#### `GlobalExceptionHandler`

Role:

- catches controller-layer exceptions
- converts validation, auth, access, and unexpected errors into JSON `ErrorResponse`

Why it matters:

- it guarantees the frontend receives a predictable JSON error body

### 4.3 Response Wrappers

#### `ApiResponse<T>`

Role:

- standard success wrapper for all controller responses
- contains `data`, `message`, and `timestamp`

Why it matters:

- it keeps all successful responses consistent

#### `ErrorResponse`

Role:

- standard error wrapper with `errorCode`, `message`, and `timestamp`

Why it matters:

- it keeps all failed responses consistent

### 4.4 Security Classes

#### `AucaUserDetails`

Role:

- custom Spring Security principal
- stores:
  - user UUID
  - email
  - password hash
  - granted authorities
  - enabled flag
  - account locked flag

Why it matters:

- the UUID is needed so controllers and services can identify the authenticated user without an extra query just to read a username

#### `CustomUserDetailsService`

Role:

- loads a user by email or UUID
- converts the `User` entity into `AucaUserDetails`

Why it matters:

- Spring Security depends on it when reconstructing the authenticated principal from a JWT

#### `JwtTokenProvider`

Role:

- generates login tokens
- generates viewer-session tokens
- extracts user ID, email, and role claims
- validates token structure and expiration
- validates viewer-token ownership

Why it matters:

- it is the single authority for JWT creation and parsing

#### `JwtAuthenticationFilter`

Role:

- runs once per request
- reads bearer tokens from headers
- validates the token
- loads the user
- places authentication into `SecurityContextHolder`

Why it matters:

- it is what turns a raw JWT string into an authenticated request

### 4.5 Utility Class

#### `SecurityUtils`

Role:

- reads the current authenticated user ID from `SecurityContextHolder`

Why it matters:

- controllers use one shared helper instead of duplicating security-context parsing logic

### 4.6 Service Interfaces

#### `AuthService`

Role:

- defines the contract for login, registration, forgot-password, reset-password, and logout

Why it matters:

- the controller depends on the interface, not the implementation

#### `UserService`

Role:

- defines the contract for profile retrieval, partial profile updates, and password changes

Why it matters:

- it keeps controller code thin and focused on HTTP concerns

#### `CampusVerificationService`

Role:

- abstracts campus verification behind an interface

Why it matters:

- the simple implementation can later be replaced by the real AUCA API integration without changing controller or auth logic

#### `CampusVerificationService.CampusData`

Role:

- data carrier returned by campus verification
- holds campus-related information such as campus ID, faculty, department, academic level, and enrollment flag

Why it matters:

- it decouples registration logic from the concrete verification source

#### `AuditLogService`

Role:

- defines the contract for writing audit events

Why it matters:

- every future feature can reuse one shared audit entry point

### 4.7 Service Implementations

#### `AuthServiceImpl`

Role:

- implements:
  - login
  - register
  - forgot-password stub
  - reset-password stub
  - logout

Main responsibilities:

- normalize email input
- enforce the self-registration `STUDENT` rule
- verify campus IDs
- encode passwords
- issue JWT tokens
- update last login time
- trigger audit logging

Why it matters:

- it is the main business engine for authentication

#### `UserServiceImpl`

Role:

- implements current-user profile operations

Main responsibilities:

- load the current user
- apply partial profile updates
- validate current password before changing it
- write password-change audit records

Why it matters:

- it owns all authenticated self-service account changes

#### `SimpleCampusVerificationService`

Role:

- active `@Primary` implementation of `CampusVerificationService`

Main responsibilities:

- validate blank campus IDs
- validate numeric format
- validate the `20000` to `29000` range
- reject duplicate campus IDs already stored in the database

Why it matters:

- it gives the project a working registration gate before the real AUCA API exists

#### `RealCampusVerificationService`

Role:

- non-active stub for the future external AUCA verification integration

Why it matters:

- it documents the intended replacement path clearly and prevents confusion about what is live now

#### `AuditLogServiceImpl`

Role:

- implements async audit persistence

Main responsibilities:

- create `AuditLog` entities
- save them through `AuditLogRepository`
- serialize reason details to JSON
- swallow failures after logging them

Why it matters:

- it centralizes the audit trail and keeps audit failures from breaking real user actions

### 4.8 Controllers

#### `AuthController`

Role:

- exposes:
  - `POST /api/auth/login`
  - `POST /api/auth/register`
  - `POST /api/auth/forgot-password`
  - `POST /api/auth/reset-password`
  - `POST /api/auth/logout`

Why it matters:

- it is the public entry point for all authentication actions

#### `UserController`

Role:

- exposes:
  - `GET /api/users/me`
  - `PATCH /api/users/me`
  - `PATCH /api/users/me/password`

Why it matters:

- it is the entry point for authenticated self-service user operations

### 4.9 Request DTO Classes

#### `LoginRequest`

Role:

- captures login email and password

Why it matters:

- keeps controller input validation simple and explicit

#### `RegisterRequest`

Role:

- captures registration data:
  - email
  - campus ID
  - first name
  - last name
  - password
  - confirm password

Why it matters:

- it enforces field validation and password confirmation before service logic runs

#### `ForgotPasswordRequest`

Role:

- captures the email used for the forgot-password flow

Why it matters:

- it keeps that endpoint ready even though token generation is still deferred to Part 5B

#### `ResetPasswordRequest`

Role:

- captures reset token, new password, and password confirmation

Why it matters:

- it defines the public contract for the future reset flow

#### `UpdateProfileRequest`

Role:

- carries optional profile fields for partial updates

Why it matters:

- it supports `PATCH` semantics by allowing only the sent fields to change

#### `ChangePasswordRequest`

Role:

- carries current password, new password, and confirm-new-password

Why it matters:

- it validates password change input before `UserServiceImpl` checks the current hash

### 4.10 Response DTO Classes

#### `AuthResponse`

Role:

- returned after successful registration or login

Fields it carries:

- `userId`
- `token`
- `email`
- `firstName`
- `lastName`
- `role`
- `department`
- `academicYear`
- `faculty`

Why it matters:

- it gives the frontend everything needed to initialize the authenticated session

#### `CampusProfileResponse`

Role:

- maps `CampusProfile` into API output

Why it matters:

- it keeps campus-related profile data grouped in one nested response object

#### `UserResponse`

Role:

- maps the current user plus nested campus profile into API output

Why it matters:

- it is the main read model for `GET /api/users/me` and profile updates

### 4.11 Repository Interfaces Used Directly in Part 2

#### `UserRepository`

Role:

- loads users by email
- checks whether an email already exists
- loads users by ID

Why it matters:

- it is central to login, registration, token-based user loading, and profile operations

#### `CampusProfileRepository`

Role:

- loads campus profiles by user ID or campus ID
- checks whether a campus ID already exists

Why it matters:

- registration depends on it to prevent duplicate campus IDs

#### `AuditLogRepository`

Role:

- saves audit log entries
- supports audit filtering by user or action

Why it matters:

- it is the persistence layer for the system audit trail

### 4.12 Other Repository Interfaces Present

These repository interfaces are in the project and support later parts of the platform:

- `ProjectRepository`
- `ProjectAuthorRepository`
- `MemoirFileRepository`
- `GithubRepoRepository`
- `PublicationRepository`
- `PublicationAuthorRepository`
- `PublicationFileRepository`
- `ReservationRepository`
- `ViewerSessionRepository`
- `PageViewLogRepository`
- `ModerationActionRepository`
- `NotificationRepository`
- `WaitlistRepository`
- `SystemSettingRepository`

They are not the center of Part 2, but they are part of the full project repository layer.

## 5. Main Entities Used by the Flow

These entities were already present from the model layer, but they are the core domain objects used by Part 2:

- `User`
  Stores login identity, role, status, password hash, names, avatar URL, and last login timestamp.
- `CampusProfile`
  Stores the campus identity linked to a user, including campus ID and academic metadata.
- `AuditLog`
  Stores immutable audit events such as login, logout, login failure, and password changes.

## 6. Supporting Configuration Files

These files support the runtime flow even though they are not Java classes:

- `src/main/resources/application.properties`
  Holds JWT settings, campus verification mode, mail settings, cache settings, and async settings.
- `.env`
  Holds environment-specific secrets and external credentials for local development.

## 7. Test Classes and What They Verify

#### `JwtTokenProviderTest`

Checks:

- login-token creation
- user ID extraction
- role extraction
- expiration checks
- viewer-token validation behavior

#### `SimpleCampusVerificationServiceTest`

Checks:

- valid campus IDs
- blank campus IDs
- non-numeric campus IDs
- out-of-range campus IDs
- duplicate campus IDs

#### `AuthServiceTest`

Checks:

- successful login
- wrong-password login failure
- suspended-account behavior
- unknown-account behavior
- successful registration
- duplicate-email registration
- student-only self-registration rule

#### `AuditLogServiceTest`

Checks:

- audit save behavior
- failed audit persistence swallowing
- login-failed reason serialization

#### `AuthAndUserIntegrationTest`

Checks the real HTTP and database flow for:

- registration
- login
- protected endpoint access
- profile retrieval
- profile patch
- password change
- audit row creation
- JSON security error responses

#### `ApplicationTests`

Checks:

- Spring application context starts correctly

## 8. Final Summary

If you want to understand Part 2 quickly, these are the most important chains to remember:

- registration:
  `AuthController` -> `RegisterRequest` -> `AuthServiceImpl` -> `SimpleCampusVerificationService` -> `UserRepository` and `CampusProfileRepository` -> `JwtTokenProvider` -> `AuthResponse`
- login:
  `AuthController` -> `LoginRequest` -> `AuthServiceImpl` -> `UserRepository` -> `PasswordEncoder` -> `JwtTokenProvider` -> `AuditLogServiceImpl`
- protected request:
  `SecurityConfig` -> `JwtAuthenticationFilter` -> `JwtTokenProvider` -> `CustomUserDetailsService` -> `SecurityContextHolder` -> controller/service
- current user operations:
  `UserController` -> `SecurityUtils` -> `UserServiceImpl` -> `UserRepository` -> `UserResponse`
- error handling:
  service throws `AucaException` -> `GlobalExceptionHandler` returns `ErrorResponse`
- audit handling:
  business service -> `AuditLogServiceImpl` -> `AuditLogRepository`

Together, these classes form the complete Part 2 authentication and user-management foundation that the later project, publication, reservation, and admin features will build on.
