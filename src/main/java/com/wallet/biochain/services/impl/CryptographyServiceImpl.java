package com.wallet.biochain.services.impl;

import com.wallet.biochain.services.CryptographyService;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Slf4j
@Service
public class CryptographyServiceImpl implements CryptographyService {

    private static final String RSA_ALGORITHM = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final String AES_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String SHA_256 = "SHA-256";
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    private static final int KEY_SIZE = 2048;
    private static final int AES_KEY_SIZE = 256;
    private static final int ITERATION_COUNT = 65536;
    private static final int SALT_LENGTH = 32; // 256 bits
    private static final int IV_LENGTH = 16; // 128 bits for AES

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Override
    public KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(KEY_SIZE, new SecureRandom());
            KeyPair keyPair = keyGen.generateKeyPair();
            log.info("Generated RSA key pair successfully");
            return keyPair;
        } catch (Exception e) {
            log.error("Failed to generate key pair", e);
            throw new RuntimeException("Failed to generate key pair", e);
        }
    }

    @Override
    public String generateAddress(String publicKey) {
        try {
            // Hash the public key with SHA-256
            String hash = hash(publicKey);
            // Take first 40 characters and prepend "0x" for blockchain-style address
            String address = "0x" + hash.substring(0, 40);
            log.debug("Generated address: {}", address);
            return address;
        } catch (Exception e) {
            log.error("Failed to generate address", e);
            throw new RuntimeException("Failed to generate address", e);
        }
    }

    @Override
    public String encrypt(String data, String publicKey) {
        try {
            PublicKey key = decodePublicKey(publicKey);
            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            log.error("Failed to encrypt data", e);
            throw new RuntimeException("Failed to encrypt data", e);
        }
    }

    @Override
    public String decrypt(String encryptedData, String privateKey) {
        try {
            PrivateKey key = decodePrivateKey(privateKey);
            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Failed to decrypt data", e);
            throw new RuntimeException("Failed to decrypt data", e);
        }
    }

    @Override
    public String hash(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA_256);
            byte[] hashBytes = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (Exception e) {
            log.error("Failed to hash data", e);
            throw new RuntimeException("Failed to hash data", e);
        }
    }

    @Override
    public String sign(String data, String privateKey) {
        try {
            PrivateKey key = decodePrivateKey(privateKey);
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initSign(key);
            signature.update(data.getBytes(StandardCharsets.UTF_8));
            byte[] signatureBytes = signature.sign();
            return Base64.getEncoder().encodeToString(signatureBytes);
        } catch (Exception e) {
            log.error("Failed to sign data", e);
            throw new RuntimeException("Failed to sign data", e);
        }
    }

    @Override
    public boolean verifySignature(String data, String signatureStr, String publicKey) {
        try {
            PublicKey key = decodePublicKey(publicKey);
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initVerify(key);
            signature.update(data.getBytes(StandardCharsets.UTF_8));
            byte[] signatureBytes = Base64.getDecoder().decode(signatureStr);
            return signature.verify(signatureBytes);
        } catch (Exception e) {
            log.error("Failed to verify signature", e);
            return false;
        }
    }

    @Override
    public String encodePublicKey(PublicKey publicKey) {
        byte[] encoded = publicKey.getEncoded();
        return Base64.getEncoder().encodeToString(encoded);
    }

    @Override
    public String encodePrivateKey(PrivateKey privateKey) {
        byte[] encoded = privateKey.getEncoded();
        return Base64.getEncoder().encodeToString(encoded);
    }

    @Override
    public PublicKey decodePublicKey(String publicKeyString) {
        try {
            byte[] decoded = Base64.getDecoder().decode(publicKeyString);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(spec);
        } catch (Exception e) {
            log.error("Failed to decode public key", e);
            throw new RuntimeException("Failed to decode public key", e);
        }
    }

    @Override
    public PrivateKey decodePrivateKey(String privateKeyString) {
        try {
            byte[] decoded = Base64.getDecoder().decode(privateKeyString);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(spec);
        } catch (Exception e) {
            log.error("Failed to decode private key", e);
            throw new RuntimeException("Failed to decode private key", e);
        }
    }

    @Override
    public String encryptPrivateKey(String privateKey, String password) {
        try {
            // Generate random salt and IV
            byte[] salt = generateRandomBytes(SALT_LENGTH);
            byte[] iv = generateRandomBytes(IV_LENGTH);

            SecretKey secretKey = deriveKeyFromPassword(password, salt);
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
            byte[] encryptedBytes = cipher.doFinal(privateKey.getBytes(StandardCharsets.UTF_8));

            // Combine salt + IV + encrypted data
            ByteBuffer buffer = ByteBuffer.allocate(salt.length + iv.length + encryptedBytes.length);
            buffer.put(salt);
            buffer.put(iv);
            buffer.put(encryptedBytes);

            return Base64.getEncoder().encodeToString(buffer.array());
        } catch (Exception e) {
            log.error("Failed to encrypt private key with password", e);
            throw new RuntimeException("Failed to encrypt private key", e);
        }
    }

    @Override
    public String decryptPrivateKey(String encryptedPrivateKey, String password) {
        try {
            byte[] combined = Base64.getDecoder().decode(encryptedPrivateKey);
            ByteBuffer buffer = ByteBuffer.wrap(combined);

            // Extract salt, IV, and encrypted data
            byte[] salt = new byte[SALT_LENGTH];
            byte[] iv = new byte[IV_LENGTH];
            buffer.get(salt);
            buffer.get(iv);
            byte[] encryptedBytes = new byte[buffer.remaining()];
            buffer.get(encryptedBytes);

            SecretKey secretKey = deriveKeyFromPassword(password, salt);
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Failed to decrypt private key with password", e);
            throw new RuntimeException("Failed to decrypt private key", e);
        }
    }

    /**
     * Derive AES key from password using PBKDF2 with a random salt
     */
    private SecretKey deriveKeyFromPassword(String password, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, AES_KEY_SIZE);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        spec.clearPassword(); // Clear sensitive data
        return new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * Generate cryptographically secure random bytes
     */
    private byte[] generateRandomBytes(int length) {
        byte[] bytes = new byte[length];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(bytes);
        return bytes;
    }

    /**
     * Convert byte array to hexadecimal string
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}