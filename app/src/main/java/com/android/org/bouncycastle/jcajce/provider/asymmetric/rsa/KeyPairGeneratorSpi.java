package com.android.org.bouncycastle.jcajce.provider.asymmetric.rsa;

import com.android.org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import com.android.org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import com.android.org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import com.android.org.bouncycastle.crypto.params.RSAKeyParameters;
import com.android.org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.RSAKeyGenParameterSpec;

public class KeyPairGeneratorSpi extends KeyPairGenerator {
    static final BigInteger defaultPublicExponent = null;
    static final int defaultTests = 112;
    RSAKeyPairGenerator engine;
    RSAKeyGenerationParameters param;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.bouncycastle.jcajce.provider.asymmetric.rsa.KeyPairGeneratorSpi.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.bouncycastle.jcajce.provider.asymmetric.rsa.KeyPairGeneratorSpi.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jcajce.provider.asymmetric.rsa.KeyPairGeneratorSpi.<clinit>():void");
    }

    public KeyPairGeneratorSpi(String algorithmName) {
        super(algorithmName);
    }

    public KeyPairGeneratorSpi() {
        super("RSA");
        this.engine = new RSAKeyPairGenerator();
        this.param = new RSAKeyGenerationParameters(defaultPublicExponent, new SecureRandom(), 2048, defaultTests);
        this.engine.init(this.param);
    }

    public void initialize(int strength, SecureRandom random) {
        BigInteger bigInteger = defaultPublicExponent;
        if (random == null) {
            random = new SecureRandom();
        }
        this.param = new RSAKeyGenerationParameters(bigInteger, random, strength, defaultTests);
        this.engine.init(this.param);
    }

    public void initialize(AlgorithmParameterSpec params, SecureRandom random) throws InvalidAlgorithmParameterException {
        if (params instanceof RSAKeyGenParameterSpec) {
            RSAKeyGenParameterSpec rsaParams = (RSAKeyGenParameterSpec) params;
            BigInteger publicExponent = rsaParams.getPublicExponent();
            if (random == null) {
                random = new SecureRandom();
            }
            this.param = new RSAKeyGenerationParameters(publicExponent, random, rsaParams.getKeysize(), defaultTests);
            this.engine.init(this.param);
            return;
        }
        throw new InvalidAlgorithmParameterException("parameter object not a RSAKeyGenParameterSpec");
    }

    public KeyPair generateKeyPair() {
        AsymmetricCipherKeyPair pair = this.engine.generateKeyPair();
        return new KeyPair(new BCRSAPublicKey((RSAKeyParameters) pair.getPublic()), new BCRSAPrivateCrtKey((RSAPrivateCrtKeyParameters) pair.getPrivate()));
    }
}
