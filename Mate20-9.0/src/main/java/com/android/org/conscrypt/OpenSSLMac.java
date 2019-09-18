package com.android.org.conscrypt;

import com.android.org.conscrypt.EvpMdRef;
import com.android.org.conscrypt.NativeRef;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.MacSpi;
import javax.crypto.SecretKey;

public abstract class OpenSSLMac extends MacSpi {
    private NativeRef.HMAC_CTX ctx;
    private final long evp_md;
    private byte[] keyBytes;
    private final byte[] singleByte;
    private final int size;

    public static final class HmacMD5 extends OpenSSLMac {
        public HmacMD5() {
            super(EvpMdRef.MD5.EVP_MD, EvpMdRef.MD5.SIZE_BYTES);
        }
    }

    public static final class HmacSHA1 extends OpenSSLMac {
        public HmacSHA1() {
            super(EvpMdRef.SHA1.EVP_MD, EvpMdRef.SHA1.SIZE_BYTES);
        }
    }

    public static final class HmacSHA224 extends OpenSSLMac {
        public HmacSHA224() throws NoSuchAlgorithmException {
            super(EvpMdRef.SHA224.EVP_MD, EvpMdRef.SHA224.SIZE_BYTES);
        }
    }

    public static final class HmacSHA256 extends OpenSSLMac {
        public HmacSHA256() throws NoSuchAlgorithmException {
            super(EvpMdRef.SHA256.EVP_MD, EvpMdRef.SHA256.SIZE_BYTES);
        }
    }

    public static final class HmacSHA384 extends OpenSSLMac {
        public HmacSHA384() throws NoSuchAlgorithmException {
            super(EvpMdRef.SHA384.EVP_MD, EvpMdRef.SHA384.SIZE_BYTES);
        }
    }

    public static final class HmacSHA512 extends OpenSSLMac {
        public HmacSHA512() {
            super(EvpMdRef.SHA512.EVP_MD, EvpMdRef.SHA512.SIZE_BYTES);
        }
    }

    private OpenSSLMac(long evp_md2, int size2) {
        this.singleByte = new byte[1];
        this.evp_md = evp_md2;
        this.size = size2;
    }

    /* access modifiers changed from: protected */
    public int engineGetMacLength() {
        return this.size;
    }

    /* access modifiers changed from: protected */
    public void engineInit(Key key, AlgorithmParameterSpec params) throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (!(key instanceof SecretKey)) {
            throw new InvalidKeyException("key must be a SecretKey");
        } else if (params == null) {
            this.keyBytes = key.getEncoded();
            if (this.keyBytes != null) {
                resetContext();
                return;
            }
            throw new InvalidKeyException("key cannot be encoded");
        } else {
            throw new InvalidAlgorithmParameterException("unknown parameter type");
        }
    }

    private final void resetContext() {
        NativeRef.HMAC_CTX ctxLocal = new NativeRef.HMAC_CTX(NativeCrypto.HMAC_CTX_new());
        if (this.keyBytes != null) {
            NativeCrypto.HMAC_Init_ex(ctxLocal, this.keyBytes, this.evp_md);
        }
        this.ctx = ctxLocal;
    }

    /* access modifiers changed from: protected */
    public void engineUpdate(byte input) {
        this.singleByte[0] = input;
        engineUpdate(this.singleByte, 0, 1);
    }

    /* access modifiers changed from: protected */
    public void engineUpdate(byte[] input, int offset, int len) {
        NativeCrypto.HMAC_Update(this.ctx, input, offset, len);
    }

    /* access modifiers changed from: protected */
    public void engineUpdate(ByteBuffer input) {
        if (input.hasRemaining()) {
            if (!input.isDirect()) {
                super.engineUpdate(input);
                return;
            }
            long baseAddress = NativeCrypto.getDirectBufferAddress(input);
            if (baseAddress == 0) {
                super.engineUpdate(input);
                return;
            }
            int position = input.position();
            if (position >= 0) {
                long ptr = ((long) position) + baseAddress;
                int len = input.remaining();
                if (len >= 0) {
                    NativeCrypto.HMAC_UpdateDirect(this.ctx, ptr, len);
                    input.position(position + len);
                    return;
                }
                throw new RuntimeException("Negative remaining amount");
            }
            throw new RuntimeException("Negative position");
        }
    }

    /* access modifiers changed from: protected */
    public byte[] engineDoFinal() {
        byte[] output = NativeCrypto.HMAC_Final(this.ctx);
        resetContext();
        return output;
    }

    /* access modifiers changed from: protected */
    public void engineReset() {
        resetContext();
    }
}
