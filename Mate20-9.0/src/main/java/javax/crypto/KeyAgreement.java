package javax.crypto;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.ProviderException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import sun.security.jca.GetInstance;
import sun.security.jca.Providers;

public class KeyAgreement {
    private static final int I_NO_PARAMS = 1;
    private static final int I_PARAMS = 2;
    private static int warnCount = 10;
    private final String algorithm;
    private final Object lock;
    private Provider provider;
    private KeyAgreementSpi spi;

    protected KeyAgreement(KeyAgreementSpi keyAgreeSpi, Provider provider2, String algorithm2) {
        this.spi = keyAgreeSpi;
        this.provider = provider2;
        this.algorithm = algorithm2;
        this.lock = null;
    }

    private KeyAgreement(String algorithm2) {
        this.algorithm = algorithm2;
        this.lock = new Object();
    }

    public final String getAlgorithm() {
        return this.algorithm;
    }

    public static final KeyAgreement getInstance(String algorithm2) throws NoSuchAlgorithmException {
        for (Provider.Service s : GetInstance.getServices("KeyAgreement", algorithm2)) {
            if (JceSecurity.canUseProvider(s.getProvider())) {
                return new KeyAgreement(algorithm2);
            }
        }
        throw new NoSuchAlgorithmException("Algorithm " + algorithm2 + " not available");
    }

    public static final KeyAgreement getInstance(String algorithm2, String provider2) throws NoSuchAlgorithmException, NoSuchProviderException {
        Providers.checkBouncyCastleDeprecation(provider2, "KeyAgreement", algorithm2);
        GetInstance.Instance instance = JceSecurity.getInstance("KeyAgreement", (Class<?>) KeyAgreementSpi.class, algorithm2, provider2);
        return new KeyAgreement((KeyAgreementSpi) instance.impl, instance.provider, algorithm2);
    }

    public static final KeyAgreement getInstance(String algorithm2, Provider provider2) throws NoSuchAlgorithmException {
        Providers.checkBouncyCastleDeprecation(provider2, "KeyAgreement", algorithm2);
        GetInstance.Instance instance = JceSecurity.getInstance("KeyAgreement", (Class<?>) KeyAgreementSpi.class, algorithm2, provider2);
        return new KeyAgreement((KeyAgreementSpi) instance.impl, instance.provider, algorithm2);
    }

    /* access modifiers changed from: package-private */
    public void chooseFirstProvider() {
        if (this.spi == null) {
            synchronized (this.lock) {
                if (this.spi == null) {
                    Exception lastException = null;
                    for (Provider.Service s : GetInstance.getServices("KeyAgreement", this.algorithm)) {
                        if (JceSecurity.canUseProvider(s.getProvider())) {
                            try {
                                Object obj = s.newInstance(null);
                                if (obj instanceof KeyAgreementSpi) {
                                    this.spi = (KeyAgreementSpi) obj;
                                    this.provider = s.getProvider();
                                    return;
                                }
                            } catch (Exception e) {
                                lastException = e;
                            }
                        }
                    }
                    ProviderException e2 = new ProviderException("Could not construct KeyAgreementSpi instance");
                    if (lastException != null) {
                        e2.initCause(lastException);
                    }
                    throw e2;
                }
            }
        }
    }

    private void implInit(KeyAgreementSpi spi2, int type, Key key, AlgorithmParameterSpec params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (type == 1) {
            spi2.engineInit(key, random);
        } else {
            spi2.engineInit(key, params, random);
        }
    }

    private void chooseProvider(int initType, Key key, AlgorithmParameterSpec params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
        synchronized (this.lock) {
            if (this.spi == null || key != null) {
                Exception lastException = null;
                for (Provider.Service s : GetInstance.getServices("KeyAgreement", this.algorithm)) {
                    if (s.supportsParameter(key)) {
                        if (!JceSecurity.canUseProvider(s.getProvider())) {
                            continue;
                        } else {
                            try {
                                KeyAgreementSpi spi2 = (KeyAgreementSpi) s.newInstance(null);
                                implInit(spi2, initType, key, params, random);
                                this.provider = s.getProvider();
                                this.spi = spi2;
                                return;
                            } catch (Exception e) {
                                if (lastException == null) {
                                    lastException = e;
                                }
                            }
                        }
                    }
                }
                if (lastException instanceof InvalidKeyException) {
                    throw lastException;
                } else if (lastException instanceof InvalidAlgorithmParameterException) {
                    throw lastException;
                } else if (!(lastException instanceof RuntimeException)) {
                    String kName = key != null ? key.getClass().getName() : "(null)";
                    throw new InvalidKeyException("No installed provider supports this key: " + kName, lastException);
                } else {
                    throw lastException;
                }
            } else {
                implInit(this.spi, initType, key, params, random);
            }
        }
    }

    public final Provider getProvider() {
        chooseFirstProvider();
        return this.provider;
    }

    public final void init(Key key) throws InvalidKeyException {
        init(key, JceSecurity.RANDOM);
    }

    public final void init(Key key, SecureRandom random) throws InvalidKeyException {
        if (this.spi == null || !(key == null || this.lock == null)) {
            try {
                chooseProvider(1, key, null, random);
            } catch (InvalidAlgorithmParameterException e) {
                throw new InvalidKeyException((Throwable) e);
            }
        } else {
            this.spi.engineInit(key, random);
        }
    }

    public final void init(Key key, AlgorithmParameterSpec params) throws InvalidKeyException, InvalidAlgorithmParameterException {
        init(key, params, JceSecurity.RANDOM);
    }

    public final void init(Key key, AlgorithmParameterSpec params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (this.spi != null) {
            this.spi.engineInit(key, params, random);
        } else {
            chooseProvider(2, key, params, random);
        }
    }

    public final Key doPhase(Key key, boolean lastPhase) throws InvalidKeyException, IllegalStateException {
        chooseFirstProvider();
        return this.spi.engineDoPhase(key, lastPhase);
    }

    public final byte[] generateSecret() throws IllegalStateException {
        chooseFirstProvider();
        return this.spi.engineGenerateSecret();
    }

    public final int generateSecret(byte[] sharedSecret, int offset) throws IllegalStateException, ShortBufferException {
        chooseFirstProvider();
        return this.spi.engineGenerateSecret(sharedSecret, offset);
    }

    public final SecretKey generateSecret(String algorithm2) throws IllegalStateException, NoSuchAlgorithmException, InvalidKeyException {
        chooseFirstProvider();
        return this.spi.engineGenerateSecret(algorithm2);
    }
}
