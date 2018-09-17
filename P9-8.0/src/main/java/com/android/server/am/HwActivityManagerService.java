package com.android.server.am;

import android.app.ActivityManager.RecentTaskInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityOptions;
import android.app.AppGlobals;
import android.app.ContentProviderHolder;
import android.app.HwRecentTaskInfo;
import android.app.IActivityController;
import android.app.IActivityController.Stub;
import android.app.IApplicationThread;
import android.app.INotificationManager;
import android.app.IServiceConnection;
import android.app.ITaskStackListener;
import android.app.NotificationManager;
import android.app.mtm.MultiTaskManager;
import android.app.mtm.iaware.appmng.AppCleanParam;
import android.app.mtm.iaware.appmng.AppCleanParam.AppCleanInfo;
import android.app.mtm.iaware.appmng.IAppCleanCallback;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.IIntentReceiver;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.content.res.HwPCMultiWindowCompatibility;
import android.contentsensor.IActivityObserver;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IMWThirdpartyCallback;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.TransactionTooLargeException;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.rms.HwSysResManager;
import android.rms.HwSysResource;
import android.rms.iaware.AwareConstant.ResourceType;
import android.rms.iaware.CollectData;
import android.rms.iaware.DataContract.Apps;
import android.rms.iaware.DataContract.Input;
import android.rms.iaware.DataContract.Input.Builder;
import android.rms.iaware.LogIAware;
import android.service.notification.StatusBarNotification;
import android.util.ArrayMap;
import android.util.BootTimingsTraceLog;
import android.util.Flog;
import android.util.HwPCUtils;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.WindowManagerPolicy.PointerEventListener;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import com.android.internal.app.procstats.ProcessStats.ProcessStateHolder;
import com.android.server.AlarmManagerService;
import com.android.server.LocalServices;
import com.android.server.SMCSAMSHelper;
import com.android.server.ServiceThread;
import com.android.server.am.AbsActivityManager.AppDiedInfo;
import com.android.server.am.AbsActivityManager.AppInfo;
import com.android.server.am.ActivityManagerService.GrantUri;
import com.android.server.am.ActivityStackSupervisor.ActivityDisplay;
import com.android.server.am.PendingIntentRecord.Key;
import com.android.server.emcom.SmartcareConstants;
import com.android.server.input.HwInputManagerService.HwInputManagerServiceInternal;
import com.android.server.location.HwGpsPowerTracker;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort;
import com.android.server.mtm.iaware.appmng.AwareProcessBaseInfo;
import com.android.server.mtm.iaware.appmng.appstart.AwareAppStartupPolicy;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.pm.PackageManagerService;
import com.android.server.pm.auth.HwCertification;
import com.android.server.rms.iaware.appmng.AwareDefaultConfigList;
import com.android.server.rms.iaware.appmng.AwareFakeActivityRecg;
import com.android.server.rms.iaware.cpu.CPUFeature;
import com.android.server.rms.iaware.cpu.CPUFeatureAMSCommunicator;
import com.android.server.rms.iaware.cpu.CPUKeyBackground;
import com.android.server.rms.iaware.cpu.CPUVipThread;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.srms.BroadcastFeature;
import com.android.server.rms.iaware.srms.ResourceFeature;
import com.android.server.rms.iaware.srms.SRMSDumpRadar;
import com.android.server.rms.memrepair.ProcStateStatisData;
import com.android.server.security.securityprofile.SecurityProfileInternal;
import com.android.server.security.trustspace.TrustSpaceManagerInternal;
import com.android.server.util.AbsUserBehaviourRecord;
import com.android.server.util.HwUserBehaviourRecord;
import com.android.server.wifipro.WifiProCommonUtils;
import com.android.server.wm.HwWindowManagerService;
import com.android.server.wm.WindowManagerService;
import com.huawei.android.app.IGameObserver;
import com.huawei.android.pushagentproxy.PushService;
import com.huawei.android.smcs.STProcessRecord;
import com.huawei.displayengine.IDisplayEngineService;
import com.huawei.hsm.permission.ANRFilter;
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
import java.util.Set;
import java.util.Stack;

public final class HwActivityManagerService extends ActivityManagerService {
    public static final int BACKUP_APP_ADJ = 300;
    private static final int CACHED_PROCESS_LIMIT = 8;
    static final boolean DEBUG_HWTRIM = smcsLOGV;
    static final boolean DEBUG_HWTRIM_PERFORM = smcsLOGV;
    public static final int FOREGROUND_APP_ADJ = 0;
    public static final int HEAVY_WEIGHT_APP_ADJ = 400;
    public static final int HOME_APP_ADJ = 600;
    private static final String HW_TRIM_MEMORY_ACTION = "huawei.intent.action.HW_TRIM_MEMORY_ACTION";
    public static final boolean IS_SUPPORT_CLONE_APP = SystemProperties.getBoolean("ro.config.hw_support_clone_app", false);
    private static final boolean IS_TABLET = "tablet".equals(SystemProperties.get("ro.build.characteristics", MemoryConstant.MEM_SCENE_DEFAULT));
    public static final int NATIVE_ADJ = -1000;
    public static final int PERCEPTIBLE_APP_ADJ = 200;
    private static final int PERSISTENT_MASK = 9;
    public static final int PERSISTENT_PROC_ADJ = -800;
    public static final int PERSISTENT_SERVICE_ADJ = -700;
    public static final int PREVIOUS_APP_ADJ = 700;
    private static final int QUEUE_NUM_DEFAULT = 2;
    private static final int QUEUE_NUM_IAWARE = 6;
    private static final int QUEUE_NUM_RMS = 4;
    public static final int SERVICE_ADJ = 500;
    public static final int SERVICE_B_ADJ = 800;
    static final int SET_CUSTOM_ACTIVITY_CONTROLLER_TRANSACTION = 2101;
    private static final int SMART_TRIM_ADJ_LIMIT = SystemProperties.getInt("ro.smart_trim.adj", 3);
    private static final int SMART_TRIM_BEGIN_HW_SYSM = 41;
    private static final int SMART_TRIM_POST_MSG_DELAY = 10;
    private static final int START_HW_SERVICE_POST_MSG_DELAY = 30000;
    public static final int SYSTEM_ADJ = -900;
    private static final String SYSTEM_DEBUGGABLE = "ro.debuggable";
    static final String TAG = "HwActivityManagerService";
    public static final int VISIBLE_APP_ADJ = 100;
    private static final String descriptor = "android.app.IActivityManager";
    private static boolean enableIaware = SystemProperties.getBoolean("persist.sys.enable_iaware", false);
    static final boolean enableRms = SystemProperties.getBoolean("ro.config.enable_rms", false);
    private static IBinder mAudioService = null;
    private static final boolean mIsSMCSHWSYSMEnabled = SystemProperties.getBoolean("ro.enable.hwsysm_smcs", true);
    private static HwActivityManagerService mSelf;
    private static Set<String> sAllowedCrossUserForCloneArrays = new HashSet();
    private static HashMap<String, Integer> sHardCodeAppToSetOomAdjArrays = new HashMap();
    static final boolean smcsLOGV = SystemProperties.getBoolean("ro.enable.st_debug", false);
    private boolean isLastMultiMode = false;
    final RemoteCallbackList<IActivityObserver> mActivityObservers = new RemoteCallbackList();
    private AlarmManagerService mAlms;
    private HwSysResource mAppResource;
    public HwSysResource mAppServiceResource;
    private final ArrayMap<Integer, ArrayMap<Integer, Long>> mAssocMap = new ArrayMap();
    private HwSysResource mBroadcastResource;
    private HashMap<Integer, Intent> mCurrentSplitIntent = new HashMap();
    private AbsUserBehaviourRecord mCust;
    IActivityController mCustomController = null;
    ActivityRecord mFocusedActivityForNavi = null;
    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 41:
                    HwActivityManagerService.this.hwTrimApk_HwSysM(HwActivityManagerService.this.mTrimProcName, HwActivityManagerService.this.mTrimProcUid, HwActivityManagerService.this.mTrimType);
                    return;
                default:
                    return;
            }
        }
    };
    final HwGameAssistantController mHwGameAssistantController;
    private Intent mLastSplitIntent;
    private HwSysResource mOrderedBroadcastResource;
    OverscanTimeout mOverscanTimeout = new OverscanTimeout();
    public HashMap<String, Integer> mPkgDisplayMaps = new HashMap();
    private boolean[] mScreenStatusRequest = new boolean[]{false, false};
    private SecurityProfileInternal mSecurityProfileInternal;
    private HashMap<Integer, Stack<IBinder>> mSplitActivityEntryStack;
    private Bundle mSplitExtras;
    private int mSrvFlagLocked = 0;
    private RemoteCallbackList<IMWThirdpartyCallback> mThirdPartyCallbackList;
    private String mTrimProcName = null;
    private int mTrimProcUid = -1;
    private String mTrimType = null;
    private TrustSpaceManagerInternal mTrustSpaceManagerInternal;

    static final class IawarePointerEventListener implements PointerEventListener {
        IawarePointerEventListener() {
        }

        public void onPointerEvent(MotionEvent motionEvent) {
            int action = motionEvent.getAction();
            if (action == 0 || action == 1) {
                HwSysResManager resManager = HwSysResManager.getInstance();
                if (resManager != null && resManager.isResourceNeeded(ResourceType.getReousrceId(ResourceType.RES_INPUT))) {
                    Builder builder = Input.builder();
                    if (action == 0) {
                        builder.addEvent(IDisplayEngineService.DE_ACTION_PG_BROWSER_FRONT);
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
            Global.putString(HwActivityManagerService.this.mContext.getContentResolver(), "single_hand_mode", "");
        }
    }

    class ScreenStatusReceiver extends BroadcastReceiver {
        ScreenStatusReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.stk.check_screen_idle".equals(intent.getAction())) {
                int slotId = intent.getIntExtra("slot_id", 0);
                if (slotId < 0 || slotId >= HwActivityManagerService.this.mScreenStatusRequest.length) {
                    Slog.w(HwActivityManagerService.TAG, "ScreenStatusReceiver, slotId " + slotId + " Invalid");
                    return;
                }
                HwActivityManagerService.this.mScreenStatusRequest[slotId] = intent.getBooleanExtra("SCREEN_STATUS_REQUEST", false);
                if (HwActivityManagerService.this.mScreenStatusRequest[slotId]) {
                    ActivityRecord p = HwActivityManagerService.this.getFocusedStack().topRunningActivityLocked();
                    if (p != null) {
                        Intent StkIntent = new Intent("com.huawei.intent.action.stk.idle_screen");
                        if (p.intent.hasCategory("android.intent.category.HOME")) {
                            StkIntent.putExtra("SCREEN_IDLE", true);
                        } else {
                            StkIntent.putExtra("SCREEN_IDLE", false);
                        }
                        StkIntent.putExtra("slot_id", slotId);
                        StkIntent.addFlags(HwGlobalActionsData.FLAG_SHUTDOWN);
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

    class TrimMemoryReceiver extends BroadcastReceiver {
        TrimMemoryReceiver() {
        }

        /* JADX WARNING: Missing block: B:3:0x0008, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onReceive(Context context, Intent intent) {
            if (!(intent == null || intent.getAction() == null || !HwActivityManagerService.HW_TRIM_MEMORY_ACTION.equals(intent.getAction()))) {
                HwActivityManagerService.this.trimGLMemory(80);
            }
        }
    }

    static {
        sHardCodeAppToSetOomAdjArrays.put("com.huawei.android.pushagent.PushService", Integer.valueOf(200));
        sHardCodeAppToSetOomAdjArrays.put("com.tencent.mm", Integer.valueOf(800));
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
    }

    public HwActivityManagerService(Context mContext) {
        super(mContext);
        mSelf = this;
        this.mCpusetSwitch = CPUFeature.isCpusetEnable();
        if (SystemProperties.getInt("ro.config.gameassist", 0) == 1) {
            this.mHwGameAssistantController = new HwGameAssistantController(this);
        } else {
            this.mHwGameAssistantController = null;
        }
    }

    public static HwActivityManagerService self() {
        return mSelf;
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        ComponentName _arg0;
        Bundle _arg2;
        boolean _result;
        switch (code) {
            case 502:
                data.enforceInterface(descriptor);
                boolean res = handleANRFilterFIFO(data.readInt(), data.readInt());
                reply.writeNoException();
                reply.writeInt(res ? 1 : 0);
                return true;
            case 504:
                data.enforceInterface(descriptor);
                String packageName = getPackageNameForPid(data.readInt());
                reply.writeNoException();
                reply.writeString(packageName);
                return true;
            case 506:
                data.enforceInterface(descriptor);
                int isPreloadSuccess = preloadApplication(data.readString(), data.readInt());
                reply.writeNoException();
                reply.writeInt(isPreloadSuccess);
                return true;
            case WifiProCommonUtils.RESP_CODE_UNSTABLE /*601*/:
                data.enforceInterface(descriptor);
                setIntentInfo((Intent) data.readParcelable(null), data.readInt(), data.readBundle(), data.readInt() > 0);
                reply.writeNoException();
                return true;
            case WifiProCommonUtils.RESP_CODE_GATEWAY /*602*/:
                data.enforceInterface(descriptor);
                Parcelable[] p = getIntentInfo(data.readInt(), data.readInt() > 0);
                reply.writeNoException();
                reply.writeParcelableArray(p, 0);
                return true;
            case WifiProCommonUtils.RESP_CODE_INVALID_URL /*603*/:
                data.enforceInterface(descriptor);
                addToEntryStack(data.readInt(), data.readStrongBinder(), data.readInt(), (Intent) Intent.CREATOR.createFromParcel(data));
                reply.writeNoException();
                return true;
            case WifiProCommonUtils.RESP_CODE_ABNORMAL_SERVER /*604*/:
                data.enforceInterface(descriptor);
                clearEntryStack(data.readInt(), data.readStrongBinder());
                return true;
            case WifiProCommonUtils.RESP_CODE_REDIRECTED_HOST_CHANGED /*605*/:
                data.enforceInterface(descriptor);
                removeFromEntryStack(data.readInt(), data.readStrongBinder());
                return true;
            case WifiProCommonUtils.RESP_CODE_CONN_RESET /*606*/:
                data.enforceInterface(descriptor);
                boolean isTop = isTopSplitActivity(data.readInt(), data.readStrongBinder());
                reply.writeNoException();
                reply.writeInt(isTop ? 1 : 0);
                return true;
            case SET_CUSTOM_ACTIVITY_CONTROLLER_TRANSACTION /*2101*/:
                data.enforceInterface(descriptor);
                setCustomActivityController(Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                return true;
            case 3101:
                data.enforceInterface(descriptor);
                boolean registered = registerThirdPartyCallBack(IMWThirdpartyCallback.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                reply.writeInt(registered ? 1 : 0);
                return true;
            case 3102:
                data.enforceInterface(descriptor);
                boolean unregistered = unregisterThirdPartyCallBack(IMWThirdpartyCallback.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                reply.writeInt(unregistered ? 1 : 0);
                return true;
            case 3103:
                data.enforceInterface(descriptor);
                boolean result = isInMultiWindowMode();
                reply.writeNoException();
                reply.writeInt(result ? 1 : 0);
                return true;
            case 3201:
                data.enforceInterface(descriptor);
                registerActivityObserver(IActivityObserver.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                return true;
            case 3202:
                data.enforceInterface(descriptor);
                unregisterActivityObserver(IActivityObserver.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                return true;
            case 3211:
                data.enforceInterface(descriptor);
                if (data.readInt() != 0) {
                    _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                if (data.readInt() != 0) {
                    _arg2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg2 = null;
                }
                _result = requestContentNode(_arg0, _arg2, data.readInt());
                reply.writeNoException();
                reply.writeInt(_result ? 1 : 0);
                return true;
            case 3212:
                data.enforceInterface(descriptor);
                if (data.readInt() != 0) {
                    _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                if (data.readInt() != 0) {
                    _arg2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg2 = null;
                }
                _result = requestContentOther(_arg0, _arg2, data.readInt());
                reply.writeNoException();
                reply.writeInt(_result ? 1 : 0);
                return true;
            case 3301:
                List<String> addList = new ArrayList();
                data.enforceInterface(descriptor);
                data.readStringList(addList);
                _result = addGameSpacePackageList(addList);
                reply.writeNoException();
                reply.writeInt(_result ? 1 : 0);
                return true;
            case 3302:
                List<String> delList = new ArrayList();
                data.enforceInterface(descriptor);
                data.readStringList(delList);
                _result = delGameSpacePackageList(delList);
                reply.writeNoException();
                reply.writeInt(_result ? 1 : 0);
                return true;
            case 3303:
                data.enforceInterface(descriptor);
                _result = isInGameSpace(data.readString());
                reply.writeNoException();
                reply.writeInt(_result ? 1 : 0);
                return true;
            case 3304:
                data.enforceInterface(descriptor);
                List<String> packageList = getGameList();
                reply.writeNoException();
                reply.writeStringList(packageList);
                return true;
            case 3305:
                data.enforceInterface(descriptor);
                registerGameObserver(IGameObserver.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                return true;
            case 3306:
                data.enforceInterface(descriptor);
                unregisterGameObserver(IGameObserver.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                return true;
            case 3307:
                data.enforceInterface(descriptor);
                boolean isDndOn = isGameDndOn();
                reply.writeNoException();
                reply.writeInt(isDndOn ? 1 : 0);
                return true;
            case 3308:
                data.enforceInterface(descriptor);
                boolean isKeyControlOn = isGameKeyControlOn();
                reply.writeNoException();
                reply.writeInt(isKeyControlOn ? 1 : 0);
                return true;
            case 3309:
                data.enforceInterface(descriptor);
                boolean isGestureDisabled = isGameGestureDisabled();
                reply.writeNoException();
                reply.writeInt(isGestureDisabled ? 1 : 0);
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
                    return false;
                } else {
                    if (this.mContext.checkCallingPermission("huawei.permission.HSM_SMCS") != 0) {
                        if (DEBUG_HWTRIM) {
                            Log.e(TAG, "SMCSAMSHelper.handleTransact permission deny");
                        }
                        return false;
                    } else if (SMCSAMSHelper.getInstance().handleTransact(data, reply, flags)) {
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

    void onWakefulnessChanged(int wakefulness) {
        AwareFakeActivityRecg.self().onWakefulnessChanged(wakefulness);
        super.onWakefulnessChanged(wakefulness);
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
                    HwActivityManagerService.this.mHandler.sendEmptyMessage(41);
                }
            }, 10);
            if (DEBUG_HWTRIM_PERFORM) {
                Log.v(TAG, "AMS.hwTrimApkPost_HwSysM: cost " + (System.currentTimeMillis() - timeStart) + " ms end.");
            }
        } catch (Exception e) {
            Log.e(TAG, "AMS.hwTrimApkPost_HwSysM: catch exception: " + e.toString());
        }
    }

    /* JADX WARNING: Missing block: B:40:0x013e, code:
            if (DEBUG_HWTRIM_PERFORM == false) goto L_?;
     */
    /* JADX WARNING: Missing block: B:41:0x0140, code:
            android.util.Log.v(TAG, "AMS.hwTrimApk_HwSysM: cost " + (java.lang.System.currentTimeMillis() - r16) + " ms end.");
     */
    /* JADX WARNING: Missing block: B:44:?, code:
            return;
     */
    /* JADX WARNING: Missing block: B:45:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
                        Log.v(TAG, "AMS.hwTrimApk_HwSysM: get app cost " + (System.currentTimeMillis() - timeStart) + " ms end.");
                    }
                    if (app != null && app.curAdj > SMART_TRIM_ADJ_LIMIT) {
                        if (DEBUG_HWTRIM) {
                            Log.v(TAG, "AMS.hwTrimApk_HwSysM: go to trim " + app.processName);
                        }
                        removeProcessLocked(app, false, false, "smart trim");
                        HashSet<String> pkgList = new HashSet();
                        HashSet<String> hashSet;
                        try {
                            for (Entry<String, ProcessStateHolder> key : app.pkgList.entrySet()) {
                                pkgList.add((String) key.getKey());
                            }
                            if (DEBUG_HWTRIM_PERFORM) {
                                Log.v(TAG, "AMS.hwTrimApk");
                                Log.v(TAG, "AMS.hwTrimApk_HwSysM: trim action cost " + (System.currentTimeMillis() - timeStart) + " ms end.");
                            }
                            SMCSAMSHelper.getInstance().trimProcessPostProcess(trimProc, uid, trimType, pkgList);
                            hashSet = pkgList;
                        } catch (Throwable th2) {
                            th = th2;
                            hashSet = pkgList;
                            throw th;
                        }
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

    /* JADX WARNING: Missing block: B:56:0x0132, code:
            if (DEBUG_HWTRIM_PERFORM == false) goto L_0x015d;
     */
    /* JADX WARNING: Missing block: B:57:0x0134, code:
            android.util.Log.v(TAG, "AMS.hwTrimApk_HwSysM: cost " + (java.lang.System.currentTimeMillis() - r16) + " ms end.");
     */
    /* JADX WARNING: Missing block: B:58:0x015d, code:
            r18 = r19;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
                                for (int ia = 0; ia < NA; ia++) {
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
                                    removeProcessLocked(app, false, false, "smart trim");
                                    for (Entry<String, ProcessStateHolder> key : app.pkgList.entrySet()) {
                                        pkgList.add((String) key.getKey());
                                    }
                                }
                            }
                            try {
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
        return this.mProcessList.getMemLevel(SmartcareConstants.INVALID);
    }

    /* JADX WARNING: Missing block: B:37:0x0075, code:
            r1 = r10;
     */
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
                            } else if (app.thread == null || app.crashing || (app.notResponding ^ 1) == 0 || app.curAdj < 0) {
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
                            throw th;
                        }
                    }
                    try {
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
        for (int i = 0; i < size; i++) {
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
        boolean story = false;
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
        return false;
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
        if (!shouldPreventStartProcess(app)) {
            noteProcessStart(app.info.packageName, app.processName, app.pid, app.uid, true, hostingType, hostingNameStr);
            if (HwPCUtils.isPcCastModeInServer()) {
                int displayId;
                if (this.mPkgDisplayMaps.containsKey(app.info.packageName)) {
                    displayId = ((Integer) this.mPkgDisplayMaps.get(app.info.packageName)).intValue();
                    if (HwPCUtils.isValidExtDisplayId(displayId)) {
                        app.mDisplayId = displayId;
                        if (entryPointArgs == null) {
                            entryPointArgs = new String[]{String.valueOf(displayId)};
                        }
                    }
                }
                if (HwPCUtils.enabledInPad() && entryPointArgs == null) {
                    List<InputMethodInfo> methodList = HwPCUtils.getInputMethodList();
                    if (methodList != null) {
                        String pkgName = null;
                        int listSize = methodList.size();
                        for (int i = 0; listSize > i; i++) {
                            InputMethodInfo mi = (InputMethodInfo) methodList.get(i);
                            if (mi != null) {
                                pkgName = mi.getPackageName();
                            }
                            if (pkgName != null && pkgName.equals(app.info.packageName)) {
                                entryPointArgs = new String[]{String.valueOf(HwPCUtils.getPCDisplayID())};
                                break;
                            }
                        }
                    }
                }
                if (entryPointArgs != null && entryPointArgs.length > 0) {
                    displayId = Integer.parseInt(entryPointArgs[0]);
                    if (HwPCUtils.isValidExtDisplayId(displayId)) {
                        app.mDisplayId = displayId;
                    }
                }
            }
            super.startProcessLocked(app, hostingType, hostingNameStr, abiOverride, entryPoint, entryPointArgs);
            if (isOomAdjCustomized(app)) {
                int custMaxAdj = retrieveCustedMaxAdj(app.processName);
                if (app.maxAdj > PERSISTENT_PROC_ADJ && custMaxAdj >= SYSTEM_ADJ && custMaxAdj <= 906) {
                    app.maxAdj = custMaxAdj;
                    Slog.i(TAG, "addAppLocked, app:" + app + ", set maxadj to " + custMaxAdj);
                }
            } else if (app.maxAdj > 260 && AwareAppMngSort.checkAppMngEnable()) {
                if (isAppMngOomAdjCustomized(app.info.packageName)) {
                    app.maxAdj = 260;
                }
            }
            notifyProcessStatusChange(app.info.packageName, app.processName, hostingType, app.pid, app.uid);
        }
    }

    protected void startPushService() {
        File jarFile = new File("/system/framework/hwpush.jar");
        File custFile = HwCfgFilePolicy.getCfgFile("jars/hwpush.jar", 0);
        if ((jarFile != null && jarFile.exists()) || (custFile != null && custFile.exists())) {
            Slog.d(TAG, "start push service");
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    Intent serviceIntent = new Intent(HwActivityManagerService.this.mContext, PushService.class);
                    serviceIntent.putExtra("startFlag", "1");
                    HwActivityManagerService.this.mContext.startService(serviceIntent);
                }
            }, 30000);
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

    public void notifyAppEventToIaware(int type, String packageName) {
        CPUFeatureAMSCommunicator.getInstance().setTopAppToBoost(type, packageName);
    }

    public boolean serviceIsRunning(ComponentName serviceCmpName, int curUser) {
        boolean z;
        synchronized (this) {
            Slog.d(TAG, "serviceIsRunning, for user " + curUser + ", serviceCmpName " + serviceCmpName);
            z = this.mServices.getServicesLocked(curUser).get(serviceCmpName) != null;
        }
        return z;
    }

    void setDeviceProvisioned() {
        ContentResolver cr = this.mContext.getContentResolver();
        if ((Global.getInt(cr, "device_provisioned", 0) == 0 || Secure.getInt(cr, "user_setup_complete", 0) == 0) && ((PackageManagerService) ServiceManager.getService("package")).isSetupDisabled()) {
            Global.putInt(cr, "device_provisioned", 1);
            Secure.putInt(cr, "user_setup_complete", 1);
        }
    }

    public void systemReady(Runnable goingCallback, BootTimingsTraceLog traceLog) {
        if (!this.mSystemReady) {
            setDeviceProvisioned();
        }
        super.systemReady(goingCallback, traceLog);
        initTrustSpace();
        initSecurityProfile();
        this.mContext.registerReceiver(new ScreenStatusReceiver(), new IntentFilter("android.intent.action.stk.check_screen_idle"), "com.huawei.permission.STK_CHECK_SCREEN_IDLE", null);
        this.mContext.registerReceiver(new TrimMemoryReceiver(), new IntentFilter(HW_TRIM_MEMORY_ACTION));
        this.mThirdPartyCallbackList = new RemoteCallbackList();
    }

    public ArrayList<Integer> getIawareDumpData() {
        ArrayList<Integer> queueSizes = new ArrayList();
        for (BroadcastQueue queue : this.mBroadcastQueues) {
            ArrayList<Integer> queueSizesTemp = queue.getIawareDumpData();
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
        if (type >= 1 && type <= 2) {
            return ResourceFeature.getIawareResourceFeature(type);
        }
        if (type < 10 || type > 11) {
            return false;
        }
        return BroadcastFeature.isFeatureEnabled(type);
    }

    public long proxyBroadcast(List<String> pkgs, boolean proxy) {
        long delay;
        synchronized (this) {
            delay = 0;
            for (BroadcastQueue queue : this.mBroadcastQueues) {
                long temp = queue.proxyBroadcast(pkgs, proxy);
                if (temp > delay) {
                    delay = temp;
                }
            }
        }
        return delay;
    }

    public long proxyBroadcastByPid(List<Integer> pids, boolean proxy) {
        long delay;
        synchronized (this) {
            delay = 0;
            for (BroadcastQueue queue : this.mBroadcastQueues) {
                long temp = queue.proxyBroadcastByPid(pids, proxy);
                if (temp > delay) {
                    delay = temp;
                }
            }
        }
        return delay;
    }

    public void setProxyBCActions(List<String> actions) {
        synchronized (this) {
            for (BroadcastQueue queue : this.mBroadcastQueues) {
                queue.setProxyBCActions(actions);
            }
        }
    }

    public void setActionExcludePkg(String action, String pkg) {
        synchronized (this) {
            for (BroadcastQueue queue : this.mBroadcastQueues) {
                queue.setActionExcludePkg(action, pkg);
            }
        }
    }

    public void proxyBCConfig(int type, String key, List<String> value) {
        synchronized (this) {
            for (BroadcastQueue queue : this.mBroadcastQueues) {
                queue.proxyBCConfig(type, key, value);
            }
        }
    }

    public void checkIfScreenStatusRequestAndSendBroadcast() {
        for (int slotId = 0; slotId < this.mScreenStatusRequest.length; slotId++) {
            if (this.mScreenStatusRequest[slotId]) {
                Intent StkIntent = new Intent("com.huawei.intent.action.stk.idle_screen");
                StkIntent.addFlags(HwGlobalActionsData.FLAG_SHUTDOWN);
                StkIntent.putExtra("SCREEN_IDLE", true);
                StkIntent.putExtra("slot_id", slotId);
                this.mContext.sendBroadcast(StkIntent, "com.huawei.permission.CAT_IDLE_SCREEN");
            }
        }
    }

    public void setServiceFlagLocked(int servFlag) {
        this.mSrvFlagLocked = servFlag;
    }

    public boolean isProcessExistLocked(String processName, int uid) {
        return getProcessRecordLocked(processName, uid, true) != null;
    }

    public ProcessRecord getProcessRecordLocked(int pid) {
        ProcessRecord proc = null;
        if (pid >= 0) {
            synchronized (this.mPidsSelfLocked) {
                proc = (ProcessRecord) this.mPidsSelfLocked.get(pid);
            }
        }
        return proc;
    }

    public boolean shouldPreventSendReceiver(Intent intent, ResolveInfo resolveInfo, int callerPid, int callerUid, ProcessRecord targetApp, ProcessRecord callerApp) {
        AwareAppStartupPolicy appStartupPolicy = AwareAppStartupPolicy.self();
        if (appStartupPolicy != null) {
            return appStartupPolicy.shouldPreventSendReceiver(intent, resolveInfo, callerPid, callerUid, targetApp, callerApp);
        }
        return false;
    }

    public boolean shouldPreventStartService(ServiceInfo servInfo, int callerPid, int callerUid, ProcessRecord callerApp, boolean servExist, Intent service) {
        AwareAppStartupPolicy appStartupPolicy = AwareAppStartupPolicy.self();
        if (appStartupPolicy == null) {
            return false;
        }
        return appStartupPolicy.shouldPreventStartService(servInfo, callerPid, callerUid, callerApp, this.mSrvFlagLocked, servExist, service);
    }

    public boolean shouldPreventActivity(Intent intent, ActivityInfo aInfo, ActivityRecord record, int callerPid, int callerUid, ProcessRecord callerApp) {
        if (intent == null || aInfo == null || record == null) {
            return false;
        }
        if (isSleepingLocked() && ("com.ss.android.article.news/com.ss.android.message.sswo.SswoActivity".equals(record.shortComponentName) || "dongzheng.szkingdom.android.phone/com.dgzq.IM.ui.activity.KeepAliveActivity".equals(record.shortComponentName))) {
            return true;
        }
        AwareAppStartupPolicy appStartupPolicy = AwareAppStartupPolicy.self();
        if (appStartupPolicy == null) {
            return false;
        }
        boolean shouldPrevent = appStartupPolicy.shouldPreventStartActivity(intent, aInfo, record, callerPid, callerUid, callerApp);
        if (!shouldPrevent) {
            shouldPrevent = AwareFakeActivityRecg.self().shouldPreventStartActivity(aInfo, callerPid, callerUid, this.mBatteryStatsService.getActiveStatistics().isScreenOn());
        }
        return shouldPrevent;
    }

    public boolean shouldPreventRestartService(ServiceInfo sInfo, boolean realStart) {
        AwareAppStartupPolicy appStartupPolicy = AwareAppStartupPolicy.self();
        if (appStartupPolicy != null) {
            return appStartupPolicy.shouldPreventRestartService(sInfo, realStart);
        }
        return false;
    }

    public void recognizeFakeActivity(String compName, int pid, int uid) {
        AwareFakeActivityRecg.self().recognizeFakeActivity(compName, this.mBatteryStatsService.getActiveStatistics().isScreenOn(), pid, uid);
    }

    private void initTrustSpace() {
        this.mTrustSpaceManagerInternal = (TrustSpaceManagerInternal) LocalServices.getService(TrustSpaceManagerInternal.class);
        if (this.mTrustSpaceManagerInternal == null) {
            Slog.e(TAG, "TrustSpaceManagerInternal not find !");
        } else {
            this.mTrustSpaceManagerInternal.initTrustSpace();
        }
    }

    private void initSecurityProfile() {
        this.mSecurityProfileInternal = (SecurityProfileInternal) LocalServices.getService(SecurityProfileInternal.class);
        if (this.mSecurityProfileInternal == null) {
            Slog.e(TAG, "SecurityProfileInternal not find !");
        }
    }

    private boolean shouldPreventStartComponent(int type, String calleePackage, int callerUid, int callerPid, String callerPackage, int userId) {
        boolean shouldPrevent = false;
        if (this.mSystemReady) {
            long ident = Binder.clearCallingIdentity();
            try {
                if (this.mTrustSpaceManagerInternal != null) {
                    shouldPrevent = this.mTrustSpaceManagerInternal.checkIntent(type, calleePackage, callerUid, callerPid, callerPackage, userId);
                }
                if (!(shouldPrevent || this.mSecurityProfileInternal == null)) {
                    shouldPrevent |= this.mSecurityProfileInternal.shouldPreventInteraction(type, calleePackage, callerUid, callerPid, callerPackage, userId);
                }
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
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

    protected void exitSingleHandMode() {
        this.mHandler.removeCallbacks(this.mOverscanTimeout);
        this.mHandler.postDelayed(this.mOverscanTimeout, 200);
    }

    public boolean shouldPreventStartProvider(ProviderInfo cpi, int callerPid, int callerUid, ProcessRecord callerApp) {
        AwareAppStartupPolicy appStartupPolicy = AwareAppStartupPolicy.self();
        if (appStartupPolicy != null) {
            return appStartupPolicy.shouldPreventStartProvider(cpi, callerPid, callerUid, callerApp);
        }
        return false;
    }

    protected void setCustomActivityController(IActivityController controller) {
        enforceCallingPermission("android.permission.SET_ACTIVITY_WATCHER", "setCustomActivityController()");
        synchronized (this) {
            this.mCustomController = controller;
        }
        HwInputManagerServiceInternal inputManager = (HwInputManagerServiceInternal) LocalServices.getService(HwInputManagerServiceInternal.class);
        if (inputManager != null) {
            inputManager.setCustomActivityController(controller);
        }
    }

    public void setRequestedOrientation(IBinder token, int requestedOrientation) {
        super.setRequestedOrientation(token, requestedOrientation);
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this) {
                if (requestedOrientation == 0 || requestedOrientation == 6 || requestedOrientation == 8 || requestedOrientation == 11) {
                    Global.putString(this.mContext.getContentResolver(), "single_hand_mode", "");
                }
            }
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    protected boolean customActivityStarting(Intent intent, String packageName) {
        if (this.mCustomController != null) {
            boolean startOK = true;
            try {
                startOK = this.mCustomController.activityStarting(intent.cloneFilter(), packageName);
            } catch (RemoteException e) {
                this.mCustomController = null;
                HwInputManagerServiceInternal inputManager = (HwInputManagerServiceInternal) LocalServices.getService(HwInputManagerServiceInternal.class);
                if (inputManager != null) {
                    inputManager.setCustomActivityController(null);
                }
            }
            if (!startOK) {
                Slog.i(TAG, "Not starting activity because custom controller stop it");
                return true;
            }
        }
        return false;
    }

    protected boolean customActivityResuming(String packageName) {
        if (this.mSecurityProfileInternal != null) {
            this.mSecurityProfileInternal.handleActivityResuming(packageName);
        }
        if (this.mCustomController != null) {
            boolean resumeOK = true;
            try {
                resumeOK = this.mCustomController.activityResuming(packageName);
            } catch (RemoteException e) {
                this.mCustomController = null;
                HwInputManagerServiceInternal inputManager = (HwInputManagerServiceInternal) LocalServices.getService(HwInputManagerServiceInternal.class);
                if (inputManager != null) {
                    inputManager.setCustomActivityController(null);
                }
            }
            if (!resumeOK) {
                Slog.i(TAG, "Not resuming activity because custom controller stop it");
                return true;
            }
        }
        return false;
    }

    protected BroadcastQueue[] initialBroadcastQueue() {
        int queueNum;
        if (enableIaware) {
            queueNum = 6;
        } else if (enableRms) {
            queueNum = 4;
        } else {
            queueNum = 2;
        }
        return new BroadcastQueue[queueNum];
    }

    protected void setThirdPartyAppBroadcastQueue(BroadcastQueue[] broadcastQueues) {
        if (enableRms || enableIaware) {
            ServiceThread thirdAppHandlerThread = new ServiceThread("ThirdAppHandlerThread", 10, false);
            thirdAppHandlerThread.start();
            Handler thirdAppHandler = new Handler(thirdAppHandlerThread.getLooper());
            this.mFgThirdAppBroadcastQueue = new HwBroadcastQueue(this, thirdAppHandler, "fgthirdapp", (long) BROADCAST_FG_TIMEOUT, false);
            this.mBgThirdAppBroadcastQueue = new HwBroadcastQueue(this, thirdAppHandler, "bgthirdapp", (long) BROADCAST_BG_TIMEOUT, false);
            broadcastQueues[2] = this.mFgThirdAppBroadcastQueue;
            broadcastQueues[3] = this.mBgThirdAppBroadcastQueue;
        }
    }

    protected void setKeyAppBroadcastQueue(BroadcastQueue[] broadcastQueues) {
        if (enableIaware) {
            ServiceThread keyAppHandlerThread = new ServiceThread("keyAppHanderThread", 0, false);
            keyAppHandlerThread.start();
            Handler keyAppHandler = new Handler(keyAppHandlerThread.getLooper());
            this.mFgKeyAppBroadcastQueue = new HwBroadcastQueue(this, keyAppHandler, "fgkeyapp", (long) BROADCAST_FG_TIMEOUT, false);
            this.mBgKeyAppBroadcastQueue = new HwBroadcastQueue(this, keyAppHandler, "bgkeyapp", (long) BROADCAST_BG_TIMEOUT, false);
            broadcastQueues[4] = this.mFgKeyAppBroadcastQueue;
            broadcastQueues[5] = this.mBgKeyAppBroadcastQueue;
        }
    }

    protected boolean isThirdPartyAppBroadcastQueue(ProcessRecord callerApp) {
        boolean z = true;
        if ((!enableRms && !getIawareResourceFeature(1)) || callerApp == null) {
            return false;
        }
        if (DEBUG_HWTRIM || Log.HWINFO) {
            Log.i(TAG, "Split enqueueing broadcast [callerApp]:" + callerApp);
        }
        if (callerApp.instr != null) {
            return false;
        }
        if ((callerApp.info.flags & 1) != 0 && (callerApp.info.hwFlags & HwGlobalActionsData.FLAG_SHUTDOWN_CONFIRM) == 0) {
            z = false;
        }
        return z;
    }

    protected boolean isKeyAppBroadcastQueue(int type, String name) {
        return getIawareResourceFeature(1) && name != null && isKeyApp(type, 0, name);
    }

    protected boolean isThirdPartyAppPendingBroadcastProcessLocked(int pid) {
        boolean z = true;
        if (!enableRms && !getIawareResourceFeature(1)) {
            return false;
        }
        if (!this.mFgThirdAppBroadcastQueue.isPendingBroadcastProcessLocked(pid)) {
            z = this.mBgThirdAppBroadcastQueue.isPendingBroadcastProcessLocked(pid);
        }
        return z;
    }

    protected boolean isKeyAppPendingBroadcastProcessLocked(int pid) {
        boolean z = true;
        if (!getIawareResourceFeature(1) || this.mFgKeyAppBroadcastQueue == null || this.mBgKeyAppBroadcastQueue == null) {
            return false;
        }
        if (!this.mFgKeyAppBroadcastQueue.isPendingBroadcastProcessLocked(pid)) {
            z = this.mBgKeyAppBroadcastQueue.isPendingBroadcastProcessLocked(pid);
        }
        return z;
    }

    protected BroadcastQueue thirdPartyAppBroadcastQueueForIntent(Intent intent) {
        boolean isFg = (intent.getFlags() & 268435456) != 0;
        if (DEBUG_HWTRIM || Log.HWINFO) {
            Log.i(TAG, "thirdAppBroadcastQueueForIntent intent " + intent + " on " + (isFg ? "fgthirdapp" : "bgthirdapp") + " queue");
        }
        if (isFg) {
            return this.mFgThirdAppBroadcastQueue;
        }
        return this.mBgThirdAppBroadcastQueue;
    }

    protected BroadcastQueue keyAppBroadcastQueueForIntent(Intent intent) {
        boolean isFg = (intent.getFlags() & 268435456) != 0;
        if (DEBUG_HWTRIM || Log.HWINFO) {
            Log.i(TAG, "keyAppBroadcastQueueForIntent intent " + intent + " on " + (isFg ? "fgkeyapp" : "bgkeyapp") + " queue");
        }
        if (isFg) {
            updateSRMSStatisticsData(0);
        } else {
            updateSRMSStatisticsData(1);
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
        if (getIawareResourceFeature(2)) {
            if (this.mOrderedBroadcastResource == null) {
                if (DEBUG_HWTRIM || Log.HWINFO) {
                    Log.d(TAG, "init OrderedBroadcastResource");
                }
                this.mOrderedBroadcastResource = HwFrameworkFactory.getHwResource(31);
            }
            if (!(this.mOrderedBroadcastResource == null || (isInToOut ^ 1) == 0)) {
                this.mOrderedBroadcastResource.acquire(0, actionOrPkg, 0);
            }
        }
    }

    protected void checkBroadcastRecordSpeed(int callingUid, String callerPackage, ProcessRecord callerApp) {
        if (this.mBroadcastResource != null && callerApp != null) {
            int uid = callingUid;
            String pkg = callerPackage;
            int processType = getProcessType(callerApp);
            if (("1".equals(SystemProperties.get(SYSTEM_DEBUGGABLE, "0")) || processType == 0) && 2 == this.mBroadcastResource.acquire(callingUid, callerPackage, processType) && ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                Log.i(TAG, "This App send broadcast speed is overload! uid = " + callingUid);
            }
        }
    }

    protected void clearBroadcastResource(ProcessRecord app) {
        if (this.mBroadcastResource != null && app != null) {
            int uid = app.info.uid;
            String pkg = app.info.packageName;
            int processType = getProcessType(app);
            if ("1".equals(SystemProperties.get(SYSTEM_DEBUGGABLE, "0")) || processType == 0) {
                this.mBroadcastResource.clear(uid, pkg, processType);
            }
        }
    }

    private int getProcessType(ProcessRecord app) {
        if ((app.info.flags & 1) != 0) {
            return 2;
        }
        return 0;
    }

    public boolean isKeyApp(int type, int value, String key) {
        if (this.mBroadcastResource == null || key == null || 1 != this.mBroadcastResource.queryPkgPolicy(type, value, key)) {
            return false;
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
            this.mAppResource = HwFrameworkFactory.getHwResource(18);
        }
    }

    private void initAppServiceResourceLocked() {
        if (this.mAppServiceResource == null) {
            Log.i(TAG, "init AppServiceResource");
            this.mAppServiceResource = HwFrameworkFactory.getHwResource(17);
        }
    }

    public void initAppAndAppServiceResourceLocked() {
        initAppResourceLocked();
        initAppServiceResourceLocked();
    }

    public boolean isAcquireAppServiceResourceLocked(ServiceRecord sr, ProcessRecord app) {
        if (this.mAppServiceResource == null || sr == null || sr.appInfo.uid <= 0 || sr.appInfo.packageName == null || sr.serviceInfo.name == null || 2 != this.mAppServiceResource.acquire(sr.appInfo.uid, sr.appInfo.packageName, getProcessType(app))) {
            return true;
        }
        Log.i(TAG, "Failed to acquire AppServiceResource:" + sr.serviceInfo.name + " of " + sr.appInfo.packageName + "/" + sr.appInfo.uid);
        return false;
    }

    public boolean isAcquireAppResourceLocked(ProcessRecord app) {
        if (!(this.mAppResource == null || app == null || app.uid <= 0 || app.info == null || app.processName == null || app.startTime <= 0)) {
            int processType = ((app.info.flags & 1) != 0 && (app.info.hwFlags & HwGlobalActionsData.FLAG_SHUTDOWN_CONFIRM) == 0 && (app.info.hwFlags & 67108864) == 0) ? 2 : 0;
            Bundle args = new Bundle();
            args.putInt("callingUid", app.uid);
            args.putString(HwGpsPowerTracker.DEL_PKG, app.processName);
            args.putLong("startTime", app.startTime);
            args.putInt("processType", processType);
            args.putBoolean("launchfromActivity", app.launchfromActivity);
            args.putBoolean("topProcess", isTopProcessLocked(app));
            if (2 == this.mAppResource.acquire(null, null, args)) {
                Log.i(TAG, "Failed to acquire AppResource:" + app.info.packageName + "/" + app.uid);
                return false;
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
            int processType = ((app.info.flags & 1) != 0 && (app.info.hwFlags & HwGlobalActionsData.FLAG_SHUTDOWN_CONFIRM) == 0 && (app.info.hwFlags & 67108864) == 0) ? 2 : 0;
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
        int i = 1;
        IBinder b = getAudioService();
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        int _result = 0;
        try {
            _data.writeInterfaceToken("android.media.IAudioService");
            _data.writeInt(restore ? 1 : 0);
            _data.writeString(packageName);
            if (!isOnTop) {
                i = 0;
            }
            _data.writeInt(i);
            _data.writeString(reserved);
            b.transact(1002, _data, _reply, 0);
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

    public void trimGLMemory(int level) {
        Slog.i(TAG, " trimGLMemory begin ");
        synchronized (this.mLruProcesses) {
            int list_size = this.mLruProcesses.size();
            for (int i = 0; i < list_size; i++) {
                ProcessRecord app = (ProcessRecord) this.mLruProcesses.get(i);
                if (app.thread != null) {
                    try {
                        app.thread.scheduleTrimMemory(level);
                    } catch (RemoteException e) {
                    }
                }
            }
        }
        Slog.i(TAG, " trimGLMemory end ");
    }

    public void setWindowManager(WindowManagerService wm) {
        super.setWindowManager(wm);
        wm.registerPointerEventListener(new IawarePointerEventListener());
    }

    /* JADX WARNING: Missing block: B:3:0x0008, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void noteActivityStart(AppInfo appInfo, boolean started) {
        HwSysResManager resManager = HwSysResManager.getInstance();
        if (resManager != null && appInfo != null && resManager.isResourceNeeded(ResourceType.getReousrceId(ResourceType.RES_APP)) && this.mSystemReady) {
            int event;
            Apps.Builder builder = Apps.builder();
            if (started) {
                event = 15005;
            } else {
                event = 85005;
            }
            builder.addEvent(event);
            builder.addCalledApp(appInfo.packageName, appInfo.processName, appInfo.activityName, appInfo.pid, appInfo.uid);
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
            return false;
        }
        synchronized (this) {
            synchronized (this.mPidsSelfLocked) {
                if (procInfo.mPid == MY_PID || procInfo.mPid < 0) {
                    Slog.e(TAG, "getProcessRecordFromMTM it is failed to get process record ,mPid :" + procInfo.mPid);
                    return false;
                }
                ProcessRecord proc = (ProcessRecord) this.mPidsSelfLocked.get(procInfo.mPid);
                if (proc == null) {
                    Slog.e(TAG, "getProcessRecordFromMTM process info is null ,mUid :" + procInfo.mPid);
                    return false;
                }
                boolean z;
                if (procInfo.mType == 0) {
                    procInfo.mType = getAppType(procInfo.mPid, proc.info);
                }
                procInfo.mProcessName = proc.processName;
                procInfo.mCurSchedGroup = proc.curSchedGroup;
                procInfo.mCurAdj = proc.curAdj;
                procInfo.mAdjType = proc.adjType;
                procInfo.mAppUid = proc.info.uid;
                procInfo.mForegroundActivities = proc.foregroundActivities;
                procInfo.mForegroundServices = proc.foregroundServices;
                if (proc.forcingToImportant != null) {
                    z = true;
                } else {
                    z = false;
                }
                procInfo.mForceToForeground = z;
                if (procInfo.mPackageName.size() == 0) {
                    int list_size = proc.pkgList.size();
                    for (int i = 0; i < list_size; i++) {
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

    public Map<Integer, AwareProcessBaseInfo> getAllProcessBaseInfo() {
        ArrayMap<Integer, AwareProcessBaseInfo> list;
        synchronized (this.mPidsSelfLocked) {
            int size = this.mPidsSelfLocked.size();
            list = new ArrayMap(size);
            for (int i = 0; i < size; i++) {
                ProcessRecord p = (ProcessRecord) this.mPidsSelfLocked.valueAt(i);
                AwareProcessBaseInfo baseInfo = new AwareProcessBaseInfo();
                baseInfo.mCurAdj = p.curAdj;
                baseInfo.mForegroundActivities = p.foregroundActivities;
                baseInfo.mAdjType = p.adjType;
                baseInfo.mHasShownUi = p.hasShownUi;
                baseInfo.mUid = p.uid;
                baseInfo.mAppUid = p.info.uid;
                list.put(Integer.valueOf(p.pid), baseInfo);
            }
        }
        return list;
    }

    /* JADX WARNING: Missing block: B:10:0x002e, code:
            return r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public AwareProcessBaseInfo getProcessBaseInfo(int pid) {
        Throwable th;
        synchronized (this.mPidsSelfLocked) {
            try {
                AwareProcessBaseInfo baseInfo = new AwareProcessBaseInfo();
                try {
                    baseInfo.mCurAdj = 1001;
                    ProcessRecord p = (ProcessRecord) this.mPidsSelfLocked.get(pid);
                    if (p != null) {
                        baseInfo.mForegroundActivities = p.foregroundActivities;
                        baseInfo.mUid = p.uid;
                        baseInfo.mAppUid = p.info.uid;
                        baseInfo.mCurAdj = p.curAdj;
                        baseInfo.mAdjType = p.adjType;
                    }
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

    public boolean killProcessRecordFromMTM(ProcessInfo procInfo, boolean restartservice, String reason) {
        if (procInfo.mPid == MY_PID || procInfo.mPid < 0) {
            Slog.e(TAG, "killProcessRecordFromMTM it is failed to get process record ,mUid :" + procInfo.mUid);
            return false;
        }
        synchronized (this.mPidsSelfLocked) {
            int adj = procInfo.mCurAdj;
            ProcessRecord proc = (ProcessRecord) this.mPidsSelfLocked.get(procInfo.mPid);
            if (proc == null) {
                Slog.e(TAG, "killProcessRecordFromMTM this process has been killed or died before  :" + procInfo.mProcessName);
                return false;
            }
            synchronized (this) {
                removeProcessLocked(proc, false, restartservice, "iAwareK[" + reason + "](adj:" + adj + ",type:" + proc.adjType + ")");
            }
            return true;
        }
    }

    private int getAppType(int pid, ApplicationInfo info) {
        if (info == null) {
            Slog.e(TAG, "getAppType app info is null");
            return 0;
        }
        int flags = info.flags;
        try {
            int hwFlags = ((Integer) Class.forName("android.content.pm.ApplicationInfo").getField("hwFlags").get(info)).intValue();
            if (!((flags & 1) == 0 || (100663296 & hwFlags) == 0)) {
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
            return 1;
        }
        if ((flags & 1) != 0) {
            return 2;
        }
        return 4;
    }

    public ArrayList getAMSLru() {
        return this.mLruProcesses;
    }

    public int getAMSLruBypid(int pid) {
        synchronized (this) {
            int size = this.mLruProcesses.size();
            for (int i = 0; i < size; i++) {
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
            for (int i = 0; i < size; i++) {
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

    private void notifyProcessStatusChange(String pkg, String process, String hostingType, int pid, int uid) {
        MultiTaskManager handler = MultiTaskManager.getInstance();
        if (handler != null) {
            handler.notifyProcessStatusChange(pkg, process, hostingType, pid, uid);
        }
    }

    protected void notifyProcessDied(int pid, int uid) {
        MultiTaskManager handler = MultiTaskManager.getInstance();
        if (handler != null) {
            handler.notifyProcessDiedChange(pid, uid);
        }
    }

    protected void notifyProcessGroupChange(int pid, int uid, int grp) {
        CPUKeyBackground.getInstance().notifyProcessGroupChange(pid, uid, grp);
    }

    /* JADX WARNING: Missing block: B:26:0x0060, code:
            return r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean hasDeps(ProcessInfo procInfo, String packageName) {
        if (packageName == null || procInfo == null) {
            Slog.e(TAG, "hasDeps packageName == null || procInfo == null");
            return false;
        }
        synchronized (this.mPidsSelfLocked) {
            if (procInfo.mPid == MY_PID || procInfo.mPid < 0) {
                Slog.e(TAG, "getProcessRecordFromMTM it is failed to get process record ,mPid :" + procInfo.mPid);
                return false;
            }
            ProcessRecord proc = (ProcessRecord) this.mPidsSelfLocked.get(procInfo.mPid);
            if (proc == null) {
                Slog.e(TAG, "hasDeps proc == null");
                return false;
            }
            boolean result = proc.pkgDeps != null ? proc.pkgDeps.contains(packageName) : false;
        }
    }

    /* JADX WARNING: Missing block: B:2:0x0004, code:
            return;
     */
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

    /* JADX WARNING: Missing block: B:2:0x0004, code:
            return;
     */
    /* JADX WARNING: Missing block: B:7:0x0011, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void reportServiceRelationIAware(int relationType, ContentProviderRecord r, ProcessRecord caller) {
        if (caller != null && r != null && r.info != null && r.name != null && caller.uid != r.uid) {
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

    protected void reportPreviousInfo(int relationType, ProcessRecord prevProc) {
        if (prevProc != null) {
            HwSysResManager resManager = HwSysResManager.getInstance();
            if (resManager != null && resManager.isResourceNeeded(ResourceType.getReousrceId(ResourceType.RESOURCE_APPASSOC))) {
                int prevPid = prevProc.pid;
                int prevUid = prevProc.uid;
                Bundle bundleArgs = new Bundle();
                bundleArgs.putInt("pid", prevPid);
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
                        for (int i = 0; i < size; i++) {
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
                        args.putStringArrayList(MemoryConstant.MEM_PREREAD_ITEM_NAME, pkgs);
                        args.putInt("relationType", 4);
                        HwSysResManager.getInstance().reportData(new CollectData(ResourceType.getReousrceId(ResourceType.RESOURCE_APPASSOC), System.currentTimeMillis(), args));
                        for (ConnectionRecord cr : proc.connections) {
                            if (!(cr == null || cr.binding == null)) {
                                reportServiceRelationIAware(1, cr.binding.service, proc);
                            }
                        }
                        for (ContentProviderConnection cpc : proc.conProviders) {
                            if (cpc != null) {
                                reportServiceRelationIAware(2, cpc.provider, proc);
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
            int pid = 0;
            int uid = 0;
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
            bundleArgs.putInt("pid", pid);
            bundleArgs.putInt("tgtUid", uid);
            bundleArgs.putStringArrayList(MemoryConstant.MEM_PREREAD_ITEM_NAME, pkgs);
            bundleArgs.putInt("relationType", 11);
            CollectData data = new CollectData(ResourceType.getReousrceId(ResourceType.RESOURCE_APPASSOC), System.currentTimeMillis(), bundleArgs);
            long origId = Binder.clearCallingIdentity();
            HwSysResManager.getInstance().reportData(data);
            Binder.restoreCallingIdentity(origId);
        }
    }

    public void setPackageStoppedState(List<String> packageList, boolean stopped, int targetUid) {
        if (packageList != null) {
            int userId = UserHandle.getUserId(targetUid);
            IPackageManager pm = AppGlobals.getPackageManager();
            try {
                synchronized (this) {
                    for (String packageName : packageList) {
                        pm.setPackageStoppedState(packageName, stopped, userId);
                    }
                }
            } catch (RemoteException e) {
                Slog.w(TAG, "Failed trying to unstop package " + packageList.toString() + ": " + e);
            } catch (IllegalArgumentException e2) {
                Slog.w(TAG, "Failed trying to unstop package " + packageList.toString() + ": " + e2);
            }
        }
    }

    public boolean killProcessRecordFromIAware(ProcessInfo procInfo, boolean restartservice, boolean isAsynchronous, String reason) {
        return killProcessRecordFromIAwareInternal(procInfo, restartservice, isAsynchronous, reason, false);
    }

    public boolean killProcessRecordFromIAwareNative(ProcessInfo procInfo, boolean restartservice, boolean isAsynchronous, String reason) {
        return killProcessRecordFromIAwareInternal(procInfo, restartservice, isAsynchronous, reason, true);
    }

    protected final boolean cleanUpApplicationRecordLocked(ProcessRecord app, boolean restarting, boolean allowRestart, int index, boolean replacingPid) {
        if (IS_TABLET) {
            if (this.mSplitActivityEntryStack != null && this.mSplitActivityEntryStack.containsKey(Integer.valueOf(app.pid))) {
                Slog.w(TAG, "Split main entrance killed, clear sub activities for " + app.info.packageName + ", pid " + app.pid);
                clearEntryStack(app.pid, null);
                this.mSplitActivityEntryStack.remove(Integer.valueOf(app.pid));
            }
            if (this.mCurrentSplitIntent != null && this.mCurrentSplitIntent.containsKey(Integer.valueOf(app.pid))) {
                this.mCurrentSplitIntent.remove(Integer.valueOf(app.pid));
            }
        }
        return super.cleanUpApplicationRecordLocked(app, restarting, allowRestart, index, replacingPid);
    }

    /* JADX WARNING: Missing block: B:24:0x00aa, code:
            monitor-enter(r10);
     */
    /* JADX WARNING: Missing block: B:27:?, code:
            cleanupAppInLaunchingProvidersLocked(r4, true);
     */
    /* JADX WARNING: Missing block: B:28:0x00af, code:
            if (r13 == false) goto L_0x00b7;
     */
    /* JADX WARNING: Missing block: B:29:0x00b1, code:
            r4.killedByAm = true;
            r4.killed = true;
     */
    /* JADX WARNING: Missing block: B:30:0x00b7, code:
            r4.unlinkDeathRecipient();
            r1 = "iAwareF[" + r14 + "](" + r4.adjType + ")";
            removeProcessLocked(r4, false, r12, r1);
     */
    /* JADX WARNING: Missing block: B:31:0x00e6, code:
            if (r13 == false) goto L_0x014a;
     */
    /* JADX WARNING: Missing block: B:32:0x00e8, code:
            com.android.server.rms.iaware.memory.utils.MemoryUtils.killProcessGroupForQuickKill(r4.info.uid, r11.mPid);
            android.util.Slog.i(TAG, "Killing " + r3 + " (adj " + r4.curAdj + "): " + r1);
            android.util.EventLog.writeEvent(30023, new java.lang.Object[]{java.lang.Integer.valueOf(r2), java.lang.Integer.valueOf(r11.mPid), r3, java.lang.Integer.valueOf(r4.curAdj), r1});
     */
    /* JADX WARNING: Missing block: B:33:0x014a, code:
            cleanupBroadcastLocked(r4);
            cleanupAlarmLockedExt(r4);
     */
    /* JADX WARNING: Missing block: B:34:0x0150, code:
            monitor-exit(r10);
     */
    /* JADX WARNING: Missing block: B:35:0x0151, code:
            reportAppDiedMsg(r2, r3, r14);
     */
    /* JADX WARNING: Missing block: B:36:0x0154, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean killProcessRecordFromIAwareInternal(ProcessInfo procInfo, boolean restartservice, boolean isAsynchronous, String reason, boolean isNative) {
        synchronized (this.mPidsSelfLocked) {
            if (procInfo.mPid == MY_PID || procInfo.mPid < 0) {
                Slog.e(TAG, "killProcessRecordFromIAware it is failed to get process record ,mUid :" + procInfo.mUid);
                return false;
            }
            ProcessRecord proc = (ProcessRecord) this.mPidsSelfLocked.get(procInfo.mPid);
            if (proc == null) {
                Slog.e(TAG, "killProcessRecordFromIAware this process has been killed or died before  :" + procInfo.mProcessName);
                return false;
            }
            if (!isNative) {
                if (proc.curAdj < 200 && (AwareAppMngSort.EXEC_SERVICES.equals(proc.adjType) ^ 1) != 0) {
                    Slog.e(TAG, "killProcessRecordFromIAware process cleaner kill process: adj changed, new adj:" + proc.curAdj + ", old adj:" + procInfo.mCurAdj + ", pid:" + procInfo.mPid + ", uid:" + procInfo.mUid + ", " + procInfo.mProcessName);
                    return false;
                }
            }
            String killedProcessName = proc.processName;
            int killedAppUserId = proc.userId;
        }
    }

    public void cleanActivityByUid(List<String> packageList, int targetUid) {
        synchronized (this) {
            int userId = UserHandle.getUserId(targetUid);
            for (String packageName : packageList) {
                if (canCleanTaskRecord(packageName)) {
                    this.mStackSupervisor.finishDisabledPackageActivitiesLocked(packageName, null, true, false, userId);
                }
            }
        }
    }

    public int numOfPidWithActivity(int uid) {
        int count = 0;
        synchronized (this.mPidsSelfLocked) {
            int list_size = this.mPidsSelfLocked.size();
            for (int i = 0; i < list_size; i++) {
                ProcessRecord p = (ProcessRecord) this.mPidsSelfLocked.valueAt(i);
                if (p.uid == uid && p.hasShownUi) {
                    count++;
                }
            }
        }
        return count;
    }

    public List<Integer> getPidWithUiFromUid(int uid) {
        List<Integer> pids = new ArrayList();
        synchronized (this.mPidsSelfLocked) {
            for (int i = 0; i < this.mPidsSelfLocked.size(); i++) {
                ProcessRecord p = (ProcessRecord) this.mPidsSelfLocked.valueAt(i);
                if (p.uid == uid && p.pid != 0 && p.hasShownUi) {
                    pids.add(Integer.valueOf(p.pid));
                }
            }
        }
        return pids;
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
        if (process == null || process.pid == ActivityManagerService.MY_PID || (process.info.flags & 1) != 0) {
            return false;
        }
        return true;
    }

    protected void forceValidateHomeButton(int userId) {
        if (Secure.getIntForUser(this.mContext.getContentResolver(), "user_setup_complete", 0, userId) == 0 || Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) == 0) {
            Global.putInt(this.mContext.getContentResolver(), "device_provisioned", 1);
            Secure.putIntForUser(this.mContext.getContentResolver(), "user_setup_complete", 1, userId);
            Log.w(TAG, "DEVICE_PROVISIONED or USER_SETUP_COMPLETE set 0 to 1!");
        }
    }

    protected boolean isStartLauncherActivity(Intent intent, int userId) {
        if (intent == null) {
            Log.w(TAG, "intent is null, not start launcher!");
            return false;
        }
        PackageManager pm = this.mContext.getPackageManager();
        Intent mainIntent = new Intent("android.intent.action.MAIN").addCategory("android.intent.category.HOME").addCategory("android.intent.category.DEFAULT");
        ComponentName cmp = intent.getComponent();
        if (pm != null && intent.hasCategory("android.intent.category.HOME")) {
            long origId = Binder.clearCallingIdentity();
            try {
                for (ResolveInfo info : pm.queryIntentActivitiesAsUser(mainIntent, 0, userId)) {
                    if (info != null && info.priority == 0 && cmp != null && info.activityInfo != null && cmp.getPackageName().equals(info.activityInfo.packageName)) {
                        Log.d(TAG, "info priority is 0, cmp: " + cmp + ", userId: " + userId);
                        return true;
                    }
                }
                Binder.restoreCallingIdentity(origId);
            } finally {
                Binder.restoreCallingIdentity(origId);
            }
        }
        return false;
    }

    /* JADX WARNING: Missing block: B:9:0x0017, code:
            return r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private String getPackageNameForPid(int pid) {
        synchronized (this.mPidsSelfLocked) {
            ProcessRecord proc = (ProcessRecord) this.mPidsSelfLocked.get(pid);
            if (proc != null) {
                String str = proc.info != null ? proc.info.packageName : "android";
            } else {
                Flog.i(100, "ProcessRecord for pid " + pid + " does not exist");
                return null;
            }
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
            synchronized (this) {
                ActivityStack focusedStack = getFocusedStack();
                if (focusedStack != null) {
                    ActivityRecord top = focusedStack.topRunningActivityLocked();
                    if (top == null) {
                        Binder.restoreCallingIdentity(origId);
                        return false;
                    }
                    boolean z = top.task.mFullscreen ^ 1;
                    Binder.restoreCallingIdentity(origId);
                    return z;
                }
            }
            return false;
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        synchronized (this.mThirdPartyCallbackList) {
            try {
                int i = this.mThirdPartyCallbackList.beginBroadcast();
                Flog.i(100, "onMultiWindowModeChanged : mThirdPartyCallbackList size : " + i);
                while (i > 0) {
                    i--;
                    try {
                        ((IMWThirdpartyCallback) this.mThirdPartyCallbackList.getBroadcastItem(i)).onModeChanged(isInMultiWindowMode);
                    } catch (Exception e) {
                        Flog.e(100, "Error in sending the Callback" + e.getMessage());
                    }
                }
                this.mThirdPartyCallbackList.finishBroadcast();
            } catch (IllegalStateException e2) {
                Flog.e(100, "beginBroadcast() called while already in a broadcast");
            }
        }
        notifyMultiWinToAware(isInMultiWindowMode);
        return;
    }

    public void cleanPackageNotifications(List<String> packageList, int targetUid) {
        if (packageList != null) {
            INotificationManager service = NotificationManager.getService();
            if (service != null) {
                int userId = UserHandle.getUserId(targetUid);
                try {
                    Slog.v(TAG, "cleanupPackageNotifications, userId=" + userId + ProcStateStatisData.SEPERATOR_CHAR + packageList);
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
                        for (StatusBarNotification notification : notifications) {
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
            return false;
        }
        INotificationManager service = NotificationManager.getService();
        if (service == null) {
            return false;
        }
        try {
            StatusBarNotification[] notifications = service.getActiveNotifications("android");
            if (notifications == null) {
                return false;
            }
            for (StatusBarNotification notification : notifications) {
                if (notification.getInitialPid() == pid) {
                    return true;
                }
            }
            return false;
        } catch (RemoteException e) {
            Slog.e(TAG, "Unable to talk to notification manager. Woe!");
        }
    }

    public boolean isLauncher(String packageName) {
        if (Process.myUid() != 1000 || packageName == null || packageName.trim().isEmpty()) {
            return false;
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
                    return packageName.equals(componentName.getPackageName());
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
        return false;
    }

    int broadcastIntentInPackage(String packageName, int uid, Intent intent, String resolvedType, IIntentReceiver resultTo, int resultCode, String resultData, Bundle resultExtras, String requiredPermission, Bundle options, boolean serialized, boolean sticky, int userId) {
        int broadcastIntentInPackage;
        synchronized (this) {
            if (!(packageName == null || options == null)) {
                if (options.getBoolean("fromSystemUI")) {
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
            broadcastIntentInPackage = super.broadcastIntentInPackage(packageName, uid, intent, resolvedType, resultTo, resultCode, resultData, resultExtras, requiredPermission, options, serialized, sticky, userId);
        }
        return broadcastIntentInPackage;
    }

    private void setIntentInfo(Intent intent, int pid, Bundle bundle, boolean forLast) {
        if (forLast) {
            this.mLastSplitIntent = intent;
            this.mSplitExtras = bundle;
            return;
        }
        if (!this.mCurrentSplitIntent.containsKey(Integer.valueOf(pid))) {
            Log.e(TAG, "CRITICAL_LOG add intent info.");
        }
        this.mCurrentSplitIntent.put(Integer.valueOf(pid), intent);
    }

    private Parcelable[] getIntentInfo(int pid, boolean forLast) {
        if (forLast) {
            return new Parcelable[]{this.mLastSplitIntent, this.mSplitExtras};
        }
        return new Parcelable[]{(Parcelable) this.mCurrentSplitIntent.get(Integer.valueOf(pid)), null};
    }

    public void addToEntryStack(int pid, IBinder token, int resultCode, Intent resultData) {
        if (this.mSplitActivityEntryStack == null) {
            this.mSplitActivityEntryStack = new HashMap();
        }
        Flog.i(100, "addToEntryStack, activity is " + token);
        Stack<IBinder> pkgStack = (Stack) this.mSplitActivityEntryStack.get(Integer.valueOf(pid));
        if (pkgStack == null) {
            pkgStack = new Stack();
        }
        pkgStack.push(token);
        this.mSplitActivityEntryStack.put(Integer.valueOf(pid), pkgStack);
    }

    public void clearEntryStack(int pid, IBinder selfToken) {
        if (this.mSplitActivityEntryStack != null && !this.mSplitActivityEntryStack.isEmpty()) {
            Stack<IBinder> pkgStack = (Stack) this.mSplitActivityEntryStack.get(Integer.valueOf(pid));
            if (pkgStack != null && !pkgStack.empty() && (selfToken == null || (selfToken.equals(pkgStack.peek()) ^ 1) == 0)) {
                long ident = Binder.clearCallingIdentity();
                while (!pkgStack.empty()) {
                    IBinder token = (IBinder) pkgStack.pop();
                    if (!(token == null || (token.equals(selfToken) ^ 1) == 0)) {
                        Flog.i(100, "Clearing entry " + token);
                        finishActivity(token, 0, null, 0);
                    }
                }
                Binder.restoreCallingIdentity(ident);
                if (selfToken != null) {
                    pkgStack.push(selfToken);
                }
            }
        }
    }

    public boolean isTopSplitActivity(int pid, IBinder token) {
        if (this.mSplitActivityEntryStack == null || this.mSplitActivityEntryStack.isEmpty() || token == null) {
            return false;
        }
        Stack<IBinder> pkgStack = (Stack) this.mSplitActivityEntryStack.get(Integer.valueOf(pid));
        if (pkgStack == null || pkgStack.empty()) {
            return false;
        }
        return token.equals(pkgStack.peek());
    }

    public void removeFromEntryStack(int pid, IBinder token) {
        if (token != null && this.mSplitActivityEntryStack != null) {
            Stack<IBinder> pkgStack = (Stack) this.mSplitActivityEntryStack.get(Integer.valueOf(pid));
            if (pkgStack != null && pkgStack.empty()) {
                pkgStack.remove(token);
            }
        }
    }

    public boolean isLimitedPackageBroadcast(Intent intent) {
        String action = intent.getAction();
        if (!"android.intent.action.PACKAGE_ADDED".equals(action) && ("android.intent.action.PACKAGE_REMOVED".equals(action) ^ 1) != 0) {
            return false;
        }
        Bundle intentExtras = intent.getExtras();
        boolean limitedPackageBroadcast = intentExtras != null ? intentExtras.getBoolean("LimitedPackageBroadcast", false) : false;
        Flog.d(100, "Android Wear-isLimitedPackageBroadcast: limitedPackageBroadcast = " + limitedPackageBroadcast);
        return limitedPackageBroadcast;
    }

    private boolean isAppMngOomAdjCustomized(String packageName) {
        return AwareDefaultConfigList.getInstance().isAppMngOomAdjCustomized(packageName);
    }

    public void setAndRestoreMaxAdjIfNeed(Set<String> adjCustPkg) {
        if (adjCustPkg != null) {
            synchronized (this) {
                synchronized (this.mPidsSelfLocked) {
                    int list_size = this.mPidsSelfLocked.size();
                    for (int i = 0; i < list_size; i++) {
                        ProcessRecord p = (ProcessRecord) this.mPidsSelfLocked.valueAt(i);
                        if (p != null) {
                            boolean pkgContains = false;
                            for (String pkg : p.pkgList.keySet()) {
                                if (adjCustPkg.contains(pkg)) {
                                    pkgContains = true;
                                    break;
                                }
                            }
                            if (pkgContains) {
                                if (p.maxAdj > 260) {
                                    p.maxAdj = 260;
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

    public boolean isPkgHasAlarm(List<String> packageList, int targetUid) {
        if (packageList == null) {
            return false;
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
            return false;
        }
    }

    /* JADX WARNING: Missing block: B:55:0x00da, code:
            return 0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int preloadApplication(String packageName, int userId) {
        if (Binder.getCallingUid() != 1000) {
            return -1;
        }
        synchronized (this) {
            if (this.mConstants.CUR_MAX_CACHED_PROCESSES <= 8) {
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
            ProcessRecord app = getProcessRecordLocked(appInfo.processName, appInfo.uid, true);
            if (app != null && app.thread != null) {
                Slog.d(TAG, "process has started, packageName:" + packageName + ", processName:" + appInfo.processName);
                return -1;
            } else if ((appInfo.flags & 9) == 9) {
                Slog.d(TAG, "preloadApplication, application is persistent, return");
                return -1;
            } else {
                if (app == null) {
                    app = newProcessRecordLocked(appInfo, null, false, 0);
                    updateLruProcessLocked(app, false, null);
                    updateOomAdjLocked();
                }
                try {
                    pm.setPackageStoppedState(packageName, false, UserHandle.getUserId(app.uid));
                } catch (RemoteException e2) {
                    Slog.w(TAG, "RemoteException, Failed trying to unstop package: " + packageName);
                } catch (IllegalArgumentException e3) {
                    Slog.w(TAG, "IllegalArgumentException, Failed trying to unstop package " + packageName);
                }
                if (app.thread == null) {
                    startProcessLocked(app, "start application", app.processName, null, null, null);
                }
            }
        }
    }

    void reportAppForceStopMsg(int userId, String packageName, int callingPid) {
        String STSMANAGER_PKGNAME = "com.huawei.systemmanager";
        String POWERGENIE_PKGNAME = "com.huawei.powergenie";
        String SETTINGS_PKGNAME = "com.android.settings";
        boolean killedBySysManager = checkIfPackageNameMatchesPid(callingPid, "com.huawei.systemmanager");
        boolean killedByPowerGenie = checkIfPackageNameMatchesPid(callingPid, "com.huawei.powergenie");
        boolean killedBySettings = checkIfPackageNameMatchesPid(callingPid, "com.android.settings");
        if (killedBySysManager || (killedByPowerGenie ^ 1) == 0 || (killedBySettings ^ 1) == 0) {
            if (killedBySysManager) {
                reportAppDiedMsg(userId, packageName, "SystemManager");
            } else if (killedByPowerGenie) {
                reportAppDiedMsg(userId, packageName, "PowerGenie");
            } else {
                reportAppDiedMsg(userId, packageName, "settings");
            }
        }
    }

    private boolean checkIfPackageNameMatchesPid(int mPid, String targetPackageName) {
        if (targetPackageName.equals(getPackageNameForPid(mPid))) {
            return true;
        }
        return false;
    }

    void reportAppDiedMsg(int userId, String processName, String reason) {
        if (processName != null && !processName.contains(":") && reason != null) {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(processName).append(CPUCustBaseConfig.CPUCONFIG_INVALID_STR);
            stringBuffer.append(String.valueOf(userId)).append(CPUCustBaseConfig.CPUCONFIG_INVALID_STR);
            stringBuffer.append(reason);
            LogIAware.report(2031, stringBuffer.toString());
        }
    }

    void reportAppDiedMsg(AppDiedInfo appDiedInfo) {
        if (appDiedInfo != null) {
            if ("forceStop".equals(appDiedInfo.reason)) {
                reportAppForceStopMsg(appDiedInfo.userId, appDiedInfo.processName, appDiedInfo.callerPid);
            } else {
                reportAppDiedMsg(appDiedInfo.userId, appDiedInfo.processName, appDiedInfo.reason);
            }
        }
    }

    protected void checkAndPrintTestModeLog(List list, String intentAction, String callingMethod, String desciption) {
        if (Log.HWINFO && list != null) {
            PackageManager pm = this.mContext.getPackageManager();
            String packageName = null;
            String appName = null;
            if ("android.provider.Telephony.SMS_RECEIVED".equals(intentAction) || "android.provider.Telephony.SMS_DELIVER".equals(intentAction)) {
                int list_size = list.size();
                for (int ii = 0; ii < list_size; ii++) {
                    boolean is_data_ok;
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
                        Log.i(TAG, " <" + appName + ">[" + packageName + "]" + "[" + callingMethod + "]" + desciption);
                    }
                }
            }
        }
    }

    public void registerActivityObserver(IActivityObserver observer) {
        enforceCallingPermission("com.huawei.permission.CONTENT_SENSOR_PERMISSION", "registerActivityObserver()");
        synchronized (this) {
            this.mActivityObservers.register(observer);
        }
    }

    public void unregisterActivityObserver(IActivityObserver observer) {
        enforceCallingPermission("com.huawei.permission.CONTENT_SENSOR_PERMISSION", "registerActivityObserver()");
        synchronized (this) {
            this.mActivityObservers.unregister(observer);
        }
    }

    /* JADX WARNING: Missing block: B:8:0x001e, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean requestContentNode(ComponentName componentName, Bundle data, int token) {
        enforceCallingPermission("com.huawei.permission.CONTENT_SENSOR_PERMISSION", "requestContentNode()");
        synchronized (this) {
            ActivityRecord activity = getFocusedStack().topActivity();
            ActivityRecord realActivity = getTopActivityAppToken(componentName, activity);
            if (!(realActivity == null || realActivity.app == null)) {
                if (realActivity.app.thread != null) {
                    try {
                        realActivity.app.thread.requestContentNode(realActivity.appToken, data, token);
                        return true;
                    } catch (RemoteException e) {
                        Slog.w(TAG, "requestContentNode failed: crash calling " + activity);
                        return false;
                    }
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:8:0x001e, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean requestContentOther(ComponentName componentName, Bundle data, int token) {
        enforceCallingPermission("com.huawei.permission.CONTENT_SENSOR_PERMISSION", "requestContentOther()");
        synchronized (this) {
            ActivityRecord activity = getFocusedStack().topActivity();
            ActivityRecord realActivity = getTopActivityAppToken(componentName, activity);
            if (!(realActivity == null || realActivity.app == null)) {
                if (realActivity.app.thread != null) {
                    try {
                        realActivity.app.thread.requestContentOther(realActivity.appToken, data, token);
                        return true;
                    } catch (RemoteException e) {
                        Slog.w(TAG, "requestContentOther failed: crash calling " + activity);
                        return false;
                    }
                }
            }
        }
    }

    public void dispatchActivityResumed(IBinder token) {
        if (this.mActivityObservers != null) {
            ActivityRecord activityRecord = ActivityRecord.forToken(token);
            if (activityRecord != null && activityRecord.app != null) {
                int i = this.mActivityObservers.beginBroadcast();
                if (i > 3) {
                    i = 3;
                }
                while (i > 0) {
                    i--;
                    IActivityObserver observer = (IActivityObserver) this.mActivityObservers.getBroadcastItem(i);
                    if (observer != null) {
                        try {
                            observer.activityResumed(activityRecord.app.pid, activityRecord.app.uid, activityRecord.realActivity);
                        } catch (RemoteException e) {
                            Slog.w(TAG, "observer.activityResumed get RemoteException, remove observer " + observer);
                            unregisterActivityObserver(observer);
                        }
                    }
                }
                this.mActivityObservers.finishBroadcast();
            }
        }
    }

    public void dispatchActivityPaused(IBinder token) {
        if (this.mActivityObservers != null) {
            ActivityRecord activityRecord = ActivityRecord.forToken(token);
            if (activityRecord != null && activityRecord.app != null) {
                int i = this.mActivityObservers.beginBroadcast();
                if (i > 3) {
                    i = 3;
                }
                while (i > 0) {
                    i--;
                    IActivityObserver observer = (IActivityObserver) this.mActivityObservers.getBroadcastItem(i);
                    if (observer != null) {
                        try {
                            observer.activityPaused(activityRecord.app.pid, activityRecord.app.uid, activityRecord.realActivity);
                        } catch (RemoteException e) {
                            Slog.w(TAG, "observer.activityResumed get RemoteException, remove observer " + observer);
                            unregisterActivityObserver(observer);
                        }
                    }
                }
                this.mActivityObservers.finishBroadcast();
            }
        }
    }

    private ActivityRecord getTopActivityAppToken(ComponentName componentName, ActivityRecord activity) {
        if (activity == null) {
            Slog.w(TAG, "requestContent failed: no activity");
            return null;
        } else if (componentName == null) {
            return null;
        } else {
            if (componentName.equals(activity.realActivity)) {
                Slog.w(TAG, "componentName = " + componentName + " realActivity = " + activity.realActivity + " isEqual = " + componentName.equals(activity.realActivity));
                if (this.mLastActivityRecord != null) {
                    Slog.w(TAG, " mLastActivityRecord = " + this.mLastActivityRecord.realActivity);
                }
                return activity;
            }
            ActivityRecord lastResumedActivity = getLastResumedActivity();
            if (lastResumedActivity != null && componentName.equals(lastResumedActivity.realActivity)) {
                Slog.w(TAG, "lastResumedActivity = " + lastResumedActivity.realActivity);
                return lastResumedActivity;
            } else if (this.mLastActivityRecord == null || !componentName.equals(this.mLastActivityRecord.realActivity)) {
                return null;
            } else {
                Slog.w(TAG, " mLastActivityRecord = " + this.mLastActivityRecord.realActivity);
                return this.mLastActivityRecord;
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

    public int iawareGetUidState(int uid) {
        UidRecord uidRec = (UidRecord) this.mActiveUids.get(uid);
        return uidRec == null ? 18 : uidRec.curProcState;
    }

    protected final ProcessRecord getProcessRecordLocked(String processName, int uid, boolean keepIfLarge) {
        return super.getProcessRecordLocked(processName, UserHandle.getUid(handleUserForClone(processName, UserHandle.getUserId(uid)), uid), keepIfLarge);
    }

    protected int[] handleGidsForUser(int[] gids, int userId) {
        if (!IS_SUPPORT_CLONE_APP) {
            return gids;
        }
        long ident = Binder.clearCallingIdentity();
        try {
            List<UserInfo> profiles = this.mUserController.mInjector.getUserManager().getProfiles(userId, false);
            if (profiles.size() > 1) {
                Iterator<UserInfo> iterator = profiles.iterator();
                while (iterator.hasNext()) {
                    if (((UserInfo) iterator.next()).isManagedProfile()) {
                        iterator.remove();
                    }
                }
                if (profiles.size() > 1) {
                    for (UserInfo ui : profiles) {
                        if (ui.id != userId) {
                            int[] newGids = new int[(gids.length + 1)];
                            System.arraycopy(gids, 0, newGids, 0, gids.length);
                            newGids[gids.length] = UserHandle.getUserGid(ui.id);
                            gids = newGids;
                        }
                    }
                }
            }
            Binder.restoreCallingIdentity(ident);
            return gids;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
        }
    }

    protected final ContentProviderHolder getContentProviderImpl(IApplicationThread caller, String name, IBinder token, boolean stable, int userId) {
        ContentProviderHolder cph = super.getContentProviderImpl(caller, name, token, stable, handleUserForClone(name, userId));
        if (IS_SUPPORT_CLONE_APP && userId != 0 && cph == null) {
            UserInfo ui = this.mUserController.mInjector.getUserManagerInternal().getUserInfo(userId);
            if (ui != null && ui.isClonedProfile()) {
                return super.getContentProviderImpl(caller, name, token, stable, ui.profileGroupId);
            }
        }
        return cph;
    }

    protected final void removeContentProviderExternalUnchecked(String name, IBinder token, int userId) {
        super.removeContentProviderExternalUnchecked(name, token, handleUserForClone(name, userId));
    }

    protected int handleUserForClone(String name, int userId) {
        if (!IS_SUPPORT_CLONE_APP || userId == 0 || name == null) {
            return userId;
        }
        int newUserId = userId;
        if (userId != this.mUserController.getCurrentUserIdLocked() && sAllowedCrossUserForCloneArrays.contains(name)) {
            long ident = Binder.clearCallingIdentity();
            try {
                UserInfo ui = this.mUserController.getUserInfo(userId);
                if (ui != null && ui.isClonedProfile()) {
                    newUserId = ui.profileGroupId;
                }
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
            }
        }
        return newUserId;
    }

    final int broadcastIntentLocked(ProcessRecord callerApp, String callerPackage, Intent intent, String resolvedType, IIntentReceiver resultTo, int resultCode, String resultData, Bundle resultExtras, String[] requiredPermissions, int appOp, Bundle bOptions, boolean ordered, boolean sticky, int callingPid, int callingUid, int userId) {
        String action = null;
        if (intent != null) {
            action = intent.getAction();
        }
        if ("com.android.launcher.action.INSTALL_SHORTCUT".equals(action)) {
            intent.putExtra("android.intent.extra.USER_ID", userId);
        }
        if (this.mHwGameAssistantController != null && ("android.intent.action.PACKAGE_REMOVED".equals(action) || "android.intent.action.PACKAGE_DATA_CLEARED".equals(action))) {
            this.mHwGameAssistantController.onPackageRemovedOrDataCleared(intent);
        }
        return super.broadcastIntentLocked(callerApp, callerPackage, intent, resolvedType, resultTo, resultCode, resultData, resultExtras, requiredPermissions, appOp, bOptions, ordered, sticky, callingPid, callingUid, handleUserForClone(action, userId));
    }

    public ComponentName startService(IApplicationThread caller, Intent service, String resolvedType, boolean requireForeground, String callingPackage, int userId) throws TransactionTooLargeException {
        return super.startService(caller, service, resolvedType, requireForeground, callingPackage, handleUserForClone(getTargetFromIntentForClone(service), userId));
    }

    public int stopService(IApplicationThread caller, Intent service, String resolvedType, int userId) {
        return super.stopService(caller, service, resolvedType, handleUserForClone(getTargetFromIntentForClone(service), userId));
    }

    public int bindService(IApplicationThread caller, IBinder token, Intent service, String resolvedType, IServiceConnection connection, int flags, String callingPackage, int userId) throws TransactionTooLargeException {
        return super.bindService(caller, token, service, resolvedType, connection, flags, callingPackage, handleUserForClone(getTargetFromIntentForClone(service), userId));
    }

    ComponentName startServiceInPackage(int uid, Intent service, String resolvedType, boolean fgRequired, String callingPackage, int userId) throws TransactionTooLargeException {
        return super.startServiceInPackage(uid, service, resolvedType, fgRequired, callingPackage, handleUserForClone(getTargetFromIntentForClone(service), userId));
    }

    private boolean shouldPreventStartProcess(ProcessRecord app) {
        if (app.userId != 0) {
            for (String processName : this.mContext.getResources().getStringArray(33816583)) {
                if (processName.equals(app.processName)) {
                    Slog.i(TAG, app.processName + " is not allowed for sub user " + app.userId);
                    return true;
                }
            }
            UserInfo ui = null;
            long ident = Binder.clearCallingIdentity();
            try {
                ui = this.mUserController.getUserInfo(app.userId);
                if (ui != null && ui.isManagedProfile()) {
                    for (String processName2 : this.mContext.getResources().getStringArray(33816584)) {
                        if (processName2.equals(app.processName)) {
                            Slog.i(TAG, app.processName + " is not allowed for afw user " + app.userId);
                            return true;
                        }
                    }
                }
                if (ui != null && ui.isClonedProfile()) {
                    for (String processName22 : this.mContext.getResources().getStringArray(33816585)) {
                        if (processName22.equals(app.processName)) {
                            Slog.i(TAG, app.processName + " is not allowed for clone user " + app.userId);
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

    private String getTargetFromIntentForClone(Intent intent) {
        if (intent.getAction() == null) {
            return intent.getComponent() != null ? intent.getComponent().getPackageName() : null;
        } else {
            return intent.getAction();
        }
    }

    public void setThreadSchedPolicy(int oldSchedGroup, ProcessRecord app) {
        if (app != null) {
            ArrayList<Integer> tidStrs;
            if (app.curSchedGroup == 2) {
                if (oldSchedGroup != 2) {
                    tidStrs = new ArrayList();
                    tidStrs.add(Integer.valueOf(app.pid));
                    tidStrs.add(Integer.valueOf(app.renderThreadTid));
                    CPUVipThread.getInstance().setAppVipThread(app.pid, tidStrs, true);
                }
            } else if (oldSchedGroup == 2) {
                tidStrs = new ArrayList();
                tidStrs.add(Integer.valueOf(app.pid));
                tidStrs.add(Integer.valueOf(app.renderThreadTid));
                CPUVipThread.getInstance().setAppVipThread(app.pid, tidStrs, false);
            }
        }
    }

    public void setVipThread(ProcessRecord proc) {
        ArrayList<Integer> tidStrs = new ArrayList();
        tidStrs.add(Integer.valueOf(proc.pid));
        tidStrs.add(Integer.valueOf(proc.renderThreadTid));
        CPUVipThread.getInstance().setAppVipThread(proc.pid, tidStrs, true);
    }

    public int iawareGetUidProcNum(int uid) {
        UidRecord uidRec = (UidRecord) this.mActiveUids.get(uid);
        return uidRec == null ? 0 : uidRec.numProcs;
    }

    protected void notifyProcessWillDie(boolean byForceStop, boolean crashed, boolean byAnr, String packageName, int pid, int uid) {
        AwareFakeActivityRecg.self().notifyProcessWillDie(byForceStop, crashed, byAnr, packageName, pid, uid);
    }

    private void reportMultiWinToDevSched(boolean isInMultiWindowMode) {
        if (isInMultiWindowMode) {
            LogIAware.report(2102, "MultiOn");
        } else {
            LogIAware.report(2102, "MultiOff");
        }
    }

    private void notifyMultiWinToAware(boolean isInMultiWindowMode) {
        if (this.isLastMultiMode != isInMultiWindowMode) {
            reportMultiWinToDevSched(isInMultiWindowMode);
            this.isLastMultiMode = isInMultiWindowMode;
        }
        HwSysResManager resManager = HwSysResManager.getInstance();
        if (resManager != null) {
            int resID = ResourceType.getReousrceId(ResourceType.RESOURCE_APP_FREEZE);
            if (resManager.isResourceNeeded(resID) && this.mSystemReady) {
                long curtime = System.currentTimeMillis();
                Bundle bundle = new Bundle();
                bundle.putBoolean("is_multiwin", isInMultiWindowMode);
                bundle.putInt("eventid", 20019);
                CollectData collectData = new CollectData(resID, curtime, bundle);
                long id = Binder.clearCallingIdentity();
                resManager.reportData(collectData);
                Binder.restoreCallingIdentity(id);
            }
        }
    }

    public void togglePCMode(boolean pcMode, int displayId) {
        synchronized (this) {
            if (!pcMode) {
                ActivityDisplay activityDisplay = (ActivityDisplay) this.mStackSupervisor.mActivityDisplays.get(displayId);
                if (activityDisplay != null && (this.mStackSupervisor instanceof HwActivityStackSupervisor)) {
                    ((HwActivityStackSupervisor) this.mStackSupervisor).onDisplayRemoved(activityDisplay.mStacks);
                }
            }
            if (this.mWindowManager instanceof HwWindowManagerService) {
                ((HwWindowManagerService) this.mWindowManager).togglePCMode(pcMode, displayId);
            }
        }
    }

    public void updateFingerprintSlideSwitch() {
        if (HwPCUtils.enabled() && (this.mWindowManager instanceof HwWindowManagerService)) {
            this.mWindowManager.updateFingerprintSlideSwitch();
        }
    }

    public void restoreRotationInPcMode() {
        if (HwPCUtils.enabledInPad()) {
            try {
                if (this.mWindowManager instanceof HwWindowManagerService) {
                    this.mWindowManager.restoreRotationInPcMode();
                }
            } catch (Exception e) {
                HwPCUtils.log(TAG, "restoreRotationInPcMode " + e);
            }
        }
    }

    public void freezeOrThawRotationInPcMode() {
        if (HwPCUtils.enabledInPad()) {
            try {
                if (this.mWindowManager instanceof HwWindowManagerService) {
                    this.mWindowManager.saveRotationInPcMode();
                }
            } catch (Exception e) {
                HwPCUtils.log(TAG, "freezeOrThawRotationInPcMode " + e);
            }
            synchronized (this) {
                this.mWindowManager.freezeRotation(1);
            }
        }
    }

    public void relaunchIMEIfNecessary() {
        if (checkCallingPermission("android.permission.KILL_BACKGROUND_PROCESSES") != 0) {
            if (checkCallingPermission("android.permission.RESTART_PACKAGES") != 0) {
                String msg = "Permission Denial: killBackgroundProcesses() from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires " + "android.permission.KILL_BACKGROUND_PROCESSES";
                Slog.w(TAG, msg);
                throw new SecurityException(msg);
            }
        }
        long callingId = Binder.clearCallingIdentity();
        try {
            String packageName = Secure.getString(this.mContext.getContentResolver(), "default_input_method");
            Iterable methodList = null;
            if (HwPCUtils.enabledInPad()) {
                InputMethodManager imm = (InputMethodManager) this.mContext.getSystemService("input_method");
                if (imm != null) {
                    methodList = imm.getInputMethodList();
                }
            }
            if (packageName != null) {
                int index = packageName.indexOf(47);
                if (index == -1) {
                    Binder.restoreCallingIdentity(callingId);
                    return;
                }
                packageName = packageName.substring(0, index);
                int userId = this.mUserController.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), UserHandle.myUserId(), true, 2, "killBackgroundProcesses", null);
                IPackageManager pm = AppGlobals.getPackageManager();
                synchronized (this) {
                    int appId = -1;
                    try {
                        appId = UserHandle.getAppId(pm.getPackageUid(packageName, 268435456, userId));
                    } catch (RemoteException e) {
                        Slog.e(TAG, "Unable to get Package Uid, userId = " + userId);
                    }
                    if (appId == -1) {
                        Slog.w(TAG, "Invalid packageName: " + packageName);
                        Binder.restoreCallingIdentity(callingId);
                        return;
                    }
                    if (!HwPCUtils.enabledInPad() || methodList == null) {
                        killPackageProcessesLocked(packageName, appId, userId, 100, false, false, true, false, "relaunchIME");
                    } else {
                        for (InputMethodInfo mi : methodList) {
                            killPackageProcessesLocked(mi.getPackageName(), appId, userId, 100, false, false, true, false, "relaunchIME");
                        }
                    }
                    Binder.restoreCallingIdentity(callingId);
                    return;
                }
            }
            return;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    /* JADX WARNING: Missing block: B:49:0x0090, code:
            android.os.Binder.restoreCallingIdentity(r4);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void hwRestoreTask(int taskId, float xPos, float yPos) {
        if (HwPCUtils.isPcCastModeInServer()) {
            HwPCMultiWindowManager multiWindowMgr = HwPCMultiWindowManager.getInstance(this);
            if (multiWindowMgr != null) {
                long origId = Binder.clearCallingIdentity();
                try {
                    synchronized (this) {
                        TaskRecord tr = this.mStackSupervisor.anyTaskForIdLocked(taskId);
                        if (tr != null) {
                            if (HwPCMultiWindowCompatibility.isRestorable(tr.mWindowState)) {
                                Rect rect = multiWindowMgr.getWindowBounds(tr);
                                if (rect == null) {
                                    Binder.restoreCallingIdentity(origId);
                                    return;
                                }
                                if (!(xPos == -1.0f || yPos == -1.0f)) {
                                    Rect bounds = tr.mBounds;
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
                            } else {
                                Binder.restoreCallingIdentity(origId);
                            }
                        }
                    }
                } finally {
                    Binder.restoreCallingIdentity(origId);
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:24:0x0037 A:{Catch:{ all -> 0x00e3 }} */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0024 A:{SYNTHETIC, Splitter: B:16:0x0024} */
    /* JADX WARNING: Missing block: B:38:0x00a5, code:
            android.os.Binder.restoreCallingIdentity(r4);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void hwResizeTask(int taskId, Rect bounds) {
        if (HwPCUtils.isPcCastModeInServer()) {
            HwPCMultiWindowManager multiWindowMgr = HwPCMultiWindowManager.getInstance(this);
            if (multiWindowMgr != null) {
                long origId = Binder.clearCallingIdentity();
                try {
                    synchronized (this) {
                        TaskRecord task;
                        boolean isFullscreen = false;
                        boolean isMaximized = false;
                        if (bounds != null) {
                            if (bounds.top < 0) {
                                bounds = null;
                                isFullscreen = true;
                                task = this.mStackSupervisor.anyTaskForIdLocked(taskId);
                                if (task == null) {
                                    if (multiWindowMgr.isSupportResize(task, isFullscreen, isMaximized)) {
                                        String str;
                                        String str2 = "HwPCMultiWindowManager";
                                        StringBuilder append = new StringBuilder().append("hwResizeTask: ");
                                        if (bounds == null) {
                                            str = "null";
                                        } else {
                                            str = bounds.toShortString() + " (" + bounds.width() + ", " + bounds.height() + ")";
                                        }
                                        HwPCUtils.log(str2, append.append(str).toString());
                                        task.resize(bounds, 3, true, false);
                                    } else {
                                        HwPCUtils.log("HwPCMultiWindowManager", "hwResizeTask-fail: (" + Integer.toHexString(task.mWindowState) + ")" + "isFullscreen:" + isFullscreen + "; isMax:" + isMaximized);
                                        Binder.restoreCallingIdentity(origId);
                                        return;
                                    }
                                }
                            }
                        }
                        if (bounds != null) {
                            if (bounds.isEmpty()) {
                                bounds = multiWindowMgr.getMaximizedBounds();
                                isMaximized = true;
                            }
                        }
                        task = this.mStackSupervisor.anyTaskForIdLocked(taskId);
                        if (task == null) {
                        }
                    }
                } finally {
                    Binder.restoreCallingIdentity(origId);
                }
            }
        }
    }

    public int getWindowState(IBinder token) {
        if (!HwPCUtils.isPcCastModeInServer()) {
            return -1;
        }
        long ident = Binder.clearCallingIdentity();
        try {
            ActivityRecord r;
            synchronized (this) {
                r = ActivityRecord.isInStackLocked(token);
            }
            if (r == null || r.task == null) {
                Binder.restoreCallingIdentity(ident);
                return -1;
            }
            int windowState = r.task.getWindowState();
            Binder.restoreCallingIdentity(ident);
            return windowState;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public HwRecentTaskInfo getHwRecentTaskInfo(int taskId) {
        synchronized (this) {
            long origId = Binder.clearCallingIdentity();
            try {
                TaskRecord tr = this.mStackSupervisor.anyTaskForIdLocked(taskId);
                if (tr != null) {
                    HwRecentTaskInfo createHwRecentTaskInfoFromTaskRecord = createHwRecentTaskInfoFromTaskRecord(tr);
                    Binder.restoreCallingIdentity(origId);
                    return createHwRecentTaskInfoFromTaskRecord;
                }
                Binder.restoreCallingIdentity(origId);
                return null;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(origId);
            }
        }
    }

    protected void forceGCAfterRebooting() {
        List<RunningAppProcessInfo> runningAppInfo = getRunningAppProcesses();
        if (runningAppInfo == null) {
            HwPCUtils.log("HwPCMultiWindowManager", "forceGCAfterRebooting-fail: runningAppInfo is null");
            return;
        }
        for (RunningAppProcessInfo appProcess : runningAppInfo) {
            Process.sendSignal(appProcess.pid, 10);
        }
    }

    protected HwRecentTaskInfo createHwRecentTaskInfoFromTaskRecord(TaskRecord tr) {
        RecentTaskInfo rti = createRecentTaskInfoFromTaskRecord(tr);
        HwRecentTaskInfo hwRti = new HwRecentTaskInfo();
        hwRti.translateRecentTaskinfo(rti);
        ActivityStack stack = tr.getStack();
        if (stack != null) {
            hwRti.displayId = stack.mDisplayId;
            hwRti.isStackVisibility = stack.isVisible();
        }
        hwRti.windowState = tr.getWindowState();
        if (!tr.mActivities.isEmpty() && (this.mWindowManager instanceof HwWindowManagerService)) {
            hwRti.systemUiVisibility = ((HwWindowManagerService) this.mWindowManager).getWindowSystemUiVisibility(((ActivityRecord) tr.mActivities.get(0)).appToken);
        }
        return hwRti;
    }

    /* JADX WARNING: Missing block: B:14:0x001c, code:
            super.overridePendingTransition(r3, r4, r5, r6);
     */
    /* JADX WARNING: Missing block: B:15:0x001f, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void overridePendingTransition(IBinder token, String packageName, int enterAnim, int exitAnim) {
        synchronized (this) {
            ActivityRecord self = ActivityRecord.isInStackLocked(token);
            if (self == null) {
            } else if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(self.getDisplayId())) {
            }
        }
    }

    public void moveTaskBackwards(int task) {
        boolean handled = false;
        if (HwPCUtils.isPcCastModeInServer()) {
            synchronized (this) {
                long origId = Binder.clearCallingIdentity();
                TaskRecord tr = this.mStackSupervisor.anyTaskForIdLocked(task);
                if (tr != null && HwPCUtils.isPcDynamicStack(tr.getStackId())) {
                    handled = true;
                    tr.getStack().moveTaskToBackLocked(task);
                }
                Binder.restoreCallingIdentity(origId);
            }
        }
        if (!handled) {
            super.moveTaskBackwards(task);
        }
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

    public Bitmap getDisplayBitmap(int displayId, int width, int height) {
        if (this.mWindowManager instanceof HwWindowManagerService) {
            return ((HwWindowManagerService) this.mWindowManager).getDisplayBitmap(displayId, width, height);
        }
        return null;
    }

    protected boolean isTaskNotResizeableEx(TaskRecord task, Rect bounds) {
        if (!isTaskSizeChange(task, bounds) || (HwPCMultiWindowCompatibility.isResizable(task.getWindowState()) ^ 1) == 0) {
            return !isTaskSizeChange(task, bounds) ? HwPCMultiWindowCompatibility.isLayoutHadBounds(task.getWindowState()) ^ 1 : false;
        } else {
            return true;
        }
    }

    private boolean isTaskSizeChange(TaskRecord task, Rect rect) {
        return (task.mBounds.width() == rect.width() && task.mBounds.height() == rect.height()) ? false : true;
    }

    public void toggleHome() {
        if (HwPCUtils.isPcCastModeInServer()) {
            synchronized (this) {
                long origId = Binder.clearCallingIdentity();
                try {
                    int displayId = this.mWindowManager.getFocusedDisplayId();
                    if (HwPCUtils.isValidExtDisplayId(displayId)) {
                        ActivityDisplay activityDisplay = (ActivityDisplay) this.mStackSupervisor.mActivityDisplays.get(displayId);
                        if (activityDisplay == null) {
                            Binder.restoreCallingIdentity(origId);
                            return;
                        }
                        int stackNdx;
                        ActivityStack stack;
                        ArrayList<ActivityStack> stacks = new ArrayList();
                        stacks.addAll(activityDisplay.mStacks);
                        boolean moveAllToBack = true;
                        for (stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                            stack = (ActivityStack) stacks.get(stackNdx);
                            if ((stack instanceof HwActivityStack) && ((HwActivityStack) stack).mHiddenFromHome) {
                                moveAllToBack = false;
                                break;
                            }
                        }
                        for (stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                            stack = (ActivityStack) stacks.get(stackNdx);
                            TaskRecord task = stack.topTask();
                            if (task != null) {
                                if (moveAllToBack) {
                                    if (stack.shouldBeVisible(null) == 1 && (stack instanceof HwActivityStack) && (((HwActivityStack) stack).mHiddenFromHome ^ 1) != 0) {
                                        stack.moveTaskToBackLocked(task.taskId);
                                        ((HwActivityStack) stack).mHiddenFromHome = true;
                                    }
                                } else if (stack.shouldBeVisible(null) == 0 && (stack instanceof HwActivityStack) && ((HwActivityStack) stack).mHiddenFromHome) {
                                    stack.moveTaskToFrontLocked(task, true, null, null, "moveToFrontFromHomeKey.");
                                    ((HwActivityStack) stack).mHiddenFromHome = false;
                                }
                            }
                        }
                        Binder.restoreCallingIdentity(origId);
                    } else {
                        Binder.restoreCallingIdentity(origId);
                    }
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(origId);
                }
            }
        }
    }

    protected boolean isMaximizedPortraitAppOnPCMode(ActivityRecord r) {
        if (!(!HwPCUtils.isPcCastModeInServer() || r.getStack() == null || r.info == null || r.info.getComponentName() == null || !HwPCUtils.isValidExtDisplayId(r.getStack().mDisplayId))) {
            if (HwPCMultiWindowManager.getInstance(this).mPortraitMaximizedPkgList.contains(r.info.getComponentName().getPackageName())) {
                return true;
            }
        }
        return false;
    }

    void recordPssSampleLocked(ProcessRecord proc, int procState, long pss, long uss, long swapPss, long now) {
        if (!(proc == null || proc.info == null)) {
            ProcStateStatisData.getInstance().addPssToMap(proc.processName, proc.info.uid, proc.pid, procState, pss, SystemClock.uptimeMillis(), this.mTestPssMode);
        }
        super.recordPssSampleLocked(proc, procState, pss, uss, swapPss, now);
    }

    public void registerExternalPointerEventListener(PointerEventListener listener) {
        this.mWindowManager.registerExternalPointerEventListener(listener);
    }

    public void unregisterExternalPointerEventListener(PointerEventListener listener) {
        this.mWindowManager.unregisterExternalPointerEventListener(listener);
    }

    public void setPCScreenDpMode(int mode) {
        if (this.mWindowManager instanceof HwWindowManagerService) {
            ((HwWindowManagerService) this.mWindowManager).setPCScreenDisplayMode(mode);
        }
    }

    public int getPCScreenDisplayMode() {
        if (this.mWindowManager instanceof HwWindowManagerService) {
            return ((HwWindowManagerService) this.mWindowManager).getPCScreenDisplayMode();
        }
        return 0;
    }

    public boolean isPackageRunningOnPCMode(String packageName, int uid) {
        Slog.d(TAG, "isPackageRunningOnPCMode, packageName:" + packageName + ", uid:" + uid);
        if (packageName == null) {
            Slog.e(TAG, "isPackageRunningOnPCMode packageName == null");
            return false;
        }
        synchronized (this) {
            ProcessRecord pr = (ProcessRecord) this.mProcessNames.get(packageName, uid);
            if (pr != null) {
                Slog.d(TAG, "pr.mDisplayId:" + pr.mDisplayId);
                boolean isValidExtDisplayId = HwPCUtils.isValidExtDisplayId(pr.mDisplayId);
                return isValidExtDisplayId;
            }
            Slog.d(TAG, "no pr, packageName:" + packageName);
            return false;
        }
    }

    protected final boolean checkUriPermissionLocked(GrantUri grantUri, int uid, int modeFlags) {
        boolean result = super.checkUriPermissionLocked(grantUri, uid, modeFlags);
        if (!(!IS_SUPPORT_CLONE_APP || (result ^ 1) == 0 || grantUri == null || grantUri.uri == null || UserHandle.getUserId(uid) == this.mUserController.getCurrentUserIdLocked() || !sAllowedCrossUserForCloneArrays.contains(grantUri.uri.getAuthority()))) {
            long ident = Binder.clearCallingIdentity();
            try {
                UserInfo ui = this.mUserController.getUserInfo(UserHandle.getUserId(uid));
                if (ui == null || !ui.isClonedProfile()) {
                    Binder.restoreCallingIdentity(ident);
                } else {
                    boolean checkUriPermissionLocked = super.checkUriPermissionLocked(grantUri, UserHandle.getUid(ui.profileGroupId, uid), modeFlags);
                    return checkUriPermissionLocked;
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
        return result;
    }

    public void removePackageAlarm(String pkg, List<String> tags) {
        synchronized (this) {
            if (this.mAlms != null) {
                this.mAlms.removePackageAlarm(pkg, tags);
            } else {
                Slog.e(TAG, "removeByTag alarm instance is null");
            }
        }
    }

    public int sendIntentSender(IIntentSender target, IBinder whitelistToken, int code, Intent intent, String resolvedType, IIntentReceiver finishedReceiver, String requiredPermission, Bundle options) {
        if (HwPCUtils.isPcCastModeInServer() && (target instanceof PendingIntentRecord)) {
            Key key = ((PendingIntentRecord) target).key;
            if (key != null) {
                int displayId = options != null ? ActivityOptions.fromBundle(options).getLaunchDisplayId() : 0;
                if (!HwPCUtils.enabledInPad() || !"com.android.incallui".equals(key.packageName) || (isKeyguardLocked() ^ 1) == 0 || (HwPCUtils.isValidExtDisplayId(displayId) ^ 1) == 0) {
                    this.mPkgDisplayMaps.put(key.packageName, Integer.valueOf(displayId));
                } else {
                    Slog.d(TAG, "sendIntentSender skip when screen on, packageName: " + key.packageName + ",isKeyguardLocked: " + isKeyguardLocked() + ",displayId: " + displayId);
                    return 0;
                }
            }
        }
        return super.sendIntentSender(target, whitelistToken, code, intent, resolvedType, finishedReceiver, requiredPermission, options);
    }

    public boolean checkTaskId(int taskId) {
        ProcessRecord processRecord = (ProcessRecord) this.mPidsSelfLocked.get(Binder.getCallingPid());
        if (processRecord != null) {
            ArrayList<ActivityRecord> activityRecords = processRecord.activities;
            ArrayList taskIdContainer = new ArrayList();
            int recordsSize = activityRecords.size();
            for (int i = 0; i < recordsSize; i++) {
                TaskRecord task = ((ActivityRecord) activityRecords.get(i)).getTask();
                if (task != null) {
                    taskIdContainer.add(Integer.valueOf(task.taskId));
                }
            }
            if (taskIdContainer != null && taskIdContainer.contains(Integer.valueOf(taskId))) {
                return true;
            }
        }
        return false;
    }

    public void cleanAppForHiddenSpace() {
        final MultiTaskManager instance = MultiTaskManager.getInstance();
        IAppCleanCallback callback = new IAppCleanCallback.Stub() {
            public void onCleanFinish(AppCleanParam result) {
                List<String> pkgNames = result.getStringList();
                List<Integer> userIds = result.getIntList();
                List<Integer> killTypes = result.getIntList2();
                if (pkgNames != null && userIds != null && killTypes != null) {
                    List<AppCleanInfo> appCleanInfoList = new ArrayList();
                    for (int i = 0; i < pkgNames.size(); i++) {
                        appCleanInfoList.add(new AppCleanInfo((String) pkgNames.get(i), (Integer) userIds.get(i), (Integer) killTypes.get(i)));
                    }
                    IAppCleanCallback callback2 = new IAppCleanCallback.Stub() {
                        public void onCleanFinish(AppCleanParam result2) {
                        }
                    };
                    Slog.d(HwActivityManagerService.TAG, "executeMultiAppClean for hidden space");
                    if (instance != null) {
                        instance.executeMultiAppClean(appCleanInfoList, callback2);
                    }
                }
            }
        };
        if (instance != null) {
            instance.getAppListForUserClean(callback);
        }
    }

    public boolean isTaskVisible(int id) {
        TaskRecord tr = this.mStackSupervisor.anyTaskForIdLocked(id);
        return (tr == null || tr.getTopActivity() == null) ? false : tr.getTopActivity().visible;
    }

    public boolean addGameSpacePackageList(List<String> packageList) {
        if (this.mHwGameAssistantController != null) {
            return this.mHwGameAssistantController.addGameSpacePackageList(packageList);
        }
        return false;
    }

    public boolean delGameSpacePackageList(List<String> packageList) {
        if (this.mHwGameAssistantController != null) {
            return this.mHwGameAssistantController.delGameSpacePackageList(packageList);
        }
        return false;
    }

    public boolean isInGameSpace(String packageName) {
        if (this.mHwGameAssistantController != null) {
            return this.mHwGameAssistantController.isInGameSpace(packageName);
        }
        return false;
    }

    public List<String> getGameList() {
        if (this.mHwGameAssistantController != null) {
            return this.mHwGameAssistantController.getGameList();
        }
        return null;
    }

    public void registerGameObserver(IGameObserver observer) {
        if (this.mHwGameAssistantController != null) {
            this.mHwGameAssistantController.registerGameObserver(observer);
        }
    }

    public void unregisterGameObserver(IGameObserver observer) {
        if (this.mHwGameAssistantController != null) {
            this.mHwGameAssistantController.unregisterGameObserver(observer);
        }
    }

    public boolean isGameDndOn() {
        if (this.mHwGameAssistantController != null) {
            return this.mHwGameAssistantController.isGameDndOn();
        }
        return false;
    }

    public boolean isGameKeyControlOn() {
        if (this.mHwGameAssistantController != null) {
            return this.mHwGameAssistantController.isGameKeyControlOn();
        }
        return false;
    }

    public boolean isGameGestureDisabled() {
        if (this.mHwGameAssistantController != null) {
            return this.mHwGameAssistantController.isGameGestureDisabled();
        }
        return false;
    }
}
