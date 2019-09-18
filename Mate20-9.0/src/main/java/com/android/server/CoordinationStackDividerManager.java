package com.android.server;

import android.content.Context;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.CoordinationModeUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

public class CoordinationStackDividerManager {
    private static final int MSG_HANDLE_ADD_VIEW = 0;
    private static final int MSG_HANDLE_REMOVE_VIEW = 1;
    private static final int MSG_HANDLE_UPDATE_VIEW = 2;
    private static final String TAG = "CoordinationStackDividerManager";
    private static final String WINDOW_TITLE = "CoordinationStackDivider";
    private static CoordinationStackDividerManager mInstance;
    private Context mContext;
    private View mEdgeView;
    private Handler mHandler;
    private HandlerThread mHandlerThread;
    private boolean mIsVisible;
    private WindowManager.LayoutParams mLp;
    private final WindowManager mWindowManager;

    private final class ManagerHandler extends Handler {
        public ManagerHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    CoordinationStackDividerManager.this.handleAddView(((Boolean) msg.obj).booleanValue());
                    return;
                case 1:
                    CoordinationStackDividerManager.this.handleRemoveView();
                    return;
                case 2:
                    CoordinationStackDividerManager.this.handleUpdateView(((Boolean) msg.obj).booleanValue());
                    return;
                default:
                    return;
            }
        }
    }

    public static synchronized CoordinationStackDividerManager getInstance(Context context) {
        CoordinationStackDividerManager coordinationStackDividerManager;
        synchronized (CoordinationStackDividerManager.class) {
            if (mInstance == null) {
                mInstance = new CoordinationStackDividerManager(context);
            }
            coordinationStackDividerManager = mInstance;
        }
        return coordinationStackDividerManager;
    }

    public CoordinationStackDividerManager(Context ctx) {
        this.mContext = ctx;
        this.mWindowManager = (WindowManager) ctx.getSystemService(WindowManager.class);
        if (CoordinationModeUtils.isFoldable()) {
            inflateView();
            initHandlerThread();
        }
    }

    private void inflateView() {
        this.mEdgeView = LayoutInflater.from(this.mContext).inflate(34013292, null);
        this.mEdgeView.setBackgroundColor(-16777216);
    }

    private void initHandlerThread() {
        this.mHandlerThread = new HandlerThread(TAG);
        this.mHandlerThread.start();
        this.mHandler = new ManagerHandler(this.mHandlerThread.getLooper());
    }

    public void removeDividerView() {
        if (this.mIsVisible) {
            this.mHandler.sendEmptyMessage(1);
        }
    }

    /* access modifiers changed from: private */
    public void handleRemoveView() {
        if (this.mIsVisible) {
            if (this.mEdgeView != null) {
                this.mWindowManager.removeView(this.mEdgeView);
            }
            this.mIsVisible = false;
        }
    }

    public void addDividerView(boolean islandscape) {
        if (!this.mIsVisible) {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(0, Boolean.valueOf(islandscape)));
        }
    }

    /* access modifiers changed from: private */
    public void handleAddView(boolean islandscape) {
        int size = CoordinationModeUtils.getFoldScreenEdgeWidth();
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(islandscape ? -1 : size, islandscape ? size : -1, 2039, 262184, -3);
        this.mLp = layoutParams;
        this.mLp.token = new Binder();
        this.mLp.setTitle(WINDOW_TITLE);
        this.mLp.privateFlags |= 64;
        this.mLp.layoutInDisplayCutoutMode = 1;
        this.mEdgeView.setSystemUiVisibility(1792);
        this.mWindowManager.addView(this.mEdgeView, this.mLp);
        this.mIsVisible = true;
    }

    public void updateDividerView(boolean isLandScape) {
        if (this.mIsVisible) {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(2, Boolean.valueOf(isLandScape)));
        }
    }

    /* access modifiers changed from: private */
    public void handleUpdateView(boolean isLandScape) {
        int size = CoordinationModeUtils.getFoldScreenEdgeWidth();
        int i = -1;
        this.mLp.width = isLandScape ? -1 : size;
        WindowManager.LayoutParams layoutParams = this.mLp;
        if (isLandScape) {
            i = size;
        }
        layoutParams.height = i;
        this.mWindowManager.updateViewLayout(this.mEdgeView, this.mLp);
    }

    public boolean isVisible() {
        return this.mIsVisible;
    }
}
