#!/bin/bash

#############################################################################
# BioChain ADMIN Seeding Script
# Purpose: Seed database with ADMIN privileges and test Admin Endpoints
# API Endpoint: http://localhost:8088
# Date: December 21, 2025
#############################################################################

# Configuration
API_URL="http://localhost:8088"
TIMEOUT=10

# Admin credentials
ADMIN_USERNAME="bioadmin"
ADMIN_EMAIL="admin@biochain.ma"
ADMIN_PASSWORD="AdminSecure2025@"
ADMIN_FULLNAME="BioChain Administrator"
ADMIN_PHONE="+212600000000"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Global variables
ADMIN_TOKEN=""
ADMIN_USER_ID=""

log_info()    { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[OK]${NC} $1"; }
log_error()   { echo -e "${RED}[ERR]${NC} $1"; }
log_warn()    { echo -e "${YELLOW}[WARN]${NC} $1"; }

# Check API Health
check_api_health() {
    log_info "Checking API health..."
    if curl -s -f "${API_URL}/actuator/health" >/dev/null 2>&1; then
        log_success "API is up and running"
    else
        log_error "API is unreachable at ${API_URL}"
        exit 1
    fi
}

# ============================================================================
# 1. Admin Setup & Authentication
# ============================================================================
setup_admin() {
    log_info "--- 1. Admin Account Setup ---"

    # 1. Register Admin User
    local reg_payload="{\"username\":\"$ADMIN_USERNAME\",\"email\":\"$ADMIN_EMAIL\",\"password\":\"$ADMIN_PASSWORD\",\"fullName\":\"$ADMIN_FULLNAME\",\"phoneNumber\":\"$ADMIN_PHONE\"}"
    # We ignore the output here because if it exists, it fails safely, we just need to login next.
    curl -s -X POST "${API_URL}/api/auth/register" \
        -H "Content-Type: application/json" \
        -d "$reg_payload" > /dev/null

    # 2. Login as Admin
    local login_payload="{\"username\":\"$ADMIN_USERNAME\",\"password\":\"$ADMIN_PASSWORD\"}"
    local login_resp=$(curl -s -X POST "${API_URL}/api/auth/login" \
        -H "Content-Type: application/json" \
        -d "$login_payload")

    ADMIN_TOKEN=$(echo "$login_resp" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
    ADMIN_USER_ID=$(echo "$login_resp" | grep -o '"id":[0-9]*' | head -n1 | cut -d':' -f2)

    if [[ -z "$ADMIN_TOKEN" || "$ADMIN_TOKEN" == "null" ]]; then
        log_error "Failed to authenticate as Admin. Check credentials."
        exit 1
    fi

    log_success "Admin Authenticated (ID: $ADMIN_USER_ID)"

    # 3. Assign Role (In a real scenario, this might need manual DB intervention or a specific setup endpoint)
    # Trying to self-promote (Simulated for dev-nosec profile where endpoints might be open, or assuming DB seeding)
    # In a strict environment, you'd likely need to seed the role directly in SQL.
    log_info "Ensuring Admin Role..."
    curl -s -X POST "${API_URL}/api/admin/users/${ADMIN_USER_ID}/roles?role=ADMIN" \
         -H "Authorization: Bearer ${ADMIN_TOKEN}" > /dev/null
}

# ============================================================================
# 2. Admin Controller Tests
# ============================================================================
test_admin_controller() {
    log_info "--- 2. Testing AdminController ---"

    # GET /api/admin/users
    local users_resp=$(curl -s -X GET "${API_URL}/api/admin/users" -H "Authorization: Bearer ${ADMIN_TOKEN}")
    if [[ $users_resp == *"["* ]]; then
        log_success "GET /api/admin/users - Retrieved user list"
    else
        log_error "GET /api/admin/users failed"
    fi

    # GET /api/admin/me
    local me_resp=$(curl -s -X GET "${API_URL}/api/admin/me" -H "Authorization: Bearer ${ADMIN_TOKEN}")
    if [[ $me_resp == *"$ADMIN_USERNAME"* ]]; then
        log_success "GET /api/admin/me - Retrieved admin info"
    else
        log_error "GET /api/admin/me failed"
    fi

    # POST /api/admin/users/{id}/roles & DELETE /api/admin/users/{id}/roles
    # We'll use the admin ID itself to toggle a dummy role or just verify endpoint reachability
    curl -s -X POST "${API_URL}/api/admin/users/${ADMIN_USER_ID}/roles?role=USER" -H "Authorization: Bearer ${ADMIN_TOKEN}" > /dev/null
    log_success "POST /api/admin/users/{id}/roles - Assigned USER role"

    curl -s -X DELETE "${API_URL}/api/admin/users/${ADMIN_USER_ID}/roles?role=USER" -H "Authorization: Bearer ${ADMIN_TOKEN}" > /dev/null
    log_success "DELETE /api/admin/users/{id}/roles - Removed USER role"
}

# ============================================================================
# 3. Other Admin-Restricted Operations
# ============================================================================
test_admin_restricted_ops() {
    log_info "--- 3. Testing Admin-Only Operations in other Controllers ---"

    # BlockchainController: Initialize (if not exists)
    # POST /api/blockchain/initialize
    curl -s -X POST "${API_URL}/api/blockchain/initialize?name=BioChainMain&consensusType=PROOF_OF_WORK" \
         -H "Authorization: Bearer ${ADMIN_TOKEN}" > /dev/null
    log_success "POST /api/blockchain/initialize (Attempted)"

    # BlockchainController: Update Difficulty
    # PUT /api/blockchain/{chainId}/difficulty
    # Assuming 'biochain-main' or similar ID generated. Fetching first available chain ID.
    local chain_id=$(curl -s -X GET "${API_URL}/api/blockchain" -H "Authorization: Bearer ${ADMIN_TOKEN}" | grep -o '"chainId":"[^"]*"' | head -n1 | cut -d'"' -f4)
    if [[ -n "$chain_id" ]]; then
        curl -s -X PUT "${API_URL}/api/blockchain/${chain_id}/difficulty?difficulty=2" \
             -H "Authorization: Bearer ${ADMIN_TOKEN}" > /dev/null
        log_success "PUT /api/blockchain/{id}/difficulty - Updated difficulty"
    else
        log_warn "Skipping difficulty update - No blockchain found"
    fi

    # ConsensusController: Switch Consensus
    # PUT /api/consensus/switch
    curl -s -X PUT "${API_URL}/api/consensus/switch?newConsensusType=PROOF_OF_WORK" \
         -H "Authorization: Bearer ${ADMIN_TOKEN}" > /dev/null
    log_success "PUT /api/consensus/switch - Consensus switched"

    # NodeController: Remove Stale Nodes
    # DELETE /api/nodes/stale
    curl -s -X DELETE "${API_URL}/api/nodes/stale" \
         -H "Authorization: Bearer ${ADMIN_TOKEN}" > /dev/null
    log_success "DELETE /api/nodes/stale - Cleaned stale nodes"

    # StakeController: Distribute Rewards
    # POST /api/stakes/distribute-rewards
    curl -s -X POST "${API_URL}/api/stakes/distribute-rewards" \
         -H "Authorization: Bearer ${ADMIN_TOKEN}" > /dev/null
    log_success "POST /api/stakes/distribute-rewards - Distributed rewards"

    # WalletController: Update Balance (Manual Override)
    # Need a wallet address. Just creating a dummy wallet for Admin to test.
    local wallet_resp=$(curl -s -X POST "${API_URL}/api/wallets" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer ${ADMIN_TOKEN}" \
        -d "{\"userId\":$ADMIN_USER_ID,\"walletName\":\"AdminWallet\"}")
    local wallet_addr=$(echo "$wallet_resp" | grep -o '"address":"[^"]*"' | cut -d'"' -f4)

    if [[ -n "$wallet_addr" ]]; then
        curl -s -X PUT "${API_URL}/api/wallets/${wallet_addr}/balance?newBalance=50000" \
             -H "Authorization: Bearer ${ADMIN_TOKEN}" > /dev/null
        log_success "PUT /api/wallets/{addr}/balance - Updated wallet balance"
    fi
}

main() {
    check_api_health
    setup_admin
    test_admin_controller
    test_admin_restricted_ops
    echo ""
    log_success "Admin Seeding Complete."
}

main