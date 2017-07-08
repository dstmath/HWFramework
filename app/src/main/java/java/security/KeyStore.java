package java.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import javax.crypto.SecretKey;
import javax.security.auth.DestroyFailedException;
import javax.security.auth.Destroyable;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;

public class KeyStore {
    private static final String KEYSTORE_TYPE = "keystore.type";
    private boolean initialized;
    private KeyStoreSpi keyStoreSpi;
    private Provider provider;
    private String type;

    public static abstract class Builder {
        static final int MAX_CALLBACK_TRIES = 3;

        /* renamed from: java.security.KeyStore.Builder.1 */
        static class AnonymousClass1 extends Builder {
            private volatile boolean getCalled;
            final /* synthetic */ KeyStore val$keyStore;
            final /* synthetic */ ProtectionParameter val$protectionParameter;

            AnonymousClass1(KeyStore val$keyStore, ProtectionParameter val$protectionParameter) {
                this.val$keyStore = val$keyStore;
                this.val$protectionParameter = val$protectionParameter;
            }

            public KeyStore getKeyStore() {
                this.getCalled = true;
                return this.val$keyStore;
            }

            public ProtectionParameter getProtectionParameter(String alias) {
                if (alias == null) {
                    throw new NullPointerException();
                } else if (this.getCalled) {
                    return this.val$protectionParameter;
                } else {
                    throw new IllegalStateException("getKeyStore() must be called first");
                }
            }
        }

        /* renamed from: java.security.KeyStore.Builder.2 */
        static class AnonymousClass2 extends Builder {
            private final PrivilegedExceptionAction<KeyStore> action;
            private volatile boolean getCalled;
            private IOException oldException;
            final /* synthetic */ AccessControlContext val$context;
            final /* synthetic */ ProtectionParameter val$protection;
            final /* synthetic */ Provider val$provider;
            final /* synthetic */ String val$type;

            /* renamed from: java.security.KeyStore.Builder.2.1 */
            class AnonymousClass1 implements PrivilegedExceptionAction<KeyStore> {
                final /* synthetic */ ProtectionParameter val$protection;
                final /* synthetic */ Provider val$provider;
                final /* synthetic */ String val$type;

                AnonymousClass1(Provider val$provider, String val$type, ProtectionParameter val$protection) {
                    this.val$provider = val$provider;
                    this.val$type = val$type;
                    this.val$protection = val$protection;
                }

                public KeyStore run() throws Exception {
                    KeyStore ks;
                    if (this.val$provider == null) {
                        ks = KeyStore.getInstance(this.val$type);
                    } else {
                        ks = KeyStore.getInstance(this.val$type, this.val$provider);
                    }
                    LoadStoreParameter param = new SimpleLoadStoreParameter(this.val$protection);
                    if (this.val$protection instanceof CallbackHandlerProtection) {
                        int tries = 0;
                        do {
                            tries++;
                            try {
                                ks.load(param);
                            } catch (IOException e) {
                                if (!(e.getCause() instanceof UnrecoverableKeyException)) {
                                    break;
                                } else if (tries >= Builder.MAX_CALLBACK_TRIES) {
                                    AnonymousClass2.this.oldException = e;
                                }
                                throw e;
                            }
                        } while (tries >= Builder.MAX_CALLBACK_TRIES);
                        AnonymousClass2.this.oldException = e;
                        throw e;
                    }
                    ks.load(param);
                    AnonymousClass2.this.getCalled = true;
                    return ks;
                }
            }

            AnonymousClass2(AccessControlContext val$context, ProtectionParameter val$protection, Provider val$provider, String val$type) {
                this.val$context = val$context;
                this.val$protection = val$protection;
                this.val$provider = val$provider;
                this.val$type = val$type;
                this.action = new AnonymousClass1(this.val$provider, this.val$type, this.val$protection);
            }

            public synchronized KeyStore getKeyStore() throws KeyStoreException {
                if (this.oldException != null) {
                    throw new KeyStoreException("Previous KeyStore instantiation failed", this.oldException);
                }
                try {
                } catch (PrivilegedActionException e) {
                    throw new KeyStoreException("KeyStore instantiation failed", e.getCause());
                }
                return (KeyStore) AccessController.doPrivileged(this.action, this.val$context);
            }

            public ProtectionParameter getProtectionParameter(String alias) {
                if (alias == null) {
                    throw new NullPointerException();
                } else if (this.getCalled) {
                    return this.val$protection;
                } else {
                    throw new IllegalStateException("getKeyStore() must be called first");
                }
            }
        }

        private static final class FileBuilder extends Builder {
            private final AccessControlContext context;
            private final File file;
            private ProtectionParameter keyProtection;
            private KeyStore keyStore;
            private Throwable oldException;
            private ProtectionParameter protection;
            private final Provider provider;
            private final String type;

            FileBuilder(String type, Provider provider, File file, ProtectionParameter protection, AccessControlContext context) {
                this.type = type;
                this.provider = provider;
                this.file = file;
                this.protection = protection;
                this.context = context;
            }

            public synchronized KeyStore getKeyStore() throws KeyStoreException {
                if (this.keyStore != null) {
                    return this.keyStore;
                } else if (this.oldException != null) {
                    throw new KeyStoreException("Previous KeyStore instantiation failed", this.oldException);
                } else {
                    try {
                        this.keyStore = (KeyStore) AccessController.doPrivileged(new PrivilegedExceptionAction<KeyStore>() {
                            public KeyStore run() throws Exception {
                                if (!(FileBuilder.this.protection instanceof CallbackHandlerProtection)) {
                                    return run0();
                                }
                                int tries = 0;
                                do {
                                    tries++;
                                    try {
                                        return run0();
                                    } catch (IOException e) {
                                        if (tries >= Builder.MAX_CALLBACK_TRIES) {
                                            break;
                                        } else if (!(e.getCause() instanceof UnrecoverableKeyException)) {
                                        }
                                        throw e;
                                    }
                                } while (e.getCause() instanceof UnrecoverableKeyException);
                                throw e;
                            }

                            public KeyStore run0() throws Exception {
                                KeyStore ks;
                                Throwable th;
                                if (FileBuilder.this.provider == null) {
                                    ks = KeyStore.getInstance(FileBuilder.this.type);
                                } else {
                                    ks = KeyStore.getInstance(FileBuilder.this.type, FileBuilder.this.provider);
                                }
                                InputStream inputStream = null;
                                try {
                                    InputStream in = new FileInputStream(FileBuilder.this.file);
                                    try {
                                        char[] password;
                                        if (FileBuilder.this.protection instanceof PasswordProtection) {
                                            password = ((PasswordProtection) FileBuilder.this.protection).getPassword();
                                            FileBuilder.this.keyProtection = FileBuilder.this.protection;
                                        } else {
                                            CallbackHandler handler = ((CallbackHandlerProtection) FileBuilder.this.protection).getCallbackHandler();
                                            PasswordCallback callback = new PasswordCallback("Password for keystore " + FileBuilder.this.file.getName(), false);
                                            handler.handle(new Callback[]{callback});
                                            password = callback.getPassword();
                                            if (password == null) {
                                                throw new KeyStoreException("No password provided");
                                            }
                                            callback.clearPassword();
                                            FileBuilder.this.keyProtection = new PasswordProtection(password);
                                        }
                                        ks.load(in, password);
                                        if (in != null) {
                                            in.close();
                                        }
                                        return ks;
                                    } catch (Throwable th2) {
                                        th = th2;
                                        inputStream = in;
                                        if (inputStream != null) {
                                            inputStream.close();
                                        }
                                        throw th;
                                    }
                                } catch (Throwable th3) {
                                    th = th3;
                                    if (inputStream != null) {
                                        inputStream.close();
                                    }
                                    throw th;
                                }
                            }
                        }, this.context);
                        return this.keyStore;
                    } catch (PrivilegedActionException e) {
                        this.oldException = e.getCause();
                        throw new KeyStoreException("KeyStore instantiation failed", this.oldException);
                    }
                }
            }

            public synchronized ProtectionParameter getProtectionParameter(String alias) {
                if (alias == null) {
                    throw new NullPointerException();
                } else if (this.keyStore == null) {
                    throw new IllegalStateException("getKeyStore() must be called first");
                }
                return this.keyProtection;
            }
        }

        public abstract KeyStore getKeyStore() throws KeyStoreException;

        public abstract ProtectionParameter getProtectionParameter(String str) throws KeyStoreException;

        protected Builder() {
        }

        public static Builder newInstance(KeyStore keyStore, ProtectionParameter protectionParameter) {
            if (keyStore == null || protectionParameter == null) {
                throw new NullPointerException();
            } else if (keyStore.initialized) {
                return new AnonymousClass1(keyStore, protectionParameter);
            } else {
                throw new IllegalArgumentException("KeyStore not initialized");
            }
        }

        public static Builder newInstance(String type, Provider provider, File file, ProtectionParameter protection) {
            if (type == null || file == null || protection == null) {
                throw new NullPointerException();
            } else if (!(protection instanceof PasswordProtection) && !(protection instanceof CallbackHandlerProtection)) {
                throw new IllegalArgumentException("Protection must be PasswordProtection or CallbackHandlerProtection");
            } else if (file.isFile()) {
                return new FileBuilder(type, provider, file, protection, AccessController.getContext());
            } else {
                throw new IllegalArgumentException("File does not exist or it does not refer to a normal file: " + file);
            }
        }

        public static Builder newInstance(String type, Provider provider, ProtectionParameter protection) {
            if (type != null && protection != null) {
                return new AnonymousClass2(AccessController.getContext(), protection, provider, type);
            }
            throw new NullPointerException();
        }
    }

    public interface ProtectionParameter {
    }

    public static class CallbackHandlerProtection implements ProtectionParameter {
        private final CallbackHandler handler;

        public CallbackHandlerProtection(CallbackHandler handler) {
            if (handler == null) {
                throw new NullPointerException("handler must not be null");
            }
            this.handler = handler;
        }

        public CallbackHandler getCallbackHandler() {
            return this.handler;
        }
    }

    public interface Entry {
    }

    public interface LoadStoreParameter {
        ProtectionParameter getProtectionParameter();
    }

    public static class PasswordProtection implements ProtectionParameter, Destroyable {
        private volatile boolean destroyed;
        private final char[] password;

        public PasswordProtection(char[] password) {
            char[] cArr = null;
            this.destroyed = false;
            if (password != null) {
                cArr = (char[]) password.clone();
            }
            this.password = cArr;
        }

        public synchronized char[] getPassword() {
            if (this.destroyed) {
                throw new IllegalStateException("password has been cleared");
            }
            return this.password;
        }

        public synchronized void destroy() throws DestroyFailedException {
            this.destroyed = true;
            if (this.password != null) {
                Arrays.fill(this.password, ' ');
            }
        }

        public synchronized boolean isDestroyed() {
            return this.destroyed;
        }
    }

    public static final class PrivateKeyEntry implements Entry {
        private final Certificate[] chain;
        private final PrivateKey privKey;

        public PrivateKeyEntry(PrivateKey privateKey, Certificate[] chain) {
            if (privateKey == null || chain == null) {
                throw new NullPointerException("invalid null input");
            } else if (chain.length == 0) {
                throw new IllegalArgumentException("invalid zero-length input chain");
            } else {
                Object clonedChain = (Certificate[]) chain.clone();
                String certType = clonedChain[0].getType();
                int i = 1;
                while (i < clonedChain.length) {
                    if (certType.equals(clonedChain[i].getType())) {
                        i++;
                    } else {
                        throw new IllegalArgumentException("chain does not contain certificates of the same type");
                    }
                }
                if (privateKey.getAlgorithm().equals(clonedChain[0].getPublicKey().getAlgorithm())) {
                    this.privKey = privateKey;
                    if (!(clonedChain[0] instanceof X509Certificate) || (clonedChain instanceof X509Certificate[])) {
                        this.chain = clonedChain;
                        return;
                    }
                    this.chain = new X509Certificate[clonedChain.length];
                    System.arraycopy(clonedChain, 0, this.chain, 0, clonedChain.length);
                    return;
                }
                throw new IllegalArgumentException("private key algorithm does not match algorithm of public key in end entity certificate (at index 0)");
            }
        }

        public PrivateKey getPrivateKey() {
            return this.privKey;
        }

        public Certificate[] getCertificateChain() {
            return (Certificate[]) this.chain.clone();
        }

        public Certificate getCertificate() {
            return this.chain[0];
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Private key entry and certificate chain with ").append(this.chain.length).append(" elements:\r\n");
            for (Object cert : this.chain) {
                sb.append(cert);
                sb.append("\r\n");
            }
            return sb.toString();
        }
    }

    public static final class SecretKeyEntry implements Entry {
        private final SecretKey sKey;

        public SecretKeyEntry(SecretKey secretKey) {
            if (secretKey == null) {
                throw new NullPointerException("invalid null input");
            }
            this.sKey = secretKey;
        }

        public SecretKey getSecretKey() {
            return this.sKey;
        }

        public String toString() {
            return "Secret key entry with algorithm " + this.sKey.getAlgorithm();
        }
    }

    static class SimpleLoadStoreParameter implements LoadStoreParameter {
        private final ProtectionParameter protection;

        SimpleLoadStoreParameter(ProtectionParameter protection) {
            this.protection = protection;
        }

        public ProtectionParameter getProtectionParameter() {
            return this.protection;
        }
    }

    public static final class TrustedCertificateEntry implements Entry {
        private final Certificate cert;

        public TrustedCertificateEntry(Certificate trustedCert) {
            if (trustedCert == null) {
                throw new NullPointerException("invalid null input");
            }
            this.cert = trustedCert;
        }

        public Certificate getTrustedCertificate() {
            return this.cert;
        }

        public String toString() {
            return "Trusted certificate entry:\r\n" + this.cert.toString();
        }
    }

    protected KeyStore(KeyStoreSpi keyStoreSpi, Provider provider, String type) {
        this.initialized = false;
        this.keyStoreSpi = keyStoreSpi;
        this.provider = provider;
        this.type = type;
    }

    public static KeyStore getInstance(String type) throws KeyStoreException {
        try {
            Object[] objs = Security.getImpl(type, "KeyStore", (String) null);
            return new KeyStore((KeyStoreSpi) objs[0], (Provider) objs[1], type);
        } catch (NoSuchAlgorithmException nsae) {
            throw new KeyStoreException(type + " not found", nsae);
        } catch (NoSuchProviderException nspe) {
            throw new KeyStoreException(type + " not found", nspe);
        }
    }

    public static KeyStore getInstance(String type, String provider) throws KeyStoreException, NoSuchProviderException {
        if (provider == null || provider.length() == 0) {
            throw new IllegalArgumentException("missing provider");
        }
        try {
            Object[] objs = Security.getImpl(type, "KeyStore", provider);
            return new KeyStore((KeyStoreSpi) objs[0], (Provider) objs[1], type);
        } catch (NoSuchAlgorithmException nsae) {
            throw new KeyStoreException(type + " not found", nsae);
        }
    }

    public static KeyStore getInstance(String type, Provider provider) throws KeyStoreException {
        if (provider == null) {
            throw new IllegalArgumentException("missing provider");
        }
        try {
            Object[] objs = Security.getImpl(type, "KeyStore", provider);
            return new KeyStore((KeyStoreSpi) objs[0], (Provider) objs[1], type);
        } catch (NoSuchAlgorithmException nsae) {
            throw new KeyStoreException(type + " not found", nsae);
        }
    }

    public static final String getDefaultType() {
        String kstype = (String) AccessController.doPrivileged(new PrivilegedAction<String>() {
            public String run() {
                return Security.getProperty(KeyStore.KEYSTORE_TYPE);
            }
        });
        if (kstype == null) {
            return "jks";
        }
        return kstype;
    }

    public final Provider getProvider() {
        return this.provider;
    }

    public final String getType() {
        return this.type;
    }

    public final Key getKey(String alias, char[] password) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        if (this.initialized) {
            return this.keyStoreSpi.engineGetKey(alias, password);
        }
        throw new KeyStoreException("Uninitialized keystore");
    }

    public final Certificate[] getCertificateChain(String alias) throws KeyStoreException {
        if (this.initialized) {
            return this.keyStoreSpi.engineGetCertificateChain(alias);
        }
        throw new KeyStoreException("Uninitialized keystore");
    }

    public final Certificate getCertificate(String alias) throws KeyStoreException {
        if (this.initialized) {
            return this.keyStoreSpi.engineGetCertificate(alias);
        }
        throw new KeyStoreException("Uninitialized keystore");
    }

    public final Date getCreationDate(String alias) throws KeyStoreException {
        if (this.initialized) {
            return this.keyStoreSpi.engineGetCreationDate(alias);
        }
        throw new KeyStoreException("Uninitialized keystore");
    }

    public final void setKeyEntry(String alias, Key key, char[] password, Certificate[] chain) throws KeyStoreException {
        if (!this.initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        } else if ((key instanceof PrivateKey) && (chain == null || chain.length == 0)) {
            throw new IllegalArgumentException("Private key must be accompanied by certificate chain");
        } else {
            this.keyStoreSpi.engineSetKeyEntry(alias, key, password, chain);
        }
    }

    public final void setKeyEntry(String alias, byte[] key, Certificate[] chain) throws KeyStoreException {
        if (this.initialized) {
            this.keyStoreSpi.engineSetKeyEntry(alias, key, chain);
            return;
        }
        throw new KeyStoreException("Uninitialized keystore");
    }

    public final void setCertificateEntry(String alias, Certificate cert) throws KeyStoreException {
        if (this.initialized) {
            this.keyStoreSpi.engineSetCertificateEntry(alias, cert);
            return;
        }
        throw new KeyStoreException("Uninitialized keystore");
    }

    public final void deleteEntry(String alias) throws KeyStoreException {
        if (this.initialized) {
            this.keyStoreSpi.engineDeleteEntry(alias);
            return;
        }
        throw new KeyStoreException("Uninitialized keystore");
    }

    public final Enumeration<String> aliases() throws KeyStoreException {
        if (this.initialized) {
            return this.keyStoreSpi.engineAliases();
        }
        throw new KeyStoreException("Uninitialized keystore");
    }

    public final boolean containsAlias(String alias) throws KeyStoreException {
        if (this.initialized) {
            return this.keyStoreSpi.engineContainsAlias(alias);
        }
        throw new KeyStoreException("Uninitialized keystore");
    }

    public final int size() throws KeyStoreException {
        if (this.initialized) {
            return this.keyStoreSpi.engineSize();
        }
        throw new KeyStoreException("Uninitialized keystore");
    }

    public final boolean isKeyEntry(String alias) throws KeyStoreException {
        if (this.initialized) {
            return this.keyStoreSpi.engineIsKeyEntry(alias);
        }
        throw new KeyStoreException("Uninitialized keystore");
    }

    public final boolean isCertificateEntry(String alias) throws KeyStoreException {
        if (this.initialized) {
            return this.keyStoreSpi.engineIsCertificateEntry(alias);
        }
        throw new KeyStoreException("Uninitialized keystore");
    }

    public final String getCertificateAlias(Certificate cert) throws KeyStoreException {
        if (this.initialized) {
            return this.keyStoreSpi.engineGetCertificateAlias(cert);
        }
        throw new KeyStoreException("Uninitialized keystore");
    }

    public final void store(OutputStream stream, char[] password) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        if (this.initialized) {
            this.keyStoreSpi.engineStore(stream, password);
            return;
        }
        throw new KeyStoreException("Uninitialized keystore");
    }

    public final void store(LoadStoreParameter param) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        if (this.initialized) {
            this.keyStoreSpi.engineStore(param);
            return;
        }
        throw new KeyStoreException("Uninitialized keystore");
    }

    public final void load(InputStream stream, char[] password) throws IOException, NoSuchAlgorithmException, CertificateException {
        this.keyStoreSpi.engineLoad(stream, password);
        this.initialized = true;
    }

    public final void load(LoadStoreParameter param) throws IOException, NoSuchAlgorithmException, CertificateException {
        this.keyStoreSpi.engineLoad(param);
        this.initialized = true;
    }

    public final Entry getEntry(String alias, ProtectionParameter protParam) throws NoSuchAlgorithmException, UnrecoverableEntryException, KeyStoreException {
        if (alias == null) {
            throw new NullPointerException("invalid null input");
        } else if (this.initialized) {
            return this.keyStoreSpi.engineGetEntry(alias, protParam);
        } else {
            throw new KeyStoreException("Uninitialized keystore");
        }
    }

    public final void setEntry(String alias, Entry entry, ProtectionParameter protParam) throws KeyStoreException {
        if (alias == null || entry == null) {
            throw new NullPointerException("invalid null input");
        } else if (this.initialized) {
            this.keyStoreSpi.engineSetEntry(alias, entry, protParam);
        } else {
            throw new KeyStoreException("Uninitialized keystore");
        }
    }

    public final boolean entryInstanceOf(String alias, Class<? extends Entry> entryClass) throws KeyStoreException {
        if (alias == null || entryClass == null) {
            throw new NullPointerException("invalid null input");
        } else if (this.initialized) {
            return this.keyStoreSpi.engineEntryInstanceOf(alias, entryClass);
        } else {
            throw new KeyStoreException("Uninitialized keystore");
        }
    }
}
