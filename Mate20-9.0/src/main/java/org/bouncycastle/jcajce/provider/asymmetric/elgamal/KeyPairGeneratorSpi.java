package org.bouncycastle.jcajce.provider.asymmetric.elgamal;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.spec.DHParameterSpec;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.crypto.generators.ElGamalKeyPairGenerator;
import org.bouncycastle.crypto.generators.ElGamalParametersGenerator;
import org.bouncycastle.crypto.params.ElGamalKeyGenerationParameters;
import org.bouncycastle.crypto.params.ElGamalParameters;
import org.bouncycastle.crypto.params.ElGamalPrivateKeyParameters;
import org.bouncycastle.crypto.params.ElGamalPublicKeyParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ElGamalParameterSpec;

public class KeyPairGeneratorSpi extends KeyPairGenerator {
    int certainty = 20;
    ElGamalKeyPairGenerator engine = new ElGamalKeyPairGenerator();
    boolean initialised = false;
    ElGamalKeyGenerationParameters param;
    SecureRandom random = CryptoServicesRegistrar.getSecureRandom();
    int strength = 1024;

    public KeyPairGeneratorSpi() {
        super("ElGamal");
    }

    public KeyPair generateKeyPair() {
        ElGamalKeyGenerationParameters elGamalKeyGenerationParameters;
        if (!this.initialised) {
            DHParameterSpec dHDefaultParameters = BouncyCastleProvider.CONFIGURATION.getDHDefaultParameters(this.strength);
            if (dHDefaultParameters != null) {
                elGamalKeyGenerationParameters = new ElGamalKeyGenerationParameters(this.random, new ElGamalParameters(dHDefaultParameters.getP(), dHDefaultParameters.getG(), dHDefaultParameters.getL()));
            } else {
                ElGamalParametersGenerator elGamalParametersGenerator = new ElGamalParametersGenerator();
                elGamalParametersGenerator.init(this.strength, this.certainty, this.random);
                elGamalKeyGenerationParameters = new ElGamalKeyGenerationParameters(this.random, elGamalParametersGenerator.generateParameters());
            }
            this.param = elGamalKeyGenerationParameters;
            this.engine.init(this.param);
            this.initialised = true;
        }
        AsymmetricCipherKeyPair generateKeyPair = this.engine.generateKeyPair();
        return new KeyPair(new BCElGamalPublicKey((ElGamalPublicKeyParameters) generateKeyPair.getPublic()), new BCElGamalPrivateKey((ElGamalPrivateKeyParameters) generateKeyPair.getPrivate()));
    }

    public void initialize(int i, SecureRandom secureRandom) {
        this.strength = i;
        this.random = secureRandom;
    }

    public void initialize(AlgorithmParameterSpec algorithmParameterSpec, SecureRandom secureRandom) throws InvalidAlgorithmParameterException {
        ElGamalKeyGenerationParameters elGamalKeyGenerationParameters;
        boolean z = algorithmParameterSpec instanceof ElGamalParameterSpec;
        if (z || (algorithmParameterSpec instanceof DHParameterSpec)) {
            if (z) {
                ElGamalParameterSpec elGamalParameterSpec = (ElGamalParameterSpec) algorithmParameterSpec;
                elGamalKeyGenerationParameters = new ElGamalKeyGenerationParameters(secureRandom, new ElGamalParameters(elGamalParameterSpec.getP(), elGamalParameterSpec.getG()));
            } else {
                DHParameterSpec dHParameterSpec = (DHParameterSpec) algorithmParameterSpec;
                elGamalKeyGenerationParameters = new ElGamalKeyGenerationParameters(secureRandom, new ElGamalParameters(dHParameterSpec.getP(), dHParameterSpec.getG(), dHParameterSpec.getL()));
            }
            this.param = elGamalKeyGenerationParameters;
            this.engine.init(this.param);
            this.initialised = true;
            return;
        }
        throw new InvalidAlgorithmParameterException("parameter object not a DHParameterSpec or an ElGamalParameterSpec");
    }
}
