# ElgibilityGPT API Documentation

Complete API reference for all services.

## Base URLs

- **Gateway**: `http://localhost:8080`
- **Core Service**: `http://localhost:8081` (internal)
- **RAG Service**: `http://localhost:8000` (internal)
- **Frontend**: `http://localhost:3000`

All client requests should go through the Gateway (port 8080).

## Authentication

### Register User

```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePass123!",
  "fullName": "John Doe"
}
```

**Response:**
```json
{
  "id": 1,
  "email": "user@example.com",
  "fullName": "John Doe",
  "createdAt": "2026-07-25T10:00:00Z"
}
```

### Login

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePass123!"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "type": "Bearer",
  "expiresIn": 86400,
  "user": {
    "id": 1,
    "email": "user@example.com",
    "fullName": "John Doe"
  }
}
```

### Authenticated Requests

Include JWT token in Authorization header:
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

## User Profile Management

### Get User Profile

```http
GET /api/profiles/me
Authorization: Bearer {token}
```

**Response:**
```json
{
  "id": 1,
  "userId": 1,
  "dateOfBirth": "2001-07-03",
  "category": "OBC",
  "gender": "MALE",
  "isPwbd": false,
  "isExServiceman": false,
  "domicileState": "Maharashtra",
  "educationLevel": "Bachelor's Degree",
  "educationSpecialization": "Commerce",
  "isDepartmentalCandidate": false
}
```

### Update User Profile

```http
PUT /api/profiles/me
Authorization: Bearer {token}
Content-Type: application/json

{
  "dateOfBirth": "2001-07-03",
  "category": "OBC",
  "gender": "MALE",
  "isPwbd": false,
  "educationLevel": "Bachelor's Degree",
  "educationSpecialization": "Commerce",
  "domicileState": "Maharashtra"
}
```

### Session Profile (Temporary, No Auth Required)

```http
POST /api/profiles/session
Content-Type: application/json

{
  "sessionId": "unique-session-uuid",
  "profileData": {
    "dateOfBirth": "2001-07-03",
    "category": "OBC",
    "gender": "MALE",
    "educationLevel": "Bachelor's Degree"
  }
}
```

## Job Posting / PDF Upload

### Upload Notification PDF

```http
POST /api/rag/upload/pdf
Authorization: Bearer {token}
Content-Type: multipart/form-data

file: (binary PDF file)
metadata: {
  "notificationTitle": "SSC CGL 2026 Notification",
  "issuingBody": "SSC",
  "notificationNumber": "SSC-CGL-2026-01",
  "notificationDate": "2026-07-15",
  "applicationStartDate": "2026-08-01",
  "applicationEndDate": "2026-08-31"
}
```

**Response:**
```json
{
  "jobPostingId": 1,
  "status": "PROCESSING",
  "message": "PDF uploaded successfully. Processing started.",
  "estimatedTimeMinutes": 5
}
```

### Check Processing Status

```http
GET /api/job-postings/1/status
Authorization: Bearer {token}
```

**Response:**
```json
{
  "jobPostingId": 1,
  "status": "COMPLETED",
  "progress": {
    "parsing": "COMPLETED",
    "chunking": "COMPLETED",
    "embedding": "COMPLETED",
    "extraction": "COMPLETED"
  },
  "totalPosts": 25,
  "totalChunks": 342,
  "processedAt": "2026-07-25T10:15:00Z"
}
```

### List Job Postings

```http
GET /api/job-postings?page=0&size=10&issuingBody=SSC
Authorization: Bearer {token}
```

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "notificationTitle": "SSC CGL 2026 Notification",
      "issuingBody": "SSC",
      "notificationNumber": "SSC-CGL-2026-01",
      "uploadDate": "2026-07-25T10:00:00Z",
      "totalPages": 125,
      "processingStatus": "COMPLETED"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 10,
  "number": 0
}
```

### Get Job Posting Details

```http
GET /api/job-postings/1
Authorization: Bearer {token}
```

**Response:**
```json
{
  "id": 1,
  "notificationTitle": "SSC CGL 2026 Notification",
  "issuingBody": "SSC",
  "notificationNumber": "SSC-CGL-2026-01",
  "uploadDate": "2026-07-25T10:00:00Z",
  "notificationDate": "2026-07-15",
  "applicationStartDate": "2026-08-01",
  "applicationEndDate": "2026-08-31",
  "examDate": "2026-10-15",
  "totalPages": 125,
  "totalPosts": 25,
  "processingStatus": "COMPLETED",
  "posts": [
    {
      "postName": "Income Tax Inspector",
      "postCode": "POST-001",
      "minAge": 18,
      "maxAge": 30,
      "educationRequirements": ["Bachelor's Degree"]
    }
  ]
}
```

## RAG Query (Q&A)

### Ask Question (General)

```http
POST /api/rag/query/ask
Authorization: Bearer {token}
Content-Type: application/json

{
  "jobPostingId": 1,
  "question": "What is the application fee for OBC female candidates?",
  "sessionId": "optional-session-uuid"
}
```

**Response:**
```json
{
  "answer": "For OBC female candidates, the application fee is ₹100 (reduced from the general category fee). This fee is non-refundable and must be paid online through the official portal.",
  "sources": [
    {
      "page": 45,
      "section": "Application Fee Structure",
      "snippet": "Female candidates (all categories except Gen/EWS): ₹100..."
    }
  ],
  "confidence": 0.92,
  "answerType": "GROUNDED"
}
```

### Eligibility Check (Single Post)

```http
POST /api/rag/eligibility/check
Authorization: Bearer {token}
Content-Type: application/json

{
  "jobPostingId": 1,
  "postCode": "POST-001",
  "postName": "Income Tax Inspector",
  "userProfile": {
    "dateOfBirth": "2001-07-03",
    "category": "OBC",
    "gender": "MALE",
    "educationLevel": "Bachelor's Degree",
    "educationSpecialization": "Commerce"
  }
}
```

**Response:**
```json
{
  "postName": "Income Tax Inspector",
  "postCode": "POST-001",
  "isEligible": true,
  "verdict": "You are eligible for this post. Your age as of 01-01-2026 is 24 years 6 months, which falls within the age limit of 18-30 years for OBC category (with 3 years relaxation). Your Bachelor's Degree in Commerce meets the educational requirement.",
  "details": {
    "ageCheck": {
      "passed": true,
      "currentAge": 24.5,
      "minAge": 18,
      "maxAge": 33,
      "relaxationApplied": 3,
      "asOnDate": "2026-01-01"
    },
    "educationCheck": {
      "passed": true,
      "required": ["Bachelor's Degree from a recognized university"],
      "provided": "Bachelor's Degree in Commerce"
    },
    "categoryCheck": {
      "passed": true,
      "category": "OBC",
      "ageRelaxation": 3
    }
  },
  "sources": [
    {
      "page": 46,
      "section": "Eligibility Criteria - Income Tax Inspector"
    }
  ],
  "missingFields": [],
  "confidence": 0.95
}
```

### Batch Eligibility Scan

```http
POST /api/rag/eligibility/scan
Authorization: Bearer {token}
Content-Type: application/json

{
  "jobPostingId": 1,
  "userProfile": {
    "dateOfBirth": "2001-07-03",
    "category": "OBC",
    "gender": "MALE",
    "educationLevel": "Bachelor's Degree",
    "educationSpecialization": "Commerce"
  }
}
```

**Response:**
```json
{
  "jobPostingId": 1,
  "totalPosts": 25,
  "eligiblePosts": [
    {
      "postName": "Income Tax Inspector",
      "postCode": "POST-001",
      "eligibilityScore": 1.0,
      "summary": "Fully eligible - meets all criteria"
    },
    {
      "postName": "Assistant Section Officer",
      "postCode": "POST-003",
      "eligibilityScore": 1.0,
      "summary": "Fully eligible - meets all criteria"
    }
  ],
  "ineligiblePosts": [
    {
      "postName": "Statistical Investigator",
      "postCode": "POST-005",
      "reason": "Requires Bachelor's Degree in Statistics/Mathematics. Your Commerce degree doesn't meet this requirement."
    }
  ],
  "insufficientInfoPosts": [
    {
      "postName": "Departmental Grade Officer",
      "postCode": "POST-008",
      "missingFields": ["isDepartmentalCandidate", "department"]
    }
  ]
}
```

### Follow-up Question (Slot Filling)

```http
POST /api/rag/eligibility/followup
Authorization: Bearer {token}
Content-Type: application/json

{
  "jobPostingId": 1,
  "postCode": "POST-008",
  "sessionId": "session-uuid"
}
```

**Response:**
```json
{
  "question": "Are you a departmental candidate? This post requires candidates to be currently serving in a government department.",
  "requiredField": "isDepartmentalCandidate",
  "additionalFields": ["department"],
  "context": "Departmental Grade Officer (POST-008) is reserved for departmental candidates only."
}
```

## User Applications

### Mark Post for Application

```http
POST /api/applications
Authorization: Bearer {token}
Content-Type: application/json

{
  "jobPostingId": 1,
  "postCode": "POST-001",
  "postName": "Income Tax Inspector",
  "eligibilityStatus": "ELIGIBLE",
  "notes": "Preparing documents for this post"
}
```

### Get User Applications

```http
GET /api/applications?status=ELIGIBLE
Authorization: Bearer {token}
```

**Response:**
```json
{
  "applications": [
    {
      "id": 1,
      "jobPostingId": 1,
      "notificationTitle": "SSC CGL 2026",
      "postName": "Income Tax Inspector",
      "postCode": "POST-001",
      "eligibilityStatus": "ELIGIBLE",
      "appliedAt": "2026-07-25T12:00:00Z",
      "notes": "Preparing documents for this post"
    }
  ]
}
```

## Error Responses

All endpoints return errors in this format:

```json
{
  "timestamp": "2026-07-25T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid date format for dateOfBirth",
  "path": "/api/profiles/me"
}
```

### Common HTTP Status Codes

- **200 OK** - Success
- **201 Created** - Resource created
- **400 Bad Request** - Invalid input
- **401 Unauthorized** - Missing or invalid token
- **403 Forbidden** - Insufficient permissions
- **404 Not Found** - Resource not found
- **500 Internal Server Error** - Server error
- **503 Service Unavailable** - Service down (check circuit breaker)

## Rate Limiting

Gateway applies rate limiting:
- **100 requests/second** per IP (replenish rate)
- **Burst capacity**: 200 requests

Exceeded limits return `429 Too Many Requests`.

## WebSocket (Future)

Real-time processing updates (planned for v0.2):

```javascript
const ws = new WebSocket('ws://localhost:8080/ws/processing/{jobPostingId}');

ws.onmessage = (event) => {
  const update = JSON.parse(event.data);
  console.log(update.stage, update.progress);
};
```

## Interactive API Documentation

- **RAG Service**: http://localhost:8000/docs (Swagger UI)
- **Core Service**: http://localhost:8081/swagger-ui.html (if enabled)

## SDKs and Examples

See `/examples` directory for:
- Python client examples
- JavaScript/React examples
- cURL command examples
- Postman collection
