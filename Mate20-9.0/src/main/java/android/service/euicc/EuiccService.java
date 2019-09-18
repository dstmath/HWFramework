package android.service.euicc;

import android.annotation.SystemApi;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.service.euicc.IEuiccService;
import android.telephony.euicc.DownloadableSubscription;
import android.telephony.euicc.EuiccInfo;
import android.util.ArraySet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@SystemApi
public abstract class EuiccService extends Service {
    public static final String ACTION_MANAGE_EMBEDDED_SUBSCRIPTIONS = "android.service.euicc.action.MANAGE_EMBEDDED_SUBSCRIPTIONS";
    public static final String ACTION_PROVISION_EMBEDDED_SUBSCRIPTION = "android.service.euicc.action.PROVISION_EMBEDDED_SUBSCRIPTION";
    public static final String ACTION_RESOLVE_CONFIRMATION_CODE = "android.service.euicc.action.RESOLVE_CONFIRMATION_CODE";
    public static final String ACTION_RESOLVE_DEACTIVATE_SIM = "android.service.euicc.action.RESOLVE_DEACTIVATE_SIM";
    public static final String ACTION_RESOLVE_NO_PRIVILEGES = "android.service.euicc.action.RESOLVE_NO_PRIVILEGES";
    public static final String CATEGORY_EUICC_UI = "android.service.euicc.category.EUICC_UI";
    public static final String EUICC_SERVICE_INTERFACE = "android.service.euicc.EuiccService";
    public static final String EXTRA_RESOLUTION_CALLING_PACKAGE = "android.service.euicc.extra.RESOLUTION_CALLING_PACKAGE";
    public static final String EXTRA_RESOLUTION_CONFIRMATION_CODE = "android.service.euicc.extra.RESOLUTION_CONFIRMATION_CODE";
    public static final String EXTRA_RESOLUTION_CONFIRMATION_CODE_RETRIED = "android.service.euicc.extra.RESOLUTION_CONFIRMATION_CODE_RETRIED";
    public static final String EXTRA_RESOLUTION_CONSENT = "android.service.euicc.extra.RESOLUTION_CONSENT";
    public static final ArraySet<String> RESOLUTION_ACTIONS = new ArraySet<>();
    public static final int RESULT_FIRST_USER = 1;
    public static final int RESULT_MUST_DEACTIVATE_SIM = -1;
    public static final int RESULT_NEED_CONFIRMATION_CODE = -2;
    public static final int RESULT_OK = 0;
    /* access modifiers changed from: private */
    public ThreadPoolExecutor mExecutor;
    private final IEuiccService.Stub mStubWrapper = new IEuiccServiceWrapper();

    private class IEuiccServiceWrapper extends IEuiccService.Stub {
        private IEuiccServiceWrapper() {
        }

        public void downloadSubscription(int slotId, DownloadableSubscription subscription, boolean switchAfterDownload, boolean forceDeactivateSim, IDownloadSubscriptionCallback callback) {
            ThreadPoolExecutor access$100 = EuiccService.this.mExecutor;
            final int i = slotId;
            final DownloadableSubscription downloadableSubscription = subscription;
            final boolean z = switchAfterDownload;
            final boolean z2 = forceDeactivateSim;
            final IDownloadSubscriptionCallback iDownloadSubscriptionCallback = callback;
            AnonymousClass1 r1 = new Runnable() {
                public void run() {
                    try {
                        iDownloadSubscriptionCallback.onComplete(EuiccService.this.onDownloadSubscription(i, downloadableSubscription, z, z2));
                    } catch (RemoteException e) {
                    }
                }
            };
            access$100.execute(r1);
        }

        public void getEid(final int slotId, final IGetEidCallback callback) {
            EuiccService.this.mExecutor.execute(new Runnable() {
                public void run() {
                    try {
                        callback.onSuccess(EuiccService.this.onGetEid(slotId));
                    } catch (RemoteException e) {
                    }
                }
            });
        }

        public void startOtaIfNecessary(final int slotId, final IOtaStatusChangedCallback statusChangedCallback) {
            EuiccService.this.mExecutor.execute(new Runnable() {
                public void run() {
                    EuiccService.this.onStartOtaIfNecessary(slotId, new OtaStatusChangedCallback() {
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

        public void getOtaStatus(final int slotId, final IGetOtaStatusCallback callback) {
            EuiccService.this.mExecutor.execute(new Runnable() {
                public void run() {
                    try {
                        callback.onSuccess(EuiccService.this.onGetOtaStatus(slotId));
                    } catch (RemoteException e) {
                    }
                }
            });
        }

        public void getDownloadableSubscriptionMetadata(int slotId, DownloadableSubscription subscription, boolean forceDeactivateSim, IGetDownloadableSubscriptionMetadataCallback callback) {
            ThreadPoolExecutor access$100 = EuiccService.this.mExecutor;
            final int i = slotId;
            final DownloadableSubscription downloadableSubscription = subscription;
            final boolean z = forceDeactivateSim;
            final IGetDownloadableSubscriptionMetadataCallback iGetDownloadableSubscriptionMetadataCallback = callback;
            AnonymousClass5 r1 = new Runnable() {
                public void run() {
                    try {
                        iGetDownloadableSubscriptionMetadataCallback.onComplete(EuiccService.this.onGetDownloadableSubscriptionMetadata(i, downloadableSubscription, z));
                    } catch (RemoteException e) {
                    }
                }
            };
            access$100.execute(r1);
        }

        public void getDefaultDownloadableSubscriptionList(final int slotId, final boolean forceDeactivateSim, final IGetDefaultDownloadableSubscriptionListCallback callback) {
            EuiccService.this.mExecutor.execute(new Runnable() {
                public void run() {
                    try {
                        callback.onComplete(EuiccService.this.onGetDefaultDownloadableSubscriptionList(slotId, forceDeactivateSim));
                    } catch (RemoteException e) {
                    }
                }
            });
        }

        public void getEuiccProfileInfoList(final int slotId, final IGetEuiccProfileInfoListCallback callback) {
            EuiccService.this.mExecutor.execute(new Runnable() {
                public void run() {
                    try {
                        callback.onComplete(EuiccService.this.onGetEuiccProfileInfoList(slotId));
                    } catch (RemoteException e) {
                    }
                }
            });
        }

        public void getEuiccInfo(final int slotId, final IGetEuiccInfoCallback callback) {
            EuiccService.this.mExecutor.execute(new Runnable() {
                public void run() {
                    try {
                        callback.onSuccess(EuiccService.this.onGetEuiccInfo(slotId));
                    } catch (RemoteException e) {
                    }
                }
            });
        }

        public void deleteSubscription(final int slotId, final String iccid, final IDeleteSubscriptionCallback callback) {
            EuiccService.this.mExecutor.execute(new Runnable() {
                public void run() {
                    try {
                        callback.onComplete(EuiccService.this.onDeleteSubscription(slotId, iccid));
                    } catch (RemoteException e) {
                    }
                }
            });
        }

        public void switchToSubscription(int slotId, String iccid, boolean forceDeactivateSim, ISwitchToSubscriptionCallback callback) {
            ThreadPoolExecutor access$100 = EuiccService.this.mExecutor;
            final int i = slotId;
            final String str = iccid;
            final boolean z = forceDeactivateSim;
            final ISwitchToSubscriptionCallback iSwitchToSubscriptionCallback = callback;
            AnonymousClass10 r1 = new Runnable() {
                public void run() {
                    try {
                        iSwitchToSubscriptionCallback.onComplete(EuiccService.this.onSwitchToSubscription(i, str, z));
                    } catch (RemoteException e) {
                    }
                }
            };
            access$100.execute(r1);
        }

        public void updateSubscriptionNickname(int slotId, String iccid, String nickname, IUpdateSubscriptionNicknameCallback callback) {
            ThreadPoolExecutor access$100 = EuiccService.this.mExecutor;
            final int i = slotId;
            final String str = iccid;
            final String str2 = nickname;
            final IUpdateSubscriptionNicknameCallback iUpdateSubscriptionNicknameCallback = callback;
            AnonymousClass11 r1 = new Runnable() {
                public void run() {
                    try {
                        iUpdateSubscriptionNicknameCallback.onComplete(EuiccService.this.onUpdateSubscriptionNickname(i, str, str2));
                    } catch (RemoteException e) {
                    }
                }
            };
            access$100.execute(r1);
        }

        public void eraseSubscriptions(final int slotId, final IEraseSubscriptionsCallback callback) {
            EuiccService.this.mExecutor.execute(new Runnable() {
                public void run() {
                    try {
                        callback.onComplete(EuiccService.this.onEraseSubscriptions(slotId));
                    } catch (RemoteException e) {
                    }
                }
            });
        }

        public void retainSubscriptionsForFactoryReset(final int slotId, final IRetainSubscriptionsForFactoryResetCallback callback) {
            EuiccService.this.mExecutor.execute(new Runnable() {
                public void run() {
                    try {
                        callback.onComplete(EuiccService.this.onRetainSubscriptionsForFactoryReset(slotId));
                    } catch (RemoteException e) {
                    }
                }
            });
        }
    }

    public static abstract class OtaStatusChangedCallback {
        public abstract void onOtaStatusChanged(int i);
    }

    public abstract int onDeleteSubscription(int i, String str);

    public abstract int onDownloadSubscription(int i, DownloadableSubscription downloadableSubscription, boolean z, boolean z2);

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
        RESOLUTION_ACTIONS.add(ACTION_RESOLVE_CONFIRMATION_CODE);
    }

    public void onCreate() {
        super.onCreate();
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(4, 4, 30, TimeUnit.SECONDS, new LinkedBlockingQueue(), new ThreadFactory() {
            private final AtomicInteger mCount = new AtomicInteger(1);

            public Thread newThread(Runnable r) {
                return new Thread(r, "EuiccService #" + this.mCount.getAndIncrement());
            }
        });
        this.mExecutor = threadPoolExecutor;
        this.mExecutor.allowCoreThreadTimeOut(true);
    }

    public void onDestroy() {
        this.mExecutor.shutdownNow();
        super.onDestroy();
    }

    public IBinder onBind(Intent intent) {
        return this.mStubWrapper;
    }
}
