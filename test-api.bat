@echo off
REM NotifyRAG API Test Script for Windows
REM Tests the core endpoints after services are running

echo ======================================
echo   NotifyRAG API Automated Testing
echo ======================================
echo.

set GATEWAY_URL=http://localhost:8080
set RAG_URL=http://localhost:8000
set CORE_URL=http://localhost:8081

set PASSED=0
set FAILED=0

echo Step 1: Health Checks
echo ---------------------

REM Check Gateway Health
curl -s %GATEWAY_URL%/actuator/health > nul 2>&1
if %errorlevel%==0 (
    echo [PASS] Gateway Health
    set /a PASSED+=1
) else (
    echo [FAIL] Gateway Health
    set /a FAILED+=1
)

REM Check Core Service Health
curl -s %CORE_URL%/actuator/health > nul 2>&1
if %errorlevel%==0 (
    echo [PASS] Core Service Health
    set /a PASSED+=1
) else (
    echo [FAIL] Core Service Health
    set /a FAILED+=1
)

REM Check RAG Service Health
curl -s %RAG_URL%/health > nul 2>&1
if %errorlevel%==0 (
    echo [PASS] RAG Service Health
    set /a PASSED+=1
) else (
    echo [FAIL] RAG Service Health
    set /a FAILED+=1
)

echo.
echo Step 2: Authentication
echo ---------------------

REM Register user
echo Registering test user...
curl -s -X POST %GATEWAY_URL%/api/auth/register ^
    -H "Content-Type: application/json" ^
    -d "{\"email\":\"test@notifyrag.com\",\"password\":\"Test123!@#\",\"fullName\":\"Test User\"}" ^
    > register_response.txt 2>&1

if %errorlevel%==0 (
    echo [PASS] User registration endpoint accessible
    set /a PASSED+=1
) else (
    echo [WARN] User registration
)

REM Login
echo Logging in...
curl -s -X POST %GATEWAY_URL%/api/auth/login ^
    -H "Content-Type: application/json" ^
    -d "{\"email\":\"test@notifyrag.com\",\"password\":\"Test123!@#\"}" ^
    > login_response.txt 2>&1

if %errorlevel%==0 (
    echo [PASS] Login endpoint accessible
    set /a PASSED+=1
) else (
    echo [FAIL] Login failed
    set /a FAILED+=1
)

echo.
echo ======================================
echo   Test Summary
echo ======================================
echo Total Passed: %PASSED%
echo Total Failed: %FAILED%
echo.

if %FAILED%==0 (
    echo All tests passed!
) else (
    echo Some tests failed. Check service logs.
)

REM Cleanup
del register_response.txt login_response.txt 2> nul

pause
