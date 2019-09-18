package com.android.server.wifi.wifipro;

import android.common.HwFrameworkFactory;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiScanner;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.LocalServices;
import com.android.server.policy.AbsPhoneWindowManager;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.wifi.FrameworkFacade;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifipro.WifiProCommonUtils;
import java.util.ArrayList;
import java.util.Iterator;
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
    /* access modifiers changed from: private */
    public int mCHRMixAPScanCount = 0;
    private List<HwDualBandMonitorInfo> mCHRSavedAPList = new ArrayList();
    /* access modifiers changed from: private */
    public int mCHRScanAPType = 0;
    /* access modifiers changed from: private */
    public int mCHRSingleAPScanCount = 0;
    /* access modifiers changed from: private */
    public State mConnectedState = new ConnectedState();
    /* access modifiers changed from: private */
    public Context mContext;
    private final CustomizedScanListener mCustomizedScanListener = new CustomizedScanListener();
    /* access modifiers changed from: private */
    public State mDefaultState = new DefaultState();
    /* access modifiers changed from: private */
    public State mDisabledState = new DisabledState();
    /* access modifiers changed from: private */
    public List<HwDualBandMonitorInfo> mDisappearAPList = new ArrayList();
    /* access modifiers changed from: private */
    public State mDisconnectedState = new DisconnectedState();
    private FrameworkFacade mFrameworkFacade;
    /* access modifiers changed from: private */
    public HwDualBandAdaptiveThreshold mHwDualBandAdaptiveThreshold;
    /* access modifiers changed from: private */
    public HwDualBandInformationManager mHwDualBandInformationManager = null;
    /* access modifiers changed from: private */
    public HwDualBandRelationManager mHwDualBandRelationManager = null;
    private HwDualBandWiFiMonitor mHwDualBandWiFiMonitor = null;
    /* access modifiers changed from: private */
    public IDualBandManagerCallback mIDualbandManagerCallback = null;
    /* access modifiers changed from: private */
    public State mInternetReadyState = new InternetReadyState();
    /* access modifiers changed from: private */
    public boolean mIsDualbandScanning = false;
    /* access modifiers changed from: private */
    public List<HwDualBandMonitorInfo> mMonitorAPList = new ArrayList();
    /* access modifiers changed from: private */
    public State mMonitorState = new MonitorState();
    /* access modifiers changed from: private */
    public PowerManager mPowerManager;
    /* access modifiers changed from: private */
    public State mStopState = new StopState();
    /* access modifiers changed from: private */
    public List<HwDualBandMonitorInfo> mTargetAPList;
    /* access modifiers changed from: private */
    public WifiManager mWifiManager;
    /* access modifiers changed from: private */
    public WifiScanner mWifiScanner;

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
            int i = message.what;
            if (i != 1) {
                switch (i) {
                    case 11:
                        Log.e(HwDualBandMessageUtil.TAG, "MSG_WIFI_INTERNET_CONNECTED");
                        WifiInfo mInternetWifiInfo = HwDualBandStateMachine.this.mWifiManager.getConnectionInfo();
                        if (mInternetWifiInfo != null && mInternetWifiInfo.getBSSID() != null) {
                            HwDualBandStateMachine.this.transitionTo(HwDualBandStateMachine.this.mInternetReadyState);
                            HwDualBandStateMachine.this.sendMessage(104);
                            break;
                        } else {
                            if (mInternetWifiInfo == null) {
                                Log.e(HwDualBandMessageUtil.TAG, "MSG_WIFI_INTERNET_CONNECTED mInternetWifiInfo == null");
                            } else {
                                Log.e(HwDualBandMessageUtil.TAG, "MSG_WIFI_INTERNET_CONNECTED mInternetWifiInfo.getBSSID() == null");
                            }
                            HwDualBandStateMachine.this.sendMessageDelayed(11, 2000);
                            return true;
                        }
                        break;
                    case 12:
                    case 13:
                        Log.e(HwDualBandMessageUtil.TAG, "MSG_WIFI_INTERNET_DISCONNECTED");
                        WifiInfo mNoInternetWifiInfo = HwDualBandStateMachine.this.mWifiManager.getConnectionInfo();
                        if (!(mNoInternetWifiInfo == null || mNoInternetWifiInfo.getBSSID() == null)) {
                            WifiProDualBandApInfoRcd info = HwDualBandStateMachine.this.mHwDualBandInformationManager.getDualBandAPInfo(mNoInternetWifiInfo.getBSSID());
                            if (info != null) {
                                info.mInetCapability = 2;
                                HwDualBandStateMachine.this.mHwDualBandInformationManager.updateAPInfo(info);
                                break;
                            }
                        }
                        break;
                    default:
                        return false;
                }
            }
            return true;
        }
    }

    private class CustomizedScanListener implements WifiScanner.ScanListener {
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

        public void onResults(WifiScanner.ScanData[] results) {
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
            int i = message.what;
            if (i == 4) {
                HwDualBandStateMachine.this.transitionTo(HwDualBandStateMachine.this.mDisabledState);
            } else if (i == 8) {
                Log.e(HwDualBandMessageUtil.TAG, "DefaultState MSG_WIFI_CONFIG_CHANGED");
                Bundle data = message.getData();
                String string = data.getString("bssid");
                String ssid = data.getString("ssid");
                int authtype = data.getInt(HwDualBandMessageUtil.MSG_KEY_AUTHTYPE);
                Log.e(HwDualBandMessageUtil.TAG, "MSG_WIFI_CONFIG_CHANGED ssid = " + ssid);
                if (ssid != null) {
                    HwDualBandStateMachine.this.mHwDualBandInformationManager.delectDualBandAPInfoBySsid(ssid, authtype);
                }
            } else if (i != 101) {
                switch (i) {
                    case 1:
                        Log.e(HwDualBandMessageUtil.TAG, "DefaultState MSG_WIFI_CONNECTED");
                        HwDualBandStateMachine.this.transitionTo(HwDualBandStateMachine.this.mConnectedState);
                        break;
                    case 2:
                        HwDualBandStateMachine.this.transitionTo(HwDualBandStateMachine.this.mDisconnectedState);
                        break;
                }
            } else {
                HwDualBandStateMachine.this.transitionTo(HwDualBandStateMachine.this.mStopState);
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
            if (message.what != 4) {
                return false;
            }
            return true;
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
            if (message.what != 2) {
                return false;
            }
            return true;
        }
    }

    class InternetReadyState extends State {
        private HwDualBandMonitorInfo hasDualBandMonitorCandidate = null;
        private String mCurrentBSSID = null;
        private String mCurrentSSID = null;
        private int mLastRecordLevel = 0;

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
            int i = message.what;
            short s = 1;
            if (i == 1) {
                WifiInfo mConnectedWifiInfo = HwDualBandStateMachine.this.mWifiManager.getConnectionInfo();
                if (!(mConnectedWifiInfo == null || mConnectedWifiInfo.getBSSID() == null || mConnectedWifiInfo.getBSSID().equals(this.mCurrentBSSID))) {
                    Log.e(HwDualBandMessageUtil.TAG, "InternetReadyState MSG_WIFI_CONNECTED");
                    HwDualBandStateMachine.this.transitionTo(HwDualBandStateMachine.this.mConnectedState);
                }
            } else if (i != 7) {
                if (i == 19) {
                    Log.e(HwDualBandMessageUtil.TAG, "InternetReadyState MSG_WIFI_VERIFYING_POOR_LINK");
                    HwDualBandStateMachine.this.transitionTo(HwDualBandStateMachine.this.mConnectedState);
                } else if (i == 102) {
                    List unused = HwDualBandStateMachine.this.mTargetAPList = message.getData().getParcelableArrayList(HwDualBandMessageUtil.MSG_KEY_APLIST);
                    if (HwDualBandStateMachine.this.mTargetAPList != null) {
                        Log.e(HwDualBandMessageUtil.TAG, "CMD_START_MONITOR size = " + HwDualBandStateMachine.this.mTargetAPList.size());
                    }
                    HwDualBandStateMachine.this.transitionTo(HwDualBandStateMachine.this.mMonitorState);
                } else if (i != 104) {
                    switch (i) {
                        case 11:
                        case 13:
                            break;
                        case 12:
                            Log.e(HwDualBandMessageUtil.TAG, "InternetReadyState MSG_WIFI_INTERNET_DISCONNECTED");
                            HwDualBandStateMachine.this.transitionTo(HwDualBandStateMachine.this.mConnectedState);
                            HwDualBandStateMachine.this.sendMessage(12);
                            break;
                        default:
                            int i2 = 0;
                            switch (i) {
                                case 16:
                                    Log.e(HwDualBandMessageUtil.TAG, "MSG_DUAL_BAND_WIFI_TYPE_SINGLE");
                                    WifiInfo mWifiInfo = HwDualBandStateMachine.this.mWifiManager.getConnectionInfo();
                                    if (!(mWifiInfo == null || mWifiInfo.getBSSID() == null)) {
                                        WifiProDualBandApInfoRcd apinfo = HwDualBandStateMachine.this.mHwDualBandInformationManager.getDualBandAPInfo(mWifiInfo.getBSSID());
                                        if (apinfo != null) {
                                            List<WifiProRelateApRcd> mLists = apinfo.getRelateApRcds();
                                            if (mLists.size() > 0) {
                                                Log.e(HwDualBandMessageUtil.TAG, "MSG_DUAL_BAND_WIFI_TYPE_SINGLE mLists.size() = " + mLists.size());
                                                WifiProRelateApRcd info = mLists.get(0);
                                                WifiProDualBandApInfoRcd m5GAPInfo = HwDualBandStateMachine.this.mHwDualBandInformationManager.getDualBandAPInfo(info.mRelatedBSSID);
                                                if (m5GAPInfo != null) {
                                                    if (m5GAPInfo.isInBlackList != 1) {
                                                        HwDualBandMonitorInfo hwDualBandMonitorInfo = new HwDualBandMonitorInfo(info.mRelatedBSSID, m5GAPInfo.mApSSID, m5GAPInfo.mApAuthType.shortValue(), 0, 0, info.mRelateType);
                                                        hwDualBandMonitorInfo.mIsNearAP = 1;
                                                        List<HwDualBandMonitorInfo> apList = new ArrayList<>();
                                                        Log.e(HwDualBandMessageUtil.TAG, "MSG_DUAL_BAND_WIFI_TYPE_SINGLE find ssid = " + hwDualBandMonitorInfo.mSsid + " m5GAPInfo.mApAuthType = " + m5GAPInfo.mApAuthType);
                                                        apList.add(hwDualBandMonitorInfo);
                                                        HwDualBandStateMachine.this.mIDualbandManagerCallback.onDualBandNetWorkType(1, apList);
                                                        break;
                                                    } else {
                                                        Log.e(HwDualBandMessageUtil.TAG, "MSG_DUAL_BAND_WIFI_TYPE_SINGLE m5GAPInfo.isInBlackList = " + m5GAPInfo.isInBlackList);
                                                        break;
                                                    }
                                                } else {
                                                    Log.e(HwDualBandMessageUtil.TAG, "MSG_DUAL_BAND_WIFI_TYPE_SINGLE m5GAPInfo == null");
                                                    break;
                                                }
                                            } else {
                                                Log.e(HwDualBandMessageUtil.TAG, "MSG_DUAL_BAND_WIFI_TYPE_SINGLE mLists.size() <= 0");
                                                break;
                                            }
                                        } else {
                                            Log.e(HwDualBandMessageUtil.TAG, "MSG_DUAL_BAND_WIFI_TYPE_SINGLE apinfo == null");
                                            break;
                                        }
                                    }
                                case 17:
                                    Log.e(HwDualBandMessageUtil.TAG, "MSG_DUAL_BAND_WIFI_TYPE_MIX");
                                    this.hasDualBandMonitorCandidate = null;
                                    this.mLastRecordLevel = 0;
                                    WifiInfo mMixWifiInfo = HwDualBandStateMachine.this.mWifiManager.getConnectionInfo();
                                    if (!(mMixWifiInfo == null || mMixWifiInfo.getBSSID() == null)) {
                                        List<HwDualBandMonitorInfo> mMixAPList = new ArrayList<>();
                                        WifiProDualBandApInfoRcd mMixAPinfo = HwDualBandStateMachine.this.mHwDualBandInformationManager.getDualBandAPInfo(mMixWifiInfo.getBSSID());
                                        if (mMixAPinfo != null) {
                                            List<ScanResult> lists = WifiproUtils.getScanResultsFromWsm();
                                            if (lists != null) {
                                                List<WifiProRelateApRcd> mMixLists = mMixAPinfo.getRelateApRcds();
                                                Log.e(HwDualBandMessageUtil.TAG, "MSG_DUAL_BAND_WIFI_TYPE_MIX mMixLists.size() = " + mMixLists.size());
                                                for (WifiProRelateApRcd record : mMixLists) {
                                                    WifiProDualBandApInfoRcd m5GAPInfo2 = HwDualBandStateMachine.this.mHwDualBandInformationManager.getDualBandAPInfo(record.mRelatedBSSID);
                                                    if (m5GAPInfo2 == null) {
                                                        Log.e(HwDualBandMessageUtil.TAG, "MSG_DUAL_BAND_WIFI_TYPE_MIX m5GAPInfo == null");
                                                    } else if (m5GAPInfo2.isInBlackList == s) {
                                                        Log.e(HwDualBandMessageUtil.TAG, "MSG_DUAL_BAND_WIFI_TYPE_MIX m5GAPInfo.mIsInblackList = " + m5GAPInfo2.isInBlackList);
                                                    } else {
                                                        if (m5GAPInfo2.mInetCapability.shortValue() == s) {
                                                            HwDualBandMonitorInfo hwDualBandMonitorInfo2 = new HwDualBandMonitorInfo(m5GAPInfo2.apBSSID, m5GAPInfo2.mApSSID, m5GAPInfo2.mApAuthType.shortValue(), 0, 0, record.mRelateType);
                                                            if (hwDualBandMonitorInfo2.mIsDualbandAP == s || isNearAP(record)) {
                                                                hwDualBandMonitorInfo2.mIsNearAP = s;
                                                            } else {
                                                                hwDualBandMonitorInfo2.mIsNearAP = i2;
                                                            }
                                                            Iterator<ScanResult> it = lists.iterator();
                                                            while (true) {
                                                                if (it.hasNext()) {
                                                                    ScanResult result = it.next();
                                                                    if (result.SSID != null && result.SSID.length() > 0 && result.BSSID != null && result.BSSID.equals(hwDualBandMonitorInfo2.mBssid)) {
                                                                        Log.d(HwDualBandMessageUtil.TAG, "MSG_DUAL_BAND_WIFI_TYPE_MIX find ssid = " + hwDualBandMonitorInfo2.mSsid + " , mApAuthType = " + m5GAPInfo2.mApAuthType + " , mIsNearAP = " + hwDualBandMonitorInfo2.mIsDualbandAP + " , level = " + result.level);
                                                                        if (this.hasDualBandMonitorCandidate == null) {
                                                                            this.hasDualBandMonitorCandidate = hwDualBandMonitorInfo2;
                                                                            this.mLastRecordLevel = result.level;
                                                                        } else if (this.hasDualBandMonitorCandidate.mIsDualbandAP == 1 && this.mLastRecordLevel < 0 && this.mLastRecordLevel >= -65) {
                                                                            Log.d(HwDualBandMessageUtil.TAG, "MSG_DUAL_BAND_WIFI_TYPE_MIX  hasDualBandMonitorCandidate is AP_TYPE_SINGLE");
                                                                        } else if (hwDualBandMonitorInfo2.mIsDualbandAP != 1 || result.level < -65) {
                                                                            Log.d(HwDualBandMessageUtil.TAG, "MSG_DUAL_BAND_WIFI_TYPE_MIX result.level = " + result.level + ", mLastRecordLevel = " + this.mLastRecordLevel);
                                                                            if (this.mLastRecordLevel < 0 && result.level > this.mLastRecordLevel) {
                                                                                this.hasDualBandMonitorCandidate = hwDualBandMonitorInfo2;
                                                                                this.mLastRecordLevel = result.level;
                                                                            }
                                                                        } else {
                                                                            this.hasDualBandMonitorCandidate = hwDualBandMonitorInfo2;
                                                                            this.mLastRecordLevel = result.level;
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        } else {
                                                            Log.e(HwDualBandMessageUtil.TAG, "ssid = " + m5GAPInfo2.mApSSID + " have no internet");
                                                        }
                                                        s = 1;
                                                        i2 = 0;
                                                    }
                                                }
                                                if (this.hasDualBandMonitorCandidate != null) {
                                                    Log.d(HwDualBandMessageUtil.TAG, "MSG_DUAL_BAND_WIFI_TYPE_MIX select ssid = " + this.hasDualBandMonitorCandidate.mSsid);
                                                    mMixAPList.add(this.hasDualBandMonitorCandidate);
                                                }
                                                Log.e(HwDualBandMessageUtil.TAG, "MSG_DUAL_BAND_WIFI_TYPE_MIX after filter mMixAPList.size() = " + mMixAPList.size());
                                                if (mMixAPList.size() > 0) {
                                                    HwDualBandStateMachine.this.mIDualbandManagerCallback.onDualBandNetWorkType(2, mMixAPList);
                                                    break;
                                                }
                                            } else {
                                                Log.d(HwDualBandMessageUtil.TAG, "getScanResultsFromWsm lists is null");
                                                break;
                                            }
                                        } else {
                                            Log.e(HwDualBandMessageUtil.TAG, "MSG_DUAL_BAND_WIFI_TYPE_MIX mMixAPinfo == null");
                                            break;
                                        }
                                    }
                                    break;
                                default:
                                    return false;
                            }
                            break;
                    }
                } else {
                    WifiInfo mInternetWifiInfo = HwDualBandStateMachine.this.mWifiManager.getConnectionInfo();
                    if (mInternetWifiInfo == null || mInternetWifiInfo.getBSSID() == null) {
                        Log.e(HwDualBandMessageUtil.TAG, "InternetReadyState mInternetWifiInfo == null");
                    } else if (HwDualBandStateMachine.this.mHwDualBandInformationManager.isEnterpriseSecurity(mInternetWifiInfo.getNetworkId()) || HwDualBandStateMachine.this.mHwDualBandInformationManager.isEnterpriseAP(mInternetWifiInfo.getBSSID()) || isMobileAP()) {
                        Log.e(HwDualBandMessageUtil.TAG, "InternetReadyState isEnterpriseAP");
                        HwDualBandStateMachine.this.mHwDualBandInformationManager.delectDualBandAPInfoBySsid(mInternetWifiInfo.getSSID(), HwDualBandStateMachine.this.mHwDualBandInformationManager.getAuthType(mInternetWifiInfo.getNetworkId()));
                    } else {
                        HwDualBandStateMachine.this.mHwDualBandInformationManager.saveAPInfo();
                        HwDualBandStateMachine.this.mHwDualBandRelationManager.updateAPRelation();
                    }
                }
            } else if (HwDualBandBlackListManager.getHwDualBandBlackListMgrInstance().getWifiBlacklist().isEmpty() && HwDualBandStateMachine.this.mWifiManager != null) {
                List<ScanResult> mLists2 = HwDualBandStateMachine.this.mWifiManager.getScanResults();
                if (mLists2 != null && mLists2.size() > 0 && is5gApAvailble(mLists2)) {
                    Log.d(HwDualBandMessageUtil.TAG, "InternetReadyState MSG_WIFI_UPDATE_SCAN_RESULT");
                    HwDualBandStateMachine.this.sendMessage(104);
                }
            }
            return true;
        }

        private boolean is5gApAvailble(List<ScanResult> scanResults) {
            if (scanResults == null || HwDualBandStateMachine.this.mWifiManager == null) {
                return false;
            }
            List<WifiConfiguration> configNetworks = HwDualBandStateMachine.this.mWifiManager.getConfiguredNetworks();
            int scanResultsSize = scanResults.size();
            for (int i = 0; i < scanResultsSize; i++) {
                ScanResult nextResult = scanResults.get(i);
                if (!(nextResult == null || configNetworks == null || !nextResult.is5GHz())) {
                    int signalLevel = HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(nextResult.frequency, nextResult.level);
                    int configNetworksSize = configNetworks.size();
                    if (signalLevel >= 3) {
                        for (int k = 0; k < configNetworksSize; k++) {
                            WifiConfiguration nextConfig = configNetworks.get(k);
                            StringBuilder sb = new StringBuilder();
                            sb.append("\"");
                            sb.append(nextResult.SSID);
                            sb.append("\"");
                            if ((nextConfig != null && nextConfig.SSID != null && nextConfig.SSID.equals(sb.toString()) && WifiProCommonUtils.isSameEncryptType(nextResult.capabilities, nextConfig.configKey())) && !nextConfig.noInternetAccess && !WifiProCommonUtils.isOpenAndPortal(nextConfig)) {
                                return true;
                            }
                        }
                        continue;
                    } else {
                        continue;
                    }
                }
            }
            return false;
        }

        private boolean isNearAP(WifiProRelateApRcd record) {
            if (record.mMaxRelatedRSSI == 0 || record.mMinCurrentRSSI == 0) {
                if (record.mMaxCurrentRSSI - record.mMinRelatedRSSI <= 10) {
                    return true;
                }
            } else if (record.mMaxCurrentRSSI - record.mMinRelatedRSSI <= 10 && record.mMaxRelatedRSSI - record.mMinCurrentRSSI <= 10) {
                return true;
            }
            return false;
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
                WifiScanner unused = HwDualBandStateMachine.this.mWifiScanner = WifiInjector.getInstance().getWifiScanner();
            }
            boolean unused2 = HwDualBandStateMachine.this.mIsDualbandScanning = true;
            this.mScanIndex = 0;
            int unused3 = HwDualBandStateMachine.this.mCHRSingleAPScanCount = 0;
            int unused4 = HwDualBandStateMachine.this.mCHRMixAPScanCount = 0;
            int unused5 = HwDualBandStateMachine.this.mCHRScanAPType = 0;
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
            boolean unused = HwDualBandStateMachine.this.mIsDualbandScanning = false;
            this.mScanIndex = 0;
            if (!(HwDualBandStateMachine.this.mMonitorAPList == null || HwDualBandStateMachine.this.mMonitorAPList.size() == 0)) {
                HwDualBandStateMachine.this.mMonitorAPList.clear();
            }
            int unused2 = HwDualBandStateMachine.this.mCHRSingleAPScanCount = 0;
            int unused3 = HwDualBandStateMachine.this.mCHRMixAPScanCount = 0;
            int unused4 = HwDualBandStateMachine.this.mCHRScanAPType = 0;
        }

        public boolean processMessage(Message message) {
            boolean sceneLimited = false;
            switch (message.what) {
                case 2:
                    HwDualBandStateMachine.this.initDualbandChrHandoverTooSlow(this.m24GBssid, this.m24GRssi);
                    HwDualBandStateMachine.this.transitionTo(HwDualBandStateMachine.this.mDisconnectedState);
                    break;
                case 7:
                    Log.e(HwDualBandMessageUtil.TAG, "MonitorState MSG_WIFI_UPDATE_SCAN_RESULT");
                    List<ScanResult> mLists = HwDualBandStateMachine.this.mWifiManager.getScanResults();
                    if (mLists != null && mLists.size() > 0 && isSatisfiedScanResult(mLists)) {
                        Log.e(HwDualBandMessageUtil.TAG, "MonitorState MSG_WIFI_UPDATE_SCAN_RESULT find AP");
                        HwDualBandStateMachine.this.transitionTo(HwDualBandStateMachine.this.mInternetReadyState);
                        break;
                    }
                case 8:
                    Bundle data = message.getData();
                    String string = data.getString("bssid");
                    String ssid = data.getString("ssid");
                    int authtype = data.getInt(HwDualBandMessageUtil.MSG_KEY_AUTHTYPE);
                    Log.e(HwDualBandMessageUtil.TAG, "MonitorState MSG_WIFI_REMOVE_CONFIG_CHANGED ssid = " + ssid);
                    if (ssid != null) {
                        HwDualBandStateMachine.this.mHwDualBandInformationManager.delectDualBandAPInfoBySsid(ssid, authtype);
                        removeFromMonitorList(ssid);
                        Log.e(HwDualBandMessageUtil.TAG, "MonitorState MSG_WIFI_REMOVE_CONFIG_CHANGED mMonitorAPList.size() = " + HwDualBandStateMachine.this.mMonitorAPList.size());
                        HwDualBandStateMachine.this.mIDualbandManagerCallback.onDualBandNetWorkType(0, (ArrayList) ((ArrayList) HwDualBandStateMachine.this.mMonitorAPList).clone());
                        HwDualBandStateMachine.this.transitionTo(HwDualBandStateMachine.this.mInternetReadyState);
                        break;
                    }
                    break;
                case 18:
                    this.m24GRssi = message.getData().getInt(HwDualBandMessageUtil.MSG_KEY_RSSI);
                    if (isFullscreen() || WifiProCommonUtils.isCalling(HwDualBandStateMachine.this.mContext) || WifiProCommonUtils.isLandscapeMode(HwDualBandStateMachine.this.mContext) || !HwDualBandStateMachine.this.mPowerManager.isScreenOn() || !isSuppOnCompletedState()) {
                        sceneLimited = true;
                    }
                    Log.d(HwDualBandMessageUtil.TAG, "MonitorState m24GRssi = " + this.m24GRssi + " , sceneLimited = " + sceneLimited);
                    if (!sceneLimited && isSatisfiedScanCondition(this.m24GRssi)) {
                        if (this.mScanIndex >= 3 || WifiProCommonUtils.isQueryActivityMatched(HwDualBandStateMachine.this.mContext, "com.android.settings.Settings$WifiSettingsActivity")) {
                            Log.e(HwDualBandMessageUtil.TAG, "startScan for full channels, mScanIndex = " + this.mScanIndex);
                            HwDualBandStateMachine.this.mWifiManager.startScan();
                        } else {
                            WifiScanner.ScanSettings settings = getCustomizedScanSettings();
                            Log.e(HwDualBandMessageUtil.TAG, "startScan for restrict channels, mScanIndex = " + this.mScanIndex);
                            if (settings != null) {
                                HwDualBandStateMachine.this.startCustomizedScan(settings);
                            } else {
                                HwDualBandStateMachine.this.mWifiManager.startScan();
                            }
                        }
                        this.mScanIndex++;
                        int i = 5;
                        if (this.mScanIndex <= 5) {
                            i = this.mScanIndex;
                        }
                        this.mScanIndex = i;
                        if (HwDualBandStateMachine.this.mCHRScanAPType != 1) {
                            if (HwDualBandStateMachine.this.mCHRScanAPType == 2) {
                                int unused = HwDualBandStateMachine.this.mCHRMixAPScanCount = HwDualBandStateMachine.this.mCHRMixAPScanCount + 1;
                                break;
                            }
                        } else {
                            int unused2 = HwDualBandStateMachine.this.mCHRSingleAPScanCount = HwDualBandStateMachine.this.mCHRSingleAPScanCount + 1;
                            break;
                        }
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

        private boolean isFullscreen() {
            AbsPhoneWindowManager policy = (AbsPhoneWindowManager) LocalServices.getService(WindowManagerPolicy.class);
            return policy != null && policy.isTopIsFullscreen();
        }

        private boolean isSuppOnCompletedState() {
            WifiInfo info = HwDualBandStateMachine.this.mWifiManager.getConnectionInfo();
            if (info == null || info.getSupplicantState().ordinal() != SupplicantState.COMPLETED.ordinal()) {
                return false;
            }
            return true;
        }

        private boolean isSatisfiedScanCondition(int rssi) {
            for (HwDualBandMonitorInfo info : HwDualBandStateMachine.this.mMonitorAPList) {
                int unused = HwDualBandStateMachine.this.mCHRScanAPType = info.mIsDualbandAP;
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
            List<HwDualBandMonitorInfo> mAPList = new ArrayList<>();
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
                        Log.e(HwDualBandMessageUtil.TAG, "isSatisfiedScanResult result.SSID = " + result2.SSID + " result.level = " + result2.level);
                        isMonitorAPFound = true;
                        scanResultsFound++;
                        String scanSSID = "\"" + result2.SSID + "\"";
                        if (result2.frequency != info2.mDualBandApInfoRcd.mChannelFrequency || !info2.mSsid.equals(scanSSID)) {
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
            if (this.mScanIndex >= 5) {
                int unused = HwDualBandStateMachine.this.mCHRSingleAPScanCount = 0;
                int unused2 = HwDualBandStateMachine.this.mCHRMixAPScanCount = 0;
                int unused3 = HwDualBandStateMachine.this.mCHRScanAPType = 0;
                updateAPInfo(HwDualBandStateMachine.this.mDisappearAPList);
                HwDualBandStateMachine.this.sendMessage(103);
                return false;
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

        private WifiScanner.ScanSettings getCustomizedScanSettings() {
            WifiInfo mWifiInfo = HwDualBandStateMachine.this.mWifiManager.getConnectionInfo();
            if (mWifiInfo == null || mWifiInfo.getBSSID() == null || HwDualBandStateMachine.this.mHwDualBandInformationManager.getDualBandAPInfo(mWifiInfo.getBSSID()) == null) {
                return null;
            }
            WifiScanner.ScanSettings settings = new WifiScanner.ScanSettings();
            settings.band = 0;
            settings.reportEvents = 3;
            settings.numBssidsPerScan = 0;
            settings.channels = new WifiScanner.ChannelSpec[HwDualBandStateMachine.this.mMonitorAPList.size()];
            int index = 0;
            for (HwDualBandMonitorInfo record : HwDualBandStateMachine.this.mMonitorAPList) {
                WifiProDualBandApInfoRcd scanAPInfo = HwDualBandStateMachine.this.mHwDualBandInformationManager.getDualBandAPInfo(record.mBssid);
                if (scanAPInfo != null) {
                    settings.channels[index] = new WifiScanner.ChannelSpec(scanAPInfo.mChannelFrequency);
                    Log.d(HwDualBandMessageUtil.TAG, "getCustomizedScanSettings:  Frequency = " + scanAPInfo.mChannelFrequency);
                    index++;
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
            List<HwDualBandMonitorInfo> delectList = new ArrayList<>();
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
                    info.mDualBandApInfoRcd.mDisappearCount++;
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
            if (message.what == 100) {
                HwDualBandStateMachine.this.transitionTo(HwDualBandStateMachine.this.mDefaultState);
            }
            return true;
        }
    }

    public HwDualBandStateMachine(Context context, IDualBandManagerCallback callBack) {
        super("HwDualBandStateMachine");
        this.mContext = context;
        this.mIDualbandManagerCallback = callBack;
        Context context2 = this.mContext;
        Context context3 = this.mContext;
        this.mWifiManager = (WifiManager) context2.getSystemService("wifi");
        this.mHwDualBandWiFiMonitor = new HwDualBandWiFiMonitor(context, getHandler());
        this.mHwDualBandInformationManager = HwDualBandInformationManager.createInstance(context);
        this.mHwDualBandRelationManager = HwDualBandRelationManager.createInstance(context, getHandler());
        this.mHwDualBandAdaptiveThreshold = new HwDualBandAdaptiveThreshold(context);
        this.mPowerManager = (PowerManager) context.getSystemService("power");
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

    /* access modifiers changed from: private */
    public void initDualbandChrHandoverTooSlow(String ssid, int rssi) {
        if (this.mCHRHandoverTooSlow != null) {
            this.mCHRHandoverTooSlow.mSSID_2G = ssid;
            this.mCHRHandoverTooSlow.mRSSI_2G = (short) rssi;
            Log.e(HwDualBandMessageUtil.TAG, "db_chr initDualbandChrHandoverTooSlow mCHR24GSsid" + ssid + ", m24GRssi = " + this.mCHRHandoverTooSlow.mRSSI_2G);
            for (HwDualBandMonitorInfo info : this.mMonitorAPList) {
                this.mCHRSavedAPList.add(info);
            }
        }
    }

    /* access modifiers changed from: private */
    public void startCustomizedScan(WifiScanner.ScanSettings requested) {
        this.mWifiScanner.startScan(requested, this.mCustomizedScanListener, null);
    }

    /* access modifiers changed from: private */
    public void addDisappearAPList(HwDualBandMonitorInfo info) {
        boolean addFlag = true;
        Iterator<HwDualBandMonitorInfo> it = this.mDisappearAPList.iterator();
        while (true) {
            if (it.hasNext()) {
                if (it.next().mBssid.equals(info.mBssid)) {
                    addFlag = false;
                    break;
                }
            } else {
                break;
            }
        }
        if (addFlag) {
            this.mDisappearAPList.add(info);
        }
    }
}
