package com.huawei.aod;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.RemoteException;
import android.util.Slog;
import com.android.server.swing.HwSwingMotionGestureConstant;
import com.huawei.hiai.awareness.client.AwarenessEnvelope;
import com.huawei.hiai.awareness.client.AwarenessFence;
import com.huawei.hiai.awareness.client.AwarenessManager;
import com.huawei.hiai.awareness.client.AwarenessRequest;
import com.huawei.hiai.awareness.client.AwarenessResult;
import com.huawei.hiai.awareness.client.AwarenessServiceConnection;
import com.huawei.hiai.awareness.client.FenceState;
import com.huawei.hiai.awareness.client.OnEnvelopeReceiver;
import com.huawei.hiai.awareness.client.OnResultListener;

public class AwarenessClient {
    private static final Object CONNECT_LOCK = new Object();
    private static final String PERMISSION_AWARENESS_SERVICE_CREATE = "com.huawei.hiai.awareness.permission.NOTIFY_RESTART_SERVICE";
    private static final String TAG = AwarenessClient.class.getSimpleName();
    private AwarenessFence mAwarenessFence = null;
    private AwarenessManager mAwarenessManager = null;
    private AwarenessServiceConnection mAwarenessServiceConnection = new AwarenessServiceConnection() {
        /* class com.huawei.aod.AwarenessClient.AnonymousClass3 */

        @Override // com.huawei.hiai.awareness.client.AwarenessServiceConnection
        public void onConnected() {
            synchronized (AwarenessClient.CONNECT_LOCK) {
                if (!AwarenessClient.this.mIsConnected) {
                    AwarenessClient.this.mIsConnected = true;
                }
                AwarenessRequest awarenessRequest = AwarenessRequest.registerFence(AwarenessClient.this.getEyeGazeFence(), AwarenessClient.this.onEnvelopeReceiver);
                awarenessRequest.addOnResultListener(AwarenessClient.this.onResultListener);
                boolean dispatchRst = AwarenessClient.this.mAwarenessManager.dispatch(awarenessRequest);
                String str = AwarenessClient.TAG;
                Slog.i(str, "onConnected dispatch return " + dispatchRst);
            }
        }

        @Override // com.huawei.hiai.awareness.client.AwarenessServiceConnection
        public void onDisconnected() {
            AwarenessClient.this.mIsConnected = false;
            String str = AwarenessClient.TAG;
            Slog.d(str, "onDisconnected: mIsConnected=" + AwarenessClient.this.mIsConnected);
        }
    };
    private CACreateReceiver mAwarenessServiceCreateReceiver = null;
    private Context mContext;
    private boolean mIsConnected = false;
    private OnEnvelopeReceiver onEnvelopeReceiver = new OnEnvelopeReceiver.Stub() {
        /* class com.huawei.aod.AwarenessClient.AnonymousClass2 */

        @Override // com.huawei.hiai.awareness.client.OnEnvelopeReceiver
        public void onReceive(AwarenessEnvelope awarenessEnvelope) throws RemoteException {
            AwarenessFence awarenessFenceLocal = AwarenessFence.parseFrom(awarenessEnvelope);
            if (awarenessFenceLocal != null) {
                FenceState state = awarenessFenceLocal.getState();
                String str = AwarenessClient.TAG;
                Slog.i(str, "AwarenessClient OnEnvelopeReceiver.onReceive:" + state.toString());
            }
        }
    };
    private OnResultListener onResultListener = new OnResultListener.Stub() {
        /* class com.huawei.aod.AwarenessClient.AnonymousClass1 */

        @Override // com.huawei.hiai.awareness.client.OnResultListener
        public void onResult(AwarenessResult awarenessResult) throws RemoteException {
            String str = AwarenessClient.TAG;
            Slog.i(str, "onResultListener: " + awarenessResult.toString());
        }
    };

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private AwarenessFence getEyeGazeFence() {
        if (this.mAwarenessFence == null) {
            this.mAwarenessFence = AwarenessFence.create("eye_gaze_fence").putArg("people", "single").putArg(HwSwingMotionGestureConstant.KEY_SCREEN_DISPLAY, HwSwingMotionGestureConstant.VALUE_SCREEN_OFF);
        }
        return this.mAwarenessFence;
    }

    public AwarenessClient(Context context) {
        if (context != null) {
            this.mContext = context;
            this.mAwarenessManager = new AwarenessManager(this.mContext);
        }
    }

    public void startAwarenessManager() {
        synchronized (CONNECT_LOCK) {
            if (this.mIsConnected) {
                Slog.i(TAG, "startAwarenessManager, already connected, return.");
                return;
            }
            if (this.mAwarenessManager == null) {
                this.mAwarenessManager = new AwarenessManager(this.mContext);
            }
            this.mIsConnected = this.mAwarenessManager.connectService(this.mAwarenessServiceConnection);
            initCaBootReceiver();
            String str = TAG;
            Slog.i(str, "startAwarenessManager call connectService return connectRet:" + this.mIsConnected);
        }
    }

    public void stopAwarenessManager() {
        synchronized (CONNECT_LOCK) {
            cancelCaBootReceiver();
            if (this.mIsConnected) {
                if (this.mAwarenessManager != null) {
                    boolean disconnectRet = false;
                    boolean dispatchRet = this.mAwarenessManager.dispatch(AwarenessRequest.unregisterFence(getEyeGazeFence()));
                    if (dispatchRet) {
                        disconnectRet = this.mAwarenessManager.disconnectService();
                    }
                    this.mIsConnected = false;
                    String str = TAG;
                    Slog.i(str, "stopAwarenessManager call dispatch return " + dispatchRet + ", call disconnectService return:" + disconnectRet);
                    return;
                }
            }
            Slog.i(TAG, "stopAwarenessManager, already disconnected, return.");
        }
    }

    private void initCaBootReceiver() {
        if (this.mAwarenessServiceCreateReceiver == null) {
            this.mAwarenessServiceCreateReceiver = new CACreateReceiver();
        }
        Context context = this.mContext;
        CACreateReceiver cACreateReceiver = this.mAwarenessServiceCreateReceiver;
        context.registerReceiver(cACreateReceiver, cACreateReceiver.getFilter(), "com.huawei.hiai.awareness.permission.NOTIFY_RESTART_SERVICE", null);
    }

    private void cancelCaBootReceiver() {
        CACreateReceiver cACreateReceiver = this.mAwarenessServiceCreateReceiver;
        if (cACreateReceiver != null) {
            this.mContext.unregisterReceiver(cACreateReceiver);
        }
    }

    /* access modifiers changed from: private */
    public class CACreateReceiver extends BroadcastReceiver {
        private static final String ACTION_AWARENESS_SERVICE_CREATE = "com.huawei.hiai.awareness.action.service.create";
        private static final String AWARENESS_SERVICE_CREATE_TYPE = "awareness_service_create_type";
        private static final int DEFAULT_VALUE = -1;
        private static final int SERVICE_EXP_RESTART = 2;
        private static final int SYSTEM_BOOT_CREATE = 1;

        private CACreateReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                Slog.e(AwarenessClient.TAG, "AwarenessServiceCreateReceiver onReceive intent is null");
                return;
            }
            String action = intent.getAction();
            if (action == null) {
                Slog.e(AwarenessClient.TAG, "AwarenessServiceCreateReceiver onReceive action is null");
                return;
            }
            int notifyType = -1;
            if ("com.huawei.hiai.awareness.action.service.create".equals(action)) {
                notifyType = intent.getIntExtra("awareness_service_create_type", -1);
            }
            String str = AwarenessClient.TAG;
            Slog.w(str, "Awareness engine system crash: notifyType = " + notifyType);
            if (notifyType == 1 || notifyType == 2) {
                synchronized (AwarenessClient.CONNECT_LOCK) {
                    if (!AwarenessClient.this.mIsConnected) {
                        AwarenessClient.this.mIsConnected = AwarenessClient.this.mAwarenessManager.connectService(AwarenessClient.this.mAwarenessServiceConnection);
                    }
                }
            }
        }

        public IntentFilter getFilter() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("com.huawei.hiai.awareness.action.service.create");
            return filter;
        }
    }
}
