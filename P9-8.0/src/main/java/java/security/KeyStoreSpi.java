package java.security;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore.CallbackHandlerProtection;
import java.security.KeyStore.Entry;
import java.security.KeyStore.LoadStoreParameter;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStore.ProtectionParameter;
import java.security.KeyStore.SecretKeyEntry;
import java.security.KeyStore.TrustedCertificateEntry;
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

    public void engineStore(LoadStoreParameter param) throws IOException, NoSuchAlgorithmException, CertificateException {
        throw new UnsupportedOperationException();
    }

    public void engineLoad(LoadStoreParameter param) throws IOException, NoSuchAlgorithmException, CertificateException {
        if (param == null) {
            engineLoad(null, (char[]) null);
        } else if (param instanceof SimpleLoadStoreParameter) {
            char[] password;
            ProtectionParameter protection = param.getProtectionParameter();
            if (protection instanceof PasswordProtection) {
                password = ((PasswordProtection) protection).getPassword();
            } else if (protection instanceof CallbackHandlerProtection) {
                CallbackHandler handler = ((CallbackHandlerProtection) protection).getCallbackHandler();
                PasswordCallback callback = new PasswordCallback("Password: ", false);
                try {
                    handler.handle(new Callback[]{callback});
                    password = callback.getPassword();
                    callback.clearPassword();
                    if (password == null) {
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

    public Entry engineGetEntry(String alias, ProtectionParameter protParam) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableEntryException {
        if (!engineContainsAlias(alias)) {
            return null;
        }
        if (protParam != null && ((protParam instanceof PasswordProtection) ^ 1) != 0) {
            throw new UnsupportedOperationException();
        } else if (!engineIsCertificateEntry(alias)) {
            if (engineIsKeyEntry(alias)) {
                char[] password = null;
                if (protParam != null) {
                    password = ((PasswordProtection) protParam).getPassword();
                }
                Key key = engineGetKey(alias, password);
                if (key instanceof PrivateKey) {
                    return new PrivateKeyEntry((PrivateKey) key, engineGetCertificateChain(alias));
                } else if (key instanceof SecretKey) {
                    return new SecretKeyEntry((SecretKey) key);
                }
            }
            throw new UnsupportedOperationException();
        } else if (protParam == null) {
            return new TrustedCertificateEntry(engineGetCertificate(alias));
        } else {
            throw new UnsupportedOperationException("trusted certificate entries are not password-protected");
        }
    }

    public void engineSetEntry(String alias, Entry entry, ProtectionParameter protParam) throws KeyStoreException {
        if (protParam == null || ((protParam instanceof PasswordProtection) ^ 1) == 0) {
            PasswordProtection pProtect = null;
            if (protParam != null) {
                pProtect = (PasswordProtection) protParam;
            }
            char[] password = pProtect == null ? null : pProtect.getPassword();
            if (entry instanceof TrustedCertificateEntry) {
                engineSetCertificateEntry(alias, ((TrustedCertificateEntry) entry).getTrustedCertificate());
                return;
            } else if (entry instanceof PrivateKeyEntry) {
                engineSetKeyEntry(alias, ((PrivateKeyEntry) entry).getPrivateKey(), password, ((PrivateKeyEntry) entry).getCertificateChain());
                return;
            } else if (entry instanceof SecretKeyEntry) {
                engineSetKeyEntry(alias, ((SecretKeyEntry) entry).getSecretKey(), password, (Certificate[]) null);
                return;
            } else {
                throw new KeyStoreException("unsupported entry type: " + entry.getClass().getName());
            }
        }
        throw new KeyStoreException("unsupported protection parameter");
    }

    public boolean engineEntryInstanceOf(String alias, Class<? extends Entry> entryClass) {
        boolean z = true;
        boolean z2 = false;
        if (entryClass == TrustedCertificateEntry.class) {
            return engineIsCertificateEntry(alias);
        }
        if (entryClass == PrivateKeyEntry.class) {
            if (!engineIsKeyEntry(alias)) {
                z = false;
            } else if (engineGetCertificate(alias) == null) {
                z = false;
            }
            return z;
        } else if (entryClass != SecretKeyEntry.class) {
            return false;
        } else {
            if (engineIsKeyEntry(alias) && engineGetCertificate(alias) == null) {
                z2 = true;
            }
            return z2;
        }
    }
}
