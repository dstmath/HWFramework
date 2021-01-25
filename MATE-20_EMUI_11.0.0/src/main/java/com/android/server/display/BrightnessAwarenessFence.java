package com.android.server.display;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.RemoteException;
import com.android.server.display.HwBrightnessSceneRecognition;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.huawei.displayengine.DeLog;
import com.huawei.hiai.awareness.service.AwarenessFence;
import com.huawei.hiai.awareness.service.AwarenessManager;
import com.huawei.hiai.awareness.service.AwarenessServiceConnection;
import com.huawei.hiai.awareness.service.IRequestCallBack;
import com.huawei.hiai.awareness.service.RequestResult;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/* access modifiers changed from: package-private */
public class BrightnessAwarenessFence {
    private static final String AWARENESS_STATUS_CHANGE_ACTION = "com.huawei.server.display.awareness_status_change";
    private static final String BROADCAST_PERMISSION = "com.huawei.permission.CONFIG.BRIGHTNESS";
    private static final int RECONNECT_MAX_COUNT = 5;
    private static final int RECOONECT_WAIT_TIME_MS = 10000;
    private static final int REGESTERATION_TIME_OUT_MILLISECONDS = 2000;
    private static final String TAG = "DE J AwarenessFence";
    private AwarenessBroadcastReceiver mAwarenessBroadcastReceiver;
    private IRequestCallBack mAwarenessCallback;
    private AwarenessFence mAwarenessFence;
    private Handler mAwarenessHandler = new Handler();
    private int mAwarenessLocationStatus = -1;
    private AwarenessManager mAwarenessManager;
    private PendingIntent mAwarenessPendingIntent;
    private int mAwarenessReconnectTimes;
    private AwarenessServiceConnection mAwarenessServiceConnection;
    private final Context mContext;
    private final HwBrightnessSceneRecognition mHwBrightnessSceneRecognition;
    private volatile boolean mIsAwarenessConnected;
    private volatile boolean mNeedPendingRegisteration = false;

    static /* synthetic */ int access$708(BrightnessAwarenessFence x0) {
        int i = x0.mAwarenessReconnectTimes;
        x0.mAwarenessReconnectTimes = i + 1;
        return i;
    }

    public BrightnessAwarenessFence(Context context, HwBrightnessSceneRecognition hwBrightnessSceneRecognition) {
        this.mContext = context;
        this.mHwBrightnessSceneRecognition = hwBrightnessSceneRecognition;
    }

    /* access modifiers changed from: private */
    public class AwarenessBroadcastReceiver extends BroadcastReceiver {
        public AwarenessBroadcastReceiver() {
            registerAwarenessReceiver();
        }

        private void registerAwarenessReceiver() {
            BrightnessAwarenessFence.this.mContext.registerReceiver(this, new IntentFilter(BrightnessAwarenessFence.AWARENESS_STATUS_CHANGE_ACTION), BrightnessAwarenessFence.BROADCAST_PERMISSION, null);
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (BrightnessAwarenessFence.this.mIsAwarenessConnected) {
                BrightnessAwarenessFence.this.handleFenceResult();
            } else {
                DeLog.e(BrightnessAwarenessFence.TAG, "mIsAwarenessConnected is false!");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFenceResult() {
        int triggerStatus = 0;
        int locationStatus = -1;
        RequestResult triggerResult = this.mAwarenessManager.getFenceTriggerResult(this.mAwarenessFence, this.mAwarenessPendingIntent);
        if (triggerResult != null) {
            triggerStatus = triggerResult.getTriggerStatus();
            locationStatus = triggerResult.getStatus();
        }
        if (triggerStatus != 2) {
            this.mNeedPendingRegisteration = !registerAwarenessFence();
        }
        if (triggerStatus == 1 && locationStatus != this.mAwarenessLocationStatus) {
            this.mAwarenessLocationStatus = locationStatus;
            String tag = HwBrightnessSceneRecognition.SceneTag.LOCATION_UNKNOWN;
            if (this.mAwarenessLocationStatus == 1) {
                tag = HwBrightnessSceneRecognition.SceneTag.LOCATION_HOME;
            }
            if (this.mAwarenessLocationStatus == 2) {
                tag = HwBrightnessSceneRecognition.SceneTag.LOCATION_NOT_HOME;
            }
            this.mHwBrightnessSceneRecognition.setLocationStatus(tag);
        }
        DeLog.i(TAG, "Awareness registerReceiver, triggerResult = " + triggerResult);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean registerAwarenessFence() {
        ExecutorService pool = Executors.newFixedThreadPool(1);
        try {
            if (pool.isShutdown()) {
                return false;
            }
            FutureTask<Boolean> futureTask = new FutureTask<>(new AwarenessFenceRegisterationTask());
            pool.execute(futureTask);
            Boolean result = futureTask.get(2000, TimeUnit.MILLISECONDS);
            pool.shutdownNow();
            if (result != null) {
                return result.booleanValue();
            }
            return false;
        } catch (InterruptedException e) {
            DeLog.e(TAG, "registerAwarenessFence thread interrupt exception");
            return false;
        } catch (ExecutionException e2) {
            DeLog.e(TAG, "registerAwarenessFence thread execution exception");
            return false;
        } catch (TimeoutException e3) {
            DeLog.e(TAG, "registerAwarenessFence timeout exception");
            pool.shutdownNow();
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public class AwarenessFenceRegisterationTask implements Callable<Boolean> {
        AwarenessFenceRegisterationTask() {
        }

        @Override // java.util.concurrent.Callable
        public Boolean call() throws Exception {
            DeLog.i(BrightnessAwarenessFence.TAG, "AwarenessFenceRegisterationTask starts");
            boolean ret = BrightnessAwarenessFence.this.mAwarenessManager.registerLocationFence(BrightnessAwarenessFence.this.mAwarenessCallback, BrightnessAwarenessFence.this.mAwarenessFence, BrightnessAwarenessFence.this.mAwarenessPendingIntent);
            DeLog.i(BrightnessAwarenessFence.TAG, "AwarenessFenceRegisterationTask ret = " + ret);
            return Boolean.valueOf(ret);
        }
    }

    private void initAwareness() {
        this.mAwarenessManager = new AwarenessManager(this.mContext);
        this.mAwarenessBroadcastReceiver = new AwarenessBroadcastReceiver();
        this.mAwarenessFence = new AwarenessFence(6, 3, 3, null);
        this.mAwarenessPendingIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(AWARENESS_STATUS_CHANGE_ACTION), 0);
        this.mAwarenessCallback = new IRequestCallBack.Stub() {
            /* class com.android.server.display.BrightnessAwarenessFence.AnonymousClass1 */

            @Override // com.huawei.hiai.awareness.service.IRequestCallBack
            public void onRequestResult(RequestResult result) throws RemoteException {
                DeLog.i(BrightnessAwarenessFence.TAG, "registerFence() result = " + result);
            }
        };
        this.mAwarenessServiceConnection = new AwarenessServiceConnection() {
            /* class com.android.server.display.BrightnessAwarenessFence.AnonymousClass2 */

            @Override // com.huawei.hiai.awareness.service.AwarenessServiceConnection
            public void onServiceConnected() {
                BrightnessAwarenessFence.this.mIsAwarenessConnected = true;
                BrightnessAwarenessFence.this.mAwarenessReconnectTimes = 0;
                DeLog.i(BrightnessAwarenessFence.TAG, "mAwarenessServiceConnection onServiceConnected...");
                BrightnessAwarenessFence brightnessAwarenessFence = BrightnessAwarenessFence.this;
                brightnessAwarenessFence.mNeedPendingRegisteration = !brightnessAwarenessFence.registerAwarenessFence();
                DeLog.i(BrightnessAwarenessFence.TAG, "mIsAwarenessConnected, registerLocationFence ret = " + (true ^ BrightnessAwarenessFence.this.mNeedPendingRegisteration));
            }

            @Override // com.huawei.hiai.awareness.service.AwarenessServiceConnection
            public void onServiceDisconnected() {
                BrightnessAwarenessFence.this.mIsAwarenessConnected = false;
                DeLog.w(BrightnessAwarenessFence.TAG, "mAwarenessServiceConnection onServiceDisconnected, wait 10000 ms to reconnect...");
                BrightnessAwarenessFence.this.mAwarenessHandler.postDelayed(new Runnable() {
                    /* class com.android.server.display.BrightnessAwarenessFence.AnonymousClass2.AnonymousClass1 */

                    @Override // java.lang.Runnable
                    public void run() {
                        BrightnessAwarenessFence.access$708(BrightnessAwarenessFence.this);
                        if (!BrightnessAwarenessFence.this.mIsAwarenessConnected && BrightnessAwarenessFence.this.mAwarenessReconnectTimes < 5) {
                            DeLog.w(BrightnessAwarenessFence.TAG, "mAwarenessHandler retry " + BrightnessAwarenessFence.this.mAwarenessReconnectTimes + " time connectService...");
                            BrightnessAwarenessFence.this.mAwarenessManager.connectService(BrightnessAwarenessFence.this.mAwarenessServiceConnection);
                            BrightnessAwarenessFence.this.mAwarenessHandler.postDelayed(this, HwArbitrationDEFS.TIMEOUT_FOR_QUERY_QOE_WM);
                        }
                    }
                }, HwArbitrationDEFS.TIMEOUT_FOR_QUERY_QOE_WM);
            }
        };
    }

    public boolean initBootCompleteValues() {
        initAwareness();
        return this.mAwarenessManager.connectService(this.mAwarenessServiceConnection);
    }

    public void onScreenStatusChanged() {
        if (this.mNeedPendingRegisteration) {
            this.mNeedPendingRegisteration = false;
            this.mNeedPendingRegisteration = !registerAwarenessFence();
        }
    }
}
