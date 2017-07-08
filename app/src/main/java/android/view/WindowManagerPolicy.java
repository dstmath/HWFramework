package android.view;

import android.content.Context;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.IAodStateCallback;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.view.InputEventReceiver.Factory;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import com.android.internal.policy.IShortcutService;
import java.io.PrintWriter;

public interface WindowManagerPolicy {
    public static final String ACTION_HDMI_PLUGGED = "android.intent.action.HDMI_PLUGGED";
    public static final int ACTION_PASS_TO_USER = 1;
    public static final String EXTRA_FROM_HOME_KEY = "android.intent.extra.FROM_HOME_KEY";
    public static final String EXTRA_HDMI_PLUGGED_STATE = "state";
    public static final int FINISH_LAYOUT_REDO_ANIM = 8;
    public static final int FINISH_LAYOUT_REDO_CONFIG = 2;
    public static final int FINISH_LAYOUT_REDO_LAYOUT = 1;
    public static final int FINISH_LAYOUT_REDO_WALLPAPER = 4;
    public static final int FLAG_DISABLE_KEY_REPEAT = 134217728;
    public static final int FLAG_FILTERED = 67108864;
    public static final int FLAG_INJECTED = 16777216;
    public static final int FLAG_INTERACTIVE = 536870912;
    public static final int FLAG_PASS_TO_USER = 1073741824;
    public static final int FLAG_TRUSTED = 33554432;
    public static final int FLAG_VIRTUAL = 2;
    public static final int FLAG_WAKE = 1;
    public static final int KEYGUARD_GOING_AWAY_FLAG_NO_WINDOW_ANIMATIONS = 2;
    public static final int KEYGUARD_GOING_AWAY_FLAG_TO_SHADE = 1;
    public static final int KEYGUARD_GOING_AWAY_FLAG_WITH_WALLPAPER = 4;
    public static final int OFF_BECAUSE_OF_ADMIN = 1;
    public static final int OFF_BECAUSE_OF_PHONE_CALL = 7;
    public static final int OFF_BECAUSE_OF_PROX_SENSOR = 6;
    public static final int OFF_BECAUSE_OF_TIMEOUT = 3;
    public static final int OFF_BECAUSE_OF_USER = 2;
    public static final int POLICY_FLAG_REMOVE_HANDYMODE = Integer.MIN_VALUE;
    public static final int PRESENCE_EXTERNAL = 2;
    public static final int PRESENCE_INTERNAL = 1;
    public static final int TRANSIT_ENTER = 1;
    public static final int TRANSIT_EXIT = 2;
    public static final int TRANSIT_HIDE = 4;
    public static final int TRANSIT_PREVIEW_DONE = 5;
    public static final int TRANSIT_SHOW = 3;
    public static final int USER_ROTATION_FREE = 0;
    public static final int USER_ROTATION_LOCKED = 1;
    public static final boolean WATCH_POINTER = false;

    public interface InputConsumer {
        void dismiss();
    }

    public interface KeyguardDismissDoneListener {
        void onKeyguardDismissDone();
    }

    public interface OnKeyguardExitResult {
        void onKeyguardExitResult(boolean z);
    }

    public interface PointerEventListener {
        void onPointerEvent(MotionEvent motionEvent);
    }

    public interface ScreenOnListener {
        void onScreenOn();
    }

    public interface WindowManagerFuncs {
        public static final int CAMERA_LENS_COVERED = 1;
        public static final int CAMERA_LENS_COVER_ABSENT = -1;
        public static final int CAMERA_LENS_UNCOVERED = 0;
        public static final int LID_ABSENT = -1;
        public static final int LID_CLOSED = 0;
        public static final int LID_OPEN = 1;

        InputConsumer addInputConsumer(Looper looper, Factory factory);

        int getCameraLensCoverState();

        int getDockedDividerInsetsLw();

        int getLidState();

        void getStackBounds(int i, Rect rect);

        Object getWindowManagerLock();

        void lockDeviceNow();

        void rebootSafeMode(boolean z);

        void reevaluateStatusBarSize(boolean z);

        void reevaluateStatusBarVisibility();

        void registerPointerEventListener(PointerEventListener pointerEventListener);

        void shutdown(boolean z);

        void switchInputMethod(boolean z);

        void unregisterPointerEventListener(PointerEventListener pointerEventListener);
    }

    public interface WindowState {
        boolean canCarryColors();

        void computeFrameLw(Rect rect, Rect rect2, Rect rect3, Rect rect4, Rect rect5, Rect rect6, Rect rect7, Rect rect8);

        IApplicationToken getAppToken();

        LayoutParams getAttrs();

        int getBaseType();

        Rect getContentFrameLw();

        Rect getDisplayFrameLw();

        Rect getFrameLw();

        Rect getGivenContentInsetsLw();

        boolean getGivenInsetsPendingLw();

        Rect getGivenVisibleInsetsLw();

        boolean getNeedsMenuLw(WindowState windowState);

        Rect getOverscanFrameLw();

        String getOwningPackage();

        int getOwningUid();

        Point getShownPositionLw();

        int getStackId();

        int getSurfaceLayer();

        int getSystemUiVisibility();

        Rect getVisibleFrameLw();

        boolean hasAppShownWindows();

        boolean hasDrawnLw();

        boolean hideLw(boolean z);

        boolean isAlive();

        boolean isAnimatingLw();

        boolean isDefaultDisplay();

        boolean isDimming();

        boolean isDisplayedLw();

        boolean isDrawnLw();

        boolean isGoneForLayoutLw();

        boolean isInMultiWindowMode();

        boolean isVisibleLw();

        boolean isVisibleOrBehindKeyguardLw();

        boolean isVoiceInteraction();

        void setCanCarryColors(boolean z);

        boolean showLw(boolean z);
    }

    View addStartingWindow(IBinder iBinder, String str, int i, CompatibilityInfo compatibilityInfo, CharSequence charSequence, int i2, int i3, int i4, int i5, Configuration configuration);

    void adjustConfigurationLw(Configuration configuration, int i, int i2);

    int adjustSystemUiVisibilityLw(int i);

    void adjustWindowParamsLw(LayoutParams layoutParams);

    boolean allowAppAnimationsLw();

    void applyPostLayoutPolicyLw(WindowState windowState, LayoutParams layoutParams, WindowState windowState2);

    void beginLayoutLw(boolean z, int i, int i2, int i3, int i4);

    void beginPostLayoutPolicyLw(int i, int i2);

    boolean canBeForceHidden(WindowState windowState, LayoutParams layoutParams);

    boolean canMagnifyWindow(int i);

    void cancelWaitKeyguardDismissDone();

    int checkAddPermission(LayoutParams layoutParams, int[] iArr);

    boolean checkShowToOwnerOnly(LayoutParams layoutParams);

    Animation createForceHideEnterAnimation(boolean z, boolean z2);

    Animation createForceHideWallpaperExitAnimation(boolean z);

    void dismissKeyguardLw();

    KeyEvent dispatchUnhandledKey(WindowState windowState, KeyEvent keyEvent, int i);

    void dump(String str, PrintWriter printWriter, String[] strArr);

    void enableKeyguard(boolean z);

    void enableScreenAfterBoot();

    void exitKeyguardSecurely(OnKeyguardExitResult onKeyguardExitResult);

    void finishLayoutLw();

    int finishPostLayoutPolicyLw();

    void finishedGoingToSleep(int i);

    void finishedWakingUp();

    int focusChangedLw(WindowState windowState, WindowState windowState2);

    int getConfigDisplayHeight(int i, int i2, int i3, int i4);

    int getConfigDisplayWidth(int i, int i2, int i3, int i4);

    void getContentRectLw(Rect rect);

    int getInputMethodWindowVisibleHeightLw();

    boolean getInsetHintLw(LayoutParams layoutParams, Rect rect, int i, int i2, int i3, Rect rect2, Rect rect3, Rect rect4);

    boolean getInterceptInputForWaitBrightness();

    int getMaxWallpaperLayer();

    int getNonDecorDisplayHeight(int i, int i2, int i3, int i4);

    int getNonDecorDisplayWidth(int i, int i2, int i3, int i4);

    void getNonDecorInsetsLw(int i, int i2, int i3, Rect rect);

    void getStableInsetsLw(int i, int i2, int i3, Rect rect);

    int getSystemDecorLayerLw();

    int getUserRotationMode();

    WindowState getWinShowWhenLockedLw();

    boolean hasNavigationBar();

    void hideBootMessages();

    boolean inKeyguardRestrictedKeyInputMode();

    void init(Context context, IWindowManager iWindowManager, WindowManagerFuncs windowManagerFuncs);

    long interceptKeyBeforeDispatching(WindowState windowState, KeyEvent keyEvent, int i);

    int interceptKeyBeforeQueueing(KeyEvent keyEvent, int i);

    int interceptMotionBeforeQueueingNonInteractive(long j, int i);

    boolean isDefaultOrientationForced();

    boolean isDockSideAllowed(int i);

    boolean isForceHiding(LayoutParams layoutParams);

    boolean isKeyguardDrawnLw();

    boolean isKeyguardHostWindow(LayoutParams layoutParams);

    boolean isKeyguardLocked();

    boolean isKeyguardSecure(int i);

    boolean isKeyguardShowingOrOccluded();

    boolean isNavBarForcedShownLw(WindowState windowState);

    boolean isScreenOn();

    boolean isStatusBarKeyguardShowing();

    boolean isTopIsFullscreen();

    boolean isTopLevelWindow(int i);

    void keepScreenOnStartedLw();

    void keepScreenOnStoppedLw();

    void layoutWindowLw(WindowState windowState, WindowState windowState2);

    void lockNow(Bundle bundle);

    void notifyActivityDrawnForKeyguardLw();

    void notifyCameraLensCoverSwitchChanged(long j, boolean z);

    void notifyLidSwitchChanged(long j, boolean z);

    void onConfigurationChanged();

    boolean performHapticFeedbackLw(WindowState windowState, int i, boolean z);

    int prepareAddWindowLw(WindowState windowState, LayoutParams layoutParams);

    void regeditAodStateCallback(IAodStateCallback iAodStateCallback);

    void registerShortcutKey(long j, IShortcutService iShortcutService) throws RemoteException;

    void removeStartingWindow(IBinder iBinder, View view);

    void removeWindowLw(WindowState windowState);

    int rotationForOrientationLw(int i, int i2);

    boolean rotationHasCompatibleMetricsLw(int i, int i2);

    void screenTurnedOff();

    void screenTurnedOn();

    void screenTurningOn(ScreenOnListener screenOnListener);

    int selectAnimationLw(WindowState windowState, int i);

    void selectRotationAnimationLw(int[] iArr);

    void setCurrentOrientationLw(int i);

    void setCurrentUserLw(int i);

    void setDisplayOverscan(Display display, int i, int i2, int i3, int i4);

    void setInitialDisplaySize(Display display, int i, int i2, int i3);

    void setInputMethodWindowVisible(boolean z);

    void setInterceptInputForWaitBrightness(boolean z);

    void setLastInputMethodWindowLw(WindowState windowState, WindowState windowState2);

    void setNaviBarFlag(boolean z);

    void setRotationLw(int i);

    void setSafeMode(boolean z);

    void setUserRotationMode(int i, int i2);

    void showBootMessage(CharSequence charSequence, boolean z);

    void showGlobalActions();

    void showRecentApps(boolean z);

    void startKeyguardExitAnimation(long j, long j2);

    void startedGoingToSleep(int i);

    void startedWakingUp();

    int subWindowTypeToLayerLw(int i);

    void swipFromTop();

    void systemBooted();

    void systemReady();

    void unregeditAodStateCallback(IAodStateCallback iAodStateCallback);

    void updateNavigationBar(boolean z);

    void updateSystemUiColorLw(WindowState windowState);

    void userActivity();

    boolean validateRotationAnimationLw(int i, int i2, boolean z);

    void waitKeyguardDismissDone(KeyguardDismissDoneListener keyguardDismissDoneListener);

    int windowTypeToLayerLw(int i);
}
