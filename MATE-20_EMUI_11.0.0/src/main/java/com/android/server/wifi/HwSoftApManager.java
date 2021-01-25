package com.android.server.wifi;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.hwcoex.HiCoexChrImpl;
import com.android.server.wifi.hwcoex.HiCoexManagerImpl;
import com.android.server.wifi.util.ApConfigUtil;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import libcore.io.IoUtils;

public class HwSoftApManager extends SoftApManager {
    private static final String ACTION_WIFI_AP_STA_JOIN = "com.huawei.net.wifi.WIFI_AP_STA_JOIN";
    private static final String ACTION_WIFI_AP_STA_LEAVE = "com.huawei.net.wifi.WIFI_AP_STA_LEAVE";
    private static final String ANONYMOUS_MAC = "**:**:**:**";
    private static final int ANONYMOUS_MAC_INDEX = 11;
    private static final int AP_BANDWIDTH_DVHT = 8;
    private static final int BROADCAST_SSID_MENU_DISPLAY = 1;
    private static final int BROADCAST_SSID_MENU_HIDE = 0;
    private static final int CMD_CALI_FEM_MODE = 130;
    private static final int CMD_SET_SOFTAP_2G_MSS = 106;
    private static final String COMM_IFACE = "wlan0";
    private static boolean DBG = HWFLOW;
    private static final int DEFAULT_ISMCOEX_WIFI_AP_CHANNEL = 11;
    private static final int DEFAULT_WIFI_AP_CHANNEL = 0;
    private static final int DEFAULT_WIFI_AP_MAXSCB = 8;
    private static final String EXTRA_CURRENT_TIME = "currentTime";
    private static final String EXTRA_STA_COUNT = "staCount";
    private static final String EXTRA_STA_INFO = "macInfo";
    protected static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final String ISM_COEX_ON = "ro.config.hw_ismcoex";
    private static final boolean IS_MSS_2G_ON = SystemProperties.getBoolean("ro.config.ap_24_mimo_on", true);
    private static final int MAX_AP_LINKED_COUNT = 8;
    private static final int MIMO_THRESHOLD = 45;
    private static final int MIN_DHCPLEASE_LENGTH = 4;
    private static final int NT_CHINA_CMCC = 3;
    private static final int NT_CHINA_UT = 2;
    private static final int NT_FOREIGN = 1;
    private static final int NT_UNREG = 0;
    private static final String PATH_DHCP_FILE = "/data/misc/dhcp/dnsmasq.leases";
    private static final String PERFORMANCE_APP = "com.example.wptp.testapp";
    private static final String PROP_NET_SHARE_UI = "ro.feature.mobile_network_sharing_lite";
    private static final int SEGMENT_LENGTH_MIN = 2;
    private static final int SEND_BROADCAST = 0;
    private static final String SOFTAP_BLACKLIST_COUNT = "SoftApBlacklistCnt";
    private static final String SPLIT_DOT = ",";
    private static final String SPLIT_EQUAL = "=";
    private static final int STA_JOIN_HANDLE_DELAY = 5000;
    private static final String TAG = "HwSoftApManager";
    private Handler mApLinkedStaChangedHandler = new Handler() {
        /* class com.android.server.wifi.HwSoftApManager.AnonymousClass1 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            String action = null;
            long mCurrentTime = System.currentTimeMillis();
            Bundle bundle = msg.getData();
            String event = bundle.getString("event_key");
            String macAddress = bundle.getString("mac_key").toLowerCase(Locale.ENGLISH);
            if (event != null && macAddress != null) {
                if ("STA_JOIN".equals(event)) {
                    action = HwSoftApManager.ACTION_WIFI_AP_STA_JOIN;
                    if (!HwSoftApManager.this.mMacList.contains(macAddress)) {
                        HwSoftApManager.this.mMacList.add(macAddress);
                        HwSoftApManager.access$108(HwSoftApManager.this);
                    } else {
                        HwHiLog.e(HwSoftApManager.TAG, false, "%{private}s had been added, but still get event %{public}s", new Object[]{HwSoftApManager.this.anonyMac(macAddress), event});
                        HwSoftApManager.this.updateLinkedInfo();
                    }
                } else if ("STA_LEAVE".equals(event)) {
                    action = HwSoftApManager.ACTION_WIFI_AP_STA_LEAVE;
                    if (HwSoftApManager.this.mApLinkedStaChangedHandler.hasMessages(msg.what, "STA_JOIN") || HwSoftApManager.this.mMacList.contains(macAddress)) {
                        if (HwSoftApManager.this.mApLinkedStaChangedHandler.hasMessages(msg.what, "STA_JOIN")) {
                            HwSoftApManager.this.mApLinkedStaChangedHandler.removeMessages(msg.what, "STA_JOIN");
                            HwHiLog.d(HwSoftApManager.TAG, false, "event=%{public}s, remove STA_JOIN message, mac=%{private}s", new Object[]{event, HwSoftApManager.this.anonyMac(macAddress)});
                        }
                        if (HwSoftApManager.this.mMacList.contains(macAddress)) {
                            HwSoftApManager.this.mMacList.remove(macAddress);
                            HwSoftApManager.access$110(HwSoftApManager.this);
                        }
                    } else {
                        HwHiLog.e(HwSoftApManager.TAG, false, "%{private}s had been removed, but still get event %{public}s", new Object[]{HwSoftApManager.this.anonyMac(macAddress), event});
                        HwSoftApManager.this.updateLinkedInfo();
                    }
                }
                HwHiLog.d(HwSoftApManager.TAG, false, "ApLinkedStaChanged message handled, event=%{public}s mac=%{private}s, mLinkedStaCount=%{public}d", new Object[]{event, HwSoftApManager.this.anonyMac(macAddress), Integer.valueOf(HwSoftApManager.this.mLinkedStaCount)});
                if (HwSoftApManager.this.mLinkedStaCount < 0 || HwSoftApManager.this.mLinkedStaCount > 8 || HwSoftApManager.this.mLinkedStaCount != HwSoftApManager.this.mMacList.size()) {
                    HwHiLog.e(HwSoftApManager.TAG, false, "mLinkedStaCount over flow, need synchronize. mLinkedStaCount=%{public}d, mMacList.size()=%{public}d", new Object[]{Integer.valueOf(HwSoftApManager.this.mLinkedStaCount), Integer.valueOf(HwSoftApManager.this.mMacList.size())});
                    HwSoftApManager.this.updateLinkedInfo();
                }
                HwHiLog.d(HwSoftApManager.TAG, false, "Send broadcast: %{public}s, event=%{public}s, extraInfo: MAC=%{private}s TIME=%{public}d STACNT=%{public}d", new Object[]{action, event, HwSoftApManager.this.anonyMac(macAddress), Long.valueOf(mCurrentTime), Integer.valueOf(HwSoftApManager.this.mLinkedStaCount)});
                Intent broadcast = new Intent(action);
                broadcast.addFlags(16777216);
                broadcast.putExtra(HwSoftApManager.EXTRA_STA_INFO, macAddress);
                broadcast.putExtra(HwSoftApManager.EXTRA_CURRENT_TIME, mCurrentTime);
                broadcast.putExtra(HwSoftApManager.EXTRA_STA_COUNT, HwSoftApManager.this.mLinkedStaCount);
                HwSoftApManager.this.mContext.sendBroadcast(broadcast, "android.permission.ACCESS_WIFI_STATE");
            }
        }
    };
    private Context mContext;
    private int mDataSub = -1;
    private HwPhoneCloneChr mHwPhoneCloneChr;
    private HwWifiCHRService mHwWifiCHRService;
    private int mLinkedStaCount = 0;
    private int mMacFilterStaCount = 0;
    private String mMacFilterStr = "";
    private List<String> mMacList = new ArrayList();
    private String mOperatorNumericSub0 = null;
    private String mOperatorNumericSub1 = null;
    private PhoneStateListener[] mPhoneStateListener;
    private int mServiceStateSub0 = 1;
    private int mServiceStateSub1 = 1;
    private TelephonyManager mTelephonyManager;
    private WifiChannelXmlParse mWifiChannelXmlParse = null;
    private WifiNative mWifiNative;

    static /* synthetic */ int access$108(HwSoftApManager x0) {
        int i = x0.mLinkedStaCount;
        x0.mLinkedStaCount = i + 1;
        return i;
    }

    static /* synthetic */ int access$110(HwSoftApManager x0) {
        int i = x0.mLinkedStaCount;
        x0.mLinkedStaCount = i - 1;
        return i;
    }

    public HwSoftApManager(Context context, Looper looper, FrameworkFacade frameworkFacade, WifiNative wifiNative, String countryCode, WifiManager.SoftApCallback callback, WifiApConfigStore wifiApConfigStore, SoftApModeConfiguration config, WifiMetrics wifiMetrics, SarManager sarManager) {
        super(context, looper, frameworkFacade, wifiNative, countryCode, callback, wifiApConfigStore, config, wifiMetrics, sarManager);
        this.mContext = context;
        this.mWifiNative = wifiNative;
        this.mHwWifiCHRService = HwWifiServiceFactory.getHwWifiCHRService();
        this.mHwPhoneCloneChr = HwPhoneCloneChr.getInstance();
    }

    private void registerPhoneStateListener(Context context) {
        this.mTelephonyManager = (TelephonyManager) context.getSystemService("phone");
        this.mPhoneStateListener = new PhoneStateListener[2];
        for (int i = 0; i < 2; i++) {
            this.mPhoneStateListener[i] = getPhoneStateListener(i);
            this.mTelephonyManager.listen(this.mPhoneStateListener[i], 1);
        }
    }

    private PhoneStateListener getPhoneStateListener(int subId) {
        return new PhoneStateListener(Integer.valueOf(subId)) {
            /* class com.android.server.wifi.HwSoftApManager.AnonymousClass2 */

            @Override // android.telephony.PhoneStateListener
            public void onServiceStateChanged(ServiceState state) {
                if (state != null) {
                    if (HwSoftApManager.DBG) {
                        HwHiLog.d(HwSoftApManager.TAG, false, "PhoneStateListener %{public}d", new Object[]{this.mSubId});
                    }
                    if (this.mSubId.intValue() == 0) {
                        HwSoftApManager.this.mServiceStateSub0 = state.getDataRegState();
                        HwSoftApManager.this.mOperatorNumericSub0 = state.getOperatorNumeric();
                    } else if (this.mSubId.intValue() == 1) {
                        HwSoftApManager.this.mServiceStateSub1 = state.getDataRegState();
                        HwSoftApManager.this.mOperatorNumericSub1 = state.getOperatorNumeric();
                    }
                }
            }
        };
    }

    private int getRegistedNetworkType() {
        String numeric;
        int serviceState;
        int serviceState2 = this.mDataSub;
        if (serviceState2 == 0) {
            serviceState = this.mServiceStateSub0;
            numeric = this.mOperatorNumericSub0;
        } else if (serviceState2 != 1) {
            return 0;
        } else {
            serviceState = this.mServiceStateSub0;
            numeric = this.mOperatorNumericSub0;
        }
        HwHiLog.d(TAG, false, "isRegistedNetworkType mDataSub %{public}d, serviceState %{public}d, numeric %{public}s", new Object[]{Integer.valueOf(this.mDataSub), Integer.valueOf(serviceState), numeric});
        if (serviceState != 0 || (numeric != null && numeric.length() >= 5 && numeric.substring(0, 5).equals("99999"))) {
            return 0;
        }
        if (numeric == null || numeric.length() < 3 || !numeric.substring(0, 3).equals("460")) {
            return (numeric == null || numeric.equals("")) ? 0 : 1;
        }
        if ("46000".equals(this.mOperatorNumericSub0) || "46002".equals(this.mOperatorNumericSub0) || "46007".equals(this.mOperatorNumericSub0)) {
            return 3;
        }
        return 2;
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0085: APUT  (r2v1 java.lang.Object[]), (0 ??[int, short, byte, char]), (r5v1 java.lang.String) */
    private static String getCurrentBand() {
        String ret = null;
        String[] bandrst = HwTelephonyManagerInner.getDefault().queryServiceCellBand();
        if (bandrst != null) {
            if (bandrst.length < 2) {
                if (!DBG) {
                    return null;
                }
                HwHiLog.d(TAG, false, "getCurrentBand bandrst error.", new Object[0]);
                return null;
            } else if ("GSM".equals(bandrst[0])) {
                int gsmType = -1;
                try {
                    gsmType = Integer.parseInt(bandrst[1]);
                } catch (NumberFormatException e) {
                    HwHiLog.e(TAG, false, "get gsmType fail", new Object[0]);
                }
                if (gsmType == 0) {
                    ret = "GSM850";
                } else if (gsmType == 1) {
                    ret = "GSM900";
                } else if (gsmType == 2) {
                    ret = "GSM1800";
                } else if (gsmType != 3) {
                    HwHiLog.e(TAG, false, "should not be here.", new Object[0]);
                } else {
                    ret = "GSM1900";
                }
            } else if ("CDMA".equals(bandrst[0])) {
                ret = "BC0";
            } else {
                ret = bandrst[0] + bandrst[1];
            }
        }
        if (DBG) {
            Object[] objArr = new Object[1];
            objArr[0] = ret == null ? "null" : ret;
            HwHiLog.d(TAG, false, "getCurrentBand rst is %{public}s", objArr);
        }
        return ret;
    }

    private ArrayList<Integer> getAllowed2GChannels(ArrayList<Integer> allowedChannels) {
        int networkType = getRegistedNetworkType();
        ArrayList<Integer> intersectChannels = new ArrayList<>();
        if (allowedChannels == null) {
            return null;
        }
        if (networkType == 3) {
            intersectChannels.add(6);
        } else if (networkType == 2) {
            intersectChannels.add(1);
            intersectChannels.add(6);
        } else if (networkType == 1) {
            this.mWifiChannelXmlParse = WifiChannelXmlParse.getInstance();
            ArrayList<Integer> vaildChannels = this.mWifiChannelXmlParse.getValidChannels(getCurrentBand(), true);
            intersectChannels = (ArrayList) allowedChannels.clone();
            if (vaildChannels != null) {
                intersectChannels.retainAll(vaildChannels);
            }
            if (intersectChannels.size() == 0) {
                intersectChannels = allowedChannels;
            }
        } else {
            intersectChannels = allowedChannels;
        }
        filterRilCoexChannels(intersectChannels);
        if (DBG) {
            StringBuilder sb = new StringBuilder();
            sb.append("channels: ");
            Iterator<Integer> it = intersectChannels.iterator();
            while (it.hasNext()) {
                sb.append(it.next().toString() + SPLIT_DOT);
            }
            HwHiLog.d(TAG, false, "2G %{public}s", new Object[]{sb.toString()});
        }
        return intersectChannels;
    }

    /* JADX WARNING: Removed duplicated region for block: B:23:0x0057  */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0095  */
    private static int[] getAllowed5GChannels(WifiNative wifiNative) {
        int[] allowedChannels = wifiNative.getChannelsForBand(2);
        if (allowedChannels == null || allowedChannels.length <= 1) {
            return allowedChannels;
        }
        int[] values = new int[allowedChannels.length];
        ArrayList<Integer> vaildChannels = WifiChannelXmlParse.getInstance().getValidChannels(getCurrentBand(), false);
        int counter = 0;
        if (vaildChannels != null) {
            for (int i = 0; i < allowedChannels.length; i++) {
                try {
                    if (vaildChannels.contains(Integer.valueOf(ApConfigUtil.convertFrequencyToChannel(allowedChannels[i])))) {
                        int counter2 = counter + 1;
                        try {
                            values[counter] = allowedChannels[i];
                            counter = counter2;
                        } catch (Exception e) {
                            counter = counter2;
                            HwHiLog.e(TAG, false, "getAllowed5GChannels happened exception", new Object[0]);
                            if (counter == 0) {
                            }
                        }
                    }
                } catch (Exception e2) {
                    HwHiLog.e(TAG, false, "getAllowed5GChannels happened exception", new Object[0]);
                    if (counter == 0) {
                    }
                }
            }
        }
        if (counter == 0) {
            HwHiLog.d(TAG, false, "5G counter is 0", new Object[0]);
            if (DBG) {
                StringBuilder sb = new StringBuilder();
                sb.append("allowedChannels channels: ");
                for (int i2 = 0; i2 < allowedChannels.length; i2++) {
                    sb.append(ApConfigUtil.convertFrequencyToChannel(allowedChannels[i2]) + SPLIT_DOT);
                }
                HwHiLog.d(TAG, false, "5G %{public}s", new Object[]{sb.toString()});
            }
            return allowedChannels;
        }
        int[] intersectChannels = new int[counter];
        for (int i3 = 0; i3 < counter; i3++) {
            intersectChannels[i3] = values[i3];
        }
        if (DBG) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append("allowedChannels channels: ");
            for (int i4 = 0; i4 < allowedChannels.length; i4++) {
                sb2.append(ApConfigUtil.convertFrequencyToChannel(allowedChannels[i4]) + SPLIT_DOT);
            }
            sb2.append("intersectChannels channels: ");
            for (int i5 = 0; i5 < counter; i5++) {
                sb2.append(ApConfigUtil.convertFrequencyToChannel(intersectChannels[i5]) + SPLIT_DOT);
            }
            HwHiLog.d(TAG, false, "5G %{public}s", new Object[]{sb2.toString()});
        }
        return intersectChannels;
    }

    private boolean isInstalledApp(String pkgName) {
        if (pkgName == null || pkgName.length() <= 0) {
            HwHiLog.e(TAG, false, "pkgName is null!", new Object[0]);
            return false;
        }
        List<PackageInfo> packages = this.mContext.getPackageManager().getInstalledPackages(0);
        if (packages == null) {
            HwHiLog.e(TAG, false, "get installed packages is null!", new Object[0]);
            return false;
        }
        for (PackageInfo packageInfo : packages) {
            if (pkgName.equals(packageInfo.packageName)) {
                return true;
            }
        }
        return false;
    }

    private int getBatteryLevel(Context context) {
        if (context != null) {
            return ((BatteryManager) context.getSystemService("batterymanager")).getIntProperty(4);
        }
        HwHiLog.e(TAG, false, "context is nul!", new Object[0]);
        return 0;
    }

    private void setMssFor2GChannels(WifiConfiguration config) {
        boolean z = IS_MSS_2G_ON;
        if (!z) {
            HwHiLog.e(TAG, false, "IS_MSS_2G_ON: %{public}s", new Object[]{Boolean.valueOf(z)});
        } else if (config == null) {
            HwHiLog.e(TAG, false, "softap config is null!", new Object[0]);
        } else if (config.apBand == 0) {
            byte[] cmdParam = {77};
            int mimoCnt = 0;
            int sisoCnt = 0;
            int batteryLevel = getBatteryLevel(this.mContext);
            if (isInstalledApp(PERFORMANCE_APP) || batteryLevel > MIMO_THRESHOLD) {
                this.mWifiNative.mHwWifiNativeEx.sendCmdToDriver(this.mWifiNative.getSoftApInterfaceName(), 106, cmdParam);
                mimoCnt = 1;
                HwHiLog.d(TAG, false, "set softap in MIMO mode, current battery level is: %{public}s", new Object[]{Integer.valueOf(batteryLevel)});
            } else {
                sisoCnt = 1;
                HwHiLog.d(TAG, false, "set softap in SISO mode, current battery level is: %{public}s", new Object[]{Integer.valueOf(batteryLevel)});
            }
            if (this.mHwWifiCHRService != null) {
                Bundle chrData = new Bundle();
                chrData.putInt("isMimo", mimoCnt);
                chrData.putInt("isSiso", sisoCnt);
                chrData.putInt("batteryLevel", batteryLevel);
                this.mHwWifiCHRService.uploadDFTEvent(11, chrData);
            }
        }
    }

    public void start() {
        setFemCalibrateMode(false);
        HwSoftApManager.super.start();
        ClientModeImpl wsm = WifiInjector.getInstance().getClientModeImpl();
        if (wsm instanceof HwWifiStateMachine) {
            ((HwWifiStateMachine) wsm).registHwSoftApManager(this);
        }
        this.mHwPhoneCloneChr.notifyApStarted(this.mApConfig);
    }

    public void stop() {
        setFemCalibrateMode(true);
        this.mHwPhoneCloneChr.notifyApStopped(this.mContext);
        HwSoftApManager.super.stop();
        ClientModeImpl wsm = WifiInjector.getInstance().getClientModeImpl();
        if (wsm instanceof HwWifiStateMachine) {
            ((HwWifiStateMachine) wsm).clearHwSoftApManager();
        }
    }

    public int updateApChannelConfig(WifiNative wifiNative, String wifiCountryCode, ArrayList<Integer> allowed24GChannels, WifiConfiguration wifiConfig) {
        if (!wifiNative.isHalStarted()) {
            wifiConfig.apBand = 0;
            wifiConfig.apChannel = 6;
            setMssFor2GChannels(wifiConfig);
            return 0;
        } else if (wifiConfig.apBand == 1 && wifiCountryCode == null) {
            HwHiLog.e(TAG, false, "5GHz band is not allowed without country code", new Object[0]);
            return 2;
        } else {
            if (wifiConfig.apChannel == 0) {
                setApBandToCoexChr(wifiConfig.apBand);
                wifiConfig.apChannel = ApConfigUtil.chooseApChannel(wifiConfig.apBand, getAllowed2GChannels(allowed24GChannels), getFreqListFor5G());
                setApBandToCoexChr(-1);
                if (wifiConfig.apChannel == -1) {
                    wifiConfig.apBand = 0;
                    wifiConfig.apChannel = 6;
                }
            }
            if (this.mHwWifiCHRService != null) {
                Bundle data = new Bundle();
                data.putInt("apBand", wifiConfig.apBand);
                data.putString("apRat", getCurrentBand());
                data.putInt("apChannel", wifiConfig.apChannel);
                this.mHwWifiCHRService.uploadDFTEvent(2, data);
            }
            if (DBG) {
                HwHiLog.d(TAG, false, "updateApChannelConfig apChannel: %{public}d", new Object[]{Integer.valueOf(wifiConfig.apChannel)});
            }
            setMssFor2GChannels(wifiConfig);
            this.mHwPhoneCloneChr.updateApConfiguration(wifiConfig);
            return 0;
        }
    }

    public int getApChannel(WifiConfiguration config) {
        int apChannel;
        if (config.apBand != 0 || !SystemProperties.getBoolean(ISM_COEX_ON, false)) {
            apChannel = Settings.Secure.getInt(this.mContext.getContentResolver(), "wifi_ap_channel", 0);
            if (apChannel == 0 || ((config.apBand == 0 && apChannel > 14) || (config.apBand == 1 && apChannel < 34))) {
                apChannel = config.apChannel;
            }
        } else {
            apChannel = 11;
        }
        HwHiLog.d(TAG, false, "softap channel=%{public}d", new Object[]{Integer.valueOf(apChannel)});
        return apChannel;
    }

    public void notifyApLinkedStaListChange(Bundle bundle) {
        if (bundle == null) {
            HwHiLog.e(TAG, false, "notifyApLinkedStaListChange: get bundle is null", new Object[0]);
            return;
        }
        int macHashCode = 0;
        String macAddress = bundle.getString("mac_key");
        if (macAddress != null) {
            macHashCode = macAddress.hashCode();
        }
        String event = bundle.getString("event_key");
        Message msg = Message.obtain();
        msg.what = macHashCode;
        msg.obj = event;
        msg.setData(bundle);
        if ("STA_JOIN".equals(event)) {
            this.mApLinkedStaChangedHandler.sendMessageDelayed(msg, 5000);
        } else if ("STA_LEAVE".equals(event)) {
            this.mApLinkedStaChangedHandler.sendMessage(msg);
        }
        HwHiLog.d(TAG, false, "Message sent to ApLinkedStaChangedHandler,event= %{public}s, mac=%{private}s", new Object[]{event, anonyMac(macAddress)});
    }

    private String[] getApLinkedMacListByNative() {
        HwHiLog.d(TAG, false, "getApLinkedMacListByNative is called", new Object[0]);
        String softapClients = WifiInjector.getInstance().getWifiNative().mHwWifiNativeEx.getSoftapClientsHw();
        if (softapClients != null && !softapClients.isEmpty()) {
            return softapClients.split("\\n");
        }
        HwHiLog.e(TAG, false, "getApLinkedMacListByNative Error: getSoftapClientsHw return NULL or empty string", new Object[0]);
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateLinkedInfo() {
        String[] macList = getApLinkedMacListByNative();
        this.mMacList = new ArrayList();
        if (macList == null) {
            this.mLinkedStaCount = 0;
            return;
        }
        for (String mac : macList) {
            if (mac == null) {
                HwHiLog.e(TAG, false, "get mac from macList is null", new Object[0]);
            } else {
                this.mMacList.add(mac.toLowerCase(Locale.ENGLISH));
            }
        }
        this.mLinkedStaCount = this.mMacList.size();
    }

    public List<String> getApLinkedStaList() {
        HwHiLog.d(TAG, false, "getApLinkedStaList is called", new Object[0]);
        List<String> list = this.mMacList;
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> dhcpList = readSoftapStaDhcpInfo();
        List<String> infoList = new ArrayList<>();
        int macListSize = this.mMacList.size();
        for (int index = 0; index < macListSize; index++) {
            infoList.add(getApLinkedStaInfo(this.mMacList.get(index), dhcpList));
        }
        HwHiLog.d(TAG, false, "getApLinkedStaList: info size=%{public}d", new Object[]{Integer.valueOf(infoList.size())});
        return infoList;
    }

    @Deprecated
    private List<String> getApLinkedDhcpList() {
        HwHiLog.d(TAG, false, "getApLinkedDhcpList: softap getdhcplease", new Object[0]);
        String softapDhcpLease = WifiInjector.getInstance().getWifiNative().mHwWifiNativeEx.readSoftapDhcpLeaseFileHw();
        if (softapDhcpLease == null || softapDhcpLease.isEmpty()) {
            HwHiLog.e(TAG, false, "getApLinkedDhcpList Error: readSoftapDhcpLeaseFileHw return NULL or empty string", new Object[0]);
            return null;
        }
        String[] dhcpleaseList = softapDhcpLease.split("\\n");
        List<String> dhcpList = new ArrayList<>();
        for (String dhcplease : dhcpleaseList) {
            dhcpList.add(dhcplease);
        }
        HwHiLog.d(TAG, false, "getApLinkedDhcpList: mDhcpList size=%{public}d", new Object[]{Integer.valueOf(dhcpList.size())});
        return dhcpList;
    }

    public static List<String> readSoftapStaDhcpInfo() {
        List<String> dhcpInfos = new ArrayList<>();
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try {
            fileReader = new FileReader(PATH_DHCP_FILE);
            bufferedReader = new BufferedReader(fileReader);
            while (true) {
                String line = bufferedReader.readLine();
                if (line == null) {
                    break;
                }
                dhcpInfos.add(line);
            }
        } catch (FileNotFoundException e) {
            HwHiLog.e(TAG, false, "Failed to read file %{public}s, message: %{public}s", new Object[]{PATH_DHCP_FILE, e.getMessage()});
        } catch (IOException e2) {
            HwHiLog.e(TAG, false, "Failed to read softap sta dhcp info: %{public}s", new Object[]{e2.getMessage()});
        } catch (Throwable th) {
            IoUtils.closeQuietly((AutoCloseable) null);
            IoUtils.closeQuietly((AutoCloseable) null);
            throw th;
        }
        IoUtils.closeQuietly(bufferedReader);
        IoUtils.closeQuietly(fileReader);
        return dhcpInfos;
    }

    public static String getApLinkedStaInfo(String mac, List<String> dhcpList) {
        String apLinkedStaInfo = String.format("MAC=%s", mac);
        String mac2 = mac.toLowerCase(Locale.ENGLISH);
        if (dhcpList != null) {
            for (String dhcplease : dhcpList) {
                if (dhcplease.contains(mac2)) {
                    String[] tokens = dhcplease.split(" ");
                    if (4 <= tokens.length) {
                        HwHiLog.d(TAG, false, "getApLinkedStaInfo: dhcplease token", new Object[0]);
                        apLinkedStaInfo = String.format(apLinkedStaInfo + " IP=%s DEVICE=%s", tokens[2], tokens[3]);
                    }
                }
            }
        }
        return apLinkedStaInfo;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String anonyMac(String mac) {
        if (mac == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(mac);
        sb.replace(0, 11, ANONYMOUS_MAC);
        return sb.toString();
    }

    public boolean isHideBroadcastSsid() {
        Context context = this.mContext;
        if (context == null) {
            HwHiLog.e(TAG, false, "error mContext is null", new Object[0]);
            return false;
        } else if (1 != Settings.System.getInt(context.getContentResolver(), "show_broadcast_ssid_config", 0)) {
            return false;
        } else {
            boolean isHideSsid = Settings.Secure.getInt(this.mContext.getContentResolver(), "wifi_ap_ignorebroadcastssid", 0) != 0;
            HwHiLog.i(TAG, false, "isHideSsid = %{public}s", new Object[]{String.valueOf(isHideSsid)});
            return isHideSsid;
        }
    }

    public void clearCallbacksAndMessages() {
        this.mApLinkedStaChangedHandler.removeCallbacksAndMessages(null);
    }

    /* access modifiers changed from: protected */
    public void updateApState(int newState, int currentState, int reason) {
        if (newState == 13) {
            handleSetWifiApConfigurationHw();
        }
        HwSoftApManager.super.updateApState(newState, currentState, reason);
    }

    private void handleSetWifiApConfigurationHw() {
        String apChannel = String.valueOf(getApChannel(this.mApConfig));
        int maxScb = Settings.Secure.getInt(this.mContext.getContentResolver(), "wifi_ap_maxscb", 8);
        HwHiLog.d(TAG, false, "HandleSetWifiApConfigurationHw is called, channel:%{public}s maxScb: %{public}d", new Object[]{apChannel, Integer.valueOf(maxScb)});
        if (!WifiInjector.getInstance().getWifiNative().mHwWifiNativeEx.setSoftapHw(apChannel, String.valueOf(maxScb))) {
            HwHiLog.e(TAG, false, "Failed to setSoftapHw", new Object[0]);
        }
    }

    /* access modifiers changed from: package-private */
    public void setSoftApDisassociateSta(String mac) {
        if (!WifiInjector.getInstance().getWifiNative().mHwWifiNativeEx.disassociateSoftapStaHw(mac)) {
            HwHiLog.e(TAG, false, "Failed to disassociateSoftapStaHw, mac: %{private}s", new Object[]{mac});
        }
    }

    /* access modifiers changed from: package-private */
    public void setSoftapMacFilter(String macFilter) {
        if (!WifiInjector.getInstance().getWifiNative().mHwWifiNativeEx.setSoftapMacFltrHw(macFilter)) {
            HwHiLog.e(TAG, false, "Failed to setSoftapMacFltrHw, macFilter: %{private}s", new Object[]{macFilter});
            return;
        }
        if (this.mHwWifiCHRService != null && !TextUtils.isEmpty(this.mMacFilterStr) && !TextUtils.isEmpty(macFilter) && !this.mMacFilterStr.equals(macFilter)) {
            String[] macFilterStrs = macFilter.split(SPLIT_DOT);
            if (macFilterStrs.length < 2) {
                HwHiLog.e(TAG, false, "length of macFilterStrs is not enough ", new Object[0]);
                return;
            }
            String[] macFilterCountStrs = macFilterStrs[1].split(SPLIT_EQUAL);
            if (macFilterCountStrs.length < 2) {
                HwHiLog.e(TAG, false, "length of macFilterCntStrs is not enough ", new Object[0]);
                return;
            }
            try {
                int macFilterCount = Integer.parseInt(macFilterCountStrs[1]);
                HwHiLog.d(TAG, false, "setSoftapMacFilter count = %{public}d, existed=%{public}d", new Object[]{Integer.valueOf(macFilterCount), Integer.valueOf(this.mMacFilterStaCount)});
                if (macFilterCount > 0 && macFilterCount >= this.mMacFilterStaCount) {
                    Bundle data = new Bundle();
                    data.putInt(SOFTAP_BLACKLIST_COUNT, 1);
                    this.mHwWifiCHRService.uploadDFTEvent(23, data);
                }
                this.mMacFilterStaCount = macFilterCount;
            } catch (NumberFormatException e) {
                HwHiLog.e(TAG, false, "Exception happens", new Object[0]);
                return;
            }
        }
        this.mMacFilterStr = macFilter;
    }

    static int[] getSoftApChannelListFor5G() {
        int[] channels = SoftApChannelXmlParse.convertFrequencyListToChannel(getAllowed5GChannels(WifiInjector.getInstance().getWifiNative()));
        if (shouldUseLiteUi()) {
            return filterRilCoexChannels(get5GChannelsWithoutIndoor(channels));
        }
        int[] filteredChannels = filterRilCoexChannels(channels);
        HwHiLog.d(TAG, false, "Got channels for 5G band: %{public}s", new Object[]{Arrays.toString(filteredChannels)});
        return filteredChannels;
    }

    public static int[] getChannelListFor5GWithoutIndoor() {
        return get5GChannelsWithoutIndoor(SoftApChannelXmlParse.convertFrequencyListToChannel(getAllowed5GChannels(WifiInjector.getInstance().getWifiNative())));
    }

    private static int[] get5GChannelsWithoutIndoor(int[] channels) {
        if (channels == null || channels.length == 0) {
            return channels;
        }
        ClientModeImpl wsm = WifiInjector.getInstance().getClientModeImpl();
        if (!(wsm instanceof HwWifiStateMachine)) {
            return channels;
        }
        int[] channelsWithoutIndoorChannels = ((HwWifiStateMachine) wsm).getSoftApChannelXmlParse().getChannelListWithoutIndoor(channels, WifiInjector.getInstance().getWifiCountryCode().getCountryCodeSentToDriver());
        HwHiLog.d(TAG, false, "Got channels without indoor for 5G band: %{public}s", new Object[]{Arrays.toString(channelsWithoutIndoorChannels)});
        return channelsWithoutIndoorChannels;
    }

    private int[] getFreqListFor5G() {
        return SoftApChannelXmlParse.convertChannelListToFrequency(getSoftApChannelListFor5G());
    }

    public static boolean shouldUseLiteUi() {
        boolean isUseLiteUi = SystemProperties.getBoolean(PROP_NET_SHARE_UI, true);
        HwHiLog.d(TAG, false, "shouldUseLiteUi: %{public}s", new Object[]{String.valueOf(isUseLiteUi)});
        return isUseLiteUi;
    }

    private static void setApBandToCoexChr(int band) {
        HiCoexChrImpl hiCoexChrImpl = HiCoexChrImpl.getInstance();
        if (hiCoexChrImpl != null) {
            hiCoexChrImpl.setApBand(band);
        }
    }

    private static int[] filterRilCoexChannels(int[] channels) {
        HiCoexManagerImpl hiCoexManagerImpl;
        if (channels == null || channels.length == 0 || (hiCoexManagerImpl = HiCoexManagerImpl.getHiCoexManagerImpl()) == null) {
            return channels;
        }
        HiCoexChrImpl hiCoexChrImpl = HiCoexChrImpl.getInstance();
        List<Integer> deprecatedChannels = hiCoexManagerImpl.getDeprecatedWiFiChannel();
        if (deprecatedChannels == null || deprecatedChannels.size() == 0) {
            hiCoexChrImpl.updateApChannelOptimization(1, false);
            return channels;
        }
        int[] filteredChannels = new int[channels.length];
        int count = 0;
        for (int channel : channels) {
            if (!deprecatedChannels.contains(Integer.valueOf(channel))) {
                filteredChannels[count] = channel;
                count++;
            }
        }
        if (count == 0 || count == channels.length) {
            hiCoexChrImpl.updateApChannelOptimization(1, false);
            return channels;
        }
        hiCoexChrImpl.updateApChannelOptimization(1, true);
        return Arrays.copyOf(filteredChannels, count);
    }

    private static void filterRilCoexChannels(List<Integer> oriChannels) {
        HiCoexManagerImpl hiCoexManagerImpl;
        if (!(oriChannels == null || oriChannels.size() == 0 || (hiCoexManagerImpl = HiCoexManagerImpl.getHiCoexManagerImpl()) == null)) {
            HiCoexChrImpl hiCoexChrImpl = HiCoexChrImpl.getInstance();
            List<Integer> deprecatedChannels = hiCoexManagerImpl.getDeprecatedWiFiChannel();
            if (deprecatedChannels == null || deprecatedChannels.size() == 0) {
                hiCoexChrImpl.updateApChannelOptimization(0, false);
                return;
            }
            ArrayList<Integer> channels = new ArrayList<>(oriChannels.size());
            channels.addAll(oriChannels);
            for (int i = channels.size() - 1; i >= 0; i--) {
                if (deprecatedChannels.contains(channels.get(i))) {
                    channels.remove(i);
                }
            }
            int size = channels.size();
            if (size == 0 || size == oriChannels.size()) {
                hiCoexChrImpl.updateApChannelOptimization(0, false);
                return;
            }
            oriChannels.clear();
            oriChannels.addAll(channels);
            hiCoexChrImpl.updateApChannelOptimization(0, true);
        }
    }

    private void setFemCalibrateMode(boolean isEnable) {
        if (this.mWifiNative == null) {
            HwHiLog.d(TAG, false, "mWifiNative is null", new Object[0]);
            return;
        }
        byte[] parameters = {1};
        if (!isEnable && this.mApConfig != null && this.mApConfig.apBandwidth == 8) {
            parameters[0] = 0;
        }
        this.mWifiNative.mHwWifiNativeEx.sendCmdToDriver(COMM_IFACE, (int) CMD_CALI_FEM_MODE, parameters);
    }
}
