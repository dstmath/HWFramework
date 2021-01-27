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
import android.view.ViewGroup;
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

    public CoordinationStackDividerManager(Context ctx) {
        this.mContext = ctx;
        this.mWindowManager = (WindowManager) ctx.getSystemService(WindowManager.class);
        if (CoordinationModeUtils.isFoldable()) {
            inflateView();
            initHandlerThread();
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

    private void inflateView() {
        this.mEdgeView = LayoutInflater.from(this.mContext).inflate(34013239, (ViewGroup) null);
        this.mEdgeView.setBackgroundColor(-16777216);
    }

    private void initHandlerThread() {
        this.mHandlerThread = new HandlerThread(TAG);
        this.mHandlerThread.start();
        this.mHandler = new ManagerHandler(this.mHandlerThread.getLooper());
    }

    /* access modifiers changed from: private */
    public final class ManagerHandler extends Handler {
        ManagerHandler(Looper looper) {
            super(looper, null, true);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 0) {
                CoordinationStackDividerManager.this.handleAddView(((Boolean) msg.obj).booleanValue());
            } else if (i == 1) {
                CoordinationStackDividerManager.this.handleRemoveView();
            } else if (i == 2) {
                CoordinationStackDividerManager.this.handleUpdateView(((Boolean) msg.obj).booleanValue());
            }
        }
    }

    public void removeDividerView() {
        if (this.mIsVisible) {
            this.mHandler.sendEmptyMessage(1);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleRemoveView() {
        if (this.mIsVisible) {
            View view = this.mEdgeView;
            if (view != null) {
                this.mWindowManager.removeView(view);
            }
            this.mIsVisible = false;
        }
    }

    public void addDividerView(boolean isLandscape) {
        if (!this.mIsVisible) {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(0, Boolean.valueOf(isLandscape)));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleAddView(boolean isLandscape) {
        int size = CoordinationModeUtils.getFoldScreenEdgeWidth();
        this.mLp = new WindowManager.LayoutParams(isLandscape ? -1 : size, isLandscape ? size : -1, 2039, 262184, -3);
        this.mLp.token = new Binder();
        this.mLp.setTitle(WINDOW_TITLE);
        this.mLp.privateFlags |= 64;
        this.mLp.privateFlags |= 16;
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
    /* access modifiers changed from: public */
    private void handleUpdateView(boolean isLandScape) {
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
