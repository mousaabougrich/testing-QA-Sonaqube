package com.wallet.biochain.controllers;

import com.wallet.biochain.services.CryptographyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.KeyPair;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/crypto")
@RequiredArgsConstructor
@Tag(name = "Cryptography", description = "Cryptographic operations endpoints")
public class CryptographyController {

    private final CryptographyService cryptographyService;

    @PostMapping("/generate-keypair")
    @Operation(summary = "Generate key pair", description = "Generates a new RSA key pair")
    public ResponseEntity<Map<String, String>> generateKeyPair() {
        log.info("REST request to generate key pair");

        try {
            KeyPair keyPair = cryptographyService.generateKeyPair();

            String publicKey = cryptographyService.encodePublicKey(keyPair.getPublic());
            String privateKey = cryptographyService.encodePrivateKey(keyPair.getPrivate());

            Map<String, String> keys = new HashMap<>();
            keys.put("publicKey", publicKey);
            keys.put("privateKey", privateKey);
            keys.put("message", "⚠️ CRITICAL: Save your private key securely. It cannot be recovered!");

            return ResponseEntity.ok(keys);
        } catch (Exception e) {
            log.error("Failed to generate key pair", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/generate-address")
    @Operation(summary = "Generate address", description = "Generates blockchain address from public key")
    public ResponseEntity<Map<String, String>> generateAddress(@RequestParam String publicKey) {
        log.info("REST request to generate address from public key");

        try {
            String address = cryptographyService.generateAddress(publicKey);

            Map<String, String> response = new HashMap<>();
            response.put("address", address);
            response.put("publicKey", publicKey);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to generate address", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/hash")
    @Operation(summary = "Hash data", description = "Generates SHA-256 hash of data")
    public ResponseEntity<Map<String, String>> hash(@RequestParam String data) {
        log.debug("REST request to hash data");

        try {
            String hash = cryptographyService.hash(data);

            Map<String, String> response = new HashMap<>();
            response.put("original", data);
            response.put("hash", hash);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to hash data", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/sign")
    @Operation(summary = "Sign data", description = "Signs data with private key")
    public ResponseEntity<Map<String, String>> sign(
            @RequestParam String data,
            @RequestParam String privateKey) {
        log.debug("REST request to sign data");

        try {
            String signature = cryptographyService.sign(data, privateKey);

            Map<String, String> response = new HashMap<>();
            response.put("data", data);
            response.put("signature", signature);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to sign data", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify signature", description = "Verifies signature with public key")
    public ResponseEntity<Map<String, Object>> verifySignature(
            @RequestParam String data,
            @RequestParam String signature,
            @RequestParam String publicKey) {
        log.debug("REST request to verify signature");

        try {
            boolean isValid = cryptographyService.verifySignature(data, signature, publicKey);

            Map<String, Object> response = new HashMap<>();
            response.put("isValid", isValid);
            response.put("message", isValid ? "Signature is valid" : "Signature is invalid");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to verify signature", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/encrypt")
    @Operation(summary = "Encrypt data", description = "Encrypts data with public key (RSA)")
    public ResponseEntity<Map<String, String>> encrypt(
            @RequestParam String data,
            @RequestParam String publicKey) {
        log.debug("REST request to encrypt data");

        try {
            String encryptedData = cryptographyService.encrypt(data, publicKey);

            Map<String, String> response = new HashMap<>();
            response.put("encryptedData", encryptedData);
            response.put("algorithm", "RSA");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to encrypt data", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/decrypt")
    @Operation(summary = "Decrypt data", description = "Decrypts data with private key (RSA)")
    public ResponseEntity<Map<String, String>> decrypt(
            @RequestParam String encryptedData,
            @RequestParam String privateKey) {
        log.debug("REST request to decrypt data");

        try {
            String decryptedData = cryptographyService.decrypt(encryptedData, privateKey);

            Map<String, String> response = new HashMap<>();
            response.put("decryptedData", decryptedData);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to decrypt data", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/encrypt-private-key")
    @Operation(summary = "Encrypt private key", description = "Encrypts private key with password (AES)")
    public ResponseEntity<Map<String, String>> encryptPrivateKey(
            @RequestParam String privateKey,
            @RequestParam String password) {
        log.info("REST request to encrypt private key with password");

        try {
            String encryptedPrivateKey = cryptographyService.encryptPrivateKey(privateKey, password);

            Map<String, String> response = new HashMap<>();
            response.put("encryptedPrivateKey", encryptedPrivateKey);
            response.put("message", "Private key encrypted successfully. Remember your password!");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to encrypt private key", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/decrypt-private-key")
    @Operation(summary = "Decrypt private key", description = "Decrypts private key with password (AES)")
    public ResponseEntity<Map<String, String>> decryptPrivateKey(
            @RequestParam String encryptedPrivateKey,
            @RequestParam String password) {
        log.info("REST request to decrypt private key with password");

        try {
            String privateKey = cryptographyService.decryptPrivateKey(encryptedPrivateKey, password);

            Map<String, String> response = new HashMap<>();
            response.put("privateKey", privateKey);
            response.put("warning", "⚠️ Keep this private key secure!");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to decrypt private key (incorrect password?)", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to decrypt. Incorrect password?");
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/info")
    @Operation(summary = "Get cryptography info", description = "Gets information about crypto algorithms used")
    public ResponseEntity<Map<String, String>> getCryptoInfo() {
        log.debug("REST request to get cryptography info");

        Map<String, String> info = new HashMap<>();
        info.put("asymmetricAlgorithm", "RSA-2048");
        info.put("symmetricAlgorithm", "AES-256");
        info.put("hashAlgorithm", "SHA-256");
        info.put("signatureAlgorithm", "SHA256withRSA");
        info.put("keyDerivation", "PBKDF2WithHmacSHA256");
        info.put("provider", "BouncyCastle");

        return ResponseEntity.ok(info);
    }

    @PostMapping("/validate-keypair")
    @Operation(summary = "Validate key pair", description = "Validates that public and private keys match")
    public ResponseEntity<Map<String, Object>> validateKeyPair(
            @RequestParam String publicKey,
            @RequestParam String privateKey) {
        log.debug("REST request to validate key pair");

        try {
            // Test by signing and verifying
            String testData = "test-validation-" + System.currentTimeMillis();
            String signature = cryptographyService.sign(testData, privateKey);
            boolean isValid = cryptographyService.verifySignature(testData, signature, publicKey);

            Map<String, Object> response = new HashMap<>();
            response.put("isValid", isValid);
            response.put("message", isValid ?
                    "Key pair is valid and matches" :
                    "Key pair is invalid or doesn't match");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to validate key pair", e);
            Map<String, Object> error = new HashMap<>();
            error.put("isValid", false);
            error.put("error", "Invalid key format or keys don't match");
            return ResponseEntity.badRequest().body(error);
        }
    }
}