package com.google.android.maps;

import android.os.Handler;
import android.view.MotionEvent;
import com.google.android.maps.MapView.LayoutParams;

public class TrackballGestureDetector {
    private boolean mAlwaysInTapRegion;
    private float mCurrentDownX;
    private float mCurrentDownY;
    private long mDownTime;
    private float mFirstDownX;
    private float mFirstDownY;
    private Handler mHandler;
    private boolean mInLongPress;
    private boolean mIsDoubleTap;
    private boolean mIsScroll;
    private boolean mIsTap;
    private float mLastMotionX;
    private float mLastMotionY;
    private Runnable mOurLongPressRunnable = new Runnable() {
        public void run() {
            TrackballGestureDetector.this.dispatchLongPress();
        }
    };
    private long mPreviousDownTime;
    private float mScrollX;
    private float mScrollY;
    private Runnable mUserLongPressRunnable;

    TrackballGestureDetector(Handler handler) {
        this.mHandler = handler;
    }

    public void analyze(MotionEvent ev) {
        int action = ev.getAction();
        float y = ev.getY();
        float x = ev.getX();
        this.mIsScroll = false;
        this.mIsTap = false;
        this.mIsDoubleTap = false;
        switch (action) {
            case LayoutParams.MODE_MAP /*0*/:
                this.mLastMotionX = x;
                this.mLastMotionY = y;
                this.mFirstDownX = this.mCurrentDownX;
                this.mFirstDownY = this.mCurrentDownY;
                this.mCurrentDownX = x;
                this.mCurrentDownY = y;
                this.mPreviousDownTime = this.mDownTime;
                this.mDownTime = ev.getDownTime();
                this.mAlwaysInTapRegion = true;
                this.mInLongPress = false;
                this.mHandler.removeCallbacks(this.mOurLongPressRunnable);
                this.mHandler.postAtTime(this.mOurLongPressRunnable, this.mDownTime + 1500);
                return;
            case 1:
                if (this.mInLongPress) {
                    this.mInLongPress = false;
                    return;
                }
                if (this.mAlwaysInTapRegion) {
                    long eventTime = ev.getEventTime();
                    if (eventTime - this.mPreviousDownTime < 600) {
                        this.mIsDoubleTap = true;
                    } else if (eventTime - this.mDownTime < 300) {
                        this.mIsTap = true;
                    }
                }
                this.mHandler.removeCallbacks(this.mOurLongPressRunnable);
                return;
            case OverlayItem.ITEM_STATE_SELECTED_MASK /*2*/:
                if (!this.mInLongPress) {
                    this.mScrollX = this.mLastMotionX - x;
                    this.mScrollY = this.mLastMotionY - y;
                    this.mLastMotionX = x;
                    this.mLastMotionY = y;
                    if (((int) (Math.abs(x - this.mCurrentDownX) + Math.abs(y - this.mCurrentDownY))) > 5) {
                        this.mAlwaysInTapRegion = false;
                        this.mHandler.removeCallbacks(this.mOurLongPressRunnable);
                    }
                    this.mIsScroll = true;
                    return;
                }
                return;
            default:
                return;
        }
    }

    public void registerLongPressCallback(Runnable runnable) {
        this.mUserLongPressRunnable = runnable;
    }

    private void dispatchLongPress() {
        this.mInLongPress = true;
        if (this.mUserLongPressRunnable != null) {
            this.mUserLongPressRunnable.run();
        }
    }

    public boolean isScroll() {
        return this.mIsScroll;
    }

    public float scrollX() {
        return this.mScrollX;
    }

    public float scrollY() {
        return this.mScrollY;
    }

    public boolean isTap() {
        return this.mIsTap;
    }

    public float getCurrentDownX() {
        return this.mCurrentDownX;
    }

    public float getCurrentDownY() {
        return this.mCurrentDownY;
    }

    public boolean isDoubleTap() {
        return this.mIsDoubleTap;
    }

    public float getFirstDownX() {
        return this.mFirstDownX;
    }

    public float getFirstDownY() {
        return this.mFirstDownY;
    }
}
