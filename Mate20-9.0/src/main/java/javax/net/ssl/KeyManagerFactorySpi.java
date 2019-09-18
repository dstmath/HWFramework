package javax.net.ssl;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

public abstract class KeyManagerFactorySpi {
    /* access modifiers changed from: protected */
    public abstract KeyManager[] engineGetKeyManagers();

    /* access modifiers changed from: protected */
    public abstract void engineInit(KeyStore keyStore, char[] cArr) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException;

    /* access modifiers changed from: protected */
    public abstract void engineInit(ManagerFactoryParameters managerFactoryParameters) throws InvalidAlgorithmParameterException;
}
