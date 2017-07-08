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
import huawei.com.android.internal.widget.HwFragmentMenuItemView;

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
    private final Handler mHandler;
    private boolean mIsEverReset;
    private boolean mIsInputMethodOn;
    private int mLastY;
    private int mMask;
    private int mOffset;
    private OnGlobalLayoutListener mOnGlobalLayoutListener;
    private final Runnable mResetAction;
    private HwSensorManager mSensorManager;
    private final Runnable mStartAction;
    private int mState;
    private Window mWindow;

    /* renamed from: com.huawei.android.hwcontrol.HwDialogStubImpl.4 */
    class AnonymousClass4 extends HwSensorActionDetectorAdapter {
        final /* synthetic */ View val$view;

        AnonymousClass4(View val$view) {
            this.val$view = val$view;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onDirectionChanged(int direction) {
            super.onDirectionChanged(direction);
            if (!(HwDialogStubImpl.this.mIsInputMethodOn || !HwDialogStubImpl.this.isShouldChange(HwDialogStubImpl.this.mState, direction) || HwDialogStubImpl.this.mAnimator == null || HwDialogStubImpl.this.mAnimator.isRunning() || HwDialogStubImpl.this.mContext.getResources().getConfiguration().orientation != HwDialogStubImpl.STATE_DOWN)) {
                HwDialogStubImpl.this.setState(this.val$view, direction);
                if (Looper.myLooper() == HwDialogStubImpl.this.mHandler.getLooper()) {
                    HwDialogStubImpl.this.startAnim();
                } else {
                    HwDialogStubImpl.this.mHandler.post(HwDialogStubImpl.this.mStartAction);
                }
            }
        }
    }

    /* renamed from: com.huawei.android.hwcontrol.HwDialogStubImpl.5 */
    class AnonymousClass5 implements AnimatorUpdateListener {
        final /* synthetic */ View val$view;
        final /* synthetic */ WindowManager val$windowManager;

        AnonymousClass5(View val$view, WindowManager val$windowManager) {
            this.val$view = val$view;
            this.val$windowManager = val$windowManager;
        }

        public void onAnimationUpdate(ValueAnimator animation) {
            float fraction = animation.getAnimatedFraction();
            LayoutParams l = HwDialogStubImpl.this.mWindow.getAttributes();
            l.y = HwDialogStubImpl.this.mState == HwDialogStubImpl.STATE_DOWN ? (int) (((float) HwDialogStubImpl.this.mOffset) * fraction) : (int) (((float) HwDialogStubImpl.this.mOffset) * (HwFragmentMenuItemView.ALPHA_NORMAL - fraction));
            if (this.val$view != null) {
                try {
                    this.val$windowManager.updateViewLayout(this.val$view, l);
                } catch (IllegalArgumentException e) {
                }
            }
        }
    }

    /* renamed from: com.huawei.android.hwcontrol.HwDialogStubImpl.6 */
    class AnonymousClass6 extends AnimatorListenerAdapter {
        final /* synthetic */ View val$view;

        AnonymousClass6(View val$view) {
            this.val$view = val$view;
        }

        public void onAnimationEnd(Animator anim) {
            super.onAnimationEnd(anim);
            HwDialogStubImpl.this.mLastY = HwDialogStubImpl.this.getLocationYOnScreen(this.val$view);
        }
    }

    public HwDialogStubImpl(Context context, Window window, Dialog dialog, int mask) {
        this.mState = STATE_TOP;
        this.mOffset = STATE_TOP;
        this.mHandler = new Handler();
        this.mStartAction = new Runnable() {
            public void run() {
                HwDialogStubImpl.this.startAnim();
            }
        };
        this.mResetAction = new Runnable() {
            public void run() {
                HwDialogStubImpl.this.resetPositionForInputMethodOn();
            }
        };
        this.mOnGlobalLayoutListener = new OnGlobalLayoutListener() {
            private int screenHeight;

            public void onGlobalLayout() {
                if (!(HwDialogStubImpl.this.mWindow == null || HwDialogStubImpl.this.mAnimator == null || HwDialogStubImpl.this.mAnimator.isRunning())) {
                    View view = HwDialogStubImpl.this.mWindow.getDecorView();
                    if (view == null) {
                        Log.i(HwDialogStubImpl.TAG, "OnGlobalLayoutListener view is null");
                    } else if (HwDialogStubImpl.this.isInputMethodTarget(view)) {
                        int locationY = HwDialogStubImpl.this.getLocationYOnScreen(view);
                        if (HwDialogStubImpl.this.mLastY == 0) {
                            HwDialogStubImpl.this.mLastY = locationY;
                            return;
                        }
                        int deltLimit = getScreenHeight() / HwDialogStubImpl.PERCENT;
                        boolean ignoreChange = Math.abs(HwDialogStubImpl.this.mLastY - locationY) < deltLimit ? true : HwDialogStubImpl.DB;
                        boolean isChangeY = (HwDialogStubImpl.this.mLastY == 0 || HwDialogStubImpl.this.mLastY - locationY <= deltLimit) ? HwDialogStubImpl.DB : true;
                        if (!ignoreChange) {
                            HwDialogStubImpl hwDialogStubImpl = HwDialogStubImpl.this;
                            if (HwDialogStubImpl.this.mIsEverReset) {
                                isChangeY = true;
                            }
                            hwDialogStubImpl.mIsInputMethodOn = isChangeY;
                        }
                        HwDialogStubImpl.this.mIsEverReset = HwDialogStubImpl.DB;
                        if (!HwDialogStubImpl.this.mIsInputMethodOn || HwDialogStubImpl.this.mState != HwDialogStubImpl.STATE_DOWN) {
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
                this.screenHeight = res.getDisplayMetrics().heightPixels - res.getDimensionPixelSize(17104919);
                return this.screenHeight;
            }
        };
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
            View view = this.mWindow.getDecorView();
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
                    this.mSensorManager.setSensorListener(new AnonymousClass4(view));
                } else {
                    Log.w(TAG, "showDialogFactory sensor register fail, isRegister = " + isRegister);
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void dismissDialogFactory() {
        if (this.mSensorManager != null && this.mWindow != null && this.mContext != null && this.mWindow.getAttributes().type != 2011) {
            removeInputMethodListener(this.mWindow.getDecorView());
            if (this.mAnimator != null && this.mAnimator.isRunning()) {
                this.mAnimator.cancel();
                this.mAnimator.removeAllUpdateListeners();
            }
            this.mAnimator = null;
            this.mSensorManager.setMoveDirection(INIT);
            this.mSensorManager.unRegisterListeners();
            this.mSensorManager.setSensorListener(null);
        }
    }

    private void initAnimatorParams(WindowManager windowManager, View view) {
        Interpolator interpolator = AnimationUtils.loadInterpolator(this.mContext, 34209799);
        PropertyValuesHolder[] propertyValuesHolderArr = new PropertyValuesHolder[STATE_DOWN];
        propertyValuesHolderArr[STATE_TOP] = PropertyValuesHolder.ofFloat("y", new float[]{0.0f, 0.0f});
        this.mAnimator = ValueAnimator.ofPropertyValuesHolder(propertyValuesHolderArr);
        this.mAnimator.setInterpolator(interpolator);
        this.mAnimator.setDuration(400);
        this.mAnimator.addUpdateListener(new AnonymousClass5(view, windowManager));
        this.mAnimator.addListener(new AnonymousClass6(view));
    }

    private boolean isShouldChange(int state, int direction) {
        if (state == 0 && direction == DIALOG_AIMATE_TO_BOTTOM) {
            return true;
        }
        if (state == STATE_DOWN && direction == DIALOG_AIMATE_TO_TOP) {
            return true;
        }
        return DB;
    }

    private void startAnim() {
        if (this.mAnimator != null) {
            this.mAnimator.start();
        }
    }

    private void setState(View view, int direction) {
        if (this.mContext != null) {
            int height;
            Resources res = this.mContext.getResources();
            int displayHeight = res.getDisplayMetrics().heightPixels;
            int statusBarHeight = res.getDimensionPixelSize(17104919);
            if (view != null) {
                height = view.getHeight();
            } else {
                height = STATE_TOP;
            }
            int offset = (((displayHeight - statusBarHeight) - height) / 2) - ((int) ((res.getDisplayMetrics().density * 36.0f) + HwFragmentMenuItemView.ALPHA_PRESSED));
            if (offset < 0) {
                offset = STATE_TOP;
            }
            this.mOffset = offset;
            switch (direction) {
                case DIALOG_AIMATE_TO_TOP /*3*/:
                    this.mState = STATE_TOP;
                    break;
                case DIALOG_AIMATE_TO_BOTTOM /*4*/:
                    this.mState = STATE_DOWN;
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
            for (int i = STATE_TOP; i < count; i += STATE_DOWN) {
                if (isInputMethodTarget(group.getChildAt(i))) {
                    return true;
                }
            }
            return DB;
        } else if (view instanceof TextView) {
            return ((TextView) view).isInputMethodTarget();
        } else {
            return DB;
        }
    }

    private int getLocationYOnScreen(View view) {
        if (view == null) {
            Log.w(TAG, "getLocationYOnScreen view is null");
            return STATE_TOP;
        }
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return location[STATE_DOWN];
    }

    private void resetPositionForInputMethodOn() {
        if (this.mWindow != null) {
            LayoutParams l = this.mWindow.getAttributes();
            View view = this.mWindow.getDecorView();
            l.y = STATE_TOP;
            this.mWindow.getWindowManager().updateViewLayout(view, l);
            this.mState = STATE_TOP;
            this.mIsEverReset = true;
        }
    }

    public boolean hasButtons() {
        if (hasButton(INIT) || hasButton(-2)) {
            return true;
        }
        return hasButton(-3);
    }

    private boolean hasButton(int whichButton) {
        boolean z = DB;
        if (this.mDialog == null || !(this.mDialog instanceof AlertDialog)) {
            return DB;
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
