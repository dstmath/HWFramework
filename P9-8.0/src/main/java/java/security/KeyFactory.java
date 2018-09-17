package java.security;

import java.security.Provider.Service;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Iterator;
import sun.security.jca.GetInstance;
import sun.security.jca.GetInstance.Instance;
import sun.security.util.Debug;

public class KeyFactory {
    private static final Debug debug = Debug.getInstance("jca", "KeyFactory");
    private final String algorithm;
    private final Object lock = new Object();
    private Provider provider;
    private Iterator<Service> serviceIterator;
    private volatile KeyFactorySpi spi;

    protected KeyFactory(KeyFactorySpi keyFacSpi, Provider provider, String algorithm) {
        this.spi = keyFacSpi;
        this.provider = provider;
        this.algorithm = algorithm;
    }

    private KeyFactory(String algorithm) throws NoSuchAlgorithmException {
        this.algorithm = algorithm;
        this.serviceIterator = GetInstance.getServices("KeyFactory", algorithm).iterator();
        if (nextSpi(null) == null) {
            throw new NoSuchAlgorithmException(algorithm + " KeyFactory not available");
        }
    }

    public static KeyFactory getInstance(String algorithm) throws NoSuchAlgorithmException {
        return new KeyFactory(algorithm);
    }

    public static KeyFactory getInstance(String algorithm, String provider) throws NoSuchAlgorithmException, NoSuchProviderException {
        Instance instance = GetInstance.getInstance("KeyFactory", KeyFactorySpi.class, algorithm, provider);
        return new KeyFactory((KeyFactorySpi) instance.impl, instance.provider, algorithm);
    }

    public static KeyFactory getInstance(String algorithm, Provider provider) throws NoSuchAlgorithmException {
        Instance instance = GetInstance.getInstance("KeyFactory", KeyFactorySpi.class, algorithm, provider);
        return new KeyFactory((KeyFactorySpi) instance.impl, instance.provider, algorithm);
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

    private KeyFactorySpi nextSpi(KeyFactorySpi oldSpi) {
        synchronized (this.lock) {
            if (oldSpi != null) {
                if (oldSpi != this.spi) {
                    KeyFactorySpi keyFactorySpi = this.spi;
                    return keyFactorySpi;
                }
            }
            if (this.serviceIterator == null) {
                return null;
            }
            while (this.serviceIterator.hasNext()) {
                Service s = (Service) this.serviceIterator.next();
                try {
                    Object obj = s.newInstance(null);
                    if (obj instanceof KeyFactorySpi) {
                        KeyFactorySpi spi = (KeyFactorySpi) obj;
                        this.provider = s.getProvider();
                        this.spi = spi;
                        return spi;
                    }
                } catch (NoSuchAlgorithmException e) {
                }
            }
            this.serviceIterator = null;
            return null;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0024  */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0021  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
                    if (failure instanceof RuntimeException) {
                    }
                }
            }
        } while (mySpi == null);
        if (failure instanceof RuntimeException) {
            throw ((RuntimeException) failure);
        } else if (failure instanceof InvalidKeySpecException) {
            throw ((InvalidKeySpecException) failure);
        } else {
            throw new InvalidKeySpecException("Could not generate public key", failure);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0024  */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0021  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
                    if (failure instanceof RuntimeException) {
                    }
                }
            }
        } while (mySpi == null);
        if (failure instanceof RuntimeException) {
            throw ((RuntimeException) failure);
        } else if (failure instanceof InvalidKeySpecException) {
            throw ((InvalidKeySpecException) failure);
        } else {
            throw new InvalidKeySpecException("Could not generate private key", failure);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0024  */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0021  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
                    if (failure instanceof RuntimeException) {
                    }
                }
            }
        } while (mySpi == null);
        if (failure instanceof RuntimeException) {
            throw ((RuntimeException) failure);
        } else if (failure instanceof InvalidKeySpecException) {
            throw ((InvalidKeySpecException) failure);
        } else {
            throw new InvalidKeySpecException("Could not get key spec", failure);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0024  */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0021  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
                    if (failure instanceof RuntimeException) {
                    }
                }
            }
        } while (mySpi == null);
        if (failure instanceof RuntimeException) {
            throw ((RuntimeException) failure);
        } else if (failure instanceof InvalidKeyException) {
            throw ((InvalidKeyException) failure);
        } else {
            throw new InvalidKeyException("Could not translate key", failure);
        }
    }
}
