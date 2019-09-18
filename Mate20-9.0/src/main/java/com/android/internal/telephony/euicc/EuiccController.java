package com.android.internal.telephony.euicc;

import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.ServiceManager;
import android.provider.Settings;
import android.service.euicc.GetDefaultDownloadableSubscriptionListResult;
import android.service.euicc.GetDownloadableSubscriptionMetadataResult;
import android.service.euicc.GetEuiccProfileInfoListResult;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.UiccAccessRule;
import android.telephony.euicc.DownloadableSubscription;
import android.telephony.euicc.EuiccInfo;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.euicc.EuiccConnector;
import com.android.internal.telephony.euicc.IEuiccController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class EuiccController extends IEuiccController.Stub {
    private static final int ERROR = 2;
    private static final String EXTRA_EMBEDDED_SUBSCRIPTION_DOWNLOADABLE_SUBSCRIPTION = "android.telephony.euicc.extra.EMBEDDED_SUBSCRIPTION_DOWNLOADABLE_SUBSCRIPTION";
    @VisibleForTesting
    static final String EXTRA_OPERATION = "operation";
    private static final int OK = 0;
    private static final int RESOLVABLE_ERROR = 1;
    private static final String TAG = "EuiccController";
    private static EuiccController sInstance;
    private final AppOpsManager mAppOpsManager;
    private final EuiccConnector mConnector;
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public final PackageManager mPackageManager;
    private final SubscriptionManager mSubscriptionManager;

    class DownloadSubscriptionGetMetadataCommandCallback extends GetMetadataCommandCallback {
        private final boolean mForceDeactivateSim;
        private final boolean mSwitchAfterDownload;

        DownloadSubscriptionGetMetadataCommandCallback(long callingToken, DownloadableSubscription subscription, boolean switchAfterDownload, String callingPackage, boolean forceDeactivateSim, PendingIntent callbackIntent) {
            super(callingToken, subscription, callingPackage, callbackIntent);
            this.mSwitchAfterDownload = switchAfterDownload;
            this.mForceDeactivateSim = forceDeactivateSim;
        }

        /* JADX WARNING: type inference failed for: r3v8, types: [java.lang.Object[]] */
        /* JADX WARNING: Multi-variable type inference failed */
        public void onGetMetadataComplete(GetDownloadableSubscriptionMetadataResult result) {
            if (result.getResult() == -1) {
                Intent extrasIntent = new Intent();
                EuiccController.this.addResolutionIntent(extrasIntent, "android.service.euicc.action.RESOLVE_NO_PRIVILEGES", this.mCallingPackage, false, EuiccOperation.forDownloadNoPrivileges(this.mCallingToken, this.mSubscription, this.mSwitchAfterDownload, this.mCallingPackage));
                EuiccController.this.sendResult(this.mCallbackIntent, 1, extrasIntent);
            } else if (result.getResult() != 0) {
                super.onGetMetadataComplete(result);
            } else {
                DownloadableSubscription subscription = result.getDownloadableSubscription();
                UiccAccessRule[] rules = null;
                List<UiccAccessRule> rulesList = subscription.getAccessRules();
                if (rulesList != null) {
                    rules = rulesList.toArray(new UiccAccessRule[rulesList.size()]);
                }
                UiccAccessRule[] rules2 = rules;
                if (rules2 == null) {
                    Log.e(EuiccController.TAG, "No access rules but caller is unprivileged");
                    EuiccController.this.sendResult(this.mCallbackIntent, 2, null);
                    return;
                }
                try {
                    PackageInfo info = EuiccController.this.mPackageManager.getPackageInfo(this.mCallingPackage, 64);
                    int i = 0;
                    while (true) {
                        int i2 = i;
                        if (i2 >= rules2.length) {
                            Log.e(EuiccController.TAG, "Caller is not permitted to download this profile");
                            EuiccController.this.sendResult(this.mCallbackIntent, 2, null);
                            return;
                        } else if (rules2[i2].getCarrierPrivilegeStatus(info) != 1) {
                            i = i2 + 1;
                        } else if (EuiccController.this.canManageActiveSubscription(this.mCallingPackage)) {
                            EuiccController.this.downloadSubscriptionPrivileged(this.mCallingToken, subscription, this.mSwitchAfterDownload, this.mForceDeactivateSim, this.mCallingPackage, this.mCallbackIntent);
                            return;
                        } else {
                            Intent extrasIntent2 = new Intent();
                            EuiccController.this.addResolutionIntent(extrasIntent2, "android.service.euicc.action.RESOLVE_NO_PRIVILEGES", this.mCallingPackage, false, EuiccOperation.forDownloadNoPrivileges(this.mCallingToken, subscription, this.mSwitchAfterDownload, this.mCallingPackage));
                            EuiccController.this.sendResult(this.mCallbackIntent, 1, extrasIntent2);
                            return;
                        }
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e(EuiccController.TAG, "Calling package valid but gone");
                    EuiccController.this.sendResult(this.mCallbackIntent, 2, null);
                }
            }
        }

        /* access modifiers changed from: protected */
        public EuiccOperation getOperationForDeactivateSim() {
            return EuiccOperation.forDownloadDeactivateSim(this.mCallingToken, this.mSubscription, this.mSwitchAfterDownload, this.mCallingPackage);
        }
    }

    class GetDefaultListCommandCallback implements EuiccConnector.GetDefaultListCommandCallback {
        final PendingIntent mCallbackIntent;
        final String mCallingPackage;
        final long mCallingToken;

        GetDefaultListCommandCallback(long callingToken, String callingPackage, PendingIntent callbackIntent) {
            this.mCallingToken = callingToken;
            this.mCallingPackage = callingPackage;
            this.mCallbackIntent = callbackIntent;
        }

        public void onGetDefaultListComplete(GetDefaultDownloadableSubscriptionListResult result) {
            int resultCode;
            Intent extrasIntent = new Intent();
            switch (result.getResult()) {
                case -1:
                    EuiccController.this.addResolutionIntent(extrasIntent, "android.service.euicc.action.RESOLVE_DEACTIVATE_SIM", this.mCallingPackage, false, EuiccOperation.forGetDefaultListDeactivateSim(this.mCallingToken, this.mCallingPackage));
                    resultCode = 1;
                    break;
                case 0:
                    resultCode = 0;
                    List<DownloadableSubscription> list = result.getDownloadableSubscriptions();
                    if (list != null && list.size() > 0) {
                        extrasIntent.putExtra("android.telephony.euicc.extra.EMBEDDED_SUBSCRIPTION_DOWNLOADABLE_SUBSCRIPTIONS", (Parcelable[]) list.toArray(new DownloadableSubscription[list.size()]));
                        break;
                    }
                default:
                    resultCode = 2;
                    extrasIntent.putExtra("android.telephony.euicc.extra.EMBEDDED_SUBSCRIPTION_DETAILED_CODE", result.getResult());
                    break;
            }
            EuiccController.this.sendResult(this.mCallbackIntent, resultCode, extrasIntent);
        }

        public void onEuiccServiceUnavailable() {
            EuiccController.this.sendResult(this.mCallbackIntent, 2, null);
        }
    }

    class GetMetadataCommandCallback implements EuiccConnector.GetMetadataCommandCallback {
        protected final PendingIntent mCallbackIntent;
        protected final String mCallingPackage;
        protected final long mCallingToken;
        protected final DownloadableSubscription mSubscription;

        GetMetadataCommandCallback(long callingToken, DownloadableSubscription subscription, String callingPackage, PendingIntent callbackIntent) {
            this.mCallingToken = callingToken;
            this.mSubscription = subscription;
            this.mCallingPackage = callingPackage;
            this.mCallbackIntent = callbackIntent;
        }

        public void onGetMetadataComplete(GetDownloadableSubscriptionMetadataResult result) {
            int resultCode;
            Intent extrasIntent = new Intent();
            switch (result.getResult()) {
                case -1:
                    EuiccController.this.addResolutionIntent(extrasIntent, "android.service.euicc.action.RESOLVE_DEACTIVATE_SIM", this.mCallingPackage, false, getOperationForDeactivateSim());
                    resultCode = 1;
                    break;
                case 0:
                    resultCode = 0;
                    extrasIntent.putExtra(EuiccController.EXTRA_EMBEDDED_SUBSCRIPTION_DOWNLOADABLE_SUBSCRIPTION, result.getDownloadableSubscription());
                    break;
                default:
                    resultCode = 2;
                    extrasIntent.putExtra("android.telephony.euicc.extra.EMBEDDED_SUBSCRIPTION_DETAILED_CODE", result.getResult());
                    break;
            }
            EuiccController.this.sendResult(this.mCallbackIntent, resultCode, extrasIntent);
        }

        public void onEuiccServiceUnavailable() {
            EuiccController.this.sendResult(this.mCallbackIntent, 2, null);
        }

        /* access modifiers changed from: protected */
        public EuiccOperation getOperationForDeactivateSim() {
            return EuiccOperation.forGetMetadataDeactivateSim(this.mCallingToken, this.mSubscription, this.mCallingPackage);
        }
    }

    public static EuiccController init(Context context) {
        synchronized (EuiccController.class) {
            if (sInstance == null) {
                sInstance = new EuiccController(context);
            } else {
                Log.wtf(TAG, "init() called multiple times! sInstance = " + sInstance);
            }
        }
        return sInstance;
    }

    public static EuiccController get() {
        if (sInstance == null) {
            synchronized (EuiccController.class) {
                if (sInstance == null) {
                    throw new IllegalStateException("get() called before init()");
                }
            }
        }
        return sInstance;
    }

    /* JADX WARNING: type inference failed for: r1v0, types: [com.android.internal.telephony.euicc.EuiccController, android.os.IBinder] */
    private EuiccController(Context context) {
        this(context, new EuiccConnector(context));
        ServiceManager.addService("econtroller", this);
    }

    @VisibleForTesting
    public EuiccController(Context context, EuiccConnector connector) {
        this.mContext = context;
        this.mConnector = connector;
        this.mSubscriptionManager = (SubscriptionManager) context.getSystemService("telephony_subscription_service");
        this.mAppOpsManager = (AppOpsManager) context.getSystemService("appops");
        this.mPackageManager = context.getPackageManager();
    }

    public void continueOperation(Intent resolutionIntent, Bundle resolutionExtras) {
        if (callerCanWriteEmbeddedSubscriptions()) {
            long token = Binder.clearCallingIdentity();
            try {
                EuiccOperation op = (EuiccOperation) resolutionIntent.getParcelableExtra(EXTRA_OPERATION);
                if (op != null) {
                    PendingIntent callbackIntent = (PendingIntent) resolutionIntent.getParcelableExtra("android.telephony.euicc.extra.EMBEDDED_SUBSCRIPTION_RESOLUTION_CALLBACK_INTENT");
                    if (callbackIntent != null) {
                        op.continueOperation(resolutionExtras, callbackIntent);
                    }
                    return;
                }
                throw new IllegalArgumentException("Invalid resolution intent");
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        } else {
            throw new SecurityException("Must have WRITE_EMBEDDED_SUBSCRIPTIONS to continue operation");
        }
    }

    public String getEid() {
        if (callerCanReadPhoneStatePrivileged() || callerHasCarrierPrivilegesForActiveSubscription()) {
            long token = Binder.clearCallingIdentity();
            try {
                return blockingGetEidFromEuiccService();
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        } else {
            throw new SecurityException("Must have carrier privileges on active subscription to read EID");
        }
    }

    public int getOtaStatus() {
        if (callerCanWriteEmbeddedSubscriptions()) {
            long token = Binder.clearCallingIdentity();
            try {
                return blockingGetOtaStatusFromEuiccService();
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        } else {
            throw new SecurityException("Must have WRITE_EMBEDDED_SUBSCRIPTIONS to get OTA status");
        }
    }

    public void startOtaUpdatingIfNecessary() {
        this.mConnector.startOtaIfNecessary(new EuiccConnector.OtaStatusChangedCallback() {
            public void onOtaStatusChanged(int status) {
                EuiccController.this.sendOtaStatusChangedBroadcast();
            }

            public void onEuiccServiceUnavailable() {
            }
        });
    }

    public void getDownloadableSubscriptionMetadata(DownloadableSubscription subscription, String callingPackage, PendingIntent callbackIntent) {
        getDownloadableSubscriptionMetadata(subscription, false, callingPackage, callbackIntent);
    }

    /* access modifiers changed from: package-private */
    public void getDownloadableSubscriptionMetadata(DownloadableSubscription subscription, boolean forceDeactivateSim, String callingPackage, PendingIntent callbackIntent) {
        if (callerCanWriteEmbeddedSubscriptions()) {
            this.mAppOpsManager.checkPackage(Binder.getCallingUid(), callingPackage);
            long token = Binder.clearCallingIdentity();
            try {
                EuiccConnector euiccConnector = this.mConnector;
                GetMetadataCommandCallback getMetadataCommandCallback = new GetMetadataCommandCallback(token, subscription, callingPackage, callbackIntent);
                euiccConnector.getDownloadableSubscriptionMetadata(subscription, forceDeactivateSim, getMetadataCommandCallback);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        } else {
            throw new SecurityException("Must have WRITE_EMBEDDED_SUBSCRIPTIONS to get metadata");
        }
    }

    public void downloadSubscription(DownloadableSubscription subscription, boolean switchAfterDownload, String callingPackage, PendingIntent callbackIntent) {
        downloadSubscription(subscription, switchAfterDownload, callingPackage, false, callbackIntent);
    }

    /* access modifiers changed from: package-private */
    public void downloadSubscription(DownloadableSubscription subscription, boolean switchAfterDownload, String callingPackage, boolean forceDeactivateSim, PendingIntent callbackIntent) {
        boolean callerCanWriteEmbeddedSubscriptions = callerCanWriteEmbeddedSubscriptions();
        String str = callingPackage;
        this.mAppOpsManager.checkPackage(Binder.getCallingUid(), str);
        long token = Binder.clearCallingIdentity();
        if (callerCanWriteEmbeddedSubscriptions) {
            try {
                downloadSubscriptionPrivileged(token, subscription, switchAfterDownload, forceDeactivateSim, str, callbackIntent);
                Binder.restoreCallingIdentity(token);
            } catch (Throwable th) {
                th = th;
                DownloadableSubscription downloadableSubscription = subscription;
                boolean z = forceDeactivateSim;
                Binder.restoreCallingIdentity(token);
                throw th;
            }
        } else {
            EuiccConnector euiccConnector = this.mConnector;
            DownloadSubscriptionGetMetadataCommandCallback downloadSubscriptionGetMetadataCommandCallback = new DownloadSubscriptionGetMetadataCommandCallback(token, subscription, switchAfterDownload, str, forceDeactivateSim, callbackIntent);
            try {
                euiccConnector.getDownloadableSubscriptionMetadata(subscription, forceDeactivateSim, downloadSubscriptionGetMetadataCommandCallback);
                Binder.restoreCallingIdentity(token);
            } catch (Throwable th2) {
                th = th2;
                Binder.restoreCallingIdentity(token);
                throw th;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void downloadSubscriptionPrivileged(long callingToken, DownloadableSubscription subscription, boolean switchAfterDownload, boolean forceDeactivateSim, String callingPackage, PendingIntent callbackIntent) {
        EuiccConnector euiccConnector = this.mConnector;
        final DownloadableSubscription downloadableSubscription = subscription;
        final boolean z = switchAfterDownload;
        final PendingIntent pendingIntent = callbackIntent;
        final String str = callingPackage;
        final long j = callingToken;
        AnonymousClass2 r0 = new EuiccConnector.DownloadCommandCallback() {
            public void onDownloadComplete(int result) {
                int resultCode;
                int resultCode2;
                Intent extrasIntent = new Intent();
                switch (result) {
                    case -2:
                        resultCode2 = 1;
                        boolean retried = false;
                        if (!TextUtils.isEmpty(downloadableSubscription.getConfirmationCode())) {
                            retried = true;
                        }
                        boolean retried2 = retried;
                        EuiccController.this.addResolutionIntent(extrasIntent, "android.service.euicc.action.RESOLVE_CONFIRMATION_CODE", str, retried2, EuiccOperation.forDownloadConfirmationCode(j, downloadableSubscription, z, str));
                        break;
                    case -1:
                        resultCode2 = 1;
                        EuiccController.this.addResolutionIntent(extrasIntent, "android.service.euicc.action.RESOLVE_DEACTIVATE_SIM", str, false, EuiccOperation.forDownloadDeactivateSim(j, downloadableSubscription, z, str));
                        break;
                    case 0:
                        resultCode = 0;
                        Settings.Global.putInt(EuiccController.this.mContext.getContentResolver(), "euicc_provisioned", 1);
                        extrasIntent.putExtra(EuiccController.EXTRA_EMBEDDED_SUBSCRIPTION_DOWNLOADABLE_SUBSCRIPTION, downloadableSubscription);
                        if (!z) {
                            EuiccController.this.refreshSubscriptionsAndSendResult(pendingIntent, 0, extrasIntent);
                            return;
                        }
                        break;
                    default:
                        resultCode = 2;
                        extrasIntent.putExtra("android.telephony.euicc.extra.EMBEDDED_SUBSCRIPTION_DETAILED_CODE", result);
                        break;
                }
                resultCode = resultCode2;
                EuiccController.this.sendResult(pendingIntent, resultCode, extrasIntent);
            }

            public void onEuiccServiceUnavailable() {
                EuiccController.this.sendResult(pendingIntent, 2, null);
            }
        };
        euiccConnector.downloadSubscription(subscription, switchAfterDownload, forceDeactivateSim, r0);
    }

    public GetEuiccProfileInfoListResult blockingGetEuiccProfileInfoList() {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<GetEuiccProfileInfoListResult> resultRef = new AtomicReference<>();
        this.mConnector.getEuiccProfileInfoList(new EuiccConnector.GetEuiccProfileInfoListCommandCallback() {
            public void onListComplete(GetEuiccProfileInfoListResult result) {
                resultRef.set(result);
                latch.countDown();
            }

            public void onEuiccServiceUnavailable() {
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return (GetEuiccProfileInfoListResult) resultRef.get();
    }

    public void getDefaultDownloadableSubscriptionList(String callingPackage, PendingIntent callbackIntent) {
        getDefaultDownloadableSubscriptionList(false, callingPackage, callbackIntent);
    }

    /* access modifiers changed from: package-private */
    public void getDefaultDownloadableSubscriptionList(boolean forceDeactivateSim, String callingPackage, PendingIntent callbackIntent) {
        if (callerCanWriteEmbeddedSubscriptions()) {
            this.mAppOpsManager.checkPackage(Binder.getCallingUid(), callingPackage);
            long token = Binder.clearCallingIdentity();
            try {
                EuiccConnector euiccConnector = this.mConnector;
                GetDefaultListCommandCallback getDefaultListCommandCallback = new GetDefaultListCommandCallback(token, callingPackage, callbackIntent);
                euiccConnector.getDefaultDownloadableSubscriptionList(forceDeactivateSim, getDefaultListCommandCallback);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        } else {
            throw new SecurityException("Must have WRITE_EMBEDDED_SUBSCRIPTIONS to get default list");
        }
    }

    public EuiccInfo getEuiccInfo() {
        long token = Binder.clearCallingIdentity();
        try {
            return blockingGetEuiccInfoFromEuiccService();
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public void deleteSubscription(int subscriptionId, String callingPackage, PendingIntent callbackIntent) {
        boolean callerCanWriteEmbeddedSubscriptions = callerCanWriteEmbeddedSubscriptions();
        this.mAppOpsManager.checkPackage(Binder.getCallingUid(), callingPackage);
        long token = Binder.clearCallingIdentity();
        try {
            SubscriptionInfo sub = getSubscriptionForSubscriptionId(subscriptionId);
            if (sub == null) {
                Log.e(TAG, "Cannot delete nonexistent subscription: " + subscriptionId);
                sendResult(callbackIntent, 2, null);
                return;
            }
            if (!callerCanWriteEmbeddedSubscriptions) {
                if (!sub.canManageSubscription(this.mContext, callingPackage)) {
                    Log.e(TAG, "No permissions: " + subscriptionId);
                    sendResult(callbackIntent, 2, null);
                    Binder.restoreCallingIdentity(token);
                    return;
                }
            }
            deleteSubscriptionPrivileged(sub.getIccId(), callbackIntent);
            Binder.restoreCallingIdentity(token);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    /* access modifiers changed from: package-private */
    public void deleteSubscriptionPrivileged(String iccid, final PendingIntent callbackIntent) {
        this.mConnector.deleteSubscription(iccid, new EuiccConnector.DeleteCommandCallback() {
            public void onDeleteComplete(int result) {
                Intent extrasIntent = new Intent();
                if (result != 0) {
                    extrasIntent.putExtra("android.telephony.euicc.extra.EMBEDDED_SUBSCRIPTION_DETAILED_CODE", result);
                    EuiccController.this.sendResult(callbackIntent, 2, extrasIntent);
                    return;
                }
                EuiccController.this.refreshSubscriptionsAndSendResult(callbackIntent, 0, extrasIntent);
            }

            public void onEuiccServiceUnavailable() {
                EuiccController.this.sendResult(callbackIntent, 2, null);
            }
        });
    }

    public void switchToSubscription(int subscriptionId, String callingPackage, PendingIntent callbackIntent) {
        switchToSubscription(subscriptionId, false, callingPackage, callbackIntent);
    }

    /* access modifiers changed from: package-private */
    public void switchToSubscription(int subscriptionId, boolean forceDeactivateSim, String callingPackage, PendingIntent callbackIntent) {
        long token;
        String iccid;
        int i = subscriptionId;
        String str = callingPackage;
        PendingIntent pendingIntent = callbackIntent;
        boolean callerCanWriteEmbeddedSubscriptions = callerCanWriteEmbeddedSubscriptions();
        this.mAppOpsManager.checkPackage(Binder.getCallingUid(), str);
        long token2 = Binder.clearCallingIdentity();
        boolean forceDeactivateSim2 = callerCanWriteEmbeddedSubscriptions ? true : forceDeactivateSim;
        if (i != -1) {
            try {
                SubscriptionInfo sub = getSubscriptionForSubscriptionId(subscriptionId);
                if (sub == null) {
                    Log.e(TAG, "Cannot switch to nonexistent subscription: " + i);
                    sendResult(pendingIntent, 2, null);
                    Binder.restoreCallingIdentity(token2);
                    return;
                }
                if (!callerCanWriteEmbeddedSubscriptions) {
                    if (!this.mSubscriptionManager.canManageSubscription(sub, str)) {
                        Log.e(TAG, "Not permitted to switch to subscription: " + i);
                        sendResult(pendingIntent, 2, null);
                        Binder.restoreCallingIdentity(token2);
                        return;
                    }
                }
                iccid = sub.getIccId();
            } catch (Throwable th) {
                th = th;
                token = token2;
                Binder.restoreCallingIdentity(token);
                throw th;
            }
        } else if (!callerCanWriteEmbeddedSubscriptions) {
            try {
                Log.e(TAG, "Not permitted to switch to empty subscription");
                sendResult(pendingIntent, 2, null);
                Binder.restoreCallingIdentity(token2);
                return;
            } catch (Throwable th2) {
                th = th2;
                token = token2;
                Binder.restoreCallingIdentity(token);
                throw th;
            }
        } else {
            iccid = null;
        }
        if (!callerCanWriteEmbeddedSubscriptions) {
            if (!canManageActiveSubscription(str)) {
                Intent extrasIntent = new Intent();
                addResolutionIntent(extrasIntent, "android.service.euicc.action.RESOLVE_NO_PRIVILEGES", str, false, EuiccOperation.forSwitchNoPrivileges(token2, i, str));
                sendResult(pendingIntent, 1, extrasIntent);
                Binder.restoreCallingIdentity(token2);
                return;
            }
        }
        token = token2;
        try {
            switchToSubscriptionPrivileged(token2, i, iccid, forceDeactivateSim2, callingPackage, callbackIntent);
            Binder.restoreCallingIdentity(token);
        } catch (Throwable th3) {
            th = th3;
            Binder.restoreCallingIdentity(token);
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public void switchToSubscriptionPrivileged(long callingToken, int subscriptionId, boolean forceDeactivateSim, String callingPackage, PendingIntent callbackIntent) {
        String iccid = null;
        int i = subscriptionId;
        SubscriptionInfo sub = getSubscriptionForSubscriptionId(i);
        if (sub != null) {
            iccid = sub.getIccId();
        }
        switchToSubscriptionPrivileged(callingToken, i, iccid, forceDeactivateSim, callingPackage, callbackIntent);
    }

    /* access modifiers changed from: package-private */
    public void switchToSubscriptionPrivileged(long callingToken, int subscriptionId, String iccid, boolean forceDeactivateSim, String callingPackage, PendingIntent callbackIntent) {
        EuiccConnector euiccConnector = this.mConnector;
        final String str = callingPackage;
        final long j = callingToken;
        final int i = subscriptionId;
        final PendingIntent pendingIntent = callbackIntent;
        AnonymousClass5 r0 = new EuiccConnector.SwitchCommandCallback() {
            public void onSwitchComplete(int result) {
                int resultCode;
                Intent extrasIntent = new Intent();
                switch (result) {
                    case -1:
                        EuiccController.this.addResolutionIntent(extrasIntent, "android.service.euicc.action.RESOLVE_DEACTIVATE_SIM", str, false, EuiccOperation.forSwitchDeactivateSim(j, i, str));
                        resultCode = 1;
                        break;
                    case 0:
                        resultCode = 0;
                        break;
                    default:
                        resultCode = 2;
                        extrasIntent.putExtra("android.telephony.euicc.extra.EMBEDDED_SUBSCRIPTION_DETAILED_CODE", result);
                        break;
                }
                EuiccController.this.sendResult(pendingIntent, resultCode, extrasIntent);
            }

            public void onEuiccServiceUnavailable() {
                EuiccController.this.sendResult(pendingIntent, 2, null);
            }
        };
        euiccConnector.switchToSubscription(iccid, forceDeactivateSim, r0);
    }

    public void updateSubscriptionNickname(int subscriptionId, String nickname, final PendingIntent callbackIntent) {
        if (callerCanWriteEmbeddedSubscriptions()) {
            long token = Binder.clearCallingIdentity();
            try {
                SubscriptionInfo sub = getSubscriptionForSubscriptionId(subscriptionId);
                if (sub == null) {
                    Log.e(TAG, "Cannot update nickname to nonexistent subscription: " + subscriptionId);
                    sendResult(callbackIntent, 2, null);
                    return;
                }
                this.mConnector.updateSubscriptionNickname(sub.getIccId(), nickname, new EuiccConnector.UpdateNicknameCommandCallback() {
                    public void onUpdateNicknameComplete(int result) {
                        int resultCode;
                        Intent extrasIntent = new Intent();
                        if (result != 0) {
                            resultCode = 2;
                            extrasIntent.putExtra("android.telephony.euicc.extra.EMBEDDED_SUBSCRIPTION_DETAILED_CODE", result);
                        } else {
                            resultCode = 0;
                        }
                        EuiccController.this.sendResult(callbackIntent, resultCode, extrasIntent);
                    }

                    public void onEuiccServiceUnavailable() {
                        EuiccController.this.sendResult(callbackIntent, 2, null);
                    }
                });
                Binder.restoreCallingIdentity(token);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        } else {
            throw new SecurityException("Must have WRITE_EMBEDDED_SUBSCRIPTIONS to update nickname");
        }
    }

    public void eraseSubscriptions(final PendingIntent callbackIntent) {
        if (callerCanWriteEmbeddedSubscriptions()) {
            long token = Binder.clearCallingIdentity();
            try {
                this.mConnector.eraseSubscriptions(new EuiccConnector.EraseCommandCallback() {
                    public void onEraseComplete(int result) {
                        Intent extrasIntent = new Intent();
                        if (result != 0) {
                            extrasIntent.putExtra("android.telephony.euicc.extra.EMBEDDED_SUBSCRIPTION_DETAILED_CODE", result);
                            EuiccController.this.sendResult(callbackIntent, 2, extrasIntent);
                            return;
                        }
                        EuiccController.this.refreshSubscriptionsAndSendResult(callbackIntent, 0, extrasIntent);
                    }

                    public void onEuiccServiceUnavailable() {
                        EuiccController.this.sendResult(callbackIntent, 2, null);
                    }
                });
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        } else {
            throw new SecurityException("Must have WRITE_EMBEDDED_SUBSCRIPTIONS to erase subscriptions");
        }
    }

    public void retainSubscriptionsForFactoryReset(final PendingIntent callbackIntent) {
        this.mContext.enforceCallingPermission("android.permission.MASTER_CLEAR", "Must have MASTER_CLEAR to retain subscriptions for factory reset");
        long token = Binder.clearCallingIdentity();
        try {
            this.mConnector.retainSubscriptions(new EuiccConnector.RetainSubscriptionsCommandCallback() {
                public void onRetainSubscriptionsComplete(int result) {
                    int resultCode;
                    Intent extrasIntent = new Intent();
                    if (result != 0) {
                        resultCode = 2;
                        extrasIntent.putExtra("android.telephony.euicc.extra.EMBEDDED_SUBSCRIPTION_DETAILED_CODE", result);
                    } else {
                        resultCode = 0;
                    }
                    EuiccController.this.sendResult(callbackIntent, resultCode, extrasIntent);
                }

                public void onEuiccServiceUnavailable() {
                    EuiccController.this.sendResult(callbackIntent, 2, null);
                }
            });
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    public void refreshSubscriptionsAndSendResult(PendingIntent callbackIntent, int resultCode, Intent extrasIntent) {
        SubscriptionController.getInstance().requestEmbeddedSubscriptionInfoListRefresh(new Runnable(callbackIntent, resultCode, extrasIntent) {
            private final /* synthetic */ PendingIntent f$1;
            private final /* synthetic */ int f$2;
            private final /* synthetic */ Intent f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            public final void run() {
                EuiccController.this.sendResult(this.f$1, this.f$2, this.f$3);
            }
        });
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    public void sendResult(PendingIntent callbackIntent, int resultCode, Intent extrasIntent) {
        try {
            callbackIntent.send(this.mContext, resultCode, extrasIntent);
        } catch (PendingIntent.CanceledException e) {
        }
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    public void addResolutionIntent(Intent extrasIntent, String resolutionAction, String callingPackage, boolean confirmationCodeRetried, EuiccOperation op) {
        Intent intent = new Intent("android.telephony.euicc.action.RESOLVE_ERROR");
        intent.putExtra("android.telephony.euicc.extra.EMBEDDED_SUBSCRIPTION_RESOLUTION_ACTION", resolutionAction);
        intent.putExtra("android.service.euicc.extra.RESOLUTION_CALLING_PACKAGE", callingPackage);
        intent.putExtra("android.service.euicc.extra.RESOLUTION_CONFIRMATION_CODE_RETRIED", confirmationCodeRetried);
        intent.putExtra(EXTRA_OPERATION, op);
        extrasIntent.putExtra("android.telephony.euicc.extra.EMBEDDED_SUBSCRIPTION_RESOLUTION_INTENT", PendingIntent.getActivity(this.mContext, 0, intent, 1073741824));
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.DUMP", "Requires DUMP");
        long token = Binder.clearCallingIdentity();
        try {
            this.mConnector.dump(fd, pw, args);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    public void sendOtaStatusChangedBroadcast() {
        Intent intent = new Intent("android.telephony.euicc.action.OTA_STATUS_CHANGED");
        EuiccConnector euiccConnector = this.mConnector;
        ComponentInfo bestComponent = EuiccConnector.findBestComponent(this.mContext.getPackageManager());
        if (bestComponent != null) {
            intent.setPackage(bestComponent.packageName);
        }
        this.mContext.sendBroadcast(intent, "android.permission.WRITE_EMBEDDED_SUBSCRIPTIONS");
    }

    private SubscriptionInfo getSubscriptionForSubscriptionId(int subscriptionId) {
        List<SubscriptionInfo> subs = this.mSubscriptionManager.getAvailableSubscriptionInfoList();
        int subCount = subs.size();
        for (int i = 0; i < subCount; i++) {
            SubscriptionInfo sub = subs.get(i);
            if (subscriptionId == sub.getSubscriptionId()) {
                return sub;
            }
        }
        return null;
    }

    private String blockingGetEidFromEuiccService() {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<String> eidRef = new AtomicReference<>();
        this.mConnector.getEid(new EuiccConnector.GetEidCommandCallback() {
            public void onGetEidComplete(String eid) {
                eidRef.set(eid);
                latch.countDown();
            }

            public void onEuiccServiceUnavailable() {
                latch.countDown();
            }
        });
        return (String) awaitResult(latch, eidRef);
    }

    private int blockingGetOtaStatusFromEuiccService() {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Integer> statusRef = new AtomicReference<>(5);
        this.mConnector.getOtaStatus(new EuiccConnector.GetOtaStatusCommandCallback() {
            public void onGetOtaStatusComplete(int status) {
                statusRef.set(Integer.valueOf(status));
                latch.countDown();
            }

            public void onEuiccServiceUnavailable() {
                latch.countDown();
            }
        });
        return ((Integer) awaitResult(latch, statusRef)).intValue();
    }

    private EuiccInfo blockingGetEuiccInfoFromEuiccService() {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<EuiccInfo> euiccInfoRef = new AtomicReference<>();
        this.mConnector.getEuiccInfo(new EuiccConnector.GetEuiccInfoCommandCallback() {
            public void onGetEuiccInfoComplete(EuiccInfo euiccInfo) {
                euiccInfoRef.set(euiccInfo);
                latch.countDown();
            }

            public void onEuiccServiceUnavailable() {
                latch.countDown();
            }
        });
        return (EuiccInfo) awaitResult(latch, euiccInfoRef);
    }

    private static <T> T awaitResult(CountDownLatch latch, AtomicReference<T> resultRef) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return resultRef.get();
    }

    /* access modifiers changed from: private */
    public boolean canManageActiveSubscription(String callingPackage) {
        List<SubscriptionInfo> subInfoList = this.mSubscriptionManager.getActiveSubscriptionInfoList();
        if (subInfoList == null) {
            return false;
        }
        int size = subInfoList.size();
        for (int subIndex = 0; subIndex < size; subIndex++) {
            SubscriptionInfo subInfo = subInfoList.get(subIndex);
            if (subInfo.isEmbedded() && this.mSubscriptionManager.canManageSubscription(subInfo, callingPackage)) {
                return true;
            }
        }
        return false;
    }

    private boolean callerCanReadPhoneStatePrivileged() {
        return this.mContext.checkCallingPermission("android.permission.READ_PRIVILEGED_PHONE_STATE") == 0;
    }

    private boolean callerCanWriteEmbeddedSubscriptions() {
        return this.mContext.checkCallingPermission("android.permission.WRITE_EMBEDDED_SUBSCRIPTIONS") == 0;
    }

    private boolean callerHasCarrierPrivilegesForActiveSubscription() {
        return ((TelephonyManager) this.mContext.getSystemService("phone")).hasCarrierPrivileges();
    }
}
