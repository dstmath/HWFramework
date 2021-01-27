package com.huawei.server.magicwin;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.BadParcelableException;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.HwMwUtils;
import android.util.Log;
import com.android.server.am.ActivityManagerServiceEx;
import com.android.server.am.HwActivityManagerService;
import com.android.server.wm.ActivityRecordEx;
import com.android.server.wm.ActivityStackEx;
import com.android.server.wm.HwActivityTaskManagerServiceEx;
import com.android.server.wm.HwMagicContainer;
import com.android.server.wm.HwMagicWinManager;
import com.android.server.wm.HwMultiWindowManager;
import com.android.server.wm.WindowManagerServiceEx;
import com.huawei.android.content.ContextEx;
import com.huawei.android.fsm.HwFoldScreenManagerEx;
import com.huawei.android.os.FileUtilsEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.util.SlogEx;
import com.huawei.android.view.HwTaskSnapshotWrapper;
import com.huawei.hwpartmagicwindowservices.BuildConfig;
import com.huawei.internal.os.BackgroundThreadEx;
import com.huawei.server.utils.SharedParameters;
import com.huawei.server.utils.Utils;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

public class HwMagicWindowManagerService extends DefaultHwMagicWindowManagerService {
    public static final String ACTION_FROM_SYSTEMMANAGER = "huawei.intent.action.UPDATE_MAGIC_WINDOW_FEATURE_ACTION";
    public static final int ALARM_TIME_FOR_UPDATE_CONFIG = 4;
    public static final String ALARM_UPDATE_CONFIG_ACTION = "huawei.intent.action.ALARM_UPDATE_CONFIG_ACTION";
    private static final String APP_CONFIG_UPDATE_ACTION = "com.huawei.easygo.package_data_change";
    private static final String BUNDLE_GET_POS_INT = "int";
    private static final String BUNDLE_IS_IN_MAGIC_WIN_MODE = "boolean";
    private static final long CHECK_REPORT_MAGICWIN_USAGE_PERIOD = 1800000;
    public static final String CONFIG_URI = "configUri";
    private static final String DELAYED_UPDATE_CONFIG_PERMISSION = "com.huawei.permission.mgcw.UPDATE_CLOUD_CONFIG";
    private static final String EASYGO_LOGIN_STATUS_KEY = "@int:loginStatus";
    private static final String EASYGO_PERMISSION_SEND_SYS_BCST = "com.huawei.easygo.permission.SEND_SYS_BCST";
    private static final String EASYGO_REGISTER_FUNCTION_KEY = "function";
    private static final String EASYGO_REGISTER_FUNCTION_VALUE = "magicwindow";
    private static final String EASYGO_REGISTER_SERVICE_KEY = "server_service_name";
    private static final String EASYGO_REGISTER_SERVICE_VALUE = "com.android.server.magicwin.HwMagicWindowService";
    private static final String EASYGO_TARGET_POS_KEY = "@int:targetPosition";
    private static final String EASYGO_TASK_ID_KEY = "@int:taskId";
    private static final String FUNC_GET_TASK_POS = "getTaskPosition";
    private static final String FUNC_IS_IN_MAGIC_WIN_MODE = "isInMagicWindowMode";
    private static final String FUNC_SET_LOGIN_STATUS = "setLoginStatus";
    private static final String FUNC_SET_TASK_POS = "setTaskPosition";
    private static final String KEY_MULTI_RESUME = "android.allow_multiple_resumed_activities";
    private static final String MULTIWIN_FOR_CAMERA_PROP_KEY = "sys.multiwin_for_camera";
    private static final int PARAM_NUM_UPDATE_WALLPAPER = 1;
    public static final String SYSTEM_MANAGER_PERMISSION = "com.huawei.systemmanager.permission.ACCESS_INTERFACE";
    private static final String TAG = "HWMW_HwMagicWindowManagerService";
    private PendingIntent mAlarmIntent;
    private AlarmManager mAlarmManager;
    private final BroadcastReceiver mAlarmReceiver = new BroadcastReceiver() {
        /* class com.huawei.server.magicwin.HwMagicWindowManagerService.AnonymousClass8 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (!HwMagicWindowManagerService.this.isScreenOn()) {
                HwMagicWindowManagerService.this.mHandler.sendMessage(HwMagicWindowManagerService.this.mHandler.obtainMessage(4));
                HwMagicWindowManagerService.this.mAlarmManager.cancel(HwMagicWindowManagerService.this.mAlarmIntent);
                context.unregisterReceiver(this);
                return;
            }
            SlogEx.i(HwMagicWindowManagerService.TAG, "Ignore updating config because screen on. Will be rescheduled at 4 oclock next day.");
        }
    };
    private HwActivityManagerService mAms;
    private ActivityManagerServiceEx mAmsEx;
    private BroadcastReceiver mAppConfigUpdateReceiver = new BroadcastReceiver() {
        /* class com.huawei.server.magicwin.HwMagicWindowManagerService.AnonymousClass2 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            SlogEx.i(HwMagicWindowManagerService.TAG, "App Receive Config Update and loadAppAdapterInfo");
            if (intent != null) {
                Message msg = HwMagicWindowManagerService.this.mHandler.obtainMessage(7);
                Bundle bundle = null;
                try {
                    bundle = intent.getBundleExtra("params");
                } catch (BadParcelableException e) {
                    SlogEx.e(HwMagicWindowManagerService.TAG, "get Bundle extra error");
                }
                if (bundle == null) {
                    SlogEx.w(HwMagicWindowManagerService.TAG, "bundle of params is null");
                    return;
                }
                msg.obj = bundle.getString("client_name");
                HwMagicWindowManagerService.this.mHandler.sendMessage(msg);
            }
        }
    };
    private BroadcastReceiver mBdControlReceiver = new BroadcastReceiver() {
        /* class com.huawei.server.magicwin.HwMagicWindowManagerService.AnonymousClass3 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            HwMagicWindowManagerService.this.mHandler.sendMessage(HwMagicWindowManagerService.this.mHandler.obtainMessage(8, intent));
        }
    };
    private String mCloudConfigUri = null;
    private Context mContext = null;
    private HwFoldScreenManagerEx.FoldDisplayModeListener mDisplayListener = new HwFoldScreenManagerEx.FoldDisplayModeListener() {
        /* class com.huawei.server.magicwin.HwMagicWindowManagerService.AnonymousClass9 */

        public void onScreenDisplayModeChange(int displayMode) {
            SlogEx.d(HwMagicWindowManagerService.TAG, "receive display mode change to : " + displayMode);
            if (HwMagicWindowManagerService.this.mLastFoldMode != displayMode) {
                HwMagicWindowManagerService.this.mLastFoldMode = displayMode;
                if (displayMode == 2 || displayMode == 3) {
                    HwMagicWindowManagerService.this.mHandler.sendEmptyMessage(10);
                } else if (displayMode == 1) {
                    HwMagicWindowManagerService.this.mHandler.sendEmptyMessage(11);
                }
            }
        }
    };
    public final Handler mHandler = new Handler(BackgroundThreadEx.getHandler().getLooper()) {
        /* class com.huawei.server.magicwin.HwMagicWindowManagerService.AnonymousClass1 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (HwMagicWindowManagerService.this.mMwManager.isAnyContainerExist()) {
                        HwMagicWindowManagerService.this.mUIController.initWallpaperGaussBmp();
                        return;
                    }
                    return;
                case 2:
                    SlogEx.i(HwMagicWindowManagerService.TAG, "case Utils.MSG_UPDATE_MAGIC_WINDOW_CONFIG:");
                    HwMagicWindowManagerService.this.updateMagicWindowConfig();
                    HwMagicWindowManagerService.this.copyBlackListFile();
                    return;
                case 3:
                    SystemPropertiesEx.set(HwMagicWindowManagerService.MULTIWIN_FOR_CAMERA_PROP_KEY, String.format(Locale.ROOT, "%s", msg.obj));
                    return;
                case 4:
                    SlogEx.i(HwMagicWindowManagerService.TAG, "Update config at idle time.");
                    HwMagicWindowManagerService.this.applyNewCloudConfig();
                    return;
                case 5:
                    SlogEx.i(HwMagicWindowManagerService.TAG, "case Utils.MSG_USER_SWITCH:");
                    HwMagicWindowManagerService.this.registerEasyGoService();
                    if (HwMagicWindowManagerService.this.mMwManager.getLocalContainer() != null) {
                        HwMagicWindowManagerService.this.mMwManager.getLocalContainer().getConfig().onUserSwitch();
                        HwMagicWindowManagerService.this.mUIController.onUserSwitch();
                        HwMagicWindowManagerService.this.mMwManager.getAmsPolicy().getOrientationPolicy().updateSettings();
                        HwMagicWindowManagerService.this.mMwManager.getLocalContainer().calcHwSplitStackBounds();
                        return;
                    }
                    return;
                case Utils.MSG_FORCE_STOP_PACKAGE /* 6 */:
                    SlogEx.i(HwMagicWindowManagerService.TAG, "case Utils.MSG_FORCE_STOP_PACKAGE:");
                    HwMagicWindowManagerService.this.mMwManager.getAmsPolicy().removeRecentMagicWindowApp((String) msg.obj, 0);
                    return;
                case Utils.MSG_UPDATE_APP_CONFIG /* 7 */:
                    SlogEx.i(HwMagicWindowManagerService.TAG, "case Utils.MSG_UPDATE_APP_CONFIG:");
                    if (msg.obj != null && (msg.obj instanceof String)) {
                        if (HwMagicWindowManagerService.this.mMwManager.getLocalContainer() != null) {
                            HwMagicWindowManagerService.this.mMwManager.getLocalContainer().getConfig().loadAppAdapterInfo((String) msg.obj);
                        }
                        if (HwMagicWindowManagerService.this.mMwManager.getVirtualContainer() != null) {
                            HwMagicWindowManagerService.this.mMwManager.getVirtualContainer().getConfig().loadAppAdapterInfo((String) msg.obj);
                            return;
                        }
                        return;
                    }
                    return;
                case 8:
                    if (msg.obj != null && (msg.obj instanceof Intent)) {
                        String action = ((Intent) msg.obj).getAction();
                        if ("android.intent.action.SCREEN_OFF".equals(action)) {
                            SlogEx.i(HwMagicWindowManagerService.TAG, "Handle screen off");
                            Optional.ofNullable(HwMagicWindowManagerService.this.mMwManager.getLocalContainer()).ifPresent($$Lambda$HwMagicWindowManagerService$1$nD3Ftg2J5EEuL6bEFocSY1yYOQ.INSTANCE);
                            HwMagicContainer container = HwMagicWindowManagerService.this.mMwManager.getLocalContainer();
                            if (container != null) {
                                container.getCameraRotation().updateCameraRotation(-1);
                            }
                            HwMagicWindowManagerService.this.mMwManager.getAmsPolicy().pauseTopWhenScreenOff();
                        }
                        if ("android.intent.action.ACTION_SHUTDOWN".equals(action)) {
                            SlogEx.i(HwMagicWindowManagerService.TAG, "Handle shutdown");
                            HwMagicWinStatistics.stopTicks("shut_down");
                            HwMagicWinStatistics.saveCache();
                            return;
                        }
                        return;
                    }
                    return;
                case Utils.MSG_REPORT_USAGE_STATISTICS /* 9 */:
                    SlogEx.i(HwMagicWindowManagerService.TAG, "case Utils.MSG_REPORT_USAGE_STATISTICS:");
                    HwMagicWinStatistics.reportAll(HwMagicWindowManagerService.this.mContext);
                    HwMagicWindowManagerService.this.mHandler.sendEmptyMessageDelayed(9, HwMagicWindowManagerService.CHECK_REPORT_MAGICWIN_USAGE_PERIOD);
                    return;
                case Utils.MSG_MOVE_LATEST_TO_TOP /* 10 */:
                    SlogEx.i(HwMagicWindowManagerService.TAG, "case Utils.MSG_MOVE_LATEST_TO_TOP:");
                    HwMagicWindowManagerService.this.mMwManager.getAmsPolicy().mModeSwitcher.moveLatestActivityToTop(true, false);
                    return;
                case Utils.MSG_BACK_TO_FOLD_FULL_DISPLAY /* 11 */:
                    SlogEx.d(HwMagicWindowManagerService.TAG, "case Utils.MSG_BACK_TO_FOLD_FULL_DISPLAY:");
                    HwMagicWindowManagerService.this.mMwManager.getAmsPolicy().mModeSwitcher.backToFoldFullDisplay();
                    return;
                case Utils.MSG_UPDATE_STACK_VISIBILITY /* 12 */:
                    if (ActivityRecordEx.isActivityRecordInstance(msg.obj)) {
                        HwMagicWindowManagerService.this.mMwManager.getAmsPolicy().updateStackVisibility(ActivityRecordEx.createNewInstance(msg.obj), false);
                        return;
                    }
                    return;
                case Utils.MSG_START_RELATE_ACT /* 13 */:
                    SlogEx.i(HwMagicWindowManagerService.TAG, "case Utils.MSG_START_RELATE_ACT:");
                    HwMagicWindowManagerService.this.startRelateIntent(msg);
                    return;
                case Utils.MSG_ADJWIN_TOFULL_WHEN_RESUME /* 14 */:
                    HwMagicWindowManagerService.this.adjustWinToFullWhenResume(msg);
                    return;
                case Utils.MSG_WRITE_SETTING_XML /* 15 */:
                    if (HwMagicWindowManagerService.this.mMwManager.getLocalContainer() != null && (msg.obj instanceof String)) {
                        HwMagicWindowManagerService.this.mMwManager.getLocalContainer().getConfig().writeSetting((String) msg.obj);
                        return;
                    }
                    return;
                case 16:
                    HwMagicWindowManagerService.this.updateSystemBoundSize();
                    return;
                case Utils.MSG_SERVICE_INIT /* 17 */:
                    SlogEx.i(HwMagicWindowManagerService.TAG, "case Utils.MSG_SERVICE_INIT:");
                    HwMagicWindowManagerService.this.init();
                    return;
                case Utils.MSG_LOCAL_CHANGE /* 18 */:
                    HwMagicWindowManagerService.this.processLocaleChange();
                    return;
                case Utils.MSG_ONE_HOP_CONN_STATE /* 19 */:
                    HwMagicWindowManagerService.this.mMwManager.handleMsgOneHopConnState(msg);
                    return;
                case Utils.MSG_ONE_HOP_SVC_STATE /* 20 */:
                    HwMagicWindowManagerService.this.mMwManager.handleMsgOneHopSvcConnState(msg);
                    return;
                case Utils.MSG_ONE_HOP_CONFIG_FILES /* 21 */:
                    if (msg.obj instanceof List) {
                        HwMagicWindowManagerService.this.mMwManager.handleConfigFiles((List) msg.obj);
                        return;
                    }
                    return;
                case Utils.MSG_ONE_HOP_APP_SWITCH_CHANGE /* 22 */:
                    if (msg.obj instanceof String) {
                        HwMagicWindowManagerService.this.mMwManager.syncAppSwitch((String) msg.obj);
                        return;
                    }
                    return;
                case Utils.MSG_SET_CAMERA_FULLSCREEN /* 23 */:
                    SystemPropertiesEx.set(Utils.SYS_OSD_CANERA, String.format(Locale.ROOT, "%s", msg.obj));
                    return;
                case Utils.MSG_SCHEDULE_IDLE /* 24 */:
                    SlogEx.i(HwMagicWindowManagerService.TAG, "case Utils.MSG_SCHEDULE_IDLE");
                    HwMagicWindowManagerService.this.mAmsEx.getActivityTaskManagerEx().scheduleIdleLockedFromStackSupervisor();
                    return;
                default:
                    return;
            }
        }
    };
    private BroadcastReceiver mInstallReceiver = new BroadcastReceiver() {
        /* class com.huawei.server.magicwin.HwMagicWindowManagerService.AnonymousClass7 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (context == null || intent == null || intent.getData() == null) {
                SlogEx.e(HwMagicWindowManagerService.TAG, "InstallReceiver context or intent or data is null");
                return;
            }
            Utils.dbg(Utils.TAG_SETTING, "onAppInstalled");
            HwMagicWindowManagerService.this.mMwManager.addInstallAppConfig(intent.getData().getSchemeSpecificPart());
        }
    };
    private boolean mIsCloudConfigChanged = false;
    private boolean mIsInited = false;
    private int mLastFoldMode = 0;
    private BroadcastReceiver mLocaleChangeReceiver = new BroadcastReceiver() {
        /* class com.huawei.server.magicwin.HwMagicWindowManagerService.AnonymousClass10 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            HwMagicContainer container = HwMagicWindowManagerService.this.mMwManager.getLocalContainer();
            if (container != null) {
                boolean isRtl = true;
                if (TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) != 1) {
                    isRtl = false;
                }
                if (container.getConfig().isRtl() != isRtl) {
                    SlogEx.i(HwMagicWindowManagerService.TAG, "LocaleChanged, isRtl changed to " + isRtl);
                    container.getConfig().setIsRtl(isRtl);
                    HwMagicWindowManagerService.this.mHandler.sendEmptyMessage(18);
                }
            }
        }
    };
    private HwMagicWinManager mMwManager;
    private PowerManager mPowerManager;
    private HwMagicWindowUIController mUIController = null;
    private BroadcastReceiver mUnistallReciver = new BroadcastReceiver() {
        /* class com.huawei.server.magicwin.HwMagicWindowManagerService.AnonymousClass6 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (context == null || intent == null || intent.getData() == null) {
                SlogEx.e(HwMagicWindowManagerService.TAG, "UnistallReciver context or intent or data is null");
                return;
            }
            String pkgName = intent.getData().getSchemeSpecificPart();
            int userId = 0;
            try {
                userId = intent.getIntExtra("android.intent.extra.user_handle", 0);
            } catch (BadParcelableException e) {
                SlogEx.e(HwMagicWindowManagerService.TAG, "get Int extra error");
            }
            HwMagicWindowManagerService.this.mMwManager.getAmsPolicy().mMagicWinSplitMng.removeReportLoginStatus(HwMagicWindowManagerService.this.mMwManager.getAmsPolicy().getJoinStr(pkgName, userId));
            if (UserHandleEx.isClonedProfile(userId)) {
                SlogEx.d(HwMagicWindowManagerService.TAG, "Do not remove data for " + pkgName + " cloned profile");
                return;
            }
            boolean isReplace = false;
            try {
                isReplace = intent.getBooleanExtra("android.intent.extra.REPLACING", false);
            } catch (BadParcelableException e2) {
                SlogEx.e(HwMagicWindowManagerService.TAG, "get Boolean extra error");
            }
            HwMagicWindowManagerService.this.mMwManager.removeUninstallAppConfig(pkgName, Boolean.valueOf(isReplace));
        }
    };
    private final BroadcastReceiver mUpdateMagicWindowConfigBdReceiver = new BroadcastReceiver() {
        /* class com.huawei.server.magicwin.HwMagicWindowManagerService.AnonymousClass4 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            try {
                HwMagicWindowManagerService.this.mCloudConfigUri = intent.getStringExtra(HwMagicWindowManagerService.CONFIG_URI);
            } catch (BadParcelableException e) {
                SlogEx.e(HwMagicWindowManagerService.TAG, "get String extra error");
            }
            SlogEx.i(HwMagicWindowManagerService.TAG, "mCloudConfigUri : " + HwMagicWindowManagerService.this.mCloudConfigUri);
            if (HwMagicWindowManagerService.this.mCloudConfigUri != null) {
                HwMagicWindowManagerService.this.mHandler.sendMessage(HwMagicWindowManagerService.this.mHandler.obtainMessage(2));
            }
        }
    };
    private BroadcastReceiver mUserSwitchReceiver = new BroadcastReceiver() {
        /* class com.huawei.server.magicwin.HwMagicWindowManagerService.AnonymousClass5 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            HwMagicWindowManagerService.this.mHandler.sendMessage(HwMagicWindowManagerService.this.mHandler.obtainMessage(5));
        }
    };
    private WindowManagerServiceEx mWmsEx;

    public HwMagicWindowManagerService(Context context, ActivityManagerServiceEx amsEx, WindowManagerServiceEx wmsEx) {
        super(context, amsEx, wmsEx);
        this.mAmsEx = amsEx;
        this.mAms = amsEx.switchToHwActivityManagerService();
        this.mWmsEx = wmsEx;
        this.mContext = context;
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        this.mHandler.sendEmptyMessage(17);
        registerUserSwitchReceiver();
    }

    private void registerLocaleChangeReceiver() {
        ContextEx.registerReceiverAsUser(this.mContext, this.mLocaleChangeReceiver, UserHandleEx.CURRENT, new IntentFilter("android.intent.action.LOCALE_CHANGED"), (String) null, (Handler) null);
    }

    private void registerFoldDisplayReceiver() {
        if (HwMwUtils.IS_FOLD_SCREEN_DEVICE) {
            HwFoldScreenManagerEx.registerFoldDisplayMode(this.mDisplayListener);
        }
    }

    private void registerBdControlReceiver() {
        IntentFilter bdControlFilter = new IntentFilter("android.intent.action.SCREEN_OFF");
        bdControlFilter.addAction("android.intent.action.ACTION_SHUTDOWN");
        this.mContext.registerReceiver(this.mBdControlReceiver, bdControlFilter);
    }

    private void registerAppConfigUpdateReceiver() {
        try {
            this.mContext.unregisterReceiver(this.mAppConfigUpdateReceiver);
        } catch (IllegalArgumentException e) {
            SlogEx.e(TAG, "unregisterReceiver AppConfig fail");
        }
        try {
            IntentFilter appConfigUpdateFilter = new IntentFilter();
            appConfigUpdateFilter.addAction(APP_CONFIG_UPDATE_ACTION);
            appConfigUpdateFilter.addDataScheme("package");
            appConfigUpdateFilter.addDataSchemeSpecificPart("magicwin", 0);
            ContextEx.registerReceiverAsUser(this.mContext, this.mAppConfigUpdateReceiver, UserHandleEx.CURRENT, appConfigUpdateFilter, EASYGO_PERMISSION_SEND_SYS_BCST, (Handler) null);
        } catch (IllegalArgumentException e2) {
            SlogEx.e(TAG, "register AppConfig Update fail!");
        }
    }

    private void registerUninstallReceiver() {
        IntentFilter uninstallFilter = new IntentFilter();
        uninstallFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        uninstallFilter.addDataScheme("package");
        ContextEx.registerReceiverAsUser(this.mContext, this.mUnistallReciver, UserHandleEx.ALL, uninstallFilter, (String) null, (Handler) null);
    }

    private void registerInstallReceiver() {
        IntentFilter installFilter = new IntentFilter();
        installFilter.addAction("android.intent.action.PACKAGE_ADDED");
        installFilter.addDataScheme("package");
        ContextEx.registerReceiverAsUser(this.mContext, this.mInstallReceiver, UserHandleEx.ALL, installFilter, (String) null, (Handler) null);
    }

    private void registerUserSwitchReceiver() {
        IntentFilter userSwitchFilter = new IntentFilter();
        userSwitchFilter.addAction("android.intent.action.USER_SWITCHED");
        this.mContext.registerReceiver(this.mUserSwitchReceiver, userSwitchFilter);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void init() {
        this.mMwManager = HwMagicWinManager.getInstance();
        this.mUIController = new HwMagicWindowUIController(this.mMwManager, this.mContext, this.mAms.getActivityTaskManagerServiceEx());
        HwMagicWinManager hwMagicWinManager = this.mMwManager;
        hwMagicWinManager.init(new SharedParameters(hwMagicWinManager, this.mContext, this.mAmsEx, this.mWmsEx), this);
        HwMagicWinStatistics.loadCache();
        this.mHandler.sendEmptyMessage(1);
        this.mHandler.sendEmptyMessageDelayed(9, CHECK_REPORT_MAGICWIN_USAGE_PERIOD);
        registerCloudUpdateReceiver();
        registerForHwMultiWindow(this.mAms);
        registerUninstallReceiver();
        registerInstallReceiver();
        registerBdControlReceiver();
        registerFoldDisplayReceiver();
        registerLocaleChangeReceiver();
        this.mIsInited = true;
        SlogEx.e(TAG, "service init completed!");
    }

    private void registerForHwMultiWindow(HwActivityManagerService ams) {
        HwMultiWindowManager hwMultiWindowManager = HwMultiWindowManager.getInstance(ams.getActivityTaskManagerServiceEx());
        if (hwMultiWindowManager == null) {
            SlogEx.w(TAG, "registerForHwMultiWindow failed, cause hwMultiWindowManager is null!");
        } else {
            hwMultiWindowManager.setHwMagicWindowService(this);
        }
    }

    private void enforceSystemUid() {
        if (UserHandleEx.getAppId(Binder.getCallingUid()) != 1000) {
            throw new SecurityException("Only available to system processes");
        }
    }

    public Bitmap getWallpaperScreenShot() {
        HwMagicContainer container = this.mMwManager.getLocalContainer();
        if (container == null) {
            return null;
        }
        return this.mUIController.getWallpaperScreenShot(container.getDisplayId());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void registerEasyGoService() {
        if (this.mMwManager.isSupportOpenCapability()) {
            registerEasyGo();
            registerAppConfigUpdateReceiver();
            if (this.mMwManager.getLocalContainer() != null) {
                this.mMwManager.getLocalContainer().getConfig().loadAppAdapterInfo("*");
            }
        }
    }

    private void registerEasyGo() {
        Bundle reply;
        SlogEx.i(TAG, "registerEasyGo");
        Bundle bundle = new Bundle();
        bundle.putString(EASYGO_REGISTER_FUNCTION_KEY, EASYGO_REGISTER_FUNCTION_VALUE);
        bundle.putString("server_data_schema", "package:magicwin");
        bundle.putString("service_name", "HwMagicWindowService");
        bundle.putString("service_version", BuildConfig.VERSION_NAME);
        try {
            Context context = ContextEx.createPackageContextAsUser(this.mContext, "com.huawei.systemserver", 0, UserHandleEx.of(this.mAms.getCurrentUserId()));
            if (context != null && (reply = context.getContentResolver().call(Uri.parse("content://com.huawei.easygo.easygoprovider/v_function"), "register_server", (String) null, bundle)) != null) {
                SlogEx.i(TAG, "registerEasyGo reply " + reply.toString());
            }
        } catch (PackageManager.NameNotFoundException e) {
            SlogEx.e(TAG, "register EasyGo no package context");
        } catch (IllegalArgumentException e2) {
            SlogEx.e(TAG, "registerEasyGo fail : " + e2);
        }
    }

    private void registerCloudUpdateReceiver() {
        IntentFilter updateCfgFilter = new IntentFilter();
        updateCfgFilter.addAction(ACTION_FROM_SYSTEMMANAGER);
        ContextEx.registerReceiverAsUser(this.mContext, this.mUpdateMagicWindowConfigBdReceiver, UserHandleEx.ALL, updateCfgFilter, SYSTEM_MANAGER_PERMISSION, (Handler) null);
    }

    private void registerAlarmUpdateReceiver() {
        IntentFilter alarmUpdateFilter = new IntentFilter();
        alarmUpdateFilter.addAction(ALARM_UPDATE_CONFIG_ACTION);
        this.mContext.registerReceiver(this.mAlarmReceiver, alarmUpdateFilter, DELAYED_UPDATE_CONFIG_PERMISSION, null);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startRelateIntent(Message msg) {
        Intent intent = msg.obj instanceof Intent ? (Intent) msg.obj : null;
        if (intent != null && this.mContext != null) {
            try {
                Bundle option = new Bundle();
                option.putInt("android.activity.launchDisplayId", msg.arg2);
                ContextEx.startActivityAsUser(this.mContext, intent, option, UserHandleEx.of(msg.arg1));
            } catch (ActivityNotFoundException e) {
                SlogEx.e(TAG, "can't find the relate activity");
            } catch (Exception e2) {
                SlogEx.e(TAG, "start relate activity exception");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void adjustWinToFullWhenResume(Message msg) {
        SlogEx.i(TAG, "case Utils.MSG_ADJWIN_TOFULL_WHEN_RESUME");
        if (ActivityRecordEx.isActivityRecordInstance(msg.obj)) {
            synchronized (this.mAmsEx.getActivityTaskManagerEx().getGlobalLock()) {
                ActivityRecordEx arEx = ActivityRecordEx.createNewInstance(msg.obj);
                if (this.mMwManager.getAmsPolicy().getActivityByPosition(arEx, 2, 0) == null || this.mMwManager.getAmsPolicy().getActivityByPosition(arEx, 1, 0) == null) {
                    this.mMwManager.getAmsPolicy().mModeSwitcher.updateActivityToFullScreenConfiguration(arEx);
                }
            }
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public Bundle performHwMagicWindowPolicy(int policy, List params) {
        if (!this.mIsInited) {
            return Bundle.EMPTY;
        }
        Bundle result = new Bundle();
        if (!(policy == 0 || policy == 1 || policy == 2 || policy == 3 || policy == 4 || policy == 5 || policy == 6 || policy == 7 || policy == 31 || policy == 32)) {
            if (policy == 51) {
                this.mMwManager.getWmsPolicy().performHwMagicWindowPolicy(policy, params, result);
            } else if (policy != 52) {
                if (!(policy == 61 || policy == 62)) {
                    if (!(policy == 70 || policy == 71)) {
                        if (!(policy == 132 || policy == 133)) {
                            switch (policy) {
                                case Utils.MSG_UPDATE_APP_CONFIG /* 7 */:
                                case Utils.MSG_REPORT_USAGE_STATISTICS /* 9 */:
                                case Utils.MSG_MOVE_LATEST_TO_TOP /* 10 */:
                                case 28:
                                case 41:
                                case 80:
                                    break;
                                case 8:
                                    updateMwWallpaperVisibility(params);
                                    break;
                                case 128:
                                case 208:
                                    break;
                                default:
                                    switch (policy) {
                                        default:
                                            switch (policy) {
                                                case Utils.MSG_ONE_HOP_SVC_STATE /* 20 */:
                                                    this.mMwManager.getWmsPolicy().getRatio(params, result);
                                                    break;
                                                case Utils.MSG_ONE_HOP_CONFIG_FILES /* 21 */:
                                                case 25:
                                                    break;
                                                case Utils.MSG_ONE_HOP_APP_SWITCH_CHANGE /* 22 */:
                                                case Utils.MSG_SET_CAMERA_FULLSCREEN /* 23 */:
                                                case Utils.MSG_SCHEDULE_IDLE /* 24 */:
                                                    break;
                                                default:
                                                    switch (policy) {
                                                        case 100:
                                                        case 108:
                                                        case Utils.POLICY_GET_APP_INFO /* 110 */:
                                                            break;
                                                        case HwMagicWinAnimationScene.SCENE_MIDDLE /* 101 */:
                                                        case HwMagicWinAnimationScene.SCENE_START_APP /* 102 */:
                                                        case HwMagicWinAnimationScene.SCENE_EXIT_SLAVE_TO_SLAVE /* 103 */:
                                                        case HwMagicWinAnimationScene.SCENE_ANAN_MASTER_TO_SLAVE /* 104 */:
                                                        case HwMagicWinAnimationScene.SCENE_EXIT_MASTER_TO_SLAVE /* 105 */:
                                                        case HwMagicWinAnimationScene.SCENE_EXIT_BY_MAGIC_WINDOW /* 106 */:
                                                        case 107:
                                                            break;
                                                        case 109:
                                                            canDragToFullScreen(params, result);
                                                            break;
                                                        default:
                                                            switch (policy) {
                                                                case 135:
                                                                case 136:
                                                                case 137:
                                                                    break;
                                                                default:
                                                                    SlogEx.e(TAG, "policy type error : " + policy);
                                                                    break;
                                                            }
                                                    }
                                            }
                                        case Utils.MSG_START_RELATE_ACT /* 13 */:
                                        case Utils.MSG_ADJWIN_TOFULL_WHEN_RESUME /* 14 */:
                                        case Utils.MSG_WRITE_SETTING_XML /* 15 */:
                                        case 16:
                                        case Utils.MSG_SERVICE_INIT /* 17 */:
                                        case Utils.MSG_LOCAL_CHANGE /* 18 */:
                                            this.mMwManager.getAmsPolicy().performHwMagicWindowPolicy(policy, params, result);
                                            break;
                                    }
                            }
                            return result;
                        }
                    }
                    this.mMwManager.getWmsPolicy().performHwMagicWindowPolicy(policy, params, result);
                    return result;
                }
            }
            this.mMwManager.getAmsPolicy().performHwMagicWindowPolicy(policy, params, result);
            return result;
        }
        this.mMwManager.getAmsPolicy().performHwMagicWindowPolicy(policy, params, result);
        return result;
    }

    public Map<String, Boolean> getHwMagicWinEnabledApps() {
        HwMagicContainer container = this.mMwManager.getLocalContainer();
        if (this.mIsInited && container != null) {
            return container.getConfig().getHwMagicWinEnabledApps();
        }
        SlogEx.w(TAG, "getHwMagicWinEnabledApps, service is not ready!");
        return new HashMap();
    }

    public boolean setHwMagicWinEnabled(String pkg, boolean isEnabled) {
        SlogEx.d(TAG, "setHwMagicWinEnabled, pkg = " + pkg + ", enabled = " + isEnabled);
        HwMagicContainer container = this.mMwManager.getLocalContainer();
        if (pkg == null || !this.mIsInited || container == null) {
            return false;
        }
        enforceSystemUid();
        container.getConfig().onAppSwitchChanged(pkg, isEnabled);
        this.mMwManager.sendAppSwitchToSource(pkg, isEnabled);
        Message msg = this.mHandler.obtainMessage(6);
        msg.obj = pkg;
        this.mHandler.sendMessage(msg);
        return true;
    }

    public boolean getHwMagicWinEnabled(String pkg) {
        HwMagicContainer container = this.mMwManager.getLocalContainer();
        return container != null && this.mIsInited && container.getHwMagicWinEnabled(pkg);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateMagicWindowConfig() {
        String[] fromFileArray = {this.mCloudConfigUri + "/" + HwMagicWindowConfigLoader.CLOUD_PACKAGE_CONFIG_FILE_NAME};
        String[] toFileArray = {"/data/system/magicWindowFeature_magic_window_application_list.xml"};
        int copyCnt = fromFileArray.length < toFileArray.length ? fromFileArray.length : toFileArray.length;
        for (int i = 0; i < copyCnt; i++) {
            File file = new File(fromFileArray[i]);
            if (file.exists()) {
                try {
                    FileUtilsEx.copy(file, new File(toFileArray[i]));
                    SlogEx.i(TAG, "copy " + fromFileArray[i] + " to " + toFileArray[i]);
                    this.mIsCloudConfigChanged = true;
                } catch (IOException e) {
                    SlogEx.e(TAG, "FileUtils.copy from : " + fromFileArray[i] + " to : " + toFileArray[i] + Log.getStackTraceString(e));
                }
            } else {
                SlogEx.w(TAG, "Cloud update cache file not exist.");
            }
        }
        if (HwMwUtils.ENABLED && this.mIsCloudConfigChanged) {
            if (!isScreenOn()) {
                SlogEx.i(TAG, "update config immediately.");
                applyNewCloudConfig();
                return;
            }
            registerAlarmUpdateReceiver();
            sendAlarmUpdateConfiguraion();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void copyBlackListFile() {
        String fromFileArray = this.mCloudConfigUri + "/" + HwMagicWindowConfigLoader.CLOUD_MULTISCREEN_PROJECTION_LIMIT_FILE_NAME;
        File file = new File(fromFileArray);
        if (file.exists()) {
            try {
                FileUtilsEx.copy(file, new File("/data/system/magicWindowFeature_multiscreen_projection_limit.xml"));
                SlogEx.i(TAG, "copy " + fromFileArray + " to /data/system/magicWindowFeature_multiscreen_projection_limit.xml");
            } catch (IOException e) {
                SlogEx.e(TAG, "FileUtils.copy from : " + fromFileArray + " to : /data/system/magicWindowFeature_multiscreen_projection_limit.xml" + Log.getStackTraceString(e));
            }
        } else {
            SlogEx.w(TAG, "Cloud update black list file not exist.");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void applyNewCloudConfig() {
        this.mIsCloudConfigChanged = false;
        HwMagicContainer container = this.mMwManager.getLocalContainer();
        if (container != null) {
            this.mMwManager.getAmsPolicy().removeCachedMagicWindowApps(container.getConfig().onCloudUpdate());
        }
    }

    private void sendAlarmUpdateConfiguraion() {
        Calendar calendar = Calendar.getInstance();
        int hourOfDay = calendar.get(11);
        int dayOfYear = calendar.get(6);
        if (hourOfDay > 4) {
            dayOfYear++;
        }
        calendar.set(12, 0);
        calendar.set(13, 0);
        calendar.set(14, 0);
        calendar.set(11, 4);
        calendar.set(6, dayOfYear);
        Intent intent = new Intent(ALARM_UPDATE_CONFIG_ACTION);
        intent.setPackage("android");
        this.mAlarmIntent = PendingIntent.getBroadcast(this.mContext, 0, intent, 0);
        SlogEx.i(TAG, "Will update config at day " + dayOfYear + " 4 oclock.");
        this.mAlarmManager.setRepeating(0, calendar.getTimeInMillis(), 86400000, this.mAlarmIntent);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isScreenOn() {
        return this.mPowerManager.isScreenOn();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void processLocaleChange() {
        HwMagicContainer container = this.mMwManager.getLocalContainer();
        if (container != null) {
            container.getConfig().updateSystemBoundSize(container.getDisplayMetrics());
            container.getConfig().updateDragModeForLocaleChange();
            this.mMwManager.getAmsPolicy().sendMsgToWriteSettingsXml("locale_change");
            container.calcHwSplitStackBounds();
            this.mMwManager.getAmsPolicy().mModeSwitcher.changeLayoutDirection(container.getConfig().isRtl());
        }
    }

    private void updateMwWallpaperVisibility(List params) {
        if (!(params.get(0) instanceof Boolean) || !(params.get(1) instanceof Integer)) {
            SlogEx.e(TAG, "Err : updateMwWallpaperVisibility params error params.get(0) = " + params.get(0) + ",params.get(1) = " + params.get(1));
            return;
        }
        this.mUIController.updateMwWallpaperVisibilityIfNeed(((Boolean) params.get(0)).booleanValue(), ((Integer) params.get(1)).intValue());
    }

    private void canDragToFullScreen(List params, Bundle result) {
        if (params == null || !(params.get(0) instanceof Integer)) {
            SlogEx.e(TAG, "Err : canDragToFullScreen params error!");
            return;
        }
        HwMagicContainer container = this.mMwManager.getContainerByDisplayId(((Integer) params.get(0)).intValue());
        ActivityRecordEx topAr = this.mMwManager.getAmsPolicy().getTopActivity(container);
        if (topAr != null) {
            result.putBoolean("RESULT_CAN_DRAG_TO_FS", container.getConfig().isSupportDraggingToFullScreen(Utils.getPackageName(topAr)));
        }
    }

    public void updateSystemBoundSize() {
        HwMagicContainer container = this.mMwManager.getLocalContainer();
        if (container != null) {
            container.getConfig().updateSystemBoundSize(container.getDisplayMetrics());
            container.calcHwSplitStackBounds();
            this.mUIController.setNeedUpdateWallpaperSize(true);
            Map<String, Boolean> enableMagicApps = new HashMap<>();
            container.getConfig().getHwMagicWinSettingConfigs().forEach(new BiConsumer(enableMagicApps) {
                /* class com.huawei.server.magicwin.$$Lambda$HwMagicWindowManagerService$oGt8nu2hd2eyWeSlnNTZuVWM6vs */
                private final /* synthetic */ Map f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.BiConsumer
                public final void accept(Object obj, Object obj2) {
                    HwMagicWindowManagerService.lambda$updateSystemBoundSize$0(this.f$0, (String) obj, (SettingConfig) obj2);
                }
            });
            Set<String> tempApps = new HashSet<>();
            for (Map.Entry<String, Boolean> entry : enableMagicApps.entrySet()) {
                if (entry != null && entry.getValue().booleanValue()) {
                    tempApps.add(entry.getKey());
                }
            }
            this.mMwManager.getAmsPolicy().removeCachedMagicWindowApps(tempApps);
        }
    }

    static /* synthetic */ void lambda$updateSystemBoundSize$0(Map enableMagicApps, String key, SettingConfig value) {
        Boolean bool = (Boolean) enableMagicApps.put(key, Boolean.valueOf(value.getHwMagicWinEnabled()));
    }

    public Bundle invokeSync(String packageName, String method, String params, Bundle objects) {
        SlogEx.i(TAG, "EasyGo Binder invokeSync packageName : " + packageName + " method : " + method + " params : " + params);
        Bundle result = new Bundle();
        if (method == null || packageName == null || params == null) {
            return result;
        }
        int uid = Binder.getCallingUid();
        long identity = Binder.clearCallingIdentity();
        try {
            if (method.equals(FUNC_SET_LOGIN_STATUS)) {
                this.mMwManager.getAmsPolicy().setLoginStatus(packageName, HwMagicWindowConfig.parseIntParama(params, EASYGO_LOGIN_STATUS_KEY), uid);
                return result;
            }
            int taskId = HwMagicWindowConfig.parseIntParama(params, EASYGO_TASK_ID_KEY);
            if (taskId < 0) {
                Binder.restoreCallingIdentity(identity);
                return result;
            }
            if (method.equals(FUNC_SET_TASK_POS)) {
                this.mMwManager.getAmsPolicy().setTaskPosition(packageName, taskId, HwMagicWindowConfig.parseIntParama(params, EASYGO_TARGET_POS_KEY));
            } else if (method.equals(FUNC_GET_TASK_POS)) {
                result.putInt(BUNDLE_GET_POS_INT, this.mMwManager.getAmsPolicy().getTaskPosition(packageName, taskId));
            } else if (method.equals(FUNC_IS_IN_MAGIC_WIN_MODE)) {
                result.putBoolean(BUNDLE_IS_IN_MAGIC_WIN_MODE, this.mMwManager.getAmsPolicy().isInMagicWindowMode(taskId));
            } else {
                SlogEx.e(TAG, "no such function!");
            }
            Binder.restoreCallingIdentity(identity);
            return result;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void invokeAsync(String packageName, String method, String params, Bundle objects, IBinder callback) {
        SlogEx.i(TAG, "EasyGo Binder invokeAsync packageName : " + packageName + " method : " + method);
    }

    public ActivityStackEx getNewTopStack(ActivityStackEx oldStack, int otherSideModeToChange) {
        return this.mMwManager.getAmsPolicy().mMagicWinSplitMng.getNewTopStack(oldStack, otherSideModeToChange);
    }

    public void addOtherSnapShot(ActivityStackEx stack, HwActivityTaskManagerServiceEx hwAtmsEx, List<HwTaskSnapshotWrapper> snapShots) {
        this.mMwManager.getAmsPolicy().mMagicWinSplitMng.addOtherSnapShot(stack, hwAtmsEx, snapShots);
    }

    public void updateAppMagicWinStatusInMultiDevice(int reason, int targetDisplayid, int targetWidth, int targetHeight) {
        enforceSystemUid();
        this.mMwManager.updateAppMagicWinStatusInMultiDevice(reason, targetDisplayid, targetWidth, targetHeight);
    }

    public boolean isSupportMagicWindowSink() {
        enforceSystemUid();
        boolean isSupport = HwMwUtils.ENABLED && this.mMwManager.getLocalSysCfg() != null && this.mMwManager.getLocalSysCfg().isSupportLocalConfig();
        SlogEx.i(TAG, "isSupportMagicWindowSink = " + isSupport);
        return isSupport;
    }

    public boolean isSupportMagicWindowSource() {
        return HwMwUtils.ENABLED && this.mMwManager.getLocalSysCfg() != null && this.mMwManager.getLocalSysCfg().isSupportVirtualConfig();
    }

    public boolean notifyConnectionState(boolean isSink, boolean isConnected) {
        enforceSystemUid();
        if (isSink && !isSupportMagicWindowSink()) {
            SlogEx.i(TAG, "Sink not support local config.");
            return false;
        } else if (isSink || isSupportMagicWindowSource()) {
            SlogEx.d(TAG, "send MSG_ONE_HOP_CONN_STATE. sink ? " + isSink + " connected ? " + isConnected);
            Message msg = this.mHandler.obtainMessage(19);
            msg.arg1 = isSink ? 1 : 0;
            msg.arg2 = isConnected ? 1 : 0;
            this.mHandler.sendMessage(msg);
            return true;
        } else {
            SlogEx.i(TAG, "Source not support virtual config.");
            return false;
        }
    }

    public void handleOneHopSvcStateChanged(boolean isConnected) {
        SlogEx.d(TAG, "send MSG_ONE_HOP_SVC_STATE. connected ? " + isConnected);
        Message msg = this.mHandler.obtainMessage(20);
        msg.arg1 = isConnected ? 1 : 0;
        this.mHandler.sendMessage(msg);
    }

    public void handleConfigFiles(List<Uri> uris) {
        SlogEx.d(TAG, "send MSG_ONE_HOP_CONFIG_FILES. uris ? " + uris.size());
        Message msg = this.mHandler.obtainMessage(21);
        msg.obj = uris;
        this.mHandler.sendMessage(msg);
    }

    public void handleAppSwitchChange(String data) {
        SlogEx.d(TAG, "send MSG_ONE_HOP_APP_SWITCH_CHANGE. data ? " + data);
        Message msg = this.mHandler.obtainMessage(22);
        msg.obj = data;
        this.mHandler.sendMessage(msg);
    }

    public HwMagicWindowUIController getUIController() {
        return this.mUIController;
    }
}
