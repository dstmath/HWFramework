package com.android.server.wifi.ABS;

import android.common.HwFrameworkFactory;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings.System;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.PhoneCallback;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import com.android.ims.ImsManager;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.wifi.WifiStateMachine;
import com.android.server.wifi.hotspot2.NetworkDetail;
import com.android.server.wifi.util.ScanResultUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HwABSStateMachine extends StateMachine {
    private static final long ABS_INTERVAL_TIME = 1800000;
    private static final long ABS_PUNISH_TIME = 30000;
    private static final long ABS_SCREEN_ON_TIME = 10000;
    private static final String ACTION_ABS_HANDOVER_TIMER = "android.net.wifi.abs_handover_timer";
    private static final int SIM_CARD_STATE_MIMO = 2;
    private static final int SIM_CARD_STATE_SISO = 1;
    private static HwABSStateMachine mHwABSStateMachine = null;
    private int ABS_HANDOVER_TIMES = 0;
    private long ABS_LAST_HANDOVER_TIME = 0;
    private boolean ANTENNA_STATE_IN_CALL = false;
    private boolean ANTENNA_STATE_IN_CONNECT = false;
    private boolean ANTENNA_STATE_IN_PREEMPTED = false;
    private boolean ANTENNA_STATE_IN_SEARCH = false;
    private int MODEM_TUNERIC_ACTIVE = 1;
    private int MODEM_TUNERIC_IACTIVE = 0;
    private int RESENT_MODEM_TUNERIC_ACTIVE_TIMES = 0;
    private int RESENT_MODEM_TUNERIC_IACTIVE_TIMES = 0;
    private boolean isSwitching = false;
    private long mABSMIMOScreenOnStartTime = 0;
    private long mABSMIMOStartTime = 0;
    private long mABSSISOScreenOnStartTime = 0;
    private long mABSSISOStartTime = 0;
    private PhoneCallback mActiveCallback = new PhoneCallback() {
        public void onPhoneCallback1(int parm) {
            HwABSStateMachine.this.sendMessage(33, parm);
        }
    };
    private int mAddBlackListReason = 0;
    private String mAssociateBSSID = null;
    private String mAssociateSSID = null;
    private Context mContext;
    private State mDefaultState = new DefaultState();
    private HwABSCHRManager mHwABSCHRManager;
    private HwABSDataBaseManager mHwABSDataBaseManager;
    private HwABSWiFiHandler mHwABSWiFiHandler;
    private HwABSWiFiScenario mHwABSWiFiScenario;
    private PhoneCallback mIactiveCallback = new PhoneCallback() {
        public void onPhoneCallback1(int parm) {
            HwABSStateMachine.this.sendMessage(35, parm);
        }
    };
    private boolean mIsInCallPunish = false;
    private boolean mIsSupportVoWIFI = false;
    private State mMimoState = new MimoState();
    private List<Integer> mModemStateList = new ArrayList();
    private State mSisoState = new SisoState();
    private int mSwitchEvent = 0;
    private int mSwitchType = 0;
    private TelephonyManager mTelephonyManager;
    private State mWiFiConnectedState = new WiFiConnectedState();
    private State mWiFiDisableState = new WiFiDisableState();
    private State mWiFiDisconnectedState = new WiFiDisconnectedState();
    private State mWiFiEnableState = new WiFiEnableState();
    private WifiManager mWifiManager;

    class DefaultState extends State {
        Bundle mData = null;
        int mSubId = -1;

        DefaultState() {
        }

        public boolean processMessage(Message message) {
            HwABSStateMachine hwABSStateMachine;
            switch (message.what) {
                case 1:
                    HwABSUtils.logD("DefaultState MSG_WIFI_CONNECTED");
                    HwABSStateMachine.this.transitionTo(HwABSStateMachine.this.mWiFiConnectedState);
                    HwABSStateMachine.this.sendMessage(1);
                    break;
                case 2:
                    HwABSUtils.logD("DefaultState MSG_WIFI_DISCONNECTED");
                    HwABSStateMachine.this.transitionTo(HwABSStateMachine.this.mWiFiDisconnectedState);
                    break;
                case 3:
                    HwABSUtils.logD("DefaultState MSG_WIFI_ENABLED");
                    HwABSStateMachine.this.transitionTo(HwABSStateMachine.this.mWiFiEnableState);
                    break;
                case 4:
                    HwABSUtils.logD("DefaultState MSG_WIFI_DISABLE");
                    HwABSStateMachine.this.transitionTo(HwABSStateMachine.this.mWiFiDisableState);
                    break;
                case 7:
                    HwABSUtils.logD("DefaultState MSG_OUTGOING_CALL");
                    HwABSStateMachine.this.ANTENNA_STATE_IN_CALL = true;
                    HwABSStateMachine.this.resetCapablity(1);
                    break;
                case 8:
                    HwABSUtils.logD("DefaultState MSG_CALL_STATE_IDLE");
                    HwABSStateMachine.this.ANTENNA_STATE_IN_CALL = false;
                    HwABSStateMachine.this.resetCapablity(2);
                    break;
                case 9:
                    HwABSUtils.logD("DefaultState MSG_CALL_STATE_RINGING");
                    HwABSStateMachine.this.ANTENNA_STATE_IN_CALL = true;
                    HwABSStateMachine.this.resetCapablity(1);
                    break;
                case 11:
                case 12:
                    HwABSUtils.logD("DefaultState MSG_MODEM_ENTER_CONNECT_STATE");
                    HwABSStateMachine.this.ANTENNA_STATE_IN_CONNECT = true;
                    HwABSStateMachine.this.resetCapablity(1);
                    break;
                case 13:
                    HwABSUtils.logD("DefaultState MSG_MODEM_EXIT_CONNECT_STATE");
                    if (HwABSStateMachine.this.ANTENNA_STATE_IN_CONNECT) {
                        HwABSStateMachine.this.ANTENNA_STATE_IN_CONNECT = false;
                        HwABSStateMachine.this.resetCapablity(2);
                        break;
                    }
                    break;
                case 14:
                    HwABSUtils.logD("DefaultState MSG_MODEM_ENTER_SEARCHING_STATE");
                    HwABSStateMachine.this.ANTENNA_STATE_IN_SEARCH = true;
                    HwABSStateMachine.this.resetCapablity(1);
                    this.mData = message.getData();
                    this.mSubId = this.mData.getInt(HwABSUtils.SUB_ID);
                    HwABSStateMachine.this.addModemState(this.mSubId);
                    break;
                case 15:
                    HwABSUtils.logD("DefaultState MSG_MODEM_EXIT_SEARCHING_STATE");
                    this.mData = message.getData();
                    this.mSubId = this.mData.getInt(HwABSUtils.SUB_ID);
                    if (HwABSStateMachine.this.removeModemState(this.mSubId) == 0) {
                        HwABSStateMachine.this.ANTENNA_STATE_IN_SEARCH = false;
                        HwABSStateMachine.this.resetCapablity(2);
                        break;
                    }
                    break;
                case 16:
                    HwABSUtils.logD("DefaultState MSG_WIFI_ANTENNA_PREEMPTED");
                    break;
                case 22:
                    HwABSStateMachine.this.handlePowerOffMessage();
                    if (HwABSStateMachine.this.isModemStateInIdle()) {
                        HwABSStateMachine.this.resetCapablity(2);
                        break;
                    }
                    break;
                case 25:
                    HwABSStateMachine.this.mIsSupportVoWIFI = ImsManager.isWfcEnabledByPlatform(HwABSStateMachine.this.mContext);
                    HwABSUtils.logD("DefaultState mIsSupportVoWIFI = " + HwABSStateMachine.this.mIsSupportVoWIFI);
                    break;
                case HwABSUtils.MSG_MODEM_TUNERIC_ACTIVE_RESULT /*33*/:
                    int active_result = message.arg1;
                    HwABSUtils.logD("DefaultState MSG_MODEM_TUNERIC_ACTIVE_RESULT active_result = " + active_result + "  RESENT_MODEM_TUNERIC_ACTIVE_TIMES = " + HwABSStateMachine.this.RESENT_MODEM_TUNERIC_ACTIVE_TIMES);
                    if (active_result != 1) {
                        if (HwABSStateMachine.this.mWifiManager.isWifiEnabled() && HwABSStateMachine.this.RESENT_MODEM_TUNERIC_ACTIVE_TIMES < 3) {
                            HwABSStateMachine.this.removeMessages(34);
                            HwABSStateMachine.this.sendMessageDelayed(34, 5000);
                            break;
                        }
                    }
                    HwABSStateMachine.this.RESENT_MODEM_TUNERIC_ACTIVE_TIMES = 0;
                    break;
                case 34:
                    HwABSUtils.logD("DefaultState MSG_RESEND_TUNERIC_ACTIVE_MSG");
                    if (HwABSStateMachine.this.mWifiManager.isWifiEnabled()) {
                        HwTelephonyManagerInner.getDefault().notifyCModemStatus(HwABSStateMachine.this.MODEM_TUNERIC_ACTIVE, HwABSStateMachine.this.mActiveCallback);
                        hwABSStateMachine = HwABSStateMachine.this;
                        hwABSStateMachine.RESENT_MODEM_TUNERIC_ACTIVE_TIMES = hwABSStateMachine.RESENT_MODEM_TUNERIC_ACTIVE_TIMES + 1;
                        break;
                    }
                    break;
                case HwABSUtils.MSG_MODEM_TUNERIC_IACTIVE_RESULT /*35*/:
                    int iactive_result = message.arg1;
                    HwABSUtils.logD("DefaultState MSG_MODEM_TUNERIC_IACTIVE_RESULT iactive_result = " + iactive_result + "  RESENT_MODEM_TUNERIC_IACTIVE_TIMES = " + HwABSStateMachine.this.RESENT_MODEM_TUNERIC_IACTIVE_TIMES);
                    if (iactive_result != 1) {
                        if (!HwABSStateMachine.this.mWifiManager.isWifiEnabled() && HwABSStateMachine.this.RESENT_MODEM_TUNERIC_IACTIVE_TIMES < 3) {
                            HwABSStateMachine.this.removeMessages(34);
                            HwABSStateMachine.this.sendMessageDelayed(36, 5000);
                            break;
                        }
                    }
                    HwABSStateMachine.this.RESENT_MODEM_TUNERIC_IACTIVE_TIMES = 0;
                    break;
                case HwABSUtils.MSG_RESEND_TUNERIC_IACTIVE_MSG /*36*/:
                    HwABSUtils.logD("DefaultState MSG_RESEND_TUNERIC_IACTIVE_MSG");
                    if (!HwABSStateMachine.this.mWifiManager.isWifiEnabled()) {
                        HwTelephonyManagerInner.getDefault().notifyCModemStatus(HwABSStateMachine.this.MODEM_TUNERIC_IACTIVE, HwABSStateMachine.this.mIactiveCallback);
                        hwABSStateMachine = HwABSStateMachine.this;
                        hwABSStateMachine.RESENT_MODEM_TUNERIC_IACTIVE_TIMES = hwABSStateMachine.RESENT_MODEM_TUNERIC_IACTIVE_TIMES + 1;
                        break;
                    }
                    break;
                case HwABSUtils.MSG_BOOT_COMPLETED /*37*/:
                    if (!HwABSStateMachine.this.mWifiManager.isWifiEnabled()) {
                        HwABSUtils.logD("DefaultState send MODEM_TUNERIC_IACTIVE_MSG");
                        HwTelephonyManagerInner.getDefault().notifyCModemStatus(HwABSStateMachine.this.MODEM_TUNERIC_IACTIVE, HwABSStateMachine.this.mIactiveCallback);
                        HwABSStateMachine.this.RESENT_MODEM_TUNERIC_IACTIVE_TIMES = 0;
                        break;
                    }
                    HwABSUtils.logD("DefaultState send MODEM_TUNERIC_ACTIVE_MSG");
                    HwTelephonyManagerInner.getDefault().notifyCModemStatus(HwABSStateMachine.this.MODEM_TUNERIC_ACTIVE, HwABSStateMachine.this.mActiveCallback);
                    HwABSStateMachine.this.RESENT_MODEM_TUNERIC_ACTIVE_TIMES = 0;
                    HwABSStateMachine.this.mHwABSWiFiHandler.setAPCapability(HwABSStateMachine.this.mHwABSWiFiHandler.getCurrentCapability());
                    HwABSStateMachine.this.setBlackListBssid();
                    break;
                case HwABSUtils.MSG_SEL_ENGINE_RESET_COMPLETED /*38*/:
                    HwABSUtils.logD("DefaultState MSG_SEL_ENGINE_RESET_COMPLETED");
                    HwABSStateMachine.this.transitionTo(HwABSStateMachine.this.mWiFiConnectedState);
                    HwABSStateMachine.this.sendMessage(1);
                    break;
            }
            return true;
        }
    }

    class MimoState extends State {
        private String mCurrentBSSID = null;
        private String mCurrentSSID = null;
        Bundle mData = null;
        int mSubId = -1;

        MimoState() {
        }

        public void enter() {
            HwABSUtils.logD("enter MimoState");
            HwABSStateMachine.this.setWiFiAntennaMonitor(true);
            HwABSStateMachine.this.mABSMIMOStartTime = System.currentTimeMillis();
            if (HwABSStateMachine.this.isScreenOn()) {
                HwABSStateMachine.this.mABSMIMOScreenOnStartTime = System.currentTimeMillis();
            }
            WifiInfo wifiInfo = HwABSStateMachine.this.mWifiManager.getConnectionInfo();
            if (wifiInfo != null && wifiInfo.getSSID() != null && wifiInfo.getBSSID() != null) {
                HwABSUtils.logD("MimoState bssid = " + wifiInfo.getBSSID() + " ssid = " + wifiInfo.getSSID());
                this.mCurrentSSID = HwABSCHRManager.getAPSSID(wifiInfo);
                this.mCurrentBSSID = wifiInfo.getBSSID();
            }
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case 1:
                    HwABSUtils.logE("MimoState MSG_WIFI_CONNECTED mIsSupportVoWIFI = " + HwABSStateMachine.this.mIsSupportVoWIFI);
                    if (HwABSStateMachine.this.mHwABSWiFiHandler.getIsABSHandover()) {
                        if (HwABSStateMachine.this.mIsSupportVoWIFI && HwABSStateMachine.this.mHwABSWiFiHandler.isHandoverTimeout()) {
                            HwABSUtils.logE("MimoState MSG_WIFI_CONNECTED handover timeout");
                            HwABSStateMachine.this.updateABSAssociateTimes(0, 1);
                        } else {
                            HwABSStateMachine.this.updateABSAssociateSuccess();
                        }
                        HwABSStateMachine.this.mHwABSWiFiHandler.setIsABSHandover(false);
                        break;
                    }
                    break;
                case 2:
                    HwABSUtils.logD("MimoState MSG_WIFI_DISCONNECTED");
                    if (HwABSStateMachine.this.mHwABSWiFiHandler.getIsABSHandover()) {
                        HwABSStateMachine.this.mHwABSWiFiHandler.setIsABSHandover(false);
                        HwABSStateMachine.this.mHwABSCHRManager.uploadABSReassociateExeption();
                        HwABSStateMachine.this.updateABSAssociateTimes(0, 1);
                    }
                    HwABSStateMachine.this.transitionTo(HwABSStateMachine.this.mWiFiDisconnectedState);
                    break;
                case 5:
                    HwABSUtils.logE("MimoState MSG_SCREEN_ON");
                    HwABSStateMachine.this.mABSMIMOScreenOnStartTime = System.currentTimeMillis();
                    break;
                case 6:
                    HwABSUtils.logE("MimoState MSG_SCREEN_OFF");
                    if (HwABSStateMachine.this.mABSMIMOScreenOnStartTime != 0) {
                        HwABSStateMachine.this.mHwABSCHRManager.updateABSTime(this.mCurrentSSID, 0, 0, System.currentTimeMillis() - HwABSStateMachine.this.mABSMIMOScreenOnStartTime, 0);
                        HwABSStateMachine.this.mABSMIMOScreenOnStartTime = 0;
                        break;
                    }
                    break;
                case 7:
                    HwABSUtils.logE("MimoState MSG_OUTGOING_CALL isAirModeOn =  " + HwABSStateMachine.this.isAirModeOn());
                    if (!HwABSStateMachine.this.isAirModeOn()) {
                        HwABSStateMachine.this.ANTENNA_STATE_IN_CALL = true;
                        if (HwABSStateMachine.this.mHwABSWiFiScenario.isSupInCompleteState() && !HwABSStateMachine.this.mHwABSWiFiHandler.getIsABSHandover()) {
                            if (HwABSStateMachine.this.mHwABSWiFiHandler.isNeedHandover()) {
                                HwABSStateMachine.this.updateABSAssociateTimes(1, 0);
                                HwABSStateMachine.this.mHwABSCHRManager.initABSHandoverException(7);
                                HwABSStateMachine.this.mHwABSCHRManager.increaseEventStatistics(6);
                                HwABSStateMachine.this.mHwABSWiFiHandler.hwABSHandover(1);
                            } else {
                                HwABSStateMachine.this.mHwABSWiFiHandler.setAPCapability(1);
                                HwABSStateMachine.this.mHwABSWiFiHandler.setABSCurrentState(1);
                            }
                            HwABSStateMachine.this.transitionTo(HwABSStateMachine.this.mSisoState);
                            break;
                        }
                        HwABSStateMachine.this.mSwitchType = 7;
                        HwABSStateMachine.this.mSwitchEvent = 6;
                        HwABSStateMachine.this.sendMessageDelayed(23, 1000);
                        break;
                    }
                    break;
                case 9:
                    HwABSUtils.logE("MimoState MSG_CALL_STATE_RINGING isAirModeOn =  " + HwABSStateMachine.this.isAirModeOn());
                    if (!HwABSStateMachine.this.isAirModeOn()) {
                        HwABSStateMachine.this.ANTENNA_STATE_IN_CALL = true;
                        if (HwABSStateMachine.this.mHwABSWiFiScenario.isSupInCompleteState() && !HwABSStateMachine.this.mHwABSWiFiHandler.getIsABSHandover()) {
                            if (HwABSStateMachine.this.mHwABSWiFiHandler.isNeedHandover()) {
                                HwABSStateMachine.this.updateABSAssociateTimes(1, 0);
                                HwABSStateMachine.this.mHwABSCHRManager.initABSHandoverException(6);
                                HwABSStateMachine.this.mHwABSCHRManager.increaseEventStatistics(6);
                                HwABSStateMachine.this.mHwABSWiFiHandler.hwABSHandover(1);
                            } else {
                                HwABSStateMachine.this.mHwABSWiFiHandler.setAPCapability(1);
                                HwABSStateMachine.this.mHwABSWiFiHandler.setABSCurrentState(1);
                            }
                            HwABSStateMachine.this.transitionTo(HwABSStateMachine.this.mSisoState);
                            break;
                        }
                        HwABSStateMachine.this.mSwitchType = 6;
                        HwABSStateMachine.this.mSwitchEvent = 6;
                        HwABSStateMachine.this.sendMessageDelayed(23, 1000);
                        break;
                    }
                    break;
                case 11:
                    HwABSUtils.logE("MimoState MSG_MODEM_ENTER_CONNECT_STATE");
                    HwABSStateMachine.this.ANTENNA_STATE_IN_CONNECT = true;
                    if (HwABSStateMachine.this.mHwABSWiFiScenario.isSupInCompleteState() && !HwABSStateMachine.this.mHwABSWiFiHandler.getIsABSHandover()) {
                        if (HwABSStateMachine.this.mHwABSWiFiHandler.isNeedHandover()) {
                            HwABSStateMachine.this.updateABSAssociateTimes(1, 0);
                            HwABSStateMachine.this.mHwABSCHRManager.initABSHandoverException(1);
                            HwABSStateMachine.this.mHwABSCHRManager.increaseEventStatistics(1);
                            HwABSStateMachine.this.hwABSWiFiHandover(1);
                        } else {
                            HwABSStateMachine.this.mHwABSWiFiHandler.setAPCapability(1);
                            HwABSStateMachine.this.mHwABSWiFiHandler.setABSCurrentState(1);
                        }
                        HwABSStateMachine.this.transitionTo(HwABSStateMachine.this.mSisoState);
                        break;
                    }
                    HwABSStateMachine.this.mSwitchType = 1;
                    HwABSStateMachine.this.mSwitchEvent = 1;
                    HwABSStateMachine.this.sendMessageDelayed(23, 1000);
                    break;
                    break;
                case 12:
                    HwABSUtils.logE("MimoState MSG_MODEM_ENTER_CONNECT_STATE");
                    HwABSStateMachine.this.ANTENNA_STATE_IN_CONNECT = true;
                    if (HwABSStateMachine.this.mHwABSWiFiScenario.isSupInCompleteState() && !HwABSStateMachine.this.mHwABSWiFiHandler.getIsABSHandover()) {
                        if (HwABSStateMachine.this.mHwABSWiFiHandler.isNeedHandover()) {
                            HwABSStateMachine.this.updateABSAssociateTimes(1, 0);
                            HwABSStateMachine.this.mHwABSCHRManager.initABSHandoverException(2);
                            HwABSStateMachine.this.mHwABSCHRManager.increaseEventStatistics(2);
                            HwABSStateMachine.this.hwABSWiFiHandover(1);
                        } else {
                            HwABSStateMachine.this.mHwABSWiFiHandler.setAPCapability(1);
                            HwABSStateMachine.this.mHwABSWiFiHandler.setABSCurrentState(1);
                        }
                        HwABSStateMachine.this.transitionTo(HwABSStateMachine.this.mSisoState);
                        break;
                    }
                    HwABSStateMachine.this.mSwitchType = 2;
                    HwABSStateMachine.this.mSwitchEvent = 2;
                    HwABSStateMachine.this.sendMessageDelayed(23, 1000);
                    break;
                case 13:
                    HwABSUtils.logE("MimoState MSG_MODEM_EXIT_CONNECT_STATE ANTENNA_STATE_IN_CONNECT = " + HwABSStateMachine.this.ANTENNA_STATE_IN_CONNECT);
                    if (HwABSStateMachine.this.ANTENNA_STATE_IN_CONNECT) {
                        HwABSStateMachine.this.ANTENNA_STATE_IN_CONNECT = false;
                        break;
                    }
                    break;
                case 14:
                    HwABSUtils.logE("MimoState MSG_MODEM_ENTER_SEARCHING_STATE");
                    HwABSStateMachine.this.ANTENNA_STATE_IN_SEARCH = true;
                    this.mSubId = message.getData().getInt(HwABSUtils.SUB_ID);
                    HwABSStateMachine.this.addModemState(this.mSubId);
                    if (HwABSStateMachine.this.mHwABSWiFiScenario.isSupInCompleteState() && !HwABSStateMachine.this.mHwABSWiFiHandler.getIsABSHandover()) {
                        if (HwABSStateMachine.this.mHwABSWiFiHandler.isNeedHandover()) {
                            HwABSStateMachine.this.updateABSAssociateTimes(1, 0);
                            HwABSStateMachine.this.mHwABSCHRManager.initABSHandoverException(3);
                            HwABSStateMachine.this.mHwABSCHRManager.increaseEventStatistics(3);
                            HwABSStateMachine.this.hwABSWiFiHandover(1);
                        } else {
                            HwABSStateMachine.this.mHwABSWiFiHandler.setAPCapability(1);
                            HwABSStateMachine.this.mHwABSWiFiHandler.setABSCurrentState(1);
                        }
                        HwABSStateMachine.this.transitionTo(HwABSStateMachine.this.mSisoState);
                        break;
                    }
                    HwABSStateMachine.this.mSwitchType = 3;
                    HwABSStateMachine.this.mSwitchEvent = 3;
                    HwABSStateMachine.this.sendMessageDelayed(23, 1000);
                    break;
                    break;
                case 15:
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
                case 16:
                    HwABSUtils.logE("MimoState MSG_WIFI_ANTENNA_PREEMPTED");
                    HwABSStateMachine.this.ANTENNA_STATE_IN_PREEMPTED = true;
                    if (HwABSStateMachine.this.isScreenOn()) {
                        HwABSStateMachine.this.mSwitchType = 4;
                        HwABSStateMachine.this.mSwitchEvent = 4;
                    } else {
                        HwABSStateMachine.this.mSwitchType = 5;
                        HwABSStateMachine.this.mSwitchEvent = 5;
                    }
                    if (HwABSStateMachine.this.mHwABSWiFiScenario.isSupInCompleteState() && !HwABSStateMachine.this.mHwABSWiFiHandler.getIsABSHandover()) {
                        if (HwABSStateMachine.this.mHwABSWiFiHandler.isNeedHandover()) {
                            HwABSStateMachine.this.updateABSAssociateTimes(1, 0);
                            HwABSStateMachine.this.mHwABSCHRManager.initABSHandoverException(HwABSStateMachine.this.mSwitchType);
                            HwABSStateMachine.this.mHwABSCHRManager.increaseEventStatistics(HwABSStateMachine.this.mSwitchEvent);
                            HwABSStateMachine.this.hwABSWiFiHandover(1);
                        } else {
                            HwABSStateMachine.this.mHwABSWiFiHandler.setAPCapability(1);
                            HwABSStateMachine.this.mHwABSWiFiHandler.setABSCurrentState(1);
                        }
                        HwABSStateMachine.this.transitionTo(HwABSStateMachine.this.mSisoState);
                        break;
                    }
                    HwABSStateMachine.this.sendMessageDelayed(23, 1000);
                    break;
                    break;
                case 23:
                    HwABSUtils.logD("MIMO MSG_DELAY_SWITCH ANTENNA_STATE_IN_CALL = " + HwABSStateMachine.this.ANTENNA_STATE_IN_CALL + " ANTENNA_STATE_IN_SEARCH = " + HwABSStateMachine.this.ANTENNA_STATE_IN_SEARCH + " ANTENNA_STATE_IN_CONNECT = " + HwABSStateMachine.this.ANTENNA_STATE_IN_CONNECT + " ANTENNA_STATE_IN_PREEMPTED = " + HwABSStateMachine.this.ANTENNA_STATE_IN_PREEMPTED);
                    if (HwABSStateMachine.this.ANTENNA_STATE_IN_CALL || HwABSStateMachine.this.ANTENNA_STATE_IN_SEARCH || HwABSStateMachine.this.ANTENNA_STATE_IN_CONNECT || HwABSStateMachine.this.ANTENNA_STATE_IN_PREEMPTED) {
                        HwABSUtils.logD("MIMO MSG_DELAY_SWITCH mSwitchType = " + HwABSStateMachine.this.mSwitchType + " mSwitchEvent = " + HwABSStateMachine.this.mSwitchEvent);
                        if (HwABSStateMachine.this.mHwABSWiFiScenario.isSupInCompleteState() && !HwABSStateMachine.this.mHwABSWiFiHandler.getIsABSHandover()) {
                            if (HwABSStateMachine.this.mHwABSWiFiHandler.isNeedHandover()) {
                                HwABSStateMachine.this.updateABSAssociateTimes(1, 0);
                                HwABSStateMachine.this.mHwABSCHRManager.initABSHandoverException(HwABSStateMachine.this.mSwitchType);
                                HwABSStateMachine.this.mHwABSCHRManager.increaseEventStatistics(HwABSStateMachine.this.mSwitchEvent);
                                HwABSStateMachine.this.hwABSWiFiHandover(1);
                            } else {
                                HwABSStateMachine.this.mHwABSWiFiHandler.setAPCapability(1);
                                HwABSStateMachine.this.mHwABSWiFiHandler.setABSCurrentState(1);
                            }
                            HwABSStateMachine.this.transitionTo(HwABSStateMachine.this.mSisoState);
                            break;
                        }
                        HwABSStateMachine.this.sendMessageDelayed(23, 1000);
                        break;
                    }
                    break;
                case 24:
                    handleSuppliantComplete();
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            HwABSUtils.logD("exit MimoState");
            HwABSStateMachine.this.removeMessages(23);
            long mimoScreenOnTime = 0;
            long mimoTime = System.currentTimeMillis() - HwABSStateMachine.this.mABSMIMOStartTime;
            if (HwABSStateMachine.this.mABSMIMOScreenOnStartTime != 0) {
                mimoScreenOnTime = System.currentTimeMillis() - HwABSStateMachine.this.mABSMIMOScreenOnStartTime;
            }
            HwABSStateMachine.this.mHwABSCHRManager.updateABSTime(this.mCurrentSSID, mimoTime, 0, mimoScreenOnTime, 0);
            HwABSStateMachine.this.mABSMIMOScreenOnStartTime = 0;
            HwABSStateMachine.this.mABSMIMOStartTime = 0;
        }

        private void handleSuppliantComplete() {
            WifiInfo wifiInfo = HwABSStateMachine.this.mWifiManager.getConnectionInfo();
            if (wifiInfo != null && wifiInfo.getSSID() != null && wifiInfo.getBSSID() != null) {
                HwABSUtils.logD("MimoState bssid = " + wifiInfo.getBSSID() + " ssid = " + wifiInfo.getSSID() + " mCurrentBSSID = " + this.mCurrentBSSID + " mCurrentSSID = " + this.mCurrentSSID + " mHwABSWiFiHandler.getIsABSHandover() = " + HwABSStateMachine.this.mHwABSWiFiHandler.getIsABSHandover());
                if (this.mCurrentBSSID.equals(wifiInfo.getBSSID()) || !this.mCurrentSSID.equals(HwABSCHRManager.getAPSSID(wifiInfo))) {
                    if (HwABSStateMachine.this.mHwABSWiFiHandler.getIsABSHandover() && this.mCurrentBSSID.equals(wifiInfo.getBSSID())) {
                        HwABSUtils.logD("mimo reassociate success");
                        HwABSStateMachine.this.mHwABSWiFiHandler.setTargetBssid("any");
                        HwABSStateMachine.this.sendMessage(1);
                    }
                } else if (HwABSStateMachine.this.isApInDatabase(wifiInfo.getBSSID())) {
                    HwABSStateMachine.this.transitionTo(HwABSStateMachine.this.mMimoState);
                } else {
                    Message msg = new Message();
                    msg.what = 1;
                    HwABSStateMachine.this.deferMessage(msg);
                    HwABSStateMachine.this.transitionTo(HwABSStateMachine.this.mWiFiConnectedState);
                }
            }
        }
    }

    class SisoState extends State {
        private String mCurrentBSSID = null;
        private String mCurrentSSID = null;

        SisoState() {
        }

        public void enter() {
            HwABSUtils.logD("enter SisoState ANTENNA_STATE_IN_PREEMPTED = " + HwABSStateMachine.this.ANTENNA_STATE_IN_PREEMPTED);
            HwABSStateMachine.this.setWiFiAntennaMonitor(true);
            if (!HwABSStateMachine.this.isScreenOn()) {
                HwABSStateMachine.this.mABSSISOScreenOnStartTime = System.currentTimeMillis();
            }
            HwABSStateMachine.this.mABSSISOStartTime = System.currentTimeMillis();
            WifiInfo wifiInfo = HwABSStateMachine.this.mWifiManager.getConnectionInfo();
            if (!(wifiInfo == null || wifiInfo.getSSID() == null || wifiInfo.getBSSID() == null)) {
                HwABSUtils.logD("SisoState bssid = " + wifiInfo.getBSSID() + " ssid = " + wifiInfo.getSSID());
                this.mCurrentSSID = HwABSCHRManager.getAPSSID(wifiInfo);
                this.mCurrentBSSID = wifiInfo.getBSSID();
            }
            if (HwABSStateMachine.this.ANTENNA_STATE_IN_PREEMPTED && HwABSStateMachine.this.isModemStateInIdle()) {
                HwABSStateMachine.this.ANTENNA_STATE_IN_PREEMPTED = false;
                HwABSStateMachine.this.handoverToMIMO();
            }
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case 1:
                    HwABSUtils.logE("SiSOState MSG_WIFI_CONNECTED mIsSupportVoWIFI = " + HwABSStateMachine.this.mIsSupportVoWIFI);
                    if (HwABSStateMachine.this.mHwABSWiFiHandler.getIsABSHandover()) {
                        if (HwABSStateMachine.this.mIsSupportVoWIFI && HwABSStateMachine.this.mHwABSWiFiHandler.isHandoverTimeout()) {
                            HwABSUtils.logE("SiSOState MSG_WIFI_CONNECTED handover time out");
                            HwABSStateMachine.this.updateABSAssociateTimes(0, 1);
                        } else {
                            HwABSStateMachine.this.updateABSAssociateSuccess();
                        }
                        HwABSStateMachine.this.mHwABSWiFiHandler.setIsABSHandover(false);
                        break;
                    }
                    break;
                case 2:
                    HwABSUtils.logD("SiSOState MSG_WIFI_DISCONNECTED");
                    if (HwABSStateMachine.this.mHwABSWiFiHandler.getIsABSHandover()) {
                        HwABSStateMachine.this.mHwABSWiFiHandler.setIsABSHandover(false);
                        HwABSStateMachine.this.mHwABSCHRManager.uploadABSReassociateExeption();
                        HwABSStateMachine.this.updateABSAssociateTimes(0, 1);
                    }
                    HwABSStateMachine.this.transitionTo(HwABSStateMachine.this.mWiFiDisconnectedState);
                    break;
                case 5:
                    HwABSUtils.logE("SiSOState MSG_SCREEN_ON isModemStateInIdle = " + HwABSStateMachine.this.isModemStateInIdle());
                    if (HwABSStateMachine.this.isModemStateInIdle()) {
                        if (HwABSStateMachine.this.isInPunishTime()) {
                            long mOverPunishTime = HwABSStateMachine.this.getPunishTime() - (System.currentTimeMillis() - HwABSStateMachine.this.ABS_LAST_HANDOVER_TIME);
                            HwABSUtils.logE("SiSOState MSG_SCREEN_ON inpunish time = " + mOverPunishTime);
                            if (mOverPunishTime > HwABSStateMachine.ABS_SCREEN_ON_TIME) {
                                HwABSStateMachine.this.sendHandoverToMIMOMsg(101, mOverPunishTime);
                            } else {
                                HwABSStateMachine.this.sendHandoverToMIMOMsg(101, HwABSStateMachine.ABS_SCREEN_ON_TIME);
                            }
                        } else {
                            HwABSStateMachine.this.sendHandoverToMIMOMsg(101, HwABSStateMachine.ABS_SCREEN_ON_TIME);
                        }
                    }
                    HwABSStateMachine.this.mABSSISOScreenOnStartTime = System.currentTimeMillis();
                    break;
                case 6:
                    HwABSUtils.logE("SiSOState MSG_SCREEN_OFF");
                    HwABSStateMachine.this.removeMessages(101);
                    if (HwABSStateMachine.this.mABSSISOScreenOnStartTime != 0) {
                        HwABSStateMachine.this.mHwABSCHRManager.updateABSTime(this.mCurrentSSID, 0, 0, 0, System.currentTimeMillis() - HwABSStateMachine.this.mABSSISOScreenOnStartTime);
                        HwABSStateMachine.this.mABSSISOScreenOnStartTime = 0;
                        break;
                    }
                    break;
                case 7:
                case 9:
                    HwABSUtils.logD("siso in or out call");
                    HwABSStateMachine.this.ANTENNA_STATE_IN_CALL = true;
                    break;
                case 8:
                    HwABSUtils.logE("SisoState MSG_ANTENNA_STATE_IDLE ANTENNA_STATE_IN_CALL = " + HwABSStateMachine.this.ANTENNA_STATE_IN_CALL);
                    if (HwABSStateMachine.this.ANTENNA_STATE_IN_CALL) {
                        HwABSStateMachine.this.ANTENNA_STATE_IN_CALL = false;
                        HwABSStateMachine.this.mIsInCallPunish = true;
                        HwABSStateMachine.this.handoverToMIMO();
                        break;
                    }
                    break;
                case 11:
                case 12:
                    HwABSUtils.logE("SisoState MSG_MODEM_ENTER_CONNECT_STATE");
                    HwABSStateMachine.this.ANTENNA_STATE_IN_CONNECT = true;
                    break;
                case 13:
                    HwABSUtils.logE("SisoState MSG_MODEM_EXIT_CONNECT_STATE ANTENNA_STATE_IN_CONNECT = " + HwABSStateMachine.this.ANTENNA_STATE_IN_CONNECT);
                    if (HwABSStateMachine.this.ANTENNA_STATE_IN_CONNECT) {
                        HwABSStateMachine.this.ANTENNA_STATE_IN_CONNECT = false;
                        HwABSStateMachine.this.handoverToMIMO();
                        break;
                    }
                    break;
                case 14:
                    HwABSUtils.logE("SisoState MSG_MODEM_ENTER_SEARCHING_STATE");
                    HwABSStateMachine.this.ANTENNA_STATE_IN_SEARCH = true;
                    HwABSStateMachine.this.removeMessages(101);
                    HwABSStateMachine.this.addModemState(message.getData().getInt(HwABSUtils.SUB_ID));
                    break;
                case 15:
                    HwABSUtils.logE("SisoState MSG_MODEM_EXIT_SEARCHING_STATE mModemStateList.size() ==" + HwABSStateMachine.this.mModemStateList.size());
                    if (HwABSStateMachine.this.mModemStateList.size() != 0) {
                        Bundle mData = message.getData();
                        int mSubId = mData.getInt(HwABSUtils.SUB_ID);
                        int mResult = mData.getInt(HwABSUtils.RES);
                        if (HwABSStateMachine.this.removeModemState(mSubId) == 0) {
                            HwABSStateMachine.this.ANTENNA_STATE_IN_SEARCH = false;
                        }
                        if (!HwABSStateMachine.this.isHaveSIMCard(mSubId)) {
                            if (!HwABSStateMachine.this.ANTENNA_STATE_IN_SEARCH && (mResult == 0 || mResult == 1)) {
                                HwABSStateMachine.this.handoverToMIMO();
                                break;
                            }
                            HwABSUtils.logE("SisoState keep stay in siso, have no sim card ANTENNA_STATE_IN_SEARCH = " + HwABSStateMachine.this.ANTENNA_STATE_IN_SEARCH);
                            break;
                        } else if (mResult == 0 && (HwABSStateMachine.this.ANTENNA_STATE_IN_SEARCH ^ 1) != 0) {
                            HwABSStateMachine.this.handoverToMIMO();
                            break;
                        } else {
                            HwABSUtils.logE("SisoState keep stay in siso, have sim card ANTENNA_STATE_IN_SEARCH = " + HwABSStateMachine.this.ANTENNA_STATE_IN_SEARCH);
                            break;
                        }
                    }
                    break;
                case 22:
                    HwABSStateMachine.this.handlePowerOffMessage();
                    if (HwABSStateMachine.this.isModemStateInIdle()) {
                        HwABSStateMachine.this.handoverToMIMO();
                        break;
                    }
                    break;
                case 24:
                    WifiInfo wifiInfo = HwABSStateMachine.this.mWifiManager.getConnectionInfo();
                    if (!(wifiInfo == null || wifiInfo.getSSID() == null || wifiInfo.getBSSID() == null)) {
                        HwABSUtils.logD("sisoState bssid = " + wifiInfo.getBSSID() + " ssid = " + wifiInfo.getSSID() + " mCurrentBSSID = " + this.mCurrentBSSID + " mCurrentSSID = " + this.mCurrentSSID);
                        if (!this.mCurrentBSSID.equals(wifiInfo.getBSSID()) && this.mCurrentSSID.equals(HwABSCHRManager.getAPSSID(wifiInfo))) {
                            if (!HwABSStateMachine.this.isApInDatabase(wifiInfo.getBSSID())) {
                                Message msg = new Message();
                                msg.what = 1;
                                HwABSStateMachine.this.deferMessage(msg);
                                HwABSStateMachine.this.transitionTo(HwABSStateMachine.this.mWiFiConnectedState);
                                break;
                            }
                            HwABSStateMachine.this.transitionTo(HwABSStateMachine.this.mSisoState);
                            break;
                        }
                        HwABSUtils.logD("sisoState mHwABSWiFiHandler.getIsABSHandover() = " + HwABSStateMachine.this.mHwABSWiFiHandler.getIsABSHandover() + " bssid = " + wifiInfo.getBSSID() + " mCurrentBSSID = " + this.mCurrentBSSID);
                        if (HwABSStateMachine.this.mHwABSWiFiHandler.getIsABSHandover() && this.mCurrentBSSID.equals(wifiInfo.getBSSID())) {
                            HwABSUtils.logD("siso reassociate success");
                            HwABSStateMachine.this.mHwABSWiFiHandler.setTargetBssid("any");
                            HwABSStateMachine.this.sendMessage(1);
                            break;
                        }
                    }
                    break;
                case 101:
                    boolean isModemStateIdle = HwABSStateMachine.this.isModemStateInIdle();
                    boolean isSIMCardInService = HwABSStateMachine.this.isSIMCardStatusIdle();
                    boolean isInBlackList = HwABSStateMachine.this.isAPInBlackList();
                    HwABSUtils.logE("SiSOState CMD_WIFI_SWITCH_MIMO isModemStateInIdle = " + isModemStateIdle + " isSIMCardInService = " + isSIMCardInService + " isInBlackList = " + isInBlackList);
                    if (isModemStateIdle && isSIMCardInService && (isInBlackList ^ 1) != 0) {
                        if (HwABSStateMachine.this.mHwABSWiFiScenario.isSupInCompleteState() && !HwABSStateMachine.this.mHwABSWiFiHandler.getIsABSHandover()) {
                            if (HwABSStateMachine.this.mHwABSWiFiHandler.isNeedHandover()) {
                                HwABSStateMachine.this.mHwABSCHRManager.initABSHandoverException(8);
                                HwABSStateMachine.this.mHwABSCHRManager.increaseEventStatistics(7);
                                HwABSStateMachine.this.updateABSAssociateTimes(1, 0);
                                HwABSStateMachine.this.hwABSWiFiHandover(2);
                            } else {
                                HwABSStateMachine.this.mHwABSWiFiHandler.setAPCapability(2);
                                HwABSStateMachine.this.mHwABSWiFiHandler.setABSCurrentState(2);
                            }
                            HwABSStateMachine.this.transitionTo(HwABSStateMachine.this.mMimoState);
                            break;
                        }
                        HwABSStateMachine.this.sendMessageDelayed(101, 1000);
                        break;
                    }
                    HwABSUtils.logE("SiSOState CMD_WIFI_SWITCH_MIMO keep in SISO");
                    break;
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            HwABSUtils.logD("exit SisoState");
            long sisoScreenOnTime = 0;
            long sisoTime = System.currentTimeMillis() - HwABSStateMachine.this.mABSSISOStartTime;
            if (HwABSStateMachine.this.mABSSISOScreenOnStartTime != 0) {
                sisoScreenOnTime = System.currentTimeMillis() - HwABSStateMachine.this.mABSSISOScreenOnStartTime;
            }
            HwABSStateMachine.this.mHwABSCHRManager.updateABSTime(this.mCurrentSSID, 0, sisoTime, 0, sisoScreenOnTime);
            HwABSStateMachine.this.mABSSISOScreenOnStartTime = 0;
            HwABSStateMachine.this.mABSSISOStartTime = 0;
        }
    }

    class WiFiConnectedState extends State {
        WiFiConnectedState() {
        }

        public void enter() {
            HwABSUtils.logD("enter WiFiConnectedState");
        }

        /* JADX WARNING: Missing block: B:19:0x005c, code:
            if (r0 != null) goto L_0x005e;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean processMessage(Message message) {
            switch (message.what) {
                case 1:
                    HwABSUtils.logE("WiFiConnectedState MSG_WIFI_CONNECTED");
                    WifiInfo mWifiInfo = HwABSStateMachine.this.mWifiManager.getConnectionInfo();
                    if (mWifiInfo != null && mWifiInfo.getBSSID() != null) {
                        if (!HwABSStateMachine.this.isAPSupportMIMOCapability(mWifiInfo.getBSSID()) || !ScanResult.is24GHz(mWifiInfo.getFrequency()) || (HwABSStateMachine.this.isMobileAP() ^ 1) == 0) {
                            HwABSUtils.logE(" It is a 5G AP or siso AP");
                            break;
                        }
                        HwABSApInfoData data = HwABSStateMachine.this.mHwABSDataBaseManager.getApInfoByBssid(mWifiInfo.getBSSID());
                        if (data == null) {
                            data = initApInfoData(mWifiInfo);
                            break;
                        }
                        data.mLast_connect_time = System.currentTimeMillis();
                        HwABSStateMachine.this.mHwABSDataBaseManager.addOrUpdateApInfos(data);
                        HwABSUtils.logD("now capability = " + HwABSStateMachine.this.mHwABSWiFiHandler.getCurrentCapability());
                        if (data.mIn_black_List == 1 && HwABSStateMachine.this.mHwABSWiFiHandler.getCurrentCapability() == 2) {
                            HwABSUtils.logD("current AP is in blackList reset capability");
                            HwABSStateMachine.this.mHwABSWiFiHandler.setAPCapability(1);
                            HwABSStateMachine.this.setBlackListBssid();
                        }
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
                case 17:
                    HwABSStateMachine.this.mHwABSWiFiHandler.hwABScheckLinked();
                    break;
                case 18:
                    HwABSUtils.logE("WiFiConnectedState MSG_WIFI_CHECK_LINK_SUCCESS");
                    if (!HwABSStateMachine.this.isUsingMIMOCapability()) {
                        HwABSStateMachine.this.transitionTo(HwABSStateMachine.this.mSisoState);
                        break;
                    }
                    HwABSStateMachine.this.transitionTo(HwABSStateMachine.this.mMimoState);
                    break;
                case 19:
                    HwABSUtils.logE("WiFiConnectedState MSG_WIFI_CHECK_LINK_FAILED");
                    WifiInfo wifiInfo = HwABSStateMachine.this.mWifiManager.getConnectionInfo();
                    if (wifiInfo != null && wifiInfo.getBSSID() != null) {
                        HwABSApInfoData hwABSApInfoData = HwABSStateMachine.this.mHwABSDataBaseManager.getApInfoByBssid(wifiInfo.getBSSID());
                        if (hwABSApInfoData != null) {
                            hwABSApInfoData.mSwitch_siso_type = 2;
                            HwABSStateMachine.this.mHwABSDataBaseManager.addOrUpdateApInfos(hwABSApInfoData);
                            HwABSStateMachine.this.hwABSWiFiHandover(1);
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
            int authType = 0;
            WifiConfiguration sWifiConfiguration = getCurrntConfig(wifiInfo);
            if (sWifiConfiguration != null && sWifiConfiguration.allowedKeyManagement.cardinality() <= 1) {
                authType = sWifiConfiguration.getAuthType();
            }
            return new HwABSApInfoData(wifiInfo.getBSSID(), HwABSCHRManager.getAPSSID(wifiInfo), 2, 2, authType, 0, 0, 0, 0, System.currentTimeMillis());
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
            if (config.allowedKeyManagement.cardinality() > 1) {
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
            HwABSUtils.logD("WiFiDisableState send MODEM_TUNERIC_IACTIVE_MSG");
            HwTelephonyManagerInner.getDefault().notifyCModemStatus(HwABSStateMachine.this.MODEM_TUNERIC_IACTIVE, HwABSStateMachine.this.mIactiveCallback);
            HwABSStateMachine.this.RESENT_MODEM_TUNERIC_IACTIVE_TIMES = 0;
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case 1:
                case 2:
                case 4:
                    HwABSUtils.logD("WiFiDisableState handle message.what = " + message.what);
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
                case 2:
                    HwABSUtils.logD("WiFiDisconnectedState MSG_WIFI_DISCONNECTED");
                    return true;
                case 4:
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
            HwABSStateMachine.this.mHwABSWiFiHandler.setAPCapability(HwABSStateMachine.this.mHwABSWiFiHandler.getCurrentCapability());
            HwABSUtils.logD("WiFiEnableState send MODEM_TUNERIC_ACTIVE_MSG");
            HwTelephonyManagerInner.getDefault().notifyCModemStatus(HwABSStateMachine.this.MODEM_TUNERIC_ACTIVE, HwABSStateMachine.this.mActiveCallback);
            HwABSStateMachine.this.RESENT_MODEM_TUNERIC_ACTIVE_TIMES = 0;
            HwABSStateMachine.this.setBlackListBssid();
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case 3:
                    HwABSUtils.logD("WiFiEnableState MSG_WIFI_ENABLED");
                    return true;
                case 4:
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

    public static HwABSStateMachine createHwABSStateMachine(Context context, WifiStateMachine wifiStateMachine) {
        if (mHwABSStateMachine == null) {
            mHwABSStateMachine = new HwABSStateMachine(context, wifiStateMachine);
        }
        return mHwABSStateMachine;
    }

    private HwABSStateMachine(Context context, WifiStateMachine wifiStateMachine) {
        super("HwABSStateMachine");
        this.mContext = context;
        this.mHwABSDataBaseManager = HwABSDataBaseManager.getInstance(context);
        this.mHwABSWiFiScenario = new HwABSWiFiScenario(context, getHandler());
        HwABSModemScenario hwABSModemScenario = new HwABSModemScenario(context, getHandler());
        this.mHwABSWiFiHandler = new HwABSWiFiHandler(context, getHandler(), wifiStateMachine);
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        this.mHwABSCHRManager = HwABSCHRManager.getInstance(context);
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

    private NetworkDetail getNetWorkDetail(String bssid) {
        NetworkDetail detail = null;
        for (ScanResult result : this.mWifiManager.getScanResults()) {
            if (result.BSSID.equals(bssid)) {
                detail = ScanResultUtil.toScanDetail(result).getNetworkDetail();
            }
        }
        return detail;
    }

    private boolean isUsingMIMOCapability() {
        if (this.mHwABSWiFiHandler.getCurrentCapability() == 2) {
            return true;
        }
        return false;
    }

    private boolean isAPSupportMIMOCapability(String bssid) {
        NetworkDetail mNetworkDetail = getNetWorkDetail(bssid);
        if (mNetworkDetail != null) {
            HwABSUtils.logD("isAPSupportMIMOCapability mNetworkDetail.getStream1() = " + mNetworkDetail.getStream1() + " mNetworkDetail.getStream2() = " + mNetworkDetail.getStream2() + " mNetworkDetail.getStream3() = " + mNetworkDetail.getStream3() + " mNetworkDetail.getStream4() = " + mNetworkDetail.getStream4());
            if (((mNetworkDetail.getStream1() + mNetworkDetail.getStream2()) + mNetworkDetail.getStream3()) + mNetworkDetail.getStream4() >= 2) {
                return true;
            }
        }
        return false;
    }

    private void hwABSWiFiHandover(int capability) {
        HwABSUtils.logD("hwABSWiFiHandover capability = " + capability);
        if (capability == 1) {
            setPunishTime();
        }
        this.mHwABSWiFiHandler.hwABSHandover(capability);
    }

    private void setPunishTime() {
        if (this.ABS_LAST_HANDOVER_TIME == 0 || System.currentTimeMillis() - this.ABS_LAST_HANDOVER_TIME > ABS_INTERVAL_TIME) {
            this.ABS_HANDOVER_TIMES = 1;
            HwABSUtils.logD("setPunishTime reset times ABS_HANDOVER_TIMES = " + this.ABS_HANDOVER_TIMES);
        } else {
            this.ABS_HANDOVER_TIMES++;
            if (this.ABS_HANDOVER_TIMES == 10) {
                this.mHwABSCHRManager.increaseEventStatistics(8);
            } else if (this.ABS_HANDOVER_TIMES >= 10) {
                HwABSCHRStatistics record = this.mHwABSCHRManager.getStatisticsInfo();
                if (record != null && record.max_ping_pong_times < this.ABS_HANDOVER_TIMES) {
                    record.max_ping_pong_times = this.ABS_HANDOVER_TIMES;
                    this.mHwABSCHRManager.updateCHRInfo(record);
                }
            }
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

    private boolean isSIMCardStatusIdle() {
        boolean isCardReady = false;
        int phoneNum = this.mTelephonyManager.getPhoneCount();
        HwABSUtils.logD("isSIMCardStatusIdle phoneNum = " + phoneNum);
        if (phoneNum == 0) {
            return true;
        }
        for (int i = 0; i < phoneNum; i++) {
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
        for (int subId = 0; subId < cardNum; subId++) {
            int cardState = this.mTelephonyManager.getSimState(subId);
            HwABSUtils.logD("compareSIMStatusWithCardReady subId = " + subId + " cardState = " + cardState);
            if (cardState == 5) {
                ServiceState serviceState = this.mTelephonyManager.getServiceStateForSubscriber(subId);
                if (serviceState != null) {
                    int voiceState = serviceState.getState();
                    HwABSUtils.logD("compareSIMStatusWithCardReady subId = " + subId + " voiceState = " + voiceState);
                    switch (voiceState) {
                        case 0:
                        case 3:
                            statusList.add(Integer.valueOf(2));
                            break;
                        default:
                            statusList.add(Integer.valueOf(1));
                            break;
                    }
                }
                statusList.add(Integer.valueOf(1));
            } else {
                statusList.add(Integer.valueOf(2));
            }
        }
        for (int i = 0; i < statusList.size(); i++) {
            if (((Integer) statusList.get(i)).intValue() != 2) {
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
        if (capablity != 2) {
            this.mHwABSWiFiHandler.setAPCapability(capablity);
            this.mHwABSWiFiHandler.setABSCurrentState(capablity);
        } else if (isModemStateInIdle() && (isInPunishTime() ^ 1) != 0) {
            this.mHwABSWiFiHandler.setAPCapability(capablity);
            this.mHwABSWiFiHandler.setABSCurrentState(capablity);
        }
    }

    private boolean isModemStateInIdle() {
        if (this.ANTENNA_STATE_IN_CALL || (this.ANTENNA_STATE_IN_SEARCH ^ 1) == 0 || (this.ANTENNA_STATE_IN_CONNECT ^ 1) == 0 || !isScreenOn()) {
            HwABSUtils.logD("isModemStateInIdle return false ANTENNA_STATE_IN_CALL = " + this.ANTENNA_STATE_IN_CALL + "  ANTENNA_STATE_IN_SEARCH = " + this.ANTENNA_STATE_IN_SEARCH + "  ANTENNA_STATE_IN_CONNECT = " + this.ANTENNA_STATE_IN_CONNECT + " isScreenOn() = " + isScreenOn());
            return false;
        }
        HwABSUtils.logD("isModemStateInIdle return true");
        return true;
    }

    private void addModemState(int subId) {
        HwABSUtils.logD("addModemState subId = " + subId);
        if (this.mModemStateList.size() == 0) {
            this.mModemStateList.add(Integer.valueOf(subId));
        } else {
            int i = 0;
            while (i < this.mModemStateList.size()) {
                if (((Integer) this.mModemStateList.get(i)).intValue() != subId) {
                    i++;
                } else {
                    return;
                }
            }
            this.mModemStateList.add(Integer.valueOf(subId));
        }
        HwABSUtils.logD("addModemState size = " + this.mModemStateList.size());
    }

    private int removeModemState(int subId) {
        HwABSUtils.logD("removeModemState size = " + this.mModemStateList.size() + " subId = " + subId);
        if (this.mModemStateList.size() == 0) {
            return 0;
        }
        int flag = -1;
        for (int i = 0; i < this.mModemStateList.size(); i++) {
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
        if (isModemStateInIdle()) {
            if (hasMessages(101)) {
                removeMessages(101);
                HwABSUtils.logD("handoverToMIMO is already have message remove it");
            }
            if (isInPunishTime()) {
                long mOverPunishTime = getPunishTime() - (System.currentTimeMillis() - this.ABS_LAST_HANDOVER_TIME);
                HwABSUtils.logD("handoverToMIMO mOverPunishTime = " + mOverPunishTime + " mIsInCallPunish = " + this.mIsInCallPunish);
                if (!this.mIsInCallPunish || mOverPunishTime >= ABS_PUNISH_TIME) {
                    sendMessageDelayed(101, mOverPunishTime);
                } else {
                    HwABSUtils.logD("handoverToMIMO reset punish time  = 30000");
                    sendMessageDelayed(101, ABS_PUNISH_TIME);
                }
            } else if (this.mIsInCallPunish) {
                HwABSUtils.logD("handoverToMIMO mIsInCallPunish punish time  = 30000");
                sendMessageDelayed(101, ABS_PUNISH_TIME);
            } else {
                sendMessageDelayed(101, 2000);
            }
            this.mIsInCallPunish = false;
            return;
        }
        HwABSUtils.logD("handoverToMIMO is not in idle ignore it");
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
        if (System.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) != 1) {
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
        for (int i = 0; i < phoneNum; i++) {
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

    private void updateABSAssociateTimes(int associateTimes, int associateFailedTimes) {
        String bssid;
        String ssid;
        if (associateTimes == 1) {
            WifiInfo mWifiInfo = this.mWifiManager.getConnectionInfo();
            if (mWifiInfo == null || mWifiInfo.getBSSID() == null || mWifiInfo.getSSID() == null) {
                HwABSUtils.logE("updateABSAssociateTimes mWifiInfo error");
                return;
            }
            bssid = mWifiInfo.getBSSID();
            ssid = HwABSCHRManager.getAPSSID(mWifiInfo);
            this.mAssociateSSID = ssid;
            this.mAssociateBSSID = bssid;
        } else {
            ssid = this.mAssociateSSID;
            bssid = this.mAssociateBSSID;
        }
        HwABSUtils.logE("updateABSAssociateTimes bssid = " + bssid + " associateTimes = " + associateTimes + " associateFailedTimes = " + associateFailedTimes);
        HwABSApInfoData hwABSApInfoData = this.mHwABSDataBaseManager.getApInfoByBssid(bssid);
        if (hwABSApInfoData != null) {
            int blackListStatus = hwABSApInfoData.mIn_black_List;
            hwABSApInfoData.mReassociate_times += associateTimes;
            hwABSApInfoData.mFailed_times += associateFailedTimes;
            if (associateFailedTimes != 0) {
                updateABSAssociateFailedEvent(hwABSApInfoData);
            }
            this.mHwABSDataBaseManager.addOrUpdateApInfos(hwABSApInfoData);
            if (blackListStatus == 0 && hwABSApInfoData.mIn_black_List == 1) {
                setBlackListBssid();
                uploadBlackListException(hwABSApInfoData);
            }
        } else {
            HwABSUtils.logE("updateABSAssociateTimes error!!");
        }
        this.mHwABSCHRManager.updateCHRAssociateTimes(ssid, associateTimes, associateFailedTimes);
    }

    private void updateABSAssociateFailedEvent(HwABSApInfoData data) {
        int continuousTimes;
        int highFailedRate;
        int lowFailedRate;
        int failedRate = 0;
        HwABSUtils.logE("updateABSAssociateFailedEvent mIsSupportVoWIFI = " + this.mIsSupportVoWIFI);
        if (this.mIsSupportVoWIFI) {
            continuousTimes = 2;
            highFailedRate = 5;
            lowFailedRate = 15;
        } else {
            continuousTimes = 3;
            highFailedRate = 10;
            lowFailedRate = 30;
        }
        data.mContinuous_failure_times++;
        if (data.mContinuous_failure_times >= continuousTimes) {
            HwABSUtils.logE("updateABSAssociateFailedEvent mContinuous_failure_times = " + data.mContinuous_failure_times);
            data.mIn_black_List = 1;
            this.mAddBlackListReason = 1;
        }
        if (data.mReassociate_times > 50) {
            failedRate = highFailedRate;
        } else if (data.mReassociate_times > 10) {
            failedRate = lowFailedRate;
        }
        int temp = (data.mFailed_times * 100) / data.mReassociate_times;
        HwABSUtils.logE("updateABSAssociateFailedEvent temp = " + temp + " failedRate = " + failedRate);
        if (failedRate > 0 && temp > failedRate) {
            data.mIn_black_List = 1;
            this.mAddBlackListReason = 2;
        }
    }

    private boolean isAPInBlackList() {
        boolean z = true;
        WifiInfo mWifiInfo = this.mWifiManager.getConnectionInfo();
        if (mWifiInfo == null || mWifiInfo.getBSSID() == null) {
            HwABSUtils.logE("isAPInBlackList mWifiInfo error");
            return false;
        }
        HwABSApInfoData hwABSApInfoData = this.mHwABSDataBaseManager.getApInfoByBssid(mWifiInfo.getBSSID());
        if (hwABSApInfoData == null) {
            return false;
        }
        if (hwABSApInfoData.mIn_black_List != 1) {
            z = false;
        }
        return z;
    }

    private void updateABSAssociateSuccess() {
        WifiInfo mWifiInfo = this.mWifiManager.getConnectionInfo();
        if (mWifiInfo == null || mWifiInfo.getBSSID() == null) {
            HwABSUtils.logE("updateABSAssociateSuccess mWifiInfo error");
            return;
        }
        HwABSApInfoData hwABSApInfoData = this.mHwABSDataBaseManager.getApInfoByBssid(mWifiInfo.getBSSID());
        if (hwABSApInfoData != null) {
            hwABSApInfoData.mContinuous_failure_times = 0;
            this.mHwABSDataBaseManager.addOrUpdateApInfos(hwABSApInfoData);
        }
    }

    public void setBlackListBssid() {
        StringBuilder blackList = new StringBuilder();
        List<HwABSApInfoData> lists = initBlackListDate();
        if (lists.size() != 0) {
            for (HwABSApInfoData data : lists) {
                blackList.append(data.mBssid);
                blackList.append(";");
            }
            this.mHwABSWiFiHandler.setABSBlackList(blackList.toString());
        }
    }

    private List<HwABSApInfoData> initBlackListDate() {
        List<HwABSApInfoData> lists = this.mHwABSDataBaseManager.getApInfoInBlackList();
        if (lists.size() <= 10) {
            return lists;
        }
        return seleteBlackApInfo(lists);
    }

    private List<HwABSApInfoData> seleteBlackApInfo(List<HwABSApInfoData> lists) {
        int size;
        List<HwABSApInfoData> result = new ArrayList();
        Collections.sort(lists);
        Collections.reverse(lists);
        if (lists.size() <= 10) {
            size = lists.size();
        } else {
            size = 10;
        }
        for (int i = 0; i < size; i++) {
            result.add((HwABSApInfoData) lists.get(i));
        }
        return result;
    }

    private boolean isApInDatabase(String bssid) {
        if (this.mHwABSDataBaseManager.getApInfoByBssid(bssid) != null) {
            return true;
        }
        return false;
    }

    private void uploadBlackListException(HwABSApInfoData data) {
        List<HwABSApInfoData> lists = this.mHwABSDataBaseManager.getAllApInfo();
        List<HwABSApInfoData> blacklists = this.mHwABSDataBaseManager.getApInfoInBlackList();
        HwABSCHRBlackListEvent event = new HwABSCHRBlackListEvent();
        event.mABSApSsid = data.mSsid;
        event.mABSApBssid = data.mBssid;
        event.mABSAddReason = this.mAddBlackListReason;
        event.mABSSuportVoWifi = this.mIsSupportVoWIFI ? 1 : 0;
        event.mABSSwitchTimes = data.mReassociate_times;
        event.mABSFailedTimes = data.mFailed_times;
        if (lists != null) {
            event.mABSTotalNum = lists.size();
        }
        if (blacklists != null) {
            event.mABSBlackListNum = blacklists.size();
        }
        this.mHwABSCHRManager.uploadBlackListException(event);
    }

    public void notifySelEngineEnableWiFi() {
        HwABSUtils.logD("notifySelEngineEnableWiFi");
        this.mHwABSWiFiHandler.setAPCapability(this.mHwABSWiFiHandler.getCurrentCapability());
    }

    public void notifySelEngineResetCompelete() {
        HwABSUtils.logD("notifySelEngineResetCompelete");
        sendMessage(38);
    }
}
