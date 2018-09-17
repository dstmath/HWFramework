package com.huawei.android.hwcontrol;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.hwcontrol.HwWidgetFactory.HwDialogStub;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.TextView;
import com.huawei.android.hardware.HwSensorActionDetectorAdapter;
import com.huawei.android.hardware.HwSensorManager;

public class HwDialogStubImpl implements HwDialogStub {
    private static final boolean DB = false;
    private static final int DIALOG_AIMATE_TO_BOTTOM = 4;
    private static final int DIALOG_AIMATE_TO_TOP = 3;
    private static final int DIALOG_AIMATE_UP_DOWN_DURATION = 400;
    private static final int INIT = -1;
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
    private OnGlobalLayoutListener mOnGlobalLayoutListener = new OnGlobalLayoutListener() {
        private int screenHeight;

        public void onGlobalLayout() {
            if (!(HwDialogStubImpl.this.mWindow == null || HwDialogStubImpl.this.mAnimator == null || (HwDialogStubImpl.this.mAnimator.isRunning() ^ 1) == 0)) {
                View view = HwDialogStubImpl.this.mWindow.getDecorView();
                if (view == null) {
                    Log.i(HwDialogStubImpl.TAG, "OnGlobalLayoutListener view is null");
                } else if (HwDialogStubImpl.this.isInputMethodTarget(view)) {
                    int locationY = HwDialogStubImpl.this.getLocationYOnScreen(view);
                    if (HwDialogStubImpl.this.mLastY == 0) {
                        HwDialogStubImpl.this.mLastY = locationY;
                        return;
                    }
                    int deltLimit = getScreenHeight() / 6;
                    boolean ignoreChange = Math.abs(HwDialogStubImpl.this.mLastY - locationY) < deltLimit;
                    boolean isChangeY = HwDialogStubImpl.this.mLastY != 0 && HwDialogStubImpl.this.mLastY - locationY > deltLimit;
                    if (!ignoreChange) {
                        HwDialogStubImpl hwDialogStubImpl = HwDialogStubImpl.this;
                        if (HwDialogStubImpl.this.mIsEverReset) {
                            isChangeY = true;
                        }
                        hwDialogStubImpl.mIsInputMethodOn = isChangeY;
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
            if (this.screenHeight > 0) {
                return this.screenHeight;
            }
            Resources res = HwDialogStubImpl.this.mContext.getResources();
            this.screenHeight = res.getDisplayMetrics().heightPixels - res.getDimensionPixelSize(17105234);
            return this.screenHeight;
        }
    };
    private final Runnable mResetAction = new Runnable() {
        public void run() {
            HwDialogStubImpl.this.resetPositionForInputMethodOn();
        }
    };
    private HwSensorManager mSensorManager;
    private final Runnable mStartAction = new Runnable() {
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
        if (this.mSensorManager != null && this.mWindow != null && this.mContext != null) {
            if (this.mWindow.getAttributes().type == 2011) {
                this.mSensorManager = null;
                return;
            }
            WindowManager wm = this.mWindow.getWindowManager();
            final View view = this.mWindow.getDecorView();
            if (wm != null && view != null) {
                boolean isRegister = this.mSensorManager.registerSensorListener(2);
                if (isRegister) {
                    addInputMethodListener(view);
                    this.mLastY = getLocationYOnScreen(view);
                    if (this.mAnimator == null) {
                        initAnimatorParams(wm, view);
                    }
                    this.mSensorManager.setRotationAngle(ROTATION_ANGLE);
                    this.mSensorManager.setAngularSpeedThreshold(ROTATION_SPEED);
                    this.mSensorManager.setSensorListener(new HwSensorActionDetectorAdapter() {
                        /* JADX WARNING: Missing block: B:10:0x002f, code:
            return;
     */
                        /* Code decompiled incorrectly, please refer to instructions dump. */
                        public void onDirectionChanged(int direction) {
                            super.onDirectionChanged(direction);
                            if (!(HwDialogStubImpl.this.mIsInputMethodOn || !HwDialogStubImpl.this.isShouldChange(HwDialogStubImpl.this.mState, direction) || HwDialogStubImpl.this.mAnimator == null || HwDialogStubImpl.this.mAnimator.isRunning() || HwDialogStubImpl.this.mContext.getResources().getConfiguration().orientation != 1)) {
                                HwDialogStubImpl.this.setState(view, direction);
                                if (Looper.myLooper() == HwDialogStubImpl.this.mHandler.getLooper()) {
                                    HwDialogStubImpl.this.startAnim();
                                } else {
                                    HwDialogStubImpl.this.mHandler.post(HwDialogStubImpl.this.mStartAction);
                                }
                            }
                        }
                    });
                } else {
                    Log.w(TAG, "showDialogFactory sensor register fail, isRegister = " + isRegister);
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:4:0x0009, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void dismissDialogFactory() {
        if (this.mSensorManager != null && this.mWindow != null && this.mContext != null && this.mWindow.getAttributes().type != 2011) {
            removeInputMethodListener(this.mWindow.getDecorView());
            if (this.mAnimator != null && this.mAnimator.isRunning()) {
                this.mAnimator.cancel();
                this.mAnimator.removeAllUpdateListeners();
            }
            this.mAnimator = null;
            this.mSensorManager.setMoveDirection(-1);
            this.mSensorManager.unRegisterListeners();
            this.mSensorManager.setSensorListener(null);
        }
    }

    private void initAnimatorParams(final WindowManager windowManager, final View view) {
        Interpolator interpolator = AnimationUtils.loadInterpolator(this.mContext, 34078723);
        PropertyValuesHolder holder = PropertyValuesHolder.ofFloat("y", new float[]{0.0f, 0.0f});
        this.mAnimator = ValueAnimator.ofPropertyValuesHolder(new PropertyValuesHolder[]{holder});
        this.mAnimator.setInterpolator(interpolator);
        this.mAnimator.setDuration(400);
        this.mAnimator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = animation.getAnimatedFraction();
                LayoutParams l = HwDialogStubImpl.this.mWindow.getAttributes();
                l.y = HwDialogStubImpl.this.mState == 1 ? (int) (((float) HwDialogStubImpl.this.mOffset) * fraction) : (int) (((float) HwDialogStubImpl.this.mOffset) * (1.0f - fraction));
                if (view != null) {
                    try {
                        windowManager.updateViewLayout(view, l);
                    } catch (IllegalArgumentException e) {
                    }
                }
            }
        });
        this.mAnimator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator anim) {
                super.onAnimationEnd(anim);
                HwDialogStubImpl.this.mLastY = HwDialogStubImpl.this.getLocationYOnScreen(view);
            }
        });
    }

    private boolean isShouldChange(int state, int direction) {
        if (state == 0 && direction == 4) {
            return true;
        }
        if (state == 1 && direction == 3) {
            return true;
        }
        return false;
    }

    private void startAnim() {
        if (this.mAnimator != null) {
            this.mAnimator.start();
        }
    }

    private void setState(View view, int direction) {
        if (this.mContext != null) {
            Resources res = this.mContext.getResources();
            int offset = (((res.getDisplayMetrics().heightPixels - res.getDimensionPixelSize(17105234)) - (view != null ? view.getHeight() : 0)) / 2) - ((int) ((res.getDisplayMetrics().density * 36.0f) + 0.5f));
            if (offset < 0) {
                offset = 0;
            }
            this.mOffset = offset;
            switch (direction) {
                case 3:
                    this.mState = 0;
                    break;
                case 4:
                    this.mState = 1;
                    break;
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

    private int getLocationYOnScreen(View view) {
        if (view == null) {
            Log.w(TAG, "getLocationYOnScreen view is null");
            return 0;
        }
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return location[1];
    }

    private void resetPositionForInputMethodOn() {
        if (this.mWindow != null) {
            LayoutParams l = this.mWindow.getAttributes();
            View view = this.mWindow.getDecorView();
            l.y = 0;
            this.mWindow.getWindowManager().updateViewLayout(view, l);
            this.mState = 0;
            this.mIsEverReset = true;
        }
    }

    public boolean hasButtons() {
        if (hasButton(-1) || hasButton(-2)) {
            return true;
        }
        return hasButton(-3);
    }

    private boolean hasButton(int whichButton) {
        boolean z = false;
        if (this.mDialog == null || !(this.mDialog instanceof AlertDialog)) {
            return false;
        }
        Button button = this.mDialog.getButton(whichButton);
        if (button != null && button.getVisibility() == 0) {
            z = true;
        }
        return z;
    }

    public int getMask() {
        return this.mMask;
    }
}
