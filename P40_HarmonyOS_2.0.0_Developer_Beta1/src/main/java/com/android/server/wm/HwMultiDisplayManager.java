package com.android.server.wm;

import android.app.AbsWallpaperManagerInner;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.TaskStackListener;
import android.app.WallpaperManager;
import android.app.WindowConfiguration;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.HardwareBuffer;
import android.hardware.display.DisplayManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.pc.IHwPCManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Flog;
import android.util.HwPCUtils;
import android.util.Pair;
import android.util.Slog;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.IHwRotateObserver;
import android.view.SurfaceControl;
import com.android.server.am.AppTimeTracker;
import com.android.server.intellicom.common.SmartDualCardConsts;
import com.android.server.multiwin.HwBlur;
import com.android.server.policy.PhoneWindowManager;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.wm.HwMultiDisplayManager;
import com.android.server.wm.utils.HwDisplaySizeUtil;
import com.google.android.collect.Sets;
import com.huawei.android.app.HwActivityTaskManager;
import com.huawei.android.app.WallpaperManagerExt;
import com.huawei.android.app.WindowManagerEx;
import com.huawei.android.biometric.FingerprintServiceEx;
import com.huawei.android.fsm.HwFoldScreenManager;
import com.huawei.android.os.HwPowerManager;
import com.huawei.server.HwPartMagicWindowServiceFactory;
import com.huawei.server.inputmethod.InputMethodManagerInternalEx;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public class HwMultiDisplayManager {
    private static final String ACCESSIBILITY_SCREENREADER_ENABLED = "accessibility_screenreader_enabled";
    private static final int ALL_TASK_ID = -1;
    private static final int BLUR_RADIUS = 200;
    private static final String CAMERA_PACKAGE_NAME = "com.huawei.camera";
    private static final String CONFIG_HW_MULTI_CAST = "hw_mc.multiscreen.windowcast.value";
    private static final String CONFIG_HW_MULTI_CAST_DEBUG = "persist.sys.hw_multi_cast";
    private static final int DEFAULT_MAIN_TASKID = 0;
    private static final float DEFAULT_SCALE = 0.2f;
    private static final String DEVICE_MULTI_DISPLAY_PERMISSION = "com.huawei.permission.hwmultidisplay.DEVICE_MULTI_DISPLAY";
    private static final int DOWN_SCALE = 20;
    private static final int IDLE_TIME_DELAY = 200;
    private static final int INVALID_ARG = -1;
    private static final String KEY_DISPLAY_SIZE_LOCAL_DISPLAY_RECT_LAND = "local_diaplay_rect_land";
    private static final String KEY_DISPLAY_SIZE_LOCAL_DISPLAY_RECT_PORT = "local_diaplay_rect_port";
    private static final String KEY_DISPLAY_SIZE_LOCAL_LAYER_RECT_LAND = "local_layer_rect_land";
    private static final String KEY_DISPLAY_SIZE_LOCAL_LAYER_RECT_PORT = "local_layer_rect_port";
    private static final String KEY_DISPLAY_SIZE_PC_WINDOW_DISPLAY = "pc_window_display";
    private static final String KEY_DISPLAY_SIZE_VIRTUAL_DISPLAY_RECT_LAND = "virtual_diaplay_rect_land";
    private static final String KEY_DISPLAY_SIZE_VIRTUAL_DISPLAY_RECT_PORT = "virtual_diaplay_rect_port";
    private static final String KEY_DISPLAY_SIZE_VIRTUAL_LAYER_RECT_LAND = "virtual_layer_rect_land";
    private static final String KEY_DISPLAY_SIZE_VIRTUAL_LAYER_RECT_PORT = "virtual_layer_rect_port";
    private static final String KEY_IS_OVER_GPU_SIZE = "is_over_gpu_size";
    public static final String KEY_PC_APPLOCK = "pcmuticastwindow_applock";
    public static final String KEY_PC_BACKGROUND_BLUR = "pcmuticastwindow_background_blur";
    public static final String KEY_PC_BUNDLE = "pc_bundle_data";
    public static final String KEY_PC_CIR_SUPPORT = "pcmuticastwindow_cir_support";
    private static final String KEY_PC_CONFIG_NUM = "pc_config_num";
    private static final String KEY_PC_DISPLAY_ROTATION = "pcmuticastwindow_display_rotation";
    public static final String KEY_PC_EXTENDED = "pcmuticastwindow_extendarea";
    public static final String KEY_PC_FLOATING = "pcmuticastwindow_floatwindow";
    public static final String KEY_PC_ISOVERLAY = "pcmuticastwindow_isoverlay";
    public static final String KEY_PC_MULTI_WINDOW_MODE = "pcmuticastwindow_multi_window_mode";
    public static final String KEY_PC_ORIENTATION = "pcmuticastwindow_orientation";
    public static final String KEY_PC_PKGNAME = "pcmuticastwindow_pkgname";
    public static final String KEY_PC_RECT = "pcmuticastwindow_rect";
    public static final String KEY_PC_RESIZEABLE = "pcmuticastwindow_resizeable";
    public static final String KEY_PC_SUPPORT_PCSIZE_MAXIMIZED = "pcmuticastwindow_pcsize_maximized";
    public static final String KEY_PC_TASKID = "pcmuticastwindow_taskid";
    private static final String KEY_PC_TASK_BITMAP_BLUR = "pcmuticastwindow_task_bitmap_blur";
    public static final String KEY_PC_TYPE = "pcmuticastwindow_type";
    public static final String KEY_PC_VISIBLE = "pcmuticastwindow_visible";
    private static final String LAUNCHER_PACKAGE_NAME = "com.huawei.android.launcher";
    private static final int LAYER_HIDDEN = 0;
    private static final int LAYER_SHOW = 1;
    private static final int MAX_CAPTURE_IMAGES_SIZE = 40960;
    private static final int MAX_IMAGES_SIZE = 40960;
    private static final int MAX_SEARCH_APP_SIZE = 50;
    private static final int MSG_FROM_PHONE_APP_HAS_LOCKED = 18;
    private static final int MSG_FROM_PHONE_DISPLAY_LOCKSCREEN_LAYER = 9;
    private static final int MSG_FROM_PHONE_DISPLAY_ORIENTATION_UPDATE = 6;
    private static final int MSG_FROM_PHONE_DISPLAY_PIP_DISABLED = 7;
    private static final int MSG_FROM_PHONE_DISPLAY_SECURE_LAYER = 10;
    private static final int MSG_FROM_PHONE_DISPLAY_SEND_HINT = 12;
    private static final int MSG_FROM_PHONE_DISPLAY_SEND_TASK = 14;
    private static final int MSG_FROM_PHONE_DISPLAY_SEND_WALLPAPER = 11;
    private static final int MSG_FROM_PHONE_DISPLAY_SINGLE_HAND_DISABLED = 8;
    private static final int MSG_FROM_PHONE_START_ACTIVITY = 15;
    private static final int MSG_FROM_PHONE_TASK_CREATE = 1;
    private static final int MSG_FROM_PHONE_TASK_ORIENTATION_UPDATE = 4;
    private static final int MSG_FROM_PHONE_TASK_REMOVED = 2;
    private static final int MSG_FROM_PHONE_TASK_STACK_CHANGE = 5;
    private static final int MSG_FROM_PHONE_TASK_UPDATE = 3;
    private static final int MSG_FROM_PHONE_WINDOW_FOCUS_CHANGE = 16;
    private static final int MSG_FROM_PHONE_WINDOW_TASK_EXIST = 17;
    private static final int MULTI_CAST_NUM = SystemProperties.getInt(CONFIG_HW_MULTI_CAST, -1);
    private static final int MULTI_CAST_WIN_NUM_MAX = 2;
    private static final Object M_LOCK = new Object();
    private static final int PARAM_TYPE_DISPLAY_SIZE = 1;
    private static final int PARAM_TYPE_PC_CONFIG_NUM = 2;
    private static final int PARAM_TYPE_PC_WINDOW_DISPLAY_SIZE = 4;
    private static final int PARAM_TYPE_START_FLOAT_WIN = 3;
    public static final String PC_DISPLAY_NAME = "HiSightPCDisplay";
    private static final int PC_MODE_APP_LOCK = 2;
    private static final int PC_MODE_MAX_EXCEED = 1;
    private static final int PC_MODE_OK = 0;
    private static final int PC_MODE_UNKNOWN = -1;
    private static final int PC_MODE_UNRESIZE = 3;
    private static final String PERMISSION_PACKAGE_NAME = "com.android.permissioncontroller";
    public static final int REMINDER_TYPE_KEYGUARD_LOCKED = 1;
    public static final int REMINDER_TYPE_SECURE_VIEW = 2;
    private static final String SEMICOLON_STR = ";";
    private static final int STACK_VISIBILITY_VISIBLE_BEHIND_TRANSLUCENT = 1;
    public static final String TAG = "HwMultiDisplayManager";
    private static final int UNFREEZE_TIME_DELAY = 200;
    private static int sMultiCastWindowNumConfig = 0;
    private static volatile HwMultiDisplayManager sSingleInstance = null;
    private AbsWallpaperManagerInner.IBlurWallpaperCallback mBlurCallback;
    private boolean mCanResendAppLock = true;
    private List<String> mConfigDirectlyPCFullScreenList = new ArrayList();
    private List<String> mConfigLandScapeList = new ArrayList();
    private List<String> mConfigResizeByPCSizeList = new ArrayList();
    private List<String> mConfigSupportMagicWindowList = new ArrayList();
    private List<String> mConfigSupportPCMultiCastList = new ArrayList();
    private List<String> mConfigSupportRotateList = new ArrayList();
    private List<String> mConfigUnSupportPCMultiCastList = new ArrayList();
    private List<String> mConfigVideosList = new ArrayList();
    private int mCurOrientation;
    private DisplayManager mDisplayManager;
    private int mFullHeightForLand;
    private int mFullHeightForPort;
    private int mFullWidthForLand;
    private int mFullWidthForPort;
    private Map<Integer, Boolean> mHoldScreenDisplayStates = new HashMap();
    private Map<Integer, ScreenLock> mHoldScreenLocks = new HashMap();
    private boolean mIsFullScreenPadCast;
    private boolean mIsLockedViewShown = false;
    private boolean mIsOverGPUSize = false;
    private boolean mIsPCMultiCastMode = false;
    private boolean mIsSecurePadCast;
    private Rect mLocalDisplayRectForLand = new Rect();
    private Rect mLocalDisplayRectForPort = new Rect();
    private Rect mLocalLayerRectForLand = new Rect();
    private Rect mLocalLayerRectForPort = new Rect();
    private final ArrayList<Messenger> mMessengers = new ArrayList<>();
    private int mOverGPUHeight = 0;
    private int mOverGPUWidth = 0;
    private int mPCCastVirtualDisplayId = -1;
    private int mPadCastVirtualDisplayFirstStackId = -1;
    private int mPadCastVirtualDisplayId = -1;
    private Bitmap mPadCastWallpaperBitmap;
    private int mPcHeight;
    private int mPcWidth;
    private WindowManagerPolicy mPolicy;
    private PowerManager mPowerManager;
    private ArrayList<Bundle> mRecentTaskList = new ArrayList<>();
    private IHwRotateObserver mRotateObserver = new IHwRotateObserver.Stub() {
        /* class com.android.server.wm.HwMultiDisplayManager.AnonymousClass3 */

        public void onRotate(int oldRotation, int newRotation) {
            int orientation = 1;
            if (newRotation == 1 || newRotation == 3) {
                orientation = 0;
            }
            Bundle msgData = new Bundle();
            msgData.putInt(HwMultiDisplayManager.KEY_PC_DISPLAY_ROTATION, newRotation);
            boolean fixMultiCastWindowAfterTaskRemoved = HwMultiDisplayManager.this.fixMultiCastWindowAfterTaskRemoved();
            Slog.i(HwMultiDisplayManager.TAG, "onRotationChanged MSG_FROM_PHONE_DISPLAY_ORIENTATION_UPDATE , oldRotation = " + oldRotation + ", newRotation = " + newRotation + ", orientation = " + orientation);
            HwMultiDisplayManager.this.sendToMessengers(6, orientation, fixMultiCastWindowAfterTaskRemoved ? 1 : 0, msgData);
        }
    };
    private final BroadcastReceiver mScreenStateReceiver = new BroadcastReceiver() {
        /* class com.android.server.wm.HwMultiDisplayManager.AnonymousClass2 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null && HwMultiDisplayManager.this.mIsPCMultiCastMode && HwMultiDisplayManager.this.mPolicy != null) {
                String action = intent.getAction();
                char c = 65535;
                int hashCode = action.hashCode();
                if (hashCode != -2128145023) {
                    if (hashCode != -1454123155) {
                        if (hashCode == 823795052 && action.equals(FingerprintServiceEx.ACTION_USER_PRESENT)) {
                            c = 2;
                        }
                    } else if (action.equals(SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_ON)) {
                        c = 1;
                    }
                } else if (action.equals(SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_OFF)) {
                    c = 0;
                }
                if (c == 0) {
                    Slog.i(HwMultiDisplayManager.TAG, "Receive broadcast: ACTION_SCREEN_OFF");
                    if (HwMultiDisplayManager.this.mPolicy.isKeyguardLocked()) {
                        HwMultiDisplayManager.this.showAllReminderIfNeeded();
                    }
                } else if (c == 1) {
                    Slog.i(HwMultiDisplayManager.TAG, "Receive broadcast: ACTION_SCREEN_ON");
                    if (!HwMultiDisplayManager.this.mPolicy.isKeyguardLocked()) {
                        HwMultiDisplayManager.this.hideAllReminderIfNeeded();
                    }
                } else if (c == 2) {
                    Slog.i(HwMultiDisplayManager.TAG, "Receive broadcast: ACTION_USER_PRESENT");
                    HwMultiDisplayManager.this.hideAllReminderIfNeeded();
                }
            }
        }
    };
    final ActivityTaskManagerService mService;
    private final TaskStackListener mTaskStackListener = new TaskStackListener() {
        /* class com.android.server.wm.HwMultiDisplayManager.AnonymousClass1 */

        public void onTaskRemoved(int taskId) throws RemoteException {
            if (HwMultiDisplayManager.this.mIsPCMultiCastMode) {
                HwMultiDisplayManager.this.onTaskRemoved(taskId);
            }
        }

        public void onTaskMovedToFront(ActivityManager.RunningTaskInfo runningTaskInfo) throws RemoteException {
            if (HwMultiDisplayManager.this.mIsPCMultiCastMode) {
                HwMultiDisplayManager.this.onTaskMovedToFront(runningTaskInfo);
            }
        }

        public void onActivityRequestedOrientationChanged(int i, int i1) throws RemoteException {
            if (HwMultiDisplayManager.this.mIsPCMultiCastMode) {
                HwMultiDisplayManager.this.onTaskRequestedOrientationChanged(i, i1);
            }
        }

        public void onTaskRemovalStarted(ActivityManager.RunningTaskInfo runningTaskInfo) throws RemoteException {
            if (HwMultiDisplayManager.this.mIsPCMultiCastMode) {
                HwMultiDisplayManager.this.onTaskRemovalStarted(runningTaskInfo);
            }
        }
    };
    private String mTopFullScreenPackageName = "";
    private Rect mVirtualDisplayRectForLand = new Rect();
    private Rect mVirtualDisplayRectForPort = new Rect();
    private int mVirtualHeightForLand;
    private int mVirtualHeightForPort;
    private Rect mVirtualLayerRectForLand = new Rect();
    private Rect mVirtualLayerRectForPort = new Rect();
    private int mVirtualWidthForLand;
    private int mVirtualWidthForPort;
    private WallpaperManager mWallpaperManager;

    private HwMultiDisplayManager(ActivityTaskManagerService service) {
        this.mService = service;
    }

    public static HwMultiDisplayManager getInstance(ActivityTaskManagerServiceEx atmsEx) {
        if (atmsEx == null || atmsEx.getActivityTaskManagerService() == null) {
            return null;
        }
        return getInstance(atmsEx.getActivityTaskManagerService());
    }

    public static HwMultiDisplayManager getInstance(ActivityTaskManagerService service) {
        if (sSingleInstance == null) {
            synchronized (M_LOCK) {
                if (sSingleInstance == null) {
                    sSingleInstance = new HwMultiDisplayManager(service);
                }
            }
        }
        return sSingleInstance;
    }

    public void onSystemReady() {
        this.mDisplayManager = (DisplayManager) this.mService.mContext.getSystemService("display");
        this.mPowerManager = (PowerManager) this.mService.mContext.getSystemService("power");
        this.mPolicy = this.mService.mWindowManager.getPolicy();
        registerCastDisplayListener();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isPCMultiCastDisplay(String uniqueId) {
        return this.mIsPCMultiCastMode && !TextUtils.isEmpty(uniqueId) && uniqueId.contains(PC_DISPLAY_NAME);
    }

    private void registerCastDisplayListener() {
        if (Build.VERSION.SDK_INT >= 17) {
            this.mDisplayManager.registerDisplayListener(new DisplayManager.DisplayListener() {
                /* class com.android.server.wm.HwMultiDisplayManager.AnonymousClass4 */

                @Override // android.hardware.display.DisplayManager.DisplayListener
                public void onDisplayAdded(int displayId) {
                    Display display = HwMultiDisplayManager.this.mDisplayManager.getDisplay(displayId);
                    Slog.i(HwMultiDisplayManager.TAG, "onDisplayAdded: " + displayId);
                    if (display != null && HwActivityTaskManager.isCastDisplay(display.getUniqueId(), "padCast")) {
                        Slog.i(HwMultiDisplayManager.TAG, "set pad cast virtual display id");
                        SystemProperties.set("hw.multidisplay.mode.pad", Boolean.TRUE.toString());
                        HwMultiDisplayManager.this.mPadCastVirtualDisplayId = displayId;
                        Settings.Secure.putInt(HwMultiDisplayManager.this.mService.mContext.getContentResolver(), "hw_multidisplay_mode", 1);
                    } else if (display != null && HwMultiDisplayManager.this.isPCMultiCastDisplay(display.getUniqueId())) {
                        Slog.i(HwMultiDisplayManager.TAG, "set pc cast virtual display id");
                        HwMultiDisplayManager.this.mPCCastVirtualDisplayId = displayId;
                        HwMultiDisplayManager hwMultiDisplayManager = HwMultiDisplayManager.this;
                        hwMultiDisplayManager.mWallpaperManager = (WallpaperManager) hwMultiDisplayManager.mService.mContext.getSystemService("wallpaper");
                        IntentFilter unlockFilter = new IntentFilter();
                        unlockFilter.addAction(FingerprintServiceEx.ACTION_USER_PRESENT);
                        unlockFilter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_OFF);
                        unlockFilter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_ON);
                        HwMultiDisplayManager.this.mService.mContext.registerReceiver(HwMultiDisplayManager.this.mScreenStateReceiver, unlockFilter);
                        HwMultiDisplayManager hwMultiDisplayManager2 = HwMultiDisplayManager.this;
                        hwMultiDisplayManager2.mBlurCallback = new BlurWallpaperCallback();
                        HwMultiDisplayManager.this.mWallpaperManager.setCallback(HwMultiDisplayManager.this.mBlurCallback);
                        HwMultiDisplayManager.this.mService.registerTaskStackListener(HwMultiDisplayManager.this.mTaskStackListener);
                        WindowManagerEx.registerRotateObserver(HwMultiDisplayManager.this.mRotateObserver);
                        HwMultiDisplayManager.this.sendWallpaperBlurBitmap();
                        HwMultiDisplayManager.this.showSecureViewIfNeed(null);
                        if (HwMultiDisplayManager.this.mPolicy != null && HwMultiDisplayManager.this.mPolicy.isKeyguardLocked()) {
                            HwMultiDisplayManager.this.showAllReminderIfNeeded();
                        }
                        ActivityDisplay at = HwMultiDisplayManager.this.mService.mRootActivityContainer.getDefaultDisplay();
                        if (at != null && at.mDisplayContent != null) {
                            try {
                                HwMultiDisplayManager.this.mRotateObserver.onRotate(0, at.mDisplayContent.getRotation());
                            } catch (RemoteException e) {
                                Slog.e(HwMultiDisplayManager.TAG, "onRotationChanged failed when display added.");
                            }
                        }
                    }
                }

                @Override // android.hardware.display.DisplayManager.DisplayListener
                public void onDisplayRemoved(int displayId) {
                    Slog.i(HwMultiDisplayManager.TAG, "onDisplayRemoved: " + displayId);
                    if (displayId == HwMultiDisplayManager.this.mPadCastVirtualDisplayId) {
                        HwMultiDisplayManager.this.mPadCastVirtualDisplayId = -1;
                        Settings.Secure.putInt(HwMultiDisplayManager.this.mService.mContext.getContentResolver(), "hw_multidisplay_mode", 0);
                        SystemProperties.set("hw.multidisplay.mode.pad", Boolean.FALSE.toString());
                        HwMultiDisplayManager.this.mIsFullScreenPadCast = false;
                        HwMultiDisplayManager.this.mIsSecurePadCast = false;
                        synchronized (HwMultiDisplayManager.this.mService.mGlobalLock) {
                            if (HwMultiDisplayManager.this.mPadCastWallpaperBitmap != null) {
                                HwMultiDisplayManager.this.mPadCastWallpaperBitmap.recycle();
                                HwMultiDisplayManager.this.mPadCastWallpaperBitmap = null;
                            }
                            HwMultiDisplayManager.this.mHoldScreenDisplayStates.remove(Integer.valueOf(displayId));
                            HwMultiWindowSplitUI.removeSplitUIVirtualDisplay(displayId);
                        }
                    }
                    if (displayId == HwMultiDisplayManager.this.mPCCastVirtualDisplayId) {
                        HwMultiDisplayManager.this.mPCCastVirtualDisplayId = -1;
                        HwMultiDisplayManager.this.resetParamsInDisplayRemove();
                        HwMultiDisplayManager.this.mService.unregisterTaskStackListener(HwMultiDisplayManager.this.mTaskStackListener);
                        WindowManagerEx.unregisterRotateObserver(HwMultiDisplayManager.this.mRotateObserver);
                        HwMultiDisplayManager.this.mService.mContext.unregisterReceiver(HwMultiDisplayManager.this.mScreenStateReceiver);
                        HwMultiDisplayManager.this.mBlurCallback = null;
                    }
                }

                @Override // android.hardware.display.DisplayManager.DisplayListener
                public void onDisplayChanged(int displayId) {
                    Slog.i(HwMultiDisplayManager.TAG, "onDisplayChanged: " + displayId);
                    HwMultiDisplayManager.this.mDisplayManager.getDisplay(displayId);
                }
            }, null);
        }
    }

    public void loadPadCastBlackList(String filePath) {
        HwPadMultiWinConfigLoader.getInstance(this.mService.mContext).loadBlackPackageNameList(filePath);
    }

    public Set<String> getPadCastBlackList() {
        return HwPadMultiWinConfigLoader.getInstance(this.mService.mContext).getBlackPackageNameList();
    }

    public Rect resizeActivityStack(IBinder token, Rect bounds, boolean isAlwaysOnTop) {
        if (!isCallerHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", Binder.getCallingPid(), Binder.getCallingUid(), "resizeActivityStack")) {
            return new Rect();
        }
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mService.getGlobalLock()) {
                ActivityRecord activity = ActivityRecord.forTokenLocked(token);
                if (!(activity == null || activity.getActivityStack() == null || bounds == null || bounds.isEmpty())) {
                    if (activity.inHwFreeFormWindowingMode()) {
                        activity.getActivityStack().setAlwaysOnTopOnly(isAlwaysOnTop);
                        activity.getActivityStack().resize(bounds, (Rect) null, (Rect) null);
                        this.mService.mStackSupervisor.mRootActivityContainer.ensureActivitiesVisible((ActivityRecord) null, 0, true);
                        this.mService.mStackSupervisor.mRootActivityContainer.resumeFocusedStacksTopActivities();
                        Rect rect = new Rect(activity.getActivityStack().getBounds());
                        Binder.restoreCallingIdentity(ident);
                        return rect;
                    }
                }
                return new Rect();
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public int getVirtualDisplayId(String castType) {
        if ("padCast".equals(castType)) {
            return this.mPadCastVirtualDisplayId;
        }
        return -1;
    }

    public boolean isVirtualDisplayId(int displayId, String castType) {
        int i;
        if (displayId != 0 && "padCast".equals(castType) && displayId == (i = this.mPadCastVirtualDisplayId) && i != -1) {
            return true;
        }
        return false;
    }

    public int getPadCastVirtualDisplayFirstStackId() {
        return this.mPadCastVirtualDisplayFirstStackId;
    }

    public boolean computeBounds(ActivityRecord activity, Rect outBounds) {
        if (activity.getDisplayId() == 0 && activity.mAtmService.mHwATMSEx.isSupportMagicRotatingScreen(activity.packageName)) {
            if (activity.mAppWindowToken == null || !activity.mAppWindowToken.isForcedPortraitOrientation() || this.mService.mContext.getResources().getConfiguration().orientation != 2 || activity.getWindowingMode() != 1) {
                outBounds.setEmpty();
            } else {
                showInMiddle(activity, outBounds, 0);
            }
            return true;
        } else if (!isVirtualDisplayId(activity.getDisplayId(), "padCast") || CAMERA_PACKAGE_NAME.equals(activity.packageName) || activity.inHwMagicWindowingMode() || HwPartMagicWindowServiceFactory.getInstance().getHwPartMagicWindowServiceFactoryImpl().getHwMagicWinManager().getHwMagicWinEnabled(1, activity.packageName) || activity.mAppWindowToken == null || !activity.mAppWindowToken.isForcedPortraitOrientation()) {
            return false;
        } else {
            showInMiddle(activity, outBounds, 1);
            return true;
        }
    }

    private void showInMiddle(ActivityRecord activity, Rect outBounds, int type) {
        int width;
        if (activity.getDisplay() != null && activity.getDisplay().mDisplayContent != null) {
            DisplayInfo displayInfo = activity.getDisplay().mDisplayContent.getDisplayInfo();
            Rect bounds = activity.mAtmService.mHwATMSEx.getHwMagicWinMiddleBounds(type);
            if (bounds == null || bounds.isEmpty()) {
                width = (displayInfo.logicalHeight * 3) / 4;
            } else {
                width = bounds.width();
            }
            outBounds.left = (displayInfo.logicalWidth - width) / 2;
            outBounds.right = outBounds.left + width;
            outBounds.bottom = displayInfo.logicalHeight;
            activity.mAppWindowToken.mIsNeedBackgroundSurface = true;
        }
    }

    public void finishRootActivity(ActivityRecord activity) {
        int displayId = activity.getDisplayId();
        if (isVirtualDisplayId(displayId, "padCast")) {
            if (activity.visible && activity.appToken != null && activity.getDisplay().getChildCount() == 1) {
                this.mService.mWindowManager.getWindowManagerServiceEx().takeTaskSnapshot(activity.appToken, false);
                this.mService.getTaskChangeNotificationController().notifyTaskStackChanged();
            }
            this.mService.mH.postDelayed(new Runnable(displayId) {
                /* class com.android.server.wm.$$Lambda$HwMultiDisplayManager$9VGb0_40ni16J7WNA_bqUFCzYkk */
                private final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    HwMultiDisplayManager.this.lambda$finishRootActivity$0$HwMultiDisplayManager(this.f$1);
                }
            }, 200);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: sendIdleMsg */
    public void lambda$finishRootActivity$0$HwMultiDisplayManager(int displayId) {
        ActivityDisplay padCastDisplay = this.mService.getRootActivityContainer().getActivityDisplay(displayId);
        if (padCastDisplay != null && padCastDisplay.topRunningActivity() == null && padCastDisplay.getChildCount() != 0) {
            this.mService.mStackSupervisor.scheduleIdleLocked();
        }
    }

    public void updatePadCastVirtualDisplayFirstStackId(ActivityStack stack) {
        ActivityDisplay activityDisplay;
        if (stack != null && isVirtualDisplayId(stack.mDisplayId, "padCast") && (activityDisplay = stack.getDisplay()) != null && !activityDisplay.mStacks.isEmpty() && !activityDisplay.mStacks.contains(stack) && this.mPadCastVirtualDisplayFirstStackId == stack.mStackId) {
            Slog.i(TAG, "updatePadCastVirtualDisplayFirstStackId top stack: " + activityDisplay.getTopStack().mStackId);
            this.mPadCastVirtualDisplayFirstStackId = activityDisplay.getTopStack().mStackId;
        }
    }

    public boolean moveStacksToDisplay(int fromDisplayId, int toDisplayId, boolean isOnlyFocus) {
        if (!isCallerHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", Binder.getCallingPid(), Binder.getCallingUid(), "moveStackToDisplay")) {
            return false;
        }
        Slog.i(TAG, "Attempt to move stack from display:" + fromDisplayId + ", to display:" + toDisplayId + ", isOnlyFocus: " + isOnlyFocus);
        return moveStacksToDisplay(fromDisplayId, toDisplayId, isOnlyFocus, true);
    }

    public boolean moveStacksToDisplay(int fromDisplayId, int toDisplayId, boolean isOnlyFocus, boolean onTop) {
        ActivityStack activityStack;
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mService.getGlobalLock()) {
                ActivityDisplay fromDisplay = this.mService.getRootActivityContainer().getActivityDisplay(fromDisplayId);
                ActivityDisplay toDisplay = this.mService.getRootActivityContainer().getActivityDisplay(toDisplayId);
                if (fromDisplay != null) {
                    if (toDisplay != null) {
                        ActivityStack fromFocus = fromDisplay.getFocusedStack();
                        if (fromFocus == null || !fromFocus.isActivityTypeHome()) {
                            if (fromFocus != null && toDisplayId == this.mPadCastVirtualDisplayId) {
                                this.mPadCastVirtualDisplayFirstStackId = fromFocus.mStackId;
                            }
                            if (isOnlyFocus) {
                                if (fromFocus != null) {
                                    this.mService.mWindowManager.startFreezingScreen(0, 0);
                                    moveAboveAppWindowsToDisplay(fromDisplay, toDisplay, fromFocus);
                                    fromFocus.reparent(toDisplay, onTop, true);
                                    if (!fromFocus.inMultiWindowMode() || fromFocus.inHwMagicWindowingMode()) {
                                        activityStack = null;
                                    } else {
                                        activityStack = null;
                                        fromFocus.setWindowingMode(0, false, false, false, true, false);
                                    }
                                    this.mService.mHwATMSEx.focusStackChange(this.mService.mRootActivityContainer.mCurrentUser, fromDisplayId, fromDisplay.getFocusedStack(), activityStack);
                                    fromFocus.postReparent();
                                    updateForSideScreen(fromDisplayId, toDisplayId);
                                    this.mService.mH.postDelayed(new Runnable() {
                                        /* class com.android.server.wm.$$Lambda$HwMultiDisplayManager$ZoxMScnEn4hGGl6E6unc9Tx7HkA */

                                        @Override // java.lang.Runnable
                                        public final void run() {
                                            HwMultiDisplayManager.this.lambda$moveStacksToDisplay$1$HwMultiDisplayManager();
                                        }
                                    }, 200);
                                    synchronized (this.mService.mGlobalLock) {
                                        notifyHoldScreenStateChangeInner();
                                    }
                                    Binder.restoreCallingIdentity(ident);
                                    return true;
                                }
                            } else if (fromDisplay.getChildCount() > 0) {
                                this.mService.mWindowManager.startFreezingScreen(0, 0);
                                moveAboveAppWindowsToDisplay(fromDisplay, toDisplay, null);
                                int stackNdx = 0;
                                int numStacks = fromDisplay.getChildCount();
                                while (stackNdx < numStacks) {
                                    ActivityStack stack = fromDisplay.getChildAt(stackNdx);
                                    stack.reparent(toDisplay, onTop, true);
                                    if (stack.inMultiWindowMode() && !stack.inHwMagicWindowingMode()) {
                                        stack.setWindowingMode(0, false, false, false, true, false);
                                    }
                                    int stackNdx2 = stackNdx - (numStacks - fromDisplay.getChildCount());
                                    numStacks = fromDisplay.getChildCount();
                                    if (numStacks == 0) {
                                        stack.postReparent();
                                    }
                                    stackNdx = stackNdx2 + 1;
                                }
                                updateForSideScreen(fromDisplayId, toDisplayId);
                                this.mService.mH.postDelayed(new Runnable() {
                                    /* class com.android.server.wm.$$Lambda$HwMultiDisplayManager$dc4Ddt3ilq7LkiWiu0SW5wfSbeI */

                                    @Override // java.lang.Runnable
                                    public final void run() {
                                        HwMultiDisplayManager.this.lambda$moveStacksToDisplay$2$HwMultiDisplayManager();
                                    }
                                }, 200);
                                synchronized (this.mService.mGlobalLock) {
                                    notifyHoldScreenStateChangeInner();
                                }
                                Binder.restoreCallingIdentity(ident);
                                return true;
                            }
                            synchronized (this.mService.mGlobalLock) {
                                notifyHoldScreenStateChangeInner();
                            }
                            Binder.restoreCallingIdentity(ident);
                            return false;
                        }
                        Slog.i(TAG, "Fail to move stack for home fromFocus:" + fromFocus);
                        synchronized (this.mService.mGlobalLock) {
                            notifyHoldScreenStateChangeInner();
                        }
                        Binder.restoreCallingIdentity(ident);
                        return false;
                    }
                }
                Slog.i(TAG, "Fail to move stack for fromDisplay:" + fromDisplay + ", toDisplay:" + toDisplay);
                synchronized (this.mService.mGlobalLock) {
                    notifyHoldScreenStateChangeInner();
                }
                Binder.restoreCallingIdentity(ident);
                return false;
            }
        } catch (Throwable th) {
            synchronized (this.mService.mGlobalLock) {
                notifyHoldScreenStateChangeInner();
                Binder.restoreCallingIdentity(ident);
                throw th;
            }
        }
    }

    public /* synthetic */ void lambda$moveStacksToDisplay$1$HwMultiDisplayManager() {
        this.mService.mWindowManager.stopFreezingScreen();
    }

    public /* synthetic */ void lambda$moveStacksToDisplay$2$HwMultiDisplayManager() {
        this.mService.mWindowManager.stopFreezingScreen();
    }

    private void updateForSideScreen(int fromDisplayId, int toDisplayId) {
        if (fromDisplayId == 0 || toDisplayId == 0) {
            ActivityDisplay activityDisplay = this.mService.getRootActivityContainer().getActivityDisplay(0);
            if (activityDisplay == null || activityDisplay.mDisplayContent == null) {
                Slog.w(TAG, "Fail to update for side screen");
                return;
            }
            WindowState windowState = activityDisplay.mDisplayContent.findVisibleUnfloatingModeWindow();
            if (windowState != null && HwDisplaySizeUtil.hasSideInScreen()) {
                WindowManagerPolicy policy = this.mService.mWindowManager.getPolicy();
                if (policy instanceof PhoneWindowManager) {
                    ((PhoneWindowManager) policy).notchControlFilletForSideScreen(windowState, true);
                }
            }
        }
    }

    public void moveAboveAppWindowsToDisplay(ActivityDisplay fromDisplay, ActivityDisplay toDisplay, ActivityStack focusStack) {
        if (fromDisplay == null || toDisplay == null) {
            Slog.w(TAG, "fromDisplay or toDisplay is null.");
            return;
        }
        DisplayContent fromDisplayContent = fromDisplay.mDisplayContent;
        DisplayContent toDisplayContent = toDisplay.mDisplayContent;
        if (fromDisplayContent == null || toDisplayContent == null) {
            Slog.w(TAG, "Fail to move AboveAppWindows for fromDisplay:" + fromDisplay + ", toDisplay:" + toDisplay);
            return;
        }
        Map<String, Set<Integer>> stackMap = new HashMap<>();
        if (focusStack != null) {
            fillDisplayStackMap(focusStack, stackMap);
        } else {
            int numStacks = fromDisplay.getChildCount();
            for (int stackNdx = 0; stackNdx < numStacks; stackNdx++) {
                fillDisplayStackMap(fromDisplay.getChildAt(stackNdx), stackMap);
            }
        }
        for (int i = fromDisplayContent.mAboveAppWindowsContainers.getChildCount() - 1; i >= 0; i--) {
            WindowToken windowToken = fromDisplayContent.mAboveAppWindowsContainers.getChildAt(i);
            WindowState win = windowToken.getTopChild();
            if ((win == null || win.mAttrs.type != 2009) && win != null && stackMap.containsKey(win.getOwningPackage()) && stackMap.get(win.getOwningPackage()).contains(Integer.valueOf(win.getPid()))) {
                windowToken.onDisplayChanged(toDisplayContent);
            }
        }
    }

    private void fillDisplayStackMap(ActivityStack stack, Map<String, Set<Integer>> stackMap) {
        if (stack != null && stack.topTask() != null) {
            fillActivityInfo(stack.getTopActivity(), stackMap);
            fillActivityInfo(stack.topTask().getRootActivity(), stackMap);
        }
    }

    private void fillActivityInfo(ActivityRecord activity, Map<String, Set<Integer>> stackMap) {
        if (activity != null) {
            String packageName = activity.packageName;
            if (!stackMap.containsKey(packageName)) {
                stackMap.put(packageName, new HashSet());
            }
            if (activity.app != null) {
                stackMap.get(packageName).add(Integer.valueOf(activity.app.getPid()));
            }
        }
    }

    private boolean isCallerHasPermission(String permission, int callingPid, int callingUid, String function) {
        ActivityTaskManagerService activityTaskManagerService = this.mService;
        boolean isAllowed = ActivityTaskManagerService.checkPermission(permission, callingPid, callingUid) == 0;
        if (!isAllowed) {
            Slog.i(TAG, "permission denied for " + function + ", callingPid:" + callingPid + ", callingUid:" + callingUid + ", requires: " + permission);
        }
        return isAllowed;
    }

    public void calcHwMultiWindowStackBoundsForConfigChange(ActivityStack stack, Rect outBounds, Rect oldStackBounds, int oldDisplayWidth, int oldDisplayHeight, int newDisplayWidth, int newDisplayHeight, boolean isModeChanged) {
        if (stack != null) {
            Slog.i(TAG, "stack: " + stack + " oldStackBounds " + oldStackBounds + ", oldDisplayWidth: " + oldDisplayWidth + ", oldDisplayHeight: " + oldDisplayHeight + ", newDisplayWidth: " + newDisplayWidth + ", newDisplayHeight: " + newDisplayHeight + ", isModeChanged: " + isModeChanged);
            if (!outBounds.equals(oldStackBounds) || (oldDisplayWidth == newDisplayWidth && oldDisplayHeight == newDisplayHeight)) {
                HashMap<Integer, ArrayList<Bundle>> taskMap = getMultiCastWindowTask();
                int taskSize = taskMap.size();
                int edgeLength = getDisplayEdge();
                TaskRecord topTask = stack.topTask();
                String pkgName = topTask == null ? "" : topTask.affinity;
                int taskId = topTask == null ? -1 : topTask.taskId;
                Slog.i(TAG, "calcHwMultiWindowStackBoundsForConfigChange pkgname:" + pkgName + " bound before adapt:" + outBounds + " task size:" + taskSize + " taskId " + taskId);
                boolean startFreeFormForLaucher = false;
                boolean startFreeFormForLaucher2 = stack.getWindowingMode() == 105;
                Iterator<Map.Entry<Integer, ArrayList<Bundle>>> it = taskMap.entrySet().iterator();
                while (it.hasNext()) {
                    boolean startFreeFormForLaucher3 = startFreeFormForLaucher2;
                    int i = 0;
                    boolean showInMiddle = startFreeFormForLaucher;
                    for (ArrayList<Bundle> array = it.next().getValue(); i < array.size(); array = array) {
                        Bundle window = array.get(i);
                        Rect rect = (Rect) window.getParcelable(KEY_PC_RECT);
                        int tmpTaskId = window.getInt(KEY_PC_TASKID);
                        if (rect.left >= edgeLength + edgeLength) {
                            showInMiddle = true;
                        }
                        if (this.mIsOverGPUSize && rect.left >= this.mOverGPUHeight + edgeLength) {
                            showInMiddle = true;
                        }
                        if (tmpTaskId == taskId) {
                            startFreeFormForLaucher3 = false;
                        }
                        i++;
                        it = it;
                    }
                    startFreeFormForLaucher2 = startFreeFormForLaucher3;
                    startFreeFormForLaucher = showInMiddle;
                    it = it;
                }
                if (startFreeFormForLaucher2) {
                    taskSize++;
                    Slog.i(TAG, "start pc freeform activity from laucher");
                }
                if (outBounds.left == 0 && taskSize <= sMultiCastWindowNumConfig) {
                    int offset = taskSize * edgeLength;
                    if (this.mIsOverGPUSize) {
                        if (taskSize > 1) {
                            offset = ((taskSize - 1) * this.mOverGPUHeight) + edgeLength;
                        } else {
                            offset = taskSize * edgeLength;
                        }
                        if (outBounds.width() < outBounds.height()) {
                            outBounds.set(0, 0, this.mOverGPUWidth, this.mOverGPUHeight);
                        } else {
                            outBounds.set(0, 0, this.mOverGPUHeight, this.mOverGPUWidth);
                        }
                    }
                    if (startFreeFormForLaucher) {
                        offset = edgeLength;
                    }
                    outBounds.offsetTo(outBounds.left + offset, outBounds.top);
                }
                Slog.i(TAG, "calcHwMultiWindowStackBoundsForConfigChange bound end outBounds = " + outBounds);
                calcHwFreeFormBoundsForLandWindow(pkgName, Math.max(newDisplayWidth, newDisplayHeight), outBounds);
            }
        }
    }

    private void calcHwFreeFormBoundsForLandWindow(String pkName, int landWindowWidth, Rect outBounds) {
        if (isSupportLandScapeInConfig(pkName)) {
            int landWindowHeight = Float.valueOf(((float) landWindowWidth) * (Float.valueOf((float) getPcHeight()).floatValue() / Float.valueOf((float) getPcWidth()).floatValue())).intValue();
            outBounds.right = outBounds.left + landWindowWidth;
            outBounds.bottom = outBounds.top + landWindowHeight;
            Slog.i(TAG, "calcHwFreeFormBoundsForLandWindow bound end outBounds = " + outBounds);
        }
    }

    public void setPCFullSize(int fullWidth, int fullHeight, int phoneOrientation) {
        if (isCallerHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", Binder.getCallingPid(), Binder.getCallingUid(), "setPCFullSize")) {
            if (phoneOrientation == 0) {
                this.mFullWidthForPort = fullWidth;
                this.mFullHeightForPort = fullHeight;
                Slog.i(TAG, "setPCFullSize mFullWidthForPort=" + this.mFullWidthForPort + ",mFullHeightForPort=" + this.mFullHeightForPort);
                return;
            }
            this.mFullWidthForLand = fullWidth;
            this.mFullHeightForLand = fullHeight;
            Slog.i(TAG, "setPCFullSize mFullWidthForLand=" + this.mFullWidthForLand + ",mFullHeightForLand=" + this.mFullHeightForLand);
        }
    }

    public void setPCVirtualSize(int virtualWidth, int virtualHeight, int phoneOrientation) {
        if (isCallerHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", Binder.getCallingPid(), Binder.getCallingUid(), "setPCVirtualSize")) {
            if (phoneOrientation == 0) {
                this.mVirtualWidthForPort = virtualWidth;
                this.mVirtualHeightForPort = virtualHeight;
                Slog.i(TAG, "setPCVirtualSize mVirtualWidthForPort=" + this.mVirtualWidthForPort + ",mVirtualHeightForPort=" + this.mVirtualHeightForPort);
                return;
            }
            this.mVirtualWidthForLand = virtualWidth;
            this.mVirtualHeightForLand = virtualHeight;
            Slog.i(TAG, "setPCVirtualSize mVirtualWidthForLand=" + this.mVirtualWidthForLand + ",mVirtualHeightForLand=" + this.mVirtualHeightForLand);
        }
    }

    public void setPCMultiCastMode(boolean isPCMultiCastMode) {
        if (!isCallerHasPermission(DEVICE_MULTI_DISPLAY_PERMISSION, Binder.getCallingPid(), Binder.getCallingUid(), "setPCMultiCastMode")) {
            Slog.i(TAG, "setPCMultiCastMode fail as permission denied");
        } else {
            setPCMultiCastModeInternal(isPCMultiCastMode);
        }
    }

    public void setCurOrientation(int curOrientation) {
        if (!isCallerHasPermission(DEVICE_MULTI_DISPLAY_PERMISSION, Binder.getCallingPid(), Binder.getCallingUid(), "setCurOrientation")) {
            Slog.i(TAG, "setCurOrientation fail as permission denied");
            return;
        }
        Slog.i(TAG, "setCurOrientation curOrientation=" + curOrientation);
        this.mCurOrientation = curOrientation;
    }

    public int getCurOrientation() {
        Slog.i(TAG, "getCurOrientation curOrientation=" + this.mCurOrientation);
        return this.mCurOrientation;
    }

    public int getPCFullWidth() {
        return this.mCurOrientation == 0 ? this.mFullWidthForPort : this.mFullWidthForLand;
    }

    public int getPCFullHeight() {
        return this.mCurOrientation == 0 ? this.mFullHeightForPort : this.mFullHeightForLand;
    }

    public int getPCVirtualWidth() {
        return this.mCurOrientation == 0 ? this.mVirtualWidthForPort : this.mVirtualWidthForLand;
    }

    public int getPCVirtualHeight() {
        return this.mCurOrientation == 0 ? this.mVirtualHeightForPort : this.mVirtualHeightForLand;
    }

    public boolean getPCMultiCastMode() {
        return this.mIsPCMultiCastMode;
    }

    public void registerMultiDisplayMessenger(Messenger messenger) {
        if (!isCallerHasPermission(DEVICE_MULTI_DISPLAY_PERMISSION, Binder.getCallingPid(), Binder.getCallingUid(), "registerMultiDisplayMessenger")) {
            Slog.i(TAG, "registerMultiDisplayMessenger fail as permission denied");
            return;
        }
        Slog.i(TAG, "registerMultiDisplayMessenger " + messenger);
        if (messenger != null) {
            synchronized (this.mMessengers) {
                if (!this.mMessengers.contains(messenger)) {
                    this.mMessengers.add(messenger);
                }
            }
        }
    }

    public void unregisterMultiDisplayMessenger(Messenger messenger) {
        if (!isCallerHasPermission(DEVICE_MULTI_DISPLAY_PERMISSION, Binder.getCallingPid(), Binder.getCallingUid(), "unregisterMultiDisplayMessenger")) {
            Slog.i(TAG, "unregisterMultiDisplayMessenger fail as permission denied");
            return;
        }
        Slog.i(TAG, "unregistMultiDisplayMessenger " + messenger);
        if (messenger != null) {
            synchronized (this.mMessengers) {
                this.mMessengers.remove(messenger);
            }
        }
    }

    public Rect getMaximizeWindowBounds() {
        Point realSize = new Point();
        ActivityTaskManagerService activityTaskManagerService = this.mService;
        if (activityTaskManagerService == null) {
            return null;
        }
        activityTaskManagerService.getUiContext().getDisplay().getRealSize(realSize);
        return new Rect(0, 48, realSize.x, realSize.y);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendToMessengers(int what, int arg1, int arg2, Object obj) {
        synchronized (this.mMessengers) {
            Iterator<Messenger> messengerIterator = this.mMessengers.iterator();
            while (messengerIterator.hasNext()) {
                try {
                    Message message = Message.obtain();
                    message.what = what;
                    message.arg1 = arg1;
                    message.arg2 = arg2;
                    message.obj = obj;
                    messengerIterator.next().send(message);
                } catch (RemoteException e) {
                    messengerIterator.remove();
                    Slog.i(TAG, "sendToMessengers RemoteException. what=" + what);
                }
            }
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: com.android.server.wm.HwMultiDisplayManager */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v0, types: [boolean, int] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public void onTaskRemoved(int taskId) {
        ?? fixMultiCastWindowAfterTaskRemoved = fixMultiCastWindowAfterTaskRemoved();
        Slog.i(TAG, "onTaskRemoved isNeedResizeDisplay  = " + (fixMultiCastWindowAfterTaskRemoved == true ? 1 : 0) + " removed taskid = " + taskId);
        sendToMessengers(2, taskId, fixMultiCastWindowAfterTaskRemoved, null);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean fixMultiCastWindowAfterTaskRemoved() {
        if (!checkMultiCastDisplayAbility(sMultiCastWindowNumConfig)) {
            return true;
        }
        for (Map.Entry<Integer, ArrayList<Bundle>> entry : getMultiCastWindowTask().entrySet()) {
            ArrayList<Bundle> array = entry.getValue();
            int index = 0;
            while (true) {
                if (index < array.size()) {
                    Rect rect = (Rect) array.get(index).getParcelable(KEY_PC_RECT);
                    int edgeLen = getDisplayEdge();
                    if (rect != null && rect.left >= sMultiCastWindowNumConfig * edgeLen) {
                        return false;
                    }
                    if (rect != null && this.mIsOverGPUSize && sMultiCastWindowNumConfig > 1 && rect.left >= ((sMultiCastWindowNumConfig - 1) * this.mOverGPUHeight) + edgeLen) {
                        return false;
                    }
                    index++;
                }
            }
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onTaskRequestedOrientationChanged(int taskId, int orientation) {
        Slog.i(TAG, "onTaskRequestedOrientationChanged, taskId: " + taskId + ", orientation=" + orientation);
        HashMap<Integer, ArrayList<Bundle>> taskMap = getMultiCastWindowTask();
        for (Map.Entry<Integer, ArrayList<Bundle>> entry : taskMap.entrySet()) {
            List<Bundle> array = entry.getValue();
            int listSize = array.size();
            int i = 0;
            while (true) {
                if (i < listSize) {
                    if (array.get(i).getInt(KEY_PC_TASKID) != taskId || i == listSize - 1) {
                        i++;
                    } else {
                        return;
                    }
                }
            }
        }
        if (!taskMap.isEmpty()) {
            Slog.i(TAG, "begin to request orientation update");
            sendToMessengers(4, taskId, orientation, null);
        }
    }

    public void onWindowModeChange(int taskId, Rect rect) {
        Slog.i(TAG, "onWindowModeChange, taskId: " + taskId + ", rect:" + rect);
        if (rect != null && rect.left >= getDisplayEdge()) {
            moveTaskToFrontWhenStart(taskId, rect.left);
        }
    }

    public void onTaskMovedToFront(ActivityManager.RunningTaskInfo runningTaskInfo) {
        Slog.i(TAG, "onTaskMovedToFront, task id : " + runningTaskInfo.taskId + ", task bounds : " + runningTaskInfo.bounds);
        moveTaskToFrontWhenStart(runningTaskInfo.taskId, runningTaskInfo.bounds.left);
    }

    private void takeTaskSnapshot(int taskId) {
        synchronized (this.mService.getGlobalLock()) {
            TaskRecord tr = this.mService.mRootActivityContainer.anyTaskForId(taskId);
            if (!(tr == null || tr.mTask == null)) {
                this.mService.mWindowManager.getTaskSnapshotController().snapshotTasks(Sets.newArraySet(new Task[]{tr.mTask}));
            }
        }
    }

    public void onTaskRemovalStarted(ActivityManager.RunningTaskInfo runningTaskInfo) {
        if (runningTaskInfo != null) {
            Slog.i(TAG, "onTaskRemovalStarted, taskId : " + runningTaskInfo.taskId + ", task bounds : " + runningTaskInfo.bounds);
            int taskId = runningTaskInfo.taskId;
            takeTaskSnapshot(taskId);
            List<Bundle> array = getMultiCastWindowTask().get(Integer.valueOf(runningTaskInfo.bounds.left));
            if (array != null && array.size() > 0) {
                for (int i = array.size() - 1; i >= 0; i--) {
                    Bundle taskBundle = array.get(i);
                    if (taskBundle.getBoolean(KEY_PC_VISIBLE)) {
                        int curTaskId = taskBundle.getInt(KEY_PC_TASKID);
                        Slog.i(TAG, "show top task when current task finished : " + curTaskId + ", last id : " + taskId);
                        if (taskId != curTaskId) {
                            sendToMessengers(3, curTaskId, taskId, null);
                            if (runningTaskInfo.isRunning) {
                                this.mService.mH.postDelayed(new Runnable(taskId) {
                                    /* class com.android.server.wm.$$Lambda$HwMultiDisplayManager$TgHbzBL0xZpueqcju9LHkrDbIUc */
                                    private final /* synthetic */ int f$1;

                                    {
                                        this.f$1 = r2;
                                    }

                                    @Override // java.lang.Runnable
                                    public final void run() {
                                        HwMultiDisplayManager.this.lambda$onTaskRemovalStarted$3$HwMultiDisplayManager(this.f$1);
                                    }
                                }, 200);
                                return;
                            }
                            return;
                        }
                        return;
                    }
                }
                Slog.i(TAG, "no task to be shown");
                hwTogglePhoneFullScreen(taskId);
            }
        }
    }

    public void moveTaskToFrontForMultiDisplay(int taskId) {
        if (isCallerHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", Binder.getCallingPid(), Binder.getCallingUid(), "moveTaskToFrontForMultiDisplay")) {
            Slog.i(TAG, "moveTaskToFrontForMultiDisplay, taskId: " + taskId);
            synchronized (this.mService.getGlobalLock()) {
                long origId = Binder.clearCallingIdentity();
                List<Bundle> taskBundles = getTaskList();
                Slog.i(TAG, "moveTaskToFrontForMultiDisplay, getTaskList: " + taskBundles);
                Rect currentRect = new Rect();
                Optional<Bundle> optional = taskBundles.stream().filter(new Predicate(taskId) {
                    /* class com.android.server.wm.$$Lambda$HwMultiDisplayManager$x21wDti3zVKe0aOXRsCcSLPOwx0 */
                    private final /* synthetic */ int f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // java.util.function.Predicate
                    public final boolean test(Object obj) {
                        return HwMultiDisplayManager.lambda$moveTaskToFrontForMultiDisplay$4(this.f$0, (Bundle) obj);
                    }
                }).findFirst();
                if (optional.isPresent()) {
                    currentRect = (Rect) optional.get().getParcelable(KEY_PC_RECT);
                }
                for (int index = taskBundles.size() - 1; index >= 0; index--) {
                    Bundle bundle = taskBundles.get(index);
                    Rect rect = (Rect) bundle.getParcelable(KEY_PC_RECT);
                    if (bundle.getBoolean(KEY_PC_FLOATING) && rect != null && rect.left == currentRect.left) {
                        moveTaskToFrontForMultiDisplayLocked(bundle.getInt(KEY_PC_TASKID));
                    }
                }
                Binder.restoreCallingIdentity(origId);
            }
        }
    }

    static /* synthetic */ boolean lambda$moveTaskToFrontForMultiDisplay$4(int taskId, Bundle task) {
        return task.getInt(KEY_PC_TASKID) == taskId;
    }

    public void moveTaskBackwardsForMultiDisplay(int taskId) {
        TaskRecord tr;
        if (isCallerHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", Binder.getCallingPid(), Binder.getCallingUid(), "moveTaskBackwardsForMultiDisplay")) {
            Slog.i(TAG, "moveTaskBackwardsForMultiDisplay, taskId: " + taskId);
            synchronized (this.mService.getGlobalLock()) {
                long origId = Binder.clearCallingIdentity();
                List<Bundle> taskBundles = getTaskList();
                Slog.i(TAG, "moveTaskBackwardsForMultiDisplay, getTaskList: " + taskBundles);
                Rect currentRect = new Rect();
                Optional<Bundle> optional = taskBundles.stream().filter(new Predicate(taskId) {
                    /* class com.android.server.wm.$$Lambda$HwMultiDisplayManager$wGLpecIXi0oyBN5jDSQaEKhFXlw */
                    private final /* synthetic */ int f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // java.util.function.Predicate
                    public final boolean test(Object obj) {
                        return HwMultiDisplayManager.lambda$moveTaskBackwardsForMultiDisplay$5(this.f$0, (Bundle) obj);
                    }
                }).findFirst();
                if (optional.isPresent()) {
                    currentRect = (Rect) optional.get().getParcelable(KEY_PC_RECT);
                }
                for (int index = 0; index <= taskBundles.size() - 1; index++) {
                    Bundle bundle = taskBundles.get(index);
                    Rect rect = (Rect) bundle.getParcelable(KEY_PC_RECT);
                    if (bundle.getBoolean(KEY_PC_FLOATING) && rect != null && rect.left == currentRect.left && (tr = this.mService.mRootActivityContainer.anyTaskForId(bundle.getInt(KEY_PC_TASKID))) != null) {
                        ActivityRecord activityRecord = tr.getTopActivity();
                        if (!(activityRecord == null || activityRecord.appToken == null)) {
                            this.mService.mWindowManager.getWindowManagerServiceEx().takeTaskSnapshot(activityRecord.appToken, false);
                        }
                        tr.getStack().moveTaskToBackLocked(bundle.getInt(KEY_PC_TASKID));
                    }
                }
                Binder.restoreCallingIdentity(origId);
            }
        }
    }

    static /* synthetic */ boolean lambda$moveTaskBackwardsForMultiDisplay$5(int taskId, Bundle task) {
        return task.getInt(KEY_PC_TASKID) == taskId;
    }

    public void setFocusedTaskForMultiDisplay(int taskId) {
        Rect bounds;
        if (isCallerHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", Binder.getCallingPid(), Binder.getCallingUid(), "setFocusedTaskForMultiDisplay")) {
            Slog.i(TAG, "setFocusedTask,taskId: " + taskId);
            synchronized (this.mService.getGlobalLock()) {
                TaskRecord task = this.mService.mRootActivityContainer.anyTaskForId(taskId);
                if (task != null && (bounds = task.getBounds()) != null && !bounds.isEmpty() && bounds.left > 0) {
                    List<Bundle> array = getMultiCastWindowTask().getOrDefault(Integer.valueOf(bounds.left), new ArrayList<>());
                    for (int idx = array.size() - 1; idx >= 0; idx--) {
                        int otherTaskId = array.get(idx).getInt(KEY_PC_TASKID);
                        if (otherTaskId != taskId) {
                            if (this.mService.mRootActivityContainer.anyTaskForId(otherTaskId) != null && task.mStack.getVisibility((ActivityRecord) null) == 1) {
                                Slog.i(TAG, "setFocusedTask,taskId: " + taskId + " cannot set focus!");
                                return;
                            }
                        }
                    }
                }
                this.mService.setFocusedTask(taskId);
            }
        }
    }

    public void hwResizeTaskForMultiDisplay(int taskId, Rect bounds) {
        if (isCallerHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", Binder.getCallingPid(), Binder.getCallingUid(), "hwResizeTaskForMultiDisplay")) {
            hwResizeTaskForMultiDisplayInternal(taskId, bounds);
        }
    }

    private void hwResizeTaskForMultiDisplayInternal(int taskId, Rect bounds) {
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mService.getGlobalLock()) {
                TaskRecord task = this.mService.mRootActivityContainer.anyTaskForId(taskId);
                task.resize(bounds, 3, true, false);
                if (task.getStack() != null && !bounds.equals(task.getStack().getBounds())) {
                    task.getStack().resize(bounds, (Rect) null, (Rect) null);
                    Slog.i(TAG, "hwResizeTask: resize stack bounds = " + task.getStack().getBounds());
                }
            }
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    private int getPCMultiCastTopFullScreenTaskId(List<Bundle> taskList) {
        int taskId = -1;
        for (int index = taskList.size() - 1; index >= 0; index--) {
            Bundle bundle = taskList.get(index);
            if (!bundle.getBoolean(KEY_PC_FLOATING) && bundle.getBoolean(KEY_PC_VISIBLE)) {
                Flog.bdReport(991310261, String.format("{packageName:%s}", bundle.getString(KEY_PC_PKGNAME)));
                Slog.i(TAG, "getPCMultiCastTopFullScreenTaskId, taskId = " + bundle.getInt(KEY_PC_TASKID) + ", name: " + bundle.getString(KEY_PC_PKGNAME) + ", isFloat: " + bundle.getBoolean(KEY_PC_FLOATING) + ", isVisible: " + bundle.getBoolean(KEY_PC_VISIBLE) + ", rect = " + bundle.getParcelable(KEY_PC_RECT));
                taskId = bundle.getInt(KEY_PC_TASKID);
                TaskRecord task = this.mService.mRootActivityContainer.anyTaskForId(taskId);
                if (task != null && (!task.inMultiWindowMode() || (HwFoldScreenManager.isFoldable() && task.inHwMagicWindowingMode()))) {
                    return bundle.getInt(KEY_PC_TASKID);
                }
            }
        }
        return taskId;
    }

    private String getPkNameByTaskId(int taskId) {
        String str;
        synchronized (this.mService.getGlobalLock()) {
            String pkName = "";
            TaskRecord tr = this.mService.mRootActivityContainer.anyTaskForId(taskId);
            if (tr != null) {
                ComponentName realActivity = tr.realActivity;
                pkName = realActivity == null ? "" : realActivity.getPackageName();
            }
            str = (String) Optional.ofNullable(pkName).orElse("");
        }
        return str;
    }

    public void hwTogglePCFloatWindow(int taskId) {
        if (isCallerHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", Binder.getCallingPid(), Binder.getCallingUid(), "hwTogglePCFloatWindow")) {
            Flog.bdReport(991310260);
            boolean toggleSuccess = false;
            synchronized (this.mService.getGlobalLock()) {
                Slog.i(TAG, "hwTogglePCFloatWindow, taskId: " + taskId);
                List<Bundle> taskRecordBundles = getTaskList();
                Slog.i(TAG, "hwTogglePCFloatWindow, getTaskList: " + taskRecordBundles);
                if (taskId == 0) {
                    int taskId2 = getPCMultiCastTopFullScreenTaskId(taskRecordBundles);
                    if (!isSupportPCMultiCastInConfig(getPkNameByTaskId(taskId2))) {
                        return;
                    }
                    if (taskId2 > 1) {
                        hwRestoreTaskForMultiDisplay(taskId2);
                        toggleSuccess = true;
                    }
                } else if (taskId < 0) {
                    int count = 0;
                    for (int index = this.mRecentTaskList.size() - 1; index >= 0; index--) {
                        int recentTaskId = this.mRecentTaskList.get(index).getInt(KEY_PC_TASKID, 0);
                        int taskIndex = taskRecordBundles.size() - 1;
                        while (true) {
                            if (taskIndex < 0) {
                                break;
                            }
                            Bundle taskBundle = taskRecordBundles.get(taskIndex);
                            int validTaskId = taskBundle.getInt(KEY_PC_TASKID);
                            if (recentTaskId != 0 && recentTaskId == validTaskId && !taskBundle.getBoolean(KEY_PC_FLOATING)) {
                                if (isSupportPCMultiCastInConfig(getPkNameByTaskId(taskBundle.getInt(KEY_PC_TASKID)))) {
                                    toggleSuccess = true;
                                    hwRestoreTaskForMultiDisplay(taskBundle.getInt(KEY_PC_TASKID));
                                    moveTaskToFrontForMultiDisplay(taskBundle.getInt(KEY_PC_TASKID));
                                    count++;
                                    if (count >= sMultiCastWindowNumConfig) {
                                        break;
                                    }
                                }
                            }
                            taskIndex--;
                        }
                    }
                    this.mRecentTaskList.clear();
                }
            }
            if (toggleSuccess) {
                InputMethodManagerInternalEx.hideCurrentInputMethod();
            }
        }
    }

    private void resumeCurrentBrightness() {
        Slog.i(TAG, "some key pressed, resumeCurrentBrightness");
        Bundle bundle = new Bundle();
        bundle.putBoolean("UpdateBrightnessEnable", true);
        int ret = HwPowerManager.setHwBrightnessData("ResetCurrentBrightness", bundle);
        if (ret != 0) {
            Slog.w(TAG, "resumeCurrentBrightness failed, ret = " + ret);
        }
    }

    private void setScreenPower(boolean isPowerOn) {
        try {
            IHwPCManager pcMgr = HwPCUtils.getHwPCManager();
            if (pcMgr != null && !pcMgr.isScreenPowerOn()) {
                Slog.i(TAG, "setScreenPower " + isPowerOn);
                pcMgr.setScreenPower(isPowerOn);
                resumeCurrentBrightness();
                this.mPowerManager.userActivity(SystemClock.uptimeMillis(), 0, 0);
            }
        } catch (RemoteException e) {
            Slog.i(TAG, "showSecureViewAndTurnScreenOn...");
        }
    }

    public void hwTogglePhoneFullScreen(int taskId) {
        if (isCallerHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", Binder.getCallingPid(), Binder.getCallingUid(), "hwTogglePhoneFullScreen")) {
            synchronized (this.mService.getGlobalLock()) {
                Slog.i(TAG, "hwTogglePhoneFullScreen, taskId: " + taskId);
                List<Bundle> taskBundles = getTaskList();
                Slog.i(TAG, "hwTogglePhoneFullScreen, getTaskList: " + taskBundles);
                if (taskId > 0) {
                    Rect currentRect = new Rect();
                    new Rect();
                    Optional<Bundle> optional = taskBundles.stream().filter(new Predicate(taskId) {
                        /* class com.android.server.wm.$$Lambda$HwMultiDisplayManager$mrPq6HvztGUvWrgCn62CXYhzrr0 */
                        private final /* synthetic */ int f$0;

                        {
                            this.f$0 = r1;
                        }

                        @Override // java.util.function.Predicate
                        public final boolean test(Object obj) {
                            return HwMultiDisplayManager.lambda$hwTogglePhoneFullScreen$6(this.f$0, (Bundle) obj);
                        }
                    }).findFirst();
                    if (optional.isPresent()) {
                        currentRect = (Rect) optional.get().getParcelable(KEY_PC_RECT);
                    }
                    for (int index = 0; index <= taskBundles.size() - 1; index++) {
                        Bundle bundle = taskBundles.get(index);
                        Rect rect = (Rect) bundle.getParcelable(KEY_PC_RECT);
                        if (bundle.getBoolean(KEY_PC_FLOATING) && rect != null && rect.left == currentRect.left) {
                            lambda$onTaskRemovalStarted$3$HwMultiDisplayManager(bundle.getInt(KEY_PC_TASKID));
                        }
                    }
                    return;
                }
                this.mRecentTaskList.clear();
                for (int index2 = taskBundles.size() - 1; index2 >= 0; index2--) {
                    Bundle bundle2 = taskBundles.get(index2);
                    if (bundle2.getBoolean(KEY_PC_FLOATING)) {
                        moveTaskBackwardsForMultiDisplay(bundle2.getInt(KEY_PC_TASKID));
                        lambda$onTaskRemovalStarted$3$HwMultiDisplayManager(bundle2.getInt(KEY_PC_TASKID));
                        this.mRecentTaskList.add(bundle2);
                    }
                }
            }
        }
    }

    static /* synthetic */ boolean lambda$hwTogglePhoneFullScreen$6(int taskId, Bundle task) {
        return task.getInt(KEY_PC_TASKID) == taskId;
    }

    private void hwRestoreTaskForMultiDisplay(int taskId) {
        long origId = Binder.clearCallingIdentity();
        try {
            this.mService.mWindowManager.startFreezingScreen(0, 0);
            TaskRecord tr = this.mService.mRootActivityContainer.anyTaskForId(taskId);
            if (tr != null) {
                ActivityStack activityStack = tr.getStack();
                if (activityStack != null && (activityStack.getWindowingMode() == 1 || (HwFoldScreenManager.isFoldable() && activityStack.inHwMagicWindowingMode()))) {
                    Slog.i(TAG, "GOOD TO GO, hwRestoreTaskForMultiDisplay, taskId: " + taskId);
                    activityStack.setWindowingMode(105);
                    hideMainWindowSecureView(activityStack);
                    showSecureViewIfNeed(activityStack);
                    if (HwDisplaySizeUtil.hasSideInScreen()) {
                        WindowManagerPolicy policy = this.mService.mWindowManager.getPolicy();
                        if ((policy instanceof PhoneWindowManager) && tr.getTopActivity() != null) {
                            PhoneWindowManager phoneWindowManager = (PhoneWindowManager) policy;
                            WindowState mainWindow = tr.getTopActivity().mAppWindowToken.findMainWindow(false);
                            if (mainWindow != null) {
                                phoneWindowManager.notchControlFilletForSideScreen(mainWindow, true);
                            }
                        }
                    }
                }
                ((ActivityTaskManagerService) this.mService).mH.postDelayed(new Runnable() {
                    /* class com.android.server.wm.$$Lambda$HwMultiDisplayManager$bfYhUGRxGSwEnlJTw6U2c7IcW30 */

                    @Override // java.lang.Runnable
                    public final void run() {
                        HwMultiDisplayManager.this.lambda$hwRestoreTaskForMultiDisplay$7$HwMultiDisplayManager();
                    }
                }, 200);
                Binder.restoreCallingIdentity(origId);
            }
        } finally {
            this.mService.mH.postDelayed(new Runnable() {
                /* class com.android.server.wm.$$Lambda$HwMultiDisplayManager$bfYhUGRxGSwEnlJTw6U2c7IcW30 */

                @Override // java.lang.Runnable
                public final void run() {
                    HwMultiDisplayManager.this.lambda$hwRestoreTaskForMultiDisplay$7$HwMultiDisplayManager();
                }
            }, 200);
            Binder.restoreCallingIdentity(origId);
        }
    }

    public /* synthetic */ void lambda$hwRestoreTaskForMultiDisplay$7$HwMultiDisplayManager() {
        this.mService.mWindowManager.stopFreezingScreen();
    }

    /* access modifiers changed from: private */
    /* renamed from: hwTogglePhoneFullScreenWindow */
    public void lambda$onTaskRemovalStarted$3$HwMultiDisplayManager(int taskId) {
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mService.getGlobalLock()) {
                TaskRecord task = this.mService.mRootActivityContainer.anyTaskForId(taskId);
                if (task == null) {
                    Slog.i(TAG, "hwTogglePhoneFullScreenWindow task is null");
                    return;
                }
                ActivityStack stack = task.getStack();
                if (stack == null) {
                    Slog.i(TAG, "hwTogglePhoneFullScreenWindow stack is null");
                    Binder.restoreCallingIdentity(ident);
                    return;
                }
                if (stack.inHwPCMultiStackWindowingMode()) {
                    this.mService.mWindowManager.startFreezingScreen(0, 0);
                    Slog.i(TAG, "hwTogglePhoneFullScreenWindow " + stack + " setWindowingMode to 1");
                    stack.setWindowingMode(1);
                    showSecureViewIfNeed(stack);
                    ActivityRecord ar = stack.topRunningActivityLocked();
                    if (ar != null) {
                        ar.ensureActivityConfiguration(0, false);
                    }
                    onTaskRemoved(taskId);
                    this.mService.mH.postDelayed(new Runnable() {
                        /* class com.android.server.wm.$$Lambda$HwMultiDisplayManager$hhYJdA4_LDrUVsp52iH6eDJAB0M */

                        @Override // java.lang.Runnable
                        public final void run() {
                            HwMultiDisplayManager.this.lambda$hwTogglePhoneFullScreenWindow$8$HwMultiDisplayManager();
                        }
                    }, 200);
                }
                Binder.restoreCallingIdentity(ident);
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public /* synthetic */ void lambda$hwTogglePhoneFullScreenWindow$8$HwMultiDisplayManager() {
        this.mService.mWindowManager.stopFreezingScreen();
    }

    public void hwTogglePhoneFullScreenFromLauncherOrRecent(int taskId) {
        for (Map.Entry<Integer, ArrayList<Bundle>> entry : getMultiCastWindowTask().entrySet()) {
            List<Bundle> array = entry.getValue();
            if (array.stream().filter(new Predicate(taskId) {
                /* class com.android.server.wm.$$Lambda$HwMultiDisplayManager$QW1fwrvgcKqVIVfyO1mzbJYIGo */
                private final /* synthetic */ int f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Predicate
                public final boolean test(Object obj) {
                    return HwMultiDisplayManager.lambda$hwTogglePhoneFullScreenFromLauncherOrRecent$9(this.f$0, (Bundle) obj);
                }
            }).findFirst().isPresent()) {
                int listSize = array.size();
                if (taskId == array.get(listSize - 1).getInt(KEY_PC_TASKID) && listSize > 1) {
                    moveTaskToFrontForMultiDisplayLocked(array.get(listSize - 2).getInt(KEY_PC_TASKID));
                }
                lambda$onTaskRemovalStarted$3$HwMultiDisplayManager(taskId);
                return;
            }
        }
    }

    static /* synthetic */ boolean lambda$hwTogglePhoneFullScreenFromLauncherOrRecent$9(int taskId, Bundle task) {
        return task.getInt(KEY_PC_TASKID) == taskId;
    }

    private void moveTaskToFrontForMultiDisplayLocked(int taskId) {
        Slog.i(TAG, "moveTaskToFrontForMultiDisplayLocked, taskId: " + taskId);
        synchronized (this.mService.getGlobalLock()) {
            TaskRecord tr = this.mService.mRootActivityContainer.anyTaskForId(taskId);
            if (tr != null) {
                tr.getStack().moveTaskToFrontLocked(tr, true, (ActivityOptions) null, (AppTimeTracker) null, "moveTaskToFront");
            }
        }
    }

    public void notifyFullScreenStateChange(int displayId, boolean isRequestFullScreen) {
        if (isVirtualDisplayId(displayId, "padCast") && isRequestFullScreen != this.mIsFullScreenPadCast) {
            Bundle bundle = new Bundle();
            bundle.putString("android.intent.extra.REASON", "fullScreenStateChange");
            bundle.putBoolean("isRequestFullScreen", isRequestFullScreen);
            bundle.putInt("displayId", displayId);
            bundle.putInt("android.intent.extra.user_handle", this.mService.mWindowManager.mCurrentUserId);
            this.mService.mHwATMSEx.call(bundle);
            this.mIsFullScreenPadCast = isRequestFullScreen;
            Slog.i(TAG, "notifyFullScreenStateChange mIsFullScreenPadCast=" + this.mIsFullScreenPadCast);
        }
    }

    public void notifyHoldScreenStateChange(String tag, int lockHash, int ownerUid, int ownerPid, String state) {
        this.mService.mH.post(new Runnable(state, tag, ownerUid, ownerPid, lockHash) {
            /* class com.android.server.wm.$$Lambda$HwMultiDisplayManager$2Kdq7tWwVDpmqge3FX62FGq1hoI */
            private final /* synthetic */ String f$1;
            private final /* synthetic */ String f$2;
            private final /* synthetic */ int f$3;
            private final /* synthetic */ int f$4;
            private final /* synthetic */ int f$5;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
            }

            @Override // java.lang.Runnable
            public final void run() {
                HwMultiDisplayManager.this.lambda$notifyHoldScreenStateChange$10$HwMultiDisplayManager(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5);
            }
        });
    }

    public /* synthetic */ void lambda$notifyHoldScreenStateChange$10$HwMultiDisplayManager(String state, String tag, int ownerUid, int ownerPid, int lockHash) {
        synchronized (this.mService.getGlobalLock()) {
            if ("acquire".equals(state)) {
                if (!"WindowManager".equals(tag) || ownerUid != 1000 || ownerPid != Process.myPid() || this.mService.mWindowManager.mRoot.mHoldScreenWindow == null || this.mService.mWindowManager.mRoot.mHoldScreenWindow.mSession == null) {
                    this.mHoldScreenLocks.put(Integer.valueOf(lockHash), new ScreenLock(lockHash, ownerUid, ownerPid));
                } else {
                    this.mHoldScreenLocks.put(Integer.valueOf(lockHash), new ScreenLock(lockHash, this.mService.mWindowManager.mRoot.mHoldScreenWindow.mSession.mUid, this.mService.mWindowManager.mRoot.mHoldScreenWindow.mSession.mPid));
                }
            }
            if ("release".equals(state)) {
                this.mHoldScreenLocks.remove(Integer.valueOf(lockHash));
            }
            notifyHoldScreenStateChangeInner();
        }
    }

    private void notifyHoldScreenStateChangeInner() {
        Map<Integer, Boolean> holdScreenDisplayStates = new HashMap<>();
        for (ScreenLock lock : this.mHoldScreenLocks.values()) {
            WindowProcessController wpc = this.mService.getProcessController(lock.mOwnerPid, lock.mOwnerUid);
            if (wpc != null) {
                holdScreenDisplayStates.put(Integer.valueOf(wpc.getActivityDisplayId()), true);
            }
        }
        try {
            if (this.mPadCastVirtualDisplayId != -1) {
                if (this.mHoldScreenDisplayStates.equals(holdScreenDisplayStates)) {
                    this.mHoldScreenDisplayStates = holdScreenDisplayStates;
                    return;
                }
                Set<Integer> displayIds = new HashSet<>();
                displayIds.addAll(this.mHoldScreenDisplayStates.keySet());
                displayIds.addAll(holdScreenDisplayStates.keySet());
                for (Integer num : displayIds) {
                    int displayId = num.intValue();
                    Boolean newValue = holdScreenDisplayStates.getOrDefault(Integer.valueOf(displayId), false);
                    if (!this.mHoldScreenDisplayStates.getOrDefault(Integer.valueOf(displayId), false).equals(newValue)) {
                        Bundle bundle = new Bundle();
                        bundle.putString("android.intent.extra.REASON", "holdScreenStateChange");
                        bundle.putInt("displayId", displayId);
                        bundle.putBoolean("isHoldScreen", newValue.booleanValue());
                        bundle.putInt("android.intent.extra.user_handle", this.mService.mWindowManager.mCurrentUserId);
                        this.mService.mHwATMSEx.call(bundle);
                        Slog.i(TAG, "notifyHoldScreenStateChange isHoldScreen=" + newValue + " displayId=" + displayId);
                    }
                }
                this.mHoldScreenDisplayStates = holdScreenDisplayStates;
            }
        } finally {
            this.mHoldScreenDisplayStates = holdScreenDisplayStates;
        }
    }

    public void notifyDisplayStacksEmpty(int displayId) {
        if (isVirtualDisplayId(displayId, "padCast")) {
            Bundle bundle = new Bundle();
            bundle.putString("android.intent.extra.REASON", "displayStacksEmpty");
            bundle.putInt("displayId", displayId);
            bundle.putInt("android.intent.extra.user_handle", this.mService.mWindowManager.mCurrentUserId);
            this.mService.mHwATMSEx.call(bundle);
            Slog.i(TAG, "notifyDisplayStacksEmpty displayId=" + displayId);
        }
    }

    public boolean isDisplayHoldScreen(int displayId) {
        boolean z;
        synchronized (this.mService.getGlobalLock()) {
            z = this.mHoldScreenDisplayStates.containsKey(Integer.valueOf(displayId)) && this.mHoldScreenDisplayStates.get(Integer.valueOf(displayId)).booleanValue();
        }
        return z;
    }

    public boolean isPadCastMaxSizeEnable() {
        if (Settings.System.getIntForUser(this.mService.mContext.getContentResolver(), "new_simple_mode", 0, this.mService.mWindowManager.mCurrentUserId) == 1) {
            Slog.i(TAG, "isPadCastMaxSizeEnable new_simple_mode");
            return false;
        } else if (Settings.System.getIntForUser(this.mService.mContext.getContentResolver(), "Simple mode", 0, this.mService.mWindowManager.mCurrentUserId) == 1) {
            Slog.i(TAG, "isPadCastMaxSizeEnable Simple mode");
            return false;
        } else if (Settings.Global.getInt(this.mService.mContext.getContentResolver(), "vdrive_is_run_state", 0) == 1) {
            Slog.i(TAG, "isPadCastMaxSizeEnable vdrive_is_run_state");
            return false;
        } else if (SystemProperties.getBoolean("super_power_save", false)) {
            Slog.i(TAG, "isPadCastMaxSizeEnable super_power_save");
            return false;
        } else if (Settings.Secure.getIntForUser(this.mService.mContext.getContentResolver(), ACCESSIBILITY_SCREENREADER_ENABLED, 0, this.mService.mWindowManager.mCurrentUserId) == 1) {
            Slog.i(TAG, "isPadCastMaxSizeEnable screen reader mode");
            return false;
        } else if (!TextUtils.isEmpty(Settings.Global.getString(this.mService.mContext.getContentResolver(), "single_hand_mode"))) {
            Slog.i(TAG, "isPadCastMaxSizeEnable single_hand_mode");
            return false;
        } else if (this.mService.mWindowManager.isInSubFoldScaleMode()) {
            Slog.i(TAG, "isPadCastMaxSizeEnable tahiti sub mode");
            return false;
        } else {
            long origId = Binder.clearCallingIdentity();
            try {
                synchronized (this.mService.getGlobalLock()) {
                    ActivityDisplay defaultDisplay = this.mService.mRootActivityContainer.getDefaultDisplay();
                    ActivityStack topStack = getTopStack(defaultDisplay);
                    if (topStack == null) {
                        Slog.i(TAG, "isPadCastMaxSizeEnable topStack is null");
                        return false;
                    } else if (topStack == defaultDisplay.getHomeStack()) {
                        Slog.i(TAG, "isPadCastMaxSizeEnable home is top");
                        Binder.restoreCallingIdentity(origId);
                        return false;
                    } else if (!topStack.inMultiWindowMode() || topStack.inHwMagicWindowingMode()) {
                        ActivityRecord topActivity = getTopActivity(topStack);
                        if (topActivity != null && CAMERA_PACKAGE_NAME.equals(topActivity.packageName)) {
                            Slog.i(TAG, "isPadCastMaxSizeEnable hw camera does not support maximum");
                            Binder.restoreCallingIdentity(origId);
                            return false;
                        } else if (topActivity == null || !getPadCastBlackList().contains(topActivity.packageName)) {
                            if (!(topActivity == null || topActivity.mAppWindowToken == null || !topActivity.mAppWindowToken.isForcedPortraitOrientation())) {
                                String packageName = (topActivity.task == null || topActivity.task.realActivity == null) ? topActivity.packageName : topActivity.task.realActivity.getPackageName();
                                if (!HwPartMagicWindowServiceFactory.getInstance().getHwPartMagicWindowServiceFactoryImpl().getHwMagicWinManager().getHwMagicWinEnabled(1, packageName)) {
                                    Slog.i(TAG, "isPadCastMaxSizeEnable not support magic window mode: " + packageName);
                                    Binder.restoreCallingIdentity(origId);
                                    return false;
                                }
                            }
                            if (topActivity != null && isAppHasLock(topActivity.packageName, topActivity.mUserId)) {
                                Slog.i(TAG, "isPadCastMaxSizeEnable app lock: " + topActivity.packageName);
                                Binder.restoreCallingIdentity(origId);
                                return false;
                            } else if (hasHwSecureWindowOnScreen(topStack)) {
                                Slog.i(TAG, "isPadCastMaxSizeEnable hasHwSecureWindowOnScreen");
                                Binder.restoreCallingIdentity(origId);
                                return false;
                            } else {
                                Binder.restoreCallingIdentity(origId);
                                return true;
                            }
                        } else {
                            Slog.i(TAG, "isPadCastMaxSizeEnable black list app: " + topActivity.packageName);
                            Binder.restoreCallingIdentity(origId);
                            return false;
                        }
                    } else {
                        Slog.i(TAG, "isPadCastMaxSizeEnable hw multi window visible");
                        Binder.restoreCallingIdentity(origId);
                        return false;
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(origId);
            }
        }
    }

    private ActivityRecord getTopActivity(ActivityStack topStack) {
        ArrayList<ActivityRecord> activities = topStack.topTask().mActivities;
        for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
            ActivityRecord activity = activities.get(activityNdx);
            if (!activity.finishing && (activity.fullscreen || !"com.android.permissioncontroller".equals(activity.packageName))) {
                return activity;
            }
        }
        return null;
    }

    private boolean hasHwSecureWindowOnScreen(ActivityStack topStack) {
        ArrayList<ActivityRecord> activities = topStack.topTask().mActivities;
        for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
            ActivityRecord activity = activities.get(activityNdx);
            if (!activity.finishing) {
                if (!activity.visible) {
                    return false;
                }
                if (activity.mAppWindowToken != null && activity.mAppWindowToken.hasHwSecureWindowOnScreen()) {
                    return true;
                }
            }
        }
        return false;
    }

    private ActivityStack getTopStack(ActivityDisplay defaultDisplay) {
        for (int stackNdx = defaultDisplay.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
            ActivityStack stack = defaultDisplay.getChildAt(stackNdx);
            if (stack.topRunningActivityLocked() != null) {
                return stack;
            }
        }
        return null;
    }

    private boolean isAppHasLock(String pgkName, int userId) {
        if (pgkName != null && Settings.Secure.getInt(this.mService.mContext.getContentResolver(), "app_lock_func_status", 0) == 1) {
            if (!(";" + Settings.Secure.getStringForUser(this.mService.mContext.getContentResolver(), "applock_unlocked_list", userId) + ";").contains(";" + pgkName + ";")) {
                if ((";" + Settings.Secure.getStringForUser(this.mService.mContext.getContentResolver(), "app_lock_list", userId) + ";").contains(";" + pgkName + ";")) {
                }
            }
            return true;
        }
        return false;
    }

    public boolean isMirrorCast(String castType) {
        boolean z = true;
        if ("padCast".equals(castType)) {
            synchronized (this.mService.getGlobalLock()) {
                ActivityDisplay padCastDisplay = this.mService.getRootActivityContainer().getActivityDisplay(this.mPadCastVirtualDisplayId);
                if (padCastDisplay != null) {
                    if (padCastDisplay.getChildCount() != 0) {
                        z = false;
                    }
                    return z;
                }
            }
        }
        return true;
    }

    public void moveStackToFrontEx(ActivityOptions options, ActivityStack stack) {
        if (stack != null && stack.mDisplayId != -1 && !stack.isActivityTypeHome() && this.mPadCastVirtualDisplayId != -1) {
            if ((options == null || options.getLaunchDisplayId() != this.mPadCastVirtualDisplayId) && stack.mDisplayId == this.mPadCastVirtualDisplayId) {
                ActivityDisplay toDisplay = this.mService.getRootActivityContainer().getDefaultDisplay();
                this.mService.mWindowManager.startFreezingScreen(0, 0);
                moveAboveAppWindowsToDisplay(this.mService.getRootActivityContainer().getActivityDisplay(this.mPadCastVirtualDisplayId), toDisplay, stack);
                stack.reparent(toDisplay, true, true);
                if (stack.inMultiWindowMode() && !stack.inHwMagicWindowingMode()) {
                    stack.setWindowingMode(0, false, false, false, true, false);
                }
                notifyHoldScreenStateChangeInner();
                this.mService.mH.postDelayed(new Runnable() {
                    /* class com.android.server.wm.$$Lambda$HwMultiDisplayManager$1EtvIaxZ7jx5EKNZYXUD6pNLs8g */

                    @Override // java.lang.Runnable
                    public final void run() {
                        HwMultiDisplayManager.this.lambda$moveStackToFrontEx$11$HwMultiDisplayManager();
                    }
                }, 200);
            } else if (options != null && options.getLaunchDisplayId() == this.mPadCastVirtualDisplayId && stack.mDisplayId != this.mPadCastVirtualDisplayId) {
                ActivityDisplay toDisplay2 = this.mService.getRootActivityContainer().getActivityDisplay(this.mPadCastVirtualDisplayId);
                ActivityDisplay fromDisplay = this.mService.getRootActivityContainer().getDefaultDisplay();
                if (toDisplay2 != null) {
                    this.mService.mWindowManager.startFreezingScreen(0, 0);
                    moveAboveAppWindowsToDisplay(fromDisplay, toDisplay2, stack);
                    stack.reparent(toDisplay2, true, true);
                    if (stack.inMultiWindowMode() && !stack.inHwMagicWindowingMode()) {
                        stack.setWindowingMode(0, false, false, false, true, false);
                    }
                    notifyHoldScreenStateChangeInner();
                    this.mService.mH.postDelayed(new Runnable() {
                        /* class com.android.server.wm.$$Lambda$HwMultiDisplayManager$ILAhF_W6FWvnEd4NDS7_pSepI8c */

                        @Override // java.lang.Runnable
                        public final void run() {
                            HwMultiDisplayManager.this.lambda$moveStackToFrontEx$12$HwMultiDisplayManager();
                        }
                    }, 200);
                }
            }
        }
    }

    public /* synthetic */ void lambda$moveStackToFrontEx$11$HwMultiDisplayManager() {
        this.mService.mWindowManager.stopFreezingScreen();
    }

    public /* synthetic */ void lambda$moveStackToFrontEx$12$HwMultiDisplayManager() {
        this.mService.mWindowManager.stopFreezingScreen();
    }

    public List<Bundle> getTaskList() {
        RootActivityContainer rootActivityContainer;
        boolean z;
        HwMultiDisplayManager hwMultiDisplayManager = this;
        String str = null;
        if (!hwMultiDisplayManager.isCallerHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", Binder.getCallingPid(), Binder.getCallingUid(), "getTaskList")) {
            return null;
        }
        synchronized (hwMultiDisplayManager.mService.getGlobalLock()) {
            RootActivityContainer rootActivityContainer2 = hwMultiDisplayManager.mService.mRootActivityContainer;
            ActivityDisplay activityDisplay = null;
            boolean z2 = true;
            if (rootActivityContainer2 != null) {
                int i = rootActivityContainer2.getChildCount() - 1;
                while (true) {
                    if (i < 0) {
                        break;
                    } else if (rootActivityContainer2.getChildAt(i).mDisplayId == 0) {
                        activityDisplay = rootActivityContainer2.getChildAt(i);
                        break;
                    } else {
                        i--;
                    }
                }
            }
            if (activityDisplay == null) {
                return null;
            }
            List<Bundle> latestTaskList = new ArrayList<>();
            boolean z3 = false;
            int stackNdx = 0;
            while (stackNdx < activityDisplay.getChildCount()) {
                ActivityStack stack = activityDisplay.getChildAt(stackNdx);
                TaskRecord tr = stack.topTask();
                if (tr == null) {
                    rootActivityContainer = rootActivityContainer2;
                    z = z3;
                } else {
                    Bundle taskRecordBundle = new Bundle();
                    taskRecordBundle.putInt(KEY_PC_TASKID, tr.taskId);
                    ComponentName realActivity = tr.realActivity;
                    String packageName = realActivity == null ? str : realActivity.getPackageName();
                    taskRecordBundle.putString(KEY_PC_PKGNAME, packageName);
                    int mode = tr.getWindowingMode();
                    boolean isPCWindowMode = mode == 105 ? z2 : z3;
                    boolean visible = stack.getVisibility(str) != 2 ? true : z3;
                    boolean resizeAble = (!tr.isResizeable() || tr.isActivityTypeHome() || !hwMultiDisplayManager.isSupportPCMultiCastInConfig(packageName)) ? z3 : true;
                    boolean isAppLock = hwMultiDisplayManager.isAppHasLock(packageName, tr.userId);
                    boolean isPCSizeable = hwMultiDisplayManager.isSupportTaskResizeByPCSizeInConfig(packageName) && hwMultiDisplayManager.isSupportDirectlyPCFullScreenInConfig(packageName);
                    rootActivityContainer = rootActivityContainer2;
                    taskRecordBundle.putBoolean(KEY_PC_FLOATING, isPCWindowMode);
                    taskRecordBundle.putBoolean(KEY_PC_VISIBLE, visible);
                    taskRecordBundle.putBoolean(KEY_PC_RESIZEABLE, resizeAble);
                    taskRecordBundle.putBoolean(KEY_PC_APPLOCK, isAppLock);
                    taskRecordBundle.putBoolean(KEY_PC_SUPPORT_PCSIZE_MAXIMIZED, isPCSizeable);
                    taskRecordBundle.putInt(KEY_PC_MULTI_WINDOW_MODE, mode);
                    taskRecordBundle.putParcelable(KEY_PC_RECT, new Rect(tr.getBounds()));
                    ActivityRecord topActivity = tr.getTopActivity();
                    if (topActivity != null) {
                        taskRecordBundle.putInt(KEY_PC_ORIENTATION, topActivity.getOrientation());
                        taskRecordBundle.putBoolean(KEY_PC_ISOVERLAY, (topActivity.intent.getHwFlags() & 16777216) != 0);
                        z = false;
                    } else {
                        taskRecordBundle.putInt(KEY_PC_ORIENTATION, -1);
                        Slog.i(TAG, "reset  taskId = " + tr.taskId + " orientation to SCREEN_ORIENTATION_UNSPECIFIED");
                        z = false;
                        taskRecordBundle.putBoolean(KEY_PC_ISOVERLAY, false);
                    }
                    latestTaskList.add(taskRecordBundle);
                }
                stackNdx++;
                str = null;
                z2 = true;
                z3 = z;
                rootActivityContainer2 = rootActivityContainer;
                hwMultiDisplayManager = this;
            }
            return latestTaskList;
        }
    }

    public int getCurTopFullScreenTaskState() {
        if (!isCallerHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", Binder.getCallingPid(), Binder.getCallingUid(), "getCurTopFullScreenTaskState")) {
            return -1;
        }
        synchronized (this.mService.getGlobalLock()) {
            List<Bundle> tasks = getTaskList();
            Slog.i(TAG, "getCurTopFullScreenTaskState, getTaskList: " + tasks);
            if (tasks == null) {
                return -1;
            }
            boolean hasFindTopTaskInScreen = false;
            for (int index = tasks.size() - 1; index >= 0; index--) {
                Bundle bundle = tasks.get(index);
                TaskRecord task = this.mService.mRootActivityContainer.anyTaskForId(bundle.getInt(KEY_PC_TASKID));
                if (task != null) {
                    if (!task.inMultiWindowMode()) {
                        if (!hasFindTopTaskInScreen && bundle.getBoolean(KEY_PC_VISIBLE)) {
                            hasFindTopTaskInScreen = true;
                            if (bundle.getBoolean(KEY_PC_RESIZEABLE)) {
                                if (isSupportPCMultiCastInConfig(getPkNameByTaskId(bundle.getInt(KEY_PC_TASKID)))) {
                                    if (bundle.getBoolean(KEY_PC_APPLOCK)) {
                                        onTaskStackChangedForMultiDisplay();
                                        return 2;
                                    }
                                }
                            }
                            return 3;
                        }
                    }
                }
            }
            Slog.i(TAG, "getCurTopFullScreenTaskState, Good can toggle pc window. PCAssistant should check whether exceed max nums");
            return 0;
        }
    }

    public int getCurPCWindowAreaNum() {
        if (!isCallerHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", Binder.getCallingPid(), Binder.getCallingUid(), "getCurPCWindowAreaNum")) {
            return -1;
        }
        List<Bundle> tasks = getTaskList();
        Slog.i(TAG, "getCurPCWindowAreaNum, getTaskList: " + tasks);
        Set<Integer> pcWindowAreaNum = new HashSet<>();
        for (int index = tasks.size() + -1; index >= 0; index--) {
            Bundle bundle = tasks.get(index);
            if (bundle.getBoolean(KEY_PC_FLOATING)) {
                Rect rect = (Rect) bundle.getParcelable(KEY_PC_RECT);
                int leftArea = rect != null ? rect.left : 0;
                if (leftArea != 0) {
                    pcWindowAreaNum.add(Integer.valueOf(leftArea));
                }
            }
        }
        return pcWindowAreaNum.size();
    }

    public List<Bundle> getLastRencentTaskList() {
        ArrayList<Bundle> arrayList;
        if (!isCallerHasPermission(DEVICE_MULTI_DISPLAY_PERMISSION, Binder.getCallingPid(), Binder.getCallingUid(), "getLastRencentTaskList")) {
            Slog.i(TAG, "getLastRencentTaskList failed as permission denied");
            return null;
        }
        synchronized (this.mService.getGlobalLock()) {
            arrayList = this.mRecentTaskList;
        }
        return arrayList;
    }

    public int retrievePCMultiWinConfig(String configXML) {
        if (!isCallerHasPermission(DEVICE_MULTI_DISPLAY_PERMISSION, Binder.getCallingPid(), Binder.getCallingUid(), "retrievePCMultiWinConfig")) {
            Slog.i(TAG, "retrievePCMultiWinConfig failed as permission denied");
            return 1;
        }
        this.mConfigSupportPCMultiCastList.clear();
        this.mConfigUnSupportPCMultiCastList.clear();
        this.mConfigVideosList.clear();
        this.mConfigResizeByPCSizeList.clear();
        this.mConfigDirectlyPCFullScreenList.clear();
        this.mConfigLandScapeList.clear();
        this.mConfigSupportMagicWindowList.clear();
        this.mConfigSupportRotateList.clear();
        HwPCMultiWinConfigLoader configLoader = HwPCMultiWinConfigLoader.getInstance(this.mService.mContext);
        int parseResult = configLoader.loadPcMultiWinConfigListFromXml(configXML);
        if (parseResult != 1) {
            loadPCMultiWinConfigPackages(configLoader.getPcMultiWinConfigList());
        }
        return parseResult;
    }

    private void loadPCMultiWinConfigPackages(List<Pair<String, Integer>> pcMultiWinConfigList) {
        if (!pcMultiWinConfigList.isEmpty()) {
            for (Pair<String, Integer> pcMultiWinConfig : pcMultiWinConfigList) {
                if (pcMultiWinConfig != null) {
                    int[] mask = {1, 2, 4, 16, 8, 32, 64, 128};
                    for (int index = 0; index < mask.length; index++) {
                        if ((((Integer) pcMultiWinConfig.second).intValue() & mask[index]) != 0) {
                            int i = mask[index];
                            if (i == 1) {
                                this.mConfigSupportPCMultiCastList.add((String) pcMultiWinConfig.first);
                            } else if (i == 2) {
                                this.mConfigUnSupportPCMultiCastList.add((String) pcMultiWinConfig.first);
                            } else if (i == 4) {
                                this.mConfigVideosList.add((String) pcMultiWinConfig.first);
                            } else if (i == 8) {
                                this.mConfigDirectlyPCFullScreenList.add((String) pcMultiWinConfig.first);
                            } else if (i == 16) {
                                this.mConfigResizeByPCSizeList.add((String) pcMultiWinConfig.first);
                            } else if (i == 32) {
                                this.mConfigLandScapeList.add((String) pcMultiWinConfig.first);
                            } else if (i == 64) {
                                this.mConfigSupportMagicWindowList.add((String) pcMultiWinConfig.first);
                            } else if (i == 128) {
                                this.mConfigSupportRotateList.add((String) pcMultiWinConfig.first);
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean isSupportPCMultiCastInConfig(String pkName) {
        List<String> list = this.mConfigSupportPCMultiCastList;
        if (list == null || list.isEmpty()) {
            return false;
        }
        return this.mConfigSupportPCMultiCastList.contains(pkName);
    }

    public List<String> getConfigSupportPCMultiCastList() {
        List<String> list = this.mConfigSupportPCMultiCastList;
        if (list == null || list.isEmpty()) {
            return new ArrayList();
        }
        return this.mConfigSupportPCMultiCastList;
    }

    private boolean isUnSupportPCMultiCastInConfig(String pkName) {
        List<String> list = this.mConfigUnSupportPCMultiCastList;
        if (list == null || list.isEmpty()) {
            return false;
        }
        return this.mConfigUnSupportPCMultiCastList.contains(pkName);
    }

    public boolean isVideosNeedFullScreenInConfig(String pkName) {
        List<String> list = this.mConfigVideosList;
        if (list == null || list.isEmpty()) {
            return false;
        }
        return this.mConfigVideosList.contains(pkName);
    }

    private boolean isSupportTaskResizeByPCSizeInConfig(String pkName) {
        List<String> list = this.mConfigResizeByPCSizeList;
        if (list == null || list.isEmpty()) {
            return false;
        }
        return this.mConfigResizeByPCSizeList.contains(pkName);
    }

    private boolean isSupportDirectlyPCFullScreenInConfig(String pkName) {
        List<String> list = this.mConfigDirectlyPCFullScreenList;
        if (list == null || list.isEmpty()) {
            return false;
        }
        return this.mConfigDirectlyPCFullScreenList.contains(pkName);
    }

    private boolean isSupportLandScapeInConfig(String pkName) {
        List<String> list = this.mConfigLandScapeList;
        if (list == null || list.isEmpty()) {
            return false;
        }
        return this.mConfigLandScapeList.contains(pkName);
    }

    private boolean isSupportMagicWindowInConfig(String pkName) {
        List<String> list = this.mConfigSupportMagicWindowList;
        if (list == null || list.isEmpty()) {
            return false;
        }
        return this.mConfigSupportMagicWindowList.contains(pkName);
    }

    private boolean isSupportRotateInConfig(String pkName) {
        List<String> list = this.mConfigSupportRotateList;
        if (list == null || list.isEmpty()) {
            return false;
        }
        return this.mConfigSupportRotateList.contains(pkName);
    }

    public void setPcSize(int pcWidth, int pcHeight) {
        if (!isCallerHasPermission(DEVICE_MULTI_DISPLAY_PERMISSION, Binder.getCallingPid(), Binder.getCallingUid(), "setPcSize")) {
            Slog.i(TAG, "setPcSize failed as permission denied");
        } else {
            setPcSizeInternal(pcWidth, pcHeight);
        }
    }

    public int getPcWidth() {
        return this.mPcWidth;
    }

    public int getPcHeight() {
        return this.mPcHeight;
    }

    private boolean checkMultiCastDisplayAbility(int size) {
        return size > 0 && size <= 2;
    }

    private int getDisplayEdge() {
        ActivityDisplay at = this.mService.mRootActivityContainer.getDefaultDisplay();
        if (at != null) {
            return HwActivityTaskManager.getDisplayEdge(at.mDisplay);
        }
        return HwActivityTaskManager.getDisplayEdge((Display) null);
    }

    private HashMap<Integer, ArrayList<Bundle>> getMultiCastWindowTask() {
        List<Bundle> list = getTaskList();
        Slog.i(TAG, "getMultiCastWindowTask, getTaskList: " + list);
        HashMap<Integer, ArrayList<Bundle>> taskMap = new HashMap<>();
        for (Bundle task : list) {
            if (task.getBoolean(KEY_PC_FLOATING)) {
                Rect rect = (Rect) task.getParcelable(KEY_PC_RECT);
                ArrayList<Bundle> activityArray = taskMap.getOrDefault(Integer.valueOf(rect.left), new ArrayList<>());
                activityArray.add(task);
                taskMap.put(Integer.valueOf(rect.left), activityArray);
            }
        }
        return taskMap;
    }

    @Deprecated
    public void adjustProcessGlobalConfigLocked(TaskRecord tr, Rect rect, int windowMode) {
    }

    public void updateTaskByRequestedOrientationForPCCast(int taskId, int requestedOrientation) {
        List<Bundle> list = getTaskList();
        Slog.i(TAG, "updateTaskByRequestedOrientationForPCCast, getTaskList: " + list);
        Optional<Bundle> optional = list.stream().filter(new Predicate(taskId) {
            /* class com.android.server.wm.$$Lambda$HwMultiDisplayManager$FgMl0n1DTAgFO1mjkd_km9_RV_U */
            private final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return HwMultiDisplayManager.lambda$updateTaskByRequestedOrientationForPCCast$13(this.f$0, (Bundle) obj);
            }
        }).findFirst();
        String pkName = getPkNameByTaskId(taskId);
        if (isSupportTaskResizeByPCSizeInConfig(pkName) && isSupportDirectlyPCFullScreenInConfig(pkName)) {
            sendCaptureScreenToPc(taskId, getTaskSnapshotBitmap(taskId, false));
        } else if (optional.isPresent()) {
            Bundle task = optional.get();
            Rect bounds = (Rect) task.getParcelable(KEY_PC_RECT);
            if (!isBoundsMatchOrientation(requestedOrientation, bounds) && task.getBoolean(KEY_PC_FLOATING)) {
                int width = bounds != null ? bounds.width() : 0;
                int height = bounds != null ? bounds.height() : 0;
                Rect newBounds = new Rect(bounds);
                newBounds.right = newBounds.left + height;
                newBounds.bottom = newBounds.top + width;
                sendCaptureScreenToPc(taskId, getTaskSnapshotBitmap(taskId, false));
                hwResizeTaskForMultiDisplayInternal(taskId, newBounds);
                Slog.i(TAG, "updateTaskByRequestedOrientation, resize floating task, new bounds = " + newBounds + ", requestedOrientation = " + requestedOrientation);
            }
        }
    }

    static /* synthetic */ boolean lambda$updateTaskByRequestedOrientationForPCCast$13(int taskId, Bundle task) {
        return task.getInt(KEY_PC_TASKID) == taskId;
    }

    private static boolean isOrientationMatch(int requestedOrientation, int orientation) {
        return (ActivityInfo.isFixedOrientationPortrait(requestedOrientation) && ActivityInfo.isFixedOrientationPortrait(orientation)) || (ActivityInfo.isFixedOrientationLandscape(requestedOrientation) && ActivityInfo.isFixedOrientationLandscape(orientation));
    }

    private boolean isAllowRequestedOrientation(int taskId) {
        return isSupportRotateInConfig(getPkNameByTaskId(taskId));
    }

    public void hwSetRequestedOrientation(int taskId, int orientation) {
        if (isCallerHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", Binder.getCallingPid(), Binder.getCallingUid(), "hwSetRequestedOrientation")) {
            synchronized (this.mService.getGlobalLock()) {
                Slog.i(TAG, "hwSetRequestedOrientation taskId  =" + taskId + " orientation = " + orientation);
                TaskRecord tr = this.mService.mRootActivityContainer.anyTaskForId(taskId);
                if (tr != null) {
                    if (tr.topRunningActivityLocked() != null) {
                        if (isAllowRequestedOrientation(taskId)) {
                            long origId = Binder.clearCallingIdentity();
                            try {
                                updateTaskByRequestedOrientationForPCCast(taskId, orientation);
                            } finally {
                                Binder.restoreCallingIdentity(origId);
                            }
                        }
                    }
                }
            }
        }
    }

    private void moveTaskToFrontWhenStart(int taskId, int boundLeft) {
        List<Bundle> array = getMultiCastWindowTask().getOrDefault(Integer.valueOf(boundLeft), null);
        if (array != null && array.size() > 0) {
            int lastTaskId = -1;
            int listSize = array.size();
            if (listSize > 1) {
                lastTaskId = array.get(listSize - 2).getInt(KEY_PC_TASKID);
            }
            int curTaskId = array.get(listSize - 1).getInt(KEY_PC_TASKID);
            Slog.i(TAG, "task move to front when start : " + curTaskId + ", last id : " + lastTaskId);
            if (curTaskId == taskId) {
                sendToMessengers(3, curTaskId, lastTaskId, null);
            }
        }
    }

    public void onTaskStackChangedForMultiDisplay() {
        boolean appLockStatus;
        int isResizable;
        int isAppLock;
        if (!this.mConfigSupportPCMultiCastList.isEmpty()) {
            List<Bundle> taskList = getTaskList();
            if (!taskList.isEmpty() && sMultiCastWindowNumConfig > 0) {
                String pkgName = "";
                Bundle msgData = new Bundle();
                int index = taskList.size() - 1;
                while (true) {
                    if (index < 0) {
                        appLockStatus = false;
                        break;
                    }
                    Bundle bundle = taskList.get(index);
                    TaskRecord task = this.mService.mRootActivityContainer.anyTaskForId(bundle.getInt(KEY_PC_TASKID));
                    if (task == null || task.inHwSplitScreenWindowingMode()) {
                        msgData.putInt(KEY_PC_CIR_SUPPORT, 0);
                        msgData.putInt(KEY_PC_RESIZEABLE, 0);
                        msgData.putInt(KEY_PC_APPLOCK, 0);
                        msgData.putString(KEY_PC_PKGNAME, "com.huawei.android.launcher");
                    } else if ((!task.inMultiWindowMode() || (HwFoldScreenManager.isFoldable() && task.inHwMagicWindowingMode())) && bundle.getBoolean(KEY_PC_VISIBLE)) {
                        pkgName = bundle.getString(KEY_PC_PKGNAME);
                        if (pkgName == null) {
                            Slog.i(TAG, "pkg name is empty, return");
                            return;
                        }
                        int circulationSupport = 0;
                        if (bundle.getBoolean(KEY_PC_RESIZEABLE) && !bundle.getBoolean(KEY_PC_APPLOCK)) {
                            circulationSupport = 1;
                        }
                        if (bundle.getBoolean(KEY_PC_RESIZEABLE)) {
                            isResizable = 1;
                        } else {
                            isResizable = 0;
                        }
                        if (!bundle.getBoolean(KEY_PC_APPLOCK)) {
                            appLockStatus = false;
                            isAppLock = 0;
                        } else if (this.mCanResendAppLock) {
                            this.mCanResendAppLock = false;
                            appLockStatus = true;
                            isAppLock = 1;
                        } else {
                            appLockStatus = false;
                            isAppLock = 1;
                        }
                        msgData.putInt(KEY_PC_CIR_SUPPORT, circulationSupport);
                        msgData.putInt(KEY_PC_RESIZEABLE, isResizable);
                        msgData.putInt(KEY_PC_APPLOCK, isAppLock);
                        msgData.putString(KEY_PC_PKGNAME, pkgName);
                    }
                    index--;
                }
                if (!pkgName.equals(this.mTopFullScreenPackageName) || appLockStatus) {
                    Slog.i(TAG, "pkg name:" + pkgName);
                    if (msgData.getInt(KEY_PC_APPLOCK) == 0) {
                        this.mCanResendAppLock = true;
                    }
                    this.mTopFullScreenPackageName = pkgName;
                    sendToMessengers(5, -1, -1, msgData);
                }
            }
        }
    }

    public void adjustActivityOptionsForPCCast(ActivityRecord ar, ActivityOptions options) {
        Rect optionsRect = options.getLaunchBounds();
        if (optionsRect != null && !optionsRect.isEmpty()) {
            Rect bounds = new Rect(optionsRect);
            ActivityDisplay at = this.mService.mRootActivityContainer.getDefaultDisplay();
            if (!(at == null || at.mDisplay == null)) {
                DisplayMetrics metric = new DisplayMetrics();
                at.mDisplay.getRealMetrics(metric);
                int width = Math.min(metric.widthPixels, metric.heightPixels);
                int height = Math.max(metric.widthPixels, metric.heightPixels);
                if (this.mIsOverGPUSize) {
                    width = this.mOverGPUWidth;
                    height = this.mOverGPUHeight;
                }
                if (ActivityInfo.isFixedOrientationPortrait(orientationFromBounds(bounds))) {
                    bounds.right = bounds.left + width;
                    bounds.bottom = bounds.top + height;
                } else {
                    bounds.right = bounds.left + height;
                    bounds.bottom = bounds.top + width;
                }
                options.setLaunchBounds(bounds);
            }
            if (!isBoundsMatchOrientation(resolveOrientation(ar), bounds)) {
                Rect adjustedBounds = new Rect(bounds);
                adjustedBounds.right = bounds.left + bounds.height();
                adjustedBounds.bottom = bounds.top + bounds.width();
                options.setLaunchBounds(adjustedBounds);
            }
            Slog.i(TAG, "adjustActivityOptionsForPCCast, bounds = " + bounds + ", adjustedBounds = " + options.getLaunchBounds());
        }
    }

    public void adjustOverlayActivityOptionsForPCCast(ActivityRecord ar, ActivityOptions options) {
        try {
            ActivityManager.StackInfo focusedStackInfo = this.mService.getFocusedStackInfo();
            if (focusedStackInfo != null && focusedStackInfo.configuration != null && focusedStackInfo.configuration.windowConfiguration != null && focusedStackInfo.configuration.windowConfiguration.inHwPCFreeFormWindowingMode() && focusedStackInfo.configuration.windowConfiguration.getBounds() != null) {
                options.setLaunchWindowingMode(105);
                options.setLaunchBounds(focusedStackInfo.configuration.windowConfiguration.getBounds());
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "adjustOverlayActivityOptionsForPCCast failed");
        }
    }

    public void handleActivityResumedForPCCast(ActivityRecord ar) {
        int orientation = ar.getOrientation();
        Rect bounds = new Rect(ar.getBounds());
        if (!isBoundsMatchOrientation(orientation, bounds)) {
            ar.setRequestedOrientation(orientation);
            Slog.i(TAG, "handleActivityResumedForPCCast, bounds = " + bounds + ", orientation = " + orientation);
        }
    }

    private static boolean isBoundsMatchOrientation(int orientation, Rect bounds) {
        if (bounds == null || bounds.isEmpty() || !isFixedOrientation(orientation)) {
            return true;
        }
        return isOrientationMatch(orientation, orientationFromBounds(bounds));
    }

    private static boolean isFixedOrientation(int orientation) {
        return ActivityInfo.isFixedOrientationLandscape(orientation) || ActivityInfo.isFixedOrientationPortrait(orientation);
    }

    private static boolean isFixedOrientationLocked(int orientation) {
        return orientation == 5 || orientation == 14;
    }

    private static int orientationFromBounds(Rect bounds) {
        if (bounds.width() > bounds.height()) {
            return 0;
        }
        return 1;
    }

    private static int resolveOrientation(ActivityRecord activity) {
        int orientation = activity.info.screenOrientation;
        if (ActivityInfo.isFixedOrientationLandscape(orientation)) {
            return 0;
        }
        if (ActivityInfo.isFixedOrientationPortrait(orientation)) {
            return 1;
        }
        if (isFixedOrientationLocked(orientation)) {
            return 14;
        }
        return -1;
    }

    public void setStackWindowingMode(IBinder token, int windowingMode, Rect bounds) {
        if (isCallerHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", Binder.getCallingPid(), Binder.getCallingUid(), "resizeActivityStack")) {
            long ident = Binder.clearCallingIdentity();
            try {
                synchronized (this.mService.getGlobalLock()) {
                    ActivityRecord activity = ActivityRecord.forTokenLocked(token);
                    if (activity != null) {
                        if (activity.getActivityStack() != null) {
                            Slog.i(TAG, "setStackWindowingMode activity: " + activity + ", windowingMode: " + windowingMode + ", bounds: " + bounds);
                            if ((activity.inHwFreeFormWindowingMode() && windowingMode == 1) || (!activity.inMultiWindowMode() && windowingMode == 102)) {
                                activity.getActivityStack().setWindowingMode(windowingMode, false, false, false, true, false);
                                if (WindowConfiguration.isHwFreeFormWindowingMode(windowingMode) && bounds != null && !bounds.isEmpty()) {
                                    activity.getActivityStack().resize(bounds, (Rect) null, (Rect) null);
                                }
                                if (windowingMode == 102) {
                                    activity.getActivityStack().moveToFront("setStackWindowingMode");
                                }
                                this.mService.mStackSupervisor.mRootActivityContainer.ensureActivitiesVisible((ActivityRecord) null, 0, true);
                                this.mService.mStackSupervisor.mRootActivityContainer.resumeFocusedStacksTopActivities();
                            }
                            Binder.restoreCallingIdentity(ident);
                        }
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showSecureViewIfNeed(ActivityStack stack) {
        synchronized (this.mService.getGlobalLock()) {
            if (stack == null) {
                stack = getTopStack(this.mService.mRootActivityContainer.getDefaultDisplay());
                if (stack == null) {
                    return;
                }
            }
            ActivityRecord top = stack.getTopActivity();
            if (!(top == null || top.mAppWindowToken == null || top.mAppWindowToken.findSecureWindow() == null || top.task == null)) {
                if (stack.getWindowingMode() == 1) {
                    setScreenPower(true);
                }
                showSecureReminderIfNeeded(top.task.taskId, stack.getWindowingMode());
            }
        }
    }

    private void hideMainWindowSecureView(ActivityStack stack) {
        synchronized (this.mService.getGlobalLock()) {
            if (stack != null) {
                ActivityRecord top = stack.getTopActivity();
                if (!(top == null || top.mAppWindowToken == null || top.mAppWindowToken.findSecureWindow() == null || top.task == null)) {
                    hideSecureReminderIfNeeded(top.task.taskId, 1);
                }
            }
        }
    }

    public void showAllReminderIfNeeded() {
        Slog.i(TAG, "showAllReminderIfNeeded msg: 9 , taskid= -1 , state 1 , mIsLockedViewShown " + this.mIsLockedViewShown);
        if (!this.mIsLockedViewShown) {
            sendToMessengers(9, -1, 1, null);
            this.mIsLockedViewShown = true;
        }
    }

    public void hideAllReminderIfNeeded() {
        Slog.i(TAG, "hideAllReminderIfNeeded msg: 9 , taskid= -1 , state 0 , mIsLockedViewShown " + this.mIsLockedViewShown);
        if (this.mIsLockedViewShown) {
            sendToMessengers(9, -1, 0, null);
            this.mIsLockedViewShown = false;
        }
    }

    public void showSecureReminderIfNeeded(int taskId, int mode) {
        boolean isFloat;
        Bundle msgData = new Bundle();
        if (mode == 105) {
            isFloat = true;
        } else {
            isFloat = false;
        }
        msgData.putBoolean(KEY_PC_FLOATING, isFloat);
        sendToMessengers(10, taskId, 1, msgData);
        Slog.i(TAG, "showSecureReminderIfNeeded msg: 10 , taskid= " + taskId + " , state 1 , isFloat " + isFloat);
    }

    public void hideSecureReminderIfNeeded(int taskId, int mode) {
        boolean isFloat;
        if (mode == 105) {
            isFloat = true;
        } else {
            isFloat = false;
        }
        Bundle msgData = new Bundle();
        msgData.putBoolean(KEY_PC_FLOATING, isFloat);
        sendToMessengers(10, taskId, 0, msgData);
        Slog.i(TAG, "hideSecureReminderIfNeeded msg: 10 , taskid= " + taskId + " , state 0 , isFloat " + isFloat);
    }

    public void sendMessageHint(int taskId) {
        sendToMessengers(12, taskId, -1, null);
    }

    public boolean isNewPcMultiCastMode() {
        if (MULTI_CAST_NUM > 0) {
            return this.mIsPCMultiCastMode;
        }
        return false;
    }

    public void onEnteringPipForMultiDisplay(int taskId) {
        sendToMessengers(7, taskId, 0, null);
    }

    public void onEnteringSingleHandForMultiDisplay() {
        sendToMessengers(8, 0, 0, null);
    }

    /* access modifiers changed from: private */
    public class BlurWallpaperCallback implements AbsWallpaperManagerInner.IBlurWallpaperCallback {
        public BlurWallpaperCallback() {
        }

        public void onBlurWallpaperChanged() {
            Slog.i(HwMultiDisplayManager.TAG, "onBlurWallpaperChanged...");
            HwMultiDisplayManager.this.sendWallpaperBlurBitmap();
            HwMultiDisplayManager.this.mService.mH.post(new Runnable() {
                /* class com.android.server.wm.$$Lambda$HwMultiDisplayManager$BlurWallpaperCallback$KYYZ9019F4GxdI9M2cSwhGjzE5s */

                @Override // java.lang.Runnable
                public final void run() {
                    HwMultiDisplayManager.BlurWallpaperCallback.this.lambda$onBlurWallpaperChanged$0$HwMultiDisplayManager$BlurWallpaperCallback();
                }
            });
        }

        public /* synthetic */ void lambda$onBlurWallpaperChanged$0$HwMultiDisplayManager$BlurWallpaperCallback() {
            synchronized (HwMultiDisplayManager.this.mService.mGlobalLock) {
                if (HwMultiDisplayManager.this.mPadCastWallpaperBitmap != null) {
                    HwMultiDisplayManager.this.mPadCastWallpaperBitmap.recycle();
                    HwMultiDisplayManager.this.mPadCastWallpaperBitmap = null;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendWallpaperBlurBitmap() {
        byte[] bitmapArray = getBlurBitmapArray(40960);
        if (bitmapArray == null || bitmapArray.length == 0) {
            Slog.e(TAG, "sendWallpaperBlurBitmap bitmap is null");
            return;
        }
        Bundle msgData = new Bundle();
        msgData.putByteArray(KEY_PC_BACKGROUND_BLUR, bitmapArray);
        sendToMessengers(11, -1, -1, msgData);
    }

    private byte[] getBlurBitmapArray(int maxSize) {
        DisplayMetrics metrics = new DisplayMetrics();
        ((DisplayManager) this.mService.mContext.getSystemService("display")).getDisplay(0).getRealMetrics(metrics);
        return getScreenShotBitmapArray(WallpaperManagerExt.getBlurBitmap(WallpaperManager.getInstance(this.mService.mContext), new Rect(0, 0, metrics.widthPixels, metrics.heightPixels)), maxSize);
    }

    private byte[] getScreenShotBitmapArray(Bitmap taskBitmap, int maxSize) {
        if (taskBitmap == null) {
            Slog.e(TAG, "getScreenShotBitmapArray error!!!");
            return new byte[0];
        }
        Matrix matrix = new Matrix();
        float scale = getScale(taskBitmap.getWidth(), taskBitmap.getHeight(), maxSize);
        matrix.setScale(scale, scale);
        Bitmap compressBitmap = Bitmap.createBitmap(taskBitmap, 0, 0, taskBitmap.getWidth(), taskBitmap.getHeight(), matrix, true);
        taskBitmap.recycle();
        if (compressBitmap == null) {
            Slog.e(TAG, "getBlurBitmapArray createBitmap error!!!");
            return new byte[0];
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        compressBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        int options = 95;
        while (baos.toByteArray().length > maxSize / 2) {
            baos.reset();
            compressBitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
            Slog.i(TAG, "bitmap is oversize size: " + baos.toByteArray().length);
            if (options <= 5) {
                break;
            }
            options -= 5;
        }
        byte[] datas = baos.toByteArray();
        Slog.i(TAG, "getBlurBitmapArray scale: " + scale + " width: " + compressBitmap.getWidth() + " height: " + compressBitmap.getHeight() + " size " + compressBitmap.getByteCount() + " len " + datas.length);
        compressBitmap.recycle();
        return datas;
    }

    private float getScale(int width, int height, int maxSize) {
        if (height == 0) {
            return 0.2f;
        }
        return (float) (Math.sqrt(((double) maxSize) / (4.0d * ((((double) width) * 1.0d) / ((double) height)))) / ((double) height));
    }

    public Rect adjustScreenShotRectForPCCast(Rect sourceCrop) {
        if (HwFoldScreenManager.isFoldable()) {
            return sourceCrop;
        }
        Rect localLayerRect = new Rect(getLocalLayerRectForMultiDisplay());
        Rect localDisplayRect = new Rect(getLocalDisplayRectForMultiDisplay());
        int pcFullWidth = localLayerRect.width();
        int pcFullHeight = localLayerRect.height();
        boolean isInHighResolutioin = localLayerRect.equals(localDisplayRect);
        if (pcFullWidth <= 0 || pcFullHeight <= 0 || sourceCrop == null || !isInHighResolutioin) {
            return sourceCrop;
        }
        Rect adjustedSourceCrop = new Rect(sourceCrop);
        DisplayInfo displayInfo = this.mService.mWindowManager.mDisplayManagerInternal.getDisplayInfo(0);
        if (displayInfo != null) {
            int logicalWidth = displayInfo.logicalWidth;
            int logicalHeight = displayInfo.logicalHeight;
            if (adjustedSourceCrop.isEmpty()) {
                adjustedSourceCrop.set(0, 0, logicalWidth, logicalHeight);
            }
            int rotation = displayInfo.rotation;
            if (rotation == 1) {
                adjustedSourceCrop.offset(pcFullHeight - logicalHeight, 0);
            } else if (rotation == 2) {
                adjustedSourceCrop.offset(pcFullWidth - logicalHeight, pcFullHeight - logicalWidth);
            } else if (rotation == 3) {
                adjustedSourceCrop.offset(0, pcFullWidth - logicalWidth);
            }
        }
        Slog.i(TAG, "adjustScreenShotRectForPCCast, sourceCrop = " + sourceCrop + ", adjustCrop = " + adjustedSourceCrop);
        return adjustedSourceCrop;
    }

    public void setMultiDisplayParamsWithType(int type, Bundle bundle) {
        if (!isCallerHasPermission(DEVICE_MULTI_DISPLAY_PERMISSION, Binder.getCallingPid(), Binder.getCallingUid(), "setMultiDisplayParamsWithType")) {
            Slog.i(TAG, "setMultiDisplayParamsWithType failed as permission denied");
            return;
        }
        Slog.i(TAG, "setMultiDisplayParamsWithType, type=" + type);
        if (type == 1) {
            handleSetDisplaySize(bundle);
        } else if (type == 2) {
            handleSetMinConfigNum(bundle);
        } else if (type == 3) {
            handleStartFloatWin(bundle);
        } else if (type != 4) {
            Slog.e(TAG, "setMultiDisplayParamsWithType, con not identify type=" + type);
        } else {
            handleSetOverGPUAttr(bundle);
        }
    }

    private void handleSetOverGPUAttr(Bundle bundle) {
        if (bundle != null) {
            Object object = bundle.getParcelable(KEY_DISPLAY_SIZE_PC_WINDOW_DISPLAY);
            if (object instanceof Rect) {
                int height = ((Rect) object).height();
                int width = ((Rect) object).width();
                this.mOverGPUHeight = Math.max(height, width);
                this.mOverGPUWidth = Math.min(height, width);
            }
            this.mIsOverGPUSize = bundle.getBoolean(KEY_IS_OVER_GPU_SIZE, false);
            Slog.i(TAG, "handleSetOverGPUAttr mOverGPUHeight = " + this.mOverGPUHeight + " mOverGPUWidth = " + this.mOverGPUWidth + " mIsOverGPUSize =" + this.mIsOverGPUSize);
        }
    }

    public Rect getLocalLayerRectForMultiDisplay() {
        return this.mCurOrientation == 0 ? this.mLocalLayerRectForPort : this.mLocalLayerRectForLand;
    }

    public Rect getLocalDisplayRectForMultiDisplay() {
        return this.mCurOrientation == 0 ? this.mLocalDisplayRectForPort : this.mLocalDisplayRectForLand;
    }

    public Rect getVirtualLayerRectForMultiDisplay() {
        return this.mCurOrientation == 0 ? this.mVirtualLayerRectForPort : this.mVirtualLayerRectForLand;
    }

    public Rect getVirtualDisplayRectForMultiDisplay() {
        return this.mCurOrientation == 0 ? this.mVirtualDisplayRectForPort : this.mVirtualDisplayRectForLand;
    }

    private void handleSetDisplaySize(Bundle bundle) {
        if (bundle == null) {
            Slog.e(TAG, "handleSetDisplaySize bundle is null");
            return;
        }
        Object objectLocalLayerPort = bundle.getParcelable(KEY_DISPLAY_SIZE_LOCAL_LAYER_RECT_PORT);
        if (objectLocalLayerPort instanceof Rect) {
            this.mLocalLayerRectForPort = (Rect) objectLocalLayerPort;
        }
        Object objectLocalDisplayPort = bundle.getParcelable(KEY_DISPLAY_SIZE_LOCAL_DISPLAY_RECT_PORT);
        if (objectLocalDisplayPort instanceof Rect) {
            this.mLocalDisplayRectForPort = (Rect) objectLocalDisplayPort;
        }
        Object objectVirtualLayerPort = bundle.getParcelable(KEY_DISPLAY_SIZE_VIRTUAL_LAYER_RECT_PORT);
        if (objectVirtualLayerPort instanceof Rect) {
            this.mVirtualLayerRectForPort = (Rect) objectVirtualLayerPort;
        }
        Object objectVirtualDisplayPort = bundle.getParcelable(KEY_DISPLAY_SIZE_VIRTUAL_DISPLAY_RECT_PORT);
        if (objectVirtualDisplayPort instanceof Rect) {
            this.mVirtualDisplayRectForPort = (Rect) objectVirtualDisplayPort;
        }
        Object objectLocalLayerLand = bundle.getParcelable(KEY_DISPLAY_SIZE_LOCAL_LAYER_RECT_LAND);
        if (objectLocalLayerLand instanceof Rect) {
            this.mLocalLayerRectForLand = (Rect) objectLocalLayerLand;
        }
        Object objectLocalDisplayLand = bundle.getParcelable(KEY_DISPLAY_SIZE_LOCAL_DISPLAY_RECT_LAND);
        if (objectLocalDisplayLand instanceof Rect) {
            this.mLocalDisplayRectForLand = (Rect) objectLocalDisplayLand;
        }
        Object objectVirtualLayerLand = bundle.getParcelable(KEY_DISPLAY_SIZE_VIRTUAL_LAYER_RECT_LAND);
        if (objectVirtualLayerLand instanceof Rect) {
            this.mVirtualLayerRectForLand = (Rect) objectVirtualLayerLand;
        }
        Object objectVirtualDisplayLand = bundle.getParcelable(KEY_DISPLAY_SIZE_VIRTUAL_DISPLAY_RECT_LAND);
        if (objectVirtualDisplayLand instanceof Rect) {
            this.mVirtualDisplayRectForLand = (Rect) objectVirtualDisplayLand;
        }
        Slog.i(TAG, "mLocalLayerRectForPort=" + this.mLocalLayerRectForPort + ", mLocalDisplayRectForPort=" + this.mLocalDisplayRectForPort + ", mVirtualLayerRectForPort=" + this.mVirtualLayerRectForPort + ", mVirtualDisplayRectForPort=" + this.mVirtualDisplayRectForPort + ", mLocalLayerRectForLand=" + this.mLocalLayerRectForLand + ", mLocalDisplayRectForLand=" + this.mLocalDisplayRectForLand + ", mVirtualLayerRectForLand=" + this.mVirtualLayerRectForLand + ", mVirtualDisplayRectForLand=" + this.mVirtualDisplayRectForLand);
    }

    public Bitmap getPadCastWallpaperBitmap(int displayId) {
        int padDisplayId;
        WallpaperManager wallpaperManager;
        if (displayId == 0) {
            padDisplayId = 0;
        } else if (this.mPadCastVirtualDisplayId == -1) {
            return null;
        } else {
            padDisplayId = this.mPadCastVirtualDisplayId;
        }
        if (this.mPadCastWallpaperBitmap == null && (wallpaperManager = WallpaperManager.getInstance(this.mService.mContext)) != null) {
            int defaultWidth = 1;
            int defaultHeight = 1;
            int padCastWidth = 1;
            int padCastHeight = 1;
            synchronized (this.mService.getGlobalLock()) {
                if (this.mService.mRootActivityContainer.getDefaultDisplay() != null) {
                    DisplayInfo defaultDisplayInfo = this.mService.mRootActivityContainer.getDefaultDisplay().mDisplayContent.getDisplayInfo();
                    defaultWidth = defaultDisplayInfo.logicalWidth;
                    defaultHeight = defaultDisplayInfo.logicalHeight;
                }
                ActivityDisplay padcastDisplay = ((ActivityTaskManagerService) this.mService).mRootActivityContainer.getActivityDisplay(padDisplayId);
                if (padcastDisplay != null) {
                    DisplayInfo padCastDisplayInfo = padcastDisplay.mDisplayContent.getDisplayInfo();
                    padCastWidth = padCastDisplayInfo.logicalWidth;
                    padCastHeight = padCastDisplayInfo.logicalHeight;
                }
            }
            Bitmap blurBitmap = wallpaperManager.getBlurBitmap(new Rect(0, 0, defaultWidth, defaultHeight));
            if (blurBitmap != null) {
                this.mPadCastWallpaperBitmap = Bitmap.createScaledBitmap(blurBitmap, padCastWidth, padCastHeight, true);
            }
        }
        return this.mPadCastWallpaperBitmap;
    }

    public void notifySecureStateChange(int displayId, boolean isSecure) {
        if (displayId != -1 && displayId == this.mPadCastVirtualDisplayId && this.mIsSecurePadCast != isSecure) {
            Bundle bundle = new Bundle();
            bundle.putString("android.intent.extra.REASON", "secureStateChange");
            bundle.putBoolean("isSecure", isSecure);
            bundle.putInt("displayId", displayId);
            bundle.putInt("android.intent.extra.user_handle", this.mService.mWindowManager.mCurrentUserId);
            this.mService.mHwATMSEx.call(bundle);
            this.mIsSecurePadCast = isSecure;
            Slog.i(TAG, "notifySecureStateChange mIsSecurePadCast=" + this.mIsSecurePadCast);
        }
    }

    private void handleSetMinConfigNum(Bundle bundle) {
        if (bundle == null) {
            Slog.e(TAG, "handleSetMinConfigNum bundle is null");
        } else {
            sMultiCastWindowNumConfig = bundle.getInt(KEY_PC_CONFIG_NUM);
        }
    }

    private boolean getTaskVisible(int taskId) {
        synchronized (this.mService.getGlobalLock()) {
            TaskRecord tr = this.mService.mRootActivityContainer.anyTaskForId(taskId);
            if (tr == null) {
                return false;
            }
            return tr.isVisible();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:35:0x008f, code lost:
        if (getTaskVisible(r7) == false) goto L_0x0099;
     */
    private void handleStartFloatWin(Bundle bundle) {
        Intent intent;
        String pkg;
        if (bundle != null && (intent = (Intent) bundle.getParcelable("intent")) != null) {
            String packageName = intent.getComponent() != null ? intent.getComponent().getPackageName() : "";
            if (TextUtils.isEmpty(packageName)) {
                Slog.i(TAG, "handleStartFloatWin packageName is empty");
            } else if (isAppHasLock(packageName, this.mService.mWindowManager.mCurrentUserId)) {
                sendToMessengers(18, 0, -1, null);
                Slog.i(TAG, "handleStartFloatWin send msg 18");
            } else {
                Iterator<ActivityManager.RunningTaskInfo> it = this.mService.getTasks(50).iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    ActivityManager.RunningTaskInfo task = it.next();
                    if (task.topActivity != null) {
                        pkg = task.topActivity.getPackageName();
                    } else {
                        pkg = "";
                    }
                    if (packageName.equals(pkg)) {
                        int windowingMode = task.windowMode;
                        int taskId = task.taskId;
                        if (windowingMode == 105) {
                            if (getTaskVisible(taskId)) {
                                setFocusedTaskForMultiDisplay(taskId);
                            } else {
                                moveTaskToFrontForMultiDisplay(taskId);
                            }
                        } else if (windowingMode == 1) {
                        }
                        sendToMessengers(17, windowingMode, -1, null);
                        return;
                    }
                }
                Slog.i(TAG, "handleStartFloatWin start: " + packageName);
                sendToMessengers(15, -1, -1, bundle);
            }
        }
    }

    public void captureScreenToPc(SurfaceControl.ScreenshotGraphicBuffer gb) {
        if (gb != null) {
            try {
                sendCaptureScreenToPc(0, Bitmap.wrapHardwareBuffer(gb.getGraphicBuffer(), gb.getColorSpace()));
            } catch (IllegalArgumentException e) {
                Slog.e(TAG, "captureScreenToPc get IllegalArgumentException" + e.getMessage());
            }
        }
    }

    /* access modifiers changed from: private */
    public class ScreenLock {
        int mLockHash;
        int mOwnerPid;
        int mOwnerUid;

        ScreenLock(int lockHash, int ownerUid, int ownerPid) {
            this.mLockHash = lockHash;
            this.mOwnerUid = ownerUid;
            this.mOwnerPid = ownerPid;
        }

        public int hashCode() {
            return this.mLockHash;
        }

        public boolean equals(Object obj) {
            return (obj instanceof ScreenLock) && this.mLockHash == ((ScreenLock) obj).mLockHash;
        }
    }

    private void sendCaptureScreenToPc(int taskId, Bitmap bitmap) {
        if (bitmap != null) {
            byte[] bitmapArray = getScreenShotBitmapArray(createGaussBitmap(bitmap), 40960);
            if (bitmapArray == null || bitmapArray.length == 0) {
                Slog.e(TAG, "sendCaptureScreenToPc bitmap is null");
                return;
            }
            Bundle msgData = new Bundle();
            msgData.putByteArray(KEY_PC_TASK_BITMAP_BLUR, bitmapArray);
            sendToMessengers(14, taskId, -1, msgData);
        }
    }

    private Bitmap getTaskSnapshotBitmap(int taskId, boolean reducedResolution) {
        ActivityManager.TaskSnapshot snapshot = HwActivityTaskManager.getTaskSnapshot(taskId, reducedResolution);
        if (snapshot == null) {
            return null;
        }
        return Bitmap.wrapHardwareBuffer(HardwareBuffer.createFromGraphicBuffer(snapshot.getSnapshot()), snapshot.getColorSpace());
    }

    private Bitmap createGaussBitmap(Bitmap bitmap) {
        return HwBlur.blur(bitmap, 200, 20, true);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetParamsInDisplayRemove() {
        hwTogglePhoneFullScreen(-1);
        setPCMultiCastModeInternal(false);
        this.mIsLockedViewShown = false;
        setPcSizeInternal(0, 0);
        this.mLocalLayerRectForPort.setEmpty();
        this.mLocalDisplayRectForPort.setEmpty();
        this.mVirtualLayerRectForPort.setEmpty();
        this.mVirtualDisplayRectForPort.setEmpty();
        this.mLocalLayerRectForLand.setEmpty();
        this.mLocalDisplayRectForLand.setEmpty();
        this.mVirtualLayerRectForLand.setEmpty();
        this.mVirtualDisplayRectForLand.setEmpty();
    }

    public void onWindowFocusChangedForMultiDisplay(AppWindowToken newFocus) {
        if (this.mPCCastVirtualDisplayId != -1 && newFocus != null) {
            Task newFocusTask = newFocus.getTask();
            if (newFocusTask == null) {
                Slog.e(TAG, "onWindowFocusChangedForMultiDisplay, newFocusTask is null!");
                return;
            }
            int taskId = newFocusTask.mTaskId;
            Slog.i(TAG, "onWindowFocusChangedForMultiDisplay, taskId = " + taskId);
            if (!newFocus.inHwPCMultiStackWindowingMode()) {
                taskId = 0;
            }
            Bundle msgData = new Bundle();
            msgData.putInt(KEY_PC_TYPE, 0);
            msgData.putInt(KEY_PC_TASKID, taskId);
            sendToMessengers(16, -1, -1, msgData);
        }
    }

    private void setPCMultiCastModeInternal(boolean isPCMultiCastMode) {
        Slog.i(TAG, "setPCMultiCastMode isPCMultiCastMode=" + isPCMultiCastMode);
        this.mIsPCMultiCastMode = isPCMultiCastMode;
        if (!this.mIsPCMultiCastMode) {
            this.mTopFullScreenPackageName = "";
        }
        SystemProperties.set("hw.multidisplay.mode.pc", Boolean.toString(this.mIsPCMultiCastMode));
    }

    private void setPcSizeInternal(int pcWidth, int pcHeight) {
        this.mPcWidth = pcWidth;
        this.mPcHeight = pcHeight;
    }
}
