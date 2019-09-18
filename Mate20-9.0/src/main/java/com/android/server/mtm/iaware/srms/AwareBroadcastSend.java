package com.android.server.mtm.iaware.srms;

import android.app.mtm.iaware.appmng.AppMngConstant;
import android.content.Intent;
import android.hardware.health.V1_0.HealthInfo;
import android.net.NetworkInfo;
import android.rms.iaware.AwareLog;
import com.android.server.am.HwActivityManagerService;
import com.android.server.mtm.iaware.appmng.DecisionMaker;
import com.android.server.pfw.autostartup.comm.XmlConst;
import com.android.server.rms.iaware.srms.BroadcastExFeature;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class AwareBroadcastSend {
    private static final int BATTERY_BR_DATA_COUNT = 14;
    private static final int BATTERY_CONFIG_ITEM_COUNT = 5;
    private static final String TAG = "AwareBroadcastSend brsend";
    private static final String TAG_MISC_BATTERY_CONFIG = "brsend_config_battery_changed";
    private static final String TAG_MISC_WIFI_SC_CONFIG = "brsend_config_wifi_state_change";
    private static AwareBroadcastSend mBroadcastSend = null;
    private int mBatteryNormalTempHigh;
    private int mBatteryNormalTempLow;
    private final Object mBatteryStatLock;
    private boolean mBrSendControlDynamicSwitch;
    private final Object mConfigLock;
    private ArrayList<String> mControlledBrs;
    private int mCountAbnormalTemp;
    private int mCountBatteryBrSkip;
    private int mCountBatteryBrTotal;
    private int mCountMainFactorChange;
    private int mCountMaxCVBigChange;
    private int mCountNormalTempBigChange;
    private int mCountVoltageBigChange;
    private int mCountWifiStateChangeSkip;
    private int mCountWifiStateChangeTotal;
    private HealthInfo mHealthInfo;
    private HwActivityManagerService mHwAMS;
    private int mInvalidCharger;
    private boolean mIsBatteryDataSuccessUpdated;
    private boolean mIsWifiSCDataSuccessUpdated;
    private int mLastBatteryHealth;
    private int mLastBatteryLevel;
    private boolean mLastBatteryPresent;
    private int mLastBatteryStatus;
    private int mLastBatteryTemperature;
    private int mLastBatteryVoltage;
    private int mLastChargeCounter;
    private int mLastInvalidCharger;
    private int mLastMaxChargingCurrent;
    private int mLastMaxChargingVoltage;
    private NetworkInfo mLastNetworkInfo;
    private int mLastPlugType;
    private int mMaxChargingVolChangeLowestStep;
    private NetworkInfo mNetworkInfo;
    private int mPlugType;
    private boolean mSkipAuthenticating;
    private int mTempChangeLowestStep;
    private int mVolChangeLowestStep;
    private final Object mWifiStatLock;

    public static synchronized AwareBroadcastSend getInstance() {
        AwareBroadcastSend awareBroadcastSend;
        synchronized (AwareBroadcastSend.class) {
            if (mBroadcastSend == null) {
                mBroadcastSend = new AwareBroadcastSend();
                mBroadcastSend.updateConfigData();
            }
            awareBroadcastSend = mBroadcastSend;
        }
        return awareBroadcastSend;
    }

    private AwareBroadcastSend() {
        this.mHwAMS = null;
        this.mConfigLock = new Object();
        this.mBrSendControlDynamicSwitch = true;
        this.mControlledBrs = null;
        this.mBatteryStatLock = new Object();
        this.mCountBatteryBrTotal = 0;
        this.mCountBatteryBrSkip = 0;
        this.mCountMainFactorChange = 0;
        this.mCountVoltageBigChange = 0;
        this.mCountMaxCVBigChange = 0;
        this.mCountAbnormalTemp = 0;
        this.mCountNormalTempBigChange = 0;
        this.mIsBatteryDataSuccessUpdated = false;
        this.mBatteryNormalTempLow = 150;
        this.mBatteryNormalTempHigh = 400;
        this.mTempChangeLowestStep = 20;
        this.mVolChangeLowestStep = 50;
        this.mMaxChargingVolChangeLowestStep = 50000;
        this.mWifiStatLock = new Object();
        this.mNetworkInfo = null;
        this.mLastNetworkInfo = null;
        this.mCountWifiStateChangeTotal = 0;
        this.mCountWifiStateChangeSkip = 0;
        this.mIsWifiSCDataSuccessUpdated = false;
        this.mSkipAuthenticating = true;
        this.mHwAMS = HwActivityManagerService.self();
    }

    public void updateConfigData() {
        if (this.mHwAMS == null) {
            AwareLog.e(TAG, "failed to get HwAMS");
            return;
        }
        DecisionMaker.getInstance().updateRule(AppMngConstant.AppMngFeature.BROADCAST, this.mHwAMS.getUiContext());
        synchronized (this.mConfigLock) {
            this.mControlledBrs = DecisionMaker.getInstance().getRawConfig(AppMngConstant.AppMngFeature.BROADCAST.getDesc(), BroadcastExFeature.BR_SEND_SWITCH);
            ArrayList<String> batteryConfigs = DecisionMaker.getInstance().getRawConfig(AppMngConstant.AppMngFeature.BROADCAST.getDesc(), TAG_MISC_BATTERY_CONFIG);
            if (batteryConfigs != null) {
                updateBatteryConfigData(batteryConfigs);
            }
            ArrayList<String> wifiSCConfigs = DecisionMaker.getInstance().getRawConfig(AppMngConstant.AppMngFeature.BROADCAST.getDesc(), TAG_MISC_WIFI_SC_CONFIG);
            if (wifiSCConfigs != null) {
                updateWifiSCConfigData(wifiSCConfigs);
            }
        }
    }

    private void updateBatteryConfigData(ArrayList<String> configs) {
        if (configs.size() == 5) {
            String cf0 = configs.get(0);
            String cf1 = configs.get(1);
            String cf2 = configs.get(2);
            String cf3 = configs.get(3);
            String cf4 = configs.get(4);
            if (cf0 != null && cf1 != null && cf2 != null && cf3 != null && cf4 != null) {
                try {
                    int tempLow = Integer.parseInt(cf0.trim());
                    int tempHigh = Integer.parseInt(cf1.trim());
                    int tempStep = Integer.parseInt(cf2.trim());
                    int volStep = Integer.parseInt(cf3.trim());
                    int maxCVStep = Integer.parseInt(cf4.trim());
                    this.mBatteryNormalTempLow = tempLow;
                    this.mBatteryNormalTempHigh = tempHigh;
                    this.mTempChangeLowestStep = tempStep;
                    this.mVolChangeLowestStep = volStep;
                    this.mMaxChargingVolChangeLowestStep = maxCVStep;
                } catch (NumberFormatException e) {
                    AwareLog.e(TAG, "invalid battery config");
                }
            }
        }
    }

    private void updateWifiSCConfigData(ArrayList<String> configs) {
        if (configs.size() != 0) {
            String cf0 = configs.get(0);
            if (cf0 != null) {
                try {
                    int tempValue = Integer.parseInt(cf0.trim());
                    if (tempValue == 0) {
                        this.mSkipAuthenticating = false;
                    } else if (tempValue == 1) {
                        this.mSkipAuthenticating = true;
                    }
                } catch (NumberFormatException e) {
                    AwareLog.e(TAG, "invalid wifi.STATE_CHANGE config");
                }
            }
        }
    }

    public boolean setData(String action, Object[] data) {
        if (action == null || data == null) {
            return false;
        }
        if (action.equals("android.intent.action.BATTERY_CHANGED")) {
            setBatteryBrData(data);
            return true;
        } else if (!action.equals("android.net.wifi.STATE_CHANGE")) {
            return false;
        } else {
            setWifiStateChangeData(data);
            return true;
        }
    }

    private void setBatteryBrData(Object[] data) {
        if (data.length == 14) {
            this.mIsBatteryDataSuccessUpdated = false;
            try {
                this.mHealthInfo = data[0];
                if (this.mHealthInfo != null) {
                    this.mLastBatteryStatus = data[1].intValue();
                    this.mLastBatteryHealth = data[2].intValue();
                    this.mLastBatteryPresent = data[3].booleanValue();
                    this.mLastBatteryLevel = data[4].intValue();
                    this.mPlugType = data[5].intValue();
                    this.mLastPlugType = data[6].intValue();
                    this.mLastBatteryVoltage = data[7].intValue();
                    this.mLastBatteryTemperature = data[8].intValue();
                    this.mLastMaxChargingCurrent = data[9].intValue();
                    this.mLastMaxChargingVoltage = data[10].intValue();
                    this.mLastChargeCounter = data[11].intValue();
                    this.mInvalidCharger = data[12].intValue();
                    this.mLastInvalidCharger = data[13].intValue();
                    this.mIsBatteryDataSuccessUpdated = true;
                }
            } catch (ClassCastException e) {
                if (AwareBroadcastDebug.getDebugDetail()) {
                    AwareLog.e(TAG, "invalid battery data");
                }
            }
        }
    }

    private void setWifiStateChangeData(Object[] data) {
        if (data.length >= 1) {
            this.mIsWifiSCDataSuccessUpdated = false;
            try {
                Intent intent = data[0];
                if (intent != null) {
                    this.mNetworkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    this.mIsWifiSCDataSuccessUpdated = true;
                }
            } catch (ClassCastException e) {
                if (AwareBroadcastDebug.getDebugDetail()) {
                    AwareLog.e(TAG, "invalid wifi.STATE_CHANGE data");
                }
            }
        }
    }

    public boolean needSkipBroadcastSend(String action) {
        if (action == null || !this.mBrSendControlDynamicSwitch || !isBrControlled(action)) {
            return false;
        }
        if (action.equals("android.intent.action.BATTERY_CHANGED")) {
            return needSkipBatteryBrSend();
        }
        if (action.equals("android.net.wifi.STATE_CHANGE")) {
            return needSkipWifiStateChangeBrSend();
        }
        return false;
    }

    private boolean isBrControlled(String action) {
        synchronized (this.mConfigLock) {
            if (this.mControlledBrs == null) {
                return false;
            }
            boolean contains = this.mControlledBrs.contains(action);
            return contains;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00fb, code lost:
        if (com.android.server.mtm.iaware.srms.AwareBroadcastDebug.getDebugDetail() == false) goto L_0x0117;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00fd, code lost:
        android.rms.iaware.AwareLog.i(TAG, "battery br summary (skip,total,mainFactorChange,maxCVBigChange,volBigChange,abnormalTemp,normalTempBigChange): " + getBatteryStatisticsData());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x0117, code lost:
        return true;
     */
    private boolean needSkipBatteryBrSend() {
        if (!this.mIsBatteryDataSuccessUpdated) {
            return false;
        }
        if (AwareBroadcastDebug.getDebugDetail()) {
            AwareLog.i(TAG, "battery  new data: " + batteryInfoToString(true));
            AwareLog.i(TAG, "battery last data: " + batteryInfoToString(false));
        }
        synchronized (this.mBatteryStatLock) {
            this.mCountBatteryBrTotal++;
            if (this.mHealthInfo.batteryStatus == this.mLastBatteryStatus && this.mHealthInfo.batteryHealth == this.mLastBatteryHealth && this.mHealthInfo.batteryPresent == this.mLastBatteryPresent && this.mHealthInfo.batteryLevel == this.mLastBatteryLevel && this.mPlugType == this.mLastPlugType && this.mHealthInfo.maxChargingCurrent == this.mLastMaxChargingCurrent) {
                if (this.mInvalidCharger == this.mLastInvalidCharger) {
                    if (this.mHealthInfo.maxChargingVoltage != this.mLastMaxChargingVoltage && Math.abs(this.mHealthInfo.maxChargingVoltage - this.mLastMaxChargingVoltage) >= this.mMaxChargingVolChangeLowestStep) {
                        this.mCountMaxCVBigChange++;
                        return false;
                    } else if (this.mHealthInfo.batteryVoltage == this.mLastBatteryVoltage || Math.abs(this.mHealthInfo.batteryVoltage - this.mLastBatteryVoltage) < this.mVolChangeLowestStep) {
                        if (this.mHealthInfo.batteryTemperature != this.mLastBatteryTemperature) {
                            if (this.mHealthInfo.batteryTemperature > this.mBatteryNormalTempLow) {
                                if (this.mHealthInfo.batteryTemperature < this.mBatteryNormalTempHigh) {
                                    if (Math.abs(this.mHealthInfo.batteryTemperature - this.mLastBatteryTemperature) >= this.mTempChangeLowestStep) {
                                        this.mCountNormalTempBigChange++;
                                        return false;
                                    }
                                }
                            }
                            this.mCountAbnormalTemp++;
                            return false;
                        }
                        this.mCountBatteryBrSkip++;
                    } else {
                        this.mCountVoltageBigChange++;
                        return false;
                    }
                }
            }
            this.mCountMainFactorChange++;
            return false;
        }
    }

    private boolean needSkipWifiStateChangeBrSend() {
        if (!this.mIsWifiSCDataSuccessUpdated) {
            return false;
        }
        if (AwareBroadcastDebug.getDebugDetail()) {
            String dState = this.mNetworkInfo == null ? "Null" : this.mNetworkInfo.getDetailedState().toString();
            AwareLog.i(TAG, "wifi.STATE_CHANGE detailedState: " + dState);
        }
        boolean bSkip = false;
        synchronized (this.mWifiStatLock) {
            this.mCountWifiStateChangeTotal++;
            if (this.mNetworkInfo != null) {
                NetworkInfo.DetailedState detailedState = this.mNetworkInfo.getDetailedState();
                if (NetworkInfo.DetailedState.CONNECTED.equals(detailedState)) {
                    bSkip = false;
                } else if (NetworkInfo.DetailedState.AUTHENTICATING.equals(detailedState) && this.mSkipAuthenticating) {
                    if (AwareBroadcastDebug.getDebugDetail()) {
                        AwareLog.i(TAG, "Skip broadcast wifi.STATE_CHANGE, reason: detailedState is AUTHENTICATING");
                    }
                    this.mCountWifiStateChangeSkip++;
                    bSkip = true;
                } else if (this.mLastNetworkInfo == null) {
                    bSkip = false;
                } else if (this.mLastNetworkInfo.getDetailedState() == detailedState) {
                    if (AwareBroadcastDebug.getDebugDetail()) {
                        AwareLog.i(TAG, "Skip broadcast wifi.STATE_CHANGE, reason: detailedState same as previous (" + detailedState + ")");
                    }
                    this.mCountWifiStateChangeSkip++;
                    bSkip = true;
                } else {
                    bSkip = false;
                }
            }
        }
        if (!bSkip) {
            this.mLastNetworkInfo = this.mNetworkInfo;
        }
        if (AwareBroadcastDebug.getDebugDetail()) {
            AwareLog.i(TAG, "wifi.STATE_CHANGE br summary (skip,total): " + getWifiStateChangeDebugData());
        }
        return bSkip;
    }

    private String batteryInfoToString(boolean newData) {
        if (newData) {
            return " voltage:" + this.mHealthInfo.batteryVoltage + " temper:" + this.mHealthInfo.batteryTemperature + " maxCV:" + this.mHealthInfo.maxChargingVoltage + " maxCC:" + this.mHealthInfo.maxChargingCurrent + " status:" + this.mHealthInfo.batteryStatus + " health:" + this.mHealthInfo.batteryHealth + " present:" + this.mHealthInfo.batteryPresent + " level:" + this.mHealthInfo.batteryLevel + " plug:" + this.mPlugType + " invalidCharger:" + this.mInvalidCharger + " chargeCntr:" + this.mHealthInfo.batteryChargeCounter;
        }
        return " voltage:" + this.mLastBatteryVoltage + " temper:" + this.mLastBatteryTemperature + " maxCV:" + this.mLastMaxChargingVoltage + " maxCC:" + this.mLastMaxChargingCurrent + " status:" + this.mLastBatteryStatus + " health:" + this.mLastBatteryHealth + " present:" + this.mLastBatteryPresent + " level:" + this.mLastBatteryLevel + " plug:" + this.mLastPlugType + " invalidCharger:" + this.mLastInvalidCharger + " chargeCntr:" + this.mLastChargeCounter;
    }

    public void changeSwitch(boolean switchValue) {
        this.mBrSendControlDynamicSwitch = switchValue;
    }

    public HashMap<String, String> getStatisticsData() {
        HashMap<String, String> data = new HashMap<>();
        data.put("android.intent.action.BATTERY_CHANGED", getBatteryStatisticsData());
        data.put("android.net.wifi.STATE_CHANGE", getWifiStateChangeStatisticsData());
        return data;
    }

    public void resetStatisticsData() {
        synchronized (this.mBatteryStatLock) {
            this.mCountBatteryBrSkip = 0;
            this.mCountBatteryBrTotal = 0;
            this.mCountMainFactorChange = 0;
            this.mCountMaxCVBigChange = 0;
            this.mCountVoltageBigChange = 0;
            this.mCountAbnormalTemp = 0;
            this.mCountNormalTempBigChange = 0;
        }
        synchronized (this.mWifiStatLock) {
            this.mCountWifiStateChangeSkip = 0;
            this.mCountWifiStateChangeTotal = 0;
        }
    }

    private String getBatteryStatisticsData() {
        String str;
        synchronized (this.mBatteryStatLock) {
            str = this.mCountBatteryBrSkip + "," + this.mCountBatteryBrTotal + "," + this.mCountMainFactorChange + "," + this.mCountMaxCVBigChange + "," + this.mCountVoltageBigChange + "," + this.mCountAbnormalTemp + "," + this.mCountNormalTempBigChange;
        }
        return str;
    }

    private String getWifiStateChangeStatisticsData() {
        String str;
        synchronized (this.mWifiStatLock) {
            str = this.mCountWifiStateChangeSkip + "," + this.mCountWifiStateChangeTotal + ",0,0,0,0,0";
        }
        return str;
    }

    private String getWifiStateChangeDebugData() {
        String str;
        synchronized (this.mWifiStatLock) {
            str = this.mCountWifiStateChangeSkip + "," + this.mCountWifiStateChangeTotal;
        }
        return str;
    }

    public void dumpBRSendInfo(PrintWriter pw) {
        pw.println("battery br summary (skip,total,mainFactorChange,maxCVBigChange,volBigChange,abnormalTemp,normalTempBigChange): " + getBatteryStatisticsData());
        pw.println("wifi.STATE_CHANGE br summary (skip,total): " + getWifiStateChangeDebugData());
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0107, code lost:
        return;
     */
    public void dumpBRSendConfig(PrintWriter pw) {
        updateConfigData();
        pw.println("BrSend feature enable: " + BroadcastExFeature.isFeatureEnabled(2));
        StringBuilder sb = new StringBuilder();
        sb.append("switch dynamic status: ");
        sb.append(this.mBrSendControlDynamicSwitch ? XmlConst.PreciseIgnore.COMP_SCREEN_ON_VALUE_ : "off");
        pw.println(sb.toString());
        synchronized (this.mConfigLock) {
            if (this.mControlledBrs != null) {
                Iterator<String> iterator = this.mControlledBrs.iterator();
                if (iterator != null) {
                    while (iterator.hasNext()) {
                        pw.println("Controlled Br: " + iterator.next());
                    }
                    pw.println("");
                    pw.println("battery br send control configs:");
                    pw.println("normal temp low threshold: " + this.mBatteryNormalTempLow);
                    pw.println("normal temp high threshold: " + this.mBatteryNormalTempHigh);
                    pw.println("normal temp change lowest step: " + this.mTempChangeLowestStep);
                    pw.println("voltage change lowest step: " + this.mVolChangeLowestStep);
                    pw.println("max charging voltage change lowest step: " + this.mMaxChargingVolChangeLowestStep);
                    pw.println("");
                    pw.println("wifi.STATE_CHANGE configs:");
                    pw.println("Skip Authenticating: " + this.mSkipAuthenticating);
                }
            }
        }
    }
}
