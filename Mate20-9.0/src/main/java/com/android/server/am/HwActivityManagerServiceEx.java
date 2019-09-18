package com.android.server.am;

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.AppGlobals;
import android.app.IHwActivityNotifier;
import android.app.KeyguardManager;
import android.app.mtm.MultiTaskManager;
import android.app.mtm.iaware.appmng.AppCleanParam;
import android.app.mtm.iaware.appmng.IAppCleanCallback;
import android.app.usage.UsageStatsManagerInternal;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.UserInfo;
import android.cover.CoverManager;
import android.cover.HallState;
import android.cover.IHallCallback;
import android.database.ContentObserver;
import android.freeform.HwFreeFormUtils;
import android.graphics.Rect;
import android.hardware.display.DisplayManagerInternal;
import android.hardware.display.HwFoldScreenState;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IMWThirdpartyCallback;
import android.os.Message;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.system.ErrnoException;
import android.system.Os;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.CoordinationModeUtils;
import android.util.ERecovery;
import android.util.ERecoveryEvent;
import android.util.Flog;
import android.util.HwPCUtils;
import android.util.Log;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseArray;
import android.util.TimingsTraceLog;
import android.util.Xml;
import android.view.MotionEvent;
import android.view.WindowManagerPolicyConstants;
import android.view.inputmethod.InputMethodInfo;
import android.vrsystem.IVRSystemServiceManager;
import android.widget.Toast;
import android.zrhung.ZrHungData;
import com.android.server.CoordinationStackDividerManager;
import com.android.server.HwServiceFactory;
import com.android.server.LocalServices;
import com.android.server.PPPOEStateMachine;
import com.android.server.ServiceThread;
import com.android.server.UiThread;
import com.android.server.am.ActivityStack;
import com.android.server.am.AppNotRespondingDialog;
import com.android.server.am.PendingIntentRecord;
import com.android.server.gesture.GestureNavConst;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.os.HwBootCheck;
import com.android.server.pm.HwPackageManagerServiceEx;
import com.android.server.pm.auth.HwCertification;
import com.android.server.rms.iaware.appmng.AwareDefaultConfigList;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.security.hsm.HwAddViewHelper;
import com.android.server.security.hsm.HwSystemManagerPlugin;
import com.android.server.security.securityprofile.ISecurityProfileController;
import com.android.server.security.trustspace.ITrustSpaceController;
import com.android.server.wifipro.WifiProCommonUtils;
import com.android.server.wm.WindowManagerService;
import com.android.server.wm.WindowState;
import com.huawei.android.fsm.HwFoldScreenManagerInternal;
import com.huawei.android.pushagentproxy.PushService;
import com.huawei.hsm.permission.ANRFilter;
import huawei.com.android.internal.policy.HiTouchSensor;
import huawei.com.android.server.fingerprint.FingerViewController;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.xmlpull.v1.XmlPullParser;

public final class HwActivityManagerServiceEx implements IHwActivityManagerServiceEx {
    private static final String ACTION_CONFIRM_APPLOCK_CREDENTIAL = "huawei.intent.action.APPLOCK_FRAMEWORK_MANAGER";
    private static final String ACTION_CONFIRM_APPLOCK_PACKAGENAME = "com.huawei.systemmanager";
    private static final String ACTION_HWOUC_SHOW_UPGRADE_REMIND = "com.huawei.android.hwouc.action.SHOW_UPGRADE_REMIND";
    private static final Set<String> ACTIVITY_NOTIFIER_TYPES = new HashSet<String>() {
        {
            add("returnToHome");
            add("activityLifeState");
            add("appSwitch");
        }
    };
    private static final String APKPATCH_META_DATA = "android.huawei.MARKETED_SYSTEM_APP";
    private static final long APPEYE_UIPINPUT_FAULTID = 901004006;
    private static final long APPEYE_UIPINPUT_KILL = 401007005;
    private static final long APPEYE_UIPINPUT_NOTIFY = 401007006;
    private static final int APP_ASSOC_HOME_UPDATE = 11;
    private static final String ASSOC_CALL_PID = "callPid";
    private static final String ASSOC_CALL_PROCNAME = "callProcName";
    private static final String ASSOC_CALL_UID = "callUid";
    private static final String ASSOC_PID = "pid";
    private static final String ASSOC_PKGNAME = "pkgname";
    private static final String ASSOC_RELATION_TYPE = "relationType";
    private static final int ASSOC_REPORT_MIN_TIME = 60000;
    private static final String ASSOC_TGT_COMPNAME = "compName";
    private static final String ASSOC_TGT_PROCNAME = "tgtProcName";
    private static final String ASSOC_TGT_UID = "tgtUid";
    private static final int CACHED_PROCESS_LIMIT = 8;
    private static final Set<String> CLONEPROFILE_PERMISSION = new HashSet<String>() {
        {
            add("com.huawei.hidisk");
            add("com.android.gallery3d");
            add("com.hicloud.android.clone");
            add("com.huawei.KoBackup");
        }
    };
    static final boolean DEBUG_HWTRIM = smcsLOGV;
    static final boolean DEBUG_HWTRIM_PERFORM = smcsLOGV;
    private static final Set<String> EXEMPTED_AUTHORITIES = new HashSet<String>() {
        {
            add("com.huawei.systemmanager.fileProvider");
            add("com.huawei.pcassistant.provider");
        }
    };
    private static final int FG_TO_TOP_APP_MSG = 70;
    private static final String HIVOICE_PKGNAME = "com.huawei.vassistant";
    private static final int HWOUC_UPDATE_REMIND_MSG = 80;
    private static final String HW_LAUNCHER_PKGNAME = "com.huawei.android.launcher";
    private static final boolean HW_SUPPORT_LAUNCHER_EXIT_ANIM = (!SystemProperties.getBoolean("ro.config.disable_launcher_exit_anim", false));
    private static final String HW_SYSTEM_SERVER_START = "com.huawei.systemserver.START";
    private static final boolean IS_BOPD = SystemProperties.getBoolean("sys.bopd", false);
    private static final boolean IS_CHINA = "CN".equalsIgnoreCase(SystemProperties.get(WifiProCommonUtils.KEY_PROP_LOCALE, ""));
    public static final boolean IS_SUPPORT_CLONE_APP = SystemProperties.getBoolean("ro.config.hw_support_clone_app", false);
    static final int KILL_APPLICATION_MSG = 22;
    private static final int MIN_CLEAN_PKG = 5;
    static final int MULTI_WINDOW_MODE_CHANGED_MSG = 23;
    private static final int NOTIFY_ACTIVITY_STATE = 71;
    static final int NOTIFY_CALL = 24;
    private static final Set<String> ONLY_NOTIFY_SYSTEM_USER = new HashSet<String>() {
        {
            add(FingerViewController.PKGNAME_OF_KEYGUARD);
            add("com.huawei.systemserver");
        }
    };
    private static final String PACKAGE_HWOUC = "com.huawei.android.hwouc";
    private static final int PENDINGINTENT_ALLOW_TIME_THRESHOLD = 2000;
    private static final String PERMISSION_HWOUC_UPGRADE_REMIND = "com.huawei.android.hwouc.permission.UPGRADE_REMIND";
    private static final int PERSISTENT_MASK = 9;
    private static final String PICKCOLOR_BLACK_LIST = "pickcolor_blacklist.xml";
    private static final int PRIMARY_SYSTEM_GID = 1000;
    private static final Set<String> PROCESS_NAME_IN_REPAIR_MODE = new HashSet<String>() {
        {
            add("com.huawei.ddtTest");
            add("com.huawei.morpheus");
            add("com.huawei.hwdetectrepair");
            add("com.huawei.hiviewtunnel");
        }
    };
    public static final int PROVISIONED_OFF = 0;
    public static final int PROVISIONED_ON = 1;
    private static final String REASON_SYS_REPLACE = "replace sys pkg";
    private static final int REPAIR_MODE_SYSTEM_UID = 12701000;
    private static final String RESOURCE_APPASSOC = "RESOURCE_APPASSOC";
    private static final String SETTING_GUEST_HAS_LOGGED_IN = "guest_has_logged_in";
    private static final int SHOW_APPFREEZE_DIALOG_MSG = 51;
    private static final int SHOW_GUEST_SWITCH_DIALOG_MSG = 50;
    private static final int SHOW_SWITCH_DIALOG_MSG = 49;
    static final int SHOW_UNINSTALL_LAUNCHER_MSG = 48;
    private static final String SPLIT_SCREEN_APP_NAME = "splitscreen.SplitScreenAppActivity";
    private static final int START_HW_SERVICE_POST_MSG_DELAY = 30000;
    static final String TAG = "HwActivityManagerServiceEx";
    private static final String XML_ATTRIBUTE_NAME = "name";
    private static final String XML_NOT_PICKCOLOR_APP = "nopick_app";
    private static final String XML_PICKCOLOR_BLACK_LIST = "pickcolor_blacklist";
    private static Set<String> mPIPWhitelists = new HashSet();
    private static Set<String> mTranslucentWhitelists = new HashSet();
    private static Set<String> mWhitelistActivities = new HashSet();
    private static Set<String> sAllowedCrossUserForCloneArrays = new HashSet();
    private static HashMap<String, Integer> sHardCodeAppToSetOomAdjArrays = new HashMap<>();
    static final boolean smcsLOGV = SystemProperties.getBoolean("ro.enable.st_debug", false);
    private final String FACE_KEYGUARD = "face_bind_with_lock";
    private final String FP_KEYGUARD = "fp_keyguard_enable";
    private boolean isLastMultiMode = false;
    final RemoteCallbackList<IHwActivityNotifier> mActivityNotifiers = new RemoteCallbackList<>();
    private final ArrayMap<Integer, ArrayMap<Integer, Long>> mAssocMap = new ArrayMap<>();
    final Context mContext;
    FaceSettingsObserver mFaceSettingsObserver;
    FingerprintSettingsObserver mFingerprintSettingsObserver;
    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i != 80) {
                switch (i) {
                    case 48:
                        HwActivityManagerServiceEx.this.showUninstallLauncher();
                        return;
                    case 49:
                        HwActivityManagerServiceEx.this.mIAmsInner.getUserController().showUserSwitchDialog((Pair) msg.obj);
                        return;
                    case 50:
                        HwActivityManagerServiceEx.this.showGuestSwitchDialog(msg.arg1, (String) msg.obj);
                        return;
                    case 51:
                        HwActivityManagerServiceEx.this.showAppEyeAnrUi(msg);
                        return;
                    default:
                        switch (i) {
                            case 70:
                                HwActivityManagerServiceEx.this.reportFgToTopMsg(msg);
                                return;
                            case HwActivityManagerServiceEx.NOTIFY_ACTIVITY_STATE /*71*/:
                                HwActivityManagerServiceEx.this.handleNotifyActivityState(msg);
                                return;
                            default:
                                return;
                        }
                }
            } else {
                Slog.i(HwActivityManagerServiceEx.TAG, "send UPDATE REMIND broacast to HWOUC");
                Intent intent = new Intent(HwActivityManagerServiceEx.ACTION_HWOUC_SHOW_UPGRADE_REMIND);
                intent.setPackage(HwActivityManagerServiceEx.PACKAGE_HWOUC);
                HwActivityManagerServiceEx.this.mContext.sendBroadcastAsUser(intent, UserHandle.SYSTEM, HwActivityManagerServiceEx.PERMISSION_HWOUC_UPGRADE_REMIND);
            }
        }
    };
    private HiTouchSensor mHiTouchSensor;
    Handler mHwHandler = null;
    ServiceThread mHwHandlerThread = null;
    final TaskChangeNotificationController mHwTaskChangeNotificationController;
    IHwActivityManagerInner mIAmsInner = null;
    public boolean mKeepPrimaryCoordinationResumed;
    /* access modifiers changed from: private */
    public String mLastLauncherName;
    /* access modifiers changed from: private */
    public boolean mNeedRemindHwOUC = false;
    private ResetSessionDialog mNewSessionDialog;
    public HashMap<String, Long> mPCUsageStats = new HashMap<>();
    public HashMap<String, Integer> mPkgDisplayMaps = new HashMap<>();
    private Intent mQuickSlideIntent = null;
    private long mQuickSlideStartTime;
    private SettingsObserver mSettingsObserver;
    /* access modifiers changed from: private */
    public RemoteCallbackList<IMWThirdpartyCallback> mThirdPartyCallbackList;
    ITrustSpaceController mTrustSpaceController;
    private IVRSystemServiceManager mVrMananger;
    private List<String> pickColorBlackList = null;

    private final class FaceSettingsObserver extends ContentObserver {
        private final Uri FACE_KEYGUARD_URI = Settings.Secure.getUriFor("face_bind_with_lock");

        public FaceSettingsObserver() {
            super(HwActivityManagerServiceEx.this.mHandler);
            HwActivityManagerServiceEx.this.mContext.getContentResolver().registerContentObserver(this.FACE_KEYGUARD_URI, false, this, -1);
        }

        public void onChange(boolean selfChange, Uri uri, int userId) {
            HwActivityManagerServiceEx.this.updateUnlockBoostStatus(userId);
        }
    }

    private final class FingerprintSettingsObserver extends ContentObserver {
        private final Uri FP_KEYGUARD_URI = Settings.Secure.getUriFor("fp_keyguard_enable");

        public FingerprintSettingsObserver() {
            super(HwActivityManagerServiceEx.this.mHandler);
            HwActivityManagerServiceEx.this.mContext.getContentResolver().registerContentObserver(this.FP_KEYGUARD_URI, false, this, -1);
        }

        public void onChange(boolean selfChange, Uri uri, int userId) {
            HwActivityManagerServiceEx.this.updateUnlockBoostStatus(userId);
        }
    }

    private class ResetSessionDialog extends AlertDialog implements DialogInterface.OnClickListener {
        private final int mUserId;

        public ResetSessionDialog(Context context, int userId) {
            super(context, context.getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null));
            getWindow().setType(2014);
            getWindow().addFlags(655360);
            KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService("keyguard");
            if (keyguardManager != null && keyguardManager.isKeyguardLocked()) {
                getWindow().addPrivateFlags(Integer.MIN_VALUE);
            }
            setMessage(context.getString(33685841));
            setButton(-1, context.getString(33685843), this);
            setButton(-2, context.getString(33685842), this);
            setCanceledOnTouchOutside(false);
            this.mUserId = userId;
        }

        public void onClick(DialogInterface dialog, int which) {
            Slog.i(HwActivityManagerServiceEx.TAG, "onClick which:" + which);
            if (which == -2) {
                HwActivityManagerServiceEx.this.wipeGuestSession(this.mUserId);
                dismiss();
            } else if (which == -1) {
                cancel();
                HwActivityManagerServiceEx.this.sendMessageToSwitchUser(this.mUserId, HwActivityManagerServiceEx.this.getGuestName());
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
            ContentResolver resolver = HwActivityManagerServiceEx.this.mContext.getContentResolver();
            boolean z = false;
            resolver.registerContentObserver(this.URI_HW_UPGRADE_REMIND, false, this, 0);
            HwActivityManagerServiceEx hwActivityManagerServiceEx = HwActivityManagerServiceEx.this;
            if (Settings.Secure.getIntForUser(resolver, KEY_HW_UPGRADE_REMIND, 0, 0) != 0) {
                z = true;
            }
            boolean unused = hwActivityManagerServiceEx.mNeedRemindHwOUC = z;
        }

        public void onChange(boolean selfChange, Uri uri) {
            if (this.URI_HW_UPGRADE_REMIND.equals(uri)) {
                HwActivityManagerServiceEx hwActivityManagerServiceEx = HwActivityManagerServiceEx.this;
                boolean z = false;
                if (Settings.Secure.getIntForUser(HwActivityManagerServiceEx.this.mContext.getContentResolver(), KEY_HW_UPGRADE_REMIND, 0, 0) != 0) {
                    z = true;
                }
                boolean unused = hwActivityManagerServiceEx.mNeedRemindHwOUC = z;
                Slog.i(HwActivityManagerServiceEx.TAG, "mNeedRemindHwOUC has changed to : " + HwActivityManagerServiceEx.this.mNeedRemindHwOUC);
            }
        }
    }

    static {
        sAllowedCrossUserForCloneArrays.add(HwCertification.SIGNATURE_MEDIA);
        sAllowedCrossUserForCloneArrays.add("com.android.providers.media.documents");
        sAllowedCrossUserForCloneArrays.add("com.huawei.android.launcher.settings");
        sAllowedCrossUserForCloneArrays.add("com.android.badge");
        sAllowedCrossUserForCloneArrays.add("com.android.providers.media");
        sAllowedCrossUserForCloneArrays.add("android.media.IMediaScannerService");
        sAllowedCrossUserForCloneArrays.add("com.android.contacts.files");
        sAllowedCrossUserForCloneArrays.add("com.android.contacts.app");
        sAllowedCrossUserForCloneArrays.add("com.huawei.numberlocation");
        sAllowedCrossUserForCloneArrays.add("csp-prefs-cfg");
        sAllowedCrossUserForCloneArrays.add("contacts");
        sAllowedCrossUserForCloneArrays.add("com.android.contacts");
        sAllowedCrossUserForCloneArrays.add("android.process.media");
        sAllowedCrossUserForCloneArrays.add("com.huawei.android.launcher");
        sAllowedCrossUserForCloneArrays.add("android.process.acore");
        sAllowedCrossUserForCloneArrays.add("call_log");
        sAllowedCrossUserForCloneArrays.add("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        sAllowedCrossUserForCloneArrays.add("android.intent.action.MEDIA_SCANNER_SCAN_FOLDER");
        sAllowedCrossUserForCloneArrays.add("com.android.launcher.action.INSTALL_SHORTCUT");
        sAllowedCrossUserForCloneArrays.add("mms");
        sAllowedCrossUserForCloneArrays.add("sms");
        sAllowedCrossUserForCloneArrays.add("mms-sms");
        sAllowedCrossUserForCloneArrays.add("com.android.providers.downloads");
        sAllowedCrossUserForCloneArrays.add("downloads");
        sAllowedCrossUserForCloneArrays.add("com.android.providers.downloads.documents");
        sHardCodeAppToSetOomAdjArrays.put("com.huawei.android.pushagent.PushService", Integer.valueOf(HwActivityManagerService.PERSISTENT_PROC_ADJ));
        sHardCodeAppToSetOomAdjArrays.put("com.tencent.mm", 800);
        mWhitelistActivities.add("com.vlocker.settings.DismissActivity");
        mTranslucentWhitelists.add("com.android.packageinstaller.permission.ui.GrantPermissionsActivity");
        mPIPWhitelists.add("com.android.systemui.pip.phone.PipMenuActivity");
    }

    public HwActivityManagerServiceEx(IHwActivityManagerInner iams, Context context) {
        this.mIAmsInner = iams;
        this.mContext = context;
        this.mHwHandlerThread = new ServiceThread(TAG, -2, false);
        this.mHwHandlerThread.start();
        this.mHwHandler = new Handler(this.mHwHandlerThread.getLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 22:
                        synchronized (HwActivityManagerServiceEx.this.mIAmsInner.getAMSForLock()) {
                            int appId = msg.arg1;
                            int userId = msg.arg2;
                            Bundle bundle = (Bundle) msg.obj;
                            String pkg = bundle.getString("pkg");
                            String reason = bundle.getString("reason");
                            Slog.w(HwActivityManagerServiceEx.TAG, "killApplication start for pkg: " + pkg + ", userId: " + userId);
                            HwActivityManagerServiceEx.this.mIAmsInner.forceStopPackageLockedInner(pkg, appId, false, false, true, false, false, userId, reason);
                            Slog.w(HwActivityManagerServiceEx.TAG, "killApplication end for pkg: " + pkg + ", userId: " + userId);
                        }
                        return;
                    case 23:
                        boolean isInMultiWindowMode = ((Boolean) msg.obj).booleanValue();
                        synchronized (HwActivityManagerServiceEx.this.mThirdPartyCallbackList) {
                            try {
                                int i = HwActivityManagerServiceEx.this.mThirdPartyCallbackList.beginBroadcast();
                                Flog.i(100, "onMultiWindowModeChanged begin : mThirdPartyCallbackList size : " + i);
                                while (i > 0) {
                                    i--;
                                    try {
                                        HwActivityManagerServiceEx.this.mThirdPartyCallbackList.getBroadcastItem(i).onModeChanged(isInMultiWindowMode);
                                    } catch (Exception e) {
                                        Flog.e(100, "Error in sending the Callback");
                                    }
                                }
                                Flog.i(100, "onMultiWindowModeChanged end : mThirdPartyCallbackList size : " + i);
                                HwActivityManagerServiceEx.this.mThirdPartyCallbackList.finishBroadcast();
                            } catch (IllegalStateException e2) {
                                Flog.e(100, "beginBroadcast() called while already in a broadcast");
                            }
                        }
                        return;
                    case 24:
                        synchronized (HwActivityManagerServiceEx.this.mActivityNotifiers) {
                            try {
                                long start = System.currentTimeMillis();
                                Bundle bundle2 = (Bundle) msg.obj;
                                String userId2 = String.valueOf(bundle2.getInt("android.intent.extra.user_handle"));
                                String reason2 = bundle2.getString("android.intent.extra.REASON");
                                int i2 = HwActivityManagerServiceEx.this.mActivityNotifiers.beginBroadcast();
                                while (i2 > 0) {
                                    i2--;
                                    IHwActivityNotifier notifier = HwActivityManagerServiceEx.this.mActivityNotifiers.getBroadcastItem(i2);
                                    HashMap<String, String> cookie = (HashMap) HwActivityManagerServiceEx.this.mActivityNotifiers.getBroadcastCookie(i2);
                                    if ((userId2.equals(cookie.get("android.intent.extra.user_handle")) || cookie.get("android.intent.extra.USER") != null) && reason2.equals(cookie.get("android.intent.extra.REASON"))) {
                                        try {
                                            HwActivityManagerServiceEx.this.mActivityNotifiers.getBroadcastItem(i2).call(bundle2);
                                        } catch (RemoteException e3) {
                                            Flog.e(100, "observer.call get RemoteException, remove notifier " + notifier);
                                            HwActivityManagerServiceEx.this.mActivityNotifiers.unregister(notifier);
                                        }
                                    }
                                }
                                HwActivityManagerServiceEx.this.mActivityNotifiers.finishBroadcast();
                                Slog.w(HwActivityManagerServiceEx.TAG, "HwActivityNotifier end call for " + reason2 + " under user " + userId2 + " cost " + (System.currentTimeMillis() - start));
                            } catch (Exception e4) {
                                Flog.e(100, "HwActivityNotifier call error");
                            }
                        }
                        return;
                    default:
                        return;
                }
            }
        };
        HwBootCheck.bootSceneStart(100, AppHibernateCst.DELAY_ONE_MINS);
        this.mHwTaskChangeNotificationController = new TaskChangeNotificationController(this.mIAmsInner.getAMSForLock(), this.mIAmsInner.getStackSupervisor(), this.mHandler);
        this.mThirdPartyCallbackList = new RemoteCallbackList<>();
        this.mVrMananger = HwFrameworkFactory.getVRSystemServiceManager();
    }

    /* access modifiers changed from: package-private */
    public final void reportFgToTopMsg(Message msg) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(String.valueOf(msg.arg1));
        stringBuffer.append(CPUCustBaseConfig.CPUCONFIG_INVALID_STR);
        stringBuffer.append(String.valueOf(msg.arg2));
        stringBuffer.append(CPUCustBaseConfig.CPUCONFIG_INVALID_STR);
        stringBuffer.append(msg.obj);
        this.mIAmsInner.getDAMonitor().DAMonitorReport(this.mIAmsInner.getDAMonitor().getFirstDevSchedEventId(), stringBuffer.toString());
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v8, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v2, resolved type: java.lang.String} */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Multi-variable type inference failed */
    public final void handleNotifyActivityState(Message msg) {
        if (msg != null) {
            String activityInfo = null;
            if (msg.obj instanceof String) {
                activityInfo = msg.obj;
            }
            if (activityInfo == null) {
                Slog.e(TAG, "msg.obj type error.");
                return;
            }
            if (!(this.mIAmsInner == null || this.mIAmsInner.getDAMonitor() == null)) {
                this.mIAmsInner.getDAMonitor().notifyActivityState(activityInfo);
            }
        }
    }

    /* access modifiers changed from: private */
    public void showUninstallLauncher() {
        Context mUiContext = this.mIAmsInner.getUiContext();
        try {
            PackageInfo pInfo = this.mContext.getPackageManager().getPackageInfo(this.mLastLauncherName, 0);
            if (pInfo != null) {
                AlertDialog d = new BaseErrorDialog(mUiContext);
                d.getWindow().setType(HwArbitrationDEFS.MSG_MPLINK_UNBIND_FAIL);
                d.setCancelable(false);
                d.setTitle(mUiContext.getString(33685930));
                d.setMessage(mUiContext.getString(33685932, new Object[]{this.mContext.getPackageManager().getApplicationLabel(pInfo.applicationInfo).toString()}));
                d.setButton(-1, mUiContext.getString(33685931), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            HwActivityManagerServiceEx.this.mContext.getPackageManager().deletePackage(HwActivityManagerServiceEx.this.mLastLauncherName, null, 0);
                        } catch (Exception e) {
                            Slog.e(HwActivityManagerServiceEx.TAG, "showUninstallLauncher error because of Exception!");
                        }
                    }
                });
                d.setButton(-2, mUiContext.getString(17039360), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                d.show();
            }
        } catch (PackageManager.NameNotFoundException e) {
        }
    }

    public int changeGidIfRepairMode(int uid, String processName) {
        if (uid != REPAIR_MODE_SYSTEM_UID || !PROCESS_NAME_IN_REPAIR_MODE.contains(processName)) {
            return uid;
        }
        return 1000;
    }

    public void showUninstallLauncherDialog(String pkgName) {
        this.mLastLauncherName = pkgName;
        this.mHandler.sendMessage(this.mHandler.obtainMessage(48));
    }

    /* access modifiers changed from: private */
    public void showGuestSwitchDialog(int userId, String userName) {
        cancelDialog();
        ContentResolver cr = this.mContext.getContentResolver();
        int notFirstLogin = Settings.System.getIntForUser(cr, SETTING_GUEST_HAS_LOGGED_IN, 0, userId);
        Slog.i(TAG, "notFirstLogin:" + notFirstLogin + ", userid=" + userId);
        if (notFirstLogin != 0) {
            showGuestResetSessionDialog(userId);
            return;
        }
        Settings.System.putIntForUser(cr, SETTING_GUEST_HAS_LOGGED_IN, 1, userId);
        sendMessageToSwitchUser(userId, userName);
    }

    public void killApplication(String pkg, int appId, int userId, String reason) {
        if (appId < 0) {
            Slog.w(TAG, "Invalid appid specified for pkg : " + pkg);
            return;
        }
        int callerUid = Binder.getCallingUid();
        if (UserHandle.getAppId(callerUid) == 1000) {
            Message msg = this.mHwHandler.obtainMessage(22);
            msg.arg1 = appId;
            msg.arg2 = userId;
            Bundle bundle = new Bundle();
            Slog.w(TAG, "killApplication send message for pkg: " + pkg + ", userId: " + userId);
            bundle.putString("pkg", pkg);
            bundle.putString("reason", reason);
            msg.obj = bundle;
            this.mHwHandler.sendMessage(msg);
            return;
        }
        throw new SecurityException(callerUid + " cannot kill pkg: " + pkg);
    }

    private final boolean cleanProviderLocked(ProcessRecord proc, ContentProviderRecord cpr, boolean always) {
        boolean inLaunching = this.mIAmsInner.getLaunchingProviders().contains(cpr);
        if (!inLaunching || always) {
            synchronized (cpr) {
                cpr.launchingApp = null;
                cpr.notifyAll();
            }
            this.mIAmsInner.getProviderMap().removeProviderByClass(cpr.name, UserHandle.getUserId(cpr.uid));
            String[] names = cpr.info.authority.split(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
            for (String removeProviderByName : names) {
                this.mIAmsInner.getProviderMap().removeProviderByName(removeProviderByName, UserHandle.getUserId(cpr.uid));
            }
        }
        for (int i = cpr.connections.size() - 1; i >= 0; i--) {
            ContentProviderConnection conn = (ContentProviderConnection) cpr.connections.get(i);
            if (!conn.waiting || !inLaunching || always) {
                ProcessRecord capp = conn.client;
                conn.dead = true;
                if (conn.stableCount > 0) {
                    if (!(capp.persistent || capp.thread == null || capp.pid == 0 || capp.pid == this.mIAmsInner.getAmsPid())) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("depends on provider ");
                        sb.append(cpr.name.flattenToShortString());
                        sb.append(" in dying proc ");
                        sb.append(proc != null ? proc.processName : "??");
                        capp.kill(sb.toString(), true);
                    }
                } else if (!(capp.thread == null || conn.provider.provider == null)) {
                    try {
                        capp.thread.unstableProviderDied(conn.provider.provider.asBinder());
                    } catch (RemoteException e) {
                        Slog.e(TAG, "cleanProviderLocked error because RemoteException!");
                    }
                    cpr.connections.remove(i);
                    if (conn.client.conProviders.remove(conn)) {
                        this.mIAmsInner.stopAssociationLockedInner(capp.uid, capp.processName, cpr.uid, cpr.name);
                    }
                }
            }
        }
        if (inLaunching && always) {
            this.mIAmsInner.getLaunchingProviders().remove(cpr);
        }
        return inLaunching;
    }

    /* JADX WARNING: Removed duplicated region for block: B:28:0x0059 A[Catch:{ all -> 0x00f0, all -> 0x00f4 }] */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x005e A[Catch:{ all -> 0x00f0, all -> 0x00f4 }] */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x0085 A[Catch:{ all -> 0x00f0, all -> 0x00f4 }] */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0091 A[Catch:{ all -> 0x00f0, all -> 0x00f4 }, LOOP:1: B:36:0x008e->B:38:0x0091, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x00a7 A[Catch:{ all -> 0x00f0, all -> 0x00f4 }, LOOP:2: B:40:0x00a5->B:41:0x00a7, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x00b9 A[Catch:{ all -> 0x00f0, all -> 0x00f4 }] */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00c3 A[ADDED_TO_REGION, Catch:{ all -> 0x00f0, all -> 0x00f4 }] */
    public boolean cleanPackageRes(List<String> packageList, Map<String, List<String>> alarmTags, int targetUid, boolean cleanAlarm, boolean isNative, boolean hasPerceptAlarm) {
        String packageName;
        int userId;
        String packageName2;
        ArrayList<ContentProviderRecord> providers;
        String packageName3;
        int i;
        int i2;
        Map<String, List<String>> map = alarmTags;
        int i3 = targetUid;
        if (packageList == null) {
            return false;
        }
        boolean didSomething = false;
        int userId2 = UserHandle.getUserId(targetUid);
        synchronized (this.mIAmsInner.getAMSForLock()) {
            try {
                Iterator<String> it = packageList.iterator();
                while (it.hasNext()) {
                    String packageName4 = it.next();
                    if (!isNative) {
                        try {
                            if (!canCleanTaskRecord(packageName4)) {
                                packageName = packageName4;
                                userId = userId2;
                                if (this.mIAmsInner.bringDownDisabledPackageServicesLocked(packageName, null, userId2, false, true, true)) {
                                    didSomething = true;
                                }
                                packageName2 = packageName;
                                if (packageName2 == null) {
                                    this.mIAmsInner.getStickyBroadcasts().remove(userId);
                                }
                                providers = new ArrayList<>();
                                Iterator<String> it2 = it;
                                packageName3 = packageName2;
                                if (this.mIAmsInner.getProviderMap().collectPackageProvidersLocked(packageName2, null, true, false, userId, providers)) {
                                    didSomething = true;
                                }
                                ArrayList<ContentProviderRecord> providers2 = providers;
                                for (i = providers2.size() - 1; i >= 0; i--) {
                                    cleanProviderLocked(null, providers2.get(i), true);
                                }
                                for (i2 = this.mIAmsInner.getBroadcastQueues().length - 1; i2 >= 0; i2--) {
                                    didSomething |= this.mIAmsInner.getBroadcastQueues()[i2].cleanupDisabledPackageReceiversLocked(packageName3, null, userId, true);
                                }
                                if (map != null) {
                                    this.mIAmsInner.getAlarmService().removePackageAlarm(packageName3, null, i3);
                                } else if (cleanAlarm && this.mIAmsInner.getAlarmService() != null) {
                                    List<String> tags = map.get(packageName3);
                                    if (tags != null) {
                                        this.mIAmsInner.getAlarmService().removePackageAlarm(packageName3, tags, i3);
                                    }
                                }
                                if (!isNative || !hasPerceptAlarm) {
                                    this.mIAmsInner.finishForceStopPackageLockedInner(packageName3, i3);
                                }
                                userId2 = userId;
                                it = it2;
                            }
                        } catch (Throwable th) {
                            th = th;
                            int i4 = userId2;
                            throw th;
                        }
                    }
                    packageName = packageName4;
                    if (this.mIAmsInner.finishDisabledPackageActivitiesLocked(packageName4, null, true, false, userId2)) {
                        didSomething = true;
                    }
                    userId = userId2;
                    if (this.mIAmsInner.bringDownDisabledPackageServicesLocked(packageName, null, userId2, false, true, true)) {
                    }
                    packageName2 = packageName;
                    if (packageName2 == null) {
                    }
                    providers = new ArrayList<>();
                    Iterator<String> it22 = it;
                    packageName3 = packageName2;
                    if (this.mIAmsInner.getProviderMap().collectPackageProvidersLocked(packageName2, null, true, false, userId, providers)) {
                    }
                    ArrayList<ContentProviderRecord> providers22 = providers;
                    while (i >= 0) {
                    }
                    while (i2 >= 0) {
                    }
                    if (map != null) {
                    }
                    if (!isNative) {
                    }
                    this.mIAmsInner.finishForceStopPackageLockedInner(packageName3, i3);
                    userId2 = userId;
                    it = it22;
                }
                return didSomething;
            } catch (Throwable th2) {
                th = th2;
                throw th;
            }
        }
    }

    public boolean killProcessRecordFast(String processName, int pid, int uid, boolean restartservice, boolean isAsynchronous, String reason, boolean needCheckAdj) {
        return killProcessRecordInternal(processName, pid, uid, restartservice, isAsynchronous, reason, false, needCheckAdj);
    }

    public boolean killNativeProcessRecordFast(String processName, int pid, int uid, boolean restartservice, boolean isAsynchronous, String reason) {
        return killProcessRecordInternal(processName, pid, uid, restartservice, isAsynchronous, reason, true, true);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:41:?, code lost:
        r2.unlinkDeathRecipient();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00ac, code lost:
        android.util.Slog.w(TAG, "Unexpected exception while unlink death");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00b5, code lost:
        android.util.Slog.w(TAG, "null while unlink death");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00be, code lost:
        android.util.Slog.w(TAG, "NoSuchElementException while unlink death.");
     */
    private boolean killProcessRecordInternal(String processName, int pid, int uid, boolean restartservice, boolean isAsynchronous, String reason, boolean isNative, boolean needCheckAdj) {
        ProcessRecord proc;
        boolean isImportantAdj;
        if (processName == null) {
            Slog.w(TAG, "input params processName is null, error.");
            return false;
        }
        SparseArray<ProcessRecord> pidsSelfLocked = this.mIAmsInner.getPidsSelfLocked();
        if (pidsSelfLocked == null) {
            Slog.w(TAG, "get pidsSelfLocked is null, error.");
            return false;
        }
        synchronized (pidsSelfLocked) {
            if (pid == this.mIAmsInner.getMyPid() || pid < 0) {
                Slog.e(TAG, "failed to get process record, mUid: " + uid);
                return false;
            }
            proc = pidsSelfLocked.get(pid);
            if (proc == null) {
                Slog.e(TAG, "this process has been killed or died before:" + processName);
                return false;
            }
            if (!proc.killed) {
                if (!proc.killedByAm) {
                    if (proc.curAdj > 0) {
                        if (!needCheckAdj || proc.curAdj >= 200 || AwareAppMngSort.EXEC_SERVICES.equals(proc.adjType)) {
                            isImportantAdj = false;
                            if (isNative && isImportantAdj) {
                                Slog.e(TAG, "process cleaner kill process: adj changed, new adj: " + proc.curAdj + ", pid:" + pid + ", uid:" + uid + ", " + processName);
                                return false;
                            }
                        }
                    }
                    isImportantAdj = true;
                    if (isNative) {
                    }
                }
            }
            Slog.i(TAG, "the process name=" + proc.processName + "is killed");
            return false;
        }
        this.mIAmsInner.getDAMonitor().notifyProcessDied(proc.pid, proc.uid);
        proc.kill("iAwareF[" + reason + "](" + proc.adjType + ")", true);
        this.mIAmsInner.getDAMonitor().reportAppDiedMsg(proc.userId, proc.processName, reason);
        return true;
    }

    private ProcessRecord getProcessRecord(String processName, int pid) {
        if (processName == null) {
            Slog.w(TAG, "getProcessRecord, processName is null, pid: " + pid);
            return null;
        }
        SparseArray<ProcessRecord> pidsSelfLocked = this.mIAmsInner.getPidsSelfLocked();
        if (pidsSelfLocked == null) {
            Slog.w(TAG, "getProcessRecord, get pidsSelfLocked is null, error.");
            return null;
        }
        synchronized (pidsSelfLocked) {
            ProcessRecord proc = pidsSelfLocked.get(pid);
            if (proc == null) {
                Slog.w(TAG, "process(" + processName + ") do not exist in mPidsSelfLocked");
                return null;
            } else if (processName.equals(proc.processName)) {
                return proc;
            } else {
                Slog.w(TAG, "input params process(" + processName + ") is differet from process (" + proc.processName + ") from mPidsSelfLocked. pid:" + pid);
                return null;
            }
        }
    }

    public boolean cleanProcessResourceFast(String processName, int pid, IBinder thread, boolean restartService, boolean isNative) {
        boolean result;
        if (processName == null) {
            Slog.i(TAG, "processName is null.");
            return false;
        }
        ProcessRecord app = getProcessRecord(processName, pid);
        if (app == null) {
            Slog.i(TAG, "ProcessRecord is null, processName:" + processName + ", pid:" + pid);
            return false;
        }
        synchronized (this.mIAmsInner.getAMSForLock()) {
            boolean hasShowUi = app.hasShownUi;
            result = this.mIAmsInner.removeProcessLockedInner(app, pid, thread, false, restartService, "iAwareK[" + restartService + "] fast");
            if (result && hasShowUi) {
                handleActivityWithAppDiedLocked(app, isNative);
            }
        }
        return result;
    }

    private final void handleActivityWithAppDiedLocked(ProcessRecord app, boolean isNative) {
        if (app != null) {
            String packageName = app.info != null ? app.info.packageName : null;
            if (isNative || canCleanTaskRecord(packageName)) {
                this.mIAmsInner.finishDisabledPackageActivitiesLocked(packageName, null, true, false, UserHandle.getUserId(app.uid));
            }
        }
    }

    public boolean needCheckProcDied(ProcessRecord app) {
        if (app == null || !app.killed || !this.mIAmsInner.getDAMonitor().isFastKillSwitch(app.processName, app.uid)) {
            return false;
        }
        boolean isKilled = app.killed;
        if (isKilled) {
            Slog.d(TAG, "app is killed, app=" + app);
        }
        return isKilled;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00af, code lost:
        return true;
     */
    public boolean canCleanTaskRecord(String packageName) {
        if (packageName == null) {
            return true;
        }
        synchronized (this.mIAmsInner.getAMSForLock()) {
            ArrayList<TaskRecord> recentTasks = this.mIAmsInner.getRecentRawTasks();
            if (recentTasks == null) {
                return true;
            }
            int size = recentTasks.size();
            int maxFoundNum = this.mIAmsInner.getDAMonitor().getActivityImportCount();
            int foundNum = 0;
            for (int i = 0; i < size && foundNum < maxFoundNum; i++) {
                TaskRecord tr = recentTasks.get(i);
                if (tr != null) {
                    if (tr.mActivities != null) {
                        if (!(tr.mActivities.size() <= 0 || tr.getBaseIntent() == null || tr.getBaseIntent().getComponent() == null)) {
                            if (packageName.equals(tr.getBaseIntent().getComponent().getPackageName())) {
                                return false;
                            }
                            if (!this.mIAmsInner.getDAMonitor().getRecentTask().equals(tr.getBaseIntent().getComponent().flattenToShortString())) {
                                if ((tr.getBaseIntent().getFlags() & 8388608) != 0) {
                                }
                            }
                        }
                        foundNum++;
                    }
                }
            }
            if ((this.mIAmsInner.getStackSupervisor() instanceof HwActivityStackSupervisor) && this.mIAmsInner.getStackSupervisor().isInVisibleStack(packageName)) {
                return false;
            }
        }
    }

    public Boolean switchUser(int userId) {
        boolean isStorageLow = false;
        try {
            isStorageLow = AppGlobals.getPackageManager().isStorageLow();
        } catch (RemoteException e) {
            Slog.e(TAG, "check low storage error because e: " + e);
        }
        if (isStorageLow) {
            UiThread.getHandler().post(new Runnable() {
                public void run() {
                    Toast toast = Toast.makeText(HwActivityManagerServiceEx.this.mContext, HwActivityManagerServiceEx.this.mContext.getResources().getString(33686138), 1);
                    toast.getWindowParams().type = 2101;
                    toast.getWindowParams().privateFlags |= 16;
                    toast.show();
                }
            });
            return Boolean.FALSE;
        }
        UserInfo targetUser = this.mIAmsInner.getUserController().getUserInfo(userId);
        if (targetUser == null || !targetUser.isGuest()) {
            return null;
        }
        this.mHandler.removeMessages(50);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(50, userId, 0, targetUser.name));
        return Boolean.TRUE;
    }

    /* access modifiers changed from: private */
    public void sendMessageToSwitchUser(int userId, String userName) {
        UserController userctl = this.mIAmsInner.getUserController();
        Pair<UserInfo, UserInfo> userNames = new Pair<>(userctl.getUserInfo(userctl.getCurrentUserId()), userctl.getUserInfo(userId));
        this.mHandler.removeMessages(49);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(49, userNames));
    }

    private void showGuestResetSessionDialog(int guestId) {
        this.mNewSessionDialog = new ResetSessionDialog(this.mContext, guestId);
        this.mNewSessionDialog.show();
    }

    private void cancelDialog() {
        if (this.mNewSessionDialog != null && this.mNewSessionDialog.isShowing()) {
            this.mNewSessionDialog.cancel();
            this.mNewSessionDialog = null;
        }
    }

    private String getUserName(int userId) {
        if (this.mIAmsInner.getUserController() == null) {
            return null;
        }
        UserInfo info = this.mIAmsInner.getUserController().getUserInfo(userId);
        if (info == null) {
            return null;
        }
        return info.name;
    }

    /* access modifiers changed from: private */
    public String getGuestName() {
        return this.mContext.getString(33685844);
    }

    /* access modifiers changed from: private */
    public void wipeGuestSession(int userId) {
        UserManager userManager = (UserManager) this.mContext.getSystemService("user");
        if (!userManager.markGuestForDeletion(userId)) {
            Slog.w(TAG, "Couldn't mark the guest for deletion for user " + userId);
            return;
        }
        UserInfo newGuest = userManager.createGuest(this.mContext, getGuestName());
        if (newGuest == null) {
            Slog.e(TAG, "Could not create new guest, switching back to owner");
            sendMessageToSwitchUser(0, getUserName(0));
            userManager.removeUser(userId);
            return;
        }
        Slog.d(TAG, "Create new guest, switching to = " + newGuest.id);
        sendMessageToSwitchUser(newGuest.id, newGuest.name);
        Settings.System.putIntForUser(this.mContext.getContentResolver(), SETTING_GUEST_HAS_LOGGED_IN, 1, newGuest.id);
        userManager.removeUser(userId);
    }

    public TaskChangeNotificationController getHwTaskChangeController() {
        return this.mHwTaskChangeNotificationController;
    }

    public void onAppGroupChanged(int pid, int uid, String pkgName, int oldSchedGroup, int newSchedGroup) {
        if (newSchedGroup == 3) {
            Message msg = this.mHandler.obtainMessage(70);
            msg.arg1 = pid;
            msg.arg2 = uid;
            msg.obj = pkgName;
            this.mHandler.sendMessage(msg);
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

    public boolean isInMultiWindowMode() {
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mIAmsInner.getAMSForLock()) {
                ActivityStack focusedStack = this.mIAmsInner.getStackSupervisor().getFocusedStack();
                if (focusedStack == null) {
                    Binder.restoreCallingIdentity(origId);
                    return false;
                }
                boolean inMultiWindowMode = focusedStack.inMultiWindowMode();
                Binder.restoreCallingIdentity(origId);
                return inMultiWindowMode;
            }
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(origId);
            throw th;
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

    public void notifyActivityState(ActivityRecord r, String state) {
        Message msg = this.mHandler.obtainMessage(NOTIFY_ACTIVITY_STATE);
        String activityInfo = parseActivityStateInfo(r, state);
        if (activityInfo == null) {
            Slog.e(TAG, "parse activity info error.");
            return;
        }
        msg.obj = activityInfo;
        this.mHandler.sendMessage(msg);
        if (this.mNeedRemindHwOUC && r.userId == 0 && r.isActivityTypeHome() && state.equals(ActivityStack.ActivityState.RESUMED.toString())) {
            this.mHandler.removeMessages(80);
            this.mHandler.sendEmptyMessage(80);
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
            stringBuffer.append(CPUCustBaseConfig.CPUCONFIG_INVALID_STR);
            stringBuffer.append(r.shortComponentName);
            stringBuffer.append(CPUCustBaseConfig.CPUCONFIG_INVALID_STR);
            stringBuffer.append(r.appInfo.uid);
            stringBuffer.append(CPUCustBaseConfig.CPUCONFIG_INVALID_STR);
            stringBuffer.append(r.app.pid);
            stringBuffer.append(CPUCustBaseConfig.CPUCONFIG_INVALID_STR);
            stringBuffer.append(state);
            return stringBuffer.toString();
        }
    }

    public boolean isApplyPersistAppPatch(String ssp, int uid, int userId, boolean bWillRestart, boolean evenPersistent, String reason, String action) {
        String str = reason;
        String str2 = action;
        boolean bResult = false;
        boolean bHandle = "android.intent.action.PACKAGE_REMOVED".equals(str2);
        if (!(bWillRestart && evenPersistent && str != null && str.endsWith(REASON_SYS_REPLACE)) && !bHandle) {
            return false;
        }
        String str3 = ssp;
        ApplicationInfo info = this.mIAmsInner.getPackageManagerInternal().getApplicationInfo(str3, 1152, Process.myUid(), userId);
        if (info == null) {
            return false;
        }
        ProcessRecord apprecord = this.mIAmsInner.getProcessRecord(info.processName, uid, true);
        if ((bHandle && apprecord != null && !apprecord.persistent) || apprecord == null) {
            return false;
        }
        if (!(apprecord.info == null || apprecord.info.sourceDir == null || ((apprecord.info.hwFlags & 536870912) == 0 && (info.metaData == null || !info.metaData.getBoolean(APKPATCH_META_DATA, false))))) {
            if (!apprecord.info.sourceDir.equals(info.sourceDir) && bHandle) {
                this.mIAmsInner.forceStopPackageLockedInner(str3, uid, true, false, true, true, false, userId, REASON_SYS_REPLACE);
                Slog.i("PatchService", str2 + TAG + "-----kill & restart---");
                this.mIAmsInner.startPersistApp(info, null, false, null);
            }
            bResult = true;
        }
        return bResult;
    }

    public boolean isSpecialVideoForPCMode(ActivityRecord r) {
        if (HwPCUtils.isPcCastModeInServer()) {
            HwPCMultiWindowManager multiWindowMgr = HwPCMultiWindowManager.getInstance(this.mIAmsInner.getAMSForLock());
            if (multiWindowMgr != null) {
                int stackId = r.getStack().getStackId();
                String packageName = r.packageName;
                if (packageName != null && HwPCUtils.isPcDynamicStack(stackId) && ((HwPCUtils.enabledInPad() && multiWindowMgr.isOlnyFullscreen(packageName)) || multiWindowMgr.isPortraitApp(r.getTask()))) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean canSleepForPCMode() {
        if (!(this.mIAmsInner.getStackSupervisor() instanceof HwActivityStackSupervisor)) {
            return false;
        }
        HwActivityStackSupervisor HwSupervisor = this.mIAmsInner.getStackSupervisor();
        int i = HwSupervisor.getChildCount() - 1;
        while (i >= 0) {
            ActivityDisplay display = HwSupervisor.getActivityDisplay(i);
            if (display == null || (!display.mStacks.isEmpty() && display.mAllSleepTokens.isEmpty())) {
                i--;
            } else {
                HwPCUtils.log(TAG, "the display " + display.mDisplayId + " has (" + display.mAllSleepTokens.size() + ") SleepTokens when goToSleep,mStacks " + display.mStacks);
                return true;
            }
        }
        return false;
    }

    public boolean canUpdateSleepForPCMode() {
        if (!(this.mIAmsInner.getStackSupervisor() instanceof HwActivityStackSupervisor)) {
            return false;
        }
        HwActivityStackSupervisor HwSupervisor = this.mIAmsInner.getStackSupervisor();
        int i = HwSupervisor.getChildCount() - 1;
        while (i >= 0) {
            ActivityDisplay display = HwSupervisor.getActivityDisplay(i);
            if (display == null || display.mAllSleepTokens.isEmpty()) {
                i--;
            } else {
                HwPCUtils.log(TAG, "the display " + display.mDisplayId + " has (" + display.mAllSleepTokens.size() + ") SleepTokens when updateSleep");
                return false;
            }
        }
        return true;
    }

    public String[] updateEntryPointArgsForPCMode(ProcessRecord app, String[] entryPointArgs) {
        if ((HwPCUtils.isPcCastModeInServer() || this.mVrMananger.isVRDeviceConnected()) && app.entryPointArgs != null) {
            return concat(entryPointArgs, app.entryPointArgs);
        }
        return entryPointArgs;
    }

    private static String[] concat(String[] first, String[] second) {
        String[] result = new String[(first.length + second.length)];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    public boolean isFreeFormVisible() {
        if (!HwFreeFormUtils.isFreeFormEnable()) {
            return false;
        }
        ActivityStack stack = this.mIAmsInner.getStackSupervisor().getStack(5, 1);
        if (stack == null) {
            return false;
        }
        KeyguardManager km = (KeyguardManager) this.mContext.getSystemService("keyguard");
        if (km == null || !km.isKeyguardLocked()) {
            return stack.getFreeFormStackVisible();
        }
        return false;
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
            String[] callingPkgNames = this.mContext.getPackageManager().getPackagesForUid(Binder.getCallingUid());
            if (callingPkgNames != null) {
                int length = callingPkgNames.length;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        break;
                    }
                    if (ONLY_NOTIFY_SYSTEM_USER.contains(callingPkgNames[i])) {
                        cookie.put("android.intent.extra.USER", String.valueOf(Binder.getCallingUserHandle().getIdentifier()));
                        break;
                    }
                    i++;
                }
            }
            synchronized (this.mActivityNotifiers) {
                this.mActivityNotifiers.register(notifier, cookie);
            }
        }
    }

    public void unregisterHwActivityNotifier(IHwActivityNotifier notifier) {
        if (notifier != null && this.mContext.checkCallingOrSelfPermission("com.huawei.permission.ACTIVITY_NOTIFIER_PERMISSION") == 0) {
            synchronized (this.mActivityNotifiers) {
                this.mActivityNotifiers.unregister(notifier);
            }
        }
    }

    public void notifyAppSwitch(ActivityRecord from, ActivityRecord to) {
        if (HW_SUPPORT_LAUNCHER_EXIT_ANIM && from != null && to != null && from != to && to.isActivityTypeHome() && "com.huawei.android.launcher".equals(to.packageName)) {
            String packageName = from.packageName;
            TaskRecord tr = from.getTask();
            if ("com.huawei.systemmanager".equals(packageName) && ACTION_CONFIRM_APPLOCK_CREDENTIAL.equals(from.intent.getAction())) {
                packageName = from.intent.getStringExtra("android.intent.extra.PACKAGE_NAME");
            } else if (!(tr == null || tr.getRootActivity() == null)) {
                packageName = tr.getRootActivity().packageName;
            }
            Slog.w(TAG, "setResumedActivityUncheckLocked start call, from: " + from + ", to: " + to);
            Bundle bundle = new Bundle();
            bundle.putString("package", packageName);
            bundle.putString("topPackage", from.packageName);
            bundle.putBoolean("isTransluent", from.isTransluent());
            bundle.putInt("userId", from.userId);
            bundle.putString("android.intent.extra.REASON", "returnToHome");
            bundle.putInt("android.intent.extra.user_handle", to.userId);
            Message msg = this.mHwHandler.obtainMessage(24);
            msg.obj = bundle;
            this.mHwHandler.sendMessageAtFrontOfQueue(msg);
            if (from.getConfiguration().orientation == 2 && from.appToken != null) {
                Slog.v(TAG, "takeTaskSnapShot package " + from.packageName);
                this.mIAmsInner.getAMSForLock().mWindowManager.getWindowManagerServiceEx().takeTaskSnapshot(from.appToken);
            }
        }
        if (from != null && to != null && !from.packageName.equals(to.packageName)) {
            Slog.w(TAG, "appSwitch from: " + from.packageName + " to: " + to.packageName);
            Bundle bundle2 = new Bundle();
            bundle2.putString("fromPackage", from.packageName);
            bundle2.putInt("fromUid", from.getUid());
            bundle2.putString("toPackage", to.packageName);
            bundle2.putInt("toUid", to.getUid());
            bundle2.putString("android.intent.extra.REASON", "appSwitch");
            bundle2.putInt("android.intent.extra.user_handle", this.mIAmsInner.getUserController().getCurrentUserIdLU());
            bundle2.putInt("toTaskId", to.getTask().taskId);
            bundle2.putParcelable("toActivity", to.info.getComponentName());
            bundle2.putInt("toPid", to.app.pid);
            Message msg2 = this.mHwHandler.obtainMessage(24);
            msg2.obj = bundle2;
            this.mHwHandler.sendMessage(msg2);
        }
    }

    public void dispatchActivityLifeState(ActivityRecord r, String state) {
        if (r != null) {
            Bundle bundle = new Bundle();
            TaskRecord task = r.task;
            boolean z = true;
            if (task != null) {
                if (mTranslucentWhitelists.contains(r.info.name)) {
                    int i = task.mActivities.size() - 1;
                    while (true) {
                        if (i < 0) {
                            break;
                        }
                        ActivityRecord activityRecord = (ActivityRecord) task.mActivities.get(i);
                        if (activityRecord != null && !activityRecord.finishing && activityRecord != r) {
                            r = activityRecord;
                            break;
                        }
                        i--;
                    }
                }
                bundle.putInt("taskId", task.taskId);
            }
            if (r.app != null) {
                bundle.putInt("uid", r.app.uid);
                bundle.putInt("pid", r.app.pid);
            }
            ComponentName comp = r.info.getComponentName();
            Rect bounds = r.getBounds();
            int displayMode = 0;
            if (HwFoldScreenState.isFoldScreenDevice()) {
                displayMode = ((HwFoldScreenManagerInternal) LocalServices.getService(HwFoldScreenManagerInternal.class)).getDisplayMode();
                bundle.putInt("displayMode", displayMode);
            }
            if (displayMode != 1 && (r.maxAspectRatio <= GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO || r.maxAspectRatio >= ActivityRecord.mDeviceMaxRatio || mWhitelistActivities.contains(comp.getClassName()))) {
                bounds = new Rect();
            } else if (displayMode == 1 && (r.minAspectRatio <= GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO || !r.appInfo.canChangeAspectRatio("minAspectRatio"))) {
                bounds = new Rect();
            }
            bundle.putBoolean("isTop", r == this.mIAmsInner.getLastResumedActivityRecord());
            bundle.putString("state", state);
            bundle.putParcelable("comp", comp);
            bundle.putParcelable("bounds", bounds);
            if (!mPIPWhitelists.contains(comp.getClassName()) && !r.isFloating()) {
                z = false;
            }
            bundle.putBoolean("isFloating", z);
            bundle.putFloat("maxRatio", r.maxAspectRatio);
            bundle.putBoolean("isHomeActivity", r.isActivityTypeHome());
            bundle.putString("android.intent.extra.REASON", "activityLifeState");
            bundle.putInt("android.intent.extra.user_handle", this.mIAmsInner.getUserController().getCurrentUserIdLU());
            Message msg = this.mHwHandler.obtainMessage(24);
            msg.obj = bundle;
            this.mHwHandler.sendMessage(msg);
        }
    }

    private Intent slideGetDefaultIntent() {
        Intent intent = new Intent();
        intent.setPackage("com.android.settings");
        intent.setAction("android.intent.action.MAIN");
        intent.setClassName("com.android.settings", "com.android.settings.accessibility.FirstSlideCoverDialogActivity");
        return intent;
    }

    private boolean isSupportKeyguardQuickCamera(ContentResolver resolver, int uid) {
        return Settings.Secure.getIntForUser(resolver, "keyguard_slide_open_camera_state", -1, uid) == 1;
    }

    private Intent slideGetIntentFromSetting(boolean isSecure) {
        String keyStr = isSecure ? "quick_slide_app_db_secure" : "quick_slide_app_db";
        Intent intent = null;
        ContentResolver resolver = this.mContext.getContentResolver();
        int uid = this.mIAmsInner.getUserController().getCurrentUserId();
        if (!isSecure || isSupportKeyguardQuickCamera(resolver, uid)) {
            String intentStr = Settings.Secure.getStringForUser(resolver, keyStr, uid);
            if (intentStr == null) {
                return null;
            }
            if (intentStr.equals("first_slide")) {
                return slideGetDefaultIntent();
            }
            try {
                intent = Intent.parseUri(intentStr, 0);
            } catch (Exception e) {
                Slog.e(TAG, "startActivity get intent err : " + intentStr);
            }
            return intent;
        }
        Slog.v(TAG, "slideGetIntentFromSetting skipped as not support");
        return null;
    }

    /* access modifiers changed from: private */
    public void slideOpenStartActivity() {
        boolean isSecure = this.mIAmsInner.isSleeping();
        if (!isSecure || !this.mIAmsInner.getAMSForLock().mWindowManager.isKeyguardOccluded()) {
            Intent intent = slideGetIntentFromSetting(isSecure);
            if (intent == null) {
                Slog.i(TAG, "slideOpenStartActivity get intent is null, return!");
                return;
            }
            this.mQuickSlideIntent = intent;
            this.mQuickSlideStartTime = SystemClock.uptimeMillis();
            ActivityRecord lastResumedActivity = this.mIAmsInner.getLastResumedActivityRecord();
            String lastResumedPkg = lastResumedActivity != null ? lastResumedActivity.packageName : null;
            Context context = this.mContext;
            Flog.bdReport(context, PPPOEStateMachine.PPPOE_EVENT_CODE, "{curPkgName:" + lastResumedPkg + ",startPkgName:" + intent.getPackage() + "}");
            StringBuilder sb = new StringBuilder();
            sb.append("slideOpenStartActivity lastResumedPkg:");
            sb.append(lastResumedPkg);
            sb.append(", startPkgName:");
            sb.append(intent.getPackage());
            Slog.i(TAG, sb.toString());
            if (intent.getPackage() == null || intent.getPackage().equals("no_set") || (lastResumedActivity != null && lastResumedActivity.visible && !isSecure && !intent.getPackage().equals("com.android.settings") && lastResumedActivity.packageName.equals(intent.getPackage()))) {
                Slog.i(TAG, "no_set or has been started, need not start activity! sleep " + isSecure);
                return;
            }
            if (HIVOICE_PKGNAME.equals(intent.getPackage())) {
                WindowState focusedWindow = this.mIAmsInner.getAMSForLock().mWindowManager.getFocusedWindow();
                if (this.mHiTouchSensor == null) {
                    this.mHiTouchSensor = new HiTouchSensor(this.mContext);
                }
                if (focusedWindow != null) {
                    this.mHiTouchSensor.processTonySlide(focusedWindow.getOwningPackage(), focusedWindow.getAttrs().type, intent, this.mHandler);
                }
            } else {
                this.mContext.startActivityAsUser(intent, UserHandle.CURRENT);
            }
            return;
        }
        Slog.i(TAG, "slideOpenStartActivity skip as occluded, return!");
    }

    public void slideCloseMoveActivityToBack() {
        String pkgName = this.mQuickSlideIntent != null ? this.mQuickSlideIntent.getPackage() : null;
        long curTime = SystemClock.uptimeMillis();
        long durTime = (this.mQuickSlideStartTime == 0 || curTime < this.mQuickSlideStartTime) ? 0 : curTime - this.mQuickSlideStartTime;
        Context context = this.mContext;
        Flog.bdReport(context, 653, "{durTime:" + durTime + ",pkgName:" + pkgName + "}");
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
    public void registerHallCallback() {
        if (!new CoverManager().registerHallCallback("android", 1, new IHallCallback.Stub() {
            public void onStateChange(HallState hallState) {
                long ident = Binder.clearCallingIdentity();
                try {
                    if (hallState.state == 2) {
                        HwActivityManagerServiceEx.this.slideOpenStartActivity();
                    } else if (hallState.state == 0) {
                        HwActivityManagerServiceEx.this.slideCloseMoveActivityToBack();
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
                public void onReceive(Context context, Intent intent) {
                    if (intent != null && HwActivityManagerServiceEx.HW_SYSTEM_SERVER_START.equals(intent.getAction())) {
                        Slog.i(HwActivityManagerServiceEx.TAG, "registerBroadcastReceiver");
                        HwActivityManagerServiceEx.this.registerHallCallback();
                    }
                }
            };
            IntentFilter filter = new IntentFilter();
            filter.addAction(HW_SYSTEM_SERVER_START);
            this.mContext.registerReceiverAsUser(receiver, UserHandle.ALL, filter, null, null);
        }
    }

    public void systemReady(Runnable goingCallback, TimingsTraceLog traceLog) {
        this.mSettingsObserver = new SettingsObserver(this.mHandler);
        this.mSettingsObserver.init();
        initTrustSpace();
        this.mFingerprintSettingsObserver = new FingerprintSettingsObserver();
        this.mFaceSettingsObserver = new FaceSettingsObserver();
        IntentFilter userSwitchedFilter = new IntentFilter();
        userSwitchedFilter.addAction("android.intent.action.USER_SWITCHED");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                UserController userController = HwActivityManagerServiceEx.this.mIAmsInner.getUserController();
                if (intent != null && userController != null) {
                    HwActivityManagerServiceEx.this.updateUnlockBoostStatus(userController.getCurrentUserId());
                }
            }
        }, userSwitchedFilter);
        ActivityStackSupervisor stackSupervisor = this.mIAmsInner.getStackSupervisor();
        if (!(stackSupervisor == null || stackSupervisor.getKeyguardController() == null)) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    UserController userController = HwActivityManagerServiceEx.this.mIAmsInner.getUserController();
                    if (userController != null) {
                        HwActivityManagerServiceEx.this.updateUnlockBoostStatus(userController.getCurrentUserId());
                    }
                }
            });
        }
    }

    public boolean handleANRFilterFIFO(int uid, int cmd) {
        switch (cmd) {
            case 0:
                return ANRFilter.getInstance().addUid(uid);
            case 1:
                return ANRFilter.getInstance().removeUid(uid);
            case 2:
                return ANRFilter.getInstance().checkUid(uid);
            default:
                return false;
        }
    }

    public void appEyeNotifyRecoveryEnd(boolean acNotify) {
        ERecoveryEvent endEvent = new ERecoveryEvent();
        if (acNotify) {
            endEvent.setERecoveryID(APPEYE_UIPINPUT_NOTIFY);
        } else {
            endEvent.setERecoveryID(APPEYE_UIPINPUT_KILL);
        }
        endEvent.setFaultID(APPEYE_UIPINPUT_FAULTID);
        endEvent.setState(1);
        ERecovery.eRecoveryReport(endEvent);
    }

    private void handleShowAppEyeAnrUi(int pid, int uid, String processName, String packageName) {
        Message msg = this.mHandler.obtainMessage(51);
        msg.arg1 = pid;
        msg.arg2 = uid;
        Bundle bundle = new Bundle();
        if (packageName != null) {
            bundle.putString("pkg", packageName);
        }
        if (processName != null) {
            bundle.putString("proc", processName);
        }
        msg.obj = bundle;
        msg.sendToTarget();
    }

    /* access modifiers changed from: private */
    public void showAppEyeAnrUi(Message msg) {
        ProcessRecord app;
        int i = msg.arg1;
        int uid = msg.arg2;
        String processName = ((Bundle) msg.obj).getString("proc");
        if (processName != null) {
            synchronized (this.mIAmsInner.getAMSForLock()) {
                app = this.mIAmsInner.getProcessRecord(processName, uid, true);
            }
            if (app != null) {
                appEyeAppNotResponding(app);
            } else {
                Slog.e(TAG, "showAppEyeAnrUi null!");
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0093, code lost:
        appEyeNotifyRecoveryEnd(true);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0096, code lost:
        return;
     */
    private void appEyeAppNotResponding(ProcessRecord app) {
        ProcessRecord processRecord = app;
        boolean showBackground = Settings.Secure.getInt(this.mContext.getContentResolver(), "anr_show_background", 0) != 0;
        synchronized (this.mIAmsInner.getAMSForLock()) {
            if (!showBackground) {
                try {
                    if (!app.isInterestingToUserLocked() && processRecord.pid != this.mIAmsInner.getAmsPid()) {
                        processRecord.kill("BG ANR", true);
                        appEyeNotifyRecoveryEnd(false);
                        zrHungSendEvent("recoverresult", 0, 0, processRecord.info.packageName, null, "BG Kill");
                        return;
                    }
                } catch (Throwable th) {
                    while (true) {
                        throw th;
                    }
                }
            }
            AppErrors mAppErrors = this.mIAmsInner.getAppErrors();
            if (mAppErrors != null) {
                mAppErrors.makeAppeyeAppNotRespondingLocked(processRecord, null, "AppFreeze!", null);
                Message msg = Message.obtain();
                msg.what = 2;
                msg.obj = new AppNotRespondingDialog.Data(processRecord, null, false);
                processRecord.anrType = 2;
                if (zrHungSendEvent("showanrdialog", processRecord.pid, processRecord.uid, processRecord.info.packageName, null, "appeye")) {
                    Handler mUiHandler = this.mIAmsInner.getUiHandler();
                    if (mUiHandler != null) {
                        mUiHandler.sendMessage(msg);
                    }
                }
            }
        }
    }

    public boolean zrHungSendEvent(String eventType, int pid, int uid, String packageName, String processName, String event) {
        ZrHungData data = new ZrHungData();
        if (eventType == null) {
            Slog.e(TAG, "eventType is null");
            return true;
        } else if (eventType.equals("handleshowdialog")) {
            handleShowAppEyeAnrUi(pid, uid, processName, packageName);
            return true;
        } else {
            if (pid > 0) {
                data.putInt("pid", pid);
            }
            if (uid > 0) {
                data.putInt("uid", uid);
            }
            if (packageName != null) {
                data.putString("packageName", packageName);
            }
            if (processName != null) {
                data.putString("processName", processName);
            }
            if (event != null) {
                data.putString("result", event);
            }
            data.putString("eventtype", eventType);
            if (HwServiceFactory.getZRHungService().sendEvent(data)) {
                return true;
            }
            Slog.e(TAG, "zrHungSendEvent failed!");
            return false;
        }
    }

    public boolean isTaskVisible(int id) {
        int callerUid = Binder.getCallingUid();
        if (callerUid == 1000) {
            boolean z = false;
            if (this.mIAmsInner == null) {
                return false;
            }
            ActivityStackSupervisor mStackSupervisor = this.mIAmsInner.getStackSupervisor();
            if (mStackSupervisor == null) {
                return false;
            }
            TaskRecord tr = mStackSupervisor.anyTaskForIdLocked(id);
            if (!(tr == null || tr.getTopActivity() == null || !tr.getTopActivity().visible)) {
                z = true;
            }
            return z;
        }
        throw new SecurityException("Process with uid=" + callerUid + " cannot call function isTaskVisible.");
    }

    public boolean shouldPreventStartProcess(String processName, int userId) {
        if (userId != 0) {
            for (String procName : this.mContext.getResources().getStringArray(33816583)) {
                if (procName.equals(processName)) {
                    Slog.i(TAG, processName + " is not allowed for sub user " + userId);
                    return true;
                }
            }
            long ident = Binder.clearCallingIdentity();
            try {
                UserInfo ui = this.mIAmsInner.getUserController().getUserInfo(userId);
                if (ui != null && ui.isManagedProfile()) {
                    for (String procName2 : this.mContext.getResources().getStringArray(33816584)) {
                        if (procName2.equals(processName)) {
                            Slog.i(TAG, processName + " is not allowed for afw user " + userId);
                            return true;
                        }
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
        return false;
    }

    public int getEffectiveUid(int hwHbsUid, int defaultUid) {
        int hbsCoreUid;
        int effectiveUid = defaultUid;
        if (19959 > hwHbsUid || hwHbsUid > 19999) {
            Slog.e(TAG, "Invalid HBS app uid " + hwHbsUid);
            return effectiveUid;
        }
        try {
            hbsCoreUid = Os.lstat("/data/data/com.huawei.hbs.framework").st_uid;
            Slog.i(TAG, "HBS uid found: " + hbsCoreUid);
        } catch (ErrnoException e) {
            Slog.i(TAG, "HBS uid not found");
            hbsCoreUid = -1;
        }
        if (defaultUid == hbsCoreUid) {
            Slog.i(TAG, "Change HBS app uid " + defaultUid + " -> " + hwHbsUid);
            return hwHbsUid;
        }
        Slog.i(TAG, "Invalid HBS core uid " + defaultUid);
        return effectiveUid;
    }

    public boolean isExemptedAuthority(Uri grantUri) {
        return EXEMPTED_AUTHORITIES.contains(grantUri.getAuthority());
    }

    private void setVipThreadPolicy(int oldSchedGroup, ProcessRecord app, int targetSchedGroup) {
        if (app.curSchedGroup == targetSchedGroup) {
            if (oldSchedGroup != targetSchedGroup) {
                this.mIAmsInner.getDAMonitor().setVipThread(app.pid, app.renderThreadTid, true);
            }
        } else if (oldSchedGroup == targetSchedGroup) {
            this.mIAmsInner.getDAMonitor().setVipThread(app.pid, app.renderThreadTid, false);
        }
    }

    public void setThreadSchedPolicy(int oldSchedGroup, ProcessRecord app) {
        if (app != null) {
            if (app.processName.equals(AwareIntelligentRecg.getInstance().getDefaultInputMethod())) {
                setVipThreadPolicy(oldSchedGroup, app, 2);
            } else {
                setVipThreadPolicy(oldSchedGroup, app, 3);
            }
        }
    }

    public WindowManagerPolicyConstants.PointerEventListener getPointerEventListener() {
        return new WindowManagerPolicyConstants.PointerEventListener() {
            public void onPointerEvent(MotionEvent motionEvent) {
                HwActivityManagerServiceEx.this.mIAmsInner.getDAMonitor().onPointerEvent(motionEvent.getAction());
            }
        };
    }

    public void noteActivityStart(String packageName, String processName, String activityName, int pid, int uid, boolean started) {
        if (this.mIAmsInner.getSystemReady()) {
            if (pid < 1) {
                ProcessRecord app = this.mIAmsInner.getProcessRecord(processName, uid, true);
                if (app != null) {
                    pid = app.pid;
                }
            }
            this.mIAmsInner.getDAMonitor().noteActivityStart(packageName, processName, activityName, pid, uid, started);
        }
    }

    public List<String> getPidWithUiFromUid(int uid) {
        List<String> pids = new ArrayList<>();
        synchronized (this.mIAmsInner.getPidsSelfLocked()) {
            int pidsSize = this.mIAmsInner.getPidsSelfLocked().size();
            for (int i = 0; i < pidsSize; i++) {
                ProcessRecord p = (ProcessRecord) this.mIAmsInner.getPidsSelfLocked().valueAt(i);
                if (p.uid == uid && p.pid != 0 && p.hasShownUi) {
                    pids.add(String.valueOf(p.pid));
                }
            }
        }
        return pids;
    }

    public void removePackageStopFlag(String packageName, int uid, String resolvedType, int resultCode, String requiredPermission, Bundle options, int userId) {
        if (packageName != null && options != null && options.getBoolean("fromSystemUI")) {
            Slog.d(TAG, "packageName: " + packageName + ", uid: " + uid + ", resolvedType: " + resolvedType + ", resultCode: " + resultCode + ", requiredPermission: " + requiredPermission + ", userId: " + userId);
            try {
                AppGlobals.getPackageManager().setPackageStoppedState(packageName, false, UserHandle.getUserId(uid));
            } catch (RemoteException e) {
                Slog.w(TAG, "Failed trying to unstop " + packageName + " due to  RemoteException");
            } catch (IllegalArgumentException e2) {
                Slog.w(TAG, "Failed trying to unstop package " + packageName + " due to IllegalArgumentException");
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:53:0x0107, code lost:
        return 0;
     */
    public int preloadApplication(String packageName, int userId) {
        if (Binder.getCallingUid() != 1000) {
            return -1;
        }
        synchronized (this.mIAmsInner.getAMSForLock()) {
            if (this.mIAmsInner.getConstants().CUR_MAX_CACHED_PROCESSES <= 8) {
                return -1;
            }
            IPackageManager pm = AppGlobals.getPackageManager();
            if (pm == null) {
                return -1;
            }
            ApplicationInfo appInfo = null;
            try {
                appInfo = pm.getApplicationInfo(packageName, 1152, userId);
            } catch (RemoteException e) {
                Slog.w(TAG, "Failed trying to get application info: " + packageName);
            }
            if (appInfo == null) {
                Slog.d(TAG, "preloadApplication, get application info failed, packageName = " + packageName);
                return -1;
            }
            ProcessRecord app = this.mIAmsInner.getProcessRecordLockedEx(appInfo.processName, appInfo.uid, true);
            if (app != null && app.thread != null) {
                Slog.d(TAG, "process has started, packageName:" + packageName + ", processName:" + appInfo.processName);
                return -1;
            } else if ((appInfo.flags & 9) == 9) {
                Slog.d(TAG, "preloadApplication, application is persistent, return");
                return -1;
            } else {
                if (app == null) {
                    app = this.mIAmsInner.newProcessRecordLockedEx(appInfo, null, false, 0);
                    this.mIAmsInner.updateLruProcessLockedEx(app, false, null);
                    this.mIAmsInner.updateOomAdjLockedEx();
                }
                try {
                    pm.setPackageStoppedState(packageName, false, UserHandle.getUserId(app.uid));
                } catch (RemoteException e2) {
                    Slog.w(TAG, "RemoteException, Failed trying to unstop package: " + packageName);
                } catch (IllegalArgumentException e3) {
                    Slog.w(TAG, "IllegalArgumentException, Failed trying to unstop package " + packageName);
                }
                if (app.thread == null) {
                    this.mIAmsInner.startProcessLockedEx(app, "start application", app.processName, null);
                }
            }
        }
    }

    private void reportAppForceStopMsg(int userId, String packageName, int callingPid) {
        if (checkIfPackageNameMatchesPid(callingPid, "com.android.settings")) {
            this.mIAmsInner.getDAMonitor().reportAppDiedMsg(userId, packageName, "settings");
        }
    }

    private boolean checkIfPackageNameMatchesPid(int mPid, String targetPackageName) {
        if (targetPackageName.equals(getPackageNameForPid(mPid))) {
            return true;
        }
        return false;
    }

    public void reportAppDiedMsg(int userId, String processName, int callerPid, String reason) {
        if (reason == null || !reason.contains("forceStop")) {
            this.mIAmsInner.getDAMonitor().reportAppDiedMsg(userId, processName, reason);
            return;
        }
        if (reason.contains("SystemManager")) {
            this.mIAmsInner.getDAMonitor().reportAppDiedMsg(userId, processName, "SystemManager");
        } else if (reason.contains("PowerGenie")) {
            this.mIAmsInner.getDAMonitor().reportAppDiedMsg(userId, processName, "PowerGenie");
        } else {
            reportAppForceStopMsg(userId, processName, callerPid);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0021, code lost:
        return r2;
     */
    private String getPackageNameForPid(int pid) {
        synchronized (this.mIAmsInner.getPidsSelfLocked()) {
            ProcessRecord proc = (ProcessRecord) this.mIAmsInner.getPidsSelfLocked().get(pid);
            if (proc != null) {
                String str = proc.info != null ? proc.info.packageName : "android";
            } else {
                Flog.i(100, "ProcessRecord for pid " + pid + " does not exist");
                return null;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:29:0x00b3, code lost:
        r12 = r1.mIAmsInner.getAMSForLock();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00b9, code lost:
        monitor-enter(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:?, code lost:
        r1.mIAmsInner.cleanupAppInLaunchingProvidersLockedEx(r4, true);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00bf, code lost:
        if (r19 == false) goto L_0x00c5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00c1, code lost:
        r4.killedByAm = true;
        r4.killed = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00c5, code lost:
        r4.unlinkDeathRecipient();
        r0 = r3 + "(" + r4.adjType + ")";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:?, code lost:
        r1.mIAmsInner.removeProcessLockedEx(r4, false, r18, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00ea, code lost:
        if (r19 == false) goto L_0x0149;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00ec, code lost:
        r1.mIAmsInner.getDAMonitor().killProcessGroupForQuickKill(r4.info.uid, r2.mPid);
        android.util.Slog.i(TAG, "Killing " + r5 + " (adj " + r4.curAdj + "): " + r0);
        android.util.EventLog.writeEvent(30023, new java.lang.Object[]{java.lang.Integer.valueOf(r6), java.lang.Integer.valueOf(r2.mPid), r5, java.lang.Integer.valueOf(r4.curAdj), r0});
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0149, code lost:
        monitor-exit(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x014a, code lost:
        r1.mIAmsInner.getDAMonitor().reportAppDiedMsg(r6, r5, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x0154, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x0155, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x0156, code lost:
        r13 = r18;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:?, code lost:
        monitor-exit(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x0159, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x015a, code lost:
        r0 = th;
     */
    public boolean killProcessRecordFromIAwareInternal(ProcessInfo procInfo, boolean restartservice, boolean isAsynchronous, String reason, boolean isNative, boolean needCheckAdj) {
        boolean isImportantAdj;
        ProcessInfo processInfo = procInfo;
        String str = reason;
        synchronized (this.mIAmsInner.getPidsSelfLocked()) {
            try {
                if (processInfo.mPid == this.mIAmsInner.getMyPid() || processInfo.mPid < 0) {
                    boolean z = restartservice;
                    Slog.e(TAG, "killProcessRecordFromIAware it is failed to get process record ,mUid :" + processInfo.mUid);
                    return false;
                }
                ProcessRecord proc = (ProcessRecord) this.mIAmsInner.getPidsSelfLocked().get(processInfo.mPid);
                if (proc == null) {
                    Slog.e(TAG, "killProcessRecordFromIAware this process has been killed or died before  :" + processInfo.mProcessName);
                    return false;
                }
                if (proc.curAdj > 0) {
                    if (!needCheckAdj || proc.curAdj >= 200 || AwareAppMngSort.EXEC_SERVICES.equals(proc.adjType)) {
                        isImportantAdj = false;
                        if (!isNative || !isImportantAdj) {
                            String killedProcessName = proc.processName;
                            int killedAppUserId = proc.userId;
                        } else {
                            Slog.e(TAG, "killProcessRecordFromIAware process cleaner kill process: adj changed, new adj:" + proc.curAdj + ", old adj:" + processInfo.mCurAdj + ", pid:" + processInfo.mPid + ", uid:" + processInfo.mUid + ", " + processInfo.mProcessName);
                            return false;
                        }
                    }
                }
                isImportantAdj = true;
                if (!isNative) {
                }
                String killedProcessName2 = proc.processName;
                int killedAppUserId2 = proc.userId;
            } catch (Throwable th) {
                th = th;
                throw th;
            }
        }
    }

    public boolean killProcessRecordFromMTM(ProcessInfo procInfo, boolean restartservice, String reason) {
        if (procInfo.mPid == this.mIAmsInner.getMyPid() || procInfo.mPid < 0) {
            Slog.e(TAG, "killProcessRecordFromMTM it is failed to get process record ,mUid :" + procInfo.mUid);
            return false;
        }
        synchronized (this.mIAmsInner.getPidsSelfLocked()) {
            int adj = procInfo.mCurAdj;
            ProcessRecord proc = (ProcessRecord) this.mIAmsInner.getPidsSelfLocked().get(procInfo.mPid);
            if (proc == null) {
                Slog.e(TAG, "killProcessRecordFromMTM this process has been killed or died before  :" + procInfo.mProcessName);
                return false;
            }
            synchronized (this.mIAmsInner.getAMSForLock()) {
                this.mIAmsInner.removeProcessLockedEx(proc, false, restartservice, "iAwareK[" + reason + "](adj:" + adj + ",type:" + proc.adjType + ")");
            }
            return true;
        }
    }

    public void removePackageAlarm(String pkg, List<String> tags, int targetUid) {
        synchronized (this.mIAmsInner.getAMSForLock()) {
            if (this.mIAmsInner.getAlarmService() != null) {
                this.mIAmsInner.getAlarmService().removePackageAlarm(pkg, tags, targetUid);
            } else {
                Slog.e(TAG, "removeByTag alarm instance is null");
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:40:?, code lost:
        r9.mLru = r8.mIAmsInner.getLruProcesses().lastIndexOf(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00c6, code lost:
        return true;
     */
    public boolean getProcessRecordFromMTM(ProcessInfo procInfo) {
        if (procInfo == null) {
            Slog.e(TAG, "getProcessRecordFromMTM procInfo is null");
            return false;
        }
        synchronized (this.mIAmsInner.getAMSForLock()) {
            synchronized (this.mIAmsInner.getPidsSelfLocked()) {
                if (procInfo.mPid == this.mIAmsInner.getMyPid() || procInfo.mPid < 0) {
                    Slog.e(TAG, "getProcessRecordFromMTM it is failed to get process record ,mPid :" + procInfo.mPid);
                    return false;
                }
                ProcessRecord proc = (ProcessRecord) this.mIAmsInner.getPidsSelfLocked().get(procInfo.mPid);
                if (proc == null) {
                    Slog.e(TAG, "getProcessRecordFromMTM process info is null ,mUid :" + procInfo.mPid);
                    return false;
                }
                if (procInfo.mType == 0) {
                    procInfo.mType = getAppType(procInfo.mPid, proc.info);
                }
                procInfo.mProcessName = proc.processName;
                procInfo.mCurSchedGroup = proc.curSchedGroup;
                procInfo.mCurAdj = proc.curAdj;
                procInfo.mAdjType = proc.adjType;
                procInfo.mAppUid = proc.info.uid;
                procInfo.mSetProcState = proc.setProcState;
                procInfo.mForegroundActivities = proc.foregroundActivities;
                procInfo.mForegroundServices = proc.foregroundServices;
                procInfo.mForceToForeground = proc.forcingToImportant != null;
                if (procInfo.mPackageName.size() == 0) {
                    int list_size = proc.pkgList.size();
                    for (int i = 0; i < list_size; i++) {
                        String packagename = (String) proc.pkgList.keyAt(i);
                        if (!procInfo.mPackageName.contains(packagename)) {
                            procInfo.mPackageName.add(packagename);
                        }
                    }
                }
            }
        }
    }

    public int canAppBoost(ActivityInfo aInfo, boolean isScreenOn) {
        if (isScreenOn || aInfo == null) {
            return 1;
        }
        int type = getAppType(-1, aInfo.applicationInfo);
        if (type == 4) {
            return 0;
        }
        if (1 != type) {
            String packageName = aInfo.packageName;
            if (packageName != null && !packageName.startsWith("com.huawei")) {
                return 0;
            }
        }
        return 1;
    }

    private int getAppType(int pid, ApplicationInfo info) {
        if (info == null) {
            Slog.e(TAG, "getAppType app info is null");
            return 0;
        }
        int flags = info.flags;
        try {
            int hwFlags = ((Integer) Class.forName("android.content.pm.ApplicationInfo").getField("hwFlags").get(info)).intValue();
            if (!((flags & 1) == 0 || (hwFlags & 100663296) == 0)) {
                return 3;
            }
        } catch (ClassNotFoundException e) {
            Slog.e(TAG, "getAppType exception: ClassNotFoundException");
        } catch (NoSuchFieldException e2) {
            Slog.e(TAG, "getAppType exception: NoSuchFieldException");
        } catch (IllegalArgumentException e3) {
            Slog.e(TAG, "getAppType exception: IllegalArgumentException");
        } catch (IllegalAccessException e4) {
            Slog.e(TAG, "getAppType exception: IllegalAccessException");
        }
        if (pid == Process.myPid()) {
            return 1;
        }
        if ((flags & 1) != 0) {
            return 2;
        }
        return 4;
    }

    public void setAndRestoreMaxAdjIfNeed(List<String> adjCustPkg) {
        if (adjCustPkg != null) {
            ArraySet<String> adjCustPkgSet = new ArraySet<>();
            adjCustPkgSet.addAll(adjCustPkg);
            synchronized (this.mIAmsInner.getAMSForLock()) {
                synchronized (this.mIAmsInner.getPidsSelfLocked()) {
                    int list_size = this.mIAmsInner.getPidsSelfLocked().size();
                    for (int i = 0; i < list_size; i++) {
                        ProcessRecord p = (ProcessRecord) this.mIAmsInner.getPidsSelfLocked().valueAt(i);
                        if (p != null) {
                            boolean pkgContains = false;
                            Iterator iter = p.pkgList.keySet().iterator();
                            while (true) {
                                if (!iter.hasNext()) {
                                    break;
                                } else if (adjCustPkgSet.contains((String) iter.next())) {
                                    pkgContains = true;
                                    break;
                                }
                            }
                            if (pkgContains) {
                                if (p.maxAdj > 260) {
                                    p.maxAdj = AwareDefaultConfigList.HW_PERCEPTIBLE_APP_ADJ;
                                }
                            } else if (p.maxAdj == 260) {
                                p.maxAdj = 1001;
                            }
                        }
                    }
                }
            }
        }
    }

    public void reportServiceRelationIAware(final int relationType, ServiceRecord r, ProcessRecord caller) {
        final ServiceRecord sr = r;
        final ProcessRecord pr = caller;
        this.mHandler.post(new Runnable() {
            public void run() {
                HwActivityManagerServiceEx.this.reportServiceRelationIAwareInner(relationType, sr, pr);
            }
        });
    }

    /* access modifiers changed from: private */
    public void reportServiceRelationIAwareInner(int relationType, ServiceRecord r, ProcessRecord caller) {
        if (r != null && caller != null && r.name != null && r.appInfo != null && caller.uid != r.appInfo.uid && this.mIAmsInner.getDAMonitor().isResourceNeeded(RESOURCE_APPASSOC)) {
            Bundle bundleArgs = new Bundle();
            int callerUid = caller.uid;
            int callerPid = caller.pid;
            String callerProcessName = caller.processName;
            int targetUid = r.appInfo.uid;
            String targetProcessName = r.processName;
            String compName = r.name.flattenToShortString();
            bundleArgs.putInt(ASSOC_CALL_PID, callerPid);
            bundleArgs.putInt(ASSOC_CALL_UID, callerUid);
            bundleArgs.putString(ASSOC_CALL_PROCNAME, callerProcessName);
            bundleArgs.putInt(ASSOC_TGT_UID, targetUid);
            bundleArgs.putString(ASSOC_TGT_PROCNAME, targetProcessName);
            bundleArgs.putString(ASSOC_TGT_COMPNAME, compName);
            bundleArgs.putInt(ASSOC_RELATION_TYPE, relationType);
            this.mIAmsInner.getDAMonitor().reportData(RESOURCE_APPASSOC, System.currentTimeMillis(), bundleArgs);
        }
    }

    public void reportServiceRelationIAware(int relationType, ContentProviderRecord r, ProcessRecord caller) {
        if (caller != null && r != null && r.info != null && r.name != null && caller.uid != r.uid && this.mIAmsInner.getDAMonitor().isResourceNeeded(RESOURCE_APPASSOC)) {
            if (r.proc != null) {
                synchronized (this.mAssocMap) {
                    ArrayMap<Integer, Long> pids = this.mAssocMap.get(Integer.valueOf(caller.pid));
                    if (pids != null) {
                        Long elaseTime = pids.get(Integer.valueOf(r.proc.pid));
                        if (elaseTime == null) {
                            pids.put(Integer.valueOf(r.proc.pid), Long.valueOf(SystemClock.elapsedRealtime()));
                        } else if (SystemClock.elapsedRealtime() - elaseTime.longValue() < AppHibernateCst.DELAY_ONE_MINS) {
                            return;
                        }
                    } else {
                        ArrayMap arrayMap = new ArrayMap();
                        arrayMap.put(Integer.valueOf(r.proc.pid), Long.valueOf(SystemClock.elapsedRealtime()));
                        this.mAssocMap.put(Integer.valueOf(caller.pid), arrayMap);
                    }
                }
            }
            Bundle bundleArgs = new Bundle();
            int callerUid = caller.uid;
            int callerPid = caller.pid;
            String callerProcessName = caller.processName;
            int targetUid = r.uid;
            String targetProcessName = r.info.processName;
            String compName = r.name.flattenToShortString();
            bundleArgs.putInt(ASSOC_CALL_PID, callerPid);
            bundleArgs.putInt(ASSOC_CALL_UID, callerUid);
            bundleArgs.putString(ASSOC_CALL_PROCNAME, callerProcessName);
            bundleArgs.putInt(ASSOC_TGT_UID, targetUid);
            bundleArgs.putString(ASSOC_TGT_PROCNAME, targetProcessName);
            bundleArgs.putString(ASSOC_TGT_COMPNAME, compName);
            bundleArgs.putInt(ASSOC_RELATION_TYPE, relationType);
            this.mIAmsInner.getDAMonitor().reportData(RESOURCE_APPASSOC, System.currentTimeMillis(), bundleArgs);
        }
    }

    public void reportProcessDied(int pid) {
        synchronized (this.mAssocMap) {
            this.mAssocMap.remove(Integer.valueOf(pid));
            Iterator<Map.Entry<Integer, ArrayMap<Integer, Long>>> it = this.mAssocMap.entrySet().iterator();
            while (it.hasNext()) {
                ArrayMap<Integer, Long> pids = (ArrayMap) it.next().getValue();
                pids.remove(Integer.valueOf(pid));
                if (pids.isEmpty()) {
                    it.remove();
                }
            }
        }
    }

    public void reportAssocDisable() {
        synchronized (this.mAssocMap) {
            this.mAssocMap.clear();
        }
    }

    public void reportPreviousInfo(int relationType, ProcessRecord prevProc) {
        if (prevProc != null && this.mIAmsInner.getDAMonitor().isResourceNeeded(RESOURCE_APPASSOC)) {
            int prevPid = prevProc.pid;
            int prevUid = prevProc.uid;
            Bundle bundleArgs = new Bundle();
            bundleArgs.putInt("pid", prevPid);
            bundleArgs.putInt(ASSOC_TGT_UID, prevUid);
            bundleArgs.putInt(ASSOC_RELATION_TYPE, relationType);
            this.mIAmsInner.getDAMonitor().reportData(RESOURCE_APPASSOC, System.currentTimeMillis(), bundleArgs);
        }
    }

    public void reportHomeProcess(ProcessRecord homeProc) {
        if (this.mIAmsInner.getDAMonitor().isResourceNeeded(RESOURCE_APPASSOC)) {
            int pid = 0;
            int uid = 0;
            ArrayList<String> pkgs = new ArrayList<>();
            if (homeProc != null) {
                try {
                    pid = homeProc.pid;
                    uid = homeProc.uid;
                    for (String pkg : homeProc.pkgList.keySet()) {
                        if (!pkgs.contains(pkg)) {
                            pkgs.add(pkg);
                        }
                    }
                } catch (ConcurrentModificationException e) {
                    Slog.i(TAG, "reportHomeProcess error happened.");
                }
            }
            Bundle bundleArgs = new Bundle();
            bundleArgs.putInt("pid", pid);
            bundleArgs.putInt(ASSOC_TGT_UID, uid);
            bundleArgs.putStringArrayList("pkgname", pkgs);
            bundleArgs.putInt(ASSOC_RELATION_TYPE, 11);
            this.mIAmsInner.getDAMonitor().reportData(RESOURCE_APPASSOC, System.currentTimeMillis(), bundleArgs);
        }
    }

    public void setHbsMiniAppUid(ApplicationInfo info, Intent intent) {
        if (!(info == null || intent == null || intent.getComponent() == null || !"com.huawei.hbs.framework".equals(intent.getComponent().getPackageName()))) {
            try {
                Slog.i(TAG, "This is HBS mini application, setHbsMiniAppUid");
                info.hwHbsUid = new Intent(intent).getIntExtra("AppUID", -1);
            } catch (Throwable th) {
                Slog.w(TAG, "HBS set app uid for mini app throw errors");
            }
        }
    }

    public boolean checkActivityStartForPCMode(ActivityStarter as, ActivityOptions options, ActivityRecord startActivity, ActivityStack targetStack) {
        if (as == null || startActivity == null || targetStack == null) {
            HwPCUtils.log(TAG, "null params, return true for checkActivityStartForPCMode");
            return true;
        } else if (!HwPCUtils.isPcCastModeInServer() || as.hasStartedOnOtherDisplay(startActivity, targetStack.mDisplayId) == -1) {
            return true;
        } else {
            ActivityOptions.abort(options);
            ActivityStack sourceStack = startActivity.resultTo != null ? startActivity.resultTo.getStack() : null;
            if (sourceStack != null) {
                sourceStack.sendActivityResultLocked(-1, startActivity.resultTo, startActivity.resultWho, startActivity.requestCode, 0, null);
            }
            HwPCUtils.log(TAG, "cancel activity start, act:" + startActivity + " targetStack:" + targetStack);
            return false;
        }
    }

    public boolean isProcessExistPidsSelfLocked(String processName, int uid) {
        boolean isExisted = false;
        SparseArray<ProcessRecord> pidsSelfLocked = this.mIAmsInner.getPidsSelfLocked();
        synchronized (pidsSelfLocked) {
            int pidsSize = pidsSelfLocked.size();
            int i = 0;
            while (true) {
                if (i >= pidsSize) {
                    break;
                }
                ProcessRecord p = pidsSelfLocked.valueAt(i);
                if (p != null && p.uid == uid && p.processName != null && p.processName.equals(processName)) {
                    isExisted = true;
                    break;
                }
                i++;
            }
        }
        return isExisted;
    }

    public boolean isTaskSupportResize(int taskId, boolean isFullscreen, boolean isMaximized) {
        if (!HwPCUtils.isPcCastModeInServer()) {
            return false;
        }
        HwPCMultiWindowManager multiWindowMgr = HwPCMultiWindowManager.getInstance(this.mIAmsInner.getAMSForLock());
        if (multiWindowMgr == null || !(this.mIAmsInner.getStackSupervisor() instanceof HwActivityStackSupervisor)) {
            return false;
        }
        HwActivityStackSupervisor hwSupervisor = this.mIAmsInner.getStackSupervisor();
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mIAmsInner.getAMSForLock()) {
                TaskRecord task = hwSupervisor.anyTaskForIdLocked(taskId);
                if (task == null) {
                    Binder.restoreCallingIdentity(origId);
                    return false;
                }
                boolean isSupportResize = multiWindowMgr.isSupportResize(task, isFullscreen, isMaximized);
                Binder.restoreCallingIdentity(origId);
                return isSupportResize;
            }
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(origId);
            throw th;
        }
    }

    public int getTopTaskIdInDisplay(int displayId, String pkgName, boolean invisibleAlso) {
        int topTaskIdInDisplay;
        if (displayId != 0 && HwPCUtils.isPcCastModeInServer() && !HwPCUtils.isValidExtDisplayId(displayId)) {
            Slog.e(TAG, "is not a valid pc display id");
            return -1;
        } else if (!(this.mIAmsInner.getStackSupervisor() instanceof HwActivityStackSupervisor)) {
            return -1;
        } else {
            HwActivityStackSupervisor hwSupervisor = this.mIAmsInner.getStackSupervisor();
            synchronized (this.mIAmsInner.getAMSForLock()) {
                topTaskIdInDisplay = hwSupervisor.getTopTaskIdInDisplay(displayId, pkgName, invisibleAlso);
            }
            return topTaskIdInDisplay;
        }
    }

    public Rect getPCTopTaskBounds(int displayId) {
        Rect pCTopTaskBounds;
        if (HwPCUtils.isPcCastModeInServer() && !HwPCUtils.isValidExtDisplayId(displayId)) {
            Slog.e(TAG, "is not a valid pc display id");
            return null;
        } else if (!(this.mIAmsInner.getStackSupervisor() instanceof HwActivityStackSupervisor)) {
            return null;
        } else {
            HwActivityStackSupervisor hwSupervisor = this.mIAmsInner.getStackSupervisor();
            synchronized (this.mIAmsInner.getAMSForLock()) {
                pCTopTaskBounds = hwSupervisor.getPCTopTaskBounds(displayId);
            }
            return pCTopTaskBounds;
        }
    }

    public void updateUsageStatsForPCMode(ActivityRecord component, boolean visible, UsageStatsManagerInternal usageStatsService) {
        if (component != null && HwPCUtils.isPcDynamicStack(component.getStackId()) && usageStatsService != null) {
            if (!visible) {
                usageStatsService.reportEvent(component.realActivity, component.userId, 2, HwPCUtils.getPCDisplayID());
                this.mPCUsageStats.remove(component.realActivity.toShortString());
            } else if (!this.mPCUsageStats.containsKey(component.realActivity.toShortString())) {
                usageStatsService.reportEvent(component.realActivity, component.userId, 1, HwPCUtils.getPCDisplayID());
                this.mPCUsageStats.put(component.realActivity.toShortString(), Long.valueOf(System.currentTimeMillis()));
            }
        }
    }

    public void dismissSplitScreenModeWithFinish(ActivityRecord r) {
        if (r.getWindowingMode() == 4 && r.getActivityType() == 1) {
            ActivityStackSupervisor mStackSupervisor = this.mIAmsInner.getStackSupervisor();
            if (mStackSupervisor == null) {
                Slog.w(TAG, "dismissSplitScreenModeWithFinish:mStackSupervisor not found.");
            } else if (r.info.name.contains(SPLIT_SCREEN_APP_NAME)) {
                dismissSplitScreenToPrimaryStack(mStackSupervisor);
            } else {
                ActivityStack nextTargetAs = mStackSupervisor.getNextStackInSplitSecondary(r.getStack());
                if (nextTargetAs != null) {
                    ActivityRecord nextTargetAR = nextTargetAs.topRunningActivityLocked();
                    if (nextTargetAR != null && nextTargetAR.info.name.contains(SPLIT_SCREEN_APP_NAME)) {
                        dismissSplitScreenToPrimaryStack(mStackSupervisor);
                    }
                }
            }
        }
    }

    private void dismissSplitScreenToPrimaryStack(ActivityStackSupervisor mStackSupervisor) {
        long ident = Binder.clearCallingIdentity();
        try {
            ActivityStack stack = mStackSupervisor.getDefaultDisplay().getSplitScreenPrimaryStack();
            if (stack == null) {
                Slog.w(TAG, "dismissSplitScreenToPrimaryStack: primary split-screen stack not found.");
                return;
            }
            this.mIAmsInner.getAMSForLock().mWindowManager.mShouldResetTime = true;
            this.mIAmsInner.getAMSForLock().mWindowManager.startFreezingScreen(0, 0);
            stack.moveToFront("dismissSplitScreenToPrimaryStack");
            stack.setWindowingMode(1);
            this.mIAmsInner.getAMSForLock().mWindowManager.stopFreezingScreen();
            Binder.restoreCallingIdentity(ident);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    /* JADX INFO: finally extract failed */
    public int[] changeGidsIfNeeded(ProcessRecord app, int[] gids) {
        ProcessRecord processRecord = app;
        int[] gids2 = gids;
        int userId = UserHandle.getUserId(processRecord.uid);
        boolean needAddPermission = true;
        int i = 0;
        if (!"com.huawei.securitymgr".equals(processRecord.info.packageName) || userId == 0) {
            if (IS_SUPPORT_CLONE_APP) {
                long ident = Binder.clearCallingIdentity();
                try {
                    List<UserInfo> profiles = ((UserManager) this.mContext.getSystemService("user")).getProfiles(userId);
                    if (profiles.size() > 1) {
                        Iterator<UserInfo> iterator = profiles.iterator();
                        while (iterator.hasNext()) {
                            if (iterator.next().isManagedProfile()) {
                                iterator.remove();
                            }
                        }
                        if (profiles.size() > 1) {
                            for (UserInfo ui : profiles) {
                                if (ui.id != userId) {
                                    int[] newGids = new int[(gids2.length + 2)];
                                    System.arraycopy(gids2, i, newGids, i, gids2.length);
                                    newGids[gids2.length] = UserHandle.getUserGid(ui.id);
                                    newGids[gids2.length + 1] = UserHandle.getUid(ui.id, 1023);
                                    gids2 = newGids;
                                    i = 0;
                                }
                            }
                        }
                    }
                    Binder.restoreCallingIdentity(ident);
                    if (!CLONEPROFILE_PERMISSION.contains(processRecord.info.packageName) && !HwPackageManagerServiceEx.isSupportCloneAppInCust(processRecord.info.packageName) && !processRecord.info.processName.contains("android.process.media")) {
                        needAddPermission = false;
                    }
                    if (needAddPermission && userId == 0) {
                        int[] newGids2 = new int[(gids2.length + (2 * 20))];
                        int i2 = 0;
                        System.arraycopy(gids2, 0, newGids2, 0, gids2.length);
                        while (true) {
                            int i3 = i2;
                            if (i3 >= 20) {
                                break;
                            }
                            newGids2[gids2.length + i3] = UserHandle.getUid(128 + i3, 1023);
                            newGids2[gids2.length + 20 + i3] = UserHandle.getUserGid(128 + i3);
                            i2 = i3 + 1;
                        }
                        gids2 = newGids2;
                    }
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(ident);
                    throw th;
                }
            }
            return gids2;
        }
        int[] newGids3 = new int[(gids2.length + 1)];
        System.arraycopy(gids2, 0, newGids3, 0, gids2.length);
        newGids3[gids2.length] = UserHandle.getUserGid(0);
        return newGids3;
    }

    public void startPushService() {
        File jarFile = new File("/system/framework/hwpush.jar");
        File custFile = HwCfgFilePolicy.getCfgFile("jars/hwpush.jar", 0);
        if (jarFile.exists() || (custFile != null && custFile.exists())) {
            Slog.d(TAG, "start push service");
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    Intent serviceIntent = new Intent(HwActivityManagerServiceEx.this.mContext, PushService.class);
                    serviceIntent.putExtra("startFlag", "1");
                    HwActivityManagerServiceEx.this.mContext.startService(serviceIntent);
                }
            }, HwArbitrationDEFS.DelayTimeMillisA);
        }
    }

    private final boolean isOomAdjCustomized(ProcessRecord app) {
        if (sHardCodeAppToSetOomAdjArrays.containsKey(app.info.packageName) || sHardCodeAppToSetOomAdjArrays.containsKey(app.processName)) {
            return true;
        }
        return false;
    }

    private int retrieveCustedMaxAdj(String processName) {
        int rc = -901;
        if (sHardCodeAppToSetOomAdjArrays.containsKey(processName)) {
            rc = sHardCodeAppToSetOomAdjArrays.get(processName).intValue();
        }
        Slog.i(TAG, "retrieveCustedMaxAdj for processName:" + processName + ", get adj:" + rc);
        return rc;
    }

    public void updateProcessRecordCurAdj(int adjSeq, ProcessRecord app) {
        if (adjSeq == app.adjSeq && app.curAdj > app.maxAdj && isOomAdjCustomized(app)) {
            app.curAdj = app.maxAdj;
        }
    }

    public void updateProcessRecordInfoBefStart(ProcessRecord app) {
        if (HwPCUtils.isPcCastModeInServer()) {
            if (this.mPkgDisplayMaps.containsKey(app.info.packageName)) {
                int displayId = this.mPkgDisplayMaps.get(app.info.packageName).intValue();
                if (HwPCUtils.isValidExtDisplayId(displayId)) {
                    app.mDisplayId = displayId;
                    if (app.entryPointArgs == null) {
                        app.entryPointArgs = new String[]{String.valueOf(displayId)};
                    }
                }
            }
            if (HwPCUtils.enabledInPad() != 0) {
                List<InputMethodInfo> methodList = HwPCUtils.getInputMethodList();
                if (methodList != null) {
                    int listSize = methodList.size();
                    String pkgName = null;
                    int i = 0;
                    while (true) {
                        if (listSize <= i) {
                            break;
                        }
                        InputMethodInfo mi = methodList.get(i);
                        if (mi != null) {
                            pkgName = mi.getPackageName();
                        }
                        if (pkgName != null && pkgName.equals(app.info.packageName)) {
                            app.entryPointArgs = new String[]{String.valueOf(HwPCUtils.getPCDisplayID())};
                            break;
                        }
                        i++;
                    }
                }
            }
        }
        if (this.mVrMananger.isVRDeviceConnected()) {
            Slog.e(TAG, "hwAMS startProcessLocked is VR Display is " + this.mVrMananger.getVRDisplayID());
            IVRSystemServiceManager iVRSystemServiceManager = this.mVrMananger;
            IVRSystemServiceManager iVRSystemServiceManager2 = this.mVrMananger;
            app.entryPointArgs = new String[]{String.valueOf(this.mVrMananger.getVRDisplayID()), String.valueOf(2880), String.valueOf(1600)};
        }
    }

    public void updateProcessRecordMaxAdj(ProcessRecord app) {
        if (isOomAdjCustomized(app)) {
            int custMaxAdj = retrieveCustedMaxAdj(app.processName);
            if (app.maxAdj > -800 && custMaxAdj >= -900 && custMaxAdj <= 906) {
                app.maxAdj = custMaxAdj;
                Slog.i(TAG, "addAppLocked, app:" + app + ", set maxadj to " + custMaxAdj);
            }
        }
    }

    public boolean shouldSkipSendIntentSender(IIntentSender target, Bundle options) {
        if (HwPCUtils.isPcCastModeInServer() && (target instanceof PendingIntentRecord)) {
            PendingIntentRecord.Key key = ((PendingIntentRecord) target).key;
            if (key != null) {
                int displayId = options != null ? ActivityOptions.fromBundle(options).getLaunchDisplayId() : 0;
                if (!HwPCUtils.enabledInPad() || !"com.android.incallui".equals(key.packageName) || this.mIAmsInner.isKeyguardLockedEx() || HwPCUtils.isValidExtDisplayId(displayId)) {
                    this.mPkgDisplayMaps.put(key.packageName, Integer.valueOf(displayId));
                } else {
                    Slog.d(TAG, "sendIntentSender skip when screen on, packageName: " + key.packageName + ",isKeyguardLocked: " + this.mIAmsInner.isKeyguardLockedEx() + ",displayId: " + displayId);
                    return true;
                }
            }
        }
        return false;
    }

    public HashMap<String, Integer> getPkgDisplayMaps() {
        return this.mPkgDisplayMaps;
    }

    private void initTrustSpace() {
        this.mTrustSpaceController = HwServiceFactory.getTrustSpaceController();
        if (this.mTrustSpaceController != null) {
            this.mTrustSpaceController.initTrustSpace();
        }
    }

    private boolean shouldPreventStartComponent(int type, String calleePackage, int callerUid, int callerPid, String callerPackage, int userId) {
        boolean shouldPrevent = false;
        if (!this.mIAmsInner.getSystemReady()) {
            return false;
        }
        long ident = Binder.clearCallingIdentity();
        try {
            if (this.mTrustSpaceController != null) {
                shouldPrevent = this.mTrustSpaceController.checkIntent(type, calleePackage, callerUid, callerPid, callerPackage, userId);
            }
            ISecurityProfileController spc = HwServiceFactory.getSecurityProfileController();
            if (spc != null) {
                shouldPrevent |= spc.shouldPreventInteraction(type, calleePackage, callerUid, callerPid, callerPackage, userId);
            }
            return shouldPrevent | HwSystemManagerPlugin.getInstance(this.mContext).shouldPreventStartComponent(type, calleePackage, callerUid, callerPid, callerPackage, userId);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public boolean shouldPreventStartService(ServiceInfo sInfo, int callerUid, int callerPid, String callerPackage, int userId) {
        if (sInfo == null) {
            return false;
        }
        return shouldPreventStartComponent(2, sInfo.applicationInfo.packageName, callerUid, callerPid, callerPackage, userId);
    }

    public boolean shouldPreventStartActivity(ActivityInfo aInfo, int callerUid, int callerPid, String callerPackage, int userId) {
        if (aInfo == null) {
            return false;
        }
        return shouldPreventStartComponent(0, aInfo.applicationInfo.packageName, callerUid, callerPid, callerPackage, userId);
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

    public String getTargetFromIntentForClone(Intent intent) {
        if (intent.getAction() != null) {
            return intent.getAction();
        }
        if (intent.getComponent() != null) {
            return intent.getComponent().getPackageName();
        }
        return null;
    }

    public int getCloneAppUserId(String name, int userId) {
        if (!IS_SUPPORT_CLONE_APP || userId == 0 || name == null) {
            return userId;
        }
        int newUserId = userId;
        UserController userctl = this.mIAmsInner.getUserController();
        if (userId != userctl.getCurrentUserIdLU() && sAllowedCrossUserForCloneArrays.contains(name)) {
            long ident = Binder.clearCallingIdentity();
            try {
                UserInfo ui = userctl.getUserInfo(userId);
                if (ui != null && ui.isClonedProfile()) {
                    newUserId = ui.profileGroupId;
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
        return newUserId;
    }

    public int getContentProviderUserId(String name, int userId) {
        if (!(userId == 0 || name == null || userId == this.mIAmsInner.getUserController().getCurrentUserIdLU())) {
            long ident = Binder.clearCallingIdentity();
            try {
                UserInfo ui = this.mIAmsInner.getUserController().getUserInfo(userId);
                boolean isClonedProfile = ui != null && ui.isClonedProfile();
                if (ui != null && ((IS_SUPPORT_CLONE_APP && isClonedProfile && sAllowedCrossUserForCloneArrays.contains(name)) || (ui.isManagedProfile() && "com.huawei.android.launcher.settings".equals(name)))) {
                    return ui.profileGroupId;
                }
                if (isClonedProfile) {
                    ProviderInfo cpi = null;
                    try {
                        cpi = AppGlobals.getPackageManager().resolveContentProvider(name, 0, userId);
                    } catch (RemoteException e) {
                    }
                    if (cpi == null) {
                        int i = ui.profileGroupId;
                        Binder.restoreCallingIdentity(ident);
                        return i;
                    }
                }
                Binder.restoreCallingIdentity(ident);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
        return userId;
    }

    public void checkAndPrintTestModeLog(List list, String intentAction, String callingMethod, String desciption) {
        boolean is_data_ok;
        if (Log.HWINFO && list != null) {
            PackageManager pm = this.mContext.getPackageManager();
            if ("android.provider.Telephony.SMS_RECEIVED".equals(intentAction) || "android.provider.Telephony.SMS_DELIVER".equals(intentAction)) {
                int list_size = list.size();
                String appName = null;
                String packageName = null;
                for (int ii = 0; ii < list_size; ii++) {
                    try {
                        Object myReceiver = list.get(ii);
                        if (myReceiver instanceof ResolveInfo) {
                            packageName = ((ResolveInfo) myReceiver).getComponentInfo().packageName;
                        } else if (myReceiver instanceof BroadcastFilter) {
                            packageName = ((BroadcastFilter) myReceiver).packageName;
                        }
                        appName = pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString();
                        is_data_ok = true;
                    } catch (Exception e) {
                        Log.e(TAG, "checkAndPrintTestModeLog(). error: " + e.toString());
                        is_data_ok = false;
                    }
                    if (is_data_ok) {
                        Log.i(TAG, " <" + appName + ">[" + packageName + "][" + callingMethod + "]" + desciption);
                    }
                }
                String str = packageName;
                String packageName2 = appName;
            }
        }
    }

    public boolean isHiddenSpaceSwitch(UserInfo first, UserInfo second) {
        if (first == null || second == null || first.id == second.id) {
            return false;
        }
        if (first.isHwHiddenSpace() || second.isHwHiddenSpace()) {
            Slog.i(TAG, "isHiddenSpaceSwitch!");
            return true;
        }
        Slog.i(TAG, "not hiddenSpaceSwitch");
        return false;
    }

    public void cleanAppForHiddenSpace() {
        if (MultiTaskManager.getInstance() != null) {
            MultiTaskManager.getInstance().getAppListForUserClean(new IAppCleanCallback.Stub() {
                public void onCleanFinish(AppCleanParam result) {
                    List<String> pkgNames = result.getStringList();
                    List<Integer> userIds = result.getIntList();
                    List<Integer> killTypes = result.getIntList2();
                    if (pkgNames != null && userIds != null && killTypes != null) {
                        if (pkgNames.size() <= 5) {
                            Slog.d(HwActivityManagerServiceEx.TAG, "less then 5 pkgs, abandon cleanAppForHiddenSpace");
                            return;
                        }
                        List<AppCleanParam.AppCleanInfo> appCleanInfoList = new ArrayList<>();
                        int pkgNum = pkgNames.size();
                        for (int i = 0; i < pkgNum; i++) {
                            appCleanInfoList.add(new AppCleanParam.AppCleanInfo(pkgNames.get(i), userIds.get(i), killTypes.get(i)));
                        }
                        IAppCleanCallback callback2 = new IAppCleanCallback.Stub() {
                            public void onCleanFinish(AppCleanParam result2) {
                            }
                        };
                        Slog.d(HwActivityManagerServiceEx.TAG, "executeMultiAppClean for hidden space");
                        MultiTaskManager.getInstance().executeMultiAppClean(appCleanInfoList, callback2);
                    }
                }
            });
        }
    }

    public void forceGCAfterRebooting() {
        List<ActivityManager.RunningAppProcessInfo> runningAppInfo = this.mIAmsInner.getAMSForLock().getRunningAppProcesses();
        if (runningAppInfo == null) {
            HwPCUtils.log("HwPCMultiWindowManager", "forceGCAfterRebooting-fail: runningAppInfo is null");
            return;
        }
        for (ActivityManager.RunningAppProcessInfo appProcess : runningAppInfo) {
            Process.sendSignal(appProcess.pid, 10);
        }
    }

    public boolean isLimitedPackageBroadcast(Intent intent) {
        String action = intent.getAction();
        boolean limitedPackageBroadcast = false;
        if (!"android.intent.action.PACKAGE_ADDED".equals(action) && !"android.intent.action.PACKAGE_REMOVED".equals(action)) {
            return false;
        }
        Bundle intentExtras = intent.getExtras();
        if (intentExtras != null) {
            limitedPackageBroadcast = intentExtras.getBoolean("LimitedPackageBroadcast", false);
        }
        Flog.d(100, "Android Wear-isLimitedPackageBroadcast: limitedPackageBroadcast = " + limitedPackageBroadcast);
        return limitedPackageBroadcast;
    }

    public int getUidByUriAuthority(Uri uri, int uid) {
        if (IS_SUPPORT_CLONE_APP && uri != null && UserHandle.getUserId(uid) != this.mIAmsInner.getUserController().getCurrentUserIdLU() && sAllowedCrossUserForCloneArrays.contains(uri.getAuthority())) {
            UserInfo ui = this.mIAmsInner.getUserController().getUserInfo(UserHandle.getUserId(uid));
            if (ui != null && ui.isClonedProfile()) {
                return UserHandle.getUid(ui.profileGroupId, uid);
            }
        }
        return uid;
    }

    public int getCaptionState(IBinder token) {
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mIAmsInner.getAMSForLock()) {
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                if (r == null) {
                    Binder.restoreCallingIdentity(ident);
                    return 0;
                } else if (HwFreeFormUtils.sHideCaptionActivity.contains(r.info.getComponentName().flattenToString())) {
                    Binder.restoreCallingIdentity(ident);
                    return 8;
                } else {
                    Binder.restoreCallingIdentity(ident);
                    return 0;
                }
            }
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
            throw th;
        }
    }

    public int getActivityWindowMode(IBinder token) {
        synchronized (this.mIAmsInner.getAMSForLock()) {
            ActivityStack stack = ActivityRecord.getStackLocked(token);
            if (stack == null) {
                return 0;
            }
            int windowingMode = stack.getWindowingMode();
            return windowingMode;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:31:0x005e, code lost:
        android.os.Binder.restoreCallingIdentity(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0061, code lost:
        return;
     */
    public void updateFreeFormOutLine(int colorState) {
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mIAmsInner.getAMSForLock()) {
                ActivityStack freeformStack = this.mIAmsInner.getStackSupervisor().getStack(5, 1);
                if (freeformStack != null && freeformStack.getFreeFormStackVisible()) {
                    ActivityRecord activity = freeformStack.topRunningActivityLocked();
                    if (activity == null) {
                        Slog.w(TAG, "updateFreeFormOutLine failed: no top activity");
                        return;
                    } else if (activity.task != null && activity.task.getStack() != null && activity.app != null && activity.app.thread != null) {
                        try {
                            activity.app.thread.scheduleFreeFormOutLineChanged(activity.appToken, colorState);
                        } catch (RemoteException e) {
                            Slog.e(TAG, "scheduleFreeFormOutLineChanged error!");
                        }
                    }
                }
                Binder.restoreCallingIdentity(ident);
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private boolean isStartFromPendingIntent(String packageName, int userId) {
        boolean isFromPengingIntent = false;
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        if (this.mIAmsInner.getAMSForLock().mIntentSenderRecords.size() > 0) {
            Iterator<WeakReference<PendingIntentRecord>> it = this.mIAmsInner.getAMSForLock().mIntentSenderRecords.values().iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                WeakReference<PendingIntentRecord> wpir = it.next();
                if (wpir != null) {
                    PendingIntentRecord pir = (PendingIntentRecord) wpir.get();
                    if (packageName.equals(pir.key.packageName) && userId == pir.key.userId && pir.sent && System.currentTimeMillis() - pir.sendTime < 2000) {
                        isFromPengingIntent = true;
                        break;
                    }
                }
            }
        }
        return isFromPengingIntent;
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
            boolean isNotTop = (topActivity == null || aInfo.packageName == null || aInfo.packageName.equals(topActivity.packageName)) ? false : true;
            if (!callerPkg.equals(aInfo.packageName) || !isNotTop) {
                return true;
            }
            activityMode = 4;
        }
        if (aInfo.applicationInfo != null) {
            uid = aInfo.applicationInfo.uid;
        }
        boolean hsmCheck = HwAddViewHelper.getInstance(context).addViewPermissionCheck(aInfo.packageName, activityMode, uid);
        if (hsmCheck || activityMode != 4 || !isStartFromPendingIntent(aInfo.packageName, UserHandle.getUserId(uid))) {
            if (!hsmCheck) {
                Slog.i(TAG, "isAllowToStartActivity:" + hsmCheck + ", activityMode:" + activityMode + ", callerPkg:" + callerPkg + ", destInfo:" + aInfo + ", topActivity:" + topActivity);
            }
            return hsmCheck;
        }
        Slog.i(TAG, "starting activity through the pendingintent!");
        return true;
    }

    private void addPickColorBlackList(String pkgName) {
        if (pkgName != null && !this.pickColorBlackList.contains(pkgName)) {
            this.pickColorBlackList.add(pkgName);
        }
    }

    private void loadPickColorBlackList() {
        InputStream inputStream = null;
        try {
            File file = HwCfgFilePolicy.getCfgFile("xml/pickcolor_blacklist.xml", 0);
            if (!file.exists()) {
                Log.i(TAG, "file not exist!");
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        Log.e(TAG, "load notch screen config: IO Exception while closing stream", e);
                    }
                }
                return;
            }
            InputStream inputStream2 = new FileInputStream(file);
            XmlPullParser xmlParser = Xml.newPullParser();
            xmlParser.setInput(inputStream2, null);
            int xmlEventType = xmlParser.next();
            while (true) {
                if (xmlEventType != 1) {
                    if (xmlEventType != 2 || !XML_NOT_PICKCOLOR_APP.equals(xmlParser.getName())) {
                        if (xmlEventType == 3 && XML_PICKCOLOR_BLACK_LIST.equals(xmlParser.getName())) {
                            break;
                        }
                    } else {
                        addPickColorBlackList(xmlParser.getAttributeValue(null, "name"));
                    }
                    xmlEventType = xmlParser.next();
                }
            }
            try {
                inputStream2.close();
            } catch (IOException e2) {
                Log.e(TAG, "load notch screen config: IO Exception while closing stream", e2);
            }
        } catch (Exception e3) {
            Log.d(TAG, "load PickColorBlackList error!");
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e4) {
                    Log.e(TAG, "load notch screen config: IO Exception while closing stream", e4);
                }
            }
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e5) {
                    Log.e(TAG, "load notch screen config: IO Exception while closing stream", e5);
                }
            }
            throw th;
        }
    }

    private void initData() {
        this.pickColorBlackList = new ArrayList();
        loadPickColorBlackList();
    }

    public boolean canPickColor(String pkgName) {
        if (pkgName == null) {
            return false;
        }
        if (this.pickColorBlackList == null) {
            initData();
        }
        if (this.pickColorBlackList.contains(pkgName)) {
            return false;
        }
        return true;
    }

    public Intent changeStartActivityIfNeed(Intent intent) {
        if (!IS_BOPD || intent == null || Settings.Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) == 0) {
            return intent;
        }
        intent.removeCategory("android.intent.category.HOME");
        intent.addFlags(4194304);
        intent.setComponent(new ComponentName("com.huawei.KoBackup", "com.huawei.KoBackup.EmergencyBackupActivity"));
        return intent;
    }

    public boolean customActivityResuming(String packageName) {
        ISecurityProfileController spc = HwServiceFactory.getSecurityProfileController();
        if (spc != null) {
            spc.handleActivityResuming(packageName);
        }
        return false;
    }

    public void dismissSplitScreenToFocusedStack() {
        this.mIAmsInner.getAMSForLock().enforceCallerIsRecentsOrHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", "dismissSplitScreenToFocusedStack()");
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mIAmsInner.getAMSForLock()) {
                ActivityStackSupervisor stackSupervisor = this.mIAmsInner.getStackSupervisor();
                if (stackSupervisor == null) {
                    Slog.e(TAG, "dismissSplitScreenToFocusedStack:stackSupervisor not found.");
                    Binder.restoreCallingIdentity(ident);
                    return;
                }
                ActivityStack stack = stackSupervisor.getDefaultDisplay().getSplitScreenPrimaryStack();
                if (stack == null) {
                    Slog.e(TAG, "dismissSplitScreenToFocusedStack: primary split-screen stack not found.");
                    Binder.restoreCallingIdentity(ident);
                    return;
                }
                ActivityStack focusStack = stackSupervisor.getFocusedStack();
                if (focusStack == null) {
                    Slog.e(TAG, "dismissSplitScreenToFocusedStack: focusStack == null return");
                    Binder.restoreCallingIdentity(ident);
                    return;
                }
                focusStack.moveToFront("dismissSplitScreenModeToFocusedStack");
                stack.setWindowingMode(1);
                Binder.restoreCallingIdentity(ident);
            }
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
            throw th;
        }
    }

    public boolean enterCoordinationMode(Intent intent) {
        int currentState = ((DisplayManagerInternal) LocalServices.getService(DisplayManagerInternal.class)).getDisplayMode();
        boolean z = false;
        if (!HwFoldScreenState.isFoldScreenDevice() || currentState == 1 || currentState == 4 || currentState == 0) {
            return false;
        }
        Slog.d(TAG, "enterCoordinationMode");
        synchronized (this.mIAmsInner.getAMSForLock()) {
            long ident = Binder.clearCallingIdentity();
            try {
                ActivityStack stack = this.mIAmsInner.getStackSupervisor().getFocusedStack();
                if (stack == null) {
                    Slog.w(TAG, "enterCoordinationMode: No stack:" + stack);
                    return false;
                }
                CoordinationModeUtils utils = CoordinationModeUtils.getInstance(this.mContext);
                if (utils.isEnterOrExitCoordinationMode()) {
                    Slog.w(TAG, "enterCoordinationMode: already in CoordinationMode");
                    Binder.restoreCallingIdentity(ident);
                    return false;
                }
                utils.setCoordinationState(1);
                setDisplayMode(4);
                setCoordinationCreateMode(currentState);
                stack.moveToFront("enterCoordinationMode", null);
                stack.setWindowingMode(11);
                Rect dockedBounds = new Rect();
                utils.getStackCoordinationModeBounds(true, this.mIAmsInner.getAMSForLock().mWindowManager.getDefaultDisplayContentLocked().getDisplayInfo().rotation, dockedBounds);
                resizeCoordinationStackLocked(dockedBounds);
                this.mKeepPrimaryCoordinationResumed = true;
                CoordinationStackDividerManager instance = CoordinationStackDividerManager.getInstance(this.mContext);
                if (this.mContext.getResources().getConfiguration().orientation == 2) {
                    z = true;
                }
                instance.addDividerView(z);
                intent.addFlags(268435456);
                intent.addHwFlags(8192);
                ActivityOptions opts = ActivityOptions.makeBasic();
                opts.setLaunchWindowingMode(12);
                this.mContext.startActivity(intent, opts.toBundle());
                utils.setCoordinationState(2);
                Slog.d(TAG, "enterCoordinationMode over");
                Binder.restoreCallingIdentity(ident);
                return true;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    private void setDisplayMode(int state) {
        HwFoldScreenManagerInternal fsm = (HwFoldScreenManagerInternal) LocalServices.getService(HwFoldScreenManagerInternal.class);
        if (fsm != null) {
            fsm.setDisplayMode(state);
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
        Rect otherTaskRect;
        Rect tempRect;
        int i;
        boolean z;
        ActivityStackSupervisor stackSupervisor = this.mIAmsInner.getStackSupervisor();
        ActivityDisplay display = stackSupervisor.getDefaultDisplay();
        ActivityStack coordinationPrimaryStack = display.getCoordinationPrimaryStack();
        if (coordinationPrimaryStack == null) {
            Slog.w(TAG, "resizeCoordinationStackLocked: coordinationPrimaryStack not found");
            return;
        }
        WindowManagerService windowManager = this.mIAmsInner.getAMSForLock().mWindowManager;
        windowManager.deferSurfaceLayout();
        try {
            ActivityRecord r = coordinationPrimaryStack.topRunningActivityLocked();
            Rect rect = null;
            try {
                coordinationPrimaryStack.resize(dockedBounds, null, null);
                Rect otherTaskRect2 = new Rect();
                Rect tempRect2 = new Rect();
                boolean z2 = true;
                int i2 = display.getChildCount() - 1;
                while (true) {
                    int i3 = i2;
                    if (i3 >= 0) {
                        ActivityStack current = display.getChildAt(i3);
                        if (current.getWindowingMode() == 11) {
                            i = i3;
                            z = z2;
                            tempRect = tempRect2;
                            otherTaskRect = otherTaskRect2;
                        } else {
                            current.setWindowingMode(12);
                            current.getStackDockedModeBounds(rect, tempRect2, otherTaskRect2, z2);
                            Rect rect2 = !tempRect2.isEmpty() ? tempRect2 : rect;
                            ActivityStack activityStack = current;
                            ActivityStack activityStack2 = current;
                            Rect rect3 = !otherTaskRect2.isEmpty() ? otherTaskRect2 : rect;
                            i = i3;
                            z = z2;
                            tempRect = tempRect2;
                            otherTaskRect = otherTaskRect2;
                            stackSupervisor.resizeStackLocked(activityStack, rect2, rect3, null, true, true, false);
                        }
                        i2 = i - 1;
                        z2 = z;
                        tempRect2 = tempRect;
                        otherTaskRect2 = otherTaskRect;
                        rect = null;
                    } else {
                        Rect rect4 = tempRect2;
                        Rect rect5 = otherTaskRect2;
                        coordinationPrimaryStack.ensureVisibleActivitiesConfigurationLocked(r, z2);
                        windowManager.continueSurfaceLayout();
                        return;
                    }
                }
            } catch (Throwable th) {
                th = th;
                windowManager.continueSurfaceLayout();
                throw th;
            }
        } catch (Throwable th2) {
            th = th2;
            Rect rect6 = dockedBounds;
            windowManager.continueSurfaceLayout();
            throw th;
        }
    }

    public boolean exitCoordinationModeInner(boolean toTop, boolean changeMode) {
        synchronized (this.mIAmsInner.getAMSForLock()) {
            long ident = Binder.clearCallingIdentity();
            try {
                Slog.d(TAG, "exitCoordinationMode");
                ActivityDisplay display = this.mIAmsInner.getStackSupervisor().getDefaultDisplay();
                ActivityStack primaryCoordinationStack = display.getCoordinationPrimaryStack();
                ActivityStack secondaryCoordinationStack = display.getTopStackInWindowingMode(12);
                if (primaryCoordinationStack != null) {
                    if (secondaryCoordinationStack != null) {
                        CoordinationModeUtils utils = CoordinationModeUtils.getInstance(this.mContext);
                        if (utils.isEnterOrExitCoordinationMode()) {
                            Slog.w(TAG, "exitCoordinationMode: not in CoordinationMode");
                            Binder.restoreCallingIdentity(ident);
                            return false;
                        }
                        int currentCreateMode = utils.getCoordinationCreateMode();
                        utils.setCoordinationState(3);
                        secondaryCoordinationStack.finishAllActivitiesLocked(true);
                        if (changeMode) {
                            if (currentCreateMode == 3) {
                                setDisplayMode(3);
                            } else if (currentCreateMode == 4) {
                                setDisplayMode(2);
                            }
                        }
                        utils.setCoordinationCreateMode(0);
                        if (toTop) {
                            primaryCoordinationStack.moveToFront("exitCoordinationMode");
                        }
                        primaryCoordinationStack.setWindowingMode(1);
                        CoordinationStackDividerManager.getInstance(this.mContext).removeDividerView();
                        utils.setCoordinationState(4);
                        Slog.d(TAG, "exitCoordinationMode over");
                        Binder.restoreCallingIdentity(ident);
                        return true;
                    }
                }
                Slog.w(TAG, "exitCoordinationMode: docked stack not found primaryCoordinationStack:" + primaryCoordinationStack + " secondaryCoordinationStack:" + secondaryCoordinationStack);
                return false;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    public void resumeCoordinationPrimaryStack(ActivityRecord r) {
        if (r != null && this.mKeepPrimaryCoordinationResumed) {
            ActivityStackSupervisor stackSupervisor = this.mIAmsInner.getStackSupervisor();
            ActivityDisplay display = stackSupervisor.getDefaultDisplay();
            ActivityStack primaryCoordinationStack = display.getCoordinationPrimaryStack();
            ActivityStack secondaryCoordinationStack = display.getTopStackInWindowingMode(12);
            if (primaryCoordinationStack == null || r.getStackId() != secondaryCoordinationStack.mStackId) {
                this.mKeepPrimaryCoordinationResumed = false;
            } else {
                ActivityRecord ar = primaryCoordinationStack.topRunningActivityLocked();
                this.mKeepPrimaryCoordinationResumed = false;
                if (stackSupervisor.moveFocusableActivityStackToFrontLocked(ar, "setFocusedTask")) {
                    secondaryCoordinationStack.startPausingLocked(false, false, null, true);
                }
            }
        }
    }

    public boolean shouldResumeCoordinationPrimaryStack() {
        return this.mKeepPrimaryCoordinationResumed;
    }

    /* access modifiers changed from: private */
    public void updateUnlockBoostStatus(int userId) {
        boolean fpEnabled;
        ActivityStackSupervisor stackSupervisor = this.mIAmsInner.getStackSupervisor();
        if (stackSupervisor != null) {
            KeyguardController keyguardController = stackSupervisor.getKeyguardController();
            if (keyguardController != null) {
                boolean faceEnabled = true;
                Slog.i(TAG, "update fingerprint unlock boost status [" + fpEnabled + "]");
                if (Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "face_bind_with_lock", 0, userId) == 0) {
                    faceEnabled = false;
                }
                Slog.i(TAG, "update face unlock boost status [" + faceEnabled + "]");
                keyguardController.setFingerprintUnlockBoostStatus(fpEnabled | faceEnabled);
            }
        }
    }
}
