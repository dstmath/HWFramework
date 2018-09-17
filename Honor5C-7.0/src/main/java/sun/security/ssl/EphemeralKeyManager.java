package sun.security.ssl;

import java.lang.reflect.Modifier;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;

final class EphemeralKeyManager {
    private static final int INDEX_RSA1024 = 1;
    private static final int INDEX_RSA512 = 0;
    private final EphemeralKeyPair[] keys;

    private static class EphemeralKeyPair {
        private static final int MAX_USE = 200;
        private static final long USE_INTERVAL = 3600000;
        private long expirationTime;
        private KeyPair keyPair;
        private int uses;

        private EphemeralKeyPair(KeyPair keyPair) {
            this.keyPair = keyPair;
            this.expirationTime = System.currentTimeMillis() + USE_INTERVAL;
        }

        private boolean isValid() {
            if (this.keyPair == null || this.uses >= MAX_USE || System.currentTimeMillis() >= this.expirationTime) {
                return false;
            }
            return true;
        }

        private KeyPair getKeyPair() {
            if (isValid()) {
                this.uses += EphemeralKeyManager.INDEX_RSA1024;
                return this.keyPair;
            }
            this.keyPair = null;
            return null;
        }
    }

    EphemeralKeyManager() {
        this.keys = new EphemeralKeyPair[]{new EphemeralKeyPair(null), new EphemeralKeyPair(null)};
    }

    KeyPair getRSAKeyPair(boolean export, SecureRandom random) {
        int length;
        int index;
        KeyPair kp;
        if (export) {
            length = Modifier.INTERFACE;
            index = 0;
        } else {
            length = Record.maxExpansion;
            index = INDEX_RSA1024;
        }
        synchronized (this.keys) {
            kp = this.keys[index].getKeyPair();
            if (kp == null) {
                try {
                    KeyPairGenerator kgen = JsseJce.getKeyPairGenerator("RSA");
                    kgen.initialize(length, random);
                    this.keys[index] = new EphemeralKeyPair(null);
                    kp = this.keys[index].getKeyPair();
                } catch (Exception e) {
                }
            }
        }
        return kp;
    }
}
