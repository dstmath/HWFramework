package com.android.org.bouncycastle.crypto.generators;

import com.android.org.bouncycastle.crypto.params.DHParameters;
import com.android.org.bouncycastle.math.ec.WNafUtil;
import com.android.org.bouncycastle.util.BigIntegers;
import java.math.BigInteger;
import java.security.SecureRandom;

class DHKeyGeneratorHelper {
    static final DHKeyGeneratorHelper INSTANCE = null;
    private static final BigInteger ONE = null;
    private static final BigInteger TWO = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.bouncycastle.crypto.generators.DHKeyGeneratorHelper.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.bouncycastle.crypto.generators.DHKeyGeneratorHelper.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.crypto.generators.DHKeyGeneratorHelper.<clinit>():void");
    }

    private DHKeyGeneratorHelper() {
    }

    BigInteger calculatePrivate(DHParameters dhParams, SecureRandom random) {
        int limit = dhParams.getL();
        int minWeight;
        BigInteger x;
        if (limit != 0) {
            minWeight = limit >>> 2;
            do {
                x = new BigInteger(limit, random).setBit(limit - 1);
            } while (WNafUtil.getNafWeight(x) < minWeight);
            return x;
        }
        BigInteger min = TWO;
        int m = dhParams.getM();
        if (m != 0) {
            min = ONE.shiftLeft(m - 1);
        }
        BigInteger q = dhParams.getQ();
        if (q == null) {
            q = dhParams.getP();
        }
        BigInteger max = q.subtract(TWO);
        minWeight = max.bitLength() >>> 2;
        do {
            x = BigIntegers.createRandomInRange(min, max, random);
        } while (WNafUtil.getNafWeight(x) < minWeight);
        return x;
    }

    BigInteger calculatePublic(DHParameters dhParams, BigInteger x) {
        return dhParams.getG().modPow(x, dhParams.getP());
    }
}
