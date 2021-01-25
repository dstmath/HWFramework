package com.android.server.wm;

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.AppGlobals;
import android.app.HwRecentTaskInfo;
import android.app.IActivityController;
import android.app.IHwActivityNotifier;
import android.app.IHwDockCallBack;
import android.app.ITaskStackListener;
import android.app.KeyguardManager;
import android.app.WindowConfiguration;
import android.app.mtm.MultiTaskUtils;
import android.app.usage.UsageStatsManagerInternal;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Configuration;
import android.content.res.HwPCMultiWindowCompatibility;
import android.cover.CoverManager;
import android.cover.HallState;
import android.cover.IHallCallback;
import android.database.ContentObserver;
import android.freeform.HwFreeFormUtils;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.DisplayManagerInternal;
import android.hardware.display.HwFoldScreenState;
import android.hdm.HwDeviceManager;
import android.net.Uri;
import android.os.BadParcelableException;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.IMWThirdpartyCallback;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.rms.iaware.AppTypeRecoManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.CoordinationModeUtils;
import android.util.Flog;
import android.util.HwMwUtils;
import android.util.HwPCUtils;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import android.view.ContextThemeWrapper;
import android.view.IApplicationToken;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceControl;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.Toast;
import com.android.server.CoordinationStackDividerManager;
import com.android.server.HwServiceFactory;
import com.android.server.LocalServices;
import com.android.server.ServiceThread;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.AppTimeTracker;
import com.android.server.am.BaseErrorDialog;
import com.android.server.am.HwActivityManagerService;
import com.android.server.am.HwActivityManagerServiceEx;
import com.android.server.camera.IHwCameraServiceProxy;
import com.android.server.cust.utils.HwCustPkgNameConstant;
import com.android.server.gesture.DefaultGestureNavManager;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.android.server.intellicom.common.SmartDualCardConsts;
import com.android.server.multiwin.HwMultiWinUtils;
import com.android.server.pm.HwThemeInstaller;
import com.android.server.pm.auth.HwCertification;
import com.android.server.policy.HwPhoneWindowManager;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.security.securityprofile.ISecurityProfileController;
import com.android.server.security.securityprofile.IntentCaller;
import com.android.server.security.trustspace.ITrustSpaceController;
import com.android.server.wifipro.WifiProCommonUtils;
import com.android.server.wm.ActivityStack;
import com.android.server.wm.RecentTasks;
import com.android.server.wm.utils.HwDisplaySizeUtil;
import com.huawei.android.app.HwActivityTaskManager;
import com.huawei.android.app.IGameObserver;
import com.huawei.android.app.IGameObserverEx;
import com.huawei.android.fsm.HwFoldScreenManagerInternal;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.util.HwPCUtilsEx;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import com.huawei.hiai.awareness.client.AwarenessRequest;
import com.huawei.hwaps.HwApsImpl;
import com.huawei.hwextdevice.HWExtDeviceManager;
import com.huawei.pgmng.log.LogPower;
import com.huawei.server.HwPCFactory;
import com.huawei.server.HwPartIawareUtil;
import com.huawei.server.HwPartMagicWindowServiceFactory;
import com.huawei.server.magicwin.DefaultHwMagicWinCombineManager;
import com.huawei.server.multiwindowtip.HwMultiWindowTips;
import com.huawei.server.rme.hyperhold.SceneConst;
import com.huawei.server.rme.hyperhold.SceneProcessing;
import com.huawei.server.rme.hyperhold.Swap;
import com.huawei.server.security.HwServiceSecurityPartsFactoryEx;
import com.huawei.server.wm.WindowProcessControllerEx;
import huawei.com.android.server.policy.HwGlobalActionsData;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class HwActivityTaskManagerServiceEx implements IHwActivityTaskManagerServiceEx {
    private static final String ACTION_DOCK_SHOW_CLOSE = "com.huawei.hwdockbar.dock.operation.action";
    private static final String ACTION_HWOUC_SHOW_UPGRADE_REMIND = "com.huawei.android.hwouc.action.SHOW_UPGRADE_REMIND";
    private static final Set<String> ACTIVITY_NOTIFIER_TYPES = new HashSet<String>() {
        /* class com.android.server.wm.HwActivityTaskManagerServiceEx.AnonymousClass1 */

        {
            add("returnToHome");
            add("activityLifeState");
            add("appSwitch");
            add("appDie");
            add("toggleFreeform");
            add("freeformBallLifeState");
            add("focusStackChange");
            add("fullScreenStateChange");
            add("holdScreenStateChange");
            add("displayStacksEmpty");
            add("secureStateChange");
            add("magicAppStateChange");
            add("pipStateChange");
            add("freeformNotificationChange");
            add("switchToDefaultDisplay");
            add("freeFormDragEnd");
        }
    };
    private static final int APP_ASSOC_HOME_UPDATE = 11;
    private static final int APP_FOCUS_CHANGE = 230;
    private static final int APP_TASK_LIMITS = 20;
    private static final String ASSOC_PID = "pid";
    private static final String ASSOC_PKGNAME = "pkgname";
    private static final String ASSOC_RELATION_TYPE = "relationType";
    private static final String ASSOC_TGT_UID = "tgtUid";
    private static String BACKUP_ACTIVITY_NAME = "";
    private static String BACKUP_PKG_NAME = "";
    private static final String BOOT_PERMISSION = "android.permission.RECEIVE_BOOT_COMPLETED";
    private static final String BROADCAST_CATION_VISUAL = "com.huawei.motion.change.noification";
    public static final String CATEGORY_BACK_ENTER_RECENT = "enter_recent";
    public static final String CATEGORY_EXIT_RECENT = "exit_recent";
    public static final String CATEGORY_RETURN_HOME = "return_home";
    public static final String CATEGORY_RETURN_HOME_END = "return_home_end";
    public static final String CATEGORY_SWITCH_TASK = "quick_switch_task";
    private static final boolean DESKTOP_ENABLED = SystemPropertiesEx.getBoolean("ro.config.hw_emui_desktop_mode", false);
    private static final String DOCKBAR_MULTITASK_MGR_CLASS = "com.huawei.hwdockbar/.multitask.view.MultiTaskActivity";
    private static final String EMERGENCY_PACKAGENAME = "com.android.emergency";
    private static final boolean ENABLED_IN_PAD = SystemPropertiesEx.getBoolean("ro.config.hw_emui_pad_pc_mode", false);
    private static final long ENABLE_EVENT_DISPATCHING_DELAY_MILLIS = 1000;
    private static final int EXIT_COORDINATION_MODE_TIMEOUT = 1500;
    private static final long EXIT_RECENT_STATE_DELAY_TIME = 300;
    private static final int EXSPLASH_ENABLE = 1;
    private static final int FULLSCREEN_REC_TOP = -1;
    private static final String HMS_PERSISTENT_NAME = "com.huawei.hwid.persistent";
    private static final String HMS_PKG_NAME = "com.huawei.hwid";
    private static final int HWOUC_UPDATE_REMIND_MSG = 80;
    private static final String HW_DOCK_PACKAGE_NAME = "com.huawei.hwdockbar";
    private static final String HW_LAUNCHER_PKGNAME = "com.huawei.android.launcher";
    private static final int HW_MULTI_WINDOWING_MODE_FREEFORM = 102;
    private static final boolean HW_SHOW_INCOMPATIBLE_DIALOG = SystemProperties.getBoolean("ro.config.incompatible_dialog", false);
    private static final boolean HW_SNAPSHOT = SystemProperties.getBoolean("ro.huawei.only_hwsnapshot", true);
    private static final boolean HW_SUPPORT_LAUNCHER_EXIT_ANIM = (!SystemProperties.getBoolean("ro.config.disable_launcher_exit_anim", false));
    private static final String HW_SYSTEM_SERVER_START = "com.huawei.systemserver.START";
    private static final boolean IS_BOPD = SystemProperties.getBoolean("sys.bopd", false);
    private static final boolean IS_CHINA = "CN".equalsIgnoreCase(SystemProperties.get(WifiProCommonUtils.KEY_PROP_LOCALE, ""));
    private static final boolean IS_HW_MULTIWINDOW_SUPPORTED = SystemProperties.getBoolean("ro.config.hw_multiwindow_optimization", false);
    private static final boolean IS_PROP_FINGER_BOOST = SystemProperties.getBoolean("persist.debug.finger_boost", true);
    private static final String KEY_DISPLAY_ID = "displayId";
    private static final String KEY_PACKAGE_NAME = "pkgName";
    private static final String KEY_USER_ID = "userId";
    private static final String LIFECYCLE_ON_RESUME = "onResume";
    private static final int MAXIMIZE_REC_TOP = 0;
    private static final int MULTI_WINDOW_MODE_CHANGED_MSG = 23;
    private static final int NEED_HMS_ACTIVE = 1;
    private static final Set<String> NERVER_USE_COMPAT_MODE_APPS = new HashSet<String>() {
        /* class com.android.server.wm.HwActivityTaskManagerServiceEx.AnonymousClass2 */

        {
            add("com.huawei.camera");
            add("com.android.incallui");
            add(HwThemeInstaller.HWT_OLD_CONTACT);
        }
    };
    private static final int NOTIFY_ACTIVITY_STATE = 71;
    private static final int NOTIFY_CALL = 24;
    private static final int NOTIFY_NEED_SWING_ROTATION = 90;
    private static final int NOTIFY_SHOW_EXSPLASH = 91;
    private static final Set<String> ONLY_NOTIFY_SYSTEM_USER_PROCESS = new HashSet<String>() {
        /* class com.android.server.wm.HwActivityTaskManagerServiceEx.AnonymousClass3 */

        {
            add("com.android.systemui");
            add("com.huawei.android.extdisplay");
            add(HwActivityTaskManagerServiceEx.HW_DOCK_PACKAGE_NAME);
            add("com.huawei.systemserver");
            add("com.huawei.iaware");
        }
    };
    private static final String PACKAGE_HWOUC = "com.huawei.android.hwouc";
    private static final String PERMISSION_DOCK_SHOW_CLOSE = "com.huawei.hwdockbar.permission.START_DOCK";
    private static final String PERMISSION_HWOUC_UPGRADE_REMIND = "com.huawei.android.hwouc.permission.UPGRADE_REMIND";
    private static final String PERMISSION_LAUNCHER_BROADCASTS = "com.android.launcher.permission.RECEIVE_LAUNCH_BROADCASTS";
    private static final File PRELOAD_APP_NOT_REMIND_XML = new File(Environment.getDataSystemDirectory(), "NotRemindIncompatibleApp.xml");
    public static final int PROVISIONED_OFF = 0;
    public static final int PROVISIONED_ON = 1;
    private static final int READ_INCOMPATIBLE_XML_MSG = 50;
    private static final int READ_NOT_REMIND_XML_MSG = 51;
    private static final long RECENTLY_BACK_HOME_TIMEOUT = 1000;
    private static final int REMOVE_EXCEEDS_LIMIT_TASKS_MSG = 92;
    private static final String RESOURCE_APPASSOC = "RESOURCE_APPASSOC";
    private static final int SHOW_UNINSTALL_LAUNCHER_MSG = 48;
    private static final int SHOW_UPDATE_INCOMPATIBLE_APP_MSG = 49;
    private static final int SIDELEFT_REC_TOP = -2;
    private static final int SIDERIGHT_REC_TOP = -3;
    private static final String SPLIT_SCREEN_APP_NAME = "splitscreen.SplitScreenAppActivity";
    private static final String STRING_INCOMPATIBLE_CFG_DIR = "xml/PreloadAppCompatibility";
    private static final String STRING_INCOMPATIBLE_CFG_FILE_PATH = "xml/PreloadAppCompatibility/PreloadAppCompatibility.xml";
    private static final String STRING_INCOMPATIBLE_XML_ATTRIBUTE_NAME = "name";
    private static final String STRING_INCOMPATIBLE_XML_ATTRIBUTE_PKG_VERSION = "pkgVersion";
    private static final String STRING_INCOMPATIBLE_XML_PKG = "package";
    private static final String STRING_INCOMPATIBLE_XML_PKG_LIST = "package_list";
    static final String TAG = "HwActivityTaskManagerServiceEx";
    private static final int TYPE_INCOMPATIBLE_XML_TO_HASHMAP = 0;
    private static final int TYPE_NOT_REMIND_XML_TO_SET = 1;
    private static final int WRITE_NOT_REMIND_XML_MSG = 52;
    private static HashMap<String, String> sCompatibileFileHashMap = new HashMap<>();
    private static float sDeviceMaxRatio = -1.0f;
    private static Set<String> sPipWhitelists = new HashSet();
    private static Set<String> sPreventStartWhenSleeping = new HashSet();
    private static Set<String> sTranslucentWhitelists = new HashSet();
    private static Set<String> sWhitelistActivities = new HashSet();
    private final String faceKeyguardUriName = "face_bind_with_lock";
    private final String fingerPrintKeyguardUriName = "fp_keyguard_enable";
    private final String fingerUnlockBoostWhiteListFileName = "hw_finger_unlock_boost_whitelist";
    private int hmsPersistentUid = -1;
    private boolean isLastMultiMode = false;
    private final RemoteCallbackList<IHwActivityNotifier> mActivityNotifiers = new RemoteCallbackList<>();
    private Set<String> mCompatibileNoRemindSet = new HashSet();
    private File mCompatibileXmlFile = HwCfgFilePolicy.getCfgFile(STRING_INCOMPATIBLE_CFG_FILE_PATH, 0);
    final Context mContext;
    private Intent mCoordinationIntent;
    private String mCurrentPkgName;
    private IActivityController mCustomController = null;
    private FaceSettingsObserver mFaceSettingsObserver;
    private FingerprintSettingsObserver mFingerprintSettingsObserver;
    private List<String> mFingerprintUnlockBoostWhiteList;
    HwFreeFormAssistant mHwFreeFormAssistant = null;
    final HwGameAssistantController mHwGameAssistantController;
    Handler mHwHandler = null;
    ServiceThread mHwHandlerThread = null;
    HwMultiDisplayManager mHwMdm = null;
    private HwMultiWindowTips mHwMultiWindowTip = null;
    HwMultiWindowManager mHwMwm = null;
    private final TaskChangeNotificationController mHwTaskChangeNotificationController;
    IHwActivityTaskManagerInner mIAtmsInner = null;
    private boolean mIsClickCancelButton = false;
    private boolean mIsDialogShow = false;
    private boolean mIsEnterRecent = false;
    private boolean mIsInRecentState = false;
    private boolean mIsInitMultiWindowDisabledState = false;
    private boolean mIsMultiWindowDisabled = false;
    private boolean mIsSetFingerprintOrFaceKeyGuard = false;
    private boolean mIsSupportPopupCamera = false;
    private boolean mIsSupportPopupCameraInit = false;
    private boolean mIsSupportsFreeformBefore = false;
    private boolean mIsSupportsSplitScreenBefore = false;
    public boolean mKeepPrimaryCoordinationResumed;
    private String mLastLauncherName;
    private boolean mNeedRemindHwOUC = false;
    OverscanTimeout mOverscanTimeout = new OverscanTimeout();
    private HashMap<String, Long> mPCUsageStats = new HashMap<>();
    private HashMap<String, Integer> mPkgDisplayMaps = new HashMap<>();
    private String mPkgNameInCoordinationMode = "";
    private ActivityRecord mPreviousResumedActivity;
    private Intent mQuickSlideIntent = null;
    private long mQuickSlideStartTime;
    private String mReturnHomePkg = null;
    private long mReturnHomeTimeStamp = 0;
    private SettingsObserver mSettingsObserver;
    private Set<Integer> mSkipAddToBallTaskSet = new HashSet();
    private final RemoteCallbackList<IMWThirdpartyCallback> mThirdPartyCallbackList;
    ITrustSpaceController mTrustSpaceController;

    static {
        sWhitelistActivities.add("com.vlocker.settings.DismissActivity");
        sTranslucentWhitelists.add("com.android.packageinstaller.permission.ui.GrantPermissionsActivity");
        sPipWhitelists.add("com.android.systemui.pip.phone.PipMenuActivity");
        sPreventStartWhenSleeping.add("com.ss.android.article.news/com.ss.android.message.sswo.SswoActivity");
        sPreventStartWhenSleeping.add("com.ss.android.article.video/com.ss.android.message.sswo.SswoActivity");
        sPreventStartWhenSleeping.add("dongzheng.szkingdom.android.phone/com.dgzq.IM.ui.activity.KeepAliveActivity");
        sPreventStartWhenSleeping.add("com.tencent.news/.push.alive.offactivity.OffActivity");
    }

    public HwActivityTaskManagerServiceEx(IHwActivityTaskManagerInner atms, Context context) {
        HwGameAssistantController hwGameAssistantController = null;
        this.mIAtmsInner = atms;
        this.mContext = context;
        this.mHwHandlerThread = new ServiceThread(TAG, -2, false);
        this.mHwHandlerThread.start();
        this.mHwHandler = new Handler(this.mHwHandlerThread.getLooper()) {
            /* class com.android.server.wm.HwActivityTaskManagerServiceEx.AnonymousClass4 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                int i = msg.what;
                if (i == 23) {
                    boolean isInMultiWindowMode = ((Boolean) msg.obj).booleanValue();
                    synchronized (HwActivityTaskManagerServiceEx.this.mThirdPartyCallbackList) {
                        try {
                            int i2 = HwActivityTaskManagerServiceEx.this.mThirdPartyCallbackList.beginBroadcast();
                            Flog.i(100, "onMultiWindowModeChanged begin : mThirdPartyCallbackList size : " + i2);
                            while (i2 > 0) {
                                i2--;
                                try {
                                    HwActivityTaskManagerServiceEx.this.mThirdPartyCallbackList.getBroadcastItem(i2).onModeChanged(isInMultiWindowMode);
                                } catch (Exception e) {
                                    Flog.e(100, "Error in sending the Callback");
                                }
                            }
                            Flog.i(100, "onMultiWindowModeChanged end : mThirdPartyCallbackList size : " + i2);
                            HwActivityTaskManagerServiceEx.this.mThirdPartyCallbackList.finishBroadcast();
                        } catch (IllegalStateException e2) {
                            Flog.e(100, "beginBroadcast() called while already in a broadcast");
                        }
                    }
                } else if (i == 24) {
                    synchronized (HwActivityTaskManagerServiceEx.this.mActivityNotifiers) {
                        try {
                            Bundle bundle = (Bundle) msg.obj;
                            String userId = String.valueOf(bundle.getInt("android.intent.extra.user_handle"));
                            String reason = bundle.getString("android.intent.extra.REASON");
                            int i3 = HwActivityTaskManagerServiceEx.this.mActivityNotifiers.beginBroadcast();
                            while (i3 > 0) {
                                i3--;
                                IHwActivityNotifier notifier = HwActivityTaskManagerServiceEx.this.mActivityNotifiers.getBroadcastItem(i3);
                                HashMap<String, String> cookie = (HashMap) HwActivityTaskManagerServiceEx.this.mActivityNotifiers.getBroadcastCookie(i3);
                                if ((userId.equals(cookie.get("android.intent.extra.user_handle")) || cookie.get("android.intent.extra.USER") != null) && reason != null && reason.equals(cookie.get("android.intent.extra.REASON"))) {
                                    try {
                                        HwActivityTaskManagerServiceEx.this.mActivityNotifiers.getBroadcastItem(i3).call(bundle);
                                    } catch (Exception e3) {
                                        Flog.e(100, "observer.call get Exception, remove notifier " + notifier, e3);
                                        HwActivityTaskManagerServiceEx.this.mActivityNotifiers.unregister(notifier);
                                    }
                                }
                            }
                            HwActivityTaskManagerServiceEx.this.mActivityNotifiers.finishBroadcast();
                        } catch (Exception e4) {
                            Flog.e(100, "HwActivityNotifier call error");
                        }
                    }
                } else if (i == HwActivityTaskManagerServiceEx.NOTIFY_ACTIVITY_STATE) {
                    HwActivityTaskManagerServiceEx.this.handleNotifyActivityState(msg);
                } else if (i != 80) {
                    switch (i) {
                        case 48:
                            HwActivityTaskManagerServiceEx.this.showUninstallLauncher();
                            return;
                        case 49:
                            HwActivityTaskManagerServiceEx.this.showIncompatibleDialog();
                            return;
                        case 50:
                            HwActivityTaskManagerServiceEx.this.readIncompatibleXmlToHashMap();
                            return;
                        case 51:
                            HwActivityTaskManagerServiceEx.this.readNotRemindXmlToSet();
                            return;
                        case 52:
                            HwActivityTaskManagerServiceEx.this.writeNotRemindIncompatibleAppXml(HwActivityTaskManagerServiceEx.PRELOAD_APP_NOT_REMIND_XML);
                            return;
                        default:
                            switch (i) {
                                case HwActivityTaskManagerServiceEx.NOTIFY_NEED_SWING_ROTATION /* 90 */:
                                    HwActivityTaskManagerServiceEx.this.handleAppNeedsSwingRotation(msg);
                                    return;
                                case 91:
                                    HwActivityTaskManagerServiceEx.this.showExSplash(msg);
                                    return;
                                case 92:
                                    if (msg.obj instanceof Bundle) {
                                        Bundle bundle2 = (Bundle) msg.obj;
                                        HwActivityTaskManagerServiceEx.this.removeTasksIfNeeded(bundle2.getString(HwActivityTaskManagerServiceEx.KEY_PACKAGE_NAME), bundle2.getInt(HwActivityTaskManagerServiceEx.KEY_USER_ID), bundle2.getInt(HwActivityTaskManagerServiceEx.KEY_DISPLAY_ID));
                                        return;
                                    }
                                    return;
                                default:
                                    return;
                            }
                    }
                } else {
                    Slog.i(HwActivityTaskManagerServiceEx.TAG, "send UPDATE REMIND broacast to HWOUC");
                    Intent intent = new Intent(HwActivityTaskManagerServiceEx.ACTION_HWOUC_SHOW_UPGRADE_REMIND);
                    intent.setPackage("com.huawei.android.hwouc");
                    HwActivityTaskManagerServiceEx.this.mContext.sendBroadcastAsUser(intent, UserHandle.SYSTEM, HwActivityTaskManagerServiceEx.PERMISSION_HWOUC_UPGRADE_REMIND);
                }
            }
        };
        this.mThirdPartyCallbackList = new RemoteCallbackList<>();
        this.mHwTaskChangeNotificationController = new TaskChangeNotificationController(this.mIAtmsInner.getATMS().getGlobalLock(), this.mIAtmsInner.getStackSupervisor(), this.mHwHandler);
        this.mHwGameAssistantController = SystemProperties.getInt("ro.config.gameassist", 0) == 1 ? new HwGameAssistantController(this.mContext) : hwGameAssistantController;
        initFingerBoostWhiteListData();
        this.mHwMwm = HwMultiWindowManager.getInstance(atms.getATMS());
        this.mHwMdm = HwMultiDisplayManager.getInstance(atms.getATMS());
        this.mHwFreeFormAssistant = HwFreeFormAssistant.getInstance(atms.getATMS());
    }

    public void notifyDisplayModeChange(int displaymode, int currDisplaymode) {
        this.mHwMwm.notifyDisplayModeChange(displaymode, currDisplaymode);
    }

    public void onSystemReady() {
        this.mSettingsObserver = new SettingsObserver(this.mHwHandler);
        this.mSettingsObserver.init();
        HwGameAssistantController hwGameAssistantController = this.mHwGameAssistantController;
        if (hwGameAssistantController != null) {
            hwGameAssistantController.systemReady();
        }
        registerBroadcastReceiver();
        initTrustSpace();
        this.mFingerprintSettingsObserver = new FingerprintSettingsObserver();
        this.mFaceSettingsObserver = new FaceSettingsObserver();
        IntentFilter userSwitchedFilter = new IntentFilter();
        userSwitchedFilter.addAction(SmartDualCardConsts.SYSTEM_STATE_ACTION_USER_SWITCHED);
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.wm.HwActivityTaskManagerServiceEx.AnonymousClass5 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                HwActivityTaskManagerServiceEx hwActivityTaskManagerServiceEx = HwActivityTaskManagerServiceEx.this;
                hwActivityTaskManagerServiceEx.updateUnlockBoostStatus(hwActivityTaskManagerServiceEx.mIAtmsInner.getATMS().getCurrentUserId());
            }
        }, userSwitchedFilter);
        this.mHwHandler.post(new Runnable() {
            /* class com.android.server.wm.HwActivityTaskManagerServiceEx.AnonymousClass6 */

            @Override // java.lang.Runnable
            public void run() {
                HwActivityTaskManagerServiceEx hwActivityTaskManagerServiceEx = HwActivityTaskManagerServiceEx.this;
                hwActivityTaskManagerServiceEx.updateUnlockBoostStatus(hwActivityTaskManagerServiceEx.mIAtmsInner.getATMS().getCurrentUserId());
            }
        });
        this.mHwHandler.post(new Runnable() {
            /* class com.android.server.wm.HwActivityTaskManagerServiceEx.AnonymousClass7 */

            @Override // java.lang.Runnable
            public void run() {
                HwActivityTaskManagerServiceEx hwActivityTaskManagerServiceEx = HwActivityTaskManagerServiceEx.this;
                hwActivityTaskManagerServiceEx.updateHmsPersistenUid(hwActivityTaskManagerServiceEx.mIAtmsInner.getATMS().getCurrentUserId());
            }
        });
        this.mHwMwm.onSystemReady();
        this.mHwMdm.onSystemReady();
        this.mHwFreeFormAssistant.onSystemReady();
        this.mIsMultiWindowDisabled = HwDeviceManager.disallowOp(54);
        if (IS_HW_MULTIWINDOW_SUPPORTED) {
            registerRecentTaskCallback();
            this.mHwMultiWindowTip = HwMultiWindowTips.getInstance(this.mContext);
        }
        IntentFilter recentActionFilter = new IntentFilter();
        recentActionFilter.addAction(BROADCAST_CATION_VISUAL);
        this.mContext.registerReceiverAsUser(new BroadcastReceiver() {
            /* class com.android.server.wm.HwActivityTaskManagerServiceEx.AnonymousClass8 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                HwActivityTaskManagerServiceEx.this.updateRecentStatus(intent);
            }
        }, UserHandle.ALL, recentActionFilter, PERMISSION_LAUNCHER_BROADCASTS, null);
        setBackupProperties();
        regiserDockShowHideReceiver(this.mContext);
    }

    private void regiserDockShowHideReceiver(Context mContext2) {
        IntentFilter dockHideShowFilter = new IntentFilter();
        dockHideShowFilter.addAction(ACTION_DOCK_SHOW_CLOSE);
        mContext2.registerReceiverAsUser(new BroadcastReceiver() {
            /* class com.android.server.wm.HwActivityTaskManagerServiceEx.AnonymousClass9 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (intent != null && HwActivityTaskManagerServiceEx.this.mHwMwm != null && HwActivityTaskManagerServiceEx.ACTION_DOCK_SHOW_CLOSE.equals(intent.getAction())) {
                    try {
                        boolean useHdr = intent.getBooleanExtra("isShow", false);
                        synchronized (HwActivityTaskManagerServiceEx.this.mIAtmsInner.getATMS().getGlobalLock()) {
                            HwActivityTaskManagerServiceEx.this.mHwMwm.setDockShowing(useHdr);
                            if (!useHdr) {
                                if (HwActivityTaskManagerServiceEx.this.mHwMwm.hasVisibleHwMultiStack(HwActivityTaskManagerServiceEx.this.mIAtmsInner.getStackSupervisor().mRootActivityContainer.getDefaultDisplay())) {
                                    useHdr = true;
                                }
                            }
                        }
                        HwActivityTaskManagerServiceEx.this.mHwMwm.setColorMgrInfo(useHdr);
                    } catch (BadParcelableException e) {
                        Slog.e(HwActivityTaskManagerServiceEx.TAG, "get dock show bad parcel exception!");
                    } catch (Exception e2) {
                        Slog.e(HwActivityTaskManagerServiceEx.TAG, "get dock show othre exception!");
                    }
                }
            }
        }, UserHandle.ALL, dockHideShowFilter, PERMISSION_DOCK_SHOW_CLOSE, null);
    }

    private void setBackupProperties() {
        if (IS_BOPD) {
            String packageName = "com.huawei.localBackup";
            String activityName = "com.huawei.localBackup.EmergencyBackupActivity";
            if (!isNewBackupAppExist(packageName)) {
                packageName = "com.huawei.KoBackup";
                activityName = "com.huawei.KoBackup.EmergencyBackupActivity";
            }
            SystemProperties.set("sys.bopd.package.name", packageName);
            SystemProperties.set("sys.bopd.activity.name", activityName);
            BACKUP_PKG_NAME = packageName;
            BACKUP_ACTIVITY_NAME = activityName;
        }
    }

    private boolean isNewBackupAppExist(String packagename) {
        Context context = this.mContext;
        if (context == null) {
            Slog.e(TAG, "context is null.");
            return false;
        }
        try {
            context.getPackageManager().getPackageInfo(packagename, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Slog.e(TAG, "get backup packageInfo failed!");
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateRecentStatus(Intent intent) {
        String categoryName = intent.getStringExtra("category");
        if (categoryName == null || categoryName.isEmpty()) {
            Slog.i(TAG, "no category, skipping update recent status.");
            return;
        }
        boolean z = true;
        boolean isInLazyMode = this.mIAtmsInner.getATMS().mWindowManager.getLazyMode() != 0;
        char c = 65535;
        int hashCode = categoryName.hashCode();
        if (hashCode != -1938314084) {
            if (hashCode != -1486606706) {
                if (hashCode == -543270494 && categoryName.equals(CATEGORY_BACK_ENTER_RECENT)) {
                    c = 0;
                }
            } else if (categoryName.equals(CATEGORY_RETURN_HOME)) {
                c = 2;
            }
        } else if (categoryName.equals(CATEGORY_EXIT_RECENT)) {
            c = 1;
        }
        if (c == 0) {
            this.mIsEnterRecent = true;
            this.mIsInRecentState = true;
            if (!isInLazyMode) {
                Slog.i(TAG, "update recent status, enter_recent.");
                this.mIAtmsInner.getATMS().mWindowManager.setScreenSideBoxAndCornerVisibility(0, false);
            }
        } else if (c == 1) {
            this.mIsEnterRecent = false;
            this.mHwHandler.postDelayed(new Runnable() {
                /* class com.android.server.wm.$$Lambda$HwActivityTaskManagerServiceEx$KTszFd9yAfumzTQ0Uwq8FcpZfs */

                @Override // java.lang.Runnable
                public final void run() {
                    HwActivityTaskManagerServiceEx.this.lambda$updateRecentStatus$0$HwActivityTaskManagerServiceEx();
                }
            }, EXIT_RECENT_STATE_DELAY_TIME);
        } else if (c == 2) {
            boolean isSplitScreen = false;
            if (!IS_HW_MULTIWINDOW_SUPPORTED) {
                synchronized (this.mIAtmsInner.getATMS().getGlobalLock()) {
                    ActivityDisplay display = this.mIAtmsInner.getRootActivityContainer().getDefaultDisplay();
                    if (display != null) {
                        if (display.getSplitScreenPrimaryStack() == null) {
                            z = false;
                        }
                        isSplitScreen = z;
                    }
                }
            }
            if (!isSplitScreen && !isInLazyMode) {
                this.mIAtmsInner.getATMS().mWindowManager.setScreenSideBoxAndCornerVisibility(0, false);
            }
        }
    }

    public /* synthetic */ void lambda$updateRecentStatus$0$HwActivityTaskManagerServiceEx() {
        if (!this.mIsEnterRecent) {
            this.mIsInRecentState = false;
            Slog.i(TAG, "update recent status, exit_recent.");
        }
    }

    public boolean isNeedSkipForceStopForHwMultiWindow(String packageName, int userId, String reason) {
        if (!IS_HW_MULTIWINDOW_SUPPORTED || !this.mIsInRecentState || reason == null || !reason.startsWith("iAware") || packageName == null || packageName.isEmpty()) {
            return false;
        }
        List<ActivityManager.RecentTaskInfo> appTasks = getFilteredTasks(userId, -1, packageName, null, false, -1);
        if (appTasks != null) {
            for (ActivityManager.RecentTaskInfo task : appTasks) {
                if (task.windowMode == 102) {
                    Slog.i(TAG, "skip force stop for hw freeform. package:" + packageName + " user:" + userId);
                    return true;
                } else if (task.baseIntent != null && (task.baseIntent.getHwFlags() & 1048576) != 0) {
                    Slog.i(TAG, "skip force stop for one step window. package:" + packageName + " user:" + userId);
                    return true;
                }
            }
        }
        if (!isAppSupportMultiInstance(packageName, userId)) {
            return false;
        }
        Slog.i(TAG, "skip force stop for multiple instance, package:" + packageName + " user:" + userId);
        return true;
    }

    private boolean isAppSupportMultiInstance(String packageName, int userId) {
        ApplicationInfo appInfo = null;
        try {
            appInfo = this.mContext.getPackageManager().getApplicationInfoAsUser(packageName, 128, userId);
        } catch (PackageManager.NameNotFoundException e) {
            Slog.w(TAG, "get application info failed, package:" + packageName);
        }
        return appInfo != null && appInfo.isSupportMultiInstance;
    }

    private void registerRecentTaskCallback() {
        this.mIAtmsInner.getATMS().getRecentTasks().registerCallback(new RecentTasks.Callbacks() {
            /* class com.android.server.wm.HwActivityTaskManagerServiceEx.AnonymousClass10 */

            public void onRecentTaskAdded(TaskRecord task) {
                HwActivityTaskManagerServiceEx.this.handleRecentTaskAdded(task);
            }

            public void onRecentTaskRemoved(TaskRecord task, boolean wasTrimmed, boolean killProcess) {
                HwActivityTaskManagerServiceEx.this.handleRecentTaskRemoved(task);
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleRecentTaskAdded(TaskRecord task) {
        if (task != null && task.intent != null && (task.intent.getHwFlags() & 131072) != 0) {
            String pkgName = task.realActivity != null ? task.realActivity.getPackageName() : null;
            if (pkgName != null) {
                Bundle bundle = new Bundle();
                bundle.putString(KEY_PACKAGE_NAME, pkgName);
                bundle.putInt(KEY_USER_ID, task.userId);
                bundle.putInt(KEY_DISPLAY_ID, task.getStack().mDisplayId);
                Message msg = this.mHwHandler.obtainMessage(92);
                msg.obj = bundle;
                this.mHwHandler.sendMessage(msg);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleRecentTaskRemoved(TaskRecord task) {
        if (task == null) {
            return;
        }
        if (task.inHwFreeFormWindowingMode() || this.mHwMwm.isPadCastStack(task.getStack())) {
            List<TaskRecord> removeTasks = new ArrayList<>(1);
            removeTasks.add(task);
            dispatchFreeformBallLifeState(removeTasks, "remove");
        }
    }

    public void call(Bundle extras) {
        Message msg = this.mHwHandler.obtainMessage(24);
        msg.obj = extras;
        this.mHwHandler.sendMessage(msg);
    }

    public void registerHwActivityNotifier(IHwActivityNotifier notifier, String reason) {
        if (notifier != null && ACTIVITY_NOTIFIER_TYPES.contains(reason) && this.mContext.checkCallingOrSelfPermission("com.huawei.permission.ACTIVITY_NOTIFIER_PERMISSION") == 0) {
            Map<String, String> cookie = new HashMap<>();
            cookie.put("android.intent.extra.REASON", reason);
            cookie.put("android.intent.extra.user_handle", String.valueOf(Binder.getCallingUserHandle().getIdentifier()));
            if (!UserHandle.isApp(Binder.getCallingUid())) {
                Iterator<String> it = ONLY_NOTIFY_SYSTEM_USER_PROCESS.iterator();
                while (true) {
                    if (it.hasNext()) {
                        WindowProcessController wpc = this.mIAtmsInner.getATMS().getProcessController(it.next(), Binder.getCallingUid());
                        if (wpc != null && wpc.mPid == Binder.getCallingPid()) {
                            cookie.put("android.intent.extra.USER", String.valueOf(Binder.getCallingUserHandle().getIdentifier()));
                            break;
                        }
                    } else {
                        break;
                    }
                }
            } else {
                WindowProcessController wpc2 = this.mIAtmsInner.getATMS().getProcessController(Binder.getCallingPid(), Binder.getCallingUid());
                if (wpc2 != null && ONLY_NOTIFY_SYSTEM_USER_PROCESS.contains(wpc2.mInfo.packageName)) {
                    cookie.put("android.intent.extra.USER", String.valueOf(Binder.getCallingUserHandle().getIdentifier()));
                }
            }
            if (Binder.getCallingPid() == Process.myPid()) {
                cookie.put("android.intent.extra.USER", String.valueOf(Binder.getCallingUserHandle().getIdentifier()));
            }
            synchronized (this.mActivityNotifiers) {
                Slog.i(TAG, "registerHwActivityNotifier notifier: " + notifier + ", reason: " + reason + ", callingUid: " + Binder.getCallingUid() + " callingPid: " + Binder.getCallingPid());
                this.mActivityNotifiers.register(notifier, cookie);
            }
        }
    }

    public void unregisterHwActivityNotifier(IHwActivityNotifier notifier) {
        if (notifier != null && this.mContext.checkCallingOrSelfPermission("com.huawei.permission.ACTIVITY_NOTIFIER_PERMISSION") == 0) {
            synchronized (this.mActivityNotifiers) {
                Slog.i(TAG, "unregisterHwActivityNotifier notifier: " + notifier + ", callingUid: " + Binder.getCallingUid() + " callingPid: " + Binder.getCallingPid());
                this.mActivityNotifiers.unregister(notifier);
            }
        }
    }

    public boolean requestContentNode(ComponentName componentName, Bundle data, int token) {
        ActivityDisplay activityDisplay;
        this.mIAtmsInner.getATMS().mAmInternal.enforceCallingPermission("com.huawei.permission.CONTENT_SENSOR_PERMISSION", "requestContentNode()");
        synchronized (this.mIAtmsInner.getATMS().getGlobalLock()) {
            int displayId = 0;
            if (data != null) {
                try {
                    displayId = data.getInt("display_id", 0);
                } catch (Throwable th) {
                    throw th;
                }
            }
            if (displayId == 0) {
                activityDisplay = this.mIAtmsInner.getATMS().getTopDisplayFocusedStack().getTopActivity();
            } else {
                ActivityDisplay activityDisplay2 = getPCActivityDisplay(displayId);
                if (activityDisplay2 != null) {
                    if (activityDisplay2.getFocusedStack() != null) {
                        activityDisplay = activityDisplay2.getFocusedStack().getTopActivity();
                    }
                }
                Slog.w(TAG, "requestContentNode activityDisplay or focusedStack is null displayId: " + displayId);
                return false;
            }
            ActivityRecord realActivity = getTopActivityAppToken(componentName, activityDisplay);
            if (realActivity == null || realActivity.app == null || realActivity.app.mThread == null) {
                Slog.w(TAG, "requestContentNode failed! ");
                return false;
            }
            try {
                realActivity.app.mThread.requestContentNode(realActivity.appToken, data, token);
                return true;
            } catch (RemoteException e) {
                Slog.w(TAG, "requestContentNode failed: crash calling " + realActivity);
                return false;
            }
        }
    }

    public boolean requestContentOther(ComponentName componentName, Bundle data, int token) {
        this.mIAtmsInner.getATMS().mAmInternal.enforceCallingPermission("com.huawei.permission.CONTENT_SENSOR_PERMISSION", "requestContentOther()");
        synchronized (this.mIAtmsInner.getATMS().getGlobalLock()) {
            ActivityRecord realActivity = getTopActivityAppToken(componentName, this.mIAtmsInner.getATMS().getTopDisplayFocusedStack().getTopActivity());
            if (realActivity == null || realActivity.app == null || realActivity.app.mThread == null) {
                return false;
            }
            try {
                realActivity.app.mThread.requestContentOther(realActivity.appToken, data, token);
                return true;
            } catch (RemoteException e) {
                Slog.w(TAG, "requestContentOther failed: crash calling " + realActivity);
                return false;
            }
        }
    }

    public void setResumedActivityUncheckLocked(ActivityRecord from, ActivityRecord to, String reason) {
        ActivityRecord topActivity;
        if (!(from == null || to == null || from.packageName == null || from.packageName.equals(to.packageName))) {
            this.mHwMwm.getPackageNameRotations().remove(from.packageName);
        }
        if (!(to == null || to.packageName == null || "com.huawei.android.launcher".equals(to.packageName) || !to.inHwFreeFormWindowingMode() || to.getActivityStack() == null || to.getActivityStack().getDisplay() == null || to.getActivityStack().getDisplay().mDisplayContent == null)) {
            this.mHwMwm.updateCameraRotatio(to.packageName, to.getActivityStack().getDisplay().mDisplayContent.getRotation(), to.getConfiguration().windowConfiguration.getRotation());
        }
        if (HW_SUPPORT_LAUNCHER_EXIT_ANIM && from != null && to != null && from != to && to.isActivityTypeHome() && "com.huawei.android.launcher".equals(to.packageName)) {
            String packageName = from.packageName;
            TaskRecord tr = from.getTaskRecord();
            if (isStartAppLock(packageName, from.intent.getAction())) {
                packageName = from.intent.getStringExtra("android.intent.extra.PACKAGE_NAME");
            } else if (!(tr == null || tr.getRootActivity() == null)) {
                packageName = tr.getRootActivity().packageName;
            }
            Slog.w(TAG, "setResumedActivityUncheckLocked start call, from: " + from + ", to: " + to);
            Bundle bundle = new Bundle();
            bundle.putString(STRING_INCOMPATIBLE_XML_PKG, packageName);
            bundle.putString("topPackage", from.packageName);
            bundle.putBoolean("isTransluent", from.isTransluent());
            bundle.putInt(KEY_USER_ID, from.mUserId);
            bundle.putString("android.intent.extra.REASON", "returnToHome");
            bundle.putInt("android.intent.extra.user_handle", to.mUserId);
            bundle.putInt("windowingMode", from.getWindowingMode());
            Message msg = this.mHwHandler.obtainMessage(24);
            msg.obj = bundle;
            this.mHwHandler.sendMessageAtFrontOfQueue(msg);
            if (isPadCastStack(from.getActivityStack()) && !from.inMultiWindowMode() && from.task != null) {
                Slog.i(TAG, "pad cast fullscreen window to home. from: " + from);
                List<TaskRecord> handleTasks = new ArrayList<>(1);
                handleTasks.add(from.task);
                dispatchFreeformBallLifeState(handleTasks, "add");
            }
        }
        handleFreeformBallForFocusChange(from, to, reason);
        if (to != null && (from == null || !from.packageName.equals(to.packageName) || from.getUid() != to.getUid())) {
            String str = null;
            LogPower.push((int) APP_FOCUS_CHANGE, from == null ? null : from.packageName, to.packageName);
            StringBuilder sb = new StringBuilder();
            sb.append("appSwitch from: ");
            if (from != null) {
                str = from.packageName;
            }
            sb.append(str);
            sb.append(" to: ");
            sb.append(to.packageName);
            Slog.w(TAG, sb.toString());
            Bundle bundle2 = new Bundle();
            if (from != null) {
                bundle2.putString("fromPackage", from.packageName);
                bundle2.putInt("fromUid", from.getUid());
            }
            bundle2.putString("toPackage", to.packageName);
            bundle2.putInt("toUid", to.getUid());
            bundle2.putInt("toDisplayId", to.getDisplayId());
            bundle2.putString("android.intent.extra.REASON", "appSwitch");
            bundle2.putInt("android.intent.extra.user_handle", this.mIAtmsInner.getATMS().mWindowManager.mCurrentUserId);
            TaskRecord task = to.getTaskRecord();
            if (task != null) {
                bundle2.putInt("toTaskId", task.taskId);
                bundle2.putBoolean("toTaskInMultiWindowMode", task.inMultiWindowMode());
            }
            bundle2.putParcelable("toActivity", to.info.getComponentName());
            bundle2.putInt("toPid", to.app != null ? to.app.mPid : 0);
            bundle2.putInt("windowingMode", to.getWindowingMode());
            bundle2.putString("toProcessName", to.processName);
            Message msg2 = this.mHwHandler.obtainMessage(24);
            msg2.obj = bundle2;
            this.mHwHandler.sendMessage(msg2);
            sendPadCastSwitchMsg(to, bundle2);
            if (DisplayRotation.IS_SWING_ENABLED) {
                Bundle swingBundle = new Bundle();
                swingBundle.putString(HwCertification.KEY_PACKAGE_NAME, to.packageName);
                Message swingMsg = this.mHwHandler.obtainMessage(NOTIFY_NEED_SWING_ROTATION);
                swingMsg.obj = swingBundle;
                this.mHwHandler.sendMessage(swingMsg);
            }
            if (from != null && from.inHwMultiStackWindowingMode() && !to.inMultiWindowMode() && from.getActivityStack() != to.getActivityStack()) {
                ActivityDisplay display = from.getDisplay();
                if (display != null) {
                    for (int stackNdx = display.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                        ActivityStack stack = display.getChildAt(stackNdx);
                        if (!(stack.inMultiWindowMode() || stack == to.getActivityStack())) {
                            break;
                        }
                        if (stack.inHwMultiStackWindowingMode() && !stack.isAlwaysOnTop() && (topActivity = stack.getTopActivity()) != null && topActivity.visible && topActivity.appToken != null) {
                            Slog.v(TAG, "takeTaskSnapShot multiwindow package " + topActivity.packageName);
                            this.mIAtmsInner.getATMS().mWindowManager.getWindowManagerServiceEx().takeTaskSnapshot(topActivity.appToken, false);
                        }
                    }
                }
            } else if (from != null && !from.inMultiWindowMode() && !to.inMultiWindowMode() && HW_SNAPSHOT && from.getActivityStack() != to.getActivityStack() && from.getConfiguration().orientation == 2 && from.appToken != null) {
                Slog.v(TAG, "takeTaskSnapShot package " + from.packageName);
                this.mIAtmsInner.getATMS().mWindowManager.getWindowManagerServiceEx().takeTaskSnapshot(from.appToken, false);
            }
        }
        this.mPreviousResumedActivity = from;
    }

    private void sendPadCastSwitchMsg(ActivityRecord to, Bundle bundle) {
        if (this.mHwMdm.isVirtualDisplayId(to.getDisplayId(), "padCast") && this.mHwMdm.getPadCastBlackList().contains(to.packageName)) {
            bundle.putString("android.intent.extra.REASON", "switchToDefaultDisplay");
            bundle.putString("subReason", "appSwitch");
            Message switchMsg = this.mHwHandler.obtainMessage(24);
            switchMsg.obj = bundle;
            this.mHwHandler.sendMessage(switchMsg);
            Slog.i(TAG, "notify switchToDefaultDisplay displayId=" + to.getDisplayId() + " packageName=" + to.packageName);
        }
    }

    private void handleFreeformBallForFocusChange(ActivityRecord from, ActivityRecord to, String reason) {
        if (from != null && to != null && from != to) {
            TaskRecord fromTask = from.getTaskRecord();
            TaskRecord toTask = to.getTaskRecord();
            if (fromTask != null && toTask != null && fromTask != toTask) {
                if (from.visible && to.visible && ("setFocusedTask".equals(reason) || "setFocusedStack".equals(reason))) {
                    return;
                }
                if (!"appDied".equals(reason) || !this.mHwMwm.isPadCastStack(from.getActivityStack())) {
                    if (fromTask.inHwFreeFormWindowingMode() && ((!from.isAlwaysOnTop() || (from.finishing && fromTask.getTopActivity() == null)) && this.mIAtmsInner.getRecentRawTasks().contains(fromTask))) {
                        List<TaskRecord> addTasks = new ArrayList<>(1);
                        addTasks.add(fromTask);
                        dispatchFreeformBallLifeState(addTasks, "add");
                    }
                    if (toTask.inHwFreeFormWindowingMode() && to.isAlwaysOnTop()) {
                        List<TaskRecord> removeTasks = new ArrayList<>(1);
                        removeTasks.add(toTask);
                        dispatchFreeformBallLifeState(removeTasks, "remove");
                    }
                }
            }
        }
    }

    public void dispatchActivityLifeState(ActivityRecord r, String state) {
        if (r != null) {
            TaskRecord task = r.task;
            if (sTranslucentWhitelists.contains(r.info.name) && task != null) {
                int i = task.mActivities.size() - 1;
                while (true) {
                    if (i >= 0) {
                        ActivityRecord activityRecord = (ActivityRecord) task.mActivities.get(i);
                        if (activityRecord != null && !activityRecord.finishing && activityRecord != r) {
                            r = activityRecord;
                            break;
                        }
                        i--;
                    } else {
                        break;
                    }
                }
            }
            handleStartActivity(r, state);
            handlePadCastFullStackBallState(r, state);
            Bundle bundle = new Bundle();
            if (r.app != null) {
                bundle.putInt("uid", r.app.mUid);
                bundle.putInt(ASSOC_PID, r.app.mPid);
            }
            ComponentName comp = r.info.getComponentName();
            Rect bounds = r.getBounds();
            int displayMode = 0;
            if (HwFoldScreenState.isFoldScreenDevice()) {
                displayMode = ((HwFoldScreenManagerInternal) LocalServices.getService(HwFoldScreenManagerInternal.class)).getDisplayMode();
                bundle.putInt("displayMode", displayMode);
            }
            if (displayMode != 1 && (r.maxAspectRatio <= 0.0f || r.maxAspectRatio >= sDeviceMaxRatio || sWhitelistActivities.contains(comp.getClassName()))) {
                bounds = new Rect();
            } else if (displayMode == 1 && (r.minAspectRatio <= 0.0f || ((double) Math.abs(r.minAspectRatio - HwFoldScreenState.getScreenFoldFullRatio())) < 1.0E-8d)) {
                bounds = new Rect();
            } else if (r.inMultiWindowMode()) {
                bounds = new Rect();
            }
            if (LIFECYCLE_ON_RESUME.equals(state)) {
                bundle.putBoolean("isTop", r == this.mIAtmsInner.getLastResumedActivityRecord() || r.getActivityStack() == this.mHwMwm.getFilteredTopStack(r.getDisplay(), true));
            }
            bundle.putString("state", state);
            bundle.putParcelable("comp", comp);
            bundle.putParcelable("bounds", bounds);
            bundle.putBoolean("isFloating", sPipWhitelists.contains(comp.getClassName()) || r.isFloating());
            bundle.putFloat("maxRatio", r.maxAspectRatio);
            bundle.putBoolean("isHomeActivity", r.isActivityTypeHome());
            TaskRecord task2 = r.task;
            if (task2 != null) {
                bundle.putInt(HwWmConstants.TASK_ID, task2.taskId);
            }
            bundle.putInt("windowingMode", r.getWindowingMode());
            bundle.putString("android.intent.extra.REASON", "activityLifeState");
            bundle.putInt("android.intent.extra.user_handle", this.mIAtmsInner.getATMS().mWindowManager.mCurrentUserId);
            Message msg = this.mHwHandler.obtainMessage(24);
            msg.obj = bundle;
            this.mHwHandler.sendMessage(msg);
            if (LIFECYCLE_ON_RESUME.equals(state) && HwMwUtils.ENABLED) {
                HwMwUtils.performPolicy(51, new Object[]{r.appToken, comp.getPackageName()});
            }
        }
    }

    private void handleStartActivity(ActivityRecord r, String state) {
        ActivityStack stack = null;
        if (LIFECYCLE_ON_RESUME.equals(state) && r.getWindowingMode() == 102) {
            try {
                stack = r.getActivityStack();
                if (!(stack == null || r.getTaskRecord() == null)) {
                    if (stack.mTaskStack != null) {
                        if (this.mHwMultiWindowTip.getFreeformGuideCount() == 0 && this.mHwMwm.mFreeformGuideMap.containsKey(Integer.valueOf(stack.mStackId))) {
                            if (!ActivityStartInterceptorBridge.isAppLockActivity(r.shortComponentName) && !DOCKBAR_MULTITASK_MGR_CLASS.equals(r.shortComponentName)) {
                                HwMultiWindowManager hwMultiWindowManager = this.mHwMwm;
                                if (!HwMultiWindowManager.sRemoveFromBallActivitys.contains(r.shortComponentName)) {
                                    Rect defaultBounds = new Rect();
                                    this.mHwMwm.calcDefaultFreeFormBounds(defaultBounds, r.getActivityStack(), false, false);
                                    Slog.i(TAG, "handleStartGuideActivity:" + r + " defaultBounds:" + defaultBounds);
                                    TaskStack taskStack = r.getActivityStack().mTaskStack;
                                    Rect visibleRect = new Rect(taskStack.getBounds());
                                    float scale = taskStack.mHwStackScale;
                                    if (defaultBounds.equals(visibleRect) && Math.abs(scale - this.mHwMwm.ratio) < 1.0E-4f) {
                                        Intent intent = new Intent();
                                        HwMultiWindowManager hwMultiWindowManager2 = this.mHwMwm;
                                        intent.setComponent(new ComponentName(HW_DOCK_PACKAGE_NAME, HwMultiWindowManager.HW_DOCK_START_ACTIVITY));
                                        intent.addFlags(268435456);
                                        Bundle bundle = new Bundle();
                                        bundle.putParcelable("rect", visibleRect);
                                        bundle.putInt(HwWmConstants.TASK_ID, r.getTaskRecord().taskId);
                                        bundle.putInt(KEY_USER_ID, r.getTaskRecord().userId);
                                        ComponentName realActivity = r.getTaskRecord().realActivity;
                                        if (realActivity != null) {
                                            bundle.putString("pkg", realActivity.getPackageName());
                                        } else {
                                            bundle.putString("pkg", r.packageName);
                                        }
                                        bundle.putFloat("scale", scale);
                                        intent.putExtra("bundle", bundle);
                                        long identity = Binder.clearCallingIdentity();
                                        try {
                                            this.mContext.startActivityAsUser(intent, UserHandle.of(r.getTaskRecord().userId));
                                        } finally {
                                            Binder.restoreCallingIdentity(identity);
                                        }
                                    }
                                }
                            }
                            if (this.mHwMwm.mFreeformGuideMap.containsKey(Integer.valueOf(stack.getStackId()))) {
                                this.mHwMwm.mFreeformGuideMap.remove(Integer.valueOf(stack.getStackId()));
                                return;
                            }
                            return;
                        }
                        if (!this.mHwMwm.mFreeformGuideMap.containsKey(Integer.valueOf(stack.getStackId()))) {
                            return;
                        }
                        this.mHwMwm.mFreeformGuideMap.remove(Integer.valueOf(stack.getStackId()));
                        return;
                    }
                }
                Slog.i(TAG, "handleStartGuideActivity stack is null");
                if (stack != null && this.mHwMwm.mFreeformGuideMap.containsKey(Integer.valueOf(stack.getStackId()))) {
                    this.mHwMwm.mFreeformGuideMap.remove(Integer.valueOf(stack.getStackId()));
                }
            } catch (ActivityNotFoundException e) {
                Log.e(TAG, "start guide activity fail");
                if (0 == 0 || !this.mHwMwm.mFreeformGuideMap.containsKey(Integer.valueOf(stack.getStackId()))) {
                }
            } catch (Throwable th) {
                if (0 != 0 && this.mHwMwm.mFreeformGuideMap.containsKey(Integer.valueOf(stack.getStackId()))) {
                    this.mHwMwm.mFreeformGuideMap.remove(Integer.valueOf(stack.getStackId()));
                }
                throw th;
            }
        }
    }

    private void handlePadCastFullStackBallState(ActivityRecord r, String state) {
        if (this.mHwMwm.isPadCastStack(r.getActivityStack()) && !r.inMultiWindowMode() && r.task != null) {
            String reason = null;
            if (LIFECYCLE_ON_RESUME.equals(state)) {
                reason = "remove";
            } else if ("onPause".equals(state) && !r.task.isVisible()) {
                reason = "add";
            }
            if (!TextUtils.isEmpty(reason)) {
                List<TaskRecord> handleTasks = new ArrayList<>(1);
                handleTasks.add(r.task);
                dispatchFreeformBallLifeState(handleTasks, reason);
            }
        }
    }

    public HashMap<String, Integer> getPkgDisplayMaps() {
        return this.mPkgDisplayMaps;
    }

    public int canAppBoost(ActivityInfo aInfo, boolean isScreenOn) {
        String packageName;
        if (isScreenOn || aInfo == null) {
            return 1;
        }
        int type = MultiTaskUtils.getAppType(-1, aInfo.applicationInfo);
        if (type == 4) {
            return 0;
        }
        if (type == 1 || (packageName = aInfo.packageName) == null || packageName.startsWith("com.huawei")) {
            return 1;
        }
        return 0;
    }

    public boolean isTaskSupportResize(int taskId, boolean isFullscreen, boolean isMaximized) {
        DefaultHwPCMultiWindowManager multiWindowMgr;
        Object instance;
        if (!HwPCUtils.isPcCastModeInServer() || (multiWindowMgr = getHwPCMultiWindowManager(buildAtmsEx(this.mIAtmsInner.getATMS()))) == null || !(this.mIAtmsInner.getStackSupervisor() instanceof ActivityStackSupervisorBridge) || (instance = this.mIAtmsInner.getRootActivityContainer()) == null || !(instance instanceof RootActivityContainer)) {
            return false;
        }
        RootActivityContainer rootActivityContainer = (RootActivityContainer) instance;
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mIAtmsInner.getATMS().getGlobalLock()) {
                TaskRecord task = rootActivityContainer.anyTaskForId(taskId);
                if (task == null) {
                    return false;
                }
                boolean isSupportResize = multiWindowMgr.isSupportResize(buildTaskRecordEx(task), isFullscreen, isMaximized);
                Binder.restoreCallingIdentity(origId);
                return isSupportResize;
            }
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    public boolean isSupportsSplitScreenWindowingMode(IBinder activityToken) {
        boolean z;
        synchronized (this.mIAtmsInner.getATMS().getGlobalLock()) {
            ActivityRecord ar = ActivityRecord.forTokenLocked(activityToken);
            z = (ar == null || ar.task == null || !ar.task.supportsSplitScreenWindowingMode()) ? false : true;
        }
        return z;
    }

    public int getTopTaskIdInDisplay(int displayId, String pkgName, boolean invisibleAlso) {
        if (displayId == 0 || !HwPCUtils.isPcCastModeInServer() || HwPCUtils.isValidExtDisplayId(displayId)) {
            int childCount = this.mIAtmsInner.getATMS().mRootActivityContainer.getChildCount();
            if (Log.HWINFO) {
                HwPCUtils.log(TAG, "getTopTaskIdInDisplay displayId = " + displayId + ", childCount = " + childCount + ", pkgName = " + pkgName);
            }
            if (displayId < 0) {
                return -1;
            }
            ActivityDisplay activityDisplay = getPCActivityDisplay(displayId);
            if (activityDisplay == null) {
                HwPCUtils.log(TAG, "getTopTaskIdInDisplay activityDisplay not exist");
                return -1;
            }
            for (int stackNdx = activityDisplay.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                int taskId = getSpecialTaskId(displayId == 0, activityDisplay.getChildAt(stackNdx), pkgName, invisibleAlso);
                if (taskId != -1) {
                    return taskId;
                }
            }
            return -1;
        }
        Slog.e(TAG, "is not a valid pc display id");
        return -1;
    }

    private ActivityDisplay getPCActivityDisplay(int displayId) {
        RootActivityContainer rootActivityContainer = this.mIAtmsInner.getATMS().mRootActivityContainer;
        if (rootActivityContainer == null) {
            return null;
        }
        for (int i = rootActivityContainer.getChildCount() - 1; i >= 0; i--) {
            if (rootActivityContainer.getChildAt(i).mDisplayId == displayId) {
                return rootActivityContainer.getChildAt(i);
            }
        }
        return null;
    }

    public Rect getPCTopTaskBounds(int displayId) {
        if (!HwPCUtils.isPcCastModeInServer() || HwPCUtils.isValidExtDisplayId(displayId)) {
            HwPCUtils.log(TAG, "getPCTopTaskBounds displayId = " + displayId + ", childCount = " + this.mIAtmsInner.getATMS().mRootActivityContainer.getChildCount());
            if (displayId < 0) {
                return null;
            }
            ActivityDisplay activityDisplay = getPCActivityDisplay(displayId);
            if (activityDisplay == null) {
                HwPCUtils.log(TAG, "getPCTopTaskBounds activityDisplay not exist");
                return null;
            }
            Rect rect = new Rect();
            for (int stackNdx = activityDisplay.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                TaskRecord tr = activityDisplay.getChildAt(stackNdx).topTask();
                if (tr != null && tr.isVisible()) {
                    tr.getWindowContainerBounds(rect);
                    HwPCUtils.log(TAG, "getTaskIdInPCDisplayLocked tr.taskId = " + tr.taskId + ", rect = " + rect);
                    return rect;
                }
            }
            return null;
        }
        Slog.e(TAG, "is not a valid pc display id");
        return null;
    }

    private int getSpecialTaskId(boolean isDefaultDisplay, ActivityStack stack, String pkgName, boolean invisibleAlso) {
        if (pkgName != null && !"".equals(pkgName)) {
            ArrayList<TaskRecord> tasks = stack.getAllTasks();
            for (int taskNdx = tasks.size() - 1; taskNdx >= 0; taskNdx--) {
                TaskRecord tr = tasks.get(taskNdx);
                if (tr != null && (invisibleAlso || tr.isVisible())) {
                    ActivityRecord[] candicatedArs = {tr.topRunningActivityLocked(), tr.getRootActivity()};
                    for (ActivityRecord ar : candicatedArs) {
                        HwPCUtils.log(TAG, "getSpecialTaskId ar = " + ar + ", tr.isVisible() = " + tr.isVisible());
                        if (!(ar == null || ar.packageName == null || !ar.packageName.equals(pkgName))) {
                            return tr.taskId;
                        }
                    }
                    continue;
                }
            }
            return -1;
        } else if (!isDefaultDisplay) {
            TaskRecord tr2 = stack.topTask();
            if (tr2 == null) {
                return -1;
            }
            if (!(invisibleAlso || tr2.isVisible())) {
                return -1;
            }
            HwPCUtils.log(TAG, "getSpecialTaskId tr.taskId = " + tr2.taskId);
            return tr2.taskId;
        } else {
            ArrayList<TaskRecord> tasks2 = stack.getAllTasks();
            for (int taskNdx2 = tasks2.size() - 1; taskNdx2 >= 0; taskNdx2--) {
                TaskRecord tr3 = tasks2.get(taskNdx2);
                if (tr3 != null && (invisibleAlso || tr3.isVisible())) {
                    return tr3.taskId;
                }
            }
            return -1;
        }
    }

    public void noteActivityStart(String packageName, String processName, String activityName, int pid, int uid, boolean started) {
        WindowProcessController app;
        if (Swap.getInstance().isSwapEnabled()) {
            SceneConst.ScenePara temp = new SceneConst.ScenePara(packageName, uid);
            temp.setActivityName(activityName);
            SceneProcessing.getInstance().notifySceneData("SWITCH_TO_FOREGROUND", temp);
        }
        if (this.mIAtmsInner.getSystemReady()) {
            if (pid < 1 && (app = this.mIAtmsInner.getProcessControllerForHwAtmsEx(processName, uid)) != null) {
                pid = app.getPid();
            }
            this.mIAtmsInner.getAtmDAMonitor().noteActivityStart(packageName, processName, activityName, pid, uid, started);
            if (!started) {
                HwApsImpl.notifyActivityIdle(packageName, processName, activityName);
            }
        }
    }

    public boolean noteActivityInitializing(ActivityRecord startActivity, ActivityRecord reusedActivity) {
        WindowProcessController app;
        TaskRecord taskRecord;
        boolean unReady = !this.mIAtmsInner.getSystemReady() || startActivity == null || startActivity.info == null;
        if (HwMwUtils.ENABLED && reusedActivity != null) {
            HwMwUtils.performPolicy(136, new Object[]{reusedActivity});
        }
        if (unReady) {
            return false;
        }
        if ((reusedActivity != null && ((taskRecord = reusedActivity.task) == null || taskRecord.realActivity == null || taskRecord.realActivity.equals(startActivity.mActivityComponent) || taskRecord.findActivityInHistoryLocked(startActivity) != null)) || (app = this.mIAtmsInner.getProcessControllerForHwAtmsEx(startActivity.processName, startActivity.getUid())) == null || app.getPid() <= 0) {
            return false;
        }
        this.mIAtmsInner.getAtmDAMonitor().noteActivityDisplayed(startActivity.shortComponentName, startActivity.getUid(), app.getPid(), true);
        return true;
    }

    public void noteActivityDisplayed(String componentName, int uid, int pid, boolean isStart) {
        if (this.mIAtmsInner.getSystemReady()) {
            this.mIAtmsInner.getAtmDAMonitor().noteActivityDisplayed(componentName, uid, pid, isStart);
        }
    }

    public boolean isInMultiWindowMode() {
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mIAtmsInner.getATMS().getGlobalLock()) {
                ActivityStack focusedStack = this.mIAtmsInner.getRootActivityContainer().getTopDisplayFocusedStack();
                boolean z = false;
                if (focusedStack == null) {
                    return false;
                }
                if (focusedStack.inMultiWindowMode() && !focusedStack.inHwMagicWindowingMode()) {
                    z = true;
                }
                Binder.restoreCallingIdentity(origId);
                return z;
            }
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        if (this.isLastMultiMode != isInMultiWindowMode) {
            this.isLastMultiMode = isInMultiWindowMode;
            Message msg = this.mHwHandler.obtainMessage(23);
            msg.obj = Boolean.valueOf(isInMultiWindowMode);
            this.mHwHandler.sendMessage(msg);
        }
    }

    public boolean registerThirdPartyCallBack(IMWThirdpartyCallback aCallBackHandler) {
        boolean lRegistered = false;
        if (aCallBackHandler != null) {
            synchronized (this.mThirdPartyCallbackList) {
                lRegistered = this.mThirdPartyCallbackList.register(aCallBackHandler);
            }
        }
        return lRegistered;
    }

    public boolean unregisterThirdPartyCallBack(IMWThirdpartyCallback aCallBackHandler) {
        boolean lUnregistered = false;
        if (aCallBackHandler != null) {
            synchronized (this.mThirdPartyCallbackList) {
                lUnregistered = this.mThirdPartyCallbackList.unregister(aCallBackHandler);
            }
        }
        return lUnregistered;
    }

    public boolean isTaskVisible(int id) {
        ActivityStackSupervisor mStackSupervisor;
        TaskRecord tr;
        int callerUid = Binder.getCallingUid();
        if (callerUid == 1000) {
            IHwActivityTaskManagerInner iHwActivityTaskManagerInner = this.mIAtmsInner;
            if (iHwActivityTaskManagerInner == null || (mStackSupervisor = iHwActivityTaskManagerInner.getStackSupervisor()) == null || (tr = mStackSupervisor.mRootActivityContainer.anyTaskForId(id)) == null || tr.getTopActivity() == null || !tr.getTopActivity().visible) {
                return false;
            }
            return true;
        }
        throw new SecurityException("Process with uid=" + callerUid + " cannot call function isTaskVisible.");
    }

    private void initTrustSpace() {
        this.mTrustSpaceController = HwServiceFactory.getTrustSpaceController();
        ITrustSpaceController iTrustSpaceController = this.mTrustSpaceController;
        if (iTrustSpaceController != null) {
            iTrustSpaceController.initTrustSpace();
        }
    }

    private boolean shouldPreventStartComponent(int type, String calleePackage, int callerUid, int callerPid, String callerPackage, int userId) {
        ISecurityProfileController spc;
        boolean shouldPrevent = false;
        if (this.mIAtmsInner.getSystemReady()) {
            long ident = Binder.clearCallingIdentity();
            try {
                if (this.mTrustSpaceController != null) {
                    shouldPrevent = this.mTrustSpaceController.checkIntent(type, calleePackage, callerUid, callerPid, callerPackage, userId);
                }
                ISecurityProfileController spc2 = HwServiceFactory.getSecurityProfileController();
                if (spc2 != null) {
                    try {
                    } catch (Throwable th) {
                        spc = th;
                        Binder.restoreCallingIdentity(ident);
                        throw spc;
                    }
                    try {
                        shouldPrevent |= spc2.shouldPreventInteraction(type, calleePackage, new IntentCaller(callerPackage, callerUid, callerPid), userId);
                    } catch (Throwable th2) {
                        spc = th2;
                        Binder.restoreCallingIdentity(ident);
                        throw spc;
                    }
                }
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th3) {
                spc = th3;
                Binder.restoreCallingIdentity(ident);
                throw spc;
            }
        }
        return shouldPrevent;
    }

    public boolean shouldPreventStartService(ServiceInfo sInfo, int callerUid, int callerPid, String callerPackage, int userId) {
        if (sInfo == null) {
            return false;
        }
        return shouldPreventStartComponent(2, sInfo.applicationInfo.packageName, callerUid, callerPid, callerPackage, userId);
    }

    private boolean iAwareShouldPreventActivity(Intent intent, ActivityInfo aInfo, int callerPid, int callerUid, WindowProcessController callerApp) {
        if (intent == null || aInfo == null) {
            return false;
        }
        if (this.mIAtmsInner.getATMS().isSleepingLocked() && sPreventStartWhenSleeping.contains(aInfo.getComponentName().flattenToShortString())) {
            return true;
        }
        boolean shouldPrevent = HwPartIawareUtil.shouldPreventStartActivity(intent, aInfo, callerPid, callerUid, new WindowProcessControllerEx(callerApp));
        if (!shouldPrevent) {
            return HwPartIawareUtil.shouldPreventStartActivity(aInfo, callerPid, callerUid);
        }
        return shouldPrevent;
    }

    private boolean isInstall(Intent intent) {
        ComponentName componentName;
        if (intent == null) {
            return false;
        }
        String action = intent.getAction();
        String type = intent.getType();
        boolean story = false;
        if ("android.intent.action.INSTALL_PACKAGE".equals(action)) {
            story = true;
        }
        if ("application/vnd.android.package-archive".equals(type)) {
            story = true;
        }
        if (!"android.intent.action.VIEW".equals(action) || (componentName = intent.getComponent()) == null || !"com.android.packageinstaller".equals(componentName.getPackageName())) {
            return story;
        }
        return true;
    }

    private boolean mdmShouldPreventStartActivity(Intent intent, WindowProcessController callerApp) {
        String callerPackage = null;
        if (callerApp != null) {
            callerPackage = callerApp.mInfo.packageName;
        }
        if (callerPackage == null) {
            return false;
        }
        String callerIndex = null;
        if (!isInstall(intent)) {
            return false;
        }
        try {
            callerIndex = intent.getStringExtra("caller_package");
        } catch (Exception e) {
            Slog.e(TAG, "mdmShouldPreventStartActivity, Get package info faild catch Exception");
        }
        if (callerIndex != null && "com.android.packageinstaller".equals(callerPackage)) {
            callerPackage = callerIndex;
        }
        intent.putExtra("caller_package", callerPackage);
        if (!HwDeviceManager.disallowOp(intent)) {
            return false;
        }
        Slog.i(TAG, "due to disallow op launching activity aborted");
        this.mIAtmsInner.getATMS().mUiHandler.post(new Runnable() {
            /* class com.android.server.wm.$$Lambda$HwActivityTaskManagerServiceEx$NmWp3kOu8G1yI6T4x2RLzdSbe3o */

            @Override // java.lang.Runnable
            public final void run() {
                HwActivityTaskManagerServiceEx.this.lambda$mdmShouldPreventStartActivity$1$HwActivityTaskManagerServiceEx();
            }
        });
        return true;
    }

    public /* synthetic */ void lambda$mdmShouldPreventStartActivity$1$HwActivityTaskManagerServiceEx() {
        Context context = this.mContext;
        if (context != null) {
            Toast toast = Toast.makeText(context, context.getString(33686023), 0);
            toast.getWindowParams().privateFlags |= 16;
            toast.show();
        }
    }

    public boolean shouldPreventStartActivity(ActivityInfo aInfo, int callerUid, int callerPid, String callerPackage, int userId, Intent intent, WindowProcessController callerApp, ActivityOptions options) {
        Throwable th;
        ActivityStack uncombinedStack;
        ActivityStack uncombinedStack2;
        ActivityStack uncombinedStack3;
        ActivityStack uncombinedStack4;
        ActivityStack uncombinedStack5;
        ActivityStack uncombinedStack6;
        ActivityStack uncombinedStack7;
        if (aInfo == null) {
            if (!(0 == 0 || options == null)) {
                int windowingMode = options.getLaunchWindowingMode();
                if (!(!WindowConfiguration.isHwSplitScreenWindowingMode(windowingMode) || (uncombinedStack7 = this.mHwMwm.findUncombinedSplitScreenStack()) == null || uncombinedStack7.getWindowingMode() == windowingMode)) {
                    Slog.i(TAG, "exit uncombined stack: " + uncombinedStack7.toShortString());
                    HwMultiWindowManager.exitHwMultiStack(uncombinedStack7);
                }
            }
            return false;
        }
        try {
            if (!HwActivityManagerServiceEx.checkTvBlackList(aInfo.applicationInfo)) {
                Slog.e(TAG, "packageName is in tv black list, start activity permission denied " + aInfo.packageName);
                if (!(1 == 0 || options == null)) {
                    int windowingMode2 = options.getLaunchWindowingMode();
                    if (!(!WindowConfiguration.isHwSplitScreenWindowingMode(windowingMode2) || (uncombinedStack6 = this.mHwMwm.findUncombinedSplitScreenStack()) == null || uncombinedStack6.getWindowingMode() == windowingMode2)) {
                        Slog.i(TAG, "exit uncombined stack: " + uncombinedStack6.toShortString());
                        HwMultiWindowManager.exitHwMultiStack(uncombinedStack6);
                    }
                }
                return true;
            }
            try {
                if (!isSkipIawareCheck(callerUid, callerPid, callerPackage) && iAwareShouldPreventActivity(intent, aInfo, callerPid, callerUid, callerApp)) {
                    if (!(1 == 0 || options == null)) {
                        int windowingMode3 = options.getLaunchWindowingMode();
                        if (!(!WindowConfiguration.isHwSplitScreenWindowingMode(windowingMode3) || (uncombinedStack5 = this.mHwMwm.findUncombinedSplitScreenStack()) == null || uncombinedStack5.getWindowingMode() == windowingMode3)) {
                            Slog.i(TAG, "exit uncombined stack: " + uncombinedStack5.toShortString());
                            HwMultiWindowManager.exitHwMultiStack(uncombinedStack5);
                        }
                    }
                    return true;
                } else if (mdmShouldPreventStartActivity(intent, callerApp)) {
                    if (!(1 == 0 || options == null)) {
                        int windowingMode4 = options.getLaunchWindowingMode();
                        if (!(!WindowConfiguration.isHwSplitScreenWindowingMode(windowingMode4) || (uncombinedStack4 = this.mHwMwm.findUncombinedSplitScreenStack()) == null || uncombinedStack4.getWindowingMode() == windowingMode4)) {
                            Slog.i(TAG, "exit uncombined stack: " + uncombinedStack4.toShortString());
                            HwMultiWindowManager.exitHwMultiStack(uncombinedStack4);
                        }
                    }
                    return true;
                } else if (customActivityStarting(intent, aInfo.applicationInfo.packageName)) {
                    if (!(1 == 0 || options == null)) {
                        int windowingMode5 = options.getLaunchWindowingMode();
                        if (!(!WindowConfiguration.isHwSplitScreenWindowingMode(windowingMode5) || (uncombinedStack3 = this.mHwMwm.findUncombinedSplitScreenStack()) == null || uncombinedStack3.getWindowingMode() == windowingMode5)) {
                            Slog.i(TAG, "exit uncombined stack: " + uncombinedStack3.toShortString());
                            HwMultiWindowManager.exitHwMultiStack(uncombinedStack3);
                        }
                    }
                    return true;
                } else {
                    boolean result = shouldPreventStartComponent(0, aInfo.applicationInfo.packageName, callerUid, callerPid, callerPackage, userId);
                    if (result && options != null) {
                        int windowingMode6 = options.getLaunchWindowingMode();
                        if (!(!WindowConfiguration.isHwSplitScreenWindowingMode(windowingMode6) || (uncombinedStack2 = this.mHwMwm.findUncombinedSplitScreenStack()) == null || uncombinedStack2.getWindowingMode() == windowingMode6)) {
                            Slog.i(TAG, "exit uncombined stack: " + uncombinedStack2.toShortString());
                            HwMultiWindowManager.exitHwMultiStack(uncombinedStack2);
                        }
                    }
                    return result;
                }
            } catch (Throwable th2) {
                th = th2;
                if (!(0 == 0 || options == null)) {
                    int windowingMode7 = options.getLaunchWindowingMode();
                    if (!(!WindowConfiguration.isHwSplitScreenWindowingMode(windowingMode7) || (uncombinedStack = this.mHwMwm.findUncombinedSplitScreenStack()) == null || uncombinedStack.getWindowingMode() == windowingMode7)) {
                        Slog.i(TAG, "exit uncombined stack: " + uncombinedStack.toShortString());
                        HwMultiWindowManager.exitHwMultiStack(uncombinedStack);
                    }
                }
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            int windowingMode72 = options.getLaunchWindowingMode();
            Slog.i(TAG, "exit uncombined stack: " + uncombinedStack.toShortString());
            HwMultiWindowManager.exitHwMultiStack(uncombinedStack);
            throw th;
        }
    }

    private boolean isSkipIawareCheck(int callerUid, int callerPid, String callerPackage) {
        boolean isAllowed = false;
        if (!HW_DOCK_PACKAGE_NAME.equals(callerPackage)) {
            return false;
        }
        this.mIAtmsInner.getATMS();
        if (ActivityTaskManagerService.checkPermission("android.permission.START_ACTIVITIES_FROM_BACKGROUND", callerPid, callerUid) == 0) {
            isAllowed = true;
        }
        return isAllowed;
    }

    public boolean shouldPreventStartProvider(ProviderInfo cpi, int callerUid, int callerPid, String callerPackage, int userId) {
        if (cpi == null) {
            return false;
        }
        return shouldPreventStartComponent(3, cpi.packageName, callerUid, callerPid, callerPackage, userId);
    }

    public boolean shouldPreventSendBroadcast(Intent intent, String receiver, int callerUid, int callerPid, String callingPackage, int userId) {
        return shouldPreventStartComponent(1, receiver, callerUid, callerPid, callingPackage, userId);
    }

    public void dismissSplitScreenModeWithFinish(ActivityRecord r) {
        ActivityRecord nextTargetAR;
        if (r.getWindowingMode() == 4 && r.getActivityType() == 1) {
            ActivityStackSupervisor mStackSupervisor = this.mIAtmsInner.getStackSupervisor();
            RootActivityContainer mRootActivityContainer = this.mIAtmsInner.getRootActivityContainer();
            if (mStackSupervisor == null) {
                Slog.w(TAG, "dismissSplitScreenModeWithFinish:mStackSupervisor not found.");
            } else if (r.info.name.contains(SPLIT_SCREEN_APP_NAME)) {
                dismissSplitScreenToPrimaryStack(mRootActivityContainer);
            } else {
                ActivityStack nextTargetAs = mStackSupervisor.getNextStackInSplitSecondary(r.getActivityStack());
                if (nextTargetAs != null && (nextTargetAR = nextTargetAs.topRunningActivityLocked()) != null && nextTargetAR.info.name.contains(SPLIT_SCREEN_APP_NAME)) {
                    dismissSplitScreenToPrimaryStack(mRootActivityContainer);
                }
            }
        }
    }

    private final class SettingsObserver extends ContentObserver {
        private static final String KEY_HW_UPGRADE_REMIND = "hw_upgrade_remind";
        private final Uri URI_HW_UPGRADE_REMIND = Settings.Secure.getUriFor(KEY_HW_UPGRADE_REMIND);

        SettingsObserver(Handler handler) {
            super(handler);
        }

        public void init() {
            ContentResolver resolver = HwActivityTaskManagerServiceEx.this.mContext.getContentResolver();
            boolean z = false;
            resolver.registerContentObserver(this.URI_HW_UPGRADE_REMIND, false, this, 0);
            HwActivityTaskManagerServiceEx hwActivityTaskManagerServiceEx = HwActivityTaskManagerServiceEx.this;
            if (Settings.Secure.getIntForUser(resolver, KEY_HW_UPGRADE_REMIND, 0, 0) != 0) {
                z = true;
            }
            hwActivityTaskManagerServiceEx.mNeedRemindHwOUC = z;
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri) {
            if (this.URI_HW_UPGRADE_REMIND.equals(uri)) {
                HwActivityTaskManagerServiceEx hwActivityTaskManagerServiceEx = HwActivityTaskManagerServiceEx.this;
                boolean z = false;
                if (Settings.Secure.getIntForUser(hwActivityTaskManagerServiceEx.mContext.getContentResolver(), KEY_HW_UPGRADE_REMIND, 0, 0) != 0) {
                    z = true;
                }
                hwActivityTaskManagerServiceEx.mNeedRemindHwOUC = z;
                Slog.i(HwActivityTaskManagerServiceEx.TAG, "mNeedRemindHwOUC has changed to : " + HwActivityTaskManagerServiceEx.this.mNeedRemindHwOUC);
            }
        }
    }

    private void dismissSplitScreenToPrimaryStack(RootActivityContainer mRootActivityContainer) {
        long ident = Binder.clearCallingIdentity();
        try {
            ActivityStack stack = mRootActivityContainer.getDefaultDisplay().getSplitScreenPrimaryStack();
            if (stack == null) {
                Slog.w(TAG, "dismissSplitScreenToPrimaryStack: primary split-screen stack not found.");
                return;
            }
            this.mIAtmsInner.getATMS().mWindowManager.mShouldResetTime = true;
            this.mIAtmsInner.getATMS().mWindowManager.startFreezingScreen(0, 0);
            stack.moveToFront("dismissSplitScreenToPrimaryStack");
            stack.setWindowingMode(1);
            this.mIAtmsInner.getATMS().mWindowManager.stopFreezingScreen();
            Binder.restoreCallingIdentity(ident);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void notifyActivityState(ActivityRecord r, String state) {
        Message msg = this.mHwHandler.obtainMessage(NOTIFY_ACTIVITY_STATE);
        String activityInfo = parseActivityStateInfo(r, state);
        if (activityInfo == null) {
            Slog.e(TAG, "parse activity info error.");
            return;
        }
        msg.obj = activityInfo;
        this.mHwHandler.sendMessage(msg);
        if (this.mNeedRemindHwOUC && r.mUserId == 0 && r.isActivityTypeHome() && state.equals(ActivityStack.ActivityState.RESUMED.toString())) {
            this.mHwHandler.removeMessages(80);
            this.mHwHandler.sendEmptyMessage(80);
        }
    }

    private String parseActivityStateInfo(ActivityRecord r, String state) {
        if (r == null || state == null) {
            Slog.e(TAG, "invalid input param, error.");
            return null;
        } else if (r.packageName == null || r.shortComponentName == null || r.app == null || r.appInfo == null || r.appInfo.uid <= 1000) {
            Slog.e(TAG, "invalid ActivityRecord, error.");
            return null;
        } else {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(r.packageName);
            stringBuffer.append("#");
            stringBuffer.append(r.shortComponentName);
            stringBuffer.append("#");
            stringBuffer.append(r.appInfo.uid);
            stringBuffer.append("#");
            stringBuffer.append(r.app.mPid);
            stringBuffer.append("#");
            stringBuffer.append(state);
            return stringBuffer.toString();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleAppNeedsSwingRotation(Message msg) {
        if (msg != null) {
            Bundle bundle = (Bundle) msg.obj;
            String packageName = "";
            if (bundle != null) {
                packageName = bundle.getString(HwCertification.KEY_PACKAGE_NAME, "");
            }
            if (packageName != null && !packageName.isEmpty()) {
                int type = AppTypeRecoManager.getInstance().getAppType(packageName);
                Slog.v(TAG, "handleAppNeedsSwingRotation packageName: " + packageName + " type: " + type);
                DisplayRotation displayRotation = this.mIAtmsInner.getATMS().mWindowManager.getDefaultDisplayContentLocked().getDisplayRotation();
                if (type == 3 || type == 9) {
                    displayRotation.setSwingDisabled(true);
                } else {
                    displayRotation.setSwingDisabled(false);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void handleNotifyActivityState(Message msg) {
        if (msg != null) {
            String activityInfo = null;
            if (msg.obj instanceof String) {
                activityInfo = (String) msg.obj;
            }
            if (activityInfo == null) {
                Slog.e(TAG, "msg.obj type error.");
                return;
            }
            IHwActivityTaskManagerInner iHwActivityTaskManagerInner = this.mIAtmsInner;
            if (iHwActivityTaskManagerInner != null && iHwActivityTaskManagerInner.getAtmDAMonitor() != null) {
                this.mIAtmsInner.getAtmDAMonitor().notifyActivityState(activityInfo);
            }
        }
    }

    private Intent slideGetDefaultIntent() {
        Intent intent = new Intent();
        intent.setPackage(WifiProCommonUtils.WIFI_SETTINGS_PHONE);
        intent.setAction("android.intent.action.MAIN");
        intent.setClassName(WifiProCommonUtils.WIFI_SETTINGS_PHONE, "com.android.settings.accessibility.FirstSlideCoverDialogActivity");
        return intent;
    }

    private boolean isSupportKeyguardQuickCamera(ContentResolver resolver, int uid) {
        return Settings.Secure.getIntForUser(resolver, "keyguard_slide_open_camera_state", -1, uid) == 1;
    }

    private Intent slideGetIntentFromSetting(boolean isSecure) {
        String keyStr = isSecure ? "quick_slide_app_db_secure" : "quick_slide_app_db";
        ContentResolver resolver = this.mContext.getContentResolver();
        int uid = this.mIAtmsInner.getATMS().getCurrentUserId();
        if (!isSecure || isSupportKeyguardQuickCamera(resolver, uid)) {
            String intentStr = Settings.Secure.getStringForUser(resolver, keyStr, uid);
            if (intentStr == null) {
                return null;
            }
            if ("first_slide".equals(intentStr)) {
                return slideGetDefaultIntent();
            }
            try {
                return Intent.parseUri(intentStr, 0);
            } catch (Exception e) {
                Slog.e(TAG, "startActivity get intent err : " + intentStr);
                return null;
            }
        } else {
            Slog.v(TAG, "slideGetIntentFromSetting skipped as not support");
            return null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void slideOpenStartActivity() {
        boolean isSecure = this.mIAtmsInner.getATMS().isSleepingLocked();
        if (!isSecure || !this.mIAtmsInner.getATMS().mWindowManager.mPolicy.isKeyguardOccluded()) {
            Intent intent = slideGetIntentFromSetting(isSecure);
            if (intent == null) {
                Slog.i(TAG, "slideOpenStartActivity get intent is null, return!");
                return;
            }
            this.mQuickSlideIntent = intent;
            this.mQuickSlideStartTime = SystemClock.uptimeMillis();
            ActivityRecord lastResumedActivity = this.mIAtmsInner.getLastResumedActivityRecord();
            String lastResumedPkg = lastResumedActivity != null ? lastResumedActivity.packageName : null;
            Flog.bdReport(991310652, "{curPkgName:" + lastResumedPkg + ",startPkgName:" + intent.getPackage() + "}");
            StringBuilder sb = new StringBuilder();
            sb.append("slideOpenStartActivity lastResumedPkg:");
            sb.append(lastResumedPkg);
            sb.append(", startPkgName:");
            sb.append(intent.getPackage());
            Slog.i(TAG, sb.toString());
            if (intent.getPackage() == null || "no_set".equals(intent.getPackage()) || (lastResumedActivity != null && lastResumedActivity.visible && !isSecure && !WifiProCommonUtils.WIFI_SETTINGS_PHONE.equals(intent.getPackage()) && lastResumedActivity.packageName.equals(intent.getPackage()))) {
                Slog.i(TAG, "no_set or has been started, need not start activity! sleep " + isSecure);
                return;
            }
            this.mContext.startActivityAsUser(intent, UserHandle.CURRENT);
            return;
        }
        Slog.i(TAG, "slideOpenStartActivity skip as occluded, return!");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void slideCloseMoveActivityToBack() {
        Intent intent = this.mQuickSlideIntent;
        String pkgName = intent != null ? intent.getPackage() : null;
        long curTime = SystemClock.uptimeMillis();
        long j = this.mQuickSlideStartTime;
        long durTime = (j == 0 || curTime < j) ? 0 : curTime - j;
        Flog.bdReport(991310653, "{durTime:" + durTime + ",pkgName:" + pkgName + "}");
        StringBuilder sb = new StringBuilder();
        sb.append("slideCloseMoveActivityToBack durTime:");
        sb.append(durTime);
        sb.append(", pkgName:");
        sb.append(pkgName);
        Slog.i(TAG, sb.toString());
        this.mQuickSlideIntent = null;
        this.mQuickSlideStartTime = 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void registerHallCallback() {
        if (!new CoverManager().registerHallCallback("android", 1, new IHallCallback.Stub() {
            /* class com.android.server.wm.HwActivityTaskManagerServiceEx.AnonymousClass11 */

            public void onStateChange(HallState hallState) {
                long ident = Binder.clearCallingIdentity();
                try {
                    if (hallState.state == 2) {
                        HwActivityTaskManagerServiceEx.this.slideOpenStartActivity();
                    } else if (hallState.state == 0) {
                        HwActivityTaskManagerServiceEx.this.slideCloseMoveActivityToBack();
                    }
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        })) {
            Slog.i(TAG, "registerHallCallback err!");
        }
    }

    public void registerBroadcastReceiver() {
        if ((SystemProperties.getInt("ro.config.hw_hall_prop", 0) & 1) != 0) {
            BroadcastReceiver receiver = new BroadcastReceiver() {
                /* class com.android.server.wm.HwActivityTaskManagerServiceEx.AnonymousClass12 */

                @Override // android.content.BroadcastReceiver
                public void onReceive(Context context, Intent intent) {
                    if (intent != null && "com.huawei.systemserver.START".equals(intent.getAction())) {
                        Slog.i(HwActivityTaskManagerServiceEx.TAG, "registerBroadcastReceiver");
                        HwActivityTaskManagerServiceEx.this.registerHallCallback();
                    }
                }
            };
            IntentFilter filter = new IntentFilter();
            filter.addAction("com.huawei.systemserver.START");
            this.mContext.registerReceiverAsUser(receiver, UserHandle.ALL, filter, BOOT_PERMISSION, null);
        }
    }

    public void hwRestoreTask(int taskId, float xPos, float yPos) {
        DefaultHwPCMultiWindowManager multiWindowMgr;
        if (HwPCUtils.isPcCastModeInServer() && (multiWindowMgr = getHwPCMultiWindowManager(buildAtmsEx(this.mIAtmsInner.getATMS()))) != null) {
            long origId = Binder.clearCallingIdentity();
            try {
                synchronized (this.mIAtmsInner.getATMS().getGlobalLock()) {
                    TaskRecord tr = this.mIAtmsInner.getATMS().mRootActivityContainer.anyTaskForId(taskId);
                    if (tr != null) {
                        if (!HwPCMultiWindowCompatibility.isRestorable(tr.mWindowState)) {
                            Binder.restoreCallingIdentity(origId);
                            return;
                        }
                        if (tr.getStack() != null) {
                            tr.getStack().mHwActivityStackEx.resetOtherStacksVisible(true);
                        }
                        Rect rect = multiWindowMgr.getWindowBounds(buildTaskRecordEx(tr));
                        if (rect == null) {
                            Binder.restoreCallingIdentity(origId);
                            return;
                        }
                        if (!(xPos == -1.0f || yPos == -1.0f)) {
                            Rect bounds = tr.getRequestedOverrideBounds();
                            if (bounds == null) {
                                bounds = multiWindowMgr.getMaximizedBounds();
                            }
                            if (bounds.width() == 0 || bounds.height() == 0) {
                                Binder.restoreCallingIdentity(origId);
                                return;
                            }
                            rect.offsetTo((int) (xPos - (((float) rect.width()) * ((xPos - ((float) bounds.left)) / ((float) bounds.width())))), (int) (yPos - (((float) rect.height()) * ((yPos - ((float) bounds.top)) / ((float) bounds.height())))));
                        }
                        tr.resize(rect, 3, true, false);
                        Binder.restoreCallingIdentity(origId);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(origId);
            }
        }
    }

    public void hwResizeTask(int taskId, Rect bounds) {
        DefaultHwPCMultiWindowManager multiWindowMgr;
        String str;
        if (HwPCUtils.isPcCastModeInServer() && (multiWindowMgr = getHwPCMultiWindowManager(buildAtmsEx(this.mIAtmsInner.getATMS()))) != null) {
            long origId = Binder.clearCallingIdentity();
            try {
                synchronized (this.mIAtmsInner.getATMS().getGlobalLock()) {
                    boolean isFullscreen = false;
                    boolean isMaximized = false;
                    boolean isSplitWindow = false;
                    if (bounds.isEmpty() && bounds.top == bounds.bottom && bounds.left == bounds.right) {
                        int i = bounds.top;
                        if (i == -3) {
                            bounds.set(multiWindowMgr.getSplitRightWindowBounds());
                            isSplitWindow = true;
                        } else if (i == -2) {
                            bounds.set(multiWindowMgr.getSplitLeftWindowBounds());
                            isSplitWindow = true;
                        } else if (i == -1) {
                            bounds = null;
                            isFullscreen = true;
                        } else if (i == 0) {
                            bounds.set(multiWindowMgr.getMaximizedBounds());
                            isMaximized = true;
                        }
                    }
                    TaskRecord task = this.mIAtmsInner.getATMS().mRootActivityContainer.anyTaskForId(taskId);
                    if (task != null) {
                        if (!multiWindowMgr.isSupportResize(buildTaskRecordEx(task), isFullscreen, isMaximized)) {
                            HwPCUtils.log(DefaultHwPCMultiWindowManager.TAG, "hwResizeTask-fail: (" + Integer.toHexString(task.mWindowState) + ")isFullscreen:" + isFullscreen + "; isMax:" + isMaximized + "; isSplitWindow:" + isSplitWindow);
                            Binder.restoreCallingIdentity(origId);
                            return;
                        }
                        StringBuilder sb = new StringBuilder();
                        sb.append("hwResizeTask: ");
                        if (bounds == null) {
                            str = "null";
                        } else {
                            str = bounds.toShortString() + " (" + bounds.width() + ", " + bounds.height() + ")";
                        }
                        sb.append(str);
                        HwPCUtils.log(DefaultHwPCMultiWindowManager.TAG, sb.toString());
                        task.resize(bounds, 3, true, false);
                        if (task.getStack() != null && (isFullscreen || isMaximized || isSplitWindow)) {
                            task.getStack().mHwActivityStackEx.resetOtherStacksVisible(false);
                        }
                        Binder.restoreCallingIdentity(origId);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(origId);
            }
        }
    }

    public int getWindowState(IBinder token) {
        ActivityRecord r;
        if (!HwPCUtils.isPcCastModeInServer()) {
            return -1;
        }
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mIAtmsInner.getATMS().getGlobalLock()) {
                r = ActivityRecord.isInStackLocked(token);
            }
            if (r == null || r.task == null) {
                Binder.restoreCallingIdentity(ident);
                return -1;
            }
            return r.task.getWindowState();
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public HwRecentTaskInfo getHwRecentTaskInfo(int taskId) {
        synchronized (this.mIAtmsInner.getATMS().getGlobalLock()) {
            long origId = Binder.clearCallingIdentity();
            try {
                TaskRecord tr = this.mIAtmsInner.getATMS().mRootActivityContainer.anyTaskForId(taskId);
                if (tr != null) {
                    return createHwRecentTaskInfoFromTaskRecord(tr);
                }
                Binder.restoreCallingIdentity(origId);
                return null;
            } finally {
                Binder.restoreCallingIdentity(origId);
            }
        }
    }

    private HwRecentTaskInfo createHwRecentTaskInfoFromTaskRecord(TaskRecord tr) {
        ActivityManager.RecentTaskInfo rti = this.mIAtmsInner.getATMS().getRecentTasks().createRecentTaskInfo(tr);
        HwRecentTaskInfo hwRti = new HwRecentTaskInfo();
        hwRti.translateRecentTaskinfo(rti);
        ActivityStack stack = tr.getStack();
        if (stack != null) {
            hwRti.displayId = stack.mDisplayId;
        }
        hwRti.windowState = tr.getWindowState();
        if (!tr.mActivities.isEmpty() && (this.mIAtmsInner.getATMS().mWindowManager instanceof HwWindowManagerService)) {
            hwRti.systemUiVisibility = this.mIAtmsInner.getATMS().mWindowManager.getWindowSystemUiVisibility(((ActivityRecord) tr.mActivities.get(0)).appToken);
        }
        return hwRti;
    }

    public boolean skipOverridePendingTransitionForPC(ActivityRecord self) {
        return HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(self.getDisplayId());
    }

    public boolean skipOverridePendingTransitionForMagicWindow(ActivityRecord self) {
        long ident = Binder.clearCallingIdentity();
        boolean result = false;
        if (self != null && self.inHwMagicWindowingMode()) {
            result = HwMwUtils.performPolicy(208, new Object[]{self.mAppWindowToken}).getBoolean("RESULT_NEED_SYSTEM_ANIMATION", true);
        }
        Binder.restoreCallingIdentity(ident);
        return result;
    }

    public boolean isTaskNotResizeableEx(TaskRecord task, Rect bounds) {
        return (isTaskSizeChange(task, bounds) && !HwPCMultiWindowCompatibility.isResizable(task.getWindowState())) || (!isTaskSizeChange(task, bounds) && !HwPCMultiWindowCompatibility.isLayoutHadBounds(task.getWindowState()));
    }

    private boolean isTaskSizeChange(TaskRecord task, Rect rect) {
        return (task.getRequestedOverrideBounds().width() == rect.width() && task.getRequestedOverrideBounds().height() == rect.height()) ? false : true;
    }

    public void togglePCMode(boolean pcMode, int displayId) {
        ActivityDisplay activityDisplay;
        synchronized (this.mIAtmsInner.getATMS().getGlobalLock()) {
            if (!pcMode) {
                if (!(this.mIAtmsInner.getStackSupervisor() == null || (activityDisplay = getPCActivityDisplay(displayId)) == null)) {
                    int size = activityDisplay.getChildCount();
                    ArrayList<ActivityStack> stacks = new ArrayList<>();
                    for (int i = 0; i < size; i++) {
                        stacks.add(activityDisplay.getChildAt(i));
                    }
                    this.mIAtmsInner.getStackSupervisor().mHwActivityStackSupervisorEx.onDisplayRemoved(stacks);
                }
            }
            this.mIAtmsInner.getATMS().mWindowManager.mHwWMSEx.togglePCMode(pcMode, displayId);
        }
    }

    public void toggleHome() {
        if (HwPCUtils.isPcCastModeInServer()) {
            synchronized (this.mIAtmsInner.getATMS().getGlobalLock()) {
                long origId = Binder.clearCallingIdentity();
                try {
                    int displayId = HwPCUtils.getPCDisplayID();
                    if (HwPCUtils.isValidExtDisplayId(displayId)) {
                        ActivityDisplay activityDisplay = getPCActivityDisplay(displayId);
                        if (activityDisplay == null) {
                            Binder.restoreCallingIdentity(origId);
                            return;
                        }
                        ArrayList<ActivityStack> stacks = new ArrayList<>();
                        int size = activityDisplay.getChildCount();
                        for (int i = 0; i < size; i++) {
                            stacks.add(activityDisplay.getChildAt(i));
                        }
                        boolean moveAllToBack = true;
                        boolean forceAllToBack = HwPCUtils.isHiCarCastMode();
                        if (!forceAllToBack) {
                            int stackNdx = stacks.size() - 1;
                            while (true) {
                                if (stackNdx < 0) {
                                    break;
                                }
                                ActivityStack stack = stacks.get(stackNdx);
                                if (stack != null && stack.mHwActivityStackEx.getHiddenFromHome()) {
                                    moveAllToBack = false;
                                    break;
                                }
                                stackNdx--;
                            }
                        }
                        for (int stackNdx2 = 0; stackNdx2 < stacks.size(); stackNdx2++) {
                            ActivityStack stack2 = stacks.get(stackNdx2);
                            TaskRecord task = stack2.topTask();
                            if (task != null) {
                                if (moveAllToBack) {
                                    if (stack2.shouldBeVisible((ActivityRecord) null)) {
                                        if (forceAllToBack || !stack2.mHwActivityStackEx.getHiddenFromHome()) {
                                            stack2.moveTaskToBackLocked(task.taskId);
                                            stack2.mHwActivityStackEx.setHiddenFromHome(true);
                                        }
                                    }
                                } else if (!stack2.shouldBeVisible((ActivityRecord) null) && stack2.mHwActivityStackEx.getHiddenFromHome()) {
                                    stack2.moveTaskToFrontLocked(task, true, (ActivityOptions) null, (AppTimeTracker) null, "moveToFrontFromHomeKey.");
                                    stack2.mHwActivityStackEx.setHiddenFromHome(false);
                                }
                            }
                        }
                        Binder.restoreCallingIdentity(origId);
                    }
                } finally {
                    Binder.restoreCallingIdentity(origId);
                }
            }
        }
    }

    public boolean isMaximizedPortraitAppOnPCMode(ActivityRecord r) {
        if (!HwPCUtils.isPcCastModeInServer() || r.getActivityStack() == null || r.info == null || r.info.getComponentName() == null || !HwPCUtils.isValidExtDisplayId(r.getActivityStack().mDisplayId)) {
            return false;
        }
        return getHwPCMultiWindowManager(buildAtmsEx(this.mIAtmsInner.getATMS())).getPortraitMaximizedPkgList().contains(r.info.getComponentName().getPackageName());
    }

    public TaskChangeNotificationController getHwTaskChangeController() {
        return this.mHwTaskChangeNotificationController;
    }

    public void registerHwTaskStackListener(ITaskStackListener listener) {
        TaskChangeNotificationController taskctl = getHwTaskChangeController();
        if (taskctl != null) {
            taskctl.registerTaskStackListener(listener);
        }
    }

    public void unRegisterHwTaskStackListener(ITaskStackListener listener) {
        if (getHwTaskChangeController() != null) {
            getHwTaskChangeController().unregisterTaskStackListener(listener);
        }
    }

    public void updateUsageStatsForPCMode(ActivityRecord component, boolean visible, UsageStatsManagerInternal usageStatsService) {
        if (component != null && HwPCUtils.isPcDynamicStack(component.getStackId()) && usageStatsService != null) {
            if (!visible) {
                usageStatsService.reportEvent(component.mActivityComponent, component.mUserId, 2, HwPCUtils.getPCDisplayID(), (ComponentName) null);
                this.mPCUsageStats.remove(component.mActivityComponent.toShortString());
            } else if (!this.mPCUsageStats.containsKey(component.mActivityComponent.toShortString())) {
                usageStatsService.reportEvent(component.mActivityComponent, component.mUserId, 1, HwPCUtils.getPCDisplayID(), (ComponentName) null);
                this.mPCUsageStats.put(component.mActivityComponent.toShortString(), Long.valueOf(System.currentTimeMillis()));
            }
        }
    }

    public void moveTaskBackwards(int taskId) {
        if (HwPCUtils.isPcCastModeInServer()) {
            synchronized (this.mIAtmsInner.getATMS().getGlobalLock()) {
                long origId = Binder.clearCallingIdentity();
                TaskRecord tr = this.mIAtmsInner.getATMS().mRootActivityContainer.anyTaskForId(taskId);
                if (tr != null && HwPCUtils.isExtDynamicStack(tr.getStackId())) {
                    tr.getStack().moveTaskToBackLocked(taskId);
                }
                Binder.restoreCallingIdentity(origId);
            }
        }
    }

    public boolean checkTaskId(int taskId) {
        if (this.mIAtmsInner.getATMS().mRootActivityContainer == null || this.mIAtmsInner.getATMS().mRootActivityContainer.anyTaskForId(taskId) == null) {
            return false;
        }
        return true;
    }

    public Bitmap getTaskThumbnailOnPCMode(int taskId) {
        ActivityRecord r;
        Bitmap bitmap = null;
        synchronized (this) {
            TaskRecord tr = this.mIAtmsInner.getATMS().mRootActivityContainer.anyTaskForId(taskId, 1);
            if (!(tr == null || tr.mStack == null || !(this.mIAtmsInner.getATMS().mWindowManager instanceof HwWindowManagerService) || (r = tr.topRunningActivityLocked()) == null)) {
                bitmap = this.mIAtmsInner.getATMS().mWindowManager.getTaskSnapshotForPc(r.getDisplayId(), r.appToken, tr.taskId, tr.userId);
            }
        }
        return bitmap;
    }

    private ActivityRecord getTopActivityAppToken(ComponentName componentName, ActivityRecord activity) {
        ActivityRecord lastActivityRecord = this.mPreviousResumedActivity;
        if (activity == null) {
            Slog.w(TAG, "requestContent failed: no activity");
            return null;
        } else if (activity.app == null || activity.app.mThread == null) {
            Slog.w(TAG, "requestContent failed: no process for " + activity);
            return null;
        } else if (componentName == null) {
            return null;
        } else {
            if (componentName.equals(activity.info.getComponentName())) {
                Slog.w(TAG, "componentName = " + componentName + " realActivity = " + activity.info.getComponentName() + " isEqual = " + componentName.equals(activity.info.getComponentName()));
                if (lastActivityRecord != null) {
                    Slog.w(TAG, "lastActivityRecord = " + lastActivityRecord.info.getComponentName());
                }
                return activity;
            } else if (lastActivityRecord == null || !componentName.equals(lastActivityRecord.info.getComponentName())) {
                return null;
            } else {
                Slog.w(TAG, "lastActivityRecord = " + lastActivityRecord.info.getComponentName());
                return lastActivityRecord;
            }
        }
    }

    public boolean isGameDndOn() {
        HwGameAssistantController hwGameAssistantController = this.mHwGameAssistantController;
        if (hwGameAssistantController != null) {
            return hwGameAssistantController.isGameDndOn();
        }
        return false;
    }

    public boolean isGameDndOnEx() {
        HwGameAssistantController hwGameAssistantController = this.mHwGameAssistantController;
        return hwGameAssistantController != null && hwGameAssistantController.isGameDndOnEx();
    }

    public boolean addGameSpacePackageList(List<String> packageList) {
        HwGameAssistantController hwGameAssistantController = this.mHwGameAssistantController;
        return hwGameAssistantController != null && hwGameAssistantController.addGameSpacePackageList(packageList);
    }

    public boolean delGameSpacePackageList(List<String> packageList) {
        HwGameAssistantController hwGameAssistantController = this.mHwGameAssistantController;
        return hwGameAssistantController != null && hwGameAssistantController.delGameSpacePackageList(packageList);
    }

    public boolean isInGameSpace(String packageName) {
        HwGameAssistantController hwGameAssistantController = this.mHwGameAssistantController;
        return hwGameAssistantController != null && hwGameAssistantController.isInGameSpace(packageName);
    }

    public List<String> getGameList() {
        HwGameAssistantController hwGameAssistantController = this.mHwGameAssistantController;
        if (hwGameAssistantController != null) {
            return hwGameAssistantController.getGameList();
        }
        return null;
    }

    public void registerGameObserver(IGameObserver observer) {
        HwGameAssistantController hwGameAssistantController = this.mHwGameAssistantController;
        if (hwGameAssistantController != null) {
            hwGameAssistantController.registerGameObserver(observer);
        }
    }

    public void unregisterGameObserver(IGameObserver observer) {
        HwGameAssistantController hwGameAssistantController = this.mHwGameAssistantController;
        if (hwGameAssistantController != null) {
            hwGameAssistantController.unregisterGameObserver(observer);
        }
    }

    public void registerGameObserverEx(IGameObserverEx observer) {
        HwGameAssistantController hwGameAssistantController = this.mHwGameAssistantController;
        if (hwGameAssistantController != null) {
            hwGameAssistantController.registerGameObserverEx(observer);
        }
    }

    public void unregisterGameObserverEx(IGameObserverEx observer) {
        HwGameAssistantController hwGameAssistantController = this.mHwGameAssistantController;
        if (hwGameAssistantController != null) {
            hwGameAssistantController.unregisterGameObserverEx(observer);
        }
    }

    public boolean isGameKeyControlOn() {
        HwGameAssistantController hwGameAssistantController = this.mHwGameAssistantController;
        return hwGameAssistantController != null && hwGameAssistantController.isGameKeyControlOn();
    }

    public boolean isGameGestureDisabled() {
        HwGameAssistantController hwGameAssistantController = this.mHwGameAssistantController;
        return hwGameAssistantController != null && hwGameAssistantController.isGameGestureDisabled();
    }

    public void reportPreviousInfo(int relationType, WindowProcessController prevProc) {
        if (prevProc != null && this.mIAtmsInner.getAtmDAMonitor().isResourceNeeded(RESOURCE_APPASSOC)) {
            Bundle bundleArgs = new Bundle();
            bundleArgs.putInt(ASSOC_PID, prevProc.mPid);
            bundleArgs.putInt(ASSOC_TGT_UID, prevProc.mUid);
            bundleArgs.putInt(ASSOC_RELATION_TYPE, relationType);
            this.mIAtmsInner.getAtmDAMonitor().reportData(RESOURCE_APPASSOC, System.currentTimeMillis(), bundleArgs);
        }
    }

    public void reportHomeProcess(WindowProcessController homeProc) {
        if (this.mIAtmsInner.getAtmDAMonitor().isResourceNeeded(RESOURCE_APPASSOC)) {
            int pid = 0;
            int uid = 0;
            ArrayList<String> pkgs = new ArrayList<>();
            if (homeProc != null) {
                try {
                    pid = homeProc.mPid;
                    uid = homeProc.mUid;
                    Iterator it = homeProc.mPkgList.iterator();
                    while (it.hasNext()) {
                        String pkg = (String) it.next();
                        if (!pkgs.contains(pkg)) {
                            pkgs.add(pkg);
                        }
                    }
                } catch (ConcurrentModificationException e) {
                    Slog.i(TAG, "reportHomeProcess error happened.");
                }
            }
            Bundle bundleArgs = new Bundle();
            bundleArgs.putInt(ASSOC_PID, pid);
            bundleArgs.putInt(ASSOC_TGT_UID, uid);
            bundleArgs.putStringArrayList(ASSOC_PKGNAME, pkgs);
            bundleArgs.putInt(ASSOC_RELATION_TYPE, 11);
            this.mIAtmsInner.getAtmDAMonitor().reportData(RESOURCE_APPASSOC, System.currentTimeMillis(), bundleArgs);
        }
    }

    public float getAspectRatioWithUserSet(String packageName, String aspectName, ActivityInfo info) {
        if (!"maxAspectRatio".equals(aspectName)) {
            return 0.0f;
        }
        float maxAspectRatio = info.maxAspectRatio;
        sDeviceMaxRatio = this.mIAtmsInner.getATMS().mWindowManager.getDeviceMaxRatio();
        float userMaxAspectRatio = 0.0f;
        if (sDeviceMaxRatio > 0.0f && this.mIAtmsInner.getATMS() != null && !TextUtils.isEmpty(packageName)) {
            userMaxAspectRatio = this.mIAtmsInner.getATMS().getPackageManagerInternalLocked().getUserAspectRatio(packageName, aspectName);
        }
        return (userMaxAspectRatio == 0.0f || userMaxAspectRatio < sDeviceMaxRatio) ? maxAspectRatio : userMaxAspectRatio;
    }

    public boolean isFreeFormVisible() {
        KeyguardManager km;
        Object keyguardService = this.mContext.getSystemService("keyguard");
        boolean stackVisible = false;
        if ((keyguardService instanceof KeyguardManager) && (km = (KeyguardManager) keyguardService) != null && km.isKeyguardLocked()) {
            return false;
        }
        synchronized (this.mIAtmsInner.getATMS().getGlobalLock()) {
            ActivityStack stack = this.mIAtmsInner.getRootActivityContainer().getStack(5, 1);
            ActivityStack hwFreeFormStack = this.mIAtmsInner.getRootActivityContainer().getStack(102, 1);
            if (stack == null || hwFreeFormStack == null) {
                if (stack != null) {
                    stackVisible = stack.isTopActivityVisible();
                } else if (hwFreeFormStack == null) {
                    return false;
                } else {
                    stackVisible = hwFreeFormStack.isTopActivityVisible();
                }
            } else if (stack.isTopActivityVisible() || hwFreeFormStack.isTopActivityVisible()) {
                stackVisible = true;
            }
            return stackVisible;
        }
    }

    public Intent changeStartActivityIfNeed(Intent intent) {
        if (IS_BOPD) {
            if (intent == null || Settings.Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) == 0) {
                Slog.i(TAG, "failed to set activity as EmergencyBackupActivity for bopd due to oobe not finished.");
                return intent;
            }
            intent.removeCategory("android.intent.category.HOME");
            intent.addFlags(HwGlobalActionsData.FLAG_KEYCOMBINATION_INIT_STATE);
            intent.setComponent(new ComponentName(BACKUP_PKG_NAME, BACKUP_ACTIVITY_NAME));
            Slog.i(TAG, "set activity as EmergencyBackupActivity in the mode of bopd successfully.");
        }
        return intent;
    }

    public void exitSingleHandMode() {
        this.mHwHandler.removeCallbacks(this.mOverscanTimeout);
        this.mHwHandler.postDelayed(this.mOverscanTimeout, 200);
    }

    private class OverscanTimeout implements Runnable {
        private OverscanTimeout() {
        }

        @Override // java.lang.Runnable
        public void run() {
            Slog.i(HwActivityTaskManagerServiceEx.TAG, "OverscanTimeout run");
            Settings.Global.putString(HwActivityTaskManagerServiceEx.this.mContext.getContentResolver(), "single_hand_mode", "");
        }
    }

    public boolean isSpecialVideoForPCMode(ActivityRecord r) {
        DefaultHwPCMultiWindowManager multiWindowMgr;
        if (!HwPCUtils.isPcCastModeInServer() || (multiWindowMgr = getHwPCMultiWindowManager(buildAtmsEx(this.mIAtmsInner.getATMS()))) == null || r == null || r.task == null) {
            return false;
        }
        int stackId = r.task.getStackId();
        String packageName = r.packageName;
        if (packageName == null || !HwPCUtils.isPcDynamicStack(stackId)) {
            return false;
        }
        if ((!HwPCUtils.enabledInPad() || !multiWindowMgr.isOlnyFullscreen(packageName)) && !multiWindowMgr.isPortraitApp(buildTaskRecordEx(r.getTaskRecord()))) {
            return false;
        }
        return true;
    }

    public boolean isVideosNeedFullScreenInConfig(String pkName) {
        if (HwActivityTaskManager.isPCMultiCastMode()) {
            return this.mHwMdm.isVideosNeedFullScreenInConfig(pkName);
        }
        return false;
    }

    public void updateFreeFormOutLine(int colorState) {
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mIAtmsInner.getATMS().getGlobalLock()) {
                ActivityStackSupervisor stackSupervisor = this.mIAtmsInner.getStackSupervisor();
                if (stackSupervisor.mRootActivityContainer != null) {
                    boolean isNull = true;
                    ActivityStack freeformStack = stackSupervisor.mRootActivityContainer.getStack(5, 1);
                    if (freeformStack == null) {
                        freeformStack = stackSupervisor.mRootActivityContainer.getStack(102, 1);
                    }
                    if (freeformStack != null && freeformStack.isFocusable()) {
                        ActivityRecord activity = freeformStack.topRunningActivityLocked();
                        if (activity == null) {
                            Slog.w(TAG, "updateFreeFormOutLine failed: no top activity");
                            Binder.restoreCallingIdentity(ident);
                            return;
                        }
                        if (!(activity.task == null || activity.task.getStack() == null || activity.app == null || activity.app.getThread() == null)) {
                            isNull = false;
                        }
                        if (isNull) {
                            Binder.restoreCallingIdentity(ident);
                            return;
                        } else {
                            try {
                                activity.app.getThread().scheduleFreeFormOutLineChanged(activity.appToken, colorState);
                            } catch (RemoteException e) {
                                Slog.e(TAG, "scheduleFreeFormOutLineChanged error!");
                            }
                        }
                    }
                    Binder.restoreCallingIdentity(ident);
                }
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public boolean isFullScreen(IBinder token) {
        ActivityRecord oriActivity = ActivityRecord.isInStackLocked(token);
        if (oriActivity == null) {
            return false;
        }
        return oriActivity.fullscreen;
    }

    public void updateFreeFormOutLineForFloating(IBinder token, int state) {
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mIAtmsInner.getATMS().getGlobalLock()) {
                ActivityRecord oriActivity = ActivityRecord.isInStackLocked(token);
                if (oriActivity != null) {
                    if (!oriActivity.fullscreen) {
                        TaskRecord tr = oriActivity.getTaskRecord();
                        if (tr != null && tr.getChildCount() > 0) {
                            if (tr.inHwFreeFormWindowingMode()) {
                                ActivityRecord r = null;
                                int activityNdx = tr.getChildCount() - 1;
                                while (true) {
                                    if (activityNdx >= 0) {
                                        ActivityRecord rTmp = tr.getChildAt(activityNdx);
                                        if (rTmp != null && !rTmp.finishing && rTmp.fullscreen && rTmp.visible) {
                                            r = rTmp;
                                            break;
                                        }
                                        activityNdx--;
                                    } else {
                                        break;
                                    }
                                }
                                if (r != null) {
                                    try {
                                        if (!(r.app == null || r.app.getThread() == null)) {
                                            r.app.getThread().scheduleFreeFormOutLineChangedForFloating(r.appToken, state);
                                        }
                                    } catch (RemoteException e) {
                                        Slog.e(TAG, "updateFreeFormOutLineForFloating error!");
                                    }
                                }
                                Binder.restoreCallingIdentity(ident);
                                return;
                            }
                        }
                        Binder.restoreCallingIdentity(ident);
                    }
                }
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public int getCaptionState(IBinder token) {
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mIAtmsInner.getATMS().getGlobalLock()) {
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                int i = 0;
                if (r != null) {
                    if (HwFreeFormUtils.sHideCaptionActivity.contains(r.info.getComponentName().flattenToString())) {
                        i = 8;
                    }
                    return i;
                }
                Binder.restoreCallingIdentity(ident);
                return 0;
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public int getActivityWindowMode(IBinder token) {
        synchronized (this.mIAtmsInner.getATMS().getGlobalLock()) {
            ActivityStack stack = ActivityRecord.getStackLocked(token);
            if (stack == null) {
                return 0;
            }
            return stack.getWindowingMode();
        }
    }

    public boolean canCleanTaskRecord(String packageName, int maxFoundNum, String recentTaskPkg) {
        if (packageName == null || recentTaskPkg == null) {
            return true;
        }
        synchronized (this.mIAtmsInner.getATMS().getGlobalLock()) {
            ArrayList<TaskRecord> recentTasks = this.mIAtmsInner.getRecentRawTasks();
            if (recentTasks == null) {
                return true;
            }
            int size = recentTasks.size();
            int foundNum = 0;
            for (int i = 0; i < size && foundNum < maxFoundNum; i++) {
                TaskRecord tr = recentTasks.get(i);
                if (tr != null) {
                    if (tr.mActivities != null) {
                        if (!(tr.mActivities.size() <= 0 || tr.getBaseIntent() == null || tr.getBaseIntent().getComponent() == null)) {
                            if (packageName.equals(tr.getBaseIntent().getComponent().getPackageName())) {
                                return false;
                            }
                            if (!recentTaskPkg.equals(tr.getBaseIntent().getComponent().flattenToShortString())) {
                                if ((tr.getBaseIntent().getFlags() & 8388608) != 0) {
                                }
                            }
                        }
                        foundNum++;
                    }
                }
            }
            Object instance = this.mIAtmsInner.getStackSupervisor();
            if (instance == null || !(instance instanceof ActivityStackSupervisorBridge) || !((ActivityStackSupervisorBridge) instance).isInVisibleStack(packageName)) {
                return true;
            }
            return false;
        }
    }

    public int getPreferedDisplayId(ActivityRecord startingActivity, ActivityOptions options, int preferredDisplayId) {
        if (this.mIAtmsInner.getATMS().mVrMananger.isVRDeviceConnected()) {
            return this.mIAtmsInner.getATMS().mVrMananger.getVrPreferredDisplayId(startingActivity.launchedFromPackage, startingActivity.packageName, preferredDisplayId);
        }
        if (HwPCUtils.isPcCastModeInServer()) {
            HwPCUtils.log(TAG, "getPreferedDisplayId:" + startingActivity + ", preferred:" + preferredDisplayId);
            if (startingActivity != null && ((startingActivity.isActivityTypeHome() && "android".equals(startingActivity.launchedFromPackage)) || TextUtils.equals(startingActivity.packageName, "com.huawei.desktop.systemui") || (HwPCUtils.isHiCarCastMode() && WifiProCommonUtils.WIFI_SETTINGS_PHONE.equals(startingActivity.packageName)))) {
                return 0;
            }
            if (!(!HwPCUtils.isHiCarCastMode() || startingActivity == null || startingActivity.intent == null || startingActivity.intent.getComponent() == null)) {
                try {
                    List<String> records = HwPCUtils.getHwPCManager().getCarAppList();
                    int tempDisplay = HwPCUtils.getPCDisplayID();
                    if (records != null && !records.isEmpty()) {
                        for (int i = 0; i < records.size(); i++) {
                            if (startingActivity.intent.getComponent().flattenToString().equals(records.get(i))) {
                                HwPCUtils.log(TAG, "start this activity in display :" + records.get(i) + ", " + tempDisplay);
                                return tempDisplay;
                            }
                        }
                    }
                } catch (RemoteException e) {
                    HwPCUtils.log(TAG, "remote exception while getCarAppList");
                }
            }
            if (HwPCUtils.enabledInPad()) {
                return HwPCUtils.getPCDisplayID();
            }
            if (HwPCUtils.isHiCarCastMode() && options != null && options.getLaunchDisplayId() == -1 && startingActivity != null && getPkgDisplayMaps().containsKey(startingActivity.launchedFromPackage)) {
                HwPCUtils.log(TAG, "shortcut start activity in hicar mode");
                return 0;
            } else if (options == null || !HwPCUtils.isValidExtDisplayId(options.getLaunchDisplayId())) {
                if (startingActivity != null) {
                    HashMap<String, Integer> maps = getPkgDisplayMaps();
                    int displayId = 0;
                    if (!TextUtils.isEmpty(startingActivity.launchedFromPackage)) {
                        if (maps.containsKey(startingActivity.launchedFromPackage)) {
                            displayId = maps.get(startingActivity.launchedFromPackage).intValue();
                            HwPCUtils.log(TAG, "launch from package displayMap, display:" + displayId);
                        }
                    } else if (!TextUtils.isEmpty(startingActivity.packageName) && maps.containsKey(startingActivity.packageName)) {
                        displayId = maps.get(startingActivity.packageName).intValue();
                        HwPCUtils.log(TAG, "start from package displayMap, display:" + displayId);
                    }
                    if (HwPCUtils.isValidExtDisplayId(displayId)) {
                        return displayId;
                    }
                }
                if (startingActivity != null && HwPCUtils.isHiCarCastMode() && "com.android.incallui".equals(startingActivity.packageName)) {
                    return HwPCUtils.getPCDisplayID();
                }
            } else {
                HwPCUtils.log(TAG, "getPreferedDisplayId option not null display:" + options.getLaunchDisplayId());
                return options.getLaunchDisplayId();
            }
        }
        return preferredDisplayId;
    }

    private final class FingerprintSettingsObserver extends ContentObserver {
        private final Uri fingerPrintKeyguardUri = Settings.Secure.getUriFor("fp_keyguard_enable");

        FingerprintSettingsObserver() {
            super(HwActivityTaskManagerServiceEx.this.mHwHandler);
            HwActivityTaskManagerServiceEx.this.mContext.getContentResolver().registerContentObserver(this.fingerPrintKeyguardUri, false, this, -1);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri, int userId) {
            HwActivityTaskManagerServiceEx.this.updateUnlockBoostStatus(userId);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateUnlockBoostStatus(int userId) {
        boolean isFaceEnabled = false;
        boolean isFpEnabled = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "fp_keyguard_enable", 0, userId) > 0;
        Slog.i(TAG, "update fingerprint unlock boost status [" + isFpEnabled + "]");
        if (Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "face_bind_with_lock", 0, userId) != 0) {
            isFaceEnabled = true;
        }
        Slog.i(TAG, "update face unlock boost status [" + isFaceEnabled + "]");
        this.mIsSetFingerprintOrFaceKeyGuard = isFpEnabled | isFaceEnabled;
    }

    private final class FaceSettingsObserver extends ContentObserver {
        private final Uri faceKeyguardURI = Settings.Secure.getUriFor("face_bind_with_lock");

        FaceSettingsObserver() {
            super(HwActivityTaskManagerServiceEx.this.mHwHandler);
            HwActivityTaskManagerServiceEx.this.mContext.getContentResolver().registerContentObserver(this.faceKeyguardURI, false, this, -1);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri, int userId) {
            HwActivityTaskManagerServiceEx.this.updateUnlockBoostStatus(userId);
        }
    }

    public boolean isActivityVisiableInFingerBoost(ActivityRecord r) {
        List<String> list;
        if (!IS_PROP_FINGER_BOOST || !this.mIsSetFingerprintOrFaceKeyGuard || this.mIAtmsInner.getATMS().mWindowManager.isInWallpaperEffect() || r == null || r.info == null || r.inHwFreeFormWindowingMode() || (list = this.mFingerprintUnlockBoostWhiteList) == null || !list.contains(r.info.packageName) || isLandscapeActivity(r)) {
            return false;
        }
        return true;
    }

    private boolean isLandscapeActivity(ActivityRecord r) {
        int iOrientation = r.getOrientation();
        if (iOrientation == 0 || iOrientation == 6 || iOrientation == 8 || iOrientation == 11) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:19:0x003c A[Catch:{ FileNotFoundException -> 0x0032, XmlPullParserException -> 0x002f, IOException -> 0x002c, all -> 0x0029 }] */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0064  */
    private void initFingerBoostWhiteListData() {
        StringBuilder sb;
        this.mFingerprintUnlockBoostWhiteList = new ArrayList();
        InputStream inputStream = null;
        try {
            File fingerBoostWhiteListXMLFile = HwCfgFilePolicy.getCfgFile("xml/hw_finger_unlock_boost_whitelist.xml", 0);
            if (fingerBoostWhiteListXMLFile != null) {
                try {
                    if (fingerBoostWhiteListXMLFile.exists()) {
                        inputStream = new FileInputStream(fingerBoostWhiteListXMLFile);
                        if (inputStream != null) {
                            Slog.e(TAG, "load finger boost whitelist fail,file not found: hw_finger_unlock_boost_whitelist");
                            if (inputStream != null) {
                                try {
                                    inputStream.close();
                                } catch (IOException e) {
                                    Slog.e(TAG, "load finger boost whitelist: IO Exception while closing stream");
                                }
                            }
                            Slog.d(TAG, "finger boost whitelist size:" + this.mFingerprintUnlockBoostWhiteList.size());
                            return;
                        }
                        XmlPullParser xmlParser = Xml.newPullParser();
                        xmlParser.setInput(inputStream, null);
                        for (int xmlEventType = xmlParser.next(); xmlEventType != 1; xmlEventType = xmlParser.next()) {
                            if (xmlEventType == 2 && "fingeer_unlock_boost".equals(xmlParser.getName())) {
                                addWhitListPackageName(xmlParser.getAttributeValue(null, AwarenessRequest.Field.PACKAGE_NAME));
                            }
                        }
                        try {
                            inputStream.close();
                        } catch (IOException e2) {
                            Slog.e(TAG, "load finger boost whitelist: IO Exception while closing stream");
                        }
                        sb = new StringBuilder();
                        sb.append("finger boost whitelist size:");
                        sb.append(this.mFingerprintUnlockBoostWhiteList.size());
                        Slog.d(TAG, sb.toString());
                        return;
                    }
                } catch (FileNotFoundException e3) {
                    Slog.e(TAG, "load finger boost whitelist fail,FileNotFound ");
                    if (0 != 0) {
                        try {
                            inputStream.close();
                        } catch (IOException e4) {
                            Slog.e(TAG, "load finger boost whitelist: IO Exception while closing stream");
                        }
                    }
                    sb = new StringBuilder();
                } catch (XmlPullParserException e5) {
                    Slog.e(TAG, "load finger boost whitelist fail: ParserException ");
                    if (0 != 0) {
                        try {
                            inputStream.close();
                        } catch (IOException e6) {
                            Slog.e(TAG, "load finger boost whitelist: IO Exception while closing stream");
                        }
                    }
                    sb = new StringBuilder();
                } catch (IOException e7) {
                    Slog.e(TAG, "load finger boost whitelist fail: ");
                    if (0 != 0) {
                        try {
                            inputStream.close();
                        } catch (IOException e8) {
                            Slog.e(TAG, "load finger boost whitelist: IO Exception while closing stream");
                        }
                    }
                    sb = new StringBuilder();
                } catch (Throwable th) {
                    if (0 != 0) {
                        try {
                            inputStream.close();
                        } catch (IOException e9) {
                            Slog.e(TAG, "load finger boost whitelist: IO Exception while closing stream");
                        }
                    }
                    Slog.d(TAG, "finger boost whitelist size:" + this.mFingerprintUnlockBoostWhiteList.size());
                    throw th;
                }
            }
            Slog.w(TAG, "hw_finger_unlock_boost_whitelist is not exist");
            if (inputStream != null) {
            }
        } catch (NoClassDefFoundError e10) {
            Slog.d(TAG, "HwCfgFilePolicy NoClassDefFoundError");
        }
    }

    private void addWhitListPackageName(String packageName) {
        if (packageName != null && !this.mFingerprintUnlockBoostWhiteList.contains(packageName)) {
            this.mFingerprintUnlockBoostWhiteList.add(packageName);
        }
    }

    public void showUninstallLauncherDialog(String pkgName) {
        this.mLastLauncherName = pkgName;
        Handler handler = this.mHwHandler;
        handler.sendMessage(handler.obtainMessage(48));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showUninstallLauncher() {
        Context uiContext = this.mIAtmsInner.getUiContext();
        try {
            PackageInfo pInfo = this.mContext.getPackageManager().getPackageInfo(this.mLastLauncherName, 0);
            if (pInfo != null) {
                AlertDialog d = new BaseErrorDialog(uiContext);
                d.getWindow().setType(HwArbitrationDEFS.MSG_MPLINK_UNBIND_FAIL);
                d.setCancelable(false);
                d.setTitle(uiContext.getString(33685930));
                d.setMessage(uiContext.getString(33685932, this.mContext.getPackageManager().getApplicationLabel(pInfo.applicationInfo).toString()));
                d.setButton(-1, uiContext.getString(33685931), new DialogInterface.OnClickListener() {
                    /* class com.android.server.wm.HwActivityTaskManagerServiceEx.AnonymousClass13 */

                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            HwActivityTaskManagerServiceEx.this.mContext.getPackageManager().deletePackage(HwActivityTaskManagerServiceEx.this.mLastLauncherName, null, 0);
                        } catch (Exception e) {
                            Slog.e(HwActivityTaskManagerServiceEx.TAG, "showUninstallLauncher error because of Exception!");
                        }
                    }
                });
                d.setButton(-2, uiContext.getString(17039360), new DialogInterface.OnClickListener() {
                    /* class com.android.server.wm.HwActivityTaskManagerServiceEx.AnonymousClass14 */

                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                d.show();
            }
        } catch (PackageManager.NameNotFoundException e) {
        }
    }

    public boolean isAllowToStartActivity(Context context, String callerPkg, ActivityInfo aInfo, boolean isKeyguard, ActivityInfo topActivity) {
        int activityMode;
        if (!IS_CHINA || callerPkg == null || aInfo == null) {
            return true;
        }
        int uid = 0;
        if (isKeyguard) {
            activityMode = 8;
        } else {
            boolean isBackStage = (topActivity == null || aInfo.packageName == null || aInfo.packageName.equals(topActivity.packageName)) ? false : true;
            if (!callerPkg.equals(aInfo.packageName) || !isBackStage) {
                return true;
            }
            activityMode = 4;
        }
        if (aInfo.applicationInfo != null) {
            uid = aInfo.applicationInfo.uid;
        }
        boolean hsmCheck = HwServiceSecurityPartsFactoryEx.getInstance().getHwAddViewHelper(context).addViewPermissionCheck(aInfo.packageName, activityMode, uid);
        if (hsmCheck || activityMode != 4 || !this.mIAtmsInner.getATMS().mPendingIntentController.isStartFromPendingIntent(aInfo.packageName, UserHandle.getUserId(uid))) {
            if (!hsmCheck) {
                Slog.i(TAG, "isAllowToStartActivity:" + hsmCheck + ", activityMode:" + activityMode + ", callerPkg:" + callerPkg + ", destInfo:" + aInfo + ", topActivity:" + topActivity);
            }
            return hsmCheck;
        }
        Slog.i(TAG, "starting activity through the pendingintent!");
        return true;
    }

    public void setRequestedOrientation(int requestedOrientation) {
        boolean isInLazyMode = false;
        if (this.mIAtmsInner.getATMS().mWindowManager.getLazyMode() != 0) {
            isInLazyMode = true;
        }
        if (isInLazyMode) {
            long origId = Binder.clearCallingIdentity();
            if (requestedOrientation == 0 || requestedOrientation == 6 || requestedOrientation == 8 || requestedOrientation == 11) {
                try {
                    Settings.Global.putString(this.mContext.getContentResolver(), "single_hand_mode", "");
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(origId);
                    throw th;
                }
            }
            Binder.restoreCallingIdentity(origId);
        }
    }

    private boolean isShowExtend(ActivityRecord record) {
        if (record == null || record.inMultiWindowMode()) {
            return false;
        }
        int displayMode = 0;
        if (HwFoldScreenState.isFoldScreenDevice()) {
            displayMode = ((HwFoldScreenManagerInternal) LocalServices.getService(HwFoldScreenManagerInternal.class)).getDisplayMode();
        }
        if (displayMode == 1) {
            if (record.minAspectRatio <= 0.0f || ((double) Math.abs(record.minAspectRatio - HwFoldScreenState.getScreenFoldFullRatio())) <= 1.0E-8d || sWhitelistActivities.contains(record.info.getComponentName().getClassName())) {
                return false;
            }
            return true;
        } else if (record.maxAspectRatio <= 0.0f || record.maxAspectRatio >= sDeviceMaxRatio || sWhitelistActivities.contains(record.info.getComponentName().getClassName())) {
            return false;
        } else {
            return true;
        }
    }

    public Bundle getTopActivity() {
        WindowState mainWindow;
        Bundle activityInfoBundle = new Bundle();
        int callingUid = UserHandle.getAppId(Binder.getCallingUid());
        int i = 0;
        if (callingUid != 1000) {
            int callingPid = Binder.getCallingPid();
            this.mIAtmsInner.getATMS();
            if (!(ActivityTaskManagerService.checkPermission("android.permission.REAL_GET_TASKS", callingPid, callingUid) == 0)) {
                Slog.d(TAG, "permission denied for, callingPid:" + callingPid + ", callingUid:" + callingUid + ", requires: android.Manifest.permission.REAL_GET_TASKS");
                return activityInfoBundle;
            }
        }
        ActivityRecord r = this.mIAtmsInner.getLastResumedActivityRecord();
        if (r == null) {
            return activityInfoBundle;
        }
        boolean isShowExtend = isShowExtend(r);
        if (r.getWindowingMode() == 105) {
            synchronized (this.mIAtmsInner.getATMS().getGlobalLock()) {
                TaskRecord task = r.task;
                if (!(task == null || task.getTask() == null)) {
                    activityInfoBundle.putInt("realOrientation", task.getTask().getOrientation());
                }
            }
        }
        activityInfoBundle.putBoolean("isShowExtend", isShowExtend);
        activityInfoBundle.putBoolean("visible", r.visible);
        activityInfoBundle.putParcelable("activityInfo", r.info);
        if (r.app != null) {
            activityInfoBundle.putInt(ASSOC_PID, r.app.mPid);
        }
        if (r.mAppWindowToken != null) {
            i = r.mAppWindowToken.getOrientation();
        }
        activityInfoBundle.putInt("orientation", i);
        activityInfoBundle.putInt(KEY_DISPLAY_ID, r.getDisplayId());
        if (r.getTaskRecord() != null) {
            activityInfoBundle.putInt(HwWmConstants.TASK_ID, r.getTaskRecord().taskId);
        }
        activityInfoBundle.putBoolean("inMultiWindowMode", r.inMultiWindowMode());
        if (!(r.mAppWindowToken == null || (mainWindow = r.mAppWindowToken.findMainWindow()) == null || mainWindow.getAttrs() == null)) {
            activityInfoBundle.putInt("flags", mainWindow.getAttrs().flags);
        }
        if (r.getWindowConfiguration() != null) {
            activityInfoBundle.putParcelable("bounds", r.getWindowConfiguration().getBounds());
            activityInfoBundle.putParcelable("appBounds", r.getWindowConfiguration().getAppBounds());
        }
        return activityInfoBundle;
    }

    public void addStackReferenceIfNeeded(ActivityStack stack) {
        this.mHwMwm.addStackReferenceIfNeeded(stack);
    }

    public void removeStackReferenceIfNeeded(ActivityStack stack) {
        this.mHwMwm.removeStackReferenceIfNeeded(stack);
    }

    public List<ActivityStack> findCombinedSplitScreenStacks(ActivityStack stack) {
        return this.mHwMwm.findCombinedSplitScreenStacks(stack);
    }

    public int[] getCombinedSplitScreenTaskIds(ActivityStack stack) {
        return this.mHwMwm.getCombinedSplitScreenTaskIds(stack);
    }

    public void calcHwMultiWindowStackBoundsForConfigChange(ActivityStack stack, Rect outBounds, Rect oldStackBounds, int oldDisplayWidth, int oldDisplayHeight, int newDisplayWidth, int newDisplayHeight, boolean isModeChanged) {
        if (stack.inHwPCMultiStackWindowingMode()) {
            this.mHwMdm.calcHwMultiWindowStackBoundsForConfigChange(stack, outBounds, oldStackBounds, oldDisplayWidth, oldDisplayHeight, newDisplayWidth, newDisplayHeight, isModeChanged);
        } else {
            this.mHwMwm.calcHwMultiWindowStackBoundsForConfigChange(stack, outBounds, oldStackBounds, oldDisplayWidth, oldDisplayHeight, newDisplayWidth, newDisplayHeight, isModeChanged);
        }
    }

    public void onDisplayConfigurationChanged(int displayId) {
        this.mHwMwm.onConfigurationChanged(displayId);
    }

    public void focusStackChange(int currentUser, int displayId, ActivityStack currentFocusedStack, ActivityStack lastFocusedStack) {
        this.mHwMwm.focusStackChange(currentUser, displayId, currentFocusedStack, lastFocusedStack);
        dispatchFocusStackChange(currentUser, displayId, currentFocusedStack, lastFocusedStack);
    }

    public void addSurfaceInNotchIfNeed() {
        this.mHwMwm.addSurfaceInNotchIfNeed();
    }

    private boolean isActivityRecordVisible(ActivityRecord record, ActivityRecord lastResumed, boolean isSleeping) {
        if (record == null) {
            return false;
        }
        if (record.visible) {
            return true;
        }
        if ((isSleeping && !record.canShowWhenLocked()) || lastResumed == null || record.packageName == null) {
            return false;
        }
        if (lastResumed == record) {
            return true;
        }
        if (!record.packageName.equals(lastResumed.packageName) || record.getUid() != lastResumed.getUid() || !isActivityStackVisible(record.getActivityStack())) {
            return false;
        }
        return true;
    }

    private boolean isActivityStackVisible(ActivityStack stack) {
        return (stack == null || stack.getTaskStack() == null || !stack.getTaskStack().isVisible()) ? false : true;
    }

    public List<ActivityManager.RunningTaskInfo> getVisibleTasks() {
        RootActivityContainer rootActivityContainer;
        HwActivityTaskManagerServiceEx hwActivityTaskManagerServiceEx = this;
        int callingPid = Binder.getCallingPid();
        int callingUid = Binder.getCallingUid();
        hwActivityTaskManagerServiceEx.mIAtmsInner.getATMS();
        int i = 1;
        if (!(ActivityTaskManagerService.checkPermission("android.permission.REAL_GET_TASKS", callingPid, callingUid) == 0)) {
            Slog.d(TAG, "permission denied for getVisibleTasks, callingPid:" + callingPid + ", callingUid:" + callingUid + ", requires: android.Manifest.permission.REAL_GET_TASKS");
            return null;
        }
        List<ActivityManager.RunningTaskInfo> list = new ArrayList<>();
        synchronized (hwActivityTaskManagerServiceEx.mIAtmsInner.getATMS().getGlobalLock()) {
            RootActivityContainer rootActivityContainer2 = hwActivityTaskManagerServiceEx.mIAtmsInner.getATMS().mRootActivityContainer;
            ActivityRecord lastResumed = hwActivityTaskManagerServiceEx.mIAtmsInner.getATMS().mLastResumedActivity;
            int i2 = rootActivityContainer2.getChildCount() - 1;
            while (i2 >= 0) {
                ActivityDisplay display = rootActivityContainer2.getChildAt(i2);
                boolean isSleeping = display.shouldSleep();
                int stackNdx = display.getChildCount() - i;
                while (stackNdx >= 0) {
                    ActivityStack stack = display.getChildAt(stackNdx);
                    List<TaskRecord> taskHistory = stack.getTaskHistory();
                    int taskNdx = taskHistory.size() - i;
                    while (true) {
                        if (taskNdx < 0) {
                            rootActivityContainer = rootActivityContainer2;
                            break;
                        }
                        TaskRecord task = taskHistory.get(taskNdx);
                        ActivityRecord topActivity = task.getTopActivity();
                        if (!hwActivityTaskManagerServiceEx.isActivityRecordVisible(topActivity, lastResumed, isSleeping)) {
                            rootActivityContainer = rootActivityContainer2;
                            if (!stack.inMultiWindowMode() && topActivity != null) {
                                break;
                            }
                        } else {
                            ActivityManager.RunningTaskInfo rti = new ActivityManager.RunningTaskInfo();
                            rootActivityContainer = rootActivityContainer2;
                            task.fillTaskInfo(rti);
                            rti.id = rti.taskId;
                            list.add(rti);
                        }
                        taskNdx--;
                        hwActivityTaskManagerServiceEx = this;
                        rootActivityContainer2 = rootActivityContainer;
                    }
                    stackNdx--;
                    i = 1;
                    hwActivityTaskManagerServiceEx = this;
                    rootActivityContainer2 = rootActivityContainer;
                }
                i2--;
                i = 1;
                hwActivityTaskManagerServiceEx = this;
            }
        }
        return list;
    }

    /* JADX WARNING: Removed duplicated region for block: B:7:0x002b  */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x0050  */
    public ActivityManager.TaskSnapshot getTaskSnapshot(int taskId, boolean reducedResolution) {
        boolean isAllowed;
        int callingPid = Binder.getCallingPid();
        int callingUid = Binder.getCallingUid();
        boolean isCallerFromHome = this.mIAtmsInner.getATMS().getRecentTasks().isCallerRecents(callingUid);
        if (!isCallerFromHome) {
            this.mIAtmsInner.getATMS();
            if (ActivityTaskManagerService.checkPermission("android.permission.READ_FRAME_BUFFER", callingPid, callingUid) != 0) {
                isAllowed = false;
                if (!isAllowed) {
                    return getTaskSnapshot(taskId, reducedResolution, isCallerFromHome);
                }
                Slog.d(TAG, "permission denied for getTaskSnapshot, callingPid:" + callingPid + ", callingUid:" + callingUid + ", requires: android.Manifest.permission.READ_FRAME_BUFFER");
                return null;
            }
        }
        isAllowed = true;
        if (!isAllowed) {
        }
    }

    public ActivityManager.TaskSnapshot getActivityTaskSnapshot(IBinder activityToken, boolean isReducedResolution) {
        int taskId;
        boolean isCallerFromHome;
        synchronized (this.mIAtmsInner.getATMS().getGlobalLock()) {
            ActivityRecord ar = ActivityRecord.forTokenLocked(activityToken);
            if (ar != null) {
                if (ar.task != null) {
                    taskId = ar.task.taskId;
                    isCallerFromHome = this.mIAtmsInner.getATMS().getRecentTasks().isCallerRecents(Binder.getCallingUid());
                }
            }
            return null;
        }
        if (taskId != -1) {
            return getTaskSnapshot(taskId, isReducedResolution, isCallerFromHome);
        }
        return null;
    }

    private ActivityManager.TaskSnapshot getTaskSnapshot(int taskId, boolean isReducedResolution, boolean isCallerFromHome) {
        AppWindowToken appWindowToken;
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mIAtmsInner.getATMS().getGlobalLock()) {
                TaskRecord task = this.mIAtmsInner.getATMS().mRootActivityContainer.anyTaskForId(taskId, 1);
                if (task == null) {
                    Slog.w(TAG, "getTaskSnapshot: taskId=" + taskId + " not found");
                    return null;
                }
                ActivityRecord topActivity = task.getTopActivity();
                if (topActivity != null && topActivity.visible) {
                    boolean z = false;
                    if (isCallerFromHome && (appWindowToken = this.mIAtmsInner.getATMS().mWindowManager.getRoot().getAppWindowToken(topActivity.appToken)) != null) {
                        appWindowToken.mHadTakenSnapShot = false;
                    }
                    IHwWindowManagerServiceEx windowManagerServiceEx = this.mIAtmsInner.getATMS().mWindowManager.getWindowManagerServiceEx();
                    IApplicationToken.Stub stub = topActivity.appToken;
                    if (!isCallerFromHome) {
                        z = true;
                    }
                    windowManagerServiceEx.takeTaskSnapshot(stub, z);
                }
                ActivityManager.TaskSnapshot snapshot = task.getSnapshot(isReducedResolution, true);
                Binder.restoreCallingIdentity(ident);
                return snapshot;
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void onCaptionDropAnimationDone(IBinder activityToken) {
        this.mIAtmsInner.getATMS().mUiHandler.post(new Runnable(activityToken) {
            /* class com.android.server.wm.$$Lambda$HwActivityTaskManagerServiceEx$6fhHl505tO27XInRP9pZPTqZa_I */
            private final /* synthetic */ IBinder f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                HwMultiWindowSwitchManager.this.onCaptionDropAnimationDone(this.f$1);
            }
        });
    }

    public void moveStackToFrontEx(ActivityOptions options, ActivityStack stack, ActivityRecord startActivity, ActivityRecord sourceRecord, Rect bounds) {
        this.mHwMdm.moveStackToFrontEx(options, stack);
        this.mHwMwm.moveStackToFrontEx(options, stack, startActivity, sourceRecord, bounds);
    }

    public Rect checkBoundInheritFromSource(ActivityRecord sourceRecord, TaskRecord task) {
        return this.mHwMwm.checkBoundInheritFromSource(sourceRecord, task);
    }

    public void dismissSplitScreenToFocusedStack() {
        this.mIAtmsInner.getATMS().enforceCallerIsRecentsOrHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", "dismissSplitScreenToFocusedStack()");
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mIAtmsInner.getATMS().getGlobalLock()) {
                ActivityStackSupervisor stackSupervisor = this.mIAtmsInner.getStackSupervisor();
                if (!(stackSupervisor == null || stackSupervisor.mRootActivityContainer == null)) {
                    if (stackSupervisor.mRootActivityContainer.getDefaultDisplay() != null) {
                        ActivityStack stack = stackSupervisor.mRootActivityContainer.getDefaultDisplay().getSplitScreenPrimaryStack();
                        if (stack == null) {
                            Slog.e(TAG, "dismissSplitScreenToFocusedStack: primary split-screen stack not found.");
                            Binder.restoreCallingIdentity(ident);
                            return;
                        }
                        ActivityStack focusStack = stackSupervisor.mRootActivityContainer.getDefaultDisplay().getFocusedStack();
                        if (focusStack == null) {
                            Slog.e(TAG, "dismissSplitScreenToFocusedStack: focusStack == null return");
                            Binder.restoreCallingIdentity(ident);
                            return;
                        }
                        focusStack.moveToFront("dismissSplitScreenModeToFocusedStack");
                        stack.setWindowingMode(1);
                        Binder.restoreCallingIdentity(ident);
                        return;
                    }
                }
                Slog.e(TAG, "dismissSplitScreenToFocusedStack:stackSupervisor not found.");
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void handleMultiWindowSwitch(IBinder activityToken, Bundle info) {
        this.mIAtmsInner.getATMS().mUiHandler.post(new Runnable(activityToken, info) {
            /* class com.android.server.wm.$$Lambda$HwActivityTaskManagerServiceEx$45nmc_xkDm4HjtWPyYX1Qxc_w */
            private final /* synthetic */ IBinder f$1;
            private final /* synthetic */ Bundle f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.lang.Runnable
            public final void run() {
                HwMultiWindowSwitchManager.this.addHotArea(this.f$1, this.f$2);
            }
        });
    }

    public Rect relocateOffScreenWindow(Rect originalWindowBounds, ActivityStack stack, float scale) {
        return this.mHwMwm.relocateOffScreenWindow(originalWindowBounds, stack, scale);
    }

    public Point getDragBarCenterPoint(Rect originalWindowBounds, ActivityStack stack) {
        return this.mHwMwm.getDragBarCenterPoint(originalWindowBounds, stack);
    }

    private void setDimmer(ActivityRecord activityRecord) {
        WindowState mainWindow;
        synchronized (this.mIAtmsInner.getATMS().getGlobalLock()) {
            if (activityRecord == null) {
                ActivityStack stack = null;
                try {
                    if (!(this.mIAtmsInner.getStackSupervisor() == null || this.mIAtmsInner.getStackSupervisor().mRootActivityContainer == null || this.mIAtmsInner.getStackSupervisor().mRootActivityContainer.getDefaultDisplay() == null)) {
                        stack = this.mIAtmsInner.getStackSupervisor().mRootActivityContainer.getDefaultDisplay().getFocusedStack();
                    }
                    if (stack == null) {
                        Slog.w(TAG, "setDimmer: No stack:" + stack);
                        return;
                    }
                    activityRecord = stack.getTopActivity();
                } catch (Throwable stack2) {
                    throw stack2;
                }
            }
            if (!(activityRecord == null || activityRecord.mAppWindowToken == null || (mainWindow = activityRecord.mAppWindowToken.findMainWindow()) == null || mainWindow.getDimmer() == null)) {
                mainWindow.getDimmer().stopDim(mainWindow.getPendingTransaction());
                mainWindow.getDimmer().resetDimStates();
                mainWindow.getDimmer().setHideFreeFormFlag(true);
            }
        }
    }

    public int[] setFreeformStackVisibility(int displayId, int[] stackIdArray, boolean isVisible) {
        Binder.getCallingPid();
        int appId = UserHandle.getAppId(Binder.getCallingUid());
        if (!(appId == 1000 || appId == 0)) {
            this.mIAtmsInner.getATMS().enforceCallerIsRecentsOrHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", "setFreeformStackVisibility()");
        }
        if (!isVisible) {
            setDimmer(null);
        }
        long ident = Binder.clearCallingIdentity();
        try {
            return this.mHwMwm.setFreeformStackVisibility(displayId, stackIdArray, isVisible);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void updateDragFreeFormPos(ActivityStack stack) {
        this.mHwMwm.updateDragFreeFormPos(stack);
    }

    public Bundle getSplitStacksPos(int displayId, int splitRatio) {
        return this.mHwMwm.getSplitStacksPos(displayId, splitRatio);
    }

    public float[] getScaleRange(ActivityStack stack) {
        return this.mHwMwm.getScaleRange(stack);
    }

    public boolean isInDisplaySurfaceScaled() {
        return this.mHwMwm.isInDisplaySurfaceScaled();
    }

    public void maximizeHwFreeForm() {
        this.mHwMwm.maximizeHwFreeForm();
    }

    public boolean enterCoordinationMode(Intent intent) {
        Throwable th;
        int currentState = ((DisplayManagerInternal) LocalServices.getService(DisplayManagerInternal.class)).getDisplayMode();
        if (HwFoldScreenState.isFoldScreenDevice() && currentState != 1 && currentState != 4) {
            if (currentState != 0) {
                HwFoldScreenManagerInternal fsmInternal = null;
                Object instance = LocalServices.getService(HwFoldScreenManagerInternal.class);
                if (instance instanceof HwFoldScreenManagerInternal) {
                    fsmInternal = (HwFoldScreenManagerInternal) instance;
                }
                if (fsmInternal == null) {
                    Slog.i(TAG, "Not find HwFoldScreenManagerInternal service, enterCoordinationMode return");
                    return false;
                } else if (fsmInternal.getFoldableState() == 1) {
                    Slog.i(TAG, "Current FoldableState EXPAND, enterCoordinationMode return");
                    return false;
                } else if (fsmInternal.isPausedDispModeChange()) {
                    Slog.i(TAG, "FSM isPausedDispModeChange enterCoordinationMode return");
                    return false;
                } else {
                    Slog.d(TAG, "enterCoordinationMode");
                    synchronized (this.mIAtmsInner.getATMS().getGlobalLock()) {
                        try {
                            long ident = Binder.clearCallingIdentity();
                            try {
                                ActivityStack stack = this.mIAtmsInner.getStackSupervisor().mRootActivityContainer.getDefaultDisplay().getFocusedStack();
                                if (stack == null) {
                                    Slog.w(TAG, "enterCoordinationMode:No stack:" + stack);
                                    Binder.restoreCallingIdentity(ident);
                                    return false;
                                }
                                CoordinationModeUtils utils = CoordinationModeUtils.getInstance(this.mContext);
                                if (utils.isEnterOrExitCoordinationMode()) {
                                    Slog.w(TAG, "enterCoordinationMode:It is now Entering or Exiting CoordinationMode");
                                    Binder.restoreCallingIdentity(ident);
                                    return false;
                                } else if (utils.getCoordinationCreateMode() != 0) {
                                    Slog.w(TAG, "enterCoordinationMode:It is now In CoordinationMode");
                                    Binder.restoreCallingIdentity(ident);
                                    return false;
                                } else {
                                    this.mIAtmsInner.getATMS().mWindowManager.disableEventDispatching(1000);
                                    utils.setCoordinationState(1);
                                    setFreeformStackVisibility(0, null, false);
                                    setDisplayMode(4);
                                    setCoordinationCreateMode(currentState);
                                    try {
                                        this.mCoordinationIntent = intent;
                                        this.mCoordinationIntent.addFlags(268435456);
                                        this.mCoordinationIntent.addHwFlags(32768);
                                        this.mPkgNameInCoordinationMode = this.mCoordinationIntent.getPackage();
                                        Slog.d(TAG, "enterCoordinationMode step one");
                                        Binder.restoreCallingIdentity(ident);
                                        return true;
                                    } catch (Throwable th2) {
                                        th = th2;
                                        throw th;
                                    }
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                Binder.restoreCallingIdentity(ident);
                                throw th;
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            throw th;
                        }
                    }
                }
            }
        }
        return false;
    }

    public void enterCoordinationMode() {
        WindowState mainWindow;
        synchronized (this.mIAtmsInner.getATMS().getGlobalLock()) {
            long ident = Binder.clearCallingIdentity();
            try {
                Slog.d(TAG, "enterCoordinationMode step two");
                ActivityStack stack = this.mIAtmsInner.getStackSupervisor().mRootActivityContainer.getDefaultDisplay().getFocusedStack();
                if (stack != null) {
                    if (this.mCoordinationIntent != null) {
                        CoordinationModeUtils utils = CoordinationModeUtils.getInstance(this.mContext);
                        boolean z = true;
                        if (utils.getCoordinationState() != 1) {
                            Binder.restoreCallingIdentity(ident);
                            return;
                        }
                        ActivityRecord topActivity = stack.getTopActivity();
                        if (topActivity == null || topActivity.packageName == null || topActivity.packageName.equals(this.mCoordinationIntent.getPackage())) {
                            stack.moveToFront("enterCoordinationMode", (TaskRecord) null);
                            stack.setWindowingMode(11);
                            Rect dockedBounds = new Rect();
                            int rotation = this.mIAtmsInner.getATMS().mWindowManager.getDefaultDisplayContentLocked().getDisplayInfo().rotation;
                            utils.getStackCoordinationModeBounds(true, rotation, dockedBounds);
                            resizeCoordinationStackLocked(dockedBounds);
                            this.mKeepPrimaryCoordinationResumed = true;
                            CoordinationStackDividerManager instance = CoordinationStackDividerManager.getInstance(this.mContext);
                            if (this.mContext.getResources().getConfiguration().orientation != 2) {
                                z = false;
                            }
                            instance.addDividerView(z);
                            ActivityOptions opts = ActivityOptions.makeBasic();
                            Rect secondaryBounds = new Rect();
                            CoordinationModeUtils.getInstance(this.mContext).getStackCoordinationModeBounds(false, rotation, secondaryBounds);
                            opts.setLaunchBounds(secondaryBounds);
                            opts.setLaunchWindowingMode(12);
                            this.mContext.startActivityAsUser(new Intent(this.mCoordinationIntent), opts.toBundle(), new UserHandle(this.mIAtmsInner.getATMS().getCurrentUserId()));
                            utils.setCoordinationState(2);
                            Slog.d(TAG, "enterCoordinationMode over");
                            this.mCoordinationIntent = null;
                            if (!(topActivity == null || topActivity.mAppWindowToken == null || (mainWindow = topActivity.mAppWindowToken.findMainWindow()) == null)) {
                                mainWindow.reportResized();
                            }
                            Binder.restoreCallingIdentity(ident);
                            return;
                        }
                        Slog.w(TAG, "enterCoordinationMode, no match package, primary:" + topActivity.packageName);
                        utils.setCoordinationState(2);
                        Binder.restoreCallingIdentity(ident);
                        return;
                    }
                }
                Slog.w(TAG, "enterCoordinationMode: No stack:" + stack);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    private void setDisplayMode(int state) {
        HwFoldScreenManagerInternal foldScreenManagerInternal = (HwFoldScreenManagerInternal) LocalServices.getService(HwFoldScreenManagerInternal.class);
        if (foldScreenManagerInternal != null) {
            foldScreenManagerInternal.setDisplayMode(state);
        }
    }

    private void setCoordinationCreateMode(int state) {
        CoordinationModeUtils utils = CoordinationModeUtils.getInstance(this.mContext);
        if (state == 3) {
            utils.setCoordinationCreateMode(3);
        } else {
            utils.setCoordinationCreateMode(4);
        }
    }

    private void resizeCoordinationStackLocked(Rect dockedBounds) {
        Throwable th;
        ActivityStackSupervisor stackSupervisor = this.mIAtmsInner.getStackSupervisor();
        if (stackSupervisor.mRootActivityContainer != null) {
            ActivityDisplay display = stackSupervisor.mRootActivityContainer.getDefaultDisplay();
            ActivityStack coordinationPrimaryStack = display.getCoordinationPrimaryStack();
            if (coordinationPrimaryStack == null) {
                Slog.w(TAG, "resizeCoordinationStackLocked: coordinationPrimaryStack not found");
                return;
            }
            WindowManagerService windowManager = this.mIAtmsInner.getATMS().mWindowManager;
            windowManager.deferSurfaceLayout();
            try {
                ActivityRecord r = coordinationPrimaryStack.topRunningActivityLocked();
                try {
                    coordinationPrimaryStack.resize(dockedBounds, (Rect) null, (Rect) null);
                    Rect otherTaskRect = new Rect();
                    Rect tempRect = new Rect();
                    for (int i = display.getChildCount() - 1; i >= 0; i--) {
                        ActivityStack current = display.getChildAt(i);
                        if (current.getWindowingMode() != 11 && !current.inHwFreeFormWindowingMode()) {
                            if (!current.inPinnedWindowingMode()) {
                                current.setWindowingMode(12);
                                current.getStackDockedModeBounds((Rect) null, (Rect) null, tempRect, otherTaskRect);
                                stackSupervisor.mRootActivityContainer.resizeStack(current, !tempRect.isEmpty() ? tempRect : null, !otherTaskRect.isEmpty() ? otherTaskRect : null, (Rect) null, true, true, false);
                            }
                        }
                    }
                    coordinationPrimaryStack.ensureVisibleActivitiesConfigurationLocked(r, true);
                    windowManager.continueSurfaceLayout();
                } catch (Throwable th2) {
                    th = th2;
                    windowManager.continueSurfaceLayout();
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                windowManager.continueSurfaceLayout();
                throw th;
            }
        }
    }

    public boolean exitCoordinationMode(boolean toTop, boolean changeMode) {
        synchronized (this.mIAtmsInner.getATMS().getGlobalLock()) {
            long ident = Binder.clearCallingIdentity();
            try {
                Slog.d(TAG, "exitCoordinationMode");
                final CoordinationModeUtils utils = CoordinationModeUtils.getInstance(this.mContext);
                if (utils.isEnterOrExitCoordinationMode()) {
                    Slog.w(TAG, "exitCoordinationMode:It is now Exiting or Entering CoordinationMode");
                    return false;
                } else if (utils.getCoordinationCreateMode() == 0) {
                    Slog.w(TAG, "exitCoordinationMode:It is not in CoordinationMode");
                    Binder.restoreCallingIdentity(ident);
                    return false;
                } else {
                    utils.setCoordinationState(3);
                    int currentCreateMode = utils.getCoordinationCreateMode();
                    utils.setCoordinationCreateMode(2);
                    if (changeMode) {
                        if (currentCreateMode == 3) {
                            setDisplayMode(3);
                        } else if (currentCreateMode == 4) {
                            setDisplayMode(2);
                        }
                    }
                    Slog.d(TAG, "exitCoordinationMode step one");
                    this.mHwHandler.postDelayed(new Runnable() {
                        /* class com.android.server.wm.HwActivityTaskManagerServiceEx.AnonymousClass15 */

                        @Override // java.lang.Runnable
                        public void run() {
                            if (utils.getCoordinationState() == 3) {
                                Slog.d(HwActivityTaskManagerServiceEx.TAG, "exitCoordinationMode step two timeout");
                                HwActivityTaskManagerServiceEx.this.exitCoordinationMode();
                            }
                        }
                    }, 1500);
                    Binder.restoreCallingIdentity(ident);
                    return true;
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    public void exitCoordinationMode() {
        synchronized (this.mIAtmsInner.getATMS().getGlobalLock()) {
            long ident = Binder.clearCallingIdentity();
            try {
                Slog.d(TAG, "exitCoordinationMode step two");
                CoordinationModeUtils utils = CoordinationModeUtils.getInstance(this.mContext);
                if (utils.getCoordinationState() == 3) {
                    ActivityStackSupervisor stackSupervisor = this.mIAtmsInner.getStackSupervisor();
                    if (stackSupervisor == null || stackSupervisor.mRootActivityContainer == null) {
                        Binder.restoreCallingIdentity(ident);
                        return;
                    }
                    ActivityDisplay display = stackSupervisor.mRootActivityContainer.getDefaultDisplay();
                    ActivityStack primaryCoordinationStack = display.getCoordinationPrimaryStack();
                    ActivityStack secondaryCoordinationStack = display.getTopStackInWindowingMode(12);
                    if (primaryCoordinationStack == null || secondaryCoordinationStack == null) {
                        Slog.w(TAG, "exitCoordinationMode:not found primaryCoordinationStack:" + primaryCoordinationStack + " or secondaryCoordinationStack:" + secondaryCoordinationStack);
                        ArrayList<ActivityStack> stacks = display.getAllStacksInWindowingMode(1);
                        int stackSize = stacks.size();
                        for (int i = 0; i <= stackSize - 1; i++) {
                            ActivityStack otherstack = stacks.get(i);
                            if (otherstack != null && otherstack.getTopActivity() != null) {
                                if (otherstack.getTopActivity().packageName.equals(this.mPkgNameInCoordinationMode)) {
                                    if (otherstack.getTopActivity().shortComponentName.contains("CollaborationActivity")) {
                                        otherstack.finishAllActivitiesLocked(true);
                                    }
                                }
                            }
                        }
                        utils.setCoordinationCreateMode(0);
                        CoordinationStackDividerManager.getInstance(this.mContext).removeDividerView();
                        utils.setCoordinationState(4);
                        this.mPkgNameInCoordinationMode = "";
                        Binder.restoreCallingIdentity(ident);
                        return;
                    }
                    ArrayList<ActivityStack> stacks2 = display.getAllStacksInWindowingMode(12);
                    int stackSize2 = stacks2.size();
                    for (int i2 = 0; i2 <= stackSize2 - 1; i2++) {
                        ActivityStack otherstack2 = stacks2.get(i2);
                        ActivityRecord topActivity = otherstack2.getTopActivity();
                        if (!(topActivity == null || topActivity.packageName == null || topActivity.shortComponentName == null || !topActivity.packageName.equals(this.mPkgNameInCoordinationMode) || !topActivity.shortComponentName.contains("CollaborationActivity"))) {
                            otherstack2.finishAllActivitiesLocked(true);
                        }
                    }
                    utils.setCoordinationCreateMode(0);
                    primaryCoordinationStack.setWindowingMode(1);
                    CoordinationStackDividerManager.getInstance(this.mContext).removeDividerView();
                    utils.setCoordinationState(4);
                    this.mPkgNameInCoordinationMode = "";
                    HwFoldScreenManagerInternal fsmInternal = (HwFoldScreenManagerInternal) LocalServices.getService(HwFoldScreenManagerInternal.class);
                    if (fsmInternal != null) {
                        fsmInternal.resumeDispModeChange();
                    }
                    Slog.d(TAG, "exitCoordinationMode over");
                    Binder.restoreCallingIdentity(ident);
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    public void resumeCoordinationPrimaryStack(ActivityRecord r) {
        ActivityStackSupervisor stackSupervisor;
        if (r != null && this.mKeepPrimaryCoordinationResumed && (stackSupervisor = this.mIAtmsInner.getStackSupervisor()) != null && stackSupervisor.mRootActivityContainer != null) {
            ActivityDisplay display = stackSupervisor.mRootActivityContainer.getDefaultDisplay();
            ActivityStack primaryCoordinationStack = display.getCoordinationPrimaryStack();
            ActivityStack secondaryCoordinationStack = display.getTopStackInWindowingMode(12);
            if (primaryCoordinationStack == null || r.getStackId() != secondaryCoordinationStack.mStackId) {
                this.mKeepPrimaryCoordinationResumed = false;
                return;
            }
            ActivityRecord ar = primaryCoordinationStack.topRunningActivityLocked();
            this.mKeepPrimaryCoordinationResumed = false;
            ar.moveFocusableActivityToTop("setFocusedTask");
        }
    }

    public boolean shouldResumeCoordinationPrimaryStack() {
        return this.mKeepPrimaryCoordinationResumed;
    }

    public void setSplitBarVisibility(boolean isVisibility) {
        this.mHwMwm.setSplitBarVisibility(isVisibility);
    }

    public boolean isSwitchToMagicWin(int stackId, boolean isFreeze, int orientation) {
        if (!HwMwUtils.ENABLED) {
            return false;
        }
        return HwMwUtils.performPolicy(10, new Object[]{Integer.valueOf(stackId), Boolean.valueOf(isFreeze), Integer.valueOf(orientation)}).getBoolean("RESULT_SPLITE_SCREEN", false);
    }

    public boolean showIncompatibleAppDialog(ActivityInfo activityInfo, String callingPackage) {
        if (!HW_SHOW_INCOMPATIBLE_DIALOG || activityInfo == null || TextUtils.isEmpty(callingPackage) || this.mIsDialogShow) {
            return false;
        }
        if (this.mIsClickCancelButton) {
            this.mIsClickCancelButton = false;
            return false;
        }
        String pkgName = activityInfo.packageName;
        if (TextUtils.isEmpty(pkgName)) {
            return false;
        }
        if (checkNeedShowDialog(activityInfo, callingPackage, pkgName)) {
            this.mCurrentPkgName = pkgName;
            Handler handler = this.mHwHandler;
            handler.sendMessage(handler.obtainMessage(49));
            this.mIsDialogShow = true;
            return true;
        }
        this.mIsDialogShow = false;
        return false;
    }

    public void notifyPopupCamera(final String shortCompName) {
        if (isSupportPopupCamera() && !TextUtils.isEmpty(shortCompName)) {
            this.mHwHandler.post(new Runnable() {
                /* class com.android.server.wm.HwActivityTaskManagerServiceEx.AnonymousClass16 */

                @Override // java.lang.Runnable
                public void run() {
                    try {
                        IHwCameraServiceProxy hwCameraServiceProxy = HwServiceFactory.getHwCameraServiceProxy(HwActivityTaskManagerServiceEx.this.mContext);
                        if (hwCameraServiceProxy != null) {
                            hwCameraServiceProxy.notifyPopupCamera(shortCompName);
                        } else {
                            Slog.e(HwActivityTaskManagerServiceEx.TAG, "Can't get hwCameraServiceProxy when notifyPopupCamera!");
                        }
                    } catch (RuntimeException e) {
                    } catch (Exception e2) {
                    }
                }
            });
        }
    }

    private boolean isSupportPopupCamera() {
        HWExtDeviceManager hwExtDeviceManager;
        if (!this.mIsSupportPopupCameraInit && (hwExtDeviceManager = HWExtDeviceManager.getInstance(this.mContext)) != null) {
            this.mIsSupportPopupCamera = hwExtDeviceManager.supportMotionFeature(3100);
            this.mIsSupportPopupCameraInit = true;
            Slog.i(TAG, "Init isSupportPopupCamera: " + this.mIsSupportPopupCamera);
        }
        return this.mIsSupportPopupCamera;
    }

    private boolean checkNeedShowDialog(ActivityInfo activityInfo, String callingPackage, String pkgName) {
        File file;
        if (!isColdLaunch(activityInfo) || !isCallerHome(callingPackage) || (file = this.mCompatibileXmlFile) == null || !file.exists()) {
            return false;
        }
        if (sCompatibileFileHashMap.isEmpty()) {
            String[] compatibilityXmlDir = HwCfgFilePolicy.getDownloadCfgFile(STRING_INCOMPATIBLE_CFG_DIR, STRING_INCOMPATIBLE_CFG_FILE_PATH);
            if (compatibilityXmlDir != null) {
                this.mCompatibileXmlFile = new File(compatibilityXmlDir[0]);
            }
            Handler handler = this.mHwHandler;
            handler.sendMessage(handler.obtainMessage(50));
            if (!readIncompatibleOrNotRemindXml(this.mCompatibileXmlFile, pkgName, true)) {
                return false;
            }
        } else {
            String xmlPkgVersion = sCompatibileFileHashMap.getOrDefault(pkgName, "");
            if (TextUtils.isEmpty(xmlPkgVersion) || !checkPreloadAppVersion(pkgName, xmlPkgVersion)) {
                return false;
            }
        }
        if (!PRELOAD_APP_NOT_REMIND_XML.exists()) {
            if (this.mCompatibileNoRemindSet.size() != 0) {
                this.mCompatibileNoRemindSet.clear();
            }
            return true;
        } else if (this.mCompatibileNoRemindSet.size() != 0) {
            return !this.mCompatibileNoRemindSet.contains(pkgName);
        } else {
            Handler handler2 = this.mHwHandler;
            handler2.sendMessage(handler2.obtainMessage(51));
            return !readIncompatibleOrNotRemindXml(PRELOAD_APP_NOT_REMIND_XML, pkgName, false);
        }
    }

    private boolean isCallerHome(String callingPackage) {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        List<ResolveInfo> infos = this.mContext.getPackageManager().queryIntentActivities(intent, 65536);
        if (infos == null || infos.size() == 0) {
            return false;
        }
        for (ResolveInfo homeInfo : infos) {
            if (!(homeInfo == null || homeInfo.activityInfo == null || !callingPackage.equals(homeInfo.activityInfo.packageName))) {
                if (!ActivityTaskManagerDebugConfig.DEBUG_HW_ACTIVITY) {
                    return true;
                }
                Log.i(TAG, "showIncompatibleAppDialog caller is home");
                return true;
            }
        }
        return false;
    }

    private boolean isColdLaunch(ActivityInfo activityInfo) {
        WindowProcessController windowProcessController = this.mIAtmsInner.getATMS().getProcessController(activityInfo.processName, activityInfo.applicationInfo.uid);
        if (windowProcessController != null && windowProcessController.hasThread()) {
            return false;
        }
        if (!ActivityTaskManagerDebugConfig.DEBUG_HW_ACTIVITY) {
            return true;
        }
        Log.i(TAG, "showIncompatibleAppDialog isColdLaunch true");
        return true;
    }

    private boolean readIncompatibleOrNotRemindXml(File xmlFile, String pkgName, boolean needCheckPkgVersion) {
        InputStream inputStream = null;
        try {
            InputStream inputStream2 = new FileInputStream(xmlFile);
            XmlPullParser xmlParser = Xml.newPullParser();
            xmlParser.setInput(inputStream2, null);
            int xmlEventType = xmlParser.next();
            while (xmlEventType != 1) {
                xmlEventType = xmlParser.next();
                if (xmlEventType == 2) {
                    if (STRING_INCOMPATIBLE_XML_PKG.equals(xmlParser.getName())) {
                        String xmlPkgName = xmlParser.getAttributeValue(null, STRING_INCOMPATIBLE_XML_ATTRIBUTE_NAME);
                        if (TextUtils.isEmpty(xmlPkgName)) {
                            continue;
                        } else if (xmlPkgName.equals(pkgName)) {
                            if (needCheckPkgVersion) {
                                boolean checkPreloadAppVersion = checkPreloadAppVersion(pkgName, xmlParser.getAttributeValue(null, STRING_INCOMPATIBLE_XML_ATTRIBUTE_PKG_VERSION));
                                try {
                                    inputStream2.close();
                                } catch (IOException e) {
                                    Log.e(TAG, "readIncompatibleOrNotRemindXml IOException while closing stream");
                                }
                                return checkPreloadAppVersion;
                            }
                            try {
                                inputStream2.close();
                            } catch (IOException e2) {
                                Log.e(TAG, "readIncompatibleOrNotRemindXml IOException while closing stream");
                            }
                            return true;
                        }
                    }
                }
            }
            try {
                inputStream2.close();
                return false;
            } catch (IOException e3) {
                Log.e(TAG, "readIncompatibleOrNotRemindXml IOException while closing stream");
                return false;
            }
        } catch (FileNotFoundException e4) {
            Log.e(TAG, "readIncompatibleOrNotRemindXml FileNotFoundException");
            if (0 == 0) {
                return false;
            }
            inputStream.close();
            return false;
        } catch (XmlPullParserException e5) {
            Log.e(TAG, "readIncompatibleOrNotRemindXml XmlPullParserException");
            if (0 == 0) {
                return false;
            }
            inputStream.close();
            return false;
        } catch (IOException e6) {
            Log.e(TAG, "readIncompatibleOrNotRemindXml IOException");
            if (0 == 0) {
                return false;
            }
            inputStream.close();
            return false;
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    inputStream.close();
                } catch (IOException e7) {
                    Log.e(TAG, "readIncompatibleOrNotRemindXml IOException while closing stream");
                }
            }
            throw th;
        }
    }

    private void readXmlToSetOrMap(File xmlFile, int typeSetOrMap) {
        boolean isTypeHashMap;
        if (typeSetOrMap == 0) {
            isTypeHashMap = true;
        } else if (typeSetOrMap == 1) {
            isTypeHashMap = false;
        } else {
            return;
        }
        InputStream inputStream = null;
        try {
            InputStream inputStream2 = new FileInputStream(xmlFile);
            XmlPullParser xmlParser = Xml.newPullParser();
            xmlParser.setInput(inputStream2, null);
            int xmlEventType = xmlParser.next();
            Set<String> tempSet = new HashSet<>();
            HashMap<String, String> tempMap = new HashMap<>();
            while (xmlEventType != 1) {
                xmlEventType = xmlParser.next();
                if (xmlEventType == 2) {
                    if (STRING_INCOMPATIBLE_XML_PKG.equals(xmlParser.getName())) {
                        String xmlPkgName = xmlParser.getAttributeValue(null, STRING_INCOMPATIBLE_XML_ATTRIBUTE_NAME);
                        String xmlPkgVersion = xmlParser.getAttributeValue(null, STRING_INCOMPATIBLE_XML_ATTRIBUTE_PKG_VERSION);
                        if (!TextUtils.isEmpty(xmlPkgName)) {
                            if (isTypeHashMap) {
                                tempMap.put(xmlPkgName, xmlPkgVersion);
                            } else {
                                tempSet.add(xmlPkgName);
                            }
                        }
                    }
                }
            }
            if (isTypeHashMap) {
                sCompatibileFileHashMap = tempMap;
            } else {
                this.mCompatibileNoRemindSet = tempSet;
            }
            try {
                inputStream2.close();
            } catch (IOException e) {
                Log.e(TAG, "readXmlToSetOrMap IOException while closing stream");
            }
        } catch (FileNotFoundException e2) {
            Log.e(TAG, "readXmlToSetOrMap FileNotFoundException");
            if (0 != 0) {
                inputStream.close();
            }
        } catch (XmlPullParserException e3) {
            Log.e(TAG, "readXmlToSetOrMap XmlPullParserException");
            if (0 != 0) {
                inputStream.close();
            }
        } catch (IOException e4) {
            Log.e(TAG, "readXmlToSetOrMap IOException");
            if (0 != 0) {
                inputStream.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    inputStream.close();
                } catch (IOException e5) {
                    Log.e(TAG, "readXmlToSetOrMap IOException while closing stream");
                }
            }
            throw th;
        }
    }

    private boolean checkPreloadAppVersion(String pkgName, String xmlPkgVersion) {
        int currentVersionCode;
        int xmlVersionCode;
        if (TextUtils.isEmpty(xmlPkgVersion)) {
            return false;
        }
        try {
            PackageInfo packageInfo = this.mContext.getPackageManager().getPackageInfo(pkgName, 0);
            if (packageInfo == null || (currentVersionCode = packageInfo.versionCode) >= (xmlVersionCode = Integer.parseInt(xmlPkgVersion))) {
                return false;
            }
            if (!ActivityTaskManagerDebugConfig.DEBUG_HW_ACTIVITY) {
                return true;
            }
            Log.i(TAG, "showIncompatibleAppDialog checkVersion pkg:" + pkgName + " currentVersion:" + currentVersionCode + " xmlVersion:" + xmlVersionCode);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "checkPreloadAppVersion PackageManager.NameNotFoundException");
            return false;
        } catch (NumberFormatException e2) {
            Log.e(TAG, "checkPreloadAppVersion NumberFormatException");
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showIncompatibleDialog() {
        PackageManager packageManager = this.mContext.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(this.mCurrentPkgName, 0);
            if (packageInfo == null) {
                this.mIsDialogShow = false;
            } else {
                initShowDialog(packageManager.getApplicationLabel(packageInfo.applicationInfo).toString());
            }
        } catch (PackageManager.NameNotFoundException e) {
            this.mIsDialogShow = false;
        }
    }

    private void initShowDialog(String appName) {
        Context uiContext = this.mIAtmsInner.getUiContext();
        AlertDialog alterDialog = new AlertDialog.Builder(uiContext, 33948078).create();
        alterDialog.getWindow().setType(2003);
        alterDialog.setCancelable(false);
        alterDialog.setTitle(uiContext.getString(33686265));
        alterDialog.setMessage(uiContext.getString(33686266, appName));
        View dialogView = LayoutInflater.from(uiContext).cloneInContext(new ContextThemeWrapper(uiContext, 33948078)).inflate(34013289, (ViewGroup) null);
        alterDialog.setView(dialogView);
        final CheckBox checkBox = (CheckBox) dialogView.findViewById(16908289);
        checkBox.setText(33685813);
        alterDialog.setButton(-1, uiContext.getString(33686268), new DialogInterface.OnClickListener() {
            /* class com.android.server.wm.HwActivityTaskManagerServiceEx.AnonymousClass17 */

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int which) {
                HwActivityTaskManagerServiceEx.this.doIncompatibleDialogUpdate();
            }
        });
        alterDialog.setButton(-2, uiContext.getString(33686267), new DialogInterface.OnClickListener() {
            /* class com.android.server.wm.HwActivityTaskManagerServiceEx.AnonymousClass18 */

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int which) {
                if (checkBox.isChecked()) {
                    HwActivityTaskManagerServiceEx.this.doIncompatibleDialogNoNotifity();
                }
                HwActivityTaskManagerServiceEx.this.doIncompatibleDialogCancel();
            }
        });
        WindowManager.LayoutParams attrs = alterDialog.getWindow().getAttributes();
        attrs.privateFlags = 16;
        alterDialog.getWindow().setAttributes(attrs);
        alterDialog.show();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doIncompatibleDialogNoNotifity() {
        this.mCompatibileNoRemindSet.add(this.mCurrentPkgName);
        Handler handler = this.mHwHandler;
        handler.sendMessage(handler.obtainMessage(52));
        Log.i(TAG, "showIncompatibleAppDialog add not remind pkg:" + this.mCurrentPkgName);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doIncompatibleDialogUpdate() {
        this.mIsDialogShow = false;
        Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=" + this.mCurrentPkgName));
        intent.addFlags(268435456);
        try {
            this.mContext.startActivityAsUser(intent, new UserHandle(this.mIAtmsInner.getATMS().mWindowManager.mCurrentUserId));
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "doIncompatibleDialogUpdate activity not found");
        } catch (Exception e2) {
            Log.e(TAG, "doIncompatibleDialogUpdate Exception");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doIncompatibleDialogCancel() {
        this.mIsDialogShow = false;
        if (!noNeedLaunchActivity()) {
            this.mIsClickCancelButton = true;
            new Intent();
            Intent intent = this.mContext.getPackageManager().getLaunchIntentForPackage(this.mCurrentPkgName);
            if (intent != null) {
                intent.setFlags(270532608);
                try {
                    this.mContext.startActivityAsUser(intent, new UserHandle(this.mIAtmsInner.getATMS().mWindowManager.mCurrentUserId));
                } catch (ActivityNotFoundException e) {
                    Log.e(TAG, "doIncompatibleDialogCancel activity not found");
                } catch (Exception e2) {
                    Log.e(TAG, "doIncompatibleDialogCancel Exception");
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void readIncompatibleXmlToHashMap() {
        readXmlToSetOrMap(this.mCompatibileXmlFile, 0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void readNotRemindXmlToSet() {
        readXmlToSetOrMap(PRELOAD_APP_NOT_REMIND_XML, 1);
    }

    /* JADX INFO: Multiple debug info for r2v1 java.io.FileOutputStream: [D('fos' java.io.FileOutputStream), D('deleted' boolean)] */
    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void writeNotRemindIncompatibleAppXml(File xmlFile) {
        if (xmlFile.exists()) {
            boolean deleted = false;
            try {
                deleted = xmlFile.delete();
            } catch (SecurityException e) {
                Log.e(TAG, "SecurityException when delete xmlFile in writeNotRemindIncompatibleAppXml");
            }
            if (!deleted) {
                Log.e(TAG, "canot delete xmlFile in writeNotRemindIncompatibleAppXml");
                return;
            }
        }
        FileOutputStream fos = null;
        try {
            XmlSerializer serializer = Xml.newSerializer();
            FileOutputStream fos2 = new FileOutputStream(xmlFile);
            serializer.setOutput(fos2, "utf-8");
            String enter = System.getProperty("line.separator");
            serializer.startTag(null, STRING_INCOMPATIBLE_XML_PKG_LIST);
            serializer.text(enter);
            for (String pkgName : this.mCompatibileNoRemindSet) {
                writePkgNameToNoNotifyXml(serializer, pkgName);
            }
            serializer.endTag(null, STRING_INCOMPATIBLE_XML_PKG_LIST);
            serializer.endDocument();
            try {
                fos2.close();
            } catch (IOException e2) {
                Log.e(TAG, "writeNotRemindIncompatibleAppXml:- IOE while closing stream");
            }
        } catch (IOException e3) {
            Log.e(TAG, "writeNotRemindIncompatibleAppXml exception");
            if (0 != 0) {
                fos.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    fos.close();
                } catch (IOException e4) {
                    Log.e(TAG, "writeNotRemindIncompatibleAppXml:- IOE while closing stream");
                }
            }
            throw th;
        }
    }

    private void writePkgNameToNoNotifyXml(XmlSerializer serializer, String pkgName) {
        try {
            String enter = System.getProperty("line.separator");
            serializer.startTag(null, STRING_INCOMPATIBLE_XML_PKG);
            serializer.attribute(null, STRING_INCOMPATIBLE_XML_ATTRIBUTE_NAME, pkgName);
            serializer.endTag(null, STRING_INCOMPATIBLE_XML_PKG);
            serializer.text(enter);
        } catch (IOException e) {
            Log.e(TAG, "writePkgNameToNoNotifyXml exception");
        }
    }

    private boolean noNeedLaunchActivity() {
        return isAlarm(ActivityManager.getCurrentUser()) || isCallRinging();
    }

    private boolean isAlarm(int user) {
        ComponentName oldCmpName = ComponentName.unflattenFromString("com.android.deskclock/.alarmclock.AlarmKlaxon");
        ComponentName newCmpName = ComponentName.unflattenFromString("com.huawei.deskclock/.alarmclock.AlarmKlaxon");
        HwActivityManagerService hwAms = (HwActivityManagerService) ServiceManager.getService("activity");
        return hwAms.serviceIsRunning(oldCmpName, user) || hwAms.serviceIsRunning(newCmpName, user);
    }

    private boolean isCallRinging() {
        return getPhoneState() == 1;
    }

    private int getPhoneState() {
        int phoneState = 0;
        TelephonyManager telephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        int simCount = telephonyManager.getPhoneCount();
        for (int i = 0; i < simCount; i++) {
            phoneState = telephonyManager.getCallState(i);
            if (phoneState != 0) {
                return phoneState;
            }
        }
        return phoneState;
    }

    public void moveActivityTaskToBackEx(IBinder token) {
        ActivityRecord record;
        if (token != null && (record = ActivityRecord.forTokenLocked(token)) != null) {
            if (this.mHwMdm.isVirtualDisplayId(record.getDisplayId(), "padCast")) {
                ActivityStack stack = record.getActivityStack();
                if (record.getDisplay().getChildCount() == 1) {
                    long origId = Binder.clearCallingIdentity();
                    try {
                        if (record.visible && record.appToken != null) {
                            this.mIAtmsInner.getATMS().mWindowManager.getWindowManagerServiceEx().takeTaskSnapshot(record.appToken, false);
                            this.mIAtmsInner.getATMS().getTaskChangeNotificationController().notifyTaskStackChanged();
                        }
                        ActivityDisplay fromDisplay = this.mIAtmsInner.getRootActivityContainer().getActivityDisplay(record.getDisplayId());
                        ActivityDisplay toDisplay = this.mIAtmsInner.getRootActivityContainer().getDefaultDisplay();
                        this.mHwMdm.moveAboveAppWindowsToDisplay(fromDisplay, toDisplay, stack);
                        stack.reparent(toDisplay, false, true);
                        if (stack.inMultiWindowMode() && !stack.inHwMagicWindowingMode()) {
                            stack.setWindowingMode(0, false, false, false, true, false);
                        }
                        stack.postReparent();
                    } finally {
                        Binder.restoreCallingIdentity(origId);
                    }
                } else if (stack.mStackId == this.mHwMdm.getPadCastVirtualDisplayFirstStackId()) {
                    Slog.i(TAG, "move stacks to default display. mStackId=" + stack.mStackId);
                    this.mHwMdm.moveStacksToDisplay(record.getDisplayId(), 0, false, false);
                }
            }
            if (record.inHwSplitScreenWindowingMode() && record.visible) {
                this.mIAtmsInner.getATMS().mWindowManager.getWindowManagerServiceEx().takeTaskSnapshot(record.appToken, false);
            } else if (record.inHwFreeFormWindowingMode() || this.mHwMwm.isPadCastStack(record.getActivityStack())) {
                Flog.bdReport(991311032, KEY_PACKAGE_NAME, record.packageName);
                TaskRecord taskRecord = record.getTaskRecord();
                if (taskRecord != null) {
                    List<TaskRecord> addTasks = new ArrayList<>(1);
                    addTasks.add(taskRecord);
                    dispatchFreeformBallLifeState(addTasks, "add");
                }
            }
        }
    }

    public void toggleFreeformWindowingModeEx(ActivityRecord record) {
        HwMultiWindowManager hwMultiWindowManager;
        List<ActivityStack> activityStacks;
        if (record != null) {
            int pid = 0;
            if (record.inHwFreeFormWindowingMode()) {
                ActivityDisplay display = record.getDisplay();
                if (!(display == null || (hwMultiWindowManager = this.mHwMwm) == null || (activityStacks = hwMultiWindowManager.getForegroundFreeform(display, true)) == null || activityStacks.size() > 1)) {
                    this.mHwMwm.setColorMgrInfo(false);
                }
                Flog.bdReport(991311033, KEY_PACKAGE_NAME, record.packageName);
                this.mHwMwm.handlePadCastFullStackBallState();
            }
            this.mHwMwm.removeSplitScreenDividerBar(-1, true, record.getDisplayId());
            if (record.inMultiWindowMode()) {
                if (record.app != null) {
                    pid = record.app.mPid;
                }
                HwGameAssistantController hwGameAssistantController = this.mHwGameAssistantController;
                if (hwGameAssistantController != null) {
                    hwGameAssistantController.updateByToggleFreeFormMaximize(pid, record.getUid(), record.packageName);
                }
                Bundle bundle = new Bundle();
                TaskRecord task = record.task;
                if (task != null) {
                    bundle.putInt(HwWmConstants.TASK_ID, task.taskId);
                }
                bundle.putInt(ASSOC_PID, pid);
                bundle.putInt("uid", record.getUid());
                bundle.putString(STRING_INCOMPATIBLE_XML_PKG, record.packageName);
                bundle.putString("android.intent.extra.REASON", "toggleFreeform");
                bundle.putInt("android.intent.extra.user_handle", this.mIAtmsInner.getATMS().mWindowManager.mCurrentUserId);
                Message msg = this.mHwHandler.obtainMessage(24);
                msg.obj = bundle;
                this.mHwHandler.sendMessage(msg);
            }
        }
    }

    public boolean setCustomActivityController(IActivityController controller) {
        this.mIAtmsInner.getATMS().mAmInternal.enforceCallingPermission("android.permission.SET_ACTIVITY_WATCHER", "setCustomActivityController()");
        this.mCustomController = controller;
        return true;
    }

    public boolean customActivityStarting(Intent intent, String packageName) {
        if (this.mCustomController == null) {
            return false;
        }
        boolean isStartOk = true;
        try {
            isStartOk = this.mCustomController.activityStarting(intent.cloneFilter(), packageName);
        } catch (RemoteException e) {
            this.mCustomController = null;
        }
        if (isStartOk) {
            return false;
        }
        Slog.i(TAG, "Not starting activity because custom controller stop it");
        return true;
    }

    public boolean customActivityResuming(String packageName) {
        ISecurityProfileController spc = HwServiceFactory.getSecurityProfileController();
        if (spc != null) {
            spc.handleActivityResuming(packageName);
        }
        IActivityController iActivityController = this.mCustomController;
        if (iActivityController == null) {
            return false;
        }
        boolean isResumeOk = true;
        try {
            isResumeOk = iActivityController.activityResuming(packageName);
        } catch (RemoteException e) {
            this.mCustomController = null;
        }
        if (isResumeOk) {
            return false;
        }
        Slog.i(TAG, "Not resuming activity because custom controller stop it");
        return true;
    }

    public boolean isExSplashEnable(Bundle bundle) {
        String[] appArray;
        List<String> appList;
        if (bundle == null) {
            return false;
        }
        String callingPackage = bundle.getString("exsplash_callingpackage");
        String packageName = bundle.getString("exsplash_package");
        int requestCode = bundle.getInt("exsplash_requestcode");
        boolean isIntercepted = bundle.getBoolean("exsplash_isintercepted", true);
        int userId = bundle.getInt("exsplash_userId");
        if (TextUtils.isEmpty(packageName) || requestCode >= 0 || isIntercepted || UserHandle.isClonedProfile(userId)) {
            return false;
        }
        Parcelable parcelable = bundle.getParcelable("exsplash_info");
        ActivityInfo info = null;
        if (parcelable instanceof ActivityInfo) {
            info = (ActivityInfo) parcelable;
        }
        if (!isColdStart(packageName, info) || HMS_PKG_NAME.equals(callingPackage)) {
            return false;
        }
        try {
            String appListStr = Settings.Global.getString(this.mContext.getContentResolver(), "ex_splash_list");
            int exsplashStatus = Settings.Global.getInt(this.mContext.getContentResolver(), "ex_splash_func_status");
            int canStartExsplash = Settings.Global.getInt(this.mContext.getContentResolver(), "ex_splash_hms_active", 1);
            String hwidPersistentName = Settings.Global.getString(this.mContext.getContentResolver(), "ex_splash_persistent_name");
            if (TextUtils.isEmpty(hwidPersistentName)) {
                hwidPersistentName = HMS_PERSISTENT_NAME;
            }
            if (exsplashStatus != 1 || TextUtils.isEmpty(appListStr) || (appArray = appListStr.split(AwarenessInnerConstants.SEMI_COLON_KEY)) == null || (appList = Arrays.asList(appArray)) == null || !appList.contains(packageName) || (canStartExsplash == 1 && !isHmsActive(hwidPersistentName))) {
                return false;
            }
            return true;
        } catch (Settings.SettingNotFoundException e) {
            Slog.w(TAG, "read exsplash setting error");
            return false;
        }
    }

    private boolean isHmsActive(String hmsProcess) {
        WindowProcessController wpc = this.mIAtmsInner.getATMS().getProcessController(hmsProcess, this.hmsPersistentUid);
        StringBuilder sb = new StringBuilder();
        sb.append("app:");
        sb.append(wpc == null);
        Slog.d(TAG, sb.toString());
        return wpc != null;
    }

    public boolean isColdStart(String packageName, ActivityInfo info) {
        WindowProcessController wpc;
        if (TextUtils.isEmpty(packageName) || info == null || info.applicationInfo == null || (wpc = this.mIAtmsInner.getATMS().getProcessController(packageName, info.applicationInfo.uid)) == null || !wpc.hasActivities()) {
            return true;
        }
        return false;
    }

    public void startExSplash(Bundle bundle, ActivityOptions checkedOptions) {
        if (bundle != null) {
            Parcelable parcelable = bundle.getParcelable("android.intent.extra.INTENT");
            Intent intent = null;
            if (parcelable instanceof Intent) {
                intent = (Intent) parcelable;
            }
            if (intent != null && intent.getComponent() != null) {
                String packageName = intent.getComponent().getPackageName();
                IntentSender target = createIntentSenderForExSplash(intent, checkedOptions, bundle);
                Bundle extras = new Bundle();
                extras.putString("android.intent.extra.PACKAGE_NAME", packageName);
                extras.putParcelable("android.intent.extra.INTENT", target);
                this.mHwHandler.sendMessage(this.mHwHandler.obtainMessage(91, extras));
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateHmsPersistenUid(int userId) {
        IPackageManager pm = AppGlobals.getPackageManager();
        if (pm == null) {
            Slog.w(TAG, "Failed trying to get IPackageManager");
            return;
        }
        ApplicationInfo appInfo = null;
        try {
            appInfo = pm.getApplicationInfo(HMS_PKG_NAME, 1152, userId);
        } catch (RemoteException e) {
            Slog.w(TAG, "Failed trying to get application info: com.huawei.hwid");
        }
        if (appInfo == null) {
            Slog.i(TAG, "get application info failed, packageName = com.huawei.hwid");
        } else {
            this.hmsPersistentUid = appInfo.uid;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showExSplash(Message msg) {
        Bundle bundle;
        try {
            Intent newIntent = new Intent("com.huawei.android.hms.ppskit.PPS_EXSPLASH_SERVICE");
            newIntent.setFlags(276840448);
            if ((msg.obj instanceof Bundle) && (bundle = (Bundle) msg.obj) != null) {
                newIntent.putExtra("android.intent.extra.PACKAGE_NAME", bundle.getString("android.intent.extra.PACKAGE_NAME"));
                Parcelable target = bundle.getParcelable("android.intent.extra.INTENT");
                if (target instanceof IntentSender) {
                    newIntent.putExtra("android.intent.extra.INTENT", (IntentSender) target);
                }
            }
            newIntent.setPackage(HMS_PKG_NAME);
            this.mContext.startService(newIntent);
        } catch (ClassCastException e) {
            Slog.w(TAG, "start exsplash error");
            Settings.Global.putInt(this.mContext.getContentResolver(), "ex_splash_func_status", 0);
        } catch (Exception e2) {
            Slog.w(TAG, "start exsplash error");
            Settings.Global.putInt(this.mContext.getContentResolver(), "ex_splash_func_status", 0);
        }
    }

    private IntentSender createIntentSenderForExSplash(Intent intent, ActivityOptions checkedOptions, Bundle bundle) {
        ActivityOptions options;
        Bundle activityOptions;
        if (bundle == null) {
            return null;
        }
        String callingPackage = bundle.getString("exsplash_callingpackage");
        int callingUid = bundle.getInt("exsplash_callingUid");
        int userId = bundle.getInt("exsplash_userId");
        String resolvedType = bundle.getString("exsplash_resolvedType");
        Bundle activityOptions2 = null;
        if (checkedOptions == null || checkedOptions.getAnimationType() != 12) {
            options = checkedOptions;
        } else {
            activityOptions2 = ActivityOptions.makeOpenCrossProfileAppsAnimation().toBundle();
            options = null;
        }
        if (!HwPCUtils.isPcCastModeInServer() || options == null || !HwPCUtils.isValidExtDisplayId(options.getLaunchDisplayId())) {
            activityOptions = activityOptions2;
        } else {
            ActivityOptions aos = ActivityOptions.makeBasic();
            aos.setLaunchDisplayId(HwPCUtils.getPCDisplayID());
            aos.setLaunchWindowingMode(10);
            activityOptions = aos.toBundle();
        }
        return new IntentSender(this.mIAtmsInner.getATMS().getIntentSenderLocked(2, callingPackage, callingUid, userId, (IBinder) null, (String) null, 0, new Intent[]{intent}, new String[]{resolvedType}, 1409286144, activityOptions));
    }

    public boolean isSplitStackVisible(ActivityDisplay display, int primaryPosition) {
        return this.mHwMwm.isSplitStackVisible(display, primaryPosition);
    }

    public static void updateIncompatibleListByOuc() {
        Log.i(TAG, "updateIncompatibleListByOuc");
        sCompatibileFileHashMap.clear();
    }

    public boolean isHwFreeFormOnlyApp(String packageName) {
        int appAttr;
        if (!HwMultiWindowManager.IS_HW_MULTIWINDOW_SUPPORTED || TextUtils.isEmpty(packageName) || (appAttr = AppTypeRecoManager.getInstance().getAppAttribute(packageName)) == -1 || (65536 & appAttr) == 0 || (131072 & appAttr) == 0) {
            return false;
        }
        return true;
    }

    public boolean isResizableApp(String packageName, int mode) {
        if (!HwMultiWindowManager.IS_HW_MULTIWINDOW_SUPPORTED || TextUtils.isEmpty(packageName)) {
            return ActivityInfo.isResizeableMode(mode);
        }
        int appAttr = AppTypeRecoManager.getInstance().getAppAttribute(packageName);
        if (appAttr == -1) {
            return ActivityInfo.isResizeableMode(mode);
        }
        if ((appAttr & 65536) == 65536) {
            return true;
        }
        if ((appAttr & 131072) == 131072) {
            return false;
        }
        if (Binder.getCallingPid() == ActivityManagerService.MY_PID) {
            return ActivityInfo.isResizeableMode(mode);
        }
        if (!ActivityInfo.isResizeableMode(mode) || ActivityInfo.isPreserveOrientationMode(mode)) {
            return false;
        }
        return true;
    }

    public Bundle getHwMultiWindowAppControlLists() {
        Bundle bundle = new Bundle();
        if (!HwMultiWindowManager.IS_HW_MULTIWINDOW_SUPPORTED) {
            return bundle;
        }
        Map<Integer, List<String>> listMap = AppTypeRecoManager.getInstance().getAppsByAttributes(new ArrayList<>(Arrays.asList(65536, 131072, 262144)));
        List<String> configSupportPCMultiCastList = new ArrayList<>();
        if (HwActivityTaskManager.isPCMultiCastMode()) {
            configSupportPCMultiCastList = this.mHwMdm.getConfigSupportPCMultiCastList();
        }
        if (listMap == null) {
            return bundle;
        }
        List<String> whiteList = listMap.get(65536);
        List<String> blackList = listMap.get(131072);
        List<String> recommendList = listMap.get(262144);
        if (whiteList instanceof ArrayList) {
            bundle.putStringArrayList("whitelist", (ArrayList) whiteList);
        } else {
            whiteList = null;
        }
        if (blackList instanceof ArrayList) {
            bundle.putStringArrayList("blacklist", (ArrayList) blackList);
        } else {
            blackList = null;
        }
        if (recommendList instanceof ArrayList) {
            bundle.putStringArrayList("recomlist", (ArrayList) recommendList);
        }
        ArrayList<String> arrayList = null;
        ArrayList<String> arrayList2 = whiteList != null ? (ArrayList) whiteList : null;
        if (blackList != null) {
            arrayList = (ArrayList) blackList;
        }
        processAppControlList(arrayList2, arrayList, bundle);
        if (configSupportPCMultiCastList instanceof ArrayList) {
            bundle.putStringArrayList("pc_multicast_whitelist", (ArrayList) configSupportPCMultiCastList);
        }
        return bundle;
    }

    private void processAppControlList(ArrayList<String> whiteList, ArrayList<String> blackList, Bundle bundle) {
        if (bundle != null) {
            if (whiteList == null) {
                whiteList = new ArrayList<>();
            }
            if (blackList == null) {
                blackList = new ArrayList<>();
            }
            ArrayList<String> finalWhiteList = new ArrayList<>(whiteList);
            ArrayList<String> finalBlackList = new ArrayList<>(blackList);
            finalWhiteList.removeAll(blackList);
            finalBlackList.removeAll(whiteList);
            whiteList.retainAll(blackList);
            bundle.putStringArrayList("whitelist", finalWhiteList);
            bundle.putStringArrayList("blacklist", finalBlackList);
            bundle.putStringArrayList("whitelist_freeform_only", whiteList);
        }
    }

    public boolean isNeedAdapterCaptionView(String packageName) {
        int appAttr;
        if (!HwMultiWindowManager.IS_HW_MULTIWINDOW_SUPPORTED || TextUtils.isEmpty(packageName) || (appAttr = AppTypeRecoManager.getInstance().getAppAttribute(packageName)) == -1 || (appAttr & 262144) != 262144) {
            return false;
        }
        return true;
    }

    public void saveMultiWindowTipState(String tipKey, int state) {
        HwMultiWindowTips.getInstance(this.mContext).saveMultiWindowTipState(tipKey, state);
    }

    public boolean isSupportDragForMultiWin(IBinder token) {
        return HwMultiWindowSwitchManager.getInstance(this).isSupportedSplit(token);
    }

    public boolean isOverrideConfigByMagicWin(Configuration config) {
        if (!HwMwUtils.ENABLED) {
            return false;
        }
        HwMwUtils.performPolicy(6, new Object[]{config});
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:11:0x0051  */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x002d  */
    public List<String> getVisiblePackages() {
        boolean isAllowed;
        int callingPid;
        HwActivityTaskManagerServiceEx hwActivityTaskManagerServiceEx = this;
        int callingPid2 = Binder.getCallingPid();
        int callingUid = Binder.getCallingUid();
        int appId = UserHandle.getAppId(callingUid);
        int i = 1;
        if (!(appId == 1000 || appId == 0)) {
            hwActivityTaskManagerServiceEx.mIAtmsInner.getATMS();
            if (ActivityTaskManagerService.checkPermission("android.permission.REAL_GET_TASKS", callingPid2, callingUid) != 0) {
                isAllowed = false;
                List<String> list = new ArrayList<>();
                if (isAllowed) {
                    Slog.d(TAG, "permission denied for getVisibleTasks, callingPid:" + callingPid2 + ", callingUid:" + callingUid + ", requires: android.Manifest.permission.REAL_GET_TASKS");
                    return list;
                }
                synchronized (hwActivityTaskManagerServiceEx.mIAtmsInner.getATMS().getGlobalLock()) {
                    try {
                        RootActivityContainer rootActivityContainer = hwActivityTaskManagerServiceEx.mIAtmsInner.getATMS().mRootActivityContainer;
                        ActivityRecord lastResumed = hwActivityTaskManagerServiceEx.mIAtmsInner.getATMS().mLastResumedActivity;
                        int i2 = rootActivityContainer.getChildCount() - 1;
                        while (i2 >= 0) {
                            ActivityDisplay display = rootActivityContainer.getChildAt(i2);
                            boolean isSleeping = display.shouldSleep();
                            int stackNdx = display.getChildCount() - i;
                            while (true) {
                                if (stackNdx < 0) {
                                    callingPid = callingPid2;
                                    break;
                                }
                                ActivityStack stack = display.getChildAt(stackNdx);
                                ActivityRecord topActivity = stack.getTopActivity();
                                if (hwActivityTaskManagerServiceEx.isActivityRecordVisible(topActivity, lastResumed, isSleeping)) {
                                    if (!list.contains(topActivity.packageName)) {
                                        try {
                                            list.add(topActivity.packageName);
                                        } catch (Throwable th) {
                                            th = th;
                                        }
                                    }
                                    if (!topActivity.fullscreen) {
                                        TaskRecord topTask = stack.topTask();
                                        callingPid = callingPid2;
                                        int recordNdx = topTask.getChildCount() - 2;
                                        while (true) {
                                            if (recordNdx < 0) {
                                                break;
                                            }
                                            ActivityRecord activityRecord = topTask.getChildAt(recordNdx);
                                            if (hwActivityTaskManagerServiceEx.isActivityRecordVisible(activityRecord, lastResumed, isSleeping)) {
                                                if (!list.contains(activityRecord.packageName)) {
                                                    list.add(activityRecord.packageName);
                                                }
                                                if (activityRecord.fullscreen) {
                                                    break;
                                                }
                                                recordNdx--;
                                                hwActivityTaskManagerServiceEx = this;
                                                topTask = topTask;
                                            } else {
                                                break;
                                            }
                                        }
                                    } else {
                                        callingPid = callingPid2;
                                    }
                                } else {
                                    callingPid = callingPid2;
                                    if (!stack.inMultiWindowMode() && topActivity != null) {
                                        break;
                                    }
                                }
                                stackNdx--;
                                hwActivityTaskManagerServiceEx = this;
                                callingPid2 = callingPid;
                            }
                            i2--;
                            i = 1;
                            hwActivityTaskManagerServiceEx = this;
                            callingPid2 = callingPid;
                        }
                        return list;
                    } catch (Throwable th2) {
                        th = th2;
                        throw th;
                    }
                }
            }
        }
        isAllowed = true;
        List<String> list2 = new ArrayList<>();
        if (isAllowed) {
        }
    }

    public boolean setMultiWindowDisabled(boolean disabled) {
        int appId = UserHandle.getAppId(Binder.getCallingUid());
        if (appId == 1000 || appId == 0) {
            ActivityTaskManagerService atms = this.mIAtmsInner.getATMS();
            if (!this.mIsInitMultiWindowDisabledState) {
                this.mIsSupportsFreeformBefore = atms.mSupportsFreeformWindowManagement;
                this.mIsSupportsSplitScreenBefore = atms.mSupportsSplitScreenMultiWindow;
                atms.mSupportsFreeformWindowManagement = this.mIsSupportsFreeformBefore && !this.mIsMultiWindowDisabled;
                atms.mSupportsSplitScreenMultiWindow = this.mIsSupportsSplitScreenBefore && !this.mIsMultiWindowDisabled;
                this.mIsInitMultiWindowDisabledState = true;
                Slog.d(TAG, "setMultiWindowDisabled init state set " + this.mIsMultiWindowDisabled);
                return false;
            } else if (disabled || this.mIsSupportsFreeformBefore || this.mIsSupportsSplitScreenBefore) {
                if (this.mIsSupportsFreeformBefore) {
                    atms.mSupportsFreeformWindowManagement = !disabled;
                } else {
                    Slog.d(TAG, "setMultiWindowDisabled freeform is not supported");
                }
                if (this.mIsSupportsSplitScreenBefore) {
                    atms.mSupportsSplitScreenMultiWindow = !disabled;
                } else {
                    Slog.d(TAG, "setMultiWindowDisabled split-screen is not supported");
                }
                this.mIsMultiWindowDisabled = disabled;
                Slog.d(TAG, "setMultiWindowDisabled set: " + disabled);
                return true;
            } else {
                Slog.d(TAG, "setMultiWindowDisabled freeform and split-screen are not supported");
                return false;
            }
        } else {
            Slog.d(TAG, "setMultiWindowDisabled the caller is not system");
            return false;
        }
    }

    public boolean getMultiWindowDisabled() {
        return this.mIsMultiWindowDisabled;
    }

    private TaskRecordEx buildTaskRecordEx(TaskRecord taskRecord) {
        TaskRecordEx taskRecordEx = new TaskRecordEx();
        taskRecordEx.setTaskRecord(taskRecord);
        return taskRecordEx;
    }

    private ActivityTaskManagerServiceEx buildAtmsEx(ActivityTaskManagerService atms) {
        ActivityTaskManagerServiceEx atmsEx = new ActivityTaskManagerServiceEx();
        atmsEx.setActivityTaskManagerService(atms);
        return atmsEx;
    }

    private DefaultHwPCMultiWindowManager getHwPCMultiWindowManager(ActivityTaskManagerServiceEx atmsEx) {
        return HwPCFactory.getHwPCFactory().getHwPCFactoryImpl().getHwPCMultiWindowManager(atmsEx);
    }

    private boolean isSosPcMode() {
        if (!DESKTOP_ENABLED || !ENABLED_IN_PAD || !HwPCUtilsEx.isPcCastMode()) {
            return false;
        }
        return true;
    }

    public void setCallingPkg(String callingPkg) {
        this.mHwMwm.setCallingPackage(callingPkg);
    }

    public void setAlwaysOnTopOnly(ActivityDisplay display, ActivityStack stack, boolean isNewStack, boolean alwaysOnTop) {
        this.mHwMwm.setAlwaysOnTopOnly(display, stack, isNewStack, alwaysOnTop);
    }

    public boolean isMagicWinExcludeTaskFromRecents(TaskRecord task) {
        if (!HwMwUtils.ENABLED || !task.inHwMagicWindowingMode()) {
            return false;
        }
        DefaultHwMagicWinCombineManager combineManager = HwPartMagicWindowServiceFactory.getInstance().getHwPartMagicWindowServiceFactoryImpl().getHwMagicWinCombineManager();
        TaskRecordEx taskRecordEx = new TaskRecordEx();
        taskRecordEx.setTaskRecord(task);
        String packageName = combineManager.getTaskPackageName(taskRecordEx);
        Set<Integer> splitScreenStackIds = combineManager.getSplitScreenStackIds(packageName, task.userId);
        if (splitScreenStackIds != null && !combineManager.isForegroundTaskIds(combineManager.getForegroundTaskIds(packageName, task.userId), task.taskId)) {
            return splitScreenStackIds.contains(Integer.valueOf(task.getStackId()));
        }
        return false;
    }

    public boolean isMagicWinSkipRemoveFromRecentTasks(TaskRecord addingTask, TaskRecord removingTask) {
        if (!HwMwUtils.ENABLED || !addingTask.inHwMagicWindowingMode() || !removingTask.inHwMagicWindowingMode() || addingTask.affinity == null || !addingTask.affinity.equals(removingTask.affinity)) {
            return false;
        }
        DefaultHwMagicWinCombineManager combineManager = HwPartMagicWindowServiceFactory.getInstance().getHwPartMagicWindowServiceFactoryImpl().getHwMagicWinCombineManager();
        TaskRecordEx taskRecordEx = new TaskRecordEx();
        taskRecordEx.setTaskRecord(addingTask);
        String packageName = combineManager.getTaskPackageName(taskRecordEx);
        if (combineManager.getSplitScreenStackIds(packageName, addingTask.userId) == null) {
            return false;
        }
        int[] foregourndTaskIds = combineManager.getForegroundTaskIds(packageName, addingTask.userId);
        if (!combineManager.isForegroundTaskIds(foregourndTaskIds, addingTask.taskId) || !combineManager.isForegroundTaskIds(foregourndTaskIds, removingTask.taskId)) {
            return false;
        }
        return true;
    }

    public void updateSplitBarPosForIm(int position, int displayId) {
        this.mHwMwm.updateSplitBarPosForIm(position, displayId);
    }

    public void loadPadCastBlackList(String filePath) {
        this.mHwMdm.loadPadCastBlackList(filePath);
    }

    public Rect resizeActivityStack(IBinder token, Rect bounds, boolean isAlwaysOnTop) {
        return this.mHwMdm.resizeActivityStack(token, bounds, isAlwaysOnTop);
    }

    public int getVirtualDisplayId(String castType) {
        return this.mHwMdm.getVirtualDisplayId(castType);
    }

    public boolean isVirtualDisplayId(int displayId, String castType) {
        return this.mHwMdm.isVirtualDisplayId(displayId, castType);
    }

    public boolean computeBounds(ActivityRecord activity, Rect outBounds) {
        return this.mHwMdm.computeBounds(activity, outBounds);
    }

    public void finishRootActivity(ActivityRecord activity) {
        this.mHwMdm.finishRootActivity(activity);
    }

    public void updatePictureInPictureMode(ActivityRecord activity, boolean inPictureInPictureMode) {
        if (this.mHwMdm.getVirtualDisplayId("padCast") != -1 && this.mHwMdm.isMirrorCast("padCast")) {
            int pid = activity.app != null ? activity.app.mPid : 0;
            Bundle bundle = new Bundle();
            TaskRecord task = activity.task;
            if (task != null) {
                bundle.putInt(HwWmConstants.TASK_ID, task.taskId);
            }
            bundle.putInt(ASSOC_PID, pid);
            bundle.putInt("uid", activity.getUid());
            bundle.putString(STRING_INCOMPATIBLE_XML_PKG, activity.packageName);
            bundle.putString("android.intent.extra.REASON", "pipStateChange");
            bundle.putInt("android.intent.extra.user_handle", this.mIAtmsInner.getATMS().mWindowManager.mCurrentUserId);
            Message msg = this.mHwHandler.obtainMessage(24);
            msg.obj = bundle;
            this.mHwHandler.sendMessage(msg);
        }
    }

    public void updateHwFreeformNotificationState(int displayId, String state) {
        if (this.mHwMwm.hasPendingShowStack(displayId)) {
            Bundle bundle = new Bundle();
            bundle.putInt(KEY_DISPLAY_ID, displayId);
            bundle.putString("state", state);
            if (ActivityTaskManagerDebugConfig.DEBUG_HWFREEFORM) {
                Slog.i(TAG, "updateFreeformNotificationState displayId:" + displayId + ", state:" + state);
            }
            bundle.putString("android.intent.extra.REASON", "freeformNotificationChange");
            bundle.putInt("android.intent.extra.user_handle", this.mIAtmsInner.getATMS().mWindowManager.mCurrentUserId);
            Message msg = this.mHwHandler.obtainMessage(24);
            msg.obj = bundle;
            this.mHwHandler.sendMessage(msg);
            if ("ready".equals(state)) {
                this.mHwMwm.clearPendingShowStack(displayId);
            }
        }
    }

    public boolean moveStacksToDisplay(int fromDisplayId, int toDisplayId, boolean isOnlyFocus) {
        return this.mHwMdm.moveStacksToDisplay(fromDisplayId, toDisplayId, isOnlyFocus);
    }

    public void stackCreated(ActivityStack stack, ActivityRecord root) {
        this.mHwMwm.stackCreated(stack, root);
    }

    public void notifyFullScreenStateChange(int displayId, boolean isRequestFullScreen) {
        this.mHwMdm.notifyFullScreenStateChange(displayId, isRequestFullScreen);
    }

    public void notifyHoldScreenStateChange(String tag, int lockHash, int ownerUid, int ownerPid, String state) {
        this.mHwMdm.notifyHoldScreenStateChange(tag, lockHash, ownerUid, ownerPid, state);
    }

    public void notifyDisplayStacksEmpty(int displayId) {
        this.mHwMdm.notifyDisplayStacksEmpty(displayId);
    }

    public void notifySecureStateChange(int displayId, boolean isSecure) {
        this.mHwMdm.notifySecureStateChange(displayId, isSecure);
    }

    public Bitmap getPadCastWallpaperBitmap() {
        return this.mHwMdm.getPadCastWallpaperBitmap();
    }

    public boolean isPadCastStack(ActivityStack stack) {
        return this.mHwMwm.isPadCastStack(stack);
    }

    public boolean isDisplayHoldScreen(int displayId) {
        return this.mHwMdm.isDisplayHoldScreen(displayId);
    }

    public boolean isPadCastMaxSizeEnable() {
        return this.mHwMdm.isPadCastMaxSizeEnable();
    }

    public boolean isMirrorCast(String castType) {
        return this.mHwMdm.isMirrorCast(castType);
    }

    public void setStackWindowingMode(IBinder token, int windowingMode, Rect bounds) {
        this.mHwMdm.setStackWindowingMode(token, windowingMode, bounds);
    }

    public boolean isNerverUseSizeCompateMode(String packageName) {
        int appAttr;
        if (NERVER_USE_COMPAT_MODE_APPS.contains(packageName)) {
            return true;
        }
        if (EMERGENCY_PACKAGENAME.equals(packageName) && isSosPcMode()) {
            return true;
        }
        if (!HwMultiWindowManager.IS_HW_MULTIWINDOW_SUPPORTED || TextUtils.isEmpty(packageName) || (appAttr = AppTypeRecoManager.getInstance().getAppAttribute(packageName)) == -1 || (appAttr & 65536) != 65536) {
            return false;
        }
        return true;
    }

    public Bundle getHwMultiWindowState() {
        return this.mHwMwm.getHwMultiWindowState();
    }

    public boolean isPhoneLandscape(DisplayContent displayContent) {
        return this.mHwMwm.isPhoneLandscape(displayContent);
    }

    public void setForegroundFreeFormNum(int num) {
        Binder.getCallingPid();
        int appId = UserHandle.getAppId(Binder.getCallingUid());
        if (!(appId == 1000 || appId == 0)) {
            this.mIAtmsInner.getATMS().enforceCallerIsRecentsOrHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", "setForegroundFreeFormNum()");
        }
        long ident = Binder.clearCallingIdentity();
        try {
            this.mHwMwm.setForegroundFreeFormNum(num);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public Map<String, Boolean> getAppUserAwarenessState(int displayId, List<String> packageNames) {
        if (!isCallerHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", Binder.getCallingPid(), Binder.getCallingUid())) {
            return new HashMap();
        }
        long ident = Binder.clearCallingIdentity();
        try {
            return this.mHwMwm.getAppUserAwarenessState(displayId, packageNames);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public boolean isStatusBarPermenantlyShowing() {
        return this.mHwMwm.isStatusBarPermenantlyShowing();
    }

    public void adjustHwFreeformPosIfNeed(DisplayContent displayContent, boolean isStatusShowing) {
        this.mHwMwm.adjustHwFreeformPosIfNeed(displayContent, isStatusShowing);
    }

    public boolean blockSwipeFromTop(MotionEvent event, DisplayContent display) {
        return this.mHwMwm.blockSwipeFromTop(event, display);
    }

    public void setHwWinCornerRaduis(WindowState win, SurfaceControl control) {
        this.mHwMwm.setHwWinCornerRaduis(win, control);
    }

    public float getHwMultiWinCornerRadius(int windowingMode) {
        return this.mHwMwm.getHwMultiWinCornerRadius(windowingMode);
    }

    public List<ActivityManager.RecentTaskInfo> getFilteredTasks(int userId, int displayId, String packageName, int[] windowingModes, boolean isIgnoreVisible, int maxNum) {
        Set<Integer> modeSet;
        if (!isCallerHasPermission("android.permission.REAL_GET_TASKS", Binder.getCallingPid(), Binder.getCallingUid())) {
            return null;
        }
        List<ActivityManager.RecentTaskInfo> list = new ArrayList<>();
        if (windowingModes == null || windowingModes.length <= 0) {
            modeSet = null;
        } else {
            Set<Integer> modeSet2 = new HashSet<>(windowingModes.length);
            for (int mode : windowingModes) {
                modeSet2.add(Integer.valueOf(mode));
            }
            modeSet = modeSet2;
        }
        synchronized (this.mIAtmsInner.getATMS().getGlobalLock()) {
            try {
                ArrayList<TaskRecord> tmpSortedList = new ArrayList<>(this.mIAtmsInner.getATMS().getRecentRawTasks());
                if (IS_HW_MULTIWINDOW_SUPPORTED) {
                    adjustTaskForHwMultiWindow(tmpSortedList);
                }
                int size = 0;
                Iterator<TaskRecord> it = tmpSortedList.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    TaskRecord tr = it.next();
                    if (maxNum > 0 && size >= maxNum) {
                        break;
                    } else if (tr.realActivity != null) {
                        if (!isExcludeFromRecents(tr)) {
                            if (userId != -1) {
                                try {
                                    if (!this.mIAtmsInner.getStackSupervisor().isCurrentProfileLocked(tr.userId)) {
                                    }
                                } catch (Throwable th) {
                                    th = th;
                                    throw th;
                                }
                            }
                            if (displayId == -1 || tr.getStack() == null || tr.getStack().mDisplayId == displayId) {
                                if (!TextUtils.isEmpty(packageName)) {
                                    if (!packageName.equals(tr.realActivity.getPackageName())) {
                                    }
                                }
                                if (modeSet == null || modeSet.contains(Integer.valueOf(tr.getWindowingMode()))) {
                                    if (!isIgnoreVisible || !tr.isVisible()) {
                                        list.add(this.mIAtmsInner.getATMS().getRecentTasks().createRecentTaskInfo(tr));
                                        size++;
                                    }
                                }
                            }
                        }
                    }
                }
                return list;
            } catch (Throwable th2) {
                th = th2;
                throw th;
            }
        }
    }

    private void adjustTaskForHwMultiWindow(ArrayList<TaskRecord> taskList) {
        if (taskList != null && !taskList.isEmpty()) {
            int i = 0;
            while (i < taskList.size()) {
                if (taskList.get(i).inHwSplitScreenWindowingMode() && adjustTaskGroupForMultiWindow(i, taskList)) {
                    i++;
                }
                i++;
            }
        }
    }

    private boolean adjustTaskGroupForMultiWindow(int index, ArrayList<TaskRecord> taskList) {
        int[] combinedGroupIds;
        TaskRecord tr = taskList.get(index);
        if (tr.mStack == null || (combinedGroupIds = getCombinedSplitScreenTaskIds(tr.mStack)) == null) {
            return false;
        }
        int combinedTaskId = tr.taskId == combinedGroupIds[0] ? combinedGroupIds[combinedGroupIds.length - 1] : combinedGroupIds[0];
        int combinedTaskIndex = -1;
        int i = index + 1;
        while (true) {
            if (i >= taskList.size()) {
                break;
            } else if (taskList.get(i).taskId == combinedTaskId) {
                combinedTaskIndex = i;
                break;
            } else {
                i++;
            }
        }
        if (combinedTaskIndex == -1) {
            return false;
        }
        taskList.add(index + 1, taskList.remove(combinedTaskIndex));
        return true;
    }

    public void removeTasks(int[] taskIds) {
        if (!(taskIds == null || taskIds.length == 0 || !isCallerHasPermission("android.permission.REMOVE_TASKS", Binder.getCallingPid(), Binder.getCallingUid()))) {
            synchronized (this.mIAtmsInner.getATMS().getGlobalLock()) {
                for (int taskId : taskIds) {
                    if (taskId > -1) {
                        TaskRecord tr = this.mIAtmsInner.getATMS().mRootActivityContainer.anyTaskForId(taskId, 1);
                        if (tr != null) {
                            if (!this.mHwMwm.isPadCastStack(tr.getStack())) {
                                this.mIAtmsInner.getATMS().getRecentTasks().remove(tr);
                            }
                        }
                        this.mIAtmsInner.getATMS().removeTask(taskId);
                    }
                }
            }
        }
    }

    private boolean isExcludeFromRecents(TaskRecord tr) {
        if (tr.intent != null && (tr.intent.getFlags() & 8388608) != 0) {
            return true;
        }
        if (!tr.autoRemoveRecents || tr.getTopActivity() != null) {
            return false;
        }
        return true;
    }

    public void toggleFreeformWindowingMode(IBinder appToken, String packageName) {
        ActivityRecord activityRecord;
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mIAtmsInner.getATMS().getGlobalLock()) {
                IBinder token = null;
                if (appToken != null) {
                    token = appToken;
                    activityRecord = ActivityRecord.forTokenLocked(token);
                } else {
                    activityRecord = this.mIAtmsInner.getLastResumedActivityRecord();
                    if (activityRecord != null && !TextUtils.isEmpty(packageName) && packageName.equals(activityRecord.packageName)) {
                        token = activityRecord.appToken;
                    }
                }
                if (token == null || activityRecord == null || !activityRecord.inHwFreeFormWindowingMode()) {
                    Log.e(TAG, "toggleFreeformWindowingMode activityRecord is null ");
                } else {
                    this.mIAtmsInner.getATMS().toggleFreeformWindowingMode(token);
                }
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public boolean setStackScale(int taskId, float scale) {
        if (taskId <= -1) {
            Slog.e(TAG, "setStackScale taskId is invalid");
            return false;
        } else if (!isCallerHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", Binder.getCallingPid(), Binder.getCallingUid())) {
            Slog.e(TAG, "setStackScale no right");
            return false;
        } else {
            long ident = Binder.clearCallingIdentity();
            synchronized (this.mIAtmsInner.getATMS().getGlobalLock()) {
                try {
                    TaskRecord taskRecord = this.mIAtmsInner.getATMS().mRootActivityContainer.anyTaskForId(taskId, 1);
                    if (taskRecord == null) {
                        Slog.e(TAG, "HwActivityTaskManagerServiceEx setStackScale get stack is null taskId:" + taskId);
                        return false;
                    }
                    ActivityRecord activityRecord = taskRecord.topRunningActivityLocked();
                    if (activityRecord == null || activityRecord.getActivityStack() == null || activityRecord.getActivityStack().getTaskStack() == null) {
                        Slog.e(TAG, "HwActivityTaskManagerServiceEx setStackScale: ts is null");
                        Binder.restoreCallingIdentity(ident);
                        return false;
                    }
                    float[] scaleRange = getScaleRange(activityRecord.getActivityStack());
                    if (scale < scaleRange[0]) {
                        scale = scaleRange[0];
                    } else if (scale > scaleRange[1]) {
                        scale = scaleRange[1];
                    }
                    activityRecord.getActivityStack().getTaskStack().mHwStackScale = scale;
                    this.mIAtmsInner.getATMS().mWindowManager.requestTraversal();
                    Binder.restoreCallingIdentity(ident);
                    return true;
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }
    }

    public Rect handleStackFromOneStep(ActivityRecord activity, ActivityStack stack, ActivityRecord sourceRecord) {
        return this.mHwMwm.handleStackFromOneStep(activity, stack, sourceRecord);
    }

    public boolean removeTask(int taskId, IBinder token, String packageName, boolean isRemoveFromRecents, String reason) {
        ActivityRecord lastResumed;
        Flog.bdReport(991311113, ASSOC_PKGNAME, packageName);
        if (taskId <= -1) {
            if (token != null) {
                ActivityRecord ar = ActivityRecord.forTokenLocked(token);
                if (!(ar == null || ar.task == null)) {
                    if ("close-freeform".equals(reason)) {
                        recordHwFreeFormBounds(ar.task, false);
                    }
                    return removeTaskLocked(ar.task.taskId, isRemoveFromRecents, ar.task);
                }
            } else if (!TextUtils.isEmpty(packageName) && (lastResumed = this.mIAtmsInner.getLastResumedActivityRecord()) != null && packageName.equals(lastResumed.packageName) && lastResumed.task != null) {
                return removeTaskLocked(lastResumed.task.taskId, isRemoveFromRecents, lastResumed.task);
            }
            return false;
        } else if (!isCallerHasPermission("android.permission.REMOVE_TASKS", Binder.getCallingPid(), Binder.getCallingUid())) {
            return false;
        } else {
            synchronized (this.mIAtmsInner.getATMS().getGlobalLock()) {
                TaskRecord tr = null;
                if ("iAware".equals(reason) || isRemoveFromRecents) {
                    tr = this.mIAtmsInner.getATMS().mRootActivityContainer.anyTaskForId(taskId, 1);
                }
                if (!this.mIsInRecentState || tr == null || !"iAware".equals(reason) || !tr.inHwFreeFormWindowingMode() || tr.inHwPCMultiStackWindowingMode()) {
                    return removeTaskLocked(taskId, isRemoveFromRecents, tr);
                }
                Slog.i(TAG, "removeTask from iAware, skipping FreeForm, taskId=" + taskId);
                return false;
            }
        }
    }

    private boolean removeTaskLocked(int taskId, boolean isRemoveFromRecents, TaskRecord tr) {
        boolean re;
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mIAtmsInner.getATMS().getGlobalLock()) {
                if (isRemoveFromRecents && tr != null) {
                    if (tr.inHwFreeFormWindowingMode()) {
                        this.mSkipAddToBallTaskSet.add(Integer.valueOf(taskId));
                    }
                }
                re = this.mIAtmsInner.getATMS().removeTask(taskId);
                this.mSkipAddToBallTaskSet.remove(Integer.valueOf(taskId));
            }
            return re;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void dispatchFreeformBallLifeState(List<TaskRecord> tasks, String state) {
        if (!(tasks == null || tasks.isEmpty())) {
            ActivityDisplay display = null;
            List<ActivityManager.RecentTaskInfo> recentTaskInfos = new ArrayList<>(tasks.size());
            for (TaskRecord task : tasks) {
                if (task != null && this.mIAtmsInner.getStackSupervisor().isCurrentProfileLocked(task.userId)) {
                    if (!this.mHwMwm.isPadCastTask(task)) {
                        if (!(task.realActivity == null || task.getWindowConfiguration() == null)) {
                            if ("add".equals(state)) {
                                recordHwFreeFormBounds(task, true);
                            } else if ("remove".equals(state)) {
                                removeHwFreeFormBoundsRecordById(task.taskId);
                            }
                        }
                        if ((!"add".equals(state) || !isSkipAddToFreeformBall(task)) && !task.inHwPCMultiStackWindowingMode()) {
                            ActivityManager.RecentTaskInfo rti = this.mIAtmsInner.getATMS().getRecentTasks().createRecentTaskInfo(task);
                            if (display == null && task.mStack != null) {
                                display = task.mStack.getDisplay();
                            }
                            recentTaskInfos.add(rti);
                            if (ActivityTaskManagerDebugConfig.DEBUG_HWFREEFORM) {
                                Slog.i(TAG, "dispatchFreeformBall rti:" + rti);
                            }
                        }
                    } else if (task.getChildCount() != 0 || "remove".equals(state)) {
                        recentTaskInfos.add(this.mIAtmsInner.getATMS().getRecentTasks().createRecentTaskInfo(task));
                    }
                }
            }
            if (!recentTaskInfos.isEmpty()) {
                if ("add".equals(state) && !this.mHwMwm.hasVisibleHwMultiStack(display)) {
                    this.mHwMwm.setColorMgrInfo(false);
                }
                if (ActivityTaskManagerDebugConfig.DEBUG_HWFREEFORM) {
                    Slog.i(TAG, "dispatchFreeformBall state:" + state, new Exception());
                }
                Bundle bundle = new Bundle();
                bundle.putString("state", state);
                bundle.putParcelableList("taskInfos", recentTaskInfos);
                bundle.putString("android.intent.extra.REASON", "freeformBallLifeState");
                bundle.putInt("android.intent.extra.user_handle", this.mIAtmsInner.getATMS().mWindowManager.mCurrentUserId);
                Message msg = this.mHwHandler.obtainMessage(24);
                msg.obj = bundle;
                this.mHwHandler.sendMessage(msg);
            }
        }
    }

    private boolean isSkipAddToFreeformBall(TaskRecord task) {
        if ((isExcludeFromRecents(task) && !this.mHwMwm.isPadCastTask(task)) || isTopAppLockTask(task) || this.mSkipAddToBallTaskSet.contains(Integer.valueOf(task.taskId))) {
            return true;
        }
        ActivityStackSupervisor stackSupervisor = this.mIAtmsInner.getStackSupervisor();
        if (stackSupervisor == null || stackSupervisor.mRecentTasks == null || stackSupervisor.mRecentTasks.getTask(task.taskId) != null) {
            return false;
        }
        return true;
    }

    public void dispatchFocusStackChange(int currentUser, int displayId, ActivityStack to, ActivityStack from) {
        ActivityRecord topActivityRecord;
        if (to != null && (topActivityRecord = to.topRunningActivityLocked()) != null && topActivityRecord.info != null) {
            Bundle bundle = new Bundle();
            bundle.putInt(KEY_USER_ID, currentUser);
            bundle.putInt(KEY_DISPLAY_ID, displayId);
            bundle.putParcelable("comp", topActivityRecord.info.getComponentName());
            bundle.putString("android.intent.extra.REASON", "focusStackChange");
            bundle.putInt("android.intent.extra.user_handle", this.mIAtmsInner.getATMS().mWindowManager.mCurrentUserId);
            Message msg = this.mHwHandler.obtainMessage(24);
            msg.obj = bundle;
            this.mHwHandler.sendMessage(msg);
        }
    }

    private boolean isTopAppLockTask(TaskRecord task) {
        ActivityRecord topActivityRecord;
        if (task == null || (topActivityRecord = task.getTopActivity()) == null) {
            return false;
        }
        return ActivityStartInterceptorBridge.isAppLockActivity(topActivityRecord.shortComponentName);
    }

    private boolean isCallerHasPermission(String permission, int callingPid, int callingUid) {
        this.mIAtmsInner.getATMS();
        boolean isAllowed = ActivityTaskManagerService.checkPermission(permission, callingPid, callingUid) == 0;
        if (!isAllowed) {
            Slog.d(TAG, "permission denied for removeTask, callingPid:" + callingPid + ", callingUid:" + callingUid + ", requires: " + permission);
        }
        return isAllowed;
    }

    public int[] startActivitiesFromRecents(int[] taskIds, List<Bundle> bOptions, boolean divideSplitScreen, int flag) {
        TaskRecord taskRecord;
        Rect hwFreeFormTaskRect;
        Rect hwFreeFormTaskRect2;
        float stackScale;
        boolean z;
        ActivityRecord top;
        HwActivityTaskManagerServiceEx hwActivityTaskManagerServiceEx = this;
        int[] iArr = taskIds;
        List<Bundle> list = bOptions;
        boolean z2 = divideSplitScreen;
        int i = 0;
        if (iArr == null || list == null || iArr.length != bOptions.size()) {
            return new int[0];
        }
        int[] result = new int[iArr.length];
        synchronized (hwActivityTaskManagerServiceEx.mIAtmsInner.getATMS().getGlobalLock()) {
            hwActivityTaskManagerServiceEx.mIAtmsInner.getATMS().mStartFromSelector = flag;
            int i2 = 0;
            while (i2 < iArr.length) {
                int taskId = iArr[i2];
                Bundle bundleOptions = list.get(i2);
                Slog.i(TAG, "startActivitiesFromRecents taskId:" + taskId + " divide:" + z2 + " flag:" + flag);
                ActivityStackSupervisor stackSupervisor = hwActivityTaskManagerServiceEx.mIAtmsInner.getStackSupervisor();
                if (stackSupervisor != null) {
                    taskRecord = stackSupervisor.mRootActivityContainer.anyTaskForId(taskId, i);
                } else {
                    taskRecord = null;
                }
                TaskRecord task = taskRecord;
                if (task != null) {
                    try {
                        if (hwActivityTaskManagerServiceEx.mHwMwm.isPadCastStack(task.getStack())) {
                            bundleOptions.putInt("android.activity.windowingMode", i);
                        }
                    } catch (BadParcelableException e) {
                        Slog.e(TAG, "start activities form recent badparcel err!");
                    } catch (Exception e2) {
                        Slog.e(TAG, "start activities form recent bad err!");
                    }
                }
                if (!(task == null || bundleOptions.getInt("android.activity.windowingMode") != 102 || (top = task.getTopActivity()) == null || top.appInfo == null || !"com.huawei.camera".equals(top.appInfo.packageName))) {
                    bundleOptions.putInt("android.activity.windowingMode", i);
                }
                SafeActivityOptions safeOptions = SafeActivityOptions.fromBundle(bundleOptions);
                ActivityOptions activityOptions = (stackSupervisor == null || safeOptions == null) ? null : safeOptions.getOptions(stackSupervisor);
                boolean isInStacks = true;
                if (!(task != null || stackSupervisor == null || stackSupervisor.mRecentTasks == null)) {
                    task = stackSupervisor.mRecentTasks.getTask(taskId);
                    isInStacks = false;
                }
                if (z2) {
                    if (activityOptions == null) {
                        Slog.w(TAG, "startActivitiesFromRecents canceled because of null Options. taskId=" + taskId);
                        result[i2] = -96;
                        i2++;
                        hwActivityTaskManagerServiceEx = this;
                        iArr = taskIds;
                        list = bOptions;
                        z2 = divideSplitScreen;
                        i = 0;
                    } else {
                        if (activityOptions.getLaunchWindowingMode() != 102) {
                            hwActivityTaskManagerServiceEx.mIAtmsInner.getATMS().mStartFromSelector = 0;
                        }
                        ActivityStack splitStack = task != null ? task.getStack() : null;
                        if (splitStack != null && splitStack.inHwSplitScreenWindowingMode()) {
                            hwActivityTaskManagerServiceEx.mHwMwm.moveStackToFrontEx(activityOptions, splitStack, null, null, null);
                        }
                    }
                }
                boolean isInScaleMode = hwActivityTaskManagerServiceEx.mIAtmsInner.getATMS().mWindowManager.getLazyMode() != 0;
                if (HwFoldScreenState.isFoldScreenDevice()) {
                    int displayMode = ((HwFoldScreenManagerInternal) LocalServices.getService(HwFoldScreenManagerInternal.class)).getDisplayMode();
                    if (!isInScaleMode) {
                        hwFreeFormTaskRect = null;
                        if (displayMode != 3) {
                            z = false;
                            isInScaleMode = z;
                        }
                    } else {
                        hwFreeFormTaskRect = null;
                    }
                    z = true;
                    isInScaleMode = z;
                } else {
                    hwFreeFormTaskRect = null;
                }
                float stackScale2 = -1.0f;
                if (activityOptions != null) {
                    Rect hwFreeFormTaskRect3 = activityOptions.getLaunchBounds();
                    float stackScale3 = activityOptions.getStackScale();
                    if (isInScaleMode) {
                        stackScale = stackScale3;
                        activityOptions.setLaunchWindowingMode(1);
                    } else {
                        stackScale = stackScale3;
                    }
                    hwFreeFormTaskRect = hwFreeFormTaskRect3;
                    stackScale2 = stackScale;
                }
                if (hwFreeFormTaskRect == null) {
                    hwFreeFormTaskRect2 = new Rect();
                } else {
                    hwFreeFormTaskRect2 = hwFreeFormTaskRect;
                }
                if (!(isInStacks || task == null || task.realActivity == null)) {
                    if (hwActivityTaskManagerServiceEx.isFreeFormOrUndefinedWinModeOptions(activityOptions) && hwActivityTaskManagerServiceEx.hasHwFreeFormTaskBoundsRecordById(task.taskId)) {
                        activityOptions = hwActivityTaskManagerServiceEx.updateToLastHwFreeFormOptions(activityOptions, task.taskId, hwFreeFormTaskRect2);
                        if (stackScale2 < 0.0f && activityOptions != null) {
                            stackScale2 = activityOptions.getStackScale();
                        }
                    }
                    if (hwActivityTaskManagerServiceEx.mIAtmsInner.getATMS().mRootActivityContainer.getHwRootActivityContainerEx().isAppInLockList(task.realActivity.getPackageName(), task.userId) || isInScaleMode) {
                        List<TaskRecord> removeTasks = new ArrayList<>(1);
                        removeTasks.add(task);
                        hwActivityTaskManagerServiceEx.dispatchFreeformBallLifeState(removeTasks, "remove");
                    }
                    stackScale2 = stackScale2;
                }
                result[i2] = hwActivityTaskManagerServiceEx.mIAtmsInner.getATMS().startActivityFromRecents(taskId, activityOptions != null ? activityOptions.toBundle() : null);
                ActivityStack activityStack = task != null ? task.getStack() : null;
                hwActivityTaskManagerServiceEx.updateToLastHwFreeFormBounds(activityStack, hwFreeFormTaskRect2, stackScale2);
                hwActivityTaskManagerServiceEx.updateDragFreeFormPos(activityStack);
                hwActivityTaskManagerServiceEx.checkBoundsFromSelctor(activityOptions, activityStack);
                hwActivityTaskManagerServiceEx.mIAtmsInner.getATMS().mStartFromSelector = 0;
                i2++;
                hwActivityTaskManagerServiceEx = this;
                iArr = taskIds;
                list = bOptions;
                z2 = divideSplitScreen;
                i = 0;
            }
        }
        return result;
    }

    private ActivityOptions updateToLastHwFreeFormOptions(ActivityOptions options, int taskId, Rect bounds) {
        ActivityOptions newOptions = ActivityOptions.makeBasic();
        if (options != null) {
            newOptions.update(options);
        }
        newOptions.setLaunchWindowingMode(102);
        if (bounds == null || !bounds.isEmpty()) {
            return newOptions;
        }
        float stackScale = getReusableHwFreeFormBoundsById(taskId, bounds);
        if (!bounds.isEmpty()) {
            newOptions.setLaunchBounds(new Rect(bounds));
            newOptions.setStackScale(stackScale);
        }
        return newOptions;
    }

    private boolean isFreeFormOrUndefinedWinModeOptions(ActivityOptions options) {
        return options == null || options.getLaunchWindowingMode() == 0 || options.getLaunchWindowingMode() == 102;
    }

    private void updateToLastHwFreeFormBounds(ActivityStack activityStack, Rect lastHwFreeFormTaskRect, float stackScale) {
        if (activityStack != null && lastHwFreeFormTaskRect != null && !lastHwFreeFormTaskRect.isEmpty() && activityStack.inHwFreeFormWindowingMode()) {
            if (stackScale > 0.0f && activityStack.getTaskStack() != null) {
                activityStack.getTaskStack().mHwStackScale = stackScale;
            }
            if (!lastHwFreeFormTaskRect.equals(activityStack.getBounds())) {
                activityStack.resize(lastHwFreeFormTaskRect, (Rect) null, (Rect) null);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeTasksIfNeeded(String packageName, int userId, int displayId) {
        List<ActivityManager.RecentTaskInfo> tasks = getFilteredTasks(userId, displayId, packageName, null, false, -1);
        if (tasks != null && tasks.size() > 20) {
            int[] taskIds = new int[(tasks.size() - 20)];
            int index = 0;
            int i = tasks.size() - 1;
            while (i >= 20) {
                int taskId = tasks.get(i).taskId;
                taskIds[index] = taskId;
                Slog.i(TAG, "task number exceeds the limit, remove task. packageName=" + packageName + " taskId=" + taskId);
                i += -1;
                index++;
            }
            removeTasks(taskIds);
        }
    }

    private void checkBoundsFromSelctor(ActivityOptions activityOptions, ActivityStack activityStack) {
        if (this.mIAtmsInner.getATMS().mStartFromSelector == 1 && activityOptions != null && activityStack != null) {
            Rect launchBounds = activityOptions.getLaunchBounds();
            Rect stackbounds = activityStack.getBounds();
            if (launchBounds == null || launchBounds.isEmpty() || stackbounds == null || stackbounds.isEmpty()) {
                Slog.w(TAG, "change bounds failed, cause bounds is null!");
            } else if (!launchBounds.equals(stackbounds)) {
                activityStack.resize(launchBounds, (Rect) null, (Rect) null);
            }
        }
    }

    public void oneStepHwMultiWindowBdReport(ActivityRecord startActivity, int windowMode, ActivityOptions options) {
        this.mHwMwm.oneStepHwMultiWindowBdReport(startActivity, windowMode, options);
    }

    public void reportAppWindowVisibleOrGone(ActivityRecord record) {
        this.mHwMwm.reportAppWindowVisibleOrGone(record);
    }

    public void reportAppWindowMode(int appEventType, ActivityRecord activityRecord, int windowMode, String reason) {
        this.mHwMwm.reportAppWindowMode(appEventType, activityRecord, windowMode, reason);
    }

    public void moveTaskToFrontForMultiDisplay(int taskId) {
        if (HwActivityTaskManager.isPCMultiCastMode()) {
            this.mHwMdm.moveTaskToFrontForMultiDisplay(taskId);
        }
    }

    public void moveTaskBackwardsForMultiDisplay(int taskId) {
        if (HwActivityTaskManager.isPCMultiCastMode()) {
            this.mHwMdm.moveTaskBackwardsForMultiDisplay(taskId);
        }
    }

    public void setFocusedTaskForMultiDisplay(int taskId) {
        if (HwActivityTaskManager.isPCMultiCastMode()) {
            this.mHwMdm.setFocusedTaskForMultiDisplay(taskId);
        }
    }

    public void hwResizeTaskForMultiDisplay(int taskId, Rect bounds) {
        if (HwActivityTaskManager.isPCMultiCastMode()) {
            this.mHwMdm.hwResizeTaskForMultiDisplay(taskId, bounds);
        }
    }

    @Deprecated
    public void setPCFullSize(int fullWidth, int fullHeight, int phoneOrientation) {
        if (HwActivityTaskManager.isPCMultiCastMode()) {
            this.mHwMdm.setPCFullSize(fullWidth, fullHeight, phoneOrientation);
        }
    }

    @Deprecated
    public void setPCVirtualSize(int virtualWidth, int virtualHeight, int phoneOrientation) {
        if (HwActivityTaskManager.isPCMultiCastMode()) {
            this.mHwMdm.setPCVirtualSize(virtualWidth, virtualHeight, phoneOrientation);
        }
    }

    public void setPCMultiCastMode(boolean isPCMultiCastMode) {
        this.mHwMdm.setPCMultiCastMode(isPCMultiCastMode);
    }

    public void setCurOrientation(int curOrientation) {
        if (HwActivityTaskManager.isPCMultiCastMode()) {
            this.mHwMdm.setCurOrientation(curOrientation);
        }
    }

    @Deprecated
    public int getPCVirtualWidth() {
        if (HwActivityTaskManager.isPCMultiCastMode()) {
            return this.mHwMdm.getPCVirtualWidth();
        }
        return -1;
    }

    @Deprecated
    public int getPCVirtualHeight() {
        if (HwActivityTaskManager.isPCMultiCastMode()) {
            return this.mHwMdm.getPCVirtualHeight();
        }
        return -1;
    }

    @Deprecated
    public int getPCFullWidth() {
        if (HwActivityTaskManager.isPCMultiCastMode()) {
            return this.mHwMdm.getPCFullWidth();
        }
        return -1;
    }

    @Deprecated
    public int getPCFullHeight() {
        if (HwActivityTaskManager.isPCMultiCastMode()) {
            return this.mHwMdm.getPCFullHeight();
        }
        return -1;
    }

    public void registerMultiDisplayMessenger(Messenger messenger) {
        this.mHwMdm.registerMultiDisplayMessenger(messenger);
    }

    public void unregisterMultiDisplayMessenger(Messenger messenger) {
        this.mHwMdm.unregisterMultiDisplayMessenger(messenger);
    }

    public void onWindowModeChange(int taskId, Rect rect) {
        if (HwActivityTaskManager.isPCMultiCastMode()) {
            this.mHwMdm.onWindowModeChange(taskId, rect);
        }
    }

    public void hwTogglePCFloatWindow(int taskId) {
        if (HwActivityTaskManager.isPCMultiCastMode()) {
            this.mHwMdm.hwTogglePCFloatWindow(taskId);
        }
    }

    public void hwTogglePhoneFullScreen(int taskId) {
        if (HwActivityTaskManager.isPCMultiCastMode() || taskId == -1) {
            this.mHwMdm.hwTogglePhoneFullScreen(taskId);
        }
    }

    public List<Bundle> getTaskList() {
        if (HwActivityTaskManager.isPCMultiCastMode()) {
            return this.mHwMdm.getTaskList();
        }
        return null;
    }

    public int getCurTopFullScreenTaskState() {
        if (HwActivityTaskManager.isPCMultiCastMode()) {
            return this.mHwMdm.getCurTopFullScreenTaskState();
        }
        return -1;
    }

    public int getCurPCWindowAreaNum() {
        if (HwActivityTaskManager.isPCMultiCastMode()) {
            return this.mHwMdm.getCurPCWindowAreaNum();
        }
        return -1;
    }

    public List<Bundle> getLastRencentTaskList() {
        return this.mHwMdm.getLastRencentTaskList();
    }

    public int retrievePCMultiWinConfig(String configXML) {
        return this.mHwMdm.retrievePCMultiWinConfig(configXML);
    }

    public void setPcSize(int pcWidth, int pcHeight) {
        if (HwActivityTaskManager.isPCMultiCastMode()) {
            this.mHwMdm.setPcSize(pcWidth, pcHeight);
        }
    }

    public int getPcWidth() {
        if (HwActivityTaskManager.isPCMultiCastMode()) {
            return this.mHwMdm.getPcWidth();
        }
        return 0;
    }

    public int getPcHeight() {
        if (HwActivityTaskManager.isPCMultiCastMode()) {
            return this.mHwMdm.getPcHeight();
        }
        return 0;
    }

    public void setMultiDisplayParamsWithType(int type, Bundle bundle) {
        this.mHwMdm.setMultiDisplayParamsWithType(type, bundle);
    }

    public Rect getLocalLayerRectForMultiDisplay() {
        if (HwActivityTaskManager.isPCMultiCastMode()) {
            return this.mHwMdm.getLocalLayerRectForMultiDisplay();
        }
        return new Rect();
    }

    public Rect getLocalDisplayRectForMultiDisplay() {
        if (HwActivityTaskManager.isPCMultiCastMode()) {
            return this.mHwMdm.getLocalDisplayRectForMultiDisplay();
        }
        return new Rect();
    }

    public Rect getVirtualLayerRectForMultiDisplay() {
        if (HwActivityTaskManager.isPCMultiCastMode()) {
            return this.mHwMdm.getVirtualLayerRectForMultiDisplay();
        }
        return new Rect();
    }

    public Rect getVirtualDisplayRectForMultiDisplay() {
        if (HwActivityTaskManager.isPCMultiCastMode()) {
            return this.mHwMdm.getVirtualDisplayRectForMultiDisplay();
        }
        return new Rect();
    }

    public void onTaskStackChangedForMultiDisplay() {
        if (HwActivityTaskManager.isPCMultiCastMode()) {
            this.mHwMdm.onTaskStackChangedForMultiDisplay();
        }
    }

    public void adjustProcessGlobalConfigLocked(TaskRecord tr, Rect rect, int windowMode) {
        if (HwActivityTaskManager.isPCMultiCastMode()) {
            this.mHwMdm.adjustProcessGlobalConfigLocked(tr, rect, windowMode);
        }
    }

    public void updateTaskByRequestedOrientationForPCCast(int taskId, int requestedOrientation) {
        if (HwActivityTaskManager.isPCMultiCastMode()) {
            this.mHwMdm.updateTaskByRequestedOrientationForPCCast(taskId, requestedOrientation);
        }
    }

    public float getStackScale(int taskId) {
        return this.mHwMwm.getStackScale(taskId);
    }

    public void adjustActivityOptionsForPCCast(ActivityRecord ar, ActivityOptions options) {
        if (HwActivityTaskManager.isPCMultiCastMode() && options != null) {
            if (WindowConfiguration.isHwPCFreeFormWindowingMode(options.getLaunchWindowingMode())) {
                this.mHwMdm.adjustActivityOptionsForPCCast(ar, options);
            }
            if ((ar.intent.getHwFlags() & 16777216) != 0) {
                this.mHwMdm.adjustOverlayActivityOptionsForPCCast(ar, options);
            }
        }
    }

    public void handleActivityResumedForPCCast(ActivityRecord ar) {
        if (HwActivityTaskManager.isPCMultiCastMode() && ar != null && ar.visible && ar.inHwPCMultiStackWindowingMode()) {
            this.mHwMdm.handleActivityResumedForPCCast(ar);
        }
    }

    public Rect adjustScreenShotRectForPCCast(Rect sourceCrop) {
        if (getCurPCWindowAreaNum() > 0) {
            return this.mHwMdm.adjustScreenShotRectForPCCast(sourceCrop);
        }
        return sourceCrop;
    }

    public void hwSetRequestedOrientation(int taskId, int orientation) {
        this.mHwMdm.hwSetRequestedOrientation(taskId, orientation);
    }

    public void ensureTaskRemovedForPCCast(int taskId) {
        if (HwActivityTaskManager.isPCMultiCastMode()) {
            this.mHwMdm.onTaskRemoved(taskId);
        }
    }

    public Rect getHwMagicWinMiddleBounds(int type) {
        return HwPartMagicWindowServiceFactory.getInstance().getHwPartMagicWindowServiceFactoryImpl().getHwMagicWinManager().getHwMagicWinMiddleBounds(type);
    }

    public void removeFreeformBallWhenDestroy(ActivityRecord activityRecord, TaskRecord taskRecord) {
        this.mHwMwm.removeFreeformBallWhenDestroy(activityRecord, taskRecord);
    }

    public void hwTogglePhoneFullScreenFromLauncherOrRecent(int taskId) {
        if (HwActivityTaskManager.isPCMultiCastMode()) {
            this.mHwMdm.hwTogglePhoneFullScreenFromLauncherOrRecent(taskId);
        }
    }

    public void onEnteringPipForMultiDisplay(int taskId) {
        this.mHwMdm.onEnteringPipForMultiDisplay(taskId);
    }

    public void onEnteringSingleHandForMultiDisplay() {
        this.mHwMdm.onEnteringSingleHandForMultiDisplay();
    }

    public boolean isStartAppLock(String packageName, String action) {
        return ActivityStartInterceptorBridge.isAppLockPackageName(packageName) && ActivityStartInterceptorBridge.isAppLockAction(action);
    }

    public void updateFloatingBallPos(Rect pos) {
        this.mHwMwm.updateFloatingBallPos(pos);
    }

    public boolean minimizeHwFreeForm(IBinder token, String packageName, boolean nonRoot) {
        long identity = Binder.clearCallingIdentity();
        ActivityRecord record = null;
        if (token != null) {
            try {
                record = ActivityRecord.forTokenLocked(token);
            } catch (Throwable record2) {
                Binder.restoreCallingIdentity(identity);
                throw record2;
            }
        } else if (!TextUtils.isEmpty(packageName)) {
            ActivityRecord lastResumed = this.mIAtmsInner.getLastResumedActivityRecord();
            if (!(lastResumed == null || !packageName.equals(lastResumed.packageName) || lastResumed.appToken == null)) {
                record = lastResumed;
            }
        } else {
            Binder.restoreCallingIdentity(identity);
            return false;
        }
        if (record == null) {
            Binder.restoreCallingIdentity(identity);
            return false;
        }
        setDimmer(record);
        boolean isAddToFloatingBall = true;
        TaskRecord task = record.task;
        if (task != null) {
            isAddToFloatingBall = !isSkipAddToFreeformBall(task);
        }
        boolean minimizeHwFreeForm = this.mHwMwm.lambda$minimizeHwFreeForm$4$HwMultiWindowManager(record, nonRoot, isAddToFloatingBall);
        Binder.restoreCallingIdentity(identity);
        return minimizeHwFreeForm;
    }

    public void doReplaceSplitStack(ActivityStack stack) {
        this.mHwMwm.doReplaceSplitStack(stack);
    }

    public void updateWindowForPcFreeForm(ActivityManager.RunningTaskInfo info) {
        if (HwActivityTaskManager.isPCMultiCastMode()) {
            Slog.i(TAG, "update window for pc freeform");
            this.mHwMdm.onTaskRemovalStarted(info);
        }
    }

    public boolean setDockCallBackInfo(IHwDockCallBack callBack, int type) {
        if (!isCallerHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", Binder.getCallingPid(), Binder.getCallingUid())) {
            Slog.e(TAG, "setDockCallBackInfo no right");
            return false;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            return ((DefaultGestureNavManager) LocalServices.getService(DefaultGestureNavManager.class)).setDockCallBackInfo(callBack, type);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void captureScreenToPc(SurfaceControl.ScreenshotGraphicBuffer gb) {
        this.mHwMdm.captureScreenToPc(gb);
    }

    public boolean isNewPcMultiCastMode() {
        return this.mHwMdm.isNewPcMultiCastMode();
    }

    public void recordHwFreeFormBounds(TaskRecord task, boolean isRecordByTaskId) {
        HwFreeFormAssistant hwFreeFormAssistant = this.mHwFreeFormAssistant;
        if (hwFreeFormAssistant != null) {
            hwFreeFormAssistant.recordHwFreeFormBounds(task, isRecordByTaskId);
        }
    }

    public void removeHwFreeFormBoundsRecord(String packageName, int userId) {
        HwFreeFormAssistant hwFreeFormAssistant = this.mHwFreeFormAssistant;
        if (hwFreeFormAssistant != null) {
            hwFreeFormAssistant.removeBoundsRecord(packageName, userId);
        }
    }

    public void removeHwFreeFormBoundsRecordById(int taskId) {
        HwFreeFormAssistant hwFreeFormAssistant = this.mHwFreeFormAssistant;
        if (hwFreeFormAssistant != null) {
            hwFreeFormAssistant.removeBoundsRecordById(taskId);
        }
    }

    public ActivityOptions updateToHwFreeFormIfNeeded(Intent intent, ActivityInfo aInfo, TaskRecord inTask, int launchFlags, ActivityRecord resultTo, ActivityOptions options) {
        HwFreeFormAssistant hwFreeFormAssistant = this.mHwFreeFormAssistant;
        if (hwFreeFormAssistant != null) {
            return hwFreeFormAssistant.updateToHwFreeFormIfNeeded(intent, aInfo, inTask, launchFlags, resultTo, options);
        }
        return options;
    }

    public float getReusableHwFreeFormBounds(String packageName, int userId, Rect outBounds) {
        HwFreeFormAssistant hwFreeFormAssistant = this.mHwFreeFormAssistant;
        if (hwFreeFormAssistant != null) {
            return hwFreeFormAssistant.getReusableBounds(packageName, userId, outBounds);
        }
        return -1.0f;
    }

    private boolean hasHwFreeFormTaskBoundsRecordById(int taskId) {
        HwFreeFormAssistant hwFreeFormAssistant = this.mHwFreeFormAssistant;
        if (hwFreeFormAssistant != null) {
            return hwFreeFormAssistant.hasHwFreeFormTaskRecordById(taskId);
        }
        return false;
    }

    public float getReusableHwFreeFormBoundsById(int taskId, Rect outBounds) {
        HwFreeFormAssistant hwFreeFormAssistant = this.mHwFreeFormAssistant;
        if (hwFreeFormAssistant != null) {
            return hwFreeFormAssistant.getReusableBoundsById(taskId, outBounds);
        }
        return -1.0f;
    }

    public void resetHwFreeFormBoundsRecords(int displayId) {
        HwFreeFormAssistant hwFreeFormAssistant = this.mHwFreeFormAssistant;
        if (hwFreeFormAssistant != null) {
            hwFreeFormAssistant.resetHwFreeFormBoundsRecords(displayId);
        }
    }

    public void notifyCameraStateForAtms(Bundle options) {
        this.mHwMwm.notifyCameraStateForAtms(options);
    }

    public Map<String, String> getPackageNameRotations() {
        return this.mHwMwm.getPackageNameRotations();
    }

    public void updateCameraRotatio(String packageName, int screenRotation, int windowRotation) {
        this.mHwMwm.updateCameraRotatio(packageName, screenRotation, windowRotation);
    }

    public void dumpHwFreeFormBoundsRecords(PrintWriter pw, String cmd, String[] args) {
        HwFreeFormAssistant hwFreeFormAssistant = this.mHwFreeFormAssistant;
        if (hwFreeFormAssistant != null) {
            hwFreeFormAssistant.doDump(pw, cmd, args);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:23:0x0057 A[Catch:{ all -> 0x0172 }] */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x00d5  */
    public void notifyLauncherAction(String category, Bundle bundle) {
        boolean z;
        if (this.mIAtmsInner.getATMS().getRecentTasks().isCallerRecents(Binder.getCallingUid()) && category != null) {
            long origId = Binder.clearCallingIdentity();
            try {
                int hashCode = category.hashCode();
                if (hashCode != -1486606706) {
                    if (hashCode != -442830934) {
                        if (hashCode == -251355042 && category.equals(CATEGORY_SWITCH_TASK)) {
                            z = false;
                            if (!z) {
                                if (!z) {
                                    if (!z) {
                                        Slog.e(TAG, "notifyLauncherAction with unknown category: " + category);
                                    } else {
                                        Slog.d(TAG, "notifyLauncherAction: " + category);
                                        stopInterceptionWhenBackHome();
                                    }
                                } else if (bundle == null) {
                                    Slog.e(TAG, "notifyLauncherAction: " + category + " exit due to empty extras!");
                                    Binder.restoreCallingIdentity(origId);
                                    return;
                                } else {
                                    Slog.d(TAG, "notifyLauncherAction: " + category);
                                    this.mReturnHomeTimeStamp = System.currentTimeMillis();
                                    this.mReturnHomePkg = bundle.getString(STRING_INCOMPATIBLE_XML_PKG);
                                }
                            } else if (!HwDisplaySizeUtil.hasSideInScreen() || bundle == null) {
                                Binder.restoreCallingIdentity(origId);
                                return;
                            } else {
                                synchronized (this.mIAtmsInner.getATMS().getGlobalLock()) {
                                    int taskId = bundle.getInt(HwWmConstants.TASK_ID, -1);
                                    Slog.i(TAG, "notifyLauncherAction quick_switch_task: " + taskId);
                                    if (taskId > 0) {
                                        WindowManagerPolicy policy = this.mIAtmsInner.getATMS().mWindowManager.getPolicy();
                                        if (policy instanceof HwPhoneWindowManager) {
                                            HwPhoneWindowManager phoneWindowManager = (HwPhoneWindowManager) policy;
                                            TaskRecord tr = this.mIAtmsInner.getATMS().mRootActivityContainer.anyTaskForId(taskId, 0);
                                            if (tr == null) {
                                                phoneWindowManager.mIsSkipUpdateSideAndCorner = true;
                                                Binder.restoreCallingIdentity(origId);
                                                return;
                                            }
                                            int index = tr.getChildCount() - 1;
                                            while (true) {
                                                if (index < 0) {
                                                    break;
                                                }
                                                ActivityRecord activity = tr.getChildAt(index);
                                                if (activity.mAppWindowToken == null || HwCustPkgNameConstant.HW_PERMISSION_CONTROLLER_PACKAGE.equals(activity.packageName)) {
                                                    index--;
                                                } else {
                                                    WindowState win = activity.mAppWindowToken.findMainWindow();
                                                    if (win != null) {
                                                        phoneWindowManager.notchControlFilletForSideScreen(win, true);
                                                    }
                                                }
                                            }
                                            phoneWindowManager.mIsSkipUpdateSideAndCorner = true;
                                        } else {
                                            Binder.restoreCallingIdentity(origId);
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    } else if (category.equals(CATEGORY_RETURN_HOME_END)) {
                        z = true;
                        if (!z) {
                        }
                    }
                } else if (category.equals(CATEGORY_RETURN_HOME)) {
                    z = true;
                    if (!z) {
                    }
                }
                z = true;
                if (!z) {
                }
            } finally {
                Binder.restoreCallingIdentity(origId);
            }
        }
    }

    public void onWindowFocusChangedForMultiDisplay(AppWindowToken newFocus) {
        this.mHwMdm.onWindowFocusChangedForMultiDisplay(newFocus);
    }

    public Bitmap getApplicationIcon(IBinder activityToken, boolean isCheckAppLock) {
        long identity = Binder.clearCallingIdentity();
        try {
            synchronized (this.mIAtmsInner.getATMS().getGlobalLock()) {
                ActivityRecord ar = ActivityRecord.forToken(activityToken);
                if (ar == null) {
                    return null;
                }
                boolean isAppLock = isCheckAppLock && ActivityStartInterceptorBridge.isAppLockActivity(ar.shortComponentName);
                Bitmap drawable2Bitmap = HwMultiWinUtils.drawable2Bitmap(HwMultiWinUtils.getAppIcon(this.mIAtmsInner.getUiContext(), (isAppLock || ar.task == null || ar.task.realActivity == null) ? ar.packageName : ar.task.realActivity.getPackageName(), (isAppLock || ar.task == null) ? ar.mUserId : ar.task.userId));
                Binder.restoreCallingIdentity(identity);
                return drawable2Bitmap;
            }
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public boolean isSupportDragToSplitScreen(IBinder token, boolean isCheckAppLock) {
        return HwMultiWindowSwitchManager.getInstance(this).isSupportDragToSplitScreen(token, isCheckAppLock);
    }

    public List<String> getVisibleCanShowWhenLockedPackages(int displayId) {
        return this.mHwMwm.getVisibleCanShowWhenLockedPackages(displayId);
    }

    public boolean shouldAbortSelfLaunchWhenReturnHome(String pkg, int callingUid, int realCallingUid) {
        return isBackHomeRecently() && isBackHomeApp(pkg) && callingUid == realCallingUid && !this.mIAtmsInner.getATMS().getRecentTasks().isCallerRecents(callingUid);
    }

    /* access modifiers changed from: package-private */
    public boolean isBackHomeRecently() {
        if (this.mReturnHomeTimeStamp == 0 || System.currentTimeMillis() >= this.mReturnHomeTimeStamp + 1000) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean isBackHomeApp(String pkg) {
        String str = this.mReturnHomePkg;
        return str != null && str.equals(pkg);
    }

    public void stopInterceptionWhenBackHome() {
        setReturnHomeInfo(0, null);
    }

    /* access modifiers changed from: package-private */
    public void setReturnHomeInfo(long returnHomeTimeStamp, String pkg) {
        this.mReturnHomeTimeStamp = returnHomeTimeStamp;
        this.mReturnHomePkg = pkg;
    }

    public Bundle getFreeformBoundsInCenter(int displayId, int centerX) {
        Bundle freeformBoundsInCenter;
        synchronized (this.mIAtmsInner.getATMS().getGlobalLock()) {
            freeformBoundsInCenter = this.mHwMwm.getFreeformBoundsInCenter(displayId, centerX);
        }
        return freeformBoundsInCenter;
    }

    public void notifyNotificationAnimationFinish(int displayId) {
        this.mHwMwm.notifyNotificationAnimationFinish(displayId);
    }

    public void setTaskStackHide(ActivityStack activityStack, boolean isHide) {
        this.mHwMwm.setTaskStackHide(activityStack, isHide);
    }
}
