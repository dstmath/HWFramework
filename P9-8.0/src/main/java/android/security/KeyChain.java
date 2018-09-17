package android.security;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
import android.os.UserHandle;
import android.provider.SettingsStringUtil;
import android.security.IKeyChainAliasCallback.Stub;
import android.security.keystore.AndroidKeyStoreProvider;
import android.security.keystore.KeyProperties;
import android.util.LogException;
import com.android.org.conscrypt.TrustedCertificateStore;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.security.Principal;
import java.security.PrivateKey;
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

    private static class AliasResponse extends Stub {
        private final KeyChainAliasCallback keyChainAliasResponse;

        /* synthetic */ AliasResponse(KeyChainAliasCallback keyChainAliasResponse, AliasResponse -this1) {
            this(keyChainAliasResponse);
        }

        private AliasResponse(KeyChainAliasCallback keyChainAliasResponse) {
            this.keyChainAliasResponse = keyChainAliasResponse;
        }

        public void alias(String alias) {
            this.keyChainAliasResponse.alias(alias);
        }
    }

    public static class KeyChainConnection implements Closeable {
        private final Context context;
        private final IKeyChainService service;
        private final ServiceConnection serviceConnection;

        protected KeyChainConnection(Context context, ServiceConnection serviceConnection, IKeyChainService service) {
            this.context = context;
            this.serviceConnection = serviceConnection;
            this.service = service;
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
        Uri uri = null;
        if (host != null) {
            uri = new Builder().authority(host + (port != -1 ? SettingsStringUtil.DELIMITER + port : LogException.NO_VALUE)).build();
        }
        choosePrivateKeyAlias(activity, response, keyTypes, issuers, uri, alias);
    }

    public static void choosePrivateKeyAlias(Activity activity, KeyChainAliasCallback response, String[] keyTypes, Principal[] issuers, Uri uri, String alias) {
        if (activity == null) {
            throw new NullPointerException("activity == null");
        } else if (response == null) {
            throw new NullPointerException("response == null");
        } else {
            Intent intent = new Intent(ACTION_CHOOSER);
            intent.setPackage("com.android.keychain");
            intent.putExtra("response", new AliasResponse(response, null));
            intent.putExtra("uri", uri);
            intent.putExtra(EXTRA_ALIAS, alias);
            intent.putExtra(EXTRA_SENDER, PendingIntent.getActivity(activity, 0, new Intent(), 0));
            activity.startActivity(intent);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:47:0x0068 A:{Splitter: B:44:0x005e, ExcHandler: java.lang.RuntimeException (r1_0 'e' java.lang.Throwable)} */
    /* JADX WARNING: Missing block: B:47:0x0068, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:49:0x006e, code:
            throw new android.security.KeyChainException(r1);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static PrivateKey getPrivateKey(Context context, String alias) throws KeyChainException, InterruptedException {
        KeyChainConnection keyChainConnection;
        Throwable th;
        Throwable th2 = null;
        if (alias == null) {
            throw new NullPointerException("alias == null");
        } else if (context == null) {
            throw new NullPointerException("context == null");
        } else {
            keyChainConnection = null;
            try {
                keyChainConnection = bind(context.getApplicationContext());
                String keyId = keyChainConnection.getService().requestPrivateKey(alias);
                if (keyChainConnection != null) {
                    try {
                        keyChainConnection.close();
                    } catch (Throwable th3) {
                        th = th3;
                    }
                }
                th = null;
                if (th != null) {
                    try {
                        throw th;
                    } catch (Throwable e) {
                        throw new KeyChainException(e);
                    } catch (Throwable e2) {
                        throw new KeyChainException(e2);
                    }
                } else if (keyId == null) {
                    return null;
                } else {
                    try {
                        return AndroidKeyStoreProvider.loadAndroidKeyStorePrivateKeyFromKeystore(KeyStore.getInstance(), keyId, -1);
                    } catch (Throwable e3) {
                    }
                }
            } catch (Throwable th22) {
                Throwable th4 = th22;
                th22 = th;
                th = th4;
            }
        }
        if (keyChainConnection != null) {
            try {
                keyChainConnection.close();
            } catch (Throwable th5) {
                if (th22 == null) {
                    th22 = th5;
                } else if (th22 != th5) {
                    th22.addSuppressed(th5);
                }
            }
        }
        if (th22 != null) {
            throw th22;
        } else {
            throw th;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:57:0x00a8 A:{Splitter: B:48:0x0067, ExcHandler: java.security.cert.CertificateException (r6_0 'e' java.lang.Throwable)} */
    /* JADX WARNING: Missing block: B:57:0x00a8, code:
            r6 = move-exception;
     */
    /* JADX WARNING: Missing block: B:59:0x00ae, code:
            throw new android.security.KeyChainException(r6);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static X509Certificate[] getCertificateChain(Context context, String alias) throws KeyChainException, InterruptedException {
        Throwable th;
        KeyChainConnection keyChainConnection;
        Throwable th2;
        if (alias == null) {
            throw new NullPointerException("alias == null");
        }
        th = null;
        keyChainConnection = null;
        try {
            keyChainConnection = bind(context.getApplicationContext());
            IKeyChainService keyChainService = keyChainConnection.getService();
            byte[] certificateBytes = keyChainService.getCertificate(alias);
            if (certificateBytes == null) {
                if (keyChainConnection != null) {
                    try {
                        keyChainConnection.close();
                    } catch (Throwable th3) {
                        th = th3;
                    }
                }
                if (th == null) {
                    return null;
                }
                try {
                    throw th;
                } catch (Throwable e) {
                    throw new KeyChainException(e);
                } catch (Throwable e2) {
                    throw new KeyChainException(e2);
                }
            }
            byte[] certChainBytes = keyChainService.getCaCertificates(alias);
            if (keyChainConnection != null) {
                try {
                    keyChainConnection.close();
                } catch (Throwable th4) {
                    th = th4;
                }
            }
            if (th != null) {
                throw th;
            } else {
                try {
                    X509Certificate leafCert = toCertificate(certificateBytes);
                    if (certChainBytes == null || certChainBytes.length == 0) {
                        List<X509Certificate> chain = new TrustedCertificateStore().getCertificateChain(leafCert);
                        return (X509Certificate[]) chain.toArray(new X509Certificate[chain.size()]);
                    }
                    Collection<X509Certificate> chain2 = toCertificates(certChainBytes);
                    ArrayList<X509Certificate> fullChain = new ArrayList(chain2.size() + 1);
                    fullChain.add(leafCert);
                    fullChain.addAll(chain2);
                    return (X509Certificate[]) fullChain.toArray(new X509Certificate[fullChain.size()]);
                } catch (Throwable e3) {
                }
            }
        } catch (Throwable th5) {
            Throwable th6 = th5;
            th5 = th2;
            th2 = th6;
        }
        if (keyChainConnection != null) {
            try {
                keyChainConnection.close();
            } catch (Throwable th7) {
                if (th5 == null) {
                    th5 = th7;
                } else if (th5 != th7) {
                    th5.addSuppressed(th7);
                }
            }
        }
        if (th5 != null) {
            throw th5;
        }
        throw th2;
    }

    public static boolean isKeyAlgorithmSupported(String algorithm) {
        String algUpper = algorithm.toUpperCase(Locale.US);
        if (KeyProperties.KEY_ALGORITHM_EC.equals(algUpper)) {
            return true;
        }
        return KeyProperties.KEY_ALGORITHM_RSA.equals(algUpper);
    }

    @Deprecated
    public static boolean isBoundKeyAlgorithm(String algorithm) {
        if (isKeyAlgorithmSupported(algorithm)) {
            return KeyStore.getInstance().isHardwareBacked(algorithm);
        }
        return false;
    }

    public static X509Certificate toCertificate(byte[] bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException("bytes == null");
        }
        try {
            return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(bytes));
        } catch (CertificateException e) {
            throw new AssertionError(e);
        }
    }

    public static Collection<X509Certificate> toCertificates(byte[] bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException("bytes == null");
        }
        try {
            return CertificateFactory.getInstance("X.509").generateCertificates(new ByteArrayInputStream(bytes));
        } catch (CertificateException e) {
            throw new AssertionError(e);
        }
    }

    public static KeyChainConnection bind(Context context) throws InterruptedException {
        return bindAsUser(context, Process.myUserHandle());
    }

    public static KeyChainConnection bindAsUser(Context context, UserHandle user) throws InterruptedException {
        if (context == null) {
            throw new NullPointerException("context == null");
        }
        ensureNotOnMainThread(context);
        final BlockingQueue<IKeyChainService> q = new LinkedBlockingQueue(1);
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
        if (comp != null && (context.bindServiceAsUser(intent, keyChainServiceConnection, 1, user) ^ 1) == 0) {
            return new KeyChainConnection(context, keyChainServiceConnection, (IKeyChainService) q.take());
        }
        throw new AssertionError("could not bind to KeyChainService");
    }

    private static void ensureNotOnMainThread(Context context) {
        Looper looper = Looper.myLooper();
        if (looper != null && looper == context.getMainLooper()) {
            throw new IllegalStateException("calling this from your main thread can lead to deadlock");
        }
    }
}
