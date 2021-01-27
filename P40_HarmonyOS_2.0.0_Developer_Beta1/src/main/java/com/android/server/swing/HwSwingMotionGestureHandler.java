package com.android.server.swing;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.hardware.input.InputManager;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.rms.iaware.AppTypeRecoManager;
import android.text.TextUtils;
import android.util.Slog;
import android.view.Display;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import com.android.server.intellicom.common.SmartDualCardConsts;
import com.android.server.lights.LightsManagerEx;
import com.android.server.multiwin.HwMultiWinConstants;
import com.huawei.android.app.HwMultiWindowEx;
import com.huawei.android.app.WindowManagerEx;
import com.huawei.android.view.inputmethod.InputMethodManagerEx;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import com.huawei.hsm.IHsmMusicWatch;
import com.huawei.hsm.MediaTransactWrapperEx;
import com.huawei.systemserver.swing.IHwSwingEventNotifier;
import java.util.List;
import java.util.Locale;

public class HwSwingMotionGestureHandler {
    private static final String ACTION_SYSTEMUI_SWING_SERVICE = "com.android.systemui.swing.HwSwingService";
    private static final String APS_RESOLUTION_CHANGE_ACTION = "huawei.intent.action.APS_RESOLUTION_CHANGE_ACTION";
    private static final String APS_RESOLUTION_CHANGE_PERSISSIONS = "huawei.intent.permissions.APS_RESOLUTION_CHANGE_ACTION";
    private static final int DEFAULT_EDGE_FLAGS = 0;
    private static final int DEFAULT_META_STATE = 0;
    private static final float DEFAULT_PRECISION_X = 1.0f;
    private static final float DEFAULT_PRECISION_Y = 1.0f;
    private static final float DEFAULT_SIZE = 1.0f;
    private static final String DEFAULT_SWING_GESTURE_LAND_CFG = "u,0.5,0.5,0.333,300;d,0.5,0.5,0.333,300;l,0.5,0.5,0.375,200;r,0.5,0.5,0.375,200";
    private static final String DEFAULT_SWING_GESTURE_PORT_CFG = "u,0.667,0.375,0.375,300;d,0.667,0.375,0.375,300;l,0.75,0.667,0.5,200;r,0.25,0.667,0.5,200";
    private static final int[] DISABLE_SWING_APP_TYPES = {3, 9, 22};
    private static final int INDEX_DIRECTION = 0;
    private static final int INDEX_START_X = 1;
    private static final int INDEX_START_Y = 2;
    private static final int INDEX_SWIPE_DISTANCE = 3;
    private static final int INDEX_SWIPE_TIME = 4;
    private static final int INJECT_MOTION_EVENT_DELAY = 5;
    private static final boolean IS_DISABLE_MUSICSPORT = SystemProperties.getBoolean("hsdf.keyguard.disable_musicsport_style", true);
    private static final String KEYGUARD_UI_WINDOW_TITLE = "StatusBar";
    private static final String PKG_SYSTEMUI = "com.android.systemui";
    private static final int STATE_LEFT = 1;
    private static final int STATE_MIDDLE = 0;
    private static final int STATE_RIGHT = 2;
    private static final String TAG = "HwSwingMotionGestureHandler";
    private BroadcastReceiver mApsResolutionChangeReceiver = new BroadcastReceiver() {
        /* class com.android.server.swing.HwSwingMotionGestureHandler.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                Slog.i(HwSwingMotionGestureHandler.TAG, "updateDisplaySize because resolution changed");
                HwSwingMotionGestureHandler.this.updateDisplaySize();
            }
        }
    };
    private Context mContext;
    private int mDisplayHeight;
    private int mDisplayWidth;
    private Point mEndPoint = new Point();
    private String mFocusPkgName;
    private String mFocusWindowTitle;
    private Handler mHandler;
    private boolean mIsFingersTouching;
    private KeyguardManager mKeyguardManager;
    private String mLastMusicPlayPkgName = "";
    private int mOrientation = 0;
    private Point mStartPoint = new Point();
    private int mSwingDuration;
    private HwSwingEventNotifierUtil mSwingEventNotifierUtil;
    private float mSwipeDownLandEndX;
    private float mSwipeDownLandEndY;
    private float mSwipeDownLandStartX;
    private float mSwipeDownLandStartY;
    private int mSwipeDownLandTime;
    private float mSwipeDownPortEndX;
    private float mSwipeDownPortEndY;
    private float mSwipeDownPortStartX;
    private float mSwipeDownPortStartY;
    private int mSwipeDownPortTime;
    private float mSwipeLeftLandEndX;
    private float mSwipeLeftLandEndY;
    private float mSwipeLeftLandStartX;
    private float mSwipeLeftLandStartY;
    private int mSwipeLeftLandTime;
    private float mSwipeLeftPortEndX;
    private float mSwipeLeftPortEndY;
    private float mSwipeLeftPortStartX;
    private float mSwipeLeftPortStartY;
    private int mSwipeLeftPortTime;
    private float mSwipeRightLandEndX;
    private float mSwipeRightLandEndY;
    private float mSwipeRightLandStartX;
    private float mSwipeRightLandStartY;
    private int mSwipeRightLandTime;
    private float mSwipeRightPortEndX;
    private float mSwipeRightPortEndY;
    private float mSwipeRightPortStartX;
    private float mSwipeRightPortStartY;
    private int mSwipeRightPortTime;
    private float mSwipeUpLandEndX;
    private float mSwipeUpLandEndY;
    private float mSwipeUpLandStartX;
    private float mSwipeUpLandStartY;
    private int mSwipeUpLandTime;
    private float mSwipeUpPortEndX;
    private float mSwipeUpPortEndY;
    private float mSwipeUpPortStartX;
    private float mSwipeUpPortStartY;
    private int mSwipeUpPortTime;
    private BroadcastReceiver mUserSwitchReceiver = new BroadcastReceiver() {
        /* class com.android.server.swing.HwSwingMotionGestureHandler.AnonymousClass2 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Slog.i(HwSwingMotionGestureHandler.TAG, "on receive userSwitchReceiver");
            HwSwingMotionGestureHandler.this.updateDisplaySize();
        }
    };

    public HwSwingMotionGestureHandler(Context context) {
        this.mContext = context;
        this.mHandler = new Handler();
        this.mSwingEventNotifierUtil = HwSwingEventNotifierUtil.getInstance(this.mContext);
        updateDisplaySize();
        this.mContext.registerReceiverAsUser(this.mApsResolutionChangeReceiver, UserHandle.ALL, new IntentFilter(APS_RESOLUTION_CHANGE_ACTION), APS_RESOLUTION_CHANGE_PERSISSIONS, null);
        this.mContext.registerReceiver(this.mUserSwitchReceiver, new IntentFilter(SmartDualCardConsts.SYSTEM_STATE_ACTION_USER_SWITCHED));
        parseSwingGestureConfig();
        MediaTransactWrapperEx.registerMusicObserver(new IHsmMusicWatch.Stub() {
            /* class com.android.server.swing.HwSwingMotionGestureHandler.AnonymousClass3 */

            public int onMusicPlaying(int uid, int pid) throws RemoteException {
                String currentMusicPlayPkgName = HwSwingMotionGestureHandler.this.getCurrentMusicPackageName(pid);
                Slog.i(HwSwingMotionGestureHandler.TAG, "onMusicPlaying pkgName:" + currentMusicPlayPkgName);
                if ("".equals(currentMusicPlayPkgName)) {
                    return 0;
                }
                HwSwingMotionGestureHandler.this.mLastMusicPlayPkgName = currentMusicPlayPkgName;
                return 0;
            }

            public int onMusicPauseOrStop(int uid, int pid) throws RemoteException {
                return 0;
            }
        });
    }

    public boolean dispatchUnhandledKey(KeyEvent event) {
        Slog.i(TAG, "dispatchUnhandledKey:" + event);
        int keyCode = event.getKeyCode();
        if (keyCode == 712) {
            this.mHandler.post(new Runnable() {
                /* class com.android.server.swing.$$Lambda$HwSwingMotionGestureHandler$kvQFu7AlcqTtm9SKymY4HgEQKi4 */

                @Override // java.lang.Runnable
                public final void run() {
                    HwSwingMotionGestureHandler.this.lambda$dispatchUnhandledKey$0$HwSwingMotionGestureHandler();
                }
            });
            return true;
        } else if (keyCode == 713) {
            this.mHandler.post(new Runnable() {
                /* class com.android.server.swing.$$Lambda$HwSwingMotionGestureHandler$RWtrY9ZvqaBWi9ULXKXS4Ww9xWM */

                @Override // java.lang.Runnable
                public final void run() {
                    HwSwingMotionGestureHandler.this.lambda$dispatchUnhandledKey$1$HwSwingMotionGestureHandler();
                }
            });
            return true;
        } else if (keyCode == 710) {
            this.mHandler.post(new Runnable() {
                /* class com.android.server.swing.$$Lambda$HwSwingMotionGestureHandler$dOvp95T8MctFEkVq40v2kHCrSzg */

                @Override // java.lang.Runnable
                public final void run() {
                    HwSwingMotionGestureHandler.this.lambda$dispatchUnhandledKey$2$HwSwingMotionGestureHandler();
                }
            });
            return true;
        } else if (keyCode == 711) {
            this.mHandler.post(new Runnable() {
                /* class com.android.server.swing.$$Lambda$HwSwingMotionGestureHandler$6C8pKd2Tc1UAdhMSxxkdZ_HJ4 */

                @Override // java.lang.Runnable
                public final void run() {
                    HwSwingMotionGestureHandler.this.lambda$dispatchUnhandledKey$3$HwSwingMotionGestureHandler();
                }
            });
            return true;
        } else if (keyCode != 714) {
            return false;
        } else {
            this.mHandler.post(new Runnable() {
                /* class com.android.server.swing.$$Lambda$HwSwingMotionGestureHandler$raeno88L0G5QauwjuwVaMKVAOQM */

                @Override // java.lang.Runnable
                public final void run() {
                    HwSwingMotionGestureHandler.this.lambda$dispatchUnhandledKey$4$HwSwingMotionGestureHandler();
                }
            });
            return true;
        }
    }

    public /* synthetic */ void lambda$dispatchUnhandledKey$0$HwSwingMotionGestureHandler() {
        emulateSwingTouch(3);
    }

    public /* synthetic */ void lambda$dispatchUnhandledKey$1$HwSwingMotionGestureHandler() {
        emulateSwingTouch(4);
    }

    public /* synthetic */ void lambda$dispatchUnhandledKey$2$HwSwingMotionGestureHandler() {
        emulateSwingTouch(1);
    }

    public /* synthetic */ void lambda$dispatchUnhandledKey$3$HwSwingMotionGestureHandler() {
        emulateSwingTouch(2);
    }

    public void notifyFingersTouching(boolean isTouching) {
        this.mIsFingersTouching = isTouching;
    }

    public void notifyFocusChange(String focusWindowTitle, String focusPkgName) {
        this.mFocusPkgName = focusPkgName;
        this.mFocusWindowTitle = focusWindowTitle;
    }

    private void parseSwingGestureConfig() {
        String swingGesturePortCfg = SystemProperties.get("persist.sys.swing_gesture_p_cfg", "");
        if ("".equals(swingGesturePortCfg)) {
            swingGesturePortCfg = DEFAULT_SWING_GESTURE_PORT_CFG;
        }
        parseSwingGesturePortConfig(swingGesturePortCfg);
        Slog.i(TAG, "swingGesturePortCfg:" + swingGesturePortCfg);
        String swingGestureLandCfg = SystemProperties.get("persist.sys.swing_gesture_l_cfg", "");
        if ("".equals(swingGestureLandCfg)) {
            swingGestureLandCfg = DEFAULT_SWING_GESTURE_LAND_CFG;
        }
        parseSwingGestureLandConfig(swingGestureLandCfg);
        Slog.i(TAG, "swingGestureLandCfg:" + swingGestureLandCfg);
    }

    private void parseSwingGesturePortConfig(String swingGesturePortCfg) {
        for (String portDirectionConfig : swingGesturePortCfg.split(AwarenessInnerConstants.SEMI_COLON_KEY)) {
            try {
                String[] portConfigs = portDirectionConfig.split(",");
                if ("u".equals(portConfigs[0].toLowerCase(Locale.getDefault()))) {
                    this.mSwipeUpPortStartX = Float.valueOf(portConfigs[1]).floatValue();
                    this.mSwipeUpPortStartY = Float.valueOf(portConfigs[2]).floatValue();
                    this.mSwipeUpPortEndX = this.mSwipeUpPortStartX;
                    this.mSwipeUpPortEndY = this.mSwipeUpPortStartY - Float.valueOf(portConfigs[3]).floatValue();
                    this.mSwipeUpPortTime = Integer.valueOf(portConfigs[4]).intValue();
                } else if ("d".equals(portConfigs[0].toLowerCase(Locale.getDefault()))) {
                    this.mSwipeDownPortStartX = Float.valueOf(portConfigs[1]).floatValue();
                    this.mSwipeDownPortStartY = Float.valueOf(portConfigs[2]).floatValue();
                    this.mSwipeDownPortEndX = this.mSwipeUpPortStartX;
                    this.mSwipeDownPortEndY = this.mSwipeUpPortStartY + Float.valueOf(portConfigs[3]).floatValue();
                    this.mSwipeDownPortTime = Integer.valueOf(portConfigs[4]).intValue();
                } else if ("l".equals(portConfigs[0].toLowerCase(Locale.getDefault()))) {
                    this.mSwipeLeftPortStartX = Float.valueOf(portConfigs[1]).floatValue();
                    this.mSwipeLeftPortStartY = Float.valueOf(portConfigs[2]).floatValue();
                    this.mSwipeLeftPortEndX = this.mSwipeLeftPortStartX - Float.valueOf(portConfigs[3]).floatValue();
                    this.mSwipeLeftPortEndY = this.mSwipeLeftPortStartY;
                    this.mSwipeLeftPortTime = Integer.valueOf(portConfigs[4]).intValue();
                } else if ("r".equals(portConfigs[0].toLowerCase(Locale.getDefault()))) {
                    this.mSwipeRightPortStartX = Float.valueOf(portConfigs[1]).floatValue();
                    this.mSwipeRightPortStartY = Float.valueOf(portConfigs[2]).floatValue();
                    this.mSwipeRightPortEndX = this.mSwipeRightPortStartX + Float.valueOf(portConfigs[3]).floatValue();
                    this.mSwipeRightPortEndY = this.mSwipeRightPortStartY;
                    this.mSwipeRightPortTime = Integer.valueOf(portConfigs[4]).intValue();
                }
            } catch (NumberFormatException e) {
                Slog.e(TAG, "parseSwingGesturePortConfig parse number error");
            }
        }
    }

    private void parseSwingGestureLandConfig(String swingGestureLandCfg) {
        for (String portDirectionConfig : swingGestureLandCfg.split(AwarenessInnerConstants.SEMI_COLON_KEY)) {
            try {
                String[] portConfigs = portDirectionConfig.split(",");
                if ("u".equals(portConfigs[0].toLowerCase(Locale.getDefault()))) {
                    this.mSwipeUpLandStartX = Float.valueOf(portConfigs[1]).floatValue();
                    this.mSwipeUpLandStartY = Float.valueOf(portConfigs[2]).floatValue();
                    this.mSwipeUpLandEndX = this.mSwipeUpLandStartX;
                    this.mSwipeUpLandEndY = this.mSwipeUpLandStartY - Float.valueOf(portConfigs[3]).floatValue();
                    this.mSwipeUpLandTime = Integer.valueOf(portConfigs[4]).intValue();
                } else if ("d".equals(portConfigs[0].toLowerCase(Locale.getDefault()))) {
                    this.mSwipeDownLandStartX = Float.valueOf(portConfigs[1]).floatValue();
                    this.mSwipeDownLandStartY = Float.valueOf(portConfigs[2]).floatValue();
                    this.mSwipeDownLandEndX = this.mSwipeUpLandStartX;
                    this.mSwipeDownLandEndY = this.mSwipeUpLandStartY + Float.valueOf(portConfigs[3]).floatValue();
                    this.mSwipeDownLandTime = Integer.valueOf(portConfigs[4]).intValue();
                } else if ("l".equals(portConfigs[0].toLowerCase(Locale.getDefault()))) {
                    this.mSwipeLeftLandStartX = Float.valueOf(portConfigs[1]).floatValue();
                    this.mSwipeLeftLandStartY = Float.valueOf(portConfigs[2]).floatValue();
                    this.mSwipeLeftLandEndX = this.mSwipeLeftLandStartX - Float.valueOf(portConfigs[3]).floatValue();
                    this.mSwipeLeftLandEndY = this.mSwipeLeftLandStartY;
                    this.mSwipeLeftLandTime = Integer.valueOf(portConfigs[4]).intValue();
                } else if ("r".equals(portConfigs[0].toLowerCase(Locale.getDefault()))) {
                    this.mSwipeRightLandStartX = Float.valueOf(portConfigs[1]).floatValue();
                    this.mSwipeRightLandStartY = Float.valueOf(portConfigs[2]).floatValue();
                    this.mSwipeRightLandEndX = this.mSwipeRightLandStartX + Float.valueOf(portConfigs[3]).floatValue();
                    this.mSwipeRightLandEndY = this.mSwipeRightLandStartY;
                    this.mSwipeRightLandTime = Integer.valueOf(portConfigs[4]).intValue();
                }
            } catch (NumberFormatException e) {
                Slog.e(TAG, "parseSwingGesturePortConfig parse number error");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getCurrentMusicPackageName(int pid) {
        if (pid <= 0) {
            Slog.w(TAG, "getCurrentMusicPackageName pid param error");
            return "";
        }
        ActivityManager activityManager = (ActivityManager) this.mContext.getSystemService("activity");
        if (activityManager == null) {
            Slog.w(TAG, "getCurrentMusicPackageName activity service is null");
            return "";
        }
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            Slog.w(TAG, "getCurrentMusicPackageName running app info is null");
            return "";
        }
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.pid == pid) {
                String packageName = getPkgNameByProcessName(appProcess.processName);
                if (getAppTypeByPkgName(packageName) == 7) {
                    return packageName;
                }
                int pkgListLength = appProcess.pkgList.length;
                for (int i = 0; i < pkgListLength; i++) {
                    String packageName2 = appProcess.pkgList[i];
                    if (getAppTypeByPkgName(packageName2) == 7) {
                        return packageName2;
                    }
                    Slog.w(TAG, "pkglist not music app");
                }
                continue;
            }
        }
        return "";
    }

    private String getPkgNameByProcessName(String processName) {
        int indexProcessFlag = -1;
        if (!"".equals(processName)) {
            indexProcessFlag = processName.indexOf(58);
        }
        String pkgName = indexProcessFlag > 0 ? processName.substring(0, indexProcessFlag) : processName;
        Slog.i(TAG, "processName:" + processName + ",pkgName:" + pkgName);
        return pkgName;
    }

    private boolean isInputMethodKeyBoardShow() {
        int inputWindowHeight = InputMethodManagerEx.getInputMethodWindowVisibleHeight((InputMethodManager) this.mContext.getSystemService("input_method"));
        Slog.i(TAG, "inputWindowHeight : " + inputWindowHeight);
        return inputWindowHeight > 0;
    }

    private int getLazyState(Context context) {
        String str = Settings.Global.getString(context.getContentResolver(), "single_hand_mode");
        if (TextUtils.isEmpty(str)) {
            return 0;
        }
        if (str.contains(HwMultiWinConstants.LEFT_HAND_LAZY_MODE_STR)) {
            return 1;
        }
        if (str.contains(HwMultiWinConstants.RIGHT_HAND_LAZY_MODE_STR)) {
            return 2;
        }
        return 0;
    }

    private boolean isLazyMode(Context context) {
        if (context == null || getLazyState(context) == 0) {
            return false;
        }
        return true;
    }

    private int getAppTypeByPkgName(String pkgName) {
        if (pkgName == null) {
            return 255;
        }
        int appType = AppTypeRecoManager.getInstance().getAppType(pkgName);
        Slog.i(TAG, "current app:" + pkgName + ",appType:" + appType);
        return appType;
    }

    private boolean isLandScape() {
        int i = this.mOrientation;
        return i == 1 || i == 3;
    }

    private boolean disableSwingByAppType() {
        int appType = getAppTypeByPkgName(this.mFocusPkgName);
        int size = DISABLE_SWING_APP_TYPES.length;
        for (int i = 0; i < size; i++) {
            if (appType == DISABLE_SWING_APP_TYPES[i]) {
                return true;
            }
        }
        return false;
    }

    private KeyguardManager getKeyguardService() {
        if (this.mKeyguardManager == null) {
            this.mKeyguardManager = (KeyguardManager) this.mContext.getSystemService("keyguard");
        }
        return this.mKeyguardManager;
    }

    private boolean isKeyguardLocked() {
        KeyguardManager keyguardManager = getKeyguardService();
        if (keyguardManager != null) {
            return keyguardManager.isKeyguardLocked();
        }
        Slog.e(TAG, "no keyguard service");
        return false;
    }

    /* renamed from: handleMusicPausePlayIfNeeded */
    public void lambda$dispatchUnhandledKey$4$HwSwingMotionGestureHandler() {
        boolean isKeyguardLocked = isKeyguardLocked();
        Slog.i(TAG, "handleMusicPausePlay isKeyguardLocked:" + isKeyguardLocked + ",music play app:" + this.mLastMusicPlayPkgName + ",focus app:" + this.mFocusPkgName);
        if ((!isKeyguardLocked && this.mLastMusicPlayPkgName.equals(this.mFocusPkgName)) || hasMusicNotificationOnKeyguard()) {
            injectKeyEvent(85);
            Slog.i(TAG, "handleMusicPausePlay inject keyevent:85");
        }
    }

    private boolean hasMusicNotificationOnKeyguard() {
        boolean z = false;
        if (!isKeyguardLocked()) {
            return false;
        }
        IHwSwingEventNotifier hwSwingEventNotifier = this.mSwingEventNotifierUtil.getHwSwingEventNotifier();
        if (hwSwingEventNotifier == null) {
            Slog.i(TAG, "not connect to IHwSwingEventNotifier service");
            return false;
        }
        try {
            List<String> pkgList = hwSwingEventNotifier.getKeyguardNotificationInfoList();
            if (pkgList == null) {
                Slog.w(TAG, "pkgList is null");
                return false;
            }
            for (String pkgName : pkgList) {
                Slog.i(TAG, "keyguard notification pkgName:" + pkgName);
                if (pkgName.equals(this.mLastMusicPlayPkgName)) {
                    return true;
                }
            }
            boolean isMusicLockScreenStyle = false;
            try {
                if (!IS_DISABLE_MUSICSPORT && hwSwingEventNotifier.isMusicLockScreenStyle()) {
                    z = true;
                }
                isMusicLockScreenStyle = z;
            } catch (RemoteException e) {
                Slog.e(TAG, "isMusicLockScreenStyle RemoteException");
            } catch (IllegalStateException e2) {
                Slog.e(TAG, "isMusicLockScreenStyle IllegalStateException");
            }
            Slog.i(TAG, "current has no keyguard notification, musicLockScreenStyle:" + isMusicLockScreenStyle);
            return isMusicLockScreenStyle;
        } catch (RemoteException e3) {
            Slog.e(TAG, "getKeyguardNotificationInfoList RemoteException");
            return false;
        } catch (IllegalStateException e4) {
            Slog.e(TAG, "getKeyguardNotificationInfoList IllegalStateException");
            return false;
        }
    }

    private boolean shouldDisableSwingTouch() {
        if (this.mIsFingersTouching) {
            Slog.i(TAG, "fingers touching, don't inject motion event");
            return true;
        } else if (isInputMethodKeyBoardShow()) {
            Slog.i(TAG, "keyboard shows, don't inject motion event");
            return true;
        } else if (isLazyMode(this.mContext)) {
            Slog.i(TAG, "in lazy mode, don't inject motion event");
            return true;
        } else if (SystemProperties.getBoolean("hw.pc.cast.mode", false)) {
            Slog.i(TAG, "in pc mode, don't inject motion event");
            return true;
        } else if (HwMultiWindowEx.isInMultiWindowMode()) {
            Slog.i(TAG, "in multi window, don't inject motion event");
            return true;
        } else if (disableSwingByAppType()) {
            Slog.i(TAG, "for some app types, don't inject motion event");
            return true;
        } else if (WindowManagerEx.isTopFullscreen() && isLandScape() && getAppTypeByPkgName(this.mFocusPkgName) == 8) {
            Slog.i(TAG, "landscape full screen video app, don't inject motion event");
            return true;
        } else if (!isKeyguardLocked() || KEYGUARD_UI_WINDOW_TITLE.equals(this.mFocusWindowTitle)) {
            return false;
        } else {
            Slog.i(TAG, "keyguard showing and occluded, don't inject motion event");
            return true;
        }
    }

    public void emulateSwingTouch(int swipeDirection) {
        if (!shouldDisableSwingTouch()) {
            long now = SystemClock.uptimeMillis();
            if (isLandScape()) {
                calLandEmulatePointByDirection(swipeDirection);
            } else {
                calPortEmulatePointByDirection(swipeDirection);
            }
            int startX = this.mStartPoint.x;
            int startY = this.mStartPoint.y;
            int endX = this.mEndPoint.x;
            int endY = this.mEndPoint.y;
            Slog.i(TAG, "emulateSwingTouch from " + this.mStartPoint + " to " + this.mEndPoint + ",screen size(" + this.mDisplayHeight + "," + this.mDisplayWidth + ")");
            injectMotionEvent(0, now, (float) startX, (float) startY, 1.0f);
            long endTime = now + ((long) this.mSwingDuration);
            while (now < endTime) {
                float alpha = ((float) (now - now)) / ((float) this.mSwingDuration);
                injectMotionEvent(2, now, lerp((float) startX, (float) endX, alpha), lerp((float) startY, (float) endY, alpha), 1.0f);
                SystemClock.sleep(5);
                now = SystemClock.uptimeMillis();
            }
            injectMotionEvent(1, now, (float) endX, (float) endY, 0.0f);
        }
    }

    private void injectMotionEvent(int action, long when, float x, float y, float pressure) {
        MotionEvent event = MotionEvent.obtain(when, when, action, x, y, pressure, 1.0f, 0, 1.0f, 1.0f, getInputDeviceId(4098), 0);
        event.setSource(4098);
        InputManager.getInstance().injectInputEvent(event, 0);
        event.recycle();
    }

    private void injectKeyEvent(int keyCode) {
        long now = SystemClock.uptimeMillis();
        KeyEvent downEvent = new KeyEvent(now, now, 0, keyCode, 0, 0, -1, 0, 0, LightsManagerEx.LIGHT_ID_SMARTBACKLIGHT);
        KeyEvent upEvent = new KeyEvent(now, now, 1, keyCode, 0, 0, -1, 0, 0, LightsManagerEx.LIGHT_ID_SMARTBACKLIGHT);
        InputManager.getInstance().injectInputEvent(downEvent, 0);
        InputManager.getInstance().injectInputEvent(upEvent, 0);
    }

    private float lerp(float a, float b, float alpha) {
        return ((b - a) * alpha) + a;
    }

    private int getInputDeviceId(int inputSource) {
        int[] devIds = InputDevice.getDeviceIds();
        for (int devId : devIds) {
            InputDevice inputDev = InputDevice.getDevice(devId);
            if (inputDev != null && inputDev.supportsSource(inputSource)) {
                return devId;
            }
        }
        return 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateDisplaySize() {
        Display display = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
        Point screenSize = new Point();
        display.getRealSize(screenSize);
        Slog.i(TAG, "updateDisplaySize screenSize : " + screenSize);
        this.mDisplayWidth = screenSize.x > screenSize.y ? screenSize.y : screenSize.x;
        this.mDisplayHeight = screenSize.x > screenSize.y ? screenSize.x : screenSize.y;
    }

    private void calPortEmulatePointByDirection(int swipeDirection) {
        if (swipeDirection == 1) {
            Point point = this.mStartPoint;
            int i = this.mDisplayWidth;
            point.x = (int) (((float) i) * this.mSwipeLeftPortStartX);
            point.y = (int) (((float) this.mDisplayHeight) * this.mSwipeLeftPortStartY);
            Point point2 = this.mEndPoint;
            point2.x = (int) (((float) i) * this.mSwipeLeftPortEndX);
            point2.y = point.y;
            this.mSwingDuration = this.mSwipeLeftPortTime;
        } else if (swipeDirection == 2) {
            Point point3 = this.mStartPoint;
            int i2 = this.mDisplayWidth;
            point3.x = (int) (((float) i2) * this.mSwipeRightPortStartX);
            point3.y = (int) (((float) this.mDisplayHeight) * this.mSwipeRightPortStartY);
            Point point4 = this.mEndPoint;
            point4.x = (int) (((float) i2) * this.mSwipeRightPortEndX);
            point4.y = point3.y;
            this.mSwingDuration = this.mSwipeRightPortTime;
        } else if (swipeDirection == 3) {
            Point point5 = this.mStartPoint;
            point5.x = (int) (((float) this.mDisplayWidth) * this.mSwipeUpPortStartX);
            point5.y = (int) (((float) this.mDisplayHeight) * this.mSwipeUpPortStartY);
            this.mEndPoint.x = point5.x;
            this.mEndPoint.y = (int) (((float) this.mDisplayHeight) * this.mSwipeUpPortEndY);
            this.mSwingDuration = this.mSwipeUpPortTime;
        } else if (swipeDirection == 4) {
            Point point6 = this.mStartPoint;
            point6.x = (int) (((float) this.mDisplayWidth) * this.mSwipeDownPortStartX);
            point6.y = (int) (((float) this.mDisplayHeight) * this.mSwipeDownPortStartY);
            this.mEndPoint.x = point6.x;
            this.mEndPoint.y = (int) (((float) this.mDisplayHeight) * this.mSwipeDownPortEndY);
            this.mSwingDuration = this.mSwipeDownPortTime;
        }
    }

    private void calLandEmulatePointByDirection(int swipeDirection) {
        if (swipeDirection == 1) {
            Point point = this.mStartPoint;
            int i = this.mDisplayHeight;
            point.x = (int) (((float) i) * this.mSwipeLeftLandStartX);
            point.y = (int) (((float) this.mDisplayWidth) * this.mSwipeLeftLandStartY);
            Point point2 = this.mEndPoint;
            point2.x = (int) (((float) i) * this.mSwipeLeftLandEndX);
            point2.y = point.y;
            this.mSwingDuration = this.mSwipeLeftLandTime;
        } else if (swipeDirection == 2) {
            Point point3 = this.mStartPoint;
            int i2 = this.mDisplayHeight;
            point3.x = (int) (((float) i2) * this.mSwipeRightLandStartX);
            point3.y = (int) (((float) this.mDisplayWidth) * this.mSwipeRightLandStartY);
            Point point4 = this.mEndPoint;
            point4.x = (int) (((float) i2) * this.mSwipeRightLandEndX);
            point4.y = point3.y;
            this.mSwingDuration = this.mSwipeRightLandTime;
        } else if (swipeDirection == 3) {
            Point point5 = this.mStartPoint;
            point5.x = (int) (((float) this.mDisplayHeight) * this.mSwipeUpLandStartX);
            point5.y = (int) (((float) this.mDisplayWidth) * this.mSwipeUpLandStartY);
            this.mEndPoint.x = point5.x;
            this.mEndPoint.y = (int) (((float) this.mDisplayWidth) * this.mSwipeUpLandEndY);
            this.mSwingDuration = this.mSwipeUpLandTime;
        } else if (swipeDirection == 4) {
            Point point6 = this.mStartPoint;
            point6.x = (int) (((float) this.mDisplayHeight) * this.mSwipeDownLandStartX);
            point6.y = (int) (((float) this.mDisplayWidth) * this.mSwipeDownLandStartY);
            this.mEndPoint.x = point6.x;
            this.mEndPoint.y = (int) (((float) this.mDisplayWidth) * this.mSwipeDownLandEndY);
            this.mSwingDuration = this.mSwipeDownLandTime;
        }
    }

    public void notifyRotationChange(int rotation) {
        this.mOrientation = rotation;
        Slog.i(TAG, "notifyRotationChange rotation : " + rotation);
    }
}
