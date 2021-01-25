package com.huawei.server.camera;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.cover.CoverManager;
import android.cover.HallState;
import android.cover.IHallCallback;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import com.huawei.utils.HwPartResourceUtils;

public class HwCameraManualImpl {
    private static final String HW_CAMERA_NAME = "com.huawei.camera";
    private static final String SYSTEM_UI_NAME = "com.android.systemui";
    private static final String TAG = "HwCameraManaulImpl";
    private static final int TIPS_TYPE = 0;
    private static final int TOAST_TYPE = 1;
    private Context cameraContext = null;
    private CoverManager coverManager = null;
    private BroadcastReceiver coverStateReceiver = new BroadcastReceiver() {
        /* class com.huawei.server.camera.HwCameraManualImpl.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                Log.w(HwCameraManualImpl.TAG, "onReceive, the intent is null!");
                return;
            }
            Log.i(HwCameraManualImpl.TAG, "onReceive");
            HwCameraManualImpl.this.dismissSlidedownTip();
        }
    };
    private Window dialogWindow = null;
    private IHallCallback.Stub hallCallback = new IHallCallback.Stub() {
        /* class com.huawei.server.camera.HwCameraManualImpl.AnonymousClass2 */

        public void onStateChange(HallState hallState) {
            if (hallState.state == 2) {
                Log.i(HwCameraManualImpl.TAG, "hallState SLIDE_HALL_OPEN");
                HwCameraManualImpl.this.dismissSlidedownTip();
            }
        }
    };
    private Handler handler = null;
    private View imageView = null;
    private IntentFilter intentFilter = null;
    private Dialog tipDialog = null;

    public HwCameraManualImpl(Context context) {
        this.cameraContext = context;
        this.coverManager = new CoverManager();
        this.handler = new Handler(Looper.getMainLooper());
        this.handler.post(new Runnable() {
            /* class com.huawei.server.camera.$$Lambda$HwCameraManualImpl$8ARUmEeG8dDKItHvexmKqvmKjU */

            @Override // java.lang.Runnable
            public final void run() {
                HwCameraManualImpl.this.lambda$new$1$HwCameraManualImpl();
            }
        });
    }

    public /* synthetic */ void lambda$new$1$HwCameraManualImpl() {
        Context context = this.cameraContext;
        if (context == null || this.coverManager == null) {
            Log.w(TAG, "Context == null or coverManager == null");
            return;
        }
        this.tipDialog = new Dialog(context.getApplicationContext());
        this.tipDialog.requestWindowFeature(1);
        this.tipDialog.setContentView(HwPartResourceUtils.getResourceId("frontcamera_slide_tip"));
        this.tipDialog.setCanceledOnTouchOutside(true);
        this.dialogWindow = this.tipDialog.getWindow();
        this.dialogWindow.setType(2009);
        this.dialogWindow.setGravity(48);
        this.dialogWindow.setBackgroundDrawable(new ColorDrawable(TIPS_TYPE));
        this.dialogWindow.addFlags(524288);
        this.intentFilter = new IntentFilter();
        this.intentFilter.addAction("android.intent.action.SCREEN_OFF");
        this.intentFilter.addAction("android.intent.action.CLOSE_SYSTEM_DIALOGS");
        this.tipDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            /* class com.huawei.server.camera.$$Lambda$HwCameraManualImpl$fbuBBsZkW0yxAucgo425KjAKysc */

            @Override // android.content.DialogInterface.OnKeyListener
            public final boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                return HwCameraManualImpl.this.lambda$null$0$HwCameraManualImpl(dialogInterface, i, keyEvent);
            }
        });
    }

    public /* synthetic */ boolean lambda$null$0$HwCameraManualImpl(DialogInterface dialog, int keyCode, KeyEvent event) {
        if (keyCode != 4) {
            return false;
        }
        Log.i(TAG, "onKeyBack");
        dismissSlidedownTip();
        return false;
    }

    private void setType(int type) {
        this.handler.post(new Runnable(type) {
            /* class com.huawei.server.camera.$$Lambda$HwCameraManualImpl$LawaeweMTrtszgA3bwSzWpTmA */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                HwCameraManualImpl.this.lambda$setType$2$HwCameraManualImpl(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$setType$2$HwCameraManualImpl(int type) {
        Log.i(TAG, "setType " + type);
        Dialog dialog = this.tipDialog;
        if (dialog == null) {
            Log.w(TAG, "tipDialog = null");
            return;
        }
        if (this.imageView == null) {
            this.imageView = dialog.findViewById(HwPartResourceUtils.getResourceId("frontcamera_slide_imageview"));
            if (this.imageView == null) {
                return;
            }
        }
        this.imageView.setVisibility(type == 0 ? TIPS_TYPE : 8);
    }

    public void registerSlideHallService() {
        boolean isRegistered = this.coverManager.registerHallCallback("cameraserver", 1, this.hallCallback);
        Log.i(TAG, "regesiterService end with result = " + isRegistered + " and Callback = " + this.hallCallback.toString());
    }

    private void showSlidedownTip() {
        this.handler.post(new Runnable() {
            /* class com.huawei.server.camera.$$Lambda$HwCameraManualImpl$gVFjehPQdbwlUGHj7F1KYmg5Ho */

            @Override // java.lang.Runnable
            public final void run() {
                HwCameraManualImpl.this.lambda$showSlidedownTip$3$HwCameraManualImpl();
            }
        });
    }

    public /* synthetic */ void lambda$showSlidedownTip$3$HwCameraManualImpl() {
        Dialog dialog = this.tipDialog;
        if (dialog != null && !dialog.isShowing() && this.cameraContext != null) {
            Log.i(TAG, "show tipDialog = " + this.tipDialog.toString());
            this.cameraContext.registerReceiver(this.coverStateReceiver, this.intentFilter);
            this.tipDialog.show();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dismissSlidedownTip() {
        this.handler.post(new Runnable() {
            /* class com.huawei.server.camera.$$Lambda$HwCameraManualImpl$g6G4MWvAPGQ0BKplpoiMNigV5O8 */

            @Override // java.lang.Runnable
            public final void run() {
                HwCameraManualImpl.this.lambda$dismissSlidedownTip$4$HwCameraManualImpl();
            }
        });
    }

    public /* synthetic */ void lambda$dismissSlidedownTip$4$HwCameraManualImpl() {
        Dialog dialog = this.tipDialog;
        if (dialog == null || !dialog.isShowing()) {
            Log.i(TAG, "tipDialog = null");
            return;
        }
        Log.i(TAG, "dismiss tipDialog = " + this.tipDialog.toString());
        this.tipDialog.dismiss();
    }

    public void unRegisterSlideHallService() {
        dismissSlidedownTip();
        this.handler.post(new Runnable() {
            /* class com.huawei.server.camera.$$Lambda$HwCameraManualImpl$APLrMP_fHdY8OHGlr1Xz1lEUYc */

            @Override // java.lang.Runnable
            public final void run() {
                HwCameraManualImpl.this.lambda$unRegisterSlideHallService$5$HwCameraManualImpl();
            }
        });
        boolean isRegistered = this.coverManager.unRegisterHallCallbackEx(1, this.hallCallback);
        Log.i(TAG, "unRegisterHallCallbackEx result = " + isRegistered + " and Callback = " + this.hallCallback.toString());
    }

    public /* synthetic */ void lambda$unRegisterSlideHallService$5$HwCameraManualImpl() {
        try {
            if (this.coverStateReceiver != null && this.cameraContext != null) {
                this.cameraContext.unregisterReceiver(this.coverStateReceiver);
                this.coverStateReceiver = null;
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "coverStateReceiver.unregisterReceiver end with IllegalArgumentException");
        }
    }

    public void handleCameraState(int newCameraState, int facing, String clientName) {
        if (newCameraState == 0) {
            registerSlideHallService();
            if (facing == 1 && !SYSTEM_UI_NAME.equals(clientName) && this.coverManager.getHallState(1) == 0) {
                setType(1 ^ (HW_CAMERA_NAME.equals(clientName) ? 1 : 0));
                showSlidedownTip();
            }
        } else if (newCameraState == 3) {
            unRegisterSlideHallService();
        }
    }
}
