package com.android.server;

import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.emcom.EmcomManager;
import android.hdm.HwDeviceManager;
import android.net.ConnectivityManager;
import android.net.IDnsResolver;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.ResolverParamsParcel;
import android.net.UidRangeParcel;
import android.net.booster.IHwCommBoosterCallback;
import android.net.booster.IHwCommBoosterServiceManager;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.ServiceSpecificException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserManager;
import android.provider.Settings;
import android.provider.SettingsEx;
import android.text.TextUtils;
import android.util.Slog;
import com.android.internal.net.IOemNetd;
import com.android.internal.net.IOemNetdUnsolicitedEventListener;
import com.android.server.NativeDaemonConnector;
import com.android.server.NetworkManagementService;
import com.android.server.appactcontrol.AppActConstant;
import com.android.server.display.HwUibcReceiver;
import com.android.server.wm.HwWmConstants;
import com.google.android.collect.Lists;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import libcore.util.EmptyArray;

public class HwNetworkManagementService extends NetworkManagementService {
    private static final String ACTION_WIFI_AP_STA_JOIN = "com.huawei.net.wifi.WIFI_AP_STA_JOIN";
    private static final String ACTION_WIFI_AP_STA_LEAVE = "com.huawei.net.wifi.WIFI_AP_STA_LEAVE";
    private static final int ADD_NETWORK_ACCESS_LIST = 0;
    private static final String AD_APKDL_STRATEGY_PERMISSION = "com.huawei.permission.AD_APKDL_STRATEGY";
    private static final int AD_STRATEGY = 0;
    private static final int APK_CONTROL_STRATEGY = 2;
    private static final int APK_DL_STRATEGY = 1;
    private static final String ARG_ADD = "add";
    private static final String ARG_CLEAR = "clear";
    private static final String ARG_IP_WHITELIST = "ipwhitelist";
    private static final String ARG_SET = "set";
    private static final int BINARY = 2;
    private static final int BLACK_LIST = 1;
    private static final String BROWSER_UID_INFO = "BrowserUidInfo";
    private static final String CHR_BROADCAST_PERMISSION = "com.huawei.android.permission.GET_CHR_DATA";
    private static final int CHR_MAX_REPORT_APP_COUNT = 10;
    private static final String CMD_NET_FILTER = "net_filter";
    private static final int CODE_CLEAR_AD_APKDL_STRATEGY = 1103;
    private static final int CODE_CLOSE_SOCKETS_FOR_UID = 1107;
    private static final int CODE_GET_APLINKED_STA_LIST = 1005;
    private static final int CODE_GET_CHR_UID_LIST = 1123;
    private static final int CODE_GET_NETD_PID_CMD = 1112;
    private static final int CODE_GET_WIFI_DNS_STAT = 1011;
    private static final int CODE_IP_TABLE_CONFIG = 1113;
    private static final int CODE_PRINT_AD_APKDL_STRATEGY = 1104;
    private static final int CODE_SET_AD_STRATEGY_RULE = 1101;
    private static final int CODE_SET_APK_CONTROL_STRATEGY = 1109;
    private static final int CODE_SET_APK_DL_STRATEGY = 1102;
    private static final int CODE_SET_APK_DL_URL_USER_RESULT = 1105;
    private static final int CODE_SET_AP_CONFIGRATION_HW = 1008;
    private static final int CODE_SET_ARP_IGNORE_CMD = 1124;
    private static final int CODE_SET_CHR_REPORT_APP_LIST = 1108;
    private static final int CODE_SET_DNS_FORWARDING_CMD = 1121;
    private static final int CODE_SET_INTERFACE_PROXY_ARP_CMD = 1122;
    private static final int CODE_SET_MPDNS_APP = 1110;
    private static final int CODE_SET_NETWORK_ACCESS_LIST = 1106;
    private static final int CODE_SET_PG_FILTER_CMD = 1111;
    private static final int CODE_SET_SOFTAP_DISASSOCIATESTA = 1007;
    private static final int CODE_SET_SOFTAP_MACFILTER = 1006;
    private static final int CODE_SET_SOFTAP_TX_POWER = 1009;
    private static final int DATA_SEND_TO_KERNEL_APP_QOE_RSRP = 4;
    private static final int DATA_SEND_TO_KERNEL_APP_QOE_UID = 3;
    private static final int DATA_SEND_TO_KERNEL_BS_SUPPORT_VIDEO_ACC = 1;
    public static final int DATA_SEND_TO_KERNEL_SETTING_PARAMS = 9;
    private static final int DATA_SEND_TO_KERNEL_SUPPORT_AI_CHANGE = 2;
    private static final int DATA_SEND_TO_NETD_DNS_CURE_CONFIG = 11;
    private static final int DATA_SEND_TO_NETD_PRE_DNS_APP_UID = 5;
    private static final int DATA_SEND_TO_NETD_PRE_DNS_BROWSER_UID = 6;
    private static final int DATA_SEND_TO_NETD_PRE_DNS_TOP_DOMAIN = 7;
    private static final int DATA_SEND_TO_NETD_PRE_DNS_UID_FOREGROUND = 8;
    private static final int DEFAULT_ISMCOEX_WIFI_AP_CHANNEL = 11;
    private static final int DEFAULT_WIFI_AP_CHANNEL = 0;
    private static final int DEFAULT_WIFI_AP_MAXSCB = 8;
    private static final int DEFAULT_WIFI_AP_MAX_CONNECTIONS = 8;
    private static final String DESCRIPTOR = "android.net.wifi.INetworkManager";
    private static final String DESCRIPTOR_NETWORKMANAGEMENT_SERVICE = "android.os.INetworkManagementService";
    private static final String DNS_CONFIG_ASSIGNED_SERVERS = "assignedServers";
    private static final String DNS_CONFIG_DOMAIN_STRINGS = "domainStrs";
    private static final String DNS_CONFIG_NETID = "netId";
    private static final String DNS_DOMAIN_NAME = "DnsDomainName";
    private static final int DNS_RESOLVER_DEFAULT_MAX_SAMPLES = 64;
    private static final int DNS_RESOLVER_DEFAULT_MIN_SAMPLES = 8;
    private static final int DNS_RESOLVER_DEFAULT_SAMPLE_VALIDITY_SECONDS = 1800;
    private static final int DNS_RESOLVER_DEFAULT_SUCCESS_THRESHOLD_PERCENT = 25;
    private static final String EVENT_KEY = "event_key";
    private static final int EVENT_REGISTER_BOOSTER_CALLBACK = 0;
    private static final int EXIST_BLACK_AND_DOMAIN_NETWORK_POLICY_FLAG = 3;
    private static final int EXIST_BLACK_BUT_NOT_DOMAIN_NETWORK_POLICY_FLAG = 2;
    private static final int EXIST_WHITE_AND_DOMAIN_NETWORK_POLICY_FLAG = 1;
    private static final int EXIST_WHITE_BUT_NOT_DOMAIN_NETWORK_POLICY_FLAG = 0;
    private static final String EXP_INFO_REPORT_ENABLE = "ExpInfoReportState";
    private static final String EXTRA_CURRENT_TIME = "currentTime";
    private static final String EXTRA_STA_COUNT = "staCount";
    private static final String EXTRA_STA_INFO = "macInfo";
    private static final String FOREGROUND_STATE = "ForegroundState";
    private static final String FOREGROUND_UID = "ForegroundUid";
    private static final String HEX_STR = "0123456789ABCDEF";
    private static final int HIDATA_APP_QOE_TO_KERNEL_MSG_TYPE_RSRP = 1;
    private static final int HIDATA_APP_QOE_TO_KERNEL_MSG_TYPE_UID_PERIOD = 2;
    private static final int HSM_TRANSACT_CODE = 201;
    private static final String HW_SYSTEM_SERVER_START = "com.huawei.systemserver.START";
    private static final String INTENT_APKDL_URL_DETECTED = "com.huawei.intent.action.apkdl_url_detected";
    private static final String INTENT_DS_DNS_STAT = "com.huawei.intent.action.dns_stat_report";
    private static final String INTENT_DS_WIFI_WEB_STAT_REPORT = "com.huawei.chr.wifi.action.web_stat_report";
    private static final int INVALID_PID = -1;
    private static final int INVALID_UID = -1;
    private static final int IP_TABLE_CONFIG_DISABLE = 0;
    private static final int IP_TABLE_CONFIG_ENABLE = 1;
    private static final String ISM_COEX_ON = "ro.config.hw_ismcoex";
    private static final int KERNEL_DATA_MEDIA_INFO = 1;
    private static final int KERNEL_DATA_NET_SPEED_EXP_INFO = 2;
    private static final String KEY_NETWORK_POLICY_FLAG = "network_policy";
    private static final String KEY_NETWORK_POLICY_PROPERTIES = "sys.mdm.domain_network_policy";
    private static final int LENGTH_OF_4A_QUERY = 7;
    private static final int LENGTH_OF_A_QUERY = 7;
    private static final int LENGTH_OF_FAIL = 6;
    private static final String LOCAL_HOST = "127.0.0.1";
    private static final String MAC_KEY = "mac_key";
    private static final int MAX_ARGC_PER_COMMAND = 12;
    private static final int MOBILE_STAT_MAX_NUM = 254;
    private static final int NETWORK_POLICY_NOT_SET = -1;
    private static final int PER_STRATEGY_SIZE = 470;
    private static final int PER_UID_LIST_SIZE = 50;
    private static final int SET_NETWORK_ACCESS_LIST = 1;
    private static final int STATE_ON = 1;
    private static final String STA_JOIN_EVENT = "STA_JOIN";
    private static final int STA_JOIN_HANDLE_DELAY = 5000;
    private static final String STA_LEAVE_EVENT = "STA_LEAVE";
    private static final String TAG = HwNetworkManagementService.class.getSimpleName();
    private static final String TOP_APP_UID_INFO = "TogAppUidInfo";
    private static final String VIDEO_INFO_REPORT_ENABLE = "VideoInfoReportState";
    private static final int WEB_STAT = 0;
    private static final int WHITE_LIST = 0;
    private static final int WIFI_STAT_DELTA = 255;
    private Handler mApLinkedStaHandler = new Handler() {
        /* class com.android.server.HwNetworkManagementService.AnonymousClass1 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            String action = null;
            long mCurrentTime = System.currentTimeMillis();
            Bundle bundle = msg.getData();
            String event = bundle.getString(HwNetworkManagementService.EVENT_KEY);
            String macStr = bundle.getString(HwNetworkManagementService.MAC_KEY).toLowerCase();
            if (event != null && macStr != null) {
                if (HwNetworkManagementService.STA_JOIN_EVENT.equals(event)) {
                    action = HwNetworkManagementService.ACTION_WIFI_AP_STA_JOIN;
                    if (!HwNetworkManagementService.this.mMacList.contains(macStr)) {
                        HwNetworkManagementService.this.mMacList.add(macStr);
                        HwNetworkManagementService.access$108(HwNetworkManagementService.this);
                    } else {
                        String str = HwNetworkManagementService.TAG;
                        Slog.e(str, macStr + " had been added, but still get event " + event);
                    }
                } else if (HwNetworkManagementService.STA_LEAVE_EVENT.equals(event)) {
                    action = HwNetworkManagementService.ACTION_WIFI_AP_STA_LEAVE;
                    if (HwNetworkManagementService.this.mApLinkedStaHandler.hasMessages(msg.what)) {
                        HwNetworkManagementService.this.mApLinkedStaHandler.removeMessages(msg.what);
                        String str2 = HwNetworkManagementService.TAG;
                        Slog.d(str2, "event=" + event + ", remove STA_JOIN message");
                    } else if (HwNetworkManagementService.this.mMacList.contains(macStr)) {
                        HwNetworkManagementService.this.mMacList.remove(macStr);
                        HwNetworkManagementService.access$110(HwNetworkManagementService.this);
                    } else {
                        String str3 = HwNetworkManagementService.TAG;
                        Slog.e(str3, macStr + " had been removed, but still get event " + event);
                    }
                }
                String str4 = HwNetworkManagementService.TAG;
                Slog.d(str4, "handle " + event + " event, mLinkedStaCount=" + HwNetworkManagementService.this.mLinkedStaCount);
                if (HwNetworkManagementService.this.mLinkedStaCount < 0 || HwNetworkManagementService.this.mLinkedStaCount > 8 || HwNetworkManagementService.this.mLinkedStaCount != HwNetworkManagementService.this.mMacList.size()) {
                    String str5 = HwNetworkManagementService.TAG;
                    Slog.e(str5, "mLinkedStaCount over flow, need synchronize. value = " + HwNetworkManagementService.this.mLinkedStaCount);
                    HwNetworkManagementService.this.mMacList = new ArrayList();
                    HwNetworkManagementService.this.mLinkedStaCount = 0;
                }
                String staInfo = String.format("MAC=%s TIME=%d STACNT=%d", macStr, Long.valueOf(mCurrentTime), Integer.valueOf(HwNetworkManagementService.this.mLinkedStaCount));
                String str6 = HwNetworkManagementService.TAG;
                Slog.e(str6, "send broadcast, event=" + event + ", extraInfo: " + staInfo);
                Intent broadcast = new Intent(action);
                broadcast.putExtra(HwNetworkManagementService.EXTRA_STA_INFO, macStr);
                broadcast.putExtra(HwNetworkManagementService.EXTRA_CURRENT_TIME, mCurrentTime);
                broadcast.putExtra(HwNetworkManagementService.EXTRA_STA_COUNT, HwNetworkManagementService.this.mLinkedStaCount);
                HwNetworkManagementService.this.mContext.sendBroadcast(broadcast, "android.permission.ACCESS_WIFI_STATE");
            }
        }
    };
    private boolean mBoosterEnabled = SystemProperties.getBoolean("ro.config.hw_booster", true);
    private boolean mBoosterNetAiChangeEnabled = SystemProperties.getBoolean("ro.config.hisi_net_ai_change", true);
    private boolean mBoosterPreDnsEnabled = SystemProperties.getBoolean("ro.config.pre_dns_query", true);
    private boolean mBoosterVideoAccEnabled = SystemProperties.getBoolean("ro.config.hisi_video_acc", true);
    private int mChannel;
    private int[] mChrAppUidArray = new int[10];
    private AtomicInteger mCmdId;
    private NativeDaemonConnector mConnector;
    private Context mContext;
    private IDnsResolver mDnsResolver = null;
    private final BroadcastReceiver mHwSystemServerStartReceiver = new BroadcastReceiver() {
        /* class com.android.server.HwNetworkManagementService.AnonymousClass3 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                Slog.d(HwNetworkManagementService.TAG, "HwSystemServerStartReceiver intent=null");
                return;
            }
            String action = intent.getAction();
            String str = HwNetworkManagementService.TAG;
            Slog.d(str, "HwSystemServerStartReceiver action=" + action);
            if ("com.huawei.systemserver.START".equals(action) && HwNetworkManagementService.this.mBoosterEnabled) {
                HwNetworkManagementService.this.handleRegisterBoosterCallback();
            }
        }
    };
    private IHwCommBoosterCallback mIHwCommBoosterCallback = new IHwCommBoosterCallback.Stub() {
        /* class com.android.server.HwNetworkManagementService.AnonymousClass2 */

        public void callBack(int type, Bundle b) throws RemoteException {
            if (b != null) {
                switch (type) {
                    case 1:
                        if (1 == b.getInt(HwNetworkManagementService.VIDEO_INFO_REPORT_ENABLE)) {
                            HwNetworkManagementService.this.setNetBoosterVodEnabled(true);
                            return;
                        } else {
                            HwNetworkManagementService.this.setNetBoosterVodEnabled(false);
                            return;
                        }
                    case 2:
                        if (1 == b.getInt(HwNetworkManagementService.EXP_INFO_REPORT_ENABLE)) {
                            HwNetworkManagementService.this.setNetBoosterKsiEnabled(true);
                            return;
                        } else {
                            HwNetworkManagementService.this.setNetBoosterKsiEnabled(false);
                            return;
                        }
                    case 3:
                        HwNetworkManagementService.this.setNetBoosterAppUid(b);
                        return;
                    case 4:
                        HwNetworkManagementService.this.setNetBoosterRsrpRsrq(b);
                        return;
                    case 5:
                        HwNetworkManagementService.this.setNetBoosterPreDnsAppUid(b);
                        return;
                    case 6:
                        HwNetworkManagementService.this.setNetBoosterPreDnsBrowerUid(b);
                        return;
                    case 7:
                        HwNetworkManagementService.this.setNetBoosterPreDnsDomainName(b);
                        return;
                    case 8:
                        HwNetworkManagementService.this.setNetBoosterUidForeground(b);
                        return;
                    case 9:
                        HwNetworkManagementService.this.sendSettingParamsToKernel(b);
                        return;
                    case 10:
                    default:
                        return;
                    case 11:
                        HwNetworkManagementService.this.setNetBoosterDnsConfig(b);
                        return;
                }
            }
        }
    };
    private int mLinkedStaCount = 0;
    private List<String> mMacList = new ArrayList();
    private IBinder mOemNetdBinder = null;
    private IOemNetd mOemNetdService = null;
    private OemNetdUnsolicitedEventListener mOemNetdUnsolicitedEventListener = null;
    private String mSoftapIface;
    private String mWlanIface;
    private Pattern p = Pattern.compile("^.*max=([0-9]+);idx=([0-9]+);(.*)$");
    private HashMap<String, Long> startTimeMap = new HashMap<>();
    private StringBuffer urlBuffer = new StringBuffer();

    public static class BoosterConstants {
        public static final int ERROR_INVALID_PARAM = -3;
        public static final int ERROR_NO_SERVICE = -1;
        public static final int ERROR_REMOTE_EXCEPTION = -2;
        public static final int SUCCESS = 0;
    }

    static /* synthetic */ int access$108(HwNetworkManagementService x0) {
        int i = x0.mLinkedStaCount;
        x0.mLinkedStaCount = i + 1;
        return i;
    }

    static /* synthetic */ int access$110(HwNetworkManagementService x0) {
        int i = x0.mLinkedStaCount;
        x0.mLinkedStaCount = i - 1;
        return i;
    }

    static class NetdResponseCode {
        public static final int ApLinkedStaListChangeHISI = 651;
        public static final int ApLinkedStaListChangeQCOM = 901;
        public static final int HwDnsStat = 130;
        public static final int SoftapDhcpListResult = 122;
        public static final int SoftapListResult = 121;

        NetdResponseCode() {
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleRegisterBoosterCallback() {
        Slog.d(TAG, "handleRegisterBoosterCallback");
        IHwCommBoosterServiceManager bm = HwFrameworkFactory.getHwCommBoosterServiceManager();
        if (bm != null) {
            int ret = bm.registerCallBack("com.android.server", this.mIHwCommBoosterCallback);
            if (ret != 0) {
                String str = TAG;
                Slog.e(str, "handleRegisterBoosterCallback:registerCallBack failed, ret=" + ret);
                return;
            }
            return;
        }
        Slog.e(TAG, "handleRegisterBoosterCallback:null HwCommBoosterServiceManager");
    }

    /* access modifiers changed from: private */
    public class OemNetdUnsolicitedEventListener extends IOemNetdUnsolicitedEventListener.Stub {
        private OemNetdUnsolicitedEventListener() {
        }

        @Override // com.android.internal.net.IOemNetdUnsolicitedEventListener
        public void onRegistered() {
        }

        @Override // com.android.internal.net.IOemNetdUnsolicitedEventListener
        public void onNetBoosterVodReport(int[] vodParams) throws RemoteException {
            if (vodParams == null || vodParams.length != 10) {
                Slog.e(HwNetworkManagementService.TAG, "onNetBoosterVodReport params error");
                return;
            }
            Slog.i(HwNetworkManagementService.TAG, "onNetBoosterVodReport");
            HwNetworkManagementService.this.reportVodParams(vodParams[0], vodParams[1], vodParams[2], vodParams[3], vodParams[4], vodParams[5], vodParams[6], vodParams[7], vodParams[8], vodParams[9]);
        }

        @Override // com.android.internal.net.IOemNetdUnsolicitedEventListener
        public void onNetBoosterKsiReport(int slowType, int avgAmp, int duration, int timeStart) throws RemoteException {
            Slog.i(HwNetworkManagementService.TAG, "onNetBoosterKsiReport");
            HwNetworkManagementService.this.reportKsiParams(slowType, avgAmp, duration, timeStart);
        }

        @Override // com.android.internal.net.IOemNetdUnsolicitedEventListener
        public void onDataSpeedSlowDetected(String[] cooked, String raw) throws RemoteException {
            Slog.i(HwNetworkManagementService.TAG, "onDataSpeedSlowDetected");
            HwNetworkManagementService.this.sendDataSpeedSlowMessage(cooked, raw);
        }

        @Override // com.android.internal.net.IOemNetdUnsolicitedEventListener
        public void onDnsStatReportResult(String serverName, int serverNo, int[] failcount, int[] typeQueryA, int[] typeQuery4A) {
            if (failcount.length >= 6 && typeQueryA.length >= 7 && typeQuery4A.length >= 7) {
                Slog.i(HwNetworkManagementService.TAG, "onDnsStatReportResult");
                Bundle bundle = new Bundle();
                bundle.putInt("dnsServerNo", serverNo);
                bundle.putString("dnsServerName", serverName);
                for (int i = 0; i < failcount.length; i++) {
                    bundle.putInt("dnsServerFailReason" + (i + 1), failcount[i]);
                }
                bundle.putInt("dnsServerTASucc", typeQueryA[0]);
                bundle.putInt("dnsServerTAFail", typeQueryA[1]);
                for (int i2 = 2; i2 < typeQueryA.length; i2++) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("dnsServerTADelayL");
                    sb.append(i2 - 1);
                    bundle.putInt(sb.toString(), typeQueryA[i2]);
                }
                bundle.putInt("dnsServerT4ASucc", typeQuery4A[0]);
                bundle.putInt("dnsServerT4AFail", typeQuery4A[1]);
                for (int i3 = 2; i3 < typeQuery4A.length; i3++) {
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("dnsServerT4ADelayL");
                    sb2.append(i3 - 1);
                    bundle.putInt(sb2.toString(), typeQuery4A[i3]);
                }
                Intent chrIntent = new Intent(HwNetworkManagementService.INTENT_DS_DNS_STAT);
                chrIntent.putExtras(bundle);
                HwNetworkManagementService.this.mContext.sendBroadcast(chrIntent, HwNetworkManagementService.CHR_BROADCAST_PERMISSION);
            }
        }

        @Override // com.android.internal.net.IOemNetdUnsolicitedEventListener
        public void onWebStatInfoReport(String[] cooked, String raw) throws RemoteException {
            Slog.e(HwNetworkManagementService.TAG, "onWebStatInfoReport");
            HwNetworkManagementService.this.sendWebStatMessage(cooked, raw);
        }

        @Override // com.android.internal.net.IOemNetdUnsolicitedEventListener
        public void onApkDownloadUrlDetected(String[] cooked, String raw) throws RemoteException {
            Slog.i(HwNetworkManagementService.TAG, "onApkDownloadUrlDetected");
            HwNetworkManagementService.this.sendApkDownloadUrlBroadcast(cooked, raw);
        }

        @Override // com.android.internal.net.IOemNetdUnsolicitedEventListener
        public void OnDnsResultChanged(int uid, String host, int netType, String[] v4Addrs, String[] v6Addrs) {
            if (uid < 0 || TextUtils.isEmpty(host) || netType < 0 || (v4Addrs == null && v6Addrs == null)) {
                Slog.e(HwNetworkManagementService.TAG, "OnDnsResultChanged rst invalid, return");
            } else {
                EmcomManager.getInstance().notifyMpDnsResult(uid, host, netType, v4Addrs, v6Addrs);
            }
        }
    }

    private void connectOemNetdService(NetworkManagementService.SystemServices services) {
        if (services == null) {
            Slog.e(TAG, "connectOemNetdService services is null");
            return;
        }
        try {
            this.mOemNetdBinder = services.getNetd().getOemNetd();
            this.mOemNetdService = IOemNetd.Stub.asInterface(this.mOemNetdBinder);
            if (this.mOemNetdService == null) {
                Slog.e(TAG, "connectOemNetdService mOemNetdService is null");
            } else {
                this.mOemNetdService.registerOemUnsolicitedEventListener(this.mOemNetdUnsolicitedEventListener);
            }
        } catch (RemoteException | ServiceSpecificException e) {
            String str = TAG;
            Slog.e(str, "Failed to set Oem unsolicited event listener " + e);
        }
    }

    public HwNetworkManagementService(Context context, NetworkManagementService.SystemServices services) {
        super(context, services);
        this.mContext = context;
        this.mDnsResolver = IDnsResolver.Stub.asInterface(ServiceManager.getService("dnsresolver"));
        this.mOemNetdUnsolicitedEventListener = new OemNetdUnsolicitedEventListener();
        connectOemNetdService(services);
        this.mCmdId = new AtomicInteger(0);
        IntentFilter filter = new IntentFilter("com.huawei.systemserver.START");
        Context context2 = this.mContext;
        if (context2 != null) {
            context2.registerReceiver(this.mHwSystemServerStartReceiver, filter);
        }
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (code == 201) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
            return executeHsmCommand(data, reply);
        } else if (code == 1005) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
            List<String> result = getApLinkedStaList();
            reply.writeNoException();
            reply.writeStringList(result);
            return true;
        } else if (code == 1006) {
            Slog.d(TAG, "code == CODE_SET_SOFTAP_MACFILTER");
            data.enforceInterface(DESCRIPTOR);
            this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
            setSoftapMacFilter(data.readString());
            reply.writeNoException();
            return true;
        } else if (code == 1007) {
            Slog.d(TAG, "code == CODE_SET_SOFTAP_DISASSOCIATESTA");
            data.enforceInterface(DESCRIPTOR);
            this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
            setSoftapDisassociateSta(data.readString());
            reply.writeNoException();
            return true;
        } else if (code == 1008) {
            Slog.d(TAG, "code == CODE_SET_AP_CONFIGRATION_HW");
            data.enforceInterface(DESCRIPTOR);
            this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
            this.mWlanIface = data.readString();
            this.mSoftapIface = data.readString();
            reply.writeNoException();
            setAccessPointHw(this.mWlanIface, this.mSoftapIface);
            return true;
        } else if (code == 1009) {
            Slog.d(TAG, "code == CODE_SET_SOFTAP_TX_POWER");
            data.enforceInterface(DESCRIPTOR);
            this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
            setWifiTxPower(data.readString());
            reply.writeNoException();
            return true;
        } else if (code == 1011) {
            Slog.d(TAG, "code == CODE_GET_WIFI_DNS_STAT");
            data.enforceInterface(DESCRIPTOR);
            this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
            String stats = getWiFiDnsStats(data.readInt());
            reply.writeNoException();
            reply.writeString(stats);
            return true;
        } else {
            boolean needReset = false;
            if (code == CODE_SET_AD_STRATEGY_RULE) {
                Slog.d(TAG, "code == CODE_SET_AD_STRATEGY_RULE");
                data.enforceInterface(DESCRIPTOR_NETWORKMANAGEMENT_SERVICE);
                this.mContext.enforceCallingOrSelfPermission(AD_APKDL_STRATEGY_PERMISSION, TAG);
                int size = data.readInt();
                Slog.d(TAG, "CODE_SET_AD_STRATEGY_RULE, adStrategy size=" + size);
                String adStrategyStr = getStrategyStr(CODE_SET_AD_STRATEGY_RULE, size, data);
                Slog.d(TAG, "CODE_SET_AD_STRATEGY_RULE, adStrategyStr=" + adStrategyStr);
                if (data.readInt() > 0) {
                    needReset = true;
                }
                Slog.d(TAG, "CODE_SET_AD_STRATEGY_RULE, needReset=" + needReset);
                setAdFilterRules(adStrategyStr, needReset);
                reply.writeNoException();
                return true;
            } else if (code == CODE_SET_APK_DL_STRATEGY) {
                Slog.d(TAG, "code == CODE_SET_APK_DL_STRATEGY");
                data.enforceInterface(DESCRIPTOR_NETWORKMANAGEMENT_SERVICE);
                this.mContext.enforceCallingOrSelfPermission(AD_APKDL_STRATEGY_PERMISSION, TAG);
                String[] pkgName = data.createStringArray();
                if (data.readInt() > 0) {
                    needReset = true;
                }
                if (pkgName != null) {
                    Slog.d(TAG, "CODE_SET_APK_DL_STRATEGY, pkgName=" + Arrays.asList(pkgName) + ", needReset=" + needReset);
                }
                ArrayList<String> appUidList = convertPkgNameToUid(pkgName);
                Slog.d(TAG, "CODE_SET_APK_DL_STRATEGY, appUidList=" + appUidList);
                setApkDlFilterRules(appUidList, needReset);
                reply.writeNoException();
                return true;
            } else if (code == CODE_CLEAR_AD_APKDL_STRATEGY) {
                Slog.d(TAG, "code == CODE_CLEAR_AD_APKDL_STRATEGY");
                data.enforceInterface(DESCRIPTOR_NETWORKMANAGEMENT_SERVICE);
                this.mContext.enforceCallingOrSelfPermission(AD_APKDL_STRATEGY_PERMISSION, TAG);
                String[] pkgName2 = data.createStringArray();
                if (data.readInt() > 0) {
                    needReset = true;
                }
                int strategy = data.readInt();
                if (pkgName2 != null) {
                    Slog.d(TAG, "CODE_CLEAR_AD_APKDL_STRATEGY, pkgName=" + Arrays.asList(pkgName2) + ", needReset=" + needReset + ", strategy=" + strategy);
                }
                ArrayList<String> appUidList2 = convertPkgNameToUid(pkgName2);
                Slog.d(TAG, "CODE_CLEAR_AD_APKDL_STRATEGY, appUidList=" + appUidList2);
                clearAdOrApkDlFilterRules(appUidList2, needReset, strategy);
                reply.writeNoException();
                return true;
            } else if (code == CODE_PRINT_AD_APKDL_STRATEGY) {
                Slog.d(TAG, "code == CODE_PRINT_AD_APKDL_STRATEGY");
                data.enforceInterface(DESCRIPTOR_NETWORKMANAGEMENT_SERVICE);
                this.mContext.enforceCallingOrSelfPermission(AD_APKDL_STRATEGY_PERMISSION, TAG);
                int strategy2 = data.readInt();
                Slog.d(TAG, "CODE_PRINT_AD_APKDL_STRATEGY, strategy=" + strategy2);
                printAdOrApkDlFilterRules(strategy2);
                reply.writeNoException();
                return true;
            } else if (code == CODE_SET_APK_DL_URL_USER_RESULT) {
                Slog.d(TAG, "code == CODE_SET_APK_DL_URL_USER_RESULT");
                data.enforceInterface(DESCRIPTOR_NETWORKMANAGEMENT_SERVICE);
                this.mContext.enforceCallingOrSelfPermission(AD_APKDL_STRATEGY_PERMISSION, TAG);
                String downloadId = data.readString();
                if (data.readInt() > 0) {
                    needReset = true;
                }
                Slog.d(TAG, "CODE_SET_APK_DL_URL_USER_RESULT, downloadId=" + downloadId + ", result=" + needReset);
                setApkDlUrlUserResult(downloadId, needReset);
                reply.writeNoException();
                return true;
            } else if (code == CODE_IP_TABLE_CONFIG) {
                Slog.d(TAG, "code == CODE_IP_TABLE_CONFIG");
                data.enforceInterface(DESCRIPTOR_NETWORKMANAGEMENT_SERVICE);
                boolean enable = data.readBoolean();
                String iface = data.readString();
                int uid = data.readInt();
                IBinder binder = data.readStrongBinder();
                Slog.d(TAG, "CODE_IP_TABLE_CONFIG, enable: " + enable + ", iface: " + iface + ", uid: " + uid);
                ipTableConfig(enable, iface, uid, binder);
                reply.writeNoException();
                return true;
            } else if (code == 1106) {
                setOrAddNetworkAccessList(data, reply);
                return true;
            } else if (code == CODE_GET_CHR_UID_LIST) {
                getChrUidList(data, reply);
                return true;
            } else if (code == 1107) {
                data.enforceInterface(DESCRIPTOR_NETWORKMANAGEMENT_SERVICE);
                this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
                closeSocketsForUid(data.readInt());
                reply.writeNoException();
                return true;
            } else if (code == CODE_GET_NETD_PID_CMD) {
                data.enforceInterface(DESCRIPTOR_NETWORKMANAGEMENT_SERVICE);
                this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
                int pid = getNetdPid();
                reply.writeNoException();
                reply.writeInt(pid);
                return true;
            } else if (code == 1108) {
                Slog.d(TAG, "code == CODE_SET_CHR_REPORT_APP_LIST");
                data.enforceInterface(DESCRIPTOR_NETWORKMANAGEMENT_SERVICE);
                this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
                setChrReportUid(data.readInt(), data.readInt());
                return true;
            } else if (code == CODE_SET_APK_CONTROL_STRATEGY) {
                Slog.d(TAG, "code == CODE_SET_APK_CONTROL_STRATEGY");
                data.enforceInterface(DESCRIPTOR_NETWORKMANAGEMENT_SERVICE);
                this.mContext.enforceCallingOrSelfPermission(AD_APKDL_STRATEGY_PERMISSION, TAG);
                int size2 = data.readInt();
                Slog.d(TAG, "CODE_SET_APK_CONTROL_STRATEGY, apkStrategy size=" + size2);
                String apkStrategyStr = getStrategyStr(CODE_SET_APK_CONTROL_STRATEGY, size2, data);
                Slog.d(TAG, "CODE_SET_APK_CONTROL_STRATEGY, apkStrategyStr=" + apkStrategyStr);
                if (data.readInt() > 0) {
                    needReset = true;
                }
                Slog.d(TAG, "CODE_SET_AD_STRATEGY_RULE, needReset=" + needReset);
                setApkControlFilterRules(apkStrategyStr, needReset);
                reply.writeNoException();
                return true;
            } else if (code == CODE_SET_PG_FILTER_CMD) {
                data.enforceInterface(DESCRIPTOR_NETWORKMANAGEMENT_SERVICE);
                this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
                boolean pgNetFilterSetRule = pgNetFilterSetRule(data.readInt(), data.createIntArray(), data.createIntArray());
                reply.writeNoException();
                reply.writeInt(pgNetFilterSetRule ? 1 : 0);
                return true;
            } else if (code == CODE_SET_MPDNS_APP) {
                Slog.d(TAG, "rece CODE_SET_MPDNS_APP");
                data.enforceInterface(DESCRIPTOR_NETWORKMANAGEMENT_SERVICE);
                this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
                setMpDnsApp(data.readInt(), data.readInt(), data.createStringArray());
                reply.writeNoException();
                return true;
            } else if (code == CODE_SET_DNS_FORWARDING_CMD) {
                Slog.d(TAG, "code == CODE_SET_DNS_FORWARDING_CMD");
                data.enforceInterface(DESCRIPTOR_NETWORKMANAGEMENT_SERVICE);
                this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
                if (data.readInt() != 0) {
                    needReset = true;
                }
                String dnsServer = data.readString();
                Slog.d(TAG, "CODE_SET_DNS_FORWARDING_CMD, enabled=" + needReset + ", dnsServer=" + dnsServer);
                setDnsForwarding(needReset, dnsServer);
                reply.writeNoException();
                return true;
            } else if (code == CODE_SET_INTERFACE_PROXY_ARP_CMD) {
                Slog.d(TAG, "code == CODE_SET_INTERFACE_PROXY_ARP_CMD");
                data.enforceInterface(DESCRIPTOR_NETWORKMANAGEMENT_SERVICE);
                this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
                if (data.readInt() != 0) {
                    needReset = true;
                }
                String ifaceName = data.readString();
                Slog.d(TAG, "CODE_SET_INTERFACE_PROXY_ARP_CMD, enabled=" + needReset + ", ifaceName=" + ifaceName);
                setInterfaceProxyArp(needReset, ifaceName);
                reply.writeNoException();
                return true;
            } else if (code != CODE_SET_ARP_IGNORE_CMD) {
                return HwNetworkManagementService.super.onTransact(code, data, reply, flags);
            } else {
                Slog.d(TAG, "code == CODE_SET_ARP_IGNORE_CMD");
                data.enforceInterface(DESCRIPTOR_NETWORKMANAGEMENT_SERVICE);
                this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
                int value = data.readInt();
                String ifaceNameWlan0 = data.readString();
                String ifaceNameWlan1 = data.readString();
                String ifaceNameAll = data.readString();
                hwSetArpIgnore(value, ifaceNameWlan0);
                hwSetArpIgnore(value, ifaceNameWlan1);
                hwSetArpIgnore(value, ifaceNameAll);
                reply.writeNoException();
                return true;
            }
        }
    }

    private boolean executeHsmCommand(Parcel data, Parcel reply) {
        try {
            if (this.mOemNetdService == null) {
                Slog.w(TAG, "execute commond failed with service deid.");
                return false;
            }
            String cmd = data.readString();
            Object[] args = data.readArray(null);
            if (cmd != null) {
                if (args != null) {
                    if ("ifwhitelist".equals(cmd)) {
                        String ifaceName = String.valueOf(args[0]);
                        String status = String.valueOf(args[1]);
                        if ("enable_ifwhitelist".equals(status)) {
                            try {
                                this.mOemNetdService.hwBandwidthEnableInterfaceWhitelist(ifaceName);
                            } catch (RemoteException | ServiceSpecificException e) {
                                Slog.e(TAG, "Enanle with white list fail caused by service died.");
                                return false;
                            }
                        }
                        if ("disable_ifwhitelist".equals(status)) {
                            try {
                                this.mOemNetdService.hwBandwidthDisableInterfaceWhitelist(ifaceName);
                            } catch (RemoteException | ServiceSpecificException e2) {
                                Slog.e(TAG, "Disable with white list fail caused by service died.");
                                return false;
                            }
                        }
                        return true;
                    } else if ("firewall".equals(cmd)) {
                        String ifaceName2 = String.valueOf(args[0]);
                        String policyStatus = String.valueOf(args[1]);
                        String uid = String.valueOf(args[2]);
                        if ("block".equals(policyStatus)) {
                            try {
                                this.mOemNetdService.hwBandwidthAddNaughtyApps(ifaceName2, Integer.parseInt(uid));
                            } catch (RemoteException | ServiceSpecificException | NumberFormatException e3) {
                                Slog.e(TAG, "Add naughty apps fail with uid = " + uid + " caused by service died.");
                                return false;
                            }
                        }
                        if ("allow".equals(policyStatus)) {
                            try {
                                this.mOemNetdService.hwBandwidthRemoveNaughtyApps(ifaceName2, Integer.parseInt(uid));
                            } catch (RemoteException | ServiceSpecificException | NumberFormatException e4) {
                                Slog.e(TAG, "Remove naughty apps fail with uid = " + uid + " caused by service died.");
                                return false;
                            }
                        }
                        return true;
                    } else if ("requestSwap".equals(cmd)) {
                        try {
                            this.mOemNetdService.trafficSwapActiveProcessStatsMap();
                            Slog.i(TAG, "request swap success.");
                            return true;
                        } catch (RemoteException | ServiceSpecificException e5) {
                            Slog.e(TAG, "request swap failed caused by service died.");
                            return false;
                        }
                    } else if ("removeUids".equals(cmd)) {
                        int[] uids = new int[args.length];
                        for (int i = 0; i < args.length; i++) {
                            Object object = args[i];
                            if (object instanceof Integer) {
                                uids[i] = ((Integer) object).intValue();
                            }
                        }
                        getStatsFactory().removeUids(uids);
                        return true;
                    } else {
                        Slog.i(TAG, "Can not match the commond mark, so ignored.");
                        return false;
                    }
                }
            }
            Slog.w(TAG, "cmd is null or args is null.");
            return false;
        } catch (Exception e6) {
            return false;
        }
    }

    public void setConnector(NativeDaemonConnector connector) {
        this.mConnector = connector;
    }

    private String getChannel(WifiConfiguration wifiConfig) {
        if (wifiConfig.apBand != 0 || !SystemProperties.getBoolean(ISM_COEX_ON, false)) {
            this.mChannel = 0;
            if (this.mChannel == 0 || ((wifiConfig.apBand == 0 && this.mChannel > 14) || (wifiConfig.apBand == 1 && this.mChannel < 34))) {
                this.mChannel = wifiConfig.apChannel;
            }
        } else {
            this.mChannel = 11;
        }
        String str = TAG;
        Slog.d(str, "channel=" + this.mChannel);
        return String.valueOf(this.mChannel);
    }

    private String getMaxscb() {
        String str = TAG;
        Slog.d(str, "maxscb=0");
        return String.valueOf(0);
    }

    private static String getSecurityType(WifiConfiguration wifiConfig) {
        int authType = wifiConfig.getAuthType();
        if (authType == 1) {
            return "wpa-psk";
        }
        if (authType != 4) {
            return HwWmConstants.LAUNCHER_TYPE_VALUE_OPEN;
        }
        return "wpa2-psk";
    }

    public String getIgnorebroadcastssid() {
        if (1 != SettingsEx.Systemex.getInt(this.mContext.getContentResolver(), "show_broadcast_ssid_config", 0)) {
            return "broadcast";
        }
        String iIgnorebroadcastssidStr = Settings.Secure.getInt(this.mContext.getContentResolver(), "wifi_ap_ignorebroadcastssid", 0) == 0 ? "broadcast" : "hidden";
        String str = TAG;
        Slog.d(str, "iIgnorebroadcastssidStr=" + iIgnorebroadcastssidStr);
        return iIgnorebroadcastssidStr;
    }

    public void startAccessPointWithChannel(WifiConfiguration wifiConfig, String wlanIface) {
        if (wifiConfig != null) {
            try {
                this.mConnector.execute("softap", new Object[]{ARG_SET, wlanIface, wifiConfig.SSID, getIgnorebroadcastssid(), getChannel(wifiConfig), getSecurityType(wifiConfig), new NativeDaemonConnector.SensitiveArg(wifiConfig.preSharedKey), getMaxscb()});
                this.mConnector.execute("softap", new Object[]{"startap"});
            } catch (NativeDaemonConnectorException e) {
                throw e.rethrowAsParcelableException();
            }
        }
    }

    public void sendDataSpeedSlowMessage(String[] cooked, String raw) {
        if (cooked.length < 2 || !cooked[1].equals("sourceAddress")) {
            String msg1 = String.format("Invalid event from daemon (%s)", raw);
            Slog.d(TAG, "receive DataSpeedSlowDetected,return error 1");
            throw new IllegalStateException(msg1);
        }
        int sourceAddress = 0;
        try {
            sourceAddress = Integer.parseInt(cooked[2]);
        } catch (NumberFormatException e) {
            Slog.e(TAG, "sendDataSpeedSlowMessage get sourceAddress failed.");
        }
        NetworkInfo mobileNetinfo = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getNetworkInfo(0);
        Slog.d(TAG, "onEvent receive DataSpeedSlowDetected");
        if (mobileNetinfo != null && mobileNetinfo.isConnected()) {
            Slog.d(TAG, "onEvent receive DataSpeedSlowDetected,mobile network is connected!");
            Intent chrIntent = new Intent("com.huawei.intent.action.data_speed_slow");
            chrIntent.putExtra("sourceAddress", sourceAddress);
            this.mContext.sendBroadcast(chrIntent, CHR_BROADCAST_PERMISSION);
        }
    }

    public void sendDSCPChangeMessage(String[] cooked, String raw) {
        if (cooked.length < 4 || !cooked[1].equals("DSCPINFO")) {
            String msg1 = String.format("Invalid event from daemon (%s)", raw);
            Slog.d(TAG, "receive sendDSCPChangeMessage,return error 1");
            throw new IllegalStateException(msg1);
        }
        NetworkInfo networkInfoWlan = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getNetworkInfo(1);
        if (networkInfoWlan != null) {
            try {
                if (networkInfoWlan.isConnected()) {
                    Intent chrIntent = new Intent("com.android.intent.action.wifi_dscp_change");
                    chrIntent.putExtra("dscpvalue", Integer.parseInt(cooked[2]));
                    chrIntent.putExtra("uid", Integer.parseInt(cooked[3]));
                    this.mContext.sendBroadcast(chrIntent, CHR_BROADCAST_PERMISSION);
                }
            } catch (NumberFormatException e) {
                Slog.e(TAG, "sendDSCPChangeMessage get dscpvalue or uid failed.");
            }
        }
    }

    public void fillWifiAppInfo(String[] cooked, Intent wifichrIntent) {
        for (int index = 0; index < 10; index++) {
            if (this.mChrAppUidArray[index] != 0) {
                wifichrIntent.putExtra(String.format(Locale.ENGLISH, "App%dUid", Integer.valueOf(index + 1)), this.mChrAppUidArray[index]);
                wifichrIntent.putExtra(String.format(Locale.ENGLISH, "App%dWebDelay", Integer.valueOf(index + 1)), Integer.parseInt(cooked[(index * 19) + 279]));
                wifichrIntent.putExtra(String.format(Locale.ENGLISH, "App%dSuccNum", Integer.valueOf(index + 1)), Integer.parseInt(cooked[(index * 19) + 280]));
                wifichrIntent.putExtra(String.format(Locale.ENGLISH, "App%dFailNum", Integer.valueOf(index + 1)), Integer.parseInt(cooked[(index * 19) + 281]));
                wifichrIntent.putExtra(String.format(Locale.ENGLISH, "App%dNoAckNum", Integer.valueOf(index + 1)), Integer.parseInt(cooked[(index * 19) + 282]));
                wifichrIntent.putExtra(String.format(Locale.ENGLISH, "App%dTotalNum", Integer.valueOf(index + 1)), Integer.parseInt(cooked[(index * 19) + 283]));
                wifichrIntent.putExtra(String.format(Locale.ENGLISH, "App%dTcpTotalNum", Integer.valueOf(index + 1)), Integer.parseInt(cooked[(index * 19) + 284]));
                wifichrIntent.putExtra(String.format(Locale.ENGLISH, "App%dTcpSuccNum", Integer.valueOf(index + 1)), Integer.parseInt(cooked[(index * 19) + 285]));
                wifichrIntent.putExtra(String.format(Locale.ENGLISH, "App%dDelayL1", Integer.valueOf(index + 1)), Integer.parseInt(cooked[(index * 19) + 286]));
                wifichrIntent.putExtra(String.format(Locale.ENGLISH, "App%dDelayL2", Integer.valueOf(index + 1)), Integer.parseInt(cooked[(index * 19) + 287]));
                wifichrIntent.putExtra(String.format(Locale.ENGLISH, "App%dDelayL3", Integer.valueOf(index + 1)), Integer.parseInt(cooked[(index * 19) + 288]));
                wifichrIntent.putExtra(String.format(Locale.ENGLISH, "App%dDelayL4", Integer.valueOf(index + 1)), Integer.parseInt(cooked[(index * 19) + 289]));
                wifichrIntent.putExtra(String.format(Locale.ENGLISH, "App%dDelayL5", Integer.valueOf(index + 1)), Integer.parseInt(cooked[(index * 19) + 290]));
                wifichrIntent.putExtra(String.format(Locale.ENGLISH, "App%dDelayL6", Integer.valueOf(index + 1)), Integer.parseInt(cooked[(index * 19) + 291]));
                wifichrIntent.putExtra(String.format(Locale.ENGLISH, "App%dRTTL1", Integer.valueOf(index + 1)), Integer.parseInt(cooked[(index * 19) + 292]));
                wifichrIntent.putExtra(String.format(Locale.ENGLISH, "App%dRTTL2", Integer.valueOf(index + 1)), Integer.parseInt(cooked[(index * 19) + 293]));
                wifichrIntent.putExtra(String.format(Locale.ENGLISH, "App%dRTTL3", Integer.valueOf(index + 1)), Integer.parseInt(cooked[(index * 19) + 294]));
                wifichrIntent.putExtra(String.format(Locale.ENGLISH, "App%dRTTL4", Integer.valueOf(index + 1)), Integer.parseInt(cooked[(index * 19) + 295]));
                wifichrIntent.putExtra(String.format(Locale.ENGLISH, "App%dRTTL5", Integer.valueOf(index + 1)), Integer.parseInt(cooked[(index * 19) + 296]));
            }
        }
    }

    public void sendWebStatMessage(String[] cooked, String raw) {
        boolean z;
        int WifiWebDelay;
        if (cooked.length < MOBILE_STAT_MAX_NUM || !cooked[1].equals("ReportType")) {
            throw new IllegalStateException(String.format("Invalid event from daemon (%s)", raw));
        }
        try {
            Slog.d(TAG, "onEvent receive Web Stat Report:" + raw);
            if (isOnlyMobileDataConnected()) {
                try {
                    Intent chrIntent = new Intent("com.huawei.intent.action.web_stat_report");
                    chrIntent.putExtra("ReportType", Integer.parseInt(cooked[2]));
                    chrIntent.putExtra("RTT", Integer.parseInt(cooked[3]));
                    chrIntent.putExtra("WebDelay", Integer.parseInt(cooked[4]));
                    chrIntent.putExtra("SuccNum", Integer.parseInt(cooked[5]));
                    chrIntent.putExtra("FailNum", Integer.parseInt(cooked[6]));
                    chrIntent.putExtra("NoAckNum", Integer.parseInt(cooked[7]));
                    chrIntent.putExtra("TotalNum", Integer.parseInt(cooked[8]));
                    z = false;
                    try {
                        chrIntent.putExtra("TcpTotalNum", Integer.parseInt(cooked[9]));
                        chrIntent.putExtra("DelayL1", Integer.parseInt(cooked[10]));
                        chrIntent.putExtra("DelayL2", Integer.parseInt(cooked[11]));
                        chrIntent.putExtra("DelayL3", Integer.parseInt(cooked[12]));
                        chrIntent.putExtra("DelayL4", Integer.parseInt(cooked[13]));
                        chrIntent.putExtra("DelayL5", Integer.parseInt(cooked[14]));
                        chrIntent.putExtra("DelayL6", Integer.parseInt(cooked[15]));
                        chrIntent.putExtra("RTTL1", Integer.parseInt(cooked[16]));
                        chrIntent.putExtra("RTTL2", Integer.parseInt(cooked[17]));
                        chrIntent.putExtra("RTTL3", Integer.parseInt(cooked[18]));
                        chrIntent.putExtra("RTTL4", Integer.parseInt(cooked[19]));
                        chrIntent.putExtra("RTTL5", Integer.parseInt(cooked[20]));
                        chrIntent.putExtra("TcpSuccNum", Integer.parseInt(cooked[21]));
                        chrIntent.putExtra("SocketUid", Integer.parseInt(cooked[22]));
                        chrIntent.putExtra("WebFailCode", Integer.parseInt(cooked[23]));
                        chrIntent.putExtra("App1RTT", Integer.parseInt(cooked[24]));
                        chrIntent.putExtra("App1WebDelay", Integer.parseInt(cooked[25]));
                        chrIntent.putExtra("App1SuccNum", Integer.parseInt(cooked[26]));
                        chrIntent.putExtra("App1FailNum", Integer.parseInt(cooked[27]));
                        chrIntent.putExtra("App1NoAckNum", Integer.parseInt(cooked[28]));
                        chrIntent.putExtra("App1TotalNum", Integer.parseInt(cooked[29]));
                        chrIntent.putExtra("App1TcpTotalNum", Integer.parseInt(cooked[30]));
                        chrIntent.putExtra("App1TcpSuccNum", Integer.parseInt(cooked[31]));
                        chrIntent.putExtra("App1DelayL1", Integer.parseInt(cooked[32]));
                        chrIntent.putExtra("App1DelayL2", Integer.parseInt(cooked[33]));
                        chrIntent.putExtra("App1DelayL3", Integer.parseInt(cooked[34]));
                        chrIntent.putExtra("App1DelayL4", Integer.parseInt(cooked[35]));
                        chrIntent.putExtra("App1DelayL5", Integer.parseInt(cooked[36]));
                        chrIntent.putExtra("App1DelayL6", Integer.parseInt(cooked[37]));
                        chrIntent.putExtra("App1RTTL1", Integer.parseInt(cooked[38]));
                        chrIntent.putExtra("App1RTTL2", Integer.parseInt(cooked[39]));
                        chrIntent.putExtra("App1RTTL3", Integer.parseInt(cooked[40]));
                        chrIntent.putExtra("App1RTTL4", Integer.parseInt(cooked[41]));
                        chrIntent.putExtra("App1RTTL5", Integer.parseInt(cooked[42]));
                        chrIntent.putExtra("App2RTT", Integer.parseInt(cooked[43]));
                        chrIntent.putExtra("App2WebDelay", Integer.parseInt(cooked[44]));
                        chrIntent.putExtra("App2SuccNum", Integer.parseInt(cooked[45]));
                        chrIntent.putExtra("App2FailNum", Integer.parseInt(cooked[46]));
                        chrIntent.putExtra("App2NoAckNum", Integer.parseInt(cooked[47]));
                        chrIntent.putExtra("App2TotalNum", Integer.parseInt(cooked[48]));
                        chrIntent.putExtra("App2TcpTotalNum", Integer.parseInt(cooked[49]));
                        chrIntent.putExtra("App2TcpSuccNum", Integer.parseInt(cooked[50]));
                        chrIntent.putExtra("App2DelayL1", Integer.parseInt(cooked[51]));
                        chrIntent.putExtra("App2DelayL2", Integer.parseInt(cooked[52]));
                        chrIntent.putExtra("App2DelayL3", Integer.parseInt(cooked[53]));
                        chrIntent.putExtra("App2DelayL4", Integer.parseInt(cooked[54]));
                        chrIntent.putExtra("App2DelayL5", Integer.parseInt(cooked[55]));
                        chrIntent.putExtra("App2DelayL6", Integer.parseInt(cooked[56]));
                        chrIntent.putExtra("App2RTTL1", Integer.parseInt(cooked[57]));
                        chrIntent.putExtra("App2RTTL2", Integer.parseInt(cooked[58]));
                        chrIntent.putExtra("App2RTTL3", Integer.parseInt(cooked[59]));
                        chrIntent.putExtra("App2RTTL4", Integer.parseInt(cooked[60]));
                        chrIntent.putExtra("App2RTTL5", Integer.parseInt(cooked[61]));
                        chrIntent.putExtra("App3RTT", Integer.parseInt(cooked[62]));
                        chrIntent.putExtra("App3WebDelay", Integer.parseInt(cooked[63]));
                        chrIntent.putExtra("App3SuccNum", Integer.parseInt(cooked[64]));
                        chrIntent.putExtra("App3FailNum", Integer.parseInt(cooked[65]));
                        chrIntent.putExtra("App3NoAckNum", Integer.parseInt(cooked[66]));
                        chrIntent.putExtra("App3TotalNum", Integer.parseInt(cooked[67]));
                        chrIntent.putExtra("App3TcpTotalNum", Integer.parseInt(cooked[68]));
                        chrIntent.putExtra("App3TcpSuccNum", Integer.parseInt(cooked[69]));
                        chrIntent.putExtra("App3DelayL1", Integer.parseInt(cooked[70]));
                        chrIntent.putExtra("App3DelayL2", Integer.parseInt(cooked[71]));
                        chrIntent.putExtra("App3DelayL3", Integer.parseInt(cooked[72]));
                        chrIntent.putExtra("App3DelayL4", Integer.parseInt(cooked[73]));
                        chrIntent.putExtra("App3DelayL5", Integer.parseInt(cooked[74]));
                        chrIntent.putExtra("App3DelayL6", Integer.parseInt(cooked[75]));
                        chrIntent.putExtra("App3RTTL1", Integer.parseInt(cooked[76]));
                        chrIntent.putExtra("App3RTTL2", Integer.parseInt(cooked[77]));
                        chrIntent.putExtra("App3RTTL3", Integer.parseInt(cooked[78]));
                        chrIntent.putExtra("App3RTTL4", Integer.parseInt(cooked[79]));
                        chrIntent.putExtra("App3RTTL5", Integer.parseInt(cooked[80]));
                        chrIntent.putExtra("App4RTT", Integer.parseInt(cooked[81]));
                        chrIntent.putExtra("App4WebDelay", Integer.parseInt(cooked[82]));
                        chrIntent.putExtra("App4SuccNum", Integer.parseInt(cooked[83]));
                        chrIntent.putExtra("App4FailNum", Integer.parseInt(cooked[84]));
                        chrIntent.putExtra("App4NoAckNum", Integer.parseInt(cooked[85]));
                        chrIntent.putExtra("App4TotalNum", Integer.parseInt(cooked[86]));
                        chrIntent.putExtra("App4TcpTotalNum", Integer.parseInt(cooked[87]));
                        chrIntent.putExtra("App4TcpSuccNum", Integer.parseInt(cooked[88]));
                        chrIntent.putExtra("App4DelayL1", Integer.parseInt(cooked[89]));
                        chrIntent.putExtra("App4DelayL2", Integer.parseInt(cooked[90]));
                        chrIntent.putExtra("App4DelayL3", Integer.parseInt(cooked[91]));
                        chrIntent.putExtra("App4DelayL4", Integer.parseInt(cooked[92]));
                        chrIntent.putExtra("App4DelayL5", Integer.parseInt(cooked[93]));
                        chrIntent.putExtra("App4DelayL6", Integer.parseInt(cooked[94]));
                        chrIntent.putExtra("App4RTTL1", Integer.parseInt(cooked[95]));
                        chrIntent.putExtra("App4RTTL2", Integer.parseInt(cooked[96]));
                        chrIntent.putExtra("App4RTTL3", Integer.parseInt(cooked[97]));
                        chrIntent.putExtra("App4RTTL4", Integer.parseInt(cooked[98]));
                        chrIntent.putExtra("App4RTTL5", Integer.parseInt(cooked[99]));
                        chrIntent.putExtra("App5RTT", Integer.parseInt(cooked[100]));
                        chrIntent.putExtra("App5WebDelay", Integer.parseInt(cooked[101]));
                        chrIntent.putExtra("App5SuccNum", Integer.parseInt(cooked[102]));
                        chrIntent.putExtra("App5FailNum", Integer.parseInt(cooked[103]));
                        chrIntent.putExtra("App5NoAckNum", Integer.parseInt(cooked[104]));
                        chrIntent.putExtra("App5TotalNum", Integer.parseInt(cooked[105]));
                        chrIntent.putExtra("App5TcpTotalNum", Integer.parseInt(cooked[106]));
                        chrIntent.putExtra("App5TcpSuccNum", Integer.parseInt(cooked[107]));
                        chrIntent.putExtra("App5DelayL1", Integer.parseInt(cooked[108]));
                        chrIntent.putExtra("App5DelayL2", Integer.parseInt(cooked[109]));
                        chrIntent.putExtra("App5DelayL3", Integer.parseInt(cooked[110]));
                        chrIntent.putExtra("App5DelayL4", Integer.parseInt(cooked[111]));
                        chrIntent.putExtra("App5DelayL5", Integer.parseInt(cooked[112]));
                        chrIntent.putExtra("App5DelayL6", Integer.parseInt(cooked[113]));
                        chrIntent.putExtra("App5RTTL1", Integer.parseInt(cooked[114]));
                        chrIntent.putExtra("App5RTTL2", Integer.parseInt(cooked[115]));
                        chrIntent.putExtra("App5RTTL3", Integer.parseInt(cooked[116]));
                        chrIntent.putExtra("App5RTTL4", Integer.parseInt(cooked[117]));
                        chrIntent.putExtra("App5RTTL5", Integer.parseInt(cooked[118]));
                        chrIntent.putExtra("App6RTT", Integer.parseInt(cooked[119]));
                        chrIntent.putExtra("App6WebDelay", Integer.parseInt(cooked[120]));
                        chrIntent.putExtra("App6SuccNum", Integer.parseInt(cooked[121]));
                        chrIntent.putExtra("App6FailNum", Integer.parseInt(cooked[122]));
                        chrIntent.putExtra("App6NoAckNum", Integer.parseInt(cooked[123]));
                        chrIntent.putExtra("App6TotalNum", Integer.parseInt(cooked[124]));
                        chrIntent.putExtra("App6TcpTotalNum", Integer.parseInt(cooked[125]));
                        chrIntent.putExtra("App6TcpSuccNum", Integer.parseInt(cooked[126]));
                        chrIntent.putExtra("App6DelayL1", Integer.parseInt(cooked[127]));
                        chrIntent.putExtra("App6DelayL2", Integer.parseInt(cooked[128]));
                        chrIntent.putExtra("App6DelayL3", Integer.parseInt(cooked[129]));
                        chrIntent.putExtra("App6DelayL4", Integer.parseInt(cooked[130]));
                        chrIntent.putExtra("App6DelayL5", Integer.parseInt(cooked[131]));
                        chrIntent.putExtra("App6DelayL6", Integer.parseInt(cooked[132]));
                        chrIntent.putExtra("App6RTTL1", Integer.parseInt(cooked[133]));
                        chrIntent.putExtra("App6RTTL2", Integer.parseInt(cooked[134]));
                        chrIntent.putExtra("App6RTTL3", Integer.parseInt(cooked[135]));
                        chrIntent.putExtra("App6RTTL4", Integer.parseInt(cooked[136]));
                        chrIntent.putExtra("App6RTTL5", Integer.parseInt(cooked[137]));
                        chrIntent.putExtra("App7RTT", Integer.parseInt(cooked[138]));
                        chrIntent.putExtra("App7WebDelay", Integer.parseInt(cooked[139]));
                        chrIntent.putExtra("App7SuccNum", Integer.parseInt(cooked[140]));
                        chrIntent.putExtra("App7FailNum", Integer.parseInt(cooked[141]));
                        chrIntent.putExtra("App7NoAckNum", Integer.parseInt(cooked[142]));
                        chrIntent.putExtra("App7TotalNum", Integer.parseInt(cooked[143]));
                        chrIntent.putExtra("App7TcpTotalNum", Integer.parseInt(cooked[144]));
                        chrIntent.putExtra("App7TcpSuccNum", Integer.parseInt(cooked[145]));
                        chrIntent.putExtra("App7DelayL1", Integer.parseInt(cooked[146]));
                        chrIntent.putExtra("App7DelayL2", Integer.parseInt(cooked[147]));
                        chrIntent.putExtra("App7DelayL3", Integer.parseInt(cooked[148]));
                        chrIntent.putExtra("App7DelayL4", Integer.parseInt(cooked[149]));
                        chrIntent.putExtra("App7DelayL5", Integer.parseInt(cooked[150]));
                        chrIntent.putExtra("App7DelayL6", Integer.parseInt(cooked[151]));
                        chrIntent.putExtra("App7RTTL1", Integer.parseInt(cooked[152]));
                        chrIntent.putExtra("App7RTTL2", Integer.parseInt(cooked[153]));
                        chrIntent.putExtra("App7RTTL3", Integer.parseInt(cooked[154]));
                        chrIntent.putExtra("App7RTTL4", Integer.parseInt(cooked[155]));
                        chrIntent.putExtra("App7RTTL5", Integer.parseInt(cooked[156]));
                        chrIntent.putExtra("App8RTT", Integer.parseInt(cooked[157]));
                        chrIntent.putExtra("App8WebDelay", Integer.parseInt(cooked[158]));
                        chrIntent.putExtra("App8SuccNum", Integer.parseInt(cooked[159]));
                        chrIntent.putExtra("App8FailNum", Integer.parseInt(cooked[160]));
                        chrIntent.putExtra("App8NoAckNum", Integer.parseInt(cooked[161]));
                        chrIntent.putExtra("App8TotalNum", Integer.parseInt(cooked[162]));
                        chrIntent.putExtra("App8TcpTotalNum", Integer.parseInt(cooked[163]));
                        chrIntent.putExtra("App8TcpSuccNum", Integer.parseInt(cooked[164]));
                        chrIntent.putExtra("App8DelayL1", Integer.parseInt(cooked[165]));
                        chrIntent.putExtra("App8DelayL2", Integer.parseInt(cooked[166]));
                        chrIntent.putExtra("App8DelayL3", Integer.parseInt(cooked[167]));
                        chrIntent.putExtra("App8DelayL4", Integer.parseInt(cooked[168]));
                        chrIntent.putExtra("App8DelayL5", Integer.parseInt(cooked[169]));
                        chrIntent.putExtra("App8DelayL6", Integer.parseInt(cooked[170]));
                        chrIntent.putExtra("App8RTTL1", Integer.parseInt(cooked[171]));
                        chrIntent.putExtra("App8RTTL2", Integer.parseInt(cooked[172]));
                        chrIntent.putExtra("App8RTTL3", Integer.parseInt(cooked[173]));
                        chrIntent.putExtra("App8RTTL4", Integer.parseInt(cooked[174]));
                        chrIntent.putExtra("App8RTTL5", Integer.parseInt(cooked[175]));
                        chrIntent.putExtra("App9RTT", Integer.parseInt(cooked[176]));
                        chrIntent.putExtra("App9WebDelay", Integer.parseInt(cooked[177]));
                        chrIntent.putExtra("App9SuccNum", Integer.parseInt(cooked[178]));
                        chrIntent.putExtra("App9FailNum", Integer.parseInt(cooked[179]));
                        chrIntent.putExtra("App9NoAckNum", Integer.parseInt(cooked[180]));
                        chrIntent.putExtra("App9TotalNum", Integer.parseInt(cooked[181]));
                        chrIntent.putExtra("App9TcpTotalNum", Integer.parseInt(cooked[182]));
                        chrIntent.putExtra("App9TcpSuccNum", Integer.parseInt(cooked[183]));
                        chrIntent.putExtra("App9DelayL1", Integer.parseInt(cooked[184]));
                        chrIntent.putExtra("App9DelayL2", Integer.parseInt(cooked[185]));
                        chrIntent.putExtra("App9DelayL3", Integer.parseInt(cooked[186]));
                        chrIntent.putExtra("App9DelayL4", Integer.parseInt(cooked[187]));
                        chrIntent.putExtra("App9DelayL5", Integer.parseInt(cooked[188]));
                        chrIntent.putExtra("App9DelayL6", Integer.parseInt(cooked[189]));
                        chrIntent.putExtra("App9RTTL1", Integer.parseInt(cooked[190]));
                        chrIntent.putExtra("App9RTTL2", Integer.parseInt(cooked[191]));
                        chrIntent.putExtra("App9RTTL3", Integer.parseInt(cooked[192]));
                        chrIntent.putExtra("App9RTTL4", Integer.parseInt(cooked[193]));
                        chrIntent.putExtra("App9RTTL5", Integer.parseInt(cooked[194]));
                        chrIntent.putExtra("App10RTT", Integer.parseInt(cooked[195]));
                        chrIntent.putExtra("App10WebDelay", Integer.parseInt(cooked[196]));
                        chrIntent.putExtra("App10SuccNum", Integer.parseInt(cooked[197]));
                        chrIntent.putExtra("App10FailNum", Integer.parseInt(cooked[198]));
                        chrIntent.putExtra("App10NoAckNum", Integer.parseInt(cooked[199]));
                        chrIntent.putExtra("App10TotalNum", Integer.parseInt(cooked[200]));
                        chrIntent.putExtra("App10TcpTotalNum", Integer.parseInt(cooked[201]));
                        chrIntent.putExtra("App10TcpSuccNum", Integer.parseInt(cooked[202]));
                        chrIntent.putExtra("App10DelayL1", Integer.parseInt(cooked[203]));
                        chrIntent.putExtra("App10DelayL2", Integer.parseInt(cooked[204]));
                        chrIntent.putExtra("App10DelayL3", Integer.parseInt(cooked[205]));
                        chrIntent.putExtra("App10DelayL4", Integer.parseInt(cooked[206]));
                        chrIntent.putExtra("App10DelayL5", Integer.parseInt(cooked[207]));
                        chrIntent.putExtra("App10DelayL6", Integer.parseInt(cooked[208]));
                        chrIntent.putExtra("App10RTTL1", Integer.parseInt(cooked[209]));
                        chrIntent.putExtra("App10RTTL2", Integer.parseInt(cooked[210]));
                        chrIntent.putExtra("App10RTTL3", Integer.parseInt(cooked[211]));
                        chrIntent.putExtra("App10RTTL4", Integer.parseInt(cooked[212]));
                        chrIntent.putExtra("App10RTTL5", Integer.parseInt(cooked[213]));
                        chrIntent.putExtra("HighestTcpRTT", Integer.parseInt(cooked[214]));
                        chrIntent.putExtra("LowestTcpRTT", Integer.parseInt(cooked[215]));
                        chrIntent.putExtra("LastTcpRTT", Integer.parseInt(cooked[216]));
                        chrIntent.putExtra("HighestWebDelay", Integer.parseInt(cooked[217]));
                        chrIntent.putExtra("LowestWebDelay", Integer.parseInt(cooked[218]));
                        chrIntent.putExtra("LastWebDelay", Integer.parseInt(cooked[219]));
                        chrIntent.putExtra("ServerAddr", Integer.parseInt(cooked[220]));
                        chrIntent.putExtra("RTTAbnServerAddr", Integer.parseInt(cooked[221]));
                        chrIntent.putExtra("VideoAvgSpeed", Integer.parseInt(cooked[222]));
                        chrIntent.putExtra("VideoFreezNum", Integer.parseInt(cooked[223]));
                        chrIntent.putExtra("VideoTime", Integer.parseInt(cooked[224]));
                        chrIntent.putExtra("AccVideoAvgSpeed", Integer.parseInt(cooked[225]));
                        chrIntent.putExtra("AccVideoFreezNum", Integer.parseInt(cooked[226]));
                        chrIntent.putExtra("AccVideoTime", Integer.parseInt(cooked[227]));
                        chrIntent.putExtra("tcp_handshake_delay", Integer.parseInt(cooked[228]));
                        chrIntent.putExtra("http_get_delay", Integer.parseInt(cooked[229]));
                        chrIntent.putExtra("http_send_get_num", Integer.parseInt(cooked[230]));
                        chrIntent.putExtra("webStatEnum", Integer.parseInt(cooked[245]));
                        this.mContext.sendBroadcast(chrIntent, CHR_BROADCAST_PERMISSION);
                    } catch (Exception e) {
                        Slog.e(TAG, "Web Stat Report Send Broadcast Fail.");
                    }
                } catch (Exception e2) {
                    Slog.e(TAG, "Web Stat Report Send Broadcast Fail.");
                }
            } else {
                z = false;
            }
            int WifiWebDelay2 = Integer.parseInt(cooked[258]);
            try {
                int winCnt = Integer.parseInt(cooked[495]);
                int exceptionCnt = Integer.parseInt(cooked[485]);
                if (WifiWebDelay2 > 0 || exceptionCnt > 0 || winCnt > 0) {
                    try {
                        Intent wifichrIntent = new Intent(INTENT_DS_WIFI_WEB_STAT_REPORT);
                        WifiWebDelay = WifiWebDelay2;
                        try {
                            wifichrIntent.putExtra("ReportType", Integer.parseInt(cooked[256]));
                            wifichrIntent.putExtra("RTT", Integer.parseInt(cooked[257]));
                            wifichrIntent.putExtra("WebDelay", Integer.parseInt(cooked[258]));
                            wifichrIntent.putExtra("SuccNum", Integer.parseInt(cooked[259]));
                            wifichrIntent.putExtra("FailNum", Integer.parseInt(cooked[260]));
                            wifichrIntent.putExtra("NoAckNum", Integer.parseInt(cooked[261]));
                            wifichrIntent.putExtra("TotalNum", Integer.parseInt(cooked[262]));
                            wifichrIntent.putExtra("TcpTotalNum", Integer.parseInt(cooked[263]));
                            wifichrIntent.putExtra("DelayL1", Integer.parseInt(cooked[264]));
                            wifichrIntent.putExtra("DelayL2", Integer.parseInt(cooked[265]));
                            wifichrIntent.putExtra("DelayL3", Integer.parseInt(cooked[266]));
                            wifichrIntent.putExtra("DelayL4", Integer.parseInt(cooked[267]));
                            wifichrIntent.putExtra("DelayL5", Integer.parseInt(cooked[268]));
                            wifichrIntent.putExtra("DelayL6", Integer.parseInt(cooked[269]));
                            wifichrIntent.putExtra("RTTL1", Integer.parseInt(cooked[270]));
                            wifichrIntent.putExtra("RTTL2", Integer.parseInt(cooked[271]));
                            wifichrIntent.putExtra("RTTL3", Integer.parseInt(cooked[272]));
                            wifichrIntent.putExtra("RTTL4", Integer.parseInt(cooked[273]));
                            wifichrIntent.putExtra("RTTL5", Integer.parseInt(cooked[274]));
                            wifichrIntent.putExtra("TcpSuccNum", Integer.parseInt(cooked[275]));
                            fillWifiAppInfo(cooked, wifichrIntent);
                            wifichrIntent.putExtra("HighestTcpRTT", Integer.parseInt(cooked[468]));
                            wifichrIntent.putExtra("LowestTcpRTT", Integer.parseInt(cooked[469]));
                            wifichrIntent.putExtra("LastTcpRTT", Integer.parseInt(cooked[PER_STRATEGY_SIZE]));
                            wifichrIntent.putExtra("HighestWebDelay", Integer.parseInt(cooked[471]));
                            wifichrIntent.putExtra("LowestWebDelay", Integer.parseInt(cooked[472]));
                            wifichrIntent.putExtra("LastWebDelay", Integer.parseInt(cooked[473]));
                            wifichrIntent.putExtra("ServerAddr", Integer.parseInt(cooked[474]));
                            wifichrIntent.putExtra("RTTAbnServerAddr", Integer.parseInt(cooked[475]));
                            wifichrIntent.putExtra("VideoAvgSpeed", Integer.parseInt(cooked[476]));
                            wifichrIntent.putExtra("VideoFreezNum", Integer.parseInt(cooked[477]));
                            wifichrIntent.putExtra("VideoTime", Integer.parseInt(cooked[478]));
                            wifichrIntent.putExtra("AccVideoAvgSpeed", Integer.parseInt(cooked[479]));
                            wifichrIntent.putExtra("AccVideoFreezNum", Integer.parseInt(cooked[480]));
                            wifichrIntent.putExtra("AccVideoTime", Integer.parseInt(cooked[481]));
                            wifichrIntent.putExtra("tcp_handshake_delay", Integer.parseInt(cooked[482]));
                            wifichrIntent.putExtra("http_get_delay", Integer.parseInt(cooked[483]));
                            wifichrIntent.putExtra("exception_cnt", Integer.parseInt(cooked[485]));
                            wifichrIntent.putExtra("data_direct", Integer.parseInt(cooked[486]));
                            wifichrIntent.putExtra("transport_delay", Integer.parseInt(cooked[487]));
                            wifichrIntent.putExtra("ip_delay", Integer.parseInt(cooked[488]));
                            wifichrIntent.putExtra("hmac_delay", Integer.parseInt(cooked[489]));
                            wifichrIntent.putExtra("driver_delay", Integer.parseInt(cooked[490]));
                            wifichrIntent.putExtra("android_uid", Integer.parseInt(cooked[491]));
                            wifichrIntent.putExtra("sock_uid", Integer.parseInt(cooked[492]));
                            wifichrIntent.putExtra("sock_dura", Integer.parseInt(cooked[493]));
                            wifichrIntent.putExtra("cur_win", Integer.parseInt(cooked[494]));
                            wifichrIntent.putExtra("win_cnt", Integer.parseInt(cooked[495]));
                            wifichrIntent.putExtra("free_space", Integer.parseInt(cooked[496]));
                            wifichrIntent.putExtra("mime_type", Integer.parseInt(cooked[497]));
                            wifichrIntent.putExtra("tcp_srtt", Integer.parseInt(cooked[498]));
                            this.mContext.sendBroadcast(wifichrIntent, CHR_BROADCAST_PERMISSION);
                        } catch (Exception e3) {
                            Slog.e(TAG, "Web Stat Report Send Broadcast Fail.");
                        }
                    } catch (Exception e4) {
                        Slog.e(TAG, "Web Stat Report Send Broadcast Fail.");
                    }
                } else {
                    WifiWebDelay = WifiWebDelay2;
                }
                if (Integer.parseInt(cooked[500]) > 0) {
                    Intent v6Intent = new Intent(INTENT_DS_WIFI_WEB_STAT_REPORT);
                    v6Intent.putExtra("ReportType", Integer.parseInt(cooked[256]));
                    v6Intent.putExtra("reason", Integer.parseInt(cooked[500]));
                    v6Intent.putExtra("router_time", Integer.parseInt(cooked[501]));
                    v6Intent.putExtra("managed", Integer.parseInt(cooked[502]));
                    v6Intent.putExtra("other", Integer.parseInt(cooked[503]));
                    v6Intent.putExtra("prefix_len", Integer.parseInt(cooked[504]));
                    v6Intent.putExtra("prefer_time", Integer.parseInt(cooked[505]));
                    v6Intent.putExtra("valid_time", Integer.parseInt(cooked[506]));
                    v6Intent.putExtra("autoconf", Integer.parseInt(cooked[507]));
                    v6Intent.putExtra("dns_option", Integer.parseInt(cooked[508]));
                    this.mContext.sendBroadcast(v6Intent, CHR_BROADCAST_PERMISSION);
                }
            } catch (Exception e5) {
                Slog.e(TAG, "Web Stat Report Send Broadcast Fail.");
            }
        } catch (Exception e6) {
            Slog.e(TAG, "Web Stat Report Send Broadcast Fail.");
        }
    }

    private boolean isOnlyMobileDataConnected() {
        ConnectivityManager cm = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        boolean isMobileConnected = false;
        for (Network network : cm.getAllNetworks()) {
            NetworkInfo networkInfo = cm.getNetworkInfo(network);
            if (networkInfo != null && networkInfo.isConnected()) {
                if (networkInfo.getType() == 1) {
                    return false;
                }
                if (networkInfo.getType() == 0) {
                    isMobileConnected = true;
                }
            }
        }
        return isMobileConnected;
    }

    public boolean handleApLinkedStaListChange(String raw, String[] cooked) {
        Slog.d(TAG, "handleApLinkedStaListChange is called");
        if (STA_JOIN_EVENT.equals(cooked[1]) || STA_LEAVE_EVENT.equals(cooked[1])) {
            String str = TAG;
            Slog.d(str, "Got sta list change event:" + cooked[1]);
            notifyApLinkedStaListChange(cooked[1], cooked[4]);
            return true;
        }
        throw new IllegalStateException(String.format("ApLinkedStaListChange: Invalid event from daemon (%s)", raw));
    }

    public List<String> getApLinkedStaList() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        List<String> mDhcpList = getApLinkedDhcpList();
        Slog.d(TAG, "getApLinkedStaList: softap assoclist");
        List<String> infoList = new ArrayList<>();
        for (int index = 0; index < this.mMacList.size(); index++) {
            String mac = getApLinkedStaInfo(this.mMacList.get(index), mDhcpList);
            String str = TAG;
            Slog.d(str, "getApLinkedStaList ApLinkedStaInfo = " + mac);
            infoList.add(mac);
        }
        String str2 = TAG;
        Slog.d(str2, "getApLinkedStaList, info size=" + infoList.size());
        return infoList;
    }

    public void setSoftapMacFilter(String macFilter) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            String cmdStr = String.format("softap setmacfilter " + macFilter, new Object[0]);
            String str = TAG;
            Slog.d(str, "setSoftapMacFilter:" + cmdStr);
            this.mConnector.doCommand("softap", new Object[]{"setmacfilter", macFilter});
        } catch (NativeDaemonConnectorException e) {
            throw new IllegalStateException("Cannot communicate with native daemon to set MAC Filter");
        }
    }

    public void setSoftapDisassociateSta(String mac) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            String cmdStr = String.format("softap disassociatesta " + mac, new Object[0]);
            String str = TAG;
            Slog.d(str, "setSoftapDisassociateSta:" + cmdStr);
            this.mConnector.doCommand("softap", new Object[]{"disassociatesta", mac});
        } catch (NativeDaemonConnectorException e) {
            throw new IllegalStateException("Cannot communicate with native daemon to disassociate a station");
        }
    }

    public void setAccessPointHw(String wlanIface, String softapIface) throws IllegalStateException {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CHANGE_NETWORK_STATE", "NetworkManagementService");
        this.mContext.enforceCallingOrSelfPermission("android.permission.CHANGE_WIFI_STATE", "NetworkManagementService");
    }

    private List<String> getApLinkedDhcpList() {
        try {
            Slog.d(TAG, "getApLinkedDhcpList: softap getdhcplease");
            String[] dhcpleaseList = this.mConnector.doListCommand("softap", (int) NetdResponseCode.SoftapDhcpListResult, new Object[]{"getdhcplease"});
            if (dhcpleaseList == null) {
                Slog.e(TAG, "getApLinkedDhcpList Error: doListCommand return NULL");
                return null;
            }
            List<String> mDhcpList = new ArrayList<>();
            for (String dhcplease : dhcpleaseList) {
                Slog.d(TAG, "getApLinkedDhcpList dhcpList = " + dhcplease);
                mDhcpList.add(dhcplease);
            }
            Slog.d(TAG, "getApLinkedDhcpList: mDhcpList size=" + mDhcpList.size());
            return mDhcpList;
        } catch (NativeDaemonConnectorException e) {
            Slog.e(TAG, "Cannot communicate with native daemon to get dhcp lease information");
            return null;
        }
    }

    private String getApLinkedStaInfo(String mac, List<String> mDhcpList) {
        String ApLinkedStaInfo = String.format("MAC=%s", mac);
        String mac2 = mac.toLowerCase();
        if (mDhcpList != null) {
            for (String dhcplease : mDhcpList) {
                if (dhcplease.contains(mac2)) {
                    String[] Tokens = dhcplease.split(" ");
                    if (4 <= Tokens.length) {
                        Slog.d(TAG, "getApLinkedStaInfo: dhcplease token");
                        ApLinkedStaInfo = String.format(ApLinkedStaInfo + " IP=%s DEVICE=%s", Tokens[2], Tokens[3]);
                    }
                }
            }
        }
        return ApLinkedStaInfo;
    }

    private void notifyApLinkedStaListChange(String event, String macStr) {
        int macHashCode = 0;
        if (macStr != null) {
            macHashCode = macStr.hashCode();
        }
        Message msg = Message.obtain();
        Bundle bundle = new Bundle();
        msg.what = macHashCode;
        bundle.putString(EVENT_KEY, event);
        bundle.putString(MAC_KEY, macStr);
        msg.setData(bundle);
        if (STA_JOIN_EVENT.equals(event)) {
            this.mApLinkedStaHandler.sendMessageDelayed(msg, 5000);
        } else if (STA_LEAVE_EVENT.equals(event)) {
            this.mApLinkedStaHandler.sendMessage(msg);
        }
        String str = TAG;
        Slog.d(str, "send " + event + " message, mLinkedStaCount=" + this.mLinkedStaCount);
    }

    public void setWifiTxPower(String reduceCmd) {
        String str = TAG;
        Slog.d(str, "setWifiTxPower " + reduceCmd);
        try {
            this.mConnector.execute("softap", new Object[]{reduceCmd});
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    private String getWiFiDnsStats(int netid) {
        StringBuffer buf = new StringBuffer();
        try {
            String[] stats = this.mConnector.doListCommand("resolver", 130, new Object[]{"getdnsstat", Integer.valueOf(netid)});
            if (stats != null) {
                for (int i = 0; i < stats.length; i++) {
                    buf.append(stats[i]);
                    if (i < stats.length - 1) {
                        buf.append(AwarenessInnerConstants.SEMI_COLON_KEY);
                    }
                }
            }
        } catch (NativeDaemonConnectorException e) {
            Slog.e(TAG, "Cannot communicate with native daemon to get wifi dns stats");
        }
        return buf.toString();
    }

    private String strToHexStr(String str) {
        if (str == null) {
            return null;
        }
        byte[] bytes = str.getBytes(Charset.forName("UTF-8"));
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            sb.append(HEX_STR.charAt((bytes[i] & 240) >> 4));
            sb.append(HEX_STR.charAt((bytes[i] & HwUibcReceiver.CurrentPacket.INPUT_MASK) >> 0));
        }
        return sb.toString();
    }

    private String hexStrToStr(String hexStr) {
        if (hexStr == null) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream(hexStr.length() / 2);
        for (int i = 0; i < hexStr.length(); i += 2) {
            baos.write((HEX_STR.indexOf(hexStr.charAt(i)) << 4) | HEX_STR.indexOf(hexStr.charAt(i + 1)));
        }
        return new String(baos.toByteArray(), Charset.forName("UTF-8"));
    }

    private ArrayList<String> convertPkgNameToUid(String[] pkgName) {
        if (pkgName != null) {
            Slog.d(TAG, "convertPkgNameToUid, pkgName=" + Arrays.asList(pkgName));
        }
        ArrayList<String> uidList = new ArrayList<>();
        if (pkgName != null && pkgName.length > 0) {
            int userCount = UserManager.get(this.mContext).getUserCount();
            List<UserInfo> users = UserManager.get(this.mContext).getUsers();
            PackageManager pm = this.mContext.getPackageManager();
            StringBuilder appUidBuilder = new StringBuilder();
            int uidCount = 0;
            for (String pkg : pkgName) {
                for (int n = 0; n < userCount; n++) {
                    try {
                        int uid = pm.getPackageUidAsUser(pkg, users.get(n).id);
                        Slog.d(TAG, "convertPkgNameToUid, pkg=" + pkg + ", uid=" + uid + ", under user.id=" + users.get(n).id);
                        uidCount++;
                        if (uidCount % 50 == 0) {
                            appUidBuilder.append(uid);
                            appUidBuilder.append(AwarenessInnerConstants.SEMI_COLON_KEY);
                            uidList.add(appUidBuilder.toString());
                            appUidBuilder = new StringBuilder();
                        } else {
                            appUidBuilder.append(uid);
                            appUidBuilder.append(AwarenessInnerConstants.SEMI_COLON_KEY);
                        }
                    } catch (Exception e) {
                        Slog.e(TAG, "convertPkgNameToUid, skip unknown packages!");
                    }
                }
            }
            if (!TextUtils.isEmpty(appUidBuilder.toString())) {
                uidList.add(appUidBuilder.toString());
            }
        }
        return uidList;
    }

    private String[] getHwExtendCmdsList(String cmd, Object... args) {
        ArrayList<String> hwExtendCmdsList = Lists.newArrayList();
        hwExtendCmdsList.add(cmd);
        for (Object arg : args) {
            hwExtendCmdsList.add(String.valueOf(arg));
        }
        return (String[]) hwExtendCmdsList.toArray(new String[hwExtendCmdsList.size()]);
    }

    private void setAdFilterRules(String adStrategy, boolean needReset) {
        Exception e;
        String adStrategy2 = adStrategy;
        Slog.d(TAG, "setAdFilterRules, adStrategy=" + adStrategy2 + ", needReset=" + needReset);
        String operation = needReset ? "reset" : "not_reset";
        int count = 0;
        int strategyLen = 0;
        if (adStrategy2 != null) {
            strategyLen = adStrategy.length();
            count = strategyLen / PER_STRATEGY_SIZE;
            if (strategyLen % PER_STRATEGY_SIZE != 0) {
                count++;
            }
            Slog.d(TAG, "setAdFilterRules, adStrategy len=" + strategyLen + ", divided count=" + count);
        }
        int cmdId = this.mCmdId.incrementAndGet();
        try {
            Slog.d(TAG, "setAdFilterRules, count=" + count + ", cmdId=" + cmdId);
            int i = 0;
            this.mOemNetdService.hwFilterCommand(getHwExtendCmdsList("hwfilter", "set_ad_strategy_rule", operation, Integer.toString(cmdId), Integer.toString(count)));
            if (strategyLen == 0) {
                Slog.d(TAG, "setAdFilterRules, adStrategy is null!");
                return;
            }
            int i2 = 1;
            while (adStrategy2.length() > 0) {
                if (adStrategy2.length() > PER_STRATEGY_SIZE) {
                    try {
                        String adStrategyTmp = adStrategy2.substring(i, PER_STRATEGY_SIZE);
                        Slog.d(TAG, "setAdFilterRules, adStrategy len=" + adStrategyTmp.length() + ", seq=" + i2 + ", cmdId=" + cmdId);
                        this.mOemNetdService.hwFilterCommand(getHwExtendCmdsList("hwfilter", "set_ad_strategy_buf", Integer.toString(cmdId), Integer.toString(i2), adStrategyTmp));
                        adStrategy2 = adStrategy2.substring(PER_STRATEGY_SIZE);
                        i2++;
                        i = 0;
                    } catch (RemoteException | ServiceSpecificException e2) {
                        e = e2;
                        throw new IllegalStateException(e);
                    }
                } else {
                    Slog.d(TAG, "setAdFilterRules, adStrategy len=" + adStrategy2.length() + ", seq=" + i2 + ", cmdId=" + cmdId);
                    this.mOemNetdService.hwFilterCommand(getHwExtendCmdsList("hwfilter", "set_ad_strategy_buf", Integer.toString(cmdId), Integer.toString(i2), adStrategy2));
                    return;
                }
            }
        } catch (RemoteException | ServiceSpecificException e3) {
            e = e3;
            throw new IllegalStateException(e);
        }
    }

    private void setApkDlFilterRules(ArrayList<String> appUidList, boolean needReset) {
        Slog.d(TAG, "setApkDlFilterRules, appUidList=" + appUidList + ", needReset=" + needReset);
        String operation = needReset ? "reset" : "not_reset";
        if (appUidList != null) {
            try {
                if (appUidList.size() > 0) {
                    for (int i = 0; i < appUidList.size(); i++) {
                        if (i == 0) {
                            this.mOemNetdService.hwFilterCommand(getHwExtendCmdsList("hwfilter", "set_apkdl_strategy_rule", appUidList.get(i), operation));
                        } else {
                            this.mOemNetdService.hwFilterCommand(getHwExtendCmdsList("hwfilter", "set_apkdl_strategy_rule", appUidList.get(i), "not_reset"));
                        }
                    }
                    return;
                }
            } catch (RemoteException | ServiceSpecificException e) {
                throw new IllegalStateException(e);
            }
        }
        this.mOemNetdService.hwFilterCommand(getHwExtendCmdsList("hwfilter", "set_apkdl_strategy_rule", null, operation));
    }

    private void clearAdOrApkDlFilterRules(ArrayList<String> appUidList, boolean needReset, int strategy) {
        Slog.d(TAG, "clearApkDlFilterRules, appUidList=" + appUidList + ", needReset=" + needReset + ", strategy=" + strategy);
        String operation = needReset ? "reset" : "not_reset";
        if (strategy == 0) {
            if (appUidList == null || appUidList.size() <= 0) {
                this.mOemNetdService.hwFilterCommand(getHwExtendCmdsList("hwfilter", "clear_ad_strategy_rule", null, operation));
                return;
            }
            for (int i = 0; i < appUidList.size(); i++) {
                if (i == 0) {
                    this.mOemNetdService.hwFilterCommand(getHwExtendCmdsList("hwfilter", "clear_ad_strategy_rule", appUidList.get(i), operation));
                } else {
                    this.mOemNetdService.hwFilterCommand(getHwExtendCmdsList("hwfilter", "clear_ad_strategy_rule", appUidList.get(i), "not_reset"));
                }
            }
        } else if (1 == strategy) {
            if (appUidList != null) {
                try {
                    if (appUidList.size() > 0) {
                        for (int i2 = 0; i2 < appUidList.size(); i2++) {
                            if (i2 == 0) {
                                this.mOemNetdService.hwFilterCommand(getHwExtendCmdsList("hwfilter", "clear_apkdl_strategy_rule", appUidList.get(i2), operation));
                            } else {
                                this.mOemNetdService.hwFilterCommand(getHwExtendCmdsList("hwfilter", "clear_apkdl_strategy_rule", appUidList.get(i2), "not_reset"));
                            }
                        }
                        return;
                    }
                } catch (RemoteException | ServiceSpecificException e) {
                    throw new IllegalStateException(e);
                }
            }
            this.mOemNetdService.hwFilterCommand(getHwExtendCmdsList("hwfilter", "clear_apkdl_strategy_rule", null, operation));
        } else if (2 == strategy) {
            Slog.d(TAG, "clearApkDlFilterRules strategy is APK_CONTROL_STRATEGY");
            if (appUidList == null || appUidList.size() <= 0) {
                Slog.d(TAG, "clearApkDlFilterRules else netd clear_delta_install_rule");
                this.mOemNetdService.hwFilterCommand(getHwExtendCmdsList("hwfilter", "clear_delta_install_rule", null, operation));
                return;
            }
            int appUidList_size = appUidList.size();
            for (int i3 = 0; i3 < appUidList_size; i3++) {
                if (i3 == 0) {
                    Slog.d(TAG, "clearApkDlFilterRules 0==i netd clear_delta_install_rule");
                    this.mOemNetdService.hwFilterCommand(getHwExtendCmdsList("hwfilter", "clear_delta_install_rule", appUidList.get(i3), operation));
                } else {
                    Slog.d(TAG, "clearApkDlFilterRules 0!=i netd clear_delta_install_rule");
                    this.mOemNetdService.hwFilterCommand(getHwExtendCmdsList("hwfilter", "clear_delta_install_rule", appUidList.get(i3), "not_reset"));
                }
            }
        }
    }

    private void printAdOrApkDlFilterRules(int strategy) {
        String str = TAG;
        Slog.d(str, "printAdOrApkDlFilterRules, strategy=" + strategy);
        if (strategy == 0) {
            try {
                this.mOemNetdService.hwFilterCommand(getHwExtendCmdsList("hwfilter", "output_ad_strategy_rule"));
            } catch (RemoteException | ServiceSpecificException e) {
                throw new IllegalStateException(e);
            }
        } else if (1 == strategy) {
            this.mOemNetdService.hwFilterCommand(getHwExtendCmdsList("hwfilter", "output_apkdl_strategy_rule"));
        } else if (2 == strategy) {
            this.mOemNetdService.hwFilterCommand(getHwExtendCmdsList("hwfilter", "output_delta_install_rule"));
        }
    }

    private void setApkDlUrlUserResult(String downloadId, boolean result) {
        String str = TAG;
        Slog.d(str, "setApkDlUrlUserResult, downloadId=" + downloadId + ", result=" + result);
        try {
            this.mOemNetdService.hwFilterCommand(getHwExtendCmdsList("hwfilter", "apkdl_callback", downloadId, result ? "allow" : "reject"));
        } catch (RemoteException | ServiceSpecificException e) {
            throw new IllegalStateException(e);
        }
    }

    /* JADX INFO: Multiple debug info for r4v7 'uid'  int: [D('uid' int), D('userCount' int)] */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x0138  */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x0140  */
    private String getStrategyStr(int code, int size, Parcel data) {
        PackageManager pm;
        int userCount;
        PackageManager pm2;
        int userCount2;
        int uid;
        String str;
        StringBuilder sb;
        int i = size;
        if (i <= 0) {
            return null;
        }
        int userCount3 = UserManager.get(this.mContext).getUserCount();
        List<UserInfo> users = UserManager.get(this.mContext).getUsers();
        PackageManager pm3 = this.mContext.getPackageManager();
        StringBuilder StrategyBuilder = new StringBuilder();
        int i2 = 0;
        while (i2 < i) {
            String key = data.readString();
            ArrayList<String> value = data.createStringArrayList();
            int i3 = i2 + 1;
            boolean isEmpty = TextUtils.isEmpty(key);
            int i4 = CODE_SET_AD_STRATEGY_RULE;
            if (isEmpty || value == null) {
                userCount = userCount3;
                pm = pm3;
            } else if (value.size() == 0) {
                userCount = userCount3;
                pm = pm3;
            } else {
                StringBuilder tmpUrlBuilder = new StringBuilder();
                int n = 0;
                String tmpUrlStr = null;
                while (n < userCount3) {
                    try {
                        int uid2 = pm3.getPackageUidAsUser(key, users.get(n).id);
                        if (i4 == code) {
                            try {
                                str = TAG;
                                sb = new StringBuilder();
                                userCount2 = userCount3;
                                try {
                                    sb.append("CODE_SET_AD_STRATEGY_RULE, adStrategy pkgName=");
                                    sb.append(key);
                                    sb.append(", uid=");
                                    uid = uid2;
                                } catch (Exception e) {
                                    pm2 = pm3;
                                    if (CODE_SET_AD_STRATEGY_RULE == code) {
                                        Slog.e(TAG, "CODE_SET_AD_STRATEGY_RULE, skip unknown packages!");
                                    } else if (CODE_SET_APK_CONTROL_STRATEGY == code) {
                                        Slog.e(TAG, "CODE_SET_APK_CONTROL_STRATEGY, skip unknown packages!");
                                    }
                                    n++;
                                    userCount3 = userCount2;
                                    pm3 = pm2;
                                    i4 = CODE_SET_AD_STRATEGY_RULE;
                                }
                            } catch (Exception e2) {
                                userCount2 = userCount3;
                                pm2 = pm3;
                                if (CODE_SET_AD_STRATEGY_RULE == code) {
                                }
                                n++;
                                userCount3 = userCount2;
                                pm3 = pm2;
                                i4 = CODE_SET_AD_STRATEGY_RULE;
                            }
                            try {
                                sb.append(uid);
                                sb.append(", under user.id=");
                                sb.append(users.get(n).id);
                                Slog.d(str, sb.toString());
                                pm2 = pm3;
                            } catch (Exception e3) {
                                pm2 = pm3;
                                if (CODE_SET_AD_STRATEGY_RULE == code) {
                                }
                                n++;
                                userCount3 = userCount2;
                                pm3 = pm2;
                                i4 = CODE_SET_AD_STRATEGY_RULE;
                            }
                        } else {
                            userCount2 = userCount3;
                            uid = uid2;
                            if (CODE_SET_APK_CONTROL_STRATEGY == code) {
                                try {
                                    String str2 = TAG;
                                    StringBuilder sb2 = new StringBuilder();
                                    pm2 = pm3;
                                    try {
                                        sb2.append("CODE_SET_APK_CONTROL_STRATEGY, apkStrategy pkgName=");
                                        sb2.append(key);
                                        sb2.append(", uid=");
                                        sb2.append(uid);
                                        sb2.append(", under user.id=");
                                        sb2.append(users.get(n).id);
                                        Slog.d(str2, sb2.toString());
                                    } catch (Exception e4) {
                                    }
                                } catch (Exception e5) {
                                    pm2 = pm3;
                                    if (CODE_SET_AD_STRATEGY_RULE == code) {
                                    }
                                    n++;
                                    userCount3 = userCount2;
                                    pm3 = pm2;
                                    i4 = CODE_SET_AD_STRATEGY_RULE;
                                }
                            } else {
                                pm2 = pm3;
                            }
                        }
                        StrategyBuilder.append(uid);
                        StrategyBuilder.append(AwarenessInnerConstants.COLON_KEY);
                        if (tmpUrlStr == null) {
                            int count = 0;
                            int value_size = value.size();
                            for (int m = 0; m < value_size; m++) {
                                tmpUrlBuilder.append(strToHexStr(value.get(m)));
                                count++;
                                if (count < value.size()) {
                                    tmpUrlBuilder.append(",");
                                } else {
                                    tmpUrlBuilder.append(AwarenessInnerConstants.SEMI_COLON_KEY);
                                }
                            }
                            tmpUrlStr = tmpUrlBuilder.toString();
                        }
                        StrategyBuilder.append(tmpUrlStr);
                    } catch (Exception e6) {
                        userCount2 = userCount3;
                        pm2 = pm3;
                        if (CODE_SET_AD_STRATEGY_RULE == code) {
                        }
                        n++;
                        userCount3 = userCount2;
                        pm3 = pm2;
                        i4 = CODE_SET_AD_STRATEGY_RULE;
                    }
                    n++;
                    userCount3 = userCount2;
                    pm3 = pm2;
                    i4 = CODE_SET_AD_STRATEGY_RULE;
                }
                userCount = userCount3;
                pm = pm3;
                i = size;
                i2 = i3;
                userCount3 = userCount;
                pm3 = pm;
            }
            if (CODE_SET_AD_STRATEGY_RULE == code) {
                Slog.e(TAG, "CODE_SET_AD_STRATEGY_RULE, skip empty key or value!");
            } else if (CODE_SET_APK_CONTROL_STRATEGY == code) {
                Slog.e(TAG, "CODE_SET_APK_CONTROL_STRATEGY, skip empty key or value!");
            }
            i = size;
            i2 = i3;
            userCount3 = userCount;
            pm3 = pm;
        }
        return StrategyBuilder.toString();
    }

    private void setApkControlFilterRules(String apkStrategy, boolean needReset) {
        Exception e;
        String apkStrategy2 = apkStrategy;
        Slog.d(TAG, "setApkControlFilterRules, apkStrategy=" + apkStrategy2 + ", needReset=" + needReset);
        String operation = needReset ? "reset" : "not_reset";
        int count = 0;
        int strategyLen = 0;
        if (apkStrategy2 != null) {
            strategyLen = apkStrategy.length();
            count = strategyLen / PER_STRATEGY_SIZE;
            if (strategyLen % PER_STRATEGY_SIZE != 0) {
                count++;
            }
            Slog.d(TAG, "setApkControlFilterRules, apkStrategy len=" + strategyLen + ", divided count=" + count);
        }
        int cmdId = this.mCmdId.incrementAndGet();
        try {
            Slog.d(TAG, "setApkControlFilterRules, count=" + count + ", cmdId=" + cmdId);
            int i = 0;
            this.mOemNetdService.hwFilterCommand(getHwExtendCmdsList("hwfilter", "set_delta_install_rule", operation, Integer.toString(cmdId), Integer.toString(count)));
            if (strategyLen == 0) {
                Slog.d(TAG, "setApkControlFilterRules, apkStrategy is null!");
                return;
            }
            int i2 = 1;
            while (apkStrategy2.length() > 0) {
                if (apkStrategy2.length() > PER_STRATEGY_SIZE) {
                    try {
                        String apkStrategyTmp = apkStrategy2.substring(i, PER_STRATEGY_SIZE);
                        Slog.d(TAG, "setApkControlFilterRules, apkStrategy len=" + apkStrategyTmp.length() + ", seq=" + i2 + ", cmdId=" + cmdId);
                        this.mOemNetdService.hwFilterCommand(getHwExtendCmdsList("hwfilter", "set_delta_install_buf", Integer.toString(cmdId), Integer.toString(i2), apkStrategyTmp));
                        apkStrategy2 = apkStrategy2.substring(PER_STRATEGY_SIZE);
                        i2++;
                        i = 0;
                    } catch (RemoteException | ServiceSpecificException e2) {
                        e = e2;
                        throw new IllegalStateException(e);
                    }
                } else {
                    Slog.d(TAG, "setApkFilterRules, apkStrategy len=" + apkStrategy2.length() + ", seq=" + i2 + ", cmdId=" + cmdId);
                    this.mOemNetdService.hwFilterCommand(getHwExtendCmdsList("hwfilter", "set_delta_install_buf", Integer.toString(cmdId), Integer.toString(i2), apkStrategy2));
                    return;
                }
            }
        } catch (RemoteException | ServiceSpecificException e3) {
            e = e3;
            throw new IllegalStateException(e);
        }
    }

    public void sendApkDownloadUrlBroadcast(String[] cooked, String raw) {
        int max;
        String str = TAG;
        Slog.d(str, "receive report_apkdl_event, raw=" + raw);
        if (cooked.length >= 4) {
            long startTime = SystemClock.elapsedRealtime();
            String downloadId = cooked[1];
            String uid = cooked[2];
            if (!this.startTimeMap.containsKey(downloadId)) {
                this.startTimeMap.put(downloadId, Long.valueOf(startTime));
            }
            Matcher m = this.p.matcher(cooked[3]);
            if (!m.matches() || m.groupCount() < 3) {
                String url = hexStrToStr(cooked[3]);
                String str2 = TAG;
                Slog.d(str2, "receive report_apkdl_event for single segment, startTime=" + startTime + ", downloadId=" + downloadId + ", uid=" + uid + ", url=" + url);
                sendApkDownloadUrlBroadcastInner(startTime, downloadId, uid, url);
                return;
            }
            int max2 = 0;
            int idx = 0;
            try {
                max2 = Integer.parseInt(m.group(1));
                idx = Integer.parseInt(m.group(2));
                max = max2;
            } catch (NumberFormatException e) {
                Slog.e(TAG, "sendApkDownloadUrlBroadcast parse failed");
                max = max2;
            }
            String subUrl = m.group(3);
            if (idx == 1) {
                this.urlBuffer = new StringBuffer();
                this.urlBuffer.append(subUrl);
            } else {
                this.urlBuffer.append(subUrl);
            }
            if (max == idx) {
                String url2 = hexStrToStr(this.urlBuffer.toString());
                String str3 = TAG;
                Slog.d(str3, "receive report_apkdl_event for the last segment, startTime=" + startTime + ", downloadId=" + downloadId + ", uid=" + uid + ", url=" + url2);
                sendApkDownloadUrlBroadcastInner(this.startTimeMap.get(downloadId).longValue(), downloadId, uid, url2);
                return;
            }
            return;
        }
        String errorMessage = String.format("Invalid event from daemon (%s)", raw);
        Slog.d(TAG, "receive report_apkdl_event, return error");
        throw new IllegalStateException(errorMessage);
    }

    private void sendApkDownloadUrlBroadcastInner(long startTime, String downloadId, String uid, String url) {
        Intent intent = new Intent(INTENT_APKDL_URL_DETECTED);
        intent.putExtra("startTime", startTime);
        intent.putExtra("downloadId", downloadId);
        intent.putExtra("uid", uid);
        intent.putExtra("url", url);
        intent.addFlags(16777216);
        this.mContext.sendBroadcast(intent, AD_APKDL_STRATEGY_PERMISSION);
    }

    public void systemReady() {
        HwNetworkManagementService.super.systemReady();
        initNetworkAccessList();
    }

    private void initNetworkAccessList() {
        final int networkPolicyFlag = getNetworkPolicyFlag();
        if (networkPolicyFlag != -1) {
            new Thread(new Runnable() {
                /* class com.android.server.HwNetworkManagementService.AnonymousClass4 */

                @Override // java.lang.Runnable
                public void run() {
                    new ArrayList();
                    int i = networkPolicyFlag;
                    if (i == 1 || i == 0) {
                        List<String> ipPolicyList = HwDeviceManager.getList(62);
                        if (ipPolicyList == null || ipPolicyList.isEmpty()) {
                            List<String> defaultIpWhiteList = new ArrayList<>();
                            defaultIpWhiteList.add(HwNetworkManagementService.LOCAL_HOST);
                            HwNetworkManagementService.this.setNetworkAccessList(defaultIpWhiteList, 0);
                        } else {
                            HwNetworkManagementService.this.setNetworkAccessList(ipPolicyList, 0);
                        }
                        if (networkPolicyFlag == 1) {
                            SystemProperties.set(HwNetworkManagementService.KEY_NETWORK_POLICY_PROPERTIES, AppActConstant.VALUE_TRUE);
                        } else {
                            SystemProperties.set(HwNetworkManagementService.KEY_NETWORK_POLICY_PROPERTIES, AppActConstant.VALUE_FALSE);
                        }
                    } else if (i == 2 || i == 3) {
                        List<String> ipPolicyList2 = HwDeviceManager.getList(63);
                        if (ipPolicyList2 != null && !ipPolicyList2.isEmpty()) {
                            HwNetworkManagementService.this.setNetworkAccessList(ipPolicyList2, 1);
                        }
                        if (networkPolicyFlag == 3) {
                            SystemProperties.set(HwNetworkManagementService.KEY_NETWORK_POLICY_PROPERTIES, AppActConstant.VALUE_TRUE);
                        } else {
                            SystemProperties.set(HwNetworkManagementService.KEY_NETWORK_POLICY_PROPERTIES, AppActConstant.VALUE_FALSE);
                        }
                    } else {
                        Slog.d(HwNetworkManagementService.TAG, "hasNetworkWhiteOrBlackList error");
                    }
                }
            }, "initIptables").start();
        }
    }

    private void setOrAddNetworkAccessList(Parcel data, Parcel reply) {
        Slog.d(TAG, "code == CODE_SET_NETWORK_ACCESS_LIST");
        data.enforceInterface(DESCRIPTOR_NETWORKMANAGEMENT_SERVICE);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_NETWORK_MANAGER", TAG);
        List<String> networkAccessList = new ArrayList<>();
        data.readStringList(networkAccessList);
        int whiteOrBlack = data.readInt();
        int setOrAdd = data.readInt();
        if (setOrAdd == 1) {
            setNetworkAccessList(networkAccessList, whiteOrBlack);
        } else if (setOrAdd == 0) {
            addNetworkAccessList(networkAccessList, whiteOrBlack);
        } else {
            Slog.d(TAG, "setOrAdd error");
        }
        reply.writeNoException();
    }

    private void getChrUidList(Parcel data, Parcel reply) {
        data.enforceInterface(DESCRIPTOR_NETWORKMANAGEMENT_SERVICE);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_NETWORK_MANAGER", TAG);
        reply.writeNoException();
        for (int i = 0; i < 10; i++) {
            reply.writeInt(this.mChrAppUidArray[i]);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setNetworkAccessList(List<String> addrList, int whiteOrBlack) {
        if (addrList != null) {
            try {
                if (!addrList.isEmpty()) {
                    String str = TAG;
                    Slog.d(str, "set ipwhitelist: " + addrList.get(0));
                    this.mOemNetdService.HwNetFilterSetIpRules(addrList.get(0), whiteOrBlack);
                    Slog.d(TAG, "set ipwhitelist completed");
                    addrList.remove(0);
                    this.mOemNetdService.HwNetFilterAddIpRules((String[]) addrList.toArray(new String[0]), whiteOrBlack);
                    Slog.d(TAG, "add ipwhitelist completed");
                    return;
                }
            } catch (RemoteException | ServiceSpecificException e) {
                Slog.e(TAG, "runNetFilterCmd:", e);
                return;
            }
        }
        this.mOemNetdService.HwNetFilterClearIpRules(whiteOrBlack);
        Slog.d(TAG, "clear ipwhitelist completed");
    }

    private void addNetworkAccessList(List<String> addrList, int whiteOrBlack) {
        if (addrList != null) {
            try {
                if (!addrList.isEmpty()) {
                    this.mOemNetdService.HwNetFilterAddIpRules((String[]) addrList.toArray(new String[0]), whiteOrBlack);
                    Slog.d(TAG, "add ipwhitelist completed");
                }
            } catch (RemoteException | ServiceSpecificException e) {
                Slog.e(TAG, "runNetFilterCmd:", e);
            }
        }
    }

    private UidRangeParcel makeUidRangeParcel(int start, int stop) {
        UidRangeParcel range = new UidRangeParcel();
        range.start = start;
        range.stop = stop;
        return range;
    }

    public boolean closeSocketsForUid(int uid) {
        try {
            this.mNetdService.socketDestroy(new UidRangeParcel[]{makeUidRangeParcel(uid, uid)}, new int[0]);
            Slog.d(TAG, "SocketDestroy finished");
            return true;
        } catch (RemoteException | ServiceSpecificException e) {
            String str = TAG;
            Slog.e(str, "Error closing sockets for uid " + uid + ": " + e);
            return false;
        }
    }

    private void setChrReportUid(int index, int uid) {
        if (this.mOemNetdService == null) {
            Slog.e(TAG, "setChrReportUid mOemNetdService is null");
            return;
        }
        if (index >= 0 && index < 10 && uid > 0) {
            try {
                this.mChrAppUidArray[index] = uid;
            } catch (RemoteException | ServiceSpecificException e) {
                Slog.i(TAG, "chr appuid set succeeded", e);
                return;
            }
        }
        this.mOemNetdService.setChrAppUid(index, uid);
        Slog.i(TAG, "chr appuid set succeeded");
    }

    private boolean pgNetFilterSetRule(int cmd, int[] keys, int[] values) {
        IOemNetd iOemNetd = this.mOemNetdService;
        if (iOemNetd == null) {
            Slog.e(TAG, "pgNetFilterSetRule mOemNetdService is null");
            return false;
        }
        try {
            return iOemNetd.pgNetFilterSetRule(cmd, keys, values);
        } catch (RemoteException | ServiceSpecificException e) {
            throw new IllegalStateException(e);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setNetBoosterVodEnabled(boolean enable) {
        if (this.mBoosterVideoAccEnabled) {
            IOemNetd iOemNetd = this.mOemNetdService;
            if (iOemNetd == null) {
                Slog.e(TAG, "setNetBoosterVodEnabled mOemNetdService is null");
                return;
            }
            try {
                iOemNetd.setNetBoosterVodEnabled(enable);
                Slog.i(TAG, "setNetBoosterVodEnabled succeeded");
            } catch (RemoteException | ServiceSpecificException e) {
                Slog.e(TAG, "setNetBoosterVodEnabled failed", e);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setNetBoosterKsiEnabled(boolean enable) {
        if (this.mBoosterNetAiChangeEnabled) {
            IOemNetd iOemNetd = this.mOemNetdService;
            if (iOemNetd == null) {
                Slog.e(TAG, "setNetBoosterKsiEnabled mOemNetdService is null");
                return;
            }
            try {
                iOemNetd.setNetBoosterKsiEnabled(enable);
                Slog.i(TAG, "setNetBoosterKsiEnabled succeeded");
            } catch (RemoteException | ServiceSpecificException e) {
                Slog.e(TAG, "setNetBoosterKsiEnabled failed", e);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setNetBoosterAppUid(Bundle data) {
        int appUid = data.getInt("appUid");
        int period = data.getInt("reportPeriod");
        IOemNetd iOemNetd = this.mOemNetdService;
        if (iOemNetd == null) {
            Slog.e(TAG, "setNetBoosterAppUid mOemNetdService is null");
            return;
        }
        try {
            iOemNetd.setNetBoosterAppUid(appUid, period);
            Slog.i(TAG, "setNetBoosterAppUid succeeded");
        } catch (RemoteException | ServiceSpecificException e) {
            Slog.e(TAG, "setNetBoosterAppUid failed", e);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setNetBoosterRsrpRsrq(Bundle data) {
        int rsrp = data.getInt("rsrp");
        int rsrq = data.getInt("rsrq");
        IOemNetd iOemNetd = this.mOemNetdService;
        if (iOemNetd == null) {
            Slog.e(TAG, "setNetBoosterRsrpRsrq mOemNetdService is null");
            return;
        }
        try {
            iOemNetd.setNetBoosterRsrpRsrq(rsrp, rsrq);
            Slog.i(TAG, "setNetBoosterRsrpRsrq succeeded");
        } catch (RemoteException | ServiceSpecificException e) {
            Slog.e(TAG, "setNetBoosterRsrpRsrq failed", e);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendSettingParamsToKernel(Bundle data) {
        int msgId = data.getInt("msgId");
        int param1 = data.getInt("param1");
        int param2 = data.getInt("param2");
        int param3 = data.getInt("param3");
        IOemNetd iOemNetd = this.mOemNetdService;
        if (iOemNetd == null) {
            Slog.e(TAG, "sendSettingParamsToKernel mOemNetdService is null");
            return;
        }
        try {
            iOemNetd.setNetBoosterSettingParams(msgId, param1, param2, param3);
            String str = TAG;
            Slog.i(str, "sendSettingParamsToKernel, msg=" + msgId);
        } catch (RemoteException | ServiceSpecificException e) {
            Slog.e(TAG, "sendSettingParamsToKernel failed", e);
        }
    }

    public void reportVodParams(int videoSegState, int videoProtocol, int videoRemainingPlayTime, int videoStatus, int aveCodeRate, int segSize, int flowInfoRemote, int flowInfoLocal, int segDuration, int segIndex) {
        if (this.mBoosterVideoAccEnabled) {
            String str = TAG;
            Slog.d(str, "reportVodParams:videoSegState=" + videoSegState + ",videoProtocol=" + videoProtocol + ",videoRemainingPlayTime=" + videoRemainingPlayTime + ",videoStatus=" + videoStatus + ",aveCodeRate=" + aveCodeRate + ",segSize=" + segSize + ",segDuration=" + segDuration + ",segIndex=" + segIndex);
            IHwCommBoosterServiceManager bm = HwFrameworkFactory.getHwCommBoosterServiceManager();
            if (bm != null) {
                Bundle data = new Bundle();
                data.putInt("videoSegState", videoSegState);
                data.putInt("videoProtocol", videoProtocol);
                data.putInt("videoRemainingPlayTime", videoRemainingPlayTime);
                data.putInt("videoStatus", videoStatus);
                data.putInt("aveCodeRate", aveCodeRate);
                data.putInt("segSize", segSize);
                data.putInt("flowInfoRemote", flowInfoRemote);
                data.putInt("flowInfoLocal", flowInfoLocal);
                data.putInt("segDuration", segDuration);
                data.putInt("segIndex", segIndex);
                int ret = bm.reportBoosterPara("com.android.server", 1, data);
                if (ret != 0) {
                    String str2 = TAG;
                    Slog.e(str2, "reportVodParams:reportBoosterPara failed, ret=" + ret);
                    return;
                }
                return;
            }
            Slog.e(TAG, "reportVodParams:null HwCommBoosterServiceManager");
        }
    }

    public void reportKsiParams(int slowType, int avgAmp, int duration, int timeStart) {
        if (this.mBoosterNetAiChangeEnabled) {
            String str = TAG;
            Slog.d(str, "reportKsiParams:slowType=" + slowType + ",avgAmp=" + avgAmp + ",duration=" + duration + ",timeStart=" + timeStart);
            IHwCommBoosterServiceManager bm = HwFrameworkFactory.getHwCommBoosterServiceManager();
            if (bm != null) {
                Bundle data = new Bundle();
                data.putInt("slowType", slowType);
                data.putInt("avgAmp", avgAmp);
                data.putInt("duration", duration);
                data.putInt("timeStart", timeStart);
                int ret = bm.reportBoosterPara("com.android.server", 2, data);
                if (ret != 0) {
                    String str2 = TAG;
                    Slog.e(str2, "reportKsiParams:reportBoosterPara failed, ret=" + ret);
                    return;
                }
                return;
            }
            Slog.e(TAG, "reportKsiParams:null HwCommBoosterServiceManager");
        }
    }

    private int getNetdPid() {
        try {
            if (this.mOemNetdService != null) {
                return this.mOemNetdService.getNetdPid();
            }
            return -1;
        } catch (RemoteException | ServiceSpecificException e) {
            Slog.e(TAG, "getNetdPid: Exception");
            return -1;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setNetBoosterPreDnsAppUid(Bundle b) {
        if (this.mBoosterPreDnsEnabled) {
            if (this.mOemNetdService == null) {
                Slog.e(TAG, "setNetBoosterPreDnsAppUid mOemNetdService is null");
                return;
            }
            String preDnsAppUid = b.getString(TOP_APP_UID_INFO);
            String str = TAG;
            Slog.d(str, "setNetBoosterPreDnsAppUid: preDnsAppUid: " + preDnsAppUid);
            if (preDnsAppUid != null) {
                String[] apps = preDnsAppUid.split(" ");
                String[] appUid = new String[12];
                int argc = 0;
                for (int i = 0; i < apps.length; i++) {
                    int argc2 = argc + 1;
                    appUid[argc] = apps[i];
                    if (i == apps.length - 1 || argc2 == 12) {
                        String[] strArr = new String[argc2];
                        try {
                            this.mOemNetdService.setNetBoosterPreDnsAppUid((String[]) Arrays.copyOfRange(appUid, 0, argc2));
                            Slog.i(TAG, "setNetBoosterPreDnsAppUid: top_app_uid cmd execute succeeded");
                        } catch (RemoteException | ServiceSpecificException e) {
                            Slog.e(TAG, "setNetBoosterPreDnsAppUid: top_app_uid cmd execute failed", e);
                        }
                        argc2 = 0;
                    }
                    argc = argc2;
                }
                try {
                    this.mOemNetdService.setNetBoosterPreDnsAppUid(new String[]{"end"});
                    Slog.i(TAG, "setNetBoosterPreDnsAppUid: top_app_uid cmd execute end succeeded");
                } catch (RemoteException | ServiceSpecificException e2) {
                    Slog.e(TAG, "setNetBoosterPreDnsAppUid: top_app_uid cmd execute end failed", e2);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setNetBoosterPreDnsBrowerUid(Bundle b) {
        if (this.mBoosterPreDnsEnabled) {
            if (this.mOemNetdService == null) {
                Slog.e(TAG, "setNetBoosterPreDnsBrowerUid mOemNetdService is null");
                return;
            }
            String preDnsBrowserUid = b.getString(BROWSER_UID_INFO);
            String str = TAG;
            Slog.d(str, "setNetBoosterPreDnsBrowerUid: preDnsBrowserUid: " + preDnsBrowserUid);
            if (preDnsBrowserUid != null) {
                String[] browsers = preDnsBrowserUid.split(" ");
                int[] browserUid = new int[12];
                int argc = 0;
                for (int i = 0; i < browsers.length; i++) {
                    int argc2 = argc + 1;
                    try {
                        browserUid[argc] = Integer.parseInt(browsers[i]);
                        if (i == browsers.length - 1 || argc2 == 12) {
                            int[] iArr = new int[argc2];
                            this.mOemNetdService.setNetBoosterPreDnsBrowerUid(Arrays.copyOfRange(browserUid, 0, argc2));
                            Slog.i(TAG, "setNetBoosterPreDnsBrowerUid: browser_uid cmd execute succeeded");
                            argc2 = 0;
                            argc = argc2;
                        } else {
                            argc = argc2;
                        }
                    } catch (RemoteException | ServiceSpecificException | NumberFormatException e) {
                        Slog.e(TAG, "setNetBoosterPreDnsBrowerUid: browser_uid cmd execute failed");
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setNetBoosterPreDnsDomainName(Bundle b) {
        if (this.mBoosterPreDnsEnabled) {
            if (this.mOemNetdService == null) {
                Slog.e(TAG, "setNetBoosterPreDnsDomainName mOemNetdService is null");
                return;
            }
            String preDnsUrl = b.getString(DNS_DOMAIN_NAME);
            List domainList = new ArrayList();
            Slog.d(TAG, "setNetBoosterPreDnsDomainName: preDnsUrl: " + preDnsUrl);
            if (preDnsUrl != null) {
                String[] urls = preDnsUrl.split(",");
                int cmdLength = 0;
                int i = 0;
                while (true) {
                    if (i < urls.length) {
                        String[] urlAlias = urls[i].split(" ");
                        try {
                            cmdLength = Integer.parseInt(urlAlias[1]) + cmdLength + 2;
                        } catch (NumberFormatException e) {
                            Slog.e(TAG, "setNetBoosterPreDnsDomainName: top_url cnt format err", e);
                        }
                        if (cmdLength > 12) {
                            try {
                                this.mOemNetdService.setNetBoosterPreDnsDomainName((String[]) domainList.toArray(new String[domainList.size()]));
                                Slog.i(TAG, "setNetBoosterPreDnsDomainName: top_url cmd execute succeeded");
                            } catch (RemoteException | ServiceSpecificException e2) {
                                Slog.e(TAG, "setNetBoosterPreDnsDomainName: top_url cmd execute failed", e2);
                            }
                            cmdLength = 0;
                            domainList = new ArrayList();
                        }
                        for (String s : urlAlias) {
                            domainList.add(s);
                        }
                        i++;
                    } else {
                        try {
                            this.mOemNetdService.setNetBoosterPreDnsDomainName((String[]) domainList.toArray(new String[domainList.size()]));
                            this.mOemNetdService.setNetBoosterPreDnsDomainName(new String[]{"end"});
                            Slog.i(TAG, "setNetBoosterPreDnsDomainName: top_url cmd execute succeeded");
                            return;
                        } catch (RemoteException | ServiceSpecificException e3) {
                            Slog.e(TAG, "setNetBoosterPreDnsDomainName: top_url cmd execute failed", e3);
                            return;
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setNetBoosterUidForeground(Bundle b) {
        if (this.mBoosterPreDnsEnabled) {
            if (this.mOemNetdService == null) {
                Slog.e(TAG, "setNetBoosterUidForeground mOemNetdService is null");
                return;
            }
            int uid = b.getInt(FOREGROUND_UID);
            boolean isForeground = b.getBoolean(FOREGROUND_STATE);
            String str = TAG;
            Slog.i(str, "setNetBoosterUidForeground uid: " + uid + ", foreground: " + isForeground);
            try {
                this.mOemNetdService.setNetBoosterUidForeground(uid, isForeground);
                Slog.i(TAG, "setNetBoosterUidForeground: uid_foreground execute succeeded");
            } catch (RemoteException | ServiceSpecificException e) {
                Slog.e(TAG, "setNetBoosterUidForeground: uid_foreground execute failed", e);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setNetBoosterDnsConfig(Bundle data) {
        if (this.mDnsResolver == null) {
            Slog.e(TAG, "setNetBoosterDnsConfig : mDnsResolver get failed");
            return;
        }
        ResolverParamsParcel paramsParcel = new ResolverParamsParcel();
        paramsParcel.netId = data.getInt("netId");
        paramsParcel.sampleValiditySeconds = DNS_RESOLVER_DEFAULT_SAMPLE_VALIDITY_SECONDS;
        paramsParcel.successThreshold = 25;
        paramsParcel.minSamples = 8;
        paramsParcel.maxSamples = 64;
        paramsParcel.tlsName = "";
        paramsParcel.tlsServers = EmptyArray.STRING;
        paramsParcel.tlsFingerprints = EmptyArray.STRING;
        try {
            paramsParcel.servers = data.getStringArray(DNS_CONFIG_ASSIGNED_SERVERS);
            paramsParcel.domains = data.getStringArray(DNS_CONFIG_DOMAIN_STRINGS);
            this.mDnsResolver.setResolverConfiguration(paramsParcel);
            Slog.d(TAG, "setResolverConfiguration success");
        } catch (RemoteException | ArrayIndexOutOfBoundsException e) {
            Slog.e(TAG, "setResolverConfiguration failed");
        }
    }

    private void ipTableConfig(boolean enable, String iface, int uid, IBinder binder) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        String str = TAG;
        Slog.d(str, "ipTableConfig: enable: " + enable + ", iface: " + iface + ", uid: " + uid);
        if (iface != null && uid != -1 && binder != null) {
            IOemNetd iOemNetd = this.mOemNetdService;
            if (iOemNetd == null) {
                Slog.e(TAG, "ipTableConfig mOemNetdService is null");
                return;
            }
            try {
                iOemNetd.ipTableConfig(enable ? 1 : 0, uid, iface);
                if (enable) {
                    binder.linkToDeath(new IBinder.DeathRecipient(uid, iface) {
                        /* class com.android.server.$$Lambda$HwNetworkManagementService$_tQzKyoB99yvPYkL66Pa2d_mf8Q */
                        private final /* synthetic */ int f$1;
                        private final /* synthetic */ String f$2;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        @Override // android.os.IBinder.DeathRecipient
                        public final void binderDied() {
                            HwNetworkManagementService.this.lambda$ipTableConfig$0$HwNetworkManagementService(this.f$1, this.f$2);
                        }
                    }, 0);
                }
            } catch (RemoteException | ServiceSpecificException e) {
                Slog.e(TAG, "ipTableConfig failed");
            }
        }
    }

    public /* synthetic */ void lambda$ipTableConfig$0$HwNetworkManagementService(int uid, String iface) {
        try {
            this.mOemNetdService.ipTableConfig(0, uid, iface);
        } catch (RemoteException | ServiceSpecificException e) {
            Slog.e(TAG, "ipTableConfig failed");
        }
    }

    private boolean setMpDnsApp(int action, int uid, String[] hosts) {
        IOemNetd iOemNetd = this.mOemNetdService;
        if (iOemNetd == null) {
            Slog.w(TAG, "setMpDnsApp oem netd servies null");
            return false;
        }
        try {
            iOemNetd.SetMpDnsApp(action, uid, hosts);
            return true;
        } catch (RemoteException | ServiceSpecificException e) {
            Slog.e(TAG, "setMpDnsApp failed");
            return false;
        }
    }

    private int getNetworkPolicyFlag() {
        String flagStr = Settings.System.getString(this.mContext.getContentResolver(), KEY_NETWORK_POLICY_FLAG);
        if (TextUtils.isEmpty(flagStr)) {
            Slog.d(TAG, "getNetworkPolicyFlag flagStr null");
            return -1;
        }
        try {
            return Integer.parseInt(flagStr, 2);
        } catch (NumberFormatException e) {
            String str = TAG;
            Slog.e(str, "getNetworkPolicyFlag parseInt error flagStr = " + flagStr);
            return -1;
        }
    }

    private void setInterfaceProxyArp(boolean enabled, String ifaceName) {
        if (ifaceName != null && this.mOemNetdService != null) {
            try {
                String str = TAG;
                Slog.d(str, "setInterfaceProxyArp enabled: " + enabled);
                this.mOemNetdService.SetInterfaceProxyArp(enabled, ifaceName);
            } catch (RemoteException e) {
                Slog.d(TAG, e.toString());
            }
        }
    }

    private void setDnsForwarding(boolean enabled, String dnsServer) {
        if (dnsServer != null && this.mOemNetdService != null) {
            try {
                String str = TAG;
                Slog.d(str, "setDnsForwarding enabled: " + enabled + " dns: " + dnsServer);
                this.mOemNetdService.SetDnsForwarding(enabled, dnsServer);
            } catch (RemoteException e) {
                Slog.d(TAG, e.toString());
            }
        }
    }

    private void hwSetArpIgnore(int value, String ifaceName) {
        if (this.mOemNetdService == null || ifaceName == null) {
            Slog.e(TAG, "hwSetArpIgnore mOemNetdService or ifaceName is null");
            return;
        }
        String str = TAG;
        Slog.d(str, "hwSetArpIgnore value: " + value + ", ifaceName = " + ifaceName);
        try {
            this.mOemNetdService.HwSetArpIgnore(value, ifaceName);
        } catch (RemoteException e) {
            Slog.e(TAG, "hwSetArpIgnore exception happen");
        }
    }
}
