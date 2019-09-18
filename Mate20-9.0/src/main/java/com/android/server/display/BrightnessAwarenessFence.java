package com.android.server.display;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.RemoteException;
import com.android.server.display.HwBrightnessSceneRecognition;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.huawei.displayengine.DElog;
import com.huawei.hiai.awareness.service.AwarenessFence;
import com.huawei.hiai.awareness.service.AwarenessManager;
import com.huawei.hiai.awareness.service.AwarenessServiceConnection;
import com.huawei.hiai.awareness.service.IRequestCallBack;
import com.huawei.hiai.awareness.service.RequestResult;

class BrightnessAwarenessFence {
    private static final String AWARENESS_STATUS_CHANGE_ACTION = "com.android.server.display.awareness_status_change";
    private static final int RECONNECT_MAX_COUNT = 3;
    private static final int RECOONECT_WAIT_TIME_MS = 10000;
    private static final String TAG = "DE J AwarenessFence";
    private AwarenessBroadcastReceiver mAwarenessBroadcastReceiver;
    /* access modifiers changed from: private */
    public IRequestCallBack mAwarenessCallback;
    /* access modifiers changed from: private */
    public AwarenessFence mAwarenessFence;
    /* access modifiers changed from: private */
    public Handler mAwarenessHandler;
    private int mAwarenessLocationStatus = -1;
    /* access modifiers changed from: private */
    public AwarenessManager mAwarenessManager;
    /* access modifiers changed from: private */
    public PendingIntent mAwarenessPendingIntent;
    /* access modifiers changed from: private */
    public int mAwarenessReconnectTimes;
    /* access modifiers changed from: private */
    public AwarenessServiceConnection mAwarenessServiceConnection;
    /* access modifiers changed from: private */
    public final Context mContext;
    private final HwBrightnessSceneRecognition mHwBrightnessSceneRecognition;
    /* access modifiers changed from: private */
    public volatile boolean mIsAwarenessConnected;

    private class AwarenessBroadcastReceiver extends BroadcastReceiver {
        public AwarenessBroadcastReceiver() {
            registerAwarenessReceiver();
        }

        private void registerAwarenessReceiver() {
            BrightnessAwarenessFence.this.mContext.registerReceiver(this, new IntentFilter(BrightnessAwarenessFence.AWARENESS_STATUS_CHANGE_ACTION));
        }

        public void onReceive(Context context, Intent intent) {
            if (BrightnessAwarenessFence.this.mIsAwarenessConnected) {
                BrightnessAwarenessFence.this.handleFenceResult();
            } else {
                DElog.e(BrightnessAwarenessFence.TAG, "mIsAwarenessConnected is false!");
            }
        }
    }

    public BrightnessAwarenessFence(Context context, HwBrightnessSceneRecognition hwBrightnessSceneRecognition) {
        this.mContext = context;
        this.mHwBrightnessSceneRecognition = hwBrightnessSceneRecognition;
    }

    /* access modifiers changed from: private */
    public void handleFenceResult() {
        int triggerStatus = 0;
        int locationStatus = -1;
        RequestResult triggerResult = this.mAwarenessManager.getFenceTriggerResult(this.mAwarenessFence, this.mAwarenessPendingIntent);
        if (triggerResult != null) {
            triggerStatus = triggerResult.getTriggerStatus();
            locationStatus = triggerResult.getStatus();
        }
        if (triggerStatus != 2) {
            registerAwarenessFence();
        }
        if (triggerStatus == 1 && locationStatus != this.mAwarenessLocationStatus) {
            this.mAwarenessLocationStatus = locationStatus;
            String tag = HwBrightnessSceneRecognition.SceneTag.LOCATION_UNKNOWN;
            if (this.mAwarenessLocationStatus == 1) {
                tag = HwBrightnessSceneRecognition.SceneTag.LOCATION_HOME;
            } else if (this.mAwarenessLocationStatus == 2) {
                tag = HwBrightnessSceneRecognition.SceneTag.LOCATION_NOT_HOME;
            }
            this.mHwBrightnessSceneRecognition.setLocationStatus(tag);
        }
        DElog.i(TAG, "Awareness registerReceiver, triggerResult = " + triggerResult);
    }

    private boolean registerAwarenessFence() {
        boolean ret = this.mAwarenessManager.registerLocationFence(this.mAwarenessCallback, this.mAwarenessFence, this.mAwarenessPendingIntent);
        DElog.i(TAG, "AwarenessBroadcastReceiver received registerLocationFence ret = " + ret);
        return ret;
    }

    private void initAwareness() {
        this.mAwarenessManager = new AwarenessManager(this.mContext);
        this.mAwarenessBroadcastReceiver = new AwarenessBroadcastReceiver();
        this.mAwarenessFence = new AwarenessFence(6, 3, 3, null);
        this.mAwarenessPendingIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(AWARENESS_STATUS_CHANGE_ACTION), 0);
        this.mAwarenessCallback = new IRequestCallBack.Stub() {
            public void onRequestResult(RequestResult result) throws RemoteException {
                DElog.i(BrightnessAwarenessFence.TAG, "registerFence() result = " + result);
            }
        };
        this.mAwarenessServiceConnection = new AwarenessServiceConnection() {
            public void onServiceConnected() {
                boolean unused = BrightnessAwarenessFence.this.mIsAwarenessConnected = true;
                int unused2 = BrightnessAwarenessFence.this.mAwarenessReconnectTimes = 0;
                DElog.i(BrightnessAwarenessFence.TAG, "mAwarenessServiceConnection onServiceConnected...");
                boolean ret = BrightnessAwarenessFence.this.mAwarenessManager.registerLocationFence(BrightnessAwarenessFence.this.mAwarenessCallback, BrightnessAwarenessFence.this.mAwarenessFence, BrightnessAwarenessFence.this.mAwarenessPendingIntent);
                DElog.i(BrightnessAwarenessFence.TAG, "mIsAwarenessConnected, registerLocationFence ret = " + ret);
            }

            public void onServiceDisconnected() {
                boolean unused = BrightnessAwarenessFence.this.mIsAwarenessConnected = false;
                DElog.w(BrightnessAwarenessFence.TAG, "mAwarenessServiceConnection onServiceDisconnected...");
                if (BrightnessAwarenessFence.this.mAwarenessHandler == null) {
                    Handler unused2 = BrightnessAwarenessFence.this.mAwarenessHandler = new Handler();
                }
                DElog.w(BrightnessAwarenessFence.TAG, "wait 10000 ms to reconnect...");
                BrightnessAwarenessFence.this.mAwarenessHandler.postDelayed(new Runnable() {
                    public void run() {
                        int unused = BrightnessAwarenessFence.this.mAwarenessReconnectTimes = BrightnessAwarenessFence.this.mAwarenessReconnectTimes + 1;
                        if (!BrightnessAwarenessFence.this.mIsAwarenessConnected && BrightnessAwarenessFence.this.mAwarenessReconnectTimes < 3) {
                            DElog.w(BrightnessAwarenessFence.TAG, "mAwarenessHandler try connectService...");
                            if (!BrightnessAwarenessFence.this.mAwarenessManager.connectService(BrightnessAwarenessFence.this.mAwarenessServiceConnection)) {
                                DElog.w(BrightnessAwarenessFence.TAG, "connectService failed!");
                                BrightnessAwarenessFence.this.mAwarenessHandler.postDelayed(this, MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
                            }
                        }
                    }
                }, MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
            }
        };
    }

    public boolean initBootCompleteValues() {
        initAwareness();
        return this.mAwarenessManager.connectService(this.mAwarenessServiceConnection);
    }
}
