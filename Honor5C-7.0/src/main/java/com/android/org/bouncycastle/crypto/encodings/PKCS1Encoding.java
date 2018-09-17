package com.android.org.bouncycastle.crypto.encodings;

import com.android.org.bouncycastle.crypto.AsymmetricBlockCipher;
import com.android.org.bouncycastle.crypto.CipherParameters;
import com.android.org.bouncycastle.crypto.InvalidCipherTextException;
import com.android.org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import com.android.org.bouncycastle.crypto.params.ParametersWithRandom;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.SecureRandom;

public class PKCS1Encoding implements AsymmetricBlockCipher {
    private static final int HEADER_LENGTH = 10;
    public static final String NOT_STRICT_LENGTH_ENABLED_PROPERTY = "org.bouncycastle.pkcs1.not_strict";
    public static final String STRICT_LENGTH_ENABLED_PROPERTY = "org.bouncycastle.pkcs1.strict";
    private AsymmetricBlockCipher engine;
    private byte[] fallback;
    private boolean forEncryption;
    private boolean forPrivateKey;
    private int pLen;
    private SecureRandom random;
    private boolean useStrictLength;

    private static int checkPkcs1Encoding(byte[] r1, int r2) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.bouncycastle.crypto.encodings.PKCS1Encoding.checkPkcs1Encoding(byte[], int):int
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.crypto.encodings.PKCS1Encoding.checkPkcs1Encoding(byte[], int):int");
    }

    private byte[] decodeBlockOrRandom(byte[] r1, int r2, int r3) throws com.android.org.bouncycastle.crypto.InvalidCipherTextException {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.bouncycastle.crypto.encodings.PKCS1Encoding.decodeBlockOrRandom(byte[], int, int):byte[]
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.crypto.encodings.PKCS1Encoding.decodeBlockOrRandom(byte[], int, int):byte[]");
    }

    public PKCS1Encoding(AsymmetricBlockCipher cipher) {
        this.pLen = -1;
        this.fallback = null;
        this.engine = cipher;
        this.useStrictLength = useStrict();
    }

    public PKCS1Encoding(AsymmetricBlockCipher cipher, int pLen) {
        this.pLen = -1;
        this.fallback = null;
        this.engine = cipher;
        this.useStrictLength = useStrict();
        this.pLen = pLen;
    }

    public PKCS1Encoding(AsymmetricBlockCipher cipher, byte[] fallback) {
        this.pLen = -1;
        this.fallback = null;
        this.engine = cipher;
        this.useStrictLength = useStrict();
        this.fallback = fallback;
        this.pLen = fallback.length;
    }

    private boolean useStrict() {
        boolean z = true;
        String strict = (String) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return System.getProperty("com.android.org.bouncycastle.pkcs1.strict");
            }
        });
        String notStrict = (String) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return System.getProperty("com.android.org.bouncycastle.pkcs1.not_strict");
            }
        });
        if (notStrict != null) {
            if (notStrict.equals("true")) {
                z = false;
            }
            return z;
        }
        if (strict != null) {
            z = strict.equals("true");
        }
        return z;
    }

    public AsymmetricBlockCipher getUnderlyingCipher() {
        return this.engine;
    }

    public void init(boolean forEncryption, CipherParameters param) {
        AsymmetricKeyParameter kParam;
        if (param instanceof ParametersWithRandom) {
            ParametersWithRandom rParam = (ParametersWithRandom) param;
            this.random = rParam.getRandom();
            kParam = (AsymmetricKeyParameter) rParam.getParameters();
        } else {
            kParam = (AsymmetricKeyParameter) param;
            if (!kParam.isPrivate() && forEncryption) {
                this.random = new SecureRandom();
            }
        }
        this.engine.init(forEncryption, param);
        this.forPrivateKey = kParam.isPrivate();
        this.forEncryption = forEncryption;
    }

    public int getInputBlockSize() {
        int baseBlockSize = this.engine.getInputBlockSize();
        if (this.forEncryption) {
            return baseBlockSize - 10;
        }
        return baseBlockSize;
    }

    public int getOutputBlockSize() {
        int baseBlockSize = this.engine.getOutputBlockSize();
        if (this.forEncryption) {
            return baseBlockSize;
        }
        return baseBlockSize - 10;
    }

    public byte[] processBlock(byte[] in, int inOff, int inLen) throws InvalidCipherTextException {
        if (this.forEncryption) {
            return encodeBlock(in, inOff, inLen);
        }
        return decodeBlock(in, inOff, inLen);
    }

    private byte[] encodeBlock(byte[] in, int inOff, int inLen) throws InvalidCipherTextException {
        if (inLen > getInputBlockSize()) {
            throw new IllegalArgumentException("input data too large");
        }
        byte[] block = new byte[this.engine.getInputBlockSize()];
        int i;
        if (this.forPrivateKey) {
            block[0] = (byte) 1;
            for (i = 1; i != (block.length - inLen) - 1; i++) {
                block[i] = (byte) -1;
            }
        } else {
            this.random.nextBytes(block);
            block[0] = (byte) 2;
            for (i = 1; i != (block.length - inLen) - 1; i++) {
                while (block[i] == null) {
                    block[i] = (byte) this.random.nextInt();
                }
            }
        }
        block[(block.length - inLen) - 1] = (byte) 0;
        System.arraycopy(in, inOff, block, block.length - inLen, inLen);
        return this.engine.processBlock(block, 0, block.length);
    }

    private byte[] decodeBlock(byte[] in, int inOff, int inLen) throws InvalidCipherTextException {
        if (this.pLen != -1) {
            return decodeBlockOrRandom(in, inOff, inLen);
        }
        byte[] block = this.engine.processBlock(in, inOff, inLen);
        if (block.length < getOutputBlockSize()) {
            throw new InvalidCipherTextException("block truncated");
        }
        byte type = block[0];
        if (this.forPrivateKey) {
            if (type != (byte) 2) {
                throw new InvalidCipherTextException("unknown block type");
            }
        } else if (type != (byte) 1) {
            throw new InvalidCipherTextException("unknown block type");
        }
        if ((type == (byte) 1 && this.forPrivateKey) || (type == (byte) 2 && !this.forPrivateKey)) {
            throw new InvalidCipherTextException("invalid block type " + type);
        } else if (!this.useStrictLength || block.length == this.engine.getOutputBlockSize()) {
            int start = 1;
            while (start != block.length) {
                byte pad = block[start];
                if (pad == null) {
                    break;
                } else if (type != (byte) 1 || pad == (byte) -1) {
                    start++;
                } else {
                    throw new InvalidCipherTextException("block padding incorrect");
                }
            }
            start++;
            if (start > block.length || start < HEADER_LENGTH) {
                throw new InvalidCipherTextException("no data in block");
            }
            byte[] result = new byte[(block.length - start)];
            System.arraycopy(block, start, result, 0, result.length);
            return result;
        } else {
            throw new InvalidCipherTextException("block incorrect size");
        }
    }
}
