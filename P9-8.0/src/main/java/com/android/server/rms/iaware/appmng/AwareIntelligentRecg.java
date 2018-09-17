package com.android.server.rms.iaware.appmng;

import android.annotation.SuppressLint;
import android.app.ActivityManagerNative;
import android.app.IWallpaperManager;
import android.app.WallpaperInfo;
import android.app.mtm.iaware.appmng.AppMngConstant.AppMngFeature;
import android.app.mtm.iaware.appmng.AppMngConstant.AppStartSource;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.rms.iaware.AppTypeRecoManager;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CmpTypeInfo;
import android.rms.iaware.ICMSManager;
import android.rms.iaware.ICMSManager.Stub;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.webkit.WebViewZygote;
import com.android.internal.os.BackgroundThread;
import com.android.server.am.ProcessRecord;
import com.android.server.mtm.MultiTaskManagerService;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.iaware.appmng.DecisionMaker;
import com.android.server.mtm.iaware.appmng.appstart.datamgr.AppStartupDataMgr;
import com.android.server.mtm.iaware.appmng.rule.RuleNode;
import com.android.server.mtm.utils.AppStatusUtils;
import com.android.server.mtm.utils.AppStatusUtils.Status;
import com.android.server.mtm.utils.InnerUtils;
import com.android.server.rms.algorithm.AwareUserHabit;
import com.android.server.rms.iaware.appmng.AppStartPolicyCfg.AppStartCallerAction;
import com.android.server.rms.iaware.appmng.AppStartPolicyCfg.AppStartCallerStatus;
import com.android.server.rms.iaware.appmng.AppStartPolicyCfg.AppStartTargetStat;
import com.android.server.rms.iaware.appmng.AppStartPolicyCfg.AppStartTargetType;
import com.android.server.rms.iaware.appmng.AwareAppKeyBackgroup.IAwareStateCallback;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.huawei.pgmng.plug.PGSdk;
import com.huawei.pgmng.plug.PGSdk.Sink;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public final class AwareIntelligentRecg {
    private static final /* synthetic */ int[] -com-android-server-rms-iaware-appmng-AppStartPolicyCfg$AppStartCallerActionSwitchesValues = null;
    private static final /* synthetic */ int[] -com-android-server-rms-iaware-appmng-AppStartPolicyCfg$AppStartCallerStatusSwitchesValues = null;
    private static final /* synthetic */ int[] -com-android-server-rms-iaware-appmng-AppStartPolicyCfg$AppStartTargetStatSwitchesValues = null;
    private static final /* synthetic */ int[] -com-android-server-rms-iaware-appmng-AppStartPolicyCfg$AppStartTargetTypeSwitchesValues = null;
    private static final int ACTIONINDEX = 0;
    private static final int ACTIONLENGTH = 1;
    private static final int ALARMACTIONINDEX = 1;
    private static final int ALARMTAGLENGTH = 2;
    private static final int ALARM_PERCEPTION_TIME = 15000;
    private static final int ALARM_RECG_FACTOR = 5;
    private static final int ALLPROC = 1000;
    private static final String BLIND_PREFABRICATED = "blind_prefabricated";
    private static final String BLUETOOTH_PKG = "com.android.bluetooth";
    private static final String BTMEDIABROWSER_ACTION = "android.media.browse.MediaBrowserService";
    private static final int CLSINDEX = 1;
    private static final int CMPLENGTH = 2;
    public static final String CMP_CLS = "cls";
    public static final String CMP_PKGNAME = "pkgName";
    public static final String CMP_TYPE = "cmpType";
    private static final int CONNECT_PG_DELAYED = 5000;
    private static boolean DEBUG = false;
    private static final int DEFAULT_INVAILD_UID = -1;
    private static final int LOAD_CMPTYPE_MESSAGE_DELAY = 20000;
    private static final int MINCOUNT = 0;
    private static final int MSG_ALARM_NOTIFICATION = 9;
    private static final int MSG_ALARM_SOUND = 10;
    private static final int MSG_ALARM_UNPERCEPTION = 11;
    private static final int MSG_ALARM_VIBRATOR = 8;
    private static final int MSG_CACHEDATA_FLUSHTODISK = 6;
    private static final int MSG_CONNECT_WITH_PG_SDK = 5;
    private static final int MSG_DELETEDATA = 3;
    private static final int MSG_INPUTMETHODSET = 15;
    private static final int MSG_INSERTDATA = 2;
    private static final int MSG_KBG_UPDATE = 7;
    private static final int MSG_LOADDATA = 1;
    private static final int MSG_RECG_PUSHSDK = 4;
    private static final int MSG_UPDATEDB = 12;
    private static final int MSG_WALLPAPERINIT = 14;
    private static final int MSG_WALLPAPERSET = 13;
    private static final String NOTIFY_LISTENER_ACTION = "android.service.notification.NotificationListenerService";
    private static final int ONE_SECOND = 1000;
    public static final int PENDING_ALARM = 0;
    public static final int PENDING_PERC = 2;
    public static final int PENDING_UNPERC = 1;
    private static final int PKGINDEX = 0;
    private static final String PUSHSDK_BAD = "push_bad";
    private static final String PUSHSDK_GOOD = "push_good";
    private static final int PUSHSDK_START_SAME_CMP_COUNT = 2;
    private static final int REPORT_APPUPDATE_MSG = 0;
    private static final String SEPARATOR_COMMA = ",";
    private static final String SEPARATOR_POUND = "#";
    private static final String SEPARATOR_SEMICOLON = ":";
    private static final int SPEC_VALUE_DEFAULT = -1;
    private static final int SPEC_VALUE_TRUE = 1;
    private static final int STARTINFO_CACHE_TIME = 20000;
    private static final int STATUS_DEFAULT = 0;
    private static final int STATUS_PERCEPTION = 1;
    private static final int STATUS_UNPERCEPTION = 2;
    private static final String SYSTEM_PROCESS_NAME = "system";
    private static final String TAG = "RMS.AwareIntelligentRecg";
    private static final int TRANSACTION_getVisibleWindows = 1005;
    private static final int UNPERCEPTION_COUNT = 1;
    private static final int UPDATETIME = 10000000;
    private static final long UPDATE_DB_INTERVAL = 86400000;
    private static final int UPDATE_TIME_FOR_HABIT = 10000;
    private static AwareIntelligentRecg mAwareIntlgRecg = null;
    private static boolean mEnabled = false;
    private final Set<String> mAccessPkg = new ArraySet();
    private ArrayMap<String, CmpTypeInfo> mAlarmCmps = new ArrayMap();
    private ArrayMap<Integer, ArrayMap<String, AlarmInfo>> mAlarmMap = new ArrayMap();
    private Set<String> mAlarmPkgList = null;
    private ArrayMap<String, Long> mAppChangeToBGTime = new ArrayMap();
    private Sink mAppFreezeListener = new Sink() {
        public void onStateChanged(int stateType, int eventType, int pid, String pkg, int uid) {
            if (AwareIntelligentRecg.DEBUG) {
                AwareLog.d(AwareIntelligentRecg.TAG, "pkg:" + pkg + ",uid:" + uid + ",pid:" + pid + ",stateType: " + stateType + ",eventType:" + eventType);
            }
            Set -get1;
            switch (stateType) {
                case 2:
                    if (eventType == 1) {
                        AwareIntelligentRecg.this.sendPerceptionMessage(10, uid, null);
                        -get1 = AwareIntelligentRecg.this.mAudioOutInstant;
                        synchronized (-get1) {
                            AwareIntelligentRecg.this.mAudioOutInstant.add(Integer.valueOf(uid));
                            break;
                        }
                    } else if (eventType == 2) {
                        -get1 = AwareIntelligentRecg.this.mAudioOutInstant;
                        synchronized (-get1) {
                            AwareIntelligentRecg.this.mAudioOutInstant.remove(Integer.valueOf(uid));
                            break;
                        }
                    } else {
                        return;
                    }
                case 6:
                    -get1 = AwareIntelligentRecg.this.mFrozenAppList;
                    synchronized (-get1) {
                        if (eventType != 1) {
                            if (eventType == 2) {
                                AwareIntelligentRecg.this.mFrozenAppList.remove(Integer.valueOf(uid));
                                break;
                            }
                        }
                        AwareIntelligentRecg.this.mFrozenAppList.add(Integer.valueOf(uid));
                        break;
                    }
                    break;
                case 7:
                    -get1 = AwareIntelligentRecg.this.mFrozenAppList;
                    synchronized (-get1) {
                        AwareIntelligentRecg.this.mFrozenAppList.clear();
                        break;
                    }
                case 8:
                    -get1 = AwareIntelligentRecg.this.mBluetoothAppList;
                    synchronized (-get1) {
                        if (eventType != 1) {
                            if (eventType == 2) {
                                AwareIntelligentRecg.this.mBluetoothAppList.remove(Integer.valueOf(uid));
                                break;
                            }
                        }
                        AwareIntelligentRecg.this.mBluetoothAppList.add(Integer.valueOf(uid));
                        break;
                    }
                    break;
                default:
                    return;
            }
        }
    };
    private boolean mAppStartEnabled = false;
    private Set<AwareAppStartInfo> mAppStartInfoList = new ArraySet();
    private Set<Integer> mAudioOutInstant = new ArraySet();
    private AwareStateCallback mAwareStateCallback = null;
    private Set<String> mBlindPkg = new ArraySet();
    private Set<Integer> mBluetoothAppList = new ArraySet();
    private int mBluetoothUid;
    private ArrayMap<Integer, Set<String>> mCachedTypeTopN = new ArrayMap();
    private final ArraySet<IAwareToastCallback> mCallbacks = new ArraySet();
    private int mCurUserId = 0;
    private int mDBUpdateCount = 0;
    private final AtomicInteger mDataLoadCount = new AtomicInteger(2);
    private AppStartupDataMgr mDataMgr = null;
    private String mDefaultInputMethod = "";
    private int mDefaultInputMethodUid = -1;
    private String mDefaultTTS = "";
    private String mDefaultWallPaper = "";
    private int mDefaultWallPaperUid = -1;
    private Set<Integer> mFrozenAppList = new ArraySet();
    private ArraySet<String> mGmsCallerAppPkg = new ArraySet();
    private ArraySet<Integer> mGmsCallerAppUid = new ArraySet();
    private Set<String> mGoodPushSDKClsList = new ArraySet();
    private List<String> mHabbitTopN = null;
    private IntlRecgHandler mHandler = null;
    private final AtomicInteger mInitWallPaperCount = new AtomicInteger(2);
    private AtomicBoolean mIsInitialized = new AtomicBoolean(false);
    private AtomicBoolean mIsObsvInit = new AtomicBoolean(false);
    private AtomicBoolean mIsScreenOn = new AtomicBoolean(true);
    private AtomicLong mLastPerceptionTime = new AtomicLong(0);
    private ArrayMap<Integer, Long> mLastUpdateTime = new ArrayMap();
    private MultiTaskManagerService mMtmService = null;
    private PGSdk mPGSdk = null;
    private PackageManager mPackageManager = null;
    private Set<String> mPayPkgList = null;
    private Set<String> mPushSDKClsList = new ArraySet();
    private Set<String> mRecent = null;
    private LinkedList<Long> mScreenChangedTime = new LinkedList();
    private ContentObserver mSettingsObserver = null;
    private Set<String> mSharePkgList = null;
    private List<String> mSmallSampleList = new ArrayList();
    private final Set<Integer> mStatusAudioIn = new ArraySet();
    private final Set<Integer> mStatusAudioOut = new ArraySet();
    private final Set<Integer> mStatusGps = new ArraySet();
    private final Set<Integer> mStatusSensor = new ArraySet();
    private final Set<Integer> mStatusUpDown = new ArraySet();
    private final Set<String> mTTSPkg = new ArraySet();
    private int mTempHTopN = 0;
    private int mTempRecent = 0;
    private ArrayMap<Integer, Integer> mToasts = new ArrayMap();
    private Set<Integer> mUnRemindAppTypeList = null;
    private long mUpdateTimeHabbitTopN = -1;
    private long mUpdateTimeRecent = -1;
    private int mWebViewUid;

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

        public AlarmInfo(int uid, String packagename, String tag) {
            this.uid = uid;
            this.packagename = packagename;
            this.tag = tag;
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
            StringBuilder sb = new StringBuilder();
            sb.append("package:").append(this.packagename).append(',').append("tag:").append(this.tag).append(',').append("startTime:").append(this.startTime).append(',').append("status:").append(this.status).append(',').append("reason:").append(this.reason).append(',').append("count:").append(this.count).append(',').append("unperception_count:").append(this.unperception_count).append(',').append("perception_count:").append(this.perception_count).append(',').append("bfgset:").append(this.bfgset).append('.');
            return sb.toString();
        }
    }

    static class AwareAppStartInfo {
        public int callerUid;
        public String cls = null;
        public String cmp;
        public long timestamp;

        public AwareAppStartInfo(int callerUid, String cmp) {
            this.callerUid = callerUid;
            this.cmp = cmp;
            parseCmp(cmp);
            this.timestamp = SystemClock.elapsedRealtime();
        }

        private void parseCmp(String cmp) {
            if (cmp != null) {
                String[] strs = cmp.split("/");
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
            if (this.callerUid == info.callerUid) {
                z = this.cmp.equals(info.cmp);
            }
            return z;
        }

        public String toString() {
            return "{caller:" + this.callerUid + "cmp:" + this.cmp + " time:" + this.timestamp + "}";
        }
    }

    private class AwareStateCallback implements IAwareStateCallback {
        /* synthetic */ AwareStateCallback(AwareIntelligentRecg this$0, AwareStateCallback -this1) {
            this();
        }

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

    public interface IAwareToastCallback {
        void onToastWindowsChanged(int i, int i2);
    }

    private class IntlRecgHandler extends Handler {
        public IntlRecgHandler(Looper looper) {
            super(looper);
        }

        private void handleMessageBecauseOfNSIQ(Message msg) {
            switch (msg.what) {
                case 8:
                case 9:
                case 10:
                    AwareIntelligentRecg.this.handlePerceptionEvent(msg.arg1, msg.obj instanceof String ? (String) msg.obj : null, msg.what);
                    return;
                case 11:
                    AwareIntelligentRecg.this.handleUnPerceptionEvent(msg.obj instanceof AlarmInfo ? (AlarmInfo) msg.obj : null, msg.what);
                    return;
                case 13:
                    AwareIntelligentRecg.this.handleWallpaperSetMessage(msg.obj instanceof String ? (String) msg.obj : null);
                    return;
                case 14:
                    AwareIntelligentRecg.this.initDefaultWallPaper();
                    return;
                case 15:
                    AwareIntelligentRecg.this.handleInputMethodSetMessage();
                    return;
                default:
                    return;
            }
        }

        public void handleMessage(Message msg) {
            if (AwareIntelligentRecg.DEBUG) {
                AwareLog.i(AwareIntelligentRecg.TAG, "handleMessage message " + msg.what);
            }
            switch (msg.what) {
                case 0:
                    AwareIntelligentRecg.this.handleReportAppUpdateMsg(msg);
                    return;
                case 1:
                    AwareIntelligentRecg.this.initRecgResultInfo();
                    return;
                case 2:
                    AwareIntelligentRecg.this.insertCmpRecgInfo(msg.obj instanceof CmpTypeInfo ? (CmpTypeInfo) msg.obj : null);
                    return;
                case 3:
                    AwareIntelligentRecg.this.deleteCmpRecgInfo(msg.obj instanceof CmpTypeInfo ? (CmpTypeInfo) msg.obj : null);
                    return;
                case 4:
                    AwareAppStartInfo startinfo = msg.obj instanceof AwareAppStartInfo ? (AwareAppStartInfo) msg.obj : null;
                    if (AwareIntelligentRecg.this.needRecgPushSDK(startinfo)) {
                        AwareIntelligentRecg.this.recordStartInfo(startinfo);
                        AwareIntelligentRecg.this.adjustPushSDKCmp(startinfo);
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
                case 12:
                    AwareIntelligentRecg.this.handleUpdateDB();
                    return;
                default:
                    handleMessageBecauseOfNSIQ(msg);
                    return;
            }
        }
    }

    private class SettingsObserver extends ContentObserver {
        public SettingsObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange, Uri uri) {
            if (Secure.getUriFor("default_input_method").equals(uri)) {
                AwareIntelligentRecg.this.mDefaultInputMethod = AwareIntelligentRecg.this.readDefaultInputMethod();
                AwareIntelligentRecg.this.sendInputMethodSetMessage();
            } else if (Secure.getUriFor("enabled_accessibility_services").equals(uri)) {
                AwareIntelligentRecg.this.updateAccessbilityService();
            } else if (Secure.getUriFor("tts_default_synth").equals(uri)) {
                AwareIntelligentRecg.this.updateDefaultTTS();
            }
        }
    }

    private static /* synthetic */ int[] -getcom-android-server-rms-iaware-appmng-AppStartPolicyCfg$AppStartCallerActionSwitchesValues() {
        if (-com-android-server-rms-iaware-appmng-AppStartPolicyCfg$AppStartCallerActionSwitchesValues != null) {
            return -com-android-server-rms-iaware-appmng-AppStartPolicyCfg$AppStartCallerActionSwitchesValues;
        }
        int[] iArr = new int[AppStartCallerAction.values().length];
        try {
            iArr[AppStartCallerAction.BTMEDIABROWSER.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[AppStartCallerAction.NOTIFYLISTENER.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[AppStartCallerAction.PUSHSDK.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        -com-android-server-rms-iaware-appmng-AppStartPolicyCfg$AppStartCallerActionSwitchesValues = iArr;
        return iArr;
    }

    private static /* synthetic */ int[] -getcom-android-server-rms-iaware-appmng-AppStartPolicyCfg$AppStartCallerStatusSwitchesValues() {
        if (-com-android-server-rms-iaware-appmng-AppStartPolicyCfg$AppStartCallerStatusSwitchesValues != null) {
            return -com-android-server-rms-iaware-appmng-AppStartPolicyCfg$AppStartCallerStatusSwitchesValues;
        }
        int[] iArr = new int[AppStartCallerStatus.values().length];
        try {
            iArr[AppStartCallerStatus.BACKGROUND.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        -com-android-server-rms-iaware-appmng-AppStartPolicyCfg$AppStartCallerStatusSwitchesValues = iArr;
        return iArr;
    }

    private static /* synthetic */ int[] -getcom-android-server-rms-iaware-appmng-AppStartPolicyCfg$AppStartTargetStatSwitchesValues() {
        if (-com-android-server-rms-iaware-appmng-AppStartPolicyCfg$AppStartTargetStatSwitchesValues != null) {
            return -com-android-server-rms-iaware-appmng-AppStartPolicyCfg$AppStartTargetStatSwitchesValues;
        }
        int[] iArr = new int[AppStartTargetStat.values().length];
        try {
            iArr[AppStartTargetStat.ALIVE.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[AppStartTargetStat.FGACTIVITY.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[AppStartTargetStat.GUIDE.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[AppStartTargetStat.HEALTH.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[AppStartTargetStat.INPUTMETHOD.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[AppStartTargetStat.MUSIC.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[AppStartTargetStat.RECORD.ordinal()] = 7;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[AppStartTargetStat.UPDOWNLOAD.ordinal()] = 8;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[AppStartTargetStat.WALLPAPER.ordinal()] = 9;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[AppStartTargetStat.WIDGET.ordinal()] = 10;
        } catch (NoSuchFieldError e10) {
        }
        -com-android-server-rms-iaware-appmng-AppStartPolicyCfg$AppStartTargetStatSwitchesValues = iArr;
        return iArr;
    }

    private static /* synthetic */ int[] -getcom-android-server-rms-iaware-appmng-AppStartPolicyCfg$AppStartTargetTypeSwitchesValues() {
        if (-com-android-server-rms-iaware-appmng-AppStartPolicyCfg$AppStartTargetTypeSwitchesValues != null) {
            return -com-android-server-rms-iaware-appmng-AppStartPolicyCfg$AppStartTargetTypeSwitchesValues;
        }
        int[] iArr = new int[AppStartTargetType.values().length];
        try {
            iArr[AppStartTargetType.BLIND.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[AppStartTargetType.BUSINESS.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[AppStartTargetType.CLOCK.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[AppStartTargetType.EMAIL.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[AppStartTargetType.IM.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[AppStartTargetType.PAY.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[AppStartTargetType.SHARE.ordinal()] = 7;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[AppStartTargetType.TTS.ordinal()] = 8;
        } catch (NoSuchFieldError e8) {
        }
        -com-android-server-rms-iaware-appmng-AppStartPolicyCfg$AppStartTargetTypeSwitchesValues = iArr;
        return iArr;
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

    private void initPGSDK() {
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
            } catch (RemoteException e) {
                AwareLog.e(TAG, "unRegisterPGListener  happend RemoteException!");
            }
        }
    }

    private void handleReportAppUpdateMsg(Message msg) {
        int eventId = msg.arg1;
        Bundle args = msg.getData();
        String pkgName;
        switch (eventId) {
            case 1:
                pkgName = args.getString(AwareUserHabit.USERHABIT_PACKAGE_NAME);
                if (pkgName != null) {
                    initializeForInstallApp(pkgName);
                    return;
                }
                return;
            case 2:
                pkgName = args.getString(AwareUserHabit.USERHABIT_PACKAGE_NAME);
                int uid = args.getInt("uid");
                if (pkgName != null) {
                    initializeForUninstallApp(pkgName);
                    clearAlarmForUninstallApp(pkgName, uid);
                    return;
                }
                return;
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
                DecisionMaker.getInstance().updateRule(AppMngFeature.APP_START, this.mMtmService.context());
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
            if (this.mMtmService != null) {
                updateGmsCallerAppInit(this.mMtmService.context());
            }
            this.mIsInitialized.set(true);
            this.mBluetoothUid = getUIDByPackageName("com.android.bluetooth");
            this.mWebViewUid = getUIDByPackageName(WebViewZygote.getPackageName());
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
    }

    private void loadPushSDK() {
        initPushSDK();
        loadPushSDKResultFromDB();
    }

    private List<CmpTypeInfo> readCmpTypeInfoFromDB() {
        try {
            ICMSManager awareservice = Stub.asInterface(ServiceManager.getService("IAwareCMSService"));
            if (awareservice != null) {
                return awareservice.getCmpTypeList();
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

    private void initializeForUser() {
        if (this.mMtmService != null) {
            unregisterContentObserver(this.mMtmService.context(), this.mSettingsObserver);
            registerContentObserver(this.mMtmService.context(), this.mCurUserId, this.mSettingsObserver);
        }
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
            if (this.mMtmService != null) {
                unregisterContentObserver(this.mMtmService.context(), this.mSettingsObserver);
            }
            this.mDefaultInputMethod = "";
            this.mDefaultWallPaper = "";
            this.mDefaultTTS = "";
            deInitAppStatus();
            this.mIsInitialized.set(false);
        }
    }

    public void initUserSwitch(int userId) {
        if (mEnabled) {
            this.mCurUserId = userId;
            initializeForUser();
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
        if (this.mCurUserId == 0 && UserHandle.getUserId(uid) == 0) {
            deleteAppCmpRecgInfo(pkgName);
            removeAlarmCmp(pkgName);
            removeAlarms(uid);
        }
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
        if (DEBUG) {
            AwareLog.d(TAG, "PushSDK callerUid: " + callerUid + " cmp:" + compName + " targetUid: " + targetUid);
        }
        AwareAppStartInfo appStartInfo = new AwareAppStartInfo(callerUid, compName);
        Message msg = this.mHandler.obtainMessage();
        msg.obj = appStartInfo;
        msg.what = 4;
        this.mHandler.sendMessage(msg);
    }

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

    private void initPushSDK() {
        ArrayList<String> badList = DecisionMaker.getInstance().getRawConfig(AppMngFeature.APP_START.getDesc(), PUSHSDK_BAD);
        if (badList != null) {
            synchronized (this.mPushSDKClsList) {
                this.mPushSDKClsList.clear();
                this.mPushSDKClsList.addAll(badList);
            }
        }
        ArrayList<String> goodList = DecisionMaker.getInstance().getRawConfig(AppMngFeature.APP_START.getDesc(), PUSHSDK_GOOD);
        if (goodList != null) {
            synchronized (this.mGoodPushSDKClsList) {
                this.mGoodPushSDKClsList.clear();
                this.mGoodPushSDKClsList.addAll(goodList);
            }
        }
    }

    private void initPayPkg() {
        ArrayList<String> pkgList = DecisionMaker.getInstance().getRawConfig(AppMngFeature.APP_START.getDesc(), "payPkgList");
        if (pkgList != null) {
            Set<String> pkgSet = new ArraySet();
            pkgSet.addAll(pkgList);
            this.mPayPkgList = pkgSet;
        }
    }

    private void initSharePkg() {
        ArrayList<String> pkgList = DecisionMaker.getInstance().getRawConfig(AppMngFeature.APP_START.getDesc(), "sharePkgList");
        if (pkgList != null) {
            Set<String> pkgSet = new ArraySet();
            pkgSet.addAll(pkgList);
            this.mSharePkgList = pkgSet;
        }
    }

    private void initAlarmPkg() {
        ArrayList<String> alarmList = DecisionMaker.getInstance().getRawConfig(AppMngFeature.APP_START.getDesc(), "alarmPkgList");
        if (alarmList != null) {
            Set<String> alarmSet = new ArraySet();
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
        ArrayList<String> appTypeList = DecisionMaker.getInstance().getRawConfig(AppMngFeature.APP_START.getDesc(), "unRemindAppTypes");
        AwareLog.i(TAG, "initUnRemindAppType " + appTypeList);
        if (appTypeList != null) {
            Set<Integer> unRemindAppSet = new ArraySet();
            int size = appTypeList.size();
            for (int i = 0; i < size; i++) {
                Integer[] types = stringToIntArray((String) appTypeList.get(i));
                if (types.length > 0) {
                    List<Integer> typeList = Arrays.asList(types);
                    AwareLog.i(TAG, "initUnRemindAppType integer " + typeList);
                    unRemindAppSet.addAll(typeList);
                }
            }
            this.mUnRemindAppTypeList = unRemindAppSet;
        }
    }

    private void initRecgResultInfo() {
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
            this.mAwareStateCallback = new AwareStateCallback(this, null);
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

    private void updateKbgStatus(Message msg) {
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
        }
        if (2 == eventType) {
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

    /* JADX WARNING: Missing block: B:9:0x001b, code:
            if (com.android.server.rms.iaware.appmng.AwareAppKeyBackgroup.getInstance().isAudioCache(r5) == false) goto L_0x0021;
     */
    /* JADX WARNING: Missing block: B:10:0x001d, code:
            return true;
     */
    /* JADX WARNING: Missing block: B:15:0x0022, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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

    private boolean loadRecgResultInfo() {
        List<CmpTypeInfo> list = readCmpTypeInfoFromDB();
        if (list == null) {
            return false;
        }
        for (CmpTypeInfo info : list) {
            if (DEBUG) {
                AwareLog.d(TAG, "loadRecgResultInfo info " + info.toString());
            }
            switch (info.getType()) {
                case 0:
                    updatePushSDKClsFromDB(info.getCls());
                    break;
                case 3:
                case 4:
                    synchronized (this.mAlarmCmps) {
                        this.mAlarmCmps.put(info.getPkgName() + "#" + info.getCls(), info);
                    }
                    break;
                default:
                    break;
            }
        }
        return true;
    }

    public void getToastWindows(Set<Integer> toastPids) {
        if (mEnabled && toastPids != null) {
            synchronized (this.mToasts) {
                for (Entry<Integer, Integer> toast : this.mToasts.entrySet()) {
                    if (((Integer) toast.getValue()).intValue() > 0) {
                        toastPids.add((Integer) toast.getKey());
                    }
                }
            }
            if (DEBUG) {
                AwareLog.d(TAG, "ToastPids:" + toastPids);
            }
        }
    }

    private void initToastWindows() {
        Parcel data = null;
        Parcel reply = null;
        try {
            IBinder windowManager = ServiceManager.getService("window");
            if (windowManager == null) {
                AwareLog.e(TAG, "[ERROR]Connect to window Service failed.");
                return;
            }
            data = Parcel.obtain();
            reply = Parcel.obtain();
            data.writeInterfaceToken("android.view.IWindowManager");
            data.writeInt(45);
            windowManager.transact(1005, data, reply, 0);
            synchronized (this.mToasts) {
                this.mToasts.clear();
                notifyToastChange(3, -1);
                int size = reply.readInt();
                for (int i = 0; i < size; i++) {
                    int window = reply.readInt();
                    int mode = reply.readInt();
                    int code = reply.readInt();
                    int width = reply.readInt();
                    int height = reply.readInt();
                    if (DEBUG) {
                        AwareLog.i(TAG, "initToastWindows pid:" + window + " mode:" + mode + " code:" + code + " width:" + width + " height:" + height);
                    }
                    if (this.mToasts.containsKey(Integer.valueOf(window))) {
                        this.mToasts.put(Integer.valueOf(window), Integer.valueOf(((Integer) this.mToasts.get(Integer.valueOf(window))).intValue() + 1));
                    } else {
                        this.mToasts.put(Integer.valueOf(window), Integer.valueOf(1));
                        notifyToastChange(5, window);
                    }
                }
            }
            if (reply != null) {
                reply.recycle();
            }
            if (data != null) {
                data.recycle();
            }
        } catch (RemoteException e) {
            try {
                AwareLog.e(TAG, "[ERROR]Catch RemoteException when initToastWindows.");
            } finally {
                if (reply != null) {
                    reply.recycle();
                }
                if (data != null) {
                    data.recycle();
                }
            }
        }
    }

    private void addToast(int window) {
        if (window > 0) {
            synchronized (this.mToasts) {
                if (this.mToasts.containsKey(Integer.valueOf(window))) {
                    this.mToasts.put(Integer.valueOf(window), Integer.valueOf(((Integer) this.mToasts.get(Integer.valueOf(window))).intValue() + 1));
                } else {
                    this.mToasts.put(Integer.valueOf(window), Integer.valueOf(1));
                    notifyToastChange(5, window);
                }
            }
            if (DEBUG) {
                AwareLog.i(TAG, "[addToast]:" + window);
            }
        }
    }

    private void removeToast(int window) {
        if (window > 0) {
            synchronized (this.mToasts) {
                if (this.mToasts.containsKey(Integer.valueOf(window))) {
                    int count = ((Integer) this.mToasts.get(Integer.valueOf(window))).intValue();
                    if (count > 1) {
                        this.mToasts.put(Integer.valueOf(window), Integer.valueOf(count - 1));
                    } else {
                        this.mToasts.remove(Integer.valueOf(window));
                        notifyToastChange(4, window);
                    }
                }
            }
            if (DEBUG) {
                AwareLog.i(TAG, "[removeToast]:" + window);
            }
        }
    }

    private Map<String, CmpTypeInfo> getAlarmCmpFromDB() {
        Map<String, CmpTypeInfo> result = new ArrayMap();
        List<CmpTypeInfo> cmplist = null;
        try {
            ICMSManager awareservice = Stub.asInterface(ServiceManager.getService("IAwareCMSService"));
            if (awareservice != null) {
                cmplist = awareservice.getCmpTypeList();
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
                result.put(info.getPkgName() + "#" + info.getCls(), info);
            }
        }
        return result;
    }

    private boolean insertCmpRecgInfo(CmpTypeInfo info) {
        try {
            ICMSManager awareservice = Stub.asInterface(ServiceManager.getService("IAwareCMSService"));
            if (awareservice != null) {
                awareservice.insertCmpTypeInfo(info);
                return true;
            }
            AwareLog.e(TAG, "can not find service IAwareCMSService.");
            return false;
        } catch (RemoteException e) {
            AwareLog.e(TAG, "inSertCmpRecgInfo RemoteException");
            return false;
        }
    }

    private boolean deleteCmpRecgInfo(CmpTypeInfo info) {
        try {
            ICMSManager awareservice = Stub.asInterface(ServiceManager.getService("IAwareCMSService"));
            if (awareservice != null) {
                awareservice.deleteCmpTypeInfo(info);
                return true;
            }
            AwareLog.e(TAG, "can not find service IAwareCMSService.");
            return false;
        } catch (RemoteException e) {
            AwareLog.e(TAG, "deleteCmpRecgInfo RemoteException");
            return false;
        }
    }

    private boolean deleteAppCmpRecgInfo(String pkg) {
        try {
            ICMSManager awareservice = Stub.asInterface(ServiceManager.getService("IAwareCMSService"));
            if (awareservice != null) {
                awareservice.deleteAppCmpTypeInfo(pkg);
                AwareLog.e(TAG, "delete " + pkg + " from iAware.db");
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
        if (mEnabled) {
            if (DEBUG) {
                AwareLog.d(TAG, "eventId: " + eventId);
            }
            if (bundleArgs != null) {
                if (!this.mIsInitialized.get()) {
                    initialize();
                }
                switch (eventId) {
                    case 8:
                        addToast(bundleArgs.getInt("window"));
                        break;
                    case 9:
                        removeToast(bundleArgs.getInt("window"));
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
                    case 20011:
                        this.mIsScreenOn.set(true);
                        break;
                    case 90011:
                        this.mIsScreenOn.set(false);
                        break;
                    default:
                        AwareLog.e(TAG, "Unknown EventID: " + eventId);
                        break;
                }
                return;
            }
            return;
        }
        if (DEBUG) {
            AwareLog.d(TAG, "AwareIntelligentRecg feature disabled!");
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
        Set<String> ttsPkg = new ArraySet();
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
    }

    private void initBlindPkg() {
        ArrayList<String> blindList = DecisionMaker.getInstance().getRawConfig(AppMngFeature.APP_START.getDesc(), BLIND_PREFABRICATED);
        if (blindList != null) {
            synchronized (this.mBlindPkg) {
                this.mBlindPkg.clear();
                this.mBlindPkg.addAll(blindList);
            }
        }
    }

    private void updateAccessbilityService() {
        if (this.mMtmService != null) {
            String settingValue = Secure.getStringForUser(this.mMtmService.context().getContentResolver(), "enabled_accessibility_services", this.mCurUserId);
            if (DEBUG) {
                AwareLog.i(TAG, "Accessbility:" + settingValue);
            }
            Set<String> accessPkg = new ArraySet();
            if (!(settingValue == null || (settingValue.equals("") ^ 1) == 0)) {
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
            Set<String> blindPkgs = new ArraySet();
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

    private void updateDefaultTTS() {
        if (this.mMtmService != null) {
            String settingValue = Secure.getStringForUser(this.mMtmService.context().getContentResolver(), "tts_default_synth", this.mCurUserId);
            if (DEBUG) {
                AwareLog.i(TAG, "Default TTS :" + settingValue);
            }
            this.mDefaultTTS = settingValue;
        }
    }

    private String readDefaultInputMethod() {
        if (this.mMtmService == null) {
            return "";
        }
        String inputInfo = Secure.getStringForUser(this.mMtmService.context().getContentResolver(), "default_input_method", this.mCurUserId);
        if (inputInfo != null) {
            String[] defaultIms = inputInfo.split("/");
            if (defaultIms[0] != null) {
                return defaultIms[0];
            }
        }
        return "";
    }

    private void initDefaultWallPaper() {
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

    private boolean isPayPkg(String pkg) {
        if (!mEnabled || this.mPayPkgList == null) {
            return false;
        }
        return this.mPayPkgList.contains(pkg);
    }

    private boolean isSharePkg(String pkg) {
        if (!mEnabled || this.mSharePkgList == null) {
            return false;
        }
        return this.mSharePkgList.contains(pkg);
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

    private void reportWallpaper(String pkg) {
        if (DEBUG) {
            AwareLog.i(TAG, "reportWallpaper  pkg=" + pkg);
        }
        if (pkg != null && (pkg.isEmpty() ^ 1) != 0) {
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

    private void handleWallpaperSetMessage(String pkg) {
        this.mDefaultWallPaper = pkg;
        this.mDefaultWallPaperUid = getUIDByPackageName(pkg);
    }

    private void sendInputMethodSetMessage() {
        if (this.mHandler != null) {
            Message msg = this.mHandler.obtainMessage();
            msg.what = 15;
            this.mHandler.sendMessage(msg);
        }
    }

    private void handleInputMethodSetMessage() {
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
        } catch (NameNotFoundException e) {
            AwareLog.e(TAG, "get the package uid faied");
        }
        return uid;
    }

    private void recordStartInfo(AwareAppStartInfo startInfo) {
        if (startInfo != null) {
            ArrayList<AwareAppStartInfo> mRemoveList = new ArrayList();
            long currentTime = SystemClock.elapsedRealtime();
            synchronized (this.mAppStartInfoList) {
                for (AwareAppStartInfo item : this.mAppStartInfoList) {
                    if (currentTime - item.timestamp > 20000) {
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
        for (AwareAppStartInfo info : infos) {
            if (startInfo.equals(info)) {
                return true;
            }
        }
        return false;
    }

    private boolean needRecgPushSDK(AwareAppStartInfo startInfo) {
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

    private void adjustPushSDKCmp(AwareAppStartInfo startInfo) {
        if (startInfo != null) {
            ArrayMap<Integer, ArraySet<AwareAppStartInfo>> mStartMap = new ArrayMap();
            long currentTime = SystemClock.elapsedRealtime();
            synchronized (this.mAppStartInfoList) {
                for (AwareAppStartInfo item : this.mAppStartInfoList) {
                    if (currentTime - item.timestamp < 20000) {
                        if (mStartMap.containsKey(Integer.valueOf(item.callerUid))) {
                            if (!isExist((ArraySet) mStartMap.get(Integer.valueOf(item.callerUid)), item)) {
                                ((ArraySet) mStartMap.get(Integer.valueOf(item.callerUid))).add(item);
                            }
                        } else {
                            ArraySet<AwareAppStartInfo> initset = new ArraySet();
                            initset.add(item);
                            mStartMap.put(Integer.valueOf(item.callerUid), initset);
                        }
                    }
                }
            }
            ArrayMap<String, Integer> clsMap = new ArrayMap();
            for (Entry<Integer, ArraySet<AwareAppStartInfo>> map : mStartMap.entrySet()) {
                int uid = ((Integer) map.getKey()).intValue();
                ArraySet<AwareAppStartInfo> sameCallerStartInfos = (ArraySet) map.getValue();
                if (DEBUG) {
                    AwareLog.d(TAG, "PushSDK check cls uid:" + uid + " size:" + sameCallerStartInfos.size());
                }
                clsMap.clear();
                for (AwareAppStartInfo item2 : sameCallerStartInfos) {
                    if (item2.cls != null) {
                        if (DEBUG) {
                            AwareLog.d(TAG, "PushSDK check cls :" + item2.cls);
                        }
                        if (clsMap.containsKey(item2.cls)) {
                            int count = ((Integer) clsMap.get(item2.cls)).intValue();
                            clsMap.put(item2.cls, Integer.valueOf(count + 1));
                            updatePushSDKCls(item2.cls, count + 1);
                        } else {
                            clsMap.put(item2.cls, Integer.valueOf(1));
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
            if (this.mPushSDKClsList.contains(cls)) {
                return;
            }
            this.mPushSDKClsList.add(cls);
            sendUpdateCmpInfoMessage("", cls, 0, true);
        }
    }

    private boolean isUnReminderApp(List<String> packageList) {
        if (packageList == null) {
            return false;
        }
        int packageListSize = packageList.size();
        for (int i = 0; i < packageListSize; i++) {
            if (!isUnReminderApp((String) packageList.get(i))) {
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
        return this.mUnRemindAppTypeList == null ? false : this.mUnRemindAppTypeList.contains(Integer.valueOf(type));
    }

    private void initAlarm(AlarmInfo alarm, CmpTypeInfo alarmRecgInfo, boolean isAdd, boolean isWakeup, long time) {
        int i;
        boolean z = false;
        int status = alarmRecgInfo != null ? alarmRecgInfo.getType() == 4 ? 2 : 1 : 0;
        alarm.status = status;
        if (isAdd) {
            i = 1;
        } else {
            i = 0;
        }
        alarm.count = i;
        if (!isWakeup) {
            time = 0;
        }
        alarm.startTime = time;
        if (alarmRecgInfo != null) {
            i = alarmRecgInfo.getPerceptionCount();
        } else {
            i = 0;
        }
        alarm.perception_count = i;
        if (alarmRecgInfo != null) {
            i = alarmRecgInfo.getUnPerceptionCount();
        } else {
            i = 0;
        }
        alarm.unperception_count = i;
        if (isAdd) {
            if (AwareAppAssociate.getInstance().isForeGroundApp(alarm.uid)) {
                z = this.mIsScreenOn.get();
            }
            alarm.bfgset = z;
        }
    }

    /* JADX WARNING: Missing block: B:5:0x000b, code:
            return;
     */
    /* JADX WARNING: Missing block: B:39:0x00da, code:
            if (r9 == false) goto L_0x00f7;
     */
    /* JADX WARNING: Missing block: B:41:0x00e0, code:
            if (r23.mCurUserId != 0) goto L_0x00f7;
     */
    /* JADX WARNING: Missing block: B:43:0x00f0, code:
            if (r10 <= (r23.mLastPerceptionTime.get() + 15000)) goto L_0x00f7;
     */
    /* JADX WARNING: Missing block: B:44:0x00f2, code:
            sendUnPerceptionMessage(r6);
     */
    /* JADX WARNING: Missing block: B:46:0x0109, code:
            if ((android.os.SystemClock.elapsedRealtime() / 86400000) <= ((long) r23.mDBUpdateCount)) goto L_0x013a;
     */
    /* JADX WARNING: Missing block: B:47:0x010b, code:
            sendUpdateDbMessage();
            r23.mDBUpdateCount++;
     */
    /* JADX WARNING: Missing block: B:48:0x011a, code:
            if (DEBUG == false) goto L_0x013a;
     */
    /* JADX WARNING: Missing block: B:49:0x011c, code:
            android.rms.iaware.AwareLog.i(TAG, "UPDATE_DB_INTERVAL mDBUpdateCount : " + r23.mDBUpdateCount);
     */
    /* JADX WARNING: Missing block: B:50:0x013a, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void reportAlarm(String packageName, String statustag, int operation, int uid) {
        Throwable th;
        if (uid >= 10000 && packageName != null && statustag != null && !isUnReminderApp(packageName)) {
            if (this.mAlarmPkgList == null || !this.mAlarmPkgList.contains(packageName)) {
                String[] strs = statustag.split(SEPARATOR_SEMICOLON);
                if (strs.length == 2) {
                    String tag = strs[1];
                    if (DEBUG) {
                        AwareLog.i(TAG, "reportAlarm for clock app pkg : " + packageName + " tag : " + tag + " uid : " + uid + " operation : " + operation);
                    }
                    if (UserHandle.getUserId(uid) == 0) {
                        CmpTypeInfo alarmRecgInfo = getAlarmRecgResult(packageName, tag);
                        boolean isAdd = operation == 0;
                        boolean isWakeup = operation == 2;
                        long time = SystemClock.elapsedRealtime();
                        AlarmInfo alarmInfo = null;
                        synchronized (this.mAlarmMap) {
                            AlarmInfo alarm;
                            try {
                                ArrayMap<String, AlarmInfo> alarms = (ArrayMap) this.mAlarmMap.get(Integer.valueOf(uid));
                                if (alarms == null) {
                                    alarms = new ArrayMap();
                                    alarm = new AlarmInfo(uid, packageName, tag);
                                    try {
                                        initAlarm(alarm, alarmRecgInfo, isAdd, isWakeup, time);
                                        alarms.put(tag, alarm);
                                        this.mAlarmMap.put(Integer.valueOf(uid), alarms);
                                    } catch (Throwable th2) {
                                        th = th2;
                                        throw th;
                                    }
                                }
                                alarm = (AlarmInfo) alarms.get(tag);
                                if (alarm == null) {
                                    alarmInfo = new AlarmInfo(uid, packageName, tag);
                                    initAlarm(alarmInfo, alarmRecgInfo, isAdd, isWakeup, time);
                                    alarms.put(tag, alarmInfo);
                                    alarm = alarmInfo;
                                } else {
                                    if (isWakeup) {
                                        if (alarm.count > 0) {
                                            alarm.startTime = time;
                                            alarm.bhandled = false;
                                        }
                                    }
                                    if (isAdd) {
                                        alarm.count++;
                                    } else if (alarm.count > 0) {
                                        alarm.count--;
                                    }
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                alarm = alarmInfo;
                                throw th;
                            }
                        }
                    }
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:54:0x018a, code:
            r9 = r3.keySet().iterator();
     */
    /* JADX WARNING: Missing block: B:56:0x0196, code:
            if (r9.hasNext() == false) goto L_0x01b0;
     */
    /* JADX WARNING: Missing block: B:57:0x0198, code:
            r8 = (com.android.server.rms.iaware.appmng.AwareIntelligentRecg.AlarmInfo) r9.next();
            updateAlarmCmp(r8, ((java.lang.Boolean) r3.get(r8)).booleanValue());
     */
    /* JADX WARNING: Missing block: B:58:0x01b0, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handlePerceptionEvent(int uid, String pkg, int event) {
        if (DEBUG) {
            AwareLog.i(TAG, "handlePerceptionEvent uid : " + uid + " pkg : " + pkg + " for event : " + event);
        }
        if (UserHandle.getUserId(uid) != 0 || uid < 10000) {
            return;
        }
        if (event != 9 || !AwareAppAssociate.getInstance().isForeGroundApp(uid)) {
            long now = SystemClock.elapsedRealtime();
            this.mLastPerceptionTime.set(now);
            ArrayMap<AlarmInfo, Boolean> alarmMap = new ArrayMap();
            synchronized (this.mAlarmMap) {
                ArrayMap<String, AlarmInfo> alarms = (ArrayMap) this.mAlarmMap.get(Integer.valueOf(uid));
                if (alarms == null) {
                    return;
                }
                boolean bUpdateDb = false;
                for (String tag : alarms.keySet()) {
                    AlarmInfo alarm = (AlarmInfo) alarms.get(tag);
                    if (event != 9 || (alarm.bfgset ^ 1) == 0) {
                        if (this.mHandler.hasMessages(11, alarm)) {
                            this.mHandler.removeMessages(11, alarm);
                        }
                        long alarmtime = alarm.startTime;
                        if (15000 + alarmtime >= now && alarmtime <= now) {
                            if (!alarm.bhandled || alarm.reason != event) {
                                alarm.reason = event;
                                alarm.perception_count++;
                                alarm.bhandled = true;
                                int newStatus = getNewPerceptionStatus(alarm);
                                if (newStatus != alarm.status && newStatus == 1) {
                                    alarm.status = 1;
                                    bUpdateDb = true;
                                }
                                alarmMap.put(alarm.copy(), Boolean.valueOf(bUpdateDb));
                                if (DEBUG) {
                                    AwareLog.i(TAG, "alarm is a clock alarm : " + alarm.packagename + " tag : " + tag + " for reason : " + event);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void handleUnPerceptionEvent(AlarmInfo alarm, int event) {
        if (alarm != null) {
            AlarmInfo updateAlarm;
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
        if (alarm.unperception_count >= 1 && alarm.unperception_count > alarm.perception_count * 5) {
            status = 2;
        }
        return status;
    }

    private CmpTypeInfo getAlarmRecgResult(String pkg, String tag) {
        if (pkg == null || tag == null) {
            return null;
        }
        String key = pkg + "#" + tag;
        synchronized (this.mAlarmCmps) {
            CmpTypeInfo info = (CmpTypeInfo) this.mAlarmCmps.get(key);
            if (info != null) {
                return info;
            }
            return null;
        }
    }

    /* JADX WARNING: Missing block: B:18:0x005f, code:
            if (r13 == false) goto L_0x0070;
     */
    /* JADX WARNING: Missing block: B:19:0x0061, code:
            r5 = r11.mHandler.obtainMessage();
            r5.obj = r2;
            r5.what = 2;
            r11.mHandler.sendMessage(r5);
     */
    /* JADX WARNING: Missing block: B:20:0x0070, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateAlarmCmp(AlarmInfo alarm, boolean binsert) {
        Throwable th;
        if (alarm != null) {
            String key = alarm.packagename + "#" + alarm.tag;
            synchronized (this.mAlarmCmps) {
                try {
                    CmpTypeInfo cmpInfo = (CmpTypeInfo) this.mAlarmCmps.get(key);
                    if (cmpInfo == null) {
                        CmpTypeInfo cmpInfo2 = new CmpTypeInfo();
                        try {
                            cmpInfo2.setPkgName(alarm.packagename);
                            cmpInfo2.setCls(alarm.tag);
                            cmpInfo2.setTime(System.currentTimeMillis());
                            this.mAlarmCmps.put(key, cmpInfo2);
                            cmpInfo = cmpInfo2;
                        } catch (Throwable th2) {
                            th = th2;
                            cmpInfo = cmpInfo2;
                            throw th;
                        }
                    }
                    cmpInfo.setType(alarm.status == 2 ? 4 : 3);
                    cmpInfo.setPerceptionCount(alarm.perception_count);
                    cmpInfo.setUnPerceptionCount(alarm.unperception_count);
                } catch (Throwable th3) {
                    th = th3;
                    throw th;
                }
            }
        }
    }

    private void removeAlarmCmp(String pkg) {
        if (pkg != null) {
            synchronized (this.mAlarmCmps) {
                Iterator<Entry<String, CmpTypeInfo>> it = this.mAlarmCmps.entrySet().iterator();
                String pkgKey = pkg + "#";
                while (it.hasNext()) {
                    String key = (String) ((Entry) it.next()).getKey();
                    if (key != null && key.indexOf(pkgKey) == 0) {
                        it.remove();
                    }
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
                Iterator<Entry<Integer, ArrayMap<String, AlarmInfo>>> it = this.mAlarmMap.entrySet().iterator();
                while (it.hasNext()) {
                    if (uid == ((Integer) ((Entry) it.next()).getKey()).intValue()) {
                        it.remove();
                    }
                }
            }
        }
    }

    private int getAlarmActionType(String pkg, String action) {
        String key = pkg + "#" + action;
        synchronized (this.mAlarmCmps) {
            CmpTypeInfo info = (CmpTypeInfo) this.mAlarmCmps.get(key);
            if (info != null) {
                int type = info.getType();
                return type;
            }
            return -1;
        }
    }

    public List<String> getAllInvalidAlarmTags(int uid, String pkg) {
        if (pkg == null) {
            return null;
        }
        List<String> result = new ArrayList();
        if (this.mAlarmPkgList != null && this.mAlarmPkgList.contains(pkg)) {
            return result;
        }
        if (isUnReminderApp(pkg)) {
            return null;
        }
        List<String> tmp = new ArrayList();
        tmp.add(pkg);
        if (shouldReserveAlarm(uid, tmp)) {
            return result;
        }
        synchronized (this.mAlarmCmps) {
            String pkgKey = pkg + "#";
            for (Entry<String, CmpTypeInfo> m : this.mAlarmCmps.entrySet()) {
                if (m != null) {
                    String key = (String) m.getKey();
                    if (key != null && key.indexOf(pkgKey) == 0) {
                        CmpTypeInfo value = (CmpTypeInfo) m.getValue();
                        if (value.getType() == 4) {
                            result.add(value.getCls());
                        }
                    }
                }
            }
        }
        return result;
    }

    /* JADX WARNING: Missing block: B:38:0x006c, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
        if (isUnReminderApp((List) packageList)) {
            return false;
        }
        if (shouldReserveAlarm(uid, packageList)) {
            return true;
        }
        synchronized (this.mAlarmMap) {
            ArrayMap<String, AlarmInfo> alarms = (ArrayMap) this.mAlarmMap.get(Integer.valueOf(uid));
            if (alarms == null) {
                return false;
            }
            for (Entry<String, AlarmInfo> entry : alarms.entrySet()) {
                if (entry != null) {
                    AlarmInfo info = (AlarmInfo) entry.getValue();
                    if (info != null && (info.status == 0 || info.status == 1)) {
                    }
                }
            }
            return false;
        }
    }

    private void handleUpdateDB() {
        Map<String, CmpTypeInfo> alarmsInDB = getAlarmCmpFromDB();
        synchronized (this.mAlarmCmps) {
            for (Entry entry : this.mAlarmCmps.entrySet()) {
                CmpTypeInfo value = (CmpTypeInfo) entry.getValue();
                CmpTypeInfo alarmInDB = (CmpTypeInfo) alarmsInDB.get(entry.getKey());
                if (alarmInDB == null || alarmInDB.getPerceptionCount() != value.getPerceptionCount() || alarmInDB.getUnPerceptionCount() != value.getUnPerceptionCount()) {
                    insertCmpRecgInfo(value);
                }
            }
        }
    }

    public void updateWidget(Set<String> widgets) {
        if (this.mDataMgr != null && this.mDataMgr.updateWidgetList(widgets)) {
            sendFlushToDiskMessage();
        }
    }

    /* JADX WARNING: Missing block: B:35:0x0088, code:
            if (android.app.mtm.iaware.appmng.AppMngConstant.AppStartSource.THIRD_ACTIVITY.equals(r13) != false) goto L_0x0092;
     */
    /* JADX WARNING: Missing block: B:37:0x0090, code:
            if (android.app.mtm.iaware.appmng.AppMngConstant.AppStartSource.BIND_SERVICE.equals(r13) == false) goto L_0x0096;
     */
    /* JADX WARNING: Missing block: B:38:0x0092, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:43:0x0097, code:
            if (r2.length != 2) goto L_0x009c;
     */
    /* JADX WARNING: Missing block: B:44:0x0099, code:
            sendRecgPushSdkMessage(r10, r11, r12);
     */
    /* JADX WARNING: Missing block: B:45:0x009c, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isPushSdkComp(int callerUid, String compName, int targetUid, AppStartSource source, boolean isSysApp) {
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
        int index;
        String[] strs = compName.split("/");
        if (strs.length == 1) {
            index = 0;
        } else if (strs.length != 2) {
            return false;
        } else {
            index = 1;
        }
        String cls = strs[index];
        synchronized (this.mPushSDKClsList) {
            if (this.mPushSDKClsList.contains(cls)) {
                return true;
            }
        }
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
            alarmType = getAlarmActionType(pkg, status.mAction);
        } else if (status.mCompName != null) {
            alarmType = getAlarmActionType(pkg, status.mCompName);
        }
        return alarmType != 4;
    }

    public int getAppStartSpecCallerAction(String packageName, AwareAppStartStatusCache status, ArrayList<Integer> actionList, AppStartSource source) {
        if (!this.mAppStartEnabled || actionList == null) {
            return -1;
        }
        int size = actionList.size();
        for (int i = 0; i < size; i++) {
            Integer expectItem = (Integer) actionList.get(i);
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
            Integer expectItem = (Integer) statusList.get(i);
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
            Integer expectItem = (Integer) typeList.get(i);
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
            Integer expectItem = (Integer) statusList.get(i);
            if (isAppStartSpecStat(packageName, expectItem.intValue(), status)) {
                return expectItem.intValue();
            }
        }
        return -1;
    }

    private boolean isAppStartSpecCallerAction(String pkg, int appAction, AwareAppStartStatusCache status, AppStartSource source) {
        if (!this.mAppStartEnabled) {
            return false;
        }
        if (DEBUG) {
            AwareLog.i(TAG, "isAppStartSpecAction " + pkg + ",action:" + appAction);
        }
        if (appAction < 0 || appAction >= AppStartCallerAction.values().length) {
            return false;
        }
        return isAppStartSpecCallerActionComm(pkg, AppStartCallerAction.values()[appAction], status, source);
    }

    private boolean isAppStartSpecCallerActionComm(String pkg, AppStartCallerAction appAction, AwareAppStartStatusCache status, AppStartSource source) {
        if (!mEnabled) {
            return false;
        }
        switch (-getcom-android-server-rms-iaware-appmng-AppStartPolicyCfg$AppStartCallerActionSwitchesValues()[appAction.ordinal()]) {
            case 1:
                return status.mIsBtMediaBrowserCaller;
            case 2:
                return status.mNotifyListenerCaller;
            case 3:
                String pushComp = (AppStartSource.THIRD_BROADCAST.equals(source) || AppStartSource.SYSTEM_BROADCAST.equals(source)) ? status.mAction : status.mCompName;
                return isPushSdkComp(status.mCallerUid, pushComp, status.mUid, source, status.mIsSystemApp);
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
        if (appStatus < 0 || appStatus >= AppStartCallerStatus.values().length) {
            return false;
        }
        switch (-getcom-android-server-rms-iaware-appmng-AppStartPolicyCfg$AppStartCallerStatusSwitchesValues()[AppStartCallerStatus.values()[appStatus].ordinal()]) {
            case 1:
                return status.mIsCallerFg ^ 1;
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
        if (appType < 0 || appType >= AppStartTargetType.values().length) {
            return false;
        }
        AppStartTargetType appStartEnum = AppStartTargetType.values()[appType];
        switch (-getcom-android-server-rms-iaware-appmng-AppStartPolicyCfg$AppStartTargetTypeSwitchesValues()[appStartEnum.ordinal()]) {
            case 1:
                if (this.mDataMgr != null) {
                    z = this.mDataMgr.isBlindAssistPkg(pkg);
                }
                return z;
            default:
                return isAppStartSpecTypeComm(pkg, appStartEnum, status);
        }
    }

    private boolean isAppStartSpecTypeComm(String pkg, AppStartTargetType appType, AwareAppStartStatusCache status) {
        boolean z = true;
        if (!mEnabled) {
            return false;
        }
        switch (-getcom-android-server-rms-iaware-appmng-AppStartPolicyCfg$AppStartTargetTypeSwitchesValues()[appType.ordinal()]) {
            case 2:
                if (status.mAppType == -2) {
                    status.mAppType = getAppMngSpecType(pkg);
                }
                if (status.mAppType != 11) {
                    z = false;
                }
                return z;
            case 3:
                return isAlarmApp(pkg, status);
            case 4:
                return isAppMngSpecTypeFreqTopN(pkg, 1, -1);
            case 5:
                if (status.mAppType == -2) {
                    status.mAppType = getAppMngSpecType(pkg);
                }
                if (status.mAppType != 0) {
                    z = false;
                }
                return z;
            case 6:
                return isPayPkg(pkg);
            case 7:
                return isSharePkg(pkg);
            case 8:
                return isTTSPkg(pkg);
            default:
                return false;
        }
    }

    private boolean isAppStartSpecStat(String pkg, int statType, AwareAppStartStatusCache status) {
        boolean z = false;
        if (!this.mAppStartEnabled) {
            return false;
        }
        if (DEBUG) {
            AwareLog.i(TAG, "isAppStartSpecStat " + pkg + ",status:" + statType);
        }
        if (statType < 0 || statType >= AppStartTargetStat.values().length) {
            return false;
        }
        AppStartTargetStat appStartEnum = AppStartTargetStat.values()[statType];
        switch (-getcom-android-server-rms-iaware-appmng-AppStartPolicyCfg$AppStartTargetStatSwitchesValues()[appStartEnum.ordinal()]) {
            case 1:
                return status.mIsAppStop ^ 1;
            case 10:
                if (this.mDataMgr != null) {
                    z = this.mDataMgr.isWidgetExistPkg(pkg);
                }
                return z;
            default:
                return isAppStartSpecStatComm(pkg, appStartEnum, status);
        }
    }

    private boolean isAppStartSpecStatComm(String pkg, AppStartTargetStat appStartEnum, AwareAppStartStatusCache status) {
        boolean z = true;
        if (!mEnabled) {
            return false;
        }
        switch (-getcom-android-server-rms-iaware-appmng-AppStartPolicyCfg$AppStartTargetStatSwitchesValues()[appStartEnum.ordinal()]) {
            case 2:
                return status.mIsTargetFg;
            case 3:
                return isGpsStatus(status.mUid);
            case 4:
                return isSensorStatus(status.mUid);
            case 5:
                if (status.mUid != getDefaultInputMethodUid()) {
                    z = false;
                }
                return z;
            case 6:
                return isAudioOutStatus(status.mUid);
            case 7:
                return isAudioInStatus(status.mUid);
            case 8:
                return isUpDownStatus(status.mUid);
            case 9:
                if (status.mUid != getDefaultWallPaperUid()) {
                    z = false;
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
            return tmpRecent.contains(pkg) ^ 1;
        }
        return tmpRecent.contains(pkg);
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
            return ((Set) this.mCachedTypeTopN.get(Integer.valueOf(type))).contains(pkg);
        } else {
            List<String> topNList = habit.getMostFreqAppByType(type, topN);
            if (topNList == null) {
                return false;
            }
            return topNList.contains(pkg);
        }
    }

    private void updateCacheIfNeed(AwareUserHabit habit, int type) {
        long curTime = SystemClock.elapsedRealtime();
        ArrayMap<Integer, Set<String>> cachedTopNMap = this.mCachedTypeTopN;
        ArrayMap<Integer, Long> lastUpdateTimeMap = this.mLastUpdateTime;
        Long lastUpdateTime = (Long) lastUpdateTimeMap.get(Integer.valueOf(type));
        if (lastUpdateTime == null) {
            lastUpdateTime = Long.valueOf(0);
        }
        if (curTime - lastUpdateTime.longValue() >= MemoryConstant.MIN_INTERVAL_OP_TIMEOUT) {
            List<String> topN = habit.getMostFreqAppByType(type, -1);
            if (topN == null) {
                topN = new ArrayList();
            }
            ArraySet<String> cachedTopN = new ArraySet();
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
            return tmpHabbitTopN.contains(pkg) ^ 1;
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
        Status[] values = Status.values();
        if (values.length <= statType || statType < 0) {
            return false;
        }
        return AppStatusUtils.getInstance().checkAppStatus(values[statType], info);
    }

    public int getAppMngSpecStat(AwareProcessInfo info, LinkedHashMap<Integer, RuleNode> statTypes) {
        if (!mEnabled || statTypes == null || info == null) {
            return -1;
        }
        Status[] values = Status.values();
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
            for (String pkg : awareProcInfo.mProcInfo.mPackageName) {
                if (!this.mSmallSampleList.contains(pkg)) {
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
            smallSamples = new ArrayList(this.mSmallSampleList);
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
        if (!mEnabled) {
            return false;
        }
        boolean containsKey;
        synchronized (this.mToasts) {
            containsKey = this.mToasts.containsKey(Integer.valueOf(pid));
        }
        return containsKey;
    }

    private void registerContentObserver(Context context, int userId, ContentObserver observer) {
        if (!this.mIsObsvInit.get()) {
            context.getContentResolver().registerContentObserver(Secure.getUriFor("default_input_method"), false, observer, userId);
            context.getContentResolver().registerContentObserver(Secure.getUriFor("enabled_accessibility_services"), false, observer, userId);
            context.getContentResolver().registerContentObserver(Secure.getUriFor("tts_default_synth"), false, observer, userId);
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
            }
            pw.println("[dump default InputMethod]");
            pw.println(this.mDefaultInputMethod + ",uid:" + this.mDefaultInputMethodUid);
        }
    }

    public void dumpWallpaper(PrintWriter pw) {
        if (pw != null) {
            if (!mEnabled) {
                pw.println("AwareIntelligentRecg feature disabled.");
            }
            pw.println("[dump Wallpaper]");
            pw.println(this.mDefaultWallPaper + ",uid:" + this.mDefaultWallPaperUid);
        }
    }

    public void dumpAccessibility(PrintWriter pw) {
        if (pw != null) {
            if (!mEnabled) {
                pw.println("AwareIntelligentRecg feature disabled.");
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
            if (mEnabled) {
                pw.println("AwareIntelligentRecg dumpPushSDK start");
                synchronized (this.mPushSDKClsList) {
                    pw.println("recg push sdk cls:" + this.mPushSDKClsList);
                }
                synchronized (this.mGoodPushSDKClsList) {
                    pw.println("good push sdk cls:" + this.mGoodPushSDKClsList);
                }
                synchronized (this.mAppStartInfoList) {
                    for (AwareAppStartInfo item : this.mAppStartInfoList) {
                        pw.println("start record:" + item.toString());
                    }
                }
                pw.println("AwareIntelligentRecg dumpPushSDK end");
                return;
            }
            pw.println("AwareIntelligentRecg feature disabled.");
        }
    }

    private void updateGmsCallerApp(Context context) {
        ArraySet<Integer> gmsUid = new ArraySet();
        for (String pkg : this.mGmsCallerAppPkg) {
            int uid = getUIDByPackageName(context, pkg);
            if (uid != -1) {
                gmsUid.add(Integer.valueOf(uid));
            }
        }
        this.mGmsCallerAppUid = gmsUid;
    }

    private void updateGmsCallerAppInit(Context context) {
        ArrayList<String> gmsList = DecisionMaker.getInstance().getRawConfig(AppMngFeature.APP_START.getDesc(), "gmscaller");
        ArraySet<String> gmsSet = new ArraySet();
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
        this.mDataMgr = dataMgr;
        updateGmsCallerAppInit(context);
        this.mAppStartEnabled = true;
    }

    public void appStartDisable() {
        this.mAppStartEnabled = false;
    }

    public void dumpToastWindow(PrintWriter pw) {
        if (pw != null) {
            if (mEnabled) {
                ArraySet<Integer> toasts = new ArraySet();
                getToastWindows(toasts);
                pw.println("");
                pw.println("[ToastList]:" + toasts);
                return;
            }
            pw.println("AwareIntelligentRecg feature disabled.");
        }
    }

    public void dumpDefaultTts(PrintWriter pw) {
        if (pw != null) {
            if (!mEnabled) {
                pw.println("AwareIntelligentRecg feature disabled.");
            }
            pw.println("[dump Default TTS]");
            pw.println(this.mDefaultTTS);
        }
    }

    public void dumpDefaultAppType(PrintWriter pw) {
        if (pw != null) {
            if (!mEnabled) {
                pw.println("AwareIntelligentRecg feature disabled.");
            }
            pw.println("[dump Pay Pkg]");
            pw.println(this.mPayPkgList == null ? "None" : this.mPayPkgList);
            pw.println("[dump Share Pkg]");
            pw.println(this.mSharePkgList == null ? "None" : this.mSharePkgList);
        }
    }

    public void dumpFrozen(PrintWriter pw, int uid) {
        if (pw != null) {
            if (mEnabled) {
                pw.println("AwareIntelligentRecg dumpFrozen start");
                synchronized (this.mFrozenAppList) {
                    if (uid == 0) {
                        for (Integer AppUid : this.mFrozenAppList) {
                            pw.println("frozen app:" + InnerUtils.getPackageNameByUid(AppUid.intValue()) + ",uid:" + AppUid);
                        }
                    } else {
                        pw.println("uid:" + uid + ",frozen:" + isAppFrozen(uid));
                    }
                }
                return;
            }
            pw.println("AwareIntelligentRecg feature disabled.");
        }
    }

    public void dumpBluetooth(PrintWriter pw, int uid) {
        if (pw != null) {
            if (mEnabled) {
                pw.println("AwareIntelligentRecg dumpBluetooth start");
                synchronized (this.mBluetoothAppList) {
                    if (uid == 0) {
                        for (Integer AppUid : this.mBluetoothAppList) {
                            pw.println("bluetooth app:" + InnerUtils.getPackageNameByUid(AppUid.intValue()) + ",uid:" + AppUid);
                        }
                    } else {
                        pw.println("uid:" + uid + ",bluetooth:" + isAppBluetooth(uid));
                    }
                }
                return;
            }
            pw.println("AwareIntelligentRecg feature disabled.");
        }
    }

    public void dumpKbgApp(PrintWriter pw) {
        if (pw != null) {
            if (mEnabled) {
                pw.println("AwareIntelligentRecg dumpKbgApp start");
                synchronized (this.mStatusAudioIn) {
                    for (Integer AppUid : this.mStatusAudioIn) {
                        pw.println("mStatusAudioIn app:" + AppUid);
                    }
                }
                synchronized (this.mStatusAudioOut) {
                    for (Integer AppUid2 : this.mStatusAudioOut) {
                        pw.println("mStatusAudioOut app:" + AppUid2);
                    }
                }
                synchronized (this.mStatusGps) {
                    for (Integer AppUid22 : this.mStatusGps) {
                        pw.println("mStatusGps app:" + AppUid22);
                    }
                }
                synchronized (this.mStatusUpDown) {
                    for (Integer AppUid222 : this.mStatusUpDown) {
                        pw.println("mStatusUpDown app:" + AppUid222);
                    }
                }
                synchronized (this.mStatusSensor) {
                    for (Integer AppUid2222 : this.mStatusSensor) {
                        pw.println("mStatusSensor app:" + AppUid2222);
                    }
                }
                return;
            }
            pw.println("AwareIntelligentRecg feature disabled.");
        }
    }

    public void dumpAlarms(PrintWriter pw) {
        if (pw != null) {
            if (mEnabled) {
                pw.println("AwareIntelligentRecg dumpAlarms start");
                synchronized (this.mAlarmMap) {
                    for (Integer uid : this.mAlarmMap.keySet()) {
                        ArrayMap<String, AlarmInfo> alarms = (ArrayMap) this.mAlarmMap.get(uid);
                        if (alarms == null) {
                            return;
                        }
                        for (String tag : alarms.keySet()) {
                            pw.println("AwareIntelligentRecg alarm:" + alarms.get(tag));
                        }
                    }
                    return;
                }
            }
            pw.println("AwareIntelligentRecg feature disabled.");
        }
    }

    public void dumpAlarmActions(PrintWriter pw) {
        if (pw != null) {
            if (mEnabled) {
                pw.println("AwareIntelligentRecg dumpAlarmActions start");
                synchronized (this.mAlarmCmps) {
                    for (Entry value : this.mAlarmCmps.entrySet()) {
                        CmpTypeInfo info = (CmpTypeInfo) value.getValue();
                        pw.println("AwareIntelligentRecg alarm action:" + (info != null ? info.toString() : ""));
                    }
                }
                pw.println("Alarm White Pkg:");
                pw.println(this.mAlarmPkgList == null ? "None" : this.mAlarmPkgList);
                pw.println("UnRemind App Types:");
                pw.println(this.mUnRemindAppTypeList == null ? "None" : this.mUnRemindAppTypeList);
                return;
            }
            pw.println("AwareIntelligentRecg feature disabled.");
        }
    }

    public void dumpGmsCallerList(PrintWriter pw) {
        if (pw != null) {
            if (mEnabled) {
                pw.println("AwareIntelligentRecg dumpGmsCallerList start");
                pw.println("Gms UID:" + this.mGmsCallerAppUid);
                pw.println("Gms PKG:" + this.mGmsCallerAppPkg);
                return;
            }
            pw.println("AwareIntelligentRecg feature disabled.");
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
            if (this.mCallbacks.isEmpty()) {
                return;
            }
            int size = this.mCallbacks.size();
            for (int i = 0; i < size; i++) {
                ((IAwareToastCallback) this.mCallbacks.valueAt(i)).onToastWindowsChanged(type, pid);
            }
        }
    }

    public boolean isBtMediaBrowserCaller(int callerUid, String action) {
        if (callerUid == this.mBluetoothUid && BTMEDIABROWSER_ACTION.equals(action)) {
            return true;
        }
        return false;
    }

    public boolean isNotifyListenerCaller(int callerUid, String action, ProcessRecord callerApp) {
        boolean z = false;
        if (callerApp == null) {
            return false;
        }
        if (callerUid == 1000 && SYSTEM_PROCESS_NAME.equals(callerApp.processName)) {
            z = NOTIFY_LISTENER_ACTION.equals(action);
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
        for (String pkg : awareProcInfo.mProcInfo.mPackageName) {
            if (!isScreenChangedMeetCondition(pkg)) {
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

    /* JADX WARNING: Missing block: B:13:0x0026, code:
            r5 = r8.mAppChangeToBGTime;
     */
    /* JADX WARNING: Missing block: B:14:0x0028, code:
            monitor-enter(r5);
     */
    /* JADX WARNING: Missing block: B:17:0x002f, code:
            if (r8.mAppChangeToBGTime.containsKey(r9) != false) goto L_0x0036;
     */
    /* JADX WARNING: Missing block: B:18:0x0031, code:
            monitor-exit(r5);
     */
    /* JADX WARNING: Missing block: B:19:0x0032, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:24:?, code:
            r0 = ((java.lang.Long) r8.mAppChangeToBGTime.get(r9)).longValue();
     */
    /* JADX WARNING: Missing block: B:25:0x0042, code:
            monitor-exit(r5);
     */
    /* JADX WARNING: Missing block: B:27:0x0045, code:
            if (r2 >= r0) goto L_0x004b;
     */
    /* JADX WARNING: Missing block: B:28:0x0047, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:33:0x004c, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isScreenChangedMeetCondition(String pkg) {
        if (pkg == null) {
            return false;
        }
        synchronized (this.mScreenChangedTime) {
            if (AppMngConfig.getScreenChangedThreshold() > this.mScreenChangedTime.size()) {
                return false;
            }
            long screenChangedTime = ((Long) this.mScreenChangedTime.getFirst()).longValue();
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
        if (userId == currentUserId) {
            return true;
        }
        if (isCloned && currentUserId == 0) {
            return true;
        }
        return false;
    }

    public boolean isWebViewUid(int uid) {
        return uid == this.mWebViewUid;
    }

    private boolean shouldReserveAlarm(int uid, List<String> pkgList) {
        if (pkgList == null || pkgList.isEmpty()) {
            return false;
        }
        int userId = UserHandle.getUserId(uid);
        if (userId == 0) {
            return false;
        }
        if (this.mMtmService != null) {
            UserManager usm = UserManager.get(this.mMtmService.context());
            if (usm != null) {
                UserInfo info = usm.getUserInfo(userId);
                if (info != null ? info.isClonedProfile() : false) {
                    return false;
                }
            }
        }
        int size = pkgList.size();
        for (int i = 0; i < size; i++) {
            int type = AppTypeRecoManager.getInstance().getAppType((String) pkgList.get(i));
            if (type == 14 || type == 5 || type == 310) {
                return true;
            }
        }
        return false;
    }
}
