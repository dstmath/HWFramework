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
    public static final String EXTRA_KEY_ACCESSIBLE = "android.security.extra.KEY_ACCESSIBLE";
    public static final String EXTRA_KEY_ALIAS = "android.security.extra.KEY_ALIAS";
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
    public static final int KEY_GEN_FAILURE = 6;
    public static final int KEY_GEN_INVALID_ALGORITHM_PARAMETERS = 4;
    public static final int KEY_GEN_MISSING_ALIAS = 1;
    public static final int KEY_GEN_NO_KEYSTORE_PROVIDER = 5;
    public static final int KEY_GEN_NO_SUCH_ALGORITHM = 3;
    public static final int KEY_GEN_SUCCESS = 0;
    public static final int KEY_GEN_SUPERFLUOUS_ATTESTATION_CHALLENGE = 2;

    private static class AliasResponse extends IKeyChainAliasCallback.Stub {
        private final KeyChainAliasCallback keyChainAliasResponse;

        private AliasResponse(KeyChainAliasCallback keyChainAliasResponse2) {
            this.keyChainAliasResponse = keyChainAliasResponse2;
        }

        public void alias(String alias) {
            this.keyChainAliasResponse.alias(alias);
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

        public void close() {
            this.context.unbindService(this.serviceConnection);
        }

        public IKeyChainService getService() {
            return this.service;
        }
    }

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
            intent.putExtra(EXTRA_RESPONSE, new AliasResponse(response));
            intent.putExtra("uri", uri);
            intent.putExtra(EXTRA_ALIAS, alias);
            intent.putExtra(EXTRA_SENDER, PendingIntent.getActivity(activity, 0, new Intent(), 0));
            activity.startActivity(intent);
        } else {
            throw new NullPointerException("response == null");
        }
    }

    public static PrivateKey getPrivateKey(Context context, String alias) throws KeyChainException, InterruptedException {
        KeyPair keyPair = getKeyPair(context, alias);
        if (keyPair != null) {
            return keyPair.getPrivate();
        }
        return null;
    }

    public static KeyPair getKeyPair(Context context, String alias) throws KeyChainException, InterruptedException {
        KeyChainConnection keyChainConnection;
        if (alias == null) {
            throw new NullPointerException("alias == null");
        } else if (context != null) {
            try {
                keyChainConnection = bind(context.getApplicationContext());
                String keyId = keyChainConnection.getService().requestPrivateKey(alias);
                if (keyChainConnection != null) {
                    $closeResource(null, keyChainConnection);
                }
                String keyId2 = keyId;
                if (keyId2 == null) {
                    return null;
                }
                try {
                    return AndroidKeyStoreProvider.loadAndroidKeyStoreKeyPairFromKeystore(KeyStore.getInstance(), keyId2, -1);
                } catch (RuntimeException | UnrecoverableKeyException e) {
                    throw new KeyChainException((Throwable) e);
                }
            } catch (RemoteException e2) {
                throw new KeyChainException((Throwable) e2);
            } catch (RuntimeException e3) {
                throw new KeyChainException((Throwable) e3);
            } catch (Throwable th) {
                if (keyChainConnection != null) {
                    $closeResource(r1, keyChainConnection);
                }
                throw th;
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

    public static X509Certificate[] getCertificateChain(Context context, String alias) throws KeyChainException, InterruptedException {
        KeyChainConnection keyChainConnection;
        if (alias != null) {
            try {
                keyChainConnection = bind(context.getApplicationContext());
                IKeyChainService keyChainService = keyChainConnection.getService();
                byte[] certificateBytes = keyChainService.getCertificate(alias);
                if (certificateBytes == null) {
                    if (keyChainConnection != null) {
                        $closeResource(null, keyChainConnection);
                    }
                    return null;
                }
                byte[] certChainBytes = keyChainService.getCaCertificates(alias);
                if (keyChainConnection != null) {
                    $closeResource(null, keyChainConnection);
                }
                byte[] certChainBytes2 = certChainBytes;
                try {
                    X509Certificate leafCert = toCertificate(certificateBytes);
                    if (certChainBytes2 == null || certChainBytes2.length == 0) {
                        List<X509Certificate> chain = new TrustedCertificateStore().getCertificateChain(leafCert);
                        return (X509Certificate[]) chain.toArray(new X509Certificate[chain.size()]);
                    }
                    Collection<X509Certificate> chain2 = toCertificates(certChainBytes2);
                    ArrayList<X509Certificate> fullChain = new ArrayList<>(chain2.size() + 1);
                    fullChain.add(leafCert);
                    fullChain.addAll(chain2);
                    return (X509Certificate[]) fullChain.toArray(new X509Certificate[fullChain.size()]);
                } catch (RuntimeException | CertificateException e) {
                    throw new KeyChainException((Throwable) e);
                }
            } catch (RemoteException e2) {
                throw new KeyChainException((Throwable) e2);
            } catch (RuntimeException e3) {
                throw new KeyChainException((Throwable) e3);
            } catch (Throwable e4) {
                if (keyChainConnection != null) {
                    $closeResource(r1, keyChainConnection);
                }
                throw e4;
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

    public static KeyChainConnection bind(Context context) throws InterruptedException {
        return bindAsUser(context, Process.myUserHandle());
    }

    public static KeyChainConnection bindAsUser(Context context, UserHandle user) throws InterruptedException {
        if (context != null) {
            ensureNotOnMainThread(context);
            final BlockingQueue<IKeyChainService> q = new LinkedBlockingQueue<>(1);
            ServiceConnection keyChainServiceConnection = new ServiceConnection() {
                volatile boolean mConnectedAtLeastOnce = false;

                public void onServiceConnected(ComponentName name, IBinder service) {
                    if (!this.mConnectedAtLeastOnce) {
                        this.mConnectedAtLeastOnce = true;
                        try {
                            q.put(IKeyChainService.Stub.asInterface(Binder.allowBlocking(service)));
                        } catch (InterruptedException e) {
                        }
                    }
                }

                public void onServiceDisconnected(ComponentName name) {
                }
            };
            Intent intent = new Intent(IKeyChainService.class.getName());
            ComponentName comp = intent.resolveSystemService(context.getPackageManager(), 0);
            intent.setComponent(comp);
            if (comp != null && context.bindServiceAsUser(intent, keyChainServiceConnection, 1, user)) {
                return new KeyChainConnection(context, keyChainServiceConnection, q.take());
            }
            throw new AssertionError("could not bind to KeyChainService");
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
