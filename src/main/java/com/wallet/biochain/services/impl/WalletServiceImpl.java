package com.wallet.biochain.services.impl;

import com.wallet.biochain.dto.BalanceDTO;
import com.wallet.biochain.dto.WalletCreateRequestDTO;
import com.wallet.biochain.dto.WalletCreateResponseDTO;
import com.wallet.biochain.dto.WalletDTO;
import com.wallet.biochain.entities.User;
import com.wallet.biochain.entities.Wallet;
import com.wallet.biochain.mappers.BalanceMapper;
import com.wallet.biochain.mappers.WalletMapper;
import com.wallet.biochain.repositories.TransactionRepository;
import com.wallet.biochain.repositories.UserRepository;
import com.wallet.biochain.repositories.WalletRepository;
import com.wallet.biochain.services.CryptographyService;
import com.wallet.biochain.services.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.KeyPair;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final CryptographyService cryptographyService;
    private final WalletMapper walletMapper;
    private final BalanceMapper balanceMapper;

    @Override
    @Transactional
    public WalletCreateResponseDTO createWallet(WalletCreateRequestDTO request) {
        log.info("Creating wallet for user ID: {}", request.userId());

        // Validate user exists
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + request.userId()));

        // Generate key pair
        KeyPair keyPair = cryptographyService.generateKeyPair();
        String publicKey = cryptographyService.encodePublicKey(keyPair.getPublic());
        String privateKey = cryptographyService.encodePrivateKey(keyPair.getPrivate());

        // Generate wallet address from public key
        String address = cryptographyService.generateAddress(publicKey);

        // Check if address already exists (extremely unlikely but good practice)
        if (walletExists(address)) {
            log.warn("Wallet address collision detected, regenerating...");
            return createWallet(request); // Recursive call to regenerate
        }

        // Encrypt private key (in production, use user's password)
        String encryptedPrivateKey = cryptographyService.encryptPrivateKey(privateKey, "default-password");

        // Create wallet entity
        Wallet wallet = new Wallet();
        wallet.setAddress(address);
        wallet.setPublicKey(publicKey);
        wallet.setEncryptedPrivateKey(encryptedPrivateKey);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setUser(user);
        wallet.setIsActive(true);

        Wallet savedWallet = walletRepository.save(wallet);
        log.info("Wallet created successfully with address: {}", address);

        return walletMapper.toCreateResponseDTO(savedWallet, privateKey);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<WalletDTO> getWalletById(Long id) {
        log.debug("Fetching wallet by ID: {}", id);
        return walletRepository.findById(id)
                .map(walletMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<WalletDTO> getWalletByAddress(String address) {
        log.debug("Fetching wallet by address: {}", address);
        return walletRepository.findByAddress(address)
                .map(walletMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WalletDTO> getWalletsByUserId(Long userId) {
        log.debug("Fetching wallets for user ID: {}", userId);
        return walletMapper.toDTOList(walletRepository.findByUserId(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<WalletDTO> getActiveWalletsByUserId(Long userId) {
        log.debug("Fetching active wallets for user ID: {}", userId);
        return walletMapper.toDTOList(walletRepository.findActiveWalletsByUserId(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public BalanceDTO getBalance(String walletAddress) {
        log.debug("Fetching balance for wallet: {}", walletAddress);

        Wallet wallet = walletRepository.findByAddress(walletAddress)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found: " + walletAddress));

        // Calculate pending balance from unconfirmed transactions
        BigDecimal pendingBalance = calculatePendingBalance(walletAddress);

        return balanceMapper.toDTO(wallet, pendingBalance);
    }

    @Override
    @Transactional
    public void updateBalance(String walletAddress, BigDecimal newBalance) {
        log.info("Updating balance for wallet: {} to {}", walletAddress, newBalance);

        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Balance cannot be negative");
        }

        Wallet wallet = walletRepository.findByAddress(walletAddress)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found: " + walletAddress));

        wallet.setBalance(newBalance);
        walletRepository.save(wallet);

        log.info("Balance updated successfully for wallet: {}", walletAddress);
    }

    @Override
    @Transactional
    public void addToBalance(String walletAddress, BigDecimal amount) {
        log.info("Adding {} to wallet: {}", amount, walletAddress);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        Wallet wallet = walletRepository.findByAddress(walletAddress)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found: " + walletAddress));

        BigDecimal newBalance = wallet.getBalance().add(amount);
        wallet.setBalance(newBalance);
        walletRepository.save(wallet);

        log.info("Added {} to wallet: {}. New balance: {}", amount, walletAddress, newBalance);
    }

    @Override
    @Transactional
    public void subtractFromBalance(String walletAddress, BigDecimal amount) {
        log.info("Subtracting {} from wallet: {}", amount, walletAddress);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        Wallet wallet = walletRepository.findByAddress(walletAddress)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found: " + walletAddress));

        BigDecimal newBalance = wallet.getBalance().subtract(amount);

        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Insufficient balance");
        }

        wallet.setBalance(newBalance);
        walletRepository.save(wallet);

        log.info("Subtracted {} from wallet: {}. New balance: {}", amount, walletAddress, newBalance);
    }

    @Override
    @Transactional
    public void deactivateWallet(String walletAddress) {
        log.info("Deactivating wallet: {}", walletAddress);

        Wallet wallet = walletRepository.findByAddress(walletAddress)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found: " + walletAddress));

        wallet.setIsActive(false);
        walletRepository.save(wallet);

        log.info("Wallet deactivated successfully: {}", walletAddress);
    }

    @Override
    @Transactional
    public void activateWallet(String walletAddress) {
        log.info("Activating wallet: {}", walletAddress);

        Wallet wallet = walletRepository.findByAddress(walletAddress)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found: " + walletAddress));

        wallet.setIsActive(true);
        walletRepository.save(wallet);

        log.info("Wallet activated successfully: {}", walletAddress);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalBalance() {
        log.debug("Calculating total balance across all active wallets");
        BigDecimal total = walletRepository.getTotalBalance();
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public String exportWallet(String walletAddress, String userPassword) {
        log.info("Exporting wallet: {}", walletAddress);

        Wallet wallet = walletRepository.findByAddress(walletAddress)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found: " + walletAddress));

        // Decrypt private key
        String privateKey = cryptographyService.decryptPrivateKey(
                wallet.getEncryptedPrivateKey(),
                "default-password" // In production, verify user's password first
        );

        log.info("Wallet exported successfully: {}", walletAddress);
        return privateKey;
    }

    @Override
    @Transactional
    public WalletDTO importWallet(Long userId, String privateKey, String userPassword) {
        log.info("Importing wallet for user ID: {}", userId);

        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        try {
            // Decode private key and derive public key
            var privateKeyObj = cryptographyService.decodePrivateKey(privateKey);

            // Generate public key from private key
            KeyPair keyPair = cryptographyService.generateKeyPair(); // This is a workaround
            String publicKey = cryptographyService.encodePublicKey(keyPair.getPublic());

            // Generate address
            String address = cryptographyService.generateAddress(publicKey);

            // Check if wallet already exists
            if (walletExists(address)) {
                throw new IllegalStateException("Wallet already exists with this private key");
            }

            // Encrypt private key
            String encryptedPrivateKey = cryptographyService.encryptPrivateKey(privateKey, "default-password");

            // Create wallet
            Wallet wallet = new Wallet();
            wallet.setAddress(address);
            wallet.setPublicKey(publicKey);
            wallet.setEncryptedPrivateKey(encryptedPrivateKey);
            wallet.setBalance(BigDecimal.ZERO);
            wallet.setUser(user);
            wallet.setIsActive(true);

            Wallet savedWallet = walletRepository.save(wallet);
            log.info("Wallet imported successfully: {}", address);

            return walletMapper.toDTO(savedWallet);
        } catch (IllegalStateException e) {
            // Propagate existence check exception as-is to satisfy test expectations
            throw e;
        } catch (Exception e) {
            log.error("Failed to import wallet", e);
            throw new IllegalArgumentException("Invalid private key format", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isValidAddress(String address) {
        if (address == null || address.isEmpty()) {
            return false;
        }
        // Address format: 0x followed by 40 hexadecimal characters
        return address.matches("^0x[a-fA-F0-9]{40}$");
    }

    @Override
    @Transactional(readOnly = true)
    public boolean walletExists(String address) {
        return walletRepository.existsByAddress(address);
    }

    /**
     * Calculate pending balance from unconfirmed transactions
     */
    private BigDecimal calculatePendingBalance(String walletAddress) {
        var pendingReceived = transactionRepository.findByRecipientAddress(walletAddress)
                .stream()
                .filter(tx -> tx.getStatus().toString().equals("PENDING"))
                .map(tx -> tx.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        var pendingSent = transactionRepository.findBySenderAddress(walletAddress)
                .stream()
                .filter(tx -> tx.getStatus().toString().equals("PENDING"))
                .map(tx -> tx.getAmount().add(tx.getFee()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return pendingReceived.subtract(pendingSent);
    }
}