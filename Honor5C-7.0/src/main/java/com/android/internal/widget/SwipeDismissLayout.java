package com.android.internal.widget;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnEnterAnimationCompleteListener;
import android.widget.FrameLayout;
import com.android.internal.R;
import com.android.internal.telephony.RILConstants;
import huawei.cust.HwCfgFilePolicy;

public class SwipeDismissLayout extends FrameLayout {
    private static final float DISMISS_MIN_DRAG_WIDTH_RATIO = 0.33f;
    private static final String TAG = "SwipeDismissLayout";
    private int mActiveTouchId;
    private boolean mDiscardIntercept;
    private boolean mDismissed;
    private OnDismissedListener mDismissedListener;
    private float mDownX;
    private float mDownY;
    private float mLastX;
    private int mMinFlingVelocity;
    private OnEnterAnimationCompleteListener mOnEnterAnimationCompleteListener;
    private OnSwipeProgressChangedListener mProgressListener;
    private IntentFilter mScreenOffFilter;
    private BroadcastReceiver mScreenOffReceiver;
    private int mSlop;
    private boolean mSwiping;
    private float mTranslationX;
    private boolean mUseDynamicTranslucency;
    private VelocityTracker mVelocityTracker;

    public interface OnDismissedListener {
        void onDismissed(SwipeDismissLayout swipeDismissLayout);
    }

    public interface OnSwipeProgressChangedListener {
        void onSwipeCancelled(SwipeDismissLayout swipeDismissLayout);

        void onSwipeProgressChanged(SwipeDismissLayout swipeDismissLayout, float f, float f2);
    }

    public SwipeDismissLayout(Context context) {
        super(context);
        this.mUseDynamicTranslucency = true;
        this.mOnEnterAnimationCompleteListener = new OnEnterAnimationCompleteListener() {
            public void onEnterAnimationComplete() {
                if (SwipeDismissLayout.this.mUseDynamicTranslucency && (SwipeDismissLayout.this.getContext() instanceof Activity)) {
                    ((Activity) SwipeDismissLayout.this.getContext()).convertFromTranslucent();
                }
            }
        };
        this.mScreenOffReceiver = new BroadcastReceiver() {
            private Runnable mRunnable;

            {
                this.mRunnable = new Runnable() {
                    public void run() {
                        if (SwipeDismissLayout.this.mDismissed) {
                            SwipeDismissLayout.this.dismiss();
                        } else {
                            SwipeDismissLayout.this.cancel();
                        }
                        SwipeDismissLayout.this.resetMembers();
                    }
                };
            }

            public void onReceive(Context context, Intent intent) {
                SwipeDismissLayout.this.post(this.mRunnable);
            }
        };
        this.mScreenOffFilter = new IntentFilter("android.intent.action.SCREEN_OFF");
        init(context);
    }

    public SwipeDismissLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mUseDynamicTranslucency = true;
        this.mOnEnterAnimationCompleteListener = new OnEnterAnimationCompleteListener() {
            public void onEnterAnimationComplete() {
                if (SwipeDismissLayout.this.mUseDynamicTranslucency && (SwipeDismissLayout.this.getContext() instanceof Activity)) {
                    ((Activity) SwipeDismissLayout.this.getContext()).convertFromTranslucent();
                }
            }
        };
        this.mScreenOffReceiver = new BroadcastReceiver() {
            private Runnable mRunnable;

            {
                this.mRunnable = new Runnable() {
                    public void run() {
                        if (SwipeDismissLayout.this.mDismissed) {
                            SwipeDismissLayout.this.dismiss();
                        } else {
                            SwipeDismissLayout.this.cancel();
                        }
                        SwipeDismissLayout.this.resetMembers();
                    }
                };
            }

            public void onReceive(Context context, Intent intent) {
                SwipeDismissLayout.this.post(this.mRunnable);
            }
        };
        this.mScreenOffFilter = new IntentFilter("android.intent.action.SCREEN_OFF");
        init(context);
    }

    public SwipeDismissLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mUseDynamicTranslucency = true;
        this.mOnEnterAnimationCompleteListener = new OnEnterAnimationCompleteListener() {
            public void onEnterAnimationComplete() {
                if (SwipeDismissLayout.this.mUseDynamicTranslucency && (SwipeDismissLayout.this.getContext() instanceof Activity)) {
                    ((Activity) SwipeDismissLayout.this.getContext()).convertFromTranslucent();
                }
            }
        };
        this.mScreenOffReceiver = new BroadcastReceiver() {
            private Runnable mRunnable;

            {
                this.mRunnable = new Runnable() {
                    public void run() {
                        if (SwipeDismissLayout.this.mDismissed) {
                            SwipeDismissLayout.this.dismiss();
                        } else {
                            SwipeDismissLayout.this.cancel();
                        }
                        SwipeDismissLayout.this.resetMembers();
                    }
                };
            }

            public void onReceive(Context context, Intent intent) {
                SwipeDismissLayout.this.post(this.mRunnable);
            }
        };
        this.mScreenOffFilter = new IntentFilter("android.intent.action.SCREEN_OFF");
        init(context);
    }

    private void init(Context context) {
        ViewConfiguration vc = ViewConfiguration.get(context);
        this.mSlop = vc.getScaledTouchSlop();
        this.mMinFlingVelocity = vc.getScaledMinimumFlingVelocity();
        TypedArray a = context.getTheme().obtainStyledAttributes(R.styleable.Theme);
        this.mUseDynamicTranslucency = !a.hasValue(5);
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
        if (getContext() instanceof Activity) {
            getViewTreeObserver().addOnEnterAnimationCompleteListener(this.mOnEnterAnimationCompleteListener);
        }
        getContext().registerReceiver(this.mScreenOffReceiver, this.mScreenOffFilter);
    }

    protected void onDetachedFromWindow() {
        getContext().unregisterReceiver(this.mScreenOffReceiver);
        if (getContext() instanceof Activity) {
            getViewTreeObserver().removeOnEnterAnimationCompleteListener(this.mOnEnterAnimationCompleteListener);
        }
        super.onDetachedFromWindow();
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int newActionIndex = 1;
        ev.offsetLocation(this.mTranslationX, 0.0f);
        switch (ev.getActionMasked()) {
            case HwCfgFilePolicy.GLOBAL /*0*/:
                resetMembers();
                this.mDownX = ev.getRawX();
                this.mDownY = ev.getRawY();
                this.mActiveTouchId = ev.getPointerId(0);
                this.mVelocityTracker = VelocityTracker.obtain();
                this.mVelocityTracker.addMovement(ev);
                break;
            case HwCfgFilePolicy.EMUI /*1*/:
            case HwCfgFilePolicy.BASE /*3*/:
                resetMembers();
                break;
            case HwCfgFilePolicy.PC /*2*/:
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
            case HwCfgFilePolicy.CLOUD_MCC /*5*/:
                this.mActiveTouchId = ev.getPointerId(ev.getActionIndex());
                break;
            case HwCfgFilePolicy.CLOUD_DPLMN /*6*/:
                int actionIndex = ev.getActionIndex();
                if (ev.getPointerId(actionIndex) == this.mActiveTouchId) {
                    if (actionIndex != 0) {
                        newActionIndex = 0;
                    }
                    this.mActiveTouchId = ev.getPointerId(newActionIndex);
                    break;
                }
                break;
        }
        if (this.mDiscardIntercept) {
            return false;
        }
        return this.mSwiping;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        if (this.mVelocityTracker == null) {
            return super.onTouchEvent(ev);
        }
        ev.offsetLocation(this.mTranslationX, 0.0f);
        switch (ev.getActionMasked()) {
            case HwCfgFilePolicy.EMUI /*1*/:
                updateDismiss(ev);
                if (this.mDismissed) {
                    dismiss();
                } else if (this.mSwiping) {
                    cancel();
                }
                resetMembers();
                break;
            case HwCfgFilePolicy.PC /*2*/:
                this.mVelocityTracker.addMovement(ev);
                this.mLastX = ev.getRawX();
                updateSwiping(ev);
                if (this.mSwiping) {
                    if (this.mUseDynamicTranslucency && (getContext() instanceof Activity)) {
                        ((Activity) getContext()).convertToTranslucent(null, null);
                    }
                    setProgress(ev.getRawX() - this.mDownX);
                    break;
                }
                break;
            case HwCfgFilePolicy.BASE /*3*/:
                cancel();
                resetMembers();
                break;
        }
        return true;
    }

    private void setProgress(float deltaX) {
        this.mTranslationX = deltaX;
        if (this.mProgressListener != null && deltaX >= 0.0f) {
            this.mProgressListener.onSwipeProgressChanged(this, deltaX / ((float) getWidth()), deltaX);
        }
    }

    private void dismiss() {
        if (this.mDismissedListener != null) {
            this.mDismissedListener.onDismissed(this);
        }
    }

    protected void cancel() {
        if (this.mUseDynamicTranslucency && (getContext() instanceof Activity)) {
            ((Activity) getContext()).convertFromTranslucent();
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
        this.mTranslationX = 0.0f;
        this.mDownX = 0.0f;
        this.mDownY = 0.0f;
        this.mSwiping = false;
        this.mDismissed = false;
        this.mDiscardIntercept = false;
    }

    private void updateSwiping(MotionEvent ev) {
        boolean z = false;
        if (!this.mSwiping) {
            float deltaX = ev.getRawX() - this.mDownX;
            float deltaY = ev.getRawY() - this.mDownY;
            if ((deltaX * deltaX) + (deltaY * deltaY) > ((float) (this.mSlop * this.mSlop))) {
                if (deltaX > ((float) (this.mSlop * 2)) && Math.abs(deltaY) < Math.abs(deltaX)) {
                    z = true;
                }
                this.mSwiping = z;
                return;
            }
            this.mSwiping = false;
        }
    }

    private void updateDismiss(MotionEvent ev) {
        float deltaX = ev.getRawX() - this.mDownX;
        this.mVelocityTracker.addMovement(ev);
        this.mVelocityTracker.computeCurrentVelocity(RILConstants.RIL_UNSOL_RESPONSE_RADIO_STATE_CHANGED);
        if (!this.mDismissed && deltaX > ((float) getWidth()) * DISMISS_MIN_DRAG_WIDTH_RATIO && ev.getRawX() >= this.mLastX) {
            this.mDismissed = true;
        }
        if (!this.mDismissed || !this.mSwiping) {
            return;
        }
        if (deltaX < ((float) getWidth()) * DISMISS_MIN_DRAG_WIDTH_RATIO || this.mVelocityTracker.getXVelocity() < ((float) (-this.mMinFlingVelocity))) {
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
}
