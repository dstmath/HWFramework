package com.android.server;

import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.hdm.HwDeviceManager;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.NetworkInfo;
import android.net.RouteInfo;
import android.net.UidRange;
import android.net.booster.IHwCommBoosterCallback;
import android.net.booster.IHwCommBoosterServiceManager;
import android.net.wifi.HuaweiApConfiguration;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceSpecificException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserManager;
import android.provider.Settings;
import android.provider.SettingsEx;
import android.text.TextUtils;
import android.util.Slog;
import com.android.server.NativeDaemonConnector;
import com.android.server.NetworkManagementService;
import com.android.server.display.HwUibcReceiver;
import com.android.server.rms.iaware.dev.SceneInfo;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HwNetworkManagementService extends NetworkManagementService {
    private static final String ACTION_WIFI_AP_STA_JOIN = "android.net.wifi.WIFI_AP_STA_JOIN";
    private static final String ACTION_WIFI_AP_STA_LEAVE = "android.net.wifi.WIFI_AP_STA_LEAVE";
    private static final String AD_APKDL_STRATEGY_PERMISSION = "com.huawei.permission.AD_APKDL_STRATEGY";
    private static final int AD_STRATEGY = 0;
    private static final int APK_CONTROL_STRATEGY = 2;
    private static final int APK_DL_STRATEGY = 1;
    private static final String ARG_ADD = "add";
    private static final String ARG_CLEAR = "clear";
    private static final String ARG_IP_WHITELIST = "ipwhitelist";
    private static final String ARG_SET = "set";
    private static final String BROWSER_UID_INFO = "BrowserUidInfo";
    private static final String CHR_BROADCAST_PERMISSION = "com.huawei.android.permission.GET_CHR_DATA";
    private static final String CMD_NET_FILTER = "net_filter";
    private static final int CODE_CLEAR_AD_APKDL_STRATEGY = 1103;
    private static final int CODE_CLOSE_SOCKETS_FOR_UID = 1107;
    private static final int CODE_GET_APLINKED_STA_LIST = 1005;
    private static final int CODE_GET_WIFI_DNS_STAT = 1011;
    private static final int CODE_IP_TABLE_CONFIG = 1111;
    private static final int CODE_PRINT_AD_APKDL_STRATEGY = 1104;
    private static final int CODE_REMOVE_LEGACYROUTE_TO_HOST = 1015;
    private static final int CODE_SET_AD_STRATEGY_RULE = 1101;
    private static final int CODE_SET_APK_CONTROL_STRATEGY = 1109;
    private static final int CODE_SET_APK_DL_STRATEGY = 1102;
    private static final int CODE_SET_APK_DL_URL_USER_RESULT = 1105;
    private static final int CODE_SET_AP_CONFIGRATION_HW = 1008;
    private static final int CODE_SET_CHR_REPORT_APP_LIST = 1108;
    private static final int CODE_SET_FIREWALL_RULE_FOR_PID = 1110;
    private static final int CODE_SET_NETWORK_ACCESS_WHITELIST = 1106;
    private static final int CODE_SET_SOFTAP_DISASSOCIATESTA = 1007;
    private static final int CODE_SET_SOFTAP_MACFILTER = 1006;
    private static final int CODE_SET_SOFTAP_TX_POWER = 1009;
    private static final int DATA_SEND_TO_KERNEL_APP_QOE_RSRP = 4;
    private static final int DATA_SEND_TO_KERNEL_APP_QOE_UID = 3;
    private static final int DATA_SEND_TO_KERNEL_BS_SUPPORT_VIDEO_ACC = 1;
    public static final int DATA_SEND_TO_KERNEL_SETTING_PARAMS = 9;
    private static final int DATA_SEND_TO_KERNEL_SUPPORT_AI_CHANGE = 2;
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
    private static final String DNS_DOMAIN_NAME = "DnsDomainName";
    private static final String EVENT_KEY = "event_key";
    private static final int EVENT_REGISTER_BOOSTER_CALLBACK = 0;
    private static final String EXP_INFO_REPORT_ENABLE = "ExpInfoReportState";
    private static final String EXTRA_CURRENT_TIME = "currentTime";
    private static final String EXTRA_STA_COUNT = "staCount";
    private static final String EXTRA_STA_INFO = "macInfo";
    private static final String FOREGROUND_STATE = "ForegroundState";
    private static final String FOREGROUND_UID = "ForegroundUid";
    private static final String HEX_STR = "0123456789ABCDEF";
    private static final int HSM_TRANSACT_CODE = 201;
    private static final String HW_SYSTEM_SERVER_START = "com.huawei.systemserver.START";
    private static final String INTENT_APKDL_URL_DETECTED = "com.android.intent.action.apkdl_url_detected";
    private static final String INTENT_DS_WIFI_WEB_STAT_REPORT = "com.huawei.chr.wifi.action.web_stat_report";
    private static final int IP_TABLE_CONFIG_DISABLE = 0;
    private static final int IP_TABLE_CONFIG_ENABLE = 1;
    private static final String ISM_COEX_ON = "ro.config.hw_ismcoex";
    private static final int KERNEL_DATA_MEDIA_INFO = 1;
    private static final int KERNEL_DATA_NET_SPEED_EXP_INFO = 2;
    private static final String MAC_KEY = "mac_key";
    private static final int MAX_ARGC_PER_COMMAND = 12;
    private static final int PER_STRATEGY_SIZE = 470;
    private static final int PER_UID_LIST_SIZE = 50;
    private static final int STATE_ON = 1;
    private static final String STA_JOIN_EVENT = "STA_JOIN";
    private static final int STA_JOIN_HANDLE_DELAY = 5000;
    private static final String STA_LEAVE_EVENT = "STA_LEAVE";
    /* access modifiers changed from: private */
    public static final String TAG = HwNetworkManagementService.class.getSimpleName();
    private static final String TOP_APP_UID_INFO = "TogAppUidInfo";
    private static final String VIDEO_INFO_REPORT_ENABLE = "VideoInfoReportState";
    private static final int WEB_STAT = 0;
    private static final int WIFI_STAT_DELTA = 238;
    /* access modifiers changed from: private */
    public Handler mApLinkedStaHandler = new Handler() {
        public void handleMessage(Message msg) {
            String action = null;
            long mCurrentTime = System.currentTimeMillis();
            Bundle bundle = msg.getData();
            String event = bundle.getString(HwNetworkManagementService.EVENT_KEY);
            String macStr = bundle.getString(HwNetworkManagementService.MAC_KEY).toLowerCase();
            if (HwNetworkManagementService.STA_JOIN_EVENT.equals(event)) {
                action = HwNetworkManagementService.ACTION_WIFI_AP_STA_JOIN;
                if (!HwNetworkManagementService.this.mMacList.contains(macStr)) {
                    HwNetworkManagementService.this.mMacList.add(macStr);
                    int unused = HwNetworkManagementService.this.mLinkedStaCount = HwNetworkManagementService.this.mLinkedStaCount + 1;
                } else {
                    Slog.e(HwNetworkManagementService.TAG, macStr + " had been added, but still get event " + event);
                }
            } else if (HwNetworkManagementService.STA_LEAVE_EVENT.equals(event)) {
                action = HwNetworkManagementService.ACTION_WIFI_AP_STA_LEAVE;
                if (HwNetworkManagementService.this.mApLinkedStaHandler.hasMessages(msg.what)) {
                    HwNetworkManagementService.this.mApLinkedStaHandler.removeMessages(msg.what);
                    Slog.d(HwNetworkManagementService.TAG, "event=" + event + ", remove STA_JOIN message");
                } else if (HwNetworkManagementService.this.mMacList.contains(macStr)) {
                    HwNetworkManagementService.this.mMacList.remove(macStr);
                    HwNetworkManagementService.access$110(HwNetworkManagementService.this);
                } else {
                    Slog.e(HwNetworkManagementService.TAG, macStr + " had been removed, but still get event " + event);
                }
            }
            Slog.d(HwNetworkManagementService.TAG, "handle " + event + " event, mLinkedStaCount=" + HwNetworkManagementService.this.mLinkedStaCount);
            if (HwNetworkManagementService.this.mLinkedStaCount < 0 || HwNetworkManagementService.this.mLinkedStaCount > 8 || HwNetworkManagementService.this.mLinkedStaCount != HwNetworkManagementService.this.mMacList.size()) {
                Slog.e(HwNetworkManagementService.TAG, "mLinkedStaCount over flow, need synchronize. value = " + HwNetworkManagementService.this.mLinkedStaCount);
                try {
                    String[] macList = HwNetworkManagementService.this.mConnector.doListCommand("softap", 121, new Object[]{"assoclist"});
                    List unused2 = HwNetworkManagementService.this.mMacList = new ArrayList();
                    if (macList == null) {
                        int unused3 = HwNetworkManagementService.this.mLinkedStaCount = 0;
                    } else {
                        for (String mac : macList) {
                            if (mac == null) {
                                Slog.e(HwNetworkManagementService.TAG, "get mac from macList is null");
                            } else {
                                HwNetworkManagementService.this.mMacList.add(mac.toLowerCase());
                            }
                        }
                        int unused4 = HwNetworkManagementService.this.mLinkedStaCount = HwNetworkManagementService.this.mMacList.size();
                    }
                } catch (NativeDaemonConnectorException e) {
                    Slog.e(HwNetworkManagementService.TAG, "Cannot communicate with native daemon to get linked stations list");
                    List unused5 = HwNetworkManagementService.this.mMacList = new ArrayList();
                    int unused6 = HwNetworkManagementService.this.mLinkedStaCount = 0;
                }
            }
            String staInfo = String.format("MAC=%s TIME=%d STACNT=%d", new Object[]{macStr, Long.valueOf(mCurrentTime), Integer.valueOf(HwNetworkManagementService.this.mLinkedStaCount)});
            Slog.e(HwNetworkManagementService.TAG, "send broadcast, event=" + event + ", extraInfo: " + staInfo);
            Intent broadcast = new Intent(action);
            broadcast.putExtra(HwNetworkManagementService.EXTRA_STA_INFO, macStr);
            broadcast.putExtra(HwNetworkManagementService.EXTRA_CURRENT_TIME, mCurrentTime);
            broadcast.putExtra(HwNetworkManagementService.EXTRA_STA_COUNT, HwNetworkManagementService.this.mLinkedStaCount);
            HwNetworkManagementService.this.mContext.sendBroadcast(broadcast, "android.permission.ACCESS_WIFI_STATE");
        }
    };
    /* access modifiers changed from: private */
    public boolean mBoosterEnabled = SystemProperties.getBoolean("ro.config.hw_booster", true);
    private boolean mBoosterNetAiChangeEnabled = SystemProperties.getBoolean("ro.config.hisi_net_ai_change", true);
    private boolean mBoosterPreDnsEnabled = SystemProperties.getBoolean("ro.config.pre_dns_query", true);
    private boolean mBoosterVideoAccEnabled = SystemProperties.getBoolean("ro.config.hisi_video_acc", true);
    private int mChannel;
    private AtomicInteger mCmdId;
    /* access modifiers changed from: private */
    public NativeDaemonConnector mConnector;
    /* access modifiers changed from: private */
    public Context mContext;
    TablePortDeathRecipient mDeathReceipient = new TablePortDeathRecipient("", -1);
    private HuaweiApConfiguration mHwApConfig;
    private final BroadcastReceiver mHwSystemServerStartReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                Slog.d(HwNetworkManagementService.TAG, "HwSystemServerStartReceiver intent=null");
                return;
            }
            String action = intent.getAction();
            String access$200 = HwNetworkManagementService.TAG;
            Slog.d(access$200, "HwSystemServerStartReceiver action=" + action);
            if (HwNetworkManagementService.HW_SYSTEM_SERVER_START.equals(action) && HwNetworkManagementService.this.mBoosterEnabled) {
                HwNetworkManagementService.this.handleRegisterBoosterCallback();
            }
        }
    };
    private IHwCommBoosterCallback mIHwCommBoosterCallback = new IHwCommBoosterCallback.Stub() {
        public void callBack(int type, Bundle b) throws RemoteException {
            if (b != null) {
                switch (type) {
                    case 1:
                        if (1 != b.getInt(HwNetworkManagementService.VIDEO_INFO_REPORT_ENABLE)) {
                            HwNetworkManagementService.this.setNetBoosterVodEnabled(false);
                            break;
                        } else {
                            HwNetworkManagementService.this.setNetBoosterVodEnabled(true);
                            break;
                        }
                    case 2:
                        if (1 != b.getInt(HwNetworkManagementService.EXP_INFO_REPORT_ENABLE)) {
                            HwNetworkManagementService.this.setNetBoosterKsiEnabled(false);
                            break;
                        } else {
                            HwNetworkManagementService.this.setNetBoosterKsiEnabled(true);
                            break;
                        }
                    case 3:
                        HwNetworkManagementService.this.setNetBoosterAppUid(b);
                        break;
                    case 4:
                        HwNetworkManagementService.this.setNetBoosterRsrpRsrq(b);
                        break;
                    case 5:
                        HwNetworkManagementService.this.setNetBoosterPreDnsAppUid(b);
                        break;
                    case 6:
                        HwNetworkManagementService.this.setNetBoosterPreDnsBrowerUid(b);
                        break;
                    case 7:
                        HwNetworkManagementService.this.setNetBoosterPreDnsDomainName(b);
                        break;
                    case 8:
                        HwNetworkManagementService.this.setNetBoosterUidForeground(b);
                        break;
                    case 9:
                        HwNetworkManagementService.this.sendSettingParamsToKernel(b);
                        break;
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public int mLinkedStaCount = 0;
    /* access modifiers changed from: private */
    public List<String> mMacList = new ArrayList();
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

    static class NetdResponseCode {
        public static final int ApLinkedStaListChangeHISI = 651;
        public static final int ApLinkedStaListChangeQCOM = 901;
        public static final int HwDnsStat = 130;
        public static final int SoftapDhcpListResult = 122;
        public static final int SoftapListResult = 121;

        NetdResponseCode() {
        }
    }

    private class TablePortDeathRecipient implements IBinder.DeathRecipient {
        private String mInterface;
        private int mUID;

        public TablePortDeathRecipient(String iface, int uid) {
            this.mInterface = iface;
            this.mUID = uid;
        }

        public void setInterfaceAndUid(String iface, int uid) {
            this.mInterface = iface;
            this.mUID = uid;
        }

        public void binderDied() {
            String access$200 = HwNetworkManagementService.TAG;
            Slog.d(access$200, "TablePort Binder Death: mInterface=" + this.mInterface);
            try {
                HwNetworkManagementService.this.mNetdService.ipTableConfig(0, this.mUID, this.mInterface);
            } catch (RemoteException | ServiceSpecificException e) {
                String access$2002 = HwNetworkManagementService.TAG;
                Slog.e(access$2002, "binderDied error!" + e);
            }
        }
    }

    static /* synthetic */ int access$110(HwNetworkManagementService x0) {
        int i = x0.mLinkedStaCount;
        x0.mLinkedStaCount = i - 1;
        return i;
    }

    /* access modifiers changed from: private */
    public void handleRegisterBoosterCallback() {
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

    public HwNetworkManagementService(Context context, String socket, NetworkManagementService.SystemServices services) {
        super(context, socket, services);
        this.mContext = context;
        this.mCmdId = new AtomicInteger(0);
        IntentFilter filter = new IntentFilter(HW_SYSTEM_SERVER_START);
        if (this.mContext != null) {
            this.mContext.registerReceiver(this.mHwSystemServerStartReceiver, filter);
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
                Slog.d(TAG, "code == CODE_SET_NETWORK_ACCESS_WHITELIST");
                data.enforceInterface(DESCRIPTOR_NETWORKMANAGEMENT_SERVICE);
                this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_NETWORK_MANAGER", TAG);
                List<String> whitelist = new ArrayList<>();
                data.readStringList(whitelist);
                setNetworkAccessWhitelist(whitelist);
                reply.writeNoException();
                return true;
            } else if (code == 1015) {
                Slog.d(TAG, "code == CODE_REMOVE_LEGACYROUTE_TO_HOST");
                data.enforceInterface(DESCRIPTOR);
                this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
                int netId = data.readInt();
                RouteInfo routeInfo = (RouteInfo) RouteInfo.CREATOR.createFromParcel(data);
                int uid2 = data.readInt();
                Slog.d(TAG, "netId = " + netId + " uid = " + uid2 + " routeInfo = " + routeInfo);
                removeLegacyRouteForNetId(netId, routeInfo, uid2);
                reply.writeNoException();
                return true;
            } else if (code == 1107) {
                data.enforceInterface(DESCRIPTOR_NETWORKMANAGEMENT_SERVICE);
                this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
                closeSocketsForUid(data.readInt());
                reply.writeNoException();
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
            } else if (code != CODE_SET_FIREWALL_RULE_FOR_PID) {
                return HwNetworkManagementService.super.onTransact(code, data, reply, flags);
            } else {
                data.enforceInterface(DESCRIPTOR_NETWORKMANAGEMENT_SERVICE);
                this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
                setFirewallPidRule(data.readInt(), data.readInt(), data.readInt());
                reply.writeNoException();
                return true;
            }
        }
    }

    private boolean executeHsmCommand(Parcel data, Parcel reply) {
        try {
            String cmd = data.readString();
            Object[] args = data.readArray(null);
            if (this.mConnector != null) {
                reply.writeInt(this.mConnector.execute(cmd, args).isClassOk() ? 1 : 0);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void setConnector(NativeDaemonConnector connector) {
        this.mConnector = connector;
    }

    private String getChannel(WifiConfiguration wifiConfig) {
        if (wifiConfig.apBand != 0 || !SystemProperties.getBoolean(ISM_COEX_ON, false)) {
            this.mChannel = Settings.Secure.getInt(this.mContext.getContentResolver(), "wifi_ap_channel", 0);
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
        int maxscb = Settings.Secure.getInt(this.mContext.getContentResolver(), "wifi_ap_maxscb", 8);
        String str = TAG;
        Slog.d(str, "maxscb=" + maxscb);
        return String.valueOf(maxscb);
    }

    private static String getSecurityType(WifiConfiguration wifiConfig) {
        int authType = wifiConfig.getAuthType();
        if (authType == 1) {
            return "wpa-psk";
        }
        if (authType != 4) {
            return "open";
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
            String msg1 = String.format("Invalid event from daemon (%s)", new Object[]{raw});
            Slog.d(TAG, "receive DataSpeedSlowDetected,return error 1");
            throw new IllegalStateException(msg1);
        }
        int sourceAddress = Integer.parseInt(cooked[2]);
        NetworkInfo mobileNetinfo = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getNetworkInfo(0);
        Slog.d(TAG, "onEvent receive DataSpeedSlowDetected");
        if (mobileNetinfo != null && mobileNetinfo.isConnected()) {
            Slog.d(TAG, "onEvent receive DataSpeedSlowDetected,mobile network is connected!");
            Intent chrIntent = new Intent("com.android.intent.action.data_speed_slow");
            chrIntent.putExtra("sourceAddress", sourceAddress);
            this.mContext.sendBroadcast(chrIntent, CHR_BROADCAST_PERMISSION);
        }
    }

    public void sendDSCPChangeMessage(String[] cooked, String raw) {
        if (cooked.length < 4 || !cooked[1].equals("DSCPINFO")) {
            String msg1 = String.format("Invalid event from daemon (%s)", new Object[]{raw});
            Slog.d(TAG, "receive sendDSCPChangeMessage,return error 1");
            throw new IllegalStateException(msg1);
        }
        NetworkInfo networkInfoWlan = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getNetworkInfo(1);
        if (networkInfoWlan != null && networkInfoWlan.isConnected()) {
            Intent chrIntent = new Intent("com.android.intent.action.wifi_dscp_change");
            chrIntent.putExtra("dscpvalue", Integer.parseInt(cooked[2]));
            chrIntent.putExtra("uid", Integer.parseInt(cooked[3]));
            this.mContext.sendBroadcast(chrIntent, CHR_BROADCAST_PERMISSION);
        }
    }

    public void sendWebStatMessage(String[] cooked, String raw) {
        if (cooked.length < 20 || !cooked[1].equals("ReportType")) {
            throw new IllegalStateException(String.format("Invalid event from daemon (%s)", new Object[]{raw}));
        }
        try {
            NetworkInfo mobileNetinfo = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getNetworkInfo(0);
            String str = TAG;
            Slog.d(str, "onEvent receive Web Stat Report:" + raw);
            if (mobileNetinfo != null && mobileNetinfo.isConnected()) {
                Intent chrIntent = new Intent("com.android.intent.action.web_stat_report");
                chrIntent.putExtra("ReportType", Integer.parseInt(cooked[2]));
                chrIntent.putExtra("RTT", Integer.parseInt(cooked[3]));
                chrIntent.putExtra("WebDelay", Integer.parseInt(cooked[4]));
                chrIntent.putExtra("SuccNum", Integer.parseInt(cooked[5]));
                chrIntent.putExtra("FailNum", Integer.parseInt(cooked[6]));
                chrIntent.putExtra("NoAckNum", Integer.parseInt(cooked[7]));
                chrIntent.putExtra("TotalNum", Integer.parseInt(cooked[8]));
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
                this.mContext.sendBroadcast(chrIntent, CHR_BROADCAST_PERMISSION);
            }
            int WifiWebDelay = Integer.parseInt(cooked[241]);
            int exceptionCnt = Integer.parseInt(cooked[468]);
            if (WifiWebDelay > 0 || exceptionCnt > 0) {
                Intent wifichrIntent = new Intent(INTENT_DS_WIFI_WEB_STAT_REPORT);
                wifichrIntent.putExtra("ReportType", Integer.parseInt(cooked[239]));
                wifichrIntent.putExtra("RTT", Integer.parseInt(cooked[240]));
                wifichrIntent.putExtra("WebDelay", Integer.parseInt(cooked[241]));
                wifichrIntent.putExtra("SuccNum", Integer.parseInt(cooked[242]));
                wifichrIntent.putExtra("FailNum", Integer.parseInt(cooked[243]));
                wifichrIntent.putExtra("NoAckNum", Integer.parseInt(cooked[244]));
                wifichrIntent.putExtra("TotalNum", Integer.parseInt(cooked[245]));
                wifichrIntent.putExtra("TcpTotalNum", Integer.parseInt(cooked[246]));
                wifichrIntent.putExtra("DelayL1", Integer.parseInt(cooked[247]));
                wifichrIntent.putExtra("DelayL2", Integer.parseInt(cooked[248]));
                wifichrIntent.putExtra("DelayL3", Integer.parseInt(cooked[249]));
                wifichrIntent.putExtra("DelayL4", Integer.parseInt(cooked[250]));
                wifichrIntent.putExtra("DelayL5", Integer.parseInt(cooked[251]));
                wifichrIntent.putExtra("DelayL6", Integer.parseInt(cooked[252]));
                wifichrIntent.putExtra("RTTL1", Integer.parseInt(cooked[253]));
                wifichrIntent.putExtra("RTTL2", Integer.parseInt(cooked[254]));
                wifichrIntent.putExtra("RTTL3", Integer.parseInt(cooked[255]));
                wifichrIntent.putExtra("RTTL4", Integer.parseInt(cooked[256]));
                wifichrIntent.putExtra("RTTL5", Integer.parseInt(cooked[257]));
                wifichrIntent.putExtra("TcpSuccNum", Integer.parseInt(cooked[258]));
                wifichrIntent.putExtra("HighestTcpRTT", Integer.parseInt(cooked[451]));
                wifichrIntent.putExtra("LowestTcpRTT", Integer.parseInt(cooked[452]));
                wifichrIntent.putExtra("LastTcpRTT", Integer.parseInt(cooked[453]));
                wifichrIntent.putExtra("HighestWebDelay", Integer.parseInt(cooked[454]));
                wifichrIntent.putExtra("LowestWebDelay", Integer.parseInt(cooked[455]));
                wifichrIntent.putExtra("LastWebDelay", Integer.parseInt(cooked[456]));
                wifichrIntent.putExtra("ServerAddr", Integer.parseInt(cooked[457]));
                wifichrIntent.putExtra("RTTAbnServerAddr", Integer.parseInt(cooked[458]));
                wifichrIntent.putExtra("VideoAvgSpeed", Integer.parseInt(cooked[459]));
                wifichrIntent.putExtra("VideoFreezNum", Integer.parseInt(cooked[460]));
                wifichrIntent.putExtra("VideoTime", Integer.parseInt(cooked[461]));
                wifichrIntent.putExtra("AccVideoAvgSpeed", Integer.parseInt(cooked[462]));
                wifichrIntent.putExtra("AccVideoFreezNum", Integer.parseInt(cooked[463]));
                wifichrIntent.putExtra("AccVideoTime", Integer.parseInt(cooked[464]));
                wifichrIntent.putExtra("tcp_handshake_delay", Integer.parseInt(cooked[465]));
                wifichrIntent.putExtra("http_get_delay", Integer.parseInt(cooked[466]));
                wifichrIntent.putExtra("exception_cnt", Integer.parseInt(cooked[468]));
                wifichrIntent.putExtra("data_direct", Integer.parseInt(cooked[469]));
                wifichrIntent.putExtra("transport_delay", Integer.parseInt(cooked[PER_STRATEGY_SIZE]));
                wifichrIntent.putExtra("ip_delay", Integer.parseInt(cooked[471]));
                wifichrIntent.putExtra("hmac_delay", Integer.parseInt(cooked[472]));
                wifichrIntent.putExtra("driver_delay", Integer.parseInt(cooked[473]));
                wifichrIntent.putExtra("android_uid", Integer.parseInt(cooked[474]));
                this.mContext.sendBroadcast(wifichrIntent, CHR_BROADCAST_PERMISSION);
            }
        } catch (Exception e) {
            Slog.e(TAG, "Web Stat Report Send Broadcast Fail.");
        }
    }

    public boolean handleApLinkedStaListChange(String raw, String[] cooked) {
        Slog.d(TAG, "handleApLinkedStaListChange is called");
        if (STA_JOIN_EVENT.equals(cooked[1]) || STA_LEAVE_EVENT.equals(cooked[1])) {
            String str = TAG;
            Slog.d(str, "Got sta list change event:" + cooked[1]);
            notifyApLinkedStaListChange(cooked[1], cooked[4]);
            return true;
        }
        throw new IllegalStateException(String.format("ApLinkedStaListChange: Invalid event from daemon (%s)", new Object[]{raw}));
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
        HuaweiApConfiguration hwApConfig = new HuaweiApConfiguration();
        hwApConfig.channel = this.mChannel;
        hwApConfig.maxScb = Settings.Secure.getInt(this.mContext.getContentResolver(), "wifi_ap_maxscb", 8);
        try {
            String str = String.format("softap sethw " + wlanIface + " " + softapIface + " %d %d", new Object[]{Integer.valueOf(hwApConfig.channel), Integer.valueOf(hwApConfig.maxScb)});
            this.mConnector.doCommand("softap", new Object[]{"sethw", wlanIface, softapIface, String.valueOf(hwApConfig.channel), String.valueOf(hwApConfig.maxScb)});
            String str2 = TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("setAccessPointHw command: ");
            sb.append(str);
            Slog.d(str2, sb.toString());
        } catch (NativeDaemonConnectorException e) {
            throw new IllegalStateException("Error communicating to native daemon to set soft AP", e);
        }
    }

    private List<String> getApLinkedDhcpList() {
        try {
            Slog.d(TAG, "getApLinkedDhcpList: softap getdhcplease");
            String[] dhcpleaseList = this.mConnector.doListCommand("softap", 122, new Object[]{"getdhcplease"});
            if (dhcpleaseList == null) {
                Slog.e(TAG, "getApLinkedDhcpList Error: doListCommand return NULL");
                return null;
            }
            List<String> mDhcpList = new ArrayList<>();
            for (String dhcplease : dhcpleaseList) {
                String str = TAG;
                Slog.d(str, "getApLinkedDhcpList dhcpList = " + dhcplease);
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
        String ApLinkedStaInfo = String.format("MAC=%s", new Object[]{mac});
        String mac2 = mac.toLowerCase();
        if (mDhcpList != null) {
            for (String dhcplease : mDhcpList) {
                if (dhcplease.contains(mac2)) {
                    String[] Tokens = dhcplease.split(" ");
                    if (4 <= Tokens.length) {
                        Slog.d(TAG, "getApLinkedStaInfo: dhcplease token");
                        ApLinkedStaInfo = String.format(ApLinkedStaInfo + " IP=%s DEVICE=%s", new Object[]{Tokens[2], Tokens[3]});
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
        Message msg = new Message();
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
            int i = 0;
            String[] stats = this.mConnector.doListCommand("resolver", 130, new Object[]{"getdnsstat", Integer.valueOf(netid)});
            if (stats != null) {
                while (true) {
                    int i2 = i;
                    if (i2 >= stats.length) {
                        break;
                    }
                    buf.append(stats[i2]);
                    if (i2 < stats.length - 1) {
                        buf.append(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
                    }
                    i = i2 + 1;
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
            sb.append("0123456789ABCDEF".charAt((bytes[i] & 240) >> 4));
            sb.append("0123456789ABCDEF".charAt((bytes[i] & HwUibcReceiver.CurrentPacket.INPUT_MASK) >> 0));
        }
        return sb.toString();
    }

    private String hexStrToStr(String hexStr) {
        if (hexStr == null) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream(hexStr.length() / 2);
        for (int i = 0; i < hexStr.length(); i += 2) {
            baos.write(("0123456789ABCDEF".indexOf(hexStr.charAt(i)) << 4) | "0123456789ABCDEF".indexOf(hexStr.charAt(i + 1)));
        }
        return new String(baos.toByteArray(), Charset.forName("UTF-8"));
    }

    private ArrayList<String> convertPkgNameToUid(String[] pkgName) {
        String[] strArr = pkgName;
        if (strArr != null) {
            Slog.d(TAG, "convertPkgNameToUid, pkgName=" + Arrays.asList(pkgName));
        }
        ArrayList<String> uidList = new ArrayList<>();
        if (strArr != null && strArr.length > 0) {
            int userCount = UserManager.get(this.mContext).getUserCount();
            List<UserInfo> users = UserManager.get(this.mContext).getUsers();
            PackageManager pm = this.mContext.getPackageManager();
            StringBuilder appUidBuilder = new StringBuilder();
            int length = strArr.length;
            int uidCount = 0;
            int uidCount2 = 0;
            while (uidCount2 < length) {
                String pkg = strArr[uidCount2];
                int uidCount3 = uidCount;
                StringBuilder appUidBuilder2 = appUidBuilder;
                int n = 0;
                while (true) {
                    int n2 = n;
                    if (n2 >= userCount) {
                        break;
                    }
                    try {
                        int uid = pm.getPackageUidAsUser(pkg, users.get(n2).id);
                        Slog.d(TAG, "convertPkgNameToUid, pkg=" + pkg + ", uid=" + uid + ", under user.id=" + users.get(n2).id);
                        uidCount3++;
                        if (uidCount3 % 50 == 0) {
                            appUidBuilder2.append(uid);
                            appUidBuilder2.append(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
                            uidList.add(appUidBuilder2.toString());
                            appUidBuilder2 = new StringBuilder();
                        } else {
                            appUidBuilder2.append(uid);
                            appUidBuilder2.append(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
                        }
                    } catch (Exception e) {
                        Slog.e(TAG, "convertPkgNameToUid, skip unknown packages!");
                    }
                    n = n2 + 1;
                }
                uidCount2++;
                appUidBuilder = appUidBuilder2;
                uidCount = uidCount3;
            }
            if (!TextUtils.isEmpty(appUidBuilder.toString())) {
                uidList.add(appUidBuilder.toString());
            }
        }
        return uidList;
    }

    private void setAdFilterRules(String adStrategy, boolean needReset) {
        boolean z;
        String adStrategy2 = adStrategy;
        String str = TAG;
        Slog.d(str, "setAdFilterRules, adStrategy=" + adStrategy2 + ", needReset=" + z);
        String operation = z ? "reset" : "not_reset";
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
        int strategyLen2 = strategyLen;
        int count2 = count;
        int cmdId = this.mCmdId.incrementAndGet();
        try {
            Slog.d(TAG, "setAdFilterRules, count=" + count2 + ", cmdId=" + cmdId);
            this.mConnector.execute("hwfilter", new Object[]{"set_ad_strategy_rule", operation, Integer.valueOf(cmdId), Integer.valueOf(count2)});
            if (strategyLen2 == 0) {
                Slog.d(TAG, "setAdFilterRules, adStrategy is null!");
                return;
            }
            int i = 1;
            while (adStrategy2.length() > 0) {
                if (adStrategy2.length() > PER_STRATEGY_SIZE) {
                    String adStrategyTmp = adStrategy2.substring(0, PER_STRATEGY_SIZE);
                    Slog.d(TAG, "setAdFilterRules, adStrategy len=" + adStrategyTmp.length() + ", seq=" + i + ", cmdId=" + cmdId);
                    this.mConnector.execute("hwfilter", new Object[]{"set_ad_strategy_buf", Integer.valueOf(cmdId), Integer.valueOf(i), adStrategyTmp});
                    adStrategy2 = adStrategy2.substring(PER_STRATEGY_SIZE);
                    i++;
                } else {
                    Slog.d(TAG, "setAdFilterRules, adStrategy len=" + adStrategy2.length() + ", seq=" + i + ", cmdId=" + cmdId);
                    this.mConnector.execute("hwfilter", new Object[]{"set_ad_strategy_buf", Integer.valueOf(cmdId), Integer.valueOf(i), adStrategy2});
                    return;
                }
            }
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
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
                            this.mConnector.execute("hwfilter", new Object[]{"set_apkdl_strategy_rule", appUidList.get(i), operation});
                        } else {
                            this.mConnector.execute("hwfilter", new Object[]{"set_apkdl_strategy_rule", appUidList.get(i), "not_reset"});
                        }
                    }
                    return;
                }
            } catch (NativeDaemonConnectorException e) {
                throw e.rethrowAsParcelableException();
            }
        }
        this.mConnector.execute("hwfilter", new Object[]{"set_apkdl_strategy_rule", null, operation});
    }

    private void clearAdOrApkDlFilterRules(ArrayList<String> appUidList, boolean needReset, int strategy) {
        Slog.d(TAG, "clearApkDlFilterRules, appUidList=" + appUidList + ", needReset=" + needReset + ", strategy=" + strategy);
        String operation = needReset ? "reset" : "not_reset";
        if (strategy == 0) {
            if (appUidList != null) {
                try {
                    if (appUidList.size() > 0) {
                        for (int i = 0; i < appUidList.size(); i++) {
                            if (i == 0) {
                                this.mConnector.execute("hwfilter", new Object[]{"clear_ad_strategy_rule", appUidList.get(i), operation});
                            } else {
                                this.mConnector.execute("hwfilter", new Object[]{"clear_ad_strategy_rule", appUidList.get(i), "not_reset"});
                            }
                        }
                        return;
                    }
                } catch (NativeDaemonConnectorException e) {
                    throw e.rethrowAsParcelableException();
                }
            }
            this.mConnector.execute("hwfilter", new Object[]{"clear_ad_strategy_rule", null, operation});
        } else if (1 == strategy) {
            if (appUidList == null || appUidList.size() <= 0) {
                this.mConnector.execute("hwfilter", new Object[]{"clear_apkdl_strategy_rule", null, operation});
                return;
            }
            for (int i2 = 0; i2 < appUidList.size(); i2++) {
                if (i2 == 0) {
                    this.mConnector.execute("hwfilter", new Object[]{"clear_apkdl_strategy_rule", appUidList.get(i2), operation});
                } else {
                    this.mConnector.execute("hwfilter", new Object[]{"clear_apkdl_strategy_rule", appUidList.get(i2), "not_reset"});
                }
            }
        } else if (2 == strategy) {
            Slog.d(TAG, "clearApkDlFilterRules strategy is APK_CONTROL_STRATEGY");
            if (appUidList == null || appUidList.size() <= 0) {
                Slog.d(TAG, "clearApkDlFilterRules else netd clear_delta_install_rule");
                this.mConnector.execute("hwfilter", new Object[]{"clear_delta_install_rule", null, operation});
                return;
            }
            int appUidList_size = appUidList.size();
            for (int i3 = 0; i3 < appUidList_size; i3++) {
                if (i3 == 0) {
                    Slog.d(TAG, "clearApkDlFilterRules 0==i netd clear_delta_install_rule");
                    this.mConnector.execute("hwfilter", new Object[]{"clear_delta_install_rule", appUidList.get(i3), operation});
                } else {
                    Slog.d(TAG, "clearApkDlFilterRules 0!=i netd clear_delta_install_rule");
                    this.mConnector.execute("hwfilter", new Object[]{"clear_delta_install_rule", appUidList.get(i3), "not_reset"});
                }
            }
        }
    }

    private void printAdOrApkDlFilterRules(int strategy) {
        String str = TAG;
        Slog.d(str, "printAdOrApkDlFilterRules, strategy=" + strategy);
        if (strategy == 0) {
            try {
                this.mConnector.execute("hwfilter", new Object[]{"output_ad_strategy_rule"});
            } catch (NativeDaemonConnectorException e) {
                throw e.rethrowAsParcelableException();
            }
        } else if (1 == strategy) {
            this.mConnector.execute("hwfilter", new Object[]{"output_apkdl_strategy_rule"});
        } else if (2 == strategy) {
            this.mConnector.execute("hwfilter", new Object[]{"output_delta_install_rule"});
        }
    }

    private void setApkDlUrlUserResult(String downloadId, boolean result) {
        String str = TAG;
        Slog.d(str, "setApkDlUrlUserResult, downloadId=" + downloadId + ", result=" + result);
        try {
            this.mConnector.execute("hwfilter", new Object[]{"apkdl_callback", downloadId, result ? SceneInfo.ITEM_RULE_ALLOW : "reject"});
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:46:0x012e  */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x0136  */
    private String getStrategyStr(int code, int size, Parcel data) {
        int userCount;
        int userCount2;
        int userCount3;
        int uid;
        String str;
        StringBuilder sb;
        int i = code;
        int i2 = size;
        if (i2 <= 0) {
            return null;
        }
        int userCount4 = UserManager.get(this.mContext).getUserCount();
        List<UserInfo> users = UserManager.get(this.mContext).getUsers();
        PackageManager pm = this.mContext.getPackageManager();
        StringBuilder StrategyBuilder = new StringBuilder();
        int i3 = 0;
        while (i3 < i2) {
            String key = data.readString();
            ArrayList<String> value = data.createStringArrayList();
            int i4 = i3 + 1;
            int i5 = TextUtils.isEmpty(key);
            int i6 = CODE_SET_AD_STRATEGY_RULE;
            if (i5 != 0 || value == null) {
                userCount = userCount4;
            } else if (value.size() == 0) {
                userCount = userCount4;
            } else {
                StringBuilder tmpUrlBuilder = new StringBuilder();
                String tmpUrlStr = null;
                int n = 0;
                while (true) {
                    int n2 = n;
                    if (n2 >= userCount4) {
                        break;
                    }
                    try {
                        int uid2 = pm.getPackageUidAsUser(key, users.get(n2).id);
                        if (i6 == i) {
                            try {
                                str = TAG;
                                sb = new StringBuilder();
                                sb.append("CODE_SET_AD_STRATEGY_RULE, adStrategy pkgName=");
                                sb.append(key);
                                sb.append(", uid=");
                                uid = uid2;
                            } catch (Exception e) {
                                int i7 = uid2;
                                userCount3 = userCount4;
                                if (CODE_SET_AD_STRATEGY_RULE == i) {
                                    Slog.e(TAG, "CODE_SET_AD_STRATEGY_RULE, skip unknown packages!");
                                } else if (CODE_SET_APK_CONTROL_STRATEGY == i) {
                                    Slog.e(TAG, "CODE_SET_APK_CONTROL_STRATEGY, skip unknown packages!");
                                }
                                n = n2 + 1;
                                userCount4 = userCount2;
                                int i8 = size;
                                i6 = CODE_SET_AD_STRATEGY_RULE;
                            }
                            try {
                                sb.append(uid);
                                sb.append(", under user.id=");
                                sb.append(users.get(n2).id);
                                Slog.d(str, sb.toString());
                            } catch (Exception e2) {
                                userCount3 = userCount4;
                                if (CODE_SET_AD_STRATEGY_RULE == i) {
                                }
                                n = n2 + 1;
                                userCount4 = userCount2;
                                int i82 = size;
                                i6 = CODE_SET_AD_STRATEGY_RULE;
                            }
                        } else {
                            uid = uid2;
                            if (CODE_SET_APK_CONTROL_STRATEGY == i) {
                                Slog.d(TAG, "CODE_SET_APK_CONTROL_STRATEGY, apkStrategy pkgName=" + key + ", uid=" + uid + ", under user.id=" + users.get(n2).id);
                            }
                        }
                        StrategyBuilder.append(uid);
                        StrategyBuilder.append(":");
                        if (tmpUrlStr == null) {
                            int count = 0;
                            int m = 0;
                            int value_size = value.size();
                            while (m < value_size) {
                                int userCount5 = userCount4;
                                tmpUrlBuilder.append(strToHexStr(value.get(m)));
                                count++;
                                if (count < value.size()) {
                                    tmpUrlBuilder.append(",");
                                } else {
                                    tmpUrlBuilder.append(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
                                }
                                m++;
                                userCount4 = userCount5;
                            }
                            userCount2 = userCount4;
                            tmpUrlStr = tmpUrlBuilder.toString();
                        } else {
                            userCount2 = userCount4;
                        }
                        StrategyBuilder.append(tmpUrlStr);
                    } catch (Exception e3) {
                        userCount3 = userCount4;
                        if (CODE_SET_AD_STRATEGY_RULE == i) {
                        }
                        n = n2 + 1;
                        userCount4 = userCount2;
                        int i822 = size;
                        i6 = CODE_SET_AD_STRATEGY_RULE;
                    }
                    n = n2 + 1;
                    userCount4 = userCount2;
                    int i8222 = size;
                    i6 = CODE_SET_AD_STRATEGY_RULE;
                }
                userCount = userCount4;
                i3 = i4;
                userCount4 = userCount;
                i2 = size;
            }
            if (CODE_SET_AD_STRATEGY_RULE == i) {
                Slog.e(TAG, "CODE_SET_AD_STRATEGY_RULE, skip empty key or value!");
            } else if (CODE_SET_APK_CONTROL_STRATEGY == i) {
                Slog.e(TAG, "CODE_SET_APK_CONTROL_STRATEGY, skip empty key or value!");
            }
            i3 = i4;
            userCount4 = userCount;
            i2 = size;
        }
        return StrategyBuilder.toString();
    }

    private void setApkControlFilterRules(String apkStrategy, boolean needReset) {
        boolean z;
        String apkStrategy2 = apkStrategy;
        String str = TAG;
        Slog.d(str, "setApkControlFilterRules, apkStrategy=" + apkStrategy2 + ", needReset=" + z);
        String operation = z ? "reset" : "not_reset";
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
        int strategyLen2 = strategyLen;
        int count2 = count;
        int cmdId = this.mCmdId.incrementAndGet();
        try {
            Slog.d(TAG, "setApkControlFilterRules, count=" + count2 + ", cmdId=" + cmdId);
            this.mConnector.execute("hwfilter", new Object[]{"set_delta_install_rule", operation, Integer.valueOf(cmdId), Integer.valueOf(count2)});
            if (strategyLen2 == 0) {
                Slog.d(TAG, "setApkControlFilterRules, apkStrategy is null!");
                return;
            }
            int i = 1;
            while (apkStrategy2.length() > 0) {
                if (apkStrategy2.length() > PER_STRATEGY_SIZE) {
                    String apkStrategyTmp = apkStrategy2.substring(0, PER_STRATEGY_SIZE);
                    Slog.d(TAG, "setApkControlFilterRules, apkStrategy len=" + apkStrategyTmp.length() + ", seq=" + i + ", cmdId=" + cmdId);
                    this.mConnector.execute("hwfilter", new Object[]{"set_delta_install_buf", Integer.valueOf(cmdId), Integer.valueOf(i), apkStrategyTmp});
                    apkStrategy2 = apkStrategy2.substring(PER_STRATEGY_SIZE);
                    i++;
                } else {
                    Slog.d(TAG, "setApkFilterRules, apkStrategy len=" + apkStrategy2.length() + ", seq=" + i + ", cmdId=" + cmdId);
                    this.mConnector.execute("hwfilter", new Object[]{"set_delta_install_buf", Integer.valueOf(cmdId), Integer.valueOf(i), apkStrategy2});
                    return;
                }
            }
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void sendApkDownloadUrlBroadcast(String[] cooked, String raw) {
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
                Slog.d(str2, "onEvent receive report_apkdl_event, startTime=" + startTime + ", downloadId=" + downloadId + ", uid=" + uid + ", url=" + url);
                Intent intent = new Intent(INTENT_APKDL_URL_DETECTED);
                intent.putExtra("startTime", startTime);
                intent.putExtra("downloadId", downloadId);
                intent.putExtra("uid", uid);
                intent.putExtra("url", url);
                this.mContext.sendBroadcast(intent, AD_APKDL_STRATEGY_PERMISSION);
                return;
            }
            int max = Integer.parseInt(m.group(1));
            int idx = Integer.parseInt(m.group(2));
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
                Slog.d(str3, "onEvent receive report_apkdl_event, startTime=" + startTime + ", downloadId=" + downloadId + ", uid=" + uid + ", url=" + url2);
                Intent intent2 = new Intent(INTENT_APKDL_URL_DETECTED);
                intent2.putExtra("startTime", this.startTimeMap.get(downloadId));
                intent2.putExtra("downloadId", downloadId);
                intent2.putExtra("uid", uid);
                intent2.putExtra("url", url2);
                this.mContext.sendBroadcast(intent2, AD_APKDL_STRATEGY_PERMISSION);
                return;
            }
            return;
        }
        String errorMessage = String.format("Invalid event from daemon (%s)", new Object[]{raw});
        Slog.d(TAG, "receive report_apkdl_event, return error");
        throw new IllegalStateException(errorMessage);
    }

    public void systemReady() {
        HwNetworkManagementService.super.systemReady();
        initNetworkAccessWhitelist();
    }

    private void initNetworkAccessWhitelist() {
        final List<String> networkAccessWhitelist = HwDeviceManager.getList(9);
        if (networkAccessWhitelist != null && !networkAccessWhitelist.isEmpty()) {
            Slog.d(TAG, "networkAccessWhitelist has been set");
            new Thread() {
                public void run() {
                    HwNetworkManagementService.this.setNetworkAccessWhitelist(networkAccessWhitelist);
                }
            }.start();
        }
    }

    public void setNetworkAccessWhitelist(List<String> addrList) {
        if (addrList != null) {
            try {
                if (!addrList.isEmpty()) {
                    int size = addrList.size();
                    String res = (String) this.mConnector.doCommand(CMD_NET_FILTER, new Object[]{ARG_IP_WHITELIST, ARG_SET, addrList.get(0)}).get(0);
                    String str = TAG;
                    Slog.d(str, "set ipwhitelist:" + res);
                    String str2 = res;
                    for (int i = 1; i < size; i++) {
                        String res2 = (String) this.mConnector.doCommand(CMD_NET_FILTER, new Object[]{ARG_IP_WHITELIST, ARG_ADD, addrList.get(i)}).get(0);
                        String str3 = TAG;
                        Slog.d(str3, "add ipwhitelist:" + res2);
                    }
                    return;
                }
            } catch (NullPointerException npe) {
                Slog.e(TAG, "runNetFilterCmd:", npe);
                return;
            } catch (NativeDaemonConnectorException nde) {
                Slog.e(TAG, "runNetFilterCmd:", nde);
                return;
            }
        }
        String res3 = (String) this.mConnector.doCommand(CMD_NET_FILTER, new Object[]{ARG_IP_WHITELIST, ARG_CLEAR}).get(0);
        String str4 = TAG;
        Slog.d(str4, "clear ipwhitelist:" + res3);
    }

    private void removeLegacyRouteForNetId(int netId, RouteInfo routeInfo, int uid) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        NativeDaemonConnector.Command cmd = new NativeDaemonConnector.Command("network", new Object[]{"route", "legacy", Integer.valueOf(uid), "remove", Integer.valueOf(netId)});
        LinkAddress la = routeInfo.getDestinationLinkAddress();
        cmd.appendArg(routeInfo.getInterface());
        cmd.appendArg(la.getAddress().getHostAddress() + "/" + la.getPrefixLength());
        if (routeInfo.hasGateway()) {
            cmd.appendArg(routeInfo.getGateway().getHostAddress());
        }
        try {
            this.mConnector.execute(cmd);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public boolean closeSocketsForUid(int uid) {
        try {
            this.mNetdService.socketDestroy(new UidRange[]{new UidRange(uid, uid)}, new int[0]);
            return true;
        } catch (RemoteException | ServiceSpecificException e) {
            String str = TAG;
            Slog.e(str, "Error closing sockets for uid " + uid + ": " + e);
            return false;
        }
    }

    private void setChrReportUid(int index, int uid) {
        try {
            NativeDaemonConnector nativeDaemonConnector = this.mConnector;
            Object[] objArr = {"appuid", ARG_SET, Integer.valueOf(index), Integer.valueOf(uid)};
            String str = TAG;
            Slog.d(str, "chr appuid set:" + ((String) nativeDaemonConnector.doCommand("chr", objArr).get(0)));
        } catch (NullPointerException npe) {
            Slog.e(TAG, "runChrCmd:", npe);
        } catch (NativeDaemonConnectorException nde) {
            Slog.e(TAG, "runChrCmd:", nde);
        }
    }

    public void setFirewallPidRule(int chain, int pid, int rule) {
        try {
            this.mConnector.execute("firewall", new Object[]{"set_pid_rule", getFirewallChainName(chain), Integer.valueOf(pid), getFirewallRuleNameHw(chain, rule)});
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    /* access modifiers changed from: private */
    public void setNetBoosterVodEnabled(boolean enable) {
        if (this.mBoosterVideoAccEnabled) {
            try {
                String str = TAG;
                Slog.d(str, "setNetBoosterVodEnabled enable=" + enable);
                NativeDaemonConnector nativeDaemonConnector = this.mConnector;
                Object[] objArr = new Object[2];
                objArr[0] = "vod";
                objArr[1] = enable ? "enable" : "disable";
                nativeDaemonConnector.execute("hwnb", objArr);
            } catch (NativeDaemonConnectorException e) {
                Slog.e(TAG, "setNetBoosterVodEnabled:netd cmd execute failed", e);
            }
        }
    }

    /* access modifiers changed from: private */
    public void setNetBoosterKsiEnabled(boolean enable) {
        if (this.mBoosterNetAiChangeEnabled) {
            try {
                String str = TAG;
                Slog.d(str, "setNetBoosterKsiEnabled enable=" + enable);
                NativeDaemonConnector nativeDaemonConnector = this.mConnector;
                Object[] objArr = new Object[2];
                objArr[0] = "ksi";
                objArr[1] = enable ? "enable" : "disable";
                nativeDaemonConnector.execute("hwnb", objArr);
            } catch (NativeDaemonConnectorException e) {
                Slog.e(TAG, "setNetBoosterKsiEnabled:netd cmd execute failed", e);
            }
        }
    }

    /* access modifiers changed from: private */
    public void setNetBoosterAppUid(Bundle data) {
        int appUid = data.getInt("appUid");
        int period = data.getInt("reportPeriod");
        try {
            String str = TAG;
            Slog.d(str, "setNetBoosterAppUid,appUid=" + appUid + ",period=" + period);
            this.mConnector.execute("hwnb", new Object[]{"appQoe", "uid", Integer.valueOf(appUid), Integer.valueOf(period)});
        } catch (NativeDaemonConnectorException e) {
            Slog.e(TAG, "setNetBoosterAppUid:netd cmd execute failed", e);
        }
    }

    /* access modifiers changed from: private */
    public void setNetBoosterRsrpRsrq(Bundle data) {
        int rsrp = data.getInt("rsrp");
        int rsrq = data.getInt("rsrq");
        try {
            String str = TAG;
            Slog.d(str, "setNetBoosterRsrpRsrq,rsrp=" + rsrp + ",rsrq=" + rsrq);
            this.mConnector.execute("hwnb", new Object[]{"appQoe", "rsrp", Integer.valueOf(rsrp), Integer.valueOf(rsrq)});
        } catch (NativeDaemonConnectorException e) {
            Slog.e(TAG, "setNetBoosterRsrpRsrq:netd cmd execute failed", e);
        }
    }

    /* access modifiers changed from: private */
    public void sendSettingParamsToKernel(Bundle data) {
        int msgId = data.getInt("msgId");
        int param1 = data.getInt("param1");
        int param2 = data.getInt("param2");
        int param3 = data.getInt("param3");
        try {
            String str = TAG;
            Slog.d(str, "sendSettingParamsToKernel,msg=" + msgId);
            this.mConnector.execute("hwnb", new Object[]{"settingParams", Integer.valueOf(msgId), Integer.valueOf(param1), Integer.valueOf(param2), Integer.valueOf(param3)});
        } catch (NativeDaemonConnectorException e) {
            Slog.e(TAG, "sendSettingParamsToKernel:netd cmd execute failed", e);
        }
    }

    public void reportVodParams(int videoSegState, int videoProtocol, int videoRemainingPlayTime, int videoStatus, int aveCodeRate, int segSize, int flowInfoRemote, int flowInfoLocal, int segDuration, int segIndex) {
        int i = videoSegState;
        int i2 = videoProtocol;
        int i3 = videoRemainingPlayTime;
        int i4 = videoStatus;
        int i5 = aveCodeRate;
        int i6 = segSize;
        int i7 = segDuration;
        int i8 = segIndex;
        if (this.mBoosterVideoAccEnabled) {
            String str = TAG;
            Slog.d(str, "reportVodParams:videoSegState=" + i + ",videoProtocol=" + i2 + ",videoRemainingPlayTime=" + i3 + ",videoStatus=" + i4 + ",aveCodeRate=" + i5 + ",segSize=" + i6 + ",segDuration=" + i7 + ",segIndex=" + i8);
            IHwCommBoosterServiceManager bm = HwFrameworkFactory.getHwCommBoosterServiceManager();
            if (bm != null) {
                Bundle data = new Bundle();
                data.putInt("videoSegState", i);
                data.putInt("videoProtocol", i2);
                data.putInt("videoRemainingPlayTime", i3);
                data.putInt("videoStatus", i4);
                data.putInt("aveCodeRate", i5);
                data.putInt("segSize", i6);
                data.putInt("flowInfoRemote", flowInfoRemote);
                data.putInt("flowInfoLocal", flowInfoLocal);
                data.putInt("segDuration", i7);
                data.putInt("segIndex", i8);
                int ret = bm.reportBoosterPara("com.android.server", 1, data);
                if (ret != 0) {
                    String str2 = TAG;
                    Slog.e(str2, "reportVodParams:reportBoosterPara failed, ret=" + ret);
                }
            } else {
                int i9 = flowInfoRemote;
                int i10 = flowInfoLocal;
                Slog.e(TAG, "reportVodParams:null HwCommBoosterServiceManager");
            }
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
                }
            } else {
                Slog.e(TAG, "reportKsiParams:null HwCommBoosterServiceManager");
            }
        }
    }

    /* access modifiers changed from: private */
    public void setNetBoosterPreDnsAppUid(Bundle b) {
        if (this.mBoosterPreDnsEnabled) {
            String preDnsAppUid = b.getString(TOP_APP_UID_INFO);
            Slog.d(TAG, "setNetBoosterPreDnsAppUid: preDnsAppUid: " + preDnsAppUid);
            String[] apps = preDnsAppUid.split(" ");
            Object[] argv = new Object[14];
            argv[0] = "dns";
            argv[1] = "top_app_uid";
            int argc = 2;
            for (int i = 0; i < apps.length; i++) {
                int argc2 = argc + 1;
                argv[argc] = apps[i];
                if (i == apps.length - 1 || argc2 == argv.length) {
                    try {
                        this.mConnector.execute("hwnb", Arrays.copyOf(argv, argc2));
                    } catch (NativeDaemonConnectorException e) {
                        Slog.e(TAG, "setNetBoosterPreDnsAppUid: top_app_uid cmd execute failed", e);
                    }
                    argc = 2;
                } else {
                    argc = argc2;
                }
            }
            try {
                this.mConnector.execute("hwnb", new Object[]{"dns", "top_app_uid", "end"});
            } catch (NativeDaemonConnectorException e2) {
                Slog.e(TAG, "setNetBoosterPreDnsAppUid: top_app_uid end execute failed", e2);
            }
        }
    }

    /* access modifiers changed from: private */
    public void setNetBoosterPreDnsBrowerUid(Bundle b) {
        if (this.mBoosterPreDnsEnabled) {
            String preDnsBrowserUid = b.getString(BROWSER_UID_INFO);
            String str = TAG;
            Slog.d(str, "setNetBoosterPreDnsBrowerUid: preDnsBrowserUid: " + preDnsBrowserUid);
            String[] browsers = preDnsBrowserUid.split(" ");
            Object[] argv = new Object[14];
            argv[0] = "dns";
            argv[1] = "browser_uid";
            int argc = 2;
            for (int i = 0; i < browsers.length; i++) {
                int argc2 = argc + 1;
                argv[argc] = browsers[i];
                if (i == browsers.length - 1 || argc2 == argv.length) {
                    try {
                        this.mConnector.execute("hwnb", Arrays.copyOf(argv, argc2));
                    } catch (NativeDaemonConnectorException e) {
                        Slog.e(TAG, "setNetBoosterPreDnsBrowerUid: browser_uid cmd execute failed", e);
                    }
                    argc = 2;
                } else {
                    argc = argc2;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void setNetBoosterPreDnsDomainName(Bundle b) {
        if (this.mBoosterPreDnsEnabled) {
            String preDnsUrl = b.getString(DNS_DOMAIN_NAME);
            NativeDaemonConnector.Command cmd = new NativeDaemonConnector.Command("hwnb", new Object[]{"dns", "top_url"});
            Slog.d(TAG, "setNetBoosterPreDnsDomainName: preDnsUrl: " + preDnsUrl);
            String[] urls = preDnsUrl.split(",");
            int cmdLength = 0;
            NativeDaemonConnector.Command cmd2 = cmd;
            for (String split : urls) {
                String[] urlAlias = split.split(" ");
                try {
                    cmdLength = Integer.parseInt(urlAlias[1]) + cmdLength + 2;
                } catch (NumberFormatException e) {
                    Slog.e(TAG, "setNetBoosterPreDnsDomainName: top_url cnt format err", e);
                }
                if (cmdLength > 12) {
                    try {
                        this.mConnector.execute(cmd2);
                    } catch (NativeDaemonConnectorException e2) {
                        Slog.e(TAG, "setNetBoosterPreDnsDomainName: top_url cmd execute failed", e2);
                    }
                    cmdLength = 0;
                    cmd2 = new NativeDaemonConnector.Command("hwnb", new Object[]{"dns", "top_url"});
                }
                for (String s : urlAlias) {
                    cmd2.appendArg(s);
                }
            }
            try {
                this.mConnector.execute(cmd2);
                this.mConnector.execute("hwnb", new Object[]{"dns", "top_url", "end"});
            } catch (NativeDaemonConnectorException e3) {
                Slog.e(TAG, "setNetBoosterPreDnsDomainName: top_url cmd execute failed", e3);
            }
        }
    }

    /* access modifiers changed from: private */
    public void setNetBoosterUidForeground(Bundle b) {
        if (this.mBoosterPreDnsEnabled) {
            int uid = b.getInt(FOREGROUND_UID);
            boolean isForeground = b.getBoolean(FOREGROUND_STATE);
            String str = TAG;
            Slog.i(str, "setNetBoosterUidForeground uid: " + uid + ", foreground: " + isForeground);
            try {
                NativeDaemonConnector nativeDaemonConnector = this.mConnector;
                Object[] objArr = new Object[4];
                objArr[0] = "dns";
                objArr[1] = "uid_foreground";
                objArr[2] = Integer.valueOf(uid);
                objArr[3] = isForeground ? "true" : "false";
                nativeDaemonConnector.execute("hwnb", objArr);
            } catch (NativeDaemonConnectorException e) {
                Slog.e(TAG, "setNetBoosterUidForeground: uid_foreground execute failed", e);
            }
        }
    }

    private void ipTableConfig(boolean enable, String iface, int uid, IBinder binder) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        String str = TAG;
        Slog.d(str, "ipTableConfig: enable: " + enable + ", iface: " + iface + ", uid: " + uid);
        try {
            this.mNetdService.ipTableConfig((int) enable, uid, iface);
            if (binder != null && this.mDeathReceipient != null && enable) {
                this.mDeathReceipient.setInterfaceAndUid(iface, uid);
                binder.linkToDeath(this.mDeathReceipient, 0);
            }
        } catch (RemoteException | ServiceSpecificException e) {
            String str2 = TAG;
            Slog.e(str2, "ipTableConfig failed: " + e);
        }
    }
}
