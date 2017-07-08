package sun.security.ssl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Hashtable;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import sun.misc.HexDumpEncoder;

final class CipherBox {
    static final CipherBox NULL = null;
    private static final Debug debug = null;
    private static Hashtable<Integer, IvParameterSpec> masks;
    private int blockSize;
    private final Cipher cipher;
    private final boolean isCBCMode;
    private final ProtocolVersion protocolVersion;
    private SecureRandom random;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.ssl.CipherBox.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.ssl.CipherBox.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.security.ssl.CipherBox.<clinit>():void");
    }

    private CipherBox() {
        this.protocolVersion = ProtocolVersion.DEFAULT;
        this.cipher = null;
        this.isCBCMode = false;
    }

    private CipherBox(ProtocolVersion protocolVersion, BulkCipher bulkCipher, SecretKey key, IvParameterSpec iv, SecureRandom random, boolean encrypt) throws NoSuchAlgorithmException {
        try {
            AlgorithmParameterSpec iv2;
            this.protocolVersion = protocolVersion;
            this.cipher = JsseJce.getCipher(bulkCipher.transformation);
            int mode = encrypt ? 1 : 2;
            if (random == null) {
                random = JsseJce.getSecureRandom();
            }
            this.random = random;
            this.isCBCMode = bulkCipher.isCBCMode;
            if (iv == null && bulkCipher.ivSize != 0 && mode == 2 && protocolVersion.v >= ProtocolVersion.TLS11.v) {
                iv2 = getFixedMask(bulkCipher.ivSize);
            }
            this.cipher.init(mode, (Key) key, iv2, random);
            this.blockSize = this.cipher.getBlockSize();
            if (this.blockSize == 1) {
                this.blockSize = 0;
            }
        } catch (NoSuchAlgorithmException e) {
            throw e;
        } catch (Exception e2) {
            throw new NoSuchAlgorithmException("Could not create cipher " + bulkCipher, e2);
        } catch (ExceptionInInitializerError e3) {
            throw new NoSuchAlgorithmException("Could not create cipher " + bulkCipher, e3);
        }
    }

    static CipherBox newCipherBox(ProtocolVersion version, BulkCipher cipher, SecretKey key, IvParameterSpec iv, SecureRandom random, boolean encrypt) throws NoSuchAlgorithmException {
        if (!cipher.allowed) {
            throw new NoSuchAlgorithmException("Unsupported cipher " + cipher);
        } else if (cipher == CipherSuite.B_NULL) {
            return NULL;
        } else {
            return new CipherBox(version, cipher, key, iv, random, encrypt);
        }
    }

    private static IvParameterSpec getFixedMask(int ivSize) {
        if (masks == null) {
            masks = new Hashtable(5);
        }
        IvParameterSpec iv = (IvParameterSpec) masks.get(Integer.valueOf(ivSize));
        if (iv != null) {
            return iv;
        }
        iv = new IvParameterSpec(new byte[ivSize]);
        masks.put(Integer.valueOf(ivSize), iv);
        return iv;
    }

    int encrypt(byte[] buf, int offset, int len) {
        if (this.cipher == null) {
            return len;
        }
        try {
            if (this.blockSize != 0) {
                if (this.protocolVersion.v >= ProtocolVersion.TLS11.v) {
                    byte[] prefix = new byte[this.blockSize];
                    this.random.nextBytes(prefix);
                    System.arraycopy(buf, offset, buf, prefix.length + offset, len);
                    System.arraycopy(prefix, 0, buf, offset, prefix.length);
                    len += prefix.length;
                }
                len = addPadding(buf, offset, len, this.blockSize);
            }
            if (debug != null && Debug.isOn("plaintext")) {
                try {
                    HexDumpEncoder hd = new HexDumpEncoder();
                    System.out.println("Padded plaintext before ENCRYPTION:  len = " + len);
                    hd.encodeBuffer(new ByteArrayInputStream(buf, offset, len), System.out);
                } catch (IOException e) {
                }
            }
            int newLen = this.cipher.update(buf, offset, len, buf, offset);
            if (newLen == len) {
                return newLen;
            }
            throw new RuntimeException("Cipher buffering error in JCE provider " + this.cipher.getProvider().getName());
        } catch (ShortBufferException e2) {
            throw new ArrayIndexOutOfBoundsException(e2.toString());
        }
    }

    int encrypt(ByteBuffer bb) {
        int len = bb.remaining();
        if (this.cipher == null) {
            bb.position(bb.limit());
            return len;
        }
        try {
            int pos = bb.position();
            if (this.blockSize != 0) {
                if (this.protocolVersion.v >= ProtocolVersion.TLS11.v) {
                    byte[] prefix = new byte[this.blockSize];
                    this.random.nextBytes(prefix);
                    int limit = bb.limit();
                    byte[] buf;
                    if (bb.hasArray()) {
                        int arrayOffset = bb.arrayOffset();
                        buf = bb.array();
                        System.arraycopy(buf, arrayOffset + pos, buf, (arrayOffset + pos) + prefix.length, limit - pos);
                        bb.limit(prefix.length + limit);
                    } else {
                        buf = new byte[(limit - pos)];
                        bb.get(buf, 0, limit - pos);
                        bb.position(prefix.length + pos);
                        bb.limit(prefix.length + limit);
                        bb.put(buf);
                    }
                    bb.position(pos);
                    bb.put(prefix);
                    bb.position(pos);
                }
                len = addPadding(bb, this.blockSize);
                bb.position(pos);
            }
            if (debug != null && Debug.isOn("plaintext")) {
                try {
                    HexDumpEncoder hd = new HexDumpEncoder();
                    System.out.println("Padded plaintext before ENCRYPTION:  len = " + len);
                    hd.encodeBuffer(bb, System.out);
                } catch (IOException e) {
                }
                bb.position(pos);
            }
            ByteBuffer dup = bb.duplicate();
            int newLen = this.cipher.update(dup, bb);
            if (bb.position() != dup.position()) {
                throw new RuntimeException("bytebuffer padding error");
            } else if (newLen == len) {
                return newLen;
            } else {
                throw new RuntimeException("Cipher buffering error in JCE provider " + this.cipher.getProvider().getName());
            }
        } catch (ShortBufferException e2) {
            RuntimeException exc = new RuntimeException(e2.toString());
            exc.initCause(e2);
            throw exc;
        }
    }

    int decrypt(byte[] buf, int offset, int len, int tagLen) throws BadPaddingException {
        if (this.cipher == null) {
            return len;
        }
        try {
            int newLen = this.cipher.update(buf, offset, len, buf, offset);
            if (newLen != len) {
                throw new RuntimeException("Cipher buffering error in JCE provider " + this.cipher.getProvider().getName());
            }
            if (debug != null && Debug.isOn("plaintext")) {
                try {
                    HexDumpEncoder hd = new HexDumpEncoder();
                    System.out.println("Padded plaintext after DECRYPTION:  len = " + newLen);
                    hd.encodeBuffer(new ByteArrayInputStream(buf, offset, newLen), System.out);
                } catch (IOException e) {
                }
            }
            if (this.blockSize != 0) {
                newLen = removePadding(buf, offset, newLen, tagLen, this.blockSize, this.protocolVersion);
                if (this.protocolVersion.v >= ProtocolVersion.TLS11.v) {
                    if (newLen < this.blockSize) {
                        throw new BadPaddingException("invalid explicit IV");
                    }
                    System.arraycopy(buf, this.blockSize + offset, buf, offset, newLen - this.blockSize);
                    newLen -= this.blockSize;
                }
            }
            return newLen;
        } catch (ShortBufferException e2) {
            throw new ArrayIndexOutOfBoundsException(e2.toString());
        }
    }

    int decrypt(ByteBuffer bb, int tagLen) throws BadPaddingException {
        int len = bb.remaining();
        if (this.cipher == null) {
            bb.position(bb.limit());
            return len;
        }
        try {
            int pos = bb.position();
            int newLen = this.cipher.update(bb.duplicate(), bb);
            if (newLen != len) {
                throw new RuntimeException("Cipher buffering error in JCE provider " + this.cipher.getProvider().getName());
            }
            if (debug != null && Debug.isOn("plaintext")) {
                try {
                    HexDumpEncoder hd = new HexDumpEncoder();
                    System.out.println("Padded plaintext after DECRYPTION:  len = " + newLen);
                    hd.encodeBuffer((ByteBuffer) bb.duplicate().position(pos), System.out);
                } catch (IOException e) {
                }
            }
            if (this.blockSize != 0) {
                bb.position(pos);
                newLen = removePadding(bb, tagLen, this.blockSize, this.protocolVersion);
                if (this.protocolVersion.v >= ProtocolVersion.TLS11.v) {
                    if (newLen < this.blockSize) {
                        throw new BadPaddingException("invalid explicit IV");
                    }
                    int limit = bb.limit();
                    byte[] buf;
                    if (bb.hasArray()) {
                        int arrayOffset = bb.arrayOffset();
                        buf = bb.array();
                        System.arraycopy(buf, (arrayOffset + pos) + this.blockSize, buf, arrayOffset + pos, (limit - pos) - this.blockSize);
                        bb.limit(limit - this.blockSize);
                    } else {
                        buf = new byte[((limit - pos) - this.blockSize)];
                        bb.position(this.blockSize + pos);
                        bb.get(buf);
                        bb.position(pos);
                        bb.put(buf);
                        bb.limit(limit - this.blockSize);
                    }
                    bb.position(bb.limit());
                }
            }
            return newLen;
        } catch (ShortBufferException e2) {
            RuntimeException exc = new RuntimeException(e2.toString());
            exc.initCause(e2);
            throw exc;
        }
    }

    private static int addPadding(byte[] buf, int offset, int len, int blockSize) {
        int newlen = len + 1;
        if (newlen % blockSize != 0) {
            newlen += blockSize - 1;
            newlen -= newlen % blockSize;
        }
        byte pad = (byte) (newlen - len);
        if (buf.length < newlen + offset) {
            throw new IllegalArgumentException("no space to pad buffer");
        }
        byte i = (byte) 0;
        int offset2 = offset + len;
        while (i < pad) {
            offset = offset2 + 1;
            buf[offset2] = (byte) (pad - 1);
            i++;
            offset2 = offset;
        }
        return newlen;
    }

    private static int addPadding(ByteBuffer bb, int blockSize) {
        int len = bb.remaining();
        int offset = bb.position();
        int newlen = len + 1;
        if (newlen % blockSize != 0) {
            newlen += blockSize - 1;
            newlen -= newlen % blockSize;
        }
        byte pad = (byte) (newlen - len);
        bb.limit(newlen + offset);
        byte i = (byte) 0;
        int offset2 = offset + len;
        while (i < pad) {
            offset = offset2 + 1;
            bb.put(offset2, (byte) (pad - 1));
            i++;
            offset2 = offset;
        }
        bb.position(offset2);
        bb.limit(offset2);
        return newlen;
    }

    private static int[] checkPadding(byte[] buf, int offset, int len, byte pad) {
        if (len <= 0) {
            throw new RuntimeException("padding len must be positive");
        }
        int[] results = new int[]{0, 0};
        int i = 0;
        while (i <= Record.maxPadding) {
            int j = 0;
            while (j < len && i <= Record.maxPadding) {
                if (buf[offset + j] != pad) {
                    results[0] = results[0] + 1;
                } else {
                    results[1] = results[1] + 1;
                }
                j++;
                i++;
            }
        }
        return results;
    }

    private static int[] checkPadding(ByteBuffer bb, byte pad) {
        if (bb.hasRemaining()) {
            int[] results = new int[]{0, 0};
            bb.mark();
            int i = 0;
            while (i <= Record.maxPadding) {
                while (bb.hasRemaining() && i <= Record.maxPadding) {
                    if (bb.get() != pad) {
                        results[0] = results[0] + 1;
                    } else {
                        results[1] = results[1] + 1;
                    }
                    i++;
                }
                bb.reset();
            }
            return results;
        }
        throw new RuntimeException("hasRemaining() must be positive");
    }

    private static int removePadding(byte[] buf, int offset, int len, int tagLen, int blockSize, ProtocolVersion protocolVersion) throws BadPaddingException {
        int padLen = buf[(offset + len) - 1] & 255;
        int newLen = len - (padLen + 1);
        if (newLen - tagLen < 0) {
            checkPadding(buf, offset, len, (byte) (padLen & 255));
            throw new BadPaddingException("Invalid Padding length: " + padLen);
        }
        int[] results = checkPadding(buf, offset + newLen, padLen + 1, (byte) (padLen & 255));
        if (protocolVersion.v >= ProtocolVersion.TLS10.v) {
            if (results[0] != 0) {
                throw new BadPaddingException("Invalid TLS padding data");
            }
        } else if (padLen > blockSize) {
            throw new BadPaddingException("Invalid SSLv3 padding");
        }
        return newLen;
    }

    private static int removePadding(ByteBuffer bb, int tagLen, int blockSize, ProtocolVersion protocolVersion) throws BadPaddingException {
        int len = bb.remaining();
        int offset = bb.position();
        int padLen = bb.get((offset + len) - 1) & 255;
        int newLen = len - (padLen + 1);
        if (newLen - tagLen < 0) {
            checkPadding(bb.duplicate(), (byte) (padLen & 255));
            throw new BadPaddingException("Invalid Padding length: " + padLen);
        }
        int[] results = checkPadding((ByteBuffer) bb.duplicate().position(offset + newLen), (byte) (padLen & 255));
        if (protocolVersion.v >= ProtocolVersion.TLS10.v) {
            if (results[0] != 0) {
                throw new BadPaddingException("Invalid TLS padding data");
            }
        } else if (padLen > blockSize) {
            throw new BadPaddingException("Invalid SSLv3 padding");
        }
        bb.position(offset + newLen);
        bb.limit(offset + newLen);
        return newLen;
    }

    void dispose() {
        try {
            if (this.cipher != null) {
                this.cipher.doFinal();
            }
        } catch (GeneralSecurityException e) {
        }
    }

    boolean isCBCMode() {
        return this.isCBCMode;
    }

    boolean isNullCipher() {
        return this.cipher == null;
    }

    boolean sanityCheck(int tagLen, int fragmentLen) {
        boolean z = true;
        if (!this.isCBCMode) {
            if (fragmentLen < tagLen) {
                z = false;
            }
            return z;
        } else if (fragmentLen % this.blockSize != 0) {
            return false;
        } else {
            int minimal = tagLen + 1;
            if (minimal < this.blockSize) {
                minimal = this.blockSize;
            }
            if (this.protocolVersion.v >= ProtocolVersion.TLS11.v) {
                minimal += this.blockSize;
            }
            if (fragmentLen < minimal) {
                z = false;
            }
            return z;
        }
    }
}
