package java.security;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Date;
import java.util.Enumeration;
import javax.crypto.SecretKey;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

public abstract class KeyStoreSpi {
    public abstract Enumeration<String> engineAliases();

    public abstract boolean engineContainsAlias(String str);

    public abstract void engineDeleteEntry(String str) throws KeyStoreException;

    public abstract Certificate engineGetCertificate(String str);

    public abstract String engineGetCertificateAlias(Certificate certificate);

    public abstract Certificate[] engineGetCertificateChain(String str);

    public abstract Date engineGetCreationDate(String str);

    public abstract Key engineGetKey(String str, char[] cArr) throws NoSuchAlgorithmException, UnrecoverableKeyException;

    public abstract boolean engineIsCertificateEntry(String str);

    public abstract boolean engineIsKeyEntry(String str);

    public abstract void engineLoad(InputStream inputStream, char[] cArr) throws IOException, NoSuchAlgorithmException, CertificateException;

    public abstract void engineSetCertificateEntry(String str, Certificate certificate) throws KeyStoreException;

    public abstract void engineSetKeyEntry(String str, Key key, char[] cArr, Certificate[] certificateArr) throws KeyStoreException;

    public abstract void engineSetKeyEntry(String str, byte[] bArr, Certificate[] certificateArr) throws KeyStoreException;

    public abstract int engineSize();

    public abstract void engineStore(OutputStream outputStream, char[] cArr) throws IOException, NoSuchAlgorithmException, CertificateException;

    public void engineStore(KeyStore.LoadStoreParameter param) throws IOException, NoSuchAlgorithmException, CertificateException {
        throw new UnsupportedOperationException();
    }

    public void engineLoad(KeyStore.LoadStoreParameter param) throws IOException, NoSuchAlgorithmException, CertificateException {
        char[] password;
        if (param == null) {
            engineLoad(null, null);
        } else if (param instanceof KeyStore.SimpleLoadStoreParameter) {
            KeyStore.ProtectionParameter protection = param.getProtectionParameter();
            if (protection instanceof KeyStore.PasswordProtection) {
                password = ((KeyStore.PasswordProtection) protection).getPassword();
            } else if (protection instanceof KeyStore.CallbackHandlerProtection) {
                CallbackHandler handler = ((KeyStore.CallbackHandlerProtection) protection).getCallbackHandler();
                PasswordCallback callback = new PasswordCallback("Password: ", false);
                try {
                    handler.handle(new Callback[]{callback});
                    char[] password2 = callback.getPassword();
                    callback.clearPassword();
                    if (password2 != null) {
                        password = password2;
                    } else {
                        throw new NoSuchAlgorithmException("No password provided");
                    }
                } catch (UnsupportedCallbackException e) {
                    throw new NoSuchAlgorithmException("Could not obtain password", e);
                }
            } else {
                throw new NoSuchAlgorithmException("ProtectionParameter must be PasswordProtection or CallbackHandlerProtection");
            }
            engineLoad(null, password);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public KeyStore.Entry engineGetEntry(String alias, KeyStore.ProtectionParameter protParam) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableEntryException {
        if (!engineContainsAlias(alias)) {
            return null;
        }
        if (protParam == null && engineIsCertificateEntry(alias)) {
            return new KeyStore.TrustedCertificateEntry(engineGetCertificate(alias));
        }
        if (protParam == null || (protParam instanceof KeyStore.PasswordProtection)) {
            if (engineIsCertificateEntry(alias)) {
                throw new UnsupportedOperationException("trusted certificate entries are not password-protected");
            } else if (engineIsKeyEntry(alias)) {
                char[] password = null;
                if (protParam != null) {
                    password = ((KeyStore.PasswordProtection) protParam).getPassword();
                }
                Key key = engineGetKey(alias, password);
                if (key instanceof PrivateKey) {
                    return new KeyStore.PrivateKeyEntry((PrivateKey) key, engineGetCertificateChain(alias));
                } else if (key instanceof SecretKey) {
                    return new KeyStore.SecretKeyEntry((SecretKey) key);
                }
            }
        }
        throw new UnsupportedOperationException();
    }

    public void engineSetEntry(String alias, KeyStore.Entry entry, KeyStore.ProtectionParameter protParam) throws KeyStoreException {
        if (protParam == null || (protParam instanceof KeyStore.PasswordProtection)) {
            KeyStore.PasswordProtection pProtect = null;
            if (protParam != null) {
                pProtect = (KeyStore.PasswordProtection) protParam;
            }
            char[] password = pProtect == null ? null : pProtect.getPassword();
            if (entry instanceof KeyStore.TrustedCertificateEntry) {
                engineSetCertificateEntry(alias, ((KeyStore.TrustedCertificateEntry) entry).getTrustedCertificate());
            } else if (entry instanceof KeyStore.PrivateKeyEntry) {
                engineSetKeyEntry(alias, ((KeyStore.PrivateKeyEntry) entry).getPrivateKey(), password, ((KeyStore.PrivateKeyEntry) entry).getCertificateChain());
            } else if (entry instanceof KeyStore.SecretKeyEntry) {
                engineSetKeyEntry(alias, ((KeyStore.SecretKeyEntry) entry).getSecretKey(), password, null);
            } else {
                throw new KeyStoreException("unsupported entry type: " + entry.getClass().getName());
            }
        } else {
            throw new KeyStoreException("unsupported protection parameter");
        }
    }

    public boolean engineEntryInstanceOf(String alias, Class<? extends KeyStore.Entry> entryClass) {
        if (entryClass == KeyStore.TrustedCertificateEntry.class) {
            return engineIsCertificateEntry(alias);
        }
        boolean z = true;
        if (entryClass == KeyStore.PrivateKeyEntry.class) {
            if (!engineIsKeyEntry(alias) || engineGetCertificate(alias) == null) {
                z = false;
            }
            return z;
        } else if (entryClass != KeyStore.SecretKeyEntry.class) {
            return false;
        } else {
            if (!engineIsKeyEntry(alias) || engineGetCertificate(alias) != null) {
                z = false;
            }
            return z;
        }
    }
}
