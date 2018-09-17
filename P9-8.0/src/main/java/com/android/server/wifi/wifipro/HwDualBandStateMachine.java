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
import com.android.server.wifi.WifiInjector;
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
    private WifiProDualbandExceptionRecord mCHRHandoverTooSlow = new WifiProDualbandExceptionRecord();
    private int mCHRMixAPScanCount = 0;
    private List<HwDualBandMonitorInfo> mCHRSavedAPList = new ArrayList();
    private int mCHRScanAPType = 0;
    private int mCHRSingleAPScanCount = 0;
    private State mConnectedState = new ConnectedState();
    private Context mContext;
    private final CustomizedScanListener mCustomizedScanListener = new CustomizedScanListener(this, null);
    private State mDefaultState = new DefaultState();
    private State mDisabledState = new DisabledState();
    private List<HwDualBandMonitorInfo> mDisappearAPList = new ArrayList();
    private State mDisconnectedState = new DisconnectedState();
    private FrameworkFacade mFrameworkFacade;
    private HwDualBandAdaptiveThreshold mHwDualBandAdaptiveThreshold;
    private HwDualBandInformationManager mHwDualBandInformationManager = null;
    private HwDualBandRelationManager mHwDualBandRelationManager = null;
    private HwDualBandWiFiMonitor mHwDualBandWiFiMonitor = null;
    private IDualBandManagerCallback mIDualbandManagerCallback = null;
    private State mInternetReadyState = new InternetReadyState();
    private boolean mIsDualbandScanning = false;
    private List<HwDualBandMonitorInfo> mMonitorAPList = new ArrayList();
    private State mMonitorState = new MonitorState();
    private State mStopState = new StopState();
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
                case 1:
                    break;
                case 11:
                    Log.e(HwDualBandMessageUtil.TAG, "MSG_WIFI_INTERNET_CONNECTED");
                    WifiInfo mInternetWifiInfo = HwDualBandStateMachine.this.mWifiManager.getConnectionInfo();
                    if (mInternetWifiInfo != null && mInternetWifiInfo.getBSSID() != null) {
                        HwDualBandStateMachine.this.transitionTo(HwDualBandStateMachine.this.mInternetReadyState);
                        HwDualBandStateMachine.this.sendMessage(104);
                        break;
                    }
                    if (mInternetWifiInfo == null) {
                        Log.e(HwDualBandMessageUtil.TAG, "MSG_WIFI_INTERNET_CONNECTED mInternetWifiInfo == null");
                    } else {
                        Log.e(HwDualBandMessageUtil.TAG, "MSG_WIFI_INTERNET_CONNECTED mInternetWifiInfo.getBSSID() == null");
                    }
                    HwDualBandStateMachine.this.sendMessageDelayed(11, 2000);
                    return true;
                case 12:
                case 13:
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
        /* synthetic */ CustomizedScanListener(HwDualBandStateMachine this$0, CustomizedScanListener -this1) {
            this();
        }

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
                case 1:
                    Log.e(HwDualBandMessageUtil.TAG, "DefaultState MSG_WIFI_CONNECTED");
                    HwDualBandStateMachine.this.transitionTo(HwDualBandStateMachine.this.mConnectedState);
                    break;
                case 2:
                    HwDualBandStateMachine.this.transitionTo(HwDualBandStateMachine.this.mDisconnectedState);
                    break;
                case 4:
                    HwDualBandStateMachine.this.transitionTo(HwDualBandStateMachine.this.mDisabledState);
                    break;
                case 8:
                    Log.e(HwDualBandMessageUtil.TAG, "DefaultState MSG_WIFI_CONFIG_CHANGED");
                    Bundle data = message.getData();
                    String bssid = data.getString("bssid");
                    String ssid = data.getString("ssid");
                    int authtype = data.getInt(HwDualBandMessageUtil.MSG_KEY_AUTHTYPE);
                    Log.e(HwDualBandMessageUtil.TAG, "MSG_WIFI_CONFIG_CHANGED ssid = " + ssid);
                    if (ssid != null) {
                        HwDualBandStateMachine.this.mHwDualBandInformationManager.delectDualBandAPInfoBySsid(ssid, authtype);
                        break;
                    }
                    break;
                case 101:
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
                case 4:
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
                case 2:
                    return true;
                default:
                    return false;
            }
        }
    }

    class InternetReadyState extends State {
        private String mCurrentBSSID = null;
        private String mCurrentSSID = null;

        InternetReadyState() {
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
            Log.e(HwDualBandMessageUtil.TAG, "Enter InternetReadyState mCurrentSSID = " + this.mCurrentSSID);
        }

        public boolean processMessage(Message message) {
            WifiProDualBandApInfoRcd m5GAPInfo;
            HwDualBandMonitorInfo mHwDualBandMonitorInfo;
            switch (message.what) {
                case 1:
                    WifiInfo mConnectedWifiInfo = HwDualBandStateMachine.this.mWifiManager.getConnectionInfo();
                    if (!(mConnectedWifiInfo == null || mConnectedWifiInfo.getBSSID() == null || (mConnectedWifiInfo.getBSSID().equals(this.mCurrentBSSID) ^ 1) == 0)) {
                        Log.e(HwDualBandMessageUtil.TAG, "InternetReadyState MSG_WIFI_CONNECTED");
                        HwDualBandStateMachine.this.transitionTo(HwDualBandStateMachine.this.mConnectedState);
                        break;
                    }
                case 11:
                case 13:
                    break;
                case 12:
                    Log.e(HwDualBandMessageUtil.TAG, "InternetReadyState MSG_WIFI_INTERNET_DISCONNECTED");
                    HwDualBandStateMachine.this.transitionTo(HwDualBandStateMachine.this.mConnectedState);
                    HwDualBandStateMachine.this.sendMessage(12);
                    break;
                case 16:
                    Log.e(HwDualBandMessageUtil.TAG, "MSG_DUAL_BAND_WIFI_TYPE_SINGLE");
                    WifiInfo mWifiInfo = HwDualBandStateMachine.this.mWifiManager.getConnectionInfo();
                    if (!(mWifiInfo == null || mWifiInfo.getBSSID() == null)) {
                        WifiProDualBandApInfoRcd apinfo = HwDualBandStateMachine.this.mHwDualBandInformationManager.getDualBandAPInfo(mWifiInfo.getBSSID());
                        if (apinfo != null) {
                            List<WifiProRelateApRcd> mLists = apinfo.getRelateApRcds();
                            if (mLists.size() > 0) {
                                Log.e(HwDualBandMessageUtil.TAG, "MSG_DUAL_BAND_WIFI_TYPE_SINGLE mLists.size() = " + mLists.size());
                                WifiProRelateApRcd info = (WifiProRelateApRcd) mLists.get(0);
                                m5GAPInfo = HwDualBandStateMachine.this.mHwDualBandInformationManager.getDualBandAPInfo(info.mRelatedBSSID);
                                if (m5GAPInfo != null) {
                                    if (m5GAPInfo.isInBlackList != 1) {
                                        mHwDualBandMonitorInfo = new HwDualBandMonitorInfo(info.mRelatedBSSID, m5GAPInfo.mApSSID, m5GAPInfo.mApAuthType.shortValue(), 0, 0, info.mRelateType);
                                        mHwDualBandMonitorInfo.mIsNearAP = 1;
                                        List<HwDualBandMonitorInfo> apList = new ArrayList();
                                        Log.e(HwDualBandMessageUtil.TAG, "MSG_DUAL_BAND_WIFI_TYPE_SINGLE find ssid = " + mHwDualBandMonitorInfo.mSsid + " m5GAPInfo.mApAuthType = " + m5GAPInfo.mApAuthType);
                                        apList.add(mHwDualBandMonitorInfo);
                                        HwDualBandStateMachine.this.mIDualbandManagerCallback.onDualBandNetWorkType(1, apList);
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
                case 17:
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
                                    mHwDualBandMonitorInfo = new HwDualBandMonitorInfo(m5GAPInfo.apBSSID, m5GAPInfo.mApSSID, m5GAPInfo.mApAuthType.shortValue(), 0, 0, record.mRelateType);
                                    if (mHwDualBandMonitorInfo.mIsDualbandAP == 1 || isNearAP(record)) {
                                        mHwDualBandMonitorInfo.mIsNearAP = 1;
                                    } else {
                                        mHwDualBandMonitorInfo.mIsNearAP = 0;
                                    }
                                    Log.e(HwDualBandMessageUtil.TAG, "MSG_DUAL_BAND_WIFI_TYPE_MIX find ssid = " + mHwDualBandMonitorInfo.mSsid + " m5GAPInfo.mApAuthType = " + m5GAPInfo.mApAuthType + " mIsNearAP = " + mHwDualBandMonitorInfo.mIsNearAP);
                                    mMixAPList.add(mHwDualBandMonitorInfo);
                                } else {
                                    Log.e(HwDualBandMessageUtil.TAG, "ssid = " + m5GAPInfo.mApSSID + " have no internet");
                                }
                            }
                            Log.e(HwDualBandMessageUtil.TAG, "MSG_DUAL_BAND_WIFI_TYPE_MIX after filter mMixAPList.size() = " + mMixAPList.size());
                            if (mMixAPList.size() > 0) {
                                HwDualBandStateMachine.this.mIDualbandManagerCallback.onDualBandNetWorkType(2, mMixAPList);
                                break;
                            }
                        }
                        Log.e(HwDualBandMessageUtil.TAG, "MSG_DUAL_BAND_WIFI_TYPE_MIX mMixAPinfo == null");
                        break;
                    }
                    break;
                case 19:
                    Log.e(HwDualBandMessageUtil.TAG, "InternetReadyState MSG_WIFI_VERIFYING_POOR_LINK");
                    HwDualBandStateMachine.this.transitionTo(HwDualBandStateMachine.this.mConnectedState);
                    break;
                case 102:
                    HwDualBandStateMachine.this.mTargetAPList = message.getData().getParcelableArrayList(HwDualBandMessageUtil.MSG_KEY_APLIST);
                    if (HwDualBandStateMachine.this.mTargetAPList != null) {
                        Log.e(HwDualBandMessageUtil.TAG, "CMD_START_MONITOR size = " + HwDualBandStateMachine.this.mTargetAPList.size());
                    }
                    HwDualBandStateMachine.this.transitionTo(HwDualBandStateMachine.this.mMonitorState);
                    break;
                case 104:
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
                return record.mMaxCurrentRSSI - record.mMinRelatedRSSI <= 10 && record.mMaxRelatedRSSI - record.mMinCurrentRSSI <= 10;
            } else {
                if (record.mMaxCurrentRSSI - record.mMinRelatedRSSI <= 10) {
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
        private String m24GBssid = null;
        private int m24GRssi = -1;
        private int mScanIndex = 0;

        MonitorState() {
        }

        public void enter() {
            Log.e(HwDualBandMessageUtil.TAG, "Enter MonitorState");
            if (HwDualBandStateMachine.this.mWifiScanner == null) {
                HwDualBandStateMachine.this.mWifiScanner = WifiInjector.getInstance().getWifiScanner();
            }
            HwDualBandStateMachine.this.mIsDualbandScanning = true;
            this.mScanIndex = 0;
            HwDualBandStateMachine.this.mCHRSingleAPScanCount = 0;
            HwDualBandStateMachine.this.mCHRMixAPScanCount = 0;
            HwDualBandStateMachine.this.mCHRScanAPType = 0;
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
                    Log.e(HwDualBandMessageUtil.TAG, "MonitorState isdulabanAP 2.4G = " + mWifiInfo.getSSID() + " 5G = " + info.mSsid + " info.mScanRssi = " + info.mScanRssi + " info.mTargetRssi = " + info.mTargetRssi);
                    HwDualBandStateMachine.this.mMonitorAPList.add(info);
                } else {
                    WifiProDualBandApInfoRcd APInfo = null;
                    WifiProRelateApRcd RelationInfo = null;
                    if (mWifiInfo.getBSSID() != null) {
                        APInfo = HwDualBandStateMachine.this.mHwDualBandInformationManager.getDualBandAPInfo(mWifiInfo.getBSSID());
                        RelationInfo = HwDualBandStateMachine.this.mHwDualBandRelationManager.getRelateAPInfo(mWifiInfo.getBSSID(), info.mBssid);
                    }
                    if (!(RelationInfo == null || APInfo == null)) {
                        if (info.mIsNearAP != 1 || RelationInfo.mMinCurrentRSSI == 0) {
                            info.mScanRssi = RelationInfo.mMaxCurrentRSSI;
                        } else {
                            info.mScanRssi = info.mTargetRssi - 5;
                        }
                        info.mInitializationRssi = info.mScanRssi;
                        Log.e(HwDualBandMessageUtil.TAG, "MonitorState mix AP 2.4G = " + mWifiInfo.getSSID() + " 5G = " + info.mSsid + " info.mScanRssi = " + info.mScanRssi + " info.mTargetRssi = " + info.mTargetRssi + " info.mAuthType = " + info.mAuthType + " info.mIsNearAP = " + info.mIsNearAP + " APInfo.mChannelFrequency = " + APInfo.mChannelFrequency + " APInfo.mApAuthType = " + APInfo.mApAuthType);
                        HwDualBandStateMachine.this.mMonitorAPList.add(info);
                    }
                }
            }
            if (HwDualBandStateMachine.this.mMonitorAPList.size() <= 0) {
                HwDualBandStateMachine.this.mIDualbandManagerCallback.onDualBandNetWorkType(0, HwDualBandStateMachine.this.mMonitorAPList);
                HwDualBandStateMachine.this.sendMessage(103);
            }
        }

        public void exit() {
            HwDualBandStateMachine.this.mIsDualbandScanning = false;
            this.mScanIndex = 0;
            if (!(HwDualBandStateMachine.this.mMonitorAPList == null || HwDualBandStateMachine.this.mMonitorAPList.size() == 0)) {
                HwDualBandStateMachine.this.mMonitorAPList.clear();
            }
            HwDualBandStateMachine.this.mCHRSingleAPScanCount = 0;
            HwDualBandStateMachine.this.mCHRMixAPScanCount = 0;
            HwDualBandStateMachine.this.mCHRScanAPType = 0;
        }

        public boolean processMessage(Message message) {
            int i = 5;
            switch (message.what) {
                case 2:
                    HwDualBandStateMachine.this.initDualbandChrHandoverTooSlow(this.m24GBssid, this.m24GRssi);
                    HwDualBandStateMachine.this.transitionTo(HwDualBandStateMachine.this.mDisconnectedState);
                    break;
                case 7:
                    Log.e(HwDualBandMessageUtil.TAG, "MonitorState MSG_WIFI_UPDATE_SCAN_RESULT");
                    List<ScanResult> mLists = HwDualBandStateMachine.this.mWifiManager.getScanResults();
                    if (mLists.size() > 0 && isSatisfiedScanResult(mLists)) {
                        Log.e(HwDualBandMessageUtil.TAG, "MonitorState MSG_WIFI_UPDATE_SCAN_RESULT find AP");
                        HwDualBandStateMachine.this.transitionTo(HwDualBandStateMachine.this.mInternetReadyState);
                        break;
                    }
                case 8:
                    Bundle remove_data = message.getData();
                    String bssid = remove_data.getString("bssid");
                    String ssid = remove_data.getString("ssid");
                    int authtype = remove_data.getInt(HwDualBandMessageUtil.MSG_KEY_AUTHTYPE);
                    Log.e(HwDualBandMessageUtil.TAG, "MonitorState MSG_WIFI_REMOVE_CONFIG_CHANGED ssid = " + ssid);
                    if (ssid != null) {
                        HwDualBandStateMachine.this.mHwDualBandInformationManager.delectDualBandAPInfoBySsid(ssid, authtype);
                        removeFromMonitorList(ssid);
                        Log.e(HwDualBandMessageUtil.TAG, "MonitorState MSG_WIFI_REMOVE_CONFIG_CHANGED mMonitorAPList.size() = " + HwDualBandStateMachine.this.mMonitorAPList.size());
                        HwDualBandStateMachine.this.mIDualbandManagerCallback.onDualBandNetWorkType(0, HwDualBandStateMachine.this.mMonitorAPList);
                        HwDualBandStateMachine.this.transitionTo(HwDualBandStateMachine.this.mInternetReadyState);
                        break;
                    }
                    break;
                case 18:
                    this.m24GRssi = message.getData().getInt(HwDualBandMessageUtil.MSG_KEY_RSSI);
                    Log.e(HwDualBandMessageUtil.TAG, "MonitorState m24GRssi = " + this.m24GRssi);
                    if (isSatisfiedScanCondition(this.m24GRssi)) {
                        if (this.mScanIndex < 3) {
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
                        if (this.mScanIndex <= 5) {
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
                case 19:
                    Log.e(HwDualBandMessageUtil.TAG, "MonitorState MSG_WIFI_VERIFYING_POOR_LINK");
                    HwDualBandStateMachine.this.transitionTo(HwDualBandStateMachine.this.mConnectedState);
                    break;
                case 103:
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
                } else if (rssi <= -90 || rssi > info.mInitializationRssi) {
                    info.mScanRssi = info.mInitializationRssi;
                    return false;
                } else if (rssi <= info.mScanRssi) {
                    return true;
                }
            }
            return false;
        }

        private boolean isSatisfiedScanResult(List<ScanResult> mLists) {
            int scanResultsFound = 0;
            List<HwDualBandMonitorInfo> mAPList = new ArrayList();
            for (ScanResult result : mLists) {
                if (this.m24GBssid != null && this.m24GBssid.equals(result.BSSID)) {
                    this.m24GRssi = result.level;
                    Log.e(HwDualBandMessageUtil.TAG, "isSatisfiedScanResult m24GRssi = " + this.m24GRssi);
                }
                if (HwDualBandStateMachine.this.mDisappearAPList.size() > 0) {
                    for (HwDualBandMonitorInfo info : HwDualBandStateMachine.this.mDisappearAPList) {
                        if (info.mBssid.equals(result.BSSID) && result.SSID != null && result.SSID.length() > 0 && (isInMonitorList(info) ^ 1) != 0) {
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
                        Log.e(HwDualBandMessageUtil.TAG, "isSatisfiedScanResult result.SSID = " + result2.SSID + " result.level = " + result2.level);
                        isMonitorAPFound = true;
                        scanResultsFound++;
                        String scanSSID = "\"" + result2.SSID + "\"";
                        if (!(result2.frequency == info2.mDualBandApInfoRcd.mChannelFrequency && (info2.mSsid.equals(scanSSID) ^ 1) == 0)) {
                            Log.e(HwDualBandMessageUtil.TAG, "isSatisfiedScanResult update AP frequency mChannelFrequency = " + info2.mDualBandApInfoRcd.mChannelFrequency + " new frequency = " + result2.frequency);
                            info2.mDualBandApInfoRcd.mChannelFrequency = result2.frequency;
                            info2.mDualBandApInfoRcd.mApSSID = scanSSID;
                            info2.mSsid = scanSSID;
                            HwDualBandInformationManager.getInstance().updateAPInfo(info2.mDualBandApInfoRcd);
                        }
                        if (info2.mIsDualbandAP == 1) {
                            processSingleAPResult(info2, result2, mAPList);
                        } else {
                            processMixAPResult(info2, result2, mAPList);
                        }
                    }
                }
                if (!isMonitorAPFound && info2.mIsDualbandAP == 2) {
                    info2.mScanRssi = updateScanBssid(info2, WifiHandover.INVALID_RSSI);
                }
                if (this.m24GRssi > info2.mScanRssi && info2.mIsDualbandAP == 1 && !isMonitorAPFound) {
                    HwDualBandStateMachine.this.addDisappearAPList(info2);
                    HwDualBandStateMachine.this.mHwDualBandAdaptiveThreshold.updateRSSIThreshold(this.m24GBssid, info2.mBssid, this.m24GRssi, -127, info2.mScanRssi, info2.mTargetRssi);
                    info2.mScanRssi = HwDualBandStateMachine.this.mHwDualBandAdaptiveThreshold.getScanRSSIThreshold(this.m24GBssid, info2.mBssid, info2.mTargetRssi);
                    Log.e(HwDualBandMessageUtil.TAG, "isSatisfiedScanResult renew info.mSsid = " + info2.mSsid + " info.mScanRssi = " + info2.mScanRssi + " info.mTargetRssi = " + info2.mTargetRssi);
                }
            }
            Log.e(HwDualBandMessageUtil.TAG, "isSatisfiedScanResult mMonitorAPList.size = " + HwDualBandStateMachine.this.mMonitorAPList.size() + ", scanResultsFound = " + scanResultsFound + ", mScanIndex = " + this.mScanIndex + ", mAPList.size = " + mAPList.size() + ", mDisappearAPList.size() = " + HwDualBandStateMachine.this.mDisappearAPList.size());
            if (this.mScanIndex >= 5 && HwDualBandStateMachine.this.mDisappearAPList.size() > 0) {
                HwDualBandStateMachine.this.mCHRSingleAPScanCount = 0;
                HwDualBandStateMachine.this.mCHRMixAPScanCount = 0;
                HwDualBandStateMachine.this.mCHRScanAPType = 0;
                updateAPInfo(HwDualBandStateMachine.this.mDisappearAPList);
                if (HwDualBandStateMachine.this.mMonitorAPList.size() == 0) {
                    HwDualBandStateMachine.this.mIDualbandManagerCallback.onDualBandNetWorkType(0, HwDualBandStateMachine.this.mMonitorAPList);
                    HwDualBandStateMachine.this.sendMessage(103);
                    return false;
                }
            }
            if (scanResultsFound == HwDualBandStateMachine.this.mMonitorAPList.size()) {
                this.mScanIndex = 0;
            }
            Log.e(HwDualBandMessageUtil.TAG, "isSatisfiedScanResult mAPList.size() = " + mAPList.size() + ", scanResultsFound = " + scanResultsFound + ", mScanIndex = " + this.mScanIndex);
            if (mAPList.size() <= 0) {
                return false;
            }
            HwDualBandStateMachine.this.mIDualbandManagerCallback.onDualBandNetWorkFind(mAPList);
            HwDualBandStateMachine.this.sendMessage(103);
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
                info.mDualBandApInfoRcd.mDisappearCount = 0;
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

        /* JADX WARNING: Missing block: B:17:0x009d, code:
            return null;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private ScanSettings getCustomizedScanSettings() {
            WifiInfo mWifiInfo = HwDualBandStateMachine.this.mWifiManager.getConnectionInfo();
            if (mWifiInfo == null || mWifiInfo.getBSSID() == null || HwDualBandStateMachine.this.mHwDualBandInformationManager.getDualBandAPInfo(mWifiInfo.getBSSID()) == null) {
                return null;
            }
            ScanSettings settings = new ScanSettings();
            settings.band = 0;
            settings.reportEvents = 3;
            settings.numBssidsPerScan = 0;
            settings.channels = new ChannelSpec[HwDualBandStateMachine.this.mMonitorAPList.size()];
            int index = 0;
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
                targetScanRssi = info.mScanRssi + 5;
            } else {
                targetScanRssi = info.mScanRssi - 5;
            }
            if (targetScanRssi >= HwDualBandStateMachine.WIFI_MAX_SCAN_THRESHOLD || targetScanRssi <= -90) {
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
                    Log.e(HwDualBandMessageUtil.TAG, "updateAPInfo info.mSsid = " + info.mSsid + ", info.mDualBandApInfoRcd.mDisappearCount = " + info.mDualBandApInfoRcd.mDisappearCount);
                    if (info.mDualBandApInfoRcd.mDisappearCount > 3) {
                        HwDualBandInformationManager.getInstance().delectDualBandAPInfoBySsid(info.mSsid, info.mAuthType);
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
                case 100:
                    HwDualBandStateMachine.this.transitionTo(HwDualBandStateMachine.this.mDefaultState);
                    break;
            }
            return true;
        }
    }

    public HwDualBandStateMachine(Context context, IDualBandManagerCallback callBack) {
        super("HwDualBandStateMachine");
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
        getHandler().sendEmptyMessage(101);
    }

    public Handler getStateMachineHandler() {
        return getHandler();
    }

    public boolean isDualbandScanning() {
        return this.mIsDualbandScanning;
    }

    private void initDualbandChrHandoverTooSlow(String ssid, int rssi) {
        if (this.mCHRHandoverTooSlow != null) {
            this.mCHRHandoverTooSlow.mSSID_2G = ssid;
            this.mCHRHandoverTooSlow.mRSSI_2G = (short) rssi;
            Log.e(HwDualBandMessageUtil.TAG, "db_chr initDualbandChrHandoverTooSlow mCHR24GSsid" + ssid + ", m24GRssi = " + this.mCHRHandoverTooSlow.mRSSI_2G);
            for (HwDualBandMonitorInfo info : this.mMonitorAPList) {
                this.mCHRSavedAPList.add(info);
            }
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
