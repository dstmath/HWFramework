package android.widget;

import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import com.android.internal.view.menu.ShowableListMenu;
import java.lang.annotation.RCUnownedRef;

public abstract class ForwardingListener implements View.OnTouchListener, View.OnAttachStateChangeListener {
    private int mActivePointerId;
    private Runnable mDisallowIntercept;
    protected boolean mForwarding;
    private final int mLongPressTimeout = ((this.mTapTimeout + ViewConfiguration.getLongPressTimeout()) / 2);
    private final float mScaledTouchSlop;
    /* access modifiers changed from: private */
    @RCUnownedRef
    public final View mSrc;
    private final int mTapTimeout = ViewConfiguration.getTapTimeout();
    private Runnable mTriggerLongPress;

    private class DisallowIntercept implements Runnable {
        private DisallowIntercept() {
        }

        public void run() {
            ViewParent parent = ForwardingListener.this.mSrc.getParent();
            if (parent != null) {
                parent.requestDisallowInterceptTouchEvent(true);
            }
        }
    }

    private class TriggerLongPress implements Runnable {
        private TriggerLongPress() {
        }

        public void run() {
            ForwardingListener.this.onLongPress();
        }
    }

    public abstract ShowableListMenu getPopup();

    public ForwardingListener(View src) {
        this.mSrc = src;
        src.setLongClickable(true);
        src.addOnAttachStateChangeListener(this);
        this.mScaledTouchSlop = (float) ViewConfiguration.get(src.getContext()).getScaledTouchSlop();
    }

    public boolean onTouch(View v, MotionEvent event) {
        boolean forwarding;
        MotionEvent motionEvent = event;
        boolean wasForwarding = this.mForwarding;
        if (wasForwarding) {
            forwarding = onTouchForwarded(motionEvent) || !onForwardingStopped();
        } else {
            forwarding = onTouchObserved(motionEvent) && onForwardingStarted();
            if (forwarding) {
                long now = SystemClock.uptimeMillis();
                MotionEvent e = MotionEvent.obtain(now, now, 3, 0.0f, 0.0f, 0);
                this.mSrc.onTouchEvent(e);
                e.recycle();
            }
        }
        this.mForwarding = forwarding;
        if (forwarding || wasForwarding) {
            return true;
        }
        return false;
    }

    public void onViewAttachedToWindow(View v) {
    }

    public void onViewDetachedFromWindow(View v) {
        this.mForwarding = false;
        this.mActivePointerId = -1;
        if (this.mDisallowIntercept != null) {
            this.mSrc.removeCallbacks(this.mDisallowIntercept);
        }
    }

    /* access modifiers changed from: protected */
    public boolean onForwardingStarted() {
        ShowableListMenu popup = getPopup();
        if (popup != null && !popup.isShowing()) {
            popup.show();
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean onForwardingStopped() {
        ShowableListMenu popup = getPopup();
        if (popup != null && popup.isShowing()) {
            popup.dismiss();
        }
        return true;
    }

    private boolean onTouchObserved(MotionEvent srcEvent) {
        View src = this.mSrc;
        if (!src.isEnabled()) {
            return false;
        }
        switch (srcEvent.getActionMasked()) {
            case 0:
                this.mActivePointerId = srcEvent.getPointerId(0);
                if (this.mDisallowIntercept == null) {
                    this.mDisallowIntercept = new DisallowIntercept();
                }
                src.postDelayed(this.mDisallowIntercept, (long) this.mTapTimeout);
                if (this.mTriggerLongPress == null) {
                    this.mTriggerLongPress = new TriggerLongPress();
                }
                src.postDelayed(this.mTriggerLongPress, (long) this.mLongPressTimeout);
                break;
            case 1:
            case 3:
                clearCallbacks();
                break;
            case 2:
                int activePointerIndex = srcEvent.findPointerIndex(this.mActivePointerId);
                if (activePointerIndex >= 0 && !src.pointInView(srcEvent.getX(activePointerIndex), srcEvent.getY(activePointerIndex), this.mScaledTouchSlop)) {
                    clearCallbacks();
                    src.getParent().requestDisallowInterceptTouchEvent(true);
                    return true;
                }
        }
        return false;
    }

    private void clearCallbacks() {
        if (this.mTriggerLongPress != null) {
            this.mSrc.removeCallbacks(this.mTriggerLongPress);
        }
        if (this.mDisallowIntercept != null) {
            this.mSrc.removeCallbacks(this.mDisallowIntercept);
        }
    }

    /* access modifiers changed from: private */
    public void onLongPress() {
        clearCallbacks();
        View src = this.mSrc;
        if (src.isEnabled() && !src.isLongClickable() && onForwardingStarted()) {
            src.getParent().requestDisallowInterceptTouchEvent(true);
            long now = SystemClock.uptimeMillis();
            MotionEvent e = MotionEvent.obtain(now, now, 3, 0.0f, 0.0f, 0);
            src.onTouchEvent(e);
            e.recycle();
            this.mForwarding = true;
        }
    }

    private boolean onTouchForwarded(MotionEvent srcEvent) {
        View src = this.mSrc;
        ShowableListMenu popup = getPopup();
        boolean z = false;
        if (popup == null || !popup.isShowing()) {
            return false;
        }
        DropDownListView dst = (DropDownListView) popup.getListView();
        if (dst == null || !dst.isShown()) {
            return false;
        }
        MotionEvent dstEvent = MotionEvent.obtainNoHistory(srcEvent);
        src.toGlobalMotionEvent(dstEvent);
        dst.toLocalMotionEvent(dstEvent);
        boolean handled = dst.onForwardedEvent(dstEvent, this.mActivePointerId);
        dstEvent.recycle();
        int action = srcEvent.getActionMasked();
        boolean keepForwarding = (action == 1 || action == 3) ? false : true;
        if (handled && keepForwarding) {
            z = true;
        }
        return z;
    }
}
