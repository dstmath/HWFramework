package javax.crypto;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Provider.Service;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Iterator;
import sun.security.jca.GetInstance;
import sun.security.jca.GetInstance.Instance;

public class KeyGenerator {
    private static final int I_NONE = 1;
    private static final int I_PARAMS = 3;
    private static final int I_RANDOM = 2;
    private static final int I_SIZE = 4;
    private final String algorithm;
    private int initKeySize;
    private AlgorithmParameterSpec initParams;
    private SecureRandom initRandom;
    private int initType;
    private final Object lock = new Object();
    private Provider provider;
    private Iterator<Service> serviceIterator;
    private volatile KeyGeneratorSpi spi;

    protected KeyGenerator(KeyGeneratorSpi keyGenSpi, Provider provider, String algorithm) {
        this.spi = keyGenSpi;
        this.provider = provider;
        this.algorithm = algorithm;
    }

    private KeyGenerator(String algorithm) throws NoSuchAlgorithmException {
        this.algorithm = algorithm;
        this.serviceIterator = GetInstance.getServices("KeyGenerator", algorithm).iterator();
        this.initType = 1;
        if (nextSpi(null, false) == null) {
            throw new NoSuchAlgorithmException(algorithm + " KeyGenerator not available");
        }
    }

    public final String getAlgorithm() {
        return this.algorithm;
    }

    public static final KeyGenerator getInstance(String algorithm) throws NoSuchAlgorithmException {
        return new KeyGenerator(algorithm);
    }

    public static final KeyGenerator getInstance(String algorithm, String provider) throws NoSuchAlgorithmException, NoSuchProviderException {
        Instance instance = JceSecurity.getInstance("KeyGenerator", KeyGeneratorSpi.class, algorithm, provider);
        return new KeyGenerator((KeyGeneratorSpi) instance.impl, instance.provider, algorithm);
    }

    public static final KeyGenerator getInstance(String algorithm, Provider provider) throws NoSuchAlgorithmException {
        Instance instance = JceSecurity.getInstance("KeyGenerator", KeyGeneratorSpi.class, algorithm, provider);
        return new KeyGenerator((KeyGeneratorSpi) instance.impl, instance.provider, algorithm);
    }

    public final Provider getProvider() {
        Provider provider;
        synchronized (this.lock) {
            disableFailover();
            provider = this.provider;
        }
        return provider;
    }

    private KeyGeneratorSpi nextSpi(KeyGeneratorSpi oldSpi, boolean reinit) {
        synchronized (this.lock) {
            if (oldSpi != null) {
                if (oldSpi != this.spi) {
                    KeyGeneratorSpi keyGeneratorSpi = this.spi;
                    return keyGeneratorSpi;
                }
            }
            if (this.serviceIterator == null) {
                return null;
            }
            while (this.serviceIterator.hasNext()) {
                Service s = (Service) this.serviceIterator.next();
                if (JceSecurity.canUseProvider(s.getProvider())) {
                    try {
                        Object inst = s.newInstance(null);
                        if (inst instanceof KeyGeneratorSpi) {
                            KeyGeneratorSpi spi = (KeyGeneratorSpi) inst;
                            if (reinit) {
                                if (this.initType == 4) {
                                    spi.engineInit(this.initKeySize, this.initRandom);
                                } else if (this.initType == 3) {
                                    spi.engineInit(this.initParams, this.initRandom);
                                } else if (this.initType == 2) {
                                    spi.engineInit(this.initRandom);
                                } else if (this.initType != 1) {
                                    throw new AssertionError("KeyGenerator initType: " + this.initType);
                                }
                            }
                            this.provider = s.getProvider();
                            this.spi = spi;
                            return spi;
                        }
                        continue;
                    } catch (Exception e) {
                    }
                }
            }
            disableFailover();
            return null;
        }
    }

    void disableFailover() {
        this.serviceIterator = null;
        this.initType = 0;
        this.initParams = null;
        this.initRandom = null;
    }

    public final void init(SecureRandom random) {
        if (this.serviceIterator == null) {
            this.spi.engineInit(random);
            return;
        }
        RuntimeException failure = null;
        KeyGeneratorSpi mySpi = this.spi;
        do {
            try {
                mySpi.engineInit(random);
                this.initType = 2;
                this.initKeySize = 0;
                this.initParams = null;
                this.initRandom = random;
                return;
            } catch (RuntimeException e) {
                if (failure == null) {
                    failure = e;
                }
                mySpi = nextSpi(mySpi, false);
                if (mySpi == null) {
                    throw failure;
                }
            }
        } while (mySpi == null);
        throw failure;
    }

    public final void init(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {
        init(params, JceSecurity.RANDOM);
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x002d  */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x002a  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void init(AlgorithmParameterSpec params, SecureRandom random) throws InvalidAlgorithmParameterException {
        if (this.serviceIterator == null) {
            this.spi.engineInit(params, random);
            return;
        }
        Exception failure = null;
        KeyGeneratorSpi mySpi = this.spi;
        do {
            try {
                mySpi.engineInit(params, random);
                this.initType = 3;
                this.initKeySize = 0;
                this.initParams = params;
                this.initRandom = random;
                return;
            } catch (Exception e) {
                if (failure == null) {
                    failure = e;
                }
                mySpi = nextSpi(mySpi, false);
                if (mySpi == null) {
                    if (failure instanceof InvalidAlgorithmParameterException) {
                    }
                }
            }
        } while (mySpi == null);
        if (failure instanceof InvalidAlgorithmParameterException) {
            throw ((InvalidAlgorithmParameterException) failure);
        } else if (failure instanceof RuntimeException) {
            throw ((RuntimeException) failure);
        } else {
            throw new InvalidAlgorithmParameterException("init() failed", failure);
        }
    }

    public final void init(int keysize) {
        init(keysize, JceSecurity.RANDOM);
    }

    public final void init(int keysize, SecureRandom random) {
        if (this.serviceIterator == null) {
            this.spi.engineInit(keysize, random);
            return;
        }
        RuntimeException failure = null;
        KeyGeneratorSpi mySpi = this.spi;
        do {
            try {
                mySpi.engineInit(keysize, random);
                this.initType = 4;
                this.initKeySize = keysize;
                this.initParams = null;
                this.initRandom = random;
                return;
            } catch (RuntimeException e) {
                if (failure == null) {
                    failure = e;
                }
                mySpi = nextSpi(mySpi, false);
                if (mySpi == null) {
                    throw failure;
                }
            }
        } while (mySpi == null);
        throw failure;
    }

    public final SecretKey generateKey() {
        if (this.serviceIterator == null) {
            return this.spi.engineGenerateKey();
        }
        RuntimeException failure = null;
        KeyGeneratorSpi mySpi = this.spi;
        do {
            try {
                return mySpi.engineGenerateKey();
            } catch (RuntimeException e) {
                if (failure == null) {
                    failure = e;
                }
                mySpi = nextSpi(mySpi, true);
                if (mySpi == null) {
                    throw failure;
                }
            }
        } while (mySpi == null);
        throw failure;
    }
}
