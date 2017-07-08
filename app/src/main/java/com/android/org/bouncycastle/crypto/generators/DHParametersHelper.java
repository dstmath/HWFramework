package com.android.org.bouncycastle.crypto.generators;

import com.android.org.bouncycastle.math.ec.WNafUtil;
import com.android.org.bouncycastle.util.BigIntegers;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.logging.Logger;

class DHParametersHelper {
    private static final BigInteger ONE = null;
    private static final BigInteger TWO = null;
    private static final Logger logger = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.bouncycastle.crypto.generators.DHParametersHelper.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.bouncycastle.crypto.generators.DHParametersHelper.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.crypto.generators.DHParametersHelper.<clinit>():void");
    }

    DHParametersHelper() {
    }

    static BigInteger[] generateSafePrimes(int size, int certainty, SecureRandom random) {
        logger.info("Generating safe primes. This may take a long time.");
        long start = System.currentTimeMillis();
        int tries = 0;
        int qLength = size - 1;
        int minWeight = size >>> 2;
        while (true) {
            tries++;
            BigInteger q = new BigInteger(qLength, 2, random);
            BigInteger p = q.shiftLeft(1).add(ONE);
            if (p.isProbablePrime(certainty) && ((certainty <= 2 || q.isProbablePrime(certainty - 2)) && WNafUtil.getNafWeight(p) >= minWeight)) {
                logger.info("Generated safe primes: " + tries + " tries took " + (System.currentTimeMillis() - start) + "ms");
                return new BigInteger[]{p, q};
            }
        }
    }

    static BigInteger selectGenerator(BigInteger p, BigInteger q, SecureRandom random) {
        BigInteger g;
        BigInteger pMinusTwo = p.subtract(TWO);
        do {
            g = BigIntegers.createRandomInRange(TWO, pMinusTwo, random).modPow(TWO, p);
        } while (g.equals(ONE));
        return g;
    }
}
