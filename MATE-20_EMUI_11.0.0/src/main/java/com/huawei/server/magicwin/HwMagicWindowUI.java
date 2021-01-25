package com.huawei.server.magicwin;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.HwMwUtils;
import android.util.HwPCUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.android.server.wm.ActivityRecordEx;
import com.android.server.wm.HwMagicContainer;
import com.android.server.wm.HwMagicWinManager;
import com.android.server.wm.HwMultiWindowSplitUI;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.content.ContentResolverExt;
import com.huawei.android.provider.SettingsEx;
import com.huawei.android.util.SlogEx;
import com.huawei.android.view.LayoutParamsExt;
import com.huawei.utils.HwPartResourceUtils;

public class HwMagicWindowUI {
    private static final String BLACK_COLOR = "#000000";
    private static final int BLACK_TRANSPARENCY_DARK = -16777216;
    private static final int BLACK_TRANSPARENCY_LIGHT = 1291845632;
    private static final int DEFAULT_SPLIT_BG_COLOR = -1;
    private static final int DURATION_BACKGROUND_ANIMATION_INVISIBLE = 0;
    private static final int DURATION_BACKGROUND_ANIMATION_VISIBLE = 400;
    private static final int DURATION_CHANGE_BG_COLOR_FIRST_STEP = 150;
    private static final int DURATION_CHANGE_BG_COLOR_SECOND_STEP = 150;
    private static final long DURATION_DOUBLE_TO_SINGLE = 10;
    private static final long DURATION_SINGLE_TO_DOUBLE = 750;
    private static final float FULLY_TRANSPARENT = 0.0f;
    private static final int GESTURE_ANIMATION_END_HOME = 12;
    private static final int GESTURE_ANIMATION_END_LASTTASK = 15;
    private static final int GESTURE_ANIMATION_END_NEWTASK = 14;
    private static final int GESTURE_ANIMATION_END_NULL = 16;
    private static final int GESTURE_ANIMATION_END_RECENTS = 13;
    private static final int GESTURE_ANIMATION_START = 11;
    private static final String GESTURE_HOME_ANIMATOR = "gesture_home_animator";
    private static final int GESTURE_STARTNEWTASK_FAIL = 19;
    private static final int GESTURE_STARTNEWTASK_START = 17;
    private static final int GESTURE_STARTNEWTASK_SUCCESS = 18;
    private static final int HOME_ANIMATION_END = 0;
    private static final int HOME_ANIMATION_START = 1;
    private static final String LAUNCHER_PACKAGE_NAME = "com.huawei.android.launcher";
    private static final int MAGICWINDOW_WALLPAPER_DELAY_TIME = 250;
    private static final int MAGIC_WINDOW_TYPE = 103;
    private static final float OPAQUE = 1.0f;
    private static final int RECENT_ANIMATION_CANCEL = -1;
    private static final String TAG = "HWMW_HwMagicWindowUI";
    private static final int TAH_ENTER_BACKGROUND_DURATION = 750;
    private static final float TRANSPARENCY_DARK = 1.0f;
    private static final float TRANSPARENCY_LIGHT = 0.3f;
    public static final int WALLPAPER_FLAG_CHANGE_VISIBLE = 1;
    public static final int WALLPAPER_FLAG_IS_DISABLE_ANIM = 8;
    public static final int WALLPAPER_FLAG_IS_GESTURE_BACK_HOME = 4;
    public static final int WALLPAPER_FLAG_IS_MIDDLE = 2;
    public static final int WALLPAPER_FLAG_IS_REQUEST_VISIBLE = 32;
    public static final int WALLPAPER_FLAG_IS_TAH_DELAY_ANIMATION = 16;
    private ObjectAnimator alphaAnimator;
    private ObjectAnimator animator;
    private Animator.AnimatorListener mBackgroundEnterAnimatorListener;
    private Animator.AnimatorListener mBackgroundExitAnimatorListener;
    private FrameLayout mBackgroundLayout;
    private ImageView mBackgroundView;
    private HwMagicContainer mContainer;
    private Context mContext;
    private HwMagicWindowUIController mController;
    private int mDisplayId;
    private DisplayMetrics mDisplayMetrics;
    private ContentObserver mGestureHomeAnimatorObserver;
    private HwMultiWindowSplitUI mHwMultiWindowSplitUI;
    private boolean mIsEnterMwWallpaperAnimating;
    private boolean mIsMiddle;
    private boolean mIsNeedUpdateWallPaperSize;
    private boolean mIsSetWallpaperVisible;
    private HwMagicWinManager mMwManager;
    private boolean mVisible;
    private WindowManager mWindowManager;

    public HwMagicWindowUI(HwMagicContainer container, HwMagicWinManager manager, HwMagicWindowUIController controller) {
        this(null, container, manager, controller);
    }

    public HwMagicWindowUI(Context context, HwMagicContainer container, HwMagicWinManager manager, HwMagicWindowUIController controller) {
        this.mController = null;
        this.mMwManager = null;
        this.mWindowManager = null;
        this.mDisplayMetrics = null;
        this.mDisplayId = 0;
        this.mContext = null;
        this.mContainer = null;
        this.mHwMultiWindowSplitUI = null;
        this.mIsMiddle = true;
        this.mVisible = false;
        this.mIsSetWallpaperVisible = false;
        this.mIsNeedUpdateWallPaperSize = false;
        this.mBackgroundLayout = null;
        this.mBackgroundView = null;
        this.mGestureHomeAnimatorObserver = null;
        this.mIsEnterMwWallpaperAnimating = false;
        this.alphaAnimator = null;
        this.animator = null;
        this.mBackgroundEnterAnimatorListener = new Animator.AnimatorListener() {
            /* class com.huawei.server.magicwin.HwMagicWindowUI.AnonymousClass1 */

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animation) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animation) {
                HwMagicWindowUI.this.mIsEnterMwWallpaperAnimating = false;
                if (!HwMagicWindowUI.this.isNeedChangeBgColor()) {
                    Message msg = HwMagicWindowUI.this.mController.getUiHandler().obtainMessage(3);
                    msg.arg2 = HwMagicWindowUI.this.mDisplayId;
                    HwMagicWindowUI.this.mController.getUiHandler().sendMessage(msg);
                    HwMagicWindowUI.this.mBackgroundLayout.setBackground(new BitmapDrawable((Resources) null, HwMagicWindowUI.this.mController.getBmpGauss()));
                    HwMagicWindowUI.this.mBackgroundView.setBackgroundColor(Color.parseColor(HwMagicWindowUI.BLACK_COLOR));
                    float f = 1.0f;
                    if (HwMagicWindowUI.this.mContainer.isFoldableDevice()) {
                        HwMagicWindowUI.this.mBackgroundView.setAlpha(1.0f);
                        return;
                    }
                    ImageView imageView = HwMagicWindowUI.this.mBackgroundView;
                    if (HwMagicWindowUI.this.mIsMiddle) {
                        f = HwMagicWindowUI.TRANSPARENCY_LIGHT;
                    }
                    imageView.setAlpha(f);
                    return;
                }
                HwMagicWindowUI.this.setBgColor();
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animation) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationRepeat(Animator animation) {
            }
        };
        this.mBackgroundExitAnimatorListener = new Animator.AnimatorListener() {
            /* class com.huawei.server.magicwin.HwMagicWindowUI.AnonymousClass2 */

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animation) {
                if (HwMagicWindowUI.this.mHwMultiWindowSplitUI != null && !HwMagicWindowUI.this.mMwManager.getAmsPolicy().isShowDragBar(HwMagicWindowUI.this.mContainer)) {
                    SlogEx.i(HwMagicWindowUI.TAG, "wallpaper changed, will remove split bar, display id =" + HwMagicWindowUI.this.mDisplayId);
                    HwMagicWindowUI.this.mController.getUiHandler().removeMessages(3);
                    HwMagicWindowUI.this.mController.getUiHandler().removeMessages(5);
                    HwMagicWindowUI.this.mHwMultiWindowSplitUI.removeSplit(103, false);
                }
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animation) {
                if (!HwMagicWindowUI.this.mIsEnterMwWallpaperAnimating) {
                    HwMagicWindowUI.this.mBackgroundLayout.setVisibility(4);
                }
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animation) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationRepeat(Animator animation) {
            }
        };
        this.mController = controller;
        this.mMwManager = manager;
        this.mContainer = container;
        if (context != null) {
            this.mContext = context;
            this.mDisplayId = container.getDisplayId();
            initDisplayMetrics();
            initHomeGestureObserver();
        }
    }

    private void initDisplayMetrics() {
        HwMagicContainer hwMagicContainer = this.mContainer;
        if (hwMagicContainer != null) {
            this.mDisplayMetrics = hwMagicContainer.getDisplayMetrics();
            this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        }
    }

    public void updateDragViewVisibility() {
        if (this.mHwMultiWindowSplitUI == null) {
            SlogEx.w(TAG, "update Visibility, SplitUI is null, display id =" + this.mDisplayId);
        } else if (shouldAddSplitBar()) {
            SlogEx.i(TAG, "update Visibility, add split bar, display id =" + this.mDisplayId);
            this.mHwMultiWindowSplitUI.addDividerBarWindow(103);
        } else if (!this.mMwManager.getAmsPolicy().isInHwDoubleWindow(this.mContainer) && !this.mMwManager.getAmsPolicy().isShowDragBar(this.mContainer)) {
            SlogEx.i(TAG, "update Visibility, remove split bar, display id =" + this.mDisplayId);
            this.mHwMultiWindowSplitUI.removeSplit(103, false);
        }
    }

    public void forceUpdateSplitBar(boolean isVisible) {
        if (this.mHwMultiWindowSplitUI == null) {
            SlogEx.w(TAG, "force update, SplitUI is null, display id =" + this.mDisplayId);
        } else if (!isVisible) {
            SlogEx.i(TAG, "force update, remove split bar, display id =" + this.mDisplayId);
            this.mHwMultiWindowSplitUI.removeSplit(103, false);
        } else if (shouldAddSplitBar()) {
            SlogEx.i(TAG, "force update, add split bar, display id =" + this.mDisplayId);
            this.mHwMultiWindowSplitUI.addDividerBarWindow(103);
        }
    }

    private boolean shouldAddSplitBar() {
        if (!HwPCUtils.enabledInPad() || !HwPCUtils.isPcCastModeInServer()) {
            if (!this.mContainer.getConfig().isDragable(this.mMwManager.getAmsPolicy().getFocusedStackPackageName(this.mContainer)) || (!this.mMwManager.getAmsPolicy().isInHwDoubleWindow(this.mContainer) && !this.mMwManager.getAmsPolicy().isShowDragBar(this.mContainer))) {
                return false;
            }
            return true;
        }
        SlogEx.i(TAG, "no need add split bar in Pad PC mode.");
        return false;
    }

    private ActivityRecordEx getActivityByPosition(HwMagicContainer container, int position) {
        return this.mMwManager.getAmsPolicy().getActivityByPosition(container, position);
    }

    public void changeMagicWindowWallpaper(int wallpaperFlags) {
        boolean z = true;
        boolean isMiddle = (wallpaperFlags & 2) != 0;
        boolean changeVisible = (wallpaperFlags & 1) != 0;
        boolean isRequestVisible = (wallpaperFlags & 32) != 0;
        if (changeVisible && this.mVisible != isRequestVisible) {
            SlogEx.d(TAG, "changeMagicWindowWallpaper abandon, isRequestVisible=" + isRequestVisible + " mVisible=" + this.mVisible);
        } else if (!isSupportBackground()) {
            changeDragViewWithoutBg(changeVisible);
        } else {
            this.mIsMiddle = isMiddle;
            if (this.mBackgroundView == null) {
                createMagicWindowBg();
            }
            if (this.mIsNeedUpdateWallPaperSize) {
                SlogEx.i(TAG, "need change magic window wallpaper size");
                updateWallpaperSize();
                this.mIsNeedUpdateWallPaperSize = false;
            }
            if ((changeVisible || !this.mVisible) && this.mBackgroundView != null) {
                if (this.mVisible) {
                    if (getActivityByPosition(this.mContainer, 2) != null || this.mMwManager.getAmsPolicy().isShowDragBar(this.mContainer)) {
                        z = false;
                    }
                    this.mIsMiddle = z;
                }
                SlogEx.i(TAG, "changeMagicWindowWallpaper vis=" + this.mVisible + ", display id =" + this.mDisplayId + ", mid=" + this.mIsMiddle + " wallpaperFlags=" + wallpaperFlags);
                startBackgroundVisibleAnimation(this.mVisible, this.mIsMiddle, wallpaperFlags);
                return;
            }
            SlogEx.i(TAG, "startAlphaChangeAnimator, isMiddle =" + isMiddle);
            if (this.mContainer.isFoldableDevice()) {
                return;
            }
            if (isMiddle) {
                startAlphaChangeAnimator(1.0f, TRANSPARENCY_LIGHT, DURATION_DOUBLE_TO_SINGLE);
            } else {
                startAlphaChangeAnimator(TRANSPARENCY_LIGHT, 1.0f, DURATION_SINGLE_TO_DOUBLE);
            }
        }
    }

    private void changeDragViewWithoutBg(boolean changeVisible) {
        if (!changeVisible && this.mVisible) {
            return;
        }
        if (!this.mVisible) {
            HwMultiWindowSplitUI hwMultiWindowSplitUI = this.mHwMultiWindowSplitUI;
            if (hwMultiWindowSplitUI != null) {
                hwMultiWindowSplitUI.removeSplit(103, false);
            }
        } else if (!isNeedChangeBgColor()) {
            Message msg = this.mController.getUiHandler().obtainMessage(3);
            msg.arg2 = this.mDisplayId;
            this.mController.getUiHandler().sendMessage(msg);
        }
    }

    private void startAlphaChangeAnimator(float from, float to, long time) {
        if (this.mBackgroundView != null) {
            ObjectAnimator objectAnimator = this.alphaAnimator;
            if (objectAnimator != null && objectAnimator.isRunning()) {
                this.alphaAnimator.end();
            }
            this.alphaAnimator = ObjectAnimator.ofFloat(this.mBackgroundView, "alpha", from, to);
            this.alphaAnimator.setDuration(time);
            this.alphaAnimator.start();
        }
    }

    private void startBackgroundVisibleAnimation(boolean visible, boolean isMiddle, int wallpaperFlags) {
        boolean isGestureBackHome = (wallpaperFlags & 4) != 0;
        ObjectAnimator objectAnimator = this.animator;
        if (objectAnimator != null && objectAnimator.isRunning()) {
            SlogEx.i(TAG, "startBackgroundVisibleAnimation, disable animating animation");
            this.animator.end();
        }
        if (visible) {
            this.mIsEnterMwWallpaperAnimating = true;
            this.mBackgroundLayout.setVisibility(0);
            this.animator = ObjectAnimator.ofFloat(this.mBackgroundView, "alpha", 0.0f, 1.0f);
            this.mBackgroundLayout.setBackground(new BitmapDrawable((Resources) null, this.mController.getBmpGauss()));
            if (isNeedChangeBgColor()) {
                this.mBackgroundView.setBackground(new BitmapDrawable((Resources) null, getBlurAndBlackWallpaper(this.mController.getBmpGauss(), getSplitBgColor())));
                this.animator.setInterpolator(new AccelerateInterpolator());
            } else {
                changeBackGroundColorIfNeed(isMiddle, true);
            }
            this.mBackgroundView.setAlpha(1.0f);
            this.animator.addListener(this.mBackgroundEnterAnimatorListener);
        } else {
            String focusPackageName = this.mMwManager.getAmsPolicy().getFocusedStackPackageName(this.mContainer);
            if (!HwMwUtils.IS_FOLD_SCREEN_DEVICE || ((focusPackageName == null || focusPackageName.indexOf(LAUNCHER_PACKAGE_NAME) < 0) && !isGestureBackHome)) {
                this.animator = ObjectAnimator.ofFloat(this.mBackgroundView, "alpha", 1.0f, 0.0f);
                this.mBackgroundLayout.setBackground(null);
                changeBackGroundColorIfNeed(isMiddle, false);
                this.mBackgroundView.setAlpha(1.0f);
                this.animator.addListener(this.mBackgroundExitAnimatorListener);
            } else {
                this.mBackgroundLayout.setBackground(null);
                if (isNeedChangeBgColor()) {
                    this.mBackgroundView.setAlpha(1.0f);
                } else {
                    this.mBackgroundView.setAlpha(0.0f);
                }
                this.mBackgroundLayout.setVisibility(4);
                if (this.mHwMultiWindowSplitUI != null) {
                    SlogEx.i(TAG, "fold wallpaper changed, will remove split bar, display id =" + this.mDisplayId);
                    this.mController.getUiHandler().removeMessages(3);
                    this.mController.getUiHandler().removeMessages(5);
                    this.mHwMultiWindowSplitUI.removeSplit(103, false);
                    return;
                }
                return;
            }
        }
        this.animator.setDuration(getBgVisibleAnimatorDuration(visible, wallpaperFlags));
        this.animator.start();
    }

    private long getBgVisibleAnimatorDuration(boolean visible, int wallpaperFlags) {
        long duration;
        boolean isTahDelayAnimation = true;
        boolean isDisableAnim = (wallpaperFlags & 8) != 0;
        if ((wallpaperFlags & 16) == 0) {
            isTahDelayAnimation = false;
        }
        if (isDisableAnim) {
            return 0;
        }
        if (visible) {
            duration = isTahDelayAnimation ? DURATION_SINGLE_TO_DOUBLE : 400;
            if (this.mContainer.isFoldableDevice() && isNeedChangeBgColor()) {
                duration = 150;
            }
        } else {
            duration = 0;
        }
        SlogEx.i(TAG, "visible=" + visible + " bg animation duration=" + duration);
        return duration;
    }

    private void changeBackGroundColorIfNeed(boolean isMiddle, boolean isForceToBlack) {
        int color;
        if (!isNeedChangeBgColor() || isForceToBlack) {
            if (this.mContainer.isFoldableDevice()) {
                color = BLACK_TRANSPARENCY_DARK;
            } else {
                color = isMiddle ? BLACK_TRANSPARENCY_LIGHT : BLACK_TRANSPARENCY_DARK;
            }
            this.mBackgroundView.setBackground(new BitmapDrawable((Resources) null, getBlurAndBlackWallpaper(this.mController.getBmpGauss(), color)));
            return;
        }
        setBgColor();
    }

    private void updateWallpaperSize() {
        WindowManager.LayoutParams layoutParams;
        FrameLayout frameLayout = this.mBackgroundLayout;
        if (frameLayout != null && (layoutParams = (WindowManager.LayoutParams) frameLayout.getLayoutParams()) != null) {
            layoutParams.width = this.mDisplayMetrics.widthPixels;
            layoutParams.height = this.mDisplayMetrics.heightPixels;
            this.mWindowManager.updateViewLayout(this.mBackgroundLayout, layoutParams);
        }
    }

    private void createMagicWindowBg() {
        if (isSupportBackground()) {
            SlogEx.i(TAG, "Create MagicWindow Bg with display id = " + this.mDisplayId);
            View inflateView = LayoutInflater.from(this.mContext).inflate(HwPartResourceUtils.getResourceId("magic_window_bg"), (ViewGroup) null);
            if (inflateView instanceof FrameLayout) {
                this.mBackgroundLayout = (FrameLayout) inflateView;
            }
            if (this.mBackgroundLayout == null) {
                SlogEx.w(TAG, "mBackgroundLayout = null");
                return;
            }
            if (this.mContainer.isFoldableDevice()) {
                this.mDisplayMetrics = this.mContainer.getDisplayMetrics();
            }
            this.mBackgroundLayout.setVisibility(this.mVisible ? 0 : 4);
            WindowManager.LayoutParams mLp = new WindowManager.LayoutParams();
            mLp.setTitle("MagicWindow");
            mLp.type = 2103;
            int width = this.mDisplayMetrics.widthPixels;
            int height = this.mDisplayMetrics.heightPixels;
            if (!this.mContainer.isFoldableDevice() && height > width) {
                width = this.mDisplayMetrics.heightPixels;
                height = this.mDisplayMetrics.widthPixels;
            }
            SlogEx.i(TAG, "createMagicWindowBg:w= " + width + ", h=" + height + ", did=" + this.mDisplayId);
            mLp.width = width;
            mLp.height = height;
            mLp.flags = 264;
            mLp.format = -3;
            LayoutParamsExt.orPrivateFlags(mLp, 80);
            mLp.gravity = 8388659;
            this.mBackgroundLayout.setSystemUiVisibility(5894);
            this.mWindowManager.addView(this.mBackgroundLayout, mLp);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(-1, -1);
            this.mBackgroundView = new ImageView(this.mContext);
            this.mBackgroundLayout.addView(this.mBackgroundView, lp);
        }
    }

    public void updateSplitBarVisibility(boolean isVisible) {
        updateSplitBarVisibility(isVisible, isVisible);
    }

    public void updateSplitBarVisibility(boolean isVisible, boolean isNeedDelayed) {
        Message msg = this.mController.getUiHandler().obtainMessage(5);
        msg.obj = Boolean.valueOf(isVisible);
        msg.arg2 = this.mDisplayId;
        this.mController.getUiHandler().removeMessages(5);
        this.mController.getUiHandler().sendMessageDelayed(msg, isNeedDelayed ? 200 : 0);
    }

    public void changeWallpaper(boolean isMiddle) {
        SlogEx.i(TAG, "changeWallpaper, isMiddle=" + isMiddle + ", mIsMiddle=" + this.mIsMiddle);
        if (isMiddle != this.mIsMiddle) {
            Message msg = this.mController.getUiHandler().obtainMessage(1);
            msg.obj = Boolean.valueOf(isMiddle);
            msg.arg2 = this.mDisplayId;
            this.mController.getUiHandler().removeMessages(1);
            this.mController.getUiHandler().sendMessage(msg);
        } else if (isMiddle) {
            updateSplitBarVisibility(false);
        }
    }

    public void onUserSwitch() {
        if (!this.mContainer.isVirtualContainer() && isSupportBackground()) {
            registerGestureHomeAnimatorObserver();
        }
    }

    public boolean isSupportBackground() {
        return this.mContainer.getConfig().isSystemSupport(2);
    }

    public void registerGestureHomeAnimatorObserver() {
        this.mContext.getContentResolver().unregisterContentObserver(this.mGestureHomeAnimatorObserver);
        if (SettingsEx.Secure.getIntForUser(this.mContext.getContentResolver(), GESTURE_HOME_ANIMATOR, 0, ActivityManagerEx.getCurrentUser()) == 1) {
            SettingsEx.Secure.putIntForUser(this.mContext.getContentResolver(), GESTURE_HOME_ANIMATOR, 0, ActivityManagerEx.getCurrentUser());
        }
        ContentResolverExt.registerContentObserver(this.mContext.getContentResolver(), Settings.Secure.getUriFor(GESTURE_HOME_ANIMATOR), false, this.mGestureHomeAnimatorObserver, ActivityManagerEx.getCurrentUser());
    }

    private int getSplitBgColor() {
        String pkgName = this.mMwManager.getAmsPolicy().getFocusedStackPackageName(this.mContainer);
        if (this.mBackgroundView == null || !this.mContainer.getConfig().isSupportAppTaskSplitScreen(pkgName)) {
            return -1;
        }
        if (this.mMwManager.getAmsPolicy().isInAppSplitWinMode(this.mMwManager.getAmsPolicy().getTopActivity(this.mContainer))) {
            SlogEx.d(TAG, "update wallpaper for split window");
            return this.mContainer.getConfig().getSplitLineBgColor(pkgName);
        }
        SlogEx.d(TAG, "update wallpaper for normal magic");
        return this.mContainer.getConfig().getSplitBarBgColor(pkgName);
    }

    public void setBgColor() {
        int setColor = getSplitBgColor();
        if (setColor != -1 && !this.mIsEnterMwWallpaperAnimating) {
            this.mBackgroundView.setBackground(new BitmapDrawable((Resources) null, getBlurAndBlackWallpaper(this.mController.getBmpGauss(), setColor)));
            this.mBackgroundView.setAlpha(1.0f);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isNeedChangeBgColor() {
        String focusPackageName = this.mMwManager.getAmsPolicy().getFocusedStackPackageName(this.mContainer);
        int splitLineBgColor = this.mContainer.getConfig().getSplitLineBgColor(focusPackageName);
        int splitBarBgColor = this.mContainer.getConfig().getSplitBarBgColor(focusPackageName);
        if ((splitLineBgColor == -1 && splitBarBgColor == -1) || !this.mContainer.getConfig().isSupportAppTaskSplitScreen(focusPackageName)) {
            return false;
        }
        SlogEx.d(TAG, "isNeedChangeBgColor for special cast");
        return true;
    }

    private void initHomeGestureObserver() {
        this.mGestureHomeAnimatorObserver = new ContentObserver(this.mController.getUiHandler()) {
            /* class com.huawei.server.magicwin.HwMagicWindowUI.AnonymousClass3 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                boolean isTopAPPInMwMode = HwMagicWindowUI.this.mMwManager.getAmsPolicy().isStackInHwMagicWindowMode(HwMagicWindowUI.this.mContainer);
                int animStatus = SettingsEx.Secure.getIntForUser(HwMagicWindowUI.this.mContext.getContentResolver(), HwMagicWindowUI.GESTURE_HOME_ANIMATOR, 0, ActivityManagerEx.getCurrentUser());
                if (!isTopAPPInMwMode) {
                    SlogEx.i(HwMagicWindowUI.TAG, "GESTURE_HOME_ANIMATOR onChange return, isTopAPPInMwMode=" + isTopAPPInMwMode + " animStatus=" + animStatus + " mVisible=" + HwMagicWindowUI.this.mVisible);
                    return;
                }
                HwMagicWindowUI.this.handleGestureHomeAnimatorChange(animStatus);
            }
        };
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleGestureHomeAnimatorChange(int animStatus) {
        SlogEx.i(TAG, "handleGestureHomeAnimatorChanged animStatus=" + animStatus + " mVisible=" + this.mVisible);
        if (animStatus != -1) {
            if (animStatus != 1) {
                switch (animStatus) {
                    case 11:
                    case 12:
                    case 13:
                        break;
                    case 14:
                    case 16:
                    default:
                        return;
                    case 15:
                    case 17:
                        break;
                }
            }
            if (this.mVisible) {
                this.mVisible = false;
                sendUpdateWallpaperMessage(this.mDisplayId, true, true, false);
                return;
            }
            return;
        }
        boolean isFullscreen = this.mMwManager.getAmsPolicy().isFullscreenWindow(this.mContainer);
        if (!this.mVisible && !isFullscreen) {
            this.mVisible = true;
            sendUpdateWallpaperMessage(this.mDisplayId, false, true, false);
        }
    }

    public void hideMwWallpaperInNeed() {
        FrameLayout frameLayout = this.mBackgroundLayout;
        if (frameLayout != null && frameLayout.getVisibility() == 0) {
            this.mVisible = true;
            SlogEx.i(TAG, "hideMwWallpaperInNeed display id =" + this.mDisplayId);
            updateMwWallpaperVisibility(false, this.mDisplayId, false);
        }
    }

    private void showWallpaper4Tah(int displayId) {
        if (!this.mVisible) {
            if (this.mController.getBmpGauss() == null) {
                SlogEx.w(TAG, "showWallpaper4Tah(), bmpGauss = null, display id = " + displayId);
                this.mMwManager.getHandler().sendEmptyMessage(1);
            }
            this.mVisible = true;
            SlogEx.i(TAG, "showWallpaper4Tah to visible, display id =" + displayId);
            sendUpdateWallpaperMessage(displayId, false, false, true);
        }
    }

    public void updateMwWallpaperVisibility(boolean isVisible, int displayId, boolean isDisableAnim) {
        if (this.mVisible != isVisible) {
            if (this.mController.getBmpGauss() == null) {
                SlogEx.w(TAG, "updateMwWallpaperVisibility(), bmpGauss = null, display id =" + displayId);
                this.mMwManager.getHandler().sendEmptyMessage(1);
            }
            this.mVisible = isVisible;
            SlogEx.i(TAG, "updateMwWallpaperVisibility, isVisible = " + isVisible + ", display id =" + displayId);
            sendUpdateWallpaperMessage(displayId, false, isDisableAnim, false);
        }
    }

    private void sendUpdateWallpaperMessage(int displayId, boolean isGestureBackHome, boolean isDisableAnim, boolean isTahDelayAnim) {
        int wallpaperFlags = 0 | (this.mIsMiddle ? 2 : 0) | (this.mVisible ? 32 : 0) | (isGestureBackHome ? 4 : 0) | (isDisableAnim ? 8 : 0) | (isTahDelayAnim ? 16 : 0);
        Handler uiHandler = this.mController.getUiHandler();
        Message msg = uiHandler.obtainMessage(0);
        msg.arg1 = wallpaperFlags;
        msg.arg2 = displayId;
        long delayMillis = isTahDelayAnim ? 250 : 0;
        if (this.mVisible && this.mContainer.isFoldableDevice() && isNeedChangeBgColor()) {
            delayMillis = 150;
        }
        uiHandler.sendMessageDelayed(msg, delayMillis);
    }

    public Bitmap getWallpaperScreenShot() {
        int color;
        if (this.mController.getBmpGauss() == null) {
            SlogEx.w(TAG, "getWallpaperScreenShot failed, cause mBmpGauss is null!");
            return null;
        }
        Bitmap wallpaperScreenShot = this.mController.getBmpGauss().copy(Bitmap.Config.ARGB_8888, true);
        if (this.mContainer.isFoldableDevice()) {
            color = BLACK_TRANSPARENCY_DARK;
        } else {
            color = this.mIsMiddle ? BLACK_TRANSPARENCY_LIGHT : BLACK_TRANSPARENCY_DARK;
        }
        return getBlurAndBlackWallpaper(wallpaperScreenShot, color);
    }

    private Bitmap getBlurAndBlackWallpaper(Bitmap bitmap, int color) {
        if (bitmap != null) {
            Bitmap bm = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
            Canvas canvas = new Canvas(bm);
            canvas.drawBitmap(bitmap, 0.0f, 0.0f, (Paint) null);
            canvas.drawColor(color);
            return bm;
        }
        SlogEx.i(TAG, "getBlurAndBlackWallpaper bmp null with display id=" + this.mDisplayId);
        return bitmap;
    }

    public void setIsNeedUpdateWallPaperSize(boolean isNeedUpdateWallPaperSize) {
        this.mIsNeedUpdateWallPaperSize = isNeedUpdateWallPaperSize;
    }

    public void setHwMultiWindowSplitUI(HwMultiWindowSplitUI hwMultiWindowSplitUI) {
        if (hwMultiWindowSplitUI == null) {
            SlogEx.w(TAG, "SplitUI is null, display id=" + this.mDisplayId);
            return;
        }
        this.mHwMultiWindowSplitUI = hwMultiWindowSplitUI;
    }

    public HwMagicContainer getContainer() {
        return this.mContainer;
    }

    public void updateMwWallpaperVisibilityIfNeed(boolean isVisible, int displayId) {
        if (this.mMwManager.getAmsPolicy().isFullscreenWindow(this.mContainer)) {
            SlogEx.i(TAG, "updateMwWallpaperVisibilityIfNeed full screen vis=" + isVisible + ", display id =" + displayId + ", mIsMiddle =" + this.mIsMiddle);
            this.mIsSetWallpaperVisible = false;
        } else if (this.mIsSetWallpaperVisible != isVisible || (!isVisible && this.mVisible)) {
            SlogEx.i(TAG, "updateMwWallpaperVisibilityIfNeed vis=" + isVisible + ", display id =" + displayId + " mVisible=" + this.mVisible);
            this.mIsSetWallpaperVisible = isVisible;
            if (!this.mContainer.isFoldableDevice() || !isVisible) {
                updateMwWallpaperVisibility(isVisible, displayId, false);
            } else {
                showWallpaper4Tah(displayId);
            }
        }
    }
}
