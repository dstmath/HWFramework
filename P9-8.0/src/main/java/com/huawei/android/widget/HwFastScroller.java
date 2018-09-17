package com.huawei.android.widget;

import android.view.MotionEvent;
import android.widget.AbsListView;
import android.widget.FastScroller;

public class HwFastScroller {
    private FastScrollListener mFastScrollListener;
    private FastScroller mFastScroller;

    public interface FastScrollListener {
        boolean onInterceptTouchEvent(MotionEvent motionEvent);

        void onSizeChanged(int i, int i2, int i3, int i4);

        boolean onTouchEvent(MotionEvent motionEvent);
    }

    private class FastScrollerInner extends FastScroller {
        public FastScrollerInner(AbsListView listView, int styleResId) {
            super(listView, styleResId);
        }

        public FastScrollerInner(AbsListView listView) {
            super(listView, 0);
        }

        public boolean onInterceptTouchEvent(MotionEvent ev) {
            if (HwFastScroller.this.mFastScrollListener != null) {
                return HwFastScroller.this.mFastScrollListener.onInterceptTouchEvent(ev);
            }
            return super.onInterceptTouchEvent(ev);
        }

        public void onSizeChanged(int w, int h, int oldw, int oldh) {
            if (HwFastScroller.this.mFastScrollListener != null) {
                HwFastScroller.this.mFastScrollListener.onSizeChanged(w, h, oldw, oldh);
            } else {
                super.onSizeChanged(w, h, oldw, oldh);
            }
        }

        public boolean onTouchEvent(MotionEvent me) {
            if (HwFastScroller.this.mFastScrollListener != null) {
                return HwFastScroller.this.mFastScrollListener.onTouchEvent(me);
            }
            return super.onTouchEvent(me);
        }
    }

    public void setFastScrollListener(FastScrollListener listener) {
        this.mFastScrollListener = listener;
    }

    public HwFastScroller(AbsListView listView, int styleResId) {
        this.mFastScroller = new FastScrollerInner(listView, styleResId);
    }

    public void remove() {
        this.mFastScroller.remove();
    }

    public Object getFastScroller() {
        return this.mFastScroller;
    }
}
