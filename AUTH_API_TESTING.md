# Auth API Testing Guide

This guide is for manually testing the auth endpoints that currently exist in the backend.

## Current Status

| Endpoint | Status | Notes |
| --- | --- | --- |
| `POST /api/auth/register` | Working | Creates a student account and returns a JWT |
| `POST /api/auth/login` | Working | Returns a JWT for valid credentials |
| `POST /api/auth/forgot-password` | Working | Generates a one-time reset token and sends a reset link email that expires after 10 minutes |
| `POST /api/auth/reset-password` | Working | Validates the emailed reset token, updates the password, and invalidates the token after use |
| `POST /api/auth/logout` | Partially implemented | Requires JWT and writes an audit log, but does not revoke the token yet |
| `GET /api/users/me` | Working | Useful for confirming the JWT works on a protected endpoint |

## Base URL

Use:

```text
http://localhost:8081
```

A custom `server.port=8081` is configured in `src/main/resources/application.properties`.

## Response Shapes

Successful responses use:

```json
{
  "data": {},
  "message": "Operation successful",
  "timestamp": "2026-03-23T10:15:30.000"
}
```

Error responses use:

```json
{
  "errorCode": "ERR-VALIDATION",
  "message": "field: validation message",
  "timestamp": "2026-03-23T10:15:30.000"
}
```

## Test Data

### Valid registration data

Use a fresh email and a fresh campus ID each time you test registration.

The current campus verification mode is `simple`, which means:

- `campusId` must be numeric
- `campusId` must be between `20000` and `29000`
- `campusId` must not already exist in the database

Suggested test users:

| Label | Email | Campus ID | Password | First Name | Last Name |
| --- | --- | --- | --- | --- | --- |
| User A | `student1@auca.ac.rw` | `27311` | `password123` | `Serge` | `Benit` |
| User B | `student2@auca.ac.rw` | `27312` | `password123` | `Alice` | `Tester` |
| User C | `student3@auca.ac.rw` | `27313` | `password123` | `John` | `Sample` |

If one of these is already taken, keep the same pattern and pick another unused campus ID inside `20000-29000`.

### Negative test data

| Scenario | Email | Campus ID | Password | Expected Result |
| --- | --- | --- | --- | --- |
| Duplicate email | `student1@auca.ac.rw` | `27314` | `password123` | `409 ERR-AUTH-409` after User A is created |
| Duplicate campus ID | `student4@auca.ac.rw` | `27311` | `password123` | `409 ERR-AUTH-409` after User A is created |
| Invalid campus ID below range | `student5@auca.ac.rw` | `15000` | `password123` | `404 ERR-VER-404` |
| Invalid campus ID non-numeric | `student6@auca.ac.rw` | `ABCDE` | `password123` | `404 ERR-VER-404` |
| Password mismatch | `student7@auca.ac.rw` | `27315` | `password123` | `400 ERR-VALIDATION` |
| Short password | `student8@auca.ac.rw` | `27316` | `short` | `400 ERR-VALIDATION` |
| Unknown login user | `missing@auca.ac.rw` | - | `password123` | `404 ERR-VER-404` |
| Wrong login password | `student1@auca.ac.rw` | - | `wrong-password` | `401 ERR-AUTH-401` |

## PowerShell Setup

```powershell
$baseUrl = "http://localhost:8081"
```

## 1. Register

### Request body

```json
{
  "email": "student1@auca.ac.rw",
  "campusId": "27311",
  "firstName": "Serge",
  "lastName": "Benit",
  "password": "password123",
  "confirmPassword": "password123"
}
```

### PowerShell test

```powershell
$registerBody = @{
  email = "student1@auca.ac.rw"
  campusId = "27311"
  firstName = "Serge"
  lastName = "Benit"
  password = "password123"
  confirmPassword = "password123"
} | ConvertTo-Json

$registerResponse = Invoke-RestMethod `
  -Method Post `
  -Uri "$baseUrl/api/auth/register" `
  -ContentType "application/json" `
  -Body $registerBody

$registerResponse
$token = $registerResponse.data.token
```

### Expected result

- Status: `201 Created`
- Message: `Account created successfully.`
- `data.token` should be present
- `data.role` should be `STUDENT`

### Useful failure checks

- Re-submit the same email: expect `409 ERR-AUTH-409`
- Use `campusId = 15000`: expect `404 ERR-VER-404`
- Use `campusId = ABCDE`: expect `404 ERR-VER-404`
- Use mismatched `confirmPassword`: expect `400 ERR-VALIDATION`

## 2. Login

### Request body

```json
{
  "email": "student1@auca.ac.rw",
  "password": "password123"
}
```

### PowerShell test

```powershell
$loginBody = @{
  email = "student1@auca.ac.rw"
  password = "password123"
} | ConvertTo-Json

$loginResponse = Invoke-RestMethod `
  -Method Post `
  -Uri "$baseUrl/api/auth/login" `
  -ContentType "application/json" `
  -Body $loginBody

$loginResponse
$token = $loginResponse.data.token
```

### Expected result

- Status: `200 OK`
- Message: `Login successful.`
- `data.token` should be present
- `data.email` should match the login email
- `data.role` should be `STUDENT`

### Useful failure checks

- Wrong password: expect `401 ERR-AUTH-401`
- Unknown email: expect `404 ERR-VER-404`

Wrong-password example:

```powershell
$wrongLoginBody = @{
  email = "student1@auca.ac.rw"
  password = "wrong-password"
} | ConvertTo-Json

try {
  Invoke-RestMethod `
    -Method Post `
    -Uri "$baseUrl/api/auth/login" `
    -ContentType "application/json" `
    -Body $wrongLoginBody
} catch {
  $_.Exception.Response.StatusCode.value__
  $_.ErrorDetails.Message
}
```

## 3. Use the JWT on a Protected Endpoint

This is the quickest way to confirm the login token is valid.

Make sure you call the same app instance that issued the JWT. If you have another Spring Boot process still running on `8080`, stop it or switch your requests to `8081`.

```powershell
$headers = @{
  Authorization = "Bearer $token"
}

$meResponse = Invoke-RestMethod `
  -Method Get `
  -Uri "$baseUrl/api/users/me" `
  -Headers $headers

$meResponse
```

### Expected result

- Status: `200 OK`
- `data.email` should be the authenticated user

Without a token, the same endpoint should return:

- Status: `401 Unauthorized`
- Error code: `ERR-AUTH-401`

## 4. Logout

```powershell
Invoke-WebRequest `
  -Method Post `
  -Uri "$baseUrl/api/auth/logout" `
  -Headers $headers
```

### Expected result

- Status: `204 No Content`

### Important current behavior

- Logout currently records the action but does not invalidate the JWT on the server
- The same token may still work after logout until it expires

## 5. Forgot Password

```powershell
$forgotBody = @{
  email = "student1@auca.ac.rw"
} | ConvertTo-Json

$forgotResponse = Invoke-RestMethod `
  -Method Post `
  -Uri "$baseUrl/api/auth/forgot-password" `
  -ContentType "application/json" `
  -Body $forgotBody

$forgotResponse
```

### Expected result

- Status: `200 OK`
- Message: `If this email is registered, a reset link has been sent.`

### Important current behavior

- This same `200 OK` response is returned for both existing and non-existing emails
- For existing accounts, the backend generates a one-time reset token and emails a reset link
- Reset tokens expire after `10` minutes

The reset link is built from `app.auth.password-reset-url` in `src/main/resources/application.properties`.

You can also test a missing user:

```json
{
  "email": "missing@auca.ac.rw"
}
```

Expected result is still `200 OK`.

## 6. Reset Password

### Request body

Use the token from the reset email link. Do not send a login JWT here.

```json
{
  "resetToken": "token-from-email-link",
  "newPassword": "newpassword123",
  "confirmNewPassword": "newpassword123"
}
```

### PowerShell test

```powershell
$resetBody = @{
  resetToken = "token-from-email-link"
  newPassword = "newpassword123"
  confirmNewPassword = "newpassword123"
} | ConvertTo-Json

$resetResponse = Invoke-RestMethod `
  -Method Post `
  -Uri "$baseUrl/api/auth/reset-password" `
  -ContentType "application/json" `
  -Body $resetBody

$resetResponse
```

### Expected result

- Status: `200 OK`
- Message: `Password has been reset successfully.`

### Important behavior

- The reset token must come from the email link sent by `POST /api/auth/forgot-password`
- The reset token expires after `10` minutes
- The reset token can only be used once
- After a successful reset, the old password should stop working and the new password should work

### Validation checks

- Mismatched `newPassword` and `confirmNewPassword`: expect `400 ERR-VALIDATION`
- New password shorter than 8 characters: expect `400 ERR-VALIDATION`
- Invalid or expired reset token: expect `400 ERR-AUTH-400`

## Quick Retest Flow

If you want the shortest happy-path test:

1. Register a fresh user with an unused campus ID.
2. Save the returned JWT from `data.token`.
3. Call `GET /api/users/me` with `Authorization: Bearer <token>`.
4. Login with the same credentials and confirm a new JWT is returned.
5. Call logout and note that the token is not revoked yet.

## Expected Error Codes

| Code | When you should see it |
| --- | --- |
| `ERR-VALIDATION` | Invalid JSON body or bean validation failure |
| `ERR-VER-404` | Unknown account or invalid campus ID |
| `ERR-AUTH-401` | Wrong password or missing/invalid JWT |
| `ERR-AUTH-403` | Suspended user login |
| `ERR-AUTH-409` | Duplicate email or duplicate campus ID |

## Notes for Repeat Testing

- Registration requires both a unique email and a unique campus ID.
- Any unused numeric campus ID between `20000` and `29000` is valid in the current simple verification mode.
- JWT expiration is configured to `8` hours.
