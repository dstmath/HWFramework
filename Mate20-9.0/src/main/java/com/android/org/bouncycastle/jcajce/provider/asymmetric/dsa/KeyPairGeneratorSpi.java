package com.android.org.bouncycastle.jcajce.provider.asymmetric.dsa;

import com.android.org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import com.android.org.bouncycastle.crypto.digests.SHA256Digest;
import com.android.org.bouncycastle.crypto.generators.DSAKeyPairGenerator;
import com.android.org.bouncycastle.crypto.generators.DSAParametersGenerator;
import com.android.org.bouncycastle.crypto.params.DSAKeyGenerationParameters;
import com.android.org.bouncycastle.crypto.params.DSAParameterGenerationParameters;
import com.android.org.bouncycastle.crypto.params.DSAParameters;
import com.android.org.bouncycastle.crypto.params.DSAPrivateKeyParameters;
import com.android.org.bouncycastle.crypto.params.DSAPublicKeyParameters;
import com.android.org.bouncycastle.jcajce.provider.asymmetric.util.PrimeCertaintyCalculator;
import com.android.org.bouncycastle.util.Integers;
import com.android.org.bouncycastle.util.Properties;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.DSAParameterSpec;
import java.util.Hashtable;

public class KeyPairGeneratorSpi extends KeyPairGenerator {
    private static Object lock = new Object();
    private static Hashtable params = new Hashtable();
    DSAKeyPairGenerator engine = new DSAKeyPairGenerator();
    boolean initialised = false;
    DSAKeyGenerationParameters param;
    SecureRandom random = new SecureRandom();
    int strength = 1024;

    public KeyPairGeneratorSpi() {
        super("DSA");
    }

    public void initialize(int strength2, SecureRandom random2) {
        if (strength2 < 512 || strength2 > 4096 || ((strength2 < 1024 && strength2 % 64 != 0) || (strength2 >= 1024 && strength2 % 1024 != 0))) {
            throw new InvalidParameterException("strength must be from 512 - 4096 and a multiple of 1024 above 1024");
        }
        this.strength = strength2;
        this.random = random2;
        this.initialised = false;
    }

    public void initialize(AlgorithmParameterSpec params2, SecureRandom random2) throws InvalidAlgorithmParameterException {
        if (params2 instanceof DSAParameterSpec) {
            DSAParameterSpec dsaParams = (DSAParameterSpec) params2;
            this.param = new DSAKeyGenerationParameters(random2, new DSAParameters(dsaParams.getP(), dsaParams.getQ(), dsaParams.getG()));
            this.engine.init(this.param);
            this.initialised = true;
            return;
        }
        throw new InvalidAlgorithmParameterException("parameter object not a DSAParameterSpec");
    }

    public KeyPair generateKeyPair() {
        DSAParametersGenerator pGen;
        if (!this.initialised) {
            Integer paramStrength = Integers.valueOf(this.strength);
            if (params.containsKey(paramStrength)) {
                this.param = (DSAKeyGenerationParameters) params.get(paramStrength);
            } else {
                synchronized (lock) {
                    if (params.containsKey(paramStrength)) {
                        this.param = (DSAKeyGenerationParameters) params.get(paramStrength);
                    } else {
                        int certainty = PrimeCertaintyCalculator.getDefaultCertainty(this.strength);
                        if (this.strength == 1024) {
                            pGen = new DSAParametersGenerator();
                            if (Properties.isOverrideSet("org.bouncycastle.dsa.FIPS186-2for1024bits")) {
                                pGen.init(this.strength, certainty, this.random);
                            } else {
                                pGen.init(new DSAParameterGenerationParameters(1024, 160, certainty, this.random));
                            }
                        } else if (this.strength > 1024) {
                            DSAParameterGenerationParameters dsaParams = new DSAParameterGenerationParameters(this.strength, 256, certainty, this.random);
                            DSAParametersGenerator pGen2 = new DSAParametersGenerator(new SHA256Digest());
                            pGen2.init(dsaParams);
                            pGen = pGen2;
                        } else {
                            pGen = new DSAParametersGenerator();
                            pGen.init(this.strength, certainty, this.random);
                        }
                        this.param = new DSAKeyGenerationParameters(this.random, pGen.generateParameters());
                        params.put(paramStrength, this.param);
                    }
                }
            }
            this.engine.init(this.param);
            this.initialised = true;
        }
        AsymmetricCipherKeyPair pair = this.engine.generateKeyPair();
        return new KeyPair(new BCDSAPublicKey((DSAPublicKeyParameters) pair.getPublic()), new BCDSAPrivateKey((DSAPrivateKeyParameters) pair.getPrivate()));
    }
}
