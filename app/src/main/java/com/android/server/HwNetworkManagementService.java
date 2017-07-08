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
import android.net.wifi.HuaweiApConfiguration;
import android.net.wifi.WifiConfiguration;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.Slog;
import com.android.server.NativeDaemonConnector.Command;
import com.android.server.NativeDaemonConnector.SensitiveArg;
import com.android.server.rms.iaware.cpu.CPUFeature;
import com.android.server.wifipro.WifiProCHRManager;
import huawei.com.android.server.policy.HwGlobalActionsData;
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
    private static final int APK_DL_STRATEGY = 1;
    private static final String CHR_BROADCAST_PERMISSION = "com.huawei.android.permission.GET_CHR_DATA";
    private static final int CODE_AD_DEBUG = 1019;
    private static final int CODE_CLEAN_AD_STRATEGY = 1018;
    private static final int CODE_CLEAR_AD_APKDL_STRATEGY = 1103;
    private static final int CODE_GET_AD_KEY_LIST = 1016;
    private static final int CODE_GET_APLINKED_STA_LIST = 1005;
    private static final int CODE_GET_WIFI_DNS_STAT = 1011;
    private static final int CODE_PRINT_AD_APKDL_STRATEGY = 1104;
    private static final int CODE_REMOVE_LEGACYROUTE_TO_HOST = 1015;
    private static final int CODE_SET_AD_STRATEGY = 1017;
    private static final int CODE_SET_AD_STRATEGY_RULE = 1101;
    private static final int CODE_SET_APK_DL_STRATEGY = 1102;
    private static final int CODE_SET_APK_DL_URL_USER_RESULT = 1105;
    private static final int CODE_SET_AP_CONFIGRATION_HW = 1008;
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
    private static final String EXTRA_CURRENT_TIME = "currentTime";
    private static final String EXTRA_STA_COUNT = "staCount";
    private static final String EXTRA_STA_INFO = "macInfo";
    private static final String HEX_STR = "0123456789ABCDEF";
    private static final int HSM_TRANSACT_CODE = 201;
    private static final String INTENT_APKDL_URL_DETECTED = "com.android.intent.action.apkdl_url_detected";
    private static final String ISM_COEX_ON = "ro.config.hw_ismcoex";
    private static final int PER_STRATEGY_SIZE = 470;
    private static final int PER_UID_LIST_SIZE = 50;
    private static final String TAG;
    private Map<String, List<String>> mAdIdMap;
    private Map<String, List<String>> mAdViewMap;
    private int mChannel;
    private AtomicInteger mCmdId;
    private NativeDaemonConnector mConnector;
    private Context mContext;
    private HuaweiApConfiguration mHwApConfig;
    private int mLinkedStaCount;
    private String mSoftapIface;
    private String mWlanIface;
    private Pattern p;
    private HashMap<String, Long> startTimeMap;
    private StringBuffer urlBuffer;

    /* renamed from: com.android.server.HwNetworkManagementService.1 */
    class AnonymousClass1 extends Thread {
        final /* synthetic */ List val$networkAccessWhitelist;

        AnonymousClass1(List val$networkAccessWhitelist) {
            this.val$networkAccessWhitelist = val$networkAccessWhitelist;
        }

        public void run() {
            HwNetworkManagementService.this.setNetworkAccessWhitelist(this.val$networkAccessWhitelist);
        }
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

    static {
        TAG = HwNetworkManagementService.class.getSimpleName();
    }

    public HwNetworkManagementService(Context context, String socket) {
        super(context, socket);
        this.mAdViewMap = new HashMap();
        this.mAdIdMap = new HashMap();
        this.mLinkedStaCount = DEFAULT_WIFI_AP_CHANNEL;
        this.startTimeMap = new HashMap();
        this.urlBuffer = new StringBuffer();
        this.p = Pattern.compile("^.*max=([0-9]+);idx=([0-9]+);(.*)$");
        this.mContext = context;
        this.mCmdId = new AtomicInteger(DEFAULT_WIFI_AP_CHANNEL);
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (code == HSM_TRANSACT_CODE) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
            return executeHsmCommand(data, reply);
        } else if (code == CODE_GET_APLINKED_STA_LIST) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
            List<String> result = getApLinkedStaList();
            reply.writeNoException();
            reply.writeStringList(result);
            return true;
        } else if (code == CODE_SET_SOFTAP_MACFILTER) {
            Slog.d(TAG, "code == CODE_SET_SOFTAP_MACFILTER");
            data.enforceInterface(DESCRIPTOR);
            this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
            setSoftapMacFilter(data.readString());
            reply.writeNoException();
            return true;
        } else if (code == CODE_SET_SOFTAP_DISASSOCIATESTA) {
            Slog.d(TAG, "code == CODE_SET_SOFTAP_DISASSOCIATESTA");
            data.enforceInterface(DESCRIPTOR);
            this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
            setSoftapDisassociateSta(data.readString());
            reply.writeNoException();
            return true;
        } else if (code == CODE_SET_AP_CONFIGRATION_HW) {
            Slog.d(TAG, "code == CODE_SET_AP_CONFIGRATION_HW");
            data.enforceInterface(DESCRIPTOR);
            this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
            this.mWlanIface = data.readString();
            this.mSoftapIface = data.readString();
            reply.writeNoException();
            setAccessPointHw(this.mWlanIface, this.mSoftapIface);
            return true;
        } else if (code == CODE_SET_SOFTAP_TX_POWER) {
            Slog.d(TAG, "code == CODE_SET_SOFTAP_TX_POWER");
            data.enforceInterface(DESCRIPTOR);
            this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
            setWifiTxPower(data.readString());
            reply.writeNoException();
            return true;
        } else if (code == CODE_GET_WIFI_DNS_STAT) {
            Slog.d(TAG, "code == CODE_GET_WIFI_DNS_STAT");
            data.enforceInterface(DESCRIPTOR);
            this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
            String stats = getWiFiDnsStats(data.readInt());
            reply.writeNoException();
            reply.writeString(stats);
            return true;
        } else if (code == CODE_SET_AD_STRATEGY) {
            Slog.d(TAG, "code == CODE_SET_AD_STRATEGY");
            data.enforceInterface(DESCRIPTOR_ADCLEANER_MANAGER_Ex);
            this.mContext.enforceCallingOrSelfPermission(AD_APKDL_STRATEGY_PERMISSION, "permission denied");
            needReset = data.readInt() > 0;
            Slog.d(TAG, "CODE_SET_AD_STRATEGY, needReset: " + needReset);
            if (needReset) {
                this.mAdViewMap.clear();
                this.mAdIdMap.clear();
            }
            size = data.readInt();
            Slog.d(TAG, "CODE_SET_AD_STRATEGY, mAdViewMap size: " + size);
            if (size > 0) {
                for (i = DEFAULT_WIFI_AP_CHANNEL; i < size; i += APK_DL_STRATEGY) {
                    key = data.readString();
                    value = data.createStringArrayList();
                    Slog.d(TAG, "CODE_SET_AD_STRATEGY, mAdViewMap key: " + key + ", at " + i);
                    this.mAdViewMap.put(key, value);
                }
            }
            size = data.readInt();
            Slog.d(TAG, "CODE_SET_AD_STRATEGY, mAdIdMap size: " + size);
            if (size > 0) {
                for (i = DEFAULT_WIFI_AP_CHANNEL; i < size; i += APK_DL_STRATEGY) {
                    key = data.readString();
                    value = data.createStringArrayList();
                    Slog.d(TAG, "CODE_SET_AD_STRATEGY, mAdIdMap key: " + key + ", at " + i);
                    this.mAdIdMap.put(key, value);
                }
            }
            reply.writeNoException();
            return true;
        } else if (code == CODE_CLEAN_AD_STRATEGY) {
            Slog.d(TAG, "code == CODE_CLEAN_AD_STRATEGY");
            data.enforceInterface(DESCRIPTOR_ADCLEANER_MANAGER_Ex);
            this.mContext.enforceCallingOrSelfPermission(AD_APKDL_STRATEGY_PERMISSION, "permission denied");
            int flag = data.readInt();
            Slog.d(TAG, "CODE_CLEAN_AD_STRATEGY, flag: " + flag);
            if (APK_DL_STRATEGY == flag) {
                this.mAdViewMap.clear();
                this.mAdIdMap.clear();
            } else if (flag == 0) {
                ArrayList<String> adAppList = data.createStringArrayList();
                Slog.d(TAG, "CODE_CLEAN_AD_STRATEGY adAppList: ");
                if (adAppList != null) {
                    for (i = DEFAULT_WIFI_AP_CHANNEL; i < adAppList.size(); i += APK_DL_STRATEGY) {
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
        } else if (code == CODE_GET_AD_KEY_LIST) {
            Slog.d(TAG, "code == CODE_GET_AD_KEY_LIST");
            data.enforceInterface(DESCRIPTOR_HW_AD_CLEANER);
            String appName = data.readString();
            if (appName != null) {
                if (this.mAdViewMap.containsKey(appName)) {
                    reply.writeStringList((List) this.mAdViewMap.get(appName));
                    Slog.d(TAG, "appName = " + appName + "  is in the mAdViewMap!");
                    if (appName != null) {
                        if (this.mAdIdMap.containsKey(appName)) {
                            reply.writeStringList((List) this.mAdIdMap.get(appName));
                            Slog.d(TAG, "appName = " + appName + "  is in the mAdIdMap !");
                            reply.writeNoException();
                            return true;
                        }
                    }
                    reply.writeStringList(new ArrayList());
                    Slog.d(TAG, "appName = " + appName + "  is not in the mAdIdMap! reply none");
                    reply.writeNoException();
                    return true;
                }
            }
            try {
                reply.writeStringList(new ArrayList());
                Slog.d(TAG, "appName = " + appName + "  is not in the mAdViewMap! reply none");
                if (appName != null) {
                    if (this.mAdIdMap.containsKey(appName)) {
                        reply.writeStringList((List) this.mAdIdMap.get(appName));
                        Slog.d(TAG, "appName = " + appName + "  is in the mAdIdMap !");
                        reply.writeNoException();
                        return true;
                    }
                }
                reply.writeStringList(new ArrayList());
                Slog.d(TAG, "appName = " + appName + "  is not in the mAdIdMap! reply none");
            } catch (Exception e) {
                Slog.d(TAG, "---------err: Exception ");
                e.printStackTrace();
            }
            reply.writeNoException();
            return true;
        } else if (code == CODE_AD_DEBUG) {
            Set<String> keysSet;
            List<String> keysList;
            List<String> value;
            Slog.d(TAG, "code == CODE_AD_DEBUG");
            data.enforceInterface(DESCRIPTOR_ADCLEANER_MANAGER_Ex);
            this.mContext.enforceCallingOrSelfPermission(AD_APKDL_STRATEGY_PERMISSION, "permission denied");
            data.readInt();
            i = DEFAULT_WIFI_AP_CHANNEL;
            int j = DEFAULT_WIFI_AP_CHANNEL;
            StringBuffer print = new StringBuffer();
            if (!this.mAdViewMap.isEmpty()) {
                print.append("\n---------------- mAdViewMap is as followed ---------------\n");
                keysSet = this.mAdViewMap.keySet();
                keysList = new ArrayList();
                for (String add : keysSet) {
                    keysList.add(add);
                }
                while (true) {
                    if (i >= this.mAdViewMap.size()) {
                        break;
                    }
                    key = (String) keysList.get(i);
                    value = (List) this.mAdViewMap.get(key);
                    print.append("\n(" + i + ") apkName = " + key + "\n");
                    for (j = 
                    /* Method generation error in method: com.android.server.HwNetworkManagementService.onTransact(int, android.os.Parcel, android.os.Parcel, int):boolean
jadx.core.utils.exceptions.CodegenException: Error generate insn: PHI: (r15_6 'j' int) = (r15_0 'j' int), (r15_9 'j' int) binds: {(r15_9 'j' int)=B:108:0x057c, (r15_0 'j' int)=B:214:0x057f} in method: com.android.server.HwNetworkManagementService.onTransact(int, android.os.Parcel, android.os.Parcel, int):boolean
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:225)
	at jadx.core.codegen.RegionGen.makeLoop(RegionGen.java:184)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:61)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:93)
	at jadx.core.codegen.RegionGen.makeLoop(RegionGen.java:174)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:61)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:93)
	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:118)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:57)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:93)
	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:118)
	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:146)
	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:124)
	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:146)
	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:124)
	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:146)
	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:124)
	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:146)
	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:124)
	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:146)
	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:124)
	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:146)
	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:124)
	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:146)
	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:124)
	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:146)
	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:124)
	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:146)
	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:124)
	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:146)
	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:124)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:57)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:177)
	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:324)
	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:263)
	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:226)
	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:116)
	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:81)
	at jadx.core.codegen.CodeGen.visit(CodeGen.java:19)
	at jadx.core.ProcessClass.process(ProcessClass.java:43)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.CodegenException: Unknown instruction: PHI in method: com.android.server.HwNetworkManagementService.onTransact(int, android.os.Parcel, android.os.Parcel, int):boolean
	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:512)
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:219)
	... 54 more
 */

                    private boolean executeHsmCommand(Parcel data, Parcel reply) {
                        try {
                            String cmd = data.readString();
                            Object[] args = data.readArray(null);
                            if (this.mConnector != null) {
                                int i;
                                if (this.mConnector.execute(cmd, args).isClassOk()) {
                                    i = APK_DL_STRATEGY;
                                } else {
                                    i = DEFAULT_WIFI_AP_CHANNEL;
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
                            this.mChannel = DEFAULT_ISMCOEX_WIFI_AP_CHANNEL;
                        } else {
                            this.mChannel = Secure.getInt(this.mContext.getContentResolver(), "wifi_ap_channel", DEFAULT_WIFI_AP_CHANNEL);
                            if (this.mChannel != 0 && (wifiConfig.apBand != 0 || this.mChannel <= 14)) {
                                if (wifiConfig.apBand == APK_DL_STRATEGY && this.mChannel < 34) {
                                }
                            }
                            this.mChannel = wifiConfig.apChannel;
                        }
                        Slog.d(TAG, "channel=" + this.mChannel);
                        return String.valueOf(this.mChannel);
                    }

                    private String getMaxscb() {
                        int maxscb = Secure.getInt(this.mContext.getContentResolver(), "wifi_ap_maxscb", DEFAULT_WIFI_AP_MAX_CONNECTIONS);
                        Slog.d(TAG, "maxscb=" + maxscb);
                        return String.valueOf(maxscb);
                    }

                    private static String getSecurityType(WifiConfiguration wifiConfig) {
                        switch (wifiConfig.getAuthType()) {
                            case APK_DL_STRATEGY /*1*/:
                                return "wpa-psk";
                            case HwGlobalActionsData.FLAG_AIRPLANEMODE_TRANSITING /*4*/:
                                return "wpa2-psk";
                            default:
                                return "open";
                        }
                    }

                    public void startAccessPointWithChannel(WifiConfiguration wifiConfig, String wlanIface) {
                        if (wifiConfig != null) {
                            try {
                                Object[] objArr = new Object[DEFAULT_WIFI_AP_MAX_CONNECTIONS];
                                objArr[DEFAULT_WIFI_AP_CHANNEL] = "set";
                                objArr[APK_DL_STRATEGY] = wlanIface;
                                objArr[2] = wifiConfig.SSID;
                                objArr[3] = "broadcast";
                                objArr[4] = getChannel(wifiConfig);
                                objArr[5] = getSecurityType(wifiConfig);
                                objArr[6] = new SensitiveArg(wifiConfig.preSharedKey);
                                objArr[7] = getMaxscb();
                                this.mConnector.execute("softap", objArr);
                                objArr = new Object[APK_DL_STRATEGY];
                                objArr[DEFAULT_WIFI_AP_CHANNEL] = "startap";
                                this.mConnector.execute("softap", objArr);
                            } catch (NativeDaemonConnectorException e) {
                                throw e.rethrowAsParcelableException();
                            }
                        }
                    }

                    public void sendDataSpeedSlowMessage(String[] cooked, String raw) {
                        if (cooked.length < 2 || !cooked[APK_DL_STRATEGY].equals("sourceAddress")) {
                            Object[] objArr = new Object[APK_DL_STRATEGY];
                            objArr[DEFAULT_WIFI_AP_CHANNEL] = raw;
                            String msg1 = String.format("Invalid event from daemon (%s)", objArr);
                            Slog.d(TAG, "receive DataSpeedSlowDetected,return error 1");
                            throw new IllegalStateException(msg1);
                        }
                        int sourceAddress = Integer.parseInt(cooked[2]);
                        NetworkInfo mobileNetinfo = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getNetworkInfo(DEFAULT_WIFI_AP_CHANNEL);
                        Slog.d(TAG, "onEvent receive DataSpeedSlowDetected");
                        if (mobileNetinfo != null && mobileNetinfo.isConnected()) {
                            Slog.d(TAG, "onEvent receive DataSpeedSlowDetected,mobile network is connected!");
                            Intent chrIntent = new Intent("com.android.intent.action.data_speed_slow");
                            chrIntent.putExtra("sourceAddress", sourceAddress);
                            this.mContext.sendBroadcast(chrIntent, CHR_BROADCAST_PERMISSION);
                        }
                    }

                    public boolean handleApLinkedStaListChange(String raw, String[] cooked) {
                        Slog.d(TAG, "handleApLinkedStaListChange is called");
                        if ("STA_JOIN".equals(cooked[APK_DL_STRATEGY]) || "STA_LEAVE".equals(cooked[APK_DL_STRATEGY])) {
                            Slog.d(TAG, "Got sta list change event:" + cooked[APK_DL_STRATEGY]);
                            notifyApLinkedStaListChange(cooked[APK_DL_STRATEGY], cooked[4]);
                            return true;
                        }
                        Object[] objArr = new Object[APK_DL_STRATEGY];
                        objArr[DEFAULT_WIFI_AP_CHANNEL] = raw;
                        throw new IllegalStateException(String.format("ApLinkedStaListChange: Invalid event from daemon (%s)", objArr));
                    }

                    public List<String> getApLinkedStaList() {
                        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
                        try {
                            List<String> mDhcpList = getApLinkedDhcpList();
                            Slog.d(TAG, "getApLinkedStaList: softap assoclist");
                            Object[] objArr = new Object[APK_DL_STRATEGY];
                            objArr[DEFAULT_WIFI_AP_CHANNEL] = "assoclist";
                            String[] macList = this.mConnector.doListCommand("softap", WifiProCHRManager.WIFI_WIFIPRO_STATISTICS_EVENT, objArr);
                            if (macList == null) {
                                Slog.e(TAG, "getApLinkedStaList Error: doListCommand return NULL");
                                this.mLinkedStaCount = DEFAULT_WIFI_AP_CHANNEL;
                                return null;
                            }
                            this.mLinkedStaCount = macList.length;
                            List<String> infoList = new ArrayList();
                            int length = macList.length;
                            for (int i = DEFAULT_WIFI_AP_CHANNEL; i < length; i += APK_DL_STRATEGY) {
                                String mac = getApLinkedStaInfo(macList[i], mDhcpList);
                                Slog.d(TAG, "getApLinkedStaList ApLinkedStaInfo = " + mac);
                                infoList.add(mac);
                            }
                            Slog.d(TAG, "getApLinkedStaList, info size=" + infoList.size());
                            return infoList;
                        } catch (NativeDaemonConnectorException e) {
                            throw new IllegalStateException("Cannot communicate with native daemon to get linked stations list");
                        }
                    }

                    public void setSoftapMacFilter(String macFilter) {
                        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
                        try {
                            Slog.d(TAG, "setSoftapMacFilter:" + String.format("softap setmacfilter " + macFilter, new Object[DEFAULT_WIFI_AP_CHANNEL]));
                            this.mConnector.doCommand("softap", new Object[]{"setmacfilter", macFilter});
                        } catch (NativeDaemonConnectorException e) {
                            throw new IllegalStateException("Cannot communicate with native daemon to set MAC Filter");
                        }
                    }

                    public void setSoftapDisassociateSta(String mac) {
                        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
                        try {
                            Slog.d(TAG, "setSoftapDisassociateSta:" + String.format("softap disassociatesta " + mac, new Object[DEFAULT_WIFI_AP_CHANNEL]));
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
                        hwApConfig.maxScb = Secure.getInt(this.mContext.getContentResolver(), "wifi_ap_maxscb", DEFAULT_WIFI_AP_MAX_CONNECTIONS);
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
                            Object[] objArr = new Object[APK_DL_STRATEGY];
                            objArr[DEFAULT_WIFI_AP_CHANNEL] = "getdhcplease";
                            String[] dhcpleaseList = this.mConnector.doListCommand("softap", WifiProCHRManager.WIFI_WIFIPRO_EXCEPTION_EVENT, objArr);
                            if (dhcpleaseList == null) {
                                Slog.e(TAG, "getApLinkedDhcpList Error: doListCommand return NULL");
                                return null;
                            }
                            List<String> mDhcpList = new ArrayList();
                            int length = dhcpleaseList.length;
                            for (int i = DEFAULT_WIFI_AP_CHANNEL; i < length; i += APK_DL_STRATEGY) {
                                String dhcplease = dhcpleaseList[i];
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
                        Object[] objArr = new Object[APK_DL_STRATEGY];
                        objArr[DEFAULT_WIFI_AP_CHANNEL] = mac;
                        String ApLinkedStaInfo = String.format("MAC=%s", objArr);
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
                        String action = null;
                        long mCurrentTime = System.currentTimeMillis();
                        if ("STA_JOIN".equals(event)) {
                            action = ACTION_WIFI_AP_STA_JOIN;
                            this.mLinkedStaCount += APK_DL_STRATEGY;
                        } else if ("STA_LEAVE".equals(event)) {
                            action = ACTION_WIFI_AP_STA_LEAVE;
                            this.mLinkedStaCount--;
                        }
                        if (this.mLinkedStaCount < 0 || this.mLinkedStaCount > DEFAULT_WIFI_AP_MAX_CONNECTIONS) {
                            Slog.e(TAG, "mLinkedStaCount over flow, need synchronize. value = " + this.mLinkedStaCount);
                            try {
                                String[] macList = this.mConnector.doListCommand(String.format("softap assoclist", new Object[DEFAULT_WIFI_AP_CHANNEL]), WifiProCHRManager.WIFI_WIFIPRO_STATISTICS_EVENT, new Object[DEFAULT_WIFI_AP_CHANNEL]);
                                if (macList == null) {
                                    this.mLinkedStaCount = DEFAULT_WIFI_AP_CHANNEL;
                                } else {
                                    this.mLinkedStaCount = macList.length;
                                }
                            } catch (NativeDaemonConnectorException e) {
                                Slog.e(TAG, "Cannot communicate with native daemon to get linked stations list");
                                this.mLinkedStaCount = DEFAULT_WIFI_AP_CHANNEL;
                            }
                        }
                        Slog.e(TAG, "send broadcast, event=" + event + ", extraInfo: " + String.format("MAC=%s TIME=%d STACNT=%d", new Object[]{macStr, Long.valueOf(mCurrentTime), Integer.valueOf(this.mLinkedStaCount)}));
                        Intent broadcast = new Intent(action);
                        broadcast.putExtra(EXTRA_STA_INFO, macStr);
                        broadcast.putExtra(EXTRA_CURRENT_TIME, mCurrentTime);
                        broadcast.putExtra(EXTRA_STA_COUNT, this.mLinkedStaCount);
                        this.mContext.sendBroadcast(broadcast, "android.permission.ACCESS_WIFI_STATE");
                    }

                    public void setWifiTxPower(String reduceCmd) {
                        Slog.d(TAG, "setWifiTxPower " + reduceCmd);
                        try {
                            Object[] objArr = new Object[APK_DL_STRATEGY];
                            objArr[DEFAULT_WIFI_AP_CHANNEL] = reduceCmd;
                            this.mConnector.execute("softap", objArr);
                        } catch (NativeDaemonConnectorException e) {
                            throw e.rethrowAsParcelableException();
                        }
                    }

                    private String getWiFiDnsStats(int netid) {
                        StringBuffer buf = new StringBuffer();
                        try {
                            String[] stats = this.mConnector.doListCommand("resolver", CPUFeature.MSG_SET_INTERACTIVE_NOSAVE, new Object[]{"getdnsstat", Integer.valueOf(netid)});
                            if (stats != null) {
                                for (int i = DEFAULT_WIFI_AP_CHANNEL; i < stats.length; i += APK_DL_STRATEGY) {
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
                        for (int i = DEFAULT_WIFI_AP_CHANNEL; i < bytes.length; i += APK_DL_STRATEGY) {
                            sb.append(HEX_STR.charAt((bytes[i] & 240) >> 4));
                            sb.append(HEX_STR.charAt((bytes[i] & 15) >> DEFAULT_WIFI_AP_CHANNEL));
                        }
                        return sb.toString();
                    }

                    private String hexStrToStr(String hexStr) {
                        if (hexStr == null) {
                            return null;
                        }
                        ByteArrayOutputStream baos = new ByteArrayOutputStream(hexStr.length() / 2);
                        for (int i = DEFAULT_WIFI_AP_CHANNEL; i < hexStr.length(); i += 2) {
                            baos.write((HEX_STR.indexOf(hexStr.charAt(i)) << 4) | HEX_STR.indexOf(hexStr.charAt(i + APK_DL_STRATEGY)));
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
                            int uidCount = DEFAULT_WIFI_AP_CHANNEL;
                            int length = pkgName.length;
                            for (int i = DEFAULT_WIFI_AP_CHANNEL; i < length; i += APK_DL_STRATEGY) {
                                String pkg = pkgName[i];
                                for (int n = DEFAULT_WIFI_AP_CHANNEL; n < userCount; n += APK_DL_STRATEGY) {
                                    try {
                                        int uid = pm.getPackageUidAsUser(pkg, ((UserInfo) users.get(n)).id);
                                        Slog.d(TAG, "convertPkgNameToUid, pkg=" + pkg + ", uid=" + uid + ", under user.id=" + ((UserInfo) users.get(n)).id);
                                        uidCount += APK_DL_STRATEGY;
                                        if (uidCount % PER_UID_LIST_SIZE == 0) {
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
                        int count = DEFAULT_WIFI_AP_CHANNEL;
                        int strategyLen = DEFAULT_WIFI_AP_CHANNEL;
                        if (adStrategy != null) {
                            strategyLen = adStrategy.length();
                            count = strategyLen / PER_STRATEGY_SIZE;
                            if (strategyLen % PER_STRATEGY_SIZE != 0) {
                                count += APK_DL_STRATEGY;
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
                            int i = APK_DL_STRATEGY;
                            while (adStrategy.length() > 0) {
                                if (adStrategy.length() > PER_STRATEGY_SIZE) {
                                    Slog.d(TAG, "setAdFilterRules, adStrategy len=" + adStrategy.substring(DEFAULT_WIFI_AP_CHANNEL, PER_STRATEGY_SIZE).length() + ", seq=" + i + ", cmdId=" + cmdId);
                                    this.mConnector.execute("hwfilter", new Object[]{"set_ad_strategy_buf", Integer.valueOf(cmdId), Integer.valueOf(i), adStrategyTmp});
                                    adStrategy = adStrategy.substring(PER_STRATEGY_SIZE);
                                    i += APK_DL_STRATEGY;
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
                                    for (int i = DEFAULT_WIFI_AP_CHANNEL; i < appUidList.size(); i += APK_DL_STRATEGY) {
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
                                        for (i = DEFAULT_WIFI_AP_CHANNEL; i < appUidList.size(); i += APK_DL_STRATEGY) {
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
                        } else if (APK_DL_STRATEGY != strategy) {
                        } else {
                            if (appUidList == null || appUidList.size() <= 0) {
                                this.mConnector.execute("hwfilter", new Object[]{"clear_apkdl_strategy_rule", null, operation});
                                return;
                            }
                            for (i = DEFAULT_WIFI_AP_CHANNEL; i < appUidList.size(); i += APK_DL_STRATEGY) {
                                if (i == 0) {
                                    this.mConnector.execute("hwfilter", new Object[]{"clear_apkdl_strategy_rule", appUidList.get(i), operation});
                                } else {
                                    this.mConnector.execute("hwfilter", new Object[]{"clear_apkdl_strategy_rule", appUidList.get(i), "not_reset"});
                                }
                            }
                        }
                    }

                    private void printAdOrApkDlFilterRules(int strategy) {
                        Slog.d(TAG, "printAdOrApkDlFilterRules, strategy=" + strategy);
                        Object[] objArr;
                        if (strategy == 0) {
                            try {
                                objArr = new Object[APK_DL_STRATEGY];
                                objArr[DEFAULT_WIFI_AP_CHANNEL] = "output_ad_strategy_rule";
                                this.mConnector.execute("hwfilter", objArr);
                            } catch (NativeDaemonConnectorException e) {
                                throw e.rethrowAsParcelableException();
                            }
                        } else if (APK_DL_STRATEGY == strategy) {
                            objArr = new Object[APK_DL_STRATEGY];
                            objArr[DEFAULT_WIFI_AP_CHANNEL] = "output_apkdl_strategy_rule";
                            this.mConnector.execute("hwfilter", objArr);
                        }
                    }

                    private void setApkDlUrlUserResult(String downloadId, boolean result) {
                        Slog.d(TAG, "setApkDlUrlUserResult, downloadId=" + downloadId + ", result=" + result);
                        String operation = result ? "allow" : "reject";
                        try {
                            this.mConnector.execute("hwfilter", new Object[]{"apkdl_callback", downloadId, operation});
                        } catch (NativeDaemonConnectorException e) {
                            throw e.rethrowAsParcelableException();
                        }
                    }

                    public void sendApkDownloadUrlBroadcast(String[] cooked, String raw) {
                        Slog.d(TAG, "receive report_apkdl_event, raw=" + raw);
                        if (cooked.length < 4) {
                            Object[] objArr = new Object[APK_DL_STRATEGY];
                            objArr[DEFAULT_WIFI_AP_CHANNEL] = raw;
                            String errorMessage = String.format("Invalid event from daemon (%s)", objArr);
                            Slog.d(TAG, "receive report_apkdl_event, return error");
                            throw new IllegalStateException(errorMessage);
                        }
                        long startTime = SystemClock.elapsedRealtime();
                        String downloadId = cooked[APK_DL_STRATEGY];
                        String uid = cooked[2];
                        if (!this.startTimeMap.containsKey(downloadId)) {
                            this.startTimeMap.put(downloadId, Long.valueOf(startTime));
                        }
                        Matcher m = this.p.matcher(cooked[3]);
                        if (!m.matches() || m.groupCount() < 3) {
                            String url = hexStrToStr(cooked[3]);
                            Slog.d(TAG, "onEvent receive report_apkdl_event, startTime=" + startTime + ", downloadId=" + downloadId + ", uid=" + uid + ", url=" + url);
                            Intent intent = new Intent(INTENT_APKDL_URL_DETECTED);
                            intent.putExtra("startTime", startTime);
                            intent.putExtra("downloadId", downloadId);
                            intent.putExtra("uid", uid);
                            intent.putExtra("url", url);
                            this.mContext.sendBroadcast(intent, AD_APKDL_STRATEGY_PERMISSION);
                            return;
                        }
                        int max = Integer.parseInt(m.group(APK_DL_STRATEGY));
                        int idx = Integer.parseInt(m.group(2));
                        String subUrl = m.group(3);
                        if (idx == APK_DL_STRATEGY) {
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
                            this.mContext.sendBroadcast(intent, AD_APKDL_STRATEGY_PERMISSION);
                        }
                    }

                    public void systemReady() {
                        super.systemReady();
                        initNetworkAccessWhitelist();
                    }

                    private void initNetworkAccessWhitelist() {
                        List<String> networkAccessWhitelist = HwDeviceManager.getList(9);
                        if (networkAccessWhitelist != null && !networkAccessWhitelist.isEmpty()) {
                            Slog.d(TAG, "networkAccessWhitelist has been set");
                            new AnonymousClass1(networkAccessWhitelist).start();
                        }
                    }

                    public void setNetworkAccessWhitelist(List<String> addrList) {
                        if (addrList != null) {
                            try {
                                if (!addrList.isEmpty()) {
                                    int size = addrList.size();
                                    Slog.d(TAG, "set ipwhitelist:" + ((String) this.mConnector.doCommand("net_filter", new Object[]{"ipwhitelist", "set", addrList.get(DEFAULT_WIFI_AP_CHANNEL)}).get(DEFAULT_WIFI_AP_CHANNEL)));
                                    for (int i = APK_DL_STRATEGY; i < size; i += APK_DL_STRATEGY) {
                                        Slog.d(TAG, "add ipwhitelist:" + ((String) this.mConnector.doCommand("net_filter", new Object[]{"ipwhitelist", "add", addrList.get(i)}).get(DEFAULT_WIFI_AP_CHANNEL)));
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
                        Slog.d(TAG, "clear ipwhitelist:" + ((String) this.mConnector.doCommand("net_filter", new Object[]{"ipwhitelist", "clear"}).get(DEFAULT_WIFI_AP_CHANNEL)));
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
                }
