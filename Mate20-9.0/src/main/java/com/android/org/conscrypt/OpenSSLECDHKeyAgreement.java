package com.android.org.conscrypt;

import com.android.org.conscrypt.NativeRef;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.KeyAgreementSpi;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.SecretKeySpec;

public final class OpenSSLECDHKeyAgreement extends KeyAgreementSpi {
    private int mExpectedResultLength;
    private OpenSSLKey mOpenSslPrivateKey;
    private byte[] mResult;

    public Key engineDoPhase(Key key, boolean lastPhase) throws InvalidKeyException {
        byte[] result;
        if (this.mOpenSslPrivateKey == null) {
            throw new IllegalStateException("Not initialized");
        } else if (!lastPhase) {
            throw new IllegalStateException("ECDH only has one phase");
        } else if (key == null) {
            throw new InvalidKeyException("key == null");
        } else if (key instanceof PublicKey) {
            OpenSSLKey openSslPublicKey = OpenSSLKey.fromPublicKey((PublicKey) key);
            byte[] buffer = new byte[this.mExpectedResultLength];
            int actualResultLength = NativeCrypto.ECDH_compute_key(buffer, 0, openSslPublicKey.getNativeRef(), this.mOpenSslPrivateKey.getNativeRef());
            if (actualResultLength != -1) {
                if (actualResultLength == this.mExpectedResultLength) {
                    result = buffer;
                } else if (actualResultLength < this.mExpectedResultLength) {
                    result = new byte[actualResultLength];
                    System.arraycopy(buffer, 0, this.mResult, 0, this.mResult.length);
                } else {
                    throw new RuntimeException("Engine produced a longer than expected result. Expected: " + this.mExpectedResultLength + ", actual: " + actualResultLength);
                }
                this.mResult = result;
                return null;
            }
            throw new RuntimeException("Engine returned " + actualResultLength);
        } else {
            throw new InvalidKeyException("Not a public key: " + key.getClass());
        }
    }

    /* access modifiers changed from: protected */
    public int engineGenerateSecret(byte[] sharedSecret, int offset) throws ShortBufferException {
        checkCompleted();
        int available = sharedSecret.length - offset;
        if (this.mResult.length <= available) {
            System.arraycopy(this.mResult, 0, sharedSecret, offset, this.mResult.length);
            return this.mResult.length;
        }
        throw new ShortBufferException("Needed: " + this.mResult.length + ", available: " + available);
    }

    /* access modifiers changed from: protected */
    public byte[] engineGenerateSecret() {
        checkCompleted();
        return this.mResult;
    }

    /* access modifiers changed from: protected */
    public SecretKey engineGenerateSecret(String algorithm) {
        checkCompleted();
        return new SecretKeySpec(engineGenerateSecret(), algorithm);
    }

    /* access modifiers changed from: protected */
    public void engineInit(Key key, SecureRandom random) throws InvalidKeyException {
        if (key == null) {
            throw new InvalidKeyException("key == null");
        } else if (key instanceof PrivateKey) {
            OpenSSLKey openSslKey = OpenSSLKey.fromPrivateKey((PrivateKey) key);
            this.mExpectedResultLength = (NativeCrypto.EC_GROUP_get_degree(new NativeRef.EC_GROUP(NativeCrypto.EC_KEY_get1_group(openSslKey.getNativeRef()))) + 7) / 8;
            this.mOpenSslPrivateKey = openSslKey;
        } else {
            throw new InvalidKeyException("Not a private key: " + key.getClass());
        }
    }

    /* access modifiers changed from: protected */
    public void engineInit(Key key, AlgorithmParameterSpec params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (params == null) {
            engineInit(key, random);
            return;
        }
        throw new InvalidAlgorithmParameterException("No algorithm parameters supported");
    }

    private void checkCompleted() {
        if (this.mResult == null) {
            throw new IllegalStateException("Key agreement not completed");
        }
    }
}
