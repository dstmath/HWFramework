package com.android.server.hidata.mplink;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import com.android.server.rms.iaware.feature.DevSchedFeatureRT;
import com.android.server.wifipro.WifiProCommonUtils;
import huawei.android.net.hwmplink.MpLinkCommonUtils;
import java.util.ArrayList;
import java.util.List;

public class HwMpLinkContentAware {
    private static final String CUST_AIDEVICE = "1";
    private static final String CUST_NORMAL = "0";
    private static final int GETWAY_NULL = 1;
    private static final int KEYMGMT_WPA = 2;
    private static final String MPLINK_ENABLED = "1";
    private static final String MULTI_NETWORK = "1";
    private static final String REGEX = ";";
    private static final String SINGLE_NETWORK = "2";
    private static final String[] SMART_DEVICE_APPS = {"dji.go.v"};
    private static final String TAG = "HiData_MpLinkContentAware";
    private static HwMpLinkContentAware mHwMpLinkContentAware;
    private int mAiDeviceflag = 0;
    private List<HwMpLinkConfigInfo> mConfigList = new ArrayList();
    private Context mContext;
    private String mCurrDeviceApp;
    private Handler mHandler;
    private boolean mMpLinkEnabledForAlDevice = false;
    private WifiManager mWifiManager;
    private List<ScanResult> scanResultList;

    public static HwMpLinkContentAware getInstance(Context context) {
        if (mHwMpLinkContentAware == null) {
            mHwMpLinkContentAware = new HwMpLinkContentAware(context);
        }
        return mHwMpLinkContentAware;
    }

    public static HwMpLinkContentAware onlyGetInstance() {
        return mHwMpLinkContentAware;
    }

    private HwMpLinkContentAware(Context context) {
        this.mContext = context;
        this.mWifiManager = (WifiManager) this.mContext.getSystemService(DevSchedFeatureRT.WIFI_FEATURE);
    }

    public void setMpLinkVersion(String version) {
        MpLinkCommonUtils.logD(TAG, "mplink_version: " + version);
    }

    public void setMpLinkEnable(String enable) {
        this.mMpLinkEnabledForAlDevice = "1".equals(enable);
        MpLinkCommonUtils.logD(TAG, "mplink_enable: " + enable + this.mMpLinkEnabledForAlDevice);
    }

    public void addMpLinkDeviceAPP(HwMpLinkConfigInfo config) {
        this.mConfigList.add(config);
    }

    public void regiterMpLinkHander(Handler handler) {
        this.mHandler = handler;
    }

    private int getWifiApType(WifiConfiguration configuration) {
        MpLinkCommonUtils.logI(TAG, "getWifiApType");
        if (!this.mMpLinkEnabledForAlDevice) {
            MpLinkCommonUtils.logD(TAG, "MpLink For AlDevice is disabled.");
            return 0;
        }
        this.mCurrDeviceApp = null;
        int wifiApType = getApIdentifType(configuration);
        MpLinkCommonUtils.logD(TAG, "wifiApType : " + wifiApType);
        return wifiApType;
    }

    private void sendMessage(int what) {
        if (this.mHandler != null) {
            this.mHandler.sendMessage(Message.obtain(this.mHandler, what));
        }
    }

    public int getWifiApTypeAndSendMsg(WifiConfiguration configuration) {
        int CurrentWifiApType = getWifiApType(configuration);
        sendAiDeviceMsg(CurrentWifiApType);
        return CurrentWifiApType;
    }

    public void sendAiDeviceMsg(int type) {
        if (this.mAiDeviceflag != type) {
            this.mAiDeviceflag = type;
            if (1 == type) {
                sendMessage(HwMpLinkServiceImpl.MPLINK_MSG_AIDEVICE_MPLINK_OPEN);
            }
        }
    }

    public void resetAiDeviceType() {
        sendAiDeviceMsg(0);
    }

    public boolean isAiDevice() {
        return this.mAiDeviceflag == 1;
    }

    public int getAiDeviceflag() {
        return this.mAiDeviceflag;
    }

    public int getApIdentifType(WifiConfiguration configuration) {
        int condition;
        if (this.mWifiManager == null) {
            return 0;
        }
        String bssid = WifiProCommonUtils.getCurrentBssid(this.mWifiManager);
        if (TextUtils.isEmpty(bssid)) {
            return 0;
        }
        if (this.mConfigList.isEmpty()) {
            MpLinkCommonUtils.logD(TAG, "ConfigList isEmpty");
            return 0;
        } else if (isMobileAP()) {
            MpLinkCommonUtils.logD(TAG, "is Mobile AP.");
            return 0;
        } else {
            DhcpInfo dhcpInfo = this.mWifiManager.getDhcpInfo();
            if (dhcpInfo == null) {
                return 0;
            }
            int getwayType = 0;
            MpLinkCommonUtils.logI(TAG, "gateway:" + dhcpInfo.gateway);
            if (dhcpInfo.gateway == 0) {
                getwayType = 1;
            }
            int encryptType = 0;
            if (configuration != null && WifiProCommonUtils.isWpaOrWpa2(configuration)) {
                encryptType = 2;
            }
            int length = this.mConfigList.size();
            int i = 0;
            while (i < length) {
                if (this.mConfigList.get(i) != null && !TextUtils.isEmpty(this.mConfigList.get(i).getVendorOui()) && bssid.startsWith(this.mConfigList.get(i).getVendorOui())) {
                    if (!TextUtils.isEmpty(this.mConfigList.get(i).getCustMac())) {
                        String[] strings = this.mConfigList.get(i).getCustMac().split(";", 2);
                        MpLinkCommonUtils.logD(TAG, "getCustMac:" + this.mConfigList.get(i).getCustMac());
                        if (strings.length == 2 && bssid.equals(strings[0])) {
                            if ("0".equals(strings[1])) {
                                MpLinkCommonUtils.logD(TAG, "cust NORMAL_DEVICE.");
                                return 0;
                            } else if ("1".equals(strings[1])) {
                                MpLinkCommonUtils.logD(TAG, "cust AI_DEVICE.");
                                this.mCurrDeviceApp = this.mConfigList.get(i).getAppName();
                                return 1;
                            }
                        }
                    }
                    if (!TextUtils.isEmpty(this.mConfigList.get(i).getCondition())) {
                        MpLinkCommonUtils.logD(TAG, "getwayType:" + getwayType + ", encryptType:" + encryptType + "condition:" + condition);
                        if (condition == (getwayType | encryptType)) {
                            this.mCurrDeviceApp = this.mConfigList.get(i).getAppName();
                            String networkType = this.mConfigList.get(i).getMultNetwork();
                            MpLinkCommonUtils.logD(TAG, "DeviceApp: " + this.mCurrDeviceApp + ",networkType " + networkType);
                            if ("1".equals(networkType)) {
                                return 1;
                            }
                            if ("2".equals(networkType)) {
                                return 2;
                            }
                        } else {
                            continue;
                        }
                    } else {
                        continue;
                    }
                }
                i++;
            }
            return 0;
        }
    }

    public int getDeviceAppUid() {
        if (this.mMpLinkEnabledForAlDevice && !TextUtils.isEmpty(this.mCurrDeviceApp)) {
            return MpLinkCommonUtils.getAppUid(this.mContext, this.mCurrDeviceApp);
        }
        return -1;
    }

    public boolean isMobileAP() {
        if (this.mWifiManager != null) {
            WifiInfo wifiInfo = this.mWifiManager.getConnectionInfo();
            if (wifiInfo != null && wifiInfo.getMeteredHint()) {
                return true;
            }
        }
        return false;
    }

    public String getCurrentApName() {
        String name = null;
        if (this.mWifiManager != null) {
            name = WifiProCommonUtils.getCurrentSsid(this.mWifiManager);
        }
        if (name == null) {
            return "";
        }
        return name;
    }

    public void notifyDefaultNetworkChange() {
        MpLinkCommonUtils.logD(TAG, "NotifyDefaultNetworkChange");
        sendMessage(HwMpLinkServiceImpl.MPLINK_MSG_DEFAULT_NETWORK_CHANGE);
    }

    public boolean isWifiLanApp(int uid) {
        return false;
    }

    public boolean isWhitelistApp(String appName) {
        if (!TextUtils.isEmpty(appName)) {
            int length = this.mConfigList.size();
            for (int i = 0; i < length; i++) {
                if (appName.startsWith(SMART_DEVICE_APPS[i])) {
                    return true;
                }
            }
        }
        return false;
    }
}
