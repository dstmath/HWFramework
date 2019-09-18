package com.android.server.rms.iaware.appmng;

import android.annotation.SuppressLint;
import android.app.ActivityManagerNative;
import android.app.IWallpaperManager;
import android.app.WallpaperInfo;
import android.app.mtm.iaware.appmng.AppMngConstant;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.rms.HwSysResManager;
import android.rms.iaware.AppTypeRecoManager;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CmpTypeInfo;
import android.rms.iaware.ComponentRecoManager;
import android.rms.iaware.DeviceInfo;
import android.rms.iaware.IAwareCMSManager;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.DisplayMetrics;
import android.webkit.WebViewZygote;
import com.android.internal.os.BackgroundThread;
import com.android.internal.util.MemInfoReader;
import com.android.server.am.ProcessRecord;
import com.android.server.gesture.GestureNavConst;
import com.android.server.hidata.appqoe.HwAPPQoEUtils;
import com.android.server.location.HwLogRecordManager;
import com.android.server.mtm.MultiTaskManagerService;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.iaware.appmng.AwareProcessWindowInfo;
import com.android.server.mtm.iaware.appmng.DecisionMaker;
import com.android.server.mtm.iaware.appmng.appstart.comm.AppStartupUtil;
import com.android.server.mtm.iaware.appmng.appstart.datamgr.AppStartupDataMgr;
import com.android.server.mtm.iaware.appmng.rule.ListItem;
import com.android.server.mtm.iaware.appmng.rule.RuleNode;
import com.android.server.mtm.iaware.srms.AwareBroadcastDebug;
import com.android.server.mtm.iaware.srms.AwareBroadcastPolicy;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfoCollector;
import com.android.server.mtm.utils.AppStatusUtils;
import com.android.server.mtm.utils.InnerUtils;
import com.android.server.rms.algorithm.AwareUserHabit;
import com.android.server.rms.iaware.appmng.AppStartPolicyCfg;
import com.android.server.rms.iaware.appmng.AwareAppKeyBackgroup;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.sysload.SysLoadManager;
import com.android.server.security.securitydiagnose.HwSecDiagnoseConstant;
import com.android.systemui.shared.recents.hwutil.HwRecentsTaskUtils;
import com.huawei.android.view.HwWindowManager;
import com.huawei.pgmng.plug.PGSdk;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import vendor.huawei.hardware.hwdisplay.displayengine.V1_0.ConstS32;

public final class AwareIntelligentRecg {
    private static final int ACTIONINDEX = 0;
    private static final int ACTIONLENGTH = 1;
    private static final String ACT_TOP_IM_CN = "act_topim_cn";
    private static final int ALARMACTIONINDEX = 1;
    private static final int ALARMTAGLENGTH = 2;
    private static final int ALARM_PERCEPTION_TIME = 15000;
    private static final int ALARM_RECG_FACTOR = 1;
    private static final int ALARM_RECG_FACTOR_FORCLOCK = 5;
    private static final int ALLOW_BOOT_START_IM_APP_NUM = 3;
    private static final int ALLOW_BOOT_START_IM_MEM_THRESHOLD = 3072;
    private static final int ALLPROC = 1000;
    public static final int APP_ACTTOP1 = 1;
    public static final int APP_ACTTOP10 = 4;
    public static final int APP_ACTTOP100 = 7;
    public static final int APP_ACTTOP2 = 2;
    public static final int APP_ACTTOP20 = 5;
    public static final int APP_ACTTOP5 = 3;
    public static final int APP_ACTTOP50 = 6;
    public static final int APP_ACTTOP_DEFAULT = 0;
    private static final String BACKGROUND_CHECK_EXCLUDED_ACTION = "bgcheck_excluded_action";
    private static final String BLIND_PREFABRICATED = "blind_prefabricated";
    private static final int BLUETOOTH_LAST_USED_PARAMS = 2;
    private static final String BLUETOOTH_PKG = "com.android.bluetooth";
    private static final String BTMEDIABROWSER_ACTION = "android.media.browse.MediaBrowserService";
    private static final int CLSINDEX = 1;
    private static final int CMPLENGTH = 2;
    public static final String CMP_CLS = "cls";
    public static final String CMP_PKGNAME = "pkgName";
    public static final String CMP_TYPE = "cmpType";
    private static final int CONNECT_PG_DELAYED = 5000;
    /* access modifiers changed from: private */
    public static boolean DEBUG = false;
    private static final int DEFAULT_INVAILD_UID = -1;
    private static final int DEFAULT_MEMORY = -1;
    private static final long DEFAULT_REQUEST_TIMEOUT = 1800000;
    private static final int FREEZENOTCLEAN_ENTER = 1;
    private static final int FREEZENOTCLEAN_EXIT = 0;
    private static final int FREEZE_NOT_CLEAN = 10;
    private static final int FREEZE_PIDS = 100;
    private static final long GOOGLE_CONN_DELAY_TIME = 604800000;
    private static final int LOAD_CMPTYPE_MESSAGE_DELAY = 20000;
    private static final int MINCOUNT = 0;
    private static final int MSG_ADD_FG_PIDINFO = 22;
    private static final int MSG_ALARM_NOTIFICATION = 9;
    private static final int MSG_ALARM_SOUND = 10;
    private static final int MSG_ALARM_UNPERCEPTION = 11;
    private static final int MSG_ALARM_VIBRATOR = 8;
    private static final int MSG_CACHEDATA_FLUSHTODISK = 6;
    private static final int MSG_CHECK_CMPDATA = 16;
    private static final int MSG_CONNECT_WITH_PG_SDK = 5;
    private static final int MSG_DELETEDATA = 3;
    private static final int MSG_INIT_FG_PIDINFO = 21;
    private static final int MSG_INPUTMETHODSET = 15;
    private static final int MSG_INSERTDATA = 2;
    private static final int MSG_KBG_UPDATE = 7;
    private static final int MSG_LOADDATA = 1;
    private static final int MSG_RECG_PUSHSDK = 4;
    private static final int MSG_REMOVE_FG_PIDINFO = 23;
    private static final int MSG_UNFREEZE_NOTCLEAN = 17;
    private static final int MSG_UPDATEDB = 12;
    private static final int MSG_UPDOWN_CLEAR = 19;
    private static final int MSG_WAKENESS_CHANGE = 18;
    private static final int MSG_WALLPAPERINIT = 14;
    private static final int MSG_WALLPAPERSET = 13;
    private static final String NOTIFY_LISTENER_ACTION = "android.service.notification.NotificationListenerService";
    private static final int ONE_GIGA_BYTE = 1024;
    private static final int ONE_MINUTE = 60000;
    private static final int ONE_SECOND = 1000;
    public static final int PENDING_ALARM = 0;
    public static final int PENDING_PERC = 2;
    public static final int PENDING_UNPERC = 1;
    private static final int PKGINDEX = 0;
    private static final String PROPERTIES_GOOELE_CONNECTION = "persist.sys.iaware_google_conn";
    private static final String PUSHSDK_BAD = "push_bad";
    private static final String PUSHSDK_GOOD = "push_good";
    private static final int PUSHSDK_START_SAME_CMP_COUNT = 2;
    private static final int REPORT_APPUPDATE_MSG = 0;
    private static final String SEPARATOR_COMMA = ",";
    private static final String SEPARATOR_POUND = "#";
    private static final String SEPARATOR_SEMICOLON = ":";
    private static final int SPEC_VALUE_DEFAULT = -1;
    private static final int SPEC_VALUE_DEFAULT_MAX_TOPN = 999;
    private static final int SPEC_VALUE_TRUE = 1;
    private static final int STARTINFO_CACHE_TIME = 20000;
    private static final int STATUS_DEFAULT = 0;
    private static final int STATUS_PERCEPTION = 1;
    private static final int STATUS_UNPERCEPTION = 2;
    private static final String SYSTEM_PROCESS_NAME = "system";
    private static final String TAG = "RMS.AwareIntelligentRecg";
    private static final String TOP_IM_CN_PROP = "persist.sys.iaware.topimcn";
    private static final int TYPE_TOPN_EXTINCTION_TIME = 172800000;
    private static final int UNFREEZE_NOTCLEAN_INTERVAL = 60000;
    private static final String UNKNOWN_PKG = "unknownpkg";
    private static final int UNPERCEPTION_COUNT = 1;
    private static final int UPDATETIME = 10000000;
    private static final long UPDATE_DB_INTERVAL = 86400000;
    private static final int UPDATE_TIME_FOR_HABIT = 10000;
    private static final String VALUE_BT_BLE_CONNECT_APPS = "huawei_bt_ble_connect_apps";
    private static final String VALUE_BT_LAST_BLE_DISCONNECT = "huawei_bt_ble_last_disconnect";
    public static final long WIDGET_INVALID_ELAPSETIME = -1;
    private static AwareIntelligentRecg mAwareIntlgRecg = null;
    private static boolean mEnabled = false;
    private int APPMNGPROP_APPSTARTINDEX = 0;
    private int APPMNGPROP_MAXINDEX = (this.APPMNGPROP_APPSTARTINDEX + 1);
    private int GOOGLECONNPROP_MAXINDEX = (this.GOOGLECONNPROP_STARTINDEX + 2);
    private int GOOGLECONNPROP_STARTINDEX = 0;
    private final Set<String> mAccessPkg = new ArraySet();
    private String mActTopIMCN = UNKNOWN_PKG;
    private ArrayMap<String, CmpTypeInfo> mAlarmCmps = new ArrayMap<>();
    private ArrayMap<Integer, ArrayMap<String, AlarmInfo>> mAlarmMap = new ArrayMap<>();
    private Set<String> mAlarmPkgList = null;
    private ArrayMap<String, Long> mAppChangeToBGTime = new ArrayMap<>();
    private PGSdk.Sink mAppFreezeListener = new PGSdk.Sink() {
        public void onStateChanged(int stateType, int eventType, int pid, String pkg, int uid) {
            if (AwareIntelligentRecg.DEBUG) {
                AwareLog.d(AwareIntelligentRecg.TAG, "pkg:" + pkg + ",uid:" + uid + ",pid:" + pid + ",stateType: " + stateType + ",eventType:" + eventType);
            }
            if (stateType != 2) {
                if (stateType == 10) {
                    AwareIntelligentRecg.this.resolvePkgsName(pkg, eventType);
                } else if (stateType == 12) {
                    AwareIntelligentRecg.this.refreshAlivedApps(eventType, pkg, uid);
                } else if (stateType != 100) {
                    switch (stateType) {
                        case 6:
                            synchronized (AwareIntelligentRecg.this.mFrozenAppList) {
                                if (eventType == 1) {
                                    try {
                                        AwareIntelligentRecg.this.mFrozenAppList.add(Integer.valueOf(uid));
                                    } catch (Throwable th) {
                                        throw th;
                                    }
                                } else if (eventType == 2) {
                                    AwareIntelligentRecg.this.mFrozenAppList.remove(Integer.valueOf(uid));
                                }
                            }
                            return;
                        case 7:
                            synchronized (AwareIntelligentRecg.this.mFrozenAppList) {
                                AwareIntelligentRecg.this.mFrozenAppList.clear();
                            }
                            synchronized (AwareIntelligentRecg.this.mNotCleanPkgList) {
                                AwareIntelligentRecg.this.mNotCleanPkgList.clear();
                            }
                            SysLoadManager.getInstance().exitGameSceneMsg();
                            ProcessInfoCollector.getInstance().resetAwareProcessStatePgRestart();
                            return;
                        case 8:
                            synchronized (AwareIntelligentRecg.this.mBluetoothAppList) {
                                if (eventType == 1) {
                                    try {
                                        AwareIntelligentRecg.this.mBluetoothAppList.add(Integer.valueOf(uid));
                                    } catch (Throwable th2) {
                                        throw th2;
                                    }
                                } else if (eventType == 2) {
                                    AwareIntelligentRecg.this.mBluetoothAppList.remove(Integer.valueOf(uid));
                                }
                            }
                            return;
                        default:
                            return;
                    }
                } else {
                    AwareIntelligentRecg.this.resolvePids(pkg, uid, eventType);
                }
            } else if (eventType == 1) {
                AwareIntelligentRecg.this.sendPerceptionMessage(10, uid, null);
                synchronized (AwareIntelligentRecg.this.mAudioOutInstant) {
                    AwareIntelligentRecg.this.mAudioOutInstant.add(Integer.valueOf(uid));
                }
            } else if (eventType == 2) {
                synchronized (AwareIntelligentRecg.this.mAudioOutInstant) {
                    AwareIntelligentRecg.this.mAudioOutInstant.remove(Integer.valueOf(uid));
                }
            }
        }
    };
    private boolean mAppMngPropCfgInit = false;
    private int mAppStartAreaCfg = -1;
    private boolean mAppStartEnabled = false;
    private Set<AwareAppStartInfo> mAppStartInfoList = new ArraySet();
    /* access modifiers changed from: private */
    public Set<Integer> mAudioOutInstant = new ArraySet();
    private AwareStateCallback mAwareStateCallback = null;
    private ArraySet<String> mBGCheckExcludedAction = new ArraySet<>();
    private ArraySet<String> mBGCheckExcludedPkg = new ArraySet<>();
    private Set<String> mBlindPkg = new ArraySet();
    /* access modifiers changed from: private */
    public Set<Integer> mBluetoothAppList = new ArraySet();
    private final Object mBluetoothLock = new Object();
    private int mBluetoothUid;
    private ArrayList<Integer> mBtoothConnectList = new ArrayList<>();
    private int mBtoothLastPid = -1;
    private long mBtoothLastTime = 0;
    private volatile ArrayMap<Integer, Set<String>> mCachedTypeTopN = new ArrayMap<>();
    private final ArraySet<IAwareToastCallback> mCallbacks = new ArraySet<>();
    private Set<Integer> mCameraUseAppList = new ArraySet();
    private int mCurUserId = 0;
    private int mDBUpdateCount = 0;
    private final AtomicInteger mDataLoadCount = new AtomicInteger(2);
    /* access modifiers changed from: private */
    public AppStartupDataMgr mDataMgr = null;
    /* access modifiers changed from: private */
    public String mDefaultInputMethod = "";
    private int mDefaultInputMethodUid = -1;
    /* access modifiers changed from: private */
    public String mDefaultSms = "";
    private String mDefaultTTS = "";
    private String mDefaultWallPaper = "";
    private int mDefaultWallPaperUid = -1;
    public int mDeviceLevel = -1;
    private int mDeviceMemoryOfGIGA = -1;
    private long mDeviceTotalMemory = -1;
    private final ArraySet<IAwareFgChangedCallback> mFgCallbacks = new ArraySet<>();
    private ArrayMap<Integer, PidInfo> mFgPidInfos = new ArrayMap<>();
    /* access modifiers changed from: private */
    public Set<Integer> mFrozenAppList = new ArraySet();
    private ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> mGMSAppCleanPolicyList = null;
    private ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> mGMSAppStartPolicyList = null;
    private ArraySet<String> mGmsAppPkg = new ArraySet<>();
    private ArraySet<String> mGmsCallerAppPkg = new ArraySet<>();
    private ArraySet<Integer> mGmsCallerAppUid = new ArraySet<>();
    private Set<String> mGoodPushSDKClsList = new ArraySet();
    private long mGoogleConnDealyTime = 604800000;
    private boolean mGoogleConnStat = false;
    private boolean mGoogleConnStatDecay = false;
    private long mGoogleDisConnTime = 0;
    private List<String> mHabbitTopN = null;
    /* access modifiers changed from: private */
    public IntlRecgHandler mHandler = null;
    private final Set<String> mHwStopUserIdPkg = new ArraySet();
    private AwareBroadcastPolicy mIawareBrPolicy = null;
    private final AtomicInteger mInitWallPaperCount = new AtomicInteger(2);
    private boolean mIsAbroadArea = true;
    private boolean mIsInitGoogleConfig = false;
    private AtomicBoolean mIsInitialized = new AtomicBoolean(false);
    private AtomicBoolean mIsObsvInit = new AtomicBoolean(false);
    private AtomicBoolean mIsScreenOn = new AtomicBoolean(true);
    private boolean mIsScreenOnPm = true;
    private AtomicLong mLastPerceptionTime = new AtomicLong(0);
    private volatile ArrayMap<Integer, Long> mLastUpdateTime = new ArrayMap<>();
    private MultiTaskManagerService mMtmService = null;
    private int mNotCleanDuration = 60000;
    /* access modifiers changed from: private */
    public Set<String> mNotCleanPkgList = new ArraySet();
    private PGSdk mPGSdk = null;
    private PackageManager mPackageManager = null;
    private Set<String> mPayPkgList = null;
    private Set<String> mPushSDKClsList = new ArraySet();
    private Set<String> mRecent = null;
    private ArrayMap<Integer, ArrayMap<String, Long>> mRegKeepAlivePkgs = new ArrayMap<>();
    private LinkedList<Long> mScreenChangedTime = new LinkedList<>();
    private Set<Integer> mScreenRecordAppList = new ArraySet();
    private Set<Integer> mScreenRecordPidList = new ArraySet();
    private ContentObserver mSettingsObserver = null;
    private Set<String> mSharePkgList = null;
    private List<String> mSmallSampleList = new ArrayList();
    private final Set<Integer> mStatusAudioIn = new ArraySet();
    private final Set<Integer> mStatusAudioOut = new ArraySet();
    private final Set<Integer> mStatusGps = new ArraySet();
    private final Set<Integer> mStatusSensor = new ArraySet();
    private final Set<Integer> mStatusUpDown = new ArraySet();
    private final Map<Integer, Long> mStatusUpDownElapse = new ArrayMap();
    private final Set<String> mTTSPkg = new ArraySet();
    private int mTempHTopN = 0;
    private int mTempRecent = 0;
    private ArrayMap<Integer, AwareProcessWindowInfo> mToasts = new ArrayMap<>();
    private Set<Integer> mUnRemindAppTypeList = null;
    private long mUpdateTimeHabbitTopN = -1;
    private long mUpdateTimeRecent = -1;
    private int mWebViewUid;
    private int mWidgetCheckUpdateCnt = 5;
    private long mWidgetCheckUpdateInterval = 86400000;

    static class AlarmInfo {
        public boolean bfgset = false;
        public boolean bhandled = false;
        public int count = 0;
        public String packagename;
        public int perception_count = 0;
        public int reason;
        public long startTime = 0;
        public int status = 0;
        public String tag;
        public int uid;
        public int unperception_count = 0;

        public AlarmInfo(int uid2, String packagename2, String tag2) {
            this.uid = uid2;
            this.packagename = packagename2;
            this.tag = tag2;
            this.bhandled = false;
        }

        public AlarmInfo copy() {
            AlarmInfo dst = new AlarmInfo(this.uid, this.packagename, this.tag);
            dst.uid = this.uid;
            dst.packagename = this.packagename;
            dst.tag = this.tag;
            dst.startTime = this.startTime;
            dst.status = this.status;
            dst.reason = this.reason;
            dst.count = this.count;
            dst.perception_count = this.perception_count;
            dst.unperception_count = this.unperception_count;
            dst.bhandled = this.bhandled;
            dst.bfgset = this.bfgset;
            return dst;
        }

        public String toString() {
            return "package:" + this.packagename + ',' + "uid:" + this.uid + ',' + "tag:" + this.tag + ',' + "startTime:" + this.startTime + ',' + "status:" + this.status + ',' + "reason:" + this.reason + ',' + "count:" + this.count + ',' + "unperception_count:" + this.unperception_count + ',' + "perception_count:" + this.perception_count + ',' + "bfgset:" + this.bfgset + '.';
        }
    }

    static class AwareAppStartInfo {
        public int callerUid;
        public String cls = null;
        public String cmp;
        public long timestamp;

        public AwareAppStartInfo(int callerUid2, String cmp2) {
            this.callerUid = callerUid2;
            this.cmp = cmp2;
            parseCmp(cmp2);
            this.timestamp = SystemClock.elapsedRealtime();
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
            boolean z = false;
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
            if (this.cmp == null || info.cmp == null) {
                return false;
            }
            if (this.callerUid == info.callerUid && this.cmp.equals(info.cmp)) {
                z = true;
            }
            return z;
        }

        public String toString() {
            return "{caller:" + this.callerUid + "cmp:" + this.cmp + " time:" + this.timestamp + "}";
        }
    }

    private class AwareStateCallback implements AwareAppKeyBackgroup.IAwareStateCallback {
        private AwareStateCallback() {
        }

        public void onStateChanged(int stateType, int eventType, int pid, int uid) {
            Message msg = AwareIntelligentRecg.this.mHandler.obtainMessage();
            msg.what = 7;
            msg.arg1 = stateType;
            msg.arg2 = eventType;
            msg.obj = Integer.valueOf(uid);
            AwareIntelligentRecg.this.mHandler.sendMessage(msg);
        }
    }

    public interface IAwareFgChangedCallback {
        void onForegroundActivitiesChanged(ArrayList<PidInfo> arrayList, boolean z);
    }

    public interface IAwareToastCallback {
        void onToastWindowsChanged(int i, int i2);
    }

    private class IntlRecgHandler extends Handler {
        public IntlRecgHandler(Looper looper) {
            super(looper);
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v0, resolved type: java.lang.String} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v1, resolved type: java.lang.String} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v4, resolved type: java.lang.String} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v4, resolved type: com.android.server.rms.iaware.appmng.AwareIntelligentRecg$AlarmInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v8, resolved type: java.lang.String} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v9, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v10, resolved type: java.lang.String} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v15, resolved type: java.lang.String} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v16, resolved type: java.lang.String} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v17, resolved type: java.lang.String} */
        /* JADX WARNING: type inference failed for: r1v7, types: [com.android.server.rms.iaware.appmng.AwareIntelligentRecg$AlarmInfo] */
        /* JADX WARNING: Multi-variable type inference failed */
        private void handleMessageBecauseOfNSIQ(Message msg) {
            String pkg = null;
            switch (msg.what) {
                case 8:
                case 9:
                case 10:
                    int uid = msg.arg1;
                    if (msg.obj instanceof String) {
                        pkg = (String) msg.obj;
                    }
                    AwareIntelligentRecg.this.handlePerceptionEvent(uid, pkg, msg.what);
                    return;
                case 11:
                    if (msg.obj instanceof AlarmInfo) {
                        pkg = (AlarmInfo) msg.obj;
                    }
                    AwareIntelligentRecg.this.handleUnPerceptionEvent(pkg, msg.what);
                    return;
                case 13:
                    if (msg.obj instanceof String) {
                        pkg = msg.obj;
                    }
                    AwareIntelligentRecg.this.handleWallpaperSetMessage(pkg);
                    return;
                case 14:
                    AwareIntelligentRecg.this.initDefaultWallPaper();
                    return;
                case 15:
                    AwareIntelligentRecg.this.handleInputMethodSetMessage();
                    return;
                case 16:
                    AwareIntelligentRecg.this.checkAlarmCmpDataForUnistalled();
                    return;
                case 17:
                    synchronized (AwareIntelligentRecg.this.mNotCleanPkgList) {
                        AwareIntelligentRecg.this.mNotCleanPkgList.clear();
                    }
                    return;
                case 18:
                    AwareIntelligentRecg.this.handleWakenessChangeHandle();
                    return;
                case 19:
                    AwareIntelligentRecg.this.handleWidgetUpDownLoadClear();
                    return;
                default:
                    handleMessageEx(msg);
                    return;
            }
        }

        private void handleMessageEx(Message msg) {
            switch (msg.what) {
                case 21:
                    if (msg.obj instanceof ArrayMap) {
                        AwareIntelligentRecg.this.initFgPidInfo((ArrayMap) msg.obj);
                        return;
                    }
                    return;
                case 22:
                    AwareIntelligentRecg.this.updateFgPidInfo(msg.arg1, msg.arg2, true);
                    return;
                case 23:
                    AwareIntelligentRecg.this.updateFgPidInfo(msg.arg1, msg.arg2, false);
                    return;
                default:
                    return;
            }
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v8, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v4, resolved type: android.rms.iaware.CmpTypeInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v12, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v8, resolved type: android.rms.iaware.CmpTypeInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v16, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v15, resolved type: android.rms.iaware.CmpTypeInfo} */
        /* JADX WARNING: Multi-variable type inference failed */
        public void handleMessage(Message msg) {
            if (AwareIntelligentRecg.DEBUG) {
                AwareLog.i(AwareIntelligentRecg.TAG, "handleMessage message " + msg.what);
            }
            int i = msg.what;
            if (i != 12) {
                CmpTypeInfo cmpinfo = null;
                switch (i) {
                    case 0:
                        AwareIntelligentRecg.this.handleReportAppUpdateMsg(msg);
                        return;
                    case 1:
                        AwareIntelligentRecg.this.initRecgResultInfo();
                        return;
                    case 2:
                        if (msg.obj instanceof CmpTypeInfo) {
                            cmpinfo = msg.obj;
                        }
                        boolean unused = AwareIntelligentRecg.this.insertCmpRecgInfo(cmpinfo);
                        return;
                    case 3:
                        if (msg.obj instanceof CmpTypeInfo) {
                            cmpinfo = msg.obj;
                        }
                        boolean unused2 = AwareIntelligentRecg.this.deleteCmpRecgInfo(cmpinfo);
                        return;
                    case 4:
                        if (msg.obj instanceof AwareAppStartInfo) {
                            cmpinfo = (AwareAppStartInfo) msg.obj;
                        }
                        CmpTypeInfo cmpTypeInfo = cmpinfo;
                        if (AwareIntelligentRecg.this.needRecgPushSDK(cmpTypeInfo)) {
                            AwareIntelligentRecg.this.recordStartInfo(cmpTypeInfo);
                            AwareIntelligentRecg.this.adjustPushSDKCmp(cmpTypeInfo);
                            return;
                        }
                        return;
                    case 5:
                        AwareIntelligentRecg.this.initPGSDK();
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
                        handleMessageBecauseOfNSIQ(msg);
                        return;
                }
            } else {
                AwareIntelligentRecg.this.handleUpdateDB();
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

    private class SettingsObserver extends ContentObserver {
        public SettingsObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange, Uri uri) {
            if (Settings.Secure.getUriFor("default_input_method").equals(uri)) {
                String unused = AwareIntelligentRecg.this.mDefaultInputMethod = AwareIntelligentRecg.this.readDefaultInputMethod();
                AwareIntelligentRecg.this.sendInputMethodSetMessage();
            } else if (Settings.Secure.getUriFor("enabled_accessibility_services").equals(uri)) {
                AwareIntelligentRecg.this.updateAccessbilityService();
            } else if (Settings.Secure.getUriFor("tts_default_synth").equals(uri)) {
                AwareIntelligentRecg.this.updateDefaultTTS();
            } else if (Settings.Secure.getUriFor(AwareIntelligentRecg.VALUE_BT_BLE_CONNECT_APPS).equals(uri)) {
                AwareIntelligentRecg.this.updateBluetoothConnect();
            } else if (Settings.Secure.getUriFor(AwareIntelligentRecg.VALUE_BT_LAST_BLE_DISCONNECT).equals(uri)) {
                AwareIntelligentRecg.this.updateBluetoothLastDisconnect();
            } else if (Settings.Secure.getUriFor("sms_default_application").equals(uri)) {
                String unused2 = AwareIntelligentRecg.this.mDefaultSms = AwareIntelligentRecg.this.readDefaultSmsPackage();
            }
        }
    }

    private AwareIntelligentRecg() {
    }

    public static synchronized AwareIntelligentRecg getInstance() {
        AwareIntelligentRecg awareIntelligentRecg;
        synchronized (AwareIntelligentRecg.class) {
            if (mAwareIntlgRecg == null) {
                mAwareIntlgRecg = new AwareIntelligentRecg();
            }
            awareIntelligentRecg = mAwareIntlgRecg;
        }
        return awareIntelligentRecg;
    }

    /* access modifiers changed from: private */
    public void initPGSDK() {
        if (this.mPGSdk == null) {
            this.mPGSdk = PGSdk.getInstance();
            if (this.mPGSdk == null) {
                delayConnectPGSDK();
                return;
            }
            try {
                this.mPGSdk.enableStateEvent(this.mAppFreezeListener, 6);
                this.mPGSdk.enableStateEvent(this.mAppFreezeListener, 7);
                this.mPGSdk.enableStateEvent(this.mAppFreezeListener, 8);
                this.mPGSdk.enableStateEvent(this.mAppFreezeListener, 2);
                this.mPGSdk.enableStateEvent(this.mAppFreezeListener, 10);
                this.mPGSdk.enableStateEvent(this.mAppFreezeListener, 100);
                this.mPGSdk.enableStateEvent(this.mAppFreezeListener, 12);
            } catch (RemoteException e) {
                AwareLog.e(TAG, "registerPGListener happend RemoteException!");
                this.mPGSdk = null;
                delayConnectPGSDK();
            }
        }
    }

    private void delayConnectPGSDK() {
        this.mHandler.removeMessages(5);
        this.mHandler.sendEmptyMessageDelayed(5, 5000);
    }

    private void unRegisterPGListener() {
        if (this.mPGSdk != null) {
            try {
                this.mPGSdk.disableStateEvent(this.mAppFreezeListener, 6);
                this.mPGSdk.disableStateEvent(this.mAppFreezeListener, 7);
                this.mPGSdk.disableStateEvent(this.mAppFreezeListener, 8);
                this.mPGSdk.disableStateEvent(this.mAppFreezeListener, 2);
                this.mPGSdk.disableStateEvent(this.mAppFreezeListener, 10);
                this.mPGSdk.disableStateEvent(this.mAppFreezeListener, 100);
                this.mPGSdk.disableStateEvent(this.mAppFreezeListener, 12);
            } catch (RemoteException e) {
                AwareLog.e(TAG, "unRegisterPGListener  happend RemoteException!");
            }
        }
    }

    /* access modifiers changed from: private */
    public void resolvePids(String pkgs, int uid, int eventType) {
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
            String[] pkglist = pkgs.split(SEPARATOR_SEMICOLON);
            int i = 0;
            int count = pkglist.length;
            while (i < count) {
                try {
                    ProcessInfoCollector.getInstance().setAwareProcessState(Integer.parseInt(pkglist[i]), uid, state);
                    i++;
                } catch (NumberFormatException e) {
                    AwareLog.e(TAG, "pid param error");
                    return;
                }
            }
        }
    }

    public void addScreenRecord(int uid, int pid) {
        synchronized (this.mScreenRecordAppList) {
            this.mScreenRecordAppList.add(Integer.valueOf(uid));
        }
        synchronized (this.mScreenRecordPidList) {
            this.mScreenRecordPidList.add(Integer.valueOf(pid));
        }
    }

    public void removeScreenRecord(int uid, int pid) {
        synchronized (this.mScreenRecordAppList) {
            this.mScreenRecordAppList.remove(Integer.valueOf(uid));
        }
        synchronized (this.mScreenRecordPidList) {
            this.mScreenRecordPidList.remove(Integer.valueOf(pid));
        }
    }

    public boolean isScreenRecord(AwareProcessInfo awareProcInfo) {
        boolean contains;
        if (!mEnabled || awareProcInfo == null || awareProcInfo.mProcInfo == null) {
            return false;
        }
        synchronized (this.mScreenRecordAppList) {
            contains = this.mScreenRecordAppList.contains(Integer.valueOf(awareProcInfo.mProcInfo.mAppUid));
        }
        return contains;
    }

    public void removeDiedScreenProc(int uid, int pid) {
        synchronized (this.mScreenRecordPidList) {
            if (this.mScreenRecordPidList.contains(Integer.valueOf(pid))) {
                synchronized (this.mScreenRecordAppList) {
                    this.mScreenRecordAppList.remove(Integer.valueOf(uid));
                }
            }
        }
    }

    public void addCamera(int uid) {
        synchronized (this.mCameraUseAppList) {
            this.mCameraUseAppList.add(Integer.valueOf(uid));
        }
    }

    public void removeCamera(int uid) {
        synchronized (this.mCameraUseAppList) {
            this.mCameraUseAppList.remove(Integer.valueOf(uid));
        }
    }

    public boolean isCameraRecord(AwareProcessInfo awareProcInfo) {
        boolean contains;
        if (!mEnabled || awareProcInfo == null || awareProcInfo.mProcInfo == null) {
            return false;
        }
        int uid = awareProcInfo.mProcInfo.mAppUid;
        if (AwareAppAssociate.getInstance().isForeGroundApp(uid) || !AwareAppAssociate.getInstance().hasWindow(uid)) {
            return false;
        }
        synchronized (this.mCameraUseAppList) {
            contains = this.mCameraUseAppList.contains(Integer.valueOf(uid));
        }
        return contains;
    }

    /* access modifiers changed from: private */
    public void handleReportAppUpdateMsg(Message msg) {
        int eventId = msg.arg1;
        Bundle args = msg.getData();
        switch (eventId) {
            case 1:
                String pkgName = args.getString(AwareUserHabit.USERHABIT_PACKAGE_NAME);
                if (pkgName != null) {
                    initializeForInstallApp(pkgName);
                    return;
                }
                return;
            case 2:
                String pkgName2 = args.getString(AwareUserHabit.USERHABIT_PACKAGE_NAME);
                int uid = args.getInt("uid");
                int userId = UserHandle.getUserId(uid);
                if (pkgName2 != null && userId == this.mCurUserId) {
                    initializeForUninstallApp(pkgName2);
                    clearAlarmForUninstallApp(pkgName2, uid);
                    setHwStopFlag(userId, pkgName2, false);
                    return;
                } else if (this.mCurUserId == 0 && isCloneUserId(userId)) {
                    setHwStopFlag(userId, pkgName2, false);
                    return;
                } else {
                    return;
                }
            default:
                return;
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
                this.mHandler = new IntlRecgHandler(BackgroundThread.get().getLooper());
            }
            if (this.mSettingsObserver == null) {
                this.mSettingsObserver = new SettingsObserver(this.mHandler);
            }
            try {
                UserInfo currentUser = ActivityManagerNative.getDefault().getCurrentUser();
                if (currentUser != null) {
                    this.mCurUserId = currentUser.id;
                }
            } catch (RemoteException e) {
                AwareLog.e(TAG, "getCurrentUserId RemoteException");
            }
            if (this.mMtmService != null) {
                unregisterContentObserver(this.mMtmService.context(), this.mSettingsObserver);
                registerContentObserver(this.mMtmService.context(), this.mSettingsObserver);
            }
            this.mIsAbroadArea = AwareDefaultConfigList.isAbroadArea();
            this.mDeviceLevel = DeviceInfo.getDeviceLevel();
            initializeForUser();
            initPushSDK();
            sendLoadAppTypeMessage();
            initToastWindows();
            initPGSDK();
            initPayPkg();
            initSharePkg();
            initAlarmPkg();
            initUnRemindAppType();
            initAppStatus();
            initMinWindowSize();
            if (this.mMtmService != null) {
                updateGmsCallerAppInit(this.mMtmService.context());
            }
            initCommonActTopIM();
            AppStartupUtil.initCtsPkgList();
            initAppMngCustPropConfig();
            sendScreenOnFromPmMsg(true);
            sendMsgWidgetUpDownLoadClear();
            this.mIsInitialized.set(true);
            this.mBluetoothUid = getUIDByPackageName("com.android.bluetooth");
            this.mWebViewUid = getUIDByPackageName(WebViewZygote.getPackageName());
            initMemoryInfo();
        }
    }

    public void updateCloudData() {
        initPayPkg();
        initSharePkg();
        loadPushSDK();
        initBlindPkg();
        initAlarmPkg();
        initUnRemindAppType();
        updateAccessbilityService();
        if (this.mMtmService != null) {
            updateGmsCallerAppInit(this.mMtmService.context());
        }
        AwareAppMngSort.getInstance().updateCloudData();
        initGmsAppConfig();
        initCommonActTopIM();
    }

    private void loadPushSDK() {
        initPushSDK();
        loadPushSDKResultFromDB();
    }

    private List<CmpTypeInfo> readCmpTypeInfoFromDB() {
        try {
            IBinder awareservice = IAwareCMSManager.getICMSManager();
            if (awareservice != null) {
                return IAwareCMSManager.getCmpTypeList(awareservice);
            }
            AwareLog.e(TAG, "can not find service IAwareCMSService.");
            return null;
        } catch (RemoteException e) {
            AwareLog.e(TAG, "loadRecgResultInfo RemoteException");
            return null;
        }
    }

    private void updatePushSDKClsFromDB(String cls) {
        if (isInGoodPushSDKList(cls)) {
            sendUpdateCmpInfoMessage("", cls, 0, false);
            return;
        }
        synchronized (this.mPushSDKClsList) {
            this.mPushSDKClsList.add(cls);
        }
    }

    private void loadPushSDKResultFromDB() {
        List<CmpTypeInfo> list = readCmpTypeInfoFromDB();
        if (list != null) {
            for (CmpTypeInfo info : list) {
                String cls = info.getCls();
                if (info.getType() == 0) {
                    updatePushSDKClsFromDB(cls);
                }
            }
        }
    }

    private boolean isInGoodPushSDKList(String cls) {
        if (cls == null) {
            return false;
        }
        synchronized (this.mGoodPushSDKClsList) {
            if (this.mGoodPushSDKClsList.contains(cls)) {
                return true;
            }
            return false;
        }
    }

    private void initMinWindowSize() {
        if (this.mMtmService == null) {
            AwareLog.i(TAG, "mMtmService is null for min window size");
            return;
        }
        Context context = this.mMtmService.context();
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
        initTTSPkg();
        initSettings();
    }

    private void deInitialize() {
        if (this.mIsInitialized.get()) {
            synchronized (this.mAccessPkg) {
                this.mAccessPkg.clear();
            }
            synchronized (this.mTTSPkg) {
                this.mTTSPkg.clear();
            }
            synchronized (this.mGoodPushSDKClsList) {
                this.mGoodPushSDKClsList.clear();
            }
            synchronized (this.mPushSDKClsList) {
                this.mPushSDKClsList.clear();
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
            unRegisterPGListener();
            this.mPGSdk = null;
            synchronized (this.mRegKeepAlivePkgs) {
                this.mRegKeepAlivePkgs.clear();
            }
            synchronized (this.mAlarmMap) {
                this.mAlarmMap.clear();
            }
            synchronized (this.mAlarmCmps) {
                this.mAlarmCmps.clear();
            }
            synchronized (this.mSmallSampleList) {
                this.mSmallSampleList.clear();
            }
            synchronized (this.mScreenChangedTime) {
                this.mScreenChangedTime.clear();
            }
            synchronized (this.mAppChangeToBGTime) {
                this.mAppChangeToBGTime.clear();
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
            if (this.mMtmService != null) {
                unregisterContentObserver(this.mMtmService.context(), this.mSettingsObserver);
            }
            this.mDefaultInputMethod = "";
            this.mDefaultWallPaper = "";
            this.mDefaultTTS = "";
            this.mDefaultSms = "";
            deInitAppStatus();
            this.mIsInitialized.set(false);
        }
    }

    public void initUserSwitch(int userId) {
        if (mEnabled) {
            this.mCurUserId = userId;
            initializeForUser();
            sendCheckCmpDataMessage();
        }
    }

    private void initializeForInstallApp(String pkg) {
        initSpecTTSPkg(pkg);
        updateGmsCallerAppForInstall(pkg);
    }

    private void initializeForUninstallApp(String pkg) {
        removeSpecTTSPkg(pkg);
    }

    private void clearAlarmForUninstallApp(String pkgName, int uid) {
        deleteAppCmpRecgInfo(UserHandle.getUserId(uid), pkgName);
        removeAlarmCmp(uid, pkgName);
        removeAlarms(uid);
    }

    private void clearAlarmForRemoveUser(int userid) {
        deleteAppCmpRecgInfo(userid, null);
        removeAlarmCmpByUserid(userid);
        removeAlarmsByUserid(userid);
    }

    private void sendLoadAppTypeMessage() {
        Message msg = this.mHandler.obtainMessage();
        msg.what = 1;
        this.mHandler.sendMessageDelayed(msg, HwRecentsTaskUtils.MAX_REMOVE_TASK_TIME);
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
        if (DEBUG) {
            AwareLog.d(TAG, "PushSDK callerUid: " + callerUid + " cmp:" + compName + " targetUid: " + targetUid);
        }
        AwareAppStartInfo appStartInfo = new AwareAppStartInfo(callerUid, compName);
        Message msg = this.mHandler.obtainMessage();
        msg.obj = appStartInfo;
        msg.what = 4;
        this.mHandler.sendMessage(msg);
    }

    /* access modifiers changed from: private */
    public void sendPerceptionMessage(int msgid, int uid, String pkg) {
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
        this.mHandler.sendMessageDelayed(msg, HwRecentsTaskUtils.MAX_REMOVE_TASK_TIME);
    }

    private void initPushSDK() {
        ArrayList<String> badList = DecisionMaker.getInstance().getRawConfig(AppMngConstant.AppMngFeature.APP_START.getDesc(), PUSHSDK_BAD);
        if (badList != null) {
            synchronized (this.mPushSDKClsList) {
                this.mPushSDKClsList.clear();
                this.mPushSDKClsList.addAll(badList);
            }
        }
        ArrayList<String> goodList = DecisionMaker.getInstance().getRawConfig(AppMngConstant.AppMngFeature.APP_START.getDesc(), PUSHSDK_GOOD);
        if (goodList != null) {
            synchronized (this.mGoodPushSDKClsList) {
                this.mGoodPushSDKClsList.clear();
                this.mGoodPushSDKClsList.addAll(goodList);
            }
        }
    }

    private void initPayPkg() {
        ArrayList<String> pkgList = DecisionMaker.getInstance().getRawConfig(AppMngConstant.AppMngFeature.APP_START.getDesc(), "payPkgList");
        if (pkgList != null) {
            Set<String> pkgSet = new ArraySet<>();
            pkgSet.addAll(pkgList);
            this.mPayPkgList = pkgSet;
        }
    }

    private void initSharePkg() {
        ArrayList<String> pkgList = DecisionMaker.getInstance().getRawConfig(AppMngConstant.AppMngFeature.APP_START.getDesc(), "sharePkgList");
        if (pkgList != null) {
            Set<String> pkgSet = new ArraySet<>();
            pkgSet.addAll(pkgList);
            this.mSharePkgList = pkgSet;
        }
    }

    private void initAlarmPkg() {
        ArrayList<String> alarmList = DecisionMaker.getInstance().getRawConfig(AppMngConstant.AppMngFeature.APP_START.getDesc(), "alarmPkgList");
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
        String[] strs = str.split(",");
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
        if (this.mDeviceLevel == 3 || this.mDeviceLevel == 2) {
            unRemindTag = "unRemindAppTypesLowLevel";
        } else {
            unRemindTag = "unRemindAppTypes";
        }
        ArrayList<String> appTypeList = DecisionMaker.getInstance().getRawConfig(AppMngConstant.AppMngFeature.APP_START.getDesc(), unRemindTag);
        AwareLog.i(TAG, "initUnRemindAppType " + appTypeList);
        if (appTypeList != null) {
            Set<Integer> unRemindAppSet = new ArraySet<>();
            int size = appTypeList.size();
            for (int i = 0; i < size; i++) {
                Integer[] types = stringToIntArray(appTypeList.get(i));
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
    public void initRecgResultInfo() {
        if (loadRecgResultInfo()) {
            AwareLog.i(TAG, "AwareIntelligentRecg load recg pkg OK ");
        } else if (this.mDataLoadCount.get() >= 0) {
            AwareLog.i(TAG, "AwareIntelligentRecg send load recg pkg message again mDataLoadCount=" + (this.mDataLoadCount.get() - 1));
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
    public void updateKbgStatus(Message msg) {
        Set<Integer> status;
        int stateType = msg.arg1;
        int eventType = msg.arg2;
        int uid = ((Integer) msg.obj).intValue();
        switch (stateType) {
            case 1:
                status = this.mStatusAudioIn;
                break;
            case 2:
                status = this.mStatusAudioOut;
                break;
            case 3:
                status = this.mStatusGps;
                break;
            case 4:
                status = this.mStatusSensor;
                break;
            case 5:
                status = this.mStatusUpDown;
                flushUpDownLoadTrigTime(uid);
                break;
            default:
                return;
        }
        if (1 == eventType) {
            synchronized (status) {
                if (!status.contains(Integer.valueOf(uid))) {
                    status.add(Integer.valueOf(uid));
                }
            }
        } else if (2 == eventType) {
            synchronized (status) {
                if (status.contains(Integer.valueOf(uid))) {
                    status.remove(Integer.valueOf(uid));
                }
            }
        }
    }

    private boolean isAudioInStatus(int uid) {
        synchronized (this.mStatusAudioIn) {
            if (this.mStatusAudioIn.contains(Integer.valueOf(uid))) {
                return true;
            }
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001d, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001f, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x001b, code lost:
        if (com.android.server.rms.iaware.appmng.AwareAppKeyBackgroup.getInstance().isAudioCache(r4) == false) goto L_0x001e;
     */
    private boolean isAudioOutStatus(int uid) {
        synchronized (this.mStatusAudioOut) {
            if (this.mStatusAudioOut.contains(Integer.valueOf(uid))) {
                return true;
            }
        }
    }

    private boolean isGpsStatus(int uid) {
        synchronized (this.mStatusGps) {
            if (this.mStatusGps.contains(Integer.valueOf(uid))) {
                return true;
            }
            return false;
        }
    }

    private boolean isUpDownStatus(int uid) {
        synchronized (this.mStatusUpDown) {
            if (this.mStatusUpDown.contains(Integer.valueOf(uid))) {
                return true;
            }
            return false;
        }
    }

    private boolean isSensorStatus(int uid) {
        synchronized (this.mStatusSensor) {
            if (this.mStatusSensor.contains(Integer.valueOf(uid))) {
                return true;
            }
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:27:0x000c, code lost:
        continue;
     */
    private boolean loadRecgResultInfo() {
        List<CmpTypeInfo> list = readCmpTypeInfoFromDB();
        if (list == null) {
            return false;
        }
        for (CmpTypeInfo info : list) {
            if (DEBUG) {
                AwareLog.d(TAG, "loadRecgResultInfo info " + info.toString());
            }
            int type = info.getType();
            if (type != 0) {
                switch (type) {
                    case 3:
                    case 4:
                        synchronized (this.mAlarmCmps) {
                            ArrayMap<String, CmpTypeInfo> arrayMap = this.mAlarmCmps;
                            arrayMap.put(info.getUserId() + "#" + info.getPkgName() + "#" + info.getCls(), info);
                        }
                        break;
                }
            } else {
                updatePushSDKClsFromDB(info.getCls());
            }
        }
        return true;
    }

    private void sendCheckCmpDataMessage() {
        AwareLog.v(TAG, "checkCmpDataForUnistalled start0000");
        if (this.mHandler != null) {
            if (this.mHandler.hasMessages(16)) {
                this.mHandler.removeMessages(16);
            }
            Message msg = this.mHandler.obtainMessage();
            msg.what = 16;
            this.mHandler.sendMessageDelayed(msg, AppHibernateCst.DELAY_ONE_MINS);
        }
    }

    /* access modifiers changed from: private */
    public void checkAlarmCmpDataForUnistalled() {
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
                Map.Entry entry = it.next();
                String key = (String) entry.getKey();
                CmpTypeInfo value = (CmpTypeInfo) entry.getValue();
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
            Iterator<Map.Entry<Integer, ArrayMap<String, AlarmInfo>>> it = this.mAlarmMap.entrySet().iterator();
            while (it.hasNext()) {
                int uid = ((Integer) it.next().getKey()).intValue();
                if (UserHandle.getUserId(uid) == this.mCurUserId) {
                    boolean exist = false;
                    int i = 0;
                    while (true) {
                        if (i >= size) {
                            break;
                        }
                        ApplicationInfo aInfo = installedApps.get(i).applicationInfo;
                        if (aInfo != null && aInfo.uid == uid) {
                            exist = true;
                            break;
                        }
                        i++;
                    }
                    if (!exist) {
                        AwareLog.v(TAG, "checkAlarmDataForUnistalled remove uid :" + uid);
                        it.remove();
                    }
                }
            }
        }
    }

    public void getToastWindows(Set<Integer> toastPids, Set<Integer> evilPids) {
        if (mEnabled && toastPids != null) {
            synchronized (this.mToasts) {
                for (Map.Entry<Integer, AwareProcessWindowInfo> toast : this.mToasts.entrySet()) {
                    AwareLog.d(TAG, "getToastWindows pid:" + toast.getKey());
                    if (!toast.getValue().isEvil()) {
                        toastPids.add(toast.getKey());
                    } else if (evilPids != null) {
                        evilPids.add(toast.getKey());
                    }
                }
            }
            if (DEBUG) {
                AwareLog.d(TAG, "ToastPids:" + toastPids);
            }
        }
    }

    public boolean isToastWindows(int userid, String pkg) {
        if (!mEnabled || pkg == null) {
            return true;
        }
        synchronized (this.mToasts) {
            for (Map.Entry<Integer, AwareProcessWindowInfo> window : this.mToasts.entrySet()) {
                AwareProcessWindowInfo toastInfo = window.getValue();
                AwareLog.i(TAG, "[isToastWindows]:" + window.getKey() + " pkg:" + pkg + " isEvil:" + toastInfo.isEvil());
                if (pkg.equals(toastInfo.mPkg) && ((userid == -1 || userid == UserHandle.getUserId(toastInfo.mUid)) && !toastInfo.isEvil())) {
                    return true;
                }
            }
            return false;
        }
    }

    public boolean isEvilToastWindow(int window, int code) {
        boolean result;
        if (!mEnabled) {
            return false;
        }
        synchronized (this.mToasts) {
            AwareProcessWindowInfo toastInfo = this.mToasts.get(Integer.valueOf(window));
            if (toastInfo == null || !toastInfo.isEvil(code)) {
                result = false;
            } else {
                result = true;
            }
        }
        return result;
    }

    /* JADX WARNING: Removed duplicated region for block: B:32:0x00d6 A[Catch:{ all -> 0x0100, all -> 0x0105 }] */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00ec A[Catch:{ all -> 0x0100, all -> 0x0105 }] */
    private void initToastWindows() {
        Iterator<Bundle> it;
        boolean isEvil;
        AwareProcessWindowInfo toastInfo;
        List<Bundle> windowsList;
        List<Bundle> windowsList2 = HwWindowManager.getVisibleWindows(45);
        if (windowsList2 == null) {
            AwareLog.w(TAG, "Catch null when initToastWindows.");
            return;
        }
        synchronized (this.mToasts) {
            this.mToasts.clear();
            notifyToastChange(3, -1);
            Iterator<Bundle> it2 = windowsList2.iterator();
            while (it2.hasNext()) {
                Bundle windowState = it2.next();
                int window = windowState.getInt("window_pid");
                int mode = windowState.getInt("window_value");
                int code = windowState.getInt("window_state");
                int width = windowState.getInt("window_width");
                int height = windowState.getInt("window_height");
                float alpha = windowState.getFloat("window_alpha");
                boolean phide = windowState.getBoolean("window_hidden");
                String pkg = windowState.getString("window_package");
                int uid = windowState.getInt("window_uid");
                if (DEBUG) {
                    try {
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
                    } catch (Throwable th) {
                        th = th;
                        List<Bundle> list = windowsList2;
                    }
                } else {
                    it = it2;
                }
                try {
                    if (width != AwareProcessWindowInfo.getMinWindowWidth()) {
                        if (!(height == AwareProcessWindowInfo.getMinWindowHeight() || alpha == GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO || phide)) {
                            isEvil = false;
                            toastInfo = this.mToasts.get(Integer.valueOf(window));
                            if (toastInfo != null) {
                                toastInfo = new AwareProcessWindowInfo(mode, pkg, uid);
                                windowsList = windowsList2;
                                this.mToasts.put(Integer.valueOf(window), toastInfo);
                                notifyToastChange(5, window);
                            } else {
                                windowsList = windowsList2;
                            }
                            toastInfo.addWindow(Integer.valueOf(code), isEvil);
                            it2 = it;
                            windowsList2 = windowsList;
                        }
                    }
                    isEvil = true;
                    toastInfo = this.mToasts.get(Integer.valueOf(window));
                    if (toastInfo != null) {
                    }
                    toastInfo.addWindow(Integer.valueOf(code), isEvil);
                    it2 = it;
                    windowsList2 = windowsList;
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            }
        }
    }

    private void addToast(int window, int code, int width, int height, float alpha, String pkg, int uid) {
        AwareLog.i(TAG, "[addToast]:" + window + " [code]:" + code + " width:" + width + " height:" + height + " alpha:" + alpha);
        if (window > 0) {
            synchronized (this.mToasts) {
                AwareProcessWindowInfo toastInfo = this.mToasts.get(Integer.valueOf(window));
                boolean isEvil = (width == AwareProcessWindowInfo.getMinWindowWidth() && width > 0) || (height == AwareProcessWindowInfo.getMinWindowHeight() && height > 0) || alpha == GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
                if (toastInfo == null) {
                    toastInfo = new AwareProcessWindowInfo(3, pkg, uid);
                    this.mToasts.put(Integer.valueOf(window), toastInfo);
                    notifyToastChange(5, window);
                }
                toastInfo.addWindow(Integer.valueOf(code), isEvil);
            }
            if (DEBUG) {
                AwareLog.i(TAG, "[addToast]:" + window);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x005c, code lost:
        if (DEBUG == false) goto L_0x0074;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x005e, code lost:
        android.rms.iaware.AwareLog.i(TAG, "[removeToast]:" + r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0074, code lost:
        return;
     */
    private void removeToast(int window, int code) {
        AwareLog.i(TAG, "[removeToast]:" + window + " [code]:" + code);
        if (window > 0) {
            synchronized (this.mToasts) {
                AwareProcessWindowInfo toastInfo = this.mToasts.get(Integer.valueOf(window));
                if (toastInfo == null) {
                    this.mToasts.remove(Integer.valueOf(window));
                    return;
                }
                toastInfo.removeWindow(Integer.valueOf(code));
                if (toastInfo.mWindows.size() == 0) {
                    this.mToasts.remove(Integer.valueOf(window));
                    notifyToastChange(4, window);
                }
            }
        }
    }

    private void updateToast(int window, int code, int width, int height, float alpha, boolean phide) {
        boolean isEvil;
        AwareLog.i(TAG, "[updateToast]:" + window + " [code]:" + code + " width:" + width + " height:" + height + " alpha:" + alpha);
        if (window > 0) {
            synchronized (this.mToasts) {
                AwareProcessWindowInfo toastInfo = this.mToasts.get(Integer.valueOf(window));
                if (width > AwareProcessWindowInfo.getMinWindowWidth() && height > AwareProcessWindowInfo.getMinWindowHeight() && alpha != GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
                    if (!phide) {
                        isEvil = false;
                        if (toastInfo != null && toastInfo.containsWindow(code)) {
                            toastInfo.addWindow(Integer.valueOf(code), isEvil);
                        }
                    }
                }
                isEvil = true;
                toastInfo.addWindow(Integer.valueOf(code), isEvil);
            }
            if (DEBUG) {
                AwareLog.i(TAG, "[updateToast]:" + window);
            }
        }
    }

    private void hideToast(int window, int code) {
        AwareLog.i(TAG, "[hideToast]:" + window + " [code]:" + code);
        if (window > 0) {
            synchronized (this.mToasts) {
                AwareProcessWindowInfo toastInfo = this.mToasts.get(Integer.valueOf(window));
                if (toastInfo != null && toastInfo.containsWindow(code)) {
                    toastInfo.addWindow(Integer.valueOf(code), true);
                }
            }
            if (DEBUG) {
                AwareLog.i(TAG, "[hideToast]:" + window);
            }
        }
    }

    private Map<String, CmpTypeInfo> getAlarmCmpFromDB() {
        Map<String, CmpTypeInfo> result = new ArrayMap<>();
        List<CmpTypeInfo> cmplist = null;
        try {
            IBinder awareservice = IAwareCMSManager.getICMSManager();
            if (awareservice != null) {
                cmplist = IAwareCMSManager.getCmpTypeList(awareservice);
            } else {
                AwareLog.e(TAG, "can not find service IAwareCMSService.");
            }
        } catch (RemoteException e) {
            AwareLog.e(TAG, "getAlarmCmpFromDB RemoteException");
        }
        if (cmplist == null) {
            return result;
        }
        for (CmpTypeInfo info : cmplist) {
            if (info.getType() == 3 || info.getType() == 4) {
                result.put(info.getUserId() + "#" + info.getPkgName() + "#" + info.getCls(), info);
            }
        }
        return result;
    }

    /* access modifiers changed from: private */
    public boolean insertCmpRecgInfo(CmpTypeInfo info) {
        try {
            IBinder awareservice = IAwareCMSManager.getICMSManager();
            if (awareservice != null) {
                IAwareCMSManager.insertCmpTypeInfo(awareservice, info);
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
    public boolean deleteCmpRecgInfo(CmpTypeInfo info) {
        try {
            IBinder awareservice = IAwareCMSManager.getICMSManager();
            if (awareservice != null) {
                IAwareCMSManager.deleteCmpTypeInfo(awareservice, info);
                return true;
            }
            AwareLog.e(TAG, "can not find service IAwareCMSService.");
            return false;
        } catch (RemoteException e) {
            AwareLog.e(TAG, "deleteCmpRecgInfo RemoteException");
            return false;
        }
    }

    private boolean deleteAppCmpRecgInfo(int userid, String pkg) {
        try {
            IBinder awareservice = IAwareCMSManager.getICMSManager();
            if (awareservice != null) {
                IAwareCMSManager.deleteAppCmpTypeInfo(awareservice, userid, pkg);
                AwareLog.e(TAG, "delete pkg:" + pkg + " userid:" + userid + " from iAware.db");
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
        if (!mEnabled) {
            if (DEBUG) {
                AwareLog.d(TAG, "AwareIntelligentRecg feature disabled!");
            }
            return;
        }
        if (DEBUG) {
            AwareLog.d(TAG, "eventId: " + eventId);
        }
        if (bundleArgs != null) {
            if (!this.mIsInitialized.get()) {
                initialize();
            }
            switch (eventId) {
                case 8:
                    addToast(bundleArgs.getInt("window"), bundleArgs.getInt("hashcode"), bundleArgs.getInt("width"), bundleArgs.getInt("height"), bundleArgs.getFloat("alpha"), bundleArgs.getString(MemoryConstant.MEM_PREREAD_ITEM_NAME), bundleArgs.getInt("uid"));
                    break;
                case 9:
                    removeToast(bundleArgs.getInt("window"), bundleArgs.getInt("hashcode"));
                    break;
                case 17:
                    reportWallpaper(bundleArgs.getString(MemoryConstant.MEM_PREREAD_ITEM_NAME));
                    break;
                case 19:
                    sendPerceptionMessage(8, bundleArgs.getInt("tgtUid"), null);
                    break;
                case 20:
                    sendPerceptionMessage(9, bundleArgs.getInt("tgtUid"), bundleArgs.getString(MemoryConstant.MEM_PREREAD_ITEM_NAME));
                    break;
                case 22:
                    reportAlarm(bundleArgs.getString(MemoryConstant.MEM_PREREAD_ITEM_NAME), bundleArgs.getString("statstag"), bundleArgs.getInt("alarm_operation"), bundleArgs.getInt("tgtUid"));
                    break;
                case 27:
                    updateToast(bundleArgs.getInt("window"), bundleArgs.getInt("hashcode"), bundleArgs.getInt("width"), bundleArgs.getInt("height"), bundleArgs.getFloat("alpha"), bundleArgs.getBoolean("permanentlyhidden"));
                    break;
                case 28:
                    hideToast(bundleArgs.getInt("window"), bundleArgs.getInt("hashcode"));
                    break;
                case 29:
                    int userid = bundleArgs.getInt("userid");
                    if (userid != -10000) {
                        clearAlarmForRemoveUser(userid);
                        removeHwStopFlagByUserId(userid);
                        break;
                    }
                    break;
                case 20011:
                    this.mIsScreenOn.set(true);
                    break;
                case 90011:
                    this.mIsScreenOn.set(false);
                    break;
                default:
                    if (DEBUG) {
                        AwareLog.e(TAG, "Unknown EventID: " + eventId);
                        break;
                    }
                    break;
            }
        }
    }

    public void reportAppUpdate(int eventId, Bundle args) {
        if (mEnabled && args != null && this.mHandler != null) {
            Message msg = this.mHandler.obtainMessage();
            msg.what = 0;
            msg.arg1 = eventId;
            msg.setData(args);
            this.mHandler.sendMessage(msg);
        }
    }

    private void removeSpecTTSPkg(String pkg) {
        synchronized (this.mTTSPkg) {
            this.mTTSPkg.remove(pkg);
        }
    }

    private List<ResolveInfo> queryTTSService(String pkg) {
        if (this.mPackageManager == null) {
            return null;
        }
        Intent intent = new Intent("android.intent.action.TTS_SERVICE");
        if (pkg != null) {
            intent.setPackage(pkg);
        }
        return this.mPackageManager.queryIntentServicesAsUser(intent, 851968, this.mCurUserId);
    }

    private void initSpecTTSPkg(String pkg) {
        List<ResolveInfo> resolveInfos = queryTTSService(pkg);
        if (resolveInfos != null && resolveInfos.size() > 0) {
            synchronized (this.mTTSPkg) {
                this.mTTSPkg.add(pkg);
            }
        }
    }

    private void initTTSPkg() {
        List<ResolveInfo> resolveInfos = queryTTSService(null);
        Set<String> ttsPkg = new ArraySet<>();
        if (resolveInfos != null && resolveInfos.size() > 0) {
            for (ResolveInfo ri : resolveInfos) {
                String pkg = ri.getComponentInfo().packageName;
                if (!ttsPkg.contains(pkg)) {
                    ttsPkg.add(pkg);
                }
            }
        }
        synchronized (this.mTTSPkg) {
            this.mTTSPkg.clear();
            this.mTTSPkg.addAll(ttsPkg);
        }
    }

    private void initSettings() {
        this.mDefaultInputMethod = readDefaultInputMethod();
        sendInputMethodSetMessage();
        initBlindPkg();
        updateAccessbilityService();
        updateDefaultTTS();
        initDefaultWallPaper();
        this.mDefaultSms = readDefaultSmsPackage();
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
    public void updateAccessbilityService() {
        if (this.mMtmService != null) {
            String settingValue = Settings.Secure.getStringForUser(this.mMtmService.context().getContentResolver(), "enabled_accessibility_services", this.mCurUserId);
            if (DEBUG) {
                AwareLog.i(TAG, "Accessbility:" + settingValue);
            }
            Set<String> accessPkg = new ArraySet<>();
            if (settingValue != null && !settingValue.equals("")) {
                for (String accessbilityService : settingValue.split(SEPARATOR_SEMICOLON)) {
                    int indexof = accessbilityService.indexOf(47);
                    if (indexof > 0) {
                        accessPkg.add(accessbilityService.substring(0, indexof));
                    }
                }
            }
            synchronized (this.mAccessPkg) {
                this.mAccessPkg.clear();
                this.mAccessPkg.addAll(accessPkg);
            }
            Set<String> blindPkgs = new ArraySet<>();
            for (String access : accessPkg) {
                synchronized (this.mTTSPkg) {
                    if (this.mTTSPkg.contains(access)) {
                        blindPkgs.add(access);
                    }
                }
                synchronized (this.mBlindPkg) {
                    if (this.mBlindPkg.contains(access)) {
                        blindPkgs.add(access);
                    }
                }
            }
            if (this.mDataMgr != null) {
                this.mDataMgr.updateBlind(blindPkgs);
                AppStatusUtils.getInstance().updateBlind(blindPkgs);
                sendFlushToDiskMessage();
            }
        }
    }

    private void sendFlushToDiskMessage() {
        if (this.mHandler != null) {
            Message msg = this.mHandler.obtainMessage();
            msg.what = 6;
            this.mHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    public void updateDefaultTTS() {
        if (this.mMtmService != null) {
            String settingValue = Settings.Secure.getStringForUser(this.mMtmService.context().getContentResolver(), "tts_default_synth", this.mCurUserId);
            if (DEBUG) {
                AwareLog.i(TAG, "Default TTS :" + settingValue);
            }
            this.mDefaultTTS = settingValue;
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x006d, code lost:
        return;
     */
    public void updateBluetoothConnect() {
        if (this.mMtmService != null) {
            String settingValue = Settings.Secure.getStringForUser(this.mMtmService.context().getContentResolver(), VALUE_BT_BLE_CONNECT_APPS, this.mCurUserId);
            if (DEBUG) {
                AwareLog.i(TAG, "Bluetooth connect :" + settingValue);
            }
            synchronized (this.mBluetoothLock) {
                this.mBtoothConnectList.clear();
                if (settingValue != null) {
                    if (!settingValue.equals("")) {
                        for (String strPid : settingValue.split(HwLogRecordManager.VERTICAL_ESC_SEPARATE)) {
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
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x006f, code lost:
        return;
     */
    public void updateBluetoothLastDisconnect() {
        if (this.mMtmService != null) {
            String settingValue = Settings.Secure.getStringForUser(this.mMtmService.context().getContentResolver(), VALUE_BT_LAST_BLE_DISCONNECT, this.mCurUserId);
            if (DEBUG) {
                AwareLog.i(TAG, "Bluetooth last disconnect :" + settingValue);
            }
            synchronized (this.mBluetoothLock) {
                this.mBtoothLastPid = -1;
                if (settingValue != null) {
                    if (!settingValue.equals("")) {
                        String[] strings = settingValue.split(HwLogRecordManager.VERTICAL_ESC_SEPARATE);
                        if (2 == strings.length) {
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
    public String readDefaultInputMethod() {
        if (this.mMtmService == null) {
            return "";
        }
        String inputInfo = Settings.Secure.getStringForUser(this.mMtmService.context().getContentResolver(), "default_input_method", this.mCurUserId);
        if (inputInfo != null) {
            String[] defaultIms = inputInfo.split("/");
            if (defaultIms[0] != null) {
                return defaultIms[0];
            }
        }
        return "";
    }

    /* access modifiers changed from: private */
    public void initDefaultWallPaper() {
        String pkg = readDefaultWallPaper();
        if (pkg != null) {
            AwareLog.i(TAG, "initDefaultWallPaper  pkg =" + pkg);
            sendWallpaperSetMessage(pkg);
        } else if (this.mInitWallPaperCount.get() >= 0) {
            AwareLog.i(TAG, "AwareIntelligentRecg send read wallpaper message again mInitWallPaperCount=" + (this.mInitWallPaperCount.get() - 1));
            sendInitWallPaperMessage();
            this.mInitWallPaperCount.decrementAndGet();
        } else {
            AwareLog.e(TAG, "AwareIntelligentRecg has no wallpaper");
        }
    }

    private String readDefaultWallPaper() {
        WallpaperInfo winfo = null;
        IWallpaperManager wallpaper = IWallpaperManager.Stub.asInterface(ServiceManager.getService("wallpaper"));
        if (wallpaper != null) {
            try {
                winfo = wallpaper.getWallpaperInfo(UserHandle.myUserId());
            } catch (RemoteException e) {
                AwareLog.e(TAG, "Couldn't read  Default WallPaper");
            }
        }
        if (winfo != null) {
            return winfo.getPackageName();
        }
        return null;
    }

    /* access modifiers changed from: private */
    public String readDefaultSmsPackage() {
        if (this.mMtmService == null) {
            return "";
        }
        String inputInfo = Settings.Secure.getStringForUser(this.mMtmService.context().getContentResolver(), "sms_default_application", this.mCurUserId);
        if (inputInfo != null) {
            String[] defaultIms = inputInfo.split("/");
            if (defaultIms[0] != null) {
                return defaultIms[0];
            }
        }
        return "";
    }

    private boolean isTTSPkg(String pkg) {
        if (!mEnabled) {
            return false;
        }
        synchronized (this.mTTSPkg) {
            if (this.mTTSPkg.contains(pkg)) {
                return true;
            }
            return false;
        }
    }

    private boolean isPayPkg(String pkg, AwareAppStartStatusCache status) {
        if (!mEnabled) {
            return false;
        }
        if (this.mPayPkgList == null || !this.mPayPkgList.contains(pkg)) {
            return isAttrApp(pkg, status, 1);
        }
        return true;
    }

    private boolean isSharePkg(String pkg, AwareAppStartStatusCache status) {
        if (!mEnabled) {
            return false;
        }
        if (this.mSharePkgList == null || !this.mSharePkgList.contains(pkg)) {
            return isAttrApp(pkg, status, 2);
        }
        return true;
    }

    private boolean isAttrApp(String pkg, AwareAppStartStatusCache status, int attr) {
        boolean z = false;
        if (!mEnabled) {
            return false;
        }
        int appAttri = getAttributeCache(pkg, status);
        if (appAttri == -1) {
            return false;
        }
        if ((appAttri & attr) == attr) {
            z = true;
        }
        return z;
    }

    private int getAttributeCache(String pkg, AwareAppStartStatusCache status) {
        int appAttri = status.mAppAttribute;
        if (appAttri != -1) {
            return appAttri;
        }
        int appAttri2 = AppTypeRecoManager.getInstance().getAppAttribute(pkg);
        status.mAppAttribute = appAttri2;
        return appAttri2;
    }

    public String getDefaultInputMethod() {
        return this.mDefaultInputMethod;
    }

    public String getDefaultTTS() {
        return this.mDefaultTTS;
    }

    public String getDefaultWallPaper() {
        return this.mDefaultWallPaper;
    }

    public String getDefaultSmsPackage() {
        return this.mDefaultSms;
    }

    private void reportWallpaper(String pkg) {
        if (DEBUG) {
            AwareLog.i(TAG, "reportWallpaper  pkg=" + pkg);
        }
        if (pkg != null && !pkg.isEmpty()) {
            sendWallpaperSetMessage(pkg);
        }
    }

    private int getDefaultInputMethodUid() {
        return this.mDefaultInputMethodUid;
    }

    private int getDefaultWallPaperUid() {
        return this.mDefaultWallPaperUid;
    }

    private void sendWallpaperSetMessage(String pkg) {
        Message msg = this.mHandler.obtainMessage();
        msg.what = 13;
        msg.obj = pkg;
        this.mHandler.sendMessage(msg);
    }

    /* access modifiers changed from: private */
    public void handleWallpaperSetMessage(String pkg) {
        this.mDefaultWallPaper = pkg;
        this.mDefaultWallPaperUid = getUIDByPackageName(pkg);
    }

    /* access modifiers changed from: private */
    public void sendInputMethodSetMessage() {
        if (this.mHandler != null) {
            Message msg = this.mHandler.obtainMessage();
            msg.what = 15;
            this.mHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    public void handleInputMethodSetMessage() {
        this.mDefaultInputMethodUid = getUIDByPackageName(this.mDefaultInputMethod);
    }

    private int getUIDByPackageName(String pkg) {
        if (this.mMtmService == null) {
            return -1;
        }
        return getUIDByPackageName(this.mMtmService.context(), pkg);
    }

    private int getUIDByPackageName(Context context, String pkg) {
        int uid = -1;
        if (pkg == null || context == null) {
            return -1;
        }
        PackageManager pm = context.getPackageManager();
        if (pm == null) {
            return -1;
        }
        try {
            ApplicationInfo appInfo = pm.getApplicationInfo(pkg, 0);
            if (appInfo != null) {
                uid = appInfo.uid;
            }
        } catch (PackageManager.NameNotFoundException e) {
            AwareLog.e(TAG, "get the package uid faied");
        }
        return uid;
    }

    /* access modifiers changed from: private */
    public void recordStartInfo(AwareAppStartInfo startInfo) {
        if (startInfo != null) {
            ArrayList<AwareAppStartInfo> mRemoveList = new ArrayList<>();
            long currentTime = SystemClock.elapsedRealtime();
            synchronized (this.mAppStartInfoList) {
                for (AwareAppStartInfo item : this.mAppStartInfoList) {
                    if (currentTime - item.timestamp > HwRecentsTaskUtils.MAX_REMOVE_TASK_TIME) {
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

    /* access modifiers changed from: private */
    public boolean needRecgPushSDK(AwareAppStartInfo startInfo) {
        if (startInfo == null) {
            return false;
        }
        String cls = startInfo.cls;
        if (cls == null) {
            return false;
        }
        synchronized (this.mGoodPushSDKClsList) {
            if (this.mGoodPushSDKClsList.contains(cls)) {
                return false;
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    public void adjustPushSDKCmp(AwareAppStartInfo startInfo) {
        if (startInfo != null) {
            ArrayMap<Integer, ArraySet<AwareAppStartInfo>> mStartMap = new ArrayMap<>();
            long currentTime = SystemClock.elapsedRealtime();
            synchronized (this.mAppStartInfoList) {
                for (AwareAppStartInfo item : this.mAppStartInfoList) {
                    if (currentTime - item.timestamp < HwRecentsTaskUtils.MAX_REMOVE_TASK_TIME) {
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
                if (DEBUG) {
                    AwareLog.d(TAG, "PushSDK check cls uid:" + uid + " size:" + sameCallerStartInfos.size());
                }
                clsMap.clear();
                Iterator<AwareAppStartInfo> it = sameCallerStartInfos.iterator();
                while (it.hasNext()) {
                    AwareAppStartInfo item2 = it.next();
                    if (item2.cls != null) {
                        if (DEBUG) {
                            AwareLog.d(TAG, "PushSDK check cls :" + item2.cls);
                        }
                        if (!clsMap.containsKey(item2.cls)) {
                            clsMap.put(item2.cls, 1);
                        } else {
                            int count = clsMap.get(item2.cls).intValue();
                            clsMap.put(item2.cls, Integer.valueOf(count + 1));
                            updatePushSDKCls(item2.cls, count + 1);
                        }
                    }
                }
            }
        }
    }

    private void updatePushSDKCls(String cls, int count) {
        if (count < 2) {
            if (DEBUG) {
                AwareLog.d(TAG, "PushSDK start cls count must be > :2");
            }
            return;
        }
        synchronized (this.mPushSDKClsList) {
            if (!this.mPushSDKClsList.contains(cls)) {
                this.mPushSDKClsList.add(cls);
                sendUpdateCmpInfoMessage("", cls, 0, true);
            }
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
        if (DEBUG) {
            AwareLog.d(TAG, "getAppType From Habit pkg " + pkgName + " type : " + type);
        }
        boolean z = false;
        if (this.mUnRemindAppTypeList == null || !this.mUnRemindAppTypeList.contains(Integer.valueOf(type))) {
            return false;
        }
        int appAttr = AppTypeRecoManager.getInstance().getAppAttribute(pkgName);
        if (appAttr == -1) {
            return true;
        }
        if ((appAttr & 4) != 4) {
            z = true;
        }
        return z;
    }

    private void initAlarm(AlarmInfo alarm, CmpTypeInfo alarmRecgInfo, boolean isAdd, boolean isWakeup, long time) {
        boolean z = true;
        alarm.status = alarmRecgInfo != null ? alarmRecgInfo.getType() == 4 ? 2 : 1 : 0;
        alarm.count = isAdd;
        alarm.startTime = isWakeup ? time : 0;
        alarm.perception_count = alarmRecgInfo != null ? alarmRecgInfo.getPerceptionCount() : 0;
        alarm.unperception_count = alarmRecgInfo != null ? alarmRecgInfo.getUnPerceptionCount() : 0;
        if (isAdd) {
            if (!AwareAppAssociate.getInstance().isForeGroundApp(alarm.uid) || !this.mIsScreenOn.get()) {
                z = false;
            }
            alarm.bfgset = z;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:65:0x010d A[Catch:{ all -> 0x017c }] */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x0114 A[Catch:{ all -> 0x017c }] */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x0148  */
    private void reportAlarm(String packageName, String statustag, int operation, int uid) {
        ArrayMap<Integer, ArrayMap<String, AlarmInfo>> arrayMap;
        long time;
        AlarmInfo alarm;
        long time2;
        AlarmInfo alarm2;
        AlarmInfo alarm3;
        ArrayMap arrayMap2;
        String str = packageName;
        String str2 = statustag;
        int i = operation;
        int i2 = uid;
        if (UserHandle.getAppId(uid) < 10000 || str == null || str2 == null || isUnReminderApp(packageName)) {
            return;
        }
        if (this.mAlarmPkgList == null || !this.mAlarmPkgList.contains(str)) {
            String[] strs = str2.split(SEPARATOR_SEMICOLON);
            if (strs.length == 2) {
                String tag = strs[1];
                if (DEBUG) {
                    AwareLog.i(TAG, "reportAlarm for clock app pkg : " + str + " tag : " + tag + " uid : " + i2 + " operation : " + i);
                }
                int userId = UserHandle.getUserId(uid);
                CmpTypeInfo alarmRecgInfo = getAlarmRecgResult(i2, str, tag);
                boolean isAdd = i == 0;
                boolean isWakeup = i == 2;
                long time3 = SystemClock.elapsedRealtime();
                ArrayMap<Integer, ArrayMap<String, AlarmInfo>> arrayMap3 = this.mAlarmMap;
                synchronized (arrayMap3) {
                    try {
                        ArrayMap<String, AlarmInfo> alarms = this.mAlarmMap.get(Integer.valueOf(uid));
                        if (alarms == null) {
                            try {
                                ArrayMap arrayMap4 = new ArrayMap();
                                alarm3 = new AlarmInfo(i2, str, tag);
                                alarm2 = alarm3;
                                arrayMap2 = arrayMap4;
                                arrayMap = arrayMap3;
                                time2 = time3;
                            } catch (Throwable th) {
                                th = th;
                                arrayMap = arrayMap3;
                                long j = time3;
                                throw th;
                            }
                            try {
                                initAlarm(alarm3, alarmRecgInfo, isAdd, isWakeup, time3);
                                arrayMap2.put(tag, alarm2);
                                this.mAlarmMap.put(Integer.valueOf(uid), arrayMap2);
                            } catch (Throwable th2) {
                                th = th2;
                                AlarmInfo alarmInfo = alarm2;
                            }
                        } else {
                            arrayMap = arrayMap3;
                            time2 = time3;
                            try {
                                alarm = alarms.get(tag);
                                if (alarm == null) {
                                    try {
                                        alarm2 = new AlarmInfo(i2, str, tag);
                                        ArrayMap<String, AlarmInfo> alarms2 = alarms;
                                        initAlarm(alarm2, alarmRecgInfo, isAdd, isWakeup, time2);
                                        alarms2.put(tag, alarm2);
                                    } catch (Throwable th3) {
                                        th = th3;
                                        throw th;
                                    }
                                } else {
                                    ArrayMap<String, AlarmInfo> arrayMap5 = alarms;
                                    if (isWakeup) {
                                        if (alarm.count > 0) {
                                            time = time2;
                                            try {
                                                alarm.startTime = time;
                                                alarm.bhandled = false;
                                                if (!isAdd) {
                                                    alarm.count++;
                                                } else if (alarm.count > 0) {
                                                    alarm.count--;
                                                }
                                                if (isWakeup && userId == this.mCurUserId && time > this.mLastPerceptionTime.get() + 15000) {
                                                    sendUnPerceptionMessage(alarm);
                                                }
                                                AlarmInfo alarmInfo2 = alarm;
                                                if (SystemClock.elapsedRealtime() / 86400000 > ((long) this.mDBUpdateCount)) {
                                                    sendUpdateDbMessage();
                                                    this.mDBUpdateCount++;
                                                    if (DEBUG) {
                                                        AwareLog.i(TAG, "UPDATE_DB_INTERVAL mDBUpdateCount : " + this.mDBUpdateCount);
                                                    }
                                                }
                                            } catch (Throwable th4) {
                                                th = th4;
                                                throw th;
                                            }
                                        }
                                    }
                                    time = time2;
                                    if (!isAdd) {
                                    }
                                    sendUnPerceptionMessage(alarm);
                                    AlarmInfo alarmInfo22 = alarm;
                                    if (SystemClock.elapsedRealtime() / 86400000 > ((long) this.mDBUpdateCount)) {
                                    }
                                }
                            } catch (Throwable th5) {
                                th = th5;
                                long j2 = time2;
                                throw th;
                            }
                        }
                        alarm = alarm2;
                        time = time2;
                        try {
                            sendUnPerceptionMessage(alarm);
                            AlarmInfo alarmInfo222 = alarm;
                            if (SystemClock.elapsedRealtime() / 86400000 > ((long) this.mDBUpdateCount)) {
                            }
                        } catch (Throwable th6) {
                            th = th6;
                            AlarmInfo alarmInfo3 = alarm;
                            throw th;
                        }
                    } catch (Throwable th7) {
                        th = th7;
                        arrayMap = arrayMap3;
                        long j3 = time3;
                        throw th;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x0110, code lost:
        r0 = r4.keySet().iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x011c, code lost:
        if (r0.hasNext() == false) goto L_0x0132;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x011e, code lost:
        r2 = r0.next();
        updateAlarmCmp(r2, r4.get(r2).booleanValue());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x0132, code lost:
        return;
     */
    public void handlePerceptionEvent(int uid, String pkg, int event) {
        int i = uid;
        int i2 = event;
        if (DEBUG) {
            AwareLog.i(TAG, "handlePerceptionEvent uid : " + i + " pkg : " + pkg + " for event : " + i2);
        } else {
            String str = pkg;
        }
        if (UserHandle.getAppId(uid) >= 10000) {
            int i3 = 9;
            if (i2 != 9 || !AwareAppAssociate.getInstance().isForeGroundApp(i)) {
                long now = SystemClock.elapsedRealtime();
                this.mLastPerceptionTime.set(now);
                ArrayMap<AlarmInfo, Boolean> alarmMap = new ArrayMap<>();
                synchronized (this.mAlarmMap) {
                    ArrayMap<String, AlarmInfo> alarms = this.mAlarmMap.get(Integer.valueOf(uid));
                    if (alarms != null) {
                        boolean bUpdateDb = false;
                        for (String tag : alarms.keySet()) {
                            AlarmInfo alarm = alarms.get(tag);
                            if (i2 != i3 || alarm.bfgset) {
                                if (this.mHandler.hasMessages(11, alarm)) {
                                    this.mHandler.removeMessages(11, alarm);
                                }
                                long alarmtime = alarm.startTime;
                                if (alarmtime + 15000 >= now && alarmtime <= now && (!alarm.bhandled || alarm.reason != i2)) {
                                    alarm.reason = i2;
                                    alarm.perception_count++;
                                    alarm.bhandled = true;
                                    int newStatus = getNewPerceptionStatus(alarm);
                                    if (newStatus != alarm.status && newStatus == 1) {
                                        alarm.status = 1;
                                        bUpdateDb = true;
                                    }
                                    int i4 = newStatus;
                                    alarmMap.put(alarm.copy(), Boolean.valueOf(bUpdateDb));
                                    if (DEBUG) {
                                        AwareLog.i(TAG, "alarm is a clock alarm : " + alarm.packagename + " tag : " + tag + " for reason : " + i2);
                                    }
                                }
                                i3 = 9;
                                int i5 = uid;
                                String str2 = pkg;
                            }
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleUnPerceptionEvent(AlarmInfo alarm, int event) {
        AlarmInfo updateAlarm;
        if (alarm != null) {
            boolean bUpdateDb = false;
            synchronized (this.mAlarmMap) {
                alarm.reason = event;
                alarm.unperception_count++;
                alarm.bhandled = true;
                if (DEBUG) {
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
        int status = 1;
        int i = 5;
        if (AppTypeRecoManager.getInstance().getAppType(alarm.packagename) != 5) {
            i = 1;
        }
        int factor = i;
        if (alarm.unperception_count >= 1 && alarm.unperception_count > alarm.perception_count * factor) {
            status = 2;
        }
        return status;
    }

    private CmpTypeInfo getAlarmRecgResult(int uid, String pkg, String tag) {
        if (pkg == null || tag == null) {
            return null;
        }
        String key = UserHandle.getUserId(uid) + "#" + pkg + "#" + tag;
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
            String key = UserHandle.getUserId(alarm.uid) + "#" + alarm.packagename + "#" + alarm.tag;
            synchronized (this.mAlarmCmps) {
                cmpInfo = this.mAlarmCmps.get(key);
                if (cmpInfo == null) {
                    cmpInfo = new CmpTypeInfo();
                    cmpInfo.setUserId(UserHandle.getUserId(alarm.uid));
                    cmpInfo.setPkgName(alarm.packagename);
                    cmpInfo.setCls(alarm.tag);
                    cmpInfo.setTime(System.currentTimeMillis());
                    this.mAlarmCmps.put(key, cmpInfo);
                }
                cmpInfo.setType(alarm.status == 2 ? 4 : 3);
                cmpInfo.setPerceptionCount(alarm.perception_count);
                cmpInfo.setUnPerceptionCount(alarm.unperception_count);
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
                String pkgKey = UserHandle.getUserId(uid) + "#" + pkg + "#";
                while (it.hasNext()) {
                    String key = (String) it.next().getKey();
                    if (key != null && key.indexOf(pkgKey) == 0) {
                        it.remove();
                    }
                }
            }
        }
    }

    private void removeAlarmCmpByUserid(int userid) {
        synchronized (this.mAlarmCmps) {
            Iterator<Map.Entry<String, CmpTypeInfo>> it = this.mAlarmCmps.entrySet().iterator();
            String useridKey = userid + "#";
            while (it.hasNext()) {
                String key = (String) it.next().getKey();
                if (key != null && key.indexOf(useridKey) == 0) {
                    it.remove();
                }
            }
        }
    }

    private void removeAlarms(int uid) {
        if (uid > 0) {
            if (DEBUG) {
                AwareLog.i(TAG, "removeAlarms uid : " + uid);
            }
            synchronized (this.mAlarmMap) {
                Iterator<Map.Entry<Integer, ArrayMap<String, AlarmInfo>>> it = this.mAlarmMap.entrySet().iterator();
                while (it.hasNext()) {
                    if (uid == ((Integer) it.next().getKey()).intValue()) {
                        it.remove();
                    }
                }
            }
        }
    }

    private void removeAlarmsByUserid(int userid) {
        if (DEBUG) {
            AwareLog.i(TAG, "removeAlarmsByUserid userid : " + userid);
        }
        synchronized (this.mAlarmMap) {
            Iterator<Map.Entry<Integer, ArrayMap<String, AlarmInfo>>> it = this.mAlarmMap.entrySet().iterator();
            while (it.hasNext()) {
                if (userid == UserHandle.getUserId(((Integer) it.next().getKey()).intValue())) {
                    it.remove();
                }
            }
        }
    }

    public int getAlarmActionType(int uid, String pkg, String action) {
        String key = UserHandle.getUserId(uid) + "#" + pkg + "#" + action;
        synchronized (this.mAlarmCmps) {
            CmpTypeInfo info = this.mAlarmCmps.get(key);
            if (info == null) {
                return -1;
            }
            int type = info.getType();
            return type;
        }
    }

    public List<String> getAllInvalidAlarmTags(int uid, String pkg) {
        if (pkg == null) {
            return null;
        }
        List<String> result = new ArrayList<>();
        if (this.mAlarmPkgList != null && this.mAlarmPkgList.contains(pkg)) {
            return result;
        }
        if (isUnReminderApp(pkg)) {
            return null;
        }
        synchronized (this.mAlarmCmps) {
            String pkgKey = UserHandle.getUserId(uid) + "#" + pkg + "#";
            for (Map.Entry<String, CmpTypeInfo> m : this.mAlarmCmps.entrySet()) {
                if (m != null) {
                    String key = m.getKey();
                    if (key != null) {
                        if (key.indexOf(pkgKey) == 0) {
                            CmpTypeInfo value = m.getValue();
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

    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0066, code lost:
        return true;
     */
    public boolean hasPerceptAlarm(int uid, List<String> packageList) {
        if (!mEnabled) {
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
        if (isUnReminderApp(packageList) != 0) {
            return false;
        }
        synchronized (this.mAlarmMap) {
            ArrayMap<String, AlarmInfo> alarms = this.mAlarmMap.get(Integer.valueOf(uid));
            if (alarms == null) {
                return false;
            }
            for (Map.Entry<String, AlarmInfo> entry : alarms.entrySet()) {
                if (entry != null) {
                    AlarmInfo info = entry.getValue();
                    if (info == null || !(info.status == 0 || info.status == 1)) {
                    }
                }
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    public void handleUpdateDB() {
        Map<String, CmpTypeInfo> alarmsInDB = getAlarmCmpFromDB();
        synchronized (this.mAlarmCmps) {
            for (Map.Entry entry : this.mAlarmCmps.entrySet()) {
                CmpTypeInfo value = (CmpTypeInfo) entry.getValue();
                CmpTypeInfo alarmInDB = alarmsInDB.get(entry.getKey());
                if (alarmInDB == null || alarmInDB.getPerceptionCount() != value.getPerceptionCount() || alarmInDB.getUnPerceptionCount() != value.getUnPerceptionCount()) {
                    insertCmpRecgInfo(value);
                }
            }
        }
    }

    public void updateWidget(Set<String> widgets, String pkgName) {
        if (this.mDataMgr != null) {
            if (this.mDataMgr.updateWidgetList(widgets)) {
                sendFlushToDiskMessage();
            }
            widgetTrigUpdate(pkgName);
        }
    }

    public void widgetTrigUpdate(String pkgName) {
        if (pkgName != null && this.mDataMgr != null) {
            if (DEBUG) {
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
        if (pkgName == null || this.mDataMgr == null) {
            return false;
        }
        long time = this.mDataMgr.getWidgetExistPkgUpdateTime(pkgName);
        if (time == -1) {
            return false;
        }
        if (this.mDataMgr.getWidgetCnt() > this.mWidgetCheckUpdateCnt && SystemClock.elapsedRealtime() - time > this.mWidgetCheckUpdateInterval) {
            return !isUpDownTrigInIntervalTime(uid, this.mWidgetCheckUpdateInterval);
        }
        return true;
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
        if (this.mHandler != null) {
            Message msg = this.mHandler.obtainMessage();
            msg.what = 19;
            this.mHandler.removeMessages(19);
            this.mHandler.sendMessageDelayed(msg, this.mWidgetCheckUpdateInterval);
        }
    }

    /* access modifiers changed from: private */
    public void handleWidgetUpDownLoadClear() {
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

    public boolean isPushSdkComp(int callerUid, String compName, int targetUid, AppMngConstant.AppStartSource source, boolean isSysApp, boolean abroad) {
        int index;
        if (!mEnabled) {
            return false;
        }
        if (DEBUG) {
            AwareLog.d(TAG, "PushSDK isPushSdkComp callerUid :" + callerUid + " targetUid : " + targetUid + " cmp: " + compName);
        }
        if (compName == null || callerUid == targetUid || AwareAppAssociate.getInstance().getCurHomeProcessUid() == callerUid || compName.isEmpty()) {
            return false;
        }
        if (this.mDataMgr != null && (isSysApp || this.mDataMgr.isSpecialCaller(callerUid))) {
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
        if (!AppMngConstant.AppStartSource.THIRD_ACTIVITY.equals(source) && !AppMngConstant.AppStartSource.BIND_SERVICE.equals(source) && !abroad && strs.length == 2) {
            sendRecgPushSdkMessage(callerUid, compName, targetUid);
        }
        return false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001a, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001d, code lost:
        if ((r0 & 1) != 1) goto L_0x0021;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x001f, code lost:
        r1 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0021, code lost:
        return r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x000f, code lost:
        r0 = android.rms.iaware.ComponentRecoManager.getInstance().getComponentBadFunc(r5);
        r1 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0018, code lost:
        if (r0 != 0) goto L_0x001b;
     */
    private boolean isBadPushSdkComp(String cls) {
        synchronized (this.mPushSDKClsList) {
            if (this.mPushSDKClsList.contains(cls)) {
                return true;
            }
        }
    }

    private boolean isAllowStartAppByTopN(String pkg, AwareAppStartStatusCache status, int type, int topN, boolean isBoot) {
        if (this.mDeviceTotalMemory <= 3072) {
            return false;
        }
        if (status.mAppType == -100) {
            status.mAppType = getAppMngSpecType(pkg);
        }
        if (status.mAppType == type) {
            return isTopHabitAppInStart(pkg, type, topN, isBoot);
        }
        return false;
    }

    private boolean isAlarmApp(String pkg, AwareAppStartStatusCache status) {
        if (status.unPercetibleAlarm == 1 || isUnReminderApp(pkg)) {
            return false;
        }
        if (this.mAlarmPkgList != null && this.mAlarmPkgList.contains(pkg)) {
            return true;
        }
        int alarmType = -1;
        if (status.mAction != null) {
            alarmType = getAlarmActionType(status.mUid, pkg, status.mAction);
        } else if (status.mCompName != null) {
            alarmType = getAlarmActionType(status.mUid, pkg, status.mCompName);
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
        if (status.mStatusCacheExt.mAbroad) {
            return AppStartPolicyCfg.AppStartOversea.OVERSEA.ordinal();
        }
        return AppStartPolicyCfg.AppStartOversea.CHINA.ordinal();
    }

    public int getAppStartSpecAppSrcRange(String packageName, AwareAppStartStatusCache status) {
        if (!this.mAppStartEnabled) {
            return -1;
        }
        if (status.mStatusCacheExt.mAppSrcRange == -100) {
            status.mStatusCacheExt.mAppSrcRange = getAppStartSpecAppSrcRangeResult(packageName);
        }
        boolean srcRange = true;
        if (!(status.mStatusCacheExt.mAppSrcRange == 0 || status.mStatusCacheExt.mAppSrcRange == 1)) {
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
        if (status.mStatusCacheExt.mAppSrcRange == -100) {
            status.mStatusCacheExt.mAppSrcRange = getAppStartSpecAppSrcRangeResult(packageName);
        }
        if (status.mStatusCacheExt.mAppSrcRange == 0) {
            return AppStartPolicyCfg.AppStartAppOversea.CHINA.ordinal();
        }
        if (status.mStatusCacheExt.mAppSrcRange == 1) {
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
        if (!mEnabled) {
            return 1;
        }
        return this.mDeviceLevel;
    }

    private boolean isAppStartSpecCallerAction(String pkg, int appAction, AwareAppStartStatusCache status, AppMngConstant.AppStartSource source) {
        if (!this.mAppStartEnabled) {
            return false;
        }
        if (DEBUG) {
            AwareLog.i(TAG, "isAppStartSpecAction " + pkg + ",action:" + appAction);
        }
        if (appAction < 0 || appAction >= AppStartPolicyCfg.AppStartCallerAction.values().length) {
            return false;
        }
        return isAppStartSpecCallerActionComm(pkg, AppStartPolicyCfg.AppStartCallerAction.values()[appAction], status, source);
    }

    private boolean isAppStartSpecCallerActionComm(String pkg, AppStartPolicyCfg.AppStartCallerAction appAction, AwareAppStartStatusCache status, AppMngConstant.AppStartSource source) {
        if (!mEnabled) {
            return false;
        }
        switch (appAction) {
            case PUSHSDK:
                return isPushSdkComp(status.mCallerUid, (AppMngConstant.AppStartSource.THIRD_BROADCAST.equals(source) || AppMngConstant.AppStartSource.SYSTEM_BROADCAST.equals(source)) ? status.mAction : status.mCompName, status.mUid, source, status.mIsSystemApp, status.mStatusCacheExt.mAbroad);
            case BTMEDIABROWSER:
                return status.mIsBtMediaBrowserCaller;
            case NOTIFYLISTENER:
                return status.mNotifyListenerCaller;
            default:
                return false;
        }
    }

    private boolean isAppStartSpecCallerStatus(String pkg, int appStatus, AwareAppStartStatusCache status) {
        if (!this.mAppStartEnabled) {
            return false;
        }
        if (DEBUG) {
            AwareLog.i(TAG, "isAppStartSpecCallerStatus " + pkg + ",action:" + appStatus);
        }
        if (appStatus < 0 || appStatus >= AppStartPolicyCfg.AppStartCallerStatus.values().length) {
            return false;
        }
        switch (AppStartPolicyCfg.AppStartCallerStatus.values()[appStatus]) {
            case BACKGROUND:
                return !status.mIsCallerFg;
            case FOREGROUND:
                return status.mIsCallerFg;
            default:
                return false;
        }
    }

    private boolean isAppStartSpecType(String pkg, int appType, AwareAppStartStatusCache status) {
        boolean z = false;
        if (!this.mAppStartEnabled) {
            return false;
        }
        if (DEBUG) {
            AwareLog.i(TAG, "isAppStartSpecType " + pkg + ",type:" + appType);
        }
        if (appType < 0 || appType >= AppStartPolicyCfg.AppStartTargetType.values().length) {
            return false;
        }
        AppStartPolicyCfg.AppStartTargetType appStartEnum = AppStartPolicyCfg.AppStartTargetType.values()[appType];
        if (AnonymousClass2.$SwitchMap$com$android$server$rms$iaware$appmng$AppStartPolicyCfg$AppStartTargetType[appStartEnum.ordinal()] != 1) {
            return isAppStartSpecTypeComm(pkg, appStartEnum, status);
        }
        if (this.mDataMgr != null) {
            z = this.mDataMgr.isBlindAssistPkg(pkg);
        }
        return z;
    }

    private boolean isAppStartSpecTypeComm(String pkg, AppStartPolicyCfg.AppStartTargetType appType, AwareAppStartStatusCache status) {
        boolean z = false;
        if (!mEnabled) {
            return false;
        }
        switch (appType) {
            case TTS:
                return isTTSPkg(pkg);
            case IM:
                if (status.mAppType == -100) {
                    status.mAppType = getAppMngSpecType(pkg);
                }
                if (status.mAppType == 0) {
                    z = true;
                }
                return z;
            case CLOCK:
                return isAlarmApp(pkg, status);
            case PAY:
                return isPayPkg(pkg, status);
            case SHARE:
                return isSharePkg(pkg, status);
            case BUSINESS:
                if (status.mAppType == -100) {
                    status.mAppType = getAppMngSpecType(pkg);
                }
                if (status.mAppType == 11) {
                    z = true;
                }
                return z;
            case EMAIL:
                return isAppMngSpecTypeFreqTopN(pkg, 1, -1);
            case RCV_MONEY:
                if (status.mAppType == -100) {
                    status.mAppType = getAppMngSpecType(pkg);
                }
                if (status.mAppType == 34) {
                    z = true;
                }
                return z;
            case HABIT_IM:
                return isAllowStartAppByTopN(pkg, status, 0, 3, true);
            case MOSTFREQIM:
                return isAllowStartAppByTopN(pkg, status, 0, 3, false);
            default:
                return isAppStartSpecTypeCommExt(pkg, appType, status);
        }
    }

    private boolean isAppStartSpecTypeCommExt(String pkg, AppStartPolicyCfg.AppStartTargetType appType, AwareAppStartStatusCache status) {
        boolean z = false;
        if (AnonymousClass2.$SwitchMap$com$android$server$rms$iaware$appmng$AppStartPolicyCfg$AppStartTargetType[appType.ordinal()] != 12) {
            return false;
        }
        if (status.mAppType == -100) {
            status.mAppType = getAppMngSpecType(pkg);
        }
        if (status.mAppType == 5) {
            z = true;
        }
        return z;
    }

    private boolean isAppStartSpecStat(String pkg, int statType, AwareAppStartStatusCache status) {
        boolean z = false;
        if (!this.mAppStartEnabled) {
            return false;
        }
        if (DEBUG) {
            AwareLog.i(TAG, "isAppStartSpecStat " + pkg + ",status:" + statType);
        }
        if (statType < 0 || statType >= AppStartPolicyCfg.AppStartTargetStat.values().length) {
            return false;
        }
        AppStartPolicyCfg.AppStartTargetStat appStartEnum = AppStartPolicyCfg.AppStartTargetStat.values()[statType];
        switch (appStartEnum) {
            case WIDGET:
                if (this.mDataMgr != null) {
                    z = this.mDataMgr.isWidgetExistPkg(pkg);
                }
                return z;
            case WIDGETUPDATE:
                return isProtectUpdateWidget(pkg, status.mUid);
            case ALIVE:
                return !status.mIsAppStop;
            default:
                return isAppStartSpecStatComm(pkg, appStartEnum, status);
        }
    }

    private boolean isAppStartSpecStatComm(String pkg, AppStartPolicyCfg.AppStartTargetStat appStartEnum, AwareAppStartStatusCache status) {
        boolean z = false;
        if (!mEnabled) {
            return false;
        }
        switch (appStartEnum) {
            case MUSIC:
                return isAudioOutStatus(status.mUid);
            case RECORD:
                return isAudioInStatus(status.mUid);
            case GUIDE:
                return isGpsStatus(status.mUid);
            case UPDOWNLOAD:
                return isUpDownStatus(status.mUid);
            case HEALTH:
                return isSensorStatus(status.mUid);
            case FGACTIVITY:
                return status.mIsTargetFg;
            case WALLPAPER:
                if (status.mUid == getDefaultWallPaperUid()) {
                    z = true;
                }
                return z;
            case INPUTMETHOD:
                if (status.mUid == getDefaultInputMethodUid()) {
                    z = true;
                }
                return z;
            default:
                return false;
        }
    }

    public boolean isAppMngSpecRecent(String pkg, int isRecent, int recent) {
        if (!mEnabled || recent <= 0) {
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
        if (!mEnabled || recents == null) {
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
        if (!mEnabled || pkg == null) {
            return false;
        }
        AwareUserHabit habit = AwareUserHabit.getInstance();
        if (habit == null) {
            AwareLog.e(TAG, "AwareUserHabit is null");
            return false;
        } else if (topN == -1) {
            updateCacheIfNeed(habit, type);
            return this.mCachedTypeTopN.get(Integer.valueOf(type)).contains(pkg);
        } else {
            List<String> topNList = habit.getMostFreqAppByType(type, topN);
            if (topNList == null) {
                return false;
            }
            return topNList.contains(pkg);
        }
    }

    public boolean isAppMngSpecTypeFreqTopNInDay(String pkg, int type, int topN) {
        if (!mEnabled || pkg == null) {
            return false;
        }
        AwareUserHabit habit = AwareUserHabit.getInstance();
        if (habit == null) {
            AwareLog.e(TAG, "AwareUserHabit is null");
            return false;
        }
        List<String> topNList = habit.getMostFreqAppByTypeEx(type, topN);
        if (topNList == null) {
            return false;
        }
        return topNList.contains(pkg);
    }

    private boolean isTopHabitAppInStart(String pkg, int type, int topN, boolean isBoot) {
        if (!this.mAppStartEnabled || pkg == null || topN <= 0) {
            return false;
        }
        if (!isBoot) {
            return isAppMngSpecTypeFreqTopNInDay(pkg, type, topN);
        }
        AppTypeRecoManager appTypeRecoManager = AppTypeRecoManager.getInstance();
        if (appTypeRecoManager != null) {
            return appTypeRecoManager.isTopIM(pkg, topN);
        }
        AwareLog.w(TAG, "AppTypeRecoManager is null");
        return false;
    }

    private void updateCacheIfNeed(AwareUserHabit habit, int type) {
        long curTime = SystemClock.elapsedRealtime();
        ArrayMap<Integer, Set<String>> cachedTopNMap = new ArrayMap<>(this.mCachedTypeTopN);
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
            cachedTopNMap.put(Integer.valueOf(type), cachedTopN);
            lastUpdateTimeMap.put(Integer.valueOf(type), Long.valueOf(curTime));
            this.mLastUpdateTime = lastUpdateTimeMap;
            this.mCachedTypeTopN = cachedTopNMap;
        }
    }

    public int getAppMngSpecTypeFreqTopN(String pkg, LinkedHashMap<Integer, RuleNode> topNs) {
        if (!mEnabled || pkg == null || topNs == null) {
            return -1;
        }
        for (Integer topN : topNs.keySet()) {
            if (topN != null && topN.intValue() >= 0) {
                int topNInUserHabit = topN.intValue();
                if (topN.intValue() == 0) {
                    topNInUserHabit = -1;
                }
                AppTypeRecoManager atrm = AppTypeRecoManager.getInstance();
                if (isAppMngSpecTypeFreqTopN(pkg, atrm.convertType(atrm.getAppType(pkg)), topNInUserHabit)) {
                    return topN.intValue();
                }
            }
        }
        return -1;
    }

    public boolean isAppMngSpecHabbitTopN(String pkg, int isHabbitTopN, int topN) {
        if (!mEnabled || topN <= 0) {
            return false;
        }
        if (this.mHabbitTopN == null || this.mTempHTopN != topN || SystemClock.elapsedRealtimeNanos() - this.mUpdateTimeHabbitTopN > 10000000) {
            AwareUserHabit habit = AwareUserHabit.getInstance();
            if (habit == null) {
                return false;
            }
            this.mHabbitTopN = habit.getTopN(topN);
            this.mUpdateTimeHabbitTopN = SystemClock.elapsedRealtimeNanos();
            this.mTempHTopN = topN;
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

    public int getAppMngSpecHabbitTopN(String pkg, LinkedHashMap<Integer, RuleNode> topNs) {
        if (!mEnabled || topNs == null || pkg == null) {
            return -1;
        }
        for (Integer topN : topNs.keySet()) {
            if (topN != null && topN.intValue() > 0 && isAppMngSpecHabbitTopN(pkg, 1, topN.intValue())) {
                return topN.intValue();
            }
        }
        return -1;
    }

    public boolean isAppMngSpecType(String pkg, int appType) {
        if (mEnabled && appType == AppTypeRecoManager.getInstance().getAppType(pkg)) {
            return true;
        }
        return false;
    }

    public int getAppMngSpecType(String pkg) {
        if (!mEnabled || pkg == null) {
            return -1;
        }
        return AppTypeRecoManager.getInstance().getAppType(pkg);
    }

    public boolean isAppMngSpecStat(AwareProcessInfo info, int statType) {
        if (!mEnabled || info == null) {
            return false;
        }
        AppStatusUtils.Status[] values = AppStatusUtils.Status.values();
        if (values.length <= statType || statType < 0) {
            return false;
        }
        return AppStatusUtils.getInstance().checkAppStatus(values[statType], info);
    }

    public int getAppMngSpecStat(AwareProcessInfo info, LinkedHashMap<Integer, RuleNode> statTypes) {
        if (!mEnabled || statTypes == null || info == null) {
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
        if (!mEnabled || awareProcInfo == null || awareProcInfo.mProcInfo == null) {
            return false;
        }
        synchronized (this.mSmallSampleList) {
            Iterator it = awareProcInfo.mProcInfo.mPackageName.iterator();
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
        if (!mEnabled) {
            return false;
        }
        synchronized (this.mFrozenAppList) {
            if (this.mFrozenAppList.contains(Integer.valueOf(uid))) {
                return true;
            }
            return false;
        }
    }

    public boolean isAppBluetooth(int uid) {
        if (!mEnabled) {
            return false;
        }
        synchronized (this.mBluetoothAppList) {
            if (this.mBluetoothAppList.contains(Integer.valueOf(uid))) {
                return true;
            }
            return false;
        }
    }

    public boolean isAudioOutInstant(int uid) {
        if (!mEnabled) {
            return false;
        }
        synchronized (this.mAudioOutInstant) {
            if (this.mAudioOutInstant.contains(Integer.valueOf(uid))) {
                return true;
            }
            return false;
        }
    }

    public boolean isToastWindow(int pid) {
        boolean z = false;
        if (!mEnabled) {
            return false;
        }
        synchronized (this.mToasts) {
            AwareProcessWindowInfo toastInfo = this.mToasts.get(Integer.valueOf(pid));
            if (toastInfo != null && !toastInfo.isEvil()) {
                z = true;
            }
        }
        return z;
    }

    private void registerContentObserver(Context context, ContentObserver observer) {
        if (!this.mIsObsvInit.get()) {
            context.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("default_input_method"), false, observer, -1);
            context.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("enabled_accessibility_services"), false, observer, -1);
            context.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("tts_default_synth"), false, observer, -1);
            context.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(VALUE_BT_BLE_CONNECT_APPS), false, observer, -1);
            context.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(VALUE_BT_LAST_BLE_DISCONNECT), false, observer, -1);
            context.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("sms_default_application"), false, observer, -1);
            this.mIsObsvInit.set(true);
        }
    }

    private void unregisterContentObserver(Context context, ContentObserver observer) {
        if (this.mIsObsvInit.get()) {
            context.getContentResolver().unregisterContentObserver(observer);
            this.mIsObsvInit.set(false);
        }
    }

    public void dumpInputMethod(PrintWriter pw) {
        if (pw != null) {
            if (!mEnabled) {
                pw.println("AwareIntelligentRecg feature disabled.");
                return;
            }
            pw.println("[dump default InputMethod]");
            pw.println(this.mDefaultInputMethod + ",uid:" + this.mDefaultInputMethodUid);
        }
    }

    public void dumpWallpaper(PrintWriter pw) {
        if (pw != null) {
            if (!mEnabled) {
                pw.println("AwareIntelligentRecg feature disabled.");
                return;
            }
            pw.println("[dump Wallpaper]");
            pw.println(this.mDefaultWallPaper + ",uid:" + this.mDefaultWallPaperUid);
        }
    }

    public void dumpAccessibility(PrintWriter pw) {
        if (pw != null) {
            if (!mEnabled) {
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
            if (!mEnabled) {
                pw.println("AwareIntelligentRecg feature disabled.");
                return;
            }
            pw.println("[dump TTS]");
            synchronized (this.mTTSPkg) {
                for (String pkg : this.mTTSPkg) {
                    pw.println(pkg);
                }
            }
        }
    }

    public void dumpPushSDK(PrintWriter pw) {
        if (pw != null) {
            if (!mEnabled) {
                pw.println("AwareIntelligentRecg feature disabled.");
                return;
            }
            pw.println("AwareIntelligentRecg dumpPushSDK start");
            synchronized (this.mPushSDKClsList) {
                pw.println("recg push sdk cls:" + this.mPushSDKClsList);
            }
            synchronized (this.mGoodPushSDKClsList) {
                pw.println("good push sdk cls:" + this.mGoodPushSDKClsList);
            }
            synchronized (this.mAppStartInfoList) {
                Iterator<AwareAppStartInfo> it = this.mAppStartInfoList.iterator();
                while (it.hasNext()) {
                    pw.println("start record:" + it.next().toString());
                }
            }
            ComponentRecoManager.getInstance().dumpBadComponent(pw);
            pw.println("AwareIntelligentRecg dumpPushSDK end");
        }
    }

    public void dumpSms(PrintWriter pw) {
        if (pw != null) {
            if (!mEnabled) {
                pw.println("AwareIntelligentRecg feature disabled.");
                return;
            }
            pw.println("[dump default sms package]");
            pw.println(this.mDefaultSms);
        }
    }

    private void updateGmsCallerApp(Context context) {
        ArraySet<Integer> gmsUid = new ArraySet<>();
        Iterator<String> it = this.mGmsCallerAppPkg.iterator();
        while (it.hasNext()) {
            int uid = getUIDByPackageName(context, it.next());
            if (uid != -1) {
                gmsUid.add(Integer.valueOf(uid));
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
        if (this.mGmsCallerAppPkg.contains(pkg) && this.mMtmService != null) {
            updateGmsCallerApp(this.mMtmService.context());
        }
    }

    public void appStartEnable(AppStartupDataMgr dataMgr, Context context) {
        this.mIsAbroadArea = AwareDefaultConfigList.isAbroadArea();
        this.mDataMgr = dataMgr;
        updateGmsCallerAppInit(context);
        initAppMngCustPropConfig();
        initGmsAppConfig();
        initMemoryInfo();
        initCommonActTopIM();
        this.mAppStartEnabled = true;
    }

    public void appStartDisable() {
        this.mAppStartEnabled = false;
    }

    public void dumpToastWindow(PrintWriter pw) {
        if (pw != null) {
            if (!mEnabled) {
                pw.println("AwareIntelligentRecg feature disabled.");
                return;
            }
            ArraySet<Integer> toasts = new ArraySet<>();
            ArraySet<Integer> toastsEvil = new ArraySet<>();
            getToastWindows(toasts, toastsEvil);
            pw.println("");
            pw.println("[ToastList]:" + toasts);
            pw.println("[EvilToastList]:" + toastsEvil);
            synchronized (this.mToasts) {
                for (Map.Entry<Integer, AwareProcessWindowInfo> toast : this.mToasts.entrySet()) {
                    AwareProcessWindowInfo toastInfo = toast.getValue();
                    pw.println("[Toast pid ]:" + toast.getKey() + " pkg:" + toastInfo.mPkg + " isEvil:" + toastInfo.isEvil());
                }
            }
        }
    }

    public void dumpIsVisibleWindow(PrintWriter pw, int userid, String pkg, int type) {
        if (pw != null) {
            if (!mEnabled) {
                pw.println("AwareIntelligentRecg feature disabled.");
                return;
            }
            pw.println("");
            boolean result = HwSysResManager.getInstance().isVisibleWindow(userid, pkg, type);
            pw.println("[dumpIsVisibleWindow ]:" + result);
        }
    }

    public void dumpDefaultTts(PrintWriter pw) {
        if (pw != null) {
            if (!mEnabled) {
                pw.println("AwareIntelligentRecg feature disabled.");
                return;
            }
            pw.println("[dump Default TTS]");
            pw.println(this.mDefaultTTS);
        }
    }

    public void dumpDefaultAppType(PrintWriter pw) {
        if (pw != null) {
            if (!mEnabled) {
                pw.println("AwareIntelligentRecg feature disabled.");
                return;
            }
            pw.println("[dump Pay Pkg]");
            pw.println(this.mPayPkgList == null ? HwAPPQoEUtils.INVALID_STRING_VALUE : this.mPayPkgList);
            pw.println("[dump Share Pkg]");
            pw.println(this.mSharePkgList == null ? HwAPPQoEUtils.INVALID_STRING_VALUE : this.mSharePkgList);
            pw.println("[dump ActTopIMCN]");
            pw.println(this.mActTopIMCN);
        }
    }

    public void dumpFrozen(PrintWriter pw, int uid) {
        if (pw != null) {
            if (!mEnabled) {
                pw.println("AwareIntelligentRecg feature disabled.");
                return;
            }
            pw.println("AwareIntelligentRecg dumpFrozen start");
            synchronized (this.mFrozenAppList) {
                if (uid == 0) {
                    try {
                        for (Integer AppUid : this.mFrozenAppList) {
                            pw.println("frozen app:" + InnerUtils.getPackageNameByUid(AppUid.intValue()) + ",uid:" + AppUid);
                        }
                    } catch (Throwable th) {
                        throw th;
                    }
                } else {
                    pw.println("uid:" + uid + ",frozen:" + isAppFrozen(uid));
                }
            }
        }
    }

    public void dumpBluetooth(PrintWriter pw, int uid) {
        if (pw != null) {
            if (!mEnabled) {
                pw.println("AwareIntelligentRecg feature disabled.");
                return;
            }
            pw.println("AwareIntelligentRecg dumpBluetooth start");
            synchronized (this.mBluetoothAppList) {
                if (uid == 0) {
                    try {
                        for (Integer AppUid : this.mBluetoothAppList) {
                            pw.println("bluetooth app:" + InnerUtils.getPackageNameByUid(AppUid.intValue()) + ",uid:" + AppUid);
                        }
                    } catch (Throwable th) {
                        throw th;
                    }
                } else {
                    pw.println("uid:" + uid + ",bluetooth:" + isAppBluetooth(uid));
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void resolvePkgsName(String pkgs, int eventType) {
        if (eventType == 1) {
            if (pkgs != null) {
                this.mHandler.removeMessages(17);
                String[] pkglist = pkgs.split("#");
                synchronized (this.mNotCleanPkgList) {
                    for (String add : pkglist) {
                        this.mNotCleanPkgList.add(add);
                    }
                }
            }
        } else if (eventType == 0) {
            this.mHandler.sendEmptyMessageDelayed(17, (long) this.mNotCleanDuration);
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
            if (!mEnabled) {
                pw.println("AwareIntelligentRecg feature disabled.");
                return;
            }
            pw.println("AwareIntelligentRecg dumpNotClean start");
            StringBuffer sb = new StringBuffer();
            synchronized (this.mNotCleanPkgList) {
                for (String pkgName : this.mNotCleanPkgList) {
                    sb.append(pkgName);
                    sb.append(" ");
                }
            }
            pw.println(sb.toString());
        }
    }

    public void dumpKbgApp(PrintWriter pw) {
        if (pw != null) {
            if (!mEnabled) {
                pw.println("AwareIntelligentRecg feature disabled.");
                return;
            }
            pw.println("AwareIntelligentRecg dumpKbgApp start");
            synchronized (this.mStatusAudioIn) {
                Iterator<Integer> it = this.mStatusAudioIn.iterator();
                while (it.hasNext()) {
                    pw.println("mStatusAudioIn app:" + it.next());
                }
            }
            synchronized (this.mStatusAudioOut) {
                Iterator<Integer> it2 = this.mStatusAudioOut.iterator();
                while (it2.hasNext()) {
                    pw.println("mStatusAudioOut app:" + it2.next());
                }
            }
            synchronized (this.mStatusGps) {
                Iterator<Integer> it3 = this.mStatusGps.iterator();
                while (it3.hasNext()) {
                    pw.println("mStatusGps app:" + it3.next());
                }
            }
            synchronized (this.mStatusUpDown) {
                Iterator<Integer> it4 = this.mStatusUpDown.iterator();
                while (it4.hasNext()) {
                    pw.println("mStatusUpDown app:" + it4.next());
                }
            }
            synchronized (this.mStatusSensor) {
                Iterator<Integer> it5 = this.mStatusSensor.iterator();
                while (it5.hasNext()) {
                    pw.println("mStatusSensor app:" + it5.next());
                }
            }
            synchronized (this.mStatusUpDownElapse) {
                long timeCur = SystemClock.elapsedRealtime();
                for (Map.Entry<Integer, Long> m : this.mStatusUpDownElapse.entrySet()) {
                    int uid = m.getKey().intValue();
                    long time = m.getValue().longValue();
                    pw.println("mStatusUpDown History:" + uid + "," + ((timeCur - time) / 1000));
                }
            }
            pw.println("mIsScreenOnPm:" + this.mIsScreenOnPm);
            pw.println("mAppStartAreaCfg:" + this.mAppStartAreaCfg);
            pw.println("mDeviceLevel:" + this.mDeviceLevel);
        }
    }

    public void dumpAlarms(PrintWriter pw) {
        if (pw != null) {
            if (!mEnabled) {
                pw.println("AwareIntelligentRecg feature disabled.");
                return;
            }
            pw.println("AwareIntelligentRecg dumpAlarms start");
            synchronized (this.mAlarmMap) {
                for (Integer uid : this.mAlarmMap.keySet()) {
                    ArrayMap<String, AlarmInfo> alarms = this.mAlarmMap.get(uid);
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
            if (!mEnabled) {
                pw.println("AwareIntelligentRecg feature disabled.");
                return;
            }
            pw.println("AwareIntelligentRecg dumpAlarmActions start");
            synchronized (this.mAlarmCmps) {
                for (Map.Entry<String, CmpTypeInfo> value : this.mAlarmCmps.entrySet()) {
                    CmpTypeInfo info = (CmpTypeInfo) value.getValue();
                    StringBuilder sb = new StringBuilder();
                    sb.append("AwareIntelligentRecg alarm action:");
                    sb.append(info != null ? info.toString() : "");
                    pw.println(sb.toString());
                }
            }
            pw.println("Alarm White Pkg:");
            pw.println(this.mAlarmPkgList == null ? HwAPPQoEUtils.INVALID_STRING_VALUE : this.mAlarmPkgList);
            pw.println("UnRemind App Types:");
            pw.println(this.mUnRemindAppTypeList == null ? HwAPPQoEUtils.INVALID_STRING_VALUE : this.mUnRemindAppTypeList);
        }
    }

    public void dumpHwStopList(PrintWriter pw) {
        if (pw != null) {
            if (!mEnabled) {
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
            if (!mEnabled) {
                pw.println("AwareIntelligentRecg feature disabled.");
                return;
            }
            pw.println("AwareIntelligentRecg dumpScreenRecord start");
            synchronized (this.mScreenRecordAppList) {
                for (Integer uid : this.mScreenRecordAppList) {
                    pw.println(" screenRecord uid:" + uid + " and pkgName is " + InnerUtils.getPackageNameByUid(uid.intValue()));
                }
            }
        }
    }

    public void dumpCameraRecording(PrintWriter pw) {
        if (pw != null) {
            if (!mEnabled) {
                pw.println("AwareIntelligentRecg feature disabled.");
                return;
            }
            pw.println("AwareIntelligentRecg dumpCameraRecording start");
            synchronized (this.mCameraUseAppList) {
                for (Integer uid : this.mCameraUseAppList) {
                    if (!AwareAppAssociate.getInstance().isForeGroundApp(uid.intValue())) {
                        if (AwareAppAssociate.getInstance().hasWindow(uid.intValue())) {
                            pw.println("  cameraRecord uid:" + uid + " PKG is " + InnerUtils.getPackageNameByUid(uid.intValue()));
                        }
                    }
                }
            }
        }
    }

    public void dumpGmsCallerList(PrintWriter pw) {
        if (pw != null) {
            if (!mEnabled) {
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
            if (!mEnabled) {
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
        mEnabled = true;
    }

    public static void commDisable() {
        mEnabled = false;
        getInstance().deInitialize();
    }

    public static void enableDebug() {
        DEBUG = true;
    }

    public static void disableDebug() {
        DEBUG = false;
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
        if (callerUid != this.mBluetoothUid || !BTMEDIABROWSER_ACTION.equals(action)) {
            return false;
        }
        return true;
    }

    public boolean isNotifyListenerCaller(int callerUid, String action, ProcessRecord callerApp) {
        boolean z = false;
        if (callerApp == null) {
            return false;
        }
        if (callerUid == 1000 && SYSTEM_PROCESS_NAME.equals(callerApp.processName) && NOTIFY_LISTENER_ACTION.equals(action)) {
            z = true;
        }
        return z;
    }

    public boolean isGmsCaller(int callerUid) {
        if (!this.mAppStartEnabled) {
            return false;
        }
        return this.mGmsCallerAppUid.contains(Integer.valueOf(UserHandle.getAppId(callerUid)));
    }

    @SuppressLint({"PreferForInArrayList"})
    public boolean isAchScreenChangedNum(AwareProcessInfo awareProcInfo) {
        if (!mEnabled || awareProcInfo == null || awareProcInfo.mProcInfo == null) {
            return false;
        }
        Iterator it = awareProcInfo.mProcInfo.mPackageName.iterator();
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
            synchronized (this.mAppChangeToBGTime) {
                this.mAppChangeToBGTime.put(pkg, Long.valueOf(currTime));
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0027, code lost:
        r6 = r9.mAppChangeToBGTime;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0029, code lost:
        monitor-enter(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0030, code lost:
        if (r9.mAppChangeToBGTime.containsKey(r10) != false) goto L_0x0034;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0032, code lost:
        monitor-exit(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0033, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0034, code lost:
        r3 = r9.mAppChangeToBGTime.get(r10).longValue();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0041, code lost:
        monitor-exit(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0044, code lost:
        if (r1 >= r3) goto L_0x0047;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0046, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0048, code lost:
        return true;
     */
    private boolean isScreenChangedMeetCondition(String pkg) {
        if (pkg == null) {
            return false;
        }
        synchronized (this.mScreenChangedTime) {
            if (AppMngConfig.getScreenChangedThreshold() > this.mScreenChangedTime.size()) {
                return false;
            }
            long screenChangedTime = this.mScreenChangedTime.getFirst().longValue();
        }
    }

    public boolean isCurrentUser(int checkUid, int currentUserId) {
        int userId = UserHandle.getUserId(checkUid);
        boolean isCloned = false;
        if (this.mMtmService != null) {
            UserManager usm = UserManager.get(this.mMtmService.context());
            if (usm != null) {
                UserInfo info = usm.getUserInfo(userId);
                isCloned = info != null ? info.isClonedProfile() : false;
            }
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
        if (mEnabled) {
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
        if (this.mMtmService == null) {
            return false;
        }
        UserManager usm = UserManager.get(this.mMtmService.context());
        if (usm == null) {
            return false;
        }
        UserInfo info = usm.getUserInfo(userId);
        if (info == null) {
            return false;
        }
        return info.isClonedProfile();
    }

    public void setHwStopFlag(int userId, String pkg, boolean hwStop) {
        if (mEnabled && pkg != null && !"".equals(pkg)) {
            String userIdPkg = pkg + "#" + userId;
            synchronized (this.mHwStopUserIdPkg) {
                if (hwStop) {
                    try {
                        this.mHwStopUserIdPkg.add(userIdPkg);
                    } catch (Throwable th) {
                        throw th;
                    }
                } else {
                    this.mHwStopUserIdPkg.remove(userIdPkg);
                }
            }
        }
    }

    public boolean isPkgHasHwStopFlag(int userId, String pkg) {
        boolean isHwStopPkg;
        if (!mEnabled || pkg == null || "".equals(pkg)) {
            return false;
        }
        String userIdPkg = pkg + "#" + userId;
        synchronized (this.mHwStopUserIdPkg) {
            isHwStopPkg = this.mHwStopUserIdPkg.contains(userIdPkg);
        }
        if (DEBUG && isHwStopPkg) {
            AwareLog.i(TAG, "pkg:" + pkg + " userId:" + userId + " is hwStop pkg.");
        }
        return isHwStopPkg;
    }

    public void updateScreenOnFromPm(boolean wakenessChange) {
        if (mEnabled && wakenessChange) {
            sendScreenOnFromPmMsg(false);
        }
    }

    private void sendScreenOnFromPmMsg(boolean delay) {
        if (this.mHandler != null) {
            Message msg = this.mHandler.obtainMessage();
            msg.what = 18;
            if (delay) {
                this.mHandler.sendMessageDelayed(msg, 1000);
            } else {
                this.mHandler.sendMessage(msg);
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleWakenessChangeHandle() {
        if (this.mMtmService != null) {
            PowerManager pm = (PowerManager) this.mMtmService.context().getSystemService("power");
            if (pm != null) {
                this.mIsScreenOnPm = pm.isScreenOn();
            }
        }
    }

    private void initAppMngCustPropConfig() {
        if (!this.mAppMngPropCfgInit) {
            this.mAppMngPropCfgInit = true;
            String prop = SystemProperties.get("ro.config.iaware_appmngconfigs");
            if (prop != null && !prop.equals("")) {
                String[] strs = prop.split(",");
                if (strs.length >= this.APPMNGPROP_MAXINDEX) {
                    try {
                        this.mAppStartAreaCfg = Integer.parseInt(strs[this.APPMNGPROP_APPSTARTINDEX]);
                    } catch (NumberFormatException e) {
                        AwareLog.i(TAG, "initAppMngCustPropConfig error");
                    }
                }
            }
        }
    }

    public Bundle getTypeTopN(int[] appTypes) {
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
        ArrayList<String> topNList = new ArrayList<>();
        ArrayList<Integer> typeList = new ArrayList<>();
        int i = 0;
        int size = iArr.length;
        while (i < size) {
            int type = iArr[i];
            if (type >= 0) {
                List<String> topN = habit.getMostFreqAppByType(AppTypeRecoManager.getInstance().convertType(type), SPEC_VALUE_DEFAULT_MAX_TOPN);
                if (topN != null) {
                    long now = SystemClock.elapsedRealtime();
                    int j = 0;
                    int topNSize = topN.size();
                    while (j < topNSize) {
                        String name = topN.get(j);
                        Long lastUseTime = lruCache.get(name);
                        if (lastUseTime != null && now - lastUseTime.longValue() < 172800000) {
                            topNList.add(name);
                            typeList.add(Integer.valueOf(type));
                        }
                        j++;
                        int[] iArr2 = appTypes;
                    }
                }
            }
            i++;
            iArr = appTypes;
        }
        result.putIntegerArrayList(HwSecDiagnoseConstant.ANTIMAL_APK_TYPE, typeList);
        result.putStringArrayList("pkg", topNList);
        return result;
    }

    private AwareBroadcastPolicy getIawareBrPolicy() {
        if (this.mIawareBrPolicy == null && MultiTaskManagerService.self() != null) {
            this.mIawareBrPolicy = MultiTaskManagerService.self().getIawareBrPolicy();
        }
        return this.mIawareBrPolicy;
    }

    public void updateBGCheckExcludedInfo(ArraySet<String> excludedPkg) {
        this.mBGCheckExcludedPkg = excludedPkg;
        ArrayList<String> excludedAction = DecisionMaker.getInstance().getRawConfig(AppMngConstant.AppMngFeature.APP_START.getDesc(), BACKGROUND_CHECK_EXCLUDED_ACTION);
        if (excludedAction != null) {
            this.mBGCheckExcludedAction = new ArraySet<>(excludedAction);
        }
    }

    public boolean isExcludedInBGCheck(String pkg, String action) {
        boolean result = false;
        if (action != null && this.mBGCheckExcludedAction.contains(action)) {
            result = true;
        }
        if (pkg == null || !this.mBGCheckExcludedPkg.contains(pkg)) {
            return result;
        }
        return true;
    }

    public void dumpBGCheckExcludeInfo(PrintWriter pw) {
        if (pw != null) {
            if (!mEnabled) {
                pw.println("AwareIntelligentRecg feature disabled.");
                return;
            }
            pw.println("BGCheckExcludedAction :" + this.mBGCheckExcludedAction);
            pw.println("BGCheckExcludedPkg :" + this.mBGCheckExcludedPkg);
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
            ArrayList<String> delaytime = DecisionMaker.getInstance().getRawConfig(AppMngConstant.AppMngFeature.APP_START.getDesc(), "google_delaytime");
            if (delaytime != null && delaytime.size() == 1) {
                try {
                    this.mGoogleConnDealyTime = Long.parseLong(delaytime.get(0)) * 1000;
                } catch (NumberFormatException e) {
                    AwareLog.i(TAG, "initGoogledelayTime error");
                }
            }
            initGoogleConnTime();
            removeAppStartFeatureGMSList();
        }
    }

    private void initMemoryInfo() {
        MemInfoReader minfo = new MemInfoReader();
        minfo.readMemInfo();
        this.mDeviceTotalMemory = minfo.getTotalSize() / MemoryConstant.MB_SIZE;
        if (this.mDeviceTotalMemory == -1) {
            AwareLog.e(TAG, "read device memory faile");
        }
        this.mDeviceMemoryOfGIGA = calMemorySizeOfGiga(this.mDeviceTotalMemory);
        AwareLog.i(TAG, "Current Device Total Memory is " + this.mDeviceTotalMemory + " and about " + this.mDeviceMemoryOfGIGA + "G");
    }

    private void initGoogleConnTime() {
        String prop = SystemProperties.get(PROPERTIES_GOOELE_CONNECTION);
        if (prop == null || prop.equals("")) {
            this.mGoogleConnStat = false;
            this.mGoogleDisConnTime = 0;
            this.mGoogleConnStatDecay = false;
            return;
        }
        String[] strs = prop.split(",");
        if (strs.length < this.GOOGLECONNPROP_MAXINDEX) {
            this.mGoogleConnStat = false;
            this.mGoogleDisConnTime = 0;
            this.mGoogleConnStatDecay = false;
            return;
        }
        try {
            this.mGoogleConnStat = Integer.parseInt(strs[this.GOOGLECONNPROP_STARTINDEX + 1]) == 1;
            this.mGoogleDisConnTime = Long.parseLong(strs[this.GOOGLECONNPROP_STARTINDEX]) * 1000;
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
        if (getIawareBrPolicy() != null) {
            this.mIawareBrPolicy.reportGoogleConn(conn);
        }
        if (!this.mIsAbroadArea) {
            if (!conn) {
                long now = System.currentTimeMillis();
                if (this.mGoogleConnStat) {
                    this.mGoogleDisConnTime = now;
                } else if (now - this.mGoogleDisConnTime >= this.mGoogleConnDealyTime && this.mGoogleConnStatDecay) {
                    this.mGoogleConnStatDecay = false;
                    removeFeatureGMSList();
                }
            } else if (!this.mGoogleConnStatDecay) {
                this.mGoogleConnStatDecay = true;
                addFeatureGMSList();
            }
            if (this.mGoogleConnStat != conn) {
                this.mGoogleConnStat = conn;
                restoreGoogleConnTime();
            }
        }
    }

    private void addFeatureGMSList() {
        DecisionMaker.getInstance().addFeatureList(AppMngConstant.AppMngFeature.APP_CLEAN, this.mGMSAppCleanPolicyList);
        DecisionMaker.getInstance().addFeatureList(AppMngConstant.AppMngFeature.APP_START, this.mGMSAppStartPolicyList);
    }

    private void removeFeatureGMSList() {
        removeAppStartFeatureGMSList();
        removeAppCleanFeatureGMSList();
    }

    private void restoreGoogleConnTime() {
        String conn_info = this.mGoogleConnStat ? "1" : "0";
        SystemProperties.set(PROPERTIES_GOOELE_CONNECTION, String.valueOf(this.mGoogleDisConnTime / 1000) + "," + conn_info);
    }

    public void removeAppStartFeatureGMSList() {
        if (this.mIsInitGoogleConfig && !this.mIsAbroadArea && !this.mGoogleConnStatDecay) {
            this.mGMSAppStartPolicyList = DecisionMaker.getInstance().removeFeatureList(AppMngConstant.AppMngFeature.APP_START, this.mGmsAppPkg);
        }
    }

    public void removeAppCleanFeatureGMSList() {
        if (this.mIsInitGoogleConfig && !this.mIsAbroadArea && !this.mGoogleConnStatDecay) {
            this.mGMSAppCleanPolicyList = DecisionMaker.getInstance().removeFeatureList(AppMngConstant.AppMngFeature.APP_CLEAN, this.mGmsAppPkg);
        }
    }

    public void dumpGmsAppList(PrintWriter pw) {
        if (pw != null) {
            if (!mEnabled) {
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
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00e2, code lost:
        return;
     */
    public void refreshAlivedApps(int eventType, String pkgAndTime, int userId) {
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
                ArrayMap<String, Long> appMap = this.mRegKeepAlivePkgs.get(Integer.valueOf(userId));
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
                    this.mRegKeepAlivePkgs.put(Integer.valueOf(userId), appMap);
                } else if (eventType == 2) {
                    if (appMap != null) {
                        appMap.remove(splitRet[0]);
                    }
                    if (appMap == null || appMap.isEmpty()) {
                        this.mRegKeepAlivePkgs.remove(Integer.valueOf(userId));
                    }
                }
            }
        }
    }

    private long strToLong(String str) {
        long elapsedRealtime = 0;
        if (str == null || str.trim().isEmpty()) {
            return 0;
        }
        try {
            elapsedRealtime = Long.parseLong(str);
        } catch (NumberFormatException e) {
            AwareLog.i(TAG, "number format exception! str is: " + str);
        }
        return elapsedRealtime;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:31:0x009a, code lost:
        return r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x009c, code lost:
        return false;
     */
    public boolean isCurUserKeepALive(String pkg, int uid) {
        boolean ret;
        boolean ret2;
        if (pkg == null || pkg.isEmpty()) {
            return false;
        }
        int userId = UserHandle.getUserId(uid);
        if (userId != 0) {
            return false;
        }
        synchronized (this.mRegKeepAlivePkgs) {
            ArrayMap<String, Long> appMap = this.mRegKeepAlivePkgs.get(Integer.valueOf(userId));
            if (appMap != null) {
                if (!appMap.isEmpty()) {
                    if (appMap.containsKey(pkg)) {
                        long keptTime = appMap.get(pkg).longValue() - SystemClock.elapsedRealtime();
                        AwareLog.d(TAG, pkg + " still need kept in iaware for " + keptTime + " ms");
                        if (keptTime <= 0) {
                            appMap.remove(pkg);
                            ret2 = false;
                        } else if (keptTime < 1800000) {
                            ret2 = true;
                        } else if (isKeptAliveAppByPg(pkg, uid)) {
                            ret2 = true;
                        } else {
                            appMap.remove(pkg);
                            ret2 = false;
                            ret = ret2;
                        }
                        ret = ret2;
                    } else {
                        ret = false;
                        AwareLog.d(TAG, pkg + " is not in iaware alive record.");
                    }
                }
            }
        }
    }

    private boolean isKeptAliveAppByPg(String pkg, int uid) {
        if (this.mPGSdk == null || this.mMtmService == null) {
            return false;
        }
        Context context = this.mMtmService.context();
        if (context == null) {
            return false;
        }
        try {
            return this.mPGSdk.isKeptAliveApp(context, pkg, uid);
        } catch (RemoteException e) {
            if (DEBUG) {
                AwareLog.w(TAG, "call isKeptAliveApp happened RemoteException.");
            }
            return false;
        }
    }

    public void dumpKeepAlivePkgs(PrintWriter pw) {
        if (pw != null) {
            if (!mEnabled) {
                pw.println("AwareIntelligentRecg feature disabled.");
                return;
            }
            pw.println("AwareIntelligentRecg dumpKeepAlivePkgs start");
            StringBuffer sb = new StringBuffer();
            synchronized (this.mRegKeepAlivePkgs) {
                for (Map.Entry<Integer, ArrayMap<String, Long>> userApps : this.mRegKeepAlivePkgs.entrySet()) {
                    if (userApps != null) {
                        ArrayMap<String, Long> pkgMap = userApps.getValue();
                        if (pkgMap != null) {
                            if (!pkgMap.isEmpty()) {
                                long current = SystemClock.elapsedRealtime();
                                sb.append("====userId:");
                                sb.append(userApps.getKey());
                                sb.append(", cached size:");
                                sb.append(pkgMap.size());
                                sb.append(", elapsedRealtime:");
                                sb.append(current);
                                sb.append("====");
                                for (String pkg : pkgMap.keySet()) {
                                    sb.append("\n{");
                                    sb.append(pkg);
                                    sb.append(" ");
                                    sb.append(pkgMap.get(pkg));
                                    sb.append(" ");
                                    sb.append(pkgMap.get(pkg).longValue() - current);
                                    sb.append("}");
                                }
                            }
                        }
                    }
                }
            }
            sb.append("\n");
            pw.println(sb.toString());
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
        return this.mDeviceMemoryOfGIGA;
    }

    private void initCommonActTopIM() {
        String actTopIMCN = DecisionMaker.getInstance().getCommonCfg(AppMngConstant.AppMngFeature.COMMON.getDesc(), ACT_TOP_IM_CN);
        if (actTopIMCN != null) {
            this.mActTopIMCN = actTopIMCN;
            if (!this.mActTopIMCN.equals(SystemProperties.get(TOP_IM_CN_PROP, UNKNOWN_PKG))) {
                SystemProperties.set(TOP_IM_CN_PROP, this.mActTopIMCN);
            }
        }
    }

    public String getActTopIMCN() {
        if (mEnabled || this.mAppStartEnabled) {
            return this.mActTopIMCN;
        }
        return UNKNOWN_PKG;
    }

    private int getAppTypeActTop(String pkgName, int area) {
        int atti = AppTypeRecoManager.getInstance().getAppAttribute(pkgName);
        if (atti == -1) {
            return 0;
        }
        int actTop = 0;
        if (area == 0) {
            actTop = (atti & ConstS32.HIGH_BITS_AL_MARK) >> 8;
        } else if (area == 1) {
            actTop = (61440 & atti) >> 12;
        }
        return actTop;
    }

    public boolean isTopImAppBase(String pkg) {
        if (!mEnabled || getAppMngSpecType(pkg) != 0) {
            return false;
        }
        boolean z = true;
        int actTop = getAppTypeActTop(pkg, 1);
        if (actTop == 0) {
            return false;
        }
        if (actTop > 3) {
            z = false;
        }
        return z;
    }

    public boolean isBluetoothConnect(int pid) {
        if (!mEnabled) {
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

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x002f, code lost:
        return false;
     */
    public boolean isBluetoothLast(int pid, long time) {
        if (!mEnabled) {
            return false;
        }
        synchronized (this.mBluetoothLock) {
            if (!this.mBtoothConnectList.isEmpty()) {
                return false;
            }
            if (-1 != this.mBtoothLastPid) {
                if (this.mBtoothLastPid == pid) {
                    if (SystemClock.elapsedRealtime() - this.mBtoothLastTime > time) {
                        return false;
                    }
                    return true;
                }
            }
        }
    }

    public void registerFgChangedCallback(IAwareFgChangedCallback callback) {
        if (callback != null) {
            synchronized (this.mFgCallbacks) {
                if (!this.mFgCallbacks.contains(callback)) {
                    this.mFgCallbacks.add(callback);
                }
            }
        }
    }

    public void unregisterFgChangedCallback(IAwareFgChangedCallback callback) {
        if (callback != null) {
            synchronized (this.mFgCallbacks) {
                if (this.mFgCallbacks.contains(callback)) {
                    this.mFgCallbacks.remove(callback);
                }
            }
        }
    }

    private void notifyFgChanged(ArrayList<PidInfo> pidInfos, boolean isAdd) {
        synchronized (this.mFgCallbacks) {
            Iterator<IAwareFgChangedCallback> it = this.mFgCallbacks.iterator();
            while (it.hasNext()) {
                it.next().onForegroundActivitiesChanged(pidInfos, isAdd);
            }
        }
    }

    public void onFgPidInfosInit(ArrayMap<Integer, Integer> forePids) {
        if (this.mHandler != null) {
            Message msg = this.mHandler.obtainMessage();
            msg.what = 21;
            msg.obj = forePids;
            this.mHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    public void initFgPidInfo(ArrayMap<Integer, Integer> forePids) {
        AwareLog.d(TAG, " initFgPidInfo:" + forePids);
        for (Map.Entry<Integer, Integer> entry : forePids.entrySet()) {
            if (entry != null) {
                updateFgPidInfo(entry.getKey().intValue(), entry.getValue().intValue(), true);
            }
        }
    }

    public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
        sendUpdateFgPidInfo(pid, uid, foregroundActivities);
    }

    public void onProcessDied(int pid, int uid) {
        sendUpdateFgPidInfo(pid, uid, false);
    }

    private void sendUpdateFgPidInfo(int pid, int uid, boolean isAdd) {
        if (this.mHandler != null) {
            Message msg = this.mHandler.obtainMessage();
            if (isAdd) {
                msg.what = 22;
            } else {
                msg.what = 23;
            }
            msg.arg1 = pid;
            msg.arg2 = uid;
            this.mHandler.sendMessage(msg);
        }
    }

    private ArrayList<String> getPkgListByPid(int pid) {
        ProcessInfo processInfo = ProcessInfoCollector.getInstance().getProcessInfo(pid);
        if (processInfo == null || processInfo.mPackageName == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(processInfo.mPackageName);
    }

    /* access modifiers changed from: private */
    public void updateFgPidInfo(int pid, int uid, boolean isAdd) {
        boolean needNotifyFgChanged = false;
        ArrayList<PidInfo> pidInfos = new ArrayList<>();
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
                synchronized (this.mFgPidInfos) {
                    this.mFgPidInfos.put(Integer.valueOf(pid), new PidInfo(pid, uid, pkgAndType));
                    needNotifyFgChanged = true;
                }
            }
        } else {
            synchronized (this.mFgPidInfos) {
                if (this.mFgPidInfos.containsKey(Integer.valueOf(pid))) {
                    this.mFgPidInfos.remove(Integer.valueOf(pid));
                    needNotifyFgChanged = true;
                }
            }
        }
        if (needNotifyFgChanged) {
            synchronized (this.mFgPidInfos) {
                for (PidInfo pidInfo : this.mFgPidInfos.values()) {
                    pidInfos.add(new PidInfo(pidInfo));
                }
            }
            notifyFgChanged(pidInfos, isAdd);
        }
    }

    public void dumpFgInfos(PrintWriter pw) {
        if (pw != null) {
            synchronized (this.mFgPidInfos) {
                pw.println("foreground infos : " + this.mFgPidInfos);
            }
        }
    }
}
