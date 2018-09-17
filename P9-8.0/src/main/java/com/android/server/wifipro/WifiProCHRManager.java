package com.android.server.wifipro;

import android.util.Log;
import java.lang.reflect.Method;

public class WifiProCHRManager {
    public static final String CHR_GET_INSTANCE_API_NAME = "getDefault";
    public static final String CHR_MANAGER_CLASS = "com.android.server.wifi.HwWifiCHRStateManagerImpl";
    private static final int DBG_LOG_LEVEL = 1;
    private static final int ERROR_LOG_LEVEL = 3;
    private static final int INFO_LOG_LEVEL = 2;
    public static final String LOG_COMM_USER_API_NAME = "isCommercialUser";
    public static final String LOG_GET_INSTANCE_API_NAME = "getInstance";
    public static final String LOG_MANAGER_CLASS = "com.huawei.connectivitylog.LogManager";
    public static final String SUB_EVENT_ACTIVE_CHECK_FAIL = "ACTIVE_CHECK_FAIL";
    public static final String SUB_EVENT_AUTO_CLOSE_TERMINATION = "AUTO_CLOSE_TERMINATION";
    public static final String SUB_EVENT_BG_AC_RS_DIFF = "BG_AC_RS_DIFF";
    public static final String SUB_EVENT_BG_AC_TIME_LEN = "BG_AC_TIME_LEN";
    public static final String SUB_EVENT_BG_ASSOC_REJECT_CNT = "BG_ASSOC_REJECT_CNT";
    public static final String SUB_EVENT_BG_AUTH_FAIL_CNT = "BG_AUTH_FAIL_CNT";
    public static final String SUB_EVENT_BG_CONNT_TIMEOUT_CNT = "BG_CONNT_TIMEOUT_CNT";
    public static final String SUB_EVENT_BG_CONN_AP_TIME_LEN = "BG_CONN_AP_TIME_LEN";
    public static final String SUB_EVENT_BG_DHCP_FAIL_CNT = "BG_DHCP_FAIL_CNT";
    public static final String SUB_EVENT_BG_DNS_FAIL_CNT = "BG_DNS_FAIL_CNT";
    public static final String SUB_EVENT_BG_FAILED_CNT = "BG_FAILED_CNT";
    public static final String SUB_EVENT_BG_INET_OK_ACTIVE_NOT_OK = "BG_INET_OK_ACTIVE_NOT_OK";
    public static final String SUB_EVENT_BG_NOT_INET_ACTIVE_IOK = "BG_NOT_INET_ACTIVE_IOK";
    public static final String SUB_EVENT_BG_USER_SEL_AP_FISHING_CNT = "BG_USER_SEL_AP_FISHING_CNT";
    public static final String SUB_EVENT_CANT_CONNECT_FOR_LONG = "CANT_CONNECT_FOR_LONG";
    public static final String SUB_EVENT_EMPTY = "NO_SUB_EVENT";
    public static final String SUB_EVENT_ENTERPRISE_AP_INFO = "ENTERPRISE_AP_INFO";
    public static final String SUB_EVENT_HOME_AP_INFO = "HOME_AP_INFO";
    public static final String SUB_EVENT_NOT_OPEN_AP_REDIRECT = "NOT_OPEN_AP_REDIRECT";
    public static final String SUB_EVENT_PORTALAP_IN_WHITE = "PORTALAP_IN_WHITE";
    public static final String SUB_EVENT_ROVE_OUT_PARAMETER = "ROVE_OUT_PARAMETER";
    public static final String SUB_EVENT_SWITCH_PINGPONG = "SWITCH_PINGPONG";
    public static final String SUB_EVENT_WHITE_MORETHAN_500 = "WHITE_MORETHAN_500";
    private static final String TAG = "WifiProCHRManager";
    public static final int WIFI_PORTAL_AUTH_MSG_COLLECTE = 124;
    public static final int WIFI_PORTAL_SAMPLES_COLLECTE = 120;
    public static final int WIFI_WIFIPRO_EXCEPTION_EVENT = 122;
    public static final int WIFI_WIFIPRO_FREE_AP_INFO = 123;
    public static final int WIFI_WIFIPRO_STATISTICS_EVENT = 121;
    private static Object chrManagerObj = null;
    private static Method commercialUserMethod = null;
    private static boolean initialized = false;
    private static boolean isCommercialUser = true;
    private static Object logManagerObj = null;
    private static int printLogLevel = 1;
    private static WifiProCHRManager wsm = new WifiProCHRManager();

    public static WifiProCHRManager getInstance() {
        if (!initialized) {
            init();
        }
        return wsm;
    }

    private static void init() {
        chrManagerObj = null;
        logManagerObj = null;
        commercialUserMethod = null;
        isCommercialUser = true;
        logd("WifiProCHRManager init enter.");
        try {
            chrManagerObj = Class.forName(CHR_MANAGER_CLASS).getDeclaredMethod(CHR_GET_INSTANCE_API_NAME, new Class[0]).invoke(null, new Object[0]);
            initCommercialInterface();
            isCommercialUser();
        } catch (Exception e) {
            loge("WifiProCHRManager, init(), Exception msg = " + e.getMessage());
        }
        initialized = true;
    }

    private void invokeMethod(boolean betaUserOnly, String methodName, Object... args) {
        if ((!betaUserOnly || !isCommercialUser) && chrManagerObj != null && methodName != null) {
            try {
                for (Method method : chrManagerObj.getClass().getMethods()) {
                    if (methodName.equals(method.getName())) {
                        method.setAccessible(true);
                        method.invoke(chrManagerObj, args);
                        break;
                    }
                }
            } catch (Exception e) {
                loge("WifiProCHRManager, invokeMethod(), Exception msg = " + e.getMessage());
            }
        }
    }

    public void updateWifiException(int ucErrorCode, String ucSubErrorCode) {
        logd("updateWifiException call for err code:" + ucErrorCode + ", sub err code:" + ucSubErrorCode);
        invokeMethod(false, "uploadDFTEvent", Integer.valueOf(ucErrorCode), ucSubErrorCode);
    }

    private static void initCommercialInterface() {
        try {
            Class logClass = Class.forName(LOG_MANAGER_CLASS);
            logManagerObj = logClass.getDeclaredMethod(LOG_GET_INSTANCE_API_NAME, new Class[0]).invoke(null, new Object[0]);
            commercialUserMethod = logClass.getDeclaredMethod(LOG_COMM_USER_API_NAME, new Class[0]);
        } catch (Exception e) {
            loge("initCommercialInterface, Exception msg = " + e.getMessage());
        }
    }

    public static boolean isCommercialUser() {
        synchronized (wsm) {
            if (commercialUserMethod == null || logManagerObj == null) {
                loge("recall initCommercialInterface.");
                initCommercialInterface();
            } else {
                try {
                    isCommercialUser = ((Boolean) commercialUserMethod.invoke(logManagerObj, new Object[0])).booleanValue();
                    logd("get isCommercialUser = " + isCommercialUser);
                } catch (Exception e) {
                    loge("isCommercialUser, Exception msg = " + e.getMessage());
                }
            }
        }
        return isCommercialUser;
    }

    public void updatePortalAutSms(String sms_num, byte[] sms_body, int sms_body_len) {
        logd("updatePortalAutSms call enter.");
        invokeMethod(false, "updatePortalAutSms", sms_num, sms_body, Integer.valueOf(sms_body_len));
    }

    public void updatePortalAPInfo(byte[] ssid, String bssid, String cellId, int sms_body_len) {
        logd("updatePortalAPInfo call enter.");
        invokeMethod(false, "updatePortalAPInfo", ssid, bssid, cellId, Integer.valueOf(sms_body_len));
    }

    public void updatePortalWebpageInfo(String url, String phoneInputId, String sndBtnId, String codeInputId, String submitBtnId, int htmlBtnNum) {
        logd("updatePortalWebpageInfo call enter.");
        invokeMethod(false, "updatePortalWebpageInfo", url, phoneInputId, sndBtnId, codeInputId, submitBtnId, Integer.valueOf(htmlBtnNum));
    }

    public void updatePortalKeyLines(byte[] lines) {
        logd("updatePortalKeyLines call enter.");
        invokeMethod(false, "updatePortalKeyLines", lines);
    }

    public void updateStatParaPart1(int statIntervalTime, int enableTotTime, short noInetHandoverCount, short portalUnauthCount, short wifiScoCount, short portalCodeParseCount, short rcvSMS_Count, short portalAutoLoginCount) {
        invokeMethod(false, "updateStatParaPart1", Integer.valueOf(statIntervalTime), Integer.valueOf(enableTotTime), Short.valueOf(noInetHandoverCount), Short.valueOf(portalUnauthCount), Short.valueOf(wifiScoCount), Short.valueOf(portalCodeParseCount), Short.valueOf(rcvSMS_Count), Short.valueOf(portalAutoLoginCount));
    }

    public void updateStatParaPart2(short cellAutoOpenCount, short cellAutoCloseCount, short totalROC, short manualBackROC, short rssi_RO_Tot, short rssi_ErrRO_Tot, short ota_RO_Tot, short ota_ErrRO_Tot, short tcp_RO_Tot) {
        invokeMethod(false, "updateStatParaPart2", Short.valueOf(cellAutoOpenCount), Short.valueOf(cellAutoCloseCount), Short.valueOf(totalROC), Short.valueOf(manualBackROC), Short.valueOf(rssi_RO_Tot), Short.valueOf(rssi_ErrRO_Tot), Short.valueOf(ota_RO_Tot), Short.valueOf(ota_ErrRO_Tot), Short.valueOf(tcp_RO_Tot));
    }

    public void updateStatParaPart3(short tcp_ErrRO_Tot, int manualRI_TotTime, int autoRI_TotTime, short autoRI_TotCount, short rssi_RestoreRI_Count, short rssi_BetterRI_Count, short timerRI_Count, short hisScoRI_Count, short userCancelROC) {
        invokeMethod(false, "updateStatParaPart3", Short.valueOf(tcp_ErrRO_Tot), Integer.valueOf(manualRI_TotTime), Integer.valueOf(autoRI_TotTime), Short.valueOf(autoRI_TotCount), Short.valueOf(rssi_RestoreRI_Count), Short.valueOf(rssi_BetterRI_Count), Short.valueOf(timerRI_Count), Short.valueOf(hisScoRI_Count), Short.valueOf(userCancelROC));
    }

    public void updateStatParaPart4(short wifiToWifiSuccCount, short noInetAlarmCount, short wifiOobInitState, short notAutoConnPortalCnt, short highDataRateStopROC, short selectNotInetAPCount, short userUseBgScanAPCount, short pingPongCount) {
        invokeMethod(false, "updateStatParaPart4", Short.valueOf(wifiToWifiSuccCount), Short.valueOf(noInetAlarmCount), Short.valueOf(wifiOobInitState), Short.valueOf(notAutoConnPortalCnt), Short.valueOf(highDataRateStopROC), Short.valueOf(selectNotInetAPCount), Short.valueOf(userUseBgScanAPCount), Short.valueOf(pingPongCount));
    }

    public void updateStatParaPart5(short bqeBadSettingCancel, short notInetSettingCancel, short notInetUserCancel, short notInetRestoreRI, short notInetUserManualRI, short notInetWifiToWifiCount, short reopenWifiRICount, short selCSPShowDiglogCount, short selCSPAutoSwCount, short selCSPNotSwCount, short totBtnRICount, short bmd_TenMNotifyCount) {
        invokeMethod(false, "updateStatParaPart5", Short.valueOf(bqeBadSettingCancel), Short.valueOf(notInetSettingCancel), Short.valueOf(notInetUserCancel), Short.valueOf(notInetRestoreRI), Short.valueOf(notInetUserManualRI), Short.valueOf(notInetWifiToWifiCount), Short.valueOf(reopenWifiRICount), Short.valueOf(selCSPShowDiglogCount), Short.valueOf(selCSPAutoSwCount), Short.valueOf(selCSPNotSwCount), Short.valueOf(totBtnRICount), Short.valueOf(bmd_TenMNotifyCount));
    }

    public void updateStatParaPart6(short bmd_TenM_RI_Count, short bmd_FiftyMNotifyCount, short bmd_FiftyM_RI_Count, short bmd_UserDelNotifyCount, int ro_TotMobileData, short af_PhoneNumSuccCnt, short af_PhoneNumFailCnt, short af_PasswordSuccCnt, short af_PasswordFailCnt, short af_AutoLoginSuccCnt, short af_AutoLoginFailCnt) {
        invokeMethod(false, "updateStatParaPart6", Short.valueOf(bmd_TenM_RI_Count), Short.valueOf(bmd_FiftyMNotifyCount), Short.valueOf(bmd_FiftyM_RI_Count), Short.valueOf(bmd_UserDelNotifyCount), Integer.valueOf(ro_TotMobileData), Short.valueOf(af_PhoneNumSuccCnt), Short.valueOf(af_PhoneNumFailCnt), Short.valueOf(af_PasswordSuccCnt), Short.valueOf(af_PasswordFailCnt), Short.valueOf(af_AutoLoginSuccCnt), Short.valueOf(af_AutoLoginFailCnt));
    }

    public void updateStatParaPart7(short bg_RunCnt, short settingBG_RunCnt, short bg_FreeInetOkApCnt, short bg_FishingApCnt, short bg_FreeNotInetApCnt, short bg_PortalApCnt, short bg_FailedCnt, short bg_InetNotOkActiveOk, short bg_InetOkActiveNotOk) {
        invokeMethod(false, "updateStatParaPart7", Short.valueOf(bg_RunCnt), Short.valueOf(settingBG_RunCnt), Short.valueOf(bg_FreeInetOkApCnt), Short.valueOf(bg_FishingApCnt), Short.valueOf(bg_FreeNotInetApCnt), Short.valueOf(bg_PortalApCnt), Short.valueOf(bg_FailedCnt), Short.valueOf(bg_InetNotOkActiveOk), Short.valueOf(bg_InetOkActiveNotOk));
    }

    public void updateStatParaPart8(short bg_UserSelApFishingCnt, short bg_ConntTimeoutCnt, short bg_DNSFailCnt, short bg_DHCPFailCnt, short bg_AUTH_FailCnt, short bg_AssocRejectCnt, short bg_UserSelFreeInetOkCnt, short bg_UserSelNoInetCnt, short bg_UserSelPortalCnt) {
        invokeMethod(false, "updateStatParaPart8", Short.valueOf(bg_UserSelApFishingCnt), Short.valueOf(bg_ConntTimeoutCnt), Short.valueOf(bg_DNSFailCnt), Short.valueOf(bg_DHCPFailCnt), Short.valueOf(bg_AUTH_FailCnt), Short.valueOf(bg_AssocRejectCnt), Short.valueOf(bg_UserSelFreeInetOkCnt), Short.valueOf(bg_UserSelNoInetCnt), Short.valueOf(bg_UserSelPortalCnt));
    }

    public void updateStatParaPart9(short bg_FoundTwoMoreApCnt, short af_FPNSuccNotMsmCnt, short bsg_RsGoodCnt, short bsg_RsMidCnt, short bsg_RsBadCnt, short bsg_EndIn4sCnt, short bsg_EndIn4s7sCnt, short bsg_NotEndIn7sCnt) {
        invokeMethod(false, "updateStatParaPart9", Short.valueOf(bg_FoundTwoMoreApCnt), Short.valueOf(af_FPNSuccNotMsmCnt), Short.valueOf(bsg_RsGoodCnt), Short.valueOf(bsg_RsMidCnt), Short.valueOf(bsg_RsBadCnt), Short.valueOf(bsg_EndIn4sCnt), Short.valueOf(bsg_EndIn4s7sCnt), Short.valueOf(bsg_NotEndIn7sCnt));
    }

    public void updateStatParaPart10(short pBG_NCByConnectFail, short pBG_NCByCheckFail, short pBG_NCByStateErr, short pBG_NCByUnknown, short pBQE_CNUrl1FailCount, short pBQE_CNUrl2FailCount, short pBQE_CNUrl3FailCount, short pBQE_NCNUrl1FailCount, short pBQE_NCNUrl2FailCount, short pBQE_NCNUrl3FailCount) {
        invokeMethod(false, "updateStatParaPart10", Short.valueOf(pBG_NCByConnectFail), Short.valueOf(pBG_NCByCheckFail), Short.valueOf(pBG_NCByStateErr), Short.valueOf(pBG_NCByUnknown), Short.valueOf(pBQE_CNUrl1FailCount), Short.valueOf(pBQE_CNUrl2FailCount), Short.valueOf(pBQE_CNUrl3FailCount), Short.valueOf(pBQE_NCNUrl1FailCount), Short.valueOf(pBQE_NCNUrl2FailCount), Short.valueOf(pBQE_NCNUrl3FailCount));
    }

    public void updateStatParaPart11(short pBQE_ScoreUnknownCount, short pBQE_BindWlanFailCount, short pBQE_StopBqeFailCount, int pQOE_AutoRI_TotData, int pNotInet_AutoRI_TotData, short pQOE_RO_DISCONNECT_Cnt, int pQOE_RO_DISCONNECT_TotData, short pNotInetRO_DISCONNECT_Cnt, int pNotInetRO_DISCONNECT_TotData, int pTotWifiConnectTime) {
        invokeMethod(false, "updateStatParaPart11", Short.valueOf(pBQE_ScoreUnknownCount), Short.valueOf(pBQE_BindWlanFailCount), Short.valueOf(pBQE_StopBqeFailCount), Integer.valueOf(pQOE_AutoRI_TotData), Integer.valueOf(pNotInet_AutoRI_TotData), Short.valueOf(pQOE_RO_DISCONNECT_Cnt), Integer.valueOf(pQOE_RO_DISCONNECT_TotData), Short.valueOf(pNotInetRO_DISCONNECT_Cnt), Integer.valueOf(pNotInetRO_DISCONNECT_TotData), Integer.valueOf(pTotWifiConnectTime));
    }

    public void updateStatParaPart12(short pActiveCheckRS_Diff, short pNoInetAlarmOnConnCnt, short pPortalNoAutoConnCnt, short pHomeAPAddRoPeriodCnt, short pHomeAPQoeBadCnt, int pHistoryTotWifiConnHour, short pTotAPRecordCnt, short pTotHomeAPCnt, short pBigRTT_RO_Tot, short pBigRTT_ErrRO_Tot) {
        invokeMethod(false, "updateStatParaPart12", Short.valueOf(pActiveCheckRS_Diff), Short.valueOf(pNoInetAlarmOnConnCnt), Short.valueOf(pPortalNoAutoConnCnt), Short.valueOf(pHomeAPAddRoPeriodCnt), Short.valueOf(pHomeAPQoeBadCnt), Integer.valueOf(pHistoryTotWifiConnHour), Short.valueOf(pTotAPRecordCnt), Short.valueOf(pTotHomeAPCnt), Short.valueOf(pBigRTT_RO_Tot), Short.valueOf(pBigRTT_ErrRO_Tot));
    }

    public void updateStatParaPart13(short pTotalPortalConnCount, short pTotalPortalAuthSuccCount, short pManualConnBlockPortalCount, short pWifiproStateAtReportTime, short pWifiproOpenCount, short pWifiproCloseCount, short pActiveCheckRS_Same) {
        invokeMethod(false, "updateStatParaPart13", Short.valueOf(pTotalPortalConnCount), Short.valueOf(pTotalPortalAuthSuccCount), Short.valueOf(pManualConnBlockPortalCount), Short.valueOf(pWifiproStateAtReportTime), Short.valueOf(pWifiproOpenCount), Short.valueOf(pWifiproCloseCount), Short.valueOf(pActiveCheckRS_Same));
    }

    public void updateExcpRoParaPart1(short rssi_VALUE, short ota_PacketDropRate, short rttAvg, short tcpInSegs, short tcpOutSegs, short tcpRetransSegs, short wifi_NetSpeed, short ipQLevel) {
        logd("updateExcpRoParaPart1 call enter.");
        invokeMethod(false, "updateExcpRoParaPart1", Short.valueOf(rssi_VALUE), Short.valueOf(ota_PacketDropRate), Short.valueOf(rttAvg), Short.valueOf(tcpInSegs), Short.valueOf(tcpOutSegs), Short.valueOf(tcpRetransSegs), Short.valueOf(wifi_NetSpeed), Short.valueOf(ipQLevel));
    }

    public void updateExcpRoParaPart2(String ro_APSsid, short mobileSignalLevel, short ratType, short historyQuilityRO_Rate, short highDataRateRO_Rate, short creditScoreRO_Rate, short ro_Duration) {
        logd("updateExcpRoParaPart2 call enter.");
        invokeMethod(false, "updateExcpRoParaPart2", ro_APSsid, Short.valueOf(mobileSignalLevel), Short.valueOf(ratType), Short.valueOf(historyQuilityRO_Rate), Short.valueOf(highDataRateRO_Rate), Short.valueOf(creditScoreRO_Rate), Short.valueOf(ro_Duration));
    }

    public void updateWifiproTimeLen(short timeLen) {
        logd("updateWifiproTimeLen:" + timeLen);
        invokeMethod(false, "updateWifiproTimeLen", Short.valueOf(timeLen));
    }

    public void updateSSID(String ssid) {
        invokeMethod(false, "updateSSID", ssid);
    }

    public void updateBSSID(String bssid) {
        invokeMethod(false, "updateBSSID", bssid);
    }

    public void updateHomeAPJudgeTime(int hours) {
        invokeMethod(false, "updateHomeAPJudgeTime", Integer.valueOf(hours));
    }

    public void updateAPSecurityType(int secType) {
        invokeMethod(false, "updateAPSecurityType", Integer.valueOf(secType));
    }

    public void updateBG_AC_DiffType(int acDiffType) {
        invokeMethod(false, "updateBG_AC_DiffType", Integer.valueOf(acDiffType));
    }

    public void updateAutoOpenWhiteNum(short autoOpenWhiteNum) {
        logd("updateAutoOpenWhiteNum call enter.");
        invokeMethod(false, "updateAutoOpenWhiteNum", Short.valueOf(autoOpenWhiteNum));
    }

    public void updateAutoOpenRootCause(short autoOpenRootCause) {
        logd("updateAutoOpenRootCause call enter.");
        invokeMethod(false, "updateAutoOpenRootCause", Short.valueOf(autoOpenRootCause));
    }

    public void updateAutoCloseRootCause(short autoCloseRootCause) {
        logd("updateAutoCloseRootCause call enter.");
        invokeMethod(false, "updateAutoCloseRootCause", Short.valueOf(autoCloseRootCause));
    }

    public void updateFreeAP1Info(String ssid, String bssid) {
        invokeMethod(false, "updateFreeAP1Info", ssid, bssid);
    }

    public void updateFreeAP2Info(String ssid, String bssid) {
        invokeMethod(false, "updateFreeAP2Info", ssid, bssid);
    }

    public void updateFreeAP3Info(String ssid, String bssid) {
        invokeMethod(false, "updateFreeAP3Info", ssid, bssid);
    }

    public void updateFreeAPCellID(String freeAPcellID) {
        invokeMethod(false, "updateFreeAPCellID", freeAPcellID);
    }

    private static void logd(String msg) {
        if (printLogLevel <= 1) {
            Log.d(TAG, msg);
        }
    }

    private static void logi(String msg) {
        if (printLogLevel <= 2) {
            Log.i(TAG, msg);
        }
    }

    private static void loge(String msg) {
        if (printLogLevel <= 3) {
            Log.e(TAG, msg);
        }
    }
}
