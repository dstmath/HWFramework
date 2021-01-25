package com.huawei.wifi2;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.HwWifiCHRService;
import com.android.server.wifi.HwWifiServiceFactory;

public class HwWifi2ChrManager {
    private static final int DEAUTH_STA_IS_LEFING = 3;
    private static final int INVALID_VALUE_TYPE = 1;
    private static final String KEY_OF_DFTEVENTID = "dftEventId";
    private static final String KEY_OF_ENABLE = "enable";
    private static final String KEY_OF_ERRORCODE = "errorCode";
    private static final String KEY_OF_IFACE = "iface";
    private static final String KEY_OF_PMFINFO = "pmfInfo";
    private static final String KEY_OF_REASON = "reason";
    private static final String KEY_OF_REASON_CODE = "reasoncode";
    private static final String KEY_OF_STATE = "state";
    private static final String KEY_OF_SUBERRORCODE = "subErrorCode";
    private static final String KEY_OF_TYPE = "type";
    private static final String KEY_OF_WIFI2CHRERRID = "Wifi2ChrErrID";
    private static final int LINKTURBO_REL_WIFI2_TIMEOUT = 6000;
    private static final int LINKTURBO_WIFI2_QOE_DETECT_TIME = 3000;
    private static final int PMF_DFT_EVENT_TYPE = 1;
    private static final String TAG = "HwWifi2ChrManager";
    private static final int WIFI2_REQ_REL_FAIL_EID = 909009151;
    private static long sCsRequestWifi2TimeStamp = 1;
    private static HwWifiCHRService sHwWifiChrService;
    private static long sWifi2ConnectedTimeStamp = 1;

    public enum Wifi2ReqResFailReason {
        WIFI1_NOT_CONNECTED,
        P2P_IS_ACTIVE,
        P2P_IS_PROTECTED,
        WIFI2_ALREADY_CONNECTED,
        HAS_PENGDING_WIFI2_REQUESET,
        WIFI1_IS_BLACKLIST,
        WIFI1_WAPI_NETWORK,
        FIND_NO_SUITABLE_NETWORK,
        CS_REL_TIME_OUT,
        WIFI1_5G_PUBLIC_HOTSPOT
    }

    protected static void initHwWifi2ChrManager() {
        sHwWifiChrService = HwWifiServiceFactory.getHwWifiCHRService();
    }

    public static void uploadWifi2TriggerState(boolean isEnable) {
        if (sHwWifiChrService == null) {
            HwHiLog.e(TAG, false, "uploadWifi2TriggerState sHwWifiChrService is null", new Object[0]);
            return;
        }
        Bundle data = new Bundle();
        data.putBoolean(KEY_OF_ENABLE, isEnable);
        sHwWifiChrService.uploadWifi2DftEvent(1202, data);
        HwHiLog.i(TAG, false, "uploadWifi2TriggerState enable is " + isEnable, new Object[0]);
    }

    public static void uploadWifi2DisconnectException(int code) {
        if (sHwWifiChrService == null) {
            HwHiLog.e(TAG, false, "uploadWifi2DisconnectException sHwWifiChrService is null", new Object[0]);
        } else if (code == 3) {
            HwHiLog.i(TAG, false, "uploadWifi2DisconnectException normal disconnect", new Object[0]);
        } else {
            Bundle data = new Bundle();
            data.putInt(KEY_OF_REASON_CODE, code);
            data.putString(KEY_OF_ERRORCODE, String.valueOf(code));
            sHwWifiChrService.uploadWifi2DftEvent(1201, data);
            HwHiLog.i(TAG, false, "uploadWifi2DisconnectException code is " + code, new Object[0]);
        }
    }

    public static void handleWifi2Toggled(boolean isWifiEnable, int reason) {
        if (sHwWifiChrService == null) {
            HwHiLog.e(TAG, false, "handleWifi2Toggled sHwWifiChrService is null", new Object[0]);
            return;
        }
        if (reason == 1) {
            sCsRequestWifi2TimeStamp = SystemClock.elapsedRealtime();
        }
        uploadWifi2TriggerState(isWifiEnable);
        if (!isWifiEnable) {
            handleCloseWifi2Event(reason);
        }
        if (reason == 1001) {
            sCsRequestWifi2TimeStamp = 1;
        }
    }

    private static void handleCloseWifi2Event(int reason) {
        if (reason != 1008) {
            if (reason == 1007) {
                handleWifi2ReqFailException(Wifi2ReqResFailReason.FIND_NO_SUITABLE_NETWORK, true);
            } else if (reason == 1001) {
                if (sCsRequestWifi2TimeStamp == 1) {
                    HwHiLog.i(TAG, false, "cs release wifi2 but sCsRequestWifi2TimeStamp is invalid", new Object[0]);
                    return;
                }
                long currentTimeStamp = SystemClock.elapsedRealtime();
                if (currentTimeStamp - sCsRequestWifi2TimeStamp < 6000 && !HwWifi2Injector.getInstance().getWifiConnectivityManager().isWifi2Connected() && HwWifi2Injector.getInstance().getWifiConnectivityManager().getTargetWificonfiguration() != null) {
                    HwHiLog.i(TAG, false, "cs release wifi2 in 5s trigger disconnect exception", new Object[0]);
                    handleWifi2ReqFailException(Wifi2ReqResFailReason.CS_REL_TIME_OUT, true);
                }
                if (HwWifi2Injector.getInstance().getWifiConnectivityManager().isWifi2Connected()) {
                    long j = sWifi2ConnectedTimeStamp;
                    if (j != 1 && currentTimeStamp - j < 3000) {
                        HwHiLog.i(TAG, false, "trigger wifi2 qoe bad exception", new Object[0]);
                        uploadWifi2DisconnectException(1013);
                    }
                }
            } else if (HwWifi2Injector.getInstance().getWifiConnectivityManager().isWifi2Connected()) {
                HwHiLog.i(TAG, false, "wifi2 is toggled off during wifi2 connected trigger disconnect exception", new Object[0]);
                uploadWifi2DisconnectException(reason);
            }
        }
    }

    public static void handleWifi2ReqFailException(Wifi2ReqResFailReason reason, boolean isReqFail) {
        if (sHwWifiChrService == null) {
            HwHiLog.e(TAG, false, "handleWifi2ReqFail sHwWifiChrService is null", new Object[0]);
        } else if (reason == null) {
            HwHiLog.e(TAG, false, "handleWifi2ReqFail reason is null", new Object[0]);
        } else {
            Bundle data = new Bundle();
            data.putInt(KEY_OF_TYPE, !isReqFail ? 1 : 0);
            data.putInt(KEY_OF_DFTEVENTID, WIFI2_REQ_REL_FAIL_EID);
            data.putString(KEY_OF_ERRORCODE, String.valueOf(reason.ordinal()));
            if (reason == Wifi2ReqResFailReason.FIND_NO_SUITABLE_NETWORK) {
                data.putString(KEY_OF_SUBERRORCODE, String.valueOf(HwWifi2Injector.getInstance().getWifiNetworkSelector().getFiltedNetworkMask()));
            }
            sHwWifiChrService.uploadWifi2DftEvent(1210, data);
            HwHiLog.i(TAG, false, "handleWifi2ReqFail isReqFail is " + isReqFail + " errorCode is " + reason.name(), new Object[0]);
        }
    }

    public static void handleWifi2ConnectStateChange(int state) {
        if (state == 1) {
            sWifi2ConnectedTimeStamp = SystemClock.elapsedRealtime();
        } else {
            sWifi2ConnectedTimeStamp = 1;
        }
    }

    public static void uploadWifi2AccosFailException(int status) {
        if (sHwWifiChrService == null) {
            HwHiLog.e(TAG, false, "uploadWifi2AccosFailException sHwWifiChrService is null", new Object[0]);
            return;
        }
        Bundle data = new Bundle();
        data.putInt(KEY_OF_STATE, status);
        sHwWifiChrService.uploadWifi2DftEvent(1206, data);
        HwHiLog.i(TAG, false, "uploadWifi2AccosFailException status is " + status, new Object[0]);
    }

    public static void uploadWifi2AuthFailException(String iface, int reason) {
        if (sHwWifiChrService == null) {
            HwHiLog.e(TAG, false, "uploadWifi2AccosFailException sHwWifiChrService is null", new Object[0]);
            return;
        }
        Bundle data = new Bundle();
        data.putInt(KEY_OF_REASON, reason);
        data.putString(KEY_OF_IFACE, iface);
        sHwWifiChrService.uploadWifi2DftEvent(1205, data);
        HwHiLog.i(TAG, false, "uploadWifi2AuthFailException reason is " + reason + " iface is " + iface, new Object[0]);
    }

    public static void uploadWifi2PmfInfo(String pmfInfo) {
        if (sHwWifiChrService == null) {
            HwHiLog.e(TAG, false, "uploadWifi2PmfInfo sHwWifiChrService is null", new Object[0]);
            return;
        }
        Bundle data = new Bundle();
        data.putInt(KEY_OF_WIFI2CHRERRID, 1);
        data.putString(KEY_OF_PMFINFO, pmfInfo);
        sHwWifiChrService.uploadWifi2DftEvent(1208, data);
        HwHiLog.i(TAG, false, "uploadWifi2PmfInfo pmfInfo is " + pmfInfo, new Object[0]);
    }

    public static void uploadWifi2DhcpState(int state) {
        if (sHwWifiChrService == null) {
            HwHiLog.e(TAG, false, "uploadWifi2DhcpState sHwWifiChrService is null", new Object[0]);
            return;
        }
        Bundle data = new Bundle();
        data.putInt(KEY_OF_STATE, state);
        sHwWifiChrService.uploadWifi2DftEvent(1209, data);
        HwHiLog.i(TAG, false, "uploadWifi2DhcpState state is " + state, new Object[0]);
    }
}
