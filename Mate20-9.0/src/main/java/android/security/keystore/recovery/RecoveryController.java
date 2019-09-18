package android.security.keystore.recovery;

import android.annotation.SystemApi;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.ServiceSpecificException;
import android.security.KeyStore;
import android.security.keystore.AndroidKeyStoreProvider;
import com.android.internal.widget.ILockSettings;
import java.security.Key;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SystemApi
public class RecoveryController {
    public static final int ERROR_BAD_CERTIFICATE_FORMAT = 25;
    public static final int ERROR_DECRYPTION_FAILED = 26;
    public static final int ERROR_DOWNGRADE_CERTIFICATE = 29;
    public static final int ERROR_INSECURE_USER = 23;
    public static final int ERROR_INVALID_CERTIFICATE = 28;
    public static final int ERROR_INVALID_KEY_FORMAT = 27;
    public static final int ERROR_NO_SNAPSHOT_PENDING = 21;
    public static final int ERROR_SERVICE_INTERNAL_ERROR = 22;
    public static final int ERROR_SESSION_EXPIRED = 24;
    public static final int RECOVERY_STATUS_PERMANENT_FAILURE = 3;
    public static final int RECOVERY_STATUS_SYNCED = 0;
    public static final int RECOVERY_STATUS_SYNC_IN_PROGRESS = 1;
    private static final String TAG = "RecoveryController";
    private final ILockSettings mBinder;
    private final KeyStore mKeyStore;

    private RecoveryController(ILockSettings binder, KeyStore keystore) {
        this.mBinder = binder;
        this.mKeyStore = keystore;
    }

    /* access modifiers changed from: package-private */
    public ILockSettings getBinder() {
        return this.mBinder;
    }

    public static RecoveryController getInstance(Context context) {
        return new RecoveryController(ILockSettings.Stub.asInterface(ServiceManager.getService("lock_settings")), KeyStore.getInstance());
    }

    public static boolean isRecoverableKeyStoreEnabled(Context context) {
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(KeyguardManager.class);
        return keyguardManager != null && keyguardManager.isDeviceSecure();
    }

    @Deprecated
    public void initRecoveryService(String rootCertificateAlias, byte[] signedPublicKeyList) throws CertificateException, InternalRecoveryServiceException {
        throw new UnsupportedOperationException();
    }

    public void initRecoveryService(String rootCertificateAlias, byte[] certificateFile, byte[] signatureFile) throws CertificateException, InternalRecoveryServiceException {
        try {
            this.mBinder.initRecoveryServiceWithSigFile(rootCertificateAlias, certificateFile, signatureFile);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        } catch (ServiceSpecificException e2) {
            if (e2.errorCode == 25 || e2.errorCode == 28) {
                throw new CertificateException("Invalid certificate for recovery service", e2);
            } else if (e2.errorCode == 29) {
                throw new CertificateException("Downgrading certificate serial version isn't supported.", e2);
            } else {
                throw wrapUnexpectedServiceSpecificException(e2);
            }
        }
    }

    @Deprecated
    public KeyChainSnapshot getRecoveryData() throws InternalRecoveryServiceException {
        throw new UnsupportedOperationException();
    }

    public KeyChainSnapshot getKeyChainSnapshot() throws InternalRecoveryServiceException {
        try {
            return this.mBinder.getKeyChainSnapshot();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        } catch (ServiceSpecificException e2) {
            if (e2.errorCode == 21) {
                return null;
            }
            throw wrapUnexpectedServiceSpecificException(e2);
        }
    }

    public void setSnapshotCreatedPendingIntent(PendingIntent intent) throws InternalRecoveryServiceException {
        try {
            this.mBinder.setSnapshotCreatedPendingIntent(intent);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        } catch (ServiceSpecificException e2) {
            throw wrapUnexpectedServiceSpecificException(e2);
        }
    }

    public void setServerParams(byte[] serverParams) throws InternalRecoveryServiceException {
        try {
            this.mBinder.setServerParams(serverParams);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        } catch (ServiceSpecificException e2) {
            throw wrapUnexpectedServiceSpecificException(e2);
        }
    }

    @Deprecated
    public List<String> getAliases(String packageName) throws InternalRecoveryServiceException {
        throw new UnsupportedOperationException();
    }

    public List<String> getAliases() throws InternalRecoveryServiceException {
        try {
            return new ArrayList(this.mBinder.getRecoveryStatus().keySet());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        } catch (ServiceSpecificException e2) {
            throw wrapUnexpectedServiceSpecificException(e2);
        }
    }

    @Deprecated
    public void setRecoveryStatus(String packageName, String alias, int status) throws PackageManager.NameNotFoundException, InternalRecoveryServiceException {
        throw new UnsupportedOperationException();
    }

    public void setRecoveryStatus(String alias, int status) throws InternalRecoveryServiceException {
        try {
            this.mBinder.setRecoveryStatus(alias, status);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        } catch (ServiceSpecificException e2) {
            throw wrapUnexpectedServiceSpecificException(e2);
        }
    }

    @Deprecated
    public int getRecoveryStatus(String packageName, String alias) throws InternalRecoveryServiceException {
        throw new UnsupportedOperationException();
    }

    public int getRecoveryStatus(String alias) throws InternalRecoveryServiceException {
        try {
            Integer status = this.mBinder.getRecoveryStatus().get(alias);
            if (status == null) {
                return 3;
            }
            return status.intValue();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        } catch (ServiceSpecificException e2) {
            throw wrapUnexpectedServiceSpecificException(e2);
        }
    }

    public void setRecoverySecretTypes(int[] secretTypes) throws InternalRecoveryServiceException {
        try {
            this.mBinder.setRecoverySecretTypes(secretTypes);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        } catch (ServiceSpecificException e2) {
            throw wrapUnexpectedServiceSpecificException(e2);
        }
    }

    public int[] getRecoverySecretTypes() throws InternalRecoveryServiceException {
        try {
            return this.mBinder.getRecoverySecretTypes();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        } catch (ServiceSpecificException e2) {
            throw wrapUnexpectedServiceSpecificException(e2);
        }
    }

    @Deprecated
    public byte[] generateAndStoreKey(String alias, byte[] account) throws InternalRecoveryServiceException, LockScreenRequiredException {
        throw new UnsupportedOperationException("Operation is not supported, use generateKey");
    }

    @Deprecated
    public Key generateKey(String alias, byte[] account) throws InternalRecoveryServiceException, LockScreenRequiredException {
        throw new UnsupportedOperationException();
    }

    public Key generateKey(String alias) throws InternalRecoveryServiceException, LockScreenRequiredException {
        try {
            String grantAlias = this.mBinder.generateKey(alias);
            if (grantAlias != null) {
                return getKeyFromGrant(grantAlias);
            }
            throw new InternalRecoveryServiceException("null grant alias");
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        } catch (UnrecoverableKeyException e2) {
            throw new InternalRecoveryServiceException("Failed to get key from keystore", e2);
        } catch (ServiceSpecificException e3) {
            if (e3.errorCode == 23) {
                throw new LockScreenRequiredException(e3.getMessage());
            }
            throw wrapUnexpectedServiceSpecificException(e3);
        }
    }

    public Key importKey(String alias, byte[] keyBytes) throws InternalRecoveryServiceException, LockScreenRequiredException {
        try {
            String grantAlias = this.mBinder.importKey(alias, keyBytes);
            if (grantAlias != null) {
                return getKeyFromGrant(grantAlias);
            }
            throw new InternalRecoveryServiceException("Null grant alias");
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        } catch (UnrecoverableKeyException e2) {
            throw new InternalRecoveryServiceException("Failed to get key from keystore", e2);
        } catch (ServiceSpecificException e3) {
            if (e3.errorCode == 23) {
                throw new LockScreenRequiredException(e3.getMessage());
            }
            throw wrapUnexpectedServiceSpecificException(e3);
        }
    }

    public Key getKey(String alias) throws InternalRecoveryServiceException, UnrecoverableKeyException {
        try {
            String grantAlias = this.mBinder.getKey(alias);
            if (grantAlias != null) {
                if (!"".equals(grantAlias)) {
                    return getKeyFromGrant(grantAlias);
                }
            }
            return null;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        } catch (ServiceSpecificException e2) {
            throw wrapUnexpectedServiceSpecificException(e2);
        }
    }

    /* access modifiers changed from: package-private */
    public Key getKeyFromGrant(String grantAlias) throws UnrecoverableKeyException {
        return AndroidKeyStoreProvider.loadAndroidKeyStoreKeyFromKeystore(this.mKeyStore, grantAlias, -1);
    }

    public void removeKey(String alias) throws InternalRecoveryServiceException {
        try {
            this.mBinder.removeKey(alias);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        } catch (ServiceSpecificException e2) {
            throw wrapUnexpectedServiceSpecificException(e2);
        }
    }

    public RecoverySession createRecoverySession() {
        return RecoverySession.newInstance(this);
    }

    public Map<String, X509Certificate> getRootCertificates() {
        return TrustedRootCertificates.getRootCertificates();
    }

    /* access modifiers changed from: package-private */
    public InternalRecoveryServiceException wrapUnexpectedServiceSpecificException(ServiceSpecificException e) {
        if (e.errorCode == 22) {
            return new InternalRecoveryServiceException(e.getMessage());
        }
        return new InternalRecoveryServiceException("Unexpected error code for method: " + e.errorCode, e);
    }
}
