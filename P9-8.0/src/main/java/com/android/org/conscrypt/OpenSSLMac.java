package com.android.org.conscrypt;

import com.android.org.conscrypt.EvpMdRef.MD5;
import com.android.org.conscrypt.EvpMdRef.SHA1;
import com.android.org.conscrypt.EvpMdRef.SHA224;
import com.android.org.conscrypt.EvpMdRef.SHA256;
import com.android.org.conscrypt.EvpMdRef.SHA384;
import com.android.org.conscrypt.EvpMdRef.SHA512;
import com.android.org.conscrypt.NativeRef.HMAC_CTX;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.MacSpi;
import javax.crypto.SecretKey;

public abstract class OpenSSLMac extends MacSpi {
    private HMAC_CTX ctx;
    private final long evp_md;
    private byte[] keyBytes;
    private final byte[] singleByte;
    private final int size;

    public static class HmacMD5 extends OpenSSLMac {
        public HmacMD5() {
            super(MD5.EVP_MD, MD5.SIZE_BYTES, null);
        }
    }

    public static class HmacSHA1 extends OpenSSLMac {
        public HmacSHA1() {
            super(SHA1.EVP_MD, SHA1.SIZE_BYTES, null);
        }
    }

    public static class HmacSHA224 extends OpenSSLMac {
        public HmacSHA224() throws NoSuchAlgorithmException {
            super(SHA224.EVP_MD, SHA224.SIZE_BYTES, null);
        }
    }

    public static class HmacSHA256 extends OpenSSLMac {
        public HmacSHA256() throws NoSuchAlgorithmException {
            super(SHA256.EVP_MD, SHA256.SIZE_BYTES, null);
        }
    }

    public static class HmacSHA384 extends OpenSSLMac {
        public HmacSHA384() throws NoSuchAlgorithmException {
            super(SHA384.EVP_MD, SHA384.SIZE_BYTES, null);
        }
    }

    public static class HmacSHA512 extends OpenSSLMac {
        public HmacSHA512() {
            super(SHA512.EVP_MD, SHA512.SIZE_BYTES, null);
        }
    }

    /* synthetic */ OpenSSLMac(long evp_md, int size, OpenSSLMac -this2) {
        this(evp_md, size);
    }

    private OpenSSLMac(long evp_md, int size) {
        this.singleByte = new byte[1];
        this.evp_md = evp_md;
        this.size = size;
    }

    protected int engineGetMacLength() {
        return this.size;
    }

    protected void engineInit(Key key, AlgorithmParameterSpec params) throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (!(key instanceof SecretKey)) {
            throw new InvalidKeyException("key must be a SecretKey");
        } else if (params != null) {
            throw new InvalidAlgorithmParameterException("unknown parameter type");
        } else {
            this.keyBytes = key.getEncoded();
            if (this.keyBytes == null) {
                throw new InvalidKeyException("key cannot be encoded");
            }
            resetContext();
        }
    }

    private final void resetContext() {
        HMAC_CTX ctxLocal = new HMAC_CTX(NativeCrypto.HMAC_CTX_new());
        if (this.keyBytes != null) {
            NativeCrypto.HMAC_Init_ex(ctxLocal, this.keyBytes, this.evp_md);
        }
        this.ctx = ctxLocal;
    }

    protected void engineUpdate(byte input) {
        this.singleByte[0] = input;
        engineUpdate(this.singleByte, 0, 1);
    }

    protected void engineUpdate(byte[] input, int offset, int len) {
        NativeCrypto.HMAC_Update(this.ctx, input, offset, len);
    }

    protected void engineUpdate(ByteBuffer input) {
        if (!input.hasRemaining()) {
            return;
        }
        if (input.isDirect()) {
            long baseAddress = NativeCrypto.getDirectBufferAddress(input);
            if (baseAddress == 0) {
                super.engineUpdate(input);
                return;
            }
            int position = input.position();
            if (position < 0) {
                throw new RuntimeException("Negative position");
            }
            long ptr = baseAddress + ((long) position);
            int len = input.remaining();
            if (len < 0) {
                throw new RuntimeException("Negative remaining amount");
            }
            NativeCrypto.HMAC_UpdateDirect(this.ctx, ptr, len);
            input.position(position + len);
            return;
        }
        super.engineUpdate(input);
    }

    protected byte[] engineDoFinal() {
        byte[] output = NativeCrypto.HMAC_Final(this.ctx);
        resetContext();
        return output;
    }

    protected void engineReset() {
        resetContext();
    }
}
