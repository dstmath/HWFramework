package com.android.server.am;

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.AppGlobals;
import android.app.IApplicationThread;
import android.app.IServiceConnection;
import android.app.IUiModeManager;
import android.app.KeyguardManager;
import android.app.mtm.MultiTaskManager;
import android.app.mtm.MultiTaskUtils;
import android.app.mtm.iaware.appmng.AppCleanParam;
import android.app.mtm.iaware.appmng.IAppCleanCallback;
import android.common.HwFrameworkFactory;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.hwrme.HwPrommEventManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.FactoryTest;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.CoordinationModeUtils;
import android.util.ERecovery;
import android.util.ERecoveryEvent;
import android.util.EventLog;
import android.util.Flog;
import android.util.HwMwUtils;
import android.util.HwPCUtils;
import android.util.Log;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.TimingsTraceLog;
import android.util.Xml;
import android.view.MotionEvent;
import android.view.WindowManagerPolicyConstants;
import android.view.inputmethod.InputMethodInfo;
import android.vrsystem.IVRSystemServiceManager;
import android.widget.Toast;
import android.zrhung.IZrHung;
import android.zrhung.ZrHungData;
import com.android.internal.os.ProcessCpuTracker;
import com.android.internal.os.ZygoteInit;
import com.android.internal.util.MemInfoReader;
import com.android.server.CoordinationStackDividerManager;
import com.android.server.HwServiceFactory;
import com.android.server.LocalServices;
import com.android.server.ServiceThread;
import com.android.server.UiThread;
import com.android.server.Watchdog;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.AppNotRespondingDialog;
import com.android.server.am.PendingIntentRecord;
import com.android.server.appactcontrol.AppActConstant;
import com.android.server.cust.utils.ForbidShellFuncUtil;
import com.android.server.cust.utils.HwCustPkgNameConstant;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.android.server.lights.LightsManagerEx;
import com.android.server.mtm.iaware.appmng.appstart.datamgr.BaseAppStartupInfo;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.os.HwBootCheck;
import com.android.server.pm.HwPackageManagerService;
import com.android.server.pm.HwThemeInstaller;
import com.android.server.pm.PackageManagerService;
import com.android.server.pm.auth.HwCertification;
import com.android.server.security.securityprofile.ISecurityProfileController;
import com.android.server.security.securityprofile.IntentCaller;
import com.android.server.security.trustspace.ITrustSpaceController;
import com.android.server.wifipro.WifiProCommonUtils;
import com.android.server.wm.ActivityTaskManagerService;
import com.android.server.wm.ActivityTaskManagerServiceEx;
import com.android.server.wm.DefaultHwPCMultiWindowManager;
import com.android.server.wm.WindowManagerInternal;
import com.android.server.wm.WindowProcessController;
import com.android.server.zrhung.IZRHungService;
import com.huawei.android.audio.HwAudioServiceManager;
import com.huawei.android.fsm.HwFoldScreenManagerInternal;
import com.huawei.android.pushagentproxy.PushService;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import com.huawei.hsm.permission.ANRFilter;
import com.huawei.server.HwPCFactory;
import com.huawei.server.HwPartIawareUtil;
import com.huawei.server.hwmultidisplay.DefaultHwMultiDisplayUtils;
import com.huawei.server.rme.hyperhold.AliveReport;
import com.huawei.server.rme.hyperhold.SceneConst;
import com.huawei.server.rme.hyperhold.SceneProcessing;
import com.huawei.server.rme.hyperhold.Swap;
import com.huawei.server.security.HwServiceSecurityPartsFactoryEx;
import com.huawei.server.wm.WindowProcessControllerEx;
import com.msic.qarth.PatchStore;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public final class HwActivityManagerServiceEx implements IHwActivityManagerServiceEx {
    private static final String APKPATCH_META_DATA = "android.huawei.MARKETED_SYSTEM_APP";
    private static final long APPEYE_UIPINPUT_FAULTID = 901004006;
    private static final long APPEYE_UIPINPUT_KILL = 401007005;
    private static final long APPEYE_UIPINPUT_NOTIFY = 401007006;
    private static final String ASSOC_CALL_PID = "callPid";
    private static final String ASSOC_CALL_PROCNAME = "callProcName";
    private static final String ASSOC_CALL_UID = "callUid";
    private static final String ASSOC_HWFLAG = "hwFlag";
    private static final String ASSOC_RELATION_TYPE = "relationType";
    private static final int ASSOC_REPORT_MIN_TIME = 60000;
    private static final String ASSOC_TGT_COMPNAME = "compName";
    private static final String ASSOC_TGT_PROCNAME = "tgtProcName";
    private static final String ASSOC_TGT_UID = "tgtUid";
    private static final String BLACKLIST_PACKAGENAME = "packageName";
    private static final String BLACKLIST_URI = "content://com.huawei.tvsystemmanager.provider/blacklist";
    private static final int CACHED_PROCESS_LIMIT = 8;
    private static final Set<String> CLONEPROFILE_PERMISSION = new HashSet<String>() {
        /* class com.android.server.am.HwActivityManagerServiceEx.AnonymousClass1 */

        {
            add("com.huawei.hidisk");
            add("com.huawei.filemanager");
            add("com.android.gallery3d");
            add("com.huawei.photos");
            add("com.hicloud.android.clone");
            add("com.huawei.KoBackup");
            add("com.huawei.localBackup");
            add("com.huawei.phoneClone");
        }
    };
    private static final String DELAY_APPS_FILENAME = SystemProperties.get("hw_mc.systemserver.delay_apps", "/hw_product/etc/xml/delay_app_list.xml");
    private static final int DELAY_APP_NAME_INDEX = 0;
    private static final int DELAY_TIME_INDEX = 1;
    private static final String DESK_CLOCK = "com.android.deskclock";
    private static final String DESK_CLOCK_NEW = "com.huawei.deskclock";
    private static final int DOMESTIC_BETA = 3;
    private static final boolean ENABLE_ANIMATION_RTG = SystemProperties.getBoolean("persist.sys.hw_mc.iaware.animation_rtg_enable", true);
    private static final String FLAG_FROM_PC_NOTIFICATION = "flag_from_pc_notification";
    private static final String HITOUCH_PROCESS_NAME = "com.huawei.hitouch";
    private static final String HW_LAUNCHER_PKGNAME = "com.huawei.android.launcher";
    private static final String HW_SCREEN_RECORDER = "com.huawei.screenrecorder";
    private static final String INSTANTSHARE_PROCESS_NAME = "com.huawei.android.instantshare";
    private static final boolean IS_BETA;
    private static final boolean IS_HWAMSEX_THREAD_DISABLED = SystemProperties.getBoolean("ro.config.hwamsexthread.disable", false);
    private static final boolean IS_SUPPORT_CLONE_APP = SystemProperties.getBoolean("ro.config.hw_support_clone_app", false);
    private static final boolean IS_SYSTEMUI_DIE_HANDLE = SystemProperties.getBoolean("config.hw.systemui.die.handle", false);
    private static final boolean IS_TV = "tv".equals(SystemProperties.get("ro.build.characteristics", AppActConstant.VALUE_DEFAULT));
    static final int KILL_APPLICATION_MSG = 22;
    private static final String MEDIA_PROCESS_NAME = "android.process.media";
    private static final int MEMORY_MONITOR_INTERVAL = 1800000;
    static final int MEMORY_MONITOR_MSG = 23;
    private static final int MIN_CLEAN_PKG = 5;
    private static final String PACKAGE_PRIVATE_SPACE = "com.huawei.privatespace";
    private static final int PERSISTENT_MASK = 9;
    private static final String PICKCOLOR_BLACK_LIST = "pickcolor_blacklist.xml";
    private static final int PRIMARY_SYSTEM_GID = 1000;
    private static final Set<String> PROCESS_NAME_IN_REPAIR_MODE = new HashSet<String>() {
        /* class com.android.server.am.HwActivityManagerServiceEx.AnonymousClass2 */

        {
            add("com.huawei.ddtTest");
            add("com.huawei.morpheus");
            add("com.huawei.hwdetectrepair");
        }
    };
    private static final int QUERY_BLACKLIST_DELAY_TIME = 5000;
    private static final String REASON_SYS_REPLACE = "replace sys pkg";
    private static final HashSet<String> RECLAIM_PROCESSES = new HashSet<String>() {
        /* class com.android.server.am.HwActivityManagerServiceEx.AnonymousClass3 */

        {
            add("system");
            add("com.huawei.systemserver");
        }
    };
    private static final int REPAIR_MODE_SYSTEM_UID = 12701000;
    private static final String RESOURCE_APPASSOC = "RESOURCE_APPASSOC";
    private static final boolean RTG_FRAME_ENABLE = SystemProperties.getBoolean("persist.sys.iaware.rtg.frame", true);
    private static final String SETTING_GUEST_HAS_LOGGED_IN = "guest_has_logged_in";
    private static final int SHOW_APPFREEZE_DIALOG_MSG = 51;
    private static final int SHOW_GUEST_SWITCH_DIALOG_MSG = 50;
    private static final int SHOW_SWITCH_DIALOG_MSG = 49;
    private static final int SLIM_CMD_INTERVAL = 5000;
    private static final int SLIM_CMD_TIMES = 10;
    private static final long SLIM_MEMORY_THRESHOLD = 8388608;
    private static final int STABLE_PROVIDER_IN = 2;
    private static final int STABLE_PROVIDER_OUT = 1;
    private static final int START_HW_SERVICE_POST_MSG_DELAY = 30000;
    private static final int STOCK_PM_FLAGS = 1024;
    private static final int SYSTEMUI_DIE_DEFAULT_COUNT = 1;
    private static final long SYSTEMUI_DIE_INTERVAL_TIME = 20000;
    private static final int SYSTEMUI_DIE_THRESHOLD = 5;
    static final String TAG = "HwActivityManagerServiceEx";
    private static final Map<Integer, ProcessRecord> TOP_APP_MAP = new ArrayMap();
    private static final String TV_SYSTEM_MANAGER_PACKAGENAME = "com.huawei.tvsystemmanager";
    private static final int TYPE_APP_START = 4;
    private static final int TYPE_REMOTE_ANIMATION_END = 1;
    private static final int TYPE_REMOTE_ANIMATION_START = 0;
    private static final int TYPE_SET_RENDER = 2;
    private static final String XML_ATTRIBUTE_NAME = "name";
    private static final String XML_NOT_PICKCOLOR_APP = "nopick_app";
    private static final String XML_PICKCOLOR_BLACK_LIST = "pickcolor_blacklist";
    private static boolean isTv = false;
    private static final IZrHung mAppEyeBinderBlock = HwFrameworkFactory.getZrHung("appeye_ssbinderfull");
    private static Set<String> sAllowedCrossUserForCloneArrays = new HashSet();
    private static List<String> sAppBlackList = new ArrayList();
    private static HashMap<String, Integer> sHardCodeAppToSetOomAdjArrays = new HashMap<>();
    private static DefaultHwMultiDisplayUtils sHwMultiDisplayUtils = HwPCFactory.getHwPCFactory().getHwPCFactoryImpl().getHwMultiDisplayUtils();
    private static int sSlimTimes = 10;
    private boolean doColdStartAppFromLauncher = false;
    private final SparseBooleanArray mAssocInStablePrds = new SparseBooleanArray();
    private final ArrayMap<Integer, ArrayMap<Integer, Long>> mAssocMap = new ArrayMap<>();
    private final SparseBooleanArray mAssocOutStablePrds = new SparseBooleanArray();
    BlackListObserver mBlackListObserver;
    final Context mContext;
    Handler mHandler = new Handler() {
        /* class com.android.server.am.HwActivityManagerServiceEx.AnonymousClass5 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
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
                    return;
            }
        }
    };
    Handler mHwHandler = null;
    ServiceThread mHwHandlerThread = null;
    private HwServiceHooker mHwServiceHooker;
    IHwActivityManagerInner mIAmsInner = null;
    private AtomicBoolean mIsAssocEnable = new AtomicBoolean(true);
    private AtomicBoolean mIsIawareEnable = null;
    private KeyguardManager mKeyguardManager;
    private ResetSessionDialog mNewSessionDialog;
    private int mRemoteAnimationPid = -1;
    private int mSrvFlagLocked = 0;
    ITrustSpaceController mTrustSpaceController;
    private IVRSystemServiceManager mVrMananger;
    private List<String> pickColorBlackList = null;
    private int systemuiDieCount = 0;
    private long systemuiDieLastTime = 0;

    static /* synthetic */ int access$610() {
        int i = sSlimTimes;
        sSlimTimes = i - 1;
        return i;
    }

    static {
        boolean z = false;
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
        sAllowedCrossUserForCloneArrays.add(HwThemeInstaller.HWT_OLD_CONTACT);
        sAllowedCrossUserForCloneArrays.add(HwThemeInstaller.HWT_NEW_CONTACT);
        sAllowedCrossUserForCloneArrays.add(MEDIA_PROCESS_NAME);
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
        sHardCodeAppToSetOomAdjArrays.put("com.huawei.android.pushagent.PushService", Integer.valueOf((int) HwActivityManagerService.PERSISTENT_PROC_ADJ));
        sHardCodeAppToSetOomAdjArrays.put("com.tencent.mm", 800);
        if (SystemProperties.getInt("ro.logsystem.usertype", 1) == 3) {
            z = true;
        }
        IS_BETA = z;
    }

    public HwActivityManagerServiceEx(IHwActivityManagerInner iams, Context context) {
        this.mIAmsInner = iams;
        this.mContext = context;
        initHwHandlerThread();
        HwBootCheck.bootSceneStart(100, 60000);
        this.mVrMananger = HwFrameworkFactory.getVRSystemServiceManager();
    }

    private void initHwHandlerThread() {
        if (IS_HWAMSEX_THREAD_DISABLED) {
            Slog.w(TAG, "HwActivityManagerServiceEx thread is disabled.");
            return;
        }
        this.mHwHandlerThread = new ServiceThread(TAG, -2, false);
        this.mHwHandlerThread.start();
        this.mHwHandler = new Handler(this.mHwHandlerThread.getLooper()) {
            /* class com.android.server.am.HwActivityManagerServiceEx.AnonymousClass4 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                int i = msg.what;
                if (i == 22) {
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
                } else if (i == 23) {
                    HwSystemMemoryUtils.monitorMemory();
                    HwActivityManagerServiceEx.this.mHwHandler.sendEmptyMessageDelayed(23, 1800000);
                }
            }
        };
        if (IS_BETA) {
            Slog.w(TAG, "start monitor because BETA");
            this.mHwHandler.sendEmptyMessageDelayed(23, 1800000);
        }
    }

    public int changeGidIfRepairMode(int uid, String processName) {
        if (uid != REPAIR_MODE_SYSTEM_UID || !PROCESS_NAME_IN_REPAIR_MODE.contains(processName)) {
            return uid;
        }
        return 1000;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showGuestSwitchDialog(int userId, String userName) {
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
            Handler handler = this.mHwHandler;
            if (handler != null) {
                Message msg = handler.obtainMessage(22);
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
            return;
        }
        throw new SecurityException(callerUid + " cannot kill pkg: " + pkg);
    }

    private final boolean cleanProviderLocked(ProcessRecord proc, ContentProviderRecord cpr, boolean always) {
        String[] names;
        boolean inLaunching = this.mIAmsInner.getLaunchingProviders().contains(cpr);
        if (!inLaunching || always) {
            synchronized (cpr) {
                cpr.launchingApp = null;
                cpr.notifyAll();
            }
            this.mIAmsInner.getProviderMap().removeProviderByClass(cpr.name, UserHandle.getUserId(cpr.uid));
            for (String str : cpr.info.authority.split(AwarenessInnerConstants.SEMI_COLON_KEY)) {
                this.mIAmsInner.getProviderMap().removeProviderByName(str, UserHandle.getUserId(cpr.uid));
            }
        }
        for (int i = cpr.connections.size() - 1; i >= 0; i--) {
            ContentProviderConnection conn = (ContentProviderConnection) cpr.connections.get(i);
            if (!conn.waiting || !inLaunching || always) {
                ProcessRecord capp = conn.client;
                conn.dead = true;
                if (conn.stableCount > 0) {
                    if (!(capp.mPersistent || capp.thread == null || capp.pid == 0 || capp.pid == this.mIAmsInner.getAmsPid())) {
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

    public boolean cleanPackageRes(List<String> packageList, Map<String, List<String>> alarmTags, int targetUid, boolean cleanAlarm, boolean isNative, boolean hasPerceptAlarm) {
        List<String> tags;
        if (packageList == null) {
            return false;
        }
        boolean didSomething = false;
        int userId = UserHandle.getUserId(targetUid);
        synchronized (this.mIAmsInner.getAMSForLock()) {
            for (Iterator<String> it = packageList.iterator(); it.hasNext(); it = it) {
                String packageName = it.next();
                if ((isNative || canCleanTaskRecord(packageName)) && this.mIAmsInner.finishDisabledPackageActivitiesLocked(packageName, true, false, userId)) {
                    didSomething = true;
                }
                if (this.mIAmsInner.bringDownDisabledPackageServicesLocked(packageName, (Set) null, userId, false, true)) {
                    didSomething = true;
                }
                if (packageName == null) {
                    this.mIAmsInner.getStickyBroadcasts().remove(userId);
                }
                ArrayList<ContentProviderRecord> providers = new ArrayList<>();
                if (this.mIAmsInner.getProviderMap().collectPackageProvidersLocked(packageName, (Set) null, true, false, userId, providers)) {
                    didSomething = true;
                }
                int i = providers.size() - 1;
                while (i >= 0) {
                    cleanProviderLocked(null, providers.get(i), true);
                    i--;
                    providers = providers;
                }
                for (int i2 = this.mIAmsInner.getBroadcastQueues().length - 1; i2 >= 0; i2--) {
                    didSomething |= this.mIAmsInner.getBroadcastQueues()[i2].cleanupDisabledPackageReceiversLocked(packageName, (Set) null, userId, true);
                }
                if (alarmTags == null) {
                    this.mIAmsInner.getAlarmService().removePackageAlarm(packageName, (List) null, targetUid);
                } else if (!(!cleanAlarm || this.mIAmsInner.getAlarmService() == null || (tags = alarmTags.get(packageName)) == null)) {
                    this.mIAmsInner.getAlarmService().removePackageAlarm(packageName, tags, targetUid);
                }
                if (isNative || !hasPerceptAlarm) {
                    this.mIAmsInner.finishForceStopPackageLockedInner(packageName, targetUid);
                }
            }
        }
        return didSomething;
    }

    public boolean killProcessRecordFast(String processName, int pid, int uid, boolean restartservice, boolean isAsynchronous, String reason, boolean needCheckAdj) {
        return killProcessRecordInternal(processName, pid, uid, restartservice, isAsynchronous, reason, false, needCheckAdj);
    }

    public boolean killNativeProcessRecordFast(String processName, int pid, int uid, boolean restartservice, boolean isAsynchronous, String reason) {
        return killProcessRecordInternal(processName, pid, uid, restartservice, isAsynchronous, reason, true, true);
    }

    private boolean killProcessRecordInternal(String processName, int pid, int uid, boolean restartservice, boolean isAsynchronous, String reason, boolean isNative, boolean needCheckAdj) {
        ProcessRecord proc;
        boolean isImportantAdj;
        if (processName == null) {
            Slog.w(TAG, "input params processName is null, error.");
            return false;
        }
        ActivityManagerService.PidMap pidsSelfLocked = this.mIAmsInner.getPidsSelfLocked();
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
                        if (!needCheckAdj || proc.curAdj >= 200 || "exec-service".equals(proc.adjType)) {
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
        try {
            proc.unlinkDeathRecipient();
        } catch (NoSuchElementException e) {
            Slog.w(TAG, "NoSuchElementException while unlink death.");
        } catch (NullPointerException e2) {
            Slog.w(TAG, "null while unlink death.");
        } catch (Exception e3) {
            Slog.w(TAG, "Unexpected exception while unlink death.");
        }
        killAndReport(reason, proc);
        return true;
    }

    private void killAndReport(String reason, ProcessRecord proc) {
        this.mIAmsInner.getDAMonitor().notifyProcessDied(proc.pid, proc.uid);
        String realReason = reason;
        String lastUseTime = "-1";
        if (reason != null) {
            String[] reasonSplit = reason.split("#");
            if (reasonSplit.length >= 2) {
                realReason = reasonSplit[0];
                lastUseTime = reasonSplit[1];
            }
        }
        proc.kill("iAwareF[" + realReason + "](" + proc.adjType + ") " + lastUseTime + "ms", true);
        this.mIAmsInner.getDAMonitor().reportAppDiedMsg(proc.userId, proc.processName, realReason);
    }

    private ProcessRecord getProcessRecordWithPid(String processName, int pid) {
        if (processName == null) {
            Slog.w(TAG, "getProcessRecord, processName is null, pid: " + pid);
            return null;
        }
        ActivityManagerService.PidMap pidsSelfLocked = this.mIAmsInner.getPidsSelfLocked();
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
        ProcessRecord app = getProcessRecordWithPid(processName, pid);
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
                this.mIAmsInner.finishDisabledPackageActivitiesLocked(packageName, true, false, UserHandle.getUserId(app.uid));
            }
        }
    }

    public boolean needCheckProcDied(ProcessRecord app) {
        if (app == null || !app.killed || !this.mIAmsInner.getDAMonitor().isFastKillSwitch(app.processName, app.uid)) {
            return false;
        }
        Slog.d(TAG, "app is killed, app=" + app);
        return true;
    }

    public boolean canCleanTaskRecord(String packageName) {
        return this.mIAmsInner.getAMSForLock().mActivityTaskManager.mHwATMSEx.canCleanTaskRecord(packageName, this.mIAmsInner.getDAMonitor().getActivityImportCount(), this.mIAmsInner.getDAMonitor().getRecentTask());
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
                /* class com.android.server.am.HwActivityManagerServiceEx.AnonymousClass6 */

                @Override // java.lang.Runnable
                public void run() {
                    Toast toast = Toast.makeText(HwActivityManagerServiceEx.this.mContext, HwActivityManagerServiceEx.this.mContext.getResources().getString(33685976), 1);
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
        Handler handler = this.mHandler;
        handler.sendMessage(handler.obtainMessage(50, userId, 0, targetUser.name));
        return Boolean.TRUE;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendMessageToSwitchUser(int userId, String userName) {
        UserController userctl = this.mIAmsInner.getUserController();
        UserInfo mCurrentUserInfo = userctl.getUserInfo(userctl.getCurrentUserId());
        UserInfo mTargetUserInfo = userctl.getUserInfo(userId);
        if (mTargetUserInfo != null) {
            Pair<UserInfo, UserInfo> userNames = new Pair<>(mCurrentUserInfo, mTargetUserInfo);
            this.mHandler.removeMessages(49);
            Handler handler = this.mHandler;
            handler.sendMessage(handler.obtainMessage(49, userNames));
        }
    }

    private void showGuestResetSessionDialog(int guestId) {
        this.mNewSessionDialog = new ResetSessionDialog(this.mContext, guestId);
        this.mNewSessionDialog.show();
    }

    /* access modifiers changed from: private */
    public class ResetSessionDialog extends AlertDialog implements DialogInterface.OnClickListener {
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

        @Override // android.content.DialogInterface.OnClickListener
        public void onClick(DialogInterface dialog, int which) {
            Slog.i(HwActivityManagerServiceEx.TAG, "onClick which:" + which);
            if (which == -2) {
                HwActivityManagerServiceEx.this.wipeGuestSession(this.mUserId);
                dismiss();
            } else if (which == -1 && isShowing()) {
                cancel();
                HwActivityManagerServiceEx hwActivityManagerServiceEx = HwActivityManagerServiceEx.this;
                hwActivityManagerServiceEx.sendMessageToSwitchUser(this.mUserId, hwActivityManagerServiceEx.getGuestName());
            }
        }
    }

    private void cancelDialog() {
        ResetSessionDialog resetSessionDialog = this.mNewSessionDialog;
        if (resetSessionDialog != null && resetSessionDialog.isShowing()) {
            this.mNewSessionDialog.cancel();
            this.mNewSessionDialog = null;
        }
    }

    private String getUserName(int userId) {
        UserInfo info;
        if (this.mIAmsInner.getUserController() == null || (info = this.mIAmsInner.getUserController().getUserInfo(userId)) == null) {
            return null;
        }
        return info.name;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getGuestName() {
        return this.mContext.getString(33685844);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void wipeGuestSession(int userId) {
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

    public boolean isApplyPersistAppPatch(String ssp, int uid, int userId, boolean bWillRestart, boolean evenPersistent, String reason, String action) {
        ApplicationInfo info;
        boolean bHandle = "android.intent.action.PACKAGE_REMOVED".equals(action);
        if ((!(bWillRestart && evenPersistent && reason != null && reason.endsWith(REASON_SYS_REPLACE)) && !bHandle) || (info = this.mIAmsInner.getPackageManagerInternal().getApplicationInfo(ssp, 1152, Process.myUid(), userId)) == null) {
            return false;
        }
        ProcessRecord apprecord = this.mIAmsInner.getProcessRecord(info.processName, uid, true);
        if ((bHandle && apprecord != null && !apprecord.mPersistent) || apprecord == null || apprecord.info == null || apprecord.info.sourceDir == null) {
            return false;
        }
        if ((apprecord.info.hwFlags & 536870912) == 0 && (info.metaData == null || !info.metaData.getBoolean(APKPATCH_META_DATA, false))) {
            return false;
        }
        if (!apprecord.info.sourceDir.equals(info.sourceDir) && bHandle) {
            this.mIAmsInner.forceStopPackageLockedInner(ssp, uid, true, false, true, true, false, userId, REASON_SYS_REPLACE);
            Slog.i("PatchService", action + TAG + "-----kill & restart---");
            this.mIAmsInner.startPersistApp(info, (String) null, false, (String) null);
        }
        return true;
    }

    public String[] updateEntryPointArgsForPCMode(ProcessRecord app, String[] entryPointArgs) {
        if ((HwPCUtils.isPcCastModeInServer() || this.mVrMananger.isVRDeviceConnected()) && app.entryPointArgs != null) {
            return concat(entryPointArgs, app.entryPointArgs);
        }
        if (HwMwUtils.ENABLED) {
            String[] magicWindowEntryPointArgs = {"", "", "", "", ""};
            HwMwUtils.performPolicy(2, new Object[]{app.info, false, magicWindowEntryPointArgs});
            if (magicWindowEntryPointArgs[1].length() > 0) {
                return concat(entryPointArgs, magicWindowEntryPointArgs);
            }
        }
        return entryPointArgs;
    }

    private static String[] concat(String[] first, String[] second) {
        String[] result = new String[(first.length + second.length)];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    public void systemReady(Runnable goingCallback, TimingsTraceLog traceLog) {
        initTrustSpace();
        isTv = isTvMode();
        if (isTv) {
            this.mBlackListObserver = new BlackListObserver();
        }
    }

    public boolean handleANRFilterFIFO(int uid, int cmd) {
        if (cmd == 0) {
            return ANRFilter.getInstance().addUid(uid);
        }
        if (cmd == 1) {
            return ANRFilter.getInstance().removeUid(uid);
        }
        if (cmd != 2) {
            return false;
        }
        return ANRFilter.getInstance().checkUid(uid);
    }

    public void appEyeNotifyRecoveryEnd(boolean acNotify) {
        ERecoveryEvent endEvent = new ERecoveryEvent();
        if (acNotify) {
            endEvent.setERecoveryID((long) APPEYE_UIPINPUT_NOTIFY);
        } else {
            endEvent.setERecoveryID((long) APPEYE_UIPINPUT_KILL);
        }
        endEvent.setFaultID((long) APPEYE_UIPINPUT_FAULTID);
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
    /* access modifiers changed from: public */
    private void showAppEyeAnrUi(Message msg) {
        ProcessRecord app;
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

    private void appEyeAppNotResponding(ProcessRecord app) {
        boolean showBackground = Settings.Secure.getInt(this.mContext.getContentResolver(), "anr_show_background", 0) != 0;
        synchronized (this.mIAmsInner.getAMSForLock()) {
            if (!showBackground) {
                if (!app.isInterestingToUserLocked() && app.pid != this.mIAmsInner.getAmsPid()) {
                    app.kill("BG ANR", true);
                    appEyeNotifyRecoveryEnd(false);
                    zrHungSendEvent("recoverresult", 0, 0, app.info.packageName, null, "BG Kill");
                    return;
                }
            }
            if (zrHungSendEvent("showanrdialog", app.pid, app.uid, app.info.packageName, null, "appeye")) {
                Handler mUiHandler = this.mIAmsInner.getUiHandler();
                if (mUiHandler != null) {
                    app.makeAppeyeAppNotRespondingLocked((String) null, "AppFreeze!", (String) null);
                    Message msg = Message.obtain();
                    msg.what = 2;
                    msg.obj = new AppNotRespondingDialog.Data(app, (ApplicationInfo) null, false);
                    app.anrType = 2;
                    mUiHandler.sendMessage(msg);
                } else {
                    return;
                }
            }
            appEyeNotifyRecoveryEnd(true);
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
            return sendZRHungEvent(HwServiceFactory.getZRHungService(), data);
        }
    }

    private boolean sendZRHungEvent(IZRHungService service, ZrHungData data) {
        if (service == null) {
            return false;
        }
        if (service.sendEvent(data)) {
            return true;
        }
        Slog.e(TAG, "zrHungSendEvent failed!");
        return false;
    }

    public boolean shouldPreventStartProcess(ApplicationInfo info, String processName, int userId) {
        if (!checkAndInitTvBlackList(info, this.mContext)) {
            Slog.e(TAG, "packageName is in tv black list, start process permission denied " + info.packageName);
            return true;
        }
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

    public static void notifyAppToTop(int pid, int status) {
        ProcessRecord app;
        if (!RTG_FRAME_ENABLE) {
            Slog.d(TAG, "notifyAppToTop frame not enable pid:" + pid + ", enable:" + status);
            return;
        }
        synchronized (TOP_APP_MAP) {
            app = TOP_APP_MAP.get(Integer.valueOf(pid));
        }
        Slog.d(TAG, "notifyAppToTop pid:" + pid + ", enable:" + status);
        IApplicationThread appThread = null;
        if (app != null) {
            try {
                appThread = app.thread;
            } catch (RemoteException e) {
                Slog.e(TAG, "RemoteException notifyAppToTop failed, " + e.getMessage());
                return;
            }
        }
        if (appThread != null) {
            appThread.notifyAppToTop(status);
            return;
        }
        Slog.e(TAG, "Null app thread, notifyAppToTop failed: " + pid);
    }

    public static void removeProcess(int pid) {
        synchronized (TOP_APP_MAP) {
            TOP_APP_MAP.remove(Integer.valueOf(pid));
        }
    }

    public static int getRenderTid(int pid) {
        ProcessRecord app;
        synchronized (TOP_APP_MAP) {
            app = TOP_APP_MAP.get(Integer.valueOf(pid));
        }
        if (app != null) {
            return app.renderThreadTid;
        }
        return -1;
    }

    public void setProcessRecForPid(int pid) {
        ProcessRecord app;
        synchronized (this.mIAmsInner.getPidsSelfLocked()) {
            app = this.mIAmsInner.getPidsSelfLocked().get(pid);
        }
        if (app != null) {
            synchronized (TOP_APP_MAP) {
                TOP_APP_MAP.put(Integer.valueOf(app.pid), app);
            }
        }
    }

    public static void clearTopApps() {
        synchronized (TOP_APP_MAP) {
            TOP_APP_MAP.clear();
        }
    }

    public void setRtgThreadToIAware(ProcessRecord app, boolean setRender) {
        setRtgThreadToIAware(app, setRender, true);
    }

    private void setRtgThreadToIAware(ProcessRecord app, boolean setRender, boolean switchToTop) {
        int type;
        if (app == null || app.pid <= 0) {
            Slog.d(TAG, "discard to set rtg, for no process needed");
            return;
        }
        if (switchToTop && setRender && app.mHasForegroundActivities) {
            type = 2;
            this.doColdStartAppFromLauncher = false;
        } else if (switchToTop && !setRender && app.mHasForegroundActivities && app.renderThreadTid <= 0) {
            type = 4;
            this.doColdStartAppFromLauncher = true;
            if (this.mRemoteAnimationPid > 0) {
                this.mRemoteAnimationPid = -1;
            }
        } else if (ENABLE_ANIMATION_RTG && switchToTop && !setRender && app.runningRemoteAnimation && !this.doColdStartAppFromLauncher) {
            type = 0;
            this.mRemoteAnimationPid = app.pid;
        } else if (ENABLE_ANIMATION_RTG && !switchToTop && !"top-activity".equals(app.adjType) && this.mRemoteAnimationPid == app.pid) {
            type = 1;
            this.mRemoteAnimationPid = -1;
        } else {
            return;
        }
        Bundle args = new Bundle();
        args.putInt("pid", app.pid);
        args.putInt("tid", app.renderThreadTid);
        args.putInt("type", type);
        args.putInt("displayId", app.mDisplayId);
        this.mIAmsInner.getDAMonitor().reportData("RESOURCE_WINSTATE", System.currentTimeMillis(), args);
    }

    public void setThreadSchedPolicy(int oldSchedGroup, ProcessRecord app) {
        if (app != null) {
            if (app.mCurSchedGroup == 3) {
                if (oldSchedGroup != 3) {
                    this.mIAmsInner.getDAMonitor().setVipThread(app.uid, app.pid, app.renderThreadTid, true, false);
                    setRtgThreadToIAware(app, false, true);
                }
            } else if (oldSchedGroup == 3) {
                this.mIAmsInner.getDAMonitor().setVipThread(app.uid, app.pid, app.renderThreadTid, false, false);
                setRtgThreadToIAware(app, false, false);
            }
        }
    }

    public WindowManagerPolicyConstants.PointerEventListener getPointerEventListener() {
        return new WindowManagerPolicyConstants.PointerEventListener() {
            /* class com.android.server.am.HwActivityManagerServiceEx.AnonymousClass7 */

            public void onPointerEvent(MotionEvent motionEvent) {
                HwPrommEventManager prommEventMng;
                HwActivityManagerServiceEx.this.mIAmsInner.getDAMonitor().onPointerEvent(motionEvent.getAction());
                if (motionEvent.getAction() == 0 && (prommEventMng = HwPrommEventManager.getInstance()) != null) {
                    prommEventMng.getInputEvent();
                }
            }
        };
    }

    public List<String> getPidWithUiFromUid(int uid) {
        List<String> pids = new ArrayList<>();
        synchronized (this.mIAmsInner.getPidsSelfLocked()) {
            int pidsSize = this.mIAmsInner.getPidsSelfLocked().size();
            for (int i = 0; i < pidsSize; i++) {
                ProcessRecord p = this.mIAmsInner.getPidsSelfLocked().valueAt(i);
                if (p != null) {
                    if (p.uid == uid && p.pid != 0 && p.hasShownUi) {
                        pids.add(String.valueOf(p.pid));
                    }
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
                    app = this.mIAmsInner.newProcessRecordLockedEx(appInfo, (String) null, false, 0, (HostingRecord) null);
                    this.mIAmsInner.updateLruProcessLockedEx(app, false, (ProcessRecord) null);
                    this.mIAmsInner.updateOomAdjLockedEx("preload");
                }
                try {
                    pm.setPackageStoppedState(packageName, false, UserHandle.getUserId(app.uid));
                } catch (RemoteException e2) {
                    Slog.w(TAG, "RemoteException, Failed trying to unstop package: " + packageName);
                } catch (IllegalArgumentException e3) {
                    Slog.w(TAG, "IllegalArgumentException, Failed trying to unstop package " + packageName);
                }
                if (app.thread == null) {
                    this.mIAmsInner.startProcessLockedEx(app, new HostingRecord("start application", app.processName), (String) null);
                }
                return 0;
            }
        }
    }

    private void reportAppForceStopMsg(int userId, String packageName, int callingPid) {
        if (checkIfPackageNameMatchesPid(callingPid, WifiProCommonUtils.WIFI_SETTINGS_PHONE)) {
            this.mIAmsInner.getDAMonitor().reportAppDiedMsg(userId, packageName, "settings");
        }
    }

    private boolean checkIfPackageNameMatchesPid(int mPid, String targetPackageName) {
        return targetPackageName.equals(getPackageNameForPid(mPid));
    }

    public void reportAppDiedMsg(int userId, String processName, int callerPid, String reason) {
        if (CoordinationStackDividerManager.getInstance(this.mContext).isVisible() && processName.equals("com.huawei.camera")) {
            int curUserId = this.mIAmsInner.getUserController().getCurrentUserIdLU();
            Slog.i(TAG, "reportAppDiedMsg process=" + processName + " userId=" + userId + " curUserId=" + curUserId);
            if (userId == curUserId) {
                CoordinationStackDividerManager.getInstance(this.mContext).removeDividerView();
                CoordinationModeUtils.getInstance(this.mContext).setCoordinationCreateMode(0);
                CoordinationModeUtils.getInstance(this.mContext).setCoordinationState(0);
                HwFoldScreenManagerInternal fsm = (HwFoldScreenManagerInternal) LocalServices.getService(HwFoldScreenManagerInternal.class);
                if (fsm != null) {
                    fsm.setDisplayMode(2);
                }
                this.mIAmsInner.getAMSForLock().mActivityTaskManager.onCoordinationModeDismissed();
            }
        }
        if (reason == null || !reason.contains("forceStop")) {
            this.mIAmsInner.getDAMonitor().reportAppDiedMsg(userId, processName, reason);
        } else if (reason.contains("SystemManager")) {
            this.mIAmsInner.getDAMonitor().reportAppDiedMsg(userId, processName, "SystemManager");
        } else if (reason.contains("PowerGenie")) {
            this.mIAmsInner.getDAMonitor().reportAppDiedMsg(userId, processName, "PowerGenie");
        } else {
            reportAppForceStopMsg(userId, processName, callerPid);
        }
    }

    private String getPackageNameForPid(int pid) {
        synchronized (this.mIAmsInner.getPidsSelfLocked()) {
            ProcessRecord proc = this.mIAmsInner.getPidsSelfLocked().get(pid);
            if (proc != null) {
                return proc.info != null ? proc.info.packageName : "android";
            }
            Flog.i(100, "ProcessRecord for pid " + pid + " does not exist");
            return null;
        }
    }

    public boolean killProcessRecordFromIAwareInternal(ProcessInfo procInfo, boolean restartservice, boolean isAsynchronous, String reason, boolean isNative, boolean needCheckAdj) {
        ProcessRecord proc;
        boolean isImportantAdj;
        String killedProcessName;
        int killedAppUserId;
        Throwable th;
        synchronized (this.mIAmsInner.getPidsSelfLocked()) {
            try {
                if (procInfo.mPid == this.mIAmsInner.getMyPid() || procInfo.mPid < 0) {
                    Slog.e(TAG, "killProcessRecordFromIAware it is failed to get process record ,mUid :" + procInfo.mUid);
                    return false;
                }
                proc = this.mIAmsInner.getPidsSelfLocked().get(procInfo.mPid);
                if (proc == null) {
                    Slog.e(TAG, "killProcessRecordFromIAware this process has been killed or died before  :" + procInfo.mProcessName);
                    return false;
                }
                if (proc.curAdj > 0) {
                    if (!needCheckAdj || proc.curAdj >= 200 || "exec-service".equals(proc.adjType)) {
                        isImportantAdj = false;
                        if (!isNative || !isImportantAdj) {
                            killedProcessName = proc.processName;
                            killedAppUserId = proc.userId;
                        } else {
                            Slog.e(TAG, "killProcessRecordFromIAware process cleaner kill process: adj changed, new adj:" + proc.curAdj + ", old adj:" + procInfo.mCurAdj + ", pid:" + procInfo.mPid + ", uid:" + procInfo.mUid + ", " + procInfo.mProcessName);
                            return false;
                        }
                    }
                }
                isImportantAdj = true;
                if (!isNative) {
                }
                killedProcessName = proc.processName;
                killedAppUserId = proc.userId;
            } catch (Throwable th2) {
                th = th2;
                throw th;
            }
        }
        synchronized (this.mIAmsInner.getAMSForLock()) {
            try {
                this.mIAmsInner.cleanupAppInLaunchingProvidersLockedEx(proc, true);
                if (isAsynchronous) {
                    proc.killedByAm = true;
                    proc.killed = true;
                }
                proc.unlinkDeathRecipient();
                String detailedReason = reason + "(" + proc.adjType + ")";
                try {
                    this.mIAmsInner.removeProcessLockedEx(proc, false, restartservice, detailedReason);
                    if (isAsynchronous) {
                        this.mIAmsInner.getDAMonitor().killProcessGroupForQuickKill(proc.info.uid, procInfo.mPid);
                        Slog.i(TAG, "Killing " + killedProcessName + " (adj " + proc.curAdj + "): " + detailedReason);
                        EventLog.writeEvent(30023, Integer.valueOf(killedAppUserId), Integer.valueOf(procInfo.mPid), killedProcessName, Integer.valueOf(proc.curAdj), detailedReason);
                    }
                    this.mIAmsInner.getDAMonitor().reportAppDiedMsg(killedAppUserId, killedProcessName, reason);
                    return true;
                } catch (Throwable th3) {
                    th = th3;
                    throw th;
                }
            } catch (Throwable th4) {
                th = th4;
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
            ProcessRecord proc = this.mIAmsInner.getPidsSelfLocked().get(procInfo.mPid);
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

    public boolean getProcessRecordFromMTM(ProcessInfo procInfo) {
        boolean z = false;
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
                ProcessRecord proc = this.mIAmsInner.getPidsSelfLocked().get(procInfo.mPid);
                if (proc == null) {
                    Slog.e(TAG, "getProcessRecordFromMTM process info is null ,mUid :" + procInfo.mPid);
                    return false;
                }
                if (procInfo.mType == 0) {
                    procInfo.mType = MultiTaskUtils.getAppType(procInfo.mPid, proc.info);
                }
                procInfo.mProcessName = proc.processName;
                procInfo.mCurSchedGroup = proc.mCurSchedGroup;
                procInfo.mCurAdj = proc.curAdj;
                procInfo.mAdjType = proc.adjType;
                procInfo.mAppUid = proc.info.uid;
                procInfo.mTargetSdkVersion = proc.info.targetSdkVersion;
                procInfo.mSetProcState = proc.setProcState;
                procInfo.mForegroundActivities = proc.mHasForegroundActivities;
                procInfo.mForegroundServices = proc.mHasForegroundServices;
                if (proc.forcingToImportant != null) {
                    z = true;
                }
                procInfo.mForceToForeground = z;
                if (procInfo.mPackageName.size() == 0) {
                    int list_size = proc.pkgList.size();
                    for (int i = 0; i < list_size; i++) {
                        String packagename = proc.pkgList.keyAt(i);
                        if (!procInfo.mPackageName.contains(packagename)) {
                            procInfo.mPackageName.add(packagename);
                        }
                    }
                }
                procInfo.mLru = this.mIAmsInner.getLruProcesses().lastIndexOf(proc);
                return true;
            }
        }
    }

    public void setAndRestoreMaxAdjIfNeed(List<String> adjCustPkg) {
        if (adjCustPkg != null) {
            ArraySet<String> adjCustPkgSet = new ArraySet<>();
            adjCustPkgSet.addAll(adjCustPkg);
            synchronized (this.mIAmsInner.getAMSForLock()) {
                synchronized (this.mIAmsInner.getPidsSelfLocked()) {
                    int list_size = this.mIAmsInner.getPidsSelfLocked().size();
                    for (int i = 0; i < list_size; i++) {
                        ProcessRecord p = this.mIAmsInner.getPidsSelfLocked().valueAt(i);
                        if (p != null) {
                            boolean pkgContains = false;
                            Iterator iter = p.pkgList.mPkgList.keySet().iterator();
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
                                    p.maxAdj = LightsManagerEx.LIGHT_ID_BACKLIGHT_10000;
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

    public void reportServiceRelationIAware(final int relationType, final ServiceRecord r, final ProcessRecord caller, final Intent intent, final AppBindRecord b) {
        this.mHandler.post(new Runnable() {
            /* class com.android.server.am.HwActivityManagerServiceEx.AnonymousClass8 */

            @Override // java.lang.Runnable
            public void run() {
                HwActivityManagerServiceEx.this.reportServiceRelationIAwareInner(relationType, r, caller, intent, b);
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reportServiceRelationIAwareInner(int relationType, ServiceRecord r, ProcessRecord caller, Intent intent, AppBindRecord b) {
        Intent intentReal;
        if (r != null && caller != null && r.name != null) {
            if (r.appInfo != null) {
                if (caller.uid != r.appInfo.uid && this.mIAmsInner.getDAMonitor().isResourceNeeded(RESOURCE_APPASSOC)) {
                    int hwFlag = 0;
                    Bundle bundleArgs = new Bundle();
                    int callerUid = caller.uid;
                    int callerPid = caller.pid;
                    String callerProcessName = caller.processName;
                    int targetUid = r.appInfo.uid;
                    String targetProcessName = r.processName;
                    String compName = r.name.flattenToShortString();
                    if (intent != null) {
                        hwFlag = intent.getHwFlags();
                    } else if (!(b == null || b.intent == null || b.intent.intent == null || (intentReal = b.intent.intent.getIntent()) == null)) {
                        hwFlag = intentReal.getHwFlags();
                    }
                    bundleArgs.putInt(ASSOC_CALL_PID, callerPid);
                    bundleArgs.putInt(ASSOC_CALL_UID, callerUid);
                    bundleArgs.putString(ASSOC_CALL_PROCNAME, callerProcessName);
                    bundleArgs.putInt(ASSOC_TGT_UID, targetUid);
                    bundleArgs.putString(ASSOC_TGT_PROCNAME, targetProcessName);
                    bundleArgs.putString(ASSOC_TGT_COMPNAME, compName);
                    bundleArgs.putInt(ASSOC_RELATION_TYPE, relationType);
                    bundleArgs.putInt(ASSOC_HWFLAG, hwFlag);
                    this.mIAmsInner.getDAMonitor().reportData(RESOURCE_APPASSOC, System.currentTimeMillis(), bundleArgs);
                }
            }
        }
    }

    public void reportServiceRelationIAware(int relationType, ContentProviderRecord r, ProcessRecord caller, boolean isStable) {
        if (caller != null && r != null && r.info != null && r.name != null) {
            if (caller.uid == r.uid) {
                if (isStable && isStableProvider(r, caller)) {
                    synchronized (this.mAssocOutStablePrds) {
                        this.mAssocOutStablePrds.put(caller.pid, true);
                    }
                    synchronized (this.mAssocInStablePrds) {
                        this.mAssocInStablePrds.put(r.proc.pid, true);
                    }
                }
            } else if (this.mIAmsInner.getDAMonitor().isResourceNeeded(RESOURCE_APPASSOC)) {
                if (r.proc != null) {
                    synchronized (this.mAssocMap) {
                        ArrayMap<Integer, Long> pids = this.mAssocMap.get(Integer.valueOf(caller.pid));
                        if (pids != null) {
                            Long elaseTime = pids.get(Integer.valueOf(r.proc.pid));
                            if (elaseTime == null) {
                                pids.put(Integer.valueOf(r.proc.pid), Long.valueOf(SystemClock.elapsedRealtime()));
                            } else if (SystemClock.elapsedRealtime() - elaseTime.longValue() < 60000) {
                                return;
                            }
                        } else {
                            ArrayMap<Integer, Long> pids2 = new ArrayMap<>();
                            pids2.put(Integer.valueOf(r.proc.pid), Long.valueOf(SystemClock.elapsedRealtime()));
                            this.mAssocMap.put(Integer.valueOf(caller.pid), pids2);
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
    }

    private boolean isStableProvider(ContentProviderRecord cpr, ProcessRecord caller) {
        if (isIawareDisabled() || cpr.proc == null || cpr.proc.pid == caller.pid || isInvalidStableProvider(cpr.proc) || isInvalidStableProvider(caller) || caller.thread == null || caller.pid == 0) {
            return false;
        }
        return true;
    }

    private boolean isIawareDisabled() {
        if (this.mIsIawareEnable == null) {
            this.mIsIawareEnable = new AtomicBoolean(SystemProperties.getBoolean("ro.config.enable_iaware", false));
        }
        if (!this.mIsIawareEnable.get() || !this.mIsAssocEnable.get()) {
            return true;
        }
        return false;
    }

    private boolean isInvalidStableProvider(ProcessRecord proc) {
        return proc.isPersistent() || proc.pid == this.mIAmsInner.getMyPid() || "com.huawei.android.launcher".equals(proc.info.packageName) || proc.getWindowProcessController().isHomeProcess();
    }

    public int getStableProviderProcStatus(int pid) {
        int linkValue = (isAssocOutStableProc(pid) ? 1 : 0) + (isAssocInStableProc(pid) ? 2 : 0);
        if (linkValue > 0) {
            Slog.d(TAG, "stableProc,link=" + linkValue + ",pid=" + pid);
        }
        return linkValue;
    }

    private boolean isAssocOutStableProc(int pid) {
        return isAssocStableProc(pid, this.mAssocOutStablePrds);
    }

    private boolean isAssocInStableProc(int pid) {
        return isAssocStableProc(pid, this.mAssocInStablePrds);
    }

    private boolean isAssocStableProc(int pid, SparseBooleanArray stableProviders) {
        boolean z;
        if (pid <= 0) {
            return false;
        }
        synchronized (stableProviders) {
            z = stableProviders.get(pid);
        }
        return z;
    }

    public void reportProcessDied(int pid) {
        synchronized (this.mAssocMap) {
            this.mAssocMap.remove(Integer.valueOf(pid));
            Iterator<Map.Entry<Integer, ArrayMap<Integer, Long>>> it = this.mAssocMap.entrySet().iterator();
            while (it.hasNext()) {
                ArrayMap<Integer, Long> pids = it.next().getValue();
                pids.remove(Integer.valueOf(pid));
                if (pids.isEmpty()) {
                    it.remove();
                }
            }
        }
        synchronized (this.mAssocOutStablePrds) {
            this.mAssocOutStablePrds.delete(pid);
        }
        synchronized (this.mAssocInStablePrds) {
            this.mAssocInStablePrds.delete(pid);
        }
    }

    public void reportAssocDisable() {
        synchronized (this.mAssocMap) {
            this.mAssocMap.clear();
        }
        this.mIsAssocEnable.set(false);
        synchronized (this.mAssocOutStablePrds) {
            this.mAssocOutStablePrds.clear();
        }
        synchronized (this.mAssocInStablePrds) {
            this.mAssocInStablePrds.clear();
        }
    }

    /* JADX INFO: finally extract failed */
    public int[] changeGidsIfNeeded(ProcessRecord app, int[] gids) {
        int userId = UserHandle.getUserId(app.uid);
        boolean needAddPermission = true;
        if (PACKAGE_PRIVATE_SPACE.equals(app.info.packageName) && userId != 0) {
            int[] newGids = new int[(gids.length + 1)];
            System.arraycopy(gids, 0, newGids, 0, gids.length);
            newGids[gids.length] = UserHandle.getUserGid(0);
            return newGids;
        } else if (!IS_SUPPORT_CLONE_APP) {
            return gids;
        } else {
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
                                int[] newGids2 = new int[(gids.length + 2)];
                                System.arraycopy(gids, 0, newGids2, 0, gids.length);
                                newGids2[gids.length] = UserHandle.getUserGid(ui.id);
                                newGids2[gids.length + 1] = UserHandle.getUid(ui.id, 1023);
                                gids = newGids2;
                            }
                        }
                    }
                }
                Binder.restoreCallingIdentity(ident);
                if (!CLONEPROFILE_PERMISSION.contains(app.info.packageName) && !HwPackageManagerService.isSupportCloneAppInCust(app.info.packageName) && !app.info.processName.contains(MEDIA_PROCESS_NAME)) {
                    needAddPermission = false;
                }
                if (!needAddPermission || userId != 0) {
                    return gids;
                }
                int[] newGids3 = new int[(gids.length + (20 * 2))];
                System.arraycopy(gids, 0, newGids3, 0, gids.length);
                for (int i = 0; i < 20; i++) {
                    newGids3[gids.length + i] = UserHandle.getUid(i + 128, 1023);
                    newGids3[gids.length + 20 + i] = UserHandle.getUserGid(i + 128);
                }
                return newGids3;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
                throw th;
            }
        }
    }

    public void startPushService() {
        File jarFile = new File("/system/framework/hwpush.jar");
        File custFile = HwCfgFilePolicy.getCfgFile("jars/hwpush.jar", 0);
        if (jarFile.exists() || (custFile != null && custFile.exists())) {
            Slog.d(TAG, "start push service");
            this.mHandler.postDelayed(new Runnable() {
                /* class com.android.server.am.HwActivityManagerServiceEx.AnonymousClass9 */

                @Override // java.lang.Runnable
                public void run() {
                    Intent serviceIntent = new Intent(HwActivityManagerServiceEx.this.mContext, PushService.class);
                    serviceIntent.putExtra("startFlag", "1");
                    HwActivityManagerServiceEx.this.mContext.startService(serviceIntent);
                }
            }, HwArbitrationDEFS.DelayTimeMillisA);
        }
    }

    private final boolean isOomAdjCustomized(ProcessRecord app) {
        return sHardCodeAppToSetOomAdjArrays.containsKey(app.info.packageName) || sHardCodeAppToSetOomAdjArrays.containsKey(app.processName);
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
        WindowManagerInternal windowManagerInternal;
        if (HwPCUtils.isPcCastModeInServer()) {
            HashMap<String, Integer> pkgDisplayMaps = this.mIAmsInner.getAMSForLock().mActivityTaskManager.mHwATMSEx.getPkgDisplayMaps();
            if (pkgDisplayMaps != null && pkgDisplayMaps.containsKey(app.info.packageName)) {
                int displayId = pkgDisplayMaps.get(app.info.packageName).intValue();
                if (HwPCUtils.isValidExtDisplayId(displayId)) {
                    app.mDisplayId = displayId;
                    app.getWindowProcessController().mDisplayId = displayId;
                    if (app.entryPointArgs == null) {
                        DefaultHwPCMultiWindowManager.EntryEx entry = getHwPCMultiWindowManager(buildAtmsEx(this.mIAmsInner.getAMSForLock().mActivityTaskManager)).getEntry(app.info.packageName);
                        app.entryPointArgs = (entry == null || entry.getWindowBounds() == null) ? new String[]{String.valueOf(displayId)} : new String[]{String.valueOf(displayId), String.valueOf(entry.getWindowBounds().width()), String.valueOf(entry.getWindowBounds().height())};
                    }
                }
            }
            if (HwPCUtils.isHiCarCastMode() && (windowManagerInternal = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class)) != null) {
                int targetDisplayId = windowManagerInternal.getTopFocusedDisplayId();
                String imePkg = HwPCFactory.getHwPCFactory().getHwPCFactoryImpl().getHwHiCarMultiWindowManager().getCurrentImePkg();
                if (imePkg != null && imePkg.equals(app.info.packageName)) {
                    app.entryPointArgs = new String[]{String.valueOf(targetDisplayId), String.valueOf(HwPCUtils.mTouchDeviceID)};
                    HwPCFactory.getHwPCFactory().getHwPCFactoryImpl().getHiCarManager().setInputMethodUid(app.uid);
                }
            }
            if (HwPCUtils.enabledInPad()) {
                List<InputMethodInfo> methodList = HwPCUtils.getInputMethodList();
                if (methodList != null) {
                    String pkgName = null;
                    int listSize = methodList.size();
                    int i = 0;
                    while (true) {
                        if (listSize > i) {
                            InputMethodInfo mi = methodList.get(i);
                            if (mi != null) {
                                pkgName = mi.getPackageName();
                            }
                            if (pkgName != null && pkgName.equals(app.info.packageName)) {
                                app.entryPointArgs = new String[]{String.valueOf(HwPCUtils.getPCDisplayID())};
                                break;
                            }
                            i++;
                        } else {
                            break;
                        }
                    }
                }
                if (!"com.huawei.desktop.explorer".equals(app.info.packageName) && !"com.huawei.desktop.systemui".equals(app.info.packageName) && !"com.android.systemui".equals(app.info.packageName) && !"com.huawei.android.launcher".equals(app.info.packageName) && !MEDIA_PROCESS_NAME.equals(app.info.processName) && !HW_SCREEN_RECORDER.equals(app.info.packageName) && !HITOUCH_PROCESS_NAME.equals(app.info.packageName) && !INSTANTSHARE_PROCESS_NAME.equals(app.info.packageName)) {
                    app.mDisplayId = HwPCUtils.getPCDisplayID();
                    if (app.entryPointArgs == null) {
                        app.entryPointArgs = new String[]{String.valueOf(HwPCUtils.getPCDisplayID())};
                    }
                }
            }
        }
        if (this.mVrMananger.isVRDeviceConnected()) {
            Slog.e(TAG, "hwAMS startProcessLocked is VR Display is " + this.mVrMananger.getVRDisplayID());
            int[] vrDisplayParams = this.mVrMananger.getVrDisplayParams();
            if (vrDisplayParams != null) {
                int length = vrDisplayParams.length;
                IVRSystemServiceManager iVRSystemServiceManager = this.mVrMananger;
                if (length == 3) {
                    app.entryPointArgs = new String[]{String.valueOf(iVRSystemServiceManager.getVRDisplayID()), String.valueOf(vrDisplayParams[1]), String.valueOf(vrDisplayParams[2])};
                }
            }
        }
    }

    public void updateProcessRecordMaxAdj(ProcessRecord app) {
        if (isOomAdjCustomized(app)) {
            int custMaxAdj = retrieveCustedMaxAdj(app.processName);
            if (app.maxAdj > -800 && custMaxAdj >= -900 && custMaxAdj <= 999) {
                app.maxAdj = custMaxAdj;
                Slog.i(TAG, "addAppLocked, app:" + app + ", set maxadj to " + custMaxAdj);
            }
        }
    }

    public boolean shouldSkipSendIntentSender(IIntentSender target, Bundle options) {
        PendingIntentRecord.Key key;
        PendingIntentRecord.Key key2;
        int displayId;
        KeyguardManager keyguardManager;
        if (HwPCUtils.isPcCastModeInServer() && (target instanceof PendingIntentRecord) && (key2 = ((PendingIntentRecord) target).key) != null) {
            if (this.mKeyguardManager == null) {
                this.mKeyguardManager = (KeyguardManager) this.mContext.getSystemService("keyguard");
            }
            if (options != null) {
                displayId = ActivityOptions.fromBundle(options).getLaunchDisplayId();
            } else {
                displayId = 0;
            }
            boolean isFromPcNotification = options != null ? options.getBoolean(FLAG_FROM_PC_NOTIFICATION, false) : false;
            if (!HwPCUtils.enabledInPad() || !"com.android.incallui".equals(key2.packageName) || (keyguardManager = this.mKeyguardManager) == null || keyguardManager.isKeyguardLocked() || isFromPcNotification) {
                HashMap<String, Integer> pkgDisplayMaps = this.mIAmsInner.getAMSForLock().mActivityTaskManager.mHwATMSEx.getPkgDisplayMaps();
                if (pkgDisplayMaps != null) {
                    pkgDisplayMaps.put(key2.packageName, Integer.valueOf(displayId));
                }
            } else {
                Slog.d(TAG, "sendIntentSender skip when screen on, packageName: " + key2.packageName + ",KeyguardLocked: " + this.mKeyguardManager.isKeyguardLocked() + ",displayId: " + displayId + ",isFromPcNotification: " + isFromPcNotification);
                return true;
            }
        }
        if (HwPCUtils.isInWindowsCastMode() && (target instanceof PendingIntentRecord) && (key = ((PendingIntentRecord) target).key) != null && ((DESK_CLOCK.equals(key.packageName) || DESK_CLOCK_NEW.equals(key.packageName)) && !sHwMultiDisplayUtils.isScreenOnForHwMultiDisplay())) {
            sHwMultiDisplayUtils.lightScreenOnForHwMultiDisplay();
            PowerManager pm = (PowerManager) this.mContext.getSystemService("power");
            if (pm != null) {
                pm.userActivity(SystemClock.uptimeMillis(), false);
            }
        }
        return false;
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
        if (!this.mIAmsInner.getSystemReady()) {
            return false;
        }
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
            boolean shouldPrevent2 = shouldPrevent | HwServiceSecurityPartsFactoryEx.getInstance().getHwSystemManagerPluginEx(this.mContext).shouldPreventStartComponent(type, calleePackage, callerUid, callerPid, callerPackage, userId);
            Binder.restoreCallingIdentity(ident);
            return shouldPrevent2;
        } catch (Throwable th3) {
            spc = th3;
            Binder.restoreCallingIdentity(ident);
            throw spc;
        }
    }

    public boolean shouldPreventStartService(ServiceInfo sInfo, int callerPid, int callerUid, int userId, ProcessRecord callerApp, boolean servExist, Intent service) {
        if (sInfo == null) {
            return false;
        }
        boolean shouldPreventStartService = HwPartIawareUtil.shouldPreventStartService(new BaseAppStartupInfo(callerPid, callerUid, new WindowProcessControllerEx(callerApp == null ? null : callerApp.getWindowProcessController()), null, service), sInfo, this.mSrvFlagLocked, servExist);
        if (shouldPreventStartService || callerApp == null || callerApp.info == null || sInfo.applicationInfo == null) {
            return shouldPreventStartService;
        }
        return shouldPreventStartComponent(2, sInfo.applicationInfo.packageName, callerUid, callerPid, callerApp.info.packageName, userId);
    }

    public boolean shouldPreventStartProvider(ProviderInfo cpi, int userId, int callerUid, int callerPid, ProcessRecord callerApp, boolean needToCheckIAware) {
        if (cpi == null) {
            return false;
        }
        boolean shouldPreventStartProvider = false;
        if (needToCheckIAware) {
            shouldPreventStartProvider = HwPartIawareUtil.shouldPreventStartProvider(cpi, callerPid, callerUid, new WindowProcessControllerEx(callerApp == null ? null : callerApp.getWindowProcessController()));
        }
        if (shouldPreventStartProvider || callerApp == null || callerApp.info == null) {
            return shouldPreventStartProvider;
        }
        return shouldPreventStartComponent(3, cpi.packageName, callerUid, callerPid, callerApp.info.packageName, userId);
    }

    public boolean shouldPreventSendBroadcast(BroadcastRecord record, ResolveInfo resolveInfo, ProcessRecord targetApp, String calleePackage, boolean needToCheckIAware) {
        boolean shouldPreventSendBroadcast = false;
        if (record == null) {
            return false;
        }
        Intent intent = record.intent;
        ProcessRecord callerApp = record.callerApp;
        int callerPid = record.callingPid;
        int callerUid = record.callingUid;
        String callingPackage = record.callerPackage;
        if (needToCheckIAware) {
            WindowProcessController callerAppWPController = null;
            WindowProcessController targetAppWPController = targetApp == null ? null : targetApp.getWindowProcessController();
            if (callerApp != null) {
                callerAppWPController = callerApp.getWindowProcessController();
            }
            shouldPreventSendBroadcast = HwPartIawareUtil.shouldPreventSendReceiver(resolveInfo, new BaseAppStartupInfo(callerPid, callerUid, new WindowProcessControllerEx(callerAppWPController), new WindowProcessControllerEx(targetAppWPController), intent));
        }
        if (!shouldPreventSendBroadcast) {
            return shouldPreventStartComponent(1, calleePackage, callerUid, callerPid, callingPackage, record.userId);
        }
        return shouldPreventSendBroadcast;
    }

    public void setServiceFlagLocked(int servFlag) {
        this.mSrvFlagLocked = servFlag;
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
            String packageName = null;
            String appName = null;
            if ("android.provider.Telephony.SMS_RECEIVED".equals(intentAction) || "android.provider.Telephony.SMS_DELIVER".equals(intentAction)) {
                int list_size = list.size();
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
                        Log.e(TAG, "checkAndPrintTestModeLog(). error");
                        is_data_ok = false;
                    }
                    if (is_data_ok) {
                        Log.i(TAG, " <" + appName + ">[" + packageName + "][" + callingMethod + "]" + desciption);
                    }
                }
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
                /* class com.android.server.am.HwActivityManagerServiceEx.AnonymousClass10 */

                public void onCleanFinish(AppCleanParam result) {
                    List<String> pkgNames = result.getStringList();
                    List<Integer> userIds = result.getIntList();
                    List<Integer> killTypes = result.getIntListEx();
                    if (!(pkgNames == null || userIds == null || killTypes == null)) {
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
                            /* class com.android.server.am.HwActivityManagerServiceEx.AnonymousClass10.AnonymousClass1 */

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
            HwPCUtils.log(DefaultHwPCMultiWindowManager.TAG, "forceGCAfterRebooting-fail: runningAppInfo is null");
            return;
        }
        for (ActivityManager.RunningAppProcessInfo appProcess : runningAppInfo) {
            int pid = appProcess.pid;
            Process.sendSignal(pid, 10);
            slimProcessAsync(appProcess.processName, pid);
        }
        HwSystemMemoryUtils.slimForZygote();
        HwSystemMemoryUtils.slimForSystem();
    }

    private void slimProcessAsync(final String processName, final int pid) {
        if (RECLAIM_PROCESSES.contains(processName) && isAllowSlim()) {
            Handler handler = this.mHwHandler;
            if (handler == null) {
                Thread thread = new Thread(new Runnable(processName, pid) {
                    /* class com.android.server.am.$$Lambda$HwActivityManagerServiceEx$bHu5XYbCBQkx9pkLlLjmmPDJO8 */
                    private final /* synthetic */ String f$0;
                    private final /* synthetic */ int f$1;

                    {
                        this.f$0 = r1;
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        HwActivityManagerServiceEx.lambda$slimProcessAsync$0(this.f$0, this.f$1);
                    }
                });
                thread.setName("slim[" + pid + "]");
                thread.start();
                return;
            }
            handler.post(new Runnable() {
                /* class com.android.server.am.HwActivityManagerServiceEx.AnonymousClass11 */

                @Override // java.lang.Runnable
                public void run() {
                    if (HwActivityManagerServiceEx.access$610() > 0) {
                        Slog.i(HwActivityManagerServiceEx.TAG, "execute slim for [" + processName + " : " + pid + "]");
                        HwPartIawareUtil.reclaimProcessAll(pid, false);
                        HwActivityManagerServiceEx.this.mHwHandler.postDelayed(this, 5000);
                    }
                }
            });
        }
    }

    static /* synthetic */ void lambda$slimProcessAsync$0(String processName, int pid) {
        while (true) {
            try {
                int i = sSlimTimes;
                sSlimTimes = i - 1;
                if (i > 0) {
                    Slog.i(TAG, "#execute slim for [" + processName + " : " + pid + "]");
                    HwPartIawareUtil.reclaimProcessAll(pid, false);
                    Thread.sleep(5000);
                } else {
                    return;
                }
            } catch (InterruptedException e) {
                Slog.e(TAG, e.getMessage());
                return;
            }
        }
    }

    private boolean isAllowSlim() {
        if (ZygoteInit.sIsMygote || IS_TV) {
            return false;
        }
        MemInfoReader memInfo = new MemInfoReader();
        memInfo.readMemInfo();
        if (memInfo.getTotalSizeKb() < SLIM_MEMORY_THRESHOLD) {
            return true;
        }
        return false;
    }

    public boolean isLimitedPackageBroadcast(Intent intent) {
        String action = intent.getAction();
        boolean limitedPackageBroadcast = false;
        if (!"android.intent.action.PACKAGE_ADDED".equals(action) && !"android.intent.action.PACKAGE_REMOVED".equals(action)) {
            return false;
        }
        Bundle intentExtras = intent.getExtras();
        if (intentExtras != null && intentExtras.getBoolean("LimitedPackageBroadcast", false)) {
            limitedPackageBroadcast = true;
        }
        Flog.d(100, "Wear-isLimitedPackageBroadcast: limitedPackageBroadcast = " + limitedPackageBroadcast);
        return limitedPackageBroadcast;
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
                if (0 != 0) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        Log.e(TAG, "load notch screen config: IO Exception while closing stream", e);
                    }
                }
            } else {
                InputStream inputStream2 = new FileInputStream(file);
                XmlPullParser xmlParser = Xml.newPullParser();
                xmlParser.setInput(inputStream2, null);
                for (int xmlEventType = xmlParser.next(); xmlEventType != 1; xmlEventType = xmlParser.next()) {
                    if (xmlEventType == 2 && XML_NOT_PICKCOLOR_APP.equals(xmlParser.getName())) {
                        addPickColorBlackList(xmlParser.getAttributeValue(null, XML_ATTRIBUTE_NAME));
                    } else if (xmlEventType != 3) {
                        continue;
                    } else if (!XML_PICKCOLOR_BLACK_LIST.equals(xmlParser.getName())) {
                    }
                }
                try {
                    inputStream2.close();
                } catch (IOException e2) {
                    Log.e(TAG, "load notch screen config: IO Exception while closing stream", e2);
                }
            }
        } catch (Exception e3) {
            Log.d(TAG, "load PickColorBlackList error!");
            if (0 != 0) {
                try {
                    inputStream.close();
                } catch (IOException e4) {
                    Log.e(TAG, "load notch screen config: IO Exception while closing stream", e4);
                }
            }
        } catch (Throwable th) {
            if (0 != 0) {
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

    public void handleAppDiedLocked(ProcessRecord app, boolean restarting, boolean allowRestart) {
        HwAudioServiceManager.setSoundEffectState(true, app.processName, false, (String) null);
        if (HwCustPkgNameConstant.HW_HWOUC_PACKAGE.equals(app.processName)) {
            Settings.Secure.putIntForUser(this.mContext.getContentResolver(), "hwouc_keyguard_view_on_top", 0, app.userId);
        } else if ("com.huawei.android.FMRadio".equals(app.processName)) {
            Intent intent = new Intent("android.intent.action.FM");
            intent.putExtra("state", 0);
            this.mContext.sendBroadcast(intent);
        } else if (HW_SCREEN_RECORDER.equals(app.processName)) {
            Settings.System.putIntForUser(this.mContext.getContentResolver(), "show_touches", 0, this.mIAmsInner.getUserController().getCurrentUserIdLU());
        } else if ("com.huawei.vdrive".equals(app.processName)) {
            HwFrameworkFactory.getCoverManager().setCoverForbiddened(false);
        } else if (IS_SYSTEMUI_DIE_HANDLE && "com.android.systemui".equals(app.processName)) {
            handleSystemUIDie();
        }
        if (HwMwUtils.ENABLED) {
            HwMwUtils.performPolicy(18, new Object[]{Integer.valueOf(app.uid), Integer.valueOf(app.pid), app.processName});
        }
        Bundle bundle = new Bundle();
        bundle.putInt("uid", app.uid);
        bundle.putInt("pid", app.pid);
        bundle.putString("package", app.info.packageName);
        bundle.putString("android.intent.extra.REASON", "appDie");
        bundle.putString("processName", app.processName);
        bundle.putInt("android.intent.extra.user_handle", app.userId);
        this.mIAmsInner.getAMSForLock().mActivityTaskManager.mHwATMSEx.call(bundle);
    }

    private void handleSystemUIDie() {
        long curTime = System.currentTimeMillis();
        if (curTime - this.systemuiDieLastTime > SYSTEMUI_DIE_INTERVAL_TIME) {
            this.systemuiDieLastTime = curTime;
            this.systemuiDieCount = 1;
            return;
        }
        this.systemuiDieCount++;
        if (this.systemuiDieCount >= 5) {
            Slog.w(TAG, "handleSystemUIDie for obsolete oat files");
            this.mHandler.post(new Runnable() {
                /* class com.android.server.am.$$Lambda$HwActivityManagerServiceEx$jVKWse2yd2JXTAinS6wSF450tg */

                @Override // java.lang.Runnable
                public final void run() {
                    HwActivityManagerServiceEx.this.lambda$handleSystemUIDie$1$HwActivityManagerServiceEx();
                }
            });
            SystemProperties.set("sys.systemui.die.dropcache", "1");
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: deleteSystemUIOat */
    public void lambda$handleSystemUIDie$1$HwActivityManagerServiceEx() {
        PackageManagerService pms = ServiceManager.getService("package");
        try {
            Method method = PackageManagerService.class.getDeclaredMethod("deleteOatArtifactsOfPackage", String.class);
            method.setAccessible(true);
            method.invoke(pms, "com.android.systemui");
        } catch (NoSuchMethodException e) {
            Slog.e(TAG, "cannot find deleteOatArtifactsOfPackage");
        } catch (IllegalAccessException e2) {
            Slog.e(TAG, "cannot access deleteOatArtifactsOfPackage");
        } catch (InvocationTargetException e3) {
            Slog.e(TAG, "cannot invoke deleteOatArtifactsOfPackage");
        }
    }

    public File dumpStackTraces(ProcessRecord app, boolean clearTraces, ArrayList<Integer> firstPids, ProcessCpuTracker processCpuTracker, SparseArray<Boolean> lastPids, ArrayList<Integer> arrayList) {
        if ((app == null || app.info == null || (app.info.flags & 129) != 0) || Log.HWINFO) {
            int[] pids = Process.getPidsForCommands(Watchdog.NATIVE_STACKS_OF_INTEREST);
            ArrayList<Integer> nativePids = null;
            if (pids != null) {
                nativePids = new ArrayList<>(pids.length);
                for (int i : pids) {
                    nativePids.add(Integer.valueOf(i));
                }
            }
            if (mAppEyeBinderBlock != null && clearTraces) {
                ZrHungData data = new ZrHungData();
                data.putString("method", "addBinderPid");
                data.putIntegerArrayList("notnativepids", firstPids);
                data.putIntegerArrayList("nativepids", nativePids);
                if (app != null) {
                    data.putInt("pid", app.pid);
                    data.putInt("tid", app.pid);
                }
                mAppEyeBinderBlock.check(data);
            }
            return ActivityManagerService.dumpStackTraces(firstPids, processCpuTracker, lastPids, nativePids);
        }
        firstPids.clear();
        firstPids.add(Integer.valueOf(app.pid));
        return ActivityManagerService.dumpStackTraces(firstPids, processCpuTracker, lastPids, (ArrayList) null);
    }

    public void updateProcessEntryPointArgsInfo(ProcessRecord app, String[] entryPointArgs) {
        if (!HwPCUtils.isPcCastModeInServer()) {
            app.entryPointArgs = entryPointArgs;
        } else if (app.entryPointArgs == null) {
            app.entryPointArgs = entryPointArgs;
        }
    }

    public void handleApplicationCrash(String exceptionMessage, String packageName) {
        PatchStore.handleApplicationCrashForThirdParty(exceptionMessage, packageName);
    }

    public void registerServiceHooker(IBinder hooker, Intent filter) {
        if (this.mHwServiceHooker == null) {
            this.mHwServiceHooker = new HwServiceHooker();
        }
        this.mHwServiceHooker.registerServiceHooker(hooker, filter);
    }

    public void unregisterServiceHooker(IBinder hooker) {
        HwServiceHooker hwServiceHooker = this.mHwServiceHooker;
        if (hwServiceHooker != null) {
            hwServiceHooker.unregisterServiceHooker(hooker);
        }
    }

    public int bindServiceEx(IApplicationThread caller, IBinder token, Intent service, String resolvedType, IServiceConnection connection, int flags, String instanceName, String callingPackage, int userId, int defaultResult) {
        HwServiceHooker hwServiceHooker = this.mHwServiceHooker;
        return hwServiceHooker != null ? hwServiceHooker.bindServiceEx(caller, token, service, resolvedType, connection, flags, instanceName, callingPackage, userId, defaultResult) : defaultResult;
    }

    public int unbindServiceEx(IServiceConnection connection, int defaultResult) {
        HwServiceHooker hwServiceHooker = this.mHwServiceHooker;
        return hwServiceHooker != null ? hwServiceHooker.unbindServiceEx(connection, defaultResult) : defaultResult;
    }

    public void forceStopPackages(List<String> packagesNames, int userId) {
        if (packagesNames != null) {
            Set<String> runPkgs = this.mIAmsInner.getProcessNames().getMap().keySet();
            for (String pkg : packagesNames) {
                if (runPkgs.contains(pkg)) {
                    this.mIAmsInner.forceStopPackage(pkg, userId);
                }
            }
        }
    }

    private ActivityTaskManagerServiceEx buildAtmsEx(ActivityTaskManagerService atms) {
        ActivityTaskManagerServiceEx atmsEx = new ActivityTaskManagerServiceEx();
        atmsEx.setActivityTaskManagerService(atms);
        return atmsEx;
    }

    private DefaultHwPCMultiWindowManager getHwPCMultiWindowManager(ActivityTaskManagerServiceEx atmsEx) {
        return HwPCFactory.getHwPCFactory().getHwPCFactoryImpl().getHwPCMultiWindowManager(atmsEx);
    }

    public int preloadAppForLauncher(String packageName, int userId, int preloadType) {
        if (!preloadAppForLauncherCkPermision(packageName, userId)) {
            return -1;
        }
        ApplicationInfo appInfo = getAppInfo(packageName, userId).orElse(null);
        if (appInfo == null) {
            Slog.d(TAG, "preloadAppForLauncher, get application info failed, packageName = " + packageName);
            return -1;
        }
        synchronized (this.mIAmsInner.getAMSForLock()) {
            try {
                ProcessRecord app = this.mIAmsInner.getProcessRecordLockedEx(appInfo.processName, appInfo.uid, true);
                if (app != null && app.thread != null) {
                    Slog.d(TAG, "process has started " + packageName + ", proc:" + appInfo.processName);
                    return -2;
                } else if ((appInfo.flags & 9) == 9) {
                    Slog.d(TAG, "preloadApplication, application is persistent, return");
                    return -1;
                } else {
                    if (app == null) {
                        app = this.mIAmsInner.newProcessRecordLockedEx(appInfo, (String) null, false, 0, (HostingRecord) null);
                        if (app == null) {
                            Slog.d(TAG, "TouchDownPreloadApp null newProcessRecordLockedEx, return");
                            return -1;
                        }
                        this.mIAmsInner.updateLruProcessLockedEx(app, false, (ProcessRecord) null);
                        this.mIAmsInner.updateOomAdjLockedEx("preload");
                    }
                    if (app.thread != null) {
                        return -1;
                    }
                    if (preloadType == 101) {
                        app.preloadStatus = 1;
                    }
                    this.mIAmsInner.startProcessLockedEx(app, new HostingRecord("start application", app.processName), (String) null);
                    return 0;
                }
            } catch (Throwable th) {
                th = th;
                throw th;
            }
        }
    }

    private Optional<ApplicationInfo> getAppInfo(String packageName, int userId) {
        if (packageName == null || packageName.isEmpty()) {
            return Optional.empty();
        }
        IPackageManager pm = AppGlobals.getPackageManager();
        if (pm == null) {
            return Optional.empty();
        }
        ApplicationInfo appInfo = null;
        try {
            appInfo = pm.getApplicationInfo(packageName, 1152, userId);
        } catch (RemoteException e) {
            Slog.e(TAG, "RemoteException Failed trying to get application info: " + packageName);
        }
        if (appInfo == null) {
            Slog.d(TAG, "no such application info, pkg = " + packageName);
        }
        return Optional.ofNullable(appInfo);
    }

    private boolean preloadAppForLauncherCkPermision(String packageName, int userId) {
        if (packageName == null || packageName.isEmpty() || userId < 0 || Binder.getCallingUid() != 1000 || Binder.getCallingPid() != Process.myPid()) {
            return false;
        }
        return true;
    }

    public boolean isPreloadEnable() {
        return HwPartIawareUtil.isPreloadEnable();
    }

    public boolean isNeedForbidShellFunc(String packageName) {
        return ForbidShellFuncUtil.isNeedForbidShellFunc(packageName);
    }

    private class BlackListObserver extends ContentObserver {
        BlackListObserver() {
            super(HwActivityManagerServiceEx.this.mHwHandler);
            HwActivityManagerServiceEx.this.mContext.getContentResolver().registerContentObserver(Uri.parse(HwActivityManagerServiceEx.BLACKLIST_URI), false, this);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            Slog.i(HwActivityManagerServiceEx.TAG, "BlackListObserver onChange");
            HwActivityManagerServiceEx hwActivityManagerServiceEx = HwActivityManagerServiceEx.this;
            List unused = HwActivityManagerServiceEx.sAppBlackList = hwActivityManagerServiceEx.getBlacklist(hwActivityManagerServiceEx.mContext);
        }
    }

    private boolean checkAndInitTvBlackList(ApplicationInfo info, final Context context) {
        if (!isTv || info == null) {
            return true;
        }
        if (TV_SYSTEM_MANAGER_PACKAGENAME.equals(info.packageName)) {
            this.mHandler.postDelayed(new Runnable() {
                /* class com.android.server.am.HwActivityManagerServiceEx.AnonymousClass12 */

                @Override // java.lang.Runnable
                public void run() {
                    List unused = HwActivityManagerServiceEx.sAppBlackList = HwActivityManagerServiceEx.this.getBlacklist(context);
                }
            }, 5000);
        }
        String packageName = info.packageName;
        List<String> list = sAppBlackList;
        if (list == null || !list.contains(packageName)) {
            return true;
        }
        Slog.w(TAG, "tv blacklist contain:" + packageName);
        return false;
    }

    public static boolean checkTvBlackList(ApplicationInfo info) {
        if (!isTv) {
            return true;
        }
        String packageName = info.packageName;
        List<String> list = sAppBlackList;
        if (list == null || !list.contains(packageName)) {
            return true;
        }
        Slog.w(TAG, "tv blacklist contain:" + packageName);
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x005a, code lost:
        if (0 == 0) goto L_0x005d;
     */
    private List<String> getBlacklist(Context context) {
        ContentResolver resolver = context.getContentResolver();
        Uri uri = Uri.parse(BLACKLIST_URI);
        Cursor cursor = null;
        List<String> appBlackList = new ArrayList<>();
        try {
            cursor = resolver.query(uri, new String[]{"packageName"}, null, null, null);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        appBlackList.add(cursor.getString(cursor.getColumnIndex("packageName")));
                    }
                    cursor.close();
                    Slog.i(TAG, "tv black list is:" + appBlackList);
                    return appBlackList;
                }
            }
            Slog.i(TAG, "tv black list is empty");
            if (cursor != null) {
                cursor.close();
            }
            return appBlackList;
        } catch (SQLiteException e) {
            Slog.e(TAG, "open database happen SQLiteException");
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
    }

    private boolean isTvMode() {
        int uimode = 1;
        try {
            uimode = IUiModeManager.Stub.asInterface(ServiceManager.getService("uimode")).getCurrentModeType();
        } catch (RemoteException e) {
            Slog.e(TAG, "get ui mode type err");
        }
        return uimode == 4;
    }

    public void notifyProcessStatusChange(String pkg, int pid, int uid) {
        if (Swap.getInstance().isSwapEnabled()) {
            SceneProcessing.getInstance().notifySceneData("PROCESS_CREATE", new SceneConst.ScenePara(pkg, pid, uid));
            Slog.i(TAG, "notifyProcessStatusChange::pkg:" + pkg + " pid:" + pid + " uid:" + uid);
        } else if (AliveReport.getInstance().getInitStatus()) {
            AliveReport.getInstance().notifyProcessCreate(pkg, pid, uid);
        }
    }

    private Map<String, Integer> getDelayApps() {
        InputStream rawIs = null;
        Map<String, Integer> apps = new HashMap<>();
        try {
            InputStream rawIs2 = new FileInputStream(new File(DELAY_APPS_FILENAME));
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(rawIs2, "utf-8");
            for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                if (eventType != 0) {
                    if (eventType == 2) {
                        if ("item".equals(parser.getName())) {
                            apps.put(parser.getAttributeValue(0), Integer.valueOf(parser.getAttributeValue(1)));
                            parser.next();
                        }
                    }
                }
            }
            try {
                rawIs2.close();
            } catch (IOException e) {
                Slog.e(TAG, "close InputStream failed");
            }
        } catch (FileNotFoundException e2) {
            Slog.e(TAG, "delay apps file not found");
            if (0 != 0) {
                rawIs.close();
            }
        } catch (XmlPullParserException e3) {
            int col = e3.getColumnNumber();
            int line = e3.getLineNumber();
            Slog.e(TAG, "failed parsing file parser error at line:" + line + ",col:" + col);
            if (0 != 0) {
                rawIs.close();
            }
        } catch (IOException e4) {
            Slog.e(TAG, "value number format error");
            if (0 != 0) {
                rawIs.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    rawIs.close();
                } catch (IOException e5) {
                    Slog.e(TAG, "close InputStream failed");
                }
            }
            throw th;
        }
        return apps;
    }

    private ApplicationInfo getApplicationInfo(List<ApplicationInfo> apps, String packageName) {
        for (ApplicationInfo app : apps) {
            if (packageName.equals(app.packageName)) {
                return app;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void startDelayApps(final List<ApplicationInfo> delayAppInfos, final Map<String, Integer> delayApps) {
        if (delayAppInfos.size() == 0) {
            Slog.e(TAG, "No app need to delay start");
        } else {
            new Thread("startPersistentAppsDelay") {
                /* class com.android.server.am.HwActivityManagerServiceEx.AnonymousClass13 */

                @Override // java.lang.Thread, java.lang.Runnable
                public void run() {
                    int delayTimeLast = 0;
                    try {
                        for (ApplicationInfo app : delayAppInfos) {
                            if (app != null) {
                                int appDelayTime = ((Integer) delayApps.get(app.packageName)).intValue();
                                Thread.sleep((long) (appDelayTime - delayTimeLast));
                                synchronized (HwActivityManagerServiceEx.this.mIAmsInner.getAMSForLock()) {
                                    HwActivityManagerServiceEx.this.mIAmsInner.getAMSForLock().addAppLocked(app, (String) null, false, (String) null);
                                }
                                delayTimeLast = appDelayTime;
                            }
                        }
                    } catch (InterruptedException e) {
                        Slog.e(HwActivityManagerServiceEx.TAG, "Catch a interrupted exception to protect the system process");
                    }
                }
            }.start();
        }
    }

    public void startPersistentAppsDelay(int matchFlags) {
        if (FactoryTest.getMode() != 1) {
            try {
                Map<String, Integer> delayApps = getDelayApps();
                Comparator<Map.Entry<String, Integer>> valueComparator = new Comparator<Map.Entry<String, Integer>>() {
                    /* class com.android.server.am.HwActivityManagerServiceEx.AnonymousClass14 */

                    public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                        return o1.getValue().intValue() - o2.getValue().intValue();
                    }
                };
                List<Map.Entry<String, Integer>> list = new ArrayList<>(delayApps.entrySet());
                Collections.sort(list, valueComparator);
                List<String> delayAppsPkgName = new ArrayList<>();
                for (Map.Entry<String, Integer> entry : list) {
                    delayAppsPkgName.add(entry.getKey());
                }
                List<ApplicationInfo> delayAppInfos = new ArrayList<>();
                synchronized (this.mIAmsInner.getAMSForLock()) {
                    List<ApplicationInfo> apps = AppGlobals.getPackageManager().getPersistentApplications(matchFlags | 1024).getList();
                    Iterator<ApplicationInfo> it = apps.iterator();
                    while (it.hasNext()) {
                        ApplicationInfo app = it.next();
                        if (!delayAppsPkgName.contains(app.packageName) && !"android".equals(app.packageName)) {
                            this.mIAmsInner.getAMSForLock().addAppLocked(app, (String) null, false, (String) null);
                            it.remove();
                        }
                    }
                    for (String packageName : delayAppsPkgName) {
                        ApplicationInfo appInfo = getApplicationInfo(apps, packageName);
                        if (appInfo != null) {
                            delayAppInfos.add(appInfo);
                        }
                    }
                }
                startDelayApps(delayAppInfos, delayApps);
            } catch (RemoteException e) {
                Slog.e(TAG, "Catch a remote exception to protect the system process");
            }
        }
    }
}
