package com.android.server.wifi;

import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.hwUtil.StringUtilEx;

public class HwWifiNativeEx implements IHwWifiNativeEx {
    private static final String CHIPSET_WIFI_FEATURE_CAPABILITY = "chipset_wifi_feature_capability";
    private static final int CMD_CLEAR_11N_BLACKLIST = 160;
    private static final int CMD_GET_FEATURE_CAPAB = 101;
    private static final int CMD_GET_WIFI_CATEGORY = 127;
    private static final String COMM_IFACE = "wlan0";
    public static final int PRIV_FEATURE_DOT11_K = 1;
    public static final int PRIV_FEATURE_DOT11_R = 4;
    public static final int PRIV_FEATURE_DOT11_V = 2;
    public static final int PRIV_FEATURE_UNKNOWN = -1;
    private static final String TAG = "HwWifiNativeEx";
    private static final String VOWIFI_DETECT_SET_PREFIX = "VOWIFI_DETECT SET ";
    private static final int WIFI_CAPABILITY_DEFAULT = 0;
    private static final int WIFI_CATEGORY_DEFAULT = 1;
    private static final int WPA_SUPP_TYPE_CONFIG = 0;
    private static final int WPA_SUPP_TYPE_RAW_PSK = 1;
    private static HwWifiNativeEx sHwWifiNativeEx = null;
    private int chipsetCategory = 1;
    private int chipsetFeatrureCapability = 0;
    private final IHwWifiNativeInner mIHwWifiNativeInner;
    private int mPrivFeatureCapab = -1;
    private final SupplicantStaIfaceHal mSupplicantStaIfaceHal;

    private native int deauthLastRoamingBssidHwNative(String str, String str2, String str3);

    private native int disassociateSoftapStaHwNative(String str, String str2);

    private native int gameKOGAdjustSpeedNative(int i, int i2);

    private native String getSoftapClientsHwNative(String str);

    private native int getWifiAntNative(String str, int i);

    private native int hwDelArpItemNative(String str, String str2);

    private native int hwSetArpItemNative(String str, String str2, String str3);

    private native int hwSetPwrBoostNative(int i);

    private static native boolean isSpHalNullInNative();

    private native String readSoftapDhcpLeaseFileHwNative(String str);

    private static native int registerNativeMethods();

    private static native int registerNatives();

    private native int sendCmdToDriverNative(String str, int i, byte[] bArr, int i2);

    private native int sendHumanFactorNative(String str, int i);

    private native int setCmdToWifiChipNative(String str, int i, int i2, int i3, int i4);

    private native int setSoftapHwNative(String str, String str2, String str3);

    private native int setSoftapMacFltrHwNative(String str, String str2);

    private native int setWifiAntNative(String str, int i, int i2);

    private native int setWifiTxPowerNative(int i);

    private static native void unregisterHwWifiExt();

    static {
        System.loadLibrary("huaweiwifi-service");
        boolean isNeedStartWifiHidl = false;
        WifiInjector wifiInjector = WifiInjector.getInstance();
        if (!(wifiInjector == null || wifiInjector.getWifiSettingsStore() == null)) {
            isNeedStartWifiHidl = wifiInjector.getWifiSettingsStore().isWifiToggleEnabled() || wifiInjector.getWifiSettingsStore().isScanAlwaysAvailable();
        }
        HwHiLog.i(TAG, false, "registerNatives isStartWifiHidl:%{public}s", new Object[]{Boolean.valueOf(isNeedStartWifiHidl)});
        if (isNeedStartWifiHidl) {
            SystemProperties.set("ctl.start", "wificond");
            registerNatives();
            return;
        }
        registerNativeMethods();
        unregisterHwWifiExt();
    }

    public static HwWifiNativeEx createHwWifiNativeEx(IHwWifiNativeInner hwWifiNativeInner, SupplicantStaIfaceHal staIfaceHal) {
        HwHiLog.i(TAG, false, "createHwWifiNativeEx is called!", new Object[0]);
        if (sHwWifiNativeEx == null) {
            sHwWifiNativeEx = new HwWifiNativeEx(hwWifiNativeInner, staIfaceHal);
        }
        return sHwWifiNativeEx;
    }

    HwWifiNativeEx(IHwWifiNativeInner hwWifiNativeInner, SupplicantStaIfaceHal staIfaceHal) {
        this.mIHwWifiNativeInner = hwWifiNativeInner;
        this.mSupplicantStaIfaceHal = staIfaceHal;
    }

    public static HwWifiNativeEx getInstance() {
        return sHwWifiNativeEx;
    }

    public int getChipsetWifiCategory() {
        return this.chipsetCategory;
    }

    public int getChipsetWifiFeatrureCapability() {
        return this.chipsetFeatrureCapability;
    }

    public void setChipsetWifiFeatrureCapability() {
        this.chipsetFeatrureCapability = sendCmdToDriver(COMM_IFACE, 101, new byte[]{0});
        HwHiLog.d(TAG, false, "chipset wifi feature capability = " + this.chipsetFeatrureCapability, new Object[0]);
        if (this.chipsetFeatrureCapability < 0) {
            this.chipsetFeatrureCapability = 0;
        }
    }

    public void setChipsetWifiCategory() {
        this.chipsetCategory = sendCmdToDriver(COMM_IFACE, 127, new byte[]{0});
        HwHiLog.d(TAG, false, "chipset wifi category = " + this.chipsetCategory, new Object[0]);
        if (this.chipsetCategory < 1) {
            this.chipsetCategory = 1;
        }
    }

    private String getClientInterfaceName() {
        return this.mIHwWifiNativeInner.getClientInterfaceName();
    }

    public boolean startRxFilter(String ifaceName) {
        return this.mSupplicantStaIfaceHal.startRxFilter(ifaceName);
    }

    public boolean stopRxFilter(String ifaceName) {
        return this.mSupplicantStaIfaceHal.stopRxFilter(ifaceName);
    }

    public boolean setFilterEnable(String ifaceName, boolean enable) {
        return this.mSupplicantStaIfaceHal.mIHwSupplicantStaIfaceHalEx.setFilterEnable(ifaceName, enable);
    }

    public String getWpaSuppConfig() {
        return this.mSupplicantStaIfaceHal.mIHwSupplicantStaIfaceHalEx.getWpasConfig(getClientInterfaceName(), 0);
    }

    public void sendWifiPowerCommand(int level) {
        this.mSupplicantStaIfaceHal.mIHwSupplicantStaIfaceHalEx.setTxPower(getClientInterfaceName(), level);
    }

    public void setIsmcoexMode(boolean enable) {
    }

    public boolean isSupportVoWifiDetect() {
        String ret = this.mSupplicantStaIfaceHal.mIHwSupplicantStaIfaceHalEx.voWifiDetect(getClientInterfaceName(), "VOWIFI_DETECT VOWIFi_IS_SUPPORT");
        HwHiLog.e(TAG, false, "isSupportVoWifiDetect ret :%{public}s", new Object[]{ret});
        return ret != null && (ret.equals("true") || ret.equals("OK"));
    }

    public boolean voWifiDetectSet(String cmd) {
        IHwSupplicantStaIfaceHalEx iHwSupplicantStaIfaceHalEx = this.mSupplicantStaIfaceHal.mIHwSupplicantStaIfaceHalEx;
        String clientInterfaceName = getClientInterfaceName();
        String ret = iHwSupplicantStaIfaceHalEx.voWifiDetect(clientInterfaceName, VOWIFI_DETECT_SET_PREFIX + cmd);
        HwHiLog.d(TAG, false, "voWifiDetectSet ret :%{public}s", new Object[]{ret});
        return ret != null && (ret.equals("true") || ret.equals("OK"));
    }

    public String heartBeat(String param) {
        return this.mSupplicantStaIfaceHal.mIHwSupplicantStaIfaceHalEx.heartBeat(getClientInterfaceName(), param);
    }

    public void enableHiLinkHandshake(boolean uiEnable, String bssid) {
        this.mSupplicantStaIfaceHal.mIHwSupplicantStaIfaceHalEx.enableHiLinkHandshake(getClientInterfaceName(), uiEnable, bssid);
    }

    public boolean hwABSSetCapability(int capability) {
        HwHiLog.d(TAG, false, "SET_ABS_CAPABILITY ", new Object[0]);
        return this.mSupplicantStaIfaceHal.mIHwSupplicantStaIfaceHalEx.setAbsCapability(getClientInterfaceName(), capability);
    }

    public boolean hwABSSoftHandover(int type) {
        HwHiLog.d(TAG, false, "hwABSSoftHandover ", new Object[0]);
        return this.mSupplicantStaIfaceHal.mIHwSupplicantStaIfaceHalEx.absPowerCtrl(getClientInterfaceName(), type);
    }

    public boolean hwABSBlackList(String bssidList) {
        return this.mSupplicantStaIfaceHal.mIHwSupplicantStaIfaceHalEx.setAbsBlacklist(getClientInterfaceName(), bssidList);
    }

    public void query11vRoamingNetwork(int reason, String preferredBssid) {
    }

    public void query11vRoamingNetwork(int reason) {
        if (isSupportDot11V()) {
            this.mSupplicantStaIfaceHal.mIHwSupplicantStaIfaceHalEx.query11vRoamingNetwork(getClientInterfaceName(), reason);
        } else {
            HwHiLog.d(TAG, false, "unsupport 11v, dont trigger bss query", new Object[0]);
        }
    }

    public boolean isSupportRsdbByDriver() {
        String result = this.mSupplicantStaIfaceHal.mIHwSupplicantStaIfaceHalEx.getRsdbCapability(getClientInterfaceName());
        if (result != null) {
            HwHiLog.d(TAG, false, "isSupportRsdbByDriver: %{public}s", new Object[]{result});
            return "RSDB:1".equals(result);
        }
        HwHiLog.i(TAG, false, "isSupportRsdbByDriver: ", new Object[0]);
        return false;
    }

    public boolean isDfsChannel(int frequency) {
        int[] channelsDfs = this.mIHwWifiNativeInner.getChannelsForBand(4);
        if (channelsDfs == null) {
            HwHiLog.d(TAG, false, "Failed to get channels for 5GHz DFS only band,get 5GHz band", new Object[0]);
            int[] channels5G = this.mIHwWifiNativeInner.getChannelsForBand(2);
            if (channels5G == null) {
                HwHiLog.d(TAG, false, "Failed to get channels for 5GHz band", new Object[0]);
                return false;
            }
            for (int channel5G : channels5G) {
                if (frequency == channel5G) {
                    return false;
                }
            }
            HwHiLog.d(TAG, false, "isDfsChannel: true, frequency not in channels5G: %{public}d", new Object[]{Integer.valueOf(frequency)});
            return true;
        }
        for (int channelDfs : channelsDfs) {
            if (frequency == channelDfs) {
                HwHiLog.d(TAG, false, "isDfsChannel: true, DfsChannel: %{public}d", new Object[]{Integer.valueOf(channelDfs)});
                return true;
            }
        }
        return false;
    }

    public String getConnectionRawPsk() {
        return this.mSupplicantStaIfaceHal.mIHwSupplicantStaIfaceHalEx.getWpasConfig(getClientInterfaceName(), 1);
    }

    public int hwRegisterNatives() {
        return registerNatives();
    }

    public void hwUnregisterHwWifiExt() {
        unregisterHwWifiExt();
    }

    public boolean isSpHalNull() {
        return isSpHalNullInNative();
    }

    public boolean setStaticARP(String ipSrc, String mac) {
        return setStaticArp(getClientInterfaceName(), ipSrc, mac);
    }

    public boolean setStaticArp(String ifaceName, String ipSrc, String mac) {
        if (TextUtils.isEmpty(ipSrc) || TextUtils.isEmpty(mac) || TextUtils.isEmpty(ifaceName)) {
            HwHiLog.i(TAG, false, "setStaticArp, invalid params ifaceName is %{public}s, ipSrc is %{public}s, mac is %{public}s", new Object[]{ifaceName, StringUtilEx.safeDisplayIpAddress(ipSrc), StringUtilEx.safeDisplayBssid(mac)});
            return false;
        }
        HwHiLog.i(TAG, false, "setStaticArp, ifaceName is %{public}s, ipSrc is %{public}s, mac is %{public}s", new Object[]{ifaceName, StringUtilEx.safeDisplayIpAddress(ipSrc), StringUtilEx.safeDisplayBssid(mac)});
        return hwSetArpItemNative(ifaceName, ipSrc, mac) == 0;
    }

    public boolean delStaticARP(String ipSrc) {
        return delStaticArp(getClientInterfaceName(), ipSrc);
    }

    public boolean delStaticArp(String ifaceName, String ipSrc) {
        if (TextUtils.isEmpty(ipSrc) || TextUtils.isEmpty(ifaceName)) {
            HwHiLog.i(TAG, false, "delStaticArp, invalid params ifaceName is %{public}s, ipSrc is %{public}s", new Object[]{ifaceName, StringUtilEx.safeDisplayIpAddress(ipSrc)});
            return false;
        }
        HwHiLog.d(TAG, false, "delStaticArp, ifaceName is %{public}s, ipSrc is %{public}s", new Object[]{ifaceName, StringUtilEx.safeDisplayIpAddress(ipSrc)});
        return hwDelArpItemNative(ifaceName, ipSrc) == 0;
    }

    private String getAndCheckSoftApInterfaceName(String operation) {
        String ifaceName = this.mIHwWifiNativeInner.getSoftApInterfaceName();
        if (ifaceName != null) {
            return ifaceName;
        }
        HwHiLog.e(TAG, false, "No softap interfaces, do not %{public}s", new Object[]{operation});
        return null;
    }

    public boolean setSoftapHw(String chan, String mscb) {
        HwHiLog.d(TAG, false, "setSoftapHw entered", new Object[0]);
        if (TextUtils.isEmpty(chan) || TextUtils.isEmpty(mscb)) {
            HwHiLog.e(TAG, false, "Got empty string parameter input", new Object[0]);
            return false;
        }
        String softApInterfaceName = getAndCheckSoftApInterfaceName("setSoftapHw");
        if (softApInterfaceName == null || setSoftapHwNative(softApInterfaceName, chan, mscb) != 0) {
            return false;
        }
        return true;
    }

    public String getSoftapClientsHw() {
        HwHiLog.d(TAG, false, "getSoftapClientsHw entered", new Object[0]);
        String softApInterfaceName = getAndCheckSoftApInterfaceName("getSoftapClientsHw");
        if (softApInterfaceName != null) {
            return getSoftapClientsHwNative(softApInterfaceName);
        }
        return null;
    }

    public String readSoftapDhcpLeaseFileHw() {
        HwHiLog.d(TAG, false, "readSoftapDhcpLeaseFileHw entered", new Object[0]);
        String softApInterfaceName = getAndCheckSoftApInterfaceName("readSoftapDhcpLeaseFileHw");
        if (softApInterfaceName != null) {
            return readSoftapDhcpLeaseFileHwNative(softApInterfaceName);
        }
        return null;
    }

    public boolean setSoftapMacFltrHw(String filter_str) {
        HwHiLog.d(TAG, false, "setSoftapMacFltrHw entered", new Object[0]);
        if (TextUtils.isEmpty(filter_str)) {
            HwHiLog.e(TAG, false, "Got empty mac filter string", new Object[0]);
            return false;
        }
        String softApInterfaceName = getAndCheckSoftApInterfaceName("setSoftapMacFltrHw");
        if (softApInterfaceName == null || setSoftapMacFltrHwNative(softApInterfaceName, filter_str) != 0) {
            return false;
        }
        return true;
    }

    public boolean disassociateSoftapStaHw(String dis_mac) {
        HwHiLog.d(TAG, false, "disassociateSoftapStaHw entered", new Object[0]);
        if (TextUtils.isEmpty(dis_mac)) {
            HwHiLog.e(TAG, false, "Got empty disassociate mac string", new Object[0]);
            return false;
        }
        String softApInterfaceName = getAndCheckSoftApInterfaceName("disassociateSoftapStaHw");
        if (softApInterfaceName == null || disassociateSoftapStaHwNative(softApInterfaceName, dis_mac) != 0) {
            return false;
        }
        return true;
    }

    public boolean setWifiRepeaterMacFilterHw(String iface, String macFilter) {
        HwHiLog.d(TAG, false, "setWifiRepeaterMacFilterHw entered", new Object[0]);
        if (TextUtils.isEmpty(iface) || TextUtils.isEmpty(macFilter)) {
            HwHiLog.e(TAG, false, "iface is null or got empty mac filter string", new Object[0]);
            return false;
        } else if (setSoftapMacFltrHwNative(iface, macFilter) == 0) {
            return true;
        } else {
            return false;
        }
    }

    public boolean disassociateWifiRepeaterStationHw(String iface, String mac) {
        HwHiLog.d(TAG, false, "disassociateWifiRepeaterStationHw entered", new Object[0]);
        if (TextUtils.isEmpty(iface) || TextUtils.isEmpty(mac)) {
            HwHiLog.e(TAG, false, "iface is null or got empty disassociate mac string", new Object[0]);
            return false;
        } else if (disassociateSoftapStaHwNative(iface, mac) == 0) {
            return true;
        } else {
            return false;
        }
    }

    public boolean deauthLastRoamingBssidHw(String mode, String bssid) {
        if (TextUtils.isEmpty(mode)) {
            return false;
        }
        HwHiLog.d(TAG, false, "deauthLastRoamingBssidHw entered", new Object[0]);
        if (!TextUtils.isEmpty(getClientInterfaceName()) && deauthLastRoamingBssidHwNative(getClientInterfaceName(), mode, bssid) == 0) {
            return true;
        }
        return false;
    }

    public int setWifiTxPowerHw(int power) {
        return setWifiTxPowerNative(power);
    }

    public void pwrPercentBoostModeset(int rssi) {
        this.mSupplicantStaIfaceHal.mIHwSupplicantStaIfaceHalEx.pwrPercentBoostModeset(getClientInterfaceName(), rssi);
    }

    public String getMssState() {
        return this.mSupplicantStaIfaceHal.mIHwSupplicantStaIfaceHalEx.getMssState(getClientInterfaceName());
    }

    public String getApVendorInfo() {
        return this.mSupplicantStaIfaceHal.mIHwSupplicantStaIfaceHalEx.getApVendorInfo(getClientInterfaceName());
    }

    public void gameKOGAdjustSpeed(int freq, int mode) {
        HwHiLog.d(TAG, false, "gameKOGAdjustSpeed entered: %{public}d mode: %{public}d", new Object[]{Integer.valueOf(freq), Integer.valueOf(mode)});
        gameKOGAdjustSpeedNative(freq, mode);
    }

    public boolean setPwrBoost(int enable) {
        HwHiLog.d(TAG, false, "pwr:setPwrBoost entered", new Object[0]);
        if (hwSetPwrBoostNative(enable) == 0) {
            return true;
        }
        return false;
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x000a: APUT  (r0v1 java.lang.Object[]), (0 ??[int, short, byte, char]), (r1v0 java.lang.String) */
    public int setWifiAnt(String iface, int mode, int operation) {
        Object[] objArr = new Object[3];
        objArr[0] = iface == null ? "null" : iface;
        objArr[1] = Integer.valueOf(mode);
        objArr[2] = Integer.valueOf(operation);
        HwHiLog.d(TAG, false, "setWifiAnt iface: %{public}s mode: %{public}d operation: %{public}d", objArr);
        return setWifiAntNative(iface, mode, operation);
    }

    public int getWifiAnt(String iface, int mode) {
        HwHiLog.d(TAG, false, "getWifiAnt iface: %{public}s mode: %{public}d", new Object[]{iface, Integer.valueOf(mode)});
        return getWifiAntNative(iface, mode);
    }

    public int setCmdToWifiChip(String iface, int mode, int type, int action, int param) {
        HwHiLog.d(TAG, false, "setCmdToWifiChip iface: %{public}s mode: %{public}d type: %{public}d action: %{public}d param: %{public}d", new Object[]{iface, Integer.valueOf(mode), Integer.valueOf(type), Integer.valueOf(action), Integer.valueOf(param)});
        return setCmdToWifiChipNative(iface, mode, type, action, param);
    }

    public int sendCmdToDriver(String iface, int cmdid, byte[] buffers) {
        if (iface == null || buffers == null) {
            return -1;
        }
        return sendCmdToDriverNative(iface, cmdid, buffers, buffers.length);
    }

    public int sendHumanFactor(String path, int windowSize) {
        if (TextUtils.isEmpty(path)) {
            return -1;
        }
        HwHiLog.d(TAG, false, "sendHumanFactorNative", new Object[0]);
        return sendHumanFactorNative(path, windowSize);
    }

    public int getPrivFeatureCapability() {
        return this.mPrivFeatureCapab;
    }

    public void initPrivFeatureCapability() {
        byte[] buff = new byte[4];
        String ifaceName = getClientInterfaceName();
        if (TextUtils.isEmpty(ifaceName)) {
            HwHiLog.d(TAG, false, "PrivFeatureCapab invalid interfaceName", new Object[0]);
            return;
        }
        this.mPrivFeatureCapab = sendCmdToDriver(ifaceName, 101, buff);
        HwHiLog.d(TAG, false, "PrivFeatureCapab value: %{public}d", new Object[]{Integer.valueOf(this.mPrivFeatureCapab)});
        sendCmdToDriver(ifaceName, CMD_CLEAR_11N_BLACKLIST, new byte[]{0});
    }

    private boolean isSupportDot11V() {
        int i = this.mPrivFeatureCapab;
        if (i <= 0 || (i & 2) != 2) {
            return false;
        }
        return true;
    }
}
