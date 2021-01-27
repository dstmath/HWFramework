package android.security.keystore;

import android.security.KeyStoreException;

/* access modifiers changed from: package-private */
public interface KeyStoreCryptoOperationStreamer {
    byte[] doFinal(byte[] bArr, int i, int i2, byte[] bArr2, byte[] bArr3) throws KeyStoreException;

    long getConsumedInputSizeBytes();

    long getProducedOutputSizeBytes();

    byte[] update(byte[] bArr, int i, int i2) throws KeyStoreException;
}
