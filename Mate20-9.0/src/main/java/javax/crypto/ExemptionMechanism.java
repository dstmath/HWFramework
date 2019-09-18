package javax.crypto;

import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.spec.AlgorithmParameterSpec;
import sun.security.jca.GetInstance;

public class ExemptionMechanism {
    private boolean done = false;
    private ExemptionMechanismSpi exmechSpi;
    private boolean initialized = false;
    private Key keyStored = null;
    private String mechanism;
    private Provider provider;

    protected ExemptionMechanism(ExemptionMechanismSpi exmechSpi2, Provider provider2, String mechanism2) {
        this.exmechSpi = exmechSpi2;
        this.provider = provider2;
        this.mechanism = mechanism2;
    }

    public final String getName() {
        return this.mechanism;
    }

    public static final ExemptionMechanism getInstance(String algorithm) throws NoSuchAlgorithmException {
        GetInstance.Instance instance = JceSecurity.getInstance("ExemptionMechanism", ExemptionMechanismSpi.class, algorithm);
        return new ExemptionMechanism((ExemptionMechanismSpi) instance.impl, instance.provider, algorithm);
    }

    public static final ExemptionMechanism getInstance(String algorithm, String provider2) throws NoSuchAlgorithmException, NoSuchProviderException {
        GetInstance.Instance instance = JceSecurity.getInstance("ExemptionMechanism", (Class<?>) ExemptionMechanismSpi.class, algorithm, provider2);
        return new ExemptionMechanism((ExemptionMechanismSpi) instance.impl, instance.provider, algorithm);
    }

    public static final ExemptionMechanism getInstance(String algorithm, Provider provider2) throws NoSuchAlgorithmException {
        GetInstance.Instance instance = JceSecurity.getInstance("ExemptionMechanism", (Class<?>) ExemptionMechanismSpi.class, algorithm, provider2);
        return new ExemptionMechanism((ExemptionMechanismSpi) instance.impl, instance.provider, algorithm);
    }

    public final Provider getProvider() {
        return this.provider;
    }

    public final boolean isCryptoAllowed(Key key) throws ExemptionMechanismException {
        if (!this.done || key == null) {
            return false;
        }
        return this.keyStored.equals(key);
    }

    public final int getOutputSize(int inputLen) throws IllegalStateException {
        if (!this.initialized) {
            throw new IllegalStateException("ExemptionMechanism not initialized");
        } else if (inputLen >= 0) {
            return this.exmechSpi.engineGetOutputSize(inputLen);
        } else {
            throw new IllegalArgumentException("Input size must be equal to or greater than zero");
        }
    }

    public final void init(Key key) throws InvalidKeyException, ExemptionMechanismException {
        this.done = false;
        this.initialized = false;
        this.keyStored = key;
        this.exmechSpi.engineInit(key);
        this.initialized = true;
    }

    public final void init(Key key, AlgorithmParameterSpec params) throws InvalidKeyException, InvalidAlgorithmParameterException, ExemptionMechanismException {
        this.done = false;
        this.initialized = false;
        this.keyStored = key;
        this.exmechSpi.engineInit(key, params);
        this.initialized = true;
    }

    public final void init(Key key, AlgorithmParameters params) throws InvalidKeyException, InvalidAlgorithmParameterException, ExemptionMechanismException {
        this.done = false;
        this.initialized = false;
        this.keyStored = key;
        this.exmechSpi.engineInit(key, params);
        this.initialized = true;
    }

    public final byte[] genExemptionBlob() throws IllegalStateException, ExemptionMechanismException {
        if (this.initialized) {
            byte[] blob = this.exmechSpi.engineGenExemptionBlob();
            this.done = true;
            return blob;
        }
        throw new IllegalStateException("ExemptionMechanism not initialized");
    }

    public final int genExemptionBlob(byte[] output) throws IllegalStateException, ShortBufferException, ExemptionMechanismException {
        if (this.initialized) {
            int n = this.exmechSpi.engineGenExemptionBlob(output, 0);
            this.done = true;
            return n;
        }
        throw new IllegalStateException("ExemptionMechanism not initialized");
    }

    public final int genExemptionBlob(byte[] output, int outputOffset) throws IllegalStateException, ShortBufferException, ExemptionMechanismException {
        if (this.initialized) {
            int n = this.exmechSpi.engineGenExemptionBlob(output, outputOffset);
            this.done = true;
            return n;
        }
        throw new IllegalStateException("ExemptionMechanism not initialized");
    }
}
