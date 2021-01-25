package com.android.server;

import android.common.HwFrameworkFactory;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.hardware.usb.UsbManager;
import android.hdm.HwDeviceManager;
import android.net.ConnectivityManager;
import android.net.INetworkPolicyManager;
import android.net.INetworkStatsService;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.booster.IHwCommBoosterCallback;
import android.net.booster.IHwCommBoosterServiceManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.provider.Telephony;
import android.telephony.HwInnerTelephonyManagerImpl;
import android.telephony.HwTelephonyManager;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.appactcontrol.AppActConstant;
import com.android.server.connectivity.NetdEventListenerService;
import com.android.server.connectivity.NetworkAgentInfo;
import com.android.server.connectivity.Tethering;
import com.android.server.connectivity.tethering.TetheringConfiguration;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.intellicom.common.SmartDualCardConsts;
import com.android.server.intellicom.networkslice.HwNetworkSliceManager;
import com.android.server.intellicom.networkslice.css.NetworkSlicesHandler;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.android.app.admin.DeviceVpnManager;
import com.huawei.deliver.info.HwDeliverInfo;
import huawei.android.net.IConnectivityExManager;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class HwConnectivityManagerImpl implements HwConnectivityManager {
    private static final int ADD_NETWORK_ACCESS_LIST = 0;
    private static final int BINARY = 2;
    private static final int BLACK_LIST = 1;
    private static final String BOOT_PERMISSION = "android.permission.RECEIVE_BOOT_COMPLETED";
    private static final String CHR_BROADCAST_PERMISSION = "com.huawei.android.permission.GET_CHR_DATA";
    private static final int CHR_MAX_REPORT_APP_COUNT = 10;
    private static final int CODE_GET_CHR_UID_LIST = 1123;
    private static final int CODE_SET_NETWORK_ACCESS_LIST = 1106;
    public static final int CURRENT_CONNECT_TO_CELLULAR = 2;
    public static final int CURRENT_CONNECT_TO_WLAN = 1;
    private static final int DATA_SEND_TO_HWCM_DNS_COLLECT = 10;
    private static final boolean DBG = true;
    private static final String DESCRIPTOR_NETWORKMANAGEMENT_SERVICE = "android.os.INetworkManagementService";
    private static final String DISABLE_VPN = "disable-vpn";
    private static final int DNS_BIG_LATENCY = 2000;
    private static final int DNS_ERROR_IPV6_TIMEOUT = 15;
    private static final String DNS_EVENT_KEY_LANENCY = "latency";
    private static final String DNS_EVENT_KEY_NETID = "netid";
    private static final String DNS_EVENT_KEY_RETURNCODE = "returnCode";
    public static final String DNS_EVENT_KEY_UID = "uid";
    private static final int DNS_FAIL_REPORT_COUNT = 6;
    private static final int DNS_FAIL_REPORT_TIMESPAN = 45000;
    private static final int DNS_FAIL_REPORT_TIME_INTERVAL = 600000;
    private static final int DNS_FAIL_SAMPLING_RATIO = 8;
    private static final int DNS_LATENCY_1000 = 1000;
    private static final int DNS_LATENCY_150 = 150;
    private static final int DNS_LATENCY_20 = 20;
    private static final int DNS_LATENCY_500 = 500;
    private static final int DNS_OVER2000_REPORT_COUNT = 6;
    private static final int DNS_OVER2000_REPORT_TIMESPAN = 45000;
    private static final int DNS_OVER2000_REPORT_TIME_INTERVAL = 600000;
    private static final int DNS_OVER2000_SAMPLING_RATIO = 2;
    private static final int DNS_REPORT_COUNTING_INTERVAL = 100;
    private static final int DNS_REQUEST_MIN_DELAY_TIME = 8;
    private static int DNS_STAT_ENUM_ENDC = 1;
    private static int DNS_STAT_ENUM_LTE = 0;
    private static int DNS_STAT_ENUM_NRSA = 2;
    private static final int DNS_SUCCESS = 0;
    private static final int DNS_TIME_MIN = 10;
    private static final int DSQOE_START_DNS_MONITOR = 0;
    private static final int DSQOE_STOP_DNS_MONITOR = 1;
    private static final int EXIST_BLACK_AND_DOMAIN_NETWORK_POLICY_FLAG = 3;
    private static final int EXIST_BLACK_BUT_NOT_DOMAIN_NETWORK_POLICY_FLAG = 2;
    private static final int EXIST_WHITE_AND_DOMAIN_NETWORK_POLICY_FLAG = 1;
    private static final int EXIST_WHITE_BUT_NOT_DOMAIN_NETWORK_POLICY_FLAG = 0;
    protected static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final String HW_SYSTEM_SERVER_START = "com.huawei.systemserver.START";
    private static final boolean INIT_PDN_WIFI = SystemProperties.getBoolean("ro.config.forbid_roam_dun_wifi", false);
    private static final String INTENT_DS_DNS_STATISTICS = "com.intent.action.dns_statistics";
    private static final String INTENT_DS_WEB_STAT_REPORT = "com.huawei.intent.action.web_stat_report";
    private static final String INTENT_WIFI_DNS_STATISTICS = "com.intent.action.wifi_dns_statistics";
    private static final boolean IS_CHINA = "CN".equalsIgnoreCase(SystemProperties.get(WifiProCommonUtils.KEY_PROP_LOCALE, ""));
    private static final boolean IS_DOCOMO = (SystemProperties.get("ro.config.hw_opta", "").equals("341") && SystemProperties.get("ro.config.hw_optb", "").equals("392"));
    private static final boolean IS_NR_SLICES_SUPPORTED = HwFrameworkFactory.getHwInnerTelephonyManager().isNrSlicesSupported();
    private static final String KEY_NETWORK_POLICY_FLAG = "network_policy";
    private static final String LOG_TAG = "HwConnectivityManagerImpl";
    static final Uri MSIM_TELEPHONY_CARRIERS_URI = Uri.parse("content://telephony/carriers/subId");
    private static final int NETWORK_POLICY_NOT_SET = -1;
    public static final int NOT_CONNECT_TO_NETWORK = 0;
    private static final int NSA_STATE0 = 0;
    private static final int NSA_STATE1 = 1;
    private static final int NSA_STATE2 = 2;
    private static final int NSA_STATE3 = 3;
    private static final int NSA_STATE4 = 4;
    private static final int NSA_STATE5 = 5;
    private static final int NSA_STATE6 = 6;
    private static final int ON_DNS_EVENT_MSG = 0;
    private static final int ON_SET_IP_TABLES_MSG = 1;
    private static final String P2P_TETHER_IFAC = "p2p-wlan0-";
    private static final String P2P_TETHER_IFAC_110x = "p2p-p2p0-";
    private static final String P2P_TETHER_IFAC_QCOM = "p2p0";
    protected static final String PROPERTY_BTHOTSPOT_ON = "sys.isbthotspoton";
    protected static final String PROPERTY_USBTETHERING_ON = "sys.isusbtetheringon";
    protected static final String PROPERTY_WIFIHOTSPOT_ON = "sys.iswifihotspoton";
    private static final int PS_AP_SLOW_DNS_BIG_LATENCY = 6;
    private static final int PS_AP_SLOW_DNS_FAIL = 5;
    private static final String SECURE_VPN = "secure-vpn";
    public static final String SET_IP_TABLES_KEY_HOST_NAME = "hostName";
    public static final String SET_IP_TABLES_KEY_IP_COUNT = "ipCount";
    public static final String SET_IP_TABLES_KEY_IP_LIST = "ipList";
    private static final int SET_NETWORK_ACCESS_LIST = 1;
    private static final String TAG = null;
    private static final String VALID_PKGNAME_DNS = "com.android.server.dns";
    private static final boolean VDBG = false;
    private static final int WHITE_LIST = 0;
    private static final String WIFI_AP_MANUAL_CONNECT = "wifi_ap_manual_connect";
    private static int cellNetId = 0;
    private static HwConnectivityManager mInstance = new HwConnectivityManagerImpl();
    private static List<String> sNetworkListCacheOfDns = new ArrayList();
    private static int wifiNetId = 0;
    private DnsQueryStat cellEndcDnsStat = new DnsQueryStat("cell");
    private DnsQueryStat cellLteDnsStat = new DnsQueryStat("cell");
    private DnsQueryStat cellNrSaDnsStat = new DnsQueryStat("cell");
    private int[] mChrAppUidArray = new int[10];
    private ConnectivityManager mConnMgr = null;
    private int mConnectedType = 0;
    private final BroadcastReceiver mConnectivityChangeReceiver = new BroadcastReceiver() {
        /* class com.android.server.HwConnectivityManagerImpl.AnonymousClass3 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action;
            if (intent != null && (action = intent.getAction()) != null) {
                if (SmartDualCardConsts.ACTION_CONNECTIVITY_CHANGE.equals(action)) {
                    HwConnectivityManagerImpl.this.mConnMgr = (ConnectivityManager) context.getSystemService("connectivity");
                    if (HwConnectivityManagerImpl.this.mConnMgr != null) {
                        HwConnectivityManagerImpl hwConnectivityManagerImpl = HwConnectivityManagerImpl.this;
                        hwConnectivityManagerImpl.mNetworkInfoWlan = hwConnectivityManagerImpl.mConnMgr.getNetworkInfo(1);
                        HwConnectivityManagerImpl hwConnectivityManagerImpl2 = HwConnectivityManagerImpl.this;
                        hwConnectivityManagerImpl2.mNetworkInfoMobile = hwConnectivityManagerImpl2.mConnMgr.getNetworkInfo(0);
                        if (!(HwConnectivityManagerImpl.this.mNetworkInfoWlan == null || HwConnectivityManagerImpl.this.mNetworkInfoMobile == null)) {
                            if (HwConnectivityManagerImpl.this.mNetworkInfoWlan.isConnected()) {
                                HwConnectivityManagerImpl.this.mConnectedType = 1;
                            } else if (HwConnectivityManagerImpl.this.mNetworkInfoMobile.isConnected()) {
                                HwConnectivityManagerImpl.this.mConnectedType = 2;
                            } else {
                                HwConnectivityManagerImpl.this.mConnectedType = 0;
                            }
                        }
                        Network[] networks = HwConnectivityManagerImpl.this.mConnMgr.getAllNetworks();
                        for (Network network : networks) {
                            NetworkInfo networkInfo = HwConnectivityManagerImpl.this.mConnMgr.getNetworkInfo(network);
                            if (networkInfo != null) {
                                if (networkInfo.getType() == 1) {
                                    int unused = HwConnectivityManagerImpl.wifiNetId = network.netId;
                                }
                                if (networkInfo.getType() == 0) {
                                    int unused2 = HwConnectivityManagerImpl.cellNetId = network.netId;
                                }
                            }
                        }
                        HwConnectivityManagerImpl.this.clearInvalidPrivateDnsNetworkInfo();
                    }
                } else if ("com.huawei.systemserver.START".equals(action)) {
                    Log.d(HwConnectivityManagerImpl.LOG_TAG, "BroadcastReceiver booster");
                    HwConnectivityManagerImpl.this.initCommBoosterManager();
                }
            }
        }
    };
    private Context mContex = null;
    private DataServiceQoeDnsMonitor mDataServiceQoeDnsMonitor = null;
    private Handler mDnsEventHandler = null;
    private LinkedList<Date> mDnsFailQ = new LinkedList<>();
    private LinkedList<Date> mDnsOver2000Q = new LinkedList<>();
    private IHwCommBoosterServiceManager mHwCommBoosterServiceManager = null;
    private HwConnectivityService mHwConnectivityService = null;
    private IHwCommBoosterCallback mIHwCommBoosterCallback = new IHwCommBoosterCallback.Stub() {
        /* class com.android.server.HwConnectivityManagerImpl.AnonymousClass1 */

        public void callBack(int type, Bundle data) throws RemoteException {
            if (data == null) {
                Log.e(HwConnectivityManagerImpl.LOG_TAG, "data null");
            } else if (HwConnectivityManagerImpl.this.mDataServiceQoeDnsMonitor == null) {
                Log.e(HwConnectivityManagerImpl.LOG_TAG, "mDataServiceQoeDnsMonitor null");
            } else if (type == 10) {
                int action = data.getInt("action", -1);
                int timer = data.getInt("timer", 0);
                Log.d(HwConnectivityManagerImpl.LOG_TAG, "startappqoednscollection action =" + action + "timer =" + timer);
                if (action == 1) {
                    HwConnectivityManagerImpl.this.mDataServiceQoeDnsMonitor.stopDnsMonitor();
                }
                if (action == 0) {
                    HwConnectivityManagerImpl.this.mDataServiceQoeDnsMonitor.mTimer = timer;
                    HwConnectivityManagerImpl.this.mDataServiceQoeDnsMonitor.startDnsMonitor();
                }
            } else {
                Log.e(HwConnectivityManagerImpl.LOG_TAG, "appqoe_call back type null");
            }
        }
    };
    private NetworkInfo mNetworkInfoMobile = null;
    private NetworkInfo mNetworkInfoWlan = null;
    private boolean mSendDnsFailFlag = false;
    private boolean mSendDnsOver2000Flag = false;
    private NetworkSlicesHandler mSlicesHandler;
    private final DeviceVpnManager mVpnManager = new DeviceVpnManager();
    private DnsQueryStat wifiDnsStat = new DnsQueryStat("wifi");

    /* access modifiers changed from: private */
    public class DnsQueryStat {
        private String intentType = "";
        private int[] mDnsCntArray = new int[10];
        private int mDnsCount = 0;
        private int[] mDnsFailCntArray = new int[10];
        private int mDnsFailCount = 0;
        private int mDnsIpv6Timeout = 0;
        private int[] mDnsIpv6TimeoutArray = new int[10];
        private int mDnsResponse1000Count = 0;
        private int mDnsResponse150Count = 0;
        private int mDnsResponse2000Count = 0;
        private int mDnsResponse20Count = 0;
        private int mDnsResponse500Count = 0;
        private int mDnsResponseOver2000Count = 0;
        private int mDnsResponseTotalTime = 0;
        private int[] mDnsRsp1000CntArray = new int[10];
        private int[] mDnsRsp150CntArray = new int[10];
        private int[] mDnsRsp2000CntArray = new int[10];
        private int[] mDnsRsp20CntArray = new int[10];
        private int[] mDnsRsp500CntArray = new int[10];
        private int[] mDnsRspOver2000CntArray = new int[10];
        private int[] mDnsRspTotalTimeArray = new int[10];
        private int mDnsStatEnum = 0;
        private int[] mDnsUidArray = new int[10];
        private int mHicureDnsFailCount = 0;

        static /* synthetic */ int access$3008(DnsQueryStat x0) {
            int i = x0.mDnsCount;
            x0.mDnsCount = i + 1;
            return i;
        }

        static /* synthetic */ int access$3312(DnsQueryStat x0, int x1) {
            int i = x0.mDnsResponseTotalTime + x1;
            x0.mDnsResponseTotalTime = i;
            return i;
        }

        static /* synthetic */ int access$3408(DnsQueryStat x0) {
            int i = x0.mDnsResponse20Count;
            x0.mDnsResponse20Count = i + 1;
            return i;
        }

        static /* synthetic */ int access$3508(DnsQueryStat x0) {
            int i = x0.mDnsResponse150Count;
            x0.mDnsResponse150Count = i + 1;
            return i;
        }

        static /* synthetic */ int access$3608(DnsQueryStat x0) {
            int i = x0.mDnsResponse500Count;
            x0.mDnsResponse500Count = i + 1;
            return i;
        }

        static /* synthetic */ int access$3708(DnsQueryStat x0) {
            int i = x0.mDnsResponse1000Count;
            x0.mDnsResponse1000Count = i + 1;
            return i;
        }

        static /* synthetic */ int access$3808(DnsQueryStat x0) {
            int i = x0.mDnsResponse2000Count;
            x0.mDnsResponse2000Count = i + 1;
            return i;
        }

        static /* synthetic */ int access$3908(DnsQueryStat x0) {
            int i = x0.mDnsResponseOver2000Count;
            x0.mDnsResponseOver2000Count = i + 1;
            return i;
        }

        static /* synthetic */ int access$4008(DnsQueryStat x0) {
            int i = x0.mDnsIpv6Timeout;
            x0.mDnsIpv6Timeout = i + 1;
            return i;
        }

        static /* synthetic */ int access$4108(DnsQueryStat x0) {
            int i = x0.mDnsFailCount;
            x0.mDnsFailCount = i + 1;
            return i;
        }

        static /* synthetic */ int access$4208(DnsQueryStat x0) {
            int i = x0.mHicureDnsFailCount;
            x0.mHicureDnsFailCount = i + 1;
            return i;
        }

        DnsQueryStat(String connectedType) {
            if (connectedType.equals("cell")) {
                this.intentType = HwConnectivityManagerImpl.INTENT_DS_DNS_STATISTICS;
            } else {
                this.intentType = HwConnectivityManagerImpl.INTENT_WIFI_DNS_STATISTICS;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void resetAll() {
            this.mDnsCount = 0;
            this.mDnsFailCount = 0;
            this.mDnsIpv6Timeout = 0;
            this.mDnsResponse20Count = 0;
            this.mDnsResponse150Count = 0;
            this.mDnsResponse500Count = 0;
            this.mDnsResponse1000Count = 0;
            this.mDnsResponse2000Count = 0;
            this.mDnsResponseOver2000Count = 0;
            this.mDnsResponseTotalTime = 0;
            Arrays.fill(this.mDnsCntArray, 0);
            Arrays.fill(this.mDnsFailCntArray, 0);
            Arrays.fill(this.mDnsIpv6TimeoutArray, 0);
            Arrays.fill(this.mDnsRsp1000CntArray, 0);
            Arrays.fill(this.mDnsRsp150CntArray, 0);
            Arrays.fill(this.mDnsRsp2000CntArray, 0);
            Arrays.fill(this.mDnsRsp20CntArray, 0);
            Arrays.fill(this.mDnsRsp500CntArray, 0);
            Arrays.fill(this.mDnsRspOver2000CntArray, 0);
            Arrays.fill(this.mDnsRspTotalTimeArray, 0);
            Arrays.fill(this.mDnsUidArray, 0);
        }
    }

    public ConnectivityService createHwConnectivityService(Context context, INetworkManagementService netd, INetworkStatsService statsService, INetworkPolicyManager policyManager) {
        this.mContex = context;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SmartDualCardConsts.ACTION_CONNECTIVITY_CHANGE);
        intentFilter.addAction("com.huawei.systemserver.START");
        this.mContex.registerReceiver(this.mConnectivityChangeReceiver, intentFilter, BOOT_PERMISSION, null);
        initDnsEventHandler();
        this.mDataServiceQoeDnsMonitor = new DataServiceQoeDnsMonitor();
        this.mDataServiceQoeDnsMonitor.init();
        this.mHwConnectivityService = new HwConnectivityService(context, netd, statsService, policyManager);
        if (IS_NR_SLICES_SUPPORTED) {
            this.mSlicesHandler = HwNetworkSliceManager.getInstance().getHandler();
        }
        return this.mHwConnectivityService;
    }

    public static HwConnectivityManager getDefault() {
        return mInstance;
    }

    public void setPushServicePowerNormalMode() {
    }

    public boolean setPushServicePowerSaveMode(NetworkInfo networkInfo) {
        return true;
    }

    public void setTetheringProp(Tethering tetheringService, boolean tethering, boolean usb, String ifaceName) {
        Log.d(TAG, "enter setTetheringProp");
        String prop = tethering ? AppActConstant.VALUE_TRUE : AppActConstant.VALUE_FALSE;
        try {
            TetheringConfiguration cfg = tetheringService.getTetheringConfiguration();
            if (usb) {
                SystemProperties.set(PROPERTY_USBTETHERING_ON, prop);
                String str = TAG;
                Log.d(str, "set PROPERTY_USBTETHERING_ON: " + prop);
            } else if (cfg != null && cfg.isWifi(ifaceName)) {
                SystemProperties.set(PROPERTY_WIFIHOTSPOT_ON, prop);
                String str2 = TAG;
                Log.d(str2, "set iswifihotspoton = " + prop);
            } else if (cfg != null && cfg.isBluetooth(ifaceName)) {
                SystemProperties.set(PROPERTY_BTHOTSPOT_ON, prop);
                String str3 = TAG;
                Log.d(str3, "set isbthotspoton = " + prop);
            }
        } catch (RuntimeException e) {
            String str4 = TAG;
            Log.e(str4, "when setTetheringProp ,error =" + e + "  ifaceNmae =" + ifaceName);
        }
    }

    private boolean isFromDocomo(Context context) {
        if (IS_DOCOMO && !TextUtils.isEmpty(Settings.Global.getString(context.getContentResolver(), "tether_dun_apn"))) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:43:0x016b, code lost:
        if (r9 == null) goto L_0x0199;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x016d, code lost:
        r9.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x0190, code lost:
        if (0 == 0) goto L_0x0199;
     */
    public boolean checkDunExisted(Context mContext) {
        String operator;
        SystemProperties.getBoolean("ro.config.enable.gdun", false);
        if (this.mHwConnectivityService == null) {
            Log.d(TAG, "mHwConnectivityService == null ,return false");
            return false;
        }
        Log.d(TAG, "isSystemBootComplete =" + this.mHwConnectivityService.isSystemBootComplete());
        if (!this.mHwConnectivityService.isSystemBootComplete()) {
            return false;
        }
        if (isFromDocomo(mContext)) {
            return true;
        }
        int subId = SubscriptionManager.getDefaultDataSubscriptionId();
        int type = TelephonyManager.getDefault().getCurrentPhoneType(subId);
        Log.d(TAG, " type:" + type + " subId = " + subId);
        if (type == 1) {
            String operator2 = TelephonyManager.getDefault().getSimOperator(subId);
            Log.d(TAG, " operator:" + operator2);
            operator = operator2;
        } else {
            operator = HwInnerTelephonyManagerImpl.getDefault().getOperatorNumeric();
        }
        if (operator != null) {
            String[] projection = {"type", "proxy", "port"};
            String selection = "numeric = '" + operator + "' and carrier_enabled = 1";
            Cursor cursor = null;
            try {
                if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                    Uri uri = Uri.withAppendedPath(MSIM_TELEPHONY_CARRIERS_URI, Long.toString((long) subId));
                    cursor = mContext.getContentResolver().query(uri, projection, selection, null, null);
                    if (HWFLOW) {
                        Log.d(TAG, "Read DB '" + uri);
                    }
                } else {
                    cursor = mContext.getContentResolver().query(Telephony.Carriers.CONTENT_URI, projection, selection, null, null);
                    if (HWFLOW) {
                        Log.d(TAG, "Read DB '" + Telephony.Carriers.CONTENT_URI);
                    }
                }
                if (cursor != null && cursor.moveToFirst()) {
                    while (!cursor.getString(cursor.getColumnIndexOrThrow("type")).contains("dun")) {
                        if (!cursor.moveToNext()) {
                        }
                    }
                    if (!INIT_PDN_WIFI || TelephonyManager.getDefault() == null || !TelephonyManager.getDefault().isNetworkRoaming()) {
                        cursor.close();
                        return true;
                    }
                    cursor.close();
                    return false;
                }
            } catch (Exception e) {
                Log.d(TAG, "Read DB '" + Telephony.Carriers.CONTENT_URI + "' failed");
            } catch (Throwable th) {
                if (0 != 0) {
                    cursor.close();
                }
                throw th;
            }
        }
        return false;
    }

    public boolean setUsbFunctionForTethering(Context context, UsbManager usbManager, boolean enable) {
        if (!HwDeliverInfo.isIOTVersion() || !SystemProperties.getBoolean("ro.config.persist_usb_tethering", false)) {
            return false;
        }
        String str = TAG;
        Log.d(str, "tethering setCurrentFunction rndis,serial " + enable);
        if (enable) {
            if (usbManager != null) {
                usbManager.setCurrentFunction("rndis,serial", false);
            }
            Settings.Secure.putInt(context.getContentResolver(), "usb_tethering_on", 1);
        } else {
            Settings.Secure.putInt(context.getContentResolver(), "usb_tethering_on", 0);
        }
        return true;
    }

    public void captivePortalCheckCompleted(Context context, boolean isCaptivePortal) {
        if (!isCaptivePortal && 1 == Settings.System.getInt(context.getContentResolver(), WIFI_AP_MANUAL_CONNECT, 0)) {
            Settings.System.putInt(context.getContentResolver(), WIFI_AP_MANUAL_CONNECT, 0);
            Log.d(LOG_TAG, "not portal ap manual connect");
        }
    }

    private URL makeUrl(String url) {
        if (url == null) {
            return null;
        }
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Bad URL: " + url);
            return null;
        }
    }

    public void startBrowserOnClickNotification(Context context, String url) {
        URL urlExtre;
        if (url != null && (urlExtre = makeUrl(url)) != null) {
            Log.d(LOG_TAG, "startBrowserOnClickNotification url: " + urlExtre.getHost());
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.setFlags(272629760);
            intent.putExtra(WifiProCommonUtils.BROWSER_LAUNCH_FROM, WifiProCommonUtils.BROWSER_LAUNCHED_BY_WIFI_PORTAL);
            intent.putExtra("com.huawei.browser.cct_url", Uri.parse(url));
            intent.putExtra("com.huawei.browser.when_about_blank_close", true);
            intent.putExtra("com.android.browser.application_id", WifiProCommonUtils.BROWSER_LAUNCHED_BY_WIFI_PORTAL);
            try {
                intent.setData(Uri.parse("https://wifi_portal"));
                intent.setPackage("com.huawei.browser");
                Bundle bundle = new Bundle();
                bundle.putIBinder("android.support.customtabs.extra.SESSION", null);
                intent.putExtras(bundle);
                context.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Log.d(LOG_TAG, "browser not exist..");
                if (intent.getPackage() != null) {
                    intent.setData(Uri.parse(url));
                    intent.setPackage(null);
                    try {
                        context.startActivity(intent);
                    } catch (ActivityNotFoundException e2) {
                        Log.e(LOG_TAG, "browser failed");
                    }
                }
            }
        }
    }

    public Network getNetworkForTypeWifi() {
        HwConnectivityService hwConnectivityService = this.mHwConnectivityService;
        if (hwConnectivityService != null) {
            return hwConnectivityService.getNetworkForTypeWifi();
        }
        return null;
    }

    public boolean isP2pTether(String iface) {
        if (iface == null) {
            return false;
        }
        if (iface.startsWith(P2P_TETHER_IFAC) || iface.startsWith(P2P_TETHER_IFAC_110x) || iface.startsWith(P2P_TETHER_IFAC_QCOM)) {
            return true;
        }
        return false;
    }

    public void stopP2pTether(Context context) {
        if (context != null) {
            WifiP2pManager.Channel channel = null;
            WifiP2pManager.ActionListener mWifiP2pBridgeCreateListener = new WifiP2pManager.ActionListener() {
                /* class com.android.server.HwConnectivityManagerImpl.AnonymousClass2 */

                @Override // android.net.wifi.p2p.WifiP2pManager.ActionListener
                public void onSuccess() {
                    Log.d(HwConnectivityManagerImpl.TAG, " Stop p2p tether success");
                }

                @Override // android.net.wifi.p2p.WifiP2pManager.ActionListener
                public void onFailure(int reason) {
                    String str = HwConnectivityManagerImpl.TAG;
                    Log.e(str, " Stop p2p tether fail:" + reason);
                }
            };
            WifiP2pManager wifiP2pManager = (WifiP2pManager) context.getSystemService("wifip2p");
            if (wifiP2pManager != null) {
                channel = wifiP2pManager.initialize(context, context.getMainLooper(), null);
            }
            if (channel != null) {
                wifiP2pManager.removeGroup(channel, mWifiP2pBridgeCreateListener);
            }
        }
    }

    /* JADX INFO: finally extract failed */
    public boolean isVpnDisabled() {
        long ident = Binder.clearCallingIdentity();
        try {
            boolean allow = this.mVpnManager.isVpnDisabled((ComponentName) null);
            Binder.restoreCallingIdentity(ident);
            String str = TAG;
            Log.d(str, "isVpnDisabled and result is " + allow);
            return allow;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
            throw th;
        }
    }

    /* JADX INFO: finally extract failed */
    public boolean isInsecureVpnDisabled() {
        long ident = Binder.clearCallingIdentity();
        try {
            boolean allow = this.mVpnManager.isInsecureVpnDisabled((ComponentName) null);
            Binder.restoreCallingIdentity(ident);
            String str = TAG;
            Log.d(str, "isInsecureVpnDisabled and result is " + allow);
            return allow;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
            throw th;
        }
    }

    private DnsQueryStat getCellNrDnsStat(int nsaState) {
        switch (nsaState) {
            case 0:
            case 6:
                return this.cellNrSaDnsStat;
            case 1:
            case 2:
            case 3:
            case 4:
                return this.cellLteDnsStat;
            case 5:
                return this.cellEndcDnsStat;
            default:
                return null;
        }
    }

    private boolean isNsaState(int state) {
        if (2 > state || state > 5) {
            return false;
        }
        return true;
    }

    private boolean isInService(int state) {
        if (state == 0) {
            return true;
        }
        return false;
    }

    private DnsQueryStat getCellDnsStat() {
        int subId = SubscriptionManager.getDefaultDataSubscriptionId();
        int networkType = TelephonyManager.getDefault().getNetworkType(subId);
        ServiceState ss = TelephonyManager.getDefault().getServiceStateForSubscriber(subId);
        int nsaState = 0;
        if (ss != null) {
            nsaState = ss.getNsaState();
            if (isNsaState(nsaState) && isInService(ss.getDataRegState())) {
                networkType = ss.getConfigRadioTechnology();
            }
        }
        this.cellLteDnsStat.mDnsStatEnum = DNS_STAT_ENUM_LTE;
        this.cellEndcDnsStat.mDnsStatEnum = DNS_STAT_ENUM_ENDC;
        this.cellNrSaDnsStat.mDnsStatEnum = DNS_STAT_ENUM_NRSA;
        if (networkType == 13 || networkType == 19) {
            return this.cellLteDnsStat;
        }
        if (networkType != 20) {
            return null;
        }
        return getCellNrDnsStat(nsaState);
    }

    private DnsQueryStat getDnsStat(int netId) {
        if (netId != cellNetId) {
            return this.wifiDnsStat;
        }
        return getCellDnsStat();
    }

    private void initDnsEventHandler() {
        Looper mainLooper;
        Context context = this.mContex;
        if (context != null && (mainLooper = context.getMainLooper()) != null) {
            this.mDnsEventHandler = new DnsEventHandler(mainLooper);
        }
    }

    /* access modifiers changed from: private */
    public class DnsEventHandler extends Handler {
        public DnsEventHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i != 0) {
                if (i == 1) {
                    Bundle bundleData = msg.getData();
                    if (bundleData == null) {
                        Log.e(HwConnectivityManagerImpl.LOG_TAG, "set ip tables data is null");
                        return;
                    }
                    String hostName = bundleData.getString("hostName");
                    List<String> ipAddresses = new ArrayList<>();
                    try {
                        ipAddresses = bundleData.getStringArrayList("ipList");
                    } catch (ArrayIndexOutOfBoundsException e) {
                        Log.e(HwConnectivityManagerImpl.TAG, "ArrayIndexOutOfBoundsException");
                    }
                    HwConnectivityManagerImpl.this.setIpRulesOfDnsEventAction(hostName, ipAddresses, bundleData.getInt("ipCount"));
                }
            } else if (msg.obj instanceof Context) {
                Context context = (Context) msg.obj;
                Bundle data = msg.getData();
                if (data == null) {
                    Log.e(HwConnectivityManagerImpl.LOG_TAG, "DNS event data is null");
                    return;
                }
                int returnCode = data.getInt(HwConnectivityManagerImpl.DNS_EVENT_KEY_RETURNCODE);
                int latencyMs = data.getInt(HwConnectivityManagerImpl.DNS_EVENT_KEY_LANENCY);
                int netId = data.getInt(HwConnectivityManagerImpl.DNS_EVENT_KEY_NETID);
                int uid = data.getInt("uid");
                NetdEventListenerService.updateUidDnsFailCount(uid);
                HwConnectivityManagerImpl.this.onDnsEventForNrSlice(returnCode, data);
                HwConnectivityManagerImpl.this.onDnsUidEventAction(context, returnCode, latencyMs, netId, uid);
                HwConnectivityManagerImpl.this.onDnsEventAction(context, returnCode, latencyMs, netId);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onDnsEventForNrSlice(int returnCode, Bundle data) {
        NetworkSlicesHandler networkSlicesHandler;
        if (IS_NR_SLICES_SUPPORTED && returnCode == 0 && (networkSlicesHandler = this.mSlicesHandler) != null) {
            Message msg = networkSlicesHandler.obtainMessage(1);
            msg.setData(data);
            msg.sendToTarget();
        }
    }

    public void onDnsEvent(Context context, Bundle bundle) {
        Handler handler = this.mDnsEventHandler;
        if (handler != null) {
            Message setIpTablesMsg = handler.obtainMessage(1);
            setIpTablesMsg.setData(bundle);
            this.mDnsEventHandler.sendMessageAtFrontOfQueue(setIpTablesMsg);
            Message onDnsEventmsg = this.mDnsEventHandler.obtainMessage(0, context);
            onDnsEventmsg.setData(bundle);
            this.mDnsEventHandler.sendMessage(onDnsEventmsg);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onDnsUidEventAction(Context context, int returnCode, int latencyMs, int netId, int uid) {
        DnsQueryStat dnsStat;
        if (netId == wifiNetId && (dnsStat = getDnsStat(netId)) != null) {
            int uidFoundIdx = 0;
            boolean uidFound = false;
            getChrUidList();
            int uidIdx = 0;
            while (true) {
                if (uidIdx >= 10) {
                    break;
                } else if (this.mChrAppUidArray[uidIdx] == uid) {
                    dnsStat.mDnsUidArray[uidIdx] = uid;
                    uidFoundIdx = uidIdx;
                    uidFound = true;
                    break;
                } else {
                    uidIdx++;
                }
            }
            if (uidFound) {
                if (returnCode == 0) {
                    int[] iArr = dnsStat.mDnsRspTotalTimeArray;
                    iArr[uidFoundIdx] = iArr[uidFoundIdx] + latencyMs;
                    if (latencyMs > 2000) {
                        int[] iArr2 = dnsStat.mDnsRspOver2000CntArray;
                        iArr2[uidFoundIdx] = iArr2[uidFoundIdx] + 1;
                    } else if (latencyMs <= 20) {
                        int[] iArr3 = dnsStat.mDnsRsp20CntArray;
                        iArr3[uidFoundIdx] = iArr3[uidFoundIdx] + 1;
                    } else if (latencyMs <= 150) {
                        int[] iArr4 = dnsStat.mDnsRsp150CntArray;
                        iArr4[uidFoundIdx] = iArr4[uidFoundIdx] + 1;
                    } else if (latencyMs <= 500) {
                        int[] iArr5 = dnsStat.mDnsRsp500CntArray;
                        iArr5[uidFoundIdx] = iArr5[uidFoundIdx] + 1;
                    } else if (latencyMs <= 1000) {
                        int[] iArr6 = dnsStat.mDnsRsp1000CntArray;
                        iArr6[uidFoundIdx] = iArr6[uidFoundIdx] + 1;
                    } else {
                        int[] iArr7 = dnsStat.mDnsRsp2000CntArray;
                        iArr7[uidFoundIdx] = iArr7[uidFoundIdx] + 1;
                    }
                } else {
                    int[] iArr8 = dnsStat.mDnsFailCntArray;
                    iArr8[uidFoundIdx] = iArr8[uidFoundIdx] + 1;
                    if (returnCode == 15) {
                        int[] iArr9 = dnsStat.mDnsIpv6TimeoutArray;
                        iArr9[uidFoundIdx] = iArr9[uidFoundIdx] + 1;
                    }
                }
                int[] iArr10 = dnsStat.mDnsCntArray;
                iArr10[uidFoundIdx] = iArr10[uidFoundIdx] + 1;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onDnsEventAction(Context context, int returnCode, int latencyMs, int netId) {
        DnsQueryStat mDnsStat;
        if ((netId == wifiNetId || netId == cellNetId) && (mDnsStat = getDnsStat(netId)) != null) {
            if (returnCode == 0) {
                onDnsReportSuccProcess(context, mDnsStat, latencyMs, netId);
            } else {
                onDnsReportFailProcess(context, mDnsStat, returnCode, latencyMs, netId);
            }
            DnsQueryStat.access$3008(mDnsStat);
            if (mDnsStat.mDnsCount == 100) {
                sendIntentDnsEvent(context, netId, mDnsStat);
            }
            HwConnectivityService hwConnectivityService = this.mHwConnectivityService;
            if (hwConnectivityService != null) {
                hwConnectivityService.recordPrivateDnsEvent(context, returnCode, latencyMs, netId);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setIpRulesOfDnsEventAction(String hostName, List<String> ipAddresses, int ipAddressesCount) {
        int networkPolicyFlag;
        if (ipAddresses != null && !ipAddresses.isEmpty() && (networkPolicyFlag = getNetworkPolicyFlag()) != -1) {
            if (networkPolicyFlag == 1 || networkPolicyFlag == 0) {
                setWhiteRulesToIptables(hostName, ipAddresses, networkPolicyFlag);
            } else if (networkPolicyFlag == 2 || networkPolicyFlag == 3) {
                setBlackRulesToIptables(hostName, ipAddresses, networkPolicyFlag);
            } else {
                Log.d(LOG_TAG, "hasNetworkPolicyList error");
            }
        }
    }

    public void clearIpCacheOfDnsEvent() {
        sNetworkListCacheOfDns.clear();
    }

    private void onDnsReportSuccProcess(Context context, DnsQueryStat dnsStat, int latencyMs, int netId) {
        DataServiceQoeDnsMonitor dataServiceQoeDnsMonitor = this.mDataServiceQoeDnsMonitor;
        if (dataServiceQoeDnsMonitor != null) {
            DataServiceQoeDnsMonitor.access$3108(dataServiceQoeDnsMonitor);
            DataServiceQoeDnsMonitor.access$3212(this.mDataServiceQoeDnsMonitor, latencyMs);
            DnsQueryStat.access$3312(dnsStat, latencyMs);
            if (latencyMs > 8 && netId == cellNetId) {
                if (!this.mSendDnsOver2000Flag && this.mDnsOver2000Q.size() > 0) {
                    this.mDnsOver2000Q.clear();
                }
                if (!this.mSendDnsFailFlag && this.mDnsFailQ.size() > 0) {
                    this.mDnsFailQ.clear();
                }
            }
            if (latencyMs > 2000) {
                DnsQueryStat.access$3908(dnsStat);
                sendIntentPsSlowDnsOver2000(context, latencyMs, netId);
            } else if (latencyMs <= 20) {
                DnsQueryStat.access$3408(dnsStat);
            } else if (latencyMs <= 150) {
                DnsQueryStat.access$3508(dnsStat);
            } else if (latencyMs <= 500) {
                DnsQueryStat.access$3608(dnsStat);
            } else if (latencyMs <= 1000) {
                DnsQueryStat.access$3708(dnsStat);
            } else {
                DnsQueryStat.access$3808(dnsStat);
            }
        }
    }

    private void onDnsReportFailProcess(Context context, DnsQueryStat dnsStat, int returnCode, int latencyMs, int netId) {
        if (this.mDataServiceQoeDnsMonitor != null) {
            if (returnCode == 15) {
                DnsQueryStat.access$4008(dnsStat);
            }
            DnsQueryStat.access$4108(dnsStat);
            DnsQueryStat.access$4208(dnsStat);
            DataServiceQoeDnsMonitor.access$4308(this.mDataServiceQoeDnsMonitor);
            sendIntentPsSlowDnsFail(context, returnCode, latencyMs, netId);
            if (netId == cellNetId) {
                SystemProperties.set("hw.hicure.dns_fail_count", "" + dnsStat.mHicureDnsFailCount);
                return;
            }
            SystemProperties.set("hw.wifipro.dns_fail_count", "" + dnsStat.mDnsFailCount);
        }
    }

    public boolean isBypassPrivateDns(int netId) {
        HwConnectivityService hwConnectivityService = this.mHwConnectivityService;
        if (hwConnectivityService != null) {
            return hwConnectivityService.isBypassPrivateDns(netId);
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void clearInvalidPrivateDnsNetworkInfo() {
        HwConnectivityService hwConnectivityService = this.mHwConnectivityService;
        if (hwConnectivityService != null) {
            hwConnectivityService.clearInvalidPrivateDnsNetworkInfo();
        }
    }

    private void sendIntentPsSlowDnsFail(Context context, int returnCode, int latencyMs, int netId) {
        if (netId == cellNetId) {
            Date now = new Date();
            Log.d(LOG_TAG, " sendIntentPsSlowDnsFail mSendDnsFailFlag = " + this.mSendDnsFailFlag + "mDnsFailQ.size() = " + this.mDnsFailQ.size());
            if (!this.mSendDnsFailFlag) {
                this.mDnsFailQ.addLast(now);
                if (this.mDnsFailQ.size() == 6) {
                    if (now.getTime() - this.mDnsFailQ.getFirst().getTime() <= 45000) {
                        this.mSendDnsFailFlag = true;
                        if (this.mConnectedType == 2) {
                            Intent chrIntent = new Intent(INTENT_DS_WEB_STAT_REPORT);
                            chrIntent.putExtra("ReportType", 5);
                            chrIntent.putExtra("WebFailCode", returnCode);
                            chrIntent.putExtra("WebDelay", latencyMs);
                            context.sendBroadcast(chrIntent, CHR_BROADCAST_PERMISSION);
                            Log.d(LOG_TAG, " sendIntentPsSlowDnsFail");
                        }
                    }
                    this.mDnsFailQ.removeFirst();
                }
            } else if (this.mDnsFailQ.isEmpty() || now.getTime() - this.mDnsFailQ.getLast().getTime() > Constant.MAX_TRAIN_MODEL_TIME) {
                Log.d(LOG_TAG, " sendIntentPsSlowDnsFail reset mSendDnsFailFlag");
                this.mSendDnsFailFlag = false;
                this.mDnsFailQ.clear();
                this.mDnsFailQ.addLast(now);
            }
        }
    }

    private void sendIntentPsSlowDnsOver2000(Context context, int delay, int netId) {
        if (netId == cellNetId) {
            Date now = new Date();
            Log.d(LOG_TAG, " sendIntentPsSlowDnsOver2000 mSendDnsOver2000Flag = " + this.mSendDnsOver2000Flag + "mDnsOver2000Q.size() = " + this.mDnsOver2000Q.size());
            if (!this.mSendDnsOver2000Flag) {
                this.mDnsOver2000Q.addLast(now);
                if (this.mDnsOver2000Q.size() == 6) {
                    if (now.getTime() - this.mDnsOver2000Q.getFirst().getTime() <= 45000) {
                        this.mSendDnsOver2000Flag = true;
                        if (this.mConnectedType == 2) {
                            Intent chrIntent = new Intent(INTENT_DS_WEB_STAT_REPORT);
                            chrIntent.putExtra("ReportType", 6);
                            chrIntent.putExtra("WebDelay", delay);
                            context.sendBroadcast(chrIntent, CHR_BROADCAST_PERMISSION);
                            Log.d(LOG_TAG, " sendIntentPsSlowDnsOver2000");
                        }
                    }
                    this.mDnsOver2000Q.removeFirst();
                }
            } else if (this.mDnsOver2000Q.isEmpty() || now.getTime() - this.mDnsOver2000Q.getLast().getTime() > Constant.MAX_TRAIN_MODEL_TIME) {
                Log.d(LOG_TAG, " sendIntentPsSlowDnsOver2000 reset mSendDnsOver2000Flag");
                this.mSendDnsOver2000Flag = false;
                this.mDnsOver2000Q.clear();
                this.mDnsOver2000Q.addLast(now);
            }
        }
    }

    private void sendIntentDnsEvent(Context context, int netId, DnsQueryStat resDnsStat) {
        Log.d(LOG_TAG, "sendIntentDnsEvent connectType = " + this.mConnectedType);
        Log.d(LOG_TAG, "sendIntentDnsEvent mDnsCount:" + resDnsStat.mDnsCount + "mDnsIpv6Timeout:" + resDnsStat.mDnsIpv6Timeout + "mDnsResponseTotalTime:" + resDnsStat.mDnsResponseTotalTime + "mDnsFailCount:" + resDnsStat.mDnsFailCount + "mDnsResponse20Count:" + resDnsStat.mDnsResponse20Count + "mDnsResponse150Count:" + resDnsStat.mDnsResponse150Count + "mDnsResponse500Count:" + resDnsStat.mDnsResponse500Count + "mDnsResponse1000Count:" + resDnsStat.mDnsResponse1000Count + "mDnsResponse2000Count:" + resDnsStat.mDnsResponse2000Count + "mDnsResponseOver2000Count:" + resDnsStat.mDnsResponseOver2000Count);
        Bundle extras = new Bundle();
        extras.putInt("dnsCount", resDnsStat.mDnsCount);
        extras.putInt("dnsIpv6Timeout", resDnsStat.mDnsIpv6Timeout);
        extras.putInt("dnsResponseTotalTime", resDnsStat.mDnsResponseTotalTime);
        extras.putInt("dnsFailCount", resDnsStat.mDnsFailCount);
        extras.putInt("dnsResponse20Count", resDnsStat.mDnsResponse20Count);
        extras.putInt("dnsResponse150Count", resDnsStat.mDnsResponse150Count);
        extras.putInt("dnsResponse500Count", resDnsStat.mDnsResponse500Count);
        extras.putInt("dnsResponse1000Count", resDnsStat.mDnsResponse1000Count);
        extras.putInt("dnsResponse2000Count", resDnsStat.mDnsResponse2000Count);
        extras.putInt("dnsResponseOver2000Count", resDnsStat.mDnsResponseOver2000Count);
        for (int index = 0; index < 10; index++) {
            if (resDnsStat.mDnsUidArray[index] != 0) {
                extras.putInt(String.format(Locale.ENGLISH, "App%dUid", Integer.valueOf(index + 1)), resDnsStat.mDnsUidArray[index]);
                extras.putInt(String.format(Locale.ENGLISH, "App%dDnsCnt", Integer.valueOf(index + 1)), resDnsStat.mDnsCntArray[index]);
                extras.putInt(String.format(Locale.ENGLISH, "App%dDnsTotalTime", Integer.valueOf(index + 1)), resDnsStat.mDnsRspTotalTimeArray[index]);
                extras.putInt(String.format(Locale.ENGLISH, "App%dDnsIpv6Timeout", Integer.valueOf(index + 1)), resDnsStat.mDnsIpv6TimeoutArray[index]);
                extras.putInt(String.format(Locale.ENGLISH, "App%dDnsFailCnt", Integer.valueOf(index + 1)), resDnsStat.mDnsFailCntArray[index]);
                extras.putInt(String.format(Locale.ENGLISH, "App%dDnsRsp20Cnt", Integer.valueOf(index + 1)), resDnsStat.mDnsRsp20CntArray[index]);
                extras.putInt(String.format(Locale.ENGLISH, "App%dDnsRsp150Cnt", Integer.valueOf(index + 1)), resDnsStat.mDnsRsp150CntArray[index]);
                extras.putInt(String.format(Locale.ENGLISH, "App%dDnsRsp500Cnt", Integer.valueOf(index + 1)), resDnsStat.mDnsRsp500CntArray[index]);
                extras.putInt(String.format(Locale.ENGLISH, "App%dDnsRsp1000Cnt", Integer.valueOf(index + 1)), resDnsStat.mDnsRsp1000CntArray[index]);
                extras.putInt(String.format(Locale.ENGLISH, "App%dDnsRsp2000Cnt", Integer.valueOf(index + 1)), resDnsStat.mDnsRsp2000CntArray[index]);
                extras.putInt(String.format(Locale.ENGLISH, "App%dDnsRspOver2000Cnt", Integer.valueOf(index + 1)), resDnsStat.mDnsRspOver2000CntArray[index]);
            }
        }
        extras.putInt("dnsStatEnum", resDnsStat.mDnsStatEnum);
        Intent intent = new Intent(resDnsStat.intentType);
        intent.putExtras(extras);
        resDnsStat.resetAll();
        context.sendBroadcast(intent, CHR_BROADCAST_PERMISSION);
    }

    public boolean needCaptivePortalCheck(NetworkAgentInfo nai, Context context) {
        int subId = SubscriptionManager.getDefaultDataSubscriptionId();
        if (!SubscriptionManager.isUsableSubIdValue(subId) || context == null || TelephonyManager.getDefault() == null) {
            Log.e(LOG_TAG, "needCaptivePortal: subId =" + subId + " is Invalid, or context is null,return false.");
            return false;
        }
        int deviceProvisioned = Settings.Global.getInt(context.getContentResolver(), "device_provisioned", 0);
        if (deviceProvisioned != 0) {
            Log.e(LOG_TAG, "needCaptivePortal: deviceProvisioned=" + deviceProvisioned + ", return false.");
            return false;
        } else if (nai == null || nai.networkInfo == null || nai.networkInfo.getType() != 0) {
            Log.e(LOG_TAG, "needCaptivePortal: NetworkAgentInfo is not Mobile Type,return false.");
            return false;
        } else {
            String simOperator = TelephonyManager.getDefault().getSimOperator(subId);
            if (TextUtils.isEmpty(simOperator)) {
                return false;
            }
            String plmnConfig = Settings.System.getString(context.getContentResolver(), "need_captive_portal_by_hplmn");
            if (TextUtils.isEmpty(plmnConfig)) {
                return false;
            }
            for (String plmn : plmnConfig.split(",")) {
                if (simOperator.equals(plmn)) {
                    Log.d(LOG_TAG, "needCaptivePortalCheck return true for simOperator=" + simOperator);
                    return true;
                }
            }
            return false;
        }
    }

    public void informModemTetherStatusToChangeGRO(int enable, String faceName) {
        HwTelephonyManager.getDefault().informModemTetherStatusToChangeGRO(enable, faceName);
    }

    public int getNetworkAgentInfoScore() {
        ConnectivityManager connectivityManager = this.mConnMgr;
        if (connectivityManager == null) {
            return 0;
        }
        Network[] networks = connectivityManager.getAllNetworks();
        int netId = 0;
        for (Network network : networks) {
            NetworkInfo networkInfo = this.mConnMgr.getNetworkInfo(network);
            if (networkInfo != null && networkInfo.getType() == 1) {
                netId = network.netId;
            }
        }
        HwConnectivityService hwConnectivityService = this.mHwConnectivityService;
        if (hwConnectivityService == null || hwConnectivityService.getNetworkAgentInfoForNetIdHw(netId) == null) {
            return 0;
        }
        int score = this.mHwConnectivityService.getNetworkAgentInfoForNetIdHw(netId).getCurrentScore();
        Log.i(TAG, "score = " + score + ", netId = " + netId);
        return score;
    }

    public boolean isApIpv4AddressFixed() {
        Log.d(LOG_TAG, "Get isApIpv4AddressFixed");
        IConnectivityExManager sService = IConnectivityExManager.Stub.asInterface(ServiceManager.getService("hwConnectivityExService"));
        if (sService == null) {
            return false;
        }
        try {
            return sService.isApIpv4AddressFixed();
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "RemoteException.");
            return false;
        }
    }

    public void setApIpv4AddressFixed(boolean isFixed) {
        Log.d(LOG_TAG, "setApIpv4AddressFixed: " + isFixed);
        IConnectivityExManager sService = IConnectivityExManager.Stub.asInterface(ServiceManager.getService("hwConnectivityExService"));
        if (sService != null) {
            try {
                sService.setApIpv4AddressFixed(isFixed);
            } catch (RemoteException e) {
                Log.e(LOG_TAG, "RemoteException.");
            }
        }
    }

    /* access modifiers changed from: private */
    public class DataServiceQoeDnsMonitor {
        private static final int EVENT_DNS_MONITOR_TIMER = 1;
        private static final int HWCM_DNS_INFO = 801;
        private int mDnsFailCount;
        private int mDnsLatencyMs;
        private int mDnsSucceedCount;
        private MyHandler mHandler;
        private HandlerThread mHandlerThread;
        private int mTimer;

        static /* synthetic */ int access$3108(DataServiceQoeDnsMonitor x0) {
            int i = x0.mDnsSucceedCount;
            x0.mDnsSucceedCount = i + 1;
            return i;
        }

        static /* synthetic */ int access$3212(DataServiceQoeDnsMonitor x0, int x1) {
            int i = x0.mDnsLatencyMs + x1;
            x0.mDnsLatencyMs = i;
            return i;
        }

        static /* synthetic */ int access$4308(DataServiceQoeDnsMonitor x0) {
            int i = x0.mDnsFailCount;
            x0.mDnsFailCount = i + 1;
            return i;
        }

        private DataServiceQoeDnsMonitor() {
            this.mHandler = null;
            this.mHandlerThread = null;
            this.mTimer = 0;
            this.mDnsSucceedCount = 0;
            this.mDnsFailCount = 0;
            this.mDnsLatencyMs = 0;
        }

        public void init() {
            this.mHandlerThread = new HandlerThread("DataServiceQoeDnsMonitorTh");
            this.mHandlerThread.start();
            this.mHandler = new MyHandler(this.mHandlerThread.getLooper());
        }

        /* access modifiers changed from: private */
        public class MyHandler extends Handler {
            public MyHandler(Looper looper) {
                super(looper);
            }

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    DataServiceQoeDnsMonitor.this.reportBoosterDnsPara();
                    DataServiceQoeDnsMonitor.this.resetDnsMonitorTimer();
                }
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void reportBoosterDnsPara() {
            if (HwConnectivityManagerImpl.this.mHwCommBoosterServiceManager == null) {
                Log.e(HwConnectivityManagerImpl.LOG_TAG, "reportBoosterDnsPara:mHwCommBoosterServiceManager is null");
                return;
            }
            Bundle data = new Bundle();
            data.putInt("mDnsFailCount", this.mDnsFailCount);
            data.putInt("mDnsSucceedCount", this.mDnsSucceedCount);
            data.putInt("mDnsLatencyMs", this.mDnsLatencyMs);
            int ret = HwConnectivityManagerImpl.this.mHwCommBoosterServiceManager.reportBoosterPara(HwConnectivityManagerImpl.VALID_PKGNAME_DNS, 801, data);
            this.mDnsFailCount = 0;
            this.mDnsLatencyMs = 0;
            this.mDnsSucceedCount = 0;
            if (ret == -1) {
                Log.d(HwConnectivityManagerImpl.LOG_TAG, "reportBoosterDnsPara fail");
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void startDnsMonitor() {
            if (this.mHandler.hasMessages(1)) {
                this.mHandler.removeMessages(1);
                Log.e(HwConnectivityManagerImpl.LOG_TAG, "startDnsMonitor: message exist");
            }
            this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, 1), (long) this.mTimer);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void stopDnsMonitor() {
            if (this.mHandler.hasMessages(1)) {
                this.mHandler.removeMessages(1);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void resetDnsMonitorTimer() {
            if (this.mTimer < 0) {
                Log.e(HwConnectivityManagerImpl.LOG_TAG, "resetDnsMonitorTimer error");
                return;
            }
            this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, 1), (long) this.mTimer);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initCommBoosterManager() {
        this.mHwCommBoosterServiceManager = HwFrameworkFactory.getHwCommBoosterServiceManager();
        IHwCommBoosterServiceManager iHwCommBoosterServiceManager = this.mHwCommBoosterServiceManager;
        if (iHwCommBoosterServiceManager == null) {
            Log.e(LOG_TAG, "registerBoosterCallback:getHwCommBoosterServiceManager fail");
        } else if (iHwCommBoosterServiceManager.registerCallBack(VALID_PKGNAME_DNS, this.mIHwCommBoosterCallback) != 0) {
            Log.e(LOG_TAG, "registerBoosterCallback:registerBoosterCallback fail");
            this.mHwCommBoosterServiceManager = null;
        } else {
            Log.d(LOG_TAG, "initCommBoosterManager completed");
        }
    }

    private List<String> deleteSettedIpList(List<String> ipAddressesList, List<String> settedIpList) {
        if (settedIpList.isEmpty()) {
            return ipAddressesList;
        }
        for (String settedIp : settedIpList) {
            ipAddressesList.remove(settedIp);
        }
        return ipAddressesList;
    }

    private void setOrAddNetworkAccessList(List<String> addrList, int whiteOrBlack, int setOrAdd) {
        Log.d(LOG_TAG, "setOrAddWhiteRulesToIptables ");
        IBinder binder = ServiceManager.getService("network_management");
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        if (binder != null) {
            try {
                data.writeInterfaceToken(DESCRIPTOR_NETWORKMANAGEMENT_SERVICE);
                data.writeStringList(addrList);
                data.writeInt(whiteOrBlack);
                data.writeInt(setOrAdd);
                binder.transact(1106, data, reply, 0);
                reply.readException();
            } catch (RemoteException localRemoteException) {
                Log.e(TAG, "operate NetworkAccessList error", localRemoteException);
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        }
        reply.recycle();
        data.recycle();
    }

    private void getChrUidList() {
        IBinder binder = ServiceManager.getService("network_management");
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        if (binder != null) {
            try {
                data.writeInterfaceToken(DESCRIPTOR_NETWORKMANAGEMENT_SERVICE);
                binder.transact(CODE_GET_CHR_UID_LIST, data, reply, 0);
                reply.readException();
                for (int i = 0; i < 10; i++) {
                    this.mChrAppUidArray[i] = reply.readInt();
                }
            } catch (RemoteException localRemoteException) {
                Log.e(TAG, "operate getChrUidList error", localRemoteException);
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        }
        reply.recycle();
        data.recycle();
    }

    private boolean isHostNameMatchesPolicyList(String hostName, List<String> hostNamePolicyList) {
        if (hostNamePolicyList == null || hostNamePolicyList.isEmpty()) {
            return false;
        }
        for (String hostNamePolicy : hostNamePolicyList) {
            if (isHostnameMatches(hostName, hostNamePolicy)) {
                return true;
            }
        }
        return false;
    }

    private boolean isHostnameMatches(String hostName, String hostNamePolicy) {
        int index;
        if (TextUtils.isEmpty(hostName) || TextUtils.isEmpty(hostNamePolicy) || hostNamePolicy.length() > hostName.length() || (index = hostName.indexOf(hostNamePolicy)) != hostName.length() - hostNamePolicy.length() || (index != 0 && hostName.charAt(index - 1) != '.')) {
            return false;
        }
        return true;
    }

    private void setWhiteRulesToIptables(String hostName, List<String> ipAddressesList, int networkPolicyFlag) {
        Log.d(LOG_TAG, "setWhiteRulesToIptables ");
        if (networkPolicyFlag == 1 && isHostNameMatchesPolicyList(hostName, HwDeviceManager.getList(64))) {
            deleteSettedIpList(ipAddressesList, sNetworkListCacheOfDns);
            if (!ipAddressesList.isEmpty()) {
                setOrAddNetworkAccessList(ipAddressesList, 0, 0);
                sNetworkListCacheOfDns.addAll(ipAddressesList);
            }
        }
    }

    private void setBlackRulesToIptables(String hostName, List<String> ipAddressesList, int networkPolicyFlag) {
        Log.d(LOG_TAG, "setBlackRulesToIptables ");
        if (networkPolicyFlag == 3 && isHostNameMatchesPolicyList(hostName, HwDeviceManager.getList(65))) {
            List<String> blackIpPolicyList = HwDeviceManager.getList(63);
            if ((blackIpPolicyList == null || blackIpPolicyList.isEmpty()) && sNetworkListCacheOfDns.isEmpty()) {
                setOrAddNetworkAccessList(ipAddressesList, 1, 1);
                sNetworkListCacheOfDns.addAll(ipAddressesList);
                return;
            }
            deleteSettedIpList(ipAddressesList, sNetworkListCacheOfDns);
            if (!ipAddressesList.isEmpty()) {
                setOrAddNetworkAccessList(ipAddressesList, 1, 0);
                sNetworkListCacheOfDns.addAll(ipAddressesList);
            }
        }
    }

    private int getNetworkPolicyFlag() {
        String flagStr = Settings.System.getString(this.mContex.getContentResolver(), KEY_NETWORK_POLICY_FLAG);
        if (TextUtils.isEmpty(flagStr)) {
            Log.d(LOG_TAG, "getNetworkPolicyFlag flagStr null");
            return -1;
        }
        try {
            return Integer.parseInt(flagStr, 2);
        } catch (NumberFormatException e) {
            Log.e(LOG_TAG, "getNetworkPolicyFlag parseInt error flagStr = " + flagStr);
            return -1;
        }
    }
}
