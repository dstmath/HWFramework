package com.android.org.conscrypt;

import java.io.ByteArrayOutputStream;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.SignatureSpi;

public class OpenSSLSignatureRawECDSA extends SignatureSpi {
    private ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private OpenSSLKey key;

    /* access modifiers changed from: protected */
    public void engineUpdate(byte input) {
        this.buffer.write(input);
    }

    /* access modifiers changed from: protected */
    public void engineUpdate(byte[] input, int offset, int len) {
        this.buffer.write(input, offset, len);
    }

    /* access modifiers changed from: protected */
    public Object engineGetParameter(String param) throws InvalidParameterException {
        return null;
    }

    private static OpenSSLKey verifyKey(OpenSSLKey key2) throws InvalidKeyException {
        if (NativeCrypto.EVP_PKEY_type(key2.getNativeRef()) == 408) {
            return key2;
        }
        throw new InvalidKeyException("Non-EC key used to initialize EC signature.");
    }

    /* access modifiers changed from: protected */
    public void engineInitSign(PrivateKey privateKey) throws InvalidKeyException {
        this.key = verifyKey(OpenSSLKey.fromPrivateKey(privateKey));
    }

    /* access modifiers changed from: protected */
    public void engineInitVerify(PublicKey publicKey) throws InvalidKeyException {
        this.key = verifyKey(OpenSSLKey.fromPublicKey(publicKey));
    }

    /* access modifiers changed from: protected */
    public void engineSetParameter(String param, Object value) throws InvalidParameterException {
    }

    /* access modifiers changed from: protected */
    public byte[] engineSign() throws SignatureException {
        if (this.key != null) {
            int output_size = NativeCrypto.ECDSA_size(this.key.getNativeRef());
            byte[] outputBuffer = new byte[output_size];
            try {
                int bytes_written = NativeCrypto.ECDSA_sign(this.buffer.toByteArray(), outputBuffer, this.key.getNativeRef());
                if (bytes_written >= 0) {
                    if (bytes_written != output_size) {
                        byte[] newBuffer = new byte[bytes_written];
                        System.arraycopy(outputBuffer, 0, newBuffer, 0, bytes_written);
                        outputBuffer = newBuffer;
                    }
                    this.buffer.reset();
                    return outputBuffer;
                }
                throw new SignatureException("Could not compute signature.");
            } catch (Exception ex) {
                throw new SignatureException(ex);
            } catch (Throwable th) {
                this.buffer.reset();
                throw th;
            }
        } else {
            throw new SignatureException("No key provided");
        }
    }

    /* access modifiers changed from: protected */
    public boolean engineVerify(byte[] sigBytes) throws SignatureException {
        if (this.key != null) {
            try {
                int result = NativeCrypto.ECDSA_verify(this.buffer.toByteArray(), sigBytes, this.key.getNativeRef());
                if (result != -1) {
                    boolean z = true;
                    if (result != 1) {
                        z = false;
                    }
                    this.buffer.reset();
                    return z;
                }
                throw new SignatureException("Could not verify signature.");
            } catch (Exception ex) {
                throw new SignatureException(ex);
            } catch (Throwable th) {
                this.buffer.reset();
                throw th;
            }
        } else {
            throw new SignatureException("No key provided");
        }
    }
}
