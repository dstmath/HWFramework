package com.android.internal.telephony.euicc;

import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.ServiceManager;
import android.provider.Settings;
import android.service.euicc.DownloadSubscriptionResult;
import android.service.euicc.GetDefaultDownloadableSubscriptionListResult;
import android.service.euicc.GetDownloadableSubscriptionMetadataResult;
import android.service.euicc.GetEuiccProfileInfoListResult;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.UiccAccessRule;
import android.telephony.UiccCardInfo;
import android.telephony.euicc.DownloadableSubscription;
import android.telephony.euicc.EuiccInfo;
import android.text.TextUtils;
import android.util.EventLog;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.euicc.EuiccConnector;
import com.android.internal.telephony.euicc.IEuiccController;
import com.android.internal.telephony.euicc.IHwEuiccManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class EuiccController extends IEuiccController.Stub implements IHwEuiccControllerInner {
    private static final int ERROR = 2;
    private static final int EVENT_TYPE_TAG_ESIM = 1397638484;
    private static final int EVENT_VALUE_ERROR = -1;
    private static final String EVENT_VALUE_ESIM_EID = "159062405";
    private static final String EXTRA_EMBEDDED_SUBSCRIPTION_DOWNLOADABLE_PPRFLAG = "android.telephony.euicc.extra.EXTRA_EMBEDDED_SUBSCRIPTION_DOWNLOADABLE_PPRFLAG";
    private static final String EXTRA_EMBEDDED_SUBSCRIPTION_DOWNLOADABLE_PPRTYPE = "android.telephony.euicc.extra.EXTRA_EMBEDDED_SUBSCRIPTION_DOWNLOADABLE_PPRTYPE";
    private static final String EXTRA_EMBEDDED_SUBSCRIPTION_DOWNLOADABLE_SUBSCRIPTION = "android.telephony.euicc.extra.EMBEDDED_SUBSCRIPTION_DOWNLOADABLE_SUBSCRIPTION";
    @VisibleForTesting
    static final String EXTRA_OPERATION = "operation";
    private static final int OK = 0;
    private static final String RESOLUTION_ACTIVITY_CLASS_NAME = "com.android.phone.euicc.EuiccResolutionUiDispatcherActivity";
    private static final String RESOLUTION_ACTIVITY_PACKAGE_NAME = "com.android.phone";
    private static final int RESOLVABLE_ERROR = 1;
    private static final String TAG = "EuiccController";
    private static EuiccController sInstance;
    private final AppOpsManager mAppOpsManager;
    private final EuiccConnector mConnector;
    private final Context mContext;
    private IHwEuiccControllerEx mEuiccControllerEx;
    private HwInnerEuiccController mHwInnerService;
    private final PackageManager mPackageManager;
    private final SubscriptionManager mSubscriptionManager;
    private final TelephonyManager mTelephonyManager;

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

    /* JADX DEBUG: Multi-variable search result rejected for r1v0, resolved type: com.android.internal.telephony.euicc.EuiccController */
    /* JADX WARN: Multi-variable type inference failed */
    private EuiccController(Context context) {
        this(context, new EuiccConnector(context));
        ServiceManager.addService("econtroller", this);
    }

    @VisibleForTesting
    public EuiccController(Context context, EuiccConnector connector) {
        this.mHwInnerService = new HwInnerEuiccController(this);
        this.mContext = context;
        this.mConnector = connector;
        this.mSubscriptionManager = (SubscriptionManager) context.getSystemService("telephony_subscription_service");
        this.mTelephonyManager = (TelephonyManager) context.getSystemService("phone");
        this.mAppOpsManager = (AppOpsManager) context.getSystemService("appops");
        this.mPackageManager = context.getPackageManager();
        this.mEuiccControllerEx = HwTelephonyFactory.getHwEuiccControllerEx(context, this);
    }

    public void continueOperation(int cardId, Intent resolutionIntent, Bundle resolutionExtras) {
        if (callerCanWriteEmbeddedSubscriptions()) {
            long token = Binder.clearCallingIdentity();
            try {
                EuiccOperation op = (EuiccOperation) resolutionIntent.getParcelableExtra(EXTRA_OPERATION);
                if (op != null) {
                    PendingIntent callbackIntent = (PendingIntent) resolutionIntent.getParcelableExtra("android.telephony.euicc.extra.EMBEDDED_SUBSCRIPTION_RESOLUTION_CALLBACK_INTENT");
                    if (callbackIntent != null) {
                        op.continueOperation(cardId, resolutionExtras, callbackIntent);
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

    public String getEid(int cardId, String callingPackage) {
        boolean callerCanReadPhoneStatePrivileged = callerCanReadPhoneStatePrivileged();
        try {
            this.mAppOpsManager.checkPackage(Binder.getCallingUid(), callingPackage);
            long token = Binder.clearCallingIdentity();
            if (!callerCanReadPhoneStatePrivileged) {
                try {
                    if (!canManageSubscriptionOnTargetSim(cardId, callingPackage)) {
                        throw new SecurityException("Must have carrier privileges on subscription to read EID for cardId=" + cardId);
                    }
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(token);
                    throw th;
                }
            }
            String blockingGetEidFromEuiccService = blockingGetEidFromEuiccService(cardId);
            Binder.restoreCallingIdentity(token);
            return blockingGetEidFromEuiccService;
        } catch (SecurityException e) {
            EventLog.writeEvent((int) EVENT_TYPE_TAG_ESIM, EVENT_VALUE_ESIM_EID, -1, "Missing UID checking");
            throw e;
        }
    }

    public int getOtaStatus(int cardId) {
        if (callerCanWriteEmbeddedSubscriptions()) {
            long token = Binder.clearCallingIdentity();
            try {
                return blockingGetOtaStatusFromEuiccService(cardId);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        } else {
            throw new SecurityException("Must have WRITE_EMBEDDED_SUBSCRIPTIONS to get OTA status");
        }
    }

    @Override // com.android.internal.telephony.euicc.IHwEuiccControllerInner
    public void startOtaUpdatingIfNecessary() {
        startOtaUpdatingIfNecessary(this.mTelephonyManager.getCardIdForDefaultEuicc());
    }

    @Override // com.android.internal.telephony.euicc.IHwEuiccControllerInner
    public void startOtaUpdatingIfNecessary(int cardId) {
        this.mConnector.startOtaIfNecessary(cardId, new EuiccConnector.OtaStatusChangedCallback() {
            /* class com.android.internal.telephony.euicc.EuiccController.AnonymousClass1 */

            @Override // com.android.internal.telephony.euicc.EuiccConnector.OtaStatusChangedCallback
            public void onOtaStatusChanged(int status) {
                EuiccController.this.sendOtaStatusChangedBroadcast();
                EuiccController.this.mEuiccControllerEx.processOtaStatusChanged(status);
            }

            @Override // com.android.internal.telephony.euicc.EuiccConnector.BaseEuiccCommandCallback
            public void onEuiccServiceUnavailable() {
                EuiccController.this.mEuiccControllerEx.processEuiccServiceUnavailable();
            }
        });
    }

    public void getDownloadableSubscriptionMetadata(int cardId, DownloadableSubscription subscription, String callingPackage, PendingIntent callbackIntent) {
        getDownloadableSubscriptionMetadata(cardId, subscription, false, callingPackage, callbackIntent);
    }

    /* access modifiers changed from: package-private */
    public void getDownloadableSubscriptionMetadata(int cardId, DownloadableSubscription subscription, boolean forceDeactivateSim, String callingPackage, PendingIntent callbackIntent) {
        Throwable th;
        if (callerCanWriteEmbeddedSubscriptions()) {
            this.mAppOpsManager.checkPackage(Binder.getCallingUid(), callingPackage);
            long token = Binder.clearCallingIdentity();
            try {
                try {
                    this.mConnector.getDownloadableSubscriptionMetadata(cardId, subscription, forceDeactivateSim, new GetMetadataCommandCallback(token, subscription, callingPackage, callbackIntent));
                    Binder.restoreCallingIdentity(token);
                } catch (Throwable th2) {
                    th = th2;
                    Binder.restoreCallingIdentity(token);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                Binder.restoreCallingIdentity(token);
                throw th;
            }
        } else {
            throw new SecurityException("Must have WRITE_EMBEDDED_SUBSCRIPTIONS to get metadata");
        }
    }

    /* access modifiers changed from: package-private */
    public class GetMetadataCommandCallback implements EuiccConnector.GetMetadataCommandCallback {
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

        @Override // com.android.internal.telephony.euicc.EuiccConnector.GetMetadataCommandCallback
        public void onGetMetadataComplete(int cardId, GetDownloadableSubscriptionMetadataResult result) {
            int resultCode;
            Intent extrasIntent = new Intent();
            int result2 = result.getResult();
            if (result2 == -2) {
                resultCode = 1;
                DownloadableSubscription subscription = result.getDownloadableSubscription();
                int resolvableErrors = result.getResolvableErrors();
                if ((resolvableErrors & 2) != 0) {
                    extrasIntent.putExtra(EuiccController.EXTRA_EMBEDDED_SUBSCRIPTION_DOWNLOADABLE_PPRTYPE, result.getPprType());
                    extrasIntent.putExtra(EuiccController.EXTRA_EMBEDDED_SUBSCRIPTION_DOWNLOADABLE_PPRFLAG, result.getPprFlag());
                }
                extrasIntent.putExtra("android.service.euicc.extra.RESOLUTION_CONFIRMATION_CODE_RETRIED", (resolvableErrors & 1) != 0);
                extrasIntent.putExtra("android.service.euicc.extra.RESOLVABLE_ERRORS", resolvableErrors);
                extrasIntent.putExtra(EuiccController.EXTRA_EMBEDDED_SUBSCRIPTION_DOWNLOADABLE_SUBSCRIPTION, subscription);
            } else if (result2 == -1) {
                EuiccController.this.addResolutionIntent(extrasIntent, "android.service.euicc.action.RESOLVE_DEACTIVATE_SIM", this.mCallingPackage, 0, false, getOperationForDeactivateSim(), cardId);
                resultCode = 1;
            } else if (result2 != 0) {
                resultCode = 2;
                extrasIntent.putExtra("android.telephony.euicc.extra.EMBEDDED_SUBSCRIPTION_DETAILED_CODE", result.getResult());
            } else {
                resultCode = 0;
                extrasIntent.putExtra(EuiccController.EXTRA_EMBEDDED_SUBSCRIPTION_DOWNLOADABLE_SUBSCRIPTION, result.getDownloadableSubscription());
                EuiccController.this.mEuiccControllerEx.putIccidByDownloadableSubscription(this.mCallbackIntent, extrasIntent, result.getDownloadableSubscription());
            }
            EuiccController.this.lambda$refreshSubscriptionsAndSendResult$0$EuiccController(this.mCallbackIntent, resultCode, extrasIntent);
        }

        @Override // com.android.internal.telephony.euicc.EuiccConnector.BaseEuiccCommandCallback
        public void onEuiccServiceUnavailable() {
            EuiccController.this.lambda$refreshSubscriptionsAndSendResult$0$EuiccController(this.mCallbackIntent, 2, null);
        }

        /* access modifiers changed from: protected */
        public EuiccOperation getOperationForDeactivateSim() {
            return EuiccOperation.forGetMetadataDeactivateSim(this.mCallingToken, this.mSubscription, this.mCallingPackage);
        }
    }

    public void downloadSubscription(int cardId, DownloadableSubscription subscription, boolean switchAfterDownload, String callingPackage, Bundle resolvedBundle, PendingIntent callbackIntent) {
        downloadSubscription(cardId, subscription, switchAfterDownload, callingPackage, false, resolvedBundle, callbackIntent);
    }

    /* access modifiers changed from: package-private */
    public void downloadSubscription(int cardId, DownloadableSubscription subscription, boolean switchAfterDownload, String callingPackage, boolean forceDeactivateSim, Bundle resolvedBundle, PendingIntent callbackIntent) {
        long token;
        Throwable th;
        EuiccOperation forDownloadNoPrivilegesOrDeactivateSimCheckMetadata;
        boolean callerCanWriteEmbeddedSubscriptions = callerCanWriteEmbeddedSubscriptions();
        this.mAppOpsManager.checkPackage(Binder.getCallingUid(), callingPackage);
        long token2 = Binder.clearCallingIdentity();
        if (callerCanWriteEmbeddedSubscriptions) {
            token = token2;
            try {
                downloadSubscriptionPrivileged(cardId, token2, subscription, switchAfterDownload, forceDeactivateSim, callingPackage, resolvedBundle, callbackIntent);
                Binder.restoreCallingIdentity(token);
            } catch (Throwable th2) {
                th = th2;
                Binder.restoreCallingIdentity(token);
                throw th;
            }
        } else {
            token = token2;
            try {
                if (canManageSubscriptionOnTargetSim(cardId, callingPackage)) {
                    try {
                    } catch (Throwable th3) {
                        th = th3;
                        Binder.restoreCallingIdentity(token);
                        throw th;
                    }
                    try {
                        this.mConnector.getDownloadableSubscriptionMetadata(cardId, subscription, forceDeactivateSim, new DownloadSubscriptionGetMetadataCommandCallback(token, subscription, switchAfterDownload, callingPackage, forceDeactivateSim, callbackIntent, false));
                    } catch (Throwable th4) {
                        th = th4;
                        Binder.restoreCallingIdentity(token);
                        throw th;
                    }
                } else {
                    Log.i(TAG, "Caller can't manage subscription on target SIM. Ask user's consent first");
                    Intent extrasIntent = new Intent();
                    try {
                        forDownloadNoPrivilegesOrDeactivateSimCheckMetadata = EuiccOperation.forDownloadNoPrivilegesOrDeactivateSimCheckMetadata(token, subscription, switchAfterDownload, callingPackage);
                        token = token;
                    } catch (Throwable th5) {
                        th = th5;
                        token = token;
                        Binder.restoreCallingIdentity(token);
                        throw th;
                    }
                    try {
                        addResolutionIntent(extrasIntent, "android.service.euicc.action.RESOLVE_NO_PRIVILEGES", callingPackage, 0, false, forDownloadNoPrivilegesOrDeactivateSimCheckMetadata, cardId);
                        try {
                            lambda$refreshSubscriptionsAndSendResult$0$EuiccController(callbackIntent, 1, extrasIntent);
                        } catch (Throwable th6) {
                            th = th6;
                            Binder.restoreCallingIdentity(token);
                            throw th;
                        }
                    } catch (Throwable th7) {
                        th = th7;
                        Binder.restoreCallingIdentity(token);
                        throw th;
                    }
                }
                Binder.restoreCallingIdentity(token);
            } catch (Throwable th8) {
                th = th8;
                Binder.restoreCallingIdentity(token);
                throw th;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class DownloadSubscriptionGetMetadataCommandCallback extends GetMetadataCommandCallback {
        private final boolean mForceDeactivateSim;
        private final boolean mSwitchAfterDownload;
        private final boolean mWithUserConsent;

        DownloadSubscriptionGetMetadataCommandCallback(long callingToken, DownloadableSubscription subscription, boolean switchAfterDownload, String callingPackage, boolean forceDeactivateSim, PendingIntent callbackIntent, boolean withUserConsent) {
            super(callingToken, subscription, callingPackage, callbackIntent);
            this.mSwitchAfterDownload = switchAfterDownload;
            this.mForceDeactivateSim = forceDeactivateSim;
            this.mWithUserConsent = withUserConsent;
        }

        @Override // com.android.internal.telephony.euicc.EuiccController.GetMetadataCommandCallback, com.android.internal.telephony.euicc.EuiccConnector.GetMetadataCommandCallback
        public void onGetMetadataComplete(int cardId, GetDownloadableSubscriptionMetadataResult result) {
            DownloadableSubscription subscription = result.getDownloadableSubscription();
            if (this.mWithUserConsent) {
                if (result.getResult() != 0) {
                    super.onGetMetadataComplete(cardId, result);
                } else if (EuiccController.this.checkCarrierPrivilegeInMetadata(subscription, this.mCallingPackage)) {
                    EuiccController.this.downloadSubscriptionPrivileged(cardId, this.mCallingToken, subscription, this.mSwitchAfterDownload, this.mForceDeactivateSim, this.mCallingPackage, null, this.mCallbackIntent);
                } else {
                    Log.e(EuiccController.TAG, "Caller does not have carrier privilege in metadata.");
                    EuiccController.this.lambda$refreshSubscriptionsAndSendResult$0$EuiccController(this.mCallbackIntent, 2, null);
                }
            } else if (result.getResult() == -1) {
                Intent extrasIntent = new Intent();
                EuiccController.this.addResolutionIntent(extrasIntent, "android.service.euicc.action.RESOLVE_DEACTIVATE_SIM", this.mCallingPackage, 0, false, EuiccOperation.forDownloadNoPrivilegesOrDeactivateSimCheckMetadata(this.mCallingToken, this.mSubscription, this.mSwitchAfterDownload, this.mCallingPackage), cardId);
                EuiccController.this.lambda$refreshSubscriptionsAndSendResult$0$EuiccController(this.mCallbackIntent, 1, extrasIntent);
            } else if (result.getResult() != 0) {
                super.onGetMetadataComplete(cardId, result);
            } else if (EuiccController.this.checkCarrierPrivilegeInMetadata(subscription, this.mCallingPackage)) {
                EuiccController.this.downloadSubscriptionPrivileged(cardId, this.mCallingToken, subscription, this.mSwitchAfterDownload, this.mForceDeactivateSim, this.mCallingPackage, null, this.mCallbackIntent);
            } else {
                Log.e(EuiccController.TAG, "Caller is not permitted to download this profile per metadata");
                EuiccController.this.lambda$refreshSubscriptionsAndSendResult$0$EuiccController(this.mCallbackIntent, 2, null);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void downloadSubscriptionPrivilegedCheckMetadata(int cardId, long callingToken, DownloadableSubscription subscription, boolean switchAfterDownload, boolean forceDeactivateSim, String callingPackage, Bundle resolvedBundle, PendingIntent callbackIntent) {
        this.mConnector.getDownloadableSubscriptionMetadata(cardId, subscription, forceDeactivateSim, new DownloadSubscriptionGetMetadataCommandCallback(callingToken, subscription, switchAfterDownload, callingPackage, forceDeactivateSim, callbackIntent, true));
    }

    /* access modifiers changed from: package-private */
    public void downloadSubscriptionPrivileged(final int cardId, final long callingToken, final DownloadableSubscription subscription, final boolean switchAfterDownload, boolean forceDeactivateSim, final String callingPackage, Bundle resolvedBundle, final PendingIntent callbackIntent) {
        this.mConnector.downloadSubscription(cardId, subscription, switchAfterDownload, forceDeactivateSim, resolvedBundle, new EuiccConnector.DownloadCommandCallback() {
            /* class com.android.internal.telephony.euicc.EuiccController.AnonymousClass2 */

            @Override // com.android.internal.telephony.euicc.EuiccConnector.DownloadCommandCallback
            public void onDownloadComplete(DownloadSubscriptionResult result) {
                int resultCode;
                boolean retried;
                Intent extrasIntent = new Intent();
                int result2 = result.getResult();
                if (result2 == -2) {
                    if (!TextUtils.isEmpty(subscription.getConfirmationCode())) {
                        retried = true;
                    } else {
                        retried = false;
                    }
                    if (result.getResolvableErrors() != 0) {
                        EuiccController.this.addResolutionIntent(extrasIntent, "android.service.euicc.action.RESOLVE_RESOLVABLE_ERRORS", callingPackage, result.getResolvableErrors(), retried, EuiccOperation.forDownloadResolvableErrors(callingToken, subscription, switchAfterDownload, callingPackage, result.getResolvableErrors()), cardId);
                    } else {
                        EuiccController euiccController = EuiccController.this;
                        String str = callingPackage;
                        euiccController.addResolutionIntent(extrasIntent, "android.service.euicc.action.RESOLVE_CONFIRMATION_CODE", str, 0, retried, EuiccOperation.forDownloadConfirmationCode(callingToken, subscription, switchAfterDownload, str), cardId);
                    }
                    resultCode = 1;
                } else if (result2 == -1) {
                    EuiccController euiccController2 = EuiccController.this;
                    String str2 = callingPackage;
                    euiccController2.addResolutionIntent(extrasIntent, "android.service.euicc.action.RESOLVE_DEACTIVATE_SIM", str2, 0, false, EuiccOperation.forDownloadDeactivateSim(callingToken, subscription, switchAfterDownload, str2), cardId);
                    resultCode = 1;
                } else if (result2 != 0) {
                    resultCode = 2;
                    extrasIntent.putExtra("android.telephony.euicc.extra.EMBEDDED_SUBSCRIPTION_DETAILED_CODE", result.getResult());
                } else {
                    resultCode = 0;
                    Settings.Global.putInt(EuiccController.this.mContext.getContentResolver(), "euicc_provisioned", 1);
                    extrasIntent.putExtra(EuiccController.EXTRA_EMBEDDED_SUBSCRIPTION_DOWNLOADABLE_SUBSCRIPTION, subscription);
                    if (!switchAfterDownload) {
                        EuiccController.this.refreshSubscriptionsAndSendResult(callbackIntent, 0, extrasIntent);
                        return;
                    }
                }
                EuiccController.this.lambda$refreshSubscriptionsAndSendResult$0$EuiccController(callbackIntent, resultCode, extrasIntent);
            }

            @Override // com.android.internal.telephony.euicc.EuiccConnector.BaseEuiccCommandCallback
            public void onEuiccServiceUnavailable() {
                EuiccController.this.lambda$refreshSubscriptionsAndSendResult$0$EuiccController(callbackIntent, 2, null);
            }
        });
    }

    public GetEuiccProfileInfoListResult blockingGetEuiccProfileInfoList(int cardId) {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<GetEuiccProfileInfoListResult> resultRef = new AtomicReference<>();
        this.mConnector.getEuiccProfileInfoList(cardId, new EuiccConnector.GetEuiccProfileInfoListCommandCallback() {
            /* class com.android.internal.telephony.euicc.EuiccController.AnonymousClass3 */

            @Override // com.android.internal.telephony.euicc.EuiccConnector.GetEuiccProfileInfoListCommandCallback
            public void onListComplete(GetEuiccProfileInfoListResult result) {
                resultRef.set(result);
                latch.countDown();
            }

            @Override // com.android.internal.telephony.euicc.EuiccConnector.BaseEuiccCommandCallback
            public void onEuiccServiceUnavailable() {
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            Log.e(TAG, "blockingGetEuiccInfoFromEuiccService got InterruptedException e: " + e);
            Thread.currentThread().interrupt();
        }
        return (GetEuiccProfileInfoListResult) resultRef.get();
    }

    public void getDefaultDownloadableSubscriptionList(int cardId, String callingPackage, PendingIntent callbackIntent) {
        getDefaultDownloadableSubscriptionList(cardId, false, callingPackage, callbackIntent);
    }

    /* access modifiers changed from: package-private */
    public void getDefaultDownloadableSubscriptionList(int cardId, boolean forceDeactivateSim, String callingPackage, PendingIntent callbackIntent) {
        if (callerCanWriteEmbeddedSubscriptions()) {
            this.mAppOpsManager.checkPackage(Binder.getCallingUid(), callingPackage);
            long token = Binder.clearCallingIdentity();
            try {
                this.mConnector.getDefaultDownloadableSubscriptionList(cardId, forceDeactivateSim, new GetDefaultListCommandCallback(token, callingPackage, callbackIntent));
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        } else {
            throw new SecurityException("Must have WRITE_EMBEDDED_SUBSCRIPTIONS to get default list");
        }
    }

    /* access modifiers changed from: package-private */
    public class GetDefaultListCommandCallback implements EuiccConnector.GetDefaultListCommandCallback {
        final PendingIntent mCallbackIntent;
        final String mCallingPackage;
        final long mCallingToken;

        GetDefaultListCommandCallback(long callingToken, String callingPackage, PendingIntent callbackIntent) {
            this.mCallingToken = callingToken;
            this.mCallingPackage = callingPackage;
            this.mCallbackIntent = callbackIntent;
        }

        @Override // com.android.internal.telephony.euicc.EuiccConnector.GetDefaultListCommandCallback
        public void onGetDefaultListComplete(int cardId, GetDefaultDownloadableSubscriptionListResult result) {
            int resultCode;
            Intent extrasIntent = new Intent();
            int result2 = result.getResult();
            if (result2 == -1) {
                EuiccController euiccController = EuiccController.this;
                String str = this.mCallingPackage;
                euiccController.addResolutionIntent(extrasIntent, "android.service.euicc.action.RESOLVE_DEACTIVATE_SIM", str, 0, false, EuiccOperation.forGetDefaultListDeactivateSim(this.mCallingToken, str), cardId);
                resultCode = 1;
            } else if (result2 != 0) {
                resultCode = 2;
                extrasIntent.putExtra("android.telephony.euicc.extra.EMBEDDED_SUBSCRIPTION_DETAILED_CODE", result.getResult());
            } else {
                resultCode = 0;
                List<DownloadableSubscription> list = result.getDownloadableSubscriptions();
                if (list != null && list.size() > 0) {
                    extrasIntent.putExtra("android.telephony.euicc.extra.EMBEDDED_SUBSCRIPTION_DOWNLOADABLE_SUBSCRIPTIONS", (Parcelable[]) list.toArray(new DownloadableSubscription[list.size()]));
                }
            }
            EuiccController.this.lambda$refreshSubscriptionsAndSendResult$0$EuiccController(this.mCallbackIntent, resultCode, extrasIntent);
        }

        @Override // com.android.internal.telephony.euicc.EuiccConnector.BaseEuiccCommandCallback
        public void onEuiccServiceUnavailable() {
            EuiccController.this.lambda$refreshSubscriptionsAndSendResult$0$EuiccController(this.mCallbackIntent, 2, null);
        }
    }

    public EuiccInfo getEuiccInfo(int cardId) {
        long token = Binder.clearCallingIdentity();
        try {
            return blockingGetEuiccInfoFromEuiccService(cardId);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public void deleteSubscription(int cardId, int subscriptionId, String callingPackage, PendingIntent callbackIntent) {
        boolean callerCanWriteEmbeddedSubscriptions = callerCanWriteEmbeddedSubscriptions();
        this.mAppOpsManager.checkPackage(Binder.getCallingUid(), callingPackage);
        long token = Binder.clearCallingIdentity();
        try {
            SubscriptionInfo sub = getSubscriptionForSubscriptionId(subscriptionId);
            if (sub == null) {
                Log.e(TAG, "Cannot delete nonexistent subscription: " + subscriptionId);
                lambda$refreshSubscriptionsAndSendResult$0$EuiccController(callbackIntent, 2, null);
            } else if (callerCanWriteEmbeddedSubscriptions || this.mSubscriptionManager.canManageSubscription(sub, callingPackage)) {
                deleteSubscriptionPrivileged(cardId, sub.getIccId(), callbackIntent);
                Binder.restoreCallingIdentity(token);
            } else {
                Log.e(TAG, "No permissions: " + subscriptionId);
                lambda$refreshSubscriptionsAndSendResult$0$EuiccController(callbackIntent, 2, null);
                Binder.restoreCallingIdentity(token);
            }
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    /* access modifiers changed from: package-private */
    public void deleteSubscriptionPrivileged(int cardId, String iccid, final PendingIntent callbackIntent) {
        this.mConnector.deleteSubscription(cardId, iccid, new EuiccConnector.DeleteCommandCallback() {
            /* class com.android.internal.telephony.euicc.EuiccController.AnonymousClass4 */

            @Override // com.android.internal.telephony.euicc.EuiccConnector.DeleteCommandCallback
            public void onDeleteComplete(int result) {
                Intent extrasIntent = new Intent();
                if (result != 0) {
                    extrasIntent.putExtra("android.telephony.euicc.extra.EMBEDDED_SUBSCRIPTION_DETAILED_CODE", result);
                    EuiccController.this.lambda$refreshSubscriptionsAndSendResult$0$EuiccController(callbackIntent, 2, extrasIntent);
                    return;
                }
                EuiccController.this.refreshSubscriptionsAndSendResult(callbackIntent, 0, extrasIntent);
            }

            @Override // com.android.internal.telephony.euicc.EuiccConnector.BaseEuiccCommandCallback
            public void onEuiccServiceUnavailable() {
                EuiccController.this.lambda$refreshSubscriptionsAndSendResult$0$EuiccController(callbackIntent, 2, null);
            }
        });
    }

    public void switchToSubscription(int cardId, int subscriptionId, String callingPackage, PendingIntent callbackIntent) {
        switchToSubscription(cardId, subscriptionId, false, callingPackage, callbackIntent);
    }

    /* access modifiers changed from: package-private */
    public void switchToSubscription(int cardId, int subscriptionId, boolean forceDeactivateSim, String callingPackage, PendingIntent callbackIntent) {
        boolean forceDeactivateSim2;
        long token;
        Throwable th;
        String iccid;
        boolean callerCanWriteEmbeddedSubscriptions = callerCanWriteEmbeddedSubscriptions();
        this.mAppOpsManager.checkPackage(Binder.getCallingUid(), callingPackage);
        long token2 = Binder.clearCallingIdentity();
        if (callerCanWriteEmbeddedSubscriptions) {
            forceDeactivateSim2 = true;
        } else {
            forceDeactivateSim2 = forceDeactivateSim;
        }
        boolean passConsent = false;
        if (subscriptionId == -1) {
            if (!callerCanWriteEmbeddedSubscriptions) {
                try {
                    if (!canManageActiveSubscriptionOnTargetSim(cardId, callingPackage)) {
                        Log.e(TAG, "Not permitted to switch to empty subscription");
                        lambda$refreshSubscriptionsAndSendResult$0$EuiccController(callbackIntent, 2, null);
                        Binder.restoreCallingIdentity(token2);
                        return;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    token = token2;
                    Binder.restoreCallingIdentity(token);
                    throw th;
                }
            }
            passConsent = true;
            iccid = null;
        } else {
            try {
                SubscriptionInfo sub = getSubscriptionForSubscriptionId(subscriptionId);
                if (sub == null) {
                    Log.e(TAG, "Cannot switch to nonexistent sub: " + subscriptionId);
                    lambda$refreshSubscriptionsAndSendResult$0$EuiccController(callbackIntent, 2, null);
                    Binder.restoreCallingIdentity(token2);
                    return;
                }
                if (callerCanWriteEmbeddedSubscriptions) {
                    passConsent = true;
                } else if (!this.mSubscriptionManager.canManageSubscription(sub, callingPackage)) {
                    Log.e(TAG, "Not permitted to switch to sub: " + subscriptionId);
                    lambda$refreshSubscriptionsAndSendResult$0$EuiccController(callbackIntent, 2, null);
                    Binder.restoreCallingIdentity(token2);
                    return;
                } else if (canManageSubscriptionOnTargetSim(cardId, callingPackage)) {
                    passConsent = true;
                }
                iccid = sub.getIccId();
            } catch (Throwable th3) {
                th = th3;
                token = token2;
                Binder.restoreCallingIdentity(token);
                throw th;
            }
        }
        if (!passConsent) {
            Intent extrasIntent = new Intent();
            token = token2;
            try {
                addResolutionIntent(extrasIntent, "android.service.euicc.action.RESOLVE_NO_PRIVILEGES", callingPackage, 0, false, EuiccOperation.forSwitchNoPrivileges(token2, subscriptionId, callingPackage), cardId);
                lambda$refreshSubscriptionsAndSendResult$0$EuiccController(callbackIntent, 1, extrasIntent);
                Binder.restoreCallingIdentity(token);
            } catch (Throwable th4) {
                th = th4;
                Binder.restoreCallingIdentity(token);
                throw th;
            }
        } else {
            switchToSubscriptionPrivileged(cardId, token2, subscriptionId, iccid, forceDeactivateSim2, callingPackage, callbackIntent);
            Binder.restoreCallingIdentity(token2);
        }
    }

    /* access modifiers changed from: package-private */
    public void switchToSubscriptionPrivileged(int cardId, long callingToken, int subscriptionId, boolean forceDeactivateSim, String callingPackage, PendingIntent callbackIntent) {
        String iccid = null;
        SubscriptionInfo sub = getSubscriptionForSubscriptionId(subscriptionId);
        if (sub != null) {
            iccid = sub.getIccId();
        }
        switchToSubscriptionPrivileged(cardId, callingToken, subscriptionId, iccid, forceDeactivateSim, callingPackage, callbackIntent);
    }

    /* access modifiers changed from: package-private */
    public void switchToSubscriptionPrivileged(final int cardId, final long callingToken, final int subscriptionId, String iccid, boolean forceDeactivateSim, final String callingPackage, final PendingIntent callbackIntent) {
        this.mConnector.switchToSubscription(cardId, iccid, forceDeactivateSim, new EuiccConnector.SwitchCommandCallback() {
            /* class com.android.internal.telephony.euicc.EuiccController.AnonymousClass5 */

            @Override // com.android.internal.telephony.euicc.EuiccConnector.SwitchCommandCallback
            public void onSwitchComplete(int result) {
                int resultCode;
                Intent extrasIntent = new Intent();
                if (result == -1) {
                    EuiccController euiccController = EuiccController.this;
                    String str = callingPackage;
                    euiccController.addResolutionIntent(extrasIntent, "android.service.euicc.action.RESOLVE_DEACTIVATE_SIM", str, 0, false, EuiccOperation.forSwitchDeactivateSim(callingToken, subscriptionId, str), cardId);
                    resultCode = 1;
                } else if (result != 0) {
                    resultCode = 2;
                    extrasIntent.putExtra("android.telephony.euicc.extra.EMBEDDED_SUBSCRIPTION_DETAILED_CODE", result);
                } else {
                    resultCode = 0;
                }
                EuiccController.this.lambda$refreshSubscriptionsAndSendResult$0$EuiccController(callbackIntent, resultCode, extrasIntent);
            }

            @Override // com.android.internal.telephony.euicc.EuiccConnector.BaseEuiccCommandCallback
            public void onEuiccServiceUnavailable() {
                EuiccController.this.lambda$refreshSubscriptionsAndSendResult$0$EuiccController(callbackIntent, 2, null);
            }
        });
    }

    public void updateSubscriptionNickname(int cardId, int subscriptionId, String nickname, String callingPackage, final PendingIntent callbackIntent) {
        boolean callerCanWriteEmbeddedSubscriptions = callerCanWriteEmbeddedSubscriptions();
        this.mAppOpsManager.checkPackage(Binder.getCallingUid(), callingPackage);
        long token = Binder.clearCallingIdentity();
        try {
            SubscriptionInfo sub = getSubscriptionForSubscriptionId(subscriptionId);
            if (sub == null) {
                Log.e(TAG, "Cannot update nickname to nonexistent sub: " + subscriptionId);
                lambda$refreshSubscriptionsAndSendResult$0$EuiccController(callbackIntent, 2, null);
            } else if (callerCanWriteEmbeddedSubscriptions || this.mSubscriptionManager.canManageSubscription(sub, callingPackage)) {
                this.mConnector.updateSubscriptionNickname(cardId, sub.getIccId(), nickname, new EuiccConnector.UpdateNicknameCommandCallback() {
                    /* class com.android.internal.telephony.euicc.EuiccController.AnonymousClass6 */

                    @Override // com.android.internal.telephony.euicc.EuiccConnector.UpdateNicknameCommandCallback
                    public void onUpdateNicknameComplete(int result) {
                        Intent extrasIntent = new Intent();
                        if (result != 0) {
                            extrasIntent.putExtra("android.telephony.euicc.extra.EMBEDDED_SUBSCRIPTION_DETAILED_CODE", result);
                            EuiccController.this.lambda$refreshSubscriptionsAndSendResult$0$EuiccController(callbackIntent, 2, extrasIntent);
                            return;
                        }
                        EuiccController.this.refreshSubscriptionsAndSendResult(callbackIntent, 0, extrasIntent);
                    }

                    @Override // com.android.internal.telephony.euicc.EuiccConnector.BaseEuiccCommandCallback
                    public void onEuiccServiceUnavailable() {
                        EuiccController.this.lambda$refreshSubscriptionsAndSendResult$0$EuiccController(callbackIntent, 2, null);
                    }
                });
                Binder.restoreCallingIdentity(token);
            } else {
                Log.e(TAG, "No permissions: " + subscriptionId);
                lambda$refreshSubscriptionsAndSendResult$0$EuiccController(callbackIntent, 2, null);
                Binder.restoreCallingIdentity(token);
            }
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public void eraseSubscriptions(int cardId, final PendingIntent callbackIntent) {
        if (callerCanWriteEmbeddedSubscriptions()) {
            long token = Binder.clearCallingIdentity();
            try {
                this.mConnector.eraseSubscriptions(cardId, new EuiccConnector.EraseCommandCallback() {
                    /* class com.android.internal.telephony.euicc.EuiccController.AnonymousClass7 */

                    @Override // com.android.internal.telephony.euicc.EuiccConnector.EraseCommandCallback
                    public void onEraseComplete(int result) {
                        Intent extrasIntent = new Intent();
                        if (result != 0) {
                            extrasIntent.putExtra("android.telephony.euicc.extra.EMBEDDED_SUBSCRIPTION_DETAILED_CODE", result);
                            EuiccController.this.lambda$refreshSubscriptionsAndSendResult$0$EuiccController(callbackIntent, 2, extrasIntent);
                            return;
                        }
                        EuiccController.this.refreshSubscriptionsAndSendResult(callbackIntent, 0, extrasIntent);
                    }

                    @Override // com.android.internal.telephony.euicc.EuiccConnector.BaseEuiccCommandCallback
                    public void onEuiccServiceUnavailable() {
                        EuiccController.this.lambda$refreshSubscriptionsAndSendResult$0$EuiccController(callbackIntent, 2, null);
                    }
                });
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        } else {
            throw new SecurityException("Must have WRITE_EMBEDDED_SUBSCRIPTIONS to erase subscriptions");
        }
    }

    public void retainSubscriptionsForFactoryReset(int cardId, final PendingIntent callbackIntent) {
        this.mContext.enforceCallingPermission("android.permission.MASTER_CLEAR", "Must have MASTER_CLEAR to retain subscriptions for factory reset");
        long token = Binder.clearCallingIdentity();
        try {
            this.mConnector.retainSubscriptions(cardId, new EuiccConnector.RetainSubscriptionsCommandCallback() {
                /* class com.android.internal.telephony.euicc.EuiccController.AnonymousClass8 */

                @Override // com.android.internal.telephony.euicc.EuiccConnector.RetainSubscriptionsCommandCallback
                public void onRetainSubscriptionsComplete(int result) {
                    int resultCode;
                    Intent extrasIntent = new Intent();
                    if (result != 0) {
                        resultCode = 2;
                        extrasIntent.putExtra("android.telephony.euicc.extra.EMBEDDED_SUBSCRIPTION_DETAILED_CODE", result);
                    } else {
                        resultCode = 0;
                    }
                    EuiccController.this.lambda$refreshSubscriptionsAndSendResult$0$EuiccController(callbackIntent, resultCode, extrasIntent);
                }

                @Override // com.android.internal.telephony.euicc.EuiccConnector.BaseEuiccCommandCallback
                public void onEuiccServiceUnavailable() {
                    EuiccController.this.lambda$refreshSubscriptionsAndSendResult$0$EuiccController(callbackIntent, 2, null);
                }
            });
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    public void refreshSubscriptionsAndSendResult(PendingIntent callbackIntent, int resultCode, Intent extrasIntent) {
        SubscriptionController.getInstance().requestEmbeddedSubscriptionInfoListRefresh(new Runnable(callbackIntent, resultCode, extrasIntent) {
            /* class com.android.internal.telephony.euicc.$$Lambda$EuiccController$aZ8yEHh32lS1TctCOFmVEa57ekc */
            private final /* synthetic */ PendingIntent f$1;
            private final /* synthetic */ int f$2;
            private final /* synthetic */ Intent f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            @Override // java.lang.Runnable
            public final void run() {
                EuiccController.this.lambda$refreshSubscriptionsAndSendResult$0$EuiccController(this.f$1, this.f$2, this.f$3);
            }
        });
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    /* renamed from: sendResult */
    public void lambda$refreshSubscriptionsAndSendResult$0$EuiccController(PendingIntent callbackIntent, int resultCode, Intent extrasIntent) {
        try {
            this.mEuiccControllerEx.putSubIdForVsim(callbackIntent, extrasIntent);
            callbackIntent.send(this.mContext, resultCode, extrasIntent);
        } catch (PendingIntent.CanceledException e) {
        }
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    public void addResolutionIntent(Intent extrasIntent, String resolutionAction, String callingPackage, int resolvableErrors, boolean confirmationCodeRetried, EuiccOperation op, int cardId) {
        Intent intent = new Intent("android.telephony.euicc.action.RESOLVE_ERROR");
        intent.setPackage(RESOLUTION_ACTIVITY_PACKAGE_NAME);
        intent.setComponent(new ComponentName(RESOLUTION_ACTIVITY_PACKAGE_NAME, RESOLUTION_ACTIVITY_CLASS_NAME));
        intent.putExtra("android.telephony.euicc.extra.EMBEDDED_SUBSCRIPTION_RESOLUTION_ACTION", resolutionAction);
        intent.putExtra("android.service.euicc.extra.RESOLUTION_CALLING_PACKAGE", callingPackage);
        intent.putExtra("android.service.euicc.extra.RESOLVABLE_ERRORS", resolvableErrors);
        intent.putExtra("android.service.euicc.extra.RESOLUTION_CARD_ID", cardId);
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
        int subCount = subs != null ? subs.size() : 0;
        for (int i = 0; i < subCount; i++) {
            SubscriptionInfo sub = subs.get(i);
            if (subscriptionId == sub.getSubscriptionIdHw()) {
                return sub;
            }
        }
        return null;
    }

    private String blockingGetEidFromEuiccService(int cardId) {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<String> eidRef = new AtomicReference<>();
        this.mConnector.getEid(cardId, new EuiccConnector.GetEidCommandCallback() {
            /* class com.android.internal.telephony.euicc.EuiccController.AnonymousClass9 */

            @Override // com.android.internal.telephony.euicc.EuiccConnector.GetEidCommandCallback
            public void onGetEidComplete(String eid) {
                eidRef.set(eid);
                latch.countDown();
            }

            @Override // com.android.internal.telephony.euicc.EuiccConnector.BaseEuiccCommandCallback
            public void onEuiccServiceUnavailable() {
                latch.countDown();
            }
        });
        return (String) awaitResult(latch, eidRef);
    }

    private int blockingGetOtaStatusFromEuiccService(int cardId) {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Integer> statusRef = new AtomicReference<>(5);
        this.mConnector.getOtaStatus(cardId, new EuiccConnector.GetOtaStatusCommandCallback() {
            /* class com.android.internal.telephony.euicc.EuiccController.AnonymousClass10 */

            @Override // com.android.internal.telephony.euicc.EuiccConnector.GetOtaStatusCommandCallback
            public void onGetOtaStatusComplete(int status) {
                statusRef.set(Integer.valueOf(status));
                latch.countDown();
            }

            @Override // com.android.internal.telephony.euicc.EuiccConnector.BaseEuiccCommandCallback
            public void onEuiccServiceUnavailable() {
                latch.countDown();
            }
        });
        return ((Integer) awaitResult(latch, statusRef)).intValue();
    }

    private EuiccInfo blockingGetEuiccInfoFromEuiccService(int cardId) {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<EuiccInfo> euiccInfoRef = new AtomicReference<>();
        this.mConnector.getEuiccInfo(cardId, new EuiccConnector.GetEuiccInfoCommandCallback() {
            /* class com.android.internal.telephony.euicc.EuiccController.AnonymousClass11 */

            @Override // com.android.internal.telephony.euicc.EuiccConnector.GetEuiccInfoCommandCallback
            public void onGetEuiccInfoComplete(EuiccInfo euiccInfo) {
                euiccInfoRef.set(euiccInfo);
                latch.countDown();
            }

            @Override // com.android.internal.telephony.euicc.EuiccConnector.BaseEuiccCommandCallback
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
    /* access modifiers changed from: public */
    private boolean checkCarrierPrivilegeInMetadata(DownloadableSubscription subscription, String callingPackage) {
        UiccAccessRule[] rules = null;
        List<UiccAccessRule> rulesList = subscription.getAccessRules();
        if (rulesList != null) {
            rules = (UiccAccessRule[]) rulesList.toArray(new UiccAccessRule[rulesList.size()]);
        }
        if (rules == null) {
            Log.e(TAG, "No access rules but caller is unprivileged");
            return false;
        }
        try {
            PackageInfo info = this.mPackageManager.getPackageInfo(callingPackage, 64);
            for (UiccAccessRule uiccAccessRule : rules) {
                if (uiccAccessRule.getCarrierPrivilegeStatus(info) == 1) {
                    Log.i(TAG, "Calling package has carrier privilege to this profile");
                    return true;
                }
            }
            Log.e(TAG, "Calling package doesn't have carrier privilege to this profile");
            return false;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Calling package valid but gone");
            return false;
        }
    }

    private boolean supportMultiActiveSlots() {
        return this.mTelephonyManager.getPhoneCount() > 1;
    }

    private boolean canManageActiveSubscriptionOnTargetSim(int cardId, String callingPackage) {
        List<SubscriptionInfo> subInfoList = this.mSubscriptionManager.getActiveSubscriptionInfoList(false);
        if (subInfoList == null || subInfoList.size() == 0) {
            return false;
        }
        for (SubscriptionInfo subInfo : subInfoList) {
            if ((cardId == -1 || subInfo.getCardId() == cardId) && subInfo.isEmbedded() && this.mSubscriptionManager.canManageSubscription(subInfo, callingPackage)) {
                return true;
            }
        }
        return false;
    }

    private boolean canManageSubscriptionOnTargetSim(int cardId, String callingPackage) {
        List<SubscriptionInfo> subInfoList = this.mSubscriptionManager.getActiveSubscriptionInfoList(false);
        if (subInfoList == null || subInfoList.size() == 0) {
            return false;
        }
        if (supportMultiActiveSlots()) {
            List<UiccCardInfo> cardInfos = this.mTelephonyManager.getUiccCardsInfo();
            if (cardInfos == null || cardInfos.isEmpty()) {
                return false;
            }
            boolean isEuicc = false;
            Iterator<UiccCardInfo> it = cardInfos.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                UiccCardInfo info = it.next();
                if (info != null && info.getCardId() == cardId && info.isEuicc()) {
                    isEuicc = true;
                    break;
                }
            }
            if (!isEuicc) {
                Log.i(TAG, "The target SIM is not an eUICC.");
                return false;
            }
            for (SubscriptionInfo subInfo : subInfoList) {
                if (subInfo.isEmbedded() && subInfo.getCardId() == cardId) {
                    return this.mSubscriptionManager.canManageSubscription(subInfo, callingPackage);
                }
            }
            if (this.mTelephonyManager.checkCarrierPrivilegesForPackageAnyPhone(callingPackage) == 1) {
                return true;
            }
            return false;
        }
        for (SubscriptionInfo subInfo2 : subInfoList) {
            if (subInfo2.isEmbedded() && this.mSubscriptionManager.canManageSubscription(subInfo2, callingPackage)) {
                return true;
            }
        }
        return false;
    }

    private boolean callerCanReadPhoneStatePrivileged() {
        return this.mContext.checkCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE") == 0;
    }

    private boolean callerCanWriteEmbeddedSubscriptions() {
        return this.mContext.checkCallingOrSelfPermission("android.permission.WRITE_EMBEDDED_SUBSCRIPTIONS") == 0;
    }

    public IBinder getHwInnerService() {
        return this.mHwInnerService;
    }

    @Override // com.android.internal.telephony.euicc.IHwEuiccControllerInner
    public EuiccConnector getEuiccConnector() {
        return this.mConnector;
    }

    public class HwInnerEuiccController extends IHwEuiccManager.Stub {
        EuiccController mEuiccController;

        public HwInnerEuiccController(EuiccController euiccController) {
            this.mEuiccController = euiccController;
        }

        @Override // com.android.internal.telephony.euicc.IHwEuiccManager
        public void requestDefaultSmdpAddress(String cardId, PendingIntent callbackIntent) {
            EuiccController.this.mEuiccControllerEx.requestDefaultSmdpAddress(cardId, callbackIntent);
        }

        @Override // com.android.internal.telephony.euicc.IHwEuiccManager
        public void resetMemory(String cardId, int options, PendingIntent callbackIntent) {
            EuiccController.this.mEuiccControllerEx.resetMemory(cardId, options, callbackIntent);
        }

        @Override // com.android.internal.telephony.euicc.IHwEuiccManager
        public void setDefaultSmdpAddress(String cardId, String defaultSmdpAddress, PendingIntent callbackIntent) {
            EuiccController.this.mEuiccControllerEx.setDefaultSmdpAddress(cardId, defaultSmdpAddress, callbackIntent);
        }

        @Override // com.android.internal.telephony.euicc.IHwEuiccManager
        public void cancelSession() {
            EuiccController.this.mEuiccControllerEx.cancelSession();
        }

        @Override // com.android.internal.telephony.euicc.IHwEuiccManager
        public void startOtaUpdating(int cardId, int otaReason) {
            EuiccController.this.mEuiccControllerEx.startOtaUpdating(cardId, otaReason);
        }
    }
}
