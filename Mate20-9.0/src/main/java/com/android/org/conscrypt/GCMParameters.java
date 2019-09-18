package com.android.org.conscrypt;

import java.io.IOException;
import java.security.AlgorithmParametersSpi;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;

public final class GCMParameters extends AlgorithmParametersSpi {
    private static final int DEFAULT_TLEN = 96;
    private byte[] iv;
    private int tLen;

    public GCMParameters() {
    }

    GCMParameters(int tLen2, byte[] iv2) {
        this.tLen = tLen2;
        this.iv = iv2;
    }

    /* access modifiers changed from: package-private */
    public int getTLen() {
        return this.tLen;
    }

    /* access modifiers changed from: package-private */
    public byte[] getIV() {
        return this.iv;
    }

    /* access modifiers changed from: protected */
    public void engineInit(AlgorithmParameterSpec algorithmParameterSpec) throws InvalidParameterSpecException {
        GCMParameters params = Platform.fromGCMParameterSpec(algorithmParameterSpec);
        if (params != null) {
            this.tLen = params.tLen;
            this.iv = params.iv;
            return;
        }
        throw new InvalidParameterSpecException("Only GCMParameterSpec is supported");
    }

    /* access modifiers changed from: protected */
    public void engineInit(byte[] bytes) throws IOException {
        long readRef = 0;
        long seqRef = 0;
        try {
            readRef = NativeCrypto.asn1_read_init(bytes);
            seqRef = NativeCrypto.asn1_read_sequence(readRef);
            byte[] newIv = NativeCrypto.asn1_read_octetstring(seqRef);
            int newTlen = DEFAULT_TLEN;
            if (!NativeCrypto.asn1_read_is_empty(seqRef)) {
                newTlen = 8 * ((int) NativeCrypto.asn1_read_uint64(seqRef));
            }
            if (!NativeCrypto.asn1_read_is_empty(seqRef) || !NativeCrypto.asn1_read_is_empty(readRef)) {
                throw new IOException("Error reading ASN.1 encoding");
            }
            this.iv = newIv;
            this.tLen = newTlen;
        } finally {
            NativeCrypto.asn1_read_free(seqRef);
            NativeCrypto.asn1_read_free(readRef);
        }
    }

    /* access modifiers changed from: protected */
    public void engineInit(byte[] bytes, String format) throws IOException {
        if (format == null || format.equals("ASN.1")) {
            engineInit(bytes);
            return;
        }
        throw new IOException("Unsupported format: " + format);
    }

    /* access modifiers changed from: protected */
    public <T extends AlgorithmParameterSpec> T engineGetParameterSpec(Class<T> aClass) throws InvalidParameterSpecException {
        if (aClass != null && aClass.getName().equals("javax.crypto.spec.GCMParameterSpec")) {
            return (AlgorithmParameterSpec) aClass.cast(Platform.toGCMParameterSpec(this.tLen, this.iv));
        }
        throw new InvalidParameterSpecException("Unsupported class: " + aClass);
    }

    /* access modifiers changed from: protected */
    public byte[] engineGetEncoded() throws IOException {
        try {
            long cbbRef = NativeCrypto.asn1_write_init();
            long seqRef = NativeCrypto.asn1_write_sequence(cbbRef);
            NativeCrypto.asn1_write_octetstring(seqRef, this.iv);
            if (this.tLen != DEFAULT_TLEN) {
                NativeCrypto.asn1_write_uint64(seqRef, (long) (this.tLen / 8));
            }
            byte[] asn1_write_finish = NativeCrypto.asn1_write_finish(cbbRef);
            NativeCrypto.asn1_write_free(seqRef);
            NativeCrypto.asn1_write_free(cbbRef);
            return asn1_write_finish;
        } catch (IOException e) {
            NativeCrypto.asn1_write_cleanup(0);
            throw e;
        } catch (Throwable th) {
            NativeCrypto.asn1_write_free(0);
            NativeCrypto.asn1_write_free(0);
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    public byte[] engineGetEncoded(String format) throws IOException {
        if (format == null || format.equals("ASN.1")) {
            return engineGetEncoded();
        }
        throw new IOException("Unsupported format: " + format);
    }

    /* access modifiers changed from: protected */
    public String engineToString() {
        return "Conscrypt GCM AlgorithmParameters";
    }
}
