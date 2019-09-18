package com.android.server.mtm.iaware.srms;

import android.app.usage.UsageStatsManagerInternal;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ServiceManager;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import android.util.ArraySet;
import com.android.server.LocalServices;
import com.android.server.am.HwBroadcastRecord;
import com.android.server.mtm.MultiTaskManagerService;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.iaware.appmng.policy.BrFilterPolicy;
import com.android.server.mtm.iaware.brjob.controller.KeyWordController;
import com.android.server.mtm.taskstatus.ProcessInfoCollector;
import com.android.server.pfw.autostartup.comm.XmlConst;
import com.android.server.pm.PackageManagerService;
import com.android.server.rms.iaware.appmng.AwareAppKeyBackgroup;
import com.android.server.rms.iaware.appmng.AwareSceneRecognize;
import com.android.server.rms.iaware.feature.DevSchedFeatureRT;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.srms.BroadcastExFeature;
import com.android.server.rms.iaware.srms.BroadcastFeature;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class AwareBroadcastPolicy {
    private static final int APP_STATE_IDLE = 1;
    private static final int APP_STATE_NOT_IDLE = 0;
    private static final int BATTERY_DATA_FILTER = 1;
    private static final int BATTERY_DATA_NO_FILTER = 0;
    public static final String BATTERY_LEVEL_NAME = "BatteryLevel";
    public static final String BATTERY_STATUS_CHANGE_VALUE = "CHARGING_CHANGE";
    public static final String BATTERY_STATUS_CHARGINGOFF_VALUE = "CHARGING_OFF";
    public static final String BATTERY_STATUS_CHARGINGON_VALUE = "CHARGING_ON";
    public static final String BATTERY_STATUS_NAME = "BatteryStatus";
    private static final int BROADCAST_PROXY_SPEED_INDEX = 2;
    private static final int BROADCAST_PROXY_SPEED_INTERVAL = 100;
    private static final long BROADCAST_PROXY_SPEED_INTERVAL_LONG = 100;
    private static final int BROADCAST_PROXY_SPEED_NUMBER = 10;
    private static final int BROADCAST_PROXY_SPEED_NUMBER_INDEX_MAX = 9;
    public static final int CHARGING_OFF = 0;
    public static final int CHARGING_ON = 1;
    public static final String CONNECTIVITY_NAME = "ConnectStatus";
    public static final String CONNECT_STATUS_MOBILE_CON_VALUE = "MOBILEDATACON";
    public static final String CONNECT_STATUS_MOBILE_DSCON_VALUE = "MOBILEDATADSCON";
    public static final String CONNECT_STATUS_WIFI_CON_VALUE = "WIFIDATACON";
    public static final String CONNECT_STATUS_WIFI_DSCON_VALUE = "WIFIDATADSCON";
    public static final String DROP = "DROP";
    public static final String DROP_TYPE = "DropType";
    public static final String EXTRA_NAME = "Extra";
    private static final int GOOGLE_STATE_CONNECTED = 1;
    private static final int GOOGLE_STATE_DISCONNECTED = -1;
    private static final int IAWARE_APP_INSTALL_DELAY_TIME = 5000;
    private static final int IAWARE_DWONLOAD_DELAY_TIME = 3000;
    private static final int IAWARE_START_ACTIVITY_DELAY_TIME = 3000;
    public static final int INVALUED_DATA = -1;
    private static final int LOOPER_CHECK_TIME = 1000;
    private static final int LOOPER_CHECK_TIME_RECOUNT = 2000;
    public static final int MOBILE_CON = 1;
    public static final int MOBILE_DSCON = 2;
    private static final int MSG_APP_IDLE_STAT_CHANGE = 211;
    private static final int MSG_GOOGLE_CONN_STAT_CHANGE = 210;
    private static final int MSG_INSTALL_APP_TIMEOUT = 208;
    private static final int MSG_NOTIFY_OVERFLOW = 212;
    private static final int MSG_POLICY_DL_END = 202;
    private static final int MSG_POLICY_DL_START = 201;
    private static final int MSG_POLICY_END_CHECK = 206;
    private static final int MSG_POLICY_SCENE_ACTIVITY = 205;
    private static final int MSG_POLICY_SCENE_SLIP = 203;
    private static final int MSG_START_ACTIVITY_TIMEOUT = 207;
    private static final int MSG_UPDATE_BR_POLICY = 209;
    public static final String SCREEN_NAME = "ScreenStatus";
    public static final String SCREEN_STATUS_OFF = "SCREENOFF";
    public static final String SCREEN_STATUS_ON = "SCREENON";
    private static final String TAG = "AwareBroadcastPolicy";
    public static final int UNKNOW_CHARGING = -1;
    public static final int UNKNOW_CON = -1;
    public static final int WIFI_CON = 3;
    private static final int WIFI_DATA_DISCARD = 2;
    private static final int WIFI_DATA_FILTER = 1;
    private static final int WIFI_DATA_NO_FILTER = 0;
    public static final int WIFI_DSCON = 4;
    public static final String WIFI_NET_STATUS_NAME = "WifiNetStatus";
    public static final String WIFI_RSSI_STATUS_NAME = "WifiRssi";
    public static final String WIFI_STATUS_CONNECTING_VALUE = "WIFICONTING";
    public static final String WIFI_STATUS_CONNECT_VALUE = "WIFICON";
    public static final String WIFI_STATUS_DISABLED_VALUE = "WIFIDISABLED";
    public static final String WIFI_STATUS_DISCONNECT_VALUE = "WIFIDSCON";
    public static final String WIFI_STATUS_ENABLED_VALUE = "WIFIENABLED";
    public static final String WIFI_STATUS_NAME = "WifiStatus";
    public static final String WIFI_SUP_STATUS_COMPLET_VALUE = "WIFISUPCOMPLE";
    public static final String WIFI_SUP_STATUS_DISCONNECT_VALUE = "WIFISUPDSCON";
    public static final String WIFI_SUP_STATUS_NAME = "WifiSupStatus";
    private static boolean mGoogleConnStat = false;
    private static ArrayMap<String, AwareBroadcastCache> mIawareBrCaches = new ArrayMap<>();
    private AwareBroadcastConfig mAwareBroadcastConfig;
    private AwareSceneStateCallback mAwareSceneStateCallback;
    private AwareStateCallback mAwareStateCallback;
    /* access modifiers changed from: private */
    public int mBatteryLevel = -1;
    private AwareBroadcastProcess mBgIawareBr = null;
    /* access modifiers changed from: private */
    public int mCharging = -1;
    /* access modifiers changed from: private */
    public int mConnectStatus = -1;
    private Context mContext = null;
    private long mCountCheck = 0;
    private AwareBroadcastProcess mFgIawareBr = null;
    private int mForegroundAppLevel = 2;
    /* access modifiers changed from: private */
    public final IawareBroadcastPolicyHandler mHandler;
    /* access modifiers changed from: private */
    public ArraySet<Integer> mIawareDownloadingUid = new ArraySet<>();
    private ArrayMap<String, FilterStatus> mIawareFilterStatus = new ArrayMap<>();
    /* access modifiers changed from: private */
    public boolean mIawareInstallApp = false;
    private ArraySet<String> mIawareNoProxyActions = new ArraySet<>();
    private ArraySet<String> mIawareNoProxyPkgs = new ArraySet<>();
    /* access modifiers changed from: private */
    public boolean mIawareProxyActivitStart = false;
    private boolean mIawareProxySlip = false;
    private boolean mIawareScreenOn = true;
    private ArraySet<String> mIawareTrimActions = new ArraySet<>();
    private ArrayMap<String, ArraySet<String>> mIawareUnProxySys = new ArrayMap<>();
    private long mLastParallelBrTime = 0;
    private Object mLockNoProxyActions = new Object();
    private Object mLockNoProxyPkgs = new Object();
    private Object mLockTrimActions = new Object();
    private Object mLockUnProxySys = new Object();
    private int mNoTouchCheckCount = 200;
    /* access modifiers changed from: private */
    public int mPlugedtype = -1;
    /* access modifiers changed from: private */
    public int mPrePlugedtype = -1;
    private final long[][] mProxyCount = ((long[][]) Array.newInstance(long.class, new int[]{10, 2}));
    /* access modifiers changed from: private */
    public boolean mScreenOn = true;
    private boolean mSpeedParallelStartProxy = false;
    private long mStartParallelBrTime = 0;
    private int mTouchCheckCount = 60;
    private final UsageStatsManagerInternal mUsageStatsInternal;
    /* access modifiers changed from: private */
    public NetworkInfo.State mWifiNetStatue = NetworkInfo.State.UNKNOWN;
    /* access modifiers changed from: private */
    public int mWifiRssi = -127;
    /* access modifiers changed from: private */
    public int mWifiStatue = 4;
    /* access modifiers changed from: private */
    public SupplicantState mWifiSupStatue = SupplicantState.INVALID;

    /* renamed from: com.android.server.mtm.iaware.srms.AwareBroadcastPolicy$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$android$net$NetworkInfo$State = new int[NetworkInfo.State.values().length];
        static final /* synthetic */ int[] $SwitchMap$android$net$wifi$SupplicantState = new int[SupplicantState.values().length];

        static {
            try {
                $SwitchMap$android$net$wifi$SupplicantState[SupplicantState.COMPLETED.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$net$wifi$SupplicantState[SupplicantState.DISCONNECTED.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$android$net$wifi$SupplicantState[SupplicantState.INVALID.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$android$net$NetworkInfo$State[NetworkInfo.State.CONNECTED.ordinal()] = 1;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$android$net$NetworkInfo$State[NetworkInfo.State.DISCONNECTED.ordinal()] = 2;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$android$net$NetworkInfo$State[NetworkInfo.State.CONNECTING.ordinal()] = 3;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$android$net$NetworkInfo$State[NetworkInfo.State.UNKNOWN.ordinal()] = 4;
            } catch (NoSuchFieldError e7) {
            }
        }
    }

    private final class AppIdleStateChangeListener extends UsageStatsManagerInternal.AppIdleStateChangeListener {
        private AppIdleStateChangeListener() {
        }

        /* synthetic */ AppIdleStateChangeListener(AwareBroadcastPolicy x0, AnonymousClass1 x1) {
            this();
        }

        public void onAppIdleStateChanged(String packageName, int userId, boolean idle, int bucket, int reason) {
            Message msg = AwareBroadcastPolicy.this.mHandler.obtainMessage();
            msg.what = 211;
            if (idle) {
                msg.arg1 = 1;
            } else {
                msg.arg1 = 0;
            }
            msg.arg2 = userId;
            msg.obj = packageName;
            AwareBroadcastPolicy.this.mHandler.sendMessage(msg);
        }

        public void onParoleStateChanged(boolean isParoleOn) {
        }
    }

    private class AwareSceneStateCallback implements AwareSceneRecognize.IAwareSceneRecCallback {
        private AwareSceneStateCallback() {
        }

        /* synthetic */ AwareSceneStateCallback(AwareBroadcastPolicy x0, AnonymousClass1 x1) {
            this();
        }

        public void onStateChanged(int sceneType, int eventType, String pkg) {
            if (BroadcastFeature.isFeatureEnabled(10)) {
                if (sceneType == 2) {
                    Message msg = AwareBroadcastPolicy.this.mHandler.obtainMessage();
                    msg.what = 203;
                    msg.arg1 = eventType;
                    AwareBroadcastPolicy.this.mHandler.sendMessage(msg);
                } else if (sceneType == 4) {
                    Message msg2 = AwareBroadcastPolicy.this.mHandler.obtainMessage();
                    msg2.what = 205;
                    msg2.arg1 = eventType;
                    AwareBroadcastPolicy.this.mHandler.sendMessage(msg2);
                    if (AwareBroadcastPolicy.this.mHandler.hasMessages(207)) {
                        AwareBroadcastPolicy.this.mHandler.removeMessages(207);
                    }
                    if (eventType == 1) {
                        AwareBroadcastPolicy.this.mHandler.sendEmptyMessageDelayed(207, 3000);
                    }
                } else if (AwareBroadcastDebug.getDebugDetail()) {
                    AwareLog.d(AwareBroadcastPolicy.TAG, "don't process scene type " + sceneType);
                }
            }
        }
    }

    private class AwareStateCallback implements AwareAppKeyBackgroup.IAwareStateCallback {
        private AwareStateCallback() {
        }

        /* synthetic */ AwareStateCallback(AwareBroadcastPolicy x0, AnonymousClass1 x1) {
            this();
        }

        public void onStateChanged(int stateType, int eventType, int pid, int uid) {
            AwareBroadcastPolicy.this.updateProcessPolicy(stateType, eventType, pid, uid);
            if (BroadcastFeature.isFeatureEnabled(10) && stateType == 5 && uid >= 0) {
                if (eventType == 1) {
                    Message msg = AwareBroadcastPolicy.this.mHandler.obtainMessage();
                    msg.what = 201;
                    msg.arg1 = uid;
                    AwareBroadcastPolicy.this.mHandler.sendMessage(msg);
                } else if (eventType == 2) {
                    Message msg2 = AwareBroadcastPolicy.this.mHandler.obtainMessage();
                    msg2.what = 202;
                    msg2.arg1 = uid;
                    AwareBroadcastPolicy.this.mHandler.sendMessageDelayed(msg2, 3000);
                } else if (AwareBroadcastDebug.getDebugDetail()) {
                    AwareLog.d(AwareBroadcastPolicy.TAG, "don't process type " + eventType);
                }
            }
        }
    }

    private class BatteryFilterStatus extends FilterStatus {
        private BatteryFilterStatus() {
            super(AwareBroadcastPolicy.this, null);
        }

        /* synthetic */ BatteryFilterStatus(AwareBroadcastPolicy x0, AnonymousClass1 x1) {
            this();
        }

        /* JADX WARNING: Removed duplicated region for block: B:17:0x0039 A[RETURN] */
        /* JADX WARNING: Removed duplicated region for block: B:18:0x003a  */
        /* JADX WARNING: Removed duplicated region for block: B:24:0x004d  */
        /* JADX WARNING: Removed duplicated region for block: B:30:0x0060  */
        public boolean filter(Intent intent, String filterValue) {
            char c;
            int hashCode = filterValue.hashCode();
            if (hashCode == -992200083) {
                if (filterValue.equals(AwareBroadcastPolicy.BATTERY_STATUS_CHARGINGON_VALUE)) {
                    c = 1;
                    switch (c) {
                        case 0:
                            break;
                        case 1:
                            break;
                        case 2:
                            break;
                    }
                }
            } else if (hashCode == -693431679) {
                if (filterValue.equals(AwareBroadcastPolicy.BATTERY_STATUS_CHARGINGOFF_VALUE)) {
                    c = 2;
                    switch (c) {
                        case 0:
                            break;
                        case 1:
                            break;
                        case 2:
                            break;
                    }
                }
            } else if (hashCode == 427770174 && filterValue.equals(AwareBroadcastPolicy.BATTERY_STATUS_CHANGE_VALUE)) {
                c = 0;
                switch (c) {
                    case 0:
                        return AwareBroadcastPolicy.this.mPrePlugedtype == AwareBroadcastPolicy.this.mPlugedtype && AwareBroadcastPolicy.this.mPrePlugedtype != -1;
                    case 1:
                        return (AwareBroadcastPolicy.this.mCharging == 1 || AwareBroadcastPolicy.this.mCharging == -1) ? false : true;
                    case 2:
                        return (AwareBroadcastPolicy.this.mCharging == 0 || AwareBroadcastPolicy.this.mCharging == -1) ? false : true;
                    default:
                        return true;
                }
            }
            c = 65535;
            switch (c) {
                case 0:
                    break;
                case 1:
                    break;
                case 2:
                    break;
            }
        }
    }

    private class BatteryLevelFilterStatus extends FilterStatus {
        private BatteryLevelFilterStatus() {
            super(AwareBroadcastPolicy.this, null);
        }

        /* synthetic */ BatteryLevelFilterStatus(AwareBroadcastPolicy x0, AnonymousClass1 x1) {
            this();
        }

        public boolean filter(Intent intent, String filterValue) {
            try {
                String[] values = filterValue.split(":");
                if (values.length != 2) {
                    return false;
                }
                int minLevel = Integer.parseInt(values[0]);
                int maxLevel = Integer.parseInt(values[1]);
                if (minLevel >= maxLevel) {
                    return false;
                }
                if (AwareBroadcastPolicy.this.mBatteryLevel < minLevel || AwareBroadcastPolicy.this.mBatteryLevel > maxLevel) {
                    return true;
                }
                return false;
            } catch (NumberFormatException e) {
                AwareLog.e(AwareBroadcastPolicy.TAG, "iaware_br level value format is error");
                return false;
            }
        }
    }

    public enum BrCtrlType {
        NONE("do-nothing"),
        CACHEBR("cachebr"),
        DISCARDBR("discardbr"),
        DATADEFAULTBR("datadefaultbr");
        
        String mDescription;

        private BrCtrlType(String description) {
            this.mDescription = description;
        }

        public String description() {
            return this.mDescription;
        }
    }

    private class ConnectivityFilterStatus extends FilterStatus {
        private ConnectivityFilterStatus() {
            super(AwareBroadcastPolicy.this, null);
        }

        /* synthetic */ ConnectivityFilterStatus(AwareBroadcastPolicy x0, AnonymousClass1 x1) {
            this();
        }

        public boolean filter(Intent intent, String filterValue) {
            int access$3300 = AwareBroadcastPolicy.this.mConnectStatus;
            if (access$3300 == -1) {
                return false;
            }
            switch (access$3300) {
                case 1:
                    return !"MOBILEDATACON".equals(filterValue);
                case 2:
                    return !"MOBILEDATADSCON".equals(filterValue);
                case 3:
                    return !AwareBroadcastPolicy.CONNECT_STATUS_WIFI_CON_VALUE.equals(filterValue);
                case 4:
                    return !AwareBroadcastPolicy.CONNECT_STATUS_WIFI_DSCON_VALUE.equals(filterValue);
                default:
                    return true;
            }
        }
    }

    private class DropFilterStatus extends FilterStatus {
        private DropFilterStatus() {
            super(AwareBroadcastPolicy.this, null);
        }

        /* synthetic */ DropFilterStatus(AwareBroadcastPolicy x0, AnonymousClass1 x1) {
            this();
        }

        public boolean filter(Intent intent, String filterValue) {
            if (AwareBroadcastPolicy.DROP.equals(filterValue)) {
                return true;
            }
            return false;
        }
    }

    private class ExtraFilterStatus extends FilterStatus {
        private ExtraFilterStatus() {
            super(AwareBroadcastPolicy.this, null);
        }

        /* synthetic */ ExtraFilterStatus(AwareBroadcastPolicy x0, AnonymousClass1 x1) {
            this();
        }

        public boolean filter(Intent intent, String filterValue) {
            String[] extras = filterValue.split("[\\[\\]]");
            for (int i = 0; i < extras.length; i++) {
                if (!(extras[i] == null || extras[i].trim().length() == 0 || ":".equals(extras[i].trim()))) {
                    if (AwareBroadcastDebug.getDebug()) {
                        AwareLog.i(AwareBroadcastPolicy.TAG, "iaware_br compare extra: " + extras[i]);
                    }
                    String[] values = extras[i].split("[:@]");
                    if (values.length != 3) {
                        AwareLog.e(AwareBroadcastPolicy.TAG, "iaware_br extra value length is wrong.");
                        return false;
                    } else if (KeyWordController.matchReg(values[0], values[1], values[2], intent)) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    private abstract class FilterStatus {
        public abstract boolean filter(Intent intent, String str);

        private FilterStatus() {
        }

        /* synthetic */ FilterStatus(AwareBroadcastPolicy x0, AnonymousClass1 x1) {
            this();
        }
    }

    private final class IawareBroadcastPolicyHandler extends Handler {
        public IawareBroadcastPolicyHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 201:
                    synchronized (AwareBroadcastPolicy.this.mIawareDownloadingUid) {
                        AwareBroadcastPolicy.this.mIawareDownloadingUid.add(Integer.valueOf(msg.arg1));
                    }
                    return;
                case 202:
                    synchronized (AwareBroadcastPolicy.this.mIawareDownloadingUid) {
                        AwareBroadcastPolicy.this.mIawareDownloadingUid.remove(Integer.valueOf(msg.arg1));
                    }
                    return;
                case 203:
                    AwareBroadcastPolicy.this.setIawarePolicy(2, msg.arg1);
                    return;
                case 205:
                    AwareBroadcastPolicy.this.setIawarePolicy(4, msg.arg1);
                    return;
                case 206:
                    AwareBroadcastPolicy.this.startUnproxyBroadcast();
                    return;
                case 207:
                    boolean unused = AwareBroadcastPolicy.this.mIawareProxyActivitStart = false;
                    return;
                case 208:
                    boolean unused2 = AwareBroadcastPolicy.this.mIawareInstallApp = false;
                    return;
                case 209:
                    AwareBroadcastPolicy.this.updateBrPolicy(msg.arg1, msg.obj);
                    return;
                case 210:
                    AwareBroadcastPolicy.this.resetGoogleConnStat(msg.arg1);
                    return;
                case 211:
                    AwareBroadcastPolicy.this.updateAppIdleStat(msg.arg1, msg.arg2, (String) msg.obj);
                    return;
                case 212:
                    AwareBroadcastPolicy.this.unproxyCacheBr(msg.arg1);
                    return;
                default:
                    return;
            }
        }
    }

    private class ScreenFilterStatus extends FilterStatus {
        private ScreenFilterStatus() {
            super(AwareBroadcastPolicy.this, null);
        }

        /* synthetic */ ScreenFilterStatus(AwareBroadcastPolicy x0, AnonymousClass1 x1) {
            this();
        }

        public boolean filter(Intent intent, String filterValue) {
            if (AwareBroadcastPolicy.SCREEN_STATUS_ON.equals(filterValue)) {
                return !AwareBroadcastPolicy.this.mScreenOn;
            }
            if (AwareBroadcastPolicy.SCREEN_STATUS_OFF.equals(filterValue)) {
                return AwareBroadcastPolicy.this.mScreenOn;
            }
            return true;
        }
    }

    private class WifiFilterStatus extends FilterStatus {
        private WifiFilterStatus() {
            super(AwareBroadcastPolicy.this, null);
        }

        /* synthetic */ WifiFilterStatus(AwareBroadcastPolicy x0, AnonymousClass1 x1) {
            this();
        }

        public boolean filter(Intent intent, String filterValue) {
            int access$2600 = AwareBroadcastPolicy.this.mWifiStatue;
            if (access$2600 == 1) {
                return !AwareBroadcastPolicy.WIFI_STATUS_DISABLED_VALUE.equals(filterValue);
            }
            switch (access$2600) {
                case 3:
                    return !AwareBroadcastPolicy.WIFI_STATUS_ENABLED_VALUE.equals(filterValue);
                case 4:
                    return false;
                default:
                    return true;
            }
        }
    }

    private class WifiNetFilterStatus extends FilterStatus {
        private WifiNetFilterStatus() {
            super(AwareBroadcastPolicy.this, null);
        }

        /* synthetic */ WifiNetFilterStatus(AwareBroadcastPolicy x0, AnonymousClass1 x1) {
            this();
        }

        public boolean filter(Intent intent, String filterValue) {
            switch (AnonymousClass1.$SwitchMap$android$net$NetworkInfo$State[AwareBroadcastPolicy.this.mWifiNetStatue.ordinal()]) {
                case 1:
                    return !"WIFICON".equals(filterValue);
                case 2:
                    return !"WIFIDSCON".equals(filterValue);
                case 3:
                    return !AwareBroadcastPolicy.WIFI_STATUS_CONNECTING_VALUE.equals(filterValue);
                case 4:
                    return false;
                default:
                    return true;
            }
        }
    }

    private class WifiRssiFilterStatus extends FilterStatus {
        private WifiRssiFilterStatus() {
            super(AwareBroadcastPolicy.this, null);
        }

        /* synthetic */ WifiRssiFilterStatus(AwareBroadcastPolicy x0, AnonymousClass1 x1) {
            this();
        }

        public boolean filter(Intent intent, String filterValue) {
            try {
                String[] values = filterValue.split(":");
                if (values.length != 2) {
                    return false;
                }
                int minRssi = Integer.parseInt(values[0]);
                int maxRssi = Integer.parseInt(values[1]);
                if (minRssi >= maxRssi) {
                    return false;
                }
                if (AwareBroadcastPolicy.this.mWifiRssi > maxRssi || AwareBroadcastPolicy.this.mWifiRssi < minRssi) {
                    return true;
                }
                return false;
            } catch (NumberFormatException e) {
                AwareLog.e(AwareBroadcastPolicy.TAG, "iaware_br rssi value format is error");
                return false;
            }
        }
    }

    private class WifiSupFilterStatus extends FilterStatus {
        private WifiSupFilterStatus() {
            super(AwareBroadcastPolicy.this, null);
        }

        /* synthetic */ WifiSupFilterStatus(AwareBroadcastPolicy x0, AnonymousClass1 x1) {
            this();
        }

        public boolean filter(Intent intent, String filterValue) {
            switch (AnonymousClass1.$SwitchMap$android$net$wifi$SupplicantState[AwareBroadcastPolicy.this.mWifiSupStatue.ordinal()]) {
                case 1:
                    return !AwareBroadcastPolicy.WIFI_SUP_STATUS_COMPLET_VALUE.equals(filterValue);
                case 2:
                    return !AwareBroadcastPolicy.WIFI_SUP_STATUS_DISCONNECT_VALUE.equals(filterValue);
                case 3:
                    return false;
                default:
                    return true;
            }
        }
    }

    public AwareBroadcastPolicy(Handler handler, Context context) {
        this.mContext = context;
        this.mBgIawareBr = new AwareBroadcastProcess(this, handler, "iawarebackground");
        this.mFgIawareBr = new AwareBroadcastProcess(this, handler, "iawareforeground");
        this.mHandler = new IawareBroadcastPolicyHandler(handler.getLooper());
        this.mAwareBroadcastConfig = AwareBroadcastConfig.getInstance();
        this.mIawareFilterStatus.put(WIFI_NET_STATUS_NAME, new WifiNetFilterStatus(this, null));
        this.mIawareFilterStatus.put("WifiStatus", new WifiFilterStatus(this, null));
        this.mIawareFilterStatus.put(WIFI_SUP_STATUS_NAME, new WifiSupFilterStatus(this, null));
        this.mIawareFilterStatus.put(WIFI_RSSI_STATUS_NAME, new WifiRssiFilterStatus(this, null));
        this.mIawareFilterStatus.put(BATTERY_STATUS_NAME, new BatteryFilterStatus(this, null));
        this.mIawareFilterStatus.put(BATTERY_LEVEL_NAME, new BatteryLevelFilterStatus(this, null));
        this.mIawareFilterStatus.put(CONNECTIVITY_NAME, new ConnectivityFilterStatus(this, null));
        this.mIawareFilterStatus.put("Extra", new ExtraFilterStatus(this, null));
        this.mIawareFilterStatus.put(SCREEN_NAME, new ScreenFilterStatus(this, null));
        this.mIawareFilterStatus.put(DROP_TYPE, new DropFilterStatus(this, null));
        initState();
        this.mUsageStatsInternal = (UsageStatsManagerInternal) LocalServices.getService(UsageStatsManagerInternal.class);
        this.mUsageStatsInternal.addAppIdleStateChangeListener(new AppIdleStateChangeListener(this, null));
    }

    public void init() {
        this.mAwareStateCallback = new AwareStateCallback(this, null);
        AwareAppKeyBackgroup.getInstance().registerStateCallback(this.mAwareStateCallback, 5);
        AwareAppKeyBackgroup.getInstance().registerStateCallback(this.mAwareStateCallback, 1);
        AwareAppKeyBackgroup.getInstance().registerStateCallback(this.mAwareStateCallback, 2);
        AwareAppKeyBackgroup.getInstance().registerStateCallback(this.mAwareStateCallback, 3);
        AwareAppKeyBackgroup.getInstance().registerStateCallback(this.mAwareStateCallback, 4);
        this.mAwareSceneStateCallback = new AwareSceneStateCallback(this, null);
        AwareSceneRecognize.getInstance().registerStateCallback(this.mAwareSceneStateCallback, 1);
        this.mAwareBroadcastConfig.doinit();
    }

    public static void initBrCache(String name, Object ams) {
        if (!mIawareBrCaches.containsKey(name)) {
            mIawareBrCaches.put(name, new AwareBroadcastCache(name, ams));
        }
    }

    public boolean enqueueIawareProxyBroacast(boolean isParallel, HwBroadcastRecord r) {
        if (r == null) {
            return false;
        }
        if (r.isBg()) {
            this.mBgIawareBr.enqueueIawareProxyBroacast(isParallel, r);
        } else {
            this.mFgIawareBr.enqueueIawareProxyBroacast(isParallel, r);
        }
        return true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x003b, code lost:
        if (isForbidProxy(r6, r10) == false) goto L_0x003e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x003d, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0042, code lost:
        if (isIawarePrepared() != false) goto L_0x0045;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0044, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0046, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0035, code lost:
        return false;
     */
    public boolean shouldIawareProxyBroadcast(String brAction, int callingPid, int receiverUid, int receiverPid, String recevierPkg) {
        synchronized (this.mIawareDownloadingUid) {
            if (this.mIawareDownloadingUid.contains(Integer.valueOf(receiverUid))) {
                if (AwareBroadcastDebug.getDebugDetail()) {
                    AwareLog.d(TAG, "iaware_br : downloading, don't proxy : " + recevierPkg + ": action : " + brAction);
                }
            }
        }
    }

    private boolean isIawarePrepared() {
        if (this.mFgIawareBr == null || this.mBgIawareBr == null) {
            return false;
        }
        return true;
    }

    private boolean isForbidProxy(String action, String pkg) {
        boolean noProxyAction;
        boolean noProxyPkg;
        synchronized (this.mLockNoProxyActions) {
            noProxyAction = this.mIawareNoProxyActions.contains(action);
        }
        if (noProxyAction) {
            return true;
        }
        synchronized (this.mLockNoProxyPkgs) {
            noProxyPkg = this.mIawareNoProxyPkgs.contains(pkg);
        }
        return noProxyPkg;
    }

    public boolean isProxyedAllowedCondition() {
        return this.mIawareScreenOn && this.mSpeedParallelStartProxy;
    }

    public boolean isNotProxySysPkg(String pkg, String action) {
        synchronized (this.mLockUnProxySys) {
            ArraySet<String> actions = this.mIawareUnProxySys.get(pkg);
            if (actions == null) {
                return false;
            }
            boolean contains = actions.contains(action);
            return contains;
        }
    }

    public boolean isTrimAction(String action) {
        boolean contains;
        synchronized (this.mLockTrimActions) {
            contains = this.mIawareTrimActions.contains(action);
        }
        return contains;
    }

    public void updateXmlConfig() {
        if (AwareBroadcastDebug.getDebugDetail()) {
            AwareLog.i(TAG, "updateXmlConfig begin");
        }
        if (!isIawarePrepared()) {
            AwareLog.e(TAG, "iaware process broacast don't prepared.");
            return;
        }
        synchronized (this.mLockNoProxyActions) {
            this.mIawareNoProxyActions = this.mAwareBroadcastConfig.getUnProxyActionList();
        }
        synchronized (this.mLockNoProxyPkgs) {
            this.mIawareNoProxyPkgs = this.mAwareBroadcastConfig.getUnProxyPkgList();
        }
        synchronized (this.mLockUnProxySys) {
            this.mIawareUnProxySys = this.mAwareBroadcastConfig.getUnProxySysList();
        }
        synchronized (this.mLockTrimActions) {
            this.mIawareTrimActions = this.mAwareBroadcastConfig.getTrimActionList();
        }
        this.mForegroundAppLevel = this.mAwareBroadcastConfig.getFGAppLevel();
        this.mNoTouchCheckCount = this.mAwareBroadcastConfig.getNoTouchCheckCount();
        this.mTouchCheckCount = this.mAwareBroadcastConfig.getTouchCheckCount();
        this.mBgIawareBr.setUnProxyMaxDuration(this.mAwareBroadcastConfig.getUnProxyMaxDuration());
        this.mBgIawareBr.setUnProxyMaxSpeed(this.mAwareBroadcastConfig.getUnProxyMaxSpeed());
        this.mBgIawareBr.setUnProxyMinSpeed(this.mAwareBroadcastConfig.getUnProxyMinSpeed());
        this.mBgIawareBr.setUnProxyMiddleSpeed(this.mAwareBroadcastConfig.getUnProxyMiddleSpeed());
        this.mBgIawareBr.setUnProxyHighSpeed(this.mAwareBroadcastConfig.getUnProxyHighSpeed());
        this.mFgIawareBr.setUnProxyMaxDuration(this.mAwareBroadcastConfig.getUnProxyMaxDuration());
        this.mFgIawareBr.setUnProxyMaxSpeed(this.mAwareBroadcastConfig.getUnProxyMaxSpeed());
        this.mFgIawareBr.setUnProxyMinSpeed(this.mAwareBroadcastConfig.getUnProxyMinSpeed());
        this.mFgIawareBr.setUnProxyMiddleSpeed(this.mAwareBroadcastConfig.getUnProxyMiddleSpeed());
        this.mFgIawareBr.setUnProxyHighSpeed(this.mAwareBroadcastConfig.getUnProxyHighSpeed());
    }

    public void iawareStartCountBroadcastSpeed(boolean isParallel, long dispatchClockTime, int size) {
        if (this.mIawareScreenOn && !this.mSpeedParallelStartProxy && isIawarePrepared() && isParallel) {
            checkParallCount(dispatchClockTime, size);
        }
    }

    public void endCheckCount() {
        if (isIawarePrepared()) {
            Message msg = this.mHandler.obtainMessage();
            msg.what = 206;
            this.mHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    public void startUnproxyBroadcast() {
        if (isEmptyIawareBrList()) {
            this.mSpeedParallelStartProxy = false;
            return;
        }
        this.mBgIawareBr.starUnproxyBroadcast();
        this.mFgIawareBr.starUnproxyBroadcast();
    }

    private void checkParallCount(long dispatchClockTime, int size) {
        long j = dispatchClockTime;
        int i = size;
        this.mCountCheck = 0;
        int i2 = (this.mStartParallelBrTime > 0 ? 1 : (this.mStartParallelBrTime == 0 ? 0 : -1));
        int i3 = 10;
        long j2 = BROADCAST_PROXY_SPEED_INTERVAL_LONG;
        int tempIndex = 0;
        if (i2 == 0) {
            long[] jArr = this.mProxyCount[0];
            this.mStartParallelBrTime = j;
            jArr[0] = j;
            long[] jArr2 = this.mProxyCount[0];
            long j3 = (long) i;
            this.mCountCheck = j3;
            jArr2[1] = j3;
            for (int index = 1; index < 10; index++) {
                this.mProxyCount[index][0] = this.mStartParallelBrTime + (((long) index) * BROADCAST_PROXY_SPEED_INTERVAL_LONG);
                this.mProxyCount[index][1] = 0;
            }
            setProxyCount();
            return;
        }
        this.mLastParallelBrTime = j;
        long tempPeriod = this.mLastParallelBrTime - this.mStartParallelBrTime;
        if (tempPeriod < 0) {
            if (AwareBroadcastDebug.getDebugDetail()) {
                AwareLog.d(TAG, "iaware_br checkcount <0");
            }
            this.mStartParallelBrTime = 0;
            checkParallCount(dispatchClockTime, size);
        }
        if (tempPeriod >= 2000) {
            if (AwareBroadcastDebug.getDebugDetail()) {
                AwareLog.d(TAG, "iaware_br checkcount >2000");
            }
            this.mStartParallelBrTime = 0;
            checkParallCount(dispatchClockTime, size);
        } else if (tempPeriod >= 0 && tempPeriod < 1000) {
            int index2 = (int) (tempPeriod / BROADCAST_PROXY_SPEED_INTERVAL_LONG);
            this.mProxyCount[index2][1] = this.mProxyCount[index2][1] + ((long) i);
            while (true) {
                int tempIndex2 = tempIndex;
                if (tempIndex2 <= index2) {
                    this.mCountCheck += this.mProxyCount[tempIndex2][1];
                    tempIndex = tempIndex2 + 1;
                } else {
                    setProxyCount();
                    return;
                }
            }
        } else if (tempPeriod >= 1000) {
            int index3 = (int) ((tempPeriod - 1000) / BROADCAST_PROXY_SPEED_INTERVAL_LONG);
            int i4 = 9;
            if (index3 == 9) {
                this.mStartParallelBrTime = 0;
                checkParallCount(dispatchClockTime, size);
            } else if (index3 < 9) {
                this.mStartParallelBrTime = this.mProxyCount[index3 + 1][0];
                int rIndex = index3;
                int tempIndex3 = 0;
                while (tempIndex3 < i3) {
                    rIndex++;
                    if (rIndex < i3) {
                        this.mProxyCount[tempIndex3][0] = this.mProxyCount[rIndex][0];
                        this.mProxyCount[tempIndex3][1] = this.mProxyCount[rIndex][1];
                    } else if (tempIndex3 < i4) {
                        this.mProxyCount[tempIndex3][0] = this.mProxyCount[tempIndex3 - 1][0] + j2;
                        this.mProxyCount[tempIndex3][1] = 0;
                    } else {
                        this.mProxyCount[tempIndex3][0] = this.mProxyCount[tempIndex3 - 1][0] + j2;
                        this.mProxyCount[tempIndex3][1] = (long) i;
                    }
                    tempIndex3++;
                    i3 = 10;
                    j2 = BROADCAST_PROXY_SPEED_INTERVAL_LONG;
                    i4 = 9;
                }
                while (true) {
                    int countIndex = tempIndex;
                    if (countIndex < 10) {
                        this.mCountCheck += this.mProxyCount[countIndex][1];
                        tempIndex = countIndex + 1;
                    } else {
                        setProxyCount();
                        return;
                    }
                }
            }
        }
    }

    private void setProxyCount() {
        if (isStrictCondition()) {
            if (this.mCountCheck > ((long) this.mTouchCheckCount)) {
                this.mSpeedParallelStartProxy = true;
            }
        } else if (this.mCountCheck > ((long) this.mNoTouchCheckCount)) {
            this.mSpeedParallelStartProxy = true;
        }
    }

    public void reportSysEvent(int event, int eventType) {
        if (event == 15016) {
            if (AwareBroadcastDebug.getDebugDetail()) {
                AwareLog.i(TAG, "iaware_br install app: " + eventType);
            }
            if (eventType == 0) {
                this.mIawareInstallApp = true;
            } else if (eventType == 1) {
                this.mIawareInstallApp = false;
            } else {
                return;
            }
            if (this.mHandler.hasMessages(208)) {
                this.mHandler.removeMessages(208);
            }
            if (eventType == 0) {
                this.mHandler.sendEmptyMessageDelayed(208, 5000);
            }
        } else if (event == 20011) {
            if (AwareBroadcastDebug.getDebugDetail()) {
                AwareLog.d(TAG, "iaware_br dev status event screen on");
            }
            this.mIawareScreenOn = true;
        } else if (event == 90011) {
            if (AwareBroadcastDebug.getDebugDetail()) {
                AwareLog.d(TAG, "iaware_br dev status event screen off");
            }
            this.mIawareScreenOn = false;
            resetUnproxySpeedScreenOff();
        }
    }

    private void resetUnproxySpeedScreenOff() {
        if (isIawarePrepared()) {
            this.mBgIawareBr.setUnProxySpeedScreenOff();
            this.mFgIawareBr.setUnProxySpeedScreenOff();
        }
    }

    /* access modifiers changed from: private */
    public void setIawarePolicy(int type, int event) {
        if (type != 2) {
            if (type == 4) {
                if (event == 1) {
                    this.mIawareProxyActivitStart = true;
                } else if (event == 0) {
                    this.mIawareProxyActivitStart = false;
                } else if (AwareBroadcastDebug.getDebugDetail()) {
                    AwareLog.d(TAG, "don't process event " + event);
                }
            }
        } else if (event == 1) {
            this.mIawareProxySlip = true;
        } else if (event == 0) {
            this.mIawareProxySlip = false;
        } else if (AwareBroadcastDebug.getDebugDetail()) {
            AwareLog.d(TAG, "don't process event " + event);
        }
    }

    public boolean isSpeedNoCtrol() {
        return !isStrictCondition();
    }

    public boolean isScreenOff() {
        return !this.mIawareScreenOn;
    }

    private boolean isStrictCondition() {
        if (this.mIawareProxySlip || this.mIawareProxyActivitStart) {
            return true;
        }
        return false;
    }

    public boolean isEmptyIawareBrList() {
        return this.mBgIawareBr.getIawareBrSize() == 0 && this.mFgIawareBr.getIawareBrSize() == 0;
    }

    public void setStartProxy(boolean startProxy) {
        this.mSpeedParallelStartProxy = startProxy;
    }

    public boolean getStartProxy() {
        return this.mSpeedParallelStartProxy;
    }

    public void dumpIawareBr(PrintWriter pw) {
        pw.println("    feature enable :" + BroadcastFeature.isFeatureEnabled(10));
        synchronized (this.mLockNoProxyActions) {
            pw.println("    Default no proxy actions :" + this.mIawareNoProxyActions);
        }
        synchronized (this.mLockNoProxyPkgs) {
            pw.println("    Default no proxy pkgs :" + this.mIawareNoProxyPkgs);
        }
        synchronized (this.mLockUnProxySys) {
            pw.println("    Default unproxy sys :" + this.mIawareUnProxySys);
        }
        synchronized (this.mLockTrimActions) {
            pw.println("    Default trim action :" + this.mIawareTrimActions);
        }
        pw.println("    fg app level :" + this.mForegroundAppLevel);
        pw.println("    The receiver speed :" + this.mCountCheck);
        ArraySet<Integer> iawareDownloadingUid = new ArraySet<>();
        ArrayList arrayList = new ArrayList();
        synchronized (this.mIawareDownloadingUid) {
            iawareDownloadingUid.addAll(this.mIawareDownloadingUid);
        }
        if (iawareDownloadingUid.size() > 0) {
            PackageManagerService pms = ServiceManager.getService("package");
            Iterator<Integer> it = iawareDownloadingUid.iterator();
            while (it.hasNext()) {
                String name = pms.getNameForUid(it.next().intValue());
                if (name != null) {
                    arrayList.add(name);
                }
            }
        }
        pw.println("    App Downloading:" + arrayList);
        StringBuilder sb = new StringBuilder();
        sb.append("    Screen:");
        sb.append(this.mIawareScreenOn ? XmlConst.PreciseIgnore.COMP_SCREEN_ON_VALUE_ : "off");
        pw.println(sb.toString());
        StringBuilder sb2 = new StringBuilder();
        sb2.append("    Operation: [");
        sb2.append(this.mIawareProxySlip ? "slip" : "");
        sb2.append(" ");
        sb2.append(this.mIawareProxyActivitStart ? "activityStart" : "");
        sb2.append("]");
        pw.println(sb2.toString());
        pw.println("    Proxy info:");
        this.mBgIawareBr.dump(pw);
        this.mFgIawareBr.dump(pw);
    }

    public void notifyIawareUnproxyBr(int pid, int uid) {
        if (isIawarePrepared()) {
            this.mBgIawareBr.startUnproxyFgAppBroadcast(pid, uid);
            this.mFgIawareBr.startUnproxyFgAppBroadcast(pid, uid);
        }
    }

    public int getForegroundAppLevel() {
        return this.mForegroundAppLevel;
    }

    public boolean isInstallApp() {
        return this.mIawareInstallApp;
    }

    public boolean assemFilterBr(Intent intent, IntentFilter filter) {
        if (intent == null || filter == null) {
            return false;
        }
        boolean realFilter = false;
        Iterator<IntentFilter.ActionFilterEntry> it = filter.actionFilterIterator();
        if (it != null) {
            while (it.hasNext()) {
                IntentFilter.ActionFilterEntry actionFilter = it.next();
                if (actionFilter.getAction() != null && actionFilter.getAction().equals(intent.getAction())) {
                    String filterName = actionFilter.getFilterName();
                    String filterValue = actionFilter.getFilterValue();
                    if (!(filterName == null || filterValue == null)) {
                        FilterStatus filterStatus = this.mIawareFilterStatus.get(filterName);
                        if (filterStatus != null) {
                            realFilter = filterStatus.filter(intent, filterValue);
                            if (AwareBroadcastDebug.getFilterDebug()) {
                                AwareLog.i(TAG, "iaware_br, filterValue: " + filterValue + ", realFilter : " + realFilter + ", id:" + filter);
                            }
                            if (!realFilter) {
                                return realFilter;
                            }
                        } else {
                            continue;
                        }
                    }
                } else if (AwareBroadcastDebug.getFilterDebug()) {
                    AwareLog.w(TAG, "iaware_br, action not match. ");
                }
            }
        }
        return realFilter;
    }

    private NetworkInfo.State getWifiNetworkStatus(Intent intent) {
        NetworkInfo info = (NetworkInfo) intent.getParcelableExtra("networkInfo");
        if (info != null) {
            return info.getState();
        }
        return NetworkInfo.State.UNKNOWN;
    }

    private int getConnectNetworkStatus(Intent intent) {
        int type = intent.getIntExtra("networkType", -1);
        if (type != 0 && type != 1) {
            return -1;
        }
        NetworkInfo info = (NetworkInfo) intent.getParcelableExtra("networkInfo");
        if (info == null) {
            return -1;
        }
        if (type == 0) {
            if (info.isConnected()) {
                return 1;
            }
            return 2;
        } else if (info.isConnected()) {
            return 3;
        } else {
            return 4;
        }
    }

    private int getWifiStatus(Intent intent) {
        return intent.getIntExtra("wifi_state", 4);
    }

    private int getWifiRssi(Intent intent) {
        return intent.getIntExtra("newRssi", -127);
    }

    private SupplicantState getWifiSupStatus(Intent intent) {
        SupplicantState state = (SupplicantState) intent.getParcelableExtra("newState");
        if (state != null) {
            return state;
        }
        return SupplicantState.INVALID;
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    public void getStateFromSendBr(Intent intent) {
        char c;
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                switch (action.hashCode()) {
                    case -2128145023:
                        if (action.equals("android.intent.action.SCREEN_OFF")) {
                            c = 9;
                            break;
                        }
                    case -1886648615:
                        if (action.equals("android.intent.action.ACTION_POWER_DISCONNECTED")) {
                            c = 2;
                            break;
                        }
                    case -1875733435:
                        if (action.equals("android.net.wifi.WIFI_STATE_CHANGED")) {
                            c = 5;
                            break;
                        }
                    case -1538406691:
                        if (action.equals("android.intent.action.BATTERY_CHANGED")) {
                            c = 0;
                            break;
                        }
                    case -1454123155:
                        if (action.equals("android.intent.action.SCREEN_ON")) {
                            c = 8;
                            break;
                        }
                    case -1172645946:
                        if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                            c = 4;
                            break;
                        }
                    case -385684331:
                        if (action.equals("android.net.wifi.RSSI_CHANGED")) {
                            c = 7;
                            break;
                        }
                    case -343630553:
                        if (action.equals("android.net.wifi.STATE_CHANGE")) {
                            c = 3;
                            break;
                        }
                    case 233521600:
                        if (action.equals("android.net.wifi.supplicant.STATE_CHANGE")) {
                            c = 6;
                            break;
                        }
                    case 1019184907:
                        if (action.equals("android.intent.action.ACTION_POWER_CONNECTED")) {
                            c = 1;
                            break;
                        }
                    default:
                        c = 65535;
                        break;
                }
                switch (c) {
                    case 0:
                        this.mBatteryLevel = intent.getIntExtra(MemoryConstant.MEM_FILECACHE_ITEM_LEVEL, -1);
                        this.mPrePlugedtype = this.mPlugedtype;
                        this.mPlugedtype = intent.getIntExtra("plugged", -1);
                        break;
                    case 1:
                        this.mCharging = 1;
                        break;
                    case 2:
                        this.mCharging = 0;
                        break;
                    case 3:
                        this.mWifiNetStatue = getWifiNetworkStatus(intent);
                        break;
                    case 4:
                        this.mConnectStatus = getConnectNetworkStatus(intent);
                        break;
                    case 5:
                        this.mWifiStatue = getWifiStatus(intent);
                        break;
                    case 6:
                        this.mWifiSupStatue = getWifiSupStatus(intent);
                        break;
                    case 7:
                        this.mWifiRssi = getWifiRssi(intent);
                        break;
                    case 8:
                        this.mScreenOn = true;
                        break;
                    case 9:
                        this.mScreenOn = false;
                        break;
                }
            }
        }
    }

    private void initState() {
        if (this.mContext != null) {
            BatteryManager batteryManager = (BatteryManager) this.mContext.getSystemService("batterymanager");
            Context context = this.mContext;
            Context context2 = this.mContext;
            WifiManager wifiManager = (WifiManager) context.getSystemService(DevSchedFeatureRT.WIFI_FEATURE);
            ConnectivityManager connectManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
            if (batteryManager != null) {
                if (batteryManager.isCharging()) {
                    this.mCharging = 1;
                } else {
                    this.mCharging = 0;
                }
            }
            if (wifiManager != null) {
                this.mWifiStatue = wifiManager.getWifiState();
            }
            if (connectManager != null) {
                NetworkInfo connectInfo = connectManager.getActiveNetworkInfo();
                if (connectInfo != null && connectInfo.isConnected()) {
                    if (connectInfo.getType() == 1) {
                        this.mWifiNetStatue = NetworkInfo.State.CONNECTED;
                        this.mConnectStatus = 3;
                    } else if (connectInfo.getType() == 0) {
                        this.mConnectStatus = 1;
                    }
                }
            }
        }
    }

    public int filterBr(Intent intent, AwareProcessInfo pInfo) {
        if (intent == null || pInfo == null) {
            return BrCtrlType.NONE.ordinal();
        }
        String action = intent.getAction();
        if (action == null) {
            return BrCtrlType.NONE.ordinal();
        }
        BrFilterPolicy brfilterPolicy = pInfo.getBrPolicy(intent);
        if (brfilterPolicy == null) {
            return BrCtrlType.NONE.ordinal();
        }
        if (AwareBroadcastDebug.getFilterDebug()) {
            AwareLog.i(TAG, "iaware_brFilter brfilterPolicy.getPolicy() : " + brfilterPolicy.getPolicy());
        }
        if (brfilterPolicy.getPolicy() == BrCtrlType.DATADEFAULTBR.ordinal()) {
            char c = 65535;
            int hashCode = action.hashCode();
            if (hashCode != -1875733435) {
                if (hashCode != -1538406691) {
                    if (hashCode != -343630553) {
                        if (hashCode == 233521600 && action.equals("android.net.wifi.supplicant.STATE_CHANGE")) {
                            c = 2;
                        }
                    } else if (action.equals("android.net.wifi.STATE_CHANGE")) {
                        c = 0;
                    }
                } else if (action.equals("android.intent.action.BATTERY_CHANGED")) {
                    c = 3;
                }
            } else if (action.equals("android.net.wifi.WIFI_STATE_CHANGED")) {
                c = 1;
            }
            switch (c) {
                case 0:
                    return getWifiNetPolicy(action);
                case 1:
                    return getWifiPolicy(action);
                case 2:
                    return getWifiSupPolicy(action);
                case 3:
                    return getBatteryPolicy(action);
            }
        }
        return brfilterPolicy.getPolicy();
    }

    private int getWifiNetPolicy(String action) {
        int policy = BrCtrlType.NONE.ordinal();
        int dataPolicy = BroadcastExFeature.getBrFilterPolicy(action);
        if (dataPolicy == 1) {
            if (this.mWifiNetStatue == NetworkInfo.State.CONNECTED || this.mWifiNetStatue == NetworkInfo.State.DISCONNECTED) {
                return policy;
            }
            return BrCtrlType.DISCARDBR.ordinal();
        } else if (dataPolicy == 2) {
            return BrCtrlType.DISCARDBR.ordinal();
        } else {
            return policy;
        }
    }

    private int getWifiPolicy(String action) {
        int policy = BrCtrlType.NONE.ordinal();
        int dataPolicy = BroadcastExFeature.getBrFilterPolicy(action);
        if (dataPolicy == 1) {
            if (this.mWifiStatue == 3 || this.mWifiStatue == 1) {
                return policy;
            }
            return BrCtrlType.DISCARDBR.ordinal();
        } else if (dataPolicy == 2) {
            return BrCtrlType.DISCARDBR.ordinal();
        } else {
            return policy;
        }
    }

    private int getWifiSupPolicy(String action) {
        int policy = BrCtrlType.NONE.ordinal();
        int dataPolicy = BroadcastExFeature.getBrFilterPolicy(action);
        if (dataPolicy == 1) {
            if (this.mWifiSupStatue == SupplicantState.COMPLETED || this.mWifiSupStatue == SupplicantState.DISCONNECTED) {
                return policy;
            }
            return BrCtrlType.DISCARDBR.ordinal();
        } else if (dataPolicy == 2) {
            return BrCtrlType.DISCARDBR.ordinal();
        } else {
            return policy;
        }
    }

    private int getBatteryPolicy(String action) {
        int policy = BrCtrlType.NONE.ordinal();
        if (BroadcastExFeature.getBrFilterPolicy(action) == 1 && this.mPrePlugedtype == this.mPlugedtype) {
            return BrCtrlType.DISCARDBR.ordinal();
        }
        return policy;
    }

    public boolean awareTrimAndEnqueueBr(boolean isParallel, HwBroadcastRecord r, boolean notify, int pid, String pkgName) {
        if (r == null) {
            return false;
        }
        AwareBroadcastCache brCache = mIawareBrCaches.get(r.getBrQueueName());
        if (brCache != null) {
            return brCache.awareTrimAndEnqueueBr(isParallel, r, notify, pid, pkgName, this);
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void unproxyCacheBr(int pid) {
        for (Map.Entry<String, AwareBroadcastCache> ent : mIawareBrCaches.entrySet()) {
            ent.getValue().unproxyCacheBr(pid);
        }
    }

    public void clearCacheBr(int pid) {
        for (Map.Entry<String, AwareBroadcastCache> ent : mIawareBrCaches.entrySet()) {
            ent.getValue().clearCacheBr(pid);
        }
    }

    public void updateProcessBrPolicy(AwareProcessInfo info, int state) {
        if (info != null) {
            Message msg = this.mHandler.obtainMessage();
            msg.what = 209;
            msg.arg1 = state;
            msg.obj = info;
            this.mHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    public void updateAppIdleStat(int idleStat, int userId, String packageName) {
        ArrayList<AwareProcessInfo> procList = ProcessInfoCollector.getInstance().getAwareProcessInfosFromPackage(packageName, userId);
        if (AwareBroadcastDebug.getFilterDebug()) {
            AwareLog.i(TAG, "iaware_brFilter appidle:" + idleStat + ", packageName:" + packageName + ", userId:" + userId);
        }
        int count = procList.size();
        for (int i = 0; i < count; i++) {
            AwareProcessInfo info = procList.get(i);
            if (idleStat == 1) {
                ProcessInfoCollector.getInstance().setAwareProcessState(info.mPid, info.mProcInfo.mUid, 3);
            } else {
                ProcessInfoCollector.getInstance().setAwareProcessState(info.mPid, info.mProcInfo.mUid, 4);
            }
        }
    }

    /* access modifiers changed from: private */
    public void updateProcessPolicy(int stateType, int eventType, int pid, int uid) {
        if (stateType != 5) {
            if (AwareBroadcastDebug.getFilterDebug()) {
                AwareLog.i(TAG, "iaware_brFilter process status:" + stateType + ", eventType:" + eventType + ", pid:" + pid + ", uid:" + uid);
            }
            if (pid > 0) {
                ProcessInfoCollector.getInstance().setAwareProcessState(pid, uid, -1);
            } else {
                ProcessInfoCollector.getInstance().setAwareProcessStateByUid(pid, uid, -1);
            }
        }
    }

    public void reportGoogleConn(boolean conn) {
        Message msg = this.mHandler.obtainMessage();
        msg.what = 210;
        if (conn) {
            msg.arg1 = 1;
        } else {
            msg.arg1 = -1;
        }
        this.mHandler.sendMessage(msg);
    }

    /* access modifiers changed from: private */
    public void resetGoogleConnStat(int connStat) {
        boolean conn = false;
        if (connStat == 1) {
            conn = true;
        }
        if (mGoogleConnStat != conn) {
            mGoogleConnStat = conn;
            if (AwareBroadcastDebug.getFilterDebug()) {
                AwareLog.i(TAG, "iaware_brFilter google conn stat change:" + mGoogleConnStat);
            }
            ArraySet<String> googleAppPkgs = BroadcastExFeature.getBrGoogleAppList();
            int count = googleAppPkgs.size();
            for (int i = 0; i < count; i++) {
                ArrayList<AwareProcessInfo> procList = ProcessInfoCollector.getInstance().getAwareProcessInfosFromPackage(googleAppPkgs.valueAt(i), -1);
                int countProc = procList.size();
                for (int j = 0; j < countProc; j++) {
                    AwareProcessInfo info = procList.get(j);
                    ProcessInfoCollector.getInstance().setAwareProcessState(info.mPid, info.mProcInfo.mUid, -1);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void updateBrPolicy(int state, Object obj) {
        if (obj instanceof AwareProcessInfo) {
            AwareProcessInfo info = (AwareProcessInfo) obj;
            info.updateBrPolicy();
            if (state == 2 || state == 4 || state == 8 || state == 6) {
                int currentState = info.getState();
                if (currentState == 10 || currentState == 9) {
                    unproxyCacheBr(info.mPid);
                }
            }
        }
    }

    public static int getGoogleConnStat() {
        if (mGoogleConnStat) {
            return 1;
        }
        return -1;
    }

    public void notifyOverFlow(int pid) {
        if (AwareBroadcastDebug.getFilterDebug()) {
            AwareLog.i(TAG, "iaware_brFilter notifyOverFlow: " + pid);
        }
        Message msg = this.mHandler.obtainMessage();
        msg.what = 212;
        msg.arg1 = pid;
        this.mHandler.sendMessage(msg);
    }

    public void dumpIawareFilterBr(PrintWriter pw) {
        pw.println("    BrFilterfeature enable :" + BroadcastExFeature.isFeatureEnabled(1));
        pw.println("    process policy :");
        ArrayList<AwareProcessInfo> listProcess = ProcessInfoCollector.getInstance().getAwareProcessInfoList();
        int size = listProcess.size();
        for (int i = 0; i < size; i++) {
            AwareProcessInfo info = listProcess.get(i);
            pw.println("      process pid : " + info.mPid + ", process name : " + info.mProcInfo.mProcessName + ", process adj:" + info.mProcInfo.mCurAdj);
            for (Map.Entry<String, BrFilterPolicy> ent : info.getBrFilterPolicyMap().entrySet()) {
                pw.println("         policy action : " + ent.getKey() + ", value : " + ent.getValue().getPolicy() + ", state : " + ent.getValue().getProcessState());
            }
        }
        pw.println("    br before filter count :" + AwareBroadcastDumpRadar.getBrBeforeCount());
        pw.println("    br after filter count :" + AwareBroadcastDumpRadar.getBrAfterCount());
        pw.println("    br noprocess filter count :" + AwareBroadcastDumpRadar.getBrNoProcessCount());
        pw.println("    br system_server nodrop count :" + AwareBroadcastDumpRadar.getSsNoDropCount());
        pw.println("    br persistent app nodrop count :" + AwareBroadcastDumpRadar.getPerAppNoDropCount());
        pw.println("    Default white list :" + BroadcastExFeature.getBrFilterWhiteList());
        pw.println("    Default white actionAPP :" + BroadcastExFeature.getBrFilterWhiteApp());
        pw.println("    Default black actionAPP :" + BroadcastExFeature.getBrFilterBlackApp());
        pw.println("    br MP actionAPP :" + BroadcastExFeature.getBrFilterMPApp());
        pw.println("    google app list :" + BroadcastExFeature.getBrGoogleAppList());
        AwareBroadcastDumpRadar radar = MultiTaskManagerService.self().getIawareBrRadar();
        if (radar != null) {
            pw.println("    brfilter detail :");
            for (Map.Entry<String, Integer> ent2 : radar.getBrFilterDetail().entrySet()) {
                pw.println("         " + ent2.getKey() + ", count , " + ent2.getValue());
            }
        }
    }
}
