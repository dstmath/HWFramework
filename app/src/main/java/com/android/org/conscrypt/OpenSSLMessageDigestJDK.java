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
        private static final long EVP_MD = 0;
        private static final int SIZE = 0;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.conscrypt.OpenSSLMessageDigestJDK.MD5.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.conscrypt.OpenSSLMessageDigestJDK.MD5.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLMessageDigestJDK.MD5.<clinit>():void");
        }

        public MD5() throws NoSuchAlgorithmException {
            super(EVP_MD, SIZE, null);
        }
    }

    public static class SHA1 extends OpenSSLMessageDigestJDK {
        private static final long EVP_MD = 0;
        private static final int SIZE = 0;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.conscrypt.OpenSSLMessageDigestJDK.SHA1.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.conscrypt.OpenSSLMessageDigestJDK.SHA1.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLMessageDigestJDK.SHA1.<clinit>():void");
        }

        public SHA1() throws NoSuchAlgorithmException {
            super(EVP_MD, SIZE, null);
        }
    }

    public static class SHA224 extends OpenSSLMessageDigestJDK {
        private static final long EVP_MD = 0;
        private static final int SIZE = 0;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.conscrypt.OpenSSLMessageDigestJDK.SHA224.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.conscrypt.OpenSSLMessageDigestJDK.SHA224.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLMessageDigestJDK.SHA224.<clinit>():void");
        }

        public SHA224() throws NoSuchAlgorithmException {
            super(EVP_MD, SIZE, null);
        }
    }

    public static class SHA256 extends OpenSSLMessageDigestJDK {
        private static final long EVP_MD = 0;
        private static final int SIZE = 0;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.conscrypt.OpenSSLMessageDigestJDK.SHA256.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.conscrypt.OpenSSLMessageDigestJDK.SHA256.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLMessageDigestJDK.SHA256.<clinit>():void");
        }

        public SHA256() throws NoSuchAlgorithmException {
            super(EVP_MD, SIZE, null);
        }
    }

    public static class SHA384 extends OpenSSLMessageDigestJDK {
        private static final long EVP_MD = 0;
        private static final int SIZE = 0;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.conscrypt.OpenSSLMessageDigestJDK.SHA384.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.conscrypt.OpenSSLMessageDigestJDK.SHA384.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLMessageDigestJDK.SHA384.<clinit>():void");
        }

        public SHA384() throws NoSuchAlgorithmException {
            super(EVP_MD, SIZE, null);
        }
    }

    public static class SHA512 extends OpenSSLMessageDigestJDK {
        private static final long EVP_MD = 0;
        private static final int SIZE = 0;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.conscrypt.OpenSSLMessageDigestJDK.SHA512.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.conscrypt.OpenSSLMessageDigestJDK.SHA512.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLMessageDigestJDK.SHA512.<clinit>():void");
        }

        public SHA512() throws NoSuchAlgorithmException {
            super(EVP_MD, SIZE, null);
        }
    }

    /* synthetic */ OpenSSLMessageDigestJDK(long evp_md, int size, OpenSSLMessageDigestJDK openSSLMessageDigestJDK) {
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
