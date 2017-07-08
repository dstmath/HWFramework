package java.math;

import java.util.Arrays;
import org.w3c.dom.traversal.NodeFilter;

class Primality {
    private static final BigInteger[] BIprimes = null;
    private static final int[] primes = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.math.Primality.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.math.Primality.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.math.Primality.<clinit>():void");
    }

    private Primality() {
    }

    static BigInteger nextProbablePrime(BigInteger n) {
        int i;
        int[] modules = new int[primes.length];
        boolean[] isDivisible = new boolean[NodeFilter.SHOW_DOCUMENT_FRAGMENT];
        BigInt ni = n.getBigInt();
        if (ni.bitLength() <= 10) {
            int l = (int) ni.longInt();
            if (l < primes[primes.length - 1]) {
                i = 0;
                while (l >= primes[i]) {
                    i++;
                }
                return BIprimes[i];
            }
        }
        BigInt startPoint = ni.copy();
        BigInt probPrime = new BigInt();
        startPoint.addPositiveInt(BigInt.remainderByPositiveInt(ni, 2) + 1);
        for (i = 0; i < primes.length; i++) {
            modules[i] = BigInt.remainderByPositiveInt(startPoint, primes[i]) - 1024;
        }
        while (true) {
            Arrays.fill(isDivisible, false);
            for (i = 0; i < primes.length; i++) {
                modules[i] = (modules[i] + NodeFilter.SHOW_DOCUMENT_FRAGMENT) % primes[i];
                int j = modules[i] == 0 ? 0 : primes[i] - modules[i];
                while (j < NodeFilter.SHOW_DOCUMENT_FRAGMENT) {
                    isDivisible[j] = true;
                    j += primes[i];
                }
            }
            for (j = 0; j < NodeFilter.SHOW_DOCUMENT_FRAGMENT; j++) {
                if (!isDivisible[j]) {
                    probPrime.putCopy(startPoint);
                    probPrime.addPositiveInt(j);
                    if (probPrime.isPrime(100)) {
                        return new BigInteger(probPrime);
                    }
                }
            }
            startPoint.addPositiveInt(NodeFilter.SHOW_DOCUMENT_FRAGMENT);
        }
    }
}
