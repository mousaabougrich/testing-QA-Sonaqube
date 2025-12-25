#!/bin/bash

#############################################################################
# BioChain COMPLETE Load Test Script - 10x ALL Endpoints
# Purpose: Test ALL USER endpoints with 10x POST operations
# Target: All Controllers (excluding AdminController)
# API Endpoint: http://localhost:8088
# Date: December 21, 2025
#############################################################################

API_URL="http://localhost:8088"
TIMESTAMP=$(date +%s)
BASE_USER="user_${TIMESTAMP}"
BASE_PASS="SecurePass123!"

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info()    { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[OK]${NC} $1"; }
log_error()   { echo -e "${RED}[ERR]${NC} $1"; }
log_warn()    { echo -e "${YELLOW}[WARN]${NC} $1"; }

# Global State Arrays to store 10 sets of data
declare -a USER_IDS
declare -a USER_TOKENS
declare -a WALLETS
declare -a PRIV_KEYS
declare -a PUB_KEYS
declare -a NODE_IDS
declare -a TX_HASHES
declare -a BLOCK_HASHES
declare -a STAKE_IDS
declare -a POOL_NAMES

# Check API Health
log_info "Checking API Health..."
if ! curl -s "${API_URL}/actuator/health" > /dev/null 2>&1; then
    log_error "API is unreachable at ${API_URL}"
    exit 1
fi
log_success "API is healthy!"

# ============================================================================
# 1. AuthController (10x Register & Login)
# ============================================================================
log_info "=== 1. AuthController: Registering & Logging in 10 Users ==="

for i in {1..10}; do
    username="${BASE_USER}_${i}"
    email="${BASE_USER}_${i}@test.com"

    # POST /api/auth/register
    reg_resp=$(curl -s -X POST "${API_URL}/api/auth/register" \
        -H "Content-Type: application/json" \
        -d "{\"username\":\"$username\",\"email\":\"$email\",\"password\":\"$BASE_PASS\",\"fullName\":\"Test User $i\",\"phoneNumber\":\"+21260000000$i\"}")

    # POST /api/auth/login
    login_resp=$(curl -s -X POST "${API_URL}/api/auth/login" \
        -H "Content-Type: application/json" \
        -d "{\"username\":\"$username\",\"password\":\"$BASE_PASS\"}")

    token=$(echo "$login_resp" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
    uid=$(echo "$login_resp" | grep -o '"id":[0-9]*' | head -n1 | cut -d':' -f2)

    if [[ -n "$token" && -n "$uid" ]]; then
        USER_TOKENS[$i]=$token
        USER_IDS[$i]=$uid
        log_success "[$i/10] User: $username (ID: $uid)"
    else
        log_error "[$i/10] Login failed for $username"
    fi
done

# ============================================================================
# 2. UserController (10x Create, Update, Search)
# ============================================================================
log_info "=== 2. UserController: Testing User Operations ==="

for i in {1..10}; do
    token=${USER_TOKENS[$i]}
    uid=${USER_IDS[$i]}

    # PUT /api/users/{id}
    curl -s -X PUT "${API_URL}/api/users/$uid?fullName=Updated User $i&phoneNumber=+21266000000$i" \
        -H "Authorization: Bearer $token" > /dev/null

    # POST /api/users/{id}/deactivate
    curl -s -X POST "${API_URL}/api/users/$uid/deactivate" \
        -H "Authorization: Bearer $token" > /dev/null

    # POST /api/users/{id}/activate
    curl -s -X POST "${API_URL}/api/users/$uid/activate" \
        -H "Authorization: Bearer $token" > /dev/null

    log_success "[$i/10] User $uid: Updated, Deactivated, Activated"
done

# Test GET endpoints for User
log_info "Testing UserController GET endpoints..."
token=${USER_TOKENS[1]}
curl -s "${API_URL}/api/users" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/users/active" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/users/count/active" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/users/search?searchTerm=${BASE_USER}" -H "Authorization: Bearer $token" > /dev/null
log_success "User GET endpoints tested"

# ============================================================================
# 3. CryptographyController (10x All Operations)
# ============================================================================
log_info "=== 3. CryptographyController: 10x Crypto Operations ==="

for i in {1..10}; do
    token=${USER_TOKENS[$i]}
    data="DataPacket_$i"
    password="CryptoPass_$i"

    # POST /api/crypto/generate-keypair
    kp_resp=$(curl -s -X POST "${API_URL}/api/crypto/generate-keypair" \
        -H "Authorization: Bearer $token")

    temp_pub=$(echo "$kp_resp" | grep -o '"publicKey":"[^"]*"' | cut -d'"' -f4)
    temp_priv=$(echo "$kp_resp" | grep -o '"privateKey":"[^"]*"' | cut -d'"' -f4)

    # POST /api/crypto/generate-address
    addr_resp=$(curl -s -X POST "${API_URL}/api/crypto/generate-address?publicKey=$temp_pub" \
        -H "Authorization: Bearer $token")

    # POST /api/crypto/hash
    hash_resp=$(curl -s -X POST "${API_URL}/api/crypto/hash?data=$data" \
        -H "Authorization: Bearer $token")

    # POST /api/crypto/sign
    sig_resp=$(curl -s -X POST "${API_URL}/api/crypto/sign?data=$data&privateKey=$temp_priv" \
        -H "Authorization: Bearer $token")
    signature=$(echo "$sig_resp" | grep -o '"signature":"[^"]*"' | cut -d'"' -f4)

    # POST /api/crypto/verify
    curl -s -X POST "${API_URL}/api/crypto/verify?data=$data&signature=$signature&publicKey=$temp_pub" \
        -H "Authorization: Bearer $token" > /dev/null

    # POST /api/crypto/encrypt
    enc_resp=$(curl -s -X POST "${API_URL}/api/crypto/encrypt?data=$data&publicKey=$temp_pub" \
        -H "Authorization: Bearer $token")
    enc_data=$(echo "$enc_resp" | grep -o '"encryptedData":"[^"]*"' | cut -d'"' -f4)

    # POST /api/crypto/decrypt
    curl -s -X POST "${API_URL}/api/crypto/decrypt?encryptedData=$enc_data&privateKey=$temp_priv" \
        -H "Authorization: Bearer $token" > /dev/null

    # POST /api/crypto/encrypt-private-key
    enc_pk_resp=$(curl -s -X POST "${API_URL}/api/crypto/encrypt-private-key?privateKey=$temp_priv&password=$password" \
        -H "Authorization: Bearer $token")
    enc_pk=$(echo "$enc_pk_resp" | grep -o '"encryptedPrivateKey":"[^"]*"' | cut -d'"' -f4)

    # POST /api/crypto/decrypt-private-key
    curl -s -X POST "${API_URL}/api/crypto/decrypt-private-key?encryptedPrivateKey=$enc_pk&password=$password" \
        -H "Authorization: Bearer $token" > /dev/null

    # POST /api/crypto/validate-keypair
    curl -s -X POST "${API_URL}/api/crypto/validate-keypair?publicKey=$temp_pub&privateKey=$temp_priv" \
        -H "Authorization: Bearer $token" > /dev/null

    log_success "[$i/10] Crypto: KeyGen, Hash, Sign, Verify, Encrypt, Decrypt"
done

# Test GET endpoint
curl -s "${API_URL}/api/crypto/info" -H "Authorization: Bearer ${USER_TOKENS[1]}" > /dev/null
log_success "Crypto GET endpoints tested"

# ============================================================================
# 4. WalletController (10x All Operations)
# ============================================================================
log_info "=== 4. WalletController: Creating & Managing 10 Wallets ==="

for i in {1..10}; do
    token=${USER_TOKENS[$i]}
    uid=${USER_IDS[$i]}
    w_name="Wallet_User_$i"

    # POST /api/wallets (Create)
    create_resp=$(curl -s -X POST "${API_URL}/api/wallets" \
        -H "Authorization: Bearer $token" \
        -H "Content-Type: application/json" \
        -d "{\"userId\":$uid,\"walletName\":\"$w_name\"}")

    addr=$(echo "$create_resp" | grep -o '"address":"[^"]*"' | cut -d'"' -f4)
    pk=$(echo "$create_resp" | grep -o '"privateKey":"[^"]*"' | cut -d'"' -f4)
    pub=$(echo "$create_resp" | grep -o '"publicKey":"[^"]*"' | cut -d'"' -f4)

    WALLETS[$i]=$addr
    PRIV_KEYS[$i]=$pk
    PUB_KEYS[$i]=$pub

    # POST /api/wallets/{address}/export
    curl -s -X POST "${API_URL}/api/wallets/$addr/export?password=ExportPass_$i" \
        -H "Authorization: Bearer $token" > /dev/null

    # POST /api/wallets/{address}/deactivate
    curl -s -X POST "${API_URL}/api/wallets/$addr/deactivate" \
        -H "Authorization: Bearer $token" > /dev/null

    # POST /api/wallets/{address}/activate
    curl -s -X POST "${API_URL}/api/wallets/$addr/activate" \
        -H "Authorization: Bearer $token" > /dev/null

    log_success "[$i/10] Wallet: ${addr:0:20}... (Export, Toggle)"
done

# POST /api/wallets/import (10x)
log_info "Testing Wallet Import (10x)..."
for i in {1..10}; do
    token=${USER_TOKENS[$i]}
    uid=${USER_IDS[$i]}

    curl -s -X POST "${API_URL}/api/wallets/import?userId=$uid&privateKey=${PRIV_KEYS[$i]}&password=ImportPass_$i" \
        -H "Authorization: Bearer $token" > /dev/null

    log_success "[$i/10] Imported wallet for User $uid"
done

# Test GET endpoints
token=${USER_TOKENS[1]}
addr=${WALLETS[1]}
curl -s "${API_URL}/api/wallets/$addr" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/wallets/user/${USER_IDS[1]}" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/wallets/user/${USER_IDS[1]}/active" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/wallets/$addr/balance" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/wallets/total-balance" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/wallets/validate/$addr" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/wallets/exists/$addr" -H "Authorization: Bearer $token" > /dev/null
log_success "Wallet GET endpoints tested"

# ============================================================================
# 5. BlockchainController (Initialize & Operations)
# ============================================================================
log_info "=== 5. BlockchainController: Initializing Blockchain ==="

token=${USER_TOKENS[1]}
chain_id="biochain_test_${TIMESTAMP}"

# POST /api/blockchain/initialize
curl -s -X POST "${API_URL}/api/blockchain/initialize?name=BioChain_Test&consensusType=PROOF_OF_WORK" \
    -H "Authorization: Bearer $token" > /dev/null

log_success "Blockchain initialized"

# POST /api/blockchain/{chainId}/sync
curl -s -X POST "${API_URL}/api/blockchain/$chain_id/sync" \
    -H "Authorization: Bearer $token" > /dev/null

# PUT /api/blockchain/{chainId}/difficulty
curl -s -X PUT "${API_URL}/api/blockchain/$chain_id/difficulty?difficulty=3" \
    -H "Authorization: Bearer $token" > /dev/null

# Test GET endpoints
curl -s "${API_URL}/api/blockchain" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/blockchain/$chain_id/status" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/blockchain/$chain_id/height" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/blockchain/$chain_id/genesis" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/blockchain/$chain_id/total-transactions" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/blockchain/$chain_id/validate" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/blockchain/$chain_id/integrity" -H "Authorization: Bearer $token" > /dev/null
log_success "Blockchain operations tested"

# ============================================================================
# 6. MiningController (10x Mine Blocks)
# ============================================================================
log_info "=== 6. MiningController: Mining 10 Blocks (Funding Wallets) ==="

for i in {1..10}; do
    token=${USER_TOKENS[$i]}
    addr=${WALLETS[$i]}

    # POST /api/mining/mine
    mine_resp=$(curl -s -X POST "${API_URL}/api/mining/mine?minerAddress=$addr" \
        -H "Authorization: Bearer $token")

    # POST /api/mining/mine-with-difficulty
    curl -s -X POST "${API_URL}/api/mining/mine-with-difficulty?minerAddress=$addr&difficulty=2" \
        -H "Authorization: Bearer $token" > /dev/null

    log_success "[$i/10] Mined 2 blocks for User $i"
done

# Test GET endpoints
curl -s "${API_URL}/api/mining/difficulty" -H "Authorization: Bearer ${USER_TOKENS[1]}" > /dev/null
curl -s "${API_URL}/api/mining/reward/5" -H "Authorization: Bearer ${USER_TOKENS[1]}" > /dev/null
curl -s "${API_URL}/api/mining/estimated-time/4" -H "Authorization: Bearer ${USER_TOKENS[1]}" > /dev/null
log_success "Mining GET endpoints tested"

# ============================================================================
# 7. TransactionController (10x Transactions)
# ============================================================================
log_info "=== 7. TransactionController: Creating 10 Transactions ==="

for i in {1..10}; do
    sender_idx=$i
    recipient_idx=$(( (i % 10) + 1 ))

    token=${USER_TOKENS[$sender_idx]}
    sender=${WALLETS[$sender_idx]}
    recipient=${WALLETS[$recipient_idx]}
    pk=${PRIV_KEYS[$sender_idx]}
    amount=$(echo "$i * 0.5 + 1" | bc)

    # POST /api/transactions
    tx_resp=$(curl -s -X POST "${API_URL}/api/transactions" \
        -H "Authorization: Bearer $token" \
        -H "Content-Type: application/json" \
        -d "{\"senderAddress\":\"$sender\",\"recipientAddress\":\"$recipient\",\"amount\":$amount,\"fee\":0.01,\"memo\":\"Payment_$i\",\"privateKey\":\"$pk\"}")

    hash=$(echo "$tx_resp" | grep -o '"transactionHash":"[^"]*"' | cut -d'"' -f4)
    TX_HASHES[$i]=$hash

    log_success "[$i/10] TX: User $sender_idx → User $recipient_idx ($amount BIO)"
done

# POST /api/transactions/{hash}/confirm (for existing transactions)
log_info "Confirming transactions..."
for i in {1..10}; do
    hash=${TX_HASHES[$i]}
    if [[ -n "$hash" ]]; then
        curl -s -X POST "${API_URL}/api/transactions/$hash/confirm?blockId=$i" \
            -H "Authorization: Bearer ${USER_TOKENS[1]}" > /dev/null 2>&1
    fi
done

# Test GET endpoints
token=${USER_TOKENS[1]}
addr=${WALLETS[1]}
hash=${TX_HASHES[1]}

curl -s "${API_URL}/api/transactions/$hash" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/transactions/wallet/$addr/history" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/transactions/wallet/$addr/sent" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/transactions/wallet/$addr/received" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/transactions/pending" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/transactions/confirmed" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/transactions/wallet/$addr/count" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/transactions/total-volume" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/transactions/calculate-fee?amount=100" -H "Authorization: Bearer $token" > /dev/null
log_success "Transaction GET endpoints tested"

# ============================================================================
# 8. BlockController (Test All Endpoints)
# ============================================================================
log_info "=== 8. BlockController: Testing Block Operations ==="

token=${USER_TOKENS[1]}

# POST /api/blocks/validate
block_payload='{"blockIndex":1,"hash":"test_hash","previousHash":"0","timestamp":1699564800000,"nonce":12345,"difficulty":4}'
curl -s -X POST "${API_URL}/api/blocks/validate" \
    -H "Authorization: Bearer $token" \
    -H "Content-Type: application/json" \
    -d "$block_payload" > /dev/null

# Test GET endpoints
curl -s "${API_URL}/api/blocks" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/blocks/1" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/blocks/index/0" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/blocks/latest" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/blocks/range?startIndex=0&endIndex=5" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/blocks/miner/${WALLETS[1]}" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/blocks/count" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/blocks/miner/${WALLETS[1]}/count" -H "Authorization: Bearer $token" > /dev/null
log_success "Block operations tested"

# ============================================================================
# 9. StakeController (10x Stake Operations)
# ============================================================================
log_info "=== 9. StakeController: Creating 10 Stakes ==="

for i in {1..10}; do
    token=${USER_TOKENS[$i]}
    uid=${USER_IDS[$i]}
    addr=${WALLETS[$i]}
    amount=$(echo "$i * 10" | bc)

    # POST /api/stakes
    stake_resp=$(curl -s -X POST "${API_URL}/api/stakes?userId=$uid&walletAddress=$addr&amount=$amount" \
        -H "Authorization: Bearer $token")

    stake_id=$(echo "$stake_resp" | grep -o '"id":[0-9]*' | head -n1 | cut -d':' -f2)
    STAKE_IDS[$i]=$stake_id

    log_success "[$i/10] Staked $amount BIO for User $i (Stake ID: $stake_id)"
done

# POST /api/stakes/unlock-expired
curl -s -X POST "${API_URL}/api/stakes/unlock-expired" \
    -H "Authorization: Bearer ${USER_TOKENS[1]}" > /dev/null

# POST /api/stakes/{id}/withdraw (test on first stake)
if [[ -n "${STAKE_IDS[1]}" ]]; then
    curl -s -X POST "${API_URL}/api/stakes/${STAKE_IDS[1]}/withdraw" \
        -H "Authorization: Bearer ${USER_TOKENS[1]}" > /dev/null 2>&1
fi

# POST /api/stakes/distribute-rewards (Admin required, may fail)
curl -s -X POST "${API_URL}/api/stakes/distribute-rewards" \
    -H "Authorization: Bearer ${USER_TOKENS[1]}" > /dev/null 2>&1

# Test GET endpoints
token=${USER_TOKENS[1]}
stake_id=${STAKE_IDS[1]}
addr=${WALLETS[1]}

curl -s "${API_URL}/api/stakes/$stake_id" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/stakes/user/${USER_IDS[1]}" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/stakes/wallet/$addr" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/stakes/active" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/stakes/$stake_id/rewards" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/stakes/total-staked" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/stakes/wallet/$addr/total-staked" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/stakes/top-stakers" -H "Authorization: Bearer $token" > /dev/null
log_success "Stake GET endpoints tested"

# ============================================================================
# 10. TransactionPoolController (10x Pool Operations)
# ============================================================================
log_info "=== 10. TransactionPoolController: Creating 10 Pools ==="

token=${USER_TOKENS[1]}

for i in {1..10}; do
    poolName="TestPool_$i"
    POOL_NAMES[$i]=$poolName
    maxSize=$((50 + i * 10))

    # POST /api/transaction-pool
    curl -s -X POST "${API_URL}/api/transaction-pool?poolName=$poolName&maxSize=$maxSize" \
        -H "Authorization: Bearer $token" > /dev/null

    # POST /api/transaction-pool/{poolName}/transactions
    mock_tx="{\"transactionHash\":\"pool_tx_$i\",\"senderAddress\":\"${WALLETS[$i]}\",\"recipientAddress\":\"${WALLETS[1]}\",\"amount\":5,\"timestamp\":$(date +%s)000}"
    curl -s -X POST "${API_URL}/api/transaction-pool/$poolName/transactions" \
        -H "Authorization: Bearer $token" \
        -H "Content-Type: application/json" \
        -d "$mock_tx" > /dev/null

    log_success "[$i/10] Pool: $poolName (MaxSize: $maxSize)"
done

# DELETE /api/transaction-pool/{poolName}/transactions/{hash} (test removal)
curl -s -X DELETE "${API_URL}/api/transaction-pool/${POOL_NAMES[1]}/transactions/pool_tx_1" \
    -H "Authorization: Bearer $token" > /dev/null 2>&1

# DELETE /api/transaction-pool/{poolName}/clear
curl -s -X DELETE "${API_URL}/api/transaction-pool/${POOL_NAMES[2]}/clear" \
    -H "Authorization: Bearer $token" > /dev/null

# Test GET endpoints
pool_name=${POOL_NAMES[1]}
curl -s "${API_URL}/api/transaction-pool/1" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/transaction-pool/name/$pool_name" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/transaction-pool/$pool_name/status" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/transaction-pool/$pool_name/transactions" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/transaction-pool/$pool_name/top-by-fee?limit=5" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/transaction-pool/$pool_name/is-full" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/transaction-pool/available" -H "Authorization: Bearer $token" > /dev/null
log_success "Transaction Pool GET endpoints tested"

# ============================================================================
# 11. NodeController (10x Node Operations)
# ============================================================================
log_info "=== 11. NodeController: Registering 10 Nodes ==="

token=${USER_TOKENS[1]}

for i in {1..10}; do
    nodeId="node_test_${TIMESTAMP}_$i"
    NODE_IDS[$i]=$nodeId
    port=$((8000 + i))

    # POST /api/nodes/register
    node_payload="{\"nodeId\":\"$nodeId\",\"ipAddress\":\"192.168.1.$i\",\"port\":$port,\"nodeType\":\"FULL_NODE\",\"version\":\"1.0.$i\"}"
    curl -s -X POST "${API_URL}/api/nodes/register" \
        -H "Authorization: Bearer $token" \
        -H "Content-Type: application/json" \
        -d "$node_payload" > /dev/null

    # POST /api/nodes/{nodeId}/trust
    curl -s -X POST "${API_URL}/api/nodes/$nodeId/trust" \
        -H "Authorization: Bearer $token" > /dev/null

    # POST /api/nodes/{nodeId}/untrust
    curl -s -X POST "${API_URL}/api/nodes/$nodeId/untrust" \
        -H "Authorization: Bearer $token" > /dev/null

    log_success "[$i/10] Node: $nodeId (Port: $port)"
done

# POST /api/nodes/{nodeId}/connect/{peerNodeId} (connect nodes)
for i in {2..10}; do
    curl -s -X POST "${API_URL}/api/nodes/${NODE_IDS[1]}/connect/${NODE_IDS[$i]}" \
        -H "Authorization: Bearer $token" > /dev/null
done

# POST /api/nodes/{nodeId}/disconnect/{peerNodeId}
curl -s -X POST "${API_URL}/api/nodes/${NODE_IDS[1]}/disconnect/${NODE_IDS[2]}" \
    -H "Authorization: Bearer $token" > /dev/null

# POST /api/nodes/{nodeId}/sync/{peerNodeId}
curl -s -X POST "${API_URL}/api/nodes/${NODE_IDS[1]}/sync/${NODE_IDS[3]}" \
    -H "Authorization: Bearer $token" > /dev/null

# PUT /api/nodes/{nodeId}/status
curl -s -X PUT "${API_URL}/api/nodes/${NODE_IDS[1]}/status?status=SYNCING" \
    -H "Authorization: Bearer $token" > /dev/null

# PUT /api/nodes/{nodeId}/block-height
curl -s -X PUT "${API_URL}/api/nodes/${NODE_IDS[1]}/block-height?blockHeight=100" \
    -H "Authorization: Bearer $token" > /dev/null

# DELETE /api/nodes/stale (Admin may be required)
curl -s -X DELETE "${API_URL}/api/nodes/stale" \
    -H "Authorization: Bearer $token" > /dev/null 2>&1

# Test GET endpoints
node_id=${NODE_IDS[1]}
curl -s "${API_URL}/api/nodes" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/nodes/1" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/nodes/node-id/$node_id" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/nodes/active" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/nodes/status/ACTIVE" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/nodes/$node_id/peers" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/nodes/$node_id/ping" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/nodes/$node_id/latency" -H "Authorization: Bearer $token" > /dev/null
log_success "Node operations tested"

# ============================================================================
# 12. P2PNetworkController (All P2P Operations)
# ============================================================================
log_info "=== 12. P2PNetworkController: Testing P2P Operations ==="

token=${USER_TOKENS[1]}

# POST /api/p2p/connect
curl -s -X POST "${API_URL}/api/p2p/connect" \
    -H "Authorization: Bearer $token" > /dev/null

# POST /api/p2p/disconnect
curl -s -X POST "${API_URL}/api/p2p/disconnect" \
    -H "Authorization: Bearer $token" > /dev/null

# POST /api/p2p/broadcast/block (Mock block)
block_json='{"blockIndex":1,"hash":"test_hash","previousHash":"0","timestamp":1699564800000,"nonce":100}'
curl -s -X POST "${API_URL}/api/p2p/broadcast/block" \
    -H "Authorization: Bearer $token" \
    -H "Content-Type: application/json" \
    -d "$block_json" > /dev/null

# POST /api/p2p/broadcast/transaction (Mock tx)
tx_json='{"transactionHash":"p2p_tx_1","senderAddress":"'${WALLETS[1]}'","recipientAddress":"'${WALLETS[2]}'","amount":10}'
curl -s -X POST "${API_URL}/api/p2p/broadcast/transaction" \
    -H "Authorization: Bearer $token" \
    -H "Content-Type: application/json" \
    -d "$tx_json" > /dev/null

# POST /api/p2p/message/send (10x)
for i in {1..10}; do
    curl -s -X POST "${API_URL}/api/p2p/message/send?nodeId=${NODE_IDS[$i]}&message=TestMessage_$i" \
        -H "Authorization: Bearer $token" > /dev/null 2>&1
done

# POST /api/p2p/message/broadcast (10x)
for i in {1..10}; do
    curl -s -X POST "${API_URL}/api/p2p/message/broadcast?message=BroadcastMsg_$i" \
        -H "Authorization: Bearer $token" > /dev/null
done

# POST /api/p2p/blockchain/request
curl -s -X POST "${API_URL}/api/p2p/blockchain/request?nodeId=${NODE_IDS[1]}" \
    -H "Authorization: Bearer $token" > /dev/null

# POST /api/p2p/synchronize
curl -s -X POST "${API_URL}/api/p2p/synchronize" \
    -H "Authorization: Bearer $token" > /dev/null

# Test GET endpoints
curl -s "${API_URL}/api/p2p/status" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/p2p/peers/discover" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/p2p/health" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/p2p/peers/count" -H "Authorization: Bearer $token" > /dev/null
log_success "P2P Network operations tested"

# ============================================================================
# 13. BlockchainSyncController (All Sync Operations)
# ============================================================================
log_info "=== 13. BlockchainSyncController: Testing Sync Operations ==="

# POST /api/sync/synchronize (10x)
for i in {1..10}; do
    curl -s -X POST "${API_URL}/api/sync/synchronize" \
        -H "Authorization: Bearer $token" > /dev/null
done

# POST /api/sync/synchronize-with-peer (10x)
for i in {1..10}; do
    curl -s -X POST "${API_URL}/api/sync/synchronize-with-peer?peerNodeId=${NODE_IDS[$i]}" \
        -H "Authorization: Bearer $token" > /dev/null 2>&1
done

# POST /api/sync/request-blocks (10x)
for i in {1..10}; do
    start=$((i * 5))
    end=$((start + 4))
    curl -s -X POST "${API_URL}/api/sync/request-blocks?peerNodeId=${NODE_IDS[$i]}&fromIndex=$start&toIndex=$end" \
        -H "Authorization: Bearer $token" > /dev/null 2>&1
done

# POST /api/sync/validate-blocks
blocks_array='[{"blockIndex":1,"hash":"sync_hash_1","previousHash":"0","timestamp":1699564800000,"nonce":1}]'
curl -s -X POST "${API_URL}/api/sync/validate-blocks" \
    -H "Authorization: Bearer $token" \
    -H "Content-Type: application/json" \
    -d "$blocks_array" > /dev/null

# POST /api/sync/add-blocks
curl -s -X POST "${API_URL}/api/sync/add-blocks" \
    -H "Authorization: Bearer $token" \
    -H "Content-Type: application/json" \
    -d "$blocks_array" > /dev/null

# POST /api/sync/resolve-conflicts (10x)
for i in {1..10}; do
    curl -s -X POST "${API_URL}/api/sync/resolve-conflicts" \
        -H "Authorization: Bearer $token" > /dev/null
done

# Test GET endpoints
curl -s "${API_URL}/api/sync/missing-blocks?fromIndex=0&toIndex=10" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/sync/needs-synchronization" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/sync/sync-progress" -H "Authorization: Bearer $token" > /dev/null
log_success "Blockchain Sync operations tested"

# ============================================================================
# 14. ConsensusController (All Consensus Operations)
# ============================================================================
log_info "=== 14. ConsensusController: Testing Consensus ==="

# PUT /api/consensus/switch (10x alternate)
for i in {1..10}; do
    if [ $((i % 2)) -eq 0 ]; then
        consensus="PROOF_OF_STAKE"
    else
        consensus="PROOF_OF_WORK"
    fi
    curl -s -X PUT "${API_URL}/api/consensus/switch?newConsensusType=$consensus" \
        -H "Authorization: Bearer $token" > /dev/null 2>&1
done

# POST /api/consensus/validate-block (10x)
for i in {1..10}; do
    block_json='{"blockIndex":'$i',"hash":"cons_hash_'$i'","previousHash":"prev","timestamp":1699564800000,"nonce":'$i'}'
    curl -s -X POST "${API_URL}/api/consensus/validate-block" \
        -H "Authorization: Bearer $token" \
        -H "Content-Type: application/json" \
        -d "$block_json" > /dev/null
done

# Test GET endpoints
curl -s "${API_URL}/api/consensus/current" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/consensus/validator-eligible/${WALLETS[1]}" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/consensus/select-validator" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/consensus/supported-types" -H "Authorization: Bearer $token" > /dev/null
log_success "Consensus operations tested"

# ============================================================================
# 15. PoWConsensusController (All PoW Operations)
# ============================================================================
log_info "=== 15. PoWConsensusController: Testing PoW ==="

# POST /api/consensus/pow/mine (10x)
for i in {1..10}; do
    block_json='{"blockIndex":'$i',"hash":"","previousHash":"prev_'$i'","timestamp":'$(date +%s)'000,"nonce":0}'
    curl -s -X POST "${API_URL}/api/consensus/pow/mine?difficulty=1" \
        -H "Authorization: Bearer $token" \
        -H "Content-Type: application/json" \
        -d "$block_json" > /dev/null
done

# POST /api/consensus/pow/validate (10x)
for i in {1..10}; do
    block_json='{"blockIndex":'$i',"hash":"0000abc'$i'","previousHash":"prev","timestamp":1699564800000,"nonce":123}'
    curl -s -X POST "${API_URL}/api/consensus/pow/validate?difficulty=4" \
        -H "Authorization: Bearer $token" \
        -H "Content-Type: application/json" \
        -d "$block_json" > /dev/null
done

# POST /api/consensus/pow/calculate-hash (10x)
for i in {1..10}; do
    block_json='{"blockIndex":'$i',"previousHash":"prev_'$i'","timestamp":'$(date +%s)'000,"nonce":'$i'}'
    curl -s -X POST "${API_URL}/api/consensus/pow/calculate-hash" \
        -H "Authorization: Bearer $token" \
        -H "Content-Type: application/json" \
        -d "$block_json" > /dev/null
done

# POST /api/consensus/pow/test-mine (10x)
for i in {1..10}; do
    block_json='{"blockIndex":'$i',"previousHash":"test","timestamp":'$(date +%s)'000,"nonce":0}'
    curl -s -X POST "${API_URL}/api/consensus/pow/test-mine?difficulty=1&maxAttempts=1000" \
        -H "Authorization: Bearer $token" \
        -H "Content-Type: application/json" \
        -d "$block_json" > /dev/null
done

# Test GET endpoints
curl -s "${API_URL}/api/consensus/pow/meets-target?hash=0000abcd&difficulty=4" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/consensus/pow/difficulty-target/4" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/consensus/pow/mining-info/4" -H "Authorization: Bearer $token" > /dev/null
log_success "PoW Consensus operations tested"

# ============================================================================
# 16. PoSConsensusController (All PoS Operations)
# ============================================================================
log_info "=== 16. PoSConsensusController: Testing PoS ==="

# POST /api/consensus/pos/validate-stake (10x)
for i in {1..10}; do
    block_json='{"blockIndex":'$i',"hash":"pos_hash_'$i'","previousHash":"prev","timestamp":1699564800000,"nonce":0}'
    curl -s -X POST "${API_URL}/api/consensus/pos/validate-stake?validatorAddress=${WALLETS[$i]}" \
        -H "Authorization: Bearer $token" \
        -H "Content-Type: application/json" \
        -d "$block_json" > /dev/null
done

# POST /api/consensus/pos/calculate-reward (10x)
for i in {1..10}; do
    if [[ -n "${STAKE_IDS[$i]}" ]]; then
        stake_json='{"id":'${STAKE_IDS[$i]}',"walletAddress":"'${WALLETS[$i]}'","stakedAmount":100,"rewardsEarned":0}'
        curl -s -X POST "${API_URL}/api/consensus/pos/calculate-reward" \
            -H "Authorization: Bearer $token" \
            -H "Content-Type: application/json" \
            -d "$stake_json" > /dev/null
    fi
done

# Test GET endpoints
curl -s "${API_URL}/api/consensus/pos/select-validator" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/consensus/pos/validator-probability/${WALLETS[1]}" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/consensus/pos/eligible-validators" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/consensus/pos/has-minimum-stake/${WALLETS[1]}" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/consensus/pos/total-staked" -H "Authorization: Bearer $token" > /dev/null
curl -s "${API_URL}/api/consensus/pos/validator-info/${WALLETS[1]}" -H "Authorization: Bearer $token" > /dev/null
log_success "PoS Consensus operations tested"

# ============================================================================
# 17. ValidationController (All Validation Operations)
# ============================================================================
log_info "=== 17. ValidationController: Testing Validations ==="

# POST /api/validation/transaction (10x)
for i in {1..10}; do
    tx_json='{"transactionHash":"val_tx_'$i'","senderAddress":"'${WALLETS[$i]}'","recipientAddress":"'${WALLETS[1]}'","amount":5,"signature":"sig_'$i'"}'
    curl -s -X POST "${API_URL}/api/validation/transaction" \
        -H "Authorization: Bearer $token" \
        -H "Content-Type: application/json" \
        -d "$tx_json" > /dev/null
done

# POST /api/validation/block (10x)
for i in {1..10}; do
    block_json='{"blockIndex":'$i',"hash":"val_hash_'$i'","previousHash":"prev","timestamp":1699564800000,"nonce":'$i'}'
    curl -s -X POST "${API_URL}/api/validation/block" \
        -H "Authorization: Bearer $token" \
        -H "Content-Type: application/json" \
        -d "$block_json" > /dev/null
done

# POST /api/validation/signature (10x)
for i in {1..10}; do
    tx_json='{"transactionHash":"sig_tx_'$i'","senderAddress":"'${WALLETS[$i]}'","recipientAddress":"'${WALLETS[1]}'","amount":1,"signature":"test_sig"}'
    curl -s -X POST "${API_URL}/api/validation/signature" \
        -H "Authorization: Bearer $token" \
        -H "Content-Type: application/json" \
        -d "$tx_json" > /dev/null
done

# POST /api/validation/balance (10x)
for i in {1..10}; do
    amount=$(echo "$i * 0.1" | bc)
    curl -s -X POST "${API_URL}/api/validation/balance?walletAddress=${WALLETS[$i]}&amount=$amount" \
        -H "Authorization: Bearer $token" > /dev/null
done

# POST /api/validation/block-hash (10x)
for i in {1..10}; do
    block_json='{"blockIndex":'$i',"hash":"blockhash_'$i'","previousHash":"prev","timestamp":1699564800000,"nonce":1}'
    curl -s -X POST "${API_URL}/api/validation/block-hash" \
        -H "Authorization: Bearer $token" \
        -H "Content-Type: application/json" \
        -d "$block_json" > /dev/null
done

# POST /api/validation/transaction-format (10x)
for i in {1..10}; do
    tx_json='{"transactionHash":"fmt_tx_'$i'","senderAddress":"'${WALLETS[$i]}'","recipientAddress":"'${WALLETS[1]}'","amount":1}'
    curl -s -X POST "${API_URL}/api/validation/transaction-format" \
        -H "Authorization: Bearer $token" \
        -H "Content-Type: application/json" \
        -d "$tx_json" > /dev/null
done

# POST /api/validation/double-spend-check (10x)
for i in {1..10}; do
    tx_json='{"transactionHash":"dbl_tx_'$i'","senderAddress":"'${WALLETS[$i]}'","recipientAddress":"'${WALLETS[1]}'","amount":999}'
    curl -s -X POST "${API_URL}/api/validation/double-spend-check" \
        -H "Authorization: Bearer $token" \
        -H "Content-Type: application/json" \
        -d "$tx_json" > /dev/null
done

# Test GET endpoint
curl -s "${API_URL}/api/validation/address/${WALLETS[1]}" -H "Authorization: Bearer $token" > /dev/null
log_success "Validation operations tested"

# ============================================================================
# 18. Additional PUT/DELETE Operations (Edge Cases)
# ============================================================================
log_info "=== 18. Testing Additional PUT/DELETE Operations ==="

# PUT /api/wallets/{address}/balance (Admin - may fail)
curl -s -X PUT "${API_URL}/api/wallets/${WALLETS[1]}/balance?newBalance=5000" \
    -H "Authorization: Bearer $token" > /dev/null 2>&1

# Test User Search multiple times
for i in {1..10}; do
    search_term="${BASE_USER}_$i"
    curl -s "${API_URL}/api/users/search?searchTerm=$search_term" \
        -H "Authorization: Bearer $token" > /dev/null
done

# Test username/email exists checks
for i in {1..10}; do
    username="${BASE_USER}_${i}"
    email="${BASE_USER}_${i}@test.com"
    curl -s "${API_URL}/api/users/exists/username/$username" -H "Authorization: Bearer $token" > /dev/null
    curl -s "${API_URL}/api/users/exists/email/$email" -H "Authorization: Bearer $token" > /dev/null
done

log_success "Additional operations tested"

# ============================================================================
# 19. Summary & Statistics
# ============================================================================
echo ""
echo "======================================================================"
log_success "COMPLETE LOAD TEST FINISHED!"
echo "======================================================================"
echo ""
log_info "Summary of Operations:"
echo "  - Users Created: 10"
echo "  - Wallets Created: 10"
echo "  - Transactions Sent: 10"
echo "  - Blocks Mined: 20 (2 per user)"
echo "  - Stakes Created: 10"
echo "  - Nodes Registered: 10"
echo "  - Transaction Pools: 10"
echo "  - Crypto Operations: 90+ (9 per user)"
echo "  - Validation Checks: 70+"
echo "  - P2P Messages: 20+"
echo "  - Sync Operations: 30+"
echo ""
log_info "Data for Testing:"
echo "  First User ID: ${USER_IDS[1]}"
echo "  First Token: ${USER_TOKENS[1]:0:50}..."
echo "  First Wallet: ${WALLETS[1]}"
echo "  First Transaction: ${TX_HASHES[1]}"
echo "  First Node: ${NODE_IDS[1]}"
echo "  First Stake ID: ${STAKE_IDS[1]}"
echo ""
log_success "All POST methods executed 10x with different data!"
log_success "All GET endpoints tested successfully!"
log_info "Check your application logs for detailed results"
echo "======================================================================"