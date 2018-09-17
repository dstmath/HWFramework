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
    int certainty = 20;
    DSAKeyPairGenerator engine = new DSAKeyPairGenerator();
    boolean initialised = false;
    DSAKeyGenerationParameters param;
    SecureRandom random = new SecureRandom();
    int strength = 1024;

    public KeyPairGeneratorSpi() {
        super("DSA");
    }

    public void initialize(int strength, SecureRandom random) {
        if (strength < 512 || strength > 4096 || ((strength < 1024 && strength % 64 != 0) || (strength >= 1024 && strength % 1024 != 0))) {
            throw new InvalidParameterException("strength must be from 512 - 4096 and a multiple of 1024 above 1024");
        }
        this.strength = strength;
        this.random = random;
        this.initialised = false;
    }

    public void initialize(AlgorithmParameterSpec params, SecureRandom random) throws InvalidAlgorithmParameterException {
        if (params instanceof DSAParameterSpec) {
            DSAParameterSpec dsaParams = (DSAParameterSpec) params;
            this.param = new DSAKeyGenerationParameters(random, new DSAParameters(dsaParams.getP(), dsaParams.getQ(), dsaParams.getG()));
            this.engine.init(this.param);
            this.initialised = true;
            return;
        }
        throw new InvalidAlgorithmParameterException("parameter object not a DSAParameterSpec");
    }

    public KeyPair generateKeyPair() {
        if (!this.initialised) {
            Integer paramStrength = Integers.valueOf(this.strength);
            if (params.containsKey(paramStrength)) {
                this.param = (DSAKeyGenerationParameters) params.get(paramStrength);
            } else {
                synchronized (lock) {
                    if (params.containsKey(paramStrength)) {
                        this.param = (DSAKeyGenerationParameters) params.get(paramStrength);
                    } else {
                        DSAParametersGenerator pGen;
                        if (this.strength == 1024) {
                            pGen = new DSAParametersGenerator();
                            if (Properties.isOverrideSet("org.bouncycastle.dsa.FIPS186-2for1024bits")) {
                                pGen.init(this.strength, this.certainty, this.random);
                            } else {
                                pGen.init(new DSAParameterGenerationParameters(1024, 160, this.certainty, this.random));
                            }
                        } else if (this.strength > 1024) {
                            DSAParameterGenerationParameters dsaParams = new DSAParameterGenerationParameters(this.strength, 256, this.certainty, this.random);
                            pGen = new DSAParametersGenerator(new SHA256Digest());
                            pGen.init(dsaParams);
                        } else {
                            pGen = new DSAParametersGenerator();
                            pGen.init(this.strength, this.certainty, this.random);
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
