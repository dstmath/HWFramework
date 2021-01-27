package com.android.server.intellicom.smartnet;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.RemoteException;
import android.telephony.Rlog;
import android.util.Log;
import com.huawei.hiai.awareness.AwarenessConstants;
import com.huawei.hiai.awareness.service.AwarenessFence;
import com.huawei.hiai.awareness.service.AwarenessManager;
import com.huawei.hiai.awareness.service.AwarenessServiceConnection;
import com.huawei.hiai.awareness.service.IRequestCallBack;
import com.huawei.hiai.awareness.service.RequestResult;

public class HwFixRouteRecognition {
    private static final String ACTION_HW_SMARTNET_LOCATION_CHANGED = "com.huawei.intent.action.SMARTNET_LOCATION_CHANGED";
    private static final int DEFAULT_VALUE = -1;
    public static final int EVENT_LOCATION_DEFAULT = 0;
    public static final int EVENT_LOCATION_EXIT_COMPANY = 4;
    public static final int EVENT_LOCATION_EXIT_COMPANY_AND_ENTER_HOME = 1;
    public static final int EVENT_LOCATION_EXIT_HOME = 3;
    public static final int EVENT_LOCATION_EXIT_HOME_AND_ENTER_COMPANY = 2;
    private static final int EVENT_REGISTER_AWARENESS_FENCE = 1;
    private static final String INTENT_LOCATION_CHANED_ACTION = "com.android.server.intellicom.smartnet.LOCATION_CHANGED";
    private static final String PACKAGE_NAME_PHONE = "com.android.phone";
    private static final String PERMISSION_LOCATION_CHANGE_BROADCAST = "com.android.server.intellicom.smartnet.permission.PERMISSION_LOCATION_CHANGE_BROADCAST";
    private static final int RECONNECT_MAX_COUNT = 3;
    private static final long RECONNECT_TIMER = 15000;
    private static final String SMARTNET_LOCATION_CHANGED_EVENTID = "smartnet_location_changed_eventid";
    private static final String TAG = "HwFixRouteRecognition";
    private AwarenessFence mAwarenessFence;
    private AwarenessManager mAwarenessManager;
    private int mAwarenessReconnectTimes = 0;
    private AwarenessServiceConnection mAwarenessServiceConnection = new AwarenessServiceConnection() {
        /* class com.android.server.intellicom.smartnet.HwFixRouteRecognition.AnonymousClass2 */

        @Override // com.huawei.hiai.awareness.service.AwarenessServiceConnection
        public void onServiceConnected() {
            HwFixRouteRecognition.this.logd("onServiceConnected ");
            HwFixRouteRecognition.this.mIsAwarenessConnected = true;
            HwFixRouteRecognition.this.mAwarenessReconnectTimes = 0;
            HwFixRouteRecognition.this.mHandler.sendMessage(HwFixRouteRecognition.this.mHandler.obtainMessage(1));
        }

        @Override // com.huawei.hiai.awareness.service.AwarenessServiceConnection
        public void onServiceDisconnected() {
            HwFixRouteRecognition.this.loge("onServiceDisconnected ");
            HwFixRouteRecognition.this.mIsAwarenessConnected = false;
            HwFixRouteRecognition.this.logd("wait 15000 ms to reconnect");
            HwFixRouteRecognition.this.mHandler.postDelayed(new Runnable() {
                /* class com.android.server.intellicom.smartnet.HwFixRouteRecognition.AnonymousClass2.AnonymousClass1 */

                @Override // java.lang.Runnable
                public void run() {
                    HwFixRouteRecognition.access$308(HwFixRouteRecognition.this);
                    if (!HwFixRouteRecognition.this.mIsAwarenessConnected && HwFixRouteRecognition.this.mAwarenessReconnectTimes < 3 && !HwFixRouteRecognition.this.mAwarenessManager.connectService(HwFixRouteRecognition.this.mAwarenessServiceConnection)) {
                        Log.w(HwFixRouteRecognition.TAG, "onServiceDisconnected() connectService failed!");
                        HwFixRouteRecognition.this.mHandler.postDelayed(this, HwFixRouteRecognition.RECONNECT_TIMER);
                    }
                }
            }, HwFixRouteRecognition.RECONNECT_TIMER);
        }
    };
    private Context mContext;
    private Handler mHandler;
    private HwFixRouteBroadcastReceiver mHwFixRouteBroadcastReceiver;
    private boolean mIsAwarenessConnected;
    private int mLocationStatus = -1;
    private PendingIntent mPendingIntent;
    private IRequestCallBack mRequestCallBack = new IRequestCallBack.Stub() {
        /* class com.android.server.intellicom.smartnet.HwFixRouteRecognition.AnonymousClass1 */

        @Override // com.huawei.hiai.awareness.service.IRequestCallBack
        public void onRequestResult(RequestResult requestResult) throws RemoteException {
            if (requestResult != null) {
                int triggerStatus = requestResult.getTriggerStatus();
                HwFixRouteRecognition hwFixRouteRecognition = HwFixRouteRecognition.this;
                hwFixRouteRecognition.loge("Register trigger status: " + triggerStatus);
                if (triggerStatus == 4) {
                    HwFixRouteRecognition hwFixRouteRecognition2 = HwFixRouteRecognition.this;
                    hwFixRouteRecognition2.loge("Register failed getErrorCode: " + requestResult.getErrorCode());
                }
            }
        }
    };

    static /* synthetic */ int access$308(HwFixRouteRecognition x0) {
        int i = x0.mAwarenessReconnectTimes;
        x0.mAwarenessReconnectTimes = i + 1;
        return i;
    }

    public HwFixRouteRecognition(Context context) {
        if (context != null) {
            this.mContext = context;
            this.mAwarenessManager = new AwarenessManager(this.mContext);
            this.mHwFixRouteBroadcastReceiver = new HwFixRouteBroadcastReceiver();
        }
    }

    private class HwFixRouteBroadcastReceiver extends BroadcastReceiver {
        HwFixRouteBroadcastReceiver() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.BOOT_COMPLETED");
            HwFixRouteRecognition.this.mContext.registerReceiver(this, filter);
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (context == null || intent == null) {
                HwFixRouteRecognition.this.logd("Invalid input parameter!");
                return;
            }
            String action = intent.getAction();
            if (action == null) {
                HwFixRouteRecognition.this.logd("BroadcastReceiver.getAction() is null!");
                return;
            }
            HwFixRouteRecognition hwFixRouteRecognition = HwFixRouteRecognition.this;
            hwFixRouteRecognition.logd("BroadcastReceiver.onReceive() action:" + action);
            char c = 65535;
            if (action.hashCode() == 798292259 && action.equals("android.intent.action.BOOT_COMPLETED")) {
                c = 0;
            }
            if (c == 0) {
                boolean initResult = HwFixRouteRecognition.this.init();
                HwFixRouteRecognition hwFixRouteRecognition2 = HwFixRouteRecognition.this;
                hwFixRouteRecognition2.logd("init() result " + initResult);
            }
        }
    }

    private void initHandler() {
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        this.mHandler = new Handler(handlerThread.getLooper()) {
            /* class com.android.server.intellicom.smartnet.HwFixRouteRecognition.AnonymousClass3 */

            @Override // android.os.Handler
            public void handleMessage(Message message) {
                if (message.what != 1) {
                    super.handleMessage(message);
                    return;
                }
                boolean isAwarenessRegistered = HwFixRouteRecognition.this.registerAwarenessFence();
                HwFixRouteRecognition hwFixRouteRecognition = HwFixRouteRecognition.this;
                hwFixRouteRecognition.logd("registerAwarenessFence result: " + isAwarenessRegistered);
            }
        };
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean init() {
        if (this.mContext == null || this.mAwarenessManager == null) {
            return false;
        }
        initHandler();
        this.mAwarenessFence = new AwarenessFence(6, 15, 7, null);
        this.mPendingIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(INTENT_LOCATION_CHANED_ACTION), 0);
        this.mContext.registerReceiver(new LocationChangedBroadcastReceiver(), new IntentFilter(INTENT_LOCATION_CHANED_ACTION), PERMISSION_LOCATION_CHANGE_BROADCAST, null);
        initAwarenessServiceRestartReceiver();
        boolean isSuccess = this.mAwarenessManager.connectService(this.mAwarenessServiceConnection);
        logd("connectService result " + isSuccess);
        return isSuccess;
    }

    private int getLocationEventId(int locationStatus) {
        if (locationStatus != 1) {
            if (locationStatus == 2) {
                return 3;
            }
            if (locationStatus != 4) {
                if (locationStatus != 8) {
                    return 0;
                }
                return 4;
            } else if (this.mLocationStatus == 2) {
                return 2;
            } else {
                return 0;
            }
        } else if (this.mLocationStatus == 8) {
            return 1;
        } else {
            return 0;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleLocationChanged() {
        int triggerStatus = 0;
        int locationStatus = -1;
        RequestResult triggerResult = this.mAwarenessManager.getFenceTriggerResult(this.mAwarenessFence, this.mPendingIntent);
        if (triggerResult != null) {
            triggerStatus = triggerResult.getTriggerStatus();
            locationStatus = triggerResult.getStatus();
        }
        if (triggerStatus == 1) {
            int eventId = getLocationEventId(locationStatus);
            if (!(locationStatus == this.mLocationStatus || eventId == 0)) {
                this.mLocationStatus = locationStatus;
                sendBroadcastSmartNetLocationChanged(eventId);
                logd("Awareness EVENT_ALREADY_TRIGGER_TAG: eventId = " + eventId);
            }
            Handler handler = this.mHandler;
            handler.sendMessage(handler.obtainMessage(1));
        }
        logd("Awareness handleLocationChanged, triggerStatus = " + triggerStatus + " locationStatus = " + locationStatus);
    }

    private void sendBroadcastSmartNetLocationChanged(int eventId) {
        Intent intent = new Intent(ACTION_HW_SMARTNET_LOCATION_CHANGED);
        intent.setPackage(PACKAGE_NAME_PHONE);
        intent.putExtra(SMARTNET_LOCATION_CHANGED_EVENTID, eventId);
        this.mContext.sendBroadcast(intent, "android.permission.READ_PHONE_STATE");
    }

    private void initAwarenessServiceRestartReceiver() {
        AwarenessServiceRestartReceiver awarenessServiceRestartReceiver = new AwarenessServiceRestartReceiver();
        this.mContext.registerReceiver(awarenessServiceRestartReceiver, awarenessServiceRestartReceiver.getFilter(), AwarenessConstants.NotifyServiceCreateConstants.PERMISSION_AWARENESS_SERVICE_CREATE, null);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean registerAwarenessFence() {
        AwarenessManager awarenessManager;
        if (this.mIsAwarenessConnected && (awarenessManager = this.mAwarenessManager) != null) {
            return awarenessManager.registerLocationFence(this.mRequestCallBack, this.mAwarenessFence, this.mPendingIntent);
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logd(String string) {
        Rlog.i(TAG, string);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void loge(String string) {
        Rlog.e(TAG, string);
    }

    /* access modifiers changed from: private */
    public class LocationChangedBroadcastReceiver extends BroadcastReceiver {
        private LocationChangedBroadcastReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (HwFixRouteRecognition.this.mIsAwarenessConnected) {
                HwFixRouteRecognition.this.handleLocationChanged();
            } else {
                HwFixRouteRecognition.this.loge("mIsAwarenessConnected is false!");
            }
        }
    }

    /* access modifiers changed from: private */
    public class AwarenessServiceRestartReceiver extends BroadcastReceiver {
        private static final String ACTION_AWARENESS_SERVICE_CREATE = "com.huawei.hiai.awareness.action.service.create";
        private static final String AWARENESS_SERVICE_CREATE_TYPE = "awareness_service_create_type";
        private static final int SERVICE_EXP_RESTART = 2;

        private AwarenessServiceRestartReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                HwFixRouteRecognition.this.loge("AwarenessServiceRestartReceiver onReceive intent is null");
                return;
            }
            String action = intent.getAction();
            if (action == null) {
                HwFixRouteRecognition.this.logd("AwarenessServiceRestartReceiver onReceive action is null");
                return;
            }
            int notifyType = -1;
            if ("com.huawei.hiai.awareness.action.service.create".equals(action)) {
                notifyType = intent.getIntExtra("awareness_service_create_type", -1);
            }
            if (notifyType == 2) {
                HwFixRouteRecognition hwFixRouteRecognition = HwFixRouteRecognition.this;
                hwFixRouteRecognition.logd("Awareness engine system crash : notifyType = " + notifyType);
                HwFixRouteRecognition.this.mAwarenessManager.connectService(HwFixRouteRecognition.this.mAwarenessServiceConnection);
            }
        }

        public IntentFilter getFilter() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("com.huawei.hiai.awareness.action.service.create");
            return filter;
        }
    }
}
