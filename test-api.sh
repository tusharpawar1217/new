#!/bin/bash

# NotifyRAG API Test Script
# Tests the core endpoints after services are running

set -e

echo "======================================"
echo "  NotifyRAG API Automated Testing"
echo "======================================"
echo ""

# Configuration
GATEWAY_URL="http://localhost:8080"
RAG_URL="http://localhost:8000"
CORE_URL="http://localhost:8081"

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Test counter
PASSED=0
FAILED=0

# Helper function to test endpoint
test_endpoint() {
    local name=$1
    local method=$2
    local url=$3
    local data=$4
    local expected_status=$5
    
    echo -n "Testing: $name... "
    
    if [ -z "$data" ]; then
        response=$(curl -s -w "\n%{http_code}" -X $method "$url" -H "Content-Type: application/json" 2>&1)
    else
        response=$(curl -s -w "\n%{http_code}" -X $method "$url" -H "Content-Type: application/json" -d "$data" 2>&1)
    fi
    
    status=$(echo "$response" | tail -n1)
    body=$(echo "$response" | head -n-1)
    
    if [ "$status" == "$expected_status" ]; then
        echo -e "${GREEN}✓ PASSED${NC} (Status: $status)"
        ((PASSED++))
        return 0
    else
        echo -e "${RED}✗ FAILED${NC} (Expected: $expected_status, Got: $status)"
        echo "Response: $body"
        ((FAILED++))
        return 1
    fi
}

# Step 1: Check service health
echo "Step 1: Health Checks"
echo "---------------------"

test_endpoint "Gateway Health" "GET" "$GATEWAY_URL/actuator/health" "" "200"
test_endpoint "Core Service Health" "GET" "$CORE_URL/actuator/health" "" "200"
test_endpoint "RAG Service Health" "GET" "$RAG_URL/health" "" "200"

echo ""

# Step 2: Test authentication
echo "Step 2: Authentication"
echo "---------------------"

# Register new user
REGISTER_DATA='{
  "email": "test@notifyrag.com",
  "password": "Test123!@#",
  "fullName": "Test User"
}'

echo "Registering user..."
register_response=$(curl -s -w "\n%{http_code}" -X POST "$GATEWAY_URL/api/auth/register" \
    -H "Content-Type: application/json" \
    -d "$REGISTER_DATA" 2>&1)

register_status=$(echo "$register_response" | tail -n1)
register_body=$(echo "$register_response" | head -n-1)

if [ "$register_status" == "200" ] || [ "$register_status" == "201" ]; then
    echo -e "${GREEN}✓ User registered successfully${NC}"
    ((PASSED++))
else
    echo -e "${YELLOW}⚠ User might already exist (Status: $register_status)${NC}"
fi

# Login
LOGIN_DATA='{
  "email": "test@notifyrag.com",
  "password": "Test123!@#"
}'

echo "Logging in..."
login_response=$(curl -s -X POST "$GATEWAY_URL/api/auth/login" \
    -H "Content-Type: application/json" \
    -d "$LOGIN_DATA" 2>&1)

# Extract token
TOKEN=$(echo "$login_response" | grep -o '"token":"[^"]*' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    echo -e "${RED}✗ Failed to get authentication token${NC}"
    echo "Response: $login_response"
    ((FAILED++))
    exit 1
else
    echo -e "${GREEN}✓ Login successful, token received${NC}"
    ((PASSED++))
fi

echo ""

# Step 3: Test profile endpoints
echo "Step 3: Profile Management"
echo "-------------------------"

# Get profile (should be empty initially)
echo "Getting user profile..."
profile_response=$(curl -s -w "\n%{http_code}" -X GET "$GATEWAY_URL/api/profiles/me" \
    -H "Authorization: Bearer $TOKEN" 2>&1)

profile_status=$(echo "$profile_response" | tail -n1)

if [ "$profile_status" == "200" ] || [ "$profile_status" == "404" ]; then
    echo -e "${GREEN}✓ Profile endpoint accessible${NC}"
    ((PASSED++))
else
    echo -e "${RED}✗ Profile endpoint failed (Status: $profile_status)${NC}"
    ((FAILED++))
fi

# Update profile
PROFILE_DATA='{
  "dateOfBirth": "2001-07-03",
  "gender": "MALE",
  "category": "OBC",
  "isPwbd": false,
  "isExServiceman": false,
  "educationLevel": "Bachelor'\''s Degree",
  "educationSpecialization": "Commerce",
  "domicileState": "Maharashtra"
}'

echo "Updating profile..."
update_response=$(curl -s -w "\n%{http_code}" -X PUT "$GATEWAY_URL/api/profiles/me" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "$PROFILE_DATA" 2>&1)

update_status=$(echo "$update_response" | tail -n1)

if [ "$update_status" == "200" ]; then
    echo -e "${GREEN}✓ Profile updated successfully${NC}"
    ((PASSED++))
else
    echo -e "${YELLOW}⚠ Profile update (Status: $update_status)${NC}"
fi

echo ""

# Step 4: Test notification endpoints
echo "Step 4: Notification Management"
echo "------------------------------"

# List notifications
echo "Listing notifications..."
notifications_response=$(curl -s -w "\n%{http_code}" -X GET "$GATEWAY_URL/api/notifications" \
    -H "Authorization: Bearer $TOKEN" 2>&1)

notifications_status=$(echo "$notifications_response" | tail -n1)

if [ "$notifications_status" == "200" ]; then
    echo -e "${GREEN}✓ Notifications list retrieved${NC}"
    ((PASSED++))
else
    echo -e "${YELLOW}⚠ Notifications endpoint (Status: $notifications_status)${NC}"
fi

echo ""

# Step 5: Test RAG service endpoints
echo "Step 5: RAG Service"
echo "------------------"

# Test RAG health with more detail
rag_health=$(curl -s "$RAG_URL/health" 2>&1)
echo "RAG Service Health: $rag_health"

if echo "$rag_health" | grep -q "healthy"; then
    echo -e "${GREEN}✓ RAG service is healthy${NC}"
    ((PASSED++))
else
    echo -e "${RED}✗ RAG service health check failed${NC}"
    ((FAILED++))
fi

echo ""

# Summary
echo "======================================"
echo "  Test Summary"
echo "======================================"
echo -e "Total Tests: $((PASSED + FAILED))"
echo -e "${GREEN}Passed: $PASSED${NC}"
echo -e "${RED}Failed: $FAILED${NC}"
echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}All tests passed! ✓${NC}"
    exit 0
else
    echo -e "${RED}Some tests failed. Please check the logs.${NC}"
    exit 1
fi
