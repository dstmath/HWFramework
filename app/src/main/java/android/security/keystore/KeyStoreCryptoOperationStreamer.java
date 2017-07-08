package android.security.keystore;

import android.security.KeyStoreException;

interface KeyStoreCryptoOperationStreamer {
    byte[] doFinal(byte[] bArr, int i, int i2, byte[] bArr2, byte[] bArr3) throws KeyStoreException;

    long getConsumedInputSizeBytes();

    long getProducedOutputSizeBytes();

    byte[] update(byte[] bArr, int i, int i2) throws KeyStoreException;
}
