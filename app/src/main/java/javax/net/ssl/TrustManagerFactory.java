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
import sun.security.jca.GetInstance.Instance;

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

    protected TrustManagerFactory(TrustManagerFactorySpi factorySpi, Provider provider, String algorithm) {
        this.factorySpi = factorySpi;
        this.provider = provider;
        this.algorithm = algorithm;
    }

    public final String getAlgorithm() {
        return this.algorithm;
    }

    public static final TrustManagerFactory getInstance(String algorithm) throws NoSuchAlgorithmException {
        Instance instance = GetInstance.getInstance("TrustManagerFactory", TrustManagerFactorySpi.class, algorithm);
        return new TrustManagerFactory((TrustManagerFactorySpi) instance.impl, instance.provider, algorithm);
    }

    public static final TrustManagerFactory getInstance(String algorithm, String provider) throws NoSuchAlgorithmException, NoSuchProviderException {
        Instance instance = GetInstance.getInstance("TrustManagerFactory", TrustManagerFactorySpi.class, algorithm, provider);
        return new TrustManagerFactory((TrustManagerFactorySpi) instance.impl, instance.provider, algorithm);
    }

    public static final TrustManagerFactory getInstance(String algorithm, Provider provider) throws NoSuchAlgorithmException {
        Instance instance = GetInstance.getInstance("TrustManagerFactory", TrustManagerFactorySpi.class, algorithm, provider);
        return new TrustManagerFactory((TrustManagerFactorySpi) instance.impl, instance.provider, algorithm);
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
