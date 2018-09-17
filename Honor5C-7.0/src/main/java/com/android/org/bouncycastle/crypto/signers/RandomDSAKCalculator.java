package com.android.org.bouncycastle.crypto.signers;

import java.math.BigInteger;
import java.security.SecureRandom;

public class RandomDSAKCalculator implements DSAKCalculator {
    private static final BigInteger ZERO = null;
    private BigInteger q;
    private SecureRandom random;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.bouncycastle.crypto.signers.RandomDSAKCalculator.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.bouncycastle.crypto.signers.RandomDSAKCalculator.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.crypto.signers.RandomDSAKCalculator.<clinit>():void");
    }

    public boolean isDeterministic() {
        return false;
    }

    public void init(BigInteger n, SecureRandom random) {
        this.q = n;
        this.random = random;
    }

    public void init(BigInteger n, BigInteger d, byte[] message) {
        throw new IllegalStateException("Operation not supported");
    }

    public BigInteger nextK() {
        int qBitLength = this.q.bitLength();
        while (true) {
            BigInteger k = new BigInteger(qBitLength, this.random);
            if (!k.equals(ZERO) && k.compareTo(this.q) < 0) {
                return k;
            }
        }
    }
}
