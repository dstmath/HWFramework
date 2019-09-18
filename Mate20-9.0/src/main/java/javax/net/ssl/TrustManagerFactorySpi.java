package javax.net.ssl;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;

public abstract class TrustManagerFactorySpi {
    /* access modifiers changed from: protected */
    public abstract TrustManager[] engineGetTrustManagers();

    /* access modifiers changed from: protected */
    public abstract void engineInit(KeyStore keyStore) throws KeyStoreException;

    /* access modifiers changed from: protected */
    public abstract void engineInit(ManagerFactoryParameters managerFactoryParameters) throws InvalidAlgorithmParameterException;
}
