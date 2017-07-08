package android.security;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ProxyInfo;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcelable;
import android.os.Process;
import android.os.UserHandle;
import android.security.IKeyChainService.Stub;
import android.security.keystore.AndroidKeyStoreProvider;
import android.security.keystore.KeyProperties;
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
    public static final String ACTION_STORAGE_CHANGED = "android.security.STORAGE_CHANGED";
    private static final String CERT_INSTALLER_PACKAGE = "com.android.certinstaller";
    public static final String EXTRA_ALIAS = "alias";
    public static final String EXTRA_CERTIFICATE = "CERT";
    public static final String EXTRA_NAME = "name";
    public static final String EXTRA_PKCS12 = "PKCS12";
    public static final String EXTRA_RESPONSE = "response";
    public static final String EXTRA_SENDER = "sender";
    public static final String EXTRA_URI = "uri";
    private static final String KEYCHAIN_PACKAGE = "com.android.keychain";

    /* renamed from: android.security.KeyChain.1 */
    static class AnonymousClass1 implements ServiceConnection {
        volatile boolean mConnectedAtLeastOnce;
        final /* synthetic */ BlockingQueue val$q;

        AnonymousClass1(BlockingQueue val$q) {
            this.val$q = val$q;
            this.mConnectedAtLeastOnce = false;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            if (!this.mConnectedAtLeastOnce) {
                this.mConnectedAtLeastOnce = true;
                try {
                    this.val$q.put(Stub.asInterface(service));
                } catch (InterruptedException e) {
                }
            }
        }

        public void onServiceDisconnected(ComponentName name) {
        }
    }

    private static class AliasResponse extends IKeyChainAliasCallback.Stub {
        private final KeyChainAliasCallback keyChainAliasResponse;

        private AliasResponse(KeyChainAliasCallback keyChainAliasResponse) {
            this.keyChainAliasResponse = keyChainAliasResponse;
        }

        public void alias(String alias) {
            this.keyChainAliasResponse.alias(alias);
        }
    }

    public static final class KeyChainConnection implements Closeable {
        private final Context context;
        private final IKeyChainService service;
        private final ServiceConnection serviceConnection;

        private KeyChainConnection(Context context, ServiceConnection serviceConnection, IKeyChainService service) {
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
        Intent intent = new Intent(ACTION_INSTALL);
        intent.setClassName(CERT_INSTALLER_PACKAGE, "com.android.certinstaller.CertInstallerMain");
        return intent;
    }

    public static void choosePrivateKeyAlias(Activity activity, KeyChainAliasCallback response, String[] keyTypes, Principal[] issuers, String host, int port, String alias) {
        Uri uri = null;
        if (host != null) {
            uri = new Builder().authority(host + (port != -1 ? ":" + port : ProxyInfo.LOCAL_EXCL_LIST)).build();
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
            intent.setPackage(KEYCHAIN_PACKAGE);
            intent.putExtra(EXTRA_RESPONSE, new AliasResponse(null));
            intent.putExtra(EXTRA_URI, (Parcelable) uri);
            intent.putExtra(EXTRA_ALIAS, alias);
            intent.putExtra(EXTRA_SENDER, PendingIntent.getActivity(activity, 0, new Intent(), 0));
            activity.startActivity(intent);
        }
    }

    public static PrivateKey getPrivateKey(Context context, String alias) throws KeyChainException, InterruptedException {
        if (alias == null) {
            throw new NullPointerException("alias == null");
        }
        KeyChainConnection keyChainConnection = bind(context);
        try {
            String keyId = keyChainConnection.getService().requestPrivateKey(alias);
            if (keyId == null) {
                keyChainConnection.close();
                return null;
            }
            PrivateKey loadAndroidKeyStorePrivateKeyFromKeystore = AndroidKeyStoreProvider.loadAndroidKeyStorePrivateKeyFromKeystore(KeyStore.getInstance(), keyId, -1);
            keyChainConnection.close();
            return loadAndroidKeyStorePrivateKeyFromKeystore;
        } catch (Throwable e) {
            throw new KeyChainException(e);
        } catch (Throwable e2) {
            throw new KeyChainException(e2);
        } catch (Throwable e3) {
            throw new KeyChainException(e3);
        } catch (Throwable th) {
            keyChainConnection.close();
        }
    }

    public static X509Certificate[] getCertificateChain(Context context, String alias) throws KeyChainException, InterruptedException {
        if (alias == null) {
            throw new NullPointerException("alias == null");
        }
        KeyChainConnection keyChainConnection = bind(context);
        try {
            IKeyChainService keyChainService = keyChainConnection.getService();
            byte[] certificateBytes = keyChainService.getCertificate(alias);
            if (certificateBytes == null) {
                keyChainConnection.close();
                return null;
            }
            X509Certificate leafCert = toCertificate(certificateBytes);
            byte[] certChainBytes = keyChainService.getCaCertificates(alias);
            X509Certificate[] x509CertificateArr;
            if (certChainBytes == null || certChainBytes.length == 0) {
                List<X509Certificate> chain = new TrustedCertificateStore().getCertificateChain(leafCert);
                x509CertificateArr = (X509Certificate[]) chain.toArray(new X509Certificate[chain.size()]);
                keyChainConnection.close();
                return x509CertificateArr;
            }
            Collection<X509Certificate> chain2 = toCertificates(certChainBytes);
            ArrayList<X509Certificate> fullChain = new ArrayList(chain2.size() + 1);
            fullChain.add(leafCert);
            fullChain.addAll(chain2);
            x509CertificateArr = (X509Certificate[]) fullChain.toArray(new X509Certificate[fullChain.size()]);
            keyChainConnection.close();
            return x509CertificateArr;
        } catch (Throwable e) {
            throw new KeyChainException(e);
        } catch (Throwable e2) {
            throw new KeyChainException(e2);
        } catch (Throwable e3) {
            throw new KeyChainException(e3);
        } catch (Throwable th) {
            keyChainConnection.close();
        }
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
        BlockingQueue<IKeyChainService> q = new LinkedBlockingQueue(1);
        ServiceConnection keyChainServiceConnection = new AnonymousClass1(q);
        Intent intent = new Intent(IKeyChainService.class.getName());
        ComponentName comp = intent.resolveSystemService(context.getPackageManager(), 0);
        intent.setComponent(comp);
        if (comp != null && context.bindServiceAsUser(intent, keyChainServiceConnection, 1, user)) {
            return new KeyChainConnection(keyChainServiceConnection, (IKeyChainService) q.take(), null);
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
