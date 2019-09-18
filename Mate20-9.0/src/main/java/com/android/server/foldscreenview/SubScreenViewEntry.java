package com.android.server.foldscreenview;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import com.android.server.hidata.mplink.HwMpLinkServiceImpl;
import com.huawei.android.fsm.HwFoldScreenManagerEx;

public class SubScreenViewEntry {
    private static final int DELAYED_TIME = 100;
    private static final int MAIN_SCREEN_WIDTH = 1136;
    private static final int MSG_CREAT_SUB_SCREEN_VIEW = 1;
    private static final int MSG_DISPLAY_CHANGE_TO_SUB_MODE = 3;
    private static final int MSG_REMOVE_SUB_SCREEN_VIEW = 4;
    private static final int MSG_ROTATION_CHANGED = 2;
    private static final int PHONE_HEIGHT = 2480;
    private static final int PHONE_WIDTH = 904;
    private static final String TAG = "FoldScreen_SubScreenViewEntry";
    private static final int VIEW_HEIGHT = 506;
    private static final int VIEW_WIDTH = 904;
    private Context mContext;
    private int mCurrentViewHeight;
    private int mCurrentViewWidth;
    private int mCurrentXPosition;
    private int mCurrentYPosition;
    private DisplayModeListener mDisplayListener = new DisplayModeListener();
    /* access modifiers changed from: private */
    public Handler mHandler;
    private HandlerThread mHandlerThread;
    private View mSubScreenView;
    private WindowManager.LayoutParams mWParams;
    /* access modifiers changed from: private */
    public WindowManager mWindowManager;

    private class DisplayModeListener implements HwFoldScreenManagerEx.FoldDisplayModeListener {
        private DisplayModeListener() {
        }

        public void onScreenDisplayModeChange(int displayMode) {
            if (displayMode != 3) {
                Log.w(SubScreenViewEntry.TAG, "The current display mode is not sub screen, cannot show sub screen view");
                SubScreenViewEntry.this.mHandler.sendEmptyMessage(4);
                return;
            }
            SubScreenViewEntry.this.mHandler.sendMessage(SubScreenViewEntry.this.mHandler.obtainMessage(3, SubScreenViewEntry.this.mWindowManager.getDefaultDisplay().getRotation(), 0));
        }
    }

    private final class SubScreenViewHandler extends Handler {
        public SubScreenViewHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    SubScreenViewEntry.this.createSubScreenView();
                    return;
                case 2:
                    SubScreenViewEntry.this.updateSubScreenView(msg.arg1);
                    return;
                case 3:
                    SubScreenViewEntry.this.updateSubScreenView(msg.arg1);
                    return;
                case 4:
                    SubScreenViewEntry.this.removeSubScreenView();
                    return;
                default:
                    return;
            }
        }
    }

    public SubScreenViewEntry(Context context) {
        this.mContext = context;
        Context context2 = this.mContext;
        Context context3 = this.mContext;
        this.mWindowManager = (WindowManager) context2.getSystemService("window");
    }

    public void init() {
        HwFoldScreenManagerEx.registerFoldDisplayMode(this.mDisplayListener);
        this.mHandlerThread = new HandlerThread(TAG);
        this.mHandlerThread.start();
        this.mHandler = new SubScreenViewHandler(this.mHandlerThread.getLooper());
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(904, VIEW_HEIGHT, 0, 0, 2014, 520, -1);
        this.mWParams = layoutParams;
        this.mWParams.gravity = 51;
        this.mWParams.layoutInDisplayCutoutMode = 1;
        this.mWParams.setTitle(TAG);
        if (HwFoldScreenManagerEx.getDisplayMode() == 3) {
            this.mHandler.sendEmptyMessage(1);
        }
    }

    public void onRotationChanged(int rotation) {
        if (HwFoldScreenManagerEx.getDisplayMode() != 3) {
            Log.d(TAG, "The current display mode is not sub screen, cannot update sub screen view");
            return;
        }
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(2, rotation, 0), 100);
    }

    /* access modifiers changed from: private */
    public void createSubScreenView() {
        if (this.mSubScreenView != null) {
            Log.d(TAG, "the window view is exist");
            return;
        }
        updateViewParameter(this.mWindowManager.getDefaultDisplay().getRotation());
        Log.d(TAG, "Create Subview [ xPosition: " + this.mWParams.x + ", yPosition: " + this.mWParams.y + ", viewHeigh: " + this.mWParams.height + ", viewWidth: " + this.mWParams.width + "]");
        this.mSubScreenView = LayoutInflater.from(this.mContext).inflate(34013376, null);
        this.mWindowManager.addView(this.mSubScreenView, this.mWParams);
    }

    /* access modifiers changed from: private */
    public void updateSubScreenView(int rotation) {
        if (this.mSubScreenView == null) {
            createSubScreenView();
            return;
        }
        updateViewParameter(rotation);
        Log.d(TAG, "Update Subview [ xPosition: " + this.mWParams.x + ", yPosition: " + this.mWParams.y + ", viewHeigh: " + this.mWParams.height + ", viewWidth: " + this.mWParams.width + "]");
        this.mWindowManager.updateViewLayout(this.mSubScreenView, this.mWParams);
    }

    /* access modifiers changed from: private */
    public void removeSubScreenView() {
        Log.d(TAG, "Remove Subview mSubScreenView: " + this.mSubScreenView);
        if (this.mSubScreenView != null) {
            this.mWindowManager.removeView(this.mSubScreenView);
            this.mSubScreenView = null;
        }
    }

    private void updateViewParameter(int rotation) {
        switch (rotation) {
            case 0:
                this.mWParams.x = 0;
                this.mWParams.y = 0;
                this.mWParams.width = 904;
                this.mWParams.height = VIEW_HEIGHT;
                return;
            case 1:
                this.mWParams.x = 0;
                this.mWParams.y = HwMpLinkServiceImpl.MPLINK_MSG_UPDTAE_UL_FREQ_INFO;
                this.mWParams.width = VIEW_HEIGHT;
                this.mWParams.height = 904;
                return;
            case 2:
                this.mWParams.x = HwMpLinkServiceImpl.MPLINK_MSG_UPDTAE_UL_FREQ_INFO;
                this.mWParams.y = 1974;
                this.mWParams.width = 904;
                this.mWParams.height = VIEW_HEIGHT;
                return;
            case 3:
                this.mWParams.x = 1974;
                this.mWParams.y = 0;
                this.mWParams.width = VIEW_HEIGHT;
                this.mWParams.height = 904;
                return;
            default:
                return;
        }
    }
}
