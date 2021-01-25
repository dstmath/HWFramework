package com.android.server.rms.iaware.appmng;

import android.annotation.SuppressLint;
import android.app.WallpaperInfo;
import android.app.mtm.iaware.appmng.AppMngConstant;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserManager;
import android.provider.Settings;
import android.rms.HwSysResManager;
import android.rms.iaware.AppTypeRecoManager;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CmpTypeInfo;
import android.rms.iaware.ComponentRecoManager;
import android.rms.iaware.DeviceInfo;
import android.rms.iaware.IAwareCMSManager;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import com.android.internal.content.ValidGPackageHelper;
import com.android.server.mtm.MultiTaskManagerService;
import com.android.server.mtm.iaware.appmng.AppBatteryStrategy;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort;
import com.android.server.mtm.iaware.appmng.AwareProcessBlockInfo;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.iaware.appmng.AwareProcessWindowInfo;
import com.android.server.mtm.iaware.appmng.CloudPushManager;
import com.android.server.mtm.iaware.appmng.DecisionMaker;
import com.android.server.mtm.iaware.appmng.appstart.comm.AppStartupUtil;
import com.android.server.mtm.iaware.appmng.appstart.datamgr.AppStartupDataMgr;
import com.android.server.mtm.iaware.appmng.rule.AppMngRule;
import com.android.server.mtm.iaware.appmng.rule.ListItem;
import com.android.server.mtm.iaware.appmng.rule.RuleNode;
import com.android.server.mtm.iaware.srms.AwareBroadcastDebug;
import com.android.server.mtm.iaware.srms.AwareBroadcastPolicy;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfoCollector;
import com.android.server.mtm.utils.AppStatusUtils;
import com.android.server.mtm.utils.InnerUtils;
import com.android.server.mtm.utils.SparseSet;
import com.android.server.rms.algorithm.AwareUserHabit;
import com.android.server.rms.dualfwk.AwareMiddleware;
import com.android.server.rms.iaware.appmng.AppStartPolicyCfg;
import com.android.server.rms.iaware.appmng.AwareAppKeyBackgroup;
import com.android.server.rms.iaware.feature.AppAccurateRecgFeature;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.qos.AwareBinderSchedManager;
import com.android.server.rms.iaware.sysload.SysLoadManager;
import com.huawei.android.app.ActivityManagerNativeExt;
import com.huawei.android.app.IWallpaperManagerExt;
import com.huawei.android.content.ContentResolverExt;
import com.huawei.android.content.pm.PackageManagerExt;
import com.huawei.android.content.pm.ResolveInfoEx;
import com.huawei.android.content.pm.UserInfoExAdapter;
import com.huawei.android.internal.util.MemInfoReaderExt;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.os.UserManagerExt;
import com.huawei.android.pgmng.plug.PowerKit;
import com.huawei.android.provider.SettingsEx;
import com.huawei.android.view.HwWindowManager;
import com.huawei.android.webkit.WebViewZygoteEx;
import com.huawei.iaware.AwareServiceThread;
import com.huawei.internal.os.BackgroundThreadEx;
import com.huawei.server.wm.WindowProcessControllerEx;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public final class AwareIntelligentRecg {
    private static final int ACTION_INDEX = 0;
    private static final int ACTION_LENGTH = 1;
    private static final String ACT_TOP_IM_CN = "act_topim_cn";
    private static final int ALARM_ACTION_INDEX = 1;
    private static final int ALARM_PERCEPTION_TIME = 15000;
    private static final int ALARM_RECG_FACTOR = 1;
    private static final int ALARM_RECG_FACTOR_FORCLOCK = 5;
    private static final int ALARM_TAG_LENGTH = 2;
    private static final int ALLOW_BOOT_START_IM_APP_NUM = 3;
    private static final int ALLOW_BOOT_START_IM_MEM_THRESHOLD = 3072;
    private static final int APP_ACT_TOP_DEFAULT = 0;
    private static final int APP_ACT_TOP_FIVE = 3;
    private static final int APP_ACT_TOP_ONE = 1;
    private static final int APP_ACT_TOP_TWO = 2;
    private static final String APP_LOCK_CLASS = "app_lock_class";
    private static final int APP_MNG_PROP_MAX_INDEX = 1;
    private static final int APP_MNG_PROP_START_INDEX = 0;
    private static final String BACKGROUND_CHECK_EXCLUDED_ACTION = "bgcheck_excluded_action";
    private static final int BLE_TYPE = 0;
    private static final String BLIND_PREFABRICATED = "blind_prefabricated";
    private static final int BLUETOOTH_LAST_USED_PARAMS = 2;
    private static final String BLUETOOTH_PKG = "com.android.bluetooth";
    private static final String BT_MEDIA_BROWSER_ACTION = "android.media.browse.MediaBrowserService";
    private static final String CHINA_MCC = "460";
    private static final int CLS_INDEX = 1;
    private static final int CMP_LENGTH = 2;
    private static final int CONNECT_PG_DELAYED = 5000;
    private static final int DEFAULT_INVAILD_UID = -1;
    private static final int DEFAULT_MEMORY = -1;
    private static final long DEFAULT_REQUEST_TIMEOUT = 1800000;
    private static final int DUMP_APPSCENEINFO_BLE_ON = 3;
    private static final int DUMP_APPSCENEINFO_SIM = 2;
    private static final int DUMP_APP_SCENE_INFO_NOT_USED = 1;
    private static final int FREEZE_NOT_CLEAN = 10;
    private static final int FREEZE_NOT_CLEAN_ENTER = 1;
    private static final int FREEZE_NOT_CLEAN_EXIT = 0;
    private static final int FREEZE_PIDS = 100;
    private static final long GOOGLE_CONN_DELAY_TIME = 604800000;
    private static final int GOOGLE_CONN_PROP_MAX_INDEX = 2;
    private static final int GOOGLE_CONN_PROP_START_INDEX = 0;
    private static final int INVALID_TASKID = -1;
    private static final long INVALID_TIME = -1;
    private static final int LOAD_CMP_TYPE_MESSAGE_DELAY = 20000;
    private static final Object LOCK = new Object();
    private static final int MCC_LENGTH = 3;
    private static final int MSG_ADD_FG_PID_INFO = 22;
    private static final int MSG_ALARM_NOTIFICATION = 9;
    private static final int MSG_ALARM_SOUND = 10;
    private static final int MSG_ALARM_UNPERCEPTION = 11;
    private static final int MSG_ALARM_VIBRATOR = 8;
    private static final int MSG_CACHEDATA_FLUSH_TO_DISK = 6;
    private static final int MSG_CHECK_CMP_DATA = 16;
    private static final int MSG_CONNECT_WITH_PG_SDK = 5;
    private static final int MSG_DELETE_DATA = 3;
    private static final int MSG_INIT_FG_PID_INFO = 21;
    private static final int MSG_INPUT_METHOD_SET = 15;
    private static final int MSG_INSERT_DATA = 2;
    private static final int MSG_KBG_UPDATE = 7;
    private static final int MSG_LOAD_DATA = 1;
    private static final int MSG_RECG_PUSH_SDK = 4;
    private static final int MSG_REMOVE_FG_PID_INFO = 23;
    private static final int MSG_UNFREEZE_NOT_CLEAN = 17;
    private static final int MSG_UPDATE_BLE_STATUS = 20;
    private static final int MSG_UPDATE_DB = 12;
    private static final int MSG_UPDOWN_CLEAR = 19;
    private static final int MSG_WAKENESS_CHANGE = 18;
    private static final int MSG_WALLPAPER_INIT = 14;
    private static final int MSG_WALLPAPER_SET = 13;
    private static final String NOTIFY_LISTENER_ACTION = "android.service.notification.NotificationListenerService";
    private static final int ONE_GIGA_BYTE = 1024;
    private static final int ONE_MINUTE = 60000;
    private static final int ONE_SECOND = 1000;
    public static final int PENDING_ALARM = 0;
    public static final int PENDING_PERC = 2;
    public static final int PENDING_UNPERC = 1;
    private static final String PROPERTIES_GOOELE_CONNECTION = "persist.sys.iaware_google_conn";
    private static final String PUSH_SDK_BAD = "push_bad";
    private static final String PUSH_SDK_GOOD = "push_good";
    private static final int PUSH_SDK_START_SAME_CMP_COUNT = 2;
    private static final int REPORT_APP_UPDATE_MSG = 0;
    private static final String SEPARATOR_COMMA = ",";
    private static final String SEPARATOR_POUND = "#";
    private static final String SEPARATOR_SEMICOLON = ":";
    private static final int SPEC_VALUE_DEFAULT = -1;
    private static final int SPEC_VALUE_DEFAULT_MAX_TOPN = 999;
    private static final int SPEC_VALUE_TRUE = 1;
    private static final int START_INFO_CACHE_TIME = 20000;
    private static final int STATUS_DEFAULT = 0;
    private static final int STATUS_PERCEPTION = 1;
    private static final int STATUS_UNPERCEPTION = 2;
    private static final String SWITCH_STATUS_STR = "1";
    private static final String SYSTEM_PROCESS_NAME = "system";
    private static final String TAG = "RMS.AwareIntelligentRecg";
    private static final String TAG_DOZE_PROTECT = "frz_protect";
    private static final String TAG_GET_DOZE_LIST = "hsm_get_freeze_list";
    private static final String TOP_IM_CN_PROP = "persist.sys.iaware.topimcn";
    private static final int TYPE_TOPN_EXTINCTION_TIME = 172800000;
    private static final int UNFREEZE_NOT_CLEAN_INTERVAL = 60000;
    private static final String UNKNOWN_PKG = "unknownpkg";
    private static final int UNPERCEPTION_COUNT = 1;
    private static final long UPDATE_DB_INTERVAL = 86400000;
    private static final int UPDATE_TIME = 10000000;
    private static final int UPDATE_TIME_FOR_HABIT = 10000;
    private static final String URI_PREFIX = "content://";
    private static final String URI_SYSTEM_MANAGER_SMART_PROVIDER = "@com.huawei.android.smartpowerprovider";
    private static final String URI_SYSTEM_MANAGER_UNIFIED_POWER_APP = "content://com.huawei.android.smartpowerprovider/unifiedpowerapps";
    private static final String VALUE_BT_BLE_CONNECT_APPS = "huawei_bt_ble_connect_apps";
    private static final String VALUE_BT_LAST_BLE_DISCONNECT = "huawei_bt_ble_last_disconnect";
    public static final long WIDGET_INVALID_ELAPSE_TIME = -1;
    private static AwareIntelligentRecg sAwareIntlgRecg = null;
    private static boolean sBleStatus = true;
    private static boolean sDebug = false;
    private static boolean sEnabled = false;
    private final Set<String> mAccessPkg = new ArraySet();
    private String mActTopImCn = UNKNOWN_PKG;
    private final ArrayMap<String, CmpTypeInfo> mAlarmCmps = new ArrayMap<>();
    private final SparseArray<ArrayMap<String, AlarmInfo>> mAlarmMap = new SparseArray<>();
    private Set<String> mAlarmPkgList = null;
    private final Map<String, Long> mAllowStartPkgs = new ArrayMap();
    private final ArrayMap<String, Long> mAppChangeToBgTime = new ArrayMap<>();
    private PowerKit.Sink mAppFreezeListener = new PowerKit.Sink() {
        /* class com.android.server.rms.iaware.appmng.AwareIntelligentRecg.AnonymousClass1 */

        public void onStateChanged(int stateType, int eventType, int pid, String pkg, int uid) {
            if (AwareIntelligentRecg.sDebug) {
                AwareLog.d(AwareIntelligentRecg.TAG, "pkg:" + pkg + ",uid:" + uid + ",pid:" + pid + ",stateType: " + stateType + ",eventType:" + eventType);
            }
            if (stateType == 6) {
                synchronized (AwareIntelligentRecg.this.mFrozenAppList) {
                    if (eventType == 1) {
                        try {
                            AwareIntelligentRecg.this.mFrozenAppList.add(uid);
                        } catch (Throwable th) {
                            throw th;
                        }
                    } else {
                        if (eventType == 2) {
                            AwareIntelligentRecg.this.mFrozenAppList.remove(uid);
                        }
                    }
                }
            } else if (stateType != 7) {
                onStateChangedEx(stateType, eventType, pid, pkg, uid);
            } else {
                synchronized (AwareIntelligentRecg.this.mFrozenAppList) {
                    AwareIntelligentRecg.this.mFrozenAppList.clear();
                }
                synchronized (AwareIntelligentRecg.this.mNotCleanPkgList) {
                    AwareIntelligentRecg.this.mNotCleanPkgList.clear();
                }
                SysLoadManager.getInstance().exitGameSceneMsg();
                ProcessInfoCollector.getInstance().resetAwareProcessStatePgRestart();
            }
        }

        private void onStateChangedEx(int stateType, int eventType, int pid, String pkg, int uid) {
            if (stateType != 2) {
                if (stateType == 8) {
                    synchronized (AwareIntelligentRecg.this.mBluetoothAppList) {
                        if (eventType == 1) {
                            try {
                                AwareIntelligentRecg.this.mBluetoothAppList.add(uid);
                            } catch (Throwable th) {
                                throw th;
                            }
                        } else {
                            if (eventType == 2) {
                                AwareIntelligentRecg.this.mBluetoothAppList.remove(uid);
                            }
                        }
                    }
                } else if (stateType == 10) {
                    AwareIntelligentRecg.this.resolvePkgsName(pkg, eventType);
                } else if (stateType == 12) {
                    AwareIntelligentRecg.this.refreshAlivedApps(eventType, pkg, uid);
                } else if (stateType == 100) {
                    AwareIntelligentRecg.this.resolvePids(pkg, uid, eventType);
                }
            } else if (eventType == 1) {
                AwareIntelligentRecg.this.sendPerceptionMessage(10, uid, null);
                synchronized (AwareIntelligentRecg.this.mAudioOutInstant) {
                    AwareIntelligentRecg.this.mAudioOutInstant.add(uid);
                }
            } else if (eventType == 2) {
                synchronized (AwareIntelligentRecg.this.mAudioOutInstant) {
                    AwareIntelligentRecg.this.mAudioOutInstant.remove(uid);
                }
            }
        }
    };
    private String mAppLockClass = UNKNOWN_PKG;
    private final long mAppMngAllowTime = SystemPropertiesEx.getLong("persist.sys.iaware.mngallowtime", 1800);
    private boolean mAppMngPropCfgInit = false;
    private int mAppStartAreaCfg = -1;
    private boolean mAppStartEnabled = false;
    private final Set<AwareAppStartInfo> mAppStartInfoList = new ArraySet();
    private final SparseSet mAudioOutInstant = new SparseSet();
    private AwareGameModeRecg mAwareGameModeRecg = null;
    private AwareStateCallback mAwareStateCallback = null;
    private ArraySet<String> mBgCheckExcludedAction = new ArraySet<>();
    private ArraySet<String> mBgCheckExcludedPkg = new ArraySet<>();
    private final Set<String> mBlindPkg = new ArraySet();
    private final SparseSet mBluetoothAppList = new SparseSet();
    private final Object mBluetoothLock = new Object();
    private int mBluetoothUid;
    private final ArrayList<Integer> mBtoothConnectList = new ArrayList<>();
    private int mBtoothLastPid = -1;
    private long mBtoothLastTime = 0;
    private volatile ArrayMap<Integer, Set<String>> mCachedTypeTopN = new ArrayMap<>();
    private final ArraySet<IAwareToastCallback> mCallbacks = new ArraySet<>();
    private final SparseSet mCameraUseAppList = new SparseSet();
    private final ArraySet<String> mControlGmsApp = new ArraySet<>();
    private int mCurUserId = 0;
    private final AtomicInteger mDataLoadCount = new AtomicInteger(2);
    private AppStartupDataMgr mDataMgr = null;
    private int mDbUpdateCount = 0;
    private String mDefaultInputMethod = "";
    private int mDefaultInputMethodUid = -1;
    private String mDefaultSms = "";
    private String mDefaultTts = "";
    private String mDefaultWallPaper = "";
    private int mDefaultWallPaperUid = -1;
    private int mDeviceLevel = -1;
    private int mDeviceMemoryOfGiga = -1;
    private long mDeviceTotalMemory = -1;
    private final ArraySet<String> mDozeProtectPkg = new ArraySet<>();
    private ConcurrentHashMap<Integer, PidInfo> mFgPidInfos = new ConcurrentHashMap<>();
    private final SparseSet mFrozenAppList = new SparseSet();
    private ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> mGmsAppCleanPolicyList = null;
    private ArraySet<String> mGmsAppPkg = new ArraySet<>();
    private ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> mGmsAppStartPolicyList = null;
    private ArraySet<String> mGmsCallerAppPkg = new ArraySet<>();
    private SparseSet mGmsCallerAppUid = new SparseSet();
    private final Set<String> mGoodPushSdkClsList = new ArraySet();
    private long mGoogleConnDealyTime = GOOGLE_CONN_DELAY_TIME;
    private boolean mGoogleConnStat = false;
    private boolean mGoogleConnStatDecay = false;
    private long mGoogleDisConnTime = 0;
    private List<String> mHabbitTopN = null;
    private IntlRecgHandler mHandler = null;
    private ContentObserver mHsmObserver = null;
    private final Set<String> mHwStopUserIdPkg = new ArraySet();
    private AwareBroadcastPolicy mIawareBrPolicy = null;
    private final AtomicInteger mInitWallPaperCount = new AtomicInteger(2);
    private boolean mIsAbroadArea = true;
    private final boolean mIsAppMngEnhance = SystemPropertiesEx.getBoolean("persist.sys.iaware.mngenhance", false);
    private boolean mIsChinaOperator = false;
    private AtomicBoolean mIsDozeObsvInit = new AtomicBoolean(false);
    private boolean mIsGmsCoreValid = true;
    private boolean mIsGmsPhone = true;
    private boolean mIsInitGoogleConfig = false;
    private AtomicBoolean mIsInitialized = new AtomicBoolean(false);
    private AtomicBoolean mIsObsvInit = new AtomicBoolean(false);
    private AtomicBoolean mIsScreenOn = new AtomicBoolean(true);
    private boolean mIsScreenOnPm = true;
    private AtomicLong mLastPerceptionTime = new AtomicLong(0);
    private volatile ArrayMap<Integer, Long> mLastUpdateTime = new ArrayMap<>();
    private MultiTaskManagerService mMtmService = null;
    private int mNotCleanDuration = AwareAppAssociate.ASSOC_REPORT_MIN_TIME;
    private final Set<String> mNotCleanPkgList = new ArraySet();
    private PackageManager mPackageManager = null;
    private Set<String> mPayPkgList = null;
    private PowerKit mPgSdk = null;
    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        /* class com.android.server.rms.iaware.appmng.AwareIntelligentRecg.AnonymousClass2 */
        private String mLastOperator = "";

        @Override // android.telephony.PhoneStateListener
        public void onServiceStateChanged(ServiceState ss) {
            if (ss != null && ss.getState() == 0) {
                String operatorInfo = ss.getOperatorNumeric();
                if (operatorInfo != null && !operatorInfo.equals(this.mLastOperator) && operatorInfo.length() >= 3) {
                    if (AwareIntelligentRecg.CHINA_MCC.equals(operatorInfo.substring(0, 3))) {
                        AwareIntelligentRecg.this.mIsChinaOperator = true;
                    } else {
                        AwareIntelligentRecg.this.mIsChinaOperator = false;
                    }
                }
                this.mLastOperator = operatorInfo;
            }
        }
    };
    private final Set<String> mPushSdkClsList = new ArraySet();
    private Set<String> mRecent = null;
    private final SparseArray<ArrayMap<String, Long>> mRegKeepAlivePkgs = new SparseArray<>();
    private final LinkedList<Long> mScreenChangedTime = new LinkedList<>();
    private final SparseSet mScreenRecordAppList = new SparseSet();
    private final SparseSet mScreenRecordPidList = new SparseSet();
    private ContentObserver mSettingsObserver = null;
    private Set<String> mSharePkgList = null;
    private final List<String> mSmallSampleList = new ArrayList();
    private final SparseSet mStatusAudioIn = new SparseSet();
    private final SparseSet mStatusAudioOut = new SparseSet();
    private final SparseSet mStatusGps = new SparseSet();
    private final SparseSet mStatusSensor = new SparseSet();
    private final SparseSet mStatusUpDown = new SparseSet();
    private final Map<Integer, Long> mStatusUpDownElapse = new ArrayMap();
    private int mTempRecent = 0;
    private int mTempTopN = 0;
    private final SparseArray<AwareProcessWindowInfo> mToasts = new SparseArray<>();
    private final Set<String> mTtsPkg = new ArraySet();
    private SparseSet mUnRemindAppTypeList = null;
    private long mUpdateTimeHabbitTopN = -1;
    private long mUpdateTimeRecent = -1;
    private int mWebViewUid;
    private int mWidgetCheckUpdateCnt = 5;
    private long mWidgetCheckUpdateInterval = UPDATE_DB_INTERVAL;

    public interface IAwareToastCallback {
        void onToastWindowsChanged(int i, int i2);
    }

    private AwareIntelligentRecg() {
    }

    public static AwareIntelligentRecg getInstance() {
        AwareIntelligentRecg awareIntelligentRecg;
        synchronized (LOCK) {
            if (sAwareIntlgRecg == null) {
                sAwareIntlgRecg = new AwareIntelligentRecg();
            }
            awareIntelligentRecg = sAwareIntlgRecg;
        }
        return awareIntelligentRecg;
    }

    /* access modifiers changed from: private */
    public class IntlRecgHandler extends Handler {
        public IntlRecgHandler(Looper looper) {
            super(looper);
        }

        /* JADX INFO: Multiple debug info for r0v1 int: [D('alarm' com.android.server.rms.iaware.appmng.AwareIntelligentRecg$AlarmInfo), D('uid' int)] */
        private void handleMessageBecauseOfNsiq(Message msg) {
            switch (msg.what) {
                case 8:
                case 9:
                case 10:
                    int uid = msg.arg1;
                    String pkgName = null;
                    if (msg.obj instanceof String) {
                        pkgName = (String) msg.obj;
                    }
                    AwareIntelligentRecg.this.handlePerceptionEvent(uid, pkgName, msg.what);
                    return;
                case 11:
                    AlarmInfo alarm = null;
                    if (msg.obj instanceof AlarmInfo) {
                        alarm = (AlarmInfo) msg.obj;
                    }
                    AwareIntelligentRecg.this.handleUnPerceptionEvent(alarm, msg.what);
                    return;
                case 12:
                default:
                    handleMessageEx(msg);
                    return;
                case AwareIntelligentRecg.MSG_WALLPAPER_SET /* 13 */:
                    String pkg = null;
                    if (msg.obj instanceof String) {
                        pkg = (String) msg.obj;
                    }
                    AwareIntelligentRecg.this.handleWallpaperSetMessage(pkg);
                    return;
                case 14:
                    AwareIntelligentRecg.this.initDefaultWallPaper();
                    return;
                case AwareIntelligentRecg.MSG_INPUT_METHOD_SET /* 15 */:
                    AwareIntelligentRecg.this.handleInputMethodSetMessage();
                    return;
                case 16:
                    AwareIntelligentRecg.this.checkAlarmCmpDataForUnistalled();
                    return;
                case AwareIntelligentRecg.MSG_UNFREEZE_NOT_CLEAN /* 17 */:
                    synchronized (AwareIntelligentRecg.this.mNotCleanPkgList) {
                        AwareIntelligentRecg.this.mNotCleanPkgList.clear();
                    }
                    return;
                case AwareIntelligentRecg.MSG_WAKENESS_CHANGE /* 18 */:
                    AwareIntelligentRecg.this.handleWakenessChangeHandle();
                    return;
                case AwareIntelligentRecg.MSG_UPDOWN_CLEAR /* 19 */:
                    AwareIntelligentRecg.this.handleWidgetUpDownLoadClear();
                    return;
            }
        }

        private void handleMessageEx(Message msg) {
            switch (msg.what) {
                case 20:
                    AwareIntelligentRecg.this.updateBleState();
                    return;
                case AwareIntelligentRecg.MSG_INIT_FG_PID_INFO /* 21 */:
                    if (msg.obj instanceof ArrayMap) {
                        AwareIntelligentRecg.this.initFgPidInfo((ArrayMap) msg.obj);
                        return;
                    }
                    return;
                case AwareIntelligentRecg.MSG_ADD_FG_PID_INFO /* 22 */:
                    AwareIntelligentRecg.this.updateFgPidInfo(msg.arg1, msg.arg2, true);
                    return;
                case AwareIntelligentRecg.MSG_REMOVE_FG_PID_INFO /* 23 */:
                    AwareIntelligentRecg.this.updateFgPidInfo(msg.arg1, msg.arg2, false);
                    return;
                default:
                    return;
            }
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (AwareIntelligentRecg.sDebug) {
                AwareLog.i(AwareIntelligentRecg.TAG, "handleMessage message " + msg.what);
            }
            int i = msg.what;
            if (i != 12) {
                switch (i) {
                    case 0:
                        AwareIntelligentRecg.this.handleReportAppUpdateMsg(msg);
                        return;
                    case 1:
                        AwareIntelligentRecg.this.initRecgResultInfo();
                        return;
                    case 2:
                        CmpTypeInfo cmpInfo = null;
                        if (msg.obj instanceof CmpTypeInfo) {
                            cmpInfo = (CmpTypeInfo) msg.obj;
                        }
                        AwareIntelligentRecg.this.insertCmpRecgInfo(cmpInfo);
                        return;
                    case 3:
                        CmpTypeInfo cmpInfoTmp = null;
                        if (msg.obj instanceof CmpTypeInfo) {
                            cmpInfoTmp = (CmpTypeInfo) msg.obj;
                        }
                        AwareIntelligentRecg.this.deleteCmpRecgInfo(cmpInfoTmp);
                        return;
                    case 4:
                        AwareIntelligentRecg.this.handlerRecgPush(msg);
                        return;
                    case 5:
                        AwareIntelligentRecg.this.initPgSdk();
                        return;
                    case 6:
                        if (AwareIntelligentRecg.this.mDataMgr != null) {
                            AwareIntelligentRecg.this.mDataMgr.flushBootCacheDataToDisk();
                            return;
                        }
                        return;
                    case 7:
                        AwareIntelligentRecg.this.updateKbgStatus(msg);
                        return;
                    default:
                        handleMessageBecauseOfNsiq(msg);
                        return;
                }
            } else {
                AwareIntelligentRecg.this.handleUpdateDb();
            }
        }
    }

    public static class PidInfo {
        int mPid;
        ArrayMap<String, Integer> mPkgAndType;
        int mUid;

        public PidInfo(int pid, int uid, ArrayMap<String, Integer> pkgAndType) {
            this.mPid = pid;
            this.mUid = uid;
            this.mPkgAndType = new ArrayMap<>(pkgAndType);
        }

        public PidInfo(PidInfo pidInfo) {
            this.mPid = pidInfo.mPid;
            this.mUid = pidInfo.mUid;
            this.mPkgAndType = new ArrayMap<>(pidInfo.mPkgAndType);
        }

        public int getPid() {
            return this.mPid;
        }

        public int getUid() {
            return this.mUid;
        }

        public ArrayMap<String, Integer> getPkgAndType() {
            return this.mPkgAndType;
        }

        public String toString() {
            return " pid=" + this.mPid + " uid=" + this.mUid + " pkgAndType=" + this.mPkgAndType;
        }
    }

    public void onFgPidInfosInit(ArrayMap<Integer, Integer> forePids) {
        IntlRecgHandler intlRecgHandler = this.mHandler;
        if (intlRecgHandler != null) {
            Message msg = intlRecgHandler.obtainMessage();
            msg.what = MSG_INIT_FG_PID_INFO;
            msg.obj = forePids;
            this.mHandler.sendMessage(msg);
        }
    }

    public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
        sendUpdateFgPidInfo(pid, uid, foregroundActivities);
    }

    public void onProcessDied(int pid, int uid) {
        sendUpdateFgPidInfo(pid, uid, false);
    }

    private void sendUpdateFgPidInfo(int pid, int uid, boolean isAdd) {
        IntlRecgHandler intlRecgHandler = this.mHandler;
        if (intlRecgHandler != null) {
            Message msg = intlRecgHandler.obtainMessage();
            if (isAdd) {
                msg.what = MSG_ADD_FG_PID_INFO;
            } else {
                msg.what = MSG_REMOVE_FG_PID_INFO;
            }
            msg.arg1 = pid;
            msg.arg2 = uid;
            this.mHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initFgPidInfo(ArrayMap<Integer, Integer> forePids) {
        AwareLog.d(TAG, " initFgPidInfo:" + forePids);
        for (Map.Entry<Integer, Integer> entry : forePids.entrySet()) {
            updateFgPidInfo(entry.getKey().intValue(), entry.getValue().intValue(), true);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateFgPidInfo(int pid, int uid, boolean isAdd) {
        boolean needNotifyFgChanged = false;
        AwareLog.d(TAG, " updateFgPidInfo: Pid=" + pid + "Uid=" + uid + "isAdd=" + isAdd);
        if (isAdd) {
            ArrayList<String> pkgList = getPkgListByPid(pid);
            ArrayMap<String, Integer> pkgAndType = new ArrayMap<>();
            Iterator<String> it = pkgList.iterator();
            while (it.hasNext()) {
                String pkg = it.next();
                if (pkg != null) {
                    pkgAndType.put(pkg, Integer.valueOf(AppTypeRecoManager.getInstance().getAppType(pkg)));
                }
            }
            if (!pkgAndType.isEmpty()) {
                this.mFgPidInfos.put(Integer.valueOf(pid), new PidInfo(pid, uid, pkgAndType));
                needNotifyFgChanged = true;
            }
        } else if (this.mFgPidInfos.containsKey(Integer.valueOf(pid))) {
            this.mFgPidInfos.remove(Integer.valueOf(pid));
            needNotifyFgChanged = true;
        }
        if (needNotifyFgChanged) {
            notifyFgChanged();
        }
    }

    private ArrayList<String> getPkgListByPid(int pid) {
        ProcessInfo processInfo = ProcessInfoCollector.getInstance().getProcessInfo(pid);
        if (processInfo == null || processInfo.mPackageName == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(processInfo.mPackageName);
    }

    private void notifyFgChanged() {
        AwareGameModeRecg awareGameModeRecg = this.mAwareGameModeRecg;
        AwareGameModeRecg.getInstance().doGameModeRecg(this.mFgPidInfos);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlerRecgPush(Message msg) {
        AwareAppStartInfo startInfo = null;
        if (msg.obj instanceof AwareAppStartInfo) {
            startInfo = (AwareAppStartInfo) msg.obj;
        }
        if (needRecgPushSdk(startInfo)) {
            recordStartInfo(startInfo);
            adjustPushSdkCmp(startInfo);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initPgSdk() {
        if (this.mPgSdk == null) {
            this.mPgSdk = PowerKit.getInstance();
            PowerKit powerKit = this.mPgSdk;
            if (powerKit == null) {
                delayConnectPgSdk();
                return;
            }
            try {
                powerKit.enableStateEvent(this.mAppFreezeListener, 6);
                this.mPgSdk.enableStateEvent(this.mAppFreezeListener, 7);
                this.mPgSdk.enableStateEvent(this.mAppFreezeListener, 8);
                this.mPgSdk.enableStateEvent(this.mAppFreezeListener, 2);
                this.mPgSdk.enableStateEvent(this.mAppFreezeListener, 10);
                this.mPgSdk.enableStateEvent(this.mAppFreezeListener, 100);
                this.mPgSdk.enableStateEvent(this.mAppFreezeListener, 12);
            } catch (RemoteException e) {
                AwareLog.e(TAG, "registerPGListener happend RemoteException!");
                this.mPgSdk = null;
                delayConnectPgSdk();
            }
        }
    }

    private void delayConnectPgSdk() {
        this.mHandler.removeMessages(5);
        this.mHandler.sendEmptyMessageDelayed(5, 5000);
    }

    private void unRegisterPgListener() {
        PowerKit powerKit = this.mPgSdk;
        if (powerKit != null) {
            try {
                powerKit.disableStateEvent(this.mAppFreezeListener, 6);
                this.mPgSdk.disableStateEvent(this.mAppFreezeListener, 7);
                this.mPgSdk.disableStateEvent(this.mAppFreezeListener, 8);
                this.mPgSdk.disableStateEvent(this.mAppFreezeListener, 2);
                this.mPgSdk.disableStateEvent(this.mAppFreezeListener, 10);
                this.mPgSdk.disableStateEvent(this.mAppFreezeListener, 100);
                this.mPgSdk.disableStateEvent(this.mAppFreezeListener, 12);
            } catch (RemoteException e) {
                AwareLog.e(TAG, "unRegisterPgListener  happend RemoteException!");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resolvePids(String pkgs, int uid, int eventType) {
        int state;
        if (AwareBroadcastDebug.getFilterDebug()) {
            AwareLog.i(TAG, "iaware_brFilter freeze pkgs:" + pkgs + ", uid:" + uid + ", eventType:" + eventType);
        }
        if (pkgs != null) {
            if (eventType == 1) {
                state = 1;
            } else if (eventType == 2) {
                state = 2;
            } else {
                return;
            }
            for (String str : pkgs.split(SEPARATOR_SEMICOLON)) {
                try {
                    ProcessInfoCollector.getInstance().setAwareProcessState(Integer.parseInt(str), uid, state);
                } catch (NumberFormatException e) {
                    AwareLog.e(TAG, "pid param error");
                    return;
                }
            }
        }
    }

    public void addScreenRecord(int uid, int pid) {
        synchronized (this.mScreenRecordAppList) {
            this.mScreenRecordAppList.add(uid);
        }
        synchronized (this.mScreenRecordPidList) {
            this.mScreenRecordPidList.add(pid);
        }
    }

    public void removeScreenRecord(int uid, int pid) {
        synchronized (this.mScreenRecordAppList) {
            this.mScreenRecordAppList.remove(uid);
        }
        synchronized (this.mScreenRecordPidList) {
            this.mScreenRecordPidList.remove(pid);
        }
    }

    public boolean isScreenRecord(AwareProcessInfo awareProcInfo) {
        if (!sEnabled || awareProcInfo == null) {
            return false;
        }
        return isScreenRecordEx(awareProcInfo.procProcInfo);
    }

    public boolean isScreenRecordEx(ProcessInfo procInfo) {
        boolean isScreenRecord;
        if (!sEnabled || procInfo == null) {
            return false;
        }
        synchronized (this.mScreenRecordAppList) {
            isScreenRecord = this.mScreenRecordAppList.contains(procInfo.mAppUid);
        }
        if (isScreenRecord && isSystemUid(procInfo.mAppUid)) {
            synchronized (this.mScreenRecordPidList) {
                isScreenRecord = this.mScreenRecordPidList.contains(procInfo.mPid);
            }
        }
        return isScreenRecord;
    }

    public void removeDiedScreenProc(int uid, int pid) {
        synchronized (this.mScreenRecordPidList) {
            if (this.mScreenRecordPidList.contains(pid)) {
                this.mScreenRecordPidList.remove(pid);
                synchronized (this.mScreenRecordAppList) {
                    this.mScreenRecordAppList.remove(uid);
                }
            }
        }
    }

    public void addCamera(int uid) {
        synchronized (this.mCameraUseAppList) {
            this.mCameraUseAppList.add(uid);
        }
    }

    public void removeCamera(int uid) {
        synchronized (this.mCameraUseAppList) {
            this.mCameraUseAppList.remove(uid);
        }
    }

    public boolean isCameraRecord(AwareProcessInfo awareProcInfo) {
        boolean contains;
        if (!sEnabled) {
            return false;
        }
        synchronized (this.mCameraUseAppList) {
            if (this.mCameraUseAppList.isEmpty()) {
                return false;
            }
        }
        if (awareProcInfo == null || awareProcInfo.procProcInfo == null) {
            return false;
        }
        int uid = awareProcInfo.procProcInfo.mAppUid;
        if (AwareAppAssociate.getInstance().isForeGroundApp(uid) || !AwareAppAssociate.getInstance().hasWindow(uid)) {
            return false;
        }
        synchronized (this.mCameraUseAppList) {
            contains = this.mCameraUseAppList.contains(uid);
        }
        return contains;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleReportAppUpdateMsg(Message msg) {
        int eventId = msg.arg1;
        Bundle args = msg.getData();
        if (eventId == 1) {
            String pkgName = args.getString(AwareUserHabit.USER_HABIT_PACKAGE_NAME);
            if (pkgName != null) {
                initializeForInstallApp(pkgName);
                updateIsGmsCoreValid(pkgName);
            }
        } else if (eventId == 2) {
            String pkgName2 = args.getString(AwareUserHabit.USER_HABIT_PACKAGE_NAME);
            int uid = args.getInt("uid");
            int userId = UserHandleEx.getUserId(uid);
            AwareComponentPreloadManager.getInstance().reportUninstallApp(pkgName2, userId);
            if (pkgName2 != null && userId == this.mCurUserId) {
                initializeForUninstallApp(pkgName2);
                clearAlarmForUninstallApp(pkgName2, uid);
                setHwStopFlag(userId, pkgName2, false);
                updateAllowStartPkgs(userId, pkgName2, false);
                updateIsGmsCoreValid(pkgName2);
            } else if (this.mCurUserId == 0 && isCloneUserId(userId)) {
                setHwStopFlag(userId, pkgName2, false);
            }
        }
    }

    private void initialize() {
        if (!this.mIsInitialized.get()) {
            if (this.mMtmService == null) {
                this.mMtmService = MultiTaskManagerService.self();
            }
            if (this.mMtmService != null) {
                DecisionMaker.getInstance().updateRule(AppMngConstant.AppMngFeature.APP_START, this.mMtmService.context());
                DecisionMaker.getInstance().updateRule(AppMngConstant.AppMngFeature.COMMON, this.mMtmService.context());
                if (this.mPackageManager == null) {
                    this.mPackageManager = this.mMtmService.context().getPackageManager();
                }
            }
            if (this.mHandler == null) {
                initHandler();
            }
            if (this.mSettingsObserver == null) {
                this.mSettingsObserver = new SettingsObserver(this.mHandler);
            }
            if (this.mHsmObserver == null) {
                this.mHsmObserver = new HsmObserver(this.mHandler);
            }
            try {
                UserInfoExAdapter currentUser = ActivityManagerNativeExt.getCurrentUser();
                if (currentUser != null) {
                    this.mCurUserId = currentUser.getUserId();
                }
            } catch (RemoteException e) {
                AwareLog.e(TAG, "getCurrentUserId RemoteException");
            }
            MultiTaskManagerService multiTaskManagerService = this.mMtmService;
            if (multiTaskManagerService != null) {
                unregisterContentObserver(multiTaskManagerService.context(), this.mSettingsObserver);
                registerContentObserver(this.mMtmService.context(), this.mSettingsObserver);
                unregisterHsmProviderObserver(this.mMtmService.context(), this.mHsmObserver);
                registerHsmProviderObserver(this.mMtmService.context(), this.mHsmObserver);
            }
            this.mIsAbroadArea = AwareDefaultConfigList.isAbroadArea();
            this.mDeviceLevel = DeviceInfo.getDeviceLevel();
            initializeEx();
        }
    }

    private void initializeEx() {
        initializeForUser();
        initPushSdk();
        sendLoadAppTypeMessage();
        initToastWindows();
        initPgSdk();
        initPayPkg();
        initSharePkg();
        initAlarmPkg();
        initUnRemindAppType();
        initAppStatus();
        initMinWindowSize();
        MultiTaskManagerService multiTaskManagerService = this.mMtmService;
        if (multiTaskManagerService != null) {
            updateGmsCallerAppInit(multiTaskManagerService.context());
            updateNetWorkOperatorInit(this.mMtmService.context());
            AppBatteryStrategy.getInstance().init(this.mMtmService.context());
        }
        initCommonInfo();
        AppStartupUtil.initCtsPkgList();
        initAppMngCustPropConfig();
        sendScreenOnFromPmMsg(true);
        sendMsgWidgetUpDownLoadClear();
        this.mIsInitialized.set(true);
        this.mBluetoothUid = getUidByPackageName(BLUETOOTH_PKG);
        this.mWebViewUid = getUidByPackageName(WebViewZygoteEx.getPackageName());
        initMemoryInfo();
        CloudPushManager.getInstance();
        initGmsControlInfo();
    }

    public void updateAppMngConfig() {
        initPayPkg();
        initSharePkg();
        loadPushSdk();
        initBlindPkg();
        initAlarmPkg();
        initUnRemindAppType();
        updateAccessbilityService();
        MultiTaskManagerService multiTaskManagerService = this.mMtmService;
        if (multiTaskManagerService != null) {
            updateGmsCallerAppInit(multiTaskManagerService.context());
        }
        AwareAppMngSort.getInstance().updateAppMngConfig();
        initGmsAppConfig();
        initCommonInfo();
        initGmsControlInfo();
    }

    private void loadPushSdk() {
        initPushSdk();
        loadPushSdkResultFromDb();
    }

    private List<CmpTypeInfo> readCmpTypeInfoFromDb() {
        try {
            IBinder awareService = IAwareCMSManager.getICMSManager();
            if (awareService != null) {
                return IAwareCMSManager.getCmpTypeList(awareService);
            }
            AwareLog.e(TAG, "can not find service IAwareCMSService.");
            return null;
        } catch (RemoteException e) {
            AwareLog.e(TAG, "loadRecgResultInfo RemoteException");
            return null;
        }
    }

    private void updatePushSdkClsFromDb(String cls) {
        if (isInGoodPushSdkList(cls)) {
            sendUpdateCmpInfoMessage("", cls, 0, false);
            return;
        }
        synchronized (this.mPushSdkClsList) {
            this.mPushSdkClsList.add(cls);
        }
    }

    private void loadPushSdkResultFromDb() {
        List<CmpTypeInfo> list = readCmpTypeInfoFromDb();
        if (list != null) {
            for (CmpTypeInfo info : list) {
                String cls = info.getCls();
                if (info.getType() == 0) {
                    updatePushSdkClsFromDb(cls);
                }
            }
        }
    }

    private boolean isInGoodPushSdkList(String cls) {
        if (cls == null) {
            return false;
        }
        synchronized (this.mGoodPushSdkClsList) {
            if (this.mGoodPushSdkClsList.contains(cls)) {
                return true;
            }
            return false;
        }
    }

    private void initMinWindowSize() {
        MultiTaskManagerService multiTaskManagerService = this.mMtmService;
        if (multiTaskManagerService == null) {
            AwareLog.i(TAG, "mMtmService is null for min window size");
            return;
        }
        Context context = multiTaskManagerService.context();
        if (context == null) {
            AwareLog.i(TAG, "context is null for  min window size");
            return;
        }
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        if (dm == null) {
            AwareLog.i(TAG, "dm is null for  min window size");
            return;
        }
        AwareProcessWindowInfo.setMinWindowWidth((int) (((float) AwareProcessWindowInfo.getMinWindowWidth()) * dm.density));
        AwareProcessWindowInfo.setMinWindowHeight((int) (((float) AwareProcessWindowInfo.getMinWindowHeight()) * dm.density));
        AwareLog.i(TAG, "initialize start!" + AwareProcessWindowInfo.getMinWindowWidth());
    }

    private void initializeForUser() {
        initTtsPkg();
        initSettings();
    }

    private void deInitialize() {
        if (this.mIsInitialized.get()) {
            synchronized (this.mAccessPkg) {
                this.mAccessPkg.clear();
            }
            synchronized (this.mTtsPkg) {
                this.mTtsPkg.clear();
            }
            synchronized (this.mGoodPushSdkClsList) {
                this.mGoodPushSdkClsList.clear();
            }
            synchronized (this.mPushSdkClsList) {
                this.mPushSdkClsList.clear();
            }
            synchronized (this.mAppStartInfoList) {
                this.mAppStartInfoList.clear();
            }
            synchronized (this.mToasts) {
                this.mToasts.clear();
                notifyToastChange(3, -1);
            }
            synchronized (this.mFrozenAppList) {
                this.mFrozenAppList.clear();
            }
            synchronized (this.mBluetoothAppList) {
                this.mBluetoothAppList.clear();
            }
            synchronized (this.mAudioOutInstant) {
                this.mAudioOutInstant.clear();
            }
            synchronized (this.mBlindPkg) {
                this.mBlindPkg.clear();
            }
            unRegisterPgListener();
            this.mPgSdk = null;
            synchronized (this.mRegKeepAlivePkgs) {
                this.mRegKeepAlivePkgs.clear();
            }
            synchronized (this.mAlarmMap) {
                this.mAlarmMap.clear();
            }
            synchronized (this.mAlarmCmps) {
                this.mAlarmCmps.clear();
            }
            deInitializeEx();
        }
    }

    private void deInitializeEx() {
        synchronized (this.mSmallSampleList) {
            this.mSmallSampleList.clear();
        }
        synchronized (this.mScreenChangedTime) {
            this.mScreenChangedTime.clear();
        }
        synchronized (this.mAppChangeToBgTime) {
            this.mAppChangeToBgTime.clear();
        }
        synchronized (this.mHwStopUserIdPkg) {
            this.mHwStopUserIdPkg.clear();
        }
        synchronized (this.mScreenRecordAppList) {
            this.mScreenRecordAppList.clear();
        }
        synchronized (this.mScreenRecordPidList) {
            this.mScreenRecordPidList.clear();
        }
        synchronized (this.mCameraUseAppList) {
            this.mCameraUseAppList.clear();
        }
        MultiTaskManagerService multiTaskManagerService = this.mMtmService;
        if (multiTaskManagerService != null) {
            unregisterContentObserver(multiTaskManagerService.context(), this.mSettingsObserver);
            unregisterContentObserver(this.mMtmService.context(), this.mHsmObserver);
        }
        this.mDefaultInputMethod = "";
        this.mDefaultWallPaper = "";
        this.mDefaultTts = "";
        this.mDefaultSms = "";
        deInitAppStatus();
        this.mIsInitialized.set(false);
    }

    public void initUserSwitch(int userId) {
        if (sEnabled) {
            this.mCurUserId = userId;
            initializeForUser();
            sendCheckCmpDataMessage();
        }
    }

    private void initializeForInstallApp(String pkg) {
        initSpecTtsPkg(pkg);
        updateGmsCallerAppForInstall(pkg);
    }

    private void initializeForUninstallApp(String pkg) {
        removeSpecTtsPkg(pkg);
    }

    private void clearAlarmForUninstallApp(String pkgName, int uid) {
        deleteAppCmpRecgInfo(UserHandleEx.getUserId(uid), pkgName);
        removeAlarmCmp(uid, pkgName);
        removeAlarms(uid);
    }

    private void clearAlarmForRemoveUser(int userId) {
        deleteAppCmpRecgInfo(userId, null);
        removeAlarmCmpByUserid(userId);
        removeAlarmsByUserid(userId);
    }

    private void sendLoadAppTypeMessage() {
        Message msg = this.mHandler.obtainMessage();
        msg.what = 1;
        this.mHandler.sendMessageDelayed(msg, 20000);
    }

    private void sendUpdateDbMessage() {
        Message msg = this.mHandler.obtainMessage();
        msg.what = 12;
        this.mHandler.sendMessage(msg);
    }

    private void sendUpdateCmpInfoMessage(String pkg, String cls, int type, boolean added) {
        CmpTypeInfo info = new CmpTypeInfo();
        info.setPkgName(pkg);
        info.setCls(cls);
        info.setType(type);
        info.setTime(System.currentTimeMillis());
        Message msg = this.mHandler.obtainMessage();
        msg.obj = info;
        msg.what = added ? 2 : 3;
        this.mHandler.sendMessage(msg);
    }

    private void sendRecgPushSdkMessage(int callerUid, String compName, int targetUid) {
        if (sDebug) {
            AwareLog.d(TAG, "PushSdk callerUid: " + callerUid + " cmp:" + compName + " targetUid: " + targetUid);
        }
        AwareAppStartInfo appStartInfo = new AwareAppStartInfo(callerUid, compName);
        Message msg = this.mHandler.obtainMessage();
        msg.obj = appStartInfo;
        msg.what = 4;
        this.mHandler.sendMessage(msg);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendPerceptionMessage(int msgid, int uid, String pkg) {
        Message msg = this.mHandler.obtainMessage();
        msg.what = msgid;
        msg.arg1 = uid;
        msg.obj = pkg;
        this.mHandler.sendMessage(msg);
    }

    private void sendUnPerceptionMessage(AlarmInfo alarm) {
        if (alarm != null && !AwareAppAssociate.getInstance().isForeGroundApp(alarm.uid)) {
            Message msg = this.mHandler.obtainMessage();
            msg.what = 11;
            msg.obj = alarm;
            this.mHandler.sendMessageDelayed(msg, 15000);
        }
    }

    private void sendInitWallPaperMessage() {
        Message msg = this.mHandler.obtainMessage();
        msg.what = 14;
        this.mHandler.sendMessageDelayed(msg, 20000);
    }

    private void initPushSdk() {
        ArrayList<String> badList = DecisionMaker.getInstance().getRawConfig(AppMngConstant.AppMngFeature.APP_START.getDesc(), PUSH_SDK_BAD);
        if (badList != null) {
            synchronized (this.mPushSdkClsList) {
                this.mPushSdkClsList.clear();
                this.mPushSdkClsList.addAll(badList);
            }
        }
        ArrayList<String> goodList = DecisionMaker.getInstance().getRawConfig(AppMngConstant.AppMngFeature.APP_START.getDesc(), PUSH_SDK_GOOD);
        if (goodList != null) {
            synchronized (this.mGoodPushSdkClsList) {
                this.mGoodPushSdkClsList.clear();
                this.mGoodPushSdkClsList.addAll(goodList);
            }
        }
    }

    private void initPayPkg() {
        Collection<? extends String> pkgList = DecisionMaker.getInstance().getRawConfig(AppMngConstant.AppMngFeature.APP_START.getDesc(), "payPkgList");
        if (pkgList != null) {
            Set<String> pkgSet = new ArraySet<>();
            pkgSet.addAll(pkgList);
            this.mPayPkgList = pkgSet;
        }
    }

    private void initSharePkg() {
        Collection<? extends String> pkgList = DecisionMaker.getInstance().getRawConfig(AppMngConstant.AppMngFeature.APP_START.getDesc(), "sharePkgList");
        if (pkgList != null) {
            Set<String> pkgSet = new ArraySet<>();
            pkgSet.addAll(pkgList);
            this.mSharePkgList = pkgSet;
        }
    }

    private void initAlarmPkg() {
        Collection<? extends String> alarmList = DecisionMaker.getInstance().getRawConfig(AppMngConstant.AppMngFeature.APP_START.getDesc(), "alarmPkgList");
        if (alarmList != null) {
            Set<String> alarmSet = new ArraySet<>();
            alarmSet.addAll(alarmList);
            this.mAlarmPkgList = alarmSet;
        }
    }

    private Integer[] stringToIntArray(String str) {
        if (str == null || str.isEmpty()) {
            return new Integer[0];
        }
        String[] strs = str.split(SEPARATOR_COMMA);
        Integer[] array = new Integer[strs.length];
        try {
            int length = strs.length;
            for (int i = 0; i < length; i++) {
                array[i] = Integer.valueOf(Integer.parseInt(strs[i]));
            }
            return array;
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "stringToIntArray args is illegal!");
            return new Integer[0];
        }
    }

    private void initUnRemindAppType() {
        String unRemindTag;
        int i = this.mDeviceLevel;
        if (i == 3 || i == 2) {
            unRemindTag = "unRemindAppTypesLowLevel";
        } else {
            unRemindTag = "unRemindAppTypes";
        }
        ArrayList<String> appTypeList = DecisionMaker.getInstance().getRawConfig(AppMngConstant.AppMngFeature.APP_START.getDesc(), unRemindTag);
        AwareLog.i(TAG, "initUnRemindAppType " + appTypeList);
        if (appTypeList != null) {
            SparseSet unRemindAppSet = new SparseSet();
            int size = appTypeList.size();
            for (int i2 = 0; i2 < size; i2++) {
                Integer[] types = stringToIntArray(appTypeList.get(i2));
                if (types.length > 0) {
                    List<Integer> typeList = Arrays.asList(types);
                    AwareLog.i(TAG, "initUnRemindAppType integer " + typeList);
                    unRemindAppSet.addAll(typeList);
                }
            }
            this.mUnRemindAppTypeList = unRemindAppSet;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initRecgResultInfo() {
        if (loadRecgResultInfo()) {
            AwareLog.i(TAG, "AwareIntelligentRecg load recg pkg OK ");
        } else if (this.mDataLoadCount.get() >= 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("AwareIntelligentRecg send load recg pkg message again mDataLoadCount=");
            sb.append(this.mDataLoadCount.get() - 1);
            AwareLog.i(TAG, sb.toString());
            sendLoadAppTypeMessage();
            this.mDataLoadCount.decrementAndGet();
        } else {
            AwareLog.e(TAG, "AwareIntelligentRecg recg service is error");
        }
    }

    private void initAppStatus() {
        if (this.mAwareStateCallback == null) {
            this.mAwareStateCallback = new AwareStateCallback();
            AwareAppKeyBackgroup keyBackgroupInstance = AwareAppKeyBackgroup.getInstance();
            keyBackgroupInstance.registerStateCallback(this.mAwareStateCallback, 1);
            keyBackgroupInstance.registerStateCallback(this.mAwareStateCallback, 2);
            keyBackgroupInstance.registerStateCallback(this.mAwareStateCallback, 3);
            keyBackgroupInstance.registerStateCallback(this.mAwareStateCallback, 4);
            keyBackgroupInstance.registerStateCallback(this.mAwareStateCallback, 5);
        }
    }

    private void deInitAppStatus() {
        if (this.mAwareStateCallback != null) {
            AwareAppKeyBackgroup keyBackgroupInstance = AwareAppKeyBackgroup.getInstance();
            keyBackgroupInstance.unregisterStateCallback(this.mAwareStateCallback, 1);
            keyBackgroupInstance.unregisterStateCallback(this.mAwareStateCallback, 2);
            keyBackgroupInstance.unregisterStateCallback(this.mAwareStateCallback, 3);
            keyBackgroupInstance.unregisterStateCallback(this.mAwareStateCallback, 4);
            keyBackgroupInstance.unregisterStateCallback(this.mAwareStateCallback, 5);
            this.mAwareStateCallback = null;
        }
    }

    /* access modifiers changed from: private */
    public class AwareStateCallback implements AwareAppKeyBackgroup.IAwareStateCallback {
        private AwareStateCallback() {
        }

        @Override // com.android.server.rms.iaware.appmng.AwareAppKeyBackgroup.IAwareStateCallback
        public void onStateChanged(int stateType, int eventType, int pid, int uid) {
            Message msg = AwareIntelligentRecg.this.mHandler.obtainMessage();
            msg.what = 7;
            msg.arg1 = stateType;
            msg.arg2 = eventType;
            msg.obj = Integer.valueOf(uid);
            AwareIntelligentRecg.this.mHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateKbgStatus(Message msg) {
        SparseSet status;
        int stateType = msg.arg1;
        int eventType = msg.arg2;
        if (msg.obj instanceof Integer) {
            int uid = ((Integer) msg.obj).intValue();
            if (stateType == 1) {
                status = this.mStatusAudioIn;
            } else if (stateType == 2) {
                status = this.mStatusAudioOut;
            } else if (stateType == 3) {
                status = this.mStatusGps;
            } else if (stateType == 4) {
                status = this.mStatusSensor;
            } else if (stateType == 5) {
                status = this.mStatusUpDown;
                flushUpDownLoadTrigTime(uid);
            } else {
                return;
            }
            if (eventType == 1) {
                synchronized (status) {
                    if (!status.contains(uid)) {
                        status.add(uid);
                    }
                }
            } else if (eventType == 2) {
                synchronized (status) {
                    if (status.contains(uid)) {
                        status.remove(uid);
                    }
                }
            }
        }
    }

    private boolean isAudioInStatus(int uid) {
        synchronized (this.mStatusAudioIn) {
            if (this.mStatusAudioIn.contains(uid)) {
                return true;
            }
            return false;
        }
    }

    private boolean isAudioOutStatus(int uid) {
        synchronized (this.mStatusAudioOut) {
            if (this.mStatusAudioOut.contains(uid)) {
                return true;
            }
        }
        if (AwareAppKeyBackgroup.getInstance().isAudioCache(uid)) {
            return true;
        }
        return false;
    }

    private boolean isGpsStatus(int uid) {
        synchronized (this.mStatusGps) {
            if (this.mStatusGps.contains(uid)) {
                return true;
            }
            return false;
        }
    }

    private boolean isUpDownStatus(int uid) {
        synchronized (this.mStatusUpDown) {
            if (this.mStatusUpDown.contains(uid)) {
                return true;
            }
            return false;
        }
    }

    private boolean isSensorStatus(int uid) {
        synchronized (this.mStatusSensor) {
            if (this.mStatusSensor.contains(uid)) {
                return true;
            }
            return false;
        }
    }

    private boolean loadRecgResultInfo() {
        List<CmpTypeInfo> list = readCmpTypeInfoFromDb();
        if (list == null) {
            return false;
        }
        for (CmpTypeInfo info : list) {
            if (sDebug) {
                AwareLog.d(TAG, "loadRecgResultInfo info " + info.toString());
            }
            int type = info.getType();
            if (type == 0) {
                updatePushSdkClsFromDb(info.getCls());
            } else if (type == 3 || type == 4) {
                synchronized (this.mAlarmCmps) {
                    ArrayMap<String, CmpTypeInfo> arrayMap = this.mAlarmCmps;
                    arrayMap.put(info.getUserId() + "#" + info.getPkgName() + "#" + info.getCls(), info);
                }
            }
        }
        return true;
    }

    private void sendCheckCmpDataMessage() {
        AwareLog.v(TAG, "checkCmpDataForUnistalled start0000");
        IntlRecgHandler intlRecgHandler = this.mHandler;
        if (intlRecgHandler != null) {
            if (intlRecgHandler.hasMessages(16)) {
                this.mHandler.removeMessages(16);
            }
            Message msg = this.mHandler.obtainMessage();
            msg.what = 16;
            this.mHandler.sendMessageDelayed(msg, AppHibernateCst.DELAY_ONE_MINS);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkAlarmCmpDataForUnistalled() {
        List<PackageInfo> installedApps = InnerUtils.getAllInstalledAppsInfo(this.mCurUserId);
        if (installedApps != null && !installedApps.isEmpty()) {
            checkCmpDataForUnistalled(installedApps);
            checkAlarmDataForUnistalled(installedApps);
        }
    }

    private void checkCmpDataForUnistalled(List<PackageInfo> installedApps) {
        int size = installedApps.size();
        AwareLog.v(TAG, "checkCmpDataForUnistalled app size:" + size);
        synchronized (this.mAlarmCmps) {
            Iterator<Map.Entry<String, CmpTypeInfo>> it = this.mAlarmCmps.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, CmpTypeInfo> entry = it.next();
                String key = entry.getKey();
                CmpTypeInfo value = entry.getValue();
                if (value.getUserId() == this.mCurUserId) {
                    boolean exist = false;
                    int i = 0;
                    while (true) {
                        if (i >= size) {
                            break;
                        }
                        if (key.indexOf(this.mCurUserId + "#" + installedApps.get(i).packageName + "#") == 0) {
                            exist = true;
                            break;
                        }
                        i++;
                    }
                    if (!exist) {
                        AwareLog.v(TAG, "checkCmpDataForUnistalled remove cmp:" + value);
                        it.remove();
                        deleteCmpRecgInfo(value);
                    }
                }
            }
        }
    }

    private void checkAlarmDataForUnistalled(List<PackageInfo> installedApps) {
        int size = installedApps.size();
        AwareLog.v(TAG, "checkAlarmDataForUnistalled app size:" + size);
        synchronized (this.mAlarmMap) {
            for (int index = this.mAlarmMap.size() - 1; index >= 0; index--) {
                int uid = this.mAlarmMap.keyAt(index);
                if (UserHandleEx.getUserId(uid) == this.mCurUserId) {
                    removeFromAlarmMapLocked(index, uid, installedApps, size);
                }
            }
        }
    }

    private void removeFromAlarmMapLocked(int index, int uid, List<PackageInfo> installedApps, int size) {
        boolean exist = false;
        int i = 0;
        while (true) {
            if (i < size) {
                ApplicationInfo applicationInfo = installedApps.get(i).applicationInfo;
                if (applicationInfo != null && applicationInfo.uid == uid) {
                    exist = true;
                    break;
                }
                i++;
            } else {
                break;
            }
        }
        if (!exist) {
            AwareLog.v(TAG, "checkAlarmDataForUnistalled remove uid :" + uid);
            this.mAlarmMap.removeAt(index);
        }
    }

    private void getToastWindows(SparseSet toastPids, SparseSet evilPids) {
        if (sEnabled && toastPids != null) {
            synchronized (this.mToasts) {
                for (int i = this.mToasts.size() - 1; i >= 0; i--) {
                    AwareLog.d(TAG, "getToastWindows pid:" + this.mToasts.keyAt(i));
                    if (!this.mToasts.valueAt(i).isEvil()) {
                        toastPids.add(this.mToasts.keyAt(i));
                    } else if (evilPids != null) {
                        evilPids.add(this.mToasts.keyAt(i));
                    }
                }
            }
            if (sDebug) {
                AwareLog.d(TAG, "ToastPids:" + toastPids);
            }
        }
    }

    public boolean isToastWindows(int userId, String pkg) {
        if (!sEnabled || pkg == null) {
            return true;
        }
        synchronized (this.mToasts) {
            for (int i = this.mToasts.size() - 1; i >= 0; i--) {
                AwareProcessWindowInfo toastInfo = this.mToasts.valueAt(i);
                AwareLog.i(TAG, "[isToastWindows]:" + this.mToasts.keyAt(i) + " pkg:" + pkg + " isEvil:" + toastInfo.isEvil());
                if (pkg.equals(toastInfo.pkg) && ((userId == -1 || userId == UserHandleEx.getUserId(toastInfo.uid)) && !toastInfo.isEvil())) {
                    return true;
                }
            }
            return false;
        }
    }

    public boolean isEvilToastWindow(int window, int code) {
        boolean result;
        if (!sEnabled) {
            return false;
        }
        synchronized (this.mToasts) {
            AwareProcessWindowInfo toastInfo = this.mToasts.get(window);
            if (toastInfo == null || !toastInfo.isEvil(code)) {
                result = false;
            } else {
                result = true;
            }
        }
        return result;
    }

    /* JADX WARNING: Removed duplicated region for block: B:29:0x00c8  */
    private void initToastWindows() {
        Iterator<Bundle> it;
        boolean isEvil;
        AwareProcessWindowInfo toastInfo;
        List<Bundle> windowsList = HwWindowManager.getVisibleWindows(45);
        if (windowsList == null) {
            AwareLog.w(TAG, "Catch null when initToastWindows.");
            return;
        }
        synchronized (this.mToasts) {
            this.mToasts.clear();
            notifyToastChange(3, -1);
            Iterator<Bundle> it2 = windowsList.iterator();
            while (it2.hasNext()) {
                Bundle windowState = it2.next();
                if (windowState != null) {
                    int window = windowState.getInt("window_pid");
                    int mode = windowState.getInt("window_value");
                    int code = windowState.getInt("window_state");
                    int width = windowState.getInt("window_width");
                    int height = windowState.getInt("window_height");
                    float alpha = windowState.getFloat("window_alpha");
                    boolean isHide = windowState.getBoolean("window_hidden");
                    String pkg = windowState.getString("window_package");
                    int uid = windowState.getInt("window_uid");
                    if (sDebug) {
                        StringBuilder sb = new StringBuilder();
                        it = it2;
                        sb.append("initToastWindows pid:");
                        sb.append(window);
                        sb.append(" mode:");
                        sb.append(mode);
                        sb.append(" code:");
                        sb.append(code);
                        sb.append(" width:");
                        sb.append(width);
                        sb.append(" height:");
                        sb.append(height);
                        AwareLog.i(TAG, sb.toString());
                    } else {
                        it = it2;
                    }
                    if (!(width == AwareProcessWindowInfo.getMinWindowWidth() || height == AwareProcessWindowInfo.getMinWindowHeight() || alpha == 0.0f)) {
                        if (!isHide) {
                            isEvil = false;
                            toastInfo = this.mToasts.get(window);
                            if (toastInfo == null) {
                                toastInfo = new AwareProcessWindowInfo(mode, pkg, uid);
                                this.mToasts.put(window, toastInfo);
                                notifyToastChange(5, window);
                            }
                            toastInfo.addWindow(Integer.valueOf(code), isEvil);
                            it2 = it;
                        }
                    }
                    isEvil = true;
                    toastInfo = this.mToasts.get(window);
                    if (toastInfo == null) {
                    }
                    toastInfo.addWindow(Integer.valueOf(code), isEvil);
                    it2 = it;
                }
            }
        }
    }

    private void addToast(Bundle bundleArgs) {
        int window = bundleArgs.getInt("window");
        int code = bundleArgs.getInt("hashcode");
        int width = bundleArgs.getInt("width");
        int height = bundleArgs.getInt("height");
        float alpha = bundleArgs.getFloat("alpha");
        String pkg = bundleArgs.getString(MemoryConstant.MEM_PREREAD_ITEM_NAME);
        int uid = bundleArgs.getInt("uid");
        AwareLog.i(TAG, "[addToast]:" + window + " [code]:" + code + " width:" + width + " height:" + height + " alpha:" + alpha);
        if (window > 0) {
            synchronized (this.mToasts) {
                AwareProcessWindowInfo toastInfo = this.mToasts.get(window);
                boolean isEvil = true;
                boolean isInvalidWidth = width > 0 && width <= AwareProcessWindowInfo.getMinWindowWidth();
                boolean isInvalidHeight = height > 0 && height <= AwareProcessWindowInfo.getMinWindowHeight();
                boolean isTransparent = alpha == 0.0f;
                if (!isInvalidWidth && !isInvalidHeight) {
                    if (!isTransparent) {
                        isEvil = false;
                    }
                }
                if (toastInfo == null) {
                    toastInfo = new AwareProcessWindowInfo(3, pkg, uid);
                    this.mToasts.put(window, toastInfo);
                    notifyToastChange(5, window);
                }
                toastInfo.addWindow(Integer.valueOf(code), isEvil);
            }
            if (sDebug) {
                AwareLog.i(TAG, "[addToast]:" + window);
            }
        }
    }

    private void removeToast(int window, int code) {
        AwareLog.i(TAG, "[removeToast]:" + window + " [code]:" + code);
        if (window > 0) {
            synchronized (this.mToasts) {
                AwareProcessWindowInfo toastInfo = this.mToasts.get(window);
                if (toastInfo == null) {
                    this.mToasts.remove(window);
                    return;
                }
                toastInfo.removeWindow(Integer.valueOf(code));
                if (toastInfo.windows.size() == 0) {
                    this.mToasts.remove(window);
                    notifyToastChange(4, window);
                }
            }
            if (sDebug) {
                AwareLog.i(TAG, "[removeToast]:" + window);
            }
        }
    }

    private void updateToast(Bundle bundleArgs) {
        boolean isEvil;
        int window = bundleArgs.getInt("window");
        int code = bundleArgs.getInt("hashcode");
        int width = bundleArgs.getInt("width");
        int height = bundleArgs.getInt("height");
        float alpha = bundleArgs.getFloat("alpha");
        boolean isHide = bundleArgs.getBoolean("permanentlyhidden");
        AwareLog.i(TAG, "[updateToast]:" + window + " [code]:" + code + " width:" + width + " height:" + height + " alpha:" + alpha);
        if (window > 0) {
            if (width >= 0 || height >= 0) {
                synchronized (this.mToasts) {
                    AwareProcessWindowInfo toastInfo = this.mToasts.get(window);
                    if (width > AwareProcessWindowInfo.getMinWindowWidth() && height > AwareProcessWindowInfo.getMinWindowHeight() && alpha != 0.0f) {
                        if (!isHide) {
                            isEvil = false;
                            if (toastInfo != null && toastInfo.containsWindow(code)) {
                                toastInfo.addWindow(Integer.valueOf(code), isEvil);
                            }
                        }
                    }
                    isEvil = true;
                    toastInfo.addWindow(Integer.valueOf(code), isEvil);
                }
                if (sDebug) {
                    AwareLog.i(TAG, "[updateToast]:" + window);
                }
            }
        }
    }

    private void hideToast(int window, int code) {
        AwareLog.i(TAG, "[hideToast]:" + window + " [code]:" + code);
        if (window > 0) {
            synchronized (this.mToasts) {
                AwareProcessWindowInfo toastInfo = this.mToasts.get(window);
                if (toastInfo != null && toastInfo.containsWindow(code)) {
                    toastInfo.addWindow(Integer.valueOf(code), true);
                }
            }
            if (sDebug) {
                AwareLog.i(TAG, "[hideToast]:" + window);
            }
        }
    }

    private Map<String, CmpTypeInfo> getAlarmCmpFromDb() {
        Map<String, CmpTypeInfo> result = new ArrayMap<>();
        List<CmpTypeInfo> cmplist = null;
        try {
            IBinder awareService = IAwareCMSManager.getICMSManager();
            if (awareService != null) {
                cmplist = IAwareCMSManager.getCmpTypeList(awareService);
            } else {
                AwareLog.e(TAG, "can not find service IAwareCMSService.");
            }
        } catch (RemoteException e) {
            AwareLog.e(TAG, "getAlarmCmpFromDb RemoteException");
        }
        if (cmplist == null) {
            return result;
        }
        for (CmpTypeInfo info : cmplist) {
            if (info != null && (info.getType() == 3 || info.getType() == 4)) {
                result.put(info.getUserId() + "#" + info.getPkgName() + "#" + info.getCls(), info);
            }
        }
        return result;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean insertCmpRecgInfo(CmpTypeInfo info) {
        try {
            IBinder awareService = IAwareCMSManager.getICMSManager();
            if (awareService != null) {
                IAwareCMSManager.insertCmpTypeInfo(awareService, info);
                return true;
            }
            AwareLog.e(TAG, "can not find service IAwareCMSService.");
            return false;
        } catch (RemoteException e) {
            AwareLog.e(TAG, "inSertCmpRecgInfo RemoteException");
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean deleteCmpRecgInfo(CmpTypeInfo info) {
        try {
            IBinder awareService = IAwareCMSManager.getICMSManager();
            if (awareService != null) {
                IAwareCMSManager.deleteCmpTypeInfo(awareService, info);
                return true;
            }
            AwareLog.e(TAG, "can not find service IAwareCMSService.");
            return false;
        } catch (RemoteException e) {
            AwareLog.e(TAG, "deleteCmpRecgInfo RemoteException");
            return false;
        }
    }

    private boolean deleteAppCmpRecgInfo(int userId, String pkg) {
        try {
            IBinder awareService = IAwareCMSManager.getICMSManager();
            if (awareService != null) {
                IAwareCMSManager.deleteAppCmpTypeInfo(awareService, userId, pkg);
                AwareLog.e(TAG, "delete pkg:" + pkg + " userId:" + userId + " from iAware.db");
                return true;
            }
            AwareLog.e(TAG, "can not find service IAwareCMSService.");
            return false;
        } catch (RemoteException e) {
            AwareLog.e(TAG, "deleteAppCmpRecgInfo RemoteException");
            return false;
        }
    }

    public void report(int eventId, Bundle bundleArgs) {
        if (sEnabled) {
            if (sDebug) {
                AwareLog.d(TAG, "eventId: " + eventId);
            }
            if (bundleArgs != null) {
                if (!this.mIsInitialized.get()) {
                    initialize();
                }
                if (eventId == 8) {
                    addToast(bundleArgs);
                } else if (eventId == 9) {
                    removeToast(bundleArgs.getInt("window"), bundleArgs.getInt("hashcode"));
                } else if (eventId == MSG_UNFREEZE_NOT_CLEAN) {
                    reportWallpaper(bundleArgs.getString(MemoryConstant.MEM_PREREAD_ITEM_NAME));
                } else if (eventId == 20) {
                    sendPerceptionMessage(9, bundleArgs.getInt("tgtUid"), bundleArgs.getString(MemoryConstant.MEM_PREREAD_ITEM_NAME));
                } else if (eventId == MSG_ADD_FG_PID_INFO) {
                    reportAlarm(bundleArgs.getString(MemoryConstant.MEM_PREREAD_ITEM_NAME), bundleArgs.getString("statstag"), bundleArgs.getInt("alarm_operation"), bundleArgs.getInt("tgtUid"));
                } else if (eventId == 27) {
                    updateToast(bundleArgs);
                } else if (eventId != 28) {
                    reportBecauseOfNsiq(eventId, bundleArgs);
                } else {
                    hideToast(bundleArgs.getInt("window"), bundleArgs.getInt("hashcode"));
                }
            }
        } else if (sDebug) {
            AwareLog.d(TAG, "AwareIntelligentRecg feature disabled!");
        }
    }

    private void reportHandleScreen(int eventId, Bundle bundleArgs) {
        if (eventId == 20011) {
            this.mIsScreenOn.set(true);
            AwareGameModeRecg.getInstance().doGameModeRecg(this.mFgPidInfos);
        } else if (eventId == 20022) {
            AwareGameModeRecg.getInstance().doGameModeRecg(this.mFgPidInfos);
        } else if (eventId == 90011) {
            this.mIsScreenOn.set(false);
        }
    }

    private void reportBecauseOfNsiq(int eventId, Bundle bundleArgs) {
        if (bundleArgs != null) {
            if (eventId == MSG_UPDOWN_CLEAR) {
                sendPerceptionMessage(8, bundleArgs.getInt("tgtUid"), null);
            } else if (eventId == 29) {
                int userId = bundleArgs.getInt("userid");
                if (userId != -10000) {
                    clearAlarmForRemoveUser(userId);
                    removeHwStopFlagByUserId(userId);
                }
            } else if (eventId == 20011 || eventId == 20022 || eventId == 90011) {
                reportHandleScreen(eventId, bundleArgs);
            } else if (eventId == 35) {
                AwareAudioFocusManager manager = AwareAudioFocusManager.getInstance();
                if (manager != null) {
                    manager.reportAudioFocusRequest(bundleArgs.getInt("state_type"), bundleArgs.getInt("callUid"), bundleArgs.getString("request_name"));
                }
            } else if (eventId == 36) {
                AwareAudioFocusManager manager2 = AwareAudioFocusManager.getInstance();
                if (manager2 != null) {
                    manager2.reportAudioFocusLoss(bundleArgs.getInt("state_type"), bundleArgs.getInt("callUid"), bundleArgs.getString("request_name"));
                }
            } else if (sDebug) {
                AwareLog.e(TAG, "Unknown EventID: " + eventId);
            }
        }
    }

    public void reportAppUpdate(int eventId, Bundle args) {
        IntlRecgHandler intlRecgHandler;
        if (sEnabled && args != null && (intlRecgHandler = this.mHandler) != null) {
            Message msg = intlRecgHandler.obtainMessage();
            msg.what = 0;
            msg.arg1 = eventId;
            msg.setData(args);
            this.mHandler.sendMessage(msg);
        }
    }

    private void removeSpecTtsPkg(String pkg) {
        synchronized (this.mTtsPkg) {
            this.mTtsPkg.remove(pkg);
        }
    }

    private List<ResolveInfo> queryTtsService(String pkg) {
        if (this.mPackageManager == null) {
            return null;
        }
        Intent intent = new Intent("android.intent.action.TTS_SERVICE");
        if (pkg != null) {
            intent.setPackage(pkg);
        }
        return PackageManagerExt.queryIntentServicesAsUser(this.mPackageManager, intent, 851968, this.mCurUserId);
    }

    private void initSpecTtsPkg(String pkg) {
        List<ResolveInfo> resolveInfos = queryTtsService(pkg);
        if (resolveInfos != null && resolveInfos.size() > 0) {
            synchronized (this.mTtsPkg) {
                this.mTtsPkg.add(pkg);
            }
        }
    }

    private void initTtsPkg() {
        List<ResolveInfo> resolveInfos = queryTtsService(null);
        Set<String> ttsPkg = new ArraySet<>();
        if (resolveInfos != null && resolveInfos.size() > 0) {
            for (ResolveInfo ri : resolveInfos) {
                String pkg = ResolveInfoEx.getComponentInfo(ri).packageName;
                if (!ttsPkg.contains(pkg)) {
                    ttsPkg.add(pkg);
                }
            }
        }
        synchronized (this.mTtsPkg) {
            this.mTtsPkg.clear();
            this.mTtsPkg.addAll(ttsPkg);
        }
    }

    private void initSettings() {
        this.mDefaultInputMethod = readDefaultInputMethod();
        sendInputMethodSetMessage();
        initBlindPkg();
        updateAccessbilityService();
        updateDefaultTts();
        initDefaultWallPaper();
        this.mDefaultSms = readDefaultSmsPackage();
        updateDozeProtectList();
        sendBleStatusUpdate();
    }

    private void initBlindPkg() {
        ArrayList<String> blindList = DecisionMaker.getInstance().getRawConfig(AppMngConstant.AppMngFeature.APP_START.getDesc(), BLIND_PREFABRICATED);
        if (blindList != null) {
            synchronized (this.mBlindPkg) {
                this.mBlindPkg.clear();
                this.mBlindPkg.addAll(blindList);
            }
        }
    }

    /* access modifiers changed from: private */
    public class SettingsObserver extends ContentObserver {
        public SettingsObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri) {
            if (Settings.Secure.getUriFor("default_input_method").equals(uri)) {
                AwareIntelligentRecg awareIntelligentRecg = AwareIntelligentRecg.this;
                awareIntelligentRecg.mDefaultInputMethod = awareIntelligentRecg.readDefaultInputMethod();
                AwareIntelligentRecg.this.sendInputMethodSetMessage();
            } else if (Settings.Secure.getUriFor("enabled_accessibility_services").equals(uri)) {
                AwareIntelligentRecg.this.updateAccessbilityService();
            } else if (Settings.Secure.getUriFor("tts_default_synth").equals(uri)) {
                AwareIntelligentRecg.this.updateDefaultTts();
            } else if (Settings.Secure.getUriFor(AwareIntelligentRecg.VALUE_BT_BLE_CONNECT_APPS).equals(uri)) {
                AwareIntelligentRecg.this.updateBluetoothConnect();
            } else if (Settings.Secure.getUriFor(AwareIntelligentRecg.VALUE_BT_LAST_BLE_DISCONNECT).equals(uri)) {
                AwareIntelligentRecg.this.updateBluetoothLastDisconnect();
            } else if (Settings.Secure.getUriFor("sms_default_application").equals(uri)) {
                AwareIntelligentRecg awareIntelligentRecg2 = AwareIntelligentRecg.this;
                awareIntelligentRecg2.mDefaultSms = awareIntelligentRecg2.readDefaultSmsPackage();
            } else if (Settings.Secure.getUriFor("bluetooth_on").equals(uri)) {
                AwareIntelligentRecg.this.sendBleStatusUpdate();
            } else if (Settings.Global.getUriFor("unified_device_name").equals(uri)) {
                AwareMiddleware.getInstance().onProfileChanged();
            }
        }
    }

    /* access modifiers changed from: private */
    public class HsmObserver extends ContentObserver {
        public HsmObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri) {
            AwareIntelligentRecg.this.updateDozeProtectList();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateAccessbilityService() {
        MultiTaskManagerService multiTaskManagerService = this.mMtmService;
        if (multiTaskManagerService != null) {
            String settingValue = SettingsEx.Secure.getStringForUser(multiTaskManagerService.context().getContentResolver(), "enabled_accessibility_services", this.mCurUserId);
            if (sDebug) {
                AwareLog.i(TAG, "Accessbility:" + settingValue);
            }
            Set<String> accessPkg = new ArraySet<>();
            if (settingValue != null && !"".equals(settingValue)) {
                String[] accessbilityServices = settingValue.split(SEPARATOR_SEMICOLON);
                for (String accessbilityService : accessbilityServices) {
                    int indexOf = accessbilityService.indexOf(47);
                    if (indexOf > 0) {
                        accessPkg.add(accessbilityService.substring(0, indexOf));
                    }
                }
            }
            synchronized (this.mAccessPkg) {
                this.mAccessPkg.clear();
                this.mAccessPkg.addAll(accessPkg);
            }
            Set<String> blindPkgs = new ArraySet<>();
            for (String access : accessPkg) {
                synchronized (this.mTtsPkg) {
                    if (this.mTtsPkg.contains(access)) {
                        blindPkgs.add(access);
                    }
                }
                synchronized (this.mBlindPkg) {
                    if (this.mBlindPkg.contains(access)) {
                        blindPkgs.add(access);
                    }
                }
            }
            AppStartupDataMgr appStartupDataMgr = this.mDataMgr;
            if (appStartupDataMgr != null) {
                appStartupDataMgr.updateBlind(blindPkgs);
                AppStatusUtils.getInstance().updateBlind(blindPkgs);
                sendFlushToDiskMessage();
            }
        }
    }

    private void sendFlushToDiskMessage() {
        IntlRecgHandler intlRecgHandler = this.mHandler;
        if (intlRecgHandler != null) {
            Message msg = intlRecgHandler.obtainMessage();
            msg.what = 6;
            this.mHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateDefaultTts() {
        MultiTaskManagerService multiTaskManagerService = this.mMtmService;
        if (multiTaskManagerService != null) {
            String settingValue = SettingsEx.Secure.getStringForUser(multiTaskManagerService.context().getContentResolver(), "tts_default_synth", this.mCurUserId);
            if (sDebug) {
                AwareLog.i(TAG, "Default TTS :" + settingValue);
            }
            this.mDefaultTts = settingValue;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateBluetoothConnect() {
        MultiTaskManagerService multiTaskManagerService = this.mMtmService;
        if (multiTaskManagerService != null) {
            String settingValue = SettingsEx.Secure.getStringForUser(multiTaskManagerService.context().getContentResolver(), VALUE_BT_BLE_CONNECT_APPS, this.mCurUserId);
            if (sDebug) {
                AwareLog.i(TAG, "Bluetooth connect :" + settingValue);
            }
            synchronized (this.mBluetoothLock) {
                this.mBtoothConnectList.clear();
                if (settingValue != null) {
                    if (!"".equals(settingValue)) {
                        for (String strPid : settingValue.split("\\|")) {
                            try {
                                this.mBtoothConnectList.add(Integer.valueOf(Integer.parseInt(strPid)));
                            } catch (NumberFormatException e) {
                                AwareLog.i(TAG, "updateBluetoothConnect error");
                            }
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateBluetoothLastDisconnect() {
        MultiTaskManagerService multiTaskManagerService = this.mMtmService;
        if (multiTaskManagerService != null) {
            String settingValue = SettingsEx.Secure.getStringForUser(multiTaskManagerService.context().getContentResolver(), VALUE_BT_LAST_BLE_DISCONNECT, this.mCurUserId);
            if (sDebug) {
                AwareLog.i(TAG, "Bluetooth last disconnect :" + settingValue);
            }
            synchronized (this.mBluetoothLock) {
                this.mBtoothLastPid = -1;
                if (settingValue != null) {
                    if (!"".equals(settingValue)) {
                        String[] strings = settingValue.split("\\|");
                        if (strings.length == 2) {
                            try {
                                this.mBtoothLastPid = Integer.parseInt(strings[0]);
                                this.mBtoothLastTime = Long.parseLong(strings[1]);
                            } catch (NumberFormatException e) {
                                this.mBtoothLastPid = -1;
                                AwareLog.i(TAG, "updateBluetoothLastDisconnect error");
                            }
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateDozeProtectList() {
        if (this.mMtmService != null) {
            int curUserId = AwareAppAssociate.getInstance().getCurUserId();
            try {
                Bundle dozeProtectBundle = this.mMtmService.context().getContentResolver().call(Uri.parse(URI_PREFIX + curUserId + URI_SYSTEM_MANAGER_SMART_PROVIDER), TAG_GET_DOZE_LIST, AppMngRule.VALUE_ALL, (Bundle) null);
                if (dozeProtectBundle == null) {
                    AwareLog.i(TAG, "updateDozeProtectList failed: not implemented !");
                    return;
                }
                ArrayList<String> dozeProtectApps = dozeProtectBundle.getStringArrayList(TAG_DOZE_PROTECT);
                if (dozeProtectApps != null) {
                    synchronized (this.mDozeProtectPkg) {
                        this.mDozeProtectPkg.clear();
                        this.mDozeProtectPkg.addAll(dozeProtectApps);
                    }
                } else {
                    AwareLog.i(TAG, "updateDozeProtectList failed: no pkg found !");
                }
                AwareLog.i(TAG, "updateDozeProtectList :" + dozeProtectApps);
            } catch (IllegalArgumentException e) {
                AwareLog.e(TAG, "updateDozeProtectList failed: illegal argument !");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String readDefaultInputMethod() {
        String inputInfo;
        MultiTaskManagerService multiTaskManagerService = this.mMtmService;
        if (!(multiTaskManagerService == null || (inputInfo = SettingsEx.Secure.getStringForUser(multiTaskManagerService.context().getContentResolver(), "default_input_method", this.mCurUserId)) == null)) {
            String[] defaultIms = inputInfo.split("/");
            if (defaultIms[0] != null) {
                return defaultIms[0];
            }
        }
        return "";
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initDefaultWallPaper() {
        String pkg = readDefaultWallPaper();
        if (pkg != null) {
            AwareLog.i(TAG, "initDefaultWallPaper  pkg =" + pkg);
            sendWallpaperSetMessage(pkg);
        } else if (this.mInitWallPaperCount.get() >= 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("AwareIntelligentRecg send read wallpaper message again mInitWallPaperCount=");
            sb.append(this.mInitWallPaperCount.get() - 1);
            AwareLog.i(TAG, sb.toString());
            sendInitWallPaperMessage();
            this.mInitWallPaperCount.decrementAndGet();
        } else {
            AwareLog.e(TAG, "AwareIntelligentRecg has no wallpaper");
        }
    }

    private String readDefaultWallPaper() {
        WallpaperInfo wallpaperInfo = null;
        try {
            wallpaperInfo = IWallpaperManagerExt.getWallpaperInfo(UserHandleEx.myUserId());
        } catch (RemoteException e) {
            AwareLog.e(TAG, "Couldn't read  Default WallPaper");
        }
        if (wallpaperInfo != null) {
            return wallpaperInfo.getPackageName();
        }
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String readDefaultSmsPackage() {
        String inputInfo;
        MultiTaskManagerService multiTaskManagerService = this.mMtmService;
        if (!(multiTaskManagerService == null || (inputInfo = SettingsEx.Secure.getStringForUser(multiTaskManagerService.context().getContentResolver(), "sms_default_application", this.mCurUserId)) == null)) {
            String[] defaultIms = inputInfo.split("/");
            if (defaultIms[0] != null) {
                return defaultIms[0];
            }
        }
        return "";
    }

    private boolean isTtsPkg(String pkg) {
        if (!sEnabled) {
            return false;
        }
        synchronized (this.mTtsPkg) {
            if (this.mTtsPkg.contains(pkg)) {
                return true;
            }
            return false;
        }
    }

    private boolean isPayPkg(String pkg, AwareAppStartStatusCache status) {
        if (!sEnabled) {
            return false;
        }
        Set<String> set = this.mPayPkgList;
        if (set == null || !set.contains(pkg)) {
            return isAttrApp(pkg, status, 1);
        }
        return true;
    }

    private boolean isSharePkg(String pkg, AwareAppStartStatusCache status) {
        if (!sEnabled) {
            return false;
        }
        Set<String> set = this.mSharePkgList;
        if (set == null || !set.contains(pkg)) {
            return isAttrApp(pkg, status, 2);
        }
        return true;
    }

    private boolean isAttrApp(String pkg, AwareAppStartStatusCache status, int attr) {
        int appAttri;
        if (sEnabled && (appAttri = getAttributeCache(pkg, status)) != -1 && (appAttri & attr) == attr) {
            return true;
        }
        return false;
    }

    private int getAttributeCache(String pkg, AwareAppStartStatusCache status) {
        int appAttri = status.cacheAppAttribute;
        if (appAttri != -1) {
            return appAttri;
        }
        int appAttri2 = AppTypeRecoManager.getInstance().getAppAttribute(pkg);
        status.cacheAppAttribute = appAttri2;
        return appAttri2;
    }

    public String getDefaultInputMethod() {
        return this.mDefaultInputMethod;
    }

    public String getDefaultWallPaper() {
        return this.mDefaultWallPaper;
    }

    public String getDefaultSmsPackage() {
        return this.mDefaultSms;
    }

    private void reportWallpaper(String pkg) {
        if (sDebug) {
            AwareLog.i(TAG, "reportWallpaper  pkg=" + pkg);
        }
        if (pkg != null && !pkg.isEmpty()) {
            sendWallpaperSetMessage(pkg);
        }
    }

    public int getDefaultInputMethodUid() {
        return this.mDefaultInputMethodUid;
    }

    private int getDefaultWallPaperUid() {
        return this.mDefaultWallPaperUid;
    }

    private void sendWallpaperSetMessage(String pkg) {
        Message msg = this.mHandler.obtainMessage();
        msg.what = MSG_WALLPAPER_SET;
        msg.obj = pkg;
        this.mHandler.sendMessage(msg);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleWallpaperSetMessage(String pkg) {
        this.mDefaultWallPaper = pkg;
        this.mDefaultWallPaperUid = getUidByPackageName(pkg);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendInputMethodSetMessage() {
        IntlRecgHandler intlRecgHandler = this.mHandler;
        if (intlRecgHandler != null) {
            Message msg = intlRecgHandler.obtainMessage();
            msg.what = MSG_INPUT_METHOD_SET;
            this.mHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleInputMethodSetMessage() {
        this.mDefaultInputMethodUid = getUidByPackageName(this.mDefaultInputMethod);
        AwareBinderSchedManager.getInstance().reportDefaultInputMethod(this.mDefaultInputMethodUid, this.mDefaultInputMethod);
    }

    private int getUidByPackageName(String pkg) {
        MultiTaskManagerService multiTaskManagerService = this.mMtmService;
        if (multiTaskManagerService == null) {
            return -1;
        }
        return getUidByPackageName(multiTaskManagerService.context(), pkg);
    }

    private int getUidByPackageName(Context context, String pkg) {
        PackageManager pm;
        if (pkg == null || context == null || (pm = context.getPackageManager()) == null) {
            return -1;
        }
        try {
            ApplicationInfo appInfo = PackageManagerExt.getApplicationInfoAsUser(pm, pkg, 0, this.mCurUserId);
            if (appInfo != null) {
                return appInfo.uid;
            }
            return -1;
        } catch (PackageManager.NameNotFoundException e) {
            AwareLog.e(TAG, "get the package uid faied");
            return -1;
        }
    }

    private void recordStartInfo(AwareAppStartInfo startInfo) {
        if (startInfo != null) {
            ArrayList<AwareAppStartInfo> mRemoveList = new ArrayList<>();
            long currentTime = SystemClock.elapsedRealtime();
            synchronized (this.mAppStartInfoList) {
                for (AwareAppStartInfo item : this.mAppStartInfoList) {
                    if (currentTime - item.timeStamp > 20000) {
                        mRemoveList.add(item);
                    }
                }
                this.mAppStartInfoList.add(startInfo);
                if (mRemoveList.size() > 0) {
                    this.mAppStartInfoList.removeAll(mRemoveList);
                }
            }
        }
    }

    private boolean isExist(ArraySet<AwareAppStartInfo> infos, AwareAppStartInfo startInfo) {
        if (infos == null || startInfo == null) {
            return false;
        }
        Iterator<AwareAppStartInfo> it = infos.iterator();
        while (it.hasNext()) {
            if (startInfo.equals(it.next())) {
                return true;
            }
        }
        return false;
    }

    private boolean needRecgPushSdk(AwareAppStartInfo startInfo) {
        if (startInfo == null || startInfo.cls == null) {
            return false;
        }
        synchronized (this.mGoodPushSdkClsList) {
            if (this.mGoodPushSdkClsList.contains(startInfo.cls)) {
                return false;
            }
            return true;
        }
    }

    private void adjustPushSdkCmp(AwareAppStartInfo startInfo) {
        if (startInfo != null) {
            ArrayMap<Integer, ArraySet<AwareAppStartInfo>> mStartMap = new ArrayMap<>();
            long currentTime = SystemClock.elapsedRealtime();
            synchronized (this.mAppStartInfoList) {
                for (AwareAppStartInfo item : this.mAppStartInfoList) {
                    if (currentTime - item.timeStamp < 20000) {
                        if (!mStartMap.containsKey(Integer.valueOf(item.callerUid))) {
                            ArraySet<AwareAppStartInfo> initset = new ArraySet<>();
                            initset.add(item);
                            mStartMap.put(Integer.valueOf(item.callerUid), initset);
                        } else if (!isExist(mStartMap.get(Integer.valueOf(item.callerUid)), item)) {
                            mStartMap.get(Integer.valueOf(item.callerUid)).add(item);
                        }
                    }
                }
            }
            ArrayMap<String, Integer> clsMap = new ArrayMap<>();
            for (Map.Entry<Integer, ArraySet<AwareAppStartInfo>> map : mStartMap.entrySet()) {
                int uid = map.getKey().intValue();
                ArraySet<AwareAppStartInfo> sameCallerStartInfos = map.getValue();
                if (sDebug) {
                    AwareLog.d(TAG, "PushSdk check cls uid:" + uid + " size:" + sameCallerStartInfos.size());
                }
                clsMap.clear();
                Iterator<AwareAppStartInfo> it = sameCallerStartInfos.iterator();
                while (it.hasNext()) {
                    AwareAppStartInfo item2 = it.next();
                    if (item2.cls != null) {
                        if (sDebug) {
                            AwareLog.d(TAG, "PushSdk check cls :" + item2.cls);
                        }
                        if (!clsMap.containsKey(item2.cls)) {
                            clsMap.put(item2.cls, 1);
                        } else {
                            int count = clsMap.get(item2.cls).intValue();
                            clsMap.put(item2.cls, Integer.valueOf(count + 1));
                            updatePushSdkCls(item2.cls, count + 1);
                        }
                    }
                }
            }
        }
    }

    private void updatePushSdkCls(String cls, int count) {
        if (count >= 2) {
            synchronized (this.mPushSdkClsList) {
                if (!this.mPushSdkClsList.contains(cls)) {
                    this.mPushSdkClsList.add(cls);
                    sendUpdateCmpInfoMessage("", cls, 0, true);
                }
            }
        } else if (sDebug) {
            AwareLog.d(TAG, "PushSdk start cls count must be > :2");
        }
    }

    private boolean isUnReminderApp(List<String> packageList) {
        if (packageList == null) {
            return false;
        }
        int packageListSize = packageList.size();
        for (int i = 0; i < packageListSize; i++) {
            if (!isUnReminderApp(packageList.get(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean isUnReminderApp(String pkgName) {
        int type = AppTypeRecoManager.getInstance().getAppType(pkgName);
        if (sDebug) {
            AwareLog.d(TAG, "getAppType From Habit pkg " + pkgName + " type : " + type);
        }
        SparseSet sparseSet = this.mUnRemindAppTypeList;
        if (sparseSet == null || !sparseSet.contains(type)) {
            return false;
        }
        int appAttr = AppTypeRecoManager.getInstance().getAppAttribute(pkgName);
        if (appAttr == -1) {
            return true;
        }
        if ((appAttr & 4) != 4) {
            return true;
        }
        return false;
    }

    private void initAlarm(AlarmInfo alarm, CmpTypeInfo alarmRecgInfo, boolean isAdd, boolean isWakeup, long time) {
        int status = 0;
        boolean z = true;
        if (alarmRecgInfo != null) {
            status = alarmRecgInfo.getType() == 4 ? 2 : 1;
        }
        alarm.status = status;
        alarm.count = isAdd ? 1 : 0;
        alarm.startTime = isWakeup ? time : 0;
        alarm.perceptionCount = alarmRecgInfo != null ? alarmRecgInfo.getPerceptionCount() : 0;
        alarm.unPerceptionCount = alarmRecgInfo != null ? alarmRecgInfo.getUnPerceptionCount() : 0;
        if (isAdd) {
            if (!AwareAppAssociate.getInstance().isForeGroundApp(alarm.uid) || !this.mIsScreenOn.get()) {
                z = false;
            }
            alarm.fgSet = z;
        }
    }

    private boolean checkAlarmInfo(String packageName, String statustag, int uid) {
        if (UserHandleEx.getAppId(uid) < 10000 || packageName == null || statustag == null || isUnReminderApp(packageName)) {
            return false;
        }
        Set<String> set = this.mAlarmPkgList;
        if (set == null || !set.contains(packageName)) {
            return true;
        }
        return false;
    }

    private void reportAlarm(String packageName, String statustag, int operation, int uid) {
        SparseArray<ArrayMap<String, AlarmInfo>> sparseArray;
        Throwable th;
        AlarmInfo alarm;
        long time;
        if (checkAlarmInfo(packageName, statustag, uid)) {
            String[] strs = statustag.split(SEPARATOR_SEMICOLON);
            if (strs.length == 2) {
                boolean isWakeup = true;
                String tag = strs[1];
                if (sDebug) {
                    AwareLog.i(TAG, "reportAlarm for clock app pkg : " + packageName + " tag : " + tag + " uid : " + uid + " operation : " + operation);
                }
                CmpTypeInfo alarmRecgInfo = getAlarmRecgResult(uid, packageName, tag);
                boolean isAdd = operation == 0;
                if (operation != 2) {
                    isWakeup = false;
                }
                long time2 = SystemClock.elapsedRealtime();
                SparseArray<ArrayMap<String, AlarmInfo>> sparseArray2 = this.mAlarmMap;
                synchronized (sparseArray2) {
                    try {
                        ArrayMap<String, AlarmInfo> alarms = this.mAlarmMap.get(uid);
                        if (alarms == null) {
                            try {
                                ArrayMap<String, AlarmInfo> alarms2 = new ArrayMap<>();
                                AlarmInfo alarm2 = new AlarmInfo(uid, packageName, tag);
                                sparseArray = sparseArray2;
                                try {
                                    initAlarm(alarm2, alarmRecgInfo, isAdd, isWakeup, time2);
                                    alarms2.put(tag, alarm2);
                                    this.mAlarmMap.put(uid, alarms2);
                                    time = time2;
                                    alarm = alarm2;
                                } catch (Throwable th2) {
                                    th = th2;
                                    while (true) {
                                        try {
                                            break;
                                        } catch (Throwable th3) {
                                            th = th3;
                                        }
                                    }
                                    throw th;
                                }
                            } catch (Throwable th4) {
                                th = th4;
                                sparseArray = sparseArray2;
                                while (true) {
                                    break;
                                }
                                throw th;
                            }
                        } else {
                            sparseArray = sparseArray2;
                            try {
                                AlarmInfo alarm3 = alarms.get(tag);
                                if (alarm3 == null) {
                                    try {
                                        AlarmInfo alarm4 = new AlarmInfo(uid, packageName, tag);
                                        initAlarm(alarm4, alarmRecgInfo, isAdd, isWakeup, time2);
                                        alarms.put(tag, alarm4);
                                        time = time2;
                                        alarm = alarm4;
                                    } catch (Throwable th5) {
                                        th = th5;
                                        while (true) {
                                            break;
                                        }
                                        throw th;
                                    }
                                } else {
                                    if (isWakeup) {
                                        try {
                                            if (alarm3.count > 0) {
                                                time = time2;
                                                try {
                                                    alarm3.startTime = time;
                                                    alarm3.beHandled = false;
                                                    updateAlarmCount(alarm3, isAdd);
                                                    alarm = alarm3;
                                                } catch (Throwable th6) {
                                                    th = th6;
                                                }
                                            }
                                        } catch (Throwable th7) {
                                            th = th7;
                                            while (true) {
                                                break;
                                            }
                                            throw th;
                                        }
                                    }
                                    time = time2;
                                    try {
                                        updateAlarmCount(alarm3, isAdd);
                                        alarm = alarm3;
                                    } catch (Throwable th8) {
                                        th = th8;
                                        while (true) {
                                            break;
                                        }
                                        throw th;
                                    }
                                }
                            } catch (Throwable th9) {
                                th = th9;
                                while (true) {
                                    break;
                                }
                                throw th;
                            }
                        }
                        try {
                            sendUnPerceptionMessageIfNeed(isWakeup, uid, time, alarm);
                            updateDbIfNeed();
                        } catch (Throwable th10) {
                            th = th10;
                            while (true) {
                                break;
                            }
                            throw th;
                        }
                    } catch (Throwable th11) {
                        th = th11;
                        sparseArray = sparseArray2;
                        while (true) {
                            break;
                        }
                        throw th;
                    }
                }
            }
        }
    }

    private void updateAlarmCount(AlarmInfo alarm, boolean isAdd) {
        if (isAdd) {
            alarm.count++;
        } else if (alarm.count > 0) {
            alarm.count--;
        }
    }

    private void sendUnPerceptionMessageIfNeed(boolean isWakeup, int uid, long time, AlarmInfo alarm) {
        if (isWakeup && UserHandleEx.getUserId(uid) == this.mCurUserId && time > this.mLastPerceptionTime.get() + 15000) {
            sendUnPerceptionMessage(alarm);
        }
    }

    private void updateDbIfNeed() {
        if (SystemClock.elapsedRealtime() / UPDATE_DB_INTERVAL > ((long) this.mDbUpdateCount)) {
            sendUpdateDbMessage();
            this.mDbUpdateCount++;
            if (sDebug) {
                AwareLog.i(TAG, "UPDATE_DB_INTERVAL mDbUpdateCount : " + this.mDbUpdateCount);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlePerceptionEvent(int uid, String pkg, int event) {
        if (sDebug) {
            AwareLog.i(TAG, "handlePerceptionEvent uid : " + uid + " pkg : " + pkg + " for event : " + event);
        }
        if (UserHandleEx.getAppId(uid) >= 10000) {
            int i = 9;
            if (event != 9 || !AwareAppAssociate.getInstance().isForeGroundApp(uid)) {
                long now = SystemClock.elapsedRealtime();
                this.mLastPerceptionTime.set(now);
                ArrayMap<AlarmInfo, Boolean> alarmMap = new ArrayMap<>();
                synchronized (this.mAlarmMap) {
                    ArrayMap<String, AlarmInfo> alarms = this.mAlarmMap.get(uid);
                    if (alarms != null) {
                        boolean bUpdateDb = false;
                        for (String tag : alarms.keySet()) {
                            AlarmInfo alarm = alarms.get(tag);
                            if (event != i || alarm.fgSet) {
                                if (this.mHandler.hasMessages(11, alarm)) {
                                    this.mHandler.removeMessages(11, alarm);
                                }
                                long alarmtime = alarm.startTime;
                                if (alarmtime + 15000 >= now && alarmtime <= now && (!alarm.beHandled || alarm.reason != event)) {
                                    alarm.reason = event;
                                    alarm.perceptionCount++;
                                    alarm.beHandled = true;
                                    int newStatus = getNewPerceptionStatus(alarm);
                                    if (newStatus != alarm.status && newStatus == 1) {
                                        alarm.status = 1;
                                        bUpdateDb = true;
                                    }
                                    alarmMap.put(alarm.copy(), Boolean.valueOf(bUpdateDb));
                                    if (sDebug) {
                                        AwareLog.i(TAG, "alarm is a clock alarm : " + alarm.packageName + " tag : " + tag + " for reason : " + event);
                                    }
                                }
                                i = 9;
                            }
                        }
                    } else {
                        return;
                    }
                }
                for (AlarmInfo info : alarmMap.keySet()) {
                    updateAlarmCmp(info, alarmMap.get(info).booleanValue());
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleUnPerceptionEvent(AlarmInfo alarm, int event) {
        AlarmInfo updateAlarm;
        if (alarm != null) {
            boolean bUpdateDb = false;
            synchronized (this.mAlarmMap) {
                alarm.reason = event;
                alarm.unPerceptionCount++;
                alarm.beHandled = true;
                if (sDebug) {
                    AwareLog.i(TAG, "alarm is a un clock alarm : " + alarm.toString());
                }
                int newStatus = getNewPerceptionStatus(alarm);
                if (newStatus != alarm.status && newStatus == 2) {
                    alarm.status = 2;
                    bUpdateDb = true;
                }
                updateAlarm = alarm.copy();
            }
            updateAlarmCmp(updateAlarm, bUpdateDb);
        }
    }

    private int getNewPerceptionStatus(AlarmInfo alarm) {
        if (alarm == null) {
            return 0;
        }
        int factor = 5;
        if (AppTypeRecoManager.getInstance().getAppType(alarm.packageName) != 5) {
            factor = 1;
        }
        if (alarm.unPerceptionCount < 1 || alarm.unPerceptionCount <= alarm.perceptionCount * factor) {
            return 1;
        }
        return 2;
    }

    private CmpTypeInfo getAlarmRecgResult(int uid, String pkg, String tag) {
        if (pkg == null || tag == null) {
            return null;
        }
        String key = UserHandleEx.getUserId(uid) + "#" + pkg + "#" + tag;
        synchronized (this.mAlarmCmps) {
            CmpTypeInfo info = this.mAlarmCmps.get(key);
            if (info != null) {
                return info;
            }
            return null;
        }
    }

    private void updateAlarmCmp(AlarmInfo alarm, boolean binsert) {
        CmpTypeInfo cmpInfo;
        if (alarm != null) {
            String key = UserHandleEx.getUserId(alarm.uid) + "#" + alarm.packageName + "#" + alarm.tag;
            synchronized (this.mAlarmCmps) {
                cmpInfo = this.mAlarmCmps.get(key);
                if (cmpInfo == null) {
                    cmpInfo = new CmpTypeInfo();
                    cmpInfo.setUserId(UserHandleEx.getUserId(alarm.uid));
                    cmpInfo.setPkgName(alarm.packageName);
                    cmpInfo.setCls(alarm.tag);
                    cmpInfo.setTime(System.currentTimeMillis());
                    this.mAlarmCmps.put(key, cmpInfo);
                }
                cmpInfo.setType(alarm.status == 2 ? 4 : 3);
                cmpInfo.setPerceptionCount(alarm.perceptionCount);
                cmpInfo.setUnPerceptionCount(alarm.unPerceptionCount);
            }
            if (binsert) {
                Message msg = this.mHandler.obtainMessage();
                msg.obj = cmpInfo;
                msg.what = 2;
                this.mHandler.sendMessage(msg);
            }
        }
    }

    private void removeAlarmCmp(int uid, String pkg) {
        if (pkg != null) {
            synchronized (this.mAlarmCmps) {
                Iterator<Map.Entry<String, CmpTypeInfo>> it = this.mAlarmCmps.entrySet().iterator();
                String pkgKey = UserHandleEx.getUserId(uid) + "#" + pkg + "#";
                while (it.hasNext()) {
                    String key = it.next().getKey();
                    if (key != null && key.indexOf(pkgKey) == 0) {
                        it.remove();
                    }
                }
            }
        }
    }

    private void removeAlarmCmpByUserid(int userId) {
        synchronized (this.mAlarmCmps) {
            Iterator<Map.Entry<String, CmpTypeInfo>> it = this.mAlarmCmps.entrySet().iterator();
            String userIdKey = userId + "#";
            while (it.hasNext()) {
                String key = it.next().getKey();
                if (key != null && key.indexOf(userIdKey) == 0) {
                    it.remove();
                }
            }
        }
    }

    private void removeAlarms(int uid) {
        if (uid > 0) {
            if (sDebug) {
                AwareLog.i(TAG, "removeAlarms uid : " + uid);
            }
            synchronized (this.mAlarmMap) {
                this.mAlarmMap.remove(uid);
            }
        }
    }

    private void removeAlarmsByUserid(int userId) {
        if (sDebug) {
            AwareLog.i(TAG, "removeAlarmsByUserid userId : " + userId);
        }
        synchronized (this.mAlarmMap) {
            for (int i = this.mAlarmMap.size() - 1; i >= 0; i--) {
                if (userId == UserHandleEx.getUserId(this.mAlarmMap.keyAt(i))) {
                    this.mAlarmMap.removeAt(i);
                }
            }
        }
    }

    public int getAlarmActionType(int uid, String pkg, String action) {
        String key = UserHandleEx.getUserId(uid) + "#" + pkg + "#" + action;
        synchronized (this.mAlarmCmps) {
            CmpTypeInfo info = this.mAlarmCmps.get(key);
            if (info == null) {
                return -1;
            }
            return info.getType();
        }
    }

    public List<String> getAllInvalidAlarmTags(int uid, String pkg) {
        if (pkg == null) {
            return null;
        }
        List<String> result = new ArrayList<>();
        Set<String> set = this.mAlarmPkgList;
        if (set != null && set.contains(pkg)) {
            return result;
        }
        if (isUnReminderApp(pkg)) {
            return null;
        }
        synchronized (this.mAlarmCmps) {
            String pkgKey = UserHandleEx.getUserId(uid) + "#" + pkg + "#";
            for (Map.Entry<String, CmpTypeInfo> entry : this.mAlarmCmps.entrySet()) {
                if (entry != null) {
                    String key = entry.getKey();
                    if (key != null) {
                        if (key.indexOf(pkgKey) == 0) {
                            CmpTypeInfo value = entry.getValue();
                            if (value.getType() == 4) {
                                result.add(value.getCls());
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    public boolean hasPerceptAlarm(int uid, List<String> packageList) {
        if (!sEnabled) {
            return false;
        }
        if (!(packageList == null || this.mAlarmPkgList == null)) {
            int packageListSize = packageList.size();
            for (int i = 0; i < packageListSize; i++) {
                if (this.mAlarmPkgList.contains(packageList.get(i))) {
                    return true;
                }
            }
        }
        if (isUnReminderApp(packageList)) {
            return false;
        }
        synchronized (this.mAlarmMap) {
            ArrayMap<String, AlarmInfo> alarms = this.mAlarmMap.get(uid);
            if (alarms == null) {
                return false;
            }
            for (Map.Entry<String, AlarmInfo> entry : alarms.entrySet()) {
                if (entry != null) {
                    AlarmInfo info = entry.getValue();
                    if (info != null && (info.status == 0 || info.status == 1)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleUpdateDb() {
        Map<String, CmpTypeInfo> alarmsInDb = getAlarmCmpFromDb();
        synchronized (this.mAlarmCmps) {
            for (Map.Entry<String, CmpTypeInfo> entry : this.mAlarmCmps.entrySet()) {
                CmpTypeInfo value = entry.getValue();
                CmpTypeInfo alarmInDb = alarmsInDb.get(entry.getKey());
                if (alarmInDb == null || alarmInDb.getPerceptionCount() != value.getPerceptionCount() || alarmInDb.getUnPerceptionCount() != value.getUnPerceptionCount()) {
                    insertCmpRecgInfo(value);
                }
            }
        }
    }

    public void updateWidget(Set<String> widgets, String pkgName) {
        AppStartupDataMgr appStartupDataMgr = this.mDataMgr;
        if (appStartupDataMgr != null) {
            if (appStartupDataMgr.updateWidgetList(widgets)) {
                sendFlushToDiskMessage();
            }
            widgetTrigUpdate(pkgName);
        }
    }

    public void widgetTrigUpdate(String pkgName) {
        if (pkgName != null && this.mDataMgr != null) {
            if (sDebug) {
                AwareLog.i(TAG, "widgetTrigUpdate pkg:" + pkgName);
            }
            this.mDataMgr.updateWidgetUpdateTime(pkgName);
        }
    }

    private void flushUpDownLoadTrigTime(int uid) {
        long curTime = SystemClock.elapsedRealtime();
        synchronized (this.mStatusUpDownElapse) {
            this.mStatusUpDownElapse.put(Integer.valueOf(uid), Long.valueOf(curTime));
        }
    }

    private boolean isProtectUpdateWidget(String pkgName, int uid) {
        AppStartupDataMgr appStartupDataMgr;
        if (pkgName == null || (appStartupDataMgr = this.mDataMgr) == null) {
            return false;
        }
        long time = appStartupDataMgr.getWidgetExistPkgUpdateTime(pkgName);
        if (time == -1) {
            return false;
        }
        if (this.mDataMgr.getWidgetCnt() <= this.mWidgetCheckUpdateCnt) {
            return true;
        }
        long j = this.mWidgetCheckUpdateInterval;
        if (SystemClock.elapsedRealtime() - time <= j) {
            return true;
        }
        return !isUpDownTrigInIntervalTime(uid, j);
    }

    public boolean isUpDownTrigInIntervalTime(int uid, long timeMax) {
        long timeCur = SystemClock.elapsedRealtime();
        synchronized (this.mStatusUpDownElapse) {
            Long updownTime = this.mStatusUpDownElapse.get(Integer.valueOf(uid));
            if (updownTime == null) {
                return false;
            }
            if (timeCur - updownTime.longValue() <= timeMax) {
                return true;
            }
            return false;
        }
    }

    private void sendMsgWidgetUpDownLoadClear() {
        IntlRecgHandler intlRecgHandler = this.mHandler;
        if (intlRecgHandler != null) {
            Message msg = intlRecgHandler.obtainMessage();
            msg.what = MSG_UPDOWN_CLEAR;
            this.mHandler.removeMessages(MSG_UPDOWN_CLEAR);
            this.mHandler.sendMessageDelayed(msg, this.mWidgetCheckUpdateInterval);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleWidgetUpDownLoadClear() {
        long curTime = SystemClock.elapsedRealtime();
        synchronized (this.mStatusUpDownElapse) {
            Iterator<Map.Entry<Integer, Long>> iter = this.mStatusUpDownElapse.entrySet().iterator();
            while (iter.hasNext()) {
                if (curTime - iter.next().getValue().longValue() > this.mWidgetCheckUpdateInterval) {
                    iter.remove();
                }
            }
        }
        sendMsgWidgetUpDownLoadClear();
    }

    private boolean isPushSdkCompInvalid(String compName, int callerUid, int targetUid) {
        if (compName == null || callerUid == targetUid || compName.isEmpty() || AwareAppAssociate.getInstance().getCurHomeProcessUid() == callerUid) {
            return true;
        }
        return false;
    }

    private boolean isPushSdkComp(AwareAppStartStatusCache status, String compName, AppMngConstant.AppStartSource source) {
        int index;
        if (!sEnabled) {
            return false;
        }
        int callerUid = status.cacheCallerUid;
        int targetUid = status.cacheUid;
        boolean isSysApp = status.cacheIsSystemApp;
        if (sDebug) {
            AwareLog.d(TAG, "PushSdk isPushSdkComp callerUid :" + callerUid + " targetUid : " + targetUid + " cmp: " + compName);
        }
        if (isPushSdkCompInvalid(compName, callerUid, targetUid)) {
            return false;
        }
        AppStartupDataMgr appStartupDataMgr = this.mDataMgr;
        if (appStartupDataMgr != null && (isSysApp || appStartupDataMgr.isSpecialCaller(callerUid))) {
            return false;
        }
        String[] strs = compName.split("/");
        if (strs.length == 1) {
            index = 0;
        } else if (strs.length != 2) {
            return false;
        } else {
            index = 1;
        }
        if (isBadPushSdkComp(strs[index])) {
            return true;
        }
        if (!AppMngConstant.AppStartSource.THIRD_ACTIVITY.equals(source) && !AppMngConstant.AppStartSource.BIND_SERVICE.equals(source) && !status.cacheStatusCacheExt.abroad && strs.length == 2) {
            sendRecgPushSdkMessage(callerUid, compName, targetUid);
        }
        return false;
    }

    private boolean isBadPushSdkComp(String cls) {
        synchronized (this.mPushSdkClsList) {
            if (this.mPushSdkClsList.contains(cls)) {
                return true;
            }
        }
        int badFunc = ComponentRecoManager.getInstance().getComponentBadFunc(cls);
        if (badFunc != 0 && (badFunc & 1) == 1) {
            return true;
        }
        return false;
    }

    private boolean isAllowStartAppByTopN(String pkg, AwareAppStartStatusCache status, int type, int topN, boolean isBoot) {
        if (this.mDeviceTotalMemory <= 3072) {
            return false;
        }
        if (status.cacheAppType == -100) {
            status.cacheAppType = getAppMngSpecType(pkg);
        }
        if (status.cacheAppType == type) {
            return isTopHabitAppInStart(pkg, type, topN, isBoot);
        }
        return false;
    }

    private boolean isAlarmApp(String pkg, AwareAppStartStatusCache status) {
        if (status.unPercetibleAlarm == 1 || isUnReminderApp(pkg)) {
            return false;
        }
        Set<String> set = this.mAlarmPkgList;
        if (set != null && set.contains(pkg)) {
            return true;
        }
        int alarmType = -1;
        if (status.cacheAction != null) {
            alarmType = getAlarmActionType(status.cacheUid, pkg, status.cacheAction);
        } else if (status.cacheCompName != null) {
            alarmType = getAlarmActionType(status.cacheUid, pkg, status.cacheCompName);
        }
        return alarmType != 4;
    }

    public int getAppStartSpecCallerAction(String packageName, AwareAppStartStatusCache status, ArrayList<Integer> actionList, AppMngConstant.AppStartSource source) {
        if (!this.mAppStartEnabled || actionList == null) {
            return -1;
        }
        int size = actionList.size();
        for (int i = 0; i < size; i++) {
            Integer expectItem = actionList.get(i);
            if (isAppStartSpecCallerAction(packageName, expectItem.intValue(), status, source)) {
                return expectItem.intValue();
            }
        }
        return -1;
    }

    public int getAppStartSpecCallerStatus(String packageName, AwareAppStartStatusCache status, ArrayList<Integer> statusList) {
        if (!this.mAppStartEnabled || statusList == null) {
            return -1;
        }
        int size = statusList.size();
        for (int i = 0; i < size; i++) {
            Integer expectItem = statusList.get(i);
            if (isAppStartSpecCallerStatus(packageName, expectItem.intValue(), status)) {
                return expectItem.intValue();
            }
        }
        return -1;
    }

    public int getAppStartSpecTargetType(String packageName, AwareAppStartStatusCache status, ArrayList<Integer> typeList) {
        if (!this.mAppStartEnabled || typeList == null) {
            return -1;
        }
        int size = typeList.size();
        for (int i = 0; i < size; i++) {
            Integer expectItem = typeList.get(i);
            if (isAppStartSpecType(packageName, expectItem.intValue(), status)) {
                return expectItem.intValue();
            }
        }
        return -1;
    }

    public int getAppStartSpecTargetStatus(String packageName, AwareAppStartStatusCache status, ArrayList<Integer> statusList) {
        if (!this.mAppStartEnabled || statusList == null) {
            return -1;
        }
        int size = statusList.size();
        for (int i = 0; i < size; i++) {
            Integer expectItem = statusList.get(i);
            if (isAppStartSpecStat(packageName, expectItem.intValue(), status)) {
                return expectItem.intValue();
            }
        }
        return -1;
    }

    public int getAppStartSpecVerOversea(String packageName, AwareAppStartStatusCache status) {
        if (!this.mAppStartEnabled) {
            return -1;
        }
        if (status.cacheStatusCacheExt.abroad) {
            return AppStartPolicyCfg.AppStartOversea.OVERSEA.ordinal();
        }
        return AppStartPolicyCfg.AppStartOversea.CHINA.ordinal();
    }

    public int getAppStartSpecAppSrcRange(String packageName, AwareAppStartStatusCache status) {
        if (!this.mAppStartEnabled) {
            return -1;
        }
        if (status.cacheStatusCacheExt.appSrcRange == -100) {
            status.cacheStatusCacheExt.appSrcRange = getAppStartSpecAppSrcRangeResult(packageName);
        }
        boolean srcRange = true;
        if (!(status.cacheStatusCacheExt.appSrcRange == 0 || status.cacheStatusCacheExt.appSrcRange == 1)) {
            srcRange = false;
        }
        if (srcRange) {
            return AppStartPolicyCfg.AppStartAppSrcRange.PRERECG.ordinal();
        }
        return AppStartPolicyCfg.AppStartAppSrcRange.NONPRERECG.ordinal();
    }

    public int getAppStartSpecAppOversea(String packageName, AwareAppStartStatusCache status) {
        if (!this.mAppStartEnabled) {
            return -1;
        }
        if (status.cacheStatusCacheExt.appSrcRange == -100) {
            status.cacheStatusCacheExt.appSrcRange = getAppStartSpecAppSrcRangeResult(packageName);
        }
        if (status.cacheStatusCacheExt.appSrcRange == 0) {
            return AppStartPolicyCfg.AppStartAppOversea.CHINA.ordinal();
        }
        if (status.cacheStatusCacheExt.appSrcRange == 1) {
            return AppStartPolicyCfg.AppStartAppOversea.OVERSEA.ordinal();
        }
        return AppStartPolicyCfg.AppStartAppOversea.UNKNONW.ordinal();
    }

    public int getAppStartSpecScreenStatus(String packageName, AwareAppStartStatusCache status) {
        if (!this.mAppStartEnabled) {
            return -1;
        }
        if (this.mIsScreenOnPm) {
            return AppStartPolicyCfg.AppStartScreenStatus.SCREENON.ordinal();
        }
        return AppStartPolicyCfg.AppStartScreenStatus.SCREENOFF.ordinal();
    }

    public int getAppStartSpecRegion(String packageName, AwareAppStartStatusCache status) {
        if (!this.mAppStartEnabled) {
            return -1;
        }
        return this.mAppStartAreaCfg;
    }

    public int getAppMngDeviceLevel() {
        if (!sEnabled) {
            return 1;
        }
        return this.mDeviceLevel;
    }

    private boolean isAppStartSpecCallerAction(String pkg, int appAction, AwareAppStartStatusCache status, AppMngConstant.AppStartSource source) {
        if (!this.mAppStartEnabled) {
            return false;
        }
        if (sDebug) {
            AwareLog.i(TAG, "isAppStartSpecAction " + pkg + ",action:" + appAction);
        }
        if (appAction < 0 || appAction >= AppStartPolicyCfg.AppStartCallerAction.values().length) {
            return false;
        }
        return isAppStartSpecCallerActionComm(pkg, AppStartPolicyCfg.AppStartCallerAction.values()[appAction], status, source);
    }

    private boolean isAppStartSpecCallerActionComm(String pkg, AppStartPolicyCfg.AppStartCallerAction appAction, AwareAppStartStatusCache status, AppMngConstant.AppStartSource source) {
        if (!sEnabled) {
            return false;
        }
        int i = AnonymousClass3.$SwitchMap$com$android$server$rms$iaware$appmng$AppStartPolicyCfg$AppStartCallerAction[appAction.ordinal()];
        if (i == 1) {
            return isPushSdkComp(status, (AppMngConstant.AppStartSource.THIRD_BROADCAST.equals(source) || AppMngConstant.AppStartSource.SYSTEM_BROADCAST.equals(source)) ? status.cacheAction : status.cacheCompName, source);
        } else if (i == 2) {
            return status.cacheIsBtMediaBrowserCaller;
        } else {
            if (i == 3) {
                return status.cacheNotifyListenerCaller;
            }
            if (i != 4) {
                return false;
            }
            return status.cacheStatusCacheExt.hwPush;
        }
    }

    private boolean isAppStartSpecCallerStatus(String pkg, int appStatus, AwareAppStartStatusCache status) {
        if (!this.mAppStartEnabled) {
            return false;
        }
        if (sDebug) {
            AwareLog.i(TAG, "isAppStartSpecCallerStatus " + pkg + ",action:" + appStatus);
        }
        if (appStatus < 0 || appStatus >= AppStartPolicyCfg.AppStartCallerStatus.values().length) {
            return false;
        }
        int i = AnonymousClass3.$SwitchMap$com$android$server$rms$iaware$appmng$AppStartPolicyCfg$AppStartCallerStatus[AppStartPolicyCfg.AppStartCallerStatus.values()[appStatus].ordinal()];
        if (i == 1) {
            return !status.cacheIsCallerFg;
        }
        if (i != 2) {
            return false;
        }
        return status.cacheIsCallerFg;
    }

    private boolean isAppStartSpecType(String pkg, int appType, AwareAppStartStatusCache status) {
        if (!this.mAppStartEnabled) {
            return false;
        }
        if (sDebug) {
            AwareLog.i(TAG, "isAppStartSpecType " + pkg + ",type:" + appType);
        }
        if (appType < 0 || appType >= AppStartPolicyCfg.AppStartTargetType.values().length) {
            return false;
        }
        AppStartPolicyCfg.AppStartTargetType appStartEnum = AppStartPolicyCfg.AppStartTargetType.values()[appType];
        if (AnonymousClass3.$SwitchMap$com$android$server$rms$iaware$appmng$AppStartPolicyCfg$AppStartTargetType[appStartEnum.ordinal()] != 1) {
            return isAppStartSpecTypeComm(pkg, appStartEnum, status);
        }
        AppStartupDataMgr appStartupDataMgr = this.mDataMgr;
        if (appStartupDataMgr != null) {
            return appStartupDataMgr.isBlindAssistPkg(pkg);
        }
        return false;
    }

    private boolean isAppStartSpecTypeComm(String pkg, AppStartPolicyCfg.AppStartTargetType appType, AwareAppStartStatusCache status) {
        if (!sEnabled) {
            return false;
        }
        switch (appType) {
            case TTS:
                return isTtsPkg(pkg);
            case IM:
                if (status.cacheAppType == -100) {
                    status.cacheAppType = getAppMngSpecType(pkg);
                }
                if (status.cacheAppType == 0) {
                    return true;
                }
                return false;
            case CLOCK:
                return isAlarmApp(pkg, status);
            case PAY:
                return isPayPkg(pkg, status);
            case SHARE:
                return isSharePkg(pkg, status);
            case BUSINESS:
                if (status.cacheAppType == -100) {
                    status.cacheAppType = getAppMngSpecType(pkg);
                }
                if (status.cacheAppType == 11) {
                    return true;
                }
                return false;
            case EMAIL:
                return isAppMngSpecTypeFreqTopN(pkg, 1, -1);
            case RCV_MONEY:
                if (status.cacheAppType == -100) {
                    status.cacheAppType = getAppMngSpecType(pkg);
                }
                if (status.cacheAppType == 34) {
                    return true;
                }
                return false;
            case HABIT_IM:
                return isAllowStartAppByTopN(pkg, status, 0, 3, true);
            case MOSTFREQIM:
                return isAllowStartAppByTopN(pkg, status, 0, 3, false);
            default:
                return isAppStartSpecTypeCommExt(pkg, appType, status);
        }
    }

    private boolean isAppStartSpecTypeCommExt(String pkg, AppStartPolicyCfg.AppStartTargetType appType, AwareAppStartStatusCache status) {
        if (AnonymousClass3.$SwitchMap$com$android$server$rms$iaware$appmng$AppStartPolicyCfg$AppStartTargetType[appType.ordinal()] != 12) {
            return false;
        }
        if (status.cacheAppType == -100) {
            status.cacheAppType = getAppMngSpecType(pkg);
        }
        if (status.cacheAppType == 5) {
            return true;
        }
        return false;
    }

    private boolean isAppStartSpecStat(String pkg, int statType, AwareAppStartStatusCache status) {
        if (!this.mAppStartEnabled) {
            return false;
        }
        if (sDebug) {
            AwareLog.i(TAG, "isAppStartSpecStat " + pkg + ",status:" + statType);
        }
        if (statType < 0 || statType >= AppStartPolicyCfg.AppStartTargetStat.values().length) {
            return false;
        }
        AppStartPolicyCfg.AppStartTargetStat appStartEnum = AppStartPolicyCfg.AppStartTargetStat.values()[statType];
        int i = AnonymousClass3.$SwitchMap$com$android$server$rms$iaware$appmng$AppStartPolicyCfg$AppStartTargetStat[appStartEnum.ordinal()];
        if (i == 1) {
            AppStartupDataMgr appStartupDataMgr = this.mDataMgr;
            if (appStartupDataMgr != null) {
                return appStartupDataMgr.isWidgetExistPkg(pkg);
            }
            return false;
        } else if (i == 2) {
            return isProtectUpdateWidget(pkg, status.cacheUid);
        } else {
            if (i != 3) {
                return isAppStartSpecStatComm(pkg, appStartEnum, status);
            }
            return !status.cacheIsAppStop;
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.server.rms.iaware.appmng.AwareIntelligentRecg$3  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass3 {
        static final /* synthetic */ int[] $SwitchMap$com$android$server$rms$iaware$appmng$AppStartPolicyCfg$AppStartCallerAction = new int[AppStartPolicyCfg.AppStartCallerAction.values().length];
        static final /* synthetic */ int[] $SwitchMap$com$android$server$rms$iaware$appmng$AppStartPolicyCfg$AppStartCallerStatus = new int[AppStartPolicyCfg.AppStartCallerStatus.values().length];

        static {
            $SwitchMap$com$android$server$rms$iaware$appmng$AppStartPolicyCfg$AppStartTargetStat = new int[AppStartPolicyCfg.AppStartTargetStat.values().length];
            try {
                $SwitchMap$com$android$server$rms$iaware$appmng$AppStartPolicyCfg$AppStartTargetStat[AppStartPolicyCfg.AppStartTargetStat.WIDGET.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$server$rms$iaware$appmng$AppStartPolicyCfg$AppStartTargetStat[AppStartPolicyCfg.AppStartTargetStat.WIDGETUPDATE.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$server$rms$iaware$appmng$AppStartPolicyCfg$AppStartTargetStat[AppStartPolicyCfg.AppStartTargetStat.ALIVE.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$android$server$rms$iaware$appmng$AppStartPolicyCfg$AppStartTargetStat[AppStartPolicyCfg.AppStartTargetStat.MUSIC.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$android$server$rms$iaware$appmng$AppStartPolicyCfg$AppStartTargetStat[AppStartPolicyCfg.AppStartTargetStat.RECORD.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$android$server$rms$iaware$appmng$AppStartPolicyCfg$AppStartTargetStat[AppStartPolicyCfg.AppStartTargetStat.GUIDE.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$android$server$rms$iaware$appmng$AppStartPolicyCfg$AppStartTargetStat[AppStartPolicyCfg.AppStartTargetStat.UPDOWNLOAD.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$android$server$rms$iaware$appmng$AppStartPolicyCfg$AppStartTargetStat[AppStartPolicyCfg.AppStartTargetStat.HEALTH.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$android$server$rms$iaware$appmng$AppStartPolicyCfg$AppStartTargetStat[AppStartPolicyCfg.AppStartTargetStat.FGACTIVITY.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$android$server$rms$iaware$appmng$AppStartPolicyCfg$AppStartTargetStat[AppStartPolicyCfg.AppStartTargetStat.WALLPAPER.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$com$android$server$rms$iaware$appmng$AppStartPolicyCfg$AppStartTargetStat[AppStartPolicyCfg.AppStartTargetStat.INPUTMETHOD.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
            $SwitchMap$com$android$server$rms$iaware$appmng$AppStartPolicyCfg$AppStartTargetType = new int[AppStartPolicyCfg.AppStartTargetType.values().length];
            try {
                $SwitchMap$com$android$server$rms$iaware$appmng$AppStartPolicyCfg$AppStartTargetType[AppStartPolicyCfg.AppStartTargetType.BLIND.ordinal()] = 1;
            } catch (NoSuchFieldError e12) {
            }
            try {
                $SwitchMap$com$android$server$rms$iaware$appmng$AppStartPolicyCfg$AppStartTargetType[AppStartPolicyCfg.AppStartTargetType.TTS.ordinal()] = 2;
            } catch (NoSuchFieldError e13) {
            }
            try {
                $SwitchMap$com$android$server$rms$iaware$appmng$AppStartPolicyCfg$AppStartTargetType[AppStartPolicyCfg.AppStartTargetType.IM.ordinal()] = 3;
            } catch (NoSuchFieldError e14) {
            }
            try {
                $SwitchMap$com$android$server$rms$iaware$appmng$AppStartPolicyCfg$AppStartTargetType[AppStartPolicyCfg.AppStartTargetType.CLOCK.ordinal()] = 4;
            } catch (NoSuchFieldError e15) {
            }
            try {
                $SwitchMap$com$android$server$rms$iaware$appmng$AppStartPolicyCfg$AppStartTargetType[AppStartPolicyCfg.AppStartTargetType.PAY.ordinal()] = 5;
            } catch (NoSuchFieldError e16) {
            }
            try {
                $SwitchMap$com$android$server$rms$iaware$appmng$AppStartPolicyCfg$AppStartTargetType[AppStartPolicyCfg.AppStartTargetType.SHARE.ordinal()] = 6;
            } catch (NoSuchFieldError e17) {
            }
            try {
                $SwitchMap$com$android$server$rms$iaware$appmng$AppStartPolicyCfg$AppStartTargetType[AppStartPolicyCfg.AppStartTargetType.BUSINESS.ordinal()] = 7;
            } catch (NoSuchFieldError e18) {
            }
            try {
                $SwitchMap$com$android$server$rms$iaware$appmng$AppStartPolicyCfg$AppStartTargetType[AppStartPolicyCfg.AppStartTargetType.EMAIL.ordinal()] = 8;
            } catch (NoSuchFieldError e19) {
            }
            try {
                $SwitchMap$com$android$server$rms$iaware$appmng$AppStartPolicyCfg$AppStartTargetType[AppStartPolicyCfg.AppStartTargetType.RCV_MONEY.ordinal()] = 9;
            } catch (NoSuchFieldError e20) {
            }
            try {
                $SwitchMap$com$android$server$rms$iaware$appmng$AppStartPolicyCfg$AppStartTargetType[AppStartPolicyCfg.AppStartTargetType.HABIT_IM.ordinal()] = 10;
            } catch (NoSuchFieldError e21) {
            }
            try {
                $SwitchMap$com$android$server$rms$iaware$appmng$AppStartPolicyCfg$AppStartTargetType[AppStartPolicyCfg.AppStartTargetType.MOSTFREQIM.ordinal()] = 11;
            } catch (NoSuchFieldError e22) {
            }
            try {
                $SwitchMap$com$android$server$rms$iaware$appmng$AppStartPolicyCfg$AppStartTargetType[AppStartPolicyCfg.AppStartTargetType.CLOCKTYPE.ordinal()] = 12;
            } catch (NoSuchFieldError e23) {
            }
            try {
                $SwitchMap$com$android$server$rms$iaware$appmng$AppStartPolicyCfg$AppStartCallerStatus[AppStartPolicyCfg.AppStartCallerStatus.BACKGROUND.ordinal()] = 1;
            } catch (NoSuchFieldError e24) {
            }
            try {
                $SwitchMap$com$android$server$rms$iaware$appmng$AppStartPolicyCfg$AppStartCallerStatus[AppStartPolicyCfg.AppStartCallerStatus.FOREGROUND.ordinal()] = 2;
            } catch (NoSuchFieldError e25) {
            }
            try {
                $SwitchMap$com$android$server$rms$iaware$appmng$AppStartPolicyCfg$AppStartCallerAction[AppStartPolicyCfg.AppStartCallerAction.PUSHSDK.ordinal()] = 1;
            } catch (NoSuchFieldError e26) {
            }
            try {
                $SwitchMap$com$android$server$rms$iaware$appmng$AppStartPolicyCfg$AppStartCallerAction[AppStartPolicyCfg.AppStartCallerAction.BTMEDIABROWSER.ordinal()] = 2;
            } catch (NoSuchFieldError e27) {
            }
            try {
                $SwitchMap$com$android$server$rms$iaware$appmng$AppStartPolicyCfg$AppStartCallerAction[AppStartPolicyCfg.AppStartCallerAction.NOTIFYLISTENER.ordinal()] = 3;
            } catch (NoSuchFieldError e28) {
            }
            try {
                $SwitchMap$com$android$server$rms$iaware$appmng$AppStartPolicyCfg$AppStartCallerAction[AppStartPolicyCfg.AppStartCallerAction.HWPUSH.ordinal()] = 4;
            } catch (NoSuchFieldError e29) {
            }
        }
    }

    private boolean isAppStartSpecStatComm(String pkg, AppStartPolicyCfg.AppStartTargetStat appStartEnum, AwareAppStartStatusCache status) {
        if (!sEnabled) {
            return false;
        }
        switch (appStartEnum) {
            case MUSIC:
                return isAudioOutStatus(status.cacheUid);
            case RECORD:
                return isAudioInStatus(status.cacheUid);
            case GUIDE:
                return isGpsStatus(status.cacheUid);
            case UPDOWNLOAD:
                return isUpDownStatus(status.cacheUid);
            case HEALTH:
                return isSensorStatus(status.cacheUid);
            case FGACTIVITY:
                return status.cacheIsTargetFg;
            case WALLPAPER:
                if (status.cacheUid == getDefaultWallPaperUid()) {
                    return true;
                }
                return false;
            case INPUTMETHOD:
                if (status.cacheUid == getDefaultInputMethodUid()) {
                    return true;
                }
                return false;
            default:
                return false;
        }
    }

    /* JADX INFO: Multiple debug info for r0v2 java.util.Set<java.lang.String>: [D('tmpRecent' java.util.Set<java.lang.String>), D('habit' com.android.server.rms.algorithm.AwareUserHabit)] */
    public boolean isAppMngSpecRecent(String pkg, int isRecent, int recent) {
        if (!sEnabled || recent <= 0) {
            return false;
        }
        if (this.mRecent == null || this.mTempRecent != recent || SystemClock.elapsedRealtimeNanos() - this.mUpdateTimeRecent > 10000000) {
            AwareUserHabit habit = AwareUserHabit.getInstance();
            if (habit == null) {
                return false;
            }
            this.mRecent = habit.getBackgroundApps((long) recent);
            this.mUpdateTimeRecent = SystemClock.elapsedRealtimeNanos();
            this.mTempRecent = recent;
        }
        Set<String> tmpRecent = this.mRecent;
        if (tmpRecent == null) {
            return false;
        }
        if (isRecent == 0) {
            return !tmpRecent.contains(pkg);
        }
        return tmpRecent.contains(pkg);
    }

    public int getAppStartSpecAppSrcRangeResult(String pkg) {
        if (this.mDataMgr.isAutoMngPkg(pkg)) {
            return 0;
        }
        return AppTypeRecoManager.getInstance().getAppWhereFrom(pkg);
    }

    public int getAppMngSpecRecent(String pkg, LinkedHashMap<Integer, RuleNode> recents) {
        if (!sEnabled || recents == null) {
            return -1;
        }
        for (Integer recent : recents.keySet()) {
            if (recent != null && recent.intValue() > 0 && isAppMngSpecRecent(pkg, 1, recent.intValue())) {
                return recent.intValue();
            }
        }
        return -1;
    }

    public boolean isAppMngSpecTypeFreqTopN(String pkg, int type, int topN) {
        if (!sEnabled || pkg == null) {
            return false;
        }
        AwareUserHabit habit = AwareUserHabit.getInstance();
        if (habit == null) {
            AwareLog.e(TAG, "AwareUserHabit is null");
            return false;
        } else if (topN == -1) {
            updateCacheIfNeed(habit, type);
            Set<String> cachedTopN = this.mCachedTypeTopN.get(Integer.valueOf(type));
            if (cachedTopN == null) {
                return false;
            }
            return cachedTopN.contains(pkg);
        } else {
            List<String> list = habit.getMostFreqAppByType(type, topN);
            if (list == null) {
                return false;
            }
            return list.contains(pkg);
        }
    }

    private boolean isAppMngSpecTypeFreqInDayTopN(String pkg, int type, int topN) {
        if (!sEnabled || pkg == null) {
            return false;
        }
        AwareUserHabit habit = AwareUserHabit.getInstance();
        if (habit == null) {
            AwareLog.e(TAG, "AwareUserHabit is null");
            return false;
        }
        List<String> list = habit.getMostFreqAppByTypeEx(type, topN);
        if (list == null) {
            return false;
        }
        return list.contains(pkg);
    }

    private boolean isTopHabitAppInStart(String pkg, int type, int topN, boolean isBoot) {
        if (!this.mAppStartEnabled || pkg == null || topN <= 0) {
            return false;
        }
        if (!isBoot) {
            return isAppMngSpecTypeFreqInDayTopN(pkg, type, topN);
        }
        AppTypeRecoManager appTypeRecoManager = AppTypeRecoManager.getInstance();
        if (appTypeRecoManager != null) {
            return appTypeRecoManager.isTopIm(pkg, topN);
        }
        AwareLog.w(TAG, "AppTypeRecoManager is null");
        return false;
    }

    private void updateCacheIfNeed(AwareUserHabit habit, int type) {
        long curTime = SystemClock.elapsedRealtime();
        ArrayMap<Integer, Long> lastUpdateTimeMap = new ArrayMap<>(this.mLastUpdateTime);
        Long lastUpdateTime = lastUpdateTimeMap.get(Integer.valueOf(type));
        if (lastUpdateTime == null) {
            lastUpdateTime = 0L;
        }
        if (curTime - lastUpdateTime.longValue() >= MemoryConstant.MIN_INTERVAL_OP_TIMEOUT) {
            List<String> topN = habit.getMostFreqAppByType(type, -1);
            if (topN == null) {
                topN = new ArrayList<>();
            }
            ArraySet<String> cachedTopN = new ArraySet<>();
            cachedTopN.addAll(topN);
            ArrayMap<Integer, Set<String>> cachedMapTopN = new ArrayMap<>(this.mCachedTypeTopN);
            cachedMapTopN.put(Integer.valueOf(type), cachedTopN);
            lastUpdateTimeMap.put(Integer.valueOf(type), Long.valueOf(curTime));
            this.mLastUpdateTime = lastUpdateTimeMap;
            this.mCachedTypeTopN = cachedMapTopN;
        }
    }

    public int getAppMngSpecTypeFreqTopN(String pkg, LinkedHashMap<Integer, RuleNode> nodeTopN) {
        if (!sEnabled || pkg == null || nodeTopN == null) {
            return -1;
        }
        for (Integer topN : nodeTopN.keySet()) {
            if (topN != null && topN.intValue() >= 0) {
                int userHabitTopN = topN.intValue();
                if (topN.intValue() == 0) {
                    userHabitTopN = -1;
                }
                AppTypeRecoManager atrm = AppTypeRecoManager.getInstance();
                if (isAppMngSpecTypeFreqTopN(pkg, atrm.convertType(atrm.getAppType(pkg)), userHabitTopN)) {
                    return topN.intValue();
                }
            }
        }
        return -1;
    }

    /* JADX INFO: Multiple debug info for r0v2 java.util.List<java.lang.String>: [D('habit' com.android.server.rms.algorithm.AwareUserHabit), D('tmpHabbitTopN' java.util.List<java.lang.String>)] */
    public boolean isAppMngSpecHabbitTopN(String pkg, int isHabbitTopN, int topN) {
        if (!sEnabled || topN <= 0) {
            return false;
        }
        if (this.mHabbitTopN == null || this.mTempTopN != topN || SystemClock.elapsedRealtimeNanos() - this.mUpdateTimeHabbitTopN > 10000000) {
            AwareUserHabit habit = AwareUserHabit.getInstance();
            if (habit == null) {
                return false;
            }
            this.mHabbitTopN = habit.getTopN(topN);
            this.mUpdateTimeHabbitTopN = SystemClock.elapsedRealtimeNanos();
            this.mTempTopN = topN;
        }
        List<String> tmpHabbitTopN = this.mHabbitTopN;
        if (tmpHabbitTopN == null) {
            return false;
        }
        if (isHabbitTopN == 0) {
            return !tmpHabbitTopN.contains(pkg);
        }
        return tmpHabbitTopN.contains(pkg);
    }

    public int getAppMngSpecHabbitTopN(String pkg, LinkedHashMap<Integer, RuleNode> nodeTopN) {
        if (!sEnabled || nodeTopN == null || pkg == null) {
            return -1;
        }
        for (Integer topN : nodeTopN.keySet()) {
            if (topN != null && topN.intValue() > 0 && isAppMngSpecHabbitTopN(pkg, 1, topN.intValue())) {
                return topN.intValue();
            }
        }
        return -1;
    }

    public boolean isAppMngSpecType(String pkg, int appType) {
        if (sEnabled && appType == AppTypeRecoManager.getInstance().getAppType(pkg)) {
            return true;
        }
        return false;
    }

    public int getAppMngSpecType(String pkg) {
        if (!sEnabled || pkg == null) {
            return -1;
        }
        return AppTypeRecoManager.getInstance().getAppType(pkg);
    }

    public boolean isAppMngSpecStat(AwareProcessInfo info, int statType) {
        if (!sEnabled || info == null) {
            return false;
        }
        AppStatusUtils.Status[] values = AppStatusUtils.Status.values();
        if (values.length <= statType || statType < 0) {
            return false;
        }
        return AppStatusUtils.getInstance().checkAppStatus(values[statType], info);
    }

    public int getAppMngSpecStat(AwareProcessInfo info, LinkedHashMap<Integer, RuleNode> statTypes) {
        if (!sEnabled || statTypes == null || info == null) {
            return -1;
        }
        AppStatusUtils.Status[] values = AppStatusUtils.Status.values();
        for (Integer statType : statTypes.keySet()) {
            if (statType != null && values.length > statType.intValue() && statType.intValue() >= 0 && AppStatusUtils.getInstance().checkAppStatus(values[statType.intValue()], info)) {
                return statType.intValue();
            }
        }
        return -1;
    }

    @SuppressLint({"PreferForInArrayList"})
    public boolean isInSmallSampleList(AwareProcessInfo awareProcInfo) {
        if (!sEnabled || awareProcInfo == null || awareProcInfo.procProcInfo == null) {
            return false;
        }
        synchronized (this.mSmallSampleList) {
            Iterator it = awareProcInfo.procProcInfo.mPackageName.iterator();
            while (it.hasNext()) {
                if (!this.mSmallSampleList.contains((String) it.next())) {
                    return false;
                }
            }
            return true;
        }
    }

    public void setSmallSampleList(List<String> samplesList) {
        if (samplesList != null) {
            synchronized (this.mSmallSampleList) {
                this.mSmallSampleList.clear();
                this.mSmallSampleList.addAll(samplesList);
            }
        }
    }

    public List<String> getSmallSampleList() {
        List<String> smallSamples;
        synchronized (this.mSmallSampleList) {
            smallSamples = new ArrayList<>(this.mSmallSampleList);
        }
        return smallSamples;
    }

    public boolean isAppFrozen(int uid) {
        boolean contains;
        if (!sEnabled) {
            return false;
        }
        synchronized (this.mFrozenAppList) {
            contains = this.mFrozenAppList.contains(uid);
        }
        return contains;
    }

    public boolean isAppBluetooth(int uid) {
        boolean contains;
        if (!sEnabled) {
            return false;
        }
        synchronized (this.mBluetoothAppList) {
            contains = this.mBluetoothAppList.contains(uid);
        }
        return contains;
    }

    public boolean isAudioOutInstant(int uid) {
        boolean contains;
        if (!sEnabled) {
            return false;
        }
        synchronized (this.mAudioOutInstant) {
            contains = this.mAudioOutInstant.contains(uid);
        }
        return contains;
    }

    public boolean isToastWindow(int pid) {
        boolean z = false;
        if (!sEnabled) {
            return false;
        }
        synchronized (this.mToasts) {
            AwareProcessWindowInfo toastInfo = this.mToasts.get(pid);
            if (toastInfo != null && !toastInfo.isEvil()) {
                z = true;
            }
        }
        return z;
    }

    private void registerContentObserver(Context context, ContentObserver observer) {
        if (!this.mIsObsvInit.get()) {
            ContentResolverExt.registerContentObserver(context.getContentResolver(), Settings.Secure.getUriFor("default_input_method"), false, observer, -1);
            ContentResolverExt.registerContentObserver(context.getContentResolver(), Settings.Secure.getUriFor("enabled_accessibility_services"), false, observer, -1);
            ContentResolverExt.registerContentObserver(context.getContentResolver(), Settings.Secure.getUriFor("tts_default_synth"), false, observer, -1);
            ContentResolverExt.registerContentObserver(context.getContentResolver(), Settings.Secure.getUriFor(VALUE_BT_BLE_CONNECT_APPS), false, observer, -1);
            ContentResolverExt.registerContentObserver(context.getContentResolver(), Settings.Secure.getUriFor(VALUE_BT_LAST_BLE_DISCONNECT), false, observer, -1);
            ContentResolverExt.registerContentObserver(context.getContentResolver(), Settings.Secure.getUriFor("sms_default_application"), false, observer, -1);
            ContentResolverExt.registerContentObserver(context.getContentResolver(), Settings.Secure.getUriFor("bluetooth_on"), false, observer, -1);
            ContentResolverExt.registerContentObserver(context.getContentResolver(), Settings.Global.getUriFor("unified_device_name"), false, observer, -1);
            this.mIsObsvInit.set(true);
        }
    }

    private void unregisterContentObserver(Context context, ContentObserver observer) {
        if (this.mIsObsvInit.get()) {
            context.getContentResolver().unregisterContentObserver(observer);
            this.mIsObsvInit.set(false);
        }
    }

    private void registerHsmProviderObserver(Context context, ContentObserver observer) {
        if (!this.mIsDozeObsvInit.get()) {
            Uri dozeUri = Uri.parse(URI_SYSTEM_MANAGER_UNIFIED_POWER_APP);
            if (!ContentResolverExt.isProviderNull(context.getContentResolver(), dozeUri)) {
                ContentResolverExt.registerContentObserver(context.getContentResolver(), dozeUri, true, observer, -1);
                this.mIsDozeObsvInit.set(true);
                return;
            }
            AwareLog.i(TAG, "register observer failed: doze database is not exist!");
        }
    }

    private void unregisterHsmProviderObserver(Context context, ContentObserver observer) {
        if (this.mIsDozeObsvInit.get()) {
            if (!ContentResolverExt.isProviderNull(context.getContentResolver(), Uri.parse(URI_SYSTEM_MANAGER_UNIFIED_POWER_APP))) {
                context.getContentResolver().unregisterContentObserver(observer);
                this.mIsDozeObsvInit.set(false);
                return;
            }
            AwareLog.i(TAG, "unregister observer failed: doze database is not exist!");
        }
    }

    /* access modifiers changed from: package-private */
    public static class AwareAppStartInfo {
        public int callerUid;
        public String cls = null;
        public String cmp;
        public long timeStamp;

        public AwareAppStartInfo(int callerUid2, String cmp2) {
            this.callerUid = callerUid2;
            this.cmp = cmp2;
            parseCmp(cmp2);
            this.timeStamp = SystemClock.elapsedRealtime();
        }

        private void parseCmp(String cmp2) {
            if (cmp2 != null) {
                String[] strs = cmp2.split("/");
                if (strs.length == 2) {
                    this.cls = strs[1];
                }
            }
        }

        public int hashCode() {
            return super.hashCode();
        }

        public boolean equals(Object obj) {
            String str;
            if (obj == null) {
                return false;
            }
            if (this == obj) {
                return true;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            AwareAppStartInfo info = (AwareAppStartInfo) obj;
            String str2 = this.cmp;
            if (str2 == null || (str = info.cmp) == null || this.callerUid != info.callerUid || !str2.equals(str)) {
                return false;
            }
            return true;
        }

        public String toString() {
            return "{caller:" + this.callerUid + "cmp:" + this.cmp + " time:" + this.timeStamp + "}";
        }
    }

    /* access modifiers changed from: package-private */
    public static class AlarmInfo {
        public boolean beHandled = false;
        public int count = 0;
        public boolean fgSet = false;
        public String packageName;
        public int perceptionCount = 0;
        public int reason;
        public long startTime = 0;
        public int status = 0;
        public String tag;
        public int uid;
        public int unPerceptionCount = 0;

        public AlarmInfo(int uid2, String packageName2, String tag2) {
            this.uid = uid2;
            this.packageName = packageName2;
            this.tag = tag2;
            this.beHandled = false;
        }

        public AlarmInfo copy() {
            AlarmInfo dst = new AlarmInfo(this.uid, this.packageName, this.tag);
            dst.uid = this.uid;
            dst.packageName = this.packageName;
            dst.tag = this.tag;
            dst.startTime = this.startTime;
            dst.status = this.status;
            dst.reason = this.reason;
            dst.count = this.count;
            dst.perceptionCount = this.perceptionCount;
            dst.unPerceptionCount = this.unPerceptionCount;
            dst.beHandled = this.beHandled;
            dst.fgSet = this.fgSet;
            return dst;
        }

        public String toString() {
            return "package:" + this.packageName + ",uid:" + this.uid + ",tag:" + this.tag + ",startTime:" + this.startTime + ",status:" + this.status + ",reason:" + this.reason + ",count:" + this.count + ",unPerceptionCount:" + this.unPerceptionCount + ",perceptionCount:" + this.perceptionCount + ",fgSet:" + this.fgSet + '.';
        }
    }

    public void dumpInputMethod(PrintWriter pw) {
        if (pw != null) {
            if (!sEnabled) {
                pw.println("AwareIntelligentRecg feature disabled.");
                return;
            }
            pw.println("[dump default InputMethod]");
            pw.println(this.mDefaultInputMethod + ",uid:" + this.mDefaultInputMethodUid);
        }
    }

    public void dumpWallpaper(PrintWriter pw) {
        if (pw != null) {
            if (!sEnabled) {
                pw.println("AwareIntelligentRecg feature disabled.");
                return;
            }
            pw.println("[dump Wallpaper]");
            pw.println(this.mDefaultWallPaper + ",uid:" + this.mDefaultWallPaperUid);
        }
    }

    public void dumpAccessibility(PrintWriter pw) {
        if (pw != null) {
            if (!sEnabled) {
                pw.println("AwareIntelligentRecg feature disabled.");
                return;
            }
            pw.println("[dump Accessibility]");
            synchronized (this.mAccessPkg) {
                for (String pkg : this.mAccessPkg) {
                    pw.println(pkg);
                }
            }
        }
    }

    public void dumpTts(PrintWriter pw) {
        if (pw != null) {
            if (!sEnabled) {
                pw.println("AwareIntelligentRecg feature disabled.");
                return;
            }
            pw.println("[dump TTS]");
            synchronized (this.mTtsPkg) {
                for (String pkg : this.mTtsPkg) {
                    pw.println(pkg);
                }
            }
        }
    }

    public void dumpPushSdk(PrintWriter pw) {
        if (pw != null) {
            if (!sEnabled) {
                pw.println("AwareIntelligentRecg feature disabled.");
                return;
            }
            pw.println("AwareIntelligentRecg dumpPushSdk start");
            synchronized (this.mPushSdkClsList) {
                pw.println("recg push sdk cls:" + this.mPushSdkClsList);
            }
            synchronized (this.mGoodPushSdkClsList) {
                pw.println("good push sdk cls:" + this.mGoodPushSdkClsList);
            }
            synchronized (this.mAppStartInfoList) {
                Iterator<AwareAppStartInfo> it = this.mAppStartInfoList.iterator();
                while (it.hasNext()) {
                    pw.println("start record:" + it.next().toString());
                }
            }
            ComponentRecoManager.getInstance().dumpBadComponent(pw);
            pw.println("AwareIntelligentRecg dumpPushSdk end");
        }
    }

    public void dumpSms(PrintWriter pw) {
        if (pw != null) {
            if (!sEnabled) {
                pw.println("AwareIntelligentRecg feature disabled.");
                return;
            }
            pw.println("[dump default sms package]");
            pw.println(this.mDefaultSms);
        }
    }

    private void updateGmsCallerApp(Context context) {
        SparseSet gmsUid = new SparseSet();
        Iterator<String> it = this.mGmsCallerAppPkg.iterator();
        while (it.hasNext()) {
            int uid = getUidByPackageName(context, it.next());
            if (uid != -1) {
                gmsUid.add(uid);
            }
        }
        this.mGmsCallerAppUid = gmsUid;
    }

    private void updateGmsCallerAppInit(Context context) {
        ArrayList<String> gmsList = DecisionMaker.getInstance().getRawConfig(AppMngConstant.AppMngFeature.APP_START.getDesc(), "gmscaller");
        ArraySet<String> gmsSet = new ArraySet<>();
        if (gmsList != null) {
            gmsSet.addAll(gmsList);
        }
        this.mGmsCallerAppPkg = gmsSet;
        updateGmsCallerApp(context);
    }

    private void updateGmsCallerAppForInstall(String pkg) {
        MultiTaskManagerService multiTaskManagerService;
        if (this.mGmsCallerAppPkg.contains(pkg) && (multiTaskManagerService = this.mMtmService) != null) {
            updateGmsCallerApp(multiTaskManagerService.context());
        }
    }

    public void appStartEnable(AppStartupDataMgr dataMgr, Context context) {
        this.mIsAbroadArea = AwareDefaultConfigList.isAbroadArea();
        this.mDataMgr = dataMgr;
        updateGmsCallerAppInit(context);
        initAppMngCustPropConfig();
        initGmsAppConfig();
        initMemoryInfo();
        initCommonInfo();
        this.mAppStartEnabled = true;
    }

    public void appStartDisable() {
        this.mAppStartEnabled = false;
    }

    public void dumpToastWindow(PrintWriter pw) {
        if (pw != null) {
            if (!sEnabled) {
                pw.println("AwareIntelligentRecg feature disabled.");
                return;
            }
            SparseSet toasts = new SparseSet();
            SparseSet toastsEvil = new SparseSet();
            getToastWindows(toasts, toastsEvil);
            pw.println("");
            pw.println("[ToastList]:" + toasts);
            pw.println("[EvilToastList]:" + toastsEvil);
            synchronized (this.mToasts) {
                for (int i = this.mToasts.size() - 1; i >= 0; i += -1) {
                    AwareProcessWindowInfo toastInfo = this.mToasts.valueAt(i);
                    pw.println("[Toast pid ]:" + this.mToasts.keyAt(i) + " pkg:" + toastInfo.pkg + " isEvil:" + toastInfo.isEvil());
                }
            }
        }
    }

    public void dumpIsVisibleWindow(PrintWriter pw, int userId, String pkg, int type) {
        if (pw != null) {
            if (!sEnabled) {
                pw.println("AwareIntelligentRecg feature disabled.");
                return;
            }
            pw.println("");
            boolean result = HwSysResManager.getInstance().isVisibleWindow(userId, pkg, type);
            pw.println("[dumpIsVisibleWindow ]:" + result);
        }
    }

    public void dumpDefaultTts(PrintWriter pw) {
        if (pw != null) {
            if (!sEnabled) {
                pw.println("AwareIntelligentRecg feature disabled.");
                return;
            }
            pw.println("[dump Default TTS]");
            pw.println(this.mDefaultTts);
        }
    }

    public void dumpDefaultAppType(PrintWriter pw) {
        if (pw != null) {
            if (!sEnabled) {
                pw.println("AwareIntelligentRecg feature disabled.");
                return;
            }
            pw.println("[dump Pay Pkg]");
            Set<String> set = this.mPayPkgList;
            if (set == null) {
                set = "None";
            }
            pw.println(set);
            pw.println("[dump Share Pkg]");
            Set<String> set2 = this.mSharePkgList;
            if (set2 == null) {
                set2 = "None";
            }
            pw.println(set2);
            pw.println("[dump ActTopIMCN]");
            pw.println(this.mActTopImCn);
            pw.println("[dump AppLockClass]");
            pw.println(this.mAppLockClass);
        }
    }

    public void dumpFrozen(PrintWriter pw, int uid) {
        if (pw != null) {
            if (!sEnabled) {
                pw.println("AwareIntelligentRecg feature disabled.");
                return;
            }
            pw.println("AwareIntelligentRecg dumpFrozen start");
            synchronized (this.mFrozenAppList) {
                if (uid == 0) {
                    for (int i = this.mFrozenAppList.size() - 1; i >= 0; i += -1) {
                        int appUid = this.mFrozenAppList.keyAt(i);
                        pw.println("frozen app:" + InnerUtils.getPackageNameByUid(appUid) + ",uid:" + appUid);
                    }
                } else {
                    pw.println("uid:" + uid + ",frozen:" + isAppFrozen(uid));
                }
            }
        }
    }

    public void dumpBluetooth(PrintWriter pw, int uid) {
        if (pw != null) {
            if (!sEnabled) {
                pw.println("AwareIntelligentRecg feature disabled.");
                return;
            }
            pw.println("AwareIntelligentRecg dumpBluetooth start");
            synchronized (this.mBluetoothAppList) {
                if (uid == 0) {
                    for (int i = this.mBluetoothAppList.size() - 1; i >= 0; i += -1) {
                        int appUid = this.mBluetoothAppList.keyAt(i);
                        pw.println("bluetooth app:" + InnerUtils.getPackageNameByUid(appUid) + ",uid:" + appUid);
                    }
                } else {
                    pw.println("uid:" + uid + ",bluetooth:" + isAppBluetooth(uid));
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resolvePkgsName(String pkgs, int eventType) {
        if (eventType == 1) {
            if (pkgs != null) {
                this.mHandler.removeMessages(MSG_UNFREEZE_NOT_CLEAN);
                String[] pkglist = pkgs.split("#");
                synchronized (this.mNotCleanPkgList) {
                    for (String str : pkglist) {
                        this.mNotCleanPkgList.add(str);
                    }
                }
            }
        } else if (eventType == 0) {
            this.mHandler.sendEmptyMessageDelayed(MSG_UNFREEZE_NOT_CLEAN, (long) this.mNotCleanDuration);
        }
    }

    public boolean isNotBeClean(String pkg) {
        boolean contains;
        synchronized (this.mNotCleanPkgList) {
            contains = this.mNotCleanPkgList.contains(pkg);
        }
        return contains;
    }

    public void dumpNotClean(PrintWriter pw) {
        if (pw != null) {
            if (!sEnabled) {
                pw.println("AwareIntelligentRecg feature disabled.");
                return;
            }
            pw.println("AwareIntelligentRecg dumpNotClean start");
            StringBuffer stringBuffer = new StringBuffer();
            synchronized (this.mNotCleanPkgList) {
                for (String pkgName : this.mNotCleanPkgList) {
                    stringBuffer.append(pkgName);
                    stringBuffer.append(" ");
                }
            }
            pw.println(stringBuffer.toString());
        }
    }

    public void dumpKbgApp(PrintWriter pw) {
        if (pw != null) {
            if (!sEnabled) {
                pw.println("AwareIntelligentRecg feature disabled.");
                return;
            }
            pw.println("AwareIntelligentRecg dumpKbgApp start");
            synchronized (this.mStatusAudioIn) {
                for (int i = this.mStatusAudioIn.size() - 1; i >= 0; i += -1) {
                    pw.println("mStatusAudioIn app:" + this.mStatusAudioIn.keyAt(i));
                }
            }
            synchronized (this.mStatusAudioOut) {
                for (int i2 = this.mStatusAudioOut.size() - 1; i2 >= 0; i2 += -1) {
                    pw.println("mStatusAudioOut app:" + this.mStatusAudioOut.keyAt(i2));
                }
            }
            synchronized (this.mStatusGps) {
                for (int i3 = this.mStatusGps.size() - 1; i3 >= 0; i3 += -1) {
                    pw.println("mStatusGps app:" + this.mStatusGps.keyAt(i3));
                }
            }
            synchronized (this.mStatusUpDown) {
                for (int i4 = this.mStatusUpDown.size() - 1; i4 >= 0; i4 += -1) {
                    pw.println("mStatusUpDown app:" + this.mStatusUpDown.keyAt(i4));
                }
            }
            synchronized (this.mStatusSensor) {
                for (int i5 = this.mStatusSensor.size() - 1; i5 >= 0; i5 += -1) {
                    pw.println("mStatusSensor app:" + this.mStatusSensor.keyAt(i5));
                }
            }
            synchronized (this.mStatusUpDownElapse) {
                long timeCur = SystemClock.elapsedRealtime();
                for (Map.Entry<Integer, Long> entry : this.mStatusUpDownElapse.entrySet()) {
                    int uid = entry.getKey().intValue();
                    long time = entry.getValue().longValue();
                    pw.println("mStatusUpDown History:" + uid + ", " + ((timeCur - time) / 1000));
                }
            }
            pw.println("mIsScreenOnPm:" + this.mIsScreenOnPm);
            pw.println("mAppStartAreaCfg:" + this.mAppStartAreaCfg);
            pw.println("mDeviceLevel:" + this.mDeviceLevel);
        }
    }

    public void dumpAlarms(PrintWriter pw) {
        if (pw != null) {
            if (!sEnabled) {
                pw.println("AwareIntelligentRecg feature disabled.");
                return;
            }
            pw.println("AwareIntelligentRecg dumpAlarms start");
            synchronized (this.mAlarmMap) {
                for (int i = this.mAlarmMap.size() - 1; i >= 0; i--) {
                    ArrayMap<String, AlarmInfo> alarms = this.mAlarmMap.valueAt(i);
                    if (alarms != null) {
                        Iterator<String> it = alarms.keySet().iterator();
                        while (it.hasNext()) {
                            pw.println("AwareIntelligentRecg alarm:" + alarms.get(it.next()));
                        }
                    } else {
                        return;
                    }
                }
            }
        }
    }

    public void dumpAlarmActions(PrintWriter pw) {
        if (pw != null) {
            if (!sEnabled) {
                pw.println("AwareIntelligentRecg feature disabled.");
                return;
            }
            pw.println("AwareIntelligentRecg dumpAlarmActions start");
            synchronized (this.mAlarmCmps) {
                for (Map.Entry<String, CmpTypeInfo> entry : this.mAlarmCmps.entrySet()) {
                    CmpTypeInfo info = entry.getValue();
                    StringBuilder sb = new StringBuilder();
                    sb.append("AwareIntelligentRecg alarm action:");
                    sb.append(info != null ? info.toString() : "");
                    pw.println(sb.toString());
                }
            }
            pw.println("Alarm White Pkg:");
            Object obj = this.mAlarmPkgList;
            if (obj == null) {
                obj = "None";
            }
            pw.println(obj);
            pw.println("UnRemind App Types:");
            Object obj2 = this.mUnRemindAppTypeList;
            if (obj2 == null) {
                obj2 = "None";
            }
            pw.println(obj2);
        }
    }

    public void dumpHwStopList(PrintWriter pw) {
        if (pw != null) {
            if (!sEnabled) {
                pw.println("AwareIntelligentRecg feature disabled.");
                return;
            }
            pw.println("AwareIntelligentRecg dumpHwStopList start");
            synchronized (this.mHwStopUserIdPkg) {
                Iterator<String> it = this.mHwStopUserIdPkg.iterator();
                while (it.hasNext()) {
                    pw.println("  pkg#userId : " + it.next());
                }
            }
        }
    }

    public void dumpScreenRecording(PrintWriter pw) {
        if (pw != null) {
            if (!sEnabled) {
                pw.println("AwareIntelligentRecg feature disabled.");
                return;
            }
            pw.println("AwareIntelligentRecg dumpScreenRecord uid start");
            synchronized (this.mScreenRecordAppList) {
                for (int i = this.mScreenRecordAppList.size() - 1; i >= 0; i += -1) {
                    pw.println(" screenRecord uid:" + this.mScreenRecordAppList.keyAt(i));
                }
            }
            pw.println("AwareIntelligentRecg dumpScreenRecord pid start");
            synchronized (this.mScreenRecordPidList) {
                for (int i2 = this.mScreenRecordPidList.size() - 1; i2 >= 0; i2 += -1) {
                    pw.println(" screenRecord pid:" + this.mScreenRecordPidList.keyAt(i2));
                }
            }
        }
    }

    public void dumpCameraRecording(PrintWriter pw) {
        if (pw != null) {
            if (!sEnabled) {
                pw.println("AwareIntelligentRecg feature disabled.");
                return;
            }
            pw.println("AwareIntelligentRecg dumpCameraRecording start");
            synchronized (this.mCameraUseAppList) {
                for (int i = this.mCameraUseAppList.size() - 1; i >= 0; i--) {
                    int uid = this.mCameraUseAppList.keyAt(i);
                    if (!AwareAppAssociate.getInstance().isForeGroundApp(uid)) {
                        if (AwareAppAssociate.getInstance().hasWindow(uid)) {
                            pw.println("  cameraRecord uid:" + uid + " PKG is " + InnerUtils.getPackageNameByUid(uid));
                        }
                    }
                }
            }
        }
    }

    public void dumpGmsCallerList(PrintWriter pw) {
        if (pw != null) {
            if (!sEnabled) {
                pw.println("AwareIntelligentRecg feature disabled.");
                return;
            }
            pw.println("AwareIntelligentRecg dumpGmsCallerList start");
            pw.println("Gms UID:" + this.mGmsCallerAppUid);
            pw.println("Gms PKG:" + this.mGmsCallerAppPkg);
        }
    }

    public void dumpWidgetUpdateInterval(PrintWriter pw, int intervalMs) {
        if (pw != null) {
            if (!sEnabled) {
                pw.println("AwareIntelligentRecg feature disabled.");
                return;
            }
            pw.println("mWidgetCheckUpdateCnt " + this.mWidgetCheckUpdateCnt);
            pw.println("mWidgetCheckUpdateInterval raw(ms) " + this.mWidgetCheckUpdateInterval);
            this.mWidgetCheckUpdateInterval = (long) intervalMs;
            sendMsgWidgetUpDownLoadClear();
            pw.println("mWidgetCheckUpdateInterval current(ms) " + this.mWidgetCheckUpdateInterval);
        }
    }

    public static void commEnable() {
        getInstance().initialize();
        sEnabled = true;
    }

    public static void commDisable() {
        sEnabled = false;
        getInstance().deInitialize();
    }

    public static void enableDebug() {
        sDebug = true;
    }

    public static void disableDebug() {
        sDebug = false;
    }

    public void registerToastCallback(IAwareToastCallback callback) {
        if (callback != null) {
            synchronized (this.mCallbacks) {
                if (!this.mCallbacks.contains(callback)) {
                    this.mCallbacks.add(callback);
                }
            }
        }
    }

    public void unregisterToastCallback(IAwareToastCallback callback) {
        if (callback != null) {
            synchronized (this.mCallbacks) {
                if (this.mCallbacks.contains(callback)) {
                    this.mCallbacks.remove(callback);
                }
            }
        }
    }

    private void notifyToastChange(int type, int pid) {
        synchronized (this.mCallbacks) {
            if (!this.mCallbacks.isEmpty()) {
                int size = this.mCallbacks.size();
                for (int i = 0; i < size; i++) {
                    this.mCallbacks.valueAt(i).onToastWindowsChanged(type, pid);
                }
            }
        }
    }

    public boolean isBtMediaBrowserCaller(int callerUid, String action) {
        if (callerUid != this.mBluetoothUid || !BT_MEDIA_BROWSER_ACTION.equals(action)) {
            return false;
        }
        return true;
    }

    public boolean isNotifyListenerCaller(int callerUid, String action, WindowProcessControllerEx callerApp) {
        if (callerApp == null || callerApp.isWindowProcessControllerNull() || callerUid != 1000 || !SYSTEM_PROCESS_NAME.equals(callerApp.getName()) || !NOTIFY_LISTENER_ACTION.equals(action)) {
            return false;
        }
        return true;
    }

    public boolean isGmsCaller(int callerUid) {
        if (!this.mAppStartEnabled) {
            return false;
        }
        return this.mGmsCallerAppUid.contains(UserHandleEx.getAppId(callerUid));
    }

    @SuppressLint({"PreferForInArrayList"})
    public boolean isAchScreenChangedNum(AwareProcessInfo awareProcInfo) {
        if (!sEnabled || awareProcInfo == null || awareProcInfo.procProcInfo == null) {
            return false;
        }
        Iterator it = awareProcInfo.procProcInfo.mPackageName.iterator();
        while (it.hasNext()) {
            if (!isScreenChangedMeetCondition((String) it.next())) {
                return false;
            }
        }
        return true;
    }

    public void reportScreenChangedTime(long currTime) {
        synchronized (this.mScreenChangedTime) {
            this.mScreenChangedTime.add(Long.valueOf(currTime));
            while (AppMngConfig.getScreenChangedThreshold() < this.mScreenChangedTime.size()) {
                this.mScreenChangedTime.removeFirst();
            }
        }
    }

    public void reportAppChangeToBackground(String pkg, long currTime) {
        if (pkg != null) {
            synchronized (this.mAppChangeToBgTime) {
                this.mAppChangeToBgTime.put(pkg, Long.valueOf(currTime));
            }
        }
    }

    private boolean isScreenChangedMeetCondition(String pkg) {
        long screenChangedTime;
        long appChangedToBgTime;
        if (pkg == null) {
            return false;
        }
        synchronized (this.mScreenChangedTime) {
            int size = this.mScreenChangedTime.size();
            if (AppMngConfig.getScreenChangedThreshold() <= size) {
                if (size > 0) {
                    screenChangedTime = this.mScreenChangedTime.getFirst().longValue();
                }
            }
            return false;
        }
        synchronized (this.mAppChangeToBgTime) {
            if (!this.mAppChangeToBgTime.containsKey(pkg)) {
                return false;
            }
            appChangedToBgTime = this.mAppChangeToBgTime.get(pkg).longValue();
        }
        if (screenChangedTime < appChangedToBgTime) {
            return false;
        }
        return true;
    }

    public boolean isCurrentUser(int checkUid, int currentUserId) {
        UserManager userManager;
        int userId = UserHandleEx.getUserId(checkUid);
        boolean isCloned = false;
        MultiTaskManagerService multiTaskManagerService = this.mMtmService;
        if (!(multiTaskManagerService == null || (userManager = UserManagerExt.get(multiTaskManagerService.context())) == null)) {
            UserInfoExAdapter info = UserInfoExAdapter.getUserInfo(userManager, userId);
            isCloned = info != null ? info.isClonedProfile() : false;
        }
        if (userId == currentUserId || (isCloned && currentUserId == 0)) {
            return true;
        }
        return false;
    }

    public boolean isWebViewUid(int uid) {
        return uid == this.mWebViewUid;
    }

    private void removeHwStopFlagByUserId(int removeUserId) {
        if (sEnabled) {
            String removeUserIdStr = String.valueOf(removeUserId);
            synchronized (this.mHwStopUserIdPkg) {
                Iterator<String> it = this.mHwStopUserIdPkg.iterator();
                while (it.hasNext()) {
                    String[] strs = it.next().split("#");
                    int strSize = strs.length;
                    if (strSize > 1 && removeUserIdStr.equals(strs[strSize - 1])) {
                        it.remove();
                    }
                }
            }
        }
    }

    private boolean isCloneUserId(int userId) {
        UserManager userManager;
        UserInfoExAdapter info;
        MultiTaskManagerService multiTaskManagerService = this.mMtmService;
        if (multiTaskManagerService == null || (userManager = UserManagerExt.get(multiTaskManagerService.context())) == null || (info = UserInfoExAdapter.getUserInfo(userManager, userId)) == null) {
            return false;
        }
        return info.isClonedProfile();
    }

    public void setHwStopFlag(int userId, String pkg, boolean hwStop) {
        if (sEnabled && pkg != null && !"".equals(pkg)) {
            String userIdPkg = pkg + "#" + userId;
            synchronized (this.mHwStopUserIdPkg) {
                if (hwStop) {
                    this.mHwStopUserIdPkg.add(userIdPkg);
                } else {
                    this.mHwStopUserIdPkg.remove(userIdPkg);
                }
            }
        }
    }

    public boolean isPkgHasHwStopFlag(int userId, String pkg) {
        boolean isHwStopPkg;
        if (!sEnabled || pkg == null || "".equals(pkg)) {
            return false;
        }
        String userIdPkg = pkg + "#" + userId;
        synchronized (this.mHwStopUserIdPkg) {
            isHwStopPkg = this.mHwStopUserIdPkg.contains(userIdPkg);
        }
        if (sDebug && isHwStopPkg) {
            AwareLog.i(TAG, "pkg:" + pkg + " userId:" + userId + " is hwStop pkg.");
        }
        return isHwStopPkg;
    }

    public void updateScreenOnFromPm(boolean wakenessChange) {
        if (sEnabled && wakenessChange) {
            sendScreenOnFromPmMsg(false);
        }
    }

    private void sendScreenOnFromPmMsg(boolean delay) {
        IntlRecgHandler intlRecgHandler = this.mHandler;
        if (intlRecgHandler != null) {
            Message msg = intlRecgHandler.obtainMessage();
            msg.what = MSG_WAKENESS_CHANGE;
            if (delay) {
                this.mHandler.sendMessageDelayed(msg, 1000);
            } else {
                this.mHandler.sendMessage(msg);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleWakenessChangeHandle() {
        MultiTaskManagerService multiTaskManagerService = this.mMtmService;
        if (multiTaskManagerService != null) {
            PowerManager powerManager = null;
            Object obj = multiTaskManagerService.context().getSystemService("power");
            if (obj instanceof PowerManager) {
                powerManager = (PowerManager) obj;
            }
            if (powerManager != null) {
                this.mIsScreenOnPm = powerManager.isScreenOn();
            }
        }
    }

    private void initAppMngCustPropConfig() {
        if (!this.mAppMngPropCfgInit) {
            this.mAppMngPropCfgInit = true;
            String prop = SystemPropertiesEx.get("ro.config.iaware_appmngconfigs");
            if (prop != null && !"".equals(prop)) {
                String[] strs = prop.split(SEPARATOR_COMMA);
                if (strs.length >= 1) {
                    try {
                        this.mAppStartAreaCfg = Integer.parseInt(strs[0]);
                    } catch (NumberFormatException e) {
                        AwareLog.i(TAG, "initAppMngCustPropConfig error");
                    }
                }
            }
        }
    }

    public Bundle getTypeTopN(int[] appTypes) {
        List<String> topN;
        int[] iArr = appTypes;
        Bundle result = new Bundle();
        if (iArr == null || iArr.length == 0) {
            return result;
        }
        AwareUserHabit habit = AwareUserHabit.getInstance();
        if (habit == null) {
            AwareLog.e(TAG, "AwareUserHabit is null");
            return result;
        }
        LinkedHashMap<String, Long> lruCache = habit.getLruCache();
        if (lruCache == null) {
            return result;
        }
        ArrayList<String> listOfTopN = new ArrayList<>();
        ArrayList<Integer> typeList = new ArrayList<>();
        int i = 0;
        int size = iArr.length;
        while (i < size) {
            int type = iArr[i];
            if (type >= 0 && (topN = habit.getMostFreqAppByType(AppTypeRecoManager.getInstance().convertType(type), SPEC_VALUE_DEFAULT_MAX_TOPN)) != null) {
                long now = SystemClock.elapsedRealtime();
                int sizeTopN = topN.size();
                for (int j = 0; j < sizeTopN; j++) {
                    String name = topN.get(j);
                    Long lastUseTime = lruCache.get(name);
                    if (lastUseTime != null && now - lastUseTime.longValue() < 172800000) {
                        listOfTopN.add(name);
                        typeList.add(Integer.valueOf(type));
                    }
                }
            }
            i++;
            iArr = appTypes;
        }
        result.putIntegerArrayList("type", typeList);
        result.putStringArrayList("pkg", listOfTopN);
        return result;
    }

    private AwareBroadcastPolicy getAwareBrPolicy() {
        if (this.mIawareBrPolicy == null && MultiTaskManagerService.self() != null) {
            this.mIawareBrPolicy = MultiTaskManagerService.self().getAwareBrPolicy();
        }
        return this.mIawareBrPolicy;
    }

    public void updateBgCheckExcludedInfo(ArraySet<String> excludedPkg) {
        this.mBgCheckExcludedPkg = excludedPkg;
        ArrayList<String> excludedAction = DecisionMaker.getInstance().getRawConfig(AppMngConstant.AppMngFeature.APP_START.getDesc(), BACKGROUND_CHECK_EXCLUDED_ACTION);
        if (excludedAction != null) {
            this.mBgCheckExcludedAction = new ArraySet<>(excludedAction);
        }
    }

    public boolean isExcludedInBgCheck(String pkg, String action) {
        boolean result = false;
        if (action != null && this.mBgCheckExcludedAction.contains(action)) {
            result = true;
        }
        if (pkg == null || !this.mBgCheckExcludedPkg.contains(pkg)) {
            return result;
        }
        return true;
    }

    public void dumpBgCheckExcludeInfo(PrintWriter pw) {
        if (pw != null) {
            if (!sEnabled) {
                pw.println("AwareIntelligentRecg feature disabled.");
                return;
            }
            pw.println("BGCheckExcludedAction :" + this.mBgCheckExcludedAction);
            pw.println("BGCheckExcludedPkg :" + this.mBgCheckExcludedPkg);
        }
    }

    private void initGmsAppConfig() {
        if (!this.mIsAbroadArea) {
            ArrayList<String> gmsList = DecisionMaker.getInstance().getRawConfig(AppMngConstant.AppMngFeature.APP_START.getDesc(), "gmsapp");
            this.mIsInitGoogleConfig = true;
            ArraySet<String> gmsPkgs = new ArraySet<>();
            if (gmsList != null) {
                gmsPkgs.addAll(gmsList);
            }
            this.mGmsAppPkg = gmsPkgs;
            ArrayList<String> delayTime = DecisionMaker.getInstance().getRawConfig(AppMngConstant.AppMngFeature.APP_START.getDesc(), "google_delaytime");
            if (delayTime != null && delayTime.size() == 1) {
                try {
                    this.mGoogleConnDealyTime = Long.parseLong(delayTime.get(0)) * 1000;
                } catch (NumberFormatException e) {
                    AwareLog.i(TAG, "initGoogledelayTime error");
                }
            }
            initGoogleConnTime();
            removeAppStartFeatureGmsList();
        }
    }

    private void initMemoryInfo() {
        MemInfoReaderExt minfo = new MemInfoReaderExt();
        minfo.readMemInfo();
        this.mDeviceTotalMemory = minfo.getTotalSize() / MemoryConstant.MB_SIZE;
        if (this.mDeviceTotalMemory == -1) {
            AwareLog.e(TAG, "read device memory faile");
        }
        this.mDeviceMemoryOfGiga = calMemorySizeOfGiga(this.mDeviceTotalMemory);
        AwareLog.i(TAG, "Current Device Total Memory is " + this.mDeviceTotalMemory + " and about " + this.mDeviceMemoryOfGiga + "G");
    }

    private void initGoogleConnTime() {
        String prop = SystemPropertiesEx.get(PROPERTIES_GOOELE_CONNECTION);
        if (prop == null || "".equals(prop)) {
            this.mGoogleConnStat = false;
            this.mGoogleDisConnTime = 0;
            this.mGoogleConnStatDecay = false;
            return;
        }
        String[] strs = prop.split(SEPARATOR_COMMA);
        if (strs.length < 2) {
            this.mGoogleConnStat = false;
            this.mGoogleDisConnTime = 0;
            this.mGoogleConnStatDecay = false;
            return;
        }
        try {
            this.mGoogleConnStat = Integer.parseInt(strs[1]) == 1;
            this.mGoogleDisConnTime = Long.parseLong(strs[0]) * 1000;
            long now = System.currentTimeMillis();
            if (this.mGoogleConnStat) {
                this.mGoogleConnStatDecay = true;
            } else if (now - this.mGoogleDisConnTime < this.mGoogleConnDealyTime) {
                this.mGoogleConnStatDecay = true;
            } else {
                this.mGoogleConnStatDecay = false;
            }
        } catch (NumberFormatException e) {
            AwareLog.i(TAG, "initGoogleConnTime error");
        }
    }

    public boolean isGmsApp(String pkg) {
        return this.mGmsAppPkg.contains(pkg);
    }

    public boolean isGmsAppAndNeedCtrl(String pkg) {
        if (!this.mIsAbroadArea && !this.mGoogleConnStatDecay && isGmsApp(pkg)) {
            return true;
        }
        return false;
    }

    public void reportGoogleConn(boolean conn) {
        if (getAwareBrPolicy() != null) {
            this.mIawareBrPolicy.reportGoogleConn(conn);
        }
        if (!this.mIsAbroadArea) {
            if (!conn) {
                long now = System.currentTimeMillis();
                if (this.mGoogleConnStat) {
                    this.mGoogleDisConnTime = now;
                } else if (now - this.mGoogleDisConnTime >= this.mGoogleConnDealyTime && this.mGoogleConnStatDecay) {
                    this.mGoogleConnStatDecay = false;
                    removeFeatureGmsList();
                }
            } else if (!this.mGoogleConnStatDecay) {
                this.mGoogleConnStatDecay = true;
                addFeatureGmsList();
            }
            if (this.mGoogleConnStat != conn) {
                this.mGoogleConnStat = conn;
                restoreGoogleConnTime();
            }
        }
    }

    private void addFeatureGmsList() {
        DecisionMaker.getInstance().addFeatureList(AppMngConstant.AppMngFeature.APP_CLEAN, this.mGmsAppCleanPolicyList);
        DecisionMaker.getInstance().addFeatureList(AppMngConstant.AppMngFeature.APP_START, this.mGmsAppStartPolicyList);
    }

    private void removeFeatureGmsList() {
        removeAppStartFeatureGmsList();
        removeAppCleanFeatureGmsList();
    }

    private void restoreGoogleConnTime() {
        String connInfo = this.mGoogleConnStat ? SWITCH_STATUS_STR : "0";
        SystemPropertiesEx.set(PROPERTIES_GOOELE_CONNECTION, String.valueOf(this.mGoogleDisConnTime / 1000) + ", " + connInfo);
    }

    public void removeAppStartFeatureGmsList() {
        if (this.mIsInitGoogleConfig && !this.mIsAbroadArea && !this.mGoogleConnStatDecay) {
            this.mGmsAppStartPolicyList = DecisionMaker.getInstance().removeFeatureList(AppMngConstant.AppMngFeature.APP_START, this.mGmsAppPkg);
        }
    }

    public void removeAppCleanFeatureGmsList() {
        if (this.mIsInitGoogleConfig && !this.mIsAbroadArea && !this.mGoogleConnStatDecay) {
            this.mGmsAppCleanPolicyList = DecisionMaker.getInstance().removeFeatureList(AppMngConstant.AppMngFeature.APP_CLEAN, this.mGmsAppPkg);
        }
    }

    public void dumpGmsAppList(PrintWriter pw) {
        if (pw != null) {
            if (!sEnabled) {
                pw.println("AwareIntelligentRecg feature disabled.");
                return;
            }
            pw.println("AwareIntelligentRecg dumpGmsAppList start");
            Iterator<String> it = this.mGmsAppPkg.iterator();
            while (it.hasNext()) {
                pw.println("gms app pkgName: " + it.next());
            }
            pw.println("google connection delay: " + this.mGoogleConnStatDecay);
            pw.println("google connection: " + this.mGoogleConnStat);
            pw.println("google disconn time: " + this.mGoogleDisConnTime);
            pw.println("google delay time: " + this.mGoogleConnDealyTime);
            pw.println("google config init: " + this.mIsInitGoogleConfig);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void refreshAlivedApps(int eventType, String pkgAndTime, int userId) {
        if (pkgAndTime != null && !pkgAndTime.isEmpty()) {
            String[] splitRet = new String[2];
            int splitIndex = pkgAndTime.indexOf("#");
            try {
                splitRet[0] = pkgAndTime.substring(0, splitIndex);
                splitRet[1] = pkgAndTime.substring(splitIndex + 1);
            } catch (IndexOutOfBoundsException e) {
                AwareLog.i(TAG, "IndexOutOfBoundsException! pkgAndTime is: " + pkgAndTime);
            }
            if (splitRet[0] == null || splitRet[0].trim().isEmpty()) {
                AwareLog.i(TAG, "get pkg failed : " + pkgAndTime);
                return;
            }
            synchronized (this.mRegKeepAlivePkgs) {
                ArrayMap<String, Long> appMap = this.mRegKeepAlivePkgs.get(userId);
                if (eventType == 1) {
                    long elapsedRealtime = strToLong(splitRet[1]);
                    if (elapsedRealtime <= SystemClock.elapsedRealtime()) {
                        AwareLog.d(TAG, elapsedRealtime + " < " + SystemClock.elapsedRealtime());
                        return;
                    }
                    if (appMap == null) {
                        appMap = new ArrayMap<>();
                    }
                    AwareLog.d(TAG, "pkg: " + splitRet[0] + ", elapsedRealtime: " + elapsedRealtime);
                    appMap.put(splitRet[0], Long.valueOf(elapsedRealtime));
                    this.mRegKeepAlivePkgs.put(userId, appMap);
                    return;
                }
                if (eventType == 2) {
                    if (appMap != null) {
                        appMap.remove(splitRet[0]);
                    }
                    if (appMap == null || appMap.isEmpty()) {
                        this.mRegKeepAlivePkgs.remove(userId);
                    }
                }
            }
        }
    }

    private long strToLong(String str) {
        if (str == null || str.trim().isEmpty()) {
            return 0;
        }
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            AwareLog.i(TAG, "number format exception! str is: " + str);
            return 0;
        }
    }

    public boolean isCurUserKeepALive(String pkg, int uid) {
        int userId;
        if (pkg == null || pkg.isEmpty() || (userId = UserHandleEx.getUserId(uid)) != 0) {
            return false;
        }
        synchronized (this.mRegKeepAlivePkgs) {
            ArrayMap<String, Long> appMap = this.mRegKeepAlivePkgs.get(userId);
            if (appMap != null) {
                if (!appMap.isEmpty()) {
                    if (!appMap.containsKey(pkg)) {
                        AwareLog.d(TAG, pkg + " is not in iaware alive record.");
                        return false;
                    }
                    long keptTime = appMap.get(pkg).longValue() - SystemClock.elapsedRealtime();
                    AwareLog.d(TAG, pkg + " still need kept in iaware for " + keptTime + " ms");
                    if (keptTime <= 0) {
                        appMap.remove(pkg);
                        return false;
                    } else if (keptTime < DEFAULT_REQUEST_TIMEOUT) {
                        return true;
                    } else {
                        if (isKeptAliveAppByPg(pkg, uid)) {
                            return true;
                        }
                        appMap.remove(pkg);
                        return false;
                    }
                }
            }
            return false;
        }
    }

    private boolean isKeptAliveAppByPg(String pkg, int uid) {
        MultiTaskManagerService multiTaskManagerService;
        Context context;
        if (this.mPgSdk == null || (multiTaskManagerService = this.mMtmService) == null || (context = multiTaskManagerService.context()) == null) {
            return false;
        }
        try {
            return this.mPgSdk.isKeptAliveApp(context, pkg, uid);
        } catch (RemoteException e) {
            if (sDebug) {
                AwareLog.w(TAG, "call isKeptAliveApp happened RemoteException.");
            }
            return false;
        }
    }

    public void dumpKeepAlivePkgs(PrintWriter pw) {
        if (pw != null) {
            if (!sEnabled) {
                pw.println("AwareIntelligentRecg feature disabled.");
                return;
            }
            pw.println("AwareIntelligentRecg dumpKeepAlivePkgs start");
            StringBuffer stringBuffer = new StringBuffer();
            synchronized (this.mRegKeepAlivePkgs) {
                for (int i = this.mRegKeepAlivePkgs.size() - 1; i >= 0; i--) {
                    ArrayMap<String, Long> pkgMap = this.mRegKeepAlivePkgs.valueAt(i);
                    if (pkgMap != null) {
                        if (!pkgMap.isEmpty()) {
                            long current = SystemClock.elapsedRealtime();
                            stringBuffer.append("====userId:");
                            stringBuffer.append(this.mRegKeepAlivePkgs.keyAt(i));
                            stringBuffer.append(", cached size:");
                            stringBuffer.append(pkgMap.size());
                            stringBuffer.append(", elapsedRealtime:");
                            stringBuffer.append(current);
                            stringBuffer.append("====");
                            for (String pkg : pkgMap.keySet()) {
                                stringBuffer.append(System.lineSeparator());
                                stringBuffer.append("{");
                                stringBuffer.append(pkg);
                                stringBuffer.append(" ");
                                stringBuffer.append(pkgMap.get(pkg));
                                stringBuffer.append(" ");
                                stringBuffer.append(pkgMap.get(pkg).longValue() - current);
                                stringBuffer.append("}");
                            }
                        }
                    }
                }
            }
            stringBuffer.append(System.lineSeparator());
            pw.println(stringBuffer.toString());
        }
    }

    private int calMemorySizeOfGiga(long deviceMemory) {
        if (deviceMemory == -1) {
            return -1;
        }
        if (deviceMemory % 1024 == 0) {
            return (int) (deviceMemory / 1024);
        }
        return (int) ((deviceMemory / 1024) + 1);
    }

    public int getMemorySize() {
        return this.mDeviceMemoryOfGiga;
    }

    private void initCommonInfo() {
        initCommonActTopIm();
        initCommonAppLockClass();
    }

    private void initCommonActTopIm() {
        String actTopImCn = DecisionMaker.getInstance().getCommonCfg(AppMngConstant.AppMngFeature.COMMON.getDesc(), ACT_TOP_IM_CN);
        if (actTopImCn != null) {
            this.mActTopImCn = actTopImCn;
            if (!this.mActTopImCn.equals(SystemPropertiesEx.get(TOP_IM_CN_PROP, UNKNOWN_PKG))) {
                SystemPropertiesEx.set(TOP_IM_CN_PROP, this.mActTopImCn);
            }
        }
    }

    private void initCommonAppLockClass() {
        String appLock = DecisionMaker.getInstance().getCommonCfg(AppMngConstant.AppMngFeature.COMMON.getDesc(), APP_LOCK_CLASS);
        if (appLock != null) {
            this.mAppLockClass = appLock;
        }
    }

    public String getActTopIMCN() {
        if (sEnabled || this.mAppStartEnabled) {
            return this.mActTopImCn;
        }
        return UNKNOWN_PKG;
    }

    private int getAppTypeActTop(String pkgName, int area) {
        int atti = AppTypeRecoManager.getInstance().getAppAttribute(pkgName);
        if (atti == -1) {
            return 0;
        }
        if (area == 0) {
            return (atti & 3840) >> 8;
        }
        if (area == 1) {
            return (61440 & atti) >> 12;
        }
        return 0;
    }

    public boolean isTopImAppBase(String pkg) {
        int actTop;
        if (!sEnabled || getAppMngSpecType(pkg) != 0 || (actTop = getAppTypeActTop(pkg, 1)) == 0) {
            return false;
        }
        if (actTop <= 3) {
            return true;
        }
        return false;
    }

    public boolean isBluetoothConnect(int pid) {
        if (!sEnabled) {
            return false;
        }
        synchronized (this.mBluetoothLock) {
            int len = this.mBtoothConnectList.size();
            if (len == 0) {
                return false;
            }
            if (this.mBtoothConnectList.get(len - 1).intValue() == pid) {
                return true;
            }
            return false;
        }
    }

    public boolean isBluetoothLast(int pid, long time) {
        if (!sEnabled) {
            return false;
        }
        synchronized (this.mBluetoothLock) {
            if (!this.mBtoothConnectList.isEmpty()) {
                return false;
            }
            if (this.mBtoothLastPid != -1) {
                if (this.mBtoothLastPid == pid) {
                    if (SystemClock.elapsedRealtime() - this.mBtoothLastTime > time) {
                        return false;
                    }
                    return true;
                }
            }
            return false;
        }
    }

    public ArraySet<String> getDozeProtectedApps() {
        ArraySet<String> result = new ArraySet<>();
        synchronized (this.mDozeProtectPkg) {
            result.addAll((ArraySet<? extends String>) this.mDozeProtectPkg);
        }
        return result;
    }

    public boolean isRecogOptEnable() {
        return AppAccurateRecgFeature.isEnable();
    }

    public boolean checkBleStatus() {
        if (!sEnabled) {
            return true;
        }
        return sBleStatus;
    }

    private long getElapsedAppUseTime(String pkgName) {
        if ((!sEnabled && !this.mAppStartEnabled) || pkgName == null) {
            return -1;
        }
        AwareUserHabit habit = AwareUserHabit.getInstance();
        if (habit == null) {
            AwareLog.e(TAG, "AwareUserHabit is null");
            return -1;
        }
        long switchFgTime = habit.getAppSwitchFgTime(pkgName);
        if (switchFgTime == -1) {
            return -1;
        }
        long now = System.currentTimeMillis();
        if (switchFgTime > now || switchFgTime <= -1) {
            return -1;
        }
        return (now - switchFgTime) / 1000;
    }

    private boolean isAppUnusedRecent(String pkgName, int recent) {
        if (isAppMngSpecRecent(pkgName, 1, recent)) {
            return false;
        }
        long elapsedAppUseTime = getElapsedAppUseTime(pkgName);
        return elapsedAppUseTime == -1 || elapsedAppUseTime > ((long) recent);
    }

    public int getAppUnusedRecent(String pkgName, LinkedHashMap<Integer, RuleNode> recents) {
        if (recents == null || pkgName == null) {
            return -1;
        }
        for (Integer recent : recents.keySet()) {
            if (recent != null && recent.intValue() > 0 && isAppUnusedRecent(pkgName, recent.intValue())) {
                return recent.intValue();
            }
        }
        return -1;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateBleState() {
        sBleStatus = fetchSwitchStatus("bluetooth_on");
    }

    public boolean fetchSwitchStatus(String name) {
        Context context;
        String switchStatusStr;
        MultiTaskManagerService multiTaskManagerService = this.mMtmService;
        if (multiTaskManagerService == null || (context = multiTaskManagerService.context()) == null || (switchStatusStr = SettingsEx.Secure.getStringForUser(context.getContentResolver(), name, this.mCurUserId)) == null || SWITCH_STATUS_STR.equals(switchStatusStr)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendBleStatusUpdate() {
        IntlRecgHandler intlRecgHandler = this.mHandler;
        if (intlRecgHandler != null) {
            Message msg = intlRecgHandler.obtainMessage();
            msg.what = 20;
            this.mHandler.removeMessages(20);
            this.mHandler.sendMessage(msg);
        }
    }

    public void dumpAppSceneInfo(PrintWriter pw, int type, String pkgName) {
        if (pw != null) {
            if (!sEnabled) {
                pw.println("AwareIntelligentRecg feature disabled.");
                return;
            }
            pw.println("AwareIntelligentRecg dumpAppSceneInfo start:");
            if (type == 1) {
                pw.println("pkgName: " + pkgName + ",getElapsedAppUseTime: " + getElapsedAppUseTime(pkgName));
            } else if (type == 2) {
                pw.println("china sim: " + isChinaOperrator());
            } else if (type == 3) {
                pw.println("ble swicth:" + checkBleStatus());
            }
        }
    }

    public boolean isChinaOperrator() {
        if (sEnabled && this.mIsChinaOperator && !this.mIsAbroadArea) {
            return true;
        }
        return false;
    }

    private void updateNetWorkOperatorInit(Context context) {
        Object obj = this.mMtmService.context().getSystemService("phone");
        if (obj != null && (obj instanceof TelephonyManager)) {
            TelephonyManager telephonyManager = (TelephonyManager) obj;
            String networkOperator = telephonyManager.getSimOperator();
            if (networkOperator != null && networkOperator.startsWith(CHINA_MCC)) {
                this.mIsChinaOperator = true;
            }
            telephonyManager.listen(this.mPhoneStateListener, 1);
        }
    }

    public void reportAllowAppStartClean(AwareProcessBlockInfo procGroup) {
        List<AwareProcessInfo> processList;
        if (procGroup != null && (processList = procGroup.procProcessList) != null) {
            boolean needToUpdate = false;
            int size = processList.size();
            int i = 0;
            while (true) {
                if (i >= size) {
                    break;
                }
                AwareProcessInfo awareProc = processList.get(i);
                if (awareProc != null && awareProc.procTaskId != -1) {
                    needToUpdate = true;
                    break;
                }
                i++;
            }
            if (needToUpdate) {
                updateAllowStartPkgs(UserHandleEx.getUserId(procGroup.procUid), procGroup.procPackageName, true);
            }
        }
    }

    public void reportAbnormalClean(AwareProcessBlockInfo procGroup) {
        if (procGroup != null) {
            updateAllowStartPkgs(UserHandleEx.getUserId(procGroup.procUid), procGroup.procPackageName, false);
        }
    }

    private void updateAllowStartPkgs(int userId, String pkg, boolean isAdd) {
        if (this.mAppStartEnabled && !this.mIsAppMngEnhance && pkg != null && !"".equals(pkg) && userId == 0) {
            long nowTime = SystemClock.elapsedRealtime() / 1000;
            synchronized (this.mAllowStartPkgs) {
                if (isAdd) {
                    this.mAllowStartPkgs.put(pkg, Long.valueOf(nowTime));
                } else {
                    removeAllowStartPkgIfNeedLock(pkg, nowTime);
                }
            }
        }
    }

    private void removeAllowStartPkgIfNeedLock(String pkg, long nowTime) {
        Long time = this.mAllowStartPkgs.get(pkg);
        if (time != null) {
            long delTime = nowTime - time.longValue();
            if (delTime > this.mAppMngAllowTime || delTime < 0) {
                this.mAllowStartPkgs.remove(pkg);
            }
        }
    }

    public boolean isAllowStartPkgs(String pkg) {
        boolean containsKey;
        if (!this.mAppStartEnabled || this.mIsAppMngEnhance || pkg == null || "".equals(pkg)) {
            return false;
        }
        synchronized (this.mAllowStartPkgs) {
            containsKey = this.mAllowStartPkgs.containsKey(pkg);
        }
        return containsKey;
    }

    public void dumpAllowStartPkgs(PrintWriter pw) {
        if (pw != null) {
            if (!this.mAppStartEnabled) {
                pw.println("AppStart feature disabled.");
                return;
            }
            pw.println("mIsAppMngEnhance:" + this.mIsAppMngEnhance);
            pw.println("mAppMngAllowTime:" + this.mAppMngAllowTime);
            pw.println("== DumpAllowStartPkgs Start ==");
            long nowTime = SystemClock.elapsedRealtime() / 1000;
            synchronized (this.mAllowStartPkgs) {
                for (Map.Entry<String, Long> entry : this.mAllowStartPkgs.entrySet()) {
                    if (entry != null) {
                        pw.println("  pkg : " + entry.getKey() + " time : " + (nowTime - entry.getValue().longValue()));
                    }
                }
            }
        }
    }

    public boolean isAppMngEnhance() {
        return this.mIsAppMngEnhance;
    }

    public boolean isAppLockClassName(String name) {
        if ((sEnabled || this.mAppStartEnabled) && name != null) {
            return name.equals(this.mAppLockClass);
        }
        return false;
    }

    private void initGmsControlInfo() {
        this.mIsGmsPhone = !TextUtils.isEmpty(SystemPropertiesEx.get("ro.com.google.gmsversion", (String) null));
        if (!this.mIsGmsPhone) {
            loadControlGmsApp();
            updateIsGmsCoreValid();
        }
    }

    private void loadControlGmsApp() {
        ArrayList<String> pkgList = DecisionMaker.getInstance().getRawConfig(AppMngConstant.AppMngFeature.APP_START.getDesc(), "controlgmsapp");
        if (pkgList != null) {
            synchronized (this.mControlGmsApp) {
                this.mControlGmsApp.addAll(pkgList);
            }
        }
    }

    public boolean isGmsControlApp(String pkg) {
        if (!isGmsControlEnable() || pkg == null || this.mCurUserId != 0) {
            return false;
        }
        synchronized (this.mControlGmsApp) {
            if (!this.mControlGmsApp.contains(pkg)) {
                return false;
            }
            return !this.mIsGmsCoreValid;
        }
    }

    public void dumpGmsControlInfo(PrintWriter pw) {
        if (pw != null) {
            if (!sEnabled) {
                pw.println("AwareIntelligentRecg feature disabled.");
                return;
            }
            pw.println("[Is Gms Phone]");
            pw.println(this.mIsGmsPhone);
            pw.println("[Is Gms Core Valid]");
            pw.println(this.mIsGmsCoreValid);
            pw.println("[dump Control GMS App]");
            synchronized (this.mControlGmsApp) {
                Iterator<String> it = this.mControlGmsApp.iterator();
                while (it.hasNext()) {
                    pw.println(it.next());
                }
            }
        }
    }

    private boolean isGmsControlEnable() {
        return sEnabled && !this.mIsGmsPhone;
    }

    private void updateIsGmsCoreValid() {
        MultiTaskManagerService multiTaskManagerService = this.mMtmService;
        if (multiTaskManagerService != null && multiTaskManagerService.context() != null) {
            this.mIsGmsCoreValid = ValidGPackageHelper.isValidForG(this.mMtmService.context());
        }
    }

    private void updateIsGmsCoreValid(String pkg) {
        MultiTaskManagerService multiTaskManagerService;
        if (!(!isGmsControlEnable() || pkg == null || (multiTaskManagerService = this.mMtmService) == null || multiTaskManagerService.context() == null || ValidGPackageHelper.GMS_CORE_PKGS == null)) {
            for (String gmsCorePkg : ValidGPackageHelper.GMS_CORE_PKGS) {
                if (pkg.equals(gmsCorePkg)) {
                    this.mIsGmsCoreValid = ValidGPackageHelper.isValidForG(this.mMtmService.context());
                    return;
                }
            }
        }
    }

    private void initHandler() {
        Looper looper = AwareServiceThread.getInstance().getLooper();
        if (looper != null) {
            this.mHandler = new IntlRecgHandler(looper);
        } else {
            this.mHandler = new IntlRecgHandler(BackgroundThreadEx.getLooper());
        }
    }

    private boolean isSystemUid(int uid) {
        return UserHandleEx.getAppId(uid) == 1000;
    }
}
