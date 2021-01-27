package com.android.server.wifi.wifi2;

import android.content.Context;
import android.net.LinkProperties;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.os.SystemProperties;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.MSS.HwMssUtils;
import java.lang.reflect.InvocationTargetException;

public class HwWifi2Manager {
    public static final int CLOSE_WIFI2_BEGIN_REASON_INDEX = 1000;
    public static final int CLOSE_WIFI2_CONNECT_FAIL = 1005;
    public static final int CLOSE_WIFI2_CONNECT_TIMEOUT = 1006;
    public static final int CLOSE_WIFI2_CS_REALEASE = 1001;
    public static final int CLOSE_WIFI2_NO_SUITABLE_NETWORK = 1007;
    public static final int CLOSE_WIFI2_P2P_COLLISION = 1003;
    public static final int CLOSE_WIFI2_QOE_BAD = 1013;
    public static final int CLOSE_WIFI2_SCAN_FAILURE = 1015;
    public static final int CLOSE_WIFI2_UNWANTED_NETWORK = 1011;
    public static final int CLOSE_WIFI2_WIFI1_CLOSED_IMMEDIATELY = 1012;
    public static final int CLOSE_WIFI2_WIFI1_CONFLICT = 1009;
    public static final int CLOSE_WIFI2_WIFI1_DISCONNECTED = 1002;
    public static final int CLOSE_WIFI2_WIFI1_DISCONNECTED_BY_WLAN_PRO = 1014;
    public static final int CLOSE_WIFI2_WIFI1_ROAM = 1010;
    public static final int CLOSE_WIFI2_WIFI2_DISCONNECTED = 1008;
    public static final int CLOSE_WIFI2_WIFIDIRECT_ACTIVITY = 1004;
    private static final String DUAL_WIFI_SUPPORT_PROP = "hw_mc.wifi.support_dualwifi";
    public static final int OPEN_CLOSE_WIFI2_DEFAULT_REASON = 0;
    public static final int OPEN_WIFI2_CS_REQUESET = 1;
    private static final String TAG = "HwWifi2Manager";
    private static HwWifi2Manager sHwWifi2Manager = null;
    private static boolean sIsSupportDualWifi;
    private IHwWifi2Service iHwWifi2Service = null;
    private Context mContext = null;

    static {
        boolean z = false;
        if (SystemProperties.getBoolean(DUAL_WIFI_SUPPORT_PROP, false) || HwMssUtils.is1105()) {
            z = true;
        }
        sIsSupportDualWifi = z;
    }

    public HwWifi2Manager(Context context) {
        this.mContext = context.getApplicationContext();
        if (this.mContext == null) {
            this.mContext = context;
        }
        if (sIsSupportDualWifi) {
            createHwWifi2Service(this.mContext);
        }
        sHwWifi2Manager = this;
    }

    public static HwWifi2Manager getInstance() {
        return sHwWifi2Manager;
    }

    public static String msgToString(int msg) {
        if (msg == 0) {
            return "OPEN_CLOSE_WIFI2_DEFAULT_REASON";
        }
        if (msg == 1) {
            return "OPEN_WIFI2_CS_REQUESET";
        }
        switch (msg) {
            case 1001:
                return "CLOSE_WIFI2_CS_REALEASE";
            case 1002:
                return "CLOSE_WIFI2_WIFI1_DISCONNECTED";
            case 1003:
                return "CLOSE_WIFI2_P2P_COLLISION";
            case 1004:
                return "CLOSE_WIFI2_WIFIDIRECT_ACTIVITY";
            case 1005:
                return "CLOSE_WIFI2_CONNECT_FAIL";
            case 1006:
                return "CLOSE_WIFI2_CONNECT_TIMEOUT";
            case 1007:
                return "CLOSE_WIFI2_NO_SUITABLE_NETWORK";
            case CLOSE_WIFI2_WIFI2_DISCONNECTED /* 1008 */:
                return "CLOSE_WIFI2_WIFI2_DISCONNECTED";
            case 1009:
                return "CLOSE_WIFI2_WIFI1_CONFLICT";
            case CLOSE_WIFI2_WIFI1_ROAM /* 1010 */:
                return "CLOSE_WIFI2_WIFI1_ROAM";
            case CLOSE_WIFI2_UNWANTED_NETWORK /* 1011 */:
                return "CLOSE_WIFI2_UNWANTED_NETWORK";
            case CLOSE_WIFI2_WIFI1_CLOSED_IMMEDIATELY /* 1012 */:
                return "CLOSE_WIFI2_WIFI1_CLOSED_IMMEDIATELY";
            case CLOSE_WIFI2_QOE_BAD /* 1013 */:
                return "CLOSE_WIFI2_QOE_BAD";
            case CLOSE_WIFI2_WIFI1_DISCONNECTED_BY_WLAN_PRO /* 1014 */:
                return "CLOSE_WIFI2_WIFI1_DISCONNECTED_BY_WLAN_PRO";
            case CLOSE_WIFI2_SCAN_FAILURE /* 1015 */:
                return "CLOSE_WIFI2_SCAN_FAILURE";
            default:
                return "UNKNOWN_MSG";
        }
    }

    private void createHwWifi2Service(Context context) {
        HwHiLog.i(TAG, false, "createHwWifi2Service", new Object[0]);
        if (this.iHwWifi2Service == null) {
            try {
                Object hwWifi2Service = Class.forName("com.huawei.wifi2.HwWifi2Service").getConstructor(Context.class).newInstance(this.mContext);
                if (hwWifi2Service instanceof IHwWifi2Service) {
                    this.iHwWifi2Service = (IHwWifi2Service) hwWifi2Service;
                }
                if (this.iHwWifi2Service != null) {
                    HwHiLog.i(TAG, false, "successes to get HwWifiProService newInstance.", new Object[0]);
                } else {
                    HwHiLog.e(TAG, false, "fail to get HwWifiProService, HwWifiProService is not exist.", new Object[0]);
                }
            } catch (ClassNotFoundException | NoSuchMethodException | SecurityException e) {
                HwHiLog.e(TAG, false, "isHwWifi2ServiceExit class or method not found.", new Object[0]);
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException e2) {
                HwHiLog.e(TAG, false, "isHwWifi2ServiceExit newInstance expression is illegal.", new Object[0]);
            }
        }
    }

    public boolean isSupportDualWifi() {
        return sIsSupportDualWifi && this.iHwWifi2Service != null;
    }

    public void setSlaveWifiNetworkSelectionPara(int signalLevel, int callerUid, int needInternet) {
        IHwWifi2Service iHwWifi2Service2 = this.iHwWifi2Service;
        if (iHwWifi2Service2 != null) {
            iHwWifi2Service2.setSlaveWifiNetworkSelectionPara(signalLevel, callerUid, needInternet);
        }
    }

    public WifiInfo getSlaveWifiConnectionInfo() {
        IHwWifi2Service iHwWifi2Service2 = this.iHwWifi2Service;
        if (iHwWifi2Service2 == null) {
            return null;
        }
        return iHwWifi2Service2.getSlaveWifiConnectionInfo();
    }

    public LinkProperties getLinkPropertiesForSlaveWifi() {
        IHwWifi2Service iHwWifi2Service2 = this.iHwWifi2Service;
        if (iHwWifi2Service2 == null) {
            return null;
        }
        return iHwWifi2Service2.getLinkPropertiesForSlaveWifi();
    }

    public NetworkInfo getNetworkInfoForSlaveWifi() {
        IHwWifi2Service iHwWifi2Service2 = this.iHwWifi2Service;
        if (iHwWifi2Service2 == null) {
            return null;
        }
        return iHwWifi2Service2.getNetworkInfoForSlaveWifi();
    }

    public boolean setWifi2Enable(boolean isWifiEnable) {
        IHwWifi2Service iHwWifi2Service2 = this.iHwWifi2Service;
        if (iHwWifi2Service2 == null) {
            return false;
        }
        return iHwWifi2Service2.setWifi2Enable(isWifiEnable, 0);
    }

    public boolean setWifi2Enable(boolean isWifiEnable, int reason) {
        IHwWifi2Service iHwWifi2Service2 = this.iHwWifi2Service;
        if (iHwWifi2Service2 == null) {
            return false;
        }
        return iHwWifi2Service2.setWifi2Enable(isWifiEnable, reason);
    }

    public void handleP2pConnectCommand(int command) {
        IHwWifi2Service iHwWifi2Service2 = this.iHwWifi2Service;
        if (iHwWifi2Service2 != null) {
            iHwWifi2Service2.handleP2pConnectCommand(command);
        }
    }
}
