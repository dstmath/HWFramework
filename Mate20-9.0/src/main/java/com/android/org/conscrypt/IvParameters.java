package com.android.org.conscrypt;

import java.io.IOException;
import java.security.AlgorithmParametersSpi;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import javax.crypto.spec.IvParameterSpec;

public class IvParameters extends AlgorithmParametersSpi {
    private byte[] iv;

    public static class AES extends IvParameters {
    }

    public static class ChaCha20 extends IvParameters {
    }

    public static class DESEDE extends IvParameters {
    }

    /* access modifiers changed from: protected */
    public void engineInit(AlgorithmParameterSpec algorithmParameterSpec) throws InvalidParameterSpecException {
        if (algorithmParameterSpec instanceof IvParameterSpec) {
            this.iv = (byte[]) ((IvParameterSpec) algorithmParameterSpec).getIV().clone();
            return;
        }
        throw new InvalidParameterSpecException("Only IvParameterSpec is supported");
    }

    /* access modifiers changed from: protected */
    public void engineInit(byte[] bytes) throws IOException {
        long readRef = 0;
        try {
            readRef = NativeCrypto.asn1_read_init(bytes);
            byte[] newIv = NativeCrypto.asn1_read_octetstring(readRef);
            if (NativeCrypto.asn1_read_is_empty(readRef)) {
                this.iv = newIv;
                return;
            }
            throw new IOException("Error reading ASN.1 encoding");
        } finally {
            NativeCrypto.asn1_read_free(readRef);
        }
    }

    /* access modifiers changed from: protected */
    public void engineInit(byte[] bytes, String format) throws IOException {
        if (format == null || format.equals("ASN.1")) {
            engineInit(bytes);
        } else if (format.equals("RAW")) {
            this.iv = (byte[]) bytes.clone();
        } else {
            throw new IOException("Unsupported format: " + format);
        }
    }

    /* access modifiers changed from: protected */
    public <T extends AlgorithmParameterSpec> T engineGetParameterSpec(Class<T> aClass) throws InvalidParameterSpecException {
        if (aClass == IvParameterSpec.class) {
            return new IvParameterSpec(this.iv);
        }
        throw new InvalidParameterSpecException("Incompatible AlgorithmParametersSpec class: " + aClass);
    }

    /* access modifiers changed from: protected */
    public byte[] engineGetEncoded() throws IOException {
        try {
            long cbbRef = NativeCrypto.asn1_write_init();
            NativeCrypto.asn1_write_octetstring(cbbRef, this.iv);
            byte[] asn1_write_finish = NativeCrypto.asn1_write_finish(cbbRef);
            NativeCrypto.asn1_write_free(cbbRef);
            return asn1_write_finish;
        } catch (IOException e) {
            NativeCrypto.asn1_write_cleanup(0);
            throw e;
        } catch (Throwable th) {
            NativeCrypto.asn1_write_free(0);
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    public byte[] engineGetEncoded(String format) throws IOException {
        if (format == null || format.equals("ASN.1")) {
            return engineGetEncoded();
        }
        if (format.equals("RAW")) {
            return (byte[]) this.iv.clone();
        }
        throw new IOException("Unsupported format: " + format);
    }

    /* access modifiers changed from: protected */
    public String engineToString() {
        return "Conscrypt IV AlgorithmParameters";
    }
}
