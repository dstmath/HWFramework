package android.hardware.biometrics;

import android.security.keystore.AndroidKeyStoreProvider;
import java.security.Signature;
import javax.crypto.Cipher;
import javax.crypto.Mac;

public class CryptoObject {
    private final Object mCrypto;

    public CryptoObject(Signature signature) {
        this.mCrypto = signature;
    }

    public CryptoObject(Cipher cipher) {
        this.mCrypto = cipher;
    }

    public CryptoObject(Mac mac) {
        this.mCrypto = mac;
    }

    public Signature getSignature() {
        if (this.mCrypto instanceof Signature) {
            return (Signature) this.mCrypto;
        }
        return null;
    }

    public Cipher getCipher() {
        if (this.mCrypto instanceof Cipher) {
            return (Cipher) this.mCrypto;
        }
        return null;
    }

    public Mac getMac() {
        if (this.mCrypto instanceof Mac) {
            return (Mac) this.mCrypto;
        }
        return null;
    }

    public final long getOpId() {
        if (this.mCrypto != null) {
            return AndroidKeyStoreProvider.getKeyStoreOperationHandle(this.mCrypto);
        }
        return 0;
    }
}
