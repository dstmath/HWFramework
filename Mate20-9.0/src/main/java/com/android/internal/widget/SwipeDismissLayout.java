package com.android.internal.widget;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
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
import com.android.internal.widget.SwipeDismissLayout;

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
    /* access modifiers changed from: private */
    public boolean mDismissed;
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

    private class DismissAnimator implements ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener {
        private final long DISMISS_DURATION = 250;
        private final TimeInterpolator DISMISS_INTERPOLATOR = new DecelerateInterpolator(1.5f);
        private final ValueAnimator mDismissAnimator = new ValueAnimator();
        private boolean mDismissOnComplete = false;
        private boolean mWasCanceled = false;

        DismissAnimator() {
            this.mDismissAnimator.addUpdateListener(this);
            this.mDismissAnimator.addListener(this);
        }

        /* access modifiers changed from: package-private */
        public void animateDismissal(float currentTranslation) {
            animate(currentTranslation / ((float) SwipeDismissLayout.this.getWidth()), 1.0f, 250, this.DISMISS_INTERPOLATOR, true);
        }

        /* access modifiers changed from: package-private */
        public void animateRecovery(float currentTranslation) {
            animate(currentTranslation / ((float) SwipeDismissLayout.this.getWidth()), 0.0f, 250, this.DISMISS_INTERPOLATOR, false);
        }

        /* access modifiers changed from: package-private */
        public boolean isAnimating() {
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
            if (this.mWasCanceled) {
                return;
            }
            if (this.mDismissOnComplete) {
                SwipeDismissLayout.this.dismiss();
            } else {
                SwipeDismissLayout.this.cancel();
            }
        }

        public void onAnimationRepeat(Animator animation) {
        }
    }

    public interface OnDismissedListener {
        void onDismissed(SwipeDismissLayout swipeDismissLayout);
    }

    public interface OnSwipeProgressChangedListener {
        void onSwipeCancelled(SwipeDismissLayout swipeDismissLayout);

        void onSwipeProgressChanged(SwipeDismissLayout swipeDismissLayout, float f, float f2);
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

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        try {
            this.mScreenOffReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    SwipeDismissLayout.this.post(new Runnable() {
                        public final void run() {
                            SwipeDismissLayout.AnonymousClass1.lambda$onReceive$0(SwipeDismissLayout.AnonymousClass1.this);
                        }
                    });
                }

                public static /* synthetic */ void lambda$onReceive$0(AnonymousClass1 r1) {
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

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        if (this.mScreenOffReceiver != null) {
            getContext().unregisterReceiver(this.mScreenOffReceiver);
            this.mScreenOffReceiver = null;
        }
        super.onDetachedFromWindow();
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        checkGesture(ev);
        boolean z = true;
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
                if (this.mVelocityTracker != null && !this.mDiscardIntercept) {
                    int pointerIndex = ev.findPointerIndex(this.mActiveTouchId);
                    if (pointerIndex != -1) {
                        float dx = ev.getRawX() - this.mDownX;
                        float x = ev.getX(pointerIndex);
                        float y = ev.getY(pointerIndex);
                        if (dx != 0.0f && canScroll(this, false, dx, x, y)) {
                            this.mDiscardIntercept = true;
                            break;
                        } else {
                            updateSwiping(ev);
                            break;
                        }
                    } else {
                        Log.e(TAG, "Invalid pointer index: ignoring.");
                        this.mDiscardIntercept = true;
                        break;
                    }
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
        if (this.mDiscardIntercept || !this.mSwiping) {
            z = false;
        }
        return z;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        checkGesture(ev);
        if (this.mBlockGesture) {
            return true;
        }
        if (this.mVelocityTracker == null || !this.mDismissable) {
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

    /* access modifiers changed from: private */
    public void setProgress(float deltaX) {
        if (this.mProgressListener != null && deltaX >= 0.0f) {
            this.mProgressListener.onSwipeProgressChanged(this, progressToAlpha(deltaX / ((float) getWidth())), deltaX);
        }
    }

    /* access modifiers changed from: private */
    public void dismiss() {
        if (this.mDismissedListener != null) {
            this.mDismissedListener.onDismissed(this);
        }
    }

    /* access modifiers changed from: protected */
    public void cancel() {
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

    /* access modifiers changed from: private */
    public void resetMembers() {
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
        boolean oldSwiping = this.mSwiping;
        if (!this.mSwiping) {
            float deltaX = ev.getRawX() - this.mDownX;
            float deltaY = ev.getRawY() - this.mDownY;
            boolean z = false;
            if ((deltaX * deltaX) + (deltaY * deltaY) > ((float) (this.mSlop * this.mSlop))) {
                if (deltaX > ((float) (this.mSlop * 2)) && Math.abs(deltaY) < Math.abs(deltaX)) {
                    z = true;
                }
                this.mSwiping = z;
            } else {
                this.mSwiping = false;
            }
        }
        if (this.mSwiping && !oldSwiping && !this.mIsWindowNativelyTranslucent) {
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

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x007a, code lost:
        if (r0.canScrollHorizontally((int) (-r17)) != false) goto L_0x0080;
     */
    public boolean canScroll(View v, boolean checkV, float dx, float x, float y) {
        View view = v;
        boolean z = true;
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            int scrollX = view.getScrollX();
            int scrollY = view.getScrollY();
            for (int i = group.getChildCount() - 1; i >= 0; i--) {
                View child = group.getChildAt(i);
                if (x + ((float) scrollX) >= ((float) child.getLeft()) && x + ((float) scrollX) < ((float) child.getRight()) && y + ((float) scrollY) >= ((float) child.getTop()) && y + ((float) scrollY) < ((float) child.getBottom())) {
                    if (canScroll(child, true, dx, (x + ((float) scrollX)) - ((float) child.getLeft()), (y + ((float) scrollY)) - ((float) child.getTop()))) {
                        return true;
                    }
                }
            }
        }
        if (!checkV) {
            float f = dx;
        }
        z = false;
        return z;
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
