package com.android.org.bouncycastle.jcajce.provider.asymmetric.dh;

import com.android.org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import com.android.org.bouncycastle.crypto.generators.DHBasicKeyPairGenerator;
import com.android.org.bouncycastle.crypto.generators.DHParametersGenerator;
import com.android.org.bouncycastle.crypto.params.DHKeyGenerationParameters;
import com.android.org.bouncycastle.crypto.params.DHParameters;
import com.android.org.bouncycastle.crypto.params.DHPrivateKeyParameters;
import com.android.org.bouncycastle.crypto.params.DHPublicKeyParameters;
import com.android.org.bouncycastle.jce.provider.BouncyCastleProvider;
import com.android.org.bouncycastle.util.Integers;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Hashtable;
import javax.crypto.spec.DHParameterSpec;

public class KeyPairGeneratorSpi extends KeyPairGenerator {
    private static Object lock = new Object();
    private static Hashtable params = new Hashtable();
    int certainty = 20;
    DHBasicKeyPairGenerator engine = new DHBasicKeyPairGenerator();
    boolean initialised = false;
    DHKeyGenerationParameters param;
    SecureRandom random = new SecureRandom();
    int strength = 1024;

    public KeyPairGeneratorSpi() {
        super("DH");
    }

    public void initialize(int strength, SecureRandom random) {
        this.strength = strength;
        this.random = random;
        this.initialised = false;
    }

    public void initialize(AlgorithmParameterSpec params, SecureRandom random) throws InvalidAlgorithmParameterException {
        if (params instanceof DHParameterSpec) {
            DHParameterSpec dhParams = (DHParameterSpec) params;
            this.param = new DHKeyGenerationParameters(random, new DHParameters(dhParams.getP(), dhParams.getG(), null, dhParams.getL()));
            this.engine.init(this.param);
            this.initialised = true;
            return;
        }
        throw new InvalidAlgorithmParameterException("parameter object not a DHParameterSpec");
    }

    public KeyPair generateKeyPair() {
        if (!this.initialised) {
            Integer paramStrength = Integers.valueOf(this.strength);
            if (params.containsKey(paramStrength)) {
                this.param = (DHKeyGenerationParameters) params.get(paramStrength);
            } else {
                DHParameterSpec dhParams = BouncyCastleProvider.CONFIGURATION.getDHDefaultParameters(this.strength);
                if (dhParams != null) {
                    this.param = new DHKeyGenerationParameters(this.random, new DHParameters(dhParams.getP(), dhParams.getG(), null, dhParams.getL()));
                } else {
                    synchronized (lock) {
                        if (params.containsKey(paramStrength)) {
                            this.param = (DHKeyGenerationParameters) params.get(paramStrength);
                        } else {
                            DHParametersGenerator pGen = new DHParametersGenerator();
                            pGen.init(this.strength, this.certainty, this.random);
                            this.param = new DHKeyGenerationParameters(this.random, pGen.generateParameters());
                            params.put(paramStrength, this.param);
                        }
                    }
                }
            }
            this.engine.init(this.param);
            this.initialised = true;
        }
        AsymmetricCipherKeyPair pair = this.engine.generateKeyPair();
        return new KeyPair(new BCDHPublicKey((DHPublicKeyParameters) pair.getPublic()), new BCDHPrivateKey((DHPrivateKeyParameters) pair.getPrivate()));
    }
}
