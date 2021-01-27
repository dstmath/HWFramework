package com.android.server.hidata.mplink;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import com.android.server.wifipro.WifiProCommonUtils;
import huawei.android.net.hwmplink.MpLinkCommonUtils;
import java.util.ArrayList;
import java.util.List;

public class HwMpLinkContentAware {
    private static final String CUST_AIDEVICE = "1";
    private static final String CUST_NORMAL = "0";
    private static final int DEFAULT_CAPACITY = 16;
    private static final int DEFAULT_STRING_LENGTH = 2;
    private static final int DEFAULT_VALUE = -1;
    private static final int GETWAY_NULL = 1;
    private static final int KEYMGMT_WPA = 2;
    private static final String MPLINK_ENABLED = "1";
    private static final String REGEX = ";";
    private static final String[] SMART_DEVICE_APPS = {"dji.go.v"};
    private static final String TAG = "HiData_MpLinkContentAware";
    private static HwMpLinkContentAware mHwMpLinkContentAware;
    private int mAiDeviceflag = 0;
    private List<HwMpLinkConfigInfo> mConfigList = new ArrayList(16);
    private Context mContext;
    private String mCurrDeviceApp;
    private Handler mHandler;
    private boolean mMpLinkEnabledForAlDevice = false;
    private WifiManager mWifiManager;

    private HwMpLinkContentAware(Context context) {
        this.mContext = context;
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
    }

    public static HwMpLinkContentAware getInstance(Context context) {
        if (mHwMpLinkContentAware == null) {
            mHwMpLinkContentAware = new HwMpLinkContentAware(context);
        }
        return mHwMpLinkContentAware;
    }

    public static HwMpLinkContentAware onlyGetInstance() {
        return mHwMpLinkContentAware;
    }

    public void setMpLinkVersion(String version) {
        MpLinkCommonUtils.logD(TAG, false, "mplink_version: %{public}s", new Object[]{version});
    }

    public void setMpLinkEnable(String enable) {
        this.mMpLinkEnabledForAlDevice = "1".equals(enable);
        MpLinkCommonUtils.logD(TAG, false, "mplink_enable: %{public}s %{public}s", new Object[]{enable, String.valueOf(this.mMpLinkEnabledForAlDevice)});
    }

    public void addMpLinkDeviceApp(HwMpLinkConfigInfo config) {
        this.mConfigList.add(config);
    }

    public void registerMpLinkHandler(Handler handler) {
        this.mHandler = handler;
    }

    private int getWifiApType(WifiConfiguration configuration) {
        MpLinkCommonUtils.logI(TAG, false, "getWifiApType", new Object[0]);
        if (!this.mMpLinkEnabledForAlDevice) {
            MpLinkCommonUtils.logD(TAG, false, "MpLink For AlDevice is disabled.", new Object[0]);
            return 0;
        }
        this.mCurrDeviceApp = null;
        int wifiApType = getApIdentifyType(configuration);
        MpLinkCommonUtils.logD(TAG, false, "wifiApType : %{public}d", new Object[]{Integer.valueOf(wifiApType)});
        return wifiApType;
    }

    private void sendMessage(int what) {
        Handler handler = this.mHandler;
        if (handler != null) {
            handler.sendMessage(Message.obtain(handler, what));
        }
    }

    public int getWifiApTypeAndSendMsg(WifiConfiguration configuration) {
        int currentWifiApType = getWifiApType(configuration);
        sendAiDeviceMsg(currentWifiApType);
        MpLinkCommonUtils.logD(TAG, false, "getWifiApTypeAndSendMsg ret:%{public}d", new Object[]{Integer.valueOf(currentWifiApType)});
        return currentWifiApType;
    }

    public void sendAiDeviceMsg(int type) {
        if (this.mAiDeviceflag != type) {
            this.mAiDeviceflag = type;
            if (type == 1) {
                MpLinkCommonUtils.logD(TAG, false, "sendAiDeviceMsg open", new Object[0]);
            }
        }
    }

    public void resetAiDeviceType() {
        sendAiDeviceMsg(0);
    }

    public boolean isAiDevice() {
        return this.mAiDeviceflag == 1;
    }

    public int getApIdentifyType(WifiConfiguration configuration) {
        WifiManager wifiManager = this.mWifiManager;
        if (wifiManager == null) {
            return 0;
        }
        String bssid = WifiProCommonUtils.getCurrentBssid(wifiManager);
        DhcpInfo dhcpInfo = this.mWifiManager.getDhcpInfo();
        if (TextUtils.isEmpty(bssid) || dhcpInfo == null) {
            return 0;
        }
        if (this.mConfigList.isEmpty()) {
            MpLinkCommonUtils.logD(TAG, false, "ConfigList isEmpty", new Object[0]);
            return 0;
        } else if (isMobileAp()) {
            MpLinkCommonUtils.logD(TAG, false, "is Mobile AP.", new Object[0]);
            return 0;
        } else {
            int gatewayType = 0;
            MpLinkCommonUtils.logI(TAG, false, "gateway:%{public}d", new Object[]{Integer.valueOf(dhcpInfo.gateway)});
            if (dhcpInfo.gateway == 0) {
                gatewayType = 1;
            }
            int encryptType = 0;
            if (configuration != null && WifiProCommonUtils.isWpaOrWpa2(configuration)) {
                encryptType = 2;
            }
            for (HwMpLinkConfigInfo configInfo : this.mConfigList) {
                if (configInfo != null && !TextUtils.isEmpty(configInfo.getVendorOui()) && bssid.startsWith(configInfo.getVendorOui()) && checkDetailType(configInfo, bssid, gatewayType, encryptType)) {
                    return 1;
                }
            }
            return 0;
        }
    }

    private boolean checkDetailType(HwMpLinkConfigInfo configInfo, String bssid, int gatewayType, int encryptType) {
        int condition = -1;
        if (!TextUtils.isEmpty(configInfo.getCustMac())) {
            String[] strings = configInfo.getCustMac().split(";", 2);
            MpLinkCommonUtils.logD(TAG, false, "getCustMac:%{private}s", new Object[]{configInfo.getCustMac()});
            if (strings.length == 2 && bssid.equals(strings[0])) {
                if (strings[1].equals("0")) {
                    MpLinkCommonUtils.logD(TAG, false, "cust NORMAL_DEVICE.", new Object[0]);
                    return true;
                } else if (strings[1].equals("1")) {
                    MpLinkCommonUtils.logD(TAG, false, "cust AI_DEVICE.", new Object[0]);
                    return true;
                }
            }
        }
        if (!TextUtils.isEmpty(configInfo.getCondition())) {
            try {
                condition = Integer.parseInt(configInfo.getCondition());
            } catch (NumberFormatException e) {
                MpLinkCommonUtils.logD(TAG, false, "Exception happened when parseInt from configList", new Object[0]);
            }
            MpLinkCommonUtils.logD(TAG, false, "gatewayType:%{public}d, encryptType:{public}d, condition:{public}d", new Object[]{Integer.valueOf(gatewayType), Integer.valueOf(encryptType), Integer.valueOf(condition)});
            if (condition == (gatewayType | encryptType)) {
                MpLinkCommonUtils.logD(TAG, false, "AI_DEVICE.", new Object[0]);
                return true;
            }
        }
        return false;
    }

    public int getDeviceAppUid() {
        if (!this.mMpLinkEnabledForAlDevice || TextUtils.isEmpty(this.mCurrDeviceApp)) {
            return -1;
        }
        if (!isWhitelistApp(this.mCurrDeviceApp)) {
            return MpLinkCommonUtils.getAppUid(this.mContext, this.mCurrDeviceApp);
        }
        MpLinkCommonUtils.logD(TAG, false, "%{public}s isWhitelistApp", new Object[]{this.mCurrDeviceApp});
        return -1;
    }

    public boolean isMobileAp() {
        WifiInfo wifiInfo;
        WifiManager wifiManager = this.mWifiManager;
        if (wifiManager == null || (wifiInfo = wifiManager.getConnectionInfo()) == null || !wifiInfo.getMeteredHint()) {
            return false;
        }
        return true;
    }

    public String getCurrentApName() {
        String name = null;
        WifiManager wifiManager = this.mWifiManager;
        if (wifiManager != null) {
            name = WifiProCommonUtils.getCurrentSsid(wifiManager);
        }
        if (name == null) {
            return "";
        }
        return name;
    }

    public void notifyDefaultNetworkChange() {
        MpLinkCommonUtils.logD(TAG, false, "NotifyDefaultNetworkChange", new Object[0]);
    }

    public boolean isWifiLanApp(int uid) {
        return false;
    }

    public boolean isWhitelistApp(String appName) {
        if (TextUtils.isEmpty(appName)) {
            return false;
        }
        int length = this.mConfigList.size();
        for (int i = 0; i < length; i++) {
            if (appName.startsWith(SMART_DEVICE_APPS[i])) {
                return true;
            }
        }
        return false;
    }
}
