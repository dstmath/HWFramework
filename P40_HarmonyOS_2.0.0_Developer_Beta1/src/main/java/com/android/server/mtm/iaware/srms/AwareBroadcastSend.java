package com.android.server.mtm.iaware.srms;

import android.app.mtm.iaware.appmng.AppMngConstant;
import android.content.Intent;
import android.net.NetworkInfo;
import android.rms.iaware.AwareLog;
import com.android.server.am.HwActivityManagerService;
import com.android.server.mtm.iaware.appmng.DecisionMaker;
import com.android.server.rms.iaware.cpu.CpuFeature;
import com.android.server.rms.iaware.srms.BroadcastExFeature;
import com.huawei.android.hidl.HealthInfoAdapter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class AwareBroadcastSend {
    private static final int BATTERY_BR_DATA_COUNT = 14;
    private static final int BATTERY_CONFIG_ITEM_COUNT = 5;
    private static final Object LOCK = new Object();
    private static final String TAG = "AwareBroadcastSend brsend";
    private static final String TAG_MISC_BATTERY_CONFIG = "brsend_config_battery_changed";
    private static final String TAG_MISC_WIFI_SC_CONFIG = "brsend_config_wifi_state_change";
    private static AwareBroadcastSend sBroadcastSend = null;
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
    private int mCountMaxCvBigChange;
    private int mCountNormalTempBigChange;
    private int mCountVoltageBigChange;
    private int mCountWifiStateChangeSkip;
    private int mCountWifiStateChangeTotal;
    private HealthInfoAdapter mHealthInfo;
    private HwActivityManagerService mHwAms;
    private int mInvalidCharger;
    private boolean mIsBatteryDataSuccessUpdated;
    private boolean mIsWifiScDataSuccessUpdated;
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

    public static AwareBroadcastSend getInstance() {
        AwareBroadcastSend awareBroadcastSend;
        synchronized (LOCK) {
            if (sBroadcastSend == null) {
                sBroadcastSend = new AwareBroadcastSend();
            }
            awareBroadcastSend = sBroadcastSend;
        }
        return awareBroadcastSend;
    }

    private AwareBroadcastSend() {
        this.mBatteryStatLock = new Object();
        this.mConfigLock = new Object();
        this.mWifiStatLock = new Object();
        this.mControlledBrs = null;
        this.mBrSendControlDynamicSwitch = true;
        this.mIsBatteryDataSuccessUpdated = false;
        this.mIsWifiScDataSuccessUpdated = false;
        this.mSkipAuthenticating = true;
        this.mBatteryNormalTempHigh = 400;
        this.mBatteryNormalTempLow = CpuFeature.MSG_SET_VIP_THREAD_PARAMS;
        this.mCountAbnormalTemp = 0;
        this.mCountBatteryBrSkip = 0;
        this.mCountBatteryBrTotal = 0;
        this.mCountMainFactorChange = 0;
        this.mCountMaxCvBigChange = 0;
        this.mCountNormalTempBigChange = 0;
        this.mCountVoltageBigChange = 0;
        this.mCountWifiStateChangeSkip = 0;
        this.mCountWifiStateChangeTotal = 0;
        this.mMaxChargingVolChangeLowestStep = 50000;
        this.mTempChangeLowestStep = 20;
        this.mVolChangeLowestStep = 50;
        this.mHealthInfo = new HealthInfoAdapter();
        this.mHwAms = null;
        this.mLastNetworkInfo = null;
        this.mNetworkInfo = null;
        this.mHwAms = HwActivityManagerService.self();
    }

    public void updateConfigData() {
        if (this.mHwAms == null) {
            AwareLog.e(TAG, "failed to get HwAMS");
            return;
        }
        DecisionMaker.getInstance().updateRule(AppMngConstant.AppMngFeature.BROADCAST, this.mHwAms.getUiContext());
        synchronized (this.mConfigLock) {
            this.mControlledBrs = DecisionMaker.getInstance().getRawConfig(AppMngConstant.AppMngFeature.BROADCAST.getDesc(), BroadcastExFeature.BR_SEND_SWITCH);
            ArrayList<String> batteryConfigs = DecisionMaker.getInstance().getRawConfig(AppMngConstant.AppMngFeature.BROADCAST.getDesc(), TAG_MISC_BATTERY_CONFIG);
            if (batteryConfigs != null) {
                updateBatteryConfigData(batteryConfigs);
            }
            ArrayList<String> wifiScConfigs = DecisionMaker.getInstance().getRawConfig(AppMngConstant.AppMngFeature.BROADCAST.getDesc(), TAG_MISC_WIFI_SC_CONFIG);
            if (wifiScConfigs != null) {
                updateWifiScConfigData(wifiScConfigs);
            }
        }
    }

    private void updateBatteryConfigData(ArrayList<String> configs) {
        if (configs.size() == 5) {
            String tempLowStr = configs.get(0);
            String tempHighStr = configs.get(1);
            String tempStepStr = configs.get(2);
            String volStepStr = configs.get(3);
            String maxCvStepStr = configs.get(4);
            if (tempLowStr != null && tempHighStr != null && tempStepStr != null && volStepStr != null && maxCvStepStr != null) {
                try {
                    int tempLow = Integer.parseInt(tempLowStr.trim());
                    int tempHigh = Integer.parseInt(tempHighStr.trim());
                    int tempStep = Integer.parseInt(tempStepStr.trim());
                    int volStep = Integer.parseInt(volStepStr.trim());
                    int maxCvStep = Integer.parseInt(maxCvStepStr.trim());
                    this.mBatteryNormalTempLow = tempLow;
                    this.mBatteryNormalTempHigh = tempHigh;
                    this.mTempChangeLowestStep = tempStep;
                    this.mVolChangeLowestStep = volStep;
                    this.mMaxChargingVolChangeLowestStep = maxCvStep;
                } catch (NumberFormatException e) {
                    AwareLog.e(TAG, "invalid battery config");
                }
            }
        }
    }

    private void updateWifiScConfigData(ArrayList<String> configs) {
        if (configs.size() != 0) {
            boolean z = false;
            String cf0 = configs.get(0);
            if (cf0 != null) {
                try {
                    int tempValue = Integer.parseInt(cf0.trim());
                    if (tempValue == 0 || tempValue == 1) {
                        if (tempValue != 0) {
                            z = true;
                        }
                        this.mSkipAuthenticating = z;
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
        if ("android.intent.action.BATTERY_CHANGED".equals(action)) {
            setBatteryBrData(data);
            return true;
        } else if (!"android.net.wifi.STATE_CHANGE".equals(action)) {
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
                this.mHealthInfo.setHealthInfo(data[0]);
                this.mLastBatteryStatus = ((Integer) data[1]).intValue();
                this.mLastBatteryHealth = ((Integer) data[2]).intValue();
                this.mLastBatteryPresent = ((Boolean) data[3]).booleanValue();
                this.mLastBatteryLevel = ((Integer) data[4]).intValue();
                this.mPlugType = ((Integer) data[5]).intValue();
                this.mLastPlugType = ((Integer) data[6]).intValue();
                this.mLastBatteryVoltage = ((Integer) data[7]).intValue();
                this.mLastBatteryTemperature = ((Integer) data[8]).intValue();
                this.mLastMaxChargingCurrent = ((Integer) data[9]).intValue();
                this.mLastMaxChargingVoltage = ((Integer) data[10]).intValue();
                this.mLastChargeCounter = ((Integer) data[11]).intValue();
                this.mInvalidCharger = ((Integer) data[12]).intValue();
                this.mLastInvalidCharger = ((Integer) data[13]).intValue();
                this.mIsBatteryDataSuccessUpdated = true;
            } catch (ClassCastException e) {
                if (AwareBroadcastDebug.getDebugDetail()) {
                    AwareLog.e(TAG, "invalid battery data");
                }
            }
        }
    }

    private void setWifiStateChangeData(Object[] data) {
        if (data.length >= 1) {
            this.mIsWifiScDataSuccessUpdated = false;
            if (data[0] instanceof Intent) {
                Object obj = ((Intent) data[0]).getParcelableExtra("networkInfo");
                if (obj instanceof NetworkInfo) {
                    this.mNetworkInfo = (NetworkInfo) obj;
                    this.mIsWifiScDataSuccessUpdated = true;
                } else if (obj == null) {
                    this.mIsWifiScDataSuccessUpdated = true;
                } else if (AwareBroadcastDebug.getDebugDetail()) {
                    AwareLog.e(TAG, "invalid wifi.STATE_CHANGE data");
                }
            }
        }
    }

    public boolean needSkipBroadcastSend(String action) {
        if (action == null || !this.mBrSendControlDynamicSwitch || !isBrControlled(action)) {
            return false;
        }
        if ("android.intent.action.BATTERY_CHANGED".equals(action)) {
            return needSkipBatteryBrSend();
        }
        if ("android.net.wifi.STATE_CHANGE".equals(action)) {
            return needSkipWifiStateChangeBrSend();
        }
        return false;
    }

    private boolean isBrControlled(String action) {
        synchronized (this.mConfigLock) {
            if (this.mControlledBrs == null) {
                return false;
            }
            return this.mControlledBrs.contains(action);
        }
    }

    private boolean needSkipBatteryBrSendByTemperature() {
        if (this.mHealthInfo.getBatteryTemperature() != this.mLastBatteryTemperature) {
            if (this.mHealthInfo.getBatteryTemperature() <= this.mBatteryNormalTempLow || this.mHealthInfo.getBatteryTemperature() >= this.mBatteryNormalTempHigh) {
                this.mCountAbnormalTemp++;
                return false;
            } else if (Math.abs(this.mHealthInfo.getBatteryTemperature() - this.mLastBatteryTemperature) >= this.mTempChangeLowestStep) {
                this.mCountNormalTempBigChange++;
                return false;
            }
        }
        return true;
    }

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
            if (this.mHealthInfo.getBatteryStatus() == this.mLastBatteryStatus && this.mHealthInfo.getBatteryHealth() == this.mLastBatteryHealth && this.mHealthInfo.getBatteryPresent() == this.mLastBatteryPresent && this.mHealthInfo.getBatteryLevel() == this.mLastBatteryLevel && this.mPlugType == this.mLastPlugType && this.mHealthInfo.getMaxChargingCurrent() == this.mLastMaxChargingCurrent) {
                if (this.mInvalidCharger == this.mLastInvalidCharger) {
                    if (this.mHealthInfo.getMaxChargingVoltage() != this.mLastMaxChargingVoltage && Math.abs(this.mHealthInfo.getMaxChargingVoltage() - this.mLastMaxChargingVoltage) >= this.mMaxChargingVolChangeLowestStep) {
                        this.mCountMaxCvBigChange++;
                        return false;
                    } else if (this.mHealthInfo.getBatteryVoltage() != this.mLastBatteryVoltage && Math.abs(this.mHealthInfo.getBatteryVoltage() - this.mLastBatteryVoltage) >= this.mVolChangeLowestStep) {
                        this.mCountVoltageBigChange++;
                        return false;
                    } else if (!needSkipBatteryBrSendByTemperature()) {
                        return false;
                    } else {
                        this.mCountBatteryBrSkip++;
                    }
                }
            }
            this.mCountMainFactorChange++;
            return false;
        }
        if (AwareBroadcastDebug.getDebugDetail()) {
            AwareLog.i(TAG, "battery br summary (skip,total,mainFactorChange,maxCVBigChange,volBigChange,abnormalTemp,normalTempBigChange): " + getBatteryStatisticsData());
        }
        return true;
    }

    private boolean isNeedSkipWifiStateChangeBrSend(NetworkInfo.DetailedState detailedState) {
        if (NetworkInfo.DetailedState.CONNECTED.equals(detailedState)) {
            return false;
        }
        if (!NetworkInfo.DetailedState.AUTHENTICATING.equals(detailedState) || !this.mSkipAuthenticating) {
            NetworkInfo networkInfo = this.mLastNetworkInfo;
            if (networkInfo == null || networkInfo.getDetailedState() != detailedState) {
                return false;
            }
            if (AwareBroadcastDebug.getDebugDetail()) {
                AwareLog.i(TAG, "Skip broadcast wifi.STATE_CHANGE, reason: detailedState same as previous (" + detailedState + ")");
            }
            this.mCountWifiStateChangeSkip++;
            return true;
        }
        if (AwareBroadcastDebug.getDebugDetail()) {
            AwareLog.i(TAG, "Skip broadcast wifi.STATE_CHANGE, reason: detailedState is AUTHENTICATING");
        }
        this.mCountWifiStateChangeSkip++;
        return true;
    }

    private boolean needSkipWifiStateChangeBrSend() {
        if (!this.mIsWifiScDataSuccessUpdated) {
            return false;
        }
        if (AwareBroadcastDebug.getDebugDetail()) {
            NetworkInfo networkInfo = this.mNetworkInfo;
            AwareLog.i(TAG, "wifi.STATE_CHANGE detailedState: " + (networkInfo == null ? "Null" : networkInfo.getDetailedState().toString()));
        }
        boolean bSkip = false;
        synchronized (this.mWifiStatLock) {
            this.mCountWifiStateChangeTotal++;
            if (this.mNetworkInfo != null) {
                bSkip = isNeedSkipWifiStateChangeBrSend(this.mNetworkInfo.getDetailedState());
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
            return " voltage:" + this.mHealthInfo.getBatteryVoltage() + " temper:" + this.mHealthInfo.getBatteryTemperature() + " maxCV:" + this.mHealthInfo.getMaxChargingVoltage() + " maxCC:" + this.mHealthInfo.getMaxChargingCurrent() + " status:" + this.mHealthInfo.getBatteryStatus() + " health:" + this.mHealthInfo.getBatteryHealth() + " present:" + this.mHealthInfo.getBatteryPresent() + " level:" + this.mHealthInfo.getBatteryLevel() + " plug:" + this.mPlugType + " invalidCharger:" + this.mInvalidCharger + " chargeCntr:" + this.mHealthInfo.getBatteryChargeCounter();
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
            this.mCountMaxCvBigChange = 0;
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
            str = this.mCountBatteryBrSkip + "," + this.mCountBatteryBrTotal + "," + this.mCountMainFactorChange + "," + this.mCountMaxCvBigChange + "," + this.mCountVoltageBigChange + "," + this.mCountAbnormalTemp + "," + this.mCountNormalTempBigChange;
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

    public void dumpBrSendInfo(PrintWriter pw) {
        pw.println("battery br summary (skip,total,mainFactorChange,maxCVBigChange,volBigChange,abnormalTemp,normalTempBigChange): " + getBatteryStatisticsData());
        pw.println("wifi.STATE_CHANGE br summary (skip,total): " + getWifiStateChangeDebugData());
    }

    public void dumpBrSendConfig(PrintWriter pw) {
        updateConfigData();
        pw.println("BrSend feature enable: " + BroadcastExFeature.isFeatureEnabled(2));
        StringBuilder sb = new StringBuilder();
        sb.append("switch dynamic status: ");
        sb.append(this.mBrSendControlDynamicSwitch ? "on" : "off");
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
