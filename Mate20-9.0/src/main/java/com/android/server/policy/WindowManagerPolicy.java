package com.android.server.policy;

import android.content.Context;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.IAodStateCallback;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import android.view.Display;
import android.view.DisplayCutout;
import android.view.IApplicationToken;
import android.view.IHwRotateObserver;
import android.view.IWindowManager;
import android.view.InputEventReceiver;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.WindowManagerPolicyConstants;
import android.view.animation.Animation;
import com.android.internal.policy.IKeyguardDismissCallback;
import com.android.internal.policy.IShortcutService;
import com.android.server.connectivity.NetworkAgentInfo;
import com.android.server.power.IHwShutdownThread;
import com.android.server.wm.DisplayFrames;
import com.android.server.wm.utils.WmDisplayCutout;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface WindowManagerPolicy extends WindowManagerPolicyConstants {
    public static final int ACTION_PASS_TO_USER = 1;
    public static final int FINISH_LAYOUT_REDO_ANIM = 8;
    public static final int FINISH_LAYOUT_REDO_CONFIG = 2;
    public static final int FINISH_LAYOUT_REDO_LAYOUT = 1;
    public static final int FINISH_LAYOUT_REDO_WALLPAPER = 4;
    public static final int TRANSIT_ENTER = 1;
    public static final int TRANSIT_EXIT = 2;
    public static final int TRANSIT_HIDE = 4;
    public static final int TRANSIT_PREVIEW_DONE = 5;
    public static final int TRANSIT_SHOW = 3;
    public static final int USER_ROTATION_FREE = 0;
    public static final int USER_ROTATION_LOCKED = 1;

    public interface InputConsumer {
        void dismiss();
    }

    public interface KeyguardDismissDoneListener {
        void onKeyguardDismissDone();
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface NavigationBarPosition {
    }

    public interface OnKeyguardExitResult {
        void onKeyguardExitResult(boolean z);
    }

    public interface ScreenOffListener {
        void onScreenOff();
    }

    public interface ScreenOnListener {
        void onScreenOn();
    }

    public interface StartingSurface {
        void remove();
    }

    public interface TpKeepListener {
        void setTpKeep(boolean z);
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface UserRotationMode {
    }

    public interface WindowManagerFuncs {
        public static final int CAMERA_LENS_COVERED = 1;
        public static final int CAMERA_LENS_COVER_ABSENT = -1;
        public static final int CAMERA_LENS_UNCOVERED = 0;
        public static final int LID_ABSENT = -1;
        public static final int LID_CLOSED = 0;
        public static final int LID_OPEN = 1;

        InputConsumer createInputConsumer(Looper looper, String str, InputEventReceiver.Factory factory);

        int getCameraLensCoverState();

        int getDockedDividerInsetsLw();

        WindowState getInputMethodWindowLw();

        int getLidState();

        void getStackBounds(int i, int i2, Rect rect);

        Object getWindowManagerLock();

        void lockDeviceNow();

        void notifyKeyguardTrustedChanged();

        void notifyShowingDreamChanged();

        void onKeyguardShowingAndNotOccludedChanged();

        void reboot(boolean z);

        void rebootSafeMode(boolean z);

        void reevaluateStatusBarSize(boolean z);

        void reevaluateStatusBarVisibility();

        void registerExternalPointerEventListener(WindowManagerPolicyConstants.PointerEventListener pointerEventListener);

        void registerPointerEventListener(WindowManagerPolicyConstants.PointerEventListener pointerEventListener);

        void screenTurningOff(ScreenOffListener screenOffListener);

        void shutdown(boolean z);

        void switchInputMethod(boolean z);

        void switchKeyboardLayout(int i, int i2);

        void triggerAnimationFailsafe();

        void unregisterExternalPointerEventListener(WindowManagerPolicyConstants.PointerEventListener pointerEventListener);

        void unregisterPointerEventListener(WindowManagerPolicyConstants.PointerEventListener pointerEventListener);

        static String lidStateToString(int lid) {
            switch (lid) {
                case -1:
                    return "LID_ABSENT";
                case 0:
                    return "LID_CLOSED";
                case 1:
                    return "LID_OPEN";
                default:
                    return Integer.toString(lid);
            }
        }

        static String cameraLensStateToString(int lens) {
            switch (lens) {
                case -1:
                    return "CAMERA_LENS_COVER_ABSENT";
                case 0:
                    return "CAMERA_LENS_UNCOVERED";
                case 1:
                    return "CAMERA_LENS_COVERED";
                default:
                    return Integer.toString(lens);
            }
        }
    }

    public interface WindowState {
        boolean canAcquireSleepToken();

        boolean canAffectSystemUiFlags();

        boolean canCarryColors();

        void computeFrameLw(Rect rect, Rect rect2, Rect rect3, Rect rect4, Rect rect5, Rect rect6, Rect rect7, Rect rect8, WmDisplayCutout wmDisplayCutout, boolean z);

        IApplicationToken getAppToken();

        WindowManager.LayoutParams getAttrs();

        int getBaseType();

        Rect getContentFrameLw();

        Rect getDisplayFrameLw();

        int getDisplayId();

        Rect getFrameLw();

        Rect getGivenContentInsetsLw();

        boolean getGivenInsetsPendingLw();

        Rect getGivenVisibleInsetsLw();

        int getHwGestureNavOptions();

        boolean getHwNotchSupport();

        int getLayer();

        boolean getNeedsMenuLw(WindowState windowState);

        Rect getOverscanFrameLw();

        String getOwningPackage();

        int getOwningUid();

        int getRotationAnimationHint();

        int getSurfaceLayer();

        int getSystemUiVisibility();

        Rect getVisibleFrameLw();

        int getWindowingMode();

        boolean hasAppShownWindows();

        @Deprecated
        boolean hasDrawnLw();

        void hideInsetSurfaceOverlayImmediately();

        boolean hideLw(boolean z);

        boolean isAlive();

        boolean isAnimatingLw();

        boolean isDefaultDisplay();

        boolean isDimming();

        boolean isDisplayedLw();

        boolean isDrawnLw();

        boolean isGoneForLayoutLw();

        boolean isImeWithHwFlag();

        boolean isInAboveAppWindows();

        boolean isInMultiWindowMode();

        boolean isInputMethodTarget();

        boolean isInputMethodWindow();

        boolean isVisibleLw();

        boolean isVoiceInteraction();

        boolean isWindowUsingNotch();

        void setCanCarryColors(boolean z);

        void showInsetSurfaceOverlayImmediately();

        boolean showLw(boolean z);

        void writeIdentifierToProto(ProtoOutputStream protoOutputStream, long j);

        boolean isLetterboxedForDisplayCutoutLw() {
            return false;
        }

        boolean isLetterboxedOverlappingWith(Rect rect) {
            return false;
        }

        boolean canAddInternalSystemWindow() {
            return false;
        }
    }

    StartingSurface addSplashScreen(IBinder iBinder, String str, int i, CompatibilityInfo compatibilityInfo, CharSequence charSequence, int i2, int i3, int i4, int i5, Configuration configuration, int i6);

    void adjustConfigurationLw(Configuration configuration, int i, int i2);

    int adjustSystemUiVisibilityLw(int i);

    void adjustWindowParamsLw(WindowState windowState, WindowManager.LayoutParams layoutParams, boolean z);

    boolean allowAppAnimationsLw();

    void applyPostLayoutPolicyLw(WindowState windowState, WindowManager.LayoutParams layoutParams, WindowState windowState2, WindowState windowState3);

    void beginPostLayoutPolicyLw(int i, int i2);

    boolean canBeHiddenByKeyguardLw(WindowState windowState);

    boolean canDismissBootAnimation();

    void cancelWaitKeyguardDismissDone();

    int checkAddPermission(WindowManager.LayoutParams layoutParams, int[] iArr);

    boolean checkShowToOwnerOnly(WindowManager.LayoutParams layoutParams);

    Animation createHiddenByKeyguardExit(boolean z, boolean z2);

    Animation createKeyguardWallpaperExit(boolean z);

    void dismissKeyguardLw(IKeyguardDismissCallback iKeyguardDismissCallback, CharSequence charSequence);

    KeyEvent dispatchUnhandledKey(WindowState windowState, KeyEvent keyEvent, int i);

    void dump(String str, PrintWriter printWriter, String[] strArr);

    void enableKeyguard(boolean z);

    void enableScreenAfterBoot();

    void exitKeyguardSecurely(OnKeyguardExitResult onKeyguardExitResult);

    int finishPostLayoutPolicyLw();

    void finishedGoingToSleep(int i);

    void finishedWakingUp();

    int focusChangedLw(WindowState windowState, WindowState windowState2);

    int getConfigDisplayHeight(int i, int i2, int i3, int i4, int i5, DisplayCutout displayCutout);

    int getConfigDisplayWidth(int i, int i2, int i3, int i4, int i5, DisplayCutout displayCutout);

    int getDefaultNavBarHeight();

    boolean getInterceptInputForWaitBrightness();

    int getMaxWallpaperLayer();

    int getNavBarPosition();

    int getNonDecorDisplayHeight(int i, int i2, int i3, int i4, int i5, DisplayCutout displayCutout);

    int getNonDecorDisplayWidth(int i, int i2, int i3, int i4, int i5, DisplayCutout displayCutout);

    void getNonDecorInsetsLw(int i, int i2, int i3, Rect rect, int i4, DisplayCutout displayCutout);

    void getNonDecorInsetsLw(int i, int i2, int i3, DisplayCutout displayCutout, Rect rect);

    IHwPhoneWindowManagerEx getPhoneWindowManagerEx();

    int getRestrictedScreenHeight();

    void getStableInsetsLw(int i, int i2, int i3, Rect rect, int i4, DisplayCutout displayCutout);

    void getStableInsetsLw(int i, int i2, int i3, DisplayCutout displayCutout, Rect rect);

    int getSystemDecorLayerLw();

    int getUserRotationMode();

    boolean hasNavigationBar();

    void hideBootMessages();

    boolean inKeyguardRestrictedKeyInputMode();

    void init(Context context, IWindowManager iWindowManager, WindowManagerFuncs windowManagerFuncs);

    long interceptKeyBeforeDispatching(WindowState windowState, KeyEvent keyEvent, int i);

    int interceptKeyBeforeQueueing(KeyEvent keyEvent, int i);

    int interceptMotionBeforeQueueingNonInteractive(long j, int i);

    boolean isDefaultOrientationForced();

    boolean isDockSideAllowed(int i, int i2, int i3, int i4, int i5);

    boolean isInputMethodMovedUp();

    boolean isKeyguardDrawnLw();

    boolean isKeyguardHostWindow(WindowManager.LayoutParams layoutParams);

    boolean isKeyguardLocked();

    boolean isKeyguardOccluded();

    boolean isKeyguardSecure(int i);

    boolean isKeyguardShowingAndNotOccluded();

    boolean isKeyguardShowingOrOccluded();

    boolean isKeyguardTrustedLw();

    boolean isNavBarForcedShownLw(WindowState windowState);

    boolean isNavBarVisible();

    boolean isNavigationBarVisible();

    boolean isNotchDisplayDisabled();

    boolean isPendingLock();

    boolean isScreenOn();

    boolean isShowingDreamLw();

    boolean isStatusBarKeyguardShowing();

    boolean isTopIsFullscreen();

    boolean isTopLevelWindow(int i);

    boolean isWindowSupportKnuckle();

    void keepScreenOnStartedLw();

    void keepScreenOnStoppedLw();

    void lockNow(Bundle bundle);

    void notifyCameraLensCoverSwitchChanged(long j, boolean z);

    void notifyLidSwitchChanged(long j, boolean z);

    void notifyRotationChange(int i);

    boolean okToAnimate();

    void onConfigurationChanged();

    void onKeyguardOccludedChangedLw(boolean z);

    void onLockTaskStateChangedLw(int i);

    void onPowerStateChange(int i);

    void onProximityPositive();

    void onSystemUiStarted();

    boolean performHapticFeedbackLw(WindowState windowState, int i, boolean z);

    int prepareAddWindowLw(WindowState windowState, WindowManager.LayoutParams layoutParams);

    void regeditAodStateCallback(IAodStateCallback iAodStateCallback);

    void registerRotateObserver(IHwRotateObserver iHwRotateObserver);

    void registerShortcutKey(long j, IShortcutService iShortcutService) throws RemoteException;

    void removeWindowLw(WindowState windowState);

    void requestUserActivityNotification();

    int rotationForOrientationLw(int i, int i2, boolean z);

    boolean rotationHasCompatibleMetricsLw(int i, int i2);

    void screenTurnedOff();

    void screenTurnedOn();

    void screenTurningOff(ScreenOffListener screenOffListener);

    void screenTurningOn(ScreenOnListener screenOnListener);

    int selectAnimationLw(WindowState windowState, int i);

    void selectRotationAnimationLw(int[] iArr);

    boolean setAodShowing(boolean z);

    void setCurrentOrientationLw(int i);

    void setCurrentUserLw(int i);

    void setDisplayMode(int i);

    void setFullScreenWinVisibile(boolean z);

    void setFullScreenWindow(WindowState windowState);

    void setGestureNavMode(String str, int i, int i2, int i3, int i4);

    void setInitialDisplaySize(Display display, int i, int i2, int i3);

    void setInputMethodWindowVisible(boolean z);

    void setInterceptInputForWaitBrightness(boolean z);

    void setLastInputMethodWindowLw(WindowState windowState, WindowState windowState2);

    void setNavBarVirtualKeyHapticFeedbackEnabledLw(boolean z);

    void setNaviBarFlag(boolean z);

    void setPickUpFlag();

    void setPipVisibilityLw(boolean z);

    void setRecentsVisibilityLw(boolean z);

    void setRotationLw(int i);

    void setSafeMode(boolean z);

    void setSwitchingUser(boolean z);

    void setSyncPowerStateFlag();

    void setTPDozeMode(int i, int i2);

    void setTpKeep(TpKeepListener tpKeepListener);

    void setUserRotationMode(int i, int i2);

    boolean shouldRotateSeamlessly(int i, int i2);

    void showBootMessage(CharSequence charSequence, boolean z);

    void showGlobalActions();

    void showRecentApps();

    void startKeyguardExitAnimation(long j, long j2);

    void startedGoingToSleep(int i);

    void startedWakingUp();

    void swipFromTop();

    void systemBooted();

    void systemReady();

    void unregeditAodStateCallback(IAodStateCallback iAodStateCallback);

    void unregisterRotateObserver(IHwRotateObserver iHwRotateObserver);

    void updateNavigationBar(boolean z);

    void updateSystemUiColorLw(WindowState windowState);

    void userActivity();

    boolean validateRotationAnimationLw(int i, int i2, boolean z);

    void waitKeyguardDismissDone(KeyguardDismissDoneListener keyguardDismissDoneListener);

    void writeToProto(ProtoOutputStream protoOutputStream, long j);

    void onOverlayChangedLw() {
    }

    boolean getLayoutBeyondDisplayCutout() {
        return false;
    }

    int getWindowLayerLw(WindowState win) {
        return getWindowLayerFromTypeLw(win.getBaseType(), win.canAddInternalSystemWindow());
    }

    int getWindowLayerFromTypeLw(int type) {
        if (!WindowManager.LayoutParams.isSystemAlertWindowType(type)) {
            return getWindowLayerFromTypeLw(type, false);
        }
        throw new IllegalArgumentException("Use getWindowLayerFromTypeLw() or getWindowLayerLw() for alert window types");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:76:0x0091, code lost:
        return 4;
     */
    int getWindowLayerFromTypeLw(int type, boolean canAddInternalSystemWindow) {
        if (type >= 1 && type <= 99) {
            return 2;
        }
        if (type != 2103) {
            if (type == 2400) {
                return 3;
            }
            int i = 11;
            int i2 = 10;
            switch (type) {
                case IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME /*2000*/:
                    return 17;
                case 2001:
                    break;
                case 2002:
                    return 3;
                case 2003:
                    if (!canAddInternalSystemWindow) {
                        i = 10;
                    }
                    return i;
                default:
                    switch (type) {
                        case 2005:
                            return 8;
                        case 2006:
                            if (canAddInternalSystemWindow) {
                                i = 22;
                            }
                            return i;
                        case 2007:
                            return 9;
                        case 2008:
                            return 7;
                        case 2009:
                            return 20;
                        case 2010:
                            if (canAddInternalSystemWindow) {
                                i2 = 26;
                            }
                            return i2;
                        case 2011:
                            return 14;
                        case 2012:
                            return 15;
                        case 2013:
                            break;
                        case 2014:
                            return 18;
                        case 2015:
                            return 31;
                        case 2016:
                            return 29;
                        case 2017:
                            return 19;
                        case 2018:
                            return 33;
                        case 2019:
                            return 23;
                        case 2020:
                            return 21;
                        case 2021:
                            return 32;
                        case 2022:
                            return 6;
                        case 2023:
                            return 13;
                        case 2024:
                            return 24;
                        default:
                            switch (type) {
                                case 2026:
                                    return 28;
                                case 2027:
                                    return 27;
                                default:
                                    switch (type) {
                                        case 2030:
                                        case 2037:
                                            return 2;
                                        case 2031:
                                            return 5;
                                        case 2032:
                                            return 30;
                                        case 2033:
                                            break;
                                        case 2034:
                                            return 2;
                                        case 2035:
                                            return 2;
                                        case 2036:
                                            return 25;
                                        case 2038:
                                            return 12;
                                        default:
                                            Slog.e("WindowManager", "Unknown window type: " + type);
                                            return 2;
                                    }
                            }
                    }
            }
        }
        return 1;
    }

    int getSubWindowLayerFromTypeLw(int type) {
        switch (type) {
            case 1000:
            case 1003:
                return 1;
            case NetworkAgentInfo.EVENT_NETWORK_LINGER_COMPLETE:
                return -2;
            case 1002:
                return 2;
            case 1004:
                return -1;
            case 1005:
                return 3;
            default:
                Slog.e("WindowManager", "Unknown sub-window type: " + type);
                return 0;
        }
    }

    void beginLayoutLw(DisplayFrames displayFrames, int uiMode) {
    }

    void layoutWindowLw(WindowState win, WindowState attached, DisplayFrames displayFrames) {
    }

    boolean getLayoutHintLw(WindowManager.LayoutParams attrs, Rect taskBounds, DisplayFrames displayFrames, Rect outFrame, Rect outContentInsets, Rect outStableInsets, Rect outOutsets, DisplayCutout.ParcelableWrapper outDisplayCutout) {
        return false;
    }

    void setDismissImeOnBackKeyPressed(boolean newValue) {
    }

    static String userRotationModeToString(int mode) {
        switch (mode) {
            case 0:
                return "USER_ROTATION_FREE";
            case 1:
                return "USER_ROTATION_LOCKED";
            default:
                return Integer.toString(mode);
        }
    }
}
