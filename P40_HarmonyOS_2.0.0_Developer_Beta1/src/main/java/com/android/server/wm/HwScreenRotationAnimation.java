package com.android.server.wm;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.display.HwFoldScreenState;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.PathInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import com.android.server.display.SurfaceControlExt;
import com.android.server.gesture.GestureNavConst;
import com.android.server.policy.HwPhoneWindowManager;
import com.android.server.policy.WindowManagerPolicyEx;
import com.android.server.wm.utils.HwDisplaySizeUtilEx;
import com.huawei.android.hardware.display.DisplayManagerInternalEx;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.android.util.SlogEx;
import huawei.cust.HwCfgFilePolicy;
import java.util.Locale;

/* compiled from: HwScreenRotationAnimationImpl */
class HwScreenRotationAnimation extends ScreenRotationAnimationBridgeEx {
    private static final boolean ENABLE_ROUND_CORNER_DISPLAY;
    private static final float FAST_OUT_SLOW_IN_INTER_A = 0.4f;
    private static final float FAST_OUT_SLOW_IN_INTER_B = 0.0f;
    private static final float FAST_OUT_SLOW_IN_INTER_C = 0.2f;
    private static final float FAST_OUT_SLOW_IN_INTER_D = 1.0f;
    private static final long FOLD_SCREEN_MAIN_SUB_ALPHA_ENTER_ANIM_DURATION = 200;
    private static final long FOLD_SCREEN_MAIN_SUB_ALPHA_EXIT_ANIM_DURATION = 150;
    private static final long FOLD_SCREEN_STANDARD_ALPHA_ENTER_ANIM_DURATION = 200;
    private static final long FOLD_SCREEN_STANDARD_ALPHA_EXIT_ANIM_DURATION = 200;
    private static final long FOLD_SCREEN_STANDARD_ANIM_DURATION = 250;
    private static final float MAX_ALPHA = 1.0f;
    private static final double MAX_FOLD_ALPHA_ANIM_FRACTION = 0.9999d;
    private static final float MIN_ALPHA = 0.0f;
    private static final float MIN_FOLD_ALPHA_ANIM_FRACTION = 0.0f;
    private static final float NORMAL_SCALE = 1.0f;
    private static final int SCREEN_ROTATION_ANIMATION_END = 8001;
    private static final String TAG = "HwScreenRotationAnimation";
    private static final String TAG_RCD = "RoundCornerDisplay";
    private static final long ZERO_CONST = 0;
    private Rect mDisplayRect;
    private SurfaceControlExt.TransactionEx mFoldProjectionTransactionEx;
    private Rect mFullDisplayRect;
    private HwPhoneWindowManager mHwPhoneWindowManager;
    private boolean mIsExitFoldAnimEnd = false;
    private Rect mLayerStackRect;
    private Rect mMainDisplayRect;
    private WindowManagerServiceEx mService;
    private float[] mTransFloats;

    static {
        boolean z = false;
        if (HwCfgFilePolicy.getCfgFile("display/RoundCornerDisplay/config.xml", 0) != null) {
            z = true;
        }
        ENABLE_ROUND_CORNER_DISPLAY = z;
    }

    HwScreenRotationAnimation(Context context, DisplayContentEx displayContent, boolean isForceDefaultOrientation, boolean isSecure, WindowManagerServiceEx service) {
        super(context, displayContent, isForceDefaultOrientation, isSecure, service);
        this.mService = service;
        WindowManagerPolicyEx hwPolicyEx = this.mService.getPolicyEx();
        if (hwPolicyEx != null) {
            this.mHwPhoneWindowManager = hwPolicyEx.getHwPhoneWindowManager();
        }
    }

    public void setAnimationTypeInfo(Bundle animaitonTypeInfo) {
        if (animaitonTypeInfo == null) {
            SlogEx.w(TAG, "setAnimationTypeInfo: animaitonTypeInfo is null!");
            return;
        }
        Object infoObj = animaitonTypeInfo.clone();
        if (infoObj instanceof Bundle) {
            setAospAnimationTypeInfo((Bundle) infoObj);
        } else {
            SlogEx.w(TAG, "setAnimationTypeInfo: info is not type of Bundle!");
        }
        setUsingFoldAnim(getAospAnimationTypeInfo() != null && getAospAnimationTypeInfo().getBoolean(WindowManagerServiceEx.IS_FOLD_KEY) && getAospAnimationTypeInfo().getBoolean("useCustomFoldAnim"));
        SlogEx.i(TAG, "setAnimationTypeInfo: animaitonTypeInfo = " + animaitonTypeInfo + ", mAnimationTypeInfo = " + getAospAnimationTypeInfo() + ", mUsingFoldAnim = " + isUsingFoldAnim());
    }

    /* access modifiers changed from: protected */
    public void stepAnimationToEnd(Animation animation, long nowTime) {
        if (!this.mIsExitFoldAnimEnd) {
            float fraction = getFractionByNow(animation, nowTime);
            if (((double) fraction) >= MAX_FOLD_ALPHA_ANIM_FRACTION) {
                this.mIsExitFoldAnimEnd = true;
            }
            if (this.mIsExitFoldAnimEnd) {
                SlogEx.d(TAG, "ExitFoldScreenAnimationListener endFraction = " + fraction);
                this.mService.wakeDisplayModeChange(true);
            }
        }
    }

    public Bundle getAnimationTypeInfo() {
        return getAospAnimationTypeInfo();
    }

    /* access modifiers changed from: protected */
    public Animation createScreenFoldAnimation(boolean isEnter, int fromFoldMode, int toFoldMode) {
        boolean isMainSubFold = false;
        AnimationSet animationSet = new AnimationSet(false);
        SlogEx.i(TAG, "createScreenFoldAnimation: isEnter = " + isEnter + ", fromFoldMode = " + fromFoldMode + ", toFoldMode = " + toFoldMode);
        if ((fromFoldMode == 1 && toFoldMode == 2) || (fromFoldMode == 2 && toFoldMode == 1)) {
            animationSet.addAnimation(createFullMainAnimation(isEnter, fromFoldMode, toFoldMode));
        } else {
            if ((fromFoldMode == 3 && toFoldMode == 2) || (toFoldMode == 3 && fromFoldMode == 2)) {
                isMainSubFold = true;
            }
            long exitDuration = 200;
            if (isMainSubFold) {
                exitDuration = FOLD_SCREEN_MAIN_SUB_ALPHA_EXIT_ANIM_DURATION;
            }
            long duration = isEnter ? 200 : exitDuration;
            AlphaAnimation alphaAnimation = new AlphaAnimation(isEnter ? GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO : 1.0f, isEnter ? 1.0f : GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO);
            alphaAnimation.setDuration(duration);
            alphaAnimation.setInterpolator(new PathInterpolator(0.4f, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, FAST_OUT_SLOW_IN_INTER_C, 1.0f));
            animationSet.addAnimation(alphaAnimation);
            if (isEnter) {
                animationSet.setStartOffset(exitDuration);
            }
        }
        return animationSet;
    }

    public long getExitAnimDuration() {
        if (getRotateExitAnimation() == null) {
            return 0;
        }
        return getRotateExitAnimation().getDuration();
    }

    private float getFractionByNow(Animation animation, long nowTime) {
        if (animation == null) {
            SlogEx.w(TAG, "getFractionByNow failed! animation is null");
            return GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        }
        long evaluatedTime = nowTime - (animation.getStartTime() + animation.getStartOffset());
        if (evaluatedTime <= 0) {
            evaluatedTime = 0;
        }
        long duration = animation.getDuration();
        if (duration > 0) {
            return ((float) evaluatedTime) / ((float) duration);
        }
        SlogEx.w(TAG, "getFractionByNow failed! duration little than zero");
        return GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    }

    public void kill() {
        if (HwDisplaySizeUtilEx.hasSideInScreen() && !this.mService.isInDisplayFrozen()) {
            if (isStackVisible(3)) {
                WindowStateEx topVisibleWindowState = getWindowStateEx();
                if (!(topVisibleWindowState == null || this.mHwPhoneWindowManager == null)) {
                    Log.v(TAG, "kill. call notchControlFilletForSideScreen for split primary window");
                    this.mHwPhoneWindowManager.notchControlFilletForSideScreenEx(topVisibleWindowState, true);
                }
            } else if (this.mHwPhoneWindowManager != null) {
                Log.v(TAG, "kill. call notchControlFilletForSideScreen for focused window");
                this.mHwPhoneWindowManager.notchControlFilletForSideScreenEx(getFocusedWindowStateEx(), true);
            }
        }
        HwScreenRotationAnimation.super.aospKill();
        if (ENABLE_ROUND_CORNER_DISPLAY && getDisplayId() == 0) {
            Parcel dataIn = Parcel.obtain();
            dataIn.writeInterfaceToken("android.ui.ISurfaceComposer");
            try {
                IBinder sfBinder = ServiceManagerEx.getService("SurfaceFlinger");
                if (sfBinder != null && !sfBinder.transact(SCREEN_ROTATION_ANIMATION_END, dataIn, null, 1)) {
                    Log.e(TAG_RCD, "Notify screen rotation animation end failed!");
                }
            } catch (RemoteException e) {
                Log.e(TAG_RCD, "RemoteException on notify screen rotation animation end");
            } catch (Throwable th) {
                dataIn.recycle();
                throw th;
            }
            dataIn.recycle();
        }
    }

    private Animation createFullMainAnimation(boolean isEnter, int fromFoldMode, int toFoldMode) {
        float fromYScale;
        float toYScale;
        float toXScale;
        AnimationSet animationSet = new AnimationSet(false);
        if (!isEnter) {
            AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, (float) GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO);
            alphaAnimation.setDuration(FOLD_SCREEN_STANDARD_ANIM_DURATION);
            alphaAnimation.setInterpolator(new PathInterpolator(0.4f, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, FAST_OUT_SLOW_IN_INTER_C, 1.0f));
            animationSet.addAnimation(alphaAnimation);
        }
        Rect fullFoldDisplayModeRect = HwFoldScreenState.getScreenPhysicalRect(1);
        Rect mainFoldDisplayModeRect = HwFoldScreenState.getScreenPhysicalRect(2);
        float wr = ((float) fullFoldDisplayModeRect.width()) / ((float) mainFoldDisplayModeRect.width());
        float fromXScale = 1.0f;
        float toXScale2 = 1.0f;
        if (fromFoldMode == 1 && toFoldMode == 2) {
            fromXScale = isEnter ? wr : 1.0f;
            toXScale2 = isEnter ? 1.0f : 1.0f / wr;
        } else if (fromFoldMode == 2 && toFoldMode == 1) {
            fromXScale = isEnter ? 1.0f / wr : 1.0f;
            toXScale2 = isEnter ? 1.0f : wr;
        }
        int rotation = getDisplayContentEx().getRotation();
        if (rotation == 1 || rotation == 3) {
            fromXScale = 1.0f;
            toXScale = 1.0f;
            fromYScale = fromXScale;
            toYScale = toXScale2;
        } else {
            toXScale = toXScale2;
            fromYScale = 1.0f;
            toYScale = 1.0f;
        }
        ScaleAnimation scaleAnimation = new ScaleAnimation(fromXScale, toXScale, fromYScale, toYScale);
        scaleAnimation.setDuration(FOLD_SCREEN_STANDARD_ANIM_DURATION);
        scaleAnimation.setInterpolator(new PathInterpolator(0.4f, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, FAST_OUT_SLOW_IN_INTER_C, 1.0f));
        animationSet.addAnimation(scaleAnimation);
        SlogEx.i(TAG, String.format(Locale.ENGLISH, "class : %s, method : %s, full rect : %s, main rect : %s, isEnter : %b, fromXScale : %f, toXScale : %f, fromYScale : %f, toYScale : %f", "HwScreenRotationAnimationImpl", "createScreenFoldAnimation", fullFoldDisplayModeRect, mainFoldDisplayModeRect, Boolean.valueOf(isEnter), Float.valueOf(fromXScale), Float.valueOf(toXScale), Float.valueOf(fromYScale), Float.valueOf(toYScale)));
        return animationSet;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0048  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00d0  */
    public Animation createFoldProjectionAnimation(int fromFoldMode, int toFoldMode) {
        boolean isFullToMain;
        float fromYDelta;
        float toXDelta;
        float fromXDelta;
        float toYDelta;
        if (this.mFullDisplayRect == null) {
            this.mFullDisplayRect = new Rect();
        }
        if (this.mMainDisplayRect == null) {
            this.mMainDisplayRect = new Rect();
        }
        this.mFoldProjectionTransactionEx = new SurfaceControlExt.TransactionEx();
        AnimationSet animationSet = new AnimationSet(false);
        int rotation = getDisplayContentEx().getRotation();
        if (fromFoldMode == 1) {
            if (toFoldMode == 2) {
                isFullToMain = true;
                Rect fullFoldDisplayModeRect = HwFoldScreenState.getScreenPhysicalRect(1);
                Rect mainFoldDisplayModeRect = HwFoldScreenState.getScreenPhysicalRect(2);
                if (rotation != 1) {
                    this.mFullDisplayRect.set(0, 0, fullFoldDisplayModeRect.bottom, fullFoldDisplayModeRect.right);
                    this.mMainDisplayRect.set(0, 0, mainFoldDisplayModeRect.bottom, mainFoldDisplayModeRect.width());
                    fromXDelta = (float) this.mFullDisplayRect.left;
                    toXDelta = fromXDelta;
                    fromYDelta = (float) (isFullToMain ? this.mFullDisplayRect : this.mMainDisplayRect).bottom;
                    toYDelta = (float) (isFullToMain ? this.mMainDisplayRect : this.mFullDisplayRect).bottom;
                } else if (rotation == 2) {
                    this.mFullDisplayRect.set(fullFoldDisplayModeRect);
                    this.mMainDisplayRect.set(0, 0, mainFoldDisplayModeRect.width(), mainFoldDisplayModeRect.bottom);
                    fromXDelta = (float) (isFullToMain ? this.mFullDisplayRect : this.mMainDisplayRect).right;
                    toXDelta = (float) (isFullToMain ? this.mMainDisplayRect : this.mFullDisplayRect).right;
                    fromYDelta = (float) fullFoldDisplayModeRect.top;
                    toYDelta = fromYDelta;
                } else if (rotation != 3) {
                    this.mFullDisplayRect.set(fullFoldDisplayModeRect);
                    this.mMainDisplayRect.set(mainFoldDisplayModeRect);
                    fromXDelta = (float) (isFullToMain ? this.mFullDisplayRect : this.mMainDisplayRect).left;
                    toXDelta = (float) (isFullToMain ? this.mMainDisplayRect : this.mFullDisplayRect).left;
                    fromYDelta = (float) this.mFullDisplayRect.top;
                    toYDelta = fromYDelta;
                } else {
                    this.mFullDisplayRect.set(0, 0, fullFoldDisplayModeRect.bottom, fullFoldDisplayModeRect.right);
                    this.mMainDisplayRect.set(0, mainFoldDisplayModeRect.left, mainFoldDisplayModeRect.bottom, mainFoldDisplayModeRect.right);
                    fromXDelta = (float) this.mFullDisplayRect.left;
                    toXDelta = fromXDelta;
                    fromYDelta = (float) (isFullToMain ? this.mFullDisplayRect : this.mMainDisplayRect).top;
                    toYDelta = (float) (isFullToMain ? this.mMainDisplayRect : this.mFullDisplayRect).top;
                }
                TranslateAnimation translateAnimation = new TranslateAnimation(fromXDelta, toXDelta, fromYDelta, toYDelta);
                translateAnimation.setDuration(FOLD_SCREEN_STANDARD_ANIM_DURATION);
                translateAnimation.setInterpolator(new PathInterpolator(0.4f, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, FAST_OUT_SLOW_IN_INTER_C, 1.0f));
                animationSet.addAnimation(translateAnimation);
                SlogEx.i(TAG, String.format(Locale.ENGLISH, "class : %s, method : %s, mFullDisplayRect : %s, mMainDisplayRect : %s, rotation : %d, displayMode : %s, fromXDelta : %f, toXDelta : %f, fromYDelta : %f, toYDelta : %f", TAG, "createFoldProjectionAnimation", this.mFullDisplayRect, this.mMainDisplayRect, Integer.valueOf(rotation), Integer.valueOf(this.mService.getFoldDisplayMode()), Float.valueOf(fromXDelta), Float.valueOf(toXDelta), Float.valueOf(fromYDelta), Float.valueOf(toYDelta)));
                return animationSet;
            }
        }
        isFullToMain = false;
        Rect fullFoldDisplayModeRect2 = HwFoldScreenState.getScreenPhysicalRect(1);
        Rect mainFoldDisplayModeRect2 = HwFoldScreenState.getScreenPhysicalRect(2);
        if (rotation != 1) {
        }
        TranslateAnimation translateAnimation2 = new TranslateAnimation(fromXDelta, toXDelta, fromYDelta, toYDelta);
        translateAnimation2.setDuration(FOLD_SCREEN_STANDARD_ANIM_DURATION);
        translateAnimation2.setInterpolator(new PathInterpolator(0.4f, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, FAST_OUT_SLOW_IN_INTER_C, 1.0f));
        animationSet.addAnimation(translateAnimation2);
        SlogEx.i(TAG, String.format(Locale.ENGLISH, "class : %s, method : %s, mFullDisplayRect : %s, mMainDisplayRect : %s, rotation : %d, displayMode : %s, fromXDelta : %f, toXDelta : %f, fromYDelta : %f, toYDelta : %f", TAG, "createFoldProjectionAnimation", this.mFullDisplayRect, this.mMainDisplayRect, Integer.valueOf(rotation), Integer.valueOf(this.mService.getFoldDisplayMode()), Float.valueOf(fromXDelta), Float.valueOf(toXDelta), Float.valueOf(fromYDelta), Float.valueOf(toYDelta)));
        return animationSet;
    }

    /* access modifiers changed from: protected */
    public void updateFoldProjection() {
        SurfaceControlExt.TransactionEx transactionEx;
        if (!isStarted() || isFoldProjectionAnimationNull() || (transactionEx = this.mFoldProjectionTransactionEx) == null || transactionEx.isEmpty()) {
            SlogEx.i(TAG, String.format(Locale.ENGLISH, "class : %s, method : %s, updateFoldProjection return mIsExitFoldAnimEnd : %b", TAG, "updateFoldProjection", Boolean.valueOf(this.mIsExitFoldAnimEnd)));
            return;
        }
        if (this.mDisplayRect == null) {
            this.mDisplayRect = new Rect();
        }
        if (this.mLayerStackRect == null) {
            this.mLayerStackRect = new Rect();
        }
        if (this.mIsExitFoldAnimEnd) {
            setEndProjection();
            return;
        }
        if (this.mTransFloats == null) {
            this.mTransFloats = new float[9];
        }
        getValuesFromMatrix(this.mTransFloats);
        int rotation = getDisplayContentEx().getRotation();
        handleRotation(rotation);
        int rotation2 = (rotation + 3) % 4;
        this.mFoldProjectionTransactionEx.setDisplayProjection(DisplayManagerInternalEx.getInstance().getDisplayToken(getDisplayId()), rotation2, this.mLayerStackRect, this.mDisplayRect);
        SlogEx.i(TAG, String.format(Locale.ENGLISH, "class : %s, method : %s, displayId : %d, rotation : %d, mLayerStackRect : %s, mDisplayRect : %s, mIsExitFoldAnimEnd : %b", "HwScreenRotationAnimationImpl", "updateFoldProjection", Integer.valueOf(getDisplayId()), Integer.valueOf(rotation2), this.mLayerStackRect, this.mDisplayRect, Boolean.valueOf(this.mIsExitFoldAnimEnd)));
        SurfaceControlExt.mergeToGlobalTransaction(this.mFoldProjectionTransactionEx);
    }

    private void handleRotation(int rotation) {
        if (rotation == 1) {
            Rect rect = this.mDisplayRect;
            rect.left = (int) this.mTransFloats[2];
            rect.top = this.mFullDisplayRect.top;
            this.mDisplayRect.right = this.mFullDisplayRect.right;
            this.mDisplayRect.bottom = (int) this.mTransFloats[5];
            Rect rect2 = this.mLayerStackRect;
            rect2.left = 0;
            rect2.top = 0;
            rect2.right = this.mFullDisplayRect.right;
            this.mLayerStackRect.bottom = this.mDisplayRect.bottom;
        } else if (rotation == 2) {
            this.mDisplayRect.left = this.mFullDisplayRect.left;
            Rect rect3 = this.mDisplayRect;
            float[] fArr = this.mTransFloats;
            rect3.top = (int) fArr[5];
            rect3.right = (int) fArr[2];
            rect3.bottom = this.mFullDisplayRect.bottom;
            Rect rect4 = this.mLayerStackRect;
            rect4.left = 0;
            rect4.top = 0;
            rect4.right = this.mDisplayRect.right;
            this.mLayerStackRect.bottom = this.mFullDisplayRect.bottom;
        } else if (rotation != 3) {
            Rect rect5 = this.mDisplayRect;
            float[] fArr2 = this.mTransFloats;
            rect5.left = (int) fArr2[2];
            rect5.top = (int) fArr2[5];
            rect5.right = this.mFullDisplayRect.right;
            this.mDisplayRect.bottom = this.mFullDisplayRect.bottom;
            Rect rect6 = this.mLayerStackRect;
            rect6.left = 0;
            rect6.top = 0;
            rect6.right = this.mFullDisplayRect.right - this.mDisplayRect.left;
            this.mLayerStackRect.bottom = this.mFullDisplayRect.bottom;
        } else {
            Rect rect7 = this.mDisplayRect;
            float[] fArr3 = this.mTransFloats;
            rect7.left = (int) fArr3[2];
            rect7.top = (int) fArr3[5];
            rect7.right = this.mFullDisplayRect.right;
            this.mDisplayRect.bottom = this.mFullDisplayRect.bottom;
            Rect rect8 = this.mLayerStackRect;
            rect8.left = 0;
            rect8.top = 0;
            rect8.right = this.mFullDisplayRect.right;
            this.mLayerStackRect.bottom = this.mFullDisplayRect.bottom - this.mDisplayRect.top;
        }
    }

    private void setEndProjection() {
        int displayMode = this.mService.getFoldDisplayMode();
        Rect foldDisplayModeRect = HwFoldScreenState.getScreenPhysicalRect(displayMode);
        int rotation = getDisplayContentEx().getRotation();
        if (rotation == 1) {
            this.mDisplayRect.set(0, 0, foldDisplayModeRect.bottom, foldDisplayModeRect.width());
            this.mLayerStackRect.set(this.mDisplayRect);
        } else if (rotation == 2) {
            this.mDisplayRect.set(0, 0, foldDisplayModeRect.width(), foldDisplayModeRect.bottom);
            this.mLayerStackRect.set(this.mDisplayRect);
        } else if (rotation != 3) {
            this.mDisplayRect.set(foldDisplayModeRect);
            this.mLayerStackRect.set(0, 0, this.mDisplayRect.width(), this.mDisplayRect.bottom);
        } else {
            this.mDisplayRect.set(0, foldDisplayModeRect.left, foldDisplayModeRect.bottom, foldDisplayModeRect.right);
            this.mLayerStackRect.set(0, 0, this.mDisplayRect.right, this.mDisplayRect.height());
        }
        int rotation2 = (rotation + 3) % 4;
        this.mFoldProjectionTransactionEx.setDisplayProjection(this.mService.getDisplayToken(getDisplayId()), rotation2, this.mLayerStackRect, this.mDisplayRect);
        SlogEx.i(TAG, String.format(Locale.ENGLISH, "class : %s, method : %s, display mode : %d, rect : %s, rotation : %d, mDisplayRect : %s, mLayerStackRect : %s", TAG, "setEndProjection", Integer.valueOf(displayMode), foldDisplayModeRect, Integer.valueOf(rotation2), this.mDisplayRect, this.mLayerStackRect));
        SurfaceControlExt.mergeToGlobalTransaction(this.mFoldProjectionTransactionEx);
    }
}
