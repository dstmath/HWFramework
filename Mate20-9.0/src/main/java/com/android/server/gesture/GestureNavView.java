package com.android.server.gesture;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceView;

public class GestureNavView extends SurfaceView {
    private IGestureEventProxy mGestureEventProxy;
    private final Runnable mHideRunnable;
    private int mNavId;
    private WindowConfig mWindowConfig;

    public interface IGestureEventProxy {
        boolean onTouchEvent(GestureNavView gestureNavView, MotionEvent motionEvent);
    }

    public interface IGestureNavBackAnim {
        void onGestureAction(boolean z);

        void playDisappearAnim();

        void playFastSlidingAnim();

        void setAnimPosition(float f);

        boolean setAnimProcess(float f);

        void setSide(boolean z);
    }

    public static final class WindowConfig {
        public int displayHeight;
        public int displayWidth;
        public int height;
        public int locationOnScreenX;
        public int locationOnScreenY;
        public int startX;
        public int startY;
        public boolean usingNotch;
        public int width;

        public WindowConfig() {
            this(-1, -1, 0, 0, -1, -1, 0, 0);
        }

        public WindowConfig(int _displayWidth, int _displayHeight, int _startX, int _startY, int _width, int _height, int _locationOnScreenX, int _locationOnScreenY) {
            this.usingNotch = true;
            set(_displayWidth, _displayHeight, _startX, _startY, _width, _height, _locationOnScreenX, _locationOnScreenY);
        }

        private void set(int _displayWidth, int _displayHeight, int _startX, int _startY, int _width, int _height, int _locationOnScreenX, int _locationOnScreenY) {
            this.displayWidth = _displayWidth;
            this.displayHeight = _displayHeight;
            this.startX = _startX;
            this.startY = _startY;
            this.width = _width;
            this.height = _height;
            this.locationOnScreenX = _locationOnScreenX;
            this.locationOnScreenY = _locationOnScreenY;
        }

        public void update(int _displayWidth, int _displayHeight, int _startX, int _startY, int _width, int _height, int _locationOnScreenX, int _locationOnScreenY) {
            set(_displayWidth, _displayHeight, _startX, _startY, _width, _height, _locationOnScreenX, _locationOnScreenY);
        }

        public void udpateNotch(boolean _usingNotch) {
            this.usingNotch = _usingNotch;
        }

        public String toString() {
            return "d.w:" + this.displayWidth + ", d.h:" + this.displayHeight + ", s.x:" + this.startX + ", s.y:" + this.startY + ", w:" + this.width + ", h:" + this.height + ", uN:" + this.usingNotch + ", l.x:" + this.locationOnScreenX + ", l.y:" + this.locationOnScreenY;
        }
    }

    public GestureNavView(Context context) {
        this(context, 0);
    }

    public GestureNavView(Context context, int navId) {
        super(context);
        this.mNavId = -1;
        this.mWindowConfig = new WindowConfig();
        this.mHideRunnable = new Runnable() {
            public void run() {
                GestureNavView.this.setVisibility(8);
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
        this.mNavId = -1;
        this.mWindowConfig = new WindowConfig();
        this.mHideRunnable = new Runnable() {
            public void run() {
                GestureNavView.this.setVisibility(8);
            }
        };
        init();
    }

    private void init() {
        setZOrderOnTop(true);
        getHolder().setFormat(-2);
    }

    public void updateViewConfig(int _displayWidth, int _displayHeight, int _startX, int _startY, int _width, int _height, int _locationOnScreenX, int _locationOnScreenY) {
        this.mWindowConfig.update(_displayWidth, _displayHeight, _startX, _startY, _width, _height, _locationOnScreenX, _locationOnScreenY);
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

    public void show(boolean enable, boolean delay) {
        if (enable || !delay) {
            removeCallbacks(this.mHideRunnable);
            setVisibility(enable ? 0 : 8);
            return;
        }
        postDelayed(this.mHideRunnable, 500);
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (this.mGestureEventProxy != null) {
            return this.mGestureEventProxy.onTouchEvent(this, event);
        }
        return super.onTouchEvent(event);
    }
}
