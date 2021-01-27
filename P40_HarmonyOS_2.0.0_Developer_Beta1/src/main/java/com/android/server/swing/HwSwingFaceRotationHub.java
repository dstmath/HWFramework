package com.android.server.swing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Slog;
import com.android.server.intellicom.common.SmartDualCardConsts;
import com.huawei.android.view.HwWindowManager;
import com.huawei.hiai.awareness.service.AwarenessManager;
import com.huawei.hiai.awareness.service.AwarenessServiceConnection;
import com.huawei.hiai.awareness.service.ExtendAwarenessFence;
import com.huawei.hiai.awareness.service.IAwarenessListener;
import com.huawei.hiai.awareness.service.IRequestCallBack;
import com.huawei.hiai.awareness.service.RequestResult;

public class HwSwingFaceRotationHub {
    private static final int DIRECTION_INIT = 100;
    private static final int RECONNECT_MAX_COUNT = 3;
    private static final int RECONNECT_WAIT_TIME_MS = 10000;
    private static final String TAG = "HwSwingFaceRotationHub";
    private static HwSwingFaceRotationHub sInstance;
    private IRequestCallBack mAwarenessCallback = new IRequestCallBack.Stub() {
        /* class com.android.server.swing.HwSwingFaceRotationHub.AnonymousClass1 */

        @Override // com.huawei.hiai.awareness.service.IRequestCallBack
        public void onRequestResult(RequestResult result) throws RemoteException {
            Slog.i(HwSwingFaceRotationHub.TAG, "registerFence() result = " + result);
        }
    };
    private Handler mAwarenessHandler;
    private IAwarenessListener mAwarenessListener = new IAwarenessListener.Stub() {
        /* class com.android.server.swing.HwSwingFaceRotationHub.AnonymousClass2 */

        @Override // com.huawei.hiai.awareness.service.IAwarenessListener
        public void handleEvent(ExtendAwarenessFence awarenessFence, Bundle result) {
            if (result == null) {
                Slog.e(HwSwingFaceRotationHub.TAG, "handleFenceResult result is null");
                return;
            }
            Bundle faceBundle = (Bundle) result.getParcelable("FENCE_SWING_STATUS_CHANGE");
            if (faceBundle != null) {
                int faceDirection = faceBundle.getInt("FaceDirection");
                Slog.i(HwSwingFaceRotationHub.TAG, "handleFenceResult FaceDirection " + faceDirection);
                HwSwingFaceRotationHub.this.notifySwingRotation(faceDirection);
            }
        }
    };
    private AwarenessManager mAwarenessManager;
    private int mAwarenessReconnectTimes;
    private AwarenessServiceConnection mAwarenessServiceConnection = new AwarenessServiceConnection() {
        /* class com.android.server.swing.HwSwingFaceRotationHub.AnonymousClass4 */

        @Override // com.huawei.hiai.awareness.service.AwarenessServiceConnection
        public void onServiceConnected() {
            HwSwingFaceRotationHub.this.mIsAwarenessConnected = true;
            HwSwingFaceRotationHub.this.mAwarenessReconnectTimes = 0;
            HwSwingFaceRotationHub.this.registerSwingFaceOrientationsFence();
            Slog.i(HwSwingFaceRotationHub.TAG, "mAwarenessServiceConnection onServiceConnected ");
        }

        @Override // com.huawei.hiai.awareness.service.AwarenessServiceConnection
        public void onServiceDisconnected() {
            HwSwingFaceRotationHub.this.mIsAwarenessConnected = false;
            if (HwSwingFaceRotationHub.this.isRotationLocked()) {
                HwSwingFaceRotationHub.this.notifySwingRotation(100);
                return;
            }
            Slog.w(HwSwingFaceRotationHub.TAG, "mAwarenessServiceConnection onServiceDisconnected, wait 10000 ms to reconnect.");
            HwSwingFaceRotationHub.this.mAwarenessHandler.postDelayed(new Runnable() {
                /* class com.android.server.swing.HwSwingFaceRotationHub.AnonymousClass4.AnonymousClass1 */

                @Override // java.lang.Runnable
                public void run() {
                    HwSwingFaceRotationHub.access$308(HwSwingFaceRotationHub.this);
                    if (HwSwingFaceRotationHub.this.mAwarenessReconnectTimes < 3) {
                        Slog.w(HwSwingFaceRotationHub.TAG, "mAwarenessHandler try connectService.");
                        if (!HwSwingFaceRotationHub.this.doConnectService()) {
                            Slog.w(HwSwingFaceRotationHub.TAG, "connectService failed!");
                            HwSwingFaceRotationHub.this.mAwarenessHandler.postDelayed(this, 10000);
                            return;
                        }
                        return;
                    }
                    HwSwingFaceRotationHub.this.notifySwingRotation(100);
                    Slog.w(HwSwingFaceRotationHub.TAG, "had try 10000 times connectService failed!");
                }
            }, 10000);
        }
    };
    private Context mContext;
    private int mCurrentUserId = 0;
    private boolean mIsAwarenessConnected;
    private ContentObserver mObserver = new ContentObserver(new Handler()) {
        /* class com.android.server.swing.HwSwingFaceRotationHub.AnonymousClass3 */

        @Override // android.database.ContentObserver
        public void onChange(boolean isSelfChange) {
            Slog.i(HwSwingFaceRotationHub.TAG, "accelerometer status changed");
            HwSwingFaceRotationHub.this.refreshAwarenessConnection();
        }
    };
    private int mRegisteredFenceAction = 0;
    private ExtendAwarenessFence mSwingFaceOrientationsFence;

    static /* synthetic */ int access$308(HwSwingFaceRotationHub x0) {
        int i = x0.mAwarenessReconnectTimes;
        x0.mAwarenessReconnectTimes = i + 1;
        return i;
    }

    private class SwingStatusReceiver extends BroadcastReceiver {
        private SwingStatusReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                Slog.e(HwSwingFaceRotationHub.TAG, "SwingStatusReceiver intent is null");
                return;
            }
            String action = intent.getAction();
            Slog.i(HwSwingFaceRotationHub.TAG, "on receive action : " + action);
            if (SmartDualCardConsts.SYSTEM_STATE_ACTION_USER_SWITCHED.equals(action)) {
                HwSwingFaceRotationHub.this.mCurrentUserId = intent.getIntExtra("android.intent.extra.user_handle", 0);
                HwSwingFaceRotationHub.this.refreshAwarenessConnection();
            }
        }
    }

    public static synchronized HwSwingFaceRotationHub getInstance(Context context) {
        HwSwingFaceRotationHub hwSwingFaceRotationHub;
        synchronized (HwSwingFaceRotationHub.class) {
            if (sInstance == null) {
                sInstance = new HwSwingFaceRotationHub(context);
            }
            hwSwingFaceRotationHub = sInstance;
        }
        return hwSwingFaceRotationHub;
    }

    private HwSwingFaceRotationHub(Context context) {
        Slog.i(TAG, "constructor");
        this.mContext = context;
        this.mAwarenessHandler = new Handler();
        this.mAwarenessManager = new AwarenessManager(this.mContext);
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("accelerometer_rotation"), true, this.mObserver, -1);
        IntentFilter filter = new IntentFilter();
        filter.addAction(SmartDualCardConsts.SYSTEM_STATE_ACTION_USER_SWITCHED);
        this.mContext.registerReceiver(new SwingStatusReceiver(), filter);
    }

    public void start() {
        Slog.i(TAG, "start HwSwingFaceRotationHub");
        refreshAwarenessConnection();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isRotationLocked() {
        boolean isLocked = false;
        if (Settings.System.getIntForUser(this.mContext.getContentResolver(), "accelerometer_rotation", 0, this.mCurrentUserId) == 0) {
            isLocked = true;
        }
        Slog.w(TAG, "isRotationLocked " + isLocked + " mCurrentUserId: " + this.mCurrentUserId);
        return isLocked;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void refreshAwarenessConnection() {
        int action = updateFaceRotationAwarenessAction();
        if (action == 0) {
            Slog.i(TAG, "refreshAwarenessConnection, no action need to register");
            unregisterFaceRotationFence();
            if (this.mIsAwarenessConnected) {
                this.mAwarenessManager.disconnectService();
                return;
            }
            return;
        }
        Slog.i(TAG, "refreshAwarenessConnection, action " + action + " need to register");
        if (action == this.mRegisteredFenceAction) {
            doConnectService();
            return;
        }
        this.mRegisteredFenceAction = action;
        this.mSwingFaceOrientationsFence = new ExtendAwarenessFence(13, 5, this.mRegisteredFenceAction, null);
        doConnectService();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean doConnectService() {
        if (!this.mIsAwarenessConnected) {
            return this.mAwarenessManager.connectService(this.mAwarenessServiceConnection);
        }
        Slog.w(TAG, "service already connected!");
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifySwingRotation(final int rotation) {
        this.mAwarenessHandler.post(new Runnable() {
            /* class com.android.server.swing.HwSwingFaceRotationHub.AnonymousClass5 */

            @Override // java.lang.Runnable
            public void run() {
                try {
                    HwWindowManager.getService().notifySwingRotation(rotation);
                } catch (RemoteException e) {
                    Slog.e(HwSwingFaceRotationHub.TAG, "handleFenceResult transit failed");
                }
            }
        });
    }

    private int updateFaceRotationAwarenessAction() {
        if (isRotationLocked()) {
            return 0;
        }
        return 0 | 1;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void registerSwingFaceOrientationsFence() {
        if (!this.mIsAwarenessConnected) {
            Slog.i(TAG, "Awareness not connected");
        } else {
            this.mAwarenessManager.registerAwarenessListener(this.mAwarenessCallback, this.mSwingFaceOrientationsFence, this.mAwarenessListener);
        }
    }

    private void unregisterFaceRotationFence() {
        if (!this.mIsAwarenessConnected) {
            Slog.i(TAG, "Awareness not connected");
        } else {
            this.mAwarenessManager.unRegisterAwarenessListener(this.mAwarenessCallback, this.mSwingFaceOrientationsFence, this.mAwarenessListener);
        }
    }
}
