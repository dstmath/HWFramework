package java.security;

import java.security.Provider.Service;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Iterator;
import sun.security.jca.GetInstance;
import sun.security.jca.GetInstance.Instance;
import sun.security.jca.JCAUtil;

public abstract class KeyPairGenerator extends KeyPairGeneratorSpi {
    private final String algorithm;
    Provider provider;

    private static final class Delegate extends KeyPairGenerator {
        private static final int I_NONE = 1;
        private static final int I_PARAMS = 3;
        private static final int I_SIZE = 2;
        private int initKeySize;
        private AlgorithmParameterSpec initParams;
        private SecureRandom initRandom;
        private int initType;
        private final Object lock = new Object();
        private Iterator<Service> serviceIterator;
        private volatile KeyPairGeneratorSpi spi;

        Delegate(KeyPairGeneratorSpi spi, String algorithm) {
            super(algorithm);
            this.spi = spi;
        }

        Delegate(Instance instance, Iterator<Service> serviceIterator, String algorithm) {
            super(algorithm);
            this.spi = (KeyPairGeneratorSpi) instance.impl;
            this.provider = instance.provider;
            this.serviceIterator = serviceIterator;
            this.initType = 1;
        }

        private KeyPairGeneratorSpi nextSpi(KeyPairGeneratorSpi oldSpi, boolean reinit) {
            synchronized (this.lock) {
                if (oldSpi != null) {
                    if (oldSpi != this.spi) {
                        KeyPairGeneratorSpi keyPairGeneratorSpi = this.spi;
                        return keyPairGeneratorSpi;
                    }
                }
                if (this.serviceIterator == null) {
                    return null;
                }
                while (this.serviceIterator.hasNext()) {
                    Service s = (Service) this.serviceIterator.next();
                    try {
                        Object inst = s.newInstance(null);
                        if ((inst instanceof KeyPairGeneratorSpi) && !(inst instanceof KeyPairGenerator)) {
                            KeyPairGeneratorSpi spi = (KeyPairGeneratorSpi) inst;
                            if (reinit) {
                                if (this.initType == 2) {
                                    spi.initialize(this.initKeySize, this.initRandom);
                                } else if (this.initType == 3) {
                                    spi.initialize(this.initParams, this.initRandom);
                                } else if (this.initType != 1) {
                                    throw new AssertionError("KeyPairGenerator initType: " + this.initType);
                                }
                            }
                            this.provider = s.getProvider();
                            this.spi = spi;
                            return spi;
                        }
                    } catch (Exception e) {
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

        public void initialize(int keysize, SecureRandom random) {
            if (this.serviceIterator == null) {
                this.spi.initialize(keysize, random);
                return;
            }
            RuntimeException failure = null;
            KeyPairGeneratorSpi mySpi = this.spi;
            do {
                try {
                    mySpi.initialize(keysize, random);
                    this.initType = 2;
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

        /* JADX WARNING: Removed duplicated region for block: B:17:0x002d  */
        /* JADX WARNING: Removed duplicated region for block: B:15:0x002a  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void initialize(AlgorithmParameterSpec params, SecureRandom random) throws InvalidAlgorithmParameterException {
            if (this.serviceIterator == null) {
                this.spi.initialize(params, random);
                return;
            }
            Exception failure = null;
            KeyPairGeneratorSpi mySpi = this.spi;
            do {
                try {
                    mySpi.initialize(params, random);
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
                        if (failure instanceof RuntimeException) {
                        }
                    }
                }
            } while (mySpi == null);
            if (failure instanceof RuntimeException) {
                throw ((RuntimeException) failure);
            }
            throw ((InvalidAlgorithmParameterException) failure);
        }

        public KeyPair generateKeyPair() {
            if (this.serviceIterator == null) {
                return this.spi.generateKeyPair();
            }
            RuntimeException failure = null;
            KeyPairGeneratorSpi mySpi = this.spi;
            do {
                try {
                    return mySpi.generateKeyPair();
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

    protected KeyPairGenerator(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getAlgorithm() {
        return this.algorithm;
    }

    private static KeyPairGenerator getInstance(Instance instance, String algorithm) {
        KeyPairGenerator kpg;
        if (instance.impl instanceof KeyPairGenerator) {
            kpg = instance.impl;
        } else {
            kpg = new Delegate(instance.impl, algorithm);
        }
        kpg.provider = instance.provider;
        return kpg;
    }

    public static KeyPairGenerator getInstance(String algorithm) throws NoSuchAlgorithmException {
        Iterator<Service> t = GetInstance.getServices("KeyPairGenerator", algorithm).iterator();
        if (t.hasNext()) {
            NoSuchAlgorithmException noSuchAlgorithmException = null;
            do {
                try {
                    Instance instance = GetInstance.getInstance((Service) t.next(), KeyPairGeneratorSpi.class);
                    if (instance.impl instanceof KeyPairGenerator) {
                        return getInstance(instance, algorithm);
                    }
                    return new Delegate(instance, t, algorithm);
                } catch (NoSuchAlgorithmException e) {
                    if (noSuchAlgorithmException == null) {
                        noSuchAlgorithmException = e;
                    }
                    if (!t.hasNext()) {
                        throw noSuchAlgorithmException;
                    }
                }
            } while (t.hasNext());
            throw noSuchAlgorithmException;
        }
        throw new NoSuchAlgorithmException(algorithm + " KeyPairGenerator not available");
    }

    public static KeyPairGenerator getInstance(String algorithm, String provider) throws NoSuchAlgorithmException, NoSuchProviderException {
        return getInstance(GetInstance.getInstance("KeyPairGenerator", KeyPairGeneratorSpi.class, algorithm, provider), algorithm);
    }

    public static KeyPairGenerator getInstance(String algorithm, Provider provider) throws NoSuchAlgorithmException {
        return getInstance(GetInstance.getInstance("KeyPairGenerator", KeyPairGeneratorSpi.class, algorithm, provider), algorithm);
    }

    public final Provider getProvider() {
        disableFailover();
        return this.provider;
    }

    void disableFailover() {
    }

    public void initialize(int keysize) {
        initialize(keysize, JCAUtil.getSecureRandom());
    }

    public void initialize(int keysize, SecureRandom random) {
    }

    public void initialize(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {
        initialize(params, JCAUtil.getSecureRandom());
    }

    public void initialize(AlgorithmParameterSpec params, SecureRandom random) throws InvalidAlgorithmParameterException {
    }

    public final KeyPair genKeyPair() {
        return generateKeyPair();
    }

    public KeyPair generateKeyPair() {
        return null;
    }
}
