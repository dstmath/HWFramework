package com.huawei.server.magicwin;

import android.content.Context;
import android.graphics.Rect;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import com.android.server.wm.ActivityRecordEx;
import com.android.server.wm.HwMagicContainer;
import com.android.server.wm.HwMagicWinAmsPolicy;
import com.android.server.wm.HwMagicWinManager;
import com.android.server.wm.WindowManagerServiceEx;
import com.huawei.android.util.SlogEx;
import com.huawei.android.view.DisplayInfoEx;
import com.huawei.server.magicwin.HwMagicWinAnimationScene;
import com.huawei.server.utils.SharedParameters;
import com.huawei.server.utils.Utils;
import com.huawei.utils.HwPartResourceUtils;
import java.util.Arrays;

public class HwMagicWinAnimation {
    private static final int ALPHA_OFFSET = 0;
    private static final int ANIMATION_SIZE = 2;
    private static final float CENTER = 0.5f;
    private static final float CENTER_AXIS_X = 0.5f;
    private static final float CENTER_SCALE_X = 0.75f;
    private static final float DEFAULT_AXIS_Y = 0.5f;
    private static final float DEFAULT_MIN = 0.01f;
    private static final float DEFAULT_SCALE = 1.0f;
    private static final int DETLA_DIFFERENCE = 2;
    private static final int DIRECTION_COMPARISON = 0;
    private static final int DURATION_ALPHA_CROSS_FINISH = 150;
    private static final int DURATION_ALPHA_ENTER = 350;
    private static final int DURATION_ALPHA_MAGIC_EXIT_ANAN = 50;
    private static final int DURATION_OFFSET_MAGIC_EXIT_ANAN = 450;
    private static final int DURATION_ROTATION_ANIMATION = 350;
    private static final int DURATION_SLAVE_ENTER_A1AN = 350;
    private static final int DURATION_SLAVE_ENTER_ANAN = 150;
    private static final int DURATION_SLAVE_EXIT = 350;
    private static final int DURATION_TRANSLATION_CROSS_FINISH = 350;
    private static final int DURATION_TRANSLATION_ENTER = 350;
    private static final int ENTER_INDEX = 0;
    private static final float EXIT_FROM_SCALE = 1.0f;
    private static final int EXIT_INDEX = 1;
    private static final float EXIT_TO_SCALE = 0.8f;
    private static final float FROM_ALPHA = 1.0f;
    private static final float HALF_FACTOR = 2.0f;
    public static final float INVALID_THRESHOLD = 0.0f;
    private static final float LAUCHER_EXIT_SCALE = 0.9f;
    private static final float LEFT_AXIS_X = 0.25f;
    private static final float MAX_ALPHA = 1.0f;
    private static final float MAX_SCALE = 1.0f;
    private static final float MIN_ALPHA = 0.0f;
    private static final float MIN_ALPHA_EXIT = 0.85f;
    private static final float MIN_SCALE_ENTER = 0.85f;
    private static final int NUM_ANIMATIONSET = 2;
    private static final int PARAM_INDEX_ONE = 1;
    private static final int PARAM_INDEX_THREE = 3;
    private static final int PARAM_INDEX_TWO = 2;
    private static final int PARAM_INDEX_ZERO = 0;
    private static final float PIVOT_COMPENSATION = 0.5f;
    private static final float RIGHT_AXIS_X = 0.75f;
    private static final float ROTATE_DEGREES_ORIGIN = 0.0f;
    private static final float ROTATE_DEGREES_UNIT = 90.0f;
    private static final int ROTATE_FACTORS_ASPECT_RATIO = 2;
    private static final int ROTATE_FACTORS_CLOCKWISE = 4;
    private static final int ROTATION_FOCUS_CENTER = 0;
    private static final int ROTATION_FOCUS_INVALID = -999;
    private static final int ROTATION_FOCUS_MASTER = -1;
    private static final int ROTATION_FOCUS_SLAVE = 1;
    private static final float SCALE_HALF = 0.5f;
    private static final float SPLITSCREEN_ALPHA_FRACTION = 0.9f;
    private static final int SPLITSCREEN_ANIMATION_DURATION = 1000;
    private static final float SPLITSCREEN_FRACTION = 0.55f;
    private static final String TAG = "HWMW_HwMagicWinAnimation";
    private static final float TO_ALPHA = 0.0f;
    private static final int TRANSIT_TASK_CHANGE_WINDOWING_MODE = 27;
    private static final int WALLPAPER_CLOSE_ANIMATION_DURATION = 200;
    private static final float WALLPAPER_CLOSE_ANIMATION_FRACTION = 0.5f;
    private static final float ZERO_POINT = 0.0f;
    private boolean isAnimationRunning = false;
    private Animation[] mActivityFinishAnimations = new Animation[2];
    private Animation[] mActivityStartAnimations = new Animation[2];
    private Interpolator mAlphaInterpolator;
    private HwMagicContainer mContainer;
    private Context mContext;
    private Interpolator mCrossfinishMoveInterpolator;
    private Interpolator mCubicBezierInterpolator;
    private int mDurationAlphaEnter = 350;
    private int mDurationRotation = 350;
    private int mDurationSlaveEnterA1An = 350;
    private int mDurationSlaveExit = 350;
    private int mDurationTranslationEnter = 350;
    private Interpolator mFastOutSlowInterpolator;
    private int mFocus;
    private Rect mFocusBounds;
    private int mFocusMode;
    private Interpolator mMagicWinMoveInterpolator;
    private HwMagicWinManager mMwManager;
    private Interpolator mSplitAnimInterpolator;
    private int mTopActivityWidth;
    private WindowManagerServiceEx mWms;

    public HwMagicWinAnimation(SharedParameters parameters, HwMagicContainer container) {
        this.mContext = parameters.getContext();
        this.mMwManager = parameters.getMwWinManager();
        this.mWms = parameters.getWms();
        this.mContainer = container;
        this.mFastOutSlowInterpolator = AnimationUtils.loadInterpolator(this.mContext, 17563661);
        this.mAlphaInterpolator = new Interpolator() {
            /* class com.huawei.server.magicwin.HwMagicWinAnimation.AnonymousClass1 */

            @Override // android.animation.TimeInterpolator
            public float getInterpolation(float input) {
                if (input < 0.9f) {
                    return HwMagicWinAnimation.INVALID_THRESHOLD;
                }
                return HwMagicWinAnimation.this.mFastOutSlowInterpolator.getInterpolation((input - 0.9f) / 0.100000024f);
            }
        };
        this.mSplitAnimInterpolator = new Interpolator() {
            /* class com.huawei.server.magicwin.HwMagicWinAnimation.AnonymousClass2 */

            @Override // android.animation.TimeInterpolator
            public float getInterpolation(float input) {
                if (input >= HwMagicWinAnimation.SPLITSCREEN_FRACTION) {
                    return 1.0f;
                }
                return HwMagicWinAnimation.this.mFastOutSlowInterpolator.getInterpolation(input / HwMagicWinAnimation.SPLITSCREEN_FRACTION);
            }
        };
        this.mCubicBezierInterpolator = AnimationUtils.loadInterpolator(this.mContext, HwPartResourceUtils.getResourceId("cubic_bezier_interpolator_type_33_33"));
        this.mMagicWinMoveInterpolator = new HwMagicWinMoveInterpolator(this);
        this.mCrossfinishMoveInterpolator = new HwCrossfinishMoveInterpolator(this);
        this.mWms.setMagicWindowMoveInterpolator(this.mMagicWinMoveInterpolator);
    }

    public void setAnimationNull() {
        setMagicWindowAnimation(true, null, null);
        setMagicWindowAnimation(false, null, null);
    }

    public void overrideStartActivityAnimation(HwMagicWinAnimationScene.AnimationScene animationScene) {
        SlogEx.d(TAG, "overrideStartActivityAnimation, params is " + animationScene);
        overrideTranslationAnimations(true, animationScene.getAnimationScene());
        this.mWms.setMagicWindowMoveInterpolator(this.mMagicWinMoveInterpolator);
    }

    public void overrideFinishActivityAnimation(HwMagicWinAnimationScene.AnimationScene animationScene) {
        SlogEx.d(TAG, "overrideFinishActivityAnimation, params is " + animationScene);
        int finishScene = animationScene.getAnimationScene();
        boolean isMastersFinish = false;
        overrideTranslationAnimations(false, finishScene);
        if (finishScene == 105 || finishScene == 106) {
            isMastersFinish = true;
        }
        this.mWms.setMagicWindowMoveInterpolator(isMastersFinish ? this.mCrossfinishMoveInterpolator : this.mMagicWinMoveInterpolator);
    }

    private void overrideTranslationAnimations(boolean isStart, int scene) {
        SlogEx.d(TAG, "overrideTranslationAnimations, scene = " + scene + "isStart" + isStart);
        float animationScale = getTransitionAnimationScale();
        this.mDurationTranslationEnter = (int) (350.0f / animationScale);
        this.mDurationAlphaEnter = (int) (350.0f / animationScale);
        this.mDurationSlaveExit = (int) (350.0f / animationScale);
        this.mDurationSlaveEnterA1An = (int) (350.0f / animationScale);
        AnimationSet[] animationSets = preSetAnimationByScene(scene);
        if (animationSets.length <= 0) {
            SlogEx.d(TAG, "not need preSet Animation");
            return;
        }
        AnimationSet enterSet = animationSets[0];
        AnimationSet exitSet = animationSets[1];
        if (scene != 106) {
            if ((isStart ? enterSet : exitSet) != null) {
                (isStart ? enterSet : exitSet).setAnimationListener(new MoveAnimationListener(scene));
            }
        }
        setMagicWindowAnimation(isStart, enterSet, exitSet);
    }

    private AnimationSet[] preSetAnimationByScene(int scene) {
        AnimationSet[] animationSets = new AnimationSet[2];
        AnimationSet enterSet = new AnimationSet(false);
        AnimationSet exitSet = new AnimationSet(false);
        if (scene == -1) {
            return animationSets;
        }
        if (scene == 0) {
            return setMasterToSlaveAnimation(animationSets, enterSet, exitSet);
        }
        if (scene == 1) {
            return setSlaveToSlaveAnimation(animationSets, enterSet, exitSet);
        }
        if (scene == 2) {
            return setMiddleToSlaveAnimation(animationSets, enterSet, exitSet);
        }
        if (scene == 200) {
            return setAnAnSlaveToMasterAnimation(animationSets, enterSet, exitSet);
        }
        switch (scene) {
            case 100:
                return setExitAnimation(animationSets, enterSet, exitSet);
            case HwMagicWinAnimationScene.SCENE_MIDDLE /* 101 */:
                return setMiddleAnimation(animationSets, enterSet, exitSet);
            case HwMagicWinAnimationScene.SCENE_START_APP /* 102 */:
                return setOpenAppAnimation(animationSets, enterSet, exitSet);
            case HwMagicWinAnimationScene.SCENE_EXIT_SLAVE_TO_SLAVE /* 103 */:
                return setExitSlaveToSlaveAnimation(animationSets, enterSet, exitSet);
            case HwMagicWinAnimationScene.SCENE_ANAN_MASTER_TO_SLAVE /* 104 */:
                return setAnAnMasterToSlaveAnimation(animationSets, enterSet, exitSet);
            case HwMagicWinAnimationScene.SCENE_EXIT_MASTER_TO_SLAVE /* 105 */:
                return setExitMasterToSlaveAnimation(animationSets, enterSet, exitSet);
            case HwMagicWinAnimationScene.SCENE_EXIT_BY_MAGIC_WINDOW /* 106 */:
                return setMagicExitAnimation(animationSets, enterSet, exitSet);
            default:
                return new AnimationSet[0];
        }
    }

    private AnimationSet[] setOpenAppAnimation(AnimationSet[] animationSets, AnimationSet enterSet, AnimationSet exitSet) {
        animationSets[0] = null;
        animationSets[1] = getLauncherExitAnimation();
        return animationSets;
    }

    private void setMagicWindowAnimation(boolean isStart, Animation enter, Animation exit) {
        if (isStart) {
            Animation[] animationArr = this.mActivityStartAnimations;
            animationArr[0] = enter;
            animationArr[1] = exit;
            return;
        }
        Animation[] animationArr2 = this.mActivityFinishAnimations;
        animationArr2[0] = enter;
        animationArr2[1] = exit;
    }

    public void setOpenAppAnimation() {
        setMagicWindowAnimation(true, null, getLauncherExitAnimation());
    }

    private AnimationSet getLauncherExitAnimation() {
        AnimationSet as = new AnimationSet(true);
        ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 0.9f, 1.0f, 0.9f, 1, 0.5f, 1, 0.5f);
        as.addAnimation(new AlphaAnimation(1.0f, (float) INVALID_THRESHOLD));
        as.addAnimation(scaleAnimation);
        as.setDuration((long) this.mDurationAlphaEnter);
        as.setInterpolator(this.mFastOutSlowInterpolator);
        return as;
    }

    private AnimationSet[] setExitSlaveToSlaveAnimation(AnimationSet[] animationSets, AnimationSet enterSet, AnimationSet exitSet) {
        animationSets[0] = (AnimationSet) AnimationUtils.loadAnimation(this.mContext, HwPartResourceUtils.getResourceId("activity_close_enter"));
        animationSets[1] = (AnimationSet) AnimationUtils.loadAnimation(this.mContext, HwPartResourceUtils.getResourceId("activity_close_exit"));
        return animationSets;
    }

    private AnimationSet[] setExitMasterToSlaveAnimation(AnimationSet[] animationSets, AnimationSet enterSet, AnimationSet exitSet) {
        Rect masterRect = this.mContainer.getBounds(1, false);
        Rect slaveRect = this.mContainer.getBounds(2, false);
        TranslateAnimation translateExit = new TranslateAnimation((float) masterRect.left, (float) slaveRect.left, (float) masterRect.top, (float) slaveRect.top);
        translateExit.setInterpolator(this.mFastOutSlowInterpolator);
        translateExit.setDuration(350);
        AlphaAnimation alphaExit = new AlphaAnimation(1.0f, (float) INVALID_THRESHOLD);
        alphaExit.setInterpolator(this.mCubicBezierInterpolator);
        alphaExit.setDuration(150);
        alphaExit.setStartOffset(350);
        exitSet.addAnimation(translateExit);
        exitSet.addAnimation(alphaExit);
        AlphaAnimation maskingAnimation = new AlphaAnimation(1.0f, 0.85f);
        maskingAnimation.setInterpolator(this.mCubicBezierInterpolator);
        maskingAnimation.setDuration(150);
        maskingAnimation.setRepeatMode(2);
        maskingAnimation.setRepeatCount(1);
        enterSet.addAnimation(maskingAnimation);
        animationSets[0] = enterSet;
        animationSets[1] = exitSet;
        return animationSets;
    }

    private AnimationSet[] setMasterToSlaveAnimation(AnimationSet[] animationSets, AnimationSet enterSet, AnimationSet exitSet) {
        animationSets[1] = null;
        animationSets[0] = null;
        return animationSets;
    }

    private AnimationSet[] setAnAnMasterToSlaveAnimation(AnimationSet[] animationSets, AnimationSet enterSet, AnimationSet exitSet) {
        enterSet.setDuration(150);
        exitSet.setDuration(150);
        enterSet.addAnimation(getAlpahAnimation(INVALID_THRESHOLD, 1.0f, this.mFastOutSlowInterpolator));
        exitSet.addAnimation(getAlpahAnimation(1.0f, INVALID_THRESHOLD, this.mFastOutSlowInterpolator));
        animationSets[0] = enterSet;
        animationSets[1] = exitSet;
        return animationSets;
    }

    private AnimationSet[] setMiddleToSlaveAnimation(AnimationSet[] animationSets, AnimationSet enterSet, AnimationSet exitSet) {
        enterSet.addAnimation(getScaleAnimation(0.75f, 0.85f, 1.0f));
        enterSet.addAnimation(getAlphaAnimationEnter());
        exitSet.addAnimation(getAlphaAnimationExit());
        exitSet.addAnimation(getScaleAnimation(0.75f, 1.0f, 0.85f));
        animationSets[0] = enterSet;
        animationSets[1] = exitSet;
        enterSet.setInterpolator(this.mFastOutSlowInterpolator);
        exitSet.setInterpolator(this.mFastOutSlowInterpolator);
        return animationSets;
    }

    private AnimationSet[] setSlaveToSlaveAnimation(AnimationSet[] animationSets, AnimationSet enterSet, AnimationSet exitSet) {
        animationSets[0] = (AnimationSet) AnimationUtils.loadAnimation(this.mContext, HwPartResourceUtils.getResourceId("activity_open_enter"));
        animationSets[1] = (AnimationSet) AnimationUtils.loadAnimation(this.mContext, HwPartResourceUtils.getResourceId("activity_open_exit"));
        return animationSets;
    }

    private AnimationSet[] setExitAnimation(AnimationSet[] animationSets, AnimationSet enterSet, AnimationSet exitSet) {
        exitSet.addAnimation(getAlphaAnimationExit());
        exitSet.addAnimation(getScaleAnimation(0.5f, 1.0f, 0.85f));
        exitSet.setInterpolator(this.mFastOutSlowInterpolator);
        animationSets[0] = enterSet;
        animationSets[1] = exitSet;
        return animationSets;
    }

    private AnimationSet[] setMagicExitAnimation(AnimationSet[] animationSets, AnimationSet enterSet, AnimationSet exitSet) {
        AlphaAnimation maskingAnimation = new AlphaAnimation(1.0f, 0.85f);
        maskingAnimation.setInterpolator(this.mCubicBezierInterpolator);
        maskingAnimation.setDuration(150);
        maskingAnimation.setStartOffset(150);
        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, (float) INVALID_THRESHOLD);
        alphaAnimation.setInterpolator(this.mCubicBezierInterpolator);
        alphaAnimation.setDuration(50);
        alphaAnimation.setStartOffset(450);
        exitSet.addAnimation(alphaAnimation);
        exitSet.addAnimation(maskingAnimation);
        animationSets[0] = enterSet;
        animationSets[1] = exitSet;
        return animationSets;
    }

    private AnimationSet[] setAnAnSlaveToMasterAnimation(AnimationSet[] animationSets, AnimationSet enterSet, AnimationSet exitSet) {
        enterSet.addAnimation(getAlphaAnimationEnter());
        ScaleAnimation scaleEnter = new ScaleAnimation(0.85f, 1.0f, 0.85f, 1.0f, 2, 0.75f, 2, 0.5f);
        scaleEnter.setDuration((long) this.mDurationTranslationEnter);
        enterSet.addAnimation(scaleEnter);
        enterSet.setInterpolator(this.mFastOutSlowInterpolator);
        animationSets[0] = enterSet;
        exitSet.addAnimation(getAlphaAnimationExit());
        animationSets[1] = exitSet;
        return animationSets;
    }

    private AnimationSet[] setMiddleAnimation(AnimationSet[] animationSets, AnimationSet enterSet, AnimationSet exitSet) {
        enterSet.addAnimation(getAlphaAnimationEnter());
        enterSet.addAnimation(getScaleAnimation(0.5f, 0.85f, 1.0f));
        animationSets[0] = enterSet;
        exitSet.addAnimation(getAlphaAnimationExit());
        exitSet.addAnimation(getScaleAnimation(0.5f, 1.0f, 0.85f));
        animationSets[1] = exitSet;
        return animationSets;
    }

    private AlphaAnimation getAlphaAnimationEnter() {
        AlphaAnimation alphaEnter = new AlphaAnimation((float) INVALID_THRESHOLD, 1.0f);
        alphaEnter.setDuration((long) this.mDurationAlphaEnter);
        alphaEnter.setStartOffset(0);
        return alphaEnter;
    }

    private AlphaAnimation getAlphaAnimationExit() {
        AlphaAnimation alphaExit = new AlphaAnimation(1.0f, (float) INVALID_THRESHOLD);
        alphaExit.setDuration((long) this.mDurationSlaveExit);
        return alphaExit;
    }

    private ScaleAnimation getScaleAnimation(float centerX, float from, float to) {
        ScaleAnimation scaleEnter = new ScaleAnimation(from, to, from, to, 2, centerX, 2, 0.5f);
        scaleEnter.setDuration((long) this.mDurationTranslationEnter);
        return scaleEnter;
    }

    public static float[] computePivotForAppExit(Rect vRect, int iconWidth, int iconHeight, float[] centerCoordinate) {
        float iconFrameLeft = centerCoordinate[0] - (((float) iconWidth) / HALF_FACTOR);
        float iconFrameRight = centerCoordinate[0] + (((float) iconWidth) / HALF_FACTOR);
        float iconFrameTop = centerCoordinate[1] - (((float) iconHeight) / HALF_FACTOR);
        float iconFrameBottom = centerCoordinate[1] + (((float) iconHeight) / HALF_FACTOR);
        return new float[]{(((((float) vRect.right) * iconFrameLeft) - (((float) vRect.left) * iconFrameRight)) / (((((float) vRect.right) - iconFrameRight) + iconFrameLeft) - ((float) vRect.left))) + 0.5f, (((((float) vRect.bottom) * iconFrameTop) - (((float) vRect.top) * iconFrameBottom)) / (((((float) vRect.bottom) - iconFrameBottom) + iconFrameTop) - ((float) vRect.top))) + 0.5f};
    }

    public static float[] computeScaleToForAppExit(Rect vRect, int iconWidth, int iconHeight) {
        return new float[]{((float) iconWidth) / ((float) (vRect.right - vRect.left)), ((float) iconHeight) / ((float) (vRect.bottom - vRect.top))};
    }

    public void setParamsForRotation(Rect focusBounds, int focusMode) {
        this.mFocusBounds = focusBounds;
        this.mFocusMode = focusMode;
        this.mFocus = getFocusForRotation();
    }

    public void resetParamsForRotation() {
        this.mFocusBounds = null;
        this.mFocus = ROTATION_FOCUS_INVALID;
    }

    public boolean getRotationAnim(Integer[] params, Animation[] results) {
        if (this.mFocus == ROTATION_FOCUS_INVALID || this.mFocusBounds == null || this.mFocusMode != 103) {
            SlogEx.e(TAG, "get rotation animation focus not right!");
            return false;
        }
        int oldRotation = params[0].intValue();
        int delta = params[1].intValue();
        int oldHeight = params[2].intValue();
        int newHeight = params[3].intValue();
        if (this.mTopActivityWidth == 0 || oldHeight == 0 || newHeight == 0) {
            SlogEx.e(TAG, "getRotationAnimation failed ! window size is error !");
            return false;
        }
        int direction = delta - 2;
        float rotate = (((float) oldHeight) * HALF_FACTOR) / ((float) newHeight);
        if (oldRotation == 0 || oldRotation == 2) {
            float horizontalScale = ((float) this.mTopActivityWidth) / ((float) newHeight);
            results[0] = getRotateAniamtionSet(false, direction, horizontalScale, rotate);
            results[1] = getRotateAniamtionSet(true, direction, 1.0f / horizontalScale, rotate);
            return true;
        } else if (oldRotation == 1 || oldRotation == 3) {
            float verticalScale = ((float) oldHeight) / ((float) this.mTopActivityWidth);
            results[0] = getRotateAniamtionSet(false, direction, verticalScale, rotate);
            results[1] = getRotateAniamtionSet(true, direction, 1.0f / verticalScale, rotate);
            return true;
        } else {
            SlogEx.e(TAG, "getRotationAnimation failed ! oldRotation wrong !");
            return false;
        }
    }

    private AnimationSet getRotateAniamtionSet(boolean isEnter, int direction, float scaleValue, float rotateValue) {
        float scalePivotX = 0.5f;
        float scalePivotY = 0.5f;
        float rotatePivotX = 0.5f;
        float rotatePivotY = 0.5f;
        float[] params = getRotateAnimationParams(isEnter, direction, rotateValue);
        if (params.length == 4) {
            scalePivotX = params[0];
            scalePivotY = params[1];
            rotatePivotX = params[2];
            rotatePivotY = params[3];
        }
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.setInterpolator(this.mFastOutSlowInterpolator);
        float scaleFrom = isEnter ? scaleValue : 1.0f;
        float scaleTo = isEnter ? 1.0f : scaleValue;
        setDurationAndAddAnimation(animationSet, new ScaleAnimation(scaleFrom, scaleTo, scaleFrom, scaleTo, 1, scalePivotX, 1, scalePivotY));
        float toAlpha = INVALID_THRESHOLD;
        setDurationAndAddAnimation(animationSet, new RotateAnimation(isEnter ? ((float) (-direction)) * ROTATE_DEGREES_UNIT : 0.0f, isEnter ? 0.0f : ((float) direction) * ROTATE_DEGREES_UNIT, 1, rotatePivotX, 1, rotatePivotY));
        float fromAlpha = isEnter ? 0.0f : 1.0f;
        if (isEnter) {
            toAlpha = 1.0f;
        }
        setDurationAndAddAnimation(animationSet, new AlphaAnimation(fromAlpha, toAlpha));
        return animationSet;
    }

    private float[] getRotateAnimationParams(boolean isEnter, int direction, float rotateValue) {
        float[] rotateAnimationParams = new float[4];
        Arrays.fill(rotateAnimationParams, 0.5f);
        if (rotateValue == INVALID_THRESHOLD) {
            return rotateAnimationParams;
        }
        float f = 1.0f;
        if (this.mFocus == 1) {
            rotateAnimationParams[0] = isEnter ? 1.0f : 0.0f;
            rotateAnimationParams[1] = 0.0f;
            rotateAnimationParams[2] = 0.5f;
            rotateAnimationParams[3] = rotateValue / 4.0f;
            if (direction < 0) {
                rotateAnimationParams[0] = 1.0f;
                rotateAnimationParams[1] = isEnter ? 0.0f : 1.0f;
                rotateAnimationParams[2] = 1.0f - (1.0f / rotateValue);
                rotateAnimationParams[3] = 0.5f;
            }
        }
        if (this.mFocus == -1) {
            rotateAnimationParams[0] = 0.0f;
            rotateAnimationParams[1] = isEnter ? 0.0f : 1.0f;
            rotateAnimationParams[2] = 1.0f / rotateValue;
            rotateAnimationParams[3] = 0.5f;
            if (direction < 0) {
                if (isEnter) {
                    f = 0.0f;
                }
                rotateAnimationParams[0] = f;
                rotateAnimationParams[1] = 0.0f;
                rotateAnimationParams[2] = 0.5f;
                rotateAnimationParams[3] = rotateValue / 4.0f;
            }
        }
        return rotateAnimationParams;
    }

    private int getFocusForRotation() {
        Rect rect = this.mFocusBounds;
        if (rect != null) {
            this.mTopActivityWidth = rect.width();
        }
        if (this.mContainer.checkPosition(this.mFocusBounds, 2)) {
            return this.mContainer.getConfig().isRtl() ? -1 : 1;
        }
        if (this.mContainer.checkPosition(this.mFocusBounds, 1)) {
            return this.mContainer.getConfig().isRtl() ? -1 : 1;
        }
        if (this.mContainer.checkPosition(this.mFocusBounds, 3)) {
            return 0;
        }
        return ROTATION_FOCUS_INVALID;
    }

    private void setDurationAndAddAnimation(AnimationSet animationSet, Animation animation) {
        this.mDurationRotation = (int) (350.0f / getTransitionAnimationScale());
        animation.setDuration((long) this.mDurationRotation);
        animationSet.addAnimation(animation);
    }

    private float getTransitionAnimationScale() {
        return Math.abs(this.mWms.getTransitionAnimationScaleLocked() - 0.5f) < DEFAULT_MIN ? 0.5f : 1.0f;
    }

    private Animation getAlpahAnimation(float fromAlpha, float toAlpha, Interpolator interpolator) {
        Animation alphaAnimation = new AlphaAnimation(fromAlpha, toAlpha);
        alphaAnimation.setInterpolator(interpolator);
        return alphaAnimation;
    }

    private Animation getTranslateAnimation(float fromXDelta, float toXDelta, float fromYDelta, float toYDelta) {
        return new TranslateAnimation(fromXDelta, toXDelta, fromYDelta, toYDelta);
    }

    private Animation getScaleAnimation(int fromWidth, float toWidth, int fromHeight, float toHeight) {
        float startXScale = (((float) fromWidth) + 0.5f) / toWidth;
        float endXScale = 1.0f;
        float startYScale = (((float) fromHeight) + 0.5f) / toHeight;
        float endYScale = 1.0f;
        if (((float) fromWidth) < toWidth) {
            startXScale = 1.0f;
            endXScale = (toWidth + 0.5f) / ((float) fromWidth);
        }
        if (((float) fromHeight) < toHeight) {
            startYScale = 1.0f;
            endYScale = (0.5f + toHeight) / ((float) fromHeight);
        }
        return new ScaleAnimation(startXScale, endXScale, startYScale, endYScale);
    }

    public AnimationParams getSplitAnimation(Rect startBounds, Rect endBounds, DisplayInfoEx displayInfo) {
        AnimationSet animationSet = new AnimationSet(false);
        animationSet.addAnimation(getAlpahAnimation(1.0f, INVALID_THRESHOLD, this.mAlphaInterpolator));
        AnimationSet combineSet = new AnimationSet(true);
        combineSet.addAnimation(getScaleAnimation(startBounds.width(), (float) endBounds.width(), startBounds.height(), (float) endBounds.height()));
        combineSet.addAnimation(getTranslateAnimation((float) startBounds.left, (float) endBounds.left, (float) startBounds.top, (float) endBounds.top));
        combineSet.setInterpolator(this.mSplitAnimInterpolator);
        animationSet.addAnimation(combineSet);
        animationSet.setDuration(1000);
        animationSet.setFillAfter(true);
        animationSet.initialize(startBounds.width(), startBounds.height(), displayInfo.getLogicalWidth(), displayInfo.getLogicalHeight());
        return new AnimationParams(animationSet, SPLITSCREEN_FRACTION);
    }

    public AnimationParams getExitTaskAnimation(Rect startBounds, DisplayInfoEx displayInfo) {
        AnimationSet animationSet = new AnimationSet(true);
        Animation alphaAnimation = new AlphaAnimation(1.0f, (float) INVALID_THRESHOLD);
        Animation scaleAnimation = new ScaleAnimation(1.0f, EXIT_TO_SCALE, 1.0f, EXIT_TO_SCALE);
        animationSet.addAnimation(alphaAnimation);
        animationSet.addAnimation(scaleAnimation);
        animationSet.setInterpolator(this.mFastOutSlowInterpolator);
        animationSet.setDuration(550);
        animationSet.setFillAfter(true);
        animationSet.initialize(startBounds.width(), startBounds.height(), displayInfo.getLogicalWidth(), displayInfo.getLogicalHeight());
        return new AnimationParams(animationSet, INVALID_THRESHOLD);
    }

    /* access modifiers changed from: private */
    public class MoveAnimationListener implements Animation.AnimationListener {
        private int mScene = -1;

        public MoveAnimationListener(int scene) {
            this.mScene = scene;
        }

        @Override // android.view.animation.Animation.AnimationListener
        public void onAnimationStart(Animation animation) {
            HwMagicWinAnimation.this.isAnimationRunning = true;
            ActivityRecordEx topAr = HwMagicWinAnimation.this.mMwManager.getAmsPolicy().getTopActivity(HwMagicWinAnimation.this.mContainer);
            if (topAr != null) {
                topAr.setIsAniRunningBelow(false);
            }
            if (!HwMagicWinAmsPolicy.PERMISSION_ACTIVITY.equals(Utils.getClassName(topAr))) {
                boolean isMiddle = HwMagicWinAnimation.this.mMwManager.isMiddle(topAr);
                SlogEx.i(HwMagicWinAnimation.TAG, "onAnimationStart, isMiddle =" + isMiddle);
                HwMagicWinAnimation.this.mMwManager.getUIController().changeWallpaper(isMiddle, HwMagicWinAnimation.this.mContainer.getDisplayId());
            }
            updateSplitBarVisibility(false);
        }

        @Override // android.view.animation.Animation.AnimationListener
        public void onAnimationEnd(Animation animation) {
            HwMagicWinAnimation.this.isAnimationRunning = false;
            updateSplitBarVisibility(true);
        }

        @Override // android.view.animation.Animation.AnimationListener
        public void onAnimationRepeat(Animation animation) {
        }

        private void updateSplitBarVisibility(boolean isVisible) {
            int i = this.mScene;
            if (i == 200 || i == 105 || i == 100) {
                HwMagicWinAnimation.this.mMwManager.getUIController().updateSplitBarVisibility(isVisible, false, HwMagicWinAnimation.this.mContainer.getDisplayId());
            }
        }
    }

    private class HwMagicWinMoveInterpolator implements Interpolator {
        protected static final int DURATION_MOVE_WINDOW_REAL = 350;
        protected static final float EPSILON = 1.0E-6f;
        private static final float SOURCE_INTERPOLATION = 0.0f;
        protected static final float TARGETS_INTERPOLATION = 1.0f;
        private boolean isAnimationRunning = false;
        private HwMagicWinAnimation mRightAnimation = null;
        private long mStartTime;
        private long mWaitTime;

        public HwMagicWinMoveInterpolator(HwMagicWinAnimation rightAnimation) {
            this.mRightAnimation = rightAnimation;
        }

        @Override // android.animation.TimeInterpolator
        public float getInterpolation(float input) {
            if (Math.abs(input - 0.0f) < EPSILON) {
                this.mStartTime = System.currentTimeMillis();
                this.isAnimationRunning = false;
            }
            long timeNow = System.currentTimeMillis();
            if (this.mRightAnimation.isAnimationRunning && !this.isAnimationRunning) {
                this.isAnimationRunning = true;
                this.mWaitTime = timeNow - this.mStartTime;
            }
            if (!this.isAnimationRunning && ((float) (timeNow - this.mStartTime)) > 400.0f) {
                this.isAnimationRunning = true;
                this.mWaitTime = 400;
            }
            return calculateInterpolation(this.isAnimationRunning, input, this.mWaitTime);
        }

        /* access modifiers changed from: protected */
        public float calculateInterpolation(boolean isAnimationRunning2, float input, long waitTime) {
            if (!isAnimationRunning2) {
                return 0.0f;
            }
            if (!isAnimationRunning2 || input >= ((float) (350 + waitTime)) / 750.0f) {
                return TARGETS_INTERPOLATION;
            }
            return HwMagicWinAnimation.this.mFastOutSlowInterpolator.getInterpolation(((750.0f * input) - ((float) waitTime)) / 350.0f);
        }
    }

    private class HwCrossfinishMoveInterpolator extends HwMagicWinMoveInterpolator {
        private static final float GONE_INTERPOLATION = 2.0f;

        public HwCrossfinishMoveInterpolator(HwMagicWinAnimation rightAnimation) {
            super(rightAnimation);
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.server.magicwin.HwMagicWinAnimation.HwMagicWinMoveInterpolator
        public float calculateInterpolation(boolean isAnimationRunning, float input, long waitTime) {
            if (!isAnimationRunning) {
                return GONE_INTERPOLATION;
            }
            if (!isAnimationRunning || input >= ((float) (350 + waitTime)) / 750.0f) {
                return 1.0f;
            }
            if (Math.abs(HwMagicWinAnimation.this.mFastOutSlowInterpolator.getInterpolation(((750.0f * input) - ((float) waitTime)) / 350.0f) - 1.0f) < 1.0E-6f) {
                return 1.0f;
            }
            return GONE_INTERPOLATION;
        }
    }

    public Animation getMwWallpaperCloseAnimation() {
        AlphaAnimation alphaExit = new AlphaAnimation(1.0f, (float) INVALID_THRESHOLD);
        alphaExit.setDuration(200);
        alphaExit.setInterpolator(new Interpolator() {
            /* class com.huawei.server.magicwin.HwMagicWinAnimation.AnonymousClass3 */

            @Override // android.animation.TimeInterpolator
            public float getInterpolation(float input) {
                if (input < 0.5f) {
                    return input / 0.5f;
                }
                return 1.0f;
            }
        });
        return alphaExit;
    }

    public static class AnimationParams {
        private final Animation mAnimation;
        private final float mHideThreshold;

        public AnimationParams(Animation animation, float hideThreshold) {
            this.mAnimation = animation;
            this.mHideThreshold = hideThreshold;
        }

        public Animation getAnimation() {
            return this.mAnimation;
        }

        public float getHideThreshold() {
            return this.mHideThreshold;
        }
    }

    private boolean isMagicWindowAnimation(int transit) {
        return (transit == 13 || transit == 20 || transit == 10 || transit == 0) ? false : true;
    }

    public Animation getMagicAppAnimation(Animation animation, boolean enter, int transit) {
        if (!isMagicWindowAnimation(transit)) {
            return animation;
        }
        if (transit == TRANSIT_TASK_CHANGE_WINDOWING_MODE || transit == 25) {
            return enter ? getAlphaAnimationEnter() : getAlphaAnimationExit();
        }
        if (transit == 12 && enter) {
            return animation;
        }
        if ((transit == 9 || transit == 7 || transit == 25) ? false : true) {
            Animation[] animationArr = this.mActivityStartAnimations;
            return enter ? animationArr[0] : animationArr[1];
        }
        Animation[] animationArr2 = this.mActivityFinishAnimations;
        return enter ? animationArr2[0] : animationArr2[1];
    }
}
