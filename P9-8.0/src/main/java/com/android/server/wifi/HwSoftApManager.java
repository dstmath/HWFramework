package com.android.server.wifi;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.IApInterface;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.os.Handler;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.server.wifi.SoftApManager.Listener;
import com.android.server.wifi.util.ApConfigUtil;
import java.util.ArrayList;
import java.util.List;

public class HwSoftApManager extends SoftApManager {
    private static final String ACTION_WIFI_AP_STA_JOIN = "android.net.wifi.WIFI_AP_STA_JOIN";
    private static final String ACTION_WIFI_AP_STA_LEAVE = "android.net.wifi.WIFI_AP_STA_LEAVE";
    private static final String ANONYMOUS_MAC = "**:**:**:**";
    private static final int ANONYMOUS_MAC_INDEX = 11;
    private static final int BROADCAST_SSID_MENU_DISPLAY = 1;
    private static final int BROADCAST_SSID_MENU_HIDE = 0;
    private static boolean DBG = HWFLOW;
    private static final int DEFAULT_ISMCOEX_WIFI_AP_CHANNEL = 11;
    private static final int DEFAULT_WIFI_AP_CHANNEL = 0;
    private static final String EXTRA_CURRENT_TIME = "currentTime";
    private static final String EXTRA_STA_COUNT = "staCount";
    private static final String EXTRA_STA_INFO = "macInfo";
    protected static final boolean HWFLOW;
    private static final String ISM_COEX_ON = "ro.config.hw_ismcoex";
    private static final int MAX_AP_LINKED_COUNT = 8;
    private static final int MIN_DHCPLEASE_LENGTH = 4;
    private static final int NT_CHINA_CMCC = 3;
    private static final int NT_CHINA_UT = 2;
    private static final int NT_FOREIGN = 1;
    private static final int NT_UNREG = 0;
    private static final int SEND_BROADCAST = 0;
    private static final int STA_JOIN_HANDLE_DELAY = 5000;
    private static final String TAG = "HwSoftApManager";
    private Handler mApLinkedStaChangedHandler = new Handler() {
        public void handleMessage(Message msg) {
            String action = null;
            long mCurrentTime = System.currentTimeMillis();
            Bundle bundle = msg.getData();
            String event = bundle.getString("event_key");
            String macAddress = bundle.getString("mac_key").toLowerCase();
            HwSoftApManager hwSoftApManager;
            if ("STA_JOIN".equals(event)) {
                action = HwSoftApManager.ACTION_WIFI_AP_STA_JOIN;
                if (HwSoftApManager.this.mMacList.contains(macAddress)) {
                    Log.e(HwSoftApManager.TAG, HwSoftApManager.this.anonyMac(macAddress) + " had been added, but still get event " + event);
                    HwSoftApManager.this.updateLinkedInfo();
                } else {
                    HwSoftApManager.this.mMacList.add(macAddress);
                    hwSoftApManager = HwSoftApManager.this;
                    hwSoftApManager.mLinkedStaCount = hwSoftApManager.mLinkedStaCount + 1;
                }
            } else if ("STA_LEAVE".equals(event)) {
                action = HwSoftApManager.ACTION_WIFI_AP_STA_LEAVE;
                if (HwSoftApManager.this.mMacList.contains(macAddress)) {
                    HwSoftApManager.this.mMacList.remove(macAddress);
                    hwSoftApManager = HwSoftApManager.this;
                    hwSoftApManager.mLinkedStaCount = hwSoftApManager.mLinkedStaCount - 1;
                } else if (HwSoftApManager.this.mApLinkedStaChangedHandler.hasMessages(msg.what, "STA_JOIN")) {
                    HwSoftApManager.this.mApLinkedStaChangedHandler.removeMessages(msg.what, "STA_JOIN");
                    Log.d(HwSoftApManager.TAG, "event=" + event + ", remove STA_JOIN message, mac=" + HwSoftApManager.this.anonyMac(macAddress));
                } else {
                    Log.e(HwSoftApManager.TAG, HwSoftApManager.this.anonyMac(macAddress) + " had been removed, but still get event " + event);
                    HwSoftApManager.this.updateLinkedInfo();
                }
            }
            Log.d(HwSoftApManager.TAG, "handle " + event + " event, mac=" + HwSoftApManager.this.anonyMac(macAddress) + ", mLinkedStaCount=" + HwSoftApManager.this.mLinkedStaCount);
            if (HwSoftApManager.this.mLinkedStaCount < 0 || HwSoftApManager.this.mLinkedStaCount > 8 || HwSoftApManager.this.mLinkedStaCount != HwSoftApManager.this.mMacList.size()) {
                Log.e(HwSoftApManager.TAG, "mLinkedStaCount over flow, need synchronize. mLinkedStaCount=" + HwSoftApManager.this.mLinkedStaCount + ", mMacList.size()=" + HwSoftApManager.this.mMacList.size());
                HwSoftApManager.this.updateLinkedInfo();
            }
            Log.e(HwSoftApManager.TAG, "send broadcast, event=" + event + ", extraInfo: " + String.format("MAC=%s TIME=%d STACNT=%d", new Object[]{HwSoftApManager.this.anonyMac(macAddress), Long.valueOf(mCurrentTime), Integer.valueOf(HwSoftApManager.this.mLinkedStaCount)}));
            Intent broadcast = new Intent(action);
            broadcast.addFlags(16777216);
            broadcast.putExtra(HwSoftApManager.EXTRA_STA_INFO, macAddress);
            broadcast.putExtra(HwSoftApManager.EXTRA_CURRENT_TIME, mCurrentTime);
            broadcast.putExtra(HwSoftApManager.EXTRA_STA_COUNT, HwSoftApManager.this.mLinkedStaCount);
            HwSoftApManager.this.mContext.sendBroadcast(broadcast, "android.permission.ACCESS_WIFI_STATE");
        }
    };
    private Context mContext;
    private int mDataSub = -1;
    private int mLinkedStaCount = 0;
    private List<String> mMacList = new ArrayList();
    private String mOperatorNumericSub0 = null;
    private String mOperatorNumericSub1 = null;
    private PhoneStateListener[] mPhoneStateListener;
    private int mServiceStateSub0 = 1;
    private int mServiceStateSub1 = 1;
    private TelephonyManager mTelephonyManager;
    private WifiChannelXmlParse mWifiChannelXmlParse = null;

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        HWFLOW = isLoggable;
    }

    public HwSoftApManager(Context context, Looper looper, WifiNative wifiNative, String countryCode, Listener listener, IApInterface apInterface, INetworkManagementService nms, WifiApConfigStore wifiApConfigStore, WifiConfiguration config, WifiMetrics wifiMetrics) {
        super(looper, wifiNative, countryCode, listener, apInterface, nms, wifiApConfigStore, config, wifiMetrics);
        this.mContext = context;
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
        int serviceState;
        String numeric;
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
            return (numeric == null || (numeric.equals("") ^ 1) == 0) ? 0 : 1;
        } else {
            if ("46000".equals(this.mOperatorNumericSub0) || "46002".equals(this.mOperatorNumericSub0) || "46007".equals(this.mOperatorNumericSub0)) {
                return 3;
            }
            return 2;
        }
    }

    private String getCurrentBand() {
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
            } else {
                ret = "CDMA".equals(bandrst[0]) ? "BC0" : bandrst[0] + bandrst[1];
            }
        }
        if (DBG) {
            Log.d(TAG, "getCurrentBand rst is " + ret);
        }
        return ret;
    }

    private ArrayList<Integer> getAllowed2GChannels(ArrayList<Integer> allowedChannels) {
        int networkType = getRegistedNetworkType();
        ArrayList<Integer> intersectChannels = new ArrayList();
        if (allowedChannels == null) {
            return null;
        }
        if (networkType == 3) {
            intersectChannels.add(Integer.valueOf(6));
        } else if (networkType == 2) {
            intersectChannels.add(Integer.valueOf(1));
            intersectChannels.add(Integer.valueOf(6));
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
        if (DBG) {
            StringBuilder sb = new StringBuilder();
            sb.append("channels: ");
            for (Integer channel : intersectChannels) {
                sb.append(channel.toString()).append(",");
            }
            Log.d(TAG, "2G " + sb);
        }
        return intersectChannels;
    }

    /* JADX WARNING: Removed duplicated region for block: B:29:0x0093  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0049  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int[] getAllowed5GChannels(WifiNative wifiNative) {
        Exception e;
        int[] allowedChannels = wifiNative.getChannelsForBand(2);
        if (allowedChannels == null || allowedChannels.length <= 1) {
            return allowedChannels;
        }
        int i;
        int[] values = new int[allowedChannels.length];
        this.mWifiChannelXmlParse = WifiChannelXmlParse.getInstance();
        ArrayList<Integer> vaildChannels = this.mWifiChannelXmlParse.getValidChannels(getCurrentBand(), false);
        int counter = 0;
        if (vaildChannels != null) {
            i = 0;
            while (true) {
                int counter2;
                try {
                    counter2 = counter;
                    if (i >= allowedChannels.length) {
                        counter = counter2;
                        break;
                    }
                    if (vaildChannels.contains(Integer.valueOf(ApConfigUtil.convertFrequencyToChannel(allowedChannels[i])))) {
                        counter = counter2 + 1;
                        try {
                            values[counter2] = allowedChannels[i];
                        } catch (Exception e2) {
                            e = e2;
                        }
                    } else {
                        counter = counter2;
                    }
                    i++;
                } catch (Exception e3) {
                    e = e3;
                    counter = counter2;
                    e.printStackTrace();
                    if (counter != 0) {
                    }
                }
            }
        }
        StringBuilder sb;
        if (counter != 0) {
            Log.d(TAG, "5G counter is 0");
            if (DBG) {
                sb = new StringBuilder();
                sb.append("allowedChannels channels: ");
                for (int convertFrequencyToChannel : allowedChannels) {
                    sb.append(ApConfigUtil.convertFrequencyToChannel(convertFrequencyToChannel)).append(",");
                }
                Log.d(TAG, "5G " + sb);
            }
            return allowedChannels;
        }
        int[] intersectChannels = new int[counter];
        for (i = 0; i < counter; i++) {
            intersectChannels[i] = values[i];
        }
        if (DBG) {
            sb = new StringBuilder();
            sb.append("allowedChannels channels: ");
            for (int convertFrequencyToChannel2 : allowedChannels) {
                sb.append(ApConfigUtil.convertFrequencyToChannel(convertFrequencyToChannel2)).append(",");
            }
            sb.append("intersectChannels channels: ");
            for (i = 0; i < counter; i++) {
                sb.append(ApConfigUtil.convertFrequencyToChannel(intersectChannels[i])).append(",");
            }
            Log.d(TAG, "5G " + sb.toString());
        }
        return intersectChannels;
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
                config.apChannel = ApConfigUtil.chooseApChannel(config.apBand, getAllowed2GChannels(allowed2GChannels), getAllowed5GChannels(wifiNative));
                if (config.apChannel == -1) {
                    if (wifiNative.isGetChannelsForBandSupported()) {
                        Log.e(TAG, "Failed to get available channel.");
                        return 1;
                    }
                    config.apBand = 0;
                    config.apChannel = 6;
                }
            }
            if (DBG) {
                Log.d(TAG, "updateApChannelConfig apChannel: " + config.apChannel);
            }
            return 0;
        }
    }

    public int getApChannel(WifiConfiguration config) {
        int apChannel;
        if (config.apBand == 0 && SystemProperties.getBoolean(ISM_COEX_ON, false)) {
            apChannel = 11;
        } else {
            apChannel = Secure.getInt(this.mContext.getContentResolver(), "wifi_ap_channel", 0);
            if (apChannel == 0 || ((config.apBand == 0 && apChannel > 14) || (config.apBand == 1 && apChannel < 34))) {
                apChannel = config.apChannel;
            }
        }
        Log.d(TAG, "channel=" + apChannel);
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
        Log.d(TAG, "send " + event + " message, mac=" + anonyMac(macAddress) + ", mLinkedStaCount=" + this.mLinkedStaCount);
    }

    private String[] getApLinkedMacListByNative() {
        Log.d(TAG, "getApLinkedMacListByNative is called");
        String softapClients = WifiInjector.getInstance().getWifiNative().getSoftapClientsHw();
        if (softapClients != null && !softapClients.isEmpty()) {
            return softapClients.split("\\n");
        }
        Log.e(TAG, "getApLinkedMacListByNative Error: getSoftapClientsHw return NULL");
        return null;
    }

    private void updateLinkedInfo() {
        int i = 0;
        String[] macList = getApLinkedMacListByNative();
        this.mMacList = new ArrayList();
        if (macList == null) {
            this.mLinkedStaCount = 0;
            return;
        }
        int length = macList.length;
        while (i < length) {
            String mac = macList[i];
            if (mac == null) {
                Log.e(TAG, "get mac from macList is null");
            } else {
                this.mMacList.add(mac.toLowerCase());
            }
            i++;
        }
        this.mLinkedStaCount = this.mMacList.size();
    }

    public List<String> getApLinkedStaList() {
        Log.d(TAG, "getApLinkedStaList is called");
        if (this.mMacList == null || this.mMacList.isEmpty()) {
            Log.d(TAG, "infoList return null");
            return null;
        }
        List<String> dhcpList = getApLinkedDhcpList();
        List<String> infoList = new ArrayList();
        for (int index = 0; index < this.mMacList.size(); index++) {
            infoList.add(getApLinkedStaInfo((String) this.mMacList.get(index), dhcpList));
        }
        Log.d(TAG, "getApLinkedStaList: info size=" + infoList.size());
        return infoList;
    }

    private List<String> getApLinkedDhcpList() {
        Log.d(TAG, "getApLinkedDhcpList: softap getdhcplease");
        String softapDhcpLease = WifiInjector.getInstance().getWifiNative().readSoftapDhcpLeaseFileHw();
        if (softapDhcpLease == null || softapDhcpLease.isEmpty()) {
            Log.e(TAG, "getApLinkedDhcpList Error: readSoftapDhcpLeaseFileHw return NULL");
            return null;
        }
        String[] dhcpleaseList = softapDhcpLease.split("\\n");
        List<String> dhcpList = new ArrayList();
        for (String dhcplease : dhcpleaseList) {
            dhcpList.add(dhcplease);
        }
        Log.d(TAG, "getApLinkedDhcpList: mDhcpList size=" + dhcpList.size());
        return dhcpList;
    }

    private String getApLinkedStaInfo(String mac, List<String> dhcpList) {
        String apLinkedStaInfo = String.format("MAC=%s", new Object[]{mac});
        mac = mac.toLowerCase();
        if (dhcpList != null) {
            for (String dhcplease : dhcpList) {
                if (dhcplease.contains(mac)) {
                    if (4 <= dhcplease.split(HwCHRWifiCPUUsage.COL_SEP).length) {
                        Log.d(TAG, "getApLinkedStaInfo: dhcplease token");
                        apLinkedStaInfo = String.format(apLinkedStaInfo + " IP=%s DEVICE=%s", new Object[]{tokens[2], tokens[3]});
                    }
                }
            }
        }
        return apLinkedStaInfo;
    }

    private String anonyMac(String mac) {
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
        if (1 == System.getInt(this.mContext.getContentResolver(), "show_broadcast_ssid_config", 0)) {
            isHideSsid = Secure.getInt(this.mContext.getContentResolver(), "wifi_ap_ignorebroadcastssid", 0) != 0;
            Log.i(TAG, "isHideSsid = " + isHideSsid);
        }
        return isHideSsid;
    }

    public void clearCallbacksAndMessages() {
        this.mApLinkedStaChangedHandler.removeCallbacksAndMessages(null);
    }

    public Context getContext() {
        return this.mContext;
    }
}
