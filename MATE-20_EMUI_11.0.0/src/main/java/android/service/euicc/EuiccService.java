package android.service.euicc;

import android.annotation.SystemApi;
import android.app.Service;
import android.common.HwFrameworkFactory;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.service.euicc.IEuiccService;
import android.service.euicc.IHwEuiccService;
import android.telephony.euicc.DownloadableSubscription;
import android.telephony.euicc.EuiccInfo;
import android.util.ArraySet;
import android.util.Log;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@SystemApi
public abstract class EuiccService extends Service implements IHwEuiccServiceInner {
    public static final String ACTION_BIND_CARRIER_PROVISIONING_SERVICE = "android.service.euicc.action.BIND_CARRIER_PROVISIONING_SERVICE";
    public static final String ACTION_DELETE_SUBSCRIPTION_PRIVILEGED = "android.service.euicc.action.DELETE_SUBSCRIPTION_PRIVILEGED";
    public static final String ACTION_MANAGE_EMBEDDED_SUBSCRIPTIONS = "android.service.euicc.action.MANAGE_EMBEDDED_SUBSCRIPTIONS";
    public static final String ACTION_PROVISION_EMBEDDED_SUBSCRIPTION = "android.service.euicc.action.PROVISION_EMBEDDED_SUBSCRIPTION";
    public static final String ACTION_RENAME_SUBSCRIPTION_PRIVILEGED = "android.service.euicc.action.RENAME_SUBSCRIPTION_PRIVILEGED";
    @Deprecated
    public static final String ACTION_RESOLVE_CONFIRMATION_CODE = "android.service.euicc.action.RESOLVE_CONFIRMATION_CODE";
    public static final String ACTION_RESOLVE_DEACTIVATE_SIM = "android.service.euicc.action.RESOLVE_DEACTIVATE_SIM";
    public static final String ACTION_RESOLVE_NO_PRIVILEGES = "android.service.euicc.action.RESOLVE_NO_PRIVILEGES";
    public static final String ACTION_RESOLVE_RESOLVABLE_ERRORS = "android.service.euicc.action.RESOLVE_RESOLVABLE_ERRORS";
    public static final String ACTION_TOGGLE_SUBSCRIPTION_PRIVILEGED = "android.service.euicc.action.TOGGLE_SUBSCRIPTION_PRIVILEGED";
    public static final String CATEGORY_EUICC_UI = "android.service.euicc.category.EUICC_UI";
    public static final String EUICC_SERVICE_INTERFACE = "android.service.euicc.EuiccService";
    public static final String EXTRA_RESOLUTION_ALLOW_POLICY_RULES = "android.service.euicc.extra.RESOLUTION_ALLOW_POLICY_RULES";
    public static final String EXTRA_RESOLUTION_CALLING_PACKAGE = "android.service.euicc.extra.RESOLUTION_CALLING_PACKAGE";
    public static final String EXTRA_RESOLUTION_CARD_ID = "android.service.euicc.extra.RESOLUTION_CARD_ID";
    public static final String EXTRA_RESOLUTION_CONFIRMATION_CODE = "android.service.euicc.extra.RESOLUTION_CONFIRMATION_CODE";
    public static final String EXTRA_RESOLUTION_CONFIRMATION_CODE_RETRIED = "android.service.euicc.extra.RESOLUTION_CONFIRMATION_CODE_RETRIED";
    public static final String EXTRA_RESOLUTION_CONSENT = "android.service.euicc.extra.RESOLUTION_CONSENT";
    public static final String EXTRA_RESOLVABLE_ERRORS = "android.service.euicc.extra.RESOLVABLE_ERRORS";
    public static final ArraySet<String> RESOLUTION_ACTIONS = new ArraySet<>();
    public static final int RESOLVABLE_ERROR_CONFIRMATION_CODE = 1;
    public static final int RESOLVABLE_ERROR_POLICY_RULES = 2;
    public static final int RESULT_FIRST_USER = 1;
    public static final int RESULT_MUST_DEACTIVATE_SIM = -1;
    @Deprecated
    public static final int RESULT_NEED_CONFIRMATION_CODE = -2;
    public static final int RESULT_OK = 0;
    public static final int RESULT_RESOLVABLE_ERRORS = -2;
    private static final String TAG = "EuiccService";
    private IHwEuiccServiceEx mEuiccServiceEx = HwFrameworkFactory.getHwInnerTelephonyManager().getHwEuiccServiceEx(this);
    private ThreadPoolExecutor mExecutor;
    private HwInnerEuiccService mHwInnerService = new HwInnerEuiccService(this);
    private final IEuiccService.Stub mStubWrapper = new IEuiccServiceWrapper();

    public static abstract class OtaStatusChangedCallback {
        public abstract void onOtaStatusChanged(int i);
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ResolvableError {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface Result {
    }

    public abstract int onDeleteSubscription(int i, String str);

    public abstract int onEraseSubscriptions(int i);

    public abstract GetDefaultDownloadableSubscriptionListResult onGetDefaultDownloadableSubscriptionList(int i, boolean z);

    public abstract GetDownloadableSubscriptionMetadataResult onGetDownloadableSubscriptionMetadata(int i, DownloadableSubscription downloadableSubscription, boolean z);

    public abstract String onGetEid(int i);

    public abstract EuiccInfo onGetEuiccInfo(int i);

    public abstract GetEuiccProfileInfoListResult onGetEuiccProfileInfoList(int i);

    public abstract int onGetOtaStatus(int i);

    public abstract int onRetainSubscriptionsForFactoryReset(int i);

    public abstract void onStartOtaIfNecessary(int i, OtaStatusChangedCallback otaStatusChangedCallback);

    public abstract int onSwitchToSubscription(int i, String str, boolean z);

    public abstract int onUpdateSubscriptionNickname(int i, String str, String str2);

    static {
        RESOLUTION_ACTIONS.add(ACTION_RESOLVE_DEACTIVATE_SIM);
        RESOLUTION_ACTIONS.add(ACTION_RESOLVE_NO_PRIVILEGES);
        RESOLUTION_ACTIONS.add(ACTION_RESOLVE_RESOLVABLE_ERRORS);
    }

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        this.mExecutor = new ThreadPoolExecutor(4, 4, 30, TimeUnit.SECONDS, new LinkedBlockingQueue(), new ThreadFactory() {
            /* class android.service.euicc.EuiccService.AnonymousClass1 */
            private final AtomicInteger mCount = new AtomicInteger(1);

            @Override // java.util.concurrent.ThreadFactory
            public Thread newThread(Runnable r) {
                return new Thread(r, "EuiccService #" + this.mCount.getAndIncrement());
            }
        });
        this.mExecutor.allowCoreThreadTimeOut(true);
    }

    @Override // android.app.Service
    public void onDestroy() {
        this.mExecutor.shutdownNow();
        super.onDestroy();
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return this.mStubWrapper;
    }

    public DownloadSubscriptionResult onDownloadSubscription(int slotId, DownloadableSubscription subscription, boolean switchAfterDownload, boolean forceDeactivateSim, Bundle resolvedBundle) {
        return null;
    }

    @Deprecated
    public int onDownloadSubscription(int slotId, DownloadableSubscription subscription, boolean switchAfterDownload, boolean forceDeactivateSim) {
        return Integer.MIN_VALUE;
    }

    private class IEuiccServiceWrapper extends IEuiccService.Stub {
        private IEuiccServiceWrapper() {
        }

        @Override // android.service.euicc.IEuiccService
        public void downloadSubscription(final int slotId, final DownloadableSubscription subscription, final boolean switchAfterDownload, final boolean forceDeactivateSim, final Bundle resolvedBundle, final IDownloadSubscriptionCallback callback) {
            EuiccService.this.mExecutor.execute(new Runnable() {
                /* class android.service.euicc.EuiccService.IEuiccServiceWrapper.AnonymousClass1 */

                @Override // java.lang.Runnable
                public void run() {
                    DownloadSubscriptionResult result;
                    try {
                        result = EuiccService.this.onDownloadSubscription(slotId, subscription, switchAfterDownload, forceDeactivateSim, resolvedBundle);
                    } catch (AbstractMethodError e) {
                        Log.w(EuiccService.TAG, "The new onDownloadSubscription(int, DownloadableSubscription, boolean, boolean, Bundle) is not implemented. Fall back to the old one.", e);
                        result = new DownloadSubscriptionResult(EuiccService.this.onDownloadSubscription(slotId, subscription, switchAfterDownload, forceDeactivateSim), 0, -1);
                    }
                    try {
                        callback.onComplete(result);
                    } catch (RemoteException e2) {
                    }
                }
            });
        }

        @Override // android.service.euicc.IEuiccService
        public void getEid(final int slotId, final IGetEidCallback callback) {
            EuiccService.this.mExecutor.execute(new Runnable() {
                /* class android.service.euicc.EuiccService.IEuiccServiceWrapper.AnonymousClass2 */

                @Override // java.lang.Runnable
                public void run() {
                    try {
                        callback.onSuccess(EuiccService.this.onGetEid(slotId));
                    } catch (RemoteException e) {
                    }
                }
            });
        }

        @Override // android.service.euicc.IEuiccService
        public void startOtaIfNecessary(final int slotId, final IOtaStatusChangedCallback statusChangedCallback) {
            EuiccService.this.mExecutor.execute(new Runnable() {
                /* class android.service.euicc.EuiccService.IEuiccServiceWrapper.AnonymousClass3 */

                @Override // java.lang.Runnable
                public void run() {
                    EuiccService.this.onStartOtaIfNecessary(slotId, new OtaStatusChangedCallback() {
                        /* class android.service.euicc.EuiccService.IEuiccServiceWrapper.AnonymousClass3.AnonymousClass1 */

                        @Override // android.service.euicc.EuiccService.OtaStatusChangedCallback
                        public void onOtaStatusChanged(int status) {
                            try {
                                statusChangedCallback.onOtaStatusChanged(status);
                            } catch (RemoteException e) {
                            }
                        }
                    });
                }
            });
        }

        @Override // android.service.euicc.IEuiccService
        public void getOtaStatus(final int slotId, final IGetOtaStatusCallback callback) {
            EuiccService.this.mExecutor.execute(new Runnable() {
                /* class android.service.euicc.EuiccService.IEuiccServiceWrapper.AnonymousClass4 */

                @Override // java.lang.Runnable
                public void run() {
                    try {
                        callback.onSuccess(EuiccService.this.onGetOtaStatus(slotId));
                    } catch (RemoteException e) {
                    }
                }
            });
        }

        @Override // android.service.euicc.IEuiccService
        public void getDownloadableSubscriptionMetadata(final int slotId, final DownloadableSubscription subscription, final boolean forceDeactivateSim, final IGetDownloadableSubscriptionMetadataCallback callback) {
            EuiccService.this.mExecutor.execute(new Runnable() {
                /* class android.service.euicc.EuiccService.IEuiccServiceWrapper.AnonymousClass5 */

                @Override // java.lang.Runnable
                public void run() {
                    try {
                        callback.onComplete(EuiccService.this.onGetDownloadableSubscriptionMetadata(slotId, subscription, forceDeactivateSim));
                    } catch (RemoteException e) {
                    }
                }
            });
        }

        @Override // android.service.euicc.IEuiccService
        public void getDefaultDownloadableSubscriptionList(final int slotId, final boolean forceDeactivateSim, final IGetDefaultDownloadableSubscriptionListCallback callback) {
            EuiccService.this.mExecutor.execute(new Runnable() {
                /* class android.service.euicc.EuiccService.IEuiccServiceWrapper.AnonymousClass6 */

                @Override // java.lang.Runnable
                public void run() {
                    try {
                        callback.onComplete(EuiccService.this.onGetDefaultDownloadableSubscriptionList(slotId, forceDeactivateSim));
                    } catch (RemoteException e) {
                    }
                }
            });
        }

        @Override // android.service.euicc.IEuiccService
        public void getEuiccProfileInfoList(final int slotId, final IGetEuiccProfileInfoListCallback callback) {
            EuiccService.this.mExecutor.execute(new Runnable() {
                /* class android.service.euicc.EuiccService.IEuiccServiceWrapper.AnonymousClass7 */

                @Override // java.lang.Runnable
                public void run() {
                    try {
                        callback.onComplete(EuiccService.this.onGetEuiccProfileInfoList(slotId));
                    } catch (RemoteException e) {
                    }
                }
            });
        }

        @Override // android.service.euicc.IEuiccService
        public void getEuiccInfo(final int slotId, final IGetEuiccInfoCallback callback) {
            EuiccService.this.mExecutor.execute(new Runnable() {
                /* class android.service.euicc.EuiccService.IEuiccServiceWrapper.AnonymousClass8 */

                @Override // java.lang.Runnable
                public void run() {
                    try {
                        callback.onSuccess(EuiccService.this.onGetEuiccInfo(slotId));
                    } catch (RemoteException e) {
                    }
                }
            });
        }

        @Override // android.service.euicc.IEuiccService
        public void deleteSubscription(final int slotId, final String iccid, final IDeleteSubscriptionCallback callback) {
            EuiccService.this.mExecutor.execute(new Runnable() {
                /* class android.service.euicc.EuiccService.IEuiccServiceWrapper.AnonymousClass9 */

                @Override // java.lang.Runnable
                public void run() {
                    try {
                        callback.onComplete(EuiccService.this.onDeleteSubscription(slotId, iccid));
                    } catch (RemoteException e) {
                    }
                }
            });
        }

        @Override // android.service.euicc.IEuiccService
        public void switchToSubscription(final int slotId, final String iccid, final boolean forceDeactivateSim, final ISwitchToSubscriptionCallback callback) {
            EuiccService.this.mExecutor.execute(new Runnable() {
                /* class android.service.euicc.EuiccService.IEuiccServiceWrapper.AnonymousClass10 */

                @Override // java.lang.Runnable
                public void run() {
                    try {
                        callback.onComplete(EuiccService.this.onSwitchToSubscription(slotId, iccid, forceDeactivateSim));
                    } catch (RemoteException e) {
                    }
                }
            });
        }

        @Override // android.service.euicc.IEuiccService
        public void updateSubscriptionNickname(final int slotId, final String iccid, final String nickname, final IUpdateSubscriptionNicknameCallback callback) {
            EuiccService.this.mExecutor.execute(new Runnable() {
                /* class android.service.euicc.EuiccService.IEuiccServiceWrapper.AnonymousClass11 */

                @Override // java.lang.Runnable
                public void run() {
                    try {
                        callback.onComplete(EuiccService.this.onUpdateSubscriptionNickname(slotId, iccid, nickname));
                    } catch (RemoteException e) {
                    }
                }
            });
        }

        @Override // android.service.euicc.IEuiccService
        public void eraseSubscriptions(final int slotId, final IEraseSubscriptionsCallback callback) {
            EuiccService.this.mExecutor.execute(new Runnable() {
                /* class android.service.euicc.EuiccService.IEuiccServiceWrapper.AnonymousClass12 */

                @Override // java.lang.Runnable
                public void run() {
                    try {
                        callback.onComplete(EuiccService.this.onEraseSubscriptions(slotId));
                    } catch (RemoteException e) {
                    }
                }
            });
        }

        @Override // android.service.euicc.IEuiccService
        public void retainSubscriptionsForFactoryReset(final int slotId, final IRetainSubscriptionsForFactoryResetCallback callback) {
            EuiccService.this.mExecutor.execute(new Runnable() {
                /* class android.service.euicc.EuiccService.IEuiccServiceWrapper.AnonymousClass13 */

                @Override // java.lang.Runnable
                public void run() {
                    try {
                        callback.onComplete(EuiccService.this.onRetainSubscriptionsForFactoryReset(slotId));
                    } catch (RemoteException e) {
                    }
                }
            });
        }

        @Override // android.service.euicc.IEuiccService
        public void getHwInnerService(final IGetInnerServiceCallback callback) {
            EuiccService.this.mExecutor.execute(new Runnable() {
                /* class android.service.euicc.EuiccService.IEuiccServiceWrapper.AnonymousClass14 */

                @Override // java.lang.Runnable
                public void run() {
                    try {
                        callback.onComplete(EuiccService.this.mHwInnerService);
                    } catch (RemoteException e) {
                    }
                }
            });
        }
    }

    @Override // android.service.euicc.IHwEuiccServiceInner
    public ThreadPoolExecutor getExecutor() {
        return this.mExecutor;
    }

    public String onRequestDefaultSmdpAddress(String cardId) {
        return "";
    }

    public int onResetMemory(String cardId, int options) {
        return 0;
    }

    public int onSetDefaultSmdpAddress(String cardId, String address) {
        return 0;
    }

    public void onCancelSession() {
    }

    public class HwInnerEuiccService extends IHwEuiccService.Stub {
        EuiccService mEuiccService;

        public HwInnerEuiccService(EuiccService euiccService) {
            this.mEuiccService = euiccService;
        }

        @Override // android.service.euicc.IHwEuiccService
        public void requestDefaultSmdpAddress(String cardId, IHwGetSmdsAddressCallback callback) {
            EuiccService.this.mEuiccServiceEx.requestDefaultSmdpAddress(cardId, callback);
        }

        @Override // android.service.euicc.IHwEuiccService
        public void resetMemory(String cardId, int options, IHwResetMemoryCallback callback) {
            EuiccService.this.mEuiccServiceEx.resetMemory(cardId, options, callback);
        }

        @Override // android.service.euicc.IHwEuiccService
        public void setDefaultSmdpAddress(String cardId, String address, IHwSetDefaultSmdpAddressCallback callback) {
            EuiccService.this.mEuiccServiceEx.setDefaultSmdpAddress(cardId, address, callback);
        }

        @Override // android.service.euicc.IHwEuiccService
        public void cancelSession() {
            EuiccService.this.mEuiccServiceEx.cancelSession();
        }
    }
}
