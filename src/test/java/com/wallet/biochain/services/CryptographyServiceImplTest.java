package com.wallet.biochain.services;

import com.wallet.biochain.services.impl.CryptographyServiceImpl;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;

import static org.junit.jupiter.api.Assertions.*;

class CryptographyServiceImplTest {

    private final CryptographyServiceImpl crypto = new CryptographyServiceImpl();

    @Test
    void generateKeyPair_notNullAndWorksForEncodeDecode() {
        KeyPair pair = crypto.generateKeyPair();
        assertNotNull(pair);
        String pubStr = crypto.encodePublicKey(pair.getPublic());
        String privStr = crypto.encodePrivateKey(pair.getPrivate());

        assertNotNull(crypto.decodePublicKey(pubStr));
        assertNotNull(crypto.decodePrivateKey(privStr));
    }

    @Test
    void hash_sameInputSameOutput() {
        String h1 = crypto.hash("data");
        String h2 = crypto.hash("data");
        assertEquals(h1, h2);
        assertNotEquals(h1, crypto.hash("other"));
    }

    @Test
    void encryptDecrypt_roundTrip() {
        KeyPair pair = crypto.generateKeyPair();
        String pub = crypto.encodePublicKey(pair.getPublic());
        String priv = crypto.encodePrivateKey(pair.getPrivate());

        String encrypted = crypto.encrypt("secret", pub);
        String decrypted = crypto.decrypt(encrypted, priv);

        assertEquals("secret", decrypted);
    }

    @Test
    void signAndVerify_roundTrip() {
        KeyPair pair = crypto.generateKeyPair();
        String priv = crypto.encodePrivateKey(pair.getPrivate());
        String pub = crypto.encodePublicKey(pair.getPublic());

        String sig = crypto.sign("data", priv);
        assertTrue(crypto.verifySignature("data", sig, pub));
        assertFalse(crypto.verifySignature("other", sig, pub));
    }

    @Test
    void encryptDecryptPrivateKey_roundTrip() {
        KeyPair pair = crypto.generateKeyPair();
        String priv = crypto.encodePrivateKey(pair.getPrivate());

        String enc = crypto.encryptPrivateKey(priv, "pwd");
        String dec = crypto.decryptPrivateKey(enc, "pwd");

        assertEquals(priv, dec);
    }
}
