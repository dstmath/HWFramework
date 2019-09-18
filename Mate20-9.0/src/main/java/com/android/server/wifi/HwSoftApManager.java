package com.android.server.wifi;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
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
import android.util.Log;
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
    private static final String ACTION_WIFI_AP_STA_JOIN = "android.net.wifi.WIFI_AP_STA_JOIN";
    private static final String ACTION_WIFI_AP_STA_LEAVE = "android.net.wifi.WIFI_AP_STA_LEAVE";
    private static final String ANONYMOUS_MAC = "**:**:**:**";
    private static final int ANONYMOUS_MAC_INDEX = 11;
    private static final int BROADCAST_SSID_MENU_DISPLAY = 1;
    private static final int BROADCAST_SSID_MENU_HIDE = 0;
    /* access modifiers changed from: private */
    public static boolean DBG = HWFLOW;
    private static final int DEFAULT_ISMCOEX_WIFI_AP_CHANNEL = 11;
    private static final int DEFAULT_WIFI_AP_CHANNEL = 0;
    private static final int DEFAULT_WIFI_AP_MAXSCB = 8;
    private static final String EXTRA_CURRENT_TIME = "currentTime";
    private static final String EXTRA_STA_COUNT = "staCount";
    private static final String EXTRA_STA_INFO = "macInfo";
    protected static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final String ISM_COEX_ON = "ro.config.hw_ismcoex";
    private static final int MAX_AP_LINKED_COUNT = 8;
    private static final int MIN_DHCPLEASE_LENGTH = 4;
    private static final int NT_CHINA_CMCC = 3;
    private static final int NT_CHINA_UT = 2;
    private static final int NT_FOREIGN = 1;
    private static final int NT_UNREG = 0;
    private static final String PATH_DHCP_FILE = "/data/misc/dhcp/dnsmasq.leases";
    private static final String PROP_NET_SHARE_UI = "ro.feature.mobile_network_sharing_lite";
    private static final int SEND_BROADCAST = 0;
    private static final int STA_JOIN_HANDLE_DELAY = 5000;
    private static final String TAG = "HwSoftApManager";
    /* access modifiers changed from: private */
    public Handler mApLinkedStaChangedHandler = new Handler() {
        public void handleMessage(Message msg) {
            String action = null;
            long mCurrentTime = System.currentTimeMillis();
            Bundle bundle = msg.getData();
            String event = bundle.getString("event_key");
            String macAddress = bundle.getString("mac_key").toLowerCase(Locale.ENGLISH);
            if ("STA_JOIN".equals(event)) {
                action = HwSoftApManager.ACTION_WIFI_AP_STA_JOIN;
                if (!HwSoftApManager.this.mMacList.contains(macAddress)) {
                    HwSoftApManager.this.mMacList.add(macAddress);
                    int unused = HwSoftApManager.this.mLinkedStaCount = HwSoftApManager.this.mLinkedStaCount + 1;
                } else {
                    Log.e(HwSoftApManager.TAG, HwSoftApManager.this.anonyMac(macAddress) + " had been added, but still get event " + event);
                    HwSoftApManager.this.updateLinkedInfo();
                }
            } else if ("STA_LEAVE".equals(event)) {
                action = HwSoftApManager.ACTION_WIFI_AP_STA_LEAVE;
                if (HwSoftApManager.this.mApLinkedStaChangedHandler.hasMessages(msg.what, "STA_JOIN") || HwSoftApManager.this.mMacList.contains(macAddress)) {
                    if (HwSoftApManager.this.mApLinkedStaChangedHandler.hasMessages(msg.what, "STA_JOIN")) {
                        HwSoftApManager.this.mApLinkedStaChangedHandler.removeMessages(msg.what, "STA_JOIN");
                        Log.d(HwSoftApManager.TAG, "event=" + event + ", remove STA_JOIN message, mac=" + HwSoftApManager.this.anonyMac(macAddress));
                    }
                    if (HwSoftApManager.this.mMacList.contains(macAddress)) {
                        HwSoftApManager.this.mMacList.remove(macAddress);
                        HwSoftApManager.access$110(HwSoftApManager.this);
                    }
                } else {
                    Log.e(HwSoftApManager.TAG, HwSoftApManager.this.anonyMac(macAddress) + " had been removed, but still get event " + event);
                    HwSoftApManager.this.updateLinkedInfo();
                }
            }
            Log.d(HwSoftApManager.TAG, "ApLinkedStaChanged message handled, event=" + event + " mac=" + HwSoftApManager.this.anonyMac(macAddress) + ", mLinkedStaCount=" + HwSoftApManager.this.mLinkedStaCount);
            if (HwSoftApManager.this.mLinkedStaCount < 0 || HwSoftApManager.this.mLinkedStaCount > 8 || HwSoftApManager.this.mLinkedStaCount != HwSoftApManager.this.mMacList.size()) {
                Log.e(HwSoftApManager.TAG, "mLinkedStaCount over flow, need synchronize. mLinkedStaCount=" + HwSoftApManager.this.mLinkedStaCount + ", mMacList.size()=" + HwSoftApManager.this.mMacList.size());
                HwSoftApManager.this.updateLinkedInfo();
            }
            String staInfo = String.format("MAC=%s TIME=%d STACNT=%d", new Object[]{HwSoftApManager.this.anonyMac(macAddress), Long.valueOf(mCurrentTime), Integer.valueOf(HwSoftApManager.this.mLinkedStaCount)});
            Log.d(HwSoftApManager.TAG, "Send broadcast: " + action + ", event=" + event + ", extraInfo: " + staInfo);
            Intent broadcast = new Intent(action);
            broadcast.addFlags(16777216);
            broadcast.putExtra(HwSoftApManager.EXTRA_STA_INFO, macAddress);
            broadcast.putExtra(HwSoftApManager.EXTRA_CURRENT_TIME, mCurrentTime);
            broadcast.putExtra(HwSoftApManager.EXTRA_STA_COUNT, HwSoftApManager.this.mLinkedStaCount);
            HwSoftApManager.this.mContext.sendBroadcast(broadcast, "android.permission.ACCESS_WIFI_STATE");
        }
    };
    /* access modifiers changed from: private */
    public Context mContext;
    private int mDataSub = -1;
    private HwWifiCHRService mHwWifiCHRService;
    /* access modifiers changed from: private */
    public int mLinkedStaCount = 0;
    /* access modifiers changed from: private */
    public List<String> mMacList = new ArrayList();
    /* access modifiers changed from: private */
    public String mOperatorNumericSub0 = null;
    /* access modifiers changed from: private */
    public String mOperatorNumericSub1 = null;
    private PhoneStateListener[] mPhoneStateListener;
    /* access modifiers changed from: private */
    public int mServiceStateSub0 = 1;
    /* access modifiers changed from: private */
    public int mServiceStateSub1 = 1;
    private TelephonyManager mTelephonyManager;
    private WifiChannelXmlParse mWifiChannelXmlParse = null;

    static /* synthetic */ int access$110(HwSoftApManager x0) {
        int i = x0.mLinkedStaCount;
        x0.mLinkedStaCount = i - 1;
        return i;
    }

    public HwSoftApManager(Context context, Looper looper, FrameworkFacade frameworkFacade, WifiNative wifiNative, String countryCode, WifiManager.SoftApCallback callback, WifiApConfigStore wifiApConfigStore, SoftApModeConfiguration config, WifiMetrics wifiMetrics) {
        super(context, looper, frameworkFacade, wifiNative, countryCode, callback, wifiApConfigStore, config, wifiMetrics);
        this.mContext = context;
        this.mHwWifiCHRService = HwWifiServiceFactory.getHwWifiCHRService();
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
            public void onServiceStateChanged(ServiceState state) {
                if (state != null) {
                    if (HwSoftApManager.DBG) {
                        Log.d(HwSoftApManager.TAG, "PhoneStateListener " + this.mSubId);
                    }
                    if (this.mSubId.intValue() == 0) {
                        int unused = HwSoftApManager.this.mServiceStateSub0 = state.getDataRegState();
                        String unused2 = HwSoftApManager.this.mOperatorNumericSub0 = state.getOperatorNumeric();
                    } else if (this.mSubId.intValue() == 1) {
                        int unused3 = HwSoftApManager.this.mServiceStateSub1 = state.getDataRegState();
                        String unused4 = HwSoftApManager.this.mOperatorNumericSub1 = state.getOperatorNumeric();
                    }
                }
            }
        };
    }

    private int getRegistedNetworkType() {
        String numeric;
        int serviceState;
        if (this.mDataSub == 0) {
            serviceState = this.mServiceStateSub0;
            numeric = this.mOperatorNumericSub0;
        } else if (this.mDataSub != 1) {
            return 0;
        } else {
            serviceState = this.mServiceStateSub0;
            numeric = this.mOperatorNumericSub0;
        }
        Log.d(TAG, "isRegistedNetworkType mDataSub " + this.mDataSub + ", serviceState " + serviceState + " , numeric " + numeric);
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

    private static String getCurrentBand() {
        String ret = null;
        String[] bandrst = HwTelephonyManagerInner.getDefault().queryServiceCellBand();
        if (bandrst != null) {
            if (bandrst.length < 2) {
                if (DBG) {
                    Log.d(TAG, "getCurrentBand bandrst error.");
                }
                return null;
            } else if ("GSM".equals(bandrst[0])) {
                switch (Integer.parseInt(bandrst[1])) {
                    case 0:
                        ret = "GSM850";
                        break;
                    case 1:
                        ret = "GSM900";
                        break;
                    case 2:
                        ret = "GSM1800";
                        break;
                    case 3:
                        ret = "GSM1900";
                        break;
                    default:
                        Log.e(TAG, "should not be here.");
                        break;
                }
            } else if ("CDMA".equals(bandrst[0])) {
                ret = "BC0";
            } else {
                ret = bandrst[0] + bandrst[1];
            }
        }
        if (DBG) {
            Log.d(TAG, "getCurrentBand rst is " + ret);
        }
        return ret;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v5, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v3, resolved type: java.util.ArrayList<java.lang.Integer>} */
    /* JADX WARNING: Multi-variable type inference failed */
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
            intersectChannels = allowedChannels.clone();
            if (vaildChannels != null) {
                intersectChannels.retainAll(vaildChannels);
            }
            if (intersectChannels.size() == 0) {
                intersectChannels = allowedChannels;
            }
        } else {
            intersectChannels = allowedChannels;
        }
        if (DBG) {
            StringBuilder sb = new StringBuilder();
            sb.append("channels: ");
            Iterator<Integer> it = intersectChannels.iterator();
            while (it.hasNext()) {
                sb.append(it.next().toString() + ",");
            }
            Log.d(TAG, "2G " + sb);
        }
        return intersectChannels;
    }

    /* JADX WARNING: Removed duplicated region for block: B:23:0x004b  */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0098  */
    private static int[] getAllowed5GChannels(WifiNative wifiNative) {
        int counter;
        int[] allowedChannels = wifiNative.getChannelsForBand(2);
        if (allowedChannels == null || allowedChannels.length <= 1) {
            return allowedChannels;
        }
        int[] values = new int[allowedChannels.length];
        int i = 0;
        ArrayList<Integer> vaildChannels = WifiChannelXmlParse.getInstance().getValidChannels(getCurrentBand(), false);
        if (vaildChannels != null) {
            counter = 0;
            int i2 = 0;
            while (i2 < allowedChannels.length) {
                try {
                    if (vaildChannels.contains(Integer.valueOf(ApConfigUtil.convertFrequencyToChannel(allowedChannels[i2])))) {
                        int counter2 = counter + 1;
                        try {
                            values[counter] = allowedChannels[i2];
                            counter = counter2;
                        } catch (Exception e) {
                            e = e;
                            counter = counter2;
                            e.printStackTrace();
                            if (counter == 0) {
                            }
                        }
                    }
                    i2++;
                } catch (Exception e2) {
                    e = e2;
                    e.printStackTrace();
                    if (counter == 0) {
                    }
                }
            }
        } else {
            counter = 0;
        }
        if (counter == 0) {
            Log.d(TAG, "5G counter is 0");
            if (DBG) {
                StringBuilder sb = new StringBuilder();
                sb.append("allowedChannels channels: ");
                while (i < allowedChannels.length) {
                    sb.append(ApConfigUtil.convertFrequencyToChannel(allowedChannels[i]) + ",");
                    i++;
                }
                Log.d(TAG, "5G " + sb);
            }
            return allowedChannels;
        }
        int[] intersectChannels = new int[counter];
        for (int i3 = 0; i3 < counter; i3++) {
            intersectChannels[i3] = values[i3];
        }
        if (DBG != 0) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append("allowedChannels channels: ");
            for (int i4 = 0; i4 < allowedChannels.length; i4++) {
                sb2.append(ApConfigUtil.convertFrequencyToChannel(allowedChannels[i4]) + ",");
            }
            sb2.append("intersectChannels channels: ");
            while (i < counter) {
                sb2.append(ApConfigUtil.convertFrequencyToChannel(intersectChannels[i]) + ",");
                i++;
            }
            Log.d(TAG, "5G " + sb2.toString());
        }
        return intersectChannels;
    }

    public void start() {
        HwSoftApManager.super.start();
        HwWifiStateMachine wifiStateMachine = WifiInjector.getInstance().getWifiStateMachine();
        if (wifiStateMachine instanceof HwWifiStateMachine) {
            wifiStateMachine.registHwSoftApManager(this);
        }
    }

    public void stop() {
        HwSoftApManager.super.stop();
        HwWifiStateMachine wifiStateMachine = WifiInjector.getInstance().getWifiStateMachine();
        if (wifiStateMachine instanceof HwWifiStateMachine) {
            wifiStateMachine.clearHwSoftApManager();
        }
    }

    public int updateApChannelConfig(WifiNative wifiNative, String countryCode, ArrayList<Integer> allowed2GChannels, WifiConfiguration config) {
        if (!wifiNative.isHalStarted()) {
            config.apBand = 0;
            config.apChannel = 6;
            return 0;
        } else if (config.apBand == 1 && countryCode == null) {
            Log.e(TAG, "5GHz band is not allowed without country code");
            return 2;
        } else {
            if (config.apChannel == 0) {
                config.apChannel = ApConfigUtil.chooseApChannel(config.apBand, getAllowed2GChannels(allowed2GChannels), getFreqListFor5G());
                if (config.apChannel == -1) {
                    config.apBand = 0;
                    config.apChannel = 6;
                }
            }
            if (this.mHwWifiCHRService != null) {
                Bundle data = new Bundle();
                data.putInt("apBand", config.apBand);
                data.putString("apRat", getCurrentBand());
                data.putInt("apChannel", config.apChannel);
                this.mHwWifiCHRService.uploadDFTEvent(2, data);
            }
            if (DBG) {
                Log.d(TAG, "updateApChannelConfig apChannel: " + config.apChannel);
            }
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
        Log.d(TAG, "softap channel=" + apChannel);
        return apChannel;
    }

    public void notifyApLinkedStaListChange(Bundle bundle) {
        if (bundle == null) {
            Log.e(TAG, "notifyApLinkedStaListChange: get bundle is null");
            return;
        }
        int macHashCode = 0;
        String macAddress = bundle.getString("mac_key");
        if (macAddress != null) {
            macHashCode = macAddress.hashCode();
        }
        String event = bundle.getString("event_key");
        Message msg = new Message();
        msg.what = macHashCode;
        msg.obj = event;
        msg.setData(bundle);
        if ("STA_JOIN".equals(event)) {
            this.mApLinkedStaChangedHandler.sendMessageDelayed(msg, 5000);
        } else if ("STA_LEAVE".equals(event)) {
            this.mApLinkedStaChangedHandler.sendMessage(msg);
        }
        Log.d(TAG, "Message sent to ApLinkedStaChangedHandler,event= " + event + " , mac=" + anonyMac(macAddress));
    }

    private String[] getApLinkedMacListByNative() {
        Log.d(TAG, "getApLinkedMacListByNative is called");
        String softapClients = WifiInjector.getInstance().getWifiNative().getSoftapClientsHw();
        if (softapClients != null && !softapClients.isEmpty()) {
            return softapClients.split("\\n");
        }
        Log.e(TAG, "getApLinkedMacListByNative Error: getSoftapClientsHw return NULL or empyt string");
        return null;
    }

    /* access modifiers changed from: private */
    public void updateLinkedInfo() {
        String[] macList = getApLinkedMacListByNative();
        this.mMacList = new ArrayList();
        if (macList == null) {
            this.mLinkedStaCount = 0;
            return;
        }
        for (String mac : macList) {
            if (mac == null) {
                Log.e(TAG, "get mac from macList is null");
            } else {
                this.mMacList.add(mac.toLowerCase(Locale.ENGLISH));
            }
        }
        this.mLinkedStaCount = this.mMacList.size();
    }

    public List<String> getApLinkedStaList() {
        Log.d(TAG, "getApLinkedStaList is called");
        if (this.mMacList == null || this.mMacList.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> dhcpList = readSoftapStaDhcpInfo();
        List<String> infoList = new ArrayList<>();
        int macListSize = this.mMacList.size();
        for (int index = 0; index < macListSize; index++) {
            infoList.add(getApLinkedStaInfo(this.mMacList.get(index), dhcpList));
        }
        Log.d(TAG, "getApLinkedStaList: info size=" + infoList.size());
        return infoList;
    }

    @Deprecated
    private List<String> getApLinkedDhcpList() {
        Log.d(TAG, "getApLinkedDhcpList: softap getdhcplease");
        String softapDhcpLease = WifiInjector.getInstance().getWifiNative().readSoftapDhcpLeaseFileHw();
        if (softapDhcpLease == null || softapDhcpLease.isEmpty()) {
            Log.e(TAG, "getApLinkedDhcpList Error: readSoftapDhcpLeaseFileHw return NULL or empty string");
            return null;
        }
        String[] dhcpleaseList = softapDhcpLease.split("\\n");
        List<String> dhcpList = new ArrayList<>();
        for (String dhcplease : dhcpleaseList) {
            dhcpList.add(dhcplease);
        }
        Log.d(TAG, "getApLinkedDhcpList: mDhcpList size=" + dhcpList.size());
        return dhcpList;
    }

    private List<String> readSoftapStaDhcpInfo() {
        List<String> dhcpInfos = new ArrayList<>();
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try {
            fileReader = new FileReader(PATH_DHCP_FILE);
            bufferedReader = new BufferedReader(fileReader);
            while (true) {
                String readLine = bufferedReader.readLine();
                String line = readLine;
                if (readLine == null) {
                    break;
                }
                dhcpInfos.add(line);
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Failed to read file /data/misc/dhcp/dnsmasq.leases, message: " + e.getMessage());
        } catch (IOException e2) {
            Log.e(TAG, "Failed to read softap sta dhcp info: " + e2.getMessage());
        } catch (Throwable th) {
            IoUtils.closeQuietly(null);
            IoUtils.closeQuietly(null);
            throw th;
        }
        IoUtils.closeQuietly(bufferedReader);
        IoUtils.closeQuietly(fileReader);
        return dhcpInfos;
    }

    private String getApLinkedStaInfo(String mac, List<String> dhcpList) {
        String apLinkedStaInfo = String.format("MAC=%s", new Object[]{mac});
        String mac2 = mac.toLowerCase(Locale.ENGLISH);
        if (dhcpList != null) {
            for (String dhcplease : dhcpList) {
                if (dhcplease.contains(mac2)) {
                    String[] tokens = dhcplease.split(" ");
                    if (4 <= tokens.length) {
                        Log.d(TAG, "getApLinkedStaInfo: dhcplease token");
                        apLinkedStaInfo = String.format(apLinkedStaInfo + " IP=%s DEVICE=%s", new Object[]{tokens[2], tokens[3]});
                    }
                }
            }
        }
        return apLinkedStaInfo;
    }

    /* access modifiers changed from: private */
    public String anonyMac(String mac) {
        if (mac == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(mac);
        sb.replace(0, 11, ANONYMOUS_MAC);
        return sb.toString();
    }

    public boolean isHideBroadcastSsid() {
        boolean isHideSsid = false;
        if (this.mContext == null) {
            Log.e(TAG, "error mContext is null");
            return false;
        }
        boolean z = true;
        if (1 == Settings.System.getInt(this.mContext.getContentResolver(), "show_broadcast_ssid_config", 0)) {
            if (Settings.Secure.getInt(this.mContext.getContentResolver(), "wifi_ap_ignorebroadcastssid", 0) == 0) {
                z = false;
            }
            isHideSsid = z;
            Log.i(TAG, "isHideSsid = " + isHideSsid);
        }
        return isHideSsid;
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
        Log.d(TAG, "HandleSetWifiApConfigurationHw is called, channel:" + apChannel + " maxScb: " + maxScb);
        if (!WifiInjector.getInstance().getWifiNative().setSoftapHw(apChannel, String.valueOf(maxScb))) {
            Log.e(TAG, "Failed to setSoftapHw");
        }
    }

    /* access modifiers changed from: package-private */
    public void setSoftApDisassociateSta(String mac) {
        if (!WifiInjector.getInstance().getWifiNative().disassociateSoftapStaHw(mac)) {
            Log.e(TAG, "Failed to disassociateSoftapStaHw, mac: " + mac);
        }
    }

    /* access modifiers changed from: package-private */
    public void setSoftapMacFilter(String macFilter) {
        if (!WifiInjector.getInstance().getWifiNative().setSoftapMacFltrHw(macFilter)) {
            Log.e(TAG, "Failed to setSoftapMacFltrHw, macFilter: " + macFilter);
        }
    }

    static int[] getSoftApChannelListFor5G() {
        int[] channels = SoftApChannelXmlParse.convertFrequencyListToChannel(getAllowed5GChannels(WifiInjector.getInstance().getWifiNative()));
        if (shouldUseLiteUi()) {
            return get5GChannelsWithoutIndoor(channels);
        }
        Log.d(TAG, "Got channels for 5G band: " + Arrays.toString(channels));
        return channels;
    }

    static int[] getChannelListFor5GWithoutIndoor() {
        return get5GChannelsWithoutIndoor(SoftApChannelXmlParse.convertFrequencyListToChannel(getAllowed5GChannels(WifiInjector.getInstance().getWifiNative())));
    }

    private static int[] get5GChannelsWithoutIndoor(int[] channels) {
        if (channels == null || channels.length == 0) {
            return channels;
        }
        HwWifiStateMachine wifiStateMachine = WifiInjector.getInstance().getWifiStateMachine();
        if (!(wifiStateMachine instanceof HwWifiStateMachine)) {
            return channels;
        }
        int[] channelsWithoutIndoorChannels = wifiStateMachine.getSoftApChannelXmlParse().getChannelListWithoutIndoor(channels, WifiInjector.getInstance().getWifiCountryCode().getCountryCodeSentToDriver());
        Log.d(TAG, "Got channels without indoor for 5G band: " + Arrays.toString(channelsWithoutIndoorChannels));
        return channelsWithoutIndoorChannels;
    }

    private int[] getFreqListFor5G() {
        return SoftApChannelXmlParse.convertChannelListToFrequency(getSoftApChannelListFor5G());
    }

    public static boolean shouldUseLiteUi() {
        boolean isUseLiteUi = SystemProperties.getBoolean(PROP_NET_SHARE_UI, true);
        Log.d(TAG, "shouldUseLiteUi: " + isUseLiteUi);
        return isUseLiteUi;
    }
}
