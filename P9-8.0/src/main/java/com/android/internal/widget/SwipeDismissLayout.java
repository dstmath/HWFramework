package com.android.internal.widget;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ReceiverCallNotAllowedException;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import com.android.internal.R;

public class SwipeDismissLayout extends FrameLayout {
    private static final float MAX_DIST_THRESHOLD = 0.33f;
    private static final float MIN_DIST_THRESHOLD = 0.1f;
    private static final String TAG = "SwipeDismissLayout";
    private int mActiveTouchId;
    private boolean mActivityTranslucencyConverted = false;
    private boolean mBlockGesture = false;
    private boolean mDiscardIntercept;
    private final DismissAnimator mDismissAnimator = new DismissAnimator();
    private boolean mDismissable = true;
    private boolean mDismissed;
    private OnDismissedListener mDismissedListener;
    private float mDownX;
    private float mDownY;
    private boolean mIsWindowNativelyTranslucent;
    private float mLastX;
    private int mMinFlingVelocity;
    private OnSwipeProgressChangedListener mProgressListener;
    private IntentFilter mScreenOffFilter = new IntentFilter("android.intent.action.SCREEN_OFF");
    private BroadcastReceiver mScreenOffReceiver;
    private int mSlop;
    private boolean mSwiping;
    private VelocityTracker mVelocityTracker;

    public interface OnDismissedListener {
        void onDismissed(SwipeDismissLayout swipeDismissLayout);
    }

    public interface OnSwipeProgressChangedListener {
        void onSwipeCancelled(SwipeDismissLayout swipeDismissLayout);

        void onSwipeProgressChanged(SwipeDismissLayout swipeDismissLayout, float f, float f2);
    }

    private class DismissAnimator implements AnimatorUpdateListener, AnimatorListener {
        private final long DISMISS_DURATION = 250;
        private final TimeInterpolator DISMISS_INTERPOLATOR = new DecelerateInterpolator(1.5f);
        private final ValueAnimator mDismissAnimator = new ValueAnimator();
        private boolean mDismissOnComplete = false;
        private boolean mWasCanceled = false;

        DismissAnimator() {
            this.mDismissAnimator.addUpdateListener(this);
            this.mDismissAnimator.addListener(this);
        }

        void animateDismissal(float currentTranslation) {
            animate(currentTranslation / ((float) SwipeDismissLayout.this.getWidth()), 1.0f, 250, this.DISMISS_INTERPOLATOR, true);
        }

        void animateRecovery(float currentTranslation) {
            animate(currentTranslation / ((float) SwipeDismissLayout.this.getWidth()), 0.0f, 250, this.DISMISS_INTERPOLATOR, false);
        }

        boolean isAnimating() {
            return this.mDismissAnimator.isStarted();
        }

        private void animate(float from, float to, long duration, TimeInterpolator interpolator, boolean dismissOnComplete) {
            this.mDismissAnimator.cancel();
            this.mDismissOnComplete = dismissOnComplete;
            this.mDismissAnimator.setFloatValues(new float[]{from, to});
            this.mDismissAnimator.setDuration(duration);
            this.mDismissAnimator.setInterpolator(interpolator);
            this.mDismissAnimator.start();
        }

        public void onAnimationUpdate(ValueAnimator animation) {
            SwipeDismissLayout.this.setProgress(((float) SwipeDismissLayout.this.getWidth()) * ((Float) animation.getAnimatedValue()).floatValue());
        }

        public void onAnimationStart(Animator animation) {
            this.mWasCanceled = false;
        }

        public void onAnimationCancel(Animator animation) {
            this.mWasCanceled = true;
        }

        public void onAnimationEnd(Animator animation) {
            if (!this.mWasCanceled) {
                if (this.mDismissOnComplete) {
                    SwipeDismissLayout.this.dismiss();
                } else {
                    SwipeDismissLayout.this.cancel();
                }
            }
        }

        public void onAnimationRepeat(Animator animation) {
        }
    }

    public SwipeDismissLayout(Context context) {
        super(context);
        init(context);
    }

    public SwipeDismissLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SwipeDismissLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        ViewConfiguration vc = ViewConfiguration.get(context);
        this.mSlop = vc.getScaledTouchSlop();
        this.mMinFlingVelocity = vc.getScaledMinimumFlingVelocity();
        TypedArray a = context.getTheme().obtainStyledAttributes(R.styleable.Theme);
        this.mIsWindowNativelyTranslucent = a.getBoolean(5, false);
        a.recycle();
    }

    public void setOnDismissedListener(OnDismissedListener listener) {
        this.mDismissedListener = listener;
    }

    public void setOnSwipeProgressChangedListener(OnSwipeProgressChangedListener listener) {
        this.mProgressListener = listener;
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        try {
            this.mScreenOffReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    SwipeDismissLayout.this.post(new -$Lambda$Ak7AldtRrOI3bx60T1eEC89j1ns(this));
                }

                /* synthetic */ void lambda$-com_android_internal_widget_SwipeDismissLayout$1_4841() {
                    if (SwipeDismissLayout.this.mDismissed) {
                        SwipeDismissLayout.this.dismiss();
                    } else {
                        SwipeDismissLayout.this.cancel();
                    }
                    SwipeDismissLayout.this.resetMembers();
                }
            };
            getContext().registerReceiver(this.mScreenOffReceiver, this.mScreenOffFilter);
        } catch (ReceiverCallNotAllowedException e) {
            this.mScreenOffReceiver = null;
        }
    }

    protected void onDetachedFromWindow() {
        if (this.mScreenOffReceiver != null) {
            getContext().unregisterReceiver(this.mScreenOffReceiver);
            this.mScreenOffReceiver = null;
        }
        super.onDetachedFromWindow();
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean z = false;
        checkGesture(ev);
        if (this.mBlockGesture) {
            return true;
        }
        if (!this.mDismissable) {
            return super.onInterceptTouchEvent(ev);
        }
        ev.offsetLocation(ev.getRawX() - ev.getX(), 0.0f);
        switch (ev.getActionMasked()) {
            case 0:
                resetMembers();
                this.mDownX = ev.getRawX();
                this.mDownY = ev.getRawY();
                this.mActiveTouchId = ev.getPointerId(0);
                this.mVelocityTracker = VelocityTracker.obtain("int1");
                this.mVelocityTracker.addMovement(ev);
                break;
            case 1:
            case 3:
                resetMembers();
                break;
            case 2:
                if (!(this.mVelocityTracker == null || this.mDiscardIntercept)) {
                    int pointerIndex = ev.findPointerIndex(this.mActiveTouchId);
                    if (pointerIndex != -1) {
                        float dx = ev.getRawX() - this.mDownX;
                        float x = ev.getX(pointerIndex);
                        float y = ev.getY(pointerIndex);
                        if (dx != 0.0f && canScroll(this, false, dx, x, y)) {
                            this.mDiscardIntercept = true;
                            break;
                        }
                        updateSwiping(ev);
                        break;
                    }
                    Log.e(TAG, "Invalid pointer index: ignoring.");
                    this.mDiscardIntercept = true;
                    break;
                }
                break;
            case 5:
                this.mActiveTouchId = ev.getPointerId(ev.getActionIndex());
                break;
            case 6:
                int actionIndex = ev.getActionIndex();
                if (ev.getPointerId(actionIndex) == this.mActiveTouchId) {
                    this.mActiveTouchId = ev.getPointerId(actionIndex == 0 ? 1 : 0);
                    break;
                }
                break;
        }
        if (!this.mDiscardIntercept) {
            z = this.mSwiping;
        }
        return z;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        checkGesture(ev);
        if (this.mBlockGesture) {
            return true;
        }
        if (this.mVelocityTracker == null || (this.mDismissable ^ 1) != 0) {
            return super.onTouchEvent(ev);
        }
        ev.offsetLocation(ev.getRawX() - ev.getX(), 0.0f);
        switch (ev.getActionMasked()) {
            case 1:
                updateDismiss(ev);
                if (this.mDismissed) {
                    this.mDismissAnimator.animateDismissal(ev.getRawX() - this.mDownX);
                } else if (this.mSwiping && this.mLastX != -2.14748365E9f) {
                    this.mDismissAnimator.animateRecovery(ev.getRawX() - this.mDownX);
                }
                resetMembers();
                break;
            case 2:
                this.mVelocityTracker.addMovement(ev);
                this.mLastX = ev.getRawX();
                updateSwiping(ev);
                if (this.mSwiping) {
                    setProgress(ev.getRawX() - this.mDownX);
                    break;
                }
                break;
            case 3:
                cancel();
                resetMembers();
                break;
        }
        return true;
    }

    private void setProgress(float deltaX) {
        if (this.mProgressListener != null && deltaX >= 0.0f) {
            this.mProgressListener.onSwipeProgressChanged(this, progressToAlpha(deltaX / ((float) getWidth())), deltaX);
        }
    }

    private void dismiss() {
        if (this.mDismissedListener != null) {
            this.mDismissedListener.onDismissed(this);
        }
    }

    protected void cancel() {
        if (!this.mIsWindowNativelyTranslucent) {
            Activity activity = findActivity();
            if (activity != null && this.mActivityTranslucencyConverted) {
                activity.convertFromTranslucent();
                this.mActivityTranslucencyConverted = false;
            }
        }
        if (this.mProgressListener != null) {
            this.mProgressListener.onSwipeCancelled(this);
        }
    }

    private void resetMembers() {
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.recycle();
        }
        this.mVelocityTracker = null;
        this.mDownX = 0.0f;
        this.mLastX = -2.14748365E9f;
        this.mDownY = 0.0f;
        this.mSwiping = false;
        this.mDismissed = false;
        this.mDiscardIntercept = false;
    }

    private void updateSwiping(MotionEvent ev) {
        boolean z = false;
        boolean oldSwiping = this.mSwiping;
        if (!this.mSwiping) {
            float deltaX = ev.getRawX() - this.mDownX;
            float deltaY = ev.getRawY() - this.mDownY;
            if ((deltaX * deltaX) + (deltaY * deltaY) > ((float) (this.mSlop * this.mSlop))) {
                if (deltaX > ((float) (this.mSlop * 2)) && Math.abs(deltaY) < Math.abs(deltaX)) {
                    z = true;
                }
                this.mSwiping = z;
            } else {
                this.mSwiping = false;
            }
        }
        if (this.mSwiping && (oldSwiping ^ 1) != 0 && !this.mIsWindowNativelyTranslucent) {
            Activity activity = findActivity();
            if (activity != null) {
                this.mActivityTranslucencyConverted = activity.convertToTranslucent(null, null);
            }
        }
    }

    private void updateDismiss(MotionEvent ev) {
        float deltaX = ev.getRawX() - this.mDownX;
        this.mVelocityTracker.computeCurrentVelocity(1000);
        float xVelocity = this.mVelocityTracker.getXVelocity();
        if (this.mLastX == -2.14748365E9f) {
            xVelocity = deltaX / ((float) ((ev.getEventTime() - ev.getDownTime()) / 1000));
        }
        if (!this.mDismissed && ((deltaX > ((float) getWidth()) * Math.max(Math.min(((-0.23000002f * xVelocity) / ((float) this.mMinFlingVelocity)) + MAX_DIST_THRESHOLD, MAX_DIST_THRESHOLD), MIN_DIST_THRESHOLD) && ev.getRawX() >= this.mLastX) || xVelocity >= ((float) this.mMinFlingVelocity))) {
            this.mDismissed = true;
        }
        if (this.mDismissed && this.mSwiping && xVelocity < ((float) (-this.mMinFlingVelocity))) {
            this.mDismissed = false;
        }
    }

    protected boolean canScroll(View v, boolean checkV, float dx, float x, float y) {
        if (v instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) v;
            int scrollX = v.getScrollX();
            int scrollY = v.getScrollY();
            for (int i = group.getChildCount() - 1; i >= 0; i--) {
                View child = group.getChildAt(i);
                if (((float) scrollX) + x >= ((float) child.getLeft()) && ((float) scrollX) + x < ((float) child.getRight()) && ((float) scrollY) + y >= ((float) child.getTop()) && ((float) scrollY) + y < ((float) child.getBottom())) {
                    if (canScroll(child, true, dx, (((float) scrollX) + x) - ((float) child.getLeft()), (((float) scrollY) + y) - ((float) child.getTop()))) {
                        return true;
                    }
                }
            }
        }
        return checkV ? v.canScrollHorizontally((int) (-dx)) : false;
    }

    public void setDismissable(boolean dismissable) {
        if (!dismissable && this.mDismissable) {
            cancel();
            resetMembers();
        }
        this.mDismissable = dismissable;
    }

    private void checkGesture(MotionEvent ev) {
        if (ev.getActionMasked() == 0) {
            this.mBlockGesture = this.mDismissAnimator.isAnimating();
        }
    }

    private float progressToAlpha(float progress) {
        return 1.0f - ((progress * progress) * progress);
    }

    private Activity findActivity() {
        for (Context context = getContext(); context instanceof ContextWrapper; context = ((ContextWrapper) context).getBaseContext()) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
        }
        return null;
    }
}
