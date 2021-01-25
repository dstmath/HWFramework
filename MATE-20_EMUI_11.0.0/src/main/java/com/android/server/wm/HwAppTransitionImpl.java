package com.android.server.wm;

import android.animation.TimeInterpolator;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.SystemProperties;
import android.util.HwMwUtils;
import android.util.Log;
import android.util.Slog;
import android.view.Display;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ClipRectAnimation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.PathInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import com.android.server.AttributeCache;
import com.android.server.multiwin.animation.interpolator.FastOutSlowInInterpolator;
import com.huawei.hwanimation.HwClipRectAnimation;
import java.util.ArrayList;
import java.util.List;

public class HwAppTransitionImpl implements IHwAppTransition {
    private static final TimeInterpolator[] ALPHA_INTERPOLATORS = {CONST_INTERPOLATOR, new PathInterpolator(0.4f, 0.0f, 0.6f, 1.0f), CONST_INTERPOLATOR};
    private static final float ALPHA_KEY_1_FRACTION = 0.16f;
    private static final float ALPHA_KEY_2_FRACTION = 0.32f;
    private static final String ANIM_STYLE = SystemProperties.get("ro.feature.animation.style", "");
    private static final long CARD_ALPHA_ANIMATION_DURATION = 120;
    private static final long CARD_ALPHA_ANIMATION_START_OFF_TIME = 160;
    private static final long CARD_ANIMATION_DURATION = 280;
    private static final TimeInterpolator CONST_INTERPOLATOR = new TimeInterpolator() {
        /* class com.android.server.wm.HwAppTransitionImpl.AnonymousClass1 */

        @Override // android.animation.TimeInterpolator
        public float getInterpolation(float input) {
            return 1.0f;
        }
    };
    private static final boolean DBG = false;
    private static final float DEFAULT_FRAME_INTERVAL = 16.6667f;
    private static final float EPSINON = 10.0f;
    private static final long EXIT_ANIM_FOR_LOWPERM_DURATION = 300;
    private static final PathInterpolator FAST_OUT_SLOW_IN_INTERPOLATOR = new PathInterpolator(0.4f, 0.0f, 0.2f, 1.0f);
    private static final long FIND_ICON_ANIM_DURATION = 350;
    private static final long FIND_NO_ICON_ANIM_DURATION = 200;
    private static final long FLOAT_ANIMATION_MAX_TIME = SystemProperties.getLong("ro.build.hw_float_animation_max_time", (long) FULL_SCREEN_ANIMATION_DURATION);
    private static final long FLOAT_ANIMATION_MIN_TIME = SystemProperties.getLong("ro.build.hw_float_animation_min_time", (long) EXIT_ANIM_FOR_LOWPERM_DURATION);
    private static final float FLOAT_BALL_ORIGINAL_RADIUS = 22.0f;
    private static final long FLOAT_SCENCE_ANIMATION_OFFSET_TIME = SystemProperties.getLong("ro.build.hw_flaot_animation_offset", 50);
    private static final long FLOAT_SCENCE_ANIMATION_TRANS_DELAY_TIME = 100;
    private static final float FLOAT_SCENCE_MAX_SCALE_RATIO = 1.0f;
    private static final float FLOAT_SCENCE_MIN_SCALE_RATIO = 0.5f;
    private static final double FLOAT_TO_WINDOW_MAX_DISTANCE = ((double) SystemProperties.getLong("ro.build.hw_float_animation_max_distance", 3019));
    private static final float FOLDER_ICON_FINAL_SCALE_RATIO = 0.4f;
    private static final long FULL_SCREEN_ANIMATION_DURATION = 600;
    public static final boolean IS_EMUI_LITE = SystemProperties.getBoolean("ro.build.hw_emui_lite.enable", false);
    private static final boolean IS_FORCE_FULL_ANIM_ENABLE = SystemProperties.getBoolean("hw_sc.force_full_anim_enable", false);
    public static final boolean IS_NOVA_PERF = SystemProperties.getBoolean("ro.config.hw_nova_performance", false);
    private static final boolean IS_SUPER_LITE_ANIMA_STYLE = "supersimple".equals(ANIM_STYLE);
    private static final float LAUNCHER_ENTER_ALPHA_TIME_RATIO = 0.2f;
    private static final float LAUNCHER_ENTER_HIDE_TIME_RATIO = 0.3f;
    private static final float LAUNCHER_ENTER_HIDE_TIME_RATIO_LITE = 0.16f;
    private static final float LAUNCHER_ENTER_SCALE_TIME_RATIO = 0.7f;
    private static final float LAUNCHER_FROM_SCALE = 0.93f;
    private static final PathInterpolator LAUNCHER_SCALE_INTERPOLATOR = new PathInterpolator(0.2f, 0.0f, 0.1f, 1.0f);
    private static final float LAZY_MODE_COMP_FACTOR = 0.125f;
    private static final float LAZY_MODE_WIN_SCALE_FACTOR = 0.75f;
    private static final int LCD_DENSITY = SystemProperties.getInt("ro.sf.lcd_density", 480);
    private static final int LEFT_SINGLE_HAND_MODE = 1;
    private static final float MAX_ALPHA = 1.0f;
    private static final float MAX_FRACTION = 1.0f;
    private static final float MAX_SCALE = 1.0f;
    private static final float MID_SCALE_X_RATIO_HORIZANTAL = 0.54f;
    private static final float MIN_ALPHA = 0.0f;
    private static final float MIN_FRACTION = 0.0f;
    private static final long MS_IN_SEC = 1000;
    private static final int RES_ID_FLAG_MASK = -16777216;
    private static final int RES_ID_FLAG_SYSTEM = 16777216;
    private static final int RIGHT_SINGLE_HAND_MODE = 2;
    private static final float SCALE_KEY_1_FRACTION = 0.16f;
    private static final PathInterpolator SHARP_CURVE_INTERPOLATOR = new PathInterpolator(0.33f, 0.0f, 0.67f, 1.0f);
    private static final TimeInterpolator[] SIZE_BIG_INTERPOLATORS = {new PathInterpolator(0.44f, 0.43f, 0.7f, 0.75f), new PathInterpolator(0.13f, 0.79f, LAUNCHER_ENTER_HIDE_TIME_RATIO, 1.0f)};
    private static final TimeInterpolator[] SIZE_SMALL_INTERPOLATORS = {new PathInterpolator(0.41f, 0.38f, 0.7f, 0.71f), new PathInterpolator(0.16f, 0.64f, 0.33f, 1.0f)};
    private static final int SUPER_LITE_SCALE_DURATION = 250;
    private static final float SUPER_LITE_SCALE_RATIO = 0.5f;
    private static final String TAG = "HwAppTransitionImpl";
    private static final float TWO_CONST = 2.0f;
    private Context mHwextContext = null;

    public AttributeCache.Entry overrideAnimation(WindowManager.LayoutParams lp, int animAttr, Context mContext, AttributeCache.Entry mEnt, AppTransition appTransition) {
        int anim;
        int hwAnimResId;
        int hwAnimResId2;
        if (lp != null) {
            int windowAnimations = lp.windowAnimations;
            if ((RES_ID_FLAG_MASK & windowAnimations) != RES_ID_FLAG_SYSTEM) {
                Slog.d(TAG, "windowAnimations = " + Integer.toHexString(windowAnimations) + " dose not come from system, not to override it.");
                return mEnt;
            }
        }
        AttributeCache.Entry ent = null;
        if (mEnt == null) {
            return null;
        }
        Context context = mEnt.context;
        if (mEnt.array.getResourceId(animAttr, 0) != 0) {
            if (this.mHwextContext == null) {
                try {
                    this.mHwextContext = mContext.createPackageContext("androidhwext", 0);
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e(TAG, "overrideAnimation : no hwext package");
                }
            }
            if (this.mHwextContext != null) {
                int anim2 = 0;
                String title = lp != null ? lp.getTitle().toString() : "";
                if (title != null && !"".equals(title)) {
                    if ((IS_EMUI_LITE || IS_NOVA_PERF) && !IS_FORCE_FULL_ANIM_ENABLE) {
                        Resources resources = this.mHwextContext.getResources();
                        hwAnimResId2 = resources.getIdentifier("HwAnimation_lite." + title, "style", "androidhwext");
                    } else {
                        Resources resources2 = this.mHwextContext.getResources();
                        hwAnimResId2 = resources2.getIdentifier("HwAnimation." + title, "style", "androidhwext");
                    }
                    if (!(hwAnimResId2 == 0 || (ent = appTransition.getCachedAnimations("androidhwext", hwAnimResId2)) == null)) {
                        Context context2 = ent.context;
                        anim2 = ent.array.getResourceId(animAttr, 0);
                    }
                }
                if ((IS_EMUI_LITE || IS_NOVA_PERF) && !IS_FORCE_FULL_ANIM_ENABLE && anim2 == 0 && (hwAnimResId = this.mHwextContext.getResources().getIdentifier("HwAnimation_lite", "style", "androidhwext")) != 0 && (ent = appTransition.getCachedAnimations("androidhwext", hwAnimResId)) != null) {
                    Context context3 = ent.context;
                    anim2 = ent.array.getResourceId(animAttr, 0);
                }
                if (anim2 == 0) {
                    int hwAnimResId3 = this.mHwextContext.getResources().getIdentifier("HwAnimation", "style", "androidhwext");
                    if (hwAnimResId3 != 0) {
                        ent = appTransition.getCachedAnimations("androidhwext", hwAnimResId3);
                        if (ent != null) {
                            Context context4 = ent.context;
                            anim = ent.array.getResourceId(animAttr, 0);
                        } else {
                            anim = anim2;
                        }
                    } else {
                        anim = anim2;
                    }
                } else {
                    anim = anim2;
                }
                if (anim == 0) {
                    return null;
                }
            }
        }
        return ent;
    }

    static Animation createCardClipRevealAniamtion(boolean isEnter, Rect startCardRect, Rect displayFrame, float startAlpha, float endAlpha) {
        if (startCardRect == null || startCardRect.isEmpty() || displayFrame == null || displayFrame.isEmpty()) {
            return null;
        }
        AnimationSet animationSet = new AnimationSet(false);
        Animation clipRectAnimation = new ClipRectAnimation(startCardRect, displayFrame);
        clipRectAnimation.setDuration(CARD_ANIMATION_DURATION);
        clipRectAnimation.setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR);
        animationSet.addAnimation(clipRectAnimation);
        AlphaAnimation alphaAnimation = new AlphaAnimation(startAlpha, endAlpha);
        alphaAnimation.setDuration(CARD_ALPHA_ANIMATION_DURATION);
        if (!isEnter) {
            alphaAnimation.setStartOffset(CARD_ALPHA_ANIMATION_START_OFF_TIME);
        }
        alphaAnimation.setInterpolator(SHARP_CURVE_INTERPOLATOR);
        animationSet.addAnimation(alphaAnimation);
        animationSet.setFillEnabled(true);
        animationSet.setFillBefore(true);
        animationSet.setFillAfter(true);
        animationSet.setZAdjustment(1);
        animationSet.initialize(displayFrame.width(), displayFrame.height(), displayFrame.width(), displayFrame.height());
        return animationSet;
    }

    private static Animation createScaleDownAnimationForSuperLite(Rect winAnimFrame, float iconLeft, float iconRight, float iconTop, float iconBottom) {
        float pivotX;
        float pivotY;
        int appWidth = winAnimFrame.width();
        int appHeight = winAnimFrame.height();
        float centerX = (iconLeft + iconRight) / 2.0f;
        float centerY = (iconTop + iconBottom) / 2.0f;
        float halfWidth = ((float) appWidth) / 2.0f;
        float halfHeight = ((float) appHeight) / 2.0f;
        if (halfWidth - centerX > EPSINON) {
            pivotX = iconLeft;
        } else if (centerX - halfWidth > EPSINON) {
            pivotX = iconRight;
        } else {
            pivotX = centerX;
        }
        if (halfHeight - centerY > EPSINON) {
            pivotY = iconTop;
        } else if (centerY - halfHeight > EPSINON) {
            pivotY = iconBottom;
        } else {
            pivotY = centerY;
        }
        Animation scale = new ScaleAnimation(1.0f, 0.5f, 1.0f, 0.5f, pivotX, pivotY);
        scale.setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR);
        Animation alpha = new AlphaAnimation(1.0f, 0.0f);
        alpha.setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR);
        AnimationSet set = new AnimationSet(false);
        set.addAnimation(scale);
        set.addAnimation(alpha);
        set.setDetachWallpaper(true);
        set.setDuration(250);
        set.setFillAfter(true);
        set.setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR);
        set.initialize(appWidth, appHeight, appWidth, appHeight);
        return set;
    }

    /* JADX INFO: Multiple debug info for r7v1 float[]: [D('winAnimFrame' android.graphics.Rect), D('scaleOutValuesX' float[])] */
    static Animation createAppExitToIconAnimation(AppWindowToken atoken, int containingHeight, int iconWidth, int iconHeight, float originPivotX, float originPivotY, Bitmap icon, int exitFlag, int lazyMode) {
        int finalIconWidth;
        int finalIconHeight;
        long duration;
        float pivotY;
        if (atoken == null) {
            Slog.w(TAG, "create app exit animation find no app window token!");
            return null;
        }
        WindowState window = atoken.findMainWindow(IS_SUPER_LITE_ANIMA_STYLE);
        if (window == null) {
            Slog.w(TAG, "create app exit animation find no app main window!");
            return null;
        }
        Rect winAnimFrame = window.getDisplayFrameLw();
        if (winAnimFrame == null) {
            Slog.w(TAG, "create app exit animation find no winDisplayFrame!");
            return null;
        }
        Rect winDecorFrame = window.getDecorFrame();
        if (winDecorFrame == null) {
            Slog.w(TAG, "create app exit animation find no winDecorFrame!");
            return null;
        }
        winAnimFrame.intersect(winDecorFrame);
        int winWidth = winAnimFrame.width();
        int winHeight = winAnimFrame.height();
        if (winWidth > 0) {
            if (winHeight > 0) {
                boolean isHorizontal = winWidth > winHeight;
                float middleYRatio = MID_SCALE_X_RATIO_HORIZANTAL;
                float middleXRatio = isHorizontal ? 0.54f : 0.45999998f;
                if (isHorizontal) {
                    middleYRatio = 0.45999998f;
                }
                float middleX = 1.0f - ((((float) (winWidth - iconWidth)) * middleXRatio) / ((float) winWidth));
                float middleY = 1.0f - ((((float) (winHeight - iconHeight)) * middleYRatio) / ((float) winHeight));
                if (exitFlag == 1) {
                    finalIconWidth = (int) (((float) iconWidth) * 0.4f);
                    finalIconHeight = (int) (((float) iconHeight) * 0.4f);
                } else {
                    finalIconHeight = iconHeight;
                    finalIconWidth = iconWidth;
                }
                float toX = ((float) finalIconWidth) / ((float) winWidth);
                float toY = ((float) finalIconHeight) / ((float) winHeight);
                float iconLeft = originPivotX - (((float) finalIconWidth) / 2.0f);
                float iconTop = originPivotY - (((float) finalIconHeight) / 2.0f);
                float iconRight = (((float) finalIconWidth) / 2.0f) + originPivotX;
                float iconBottom = originPivotY + (((float) finalIconHeight) / 2.0f);
                if (IS_SUPER_LITE_ANIMA_STYLE) {
                    return createScaleDownAnimationForSuperLite(winAnimFrame, iconLeft, iconRight, iconTop, iconBottom);
                }
                float pivotX = ((((float) winAnimFrame.right) * iconLeft) - (((float) winAnimFrame.left) * iconRight)) / (((((float) winAnimFrame.right) - iconRight) + iconLeft) - ((float) winAnimFrame.left));
                float pivotY2 = ((((float) winAnimFrame.bottom) * iconTop) - (((float) winAnimFrame.top) * iconBottom)) / (((((float) winAnimFrame.bottom) - iconBottom) + iconTop) - ((float) winAnimFrame.top));
                if (window.mWmService.getFoldDisplayMode() == 3) {
                    pivotY2 *= window.mWmService.mSubFoldModeScale;
                    pivotX *= window.mWmService.mSubFoldModeScale;
                }
                if (window.getFrameLw() == null) {
                    Slog.w(TAG, "create app exit animation find no app window frame!");
                    return null;
                }
                if (HwWmConstants.IS_APP_LOW_PERF_ANIM) {
                    duration = 300;
                } else {
                    duration = 350;
                }
                AnimationSet appExitToIconAnimation = new AnimationSet(false);
                appExitToIconAnimation.setStartOffset(getCurrentFrameInterval(window));
                appExitToIconAnimation.setStartTime(-1);
                appExitToIconAnimation.addAnimation(createAppAlphaAnimation(duration));
                if (!HwMwUtils.ENABLED || !window.inHwMagicWindowingMode()) {
                    pivotY = pivotY2;
                } else {
                    Bundle bundle = HwMwUtils.performPolicy(105, new Object[]{window, new float[]{originPivotX, originPivotY}, Integer.valueOf(iconWidth), Integer.valueOf(iconHeight)});
                    toX = bundle.getFloat("BUNDLE_EXITANIM_SCALETOX", 0.0f);
                    toY = bundle.getFloat("BUNDLE_EXITANIM_SCALETOY", 0.0f);
                    pivotX = bundle.getFloat("BUNDLE_EXITANIM_PIVOTX", 0.0f);
                    pivotY = bundle.getFloat("BUNDLE_EXITANIM_PIVOTY", 0.0f);
                }
                float[] scaleInValues = {0.0f, 0.16f, 1.0f};
                float[] scaleOutValuesX = {1.0f, middleX, toX};
                float[] scaleOutValuesY = {1.0f, middleY, toY};
                TimeInterpolator[] sizeXInterpolators = isHorizontal ? SIZE_BIG_INTERPOLATORS : SIZE_SMALL_INTERPOLATORS;
                TimeInterpolator[] sizeYInterpolators = isHorizontal ? SIZE_SMALL_INTERPOLATORS : SIZE_BIG_INTERPOLATORS;
                PhaseInterpolator interpolatorX = new PhaseInterpolator(scaleInValues, scaleOutValuesX, sizeXInterpolators);
                ScaleAnimation scaleXAnim = new ScaleAnimation(0.0f, 1.0f, 1.0f, 1.0f, pivotX, pivotY);
                scaleXAnim.setFillEnabled(true);
                scaleXAnim.setFillBefore(true);
                scaleXAnim.setFillAfter(true);
                scaleXAnim.setDuration(duration);
                scaleXAnim.setInterpolator(interpolatorX);
                appExitToIconAnimation.addAnimation(scaleXAnim);
                PhaseInterpolator interpolatorY = new PhaseInterpolator(scaleInValues, scaleOutValuesY, sizeYInterpolators);
                ScaleAnimation scaleYAnim = new ScaleAnimation(1.0f, 1.0f, 0.0f, 1.0f, pivotX, pivotY);
                scaleYAnim.setFillEnabled(true);
                scaleYAnim.setFillBefore(true);
                scaleYAnim.setFillAfter(true);
                scaleYAnim.setDuration(duration);
                scaleYAnim.setInterpolator(interpolatorY);
                appExitToIconAnimation.addAnimation(scaleYAnim);
                appExitToIconAnimation.setZAdjustment(1);
                if (atoken.mShouldDrawIcon && icon != null) {
                    window.mWinAnimator.setWindowIconInfo(0 | 1, iconWidth, iconHeight, icon);
                }
                return appExitToIconAnimation;
            }
        }
        return null;
    }

    private static long getCurrentFrameInterval(WindowState windowState) {
        Display display;
        if (windowState == null || windowState.mWmService == null) {
            return 33;
        }
        if (windowState.mWmService.getDefaultDisplayContentLocked() == null) {
            display = null;
        } else {
            display = windowState.mWmService.getDefaultDisplayContentLocked().getDisplay();
        }
        if (display != null) {
            return (MS_IN_SEC / ((long) display.getRefreshRate())) * 2;
        }
        return 33;
    }

    private static Animation createAppAlphaAnimation(long duration) {
        AlphaAnimation alphaAnim = new AlphaAnimation(0.0f, 1.0f);
        alphaAnim.setDuration(duration);
        alphaAnim.setFillEnabled(true);
        alphaAnim.setFillBefore(true);
        alphaAnim.setFillAfter(true);
        alphaAnim.setInterpolator(new PhaseInterpolator(new float[]{0.0f, 0.16f, ALPHA_KEY_2_FRACTION, 1.0f}, new float[]{1.0f, 1.0f, 0.0f, 0.0f}, ALPHA_INTERPOLATORS));
        return alphaAnim;
    }

    static Animation createLauncherEnterAnimation(AppWindowToken atoken, int containingHeight, int iconWidth, int iconHeight, float originPivotX, float originPivotY, Bitmap iconBitmap) {
        long duration;
        if (IS_SUPER_LITE_ANIMA_STYLE) {
            return null;
        }
        if (atoken == null) {
            Slog.w(TAG, "create launcher enter animation find no app window token!");
            return null;
        }
        WindowState window = atoken.findMainWindow();
        if (window == null) {
            Slog.w(TAG, "create launcher enter animation find no app main window!");
            return null;
        }
        Rect winDisplayFrame = window.getDisplayFrameLw();
        if (winDisplayFrame == null) {
            Slog.w(TAG, "create launcher enter animation find no winDisplayFrame!");
            return null;
        }
        int winWidth = winDisplayFrame.width();
        int winHeight = winDisplayFrame.height();
        if (winWidth <= 0 || winHeight <= 0) {
            return null;
        }
        float iconLeft = originPivotX - (((float) iconWidth) / 2.0f);
        float iconTop = originPivotY - (((float) iconHeight) / 2.0f);
        float iconRight = (((float) iconWidth) / 2.0f) + originPivotX;
        float iconBottom = (((float) iconHeight) / 2.0f) + originPivotY;
        float pivotX = ((((float) winDisplayFrame.right) * iconLeft) - (((float) winDisplayFrame.left) * iconRight)) / (((((float) winDisplayFrame.right) - iconRight) + iconLeft) - ((float) winDisplayFrame.left));
        float pivotY = ((((float) winDisplayFrame.bottom) * iconTop) - (((float) winDisplayFrame.top) * iconBottom)) / (((((float) winDisplayFrame.bottom) - iconBottom) + iconTop) - ((float) winDisplayFrame.top));
        if (originPivotX < 0.0f || originPivotY < 0.0f) {
            pivotX = ((float) winWidth) / 2.0f;
            pivotY = ((float) winHeight) / 2.0f;
        }
        if (window.mWmService.getFoldDisplayMode() == 3) {
            pivotY *= window.mWmService.mSubFoldModeScale;
            pivotX *= window.mWmService.mSubFoldModeScale;
        }
        if (iconWidth < 0 || iconHeight < 0 || iconBitmap == null) {
            duration = 200;
        } else {
            duration = 350;
        }
        AnimationSet launcherEnterAnimation = new AnimationSet(false);
        launcherEnterAnimation.addAnimation(createLauncherScaleAnimation(duration, pivotX, pivotY));
        launcherEnterAnimation.addAnimation(createLauncherAlphaAnimation(duration));
        launcherEnterAnimation.setDetachWallpaper(true);
        launcherEnterAnimation.setZAdjustment(0);
        return launcherEnterAnimation;
    }

    private static Animation createLauncherScaleAnimation(long duration, float pivotX, float pivotY) {
        ScaleAnimation scaleAnimation = new ScaleAnimation(LAUNCHER_FROM_SCALE, 1.0f, LAUNCHER_FROM_SCALE, 1.0f, pivotX, pivotY);
        scaleAnimation.setFillEnabled(true);
        scaleAnimation.setFillBefore(true);
        scaleAnimation.setFillAfter(true);
        scaleAnimation.setStartOffset((long) (((float) duration) * ((!IS_EMUI_LITE || IS_FORCE_FULL_ANIM_ENABLE) ? LAUNCHER_ENTER_HIDE_TIME_RATIO : 0.16f)));
        scaleAnimation.setDuration((long) (((float) duration) * 0.7f));
        scaleAnimation.setInterpolator(LAUNCHER_SCALE_INTERPOLATOR);
        return scaleAnimation;
    }

    private static Animation createLauncherAlphaAnimation(long duration) {
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
        alphaAnimation.setFillEnabled(true);
        alphaAnimation.setFillBefore(true);
        alphaAnimation.setFillAfter(true);
        alphaAnimation.setDuration((long) (((float) duration) * 0.2f));
        alphaAnimation.setStartOffset((long) (((float) duration) * ((!IS_EMUI_LITE || IS_FORCE_FULL_ANIM_ENABLE) ? LAUNCHER_ENTER_HIDE_TIME_RATIO : 0.16f)));
        alphaAnimation.setInterpolator(new LinearInterpolator());
        return alphaAnimation;
    }

    static WindowAnimationSpec setFloatSceneCornerRadiusInfo(AppWindowToken atoken, WindowAnimationSpec windowAnimationSpec, float originPivotX, float originPivotY, boolean isPositive) {
        if (atoken == null) {
            Slog.w(TAG, "set float scene corner radius info atoken is null!");
            return windowAnimationSpec;
        }
        WindowManagerService windowManagerService = atoken.mWmService;
        if (windowManagerService == null || windowManagerService.mAtmService == null || windowManagerService.mAtmService.mHwATMSEx == null) {
            Slog.w(TAG, "set float scene corner radius info atoken windowManagerService instance exception!");
            return windowAnimationSpec;
        }
        float durationScale = windowManagerService.getTransitionAnimationScaleLocked();
        float finalRadius = getRealWindowCornerRadius(windowManagerService, atoken.findMainWindow(), windowManagerService.mAtmService.mHwATMSEx.getHwMultiWinCornerRadius(102));
        WindowState window = atoken.findMainWindow();
        if (window == null) {
            Slog.w(TAG, "create float draw back animation find no app main window!");
            return windowAnimationSpec;
        }
        float iconCornerRadius = getFloatBallOriginalRadius();
        long duration = getHwFreeFormWindowAnimationDuration(window, originPivotX, originPivotY, isPositive);
        long j = FLOAT_SCENCE_ANIMATION_OFFSET_TIME;
        long durationOpenAnim = duration + j;
        long durationDrawBackExcludeTrans = (duration - j) - FLOAT_SCENCE_ANIMATION_TRANS_DELAY_TIME;
        if (duration != 0) {
            if (durationOpenAnim != 0) {
                if (isPositive) {
                    windowAnimationSpec.setDynamicCornerRadiusInfo(new PhaseInterpolator(new float[]{0.0f, (((float) durationDrawBackExcludeTrans) * 1.0f) / (((float) duration) * 1.0f), 1.0f}, new float[]{0.0f, 1.0f, 1.0f}, new Interpolator[]{new FastOutSlowInInterpolator(), new LinearInterpolator()}), finalRadius, iconCornerRadius, durationScale, duration, 0);
                } else {
                    windowAnimationSpec.setDynamicCornerRadiusInfo(new PhaseInterpolator(new float[]{0.0f, (((float) j) * 1.0f) / (((float) durationOpenAnim) * 1.0f), 1.0f}, new float[]{0.0f, 0.0f, 1.0f}, new Interpolator[]{new LinearInterpolator(), new FastOutSlowInInterpolator()}), iconCornerRadius, finalRadius, durationScale, durationOpenAnim, 0);
                }
                return windowAnimationSpec;
            }
        }
        Slog.w(TAG, "Animation duration is wrong, set float scene corner radius failed!");
        return windowAnimationSpec;
    }

    private static float getRealWindowCornerRadius(WindowManagerService wms, WindowState window, float orignalRadius) {
        float finalRadius = orignalRadius;
        if (!(window == null || window.getStack() == null)) {
            finalRadius *= window.getStack().mHwStackScale;
        }
        if (wms == null || wms.getLazyMode() == 0) {
            return finalRadius;
        }
        return finalRadius * 0.75f;
    }

    static Animation createFloatDrawBackAnimation(WindowState window, float targetPivotX, float targetPivotY, float iconRadius) {
        RectF winAnimFrameReal = getRealBounds(window);
        int winWidth = (int) winAnimFrameReal.width();
        int winHeight = (int) winAnimFrameReal.height();
        if (winWidth <= 0 || winHeight <= 0) {
            return null;
        }
        Slog.d(TAG, "now set float draw back animation for: " + window + ", [winWidth, winHeight] = [" + winWidth + ", " + winHeight + "][originPivotX, originPivotY] = [" + targetPivotX + ", " + targetPivotY + "]");
        AnimationSet floatDrawBackAnimation = getFloatDrawBackAnimation(window, targetPivotX, targetPivotY, winAnimFrameReal, iconRadius);
        adjustFloatDrawBackAnimationForIME(floatDrawBackAnimation, window, targetPivotX, targetPivotY, iconRadius);
        return floatDrawBackAnimation;
    }

    private static RectF getRealBounds(WindowState window) {
        Rect winFrame = new Rect();
        winFrame.set(window.getBounds());
        int left = winFrame.left;
        int top = winFrame.top;
        winFrame.scale(window.getStack().mHwStackScale);
        winFrame.offsetTo(left, top);
        return new RectF((float) winFrame.left, (float) winFrame.top, (float) winFrame.right, (float) winFrame.bottom);
    }

    private static RectF getVisibleBounds(WindowState window) {
        Rect winFrame = new Rect();
        winFrame.set(window.getContainingFrame());
        int left = winFrame.left;
        int top = winFrame.top;
        winFrame.scale(window.getStack().mHwStackScale);
        winFrame.offsetTo(left, top);
        return new RectF((float) winFrame.left, (float) winFrame.top, (float) winFrame.right, (float) winFrame.bottom);
    }

    private static void addfloatDrawBackScaleAnimation(AnimationSet animationSet, long duration, int winWidth, int winHeight) {
        ScaleAnimation scaleXAnim = new ScaleAnimation(1.0f, 0.5f, 1.0f, 0.5f, ((float) winWidth) / 2.0f, ((float) winHeight) / 2.0f);
        scaleXAnim.setFillEnabled(true);
        scaleXAnim.setFillBefore(true);
        scaleXAnim.setFillAfter(true);
        scaleXAnim.setDuration((duration - FLOAT_SCENCE_ANIMATION_OFFSET_TIME) - FLOAT_SCENCE_ANIMATION_TRANS_DELAY_TIME);
        scaleXAnim.setInterpolator(new FastOutSlowInInterpolator());
        animationSet.addAnimation(scaleXAnim);
    }

    private static void addFloatDrawBackTransLateAnimation(AnimationSet animationSet, long duration, float originPivotX, float originPivotY, RectF realFrameRect) {
        TranslateAnimation transLateAnimation = new TranslateAnimation(0, 0.0f, 0, (originPivotX - (((float) ((int) realFrameRect.width())) / 2.0f)) - realFrameRect.left, 0, 0.0f, 0, (originPivotY - (((float) ((int) realFrameRect.height())) / 2.0f)) - realFrameRect.top);
        transLateAnimation.setFillEnabled(true);
        transLateAnimation.setFillBefore(true);
        transLateAnimation.setFillAfter(true);
        transLateAnimation.setDuration(duration);
        float transLateDuration = (float) (duration - FLOAT_SCENCE_ANIMATION_OFFSET_TIME);
        if (duration == 0) {
            Slog.w(TAG, "translate animation duration is wrong");
            return;
        }
        transLateAnimation.setInterpolator(new PhaseInterpolator(new float[]{0.0f, transLateDuration / ((float) duration), 1.0f}, new float[]{0.0f, 1.0f, 1.0f}, new Interpolator[]{new FastOutSlowInInterpolator(), new LinearInterpolator()}));
        animationSet.addAnimation(transLateAnimation);
    }

    private static void addFloatDrawBackClipAnimation(AnimationSet animationSet, long duration, int winWidth, int winHeight, float iconRadius) {
        HwClipRectAnimation clipRectAnimation = new HwClipRectAnimation(new Rect(0, 0, winWidth, winHeight), new Rect((int) ((((float) winWidth) / 2.0f) - iconRadius), (int) ((((float) winHeight) / 2.0f) - iconRadius), (int) ((((float) winWidth) / 2.0f) + iconRadius), (int) ((((float) winHeight) / 2.0f) + iconRadius)));
        clipRectAnimation.setFillEnabled(true);
        clipRectAnimation.setFillBefore(true);
        clipRectAnimation.setFillAfter(true);
        clipRectAnimation.setDuration((duration - FLOAT_SCENCE_ANIMATION_OFFSET_TIME) - FLOAT_SCENCE_ANIMATION_TRANS_DELAY_TIME);
        clipRectAnimation.setInterpolator(new FastOutSlowInInterpolator());
        animationSet.addAnimation(clipRectAnimation);
    }

    private static void adjustFloatDrawBackAnimationForIME(AnimationSet animationSet, WindowState window, float originPivotX, float originPivotY, float iconRadius) {
        RectF realBounds;
        ScaleAnimation adjustedAnimation;
        if (animationSet == null || window == null) {
            Slog.w(TAG, "Fail to adjust draw back animation, because animation or window is null");
            return;
        }
        List<Animation> animationList = animationSet.getAnimations();
        RectF realBounds2 = getRealBounds(window);
        RectF visibleBounds = getVisibleBounds(window);
        int relativePositionX = (int) (visibleBounds.left - realBounds2.left);
        int relativePositionY = (int) (visibleBounds.top - realBounds2.top);
        if (relativePositionX == 0 && relativePositionY == 0) {
            Slog.i(TAG, "There is no need to adjust draw back animation");
            return;
        }
        List<Animation> adjustedAnimationList = new ArrayList<>();
        float realBoundsCenterX = visibleBounds.width() / 2.0f;
        float realBoundsCenterY = visibleBounds.height() / 2.0f;
        for (Animation animation : animationList) {
            if (animation instanceof ScaleAnimation) {
                PointF iconPoint = new PointF(realBoundsCenterX, realBoundsCenterY);
                iconPoint.offset((float) relativePositionX, (float) relativePositionY);
                adjustedAnimation = new ScaleAnimation(1.0f, 0.5f, 1.0f, 0.5f, iconPoint.x, iconPoint.y);
                realBounds = realBounds2;
            } else if (animation instanceof TranslateAnimation) {
                adjustedAnimation = new TranslateAnimation(0, 0.0f, 0, ((originPivotX - realBoundsCenterX) - ((float) relativePositionX)) - realBounds2.left, 0, 0.0f, 0, ((originPivotY - realBoundsCenterY) - ((float) relativePositionY)) - realBounds2.top);
                realBounds = realBounds2;
            } else if (animation instanceof HwClipRectAnimation) {
                Rect startClipRect = new Rect(0, 0, (int) visibleBounds.width(), (int) visibleBounds.height());
                startClipRect.offset(relativePositionX, relativePositionY);
                realBounds = realBounds2;
                Rect endClipRect = new Rect((int) realBoundsCenterX, (int) realBoundsCenterY, (int) realBoundsCenterX, (int) realBoundsCenterY);
                endClipRect.inset(-((int) iconRadius), -((int) iconRadius));
                endClipRect.offset(relativePositionX, relativePositionY);
                adjustedAnimation = new HwClipRectAnimation(startClipRect, endClipRect);
            } else {
                realBounds = realBounds2;
                adjustedAnimation = animation;
            }
            adjustedAnimationList.add(copyAnimationParams(adjustedAnimation, animation));
            realBounds2 = realBounds;
        }
        animationList.clear();
        animationList.addAll(adjustedAnimationList);
    }

    private static Animation copyAnimationParams(Animation originalAnimation, Animation targetAnimation) {
        if (originalAnimation == null || targetAnimation == null) {
            return originalAnimation;
        }
        originalAnimation.setFillEnabled(true);
        originalAnimation.setFillBefore(true);
        originalAnimation.setFillAfter(true);
        originalAnimation.setDuration(targetAnimation.getDuration());
        originalAnimation.setInterpolator(targetAnimation.getInterpolator());
        return originalAnimation;
    }

    private static AnimationSet getFloatDrawBackAnimation(WindowState window, float fianlPivotX, float fianlPivotY, RectF winAnimFrameReal, float iconRadius) {
        AnimationSet floatDrawBackAnimation = new AnimationSet(false);
        long duration = getHwFreeFormWindowAnimationDuration(window, fianlPivotX, fianlPivotY, true);
        addfloatDrawBackScaleAnimation(floatDrawBackAnimation, duration, (int) winAnimFrameReal.width(), (int) winAnimFrameReal.height());
        addFloatDrawBackTransLateAnimation(floatDrawBackAnimation, duration, fianlPivotX, fianlPivotY, winAnimFrameReal);
        addFloatDrawBackClipAnimation(floatDrawBackAnimation, duration, (int) winAnimFrameReal.width(), (int) winAnimFrameReal.height(), iconRadius);
        floatDrawBackAnimation.setZAdjustment(1);
        return floatDrawBackAnimation;
    }

    static Animation createFloatOpenAnimation(WindowState window, float targetPivotX, float targetPivotY, float iconRadius) {
        RectF winAnimFrameReal = getRealBounds(window);
        int winWidth = (int) winAnimFrameReal.width();
        int winHeight = (int) winAnimFrameReal.height();
        if (winWidth <= 0 || winHeight <= 0) {
            return null;
        }
        Slog.d(TAG, "now create float open animation for: " + window + ", [winWidth, winHeight] = [" + winWidth + ", " + winHeight + "][originPivotX, originPivotY] = [" + targetPivotX + ", " + targetPivotY + "]");
        return getFloatOpenAnimation(window, targetPivotX, targetPivotY, winAnimFrameReal, iconRadius);
    }

    private static void addFloatOpenScaleAnimation(AnimationSet animationSet, long duration, int winWidth, int winHeight) {
        ScaleAnimation scaleXAnim = new ScaleAnimation(0.5f, 1.0f, 0.5f, 1.0f, ((float) winWidth) / 2.0f, ((float) winHeight) / 2.0f);
        scaleXAnim.setFillEnabled(true);
        scaleXAnim.setFillBefore(true);
        scaleXAnim.setFillAfter(true);
        long realDuration = FLOAT_SCENCE_ANIMATION_OFFSET_TIME + duration;
        if (realDuration == 0) {
            Slog.w(TAG, "Animation duration is wrong, add float open scale animation failed!");
        }
        scaleXAnim.setDuration(realDuration);
        scaleXAnim.setInterpolator(new PhaseInterpolator(new float[]{0.0f, (((float) FLOAT_SCENCE_ANIMATION_OFFSET_TIME) * 1.0f) / (((float) realDuration) * 1.0f), 1.0f}, new float[]{0.0f, 0.0f, 1.0f}, new Interpolator[]{new LinearInterpolator(), new FastOutSlowInInterpolator()}));
        animationSet.addAnimation(scaleXAnim);
    }

    private static void addFloatOpenTransLateAnimation(AnimationSet animationSet, long duration, float originPivotX, float originPivotY, RectF realFrameRect) {
        TranslateAnimation transLateAnimation = new TranslateAnimation(0, (originPivotX - (((float) ((int) realFrameRect.width())) / 2.0f)) - realFrameRect.left, 0, 0.0f, 0, (originPivotY - (((float) ((int) realFrameRect.height())) / 2.0f)) - realFrameRect.top, 0, 0.0f);
        transLateAnimation.setFillEnabled(true);
        transLateAnimation.setFillBefore(true);
        transLateAnimation.setFillAfter(true);
        long realDuration = FLOAT_SCENCE_ANIMATION_OFFSET_TIME + duration;
        if (realDuration == 0) {
            Slog.w(TAG, "Animation duration is wrong, add float open translate animation failed!");
        }
        transLateAnimation.setDuration(realDuration);
        transLateAnimation.setInterpolator(new PhaseInterpolator(new float[]{0.0f, (((float) FLOAT_SCENCE_ANIMATION_OFFSET_TIME) * 1.0f) / (((float) realDuration) * 1.0f), 1.0f}, new float[]{0.0f, 0.0f, 1.0f}, new Interpolator[]{new LinearInterpolator(), new FastOutSlowInInterpolator()}));
        animationSet.addAnimation(transLateAnimation);
    }

    private static void addFloatOpenClipAnimation(AnimationSet animationSet, long duration, int winWidth, int winHeight, float iconRadius) {
        HwClipRectAnimation clipRectAnimation = new HwClipRectAnimation(new Rect((int) ((((float) winWidth) / 2.0f) - iconRadius), (int) ((((float) winHeight) / 2.0f) - iconRadius), (int) ((((float) winWidth) / 2.0f) + iconRadius), (int) ((((float) winHeight) / 2.0f) + iconRadius)), new Rect(0, 0, winWidth, winHeight));
        clipRectAnimation.setFillEnabled(true);
        clipRectAnimation.setFillBefore(true);
        clipRectAnimation.setFillAfter(true);
        long realDuration = FLOAT_SCENCE_ANIMATION_OFFSET_TIME + duration;
        if (realDuration == 0) {
            Slog.w(TAG, "Animation duration is wrong, add float open clip animation failed!");
        }
        clipRectAnimation.setDuration(realDuration);
        clipRectAnimation.setInterpolator(new PhaseInterpolator(new float[]{0.0f, (((float) FLOAT_SCENCE_ANIMATION_OFFSET_TIME) * 1.0f) / (((float) realDuration) * 1.0f), 1.0f}, new float[]{0.0f, 0.0f, 1.0f}, new Interpolator[]{new LinearInterpolator(), new FastOutSlowInInterpolator()}));
        animationSet.addAnimation(clipRectAnimation);
    }

    private static AnimationSet getFloatOpenAnimation(WindowState window, float fianlPivotX, float fianlPivotY, RectF winAnimFrameReal, float iconRadius) {
        AnimationSet floatOpenAnimation = new AnimationSet(false);
        long duration = getHwFreeFormWindowAnimationDuration(window, fianlPivotX, fianlPivotY, false);
        addFloatOpenScaleAnimation(floatOpenAnimation, duration, (int) winAnimFrameReal.width(), (int) winAnimFrameReal.height());
        addFloatOpenTransLateAnimation(floatOpenAnimation, duration, fianlPivotX, fianlPivotY, winAnimFrameReal);
        addFloatOpenClipAnimation(floatOpenAnimation, duration, (int) winAnimFrameReal.width(), (int) winAnimFrameReal.height(), iconRadius);
        floatOpenAnimation.setZAdjustment(1);
        if (window.getWindowingMode() == 1) {
            floatOpenAnimation.setStartOffset(FLOAT_SCENCE_ANIMATION_OFFSET_TIME);
        }
        return floatOpenAnimation;
    }

    public static AnimationSet createFullScreenBackgroundAnimation(boolean isWallpaper, boolean isDrawBackScene) {
        if (isDrawBackScene) {
            return new AnimationSet(false);
        }
        AnimationSet animationSet = new AnimationSet(false);
        Animation alphaAnimation = new AlphaAnimation(1.0f, isWallpaper ? 0.0f : 1.0f);
        alphaAnimation.setDuration(FLOAT_SCENCE_ANIMATION_OFFSET_TIME + FULL_SCREEN_ANIMATION_DURATION);
        animationSet.addAnimation(alphaAnimation);
        animationSet.setHasRoundedCorners(false);
        animationSet.setStartOffset(FLOAT_SCENCE_ANIMATION_OFFSET_TIME);
        return animationSet;
    }

    private static long getHwFreeFormWindowAnimationDuration(WindowState window, float originPivotX, float originPivotY, boolean isDrawBackScene) {
        long resultDuration;
        if (window == null) {
            return FLOAT_ANIMATION_MIN_TIME;
        }
        if (window.getWindowingMode() == 1) {
            return isDrawBackScene ? FULL_SCREEN_ANIMATION_DURATION + FLOAT_SCENCE_ANIMATION_OFFSET_TIME : FULL_SCREEN_ANIMATION_DURATION;
        }
        Rect winFrame = window.getFrameLw();
        double distanceX = (double) Math.abs(originPivotX - ((float) winFrame.left));
        double distanceY = (double) Math.abs(originPivotY - ((float) winFrame.top));
        double distance = Math.sqrt((distanceX * distanceX) + (distanceY * distanceY));
        long j = FLOAT_ANIMATION_MAX_TIME;
        long j2 = FLOAT_ANIMATION_MIN_TIME;
        double duration = ((((double) (j - j2)) / FLOAT_TO_WINDOW_MAX_DISTANCE) * distance) + ((double) j2);
        if (Double.isNaN(duration) || Double.isInfinite(duration)) {
            resultDuration = FLOAT_ANIMATION_MIN_TIME;
        } else {
            resultDuration = Math.round(duration);
        }
        if (resultDuration < FLOAT_ANIMATION_MIN_TIME) {
            resultDuration = FLOAT_ANIMATION_MIN_TIME;
        } else if (resultDuration > FLOAT_ANIMATION_MAX_TIME) {
            resultDuration = FLOAT_ANIMATION_MAX_TIME;
        } else {
            Slog.d(TAG, "HwFreeFormWindowAnimationDuration : " + resultDuration);
        }
        Slog.d(TAG, "getHwFreeFormWindowAnimationDuration duration : " + duration);
        return resultDuration;
    }

    public static float getFloatBallOriginalRadius() {
        return (getLcdDensity() * FLOAT_BALL_ORIGINAL_RADIUS) / 0.5f;
    }

    private static float getLcdDensity() {
        return (((float) LCD_DENSITY) * 1.0f) / 160.0f;
    }
}
