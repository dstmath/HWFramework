package com.android.server.gesture;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;
import android.widget.FrameLayout;
import com.android.server.gesture.GestureNavView;
import com.android.server.gesture.anim.GLGestureBackView;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.view.WindowManagerEx;

public class GestureNavAnimProxy implements GestureNavView.IGestureNavBackAnim {
    private static final float ANIM_HEIGHT_RATE = 0.385f;
    private static final float ANIM_WIDTH_RATE = 0.15f;
    private static final float BASE_SCALE_RATIO = 1.0f;
    private static final int DEFAULT_ANIM_MAX_TIME = 2000;
    private static final float DP_CONVERSION_FACTOR = 0.15875f;
    private static final float MAX_BALL_PHYSICAL_WIDTH = 14.0f;
    private static final float MAX_BALL_WIDTH_RATIO = 0.92f;
    private static final int MSG_ANIM_TIMEOUT = 1;
    private static final float PIXEL_DENSITY = 160.0f;
    private static final String TAG = "GestureNavAnim";
    private int mAnimWindowHeight;
    private int mAnimWindowWidth;
    private GLGestureBackView mBackAnimView;
    private GestureAnimContainer mBackContainer;
    private Context mContext;
    private boolean mGestureNavReady;
    private Handler mHandler;
    private boolean mIsLeftSide;
    private final Object mLock = new Object();
    private float mPadMaxBallWidthRatio;
    private float mPositionY;
    private GestureNavView.WindowConfig mWindowConfig = new GestureNavView.WindowConfig();
    private WindowManager mWindowManager;
    private boolean mWindowViewSetuped;

    /* access modifiers changed from: package-private */
    public interface AnimContainerListener {
        void onAttachedToWindow();

        void onDetachedFromWindow();
    }

    public GestureNavAnimProxy(Context context, Looper looper) {
        this.mContext = context;
        this.mHandler = new AnimHandler(looper);
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
    }

    private final class AnimHandler extends Handler {
        AnimHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                Log.i(GestureNavAnimProxy.TAG, "animation timeout, force hide views");
                GestureNavAnimProxy.this.showBackContainer(false);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showBackContainer(boolean isShow) {
        if (this.mBackContainer != null) {
            if (GestureNavConst.DEBUG) {
                Log.i(TAG, "showBackContainer show=" + isShow + ", left=" + this.mBackContainer.getLeft() + ", top=" + this.mBackContainer.getTop());
            }
            this.mBackContainer.setVisibility(isShow ? 0 : 8);
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
        if (GestureNavConst.IS_TABLET) {
            updateBackViewSizeInPad();
        } else {
            this.mBackAnimView.setHwSize((this.mWindowConfig.displayWidth - this.mWindowConfig.leftOffset) - this.mWindowConfig.rightOffset, this.mWindowConfig.displayHeight);
        }
        this.mBackContainer.addView(this.mBackAnimView, -1, -1);
        GestureUtils.addWindowView(this.mWindowManager, this.mBackContainer, createLayoutParams(TAG, this.mWindowConfig));
        showBackContainer(false);
        this.mWindowViewSetuped = true;
    }

    private void updateAnimWindows() {
        if (GestureNavConst.DEBUG) {
            Log.d(TAG, "updateAnimWindows " + this.mWindowConfig);
        }
        if (GestureNavConst.IS_TABLET) {
            updateBackViewSizeInPad();
        } else {
            this.mBackAnimView.setHwSize((this.mWindowConfig.displayWidth - this.mWindowConfig.leftOffset) - this.mWindowConfig.rightOffset, this.mWindowConfig.displayHeight);
        }
        GestureUtils.updateViewLayout(this.mWindowManager, this.mBackContainer, createLayoutParams(TAG, this.mWindowConfig));
    }

    private void updateBackViewSizeInPad() {
        float navBallPixWidth = physicalWidthToPixWidth();
        int minLength = Math.min(this.mWindowConfig.displayWidth, this.mWindowConfig.displayHeight);
        if (isOverCurrentMaxBallWidth(navBallPixWidth, minLength)) {
            this.mPadMaxBallWidthRatio = navBallPixWidth / ((((float) minLength) * ANIM_WIDTH_RATE) * MAX_BALL_WIDTH_RATIO);
            this.mBackAnimView.setHwSize((int) (((float) ((this.mWindowConfig.displayWidth - this.mWindowConfig.leftOffset) - this.mWindowConfig.rightOffset)) * this.mPadMaxBallWidthRatio), (int) (((float) this.mWindowConfig.displayHeight) * this.mPadMaxBallWidthRatio));
            this.mBackAnimView.setStartPositionOffset(this.mPadMaxBallWidthRatio);
        }
    }

    private boolean isOverCurrentMaxBallWidth(float navBallPixWidth, int minLength) {
        return navBallPixWidth < (((float) minLength) * ANIM_WIDTH_RATE) * MAX_BALL_WIDTH_RATIO;
    }

    private float physicalWidthToPixWidth() {
        int densityDpi = this.mContext.getResources().getDisplayMetrics().densityDpi;
        return (((float) densityDpi) * (((((float) Math.sqrt(Math.pow((double) this.mWindowConfig.displayWidth, 2.0d) + Math.pow((double) this.mWindowConfig.displayHeight, 2.0d))) / getPhysicalScreenSize()) / ((float) densityDpi)) * 88.18898f)) / PIXEL_DENSITY;
    }

    private float getPhysicalScreenSize() {
        float xdpi = this.mContext.getResources().getDisplayMetrics().xdpi;
        float ydpi = this.mContext.getResources().getDisplayMetrics().ydpi;
        int width = this.mContext.getResources().getDisplayMetrics().widthPixels;
        int height = this.mContext.getResources().getDisplayMetrics().heightPixels;
        return (float) ((Math.sqrt((double) (((((float) width) / xdpi) * (((float) width) / xdpi)) + ((((float) height) / ydpi) * (((float) width) / xdpi)))) + Math.sqrt((double) (((((float) height) / xdpi) * (((float) height) / xdpi)) + ((((float) width) / ydpi) * (((float) height) / xdpi))))) / 2.0d);
    }

    private void destroyNavWindows() {
        Log.i(TAG, "destroyNavWindows");
        this.mWindowViewSetuped = false;
        showBackContainer(false);
        GestureUtils.removeWindowView(this.mWindowManager, this.mBackContainer, true);
        this.mBackContainer = null;
    }

    private WindowManager.LayoutParams createLayoutParams(String title, GestureNavView.WindowConfig config) {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(WindowManagerEx.LayoutParamsEx.getTypeNavigationBarPanel(), 312);
        if (ActivityManagerEx.isHighEndGfx()) {
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
        WindowManagerEx.LayoutParamsEx paramsEx = new WindowManagerEx.LayoutParamsEx(lp);
        if (config.usingNotch) {
            paramsEx.addHwFlags(65536);
        } else {
            paramsEx.clearHwFlags(65536);
        }
        return lp;
    }

    private void upateViewLocationBaseOnSide(boolean isLeftSide, float positionY) {
        synchronized (this.mLock) {
            this.mIsLeftSide = isLeftSide;
            this.mPositionY = positionY;
            calcAnimLocationLocked();
            if (this.mBackAnimView != null) {
                this.mBackAnimView.setStartPosition(this.mWindowConfig.startX);
            }
        }
        updateGestureNavAnimWindow();
    }

    private void calcAnimLocationLocked() {
        if (this.mIsLeftSide) {
            GestureNavView.WindowConfig windowConfig = this.mWindowConfig;
            windowConfig.startX = windowConfig.leftOffset;
        } else {
            GestureNavView.WindowConfig windowConfig2 = this.mWindowConfig;
            windowConfig2.startX = (windowConfig2.displayWidth - this.mWindowConfig.rightOffset) - this.mAnimWindowWidth;
        }
        GestureNavView.WindowConfig windowConfig3 = this.mWindowConfig;
        float f = this.mPositionY;
        int i = this.mAnimWindowHeight;
        windowConfig3.startY = (int) (f - (((float) i) * 0.5f));
        windowConfig3.width = this.mAnimWindowWidth;
        windowConfig3.height = i;
    }

    private void calcAnimSizeAndLocationLocked() {
        if (this.mWindowConfig.displayWidth < this.mWindowConfig.displayHeight) {
            this.mAnimWindowWidth = (int) (((float) this.mWindowConfig.displayWidth) * ANIM_WIDTH_RATE);
            this.mAnimWindowHeight = (int) (((float) this.mWindowConfig.displayWidth) * ANIM_HEIGHT_RATE);
        } else {
            this.mAnimWindowWidth = (int) (((float) this.mWindowConfig.displayHeight) * ANIM_WIDTH_RATE);
            this.mAnimWindowHeight = (int) (((float) this.mWindowConfig.displayHeight) * ANIM_HEIGHT_RATE);
        }
        if (GestureNavConst.IS_TABLET) {
            calcAnimSizeAndLocationInPad();
        }
        calcAnimLocationLocked();
    }

    private void calcAnimSizeAndLocationInPad() {
        float navBallPixWidth = physicalWidthToPixWidth();
        int i = this.mAnimWindowWidth;
        if (navBallPixWidth < ((float) i) * MAX_BALL_WIDTH_RATIO) {
            this.mPadMaxBallWidthRatio = navBallPixWidth / (((float) i) * MAX_BALL_WIDTH_RATIO);
            float f = this.mPadMaxBallWidthRatio;
            this.mAnimWindowWidth = (int) (((float) i) * f);
            this.mAnimWindowHeight = (int) (((float) this.mAnimWindowHeight) * f);
        }
    }

    public void updateViewConfig(int displayWidth, int displayHeight, int startX, int startY, int width, int height, int locationOnScreenX, int locationOnScreenY, int leftOffset, int rightOffset) {
        synchronized (this.mLock) {
            this.mWindowConfig.update(displayWidth, displayHeight, startX, startY, width, height, locationOnScreenX, locationOnScreenY, leftOffset, rightOffset);
            calcAnimSizeAndLocationLocked();
        }
    }

    public void updateViewNotchState(boolean isUsingNotch) {
        synchronized (this.mLock) {
            this.mWindowConfig.udpateNotch(isUsingNotch);
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

    /* access modifiers changed from: package-private */
    public static class GestureAnimContainer extends FrameLayout {
        private AnimContainerListener mListener;

        GestureAnimContainer(Context context) {
            super(context);
        }

        public void setListener(AnimContainerListener listener) {
            this.mListener = listener;
        }

        /* access modifiers changed from: protected */
        @Override // android.view.View, android.view.ViewGroup
        public void onAttachedToWindow() {
            super.onAttachedToWindow();
            AnimContainerListener animContainerListener = this.mListener;
            if (animContainerListener != null) {
                animContainerListener.onAttachedToWindow();
            }
        }

        /* access modifiers changed from: protected */
        @Override // android.view.View, android.view.ViewGroup
        public void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            AnimContainerListener animContainerListener = this.mListener;
            if (animContainerListener != null) {
                animContainerListener.onDetachedFromWindow();
            }
        }
    }

    /* access modifiers changed from: private */
    public final class AnimContainerListenerImpl implements AnimContainerListener {
        private AnimContainerListenerImpl() {
        }

        @Override // com.android.server.gesture.GestureNavAnimProxy.AnimContainerListener
        public void onAttachedToWindow() {
            if (GestureNavConst.DEBUG) {
                Log.i(GestureNavAnimProxy.TAG, "container attached to window");
            }
            if (GestureNavAnimProxy.this.mBackAnimView != null) {
                GestureNavAnimProxy.this.mBackAnimView.onResume();
            }
        }

        @Override // com.android.server.gesture.GestureNavAnimProxy.AnimContainerListener
        public void onDetachedFromWindow() {
            if (GestureNavConst.DEBUG) {
                Log.i(GestureNavAnimProxy.TAG, "container detached from window");
            }
            if (GestureNavAnimProxy.this.mBackAnimView != null) {
                GestureNavAnimProxy.this.mBackAnimView.onPause();
            }
        }
    }

    @Override // com.android.server.gesture.GestureNavView.IGestureNavBackAnim
    public void onGestureAction(boolean isDown) {
    }

    @Override // com.android.server.gesture.GestureNavView.IGestureNavBackAnim
    public void setSide(boolean isLeft) {
        if (GestureNavConst.DEBUG) {
            Log.i(TAG, "setSide isLeft=" + isLeft);
        }
        GLGestureBackView gLGestureBackView = this.mBackAnimView;
        if (gLGestureBackView != null) {
            gLGestureBackView.setSide(isLeft);
        }
    }

    @Override // com.android.server.gesture.GestureNavView.IGestureNavBackAnim
    public void setNightMode(boolean isNightMode) {
        if (this.mBackAnimView != null) {
            if (GestureNavConst.DEBUG) {
                Log.i(TAG, "setSide isNightMode=" + isNightMode);
            }
            this.mBackAnimView.setNightMode(isNightMode);
        }
    }

    @Override // com.android.server.gesture.GestureNavView.IGestureNavBackAnim
    public void setAnimPosition(boolean isLeft, float positionY) {
        if (this.mBackAnimView != null) {
            upateViewLocationBaseOnSide(isLeft, positionY);
            showBackContainer(true);
            this.mBackAnimView.setDraw(true);
            this.mHandler.removeMessages(1);
            if (GestureNavConst.DEBUG) {
                Log.i(TAG, "setAnimPosition y:" + positionY + ", left=" + this.mBackAnimView.getLeft() + ", top=" + this.mBackAnimView.getTop());
            }
        }
    }

    @Override // com.android.server.gesture.GestureNavView.IGestureNavBackAnim
    public boolean setAnimProcess(float process) {
        GLGestureBackView gLGestureBackView = this.mBackAnimView;
        if (gLGestureBackView == null) {
            return false;
        }
        gLGestureBackView.setAnimProcess(process);
        return true;
    }

    @Override // com.android.server.gesture.GestureNavView.IGestureNavBackAnim
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

    @Override // com.android.server.gesture.GestureNavView.IGestureNavBackAnim
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

    @Override // com.android.server.gesture.GestureNavView.IGestureNavBackAnim
    public void playScatterProcessAnim(float fromProcess, float toProcess) {
        if (GestureNavConst.DEBUG) {
            Log.i(TAG, "playScatterProcessAnim");
        }
        if (this.mBackAnimView != null) {
            if (!this.mHandler.hasMessages(1)) {
                this.mHandler.sendEmptyMessageDelayed(1, 2000);
            }
            this.mBackAnimView.playScatterProcessAnim(fromProcess, toProcess);
        }
    }

    /* access modifiers changed from: private */
    public final class GestureBackAnimListenerImpl implements GLGestureBackView.GestureBackAnimListener {
        private GestureBackAnimListenerImpl() {
        }

        @Override // com.android.server.gesture.anim.GLGestureBackView.GestureBackAnimListener
        public void onAnimationEnd(int animType) {
            if (GestureNavConst.DEBUG) {
                Log.i(GestureNavAnimProxy.TAG, "back anim end, animType=" + animType);
            }
            GestureNavAnimProxy.this.mHandler.removeMessages(1);
            if (GestureNavAnimProxy.this.mBackAnimView != null) {
                GestureNavAnimProxy.this.mBackAnimView.setDraw(false);
                GestureNavAnimProxy.this.mBackAnimView.endAnimation();
            }
            GestureNavAnimProxy.this.showBackContainer(false);
        }
    }

    @Override // com.android.server.gesture.GestureNavView.IGestureNavBackAnim
    public void switchDockIcon(boolean isSlideIn) {
        GLGestureBackView gLGestureBackView = this.mBackAnimView;
        if (gLGestureBackView != null) {
            gLGestureBackView.switchDockIcon(isSlideIn);
        }
    }

    @Override // com.android.server.gesture.GestureNavView.IGestureNavBackAnim
    public void setDockIcon(boolean isShowDockIcon) {
        GLGestureBackView gLGestureBackView = this.mBackAnimView;
        if (gLGestureBackView != null) {
            gLGestureBackView.setDockIcon(isShowDockIcon);
        }
    }
}
