package javax.crypto;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Provider.Service;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Iterator;
import sun.security.jca.GetInstance;
import sun.security.jca.GetInstance.Instance;

public class SecretKeyFactory {
    private final String algorithm;
    private final Object lock = new Object();
    private Provider provider;
    private Iterator<Service> serviceIterator;
    private volatile SecretKeyFactorySpi spi;

    protected SecretKeyFactory(SecretKeyFactorySpi keyFacSpi, Provider provider, String algorithm) {
        this.spi = keyFacSpi;
        this.provider = provider;
        this.algorithm = algorithm;
    }

    private SecretKeyFactory(String algorithm) throws NoSuchAlgorithmException {
        this.algorithm = algorithm;
        this.serviceIterator = GetInstance.getServices("SecretKeyFactory", algorithm).iterator();
        if (nextSpi(null) == null) {
            throw new NoSuchAlgorithmException(algorithm + " SecretKeyFactory not available");
        }
    }

    public static final SecretKeyFactory getInstance(String algorithm) throws NoSuchAlgorithmException {
        return new SecretKeyFactory(algorithm);
    }

    public static final SecretKeyFactory getInstance(String algorithm, String provider) throws NoSuchAlgorithmException, NoSuchProviderException {
        Instance instance = JceSecurity.getInstance("SecretKeyFactory", SecretKeyFactorySpi.class, algorithm, provider);
        return new SecretKeyFactory((SecretKeyFactorySpi) instance.impl, instance.provider, algorithm);
    }

    public static final SecretKeyFactory getInstance(String algorithm, Provider provider) throws NoSuchAlgorithmException {
        Instance instance = JceSecurity.getInstance("SecretKeyFactory", SecretKeyFactorySpi.class, algorithm, provider);
        return new SecretKeyFactory((SecretKeyFactorySpi) instance.impl, instance.provider, algorithm);
    }

    public final Provider getProvider() {
        Provider provider;
        synchronized (this.lock) {
            this.serviceIterator = null;
            provider = this.provider;
        }
        return provider;
    }

    public final String getAlgorithm() {
        return this.algorithm;
    }

    private SecretKeyFactorySpi nextSpi(SecretKeyFactorySpi oldSpi) {
        synchronized (this.lock) {
            if (oldSpi != null) {
                if (oldSpi != this.spi) {
                    SecretKeyFactorySpi secretKeyFactorySpi = this.spi;
                    return secretKeyFactorySpi;
                }
            }
            if (this.serviceIterator == null) {
                return null;
            }
            while (this.serviceIterator.hasNext()) {
                Service s = (Service) this.serviceIterator.next();
                if (JceSecurity.canUseProvider(s.getProvider())) {
                    try {
                        Object obj = s.newInstance(null);
                        if (obj instanceof SecretKeyFactorySpi) {
                            SecretKeyFactorySpi spi = (SecretKeyFactorySpi) obj;
                            this.provider = s.getProvider();
                            this.spi = spi;
                            return spi;
                        }
                    } catch (NoSuchAlgorithmException e) {
                    }
                }
            }
            this.serviceIterator = null;
            return null;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0024  */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0021  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
                    if (failure instanceof InvalidKeySpecException) {
                    }
                }
            }
        } while (mySpi == null);
        if (failure instanceof InvalidKeySpecException) {
            throw ((InvalidKeySpecException) failure);
        }
        throw new InvalidKeySpecException("Could not generate secret key", failure);
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0024  */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0021  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
                    if (failure instanceof InvalidKeySpecException) {
                    }
                }
            }
        } while (mySpi == null);
        if (failure instanceof InvalidKeySpecException) {
            throw ((InvalidKeySpecException) failure);
        }
        throw new InvalidKeySpecException("Could not get key spec", failure);
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0024  */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0021  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
                    if (failure instanceof InvalidKeyException) {
                    }
                }
            }
        } while (mySpi == null);
        if (failure instanceof InvalidKeyException) {
            throw ((InvalidKeyException) failure);
        }
        throw new InvalidKeyException("Could not translate key", failure);
    }
}
