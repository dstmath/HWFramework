package com.android.server.wifi;

import android.content.Context;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.util.Log;
import com.android.server.wifi.wifipro.WifiProStatisticsManager;
import com.android.server.wifi.wifipro.hwintelligencewifi.MessageUtil;
import java.io.File;

public class HwWifiDFTUtilImpl implements HwWifiDFTUtil {
    public static final short SUB_EVENT_DUALBAND_HANDOVER_PINGPONG = (short) 4;
    public static final short SUB_EVENT_DUALBAND_HANDOVER_SNAPSHOP = (short) 5;
    public static final short SUB_EVENT_DUALBAND_HANDOVER_TOO_SLOW = (short) 1;
    public static final short SUB_EVENT_DUALBAND_HANDOVER_TO_BAD_5G = (short) 2;
    public static final short SUB_EVENT_DUALBAND_HANDOVER_USER_REJECT = (short) 3;
    public static final short SUB_EVENT_DUALBAND_OTHER_REASON = (short) 0;
    private static final String TAG = "HwWifiDFTUtilImpl";
    private static HwWifiDFTUtil hwWifiDFTUtil;
    private static int mScanAlwaysSwCnt;
    private static int mWifiNotifationSwCnt;
    private static int mWifiProSwcnt;
    private static int mWifiSleepSwCnt;
    private static int mWifiToPdpSwCnt;
    private Context mContext;
    public short mOpenCloseFailed;
    private int mSettingNetAvailableNotify;
    private int mSettingScanAlways;
    private int mSettingSleepPolicy;
    private int mSettingWiFiProState;
    private int mSettingWiFiToPDP;
    private int mSubErrorCloseSupplicantConnectFailed;
    private int mSubErrorConnectSupplicantFailed;
    private int mSubErrorDriverFailed;
    private int mSubErrorFirmwareFailed;
    public int mSubErrorOfOpenCloseFailed;
    private int mSubErrorStartSupplicantFailed;
    private int mSubErrorTimeOut;
    private HwWifiCHRService mWifiCHRService;
    public short mWifiProExceptionDualbandSubError;

    public HwWifiDFTUtilImpl() {
        this.mWifiCHRService = null;
        this.mContext = null;
        this.mSettingScanAlways = -1;
        this.mSettingSleepPolicy = -1;
        this.mSettingNetAvailableNotify = -1;
        this.mSettingWiFiToPDP = -1;
        this.mSettingWiFiProState = -1;
        this.mSubErrorOfOpenCloseFailed = -1;
        this.mOpenCloseFailed = (short) -1;
        this.mSubErrorTimeOut = 1;
        this.mSubErrorDriverFailed = 2;
        this.mSubErrorFirmwareFailed = 3;
        this.mSubErrorStartSupplicantFailed = 4;
        this.mSubErrorConnectSupplicantFailed = 5;
        this.mSubErrorCloseSupplicantConnectFailed = 6;
        this.mWifiProExceptionDualbandSubError = (short) -1;
    }

    static {
        mScanAlwaysSwCnt = 0;
        mWifiSleepSwCnt = 0;
        mWifiNotifationSwCnt = 0;
        mWifiToPdpSwCnt = 0;
        mWifiProSwcnt = 0;
        hwWifiDFTUtil = new HwWifiDFTUtilImpl();
    }

    public static HwWifiDFTUtil getDefault() {
        return hwWifiDFTUtil;
    }

    public void updateWifiScanAlwaysState(Context mContext) {
        if (mContext != null) {
            try {
                int wifiScanAlwaysState = Global.getInt(mContext.getContentResolver(), "wifi_scan_always_enabled", 0);
                if (this.mSettingScanAlways != wifiScanAlwaysState) {
                    mScanAlwaysSwCnt++;
                    this.mSettingScanAlways = wifiScanAlwaysState;
                }
            } catch (Exception e) {
                Log.e(TAG, "update the wifi scan state error. ");
            }
        }
    }

    public int getScanAlwaysSwCnt() {
        return mScanAlwaysSwCnt;
    }

    public void updateWifiSleepPolicyState(Context mContext) {
        if (mContext != null) {
            try {
                int wifiSleepPolicyState = Global.getInt(mContext.getContentResolver(), "wifi_sleep_policy", 0);
                if (this.mSettingSleepPolicy != wifiSleepPolicyState) {
                    mWifiSleepSwCnt++;
                    this.mSettingSleepPolicy = wifiSleepPolicyState;
                }
            } catch (Exception e) {
                Log.e(TAG, "update the wifi sleep policy state error. ");
            }
        }
    }

    public int getWifiSleepSwCnt() {
        return mWifiSleepSwCnt;
    }

    public void updateWifiNetworkNotificationState(Context mContext) {
        if (mContext != null) {
            try {
                int wifiNotificationState = Global.getInt(mContext.getContentResolver(), "wifi_networks_available_notification_on", 0);
                if (this.mSettingNetAvailableNotify != wifiNotificationState) {
                    mWifiNotifationSwCnt++;
                    this.mSettingNetAvailableNotify = wifiNotificationState;
                }
            } catch (Exception e) {
                Log.e(TAG, "update the wifi network notification state error. ");
            }
        }
    }

    public int getWifiNotifationSwCnt() {
        return mWifiNotifationSwCnt;
    }

    public void updateWifiToPdpState(Context mContext) {
        if (mContext != null) {
            try {
                int wifiToPdpState = System.getInt(mContext.getContentResolver(), "wifi_to_pdp", 0);
                if (this.mSettingWiFiToPDP != wifiToPdpState) {
                    mWifiToPdpSwCnt++;
                    this.mSettingWiFiToPDP = wifiToPdpState;
                }
            } catch (Exception e) {
                Log.e(TAG, "update the wifi to pdp state error. ");
            }
        }
    }

    public int getWifiToPdpSwCnt() {
        return mWifiToPdpSwCnt;
    }

    public void updateWifiProState(Context mContext) {
        if (mContext != null) {
            try {
                int wifiProState = System.getInt(mContext.getContentResolver(), "smart_network_switching", 0);
                if (this.mSettingWiFiProState != wifiProState) {
                    mWifiProSwcnt++;
                    this.mSettingWiFiProState = wifiProState;
                }
            } catch (Exception e) {
                Log.e(TAG, "update the wifipro state error. ");
            }
        }
    }

    public int getWifiProSwcnt() {
        return mWifiProSwcnt;
    }

    public void checkAndCreatWifiLogDir() {
        try {
            File file = new File("/data/log/wifi");
            if (file.exists() || !file.mkdirs()) {
                Log.e(TAG, "creat directory failed");
            } else {
                Log.d(TAG, "creat directory successful");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public short getAccessNetFailedCount() {
        HwArpVerifier arpVerifier = HwArpVerifier.getDefault();
        return (short) (arpVerifier != null ? arpVerifier.getAccessNetFailedCount() : 0);
    }

    public boolean getWifiProState() {
        this.mWifiCHRService = HwWifiCHRServiceImpl.getDefault();
        if (this.mWifiCHRService != null) {
            return this.mWifiCHRService.getWIFIProStatus() == 11;
        } else {
            return false;
        }
    }

    public boolean getWifiAlwaysScanState() {
        this.mWifiCHRService = HwWifiCHRServiceImpl.getDefault();
        if (this.mWifiCHRService != null) {
            return this.mWifiCHRService.getPersistedScanAlwaysAvailable() == 1;
        } else {
            return false;
        }
    }

    public boolean getWifiNetworkNotificationState() {
        this.mWifiCHRService = HwWifiCHRServiceImpl.getDefault();
        if (this.mWifiCHRService != null) {
            return this.mWifiCHRService.getWIFINetworkAvailableNotificationON() == 1;
        } else {
            return false;
        }
    }

    public byte getWifiSleepPolicyState() {
        this.mWifiCHRService = HwWifiCHRServiceImpl.getDefault();
        if (this.mWifiCHRService != null) {
            return (byte) this.mWifiCHRService.getWIFISleepPolicy();
        }
        return (byte) 0;
    }

    public byte getWifiToPdpState() {
        this.mWifiCHRService = HwWifiCHRServiceImpl.getDefault();
        if (this.mWifiCHRService != null) {
            return (byte) this.mWifiCHRService.getWIFITOPDP();
        }
        return (byte) 0;
    }

    public void updateWifiDFTEvent(int type, String subErrorCode) {
        switch (type) {
            case HwWifiCHRConstImpl.WIFI_OPEN_FAILED /*80*/:
                try {
                    this.mOpenCloseFailed = (short) type;
                    if (subErrorCode.equals("TIMEOUT")) {
                        this.mSubErrorOfOpenCloseFailed = this.mSubErrorTimeOut;
                    } else if (subErrorCode.equals("DIRVER_FAILED")) {
                        this.mSubErrorOfOpenCloseFailed = this.mSubErrorDriverFailed;
                    } else if (subErrorCode.equals("FIRMWARE_FAILED")) {
                        this.mSubErrorOfOpenCloseFailed = this.mSubErrorFirmwareFailed;
                    } else if (subErrorCode.equals("START_SUPPLICANT_FAILED")) {
                        this.mSubErrorOfOpenCloseFailed = this.mSubErrorStartSupplicantFailed;
                    } else if (subErrorCode.equals("CONNECT_SUPPLICANT_FAILED")) {
                        this.mSubErrorOfOpenCloseFailed = this.mSubErrorConnectSupplicantFailed;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            case HwWifiCHRConstImpl.WIFI_CLOSE_FAILED /*81*/:
                this.mOpenCloseFailed = (short) type;
                if (subErrorCode.equals("TIMEOUT")) {
                    this.mSubErrorOfOpenCloseFailed = this.mSubErrorTimeOut;
                } else if (subErrorCode.equals("CLOSE_SUPPLICANT_CONNECT_FAILED")) {
                    this.mSubErrorOfOpenCloseFailed = this.mSubErrorCloseSupplicantConnectFailed;
                }
            case HwWifiCHRStateManagerImpl.WIFI_WIFIPRO_DUALBAND_EXCEPTION_EVENT /*125*/:
                if (subErrorCode.equals(WifiProStatisticsManager.SUB_EVENT_DUALBAND_HANDOVER_TOO_SLOW)) {
                    this.mWifiProExceptionDualbandSubError = SUB_EVENT_DUALBAND_HANDOVER_TOO_SLOW;
                } else if (subErrorCode.equals(WifiProStatisticsManager.SUB_EVENT_DUALBAND_HANDOVER_TO_BAD_5G)) {
                    this.mWifiProExceptionDualbandSubError = SUB_EVENT_DUALBAND_HANDOVER_TO_BAD_5G;
                } else if (subErrorCode.equals(WifiProStatisticsManager.SUB_EVENT_DUALBAND_HANDOVER_USER_REJECT)) {
                    this.mWifiProExceptionDualbandSubError = SUB_EVENT_DUALBAND_HANDOVER_USER_REJECT;
                } else if (subErrorCode.equals(WifiProStatisticsManager.SUB_EVENT_DUALBAND_HANDOVER_PINGPONG)) {
                    this.mWifiProExceptionDualbandSubError = SUB_EVENT_DUALBAND_HANDOVER_PINGPONG;
                } else if (subErrorCode.equals(WifiProStatisticsManager.SUB_EVENT_DUALBAND_HANDOVER_SNAPSHOP)) {
                    this.mWifiProExceptionDualbandSubError = SUB_EVENT_DUALBAND_HANDOVER_SNAPSHOP;
                }
            default:
        }
    }

    public int getDFTEventType(int type) {
        switch (type) {
            case HwWifiCHRConstImpl.WIFI_OPEN_FAILED /*80*/:
            case HwWifiCHRConstImpl.WIFI_CLOSE_FAILED /*81*/:
                return 909002011;
            case HwWifiCHRConstImpl.WIFI_ABNORMAL_DISCONNECT /*85*/:
                return 909002012;
            case HwWifiCHRConstImpl.WIFI_SCAN_FAILED /*86*/:
                return 909002013;
            case HwWifiCHRConstImpl.WIFI_ACCESS_INTERNET_FAILED /*87*/:
                return 909002015;
            case MessageUtil.CMD_START_SCAN /*102*/:
                return 909002016;
            case HwWifiCHRConstImpl.WIFI_WIFIPRO_EXCEPTION_EVENT /*122*/:
                return 909002018;
            case HwWifiCHRStateManagerImpl.WIFI_WIFIPRO_DUALBAND_EXCEPTION_EVENT /*125*/:
                return 909002019;
            case 208:
                return 909002017;
            default:
                return -1;
        }
    }

    public void clearSwCnt() {
        mScanAlwaysSwCnt = 0;
        mWifiSleepSwCnt = 0;
        mWifiNotifationSwCnt = 0;
        mWifiToPdpSwCnt = 0;
        mWifiProSwcnt = 0;
    }
}
