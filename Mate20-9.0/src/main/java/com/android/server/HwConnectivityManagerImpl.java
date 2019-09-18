package com.android.server;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.net.INetworkPolicyManager;
import android.net.INetworkStatsService;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.INetworkManagementService;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.provider.Telephony;
import android.telephony.HwInnerTelephonyManagerImpl;
import android.telephony.HwTelephonyManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.connectivity.NetworkAgentInfo;
import com.android.server.connectivity.NetworkMonitor;
import com.android.server.connectivity.Tethering;
import com.android.server.connectivity.tethering.TetheringConfiguration;
import com.android.server.location.HwGpsLogServices;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort;
import com.android.server.security.securitydiagnose.HwSecDiagnoseConstant;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.android.app.admin.DeviceVpnManager;
import com.huawei.deliver.info.HwDeliverInfo;
import huawei.android.net.IConnectivityExManager;
import java.util.Date;
import java.util.LinkedList;
import java.util.Random;

public class HwConnectivityManagerImpl implements HwConnectivityManager {
    private static final String CHR_BROADCAST_PERMISSION = "com.huawei.android.permission.GET_CHR_DATA";
    private static final String COUNTRY_CODE_CN = "460";
    public static final int CURRENT_CONNECT_TO_CELLULAR = 2;
    public static final int CURRENT_CONNECT_TO_WLAN = 1;
    private static final boolean DBG = true;
    private static final String DISABLE_VPN = "disable-vpn";
    private static final int DNS_BIG_LATENCY = 2000;
    private static final int DNS_ERROR_IPV6_TIMEOUT = 15;
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
    private static final int DNS_SUCCESS = 0;
    private static final int DNS_TIME_MIN = 10;
    protected static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final boolean INIT_PDN_WIFI = SystemProperties.getBoolean("ro.config.forbid_roam_dun_wifi", false);
    private static final String INTENT_DS_DNS_STATISTICS = "com.intent.action.dns_statistics";
    private static final String INTENT_DS_WEB_STAT_REPORT = "com.android.intent.action.web_stat_report";
    private static final String INTENT_WIFI_DNS_STATISTICS = "com.intent.action.wifi_dns_statistics";
    private static final boolean IS_CHINA = "CN".equalsIgnoreCase(SystemProperties.get(WifiProCommonUtils.KEY_PROP_LOCALE, ""));
    private static final boolean IS_DOCOMO;
    private static final String LOG_TAG = "HwConnectivityManagerImpl";
    static final Uri MSIM_TELEPHONY_CARRIERS_URI = Uri.parse("content://telephony/carriers/subId");
    public static final int NOT_CONNECT_TO_NETWORK = 0;
    private static final String P2P_TETHER_IFAC = "p2p-wlan0-";
    private static final String P2P_TETHER_IFAC_110x = "p2p-p2p0-";
    private static final String P2P_TETHER_IFAC_QCOM = "p2p0";
    protected static final String PROPERTY_BTHOTSPOT_ON = "sys.isbthotspoton";
    protected static final String PROPERTY_USBTETHERING_ON = "sys.isusbtetheringon";
    protected static final String PROPERTY_WIFIHOTSPOT_ON = "sys.iswifihotspoton";
    private static final int PS_AP_SLOW_DNS_BIG_LATENCY = 6;
    private static final int PS_AP_SLOW_DNS_FAIL = 5;
    private static final String SECURE_VPN = "secure-vpn";
    /* access modifiers changed from: private */
    public static final String TAG = null;
    private static final boolean VDBG = false;
    private static final String WIFI_AP_MANUAL_CONNECT = "wifi_ap_manual_connect";
    /* access modifiers changed from: private */
    public static int cellNetId = 0;
    private static HwConnectivityManager mInstance = new HwConnectivityManagerImpl();
    /* access modifiers changed from: private */
    public static int wifiNetId = 0;
    private DnsQueryStat cellDnsStat = new DnsQueryStat();
    /* access modifiers changed from: private */
    public ConnectivityManager mConnMgr = null;
    /* access modifiers changed from: private */
    public int mConnectedType = 0;
    private final BroadcastReceiver mConnectivityChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (action != null && "android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
                    ConnectivityManager unused = HwConnectivityManagerImpl.this.mConnMgr = (ConnectivityManager) context.getSystemService("connectivity");
                    if (HwConnectivityManagerImpl.this.mConnMgr != null) {
                        NetworkInfo unused2 = HwConnectivityManagerImpl.this.mNetworkInfoWlan = HwConnectivityManagerImpl.this.mConnMgr.getNetworkInfo(1);
                        NetworkInfo unused3 = HwConnectivityManagerImpl.this.mNetworkInfoMobile = HwConnectivityManagerImpl.this.mConnMgr.getNetworkInfo(0);
                        if (!(HwConnectivityManagerImpl.this.mNetworkInfoWlan == null || HwConnectivityManagerImpl.this.mNetworkInfoMobile == null)) {
                            if (HwConnectivityManagerImpl.this.mNetworkInfoWlan.isConnected()) {
                                int unused4 = HwConnectivityManagerImpl.this.mConnectedType = 1;
                            } else if (HwConnectivityManagerImpl.this.mNetworkInfoMobile.isConnected()) {
                                int unused5 = HwConnectivityManagerImpl.this.mConnectedType = 2;
                            } else {
                                int unused6 = HwConnectivityManagerImpl.this.mConnectedType = 0;
                            }
                        }
                        for (Network network : HwConnectivityManagerImpl.this.mConnMgr.getAllNetworks()) {
                            NetworkInfo networkInfo = HwConnectivityManagerImpl.this.mConnMgr.getNetworkInfo(network);
                            if (networkInfo != null) {
                                if (network != null && networkInfo.getType() == 1) {
                                    int unused7 = HwConnectivityManagerImpl.wifiNetId = network.netId;
                                }
                                if (network != null && networkInfo.getType() == 0) {
                                    int unused8 = HwConnectivityManagerImpl.cellNetId = network.netId;
                                }
                            }
                        }
                        HwConnectivityManagerImpl.this.clearInvalidPrivateDnsNetworkInfo();
                    }
                }
            }
        }
    };
    private Context mContex = null;
    private LinkedList<Date> mDnsFailQ = new LinkedList<>();
    private LinkedList<Date> mDnsOver2000Q = new LinkedList<>();
    private HwConnectivityService mHwConnectivityService = null;
    /* access modifiers changed from: private */
    public NetworkInfo mNetworkInfoMobile = null;
    /* access modifiers changed from: private */
    public NetworkInfo mNetworkInfoWlan = null;
    private boolean mSendDnsFailFlag = false;
    private boolean mSendDnsOver2000Flag = false;
    private final DeviceVpnManager mVpnManager = new DeviceVpnManager();
    private DnsQueryStat wifiDnsStat = new DnsQueryStat();

    private class DnsQueryStat {
        /* access modifiers changed from: private */
        public int mDnsCount;
        /* access modifiers changed from: private */
        public int mDnsFailCount;
        /* access modifiers changed from: private */
        public int mDnsIpv6Timeout;
        /* access modifiers changed from: private */
        public int mDnsResponse1000Count;
        /* access modifiers changed from: private */
        public int mDnsResponse150Count;
        /* access modifiers changed from: private */
        public int mDnsResponse2000Count;
        /* access modifiers changed from: private */
        public int mDnsResponse20Count;
        /* access modifiers changed from: private */
        public int mDnsResponse500Count;
        /* access modifiers changed from: private */
        public int mDnsResponseOver2000Count;
        /* access modifiers changed from: private */
        public int mDnsResponseTotalTime;

        private DnsQueryStat() {
            this.mDnsCount = 0;
            this.mDnsIpv6Timeout = 0;
            this.mDnsFailCount = 0;
            this.mDnsResponse20Count = 0;
            this.mDnsResponse150Count = 0;
            this.mDnsResponse500Count = 0;
            this.mDnsResponse1000Count = 0;
            this.mDnsResponse2000Count = 0;
            this.mDnsResponseOver2000Count = 0;
            this.mDnsResponseTotalTime = 0;
        }

        /* access modifiers changed from: private */
        public void resetAll() {
            this.mDnsCount = 0;
            this.mDnsFailCount = 0;
            this.mDnsIpv6Timeout = 0;
            this.mDnsResponse1000Count = 0;
            this.mDnsResponse150Count = 0;
            this.mDnsResponse500Count = 0;
            this.mDnsResponse1000Count = 0;
            this.mDnsResponse2000Count = 0;
            this.mDnsResponseOver2000Count = 0;
            this.mDnsResponseTotalTime = 0;
        }
    }

    static {
        boolean z = true;
        if (!SystemProperties.get("ro.config.hw_opta", "").equals("341") || !SystemProperties.get("ro.config.hw_optb", "").equals("392")) {
            z = false;
        }
        IS_DOCOMO = z;
    }

    public ConnectivityService createHwConnectivityService(Context context, INetworkManagementService netd, INetworkStatsService statsService, INetworkPolicyManager policyManager) {
        this.mContex = context;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        this.mContex.registerReceiver(this.mConnectivityChangeReceiver, intentFilter);
        this.mHwConnectivityService = new HwConnectivityService(context, netd, statsService, policyManager);
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
        String prop = tethering ? "true" : "false";
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

    /* JADX WARNING: Code restructure failed: missing block: B:47:0x016c, code lost:
        if (r8 == null) goto L_0x019e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x016e, code lost:
        r8.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x0195, code lost:
        if (r8 == null) goto L_0x019e;
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
            operator = TelephonyManager.getDefault().getSimOperator(subId);
            Log.d(TAG, " operator:" + operator);
        } else {
            operator = HwInnerTelephonyManagerImpl.getDefault().getOperatorNumeric();
        }
        if (operator != null) {
            String[] projection = {HwSecDiagnoseConstant.ANTIMAL_APK_TYPE, HwGpsLogServices.KEY_FREEZE_OR_UNFREEZE, "port"};
            String selection = "numeric = '" + operator + "' and carrier_enabled = 1";
            Cursor cursor = null;
            try {
                if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                    cursor = mContext.getContentResolver().query(Uri.withAppendedPath(MSIM_TELEPHONY_CARRIERS_URI, Long.toString((long) subId)), projection, selection, null, null);
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
                    while (!cursor.getString(cursor.getColumnIndexOrThrow(HwSecDiagnoseConstant.ANTIMAL_APK_TYPE)).contains("dun")) {
                        if (!cursor.moveToNext()) {
                        }
                    }
                    if (!INIT_PDN_WIFI || TelephonyManager.getDefault() == null || !TelephonyManager.getDefault().isNetworkRoaming()) {
                        if (cursor != null) {
                            cursor.close();
                        }
                        return true;
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                    return false;
                }
            } catch (Exception e) {
                Log.d(TAG, "Read DB '" + Telephony.Carriers.CONTENT_URI + "' failed: " + e);
            } catch (Throwable th) {
                if (cursor != null) {
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

    public void startBrowserOnClickNotification(Context context, String url) {
        Notification notification = new Notification();
        String usedUrl = Settings.Global.getString(context.getContentResolver(), "captive_portal_server");
        if (!TextUtils.isEmpty(usedUrl) && usedUrl.startsWith("http")) {
            Log.d(LOG_TAG, "startBrowserOnClickNotification: use the portal url from the settings");
            url = usedUrl;
        } else if (IS_CHINA) {
            String operator = TelephonyManager.getDefault().getNetworkOperator();
            if (!(operator == null || operator.length() == 0 || !operator.startsWith("460"))) {
                url = HwNetworkPropertyChecker.CHINA_MAINLAND_BACKUP_SERVER;
            }
        }
        Log.d(LOG_TAG, "startBrowserOnClickNotification url: " + url);
        Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(url));
        intent.setFlags(272629760);
        notification.contentIntent = PendingIntent.getActivity(context, 0, intent, 0);
        try {
            if (IS_CHINA) {
                String packageName = "com.android.browser";
                String className = "com.android.browser.BrowserActivity";
                if (Utils.isPackageInstalled("com.huawei.browser", context)) {
                    packageName = "com.huawei.browser";
                    className = "com.huawei.browser.Main";
                }
                intent.setClassName(packageName, className);
            }
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            try {
                Log.d(LOG_TAG, "default browser not exist..");
                notification.contentIntent.send();
            } catch (PendingIntent.CanceledException e2) {
                Log.e(LOG_TAG, "Sending contentIntent failed: " + e2);
            }
        }
    }

    public NetworkMonitor createHwNetworkMonitor(Context context, Handler handler, NetworkAgentInfo nai, NetworkRequest defaultRequest) {
        return new HwNetworkMonitor(context, handler, nai, defaultRequest);
    }

    public Network getNetworkForTypeWifi() {
        if (this.mHwConnectivityService != null) {
            return this.mHwConnectivityService.getNetworkForTypeWifi();
        }
        return null;
    }

    public boolean isP2pTether(String iface) {
        boolean z = false;
        if (iface == null) {
            return false;
        }
        if (iface.startsWith(P2P_TETHER_IFAC) || iface.startsWith(P2P_TETHER_IFAC_110x) || iface.startsWith(P2P_TETHER_IFAC_QCOM)) {
            z = true;
        }
        return z;
    }

    public void stopP2pTether(Context context) {
        if (context != null) {
            WifiP2pManager.Channel channel = null;
            WifiP2pManager.ActionListener mWifiP2pBridgeCreateListener = new WifiP2pManager.ActionListener() {
                public void onSuccess() {
                    Log.d(HwConnectivityManagerImpl.TAG, " Stop p2p tether success");
                }

                public void onFailure(int reason) {
                    String access$100 = HwConnectivityManagerImpl.TAG;
                    Log.e(access$100, " Stop p2p tether fail:" + reason);
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
            boolean allow = this.mVpnManager.isVpnDisabled(null);
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
            boolean allow = this.mVpnManager.isInsecureVpnDisabled(null);
            Binder.restoreCallingIdentity(ident);
            String str = TAG;
            Log.d(str, "isInsecureVpnDisabled and result is " + allow);
            return allow;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
            throw th;
        }
    }

    public void onDnsEvent(Context context, int returnCode, int latencyMs, int netId) {
        if (netId == wifiNetId || netId == cellNetId) {
            DnsQueryStat temp = netId == cellNetId ? this.cellDnsStat : this.wifiDnsStat;
            int unused = temp.mDnsCount = temp.mDnsCount + 1;
            if (15 == returnCode) {
                int unused2 = temp.mDnsIpv6Timeout = temp.mDnsIpv6Timeout + 1;
            }
            if (returnCode == 0) {
                int unused3 = temp.mDnsResponseTotalTime = temp.mDnsResponseTotalTime + latencyMs;
                if (!this.mSendDnsFailFlag && this.mDnsFailQ.size() > 0 && netId == cellNetId) {
                    this.mDnsFailQ.clear();
                }
                if (latencyMs > 2000) {
                    int unused4 = temp.mDnsResponseOver2000Count = temp.mDnsResponseOver2000Count + 1;
                    sendIntentPsSlowDnsOver2000(context, latencyMs, netId);
                } else {
                    if (!this.mSendDnsOver2000Flag && this.mDnsOver2000Q.size() > 0 && netId == cellNetId) {
                        this.mDnsOver2000Q.clear();
                    }
                    if (latencyMs <= 20) {
                        int unused5 = temp.mDnsResponse20Count = temp.mDnsResponse20Count + 1;
                    } else if (latencyMs <= 150) {
                        int unused6 = temp.mDnsResponse150Count = temp.mDnsResponse150Count + 1;
                    } else if (latencyMs <= 500) {
                        int unused7 = temp.mDnsResponse500Count = temp.mDnsResponse500Count + 1;
                    } else if (latencyMs <= 1000) {
                        int unused8 = temp.mDnsResponse1000Count = temp.mDnsResponse1000Count + 1;
                    } else {
                        int unused9 = temp.mDnsResponse2000Count = temp.mDnsResponse2000Count + 1;
                    }
                }
            } else {
                int unused10 = temp.mDnsFailCount = temp.mDnsFailCount + 1;
                sendIntentPsSlowDnsFail(context, returnCode, netId);
            }
            if (100 == temp.mDnsCount) {
                sendIntentDnsEvent(context, netId, temp);
            }
            if (this.mHwConnectivityService != null) {
                this.mHwConnectivityService.recordPrivateDnsEvent(context, returnCode, latencyMs, netId);
            }
        }
    }

    public boolean isBypassPrivateDns(int netId) {
        if (this.mHwConnectivityService != null) {
            return this.mHwConnectivityService.isBypassPrivateDns(netId);
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void clearInvalidPrivateDnsNetworkInfo() {
        if (this.mHwConnectivityService != null) {
            this.mHwConnectivityService.clearInvalidPrivateDnsNetworkInfo();
        }
    }

    private boolean randomSampling(int samplingRatio) {
        if (samplingRatio > 0 && new Random().nextInt(samplingRatio) != 0) {
            return false;
        }
        return true;
    }

    private void sendIntentPsSlowDnsFail(Context context, int returnCode, int netId) {
        if (netId == cellNetId) {
            Date now = new Date();
            Log.d(LOG_TAG, " sendIntentPsSlowDnsFail mSendDnsFailFlag = " + this.mSendDnsFailFlag + "mDnsFailQ.size() = " + this.mDnsFailQ.size());
            if (!this.mSendDnsFailFlag) {
                this.mDnsFailQ.addLast(now);
                if (this.mDnsFailQ.size() == 6) {
                    if (now.getTime() - this.mDnsFailQ.getFirst().getTime() <= 45000 && randomSampling(8)) {
                        this.mSendDnsFailFlag = true;
                        if (this.mConnectedType == 2) {
                            Intent chrIntent = new Intent(INTENT_DS_WEB_STAT_REPORT);
                            chrIntent.putExtra("ReportType", 5);
                            chrIntent.putExtra("WebFailCode", returnCode);
                            context.sendBroadcast(chrIntent, CHR_BROADCAST_PERMISSION);
                            Log.d(LOG_TAG, " sendIntentPsSlowDnsFail");
                        }
                    }
                    this.mDnsFailQ.removeFirst();
                }
            } else if (this.mDnsFailQ.isEmpty() || now.getTime() - this.mDnsFailQ.getLast().getTime() > AwareAppMngSort.PREVIOUS_APP_DIRCACTIVITY_DECAYTIME) {
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
                    if (now.getTime() - this.mDnsOver2000Q.getFirst().getTime() <= 45000 && randomSampling(2)) {
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
            } else if (this.mDnsOver2000Q.isEmpty() || now.getTime() - this.mDnsOver2000Q.getLast().getTime() > AwareAppMngSort.PREVIOUS_APP_DIRCACTIVITY_DECAYTIME) {
                Log.d(LOG_TAG, " sendIntentPsSlowDnsOver2000 reset mSendDnsOver2000Flag");
                this.mSendDnsOver2000Flag = false;
                this.mDnsOver2000Q.clear();
                this.mDnsOver2000Q.addLast(now);
            }
        }
    }

    private void sendIntentDnsEvent(Context context, int netId, DnsQueryStat result) {
        Intent intent;
        Log.d(LOG_TAG, "sendIntentDnsEvent connectType = " + this.mConnectedType);
        Log.d(LOG_TAG, "sendIntentDnsEvent mDnsCount:" + result.mDnsCount + "mDnsIpv6Timeout:" + result.mDnsIpv6Timeout + "mDnsResponseTotalTime:" + result.mDnsResponseTotalTime + "mDnsFailCount:" + result.mDnsFailCount + "mDnsResponse20Count:" + result.mDnsResponse20Count + "mDnsResponse150Count:" + result.mDnsResponse150Count + "mDnsResponse500Count:" + result.mDnsResponse500Count + "mDnsResponse1000Count:" + result.mDnsResponse1000Count + "mDnsResponse2000Count:" + result.mDnsResponse2000Count + "mDnsResponseOver2000Count:" + result.mDnsResponseOver2000Count);
        Bundle extras = new Bundle();
        extras.putInt("dnsCount", result.mDnsCount);
        extras.putInt("dnsIpv6Timeout", result.mDnsIpv6Timeout);
        extras.putInt("dnsResponseTotalTime", result.mDnsResponseTotalTime);
        extras.putInt("dnsFailCount", result.mDnsFailCount);
        extras.putInt("dnsResponse20Count", result.mDnsResponse20Count);
        extras.putInt("dnsResponse150Count", result.mDnsResponse150Count);
        extras.putInt("dnsResponse500Count", result.mDnsResponse500Count);
        extras.putInt("dnsResponse1000Count", result.mDnsResponse1000Count);
        extras.putInt("dnsResponse2000Count", result.mDnsResponse2000Count);
        extras.putInt("dnsResponseOver2000Count", result.mDnsResponseOver2000Count);
        if (netId == cellNetId) {
            intent = new Intent(INTENT_DS_DNS_STATISTICS);
        } else {
            intent = new Intent(INTENT_WIFI_DNS_STATISTICS);
        }
        intent.putExtras(extras);
        result.resetAll();
        context.sendBroadcast(intent, CHR_BROADCAST_PERMISSION);
    }

    public boolean needCaptivePortalCheck(NetworkAgentInfo nai, Context context) {
        int subId = SubscriptionManager.getDefaultDataSubscriptionId();
        if (!SubscriptionManager.isUsableSubIdValue(subId) || context == null || TelephonyManager.getDefault() == null) {
            Log.e(LOG_TAG, "needCaptivePortal: subId =" + subId + " is Invalid, or context is null,return false.");
            return false;
        }
        if (Settings.Global.getInt(context.getContentResolver(), "device_provisioned", 0) != 0) {
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

    public boolean isApIpv4AddressFixed() {
        Log.d(LOG_TAG, "Get isApIpv4AddressFixed");
        IConnectivityExManager sService = IConnectivityExManager.Stub.asInterface(ServiceManager.getService("hwConnectivityExService"));
        if (sService == null) {
            return false;
        }
        try {
            return sService.isApIpv4AddressFixed();
        } catch (RemoteException e) {
            Log.d(LOG_TAG, "RemoteException" + e.getMessage());
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
                Log.d(LOG_TAG, "RemoteException" + e.getMessage());
            }
        }
    }
}
