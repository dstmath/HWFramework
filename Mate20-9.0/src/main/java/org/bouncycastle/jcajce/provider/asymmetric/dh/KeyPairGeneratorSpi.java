package org.bouncycastle.jcajce.provider.asymmetric.dh;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Hashtable;
import javax.crypto.spec.DHParameterSpec;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.crypto.generators.DHBasicKeyPairGenerator;
import org.bouncycastle.crypto.generators.DHParametersGenerator;
import org.bouncycastle.crypto.params.DHKeyGenerationParameters;
import org.bouncycastle.crypto.params.DHParameters;
import org.bouncycastle.crypto.params.DHPrivateKeyParameters;
import org.bouncycastle.crypto.params.DHPublicKeyParameters;
import org.bouncycastle.jcajce.provider.asymmetric.util.PrimeCertaintyCalculator;
import org.bouncycastle.jcajce.spec.DHDomainParameterSpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.Integers;

public class KeyPairGeneratorSpi extends KeyPairGenerator {
    private static Object lock = new Object();
    private static Hashtable params = new Hashtable();
    DHBasicKeyPairGenerator engine = new DHBasicKeyPairGenerator();
    boolean initialised = false;
    DHKeyGenerationParameters param;
    SecureRandom random = CryptoServicesRegistrar.getSecureRandom();
    int strength = 2048;

    public KeyPairGeneratorSpi() {
        super("DH");
    }

    private DHKeyGenerationParameters convertParams(SecureRandom secureRandom, DHParameterSpec dHParameterSpec) {
        return dHParameterSpec instanceof DHDomainParameterSpec ? new DHKeyGenerationParameters(secureRandom, ((DHDomainParameterSpec) dHParameterSpec).getDomainParameters()) : new DHKeyGenerationParameters(secureRandom, new DHParameters(dHParameterSpec.getP(), dHParameterSpec.getG(), null, dHParameterSpec.getL()));
    }

    public KeyPair generateKeyPair() {
        DHKeyGenerationParameters convertParams;
        if (!this.initialised) {
            Integer valueOf = Integers.valueOf(this.strength);
            if (params.containsKey(valueOf)) {
                convertParams = (DHKeyGenerationParameters) params.get(valueOf);
            } else {
                DHParameterSpec dHDefaultParameters = BouncyCastleProvider.CONFIGURATION.getDHDefaultParameters(this.strength);
                if (dHDefaultParameters != null) {
                    convertParams = convertParams(this.random, dHDefaultParameters);
                } else {
                    synchronized (lock) {
                        if (params.containsKey(valueOf)) {
                            this.param = (DHKeyGenerationParameters) params.get(valueOf);
                        } else {
                            DHParametersGenerator dHParametersGenerator = new DHParametersGenerator();
                            dHParametersGenerator.init(this.strength, PrimeCertaintyCalculator.getDefaultCertainty(this.strength), this.random);
                            this.param = new DHKeyGenerationParameters(this.random, dHParametersGenerator.generateParameters());
                            params.put(valueOf, this.param);
                        }
                    }
                    this.engine.init(this.param);
                    this.initialised = true;
                }
            }
            this.param = convertParams;
            this.engine.init(this.param);
            this.initialised = true;
        }
        AsymmetricCipherKeyPair generateKeyPair = this.engine.generateKeyPair();
        return new KeyPair(new BCDHPublicKey((DHPublicKeyParameters) generateKeyPair.getPublic()), new BCDHPrivateKey((DHPrivateKeyParameters) generateKeyPair.getPrivate()));
    }

    public void initialize(int i, SecureRandom secureRandom) {
        this.strength = i;
        this.random = secureRandom;
        this.initialised = false;
    }

    public void initialize(AlgorithmParameterSpec algorithmParameterSpec, SecureRandom secureRandom) throws InvalidAlgorithmParameterException {
        if (algorithmParameterSpec instanceof DHParameterSpec) {
            try {
                this.param = convertParams(secureRandom, (DHParameterSpec) algorithmParameterSpec);
                this.engine.init(this.param);
                this.initialised = true;
            } catch (IllegalArgumentException e) {
                throw new InvalidAlgorithmParameterException(e.getMessage(), e);
            }
        } else {
            throw new InvalidAlgorithmParameterException("parameter object not a DHParameterSpec");
        }
    }
}
