package java.security;

import java.security.Provider;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Iterator;
import sun.security.jca.GetInstance;
import sun.security.jca.Providers;
import sun.security.util.Debug;

public class KeyFactory {
    private static final Debug debug = Debug.getInstance("jca", "KeyFactory");
    private final String algorithm;
    private final Object lock = new Object();
    private Provider provider;
    private Iterator<Provider.Service> serviceIterator;
    private volatile KeyFactorySpi spi;

    protected KeyFactory(KeyFactorySpi keyFacSpi, Provider provider2, String algorithm2) {
        this.spi = keyFacSpi;
        this.provider = provider2;
        this.algorithm = algorithm2;
    }

    private KeyFactory(String algorithm2) throws NoSuchAlgorithmException {
        this.algorithm = algorithm2;
        this.serviceIterator = GetInstance.getServices("KeyFactory", algorithm2).iterator();
        if (nextSpi(null) == null) {
            throw new NoSuchAlgorithmException(algorithm2 + " KeyFactory not available");
        }
    }

    public static KeyFactory getInstance(String algorithm2) throws NoSuchAlgorithmException {
        return new KeyFactory(algorithm2);
    }

    public static KeyFactory getInstance(String algorithm2, String provider2) throws NoSuchAlgorithmException, NoSuchProviderException {
        Providers.checkBouncyCastleDeprecation(provider2, "KeyFactory", algorithm2);
        GetInstance.Instance instance = GetInstance.getInstance("KeyFactory", (Class<?>) KeyFactorySpi.class, algorithm2, provider2);
        return new KeyFactory((KeyFactorySpi) instance.impl, instance.provider, algorithm2);
    }

    public static KeyFactory getInstance(String algorithm2, Provider provider2) throws NoSuchAlgorithmException {
        Providers.checkBouncyCastleDeprecation(provider2, "KeyFactory", algorithm2);
        GetInstance.Instance instance = GetInstance.getInstance("KeyFactory", (Class<?>) KeyFactorySpi.class, algorithm2, provider2);
        return new KeyFactory((KeyFactorySpi) instance.impl, instance.provider, algorithm2);
    }

    public final Provider getProvider() {
        Provider provider2;
        synchronized (this.lock) {
            this.serviceIterator = null;
            provider2 = this.provider;
        }
        return provider2;
    }

    public final String getAlgorithm() {
        return this.algorithm;
    }

    private KeyFactorySpi nextSpi(KeyFactorySpi oldSpi) {
        synchronized (this.lock) {
            if (oldSpi != null) {
                try {
                    if (oldSpi != this.spi) {
                        KeyFactorySpi keyFactorySpi = this.spi;
                        return keyFactorySpi;
                    }
                } catch (NoSuchAlgorithmException e) {
                } finally {
                }
            }
            if (this.serviceIterator == null) {
                return null;
            }
            while (this.serviceIterator.hasNext()) {
                Provider.Service s = this.serviceIterator.next();
                Object obj = s.newInstance(null);
                if (obj instanceof KeyFactorySpi) {
                    KeyFactorySpi spi2 = (KeyFactorySpi) obj;
                    this.provider = s.getProvider();
                    this.spi = spi2;
                    return spi2;
                }
            }
            this.serviceIterator = null;
            return null;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x0021  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0031  */
    public final PublicKey generatePublic(KeySpec keySpec) throws InvalidKeySpecException {
        if (this.serviceIterator == null) {
            return this.spi.engineGeneratePublic(keySpec);
        }
        Exception failure = null;
        KeyFactorySpi mySpi = this.spi;
        do {
            try {
                return mySpi.engineGeneratePublic(keySpec);
            } catch (Exception e) {
                if (failure == null) {
                    failure = e;
                }
                mySpi = nextSpi(mySpi);
                if (mySpi == null) {
                    if (!(failure instanceof RuntimeException)) {
                    }
                }
            }
        } while (mySpi == null);
        if (!(failure instanceof RuntimeException)) {
            throw ((RuntimeException) failure);
        } else if (failure instanceof InvalidKeySpecException) {
            throw ((InvalidKeySpecException) failure);
        } else {
            throw new InvalidKeySpecException("Could not generate public key", failure);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x0021  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0031  */
    public final PrivateKey generatePrivate(KeySpec keySpec) throws InvalidKeySpecException {
        if (this.serviceIterator == null) {
            return this.spi.engineGeneratePrivate(keySpec);
        }
        Exception failure = null;
        KeyFactorySpi mySpi = this.spi;
        do {
            try {
                return mySpi.engineGeneratePrivate(keySpec);
            } catch (Exception e) {
                if (failure == null) {
                    failure = e;
                }
                mySpi = nextSpi(mySpi);
                if (mySpi == null) {
                    if (!(failure instanceof RuntimeException)) {
                    }
                }
            }
        } while (mySpi == null);
        if (!(failure instanceof RuntimeException)) {
            throw ((RuntimeException) failure);
        } else if (failure instanceof InvalidKeySpecException) {
            throw ((InvalidKeySpecException) failure);
        } else {
            throw new InvalidKeySpecException("Could not generate private key", failure);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x0021  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0031  */
    public final <T extends KeySpec> T getKeySpec(Key key, Class<T> keySpec) throws InvalidKeySpecException {
        if (this.serviceIterator == null) {
            return this.spi.engineGetKeySpec(key, keySpec);
        }
        Exception failure = null;
        KeyFactorySpi mySpi = this.spi;
        do {
            try {
                return mySpi.engineGetKeySpec(key, keySpec);
            } catch (Exception e) {
                if (failure == null) {
                    failure = e;
                }
                mySpi = nextSpi(mySpi);
                if (mySpi == null) {
                    if (!(failure instanceof RuntimeException)) {
                    }
                }
            }
        } while (mySpi == null);
        if (!(failure instanceof RuntimeException)) {
            throw ((RuntimeException) failure);
        } else if (failure instanceof InvalidKeySpecException) {
            throw ((InvalidKeySpecException) failure);
        } else {
            throw new InvalidKeySpecException("Could not get key spec", failure);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x0021  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0031  */
    public final Key translateKey(Key key) throws InvalidKeyException {
        if (this.serviceIterator == null) {
            return this.spi.engineTranslateKey(key);
        }
        Exception failure = null;
        KeyFactorySpi mySpi = this.spi;
        do {
            try {
                return mySpi.engineTranslateKey(key);
            } catch (Exception e) {
                if (failure == null) {
                    failure = e;
                }
                mySpi = nextSpi(mySpi);
                if (mySpi == null) {
                    if (!(failure instanceof RuntimeException)) {
                    }
                }
            }
        } while (mySpi == null);
        if (!(failure instanceof RuntimeException)) {
            throw ((RuntimeException) failure);
        } else if (failure instanceof InvalidKeyException) {
            throw ((InvalidKeyException) failure);
        } else {
            throw new InvalidKeyException("Could not translate key", failure);
        }
    }
}
