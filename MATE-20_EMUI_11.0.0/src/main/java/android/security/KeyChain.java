package android.security;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.SettingsStringUtil;
import android.security.IKeyChainAliasCallback;
import android.security.IKeyChainService;
import android.security.keystore.AndroidKeyStoreProvider;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import com.android.org.conscrypt.TrustedCertificateStore;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.security.KeyPair;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.security.auth.x500.X500Principal;

public final class KeyChain {
    public static final String ACCOUNT_TYPE = "com.android.keychain";
    private static final String ACTION_CHOOSER = "com.android.keychain.CHOOSER";
    private static final String ACTION_INSTALL = "android.credentials.INSTALL";
    public static final String ACTION_KEYCHAIN_CHANGED = "android.security.action.KEYCHAIN_CHANGED";
    public static final String ACTION_KEY_ACCESS_CHANGED = "android.security.action.KEY_ACCESS_CHANGED";
    public static final String ACTION_STORAGE_CHANGED = "android.security.STORAGE_CHANGED";
    public static final String ACTION_TRUST_STORE_CHANGED = "android.security.action.TRUST_STORE_CHANGED";
    private static final String CERT_INSTALLER_PACKAGE = "com.android.certinstaller";
    public static final String EXTRA_ALIAS = "alias";
    public static final String EXTRA_CERTIFICATE = "CERT";
    public static final String EXTRA_ISSUERS = "issuers";
    public static final String EXTRA_KEY_ACCESSIBLE = "android.security.extra.KEY_ACCESSIBLE";
    public static final String EXTRA_KEY_ALIAS = "android.security.extra.KEY_ALIAS";
    public static final String EXTRA_KEY_TYPES = "key_types";
    public static final String EXTRA_NAME = "name";
    public static final String EXTRA_PKCS12 = "PKCS12";
    public static final String EXTRA_RESPONSE = "response";
    public static final String EXTRA_SENDER = "sender";
    public static final String EXTRA_URI = "uri";
    private static final String KEYCHAIN_PACKAGE = "com.android.keychain";
    public static final int KEY_ATTESTATION_CANNOT_ATTEST_IDS = 3;
    public static final int KEY_ATTESTATION_CANNOT_COLLECT_DATA = 2;
    public static final int KEY_ATTESTATION_FAILURE = 4;
    public static final int KEY_ATTESTATION_MISSING_CHALLENGE = 1;
    public static final int KEY_ATTESTATION_SUCCESS = 0;
    public static final int KEY_GEN_FAILURE = 7;
    public static final int KEY_GEN_INVALID_ALGORITHM_PARAMETERS = 4;
    public static final int KEY_GEN_MISSING_ALIAS = 1;
    public static final int KEY_GEN_NO_KEYSTORE_PROVIDER = 5;
    public static final int KEY_GEN_NO_SUCH_ALGORITHM = 3;
    public static final int KEY_GEN_STRONGBOX_UNAVAILABLE = 6;
    public static final int KEY_GEN_SUCCESS = 0;
    public static final int KEY_GEN_SUPERFLUOUS_ATTESTATION_CHALLENGE = 2;

    public static Intent createInstallIntent() {
        Intent intent = new Intent("android.credentials.INSTALL");
        intent.setClassName(CERT_INSTALLER_PACKAGE, "com.android.certinstaller.CertInstallerMain");
        return intent;
    }

    public static void choosePrivateKeyAlias(Activity activity, KeyChainAliasCallback response, String[] keyTypes, Principal[] issuers, String host, int port, String alias) {
        String str;
        Uri uri = null;
        if (host != null) {
            Uri.Builder builder = new Uri.Builder();
            StringBuilder sb = new StringBuilder();
            sb.append(host);
            if (port != -1) {
                str = SettingsStringUtil.DELIMITER + port;
            } else {
                str = "";
            }
            sb.append(str);
            uri = builder.authority(sb.toString()).build();
        }
        choosePrivateKeyAlias(activity, response, keyTypes, issuers, uri, alias);
    }

    public static void choosePrivateKeyAlias(Activity activity, KeyChainAliasCallback response, String[] keyTypes, Principal[] issuers, Uri uri, String alias) {
        if (activity == null) {
            throw new NullPointerException("activity == null");
        } else if (response != null) {
            Intent intent = new Intent(ACTION_CHOOSER);
            intent.setPackage("com.android.keychain");
            intent.putExtra("response", new AliasResponse(response));
            intent.putExtra("uri", uri);
            intent.putExtra(EXTRA_ALIAS, alias);
            intent.putExtra(EXTRA_KEY_TYPES, keyTypes);
            ArrayList<byte[]> issuersList = new ArrayList<>();
            if (issuers != null) {
                for (Principal issuer : issuers) {
                    if (issuer instanceof X500Principal) {
                        issuersList.add(((X500Principal) issuer).getEncoded());
                    } else {
                        throw new IllegalArgumentException(String.format("Issuer %s is of type %s, not X500Principal", issuer.toString(), issuer.getClass()));
                    }
                }
            }
            intent.putExtra(EXTRA_ISSUERS, issuersList);
            intent.putExtra(EXTRA_SENDER, PendingIntent.getActivity(activity, 0, new Intent(), 0));
            activity.startActivity(intent);
        } else {
            throw new NullPointerException("response == null");
        }
    }

    /* access modifiers changed from: private */
    public static class AliasResponse extends IKeyChainAliasCallback.Stub {
        private final KeyChainAliasCallback keyChainAliasResponse;

        private AliasResponse(KeyChainAliasCallback keyChainAliasResponse2) {
            this.keyChainAliasResponse = keyChainAliasResponse2;
        }

        @Override // android.security.IKeyChainAliasCallback
        public void alias(String alias) {
            this.keyChainAliasResponse.alias(alias);
        }
    }

    public static PrivateKey getPrivateKey(Context context, String alias) throws KeyChainException, InterruptedException {
        KeyPair keyPair = getKeyPair(context, alias);
        if (keyPair != null) {
            return keyPair.getPrivate();
        }
        return null;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x002f, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0030, code lost:
        if (r0 != null) goto L_0x0032;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0032, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0035, code lost:
        throw r2;
     */
    public static KeyPair getKeyPair(Context context, String alias) throws KeyChainException, InterruptedException {
        if (alias == null) {
            throw new NullPointerException("alias == null");
        } else if (context != null) {
            try {
                KeyChainConnection keyChainConnection = bind(context.getApplicationContext());
                String keyId = keyChainConnection.getService().requestPrivateKey(alias);
                $closeResource(null, keyChainConnection);
                if (keyId == null) {
                    return null;
                }
                try {
                    return AndroidKeyStoreProvider.loadAndroidKeyStoreKeyPairFromKeystore(KeyStore.getInstance(), keyId, -1);
                } catch (KeyPermanentlyInvalidatedException | RuntimeException | UnrecoverableKeyException e) {
                    throw new KeyChainException(e);
                }
            } catch (RemoteException e2) {
                throw new KeyChainException(e2);
            } catch (RuntimeException e3) {
                throw new KeyChainException(e3);
            }
        } else {
            throw new NullPointerException("context == null");
        }
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:25:0x006d, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x006e, code lost:
        if (r0 != null) goto L_0x0070;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0070, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0073, code lost:
        throw r2;
     */
    public static X509Certificate[] getCertificateChain(Context context, String alias) throws KeyChainException, InterruptedException {
        if (alias != null) {
            try {
                KeyChainConnection keyChainConnection = bind(context.getApplicationContext());
                IKeyChainService keyChainService = keyChainConnection.getService();
                byte[] certificateBytes = keyChainService.getCertificate(alias);
                if (certificateBytes == null) {
                    $closeResource(null, keyChainConnection);
                    return null;
                }
                byte[] certChainBytes = keyChainService.getCaCertificates(alias);
                $closeResource(null, keyChainConnection);
                try {
                    X509Certificate leafCert = toCertificate(certificateBytes);
                    if (certChainBytes == null || certChainBytes.length == 0) {
                        List<X509Certificate> chain = new TrustedCertificateStore().getCertificateChain(leafCert);
                        return (X509Certificate[]) chain.toArray(new X509Certificate[chain.size()]);
                    }
                    Collection<? extends X509Certificate> chain2 = toCertificates(certChainBytes);
                    ArrayList<X509Certificate> fullChain = new ArrayList<>(chain2.size() + 1);
                    fullChain.add(leafCert);
                    fullChain.addAll(chain2);
                    return (X509Certificate[]) fullChain.toArray(new X509Certificate[fullChain.size()]);
                } catch (RuntimeException | CertificateException e) {
                    throw new KeyChainException(e);
                }
            } catch (RemoteException e2) {
                throw new KeyChainException(e2);
            } catch (RuntimeException e3) {
                throw new KeyChainException(e3);
            }
        } else {
            throw new NullPointerException("alias == null");
        }
    }

    public static boolean isKeyAlgorithmSupported(String algorithm) {
        String algUpper = algorithm.toUpperCase(Locale.US);
        return KeyProperties.KEY_ALGORITHM_EC.equals(algUpper) || KeyProperties.KEY_ALGORITHM_RSA.equals(algUpper);
    }

    @Deprecated
    public static boolean isBoundKeyAlgorithm(String algorithm) {
        if (!isKeyAlgorithmSupported(algorithm)) {
            return false;
        }
        return KeyStore.getInstance().isHardwareBacked(algorithm);
    }

    public static X509Certificate toCertificate(byte[] bytes) {
        if (bytes != null) {
            try {
                return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(bytes));
            } catch (CertificateException e) {
                throw new AssertionError(e);
            }
        } else {
            throw new IllegalArgumentException("bytes == null");
        }
    }

    /* JADX DEBUG: Type inference failed for r1v3. Raw type applied. Possible types: java.util.Collection<? extends java.security.cert.Certificate>, java.util.Collection<java.security.cert.X509Certificate> */
    public static Collection<X509Certificate> toCertificates(byte[] bytes) {
        if (bytes != null) {
            try {
                return CertificateFactory.getInstance("X.509").generateCertificates(new ByteArrayInputStream(bytes));
            } catch (CertificateException e) {
                throw new AssertionError(e);
            }
        } else {
            throw new IllegalArgumentException("bytes == null");
        }
    }

    public static class KeyChainConnection implements Closeable {
        private final Context context;
        private final IKeyChainService service;
        private final ServiceConnection serviceConnection;

        protected KeyChainConnection(Context context2, ServiceConnection serviceConnection2, IKeyChainService service2) {
            this.context = context2;
            this.serviceConnection = serviceConnection2;
            this.service = service2;
        }

        @Override // java.io.Closeable, java.lang.AutoCloseable
        public void close() {
            this.context.unbindService(this.serviceConnection);
        }

        public IKeyChainService getService() {
            return this.service;
        }
    }

    public static KeyChainConnection bind(Context context) throws InterruptedException {
        return bindAsUser(context, Process.myUserHandle());
    }

    public static KeyChainConnection bindAsUser(Context context, UserHandle user) throws InterruptedException {
        if (context != null) {
            ensureNotOnMainThread(context);
            final BlockingQueue<Object> q = new LinkedBlockingQueue<>(1);
            ServiceConnection keyChainServiceConnection = new ServiceConnection() {
                /* class android.security.KeyChain.AnonymousClass1 */
                volatile boolean mConnectedAtLeastOnce = false;

                @Override // android.content.ServiceConnection
                public void onServiceConnected(ComponentName name, IBinder service) {
                    putResponse(IKeyChainService.Stub.asInterface(Binder.allowBlocking(service)));
                }

                @Override // android.content.ServiceConnection
                public void onNullBinding(ComponentName name) {
                    putResponse(new Object());
                }

                private void putResponse(Object service) {
                    if (!this.mConnectedAtLeastOnce) {
                        this.mConnectedAtLeastOnce = true;
                        try {
                            q.put(service);
                        } catch (InterruptedException e) {
                        }
                    }
                }

                @Override // android.content.ServiceConnection
                public void onServiceDisconnected(ComponentName name) {
                }
            };
            Intent intent = new Intent(IKeyChainService.class.getName());
            ComponentName comp = intent.resolveSystemService(context.getPackageManager(), 0);
            intent.setComponent(comp);
            if (comp == null || !context.bindServiceAsUser(intent, keyChainServiceConnection, 1, user)) {
                throw new AssertionError("could not bind to KeyChainService");
            }
            Object service = q.take();
            if (service instanceof IKeyChainService) {
                return new KeyChainConnection(context, keyChainServiceConnection, (IKeyChainService) service);
            }
            throw new AssertionError("could not connect to KeyChainService");
        }
        throw new NullPointerException("context == null");
    }

    private static void ensureNotOnMainThread(Context context) {
        Looper looper = Looper.myLooper();
        if (looper != null && looper == context.getMainLooper()) {
            throw new IllegalStateException("calling this from your main thread can lead to deadlock");
        }
    }
}
