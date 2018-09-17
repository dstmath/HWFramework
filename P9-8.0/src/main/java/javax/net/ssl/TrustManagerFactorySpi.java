package javax.net.ssl;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;

public abstract class TrustManagerFactorySpi {
    protected abstract TrustManager[] engineGetTrustManagers();

    protected abstract void engineInit(KeyStore keyStore) throws KeyStoreException;

    protected abstract void engineInit(ManagerFactoryParameters managerFactoryParameters) throws InvalidAlgorithmParameterException;
}
