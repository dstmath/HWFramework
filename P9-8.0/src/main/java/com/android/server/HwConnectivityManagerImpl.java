package com.android.server;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
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
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Bundle;
import android.os.Handler;
import android.os.INetworkManagementService;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.provider.Telephony.Carriers;
import android.telephony.HwInnerTelephonyManagerImpl;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.connectivity.NetworkAgentInfo;
import com.android.server.connectivity.NetworkMonitor;
import com.android.server.connectivity.Tethering;
import com.android.server.connectivity.tethering.TetheringConfiguration;
import com.android.server.devicepolicy.StorageUtils;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.security.securitydiagnose.HwSecDiagnoseConstant;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.android.app.admin.DeviceVpnManager;
import com.huawei.deliver.info.HwDeliverInfo;
import java.util.Date;
import java.util.LinkedList;

public class HwConnectivityManagerImpl implements HwConnectivityManager {
    private static final String CHR_BROADCAST_PERMISSION = "com.huawei.android.permission.GET_CHR_DATA";
    private static final String COUNTRY_CODE_CN = "460";
    public static final int CURRENT_CONNECT_TO_CELLULAR = 2;
    public static final int CURRENT_CONNECT_TO_WLAN = 1;
    private static final boolean DBG = true;
    private static final String DISABLE_VPN = "disable-vpn";
    private static final int DNS_BIG_LATENCY = 2000;
    private static final int DNS_ERROR_IPV6_TIMEOUT = 15;
    private static final int DNS_FAIL_REPORT_COUNT = 5;
    private static final int DNS_FAIL_REPORT_TIMESPAN = 60000;
    private static final int DNS_FAIL_REPORT_TIME_INTERVAL = 3600000;
    private static final int DNS_LATENCY_1000 = 1000;
    private static final int DNS_LATENCY_150 = 150;
    private static final int DNS_LATENCY_20 = 20;
    private static final int DNS_LATENCY_500 = 500;
    private static final int DNS_OVER2000_REPORT_COUNT = 5;
    private static final int DNS_OVER2000_REPORT_TIMESPAN = 60000;
    private static final int DNS_OVER2000_REPORT_TIME_INTERVAL = 3600000;
    private static final int DNS_REPORT_COUNTING_INTERVAL = 100;
    private static final int DNS_SUCCESS = 0;
    private static final int DNS_TIME_MIN = 10;
    protected static final boolean HWFLOW;
    private static final boolean INIT_PDN_WIFI = SystemProperties.getBoolean("ro.config.forbid_roam_dun_wifi", false);
    private static final String INTENT_DS_DNS_STATISTICS = "com.intent.action.dns_statistics";
    private static final String INTENT_DS_WEB_STAT_REPORT = "com.android.intent.action.web_stat_report";
    private static final String INTENT_WIFI_DNS_STATISTICS = "com.intent.action.wifi_dns_statistics";
    private static final boolean IS_CHINA = "CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", ""));
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
    private static final String TAG = null;
    private static final boolean VDBG = false;
    private static final String WIFI_AP_MANUAL_CONNECT = "wifi_ap_manual_connect";
    private static HwConnectivityManager mInstance = new HwConnectivityManagerImpl();
    private ConnectivityManager mConnMgr = null;
    private int mConnectedType = 0;
    private final BroadcastReceiver mConnectivityChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (action != null && "android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
                    HwConnectivityManagerImpl.this.mConnMgr = (ConnectivityManager) context.getSystemService("connectivity");
                    if (HwConnectivityManagerImpl.this.mConnMgr != null) {
                        HwConnectivityManagerImpl.this.mNetworkInfoWlan = HwConnectivityManagerImpl.this.mConnMgr.getNetworkInfo(1);
                        HwConnectivityManagerImpl.this.mNetworkInfoMobile = HwConnectivityManagerImpl.this.mConnMgr.getNetworkInfo(0);
                        if (!(HwConnectivityManagerImpl.this.mNetworkInfoWlan == null || HwConnectivityManagerImpl.this.mNetworkInfoMobile == null)) {
                            if (HwConnectivityManagerImpl.this.mNetworkInfoWlan.isConnected()) {
                                HwConnectivityManagerImpl.this.mConnectedType = 1;
                                HwConnectivityManagerImpl.this.sendIntentDnsEvent(context, HwConnectivityManagerImpl.this.mLastConnectedType);
                                HwConnectivityManagerImpl.this.mLastConnectedType = HwConnectivityManagerImpl.this.mConnectedType;
                            } else if (HwConnectivityManagerImpl.this.mNetworkInfoMobile.isConnected()) {
                                HwConnectivityManagerImpl.this.mConnectedType = 2;
                                HwConnectivityManagerImpl.this.sendIntentDnsEvent(context, HwConnectivityManagerImpl.this.mLastConnectedType);
                                HwConnectivityManagerImpl.this.mLastConnectedType = HwConnectivityManagerImpl.this.mConnectedType;
                            } else {
                                HwConnectivityManagerImpl.this.mConnectedType = 0;
                                HwConnectivityManagerImpl.this.sendIntentDnsEvent(context, HwConnectivityManagerImpl.this.mLastConnectedType);
                                HwConnectivityManagerImpl.this.mLastConnectedType = HwConnectivityManagerImpl.this.mConnectedType;
                            }
                        }
                    }
                }
            }
        }
    };
    private Context mContex = null;
    private int mDnsCount = 0;
    private int mDnsFailCount = 0;
    private LinkedList<Date> mDnsFailQ = new LinkedList();
    private int mDnsIpv6Timeout = 0;
    private LinkedList<Date> mDnsOver2000Q = new LinkedList();
    private int mDnsResponse1000Count = 0;
    private int mDnsResponse150Count = 0;
    private int mDnsResponse2000Count = 0;
    private int mDnsResponse20Count = 0;
    private int mDnsResponse500Count = 0;
    private int mDnsResponseOver2000Count = 0;
    private int mDnsResponseTotalTime = 0;
    private HwConnectivityService mHwConnectivityService = null;
    private int mLastConnectedType = 0;
    private NetworkInfo mNetworkInfoMobile = null;
    private NetworkInfo mNetworkInfoWlan = null;
    private boolean mSendDnsFailFlag = false;
    private boolean mSendDnsOver2000Flag = false;
    private final DeviceVpnManager mVpnManager = new DeviceVpnManager();

    static {
        boolean z = false;
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        HWFLOW = isLoggable;
        if (SystemProperties.get("ro.config.hw_opta", "").equals("341")) {
            z = SystemProperties.get("ro.config.hw_optb", "").equals("392");
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
        String prop = tethering ? StorageUtils.SDCARD_ROMOUNTED_STATE : StorageUtils.SDCARD_RWMOUNTED_STATE;
        try {
            TetheringConfiguration cfg = tetheringService.getTetheringConfiguration();
            if (usb) {
                SystemProperties.set(PROPERTY_USBTETHERING_ON, prop);
                Log.d(TAG, "set PROPERTY_USBTETHERING_ON: " + prop);
                return;
            }
            if (cfg != null) {
                if (cfg.isWifi(ifaceName)) {
                    SystemProperties.set(PROPERTY_WIFIHOTSPOT_ON, prop);
                    Log.d(TAG, "set iswifihotspoton = " + prop);
                    return;
                }
            }
            if (cfg == null) {
                return;
            }
            if (cfg.isBluetooth(ifaceName)) {
                SystemProperties.set(PROPERTY_BTHOTSPOT_ON, prop);
                Log.d(TAG, "set isbthotspoton = " + prop);
            }
        } catch (RuntimeException e) {
            Log.e(TAG, "when setTetheringProp ,error =" + e + "  ifaceNmae =" + ifaceName);
        }
    }

    private boolean isFromDocomo(Context context) {
        if (IS_DOCOMO && !TextUtils.isEmpty(Global.getString(context.getContentResolver(), "tether_dun_apn"))) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:58:0x01c3  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean checkDunExisted(Context mContext) {
        boolean z = SystemProperties.getBoolean("ro.config.enable.gdun", false);
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
        String operator;
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
            String[] projection = new String[]{HwSecDiagnoseConstant.ANTIMAL_APK_TYPE, "proxy", "port"};
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
                    cursor = mContext.getContentResolver().query(Carriers.CONTENT_URI, projection, selection, null, null);
                    if (HWFLOW) {
                        Log.d(TAG, "Read DB '" + Carriers.CONTENT_URI);
                    }
                }
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        if (cursor.getString(cursor.getColumnIndexOrThrow(HwSecDiagnoseConstant.ANTIMAL_APK_TYPE)).contains("dun")) {
                            if (INIT_PDN_WIFI && TelephonyManager.getDefault() != null && TelephonyManager.getDefault().isNetworkRoaming()) {
                                if (cursor != null) {
                                    cursor.close();
                                }
                                return false;
                            }
                            if (cursor != null) {
                                cursor.close();
                            }
                            return true;
                        }
                    } while (cursor.moveToNext());
                    if (cursor != null) {
                    }
                } else if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception e) {
                Log.d(TAG, "Read DB '" + Carriers.CONTENT_URI + "' failed: " + e);
                if (cursor != null) {
                    cursor.close();
                }
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
        Log.d(TAG, "tethering setCurrentFunction rndis,serial " + enable);
        if (enable) {
            if (usbManager != null) {
                usbManager.setCurrentFunction("rndis,serial", false);
            }
            Secure.putInt(context.getContentResolver(), "usb_tethering_on", 1);
        } else {
            Secure.putInt(context.getContentResolver(), "usb_tethering_on", 0);
        }
        return true;
    }

    public void captivePortalCheckCompleted(Context context, boolean isCaptivePortal) {
        if (!isCaptivePortal && 1 == System.getInt(context.getContentResolver(), WIFI_AP_MANUAL_CONNECT, 0)) {
            System.putInt(context.getContentResolver(), WIFI_AP_MANUAL_CONNECT, 0);
            Log.d(LOG_TAG, "not portal ap manual connect");
        }
    }

    public void startBrowserOnClickNotification(Context context, String url) {
        Notification notification = new Notification();
        if (IS_CHINA) {
            String operator = TelephonyManager.getDefault().getNetworkOperator();
            if (!(operator == null || operator.length() == 0 || !operator.startsWith("460"))) {
                url = HwNetworkPropertyChecker.CHINA_MAINLAND_MAIN_SERVER;
            }
        }
        Log.d(LOG_TAG, "startBrowserOnClickNotification url: " + url);
        Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(url));
        intent.setFlags(272629760);
        notification.contentIntent = PendingIntent.getActivity(context, 0, intent, 0);
        try {
            intent.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            try {
                Log.d(LOG_TAG, "default browser not exist..");
                notification.contentIntent.send();
            } catch (CanceledException e2) {
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
        if (iface == null) {
            return false;
        }
        boolean startsWith = (iface.startsWith(P2P_TETHER_IFAC) || iface.startsWith(P2P_TETHER_IFAC_110x)) ? true : iface.startsWith(P2P_TETHER_IFAC_QCOM);
        return startsWith;
    }

    public void stopP2pTether(Context context) {
        if (context != null) {
            Channel channel = null;
            ActionListener mWifiP2pBridgeCreateListener = new ActionListener() {
                public void onSuccess() {
                    Log.d(HwConnectivityManagerImpl.TAG, " Stop p2p tether success");
                }

                public void onFailure(int reason) {
                    Log.e(HwConnectivityManagerImpl.TAG, " Stop p2p tether fail:" + reason);
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

    public boolean isVpnDisabled() {
        boolean allow = this.mVpnManager.isVpnDisabled(null);
        Log.d(TAG, "isVpnDisabled and result is " + allow);
        return allow;
    }

    public boolean isInsecureVpnDisabled() {
        boolean allow = this.mVpnManager.isInsecureVpnDisabled(null);
        Log.d(TAG, "isInsecureVpnDisabled and result is " + allow);
        return allow;
    }

    public void onDnsEvent(Context context, int returnCode, int latencyMs) {
        this.mDnsCount++;
        if (15 == returnCode) {
            this.mDnsIpv6Timeout++;
        }
        if (returnCode == 0) {
            this.mDnsResponseTotalTime += latencyMs;
            if (!this.mSendDnsFailFlag && this.mDnsFailQ.size() > 0) {
                this.mDnsFailQ.clear();
            }
            if (latencyMs > 2000) {
                this.mDnsResponseOver2000Count++;
                sendIntentPsSlowDnsOver2000(context);
            } else if (!this.mSendDnsOver2000Flag && this.mDnsOver2000Q.size() > 0) {
                this.mDnsOver2000Q.clear();
            }
        } else {
            this.mDnsFailCount++;
            sendIntentPsSlowDnsFail(context);
        }
        if (latencyMs <= 20) {
            this.mDnsResponse20Count++;
        } else if (latencyMs <= 150) {
            this.mDnsResponse150Count++;
        } else if (latencyMs <= 500) {
            this.mDnsResponse500Count++;
        } else if (latencyMs <= 1000) {
            this.mDnsResponse1000Count++;
        } else if (latencyMs <= 2000) {
            this.mDnsResponse2000Count++;
        }
        if (100 == this.mDnsCount) {
            sendIntentDnsEvent(context, this.mLastConnectedType);
        }
    }

    private void sendIntentPsSlowDnsFail(Context context) {
        Date now = new Date();
        Log.d(LOG_TAG, " sendIntentPsSlowDnsFail mSendDnsFailFlag = " + this.mSendDnsFailFlag + "mDnsFailQ.size() = " + this.mDnsFailQ.size());
        if (!this.mSendDnsFailFlag) {
            this.mDnsFailQ.addLast(now);
            if (this.mDnsFailQ.size() == 5) {
                if (now.getTime() - ((Date) this.mDnsFailQ.getFirst()).getTime() <= AppHibernateCst.DELAY_ONE_MINS) {
                    this.mSendDnsFailFlag = true;
                    Intent chrIntent = new Intent(INTENT_DS_WEB_STAT_REPORT);
                    chrIntent.putExtra("ReportType", 5);
                    context.sendBroadcast(chrIntent, CHR_BROADCAST_PERMISSION);
                    Log.d(LOG_TAG, " sendIntentPsSlowDnsFail");
                }
                this.mDnsFailQ.removeFirst();
            }
        } else if (this.mDnsFailQ.isEmpty() || now.getTime() - ((Date) this.mDnsFailQ.getLast()).getTime() > WifiProCommonUtils.RECHECK_DELAYED_MS) {
            Log.d(LOG_TAG, " sendIntentPsSlowDnsFail reset mSendDnsFailFlag");
            this.mSendDnsFailFlag = false;
            this.mDnsFailQ.clear();
            this.mDnsFailQ.addLast(now);
        }
    }

    private void sendIntentPsSlowDnsOver2000(Context context) {
        Date now = new Date();
        Log.d(LOG_TAG, " sendIntentPsSlowDnsOver2000 mSendDnsOver2000Flag = " + this.mSendDnsOver2000Flag + "mDnsOver2000Q.size() = " + this.mDnsOver2000Q.size());
        if (!this.mSendDnsOver2000Flag) {
            this.mDnsOver2000Q.addLast(now);
            if (this.mDnsOver2000Q.size() == 5) {
                if (now.getTime() - ((Date) this.mDnsOver2000Q.getFirst()).getTime() <= AppHibernateCst.DELAY_ONE_MINS) {
                    this.mSendDnsOver2000Flag = true;
                    Intent chrIntent = new Intent(INTENT_DS_WEB_STAT_REPORT);
                    chrIntent.putExtra("ReportType", 6);
                    context.sendBroadcast(chrIntent, CHR_BROADCAST_PERMISSION);
                    Log.d(LOG_TAG, " sendIntentPsSlowDnsOver2000");
                }
                this.mDnsOver2000Q.removeFirst();
            }
        } else if (this.mDnsOver2000Q.isEmpty() || now.getTime() - ((Date) this.mDnsOver2000Q.getLast()).getTime() > WifiProCommonUtils.RECHECK_DELAYED_MS) {
            Log.d(LOG_TAG, " sendIntentPsSlowDnsOver2000 reset mSendDnsOver2000Flag");
            this.mSendDnsOver2000Flag = false;
            this.mDnsOver2000Q.clear();
            this.mDnsOver2000Q.addLast(now);
        }
    }

    private void sendIntentDnsEvent(Context context, int connectType) {
        if (connectType == 0 || this.mDnsCount == 0) {
            Log.d(LOG_TAG, " not connect to network or DNS count is 0, return");
            return;
        }
        Intent intent;
        Log.d(LOG_TAG, "sendIntentDnsEvent connectType " + connectType);
        Log.d(LOG_TAG, "sendIntentDnsEvent mDnsCount:" + this.mDnsCount + " mDnsIpv6Timeout:" + this.mDnsIpv6Timeout + " mDnsResponseTotalTime:" + this.mDnsResponseTotalTime + " mDnsFailCount:" + this.mDnsFailCount + " mDnsResponse20Count:" + this.mDnsResponse20Count + " mDnsResponse150Count:" + this.mDnsResponse150Count + " mDnsResponse500Count:" + this.mDnsResponse500Count + " mDnsResponse1000Count:" + this.mDnsResponse1000Count + " mDnsResponse2000Count:" + this.mDnsResponse2000Count + " mDnsResponseOver2000Count:" + this.mDnsResponseOver2000Count);
        if (connectType == 2) {
            intent = new Intent(INTENT_DS_DNS_STATISTICS);
        } else {
            intent = new Intent(INTENT_WIFI_DNS_STATISTICS);
        }
        Bundle extras = new Bundle();
        extras.putInt("dnsCount", this.mDnsCount);
        extras.putInt("dnsIpv6Timeout", this.mDnsIpv6Timeout);
        extras.putInt("dnsResponseTotalTime", this.mDnsResponseTotalTime);
        extras.putInt("dnsFailCount", this.mDnsFailCount);
        extras.putInt("dnsResponse20Count", this.mDnsResponse20Count);
        extras.putInt("dnsResponse150Count", this.mDnsResponse150Count);
        extras.putInt("dnsResponse500Count", this.mDnsResponse500Count);
        extras.putInt("dnsResponse1000Count", this.mDnsResponse1000Count);
        extras.putInt("dnsResponse2000Count", this.mDnsResponse2000Count);
        extras.putInt("dnsResponseOver2000Count", this.mDnsResponseOver2000Count);
        intent.putExtras(extras);
        this.mDnsCount = 0;
        this.mDnsIpv6Timeout = 0;
        this.mDnsResponseTotalTime = 0;
        this.mDnsFailCount = 0;
        this.mDnsResponse20Count = 0;
        this.mDnsResponse150Count = 0;
        this.mDnsResponse500Count = 0;
        this.mDnsResponse1000Count = 0;
        this.mDnsResponse2000Count = 0;
        this.mDnsResponseOver2000Count = 0;
        context.sendBroadcast(intent, CHR_BROADCAST_PERMISSION);
    }

    public boolean needCaptivePortalCheck(NetworkAgentInfo nai, Context context) {
        int subId = SubscriptionManager.getDefaultDataSubscriptionId();
        if (!SubscriptionManager.isUsableSubIdValue(subId) || context == null || TelephonyManager.getDefault() == null) {
            Log.e(LOG_TAG, "needCaptivePortal: subId =" + subId + " is Invalid, or context is null,return false.");
            return false;
        }
        int deviceProvisioned = Global.getInt(context.getContentResolver(), "device_provisioned", 0);
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
            String plmnConfig = System.getString(context.getContentResolver(), "need_captive_portal_by_hplmn");
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
}
