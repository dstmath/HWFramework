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
        /* class com.android.server.devicepolicy.CertificateMonitor.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
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

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x002a, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x002b, code lost:
        if (r4 != null) goto L_0x002d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x002d, code lost:
        $closeResource(r5, r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0030, code lost:
        throw r6;
     */
    public String installCaCert(UserHandle userHandle, byte[] certBuffer) {
        try {
            byte[] pemCert = Credentials.convertToPem(new Certificate[]{parseCert(certBuffer)});
            try {
                KeyChain.KeyChainConnection keyChainConnection = this.mInjector.keyChainBindAsUser(userHandle);
                String installCaCertificate = keyChainConnection.getService().installCaCertificate(pemCert);
                $closeResource(null, keyChainConnection);
                return installCaCertificate;
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

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0021, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0022, code lost:
        if (r1 != null) goto L_0x0024;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0024, code lost:
        $closeResource(r2, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0027, code lost:
        throw r3;
     */
    public void uninstallCaCerts(UserHandle userHandle, String[] aliases) {
        try {
            KeyChain.KeyChainConnection keyChainConnection = this.mInjector.keyChainBindAsUser(userHandle);
            for (String str : aliases) {
                keyChainConnection.getService().deleteCaCertificate(str);
            }
            if (keyChainConnection != null) {
                $closeResource(null, keyChainConnection);
            }
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "from CaCertUninstaller: ", e);
        } catch (InterruptedException ie) {
            Log.w(LOG_TAG, "CaCertUninstaller: ", ie);
            Thread.currentThread().interrupt();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0019, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x001a, code lost:
        if (r1 != null) goto L_0x001c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001c, code lost:
        $closeResource(r2, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x001f, code lost:
        throw r3;
     */
    public List<String> getInstalledCaCertificates(UserHandle userHandle) throws RemoteException, RuntimeException {
        try {
            KeyChain.KeyChainConnection conn = this.mInjector.keyChainBindAsUser(userHandle);
            List<String> list = conn.getService().getUserCaAliases().getList();
            $closeResource(null, conn);
            return list;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (AssertionError e2) {
            throw new RuntimeException(e2);
        }
    }

    public /* synthetic */ void lambda$onCertificateApprovalsChanged$0$CertificateMonitor(int userId) {
        updateInstalledCertificates(UserHandle.of(userId));
    }

    public void onCertificateApprovalsChanged(int userId) {
        this.mHandler.post(new Runnable(userId) {
            /* class com.android.server.devicepolicy.$$Lambda$CertificateMonitor$nzwzuvk_fK7AIlili6jDKrKWLJM */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                CertificateMonitor.this.lambda$onCertificateApprovalsChanged$0$CertificateMonitor(this.f$1);
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateInstalledCertificates(UserHandle userHandle) {
        if (this.mInjector.getUserManager().isUserUnlocked(userHandle.getIdentifier())) {
            try {
                List<String> installedCerts = getInstalledCaCertificates(userHandle);
                this.mService.onInstalledCertificatesChanged(userHandle, installedCerts);
                int pendingCertificateCount = installedCerts.size() - this.mService.getAcceptedCaCertificates(userHandle).size();
                if (pendingCertificateCount != 0) {
                    Notification noti = buildNotification(userHandle, pendingCertificateCount);
                    HwCustDevicePolicyManagerService hwCustDevicePolicyManagerService = mHwCustDevicePolicyManagerService;
                    if (hwCustDevicePolicyManagerService == null || hwCustDevicePolicyManagerService.isCertNotificationAllowed(this.notAllowedApp)) {
                        this.mInjector.getNotificationManager().notifyAsUser(LOG_TAG, 33, noti, userHandle);
                        return;
                    }
                    return;
                }
                this.mInjector.getNotificationManager().cancelAsUser(LOG_TAG, 33, userHandle);
            } catch (RemoteException | RuntimeException e) {
                Log.e(LOG_TAG, "Could not retrieve certificates from KeyChain service", e);
            }
        }
    }

    private Notification buildNotification(UserHandle userHandle, int pendingCertificateCount) {
        String contentText;
        int parentUserId;
        int smallIconId;
        try {
            Context userContext = this.mInjector.createContextAsUser(userHandle);
            Resources resources = this.mInjector.getResources();
            int parentUserId2 = userHandle.getIdentifier();
            if (this.mService.getProfileOwner(userHandle.getIdentifier()) != null) {
                String contentText2 = resources.getString(17041294, this.mService.getProfileOwnerName(userHandle.getIdentifier()));
                int parentUserId3 = this.mService.getProfileParentId(userHandle.getIdentifier());
                this.notAllowedApp = this.mService.getProfileOwnerName(userHandle.getIdentifier());
                parentUserId = parentUserId3;
                contentText = contentText2;
                smallIconId = 17303575;
            } else if (this.mService.getDeviceOwnerUserId() == userHandle.getIdentifier()) {
                this.mService.getDeviceOwnerName();
                String contentText3 = resources.getString(17041294, this.mService.getDeviceOwnerName());
                this.notAllowedApp = this.mService.getDeviceOwnerName();
                parentUserId = parentUserId2;
                contentText = contentText3;
                smallIconId = 17303575;
            } else {
                parentUserId = parentUserId2;
                contentText = resources.getString(17041293);
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
            return new Notification.Builder(userContext, SystemNotificationChannels.SECURITY).setSmallIcon(smallIconId).setContentTitle(resources.getQuantityText(18153498, pendingCertificateCount)).setContentText(contentText).setContentIntent(this.mInjector.pendingIntentGetActivityAsUser(userContext, 0, dialogIntent, DumpState.DUMP_HWFEATURES, null, UserHandle.of(parentUserId))).setShowWhen(false).setColor(17170460).build();
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(LOG_TAG, "Create context as " + userHandle + " failed", e);
            return null;
        }
    }

    private static X509Certificate parseCert(byte[] certBuffer) throws CertificateException {
        return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(certBuffer));
    }
}
