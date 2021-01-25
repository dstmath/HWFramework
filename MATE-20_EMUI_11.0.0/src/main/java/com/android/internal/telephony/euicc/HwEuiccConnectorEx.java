package com.android.internal.telephony.euicc;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.service.euicc.HwEuiccService;
import android.service.euicc.IEuiccService;
import android.service.euicc.IHwGetSmdsAddressCallback;
import android.service.euicc.IHwResetMemoryCallback;
import android.service.euicc.IHwSetDefaultSmdpAddressCallback;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.euicc.EuiccConnector;
import com.android.internal.telephony.euicc.HwEuiccConnectorEx;

public class HwEuiccConnectorEx implements IHwEuiccConnectorEx {
    private static final int CMD_CANCEL_SESSION = 1004;
    private static final int CMD_HW_BASE = 1000;
    private static final int CMD_REQUEST_DEFAULT_SMDP_ADDRESS = 1001;
    private static final int CMD_RESET_MEMORY = 1002;
    private static final int CMD_SET_DEFAULT_SMDP_ADDRESS = 1003;
    private static final int EVENT_GET_BEST_COMPONENT_RETRY = 2001;
    private static final int EVENT_HW_BASE = 2000;
    private static final int EVENT_REFRESH_SUBSCRIPTION_FOR_EUICC = 2002;
    private static final int GET_BEST_COMPONENT_RETRY_DELAY_TIME = 2000;
    private static final String LOG_TAG = "HwEuiccConnectorEx";
    private static final int REFRESH_SUBSCRIPTION_DELAY_TIME = 100;
    private static final int RETRY_MAX_TIME = 60;
    private Context mContext;
    private EuiccConnector mEuiccConnector;
    private Handler mHandler;
    private IHwEuiccConnectorInner mInner;
    private PackageManager mPm;
    private int mRetryTimes;
    private TelephonyManager mTelephonyManager = TelephonyManager.from(this.mContext);

    public HwEuiccConnectorEx(Context context, IHwEuiccConnectorInner euiccConnector) {
        this.mContext = context;
        this.mInner = euiccConnector;
        this.mEuiccConnector = (EuiccConnector) euiccConnector;
        this.mHandler = new MyHandler();
        initBestComponent();
    }

    /* access modifiers changed from: private */
    public static void logd(String log) {
        Rlog.i(LOG_TAG, log);
    }

    /* access modifiers changed from: private */
    public static void logi(String log) {
        Rlog.i(LOG_TAG, log);
    }

    /* access modifiers changed from: private */
    public static void loge(String log) {
        Rlog.e(LOG_TAG, log);
    }

    public static EuiccConnector.BaseEuiccCommandCallback getCallback(Message message) {
        switch (message.what) {
            case CMD_REQUEST_DEFAULT_SMDP_ADDRESS /* 1001 */:
                return ((GetDefaultSmdpAddressRequest) message.obj).mCallback;
            case CMD_RESET_MEMORY /* 1002 */:
                return ((ResetMemoryRequest) message.obj).mCallback;
            case CMD_SET_DEFAULT_SMDP_ADDRESS /* 1003 */:
                return ((SetDefaultSmdpAddressRequest) message.obj).mCallback;
            case CMD_CANCEL_SESSION /* 1004 */:
                return (EuiccConnector.BaseEuiccCommandCallback) message.obj;
            default:
                throw new IllegalArgumentException("Unsupported message: " + message.what);
        }
    }

    private void initBestComponent() {
        this.mPm = this.mContext.getPackageManager();
        if (EuiccConnector.findBestComponent(this.mPm) == null) {
            Handler handler = this.mHandler;
            handler.sendMessageDelayed(handler.obtainMessage(EVENT_GET_BEST_COMPONENT_RETRY), 2000);
            this.mRetryTimes = 0;
            return;
        }
        logi("initBestComponent, has got best component.");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleGetBestComponentRetry() {
        PackageManager packageManager = this.mPm;
        if (packageManager == null || this.mRetryTimes >= RETRY_MAX_TIME) {
            logi("handleGetBestComponentRetry has arrived max times, stop!");
            this.mRetryTimes = 0;
        } else if (EuiccConnector.findBestComponent(packageManager) == null) {
            Handler handler = this.mHandler;
            handler.sendMessageDelayed(handler.obtainMessage(EVENT_GET_BEST_COMPONENT_RETRY), 2000);
            this.mRetryTimes++;
        } else {
            logi("handleGetBestComponentRetry has got euicc service, try to refresh subscription!");
            this.mEuiccConnector.sendMessage(1);
            Handler handler2 = this.mHandler;
            handler2.sendMessageDelayed(handler2.obtainMessage(EVENT_REFRESH_SUBSCRIPTION_FOR_EUICC), 100);
            this.mRetryTimes = 0;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleRefreshSubscriptionForEuicc() {
        PhoneFactory.requestEmbeddedSubscriptionInfoListRefresh(this.mTelephonyManager.getCardIdForDefaultEuicc(), (Runnable) null);
    }

    public boolean handleConnectedStateMessage(Message message) {
        logi("handleConnectedStateMessage, message.what = " + message.what);
        final EuiccConnector.BaseEuiccCommandCallback callback = getCallback(message);
        IEuiccService service = this.mInner.getEuiccService();
        try {
            switch (message.what) {
                case CMD_REQUEST_DEFAULT_SMDP_ADDRESS /* 1001 */:
                    HwEuiccService.requestDefaultSmdpAddress(service, ((GetDefaultSmdpAddressRequest) message.obj).mCardId, new IHwGetSmdsAddressCallback.Stub() {
                        /* class com.android.internal.telephony.euicc.HwEuiccConnectorEx.AnonymousClass1 */

                        public void onComplete(String result) {
                            HwEuiccConnectorEx.logi("IHwGetSmdsAddressCallback, onComplete");
                            HwEuiccConnectorEx.this.mEuiccConnector.sendMessage(6, new Runnable(callback, result) {
                                /* class com.android.internal.telephony.euicc.$$Lambda$HwEuiccConnectorEx$1$T7JqyfDj2beGQAgz4tWoQAdl1L4 */
                                private final /* synthetic */ EuiccConnector.BaseEuiccCommandCallback f$1;
                                private final /* synthetic */ String f$2;

                                {
                                    this.f$1 = r2;
                                    this.f$2 = r3;
                                }

                                @Override // java.lang.Runnable
                                public final void run() {
                                    HwEuiccConnectorEx.AnonymousClass1.this.lambda$onComplete$0$HwEuiccConnectorEx$1(this.f$1, this.f$2);
                                }
                            });
                        }

                        public /* synthetic */ void lambda$onComplete$0$HwEuiccConnectorEx$1(EuiccConnector.BaseEuiccCommandCallback callback, String result) {
                            ((EuiccConnector.RequestDefaultSmdpAddressCommandCallback) callback).onRequestDefaultSmdpAddressComplete(result);
                            HwEuiccConnectorEx.this.mInner.onCommandEndEx(callback);
                        }
                    });
                    return true;
                case CMD_RESET_MEMORY /* 1002 */:
                    ResetMemoryRequest request = (ResetMemoryRequest) message.obj;
                    HwEuiccService.resetMemory(service, request.mCardId, request.mOptions, new IHwResetMemoryCallback.Stub() {
                        /* class com.android.internal.telephony.euicc.HwEuiccConnectorEx.AnonymousClass2 */

                        public void onComplete(int result) {
                            HwEuiccConnectorEx.logi("IHwResetMemoryCallback, onComplete, result = " + result);
                            HwEuiccConnectorEx.this.mEuiccConnector.sendMessage(6, new Runnable(callback, result) {
                                /* class com.android.internal.telephony.euicc.$$Lambda$HwEuiccConnectorEx$2$AJX01F2eDEW4h6dmfnpZ9SxMWwk */
                                private final /* synthetic */ EuiccConnector.BaseEuiccCommandCallback f$1;
                                private final /* synthetic */ int f$2;

                                {
                                    this.f$1 = r2;
                                    this.f$2 = r3;
                                }

                                @Override // java.lang.Runnable
                                public final void run() {
                                    HwEuiccConnectorEx.AnonymousClass2.this.lambda$onComplete$0$HwEuiccConnectorEx$2(this.f$1, this.f$2);
                                }
                            });
                        }

                        public /* synthetic */ void lambda$onComplete$0$HwEuiccConnectorEx$2(EuiccConnector.BaseEuiccCommandCallback callback, int result) {
                            ((EuiccConnector.ResetMemoryCommandCallback) callback).onResetMemoryComplete(result);
                            HwEuiccConnectorEx.this.mInner.onCommandEndEx(callback);
                        }
                    });
                    return true;
                case CMD_SET_DEFAULT_SMDP_ADDRESS /* 1003 */:
                    SetDefaultSmdpAddressRequest request2 = (SetDefaultSmdpAddressRequest) message.obj;
                    HwEuiccService.setDefaultSmdpAddress(service, request2.mCardId, request2.mAddress, new IHwSetDefaultSmdpAddressCallback.Stub() {
                        /* class com.android.internal.telephony.euicc.HwEuiccConnectorEx.AnonymousClass3 */

                        public void onComplete(int result) {
                            HwEuiccConnectorEx.logi("IHwSetDefaultSmdpAddressCallback, onComplete, result = " + result);
                            HwEuiccConnectorEx.this.mEuiccConnector.sendMessage(6, new Runnable(callback, result) {
                                /* class com.android.internal.telephony.euicc.$$Lambda$HwEuiccConnectorEx$3$jJyrBOEbC7XXtfQt1KD_ktduvBg */
                                private final /* synthetic */ EuiccConnector.BaseEuiccCommandCallback f$1;
                                private final /* synthetic */ int f$2;

                                {
                                    this.f$1 = r2;
                                    this.f$2 = r3;
                                }

                                @Override // java.lang.Runnable
                                public final void run() {
                                    HwEuiccConnectorEx.AnonymousClass3.this.lambda$onComplete$0$HwEuiccConnectorEx$3(this.f$1, this.f$2);
                                }
                            });
                        }

                        public /* synthetic */ void lambda$onComplete$0$HwEuiccConnectorEx$3(EuiccConnector.BaseEuiccCommandCallback callback, int result) {
                            ((EuiccConnector.SetDefaultSmdpAddressCommandCallback) callback).onSetDefaultSmdpAddressComplete(result);
                            HwEuiccConnectorEx.this.mInner.onCommandEndEx(callback);
                        }
                    });
                    return true;
                case CMD_CANCEL_SESSION /* 1004 */:
                    HwEuiccService.cancelSession(service);
                    return true;
                default:
                    return false;
            }
        } catch (RemoteException e) {
            loge("Exception making binder call to HwEuiccConnectorEx");
            callback.onEuiccServiceUnavailable();
            this.mInner.onCommandEndEx(callback);
            return true;
        }
    }

    public void requestDefaultSmdpAddress(String cardId, EuiccConnector.RequestDefaultSmdpAddressCommandCallback callback) {
        logi("requestDefaultSmdpAddress enter");
        GetDefaultSmdpAddressRequest request = new GetDefaultSmdpAddressRequest();
        request.mCardId = cardId;
        request.mCallback = callback;
        this.mEuiccConnector.sendMessage((int) CMD_REQUEST_DEFAULT_SMDP_ADDRESS, request);
    }

    public void resetMemory(String cardId, int options, EuiccConnector.ResetMemoryCommandCallback callback) {
        logi("resetMemory enter");
        ResetMemoryRequest request = new ResetMemoryRequest();
        request.mCardId = cardId;
        request.mOptions = options;
        request.mCallback = callback;
        this.mEuiccConnector.sendMessage((int) CMD_RESET_MEMORY, request);
    }

    public void setDefaultSmdpAddress(String cardId, String address, EuiccConnector.SetDefaultSmdpAddressCommandCallback callback) {
        logi("setDefaultSmdpAddress enter");
        SetDefaultSmdpAddressRequest request = new SetDefaultSmdpAddressRequest();
        request.mCardId = cardId;
        request.mAddress = address;
        request.mCallback = callback;
        this.mEuiccConnector.sendMessage((int) CMD_SET_DEFAULT_SMDP_ADDRESS, request);
    }

    public void cancelSession() {
        logi("cancelSession enter");
        this.mEuiccConnector.sendMessage((int) CMD_CANCEL_SESSION, new EuiccConnector.BaseEuiccCommandCallback() {
            /* class com.android.internal.telephony.euicc.HwEuiccConnectorEx.AnonymousClass4 */

            public void onEuiccServiceUnavailable() {
                HwEuiccConnectorEx.loge("onEuiccServiceUnavailable, do nothing.");
            }
        });
    }

    /* access modifiers changed from: package-private */
    public static class GetDefaultSmdpAddressRequest {
        EuiccConnector.RequestDefaultSmdpAddressCommandCallback mCallback;
        String mCardId;

        GetDefaultSmdpAddressRequest() {
        }
    }

    /* access modifiers changed from: package-private */
    public static class ResetMemoryRequest {
        EuiccConnector.ResetMemoryCommandCallback mCallback;
        String mCardId;
        int mOptions;

        ResetMemoryRequest() {
        }
    }

    /* access modifiers changed from: package-private */
    public static class SetDefaultSmdpAddressRequest {
        String mAddress;
        EuiccConnector.SetDefaultSmdpAddressCommandCallback mCallback;
        String mCardId;

        SetDefaultSmdpAddressRequest() {
        }
    }

    private class MyHandler extends Handler {
        private MyHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == HwEuiccConnectorEx.EVENT_GET_BEST_COMPONENT_RETRY) {
                HwEuiccConnectorEx.logd("handleMessage: EVENT_GET_BEST_COMPONENT_RETRY, mRetryTimes = " + HwEuiccConnectorEx.this.mRetryTimes);
                HwEuiccConnectorEx.this.handleGetBestComponentRetry();
            } else if (i == HwEuiccConnectorEx.EVENT_REFRESH_SUBSCRIPTION_FOR_EUICC) {
                HwEuiccConnectorEx.logi("handleMessage: EVENT_REFRESH_SUBSCRIPTION_FOR_EUICC");
                HwEuiccConnectorEx.this.handleRefreshSubscriptionForEuicc();
            }
            super.handleMessage(msg);
        }
    }
}
