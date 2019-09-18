package javax.crypto;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Iterator;
import sun.security.jca.GetInstance;
import sun.security.jca.Providers;

public class SecretKeyFactory {
    private final String algorithm;
    private final Object lock = new Object();
    private Provider provider;
    private Iterator<Provider.Service> serviceIterator;
    private volatile SecretKeyFactorySpi spi;

    protected SecretKeyFactory(SecretKeyFactorySpi keyFacSpi, Provider provider2, String algorithm2) {
        this.spi = keyFacSpi;
        this.provider = provider2;
        this.algorithm = algorithm2;
    }

    private SecretKeyFactory(String algorithm2) throws NoSuchAlgorithmException {
        this.algorithm = algorithm2;
        this.serviceIterator = GetInstance.getServices("SecretKeyFactory", algorithm2).iterator();
        if (nextSpi(null) == null) {
            throw new NoSuchAlgorithmException(algorithm2 + " SecretKeyFactory not available");
        }
    }

    public static final SecretKeyFactory getInstance(String algorithm2) throws NoSuchAlgorithmException {
        return new SecretKeyFactory(algorithm2);
    }

    public static final SecretKeyFactory getInstance(String algorithm2, String provider2) throws NoSuchAlgorithmException, NoSuchProviderException {
        Providers.checkBouncyCastleDeprecation(provider2, "SecretKeyFactory", algorithm2);
        GetInstance.Instance instance = JceSecurity.getInstance("SecretKeyFactory", (Class<?>) SecretKeyFactorySpi.class, algorithm2, provider2);
        return new SecretKeyFactory((SecretKeyFactorySpi) instance.impl, instance.provider, algorithm2);
    }

    public static final SecretKeyFactory getInstance(String algorithm2, Provider provider2) throws NoSuchAlgorithmException {
        Providers.checkBouncyCastleDeprecation(provider2, "SecretKeyFactory", algorithm2);
        GetInstance.Instance instance = JceSecurity.getInstance("SecretKeyFactory", (Class<?>) SecretKeyFactorySpi.class, algorithm2, provider2);
        return new SecretKeyFactory((SecretKeyFactorySpi) instance.impl, instance.provider, algorithm2);
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

    private SecretKeyFactorySpi nextSpi(SecretKeyFactorySpi oldSpi) {
        synchronized (this.lock) {
            if (oldSpi != null) {
                try {
                    if (oldSpi != this.spi) {
                        SecretKeyFactorySpi secretKeyFactorySpi = this.spi;
                        return secretKeyFactorySpi;
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
                if (JceSecurity.canUseProvider(s.getProvider())) {
                    Object obj = s.newInstance(null);
                    if (obj instanceof SecretKeyFactorySpi) {
                        SecretKeyFactorySpi spi2 = (SecretKeyFactorySpi) obj;
                        this.provider = s.getProvider();
                        this.spi = spi2;
                        return spi2;
                    }
                }
            }
            this.serviceIterator = null;
            return null;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x0021  */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0025  */
    public final SecretKey generateSecret(KeySpec keySpec) throws InvalidKeySpecException {
        if (this.serviceIterator == null) {
            return this.spi.engineGenerateSecret(keySpec);
        }
        Exception failure = null;
        SecretKeyFactorySpi mySpi = this.spi;
        do {
            try {
                return mySpi.engineGenerateSecret(keySpec);
            } catch (Exception e) {
                if (failure == null) {
                    failure = e;
                }
                mySpi = nextSpi(mySpi);
                if (mySpi == null) {
                    if (!(failure instanceof InvalidKeySpecException)) {
                    }
                }
            }
        } while (mySpi == null);
        if (!(failure instanceof InvalidKeySpecException)) {
            throw ((InvalidKeySpecException) failure);
        }
        throw new InvalidKeySpecException("Could not generate secret key", failure);
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x0021  */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0025  */
    public final KeySpec getKeySpec(SecretKey key, Class<?> keySpec) throws InvalidKeySpecException {
        if (this.serviceIterator == null) {
            return this.spi.engineGetKeySpec(key, keySpec);
        }
        Exception failure = null;
        SecretKeyFactorySpi mySpi = this.spi;
        do {
            try {
                return mySpi.engineGetKeySpec(key, keySpec);
            } catch (Exception e) {
                if (failure == null) {
                    failure = e;
                }
                mySpi = nextSpi(mySpi);
                if (mySpi == null) {
                    if (!(failure instanceof InvalidKeySpecException)) {
                    }
                }
            }
        } while (mySpi == null);
        if (!(failure instanceof InvalidKeySpecException)) {
            throw ((InvalidKeySpecException) failure);
        }
        throw new InvalidKeySpecException("Could not get key spec", failure);
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x0021  */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0025  */
    public final SecretKey translateKey(SecretKey key) throws InvalidKeyException {
        if (this.serviceIterator == null) {
            return this.spi.engineTranslateKey(key);
        }
        Exception failure = null;
        SecretKeyFactorySpi mySpi = this.spi;
        do {
            try {
                return mySpi.engineTranslateKey(key);
            } catch (Exception e) {
                if (failure == null) {
                    failure = e;
                }
                mySpi = nextSpi(mySpi);
                if (mySpi == null) {
                    if (!(failure instanceof InvalidKeyException)) {
                    }
                }
            }
        } while (mySpi == null);
        if (!(failure instanceof InvalidKeyException)) {
            throw ((InvalidKeyException) failure);
        }
        throw new InvalidKeyException("Could not translate key", failure);
    }
}
