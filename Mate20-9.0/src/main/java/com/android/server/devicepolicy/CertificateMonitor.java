package com.android.server.devicepolicy;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Handler;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.storage.StorageManager;
import android.security.Credentials;
import android.security.KeyChain;
import android.util.Log;
import com.android.internal.notification.SystemNotificationChannels;
import com.android.server.devicepolicy.DevicePolicyManagerService;
import com.android.server.pm.DumpState;
import huawei.cust.HwCustUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;

public class CertificateMonitor {
    protected static final String LOG_TAG = "DevicePolicyManager";
    protected static final int MONITORING_CERT_NOTIFICATION_ID = 33;
    private static HwCustDevicePolicyManagerService mHwCustDevicePolicyManagerService = ((HwCustDevicePolicyManagerService) HwCustUtils.createObj(HwCustDevicePolicyManagerService.class, new Object[0]));
    private final Handler mHandler;
    private final DevicePolicyManagerService.Injector mInjector;
    private final BroadcastReceiver mRootCaReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (!StorageManager.inCryptKeeperBounce()) {
                CertificateMonitor.this.updateInstalledCertificates(UserHandle.of(intent.getIntExtra("android.intent.extra.user_handle", getSendingUserId())));
            }
        }
    };
    private final DevicePolicyManagerService mService;
    private String notAllowedApp = null;

    public CertificateMonitor(DevicePolicyManagerService service, DevicePolicyManagerService.Injector injector, Handler handler) {
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

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0026, code lost:
        r3 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0027, code lost:
        r4 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x002b, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x002c, code lost:
        r5 = r4;
        r4 = r3;
        r3 = r5;
     */
    public String installCaCert(UserHandle userHandle, byte[] certBuffer) {
        Throwable th;
        Throwable th2;
        try {
            byte[] pemCert = Credentials.convertToPem(new Certificate[]{parseCert(certBuffer)});
            try {
                KeyChain.KeyChainConnection keyChainConnection = this.mInjector.keyChainBindAsUser(userHandle);
                String installCaCertificate = keyChainConnection.getService().installCaCertificate(pemCert);
                if (keyChainConnection != null) {
                    $closeResource(null, keyChainConnection);
                }
                return installCaCertificate;
                if (keyChainConnection != null) {
                    $closeResource(th, keyChainConnection);
                }
                throw th2;
            } catch (RemoteException e) {
                Log.e(LOG_TAG, "installCaCertsToKeyChain(): ", e);
                return null;
            } catch (InterruptedException e1) {
                Log.w(LOG_TAG, "installCaCertsToKeyChain(): ", e1);
                Thread.currentThread().interrupt();
                return null;
            }
        } catch (IOException | CertificateException ce) {
            Log.e(LOG_TAG, "Problem converting cert", ce);
            return null;
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

    public void uninstallCaCerts(UserHandle userHandle, String[] aliases) {
        KeyChain.KeyChainConnection keyChainConnection;
        try {
            keyChainConnection = this.mInjector.keyChainBindAsUser(userHandle);
            for (String deleteCaCertificate : aliases) {
                keyChainConnection.getService().deleteCaCertificate(deleteCaCertificate);
            }
            if (keyChainConnection != null) {
                $closeResource(null, keyChainConnection);
            }
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "from CaCertUninstaller: ", e);
        } catch (InterruptedException ie) {
            Log.w(LOG_TAG, "CaCertUninstaller: ", ie);
            Thread.currentThread().interrupt();
        } catch (Throwable th) {
            if (keyChainConnection != null) {
                $closeResource(r1, keyChainConnection);
            }
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001a, code lost:
        r3 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x001e, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x001f, code lost:
        r4 = r3;
        r3 = r2;
        r2 = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0019, code lost:
        r2 = th;
     */
    public List<String> getInstalledCaCertificates(UserHandle userHandle) throws RemoteException, RuntimeException {
        Throwable th;
        Throwable th2;
        try {
            KeyChain.KeyChainConnection conn = this.mInjector.keyChainBindAsUser(userHandle);
            List<String> list = conn.getService().getUserCaAliases().getList();
            if (conn != null) {
                $closeResource(null, conn);
            }
            return list;
            if (conn != null) {
                $closeResource(th, conn);
            }
            throw th2;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (AssertionError e2) {
            throw new RuntimeException(e2);
        }
    }

    public void onCertificateApprovalsChanged(int userId) {
        this.mHandler.post(new Runnable(userId) {
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                CertificateMonitor.this.updateInstalledCertificates(UserHandle.of(this.f$1));
            }
        });
    }

    /* access modifiers changed from: private */
    public void updateInstalledCertificates(UserHandle userHandle) {
        if (this.mInjector.getUserManager().isUserUnlocked(userHandle.getIdentifier())) {
            try {
                List<String> installedCerts = getInstalledCaCertificates(userHandle);
                this.mService.onInstalledCertificatesChanged(userHandle, installedCerts);
                int pendingCertificateCount = installedCerts.size() - this.mService.getAcceptedCaCertificates(userHandle).size();
                if (pendingCertificateCount != 0) {
                    Notification noti = buildNotification(userHandle, pendingCertificateCount);
                    if (mHwCustDevicePolicyManagerService == null || mHwCustDevicePolicyManagerService.isCertNotificationAllowed(this.notAllowedApp)) {
                        this.mInjector.getNotificationManager().notifyAsUser(LOG_TAG, 33, noti, userHandle);
                    }
                } else {
                    this.mInjector.getNotificationManager().cancelAsUser(LOG_TAG, 33, userHandle);
                }
            } catch (RemoteException | RuntimeException e) {
                Log.e(LOG_TAG, "Could not retrieve certificates from KeyChain service", e);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x00bd  */
    private Notification buildNotification(UserHandle userHandle, int pendingCertificateCount) {
        String contentText;
        int parentUserId;
        int smallIconId;
        ActivityInfo targetInfo;
        String contentText2;
        int i = pendingCertificateCount;
        try {
            Context userContext = this.mInjector.createContextAsUser(userHandle);
            Resources resources = this.mInjector.getResources();
            int parentUserId2 = userHandle.getIdentifier();
            if (this.mService.getProfileOwner(userHandle.getIdentifier()) != null) {
                contentText2 = resources.getString(17041171, new Object[]{this.mService.getProfileOwnerName(userHandle.getIdentifier())});
                smallIconId = 17303513;
                parentUserId2 = this.mService.getProfileParentId(userHandle.getIdentifier());
                this.notAllowedApp = this.mService.getProfileOwnerName(userHandle.getIdentifier());
            } else if (this.mService.getDeviceOwnerUserId() == userHandle.getIdentifier()) {
                String deviceOwnerName = this.mService.getDeviceOwnerName();
                String contentText3 = resources.getString(17041171, new Object[]{this.mService.getDeviceOwnerName()});
                this.notAllowedApp = this.mService.getDeviceOwnerName();
                parentUserId = parentUserId2;
                contentText = contentText3;
                smallIconId = 17303513;
                int smallIconId2 = smallIconId;
                Intent dialogIntent = new Intent("com.android.settings.MONITORING_CERT_INFO");
                dialogIntent.setFlags(268468224);
                dialogIntent.putExtra("android.settings.extra.number_of_certificates", i);
                dialogIntent.putExtra("android.intent.extra.USER_ID", userHandle.getIdentifier());
                targetInfo = dialogIntent.resolveActivityInfo(this.mInjector.getPackageManager(), DumpState.DUMP_DEXOPT);
                if (targetInfo != null) {
                    dialogIntent.setComponent(targetInfo.getComponentName());
                }
                ActivityInfo activityInfo = targetInfo;
                Intent intent = dialogIntent;
                return new Notification.Builder(userContext, SystemNotificationChannels.SECURITY).setSmallIcon(smallIconId2).setContentTitle(resources.getQuantityText(18153500, i)).setContentText(contentText).setContentIntent(this.mInjector.pendingIntentGetActivityAsUser(userContext, 0, dialogIntent, 134217728, null, UserHandle.of(parentUserId))).setShowWhen(false).setColor(17170784).build();
            } else {
                contentText2 = resources.getString(17041170);
                smallIconId = 17301642;
            }
            parentUserId = parentUserId2;
            contentText = contentText2;
            int smallIconId22 = smallIconId;
            Intent dialogIntent2 = new Intent("com.android.settings.MONITORING_CERT_INFO");
            dialogIntent2.setFlags(268468224);
            dialogIntent2.putExtra("android.settings.extra.number_of_certificates", i);
            dialogIntent2.putExtra("android.intent.extra.USER_ID", userHandle.getIdentifier());
            targetInfo = dialogIntent2.resolveActivityInfo(this.mInjector.getPackageManager(), DumpState.DUMP_DEXOPT);
            if (targetInfo != null) {
            }
            ActivityInfo activityInfo2 = targetInfo;
            Intent intent2 = dialogIntent2;
            return new Notification.Builder(userContext, SystemNotificationChannels.SECURITY).setSmallIcon(smallIconId22).setContentTitle(resources.getQuantityText(18153500, i)).setContentText(contentText).setContentIntent(this.mInjector.pendingIntentGetActivityAsUser(userContext, 0, dialogIntent2, 134217728, null, UserHandle.of(parentUserId))).setShowWhen(false).setColor(17170784).build();
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(LOG_TAG, "Create context as " + r2 + " failed", e);
            return null;
        }
    }

    private static X509Certificate parseCert(byte[] certBuffer) throws CertificateException {
        return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(certBuffer));
    }
}
