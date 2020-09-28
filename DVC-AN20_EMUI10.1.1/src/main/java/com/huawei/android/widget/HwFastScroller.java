package com.huawei.android.widget;

import android.view.MotionEvent;
import android.widget.AbsListView;
import android.widget.FastScrollerEx;

public class HwFastScroller {
    private FastScrollListener mFastScrollListener;
    private FastScrollerEx mFastScroller;

    public interface FastScrollListener {
        boolean onInterceptTouchEvent(MotionEvent motionEvent);

        void onSizeChanged(int i, int i2, int i3, int i4);

        boolean onTouchEvent(MotionEvent motionEvent);
    }

    public HwFastScroller(AbsListView listView, int styleResId) {
        this.mFastScroller = new FastScrollerInner(listView, styleResId);
    }

    public void setFastScrollListener(FastScrollListener listener) {
        this.mFastScrollListener = listener;
    }

    public void remove() {
        this.mFastScroller.remove();
    }

    public Object getFastScroller() {
        return this.mFastScroller;
    }

    private class FastScrollerInner extends FastScrollerEx {
        FastScrollerInner(AbsListView listView, int styleResId) {
            super(listView, styleResId);
        }

        FastScrollerInner(AbsListView listView) {
            super(listView, 0);
        }

        public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
            if (HwFastScroller.this.mFastScrollListener != null) {
                return HwFastScroller.this.mFastScrollListener.onInterceptTouchEvent(motionEvent);
            }
            return HwFastScroller.super.onInterceptTouchEvent(motionEvent);
        }

        public void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
            if (HwFastScroller.this.mFastScrollListener != null) {
                HwFastScroller.this.mFastScrollListener.onSizeChanged(width, height, oldWidth, oldHeight);
            } else {
                HwFastScroller.super.onSizeChanged(width, height, oldWidth, oldHeight);
            }
        }

        public boolean onTouchEvent(MotionEvent motionEvent) {
            if (HwFastScroller.this.mFastScrollListener != null) {
                return HwFastScroller.this.mFastScrollListener.onTouchEvent(motionEvent);
            }
            return HwFastScroller.super.onTouchEvent(motionEvent);
        }
    }
}
