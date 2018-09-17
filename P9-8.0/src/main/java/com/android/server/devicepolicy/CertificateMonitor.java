package com.android.server.devicepolicy;

import android.app.Notification;
import android.app.Notification.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Handler;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.storage.StorageManager;
import android.security.Credentials;
import android.security.KeyChain.KeyChainConnection;
import android.util.Log;
import com.android.internal.notification.SystemNotificationChannels;
import java.io.ByteArrayInputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;

public class CertificateMonitor {
    protected static final String LOG_TAG = "DevicePolicyManager";
    protected static final int MONITORING_CERT_NOTIFICATION_ID = 33;
    private final Handler mHandler;
    private final Injector mInjector;
    private final BroadcastReceiver mRootCaReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (!StorageManager.inCryptKeeperBounce()) {
                CertificateMonitor.this.updateInstalledCertificates(UserHandle.of(intent.getIntExtra("android.intent.extra.user_handle", getSendingUserId())));
            }
        }
    };
    private final DevicePolicyManagerService mService;

    public CertificateMonitor(DevicePolicyManagerService service, Injector injector, Handler handler) {
        this.mService = service;
        this.mInjector = injector;
        this.mHandler = handler;
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.USER_STARTED");
        filter.addAction("android.intent.action.USER_UNLOCKED");
        filter.addAction("android.security.action.TRUST_STORE_CHANGED");
        filter.setPriority(1000);
        this.mInjector.mContext.registerReceiverAsUser(this.mRootCaReceiver, UserHandle.ALL, filter, null, this.mHandler);
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x0032 A:{Splitter: B:1:0x0001, ExcHandler: java.security.cert.CertificateException (r0_0 'ce' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:16:0x0032, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:17:0x0033, code:
            android.util.Log.e(LOG_TAG, "Problem converting cert", r0);
     */
    /* JADX WARNING: Missing block: B:18:0x003c, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public String installCaCert(UserHandle userHandle, byte[] certBuffer) {
        Throwable th;
        try {
            byte[] pemCert = Credentials.convertToPem(new Certificate[]{parseCert(certBuffer)});
            KeyChainConnection keyChainConnection = null;
            Throwable th2;
            try {
                keyChainConnection = this.mInjector.keyChainBindAsUser(userHandle);
                String installCaCertificate = keyChainConnection.getService().installCaCertificate(pemCert);
                if (keyChainConnection != null) {
                    try {
                        keyChainConnection.close();
                    } catch (Throwable th3) {
                        th2 = th3;
                    }
                }
                th2 = null;
                if (th2 == null) {
                    return installCaCertificate;
                }
                try {
                    throw th2;
                } catch (RemoteException e) {
                    Log.e(LOG_TAG, "installCaCertsToKeyChain(): ", e);
                } catch (InterruptedException e1) {
                    Log.w(LOG_TAG, "installCaCertsToKeyChain(): ", e1);
                    Thread.currentThread().interrupt();
                }
                return null;
                if (keyChainConnection != null) {
                    try {
                        keyChainConnection.close();
                    } catch (Throwable th4) {
                        if (th == null) {
                            th = th4;
                        } else if (th != th4) {
                            th.addSuppressed(th4);
                        }
                    }
                }
                if (th != null) {
                    throw th;
                } else {
                    throw th2;
                }
            } catch (Throwable th5) {
                Throwable th6 = th5;
                th5 = th2;
                th2 = th6;
            }
        } catch (Exception ce) {
        }
    }

    public void uninstallCaCerts(UserHandle userHandle, String[] aliases) {
        Throwable th;
        Throwable th2 = null;
        KeyChainConnection keyChainConnection = null;
        try {
            keyChainConnection = this.mInjector.keyChainBindAsUser(userHandle);
            for (String deleteCaCertificate : aliases) {
                keyChainConnection.getService().deleteCaCertificate(deleteCaCertificate);
            }
            if (keyChainConnection != null) {
                try {
                    keyChainConnection.close();
                } catch (Throwable th3) {
                    th2 = th3;
                }
            }
            if (th2 != null) {
                try {
                    throw th2;
                } catch (RemoteException e) {
                    Log.e(LOG_TAG, "from CaCertUninstaller: ", e);
                    return;
                } catch (InterruptedException ie) {
                    Log.w(LOG_TAG, "CaCertUninstaller: ", ie);
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            return;
        } catch (Throwable th22) {
            Throwable th4 = th22;
            th22 = th;
            th = th4;
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

    public List<String> getInstalledCaCertificates(UserHandle userHandle) throws RemoteException, RuntimeException {
        Throwable th;
        Throwable th2;
        KeyChainConnection keyChainConnection = null;
        try {
            keyChainConnection = this.mInjector.keyChainBindAsUser(userHandle);
            List<String> list = keyChainConnection.getService().getUserCaAliases().getList();
            if (keyChainConnection != null) {
                try {
                    keyChainConnection.close();
                } catch (Throwable th3) {
                    th = th3;
                }
            }
            th = null;
            if (th == null) {
                return list;
            }
            try {
                throw th;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            } catch (AssertionError e2) {
                throw new RuntimeException(e2);
            }
        } catch (Throwable th22) {
            Throwable th4 = th22;
            th22 = th;
            th = th4;
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

    /* synthetic */ void lambda$-com_android_server_devicepolicy_CertificateMonitor_5174(int userId) {
        updateInstalledCertificates(UserHandle.of(userId));
    }

    public void onCertificateApprovalsChanged(int userId) {
        this.mHandler.post(new -$Lambda$MiCJAIOaMrlZqkmbifs3FkMNjTc(userId, this));
    }

    /* JADX WARNING: Removed duplicated region for block: B:9:0x003f A:{Splitter: B:3:0x0013, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:9:0x003f, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:10:0x0040, code:
            android.util.Log.e(LOG_TAG, "Could not retrieve certificates from KeyChain service", r0);
     */
    /* JADX WARNING: Missing block: B:11:0x0049, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateInstalledCertificates(UserHandle userHandle) {
        if (this.mInjector.getUserManager().isUserUnlocked(userHandle.getIdentifier())) {
            try {
                List<String> installedCerts = getInstalledCaCertificates(userHandle);
                this.mService.onInstalledCertificatesChanged(userHandle, installedCerts);
                int pendingCertificateCount = installedCerts.size() - this.mService.getAcceptedCaCertificates(userHandle).size();
                if (pendingCertificateCount != 0) {
                    this.mInjector.getNotificationManager().notifyAsUser(LOG_TAG, 33, buildNotification(userHandle, pendingCertificateCount), userHandle);
                } else {
                    this.mInjector.getNotificationManager().cancelAsUser(LOG_TAG, 33, userHandle);
                }
            } catch (Exception e) {
            }
        }
    }

    private Notification buildNotification(UserHandle userHandle, int pendingCertificateCount) {
        try {
            String contentText;
            int smallIconId;
            Context userContext = this.mInjector.createContextAsUser(userHandle);
            Resources resources = this.mInjector.getResources();
            int parentUserId = userHandle.getIdentifier();
            if (this.mService.getProfileOwner(userHandle.getIdentifier()) != null) {
                contentText = resources.getString(17041045, new Object[]{this.mService.getProfileOwnerName(userHandle.getIdentifier())});
                smallIconId = 17303360;
                parentUserId = this.mService.getProfileParentId(userHandle.getIdentifier());
            } else if (this.mService.getDeviceOwnerUserId() == userHandle.getIdentifier()) {
                String ownerName = this.mService.getDeviceOwnerName();
                contentText = resources.getString(17041045, new Object[]{this.mService.getDeviceOwnerName()});
                smallIconId = 17303360;
            } else {
                contentText = resources.getString(17041044);
                smallIconId = 17301642;
            }
            Intent dialogIntent = new Intent("com.android.settings.MONITORING_CERT_INFO");
            dialogIntent.setFlags(268468224);
            dialogIntent.putExtra("android.settings.extra.number_of_certificates", pendingCertificateCount);
            dialogIntent.putExtra("android.intent.extra.USER_ID", userHandle.getIdentifier());
            ActivityInfo targetInfo = dialogIntent.resolveActivityInfo(this.mInjector.getPackageManager(), DumpState.DUMP_DEXOPT);
            if (targetInfo != null) {
                dialogIntent.setComponent(targetInfo.getComponentName());
            }
            return new Builder(userContext, SystemNotificationChannels.SECURITY).setSmallIcon(smallIconId).setContentTitle(resources.getQuantityText(18153499, pendingCertificateCount)).setContentText(contentText).setContentIntent(this.mInjector.pendingIntentGetActivityAsUser(userContext, 0, dialogIntent, 134217728, null, UserHandle.of(parentUserId))).setShowWhen(false).setColor(17170769).build();
        } catch (NameNotFoundException e) {
            Log.e(LOG_TAG, "Create context as " + userHandle + " failed", e);
            return null;
        }
    }

    private static X509Certificate parseCert(byte[] certBuffer) throws CertificateException {
        return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(certBuffer));
    }
}
