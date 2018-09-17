package com.android.server.am;

import android.app.AlertDialog;
import android.app.AppGlobals;
import android.app.IActivityController;
import android.app.IActivityController.Stub;
import android.app.IApplicationThread;
import android.app.INotificationManager;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.mtm.MultiTaskManager;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.IIntentReceiver;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.IMWThirdpartyCallback;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.rms.HwSysResManager;
import android.rms.HwSysResource;
import android.rms.iaware.AwareConstant.ResourceType;
import android.rms.iaware.CollectData;
import android.rms.iaware.DataContract.Apps;
import android.rms.iaware.DataContract.Input;
import android.rms.iaware.DataContract.Input.Builder;
import android.rms.iaware.LogIAware;
import android.rog.AppRogInfo;
import android.rog.AppRogInfo.UpdateRog;
import android.rog.IHwRogListener;
import android.rog.IRogManager;
import android.service.notification.StatusBarNotification;
import android.util.ArrayMap;
import android.util.Flog;
import android.util.Log;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerPolicy.PointerEventListener;
import android.widget.Toast;
import com.android.internal.app.procstats.ProcessStats.ProcessStateHolder;
import com.android.internal.os.HwBootCheck;
import com.android.internal.os.HwBootFail;
import com.android.server.AlarmManagerService;
import com.android.server.HwConnectivityService;
import com.android.server.LocalServices;
import com.android.server.PPPOEStateMachine;
import com.android.server.SMCSAMSHelper;
import com.android.server.ServiceThread;
import com.android.server.UiThread;
import com.android.server.Watchdog;
import com.android.server.am.AbsActivityManager.AppDiedInfo;
import com.android.server.am.InterceptAppLaunchUtils.InterceptAppDialog;
import com.android.server.jankshield.TableJankEvent;
import com.android.server.location.HwGpsPowerTracker;
import com.android.server.location.NetworkPosErrorEvent;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort;
import com.android.server.mtm.iaware.appmng.AwareProcessBaseInfo;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.pfw.HwPFWService;
import com.android.server.pfw.autostartup.comm.XmlConst.ControlScope;
import com.android.server.pm.HwPackageManagerService;
import com.android.server.pm.PackageManagerService;
import com.android.server.rms.iaware.appmng.AwareDefaultConfigList;
import com.android.server.rms.iaware.cpu.CPUFeature;
import com.android.server.rms.iaware.cpu.CPUFeatureAMSCommunicator;
import com.android.server.rms.iaware.cpu.CPUKeyBackground;
import com.android.server.rms.iaware.cpu.CPUResourceConfigControl;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.rms.iaware.memory.utils.EventTracker;
import com.android.server.rms.iaware.memory.utils.MemoryUtils;
import com.android.server.rms.iaware.srms.BroadcastFeature;
import com.android.server.rms.iaware.srms.ResourceFeature;
import com.android.server.rms.iaware.srms.SRMSDumpRadar;
import com.android.server.rms.shrinker.ProcessStopShrinker;
import com.android.server.security.trustcircle.IOTController;
import com.android.server.security.trustcircle.lifecycle.LifeCycleStateMachine;
import com.android.server.security.trustspace.TrustSpaceManagerInternal;
import com.android.server.util.AbsUserBehaviourRecord;
import com.android.server.util.HwUserBehaviourRecord;
import com.android.server.wifipro.WifiProCommonDefs;
import com.android.server.wifipro.WifiProCommonUtils;
import com.android.server.wm.WindowManagerService;
import com.huawei.android.pushagentproxy.PushService;
import com.huawei.android.smcs.STProcessRecord;
import com.huawei.hsm.permission.ANRFilter;
import com.huawei.pgmng.log.LogPower;
import huawei.com.android.server.policy.HwGlobalActionsData;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;

public final class HwActivityManagerService extends ActivityManagerService {
    public static final int BACKUP_APP_ADJ = 300;
    static final boolean DEBUG_HWTRIM;
    static final boolean DEBUG_HWTRIM_PERFORM;
    private static final int EMPTY_PROCESS_LIMIT = 4;
    public static final int FOREGROUND_APP_ADJ = 0;
    public static final int HEAVY_WEIGHT_APP_ADJ = 400;
    public static final int HOME_APP_ADJ = 600;
    private static final String HW_TRIM_MEMORY_ACTION = "huawei.intent.action.HW_TRIM_MEMORY_ACTION";
    static final int IS_IN_MULTIWINDOW_MODE_TRANSACTION = 3103;
    public static final boolean IS_SUPPORT_CLONE_APP;
    public static final int NATIVE_ADJ = -1000;
    public static final int PERCEPTIBLE_APP_ADJ = 200;
    private static final int PERSISTENT_MASK = 9;
    public static final int PERSISTENT_PROC_ADJ = -800;
    public static final int PERSISTENT_SERVICE_ADJ = -700;
    public static final int PREVIOUS_APP_ADJ = 700;
    private static final int QUEUE_NUM_DEFAULT = 2;
    private static final int QUEUE_NUM_IAWARE = 6;
    private static final int QUEUE_NUM_RMS = 4;
    static final int REGISTER_THIRD_PARTY_CALLBACK_TRANSACTION = 3101;
    private static final int ROG_CHANGE_EVENT_INFO = 2;
    private static final int ROG_CHANGE_EVENT_SWITCH = 1;
    public static final int SERVICE_ADJ = 500;
    public static final int SERVICE_B_ADJ = 800;
    private static final String SETTING_GUEST_HAS_LOGGED_IN = "guest_has_logged_in";
    static final int SET_CUSTOM_ACTIVITY_CONTROLLER_TRANSACTION = 2101;
    private static final int SHOW_GUEST_SWITCH_DIALOG_MSG = 50;
    private static final int SHOW_INTERCEPT_APP_DIALOG_MSG = 60;
    private static final int SHOW_SWITCH_DIALOG_MSG = 49;
    static final int SHOW_UNINSTALL_LAUNCHER_MSG = 48;
    private static final int SMART_TRIM_ADJ_LIMIT;
    private static final int SMART_TRIM_BEGIN_HW_SYSM = 41;
    private static final int SMART_TRIM_POST_MSG_DELAY = 10;
    private static final int START_HW_SERVICE_POST_MSG_DELAY = 60000;
    public static final int SYSTEM_ADJ = -900;
    private static final String SYSTEM_DEBUGGABLE = "ro.debuggable";
    static final String TAG = "HwActivityManagerService";
    static final int UNREGISTER_THIRD_PARTY_CALLBACK_TRANSACTION = 3102;
    public static final int VISIBLE_APP_ADJ = 100;
    private static boolean enableIaware;
    static final boolean enableRms;
    private static IBinder mAudioService;
    private static final boolean mIsSMCSHWSYSMEnabled;
    private static HwActivityManagerService mSelf;
    private static HashMap<String, Integer> sHardCodeAppToSetOomAdjArrays;
    static final boolean smcsLOGV;
    private AlarmManagerService mAlms;
    private HwSysResource mAppResource;
    public HwSysResource mAppServiceResource;
    private final ArrayMap<Integer, ArrayMap<Integer, Long>> mAssocMap;
    private Handler mBootCheckHandler;
    private HwSysResource mBroadcastResource;
    private String mCloneAppList;
    private AbsUserBehaviourRecord mCust;
    ActivityRecord mFocusedActivityForNavi;
    Handler mHandler;
    final Handler mHwHandler;
    final ServiceThread mHwHandlerThread;
    private InterceptAppDialog mInterceptAppDialog;
    private String mLastLauncherName;
    private ResetSessionDialog mNewSessionDialog;
    private HwSysResource mOrderedBroadcastResource;
    OverscanTimeout mOverscanTimeout;
    private Map<Integer, Set<String>> mPackagesRequireC2DMWake;
    private boolean[] mScreenStatusRequest;
    private SettingsObserver mSettingsObserver;
    private Stack<IBinder> mSplitActivityEntryStack;
    private String mSplitExtras;
    private Intent mSplitIntentInfo;
    private RemoteCallbackList<IMWThirdpartyCallback> mThirdPartyCallbackList;
    private String mTrimProcName;
    private int mTrimProcUid;
    private String mTrimType;
    private TrustSpaceManagerInternal mTrustSpaceManagerInternal;

    /* renamed from: com.android.server.am.HwActivityManagerService.2 */
    class AnonymousClass2 extends Handler {
        AnonymousClass2(Looper $anonymous0) {
            super($anonymous0);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case NetworkPosErrorEvent.NETWORK_POSITION_TIMEOUT /*22*/:
                    synchronized (HwActivityManagerService.this) {
                        int appid = msg.arg1;
                        boolean restart = msg.arg2 == HwActivityManagerService.ROG_CHANGE_EVENT_SWITCH ? true : HwActivityManagerService.IS_SUPPORT_CLONE_APP;
                        Bundle bundle = msg.obj;
                        String pkg = bundle.getString(HwGpsPowerTracker.DEL_PKG);
                        String reason = bundle.getString("reason");
                        Slog.w(HwActivityManagerService.TAG, "tsy1 stopping pkg excuting at: " + pkg);
                        HwActivityManagerService.this.hwForceStopPackageLocked(pkg, appid, restart, HwActivityManagerService.IS_SUPPORT_CLONE_APP, true, HwActivityManagerService.IS_SUPPORT_CLONE_APP, HwActivityManagerService.IS_SUPPORT_CLONE_APP, -1, reason);
                        Slog.w(HwActivityManagerService.TAG, "tsy1 end stopping" + pkg);
                        break;
                    }
                default:
            }
        }
    }

    private final class BootCheckHandler extends Handler {
        public BootCheckHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HwActivityManagerService.VISIBLE_APP_ADJ /*100*/:
                case WifiProCommonDefs.TYEP_HAS_INTERNET /*101*/:
                case WifiProCommonUtils.HISTORY_TYPE_PORTAL /*102*/:
                    try {
                        if (ActivityManagerDebugConfig.HWFLOW) {
                            Slog.i("ActivityManager_FLOW", "mBootCheckHandler: " + msg.what + ",mActivityIdle: " + HwActivityManagerService.this.mActivityIdle);
                        }
                        HwBootCheck.addBootInfo("currBootScene is: " + msg.what);
                        HwBootCheck.addBootInfo("mSystemReady is: " + HwActivityManagerService.this.mSystemReady);
                        HwActivityManagerService.this.bootSceneEnd(msg.what);
                        if (!HwActivityManagerService.this.mActivityIdle) {
                            HwActivityManagerService.this.addBootFailedLog();
                        }
                    } catch (Exception ex) {
                        Flog.e(HwActivityManagerService.VISIBLE_APP_ADJ, "BootCheckHandler exception: " + ex.toString());
                    }
                default:
            }
        }
    }

    static final class IawarePointerEventListener implements PointerEventListener {
        IawarePointerEventListener() {
        }

        public void onPointerEvent(MotionEvent motionEvent) {
            int action = motionEvent.getAction();
            if (action == 0 || action == HwActivityManagerService.ROG_CHANGE_EVENT_SWITCH) {
                HwSysResManager resManager = HwSysResManager.getInstance();
                if (resManager != null && resManager.isResourceNeeded(ResourceType.getReousrceId(ResourceType.RES_INPUT))) {
                    Builder builder = Input.builder();
                    if (action == 0) {
                        builder.addEvent(10001);
                    } else {
                        builder.addEvent(80001);
                    }
                    CollectData appsData = builder.build();
                    long id = Binder.clearCallingIdentity();
                    resManager.reportData(appsData);
                    Binder.restoreCallingIdentity(id);
                }
            }
        }
    }

    class OverscanTimeout implements Runnable {
        OverscanTimeout() {
        }

        public void run() {
            Slog.i(HwActivityManagerService.TAG, "OverscanTimeout run");
            Global.putString(HwActivityManagerService.this.mContext.getContentResolver(), "single_hand_mode", AppHibernateCst.INVALID_PKG);
        }
    }

    private class ResetSessionDialog extends AlertDialog implements OnClickListener {
        private final int mUserId;

        public ResetSessionDialog(Context context, int userId) {
            super(context, context.getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog", null, null));
            getWindow().setType(2014);
            getWindow().addFlags(655360);
            if (((KeyguardManager) context.getSystemService("keyguard")).isKeyguardLocked()) {
                getWindow().addPrivateFlags(Integer.MIN_VALUE);
            }
            setMessage(context.getString(33685834));
            setButton(-1, context.getString(33685836), this);
            setButton(-2, context.getString(33685835), this);
            setCanceledOnTouchOutside(HwActivityManagerService.IS_SUPPORT_CLONE_APP);
            this.mUserId = userId;
        }

        public void onClick(DialogInterface dialog, int which) {
            Slog.i(HwActivityManagerService.TAG, "onClick which:" + which);
            if (which == -2) {
                HwActivityManagerService.this.wipeGuestSession(this.mUserId);
                dismiss();
            } else if (which == -1) {
                cancel();
                HwActivityManagerService.this.sendMessageToSwitchUser(this.mUserId, HwActivityManagerService.this.getGuestName());
            }
        }
    }

    class ScreenStatusReceiver extends BroadcastReceiver {
        ScreenStatusReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.stk.check_screen_idle".equals(intent.getAction())) {
                int slotId = intent.getIntExtra("slot_id", HwActivityManagerService.SMART_TRIM_ADJ_LIMIT);
                if (slotId < 0 || slotId >= HwActivityManagerService.this.mScreenStatusRequest.length) {
                    Slog.w(HwActivityManagerService.TAG, "ScreenStatusReceiver, slotId " + slotId + " Invalid");
                    return;
                }
                HwActivityManagerService.this.mScreenStatusRequest[slotId] = intent.getBooleanExtra("SCREEN_STATUS_REQUEST", HwActivityManagerService.IS_SUPPORT_CLONE_APP);
                if (HwActivityManagerService.this.mScreenStatusRequest[slotId]) {
                    ActivityRecord p = HwActivityManagerService.this.getFocusedStack().topRunningActivityLocked();
                    if (p != null) {
                        Intent StkIntent = new Intent("android.intent.action.stk.idle_screen");
                        if (p.intent.hasCategory("android.intent.category.HOME")) {
                            StkIntent.putExtra("SCREEN_IDLE", true);
                        } else {
                            StkIntent.putExtra("SCREEN_IDLE", HwActivityManagerService.IS_SUPPORT_CLONE_APP);
                        }
                        StkIntent.putExtra("slot_id", slotId);
                        HwActivityManagerService.this.mContext.sendBroadcast(StkIntent, "com.huawei.permission.CAT_IDLE_SCREEN");
                        if (ActivityManagerDebugConfig.DEBUG_ALL) {
                            Slog.v(HwActivityManagerService.TAG, "Broadcasting Home Idle Screen Intent ... slot: " + slotId);
                        }
                    }
                } else if (ActivityManagerDebugConfig.DEBUG_ALL) {
                    Slog.v(HwActivityManagerService.TAG, "Screen Status request is OFF, slot: " + slotId);
                }
            }
        }
    }

    private final class SettingsObserver extends ContentObserver {
        private final Uri CLONE_APP_LIST_URI;

        SettingsObserver(Handler handler) {
            super(handler);
            this.CLONE_APP_LIST_URI = Secure.getUriFor("clone_app_list");
            ContentResolver resolver = HwActivityManagerService.this.mContext.getContentResolver();
            resolver.registerContentObserver(this.CLONE_APP_LIST_URI, HwActivityManagerService.IS_SUPPORT_CLONE_APP, this, HwActivityManagerService.SMART_TRIM_ADJ_LIMIT);
            HwActivityManagerService.this.mCloneAppList = Secure.getStringForUser(resolver, "clone_app_list", HwActivityManagerService.SMART_TRIM_ADJ_LIMIT);
        }

        public void onChange(boolean selfChange, Uri uri) {
            if (this.CLONE_APP_LIST_URI.equals(uri)) {
                String cloneAppList = Secure.getStringForUser(HwActivityManagerService.this.mContext.getContentResolver(), "clone_app_list", HwActivityManagerService.SMART_TRIM_ADJ_LIMIT);
                Flog.i(HwActivityManagerService.VISIBLE_APP_ADJ, "Secure.clone_app_list is changed, old is " + HwActivityManagerService.this.mCloneAppList + ", new is " + cloneAppList);
                if (!(HwActivityManagerService.this.mCloneAppList == null || HwActivityManagerService.this.mCloneAppList.equals(cloneAppList))) {
                    String[] packages = HwActivityManagerService.this.mCloneAppList.split(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
                    int length = packages.length;
                    for (int i = HwActivityManagerService.SMART_TRIM_ADJ_LIMIT; i < length; i += HwActivityManagerService.ROG_CHANGE_EVENT_SWITCH) {
                        String pkg = packages[i];
                        if (!(cloneAppList + CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER).contains(pkg + CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER)) {
                            synchronized (HwActivityManagerService.mSelf) {
                                HwActivityManagerService.this.deleteClonedPackage(pkg);
                                for (int i2 = HwActivityManagerService.this.mRecentTasks.size() - 1; i2 >= 0; i2--) {
                                    TaskRecord tr = (TaskRecord) HwActivityManagerService.this.mRecentTasks.get(i2);
                                    String taskPackageName = tr.getBaseIntent().getComponent().getPackageName();
                                    if (tr.userId == 0 && taskPackageName.equals(pkg) && tr.multiLaunchId != 0) {
                                        HwActivityManagerService.this.removeTaskByIdLocked(tr.taskId, HwActivityManagerService.IS_SUPPORT_CLONE_APP, true);
                                    }
                                }
                                try {
                                    HwActivityManagerService.this.mInstaller.rmClonedAppDataDir(Environment.getDataDirectory() + File.separator + "data" + File.separator + pkg + File.separator + "_hwclone");
                                } catch (Exception e) {
                                    Flog.i(HwActivityManagerService.VISIBLE_APP_ADJ, "Failed to rm cloned app data dir", e);
                                }
                                Flog.i(HwActivityManagerService.VISIBLE_APP_ADJ, "Successfully clean up when deleting cloned app " + pkg);
                            }
                        }
                    }
                }
                HwActivityManagerService.this.mCloneAppList = cloneAppList;
            }
        }
    }

    class TrimMemoryReceiver extends BroadcastReceiver {
        TrimMemoryReceiver() {
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onReceive(Context context, Intent intent) {
            if (!(intent == null || intent.getAction() == null || !HwActivityManagerService.HW_TRIM_MEMORY_ACTION.equals(intent.getAction()))) {
                HwActivityManagerService.this.trimGLMemory(80);
            }
        }
    }

    static {
        smcsLOGV = SystemProperties.getBoolean("ro.enable.st_debug", IS_SUPPORT_CLONE_APP);
        DEBUG_HWTRIM = smcsLOGV;
        DEBUG_HWTRIM_PERFORM = smcsLOGV;
        enableRms = SystemProperties.getBoolean("ro.config.enable_rms", IS_SUPPORT_CLONE_APP);
        SMART_TRIM_ADJ_LIMIT = SystemProperties.getInt("ro.smart_trim.adj", 3);
        mIsSMCSHWSYSMEnabled = SystemProperties.getBoolean("ro.enable.hwsysm_smcs", true);
        sHardCodeAppToSetOomAdjArrays = new HashMap();
        sHardCodeAppToSetOomAdjArrays.put("com.huawei.android.pushagent.PushService", Integer.valueOf(PERCEPTIBLE_APP_ADJ));
        IS_SUPPORT_CLONE_APP = SystemProperties.getBoolean("ro.config.hw_support_clone_app", IS_SUPPORT_CLONE_APP);
        enableIaware = SystemProperties.getBoolean("persist.sys.enable_iaware", IS_SUPPORT_CLONE_APP);
        mAudioService = null;
    }

    public HwActivityManagerService(Context mContext) {
        super(mContext);
        this.mAssocMap = new ArrayMap();
        this.mTrimProcName = null;
        this.mTrimProcUid = -1;
        this.mTrimType = null;
        this.mScreenStatusRequest = new boolean[]{IS_SUPPORT_CLONE_APP, IS_SUPPORT_CLONE_APP};
        this.mPackagesRequireC2DMWake = new HashMap();
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case HwActivityManagerService.SMART_TRIM_BEGIN_HW_SYSM /*41*/:
                        HwActivityManagerService.this.hwTrimApk_HwSysM(HwActivityManagerService.this.mTrimProcName, HwActivityManagerService.this.mTrimProcUid, HwActivityManagerService.this.mTrimType);
                    case HwActivityManagerService.SHOW_UNINSTALL_LAUNCHER_MSG /*48*/:
                        HwActivityManagerService.this.showUninstallLauncher();
                    case HwActivityManagerService.SHOW_SWITCH_DIALOG_MSG /*49*/:
                        HwActivityManagerService.this.mUserController.showUserSwitchDialog((Pair) msg.obj);
                    case HwActivityManagerService.SHOW_GUEST_SWITCH_DIALOG_MSG /*50*/:
                        HwActivityManagerService.this.showGuestSwitchDialog(msg.arg1, (String) msg.obj);
                    case HwActivityManagerService.SHOW_INTERCEPT_APP_DIALOG_MSG /*60*/:
                        HwActivityManagerService.this.showInterceptAppDialog((String) msg.obj);
                    default:
                }
            }
        };
        this.mFocusedActivityForNavi = null;
        this.mOverscanTimeout = new OverscanTimeout();
        mSelf = this;
        this.mHwHandlerThread = new ServiceThread(TAG, -2, IS_SUPPORT_CLONE_APP);
        this.mHwHandlerThread.start();
        this.mHwHandler = new AnonymousClass2(this.mHwHandlerThread.getLooper());
        this.mBootCheckHandler = new BootCheckHandler(HwBootCheck.getHandler().getLooper());
        bootSceneStart(VISIBLE_APP_ADJ, AppHibernateCst.DELAY_ONE_MINS);
    }

    public static HwActivityManagerService self() {
        return mSelf;
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        switch (code) {
            case 502:
                data.enforceInterface("android.app.IActivityManager");
                boolean res = handleANRFilterFIFO(data.readInt(), data.readInt());
                reply.writeNoException();
                reply.writeInt(res ? ROG_CHANGE_EVENT_SWITCH : SMART_TRIM_ADJ_LIMIT);
                return true;
            case 503:
                data.enforceInterface("android.app.IActivityManager");
                boolean isClonedProcess = isClonedProcess(data.readInt());
                reply.writeNoException();
                reply.writeInt(isClonedProcess ? ROG_CHANGE_EVENT_SWITCH : SMART_TRIM_ADJ_LIMIT);
                return true;
            case 504:
                data.enforceInterface("android.app.IActivityManager");
                String packageName = getPackageNameForPid(data.readInt());
                reply.writeNoException();
                reply.writeString(packageName);
                return true;
            case 505:
                data.enforceInterface("android.app.IActivityManager");
                boolean isPackageCloned = isPackageCloned(data.readString(), data.readInt());
                reply.writeNoException();
                reply.writeInt(isPackageCloned ? ROG_CHANGE_EVENT_SWITCH : SMART_TRIM_ADJ_LIMIT);
                return true;
            case 506:
                data.enforceInterface("android.app.IActivityManager");
                int isPreloadSuccess = preloadApplication(data.readString(), data.readInt(), data.readInt() == ROG_CHANGE_EVENT_SWITCH ? true : IS_SUPPORT_CLONE_APP);
                reply.writeNoException();
                reply.writeInt(isPreloadSuccess);
                return true;
            case WifiProCommonUtils.RESP_CODE_UNSTABLE /*601*/:
                data.enforceInterface("android.app.IActivityManager");
                setLastSplittableActivity((Intent) data.readParcelable(null), data.readString());
                reply.writeNoException();
                return true;
            case WifiProCommonUtils.RESP_CODE_GATEWAY /*602*/:
                data.enforceInterface("android.app.IActivityManager");
                boolean same = isLastSplittableActivity((Intent) data.readParcelable(null), data.readString());
                reply.writeNoException();
                reply.writeInt(same ? ROG_CHANGE_EVENT_SWITCH : SMART_TRIM_ADJ_LIMIT);
                return true;
            case WifiProCommonUtils.RESP_CODE_INVALID_URL /*603*/:
                data.enforceInterface("android.app.IActivityManager");
                addToEntryStack(data.readStrongBinder(), data.readInt(), (Intent) Intent.CREATOR.createFromParcel(data));
                reply.writeNoException();
                return true;
            case WifiProCommonUtils.RESP_CODE_ABNORMAL_SERVER /*604*/:
                data.enforceInterface("android.app.IActivityManager");
                clearEntryStack(data.readStrongBinder());
                return true;
            case WifiProCommonUtils.RESP_CODE_REDIRECTED_HOST_CHANGED /*605*/:
                data.enforceInterface("android.app.IActivityManager");
                removeFromEntryStack(data.readStrongBinder());
                return true;
            case WifiProCommonUtils.RESP_CODE_CONN_RESET /*606*/:
                data.enforceInterface("android.app.IActivityManager");
                boolean isTop = isTopSplitActivity(data.readStrongBinder());
                reply.writeNoException();
                reply.writeInt(isTop ? ROG_CHANGE_EVENT_SWITCH : SMART_TRIM_ADJ_LIMIT);
                return true;
            case SET_CUSTOM_ACTIVITY_CONTROLLER_TRANSACTION /*2101*/:
                data.enforceInterface("android.app.IActivityManager");
                setCustomActivityController(Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                return true;
            case REGISTER_THIRD_PARTY_CALLBACK_TRANSACTION /*3101*/:
                data.enforceInterface("android.app.IActivityManager");
                boolean registered = registerThirdPartyCallBack(IMWThirdpartyCallback.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                reply.writeInt(registered ? ROG_CHANGE_EVENT_SWITCH : SMART_TRIM_ADJ_LIMIT);
                return true;
            case UNREGISTER_THIRD_PARTY_CALLBACK_TRANSACTION /*3102*/:
                data.enforceInterface("android.app.IActivityManager");
                boolean unregistered = unregisterThirdPartyCallBack(IMWThirdpartyCallback.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                reply.writeInt(unregistered ? ROG_CHANGE_EVENT_SWITCH : SMART_TRIM_ADJ_LIMIT);
                return true;
            case IS_IN_MULTIWINDOW_MODE_TRANSACTION /*3103*/:
                data.enforceInterface("android.app.IActivityManager");
                boolean result = isInMultiWindowMode();
                reply.writeNoException();
                reply.writeInt(result ? ROG_CHANGE_EVENT_SWITCH : SMART_TRIM_ADJ_LIMIT);
                return true;
            case 1599294787:
                if (DEBUG_HWTRIM) {
                    Log.v(TAG, "AMS.onTransact: got HWMEMCLEAN_TRANSACTION");
                }
                if (!mIsSMCSHWSYSMEnabled) {
                    if (DEBUG_HWTRIM) {
                        Log.v(TAG, "AMS.onTransact: HWSysM SMCS is disabled.");
                        break;
                    }
                } else if (this.mContext == null) {
                    return IS_SUPPORT_CLONE_APP;
                } else {
                    if (this.mContext.checkCallingPermission("huawei.permission.HSM_SMCS") != 0) {
                        if (DEBUG_HWTRIM) {
                            Log.e(TAG, "SMCSAMSHelper.handleTransact permission deny");
                        }
                        return IS_SUPPORT_CLONE_APP;
                    }
                    if (SMCSAMSHelper.getInstance().handleTransact(data, reply, flags)) {
                        return true;
                    }
                }
                break;
        }
        return super.onTransact(code, data, reply, flags);
    }

    protected void updateUsageStats(ActivityRecord resumedComponent, boolean resumed) {
        if (resumed && mIsSMCSHWSYSMEnabled) {
            SMCSAMSHelper.getInstance().smartTrimProcessPackageResume(resumedComponent.realActivity, resumedComponent.processName);
        }
        super.updateUsageStats(resumedComponent, resumed);
    }

    public void killApplication(String pkg, int appId, int userId, String reason) {
        if (!"vold reset".equals(reason) || !"com.android.providers.media".equals(pkg)) {
            super.killApplication(pkg, appId, userId, reason);
        } else if (pkg != null) {
            if (appId < 0) {
                Slog.w(TAG, "Invalid appid specified for pkg : " + pkg);
                return;
            }
            int callerUid = Binder.getCallingUid();
            if (UserHandle.getAppId(callerUid) == IOTController.TYPE_MASTER) {
                Message msg = this.mHwHandler.obtainMessage(22);
                msg.arg1 = appId;
                msg.arg2 = SMART_TRIM_ADJ_LIMIT;
                Bundle bundle = new Bundle();
                Slog.w(TAG, "tsy1 stopping pkg at : " + pkg);
                bundle.putString(HwGpsPowerTracker.DEL_PKG, pkg);
                bundle.putString("reason", reason);
                msg.obj = bundle;
                this.mHwHandler.sendMessage(msg);
            } else {
                throw new SecurityException(callerUid + " cannot kill pkg: " + pkg);
            }
        }
    }

    public boolean handleANRFilterFIFO(int uid, int cmd) {
        Log.d(TAG, "handleANRFilterFIFO,uid = " + uid + "cmd = " + cmd);
        switch (cmd) {
            case SMART_TRIM_ADJ_LIMIT:
                return ANRFilter.getInstance().addUid(uid);
            case ROG_CHANGE_EVENT_SWITCH /*1*/:
                return ANRFilter.getInstance().removeUid(uid);
            case ROG_CHANGE_EVENT_INFO /*2*/:
                return ANRFilter.getInstance().checkUid(uid);
            default:
                return IS_SUPPORT_CLONE_APP;
        }
    }

    public final void hwTrimApkPost_HwSysM(String trimProc, int uid, String trimType) {
        long timeStart = 0;
        try {
            if (DEBUG_HWTRIM_PERFORM) {
                Log.v(TAG, "AMS.hwTrimApkPost_HwSysM");
                timeStart = System.currentTimeMillis();
            }
            this.mTrimProcName = trimProc;
            this.mTrimProcUid = uid;
            this.mTrimType = trimType;
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    HwActivityManagerService.this.mHandler.sendEmptyMessage(HwActivityManagerService.SMART_TRIM_BEGIN_HW_SYSM);
                }
            }, 10);
            if (DEBUG_HWTRIM_PERFORM) {
                Log.v(TAG, "AMS.hwTrimApkPost_HwSysM: cost " + (System.currentTimeMillis() - timeStart) + " ms end.");
            }
        } catch (Exception e) {
            Log.e(TAG, "AMS.hwTrimApkPost_HwSysM: catch exception: " + e.toString());
        }
    }

    public final void hwTrimApk_HwSysM(String trimProc, int uid, String trimType) {
        Throwable th;
        long timeStart = 0;
        try {
            if (DEBUG_HWTRIM_PERFORM) {
                Log.v(TAG, "AMS.hwTrimApk");
                timeStart = System.currentTimeMillis();
            }
            synchronized (this) {
                try {
                    ProcessRecord app = getProcessRecordLocked(trimProc, uid, true);
                    if (DEBUG_HWTRIM_PERFORM) {
                        Log.v(TAG, "AMS.hwTrimApk_HwSysM");
                        long timeGetApp = System.currentTimeMillis() - timeStart;
                        Log.v(TAG, "AMS.hwTrimApk_HwSysM: get app cost " + timeGetApp + " ms end.");
                    }
                    if (app != null && app.curAdj > SMART_TRIM_ADJ_LIMIT) {
                        if (DEBUG_HWTRIM) {
                            Log.v(TAG, "AMS.hwTrimApk_HwSysM: go to trim " + app.processName);
                        }
                        removeProcessLocked(app, IS_SUPPORT_CLONE_APP, IS_SUPPORT_CLONE_APP, "smart trim");
                        HashSet<String> pkgList = new HashSet();
                        HashSet<String> hashSet;
                        try {
                            for (Entry<String, ProcessStateHolder> entry : app.pkgList.entrySet()) {
                                pkgList.add((String) entry.getKey());
                            }
                            if (DEBUG_HWTRIM_PERFORM) {
                                Log.v(TAG, "AMS.hwTrimApk");
                                long trimActionCost = System.currentTimeMillis() - timeStart;
                                Log.v(TAG, "AMS.hwTrimApk_HwSysM: trim action cost " + trimActionCost + " ms end.");
                            }
                            SMCSAMSHelper.getInstance().trimProcessPostProcess(trimProc, uid, trimType, pkgList);
                            hashSet = pkgList;
                        } catch (Throwable th2) {
                            th = th2;
                            hashSet = pkgList;
                            throw th;
                        }
                    }
                    if (DEBUG_HWTRIM_PERFORM) {
                        long costTime = System.currentTimeMillis() - timeStart;
                        Log.v(TAG, "AMS.hwTrimApk_HwSysM: cost " + costTime + " ms end.");
                    }
                } catch (Throwable th3) {
                    th = th3;
                    throw th;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "AMS.hwTrimApk_HwSysM: catch exception: " + e.toString());
        }
    }

    public final void hwTrimApk_HwSysM(ArrayList<String> procs, HashSet<String> pkgList) {
        Exception e;
        Throwable th;
        long timeStart = 0;
        try {
            if (DEBUG_HWTRIM_PERFORM) {
                Log.v(TAG, "AMS.hwTrimApk_HwSysM");
                timeStart = System.currentTimeMillis();
            }
            if (procs == null || procs.size() == 0) {
                if (DEBUG_HWTRIM) {
                    Log.e(TAG, "AMS.hwTrimApk_HwSysM: invalid trim processes.");
                }
            } else if (pkgList != null) {
                synchronized (this) {
                    try {
                        ArrayList<ProcessRecord> trimProcs = new ArrayList();
                        try {
                            ProcessRecord app;
                            for (SparseArray<ProcessRecord> apps : this.mProcessNames.getMap().values()) {
                                int NA = apps.size();
                                for (int ia = SMART_TRIM_ADJ_LIMIT; ia < NA; ia += ROG_CHANGE_EVENT_SWITCH) {
                                    app = (ProcessRecord) apps.valueAt(ia);
                                    if (app != null) {
                                        if (procs.contains(app.processName)) {
                                            trimProcs.add(app);
                                            procs.remove(app.processName);
                                            if (procs.size() == 0) {
                                                break;
                                            }
                                        } else {
                                            continue;
                                        }
                                    }
                                }
                                if (procs.size() == 0) {
                                    break;
                                }
                            }
                            Iterator<ProcessRecord> itTrim = trimProcs.iterator();
                            while (itTrim.hasNext()) {
                                app = (ProcessRecord) itTrim.next();
                                if (app != null) {
                                    if (DEBUG_HWTRIM) {
                                        Log.v(TAG, "AMS.hwTrimApk_HwSysM: go to trim " + app.processName);
                                    }
                                    removeProcessLocked(app, IS_SUPPORT_CLONE_APP, IS_SUPPORT_CLONE_APP, "smart trim");
                                    for (Entry<String, ProcessStateHolder> entry : app.pkgList.entrySet()) {
                                        pkgList.add((String) entry.getKey());
                                    }
                                }
                            }
                            try {
                                if (DEBUG_HWTRIM_PERFORM) {
                                    long costTime = System.currentTimeMillis() - timeStart;
                                    Log.v(TAG, "AMS.hwTrimApk_HwSysM: cost " + costTime + " ms end.");
                                }
                            } catch (Exception e2) {
                                e = e2;
                                Log.e(TAG, "AMS.hwTrimApk_HwSysM: catch exception: " + e.toString());
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            ArrayList<ProcessRecord> arrayList = trimProcs;
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        throw th;
                    }
                }
            }
        } catch (Exception e3) {
            e = e3;
            Log.e(TAG, "AMS.hwTrimApk_HwSysM: catch exception: " + e.toString());
        }
    }

    public final void hwTrimPkgs_HwSysM(ArrayList<String> pkgs) {
        long timeStart = 0;
        if (DEBUG_HWTRIM_PERFORM) {
            Log.v(TAG, "AMS.hwTrimPkgs_HwSysM");
            timeStart = System.currentTimeMillis();
        }
        if (pkgs != null && pkgs.size() != 0) {
            Iterator<String> it = pkgs.iterator();
            while (it.hasNext()) {
                String sPkg = (String) it.next();
                if (sPkg != null && sPkg.length() > 0) {
                    forceStopPackage(sPkg, UserHandle.myUserId());
                }
            }
            if (DEBUG_HWTRIM_PERFORM) {
                Log.v(TAG, "AMS.hwTrimPkgs_HwSysM: cost " + (System.currentTimeMillis() - timeStart) + " ms end.");
            }
        }
    }

    public long getHWMemFreeLimit_HwSysM() {
        return this.mProcessList.getMemLevel(Integer.MAX_VALUE);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void getRunningAppProcessRecord_HwSysM(ArrayList<STProcessRecord> runList) {
        Throwable th;
        Exception e;
        try {
            synchronized (this.mLruProcesses) {
                if (runList == null) {
                    return;
                }
                try {
                    STProcessRecord stpr;
                    int i = this.mLruProcesses.size() - 1;
                    STProcessRecord stpr2 = null;
                    while (i >= 0) {
                        try {
                            ProcessRecord app = (ProcessRecord) this.mLruProcesses.get(i);
                            if (app == null) {
                                stpr = stpr2;
                            } else if (app.thread == null || app.crashing || app.notResponding || app.curAdj < 0) {
                                stpr = stpr2;
                            } else {
                                stpr = new STProcessRecord(app.processName, app.uid, app.pid, app.curAdj, changeArrayMapPkgList2HashSet(app.pkgList));
                                runList.add(stpr);
                            }
                            i--;
                            stpr2 = stpr;
                        } catch (Throwable th2) {
                            th = th2;
                            stpr = stpr2;
                        }
                    }
                    try {
                        stpr = stpr2;
                    } catch (Exception e2) {
                        e = e2;
                        stpr = stpr2;
                        Log.e(TAG, "AMS.getRunningAppProcessRecord_HwSysM: catch exception: " + e.toString());
                    }
                } catch (Throwable th3) {
                    th = th3;
                }
            }
        } catch (Exception e3) {
            e = e3;
            Log.e(TAG, "AMS.getRunningAppProcessRecord_HwSysM: catch exception: " + e.toString());
        }
    }

    public void smartTrimAddProcessRelation_HwSysM(ContentProviderConnection conn) {
        if (mIsSMCSHWSYSMEnabled && conn != null && conn.client != null && conn.provider != null && conn.provider.proc != null) {
            SMCSAMSHelper.getInstance().smartTrimAddProcessRelation(conn.client.processName, conn.client.curAdj, conn.client.pkgList, conn.provider.proc.processName, conn.provider.proc.curAdj, conn.provider.proc.pkgList);
        }
    }

    public void smartTrimAddProcessRelation_HwSysM(AppBindRecord b, AppBindRecord c) {
        if (mIsSMCSHWSYSMEnabled && c != null && c.service != null && c.service.app != null && b != null && b.client != null) {
            SMCSAMSHelper.getInstance().smartTrimAddProcessRelation(b.client.processName, b.client.curAdj, b.client.pkgList, c.service.app.processName, c.service.app.curAdj, c.service.app.pkgList);
        }
    }

    private HashSet<String> changeArrayMapPkgList2HashSet(ArrayMap<String, ProcessStateHolder> pkgListA) {
        if (pkgListA == null || pkgListA.size() == 0) {
            return null;
        }
        HashSet<String> pkgListH = new HashSet();
        int size = pkgListA.size();
        for (int i = SMART_TRIM_ADJ_LIMIT; i < size; i += ROG_CHANGE_EVENT_SWITCH) {
            String pkgName = (String) pkgListA.keyAt(i);
            if (pkgName != null && pkgName.length() > 0) {
                pkgListH.add(pkgName);
            }
        }
        return pkgListH;
    }

    public void addCallerToIntent(Intent intent, IApplicationThread caller) {
        String callerPackage = null;
        if (caller != null) {
            ProcessRecord callerApp = getRecordForAppLocked(caller);
            if (callerApp != null) {
                callerPackage = callerApp.info.packageName;
            }
        }
        if (callerPackage != null) {
            String CALLER_PACKAGE = "caller_package";
            try {
                if (isInstall(intent)) {
                    String callerIndex = intent.getStringExtra("caller_package");
                    if (callerIndex != null) {
                        callerPackage = callerIndex;
                    }
                    intent.putExtra("caller_package", callerPackage);
                }
            } catch (Exception e) {
                Log.e(TAG, "Get package info faild:" + e);
            }
        }
    }

    private boolean isInstall(Intent intent) {
        String action = intent.getAction();
        String type = intent.getType();
        boolean story = IS_SUPPORT_CLONE_APP;
        if ("android.intent.action.INSTALL_PACKAGE".equals(action)) {
            story = true;
        }
        if ("application/vnd.android.package-archive".equals(type)) {
            return true;
        }
        return story;
    }

    private final boolean isOomAdjCustomized(ProcessRecord app) {
        if (sHardCodeAppToSetOomAdjArrays.containsKey(app.info.packageName) || sHardCodeAppToSetOomAdjArrays.containsKey(app.processName)) {
            return true;
        }
        return IS_SUPPORT_CLONE_APP;
    }

    private int retrieveCustedMaxAdj(String processName) {
        int rc = -901;
        if (sHardCodeAppToSetOomAdjArrays.containsKey(processName)) {
            rc = ((Integer) sHardCodeAppToSetOomAdjArrays.get(processName)).intValue();
        }
        Slog.i(TAG, "retrieveCustedMaxAdj for processName:" + processName + ", get adj:" + rc);
        return rc;
    }

    protected final int computeOomAdjLocked(ProcessRecord app, int cachedAdj, ProcessRecord TOP_APP, boolean doingAll, long now) {
        if (this.mAdjSeq != app.adjSeq) {
            return super.computeOomAdjLocked(app, cachedAdj, TOP_APP, doingAll, now);
        }
        int app_curRawAdj = super.computeOomAdjLocked(app, cachedAdj, TOP_APP, doingAll, now);
        if (app.curAdj > app.maxAdj && isOomAdjCustomized(app)) {
            app.curAdj = app.maxAdj;
        }
        return app_curRawAdj;
    }

    protected final void startProcessLocked(ProcessRecord app, String hostingType, String hostingNameStr, String abiOverride, String entryPoint, String[] entryPointArgs) {
        noteProcessStart(app.info.packageName, app.processName, app.pid, app.uid, true, hostingType, hostingNameStr);
        super.startProcessLocked(app, hostingType, hostingNameStr, abiOverride, entryPoint, entryPointArgs);
        if (isOomAdjCustomized(app)) {
            int custMaxAdj = retrieveCustedMaxAdj(app.processName);
            if (app.maxAdj > PERSISTENT_PROC_ADJ && custMaxAdj >= SYSTEM_ADJ && custMaxAdj <= 906) {
                app.maxAdj = custMaxAdj;
                Slog.i(TAG, "addAppLocked, app:" + app + ", set maxadj to " + custMaxAdj);
            }
        } else if (app.maxAdj > AwareDefaultConfigList.HW_PERCEPTIBLE_APP_ADJ && AwareAppMngSort.checkAppMngEnable() && isAppMngOomAdjCustomized(app.info.packageName)) {
            app.maxAdj = AwareDefaultConfigList.HW_PERCEPTIBLE_APP_ADJ;
        }
    }

    protected void startPushService() {
        File jarFile = new File("/system/framework/hwpush.jar");
        File custFile = HwCfgFilePolicy.getCfgFile("jars/hwpush.jar", SMART_TRIM_ADJ_LIMIT);
        if ((jarFile != null && jarFile.exists()) || (custFile != null && custFile.exists())) {
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    Intent serviceIntent = new Intent(HwActivityManagerService.this.mContext, PushService.class);
                    serviceIntent.putExtra("startFlag", PPPOEStateMachine.PHASE_INITIALIZE);
                    HwActivityManagerService.this.mContext.startService(serviceIntent);
                }
            }, AppHibernateCst.DELAY_ONE_MINS);
        }
    }

    public Configuration getCurNaviConfiguration() {
        return this.mWindowManager.getCurNaviConfiguration();
    }

    protected void setFocusedActivityLockedForNavi(ActivityRecord r) {
        if (this.mFocusedActivityForNavi != r) {
            this.mFocusedActivityForNavi = r;
            if (r != null) {
                this.mWindowManager.setFocusedAppForNavi(r.appToken);
            }
        }
    }

    public void showUninstallLauncher() {
        try {
            PackageInfo pInfo = this.mContext.getPackageManager().getPackageInfo(this.mLastLauncherName, SMART_TRIM_ADJ_LIMIT);
            if (pInfo != null) {
                AlertDialog d = new BaseErrorDialog(this.mContext);
                d.getWindow().setType(2010);
                d.setCancelable(IS_SUPPORT_CLONE_APP);
                d.setTitle(this.mContext.getString(17041115));
                String appName = this.mContext.getPackageManager().getApplicationLabel(pInfo.applicationInfo).toString();
                Context context = this.mContext;
                Object[] objArr = new Object[ROG_CHANGE_EVENT_SWITCH];
                objArr[SMART_TRIM_ADJ_LIMIT] = appName;
                d.setMessage(context.getString(17041116, objArr));
                d.setButton(-1, this.mContext.getString(17041117), new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        HwActivityManagerService.this.mContext.getPackageManager().deletePackage(HwActivityManagerService.this.mLastLauncherName, null, HwActivityManagerService.SMART_TRIM_ADJ_LIMIT);
                    }
                });
                d.setButton(-2, this.mContext.getString(17039360), new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                d.show();
            }
        } catch (NameNotFoundException e) {
        }
    }

    public void showUninstallLauncherDialog(String pkgName) {
        this.mLastLauncherName = pkgName;
        this.mHandler.sendMessage(this.mHandler.obtainMessage(SHOW_UNINSTALL_LAUNCHER_MSG));
    }

    public String topAppName() {
        ActivityStack focusedStack;
        synchronized (this) {
            focusedStack = getFocusedStack();
        }
        ActivityRecord r = focusedStack.topRunningActivityLocked();
        if (r != null) {
            return r.shortComponentName;
        }
        return null;
    }

    public void updateCpusetSwitch() {
        this.mCpusetSwitch = CPUFeature.isCpusetEnable();
    }

    public boolean setCurProcessGroup(ProcessRecord app, int schedGroup) {
        if (app != null) {
            app.curSchedGroup = schedGroup;
        }
        return true;
    }

    public void setWhiteListProcessGroup(ProcessRecord app, ProcessRecord TOP_APP, boolean bConnectTopApp) {
        if (!(app == null || app.curSchedGroup == 0 || app == TOP_APP || bConnectTopApp || ROG_CHANGE_EVENT_SWITCH != CPUResourceConfigControl.getInstance().isWhiteList(app.processName))) {
            app.curSchedGroup = SMART_TRIM_ADJ_LIMIT;
        }
    }

    public void notifyAppEventToIaware(int duration, String packageName) {
        if (packageName != null && !packageName.contains("com.antutu")) {
            CPUFeatureAMSCommunicator.getInstance().setTopAppToBoost(duration);
        }
    }

    public void reportActivityStartFinished() {
        HwSysResManager resManager = HwSysResManager.getInstance();
        if (resManager != null && resManager.isResourceNeeded(ResourceType.getReousrceId(ResourceType.RESOURCE_SCENE_REC))) {
            Bundle bundleArgs = new Bundle();
            bundleArgs.putInt("relationType", 16);
            bundleArgs.putBoolean("start_or_stop_app", IS_SUPPORT_CLONE_APP);
            CollectData data = new CollectData(ResourceType.getReousrceId(ResourceType.RESOURCE_SCENE_REC), System.currentTimeMillis(), bundleArgs);
            long id = Binder.clearCallingIdentity();
            resManager.reportData(data);
            Binder.restoreCallingIdentity(id);
        }
    }

    public boolean serviceIsRunning(ComponentName serviceCmpName, int curUser) {
        boolean z;
        synchronized (this) {
            Slog.d(TAG, "serviceIsRunning, for user " + curUser + ", serviceCmpName " + serviceCmpName);
            z = this.mServices.getServices(curUser).get(serviceCmpName) != null ? true : IS_SUPPORT_CLONE_APP;
        }
        return z;
    }

    void setDeviceProvisioned() {
        ContentResolver cr = this.mContext.getContentResolver();
        if ((Global.getInt(cr, "device_provisioned", SMART_TRIM_ADJ_LIMIT) == 0 || Secure.getInt(cr, "user_setup_complete", SMART_TRIM_ADJ_LIMIT) == 0) && ((PackageManagerService) ServiceManager.getService(ControlScope.PACKAGE_ELEMENT_KEY)).isSetupDisabled()) {
            Global.putInt(cr, "device_provisioned", ROG_CHANGE_EVENT_SWITCH);
            Secure.putInt(cr, "user_setup_complete", ROG_CHANGE_EVENT_SWITCH);
        }
    }

    public void systemReady(Runnable goingCallback) {
        if (!testIsSystemReady()) {
            setDeviceProvisioned();
        }
        super.systemReady(goingCallback);
        initTrustSpace();
        this.mContext.registerReceiver(new ScreenStatusReceiver(), new IntentFilter("android.intent.action.stk.check_screen_idle"), "com.huawei.permission.STK_CHECK_SCREEN_IDLE", null);
        this.mContext.registerReceiver(new TrimMemoryReceiver(), new IntentFilter(HW_TRIM_MEMORY_ACTION));
        if (IS_SUPPORT_CLONE_APP) {
            this.mSettingsObserver = new SettingsObserver(this.mHandler);
        }
        this.mThirdPartyCallbackList = new RemoteCallbackList();
    }

    public ArrayList<Integer> getIawareDumpData() {
        ArrayList<Integer> queueSizes = new ArrayList();
        BroadcastQueue[] broadcastQueueArr = this.mBroadcastQueues;
        int length = broadcastQueueArr.length;
        for (int i = SMART_TRIM_ADJ_LIMIT; i < length; i += ROG_CHANGE_EVENT_SWITCH) {
            ArrayList<Integer> queueSizesTemp = broadcastQueueArr[i].getIawareDumpData();
            if (queueSizesTemp != null) {
                queueSizes.addAll(queueSizesTemp);
            }
        }
        return queueSizes;
    }

    public void updateSRMSStatisticsData(int subTypeCode) {
        SRMSDumpRadar.getInstance().updateStatisticsData(subTypeCode);
    }

    public boolean getIawareResourceFeature(int type) {
        if (type >= ROG_CHANGE_EVENT_SWITCH && type <= ROG_CHANGE_EVENT_INFO) {
            return ResourceFeature.getIawareResourceFeature(type);
        }
        if (type < SMART_TRIM_POST_MSG_DELAY || type > 11) {
            return IS_SUPPORT_CLONE_APP;
        }
        return BroadcastFeature.isFeatureEnabled(type);
    }

    public long proxyBroadcast(List<String> pkgs, boolean proxy) {
        long delay;
        synchronized (this) {
            delay = 0;
            BroadcastQueue[] broadcastQueueArr = this.mBroadcastQueues;
            int length = broadcastQueueArr.length;
            for (int i = SMART_TRIM_ADJ_LIMIT; i < length; i += ROG_CHANGE_EVENT_SWITCH) {
                delay = Math.max(broadcastQueueArr[i].proxyBroadcast(pkgs, proxy), delay);
            }
        }
        return delay;
    }

    public long proxyBroadcastByPid(List<Integer> pids, boolean proxy) {
        long delay;
        synchronized (this) {
            delay = 0;
            BroadcastQueue[] broadcastQueueArr = this.mBroadcastQueues;
            int length = broadcastQueueArr.length;
            for (int i = SMART_TRIM_ADJ_LIMIT; i < length; i += ROG_CHANGE_EVENT_SWITCH) {
                delay = Math.max(broadcastQueueArr[i].proxyBroadcastByPid(pids, proxy), delay);
            }
        }
        return delay;
    }

    public void setProxyBCActions(List<String> actions) {
        synchronized (this) {
            BroadcastQueue[] broadcastQueueArr = this.mBroadcastQueues;
            int length = broadcastQueueArr.length;
            for (int i = SMART_TRIM_ADJ_LIMIT; i < length; i += ROG_CHANGE_EVENT_SWITCH) {
                broadcastQueueArr[i].setProxyBCActions(actions);
            }
        }
    }

    public void setActionExcludePkg(String action, String pkg) {
        synchronized (this) {
            BroadcastQueue[] broadcastQueueArr = this.mBroadcastQueues;
            int length = broadcastQueueArr.length;
            for (int i = SMART_TRIM_ADJ_LIMIT; i < length; i += ROG_CHANGE_EVENT_SWITCH) {
                broadcastQueueArr[i].setActionExcludePkg(action, pkg);
            }
        }
    }

    public void proxyBCConfig(int type, String key, List<String> value) {
        synchronized (this) {
            BroadcastQueue[] broadcastQueueArr = this.mBroadcastQueues;
            int length = broadcastQueueArr.length;
            for (int i = SMART_TRIM_ADJ_LIMIT; i < length; i += ROG_CHANGE_EVENT_SWITCH) {
                broadcastQueueArr[i].proxyBCConfig(type, key, value);
            }
        }
    }

    public void checkIfScreenStatusRequestAndSendBroadcast() {
        for (int slotId = SMART_TRIM_ADJ_LIMIT; slotId < this.mScreenStatusRequest.length; slotId += ROG_CHANGE_EVENT_SWITCH) {
            if (this.mScreenStatusRequest[slotId]) {
                Intent StkIntent = new Intent("android.intent.action.stk.idle_screen");
                StkIntent.putExtra("SCREEN_IDLE", true);
                StkIntent.putExtra("slot_id", slotId);
                this.mContext.sendBroadcast(StkIntent, "com.huawei.permission.CAT_IDLE_SCREEN");
            }
        }
    }

    public void startupFilterReceiverList(Intent intent, List<ResolveInfo> receivers) {
        HwPFWService pfwService = HwPFWService.self();
        if (pfwService != null) {
            pfwService.startupFilterReceiverList(intent, receivers);
        }
    }

    public boolean shouldPreventStartService(ServiceInfo servInfo, int callerPid, int callerUid) {
        HwPFWService pfwService = HwPFWService.self();
        if (pfwService != null) {
            return pfwService.shouldPreventStartService(servInfo, callerPid, callerUid);
        }
        return IS_SUPPORT_CLONE_APP;
    }

    public boolean shouldPreventActivity(Intent intent, ActivityInfo aInfo, ActivityRecord record) {
        if (intent == null || aInfo == null || record == null) {
            return IS_SUPPORT_CLONE_APP;
        }
        if ((isSleepingLocked() && record.shortComponentName != null && "com.ss.android.article.news/com.ss.android.message.sswo.SswoActivity".equals(record.shortComponentName)) || interceptGoogleAppIfNeed(aInfo.packageName, record.userId)) {
            return true;
        }
        HwPFWService pfwService = HwPFWService.self();
        if (pfwService == null) {
            return IS_SUPPORT_CLONE_APP;
        }
        return pfwService.shouldPreventStartActivity(intent, aInfo, record);
    }

    public boolean shouldPreventRestartService(String pkgName) {
        HwPFWService pfwService = HwPFWService.self();
        if (pfwService != null) {
            return pfwService.shouldPreventRestartService(pkgName);
        }
        return IS_SUPPORT_CLONE_APP;
    }

    private void initTrustSpace() {
        this.mTrustSpaceManagerInternal = (TrustSpaceManagerInternal) LocalServices.getService(TrustSpaceManagerInternal.class);
        if (this.mTrustSpaceManagerInternal == null) {
            Slog.e(TAG, "TrustSpaceManagerInternal not find !");
        } else {
            this.mTrustSpaceManagerInternal.initTrustSpace();
        }
    }

    private boolean shouldPreventStartComponent(int type, String calleePackage, int callerUid, int callerPid, String callerPackage, int userId) {
        boolean shouldPrevent = IS_SUPPORT_CLONE_APP;
        if (this.mSystemReady && this.mTrustSpaceManagerInternal != null) {
            long ident = Binder.clearCallingIdentity();
            try {
                shouldPrevent = this.mTrustSpaceManagerInternal.checkIntent(type, calleePackage, callerUid, callerPid, callerPackage, userId);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
        return shouldPrevent;
    }

    public boolean shouldPreventStartService(ServiceInfo sInfo, int callerUid, int callerPid, String callerPackage, int userId) {
        if (sInfo == null) {
            return IS_SUPPORT_CLONE_APP;
        }
        return shouldPreventStartComponent(ROG_CHANGE_EVENT_INFO, sInfo.applicationInfo.packageName, callerUid, callerPid, callerPackage, userId);
    }

    public boolean shouldPreventStartActivity(ActivityInfo aInfo, int callerUid, int callerPid, String callerPackage, int userId) {
        if (aInfo == null) {
            return IS_SUPPORT_CLONE_APP;
        }
        return shouldPreventStartComponent(SMART_TRIM_ADJ_LIMIT, aInfo.applicationInfo.packageName, callerUid, callerPid, callerPackage, userId);
    }

    public boolean shouldPreventStartProvider(ProviderInfo cpi, int callerUid, int callerPid, String callerPackage, int userId) {
        if (cpi == null) {
            return IS_SUPPORT_CLONE_APP;
        }
        return shouldPreventStartComponent(3, cpi.packageName, callerUid, callerPid, callerPackage, userId);
    }

    public boolean shouldPreventSendBroadcast(Intent intent, String receiver, int callerUid, int callerPid, String callingPackage, int userId) {
        return shouldPreventStartComponent(ROG_CHANGE_EVENT_SWITCH, receiver, callerUid, callerPid, callingPackage, userId);
    }

    protected void exitSingleHandMode() {
        this.mHandler.removeCallbacks(this.mOverscanTimeout);
        this.mHandler.postDelayed(this.mOverscanTimeout, 200);
    }

    public boolean shouldPreventStartProvider(ProviderInfo cpi, int callerPid, int callerUid) {
        HwPFWService pfwService = HwPFWService.self();
        if (pfwService != null) {
            return pfwService.shouldPreventStartProvider(cpi, callerPid, callerUid);
        }
        return IS_SUPPORT_CLONE_APP;
    }

    protected void setCustomActivityController(IActivityController controller) {
        enforceCallingPermission("android.permission.SET_ACTIVITY_WATCHER", "setCustomActivityController()");
        synchronized (this) {
            this.mCustomController = controller;
        }
    }

    protected boolean customActivityStarting(Intent intent, String packageName) {
        if (this.mCustomController != null) {
            boolean startOK = true;
            try {
                startOK = this.mCustomController.activityStarting(intent.cloneFilter(), packageName);
            } catch (RemoteException e) {
                this.mCustomController = null;
            }
            if (!startOK) {
                Slog.i(TAG, "Not starting activity because custom controller stop it");
                return true;
            }
        }
        return IS_SUPPORT_CLONE_APP;
    }

    protected boolean customActivityResuming(String packageName) {
        if (this.mCustomController != null) {
            boolean resumeOK = true;
            try {
                resumeOK = this.mCustomController.activityResuming(packageName);
            } catch (RemoteException e) {
                this.mCustomController = null;
            }
            if (!resumeOK) {
                Slog.i(TAG, "Not resuming activity because custom controller stop it");
                return true;
            }
        }
        return IS_SUPPORT_CLONE_APP;
    }

    protected BroadcastQueue[] initialBroadcastQueue() {
        int queueNum;
        if (enableIaware) {
            queueNum = QUEUE_NUM_IAWARE;
        } else if (enableRms) {
            queueNum = QUEUE_NUM_RMS;
        } else {
            queueNum = ROG_CHANGE_EVENT_INFO;
        }
        return new BroadcastQueue[queueNum];
    }

    protected void setThirdPartyAppBroadcastQueue(BroadcastQueue[] broadcastQueues) {
        if (enableRms || enableIaware) {
            ServiceThread thirdAppHandlerThread = new ServiceThread("ThirdAppHandlerThread", SMART_TRIM_POST_MSG_DELAY, IS_SUPPORT_CLONE_APP);
            thirdAppHandlerThread.start();
            Handler thirdAppHandler = new Handler(thirdAppHandlerThread.getLooper());
            this.mFgThirdAppBroadcastQueue = new HwBroadcastQueue(this, thirdAppHandler, "fgthirdapp", (long) BROADCAST_FG_TIMEOUT, IS_SUPPORT_CLONE_APP);
            this.mBgThirdAppBroadcastQueue = new HwBroadcastQueue(this, thirdAppHandler, "bgthirdapp", AppHibernateCst.DELAY_ONE_MINS, IS_SUPPORT_CLONE_APP);
            broadcastQueues[ROG_CHANGE_EVENT_INFO] = this.mFgThirdAppBroadcastQueue;
            broadcastQueues[3] = this.mBgThirdAppBroadcastQueue;
        }
    }

    protected void setKeyAppBroadcastQueue(BroadcastQueue[] broadcastQueues) {
        if (enableIaware) {
            ServiceThread keyAppHandlerThread = new ServiceThread("keyAppHanderThread", SMART_TRIM_ADJ_LIMIT, IS_SUPPORT_CLONE_APP);
            keyAppHandlerThread.start();
            Handler keyAppHandler = new Handler(keyAppHandlerThread.getLooper());
            this.mFgKeyAppBroadcastQueue = new HwBroadcastQueue(this, keyAppHandler, "fgkeyapp", (long) BROADCAST_FG_TIMEOUT, IS_SUPPORT_CLONE_APP);
            this.mBgKeyAppBroadcastQueue = new HwBroadcastQueue(this, keyAppHandler, "bgkeyapp", AppHibernateCst.DELAY_ONE_MINS, IS_SUPPORT_CLONE_APP);
            broadcastQueues[QUEUE_NUM_RMS] = this.mFgKeyAppBroadcastQueue;
            broadcastQueues[5] = this.mBgKeyAppBroadcastQueue;
        }
    }

    protected boolean isThirdPartyAppBroadcastQueue(ProcessRecord callerApp) {
        boolean z = true;
        if ((!enableRms && !getIawareResourceFeature(ROG_CHANGE_EVENT_SWITCH)) || callerApp == null) {
            return IS_SUPPORT_CLONE_APP;
        }
        if (DEBUG_HWTRIM || Log.HWINFO) {
            Log.i(TAG, "Split enqueueing broadcast [callerApp]:" + callerApp);
        }
        if (callerApp.instrumentationClass != null) {
            return IS_SUPPORT_CLONE_APP;
        }
        if ((callerApp.info.flags & ROG_CHANGE_EVENT_SWITCH) != 0 && (callerApp.info.hwFlags & HwGlobalActionsData.FLAG_SHUTDOWN_CONFIRM) == 0) {
            z = IS_SUPPORT_CLONE_APP;
        }
        return z;
    }

    protected boolean isKeyAppBroadcastQueue(int type, String name) {
        return (getIawareResourceFeature(ROG_CHANGE_EVENT_SWITCH) && name != null && isKeyApp(type, SMART_TRIM_ADJ_LIMIT, name)) ? true : IS_SUPPORT_CLONE_APP;
    }

    protected boolean isThirdPartyAppPendingBroadcastProcessLocked(int pid) {
        boolean z = true;
        if (!enableRms && !getIawareResourceFeature(ROG_CHANGE_EVENT_SWITCH)) {
            return IS_SUPPORT_CLONE_APP;
        }
        if (!this.mFgThirdAppBroadcastQueue.isPendingBroadcastProcessLocked(pid)) {
            z = this.mBgThirdAppBroadcastQueue.isPendingBroadcastProcessLocked(pid);
        }
        return z;
    }

    protected boolean isKeyAppPendingBroadcastProcessLocked(int pid) {
        boolean z = true;
        if (!getIawareResourceFeature(ROG_CHANGE_EVENT_SWITCH) || this.mFgKeyAppBroadcastQueue == null || this.mBgKeyAppBroadcastQueue == null) {
            return IS_SUPPORT_CLONE_APP;
        }
        if (!this.mFgKeyAppBroadcastQueue.isPendingBroadcastProcessLocked(pid)) {
            z = this.mBgKeyAppBroadcastQueue.isPendingBroadcastProcessLocked(pid);
        }
        return z;
    }

    protected boolean isThirdPartyAppFGBroadcastQueue(BroadcastQueue queue) {
        return queue == this.mFgThirdAppBroadcastQueue ? true : IS_SUPPORT_CLONE_APP;
    }

    protected boolean isKeyAppFGBroadcastQueue(BroadcastQueue queue) {
        return queue == this.mFgKeyAppBroadcastQueue ? true : IS_SUPPORT_CLONE_APP;
    }

    protected BroadcastQueue thirdPartyAppBroadcastQueueForIntent(Intent intent) {
        boolean isFg = (intent.getFlags() & 268435456) != 0 ? true : IS_SUPPORT_CLONE_APP;
        if (DEBUG_HWTRIM || Log.HWINFO) {
            Log.i(TAG, "thirdAppBroadcastQueueForIntent intent " + intent + " on " + (isFg ? "fgthirdapp" : "bgthirdapp") + " queue");
        }
        if (isFg) {
            return this.mFgThirdAppBroadcastQueue;
        }
        return this.mBgThirdAppBroadcastQueue;
    }

    protected BroadcastQueue keyAppBroadcastQueueForIntent(Intent intent) {
        boolean isFg = (intent.getFlags() & 268435456) != 0 ? true : IS_SUPPORT_CLONE_APP;
        if (DEBUG_HWTRIM || Log.HWINFO) {
            Log.i(TAG, "keyAppBroadcastQueueForIntent intent " + intent + " on " + (isFg ? "fgkeyapp" : "bgkeyapp") + " queue");
        }
        if (isFg) {
            updateSRMSStatisticsData(SMART_TRIM_ADJ_LIMIT);
        } else {
            updateSRMSStatisticsData(ROG_CHANGE_EVENT_SWITCH);
        }
        if (isFg) {
            return this.mFgKeyAppBroadcastQueue;
        }
        return this.mBgKeyAppBroadcastQueue;
    }

    protected void initBroadcastResourceLocked() {
        if (this.mBroadcastResource == null) {
            if (DEBUG_HWTRIM || Log.HWINFO) {
                Log.d(TAG, "init BroadcastResource");
            }
            this.mBroadcastResource = HwFrameworkFactory.getHwResource(11);
        }
    }

    public void checkOrderedBroadcastTimeoutLocked(String actionOrPkg, int timeCost, boolean isInToOut) {
        if (getIawareResourceFeature(ROG_CHANGE_EVENT_INFO)) {
            if (this.mOrderedBroadcastResource == null) {
                if (DEBUG_HWTRIM || Log.HWINFO) {
                    Log.d(TAG, "init OrderedBroadcastResource");
                }
                this.mOrderedBroadcastResource = HwFrameworkFactory.getHwResource(37);
            }
            if (!(this.mOrderedBroadcastResource == null || isInToOut)) {
                this.mOrderedBroadcastResource.acquire(SMART_TRIM_ADJ_LIMIT, actionOrPkg, SMART_TRIM_ADJ_LIMIT);
            }
        }
    }

    protected void checkBroadcastRecordSpeed(int callingUid, String callerPackage, ProcessRecord callerApp) {
        if (this.mBroadcastResource != null && callerApp != null) {
            int uid = callingUid;
            String pkg = callerPackage;
            int processType = getProcessType(callerApp);
            if ((PPPOEStateMachine.PHASE_INITIALIZE.equals(SystemProperties.get(SYSTEM_DEBUGGABLE, PPPOEStateMachine.PHASE_DEAD)) || processType == 0) && ROG_CHANGE_EVENT_INFO == this.mBroadcastResource.acquire(callingUid, callerPackage, processType) && ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                Log.i(TAG, "This App send broadcast speed is overload! uid = " + callingUid);
            }
        }
    }

    protected void clearBroadcastResource(ProcessRecord app) {
        if (this.mBroadcastResource != null && app != null) {
            int uid = app.info.uid;
            String pkg = app.info.packageName;
            int processType = getProcessType(app);
            if (PPPOEStateMachine.PHASE_INITIALIZE.equals(SystemProperties.get(SYSTEM_DEBUGGABLE, PPPOEStateMachine.PHASE_DEAD)) || processType == 0) {
                this.mBroadcastResource.clear(uid, pkg, processType);
            }
        }
    }

    private int getProcessType(ProcessRecord app) {
        if ((app.info.flags & ROG_CHANGE_EVENT_SWITCH) != 0) {
            return ROG_CHANGE_EVENT_INFO;
        }
        return SMART_TRIM_ADJ_LIMIT;
    }

    public boolean isKeyApp(int type, int value, String key) {
        if (this.mBroadcastResource == null || key == null || ROG_CHANGE_EVENT_SWITCH != this.mBroadcastResource.queryPkgPolicy(type, value, key)) {
            return IS_SUPPORT_CLONE_APP;
        }
        if (Log.HWLog) {
            Log.i(TAG, "isKeyApp in whiteList key:" + key + " , type is " + type);
        }
        return true;
    }

    public AbsUserBehaviourRecord getRecordCust() {
        if (this.mCust == null) {
            this.mCust = new HwUserBehaviourRecord(this.mContext);
        }
        return this.mCust;
    }

    private void initAppResourceLocked() {
        if (this.mAppResource == null) {
            Log.i(TAG, "init Appresource");
            this.mAppResource = HwFrameworkFactory.getHwResource(19);
        }
    }

    private void initAppServiceResourceLocked() {
        if (this.mAppServiceResource == null) {
            Log.i(TAG, "init AppServiceResource");
            this.mAppServiceResource = HwFrameworkFactory.getHwResource(18);
        }
    }

    public void initAppAndAppServiceResourceLocked() {
        initAppResourceLocked();
        initAppServiceResourceLocked();
    }

    public boolean isAcquireAppServiceResourceLocked(ServiceRecord sr, ProcessRecord app) {
        if (this.mAppServiceResource == null || sr == null || sr.appInfo.uid <= 0 || sr.appInfo.packageName == null || sr.serviceInfo.name == null || ROG_CHANGE_EVENT_INFO != this.mAppServiceResource.acquire(sr.appInfo.uid, sr.appInfo.packageName, getProcessType(app))) {
            return true;
        }
        Log.i(TAG, "Failed to acquire AppServiceResource:" + sr.serviceInfo.name + " of " + sr.appInfo.packageName + "/" + sr.appInfo.uid);
        return IS_SUPPORT_CLONE_APP;
    }

    public boolean isAcquireAppResourceLocked(ProcessRecord app) {
        if (!(this.mAppResource == null || app == null || app.uid <= 0 || app.info == null || app.info.packageName == null || app.startTime <= 0)) {
            int processType = ((app.info.flags & ROG_CHANGE_EVENT_SWITCH) != 0 && (app.info.hwFlags & HwGlobalActionsData.FLAG_SHUTDOWN_CONFIRM) == 0 && (app.info.hwFlags & 67108864) == 0) ? ROG_CHANGE_EVENT_INFO : SMART_TRIM_ADJ_LIMIT;
            Bundle args = new Bundle();
            args.putInt("callingUid", app.uid);
            args.putString(HwGpsPowerTracker.DEL_PKG, app.info.packageName);
            args.putLong("startTime", app.startTime);
            args.putInt("processType", processType);
            args.putBoolean("launchfromActivity", app.launchfromActivity);
            if (ROG_CHANGE_EVENT_INFO == this.mAppResource.acquire(null, null, args)) {
                Log.i(TAG, "Failed to acquire AppResource:" + app.info.packageName + "/" + app.uid);
                return IS_SUPPORT_CLONE_APP;
            }
        }
        return true;
    }

    private void clearAppServiceResource(ProcessRecord app) {
        if (this.mAppServiceResource != null && app != null) {
            this.mAppServiceResource.clear(app.uid, app.info.packageName, getProcessType(app));
            Log.i(TAG, "clear AppServiceResource of " + app.info.packageName + "/" + app.uid);
        }
    }

    private void clearAppResource(ProcessRecord app) {
        if (this.mAppResource != null && app != null && app.uid > 0 && app.info != null && app.info.packageName != null) {
            int processType = ((app.info.flags & ROG_CHANGE_EVENT_SWITCH) != 0 && (app.info.hwFlags & HwGlobalActionsData.FLAG_SHUTDOWN_CONFIRM) == 0 && (app.info.hwFlags & 67108864) == 0) ? ROG_CHANGE_EVENT_INFO : SMART_TRIM_ADJ_LIMIT;
            this.mAppResource.clear(app.uid, app.info.packageName, processType);
            Log.i(TAG, "clear Appresource of " + app.info.packageName + "/" + app.uid);
        }
    }

    public void clearAppAndAppServiceResource(ProcessRecord app) {
        clearAppServiceResource(app);
        clearAppResource(app);
    }

    private static IBinder getAudioService() {
        if (mAudioService != null) {
            return mAudioService;
        }
        mAudioService = ServiceManager.getService("audio");
        return mAudioService;
    }

    protected int setSoundEffectState(boolean restore, String packageName, boolean isOnTop, String reserved) {
        int i = ROG_CHANGE_EVENT_SWITCH;
        IBinder b = getAudioService();
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        int _result = SMART_TRIM_ADJ_LIMIT;
        try {
            _data.writeInterfaceToken("android.media.IAudioService");
            _data.writeInt(restore ? ROG_CHANGE_EVENT_SWITCH : SMART_TRIM_ADJ_LIMIT);
            _data.writeString(packageName);
            if (!isOnTop) {
                i = SMART_TRIM_ADJ_LIMIT;
            }
            _data.writeInt(i);
            _data.writeString(reserved);
            b.transact(EventTracker.TRACK_TYPE_KILL, _data, _reply, SMART_TRIM_ADJ_LIMIT);
            _reply.readException();
            _result = _reply.readInt();
        } catch (RemoteException e) {
            Slog.e(TAG, "setHeadsetRevertSequenceState transact e: " + e);
        } finally {
            _reply.recycle();
            _data.recycle();
        }
        return _result;
    }

    protected void registerCtrlSocketForMm(String processname, int pid) {
        if ((HwConnectivityService.MM_PUSH_NAME.equals(processname) || "com.tencent.mobileqq:MSF".equals(processname) || "com.huawei.parentcontrol.parent".equals(processname) || "com.huawei.parentcontrol".equals(processname) || "com.huawei.hidisk".equals(processname)) && PPPOEStateMachine.PHASE_INITIALIZE.equals(SystemProperties.get("ro.config.mm_socket_ctrl", PPPOEStateMachine.PHASE_DEAD))) {
            try {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                data.writeString(processname);
                data.writeInt(pid);
                IBinder hwcs = ServiceManager.getService("connectivity");
                Log.d(TAG, "registerCtrlSocketForMm end ");
                hwcs.transact(HwPackageManagerService.TRANSACTION_CODE_DELTE_GMS_FROM_UNINSTALLED_DELAPP, data, reply, SMART_TRIM_ADJ_LIMIT);
            } catch (Exception e) {
                Flog.e(VISIBLE_APP_ADJ, "registerCtrlSocketForMm exception: " + e.toString());
            }
        }
    }

    protected void unregisterCtrlSocketForMm(String processname) {
        if ((HwConnectivityService.MM_PUSH_NAME.equals(processname) || "com.tencent.mobileqq:MSF".equals(processname) || "com.huawei.parentcontrol.parent".equals(processname) || "com.huawei.parentcontrol".equals(processname) || "com.huawei.hidisk".equals(processname)) && PPPOEStateMachine.PHASE_INITIALIZE.equals(SystemProperties.get("ro.config.mm_socket_ctrl", PPPOEStateMachine.PHASE_DEAD))) {
            try {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                data.writeString(processname);
                IBinder hwcs = ServiceManager.getService("connectivity");
                Log.d(TAG, "unregisterCtrlSocketForMm end ");
                hwcs.transact(HwPackageManagerService.TRANSACTION_CODE_CHECK_GMS_IS_UNINSTALLED, data, reply, SMART_TRIM_ADJ_LIMIT);
            } catch (Exception e) {
                Flog.e(VISIBLE_APP_ADJ, "unregisterCtrlSocketForMm exception: " + e.toString());
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void trimGLMemory(int level) {
        Slog.i(TAG, " trimGLMemory begin ");
        synchronized (this.mLruProcesses) {
            int i = SMART_TRIM_ADJ_LIMIT;
            while (true) {
                if (i < this.mLruProcesses.size()) {
                    ProcessRecord app = (ProcessRecord) this.mLruProcesses.get(i);
                    if (app.thread != null) {
                        try {
                            app.thread.scheduleTrimMemory(level);
                        } catch (RemoteException e) {
                        }
                    }
                    i += ROG_CHANGE_EVENT_SWITCH;
                }
            }
        }
        Slog.i(TAG, " trimGLMemory end ");
    }

    public void setWindowManager(WindowManagerService wm) {
        super.setWindowManager(wm);
        wm.registerPointerEventListener(new IawarePointerEventListener());
    }

    protected void noteActivityStart(String packageName, String processName, int pid, int uid, boolean started) {
        HwSysResManager resManager = HwSysResManager.getInstance();
        if (resManager != null && resManager.isResourceNeeded(ResourceType.getReousrceId(ResourceType.RES_APP)) && this.mSystemReady) {
            int event;
            Apps.Builder builder = Apps.builder();
            if (started) {
                event = 15005;
            } else {
                event = 85005;
            }
            builder.addEvent(event);
            builder.addCalledApp(packageName, processName, pid, uid);
            CollectData appsData = builder.build();
            long id = Binder.clearCallingIdentity();
            resManager.reportData(appsData);
            Binder.restoreCallingIdentity(id);
        }
    }

    protected void noteProcessStart(String packageName, String processName, int pid, int uid, boolean started, String launcherMode, String reason) {
        HwSysResManager resManager = HwSysResManager.getInstance();
        if (resManager != null) {
            resManager.noteProcessStart(packageName, processName, pid, uid, started, launcherMode, reason);
            if (resManager.isResourceNeeded(ResourceType.getReousrceId(ResourceType.RES_APP)) && this.mSystemReady) {
                int event;
                Apps.Builder builder = Apps.builder();
                if (started) {
                    event = 15001;
                } else {
                    event = 85001;
                }
                builder.addEvent(event);
                builder.addLaunchCalledApp(packageName, processName, launcherMode, reason, pid, uid);
                CollectData appsData = builder.build();
                long id = Binder.clearCallingIdentity();
                resManager.reportData(appsData);
                Binder.restoreCallingIdentity(id);
            }
        }
    }

    public boolean getProcessRecordFromMTM(ProcessInfo procInfo) {
        if (procInfo == null) {
            Slog.e(TAG, "getProcessRecordFromMTM procInfo is null");
            return IS_SUPPORT_CLONE_APP;
        }
        synchronized (this) {
            synchronized (this.mPidsSelfLocked) {
                if (procInfo.mPid == MY_PID || procInfo.mPid < 0) {
                    Slog.e(TAG, "getProcessRecordFromMTM it is failed to get process record ,mPid :" + procInfo.mPid);
                    return IS_SUPPORT_CLONE_APP;
                }
                ProcessRecord proc = (ProcessRecord) this.mPidsSelfLocked.get(procInfo.mPid);
                if (proc == null) {
                    Slog.e(TAG, "getProcessRecordFromMTM process info is null ,mUid :" + procInfo.mPid);
                    return IS_SUPPORT_CLONE_APP;
                }
                boolean z;
                if (procInfo.mType == 0) {
                    procInfo.mType = getAppType(procInfo.mPid, proc.info);
                }
                procInfo.mProcessName = proc.processName;
                procInfo.mCurSchedGroup = proc.curSchedGroup;
                procInfo.mCurAdj = proc.curAdj;
                procInfo.mAdjType = proc.adjType;
                procInfo.mForegroundActivities = proc.foregroundActivities;
                procInfo.mForegroundServices = proc.foregroundServices;
                if (proc.forcingToForeground != null) {
                    z = true;
                } else {
                    z = IS_SUPPORT_CLONE_APP;
                }
                procInfo.mForceToForeground = z;
                if (procInfo.mPackageName.size() == 0) {
                    int list_size = proc.pkgList.size();
                    for (int i = SMART_TRIM_ADJ_LIMIT; i < list_size; i += ROG_CHANGE_EVENT_SWITCH) {
                        String packagename = (String) proc.pkgList.keyAt(i);
                        if (!procInfo.mPackageName.contains(packagename)) {
                            procInfo.mPackageName.add(packagename);
                        }
                    }
                }
                procInfo.mLru = this.mLruProcesses.lastIndexOf(proc);
                return true;
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Map<Integer, AwareProcessBaseInfo> getAllProcessBaseInfo() {
        ArrayMap<Integer, AwareProcessBaseInfo> list = new ArrayMap();
        synchronized (this.mPidsSelfLocked) {
            int i = SMART_TRIM_ADJ_LIMIT;
            while (true) {
                if (i < this.mPidsSelfLocked.size()) {
                    ProcessRecord p = (ProcessRecord) this.mPidsSelfLocked.valueAt(i);
                    AwareProcessBaseInfo baseInfo = new AwareProcessBaseInfo();
                    baseInfo.mCurAdj = p.curAdj;
                    baseInfo.mForegroundActivities = p.foregroundActivities;
                    baseInfo.mAdjType = p.adjType;
                    baseInfo.mHasShownUi = p.hasShownUi;
                    baseInfo.mUid = p.uid;
                    list.put(Integer.valueOf(p.pid), baseInfo);
                    i += ROG_CHANGE_EVENT_SWITCH;
                }
            }
        }
        return list;
    }

    public AwareProcessBaseInfo getProcessBaseInfo(int pid) {
        Throwable th;
        synchronized (this.mPidsSelfLocked) {
            try {
                AwareProcessBaseInfo baseInfo = new AwareProcessBaseInfo();
                try {
                    baseInfo.mCurAdj = IOTController.TYPE_SLAVE;
                    ProcessRecord p = (ProcessRecord) this.mPidsSelfLocked.get(pid);
                    if (p != null) {
                        baseInfo.mForegroundActivities = p.foregroundActivities;
                        baseInfo.mUid = p.uid;
                        baseInfo.mCurAdj = p.curAdj;
                        baseInfo.mAdjType = p.adjType;
                    }
                    return baseInfo;
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    public boolean killProcessRecordFromMTM(ProcessInfo procInfo, boolean restartservice) {
        if (procInfo.mPid == MY_PID || procInfo.mPid < 0) {
            Slog.e(TAG, "killProcessRecordFromMTM it is failed to get process record ,mUid :" + procInfo.mUid);
            return IS_SUPPORT_CLONE_APP;
        }
        synchronized (this.mPidsSelfLocked) {
            int adj = procInfo.mCurAdj;
            ProcessRecord proc = (ProcessRecord) this.mPidsSelfLocked.get(procInfo.mPid);
            if (proc == null) {
                Slog.e(TAG, "killProcessRecordFromMTM this process has been killed or died before  :" + procInfo.mProcessName);
                return IS_SUPPORT_CLONE_APP;
            }
            synchronized (this) {
                removeProcessLocked(proc, IS_SUPPORT_CLONE_APP, restartservice, "MTM(adj:" + adj + "," + proc.curAdj + ")");
            }
            return true;
        }
    }

    private int getAppType(int pid, ApplicationInfo info) {
        if (info == null) {
            Slog.e(TAG, "getAppType app info is null");
            return SMART_TRIM_ADJ_LIMIT;
        }
        int flags = info.flags;
        try {
            int hwFlags = ((Integer) Class.forName("android.content.pm.ApplicationInfo").getField("hwFlags").get(info)).intValue();
            if (!((flags & ROG_CHANGE_EVENT_SWITCH) == 0 || (HwGlobalActionsData.FLAG_SHUTDOWN_CONFIRM & hwFlags) == 0)) {
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
        } catch (Exception e5) {
            Slog.e(TAG, "getAppType exception: Exception");
        }
        if (pid == Process.myPid()) {
            return ROG_CHANGE_EVENT_SWITCH;
        }
        if ((flags & ROG_CHANGE_EVENT_SWITCH) != 0 && (HwGlobalActionsData.FLAG_SHUTDOWN_CONFIRM & flags) != 0) {
            return 3;
        }
        if ((flags & ROG_CHANGE_EVENT_SWITCH) != 0) {
            return ROG_CHANGE_EVENT_INFO;
        }
        return QUEUE_NUM_RMS;
    }

    public ArrayList getAMSLru() {
        return this.mLruProcesses;
    }

    public int getAMSLruBypid(int pid) {
        synchronized (this) {
            int size = this.mLruProcesses.size();
            for (int i = SMART_TRIM_ADJ_LIMIT; i < size; i += ROG_CHANGE_EVENT_SWITCH) {
                if (((ProcessRecord) this.mLruProcesses.get(i)).pid == pid) {
                    return i;
                }
            }
            return -1;
        }
    }

    public void printLRU(PrintWriter pw) {
        pw.println("  LRU :");
        synchronized (this) {
            int size = this.mLruProcesses.size();
            for (int i = SMART_TRIM_ADJ_LIMIT; i < size; i += ROG_CHANGE_EVENT_SWITCH) {
                pw.println("  process " + i + ":" + ((ProcessRecord) this.mLruProcesses.get(i)).processName);
            }
        }
    }

    protected void notifyProcessGroupChange(int pid, int uid) {
        MultiTaskManager handler = MultiTaskManager.getInstance();
        if (handler != null) {
            handler.notifyProcessGroupChange(pid, uid);
        }
    }

    protected void notifyProcessGroupChange(int pid, int uid, int grp) {
        CPUKeyBackground.getInstance().notifyProcessGroupChange(pid, uid, grp);
    }

    public boolean hasDeps(ProcessInfo procInfo, String packageName) {
        if (packageName == null || procInfo == null) {
            Slog.e(TAG, "hasDeps packageName == null || procInfo == null");
            return IS_SUPPORT_CLONE_APP;
        }
        synchronized (this.mPidsSelfLocked) {
            if (procInfo.mPid == MY_PID || procInfo.mPid < 0) {
                Slog.e(TAG, "getProcessRecordFromMTM it is failed to get process record ,mPid :" + procInfo.mPid);
                return IS_SUPPORT_CLONE_APP;
            }
            ProcessRecord proc = (ProcessRecord) this.mPidsSelfLocked.get(procInfo.mPid);
            if (proc == null) {
                Slog.e(TAG, "hasDeps proc == null");
                return IS_SUPPORT_CLONE_APP;
            }
            boolean contains = proc.pkgDeps != null ? proc.pkgDeps.contains(packageName) : IS_SUPPORT_CLONE_APP;
            return contains;
        }
    }

    public boolean switchUser(int userId) {
        cancelInterceptAppDialog();
        boolean isStorageLow = IS_SUPPORT_CLONE_APP;
        try {
            isStorageLow = AppGlobals.getPackageManager().isStorageLow();
        } catch (RemoteException e) {
            Slog.e(TAG, "check low storage error because e: " + e);
        }
        if (isStorageLow) {
            UiThread.getHandler().post(new Runnable() {
                public void run() {
                    Toast toast = Toast.makeText(HwActivityManagerService.this.mContext, HwActivityManagerService.this.mContext.getResources().getString(17040234), HwActivityManagerService.ROG_CHANGE_EVENT_SWITCH);
                    toast.getWindowParams().type = 2006;
                    toast.show();
                }
            });
            return IS_SUPPORT_CLONE_APP;
        }
        UserInfo targetUser = this.mUserController.getUserInfo(userId);
        if (!targetUser.isGuest()) {
            return super.switchUser(userId);
        }
        this.mHandler.removeMessages(SHOW_GUEST_SWITCH_DIALOG_MSG);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(SHOW_GUEST_SWITCH_DIALOG_MSG, userId, SMART_TRIM_ADJ_LIMIT, targetUser.name));
        return true;
    }

    private void addBootFailedLog() {
        ArrayList<Integer> pids = new ArrayList();
        pids.add(Integer.valueOf(Process.myPid()));
        int bootErrorNo = 83886081;
        if (this.mBgBroadcastQueue.mOrderedBroadcasts.size() > 0) {
            BroadcastRecord br = (BroadcastRecord) this.mBgBroadcastQueue.mOrderedBroadcasts.get(SMART_TRIM_ADJ_LIMIT);
            if (!(br == null || br.intent == null || !"android.intent.action.PRE_BOOT_COMPLETED".equals(br.intent.getAction()))) {
                HwBootCheck.addBootInfo("currReceiver is: " + br.curComponent.toShortString());
                pids.add(Integer.valueOf(br.curApp.pid));
                bootErrorNo = 83886082;
            }
        }
        File stack = dumpStackTraces(true, pids, null, null, Watchdog.NATIVE_STACKS_OF_INTEREST);
        if (stack == null) {
            if (ActivityManagerDebugConfig.HWFLOW) {
                Slog.i("ActivityManager_FLOW", "addBootFailedLog dumpStackTraces fail");
            }
            Process.sendSignal(Process.myPid(), 3);
        }
        Watchdog.getInstance().addKernelLog();
        HwBootCheck.addBootInfo(this.mSystemServiceManager.dumpInfo());
        SystemClock.sleep(TableJankEvent.recMAXCOUNT);
        HwBootFail.bootFailError(bootErrorNo, ROG_CHANGE_EVENT_SWITCH, HwBootFail.creatFrameworkBootFailLog(stack, HwBootCheck.getBootInfo()));
    }

    public boolean bootSceneStart(int sceneId, long maxTime) {
        try {
            if (IOTController.TYPE_MASTER != Binder.getCallingUid()) {
                Slog.e(TAG, "permission not allowed. uid = " + Binder.getCallingUid());
                return IS_SUPPORT_CLONE_APP;
            } else if (HwBootCheck.getHandlerThread().isAlive()) {
                if (ActivityManagerDebugConfig.HWFLOW) {
                    Slog.i("ActivityManager_FLOW", "bootSceneStart :" + sceneId);
                }
                if (!this.mBootCheckHandler.hasMessages(sceneId)) {
                    this.mBootCheckHandler.sendEmptyMessageDelayed(sceneId, maxTime);
                }
                return true;
            } else {
                if (ActivityManagerDebugConfig.HWFLOW) {
                    Slog.w("ActivityManager_FLOW", "mBootCheckThread is not alive");
                }
                return IS_SUPPORT_CLONE_APP;
            }
        } catch (Exception ex) {
            Flog.e(VISIBLE_APP_ADJ, "bootSceneStart exception: " + ex.toString());
            return IS_SUPPORT_CLONE_APP;
        }
    }

    public boolean bootSceneEnd(int sceneId) {
        try {
            if (IOTController.TYPE_MASTER != Binder.getCallingUid()) {
                Slog.e(TAG, "permission not allowed. uid = " + Binder.getCallingUid());
                return IS_SUPPORT_CLONE_APP;
            }
            if (ActivityManagerDebugConfig.HWFLOW) {
                Slog.i("ActivityManager_FLOW", "bootSceneEnd :" + sceneId);
            }
            if (this.mBootCheckHandler.hasMessages(sceneId)) {
                this.mBootCheckHandler.removeMessages(sceneId);
            }
            return true;
        } catch (Exception ex) {
            Flog.e(VISIBLE_APP_ADJ, "bootSceneEnd exception: " + ex.toString());
            return IS_SUPPORT_CLONE_APP;
        }
    }

    private void sendMessageToSwitchUser(int userId, String userName) {
        UserInfo mCurrentUserInfo = this.mUserController.getUserInfo(this.mUserController.getCurrentUserIdLocked());
        int targetUserId = userId;
        UserInfo mTargetUserInfo = this.mUserController.getUserInfo(userId);
        this.mUserController.setTargetUserIdLocked(userId);
        Pair<UserInfo, UserInfo> userNames = new Pair(mCurrentUserInfo, mTargetUserInfo);
        this.mHandler.removeMessages(SHOW_SWITCH_DIALOG_MSG);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(SHOW_SWITCH_DIALOG_MSG, userNames));
    }

    private void showGuestSwitchDialog(int userId, String userName) {
        cancelDialog();
        ContentResolver cr = this.mContext.getContentResolver();
        int notFirstLogin = System.getIntForUser(cr, SETTING_GUEST_HAS_LOGGED_IN, SMART_TRIM_ADJ_LIMIT, userId);
        Slog.i(TAG, "notFirstLogin:" + notFirstLogin + ", userid=" + userId);
        if (notFirstLogin != 0) {
            showGuestResetSessionDialog(userId);
            return;
        }
        System.putIntForUser(cr, SETTING_GUEST_HAS_LOGGED_IN, ROG_CHANGE_EVENT_SWITCH, userId);
        sendMessageToSwitchUser(userId, userName);
    }

    private void showGuestResetSessionDialog(int guestId) {
        this.mNewSessionDialog = new ResetSessionDialog(this.mContext, guestId);
        this.mNewSessionDialog.show();
        LayoutParams lp = this.mNewSessionDialog.getWindow().getAttributes();
        lp.width = -1;
        this.mNewSessionDialog.getWindow().setAttributes(lp);
    }

    private void wipeGuestSession(int userId) {
        UserManager userManager = (UserManager) this.mContext.getSystemService("user");
        if (userManager.markGuestForDeletion(userId)) {
            UserInfo newGuest = userManager.createGuest(this.mContext, getGuestName());
            if (newGuest == null) {
                Slog.e(TAG, "Could not create new guest, switching back to owner");
                sendMessageToSwitchUser(SMART_TRIM_ADJ_LIMIT, getUserName(SMART_TRIM_ADJ_LIMIT));
                userManager.removeUser(userId);
                return;
            }
            Slog.d(TAG, "Create new guest, switching to = " + newGuest.id);
            sendMessageToSwitchUser(newGuest.id, newGuest.name);
            System.putIntForUser(this.mContext.getContentResolver(), SETTING_GUEST_HAS_LOGGED_IN, ROG_CHANGE_EVENT_SWITCH, newGuest.id);
            userManager.removeUser(userId);
            return;
        }
        Slog.w(TAG, "Couldn't mark the guest for deletion for user " + userId);
    }

    private String getUserName(int userId) {
        if (this.mUserController == null) {
            return null;
        }
        UserInfo info = this.mUserController.getUserInfo(userId);
        if (info == null) {
            return null;
        }
        return info.name;
    }

    private String getGuestName() {
        return this.mContext.getString(33685837);
    }

    private void cancelDialog() {
        if (this.mNewSessionDialog != null && this.mNewSessionDialog.isShowing()) {
            this.mNewSessionDialog.cancel();
            this.mNewSessionDialog = null;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void reportServiceRelationIAware(int relationType, ServiceRecord r, ProcessRecord caller) {
        if (r != null && caller != null && r.name != null && r.appInfo != null && caller.uid != r.appInfo.uid) {
            HwSysResManager resManager = HwSysResManager.getInstance();
            if (resManager != null && resManager.isResourceNeeded(ResourceType.getReousrceId(ResourceType.RESOURCE_APPASSOC))) {
                Bundle bundleArgs = new Bundle();
                int callerUid = caller.uid;
                int callerPid = caller.pid;
                String callerProcessName = caller.processName;
                int targetUid = r.appInfo.uid;
                String targetProcessName = r.processName;
                String compName = r.name.flattenToShortString();
                bundleArgs.putInt("callPid", callerPid);
                bundleArgs.putInt("callUid", callerUid);
                bundleArgs.putString("callProcName", callerProcessName);
                bundleArgs.putInt("tgtUid", targetUid);
                bundleArgs.putString("tgtProcName", targetProcessName);
                bundleArgs.putString("compName", compName);
                bundleArgs.putInt("relationType", relationType);
                CollectData data = new CollectData(ResourceType.getReousrceId(ResourceType.RESOURCE_APPASSOC), System.currentTimeMillis(), bundleArgs);
                long id = Binder.clearCallingIdentity();
                resManager.reportData(data);
                Binder.restoreCallingIdentity(id);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void reportServiceRelationIAware(int relationType, ContentProviderRecord r, ProcessRecord caller) {
        if (caller != null && r != null && r.info != null && r.name != null) {
            int i = caller.uid;
            int i2 = r.uid;
            if (i != r0) {
                HwSysResManager resManager = HwSysResManager.getInstance();
                if (resManager != null && resManager.isResourceNeeded(ResourceType.getReousrceId(ResourceType.RESOURCE_APPASSOC))) {
                    if (r.proc != null) {
                        synchronized (this.mAssocMap) {
                            ArrayMap<Integer, Long> pids = (ArrayMap) this.mAssocMap.get(Integer.valueOf(caller.pid));
                            if (pids != null) {
                                Long elaseTime = (Long) pids.get(Integer.valueOf(r.proc.pid));
                                if (elaseTime == null) {
                                    pids.put(Integer.valueOf(r.proc.pid), Long.valueOf(SystemClock.elapsedRealtime()));
                                } else if (SystemClock.elapsedRealtime() - elaseTime.longValue() < AppHibernateCst.DELAY_ONE_MINS) {
                                    return;
                                }
                            }
                            pids = new ArrayMap();
                            pids.put(Integer.valueOf(r.proc.pid), Long.valueOf(SystemClock.elapsedRealtime()));
                            this.mAssocMap.put(Integer.valueOf(caller.pid), pids);
                        }
                    }
                    Bundle bundleArgs = new Bundle();
                    int callerUid = caller.uid;
                    int callerPid = caller.pid;
                    String callerProcessName = caller.processName;
                    int targetUid = r.uid;
                    String targetProcessName = r.info.processName;
                    String compName = r.name.flattenToShortString();
                    bundleArgs.putInt("callPid", callerPid);
                    bundleArgs.putInt("callUid", callerUid);
                    bundleArgs.putString("callProcName", callerProcessName);
                    bundleArgs.putInt("tgtUid", targetUid);
                    bundleArgs.putString("tgtProcName", targetProcessName);
                    bundleArgs.putString("compName", compName);
                    bundleArgs.putInt("relationType", relationType);
                    CollectData data = new CollectData(ResourceType.getReousrceId(ResourceType.RESOURCE_APPASSOC), System.currentTimeMillis(), bundleArgs);
                    long id = Binder.clearCallingIdentity();
                    resManager.reportData(data);
                    Binder.restoreCallingIdentity(id);
                }
            }
        }
    }

    protected void reportPreviousInfo(int relationType, ProcessRecord prevProc) {
        if (prevProc != null) {
            HwSysResManager resManager = HwSysResManager.getInstance();
            if (resManager != null && resManager.isResourceNeeded(ResourceType.getReousrceId(ResourceType.RESOURCE_APPASSOC))) {
                int prevPid = prevProc.pid;
                int prevUid = prevProc.uid;
                Bundle bundleArgs = new Bundle();
                bundleArgs.putInt(ProcessStopShrinker.PID_KEY, prevPid);
                bundleArgs.putInt("tgtUid", prevUid);
                bundleArgs.putInt("relationType", relationType);
                CollectData data = new CollectData(ResourceType.getReousrceId(ResourceType.RESOURCE_APPASSOC), System.currentTimeMillis(), bundleArgs);
                long id = Binder.clearCallingIdentity();
                resManager.reportData(data);
                Binder.restoreCallingIdentity(id);
            }
        }
    }

    public void reportProcessDied(int pid) {
        synchronized (this.mAssocMap) {
            this.mAssocMap.remove(Integer.valueOf(pid));
            Iterator<Entry<Integer, ArrayMap<Integer, Long>>> it = this.mAssocMap.entrySet().iterator();
            while (it.hasNext()) {
                ArrayMap<Integer, Long> pids = (ArrayMap) ((Entry) it.next()).getValue();
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

    public void reportAssocEnable(ArrayMap<Integer, Integer> forePids) {
        HwSysResManager resManager = HwSysResManager.getInstance();
        if (resManager != null && resManager.isResourceNeeded(ResourceType.getReousrceId(ResourceType.RESOURCE_APPASSOC)) && forePids != null) {
            synchronized (this) {
                for (ProcessRecord proc : this.mLruProcesses) {
                    if (proc != null) {
                        if (proc.foregroundActivities) {
                            forePids.put(Integer.valueOf(proc.pid), Integer.valueOf(proc.uid));
                        }
                        ArrayList<String> pkgs = new ArrayList();
                        int size = proc.pkgList.size();
                        for (int i = SMART_TRIM_ADJ_LIMIT; i < size; i += ROG_CHANGE_EVENT_SWITCH) {
                            String pkg = (String) proc.pkgList.keyAt(i);
                            if (!pkgs.contains(pkg)) {
                                pkgs.add(pkg);
                            }
                        }
                        Bundle args = new Bundle();
                        args.putInt("callPid", proc.pid);
                        args.putInt("callUid", proc.uid);
                        args.putString("callProcName", proc.processName);
                        args.putInt("userid", proc.userId);
                        args.putStringArrayList("pkgname", pkgs);
                        args.putInt("relationType", QUEUE_NUM_RMS);
                        HwSysResManager.getInstance().reportData(new CollectData(ResourceType.getReousrceId(ResourceType.RESOURCE_APPASSOC), System.currentTimeMillis(), args));
                        for (ConnectionRecord cr : proc.connections) {
                            if (!(cr == null || cr.binding == null)) {
                                reportServiceRelationIAware(ROG_CHANGE_EVENT_SWITCH, cr.binding.service, proc);
                            }
                        }
                        for (ContentProviderConnection cpc : proc.conProviders) {
                            if (cpc != null) {
                                reportServiceRelationIAware(ROG_CHANGE_EVENT_INFO, cpc.provider, proc);
                            }
                        }
                    }
                }
                reportHomeProcess(this.mHomeProcess);
            }
        }
    }

    protected void reportHomeProcess(ProcessRecord homeProc) {
        HwSysResManager resManager = HwSysResManager.getInstance();
        if (resManager != null && resManager.isResourceNeeded(ResourceType.getReousrceId(ResourceType.RESOURCE_APPASSOC))) {
            int pid = SMART_TRIM_ADJ_LIMIT;
            int uid = SMART_TRIM_ADJ_LIMIT;
            ArrayList<String> pkgs = new ArrayList();
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
            bundleArgs.putInt(ProcessStopShrinker.PID_KEY, pid);
            bundleArgs.putInt("tgtUid", uid);
            bundleArgs.putStringArrayList("pkgname", pkgs);
            bundleArgs.putInt("relationType", 11);
            CollectData data = new CollectData(ResourceType.getReousrceId(ResourceType.RESOURCE_APPASSOC), System.currentTimeMillis(), bundleArgs);
            long origId = Binder.clearCallingIdentity();
            HwSysResManager.getInstance().reportData(data);
            Binder.restoreCallingIdentity(origId);
        }
    }

    public void setPackageStoppedState(List<String> packageList, boolean stopped) {
        if (packageList != null) {
            int userId = UserHandle.myUserId();
            IPackageManager pm = AppGlobals.getPackageManager();
            try {
                synchronized (this) {
                    for (String packageName : packageList) {
                        pm.setPackageStoppedState(packageName, stopped, userId);
                    }
                }
            } catch (RemoteException e) {
            } catch (IllegalArgumentException e2) {
                Slog.w(TAG, "Failed trying to unstop package " + packageList.toString() + ": " + e2);
            }
        }
    }

    public boolean killProcessRecordFromIAware(ProcessInfo procInfo, boolean restartservice) {
        return killProcessRecordFromIAware(procInfo, restartservice, IS_SUPPORT_CLONE_APP);
    }

    public boolean killProcessRecordFromIAware(ProcessInfo procInfo, boolean restartservice, boolean isAsynchronous) {
        synchronized (this.mPidsSelfLocked) {
            if (procInfo.mPid == MY_PID || procInfo.mPid < 0) {
                Slog.e(TAG, "killProcessRecordFromIAware it is failed to get process record ,mUid :" + procInfo.mUid);
                return IS_SUPPORT_CLONE_APP;
            }
            ProcessRecord proc = (ProcessRecord) this.mPidsSelfLocked.get(procInfo.mPid);
            if (proc == null) {
                Slog.e(TAG, "killProcessRecordFromIAware this process has been killed or died before  :" + procInfo.mProcessName);
                return IS_SUPPORT_CLONE_APP;
            } else if (proc.curAdj >= PERCEPTIBLE_APP_ADJ || AwareAppMngSort.EXEC_SERVICES.equals(proc.adjType)) {
                boolean isClone = isClonedProcess(procInfo.mPid);
                String killedProcessName = proc.processName;
                int killedAppUserId = proc.userId;
                synchronized (this) {
                    cleanupAppInLaunchingProvidersLocked(proc, true);
                    if (isAsynchronous) {
                        proc.killedByAm = true;
                        proc.killed = true;
                    }
                    proc.unlinkDeathRecipient();
                    removeProcessLocked(proc, IS_SUPPORT_CLONE_APP, restartservice, "iAware(" + proc.adjType + ")");
                    if (isAsynchronous) {
                        MemoryUtils.killProcessGroupForQuickKill(proc.info.uid, procInfo.mPid);
                    }
                    cleanupBroadcastLocked(proc);
                    cleanupAlarmLockedExt(proc);
                }
                reportAppDiedMsg(killedAppUserId, killedProcessName, isClone, "iaware");
                return true;
            } else {
                Slog.e(TAG, "killProcessRecordFromIAware process cleaner kill process: adj changed, new adj:" + proc.curAdj + ", old adj:" + procInfo.mCurAdj + ", pid:" + procInfo.mPid + ", uid:" + procInfo.mUid + ", " + procInfo.mProcessName);
                return IS_SUPPORT_CLONE_APP;
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean canCleanTaskRecord(String packageName) {
        if (packageName == null) {
            return true;
        }
        synchronized (this) {
            ArrayList<TaskRecord> recentTasks = getRecentTasks();
            if (recentTasks == null) {
                return true;
            }
            int foundNum = SMART_TRIM_ADJ_LIMIT;
            int i = SMART_TRIM_ADJ_LIMIT;
            while (true) {
                if (i >= recentTasks.size() || foundNum >= ROG_CHANGE_EVENT_SWITCH) {
                } else {
                    TaskRecord tr = (TaskRecord) recentTasks.get(i);
                    if (!(tr == null || tr.mActivities == null)) {
                        if (!(tr.mActivities.size() <= 0 || tr.getBaseIntent() == null || tr.getBaseIntent().getComponent() == null)) {
                            if (packageName.equals(tr.getBaseIntent().getComponent().getPackageName())) {
                                return IS_SUPPORT_CLONE_APP;
                            } else if (AwareAppMngSort.ACTIVITY_RECENT_TASK.equals(tr.getBaseIntent().getComponent().flattenToShortString())) {
                            }
                        }
                        foundNum += ROG_CHANGE_EVENT_SWITCH;
                    }
                    i += ROG_CHANGE_EVENT_SWITCH;
                }
            }
            return true;
        }
    }

    public void cleanActivityByUid(List<String> packageList, int targetUid) {
        synchronized (this) {
            int userId = UserHandle.getUserId(targetUid);
            for (String packageName : packageList) {
                if (canCleanTaskRecord(packageName)) {
                    this.mStackSupervisor.finishDisabledPackageActivitiesLocked(packageName, null, true, IS_SUPPORT_CLONE_APP, userId);
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int numOfPidWithActivity(int uid) {
        int count = SMART_TRIM_ADJ_LIMIT;
        synchronized (this.mPidsSelfLocked) {
            int i = SMART_TRIM_ADJ_LIMIT;
            while (true) {
                if (i < this.mPidsSelfLocked.size()) {
                    ProcessRecord p = (ProcessRecord) this.mPidsSelfLocked.valueAt(i);
                    if (p.uid == uid && p.hasShownUi) {
                        count += ROG_CHANGE_EVENT_SWITCH;
                    }
                    i += ROG_CHANGE_EVENT_SWITCH;
                }
            }
        }
        return count;
    }

    public boolean cleanPackageRes(List<String> packageList, int targetUid, boolean cleanAlarm) {
        if (packageList == null) {
            return IS_SUPPORT_CLONE_APP;
        }
        boolean didSomething = IS_SUPPORT_CLONE_APP;
        int userId = UserHandle.getUserId(targetUid);
        synchronized (this) {
            for (String packageName : packageList) {
                int i;
                if (canCleanTaskRecord(packageName) && this.mStackSupervisor.finishDisabledPackageActivitiesLocked(packageName, null, true, IS_SUPPORT_CLONE_APP, userId)) {
                    didSomething = true;
                }
                if (this.mServices.bringDownDisabledPackageServicesLocked(packageName, null, userId, IS_SUPPORT_CLONE_APP, true, true)) {
                    didSomething = true;
                }
                if (packageName == null) {
                    this.mStickyBroadcasts.remove(userId);
                }
                ArrayList<ContentProviderRecord> providers = new ArrayList();
                if (this.mProviderMap.collectPackageProvidersLocked(packageName, null, true, IS_SUPPORT_CLONE_APP, userId, providers)) {
                    didSomething = true;
                }
                ArrayList<ContentProviderRecord> providersForClone = new ArrayList();
                this.mProviderMapForClone.collectPackageProvidersLocked(packageName, null, true, IS_SUPPORT_CLONE_APP, userId, providersForClone);
                providers.addAll(providersForClone);
                for (i = providers.size() - 1; i >= 0; i--) {
                    cleanProviderLocked(null, (ContentProviderRecord) providers.get(i), true);
                }
                for (i = this.mBroadcastQueues.length - 1; i >= 0; i--) {
                    didSomething |= this.mBroadcastQueues[i].cleanupDisabledPackageReceiversLocked(packageName, null, userId, true);
                }
                if (cleanAlarm && this.mAlms != null) {
                    this.mAlms.removePackageAlarm(packageName);
                }
            }
        }
        return didSomething;
    }

    private final boolean cleanProviderLocked(ProcessRecord proc, ContentProviderRecord cpr, boolean always) {
        boolean inLaunching = this.mLaunchingProviders.contains(cpr);
        if (!inLaunching || always) {
            synchronized (cpr) {
                cpr.launchingApp = null;
                cpr.notifyAll();
            }
            ProviderMap providerMap = this.mProviderMap;
            if (cpr.info.applicationInfo.euid != 0) {
                providerMap = this.mProviderMapForClone;
            }
            providerMap.removeProviderByClass(cpr.name, UserHandle.getUserId(cpr.uid));
            String[] names = cpr.info.authority.split(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
            for (int j = SMART_TRIM_ADJ_LIMIT; j < names.length; j += ROG_CHANGE_EVENT_SWITCH) {
                providerMap.removeProviderByName(names[j], UserHandle.getUserId(cpr.uid));
            }
        }
        for (int i = cpr.connections.size() - 1; i >= 0; i--) {
            ContentProviderConnection conn = (ContentProviderConnection) cpr.connections.get(i);
            if (!conn.waiting || !inLaunching || always) {
                ProcessRecord capp = conn.client;
                conn.dead = true;
                if (conn.stableCount > 0) {
                    if (!(capp.persistent || capp.thread == null || capp.pid == 0 || capp.pid == MY_PID)) {
                        capp.kill("depends on provider " + cpr.name.flattenToShortString() + " in dying proc " + (proc != null ? proc.processName : "??"), true);
                    }
                } else if (!(capp.thread == null || conn.provider.provider == null)) {
                    try {
                        capp.thread.unstableProviderDied(conn.provider.provider.asBinder());
                    } catch (RemoteException e) {
                    }
                    cpr.connections.remove(i);
                    if (conn.client.conProviders.remove(conn)) {
                        stopAssociationLocked(capp.uid, capp.processName, cpr.uid, cpr.name);
                    }
                    if (!(proc == null || proc.uid < LifeCycleStateMachine.TIME_OUT_TIME || capp.pid == proc.pid || capp.info == null || proc.info == null || capp.info.packageName == null || capp.info.packageName.equals(proc.info.packageName))) {
                        String str = proc.processName;
                        String num = Integer.toString(capp.pid);
                        String num2 = Integer.toString(proc.pid);
                        String[] strArr = new String[ROG_CHANGE_EVENT_SWITCH];
                        strArr[SMART_TRIM_ADJ_LIMIT] = "provider";
                        LogPower.push(167, str, num, num2, strArr);
                    }
                }
            }
        }
        if (inLaunching && always) {
            this.mLaunchingProviders.remove(cpr);
        }
        return inLaunching;
    }

    public void setAlarmManagerExt(AlarmManagerService service) {
        synchronized (this) {
            this.mAlms = service;
        }
    }

    protected void cleanupAlarmLockedExt(ProcessRecord process) {
        if (!isThirdParty(process)) {
            return;
        }
        if (this.mAlms == null) {
            Log.w(TAG, "Could not get instance of AlarmManagerService.");
            return;
        }
        ArrayList<String> array = new ArrayList();
        for (String pkg : process.pkgList.keySet()) {
            array.add(pkg);
        }
        if (array.size() > 0) {
            this.mAlms.cleanupAlarmLocked(array);
        }
    }

    private static boolean isThirdParty(ProcessRecord process) {
        if (process == null || process.pid == ActivityManagerService.MY_PID || (process.info.flags & ROG_CHANGE_EVENT_SWITCH) != 0) {
            return IS_SUPPORT_CLONE_APP;
        }
        return true;
    }

    protected void forceValidateHomeButton() {
        if (Global.getInt(this.mContext.getContentResolver(), "device_provisioned", SMART_TRIM_ADJ_LIMIT) == 0 || Secure.getInt(this.mContext.getContentResolver(), "user_setup_complete", SMART_TRIM_ADJ_LIMIT) == 0) {
            Global.putInt(this.mContext.getContentResolver(), "device_provisioned", ROG_CHANGE_EVENT_SWITCH);
            Secure.putInt(this.mContext.getContentResolver(), "user_setup_complete", ROG_CHANGE_EVENT_SWITCH);
            Log.w(TAG, "DEVICE_PROVISIONED or USER_SETUP_COMPLETE set 0 to 1!");
        }
    }

    protected boolean isStartLauncherActivity(Intent intent) {
        if (intent == null) {
            Log.w(TAG, "intent is null, not start launcher!");
            return IS_SUPPORT_CLONE_APP;
        }
        PackageManager pm = this.mContext.getPackageManager();
        Intent mainIntent = new Intent("android.intent.action.MAIN").addCategory("android.intent.category.HOME").addCategory("android.intent.category.DEFAULT");
        ComponentName cmp = intent.getComponent();
        if (pm != null && intent.hasCategory("android.intent.category.HOME")) {
            for (ResolveInfo info : pm.queryIntentActivities(mainIntent, SMART_TRIM_ADJ_LIMIT)) {
                if (info != null && info.priority == 0 && cmp != null && info.activityInfo != null && cmp.getPackageName().equals(info.activityInfo.packageName)) {
                    Log.d(TAG, "info priority is 0, cmp: " + cmp);
                    return true;
                }
            }
        }
        return IS_SUPPORT_CLONE_APP;
    }

    private void deleteClonedPackage(String packageName) {
        int i;
        int appId = -1;
        try {
            appId = UserHandle.getAppId(AppGlobals.getPackageManager().getPackageUid(packageName, 268435456, SMART_TRIM_ADJ_LIMIT));
        } catch (RemoteException e) {
        }
        killPackageProcessesLocked(packageName, appId, SMART_TRIM_ADJ_LIMIT, -10000, IS_SUPPORT_CLONE_APP, true, true, IS_SUPPORT_CLONE_APP, "stop " + packageName + "delete cloned app");
        this.mStackSupervisor.finishDisabledPackageActivitiesLocked(packageName, null, true, IS_SUPPORT_CLONE_APP, 2147383647);
        this.mServices.bringDownDisabledPackageServicesLocked(packageName, null, 2147383647, IS_SUPPORT_CLONE_APP, true, true);
        ArrayList<ContentProviderRecord> providersForClone = new ArrayList();
        this.mProviderMapForClone.collectPackageProvidersLocked(packageName, null, true, IS_SUPPORT_CLONE_APP, SMART_TRIM_ADJ_LIMIT, providersForClone);
        for (i = providersForClone.size() - 1; i >= 0; i--) {
            removeDyingProviderLocked(null, (ContentProviderRecord) providersForClone.get(i), true);
        }
        for (i = this.mBroadcastQueues.length - 1; i >= 0; i--) {
            this.mBroadcastQueues[i].cleanupDisabledPackageReceiversLocked(packageName, null, 2147383647, true);
        }
        if (this.mBooted) {
            this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
            this.mStackSupervisor.scheduleIdleLocked();
        }
    }

    protected List<ResolveInfo> queryIntentReceivers(ProcessRecord callerApp, Intent intent, String resolvedType, int flags, int userId) throws RemoteException {
        List<ResolveInfo> list;
        if (callerApp == null || callerApp.info.euid == 0) {
            if ((intent.getHwFlags() & ROG_CHANGE_EVENT_SWITCH) != 0) {
            }
            list = AppGlobals.getPackageManager().queryIntentReceivers(intent, resolvedType, flags, userId).getList();
            if (IS_SUPPORT_CLONE_APP || list == null || list.size() == 0 || userId != 0) {
                return list;
            }
            Flog.i(IOTController.EV_CANCEL_AUTH_ALL, "collectReceiverComponents, callerApp: " + callerApp + ", intent:" + intent.getAction() + ", receiver size:" + list.size() + ", flags: " + Integer.toHexString(flags));
            List<ResolveInfo> finalReceivers = new ArrayList();
            for (ResolveInfo ri : list) {
                finalReceivers.add(ri);
                if ((callerApp == null || !callerApp.info.packageName.equals(ri.activityInfo.packageName)) && isPackageCloned(ri.activityInfo.packageName, userId) && (intent.getHwFlags() & 32) == 0) {
                    int i;
                    ResolveInfo copy = new ResolveInfo(ri);
                    copy.activityInfo = new ActivityInfo(copy.activityInfo);
                    copy.activityInfo.applicationInfo = new ApplicationInfo(copy.activityInfo.applicationInfo);
                    ApplicationInfo applicationInfo = copy.activityInfo.applicationInfo;
                    if (ri.activityInfo.applicationInfo.euid == 0) {
                        i = 2147383647;
                    } else {
                        i = SMART_TRIM_ADJ_LIMIT;
                    }
                    applicationInfo.euid = i;
                    finalReceivers.add(copy);
                    Flog.i(IOTController.EV_CANCEL_AUTH_ALL, "handleClonedReceivers, package: " + ri.activityInfo.packageName + " have been cloned and we copy an extra receiver " + copy);
                }
            }
            return finalReceivers;
        }
        flags |= 4194304;
        intent.addHwFlags(ROG_CHANGE_EVENT_SWITCH);
        list = AppGlobals.getPackageManager().queryIntentReceivers(intent, resolvedType, flags, userId).getList();
        if (IS_SUPPORT_CLONE_APP) {
        }
        return list;
    }

    public IIntentSender getIntentSender(int type, String packageName, IBinder token, String resultWho, int requestCode, Intent[] intents, String[] resolvedTypes, int flags, Bundle options, int userId) {
        if (intents != null && intents.length > 0) {
            boolean isClonedProcess = isClonedProcess(Binder.getCallingPid());
            boolean isPackageCloned = isPackageCloned(getPackageNameForPid(Binder.getCallingPid()), userId);
            for (int i = SMART_TRIM_ADJ_LIMIT; i < intents.length; i += ROG_CHANGE_EVENT_SWITCH) {
                if (intents[i] != null) {
                    if (isClonedProcess) {
                        intents[i].addHwFlags(ROG_CHANGE_EVENT_SWITCH);
                    }
                    if (isPackageCloned) {
                        intents[i].addHwFlags(32);
                    }
                }
            }
        }
        return super.getIntentSender(type, packageName, token, resultWho, requestCode, intents, resolvedTypes, flags, options, userId);
    }

    protected void filterRegisterReceiversForEuid(List<BroadcastFilter> registeredReceivers, ProcessRecord callerApp) {
        if (registeredReceivers != null && callerApp != null) {
            Iterator<BroadcastFilter> item = registeredReceivers.iterator();
            while (item.hasNext()) {
                BroadcastFilter filter = (BroadcastFilter) item.next();
                if (!(filter.receiverList == null || filter.receiverList.app == null || !filter.receiverList.app.info.packageName.equals(callerApp.info.packageName) || filter.receiverList.app.info.euid == callerApp.info.euid)) {
                    Slog.d(TAG, "prevent start receiver of package " + filter.receiverList.app.info.packageName + " because euid is different" + "  callerApp euid is " + callerApp.info.euid);
                    item.remove();
                }
            }
        }
    }

    public boolean isClonedProcess(int pid) {
        if (!IS_SUPPORT_CLONE_APP) {
            return IS_SUPPORT_CLONE_APP;
        }
        synchronized (this.mPidsSelfLocked) {
            ProcessRecord proc = (ProcessRecord) this.mPidsSelfLocked.get(pid);
            if (proc == null || proc.info.euid == 0) {
                return IS_SUPPORT_CLONE_APP;
            }
            Flog.i(VISIBLE_APP_ADJ, "ProcessRecord " + proc + " is a cloned process");
            return true;
        }
    }

    private String getPackageNameForPid(int pid) {
        synchronized (this.mPidsSelfLocked) {
            ProcessRecord proc = (ProcessRecord) this.mPidsSelfLocked.get(pid);
            if (proc != null) {
                String str = proc.info != null ? proc.info.packageName : "android";
                return str;
            }
            Flog.i(VISIBLE_APP_ADJ, "ProcessRecord for pid " + pid + " does not exist");
            return null;
        }
    }

    public boolean isPackageCloned(String packageName, int userId) {
        if (!IS_SUPPORT_CLONE_APP || packageName == null || packageName.trim().isEmpty() || this.mCloneAppList == null || this.mCloneAppList.trim().isEmpty() || userId != 0) {
            return IS_SUPPORT_CLONE_APP;
        }
        boolean isPackageCloned = (this.mCloneAppList + CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER).contains(packageName + CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
        if (isPackageCloned) {
            Flog.i(VISIBLE_APP_ADJ, "App " + packageName + " cloned: " + isPackageCloned);
        }
        return isPackageCloned;
    }

    public boolean registerThirdPartyCallBack(IMWThirdpartyCallback aCallBackHandler) {
        boolean lRegistered = IS_SUPPORT_CLONE_APP;
        if (aCallBackHandler != null) {
            synchronized (this.mThirdPartyCallbackList) {
                lRegistered = this.mThirdPartyCallbackList.register(aCallBackHandler);
            }
        }
        return lRegistered;
    }

    public boolean unregisterThirdPartyCallBack(IMWThirdpartyCallback aCallBackHandler) {
        boolean lUnregistered = IS_SUPPORT_CLONE_APP;
        if (aCallBackHandler != null) {
            synchronized (this.mThirdPartyCallbackList) {
                lUnregistered = this.mThirdPartyCallbackList.unregister(aCallBackHandler);
            }
        }
        return lUnregistered;
    }

    public boolean isInMultiWindowMode() {
        boolean z = IS_SUPPORT_CLONE_APP;
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this) {
                ActivityStack focusedStack = getFocusedStack();
                if (focusedStack == null) {
                    return IS_SUPPORT_CLONE_APP;
                }
                ActivityRecord top = focusedStack.topRunningActivityLocked();
                if (top == null) {
                    Binder.restoreCallingIdentity(origId);
                    return IS_SUPPORT_CLONE_APP;
                }
                if (!top.task.mFullscreen) {
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
        synchronized (this.mThirdPartyCallbackList) {
            try {
                int i = this.mThirdPartyCallbackList.beginBroadcast();
                Flog.i(VISIBLE_APP_ADJ, "onMultiWindowModeChanged : mThirdPartyCallbackList size : " + i);
                while (i > 0) {
                    i--;
                    try {
                        ((IMWThirdpartyCallback) this.mThirdPartyCallbackList.getBroadcastItem(i)).onModeChanged(isInMultiWindowMode);
                    } catch (RemoteException e) {
                        Flog.e(VISIBLE_APP_ADJ, "Error in sending the Callback");
                    }
                }
                this.mThirdPartyCallbackList.finishBroadcast();
            } catch (IllegalStateException e2) {
                Flog.e(VISIBLE_APP_ADJ, "beginBroadcast() called while already in a broadcast");
            }
        }
    }

    public void cleanPackageNotifications(List<String> packageList, int targetUid) {
        if (packageList != null) {
            INotificationManager service = NotificationManager.getService();
            if (service != null) {
                int userId = UserHandle.getUserId(targetUid);
                try {
                    Slog.v(TAG, "cleanupPackageNotifications, userId=" + userId + "|" + packageList);
                    for (String packageName : packageList) {
                        service.cancelAllNotifications(packageName, userId);
                    }
                } catch (RemoteException e) {
                    Slog.e(TAG, "Unable to talk to notification manager. Woe!");
                }
            }
        }
    }

    public void cleanNotificationWithPid(List<String> packageList, int targetUid, int pid) {
        if (packageList != null) {
            INotificationManager service = NotificationManager.getService();
            if (service != null) {
                try {
                    StatusBarNotification[] notifications = service.getActiveNotifications("android");
                    int userId = UserHandle.getUserId(targetUid);
                    if (notifications != null) {
                        int length = notifications.length;
                        for (int i = SMART_TRIM_ADJ_LIMIT; i < length; i += ROG_CHANGE_EVENT_SWITCH) {
                            StatusBarNotification notification = notifications[i];
                            if (notification.getInitialPid() == pid) {
                                for (String packageName : packageList) {
                                    service.cancelNotificationWithTag(packageName, notification.getTag(), notification.getId(), userId);
                                }
                            }
                        }
                    }
                } catch (RemoteException e) {
                    Slog.e(TAG, "Unable to talk to notification manager. Woe!");
                }
            }
        }
    }

    public boolean hasNotification(int pid) {
        if (pid < 0) {
            return IS_SUPPORT_CLONE_APP;
        }
        INotificationManager service = NotificationManager.getService();
        if (service == null) {
            return IS_SUPPORT_CLONE_APP;
        }
        try {
            StatusBarNotification[] notifications = service.getActiveNotifications("android");
            if (notifications == null) {
                return IS_SUPPORT_CLONE_APP;
            }
            int length = notifications.length;
            for (int i = SMART_TRIM_ADJ_LIMIT; i < length; i += ROG_CHANGE_EVENT_SWITCH) {
                if (notifications[i].getInitialPid() == pid) {
                    return true;
                }
            }
            return IS_SUPPORT_CLONE_APP;
        } catch (RemoteException e) {
            Slog.e(TAG, "Unable to talk to notification manager. Woe!");
        }
    }

    public boolean isLauncher(String packageName) {
        if (Process.myUid() != IOTController.TYPE_MASTER || packageName == null || packageName.trim().isEmpty()) {
            return IS_SUPPORT_CLONE_APP;
        }
        if ("com.huawei.android.launcher".equals(packageName)) {
            return true;
        }
        if (this.mContext != null) {
            List<ResolveInfo> outActivities = new ArrayList();
            PackageManager pm = this.mContext.getPackageManager();
            if (pm != null) {
                ComponentName componentName = pm.getHomeActivities(outActivities);
                if (componentName != null && componentName.getPackageName() != null) {
                    return packageName.equals(componentName.getPackageName()) ? true : IS_SUPPORT_CLONE_APP;
                } else {
                    for (ResolveInfo info : outActivities) {
                        String homePkg = info.activityInfo.packageName;
                        if (packageName.equals(homePkg)) {
                            Slog.d(TAG, "homePkg is " + homePkg + " ,isLauncher");
                            return true;
                        }
                    }
                }
            }
        }
        return IS_SUPPORT_CLONE_APP;
    }

    int broadcastIntentInPackage(String packageName, int uid, Intent intent, String resolvedType, IIntentReceiver resultTo, int resultCode, String resultData, Bundle resultExtras, String requiredPermission, Bundle options, boolean serialized, boolean sticky, int userId) {
        int broadcastIntentInPackage;
        synchronized (this) {
            if (!(packageName == null || options == null)) {
                if (options.getBoolean("fromSystemUI")) {
                    Slog.d(TAG, "packageName: " + packageName + ", uid: " + uid + ", resolvedType: " + resolvedType + ", resultCode: " + resultCode + ", requiredPermission: " + requiredPermission + ", userId: " + userId);
                    try {
                        AppGlobals.getPackageManager().setPackageStoppedState(packageName, IS_SUPPORT_CLONE_APP, UserHandle.getUserId(uid));
                    } catch (RemoteException e) {
                        Slog.w(TAG, "Failed trying to unstop " + packageName + " due to  RemoteException");
                    } catch (IllegalArgumentException e2) {
                        Slog.w(TAG, "Failed trying to unstop package " + packageName + " due to IllegalArgumentException");
                    }
                }
            }
            broadcastIntentInPackage = super.broadcastIntentInPackage(packageName, uid, intent, resolvedType, resultTo, resultCode, resultData, resultExtras, requiredPermission, options, serialized, sticky, userId);
        }
        return broadcastIntentInPackage;
    }

    private void restartAndNotifyPkgForRog(ActivityStack stack, IHwRogListener listener, int eventType, Object param) {
        try {
            String packageName = listener.getPackageName();
            ActivityRecord starting = stack.restartPackage(packageName);
            switch (eventType) {
                case ROG_CHANGE_EVENT_SWITCH /*1*/:
                    UpdateRog updateRog = (UpdateRog) param;
                    listener.onRogSwitchStateChanged(updateRog.rogEnable, updateRog.rogInfo);
                    break;
                case ROG_CHANGE_EVENT_INFO /*2*/:
                    listener.onRogInfoUpdated((AppRogInfo) param);
                    break;
                default:
                    try {
                        Slog.w(TAG, "restartAndNotifyPkgForRog->unknown msg:" + eventType);
                        break;
                    } catch (Exception e) {
                        Slog.e(TAG, "restartAndNotifyPkgForRog->notify app exception:" + e);
                        return;
                    }
            }
            if (starting != null && starting.packageName.equalsIgnoreCase(packageName)) {
                stack.ensureActivityConfigurationLocked(starting, SMART_TRIM_ADJ_LIMIT, IS_SUPPORT_CLONE_APP);
                stack.ensureActivitiesVisibleLocked(starting, SMART_TRIM_ADJ_LIMIT, IS_SUPPORT_CLONE_APP);
            }
        } catch (Exception e2) {
            Slog.e(TAG, "restartAndNotifyPkgForRog->get package name exception:" + e2);
        }
    }

    protected void applyRogStateChangedForStack(IHwRogListener listener, boolean rogEnable, AppRogInfo rogInfo, ActivityStack stack) {
        if (listener == null) {
            Slog.w(TAG, "applyRogStateChangedForStack->listener is null");
            return;
        }
        UpdateRog updateRog = new UpdateRog();
        updateRog.rogEnable = rogEnable;
        updateRog.rogInfo = rogInfo;
        restartAndNotifyPkgForRog(stack, listener, ROG_CHANGE_EVENT_SWITCH, updateRog);
    }

    protected void applyRogInfoUpdatedForStack(IHwRogListener listener, AppRogInfo rogInfo, ActivityStack stack) {
        if (listener == null) {
            Slog.w(TAG, "applyRogInfoUpdatedForStack->listener is null");
        } else {
            restartAndNotifyPkgForRog(stack, listener, ROG_CHANGE_EVENT_INFO, rogInfo);
        }
    }

    protected void attachRogInfoToApp(ProcessRecord app, ApplicationInfo appInfo) {
        IRogManager rogManager = (IRogManager) LocalServices.getService(IRogManager.class);
        if (rogManager != null) {
            if (app.instrumentationArguments == null) {
                app.instrumentationArguments = new Bundle();
            }
            app.instrumentationArguments.putBoolean("switch_state_key", rogManager.getRogSwitchState());
            app.instrumentationArguments.putParcelable("info_key", rogManager.getSpecifiedAppRogInfo(appInfo.packageName));
        }
    }

    public void setLastSplittableActivity(Intent intent, String extras) {
        this.mSplitIntentInfo = intent;
        this.mSplitExtras = extras;
    }

    public boolean isLastSplittableActivity(Intent intent, String extras) {
        boolean z = IS_SUPPORT_CLONE_APP;
        if (this.mSplitIntentInfo == null) {
            return IS_SUPPORT_CLONE_APP;
        }
        if (this.mSplitIntentInfo.filterEquals(intent)) {
            z = Objects.equals(this.mSplitExtras, extras);
        }
        return z;
    }

    public void addToEntryStack(IBinder token, int resultCode, Intent resultData) {
        if (this.mSplitActivityEntryStack == null) {
            this.mSplitActivityEntryStack = new Stack();
        }
        Flog.i(VISIBLE_APP_ADJ, "addToEntryStack, activity is " + token);
        this.mSplitActivityEntryStack.push(token);
    }

    public void clearEntryStack(IBinder selfToken) {
        if (this.mSplitActivityEntryStack != null) {
            Flog.i(VISIBLE_APP_ADJ, "selfToken is " + selfToken + ", top is " + this.mSplitActivityEntryStack.peek());
        }
        if (this.mSplitActivityEntryStack != null && (selfToken == null || selfToken.equals(this.mSplitActivityEntryStack.peek()))) {
            long ident = Binder.clearCallingIdentity();
            while (!this.mSplitActivityEntryStack.isEmpty()) {
                IBinder token = (IBinder) this.mSplitActivityEntryStack.pop();
                if (!(token == null || token.equals(selfToken))) {
                    Flog.i(VISIBLE_APP_ADJ, "Clearing entry " + token);
                    this.mWindowManager.setAppVisibility(token, IS_SUPPORT_CLONE_APP);
                    finishActivity(token, SMART_TRIM_ADJ_LIMIT, null, SMART_TRIM_ADJ_LIMIT);
                }
            }
            Binder.restoreCallingIdentity(ident);
            if (selfToken != null) {
                this.mSplitActivityEntryStack.push(selfToken);
            }
        }
    }

    public boolean isTopSplitActivity(IBinder token) {
        if (this.mSplitActivityEntryStack == null || token == null) {
            return IS_SUPPORT_CLONE_APP;
        }
        return token.equals(this.mSplitActivityEntryStack.peek());
    }

    public void removeFromEntryStack(IBinder token) {
        if (token != null && this.mSplitActivityEntryStack != null) {
            this.mSplitActivityEntryStack.remove(token);
        }
    }

    public boolean isLimitedPackageBroadcast(Intent intent) {
        String action = intent.getAction();
        if (!"android.intent.action.PACKAGE_ADDED".equals(action) && !"android.intent.action.PACKAGE_REMOVED".equals(action)) {
            return IS_SUPPORT_CLONE_APP;
        }
        Bundle intentExtras = intent.getExtras();
        boolean z = intentExtras != null ? intentExtras.getBoolean("LimitedPackageBroadcast", IS_SUPPORT_CLONE_APP) : IS_SUPPORT_CLONE_APP;
        Flog.d(VISIBLE_APP_ADJ, "Android Wear-isLimitedPackageBroadcast: limitedPackageBroadcast = " + z);
        return z;
    }

    private boolean isAppMngOomAdjCustomized(String packageName) {
        return AwareDefaultConfigList.getInstance().isAppMngOomAdjCustomized(packageName);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setAndRestoreMaxAdjIfNeed(Set<String> adjCustPkg) {
        if (adjCustPkg != null) {
            synchronized (this) {
                synchronized (this.mPidsSelfLocked) {
                    int i = SMART_TRIM_ADJ_LIMIT;
                    while (true) {
                        if (i < this.mPidsSelfLocked.size()) {
                            ProcessRecord p = (ProcessRecord) this.mPidsSelfLocked.valueAt(i);
                            if (p != null) {
                                boolean pkgContains = IS_SUPPORT_CLONE_APP;
                                for (String pkg : p.pkgList.keySet()) {
                                    if (adjCustPkg.contains(pkg)) {
                                        pkgContains = true;
                                        break;
                                    }
                                }
                                if (pkgContains) {
                                    if (p.maxAdj > AwareDefaultConfigList.HW_PERCEPTIBLE_APP_ADJ) {
                                        p.maxAdj = AwareDefaultConfigList.HW_PERCEPTIBLE_APP_ADJ;
                                    }
                                } else if (p.maxAdj == AwareDefaultConfigList.HW_PERCEPTIBLE_APP_ADJ) {
                                    p.maxAdj = IOTController.TYPE_SLAVE;
                                }
                            }
                            i += ROG_CHANGE_EVENT_SWITCH;
                        }
                    }
                }
            }
        }
    }

    public boolean isPkgHasAlarm(List<String> packageList, int targetUid) {
        if (packageList == null) {
            return IS_SUPPORT_CLONE_APP;
        }
        synchronized (this) {
            for (String packageName : packageList) {
                if (this.mIntentSenderRecords.size() > 0) {
                    for (WeakReference<PendingIntentRecord> wpir : this.mIntentSenderRecords.values()) {
                        if (wpir != null) {
                            PendingIntentRecord pir = (PendingIntentRecord) wpir.get();
                            if (!(pir == null || pir.key == null || pir.key.packageName == null || !pir.key.packageName.equals(packageName))) {
                                return true;
                            }
                        }
                    }
                    continue;
                }
            }
            return IS_SUPPORT_CLONE_APP;
        }
    }

    private int preloadApplication(String packageName, int userid, boolean isClonePackage) {
        if (Binder.getCallingUid() != IOTController.TYPE_MASTER) {
            return -1;
        }
        synchronized (this) {
            if (ProcessList.computeEmptyProcessLimit(this.mProcessLimit) <= QUEUE_NUM_RMS) {
                return -1;
            }
            IPackageManager pm = AppGlobals.getPackageManager();
            if (pm == null) {
                return -1;
            }
            ApplicationInfo appInfo = null;
            try {
                appInfo = pm.getApplicationInfo(packageName, 1152, userid);
            } catch (RemoteException e) {
                Slog.w(TAG, "Failed trying to get application info: " + packageName);
            }
            if (appInfo == null) {
                Slog.d(TAG, "preloadApplication, get application info failed, packageName = " + packageName);
                return -1;
            }
            ProcessRecord app;
            if (isClonePackage) {
                appInfo.euid = 2147383647;
                app = getProcessRecordLocked(appInfo.processName, appInfo.uid + appInfo.euid, true);
            } else {
                app = getProcessRecordLocked(appInfo.processName, appInfo.uid, true);
            }
            if (app != null && app.thread != null) {
                Slog.d(TAG, "process has started, packageName:" + packageName + ", processName:" + appInfo.processName);
                return -1;
            } else if ((appInfo.flags & PERSISTENT_MASK) == PERSISTENT_MASK) {
                Slog.d(TAG, "preloadApplication, application is persistent, return");
                return -1;
            } else {
                if (app == null) {
                    app = newProcessRecordLocked(appInfo, null, IS_SUPPORT_CLONE_APP, SMART_TRIM_ADJ_LIMIT);
                    updateLruProcessLocked(app, IS_SUPPORT_CLONE_APP, null);
                    updateOomAdjLocked();
                }
                try {
                    pm.setPackageStoppedState(packageName, IS_SUPPORT_CLONE_APP, UserHandle.getUserId(app.uid));
                } catch (RemoteException e2) {
                    Slog.w(TAG, "RemoteException, Failed trying to unstop package: " + packageName);
                } catch (IllegalArgumentException e3) {
                    Slog.w(TAG, "IllegalArgumentException, Failed trying to unstop package " + packageName);
                }
                if (app.thread == null) {
                    startProcessLocked(app, "start application", app.processName, null, null, null);
                }
                return SMART_TRIM_ADJ_LIMIT;
            }
        }
    }

    void reportAppForceStopMsg(int userId, String packageName, int callingPid) {
        String STSMANAGER_PKGNAME = "com.huawei.systemmanager";
        String POWERGENIE_PKGNAME = "com.huawei.powergenie";
        String SETTINGS_PKGNAME = WifiProCommonUtils.HUAWEI_SETTINGS;
        boolean killedBySysManager = checkIfPackageNameMatchesPid(callingPid, "com.huawei.systemmanager");
        boolean killedByPowerGenie = checkIfPackageNameMatchesPid(callingPid, "com.huawei.powergenie");
        boolean killedBySettings = checkIfPackageNameMatchesPid(callingPid, WifiProCommonUtils.HUAWEI_SETTINGS);
        if ((killedBySysManager || killedByPowerGenie || killedBySettings) && !isPackageCloned(packageName, userId)) {
            if (killedBySysManager) {
                reportAppDiedMsg(userId, packageName, IS_SUPPORT_CLONE_APP, "sysManager");
            } else if (killedByPowerGenie) {
                reportAppDiedMsg(userId, packageName, IS_SUPPORT_CLONE_APP, "powerGenie");
            } else {
                reportAppDiedMsg(userId, packageName, IS_SUPPORT_CLONE_APP, "settings");
            }
        }
    }

    private boolean checkIfPackageNameMatchesPid(int mPid, String targetPackageName) {
        if (targetPackageName.equals(getPackageNameForPid(mPid))) {
            return true;
        }
        return IS_SUPPORT_CLONE_APP;
    }

    void reportAppDiedMsg(int userId, String processName, boolean isClone, String reason) {
        if (processName != null && !processName.contains(":") && reason != null) {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(processName).append(CPUCustBaseConfig.CPUCONFIG_INVALID_STR);
            stringBuffer.append(String.valueOf(userId));
            if (isClone) {
                stringBuffer.append("#true#");
            } else {
                stringBuffer.append("#false#");
            }
            stringBuffer.append(reason);
            LogIAware.report(2031, stringBuffer.toString());
        }
    }

    void reportAppDiedMsg(AppDiedInfo appDiedInfo) {
        if (appDiedInfo != null) {
            if ("forceStop".equals(appDiedInfo.reason)) {
                reportAppForceStopMsg(appDiedInfo.userId, appDiedInfo.processName, appDiedInfo.callerPid);
            } else {
                reportAppDiedMsg(appDiedInfo.userId, appDiedInfo.processName, appDiedInfo.isClone, appDiedInfo.reason);
            }
        }
    }

    public boolean shouldNotKillProcWhenRemoveTask(String pkg) {
        if (!HwConnectivityService.MM_PKG_NAME.equals(pkg)) {
            return IS_SUPPORT_CLONE_APP;
        }
        Slog.d(TAG, " cleanUpRemovedTaskLocked, do not kill process : " + pkg);
        return true;
    }

    private void showInterceptAppDialog(String pkgName) {
        cancelInterceptAppDialog();
        this.mInterceptAppDialog = new InterceptAppDialog(this.mContext, pkgName);
        this.mInterceptAppDialog.show();
    }

    private void cancelInterceptAppDialog() {
        if (this.mInterceptAppDialog != null && this.mInterceptAppDialog.isShowing()) {
            this.mInterceptAppDialog.cancel();
            this.mInterceptAppDialog = null;
        }
    }

    private boolean interceptGoogleAppIfNeed(String pkgName, int userId) {
        if (!InterceptAppLaunchUtils.isNeedInterceptGoogleApp(pkgName, userId)) {
            return IS_SUPPORT_CLONE_APP;
        }
        this.mHandler.removeMessages(SHOW_INTERCEPT_APP_DIALOG_MSG);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(SHOW_INTERCEPT_APP_DIALOG_MSG, pkgName));
        return true;
    }

    public boolean stopServiceToken(ComponentName className, IBinder token, int startId) {
        boolean stopServiceToken;
        int i = 2147383647;
        synchronized (this) {
            if (className != null) {
                if ("com.whatsapp".equals(className.getPackageName()) && "com.whatsapp.messaging.MessageService".equals(className.getClassName()) && isPackageCloned(className.getPackageName(), UserHandle.getCallingUserId()) && "com.whatsapp".equals(getPackageNameForPid(Binder.getCallingPid()))) {
                    int i2;
                    boolean isClonedProcess = isClonedProcess(Binder.getCallingPid());
                    Map map = this.mPackagesRequireC2DMWake;
                    if (isClonedProcess) {
                        i2 = 2147383647;
                    } else {
                        i2 = SMART_TRIM_ADJ_LIMIT;
                    }
                    Set<String> set = (Set) map.get(Integer.valueOf(i2));
                    if (set == null) {
                        set = new HashSet();
                        Map map2 = this.mPackagesRequireC2DMWake;
                        if (!isClonedProcess) {
                            i = SMART_TRIM_ADJ_LIMIT;
                        }
                        map2.put(Integer.valueOf(i), set);
                    }
                    set.add(className.getPackageName());
                    Slog.i(TAG, "C2DM add package " + className.getPackageName() + ", callingPid = " + Binder.getCallingPid() + ", callingUid = " + Binder.getCallingUid() + ", isClonedProcess = " + isClonedProcess);
                }
            }
            stopServiceToken = super.stopServiceToken(className, token, startId);
        }
        return stopServiceToken;
    }

    public void serviceDoneExecuting(IBinder token, int type, int startId, int res) {
        boolean z = true;
        boolean shouldBroadcastC2DM = IS_SUPPORT_CLONE_APP;
        String str = null;
        int euid = SMART_TRIM_ADJ_LIMIT;
        synchronized (this) {
            if (token instanceof ServiceRecord) {
                ServiceRecord r = (ServiceRecord) token;
                str = r.name.getPackageName();
                if (r.userId == 0 && type == ROG_CHANGE_EVENT_INFO && "com.whatsapp".equals(str) && "com.whatsapp.messaging.MessageService".equals(r.name.getClassName()) && this.mPackagesRequireC2DMWake.get(Integer.valueOf(r.appInfo.euid)) != null && ((Set) this.mPackagesRequireC2DMWake.get(Integer.valueOf(r.appInfo.euid))).contains(str)) {
                    shouldBroadcastC2DM = true;
                    euid = r.appInfo.euid;
                }
            }
            super.serviceDoneExecuting(token, type, startId, res);
        }
        if (shouldBroadcastC2DM && this.mPackagesRequireC2DMWake.get(Integer.valueOf(euid)) != null) {
            int i;
            ((Set) this.mPackagesRequireC2DMWake.get(Integer.valueOf(euid))).remove(str);
            Intent intent = new Intent("com.google.android.c2dm.intent.RECEIVE");
            intent.setPackage(str);
            if (euid != 0) {
                i = ROG_CHANGE_EVENT_SWITCH;
            } else {
                i = SMART_TRIM_ADJ_LIMIT;
            }
            intent.addHwFlags(i | 32);
            intent.addFlags(268435456);
            this.mContext.sendOrderedBroadcast(intent, null);
            String str2 = TAG;
            StringBuilder append = new StringBuilder().append("C2DM send to package ").append(str).append(", callingPid = ").append(Binder.getCallingPid()).append(", callingUid = ").append(Binder.getCallingUid()).append(", isClonedProcess = ");
            if (euid == 0) {
                z = IS_SUPPORT_CLONE_APP;
            }
            Slog.i(str2, append.append(z).toString());
        }
    }

    public int iawareGetUidState(int uid) {
        UidRecord uidRec = (UidRecord) this.mActiveUids.get(uid);
        return uidRec == null ? -1 : uidRec.curProcState;
    }
}
