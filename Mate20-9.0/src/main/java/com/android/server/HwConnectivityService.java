package com.android.server;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.net.CaptivePortal;
import android.net.ConnectivityManager;
import android.net.ICaptivePortal;
import android.net.INetworkPolicyManager;
import android.net.INetworkStatsService;
import android.net.INetworkStatsSession;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.NetworkSpecifier;
import android.net.NetworkStatsHistory;
import android.net.NetworkTemplate;
import android.net.NetworkUtils;
import android.net.StringNetworkSpecifier;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.ServiceSpecificException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.provider.SettingsEx;
import android.telephony.CarrierConfigManager;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.HwVSimManager;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.LocalLog;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;
import com.android.internal.telephony.HwModemCapability;
import com.android.internal.telephony.HwPhoneConstants;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.server.AbstractConnectivityService;
import com.android.server.ConnectivityService;
import com.android.server.GcmFixer.HeartbeatReceiver;
import com.android.server.GcmFixer.NetworkStateReceiver;
import com.android.server.am.HwActivityManagerService;
import com.android.server.am.ProcessRecord;
import com.android.server.connectivity.DnsManager;
import com.android.server.connectivity.NetworkAgentInfo;
import com.android.server.connectivity.Vpn;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.android.server.hidata.hicure.HwHiCureManager;
import com.android.server.hidata.mplink.HwMpLinkContentAware;
import com.android.server.hidata.mplink.HwMplinkManager;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.mtm.iaware.brjob.AwareJobSchedulerConstants;
import com.android.server.pm.auth.HwCertification;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.wifipro.WifiProCommonUtils;
import com.hisi.mapcon.IMapconService;
import com.huawei.deliver.info.HwDeliverInfo;
import com.huawei.utils.reflect.EasyInvokeFactory;
import huawei.HwFeatureConfig;
import huawei.android.telephony.wrapper.WrapperFactory;
import huawei.cust.HwCfgFilePolicy;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class HwConnectivityService extends ConnectivityService {
    private static final String ACTION_BT_CONNECTION_CHANGED = "android.bluetooth.pan.profile.action.CONNECTION_STATE_CHANGED";
    public static final String ACTION_LTEDATA_COMPLETED_ACTION = "android.net.wifi.LTEDATA_COMPLETED_ACTION";
    public static final String ACTION_MAPCON_SERVICE_FAILED = "com.hisi.mapcon.servicefailed";
    public static final String ACTION_MAPCON_SERVICE_START = "com.hisi.mapcon.serviceStartResult";
    private static final String ACTION_NOTIFY_WIFI_CONNECTED_CONCURRENTLY = "com.huawei.wifipro.action.ACTION_NOTIFY_WIFI_CONNECTED_CONCURRENTLY";
    private static final String ACTION_OF_ANDROID_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
    private static final String ACTION_SIM_RECORDS_READY = "com.huawei.intent.action.ACTION_SIM_RECORDS_READY";
    private static final int CONNECTIVITY_SERVICE_NEED_SET_USER_DATA = 1100;
    private static final String COUNTRY_CODE_CN = "460";
    public static final int DEFAULT_PHONE_ID = 0;
    private static final String DEFAULT_PRIVATE_DNS_CONFIG = "1,4,4,6,8,8,10,60";
    private static final String DEFAULT_SERVER = "connectivitycheck.gstatic.com";
    private static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.82 Safari/537.36";
    private static final int DEVICE_NOT_PROVISIONED = 0;
    private static final int DEVICE_PROVISIONED = 1;
    public static final String DISABEL_DATA_SERVICE_ACTION = "android.net.conn.DISABEL_DATA_SERVICE_ACTION";
    private static final String DISABLE_PORTAL_CHECK = "disable_portal_check";
    private static final int DNS_SUCCESS = 0;
    private static String ENABLE_NOT_REMIND_FUNCTION = "enable_not_remind_function";
    public static final String EXTRA_IS_LTE_MOBILE_DATA_STATUS = "lte_mobile_data_status";
    public static final String FLAG_SETUP_WIZARD = "flag_setup_wizard";
    private static final String HW_CONNECTIVITY_ACTION = "huawei.net.conn.HW_CONNECTIVITY_CHANGE";
    private static final boolean HW_SIM_ACTIVATION = SystemProperties.getBoolean("ro.config.hw_sim_activation", false);
    private static final boolean HiCureEnabled = SystemProperties.getBoolean("ro.config.hw_hicure.enabled", true);
    private static final boolean IS_CHINA = "CN".equalsIgnoreCase(SystemProperties.get(WifiProCommonUtils.KEY_PROP_LOCALE, ""));
    private static final int LTE_SERVICE_OFF = 0;
    private static final int LTE_SERVICE_ON = 1;
    public static final int LTE_STATE_CONNECTED = 1;
    public static final int LTE_STATE_CONNECTTING = 0;
    public static final int LTE_STATE_DISCONNECTED = 3;
    public static final int LTE_STATE_DISCONNECTTING = 2;
    public static final String MAPCON_START_INTENT = "com.hisi.mmsut.started";
    private static final String MDM_VPN_PERMISSION = "com.huawei.permission.sec.MDM_VPN";
    private static final String MODULE_POWERSAVING = "powersaving";
    private static final String MODULE_WIFI = "wifi";
    private static final int PREFER_NETWORK_TIMEOUT_INTERVAL = 10000;
    private static final int PRIVATE_DNS_DELAY_BAD = 500;
    private static final int PRIVATE_DNS_DELAY_NORMAL = 150;
    private static final int PRIVATE_DNS_DELAY_VERY_BAD = 1000;
    private static final String PROP_PRIVATE_DNS_CONFIG = "hw.wifi.private_dns_config";
    public static final int SERVICE_STATE_MMS = 1;
    public static final int SERVICE_STATE_OFF = 0;
    public static final int SERVICE_STATE_ON = 1;
    public static final int SERVICE_TYPE_MMS = 0;
    public static final int SERVICE_TYPE_OTHERS = 2;
    private static final String SYSTEM_MANAGER_PKG_NAME = "com.huawei.systemmanager";
    private static final String TAG = "HwConnectivityService";
    private static final int USER_SETUP_COMPLETE = 1;
    private static final int USER_SETUP_NOT_COMPLETE = 0;
    private static String VALUE_DISABLE_NOT_REMIND_FUNCTION = "false";
    /* access modifiers changed from: private */
    public static String VALUE_ENABLE_NOT_REMIND_FUNCTION = "true";
    private static int VALUE_NOT_SHOW_PDP = 0;
    private static int VALUE_SHOW_PDP = 1;
    private static final String VALUE_SIM_CHANGE_ALERT_DATA_CONNECT = "0";
    private static final String VERIZON_ICCID_PREFIX = "891480";
    private static String WHETHER_SHOW_PDP_WARNING = "whether_show_pdp_warning";
    private static final String WIFI_AP_MANUAL_CONNECT = "wifi_ap_manual_connect";
    public static final int WIFI_PULS_CSP_DISENABLED = 1;
    public static final int WIFI_PULS_CSP_ENABLED = 0;
    /* access modifiers changed from: private */
    public static ConnectivityServiceUtils connectivityServiceUtils = EasyInvokeFactory.getInvokeUtils(ConnectivityServiceUtils.class);
    private static final String descriptor = "android.net.IConnectivityManager";
    protected static final boolean isAlwaysAllowMMS = SystemProperties.getBoolean("ro.config.hw_always_allow_mms", false);
    private static final boolean isMapconOn = SystemProperties.getBoolean("ro.vendor.config.hw_vowifi", false);
    private static final boolean isVerizon;
    private static final boolean isWifiMmsUtOn = SystemProperties.getBoolean("ro.config.hw_vowifi_mmsut", false);
    private static int mLteMobileDataState = 3;
    private static INetworkStatsService mStatsService;
    private int curMmsDataSub = -1;
    private int curPrefDataSubscription = -1;
    /* access modifiers changed from: private */
    public boolean isAlreadyPop = false;
    /* access modifiers changed from: private */
    public boolean isConnected = false;
    private ActivityManager mActivityManager;
    private HashMap<Integer, BypassPrivateDnsInfo> mBypassPrivateDnsNetwork = new HashMap<>();
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public AlertDialog mDataServiceToPdpDialog = null;
    protected int mDefaultDataSub = 0;
    /* access modifiers changed from: private */
    public DomainPreferHandler mDomainPreferHandler = null;
    private NetworkStateReceiver mGcmFixIntentReceiver = new NetworkStateReceiver();
    /* access modifiers changed from: private */
    public Handler mHandler;
    private HeartbeatReceiver mHeartbeatReceiver = new HeartbeatReceiver();
    private HwHiCureManager mHiCureManager = null;
    /* access modifiers changed from: private */
    public BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            HwConnectivityService.log("mIntentReceiver begin");
            String action = intent.getAction();
            if (HwConnectivityService.ACTION_OF_ANDROID_BOOT_COMPLETED.equals(action)) {
                HwConnectivityService.log("receive Intent.ACTION_BOOT_COMPLETED!");
                boolean unused = HwConnectivityService.this.sendWifiBroadcastAfterBootCompleted = true;
            } else if (HwConnectivityService.ACTION_BT_CONNECTION_CHANGED.equals(action)) {
                HwConnectivityService.log("receive ACTION_BT_CONNECTION_CHANGED");
                if (intent.getIntExtra("android.bluetooth.profile.extra.STATE", -1) == 0) {
                    boolean unused2 = HwConnectivityService.this.mIsBlueThConnected = false;
                }
            } else {
                HwConnectivityService.this.mRegisteredPushPkg.updateStatus(intent);
            }
        }
    };
    /* access modifiers changed from: private */
    public boolean mIsBlueThConnected = false;
    /* access modifiers changed from: private */
    public boolean mIsSimReady = false;
    /* access modifiers changed from: private */
    public boolean mIsSimStateChanged = false;
    private boolean mIsTopAppHsbb = false;
    private boolean mIsTopAppSkytone = false;
    private boolean mIsWifiConnected = false;
    private final Object mLock = new Object();
    protected BroadcastReceiver mMapconIntentReceiver = new BroadcastReceiver() {
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
                        HwConnectivityService.this.mMapconService = IMapconService.Stub.asInterface(service);
                    }

                    public void onServiceDisconnected(ComponentName className) {
                        HwConnectivityService.this.mMapconService = null;
                    }
                };
                HwConnectivityService.this.mContext.bindServiceAsUser(new Intent().setClassName("com.hisi.mapcon", "com.hisi.mapcon.MapconService"), mConnection, 1, UserHandle.OWNER);
            } else if (HwConnectivityService.ACTION_MAPCON_SERVICE_FAILED.equals(action) && mapconIntent.getIntExtra("serviceType", 2) == 0) {
                int requestId = mapconIntent.getIntExtra("request-id", -1);
                HwConnectivityService.loge("Recive ACTION_MAPCON_SERVICE_FAILED, requestId = " + requestId);
                if (requestId > 0) {
                    HwConnectivityService.this.mDomainPreferHandler.sendMessageAtFrontOfQueue(HwConnectivityService.this.mDomainPreferHandler.obtainMessage(1, requestId, 0));
                }
            }
        }
    };
    protected IMapconService mMapconService;
    PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        public void onCallStateChanged(int state, String incomingNumber) {
            HwConnectivityService.this.updateCallState(state);
        }

        public void onServiceStateChanged(ServiceState state) {
            boolean isConnect;
            if (state != null && !TextUtils.isEmpty(HwConnectivityService.this.mSimChangeAlertDataConnect) && !HwConnectivityService.this.isAlreadyPop) {
                Log.d(HwConnectivityService.TAG, "onServiceStateChanged:" + state);
                switch (state.getVoiceRegState()) {
                    case 1:
                    case 2:
                        isConnect = state.getDataRegState() == 0;
                        break;
                    case 3:
                        isConnect = false;
                        break;
                    default:
                        isConnect = true;
                        break;
                }
                if (state.getRoaming()) {
                    HwTelephonyManagerInner.getDefault().setDataRoamingEnabledWithoutPromp(false);
                    boolean unused = HwConnectivityService.this.mShowWarningRoamingToPdp = true;
                }
                if (isConnect && HwConnectivityService.this.isSetupWizardCompleted() && HwConnectivityService.this.mIsSimReady) {
                    HwConnectivityService.this.mHandler.sendEmptyMessage(0);
                    boolean unused2 = HwConnectivityService.this.isAlreadyPop = true;
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public RegisteredPushPkg mRegisteredPushPkg = new RegisteredPushPkg();
    /* access modifiers changed from: private */
    public boolean mRemindService = SystemProperties.getBoolean("ro.config.DataPopFirstBoot", false);
    private String mServer;
    private boolean mShowDlgEndCall = false;
    private boolean mShowDlgTurnOfDC = true;
    /* access modifiers changed from: private */
    public boolean mShowWarningRoamingToPdp = false;
    /* access modifiers changed from: private */
    public String mSimChangeAlertDataConnect = null;
    private BroadcastReceiver mSimStateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.SIM_STATE_CHANGED".equals(action)) {
                Log.d(HwConnectivityService.TAG, "receive ACTION_SIM_STATE_CHANGED");
                boolean unused = HwConnectivityService.this.mIsSimStateChanged = true;
                if (!TextUtils.isEmpty(HwConnectivityService.this.mSimChangeAlertDataConnect)) {
                    HwConnectivityService.this.processWhenSimStateChange(intent);
                }
                int slotId = intent.getIntExtra("slot", -1000);
                if ("LOADED".equals((String) intent.getExtra("ss", "UNKNOWN")) && HwConnectivityService.this.mDefaultDataSub == slotId && SubscriptionManager.isValidSubscriptionId(slotId)) {
                    HwConnectivityService.this.updateMobileDataAlwaysOnCust(slotId);
                }
            } else if ("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED".equals(action)) {
                int curDataSub = SubscriptionManager.getDefaultDataSubscriptionId();
                if (HwConnectivityService.this.mDefaultDataSub != curDataSub && SubscriptionManager.isValidSubscriptionId(curDataSub)) {
                    HwConnectivityService.this.mDefaultDataSub = curDataSub;
                    HwConnectivityService.this.updateMobileDataAlwaysOnCust(curDataSub);
                }
            }
        }
    };
    private BroadcastReceiver mTetheringReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && "android.hardware.usb.action.USB_STATE".equals(action)) {
                boolean usbConnected = intent.getBooleanExtra("connected", false);
                boolean rndisEnabled = intent.getBooleanExtra("rndis", false);
                int is_usb_tethering_on = Settings.Secure.getInt(HwConnectivityService.this.mContext.getContentResolver(), "usb_tethering_on", 0);
                Log.d(HwConnectivityService.TAG, "mTetheringReceiver usbConnected = " + usbConnected + ",rndisEnabled = " + rndisEnabled + ", is_usb_tethering_on = " + is_usb_tethering_on);
                if (1 == is_usb_tethering_on && usbConnected && !rndisEnabled) {
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
                            HwConnectivityService.this.setUsbTethering(true, HwConnectivityService.this.mContext.getOpPackageName());
                        }
                    }.start();
                }
            }
        }
    };
    private URL mURL;
    private BroadcastReceiver mVerizonWifiDisconnectReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.net.wifi.STATE_CHANGE".equals(intent.getAction())) {
                NetworkInfo netInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                if (netInfo != null && netInfo.getType() == 1) {
                    if (netInfo.getState() == NetworkInfo.State.CONNECTED) {
                        boolean unused = HwConnectivityService.this.isConnected = true;
                    } else if (netInfo.getState() == NetworkInfo.State.DISCONNECTED && netInfo.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED && HwConnectivityService.this.isConnected) {
                        Toast.makeText(context, 33686255, 1).show();
                        boolean unused2 = HwConnectivityService.this.isConnected = false;
                    }
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public WifiDisconnectManager mWifiDisconnectManager = new WifiDisconnectManager();
    private boolean mWifiProPropertyEnabled = false;
    private int phoneId = -1;
    /* access modifiers changed from: private */
    public boolean sendWifiBroadcastAfterBootCompleted = false;

    /* renamed from: com.android.server.HwConnectivityService$13  reason: invalid class name */
    static /* synthetic */ class AnonymousClass13 {
        static final /* synthetic */ int[] $SwitchMap$android$net$NetworkInfo$State = new int[NetworkInfo.State.values().length];

        static {
            try {
                $SwitchMap$android$net$NetworkInfo$State[NetworkInfo.State.CONNECTING.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$net$NetworkInfo$State[NetworkInfo.State.CONNECTED.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$android$net$NetworkInfo$State[NetworkInfo.State.DISCONNECTING.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$android$net$NetworkInfo$State[NetworkInfo.State.DISCONNECTED.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    private static class BypassPrivateDnsInfo {
        private static final int[] BACKOFF_TIME_INTERVAL = {1, 2, 2, 4, 4, 4, 4, 8};
        private static final String CHR_BROADCAST_PERMISSION = "com.huawei.android.permission.GET_CHR_DATA";
        private static final String INTENT_WIFI_PRIVATE_DNS_STATISTICS = "com.intent.action.wifi_private_dns_statistics";
        private final int PRIVATE_DNS_OPEN = 1;
        private int backoffCnt = 0;
        private int backoffTime = Constant.MILLISEC_TO_HOURS;
        private int badCnt = 6;
        private int badTotalCnt = 8;
        private int failedCnt = 8;
        private String mAssignedServers;
        private int mBadDelayTotalCnt;
        /* access modifiers changed from: private */
        public boolean mBypassPrivateDns;
        private Context mContext;
        private int mDelay1000Cnt;
        private int mDelay150Cnt;
        private int mDelay500Cnt;
        private int mDelayOver1000Cnt;
        private int mDnsDelayOverThresCnt = 0;
        private int mNetworkType;
        private long mPrivateDnsBackoffTime;
        private int mPrivateDnsCnt;
        private long mPrivateDnsCntResetTime;
        private int mPrivateDnsFailCount;
        private int mPrivateDnsResponseTotalTime;
        private int privateDnsOpen = 1;
        private int resetCntTime = 600000;
        private int unacceptCnt = 4;
        private int verybadCnt = 4;

        public BypassPrivateDnsInfo(Context context, int NetworkType, String assignedServers) {
            this.mContext = context;
            this.mNetworkType = NetworkType;
            this.mAssignedServers = assignedServers;
            this.mPrivateDnsCntResetTime = SystemClock.elapsedRealtime();
            this.mBypassPrivateDns = false;
            String[] privateDnsConfig = SystemProperties.get(HwConnectivityService.PROP_PRIVATE_DNS_CONFIG, HwConnectivityService.DEFAULT_PRIVATE_DNS_CONFIG).split(",");
            if (privateDnsConfig.length == 8) {
                this.privateDnsOpen = Integer.parseInt(privateDnsConfig[0]);
                this.unacceptCnt = Integer.parseInt(privateDnsConfig[1]);
                this.verybadCnt = Integer.parseInt(privateDnsConfig[2]);
                this.badCnt = Integer.parseInt(privateDnsConfig[3]);
                this.badTotalCnt = Integer.parseInt(privateDnsConfig[4]);
                this.failedCnt = Integer.parseInt(privateDnsConfig[5]);
                this.resetCntTime = Integer.parseInt(privateDnsConfig[6]) * 60 * 1000;
                this.backoffTime = Integer.parseInt(privateDnsConfig[7]) * 60 * 1000;
            }
        }

        public void reset() {
            HwConnectivityService.log("privateDnsCnt reset");
            this.mPrivateDnsCntResetTime = SystemClock.elapsedRealtime();
            this.mPrivateDnsCnt = 0;
            this.mDelay150Cnt = 0;
            this.mDelay500Cnt = 0;
            this.mDelay1000Cnt = 0;
            this.mDelayOver1000Cnt = 0;
            this.mPrivateDnsResponseTotalTime = 0;
            this.mPrivateDnsFailCount = 0;
            this.mBadDelayTotalCnt = 0;
        }

        public boolean isNeedUpdatePrivateDnsSettings() {
            if (this.mBypassPrivateDns) {
                if (SystemClock.elapsedRealtime() - this.mPrivateDnsBackoffTime >= ((long) (BACKOFF_TIME_INTERVAL[this.backoffCnt <= BACKOFF_TIME_INTERVAL.length - 1 ? this.backoffCnt : BACKOFF_TIME_INTERVAL.length - 1] * this.backoffTime))) {
                    HwConnectivityService.log("isNeedUpdatePrivateDnsSettings private dns backoff timeout");
                    reset();
                    this.backoffCnt++;
                    this.mPrivateDnsBackoffTime = 0;
                    this.mBypassPrivateDns = false;
                    sendIntentPrivateDnsEvent();
                    return true;
                }
            } else if ((this.mDelayOver1000Cnt >= this.unacceptCnt || this.mDelay1000Cnt >= this.verybadCnt || this.mDelay500Cnt >= this.badCnt || this.mBadDelayTotalCnt >= this.badTotalCnt || this.mPrivateDnsFailCount >= this.failedCnt) && this.mNetworkType == 1) {
                HwConnectivityService.log(" isNeedUpdatePrivateDnsSettings mDelay150Cnt : " + this.mDelay150Cnt + " , mDelay500Cnt : " + this.mDelay500Cnt + " , mDelay1000Cnt : " + this.mDelay1000Cnt + " , mDelayOver1000Cnt : " + this.mDelayOver1000Cnt + " , mBadDelayTotalCnt = " + this.mBadDelayTotalCnt);
                this.mPrivateDnsBackoffTime = SystemClock.elapsedRealtime();
                this.mBypassPrivateDns = true;
                this.mDnsDelayOverThresCnt = this.mDnsDelayOverThresCnt + 1;
                sendIntentPrivateDnsEvent();
                return true;
            }
            return false;
        }

        public void updateDelayCount(int returnCode, int latencyMs) {
            if (this.mBypassPrivateDns) {
                HwConnectivityService.log("updateDelayCount mBypassPrivateDns return");
                return;
            }
            if (SystemClock.elapsedRealtime() - this.mPrivateDnsCntResetTime >= ((long) this.resetCntTime)) {
                sendIntentPrivateDnsEvent();
                reset();
            }
            this.mPrivateDnsCnt++;
            if (returnCode == 0) {
                this.mPrivateDnsResponseTotalTime += latencyMs;
                if (latencyMs > 1000) {
                    this.mDelayOver1000Cnt++;
                } else if (latencyMs <= 150) {
                    this.mDelay150Cnt++;
                } else if (latencyMs <= 500) {
                    this.mDelay500Cnt++;
                } else if (latencyMs <= 1000) {
                    this.mDelay1000Cnt++;
                }
                this.mBadDelayTotalCnt = this.mDelay500Cnt + this.mDelay1000Cnt + this.mDelayOver1000Cnt;
            } else {
                this.mPrivateDnsFailCount++;
            }
        }

        public void sendIntentPrivateDnsEvent() {
            if (this.mContext != null && this.mNetworkType == 1) {
                HwConnectivityService.log("sendIntentPrivateDnsEvent ReqCnt : " + this.mPrivateDnsCnt + " , DnsAddr : " + this.mAssignedServers + ", DnsDelay : " + this.mPrivateDnsResponseTotalTime + " , ReqFailCnt : " + this.mPrivateDnsFailCount + ", DnsDelayOverThresCnt : " + this.mDnsDelayOverThresCnt + ", PrivDnsDisableCnt= " + this.backoffCnt);
                Intent intent = new Intent(INTENT_WIFI_PRIVATE_DNS_STATISTICS);
                Bundle extras = new Bundle();
                extras.putInt("ReqCnt", this.mPrivateDnsCnt);
                extras.putInt("DnsDelay", this.mPrivateDnsResponseTotalTime);
                extras.putInt("ReqFailCnt", this.mPrivateDnsFailCount);
                extras.putInt("DnsDelayOverThresCnt", this.mDnsDelayOverThresCnt);
                extras.putInt("PrivDnsDisableCnt", this.backoffCnt);
                extras.putString("DnsAddr", this.mAssignedServers);
                extras.putBoolean("PrivDns", true ^ this.mBypassPrivateDns);
                intent.putExtras(extras);
                this.mContext.sendBroadcast(intent, CHR_BROADCAST_PERMISSION);
            }
        }
    }

    private static class CtrlSocketInfo {
        public int mAllowCtrlSocketLevel;
        public List<String> mPushWhiteListPkg;
        public int mRegisteredCount;
        public List<String> mRegisteredPkg;
        public List<String> mScrOffActPkg;

        CtrlSocketInfo() {
            this.mRegisteredPkg = null;
            this.mScrOffActPkg = null;
            this.mPushWhiteListPkg = null;
            this.mAllowCtrlSocketLevel = 0;
            this.mRegisteredCount = 0;
            this.mRegisteredPkg = new ArrayList();
            this.mScrOffActPkg = new ArrayList();
            this.mPushWhiteListPkg = new ArrayList();
        }
    }

    private class DomainPreferHandler extends Handler {
        private static final int MSG_PREFER_NETWORK_FAIL = 1;
        private static final int MSG_PREFER_NETWORK_SUCCESS = 0;
        private static final int MSG_PREFER_NETWORK_TIMEOUT = 2;

        public DomainPreferHandler(Looper looper) {
            super(looper);
        }

        private String getMsgName(int whatMsg) {
            switch (whatMsg) {
                case 0:
                    return "PREFER_NETWORK_SUCCESS";
                case 1:
                    return "PREFER_NETWORK_FAIL";
                case 2:
                    return "PREFER_NETWORK_TIMEOUT";
                default:
                    return Integer.toString(whatMsg);
            }
        }

        public void handleMessage(Message msg) {
            HwConnectivityService.log("DomainPreferHandler handleMessage msg.what = " + getMsgName(msg.what));
            switch (msg.what) {
                case 0:
                    handlePreferNetworkSuccess(msg);
                    return;
                case 1:
                    handlePreferNetworkFail(msg);
                    return;
                case 2:
                    handlePreferNetworkTimeout(msg);
                    return;
                default:
                    return;
            }
        }

        private void handlePreferNetworkSuccess(Message msg) {
            NetworkRequest req = (NetworkRequest) msg.obj;
            HwConnectivityService.log("handlePreferNetworkSuccess request = " + req);
            if (HwConnectivityService.this.mDomainPreferHandler.hasMessages(2, req)) {
                HwConnectivityService.this.mDomainPreferHandler.removeMessages(2, req);
            }
        }

        private void handlePreferNetworkFail(Message msg) {
            ConnectivityService.NetworkRequestInfo nri = HwConnectivityService.this.findExistingNetworkRequestInfo(msg.arg1);
            if (nri == null || nri.mPreferType == null) {
                HwConnectivityService.log("handlePreferNetworkFail, nri or preferType is null.");
                return;
            }
            NetworkRequest req = nri.request;
            int domainPrefer = nri.mPreferType.value();
            if (HwConnectivityService.this.mDomainPreferHandler.hasMessages(2, req)) {
                HwConnectivityService.this.mDomainPreferHandler.removeMessages(2, req);
                retryNetworkRequestWhenPreferException(req, AbstractConnectivityService.DomainPreferType.fromInt(domainPrefer), "FAIL");
            }
        }

        private void handlePreferNetworkTimeout(Message msg) {
            retryNetworkRequestWhenPreferException((NetworkRequest) msg.obj, AbstractConnectivityService.DomainPreferType.fromInt(msg.arg1), "TIMEOUT");
        }

        private void retryNetworkRequestWhenPreferException(NetworkRequest req, AbstractConnectivityService.DomainPreferType prefer, String reason) {
            HwConnectivityService.log("retryNetworkRequestWhenPreferException req = " + req + " prefer = " + prefer + " reason = " + reason);
            ConnectivityService.NetworkRequestInfo nri = (ConnectivityService.NetworkRequestInfo) HwConnectivityService.this.mNetworkRequests.get(req);
            if (nri != null) {
                if ((prefer == AbstractConnectivityService.DomainPreferType.DOMAIN_PREFER_WIFI || prefer == AbstractConnectivityService.DomainPreferType.DOMAIN_PREFER_CELLULAR || prefer == AbstractConnectivityService.DomainPreferType.DOMAIN_PREFER_VOLTE) && ((NetworkAgentInfo) HwConnectivityService.this.mNetworkForRequestId.get(req.requestId)) == null && prefer != null) {
                    for (ConnectivityService.NetworkFactoryInfo nfi : HwConnectivityService.this.mNetworkFactoryInfos.values()) {
                        nfi.asyncChannel.sendMessage(536577, req);
                    }
                    NetworkCapabilities networkCapabilities = req.networkCapabilities;
                    NetworkSpecifier networkSpecifier = networkCapabilities.getNetworkSpecifier();
                    HwConnectivityService.this.mNetworkRequests.remove(req);
                    LocalLog localLog = HwConnectivityService.this.mNetworkRequestInfoLogs;
                    localLog.log("UPDATE-RELEASE " + nri);
                    if (AbstractConnectivityService.DomainPreferType.DOMAIN_PREFER_WIFI == prefer) {
                        networkCapabilities.setNetworkSpecifier(null);
                        networkCapabilities.addTransportType(0);
                        networkCapabilities.removeTransportType(1);
                        networkCapabilities.setNetworkSpecifier(networkSpecifier);
                    } else if (AbstractConnectivityService.DomainPreferType.DOMAIN_PREFER_CELLULAR == prefer || AbstractConnectivityService.DomainPreferType.DOMAIN_PREFER_VOLTE == prefer) {
                        networkCapabilities.setNetworkSpecifier(null);
                        networkCapabilities.addTransportType(1);
                        networkCapabilities.removeTransportType(0);
                        if (networkSpecifier == null) {
                            networkCapabilities.setNetworkSpecifier(new StringNetworkSpecifier(Integer.toString(SubscriptionManager.getDefaultDataSubscriptionId())));
                        } else {
                            networkCapabilities.setNetworkSpecifier(networkSpecifier);
                        }
                    }
                    HwConnectivityService.this.mNetworkRequests.put(req, nri);
                    LocalLog localLog2 = HwConnectivityService.this.mNetworkRequestInfoLogs;
                    localLog2.log("UPDATE-REGISTER " + nri);
                    HwConnectivityService.this.rematchAllNetworksAndRequests(null, 0);
                    if (HwConnectivityService.this.mNetworkForRequestId.get(req.requestId) == null) {
                        HwConnectivityService.this.sendUpdatedScoreToFactories(req, 0);
                    }
                }
            }
        }
    }

    private class HwConnectivityServiceHandler extends Handler {
        private static final int EVENT_SHOW_ENABLE_PDP_DIALOG = 0;

        private HwConnectivityServiceHandler() {
        }

        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                HwConnectivityService.this.handleShowEnablePdpDialog();
            }
        }
    }

    private class MobileEnabledSettingObserver extends ContentObserver {
        public MobileEnabledSettingObserver(Handler handler) {
            super(handler);
        }

        public void register() {
            HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).getContentResolver().registerContentObserver(Settings.Global.getUriFor("device_provisioned"), true, this);
        }

        public void onChange(boolean selfChange) {
            if (HwConnectivityService.this.mRemindService || HwConnectivityService.this.shouldShowThePdpWarning()) {
                super.onChange(selfChange);
                if (!HwConnectivityService.this.getMobileDataEnabled() && HwConnectivityService.this.mDataServiceToPdpDialog == null) {
                    AlertDialog unused = HwConnectivityService.this.mDataServiceToPdpDialog = HwConnectivityService.this.createWarningToPdp();
                    HwConnectivityService.this.mDataServiceToPdpDialog.show();
                }
            }
        }
    }

    private class RegisteredPushPkg {
        private static final String MSG_ALL_CTRLSOCKET_ALLOWED = "android.ctrlsocket.all.allowed";
        private static final String MSG_SCROFF_CTRLSOCKET_STATS = "android.scroff.ctrlsocket.status";
        private static final String ctrl_socket_version = "v2";
        private int ALLOW_ALL_CTRL_SOCKET_LEVEL = 2;
        private int ALLOW_NO_CTRL_SOCKET_LEVEL = 0;
        private int ALLOW_PART_CTRL_SOCKET_LEVEL = 1;
        private int ALLOW_SPECIAL_CTRL_SOCKET_LEVEL = 3;
        private int MAX_REGISTERED_PKG_NUM = 10;
        private final Uri WHITELIST_URI = Settings.Secure.getUriFor("push_white_apps");
        private CtrlSocketInfo mCtrlSocketInfo = new CtrlSocketInfo();
        private boolean mEnable = SystemProperties.getBoolean("ro.config.hw_power_saving", false);

        RegisteredPushPkg() {
        }

        public void init(Context context) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(HwConnectivityService.ACTION_OF_ANDROID_BOOT_COMPLETED);
            filter.addAction(HwConnectivityService.ACTION_BT_CONNECTION_CHANGED);
            if (this.mEnable) {
                filter.addAction(MSG_SCROFF_CTRLSOCKET_STATS);
                filter.addAction(MSG_ALL_CTRLSOCKET_ALLOWED);
                getCtrlSocketRegisteredPkg();
                getCtrlSocketPushWhiteList();
                context.getContentResolver().registerContentObserver(this.WHITELIST_URI, false, new ContentObserver(new Handler()) {
                    public void onChange(boolean selfChange) {
                        RegisteredPushPkg.this.getCtrlSocketPushWhiteList();
                    }
                });
            }
            context.registerReceiver(HwConnectivityService.this.mIntentReceiver, filter);
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) {
            switch (code) {
                case 1001:
                    String register_pkg = data.readString();
                    Log.d(HwConnectivityService.TAG, "CtrlSocket registerPushSocket pkg = " + register_pkg);
                    registerPushSocket(register_pkg);
                    return true;
                case 1002:
                    String pkgStr = data.readString();
                    Log.d(HwConnectivityService.TAG, "CtrlSocket unregisterPushSocket pkg = " + pkgStr);
                    unregisterPushSocket(pkgStr);
                    return true;
                case 1004:
                    reply.writeString(getActPkgInWhiteList());
                    return true;
                case 1005:
                    reply.writeInt(this.mCtrlSocketInfo.mAllowCtrlSocketLevel);
                    return true;
                case HwArbitrationDEFS.MSG_WIFI_STATE_DISCONNECT /*1006*/:
                    Log.d(HwConnectivityService.TAG, "CtrlSocket getCtrlSocketVersion = v2");
                    reply.writeString(ctrl_socket_version);
                    return true;
                default:
                    return false;
            }
        }

        private void registerPushSocket(String pkgName) {
            if (pkgName != null) {
                boolean isToAdd = false;
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
                    this.mCtrlSocketInfo.mRegisteredCount++;
                    this.mCtrlSocketInfo.mRegisteredPkg.add(pkgName);
                    updateRegisteredPkg();
                }
            }
        }

        private void unregisterPushSocket(String pkgName) {
            if (pkgName != null) {
                int count = 0;
                boolean isMatch = false;
                Iterator<String> it = this.mCtrlSocketInfo.mRegisteredPkg.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    } else if (it.next().equals(pkgName)) {
                        isMatch = true;
                        break;
                    } else {
                        count++;
                    }
                }
                if (isMatch) {
                    CtrlSocketInfo ctrlSocketInfo = this.mCtrlSocketInfo;
                    ctrlSocketInfo.mRegisteredCount--;
                    this.mCtrlSocketInfo.mRegisteredPkg.remove(count);
                    updateRegisteredPkg();
                }
            }
        }

        /* access modifiers changed from: private */
        public void getCtrlSocketPushWhiteList() {
            String wlPkg = Settings.Secure.getString(HwConnectivityService.this.mContext.getContentResolver(), "push_white_apps");
            if (wlPkg != null) {
                String[] str = wlPkg.split(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
                if (str != null && str.length > 0) {
                    this.mCtrlSocketInfo.mPushWhiteListPkg.clear();
                    for (String add : str) {
                        this.mCtrlSocketInfo.mPushWhiteListPkg.add(add);
                        Log.d(HwConnectivityService.TAG, "CtrlSocket PushWhiteList[" + i + "] = " + str[i]);
                    }
                }
            }
        }

        private void getCtrlSocketRegisteredPkg() {
            String registeredPkg = Settings.Secure.getString(HwConnectivityService.this.mContext.getContentResolver(), "registered_pkgs");
            if (registeredPkg != null) {
                String[] str = registeredPkg.split(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
                if (str != null && str.length > 0) {
                    this.mCtrlSocketInfo.mRegisteredPkg.clear();
                    int i = 0;
                    this.mCtrlSocketInfo.mRegisteredCount = 0;
                    while (true) {
                        int i2 = i;
                        if (i2 < str.length) {
                            this.mCtrlSocketInfo.mRegisteredPkg.add(str[i2]);
                            this.mCtrlSocketInfo.mRegisteredCount++;
                            i = i2 + 1;
                        } else {
                            return;
                        }
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
            Settings.Secure.putString(HwConnectivityService.this.mContext.getContentResolver(), "registered_pkgs", registeredPkg.toString());
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

        public void updateStatus(Intent intent) {
            String action = intent.getAction();
            if (MSG_SCROFF_CTRLSOCKET_STATS.equals(action)) {
                if (intent.getBooleanExtra("ctrl_socket_status", false)) {
                    this.mCtrlSocketInfo.mScrOffActPkg.clear();
                    this.mCtrlSocketInfo.mAllowCtrlSocketLevel = this.ALLOW_PART_CTRL_SOCKET_LEVEL;
                    String actPkgs = intent.getStringExtra("ctrl_socket_list");
                    if (!TextUtils.isEmpty(actPkgs)) {
                        String[] whitePackages = actPkgs.split("\t");
                        if (whitePackages != null) {
                            for (String add : whitePackages) {
                                this.mCtrlSocketInfo.mScrOffActPkg.add(add);
                            }
                        }
                    }
                }
            } else if (MSG_ALL_CTRLSOCKET_ALLOWED.equals(action)) {
                this.mCtrlSocketInfo.mAllowCtrlSocketLevel = this.ALLOW_NO_CTRL_SOCKET_LEVEL;
            }
        }
    }

    private class WifiDisconnectManager {
        private static final String ACTION_SWITCH_TO_MOBILE_NETWORK = "android.intent.action.SWITCH_TO_MOBILE_NETWORK";
        private static final String ACTION_WIFI_NETWORK_CONNECTION_CHANGED = "huawei.intent.action.WIFI_NETWORK_CONNECTION_CHANGED";
        private static final String CONNECT_STATE = "connect_state";
        private static final String SWITCH_STATE = "switch_state";
        private static final int SWITCH_TO_WIFI_AUTO = 0;
        private static final String SWITCH_TO_WIFI_TYPE = "wifi_connect_type";
        private static final String WIFI_TO_PDP = "wifi_to_pdp";
        private static final int WIFI_TO_PDP_AUTO = 1;
        private static final int WIFI_TO_PDP_NEVER = 2;
        private static final int WIFI_TO_PDP_NOTIFY = 0;
        private boolean REMIND_WIFI_TO_PDP;
        private boolean mDialogHasShown;
        NetworkInfo.State mLastWifiState;
        private BroadcastReceiver mNetworkSwitchReceiver;
        /* access modifiers changed from: private */
        public boolean mShouldStartMobile;
        private Handler mSwitchHandler;
        private DialogInterface.OnDismissListener mSwitchPdpListener;
        protected AlertDialog mWifiToPdpDialog;
        private boolean shouldShowDialogWhenConnectFailed;

        private WifiDisconnectManager() {
            this.REMIND_WIFI_TO_PDP = false;
            this.mWifiToPdpDialog = null;
            this.mShouldStartMobile = false;
            this.shouldShowDialogWhenConnectFailed = true;
            this.mDialogHasShown = false;
            this.mLastWifiState = NetworkInfo.State.DISCONNECTED;
            this.mSwitchPdpListener = new DialogInterface.OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    HwTelephonyFactory.getHwDataServiceChrManager().sendMonitorWifiSwitchToMobileMessage(5000);
                    if (WifiDisconnectManager.this.mShouldStartMobile) {
                        HwConnectivityService.this.setMobileDataEnabled("wifi", true);
                        HwConnectivityService.log("you have restart Mobile data service!");
                    }
                    boolean unused = WifiDisconnectManager.this.mShouldStartMobile = false;
                    WifiDisconnectManager.this.mWifiToPdpDialog = null;
                }
            };
            this.mNetworkSwitchReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    if (!WifiDisconnectManager.ACTION_SWITCH_TO_MOBILE_NETWORK.equals(intent.getAction())) {
                        return;
                    }
                    if (intent.getBooleanExtra(WifiDisconnectManager.SWITCH_STATE, true)) {
                        HwConnectivityService.this.mWifiDisconnectManager.switchToMobileNetwork();
                    } else {
                        HwConnectivityService.this.mWifiDisconnectManager.cancelSwitchToMobileNetwork();
                    }
                }
            };
            this.mSwitchHandler = new Handler() {
                public void handleMessage(Message msg) {
                    HwConnectivityService.log("mSwitchHandler recieve msg =" + msg.what);
                    if (msg.what == 0) {
                        WifiDisconnectManager.this.switchToMobileNetwork();
                    }
                }
            };
        }

        private boolean getAirplaneModeEnable() {
            boolean retVal;
            boolean retVal2 = true;
            if (Settings.System.getInt(HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).getContentResolver(), "airplane_mode_on", 0) != 1) {
                retVal2 = false;
            }
            HwConnectivityService.log("getAirplaneModeEnable returning " + retVal);
            return retVal;
        }

        private AlertDialog createSwitchToPdpWarning() {
            HwConnectivityService.log("create dialog of switch to pdp");
            HwTelephonyFactory.getHwDataServiceChrManager().removeMonitorWifiSwitchToMobileMessage();
            AlertDialog.Builder buider = new AlertDialog.Builder(HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this), 33947691);
            View view = LayoutInflater.from(buider.getContext()).inflate(34013282, null);
            final CheckBox checkBox = (CheckBox) view.findViewById(34603157);
            buider.setView(view);
            buider.setTitle(33685520);
            buider.setIcon(17301543);
            buider.setPositiveButton(33685567, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialoginterface, int i) {
                    boolean unused = WifiDisconnectManager.this.mShouldStartMobile = true;
                    HwConnectivityService.log("setPositiveButton: mShouldStartMobile set true");
                    WifiDisconnectManager.this.checkUserChoice(checkBox.isChecked(), true);
                }
            });
            buider.setNegativeButton(33685568, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialoginterface, int i) {
                    HwConnectivityService.log("you have chose to disconnect Mobile data service!");
                    boolean unused = WifiDisconnectManager.this.mShouldStartMobile = false;
                    WifiDisconnectManager.this.checkUserChoice(checkBox.isChecked(), false);
                }
            });
            AlertDialog dialog = buider.create();
            dialog.setCancelable(false);
            dialog.getWindow().setType(HwArbitrationDEFS.MSG_MPLINK_BIND_FAIL);
            return dialog;
        }

        /* access modifiers changed from: private */
        public void checkUserChoice(boolean rememberChoice, boolean enableDataConnect) {
            int showPopState;
            if (!rememberChoice) {
                showPopState = 0;
            } else if (enableDataConnect) {
                showPopState = 1;
            } else {
                showPopState = 0;
            }
            HwConnectivityService.log("checkUserChoice showPopState:" + showPopState + ", enableDataConnect:" + enableDataConnect);
            Settings.System.putInt(HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).getContentResolver(), WIFI_TO_PDP, showPopState);
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
            if (!isSwitchToWifiSupported() || Settings.System.getInt(HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).getContentResolver(), SWITCH_TO_WIFI_TYPE, 0) == 0) {
                return false;
            }
            return true;
        }

        private boolean isSwitchToWifiSupported() {
            return "CMCC".equalsIgnoreCase(SystemProperties.get("ro.config.operators", "")) || HwConnectivityService.this.mCust.isSupportWifiConnectMode(HwConnectivityService.this.mContext);
        }

        /* access modifiers changed from: private */
        public boolean shouldShowDialog() {
            int value = Settings.System.getInt(HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).getContentResolver(), WIFI_TO_PDP, 1);
            if (!this.shouldShowDialogWhenConnectFailed || this.mDialogHasShown || value != 0) {
                return false;
            }
            return true;
        }

        /* access modifiers changed from: private */
        public void switchToMobileNetwork() {
            if (getAirplaneModeEnable()) {
                HwConnectivityService.this.enableDefaultTypeAPN(true);
            } else if (this.shouldShowDialogWhenConnectFailed || !this.mDialogHasShown) {
                int value = Settings.System.getInt(HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).getContentResolver(), WIFI_TO_PDP, 1);
                HwConnectivityService.log("WIFI_TO_PDP value =" + value);
                int wifiplusvalue = Settings.System.getInt(HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).getContentResolver(), "wifi_csp_dispaly_state", 1);
                HwConnectivityService.log("wifiplus_csp_dispaly_state value =" + wifiplusvalue);
                HwVSimManager hwVSimManager = HwVSimManager.getDefault();
                if (hwVSimManager != null && hwVSimManager.isVSimEnabled()) {
                    HwConnectivityService.log("vsim is enabled and following process will execute enableDefaultTypeAPN(true), so do nothing that likes value == WIFI_TO_PDP_AUTO");
                } else if (value == 0) {
                    if (wifiplusvalue == 0) {
                        HwConnectivityService.log("wifi_csp_dispaly_state = 0, Don't create WifiToPdpDialog");
                        HwConnectivityService.log("enableDefaultTypeAPN(true) in switchToMobileNetwork()  ");
                        HwConnectivityService.this.shouldEnableDefaultAPN();
                        return;
                    }
                    HwConnectivityService.this.setMobileDataEnabled("wifi", false);
                    this.mShouldStartMobile = true;
                    this.mDialogHasShown = true;
                    this.mWifiToPdpDialog = createSwitchToPdpWarning();
                    this.mWifiToPdpDialog.setOnDismissListener(this.mSwitchPdpListener);
                    this.mWifiToPdpDialog.show();
                } else if (value != 1) {
                    if (1 == wifiplusvalue) {
                        HwConnectivityService.this.setMobileDataEnabled("wifi", false);
                    } else {
                        HwConnectivityService.log("wifi_csp_dispaly_state = 0, Don't setMobileDataEnabled");
                    }
                }
                HwConnectivityService.log("enableDefaultTypeAPN(true) in switchToMobileNetwork( )");
                HwConnectivityService.this.shouldEnableDefaultAPN();
            }
        }

        /* access modifiers changed from: private */
        public void cancelSwitchToMobileNetwork() {
            if (this.mWifiToPdpDialog != null) {
                Log.d(HwConnectivityService.TAG, "cancelSwitchToMobileNetwork and mWifiToPdpDialog is showing");
                this.mShouldStartMobile = true;
                this.mWifiToPdpDialog.dismiss();
            }
        }

        /* access modifiers changed from: private */
        public void registerReceiver() {
            this.REMIND_WIFI_TO_PDP = "true".equals(Settings.Global.getString(HwConnectivityService.this.mContext.getContentResolver(), "hw_RemindWifiToPdp"));
            if (this.REMIND_WIFI_TO_PDP && isSwitchToWifiSupported()) {
                IntentFilter filter = new IntentFilter();
                filter.addAction(ACTION_SWITCH_TO_MOBILE_NETWORK);
                HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).registerReceiver(this.mNetworkSwitchReceiver, filter);
            }
        }

        /* access modifiers changed from: protected */
        public void hintUserSwitchToMobileWhileWifiDisconnected(NetworkInfo.State state, int type) {
            HwConnectivityService.log("hintUserSwitchToMobileWhileWifiDisconnected, state=" + state + "  type =" + type);
            boolean shouldEnableDefaultTypeAPN = true;
            this.REMIND_WIFI_TO_PDP = "true".equals(Settings.Global.getString(HwConnectivityService.this.mContext.getContentResolver(), "hw_RemindWifiToPdp"));
            if (this.REMIND_WIFI_TO_PDP) {
                if (state == NetworkInfo.State.DISCONNECTED && type == 1 && HwConnectivityService.this.getMobileDataEnabled()) {
                    if (this.mLastWifiState == NetworkInfo.State.CONNECTED) {
                        int value = Settings.System.getInt(HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).getContentResolver(), WIFI_TO_PDP, 1);
                        HwConnectivityService.log("WIFI_TO_PDP value     =" + value);
                        if (value == 1) {
                            HwConnectivityService.this.shouldEnableDefaultAPN();
                            return;
                        }
                        this.shouldShowDialogWhenConnectFailed = true;
                        HwConnectivityService.log("mShouldEnableDefaultTypeAPN was set false");
                        shouldEnableDefaultTypeAPN = false;
                    }
                    if (shouldNotifySettings() != 0) {
                        sendWifiBroadcast(false);
                    } else if (!getAirplaneModeEnable()) {
                        this.mSwitchHandler.sendMessageDelayed(this.mSwitchHandler.obtainMessage(0), 5000);
                        HwConnectivityService.log("switch message will be send in 5 seconds");
                    } else {
                        shouldEnableDefaultTypeAPN = true;
                    }
                    if (this.mLastWifiState == NetworkInfo.State.CONNECTING) {
                        this.shouldShowDialogWhenConnectFailed = false;
                    }
                } else if ((state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING) && type == 1) {
                    if (state == NetworkInfo.State.CONNECTED) {
                        this.mDialogHasShown = false;
                    }
                    if (shouldNotifySettings()) {
                        sendWifiBroadcast(true);
                    } else if (this.mSwitchHandler.hasMessages(0)) {
                        this.mSwitchHandler.removeMessages(0);
                        HwConnectivityService.log("switch message was removed");
                    }
                    if (this.mWifiToPdpDialog != null) {
                        this.mShouldStartMobile = true;
                        this.mWifiToPdpDialog.dismiss();
                    }
                }
                if (type == 1) {
                    HwConnectivityService.log("mLastWifiState =" + this.mLastWifiState);
                    this.mLastWifiState = state;
                }
            }
            if (shouldEnableDefaultTypeAPN && state == NetworkInfo.State.DISCONNECTED && type == 1) {
                HwConnectivityService.log("enableDefaultTypeAPN(true) in hintUserSwitchToMobileWhileWifiDisconnected");
                HwConnectivityService.this.shouldEnableDefaultAPN();
            }
        }

        /* access modifiers changed from: protected */
        public void makeDefaultAndHintUser(NetworkAgentInfo newNetwork) {
        }
    }

    static {
        boolean z = true;
        if (!"389".equals(SystemProperties.get("ro.config.hw_opta")) || !"840".equals(SystemProperties.get("ro.config.hw_optb"))) {
            z = false;
        }
        isVerizon = z;
    }

    /* access modifiers changed from: private */
    public static void log(String s) {
        Slog.d(TAG, s);
    }

    /* access modifiers changed from: private */
    public static void loge(String s) {
        Slog.e(TAG, s);
    }

    public HwConnectivityService(Context context, INetworkManagementService netd, INetworkStatsService statsService, INetworkPolicyManager policyManager) {
        super(context, netd, statsService, policyManager);
        this.mContext = context;
        this.mSimChangeAlertDataConnect = Settings.System.getString(context.getContentResolver(), "hw_sim_change_alert_data_connect");
        this.mRegisteredPushPkg.init(context);
        registerSimStateReceiver(context);
        if (isVerizon) {
            registerVerizonWifiDisconnectedReceiver(context);
        }
        this.mWifiDisconnectManager.registerReceiver();
        registerPhoneStateListener(context);
        registerBootStateListener(context);
        this.mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
        this.mHandler = new HwConnectivityServiceHandler();
        this.mDomainPreferHandler = new DomainPreferHandler(this.mHandlerThread.getLooper());
        if (Boolean.valueOf(SystemProperties.getBoolean("ro.vendor.config.hw_vowifi", false)).booleanValue()) {
            registerMapconIntentReceiver(context);
        }
        this.mServer = Settings.Global.getString(context.getContentResolver(), "captive_portal_server");
        if (TextUtils.isEmpty(this.mServer) || this.mServer.startsWith("http")) {
            this.mServer = DEFAULT_SERVER;
        }
        SystemProperties.set("sys.defaultapn.enabled", "true");
        registerTetheringReceiver(context);
        initGCMFixer(context);
        if (true == HiCureEnabled) {
            this.mHiCureManager = HwHiCureManager.createInstance(context);
            log("HwHiCureManager setup.");
        }
        this.mDefaultDataSub = SubscriptionManager.getDefaultDataSubscriptionId();
        this.mWifiProPropertyEnabled = WifiProCommonUtils.isWifiProPropertyEnabled(context);
    }

    private void initGCMFixer(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_OF_ANDROID_BOOT_COMPLETED);
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        this.mContext.registerReceiver(this.mGcmFixIntentReceiver, filter);
        IntentFilter filter2 = new IntentFilter();
        filter2.addAction(HeartbeatReceiver.HEARTBEAT_FIXER_ACTION);
        this.mContext.registerReceiver(this.mHeartbeatReceiver, filter2, "android.permission.CONNECTIVITY_INTERNAL", null);
    }

    private String[] getFeature(String str) {
        if (str != null) {
            String[] result = new String[2];
            int subId = 0;
            if (WrapperFactory.getMSimTelephonyManagerWrapper().isMultiSimEnabled()) {
                subId = WrapperFactory.getMSimTelephonyManagerWrapper().getPreferredDataSubscription();
                if (str.equals("enableMMS_sub2")) {
                    str = "enableMMS";
                    subId = 1;
                } else if (str.equals("enableMMS_sub1")) {
                    str = "enableMMS";
                    subId = 0;
                }
            }
            result[0] = str;
            result[1] = String.valueOf(subId);
            Slog.d(TAG, "getFeature: return feature=" + str + " subId=" + subId);
            return result;
        }
        throw new IllegalArgumentException("getFeature() received null string");
    }

    /* access modifiers changed from: protected */
    public String getMmsFeature(String feature) {
        Slog.d(TAG, "getMmsFeature HwFeatureConfig.dual_card_mms_switch" + HwFeatureConfig.dual_card_mms_switch);
        if (!HwFeatureConfig.dual_card_mms_switch) {
            return feature;
        }
        String[] result = getFeature(feature);
        String feature2 = result[0];
        this.phoneId = Integer.parseInt(result[1]);
        this.curMmsDataSub = -1;
        return feature2;
    }

    /* access modifiers changed from: protected */
    public boolean isAlwaysAllowMMSforRoaming(int networkType, String feature) {
        if (networkType == 0) {
            boolean isAlwaysAllowMMSforRoaming = isAlwaysAllowMMS;
            if (HwPhoneConstants.IS_CHINA_TELECOM) {
                boolean isNetworkRoaming = WrapperFactory.getMSimTelephonyManagerWrapper().isNetworkRoaming(this.phoneId);
                if (isAlwaysAllowMMSforRoaming) {
                }
            }
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean isMmsAutoSetSubDiffFromDataSub(int networkType, String feature) {
        if (!HwFeatureConfig.dual_card_mms_switch) {
            return false;
        }
        this.curPrefDataSubscription = WrapperFactory.getMSimTelephonyManagerWrapper().getPreferredDataSubscription();
        this.curMmsDataSub = WrapperFactory.getMSimTelephonyManagerWrapper().getMmsAutoSetDataSubscription();
        if (!feature.equals("enableMMS") || networkType != 0) {
            return false;
        }
        if ((this.curMmsDataSub != 0 && 1 != this.curMmsDataSub) || this.phoneId == this.curMmsDataSub) {
            return false;
        }
        log("DSMMS dds is switching now, do not response request from another card, curMmsDataSub: " + this.curMmsDataSub);
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean isMmsSubDiffFromDataSub(int networkType, String feature) {
        if (!HwFeatureConfig.dual_card_mms_switch || !feature.equals("enableMMS") || networkType != 0 || this.curPrefDataSubscription == this.phoneId) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean isNetRequestersPidsContainCurrentPid(List<Integer>[] mNetRequestersPids, int usedNetworkType, Integer currentPid) {
        if (!HwFeatureConfig.dual_card_mms_switch || !WrapperFactory.getMSimTelephonyManagerWrapper().isMultiSimEnabled() || mNetRequestersPids[usedNetworkType].contains(currentPid)) {
            return true;
        }
        Slog.w(TAG, "not tearing down special network - not found pid " + currentPid);
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isNeedTearMmsAndRestoreData(int networkType, String feature, Handler mHandler2) {
        int lastPrefDataSubscription = 1;
        if (!HwFeatureConfig.dual_card_mms_switch) {
            return true;
        }
        if (networkType != 0 || !feature.equals("enableMMS")) {
            return true;
        }
        if (!WrapperFactory.getMSimTelephonyManagerWrapper().isMultiSimEnabled()) {
            return true;
        }
        int curMmsDataSub2 = WrapperFactory.getMSimTelephonyManagerWrapper().getMmsAutoSetDataSubscription();
        if (curMmsDataSub2 != 0 && 1 != curMmsDataSub2) {
            return true;
        }
        if (curMmsDataSub2 != 0) {
            lastPrefDataSubscription = 0;
        }
        int curPrefDataSubscription2 = WrapperFactory.getMSimTelephonyManagerWrapper().getPreferredDataSubscription();
        log("isNeedTearDataAndRestoreData lastPrefDataSubscription" + lastPrefDataSubscription + "curPrefDataSubscription" + curPrefDataSubscription2);
        if (lastPrefDataSubscription != curPrefDataSubscription2) {
            log("DSMMS >>>> disable a connection, after MMS net disconnected will switch back to phone " + lastPrefDataSubscription);
            WrapperFactory.getMSimTelephonyManagerWrapper().setPreferredDataSubscription(lastPrefDataSubscription);
        } else {
            log("DSMMS unexpected case, data subscription is already on " + curPrefDataSubscription2);
        }
        WrapperFactory.getMSimTelephonyManagerWrapper().setMmsAutoSetDataSubscription(-1);
        return true;
    }

    private boolean isConnectedOrConnectingOrSuspended(NetworkInfo info) {
        boolean z;
        synchronized (this) {
            if (!(info.getState() == NetworkInfo.State.CONNECTED || info.getState() == NetworkInfo.State.CONNECTING)) {
                if (info.getState() != NetworkInfo.State.SUSPENDED) {
                    z = false;
                }
            }
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: private */
    public AlertDialog createWarningRoamingToPdp() {
        AlertDialog.Builder buider = new AlertDialog.Builder(connectivityServiceUtils.getContext(this), connectivityServiceUtils.getContext(this).getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null));
        buider.setTitle(33685962);
        buider.setMessage(33685963);
        buider.setIcon(17301543);
        buider.setPositiveButton(17040146, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                HwTelephonyManagerInner.getDefault().setDataRoamingEnabledWithoutPromp(true);
                Toast.makeText(HwConnectivityService.this.mContext, 33685965, 1).show();
            }
        });
        buider.setNegativeButton(17040145, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(HwConnectivityService.this.mContext, 33685966, 1).show();
            }
        });
        AlertDialog dialog = buider.create();
        dialog.getWindow().setType(HwArbitrationDEFS.MSG_MPLINK_BIND_FAIL);
        return dialog;
    }

    /* JADX WARNING: type inference failed for: r7v6, types: [android.view.View] */
    /* access modifiers changed from: private */
    /* JADX WARNING: Multi-variable type inference failed */
    public AlertDialog createWarningToPdp() {
        AlertDialog.Builder buider;
        final String enable_Not_Remind_Function = SettingsEx.Systemex.getString(connectivityServiceUtils.getContext(this).getContentResolver(), ENABLE_NOT_REMIND_FUNCTION);
        CheckBox checkBox = null;
        if (VALUE_ENABLE_NOT_REMIND_FUNCTION.equals(enable_Not_Remind_Function)) {
            int themeID = connectivityServiceUtils.getContext(this).getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null);
            buider = new AlertDialog.Builder(new ContextThemeWrapper(connectivityServiceUtils.getContext(this), themeID), themeID);
            View view = LayoutInflater.from(buider.getContext()).inflate(34013280, null);
            checkBox = view.findViewById(34603158);
            buider.setView(view);
            buider.setTitle(17039380);
        } else {
            buider = new AlertDialog.Builder(connectivityServiceUtils.getContext(this), connectivityServiceUtils.getContext(this).getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null));
            buider.setTitle(17039380);
            buider.setMessage(33685526);
        }
        final CheckBox finalBox = checkBox;
        buider.setIcon(17301543);
        buider.setPositiveButton(17040146, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                HwTelephonyManagerInner.getDefault().setDataEnabledWithoutPromp(true);
                if (!TextUtils.isEmpty(HwConnectivityService.this.mSimChangeAlertDataConnect)) {
                    if (HwConnectivityService.this.mShowWarningRoamingToPdp) {
                        HwConnectivityService.this.createWarningRoamingToPdp().show();
                    }
                    Toast.makeText(HwConnectivityService.this.mContext, 33685967, 1).show();
                }
                if (HwConnectivityService.VALUE_ENABLE_NOT_REMIND_FUNCTION.equals(enable_Not_Remind_Function) && finalBox != null) {
                    HwConnectivityService.this.updateReminderSetting(finalBox.isChecked());
                }
                AlertDialog unused = HwConnectivityService.this.mDataServiceToPdpDialog = null;
                boolean unused2 = HwConnectivityService.this.mShowWarningRoamingToPdp = false;
            }
        });
        buider.setNegativeButton(17040145, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).sendBroadcast(new Intent(HwConnectivityService.DISABEL_DATA_SERVICE_ACTION));
                HwTelephonyManagerInner.getDefault().setDataEnabledWithoutPromp(false);
                if (!TextUtils.isEmpty(HwConnectivityService.this.mSimChangeAlertDataConnect)) {
                    Toast.makeText(HwConnectivityService.this.mContext, 33685968, 1).show();
                }
                if (HwConnectivityService.VALUE_ENABLE_NOT_REMIND_FUNCTION.equals(enable_Not_Remind_Function) && finalBox != null) {
                    HwConnectivityService.this.updateReminderSetting(finalBox.isChecked());
                }
                AlertDialog unused = HwConnectivityService.this.mDataServiceToPdpDialog = null;
                boolean unused2 = HwConnectivityService.this.mShowWarningRoamingToPdp = false;
            }
        });
        buider.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).sendBroadcast(new Intent(HwConnectivityService.DISABEL_DATA_SERVICE_ACTION));
                HwTelephonyManagerInner.getDefault().setDataEnabledWithoutPromp(false);
                if (HwConnectivityService.VALUE_ENABLE_NOT_REMIND_FUNCTION.equals(enable_Not_Remind_Function) && finalBox != null) {
                    HwConnectivityService.this.updateReminderSetting(finalBox.isChecked());
                }
                AlertDialog unused = HwConnectivityService.this.mDataServiceToPdpDialog = null;
                boolean unused2 = HwConnectivityService.this.mShowWarningRoamingToPdp = false;
            }
        });
        AlertDialog dialog = buider.create();
        dialog.getWindow().setType(HwArbitrationDEFS.MSG_MPLINK_BIND_FAIL);
        return dialog;
    }

    /* access modifiers changed from: protected */
    public void registerPhoneStateListener(Context context) {
        ((TelephonyManager) context.getSystemService("phone")).listen(this.mPhoneStateListener, 33);
    }

    /* access modifiers changed from: private */
    public final void updateCallState(int state) {
        if (!this.mRemindService && !SystemProperties.getBoolean("gsm.huawei.RemindDataService", false) && !SystemProperties.getBoolean("gsm.huawei.RemindDataService_1", false)) {
            return;
        }
        if (state == 0) {
            if (this.mShowDlgEndCall && this.mDataServiceToPdpDialog == null) {
                this.mDataServiceToPdpDialog = createWarningToPdp();
                this.mDataServiceToPdpDialog.show();
                this.mShowDlgEndCall = false;
            }
        } else if (this.mDataServiceToPdpDialog != null) {
            this.mDataServiceToPdpDialog.dismiss();
            this.mDataServiceToPdpDialog = null;
            this.mShowDlgEndCall = true;
        }
    }

    /* access modifiers changed from: protected */
    public void registerBootStateListener(Context context) {
        new MobileEnabledSettingObserver(new Handler()).register();
    }

    /* access modifiers changed from: protected */
    public boolean needSetUserDataEnabled(boolean enabled) {
        int dataStatus = Settings.Global.getInt(connectivityServiceUtils.getContext(this).getContentResolver(), "mobile_data", 1);
        if (!shouldShowThePdpWarning() || dataStatus != 0 || !enabled) {
            return true;
        }
        if (this.mShowDlgTurnOfDC) {
            this.mHandler.sendEmptyMessage(0);
            return false;
        }
        this.mShowDlgTurnOfDC = true;
        return true;
    }

    /* access modifiers changed from: private */
    public void updateReminderSetting(boolean chooseNotRemind) {
        if (chooseNotRemind) {
            Settings.System.putInt(connectivityServiceUtils.getContext(this).getContentResolver(), WHETHER_SHOW_PDP_WARNING, VALUE_NOT_SHOW_PDP);
        }
    }

    /* access modifiers changed from: private */
    public boolean shouldShowThePdpWarning() {
        if (WrapperFactory.getMSimTelephonyManagerWrapper().isMultiSimEnabled()) {
            return shouldShowThePdpWarningMsim();
        }
        String enable_Not_Remind_Function = SettingsEx.Systemex.getString(connectivityServiceUtils.getContext(this).getContentResolver(), ENABLE_NOT_REMIND_FUNCTION);
        boolean z = false;
        boolean remindDataAllow = SystemProperties.getBoolean("gsm.huawei.RemindDataService", false);
        int pdpWarningValue = Settings.System.getInt(connectivityServiceUtils.getContext(this).getContentResolver(), WHETHER_SHOW_PDP_WARNING, VALUE_SHOW_PDP);
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
                return SystemProperties.getBoolean("gsm.huawei.RemindDataService", false);
            }
            return SystemProperties.getBoolean("gsm.huawei.RemindDataService_1", false);
        } else if (1 == lDataVal) {
            return SystemProperties.getBoolean("gsm.huawei.RemindDataService_1", false);
        } else {
            return false;
        }
    }

    private boolean shouldShowThePdpWarningMsim() {
        String enableNotRemindFunction = SettingsEx.Systemex.getString(this.mContext.getContentResolver(), ENABLE_NOT_REMIND_FUNCTION);
        boolean remindDataAllow = false;
        int lDataVal = WrapperFactory.getMSimTelephonyManagerWrapper().getPreferredDataSubscription();
        boolean z = true;
        if (1 == lDataVal) {
            remindDataAllow = SystemProperties.getBoolean("gsm.huawei.RemindDataService_1", false);
        } else if (lDataVal == 0) {
            remindDataAllow = SystemProperties.getBoolean("gsm.huawei.RemindDataService", false);
        }
        int pdpWarningValue = Settings.System.getInt(this.mContext.getContentResolver(), WHETHER_SHOW_PDP_WARNING, VALUE_SHOW_PDP);
        if (!VALUE_ENABLE_NOT_REMIND_FUNCTION.equals(enableNotRemindFunction)) {
            return remindDataAllow;
        }
        if (!remindDataAllow || pdpWarningValue != VALUE_SHOW_PDP) {
            z = false;
        }
        return z;
    }

    private boolean shouldDisablePortalCheck(String ssid) {
        if (ssid != null) {
            log("wifi ssid: " + ssid);
            if (ssid.length() > 2 && ssid.charAt(0) == '\"' && ssid.charAt(ssid.length() - 1) == '\"') {
                ssid = ssid.substring(1, ssid.length() - 1);
            }
        }
        if (2 == Settings.Secure.getInt(this.mContext.getContentResolver(), WifiProCommonUtils.PORTAL_NETWORK_FLAG, 0)) {
            Settings.Secure.putInt(this.mContext.getContentResolver(), WifiProCommonUtils.PORTAL_NETWORK_FLAG, 0);
            log("not stop portal for user click wifi+ notification");
            return false;
        } else if (1 == Settings.Secure.getInt(this.mContext.getContentResolver(), "user_setup_complete", 0) && 1 == Settings.Secure.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) && 1 == Settings.Global.getInt(this.mContext.getContentResolver(), "hw_disable_portal", 0)) {
            log("stop portal check for orange");
            return true;
        } else if ("CMCC".equalsIgnoreCase(SystemProperties.get("ro.config.operators", "")) && "CMCC".equals(ssid)) {
            log("stop portal check for CMCC");
            return true;
        } else if (1 == Settings.System.getInt(connectivityServiceUtils.getContext(this).getContentResolver(), DISABLE_PORTAL_CHECK, 0)) {
            Settings.System.putInt(connectivityServiceUtils.getContext(this).getContentResolver(), DISABLE_PORTAL_CHECK, 0);
            log("stop portal check for airsharing");
            return true;
        } else if (Settings.Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) == 0 && "true".equals(SettingsEx.Systemex.getString(this.mContext.getContentResolver(), "wifi.challenge.required"))) {
            log("setup guide wifi disable portal, and does not start browser!");
            return true;
        } else if (this.mIsTopAppSkytone) {
            log("stop start broswer for TopAppSkytone");
            return true;
        } else if (this.mIsTopAppHsbb) {
            log("stop start broswer for TopAppHsbb");
            return true;
        } else if (1 != Settings.System.getInt(connectivityServiceUtils.getContext(this).getContentResolver(), WIFI_AP_MANUAL_CONNECT, 0)) {
            return false;
        } else {
            Settings.System.putInt(connectivityServiceUtils.getContext(this).getContentResolver(), WIFI_AP_MANUAL_CONNECT, 0);
            log("portal ap manual connect");
            return false;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:29:0x00eb A[Catch:{ ActivityNotFoundException -> 0x0149 }] */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x00f1 A[Catch:{ ActivityNotFoundException -> 0x0149 }] */
    public boolean startBrowserForWifiPortal(Notification notification, String ssid) {
        if (shouldDisablePortalCheck(ssid)) {
            log("do not start browser, popup system notification");
            return false;
        }
        log("setNotificationVisible: cancel notification and start browser directly for TYPE_WIFI..");
        try {
            String usedUrl = Settings.Global.getString(this.mContext.getContentResolver(), "captive_portal_server");
            if (TextUtils.isEmpty(usedUrl) || !usedUrl.startsWith("http")) {
                if (IS_CHINA) {
                    String operator = TelephonyManager.getDefault().getNetworkOperator();
                    if (operator == null || operator.length() == 0 || !operator.startsWith("460")) {
                        this.mURL = new URL("http://" + this.mServer + "/generate_204");
                    } else {
                        this.mURL = new URL("http://connectivitycheck.platform.hicloud.com/generate_204");
                    }
                } else {
                    this.mURL = new URL("http://" + this.mServer + "/generate_204");
                }
                Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(this.mURL.toString()));
                intent.setFlags(272629760);
                notification.contentIntent = PendingIntent.getActivity(connectivityServiceUtils.getContext(this), 0, intent, 0);
                try {
                    if (1 != Settings.Secure.getInt(connectivityServiceUtils.getContext(this).getContentResolver(), WifiProCommonUtils.PORTAL_NETWORK_FLAG, 0)) {
                        log("browser has been launched by notification user clicked it, don't launch browser here again.");
                        return true;
                    }
                    Settings.Secure.putInt(connectivityServiceUtils.getContext(this).getContentResolver(), WifiProCommonUtils.PORTAL_NETWORK_FLAG, 1);
                    intent.putExtra(WifiProCommonUtils.BROWSER_LAUNCH_FROM, WifiProCommonUtils.BROWSER_LAUNCHED_BY_WIFI_PORTAL);
                    if (!SystemProperties.getBoolean("runtime.hwwifi.portal_webview_support", false)) {
                        if (isSetupWizardCompleted() || IS_CHINA) {
                            if (IS_CHINA) {
                                String packageName = "com.android.browser";
                                String className = "com.android.browser.BrowserActivity";
                                if (Utils.isPackageInstalled("com.huawei.browser", this.mContext)) {
                                    packageName = "com.huawei.browser";
                                    className = "com.huawei.browser.Main";
                                }
                                intent.setClassName(packageName, className);
                            }
                            connectivityServiceUtils.getContext(this).startActivity(intent);
                            return true;
                        }
                    }
                    startCaptivePortalWebView(this.mContext, this.mURL);
                    return true;
                } catch (ActivityNotFoundException e) {
                    try {
                        log("default browser not exist..");
                        if (!isSetupWizardCompleted()) {
                            log("setup wizard is not completed");
                            startCaptivePortalWebView(this.mContext, this.mURL);
                        } else {
                            notification.contentIntent.send();
                        }
                    } catch (PendingIntent.CanceledException e2) {
                        log("Sending contentIntent failed: " + e2);
                    } catch (ActivityNotFoundException e3) {
                        loge("Activity not found: " + e3);
                    }
                }
            } else {
                log("use the portal url from the settings");
                this.mURL = new URL(usedUrl);
                Intent intent2 = new Intent("android.intent.action.VIEW", Uri.parse(this.mURL.toString()));
                intent2.setFlags(272629760);
                notification.contentIntent = PendingIntent.getActivity(connectivityServiceUtils.getContext(this), 0, intent2, 0);
                if (1 != Settings.Secure.getInt(connectivityServiceUtils.getContext(this).getContentResolver(), WifiProCommonUtils.PORTAL_NETWORK_FLAG, 0)) {
                }
            }
        } catch (MalformedURLException e4) {
            log("MalformedURLException " + e4);
        }
    }

    public boolean isSystemBootComplete() {
        return this.sendWifiBroadcastAfterBootCompleted;
    }

    /* access modifiers changed from: protected */
    public void hintUserSwitchToMobileWhileWifiDisconnected(NetworkInfo.State state, int type) {
        if (WifiProCommonUtils.isWifiSelfCuring() && state == NetworkInfo.State.DISCONNECTED && type == 1) {
            Log.d("HwSelfCureEngine", "DISCONNECTED, but enableDefaultTypeAPN-->UP is ignored due to wifi self curing.");
        } else {
            this.mWifiDisconnectManager.hintUserSwitchToMobileWhileWifiDisconnected(state, type);
        }
    }

    /* access modifiers changed from: protected */
    public void enableDefaultTypeApnWhenWifiConnectionStateChanged(NetworkInfo.State state, int type) {
        if (state == NetworkInfo.State.DISCONNECTED && type == 1) {
            this.mIsWifiConnected = false;
            this.mIsTopAppSkytone = false;
            this.mIsTopAppHsbb = false;
            if (WifiProCommonUtils.isWifiSelfCuring() || this.mWifiDisconnectManager.shouldShowDialog()) {
                SystemProperties.set("sys.defaultapn.enabled", "false");
            }
        } else if (state == NetworkInfo.State.CONNECTED && type == 1) {
            this.mIsWifiConnected = true;
            String pktName = WifiProCommonUtils.getPackageName(this.mContext, WifiProCommonUtils.getForegroundAppUid(this.mContext));
            if (pktName != null && pktName.equals("com.huawei.hiskytone")) {
                this.mIsTopAppSkytone = true;
            } else if (pktName != null && pktName.equals("com.nfyg.hsbb")) {
                this.mIsTopAppHsbb = true;
            }
            SystemProperties.set("sys.defaultapn.enabled", "true");
        }
    }

    /* access modifiers changed from: private */
    public void shouldEnableDefaultAPN() {
        if (!this.mIsBlueThConnected) {
            enableDefaultTypeAPN(true);
        }
    }

    private void sendBlueToothTetheringBroadcast(boolean isBttConnected) {
        log("sendBroad bt_tethering_connect_state = " + isBttConnected);
        Intent intent = new Intent("android.intent.action.BlueToothTethering_NETWORK_CONNECTION_CHANGED");
        intent.putExtra("btt_connect_state", isBttConnected);
        connectivityServiceUtils.getContext(this).sendBroadcast(intent);
    }

    /* access modifiers changed from: protected */
    public void enableDefaultTypeApnWhenBlueToothTetheringStateChanged(NetworkAgentInfo networkAgent, NetworkInfo newInfo) {
        if (newInfo.getType() == 7) {
            log("enter BlueToothTethering State Changed");
            NetworkInfo.State state = newInfo.getState();
            if (state == NetworkInfo.State.CONNECTED) {
                this.mIsBlueThConnected = true;
                sendBlueToothTetheringBroadcast(true);
            } else if (state == NetworkInfo.State.DISCONNECTED) {
                this.mIsBlueThConnected = false;
                sendBlueToothTetheringBroadcast(false);
                if (!this.mIsWifiConnected) {
                    enableDefaultTypeAPN(true);
                }
            }
        }
    }

    public void makeDefaultAndHintUser(NetworkAgentInfo newNetwork) {
        this.mWifiDisconnectManager.makeDefaultAndHintUser(newNetwork);
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (code == 1101) {
            data.enforceInterface(descriptor);
            int enableInt = data.readInt();
            Log.d(TAG, "needSetUserDataEnabled enableInt = " + enableInt);
            boolean result = needSetUserDataEnabled(enableInt == 1);
            Log.d(TAG, "needSetUserDataEnabled result = " + result);
            reply.writeNoException();
            reply.writeInt(result);
            return true;
        } else if (this.mRegisteredPushPkg.onTransact(code, data, reply, flags)) {
            return true;
        } else {
            return HwConnectivityService.super.onTransact(code, data, reply, flags);
        }
    }

    /* access modifiers changed from: private */
    public void setMobileDataEnabled(String module, boolean enabled) {
        Log.d(TAG, "module:" + module + " setMobileDataEnabled enabled = " + enabled);
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
        if (tm != null) {
            tm.setDataEnabled(enabled);
            tm.setDataEnabledProperties(module, enabled);
        }
    }

    private boolean getDataEnabled() {
        boolean ret = false;
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
        if (tm != null) {
            ret = tm.getDataEnabled();
        }
        Log.d(TAG, "CtrlSocket getMobileDataEnabled enabled = " + ret);
        return ret;
    }

    public boolean getMobileDataEnabled() {
        boolean ret = false;
        if (!this.mIsSimStateChanged) {
            return false;
        }
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
        if (tm != null) {
            try {
                int phoneCount = tm.getPhoneCount();
                boolean ret2 = false;
                for (int slotId = 0; slotId < phoneCount; slotId++) {
                    if (tm.getSimState(slotId) == 5) {
                        ret2 = true;
                    }
                }
                if (!ret2) {
                    Log.d(TAG, "all sim card not ready,return false");
                    return false;
                }
                ret = tm.getDataEnabled();
            } catch (NullPointerException e) {
                Log.d(TAG, "getMobileDataEnabled NPE");
            }
        }
        Log.d(TAG, "CtrlSocket getMobileDataEnabled = " + ret);
        return ret;
    }

    /* access modifiers changed from: private */
    public void enableDefaultTypeAPN(boolean enabled) {
        Log.d(TAG, "enableDefaultTypeAPN= " + enabled);
        String defaultMobileEnable = SystemProperties.get("sys.defaultapn.enabled", "true");
        Log.d(TAG, "DEFAULT_MOBILE_ENABLE before state is " + defaultMobileEnable);
        SystemProperties.set("sys.defaultapn.enabled", enabled ? "true" : "false");
        HwTelephonyManagerInner hwTm = HwTelephonyManagerInner.getDefault();
        if (hwTm != null) {
            hwTm.setDefaultMobileEnable(enabled);
        }
    }

    private void registerSimStateReceiver(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SIM_STATE_CHANGED");
        filter.addAction("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED");
        context.registerReceiver(this.mSimStateReceiver, filter);
    }

    /* access modifiers changed from: protected */
    public void updateMobileDataAlwaysOnCust(int slotId) {
        this.mCust.mMobileDataAlwaysOnCust = false;
        try {
            Boolean mobileDataAlwaysOnCfg = (Boolean) HwCfgFilePolicy.getValue("mobile_data_always_on", slotId, Boolean.class);
            if (mobileDataAlwaysOnCfg != null) {
                this.mCust.mMobileDataAlwaysOnCust = mobileDataAlwaysOnCfg.booleanValue();
            }
        } catch (Exception e) {
            log("Exception: read mobile_data_always_on error");
        }
        updateMobileDataAlwaysOn();
    }

    /* access modifiers changed from: private */
    public void handleShowEnablePdpDialog() {
        if (this.mDataServiceToPdpDialog == null) {
            this.mDataServiceToPdpDialog = createWarningToPdp();
            this.mDataServiceToPdpDialog.show();
        }
    }

    private void registerTetheringReceiver(Context context) {
        if (HwDeliverInfo.isIOTVersion() && SystemProperties.getBoolean("ro.config.persist_usb_tethering", false)) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.hardware.usb.action.USB_STATE");
            context.registerReceiver(this.mTetheringReceiver, filter);
        }
    }

    /* access modifiers changed from: protected */
    public void updataNetworkAgentInfoForHicure(NetworkAgentInfo nai) {
        if (nai.networkInfo.getType() != 0 || true != HiCureEnabled) {
            return;
        }
        if (NetworkInfo.State.CONNECTED == nai.networkInfo.getState()) {
            this.mHiCureManager.mDnsHiCureEngine.notifyConnectedInfo(nai);
        } else if (NetworkInfo.State.DISCONNECTED == nai.networkInfo.getState()) {
            this.mHiCureManager.mDnsHiCureEngine.notifyDisconnectedInfo();
        }
    }

    /* access modifiers changed from: protected */
    public boolean reportPortalNetwork(NetworkAgentInfo nai, int result) {
        if (result != 2) {
            return false;
        }
        nai.asyncChannel.sendMessage(528391, 3, 0, null);
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean ignoreRemovedByWifiPro(NetworkAgentInfo nai) {
        if (nai.networkInfo.getType() != 1 || !WifiProCommonUtils.isWifiProSwitchOn(this.mContext)) {
            return false;
        }
        NetworkInfo activeNetworkInfo = getActiveNetworkInfo();
        if (activeNetworkInfo == null || activeNetworkInfo.getType() != 7) {
            return true;
        }
        log("ignoreRemovedByWifiPro, bluetooth active, needs to remove wifi.");
        return false;
    }

    /* access modifiers changed from: protected */
    public void notifyMpLinkDefaultNetworkChange() {
        HwMpLinkContentAware.getInstance(this.mContext).notifyDefaultNetworkChange();
    }

    /* access modifiers changed from: protected */
    public boolean isAppBindedNetwork() {
        if (HwMplinkManager.getInstance() != null) {
            return HwMplinkManager.getInstance().isAppBindedNetwork();
        }
        log("HwMplinkManager is null");
        return false;
    }

    /* access modifiers changed from: protected */
    public NetworkInfo getActiveNetworkForMpLink(NetworkInfo info, int uid) {
        if (HwMplinkManager.getInstance() != null) {
            return HwMplinkManager.getInstance().getMpLinkNetworkInfo(info, uid);
        }
        log("HwMplinkManager is null");
        return info;
    }

    public Network getNetworkForTypeWifi() {
        Network activeNetwork = HwConnectivityService.super.getActiveNetwork();
        NetworkInfo activeNetworkInfo = HwConnectivityService.super.getActiveNetworkInfo();
        Network[] networks = HwConnectivityService.super.getAllNetworks();
        if (activeNetworkInfo != null && activeNetwork != null) {
            NetworkCapabilities anc = HwConnectivityService.super.getNetworkCapabilities(activeNetwork);
            boolean activeVpn = anc != null && anc.hasTransport(4);
            if (!activeVpn && activeNetworkInfo.getType() == 1) {
                return activeNetwork;
            }
            if (!activeVpn && activeNetworkInfo.getType() == 1) {
                return null;
            }
            for (int i = 0; i < networks.length; i++) {
                if (!(activeNetwork == null || networks[i].netId == activeNetwork.netId)) {
                    NetworkCapabilities nc = HwConnectivityService.super.getNetworkCapabilities(networks[i]);
                    if (nc != null && nc.hasTransport(1) && !nc.hasTransport(4)) {
                        return networks[i];
                    }
                }
            }
            return null;
        } else if (networks.length >= 1) {
            return networks[0];
        } else {
            return null;
        }
    }

    private NetworkInfo getNetworkInfoForBackgroundWifi() {
        NetworkInfo activeNetworkInfo = HwConnectivityService.super.getActiveNetworkInfo();
        Network[] networks = HwConnectivityService.super.getAllNetworks();
        if (activeNetworkInfo != null || networks.length != 1) {
            return null;
        }
        NetworkInfo result = new NetworkInfo(1, 0, ConnectivityManager.getNetworkTypeName(1), "");
        result.setDetailedState(NetworkInfo.DetailedState.CONNECTED, null, null);
        return result;
    }

    /* access modifiers changed from: protected */
    public void setVpnSettingValue(boolean enable) {
        log("WiFi_PRO, setVpnSettingValue =" + enable);
        Settings.System.putInt(this.mContext.getContentResolver(), "wifipro_network_vpn_state", enable);
    }

    private boolean isRequestedByPkgName(int pID, String pkgName) {
        HwActivityManagerService ams = HwActivityManagerService.self();
        if (ams == null) {
            return false;
        }
        ProcessRecord proc = ams.getProcessRecordLocked(pID);
        if (proc == null || proc.pkgList == null) {
            return false;
        }
        return proc.pkgList.containsKey(pkgName);
    }

    public NetworkInfo getActiveNetworkInfo() {
        NetworkInfo networkInfo = HwConnectivityService.super.getActiveNetworkInfo();
        if (networkInfo != null || !this.mWifiProPropertyEnabled || !isRequestedByPkgName(Binder.getCallingPid(), "com.huawei.systemmanager")) {
            return networkInfo;
        }
        Slog.d(TAG, "return the background wifi network info for system manager.");
        return getNetworkInfoForBackgroundWifi();
    }

    /* access modifiers changed from: protected */
    public boolean isNetworkRequestBip(NetworkRequest nr) {
        if (nr == null) {
            loge("network request is null!");
            return false;
        } else if (nr.networkCapabilities.hasCapability(23) || nr.networkCapabilities.hasCapability(24) || nr.networkCapabilities.hasCapability(25) || nr.networkCapabilities.hasCapability(26) || nr.networkCapabilities.hasCapability(27) || nr.networkCapabilities.hasCapability(28) || nr.networkCapabilities.hasCapability(29)) {
            return true;
        } else {
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public boolean checkNetworkSupportBip(NetworkAgentInfo nai, NetworkRequest nri) {
        if (HwModemCapability.isCapabilitySupport(1)) {
            log("MODEM is support BIP!");
            return false;
        } else if (nai == null || nri == null || nai.networkInfo == null) {
            loge("network agent or request is null, just return false!");
            return false;
        } else if (nai.networkInfo.getType() != 0 || !nai.isInternet()) {
            loge("NOT support internet or NOT mobile!");
            return false;
        } else if (!isNetworkRequestBip(nri)) {
            loge("network request is NOT bip!");
            return false;
        } else {
            String defaultApn = SystemProperties.get("gsm.default.apn");
            String bipApn = SystemProperties.get("gsm.bip.apn");
            if (defaultApn == null || bipApn == null) {
                loge("default apn is null or bip apn is null, default: " + defaultApn + ", bip: " + bipApn);
                return false;
            } else if (MemoryConstant.MEM_SCENE_DEFAULT.equals(bipApn)) {
                log("bip use default network, return true");
                return true;
            } else {
                String[] buffers = bipApn.split(",");
                if (buffers.length <= 1 || !defaultApn.equalsIgnoreCase(buffers[1].trim())) {
                    log("network do NOT support bip, default: " + defaultApn + ", bip: " + bipApn);
                    return false;
                }
                log("default apn support bip, default: " + defaultApn + ", bip: " + buffers[1].trim());
                return true;
            }
        }
    }

    public void setLteMobileDataEnabled(boolean enable) {
        log("[enter]setLteMobileDataEnabled " + enable);
        enforceChangePermission();
        HwTelephonyManagerInner.getDefault().setLteServiceAbility((int) enable);
        sendLteDataStateBroadcast(mLteMobileDataState);
    }

    public int checkLteConnectState() {
        enforceAccessPermission();
        return mLteMobileDataState;
    }

    /* access modifiers changed from: protected */
    public void handleLteMobileDataStateChange(NetworkInfo info) {
        int lteState;
        if (info == null) {
            Slog.e(TAG, "NetworkInfo got null!");
            return;
        }
        Slog.d(TAG, "[enter]handleLteMobileDataStateChange type=" + info.getType() + ",subType=" + info.getSubtype());
        if (info.getType() == 0) {
            if (13 == info.getSubtype()) {
                lteState = mapDataStateToLteDataState(info.getState());
            } else {
                lteState = 3;
            }
            setLteMobileDataState(lteState);
        }
    }

    private int mapDataStateToLteDataState(NetworkInfo.State state) {
        log("[enter]mapDataStateToLteDataState state=" + state);
        switch (AnonymousClass13.$SwitchMap$android$net$NetworkInfo$State[state.ordinal()]) {
            case 1:
                return 0;
            case 2:
                return 1;
            case 3:
                return 2;
            case 4:
                return 3;
            default:
                Slog.d(TAG, "mapDataStateToLteDataState ignore state = " + state);
                return 3;
        }
    }

    private synchronized void setLteMobileDataState(int state) {
        Slog.d(TAG, "[enter]setLteMobileDataState state=" + state);
        mLteMobileDataState = state;
        sendLteDataStateBroadcast(mLteMobileDataState);
    }

    private void sendLteDataStateBroadcast(int state) {
        Intent intent = new Intent(ACTION_LTEDATA_COMPLETED_ACTION);
        intent.putExtra(EXTRA_IS_LTE_MOBILE_DATA_STATUS, state);
        Slog.d(TAG, "Send sticky broadcast from ConnectivityService. intent=" + intent);
        sendStickyBroadcast(intent);
    }

    public long getLteTotalRxBytes() {
        Slog.d(TAG, "[enter]getLteTotalRxBytes");
        enforceAccessPermission();
        long lteRxBytes = 0;
        try {
            NetworkStatsHistory.Entry entry = getLteStatsEntry(2);
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

    private NetworkStatsHistory.Entry getLteStatsEntry(int fields) {
        Log.d(TAG, "[enter]getLteStatsEntry fields=" + fields);
        NetworkStatsHistory.Entry entry = null;
        INetworkStatsSession session = null;
        try {
            NetworkTemplate mobile4gTemplate = NetworkTemplate.buildTemplateMobileAll(((TelephonyManager) this.mContext.getSystemService("phone")).getSubscriberId());
            getStatsService().forceUpdate();
            session = getStatsService().openSession();
            if (session != null) {
                NetworkStatsHistory networkStatsHistory = session.getHistoryForNetwork(mobile4gTemplate, fields);
                if (networkStatsHistory != null) {
                    entry = networkStatsHistory.getValues(Long.MIN_VALUE, Long.MAX_VALUE, null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable th) {
            TrafficStats.closeQuietly(null);
            throw th;
        }
        TrafficStats.closeQuietly(session);
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

    /* access modifiers changed from: protected */
    public void registerMapconIntentReceiver(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(MAPCON_START_INTENT);
        filter.addAction(ACTION_MAPCON_SERVICE_FAILED);
        context.registerReceiver(this.mMapconIntentReceiver, filter);
    }

    private int getVoWifiServiceDomain(int phoneId2, int type) {
        int domainPrefer = -1;
        if (this.mMapconService != null) {
            try {
                domainPrefer = this.mMapconService.getVoWifiServiceDomain(phoneId2, type);
            } catch (RemoteException ex) {
                loge("getVoWifiServiceDomain failed, err = " + ex.toString());
            }
        }
        boolean isVoWifiRegistered = HwTelephonyManagerInner.getDefault().isWifiCallingAvailable(phoneId2);
        boolean isMmsFollowVowifiPreferDomain = getBooleanCarrierConfig(phoneId2, "mms_follow_vowifi_prefer_domain");
        log("getVoWifiServiceDomain  isVoWifiRegistered:" + isVoWifiRegistered + " isMmsFollowVowifiPreferDomain:" + isMmsFollowVowifiPreferDomain);
        if (!isMmsFollowVowifiPreferDomain || type != 0 || AbstractConnectivityService.DomainPreferType.DOMAIN_PREFER_VOLTE.value() != domainPrefer || !isVoWifiRegistered) {
            return domainPrefer;
        }
        log("VoWiFi registered and set mms domain as DOMAIN_PREFER_WIFI");
        return AbstractConnectivityService.DomainPreferType.DOMAIN_PREFER_WIFI.value();
    }

    private boolean getBooleanCarrierConfig(int subId, String key) {
        CarrierConfigManager configManager = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
        PersistableBundle b = null;
        if (configManager != null) {
            b = configManager.getConfigForSubId(subId);
        }
        if (b == null || b.get(key) == null) {
            return false;
        }
        return b.getBoolean(key);
    }

    private int getVoWifiServiceState(int phoneId2, int type) {
        if (this.mMapconService == null) {
            return -1;
        }
        try {
            return this.mMapconService.getVoWifiServiceState(phoneId2, type);
        } catch (RemoteException ex) {
            loge("getVoWifiServiceState failed, err = " + ex.toString());
            return -1;
        }
    }

    private int getPhoneIdFromNetworkCapabilities(NetworkCapabilities networkCapabilities) {
        int subId = -1;
        int phoneId2 = -1;
        NetworkSpecifier networkSpecifier = networkCapabilities.getNetworkSpecifier();
        if (networkSpecifier != null) {
            try {
                if (networkSpecifier instanceof StringNetworkSpecifier) {
                    subId = Integer.parseInt(networkSpecifier.toString());
                }
            } catch (NumberFormatException ex) {
                loge("getPhoneIdFromNetworkCapabilities exception, ex = " + ex.toString());
            }
        }
        if (subId != -1) {
            phoneId2 = SubscriptionManager.getPhoneId(subId);
        }
        log("getPhoneIdFromNetworkCapabilities, subId = " + subId + " phoneId:" + phoneId2);
        return phoneId2;
    }

    private boolean isDomainPreferRequest(NetworkRequest request) {
        if (NetworkRequest.Type.REQUEST != request.type || !request.networkCapabilities.hasCapability(0)) {
            return false;
        }
        return true;
    }

    private boolean rebuildNetworkRequestByPrefer(ConnectivityService.NetworkRequestInfo nri, AbstractConnectivityService.DomainPreferType prefer) {
        NetworkRequest clientRequest = new NetworkRequest(nri.request);
        NetworkCapabilities networkCapabilities = nri.request.networkCapabilities;
        NetworkSpecifier networkSpecifier = networkCapabilities.getNetworkSpecifier();
        if (AbstractConnectivityService.DomainPreferType.DOMAIN_ONLY_WIFI == prefer || AbstractConnectivityService.DomainPreferType.DOMAIN_PREFER_WIFI == prefer) {
            networkCapabilities.setNetworkSpecifier(null);
            nri.mPreferType = prefer;
            nri.clientRequest = clientRequest;
            networkCapabilities.addTransportType(1);
            networkCapabilities.removeTransportType(0);
            if (networkSpecifier == null) {
                networkCapabilities.setNetworkSpecifier(new StringNetworkSpecifier(Integer.toString(SubscriptionManager.getDefaultDataSubscriptionId())));
                return true;
            }
            networkCapabilities.setNetworkSpecifier(networkSpecifier);
            return true;
        } else if (AbstractConnectivityService.DomainPreferType.DOMAIN_PREFER_CELLULAR != prefer && AbstractConnectivityService.DomainPreferType.DOMAIN_PREFER_VOLTE != prefer) {
            return false;
        } else {
            networkCapabilities.setNetworkSpecifier(null);
            nri.mPreferType = prefer;
            nri.clientRequest = clientRequest;
            networkCapabilities.addTransportType(0);
            networkCapabilities.removeTransportType(1);
            networkCapabilities.setNetworkSpecifier(networkSpecifier);
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public void handleRegisterNetworkRequest(ConnectivityService.NetworkRequestInfo nri) {
        int phoneId2;
        NetworkCapabilities cap = nri.request.networkCapabilities;
        int domainPrefer = -1;
        if (isWifiMmsUtOn && isMapconOn && isDomainPreferRequest(nri.request)) {
            if (cap.getNetworkSpecifier() != null) {
                phoneId2 = getPhoneIdFromNetworkCapabilities(cap);
            } else {
                phoneId2 = SubscriptionManager.getPhoneId(SubscriptionManager.getDefaultDataSubscriptionId());
            }
            if (phoneId2 != -1 && cap.hasCapability(0) && 1 == getVoWifiServiceState(phoneId2, 1)) {
                domainPrefer = getVoWifiServiceDomain(phoneId2, 0);
            }
            AbstractConnectivityService.DomainPreferType prefer = AbstractConnectivityService.DomainPreferType.fromInt(domainPrefer);
            if (prefer == null || !rebuildNetworkRequestByPrefer(nri, prefer)) {
                StringBuilder sb = new StringBuilder();
                sb.append("request(");
                sb.append(nri.request);
                sb.append(") domainPrefer = ");
                sb.append(prefer != null ? prefer : "null");
                log(sb.toString());
            } else {
                log("Update request(" + nri.clientRequest + ") to " + nri.request + " by " + prefer);
                if (AbstractConnectivityService.DomainPreferType.DOMAIN_PREFER_WIFI == prefer || AbstractConnectivityService.DomainPreferType.DOMAIN_PREFER_CELLULAR == prefer || AbstractConnectivityService.DomainPreferType.DOMAIN_PREFER_VOLTE == prefer) {
                    this.mDomainPreferHandler.sendMessageDelayed(this.mDomainPreferHandler.obtainMessage(2, prefer.value(), 0, nri.request), MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
                }
            }
        }
        HwConnectivityService.super.handleRegisterNetworkRequest(nri);
    }

    /* access modifiers changed from: protected */
    public void handleReleaseNetworkRequest(NetworkRequest request, int callingUid) {
        NetworkRequest req = request;
        if (isWifiMmsUtOn && isMapconOn && isDomainPreferRequest(request)) {
            ConnectivityService.NetworkRequestInfo nri = findExistingNetworkRequestInfo(request.requestId);
            if (nri != null && !nri.request.equals(request)) {
                if (nri.clientRequest == null || !nri.clientRequest.equals(request)) {
                    loge("BUG: Do not find request in mNetworkRequests for preferRequest:" + request);
                } else {
                    req = nri.request;
                }
            }
        }
        HwConnectivityService.super.handleReleaseNetworkRequest(req, callingUid);
    }

    /* access modifiers changed from: protected */
    public void handleRemoveNetworkRequest(ConnectivityService.NetworkRequestInfo nri, int whichCallback) {
        if (isWifiMmsUtOn && isMapconOn && isDomainPreferRequest(nri.request)) {
            this.mDomainPreferHandler.removeMessages(2, nri.request);
        }
        HwConnectivityService.super.handleRemoveNetworkRequest(nri, whichCallback);
    }

    /* access modifiers changed from: protected */
    public void notifyNetworkAvailable(NetworkAgentInfo nai, ConnectivityService.NetworkRequestInfo nri) {
        if (isWifiMmsUtOn && isMapconOn && nri != null && isDomainPreferRequest(nri.request)) {
            this.mDomainPreferHandler.sendMessageAtFrontOfQueue(Message.obtain(this.mDomainPreferHandler, 0, nri.request));
        }
        HwConnectivityService.super.notifyNetworkAvailable(nai, nri);
    }

    /* access modifiers changed from: private */
    public ConnectivityService.NetworkRequestInfo findExistingNetworkRequestInfo(int requestId) {
        for (Map.Entry<NetworkRequest, ConnectivityService.NetworkRequestInfo> entry : this.mNetworkRequests.entrySet()) {
            if (entry.getValue().request.requestId == requestId) {
                return entry.getValue();
            }
        }
        return null;
    }

    public void sendNetworkStickyBroadcastAsUser(String action, NetworkAgentInfo na) {
        if (isMatchedOperator()) {
            String intfName = "lo";
            if (!(na == null || na.linkProperties == null)) {
                intfName = na.linkProperties.getInterfaceName();
            }
            log("sendNetworkStickyBroadcastAsUser " + action + "-->" + intfName);
            Intent intent = new Intent(HW_CONNECTIVITY_ACTION);
            intent.putExtra("actions", action);
            intent.putExtra("intfName", intfName);
            intent.putExtra(HwCertification.KEY_DATE_FROM, "ConnectivityService");
            try {
                this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
            } catch (Exception e) {
                log("sendNetworkStickyBroadcastAsUser failed" + e.getMessage());
            }
        }
    }

    private boolean isMatchedOperator() {
        String iccid = "" + ((TelephonyManager) this.mContext.getSystemService("phone")).getSimSerialNumber();
        if (!HW_SIM_ACTIVATION || !iccid.startsWith(VERIZON_ICCID_PREFIX)) {
            return false;
        }
        return true;
    }

    public boolean turnOffVpn(String packageName, int userId) {
        if (userId != UserHandle.getCallingUserId()) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL", "ConnectivityService");
        }
        this.mContext.enforceCallingOrSelfPermission(MDM_VPN_PERMISSION, "NEED MDM_VPN PERMISSION");
        SparseArray<Vpn> mVpns = getmVpns();
        synchronized (mVpns) {
            Vpn vpn = mVpns.get(userId);
            if (vpn == null) {
                return false;
            }
            boolean turnOffAllVpn = vpn.turnOffAllVpn(packageName);
            return turnOffAllVpn;
        }
    }

    /* access modifiers changed from: private */
    public void processWhenSimStateChange(Intent intent) {
        if (!TelephonyManager.getDefault().isMultiSimEnabled() && !this.isAlreadyPop && AwareJobSchedulerConstants.SIM_STATUS_READY.equals(intent.getStringExtra("ss"))) {
            this.mIsSimReady = true;
            connectivityServiceUtils.getContext(this).sendBroadcast(new Intent(DISABEL_DATA_SERVICE_ACTION));
            HwTelephonyManagerInner.getDefault().setDataEnabledWithoutPromp(false);
        }
    }

    /* access modifiers changed from: private */
    public boolean isSetupWizardCompleted() {
        return 1 == Settings.Secure.getInt(this.mContext.getContentResolver(), "user_setup_complete", 0) && 1 == Settings.Secure.getInt(this.mContext.getContentResolver(), "device_provisioned", 0);
    }

    private static String getCaptivePortalUserAgent(Context context) {
        return getGlobalSetting(context, "captive_portal_user_agent", DEFAULT_USER_AGENT);
    }

    private static String getGlobalSetting(Context context, String symbol, String defaultValue) {
        String value = Settings.Global.getString(context.getContentResolver(), symbol);
        return value != null ? value : defaultValue;
    }

    private void registerVerizonWifiDisconnectedReceiver(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.wifi.STATE_CHANGE");
        context.registerReceiver(this.mVerizonWifiDisconnectReceiver, filter);
    }

    /* access modifiers changed from: protected */
    public void updateDefaultNetworkRouting(NetworkAgentInfo oldDefaultNet, NetworkAgentInfo newDefaultNet) {
        if (newDefaultNet == null || newDefaultNet.linkProperties == null || newDefaultNet.networkInfo == null) {
            log("invalid new defautl net");
            return;
        }
        if (!(oldDefaultNet == null || oldDefaultNet.linkProperties == null || oldDefaultNet.networkInfo == null || oldDefaultNet.network == null || oldDefaultNet.networkInfo.getType() != 0 || 1 != newDefaultNet.networkInfo.getType())) {
            String oldIface = oldDefaultNet.linkProperties.getInterfaceName();
            if (oldIface != null) {
                try {
                    log("Remove oldIface " + oldIface + " from network " + oldDefaultNet.network.netId);
                    this.mNetd.removeInterfaceFromNetwork(oldIface, oldDefaultNet.network.netId);
                    log("Clear oldIface " + oldIface + " addresses");
                    this.mNetd.clearInterfaceAddresses(oldIface);
                    log("recovery oldIface " + oldIface + " addresses, immediately");
                    for (LinkAddress oldAddr : oldDefaultNet.linkProperties.getLinkAddresses()) {
                        String oldAddrString = oldAddr.getAddress().getHostAddress();
                        try {
                            log("add addr_x:  to interface: " + oldIface);
                            this.mNetd.getNetdService().interfaceAddAddress(oldIface, oldAddrString, oldAddr.getPrefixLength());
                        } catch (RemoteException | ServiceSpecificException e) {
                            loge("Failed to add addr : " + e);
                        } catch (Exception e2) {
                            loge("Failed to add addr : " + e2);
                        }
                    }
                    log("refresh linkproperties for recovery oldIface");
                    updateLinkPropertiesEx(oldDefaultNet, null);
                } catch (Exception e3) {
                    loge("Exception clearing interface: " + e3);
                }
            }
        }
    }

    public void recordPrivateDnsEvent(Context context, int returnCode, int latencyMs, int netId) {
        NetworkAgentInfo nai = getNetworkAgentInfoForNetId(netId);
        if (nai != null) {
            LinkProperties activeLp = nai.linkProperties;
            if (activeLp == null) {
                return;
            }
            if (!activeLp.isPrivateDnsActive()) {
                updateBypassPrivateDnsState(this.mBypassPrivateDnsNetwork.get(Integer.valueOf(netId)));
            } else if (isPrivateDnsAutoMode()) {
                countPrivateDnsResponseDelay(returnCode, latencyMs, netId, activeLp);
            }
        }
    }

    private void countPrivateDnsResponseDelay(int returnCode, int latencyMs, int netId, LinkProperties lp) {
        BypassPrivateDnsInfo privateDnsInfo = this.mBypassPrivateDnsNetwork.get(Integer.valueOf(netId));
        if (privateDnsInfo != null) {
            privateDnsInfo.updateDelayCount(returnCode, latencyMs);
            updateBypassPrivateDnsState(privateDnsInfo);
            return;
        }
        String assignedServers = Arrays.toString(NetworkUtils.makeStrings(lp.getDnsServers()));
        NetworkAgentInfo nai = getNetworkAgentInfoForNetId(netId);
        if (nai != null) {
            BypassPrivateDnsInfo newPrivateDnsInfo = new BypassPrivateDnsInfo(this.mContext, nai.networkInfo.getType(), assignedServers);
            synchronized (this.mLock) {
                this.mBypassPrivateDnsNetwork.put(Integer.valueOf(netId), newPrivateDnsInfo);
            }
            newPrivateDnsInfo.sendIntentPrivateDnsEvent();
            log(" countPrivateDnsResponseDelay netId " + netId + ", assignedServers " + assignedServers + " NetworkType = " + nai.networkInfo.getType());
        }
    }

    private void updateBypassPrivateDnsState(BypassPrivateDnsInfo privateDnsInfo) {
        if (privateDnsInfo != null && privateDnsInfo.isNeedUpdatePrivateDnsSettings()) {
            updatePrivateDnsSettings();
        }
    }

    private boolean isPrivateDnsAutoMode() {
        DnsManager dnsManager = getDnsManager();
        boolean z = false;
        if (dnsManager != null) {
            DnsManager.PrivateDnsConfig cfg = dnsManager.getPrivateDnsConfig();
            if (cfg != null) {
                if (!cfg.useTls || cfg.inStrictMode()) {
                }
                if (cfg.useTls && !cfg.inStrictMode()) {
                    z = true;
                }
                return z;
            }
        }
        return false;
    }

    public void clearInvalidPrivateDnsNetworkInfo() {
        synchronized (this.mLock) {
            Iterator<Integer> it = this.mBypassPrivateDnsNetwork.keySet().iterator();
            while (it.hasNext()) {
                int networkId = it.next().intValue();
                boolean invalidNetId = true;
                Network[] allNetworks = getAllNetworks();
                int length = allNetworks.length;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        break;
                    } else if (allNetworks[i].netId == networkId) {
                        invalidNetId = false;
                        break;
                    } else {
                        i++;
                    }
                }
                log(" clearInvalidPrivateDnsNetworkInfo networkId " + networkId + " , invalidNetId " + invalidNetId);
                if (invalidNetId) {
                    it.remove();
                }
            }
        }
    }

    public boolean isBypassPrivateDns(int netId) {
        BypassPrivateDnsInfo privateDnsInfo = this.mBypassPrivateDnsNetwork.get(Integer.valueOf(netId));
        if (privateDnsInfo != null) {
            log("isBypassPrivateDns netId: " + netId + ", mBypassPrivateDns: " + privateDnsInfo.mBypassPrivateDns);
            return privateDnsInfo.mBypassPrivateDns;
        }
        log("isBypassPrivateDns return false");
        return false;
    }

    /* JADX WARNING: type inference failed for: r5v0, types: [com.android.server.HwConnectivityService$12, android.os.IBinder] */
    private void startCaptivePortalWebView(Context context, URL url) {
        Network network = getNetworkForTypeWifi();
        if (network == null) {
            log("WebView network null");
            return;
        }
        String captivePortalUserAgent = getCaptivePortalUserAgent(context);
        Intent intentPortal = new Intent("android.net.conn.CAPTIVE_PORTAL");
        intentPortal.putExtra("android.net.extra.NETWORK", network);
        intentPortal.putExtra("android.net.extra.CAPTIVE_PORTAL", new CaptivePortal(new ICaptivePortal.Stub() {
            public void appResponse(int response) {
            }
        }));
        intentPortal.putExtra("android.net.extra.CAPTIVE_PORTAL_URL", url.toString());
        intentPortal.putExtra("android.net.extra.CAPTIVE_PORTAL_USER_AGENT", captivePortalUserAgent);
        intentPortal.setFlags(272629760);
        intentPortal.putExtra(FLAG_SETUP_WIZARD, !isSetupWizardCompleted());
        context.startActivity(intentPortal);
    }
}
