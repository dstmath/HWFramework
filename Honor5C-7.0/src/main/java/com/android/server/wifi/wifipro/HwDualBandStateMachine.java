package com.android.server.wifi.wifipro;

import android.common.HwFrameworkFactory;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiScanner;
import android.net.wifi.WifiScanner.ChannelSpec;
import android.net.wifi.WifiScanner.ScanData;
import android.net.wifi.WifiScanner.ScanListener;
import android.net.wifi.WifiScanner.ScanSettings;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.wifi.FrameworkFacade;
import com.android.server.wifi.wifipro.hwintelligencewifi.MessageUtil;
import com.android.server.wifi.wifipro.wifiscangenie.WifiScanGenieController;
import com.android.server.wifi.wifipro.wifiscangenie.WifiScanGenieDataBaseImpl;
import java.util.ArrayList;
import java.util.List;

public class HwDualBandStateMachine extends StateMachine {
    private static final int CHR_WIFI_HIGH_SCAN_FREQUENCY = 5;
    private static final int CHR_WIFI_MID_SCAN_FREQUENCY = 3;
    private static final int WIFI_MAX_SCAN_THRESHOLD = -30;
    private static final int WIFI_MIN_SCAN_THRESHOLD = -90;
    private static final int WIFI_RSSI_GAP = 10;
    private static final int WIFI_SCANNING_CHANNEL_INDEX = 3;
    private static final int WIFI_SCANNING_CHANNEL_INDEX_MAX = 5;
    private static final int WIFI_SCANNING_CHANNEL_INDEX_MIN = 0;
    private static final int WIFI_SCAN_INTERVAL = 5;
    private WifiProDualbandExceptionRecord mCHRHandoverTooSlow;
    private boolean mCHRIsManualHandover;
    private int mCHRMixAPScanCount;
    private List<HwDualBandMonitorInfo> mCHRSavedAPList;
    private int mCHRScanAPType;
    private int mCHRSingleAPScanCount;
    private State mConnectedState;
    private Context mContext;
    private final CustomizedScanListener mCustomizedScanListener;
    private State mDefaultState;
    private State mDisabledState;
    private List<HwDualBandMonitorInfo> mDisappearAPList;
    private State mDisconnectedState;
    private FrameworkFacade mFrameworkFacade;
    private HwDualBandAdaptiveThreshold mHwDualBandAdaptiveThreshold;
    private HwDualBandInformationManager mHwDualBandInformationManager;
    private HwDualBandRelationManager mHwDualBandRelationManager;
    private HwDualBandWiFiMonitor mHwDualBandWiFiMonitor;
    private IDualBandManagerCallback mIDualbandManagerCallback;
    private State mInternetReadyState;
    private boolean mIsDualbandScanning;
    private List<HwDualBandMonitorInfo> mMonitorAPList;
    private State mMonitorState;
    private State mStopState;
    private List<HwDualBandMonitorInfo> mTargetAPList;
    private WifiManager mWifiManager;
    private WifiScanner mWifiScanner;

    class ConnectedState extends State {
        ConnectedState() {
        }

        public void enter() {
            Log.e(HwDualBandMessageUtil.TAG, "Enter ConnectedState");
            WifiInfo mWifiInfo = HwDualBandStateMachine.this.mWifiManager.getConnectionInfo();
            if (mWifiInfo != null && mWifiInfo.getBSSID() != null) {
                Log.e(HwDualBandMessageUtil.TAG, "Enter ConnectedState ssid = " + mWifiInfo.getSSID());
            }
        }

        public void exit() {
            HwDualBandStateMachine.this.removeMessages(11);
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case WifiScanGenieController.MSG_CONFIGURED_CHANGED /*1*/:
                    break;
                case MessageUtil.MSG_WIFI_INTERNET_CONNECTED /*11*/:
                    Log.e(HwDualBandMessageUtil.TAG, "MSG_WIFI_INTERNET_CONNECTED");
                    WifiInfo mInternetWifiInfo = HwDualBandStateMachine.this.mWifiManager.getConnectionInfo();
                    if (mInternetWifiInfo != null && mInternetWifiInfo.getBSSID() != null) {
                        HwDualBandStateMachine.this.transitionTo(HwDualBandStateMachine.this.mInternetReadyState);
                        HwDualBandStateMachine.this.sendMessage(HwDualBandMessageUtil.CMD_UPDATE_AP_INFO);
                        break;
                    }
                    if (mInternetWifiInfo == null) {
                        Log.e(HwDualBandMessageUtil.TAG, "MSG_WIFI_INTERNET_CONNECTED mInternetWifiInfo == null");
                    } else {
                        Log.e(HwDualBandMessageUtil.TAG, "MSG_WIFI_INTERNET_CONNECTED mInternetWifiInfo.getBSSID() == null");
                    }
                    HwDualBandStateMachine.this.sendMessageDelayed(11, 2000);
                    return true;
                case MessageUtil.MSG_WIFI_INTERNET_DISCONNECTED /*12*/:
                case MessageUtil.MSG_WIFI_IS_PORTAL /*13*/:
                    Log.e(HwDualBandMessageUtil.TAG, "MSG_WIFI_INTERNET_DISCONNECTED");
                    WifiInfo mNoInternetWifiInfo = HwDualBandStateMachine.this.mWifiManager.getConnectionInfo();
                    if (!(mNoInternetWifiInfo == null || mNoInternetWifiInfo.getBSSID() == null)) {
                        WifiProDualBandApInfoRcd info = HwDualBandStateMachine.this.mHwDualBandInformationManager.getDualBandAPInfo(mNoInternetWifiInfo.getBSSID());
                        if (info != null) {
                            info.mInetCapability = Short.valueOf((short) 2);
                            HwDualBandStateMachine.this.mHwDualBandInformationManager.updateAPInfo(info);
                            break;
                        }
                    }
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    private class CustomizedScanListener implements ScanListener {
        private CustomizedScanListener() {
        }

        public void onSuccess() {
            Log.d(HwDualBandMessageUtil.TAG, "CustomizedScanListener onSuccess");
        }

        public void onFailure(int reason, String description) {
            Log.d(HwDualBandMessageUtil.TAG, "CustomizedScanListener onFailure");
        }

        public void onPeriodChanged(int periodInMs) {
            Log.d(HwDualBandMessageUtil.TAG, "CustomizedScanListener onPeriodChanged");
        }

        public void onResults(ScanData[] results) {
            Log.d(HwDualBandMessageUtil.TAG, "CustomizedScanListener onResults");
            HwDualBandStateMachine.this.sendMessage(7);
        }

        public void onFullResult(ScanResult fullScanResult) {
            Log.d(HwDualBandMessageUtil.TAG, "CustomizedScanListener onFullResult");
        }
    }

    class DefaultState extends State {
        DefaultState() {
        }

        public void enter() {
            Log.e(HwDualBandMessageUtil.TAG, "Enter DefaultState");
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case WifiScanGenieController.MSG_CONFIGURED_CHANGED /*1*/:
                    Log.e(HwDualBandMessageUtil.TAG, "DefaultState MSG_WIFI_CONNECTED");
                    HwDualBandStateMachine.this.reportDualbandChrHandoverTooSlow();
                    HwDualBandStateMachine.this.transitionTo(HwDualBandStateMachine.this.mConnectedState);
                    break;
                case WifiScanGenieDataBaseImpl.DATABASE_VERSION /*2*/:
                    HwDualBandStateMachine.this.transitionTo(HwDualBandStateMachine.this.mDisconnectedState);
                    break;
                case MessageUtil.MSG_WIFI_DISABLE /*4*/:
                    HwDualBandStateMachine.this.transitionTo(HwDualBandStateMachine.this.mDisabledState);
                    break;
                case MessageUtil.MSG_WIFI_CONFIG_CHANGED /*8*/:
                    Log.e(HwDualBandMessageUtil.TAG, "DefaultState MSG_WIFI_CONFIG_CHANGED");
                    Bundle data = message.getData();
                    String bssid = data.getString(WifiScanGenieDataBaseImpl.CHANNEL_TABLE_BSSID);
                    String ssid = data.getString(WifiScanGenieDataBaseImpl.CHANNEL_TABLE_SSID);
                    int authtype = data.getInt(HwDualBandMessageUtil.MSG_KEY_AUTHTYPE);
                    Log.e(HwDualBandMessageUtil.TAG, "MSG_WIFI_CONFIG_CHANGED bssid = " + bssid + " ssid = " + ssid);
                    if (ssid != null) {
                        HwDualBandStateMachine.this.mHwDualBandInformationManager.delectDualBandAPInfoBySsid(ssid, authtype);
                        break;
                    }
                    break;
                case MessageUtil.CMD_ON_STOP /*101*/:
                    HwDualBandStateMachine.this.transitionTo(HwDualBandStateMachine.this.mStopState);
                    break;
            }
            return true;
        }
    }

    class DisabledState extends State {
        DisabledState() {
        }

        public void enter() {
            Log.e(HwDualBandMessageUtil.TAG, "Enter DisabledState");
            if (!(HwDualBandStateMachine.this.mMonitorAPList == null || HwDualBandStateMachine.this.mMonitorAPList.size() == 0)) {
                HwDualBandStateMachine.this.mMonitorAPList.clear();
            }
            if (HwDualBandStateMachine.this.mTargetAPList != null && HwDualBandStateMachine.this.mTargetAPList.size() != 0) {
                HwDualBandStateMachine.this.mTargetAPList.clear();
            }
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case MessageUtil.MSG_WIFI_DISABLE /*4*/:
                    return true;
                default:
                    return false;
            }
        }
    }

    class DisconnectedState extends State {
        DisconnectedState() {
        }

        public void enter() {
            Log.e(HwDualBandMessageUtil.TAG, "Enter DisconnectedState");
            if (!(HwDualBandStateMachine.this.mMonitorAPList == null || HwDualBandStateMachine.this.mMonitorAPList.size() == 0)) {
                HwDualBandStateMachine.this.mMonitorAPList.clear();
            }
            if (!(HwDualBandStateMachine.this.mTargetAPList == null || HwDualBandStateMachine.this.mTargetAPList.size() == 0)) {
                HwDualBandStateMachine.this.mTargetAPList.clear();
            }
            if (HwDualBandStateMachine.this.mDisappearAPList != null && HwDualBandStateMachine.this.mDisappearAPList.size() != 0) {
                HwDualBandStateMachine.this.mDisappearAPList.clear();
            }
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case WifiScanGenieDataBaseImpl.DATABASE_VERSION /*2*/:
                    return true;
                default:
                    return false;
            }
        }
    }

    class InternetReadyState extends State {
        private String mCurrentBSSID;
        private String mCurrentSSID;

        InternetReadyState() {
            this.mCurrentSSID = null;
            this.mCurrentBSSID = null;
        }

        public void enter() {
            Log.e(HwDualBandMessageUtil.TAG, "Enter InternetReadyState");
            WifiInfo mWifiInfo = HwDualBandStateMachine.this.mWifiManager.getConnectionInfo();
            if (mWifiInfo == null || mWifiInfo.getBSSID() == null) {
                Log.e(HwDualBandMessageUtil.TAG, "Enter InternetReadyState error info");
                return;
            }
            this.mCurrentSSID = mWifiInfo.getSSID();
            this.mCurrentBSSID = mWifiInfo.getBSSID();
            Log.e(HwDualBandMessageUtil.TAG, "Enter InternetReadyState mCurrentSSID = " + this.mCurrentSSID + " mCurrentBSSID = " + this.mCurrentBSSID);
        }

        public boolean processMessage(Message message) {
            WifiProDualBandApInfoRcd m5GAPInfo;
            HwDualBandMonitorInfo mHwDualBandMonitorInfo;
            switch (message.what) {
                case WifiScanGenieController.MSG_CONFIGURED_CHANGED /*1*/:
                    WifiInfo mConnectedWifiInfo = HwDualBandStateMachine.this.mWifiManager.getConnectionInfo();
                    if (!(mConnectedWifiInfo == null || mConnectedWifiInfo.getBSSID() == null || mConnectedWifiInfo.getBSSID().equals(this.mCurrentBSSID))) {
                        Log.e(HwDualBandMessageUtil.TAG, "InternetReadyState MSG_WIFI_CONNECTED mConnectedWifiInfo.getBSSID() = " + mConnectedWifiInfo.getBSSID() + " mCurrentBSSID = " + this.mCurrentBSSID);
                        HwDualBandStateMachine.this.transitionTo(HwDualBandStateMachine.this.mConnectedState);
                        break;
                    }
                case MessageUtil.MSG_WIFI_INTERNET_CONNECTED /*11*/:
                case MessageUtil.MSG_WIFI_IS_PORTAL /*13*/:
                    break;
                case MessageUtil.MSG_WIFI_INTERNET_DISCONNECTED /*12*/:
                    Log.e(HwDualBandMessageUtil.TAG, "InternetReadyState MSG_WIFI_INTERNET_DISCONNECTED");
                    HwDualBandStateMachine.this.transitionTo(HwDualBandStateMachine.this.mConnectedState);
                    HwDualBandStateMachine.this.sendMessage(12);
                    break;
                case WifiProStatisticsManager.DUALBAND_MIX_AP_SATISFIED_COUNT /*16*/:
                    Log.e(HwDualBandMessageUtil.TAG, "MSG_DUAL_BAND_WIFI_TYPE_SINGLE");
                    WifiInfo mWifiInfo = HwDualBandStateMachine.this.mWifiManager.getConnectionInfo();
                    if (!(mWifiInfo == null || mWifiInfo.getBSSID() == null)) {
                        WifiProDualBandApInfoRcd apinfo = HwDualBandStateMachine.this.mHwDualBandInformationManager.getDualBandAPInfo(mWifiInfo.getBSSID());
                        if (apinfo != null) {
                            List<WifiProRelateApRcd> mLists = apinfo.getRelateApRcds();
                            if (mLists.size() > 0) {
                                Log.e(HwDualBandMessageUtil.TAG, "MSG_DUAL_BAND_WIFI_TYPE_SINGLE mLists.size() = " + mLists.size());
                                WifiProRelateApRcd info = (WifiProRelateApRcd) mLists.get(HwDualBandStateMachine.WIFI_SCANNING_CHANNEL_INDEX_MIN);
                                m5GAPInfo = HwDualBandStateMachine.this.mHwDualBandInformationManager.getDualBandAPInfo(info.mRelatedBSSID);
                                if (m5GAPInfo != null) {
                                    if (m5GAPInfo.isInBlackList != 1) {
                                        mHwDualBandMonitorInfo = new HwDualBandMonitorInfo(info.mRelatedBSSID, m5GAPInfo.mApSSID, m5GAPInfo.mApAuthType.shortValue(), HwDualBandStateMachine.WIFI_SCANNING_CHANNEL_INDEX_MIN, HwDualBandStateMachine.WIFI_SCANNING_CHANNEL_INDEX_MIN, info.mRelateType);
                                        mHwDualBandMonitorInfo.mIsNearAP = 1;
                                        List<HwDualBandMonitorInfo> apList = new ArrayList();
                                        Log.e(HwDualBandMessageUtil.TAG, "MSG_DUAL_BAND_WIFI_TYPE_SINGLE find bssid = " + mHwDualBandMonitorInfo.mBssid + " ssid = " + mHwDualBandMonitorInfo.mSsid + " m5GAPInfo.mApAuthType = " + m5GAPInfo.mApAuthType);
                                        apList.add(mHwDualBandMonitorInfo);
                                        HwDualBandStateMachine.this.mIDualbandManagerCallback.onDualBandNetWorkType(1, apList);
                                        WifiProStatisticsManager.getInstance().increaseDualbandStatisticCount(2);
                                        break;
                                    }
                                    Log.e(HwDualBandMessageUtil.TAG, "MSG_DUAL_BAND_WIFI_TYPE_SINGLE m5GAPInfo.isInBlackList = " + m5GAPInfo.isInBlackList);
                                    break;
                                }
                                Log.e(HwDualBandMessageUtil.TAG, "MSG_DUAL_BAND_WIFI_TYPE_SINGLE m5GAPInfo == null");
                                break;
                            }
                            Log.e(HwDualBandMessageUtil.TAG, "MSG_DUAL_BAND_WIFI_TYPE_SINGLE mLists.size() <= 0");
                            break;
                        }
                        Log.e(HwDualBandMessageUtil.TAG, "MSG_DUAL_BAND_WIFI_TYPE_SINGLE apinfo == null");
                        break;
                    }
                case WifiProStatisticsManager.DUALBAND_MIX_AP_DISAPPER_COUNT /*17*/:
                    Log.e(HwDualBandMessageUtil.TAG, "MSG_DUAL_BAND_WIFI_TYPE_MIX");
                    WifiInfo mMixWifiInfo = HwDualBandStateMachine.this.mWifiManager.getConnectionInfo();
                    if (!(mMixWifiInfo == null || mMixWifiInfo.getBSSID() == null)) {
                        List<HwDualBandMonitorInfo> mMixAPList = new ArrayList();
                        WifiProDualBandApInfoRcd mMixAPinfo = HwDualBandStateMachine.this.mHwDualBandInformationManager.getDualBandAPInfo(mMixWifiInfo.getBSSID());
                        if (mMixAPinfo != null) {
                            List<WifiProRelateApRcd> mMixLists = mMixAPinfo.getRelateApRcds();
                            Log.e(HwDualBandMessageUtil.TAG, "MSG_DUAL_BAND_WIFI_TYPE_MIX mMixLists.size() = " + mMixLists.size());
                            for (WifiProRelateApRcd record : mMixLists) {
                                m5GAPInfo = HwDualBandStateMachine.this.mHwDualBandInformationManager.getDualBandAPInfo(record.mRelatedBSSID);
                                if (m5GAPInfo == null) {
                                    Log.e(HwDualBandMessageUtil.TAG, "MSG_DUAL_BAND_WIFI_TYPE_MIX m5GAPInfo == null");
                                } else if (m5GAPInfo.isInBlackList == 1) {
                                    Log.e(HwDualBandMessageUtil.TAG, "MSG_DUAL_BAND_WIFI_TYPE_MIX m5GAPInfo.mIsInblackList = " + m5GAPInfo.isInBlackList);
                                } else if (m5GAPInfo.mInetCapability.shortValue() == (short) 1) {
                                    mHwDualBandMonitorInfo = new HwDualBandMonitorInfo(m5GAPInfo.apBSSID, m5GAPInfo.mApSSID, m5GAPInfo.mApAuthType.shortValue(), HwDualBandStateMachine.WIFI_SCANNING_CHANNEL_INDEX_MIN, HwDualBandStateMachine.WIFI_SCANNING_CHANNEL_INDEX_MIN, record.mRelateType);
                                    if (mHwDualBandMonitorInfo.mIsDualbandAP == 1 || isNearAP(record)) {
                                        mHwDualBandMonitorInfo.mIsNearAP = 1;
                                    } else {
                                        mHwDualBandMonitorInfo.mIsNearAP = HwDualBandStateMachine.WIFI_SCANNING_CHANNEL_INDEX_MIN;
                                    }
                                    Log.e(HwDualBandMessageUtil.TAG, "MSG_DUAL_BAND_WIFI_TYPE_MIX find bssid = " + mHwDualBandMonitorInfo.mBssid + " ssid = " + mHwDualBandMonitorInfo.mSsid + " m5GAPInfo.mApAuthType = " + m5GAPInfo.mApAuthType + " mIsNearAP = " + mHwDualBandMonitorInfo.mIsNearAP);
                                    mMixAPList.add(mHwDualBandMonitorInfo);
                                } else {
                                    Log.e(HwDualBandMessageUtil.TAG, "bssid = " + m5GAPInfo.apBSSID + " ssid = " + m5GAPInfo.mApSSID + " have no internet");
                                }
                            }
                            Log.e(HwDualBandMessageUtil.TAG, "MSG_DUAL_BAND_WIFI_TYPE_MIX after filter mMixAPList.size() = " + mMixAPList.size());
                            if (mMixAPList.size() > 0) {
                                HwDualBandStateMachine.this.mIDualbandManagerCallback.onDualBandNetWorkType(2, mMixAPList);
                                WifiProStatisticsManager.getInstance().increaseDualbandStatisticCount(14);
                                break;
                            }
                        }
                        Log.e(HwDualBandMessageUtil.TAG, "MSG_DUAL_BAND_WIFI_TYPE_MIX mMixAPinfo == null");
                        break;
                    }
                    break;
                case WifiProStatisticsManager.DUALBAND_MIX_AP_SCORE_NOTSATISFY_COUNT /*19*/:
                    Log.e(HwDualBandMessageUtil.TAG, "InternetReadyState MSG_WIFI_VERIFYING_POOR_LINK");
                    HwDualBandStateMachine.this.transitionTo(HwDualBandStateMachine.this.mConnectedState);
                    break;
                case MessageUtil.CMD_START_SCAN /*102*/:
                    HwDualBandStateMachine.this.mTargetAPList = message.getData().getParcelableArrayList(HwDualBandMessageUtil.MSG_KEY_APLIST);
                    if (HwDualBandStateMachine.this.mTargetAPList != null) {
                        Log.e(HwDualBandMessageUtil.TAG, "CMD_START_MONITOR size = " + HwDualBandStateMachine.this.mTargetAPList.size());
                    }
                    HwDualBandStateMachine.this.transitionTo(HwDualBandStateMachine.this.mMonitorState);
                    break;
                case HwDualBandMessageUtil.CMD_UPDATE_AP_INFO /*104*/:
                    WifiInfo mInternetWifiInfo = HwDualBandStateMachine.this.mWifiManager.getConnectionInfo();
                    if (mInternetWifiInfo != null && mInternetWifiInfo.getBSSID() != null) {
                        if (!HwDualBandStateMachine.this.mHwDualBandInformationManager.isEnterpriseAP(mInternetWifiInfo.getBSSID()) && !isMobileAP() && !HwDualBandStateMachine.this.mHwDualBandInformationManager.isHaveMultipleAP(mInternetWifiInfo.getBSSID(), mInternetWifiInfo.getSSID(), HwDualBandStateMachine.this.mHwDualBandInformationManager.getAuthType(mInternetWifiInfo.getNetworkId()))) {
                            HwDualBandStateMachine.this.mHwDualBandInformationManager.saveAPInfo();
                            HwDualBandStateMachine.this.mHwDualBandRelationManager.updateAPRelation();
                            break;
                        }
                        Log.e(HwDualBandMessageUtil.TAG, "InternetReadyState isEnterpriseAP");
                        HwDualBandStateMachine.this.mHwDualBandInformationManager.delectDualBandAPInfoBySsid(mInternetWifiInfo.getSSID(), HwDualBandStateMachine.this.mHwDualBandInformationManager.getAuthType(mInternetWifiInfo.getNetworkId()));
                        break;
                    }
                    Log.e(HwDualBandMessageUtil.TAG, "InternetReadyState mInternetWifiInfo == null");
                    break;
                    break;
                default:
                    return false;
            }
            return true;
        }

        private boolean isNearAP(WifiProRelateApRcd record) {
            if (record.mMaxRelatedRSSI != 0 && record.mMinCurrentRSSI != 0) {
                return record.mMaxCurrentRSSI - record.mMinRelatedRSSI <= HwDualBandStateMachine.WIFI_RSSI_GAP && record.mMaxRelatedRSSI - record.mMinCurrentRSSI <= HwDualBandStateMachine.WIFI_RSSI_GAP;
            } else {
                if (record.mMaxCurrentRSSI - record.mMinRelatedRSSI <= HwDualBandStateMachine.WIFI_RSSI_GAP) {
                    return true;
                }
            }
        }

        private boolean isMobileAP() {
            if (HwDualBandStateMachine.this.mContext != null) {
                return HwFrameworkFactory.getHwInnerWifiManager().getHwMeteredHint(HwDualBandStateMachine.this.mContext);
            }
            return false;
        }
    }

    class MonitorState extends State {
        private String m24GBssid;
        private int m24GRssi;
        private int mScanIndex;

        MonitorState() {
            this.m24GBssid = null;
            this.m24GRssi = -1;
            this.mScanIndex = HwDualBandStateMachine.WIFI_SCANNING_CHANNEL_INDEX_MIN;
        }

        public void enter() {
            Log.e(HwDualBandMessageUtil.TAG, "Enter MonitorState");
            if (HwDualBandStateMachine.this.mWifiScanner == null) {
                HwDualBandStateMachine.this.mFrameworkFacade = new FrameworkFacade();
                HwDualBandStateMachine.this.mWifiScanner = HwDualBandStateMachine.this.mFrameworkFacade.makeWifiScanner(HwDualBandStateMachine.this.mContext, HwDualBandStateMachine.this.getHandler().getLooper());
            }
            HwDualBandStateMachine.this.mIsDualbandScanning = true;
            this.mScanIndex = HwDualBandStateMachine.WIFI_SCANNING_CHANNEL_INDEX_MIN;
            HwDualBandStateMachine.this.mCHRSingleAPScanCount = HwDualBandStateMachine.WIFI_SCANNING_CHANNEL_INDEX_MIN;
            HwDualBandStateMachine.this.mCHRMixAPScanCount = HwDualBandStateMachine.WIFI_SCANNING_CHANNEL_INDEX_MIN;
            HwDualBandStateMachine.this.mCHRScanAPType = HwDualBandStateMachine.WIFI_SCANNING_CHANNEL_INDEX_MIN;
            this.m24GBssid = null;
            this.m24GRssi = -1;
            HwDualBandStateMachine.this.mDisappearAPList.clear();
            WifiInfo mWifiInfo = HwDualBandStateMachine.this.mWifiManager.getConnectionInfo();
            if (mWifiInfo == null) {
                Log.e(HwDualBandMessageUtil.TAG, "mWifiInfo is null");
                return;
            }
            this.m24GBssid = mWifiInfo.getBSSID();
            for (HwDualBandMonitorInfo info : HwDualBandStateMachine.this.mTargetAPList) {
                if (info.mDualBandApInfoRcd == null) {
                    Log.e(HwDualBandMessageUtil.TAG, "MonitorState info.mDualBandApInfoRcd == null, ssid = " + info.mSsid);
                } else if (info.mIsDualbandAP == 1) {
                    info.mScanRssi = HwDualBandStateMachine.this.mHwDualBandAdaptiveThreshold.getScanRSSIThreshold(mWifiInfo.getBSSID(), info.mBssid, info.mTargetRssi);
                    Log.e(HwDualBandMessageUtil.TAG, "MonitorState isdulabanAP 2.4G = " + mWifiInfo.getSSID() + " 5G = " + info.mSsid + " 5G bssid = " + info.mBssid + " info.mScanRssi = " + info.mScanRssi + " info.mTargetRssi = " + info.mTargetRssi);
                    HwDualBandStateMachine.this.mMonitorAPList.add(info);
                } else {
                    WifiProDualBandApInfoRcd wifiProDualBandApInfoRcd = null;
                    WifiProRelateApRcd RelationInfo = null;
                    if (mWifiInfo.getBSSID() != null) {
                        wifiProDualBandApInfoRcd = HwDualBandStateMachine.this.mHwDualBandInformationManager.getDualBandAPInfo(mWifiInfo.getBSSID());
                        RelationInfo = HwDualBandStateMachine.this.mHwDualBandRelationManager.getRelateAPInfo(mWifiInfo.getBSSID(), info.mBssid);
                    }
                    if (!(RelationInfo == null || wifiProDualBandApInfoRcd == null)) {
                        if (info.mIsNearAP != 1 || RelationInfo.mMinCurrentRSSI == 0) {
                            info.mScanRssi = RelationInfo.mMaxCurrentRSSI;
                        } else {
                            info.mScanRssi = info.mTargetRssi - 5;
                        }
                        info.mInitializationRssi = info.mScanRssi;
                        Log.e(HwDualBandMessageUtil.TAG, "MonitorState mix AP 2.4G = " + mWifiInfo.getSSID() + " 5G = " + info.mSsid + " 5G bssid = " + info.mBssid + " info.mScanRssi = " + info.mScanRssi + " info.mTargetRssi = " + info.mTargetRssi + " info.mAuthType = " + info.mAuthType + " info.mIsNearAP = " + info.mIsNearAP + " APInfo.mChannelFrequency = " + wifiProDualBandApInfoRcd.mChannelFrequency + " APInfo.mApAuthType = " + wifiProDualBandApInfoRcd.mApAuthType);
                        HwDualBandStateMachine.this.mMonitorAPList.add(info);
                    }
                }
            }
            if (HwDualBandStateMachine.this.mMonitorAPList.size() <= 0) {
                HwDualBandStateMachine.this.mIDualbandManagerCallback.onDualBandNetWorkType(HwDualBandStateMachine.WIFI_SCANNING_CHANNEL_INDEX_MIN, HwDualBandStateMachine.this.mMonitorAPList);
                HwDualBandStateMachine.this.sendMessage(HwDualBandMessageUtil.CMD_STOP_MONITOR);
            } else {
                HwDualBandStateMachine.this.reportDualbandChrMonitorCount();
            }
        }

        public void exit() {
            HwDualBandStateMachine.this.mIsDualbandScanning = false;
            this.mScanIndex = HwDualBandStateMachine.WIFI_SCANNING_CHANNEL_INDEX_MIN;
            if (!(HwDualBandStateMachine.this.mMonitorAPList == null || HwDualBandStateMachine.this.mMonitorAPList.size() == 0)) {
                HwDualBandStateMachine.this.reportDualbandChrFreqScan5GCount();
                HwDualBandStateMachine.this.mMonitorAPList.clear();
            }
            HwDualBandStateMachine.this.mCHRSingleAPScanCount = HwDualBandStateMachine.WIFI_SCANNING_CHANNEL_INDEX_MIN;
            HwDualBandStateMachine.this.mCHRMixAPScanCount = HwDualBandStateMachine.WIFI_SCANNING_CHANNEL_INDEX_MIN;
            HwDualBandStateMachine.this.mCHRScanAPType = HwDualBandStateMachine.WIFI_SCANNING_CHANNEL_INDEX_MIN;
        }

        public boolean processMessage(Message message) {
            int i = HwDualBandStateMachine.WIFI_SCAN_INTERVAL;
            switch (message.what) {
                case WifiScanGenieDataBaseImpl.DATABASE_VERSION /*2*/:
                    HwDualBandStateMachine.this.initDualbandChrHandoverTooSlow(this.m24GBssid, this.m24GRssi);
                    HwDualBandStateMachine.this.transitionTo(HwDualBandStateMachine.this.mDisconnectedState);
                    break;
                case MessageUtil.MSG_WIFI_UPDATE_SCAN_RESULT /*7*/:
                    Log.e(HwDualBandMessageUtil.TAG, "MonitorState MSG_WIFI_UPDATE_SCAN_RESULT");
                    List<ScanResult> mLists = HwDualBandStateMachine.this.mWifiManager.getScanResults();
                    if (mLists.size() > 0 && isSatisfiedScanResult(mLists)) {
                        Log.e(HwDualBandMessageUtil.TAG, "MonitorState MSG_WIFI_UPDATE_SCAN_RESULT find AP");
                        HwDualBandStateMachine.this.transitionTo(HwDualBandStateMachine.this.mInternetReadyState);
                        break;
                    }
                case MessageUtil.MSG_WIFI_CONFIG_CHANGED /*8*/:
                    Bundle remove_data = message.getData();
                    String bssid = remove_data.getString(WifiScanGenieDataBaseImpl.CHANNEL_TABLE_BSSID);
                    String ssid = remove_data.getString(WifiScanGenieDataBaseImpl.CHANNEL_TABLE_SSID);
                    int authtype = remove_data.getInt(HwDualBandMessageUtil.MSG_KEY_AUTHTYPE);
                    Log.e(HwDualBandMessageUtil.TAG, "MonitorState MSG_WIFI_REMOVE_CONFIG_CHANGED bssid = " + bssid + " ssid = " + ssid);
                    if (ssid != null) {
                        HwDualBandStateMachine.this.mHwDualBandInformationManager.delectDualBandAPInfoBySsid(ssid, authtype);
                        removeFromMonitorList(ssid);
                        Log.e(HwDualBandMessageUtil.TAG, "MonitorState MSG_WIFI_REMOVE_CONFIG_CHANGED mMonitorAPList.size() = " + HwDualBandStateMachine.this.mMonitorAPList.size());
                        HwDualBandStateMachine.this.mIDualbandManagerCallback.onDualBandNetWorkType(HwDualBandStateMachine.WIFI_SCANNING_CHANNEL_INDEX_MIN, HwDualBandStateMachine.this.mMonitorAPList);
                        HwDualBandStateMachine.this.transitionTo(HwDualBandStateMachine.this.mInternetReadyState);
                        break;
                    }
                    break;
                case WifiProStatisticsManager.DUALBAND_MIX_AP_INBLACK_LIST_COUNT /*18*/:
                    this.m24GRssi = message.getData().getInt(HwDualBandMessageUtil.MSG_KEY_RSSI);
                    Log.e(HwDualBandMessageUtil.TAG, "MonitorState m24GRssi = " + this.m24GRssi);
                    if (isSatisfiedScanCondition(this.m24GRssi)) {
                        if (this.mScanIndex < HwDualBandStateMachine.WIFI_SCANNING_CHANNEL_INDEX) {
                            ScanSettings settings = getCustomizedScanSettings();
                            Log.e(HwDualBandMessageUtil.TAG, "startScan for restrict channels, mScanIndex = " + this.mScanIndex);
                            if (settings != null) {
                                HwDualBandStateMachine.this.startCustomizedScan(settings);
                            } else {
                                HwDualBandStateMachine.this.mWifiManager.startScan();
                            }
                        } else {
                            Log.e(HwDualBandMessageUtil.TAG, "startScan for full channels, mScanIndex = " + this.mScanIndex);
                            HwDualBandStateMachine.this.mWifiManager.startScan();
                        }
                        this.mScanIndex++;
                        if (this.mScanIndex <= HwDualBandStateMachine.WIFI_SCAN_INTERVAL) {
                            i = this.mScanIndex;
                        }
                        this.mScanIndex = i;
                        HwDualBandStateMachine hwDualBandStateMachine;
                        if (HwDualBandStateMachine.this.mCHRScanAPType != 1) {
                            if (HwDualBandStateMachine.this.mCHRScanAPType == 2) {
                                hwDualBandStateMachine = HwDualBandStateMachine.this;
                                hwDualBandStateMachine.mCHRMixAPScanCount = hwDualBandStateMachine.mCHRMixAPScanCount + 1;
                                break;
                            }
                        }
                        hwDualBandStateMachine = HwDualBandStateMachine.this;
                        hwDualBandStateMachine.mCHRSingleAPScanCount = hwDualBandStateMachine.mCHRSingleAPScanCount + 1;
                        break;
                    }
                    break;
                case WifiProStatisticsManager.DUALBAND_MIX_AP_SCORE_NOTSATISFY_COUNT /*19*/:
                    Log.e(HwDualBandMessageUtil.TAG, "MonitorState MSG_WIFI_VERIFYING_POOR_LINK");
                    HwDualBandStateMachine.this.transitionTo(HwDualBandStateMachine.this.mConnectedState);
                    break;
                case HwDualBandMessageUtil.CMD_STOP_MONITOR /*103*/:
                    Log.e(HwDualBandMessageUtil.TAG, "MonitorState CMD_STOP_MONITOR");
                    HwDualBandStateMachine.this.transitionTo(HwDualBandStateMachine.this.mInternetReadyState);
                    break;
                default:
                    return false;
            }
            return true;
        }

        private boolean isSatisfiedScanCondition(int rssi) {
            for (HwDualBandMonitorInfo info : HwDualBandStateMachine.this.mMonitorAPList) {
                HwDualBandStateMachine.this.mCHRScanAPType = info.mIsDualbandAP;
                if (info.mIsNearAP == 1) {
                    if (rssi >= info.mScanRssi) {
                        return true;
                    }
                } else if (rssi <= HwDualBandStateMachine.WIFI_MIN_SCAN_THRESHOLD || rssi > info.mInitializationRssi) {
                    info.mScanRssi = info.mInitializationRssi;
                    return false;
                } else if (rssi <= info.mScanRssi) {
                    return true;
                }
            }
            return false;
        }

        private boolean isSatisfiedScanResult(List<ScanResult> mLists) {
            int scanResultsFound = HwDualBandStateMachine.WIFI_SCANNING_CHANNEL_INDEX_MIN;
            List<HwDualBandMonitorInfo> mAPList = new ArrayList();
            for (ScanResult result : mLists) {
                if (this.m24GBssid != null && this.m24GBssid.equals(result.BSSID)) {
                    this.m24GRssi = result.level;
                    Log.e(HwDualBandMessageUtil.TAG, "isSatisfiedScanResult m24GRssi = " + this.m24GRssi);
                }
                if (HwDualBandStateMachine.this.mDisappearAPList.size() > 0) {
                    for (HwDualBandMonitorInfo info : HwDualBandStateMachine.this.mDisappearAPList) {
                        if (info.mBssid.equals(result.BSSID) && result.SSID != null && result.SSID.length() > 0 && !isInMonitorList(info)) {
                            HwDualBandStateMachine.this.mMonitorAPList.add(info);
                        }
                    }
                }
            }
            if (this.m24GRssi == -1) {
                Log.e(HwDualBandMessageUtil.TAG, "isSatisfiedScanResult m24GBssid == -1");
                return false;
            }
            for (HwDualBandMonitorInfo info2 : HwDualBandStateMachine.this.mMonitorAPList) {
                boolean isMonitorAPFound = false;
                for (ScanResult result2 : mLists) {
                    if (info2.mBssid.equals(result2.BSSID) && result2.SSID != null && result2.SSID.length() > 0) {
                        Log.e(HwDualBandMessageUtil.TAG, "isSatisfiedScanResult result.mBssid = " + result2.BSSID + " result.SSID = " + result2.SSID + " result.level = " + result2.level);
                        isMonitorAPFound = true;
                        scanResultsFound++;
                        if (info2.mIsDualbandAP == 1) {
                            processSingleAPResult(info2, result2, mAPList);
                        } else {
                            processMixAPResult(info2, result2, mAPList);
                        }
                        if (result2.frequency != info2.mDualBandApInfoRcd.mChannelFrequency) {
                            Log.e(HwDualBandMessageUtil.TAG, "isSatisfiedScanResult update AP frequency mChannelFrequency = " + info2.mDualBandApInfoRcd.mChannelFrequency + " new frequency = " + result2.frequency);
                            info2.mDualBandApInfoRcd.mChannelFrequency = result2.frequency;
                            HwDualBandInformationManager.getInstance().updateAPInfo(info2.mDualBandApInfoRcd);
                        }
                    }
                }
                if (this.m24GRssi > info2.mScanRssi && info2.mIsDualbandAP == 1) {
                    if (isMonitorAPFound) {
                        WifiProStatisticsManager.getInstance().increaseDualbandStatisticCount(25);
                    } else {
                        HwDualBandStateMachine.this.addDisappearAPList(info2);
                        HwDualBandStateMachine.this.mHwDualBandAdaptiveThreshold.updateRSSIThreshold(this.m24GBssid, info2.mBssid, this.m24GRssi, -127, info2.mScanRssi, info2.mTargetRssi);
                        info2.mScanRssi = HwDualBandStateMachine.this.mHwDualBandAdaptiveThreshold.getScanRSSIThreshold(this.m24GBssid, info2.mBssid, info2.mTargetRssi);
                        Log.e(HwDualBandMessageUtil.TAG, "isSatisfiedScanResult renew info.mSsid = " + info2.mSsid + " info.mScanRssi = " + info2.mScanRssi + " info.mTargetRssi = " + info2.mTargetRssi);
                        WifiProStatisticsManager.getInstance().increaseDualbandStatisticCount(26);
                    }
                }
            }
            Log.e(HwDualBandMessageUtil.TAG, "isSatisfiedScanResult mMonitorAPList.size = " + HwDualBandStateMachine.this.mMonitorAPList.size() + ", scanResultsFound = " + scanResultsFound + ", mScanIndex = " + this.mScanIndex + ", mAPList.size = " + mAPList.size() + ", mDisappearAPList.size() = " + HwDualBandStateMachine.this.mDisappearAPList.size());
            if (this.mScanIndex >= HwDualBandStateMachine.WIFI_SCAN_INTERVAL && HwDualBandStateMachine.this.mDisappearAPList.size() > 0) {
                HwDualBandStateMachine.this.mCHRSingleAPScanCount = HwDualBandStateMachine.WIFI_SCANNING_CHANNEL_INDEX_MIN;
                HwDualBandStateMachine.this.mCHRMixAPScanCount = HwDualBandStateMachine.WIFI_SCANNING_CHANNEL_INDEX_MIN;
                HwDualBandStateMachine.this.mCHRScanAPType = HwDualBandStateMachine.WIFI_SCANNING_CHANNEL_INDEX_MIN;
                updateAPInfo(HwDualBandStateMachine.this.mDisappearAPList);
                if (HwDualBandStateMachine.this.mMonitorAPList.size() == 0) {
                    HwDualBandStateMachine.this.mIDualbandManagerCallback.onDualBandNetWorkType(HwDualBandStateMachine.WIFI_SCANNING_CHANNEL_INDEX_MIN, HwDualBandStateMachine.this.mMonitorAPList);
                    HwDualBandStateMachine.this.sendMessage(HwDualBandMessageUtil.CMD_STOP_MONITOR);
                    return false;
                }
            }
            if (scanResultsFound == HwDualBandStateMachine.this.mMonitorAPList.size()) {
                this.mScanIndex = HwDualBandStateMachine.WIFI_SCANNING_CHANNEL_INDEX_MIN;
            }
            Log.e(HwDualBandMessageUtil.TAG, "isSatisfiedScanResult mAPList.size() = " + mAPList.size() + ", scanResultsFound = " + scanResultsFound + ", mScanIndex = " + this.mScanIndex);
            if (mAPList.size() <= 0) {
                return false;
            }
            HwDualBandStateMachine.this.mIDualbandManagerCallback.onDualBandNetWorkFind(mAPList);
            HwDualBandStateMachine.this.reportDualbandChrSatisfiedCount();
            HwDualBandStateMachine.this.sendMessage(HwDualBandMessageUtil.CMD_STOP_MONITOR);
            return true;
        }

        private void processSingleAPResult(HwDualBandMonitorInfo info, ScanResult result, List<HwDualBandMonitorInfo> mAPList) {
            if (this.m24GRssi >= info.mScanRssi) {
                HwDualBandStateMachine.this.mHwDualBandAdaptiveThreshold.updateRSSIThreshold(this.m24GBssid, result.BSSID, this.m24GRssi, result.level, info.mScanRssi, info.mTargetRssi);
                info.mScanRssi = HwDualBandStateMachine.this.mHwDualBandAdaptiveThreshold.getScanRSSIThreshold(this.m24GBssid, result.BSSID, info.mTargetRssi);
                Log.e(HwDualBandMessageUtil.TAG, "processSingleAPResult renew info.mSsid = " + info.mSsid + " info.mScanRssi = " + info.mScanRssi + " info.mTargetRssi = " + info.mTargetRssi);
            }
            if (result.level >= info.mTargetRssi) {
                info.mCurrentRssi = result.level;
                mAPList.add(info);
                Log.e(HwDualBandMessageUtil.TAG, "processSingleAPResult info.mSsid = " + info.mSsid + " info.mCurrentRssi = " + info.mCurrentRssi + " info.mTargetRssi = " + info.mTargetRssi);
            }
            if (info.mDualBandApInfoRcd.mDisappearCount > 0) {
                Log.e(HwDualBandMessageUtil.TAG, "isSatisfiedScanResult update AP disappear number = " + info.mDualBandApInfoRcd.mDisappearCount + " --> 0");
                info.mDualBandApInfoRcd.mDisappearCount = HwDualBandStateMachine.WIFI_SCANNING_CHANNEL_INDEX_MIN;
                HwDualBandInformationManager.getInstance().updateAPInfo(info.mDualBandApInfoRcd);
            }
        }

        private void processMixAPResult(HwDualBandMonitorInfo info, ScanResult result, List<HwDualBandMonitorInfo> mAPList) {
            if (result.level >= info.mTargetRssi) {
                info.mCurrentRssi = result.level;
                mAPList.add(info);
                Log.e(HwDualBandMessageUtil.TAG, "processMixAPResult info.mSsid = " + info.mSsid + " info.mCurrentRssi = " + info.mCurrentRssi + " info.mTargetRssi = " + info.mTargetRssi);
                return;
            }
            info.mScanRssi = updateScanBssid(info, result.level);
            Log.e(HwDualBandMessageUtil.TAG, "processMixAPResult renew info.mSsid = " + info.mSsid + " info.mScanRssi = " + info.mScanRssi + " info.mTargetRssi = " + info.mTargetRssi);
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private ScanSettings getCustomizedScanSettings() {
            WifiInfo mWifiInfo = HwDualBandStateMachine.this.mWifiManager.getConnectionInfo();
            if (mWifiInfo == null || mWifiInfo.getBSSID() == null || HwDualBandStateMachine.this.mHwDualBandInformationManager.getDualBandAPInfo(mWifiInfo.getBSSID()) == null) {
                return null;
            }
            ScanSettings settings = new ScanSettings();
            settings.band = HwDualBandStateMachine.WIFI_SCANNING_CHANNEL_INDEX_MIN;
            settings.reportEvents = HwDualBandStateMachine.WIFI_SCANNING_CHANNEL_INDEX;
            settings.numBssidsPerScan = HwDualBandStateMachine.WIFI_SCANNING_CHANNEL_INDEX_MIN;
            settings.channels = new ChannelSpec[HwDualBandStateMachine.this.mMonitorAPList.size()];
            int index = HwDualBandStateMachine.WIFI_SCANNING_CHANNEL_INDEX_MIN;
            for (HwDualBandMonitorInfo record : HwDualBandStateMachine.this.mMonitorAPList) {
                WifiProDualBandApInfoRcd scanAPInfo = HwDualBandStateMachine.this.mHwDualBandInformationManager.getDualBandAPInfo(record.mBssid);
                if (scanAPInfo != null) {
                    int index2 = index + 1;
                    settings.channels[index] = new ChannelSpec(scanAPInfo.mChannelFrequency);
                    Log.d(HwDualBandMessageUtil.TAG, "getCustomizedScanSettings:  Frequency = " + scanAPInfo.mChannelFrequency);
                    index = index2;
                }
            }
            if (index != HwDualBandStateMachine.this.mMonitorAPList.size()) {
                return null;
            }
            return settings;
        }

        private int updateScanBssid(HwDualBandMonitorInfo info, int rssi) {
            int targetScanRssi;
            if (info.mIsNearAP == 1) {
                targetScanRssi = info.mScanRssi + HwDualBandStateMachine.WIFI_SCAN_INTERVAL;
            } else {
                targetScanRssi = info.mScanRssi - 5;
            }
            if (targetScanRssi >= HwDualBandStateMachine.WIFI_MAX_SCAN_THRESHOLD || targetScanRssi <= HwDualBandStateMachine.WIFI_MIN_SCAN_THRESHOLD) {
                targetScanRssi = info.mInitializationRssi;
            }
            Log.e(HwDualBandMessageUtil.TAG, "updateScanBssid targetScanRssi = " + targetScanRssi + " scanRssi = " + info.mScanRssi + " mInitializationRssi = " + info.mInitializationRssi + " rssi = " + rssi);
            return targetScanRssi;
        }

        private void removeFromMonitorList(String ssid) {
            List<HwDualBandMonitorInfo> delectList = new ArrayList();
            for (HwDualBandMonitorInfo info : HwDualBandStateMachine.this.mMonitorAPList) {
                if (info.mSsid.equals(ssid)) {
                    delectList.add(info);
                }
            }
            for (HwDualBandMonitorInfo info2 : delectList) {
                HwDualBandStateMachine.this.mMonitorAPList.remove(info2);
            }
        }

        private void updateAPInfo(List<HwDualBandMonitorInfo> disappearAPList) {
            for (HwDualBandMonitorInfo info : disappearAPList) {
                if (info.mDualBandApInfoRcd != null) {
                    WifiProDualBandApInfoRcd wifiProDualBandApInfoRcd = info.mDualBandApInfoRcd;
                    wifiProDualBandApInfoRcd.mDisappearCount++;
                    Log.e(HwDualBandMessageUtil.TAG, "updateAPInfo info.mSsid = " + info.mSsid + ", info.mBssid = " + info.mBssid + ", info.mDualBandApInfoRcd.mDisappearCount = " + info.mDualBandApInfoRcd.mDisappearCount);
                    if (info.mDualBandApInfoRcd.mDisappearCount > HwDualBandStateMachine.WIFI_SCANNING_CHANNEL_INDEX) {
                        HwDualBandInformationManager.getInstance().delectDualBandAPInfoBySsid(info.mSsid, info.mAuthType);
                        if (info.mIsDualbandAP == 1) {
                            WifiProStatisticsManager.getInstance().increaseDualbandStatisticCount(HwDualBandStateMachine.WIFI_SCAN_INTERVAL);
                        } else if (info.mIsDualbandAP == 2) {
                            WifiProStatisticsManager.getInstance().increaseDualbandStatisticCount(17);
                        }
                    } else {
                        HwDualBandInformationManager.getInstance().updateAPInfo(info.mDualBandApInfoRcd);
                    }
                }
                removeFromMonitorList(info.mSsid);
            }
        }

        private boolean isInMonitorList(HwDualBandMonitorInfo info) {
            if (HwDualBandStateMachine.this.mMonitorAPList.size() <= 0) {
                return false;
            }
            for (HwDualBandMonitorInfo monitorInfo : HwDualBandStateMachine.this.mMonitorAPList) {
                if (monitorInfo.mBssid.equals(info.mBssid)) {
                    return true;
                }
            }
            return false;
        }
    }

    class StopState extends State {
        StopState() {
        }

        public void enter() {
            Log.e(HwDualBandMessageUtil.TAG, "Enter StopState");
            if (!(HwDualBandStateMachine.this.mMonitorAPList == null || HwDualBandStateMachine.this.mMonitorAPList.size() == 0)) {
                HwDualBandStateMachine.this.mMonitorAPList.clear();
            }
            if (HwDualBandStateMachine.this.mTargetAPList != null && HwDualBandStateMachine.this.mTargetAPList.size() != 0) {
                HwDualBandStateMachine.this.mTargetAPList.clear();
            }
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case MessageUtil.CMD_ON_START /*100*/:
                    HwDualBandStateMachine.this.transitionTo(HwDualBandStateMachine.this.mDefaultState);
                    break;
            }
            return true;
        }
    }

    public HwDualBandStateMachine(Context context, IDualBandManagerCallback callBack) {
        super("HwDualBandStateMachine");
        this.mIDualbandManagerCallback = null;
        this.mHwDualBandWiFiMonitor = null;
        this.mHwDualBandInformationManager = null;
        this.mHwDualBandRelationManager = null;
        this.mCustomizedScanListener = new CustomizedScanListener();
        this.mMonitorAPList = new ArrayList();
        this.mDisappearAPList = new ArrayList();
        this.mDefaultState = new DefaultState();
        this.mDisabledState = new DisabledState();
        this.mConnectedState = new ConnectedState();
        this.mInternetReadyState = new InternetReadyState();
        this.mDisconnectedState = new DisconnectedState();
        this.mMonitorState = new MonitorState();
        this.mStopState = new StopState();
        this.mIsDualbandScanning = false;
        this.mCHRHandoverTooSlow = new WifiProDualbandExceptionRecord();
        this.mCHRIsManualHandover = false;
        this.mCHRSavedAPList = new ArrayList();
        this.mCHRSingleAPScanCount = WIFI_SCANNING_CHANNEL_INDEX_MIN;
        this.mCHRMixAPScanCount = WIFI_SCANNING_CHANNEL_INDEX_MIN;
        this.mCHRScanAPType = WIFI_SCANNING_CHANNEL_INDEX_MIN;
        this.mContext = context;
        this.mIDualbandManagerCallback = callBack;
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        this.mHwDualBandWiFiMonitor = new HwDualBandWiFiMonitor(context, getHandler());
        this.mHwDualBandInformationManager = HwDualBandInformationManager.createInstance(context);
        this.mHwDualBandRelationManager = HwDualBandRelationManager.createInstance(context, getHandler());
        this.mHwDualBandAdaptiveThreshold = new HwDualBandAdaptiveThreshold(context);
        addState(this.mDefaultState);
        addState(this.mDisabledState, this.mDefaultState);
        addState(this.mConnectedState, this.mDefaultState);
        addState(this.mMonitorState, this.mConnectedState);
        addState(this.mInternetReadyState, this.mConnectedState);
        addState(this.mDisconnectedState, this.mDefaultState);
        addState(this.mStopState, this.mDefaultState);
        setInitialState(this.mDefaultState);
        start();
    }

    public void onStart() {
        getHandler().sendEmptyMessage(100);
        this.mHwDualBandWiFiMonitor.startMonitor();
    }

    public void onStop() {
        this.mHwDualBandWiFiMonitor.stopMonitor();
        getHandler().sendEmptyMessage(MessageUtil.CMD_ON_STOP);
    }

    public Handler getStateMachineHandler() {
        return getHandler();
    }

    public boolean isDualbandScanning() {
        return this.mIsDualbandScanning;
    }

    private int dualbandChrGetAPType() {
        if (this.mMonitorAPList == null || this.mMonitorAPList.size() != 1) {
            return 2;
        }
        HwDualBandMonitorInfo info = (HwDualBandMonitorInfo) this.mMonitorAPList.get(WIFI_SCANNING_CHANNEL_INDEX_MIN);
        if (info == null || info.mIsDualbandAP != 1) {
            return 2;
        }
        return 1;
    }

    private void initDualbandChrHandoverTooSlow(String ssid, int rssi) {
        this.mCHRIsManualHandover = true;
        if (this.mCHRHandoverTooSlow != null) {
            this.mCHRHandoverTooSlow.mSSID_2G = ssid;
            this.mCHRHandoverTooSlow.mRSSI_2G = (short) rssi;
            Log.e(HwDualBandMessageUtil.TAG, "db_chr initDualbandChrHandoverTooSlow mCHR24GSsid" + ssid + ", m24GRssi = " + this.mCHRHandoverTooSlow.mRSSI_2G);
            for (HwDualBandMonitorInfo info : this.mMonitorAPList) {
                this.mCHRSavedAPList.add(info);
            }
        }
    }

    private void reportDualbandChrHandoverTooSlow() {
        if (this.mCHRIsManualHandover) {
            WifiInfo mWifiInfo = this.mWifiManager.getConnectionInfo();
            if (mWifiInfo != null && mWifiInfo.getBSSID() != null && mWifiInfo.getSSID() != null) {
                if (ScanResult.is5GHz(mWifiInfo.getFrequency())) {
                    if (this.mCHRSavedAPList.size() != 0) {
                        for (HwDualBandMonitorInfo info : this.mCHRSavedAPList) {
                            if (info.mBssid.equals(mWifiInfo.getBSSID()) && this.mCHRHandoverTooSlow != null) {
                                this.mCHRHandoverTooSlow.mSSID_5G = info.mSsid;
                                this.mCHRHandoverTooSlow.mSingleOrMixed = (short) info.mIsDualbandAP;
                                this.mCHRHandoverTooSlow.mScan_Threshod_RSSI_2G = (short) info.mScanRssi;
                                this.mCHRHandoverTooSlow.mTarget_RSSI_5G = (short) info.mTargetRssi;
                                this.mCHRHandoverTooSlow.mRSSI_5G = (short) mWifiInfo.getRssi();
                                Log.e(HwDualBandMessageUtil.TAG, "db_chr rec.mSSID_5G = " + this.mCHRHandoverTooSlow.mSSID_5G + " rec.mSingleOrMixed = " + this.mCHRHandoverTooSlow.mSingleOrMixed + " rec.mSingleOrMixed = " + this.mCHRHandoverTooSlow.mSingleOrMixed + " rec.mScan_Threshod_RSSI_2G = " + this.mCHRHandoverTooSlow.mScan_Threshod_RSSI_2G + " rec.mTarget_RSSI_5G = " + this.mCHRHandoverTooSlow.mTarget_RSSI_5G + " rec.mRSSI_5G = " + this.mCHRHandoverTooSlow.mRSSI_5G);
                                WifiProStatisticsManager.getInstance().uploadWifiProDualbandExceptionEvent(WifiProStatisticsManager.SUB_EVENT_DUALBAND_HANDOVER_TOO_SLOW, this.mCHRHandoverTooSlow);
                                this.mCHRHandoverTooSlow = null;
                                break;
                            }
                        }
                    }
                } else {
                    return;
                }
            }
            Log.w(HwDualBandMessageUtil.TAG, "MonitorState mWifiInfo == null");
            return;
        }
        this.mCHRIsManualHandover = false;
        this.mCHRSavedAPList.clear();
    }

    private void reportDualbandChrMonitorCount() {
        if (dualbandChrGetAPType() == 1) {
            WifiProStatisticsManager.getInstance().increaseDualbandStatisticCount(WIFI_SCANNING_CHANNEL_INDEX);
        } else {
            WifiProStatisticsManager.getInstance().increaseDualbandStatisticCount(15);
        }
    }

    private void reportDualbandChrSatisfiedCount() {
        if (dualbandChrGetAPType() == 1) {
            WifiProStatisticsManager.getInstance().increaseDualbandStatisticCount(4);
        } else {
            WifiProStatisticsManager.getInstance().increaseDualbandStatisticCount(16);
        }
    }

    private void reportDualbandChrFreqScan5GCount() {
        if (dualbandChrGetAPType() == 1) {
            if (this.mCHRSingleAPScanCount <= WIFI_SCANNING_CHANNEL_INDEX) {
                WifiProStatisticsManager.getInstance().increaseDualbandStatisticCount(WIFI_RSSI_GAP);
            } else if (this.mCHRSingleAPScanCount <= WIFI_SCAN_INTERVAL) {
                WifiProStatisticsManager.getInstance().increaseDualbandStatisticCount(11);
            } else {
                WifiProStatisticsManager.getInstance().increaseDualbandStatisticCount(12);
            }
        } else if (this.mCHRSingleAPScanCount <= WIFI_SCANNING_CHANNEL_INDEX) {
            WifiProStatisticsManager.getInstance().increaseDualbandStatisticCount(22);
        } else if (this.mCHRSingleAPScanCount <= WIFI_SCAN_INTERVAL) {
            WifiProStatisticsManager.getInstance().increaseDualbandStatisticCount(23);
        } else {
            WifiProStatisticsManager.getInstance().increaseDualbandStatisticCount(24);
        }
    }

    private void startCustomizedScan(ScanSettings requested) {
        this.mWifiScanner.startScan(requested, this.mCustomizedScanListener, null);
    }

    private void addDisappearAPList(HwDualBandMonitorInfo info) {
        boolean addFlag = true;
        for (HwDualBandMonitorInfo data : this.mDisappearAPList) {
            if (data.mBssid.equals(info.mBssid)) {
                addFlag = false;
                break;
            }
        }
        if (addFlag) {
            this.mDisappearAPList.add(info);
        }
    }
}
