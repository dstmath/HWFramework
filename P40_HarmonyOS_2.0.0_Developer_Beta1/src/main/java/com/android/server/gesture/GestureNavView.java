package com.android.server.gesture;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import com.huawei.android.view.WindowManagerGlobalEx;
import java.io.PrintWriter;

public class GestureNavView extends DefaultGestureNavView {
    private IGestureEventProxy mGestureEventProxy;
    private final Region mGlobalTapExcludeRegion;
    private final Runnable mHideRunnable;
    private boolean mIsExcludeCheckEnable;
    private final Region mLocalTapExcludeRegion;
    private int mNavId;
    private int mRegionId;
    private String mTag;
    private final Region mTempRegion;
    private WindowConfig mWindowConfig;

    public interface IGestureEventProxy {
        boolean onTouchEvent(GestureNavView gestureNavView, MotionEvent motionEvent);
    }

    public interface IGestureNavBackAnim {
        void onGestureAction(boolean z);

        void playDisappearAnim();

        void playFastSlidingAnim();

        void playScatterProcessAnim(float f, float f2);

        void setAnimPosition(boolean z, float f);

        boolean setAnimProcess(float f);

        void setDockIcon(boolean z);

        void setNightMode(boolean z);

        void setSide(boolean z);

        void switchDockIcon(boolean z);
    }

    public GestureNavView(Context context, int navId) {
        super(context);
        this.mTag = "GestureNavView";
        this.mNavId = -1;
        this.mWindowConfig = new WindowConfig();
        this.mGlobalTapExcludeRegion = new Region();
        this.mLocalTapExcludeRegion = new Region();
        this.mTempRegion = new Region();
        this.mHideRunnable = new Runnable() {
            /* class com.android.server.gesture.GestureNavView.AnonymousClass1 */

            @Override // java.lang.Runnable
            public void run() {
                GestureNavView.this.show(false);
            }
        };
        this.mNavId = navId;
        init();
    }

    public GestureNavView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GestureNavView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mTag = "GestureNavView";
        this.mNavId = -1;
        this.mWindowConfig = new WindowConfig();
        this.mGlobalTapExcludeRegion = new Region();
        this.mLocalTapExcludeRegion = new Region();
        this.mTempRegion = new Region();
        this.mHideRunnable = new Runnable() {
            /* class com.android.server.gesture.GestureNavView.AnonymousClass1 */

            @Override // java.lang.Runnable
            public void run() {
                GestureNavView.this.show(false);
            }
        };
        init();
    }

    private void init() {
        this.mTag = "GestureNavView#" + this.mNavId;
        this.mRegionId = hashCode();
        setZOrderOnTop(true);
        getHolder().setFormat(-2);
    }

    public static final class WindowConfig {
        public int displayHeight;
        public int displayWidth;
        public int height;
        public int leftOffset;
        public int locationOnScreenX;
        public int locationOnScreenY;
        public int rightOffset;
        public int startX;
        public int startY;
        public boolean usingNotch = true;
        public int width;

        public WindowConfig() {
            update(-1, -1, 0, 0, -1, -1, 0, 0, 0, 0);
        }

        public final void update(int displayWidth2, int displayHeight2, int startX2, int startY2, int width2, int height2, int locationOnScreenX2, int locationOnScreenY2, int leftOffset2, int rightOffset2) {
            this.displayWidth = displayWidth2;
            this.displayHeight = displayHeight2;
            this.startX = startX2;
            this.startY = startY2;
            this.width = width2;
            this.height = height2;
            this.locationOnScreenX = locationOnScreenX2;
            this.locationOnScreenY = locationOnScreenY2;
            this.leftOffset = leftOffset2;
            this.rightOffset = rightOffset2;
        }

        public void udpateNotch(boolean isUsingNotch) {
            this.usingNotch = isUsingNotch;
        }

        public Rect getFrameInDisplay() {
            int i = this.locationOnScreenX;
            int i2 = this.locationOnScreenY;
            return new Rect(i, i2, this.width + i, this.height + i2);
        }

        public String toString() {
            return "d.w:" + this.displayWidth + ", d.h:" + this.displayHeight + ", s.x:" + this.startX + ", s.y:" + this.startY + ", w:" + this.width + ", h:" + this.height + ", uN:" + this.usingNotch + ", l.x:" + this.locationOnScreenX + ", l.y:" + this.locationOnScreenY + ", l.o:" + this.leftOffset + ", r.o:" + this.rightOffset;
        }
    }

    public void updateViewConfig(int displayWidth, int displayHeight, int startX, int startY, int width, int height, int locationOnScreenX, int locationOnScreenY, int leftOffset, int rightOffset) {
        this.mWindowConfig.update(displayWidth, displayHeight, startX, startY, width, height, locationOnScreenX, locationOnScreenY, leftOffset, rightOffset);
    }

    public void updateViewNotchState(boolean usingNotch) {
        this.mWindowConfig.udpateNotch(usingNotch);
    }

    public WindowConfig getViewConfig() {
        return this.mWindowConfig;
    }

    public int getNavId() {
        return this.mNavId;
    }

    public void setGestureEventProxy(IGestureEventProxy proxy) {
        this.mGestureEventProxy = proxy;
    }

    public void show(boolean isEnable, boolean delay) {
        if (isEnable || !delay) {
            removeCallbacks(this.mHideRunnable);
            show(isEnable);
            return;
        }
        postDelayed(this.mHideRunnable, 500);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void show(boolean isEnable) {
        setVisibility(isEnable ? 0 : 8);
        scheduleUpdateExcludeRegion();
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        GestureNavView.super.onAttachedToWindow();
        scheduleUpdateExcludeRegion();
    }

    /* access modifiers changed from: protected */
    public void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        GestureNavView.super.onSizeChanged(width, height, oldWidth, oldHeight);
        scheduleUpdateExcludeRegion();
    }

    public boolean onTouchEvent(MotionEvent event) {
        IGestureEventProxy iGestureEventProxy = this.mGestureEventProxy;
        if (iGestureEventProxy != null) {
            return iGestureEventProxy.onTouchEvent(this, event);
        }
        return GestureNavView.super.onTouchEvent(event);
    }

    public void initTapExcludeCheckConfig(boolean enable) {
        this.mIsExcludeCheckEnable = enable;
    }

    public void setGlobalExcludeRegion(Region golbalExcludeRegion) {
        this.mGlobalTapExcludeRegion.set(golbalExcludeRegion);
        scheduleUpdateExcludeRegion();
    }

    private void scheduleUpdateExcludeRegion() {
        if (this.mIsExcludeCheckEnable) {
            post(new Runnable() {
                /* class com.android.server.gesture.$$Lambda$GestureNavView$n5YAdPzq3olVN4wrlmI72Gh06bk */

                @Override // java.lang.Runnable
                public final void run() {
                    GestureNavView.this.lambda$scheduleUpdateExcludeRegion$0$GestureNavView();
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: updateExcludeRegion */
    public void lambda$scheduleUpdateExcludeRegion$0$GestureNavView() {
        if (!isAttachedToWindow() || getWindowEx() == null) {
            if (GestureNavConst.DEBUG) {
                String str = this.mTag;
                Log.i(str, "View not ready, attached:" + isAttachedToWindow());
            }
        } else if (!canReceivePointerEvents()) {
            clearTapExcludeRegion();
        } else {
            calcAndUpdateLocalExcludeRegion();
        }
    }

    private void calcAndUpdateLocalExcludeRegion() {
        this.mTempRegion.set(this.mGlobalTapExcludeRegion);
        this.mTempRegion.op(this.mWindowConfig.getFrameInDisplay(), Region.Op.INTERSECT);
        if (this.mTempRegion.isEmpty()) {
            clearTapExcludeRegion();
            return;
        }
        this.mTempRegion.translate(-this.mWindowConfig.locationOnScreenX, -this.mWindowConfig.locationOnScreenY);
        this.mLocalTapExcludeRegion.set(this.mTempRegion);
        updateTapExcludeRegion();
    }

    private void updateTapExcludeRegion() {
        if (GestureNavConst.DEBUG) {
            String str = this.mTag;
            Log.i(str, "localExcludeRegion:" + this.mLocalTapExcludeRegion);
        }
        try {
            WindowManagerGlobalEx.updateTapExcludeRegion(getWindowEx(), this.mRegionId, this.mLocalTapExcludeRegion);
        } catch (RemoteException e) {
            Log.e(this.mTag, "updateTapExcludeRegion exception");
        }
    }

    private void clearTapExcludeRegion() {
        if (!this.mLocalTapExcludeRegion.isEmpty()) {
            if (GestureNavConst.DEBUG) {
                Log.i(this.mTag, "clearTapExcludeRegion");
            }
            try {
                WindowManagerGlobalEx.updateTapExcludeRegion(getWindowEx(), this.mRegionId, (Region) null);
                this.mLocalTapExcludeRegion.setEmpty();
            } catch (RemoteException e) {
                Log.e(this.mTag, "clearTapExcludeRegion exception");
            }
        }
    }

    public void dump(String prefix, PrintWriter pw) {
        pw.print(prefix);
        pw.println("Id#" + this.mNavId + "=" + this.mWindowConfig);
        pw.print(prefix);
        pw.println("mLocalTapExcludeRegion=" + this.mLocalTapExcludeRegion + ", mTempRegion=" + this.mTempRegion);
    }
}
