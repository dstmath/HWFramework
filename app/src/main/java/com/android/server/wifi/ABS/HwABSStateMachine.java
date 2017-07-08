package com.android.server.wifi.ABS;

import android.app.AlarmManager;
import android.app.AlarmManager.OnAlarmListener;
import android.common.HwFrameworkFactory;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings.System;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.wifi.WifiStateMachine;
import com.android.server.wifi.hotspot2.NetworkDetail;
import com.android.server.wifi.util.ScanDetailUtil;
import com.android.server.wifi.wifipro.HwDualBandMessageUtil;
import com.android.server.wifi.wifipro.WifiProStatisticsManager;
import com.android.server.wifi.wifipro.hwintelligencewifi.MessageUtil;
import java.util.ArrayList;
import java.util.List;

public class HwABSStateMachine extends StateMachine {
    private static final long ABS_INTERVAL_TIME = 1800000;
    private static final long ABS_PUNISH_TIME = 30000;
    private static final long ABS_SCREEN_ON_TIME = 10000;
    private static final String ACTION_ABS_HANDOVER_TIMER = "android.net.wifi.abs_handover_timer";
    private static final int SIM_CARD_STATE_MIMO = 2;
    private static final int SIM_CARD_STATE_SISO = 1;
    private static HwABSStateMachine mHwABSStateMachine;
    private int ABS_HANDOVER_TIMES;
    private long ABS_LAST_HANDOVER_TIME;
    private boolean ANTENNA_STATE_IN_CALL;
    private boolean ANTENNA_STATE_IN_CONNECT;
    private boolean ANTENNA_STATE_IN_SEARCH;
    private boolean isSwitching;
    private long mABSMIMOStartTime;
    private long mABSSISOStartTime;
    private AlarmManager mAlarmManager;
    OnAlarmListener mAutoHandoverTimerListener;
    private Context mContext;
    private State mDefaultState;
    private Handler mHandler;
    private HwABSDataBaseManager mHwABSDataBaseManager;
    private HwABSModemScenario mHwABSModemScenario;
    private HwABSWiFiHandler mHwABSWiFiHandler;
    private HwABSWiFiScenario mHwABSWiFiScenario;
    private State mMimoState;
    private List<Integer> mModemStateList;
    private State mSisoState;
    private TelephonyManager mTelephonyManager;
    private State mWiFiConnectedState;
    private State mWiFiDisableState;
    private State mWiFiDisconnectedState;
    private State mWiFiEnableState;
    private WifiManager mWifiManager;

    class DefaultState extends State {
        Bundle mData;
        int mSubId;

        DefaultState() {
            this.mData = null;
            this.mSubId = 0;
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case HwABSStateMachine.SIM_CARD_STATE_SISO /*1*/:
                    HwABSUtils.logD("DefaultState MSG_WIFI_CONNECTED");
                    HwABSStateMachine.this.transitionTo(HwABSStateMachine.this.mWiFiConnectedState);
                    HwABSStateMachine.this.sendMessage(HwABSStateMachine.SIM_CARD_STATE_SISO);
                    break;
                case HwABSStateMachine.SIM_CARD_STATE_MIMO /*2*/:
                    HwABSUtils.logD("DefaultState MSG_WIFI_DISCONNECTED");
                    HwABSStateMachine.this.transitionTo(HwABSStateMachine.this.mWiFiDisconnectedState);
                    break;
                case MessageUtil.MSG_WIFI_ENABLED /*3*/:
                    HwABSUtils.logD("DefaultState MSG_WIFI_ENABLED");
                    HwABSStateMachine.this.transitionTo(HwABSStateMachine.this.mWiFiEnableState);
                    break;
                case MessageUtil.MSG_WIFI_DISABLE /*4*/:
                    HwABSUtils.logD("DefaultState MSG_WIFI_DISABLE");
                    HwABSStateMachine.this.transitionTo(HwABSStateMachine.this.mWiFiDisableState);
                    break;
                case MessageUtil.MSG_WIFI_UPDATE_SCAN_RESULT /*7*/:
                    HwABSUtils.logD("DefaultState MSG_OUTGOING_CALL");
                    HwABSStateMachine.this.ANTENNA_STATE_IN_CALL = true;
                    HwABSStateMachine.this.resetCapablity(HwABSStateMachine.SIM_CARD_STATE_SISO);
                    break;
                case MessageUtil.MSG_WIFI_CONFIG_CHANGED /*8*/:
                    HwABSUtils.logD("DefaultState MSG_CALL_STATE_IDLE");
                    HwABSStateMachine.this.ANTENNA_STATE_IN_CALL = false;
                    HwABSStateMachine.this.resetCapablity(HwABSStateMachine.SIM_CARD_STATE_MIMO);
                    break;
                case MessageUtil.MSG_WIFI_HANDLE_DISABLE /*9*/:
                    HwABSUtils.logD("DefaultState MSG_CALL_STATE_RINGING");
                    HwABSStateMachine.this.ANTENNA_STATE_IN_CALL = true;
                    HwABSStateMachine.this.resetCapablity(HwABSStateMachine.SIM_CARD_STATE_SISO);
                    break;
                case MessageUtil.MSG_WIFI_INTERNET_CONNECTED /*11*/:
                    HwABSUtils.logD("DefaultState MSG_MODEM_ENTER_CONNECT_STATE");
                    HwABSStateMachine.this.ANTENNA_STATE_IN_CONNECT = true;
                    HwABSStateMachine.this.resetCapablity(HwABSStateMachine.SIM_CARD_STATE_SISO);
                    break;
                case MessageUtil.MSG_WIFI_INTERNET_DISCONNECTED /*12*/:
                    HwABSUtils.logD("DefaultState MSG_MODEM_EXIT_CONNECT_STATE");
                    if (HwABSStateMachine.this.ANTENNA_STATE_IN_CONNECT) {
                        HwABSStateMachine.this.ANTENNA_STATE_IN_CONNECT = false;
                        HwABSStateMachine.this.resetCapablity(HwABSStateMachine.SIM_CARD_STATE_MIMO);
                        break;
                    }
                    break;
                case MessageUtil.MSG_WIFI_IS_PORTAL /*13*/:
                    HwABSUtils.logD("DefaultState MSG_MODEM_ENTER_SEARCHING_STATE");
                    HwABSStateMachine.this.ANTENNA_STATE_IN_SEARCH = true;
                    HwABSStateMachine.this.resetCapablity(HwABSStateMachine.SIM_CARD_STATE_SISO);
                    this.mData = message.getData();
                    this.mSubId = this.mData.getInt(HwABSUtils.SUB_ID);
                    HwABSStateMachine.this.addModemState(this.mSubId);
                    break;
                case MessageUtil.MSG_WIFI_P2P_CONNECTED /*14*/:
                    HwABSUtils.logD("DefaultState MSG_MODEM_EXIT_SEARCHING_STATE");
                    this.mData = message.getData();
                    this.mSubId = this.mData.getInt(HwABSUtils.SUB_ID);
                    if (HwABSStateMachine.this.removeModemState(this.mSubId) == 0) {
                        HwABSStateMachine.this.ANTENNA_STATE_IN_SEARCH = false;
                        HwABSStateMachine.this.resetCapablity(HwABSStateMachine.SIM_CARD_STATE_MIMO);
                        break;
                    }
                    break;
                case MessageUtil.MSG_SCREEN_ON /*21*/:
                    HwABSStateMachine.this.handlePowerOffMessage();
                    if (HwABSStateMachine.this.isModemStateInIdle()) {
                        HwABSStateMachine.this.resetCapablity(HwABSStateMachine.SIM_CARD_STATE_MIMO);
                        break;
                    }
                    break;
            }
            return true;
        }
    }

    class MimoState extends State {
        private String mCurrentBSSID;
        Bundle mData;
        int mSubId;

        MimoState() {
            this.mCurrentBSSID = null;
            this.mData = null;
            this.mSubId = 0;
        }

        public void enter() {
            HwABSUtils.logD("enter MimoState");
            HwABSStateMachine.this.setWiFiAntennaMonitor(true);
            HwABSStateMachine.this.mABSMIMOStartTime = System.currentTimeMillis();
            WifiInfo wifiInfo = HwABSStateMachine.this.mWifiManager.getConnectionInfo();
            if (wifiInfo != null && wifiInfo.getBSSID() != null) {
                this.mCurrentBSSID = wifiInfo.getBSSID();
                HwABSUtils.logD("MimoState mCurrentBSSID = " + this.mCurrentBSSID);
            }
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case HwABSStateMachine.SIM_CARD_STATE_SISO /*1*/:
                    HwABSUtils.logE("MimoState MSG_WIFI_CONNECTED");
                    break;
                case MessageUtil.MSG_WIFI_FIND_TARGET /*5*/:
                    HwABSUtils.logE("MimoState MSG_SCREEN_ON");
                    releaseAutoHandoverTimer();
                    break;
                case MessageUtil.MSG_WIFI_DISABLEING /*6*/:
                    HwABSUtils.logE("MimoState MSG_SCREEN_OFF");
                    if (!HwABSStateMachine.this.isAirModeOn()) {
                        setAutoHandoverTimer();
                        break;
                    }
                    HwABSUtils.logE("MimoState MSG_SCREEN_OFF isUsingMIMOCapability() = " + HwABSStateMachine.this.isUsingMIMOCapability() + "  isAirModeOn() = " + HwABSStateMachine.this.isAirModeOn());
                    break;
                case MessageUtil.MSG_WIFI_UPDATE_SCAN_RESULT /*7*/:
                    HwABSUtils.logE("MimoState MSG_OUTGOING_CALL");
                    HwABSStateMachine.this.ANTENNA_STATE_IN_CALL = true;
                    HwABSStateMachine.this.mHwABSWiFiHandler.hwABSHandover(HwABSStateMachine.SIM_CARD_STATE_SISO);
                    HwABSStateMachine.this.transitionTo(HwABSStateMachine.this.mSisoState);
                    break;
                case MessageUtil.MSG_WIFI_HANDLE_DISABLE /*9*/:
                    HwABSUtils.logE("MimoState MSG_CALL_STATE_RINGING");
                    HwABSStateMachine.this.ANTENNA_STATE_IN_CALL = true;
                    HwABSStateMachine.this.mHwABSWiFiHandler.hwABSHandover(HwABSStateMachine.SIM_CARD_STATE_SISO);
                    HwABSStateMachine.this.transitionTo(HwABSStateMachine.this.mSisoState);
                    break;
                case MessageUtil.MSG_WIFI_INTERNET_CONNECTED /*11*/:
                    HwABSUtils.logE("MimoState MSG_MODEM_ENTER_CONNECT_STATE");
                    HwABSStateMachine.this.ANTENNA_STATE_IN_CONNECT = true;
                    HwABSStateMachine.this.hwABSWiFiHandover(HwABSStateMachine.SIM_CARD_STATE_SISO);
                    HwABSStateMachine.this.transitionTo(HwABSStateMachine.this.mSisoState);
                    break;
                case MessageUtil.MSG_WIFI_INTERNET_DISCONNECTED /*12*/:
                    HwABSUtils.logE("MimoState MSG_MODEM_EXIT_CONNECT_STATE ANTENNA_STATE_IN_CONNECT = " + HwABSStateMachine.this.ANTENNA_STATE_IN_CONNECT);
                    if (HwABSStateMachine.this.ANTENNA_STATE_IN_CONNECT) {
                        HwABSStateMachine.this.ANTENNA_STATE_IN_CONNECT = false;
                        break;
                    }
                    break;
                case MessageUtil.MSG_WIFI_IS_PORTAL /*13*/:
                    HwABSUtils.logE("MimoState MSG_MODEM_ENTER_SEARCHING_STATE");
                    HwABSStateMachine.this.ANTENNA_STATE_IN_SEARCH = true;
                    this.mSubId = message.getData().getInt(HwABSUtils.SUB_ID);
                    HwABSStateMachine.this.addModemState(this.mSubId);
                    HwABSStateMachine.this.hwABSWiFiHandover(HwABSStateMachine.SIM_CARD_STATE_SISO);
                    HwABSStateMachine.this.transitionTo(HwABSStateMachine.this.mSisoState);
                    break;
                case MessageUtil.MSG_WIFI_P2P_CONNECTED /*14*/:
                    HwABSUtils.logE("Mimo MSG_MODEM_EXIT_SEARCHING_STATE mModemStateList.size() == " + HwABSStateMachine.this.mModemStateList.size());
                    if (HwABSStateMachine.this.mModemStateList.size() != 0) {
                        this.mData = message.getData();
                        this.mSubId = this.mData.getInt(HwABSUtils.SUB_ID);
                        if (HwABSStateMachine.this.removeModemState(this.mSubId) == 0) {
                            HwABSStateMachine.this.ANTENNA_STATE_IN_SEARCH = false;
                            break;
                        }
                    }
                    break;
                case MessageUtil.MSG_WIFI_P2P_DISCONNECTED /*15*/:
                    HwABSUtils.logE("MimoState MSG_WIFI_ANTENNA_PREEMPTED");
                    HwABSStateMachine.this.hwABSWiFiHandover(HwABSStateMachine.SIM_CARD_STATE_SISO);
                    HwABSStateMachine.this.transitionTo(HwABSStateMachine.this.mSisoState);
                    break;
                case HwDualBandMessageUtil.CMD_STOP_MONITOR /*103*/:
                    HwABSStateMachine.this.mHwABSWiFiHandler.hwABSHandover(HwABSStateMachine.SIM_CARD_STATE_SISO);
                    HwABSStateMachine.this.transitionTo(HwABSStateMachine.this.mSisoState);
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            HwABSUtils.logD("exit MimoState");
            releaseAutoHandoverTimer();
            if (this.mCurrentBSSID != null) {
                HwABSStateMachine.this.updateConnectedTime(this.mCurrentBSSID, true, System.currentTimeMillis() - HwABSStateMachine.this.mABSMIMOStartTime);
            }
        }

        private void setAutoHandoverTimer() {
            HwABSUtils.logD("setAutoHandoverTimer");
            HwABSStateMachine.this.mAlarmManager.set(HwABSStateMachine.SIM_CARD_STATE_MIMO, SystemClock.elapsedRealtime() + 20000, HwABSUtils.TAG, HwABSStateMachine.this.mAutoHandoverTimerListener, HwABSStateMachine.this.getHandler());
        }

        private void releaseAutoHandoverTimer() {
            HwABSUtils.logD("releaseAutoHandoverTimer");
            HwABSStateMachine.this.mAlarmManager.cancel(HwABSStateMachine.this.mAutoHandoverTimerListener);
        }
    }

    class SisoState extends State {
        private String mCurrentBSSID;

        SisoState() {
            this.mCurrentBSSID = null;
        }

        public void enter() {
            HwABSUtils.logD("enter SisoState");
            HwABSStateMachine.this.setWiFiAntennaMonitor(true);
            if (HwABSStateMachine.this.isScreenOn() && HwABSStateMachine.this.isModemStateInIdle()) {
                HwABSStateMachine.this.mABSSISOStartTime = System.currentTimeMillis();
                HwABSStateMachine.this.sendHandoverToMIMOMsg(MessageUtil.CMD_ON_STOP, HwABSStateMachine.ABS_SCREEN_ON_TIME);
            } else {
                HwABSStateMachine.this.mABSSISOStartTime = 0;
            }
            WifiInfo wifiInfo = HwABSStateMachine.this.mWifiManager.getConnectionInfo();
            if (wifiInfo != null && wifiInfo.getBSSID() != null) {
                this.mCurrentBSSID = wifiInfo.getBSSID();
                HwABSUtils.logD("SisoState mCurrentBSSID = " + this.mCurrentBSSID);
            }
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case HwABSStateMachine.SIM_CARD_STATE_SISO /*1*/:
                    HwABSUtils.logE("SiSOState MSG_WIFI_CONNECTED");
                    break;
                case MessageUtil.MSG_WIFI_FIND_TARGET /*5*/:
                    HwABSUtils.logE("SiSOState MSG_SCREEN_ON isModemStateInIdle = " + HwABSStateMachine.this.isModemStateInIdle());
                    if (HwABSStateMachine.this.isModemStateInIdle()) {
                        if (!HwABSStateMachine.this.isInPunishTime()) {
                            HwABSStateMachine.this.sendHandoverToMIMOMsg(MessageUtil.CMD_ON_STOP, HwABSStateMachine.ABS_SCREEN_ON_TIME);
                            break;
                        }
                        long mOverPunishTime = HwABSStateMachine.this.getPunishTime() - (System.currentTimeMillis() - HwABSStateMachine.this.ABS_LAST_HANDOVER_TIME);
                        HwABSUtils.logE("SiSOState MSG_SCREEN_ON inpunish time = " + mOverPunishTime);
                        if (mOverPunishTime <= HwABSStateMachine.ABS_SCREEN_ON_TIME) {
                            HwABSStateMachine.this.sendHandoverToMIMOMsg(MessageUtil.CMD_ON_STOP, HwABSStateMachine.ABS_SCREEN_ON_TIME);
                            break;
                        }
                        HwABSStateMachine.this.sendHandoverToMIMOMsg(MessageUtil.CMD_ON_STOP, mOverPunishTime);
                        break;
                    }
                    break;
                case MessageUtil.MSG_WIFI_DISABLEING /*6*/:
                    HwABSUtils.logE("SiSOState MSG_SCREEN_OFF");
                    HwABSStateMachine.this.removeMessages(MessageUtil.CMD_ON_STOP);
                    break;
                case MessageUtil.MSG_WIFI_CONFIG_CHANGED /*8*/:
                    HwABSUtils.logE("SisoState MSG_ANTENNA_STATE_IDLE ANTENNA_STATE_IN_CALL = " + HwABSStateMachine.this.ANTENNA_STATE_IN_CALL);
                    if (HwABSStateMachine.this.ANTENNA_STATE_IN_CALL) {
                        HwABSStateMachine.this.ANTENNA_STATE_IN_CALL = false;
                        HwABSStateMachine.this.handoverToMIMO();
                        break;
                    }
                    break;
                case MessageUtil.MSG_WIFI_INTERNET_CONNECTED /*11*/:
                    HwABSUtils.logE("SisoState MSG_MODEM_ENTER_CONNECT_STATE");
                    HwABSStateMachine.this.ANTENNA_STATE_IN_CONNECT = true;
                    break;
                case MessageUtil.MSG_WIFI_INTERNET_DISCONNECTED /*12*/:
                    HwABSUtils.logE("SisoState MSG_MODEM_EXIT_CONNECT_STATE ANTENNA_STATE_IN_CONNECT = " + HwABSStateMachine.this.ANTENNA_STATE_IN_CONNECT);
                    if (HwABSStateMachine.this.ANTENNA_STATE_IN_CONNECT) {
                        HwABSStateMachine.this.ANTENNA_STATE_IN_CONNECT = false;
                        HwABSStateMachine.this.handoverToMIMO();
                        break;
                    }
                    break;
                case MessageUtil.MSG_WIFI_IS_PORTAL /*13*/:
                    HwABSUtils.logE("SisoState MSG_MODEM_ENTER_SEARCHING_STATE");
                    HwABSStateMachine.this.ANTENNA_STATE_IN_SEARCH = true;
                    HwABSStateMachine.this.removeMessages(MessageUtil.CMD_ON_STOP);
                    HwABSStateMachine.this.addModemState(message.getData().getInt(HwABSUtils.SUB_ID));
                    break;
                case MessageUtil.MSG_WIFI_P2P_CONNECTED /*14*/:
                    HwABSUtils.logE("SisoState MSG_MODEM_EXIT_SEARCHING_STATE mModemStateList.size() ==" + HwABSStateMachine.this.mModemStateList.size());
                    if (HwABSStateMachine.this.mModemStateList.size() != 0) {
                        Bundle mData = message.getData();
                        int mSubId = mData.getInt(HwABSUtils.SUB_ID);
                        int mResult = mData.getInt(HwABSUtils.RES);
                        if (HwABSStateMachine.this.removeModemState(mSubId) == 0) {
                            HwABSStateMachine.this.ANTENNA_STATE_IN_SEARCH = false;
                        }
                        if (!HwABSStateMachine.this.isHaveSIMCard(mSubId)) {
                            if (!HwABSStateMachine.this.ANTENNA_STATE_IN_SEARCH && (mResult == 0 || mResult == HwABSStateMachine.SIM_CARD_STATE_SISO)) {
                                HwABSStateMachine.this.handoverToMIMO();
                                break;
                            }
                            HwABSUtils.logE("SisoState keep stay in siso, have no sim card ANTENNA_STATE_IN_SEARCH = " + HwABSStateMachine.this.ANTENNA_STATE_IN_SEARCH);
                            break;
                        } else if (mResult == 0 && !HwABSStateMachine.this.ANTENNA_STATE_IN_SEARCH) {
                            HwABSStateMachine.this.handoverToMIMO();
                            break;
                        } else {
                            HwABSUtils.logE("SisoState keep stay in siso, have sim card ANTENNA_STATE_IN_SEARCH = " + HwABSStateMachine.this.ANTENNA_STATE_IN_SEARCH);
                            break;
                        }
                    }
                    break;
                case MessageUtil.MSG_SCREEN_ON /*21*/:
                    HwABSStateMachine.this.handlePowerOffMessage();
                    if (HwABSStateMachine.this.isModemStateInIdle()) {
                        HwABSStateMachine.this.handoverToMIMO();
                        break;
                    }
                    break;
                case MessageUtil.CMD_ON_STOP /*101*/:
                    boolean isModemStateIdle = HwABSStateMachine.this.isModemStateInIdle();
                    boolean isSIMCardInService = HwABSStateMachine.this.isSIMCardStatusIdle();
                    HwABSUtils.logE("SiSOState CMD_WIFI_SWITCH_MIMO isModemStateInIdle = " + isModemStateIdle + " isSIMCardInService = " + isSIMCardInService);
                    if (!isModemStateIdle || !isSIMCardInService) {
                        HwABSUtils.logE("SiSOState CMD_WIFI_SWITCH_MIMO keep in SISO");
                        break;
                    }
                    HwABSStateMachine.this.hwABSWiFiHandover(HwABSStateMachine.SIM_CARD_STATE_MIMO);
                    HwABSStateMachine.this.transitionTo(HwABSStateMachine.this.mMimoState);
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            HwABSUtils.logD("exit SisoState");
            if (this.mCurrentBSSID != null && HwABSStateMachine.this.mABSSISOStartTime != 0) {
                HwABSStateMachine.this.updateConnectedTime(this.mCurrentBSSID, false, System.currentTimeMillis() - HwABSStateMachine.this.mABSSISOStartTime);
            }
        }
    }

    class WiFiConnectedState extends State {
        WiFiConnectedState() {
        }

        public void enter() {
            HwABSUtils.logD("enter WiFiConnectedState");
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case HwABSStateMachine.SIM_CARD_STATE_SISO /*1*/:
                    HwABSUtils.logE("WiFiConnectedState MSG_WIFI_CONNECTED");
                    WifiInfo mWifiInfo = HwABSStateMachine.this.mWifiManager.getConnectionInfo();
                    if (mWifiInfo != null && mWifiInfo.getBSSID() != null) {
                        if (!HwABSStateMachine.this.isAPSupportMIMOCapability(mWifiInfo.getBSSID()) || !ScanResult.is24GHz(mWifiInfo.getFrequency()) || HwABSStateMachine.this.isMobileAP()) {
                            HwABSUtils.logE(" It is a 5G AP or siso AP");
                            break;
                        }
                        if (HwABSStateMachine.this.mHwABSDataBaseManager.getApInfoByBssid(mWifiInfo.getBSSID()) == null) {
                            HwABSStateMachine.this.mHwABSDataBaseManager.addOrUpdateApInfos(initApInfoData(mWifiInfo));
                        }
                        HwABSUtils.logD("now capability = " + HwABSStateMachine.this.mHwABSWiFiHandler.getCurrentCapability());
                        if (!HwABSStateMachine.this.isUsingMIMOCapability() || !HwABSStateMachine.this.isSIMCardStatusIdle()) {
                            HwABSStateMachine.this.transitionTo(HwABSStateMachine.this.mSisoState);
                            break;
                        }
                        HwABSStateMachine.this.transitionTo(HwABSStateMachine.this.mMimoState);
                        break;
                    }
                    HwABSUtils.logE("WiFiConnectedState error ");
                    break;
                    break;
                case WifiProStatisticsManager.DUALBAND_MIX_AP_SATISFIED_COUNT /*16*/:
                    HwABSStateMachine.this.mHwABSWiFiHandler.hwABScheckLinked();
                    break;
                case WifiProStatisticsManager.DUALBAND_MIX_AP_DISAPPER_COUNT /*17*/:
                    HwABSUtils.logE("WiFiConnectedState MSG_WIFI_CHECK_LINK_SUCCESS");
                    if (!HwABSStateMachine.this.isUsingMIMOCapability()) {
                        HwABSStateMachine.this.transitionTo(HwABSStateMachine.this.mSisoState);
                        break;
                    }
                    HwABSStateMachine.this.transitionTo(HwABSStateMachine.this.mMimoState);
                    break;
                case WifiProStatisticsManager.DUALBAND_MIX_AP_INBLACK_LIST_COUNT /*18*/:
                    HwABSUtils.logE("WiFiConnectedState MSG_WIFI_CHECK_LINK_FAILED");
                    WifiInfo wifiInfo = HwABSStateMachine.this.mWifiManager.getConnectionInfo();
                    if (wifiInfo != null && wifiInfo.getBSSID() != null) {
                        HwABSApInfoData hwABSApInfoData = HwABSStateMachine.this.mHwABSDataBaseManager.getApInfoByBssid(wifiInfo.getBSSID());
                        if (hwABSApInfoData != null) {
                            hwABSApInfoData.mSwitch_siso_type = HwABSStateMachine.SIM_CARD_STATE_MIMO;
                            HwABSStateMachine.this.mHwABSDataBaseManager.addOrUpdateApInfos(hwABSApInfoData);
                            HwABSStateMachine.this.hwABSWiFiHandover(HwABSStateMachine.SIM_CARD_STATE_SISO);
                            break;
                        }
                    }
                    HwABSUtils.logE("MSG_WIFI_CHECK_LINK_FAILED error ");
                    break;
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            HwABSUtils.logD("exit WiFiConnectedState");
        }

        private HwABSApInfoData initApInfoData(WifiInfo wifiInfo) {
            WifiConfiguration sWifiConfiguration = getCurrntConfig(wifiInfo);
            if (sWifiConfiguration != null) {
                return new HwABSApInfoData(wifiInfo.getBSSID(), wifiInfo.getSSID(), HwABSStateMachine.SIM_CARD_STATE_MIMO, HwABSStateMachine.SIM_CARD_STATE_MIMO, sWifiConfiguration.getAuthType(), 0, 0, 0, 0, 0);
            }
            HwABSUtils.logD("initApInfoData error");
            return null;
        }

        private WifiConfiguration getCurrntConfig(WifiInfo wifiInfo) {
            List<WifiConfiguration> configNetworks = HwABSStateMachine.this.mWifiManager.getConfiguredNetworks();
            if (configNetworks == null || configNetworks.size() == 0) {
                return null;
            }
            for (WifiConfiguration nextConfig : configNetworks) {
                if (isValidConfig(nextConfig) && nextConfig.networkId == wifiInfo.getNetworkId()) {
                    return nextConfig;
                }
            }
            return null;
        }

        private boolean isValidConfig(WifiConfiguration config) {
            boolean z = true;
            if (config == null || config.SSID == null) {
                return false;
            }
            if (config.allowedKeyManagement.cardinality() > HwABSStateMachine.SIM_CARD_STATE_SISO) {
                z = false;
            }
            return z;
        }
    }

    class WiFiDisableState extends State {
        WiFiDisableState() {
        }

        public void enter() {
            HwABSUtils.logD("enter WiFiDisableState ABS_HANDOVER_TIMES = " + HwABSStateMachine.this.ABS_HANDOVER_TIMES);
            HwABSStateMachine.this.ABS_HANDOVER_TIMES = 0;
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case MessageUtil.MSG_WIFI_DISABLE /*4*/:
                    HwABSUtils.logD("WiFiDisableState WiFiDisableState");
                    return true;
                default:
                    HwABSUtils.logD("WiFiDisableState message.what = " + message.what);
                    return false;
            }
        }

        public void exit() {
            HwABSUtils.logD("exit WiFiDisableState");
        }
    }

    static class WiFiDisconnectedState extends State {
        WiFiDisconnectedState() {
        }

        public void enter() {
            HwABSUtils.logD("enter WiFiDisconnectedState");
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case HwABSStateMachine.SIM_CARD_STATE_MIMO /*2*/:
                    HwABSUtils.logD("WiFiDisconnectedState MSG_WIFI_DISCONNECTED");
                    return true;
                case MessageUtil.MSG_WIFI_DISABLE /*4*/:
                    HwABSUtils.logD("WiFiDisconnectedState MSG_WIFI_DISABLE");
                    return false;
                default:
                    return false;
            }
        }

        public void exit() {
            HwABSUtils.logD("exit WiFiDisconnectedState");
        }
    }

    class WiFiEnableState extends State {
        WiFiEnableState() {
        }

        public void enter() {
            HwABSUtils.logD("enter WiFiEnableState");
            HwABSStateMachine.this.resetCapablity(HwABSStateMachine.this.mHwABSWiFiHandler.getCurrentCapability());
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case MessageUtil.MSG_WIFI_ENABLED /*3*/:
                    HwABSUtils.logD("WiFiEnableState MSG_WIFI_ENABLED");
                    return true;
                case MessageUtil.MSG_WIFI_DISABLE /*4*/:
                    HwABSUtils.logD("WiFiDisconnectedState MSG_WIFI_DISABLE");
                    return false;
                default:
                    return false;
            }
        }

        public void exit() {
            HwABSUtils.logD("exit WiFiEnableState");
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.ABS.HwABSStateMachine.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.ABS.HwABSStateMachine.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.ABS.HwABSStateMachine.<clinit>():void");
    }

    public static HwABSStateMachine createHwABSStateMachine(Context context, WifiStateMachine wifiStateMachine) {
        if (mHwABSStateMachine == null) {
            mHwABSStateMachine = new HwABSStateMachine(context, wifiStateMachine);
        }
        return mHwABSStateMachine;
    }

    private HwABSStateMachine(Context context, WifiStateMachine wifiStateMachine) {
        super("HwABSStateMachine");
        this.isSwitching = false;
        this.ANTENNA_STATE_IN_CALL = false;
        this.ANTENNA_STATE_IN_SEARCH = false;
        this.ANTENNA_STATE_IN_CONNECT = false;
        this.ABS_HANDOVER_TIMES = 0;
        this.ABS_LAST_HANDOVER_TIME = 0;
        this.mABSMIMOStartTime = 0;
        this.mABSSISOStartTime = 0;
        this.mModemStateList = new ArrayList();
        this.mDefaultState = new DefaultState();
        this.mWiFiEnableState = new WiFiEnableState();
        this.mWiFiDisableState = new WiFiDisableState();
        this.mWiFiConnectedState = new WiFiConnectedState();
        this.mWiFiDisconnectedState = new WiFiDisconnectedState();
        this.mMimoState = new MimoState();
        this.mSisoState = new SisoState();
        this.mAutoHandoverTimerListener = new OnAlarmListener() {
            public void onAlarm() {
                HwABSUtils.logD("mAutoHandoverTimerListener");
                if (HwABSStateMachine.this.mHandler != null) {
                    HwABSStateMachine.this.mHandler.sendEmptyMessage(HwDualBandMessageUtil.CMD_STOP_MONITOR);
                }
            }
        };
        this.mContext = context;
        this.mHandler = getHandler();
        this.mHwABSDataBaseManager = HwABSDataBaseManager.getInstance(context);
        this.mHwABSWiFiScenario = new HwABSWiFiScenario(context, getHandler());
        this.mHwABSModemScenario = new HwABSModemScenario(context, getHandler());
        this.mHwABSWiFiHandler = new HwABSWiFiHandler(context, getHandler(), wifiStateMachine);
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        addState(this.mDefaultState);
        addState(this.mWiFiEnableState, this.mDefaultState);
        addState(this.mWiFiDisableState, this.mDefaultState);
        addState(this.mWiFiConnectedState, this.mWiFiEnableState);
        addState(this.mWiFiDisconnectedState, this.mWiFiEnableState);
        addState(this.mMimoState, this.mWiFiEnableState);
        addState(this.mSisoState, this.mWiFiEnableState);
        setInitialState(this.mDefaultState);
        start();
    }

    public void onStart() {
        this.mHwABSWiFiScenario.startMonitor();
    }

    public boolean isABSSwitching() {
        HwABSUtils.logE("isABSSwitching isSwitching = " + this.isSwitching);
        return this.isSwitching;
    }

    private void updateConnectedTime(String bssid, boolean isMIMO, long addTime) {
        HwABSUtils.logE("updateConnectedTime isMIMO = " + isMIMO + " addTime = " + addTime);
        if (bssid == null) {
            HwABSUtils.logE("updateConnectedTime bssid == null");
        }
        HwABSApInfoData hwABSApInfoData = this.mHwABSDataBaseManager.getApInfoByBssid(bssid);
        if (hwABSApInfoData != null) {
            if (isMIMO) {
                hwABSApInfoData.mMimo_time += addTime;
            } else {
                hwABSApInfoData.mSiso_time += addTime;
            }
            hwABSApInfoData.mTotal_time += addTime;
            this.mHwABSDataBaseManager.addOrUpdateApInfos(hwABSApInfoData);
            return;
        }
        HwABSUtils.logE("updateConnectedTime error!!");
    }

    private NetworkDetail getNetWorkDetail(String bssid) {
        NetworkDetail detail = null;
        for (ScanResult result : this.mWifiManager.getScanResults()) {
            if (result.BSSID.equals(bssid)) {
                detail = ScanDetailUtil.toScanDetail(result).getNetworkDetail();
            }
        }
        return detail;
    }

    private boolean isUsingMIMOCapability() {
        if (this.mHwABSWiFiHandler.getCurrentCapability() == SIM_CARD_STATE_MIMO) {
            return true;
        }
        return false;
    }

    private boolean isAPSupportMIMOCapability(String bssid) {
        NetworkDetail mNetworkDetail = getNetWorkDetail(bssid);
        if (mNetworkDetail != null) {
            HwABSUtils.logD("isAPSupportMIMOCapability mNetworkDetail.getStream1() = " + mNetworkDetail.getStream1() + " mNetworkDetail.getStream2() = " + mNetworkDetail.getStream2() + " mNetworkDetail.getStream3() = " + mNetworkDetail.getStream3() + " mNetworkDetail.getStream4() = " + mNetworkDetail.getStream4());
            if (((mNetworkDetail.getStream1() + mNetworkDetail.getStream2()) + mNetworkDetail.getStream3()) + mNetworkDetail.getStream4() >= SIM_CARD_STATE_MIMO) {
                return true;
            }
        }
        return false;
    }

    private void hwABSWiFiHandover(int capability) {
        HwABSUtils.logD("hwABSWiFiHandover capability = " + capability);
        if (capability == SIM_CARD_STATE_SISO) {
            setPunishTime();
        }
        this.mHwABSWiFiHandler.hwABSHandover(capability);
    }

    private void setPunishTime() {
        if (this.ABS_LAST_HANDOVER_TIME == 0 || System.currentTimeMillis() - this.ABS_LAST_HANDOVER_TIME > ABS_INTERVAL_TIME) {
            HwABSUtils.logD("setPunishTime reset times");
            this.ABS_HANDOVER_TIMES = 0;
        } else {
            this.ABS_HANDOVER_TIMES += SIM_CARD_STATE_SISO;
        }
        this.ABS_LAST_HANDOVER_TIME = System.currentTimeMillis();
    }

    private boolean isHaveSIMCard(int subID) {
        int cardState = this.mTelephonyManager.getSimState(subID);
        if (cardState == 5) {
            HwABSUtils.logD("isHaveSIMCard subID = " + subID + "  cardState = SIM_STATE_READY");
            return true;
        }
        HwABSUtils.logD("isHaveSIMCard subID = " + subID + "  cardState = " + cardState);
        return false;
    }

    private boolean isHaveSIMCard() {
        int phoneNum = this.mTelephonyManager.getPhoneCount();
        for (int i = 0; i < phoneNum; i += SIM_CARD_STATE_SISO) {
            if (this.mTelephonyManager.getSimState(i) == 5) {
                return true;
            }
        }
        return false;
    }

    private boolean isSIMCardStatusIdle() {
        boolean isCardReady = false;
        int phoneNum = this.mTelephonyManager.getPhoneCount();
        HwABSUtils.logD("isSIMCardStatusIdle phoneNum = " + phoneNum);
        if (phoneNum == 0) {
            return true;
        }
        for (int i = 0; i < phoneNum; i += SIM_CARD_STATE_SISO) {
            if (this.mTelephonyManager.getSimState(i) == 5) {
                isCardReady = true;
                break;
            }
        }
        if (isCardReady) {
            return compareSIMStatusWithCardReady(phoneNum);
        }
        HwABSUtils.logD("isSIMCardStatusIdle return true");
        return true;
    }

    private boolean compareSIMStatusWithCardReady(int cardNum) {
        List<Integer> statusList = new ArrayList();
        if (cardNum == 0) {
            return true;
        }
        for (int subId = 0; subId < cardNum; subId += SIM_CARD_STATE_SISO) {
            int cardState = this.mTelephonyManager.getSimState(subId);
            HwABSUtils.logD("compareSIMStatusWithCardReady subId = " + subId + " cardState = " + cardState);
            if (cardState == 5) {
                ServiceState serviceState = this.mTelephonyManager.getServiceStateForSubscriber(subId);
                if (serviceState != null) {
                    int voiceState = serviceState.getState();
                    HwABSUtils.logD("compareSIMStatusWithCardReady subId = " + subId + " voiceState = " + voiceState);
                    switch (voiceState) {
                        case MessageUtil.SWITCH_TO_WIFI_AUTO /*0*/:
                        case MessageUtil.MSG_WIFI_ENABLED /*3*/:
                            statusList.add(Integer.valueOf(SIM_CARD_STATE_MIMO));
                            break;
                        default:
                            statusList.add(Integer.valueOf(SIM_CARD_STATE_SISO));
                            break;
                    }
                }
                statusList.add(Integer.valueOf(SIM_CARD_STATE_SISO));
            } else {
                statusList.add(Integer.valueOf(SIM_CARD_STATE_MIMO));
            }
        }
        for (int i = 0; i < statusList.size(); i += SIM_CARD_STATE_SISO) {
            if (((Integer) statusList.get(i)).intValue() != SIM_CARD_STATE_MIMO) {
                HwABSUtils.logD("compareSIMStatusWithCardReady return false");
                return false;
            }
        }
        HwABSUtils.logD("compareSIMStatusWithCardReady return true");
        return true;
    }

    private void setWiFiAntennaMonitor(boolean enable) {
        if (enable) {
            HwABSUtils.logD("setWiFiAntennaMonitor enable");
        } else {
            HwABSUtils.logD("setWiFiAntennaMonitor disable");
        }
    }

    private boolean isScreenOn() {
        if (((PowerManager) this.mContext.getSystemService("power")).isScreenOn()) {
            return true;
        }
        return false;
    }

    private void resetCapablity(int capablity) {
        HwABSUtils.logD("resetCapablity capablity = " + capablity);
        if (capablity != SIM_CARD_STATE_MIMO) {
            this.mHwABSWiFiHandler.setAPCapability(capablity);
        } else if (isModemStateInIdle() && !isInPunishTime()) {
            this.mHwABSWiFiHandler.setAPCapability(capablity);
        }
    }

    private boolean isModemStateInIdle() {
        if (this.ANTENNA_STATE_IN_CALL || this.ANTENNA_STATE_IN_SEARCH || this.ANTENNA_STATE_IN_CONNECT || !isScreenOn()) {
            HwABSUtils.logD("isModemStateInIdle return false ANTENNA_STATE_IN_CALL = " + this.ANTENNA_STATE_IN_CALL + "  ANTENNA_STATE_IN_SEARCH = " + this.ANTENNA_STATE_IN_SEARCH + "  ANTENNA_STATE_IN_CONNECT = " + this.ANTENNA_STATE_IN_CONNECT + " isScreenOn() = " + isScreenOn());
            return false;
        }
        HwABSUtils.logD("isModemStateInIdle return true");
        return true;
    }

    private void addModemState(int subId) {
        if (this.mModemStateList.size() == 0) {
            this.mModemStateList.add(Integer.valueOf(subId));
        } else {
            int i = 0;
            while (i < this.mModemStateList.size()) {
                if (((Integer) this.mModemStateList.get(i)).intValue() != subId) {
                    i += SIM_CARD_STATE_SISO;
                } else {
                    return;
                }
            }
            this.mModemStateList.add(Integer.valueOf(subId));
        }
        HwABSUtils.logD("addModemState size = " + this.mModemStateList.size());
    }

    private int removeModemState(int subId) {
        HwABSUtils.logD("removeModemState size = " + this.mModemStateList.size());
        if (this.mModemStateList.size() == 0) {
            return 0;
        }
        int flag = -1;
        for (int i = 0; i < this.mModemStateList.size(); i += SIM_CARD_STATE_SISO) {
            HwABSUtils.logD("removeModemState mModemStateList.get(i) = " + this.mModemStateList.get(i) + " subId = " + subId);
            if (((Integer) this.mModemStateList.get(i)).intValue() == subId) {
                flag = i;
                break;
            }
        }
        if (flag != -1) {
            this.mModemStateList.remove(flag);
        }
        HwABSUtils.logD("removeModemState size = " + this.mModemStateList.size());
        return this.mModemStateList.size();
    }

    private boolean isInPunishTime() {
        long sPunishTim = getPunishTime();
        if (System.currentTimeMillis() - this.ABS_LAST_HANDOVER_TIME > sPunishTim) {
            HwABSUtils.logD("isInPunishTime is in not in punish");
            return false;
        }
        HwABSUtils.logD("isInPunishTime is in punish  sPunishTim =" + ((this.ABS_LAST_HANDOVER_TIME + sPunishTim) - System.currentTimeMillis()));
        return true;
    }

    private long getPunishTime() {
        long sPunishTim = ((long) this.ABS_HANDOVER_TIMES) * ABS_PUNISH_TIME;
        if (sPunishTim > ABS_INTERVAL_TIME) {
            return ABS_INTERVAL_TIME;
        }
        return sPunishTim;
    }

    private void handoverToMIMO() {
        HwABSUtils.logD("handoverToMIMO");
        if (isInPunishTime()) {
            long mOverPunishTime = getPunishTime() - (System.currentTimeMillis() - this.ABS_LAST_HANDOVER_TIME);
            HwABSUtils.logD("handoverToMIMO mOverPunishTime = " + mOverPunishTime);
            sendHandoverToMIMOMsg(MessageUtil.CMD_ON_STOP, mOverPunishTime);
            return;
        }
        sendHandoverToMIMOMsg(MessageUtil.CMD_ON_STOP, 2000);
    }

    private void sendHandoverToMIMOMsg(int msg, long time) {
        if (hasMessages(msg)) {
            removeMessages(msg);
        }
        sendMessageDelayed(msg, time);
    }

    private boolean isAirModeOn() {
        boolean z = true;
        if (this.mContext == null) {
            return false;
        }
        if (System.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) != SIM_CARD_STATE_SISO) {
            z = false;
        }
        return z;
    }

    private List<Integer> getPowerOffSIMSubId() {
        List<Integer> subId = new ArrayList();
        int phoneNum = this.mTelephonyManager.getPhoneCount();
        HwABSUtils.logD("getPowerOffSIMSubId phoneNum = " + phoneNum);
        if (phoneNum == 0) {
            return subId;
        }
        for (int i = 0; i < phoneNum; i += SIM_CARD_STATE_SISO) {
            ServiceState serviceState = this.mTelephonyManager.getServiceStateForSubscriber(i);
            if (serviceState != null) {
                int voiceState = serviceState.getState();
                HwABSUtils.logD("getPowerOffSIMSubId subID = " + i + " voiceState = " + voiceState);
                if (voiceState == 3) {
                    subId.add(Integer.valueOf(i));
                }
            }
        }
        return subId;
    }

    private void handlePowerOffMessage() {
        if (this.ANTENNA_STATE_IN_SEARCH) {
            List<Integer> list = getPowerOffSIMSubId();
            if (list.size() != 0) {
                for (Integer intValue : list) {
                    removeModemState(intValue.intValue());
                }
                if (this.mModemStateList.size() == 0) {
                    this.ANTENNA_STATE_IN_SEARCH = false;
                }
            }
        }
        if (this.ANTENNA_STATE_IN_CONNECT) {
            this.ANTENNA_STATE_IN_CONNECT = false;
        }
    }

    private boolean isMobileAP() {
        if (this.mContext != null) {
            return HwFrameworkFactory.getHwInnerWifiManager().getHwMeteredHint(this.mContext);
        }
        return false;
    }
}
