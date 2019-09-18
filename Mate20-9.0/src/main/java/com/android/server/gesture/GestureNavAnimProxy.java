package com.android.server.gesture;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;
import android.widget.FrameLayout;
import com.android.server.gesture.GestureNavView;
import com.android.server.gesture.anim.GLGestureBackView;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;

public class GestureNavAnimProxy implements GestureNavView.IGestureNavBackAnim {
    private static final int DEFAULT_ANIM_MAX_TIME = 2000;
    private static final int MSG_ANIM_TIMEOUT = 1;
    private static final String TAG = "GestureNavAnim";
    /* access modifiers changed from: private */
    public GLGestureBackView mBackAnimView;
    private GestureAnimContainer mBackContainer;
    private Context mContext;
    private boolean mGestureNavReady;
    /* access modifiers changed from: private */
    public Handler mHandler;
    private final Object mLock = new Object();
    private GestureNavView.WindowConfig mWindowConfig = new GestureNavView.WindowConfig();
    private WindowManager mWindowManager;
    private boolean mWindowViewSetuped;

    interface AnimContainerListener {
        void onAttachedToWindow();

        void onDetachedFromWindow();
    }

    private final class AnimContainerListenerImpl implements AnimContainerListener {
        private AnimContainerListenerImpl() {
        }

        public void onAttachedToWindow() {
            if (GestureNavConst.DEBUG) {
                Log.i(GestureNavAnimProxy.TAG, "container attached to window");
            }
            if (GestureNavAnimProxy.this.mBackAnimView != null) {
                GestureNavAnimProxy.this.mBackAnimView.onResume();
            }
        }

        public void onDetachedFromWindow() {
            if (GestureNavConst.DEBUG) {
                Log.i(GestureNavAnimProxy.TAG, "container detached from window");
            }
            if (GestureNavAnimProxy.this.mBackAnimView != null) {
                GestureNavAnimProxy.this.mBackAnimView.onPause();
            }
        }
    }

    private final class AnimHandler extends Handler {
        public AnimHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                Log.i(GestureNavAnimProxy.TAG, "animation timeout, force hide views");
                GestureNavAnimProxy.this.showBackContainer(false);
            }
        }
    }

    static class GestureAnimContainer extends FrameLayout {
        private AnimContainerListener mListener;

        public GestureAnimContainer(Context context) {
            super(context);
        }

        public void setListener(AnimContainerListener listener) {
            this.mListener = listener;
        }

        /* access modifiers changed from: protected */
        public void onAttachedToWindow() {
            super.onAttachedToWindow();
            if (this.mListener != null) {
                this.mListener.onAttachedToWindow();
            }
        }

        /* access modifiers changed from: protected */
        public void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            if (this.mListener != null) {
                this.mListener.onDetachedFromWindow();
            }
        }
    }

    private final class GestureBackAnimListenerImpl implements GLGestureBackView.GestureBackAnimListener {
        private GestureBackAnimListenerImpl() {
        }

        public void onAnimationEnd(int animType) {
            if (GestureNavConst.DEBUG) {
                Log.i(GestureNavAnimProxy.TAG, "back anim end, animType=" + animType);
            }
            GestureNavAnimProxy.this.mHandler.removeMessages(1);
            if (GestureNavAnimProxy.this.mBackAnimView != null) {
                GestureNavAnimProxy.this.mBackAnimView.setDraw(false);
            }
            GestureNavAnimProxy.this.showBackContainer(false);
        }
    }

    public GestureNavAnimProxy(Context context, Looper looper) {
        this.mContext = context;
        this.mHandler = new AnimHandler(looper);
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
    }

    /* access modifiers changed from: private */
    public void showBackContainer(boolean show) {
        if (GestureNavConst.DEBUG) {
            Log.i(TAG, "showBackContainer show=" + show);
        }
        if (this.mBackContainer != null) {
            this.mBackContainer.setVisibility(show ? 0 : 8);
        }
    }

    private void updateGestureNavAnimWindow() {
        synchronized (this.mLock) {
            updateAnimWindowLocked();
        }
    }

    private void updateAnimWindowLocked() {
        if (this.mGestureNavReady) {
            if (!this.mWindowViewSetuped) {
                createAnimWindows();
            } else {
                updateAnimWindows();
            }
        } else if (this.mWindowViewSetuped) {
            destroyNavWindows();
        }
    }

    private void createAnimWindows() {
        Log.i(TAG, "createAnimWindows");
        this.mBackContainer = new GestureAnimContainer(this.mContext);
        this.mBackContainer.setListener(new AnimContainerListenerImpl());
        this.mBackAnimView = new GLGestureBackView(this.mContext);
        this.mBackAnimView.addAnimationListener(new GestureBackAnimListenerImpl());
        this.mBackContainer.addView(this.mBackAnimView, -1, -1);
        GestureUtils.addWindowView(this.mWindowManager, this.mBackContainer, createLayoutParams(TAG, this.mWindowConfig));
        showBackContainer(false);
        this.mWindowViewSetuped = true;
    }

    private void updateAnimWindows() {
        if (GestureNavConst.DEBUG) {
            Log.d(TAG, "updateAnimWindows");
        }
        GestureUtils.updateViewLayout(this.mWindowManager, this.mBackContainer, createLayoutParams(TAG, this.mWindowConfig));
    }

    private void destroyNavWindows() {
        Log.i(TAG, "destroyNavWindows");
        this.mWindowViewSetuped = false;
        showBackContainer(false);
        GestureUtils.removeWindowView(this.mWindowManager, this.mBackContainer, true);
        this.mBackContainer = null;
    }

    private WindowManager.LayoutParams createLayoutParams(String title, GestureNavView.WindowConfig config) {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(HwArbitrationDEFS.MSG_VPN_STATE_OPEN, MemoryConstant.MSG_PREREAD_DATA_REMOVE);
        if (ActivityManager.isHighEndGfx()) {
            lp.flags |= 16777216;
        }
        lp.flags |= 512;
        lp.format = -2;
        lp.gravity = 51;
        lp.x = config.startX;
        lp.y = config.startY;
        lp.width = config.width;
        lp.height = config.height;
        lp.windowAnimations = 0;
        lp.softInputMode = 49;
        lp.setTitle(title);
        if (config.usingNotch) {
            lp.hwFlags |= 65536;
        } else {
            lp.hwFlags &= -65537;
        }
        return lp;
    }

    public void updateViewConfig(int _displayWidth, int _displayHeight, int _startX, int _startY, int _width, int _height, int _locationOnScreenX, int _locationOnScreenY) {
        synchronized (this.mLock) {
            this.mWindowConfig.update(_displayWidth, _displayHeight, _startX, _startY, _width, _height, _locationOnScreenX, _locationOnScreenY);
        }
    }

    public void updateViewNotchState(boolean usingNotch) {
        synchronized (this.mLock) {
            this.mWindowConfig.udpateNotch(usingNotch);
        }
    }

    public void onNavCreate() {
        this.mGestureNavReady = true;
        updateGestureNavAnimWindow();
    }

    public void onNavUpdate() {
        updateGestureNavAnimWindow();
    }

    public void onNavDestroy() {
        this.mGestureNavReady = false;
        updateGestureNavAnimWindow();
    }

    public void onGestureAction(boolean down) {
    }

    public void setSide(boolean isLeft) {
        if (GestureNavConst.DEBUG) {
            Log.i(TAG, "setSide isLeft=" + isLeft);
        }
        if (this.mBackAnimView != null) {
            this.mBackAnimView.setSide(isLeft);
        }
    }

    public void setAnimPosition(float y) {
        if (GestureNavConst.DEBUG) {
            Log.i(TAG, "setAnimPosition y=" + y);
        }
        if (this.mBackAnimView != null) {
            showBackContainer(true);
            this.mBackAnimView.setDraw(true);
            this.mHandler.removeMessages(1);
            this.mBackAnimView.setAnimPosition(y);
        }
    }

    public boolean setAnimProcess(float process) {
        if (this.mBackAnimView == null) {
            return false;
        }
        this.mBackAnimView.setAnimProcess(process);
        return true;
    }

    public void playDisappearAnim() {
        if (GestureNavConst.DEBUG) {
            Log.i(TAG, "playDisappearAnim");
        }
        if (this.mBackAnimView != null) {
            if (!this.mHandler.hasMessages(1)) {
                this.mHandler.sendEmptyMessageDelayed(1, 2000);
            }
            this.mBackAnimView.playDisappearAnim();
        }
    }

    public void playFastSlidingAnim() {
        if (GestureNavConst.DEBUG) {
            Log.i(TAG, "playFastSlidingAnim");
        }
        if (this.mBackAnimView != null) {
            if (!this.mHandler.hasMessages(1)) {
                this.mHandler.sendEmptyMessageDelayed(1, 2000);
            }
            this.mBackAnimView.playFastSlidingAnim();
        }
    }
}
