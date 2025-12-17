package com.wallet.biochain.services;

import java.security.KeyPair;

public interface CryptographyService {

    /**
     * Generate RSA key pair
     */
    KeyPair generateKeyPair();

    /**
     * Generate wallet address from public key
     */
    String generateAddress(String publicKey);

    /**
     * Encrypt data with public key
     */
    String encrypt(String data, String publicKey);

    /**
     * Decrypt data with private key
     */
    String decrypt(String encryptedData, String privateKey);

    /**
     * Hash data using SHA-256
     */
    String hash(String data);

    /**
     * Sign data with private key
     */
    String sign(String data, String privateKey);

    /**
     * Verify signature with public key
     */
    boolean verifySignature(String data, String signature, String publicKey);

    /**
     * Encode public key to string
     */
    String encodePublicKey(java.security.PublicKey publicKey);

    /**
     * Encode private key to string
     */
    String encodePrivateKey(java.security.PrivateKey privateKey);

    /**
     * Decode public key from string
     */
    java.security.PublicKey decodePublicKey(String publicKeyString);

    /**
     * Decode private key from string
     */
    java.security.PrivateKey decodePrivateKey(String privateKeyString);

    /**
     * Encrypt private key with password (for storage)
     */
    String encryptPrivateKey(String privateKey, String password);

    /**
     * Decrypt private key with password
     */
    String decryptPrivateKey(String encryptedPrivateKey, String password);
}