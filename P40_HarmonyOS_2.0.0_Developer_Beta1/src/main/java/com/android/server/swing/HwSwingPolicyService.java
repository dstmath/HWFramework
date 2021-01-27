package com.android.server.swing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.swing.IHwSwingService;
import android.util.Log;
import android.view.KeyEvent;
import com.android.server.FgThread;
import com.android.server.appactcontrol.AppActConstant;

public class HwSwingPolicyService extends IHwSwingService.Stub {
    private static final String CHARACTERISTICS = SystemProperties.get("ro.build.characteristics", AppActConstant.VALUE_DEFAULT);
    private static final boolean IS_SWING_FACE_DISABLED = SystemProperties.getBoolean("hw.swing.face_rotate_disabled", false);
    private static final boolean IS_TV = "tv".equals(CHARACTERISTICS);
    public static final int SWING_ENABLE = 1;
    private static final String TAG = "HwSwingPolicyService";
    private BroadcastReceiver mBootCompletedReceiver = new BroadcastReceiver() {
        /* class com.android.server.swing.HwSwingPolicyService.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Log.i(HwSwingPolicyService.TAG, "on receive bootCompleted");
            HwSwingPolicyService.this.mHandler.post(new Runnable() {
                /* class com.android.server.swing.HwSwingPolicyService.AnonymousClass1.AnonymousClass1 */

                @Override // java.lang.Runnable
                public void run() {
                    HwSwingPolicyService.this.startSwingHub();
                }
            });
        }
    };
    private Context mContext;
    private HwSwingEyeGazeHub mEyeGazeHub;
    private Handler mHandler = null;
    private HwSwingKidsEyeHub mKidsEyeHub;
    private HwSwingFaceRotationHub mSwingFaceRotationHub;
    private HwSwingMotionGestureBaseHub mSwingMotionGestureHub;
    private HwSwingSystemUIHub mSystemUIHub;

    public HwSwingPolicyService(Context context) {
        this.mContext = context;
        this.mHandler = new Handler(FgThread.get().getLooper());
        this.mContext.registerReceiverAsUser(this.mBootCompletedReceiver, UserHandle.ALL, new IntentFilter("android.intent.action.BOOT_COMPLETED"), null, null);
        HwSwingReport.setContext(context);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startSwingHub() {
        if (IS_TV) {
            this.mSwingMotionGestureHub = HwTvSwingMotionGestureHub.getInstance(this.mContext);
        } else {
            this.mSwingMotionGestureHub = HwSwingMotionGestureHub.getInstance(this.mContext);
        }
        this.mSwingMotionGestureHub.start();
        if (!IS_SWING_FACE_DISABLED) {
            this.mSwingFaceRotationHub = HwSwingFaceRotationHub.getInstance(this.mContext);
            this.mSwingFaceRotationHub.start();
        }
        if (this.mEyeGazeHub == null) {
            this.mEyeGazeHub = new HwSwingEyeGazeHub(this.mContext);
            this.mEyeGazeHub.start();
        }
        this.mKidsEyeHub = HwSwingKidsEyeHub.getInstance(this.mContext);
        this.mKidsEyeHub.start();
        if (this.mSystemUIHub == null) {
            this.mSystemUIHub = new HwSwingSystemUIHub(this.mContext);
            this.mSystemUIHub.start();
        }
    }

    public boolean dispatchUnhandledKey(KeyEvent event, String pkgName) {
        if (UserHandle.getAppId(Binder.getCallingUid()) != 1000) {
            Log.e(TAG, "you have no permission to call dispatchUnhandledKey from uid:" + Binder.getCallingUid());
            return false;
        }
        HwSwingMotionGestureBaseHub hwSwingMotionGestureBaseHub = this.mSwingMotionGestureHub;
        if (hwSwingMotionGestureBaseHub != null) {
            return hwSwingMotionGestureBaseHub.dispatchUnhandledKey(event, pkgName);
        }
        return false;
    }

    public void notifyRotationChange(int rotation) {
        if (UserHandle.getAppId(Binder.getCallingUid()) != 1000) {
            Log.e(TAG, "you have no permission to call notifyRotationChange from uid:" + Binder.getCallingUid());
            return;
        }
        HwSwingMotionGestureBaseHub hwSwingMotionGestureBaseHub = this.mSwingMotionGestureHub;
        if (hwSwingMotionGestureBaseHub != null) {
            hwSwingMotionGestureBaseHub.notifyRotationChange(rotation);
        }
    }

    public void notifyFingersTouching(boolean isTouching) {
        HwSwingEyeGazeHub hwSwingEyeGazeHub;
        if (UserHandle.getAppId(Binder.getCallingUid()) != 1000) {
            Log.e(TAG, "you have no permission to call notifyFingersTouching from uid:" + Binder.getCallingUid());
            return;
        }
        HwSwingMotionGestureBaseHub hwSwingMotionGestureBaseHub = this.mSwingMotionGestureHub;
        if (hwSwingMotionGestureBaseHub != null && hwSwingMotionGestureBaseHub.getSwingSlideScreenEnable()) {
            this.mSwingMotionGestureHub.notifyFingersTouching(isTouching);
        }
        if (!isTouching && (hwSwingEyeGazeHub = this.mEyeGazeHub) != null && hwSwingEyeGazeHub.getEyeGazeSwitchEnable()) {
            this.mEyeGazeHub.unregisterEyeGazeFenceByTouch();
        }
    }

    public void notifyFocusChange(String focusWindowTitle, String focusPkgName) {
        if (UserHandle.getAppId(Binder.getCallingUid()) != 1000) {
            Log.e(TAG, "you have no permission to call notifyFocusChange from uid:" + Binder.getCallingUid());
            return;
        }
        HwSwingMotionGestureBaseHub hwSwingMotionGestureBaseHub = this.mSwingMotionGestureHub;
        if (hwSwingMotionGestureBaseHub != null) {
            hwSwingMotionGestureBaseHub.notifyFocusChange(focusWindowTitle, focusPkgName);
        }
    }
}
