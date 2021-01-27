package com.android.server;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.common.HwFrameworkFactory;
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
import android.net.INetd;
import android.net.INetworkPolicyManager;
import android.net.INetworkStatsService;
import android.net.INetworkStatsSession;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkAgent;
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
import android.net.shared.PrivateDnsConfig;
import android.net.util.NetdService;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
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
import android.rms.HwSysResManager;
import android.telephony.CarrierConfigManager;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.HwVSimManager;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.LocalLog;
import android.util.Slog;
import android.util.SparseArray;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;
import com.android.internal.telephony.HwModemCapability;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.util.FunctionalUtils;
import com.android.server.AbstractConnectivityService;
import com.android.server.ConnectivityService;
import com.android.server.GcmFixer.HeartbeatReceiver;
import com.android.server.GcmFixer.NetworkStateReceiver;
import com.android.server.appactcontrol.AppActConstant;
import com.android.server.connectivity.DnsManager;
import com.android.server.connectivity.NetworkAgentInfo;
import com.android.server.connectivity.Vpn;
import com.android.server.hidata.arbitration.HwArbitrationDefs;
import com.android.server.hidata.mplink.HwMpLinkContentAware;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.intellicom.common.SmartDualCardConsts;
import com.android.server.intellicom.networkslice.HwNetworkSliceManager;
import com.android.server.intellicom.networkslice.css.NetworkSlicesHandler;
import com.android.server.intellicom.smartnet.HwFixRouteRecognition;
import com.android.server.location.HwMultiNlpPolicy;
import com.android.server.pm.auth.HwCertification;
import com.android.server.swing.HwSwingMotionGestureConstant;
import com.android.server.wifipro.WifiProCommonUtils;
import com.android.server.wm.HwSplitBarConstants;
import com.hisi.mapcon.IMapconService;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.deliver.info.HwDeliverInfo;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import com.huawei.utils.reflect.EasyInvokeFactory;
import huawei.HwFeatureConfig;
import huawei.android.telephony.wrapper.WrapperFactory;
import huawei.cust.HwCfgFilePolicy;
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
    private static final String CHECK_PERMISSION_MSG = "ConnectivityService";
    private static final int CONNECTIVITY_SERVICE_NEED_SET_USER_DATA = 1100;
    private static final String DEFAULT_HTTP_URL = "http://connectivitycheck.platform.hicloud.com/generate_204";
    public static final int DEFAULT_PHONE_ID = 0;
    private static final String DEFAULT_PRIVATE_DNS_CONFIG = "1,4,4,6,8,8,10,60";
    private static final String DEFAULT_SERVER = "connectivitycheck.gstatic.com";
    private static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.82 Safari/537.36";
    private static final String DESCPRIPTOR = "android.net.IConnectivityManager";
    private static final int DEVICE_NOT_PROVISIONED = 0;
    private static final int DEVICE_PROVISIONED = 1;
    public static final String DISABEL_DATA_SERVICE_ACTION = "android.net.conn.DISABEL_DATA_SERVICE_ACTION";
    private static final String DISABLE_PORTAL_CHECK = "disable_portal_check";
    private static final int DNS_SUCCESS = 0;
    private static String ENABLE_NOT_REMIND_FUNCTION = "enable_not_remind_function";
    public static final String EXTRA_IS_LTE_MOBILE_DATA_STATUS = "lte_mobile_data_status";
    private static final String FEATURE_ENABLE_MMS_SUB1 = "enableMMS_sub1";
    private static final String FEATURE_ENABLE_MMS_SUB2 = "enableMMS_sub2";
    public static final String FLAG_SETUP_WIZARD = "flag_setup_wizard";
    private static final String HW_CONNECTIVITY_ACTION = "huawei.net.conn.HW_CONNECTIVITY_CHANGE";
    private static final boolean HW_SIM_ACTIVATION = SystemProperties.getBoolean("ro.config.hw_sim_activation", false);
    private static final boolean IS_ALWAYS_ALLOW_MMS = SystemProperties.getBoolean("ro.config.hw_always_allow_mms", false);
    private static final boolean IS_CHINA = "CN".equalsIgnoreCase(SystemProperties.get(WifiProCommonUtils.KEY_PROP_LOCALE, ""));
    private static final boolean IS_CHINA_TELECOM = ("92".equals(SystemPropertiesEx.get("ro.config.hw_opta", "0")) && "156".equals(SystemPropertiesEx.get("ro.config.hw_optb", "0")));
    private static final boolean IS_MAPCON_ON = SystemProperties.getBoolean("ro.vendor.config.hw_vowifi", false);
    private static final boolean IS_TV = "tv".equals(SystemProperties.get("ro.build.characteristics", AppActConstant.VALUE_DEFAULT));
    private static final boolean IS_VERIZON = ("389".equals(SystemProperties.get("ro.config.hw_opta")) && "840".equals(SystemProperties.get("ro.config.hw_optb")));
    private static final boolean IS_WIFI_MMS_UT_ON = SystemProperties.getBoolean("ro.config.hw_vowifi_mmsut", false);
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
    private static final int SLOT1 = 0;
    private static final int SLOT2 = 1;
    private static final String SYSTEM_MANAGER_PKG_NAME = "com.huawei.systemmanager";
    private static final String TAG = "HwConnectivityService";
    private static final int USER_SETUP_COMPLETE = 1;
    private static final int USER_SETUP_NOT_COMPLETE = 0;
    private static String VALUE_DISABLE_NOT_REMIND_FUNCTION = AppActConstant.VALUE_FALSE;
    private static String VALUE_ENABLE_NOT_REMIND_FUNCTION = AppActConstant.VALUE_TRUE;
    private static int VALUE_NOT_SHOW_PDP = 0;
    private static int VALUE_SHOW_PDP = 1;
    private static final String VALUE_SIM_CHANGE_ALERT_DATA_CONNECT = "0";
    private static final String VERIZON_ICCID_PREFIX = "891480";
    private static String WHETHER_SHOW_PDP_WARNING = "whether_show_pdp_warning";
    private static final String WIFI_AP_MANUAL_CONNECT = "wifi_ap_manual_connect";
    public static final int WIFI_PULS_CSP_DISENABLED = 1;
    public static final int WIFI_PULS_CSP_ENABLED = 0;
    private static ConnectivityServiceUtils connectivityServiceUtils = EasyInvokeFactory.getInvokeUtils(ConnectivityServiceUtils.class);
    private static INetworkStatsService sStatsService;
    private int curMmsDataSub = -1;
    private int curPrefDataSubscription = -1;
    private boolean isAlreadyPop = false;
    private boolean isConnected = false;
    private ActivityManager mActivityManager;
    private HashMap<Integer, BypassPrivateDnsInfo> mBypassPrivateDnsNetwork = new HashMap<>();
    private Context mContext;
    private int mCurPrefDataSubscription = -1;
    private AlertDialog mDataServiceToPdpDialog = null;
    private int mDefaultDataSlotId = 0;
    private DomainPreferHandler mDomainPreferHandler = null;
    private NetworkStateReceiver mGcmFixIntentReceiver = new NetworkStateReceiver();
    private Handler mHandler;
    private HeartbeatReceiver mHeartbeatReceiver = new HeartbeatReceiver();
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        /* class com.android.server.HwConnectivityService.AnonymousClass4 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            HwConnectivityService.log("mIntentReceiver begin");
            if (intent != null) {
                String action = intent.getAction();
                if (HwConnectivityService.ACTION_OF_ANDROID_BOOT_COMPLETED.equals(action)) {
                    HwConnectivityService.log("receive Intent.ACTION_BOOT_COMPLETED!");
                    HwConnectivityService.this.mIsSendWifiBroadcastAfterBootCompleted = true;
                } else if (HwConnectivityService.ACTION_BT_CONNECTION_CHANGED.equals(action)) {
                    HwConnectivityService.log("receive ACTION_BT_CONNECTION_CHANGED");
                    if (intent.getIntExtra("android.bluetooth.profile.extra.STATE", -1) == 0) {
                        HwConnectivityService.this.mIsBlueThConnected = false;
                    }
                } else {
                    HwConnectivityService.this.mRegisteredPushPkg.updateStatus(intent);
                }
            }
        }
    };
    private boolean mIsBlueThConnected = false;
    private boolean mIsRemindService = SystemProperties.getBoolean("ro.config.DataPopFirstBoot", false);
    private boolean mIsSendWifiBroadcastAfterBootCompleted = false;
    private boolean mIsShowDlgEndCall = false;
    private boolean mIsShowDlgTurnOfDc = true;
    private boolean mIsShowWarningRoamingToPdp = false;
    private boolean mIsSimReady = false;
    private boolean mIsSimStateChanged = false;
    private boolean mIsTopAppHsbb = false;
    private boolean mIsTopAppSkytone = false;
    private boolean mIsWifiConnected = false;
    private final Object mLock = new Object();
    private int mLteMobileDataState = 3;
    private BroadcastReceiver mMapconIntentReceiver = new BroadcastReceiver() {
        /* class com.android.server.HwConnectivityService.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent mapconIntent) {
            if (mapconIntent == null) {
                HwConnectivityService.log("intent is null");
                return;
            }
            String action = mapconIntent.getAction();
            HwConnectivityService.log("onReceive: action=" + action);
            if (HwConnectivityService.MAPCON_START_INTENT.equals(action)) {
                ServiceConnection mConnection = new ServiceConnection() {
                    /* class com.android.server.HwConnectivityService.AnonymousClass1.AnonymousClass1 */

                    @Override // android.content.ServiceConnection
                    public void onServiceConnected(ComponentName className, IBinder service) {
                        HwConnectivityService.this.mMapconService = IMapconService.Stub.asInterface(service);
                    }

                    @Override // android.content.ServiceConnection
                    public void onServiceDisconnected(ComponentName className) {
                        HwConnectivityService.this.mMapconService = null;
                    }
                };
                HwConnectivityService.this.mContext.bindServiceAsUser(new Intent().setClassName(HwMultiNlpPolicy.GLOBAL_NLP_CLIENT_PKG, "com.hisi.mapcon.MapconService"), mConnection, 1, UserHandle.OWNER);
            } else if (!HwConnectivityService.ACTION_MAPCON_SERVICE_FAILED.equals(action) || mapconIntent.getIntExtra("serviceType", 2) != 0) {
                HwConnectivityService.loge("MAPCON_START_INTENT not equals action");
            } else {
                int requestId = mapconIntent.getIntExtra("request-id", -1);
                HwConnectivityService.loge("Recive ACTION_MAPCON_SERVICE_FAILED, requestId = " + requestId);
                if (requestId > 0) {
                    HwConnectivityService.this.mDomainPreferHandler.sendMessageAtFrontOfQueue(HwConnectivityService.this.mDomainPreferHandler.obtainMessage(1, requestId, 0));
                }
            }
        }
    };
    private IMapconService mMapconService;
    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        /* class com.android.server.HwConnectivityService.AnonymousClass2 */

        @Override // android.telephony.PhoneStateListener
        public void onCallStateChanged(int state, String incomingNumber) {
            HwConnectivityService.this.updateCallState(state);
        }

        @Override // android.telephony.PhoneStateListener
        public void onServiceStateChanged(ServiceState state) {
            boolean isConnect;
            if (state != null && !TextUtils.isEmpty(HwConnectivityService.this.mSimChangeAlertDataConnect) && !HwConnectivityService.this.isAlreadyPop) {
                HwConnectivityService.log("onServiceStateChanged:" + state);
                int voiceRegState = state.getVoiceRegState();
                if (voiceRegState == 1 || voiceRegState == 2) {
                    isConnect = state.getDataRegState() == 0;
                } else if (voiceRegState != 3) {
                    isConnect = true;
                } else {
                    isConnect = false;
                }
                if (state.getRoaming()) {
                    HwTelephonyManagerInner.getDefault().setDataRoamingEnabledWithoutPromp(false);
                    HwConnectivityService.this.mIsShowWarningRoamingToPdp = true;
                }
                if (isConnect && HwConnectivityService.this.isSetupWizardCompleted() && HwConnectivityService.this.mIsSimReady) {
                    HwConnectivityService.this.mHandler.sendEmptyMessage(0);
                    HwConnectivityService.this.isAlreadyPop = true;
                }
            }
        }
    };
    private RegisteredPushPkg mRegisteredPushPkg = new RegisteredPushPkg();
    private String mServer;
    private String mSimChangeAlertDataConnect = null;
    private BroadcastReceiver mSimStateReceiver = new BroadcastReceiver() {
        /* class com.android.server.HwConnectivityService.AnonymousClass5 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (SmartDualCardConsts.SYSTEM_STATE_NAME_SIM_STATE_CHANGED.equals(action)) {
                    HwConnectivityService.log("receive ACTION_SIM_STATE_CHANGED");
                    HwConnectivityService.this.mIsSimStateChanged = true;
                    if (!TextUtils.isEmpty(HwConnectivityService.this.mSimChangeAlertDataConnect)) {
                        HwConnectivityService.this.processWhenSimStateChange(intent);
                    }
                    int slotId = intent.getIntExtra("phone", -1000);
                    if ("LOADED".equals((String) intent.getExtra("ss", "UNKNOWN")) && SubscriptionManager.isValidSlotIndex(slotId) && HwConnectivityService.this.mDefaultDataSlotId == slotId) {
                        HwConnectivityService.this.updateMobileDataAlwaysOnCust(slotId);
                    }
                } else if (SmartDualCardConsts.SYSTEM_STATE_NAME_DEFAULT_DATA_SUBSCRIPTION_CHANGED.equals(action)) {
                    int curDataSlotId = SubscriptionManager.getSlotIndex(SubscriptionManager.getDefaultDataSubscriptionId());
                    if (HwConnectivityService.this.mDefaultDataSlotId != curDataSlotId && SubscriptionManager.isValidSlotIndex(curDataSlotId)) {
                        HwConnectivityService.this.mDefaultDataSlotId = curDataSlotId;
                        HwConnectivityService.this.updateMobileDataAlwaysOnCust(curDataSlotId);
                    }
                } else {
                    HwConnectivityService.log("Enter SimStateReceiver else, do nothing.");
                }
            }
        }
    };
    private BroadcastReceiver mTetheringReceiver = new BroadcastReceiver() {
        /* class com.android.server.HwConnectivityService.AnonymousClass3 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && "android.hardware.usb.action.USB_STATE".equals(action)) {
                boolean isUsbConnected = intent.getBooleanExtra("connected", false);
                boolean isRndEnabled = intent.getBooleanExtra("rndis", false);
                int usbTetheringOn = Settings.Secure.getInt(HwConnectivityService.this.mContext.getContentResolver(), "usb_tethering_on", 0);
                HwConnectivityService.log("mTetheringReceiver isUsbConnected = " + isUsbConnected + ",isRndEnabled = " + isRndEnabled + ", usbTetheringOn = " + usbTetheringOn);
                if (usbTetheringOn == 1 && isUsbConnected && !isRndEnabled) {
                    new Thread("sleepThread") {
                        /* class com.android.server.HwConnectivityService.AnonymousClass3.AnonymousClass1 */

                        @Override // java.lang.Thread, java.lang.Runnable
                        public void run() {
                            do {
                                try {
                                    if (HwConnectivityService.this.isSystemBootComplete()) {
                                        Thread.sleep(200);
                                    } else {
                                        Thread.sleep(1000);
                                    }
                                } catch (InterruptedException e) {
                                    HwConnectivityService.loge("wait to boot complete error");
                                }
                            } while (!HwConnectivityService.this.isSystemBootComplete());
                            HwConnectivityService.this.setUsbTethering(true, HwConnectivityService.this.mContext.getOpPackageName());
                        }
                    }.start();
                }
            }
        }
    };
    private BroadcastReceiver mVerizonWifiDisconnectReceiver = new BroadcastReceiver() {
        /* class com.android.server.HwConnectivityService.AnonymousClass11 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            NetworkInfo netInfo;
            if (intent == null || !SmartDualCardConsts.SYSTEM_STATE_NAME_WIFI_NETWORK_STATE_CHANGED.equals(intent.getAction()) || (netInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo")) == null || netInfo.getType() != 1) {
                return;
            }
            if (netInfo.getState() == NetworkInfo.State.CONNECTED) {
                HwConnectivityService.this.isConnected = true;
            } else if (netInfo.getState() == NetworkInfo.State.DISCONNECTED && netInfo.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED && HwConnectivityService.this.isConnected) {
                Toast.makeText(context, 33686310, 1).show();
                HwConnectivityService.this.isConnected = false;
            } else {
                HwConnectivityService.log("no match netInfo state.");
            }
        }
    };
    private WifiDisconnectManager mWifiDisconnectManager = new WifiDisconnectManager();
    private int phoneId = -1;

    public HwConnectivityService(Context context, INetworkManagementService netd, INetworkStatsService statsService, INetworkPolicyManager policyManager) {
        super(context, netd, statsService, policyManager);
        this.mContext = context;
        this.mSimChangeAlertDataConnect = Settings.System.getString(context.getContentResolver(), "hw_sim_change_alert_data_connect");
        this.mRegisteredPushPkg.init(context);
        registerSimStateReceiver(context);
        if (IS_VERIZON) {
            registerVerizonWifiDisconnectedReceiver(context);
        }
        this.mWifiDisconnectManager.registerReceiver();
        registerPhoneStateListener(context);
        registerBootStateListener(context);
        this.mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
        this.mHandler = new HwConnectivityServiceHandler(this.mHandlerThread.getLooper());
        this.mDomainPreferHandler = new DomainPreferHandler(this.mHandlerThread.getLooper());
        if (SystemProperties.getBoolean("ro.vendor.config.hw_vowifi", false)) {
            registerMapconIntentReceiver(context);
        }
        this.mServer = Settings.Global.getString(context.getContentResolver(), "captive_portal_server");
        if (TextUtils.isEmpty(this.mServer) || this.mServer.startsWith("http")) {
            this.mServer = DEFAULT_SERVER;
        }
        SystemProperties.set("sys.defaultapn.enabled", AppActConstant.VALUE_TRUE);
        registerTetheringReceiver(context);
        initGCMFixer(context);
        this.mDefaultDataSlotId = SubscriptionManager.getSlotIndex(SubscriptionManager.getDefaultDataSubscriptionId());
        if (HwFrameworkFactory.getHwInnerTelephonyManager().isNrSlicesSupported()) {
            HwNetworkSliceManager.getInstance().init(this.mContext, this.mHandlerThread.getLooper());
        }
        if (SystemPropertiesEx.getBoolean("persist.smartnet.on", false)) {
            new HwFixRouteRecognition(this.mContext);
        }
    }

    /* access modifiers changed from: private */
    public static void log(String s) {
        Slog.i(TAG, s);
    }

    /* access modifiers changed from: private */
    public static void loge(String s) {
        Slog.e(TAG, s);
    }

    private void initGCMFixer(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_OF_ANDROID_BOOT_COMPLETED);
        filter.addAction(SmartDualCardConsts.ACTION_CONNECTIVITY_CHANGE);
        this.mContext.registerReceiver(this.mGcmFixIntentReceiver, filter);
        IntentFilter filter2 = new IntentFilter();
        filter2.addAction(HeartbeatReceiver.HEARTBEAT_FIXER_ACTION);
        this.mContext.registerReceiver(this.mHeartbeatReceiver, filter2, "android.permission.CONNECTIVITY_INTERNAL", null);
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0030: APUT  
      (r0v2 'result' java.lang.String[] A[D('result' java.lang.String[])])
      (0 ??[int, short, byte, char])
      (r5v1 'str' java.lang.String A[D('str' java.lang.String)])
     */
    private String[] getFeature(String str) {
        if (str != null) {
            String[] result = new String[2];
            int subId = 0;
            if (WrapperFactory.getMSimTelephonyManagerWrapper().isMultiSimEnabled()) {
                subId = WrapperFactory.getMSimTelephonyManagerWrapper().getPreferredDataSubscription();
                if (str.equals(FEATURE_ENABLE_MMS_SUB2)) {
                    str = "enableMMS";
                    subId = 1;
                } else if (FEATURE_ENABLE_MMS_SUB1.equals(str)) {
                    str = "enableMMS";
                    subId = 0;
                }
            }
            result[0] = str;
            result[1] = String.valueOf(subId);
            log("getFeature: return feature=" + str + " subId=" + subId);
            return result;
        }
        throw new IllegalArgumentException("getFeature() received null string");
    }

    /* access modifiers changed from: protected */
    public String getMmsFeature(String feature) {
        log("getMmsFeature HwFeatureConfig.dual_card_mms_switch" + HwFeatureConfig.dual_card_mms_switch);
        try {
            if (!HwFeatureConfig.dual_card_mms_switch) {
                return feature;
            }
            String[] result = getFeature(feature);
            String feature2 = result[0];
            this.phoneId = Integer.parseInt(result[1]);
            this.curMmsDataSub = -1;
            return feature2;
        } catch (NumberFormatException e) {
            log("get phoneId fail");
            return feature;
        }
    }

    /* access modifiers changed from: protected */
    public boolean isAlwaysAllowMMSforRoaming(int networkType, String feature) {
        if (networkType == 0) {
            boolean z = IS_ALWAYS_ALLOW_MMS;
            if (IS_CHINA_TELECOM) {
                boolean isRoaming = WrapperFactory.getMSimTelephonyManagerWrapper().isNetworkRoaming(this.phoneId);
                if (!z || !isRoaming) {
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
        this.mCurPrefDataSubscription = WrapperFactory.getMSimTelephonyManagerWrapper().getPreferredDataSubscription();
        this.curMmsDataSub = WrapperFactory.getMSimTelephonyManagerWrapper().getMmsAutoSetDataSubscription();
        if (!"enableMMS".equals(feature) || networkType != 0) {
            return false;
        }
        int i = this.curMmsDataSub;
        if ((i != 0 && i != 1) || this.phoneId == this.curMmsDataSub) {
            return false;
        }
        log("DSMMS dds is switching now, do not response request from another card, curMmsDataSub: " + this.curMmsDataSub);
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean isMmsSubDiffFromDataSub(int networkType, String feature) {
        if (!HwFeatureConfig.dual_card_mms_switch || !"enableMMS".equals(feature) || networkType != 0 || this.mCurPrefDataSubscription == this.phoneId) {
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
    public boolean isNeedTearMmsAndRestoreData(int networkType, String feature, Handler handler) {
        if (!HwFeatureConfig.dual_card_mms_switch || networkType != 0 || !"enableMMS".equals(feature) || !WrapperFactory.getMSimTelephonyManagerWrapper().isMultiSimEnabled()) {
            return true;
        }
        int curMmsData = WrapperFactory.getMSimTelephonyManagerWrapper().getMmsAutoSetDataSubscription();
        int lastPrefDataSubscription = 1;
        if (curMmsData != 0 && curMmsData != 1) {
            return true;
        }
        if (curMmsData != 0) {
            lastPrefDataSubscription = 0;
        }
        int curMsPrefDataSubscription = WrapperFactory.getMSimTelephonyManagerWrapper().getPreferredDataSubscription();
        log("isNeedTearDataAndRestoreData lastPrefDataSubscription" + lastPrefDataSubscription + "curMsPrefDataSubscription" + curMsPrefDataSubscription);
        if (lastPrefDataSubscription != curMsPrefDataSubscription) {
            log("DSMMS >>>> disable a connection, after MMS net disconnected will switch back to phone " + lastPrefDataSubscription);
            WrapperFactory.getMSimTelephonyManagerWrapper().setPreferredDataSubscription(lastPrefDataSubscription);
        } else {
            log("DSMMS unexpected case, data subscription is already on " + curMsPrefDataSubscription);
        }
        WrapperFactory.getMSimTelephonyManagerWrapper().setMmsAutoSetDataSubscription(-1);
        return true;
    }

    /* access modifiers changed from: private */
    public class WifiDisconnectManager {
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
        private boolean mIsDialogHasShown;
        NetworkInfo.State mLastWifiState;
        private BroadcastReceiver mNetworkSwitchReceiver;
        private boolean mRemindWifiToPdp;
        private boolean mShouldShowDialogWhenConnectFailed;
        private boolean mShouldStartMobile;
        private DialogInterface.OnDismissListener mSwitchPdpListener;
        protected AlertDialog mWifiToPdpDialog;

        private WifiDisconnectManager() {
            this.mWifiToPdpDialog = null;
            this.mLastWifiState = NetworkInfo.State.DISCONNECTED;
            this.mRemindWifiToPdp = false;
            this.mNetworkSwitchReceiver = new BroadcastReceiver() {
                /* class com.android.server.HwConnectivityService.WifiDisconnectManager.AnonymousClass1 */

                @Override // android.content.BroadcastReceiver
                public void onReceive(Context context, Intent intent) {
                    if (intent == null || !WifiDisconnectManager.ACTION_SWITCH_TO_MOBILE_NETWORK.equals(intent.getAction())) {
                        return;
                    }
                    if (intent.getBooleanExtra(WifiDisconnectManager.SWITCH_STATE, true)) {
                        HwConnectivityService.this.mWifiDisconnectManager.switchToMobileNetwork();
                    } else {
                        HwConnectivityService.this.mWifiDisconnectManager.cancelSwitchToMobileNetwork();
                    }
                }
            };
            this.mSwitchPdpListener = new DialogInterface.OnDismissListener() {
                /* class com.android.server.HwConnectivityService.WifiDisconnectManager.AnonymousClass2 */

                @Override // android.content.DialogInterface.OnDismissListener
                public void onDismiss(DialogInterface dialog) {
                    HwTelephonyFactory.getHwDataServiceChrManager().sendMonitorWifiSwitchToMobileMessage((int) HwSplitBarConstants.DARK_MODE_DELAY);
                    if (WifiDisconnectManager.this.mShouldStartMobile) {
                        HwConnectivityService.this.setMobileDataEnabled(HwConnectivityService.MODULE_WIFI, true);
                        HwConnectivityService.log("you have restart Mobile data service!");
                    }
                    WifiDisconnectManager.this.mShouldStartMobile = false;
                    WifiDisconnectManager.this.mWifiToPdpDialog = null;
                }
            };
            this.mShouldStartMobile = false;
            this.mShouldShowDialogWhenConnectFailed = true;
            this.mIsDialogHasShown = false;
        }

        private boolean getAirplaneModeEnable() {
            boolean isRetVal = false;
            if (Settings.System.getInt(HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).getContentResolver(), "airplane_mode_on", 0) == 1) {
                isRetVal = true;
            }
            HwConnectivityService.log("getAirplaneModeEnable returning " + isRetVal);
            return isRetVal;
        }

        private AlertDialog createSwitchToPdpWarning() {
            HwConnectivityService.log("create dialog of switch to pdp");
            HwTelephonyFactory.getHwDataServiceChrManager().removeMonitorWifiSwitchToMobileMessage();
            AlertDialog.Builder buider = new AlertDialog.Builder(HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this), 33947691);
            View view = LayoutInflater.from(buider.getContext()).inflate(34013282, (ViewGroup) null);
            final CheckBox checkBox = (CheckBox) view.findViewById(34603157);
            buider.setView(view);
            buider.setTitle(33685520);
            buider.setIcon(17301543);
            buider.setPositiveButton(33685567, new DialogInterface.OnClickListener() {
                /* class com.android.server.HwConnectivityService.WifiDisconnectManager.AnonymousClass3 */

                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialoginterface, int i) {
                    WifiDisconnectManager.this.mShouldStartMobile = true;
                    HwConnectivityService.log("setPositiveButton: mShouldStartMobile set true");
                    WifiDisconnectManager.this.checkUserChoice(checkBox.isChecked(), true);
                }
            });
            buider.setNegativeButton(33685568, new DialogInterface.OnClickListener() {
                /* class com.android.server.HwConnectivityService.WifiDisconnectManager.AnonymousClass4 */

                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialoginterface, int i) {
                    HwConnectivityService.log("you have chose to disconnect Mobile data service!");
                    WifiDisconnectManager.this.mShouldStartMobile = false;
                    WifiDisconnectManager.this.checkUserChoice(checkBox.isChecked(), false);
                }
            });
            AlertDialog dialog = buider.create();
            dialog.setCancelable(false);
            dialog.getWindow().setType(2008);
            return dialog;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void checkUserChoice(boolean isRememberChoice, boolean enableDataConnect) {
            int showPopState;
            if (!isRememberChoice) {
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
            if (ActivityManagerNative.isSystemReady() && HwConnectivityService.this.mIsSendWifiBroadcastAfterBootCompleted) {
                HwConnectivityService.log("notify settings:" + isConnectingOrConnected);
                Intent intent = new Intent(ACTION_WIFI_NETWORK_CONNECTION_CHANGED);
                intent.putExtra(CONNECT_STATE, isConnectingOrConnected);
                HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).sendBroadcast(intent);
            }
        }

        private boolean shouldNotifySettings() {
            return isSwitchToWifiSupported() && Settings.System.getInt(HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).getContentResolver(), SWITCH_TO_WIFI_TYPE, 0) != 0;
        }

        private boolean isSwitchToWifiSupported() {
            return "CMCC".equalsIgnoreCase(SystemProperties.get("ro.config.operators", "")) || HwConnectivityService.this.mCust.isSupportWifiConnectMode(HwConnectivityService.this.mContext);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean shouldNotShowDataIcon() {
            int value = Settings.System.getInt(HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).getContentResolver(), WIFI_TO_PDP, 1);
            if (value == 2) {
                return true;
            }
            if (this.mIsDialogHasShown || value != 0) {
                return false;
            }
            return true;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void switchToMobileNetwork() {
            if (getAirplaneModeEnable()) {
                HwConnectivityService.this.enableDefaultTypeApn(true);
            } else if (!this.mShouldShowDialogWhenConnectFailed || !this.mIsDialogHasShown) {
                int value = Settings.System.getInt(HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).getContentResolver(), WIFI_TO_PDP, 1);
                HwConnectivityService.log("WIFI_TO_PDP value =" + value);
                int wifiplusvalue = Settings.System.getInt(HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).getContentResolver(), "wifi_csp_dispaly_state", 1);
                HwConnectivityService.log("wifiplus_csp_dispaly_state value =" + wifiplusvalue);
                HwVSimManager hwVSimManager = HwVSimManager.getDefault();
                if (hwVSimManager != null && hwVSimManager.isVSimEnabled()) {
                    HwConnectivityService.log("vsim is enabled and following process will execute enableDefaultTypeApn(true), so do nothing that likes value == WIFI_TO_PDP_AUTO");
                } else if (value == 0) {
                    if (wifiplusvalue == 0) {
                        HwConnectivityService.log("wifi_csp_dispaly_state = 0, Don't create WifiToPdpDialog");
                        HwConnectivityService.log("enableDefaultTypeApn(true) in switchToMobileNetwork()  ");
                        HwConnectivityService.this.shouldEnableDefaultApn();
                        return;
                    }
                    HwConnectivityService.this.setMobileDataEnabled(HwConnectivityService.MODULE_WIFI, false);
                    this.mShouldStartMobile = true;
                    this.mIsDialogHasShown = true;
                    this.mWifiToPdpDialog = createSwitchToPdpWarning();
                    this.mWifiToPdpDialog.setOnDismissListener(this.mSwitchPdpListener);
                    this.mWifiToPdpDialog.show();
                } else if (value == 1 || wifiplusvalue != 1) {
                    HwConnectivityService.log("wifi_csp_dispaly_state = 0, Don't setMobileDataEnabled");
                } else {
                    HwConnectivityService.this.setMobileDataEnabled(HwConnectivityService.MODULE_WIFI, false);
                }
                HwConnectivityService.log("enableDefaultTypeApn(true) in switchToMobileNetwork( )");
                HwConnectivityService.this.shouldEnableDefaultApn();
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void cancelSwitchToMobileNetwork() {
            if (this.mWifiToPdpDialog != null) {
                HwConnectivityService.log("cancelSwitchToMobileNetwork and mWifiToPdpDialog is showing");
                this.mShouldStartMobile = true;
                this.mWifiToPdpDialog.dismiss();
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void registerReceiver() {
            this.mRemindWifiToPdp = AppActConstant.VALUE_TRUE.equals(Settings.Global.getString(HwConnectivityService.this.mContext.getContentResolver(), "hw_RemindWifiToPdp"));
            if (this.mRemindWifiToPdp && isSwitchToWifiSupported()) {
                IntentFilter filter = new IntentFilter();
                filter.addAction(ACTION_SWITCH_TO_MOBILE_NETWORK);
                HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).registerReceiver(this.mNetworkSwitchReceiver, filter);
            }
        }

        /* access modifiers changed from: protected */
        public void hintUserSwitchToMobileWhileWifiDisconnected(NetworkInfo.State state, int type) {
            HwConnectivityService.log("hintUserSwitchToMobileWhileWifiDisconnected, state=" + state + "  type =" + type);
            boolean shouldEnableDefaultTypeApn = true;
            if (this.mRemindWifiToPdp) {
                if (state == NetworkInfo.State.DISCONNECTED && type == 1 && HwConnectivityService.this.getMobileDataEnabled()) {
                    if (this.mLastWifiState == NetworkInfo.State.CONNECTED) {
                        int value = Settings.System.getInt(HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).getContentResolver(), WIFI_TO_PDP, 1);
                        HwConnectivityService.log("WIFI_TO_PDP value = " + value);
                        if (value == 1) {
                            HwConnectivityService.this.shouldEnableDefaultApn();
                            return;
                        }
                        this.mShouldShowDialogWhenConnectFailed = true;
                        HwConnectivityService.log("mShouldEnableDefaultTypeAPN was set false");
                        shouldEnableDefaultTypeApn = false;
                    }
                    if (shouldNotifySettings()) {
                        sendWifiBroadcast(false);
                    } else if (!getAirplaneModeEnable()) {
                        HwConnectivityService.this.mHandler.sendMessageDelayed(HwConnectivityService.this.mHandler.obtainMessage(1), HwSwingMotionGestureConstant.HOVER_SCREEN_OFF_THRESHOLD);
                        HwConnectivityService.log("switch message will be send in 5 seconds");
                    } else {
                        shouldEnableDefaultTypeApn = true;
                    }
                    if (this.mLastWifiState == NetworkInfo.State.CONNECTING) {
                        this.mShouldShowDialogWhenConnectFailed = false;
                    }
                } else if ((state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING) && type == 1) {
                    if (state == NetworkInfo.State.CONNECTED) {
                        this.mIsDialogHasShown = false;
                    }
                    if (shouldNotifySettings()) {
                        sendWifiBroadcast(true);
                    } else {
                        checkHandleMessage();
                    }
                    AlertDialog alertDialog = this.mWifiToPdpDialog;
                    if (alertDialog != null) {
                        this.mShouldStartMobile = true;
                        alertDialog.dismiss();
                    }
                } else {
                    HwConnectivityService.log("other state");
                }
                if (type == 1) {
                    HwConnectivityService.log("mLastWifiState =" + this.mLastWifiState);
                    this.mLastWifiState = state;
                }
            }
            if (shouldEnableDefaultTypeApn && state == NetworkInfo.State.DISCONNECTED && type == 1) {
                HwConnectivityService.log("enableDefaultTypeApn(true) in hintUserSwitchToMobileWhileWifiDisconnected");
                HwConnectivityService.this.shouldEnableDefaultApn();
            }
        }

        private void checkHandleMessage() {
            if (HwConnectivityService.this.mHandler.hasMessages(1)) {
                HwConnectivityService.this.mHandler.removeMessages(1);
                HwConnectivityService.log("switch message was removed");
            }
        }

        /* access modifiers changed from: protected */
        public void makeDefaultAndHintUser(NetworkAgentInfo newNetwork) {
        }
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

    private AlertDialog createWarningRoamingToPdp() {
        AlertDialog.Builder buider = new AlertDialog.Builder(connectivityServiceUtils.getContext(this), connectivityServiceUtils.getContext(this).getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null));
        buider.setTitle(33685962);
        buider.setMessage(33685963);
        buider.setIcon(17301543);
        buider.setPositiveButton(17040227, new DialogInterface.OnClickListener() {
            /* class com.android.server.HwConnectivityService.AnonymousClass6 */

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialoginterface, int i) {
                HwTelephonyManagerInner.getDefault().setDataRoamingEnabledWithoutPromp(true);
                Toast.makeText(HwConnectivityService.this.mContext, 33685965, 1).show();
            }
        });
        buider.setNegativeButton(17040226, new DialogInterface.OnClickListener() {
            /* class com.android.server.HwConnectivityService.AnonymousClass7 */

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(HwConnectivityService.this.mContext, 33685966, 1).show();
            }
        });
        AlertDialog dialog = buider.create();
        dialog.getWindow().setType(2008);
        return dialog;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private AlertDialog createWarningToPdp() {
        AlertDialog.Builder buider;
        final String enableNotRemindFunction = SettingsEx.Systemex.getString(connectivityServiceUtils.getContext(this).getContentResolver(), ENABLE_NOT_REMIND_FUNCTION);
        final CheckBox checkBox = null;
        if (VALUE_ENABLE_NOT_REMIND_FUNCTION.equals(enableNotRemindFunction)) {
            int themeId = connectivityServiceUtils.getContext(this).getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null);
            buider = new AlertDialog.Builder(new ContextThemeWrapper(connectivityServiceUtils.getContext(this), themeId), themeId);
            View view = LayoutInflater.from(buider.getContext()).inflate(34013280, (ViewGroup) null);
            checkBox = (CheckBox) view.findViewById(34603158);
            buider.setView(view);
            buider.setTitle(17039380);
        } else {
            buider = new AlertDialog.Builder(connectivityServiceUtils.getContext(this), connectivityServiceUtils.getContext(this).getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null));
            buider.setTitle(17039380);
            buider.setMessage(33685526);
        }
        buider.setIcon(17301543);
        buider.setPositiveButton(17040227, new DialogInterface.OnClickListener() {
            /* class com.android.server.HwConnectivityService.AnonymousClass8 */

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialoginterface, int i) {
                HwConnectivityService.this.clickPositiveButton(enableNotRemindFunction, checkBox);
            }
        });
        buider.setNegativeButton(17040226, new DialogInterface.OnClickListener() {
            /* class com.android.server.HwConnectivityService.AnonymousClass9 */

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int which) {
                HwConnectivityService.this.clickNegativeButton(enableNotRemindFunction, checkBox);
            }
        });
        buider.setOnCancelListener(new DialogInterface.OnCancelListener() {
            /* class com.android.server.HwConnectivityService.AnonymousClass10 */

            @Override // android.content.DialogInterface.OnCancelListener
            public void onCancel(DialogInterface dialog) {
                HwConnectivityService.this.clickCancel(enableNotRemindFunction, checkBox);
            }
        });
        AlertDialog dialog = buider.create();
        dialog.getWindow().setType(2008);
        return dialog;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void clickCancel(String enableNotRemindFunction, CheckBox finalBox) {
        connectivityServiceUtils.getContext(this).sendBroadcast(new Intent(DISABEL_DATA_SERVICE_ACTION));
        HwTelephonyManagerInner.getDefault().setDataEnabledWithoutPromp(false);
        if (VALUE_ENABLE_NOT_REMIND_FUNCTION.equals(enableNotRemindFunction) && finalBox != null) {
            updateReminderSetting(finalBox.isChecked());
        }
        this.mDataServiceToPdpDialog = null;
        this.mIsShowWarningRoamingToPdp = false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void clickNegativeButton(String enableNotRemindFunction, CheckBox finalBox) {
        connectivityServiceUtils.getContext(this).sendBroadcast(new Intent(DISABEL_DATA_SERVICE_ACTION));
        HwTelephonyManagerInner.getDefault().setDataEnabledWithoutPromp(false);
        if (!TextUtils.isEmpty(this.mSimChangeAlertDataConnect)) {
            Toast.makeText(this.mContext, 33685968, 1).show();
        }
        if (VALUE_ENABLE_NOT_REMIND_FUNCTION.equals(enableNotRemindFunction) && finalBox != null) {
            updateReminderSetting(finalBox.isChecked());
        }
        this.mDataServiceToPdpDialog = null;
        this.mIsShowWarningRoamingToPdp = false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void clickPositiveButton(String enableNotRemindFunction, CheckBox finalBox) {
        HwTelephonyManagerInner.getDefault().setDataEnabledWithoutPromp(true);
        if (!TextUtils.isEmpty(this.mSimChangeAlertDataConnect)) {
            if (this.mIsShowWarningRoamingToPdp) {
                createWarningRoamingToPdp().show();
            }
            Toast.makeText(this.mContext, 33685967, 1).show();
        }
        if (VALUE_ENABLE_NOT_REMIND_FUNCTION.equals(enableNotRemindFunction) && finalBox != null) {
            updateReminderSetting(finalBox.isChecked());
        }
        this.mDataServiceToPdpDialog = null;
        this.mIsShowWarningRoamingToPdp = false;
    }

    /* access modifiers changed from: protected */
    public void registerPhoneStateListener(Context context) {
        ((TelephonyManager) context.getSystemService("phone")).listen(this.mPhoneStateListener, 33);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateCallState(int state) {
        if (!this.mIsRemindService && !SystemProperties.getBoolean("gsm.huawei.RemindDataService", false) && !SystemProperties.getBoolean("gsm.huawei.RemindDataService_1", false)) {
            return;
        }
        if (state != 0) {
            AlertDialog alertDialog = this.mDataServiceToPdpDialog;
            if (alertDialog != null) {
                alertDialog.dismiss();
                this.mDataServiceToPdpDialog = null;
                this.mIsShowDlgEndCall = true;
            }
        } else if (this.mIsShowDlgEndCall && this.mDataServiceToPdpDialog == null) {
            this.mDataServiceToPdpDialog = createWarningToPdp();
            this.mDataServiceToPdpDialog.show();
            this.mIsShowDlgEndCall = false;
        }
    }

    /* access modifiers changed from: protected */
    public void registerBootStateListener(Context context) {
        new MobileEnabledSettingObserver(new Handler()).register();
    }

    /* access modifiers changed from: private */
    public class MobileEnabledSettingObserver extends ContentObserver {
        MobileEnabledSettingObserver(Handler handler) {
            super(handler);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void register() {
            HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).getContentResolver().registerContentObserver(Settings.Global.getUriFor("device_provisioned"), true, this);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean isSelfChange) {
            if (HwConnectivityService.this.mIsRemindService || HwConnectivityService.this.shouldShowThePdpWarning()) {
                super.onChange(isSelfChange);
                if (!HwConnectivityService.this.getMobileDataEnabled() && HwConnectivityService.this.mDataServiceToPdpDialog == null) {
                    HwConnectivityService hwConnectivityService = HwConnectivityService.this;
                    hwConnectivityService.mDataServiceToPdpDialog = hwConnectivityService.createWarningToPdp();
                    HwConnectivityService.this.mDataServiceToPdpDialog.show();
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean needSetUserDataEnabled(boolean isDateEnabled) {
        int dataStatus = Settings.Global.getInt(connectivityServiceUtils.getContext(this).getContentResolver(), "mobile_data", 1);
        if (!shouldShowThePdpWarning() || dataStatus != 0 || !isDateEnabled) {
            return true;
        }
        if (this.mIsShowDlgTurnOfDc) {
            this.mHandler.sendEmptyMessage(0);
            return false;
        }
        this.mIsShowDlgTurnOfDc = true;
        return true;
    }

    private void updateReminderSetting(boolean isChooseNotRemind) {
        if (isChooseNotRemind) {
            Settings.System.putInt(connectivityServiceUtils.getContext(this).getContentResolver(), WHETHER_SHOW_PDP_WARNING, VALUE_NOT_SHOW_PDP);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean shouldShowThePdpWarning() {
        if (WrapperFactory.getMSimTelephonyManagerWrapper().isMultiSimEnabled()) {
            return shouldShowThePdpWarningMsim();
        }
        String enableNotRemindFunction = SettingsEx.Systemex.getString(connectivityServiceUtils.getContext(this).getContentResolver(), ENABLE_NOT_REMIND_FUNCTION);
        boolean isRemindDataAllow = SystemProperties.getBoolean("gsm.huawei.RemindDataService", false);
        int pdpWarningValue = Settings.System.getInt(connectivityServiceUtils.getContext(this).getContentResolver(), WHETHER_SHOW_PDP_WARNING, VALUE_SHOW_PDP);
        if (!VALUE_ENABLE_NOT_REMIND_FUNCTION.equals(enableNotRemindFunction)) {
            return isRemindDataAllow;
        }
        if (!isRemindDataAllow || pdpWarningValue != VALUE_SHOW_PDP) {
            return false;
        }
        return true;
    }

    private boolean shouldShowThePdpWarningMsim() {
        String enableNotRemindFunction = SettingsEx.Systemex.getString(this.mContext.getContentResolver(), ENABLE_NOT_REMIND_FUNCTION);
        boolean isRemindDataAllow = false;
        int i = this.mDefaultDataSlotId;
        if (i == 1) {
            isRemindDataAllow = SystemProperties.getBoolean("gsm.huawei.RemindDataService_1", false);
        } else if (i == 0) {
            isRemindDataAllow = SystemProperties.getBoolean("gsm.huawei.RemindDataService", false);
        }
        int pdpWarningValue = Settings.System.getInt(this.mContext.getContentResolver(), WHETHER_SHOW_PDP_WARNING, VALUE_SHOW_PDP);
        if (VALUE_ENABLE_NOT_REMIND_FUNCTION.equals(enableNotRemindFunction)) {
            return isRemindDataAllow && pdpWarningValue == VALUE_SHOW_PDP;
        }
        return isRemindDataAllow;
    }

    private boolean shouldDisablePortalCheck(String ssid) {
        if (ssid != null) {
            log("wifi ssid: " + ssid);
            if (ssid.length() > 2 && ssid.charAt(0) == '\"' && ssid.charAt(ssid.length() - 1) == '\"') {
                ssid = ssid.substring(1, ssid.length() - 1);
            }
        }
        if (Settings.Secure.getInt(this.mContext.getContentResolver(), WifiProCommonUtils.PORTAL_NETWORK_FLAG, 0) == 2) {
            Settings.Secure.putInt(this.mContext.getContentResolver(), WifiProCommonUtils.PORTAL_NETWORK_FLAG, 0);
            log("not stop portal for user click wifi+ notification");
            return false;
        } else if (Settings.Secure.getInt(this.mContext.getContentResolver(), "user_setup_complete", 0) == 1 && Settings.Secure.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) == 1 && Settings.Global.getInt(this.mContext.getContentResolver(), "hw_disable_portal", 0) == 1) {
            log("stop portal check for orange");
            return true;
        } else if ("CMCC".equalsIgnoreCase(SystemProperties.get("ro.config.operators", "")) && "CMCC".equals(ssid)) {
            log("stop portal check for CMCC");
            return true;
        } else if (Settings.System.getInt(connectivityServiceUtils.getContext(this).getContentResolver(), DISABLE_PORTAL_CHECK, 0) != 1) {
            return isBrowserStrartSecurity();
        } else {
            Settings.System.putInt(connectivityServiceUtils.getContext(this).getContentResolver(), DISABLE_PORTAL_CHECK, 0);
            log("stop portal check for airsharing");
            return true;
        }
    }

    private boolean isBrowserStrartSecurity() {
        if (Settings.Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) == 0 && AppActConstant.VALUE_TRUE.equals(SettingsEx.Systemex.getString(this.mContext.getContentResolver(), "wifi.challenge.required"))) {
            log("setup guide wifi disable portal, and does not start browser!");
            return true;
        } else if (this.mIsTopAppSkytone) {
            log("stop start broswer for TopAppSkytone");
            return true;
        } else if (this.mIsTopAppHsbb) {
            log("stop start broswer for TopAppHsbb");
            return true;
        } else if (Settings.System.getInt(connectivityServiceUtils.getContext(this).getContentResolver(), WIFI_AP_MANUAL_CONNECT, 0) != 1) {
            return false;
        } else {
            Settings.System.putInt(connectivityServiceUtils.getContext(this).getContentResolver(), WIFI_AP_MANUAL_CONNECT, 0);
            log("portal ap manual connect");
            return false;
        }
    }

    public boolean startBrowserForWifiPortal(Notification notification, String ssid) {
        if (shouldDisablePortalCheck(ssid)) {
            log("do not start browser, popup system notification");
            return false;
        } else if (notification == null || notification.contentIntent == null) {
            loge("notification or contentIntent null");
            return false;
        } else {
            log("setNotificationVisible: cancel notification and start browser directly for TYPE_WIFI..");
            try {
                if (Settings.Secure.getInt(connectivityServiceUtils.getContext(this).getContentResolver(), WifiProCommonUtils.PORTAL_NETWORK_FLAG, 0) == 1) {
                    log("browser has been launched by notification user clicked it, don't launch browser here again.");
                    return true;
                }
                Settings.Secure.putInt(connectivityServiceUtils.getContext(this).getContentResolver(), WifiProCommonUtils.PORTAL_NETWORK_FLAG, 1);
                notification.contentIntent.send();
                return true;
            } catch (PendingIntent.CanceledException e) {
                loge("startBrowserForWifiPortal CanceledException");
                return false;
            }
        }
    }

    public boolean isSystemBootComplete() {
        return this.mIsSendWifiBroadcastAfterBootCompleted;
    }

    /* access modifiers changed from: protected */
    public void hintUserSwitchToMobileWhileWifiDisconnected(NetworkInfo.State state, int type) {
        if (WifiProCommonUtils.isWifiSelfCuring() && state == NetworkInfo.State.DISCONNECTED && type == 1) {
            log("DISCONNECTED, but enableDefaultTypeApn-->UP is ignored due to wifi self curing.");
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
            if (WifiProCommonUtils.isWifiSelfCuring() || this.mWifiDisconnectManager.shouldNotShowDataIcon()) {
                SystemProperties.set("sys.defaultapn.enabled", AppActConstant.VALUE_FALSE);
            }
        } else if (state == NetworkInfo.State.CONNECTED && type == 1) {
            this.mIsWifiConnected = true;
            String pktName = WifiProCommonUtils.getPackageName(this.mContext, WifiProCommonUtils.getForegroundAppUid(this.mContext));
            if (pktName != null && "com.huawei.hiskytone".equals(pktName)) {
                this.mIsTopAppSkytone = true;
            } else if (pktName != null && "com.nfyg.hsbb".equals(pktName)) {
                this.mIsTopAppHsbb = true;
            }
            SystemProperties.set("sys.defaultapn.enabled", AppActConstant.VALUE_TRUE);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void shouldEnableDefaultApn() {
        if (!this.mIsBlueThConnected) {
            enableDefaultTypeApn(true);
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
                    enableDefaultTypeApn(true);
                }
            } else {
                log("other NetworkInfo of state");
            }
        }
    }

    public void makeDefaultAndHintUser(NetworkAgentInfo newNetwork) {
        this.mWifiDisconnectManager.makeDefaultAndHintUser(newNetwork);
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (code == 1101) {
            data.enforceInterface(DESCPRIPTOR);
            int enableInt = data.readInt();
            log("needSetUserDataEnabled enableInt = " + enableInt);
            boolean result = needSetUserDataEnabled(enableInt == 1);
            log("needSetUserDataEnabled result = " + result);
            reply.writeNoException();
            reply.writeInt(result ? 1 : 0);
            return true;
        } else if (this.mRegisteredPushPkg.onTransact(code, data, reply, flags)) {
            return true;
        } else {
            return HwConnectivityService.super.onTransact(code, data, reply, flags);
        }
    }

    /* access modifiers changed from: private */
    public static class CtrlSocketInfo {
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

    /* access modifiers changed from: private */
    public class RegisteredPushPkg {
        private static final int ALLOW_NO_CTRL_SOCKET_LEVEL = 0;
        private static final int ALLOW_PART_CTRL_SOCKET_LEVEL = 1;
        private static final String CTRL_SOCKET_VERSION = "v2";
        private static final int MAX_REGISTERED_PKG_NUM = 10;
        private static final String MSG_ALL_CTRLSOCKET_ALLOWED = "android.ctrlsocket.all.allowed";
        private static final String MSG_SCROFF_CTRLSOCKET_STATS = "android.scroff.ctrlsocket.status";
        private boolean isPowerSaveEnabled = SystemProperties.getBoolean("ro.config.hw_power_saving", false);
        private CtrlSocketInfo mCtrlSocketInfo = new CtrlSocketInfo();
        private final Uri mWhiteListUri = Settings.Secure.getUriFor("push_white_apps");

        RegisteredPushPkg() {
        }

        public void init(Context context) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(HwConnectivityService.ACTION_OF_ANDROID_BOOT_COMPLETED);
            filter.addAction(HwConnectivityService.ACTION_BT_CONNECTION_CHANGED);
            if (this.isPowerSaveEnabled) {
                filter.addAction(MSG_SCROFF_CTRLSOCKET_STATS);
                filter.addAction(MSG_ALL_CTRLSOCKET_ALLOWED);
                getCtrlSocketRegisteredPkg();
                getCtrlSocketPushWhiteList();
                context.getContentResolver().registerContentObserver(this.mWhiteListUri, false, new ContentObserver(new Handler()) {
                    /* class com.android.server.HwConnectivityService.RegisteredPushPkg.AnonymousClass1 */

                    @Override // android.database.ContentObserver
                    public void onChange(boolean isSelfChange) {
                        RegisteredPushPkg.this.getCtrlSocketPushWhiteList();
                    }
                });
            }
            context.registerReceiver(HwConnectivityService.this.mIntentReceiver, filter);
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) {
            switch (code) {
                case 1001:
                    String registerPkg = data.readString();
                    HwConnectivityService.log("CtrlSocket registerPushSocket pkg = " + registerPkg);
                    registerPushSocket(registerPkg);
                    return true;
                case 1002:
                    String unregisterPkg = data.readString();
                    HwConnectivityService.log("CtrlSocket unregisterPushSocket pkg = " + unregisterPkg);
                    unregisterPushSocket(unregisterPkg);
                    return true;
                case NetworkSlicesHandler.ON_NETWORK_UNAVAILABLE /* 1003 */:
                default:
                    return false;
                case 1004:
                    reply.writeString(getActPkgInWhiteList());
                    return true;
                case 1005:
                    reply.writeInt(this.mCtrlSocketInfo.mAllowCtrlSocketLevel);
                    return true;
                case 1006:
                    HwConnectivityService.log("CtrlSocket getCtrlSocketVersion = v2");
                    reply.writeString(CTRL_SOCKET_VERSION);
                    return true;
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
                if (this.mCtrlSocketInfo.mRegisteredCount >= 10) {
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
        /* access modifiers changed from: public */
        private void getCtrlSocketPushWhiteList() {
            String[] str;
            String wlPkg = Settings.Secure.getString(HwConnectivityService.this.mContext.getContentResolver(), "push_white_apps");
            if (!(wlPkg == null || (str = wlPkg.split(AwarenessInnerConstants.SEMI_COLON_KEY)) == null || str.length <= 0)) {
                this.mCtrlSocketInfo.mPushWhiteListPkg.clear();
                for (int i = 0; i < str.length; i++) {
                    this.mCtrlSocketInfo.mPushWhiteListPkg.add(str[i]);
                    HwConnectivityService.log("CtrlSocket PushWhiteList[" + i + "] = " + str[i]);
                }
            }
        }

        private void getCtrlSocketRegisteredPkg() {
            String[] str;
            String registeredPkg = Settings.Secure.getString(HwConnectivityService.this.mContext.getContentResolver(), "registered_pkgs");
            if (!(registeredPkg == null || (str = registeredPkg.split(AwarenessInnerConstants.SEMI_COLON_KEY)) == null || str.length <= 0)) {
                this.mCtrlSocketInfo.mRegisteredPkg.clear();
                this.mCtrlSocketInfo.mRegisteredCount = 0;
                for (String str2 : str) {
                    this.mCtrlSocketInfo.mRegisteredPkg.add(str2);
                    this.mCtrlSocketInfo.mRegisteredCount++;
                }
            }
        }

        private void updateRegisteredPkg() {
            StringBuffer registeredPkg = new StringBuffer();
            for (String pkg : this.mCtrlSocketInfo.mRegisteredPkg) {
                registeredPkg.append(pkg);
                registeredPkg.append(AwarenessInnerConstants.SEMI_COLON_KEY);
            }
            Settings.Secure.putString(HwConnectivityService.this.mContext.getContentResolver(), "registered_pkgs", registeredPkg.toString());
        }

        private String getActPkgInWhiteList() {
            if (this.mCtrlSocketInfo.mAllowCtrlSocketLevel != 1) {
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
            String[] whitePackages;
            if (intent != null) {
                String action = intent.getAction();
                if (MSG_SCROFF_CTRLSOCKET_STATS.equals(action)) {
                    if (intent.getBooleanExtra("ctrl_socket_status", false)) {
                        this.mCtrlSocketInfo.mScrOffActPkg.clear();
                        this.mCtrlSocketInfo.mAllowCtrlSocketLevel = 1;
                        String actPkgs = intent.getStringExtra("ctrl_socket_list");
                        if (!(TextUtils.isEmpty(actPkgs) || (whitePackages = actPkgs.split("\t")) == null)) {
                            for (String str : whitePackages) {
                                this.mCtrlSocketInfo.mScrOffActPkg.add(str);
                            }
                        }
                    }
                } else if (MSG_ALL_CTRLSOCKET_ALLOWED.equals(action)) {
                    this.mCtrlSocketInfo.mAllowCtrlSocketLevel = 0;
                } else {
                    HwConnectivityService.log("no match action.");
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setMobileDataEnabled(String module, boolean isDataEnabled) {
        log("module:" + module + " setMobileDataEnabled enabled = " + isDataEnabled);
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
        if (tm != null) {
            tm.setDataEnabled(isDataEnabled);
            tm.setDataEnabledProperties(module, isDataEnabled);
        }
    }

    private boolean getDataEnabled() {
        boolean isDataEnabled = false;
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
        if (tm != null) {
            isDataEnabled = tm.getDataEnabled();
        }
        log("CtrlSocket getMobileDataEnabled enabled = " + isDataEnabled);
        return isDataEnabled;
    }

    public boolean getMobileDataEnabled() {
        boolean ret = false;
        if (!this.mIsSimStateChanged) {
            return false;
        }
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
        if (tm != null) {
            boolean isEnabledResult = false;
            try {
                int phoneCount = tm.getPhoneCount();
                for (int slotId = 0; slotId < phoneCount; slotId++) {
                    if (tm.getSimState(slotId) == 5) {
                        isEnabledResult = true;
                    }
                }
                if (!isEnabledResult) {
                    log("all sim card not ready, return false");
                    return false;
                }
                ret = tm.getDataEnabled();
            } catch (NullPointerException e) {
                log("getMobileDataEnabled NPE");
            }
        }
        log("CtrlSocket getMobileDataEnabled = " + ret);
        return ret;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void enableDefaultTypeApn(boolean isEnabled) {
        log("enableDefaultTypeAPN= " + isEnabled);
        String str = AppActConstant.VALUE_TRUE;
        String defaultMobileEnable = SystemProperties.get("sys.defaultapn.enabled", str);
        log("DEFAULT_MOBILE_ENABLE before state is " + defaultMobileEnable);
        if (!isEnabled) {
            str = AppActConstant.VALUE_FALSE;
        }
        SystemProperties.set("sys.defaultapn.enabled", str);
        rematchAllNetworksAndRequestsHw(null, 0);
        ConnectivityService.NetworkRequestInfo[] requests = (ConnectivityService.NetworkRequestInfo[]) obtainNetworkRequestsMapHw().values().toArray(new ConnectivityService.NetworkRequestInfo[0]);
        for (int i = 0; i < requests.length; i++) {
            if (requests[i].request.isRequest()) {
                sendUpdatedScoreToFactoriesHw(requests[i].request, null);
            }
        }
    }

    private void registerSimStateReceiver(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_SIM_STATE_CHANGED);
        filter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_DEFAULT_DATA_SUBSCRIPTION_CHANGED);
        context.registerReceiver(this.mSimStateReceiver, filter);
    }

    /* access modifiers changed from: protected */
    public void updateMobileDataAlwaysOnCust(int slotId) {
        this.mCust.mMobileDataAlwaysOnCust = false;
        try {
            Boolean isMobileDataAlwaysOnCfg = (Boolean) HwCfgFilePolicy.getValue("mobile_data_always_on", slotId, Boolean.class);
            if (isMobileDataAlwaysOnCfg != null) {
                this.mCust.mMobileDataAlwaysOnCust = isMobileDataAlwaysOnCfg.booleanValue();
            }
        } catch (Exception e) {
            log("Exception: read mobile_data_always_on error");
        }
        updateAlwaysOnNetworks();
    }

    private class HwConnectivityServiceHandler extends Handler {
        private static final int EVENT_SHOW_ENABLE_PDP_DIALOG = 0;
        private static final int EVENT_SWITCH_TO_MOBILE_NETWORK = 1;
        private static final int SWITCH_TO_MOBILE_NETWORK_DELAY = 5000;

        HwConnectivityServiceHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 0) {
                HwConnectivityService.this.handleShowEnablePdpDialog();
            } else if (i == 1 && HwConnectivityService.this.mWifiDisconnectManager != null) {
                HwConnectivityService.this.mWifiDisconnectManager.switchToMobileNetwork();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleShowEnablePdpDialog() {
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
    public void setExplicitlyUnselected(NetworkAgentInfo nai) {
        if (nai != null) {
            nai.networkMisc.explicitlySelected = false;
            nai.networkMisc.acceptUnvalidated = false;
            if (nai.networkInfo != null && ConnectivityManager.getNetworkTypeName(1).equals(nai.networkInfo.getTypeName())) {
                log("setExplicitlyUnselected, WiFi+ switch from WiFi to Cellular, enableDefaultTypeApn explicitly.");
                enableDefaultTypeApn(true);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void updateNetworkConcurrently(NetworkAgentInfo networkAgent, NetworkInfo newInfo) {
        NetworkInfo oldInfo;
        NetworkInfo.State state = newInfo.getState();
        INetd netd = NetdService.getInstance();
        synchronized (networkAgent) {
            oldInfo = networkAgent.networkInfo;
            networkAgent.networkInfo = newInfo;
        }
        if (oldInfo != null && oldInfo.getState() == state) {
            log("updateNetworkConcurrently, ignoring duplicate network state non-change");
        } else if (netd == null) {
            loge("updateNetworkConcurrently, invalid member, netd = null");
        } else {
            networkAgent.setCurrentScore(0);
            try {
                netd.networkCreatePhysical(networkAgent.network.netId, networkAgent.networkCapabilities.hasCapability(13) ? 0 : 2);
                networkAgent.created = true;
                connectivityServiceUtils.updateLinkProperties(this, networkAgent, null);
                log("updateNetworkConcurrently, nai.networkInfo = " + networkAgent.networkInfo);
                Bundle redirectUrlBundle = new Bundle();
                redirectUrlBundle.putString(NetworkAgent.REDIRECT_URL_KEY, "");
                networkAgent.asyncChannel.sendMessage(528391, 4, 0, redirectUrlBundle);
            } catch (Exception e) {
                loge("updateNetworkConcurrently, Error creating network");
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean reportPortalNetwork(NetworkAgentInfo nai, int result) {
        if ((result & 128) == 0) {
            return false;
        }
        Bundle redirectUrlBundle = new Bundle();
        redirectUrlBundle.putString(NetworkAgent.REDIRECT_URL_KEY, "");
        nai.asyncChannel.sendMessage(528391, 3, 0, redirectUrlBundle);
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
        return false;
    }

    /* access modifiers changed from: protected */
    public NetworkInfo getActiveNetworkForMpLink(NetworkInfo info, int uid) {
        return info;
    }

    public Network getNetworkForTypeWifi() {
        NetworkCapabilities nc;
        Network activeNetwork = HwConnectivityService.super.getActiveNetwork();
        NetworkInfo activeNetworkInfo = HwConnectivityService.super.getActiveNetworkInfo();
        Network[] networks = HwConnectivityService.super.getAllNetworks();
        int i = 0;
        if (activeNetworkInfo == null || activeNetwork == null) {
            int length = networks.length;
            while (i < length) {
                Network network = networks[i];
                NetworkCapabilities nc2 = HwConnectivityService.super.getNetworkCapabilities(network);
                if (!(nc2 == null || !nc2.hasTransport(1) || nc2.hasTransport(4))) {
                    return network;
                }
                i++;
            }
            return null;
        }
        NetworkCapabilities anc = HwConnectivityService.super.getNetworkCapabilities(activeNetwork);
        if (anc != null && anc.hasTransport(4)) {
            i = 1;
        }
        if (i == 0 && activeNetworkInfo.getType() == 1) {
            return activeNetwork;
        }
        if (i == 0 && activeNetworkInfo.getType() == 1) {
            log("no match wifi type.");
            return null;
        }
        for (int i2 = 0; i2 < networks.length; i2++) {
            if (!(networks[i2].netId == activeNetwork.netId || (nc = HwConnectivityService.super.getNetworkCapabilities(networks[i2])) == null || !nc.hasTransport(1) || nc.hasTransport(4))) {
                return networks[i2];
            }
        }
        return null;
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
    public void setVpnSettingValue(boolean isEnabled) {
        log("WiFi_PRO, setVpnSettingValue =" + isEnabled);
        HwSysResManager.getInstance().reportAwareVpnConnect(isEnabled);
        Settings.System.putInt(this.mContext.getContentResolver(), HwArbitrationDefs.SETTING_SECURE_VPN_WORK_VALUE, isEnabled ? 1 : 0);
    }

    private boolean isRequestedByPkgName(int pid, String pkgName) {
        String[] pkgNameList;
        List<ActivityManager.RunningAppProcessInfo> appProcessList = this.mActivityManager.getRunningAppProcesses();
        if (appProcessList == null || pkgName == null) {
            return false;
        }
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcessList) {
            if (appProcess != null && appProcess.pid == pid) {
                for (String str : appProcess.pkgList) {
                    if (pkgName.equals(str)) {
                        return true;
                    }
                }
                continue;
            }
        }
        return false;
    }

    public NetworkInfo getActiveNetworkInfo() {
        NetworkInfo networkInfo = HwConnectivityService.super.getActiveNetworkInfo();
        if (networkInfo != null || !isRequestedByPkgName(Binder.getCallingPid(), "com.huawei.systemmanager")) {
            return networkInfo;
        }
        log("return the background wifi network info for system manager.");
        return getNetworkInfoForBackgroundWifi();
    }

    /* access modifiers changed from: protected */
    public boolean isNetworkRequestBip(NetworkRequest nr) {
        if (nr == null) {
            loge("network request is null!");
            return false;
        } else if (nr.networkCapabilities.hasCapability(25) || nr.networkCapabilities.hasCapability(26) || nr.networkCapabilities.hasCapability(27) || nr.networkCapabilities.hasCapability(28) || nr.networkCapabilities.hasCapability(29) || nr.networkCapabilities.hasCapability(30) || nr.networkCapabilities.hasCapability(31)) {
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
        } else if (nai.networkInfo.getType() != 0 || !nai.isInternet() || !isNetworkRequestBip(nri)) {
            return false;
        } else {
            String defaultApn = SystemProperties.get("gsm.default.apn");
            String bipApn = SystemProperties.get("gsm.bip.apn");
            if (defaultApn == null || bipApn == null) {
                loge("default apn is null or bip apn is null, default: " + defaultApn + ", bip: " + bipApn);
                return false;
            } else if (AppActConstant.VALUE_DEFAULT.equals(bipApn)) {
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

    public void setLteMobileDataEnabled(boolean isMobileEnabled) {
        log("[enter]setLteMobileDataEnabled " + isMobileEnabled);
        enforceChangePermissionHw();
        HwTelephonyManagerInner.getDefault().setLteServiceAbility(isMobileEnabled ? 1 : 0);
        sendLteDataStateBroadcast(this.mLteMobileDataState);
    }

    public int checkLteConnectState() {
        enforceAccessPermissionHw();
        return this.mLteMobileDataState;
    }

    /* access modifiers changed from: protected */
    public void handleLteMobileDataStateChange(NetworkInfo info) {
        int lteState;
        if (info == null) {
            loge("NetworkInfo got null!");
            return;
        }
        log("[enter]handleLteMobileDataStateChange type=" + info.getType() + ",subType=" + info.getSubtype());
        if (info.getType() == 0) {
            if (info.getSubtype() == 13) {
                lteState = mapDataStateToLteDataState(info.getState());
            } else {
                lteState = 3;
            }
            setLteMobileDataState(lteState);
        }
    }

    private int mapDataStateToLteDataState(NetworkInfo.State state) {
        log("[enter]mapDataStateToLteDataState state=" + state);
        int i = AnonymousClass12.$SwitchMap$android$net$NetworkInfo$State[state.ordinal()];
        if (i == 1) {
            return 0;
        }
        if (i == 2) {
            return 1;
        }
        if (i == 3) {
            return 2;
        }
        if (i == 4) {
            return 3;
        }
        log("mapDataStateToLteDataState ignore state = " + state);
        return 3;
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.server.HwConnectivityService$12  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass12 {
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

    private synchronized void setLteMobileDataState(int state) {
        log("[enter]setLteMobileDataState state=" + state);
        this.mLteMobileDataState = state;
        sendLteDataStateBroadcast(this.mLteMobileDataState);
    }

    private void sendLteDataStateBroadcast(int state) {
        Intent intent = new Intent(ACTION_LTEDATA_COMPLETED_ACTION);
        intent.putExtra(EXTRA_IS_LTE_MOBILE_DATA_STATUS, state);
        log("Send sticky broadcast from ConnectivityService. intent=" + intent);
        sendStickyBroadcastHw(intent);
    }

    public long getLteTotalRxBytes() {
        log("[enter]getLteTotalRxBytes");
        enforceAccessPermissionHw();
        long lteRxBytes = 0;
        try {
            NetworkStatsHistory.Entry entry = getLteStatsEntry(2);
            if (entry != null) {
                lteRxBytes = entry.rxBytes;
            }
        } catch (SecurityException e) {
            loge("getLteTotalRxBytes SecurityException");
        } catch (Exception e2) {
            loge("getLteTotalRxBytes other exception");
        }
        log("lteTotalRxBytes=" + lteRxBytes);
        return lteRxBytes;
    }

    public long getLteTotalTxBytes() {
        log("[enter]getLteTotalTxBytes");
        enforceAccessPermissionHw();
        long lteTxBytes = 0;
        try {
            NetworkStatsHistory.Entry entry = getLteStatsEntry(8);
            if (entry != null) {
                lteTxBytes = entry.txBytes;
            }
        } catch (SecurityException e) {
            loge("getLteTotalTxBytes SecurityException");
        } catch (Exception e2) {
            loge("getLteTotalTxBytes other exception");
        }
        log("LteTotalTxBytes=" + lteTxBytes);
        return lteTxBytes;
    }

    private NetworkStatsHistory.Entry getLteStatsEntry(int fields) {
        NetworkStatsHistory networkStatsHistory;
        log("[enter]getLteStatsEntry fields=" + fields);
        NetworkStatsHistory.Entry entry = null;
        INetworkStatsSession session = null;
        try {
            NetworkTemplate mobile4gTemplate = NetworkTemplate.buildTemplateMobileAll(((TelephonyManager) this.mContext.getSystemService("phone")).getSubscriberId());
            getStatsService().forceUpdate();
            session = getStatsService().openSession();
            if (!(session == null || (networkStatsHistory = session.getHistoryForNetwork(mobile4gTemplate, fields)) == null)) {
                entry = networkStatsHistory.getValues(Long.MIN_VALUE, Long.MAX_VALUE, (NetworkStatsHistory.Entry) null);
            }
        } catch (SecurityException e) {
            loge("getLteStatsEntry SecurityException");
        } catch (Exception e2) {
            loge("getLteStatsEntry other exception");
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
            log("[enter]getStatsService");
            if (sStatsService == null) {
                sStatsService = INetworkStatsService.Stub.asInterface(ServiceManager.getService("netstats"));
            }
            iNetworkStatsService = sStatsService;
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
        IMapconService iMapconService = this.mMapconService;
        if (iMapconService != null) {
            try {
                domainPrefer = iMapconService.getVoWifiServiceDomain(phoneId2, type);
            } catch (RemoteException ex) {
                loge("getVoWifiServiceDomain failed, err = " + ex.toString());
            }
        }
        boolean isVoWifiRegistered = HwTelephonyManagerInner.getDefault().isWifiCallingAvailable(phoneId2);
        boolean isMmsFollowVowifiPreferDomain = getBooleanCarrierConfig(phoneId2, "mms_follow_vowifi_prefer_domain");
        log("getVoWifiServiceDomain isVoWifiRegistered:" + isVoWifiRegistered + " isMmsFollowVowifiPreferDomain:" + isMmsFollowVowifiPreferDomain);
        if (!isMmsFollowVowifiPreferDomain || type != 0 || AbstractConnectivityService.DomainPreferType.DOMAIN_PREFER_VOLTE.value() != domainPrefer || !isVoWifiRegistered) {
            return domainPrefer;
        }
        log("VoWiFi registered and set mms domain as DOMAIN_PREFER_WIFI");
        return AbstractConnectivityService.DomainPreferType.DOMAIN_PREFER_WIFI.value();
    }

    private boolean getBooleanCarrierConfig(int subId, String key) {
        CarrierConfigManager configManager = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
        PersistableBundle persistableBundle = null;
        if (configManager != null) {
            persistableBundle = configManager.getConfigForSubId(subId);
        }
        if (persistableBundle == null || persistableBundle.get(key) == null) {
            return false;
        }
        return persistableBundle.getBoolean(key);
    }

    private int getVoWifiServiceState(int phoneId2, int type) {
        IMapconService iMapconService = this.mMapconService;
        if (iMapconService == null) {
            return -1;
        }
        try {
            return iMapconService.getVoWifiServiceState(phoneId2, type);
        } catch (RemoteException ex) {
            loge("getVoWifiServiceState failed, err = " + ex.toString());
            return -1;
        }
    }

    private int getPhoneIdFromNetworkCapabilities(NetworkCapabilities networkCapabilities) {
        int subId = -1;
        int netPhoneId = -1;
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
            netPhoneId = SubscriptionManager.getPhoneId(subId);
        }
        log("getPhoneIdFromNetworkCapabilities, subId = " + subId + " phoneId:" + netPhoneId);
        return netPhoneId;
    }

    private boolean isDomainPreferRequest(NetworkRequest request) {
        if (request.type != NetworkRequest.Type.REQUEST || !request.networkCapabilities.hasCapability(0)) {
            return false;
        }
        return true;
    }

    private boolean rebuildNetworkRequestByPrefer(ConnectivityService.NetworkRequestInfo nri, AbstractConnectivityService.DomainPreferType prefer) {
        NetworkRequest clientRequest = new NetworkRequest(nri.request);
        NetworkCapabilities networkCapabilities = nri.request.networkCapabilities;
        NetworkSpecifier networkSpecifier = networkCapabilities.getNetworkSpecifier();
        if (prefer == AbstractConnectivityService.DomainPreferType.DOMAIN_ONLY_WIFI || prefer == AbstractConnectivityService.DomainPreferType.DOMAIN_PREFER_WIFI) {
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
        } else if (prefer != AbstractConnectivityService.DomainPreferType.DOMAIN_PREFER_CELLULAR && prefer != AbstractConnectivityService.DomainPreferType.DOMAIN_PREFER_VOLTE) {
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
        int netPhoneId;
        NetworkCapabilities cap = nri.request.networkCapabilities;
        int domainPrefer = -1;
        if (IS_WIFI_MMS_UT_ON && IS_MAPCON_ON && isDomainPreferRequest(nri.request)) {
            if (cap.getNetworkSpecifier() != null) {
                netPhoneId = getPhoneIdFromNetworkCapabilities(cap);
            } else {
                netPhoneId = SubscriptionManager.getPhoneId(SubscriptionManager.getDefaultDataSubscriptionId());
            }
            if (netPhoneId != -1 && cap.hasCapability(0) && getVoWifiServiceState(netPhoneId, 1) == 1) {
                domainPrefer = getVoWifiServiceDomain(netPhoneId, 0);
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
                if (prefer == AbstractConnectivityService.DomainPreferType.DOMAIN_PREFER_WIFI || prefer == AbstractConnectivityService.DomainPreferType.DOMAIN_PREFER_CELLULAR || prefer == AbstractConnectivityService.DomainPreferType.DOMAIN_PREFER_VOLTE) {
                    this.mDomainPreferHandler.sendMessageDelayed(this.mDomainPreferHandler.obtainMessage(2, prefer.value(), 0, nri.request), 10000);
                }
            }
        }
        HwConnectivityService.super.handleRegisterNetworkRequest(nri);
    }

    /* access modifiers changed from: protected */
    public void handleReleaseNetworkRequest(NetworkRequest request, int callingUid, boolean isCallOnUnavailable) {
        ConnectivityService.NetworkRequestInfo nri;
        NetworkRequest req = request;
        if (IS_WIFI_MMS_UT_ON && IS_MAPCON_ON && isDomainPreferRequest(request) && (nri = findExistingNetworkRequestInfo(request.requestId)) != null && !nri.request.equals(request)) {
            if (nri.clientRequest == null || !nri.clientRequest.equals(request)) {
                loge("BUG: Do not find request in mNetworkRequests for preferRequest:" + request);
            } else {
                req = nri.request;
            }
        }
        HwConnectivityService.super.handleReleaseNetworkRequest(req, callingUid, isCallOnUnavailable);
    }

    /* access modifiers changed from: protected */
    public void handleRemoveNetworkRequest(ConnectivityService.NetworkRequestInfo nri, int whichCallback) {
        if (IS_WIFI_MMS_UT_ON && IS_MAPCON_ON && isDomainPreferRequest(nri.request)) {
            this.mDomainPreferHandler.removeMessages(2, nri.request);
        }
        HwConnectivityService.super.handleRemoveNetworkRequest(nri, whichCallback);
    }

    /* access modifiers changed from: protected */
    public void notifyNetworkAvailable(NetworkAgentInfo nai, ConnectivityService.NetworkRequestInfo nri) {
        if (IS_WIFI_MMS_UT_ON && IS_MAPCON_ON && nri != null && isDomainPreferRequest(nri.request)) {
            this.mDomainPreferHandler.sendMessageAtFrontOfQueue(Message.obtain(this.mDomainPreferHandler, 0, nri.request));
        }
        HwConnectivityService.super.notifyNetworkAvailable(nai, nri);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private ConnectivityService.NetworkRequestInfo findExistingNetworkRequestInfo(int requestId) {
        for (Map.Entry<NetworkRequest, ConnectivityService.NetworkRequestInfo> entry : obtainNetworkRequestsMapHw().entrySet()) {
            if (entry.getValue().request.requestId == requestId) {
                return entry.getValue();
            }
        }
        return null;
    }

    /* access modifiers changed from: private */
    public class DomainPreferHandler extends Handler {
        private static final int MSG_PREFER_NETWORK_FAIL = 1;
        private static final int MSG_PREFER_NETWORK_SUCCESS = 0;
        private static final int MSG_PREFER_NETWORK_TIMEOUT = 2;

        DomainPreferHandler(Looper looper) {
            super(looper);
        }

        private String getMsgName(int whatMsg) {
            if (whatMsg == 0) {
                return "PREFER_NETWORK_SUCCESS";
            }
            if (whatMsg == 1) {
                return "PREFER_NETWORK_FAIL";
            }
            if (whatMsg != 2) {
                return Integer.toString(whatMsg);
            }
            return "PREFER_NETWORK_TIMEOUT";
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            HwConnectivityService.log("DomainPreferHandler handleMessage msg.what = " + getMsgName(msg.what));
            int i = msg.what;
            if (i == 0) {
                handlePreferNetworkSuccess(msg);
            } else if (i == 1) {
                handlePreferNetworkFail(msg);
            } else if (i == 2) {
                handlePreferNetworkTimeout(msg);
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
            ConnectivityService.NetworkRequestInfo nri = (ConnectivityService.NetworkRequestInfo) HwConnectivityService.this.obtainNetworkRequestsMapHw().get(req);
            if (nri != null) {
                if ((prefer == AbstractConnectivityService.DomainPreferType.DOMAIN_PREFER_WIFI || prefer == AbstractConnectivityService.DomainPreferType.DOMAIN_PREFER_CELLULAR || prefer == AbstractConnectivityService.DomainPreferType.DOMAIN_PREFER_VOLTE) && ((NetworkAgentInfo) HwConnectivityService.this.obtainNetworkForRequestIdArrayHw().get(req.requestId)) == null && prefer != null) {
                    for (ConnectivityService.NetworkFactoryInfo nfi : HwConnectivityService.this.obtainNetworkFactoryInfosMapHw().values()) {
                        nfi.asyncChannel.sendMessage(536577, req);
                    }
                    NetworkCapabilities networkCapabilities = req.networkCapabilities;
                    NetworkSpecifier networkSpecifier = networkCapabilities.getNetworkSpecifier();
                    HwConnectivityService.this.obtainNetworkRequestsMapHw().remove(req);
                    LocalLog obtainNetworkRequestInfoLogsHw = HwConnectivityService.this.obtainNetworkRequestInfoLogsHw();
                    obtainNetworkRequestInfoLogsHw.log("UPDATE-RELEASE " + nri);
                    if (prefer == AbstractConnectivityService.DomainPreferType.DOMAIN_PREFER_WIFI) {
                        networkCapabilities.setNetworkSpecifier(null);
                        networkCapabilities.addTransportType(0);
                        networkCapabilities.removeTransportType(1);
                        networkCapabilities.setNetworkSpecifier(networkSpecifier);
                    } else if (prefer == AbstractConnectivityService.DomainPreferType.DOMAIN_PREFER_CELLULAR || prefer == AbstractConnectivityService.DomainPreferType.DOMAIN_PREFER_VOLTE) {
                        networkCapabilities.setNetworkSpecifier(null);
                        networkCapabilities.addTransportType(1);
                        networkCapabilities.removeTransportType(0);
                        if (networkSpecifier == null) {
                            networkCapabilities.setNetworkSpecifier(new StringNetworkSpecifier(Integer.toString(SubscriptionManager.getDefaultDataSubscriptionId())));
                        } else {
                            networkCapabilities.setNetworkSpecifier(networkSpecifier);
                        }
                    } else {
                        HwConnectivityService.log("prefer is not DOMAIN_PREFER_WIFI, DOMAIN_PREFER_CELLULAR, DOMAIN_PREFER_VOLTE");
                    }
                    HwConnectivityService.this.obtainNetworkRequestsMapHw().put(req, nri);
                    LocalLog obtainNetworkRequestInfoLogsHw2 = HwConnectivityService.this.obtainNetworkRequestInfoLogsHw();
                    obtainNetworkRequestInfoLogsHw2.log("UPDATE-REGISTER " + nri);
                    HwConnectivityService.this.rematchAllNetworksAndRequestsHw(null, 0);
                    if (HwConnectivityService.this.obtainNetworkForRequestIdArrayHw().get(req.requestId) == null) {
                        HwConnectivityService.this.sendUpdatedScoreToFactoriesHw(req, null);
                    }
                }
            }
        }
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
            intent.putExtra(HwCertification.KEY_DATE_FROM, CHECK_PERMISSION_MSG);
            try {
                this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
            } catch (Exception e) {
                log("sendNetworkStickyBroadcastAsUser failed");
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
            this.mContext.enforceCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL", CHECK_PERMISSION_MSG);
        }
        this.mContext.enforceCallingOrSelfPermission(MDM_VPN_PERMISSION, "NEED MDM_VPN PERMISSION");
        SparseArray<Vpn> mVpns = getmVpns();
        synchronized (mVpns) {
            Vpn vpn = mVpns.get(userId);
            if (vpn == null) {
                return false;
            }
            return vpn.turnOffAllVpn(packageName);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void processWhenSimStateChange(Intent intent) {
        if (!TelephonyManager.getDefault().isMultiSimEnabled() && !this.isAlreadyPop && "READY".equals(intent.getStringExtra("ss"))) {
            this.mIsSimReady = true;
            connectivityServiceUtils.getContext(this).sendBroadcast(new Intent(DISABEL_DATA_SERVICE_ACTION));
            HwTelephonyManagerInner.getDefault().setDataEnabledWithoutPromp(false);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isSetupWizardCompleted() {
        return Settings.Secure.getInt(this.mContext.getContentResolver(), "user_setup_complete", 0) == 1 && Settings.Secure.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) == 1;
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
        filter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_WIFI_NETWORK_STATE_CHANGED);
        context.registerReceiver(this.mVerizonWifiDisconnectReceiver, filter);
    }

    /* access modifiers changed from: protected */
    public void updateDefaultNetworkRouting(NetworkAgentInfo oldDefaultNet, NetworkAgentInfo newDefaultNet) {
        String oldIface;
        if (newDefaultNet == null || newDefaultNet.linkProperties == null || newDefaultNet.networkInfo == null) {
            log("invalid new defautl net");
        } else if (oldDefaultNet != null && oldDefaultNet.linkProperties != null && oldDefaultNet.networkInfo != null && oldDefaultNet.network != null && oldDefaultNet.networkInfo.getType() == 0 && newDefaultNet.networkInfo.getType() == 1 && (oldIface = oldDefaultNet.linkProperties.getInterfaceName()) != null) {
            try {
                log("Remove oldIface " + oldIface + " from network " + oldDefaultNet.network.netId);
                getmNMSHw().removeInterfaceFromNetwork(oldIface, oldDefaultNet.network.netId);
                log("Clear oldIface " + oldIface + " addresses");
                getmNMSHw().clearInterfaceAddresses(oldIface);
                log("recovery oldIface " + oldIface + " addresses, immediately");
                for (LinkAddress oldAddr : oldDefaultNet.linkProperties.getLinkAddresses()) {
                    String oldAddrString = oldAddr.getAddress().getHostAddress();
                    try {
                        log("add addr_x:  to interface: " + oldIface);
                        this.mNetd.interfaceAddAddress(oldIface, oldAddrString, oldAddr.getPrefixLength());
                    } catch (RemoteException | ServiceSpecificException e) {
                        loge("Failed to add addr : " + e);
                    } catch (Exception e2) {
                        loge("Failed to add addr");
                    }
                }
                log("refresh linkproperties for recovery oldIface");
                hwUpdateLinkProperties(oldDefaultNet, null);
            } catch (Exception e3) {
                loge("Exception clearing interface");
            }
        }
    }

    public void recordPrivateDnsEvent(Context context, int returnCode, int latencyMs, int netId) {
        LinkProperties activeLp;
        NetworkAgentInfo nai = getNetworkAgentInfoForNetIdHw(netId);
        if (nai != null && (activeLp = nai.linkProperties) != null) {
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
        NetworkAgentInfo nai = getNetworkAgentInfoForNetIdHw(netId);
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
        PrivateDnsConfig cfg;
        DnsManager dnsManager = getDnsManager();
        if (dnsManager == null || (cfg = dnsManager.getPrivateDnsConfig()) == null || !cfg.useTls || cfg.inStrictMode()) {
            return false;
        }
        return true;
    }

    public void clearInvalidPrivateDnsNetworkInfo() {
        synchronized (this.mLock) {
            Iterator<Integer> it = this.mBypassPrivateDnsNetwork.keySet().iterator();
            while (it.hasNext()) {
                int networkId = it.next().intValue();
                boolean isInvalidNetId = true;
                Network[] allNetworks = getAllNetworks();
                int length = allNetworks.length;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        break;
                    } else if (allNetworks[i].netId == networkId) {
                        isInvalidNetId = false;
                        break;
                    } else {
                        i++;
                    }
                }
                log("clearInvalidPrivateDnsNetworkInfo networkId " + networkId + " ,isInvalidNetId " + isInvalidNetId);
                if (isInvalidNetId) {
                    it.remove();
                }
            }
        }
    }

    public void startCaptivePortalAppInternal(Network network, Bundle appExtras) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MAINLINE_NETWORK_STACK", CHECK_PERMISSION_MSG);
        if (SystemProperties.getBoolean("runtime.hwwifi.portal_webview_support", false) || !IS_CHINA || IS_TV) {
            Intent appIntent = new Intent("android.net.conn.CAPTIVE_PORTAL");
            appIntent.putExtras(appExtras);
            appIntent.putExtra("android.net.extra.CAPTIVE_PORTAL", new CaptivePortal(new ConnectivityService.CaptivePortalImpl(this, network).asBinder()));
            appIntent.setFlags(272629760);
            Binder.withCleanCallingIdentity(new FunctionalUtils.ThrowingRunnable(appIntent) {
                /* class com.android.server.$$Lambda$HwConnectivityService$HMw5njcSoFCv2_UFCubZsMd1Deo */
                private final /* synthetic */ Intent f$1;

                {
                    this.f$1 = r2;
                }

                public final void runOrThrow() {
                    HwConnectivityService.this.lambda$startCaptivePortalAppInternal$0$HwConnectivityService(this.f$1);
                }
            });
        } else if (appExtras != null) {
            String urlString = appExtras.getString("android.net.extra.CAPTIVE_PORTAL_URL");
            if (TextUtils.isEmpty(urlString)) {
                urlString = Settings.Global.getString(this.mContext.getContentResolver(), "captive_portal_http_url");
            }
            if (TextUtils.isEmpty(urlString)) {
                urlString = DEFAULT_HTTP_URL;
            }
            HwServiceFactory.getHwConnectivityManager().startBrowserOnClickNotification(this.mContext, urlString);
        }
    }

    public /* synthetic */ void lambda$startCaptivePortalAppInternal$0$HwConnectivityService(Intent appIntent) throws Exception {
        this.mContext.startActivityAsUser(appIntent, UserHandle.CURRENT);
    }

    public boolean isBypassPrivateDns(int netId) {
        BypassPrivateDnsInfo privateDnsInfo = this.mBypassPrivateDnsNetwork.get(Integer.valueOf(netId));
        if (privateDnsInfo != null) {
            log("isBypassPrivateDns netId: " + netId + ", mIsBypassPrivateDns: " + privateDnsInfo.mIsBypassPrivateDns);
            return privateDnsInfo.mIsBypassPrivateDns;
        }
        log("isBypassPrivateDns return false");
        return false;
    }

    /* access modifiers changed from: private */
    public static class BypassPrivateDnsInfo {
        private static final int[] BACKOFF_TIME_INTERVAL = {1, 2, 2, 4, 4, 4, 4, 8};
        private static final String CHR_BROADCAST_PERMISSION = "com.huawei.android.permission.GET_CHR_DATA";
        private static final String INTENT_WIFI_PRIVATE_DNS_STATISTICS = "com.intent.action.wifi_private_dns_statistics";
        private static final int PRIVATE_DNS_CONFIG_LENGTH = 8;
        private int backoffCnt = 0;
        private int backoffTime = Constant.MILLISEC_TO_HOURS;
        private int badCnt = 6;
        private int badTotalCnt = 8;
        private int failedCnt = 8;
        private String mAssignedServers;
        private int mBadDelayTotalCnt;
        private Context mContext;
        private int mDelay1000Cnt;
        private int mDelay150Cnt;
        private int mDelay500Cnt;
        private int mDelayOver1000Cnt;
        private int mDnsDelayOverThresCnt = 0;
        private boolean mIsBypassPrivateDns;
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

        BypassPrivateDnsInfo(Context context, int networkType, String assignedServers) {
            this.mContext = context;
            this.mNetworkType = networkType;
            this.mAssignedServers = assignedServers;
            this.mPrivateDnsCntResetTime = SystemClock.elapsedRealtime();
            this.mIsBypassPrivateDns = false;
            String[] privateDnsConfig = SystemProperties.get(HwConnectivityService.PROP_PRIVATE_DNS_CONFIG, HwConnectivityService.DEFAULT_PRIVATE_DNS_CONFIG).split(",");
            try {
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
            } catch (NumberFormatException e) {
                HwConnectivityService.log("parse DnsConfig fail");
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
            if (this.mIsBypassPrivateDns) {
                int index = this.backoffCnt;
                int[] iArr = BACKOFF_TIME_INTERVAL;
                if (index > iArr.length - 1) {
                    index = iArr.length - 1;
                }
                if (SystemClock.elapsedRealtime() - this.mPrivateDnsBackoffTime >= ((long) (BACKOFF_TIME_INTERVAL[index] * this.backoffTime))) {
                    HwConnectivityService.log("isNeedUpdatePrivateDnsSettings private dns backoff timeout");
                    reset();
                    this.backoffCnt++;
                    this.mPrivateDnsBackoffTime = 0;
                    this.mIsBypassPrivateDns = false;
                    sendIntentPrivateDnsEvent();
                    return true;
                }
            } else if ((this.mDelayOver1000Cnt >= this.unacceptCnt || this.mDelay1000Cnt >= this.verybadCnt || this.mDelay500Cnt >= this.badCnt || this.mBadDelayTotalCnt >= this.badTotalCnt || this.mPrivateDnsFailCount >= this.failedCnt) && this.mNetworkType == 1) {
                HwConnectivityService.log(" isNeedUpdatePrivateDnsSettings mDelay150Cnt : " + this.mDelay150Cnt + " , mDelay500Cnt : " + this.mDelay500Cnt + " , mDelay1000Cnt : " + this.mDelay1000Cnt + " , mDelayOver1000Cnt : " + this.mDelayOver1000Cnt + " , mBadDelayTotalCnt = " + this.mBadDelayTotalCnt);
                this.mPrivateDnsBackoffTime = SystemClock.elapsedRealtime();
                this.mIsBypassPrivateDns = true;
                this.mDnsDelayOverThresCnt = this.mDnsDelayOverThresCnt + 1;
                sendIntentPrivateDnsEvent();
                return true;
            }
            return false;
        }

        public void updateDelayCount(int returnCode, int latencyMs) {
            if (this.mIsBypassPrivateDns) {
                HwConnectivityService.log("updateDelayCount mIsBypassPrivateDns return");
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
                } else {
                    HwConnectivityService.log("no match dns delay.");
                }
                this.mBadDelayTotalCnt = this.mDelay500Cnt + this.mDelay1000Cnt + this.mDelayOver1000Cnt;
                return;
            }
            this.mPrivateDnsFailCount++;
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
                extras.putBoolean("PrivDns", true ^ this.mIsBypassPrivateDns);
                intent.putExtras(extras);
                this.mContext.sendBroadcast(intent, CHR_BROADCAST_PERMISSION);
            }
        }
    }

    public NetworkRequest requestNetwork(NetworkCapabilities networkCapabilities, Messenger messenger, int timeoutMs, IBinder binder, int legacyType) {
        NetworkRequest request;
        if (!HwFrameworkFactory.getHwInnerTelephonyManager().isNrSlicesSupported() || (request = HwNetworkSliceManager.getInstance().requestNetworkSliceForNetworkCap(networkCapabilities, messenger, Binder.getCallingUid())) == null) {
            return HwConnectivityService.super.requestNetwork(networkCapabilities, messenger, timeoutMs, binder, legacyType);
        }
        log("HwConnectivityService - requestNetwork, request = " + request);
        return request;
    }
}
