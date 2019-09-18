package com.android.server.camera;

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
import android.util.Slog;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import java.lang.annotation.RCUnownedRef;
import java.lang.annotation.RCUnownedThisRef;

public class HwCameraManaulImpl {
    private static final String HW_CAMERA_NAME = "com.huawei.camera";
    private static final String SYSTEM_UI_NAME = "com.android.systemui";
    private static final String TAG = "HwCameraManaulImpl";
    private static final int TIPS_TYPE = 0;
    private static final int TOAST_TYPE = 1;
    /* access modifiers changed from: private */
    public BroadcastReceiver coverStateReceiver = new BroadcastReceiver() {
        @RCUnownedThisRef
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                Slog.w(HwCameraManaulImpl.TAG, "onReceive, the intent is null!");
                return;
            }
            Slog.w(HwCameraManaulImpl.TAG, "onReceive");
            HwCameraManaulImpl.this.dismissSlidedownTip();
        }
    };
    /* access modifiers changed from: private */
    @RCUnownedRef
    public Window dialogWindow;
    private Handler handler;
    /* access modifiers changed from: private */
    public View imageView;
    /* access modifiers changed from: private */
    public IntentFilter intentFilter = null;
    private IHallCallback.Stub mCallback = new IHallCallback.Stub() {
        @RCUnownedThisRef
        public void onStateChange(HallState hallState) {
            if (hallState.state == 2) {
                Slog.w(HwCameraManaulImpl.TAG, "hallState SLIDE_HALL_OPEN");
                HwCameraManaulImpl.this.dismissSlidedownTip();
            }
        }
    };
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public CoverManager mCoverManager;
    private boolean ret = false;
    /* access modifiers changed from: private */
    public Dialog tipDialog;

    public HwCameraManaulImpl(Context context) {
        this.mContext = context;
        this.mCoverManager = new CoverManager();
        this.handler = new Handler(Looper.getMainLooper());
        this.handler.post(new Runnable() {
            public void run() {
                if (HwCameraManaulImpl.this.mContext == null || HwCameraManaulImpl.this.mCoverManager == null) {
                    Slog.w(HwCameraManaulImpl.TAG, "Context == null or coverManager == null");
                    return;
                }
                Dialog unused = HwCameraManaulImpl.this.tipDialog = new Dialog(HwCameraManaulImpl.this.mContext.getApplicationContext());
                HwCameraManaulImpl.this.tipDialog.requestWindowFeature(1);
                HwCameraManaulImpl.this.tipDialog.setContentView(34013297);
                HwCameraManaulImpl.this.tipDialog.setCanceledOnTouchOutside(true);
                Window unused2 = HwCameraManaulImpl.this.dialogWindow = HwCameraManaulImpl.this.tipDialog.getWindow();
                HwCameraManaulImpl.this.dialogWindow.setType(HwArbitrationDEFS.MSG_MPLINK_UNBIND_SUCCESS);
                HwCameraManaulImpl.this.dialogWindow.setGravity(48);
                HwCameraManaulImpl.this.dialogWindow.setBackgroundDrawable(new ColorDrawable(0));
                HwCameraManaulImpl.this.dialogWindow.addFlags(524288);
                IntentFilter unused3 = HwCameraManaulImpl.this.intentFilter = new IntentFilter();
                HwCameraManaulImpl.this.intentFilter.addAction("android.intent.action.SCREEN_OFF");
                HwCameraManaulImpl.this.intentFilter.addAction("android.intent.action.CLOSE_SYSTEM_DIALOGS");
                HwCameraManaulImpl.this.tipDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == 4) {
                            Slog.w(HwCameraManaulImpl.TAG, "onKeyBack");
                            HwCameraManaulImpl.this.dismissSlidedownTip();
                        }
                        return false;
                    }
                });
            }
        });
    }

    private void setType(final int type) {
        this.handler.post(new Runnable() {
            public void run() {
                Slog.w(HwCameraManaulImpl.TAG, "setType " + type);
                if (HwCameraManaulImpl.this.tipDialog == null) {
                    Slog.w(HwCameraManaulImpl.TAG, "tipDialog = null");
                    return;
                }
                if (HwCameraManaulImpl.this.imageView == null) {
                    View unused = HwCameraManaulImpl.this.imageView = HwCameraManaulImpl.this.tipDialog.findViewById(34603065);
                    if (HwCameraManaulImpl.this.imageView == null) {
                        return;
                    }
                }
                if (type == 0) {
                    HwCameraManaulImpl.this.imageView.setVisibility(0);
                } else {
                    HwCameraManaulImpl.this.imageView.setVisibility(8);
                }
            }
        });
    }

    public void registerSlideHallService() {
        Slog.w(TAG, "regesiterService begin");
        this.ret = this.mCoverManager.registerHallCallback("cameraserver", 1, this.mCallback);
        Slog.w(TAG, "regesiterService end with result = " + this.ret + " and Callback = " + this.mCallback.toString());
    }

    private void showSlidedownTip() {
        if (!this.ret) {
            Slog.w(TAG, "registerHallCallback fail!");
        }
        this.handler.post(new Runnable() {
            public void run() {
                if (HwCameraManaulImpl.this.tipDialog != null && !HwCameraManaulImpl.this.tipDialog.isShowing() && HwCameraManaulImpl.this.mContext != null) {
                    Slog.w(HwCameraManaulImpl.TAG, "show tipDialog = " + HwCameraManaulImpl.this.tipDialog.toString());
                    HwCameraManaulImpl.this.mContext.registerReceiver(HwCameraManaulImpl.this.coverStateReceiver, HwCameraManaulImpl.this.intentFilter);
                    HwCameraManaulImpl.this.tipDialog.show();
                }
            }
        });
    }

    /* access modifiers changed from: private */
    public void dismissSlidedownTip() {
        this.handler.post(new Runnable() {
            public void run() {
                if (HwCameraManaulImpl.this.tipDialog == null || !HwCameraManaulImpl.this.tipDialog.isShowing()) {
                    Slog.w(HwCameraManaulImpl.TAG, "tipDialog = null");
                    return;
                }
                Slog.w(HwCameraManaulImpl.TAG, "dismiss tipDialog = " + HwCameraManaulImpl.this.tipDialog.toString());
                HwCameraManaulImpl.this.tipDialog.dismiss();
            }
        });
    }

    public void unRegisterSlideHallService() {
        Slog.w(TAG, "unRegesiterService begin");
        dismissSlidedownTip();
        this.handler.post(new Runnable() {
            public void run() {
                try {
                    if (HwCameraManaulImpl.this.coverStateReceiver != null && HwCameraManaulImpl.this.mContext != null) {
                        Slog.w(HwCameraManaulImpl.TAG, "coverStateReceiver.unregisterReceiver begin");
                        HwCameraManaulImpl.this.mContext.unregisterReceiver(HwCameraManaulImpl.this.coverStateReceiver);
                        BroadcastReceiver unused = HwCameraManaulImpl.this.coverStateReceiver = null;
                        Slog.w(HwCameraManaulImpl.TAG, "coverStateReceiver.unregisterReceiver end");
                    }
                } catch (IllegalArgumentException e) {
                    Slog.d(HwCameraManaulImpl.TAG, e.getMessage());
                    Slog.w(HwCameraManaulImpl.TAG, "coverStateReceiver.unregisterReceiver end with error");
                }
            }
        });
        boolean ret2 = this.mCoverManager.unRegisterHallCallbackEx(1, this.mCallback);
        Slog.d(TAG, "unRegisterHallCallbackEx result = " + ret2 + " and Callback = " + this.mCallback.toString());
        Slog.w(TAG, "unRegesiterService end");
    }

    public void updateActivityCount(String cameraId, int newCameraState, int facing, String clientName) {
        if (newCameraState == 0) {
            registerSlideHallService();
            if (1 == facing && !"com.android.systemui".equals(clientName) && this.mCoverManager != null && this.mCoverManager.getHallState(1) == 0) {
                if ("com.huawei.camera".equals(clientName)) {
                    setType(0);
                } else {
                    setType(1);
                }
                showSlidedownTip();
            }
        } else if (newCameraState == 3) {
            unRegisterSlideHallService();
        }
    }
}
