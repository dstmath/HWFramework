package com.android.org.bouncycastle.crypto.macs;

import com.android.org.bouncycastle.crypto.CipherParameters;
import com.android.org.bouncycastle.crypto.Digest;
import com.android.org.bouncycastle.crypto.ExtendedDigest;
import com.android.org.bouncycastle.crypto.Mac;
import com.android.org.bouncycastle.crypto.params.KeyParameter;
import com.android.org.bouncycastle.util.Memoable;
import java.util.Hashtable;

public class HMac implements Mac {
    private static final byte IPAD = (byte) 54;
    private static final byte OPAD = (byte) 92;
    private static Hashtable blockLengths;
    private int blockLength;
    private Digest digest;
    private int digestSize;
    private byte[] inputPad;
    private Memoable ipadState;
    private Memoable opadState;
    private byte[] outputBuf;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.bouncycastle.crypto.macs.HMac.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.bouncycastle.crypto.macs.HMac.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.crypto.macs.HMac.<clinit>():void");
    }

    private static int getByteLength(Digest digest) {
        if (digest instanceof ExtendedDigest) {
            return ((ExtendedDigest) digest).getByteLength();
        }
        Integer b = (Integer) blockLengths.get(digest.getAlgorithmName());
        if (b != null) {
            return b.intValue();
        }
        throw new IllegalArgumentException("unknown digest passed: " + digest.getAlgorithmName());
    }

    public HMac(Digest digest) {
        this(digest, getByteLength(digest));
    }

    private HMac(Digest digest, int byteLength) {
        this.digest = digest;
        this.digestSize = digest.getDigestSize();
        this.blockLength = byteLength;
        this.inputPad = new byte[this.blockLength];
        this.outputBuf = new byte[(this.blockLength + this.digestSize)];
    }

    public String getAlgorithmName() {
        return this.digest.getAlgorithmName() + "/HMAC";
    }

    public Digest getUnderlyingDigest() {
        return this.digest;
    }

    public void init(CipherParameters params) {
        this.digest.reset();
        byte[] key = ((KeyParameter) params).getKey();
        int keyLength = key.length;
        if (keyLength > this.blockLength) {
            this.digest.update(key, 0, keyLength);
            this.digest.doFinal(this.inputPad, 0);
            keyLength = this.digestSize;
        } else {
            System.arraycopy(key, 0, this.inputPad, 0, keyLength);
        }
        for (int i = keyLength; i < this.inputPad.length; i++) {
            this.inputPad[i] = (byte) 0;
        }
        System.arraycopy(this.inputPad, 0, this.outputBuf, 0, this.blockLength);
        xorPad(this.inputPad, this.blockLength, IPAD);
        xorPad(this.outputBuf, this.blockLength, OPAD);
        if (this.digest instanceof Memoable) {
            this.opadState = ((Memoable) this.digest).copy();
            ((Digest) this.opadState).update(this.outputBuf, 0, this.blockLength);
        }
        this.digest.update(this.inputPad, 0, this.inputPad.length);
        if (this.digest instanceof Memoable) {
            this.ipadState = ((Memoable) this.digest).copy();
        }
    }

    public int getMacSize() {
        return this.digestSize;
    }

    public void update(byte in) {
        this.digest.update(in);
    }

    public void update(byte[] in, int inOff, int len) {
        this.digest.update(in, inOff, len);
    }

    public int doFinal(byte[] out, int outOff) {
        this.digest.doFinal(this.outputBuf, this.blockLength);
        if (this.opadState != null) {
            ((Memoable) this.digest).reset(this.opadState);
            this.digest.update(this.outputBuf, this.blockLength, this.digest.getDigestSize());
        } else {
            this.digest.update(this.outputBuf, 0, this.outputBuf.length);
        }
        int len = this.digest.doFinal(out, outOff);
        for (int i = this.blockLength; i < this.outputBuf.length; i++) {
            this.outputBuf[i] = (byte) 0;
        }
        if (this.ipadState != null) {
            ((Memoable) this.digest).reset(this.ipadState);
        } else {
            this.digest.update(this.inputPad, 0, this.inputPad.length);
        }
        return len;
    }

    public void reset() {
        this.digest.reset();
        this.digest.update(this.inputPad, 0, this.inputPad.length);
    }

    private static void xorPad(byte[] pad, int len, byte n) {
        for (int i = 0; i < len; i++) {
            pad[i] = (byte) (pad[i] ^ n);
        }
    }
}
