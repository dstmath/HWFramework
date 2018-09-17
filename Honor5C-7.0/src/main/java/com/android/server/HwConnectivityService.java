package com.android.server;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManagerNative;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.common.HwFrameworkFactory;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.ContentObserver;
import android.net.CaptivePortal;
import android.net.ConnectivityManager;
import android.net.ICaptivePortal;
import android.net.INetworkPolicyManager;
import android.net.INetworkStatsService;
import android.net.INetworkStatsSession;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkInfo.State;
import android.net.NetworkRequest;
import android.net.NetworkStatsHistory;
import android.net.NetworkTemplate;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.provider.SettingsEx.Systemex;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.HwVSimManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Slog;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import com.android.internal.telephony.HwModemCapability;
import com.android.internal.telephony.HwPhoneConstants;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.util.ArrayUtils;
import com.android.server.GcmFixer.HeartbeatReceiver;
import com.android.server.GcmFixer.NetworkStateReceiver;
import com.android.server.connectivity.NetworkAgentInfo;
import com.android.server.location.HwGnssCommParam;
import com.android.server.net.HwNetworkStatsService;
import com.android.server.pm.HwPackageManagerService;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.rms.iaware.memory.utils.EventTracker;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.security.securitydiagnose.HwSecDiagnoseConstant;
import com.android.server.security.trustcircle.IOTController;
import com.android.server.wifipro.WifiProCommonUtils;
import com.hisi.mapcon.IMapconService;
import com.hisi.mapcon.IMapconService.Stub;
import com.huawei.android.bastet.IBastetManager;
import com.huawei.deliver.info.HwDeliverInfo;
import com.huawei.utils.reflect.EasyInvokeFactory;
import huawei.HwFeatureConfig;
import huawei.android.telephony.wrapper.WrapperFactory;
import huawei.cust.HwCustUtils;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class HwConnectivityService extends ConnectivityService {
    private static final /* synthetic */ int[] -android-net-NetworkInfo$StateSwitchesValues = null;
    private static final int ACTION_BASTET_FILTER_ADD_LIST = 4;
    private static final int ACTION_BASTET_FILTER_CHECK = 1;
    private static final int ACTION_BASTET_FILTER_DEL_LIST = 5;
    private static final int ACTION_BASTET_FILTER_START = 2;
    private static final int ACTION_BASTET_FILTER_STOP = 3;
    private static final int ACTION_BASTET_FILTER_UNKNOWN = 0;
    public static final String ACTION_MAPCON_SERVICE_FAILED = "com.hisi.mapcon.servicefailed";
    public static final String ACTION_MAPCON_SERVICE_START = "com.hisi.mapcon.serviceStartResult";
    private static final String ACTION_NOTIFY_WIFI_CONNECTED_CONCURRENTLY = "com.huawei.wifipro.action.ACTION_NOTIFY_WIFI_CONNECTED_CONCURRENTLY";
    private static final String ACTION_OF_ANDROID_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
    private static final String BASTET_SERVICE = "BastetService";
    private static final int BASTET_SERVICE_CHECK_DELAY = 500;
    public static final String CONNECTIVITY_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    private static final int CONNECTIVITY_SERVICE_NEED_SET_USER_DATA = 1100;
    private static final String COUNTRY_CODE_CN = "460";
    public static final int DEFAULT_PHONE_ID = 0;
    private static final String DEFAULT_SERVER = "connectivitycheck.android.com";
    private static final int DELAY_MAX_RETRY = 5;
    public static final String DISABEL_DATA_SERVICE_ACTION = "android.net.conn.DISABEL_DATA_SERVICE_ACTION";
    private static final String DISABLE_PORTAL_CHECK = "disable_portal_check";
    private static String ENABLE_NOT_REMIND_FUNCTION = null;
    private static final int FILTER_START_DELAY_TIME = 15000;
    public static final String FLAG_SETUP_WIZARD = "flag_setup_wizard";
    private static final int HOUR_OF_MORNING = 5;
    private static final int HSM_NETWORKMANAGER_SERVICE_TRANSACTION_CODE = 201;
    private static final int HSM_NETWORK_POLICY_SERVICE_TRANSACTION_CODE = 204;
    private static final int HW_RULE_ALL_ACCESS = 0;
    private static final int HW_RULE_MOBILE_RESTRICT = 1;
    private static final int HW_RULE_WIFI_RESTRICT = 2;
    private static final int IM_ACTIVE_MILLIS = 30000;
    private static final int IM_HOUR_OF_MORNING = 5;
    private static final int IM_HOUR_OF_NIGHT = 23;
    public static final String IM_SPECIAL_PROC = "com.tencent.mm;com.tencent.mm:push;com.tencent.mobileqq;com.tencent.mobileqq:MSF;com.huawei.parentcontrol.parent;com.huawei.parentcontrol;com.huawei.hidisk";
    private static final int IM_TIMER_DAY_INTERVAL_MILLIS = 2400000;
    private static final int IM_TIMER_MORN_INTERVAL_MILLIS = 3600000;
    private static final int IM_TIMER_NIGHT_INTERVAL_MILLIS = 7200000;
    private static final int IM_TURNOFF_DC_DELAY_TIME = 5000;
    protected static final String INTENT_DAY_CLOCK = "android.filter.day.clock";
    protected static final String INTENT_IM_PENDING_PROCESS = "android.im.pending.process";
    protected static final String INTENT_IM_RESUME_PROCESS = "android.im.resume.process";
    protected static final String INTENT_NIGHT_CLOCK = "android.filter.night.clock";
    protected static final String INTENT_TURNOFF_DC = "android.telephony.turnoff_DC";
    private static final int INVALID_PID = -1;
    private static final boolean IS_CHINA;
    private static final int LTE_SERVICE_OFF = 0;
    private static final int LTE_SERVICE_ON = 1;
    public static final String MAPCON_START_INTENT = "com.hisi.mmsut.started";
    private static final int MINUTE_OF_MORNIG = 45;
    public static final int MMS_DOMAIN_CELLULAR_PREFER = 1;
    public static final int MMS_DOMAIN_WIFI_ONLY = 0;
    public static final int MMS_DOMAIN_WIFI_PREFER = 2;
    public static final int MMS_TIMER_DELAYED = 10000;
    public static final String MM_PKG_NAME = "com.tencent.mm";
    public static final String MM_PUSH_NAME = "com.tencent.mm:push";
    private static final String MODULE_POWERSAVING = "powersaving";
    private static final String MODULE_WIFI = "wifi";
    public static final String MSG_ALL_CTRLSOCKET_ALLOWED = "android.ctrlsocket.all.allowed";
    public static final String MSG_SCROFF_CTRLSOCKET_STATS = "android.scroff.ctrlsocket.status";
    public static final String PG_PENDING_ACTION = "huawei.intent.action.PG_PENDING_ALARM_ACTION";
    protected static final String POWER_SAVING_ON = "power_saving_on";
    protected static final String PROPERTY_BTHOTSPOT_ON = "sys.isbthotspoton";
    protected static final String PROPERTY_USBTETHERING_ON = "sys.isusbtetheringon";
    protected static final String PROPERTY_WIFIHOTSPOT_ON = "sys.iswifihotspoton";
    private static final int RANDOM_TIME_SECOND = 1800;
    public static final int SERVICE_STATE_MMS = 1;
    public static final int SERVICE_TYPE_MMS = 0;
    public static final int SERVICE_TYPE_OTHERS = 2;
    private static final String SYSTEM_MANAGER_PKG_NAME = "com.huawei.systemmanager";
    private static final String TAG = "HwConnectivityService";
    protected static final int TURNOFF_DC_MILLIS = 1800000;
    protected static final String TURN_OFF_DC_STATE = "turn_off_dc_state";
    private static String VALUE_DISABLE_NOT_REMIND_FUNCTION = null;
    private static String VALUE_ENABLE_NOT_REMIND_FUNCTION = null;
    private static int VALUE_NOT_SHOW_PDP = 0;
    private static int VALUE_SHOW_PDP = 0;
    private static String WHETHER_SHOW_PDP_WARNING = null;
    private static final String WIFI_AP_MANUAL_CONNECT = "wifi_ap_manual_connect";
    public static final int WIFI_PULS_CSP_DISENABLED = 1;
    public static final int WIFI_PULS_CSP_ENABLED = 0;
    private static ConnectivityServiceUtils connectivityServiceUtils = null;
    private static final String ctrl_socket_version = "v2";
    private static final String descriptor = "android.net.IConnectivityManager";
    private static final boolean isAllowBastetFilter;
    protected static final boolean isAlwaysAllowMMS;
    private static boolean mBastetFilterEnable;
    private static int mLteMobileDataState;
    private static INetworkStatsService mStatsService;
    private int ALLOW_ALL_CTRL_SOCKET_LEVEL;
    private int ALLOW_NO_CTRL_SOCKET_LEVEL;
    private int ALLOW_PART_CTRL_SOCKET_LEVEL;
    private int ALLOW_SPECIAL_CTRL_SOCKET_LEVEL;
    private int CANCEL_SPECIAL_PID;
    private int GET_KEEP_SOCKET_STATS;
    private int KEEP_SOCKET;
    private int MAX_REGISTERED_PKG_NUM;
    private int NORMAL_POWER_SAVING_MODE;
    private int POWER_SAVING_MODE;
    private int PUSH_AVAILABLE;
    private int SET_SAVING;
    private int SET_SPECIAL_PID;
    private int SUPER_POWER_SAVING_MODE;
    private final Uri WHITELIST_URI;
    private int curMmsDataSub;
    private int curPrefDataSubscription;
    private boolean isWaitWifiMms;
    private boolean isWifiMmsAlready;
    private ActivityManager mActivityManager;
    private DeathRecipient mBastetDeathRecipient;
    private int mBastetDiedRetry;
    private Object mBastetFilterLock;
    private IBinder mBastetService;
    private Context mContext;
    private CtrlSocketInfo mCtrlSocketInfo;
    private HwCustConnectivityService mCust;
    private AlertDialog mDataServiceToPdpDialog;
    protected PendingIntent mDayClockIntent;
    private ContentObserver mDbObserver;
    protected long mDeltaTime;
    private Object mFilterDelayLock;
    private Handler mFilterHandler;
    private int mFilterKeepPid;
    private int mFilterMsgFlag;
    private int mFilterSpecialSocket;
    private Object mFilterUidlistLock;
    protected boolean mFirst;
    private NetworkStateReceiver mGcmFixIntentReceiver;
    private Handler mHandler;
    private HeartbeatReceiver mHeartbeatReceiver;
    private IBastetManager mIBastetManager;
    private ArrayList<String> mIMArrayList;
    protected PendingIntent mIMPendingIntent;
    protected PendingIntent mIMResumeIntent;
    private WakeLock mIMWakeLock;
    private BroadcastReceiver mIntentReceiver;
    private boolean mIsSimStateChanged;
    private boolean mIsUnlockStats;
    protected long mLastPowerOffTime;
    private NetworkRequest mLteMmsNetworkRequest;
    LteMmsTimer mLteMmsTimer;
    protected BroadcastReceiver mMapconIntentReceiver;
    protected IMapconService mMapconService;
    protected PendingIntent mNightClockIntent;
    PhoneStateListener mPhoneStateListener;
    protected Object mPowerSavingLock;
    private boolean mRemindService;
    private String mServer;
    protected boolean mShouldPowerSave;
    private boolean mShowDlgEndCall;
    private boolean mShowDlgTurnOfDC;
    private BroadcastReceiver mSimStateReceiver;
    protected boolean mStartPowerSaving;
    private BroadcastReceiver mTetheringReceiver;
    private HandlerThread mThread;
    protected PendingIntent mTurnoffDCIntent;
    private URL mURL;
    WifiMmsTimer mWifiMmsTimer;
    private Messenger mWifiNetworkMessenger;
    private boolean m_filterIsStarted;
    private Set<Integer> m_filterUidSet;
    private int phoneId;
    private boolean sendWifiBroadcastAfterBootCompleted;
    private Set<Integer> uidSet;
    private WifiDisconnectManager wifiDisconnectManager;

    /* renamed from: com.android.server.HwConnectivityService.11 */
    class AnonymousClass11 extends ContentObserver {
        AnonymousClass11(Handler $anonymous0) {
            super($anonymous0);
        }

        public void onChange(boolean selfChange) {
            HwConnectivityService.this.getCtrlSocketPushWhiteList();
        }
    }

    /* renamed from: com.android.server.HwConnectivityService.7 */
    class AnonymousClass7 implements OnClickListener {
        final /* synthetic */ String val$enable_Not_Remind_Function;
        final /* synthetic */ CheckBox val$finalBox;

        AnonymousClass7(String val$enable_Not_Remind_Function, CheckBox val$finalBox) {
            this.val$enable_Not_Remind_Function = val$enable_Not_Remind_Function;
            this.val$finalBox = val$finalBox;
        }

        public void onClick(DialogInterface dialoginterface, int i) {
            HwTelephonyManagerInner.getDefault().setDataEnabledWithoutPromp(true);
            if (HwConnectivityService.VALUE_ENABLE_NOT_REMIND_FUNCTION.equals(this.val$enable_Not_Remind_Function) && this.val$finalBox != null) {
                HwConnectivityService.this.updateReminderSetting(this.val$finalBox.isChecked());
            }
            HwConnectivityService.this.mDataServiceToPdpDialog = null;
        }
    }

    /* renamed from: com.android.server.HwConnectivityService.8 */
    class AnonymousClass8 implements OnClickListener {
        final /* synthetic */ String val$enable_Not_Remind_Function;
        final /* synthetic */ CheckBox val$finalBox;

        AnonymousClass8(String val$enable_Not_Remind_Function, CheckBox val$finalBox) {
            this.val$enable_Not_Remind_Function = val$enable_Not_Remind_Function;
            this.val$finalBox = val$finalBox;
        }

        public void onClick(DialogInterface dialog, int which) {
            HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).sendBroadcast(new Intent(HwConnectivityService.DISABEL_DATA_SERVICE_ACTION));
            HwTelephonyManagerInner.getDefault().setDataEnabledWithoutPromp(HwConnectivityService.IS_CHINA);
            if (HwConnectivityService.VALUE_ENABLE_NOT_REMIND_FUNCTION.equals(this.val$enable_Not_Remind_Function) && this.val$finalBox != null) {
                HwConnectivityService.this.updateReminderSetting(this.val$finalBox.isChecked());
            }
            HwConnectivityService.this.mDataServiceToPdpDialog = null;
        }
    }

    /* renamed from: com.android.server.HwConnectivityService.9 */
    class AnonymousClass9 implements OnCancelListener {
        final /* synthetic */ String val$enable_Not_Remind_Function;
        final /* synthetic */ CheckBox val$finalBox;

        AnonymousClass9(String val$enable_Not_Remind_Function, CheckBox val$finalBox) {
            this.val$enable_Not_Remind_Function = val$enable_Not_Remind_Function;
            this.val$finalBox = val$finalBox;
        }

        public void onCancel(DialogInterface dialog) {
            HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).sendBroadcast(new Intent(HwConnectivityService.DISABEL_DATA_SERVICE_ACTION));
            HwTelephonyManagerInner.getDefault().setDataEnabledWithoutPromp(HwConnectivityService.IS_CHINA);
            if (HwConnectivityService.VALUE_ENABLE_NOT_REMIND_FUNCTION.equals(this.val$enable_Not_Remind_Function) && this.val$finalBox != null) {
                HwConnectivityService.this.updateReminderSetting(this.val$finalBox.isChecked());
            }
            HwConnectivityService.this.mDataServiceToPdpDialog = null;
        }
    }

    private static class CtrlSocketInfo {
        public int mAllowCtrlSocketLevel;
        public List<String> mPushWhiteListPkg;
        public int mRegisteredCount;
        public List<String> mRegisteredPkg;
        public List<String> mScrOffActPkg;
        public int mScrOffActiveCount;
        public Map<String, Integer> mSpecialPkgMap;
        public List<String> mSpecialWhiteListPkg;

        CtrlSocketInfo() {
            this.mRegisteredPkg = null;
            this.mScrOffActPkg = null;
            this.mPushWhiteListPkg = null;
            this.mSpecialWhiteListPkg = null;
            this.mSpecialPkgMap = null;
            this.mAllowCtrlSocketLevel = HwConnectivityService.WIFI_PULS_CSP_ENABLED;
            this.mRegisteredCount = HwConnectivityService.WIFI_PULS_CSP_ENABLED;
            this.mScrOffActiveCount = HwConnectivityService.WIFI_PULS_CSP_ENABLED;
            this.mRegisteredPkg = new ArrayList();
            this.mScrOffActPkg = new ArrayList();
            this.mPushWhiteListPkg = new ArrayList();
            this.mSpecialWhiteListPkg = new ArrayList();
            this.mSpecialPkgMap = new HashMap();
        }
    }

    private class HwConnectivityServiceHandler extends Handler {
        private static final int EVENT_SHOW_ENABLE_PDP_DIALOG = 0;
        private static final int EVENT_TURN_OFF_DC_TIMEOUT = 1;
        private static final int MESSAGE_BASTET_SERVICE_DIED = 2;

        private HwConnectivityServiceHandler() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_SHOW_ENABLE_PDP_DIALOG /*0*/:
                    HwConnectivityService.this.handleShowEnablePdpDialog();
                case EVENT_TURN_OFF_DC_TIMEOUT /*1*/:
                    HwConnectivityService.this.handleIMReceivingMsgAction();
                case MESSAGE_BASTET_SERVICE_DIED /*2*/:
                    if (HwConnectivityService.isAllowBastetFilter) {
                        HwConnectivityService.this.handleBastetServiceDied();
                    }
                default:
            }
        }
    }

    private class HwFilterHandler extends Handler {
        private static final int MSG_SET_OPTION = 1;
        private static final int MSG_START_STOP = 0;

        public HwFilterHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HwConnectivityService.WIFI_PULS_CSP_ENABLED /*0*/:
                    HwConnectivityService.this.handleFilterMsg(msg.arg1, msg.arg2);
                case MSG_SET_OPTION /*1*/:
                    if (HwConnectivityService.mBastetFilterEnable) {
                        HwConnectivityService.this.setBastetFilterInfo(msg.arg1, msg.arg2);
                    }
                default:
            }
        }
    }

    private class LteMmsTimer {
        private boolean isRunning;
        private TimerTask mTimerTask;
        private Timer timer;

        private void getTimerTask() {
            this.mTimerTask = new TimerTask() {
                public void run() {
                    LteMmsTimer.this.isRunning = HwConnectivityService.IS_CHINA;
                    HwConnectivityService.this.sendUpdatedScoreToFactories(HwConnectivityService.this.mLteMmsNetworkRequest, HwConnectivityService.WIFI_PULS_CSP_ENABLED);
                }
            };
        }

        public LteMmsTimer() {
            this.timer = new Timer();
            this.isRunning = HwConnectivityService.IS_CHINA;
            getTimerTask();
        }

        public void start() {
            HwConnectivityService.log("LteMmsTimer start");
            this.mTimerTask.cancel();
            getTimerTask();
            this.timer.schedule(this.mTimerTask, MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
            this.isRunning = true;
        }

        public void stop() {
            this.mTimerTask.cancel();
            this.isRunning = HwConnectivityService.IS_CHINA;
        }

        public boolean isRunning() {
            return this.isRunning;
        }
    }

    private class MobileEnabledSettingObserver extends ContentObserver {
        public MobileEnabledSettingObserver(Handler handler) {
            super(handler);
        }

        public void register() {
            HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).getContentResolver().registerContentObserver(Global.getUriFor("device_provisioned"), true, this);
        }

        public void onChange(boolean selfChange) {
            if (HwConnectivityService.this.mRemindService || HwConnectivityService.this.checkDataServiceRemindMsim()) {
                super.onChange(selfChange);
                if (!HwConnectivityService.this.getMobileDataEnabled() && HwConnectivityService.this.mDataServiceToPdpDialog == null) {
                    HwConnectivityService.this.mDataServiceToPdpDialog = HwConnectivityService.this.createWarningToPdp();
                    HwConnectivityService.this.mDataServiceToPdpDialog.show();
                }
            }
        }
    }

    private class WifiDisconnectManager {
        private static final String ACTION_SWITCH_TO_MOBILE_NETWORK = "android.intent.action.SWITCH_TO_MOBILE_NETWORK";
        private static final String ACTION_WIFI_NETWORK_CONNECTION_CHANGED = "android.intent.action.WIFI_NETWORK_CONNECTION_CHANGED";
        private static final String CONNECT_STATE = "connect_state";
        private static final String SWITCH_STATE = "switch_state";
        private static final int SWITCH_TO_WIFI_AUTO = 0;
        private static final String SWITCH_TO_WIFI_TYPE = "wifi_connect_type";
        private static final String WIFI_TO_PDP = "wifi_to_pdp";
        private static final int WIFI_TO_PDP_AUTO = 1;
        private static final int WIFI_TO_PDP_NEVER = 2;
        private static final int WIFI_TO_PDP_NOTIFY = 0;
        private final boolean REMIND_WIFI_TO_PDP;
        private boolean mDialogHasShown;
        State mLastWifiState;
        private BroadcastReceiver mNetworkSwitchReceiver;
        private boolean mShouldStartMobile;
        private Handler mSwitchHandler;
        private OnDismissListener mSwitchPdpListener;
        protected AlertDialog mWifiToPdpDialog;
        private boolean shouldShowDialogWhenConnectFailed;

        /* renamed from: com.android.server.HwConnectivityService.WifiDisconnectManager.4 */
        class AnonymousClass4 implements OnClickListener {
            final /* synthetic */ CheckBox val$checkBox;

            AnonymousClass4(CheckBox val$checkBox) {
                this.val$checkBox = val$checkBox;
            }

            public void onClick(DialogInterface dialoginterface, int i) {
                WifiDisconnectManager.this.mShouldStartMobile = true;
                HwConnectivityService.log("setPositiveButton: mShouldStartMobile set true");
                WifiDisconnectManager.this.checkUserChoice(this.val$checkBox.isChecked(), true);
            }
        }

        /* renamed from: com.android.server.HwConnectivityService.WifiDisconnectManager.5 */
        class AnonymousClass5 implements OnClickListener {
            final /* synthetic */ CheckBox val$checkBox;

            AnonymousClass5(CheckBox val$checkBox) {
                this.val$checkBox = val$checkBox;
            }

            public void onClick(DialogInterface dialoginterface, int i) {
                HwConnectivityService.log("you have chose to disconnect Mobile data service!");
                WifiDisconnectManager.this.mShouldStartMobile = HwConnectivityService.IS_CHINA;
                WifiDisconnectManager.this.checkUserChoice(this.val$checkBox.isChecked(), HwConnectivityService.IS_CHINA);
            }
        }

        private WifiDisconnectManager() {
            this.REMIND_WIFI_TO_PDP = SystemProperties.getBoolean("ro.config.hw_RemindWifiToPdp", HwConnectivityService.IS_CHINA);
            this.mWifiToPdpDialog = null;
            this.mShouldStartMobile = HwConnectivityService.IS_CHINA;
            this.shouldShowDialogWhenConnectFailed = true;
            this.mDialogHasShown = HwConnectivityService.IS_CHINA;
            this.mLastWifiState = State.DISCONNECTED;
            this.mSwitchPdpListener = new OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    HwTelephonyFactory.getHwDataServiceChrManager().sendMonitorWifiSwitchToMobileMessage(HwConnectivityService.IM_TURNOFF_DC_DELAY_TIME);
                    if (WifiDisconnectManager.this.mShouldStartMobile) {
                        HwConnectivityService.this.setMobileDataEnabled(HwConnectivityService.MODULE_WIFI, true);
                        HwConnectivityService.log("you have restart Mobile data service!");
                    }
                    WifiDisconnectManager.this.mShouldStartMobile = HwConnectivityService.IS_CHINA;
                    WifiDisconnectManager.this.mWifiToPdpDialog = null;
                }
            };
            this.mNetworkSwitchReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    if (!WifiDisconnectManager.ACTION_SWITCH_TO_MOBILE_NETWORK.equals(intent.getAction())) {
                        return;
                    }
                    if (intent.getBooleanExtra(WifiDisconnectManager.SWITCH_STATE, true)) {
                        HwConnectivityService.this.wifiDisconnectManager.switchToMobileNetwork();
                    } else {
                        HwConnectivityService.this.wifiDisconnectManager.cancelSwitchToMobileNetwork();
                    }
                }
            };
            this.mSwitchHandler = new Handler() {
                public void handleMessage(Message msg) {
                    HwConnectivityService.log("mSwitchHandler recieve msg =" + msg.what);
                    switch (msg.what) {
                        case WifiDisconnectManager.SWITCH_TO_WIFI_AUTO /*0*/:
                            WifiDisconnectManager.this.switchToMobileNetwork();
                        default:
                    }
                }
            };
        }

        private boolean getAirplaneModeEnable() {
            boolean retVal = System.getInt(HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).getContentResolver(), "airplane_mode_on", SWITCH_TO_WIFI_AUTO) == WIFI_TO_PDP_AUTO ? true : HwConnectivityService.IS_CHINA;
            HwConnectivityService.log("getAirplaneModeEnable returning " + retVal);
            return retVal;
        }

        private AlertDialog createSwitchToPdpWarning() {
            HwConnectivityService.log("create dialog of switch to pdp");
            HwTelephonyFactory.getHwDataServiceChrManager().removeMonitorWifiSwitchToMobileMessage();
            Builder buider = new Builder(HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this), 33947691);
            View view = LayoutInflater.from(buider.getContext()).inflate(34013282, null);
            CheckBox checkBox = (CheckBox) view.findViewById(34603229);
            buider.setView(view);
            buider.setTitle(33685520);
            buider.setIcon(17301543);
            buider.setPositiveButton(33685559, new AnonymousClass4(checkBox));
            buider.setNegativeButton(33685560, new AnonymousClass5(checkBox));
            AlertDialog dialog = buider.create();
            dialog.setCancelable(HwConnectivityService.IS_CHINA);
            dialog.getWindow().setType(2008);
            return dialog;
        }

        private void checkUserChoice(boolean rememberChoice, boolean enableDataConnect) {
            int showPopState;
            if (!rememberChoice) {
                showPopState = SWITCH_TO_WIFI_AUTO;
            } else if (enableDataConnect) {
                showPopState = WIFI_TO_PDP_AUTO;
            } else {
                showPopState = SWITCH_TO_WIFI_AUTO;
            }
            HwConnectivityService.log("checkUserChoice showPopState:" + showPopState + ", enableDataConnect:" + enableDataConnect);
            System.putInt(HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).getContentResolver(), WIFI_TO_PDP, showPopState);
        }

        private void sendWifiBroadcast(boolean isConnectingOrConnected) {
            if (ActivityManagerNative.isSystemReady() && HwConnectivityService.this.sendWifiBroadcastAfterBootCompleted) {
                HwConnectivityService.log("notify settings:" + isConnectingOrConnected);
                Intent intent = new Intent(ACTION_WIFI_NETWORK_CONNECTION_CHANGED);
                intent.putExtra(CONNECT_STATE, isConnectingOrConnected);
                HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).sendBroadcast(intent);
            }
        }

        private boolean shouldNotifySettings() {
            if (!isSwitchToWifiSupported() || System.getInt(HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).getContentResolver(), SWITCH_TO_WIFI_TYPE, SWITCH_TO_WIFI_AUTO) == 0) {
                return HwConnectivityService.IS_CHINA;
            }
            return true;
        }

        private boolean isSwitchToWifiSupported() {
            if ("CMCC".equalsIgnoreCase(SystemProperties.get("ro.config.operators", AppHibernateCst.INVALID_PKG))) {
                return true;
            }
            return HwConnectivityService.this.mCust.isSupportWifiConnectMode();
        }

        private void switchToMobileNetwork() {
            if (getAirplaneModeEnable()) {
                HwConnectivityService.this.enableDefaultTypeAPN(true);
            } else if (this.shouldShowDialogWhenConnectFailed || !this.mDialogHasShown) {
                int value = System.getInt(HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).getContentResolver(), WIFI_TO_PDP, WIFI_TO_PDP_AUTO);
                HwConnectivityService.log("WIFI_TO_PDP value =" + value);
                int wifiplusvalue = System.getInt(HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).getContentResolver(), "wifi_csp_dispaly_state", WIFI_TO_PDP_AUTO);
                HwConnectivityService.log("wifiplus_csp_dispaly_state value =" + wifiplusvalue);
                HwVSimManager hwVSimManager = HwVSimManager.getDefault();
                if (hwVSimManager != null && hwVSimManager.isVSimEnabled()) {
                    HwConnectivityService.log("vsim is enabled and following process will execute enableDefaultTypeAPN(true), so do nothing that likes value == WIFI_TO_PDP_AUTO");
                } else if (value == 0) {
                    if (wifiplusvalue == 0) {
                        HwConnectivityService.log("wifi_csp_dispaly_state = 0, Don't create WifiToPdpDialog");
                        HwConnectivityService.log("enableDefaultTypeAPN(true) in switchToMobileNetwork()  ");
                        HwConnectivityService.this.enableDefaultTypeAPN(true);
                        return;
                    }
                    HwConnectivityService.this.setMobileDataEnabled(HwConnectivityService.MODULE_WIFI, HwConnectivityService.IS_CHINA);
                    this.mShouldStartMobile = true;
                    this.mDialogHasShown = true;
                    this.mWifiToPdpDialog = createSwitchToPdpWarning();
                    this.mWifiToPdpDialog.setOnDismissListener(this.mSwitchPdpListener);
                    this.mWifiToPdpDialog.show();
                } else if (value != WIFI_TO_PDP_AUTO) {
                    if (WIFI_TO_PDP_AUTO == wifiplusvalue) {
                        HwConnectivityService.this.setMobileDataEnabled(HwConnectivityService.MODULE_WIFI, HwConnectivityService.IS_CHINA);
                    } else {
                        HwConnectivityService.log("wifi_csp_dispaly_state = 0, Don't setMobileDataEnabled");
                    }
                }
                HwConnectivityService.log("enableDefaultTypeAPN(true) in switchToMobileNetwork( )");
                HwConnectivityService.this.enableDefaultTypeAPN(true);
            }
        }

        private void cancelSwitchToMobileNetwork() {
            if (this.mWifiToPdpDialog != null) {
                Log.d(HwConnectivityService.TAG, "cancelSwitchToMobileNetwork and mWifiToPdpDialog is showing");
                this.mShouldStartMobile = true;
                this.mWifiToPdpDialog.dismiss();
            }
        }

        private void registerReceiver() {
            if (this.REMIND_WIFI_TO_PDP && isSwitchToWifiSupported()) {
                IntentFilter filter = new IntentFilter();
                filter.addAction(ACTION_SWITCH_TO_MOBILE_NETWORK);
                HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).registerReceiver(this.mNetworkSwitchReceiver, filter);
            }
        }

        protected void hintUserSwitchToMobileWhileWifiDisconnected(State state, int type) {
            HwConnectivityService.log("hintUserSwitchToMobileWhileWifiDisconnected, state=" + state + "  type =" + type);
            boolean shouldEnableDefaultTypeAPN = true;
            if (this.REMIND_WIFI_TO_PDP) {
                if (state == State.DISCONNECTED && type == WIFI_TO_PDP_AUTO && HwConnectivityService.this.getMobileDataEnabled()) {
                    if (this.mLastWifiState == State.CONNECTED) {
                        int value = System.getInt(HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).getContentResolver(), WIFI_TO_PDP, WIFI_TO_PDP_AUTO);
                        HwConnectivityService.log("WIFI_TO_PDP value     =" + value);
                        if (value == WIFI_TO_PDP_AUTO) {
                            HwConnectivityService.this.enableDefaultTypeAPN(true);
                            return;
                        }
                        this.shouldShowDialogWhenConnectFailed = true;
                        HwConnectivityService.log("mShouldEnableDefaultTypeAPN was set false");
                        shouldEnableDefaultTypeAPN = HwConnectivityService.IS_CHINA;
                    }
                    if (shouldNotifySettings()) {
                        sendWifiBroadcast(HwConnectivityService.IS_CHINA);
                    } else if (getAirplaneModeEnable()) {
                        shouldEnableDefaultTypeAPN = true;
                    } else {
                        this.mSwitchHandler.sendMessageDelayed(this.mSwitchHandler.obtainMessage(SWITCH_TO_WIFI_AUTO), 5000);
                        HwConnectivityService.log("switch message will be send in 5 seconds");
                    }
                    if (this.mLastWifiState == State.CONNECTING) {
                        this.shouldShowDialogWhenConnectFailed = HwConnectivityService.IS_CHINA;
                    }
                } else if ((state == State.CONNECTED || state == State.CONNECTING) && type == WIFI_TO_PDP_AUTO) {
                    if (state == State.CONNECTED) {
                        this.mDialogHasShown = HwConnectivityService.IS_CHINA;
                    }
                    if (shouldNotifySettings()) {
                        sendWifiBroadcast(true);
                    } else if (this.mSwitchHandler.hasMessages(SWITCH_TO_WIFI_AUTO)) {
                        this.mSwitchHandler.removeMessages(SWITCH_TO_WIFI_AUTO);
                        HwConnectivityService.log("switch message was removed");
                    }
                    if (this.mWifiToPdpDialog != null) {
                        this.mShouldStartMobile = true;
                        this.mWifiToPdpDialog.dismiss();
                    }
                }
                if (type == WIFI_TO_PDP_AUTO) {
                    HwConnectivityService.log("mLastWifiState =" + this.mLastWifiState);
                    this.mLastWifiState = state;
                }
            }
            if (shouldEnableDefaultTypeAPN && state == State.DISCONNECTED && type == WIFI_TO_PDP_AUTO) {
                HwConnectivityService.log("enableDefaultTypeAPN(true) in hintUserSwitchToMobileWhileWifiDisconnected");
                HwConnectivityService.this.enableDefaultTypeAPN(true);
            }
        }

        protected void makeDefaultAndHintUser(NetworkAgentInfo newNetwork) {
        }
    }

    private class WifiMmsTimer {
        private boolean isRunning;
        private TimerTask mTimerTask;
        private Timer timer;

        private void getTimerTask() {
            this.mTimerTask = new TimerTask() {
                public void run() {
                    WifiMmsTimer.this.isRunning = HwConnectivityService.IS_CHINA;
                    try {
                        HwConnectivityService.this.mMapconService.setupTunnelOverWifi(HwConnectivityService.WIFI_PULS_CSP_ENABLED, HwConnectivityService.WIFI_PULS_CSP_ENABLED, null, null);
                    } catch (RemoteException e) {
                        HwConnectivityService.loge("WifiMmsTimer,setupTunnelOverWifi,err=" + e.toString());
                    }
                }
            };
        }

        public WifiMmsTimer() {
            this.timer = new Timer();
            this.isRunning = HwConnectivityService.IS_CHINA;
            getTimerTask();
        }

        public void start() {
            HwConnectivityService.log("WifiMmsTimer start");
            this.mTimerTask.cancel();
            getTimerTask();
            this.timer.schedule(this.mTimerTask, MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
            this.isRunning = true;
        }

        public void stop() {
            this.mTimerTask.cancel();
            this.isRunning = HwConnectivityService.IS_CHINA;
        }

        public boolean isRunning() {
            return this.isRunning;
        }
    }

    private static /* synthetic */ int[] -getandroid-net-NetworkInfo$StateSwitchesValues() {
        if (-android-net-NetworkInfo$StateSwitchesValues != null) {
            return -android-net-NetworkInfo$StateSwitchesValues;
        }
        int[] iArr = new int[State.values().length];
        try {
            iArr[State.CONNECTED.ordinal()] = WIFI_PULS_CSP_DISENABLED;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[State.CONNECTING.ordinal()] = SERVICE_TYPE_OTHERS;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[State.DISCONNECTED.ordinal()] = ACTION_BASTET_FILTER_STOP;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[State.DISCONNECTING.ordinal()] = ACTION_BASTET_FILTER_ADD_LIST;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[State.SUSPENDED.ordinal()] = IM_HOUR_OF_MORNING;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[State.UNKNOWN.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        -android-net-NetworkInfo$StateSwitchesValues = iArr;
        return iArr;
    }

    static {
        connectivityServiceUtils = (ConnectivityServiceUtils) EasyInvokeFactory.getInvokeUtils(ConnectivityServiceUtils.class);
        isAlwaysAllowMMS = SystemProperties.getBoolean("ro.config.hw_always_allow_mms", IS_CHINA);
        WHETHER_SHOW_PDP_WARNING = "whether_show_pdp_warning";
        VALUE_NOT_SHOW_PDP = WIFI_PULS_CSP_ENABLED;
        VALUE_SHOW_PDP = WIFI_PULS_CSP_DISENABLED;
        ENABLE_NOT_REMIND_FUNCTION = "enable_not_remind_function";
        VALUE_ENABLE_NOT_REMIND_FUNCTION = "true";
        VALUE_DISABLE_NOT_REMIND_FUNCTION = "false";
        mLteMobileDataState = ACTION_BASTET_FILTER_STOP;
        mBastetFilterEnable = IS_CHINA;
        isAllowBastetFilter = SystemProperties.getBoolean("ro.config.hw_bastet_filter", true);
        IS_CHINA = "CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", AppHibernateCst.INVALID_PKG));
    }

    private static void log(String s) {
        Slog.d(TAG, s);
    }

    private static void loge(String s) {
        Slog.e(TAG, s);
    }

    public HwConnectivityService(Context context, INetworkManagementService netd, INetworkStatsService statsService, INetworkPolicyManager policyManager) {
        super(context, netd, statsService, policyManager);
        this.mTurnoffDCIntent = null;
        this.mIMResumeIntent = null;
        this.mIMPendingIntent = null;
        this.mNightClockIntent = null;
        this.mDayClockIntent = null;
        this.mStartPowerSaving = IS_CHINA;
        this.mShouldPowerSave = IS_CHINA;
        this.mFirst = true;
        this.mDeltaTime = 0;
        this.mLastPowerOffTime = 0;
        this.mPowerSavingLock = new Object();
        this.mBastetFilterLock = new Object();
        this.sendWifiBroadcastAfterBootCompleted = IS_CHINA;
        this.wifiDisconnectManager = new WifiDisconnectManager();
        this.phoneId = INVALID_PID;
        this.curMmsDataSub = INVALID_PID;
        this.curPrefDataSubscription = INVALID_PID;
        this.WHITELIST_URI = Secure.getUriFor("push_white_apps");
        this.NORMAL_POWER_SAVING_MODE = WIFI_PULS_CSP_ENABLED;
        this.SUPER_POWER_SAVING_MODE = ACTION_BASTET_FILTER_STOP;
        this.POWER_SAVING_MODE = ACTION_BASTET_FILTER_ADD_LIST;
        this.ALLOW_NO_CTRL_SOCKET_LEVEL = WIFI_PULS_CSP_ENABLED;
        this.ALLOW_PART_CTRL_SOCKET_LEVEL = WIFI_PULS_CSP_DISENABLED;
        this.ALLOW_ALL_CTRL_SOCKET_LEVEL = SERVICE_TYPE_OTHERS;
        this.ALLOW_SPECIAL_CTRL_SOCKET_LEVEL = ACTION_BASTET_FILTER_STOP;
        this.MAX_REGISTERED_PKG_NUM = 10;
        this.KEEP_SOCKET = WIFI_PULS_CSP_DISENABLED;
        this.PUSH_AVAILABLE = SERVICE_TYPE_OTHERS;
        this.SET_SAVING = 100;
        this.SET_SPECIAL_PID = WifiProCommonUtils.HISTORY_TYPE_PORTAL;
        this.CANCEL_SPECIAL_PID = WifiProCommonUtils.HISTORY_TYPE_EMPTY;
        this.GET_KEEP_SOCKET_STATS = IOTController.EV_CANCEL_AUTH_ALL;
        this.mIMArrayList = new ArrayList();
        this.mCtrlSocketInfo = new CtrlSocketInfo();
        this.mIsSimStateChanged = IS_CHINA;
        this.mIsUnlockStats = true;
        this.mCust = (HwCustConnectivityService) HwCustUtils.createObj(HwCustConnectivityService.class, new Object[WIFI_PULS_CSP_ENABLED]);
        this.mIBastetManager = null;
        this.mBastetService = null;
        this.mBastetDiedRetry = WIFI_PULS_CSP_ENABLED;
        this.mFilterKeepPid = WIFI_PULS_CSP_ENABLED;
        this.mFilterSpecialSocket = WIFI_PULS_CSP_ENABLED;
        this.mFilterDelayLock = new Object();
        this.mFilterUidlistLock = new Object();
        this.m_filterUidSet = new HashSet();
        this.m_filterIsStarted = IS_CHINA;
        this.mFilterMsgFlag = WIFI_PULS_CSP_ENABLED;
        this.uidSet = new HashSet();
        this.mLteMmsNetworkRequest = null;
        this.isWaitWifiMms = IS_CHINA;
        this.isWifiMmsAlready = IS_CHINA;
        this.mGcmFixIntentReceiver = new NetworkStateReceiver();
        this.mHeartbeatReceiver = new HeartbeatReceiver();
        this.mIntentReceiver = new BroadcastReceiver() {
            /* JADX WARNING: inconsistent code. */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void onReceive(Context context, Intent intent) {
                Intent it;
                NetworkInfo info;
                Object obj;
                boolean isCharging;
                Intent batteryStatus;
                HwConnectivityService.log("mIntentReceiver begin");
                String action = intent.getAction();
                boolean disable = HwFrameworkFactory.getHwKeyguardManager().isLockScreenDisabled(HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this));
                if ("android.intent.action.USER_PRESENT".equals(action)) {
                    HwConnectivityService.this.mIsUnlockStats = true;
                }
                boolean locked = HwConnectivityService.this.mIsUnlockStats ? HwConnectivityService.IS_CHINA : true;
                HwConnectivityService.log("CtrlSocket Receiver,disable: " + disable + " locked: " + locked + " action: " + action + " mSmartKeyguardLevel: " + HwConnectivityExService.mSmartKeyguardLevel + " mStartPowerSaving: " + HwConnectivityService.this.mStartPowerSaving);
                if (!locked) {
                    if ("android.intent.action.USER_PRESENT".equals(action)) {
                        Log.d(HwConnectivityService.TAG, "receive ACTION_USER_PRESENT and unlock.");
                        HwConnectivityService.this.processCtrlSocket(Process.myUid(), HwConnectivityService.this.SET_SAVING, HwConnectivityService.WIFI_PULS_CSP_ENABLED);
                    }
                }
                if ("max_level".equals(HwConnectivityExService.mSmartKeyguardLevel) && !disable) {
                    if (!locked) {
                    }
                    locked = HwConnectivityService.this.mStartPowerSaving;
                    if ("android.intent.action.USER_PRESENT".equals(action)) {
                        HwConnectivityService.log("CtrlSocket receive keyguard unlock intent!");
                        HwConnectivityService.this.restoreScrOnStatus();
                    }
                    if ("android.intent.action.SCREEN_OFF".equals(action)) {
                        if (HwConnectivityService.INTENT_TURNOFF_DC.equals(action)) {
                            if (HwConnectivityService.ACTION_OF_ANDROID_BOOT_COMPLETED.equals(action)) {
                                if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                                    if (HwConnectivityService.INTENT_IM_RESUME_PROCESS.equals(action)) {
                                        if (HwConnectivityService.INTENT_IM_PENDING_PROCESS.equals(action)) {
                                            if (HwConnectivityService.INTENT_NIGHT_CLOCK.equals(action)) {
                                                if (HwConnectivityService.INTENT_DAY_CLOCK.equals(action)) {
                                                    Log.d(HwConnectivityService.TAG, "[PS filter]receive INTENT_DAY_CLOCK and curHour = " + HwConnectivityService.this.getHourOfDay());
                                                    HwConnectivityService.this.turnonDC();
                                                    HwConnectivityService.this.setNightClockTimer();
                                                }
                                            } else {
                                                Log.d(HwConnectivityService.TAG, "[PS filter]receive INTENT_NIGHT_CLOCK and curHour = " + HwConnectivityService.this.getHourOfDay());
                                                HwConnectivityService.this.turnoffDC();
                                                HwConnectivityService.this.setDayClockTimer();
                                            }
                                        } else {
                                            if (HwConnectivityService.this.mCtrlSocketInfo.mAllowCtrlSocketLevel == HwConnectivityService.this.ALLOW_NO_CTRL_SOCKET_LEVEL) {
                                                HwConnectivityService.this.mIMArrayList.clear();
                                                HwConnectivityService.this.mIMArrayList.add(HwConnectivityService.MM_PKG_NAME);
                                                if (!HwConnectivityService.this.isWifiAvailable()) {
                                                    Log.d(HwConnectivityService.TAG, "CtrlSocketDo set Power Saving Mode 1");
                                                    HwConnectivityService.this.processCtrlSocket(Process.myUid(), HwConnectivityService.this.SET_SAVING, HwConnectivityService.WIFI_PULS_CSP_DISENABLED);
                                                }
                                                it = new Intent(HwConnectivityService.PG_PENDING_ACTION);
                                                it.putExtra("enable", true);
                                                it.putExtra("applist", HwConnectivityService.this.mIMArrayList);
                                                it.putExtra(HwSecDiagnoseConstant.ANTIMAL_APK_TYPE, HwConnectivityService.WIFI_PULS_CSP_DISENABLED);
                                                HwConnectivityService.this.mContext.sendBroadcast(it, "com.huawei.powergenie.receiverPermission");
                                                Log.d(HwConnectivityService.TAG, "tell powegenie to pending applist = " + HwConnectivityService.this.mIMArrayList);
                                                HwConnectivityService.this.setIMResumeTimer();
                                                if (HwConnectivityService.this.mIMWakeLock != null) {
                                                    if (HwConnectivityService.this.mIMWakeLock.isHeld()) {
                                                        HwConnectivityService.this.mIMWakeLock.release();
                                                    }
                                                }
                                            } else {
                                                return;
                                            }
                                        }
                                    }
                                    if (HwConnectivityService.this.mCtrlSocketInfo.mAllowCtrlSocketLevel == HwConnectivityService.this.ALLOW_NO_CTRL_SOCKET_LEVEL) {
                                        HwConnectivityService.this.turnoffDC();
                                        if (HwConnectivityService.this.mIMWakeLock.isHeld()) {
                                            HwConnectivityService.this.mIMWakeLock.release();
                                        }
                                        HwConnectivityService.this.mIMWakeLock.acquire(30000);
                                        HwConnectivityService.this.mHandler.sendMessageDelayed(HwConnectivityService.this.mHandler.obtainMessage(HwConnectivityService.WIFI_PULS_CSP_DISENABLED), 5000);
                                    } else {
                                        return;
                                    }
                                }
                                if (HwConnectivityService.this.mShouldPowerSave) {
                                    Log.d(HwConnectivityService.TAG, "[PS filter] should not power save");
                                    return;
                                }
                                info = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                                if (info == null && info.isConnected()) {
                                    HwConnectivityService.this.processCtrlSocket(Process.myUid(), HwConnectivityService.this.SET_SAVING, HwConnectivityService.WIFI_PULS_CSP_ENABLED);
                                } else if (!(info == null || info.isConnected())) {
                                    if (HwConnectivityService.this.ALLOW_NO_CTRL_SOCKET_LEVEL != HwConnectivityService.this.getUseCtrlSocketLevel()) {
                                        HwConnectivityService.this.startMobileFilter();
                                    }
                                }
                            } else {
                                HwConnectivityService.log("receive Intent.ACTION_BOOT_COMPLETED!");
                                HwConnectivityService.this.sendWifiBroadcastAfterBootCompleted = true;
                                if (SystemProperties.getBoolean("ro.config.hw_power_saving", HwConnectivityService.IS_CHINA)) {
                                    if (HwConnectivityService.this.getTurnOffDCState()) {
                                        HwConnectivityService.log("exception of power saving when power off,then turnonDC");
                                        HwConnectivityService.this.turnonDC();
                                    }
                                }
                            }
                        }
                        obj = HwConnectivityService.this.mPowerSavingLock;
                        synchronized (obj) {
                        }
                        if (HwConnectivityService.this.mStartPowerSaving) {
                            HwConnectivityService.this.calcUseCtrlSocketLevel();
                            Log.d(HwConnectivityService.TAG, "CtrlSocket allowCtrlSocketLevel = " + HwConnectivityService.this.mCtrlSocketInfo.mAllowCtrlSocketLevel);
                            if (HwConnectivityService.this.ALLOW_NO_CTRL_SOCKET_LEVEL == HwConnectivityService.this.mCtrlSocketInfo.mAllowCtrlSocketLevel) {
                                if (HwConnectivityService.this.isWifiAvailable()) {
                                    Log.d(HwConnectivityService.TAG, "startMobileFilter");
                                    HwConnectivityService.this.startMobileFilter();
                                } else {
                                    Log.d(HwConnectivityService.TAG, "[PS filter]turnoffDC mShouldPowerSave true");
                                    HwConnectivityService.this.mShouldPowerSave = true;
                                }
                            } else {
                                Slog.d(HwConnectivityService.TAG, "turn off Data Connection!");
                                HwConnectivityService.this.turnoffDC();
                                it = new Intent(HwConnectivityService.MSG_SCROFF_CTRLSOCKET_STATS);
                                it.putExtra("ctrl_socket_status", HwConnectivityService.IS_CHINA);
                                HwConnectivityService.this.mContext.sendBroadcastAsUser(it, UserHandle.ALL);
                                Log.d(HwConnectivityService.TAG, "CtrlSocket allow no broadcast");
                            }
                        }
                    } else {
                        HwConnectivityService.this.mIsUnlockStats = HwConnectivityService.IS_CHINA;
                        obj = HwConnectivityService.this.mPowerSavingLock;
                        synchronized (obj) {
                            HwConnectivityService.log("receive screen off intent!");
                            boolean isWifiApOn = SystemProperties.getBoolean(HwConnectivityService.PROPERTY_WIFIHOTSPOT_ON, HwConnectivityService.IS_CHINA);
                            boolean isUsbTetheringOn = SystemProperties.getBoolean(HwConnectivityService.PROPERTY_USBTETHERING_ON, HwConnectivityService.IS_CHINA);
                            boolean isBtTetheringOn = SystemProperties.getBoolean(HwConnectivityService.PROPERTY_BTHOTSPOT_ON, HwConnectivityService.IS_CHINA);
                            isCharging = HwConnectivityService.IS_CHINA;
                            HwConnectivityService.log("wifi tethering: " + isWifiApOn);
                            HwConnectivityService.log("Usb tethering: " + isUsbTetheringOn);
                            HwConnectivityService.log("bt tethering: " + isBtTetheringOn);
                            batteryStatus = context.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
                            if (batteryStatus != null) {
                                int pluggedStatus = batteryStatus.getIntExtra("plugged", HwConnectivityService.WIFI_PULS_CSP_ENABLED);
                                HwConnectivityService.log("pluggedStatus: " + pluggedStatus);
                                isCharging = pluggedStatus == 0 ? true : HwConnectivityService.IS_CHINA;
                            }
                            HwConnectivityService.log("is charging: " + isCharging);
                            boolean isFlightMode = Global.getInt(context.getContentResolver(), "airplane_mode_on", HwConnectivityService.WIFI_PULS_CSP_ENABLED) == 0 ? true : HwConnectivityService.IS_CHINA;
                            if (!isCharging || isWifiApOn || isUsbTetheringOn || isBtTetheringOn || isFlightMode) {
                                HwConnectivityService.this.cancelPowerSaving();
                                HwConnectivityService.this.mFirst = true;
                            } else {
                                if (HwConnectivityService.this.getMobileDataEnabled()) {
                                    if (HwConnectivityService.this.getPowerSavingState()) {
                                        if (HwConnectivityService.this.mStartPowerSaving) {
                                            if ("max_level".equals(HwConnectivityExService.mSmartKeyguardLevel)) {
                                            }
                                        }
                                        HwConnectivityService.log("start powersaving action!");
                                        if (!disable) {
                                            if (!"min_level".equals(HwConnectivityExService.mSmartKeyguardLevel)) {
                                                if ("max_level".equals(HwConnectivityExService.mSmartKeyguardLevel) && !disable) {
                                                    locked = HwConnectivityService.this.mStartPowerSaving;
                                                }
                                                HwConnectivityService.this.tryPowerSavingI(locked);
                                                HwConnectivityService.this.mStartPowerSaving = true;
                                            }
                                        }
                                        HwConnectivityService.this.tryPowerSaving();
                                        HwConnectivityService.this.mStartPowerSaving = true;
                                    }
                                }
                            }
                        }
                        return;
                    }
                }
                if ("max_level".equals(HwConnectivityExService.mSmartKeyguardLevel) && !disable) {
                    locked = HwConnectivityService.this.mStartPowerSaving;
                }
                if ("android.intent.action.SCREEN_ON".equals(action)) {
                    HwConnectivityService.log("CtrlSocket receive screen on intent!");
                    HwConnectivityService.this.restoreScrOnStatus();
                }
                if ("android.intent.action.SCREEN_OFF".equals(action)) {
                    if (HwConnectivityService.INTENT_TURNOFF_DC.equals(action)) {
                        if (HwConnectivityService.ACTION_OF_ANDROID_BOOT_COMPLETED.equals(action)) {
                            if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                                if (HwConnectivityService.INTENT_IM_RESUME_PROCESS.equals(action)) {
                                    if (HwConnectivityService.INTENT_IM_PENDING_PROCESS.equals(action)) {
                                        if (HwConnectivityService.INTENT_NIGHT_CLOCK.equals(action)) {
                                            if (HwConnectivityService.INTENT_DAY_CLOCK.equals(action)) {
                                                Log.d(HwConnectivityService.TAG, "[PS filter]receive INTENT_DAY_CLOCK and curHour = " + HwConnectivityService.this.getHourOfDay());
                                                HwConnectivityService.this.turnonDC();
                                                HwConnectivityService.this.setNightClockTimer();
                                            }
                                        } else {
                                            Log.d(HwConnectivityService.TAG, "[PS filter]receive INTENT_NIGHT_CLOCK and curHour = " + HwConnectivityService.this.getHourOfDay());
                                            HwConnectivityService.this.turnoffDC();
                                            HwConnectivityService.this.setDayClockTimer();
                                        }
                                    } else {
                                        if (HwConnectivityService.this.mCtrlSocketInfo.mAllowCtrlSocketLevel == HwConnectivityService.this.ALLOW_NO_CTRL_SOCKET_LEVEL) {
                                            HwConnectivityService.this.mIMArrayList.clear();
                                            HwConnectivityService.this.mIMArrayList.add(HwConnectivityService.MM_PKG_NAME);
                                            if (HwConnectivityService.this.isWifiAvailable()) {
                                                Log.d(HwConnectivityService.TAG, "CtrlSocketDo set Power Saving Mode 1");
                                                HwConnectivityService.this.processCtrlSocket(Process.myUid(), HwConnectivityService.this.SET_SAVING, HwConnectivityService.WIFI_PULS_CSP_DISENABLED);
                                            }
                                            it = new Intent(HwConnectivityService.PG_PENDING_ACTION);
                                            it.putExtra("enable", true);
                                            it.putExtra("applist", HwConnectivityService.this.mIMArrayList);
                                            it.putExtra(HwSecDiagnoseConstant.ANTIMAL_APK_TYPE, HwConnectivityService.WIFI_PULS_CSP_DISENABLED);
                                            HwConnectivityService.this.mContext.sendBroadcast(it, "com.huawei.powergenie.receiverPermission");
                                            Log.d(HwConnectivityService.TAG, "tell powegenie to pending applist = " + HwConnectivityService.this.mIMArrayList);
                                            HwConnectivityService.this.setIMResumeTimer();
                                            if (HwConnectivityService.this.mIMWakeLock != null) {
                                                if (HwConnectivityService.this.mIMWakeLock.isHeld()) {
                                                    HwConnectivityService.this.mIMWakeLock.release();
                                                }
                                            }
                                        } else {
                                            return;
                                        }
                                    }
                                }
                                if (HwConnectivityService.this.mCtrlSocketInfo.mAllowCtrlSocketLevel == HwConnectivityService.this.ALLOW_NO_CTRL_SOCKET_LEVEL) {
                                    HwConnectivityService.this.turnoffDC();
                                    if (HwConnectivityService.this.mIMWakeLock.isHeld()) {
                                        HwConnectivityService.this.mIMWakeLock.release();
                                    }
                                    HwConnectivityService.this.mIMWakeLock.acquire(30000);
                                    HwConnectivityService.this.mHandler.sendMessageDelayed(HwConnectivityService.this.mHandler.obtainMessage(HwConnectivityService.WIFI_PULS_CSP_DISENABLED), 5000);
                                } else {
                                    return;
                                }
                            }
                            if (HwConnectivityService.this.mShouldPowerSave) {
                                info = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                                if (info == null) {
                                }
                                if (HwConnectivityService.this.ALLOW_NO_CTRL_SOCKET_LEVEL != HwConnectivityService.this.getUseCtrlSocketLevel()) {
                                    HwConnectivityService.this.startMobileFilter();
                                }
                            } else {
                                Log.d(HwConnectivityService.TAG, "[PS filter] should not power save");
                                return;
                            }
                        }
                        HwConnectivityService.log("receive Intent.ACTION_BOOT_COMPLETED!");
                        HwConnectivityService.this.sendWifiBroadcastAfterBootCompleted = true;
                        if (SystemProperties.getBoolean("ro.config.hw_power_saving", HwConnectivityService.IS_CHINA)) {
                            if (HwConnectivityService.this.getTurnOffDCState()) {
                                HwConnectivityService.log("exception of power saving when power off,then turnonDC");
                                HwConnectivityService.this.turnonDC();
                            }
                        }
                    }
                    obj = HwConnectivityService.this.mPowerSavingLock;
                    synchronized (obj) {
                    }
                    if (HwConnectivityService.this.mStartPowerSaving) {
                        HwConnectivityService.this.calcUseCtrlSocketLevel();
                        Log.d(HwConnectivityService.TAG, "CtrlSocket allowCtrlSocketLevel = " + HwConnectivityService.this.mCtrlSocketInfo.mAllowCtrlSocketLevel);
                        if (HwConnectivityService.this.ALLOW_NO_CTRL_SOCKET_LEVEL == HwConnectivityService.this.mCtrlSocketInfo.mAllowCtrlSocketLevel) {
                            Slog.d(HwConnectivityService.TAG, "turn off Data Connection!");
                            HwConnectivityService.this.turnoffDC();
                            it = new Intent(HwConnectivityService.MSG_SCROFF_CTRLSOCKET_STATS);
                            it.putExtra("ctrl_socket_status", HwConnectivityService.IS_CHINA);
                            HwConnectivityService.this.mContext.sendBroadcastAsUser(it, UserHandle.ALL);
                            Log.d(HwConnectivityService.TAG, "CtrlSocket allow no broadcast");
                        } else {
                            if (HwConnectivityService.this.isWifiAvailable()) {
                                Log.d(HwConnectivityService.TAG, "[PS filter]turnoffDC mShouldPowerSave true");
                                HwConnectivityService.this.mShouldPowerSave = true;
                            } else {
                                Log.d(HwConnectivityService.TAG, "startMobileFilter");
                                HwConnectivityService.this.startMobileFilter();
                            }
                        }
                    }
                } else {
                    HwConnectivityService.this.mIsUnlockStats = HwConnectivityService.IS_CHINA;
                    obj = HwConnectivityService.this.mPowerSavingLock;
                    synchronized (obj) {
                        HwConnectivityService.log("receive screen off intent!");
                        boolean isWifiApOn2 = SystemProperties.getBoolean(HwConnectivityService.PROPERTY_WIFIHOTSPOT_ON, HwConnectivityService.IS_CHINA);
                        boolean isUsbTetheringOn2 = SystemProperties.getBoolean(HwConnectivityService.PROPERTY_USBTETHERING_ON, HwConnectivityService.IS_CHINA);
                        boolean isBtTetheringOn2 = SystemProperties.getBoolean(HwConnectivityService.PROPERTY_BTHOTSPOT_ON, HwConnectivityService.IS_CHINA);
                        isCharging = HwConnectivityService.IS_CHINA;
                        HwConnectivityService.log("wifi tethering: " + isWifiApOn2);
                        HwConnectivityService.log("Usb tethering: " + isUsbTetheringOn2);
                        HwConnectivityService.log("bt tethering: " + isBtTetheringOn2);
                        batteryStatus = context.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
                        if (batteryStatus != null) {
                            int pluggedStatus2 = batteryStatus.getIntExtra("plugged", HwConnectivityService.WIFI_PULS_CSP_ENABLED);
                            HwConnectivityService.log("pluggedStatus: " + pluggedStatus2);
                            if (pluggedStatus2 == 0) {
                            }
                        }
                        HwConnectivityService.log("is charging: " + isCharging);
                        if (Global.getInt(context.getContentResolver(), "airplane_mode_on", HwConnectivityService.WIFI_PULS_CSP_ENABLED) == 0) {
                        }
                        if (isCharging) {
                        }
                        HwConnectivityService.this.cancelPowerSaving();
                        HwConnectivityService.this.mFirst = true;
                    }
                    return;
                }
            }
        };
        this.mDataServiceToPdpDialog = null;
        this.mShowDlgEndCall = IS_CHINA;
        this.mShowDlgTurnOfDC = true;
        this.mRemindService = SystemProperties.getBoolean("ro.config.DataPopFirstBoot", IS_CHINA);
        this.mPhoneStateListener = new PhoneStateListener() {
            public void onCallStateChanged(int state, String incomingNumber) {
                HwConnectivityService.this.updateCallState(state);
            }
        };
        this.mSimStateReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.SIM_STATE_CHANGED".equals(intent.getAction())) {
                    Log.d(HwConnectivityService.TAG, "CtrlSocket receive ACTION_SIM_STATE_CHANGED");
                    HwConnectivityService.this.mIsSimStateChanged = true;
                }
            }
        };
        this.mTetheringReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action != null && HwGnssCommParam.ACTION_USB_STATE.equals(action)) {
                    boolean usbConnected = intent.getBooleanExtra(HwGnssCommParam.USB_CONNECTED, HwConnectivityService.IS_CHINA);
                    boolean rndisEnabled = intent.getBooleanExtra("rndis", HwConnectivityService.IS_CHINA);
                    int is_usb_tethering_on = Secure.getInt(HwConnectivityService.this.mContext.getContentResolver(), "usb_tethering_on", HwConnectivityService.WIFI_PULS_CSP_ENABLED);
                    Log.d(HwConnectivityService.TAG, "mTetheringReceiver usbConnected = " + usbConnected + ",rndisEnabled = " + rndisEnabled + ", is_usb_tethering_on = " + is_usb_tethering_on);
                    if (HwConnectivityService.WIFI_PULS_CSP_DISENABLED == is_usb_tethering_on && usbConnected && !rndisEnabled) {
                        new Thread() {
                            public void run() {
                                do {
                                    try {
                                        if (HwConnectivityService.this.isSystemBootComplete()) {
                                            Thread.sleep(200);
                                        } else {
                                            Thread.sleep(1000);
                                        }
                                    } catch (InterruptedException e) {
                                        Log.e(HwConnectivityService.TAG, "wait to boot complete error");
                                    }
                                } while (!HwConnectivityService.this.isSystemBootComplete());
                                HwConnectivityService.this.setUsbTethering(true);
                            }
                        }.start();
                    }
                }
            }
        };
        this.mBastetDeathRecipient = new DeathRecipient() {
            public void binderDied() {
                Log.e(HwConnectivityService.TAG, "Bastet service has died!");
                if (HwConnectivityService.isAllowBastetFilter) {
                    synchronized (HwConnectivityService.this.mBastetFilterLock) {
                        if (HwConnectivityService.this.mBastetService != null) {
                            HwConnectivityService.this.mBastetService.unlinkToDeath(this, HwConnectivityService.WIFI_PULS_CSP_ENABLED);
                            HwConnectivityService.this.mBastetService = null;
                            HwConnectivityService.this.mIBastetManager = null;
                        }
                        Message msg = HwConnectivityService.this.mHandler.obtainMessage();
                        msg.what = HwConnectivityService.SERVICE_TYPE_OTHERS;
                        HwConnectivityService.this.mHandler.sendMessageDelayed(msg, 500);
                    }
                }
            }
        };
        this.mMapconIntentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent mapconIntent) {
                if (mapconIntent == null) {
                    HwConnectivityService.log("intent is null");
                    return;
                }
                String action = mapconIntent.getAction();
                HwConnectivityService.log("onReceive: action=" + action);
                if (HwConnectivityService.MAPCON_START_INTENT.equals(action)) {
                    ServiceConnection mConnection = new ServiceConnection() {
                        public void onServiceConnected(ComponentName className, IBinder service) {
                            HwConnectivityService.this.mMapconService = Stub.asInterface(service);
                        }

                        public void onServiceDisconnected(ComponentName className) {
                            HwConnectivityService.this.mMapconService = null;
                        }
                    };
                    HwConnectivityService.this.mContext.bindServiceAsUser(new Intent().setClassName("com.hisi.mapcon", "com.hisi.mapcon.MapconService"), mConnection, HwConnectivityService.WIFI_PULS_CSP_DISENABLED, UserHandle.OWNER);
                } else if (HwConnectivityService.CONNECTIVITY_CHANGE_ACTION.equals(action)) {
                    NetworkInfo networkInfo = (NetworkInfo) mapconIntent.getParcelableExtra("networkInfo");
                    if (networkInfo != null && HwConnectivityService.SERVICE_TYPE_OTHERS == networkInfo.getType() && networkInfo.isConnected()) {
                        HwConnectivityService.log("onReceive: mms connection ok");
                        HwConnectivityService.this.mWifiMmsTimer.stop();
                    }
                } else if (HwConnectivityService.ACTION_MAPCON_SERVICE_START.equals(action) && mapconIntent.getIntExtra("serviceType", HwConnectivityService.SERVICE_TYPE_OTHERS) == 0) {
                    HwConnectivityService.loge("Recive ACTION_MAPCON_SERVICE_START");
                    HwConnectivityService.this.mLteMmsTimer.stop();
                    HwConnectivityService.this.isWifiMmsAlready = true;
                } else if (HwConnectivityService.ACTION_MAPCON_SERVICE_FAILED.equals(action) && mapconIntent.getIntExtra("serviceType", HwConnectivityService.SERVICE_TYPE_OTHERS) == 0) {
                    HwConnectivityService.loge("Recive ACTION_MAPCON_SERVICE_FAILED");
                    if (HwConnectivityService.this.mLteMmsTimer.isRunning()) {
                        HwConnectivityService.loge("stop mLteMmsTimer");
                        if (HwConnectivityService.this.mLteMmsTimer.isRunning()) {
                            HwConnectivityService.loge("stop mLteMmsTimer and back to cellular sendUpdatedScoreToFactories");
                            HwConnectivityService.this.sendUpdatedScoreToFactories(HwConnectivityService.this.mLteMmsNetworkRequest, HwConnectivityService.WIFI_PULS_CSP_ENABLED);
                        }
                        HwConnectivityService.this.mLteMmsTimer.stop();
                        HwConnectivityService.this.isWaitWifiMms = HwConnectivityService.IS_CHINA;
                    }
                }
            }
        };
        this.mWifiMmsTimer = new WifiMmsTimer();
        this.mLteMmsTimer = new LteMmsTimer();
        this.mContext = context;
        initCtrlSocket(context);
        registerSimStateReceiver(context);
        this.wifiDisconnectManager.registerReceiver();
        registerPhoneStateListener(context);
        registerBootStateListener(context);
        this.mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
        this.mHandler = new HwConnectivityServiceHandler();
        if (Boolean.valueOf(SystemProperties.getBoolean("ro.config.hw_vowifi", IS_CHINA)).booleanValue()) {
            registerMapconIntentReceiver(context);
        }
        this.mServer = Global.getString(context.getContentResolver(), "captive_portal_server");
        if (this.mServer == null) {
            this.mServer = DEFAULT_SERVER;
        }
        SystemProperties.set("sys.defaultapn.enabled", "true");
        registerTetheringReceiver(context);
        this.mIMWakeLock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(WIFI_PULS_CSP_DISENABLED, "IMReceiveMsg");
        if (isAllowBastetFilter) {
            checkBastetFilter();
        }
        initFilterThread();
        initGCMFixer(context);
    }

    private void initGCMFixer(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_OF_ANDROID_BOOT_COMPLETED);
        filter.addAction(CONNECTIVITY_CHANGE_ACTION);
        this.mContext.registerReceiver(this.mGcmFixIntentReceiver, filter);
        filter = new IntentFilter();
        filter.addAction(HeartbeatReceiver.HEARTBEAT_FIXER_ACTION);
        this.mContext.registerReceiver(this.mHeartbeatReceiver, filter, "android.permission.CONNECTIVITY_INTERNAL", null);
    }

    private String[] getFeature(String str) {
        if (str == null) {
            throw new IllegalArgumentException("getFeature() received null string");
        }
        String[] result = new String[SERVICE_TYPE_OTHERS];
        int subId = WIFI_PULS_CSP_ENABLED;
        if (WrapperFactory.getMSimTelephonyManagerWrapper().isMultiSimEnabled()) {
            subId = WrapperFactory.getMSimTelephonyManagerWrapper().getPreferredDataSubscription();
            if (str.equals("enableMMS_sub2")) {
                str = "enableMMS";
                subId = WIFI_PULS_CSP_DISENABLED;
            } else if (str.equals("enableMMS_sub1")) {
                str = "enableMMS";
                subId = WIFI_PULS_CSP_ENABLED;
            }
        }
        result[WIFI_PULS_CSP_ENABLED] = str;
        result[WIFI_PULS_CSP_DISENABLED] = String.valueOf(subId);
        Slog.d(TAG, "getFeature: return feature=" + str + " subId=" + subId);
        return result;
    }

    protected String getMmsFeature(String feature) {
        Slog.d(TAG, "getMmsFeature HwFeatureConfig.dual_card_mms_switch" + HwFeatureConfig.dual_card_mms_switch);
        if (!HwFeatureConfig.dual_card_mms_switch) {
            return feature;
        }
        String[] result = getFeature(feature);
        feature = result[WIFI_PULS_CSP_ENABLED];
        this.phoneId = Integer.parseInt(result[WIFI_PULS_CSP_DISENABLED]);
        this.curMmsDataSub = INVALID_PID;
        return feature;
    }

    protected boolean isAlwaysAllowMMSforRoaming(int networkType, String feature) {
        if (networkType == 0) {
            boolean isAlwaysAllowMMSforRoaming = isAlwaysAllowMMS;
            if (HwPhoneConstants.IS_CHINA_TELECOM) {
                boolean roaming = WrapperFactory.getMSimTelephonyManagerWrapper().isNetworkRoaming(this.phoneId);
                if (isAlwaysAllowMMSforRoaming) {
                    if (roaming) {
                    }
                }
            }
        }
        return true;
    }

    protected boolean isMmsAutoSetSubDiffFromDataSub(int networkType, String feature) {
        if (!HwFeatureConfig.dual_card_mms_switch) {
            return IS_CHINA;
        }
        this.curPrefDataSubscription = WrapperFactory.getMSimTelephonyManagerWrapper().getPreferredDataSubscription();
        this.curMmsDataSub = WrapperFactory.getMSimTelephonyManagerWrapper().getMmsAutoSetDataSubscription();
        if (!feature.equals("enableMMS") || networkType != 0) {
            return IS_CHINA;
        }
        if ((this.curMmsDataSub != 0 && WIFI_PULS_CSP_DISENABLED != this.curMmsDataSub) || this.phoneId == this.curMmsDataSub) {
            return IS_CHINA;
        }
        log("DSMMS dds is switching now, do not response request from another card, curMmsDataSub: " + this.curMmsDataSub);
        return true;
    }

    protected boolean isMmsSubDiffFromDataSub(int networkType, String feature) {
        if (HwFeatureConfig.dual_card_mms_switch && feature.equals("enableMMS") && networkType == 0 && this.curPrefDataSubscription != this.phoneId) {
            return true;
        }
        return IS_CHINA;
    }

    protected boolean isNetRequestersPidsContainCurrentPid(List<Integer>[] mNetRequestersPids, int usedNetworkType, Integer currentPid) {
        if (!HwFeatureConfig.dual_card_mms_switch || !WrapperFactory.getMSimTelephonyManagerWrapper().isMultiSimEnabled() || mNetRequestersPids[usedNetworkType].contains(currentPid)) {
            return true;
        }
        Slog.w(TAG, "not tearing down special network - not found pid " + currentPid);
        return IS_CHINA;
    }

    protected boolean isNeedTearMmsAndRestoreData(int networkType, String feature, Handler mHandler) {
        if (!HwFeatureConfig.dual_card_mms_switch) {
            return true;
        }
        if (networkType != 0 || !feature.equals("enableMMS")) {
            return true;
        }
        if (!WrapperFactory.getMSimTelephonyManagerWrapper().isMultiSimEnabled()) {
            return true;
        }
        int curMmsDataSub = WrapperFactory.getMSimTelephonyManagerWrapper().getMmsAutoSetDataSubscription();
        if (curMmsDataSub != 0 && WIFI_PULS_CSP_DISENABLED != curMmsDataSub) {
            return true;
        }
        int lastPrefDataSubscription;
        if (curMmsDataSub == 0) {
            lastPrefDataSubscription = WIFI_PULS_CSP_DISENABLED;
        } else {
            lastPrefDataSubscription = WIFI_PULS_CSP_ENABLED;
        }
        int curPrefDataSubscription = WrapperFactory.getMSimTelephonyManagerWrapper().getPreferredDataSubscription();
        log("isNeedTearDataAndRestoreData lastPrefDataSubscription" + lastPrefDataSubscription + "curPrefDataSubscription" + curPrefDataSubscription);
        if (lastPrefDataSubscription != curPrefDataSubscription) {
            log("DSMMS >>>> disable a connection, after MMS net disconnected will switch back to phone " + lastPrefDataSubscription);
            WrapperFactory.getMSimTelephonyManagerWrapper().setPreferredDataSubscription(lastPrefDataSubscription);
        } else {
            log("DSMMS unexpected case, data subscription is already on " + curPrefDataSubscription);
        }
        WrapperFactory.getMSimTelephonyManagerWrapper().setMmsAutoSetDataSubscription(INVALID_PID);
        return true;
    }

    private void cancelPowerSaving() {
        if (this.mTurnoffDCIntent != null) {
            ((AlarmManager) connectivityServiceUtils.getContext(this).getSystemService("alarm")).cancel(this.mTurnoffDCIntent);
            this.mTurnoffDCIntent = null;
        }
        processCtrlSocket(Process.myUid(), this.SET_SAVING, WIFI_PULS_CSP_ENABLED);
    }

    private void tryPowerSaving() {
        AlarmManager am = (AlarmManager) connectivityServiceUtils.getContext(this).getSystemService("alarm");
        if (this.mTurnoffDCIntent != null) {
            am.cancel(this.mTurnoffDCIntent);
            this.mTurnoffDCIntent = null;
        }
        Intent intent = new Intent(INTENT_TURNOFF_DC);
        intent.setPackage(connectivityServiceUtils.getContext(this).getPackageName());
        this.mFilterMsgFlag = INVALID_PID;
        this.mTurnoffDCIntent = PendingIntent.getBroadcast(connectivityServiceUtils.getContext(this), WIFI_PULS_CSP_ENABLED, intent, WIFI_PULS_CSP_ENABLED);
        am.setAndAllowWhileIdle(SERVICE_TYPE_OTHERS, SystemClock.elapsedRealtime() + HwNetworkStatsService.UPLOAD_INTERVAL, this.mTurnoffDCIntent);
        Log.d(TAG, "CtrlSocket tryPowerSaving timer duration = 1800000");
    }

    private void tryPowerSavingI(boolean keyguardlocked) {
        AlarmManager am = (AlarmManager) connectivityServiceUtils.getContext(this).getSystemService("alarm");
        if (this.mTurnoffDCIntent != null) {
            am.cancel(this.mTurnoffDCIntent);
            this.mTurnoffDCIntent = null;
        }
        if (this.mFirst || !keyguardlocked) {
            this.mDeltaTime = HwNetworkStatsService.UPLOAD_INTERVAL;
            this.mFirst = IS_CHINA;
        } else {
            this.mDeltaTime -= SystemClock.elapsedRealtime() - this.mLastPowerOffTime;
            if (this.mDeltaTime < 0) {
                this.mDeltaTime = 0;
            }
        }
        if (this.mDeltaTime > 0) {
            this.mFilterMsgFlag = INVALID_PID;
        }
        Intent intent = new Intent(INTENT_TURNOFF_DC);
        intent.setPackage(connectivityServiceUtils.getContext(this).getPackageName());
        this.mTurnoffDCIntent = PendingIntent.getBroadcast(connectivityServiceUtils.getContext(this), WIFI_PULS_CSP_ENABLED, intent, WIFI_PULS_CSP_ENABLED);
        am.setAndAllowWhileIdle(SERVICE_TYPE_OTHERS, SystemClock.elapsedRealtime() + this.mDeltaTime, this.mTurnoffDCIntent);
        this.mLastPowerOffTime = SystemClock.elapsedRealtime();
        Log.d(TAG, "CtrlSocket tryPowerSavingI timer duration = " + this.mDeltaTime);
    }

    private void setIMResumeTimer() {
        if (this.mCtrlSocketInfo.mAllowCtrlSocketLevel != this.ALLOW_NO_CTRL_SOCKET_LEVEL) {
            AlarmManager am = (AlarmManager) connectivityServiceUtils.getContext(this).getSystemService("alarm");
            Intent intent = new Intent(INTENT_IM_RESUME_PROCESS);
            intent.setPackage(connectivityServiceUtils.getContext(this).getPackageName());
            this.mIMResumeIntent = PendingIntent.getBroadcast(connectivityServiceUtils.getContext(this), WIFI_PULS_CSP_ENABLED, intent, WIFI_PULS_CSP_ENABLED);
            int hr = getHourOfDay();
            long timeout = 2400000;
            if (hr >= 0 && hr <= IM_HOUR_OF_MORNING) {
                timeout = (long) (hr == IM_HOUR_OF_MORNING ? IM_TIMER_MORN_INTERVAL_MILLIS : IM_TIMER_NIGHT_INTERVAL_MILLIS);
            } else if (hr == IM_HOUR_OF_NIGHT) {
                timeout = 7200000;
            }
            am.setExactAndAllowWhileIdle(SERVICE_TYPE_OTHERS, SystemClock.elapsedRealtime() + timeout, this.mIMResumeIntent);
            Log.d(TAG, "CtrlSocket Resume IM  timer duration = " + timeout);
        }
    }

    private String getAllActPkgInWhiteList() {
        StringBuffer buf = new StringBuffer();
        for (String pkg : this.mCtrlSocketInfo.mScrOffActPkg) {
            String pkg2;
            buf.append(pkg2);
            buf.append("\t");
        }
        for (Entry entry : this.mCtrlSocketInfo.mSpecialPkgMap.entrySet()) {
            pkg2 = (String) entry.getKey();
            if (pkg2 != null) {
                String[] arraypkg = pkg2.split(":");
                if (arraypkg.length > 0) {
                    buf.append(arraypkg[WIFI_PULS_CSP_ENABLED]);
                    buf.append("\t");
                }
            }
        }
        String activePkg = buf.toString();
        Log.d(TAG, "getAllActPkgInWhiteList " + activePkg);
        return activePkg;
    }

    private void startMobileFilter() {
        configAppUidList();
        processCtrlSocket(Process.myUid(), this.SET_SAVING, WIFI_PULS_CSP_DISENABLED);
        int curHour = getHourOfDay();
        Log.d(TAG, "[PS filter]turnoffDC intent curHour = " + curHour);
        if (curHour < 0 || curHour >= IM_HOUR_OF_MORNING) {
            setNightClockTimer();
        } else {
            turnoffDC();
            setDayClockTimer();
        }
        Intent it = new Intent(MSG_SCROFF_CTRLSOCKET_STATS);
        it.putExtra("ctrl_socket_status", true);
        it.putExtra("ctrl_socket_list", getAllActPkgInWhiteList());
        this.mContext.sendBroadcastAsUser(it, UserHandle.ALL);
        Log.d(TAG, "CtrlSocket allow part broadcast");
    }

    private int generateRangeRandom(int sencond) {
        if (sencond <= 0) {
            Log.e(TAG, "Illegal Argument");
            return WIFI_PULS_CSP_ENABLED;
        }
        int rand = new Random().nextInt(sencond);
        if (rand >= sencond || rand < 0) {
            rand = WIFI_PULS_CSP_ENABLED;
        }
        return rand;
    }

    private void setDayClockTimer() {
        AlarmManager am = (AlarmManager) connectivityServiceUtils.getContext(this).getSystemService("alarm");
        Intent intent = new Intent(INTENT_DAY_CLOCK);
        intent.setPackage(connectivityServiceUtils.getContext(this).getPackageName());
        this.mDayClockIntent = PendingIntent.getBroadcast(connectivityServiceUtils.getContext(this), WIFI_PULS_CSP_ENABLED, intent, WIFI_PULS_CSP_ENABLED);
        long interval = getCurTimeToFixedTimeInMs(IM_HOUR_OF_MORNING, MINUTE_OF_MORNIG, WIFI_PULS_CSP_ENABLED) + ((long) (generateRangeRandom(RANDOM_TIME_SECOND) * IOTController.TYPE_MASTER));
        Log.d(TAG, "[PS filter]Day clock timer duration = " + interval);
        if (interval < 0) {
            Log.d(TAG, "[PS filter]Day clock timer is invalid");
        } else {
            am.setExactAndAllowWhileIdle(SERVICE_TYPE_OTHERS, SystemClock.elapsedRealtime() + interval, this.mDayClockIntent);
        }
    }

    private void cancelDayClockTimer() {
        if (this.mDayClockIntent != null) {
            AlarmManager am = (AlarmManager) connectivityServiceUtils.getContext(this).getSystemService("alarm");
            Log.d(TAG, "[PS filter]cancelDayClockTimer");
            am.cancel(this.mDayClockIntent);
        }
    }

    private void setNightClockTimer() {
        AlarmManager am = (AlarmManager) connectivityServiceUtils.getContext(this).getSystemService("alarm");
        Intent intent = new Intent(INTENT_NIGHT_CLOCK);
        intent.setPackage(connectivityServiceUtils.getContext(this).getPackageName());
        this.mNightClockIntent = PendingIntent.getBroadcast(connectivityServiceUtils.getContext(this), WIFI_PULS_CSP_ENABLED, intent, WIFI_PULS_CSP_ENABLED);
        long interval = getCurTimeToFixedTimeInMs(24, WIFI_PULS_CSP_ENABLED, WIFI_PULS_CSP_ENABLED);
        Log.d(TAG, "[PS filter]Night clock timer duration = " + interval);
        if (interval < 0) {
            Log.d(TAG, "[PS filter]Night clock timer is invalid");
        } else {
            am.setExactAndAllowWhileIdle(SERVICE_TYPE_OTHERS, SystemClock.elapsedRealtime() + interval, this.mNightClockIntent);
        }
    }

    private void cancelNightClockTimer() {
        if (this.mNightClockIntent != null) {
            AlarmManager am = (AlarmManager) connectivityServiceUtils.getContext(this).getSystemService("alarm");
            Log.d(TAG, "[PS filter]cancelNightClockTimer");
            am.cancel(this.mNightClockIntent);
        }
    }

    private long getCurTimeToFixedTimeInMs(int hour, int minite, int second) {
        Calendar cal = Calendar.getInstance();
        long curTimeInMs = cal.getTimeInMillis();
        cal.set(14, WIFI_PULS_CSP_ENABLED);
        cal.set(13, second);
        cal.set(12, minite);
        cal.set(11, hour);
        return cal.getTimeInMillis() - curTimeInMs;
    }

    private void setIMPendingTimer() {
        if (this.mCtrlSocketInfo.mAllowCtrlSocketLevel != this.ALLOW_NO_CTRL_SOCKET_LEVEL) {
            AlarmManager am = (AlarmManager) connectivityServiceUtils.getContext(this).getSystemService("alarm");
            Intent intent = new Intent(INTENT_IM_PENDING_PROCESS);
            intent.setPackage(connectivityServiceUtils.getContext(this).getPackageName());
            this.mIMPendingIntent = PendingIntent.getBroadcast(connectivityServiceUtils.getContext(this), WIFI_PULS_CSP_ENABLED, intent, WIFI_PULS_CSP_ENABLED);
            am.setExactAndAllowWhileIdle(SERVICE_TYPE_OTHERS, SystemClock.elapsedRealtime() + 30000, this.mIMPendingIntent);
            Log.d(TAG, "CtrlSocket Pending IM timer duration = 30000");
        }
    }

    private void turnoffDC() {
        try {
            if (getDataEnabled()) {
                setMobileDataEnabled(MODULE_POWERSAVING, IS_CHINA);
                this.mShowDlgTurnOfDC = IS_CHINA;
                setTurnOffDCState(WIFI_PULS_CSP_DISENABLED);
            }
        } catch (Exception e) {
            loge("have exception in turnoffDC function!");
        }
    }

    private void turnonDC() {
        try {
            if (getTurnOffDCState()) {
                setMobileDataEnabled(MODULE_POWERSAVING, true);
                setTurnOffDCState(WIFI_PULS_CSP_ENABLED);
            }
        } catch (Exception e) {
            loge("have exception in turnonDC function!");
        }
    }

    private boolean getPowerSavingState() {
        if (System.getInt(connectivityServiceUtils.getContext(this).getContentResolver(), POWER_SAVING_ON, WIFI_PULS_CSP_ENABLED) == WIFI_PULS_CSP_DISENABLED) {
            return true;
        }
        return IS_CHINA;
    }

    private void setTurnOffDCState(int val) {
        System.putInt(connectivityServiceUtils.getContext(this).getContentResolver(), TURN_OFF_DC_STATE, val);
    }

    private boolean getTurnOffDCState() {
        boolean retVal = System.getInt(connectivityServiceUtils.getContext(this).getContentResolver(), TURN_OFF_DC_STATE, WIFI_PULS_CSP_ENABLED) == WIFI_PULS_CSP_DISENABLED ? true : IS_CHINA;
        log("TurnOffDCState: " + retVal);
        return retVal;
    }

    private boolean isConnectedOrConnectingOrSuspended(NetworkInfo info) {
        boolean z = true;
        synchronized (this) {
            if (!(info.getState() == State.CONNECTED || info.getState() == State.CONNECTING)) {
                if (info.getState() != State.SUSPENDED) {
                    z = IS_CHINA;
                }
            }
        }
        return z;
    }

    private AlertDialog createWarningToPdp() {
        Builder buider;
        String enable_Not_Remind_Function = Systemex.getString(connectivityServiceUtils.getContext(this).getContentResolver(), ENABLE_NOT_REMIND_FUNCTION);
        CheckBox checkBox = null;
        if (VALUE_ENABLE_NOT_REMIND_FUNCTION.equals(enable_Not_Remind_Function)) {
            int themeID = connectivityServiceUtils.getContext(this).getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null);
            buider = new Builder(new ContextThemeWrapper(connectivityServiceUtils.getContext(this), themeID), themeID);
            View view = LayoutInflater.from(buider.getContext()).inflate(34013280, null);
            checkBox = (CheckBox) view.findViewById(34603226);
            buider.setView(view);
            buider.setTitle(17039380);
        } else {
            buider = new Builder(connectivityServiceUtils.getContext(this), connectivityServiceUtils.getContext(this).getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null));
            buider.setTitle(17039380);
            buider.setMessage(33685526);
        }
        CheckBox finalBox = checkBox;
        buider.setIcon(17301543);
        buider.setPositiveButton(17040512, new AnonymousClass7(enable_Not_Remind_Function, finalBox));
        buider.setNegativeButton(17040513, new AnonymousClass8(enable_Not_Remind_Function, finalBox));
        buider.setOnCancelListener(new AnonymousClass9(enable_Not_Remind_Function, finalBox));
        AlertDialog dialog = buider.create();
        dialog.getWindow().setType(2008);
        return dialog;
    }

    protected void registerPhoneStateListener(Context context) {
        ((TelephonyManager) context.getSystemService("phone")).listen(this.mPhoneStateListener, 32);
    }

    private final void updateCallState(int state) {
        if (this.mRemindService || SystemProperties.getBoolean("gsm.huawei.RemindDataService", IS_CHINA)) {
            int phoneState = state;
            if (state == 0) {
                if (this.mShowDlgEndCall && this.mDataServiceToPdpDialog == null) {
                    this.mDataServiceToPdpDialog = createWarningToPdp();
                    this.mDataServiceToPdpDialog.show();
                    this.mShowDlgEndCall = IS_CHINA;
                }
            } else if (this.mDataServiceToPdpDialog != null) {
                this.mDataServiceToPdpDialog.dismiss();
                this.mDataServiceToPdpDialog = null;
                this.mShowDlgEndCall = true;
            }
        }
    }

    protected void registerBootStateListener(Context context) {
        new MobileEnabledSettingObserver(new Handler()).register();
    }

    protected boolean needSetUserDataEnabled(boolean enabled) {
        int dataStatus = Global.getInt(connectivityServiceUtils.getContext(this).getContentResolver(), "mobile_data", WIFI_PULS_CSP_DISENABLED);
        if (!shouldShowThePdpWarning() || dataStatus != 0 || !enabled) {
            return true;
        }
        if (this.mShowDlgTurnOfDC) {
            this.mHandler.sendEmptyMessage(WIFI_PULS_CSP_ENABLED);
            return IS_CHINA;
        }
        this.mShowDlgTurnOfDC = true;
        return true;
    }

    private void updateReminderSetting(boolean chooseNotRemind) {
        if (chooseNotRemind) {
            System.putInt(connectivityServiceUtils.getContext(this).getContentResolver(), WHETHER_SHOW_PDP_WARNING, VALUE_NOT_SHOW_PDP);
        }
    }

    private boolean shouldShowThePdpWarning() {
        boolean z = IS_CHINA;
        if (WrapperFactory.getMSimTelephonyManagerWrapper().isMultiSimEnabled()) {
            return shouldShowThePdpWarningMsim();
        }
        String enable_Not_Remind_Function = Systemex.getString(connectivityServiceUtils.getContext(this).getContentResolver(), ENABLE_NOT_REMIND_FUNCTION);
        boolean remindDataAllow = SystemProperties.getBoolean("gsm.huawei.RemindDataService", IS_CHINA);
        int pdpWarningValue = System.getInt(connectivityServiceUtils.getContext(this).getContentResolver(), WHETHER_SHOW_PDP_WARNING, VALUE_SHOW_PDP);
        if (!VALUE_ENABLE_NOT_REMIND_FUNCTION.equals(enable_Not_Remind_Function)) {
            return remindDataAllow;
        }
        if (remindDataAllow && pdpWarningValue == VALUE_SHOW_PDP) {
            z = true;
        }
        return z;
    }

    private boolean checkDataServiceRemindMsim() {
        int lDataVal = WrapperFactory.getMSimTelephonyManagerWrapper().getPreferredDataSubscription();
        if (lDataVal == 0) {
            if (TelephonyManager.getDefault().hasIccCard(lDataVal)) {
                return SystemProperties.getBoolean("gsm.huawei.RemindDataService", IS_CHINA);
            }
            return SystemProperties.getBoolean("gsm.huawei.RemindDataService_1", IS_CHINA);
        } else if (WIFI_PULS_CSP_DISENABLED == lDataVal) {
            return SystemProperties.getBoolean("gsm.huawei.RemindDataService_1", IS_CHINA);
        } else {
            return IS_CHINA;
        }
    }

    private boolean shouldShowThePdpWarningMsim() {
        boolean z = true;
        String enableNotRemindFunction = Systemex.getString(this.mContext.getContentResolver(), ENABLE_NOT_REMIND_FUNCTION);
        boolean remindDataAllow = IS_CHINA;
        int lDataVal = WrapperFactory.getMSimTelephonyManagerWrapper().getPreferredDataSubscription();
        if (WIFI_PULS_CSP_DISENABLED == lDataVal) {
            remindDataAllow = SystemProperties.getBoolean("gsm.huawei.RemindDataService_1", IS_CHINA);
        } else if (lDataVal == 0) {
            remindDataAllow = SystemProperties.getBoolean("gsm.huawei.RemindDataService", IS_CHINA);
        }
        int pdpWarningValue = System.getInt(this.mContext.getContentResolver(), WHETHER_SHOW_PDP_WARNING, VALUE_SHOW_PDP);
        if (!VALUE_ENABLE_NOT_REMIND_FUNCTION.equals(enableNotRemindFunction)) {
            return remindDataAllow;
        }
        if (!(remindDataAllow && pdpWarningValue == VALUE_SHOW_PDP)) {
            z = IS_CHINA;
        }
        return z;
    }

    private boolean shouldDisablePortalCheck(String ssid) {
        if (ssid != null) {
            log("wifi ssid: " + ssid);
            if (ssid.length() > SERVICE_TYPE_OTHERS && ssid.charAt(WIFI_PULS_CSP_ENABLED) == '\"' && ssid.charAt(ssid.length() + INVALID_PID) == '\"') {
                ssid = ssid.substring(WIFI_PULS_CSP_DISENABLED, ssid.length() + INVALID_PID);
            }
        }
        if (WIFI_PULS_CSP_DISENABLED == Secure.getInt(this.mContext.getContentResolver(), "user_setup_complete", WIFI_PULS_CSP_ENABLED) && WIFI_PULS_CSP_DISENABLED == Secure.getInt(this.mContext.getContentResolver(), "device_provisioned", WIFI_PULS_CSP_ENABLED) && SystemProperties.getBoolean("ro.config.hw_disable_portal", IS_CHINA)) {
            log("stop portal check for orange");
            return true;
        } else if ("CMCC".equalsIgnoreCase(SystemProperties.get("ro.config.operators", AppHibernateCst.INVALID_PKG)) && "CMCC".equals(ssid)) {
            log("stop portal check for CMCC");
            return true;
        } else if (WIFI_PULS_CSP_DISENABLED == System.getInt(connectivityServiceUtils.getContext(this).getContentResolver(), DISABLE_PORTAL_CHECK, WIFI_PULS_CSP_ENABLED)) {
            System.putInt(connectivityServiceUtils.getContext(this).getContentResolver(), DISABLE_PORTAL_CHECK, WIFI_PULS_CSP_ENABLED);
            log("stop portal check for airsharing");
            return true;
        } else if (Global.getInt(this.mContext.getContentResolver(), "device_provisioned", WIFI_PULS_CSP_ENABLED) == 0 && "true".equals(Systemex.getString(this.mContext.getContentResolver(), "wifi.challenge.required"))) {
            log("setup guide wifi disable portal, and does not start browser!");
            return true;
        } else if (WIFI_PULS_CSP_DISENABLED == System.getInt(connectivityServiceUtils.getContext(this).getContentResolver(), WIFI_AP_MANUAL_CONNECT, WIFI_PULS_CSP_ENABLED)) {
            System.putInt(connectivityServiceUtils.getContext(this).getContentResolver(), WIFI_AP_MANUAL_CONNECT, WIFI_PULS_CSP_ENABLED);
            log("portal ap manual connect");
            return IS_CHINA;
        } else if (WifiProCommonUtils.isWifiProEnable(this.mContext) && WifiProCommonUtils.isQueryActivityMatched(this.mContext, WifiProCommonUtils.HUAWEI_SETTINGS_WLAN)) {
            return IS_CHINA;
        } else {
            log("portal ap auto connect");
            return true;
        }
    }

    protected boolean startBrowserForWifiPortal(Notification notification, String ssid) {
        if (WifiProCommonUtils.isWifiProEnable(this.mContext) && WifiProCommonUtils.isPortalBackground()) {
            log("WLAN+ enabled, don't pop up portal notification status bar again!");
            WifiProCommonUtils.portalBackgroundStatusChanged(IS_CHINA);
            return true;
        } else if (shouldDisablePortalCheck(ssid)) {
            log("do not start browser, popup system notification");
            return IS_CHINA;
        } else {
            log("setNotificationVisible: cancel notification and start browser directly for TYPE_WIFI..");
            try {
                Intent intent;
                if (IS_CHINA) {
                    String operator = TelephonyManager.getDefault().getNetworkOperator();
                    if (operator == null || operator.length() == 0 || !operator.startsWith(COUNTRY_CODE_CN)) {
                        this.mURL = new URL("http://" + this.mServer + "/generate_204");
                        intent = new Intent("android.intent.action.VIEW", Uri.parse(this.mURL.toString()));
                        intent.setFlags(272629760);
                        notification.contentIntent = PendingIntent.getActivity(connectivityServiceUtils.getContext(this), WIFI_PULS_CSP_ENABLED, intent, WIFI_PULS_CSP_ENABLED);
                        try {
                            intent.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
                            connectivityServiceUtils.getContext(this).startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            try {
                                log("default browser not exist..");
                                if (isSetupWizardCompleted()) {
                                    notification.contentIntent.send();
                                } else {
                                    log("setup wizard is not completed");
                                    Network network = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getActiveNetwork();
                                    Intent intentPortal = new Intent("android.net.conn.CAPTIVE_PORTAL");
                                    intentPortal.putExtra("android.net.extra.NETWORK", network);
                                    intentPortal.putExtra("android.net.extra.CAPTIVE_PORTAL", new CaptivePortal(new ICaptivePortal.Stub() {
                                        public void appResponse(int response) {
                                        }
                                    }));
                                    intentPortal.putExtra("android.net.extra.CAPTIVE_PORTAL_URL", this.mURL.toString());
                                    intentPortal.setFlags(272629760);
                                    intentPortal.putExtra(FLAG_SETUP_WIZARD, true);
                                    connectivityServiceUtils.getContext(this).startActivity(intentPortal);
                                }
                            } catch (CanceledException e2) {
                                log("Sending contentIntent failed: " + e2);
                            } catch (ActivityNotFoundException e3) {
                                log("Activity not found: " + e3);
                            }
                        }
                        return true;
                    }
                    this.mURL = new URL(HwNetworkPropertyChecker.CHINA_MAINLAND_MAIN_SERVER);
                    intent = new Intent("android.intent.action.VIEW", Uri.parse(this.mURL.toString()));
                    intent.setFlags(272629760);
                    notification.contentIntent = PendingIntent.getActivity(connectivityServiceUtils.getContext(this), WIFI_PULS_CSP_ENABLED, intent, WIFI_PULS_CSP_ENABLED);
                    intent.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
                    connectivityServiceUtils.getContext(this).startActivity(intent);
                    return true;
                }
                this.mURL = new URL("http://" + this.mServer + "/generate_204");
                intent = new Intent("android.intent.action.VIEW", Uri.parse(this.mURL.toString()));
                intent.setFlags(272629760);
                notification.contentIntent = PendingIntent.getActivity(connectivityServiceUtils.getContext(this), WIFI_PULS_CSP_ENABLED, intent, WIFI_PULS_CSP_ENABLED);
                intent.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
                connectivityServiceUtils.getContext(this).startActivity(intent);
                return true;
            } catch (MalformedURLException e4) {
                log("MalformedURLException " + e4);
            }
        }
    }

    public boolean isSystemBootComplete() {
        return this.sendWifiBroadcastAfterBootCompleted;
    }

    protected void hintUserSwitchToMobileWhileWifiDisconnected(State state, int type) {
        if (WifiProCommonUtils.isWifiSelfCuring() && state == State.DISCONNECTED && type == WIFI_PULS_CSP_DISENABLED) {
            Log.d("HwSelfCureEngine", "DISCONNECTED, but enableDefaultTypeAPN-->UP is ignored due to wifi self curing.");
        } else {
            this.wifiDisconnectManager.hintUserSwitchToMobileWhileWifiDisconnected(state, type);
        }
    }

    protected void enableDefaultTypeApnWhenWifiConnectionStateChanged(State state, int type) {
        if (!(state == State.DISCONNECTED && type == WIFI_PULS_CSP_DISENABLED) && state == State.CONNECTED && type == WIFI_PULS_CSP_DISENABLED) {
            if (WifiProCommonUtils.isWifiSelfCuring()) {
                Log.d("HwSelfCureEngine", "CONNECTED, but enableDefaultTypeAPN-->DOWN is ignored due to wifi self curing.");
                return;
            }
            enableDefaultTypeAPN(IS_CHINA);
        }
    }

    private void sendBlueToothTetheringBroadcast(boolean isBttConnected) {
        log("sendBroad bt_tethering_connect_state = " + isBttConnected);
        Intent intent = new Intent("android.intent.action.BlueToothTethering_NETWORK_CONNECTION_CHANGED");
        intent.putExtra("btt_connect_state", isBttConnected);
        connectivityServiceUtils.getContext(this).sendBroadcast(intent);
    }

    protected void enableDefaultTypeApnWhenBlueToothTetheringStateChanged(NetworkAgentInfo networkAgent, NetworkInfo newInfo) {
        if (newInfo.getType() == 7) {
            log("enter BlueToothTethering State Changed");
            State state = newInfo.getState();
            if (state == State.CONNECTED) {
                sendBlueToothTetheringBroadcast(true);
                enableDefaultTypeAPN(IS_CHINA);
            } else if (state == State.DISCONNECTED) {
                sendBlueToothTetheringBroadcast(IS_CHINA);
                enableDefaultTypeAPN(true);
            }
        }
    }

    public void makeDefaultAndHintUser(NetworkAgentInfo newNetwork) {
        this.wifiDisconnectManager.makeDefaultAndHintUser(newNetwork);
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        switch (code) {
            case IOTController.TYPE_SLAVE /*1001*/:
                String register_pkg = data.readString();
                Log.d(TAG, "CtrlSocket registerPushSocket pkg = " + register_pkg);
                registerPushSocket(register_pkg);
                return true;
            case EventTracker.TRACK_TYPE_KILL /*1002*/:
                String unregister_pkg = data.readString();
                Log.d(TAG, "CtrlSocket unregisterPushSocket pkg = " + unregister_pkg);
                unregisterPushSocket(unregister_pkg);
                return true;
            case EventTracker.TRACK_TYPE_TRIG /*1003*/:
                int pid = data.readInt();
                int cmd = data.readInt();
                int para = data.readInt();
                Log.d(TAG, "CtrlSocket processCtrlSocket pid = " + pid + " cmd = " + cmd + " para = " + para);
                processCtrlSocket(pid, cmd, para);
                return true;
            case EventTracker.TRACK_TYPE_END /*1004*/:
                reply.writeString(getActPkgInWhiteList());
                return true;
            case EventTracker.TRACK_TYPE_STOP /*1005*/:
                reply.writeInt(this.mCtrlSocketInfo.mAllowCtrlSocketLevel);
                return true;
            case HwPackageManagerService.transaction_sendLimitedPackageBroadcast /*1006*/:
                Log.d(TAG, "CtrlSocket getCtrlSocketVersion = v2");
                reply.writeString(ctrl_socket_version);
                return true;
            case HwPackageManagerService.TRANSACTION_CODE_GET_PREINSTALLED_APK_LIST /*1007*/:
                String register_spkg = data.readString();
                int call_pid = Binder.getCallingPid();
                Log.d(TAG, "CtrlSocket registerSpecialPid name = " + register_spkg + " pid = " + call_pid);
                registerSpecialSocket(call_pid, register_spkg);
                break;
            case HwPackageManagerService.TRANSACTION_CODE_CHECK_GMS_IS_UNINSTALLED /*1008*/:
                String unregiter_spkg = data.readString();
                Log.d(TAG, "CtrlSocket unregistSpecialPid name = " + unregiter_spkg);
                unregisterSpecialSocket(unregiter_spkg);
                break;
            case HwPackageManagerService.TRANSACTION_CODE_DELTE_GMS_FROM_UNINSTALLED_DELAPP /*1009*/:
                String reg_pkg = data.readString();
                int register_pid = data.readInt();
                Log.d(TAG, "CtrlSocket registSpecialPid name = " + reg_pkg + " reg_pid = " + register_pid);
                registerSpecialSocket(register_pid, reg_pkg);
                break;
            case 1101:
                data.enforceInterface(descriptor);
                int enableInt = data.readInt();
                Log.d(TAG, "needSetUserDataEnabled enableInt = " + enableInt);
                boolean result = needSetUserDataEnabled(enableInt == WIFI_PULS_CSP_DISENABLED ? true : IS_CHINA);
                Log.d(TAG, "needSetUserDataEnabled result = " + result);
                reply.writeNoException();
                reply.writeInt(result ? WIFI_PULS_CSP_DISENABLED : WIFI_PULS_CSP_ENABLED);
                return true;
        }
        return super.onTransact(code, data, reply, flags);
    }

    private void registerPushSocket(String pkgName) {
        if (pkgName != null) {
            boolean isToAdd = IS_CHINA;
            for (String pkg : this.mCtrlSocketInfo.mRegisteredPkg) {
                if (pkg.equals(pkgName)) {
                    return;
                }
            }
            if (this.mCtrlSocketInfo.mRegisteredCount >= this.MAX_REGISTERED_PKG_NUM) {
                for (String pkg2 : this.mCtrlSocketInfo.mPushWhiteListPkg) {
                    if (pkg2.equals(pkgName)) {
                        isToAdd = true;
                    }
                }
            } else {
                isToAdd = true;
            }
            if (isToAdd) {
                CtrlSocketInfo ctrlSocketInfo = this.mCtrlSocketInfo;
                ctrlSocketInfo.mRegisteredCount += WIFI_PULS_CSP_DISENABLED;
                this.mCtrlSocketInfo.mRegisteredPkg.add(pkgName);
                updateRegisteredPkg();
            }
        }
    }

    private void unregisterPushSocket(String pkgName) {
        if (pkgName != null) {
            int count = WIFI_PULS_CSP_ENABLED;
            boolean isMatch = IS_CHINA;
            for (String pkg : this.mCtrlSocketInfo.mRegisteredPkg) {
                if (pkg.equals(pkgName)) {
                    isMatch = true;
                    break;
                }
                count += WIFI_PULS_CSP_DISENABLED;
            }
            if (isMatch) {
                CtrlSocketInfo ctrlSocketInfo = this.mCtrlSocketInfo;
                ctrlSocketInfo.mRegisteredCount += INVALID_PID;
                this.mCtrlSocketInfo.mRegisteredPkg.remove(count);
                updateRegisteredPkg();
            }
        }
    }

    private void registerSpecialSocket(int pid, String name) {
        if (name != null) {
            boolean isInWhiteList = IS_CHINA;
            for (String pkg : this.mCtrlSocketInfo.mSpecialWhiteListPkg) {
                if (pkg.equals(name)) {
                    isInWhiteList = true;
                }
            }
            if (isInWhiteList) {
                if (this.mCtrlSocketInfo.mSpecialPkgMap.containsKey(name)) {
                    int recordPid = ((Integer) this.mCtrlSocketInfo.mSpecialPkgMap.get(name)).intValue();
                    if (!(pid == recordPid || pid == recordPid)) {
                        this.mCtrlSocketInfo.mSpecialPkgMap.put(name, Integer.valueOf(pid));
                        if (isAllowBastetFilter) {
                            procCtrlSockets(pid, this.CANCEL_SPECIAL_PID, WIFI_PULS_CSP_DISENABLED);
                            procCtrlSockets(pid, this.SET_SPECIAL_PID, WIFI_PULS_CSP_DISENABLED);
                        }
                    }
                } else {
                    Log.d(TAG, "CtrlSocket add to SpecialMap pid = " + pid);
                    this.mCtrlSocketInfo.mSpecialPkgMap.put(name, Integer.valueOf(pid));
                    if (isAllowBastetFilter && mBastetFilterEnable) {
                        procCtrlSockets(pid, this.SET_SPECIAL_PID, WIFI_PULS_CSP_ENABLED);
                    }
                }
            }
        }
    }

    private void unregisterSpecialSocket(String name) {
        if (name != null && this.mCtrlSocketInfo.mSpecialPkgMap.containsKey(name)) {
            int pid = ((Integer) this.mCtrlSocketInfo.mSpecialPkgMap.get(name)).intValue();
            if (isAllowBastetFilter) {
                procCtrlSockets(pid, this.CANCEL_SPECIAL_PID, WIFI_PULS_CSP_DISENABLED);
            }
            this.mCtrlSocketInfo.mSpecialPkgMap.remove(name);
        }
    }

    private int processCtrlSocket(int pid, int cmd, int param) {
        int ret = INVALID_PID;
        if (isAllowBastetFilter) {
            ret = procCtrlSockets(pid, cmd, param);
        }
        if (ret >= 0 && this.SET_SAVING == cmd) {
            System.putInt(connectivityServiceUtils.getContext(this).getContentResolver(), "CtrlSocketSaving", param);
        }
        Log.d(TAG, "CtrlSocket processCtrlSocket pid = " + pid + " cmd = " + cmd + " param = " + param + " ret = " + ret);
        return ret;
    }

    private String getActPkgInWhiteList() {
        if (this.ALLOW_PART_CTRL_SOCKET_LEVEL != this.mCtrlSocketInfo.mAllowCtrlSocketLevel) {
            return null;
        }
        StringBuffer activePkg = new StringBuffer();
        for (String pkg : this.mCtrlSocketInfo.mScrOffActPkg) {
            activePkg.append(pkg);
            activePkg.append("\t");
        }
        return activePkg.toString();
    }

    private void calcUseCtrlSocketLevel() {
        this.mCtrlSocketInfo.mAllowCtrlSocketLevel = this.ALLOW_NO_CTRL_SOCKET_LEVEL;
        this.mCtrlSocketInfo.mScrOffActiveCount = WIFI_PULS_CSP_ENABLED;
        this.mCtrlSocketInfo.mScrOffActPkg.clear();
        int pwrSaveMode = readSysPwrSaveMode();
        if (this.SUPER_POWER_SAVING_MODE != pwrSaveMode && this.POWER_SAVING_MODE != pwrSaveMode) {
            CtrlSocketInfo ctrlSocketInfo;
            if (this.NORMAL_POWER_SAVING_MODE == pwrSaveMode) {
                this.mCtrlSocketInfo.mAllowCtrlSocketLevel = this.ALLOW_ALL_CTRL_SOCKET_LEVEL;
                for (String pkg : this.mCtrlSocketInfo.mRegisteredPkg) {
                    this.mCtrlSocketInfo.mScrOffActPkg.add(pkg);
                    ctrlSocketInfo = this.mCtrlSocketInfo;
                    ctrlSocketInfo.mScrOffActiveCount += WIFI_PULS_CSP_DISENABLED;
                }
                return;
            }
            if (this.mCtrlSocketInfo.mRegisteredCount != 0) {
                for (String pkg2 : this.mCtrlSocketInfo.mRegisteredPkg) {
                    for (String wlPkg : this.mCtrlSocketInfo.mPushWhiteListPkg) {
                        if (pkg2.equals(wlPkg)) {
                            ctrlSocketInfo = this.mCtrlSocketInfo;
                            ctrlSocketInfo.mScrOffActiveCount += WIFI_PULS_CSP_DISENABLED;
                            this.mCtrlSocketInfo.mScrOffActPkg.add(pkg2);
                        }
                    }
                }
                Log.d(TAG, "CtrlSocket calcUseCtrlSocketLevel Active.Count = " + this.mCtrlSocketInfo.mScrOffActiveCount);
                if (this.mCtrlSocketInfo.mScrOffActiveCount > 0) {
                    this.mCtrlSocketInfo.mAllowCtrlSocketLevel = this.ALLOW_PART_CTRL_SOCKET_LEVEL;
                    return;
                }
            }
            if (this.mCtrlSocketInfo.mSpecialPkgMap.size() != 0) {
                Log.d(TAG, "CtrlSocket calcUseCtrlSocketLevel Special.size = " + this.mCtrlSocketInfo.mSpecialPkgMap.size());
                this.mCtrlSocketInfo.mAllowCtrlSocketLevel = this.ALLOW_SPECIAL_CTRL_SOCKET_LEVEL;
            }
        }
    }

    private int getUseCtrlSocketLevel() {
        return this.mCtrlSocketInfo.mAllowCtrlSocketLevel;
    }

    private void resetUseCtrlSocketLevel() {
        this.mCtrlSocketInfo.mAllowCtrlSocketLevel = this.ALLOW_NO_CTRL_SOCKET_LEVEL;
    }

    private boolean isWifiAvailable() {
        ConnectivityManager connMgr = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        if (connMgr == null) {
            return IS_CHINA;
        }
        NetworkInfo ni = connMgr.getNetworkInfo(WIFI_PULS_CSP_DISENABLED);
        return (ni == null || !ni.isConnected()) ? IS_CHINA : true;
    }

    private void getCtrlSocketPushWhiteList() {
        String wlPkg = Secure.getString(this.mContext.getContentResolver(), "push_white_apps");
        if (wlPkg != null) {
            String[] str = wlPkg.split(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
            if (str != null && str.length > 0) {
                this.mCtrlSocketInfo.mPushWhiteListPkg.clear();
                for (int i = WIFI_PULS_CSP_ENABLED; i < str.length; i += WIFI_PULS_CSP_DISENABLED) {
                    this.mCtrlSocketInfo.mPushWhiteListPkg.add(str[i]);
                    Log.d(TAG, "CtrlSocket PushWhiteList[" + i + "] = " + str[i]);
                }
            }
        }
    }

    private void getCtrlSocketSpecialWhiteList() {
        String wlPkg = Secure.getString(this.mContext.getContentResolver(), "net_huawei_apps");
        if (wlPkg == null) {
            wlPkg = IM_SPECIAL_PROC;
        }
        String[] str = wlPkg.split(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
        if (str != null && str.length > 0) {
            this.mCtrlSocketInfo.mSpecialWhiteListPkg.clear();
            for (int i = WIFI_PULS_CSP_ENABLED; i < str.length; i += WIFI_PULS_CSP_DISENABLED) {
                this.mCtrlSocketInfo.mSpecialWhiteListPkg.add(str[i]);
                Log.d(TAG, "CtrlSocket SpecialWhiteList[" + i + "] = " + str[i]);
            }
        }
    }

    private int readSysPwrSaveMode() {
        if (SystemProperties.getBoolean("sys.super_power_save", IS_CHINA)) {
            return this.SUPER_POWER_SAVING_MODE;
        }
        return System.getInt(this.mContext.getContentResolver(), "SmartModeStatus", INVALID_PID);
    }

    private void restoreScrOnStatus() {
        cancelNightClockTimer();
        cancelDayClockTimer();
        if (this.mIMWakeLock.isHeld()) {
            this.mIMWakeLock.release();
        }
        synchronized (this.mPowerSavingLock) {
            Log.d(TAG, "CtrlSocket restoreScrOnStatus");
            this.mShouldPowerSave = IS_CHINA;
            if (this.ALLOW_PART_CTRL_SOCKET_LEVEL == this.mCtrlSocketInfo.mAllowCtrlSocketLevel || this.ALLOW_SPECIAL_CTRL_SOCKET_LEVEL == this.mCtrlSocketInfo.mAllowCtrlSocketLevel) {
                this.mContext.sendBroadcastAsUser(new Intent(MSG_ALL_CTRLSOCKET_ALLOWED), UserHandle.ALL);
                Log.d(TAG, "CtrlSocket restoreScrOnStatus all allowed");
            }
            resetUseCtrlSocketLevel();
            cancelPowerSaving();
            Log.d(TAG, "CtrlSocket restoreScrOnStatus reset");
            if (this.mStartPowerSaving) {
                if (this.wifiDisconnectManager.mWifiToPdpDialog == null) {
                    log("CtrlSocket restoreScrOnStatus turnonDC");
                    turnonDC();
                }
                this.mStartPowerSaving = IS_CHINA;
            }
        }
    }

    private void getCtrlSocketRegisteredPkg() {
        String registeredPkg = Secure.getString(this.mContext.getContentResolver(), "registered_pkgs");
        if (registeredPkg != null) {
            String[] str = registeredPkg.split(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
            if (str != null && str.length > 0) {
                this.mCtrlSocketInfo.mRegisteredPkg.clear();
                this.mCtrlSocketInfo.mRegisteredCount = WIFI_PULS_CSP_ENABLED;
                for (int i = WIFI_PULS_CSP_ENABLED; i < str.length; i += WIFI_PULS_CSP_DISENABLED) {
                    this.mCtrlSocketInfo.mRegisteredPkg.add(str[i]);
                    CtrlSocketInfo ctrlSocketInfo = this.mCtrlSocketInfo;
                    ctrlSocketInfo.mRegisteredCount += WIFI_PULS_CSP_DISENABLED;
                }
            }
        }
    }

    private void updateRegisteredPkg() {
        StringBuffer registeredPkg = new StringBuffer();
        for (String pkg : this.mCtrlSocketInfo.mRegisteredPkg) {
            registeredPkg.append(pkg);
            registeredPkg.append(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
        }
        Secure.putString(this.mContext.getContentResolver(), "registered_pkgs", registeredPkg.toString());
    }

    private void initCtrlSocket(Context context) {
        if (SystemProperties.getBoolean("ro.config.hw_power_saving", IS_CHINA)) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.SCREEN_ON");
            filter.addAction("android.intent.action.SCREEN_OFF");
            filter.addAction(INTENT_TURNOFF_DC);
            filter.addAction(ACTION_OF_ANDROID_BOOT_COMPLETED);
            filter.addAction("android.net.wifi.STATE_CHANGE");
            filter.addAction("android.intent.action.USER_PRESENT");
            filter.addAction(INTENT_NIGHT_CLOCK);
            filter.addAction(INTENT_DAY_CLOCK);
            this.mContext.registerReceiver(this.mIntentReceiver, filter);
            getCtrlSocketRegisteredPkg();
            getCtrlSocketPushWhiteList();
            getCtrlSocketSpecialWhiteList();
            this.mDbObserver = new AnonymousClass11(new Handler());
            context.getContentResolver().registerContentObserver(this.WHITELIST_URI, IS_CHINA, this.mDbObserver);
        }
    }

    private void setMobileDataEnabled(String module, boolean enabled) {
        Log.d(TAG, "module:" + module + " setMobileDataEnabled enabled = " + enabled);
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
        if (tm != null) {
            tm.setDataEnabled(enabled);
            tm.setDataEnabledProperties(module, enabled);
        }
    }

    private boolean getDataEnabled() {
        boolean ret = IS_CHINA;
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
        if (tm != null) {
            ret = tm.getDataEnabled();
        }
        Log.d(TAG, "CtrlSocket getMobileDataEnabled enabled = " + ret);
        return ret;
    }

    public boolean getMobileDataEnabled() {
        boolean ret = IS_CHINA;
        if (!this.mIsSimStateChanged) {
            return IS_CHINA;
        }
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
        if (tm != null) {
            boolean ret2 = IS_CHINA;
            int slotId = WIFI_PULS_CSP_ENABLED;
            while (slotId < tm.getPhoneCount()) {
                try {
                    if (tm.getSimState(slotId) == IM_HOUR_OF_MORNING) {
                        ret2 = true;
                    }
                    slotId += WIFI_PULS_CSP_DISENABLED;
                } catch (NullPointerException e) {
                    Log.d(TAG, "getMobileDataEnabled NPE");
                }
            }
            if (ret2) {
                ret = tm.getDataEnabled();
            } else {
                Log.d(TAG, "all sim card not ready,return false");
                return IS_CHINA;
            }
        }
        Log.d(TAG, "CtrlSocket getMobileDataEnabled = " + ret);
        return ret;
    }

    private void enableDefaultTypeAPN(boolean enabled) {
        Log.d(TAG, "enableDefaultTypeAPN= " + enabled);
        Log.d(TAG, "DEFAULT_MOBILE_ENABLE before state is " + SystemProperties.get("sys.defaultapn.enabled", "true"));
        SystemProperties.set("sys.defaultapn.enabled", enabled ? "true" : "false");
        HwTelephonyManagerInner hwTm = HwTelephonyManagerInner.getDefault();
        if (hwTm != null) {
            hwTm.setDefaultMobileEnable(enabled);
        }
    }

    private void registerSimStateReceiver(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SIM_STATE_CHANGED");
        context.registerReceiver(this.mSimStateReceiver, filter);
    }

    private void handleShowEnablePdpDialog() {
        if (this.mDataServiceToPdpDialog == null) {
            this.mDataServiceToPdpDialog = createWarningToPdp();
            this.mDataServiceToPdpDialog.show();
        }
    }

    private void handleIMReceivingMsgAction() {
        Log.d(TAG, "handleIMReceivingMsgAction");
        Log.d(TAG, "CtrlSocketDo set Power Saving Mode 0");
        processCtrlSocket(Process.myUid(), this.SET_SAVING, WIFI_PULS_CSP_ENABLED);
        if (this.mCtrlSocketInfo.mAllowCtrlSocketLevel != this.ALLOW_NO_CTRL_SOCKET_LEVEL) {
            turnonDC();
            this.mIMArrayList.clear();
            this.mIMArrayList.add(MM_PKG_NAME);
            Intent it = new Intent(PG_PENDING_ACTION);
            it.putExtra("enable", IS_CHINA);
            it.putExtra("applist", this.mIMArrayList);
            it.putExtra(HwSecDiagnoseConstant.ANTIMAL_APK_TYPE, WIFI_PULS_CSP_DISENABLED);
            this.mContext.sendBroadcast(it, "com.huawei.powergenie.receiverPermission");
            Log.d(TAG, "tell powergenie to resume applist = " + this.mIMArrayList);
            setIMPendingTimer();
        }
    }

    private void registerTetheringReceiver(Context context) {
        if (HwDeliverInfo.isIOTVersion() && SystemProperties.getBoolean("ro.config.persist_usb_tethering", IS_CHINA)) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(HwGnssCommParam.ACTION_USB_STATE);
            context.registerReceiver(this.mTetheringReceiver, filter);
        }
    }

    protected void setExplicitlyUnselected(NetworkAgentInfo nai) {
        if (nai != null) {
            nai.networkMisc.explicitlySelected = IS_CHINA;
            nai.networkMisc.acceptUnvalidated = IS_CHINA;
            if (nai.networkInfo != null && ConnectivityManager.getNetworkTypeName(WIFI_PULS_CSP_DISENABLED).equals(nai.networkInfo.getTypeName())) {
                log("setExplicitlyUnselected, WiFi+ switch from WiFi to Cellular, enableDefaultTypeAPN explicitly.");
                enableDefaultTypeAPN(true);
            }
        }
    }

    protected void updateNetworkConcurrently(NetworkAgentInfo networkAgent, NetworkInfo newInfo) {
        State state = newInfo.getState();
        INetworkManagementService netd = connectivityServiceUtils.getNetd(this);
        synchronized (networkAgent) {
            NetworkInfo oldInfo = networkAgent.networkInfo;
            networkAgent.networkInfo = newInfo;
        }
        if (oldInfo != null && oldInfo.getState() == state) {
            log("updateNetworkConcurrently, ignoring duplicate network state non-change");
        } else if (netd == null) {
            loge("updateNetworkConcurrently, invalid member, netd = null");
        } else {
            networkAgent.setCurrentScore(WIFI_PULS_CSP_ENABLED);
            try {
                String str;
                int i = networkAgent.network.netId;
                if (networkAgent.networkCapabilities.hasCapability(13)) {
                    str = null;
                } else {
                    str = "SYSTEM";
                }
                netd.createPhysicalNetwork(i, str);
                networkAgent.created = true;
                connectivityServiceUtils.updateLinkProperties(this, networkAgent, null);
                log("updateNetworkConcurrently, nai.networkInfo = " + networkAgent.networkInfo);
                networkAgent.asyncChannel.sendMessage(528391, ACTION_BASTET_FILTER_ADD_LIST, WIFI_PULS_CSP_ENABLED, null);
            } catch (Exception e) {
                loge("updateNetworkConcurrently, Error creating network " + networkAgent.network.netId + ": " + e.getMessage());
            }
        }
    }

    public void triggerRoamingNetworkMonitor(NetworkAgentInfo networkAgent) {
        if (networkAgent != null && networkAgent.networkMonitor != null) {
            log("triggerRoamingNetworkMonitor, nai.networkInfo = " + networkAgent.networkInfo);
            networkAgent.networkMonitor.sendMessage(532581);
        }
    }

    protected boolean reportPortalNetwork(NetworkAgentInfo nai, int result) {
        if (result != SERVICE_TYPE_OTHERS) {
            return IS_CHINA;
        }
        nai.asyncChannel.sendMessage(528391, ACTION_BASTET_FILTER_STOP, WIFI_PULS_CSP_ENABLED, null);
        return true;
    }

    protected boolean ignoreRemovedByWifiPro(NetworkAgentInfo nai) {
        if (nai.networkInfo.getType() == WIFI_PULS_CSP_DISENABLED && WifiProCommonUtils.isWifiProEnable(this.mContext)) {
            return true;
        }
        return IS_CHINA;
    }

    protected void holdWifiNetworkMessenger(NetworkAgentInfo nai) {
        if (nai.networkInfo.getType() == WIFI_PULS_CSP_DISENABLED) {
            this.mWifiNetworkMessenger = nai.messenger;
        }
    }

    private NetworkAgentInfo getWifiNai() {
        NetworkAgentInfo networkAgentInfo = null;
        HashMap<Messenger, NetworkAgentInfo> networkAgentInfos = connectivityServiceUtils.getNetworkAgentInfos(this);
        if (this.mWifiNetworkMessenger == null) {
            log("csagent getWifiNai WIFI NetworkAgent not register Error, return null.");
            return null;
        }
        if (networkAgentInfos != null && networkAgentInfos.containsKey(this.mWifiNetworkMessenger)) {
            networkAgentInfo = (NetworkAgentInfo) networkAgentInfos.get(this.mWifiNetworkMessenger);
        }
        return networkAgentInfo;
    }

    public Network getNetworkForTypeWifi() {
        enforceAccessPermission();
        NetworkAgentInfo nai = getWifiNai();
        if (nai == null) {
            return null;
        }
        return nai.network;
    }

    public NetworkInfo getNetworkInfoForWifi() {
        enforceAccessPermission();
        NetworkAgentInfo nai = getWifiNai();
        if (nai != null) {
            NetworkInfo result = new NetworkInfo(nai.networkInfo);
            result.setType(WIFI_PULS_CSP_DISENABLED);
            return result;
        }
        result = new NetworkInfo(WIFI_PULS_CSP_DISENABLED, WIFI_PULS_CSP_ENABLED, ConnectivityManager.getNetworkTypeName(WIFI_PULS_CSP_DISENABLED), AppHibernateCst.INVALID_PKG);
        result.setDetailedState(DetailedState.DISCONNECTED, null, null);
        return result;
    }

    protected void setVpnSettingValue(boolean enable) {
        log("WiFi_PRO, setVpnSettingValue =" + enable);
        System.putInt(this.mContext.getContentResolver(), "wifipro_network_vpn_state", enable ? WIFI_PULS_CSP_DISENABLED : WIFI_PULS_CSP_ENABLED);
    }

    private boolean isRequestedByPkgName(int pID, String pkgName) {
        List<RunningAppProcessInfo> appProcessList = this.mActivityManager.getRunningAppProcesses();
        if (appProcessList == null || pkgName == null) {
            return IS_CHINA;
        }
        for (RunningAppProcessInfo appProcess : appProcessList) {
            if (appProcess != null && appProcess.pid == pID) {
                String[] pkgNameList = appProcess.pkgList;
                for (int i = WIFI_PULS_CSP_ENABLED; i < pkgNameList.length; i += WIFI_PULS_CSP_DISENABLED) {
                    if (pkgName.equals(pkgNameList[i])) {
                        return true;
                    }
                }
                continue;
            }
        }
        return IS_CHINA;
    }

    public NetworkInfo getActiveNetworkInfo() {
        NetworkInfo networkInfo = super.getActiveNetworkInfo();
        if (networkInfo != null || !isRequestedByPkgName(Binder.getCallingPid(), SYSTEM_MANAGER_PKG_NAME)) {
            return networkInfo;
        }
        Slog.d(TAG, "return the background wifi network info for system manager.");
        return HwServiceFactory.getHwConnectivityManager().getNetworkInfoForWifi();
    }

    protected boolean isNetworkRequestBip(NetworkRequest nr) {
        if (nr == null) {
            loge("network request is null!");
            return IS_CHINA;
        } else if (nr.networkCapabilities.hasCapability(18) || nr.networkCapabilities.hasCapability(19) || nr.networkCapabilities.hasCapability(20) || nr.networkCapabilities.hasCapability(21) || nr.networkCapabilities.hasCapability(22) || nr.networkCapabilities.hasCapability(IM_HOUR_OF_NIGHT) || nr.networkCapabilities.hasCapability(24)) {
            return true;
        } else {
            return IS_CHINA;
        }
    }

    protected boolean checkNetworkSupportBip(NetworkAgentInfo nai, NetworkRequest nri) {
        if (HwModemCapability.isCapabilitySupport(WIFI_PULS_CSP_DISENABLED)) {
            log("MODEM is support BIP!");
            return IS_CHINA;
        } else if (nai == null || nri == null || nai.networkInfo == null) {
            loge("network agent or request is null, just return false!");
            return IS_CHINA;
        } else if (nai.networkInfo.getType() != 0 || !nai.isInternet()) {
            loge("NOT support internet or NOT mobile!");
            return IS_CHINA;
        } else if (isNetworkRequestBip(nri)) {
            String defaultApn = SystemProperties.get("gsm.default.apn");
            String bipApn = SystemProperties.get("gsm.bip.apn");
            if (defaultApn == null || bipApn == null) {
                loge("default apn is null or bip apn is null, default: " + defaultApn + ", bip: " + bipApn);
                return IS_CHINA;
            } else if (MemoryConstant.MEM_SCENE_DEFAULT.equals(bipApn)) {
                log("bip use default network, return true");
                return true;
            } else {
                String[] buffers = bipApn.split(",");
                if (buffers.length <= WIFI_PULS_CSP_DISENABLED || !defaultApn.equalsIgnoreCase(buffers[WIFI_PULS_CSP_DISENABLED].trim())) {
                    log("network do NOT support bip, default: " + defaultApn + ", bip: " + bipApn);
                    return IS_CHINA;
                }
                log("default apn support bip, default: " + defaultApn + ", bip: " + buffers[WIFI_PULS_CSP_DISENABLED].trim());
                return true;
            }
        } else {
            loge("network request is NOT bip!");
            return IS_CHINA;
        }
    }

    public void setLteMobileDataEnabled(boolean enable) {
        log("[enter]setLteMobileDataEnabled " + enable);
        enforceChangePermission();
        HwTelephonyManagerInner.getDefault().setLteServiceAbility(enable ? WIFI_PULS_CSP_DISENABLED : WIFI_PULS_CSP_ENABLED);
        sendLteDataStateBroadcast(mLteMobileDataState);
    }

    public int checkLteConnectState() {
        enforceAccessPermission();
        return mLteMobileDataState;
    }

    protected void handleLteMobileDataStateChange(NetworkInfo info) {
        if (info == null) {
            Slog.e(TAG, "NetworkInfo got null!");
            return;
        }
        Slog.d(TAG, "[enter]handleLteMobileDataStateChange type=" + info.getType() + ",subType=" + info.getSubtype());
        if (info.getType() == 0) {
            int lteState;
            if (13 == info.getSubtype()) {
                lteState = mapDataStateToLteDataState(info.getState());
            } else {
                lteState = ACTION_BASTET_FILTER_STOP;
            }
            setLteMobileDataState(lteState);
        }
    }

    private int mapDataStateToLteDataState(State state) {
        log("[enter]mapDataStateToLteDataState state=" + state);
        switch (-getandroid-net-NetworkInfo$StateSwitchesValues()[state.ordinal()]) {
            case WIFI_PULS_CSP_DISENABLED /*1*/:
                return WIFI_PULS_CSP_DISENABLED;
            case SERVICE_TYPE_OTHERS /*2*/:
                return WIFI_PULS_CSP_ENABLED;
            case ACTION_BASTET_FILTER_STOP /*3*/:
                return ACTION_BASTET_FILTER_STOP;
            case ACTION_BASTET_FILTER_ADD_LIST /*4*/:
                return SERVICE_TYPE_OTHERS;
            default:
                Slog.d(TAG, "mapDataStateToLteDataState ignore state = " + state);
                return ACTION_BASTET_FILTER_STOP;
        }
    }

    private synchronized void setLteMobileDataState(int state) {
        Slog.d(TAG, "[enter]setLteMobileDataState state=" + state);
        mLteMobileDataState = state;
        sendLteDataStateBroadcast(mLteMobileDataState);
    }

    private void sendLteDataStateBroadcast(int state) {
        Intent intent = new Intent("android.net.wifi.LTEDATA_COMPLETED_ACTION");
        intent.putExtra("lte_mobile_data_status", state);
        Slog.d(TAG, "Send sticky broadcast from ConnectivityService. intent=" + intent);
        sendStickyBroadcast(intent);
    }

    public long getLteTotalRxBytes() {
        Slog.d(TAG, "[enter]getLteTotalRxBytes");
        enforceAccessPermission();
        long lteRxBytes = 0;
        try {
            NetworkStatsHistory.Entry entry = getLteStatsEntry(SERVICE_TYPE_OTHERS);
            if (entry != null) {
                lteRxBytes = entry.rxBytes;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "lteTotalRxBytes=" + lteRxBytes);
        return lteRxBytes;
    }

    public long getLteTotalTxBytes() {
        Slog.d(TAG, "[enter]getLteTotalTxBytes");
        enforceAccessPermission();
        long lteTxBytes = 0;
        try {
            NetworkStatsHistory.Entry entry = getLteStatsEntry(8);
            if (entry != null) {
                lteTxBytes = entry.txBytes;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "LteTotalTxBytes=" + lteTxBytes);
        return lteTxBytes;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private NetworkStatsHistory.Entry getLteStatsEntry(int fields) {
        Log.d(TAG, "[enter]getLteStatsEntry fields=" + fields);
        NetworkStatsHistory.Entry entry = null;
        try {
            NetworkTemplate mobile4gTemplate = NetworkTemplate.buildTemplateMobile4g(((TelephonyManager) this.mContext.getSystemService("phone")).getSubscriberId());
            getStatsService().forceUpdate();
            INetworkStatsSession session = getStatsService().openSession();
            if (session != null) {
                NetworkStatsHistory networkStatsHistory = session.getHistoryForNetwork(mobile4gTemplate, fields);
                if (networkStatsHistory != null) {
                    entry = networkStatsHistory.getValues(Long.MIN_VALUE, Long.MAX_VALUE, null);
                }
            }
            TrafficStats.closeQuietly(session);
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable th) {
            TrafficStats.closeQuietly(null);
        }
        return entry;
    }

    private static synchronized INetworkStatsService getStatsService() {
        INetworkStatsService iNetworkStatsService;
        synchronized (HwConnectivityService.class) {
            Log.d(TAG, "[enter]getStatsService");
            if (mStatsService == null) {
                mStatsService = INetworkStatsService.Stub.asInterface(ServiceManager.getService("netstats"));
            }
            iNetworkStatsService = mStatsService;
        }
        return iNetworkStatsService;
    }

    private int[] getHwUidsWithPolicy(int policy) {
        int[] uids = new int[WIFI_PULS_CSP_ENABLED];
        IBinder networkPolicyManager = ServiceManager.getService("netpolicy");
        if (networkPolicyManager == null) {
            Log.e(TAG, "getService NETWORK_POLICY_SERVICE failed!");
            return uids;
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        if (data == null) {
            return uids;
        }
        data.writeInt(policy);
        try {
            networkPolicyManager.transact(HSM_NETWORK_POLICY_SERVICE_TRANSACTION_CODE, data, reply, WIFI_PULS_CSP_ENABLED);
            reply.readInt();
            int num = reply.readInt();
            Log.d(TAG, "getHwUidsWithPolicy uid num: " + num);
            if (num <= 0) {
                data.recycle();
                if (reply != null) {
                    reply.recycle();
                }
                return uids;
            }
            for (int i = WIFI_PULS_CSP_ENABLED; i < num; i += WIFI_PULS_CSP_DISENABLED) {
                uids = ArrayUtils.appendInt(uids, reply.readInt());
            }
            data.recycle();
            if (reply != null) {
                reply.recycle();
            }
            return uids;
        } catch (Exception e) {
            e.printStackTrace();
            data.recycle();
            if (reply != null) {
                reply.recycle();
            }
            return uids;
        } catch (Throwable th) {
            data.recycle();
            if (reply != null) {
                reply.recycle();
            }
            return uids;
        }
    }

    private void setNetworkRestrictByUid(int uid, boolean isRestrict, boolean isMobileNetwork) {
        IBinder networkManager = ServiceManager.getService("network_management");
        if (networkManager == null) {
            Log.e(TAG, "getService NETWORKMANAGEMENT_SERVICE failed!");
            return;
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        String cmd = "bandwidth";
        String[] args = new String[IM_HOUR_OF_MORNING];
        args[WIFI_PULS_CSP_ENABLED] = "firewall";
        args[WIFI_PULS_CSP_DISENABLED] = isRestrict ? "block" : "allow";
        args[SERVICE_TYPE_OTHERS] = isMobileNetwork ? "mobile" : MODULE_WIFI;
        args[ACTION_BASTET_FILTER_STOP] = String.valueOf(uid);
        args[ACTION_BASTET_FILTER_ADD_LIST] = String.valueOf(WIFI_PULS_CSP_ENABLED);
        try {
            data.writeString(cmd);
            data.writeArray(args);
            networkManager.transact(HSM_NETWORKMANAGER_SERVICE_TRANSACTION_CODE, data, reply, WIFI_PULS_CSP_DISENABLED);
            if (data != null) {
                data.recycle();
            }
            if (reply != null) {
                reply.recycle();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (data != null) {
                data.recycle();
            }
            if (reply != null) {
                reply.recycle();
            }
        } catch (Throwable th) {
            if (data != null) {
                data.recycle();
            }
            if (reply != null) {
                reply.recycle();
            }
        }
    }

    private void startFilter() {
        Log.d(TAG, "begin startFilter m_filterIsStarted:" + this.m_filterIsStarted);
        synchronized (this.mFilterUidlistLock) {
            if (this.m_filterIsStarted) {
                return;
            }
            int i;
            int[] restrictUids = getHwUidsWithPolicy(WIFI_PULS_CSP_DISENABLED);
            for (i = WIFI_PULS_CSP_ENABLED; i < restrictUids.length; i += WIFI_PULS_CSP_DISENABLED) {
                this.m_filterUidSet.remove(Integer.valueOf(restrictUids[i]));
            }
            String[] str = getAllActPkgInWhiteList().split("\t");
            for (i = WIFI_PULS_CSP_ENABLED; i < str.length; i += WIFI_PULS_CSP_DISENABLED) {
                int uid = getAppUidByName(str[i]);
                Log.d(TAG, "remove white list uid: " + uid);
                this.m_filterUidSet.remove(Integer.valueOf(uid));
            }
            this.m_filterUidSet.remove(Integer.valueOf(Process.myUid()));
            for (Integer intValue : this.m_filterUidSet) {
                setNetworkRestrictByUid(intValue.intValue(), true, true);
            }
            this.m_filterIsStarted = true;
        }
    }

    private void stopFilter() {
        Log.d(TAG, "stopFilter m_filterIsStarted:" + this.m_filterIsStarted);
        synchronized (this.mFilterUidlistLock) {
            if (this.m_filterIsStarted) {
                for (Integer intValue : this.m_filterUidSet) {
                    setNetworkRestrictByUid(intValue.intValue(), IS_CHINA, true);
                }
                this.m_filterIsStarted = IS_CHINA;
                return;
            }
        }
    }

    private int getAppUidByName(String name) {
        try {
            return this.mContext.getPackageManager().getApplicationInfo(name, WIFI_PULS_CSP_DISENABLED).uid;
        } catch (NameNotFoundException e) {
            Log.e(TAG, "getApplicationInfo failed");
            return INVALID_PID;
        }
    }

    private int getHourOfDay() {
        return Calendar.getInstance().get(11);
    }

    private void checkBastetFilter() {
        if ("true".equals(SystemProperties.get("bastet.service.enable", "false")) && setBastetFilterInfo(WIFI_PULS_CSP_DISENABLED, INVALID_PID) == -24) {
            Log.d(TAG, "Bastet filter feature is supported");
            mBastetFilterEnable = true;
        }
    }

    private void getBastetService() {
        synchronized (this.mBastetFilterLock) {
            if (this.mBastetService == null) {
                this.mBastetService = ServiceManager.getService(BASTET_SERVICE);
                if (this.mBastetService == null) {
                    Log.e(TAG, "Failed to get bastet service!");
                    return;
                }
                try {
                    this.mBastetService.linkToDeath(this.mBastetDeathRecipient, WIFI_PULS_CSP_ENABLED);
                    this.mIBastetManager = IBastetManager.Stub.asInterface(this.mBastetService);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private int setBastetFilterInfo(int action, int pid) {
        int ret = -6;
        if (action == WIFI_PULS_CSP_DISENABLED || mBastetFilterEnable) {
            try {
                getBastetService();
                synchronized (this.mBastetFilterLock) {
                    if (this.mIBastetManager != null) {
                        ret = this.mIBastetManager.setFilterInfo(action, pid);
                    }
                }
            } catch (RemoteException e) {
                Log.e(TAG, "set bastet filter information failed");
                e.printStackTrace();
            }
            return ret;
        }
        Log.d(TAG, "[tiger]setBastetFilterInfo action " + action + "pid " + pid);
        if (SERVICE_TYPE_OTHERS == action) {
            startFilter();
        } else if (ACTION_BASTET_FILTER_STOP == action) {
            stopFilter();
        } else {
            Log.d(TAG, "setBastetFilterInfo other action:" + action);
        }
        return -6;
    }

    private int procCtrlSockets(int pid, int cmd, int param) {
        int action = WIFI_PULS_CSP_ENABLED;
        Log.d(TAG, "pid=" + pid + ", cmd=" + cmd + ", param=" + param);
        if (cmd == this.CANCEL_SPECIAL_PID) {
            action = IM_HOUR_OF_MORNING;
        } else if (cmd == this.KEEP_SOCKET || cmd == this.SET_SPECIAL_PID) {
            action = ACTION_BASTET_FILTER_ADD_LIST;
            Log.d(TAG, "pid = " + pid + "socket = " + param);
            this.mFilterKeepPid = pid;
            this.mFilterSpecialSocket = param;
        } else if (cmd == this.SET_SAVING) {
            if (param == 0) {
                action = ACTION_BASTET_FILTER_STOP;
            } else {
                action = SERVICE_TYPE_OTHERS;
            }
        } else if (cmd == this.PUSH_AVAILABLE) {
            this.mFilterSpecialSocket = param;
        } else if (cmd == this.GET_KEEP_SOCKET_STATS) {
            int ret;
            Log.d(TAG, "procCtrlSockets cmd is GET_KEEP_SOCKET_STATS");
            if (this.mFilterKeepPid == 0 || this.mFilterSpecialSocket == 0) {
                ret = WIFI_PULS_CSP_ENABLED;
            } else {
                ret = WIFI_PULS_CSP_DISENABLED;
            }
            return ret;
        } else {
            Log.e(TAG, "unknown cmd: " + cmd);
            return WIFI_PULS_CSP_ENABLED;
        }
        sendFilterMsg(action, pid);
        return WIFI_PULS_CSP_ENABLED;
    }

    private void handleFilterMsg(int action, int pid) {
        Log.d(TAG, "handleFilterMsg action: " + action + "mFilterMsgFlag: " + this.mFilterMsgFlag);
        synchronized (this.mFilterDelayLock) {
            if (this.mFilterMsgFlag != action) {
                this.mFilterMsgFlag = WIFI_PULS_CSP_ENABLED;
                return;
            }
            this.mFilterMsgFlag = WIFI_PULS_CSP_ENABLED;
            setBastetFilterInfo(action, pid);
        }
    }

    private void initFilterThread() {
        this.mThread = new HandlerThread("FilterThread");
        this.mThread.start();
        this.mFilterHandler = new HwFilterHandler(this.mThread.getLooper());
    }

    private void sendFilterMsg(int arg1, int arg2) {
        Message msg = this.mFilterHandler.obtainMessage();
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        Log.d(TAG, "mFilterMsgFlag: " + this.mFilterMsgFlag + ",msg.arg1: " + msg.arg1);
        synchronized (this.mFilterDelayLock) {
            if (msg.arg1 == SERVICE_TYPE_OTHERS) {
                msg.what = WIFI_PULS_CSP_ENABLED;
                if (this.mFilterMsgFlag == 0) {
                    this.mFilterHandler.sendMessageDelayed(msg, 15000);
                } else if (INVALID_PID == this.mFilterMsgFlag) {
                    this.mFilterHandler.sendMessage(msg);
                }
                this.mFilterMsgFlag = msg.arg1;
            } else if (msg.arg1 == ACTION_BASTET_FILTER_STOP) {
                msg.what = WIFI_PULS_CSP_ENABLED;
                if (this.mFilterMsgFlag == 0 || INVALID_PID == this.mFilterMsgFlag) {
                    this.mFilterHandler.sendMessage(msg);
                }
                this.mFilterMsgFlag = msg.arg1;
            } else {
                msg.what = WIFI_PULS_CSP_DISENABLED;
                this.mFilterHandler.sendMessage(msg);
            }
        }
    }

    private void handleBastetServiceDied() {
        if (setBastetFilterInfo(ACTION_BASTET_FILTER_STOP, INVALID_PID) == 0) {
            Log.d(TAG, "Stop bastet filter success");
            this.mBastetDiedRetry = WIFI_PULS_CSP_ENABLED;
            return;
        }
        this.mBastetDiedRetry += WIFI_PULS_CSP_DISENABLED;
        if (this.mBastetDiedRetry < IM_HOUR_OF_MORNING) {
            Message msg = this.mHandler.obtainMessage();
            msg.what = SERVICE_TYPE_OTHERS;
            this.mHandler.sendMessageDelayed(msg, 500);
            return;
        }
        Log.e(TAG, "ERROR!!! CANNOT STOP BASTET FILTER");
        this.mBastetDiedRetry = WIFI_PULS_CSP_ENABLED;
    }

    private int[] getUidList() {
        for (PackageInfo pkgInfo : this.mContext.getPackageManager().getInstalledPackages(12288)) {
            String[] permissions = pkgInfo.requestedPermissions;
            if (permissions != null) {
                int length = permissions.length;
                for (int i = WIFI_PULS_CSP_ENABLED; i < length; i += WIFI_PULS_CSP_DISENABLED) {
                    if (permissions[i].equals("android.permission.INTERNET")) {
                        this.uidSet.add(Integer.valueOf(pkgInfo.applicationInfo.uid));
                        synchronized (this.mFilterUidlistLock) {
                            this.m_filterUidSet.add(Integer.valueOf(pkgInfo.applicationInfo.uid));
                        }
                    }
                }
                continue;
            }
        }
        Integer[] temp = (Integer[]) this.uidSet.toArray(new Integer[WIFI_PULS_CSP_ENABLED]);
        int[] uids = new int[temp.length];
        Log.d(TAG, "configAppUidList uid num: " + temp.length);
        for (int i2 = WIFI_PULS_CSP_ENABLED; i2 < temp.length; i2 += WIFI_PULS_CSP_DISENABLED) {
            uids[i2] = temp[i2].intValue();
        }
        return uids;
    }

    private int configAppUidList() {
        int ret = -6;
        int[] uids = getUidList();
        if (!mBastetFilterEnable) {
            return -6;
        }
        try {
            getBastetService();
            synchronized (this.mBastetFilterLock) {
                if (this.mIBastetManager != null) {
                    ret = this.mIBastetManager.configAppUidList(uids);
                }
            }
        } catch (RemoteException e) {
            Log.e(TAG, "config AppUidList information failed");
            e.printStackTrace();
        }
        return ret;
    }

    protected void registerMapconIntentReceiver(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(MAPCON_START_INTENT);
        filter.addAction(CONNECTIVITY_CHANGE_ACTION);
        filter.addAction(ACTION_MAPCON_SERVICE_START);
        filter.addAction(ACTION_MAPCON_SERVICE_FAILED);
        context.registerReceiver(this.mMapconIntentReceiver, filter);
    }

    protected boolean ifNeedToStartLteMmsTimer(NetworkRequest request) {
        if (this.mMapconService == null) {
            return IS_CHINA;
        }
        int wifiMmsSwitchOn = INVALID_PID;
        try {
            wifiMmsSwitchOn = this.mMapconService.getVoWifiServiceState(WIFI_PULS_CSP_ENABLED, WIFI_PULS_CSP_DISENABLED);
        } catch (RemoteException e) {
            loge("getVoWifiServiceState,err=" + e.toString());
        }
        loge("handleRegisterNetworkRequest,wifiMmsSwitchOn=" + wifiMmsSwitchOn);
        int domain = INVALID_PID;
        try {
            domain = this.mMapconService.getVoWifiServiceDomain(WIFI_PULS_CSP_ENABLED, WIFI_PULS_CSP_ENABLED);
        } catch (RemoteException e2) {
            loge("getVoWifiServiceDomain,err=" + e2.toString());
        }
        loge(" before LteMmsTimer start wifiMmsSwitchOn=" + wifiMmsSwitchOn);
        if (!request.networkCapabilities.hasCapability(WIFI_PULS_CSP_ENABLED) || WIFI_PULS_CSP_DISENABLED != wifiMmsSwitchOn || SERVICE_TYPE_OTHERS != domain || !this.isWaitWifiMms) {
            return IS_CHINA;
        }
        if (this.isWifiMmsAlready) {
            loge("WifiMmsAlready,dont need to LteMmstimer");
            this.isWifiMmsAlready = IS_CHINA;
        } else {
            this.mLteMmsNetworkRequest = request;
            this.mLteMmsTimer.stop();
            this.mLteMmsTimer.start();
        }
        return true;
    }

    protected NetworkCapabilities changeWifiMmsNetworkCapabilities(NetworkCapabilities networkCapabilities) {
        if (this.mMapconService == null) {
            return networkCapabilities;
        }
        Boolean isWifiMmsUtOn = Boolean.valueOf(SystemProperties.getBoolean("ro.config.hw_vowifi_mmsut", IS_CHINA));
        try {
            int wifiMmsSwitchOn = this.mMapconService.getVoWifiServiceState(WIFI_PULS_CSP_ENABLED, WIFI_PULS_CSP_DISENABLED);
            log("changeWifiMmsNetworkCapabilities,wifiMmsSwitchOn=" + wifiMmsSwitchOn);
            if (!networkCapabilities.hasCapability(WIFI_PULS_CSP_ENABLED) || !isWifiMmsUtOn.booleanValue() || WIFI_PULS_CSP_DISENABLED != wifiMmsSwitchOn) {
                return networkCapabilities;
            }
            try {
                int domain = this.mMapconService.getVoWifiServiceDomain(WIFI_PULS_CSP_ENABLED, WIFI_PULS_CSP_ENABLED);
                log("changeNetworkCapabilities,domain=" + domain);
                if (SERVICE_TYPE_OTHERS == domain) {
                    this.isWaitWifiMms = true;
                    this.isWifiMmsAlready = IS_CHINA;
                    try {
                        this.mMapconService.setupTunnelOverWifi(WIFI_PULS_CSP_ENABLED, WIFI_PULS_CSP_ENABLED, null, null);
                    } catch (RemoteException e) {
                        loge("changeWifiMmsNetworkCapabilities,setupTunnelOverWifi,err=" + e.toString());
                        return networkCapabilities;
                    }
                } else if (WIFI_PULS_CSP_DISENABLED == domain) {
                    this.mWifiMmsTimer.stop();
                    this.mWifiMmsTimer.start();
                } else if (domain == 0) {
                    String networkSpecifier = networkCapabilities.getNetworkSpecifier();
                    networkCapabilities.setNetworkSpecifier(null);
                    networkCapabilities.addTransportType(WIFI_PULS_CSP_DISENABLED);
                    networkCapabilities.removeTransportType(WIFI_PULS_CSP_ENABLED);
                    networkCapabilities.setNetworkSpecifier(networkSpecifier);
                }
                return networkCapabilities;
            } catch (RemoteException e2) {
                loge("changeWifiMmsNetworkCapabilities,getVoWifiServiceDomain,err=" + e2.toString());
                return networkCapabilities;
            }
        } catch (RemoteException e22) {
            loge("changeWifiMmsNetworkCapabilities,getVoWifiServiceState,err=" + e22.toString());
            return networkCapabilities;
        }
    }

    protected void wifiMmsRelease(NetworkRequest networkRequest) {
        if (networkRequest.networkCapabilities.hasCapability(WIFI_PULS_CSP_ENABLED) && this.mMapconService != null && Boolean.valueOf(SystemProperties.getBoolean("ro.config.hw_vowifi_mmsut", IS_CHINA)).booleanValue()) {
            try {
                int wifiMmsSwitchOn = this.mMapconService.getVoWifiServiceState(WIFI_PULS_CSP_ENABLED, WIFI_PULS_CSP_DISENABLED);
                log("wifiMmsRelease,wifiMmsSwitchOn=" + wifiMmsSwitchOn);
                if (wifiMmsSwitchOn == WIFI_PULS_CSP_DISENABLED) {
                    try {
                        int domain = this.mMapconService.getVoWifiServiceDomain(WIFI_PULS_CSP_ENABLED, WIFI_PULS_CSP_ENABLED);
                        log("wifiMmsRelease,domain=" + domain);
                        if (SERVICE_TYPE_OTHERS == domain || WIFI_PULS_CSP_DISENABLED == domain) {
                            try {
                                this.mMapconService.teardownTunnelOverWifi(WIFI_PULS_CSP_ENABLED, WIFI_PULS_CSP_ENABLED, null, null);
                            } catch (RemoteException e) {
                                loge("wifiMmsRelease,setupTunnelOverWifi,err=" + e.toString());
                            }
                        }
                    } catch (RemoteException e2) {
                        loge("wifiMmsRelease,getVoWifiServiceDomain,err=" + e2.toString());
                    }
                }
            } catch (RemoteException e22) {
                loge("wifiMmsRelease,getVoWifiServiceState,err=" + e22.toString());
            }
        }
    }

    private boolean isSetupWizardCompleted() {
        if (WIFI_PULS_CSP_DISENABLED == Secure.getInt(this.mContext.getContentResolver(), "user_setup_complete", WIFI_PULS_CSP_ENABLED)) {
            return WIFI_PULS_CSP_DISENABLED == Secure.getInt(this.mContext.getContentResolver(), "device_provisioned", WIFI_PULS_CSP_ENABLED) ? true : IS_CHINA;
        } else {
            return IS_CHINA;
        }
    }
}
