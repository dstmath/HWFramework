package com.huawei.android.hwcontrol;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.hwcontrol.HwWidgetFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.TextView;
import com.huawei.android.hardware.HwSensorActionDetectorAdapter;
import com.huawei.android.hardware.HwSensorManager;

public class HwDialogStubImpl implements HwWidgetFactory.HwDialogStub {
    private static final int DIALOG_AIMATE_TO_BOTTOM = 4;
    private static final int DIALOG_AIMATE_TO_TOP = 3;
    private static final int DIALOG_AIMATE_UP_DOWN_DURATION = 400;
    private static final float EXTRA_MARGIN = 0.5f;
    private static final int HALF_DIV = 2;
    private static final int INIT = -1;
    private static final boolean IS_DEBUG = false;
    private static final int MAX_LOCATION = 2;
    private static final int PERCENT = 6;
    private static final float ROTATION_ANGLE = 0.34906587f;
    private static final float ROTATION_SPEED = 5.6548667f;
    private static final int STATE_DOWN = 1;
    private static final int STATE_TOP = 0;
    private static final String TAG = "DialogFactory";
    private static final int VERTICAL_MARGIN = 36;
    private ValueAnimator mAnimator;
    private Context mContext;
    private Dialog mDialog;
    private final Handler mHandler = new Handler();
    private boolean mIsEverReset;
    private boolean mIsInputMethodOn;
    private int mLastY;
    private int mMask;
    private int mOffset = 0;
    private ViewTreeObserver.OnGlobalLayoutListener mOnGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        /* class com.huawei.android.hwcontrol.HwDialogStubImpl.AnonymousClass3 */
        private int screenHeight;

        @Override // android.view.ViewTreeObserver.OnGlobalLayoutListener
        public void onGlobalLayout() {
            if (HwDialogStubImpl.this.mWindow != null && HwDialogStubImpl.this.mAnimator != null && !HwDialogStubImpl.this.mAnimator.isRunning()) {
                View view = HwDialogStubImpl.this.mWindow.getDecorView();
                if (view == null) {
                    Log.i(HwDialogStubImpl.TAG, "OnGlobalLayoutListener view is null");
                } else if (HwDialogStubImpl.this.isInputMethodTarget(view)) {
                    int locationY = HwDialogStubImpl.this.getOnScreenLocationY(view);
                    if (HwDialogStubImpl.this.mLastY == 0) {
                        HwDialogStubImpl.this.mLastY = locationY;
                        return;
                    }
                    int deltaLimit = getScreenHeight() / 6;
                    boolean isChangeIgnored = Math.abs(HwDialogStubImpl.this.mLastY - locationY) < deltaLimit;
                    boolean isChangeY = HwDialogStubImpl.this.mLastY != 0 && HwDialogStubImpl.this.mLastY - locationY > deltaLimit;
                    if (!isChangeIgnored) {
                        HwDialogStubImpl hwDialogStubImpl = HwDialogStubImpl.this;
                        hwDialogStubImpl.mIsInputMethodOn = hwDialogStubImpl.mIsEverReset || isChangeY;
                    }
                    HwDialogStubImpl.this.mIsEverReset = false;
                    if (!HwDialogStubImpl.this.mIsInputMethodOn || HwDialogStubImpl.this.mState != 1) {
                        HwDialogStubImpl.this.mLastY = locationY;
                    } else if (Looper.myLooper() == HwDialogStubImpl.this.mHandler.getLooper()) {
                        HwDialogStubImpl.this.resetPositionForInputMethodOn();
                    } else {
                        HwDialogStubImpl.this.mHandler.post(HwDialogStubImpl.this.mResetAction);
                    }
                }
            }
        }

        private int getScreenHeight() {
            int i = this.screenHeight;
            if (i > 0) {
                return i;
            }
            Resources res = HwDialogStubImpl.this.mContext.getResources();
            this.screenHeight = res.getDisplayMetrics().heightPixels - res.getDimensionPixelSize(17105445);
            return this.screenHeight;
        }
    };
    private final Runnable mResetAction = new Runnable() {
        /* class com.huawei.android.hwcontrol.HwDialogStubImpl.AnonymousClass2 */

        @Override // java.lang.Runnable
        public void run() {
            HwDialogStubImpl.this.resetPositionForInputMethodOn();
        }
    };
    private HwSensorManager mSensorManager;
    private final Runnable mStartAction = new Runnable() {
        /* class com.huawei.android.hwcontrol.HwDialogStubImpl.AnonymousClass1 */

        @Override // java.lang.Runnable
        public void run() {
            HwDialogStubImpl.this.startAnim();
        }
    };
    private int mState = 0;
    private Window mWindow;

    public HwDialogStubImpl(Context context, Window window, Dialog dialog, int mask) {
        this.mDialog = dialog;
        this.mMask = mask;
        this.mContext = context;
        this.mWindow = window;
        this.mSensorManager = new HwSensorManager(context);
    }

    public void showDialogFactory() {
        Window window;
        if (this.mSensorManager != null && (window = this.mWindow) != null && this.mContext != null) {
            if (window.getAttributes().type == 2011) {
                this.mSensorManager = null;
                return;
            }
            WindowManager windowManager = this.mWindow.getWindowManager();
            final View view = this.mWindow.getDecorView();
            if (windowManager != null && view != null) {
                boolean isRegister = this.mSensorManager.registerSensorListener(2);
                if (isRegister) {
                    addInputMethodListener(view);
                    this.mLastY = getOnScreenLocationY(view);
                    if (this.mAnimator == null) {
                        initAnimatorParams(windowManager, view);
                    }
                    this.mSensorManager.setRotationAngle(ROTATION_ANGLE);
                    this.mSensorManager.setAngularSpeedThreshold(ROTATION_SPEED);
                    this.mSensorManager.setSensorListener(new HwSensorActionDetectorAdapter() {
                        /* class com.huawei.android.hwcontrol.HwDialogStubImpl.AnonymousClass4 */

                        @Override // com.huawei.android.hardware.HwSensorActionDetectorAdapter, com.huawei.android.hardware.HwSensorManager.SensorEventDetector
                        public void onDirectionChanged(int direction) {
                            super.onDirectionChanged(direction);
                            HwDialogStubImpl.this.sensorManagerOnDirectionChanged(view, direction);
                        }
                    });
                    return;
                }
                Log.w(TAG, "showDialogFactory sensor register fail, isRegister = " + isRegister);
            }
        }
    }

    public void dismissDialogFactory() {
        Window window;
        if (this.mSensorManager != null && (window = this.mWindow) != null && this.mContext != null && window.getAttributes().type != 2011) {
            removeInputMethodListener(this.mWindow.getDecorView());
            ValueAnimator valueAnimator = this.mAnimator;
            if (valueAnimator != null && valueAnimator.isRunning()) {
                this.mAnimator.cancel();
                this.mAnimator.removeAllUpdateListeners();
            }
            this.mAnimator = null;
            this.mSensorManager.setMoveDirection(-1);
            this.mSensorManager.unRegisterListeners();
            this.mSensorManager.setSensorListener(null);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sensorManagerOnDirectionChanged(View view, int direction) {
        ValueAnimator valueAnimator;
        if (!this.mIsInputMethodOn && isShouldChange(this.mState, direction) && (valueAnimator = this.mAnimator) != null && !valueAnimator.isRunning() && this.mContext.getResources().getConfiguration().orientation == 1) {
            setState(view, direction);
            if (Looper.myLooper() == this.mHandler.getLooper()) {
                startAnim();
            } else {
                this.mHandler.post(this.mStartAction);
            }
        }
    }

    private void initAnimatorParams(final WindowManager windowManager, final View view) {
        Interpolator interpolator = AnimationUtils.loadInterpolator(this.mContext, 34078723);
        this.mAnimator = ValueAnimator.ofPropertyValuesHolder(PropertyValuesHolder.ofFloat("y", 0.0f, 0.0f));
        this.mAnimator.setInterpolator(interpolator);
        this.mAnimator.setDuration(400L);
        this.mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.huawei.android.hwcontrol.HwDialogStubImpl.AnonymousClass5 */

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = animation.getAnimatedFraction();
                WindowManager.LayoutParams params = HwDialogStubImpl.this.mWindow.getAttributes();
                params.y = (int) (HwDialogStubImpl.this.mState == 1 ? ((float) HwDialogStubImpl.this.mOffset) * fraction : ((float) HwDialogStubImpl.this.mOffset) * (1.0f - fraction));
                View view = view;
                if (view != null) {
                    try {
                        windowManager.updateViewLayout(view, params);
                    } catch (IllegalArgumentException e) {
                    }
                }
            }
        });
        this.mAnimator.addListener(new AnimatorListenerAdapter() {
            /* class com.huawei.android.hwcontrol.HwDialogStubImpl.AnonymousClass6 */

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator anim) {
                super.onAnimationEnd(anim);
                HwDialogStubImpl hwDialogStubImpl = HwDialogStubImpl.this;
                hwDialogStubImpl.mLastY = hwDialogStubImpl.getOnScreenLocationY(view);
            }
        });
    }

    private boolean isShouldChange(int state, int direction) {
        if (state == 0 && direction == 4) {
            return true;
        }
        return state == 1 && direction == 3;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startAnim() {
        ValueAnimator valueAnimator = this.mAnimator;
        if (valueAnimator != null) {
            valueAnimator.start();
        }
    }

    private void setState(View view, int direction) {
        Context context = this.mContext;
        if (context != null) {
            Resources res = context.getResources();
            int offset = (((res.getDisplayMetrics().heightPixels - res.getDimensionPixelSize(17105445)) - (view != null ? view.getHeight() : 0)) / 2) - ((int) ((res.getDisplayMetrics().density * 36.0f) + 0.5f));
            this.mOffset = offset < 0 ? 0 : offset;
            if (direction == 3) {
                this.mState = 0;
            } else if (direction == 4) {
                this.mState = 1;
            }
        }
    }

    private ViewTreeObserver getViewTreeObserver(View view) {
        if (view == null) {
            Log.i(TAG, "getViewTreeObserver view is null");
            return null;
        }
        ViewTreeObserver observer = view.getViewTreeObserver();
        if (observer == null) {
            Log.i(TAG, "getViewTreeObserver observer is null");
            return null;
        }
        if (!observer.isAlive()) {
            observer = view.getViewTreeObserver();
            if (!observer.isAlive()) {
                return null;
            }
        }
        return observer;
    }

    private void addInputMethodListener(View view) {
        ViewTreeObserver observer = getViewTreeObserver(view);
        if (observer != null) {
            try {
                observer.addOnGlobalLayoutListener(this.mOnGlobalLayoutListener);
            } catch (IllegalStateException e) {
                Log.i(TAG, "addOnGlobalLayoutListener IllegalStateException");
            }
        }
    }

    private void removeInputMethodListener(View view) {
        ViewTreeObserver observer = getViewTreeObserver(view);
        if (observer != null) {
            try {
                observer.removeOnGlobalLayoutListener(this.mOnGlobalLayoutListener);
            } catch (IllegalStateException e) {
                Log.i(TAG, "removeOnGlobalLayoutListener IllegalStateException");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isInputMethodTarget(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            int count = group.getChildCount();
            for (int i = 0; i < count; i++) {
                if (isInputMethodTarget(group.getChildAt(i))) {
                    return true;
                }
            }
            return false;
        } else if (view instanceof TextView) {
            return ((TextView) view).isInputMethodTarget();
        } else {
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getOnScreenLocationY(View view) {
        if (view == null) {
            Log.w(TAG, "getOnScreenLocationY view is null");
            return 0;
        }
        int[] locations = new int[2];
        view.getLocationOnScreen(locations);
        return locations[1];
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetPositionForInputMethodOn() {
        Window window = this.mWindow;
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            View view = this.mWindow.getDecorView();
            params.y = 0;
            this.mWindow.getWindowManager().updateViewLayout(view, params);
            this.mState = 0;
            this.mIsEverReset = true;
        }
    }

    public boolean hasButtons() {
        return hasButton(-1) || hasButton(-2) || hasButton(-3);
    }

    private boolean hasButton(int whichButton) {
        Button button;
        Dialog dialog = this.mDialog;
        if (dialog == null || !(dialog instanceof AlertDialog) || (button = ((AlertDialog) dialog).getButton(whichButton)) == null || button.getVisibility() != 0) {
            return false;
        }
        return true;
    }

    public int getMask() {
        return this.mMask;
    }
}
