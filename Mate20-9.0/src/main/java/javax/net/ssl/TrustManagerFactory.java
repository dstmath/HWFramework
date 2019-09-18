package javax.net.ssl;

import java.security.AccessController;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.security.Security;
import sun.security.jca.GetInstance;

public class TrustManagerFactory {
    private String algorithm;
    private TrustManagerFactorySpi factorySpi;
    private Provider provider;

    public static final String getDefaultAlgorithm() {
        String type = (String) AccessController.doPrivileged(new PrivilegedAction<String>() {
            public String run() {
                return Security.getProperty("ssl.TrustManagerFactory.algorithm");
            }
        });
        if (type == null) {
            return "SunX509";
        }
        return type;
    }

    protected TrustManagerFactory(TrustManagerFactorySpi factorySpi2, Provider provider2, String algorithm2) {
        this.factorySpi = factorySpi2;
        this.provider = provider2;
        this.algorithm = algorithm2;
    }

    public final String getAlgorithm() {
        return this.algorithm;
    }

    public static final TrustManagerFactory getInstance(String algorithm2) throws NoSuchAlgorithmException {
        GetInstance.Instance instance = GetInstance.getInstance("TrustManagerFactory", (Class<?>) TrustManagerFactorySpi.class, algorithm2);
        return new TrustManagerFactory((TrustManagerFactorySpi) instance.impl, instance.provider, algorithm2);
    }

    public static final TrustManagerFactory getInstance(String algorithm2, String provider2) throws NoSuchAlgorithmException, NoSuchProviderException {
        GetInstance.Instance instance = GetInstance.getInstance("TrustManagerFactory", (Class<?>) TrustManagerFactorySpi.class, algorithm2, provider2);
        return new TrustManagerFactory((TrustManagerFactorySpi) instance.impl, instance.provider, algorithm2);
    }

    public static final TrustManagerFactory getInstance(String algorithm2, Provider provider2) throws NoSuchAlgorithmException {
        GetInstance.Instance instance = GetInstance.getInstance("TrustManagerFactory", (Class<?>) TrustManagerFactorySpi.class, algorithm2, provider2);
        return new TrustManagerFactory((TrustManagerFactorySpi) instance.impl, instance.provider, algorithm2);
    }

    public final Provider getProvider() {
        return this.provider;
    }

    public final void init(KeyStore ks) throws KeyStoreException {
        this.factorySpi.engineInit(ks);
    }

    public final void init(ManagerFactoryParameters spec) throws InvalidAlgorithmParameterException {
        this.factorySpi.engineInit(spec);
    }

    public final TrustManager[] getTrustManagers() {
        return this.factorySpi.engineGetTrustManagers();
    }
}
