package com.android.org.conscrypt;

import com.android.org.conscrypt.NativeRef.EVP_MD_CTX;
import java.nio.ByteBuffer;
import java.security.MessageDigestSpi;
import java.security.NoSuchAlgorithmException;

public class OpenSSLMessageDigestJDK extends MessageDigestSpi implements Cloneable {
    private final EVP_MD_CTX ctx;
    private boolean digestInitializedInContext;
    private final long evp_md;
    private final byte[] singleByte;
    private final int size;

    public static class MD5 extends OpenSSLMessageDigestJDK {
        public MD5() throws NoSuchAlgorithmException {
            super(com.android.org.conscrypt.EvpMdRef.MD5.EVP_MD, com.android.org.conscrypt.EvpMdRef.MD5.SIZE_BYTES, null);
        }
    }

    public static class SHA1 extends OpenSSLMessageDigestJDK {
        public SHA1() throws NoSuchAlgorithmException {
            super(com.android.org.conscrypt.EvpMdRef.SHA1.EVP_MD, com.android.org.conscrypt.EvpMdRef.SHA1.SIZE_BYTES, null);
        }
    }

    public static class SHA224 extends OpenSSLMessageDigestJDK {
        public SHA224() throws NoSuchAlgorithmException {
            super(com.android.org.conscrypt.EvpMdRef.SHA224.EVP_MD, com.android.org.conscrypt.EvpMdRef.SHA224.SIZE_BYTES, null);
        }
    }

    public static class SHA256 extends OpenSSLMessageDigestJDK {
        public SHA256() throws NoSuchAlgorithmException {
            super(com.android.org.conscrypt.EvpMdRef.SHA256.EVP_MD, com.android.org.conscrypt.EvpMdRef.SHA256.SIZE_BYTES, null);
        }
    }

    public static class SHA384 extends OpenSSLMessageDigestJDK {
        public SHA384() throws NoSuchAlgorithmException {
            super(com.android.org.conscrypt.EvpMdRef.SHA384.EVP_MD, com.android.org.conscrypt.EvpMdRef.SHA384.SIZE_BYTES, null);
        }
    }

    public static class SHA512 extends OpenSSLMessageDigestJDK {
        public SHA512() throws NoSuchAlgorithmException {
            super(com.android.org.conscrypt.EvpMdRef.SHA512.EVP_MD, com.android.org.conscrypt.EvpMdRef.SHA512.SIZE_BYTES, null);
        }
    }

    /* synthetic */ OpenSSLMessageDigestJDK(long evp_md, int size, OpenSSLMessageDigestJDK -this2) {
        this(evp_md, size);
    }

    private OpenSSLMessageDigestJDK(long evp_md, int size) throws NoSuchAlgorithmException {
        this.singleByte = new byte[1];
        this.evp_md = evp_md;
        this.size = size;
        this.ctx = new EVP_MD_CTX(NativeCrypto.EVP_MD_CTX_create());
    }

    private OpenSSLMessageDigestJDK(long evp_md, int size, EVP_MD_CTX ctx, boolean digestInitializedInContext) {
        this.singleByte = new byte[1];
        this.evp_md = evp_md;
        this.size = size;
        this.ctx = ctx;
        this.digestInitializedInContext = digestInitializedInContext;
    }

    private void ensureDigestInitializedInContext() {
        if (!this.digestInitializedInContext) {
            NativeCrypto.EVP_DigestInit_ex(this.ctx, this.evp_md);
            this.digestInitializedInContext = true;
        }
    }

    protected void engineReset() {
        NativeCrypto.EVP_MD_CTX_cleanup(this.ctx);
        this.digestInitializedInContext = false;
    }

    protected int engineGetDigestLength() {
        return this.size;
    }

    protected void engineUpdate(byte input) {
        this.singleByte[0] = input;
        engineUpdate(this.singleByte, 0, 1);
    }

    protected void engineUpdate(byte[] input, int offset, int len) {
        ensureDigestInitializedInContext();
        NativeCrypto.EVP_DigestUpdate(this.ctx, input, offset, len);
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
            ensureDigestInitializedInContext();
            NativeCrypto.EVP_DigestUpdateDirect(this.ctx, ptr, len);
            input.position(position + len);
            return;
        }
        super.engineUpdate(input);
    }

    protected byte[] engineDigest() {
        ensureDigestInitializedInContext();
        byte[] result = new byte[this.size];
        NativeCrypto.EVP_DigestFinal_ex(this.ctx, result, 0);
        this.digestInitializedInContext = false;
        return result;
    }

    public Object clone() {
        EVP_MD_CTX ctxCopy = new EVP_MD_CTX(NativeCrypto.EVP_MD_CTX_create());
        if (this.digestInitializedInContext) {
            NativeCrypto.EVP_MD_CTX_copy_ex(ctxCopy, this.ctx);
        }
        return new OpenSSLMessageDigestJDK(this.evp_md, this.size, ctxCopy, this.digestInitializedInContext);
    }
}
