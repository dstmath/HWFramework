package com.android.server;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.hdm.HwDeviceManager;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.NetworkInfo;
import android.net.RouteInfo;
import android.net.UidRange;
import android.net.wifi.HuaweiApConfiguration;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.provider.SettingsEx.Systemex;
import android.text.TextUtils;
import android.util.Slog;
import com.android.server.NativeDaemonConnector.Command;
import com.android.server.NativeDaemonConnector.SensitiveArg;
import com.android.server.display.Utils;
import com.android.server.rms.iaware.cpu.CPUFeature;
import com.android.server.rms.iaware.dev.SceneInfo;
import com.android.server.security.securitydiagnose.HwSecDiagnoseConstant;
import com.android.server.wifipro.WifiProCHRManager;
import com.android.server.wifipro.WifiProCommonUtils;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HwNetworkManagementService extends NetworkManagementService {
    private static final String ACTION_WIFI_AP_STA_JOIN = "android.net.wifi.WIFI_AP_STA_JOIN";
    private static final String ACTION_WIFI_AP_STA_LEAVE = "android.net.wifi.WIFI_AP_STA_LEAVE";
    private static final String AD_APKDL_STRATEGY = "com.huawei.permission.AD_APKDL_STRATEGY";
    private static final String AD_APKDL_STRATEGY_PERMISSION = "com.huawei.permission.AD_APKDL_STRATEGY";
    private static final int AD_STRATEGY = 0;
    private static final int APK_CONTROL_STRATEGY = 2;
    private static final int APK_DL_STRATEGY = 1;
    private static final String ARG_ADD = "add";
    private static final String ARG_CLEAR = "clear";
    private static final String ARG_IP_WHITELIST = "ipwhitelist";
    private static final String ARG_SET = "set";
    private static final String CHR_BROADCAST_PERMISSION = "com.huawei.android.permission.GET_CHR_DATA";
    private static final String CMD_NET_FILTER = "net_filter";
    private static final int CODE_AD_DEBUG = 1019;
    private static final int CODE_CLEAN_AD_STRATEGY = 1018;
    private static final int CODE_CLEAR_AD_APKDL_STRATEGY = 1103;
    private static final int CODE_CLOSE_SOCKETS_FOR_UID = 1107;
    private static final int CODE_GET_AD_KEY_LIST = 1016;
    private static final int CODE_GET_APLINKED_STA_LIST = 1005;
    private static final int CODE_GET_WIFI_DNS_STAT = 1011;
    private static final int CODE_PRINT_AD_APKDL_STRATEGY = 1104;
    private static final int CODE_REMOVE_LEGACYROUTE_TO_HOST = 1015;
    private static final int CODE_SET_AD_STRATEGY = 1017;
    private static final int CODE_SET_AD_STRATEGY_RULE = 1101;
    private static final int CODE_SET_APK_CONTROL_STRATEGY = 1109;
    private static final int CODE_SET_APK_DL_STRATEGY = 1102;
    private static final int CODE_SET_APK_DL_URL_USER_RESULT = 1105;
    private static final int CODE_SET_AP_CONFIGRATION_HW = 1008;
    private static final int CODE_SET_CHR_REPORT_APP_LIST = 1108;
    private static final int CODE_SET_NETWORK_ACCESS_WHITELIST = 1106;
    private static final int CODE_SET_SOFTAP_DISASSOCIATESTA = 1007;
    private static final int CODE_SET_SOFTAP_MACFILTER = 1006;
    private static final int CODE_SET_SOFTAP_TX_POWER = 1009;
    private static final int DEFAULT_ISMCOEX_WIFI_AP_CHANNEL = 11;
    private static final int DEFAULT_WIFI_AP_CHANNEL = 0;
    private static final int DEFAULT_WIFI_AP_MAXSCB = 8;
    private static final int DEFAULT_WIFI_AP_MAX_CONNECTIONS = 8;
    private static final String DESCRIPTOR = "android.net.wifi.INetworkManager";
    private static final String DESCRIPTOR_ADCLEANER_MANAGER_Ex = "android.os.AdCleanerManagerEx";
    private static final String DESCRIPTOR_HW_AD_CLEANER = "android.view.HwAdCleaner";
    private static final String DESCRIPTOR_NETWORKMANAGEMENT_SERVICE = "android.os.INetworkManagementService";
    private static final String EVENT_KEY = "event_key";
    private static final String EXTRA_CURRENT_TIME = "currentTime";
    private static final String EXTRA_STA_COUNT = "staCount";
    private static final String EXTRA_STA_INFO = "macInfo";
    private static final String HEX_STR = "0123456789ABCDEF";
    private static final int HSM_TRANSACT_CODE = 201;
    private static final String INTENT_APKDL_URL_DETECTED = "com.android.intent.action.apkdl_url_detected";
    private static final String INTENT_DS_WIFI_WEB_STAT_REPORT = "com.huawei.chr.wifi.action.web_stat_report";
    private static final String ISM_COEX_ON = "ro.config.hw_ismcoex";
    private static final String MAC_KEY = "mac_key";
    private static final int PER_STRATEGY_SIZE = 470;
    private static final int PER_UID_LIST_SIZE = 50;
    private static final String STA_JOIN_EVENT = "STA_JOIN";
    private static final int STA_JOIN_HANDLE_DELAY = 5000;
    private static final String STA_LEAVE_EVENT = "STA_LEAVE";
    private static final String TAG = HwNetworkManagementService.class.getSimpleName();
    private static final int WEB_STAT = 0;
    private static final int WIFI_STAT_DELTA = 190;
    private Map<String, List<String>> mAdIdMap = new HashMap();
    private Map<String, List<String>> mAdViewMap = new HashMap();
    private Handler mApLinkedStaHandler = new Handler() {
        public void handleMessage(Message msg) {
            String action = null;
            long mCurrentTime = System.currentTimeMillis();
            Bundle bundle = msg.getData();
            String event = bundle.getString(HwNetworkManagementService.EVENT_KEY);
            String macStr = bundle.getString(HwNetworkManagementService.MAC_KEY).toLowerCase();
            HwNetworkManagementService hwNetworkManagementService;
            if (HwNetworkManagementService.STA_JOIN_EVENT.equals(event)) {
                action = HwNetworkManagementService.ACTION_WIFI_AP_STA_JOIN;
                if (HwNetworkManagementService.this.mMacList.contains(macStr)) {
                    Slog.e(HwNetworkManagementService.TAG, macStr + " had been added, but still get event " + event);
                } else {
                    HwNetworkManagementService.this.mMacList.add(macStr);
                    hwNetworkManagementService = HwNetworkManagementService.this;
                    hwNetworkManagementService.mLinkedStaCount = hwNetworkManagementService.mLinkedStaCount + 1;
                }
            } else if (HwNetworkManagementService.STA_LEAVE_EVENT.equals(event)) {
                action = HwNetworkManagementService.ACTION_WIFI_AP_STA_LEAVE;
                if (HwNetworkManagementService.this.mApLinkedStaHandler.hasMessages(msg.what)) {
                    HwNetworkManagementService.this.mApLinkedStaHandler.removeMessages(msg.what);
                    Slog.d(HwNetworkManagementService.TAG, "event=" + event + ", remove STA_JOIN message");
                } else if (HwNetworkManagementService.this.mMacList.contains(macStr)) {
                    HwNetworkManagementService.this.mMacList.remove(macStr);
                    hwNetworkManagementService = HwNetworkManagementService.this;
                    hwNetworkManagementService.mLinkedStaCount = hwNetworkManagementService.mLinkedStaCount - 1;
                } else {
                    Slog.e(HwNetworkManagementService.TAG, macStr + " had been removed, but still get event " + event);
                }
            }
            Slog.d(HwNetworkManagementService.TAG, "handle " + event + " event, mLinkedStaCount=" + HwNetworkManagementService.this.mLinkedStaCount);
            if (HwNetworkManagementService.this.mLinkedStaCount < 0 || HwNetworkManagementService.this.mLinkedStaCount > 8 || HwNetworkManagementService.this.mLinkedStaCount != HwNetworkManagementService.this.mMacList.size()) {
                Slog.e(HwNetworkManagementService.TAG, "mLinkedStaCount over flow, need synchronize. value = " + HwNetworkManagementService.this.mLinkedStaCount);
                try {
                    String[] macList = HwNetworkManagementService.this.mConnector.doListCommand("softap", 121, new Object[]{"assoclist"});
                    HwNetworkManagementService.this.mMacList = new ArrayList();
                    if (macList == null) {
                        HwNetworkManagementService.this.mLinkedStaCount = 0;
                    } else {
                        for (String mac : macList) {
                            if (mac == null) {
                                Slog.e(HwNetworkManagementService.TAG, "get mac from macList is null");
                            } else {
                                HwNetworkManagementService.this.mMacList.add(mac.toLowerCase());
                            }
                        }
                        HwNetworkManagementService.this.mLinkedStaCount = HwNetworkManagementService.this.mMacList.size();
                    }
                } catch (NativeDaemonConnectorException e) {
                    Slog.e(HwNetworkManagementService.TAG, "Cannot communicate with native daemon to get linked stations list");
                    HwNetworkManagementService.this.mMacList = new ArrayList();
                    HwNetworkManagementService.this.mLinkedStaCount = 0;
                }
            }
            Slog.e(HwNetworkManagementService.TAG, "send broadcast, event=" + event + ", extraInfo: " + String.format("MAC=%s TIME=%d STACNT=%d", new Object[]{macStr, Long.valueOf(mCurrentTime), Integer.valueOf(HwNetworkManagementService.this.mLinkedStaCount)}));
            Intent broadcast = new Intent(action);
            broadcast.putExtra(HwNetworkManagementService.EXTRA_STA_INFO, macStr);
            broadcast.putExtra(HwNetworkManagementService.EXTRA_CURRENT_TIME, mCurrentTime);
            broadcast.putExtra(HwNetworkManagementService.EXTRA_STA_COUNT, HwNetworkManagementService.this.mLinkedStaCount);
            HwNetworkManagementService.this.mContext.sendBroadcast(broadcast, "android.permission.ACCESS_WIFI_STATE");
        }
    };
    private int mChannel;
    private AtomicInteger mCmdId;
    private NativeDaemonConnector mConnector;
    private Context mContext;
    private HuaweiApConfiguration mHwApConfig;
    private int mLinkedStaCount = 0;
    private List<String> mMacList = new ArrayList();
    private String mSoftapIface;
    private String mWlanIface;
    private Pattern p = Pattern.compile("^.*max=([0-9]+);idx=([0-9]+);(.*)$");
    private HashMap<String, Long> startTimeMap = new HashMap();
    private StringBuffer urlBuffer = new StringBuffer();

    static class NetdResponseCode {
        public static final int ApLinkedStaListChangeHISI = 651;
        public static final int ApLinkedStaListChangeQCOM = 901;
        public static final int HwDnsStat = 130;
        public static final int SoftapDhcpListResult = 122;
        public static final int SoftapListResult = 121;

        NetdResponseCode() {
        }
    }

    public HwNetworkManagementService(Context context, String socket) {
        super(context, socket);
        this.mContext = context;
        this.mCmdId = new AtomicInteger(0);
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        boolean needReset;
        int size;
        int i;
        String key;
        String[] pkgName;
        ArrayList<String> appUidList;
        int strategy;
        if (code == HSM_TRANSACT_CODE) {
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
        } else if (code == 1017) {
            ArrayList<String> value;
            Slog.d(TAG, "code == CODE_SET_AD_STRATEGY");
            data.enforceInterface(DESCRIPTOR_ADCLEANER_MANAGER_Ex);
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.AD_APKDL_STRATEGY", "permission denied");
            needReset = data.readInt() > 0;
            Slog.d(TAG, "CODE_SET_AD_STRATEGY, needReset: " + needReset);
            if (needReset) {
                this.mAdViewMap.clear();
                this.mAdIdMap.clear();
            }
            size = data.readInt();
            Slog.d(TAG, "CODE_SET_AD_STRATEGY, mAdViewMap size: " + size);
            if (size > 0) {
                for (i = 0; i < size; i++) {
                    key = data.readString();
                    value = data.createStringArrayList();
                    Slog.d(TAG, "CODE_SET_AD_STRATEGY, mAdViewMap key: " + key + ", at " + i);
                    this.mAdViewMap.put(key, value);
                }
            }
            size = data.readInt();
            Slog.d(TAG, "CODE_SET_AD_STRATEGY, mAdIdMap size: " + size);
            if (size > 0) {
                for (i = 0; i < size; i++) {
                    key = data.readString();
                    value = data.createStringArrayList();
                    Slog.d(TAG, "CODE_SET_AD_STRATEGY, mAdIdMap key: " + key + ", at " + i);
                    this.mAdIdMap.put(key, value);
                }
            }
            reply.writeNoException();
            return true;
        } else if (code == 1018) {
            Slog.d(TAG, "code == CODE_CLEAN_AD_STRATEGY");
            data.enforceInterface(DESCRIPTOR_ADCLEANER_MANAGER_Ex);
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.AD_APKDL_STRATEGY", "permission denied");
            int flag = data.readInt();
            Slog.d(TAG, "CODE_CLEAN_AD_STRATEGY, flag: " + flag);
            if (1 == flag) {
                this.mAdViewMap.clear();
                this.mAdIdMap.clear();
            } else if (flag == 0) {
                ArrayList<String> adAppList = data.createStringArrayList();
                Slog.d(TAG, "CODE_CLEAN_AD_STRATEGY adAppList: ");
                if (adAppList != null) {
                    for (i = 0; i < adAppList.size(); i++) {
                        String adAppName = (String) adAppList.get(i);
                        Slog.d(TAG, i + " = " + adAppName);
                        if (this.mAdViewMap.containsKey(adAppName)) {
                            this.mAdViewMap.remove(adAppName);
                        }
                        if (this.mAdIdMap.containsKey(adAppName)) {
                            this.mAdIdMap.remove(adAppName);
                        }
                    }
                }
            }
            reply.writeNoException();
            return true;
        } else if (code == 1016) {
            Slog.d(TAG, "code == CODE_GET_AD_KEY_LIST");
            data.enforceInterface(DESCRIPTOR_HW_AD_CLEANER);
            String appName = data.readString();
            if (appName == null || !this.mAdViewMap.containsKey(appName)) {
                try {
                    reply.writeStringList(new ArrayList());
                    Slog.d(TAG, "appName = " + appName + "  is not in the mAdViewMap! reply none");
                } catch (Exception e) {
                    Slog.d(TAG, "---------err: Exception ");
                    e.printStackTrace();
                }
            } else {
                reply.writeStringList((List) this.mAdViewMap.get(appName));
                Slog.d(TAG, "appName = " + appName + "  is in the mAdViewMap!");
            }
            if (appName == null || !this.mAdIdMap.containsKey(appName)) {
                reply.writeStringList(new ArrayList());
                Slog.d(TAG, "appName = " + appName + "  is not in the mAdIdMap! reply none");
                reply.writeNoException();
                return true;
            }
            reply.writeStringList((List) this.mAdIdMap.get(appName));
            Slog.d(TAG, "appName = " + appName + "  is in the mAdIdMap !");
            reply.writeNoException();
            return true;
        } else if (code == 1019) {
            Set<String> keysSet;
            List<String> keysList;
            List<String> value2;
            Slog.d(TAG, "code == CODE_AD_DEBUG");
            data.enforceInterface(DESCRIPTOR_ADCLEANER_MANAGER_Ex);
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.AD_APKDL_STRATEGY", "permission denied");
            data.readInt();
            int j = 0;
            StringBuffer print = new StringBuffer();
            if (this.mAdViewMap.isEmpty()) {
                print.append("mAdViewMap is empty!");
            } else {
                print.append("\n---------------- mAdViewMap is as followed ---------------\n");
                keysSet = this.mAdViewMap.keySet();
                keysList = new ArrayList();
                for (String keyString : keysSet) {
                    keysList.add(keyString);
                }
                for (i = 0; i < this.mAdViewMap.size(); i++) {
                    key = (String) keysList.get(i);
                    value2 = (List) this.mAdViewMap.get(key);
                    print.append("\n(" + i + ") apkName = " + key + "\n");
                    for (j = 
/*
Method generation error in method: com.android.server.HwNetworkManagementService.onTransact(int, android.os.Parcel, android.os.Parcel, int):boolean, dex: 
jadx.core.utils.exceptions.CodegenException: Error generate insn: PHI: (r15_6 'j' int) = (r15_0 'j' int), (r15_9 'j' int) binds: {(r15_0 'j' int)=B:196:0x057f, (r15_9 'j' int)=B:106:0x057c} in method: com.android.server.HwNetworkManagementService.onTransact(int, android.os.Parcel, android.os.Parcel, int):boolean, dex: 
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:228)
	at jadx.core.codegen.RegionGen.makeLoop(RegionGen.java:183)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:61)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:93)
	at jadx.core.codegen.RegionGen.makeLoop(RegionGen.java:189)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:61)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:93)
	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:128)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:57)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:93)
	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:118)
	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:143)
	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:124)
	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:143)
	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:124)
	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:143)
	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:124)
	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:143)
	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:124)
	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:143)
	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:124)
	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:143)
	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:124)
	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:143)
	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:124)
	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:143)
	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:124)
	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:143)
	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:124)
	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:143)
	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:124)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:57)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:173)
	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:322)
	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:260)
	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:222)
	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:112)
	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:78)
	at jadx.core.codegen.CodeGen.visit(CodeGen.java:10)
	at jadx.core.ProcessClass.process(ProcessClass.java:38)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
Caused by: jadx.core.utils.exceptions.CodegenException: PHI can be used only in fallback mode
	at jadx.core.codegen.InsnGen.fallbackOnlyInsn(InsnGen.java:539)
	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:511)
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:222)
	... 50 more

*/

    private boolean executeHsmCommand(Parcel data, Parcel reply) {
        try {
            String cmd = data.readString();
            Object[] args = data.readArray(null);
            if (this.mConnector != null) {
                int i;
                if (this.mConnector.execute(cmd, args).isClassOk()) {
                    i = 1;
                } else {
                    i = 0;
                }
                reply.writeInt(i);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void setConnector(NativeDaemonConnector connector) {
        this.mConnector = connector;
    }

    private String getChannel(WifiConfiguration wifiConfig) {
        if (wifiConfig.apBand == 0 && SystemProperties.getBoolean(ISM_COEX_ON, false)) {
            this.mChannel = 11;
        } else {
            this.mChannel = Secure.getInt(this.mContext.getContentResolver(), "wifi_ap_channel", 0);
            if (this.mChannel == 0 || ((wifiConfig.apBand == 0 && this.mChannel > 14) || (wifiConfig.apBand == 1 && this.mChannel < 34))) {
                this.mChannel = wifiConfig.apChannel;
            }
        }
        Slog.d(TAG, "channel=" + this.mChannel);
        return String.valueOf(this.mChannel);
    }

    private String getMaxscb() {
        int maxscb = Secure.getInt(this.mContext.getContentResolver(), "wifi_ap_maxscb", 8);
        Slog.d(TAG, "maxscb=" + maxscb);
        return String.valueOf(maxscb);
    }

    private static String getSecurityType(WifiConfiguration wifiConfig) {
        switch (wifiConfig.getAuthType()) {
            case 1:
                return "wpa-psk";
            case 4:
                return "wpa2-psk";
            default:
                return "open";
        }
    }

    public String getIgnorebroadcastssid() {
        String iIgnorebroadcastssidStr = "broadcast";
        if (1 == Systemex.getInt(this.mContext.getContentResolver(), "show_broadcast_ssid_config", 0)) {
            iIgnorebroadcastssidStr = Secure.getInt(this.mContext.getContentResolver(), "wifi_ap_ignorebroadcastssid", 0) == 0 ? "broadcast" : "hidden";
            Slog.d(TAG, "iIgnorebroadcastssidStr=" + iIgnorebroadcastssidStr);
        }
        return iIgnorebroadcastssidStr;
    }

    public void startAccessPointWithChannel(WifiConfiguration wifiConfig, String wlanIface) {
        if (wifiConfig != null) {
            try {
                this.mConnector.execute("softap", new Object[]{ARG_SET, wlanIface, wifiConfig.SSID, getIgnorebroadcastssid(), getChannel(wifiConfig), getSecurityType(wifiConfig), new SensitiveArg(wifiConfig.preSharedKey), getMaxscb()});
                this.mConnector.execute("softap", new Object[]{"startap"});
            } catch (NativeDaemonConnectorException e) {
                throw e.rethrowAsParcelableException();
            }
        }
    }

    public void sendDataSpeedSlowMessage(String[] cooked, String raw) {
        if (cooked.length < 2 || (cooked[1].equals("sourceAddress") ^ 1) != 0) {
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

    public void sendWebStatMessage(String[] cooked, String raw) {
        int idx = 0;
        if (cooked.length < 20 || (cooked[1].equals("ReportType") ^ 1) != 0) {
            throw new IllegalStateException(String.format("Invalid event from daemon (%s)", new Object[]{raw}));
        }
        try {
            NetworkInfo mobileNetinfo = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getNetworkInfo(0);
            Slog.d(TAG, "onEvent receive Web Stat Report:" + raw);
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
                if (cooked.length > 23) {
                    chrIntent.putExtra("TcpSuccNum", Integer.parseInt(cooked[21]));
                    chrIntent.putExtra("SocketUid", Integer.parseInt(cooked[22]));
                    chrIntent.putExtra("WebFailCode", Integer.parseInt(cooked[23]));
                }
                if (Integer.parseInt(cooked[2]) == 0) {
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
                    chrIntent.putExtra("App5DelayL1", Integer.parseInt(cooked[CPUFeature.MSG_START_BIGDATAPROCRECORD]));
                    chrIntent.putExtra("App5DelayL2", Integer.parseInt(cooked[CPUFeature.MSG_STOP_BIGDATAPROCRECORD]));
                    chrIntent.putExtra("App5DelayL3", Integer.parseInt(cooked[110]));
                    chrIntent.putExtra("App5DelayL4", Integer.parseInt(cooked[111]));
                    chrIntent.putExtra("App5DelayL5", Integer.parseInt(cooked[CPUFeature.MSG_SET_FREQUENCY]));
                    chrIntent.putExtra("App5DelayL6", Integer.parseInt(cooked[CPUFeature.MSG_RESET_FREQUENCY]));
                    chrIntent.putExtra("App5RTTL1", Integer.parseInt(cooked[CPUFeature.MSG_CPUFEATURE_OFF]));
                    chrIntent.putExtra("App5RTTL2", Integer.parseInt(cooked[CPUFeature.MSG_THREAD_BOOST]));
                    chrIntent.putExtra("App5RTTL3", Integer.parseInt(cooked[CPUFeature.MSG_SET_TOP_APP_CPUSET]));
                    chrIntent.putExtra("App5RTTL4", Integer.parseInt(cooked[CPUFeature.MSG_RESET_TOP_APP_CPUSET]));
                    chrIntent.putExtra("App5RTTL5", Integer.parseInt(cooked[118]));
                    chrIntent.putExtra("App6RTT", Integer.parseInt(cooked[119]));
                    chrIntent.putExtra("App6WebDelay", Integer.parseInt(cooked[WifiProCHRManager.WIFI_PORTAL_SAMPLES_COLLECTE]));
                    chrIntent.putExtra("App6SuccNum", Integer.parseInt(cooked[121]));
                    chrIntent.putExtra("App6FailNum", Integer.parseInt(cooked[122]));
                    chrIntent.putExtra("App6NoAckNum", Integer.parseInt(cooked[123]));
                    chrIntent.putExtra("App6TotalNum", Integer.parseInt(cooked[124]));
                    chrIntent.putExtra("App6TcpTotalNum", Integer.parseInt(cooked[CPUFeature.MSG_SET_CPUSETCONFIG_VR]));
                    chrIntent.putExtra("App6TcpSuccNum", Integer.parseInt(cooked[CPUFeature.MSG_SET_CPUSETCONFIG_SCREENON]));
                    chrIntent.putExtra("App6DelayL1", Integer.parseInt(cooked[127]));
                    chrIntent.putExtra("App6DelayL2", Integer.parseInt(cooked[128]));
                    chrIntent.putExtra("App6DelayL3", Integer.parseInt(cooked[CPUFeature.MSG_SET_INTERACTIVE_SAVE]));
                    chrIntent.putExtra("App6DelayL4", Integer.parseInt(cooked[130]));
                    chrIntent.putExtra("App6DelayL5", Integer.parseInt(cooked[CPUFeature.MSG_SET_INTERACTIVE_SPSAVE]));
                    chrIntent.putExtra("App6DelayL6", Integer.parseInt(cooked[132]));
                    chrIntent.putExtra("App6RTTL1", Integer.parseInt(cooked[133]));
                    chrIntent.putExtra("App6RTTL2", Integer.parseInt(cooked[134]));
                    chrIntent.putExtra("App6RTTL3", Integer.parseInt(cooked[CPUFeature.MSG_SET_BOOST_CPUS]));
                    chrIntent.putExtra("App6RTTL4", Integer.parseInt(cooked[CPUFeature.MSG_RESET_BOOST_CPUS]));
                    chrIntent.putExtra("App6RTTL5", Integer.parseInt(cooked[CPUFeature.MSG_SET_LIMIT_CGROUP]));
                    chrIntent.putExtra("App7RTT", Integer.parseInt(cooked[138]));
                    chrIntent.putExtra("App7WebDelay", Integer.parseInt(cooked[CPUFeature.MSG_SET_FG_CGROUP]));
                    chrIntent.putExtra("App7SuccNum", Integer.parseInt(cooked[CPUFeature.MSG_CPUCTL_SUBSWITCH]));
                    chrIntent.putExtra("App7FailNum", Integer.parseInt(cooked[CPUFeature.MSG_SET_FG_UIDS]));
                    chrIntent.putExtra("App7NoAckNum", Integer.parseInt(cooked[CPUFeature.MSG_SET_BG_UIDS]));
                    chrIntent.putExtra("App7TotalNum", Integer.parseInt(cooked[CPUFeature.MSG_SET_VIP_THREAD]));
                    chrIntent.putExtra("App7TcpTotalNum", Integer.parseInt(cooked[CPUFeature.MSG_RESET_VIP_THREAD]));
                    chrIntent.putExtra("App7TcpSuccNum", Integer.parseInt(cooked[CPUFeature.MSG_ENABLE_EAS]));
                    chrIntent.putExtra("App7DelayL1", Integer.parseInt(cooked[CPUFeature.MSG_SET_THREAD_TO_TA]));
                    chrIntent.putExtra("App7DelayL2", Integer.parseInt(cooked[CPUFeature.MSG_ENTER_GAME_SCENE]));
                    chrIntent.putExtra("App7DelayL3", Integer.parseInt(cooked[CPUFeature.MSG_EXIT_GAME_SCENE]));
                    chrIntent.putExtra("App7DelayL4", Integer.parseInt(cooked[149]));
                    chrIntent.putExtra("App7DelayL5", Integer.parseInt(cooked[CPUFeature.MSG_SET_VIP_THREAD_PARAMS]));
                    chrIntent.putExtra("App7DelayL6", Integer.parseInt(cooked[CPUFeature.MSG_BINDER_THREAD_CREATE]));
                    chrIntent.putExtra("App7RTTL1", Integer.parseInt(cooked[152]));
                    chrIntent.putExtra("App7RTTL2", Integer.parseInt(cooked[153]));
                    chrIntent.putExtra("App7RTTL3", Integer.parseInt(cooked[CPUFeature.MSG_RESET_ON_FIRE]));
                    chrIntent.putExtra("App7RTTL4", Integer.parseInt(cooked[155]));
                    chrIntent.putExtra("App7RTTL5", Integer.parseInt(cooked[156]));
                    chrIntent.putExtra("App8RTT", Integer.parseInt(cooked[CPUFeature.MSG_GAME_SCENE_LEVEL]));
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
                    chrIntent.putExtra("App8DelayL5", Integer.parseInt(cooked[HwSecDiagnoseConstant.OEMINFO_ID_ANTIMAL]));
                    chrIntent.putExtra("App8DelayL6", Integer.parseInt(cooked[HwSecDiagnoseConstant.OEMINFO_ID_DEVICE_RENEW]));
                    chrIntent.putExtra("App8RTTL1", Integer.parseInt(cooked[HwSecDiagnoseConstant.OEMINFO_ID_ROOT_CHECK]));
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
                    chrIntent.putExtra("App9RTTL1", Integer.parseInt(cooked[WIFI_STAT_DELTA]));
                    chrIntent.putExtra("App9RTTL2", Integer.parseInt(cooked[Utils.DEFAULT_COLOR_TEMPERATURE_SUNNY]));
                    chrIntent.putExtra("App9RTTL3", Integer.parseInt(cooked[192]));
                    chrIntent.putExtra("App9RTTL4", Integer.parseInt(cooked[193]));
                    chrIntent.putExtra("App9RTTL5", Integer.parseInt(cooked[194]));
                    chrIntent.putExtra("App10RTT", Integer.parseInt(cooked[195]));
                    chrIntent.putExtra("App10WebDelay", Integer.parseInt(cooked[196]));
                    chrIntent.putExtra("App10SuccNum", Integer.parseInt(cooked[197]));
                    chrIntent.putExtra("App10FailNum", Integer.parseInt(cooked[198]));
                    chrIntent.putExtra("App10NoAckNum", Integer.parseInt(cooked[199]));
                    chrIntent.putExtra("App10TotalNum", Integer.parseInt(cooked[200]));
                    chrIntent.putExtra("App10TcpTotalNum", Integer.parseInt(cooked[HSM_TRANSACT_CODE]));
                    chrIntent.putExtra("App10TcpSuccNum", Integer.parseInt(cooked[202]));
                    chrIntent.putExtra("App10DelayL1", Integer.parseInt(cooked[203]));
                    chrIntent.putExtra("App10DelayL2", Integer.parseInt(cooked[WifiProCommonUtils.HTTP_REACHALBE_GOOLE]));
                    chrIntent.putExtra("App10DelayL3", Integer.parseInt(cooked[205]));
                    chrIntent.putExtra("App10DelayL4", Integer.parseInt(cooked[206]));
                    chrIntent.putExtra("App10DelayL5", Integer.parseInt(cooked[207]));
                    chrIntent.putExtra("App10DelayL6", Integer.parseInt(cooked[208]));
                    chrIntent.putExtra("App10RTTL1", Integer.parseInt(cooked[209]));
                    chrIntent.putExtra("App10RTTL2", Integer.parseInt(cooked[210]));
                    chrIntent.putExtra("App10RTTL3", Integer.parseInt(cooked[211]));
                    chrIntent.putExtra("App10RTTL4", Integer.parseInt(cooked[212]));
                    chrIntent.putExtra("App10RTTL5", Integer.parseInt(cooked[213]));
                }
                this.mContext.sendBroadcast(chrIntent, CHR_BROADCAST_PERMISSION);
            }
            if (Integer.parseInt(cooked[2]) == 0) {
                idx = WIFI_STAT_DELTA;
            }
            if (Integer.parseInt(cooked[idx + 27]) > 0) {
                Intent wifichrIntent = new Intent(INTENT_DS_WIFI_WEB_STAT_REPORT);
                wifichrIntent.putExtra("ReportType", Integer.parseInt(cooked[idx + 25]));
                wifichrIntent.putExtra("RTT", Integer.parseInt(cooked[idx + 26]));
                wifichrIntent.putExtra("WebDelay", Integer.parseInt(cooked[idx + 27]));
                wifichrIntent.putExtra("SuccNum", Integer.parseInt(cooked[idx + 28]));
                wifichrIntent.putExtra("FailNum", Integer.parseInt(cooked[idx + 29]));
                wifichrIntent.putExtra("NoAckNum", Integer.parseInt(cooked[idx + 30]));
                wifichrIntent.putExtra("TotalNum", Integer.parseInt(cooked[idx + 31]));
                wifichrIntent.putExtra("TcpTotalNum", Integer.parseInt(cooked[idx + 32]));
                wifichrIntent.putExtra("DelayL1", Integer.parseInt(cooked[idx + 33]));
                wifichrIntent.putExtra("DelayL2", Integer.parseInt(cooked[idx + 34]));
                wifichrIntent.putExtra("DelayL3", Integer.parseInt(cooked[idx + 35]));
                wifichrIntent.putExtra("DelayL4", Integer.parseInt(cooked[idx + 36]));
                wifichrIntent.putExtra("DelayL5", Integer.parseInt(cooked[idx + 37]));
                wifichrIntent.putExtra("DelayL6", Integer.parseInt(cooked[idx + 38]));
                wifichrIntent.putExtra("RTTL1", Integer.parseInt(cooked[idx + 39]));
                wifichrIntent.putExtra("RTTL2", Integer.parseInt(cooked[idx + 40]));
                wifichrIntent.putExtra("RTTL3", Integer.parseInt(cooked[idx + 41]));
                wifichrIntent.putExtra("RTTL4", Integer.parseInt(cooked[idx + 42]));
                wifichrIntent.putExtra("RTTL5", Integer.parseInt(cooked[idx + 43]));
                this.mContext.sendBroadcast(wifichrIntent);
            }
        } catch (Exception e) {
            Slog.e(TAG, "Web Stat Report Send Broadcast Fail.");
        }
    }

    public boolean handleApLinkedStaListChange(String raw, String[] cooked) {
        Slog.d(TAG, "handleApLinkedStaListChange is called");
        if (STA_JOIN_EVENT.equals(cooked[1]) || STA_LEAVE_EVENT.equals(cooked[1])) {
            Slog.d(TAG, "Got sta list change event:" + cooked[1]);
            notifyApLinkedStaListChange(cooked[1], cooked[4]);
            return true;
        }
        throw new IllegalStateException(String.format("ApLinkedStaListChange: Invalid event from daemon (%s)", new Object[]{raw}));
    }

    public List<String> getApLinkedStaList() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        List<String> mDhcpList = getApLinkedDhcpList();
        Slog.d(TAG, "getApLinkedStaList: softap assoclist");
        List<String> infoList = new ArrayList();
        for (int index = 0; index < this.mMacList.size(); index++) {
            String mac = getApLinkedStaInfo((String) this.mMacList.get(index), mDhcpList);
            Slog.d(TAG, "getApLinkedStaList ApLinkedStaInfo = " + mac);
            infoList.add(mac);
        }
        Slog.d(TAG, "getApLinkedStaList, info size=" + infoList.size());
        return infoList;
    }

    public void setSoftapMacFilter(String macFilter) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            Slog.d(TAG, "setSoftapMacFilter:" + String.format("softap setmacfilter " + macFilter, new Object[0]));
            this.mConnector.doCommand("softap", new Object[]{"setmacfilter", macFilter});
        } catch (NativeDaemonConnectorException e) {
            throw new IllegalStateException("Cannot communicate with native daemon to set MAC Filter");
        }
    }

    public void setSoftapDisassociateSta(String mac) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            Slog.d(TAG, "setSoftapDisassociateSta:" + String.format("softap disassociatesta " + mac, new Object[0]));
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
        hwApConfig.maxScb = Secure.getInt(this.mContext.getContentResolver(), "wifi_ap_maxscb", 8);
        try {
            String str = String.format("softap sethw " + wlanIface + " " + softapIface + " %d %d", new Object[]{Integer.valueOf(hwApConfig.channel), Integer.valueOf(hwApConfig.maxScb)});
            this.mConnector.doCommand("softap", new Object[]{"sethw", wlanIface, softapIface, String.valueOf(hwApConfig.channel), String.valueOf(hwApConfig.maxScb)});
            Slog.d(TAG, "setAccessPointHw command: " + str);
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
            List<String> mDhcpList = new ArrayList();
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
        String ApLinkedStaInfo = String.format("MAC=%s", new Object[]{mac});
        mac = mac.toLowerCase();
        if (mDhcpList != null) {
            for (String dhcplease : mDhcpList) {
                if (dhcplease.contains(mac)) {
                    if (4 <= dhcplease.split(" ").length) {
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
        Slog.d(TAG, "send " + event + " message, mLinkedStaCount=" + this.mLinkedStaCount);
    }

    public void setWifiTxPower(String reduceCmd) {
        Slog.d(TAG, "setWifiTxPower " + reduceCmd);
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
                        buf.append(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
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
            sb.append("0123456789ABCDEF".charAt((bytes[i] & 240) >> 4));
            sb.append("0123456789ABCDEF".charAt((bytes[i] & 15) >> 0));
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
        if (pkgName != null) {
            Slog.d(TAG, "convertPkgNameToUid, pkgName=" + Arrays.asList(pkgName));
        }
        ArrayList<String> uidList = new ArrayList();
        if (pkgName != null && pkgName.length > 0) {
            int userCount = UserManager.get(this.mContext).getUserCount();
            List<UserInfo> users = UserManager.get(this.mContext).getUsers();
            PackageManager pm = this.mContext.getPackageManager();
            StringBuilder appUidBuilder = new StringBuilder();
            int uidCount = 0;
            for (String pkg : pkgName) {
                for (int n = 0; n < userCount; n++) {
                    try {
                        int uid = pm.getPackageUidAsUser(pkg, ((UserInfo) users.get(n)).id);
                        Slog.d(TAG, "convertPkgNameToUid, pkg=" + pkg + ", uid=" + uid + ", under user.id=" + ((UserInfo) users.get(n)).id);
                        uidCount++;
                        if (uidCount % 50 == 0) {
                            appUidBuilder.append(uid);
                            appUidBuilder.append(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
                            uidList.add(appUidBuilder.toString());
                            appUidBuilder = new StringBuilder();
                        } else {
                            appUidBuilder.append(uid);
                            appUidBuilder.append(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
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

    private void setAdFilterRules(String adStrategy, boolean needReset) {
        Slog.d(TAG, "setAdFilterRules, adStrategy=" + adStrategy + ", needReset=" + needReset);
        String operation = needReset ? "reset" : "not_reset";
        int count = 0;
        int strategyLen = 0;
        if (adStrategy != null) {
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
            this.mConnector.execute("hwfilter", new Object[]{"set_ad_strategy_rule", operation, Integer.valueOf(cmdId), Integer.valueOf(count)});
            if (strategyLen == 0) {
                Slog.d(TAG, "setAdFilterRules, adStrategy is null!");
                return;
            }
            int i = 1;
            while (adStrategy.length() > 0) {
                if (adStrategy.length() > PER_STRATEGY_SIZE) {
                    Slog.d(TAG, "setAdFilterRules, adStrategy len=" + adStrategy.substring(0, PER_STRATEGY_SIZE).length() + ", seq=" + i + ", cmdId=" + cmdId);
                    this.mConnector.execute("hwfilter", new Object[]{"set_ad_strategy_buf", Integer.valueOf(cmdId), Integer.valueOf(i), adStrategyTmp});
                    adStrategy = adStrategy.substring(PER_STRATEGY_SIZE);
                    i++;
                } else {
                    Slog.d(TAG, "setAdFilterRules, adStrategy len=" + adStrategy.length() + ", seq=" + i + ", cmdId=" + cmdId);
                    this.mConnector.execute("hwfilter", new Object[]{"set_ad_strategy_buf", Integer.valueOf(cmdId), Integer.valueOf(i), adStrategy});
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
        int i;
        if (strategy == 0) {
            if (appUidList != null) {
                try {
                    if (appUidList.size() > 0) {
                        for (i = 0; i < appUidList.size(); i++) {
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
            for (i = 0; i < appUidList.size(); i++) {
                if (i == 0) {
                    this.mConnector.execute("hwfilter", new Object[]{"clear_apkdl_strategy_rule", appUidList.get(i), operation});
                } else {
                    this.mConnector.execute("hwfilter", new Object[]{"clear_apkdl_strategy_rule", appUidList.get(i), "not_reset"});
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
            for (i = 0; i < appUidList_size; i++) {
                if (i == 0) {
                    Slog.d(TAG, "clearApkDlFilterRules 0==i netd clear_delta_install_rule");
                    this.mConnector.execute("hwfilter", new Object[]{"clear_delta_install_rule", appUidList.get(i), operation});
                } else {
                    Slog.d(TAG, "clearApkDlFilterRules 0!=i netd clear_delta_install_rule");
                    this.mConnector.execute("hwfilter", new Object[]{"clear_delta_install_rule", appUidList.get(i), "not_reset"});
                }
            }
        }
    }

    private void printAdOrApkDlFilterRules(int strategy) {
        Slog.d(TAG, "printAdOrApkDlFilterRules, strategy=" + strategy);
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
        Slog.d(TAG, "setApkDlUrlUserResult, downloadId=" + downloadId + ", result=" + result);
        String operation = result ? SceneInfo.ITEM_RULE_ALLOW : "reject";
        try {
            this.mConnector.execute("hwfilter", new Object[]{"apkdl_callback", downloadId, operation});
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    private String getStrategyStr(int code, int size, Parcel data) {
        if (size <= 0) {
            return null;
        }
        int userCount = UserManager.get(this.mContext).getUserCount();
        List<UserInfo> users = UserManager.get(this.mContext).getUsers();
        PackageManager pm = this.mContext.getPackageManager();
        StringBuilder StrategyBuilder = new StringBuilder();
        int i = 0;
        while (i < size) {
            String key = data.readString();
            ArrayList<String> value = data.createStringArrayList();
            i++;
            if (!TextUtils.isEmpty(key) && value != null && value.size() != 0) {
                StringBuilder tmpUrlBuilder = new StringBuilder();
                String tmpUrlStr = null;
                for (int n = 0; n < userCount; n++) {
                    try {
                        int uid = pm.getPackageUidAsUser(key, ((UserInfo) users.get(n)).id);
                        if (CODE_SET_AD_STRATEGY_RULE == code) {
                            Slog.d(TAG, "CODE_SET_AD_STRATEGY_RULE, adStrategy pkgName=" + key + ", uid=" + uid + ", under user.id=" + ((UserInfo) users.get(n)).id);
                        } else if (CODE_SET_APK_CONTROL_STRATEGY == code) {
                            Slog.d(TAG, "CODE_SET_APK_CONTROL_STRATEGY, apkStrategy pkgName=" + key + ", uid=" + uid + ", under user.id=" + ((UserInfo) users.get(n)).id);
                        }
                        StrategyBuilder.append(uid).append(":");
                        if (tmpUrlStr == null) {
                            int count = 0;
                            int value_size = value.size();
                            for (int m = 0; m < value_size; m++) {
                                tmpUrlBuilder.append(strToHexStr((String) value.get(m)));
                                count++;
                                if (count < value.size()) {
                                    tmpUrlBuilder.append(",");
                                } else {
                                    tmpUrlBuilder.append(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
                                }
                            }
                            tmpUrlStr = tmpUrlBuilder.toString();
                        }
                        StrategyBuilder.append(tmpUrlStr);
                    } catch (Exception e) {
                        if (CODE_SET_AD_STRATEGY_RULE == code) {
                            Slog.e(TAG, "CODE_SET_AD_STRATEGY_RULE, skip unknown packages!");
                        } else if (CODE_SET_APK_CONTROL_STRATEGY == code) {
                            Slog.e(TAG, "CODE_SET_APK_CONTROL_STRATEGY, skip unknown packages!");
                        }
                    }
                }
            } else if (CODE_SET_AD_STRATEGY_RULE == code) {
                Slog.e(TAG, "CODE_SET_AD_STRATEGY_RULE, skip empty key or value!");
            } else if (CODE_SET_APK_CONTROL_STRATEGY == code) {
                Slog.e(TAG, "CODE_SET_APK_CONTROL_STRATEGY, skip empty key or value!");
            }
        }
        return StrategyBuilder.toString();
    }

    private void setApkControlFilterRules(String apkStrategy, boolean needReset) {
        Slog.d(TAG, "setApkControlFilterRules, apkStrategy=" + apkStrategy + ", needReset=" + needReset);
        String operation = needReset ? "reset" : "not_reset";
        int count = 0;
        int strategyLen = 0;
        if (apkStrategy != null) {
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
            this.mConnector.execute("hwfilter", new Object[]{"set_delta_install_rule", operation, Integer.valueOf(cmdId), Integer.valueOf(count)});
            if (strategyLen == 0) {
                Slog.d(TAG, "setApkControlFilterRules, apkStrategy is null!");
                return;
            }
            int i = 1;
            while (apkStrategy.length() > 0) {
                if (apkStrategy.length() > PER_STRATEGY_SIZE) {
                    Slog.d(TAG, "setApkControlFilterRules, apkStrategy len=" + apkStrategy.substring(0, PER_STRATEGY_SIZE).length() + ", seq=" + i + ", cmdId=" + cmdId);
                    this.mConnector.execute("hwfilter", new Object[]{"set_delta_install_buf", Integer.valueOf(cmdId), Integer.valueOf(i), apkStrategyTmp});
                    apkStrategy = apkStrategy.substring(PER_STRATEGY_SIZE);
                    i++;
                } else {
                    Slog.d(TAG, "setApkFilterRules, apkStrategy len=" + apkStrategy.length() + ", seq=" + i + ", cmdId=" + cmdId);
                    this.mConnector.execute("hwfilter", new Object[]{"set_delta_install_buf", Integer.valueOf(cmdId), Integer.valueOf(i), apkStrategy});
                    return;
                }
            }
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void sendApkDownloadUrlBroadcast(String[] cooked, String raw) {
        Slog.d(TAG, "receive report_apkdl_event, raw=" + raw);
        if (cooked.length < 4) {
            String errorMessage = String.format("Invalid event from daemon (%s)", new Object[]{raw});
            Slog.d(TAG, "receive report_apkdl_event, return error");
            throw new IllegalStateException(errorMessage);
        }
        long startTime = SystemClock.elapsedRealtime();
        String downloadId = cooked[1];
        String uid = cooked[2];
        if (!this.startTimeMap.containsKey(downloadId)) {
            this.startTimeMap.put(downloadId, Long.valueOf(startTime));
        }
        Matcher m = this.p.matcher(cooked[3]);
        String url;
        Intent intent;
        if (!m.matches() || m.groupCount() < 3) {
            url = hexStrToStr(cooked[3]);
            Slog.d(TAG, "onEvent receive report_apkdl_event, startTime=" + startTime + ", downloadId=" + downloadId + ", uid=" + uid + ", url=" + url);
            intent = new Intent(INTENT_APKDL_URL_DETECTED);
            intent.putExtra("startTime", startTime);
            intent.putExtra("downloadId", downloadId);
            intent.putExtra("uid", uid);
            intent.putExtra("url", url);
            this.mContext.sendBroadcast(intent, "com.huawei.permission.AD_APKDL_STRATEGY");
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
            url = hexStrToStr(this.urlBuffer.toString());
            Slog.d(TAG, "onEvent receive report_apkdl_event, startTime=" + startTime + ", downloadId=" + downloadId + ", uid=" + uid + ", url=" + url);
            intent = new Intent(INTENT_APKDL_URL_DETECTED);
            intent.putExtra("startTime", (Serializable) this.startTimeMap.get(downloadId));
            intent.putExtra("downloadId", downloadId);
            intent.putExtra("uid", uid);
            intent.putExtra("url", url);
            this.mContext.sendBroadcast(intent, "com.huawei.permission.AD_APKDL_STRATEGY");
        }
    }

    public void systemReady() {
        super.systemReady();
        initNetworkAccessWhitelist();
    }

    private void initNetworkAccessWhitelist() {
        final List<String> networkAccessWhitelist = HwDeviceManager.getList(9);
        if (networkAccessWhitelist != null && (networkAccessWhitelist.isEmpty() ^ 1) != 0) {
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
                    Slog.d(TAG, "set ipwhitelist:" + ((String) this.mConnector.doCommand(CMD_NET_FILTER, new Object[]{ARG_IP_WHITELIST, ARG_SET, addrList.get(0)}).get(0)));
                    for (int i = 1; i < size; i++) {
                        Slog.d(TAG, "add ipwhitelist:" + ((String) this.mConnector.doCommand(CMD_NET_FILTER, new Object[]{ARG_IP_WHITELIST, ARG_ADD, addrList.get(i)}).get(0)));
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
        Slog.d(TAG, "clear ipwhitelist:" + ((String) this.mConnector.doCommand(CMD_NET_FILTER, new Object[]{ARG_IP_WHITELIST, ARG_CLEAR}).get(0)));
    }

    private void removeLegacyRouteForNetId(int netId, RouteInfo routeInfo, int uid) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        Command cmd = new Command("network", new Object[]{"route", "legacy", Integer.valueOf(uid), "remove", Integer.valueOf(netId)});
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

    /* JADX WARNING: Removed duplicated region for block: B:4:0x0013 A:{Splitter: B:1:0x000d, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:4:0x0013, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:5:0x0014, code:
            android.util.Slog.e(TAG, "Error closing sockets for uid " + r8 + ": " + r0);
     */
    /* JADX WARNING: Missing block: B:6:0x0038, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean closeSocketsForUid(int uid) {
        try {
            this.mNetdService.socketDestroy(new UidRange[]{new UidRange(uid, uid)}, new int[0]);
            return true;
        } catch (Exception e) {
        }
    }

    private void setChrReportUid(int index, int uid) {
        try {
            Slog.d(TAG, "chr appuid set:" + ((String) this.mConnector.doCommand("chr", new Object[]{"appuid", ARG_SET, Integer.valueOf(index), Integer.valueOf(uid)}).get(0)));
        } catch (NullPointerException npe) {
            Slog.e(TAG, "runChrCmd:", npe);
        } catch (NativeDaemonConnectorException nde) {
            Slog.e(TAG, "runChrCmd:", nde);
        }
    }
}
