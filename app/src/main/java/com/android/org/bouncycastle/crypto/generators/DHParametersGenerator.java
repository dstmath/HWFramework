package com.android.org.bouncycastle.crypto.generators;

import com.android.org.bouncycastle.crypto.params.DHParameters;
import java.math.BigInteger;
import java.security.SecureRandom;

public class DHParametersGenerator {
    private static final BigInteger TWO = null;
    private int certainty;
    private SecureRandom random;
    private int size;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.bouncycastle.crypto.generators.DHParametersGenerator.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.bouncycastle.crypto.generators.DHParametersGenerator.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.crypto.generators.DHParametersGenerator.<clinit>():void");
    }

    public void init(int size, int certainty, SecureRandom random) {
        this.size = size;
        this.certainty = certainty;
        this.random = random;
    }

    public DHParameters generateParameters() {
        BigInteger[] safePrimes = DHParametersHelper.generateSafePrimes(this.size, this.certainty, this.random);
        BigInteger p = safePrimes[0];
        BigInteger q = safePrimes[1];
        return new DHParameters(p, DHParametersHelper.selectGenerator(p, q, this.random), q, TWO, null);
    }
}
