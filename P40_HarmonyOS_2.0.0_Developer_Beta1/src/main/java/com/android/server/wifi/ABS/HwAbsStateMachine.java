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
import android.provider.Settings;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.PhoneCallback;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import com.android.ims.ImsManager;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.wifi.ClientModeImpl;
import com.android.server.wifi.ScanDetail;
import com.android.server.wifi.hotspot2.NetworkDetail;
import com.android.server.wifi.util.ScanResultUtil;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HwAbsStateMachine extends StateMachine {
    private static final long ABS_INTERVAL_TIME = 1800000;
    private static final long ABS_PUNISH_TIME = 60000;
    private static final long ABS_SCREEN_ON_TIME = 10000;
    private static final String ACTION_ABS_HANDOVER_TIMER = "android.net.wifi.abs_handover_timer";
    private static final int MAX_HANDOVER_TIME = 15;
    private static final int MODEM_TUNERIC_ACTIVE = 1;
    private static final int MODEM_TUNERIC_IACTIVE = 0;
    private static final long ONE_DAYA_TIME = 86400000;
    private static final int RESTART_ABS_TIME = 300000;
    private static final int SIM_CARD_STATE_MIMO = 2;
    private static final int SIM_CARD_STATE_SISO = 1;
    private static HwAbsStateMachine sHwAbsStateMachine = null;
    private int mAbsHandoverTime = 0;
    private long mAbsLastHandoverTime = 0;
    private long mAbsMimoScreenOnStartTime = 0;
    private long mAbsMimoStartTime = 0;
    private long mAbsSisoScreenOnStartTime = 0;
    private long mAbsSisoStartTime = 0;
    private PhoneCallback mActiveCallback = new PhoneCallback() {
        /* class com.android.server.wifi.ABS.HwAbsStateMachine.AnonymousClass1 */

        public void onPhoneCallback1(int parm) {
            HwAbsStateMachine.this.sendMessage(33, parm);
        }
    };
    private int mAddBlacklistReason = 0;
    private Map<String, ApHandoverInfo> mApHandoverInfoList = new HashMap();
    private String mAssociateBssid = null;
    private String mAssociateSsid = null;
    private Context mContext;
    private State mDefaultState = new DefaultState();
    private HwAbsChrManager mHwAbsChrManager;
    private HwAbsDataBaseManager mHwAbsDataBaseManager;
    private HwAbsWiFiHandler mHwAbsWifiHandler;
    private HwAbsWiFiScenario mHwAbsWifiScenario;
    private PhoneCallback mIactiveCallback = new PhoneCallback() {
        /* class com.android.server.wifi.ABS.HwAbsStateMachine.AnonymousClass2 */

        public void onPhoneCallback1(int parm) {
            HwAbsStateMachine.this.sendMessage(35, parm);
        }
    };
    private boolean mIsAntennaStateInCall = false;
    private boolean mIsAntennaStateInConnect = false;
    private boolean mIsAntennaStateInPreempted = false;
    private boolean mIsAntennaStateInSearch = false;
    private boolean mIsInCallPunish = false;
    private boolean mIsPauseHandover = false;
    private boolean mIsSupportVoWifi = false;
    private boolean mIsSwitching = false;
    private State mMimoState = new MimoState();
    private List<Integer> mModemStateList = new ArrayList();
    private int mResendModemTunericActiveTime = 0;
    private int mResendModemTunericIActiveTime = 0;
    private State mSisoState = new SisoState();
    private int mSwitchEvent = 0;
    private int mSwitchType = 0;
    private TelephonyManager mTelephonyManager;
    private State mWifiConnectedState = new WiFiConnectedState();
    private State mWifiDisableState = new WiFiDisableState();
    private State mWifiDisconnectedState = new WiFiDisconnectedState();
    private State mWifiEnableState = new WiFiEnableState();
    private WifiManager mWifiManager;

    static /* synthetic */ int access$1508(HwAbsStateMachine x0) {
        int i = x0.mResendModemTunericActiveTime;
        x0.mResendModemTunericActiveTime = i + 1;
        return i;
    }

    static /* synthetic */ int access$1808(HwAbsStateMachine x0) {
        int i = x0.mResendModemTunericIActiveTime;
        x0.mResendModemTunericIActiveTime = i + 1;
        return i;
    }

    private HwAbsStateMachine(Context context, ClientModeImpl wifiStateMachine) {
        super("HwAbsStateMachine");
        this.mContext = context;
        this.mHwAbsDataBaseManager = HwAbsDataBaseManager.getInstance(context);
        this.mHwAbsWifiScenario = new HwAbsWiFiScenario(context, getHandler());
        new HwAbsModemScenario(context, getHandler());
        this.mHwAbsWifiHandler = new HwAbsWiFiHandler(context, getHandler(), wifiStateMachine);
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        this.mHwAbsChrManager = HwAbsChrManager.getInstance(context);
        addState(this.mDefaultState);
        addState(this.mWifiEnableState, this.mDefaultState);
        addState(this.mWifiDisableState, this.mDefaultState);
        addState(this.mWifiConnectedState, this.mWifiEnableState);
        addState(this.mWifiDisconnectedState, this.mWifiEnableState);
        addState(this.mMimoState, this.mWifiEnableState);
        addState(this.mSisoState, this.mWifiEnableState);
        setInitialState(this.mDefaultState);
        start();
    }

    public static HwAbsStateMachine createHwAbsStateMachine(Context context, ClientModeImpl wifiStateMachine) {
        if (sHwAbsStateMachine == null) {
            sHwAbsStateMachine = new HwAbsStateMachine(context, wifiStateMachine);
        }
        return sHwAbsStateMachine;
    }

    class DefaultState extends State {
        private static final int DEFAULT_MODEM_STATE = 0;
        private static final int MAX_MODEM_TUNERIC_TIME = 3;
        private static final int RESEND_TUNERIC_MSG_DELAY = 5000;
        Bundle mData = null;
        int mSubId = -1;

        DefaultState() {
        }

        public boolean processMessage(Message message) {
            int i = message.what;
            if (i == 1) {
                HwAbsUtils.logD(false, "DefaultState MSG_WIFI_CONNECTED", new Object[0]);
                HwAbsStateMachine hwAbsStateMachine = HwAbsStateMachine.this;
                hwAbsStateMachine.transitionTo(hwAbsStateMachine.mWifiConnectedState);
                HwAbsStateMachine.this.sendMessage(1);
            } else if (i == 2) {
                HwAbsUtils.logD(false, "DefaultState MSG_WIFI_DISCONNECTED", new Object[0]);
                HwAbsStateMachine.this.removeMessages(1);
                HwAbsStateMachine hwAbsStateMachine2 = HwAbsStateMachine.this;
                hwAbsStateMachine2.transitionTo(hwAbsStateMachine2.mWifiDisconnectedState);
            } else if (i == 3) {
                HwAbsUtils.logD(false, "DefaultState MSG_WIFI_ENABLED", new Object[0]);
                HwAbsStateMachine hwAbsStateMachine3 = HwAbsStateMachine.this;
                hwAbsStateMachine3.transitionTo(hwAbsStateMachine3.mWifiEnableState);
            } else if (i == 4) {
                HwAbsUtils.logD(false, "DefaultState MSG_WIFI_DISABLE", new Object[0]);
                HwAbsStateMachine hwAbsStateMachine4 = HwAbsStateMachine.this;
                hwAbsStateMachine4.transitionTo(hwAbsStateMachine4.mWifiDisableState);
            } else if (i == 7) {
                HwAbsUtils.logD(false, "DefaultState MSG_OUTGOING_CALL", new Object[0]);
                HwAbsStateMachine.this.mIsAntennaStateInCall = true;
                HwAbsStateMachine.this.resetCapablity(1);
            } else if (i == 8) {
                HwAbsUtils.logD(false, "DefaultState MSG_CALL_STATE_IDLE", new Object[0]);
                HwAbsStateMachine.this.mIsAntennaStateInCall = false;
                HwAbsStateMachine.this.resetCapablity(2);
            } else if (i == 9) {
                HwAbsUtils.logD(false, "DefaultState MSG_CALL_STATE_RINGING", new Object[0]);
                HwAbsStateMachine.this.mIsAntennaStateInCall = true;
                HwAbsStateMachine.this.resetCapablity(1);
            } else if (i == 22) {
                HwAbsStateMachine.this.handlePowerOffMessage();
                if (HwAbsStateMachine.this.isModemStateInIdle()) {
                    HwAbsStateMachine.this.resetCapablity(2);
                }
            } else if (i == 25) {
                handleModemStateInService();
            } else if (i != 103) {
                switch (i) {
                    case 11:
                    case 12:
                        HwAbsUtils.logD(false, "DefaultState MSG_MODEM_ENTER_CONNECT_STATE", new Object[0]);
                        HwAbsStateMachine.this.mIsAntennaStateInConnect = true;
                        HwAbsStateMachine.this.resetCapablity(1);
                        break;
                    case 13:
                        handleModemExitConnect();
                        break;
                    case 14:
                        handleModemEnterSearching(message);
                        break;
                    case 15:
                        handleModemExitSearching(message);
                        break;
                    case 16:
                        HwAbsUtils.logD(false, "DefaultState MSG_WIFI_ANTENNA_PREEMPTED", new Object[0]);
                        break;
                    default:
                        switch (i) {
                            case 33:
                                handleModemTunericActive(message);
                                break;
                            case 34:
                                handleResendTunericActive();
                                break;
                            case 35:
                                handleModemTunericInactive(message);
                                break;
                            case 36:
                                handleResendTunericInactive();
                                break;
                            case HwAbsUtils.MSG_BOOT_COMPLETED /* 37 */:
                                handleBootComplete();
                                break;
                            case HwAbsUtils.MSG_SEL_ENGINE_RESET_COMPLETED /* 38 */:
                                HwAbsUtils.logD(false, "DefaultState MSG_SEL_ENGINE_RESET_COMPLETED", new Object[0]);
                                HwAbsStateMachine hwAbsStateMachine5 = HwAbsStateMachine.this;
                                hwAbsStateMachine5.transitionTo(hwAbsStateMachine5.mWifiConnectedState);
                                HwAbsStateMachine.this.sendMessage(1);
                                break;
                        }
                }
            } else {
                HwAbsUtils.logD(false, "DefaultState CMD_WIFI_PAUSE_HANDOVER", new Object[0]);
                HwAbsStateMachine.this.mIsPauseHandover = false;
            }
            return true;
        }

        private void handleModemStateInService() {
            HwAbsStateMachine hwAbsStateMachine = HwAbsStateMachine.this;
            hwAbsStateMachine.mIsSupportVoWifi = ImsManager.isWfcEnabledByPlatform(hwAbsStateMachine.mContext);
            HwAbsUtils.logD(false, "DefaultState mIsSupportVoWifi = %{public}s", String.valueOf(HwAbsStateMachine.this.mIsSupportVoWifi));
        }

        private void handleBootComplete() {
            if (HwAbsStateMachine.this.mWifiManager.isWifiEnabled()) {
                HwAbsUtils.logD(false, "DefaultState send MODEM_TUNERIC_ACTIVE_MSG", new Object[0]);
                HwTelephonyManagerInner.getDefault().notifyCModemStatus(1, HwAbsStateMachine.this.mActiveCallback);
                HwAbsStateMachine.this.mResendModemTunericActiveTime = 0;
                HwAbsStateMachine.this.mHwAbsWifiHandler.setApCapability(HwAbsStateMachine.this.mHwAbsWifiHandler.getCurrentCapability());
                HwAbsStateMachine.this.setBlacklistBssid();
                return;
            }
            HwAbsUtils.logD(false, "DefaultState send MODEM_TUNERIC_IACTIVE_MSG", new Object[0]);
            HwTelephonyManagerInner.getDefault().notifyCModemStatus(0, HwAbsStateMachine.this.mIactiveCallback);
            HwAbsStateMachine.this.mResendModemTunericIActiveTime = 0;
        }

        private void handleResendTunericInactive() {
            HwAbsUtils.logD(false, "DefaultState MSG_RESEND_TUNERIC_IACTIVE_MSG", new Object[0]);
            if (!HwAbsStateMachine.this.mWifiManager.isWifiEnabled()) {
                HwTelephonyManagerInner.getDefault().notifyCModemStatus(0, HwAbsStateMachine.this.mIactiveCallback);
                HwAbsStateMachine.access$1808(HwAbsStateMachine.this);
            }
        }

        private void handleModemTunericInactive(Message message) {
            int iActiveResult = message.arg1;
            HwAbsUtils.logD(false, "DefaultState MSG_MODEM_TUNERIC_IACTIVE_RESULT iActiveResult = %{public}d  mResendModemTunericIActiveTime = %{public}d", Integer.valueOf(iActiveResult), Integer.valueOf(HwAbsStateMachine.this.mResendModemTunericIActiveTime));
            if (iActiveResult == 1) {
                HwAbsStateMachine.this.mResendModemTunericIActiveTime = 0;
            } else if (!HwAbsStateMachine.this.mWifiManager.isWifiEnabled() && HwAbsStateMachine.this.mResendModemTunericIActiveTime < 3) {
                HwAbsStateMachine.this.removeMessages(34);
                HwAbsStateMachine.this.sendMessageDelayed(36, 5000);
            }
        }

        private void handleResendTunericActive() {
            HwAbsUtils.logD(false, "DefaultState MSG_RESEND_TUNERIC_ACTIVE_MSG", new Object[0]);
            if (HwAbsStateMachine.this.mWifiManager.isWifiEnabled()) {
                HwTelephonyManagerInner.getDefault().notifyCModemStatus(1, HwAbsStateMachine.this.mActiveCallback);
                HwAbsStateMachine.access$1508(HwAbsStateMachine.this);
            }
        }

        private void handleModemTunericActive(Message message) {
            int activeResult = message.arg1;
            HwAbsUtils.logD(false, "DefaultState MSG_MODEM_TUNERIC_ACTIVE_RESULT activeResult = %{public}d  mResendModemTunericActiveTime = %{public}d", Integer.valueOf(activeResult), Integer.valueOf(HwAbsStateMachine.this.mResendModemTunericActiveTime));
            if (activeResult == 1) {
                HwAbsStateMachine.this.mResendModemTunericActiveTime = 0;
            } else if (HwAbsStateMachine.this.mWifiManager.isWifiEnabled() && HwAbsStateMachine.this.mResendModemTunericActiveTime < 3) {
                HwAbsStateMachine.this.removeMessages(34);
                HwAbsStateMachine.this.sendMessageDelayed(34, 5000);
            }
        }

        private void handleModemExitSearching(Message message) {
            HwAbsUtils.logD(false, "DefaultState MSG_MODEM_EXIT_SEARCHING_STATE", new Object[0]);
            this.mData = message.getData();
            this.mSubId = this.mData.getInt(HwAbsUtils.SUB_ID);
            if (HwAbsStateMachine.this.removeModemState(this.mSubId) == 0) {
                HwAbsStateMachine.this.mIsAntennaStateInSearch = false;
                HwAbsStateMachine.this.resetCapablity(2);
            }
        }

        private void handleModemEnterSearching(Message message) {
            HwAbsUtils.logD(false, "DefaultState MSG_MODEM_ENTER_SEARCHING_STATE", new Object[0]);
            HwAbsStateMachine.this.mIsAntennaStateInSearch = true;
            HwAbsStateMachine.this.resetCapablity(1);
            this.mData = message.getData();
            this.mSubId = this.mData.getInt(HwAbsUtils.SUB_ID);
            HwAbsStateMachine.this.addModemState(this.mSubId);
        }

        private void handleModemExitConnect() {
            HwAbsUtils.logD(false, "DefaultState MSG_MODEM_EXIT_CONNECT_STATE", new Object[0]);
            if (HwAbsStateMachine.this.mIsAntennaStateInConnect) {
                HwAbsStateMachine.this.mIsAntennaStateInConnect = false;
                HwAbsStateMachine.this.resetCapablity(2);
            }
        }
    }

    class WiFiEnableState extends State {
        WiFiEnableState() {
        }

        public void enter() {
            HwAbsUtils.logD(false, "enter WiFiEnableState", new Object[0]);
            HwAbsStateMachine.this.mHwAbsWifiHandler.setApCapability(HwAbsStateMachine.this.mHwAbsWifiHandler.getCurrentCapability());
            HwAbsUtils.logD(false, "WiFiEnableState send MODEM_TUNERIC_ACTIVE_MSG", new Object[0]);
            HwTelephonyManagerInner.getDefault().notifyCModemStatus(1, HwAbsStateMachine.this.mActiveCallback);
            HwAbsStateMachine.this.mResendModemTunericActiveTime = 0;
            HwAbsStateMachine.this.setBlacklistBssid();
        }

        public boolean processMessage(Message message) {
            int i = message.what;
            if (i == 3) {
                HwAbsUtils.logD(false, "WiFiEnableState MSG_WIFI_ENABLED", new Object[0]);
                return true;
            } else if (i != 4) {
                return false;
            } else {
                HwAbsUtils.logD(false, "WiFiDisconnectedState MSG_WIFI_DISABLE", new Object[0]);
                return false;
            }
        }

        public void exit() {
            HwAbsUtils.logD(false, "exit WiFiEnableState", new Object[0]);
        }
    }

    static class WiFiDisconnectedState extends State {
        WiFiDisconnectedState() {
        }

        public void enter() {
            HwAbsUtils.logD(false, "enter WiFiDisconnectedState", new Object[0]);
        }

        public boolean processMessage(Message message) {
            int i = message.what;
            if (i == 2) {
                HwAbsUtils.logD(false, "WiFiDisconnectedState MSG_WIFI_DISCONNECTED", new Object[0]);
                return true;
            } else if (i != 4) {
                return false;
            } else {
                HwAbsUtils.logD(false, "WiFiDisconnectedState MSG_WIFI_DISABLE", new Object[0]);
                return false;
            }
        }

        public void exit() {
            HwAbsUtils.logD(false, "exit WiFiDisconnectedState", new Object[0]);
        }
    }

    class WiFiDisableState extends State {
        WiFiDisableState() {
        }

        public void enter() {
            HwAbsUtils.logD(false, "enter WiFiDisableState mAbsHandoverTime = %{public}d", Integer.valueOf(HwAbsStateMachine.this.mAbsHandoverTime));
            if (HwAbsStateMachine.this.isScreenOn()) {
                HwAbsStateMachine.this.mAbsHandoverTime = 0;
            }
            HwAbsUtils.logD(false, "WiFiDisableState send MODEM_TUNERIC_IACTIVE_MSG", new Object[0]);
            HwTelephonyManagerInner.getDefault().notifyCModemStatus(0, HwAbsStateMachine.this.mIactiveCallback);
            HwAbsStateMachine.this.mResendModemTunericIActiveTime = 0;
        }

        public boolean processMessage(Message message) {
            int i = message.what;
            if (i == 1 || i == 2 || i == 4) {
                HwAbsUtils.logD(false, "WiFiDisableState handle message.what = %{public}d", Integer.valueOf(message.what));
                return true;
            }
            HwAbsUtils.logD(false, "WiFiDisableState message.what = %{public}d", Integer.valueOf(message.what));
            return false;
        }

        public void exit() {
            HwAbsUtils.logD(false, "exit WiFiDisableState", new Object[0]);
        }
    }

    class WiFiConnectedState extends State {
        private static final int GET_AP_MIMO_CAPABILITY_MAX_TIME = 3;
        private static final int MSG_WIFI_CONNECTED_DELAY = 2000;
        private int mGetApMimoCapabilityTime = 0;

        WiFiConnectedState() {
        }

        public void enter() {
            HwAbsUtils.logD(false, "enter WiFiConnectedState", new Object[0]);
            this.mGetApMimoCapabilityTime = 0;
        }

        public boolean processMessage(Message message) {
            int i = message.what;
            if (i != 1) {
                switch (i) {
                    case 17:
                        HwAbsStateMachine.this.mHwAbsWifiHandler.hwAbsCheckLinked();
                        break;
                    case 18:
                        HwAbsUtils.logE(false, "WiFiConnectedState MSG_WIFI_CHECK_LINK_SUCCESS", new Object[0]);
                        if (!HwAbsStateMachine.this.isUsingMimoCapability()) {
                            HwAbsStateMachine hwAbsStateMachine = HwAbsStateMachine.this;
                            hwAbsStateMachine.transitionTo(hwAbsStateMachine.mSisoState);
                            break;
                        } else {
                            HwAbsStateMachine hwAbsStateMachine2 = HwAbsStateMachine.this;
                            hwAbsStateMachine2.transitionTo(hwAbsStateMachine2.mMimoState);
                            break;
                        }
                    case 19:
                        handleWifiCheckLinkFailed();
                        break;
                    default:
                        return false;
                }
            } else {
                WifiInfo mWifiInfo = handleMimoCapability();
                if (mWifiInfo != null) {
                    if (!ScanResult.is24GHz(mWifiInfo.getFrequency()) || HwAbsStateMachine.this.isMobileAp()) {
                        HwAbsUtils.logE(false, " It is a 5G AP or moblie AP", new Object[0]);
                        HwAbsStateMachine.this.resetCapablity(2);
                    } else {
                        HwAbsApInfoData data = getHwAbsApInfoData(mWifiInfo);
                        if (data != null) {
                            handleBlacklist(data);
                            if (HwAbsStateMachine.this.isUsingMimoCapability()) {
                                HwAbsStateMachine hwAbsStateMachine3 = HwAbsStateMachine.this;
                                hwAbsStateMachine3.transitionTo(hwAbsStateMachine3.mMimoState);
                            } else {
                                HwAbsStateMachine hwAbsStateMachine4 = HwAbsStateMachine.this;
                                hwAbsStateMachine4.transitionTo(hwAbsStateMachine4.mSisoState);
                            }
                        }
                    }
                }
            }
            return true;
        }

        private void handleWifiCheckLinkFailed() {
            HwAbsUtils.logE(false, "WiFiConnectedState MSG_WIFI_CHECK_LINK_FAILED", new Object[0]);
            WifiInfo wifiInfo = HwAbsStateMachine.this.mWifiManager.getConnectionInfo();
            if (wifiInfo == null || wifiInfo.getBSSID() == null) {
                HwAbsUtils.logE(false, "MSG_WIFI_CHECK_LINK_FAILED error ", new Object[0]);
                return;
            }
            HwAbsApInfoData hwAbsApInfoData = HwAbsStateMachine.this.mHwAbsDataBaseManager.getApInfoByBssid(wifiInfo.getBSSID());
            if (hwAbsApInfoData != null) {
                hwAbsApInfoData.mSwitchSisoType = 2;
                HwAbsStateMachine.this.mHwAbsDataBaseManager.addOrUpdateApInfos(hwAbsApInfoData);
                HwAbsStateMachine.this.hwAbsWifiHandover(1);
            }
        }

        private void handleBlacklist(HwAbsApInfoData data) {
            if (data.mInBlackList == 1 && HwAbsStateMachine.this.mHwAbsWifiHandler.getCurrentCapability() == 2) {
                HwAbsUtils.logD(false, "current AP is in blackList reset capability", new Object[0]);
                HwAbsStateMachine.this.mHwAbsWifiHandler.setApCapability(1);
                HwAbsStateMachine.this.setBlacklistBssid();
            }
        }

        private HwAbsApInfoData getHwAbsApInfoData(WifiInfo mWifiInfo) {
            HwAbsApInfoData data = HwAbsStateMachine.this.mHwAbsDataBaseManager.getApInfoByBssid(mWifiInfo.getBSSID());
            if (data == null) {
                data = initApInfoData(mWifiInfo);
                if (data == null) {
                    return null;
                }
            } else {
                data.mLastConnectTime = System.currentTimeMillis();
            }
            HwAbsStateMachine.this.mHwAbsDataBaseManager.addOrUpdateApInfos(data);
            HwAbsUtils.logD(false, "now capability = %{public}d", Integer.valueOf(HwAbsStateMachine.this.mHwAbsWifiHandler.getCurrentCapability()));
            return data;
        }

        private WifiInfo handleMimoCapability() {
            int i;
            HwAbsUtils.logD(false, "WiFiConnectedState MSG_WIFI_CONNECTED", new Object[0]);
            WifiInfo mWifiInfo = HwAbsStateMachine.this.mWifiManager.getConnectionInfo();
            if (mWifiInfo == null || mWifiInfo.getBSSID() == null) {
                HwAbsUtils.logE(false, "WiFiConnectedState error ", new Object[0]);
                return null;
            }
            int mimoCapability = HwAbsStateMachine.this.isApSupportMimoCapability(mWifiInfo.getBSSID());
            if (mimoCapability == -1) {
                HwAbsUtils.logD(false, "isApSupportMimoCapability mNetworkDetail == null", new Object[0]);
                if (HwAbsStateMachine.this.mHwAbsDataBaseManager.getApInfoByBssid(mWifiInfo.getBSSID()) == null) {
                    HwAbsUtils.logE(false, " It is a hidden AP,delay get scan result mGetApMimoCapabilityTime = %{public}d", Integer.valueOf(this.mGetApMimoCapabilityTime));
                    if (!HwAbsStateMachine.this.hasMessages(1) && (i = this.mGetApMimoCapabilityTime) < 3) {
                        this.mGetApMimoCapabilityTime = i + 1;
                        HwAbsStateMachine.this.sendMessageDelayed(1, 2000);
                    }
                    return null;
                }
            } else if (mimoCapability == 0) {
                HwAbsUtils.logE(false, " It is a siso AP", new Object[0]);
                return null;
            }
            return mWifiInfo;
        }

        public void exit() {
            HwAbsUtils.logD(false, "exit WiFiConnectedState", new Object[0]);
        }

        private HwAbsApInfoData initApInfoData(WifiInfo wifiInfo) {
            int authType = 0;
            WifiConfiguration wifiConfiguration = getCurrentConfig(wifiInfo);
            if (wifiConfiguration != null && wifiConfiguration.allowedKeyManagement.cardinality() <= 1) {
                authType = wifiConfiguration.getAuthType();
            }
            return new HwAbsApInfoData(wifiInfo.getBSSID(), HwAbsChrManager.getApSsid(wifiInfo), 2, 2, authType, 0, 0, 0, 0, System.currentTimeMillis());
        }

        private WifiConfiguration getCurrentConfig(WifiInfo wifiInfo) {
            List<WifiConfiguration> configNetworks = HwAbsStateMachine.this.mWifiManager.getConfiguredNetworks();
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
            if (config == null || config.SSID == null || config.allowedKeyManagement.cardinality() > 1) {
                return false;
            }
            return true;
        }
    }

    class MimoState extends State {
        private static final int DEFAULT_MSG_DELAY_SWITCH_TIME = 1000;
        private String mCurrentBssid = null;
        private String mCurrentSsid = null;
        Bundle mData = null;
        int mSubId = -1;

        MimoState() {
        }

        public void enter() {
            HwAbsUtils.logD(false, "enter MimoState", new Object[0]);
            HwAbsStateMachine.this.setWifiAntennaMonitor(true);
            HwAbsStateMachine.this.mAbsMimoStartTime = System.currentTimeMillis();
            if (HwAbsStateMachine.this.isScreenOn()) {
                HwAbsStateMachine.this.mAbsMimoScreenOnStartTime = System.currentTimeMillis();
            }
            WifiInfo wifiInfo = HwAbsStateMachine.this.mWifiManager.getConnectionInfo();
            if (wifiInfo != null && wifiInfo.getSSID() != null && wifiInfo.getBSSID() != null) {
                this.mCurrentSsid = HwAbsChrManager.getApSsid(wifiInfo);
                this.mCurrentBssid = wifiInfo.getBSSID();
            }
        }

        public boolean processMessage(Message message) {
            int i = message.what;
            if (i == 1) {
                handleWifiConnected();
            } else if (i == 2) {
                handleWifiDisconnected();
            } else if (i == 5) {
                handleScreenOn();
            } else if (i == 6) {
                handleScreenOff();
            } else if (i == 7) {
                handleOutgoingCall();
            } else if (i == 9) {
                handleCallStateRinging();
            } else if (i == 23) {
                handleDelaySwitch();
            } else if (i != 24) {
                switch (i) {
                    case 11:
                        handleModemEnterLongConnectState();
                        break;
                    case 12:
                        handleModemEnterShortConnectState();
                        break;
                    case 13:
                        handleModemExitConnectState();
                        break;
                    case 14:
                        handleModemEnterSearchingState(message);
                        break;
                    case 15:
                        handleModemExitSearchingState(message);
                        break;
                    case 16:
                        handleWifiAntennaPreempted();
                        break;
                    default:
                        return false;
                }
            } else {
                handleSuppliantComplete();
            }
            return true;
        }

        private void handleOutgoingCall() {
            HwAbsUtils.logE(false, "MimoState MSG_OUTGOING_CALL isAirModeOn =  %{public}s", String.valueOf(HwAbsStateMachine.this.isAirModeOn()));
            if (!HwAbsStateMachine.this.isAirModeOn()) {
                HwAbsStateMachine.this.mIsAntennaStateInCall = true;
                if (!HwAbsStateMachine.this.mHwAbsWifiScenario.isSupInCompleteState() || HwAbsStateMachine.this.mHwAbsWifiHandler.isAbsHandover()) {
                    HwAbsStateMachine.this.mSwitchType = 7;
                    HwAbsStateMachine.this.mSwitchEvent = 6;
                    HwAbsStateMachine.this.sendMessageDelayed(23, 1000);
                    return;
                }
                if (HwAbsStateMachine.this.mHwAbsWifiHandler.isNeedHandover()) {
                    HwAbsStateMachine.this.updateAbsAssociateTimes(1, 0);
                    HwAbsStateMachine.this.mHwAbsChrManager.initAbsHandoverException(7);
                    HwAbsStateMachine.this.mHwAbsChrManager.increaseEventStatistics(6);
                    HwAbsStateMachine.this.mHwAbsWifiHandler.hwAbsHandover(1);
                } else {
                    HwAbsStateMachine.this.mHwAbsWifiHandler.setApCapability(1);
                    HwAbsStateMachine.this.mHwAbsWifiHandler.setAbsCurrentState(1);
                }
                HwAbsStateMachine hwAbsStateMachine = HwAbsStateMachine.this;
                hwAbsStateMachine.transitionTo(hwAbsStateMachine.mSisoState);
            }
        }

        private void handleCallStateRinging() {
            HwAbsUtils.logD(false, "MimoState MSG_CALL_STATE_RINGING isAirModeOn =  %{public}s", String.valueOf(HwAbsStateMachine.this.isAirModeOn()));
            if (!HwAbsStateMachine.this.isAirModeOn()) {
                HwAbsStateMachine.this.mIsAntennaStateInCall = true;
                if (!HwAbsStateMachine.this.mHwAbsWifiScenario.isSupInCompleteState() || HwAbsStateMachine.this.mHwAbsWifiHandler.isAbsHandover()) {
                    HwAbsStateMachine.this.mSwitchType = 6;
                    HwAbsStateMachine.this.mSwitchEvent = 6;
                    HwAbsStateMachine.this.sendMessageDelayed(23, 1000);
                    return;
                }
                if (HwAbsStateMachine.this.mHwAbsWifiHandler.isNeedHandover()) {
                    HwAbsStateMachine.this.updateAbsAssociateTimes(1, 0);
                    HwAbsStateMachine.this.mHwAbsChrManager.initAbsHandoverException(6);
                    HwAbsStateMachine.this.mHwAbsChrManager.increaseEventStatistics(6);
                    HwAbsStateMachine.this.mHwAbsWifiHandler.hwAbsHandover(1);
                } else {
                    HwAbsStateMachine.this.mHwAbsWifiHandler.setApCapability(1);
                    HwAbsStateMachine.this.mHwAbsWifiHandler.setAbsCurrentState(1);
                }
                HwAbsStateMachine hwAbsStateMachine = HwAbsStateMachine.this;
                hwAbsStateMachine.transitionTo(hwAbsStateMachine.mSisoState);
            }
        }

        private void handleModemEnterLongConnectState() {
            HwAbsUtils.logD(false, "MimoState MSG_MODEM_ENTER_CONNECT_STATE", new Object[0]);
            HwAbsStateMachine.this.mIsAntennaStateInConnect = true;
            if (!HwAbsStateMachine.this.mHwAbsWifiScenario.isSupInCompleteState() || HwAbsStateMachine.this.mHwAbsWifiHandler.isAbsHandover()) {
                HwAbsStateMachine.this.mSwitchType = 1;
                HwAbsStateMachine.this.mSwitchEvent = 1;
                HwAbsStateMachine.this.sendMessageDelayed(23, 1000);
                return;
            }
            if (HwAbsStateMachine.this.mHwAbsWifiHandler.isNeedHandover()) {
                HwAbsStateMachine.this.updateAbsAssociateTimes(1, 0);
                HwAbsStateMachine.this.mHwAbsChrManager.initAbsHandoverException(1);
                HwAbsStateMachine.this.mHwAbsChrManager.increaseEventStatistics(1);
                HwAbsStateMachine.this.hwAbsWifiHandover(1);
            } else {
                HwAbsStateMachine.this.mHwAbsWifiHandler.setApCapability(1);
                HwAbsStateMachine.this.mHwAbsWifiHandler.setAbsCurrentState(1);
            }
            HwAbsStateMachine hwAbsStateMachine = HwAbsStateMachine.this;
            hwAbsStateMachine.transitionTo(hwAbsStateMachine.mSisoState);
        }

        private void handleModemEnterShortConnectState() {
            HwAbsUtils.logD(false, "MimoState MSG_MODEM_ENTER_CONNECT_STATE", new Object[0]);
            HwAbsStateMachine.this.mIsAntennaStateInConnect = true;
            if (!HwAbsStateMachine.this.mHwAbsWifiScenario.isSupInCompleteState() || HwAbsStateMachine.this.mHwAbsWifiHandler.isAbsHandover()) {
                HwAbsStateMachine.this.mSwitchType = 2;
                HwAbsStateMachine.this.mSwitchEvent = 2;
                HwAbsStateMachine.this.sendMessageDelayed(23, 1000);
                return;
            }
            if (HwAbsStateMachine.this.mHwAbsWifiHandler.isNeedHandover()) {
                HwAbsStateMachine.this.updateAbsAssociateTimes(1, 0);
                HwAbsStateMachine.this.mHwAbsChrManager.initAbsHandoverException(2);
                HwAbsStateMachine.this.mHwAbsChrManager.increaseEventStatistics(2);
                HwAbsStateMachine.this.hwAbsWifiHandover(1);
            } else {
                HwAbsStateMachine.this.mHwAbsWifiHandler.setApCapability(1);
                HwAbsStateMachine.this.mHwAbsWifiHandler.setAbsCurrentState(1);
            }
            HwAbsStateMachine hwAbsStateMachine = HwAbsStateMachine.this;
            hwAbsStateMachine.transitionTo(hwAbsStateMachine.mSisoState);
        }

        private void handleModemEnterSearchingState(Message message) {
            HwAbsUtils.logE(false, "MimoState MSG_MODEM_ENTER_SEARCHING_STATE", new Object[0]);
            HwAbsStateMachine.this.mIsAntennaStateInSearch = true;
            this.mSubId = message.getData().getInt(HwAbsUtils.SUB_ID);
            HwAbsStateMachine.this.addModemState(this.mSubId);
            if (!HwAbsStateMachine.this.mHwAbsWifiScenario.isSupInCompleteState() || HwAbsStateMachine.this.mHwAbsWifiHandler.isAbsHandover()) {
                HwAbsStateMachine.this.mSwitchType = 3;
                HwAbsStateMachine.this.mSwitchEvent = 3;
                HwAbsStateMachine.this.sendMessageDelayed(23, 1000);
                return;
            }
            if (HwAbsStateMachine.this.mHwAbsWifiHandler.isNeedHandover()) {
                HwAbsStateMachine.this.updateAbsAssociateTimes(1, 0);
                HwAbsStateMachine.this.mHwAbsChrManager.initAbsHandoverException(3);
                HwAbsStateMachine.this.mHwAbsChrManager.increaseEventStatistics(3);
                HwAbsStateMachine.this.hwAbsWifiHandover(1);
            } else {
                HwAbsStateMachine.this.mHwAbsWifiHandler.setApCapability(1);
                HwAbsStateMachine.this.mHwAbsWifiHandler.setAbsCurrentState(1);
            }
            HwAbsStateMachine hwAbsStateMachine = HwAbsStateMachine.this;
            hwAbsStateMachine.transitionTo(hwAbsStateMachine.mSisoState);
        }

        private void handleModemExitConnectState() {
            HwAbsUtils.logE(false, "MimoState MSG_MODEM_EXIT_CONNECT_STATE mIsAntennaStateInConnect = %{public}s", String.valueOf(HwAbsStateMachine.this.mIsAntennaStateInConnect));
            if (HwAbsStateMachine.this.mIsAntennaStateInConnect) {
                HwAbsStateMachine.this.mIsAntennaStateInConnect = false;
            }
        }

        private void handleModemExitSearchingState(Message message) {
            HwAbsUtils.logE(false, "Mimo MSG_MODEM_EXIT_SEARCHING_STATE mModemStateList.size() == %{public}d", Integer.valueOf(HwAbsStateMachine.this.mModemStateList.size()));
            if (!HwAbsStateMachine.this.mModemStateList.isEmpty()) {
                this.mData = message.getData();
                this.mSubId = this.mData.getInt(HwAbsUtils.SUB_ID);
                if (HwAbsStateMachine.this.removeModemState(this.mSubId) == 0) {
                    HwAbsStateMachine.this.mIsAntennaStateInSearch = false;
                }
            }
        }

        private void handleWifiAntennaPreempted() {
            HwAbsUtils.logE(false, "MimoState MSG_WIFI_ANTENNA_PREEMPTED", new Object[0]);
            HwAbsStateMachine.this.mIsAntennaStateInPreempted = true;
            if (HwAbsStateMachine.this.isScreenOn()) {
                HwAbsStateMachine.this.mSwitchType = 4;
                HwAbsStateMachine.this.mSwitchEvent = 4;
            } else {
                HwAbsStateMachine.this.mSwitchType = 5;
                HwAbsStateMachine.this.mSwitchEvent = 5;
            }
            handleAbsWifi();
        }

        private void handleAbsWifi() {
            if (!HwAbsStateMachine.this.mHwAbsWifiScenario.isSupInCompleteState() || HwAbsStateMachine.this.mHwAbsWifiHandler.isAbsHandover()) {
                HwAbsStateMachine.this.sendMessageDelayed(23, 1000);
                return;
            }
            if (HwAbsStateMachine.this.mHwAbsWifiHandler.isNeedHandover()) {
                HwAbsStateMachine.this.updateAbsAssociateTimes(1, 0);
                HwAbsStateMachine.this.mHwAbsChrManager.initAbsHandoverException(HwAbsStateMachine.this.mSwitchType);
                HwAbsStateMachine.this.mHwAbsChrManager.increaseEventStatistics(HwAbsStateMachine.this.mSwitchEvent);
                HwAbsStateMachine.this.hwAbsWifiHandover(1);
            } else {
                HwAbsStateMachine.this.mHwAbsWifiHandler.setApCapability(1);
                HwAbsStateMachine.this.mHwAbsWifiHandler.setAbsCurrentState(1);
            }
            HwAbsStateMachine hwAbsStateMachine = HwAbsStateMachine.this;
            hwAbsStateMachine.transitionTo(hwAbsStateMachine.mSisoState);
        }

        private void handleScreenOff() {
            HwAbsUtils.logD(false, "MimoState MSG_SCREEN_OFF", new Object[0]);
            if (HwAbsStateMachine.this.mAbsMimoScreenOnStartTime != 0) {
                HwAbsStateMachine.this.mHwAbsChrManager.updateAbsTime(this.mCurrentSsid, 0, 0, System.currentTimeMillis() - HwAbsStateMachine.this.mAbsMimoScreenOnStartTime, 0);
                HwAbsStateMachine.this.mAbsMimoScreenOnStartTime = 0;
            }
        }

        private void handleScreenOn() {
            HwAbsUtils.logD(false, "MimoState MSG_SCREEN_ON", new Object[0]);
            HwAbsStateMachine.this.mAbsMimoScreenOnStartTime = System.currentTimeMillis();
        }

        private void handleWifiConnected() {
            HwAbsUtils.logD(false, "MimoState MSG_WIFI_CONNECTED mIsSupportVoWifi = %{public}s", String.valueOf(HwAbsStateMachine.this.mIsSupportVoWifi));
            if (HwAbsStateMachine.this.mHwAbsWifiHandler.isAbsHandover()) {
                if (!HwAbsStateMachine.this.mIsSupportVoWifi || !HwAbsStateMachine.this.mHwAbsWifiHandler.isHandoverTimeout()) {
                    HwAbsStateMachine.this.updateAbsAssociateSuccess();
                } else {
                    HwAbsUtils.logE(false, "MimoState MSG_WIFI_CONNECTED handover timeout", new Object[0]);
                    HwAbsStateMachine.this.updateAbsAssociateTimes(0, 1);
                }
                HwAbsStateMachine.this.mHwAbsWifiHandler.setAbsHandover(false);
            }
        }

        private void handleWifiDisconnected() {
            HwAbsUtils.logD(false, "MimoState MSG_WIFI_DISCONNECTED", new Object[0]);
            if (HwAbsStateMachine.this.mHwAbsWifiHandler.isAbsHandover()) {
                HwAbsStateMachine.this.mHwAbsWifiHandler.setAbsHandover(false);
                HwAbsStateMachine.this.mHwAbsChrManager.uploadAbsReassociateExeption();
                HwAbsStateMachine.this.updateAbsAssociateTimes(0, 1);
            }
            HwAbsStateMachine hwAbsStateMachine = HwAbsStateMachine.this;
            hwAbsStateMachine.transitionTo(hwAbsStateMachine.mWifiDisconnectedState);
        }

        private void handleDelaySwitch() {
            HwAbsUtils.logD(false, "MIMO MSG_DELAY_SWITCH mIsAntennaStateInCall = %{public}s mIsAntennaStateInSearch = %{public}s mIsAntennaStateInConnect = %{public}s mIsAntennaStateInPreempted = %{public}s", String.valueOf(HwAbsStateMachine.this.mIsAntennaStateInCall), String.valueOf(HwAbsStateMachine.this.mIsAntennaStateInSearch), String.valueOf(HwAbsStateMachine.this.mIsAntennaStateInConnect), String.valueOf(HwAbsStateMachine.this.mIsAntennaStateInPreempted));
            if (HwAbsStateMachine.this.mIsAntennaStateInCall || HwAbsStateMachine.this.mIsAntennaStateInSearch || HwAbsStateMachine.this.mIsAntennaStateInConnect || HwAbsStateMachine.this.mIsAntennaStateInPreempted) {
                HwAbsUtils.logD(false, "MIMO MSG_DELAY_SWITCH mSwitchType=%{public}d mSwitchEvent=%{public}d", Integer.valueOf(HwAbsStateMachine.this.mSwitchType), Integer.valueOf(HwAbsStateMachine.this.mSwitchEvent));
                handleAbsWifi();
            }
        }

        public void exit() {
            HwAbsUtils.logD(false, "exit MimoState", new Object[0]);
            HwAbsStateMachine.this.removeMessages(23);
            long mimoScreenOnTime = 0;
            long mimoTime = System.currentTimeMillis() - HwAbsStateMachine.this.mAbsMimoStartTime;
            if (HwAbsStateMachine.this.mAbsMimoScreenOnStartTime != 0) {
                mimoScreenOnTime = System.currentTimeMillis() - HwAbsStateMachine.this.mAbsMimoScreenOnStartTime;
            }
            HwAbsStateMachine.this.mHwAbsChrManager.updateAbsTime(this.mCurrentSsid, mimoTime, 0, mimoScreenOnTime, 0);
            HwAbsStateMachine.this.mAbsMimoScreenOnStartTime = 0;
            HwAbsStateMachine.this.mAbsMimoStartTime = 0;
        }

        private void handleSuppliantComplete() {
            WifiInfo wifiInfo = HwAbsStateMachine.this.mWifiManager.getConnectionInfo();
            if (wifiInfo != null && wifiInfo.getSSID() != null && wifiInfo.getBSSID() != null) {
                if (this.mCurrentBssid.equals(wifiInfo.getBSSID()) || !this.mCurrentSsid.equals(HwAbsChrManager.getApSsid(wifiInfo))) {
                    if (HwAbsStateMachine.this.mHwAbsWifiHandler.isAbsHandover() && this.mCurrentBssid.equals(wifiInfo.getBSSID())) {
                        HwAbsUtils.logD(false, "mimo reassociate success", new Object[0]);
                        HwAbsWiFiHandler hwAbsWiFiHandler = HwAbsStateMachine.this.mHwAbsWifiHandler;
                        HwAbsWiFiHandler unused = HwAbsStateMachine.this.mHwAbsWifiHandler;
                        hwAbsWiFiHandler.setTargetBssid(HwAbsWiFiHandler.SUPPLICANT_BSSID_ANY);
                        HwAbsStateMachine.this.sendMessage(1);
                    }
                } else if (!HwAbsStateMachine.this.isApInDatabase(wifiInfo.getBSSID())) {
                    Message msg = Message.obtain();
                    msg.what = 1;
                    HwAbsStateMachine.this.deferMessage(msg);
                    HwAbsStateMachine hwAbsStateMachine = HwAbsStateMachine.this;
                    hwAbsStateMachine.transitionTo(hwAbsStateMachine.mWifiConnectedState);
                } else {
                    HwAbsStateMachine hwAbsStateMachine2 = HwAbsStateMachine.this;
                    hwAbsStateMachine2.transitionTo(hwAbsStateMachine2.mMimoState);
                }
            }
        }
    }

    class SisoState extends State {
        private String mCurrentBssid = null;
        private String mCurrentSsid = null;

        SisoState() {
        }

        public void enter() {
            HwAbsUtils.logD(false, "enter SisoState mIsAntennaStateInPreempted = %{public}s", String.valueOf(HwAbsStateMachine.this.mIsAntennaStateInPreempted));
            HwAbsStateMachine.this.setWifiAntennaMonitor(true);
            if (!HwAbsStateMachine.this.isScreenOn()) {
                HwAbsStateMachine.this.mAbsSisoScreenOnStartTime = System.currentTimeMillis();
            }
            HwAbsStateMachine.this.mAbsSisoStartTime = System.currentTimeMillis();
            WifiInfo wifiInfo = HwAbsStateMachine.this.mWifiManager.getConnectionInfo();
            if (!(wifiInfo == null || wifiInfo.getSSID() == null || wifiInfo.getBSSID() == null)) {
                this.mCurrentSsid = HwAbsChrManager.getApSsid(wifiInfo);
                this.mCurrentBssid = wifiInfo.getBSSID();
            }
            if (HwAbsStateMachine.this.mIsAntennaStateInPreempted && HwAbsStateMachine.this.isModemStateInIdle()) {
                HwAbsStateMachine.this.mIsAntennaStateInPreempted = false;
                HwAbsStateMachine.this.handoverToMimo();
            }
        }

        public boolean processMessage(Message message) {
            int i = message.what;
            if (i == 1) {
                handleWifiConnected();
            } else if (i == 2) {
                handleWifiDisconnected();
            } else if (i == 22) {
                HwAbsStateMachine.this.handlePowerOffMessage();
                if (HwAbsStateMachine.this.isModemStateInIdle()) {
                    HwAbsStateMachine.this.handoverToMimo();
                }
            } else if (i == 24) {
                handleSuppComplete();
            } else if (i == 101) {
                handleWifiSwitchMimo();
            } else if (i != 103) {
                switch (i) {
                    case 5:
                        handleScreenOn();
                        break;
                    case 6:
                        handleScreenOff();
                        break;
                    case 7:
                    case 9:
                        HwAbsUtils.logD(false, "siso in or out call", new Object[0]);
                        HwAbsStateMachine.this.mIsAntennaStateInCall = true;
                        if (HwAbsStateMachine.this.mIsAntennaStateInConnect) {
                            HwAbsStateMachine.this.resetAbsHandoverTimes();
                            break;
                        }
                        break;
                    case 8:
                        handleIdleState();
                        break;
                    default:
                        switch (i) {
                            case 11:
                            case 12:
                                HwAbsUtils.logE(false, "SisoState MSG_MODEM_ENTER_CONNECT_STATE", new Object[0]);
                                HwAbsStateMachine.this.mIsAntennaStateInConnect = true;
                                break;
                            case 13:
                                handleModemExitConnectState();
                                break;
                            case 14:
                                handleModemEnterSearching(message);
                                break;
                            case 15:
                                handleModemExitSearching(message);
                                break;
                            default:
                                return false;
                        }
                }
            } else {
                HwAbsUtils.logD(false, "SiSOState CMD_WIFI_PAUSE_HANDOVER", new Object[0]);
                HwAbsStateMachine.this.mIsPauseHandover = false;
                HwAbsStateMachine.this.handoverToMimo();
            }
            return true;
        }

        private void handleSuppComplete() {
            WifiInfo wifiInfo = HwAbsStateMachine.this.mWifiManager.getConnectionInfo();
            if (wifiInfo != null && wifiInfo.getSSID() != null && wifiInfo.getBSSID() != null) {
                if (this.mCurrentBssid.equals(wifiInfo.getBSSID()) || !this.mCurrentSsid.equals(HwAbsChrManager.getApSsid(wifiInfo))) {
                    if (HwAbsStateMachine.this.mHwAbsWifiHandler.isAbsHandover() && this.mCurrentBssid.equals(wifiInfo.getBSSID())) {
                        HwAbsUtils.logD(false, "siso reassociate success", new Object[0]);
                        HwAbsWiFiHandler hwAbsWiFiHandler = HwAbsStateMachine.this.mHwAbsWifiHandler;
                        HwAbsWiFiHandler unused = HwAbsStateMachine.this.mHwAbsWifiHandler;
                        hwAbsWiFiHandler.setTargetBssid(HwAbsWiFiHandler.SUPPLICANT_BSSID_ANY);
                        HwAbsStateMachine.this.sendMessage(1);
                    }
                } else if (!HwAbsStateMachine.this.isApInDatabase(wifiInfo.getBSSID())) {
                    Message msg = Message.obtain();
                    msg.what = 1;
                    HwAbsStateMachine.this.deferMessage(msg);
                    HwAbsStateMachine hwAbsStateMachine = HwAbsStateMachine.this;
                    hwAbsStateMachine.transitionTo(hwAbsStateMachine.mWifiConnectedState);
                } else {
                    HwAbsStateMachine hwAbsStateMachine2 = HwAbsStateMachine.this;
                    hwAbsStateMachine2.transitionTo(hwAbsStateMachine2.mSisoState);
                }
            }
        }

        private void handleWifiDisconnected() {
            HwAbsUtils.logD(false, "SiSOState MSG_WIFI_DISCONNECTED", new Object[0]);
            if (HwAbsStateMachine.this.mHwAbsWifiHandler.isAbsHandover()) {
                HwAbsStateMachine.this.mHwAbsWifiHandler.setAbsHandover(false);
                HwAbsStateMachine.this.mHwAbsChrManager.uploadAbsReassociateExeption();
                HwAbsStateMachine.this.updateAbsAssociateTimes(0, 1);
            }
            HwAbsStateMachine hwAbsStateMachine = HwAbsStateMachine.this;
            hwAbsStateMachine.transitionTo(hwAbsStateMachine.mWifiDisconnectedState);
        }

        private void handleWifiConnected() {
            HwAbsUtils.logE(false, "SiSOState MSG_WIFI_CONNECTED mIsSupportVoWifi = %{public}s", String.valueOf(HwAbsStateMachine.this.mIsSupportVoWifi));
            if (HwAbsStateMachine.this.mHwAbsWifiHandler.isAbsHandover()) {
                if (!HwAbsStateMachine.this.mIsSupportVoWifi || !HwAbsStateMachine.this.mHwAbsWifiHandler.isHandoverTimeout()) {
                    HwAbsStateMachine.this.updateAbsAssociateSuccess();
                } else {
                    HwAbsUtils.logE(false, "SiSOState MSG_WIFI_CONNECTED handover time out", new Object[0]);
                    HwAbsStateMachine.this.updateAbsAssociateTimes(0, 1);
                }
                HwAbsStateMachine.this.mHwAbsWifiHandler.setAbsHandover(false);
            }
        }

        private void handleWifiSwitchMimo() {
            boolean isModemStateIdle = HwAbsStateMachine.this.isModemStateInIdle();
            boolean isSimCardInService = HwAbsStateMachine.this.isSimCardStatusIdle();
            boolean isInBlacklist = HwAbsStateMachine.this.isApInBlacklist();
            HwAbsUtils.logE(false, "SiSOState CMD_WIFI_SWITCH_MIMO isModemStateInIdle = %{public}s isSimCardInService = %{public}s isInBlacklist = %{public}s", String.valueOf(isModemStateIdle), String.valueOf(isSimCardInService), String.valueOf(isInBlacklist));
            if (!isModemStateIdle || !isSimCardInService || isInBlacklist) {
                HwAbsUtils.logE(false, "SiSOState CMD_WIFI_SWITCH_MIMO keep in SISO", new Object[0]);
            } else if (!HwAbsStateMachine.this.mHwAbsWifiScenario.isSupInCompleteState() || HwAbsStateMachine.this.mHwAbsWifiHandler.isAbsHandover()) {
                HwAbsStateMachine.this.sendMessageDelayed(101, 1000);
            } else {
                if (HwAbsStateMachine.this.mHwAbsWifiHandler.isNeedHandover()) {
                    HwAbsStateMachine.this.mHwAbsChrManager.initAbsHandoverException(8);
                    HwAbsStateMachine.this.mHwAbsChrManager.increaseEventStatistics(7);
                    HwAbsStateMachine.this.updateAbsAssociateTimes(1, 0);
                    HwAbsStateMachine.this.hwAbsWifiHandover(2);
                } else {
                    HwAbsStateMachine.this.mHwAbsWifiHandler.setApCapability(2);
                    HwAbsStateMachine.this.mHwAbsWifiHandler.setAbsCurrentState(2);
                }
                HwAbsStateMachine hwAbsStateMachine = HwAbsStateMachine.this;
                hwAbsStateMachine.transitionTo(hwAbsStateMachine.mMimoState);
            }
        }

        private void handleScreenOn() {
            HwAbsUtils.logD(false, "SiSOState MSG_SCREEN_ON isModemStateInIdle = %{public}s", String.valueOf(HwAbsStateMachine.this.isModemStateInIdle()));
            if (HwAbsStateMachine.this.isModemStateInIdle()) {
                if (HwAbsStateMachine.this.isInPunishTime()) {
                    long mOverPunishTime = HwAbsStateMachine.this.getPunishTime() - (System.currentTimeMillis() - HwAbsStateMachine.this.mAbsLastHandoverTime);
                    HwAbsUtils.logE(false, "SiSOState MSG_SCREEN_ON inpunish time = %{public}s", String.valueOf(mOverPunishTime));
                    if (mOverPunishTime > HwAbsStateMachine.ABS_SCREEN_ON_TIME) {
                        HwAbsStateMachine.this.sendHandoverToMimoMsg(101, mOverPunishTime);
                    } else {
                        HwAbsStateMachine.this.sendHandoverToMimoMsg(101, HwAbsStateMachine.ABS_SCREEN_ON_TIME);
                    }
                } else {
                    HwAbsStateMachine.this.sendHandoverToMimoMsg(101, HwAbsStateMachine.ABS_SCREEN_ON_TIME);
                }
            }
            HwAbsStateMachine.this.mAbsSisoScreenOnStartTime = System.currentTimeMillis();
        }

        private void handleScreenOff() {
            HwAbsUtils.logD(false, "SiSOState MSG_SCREEN_OFF", new Object[0]);
            HwAbsStateMachine.this.removeMessages(101);
            if (HwAbsStateMachine.this.mAbsSisoScreenOnStartTime != 0) {
                HwAbsStateMachine.this.mHwAbsChrManager.updateAbsTime(this.mCurrentSsid, 0, 0, 0, System.currentTimeMillis() - HwAbsStateMachine.this.mAbsSisoScreenOnStartTime);
                HwAbsStateMachine.this.mAbsSisoScreenOnStartTime = 0;
            }
        }

        private void handleModemExitSearching(Message message) {
            HwAbsUtils.logE(false, "SisoState MSG_MODEM_EXIT_SEARCHING_STATE mModemStateList.size() ==%{public}d", Integer.valueOf(HwAbsStateMachine.this.mModemStateList.size()));
            if (!HwAbsStateMachine.this.mModemStateList.isEmpty()) {
                Bundle mData = message.getData();
                int mSubId = mData.getInt(HwAbsUtils.SUB_ID);
                int mResult = mData.getInt(HwAbsUtils.RES);
                if (HwAbsStateMachine.this.removeModemState(mSubId) == 0) {
                    HwAbsStateMachine.this.mIsAntennaStateInSearch = false;
                }
                if (HwAbsStateMachine.this.hasSimCard(mSubId)) {
                    if (mResult != 0 || HwAbsStateMachine.this.mIsAntennaStateInSearch) {
                        HwAbsUtils.logE(false, "SisoState keep stay in siso, have sim card mIsAntennaStateInSearch = %{public}s", String.valueOf(HwAbsStateMachine.this.mIsAntennaStateInSearch));
                    } else {
                        HwAbsStateMachine.this.handoverToMimo();
                    }
                } else if (HwAbsStateMachine.this.mIsAntennaStateInSearch || !(mResult == 0 || mResult == 1)) {
                    HwAbsUtils.logE(false, "SisoState keep stay in siso, have no sim card mIsAntennaStateInSearch = %{public}s", String.valueOf(HwAbsStateMachine.this.mIsAntennaStateInSearch));
                } else {
                    HwAbsStateMachine.this.handoverToMimo();
                }
            }
        }

        private void handleModemEnterSearching(Message message) {
            HwAbsUtils.logE(false, "SisoState MSG_MODEM_ENTER_SEARCHING_STATE", new Object[0]);
            HwAbsStateMachine.this.mIsAntennaStateInSearch = true;
            HwAbsStateMachine.this.removeMessages(101);
            HwAbsStateMachine.this.addModemState(message.getData().getInt(HwAbsUtils.SUB_ID));
        }

        private void handleModemExitConnectState() {
            HwAbsUtils.logE(false, "SisoState MSG_MODEM_EXIT_CONNECT_STATE mIsAntennaStateInConnect = %{public}s", String.valueOf(HwAbsStateMachine.this.mIsAntennaStateInConnect));
            if (HwAbsStateMachine.this.mIsAntennaStateInConnect) {
                HwAbsStateMachine.this.mIsAntennaStateInConnect = false;
                HwAbsStateMachine.this.handoverToMimo();
            }
        }

        private void handleIdleState() {
            HwAbsUtils.logE(false, "SisoState MSG_ANTENNA_STATE_IDLE mIsAntennaStateInCall = %{public}s", String.valueOf(HwAbsStateMachine.this.mIsAntennaStateInCall));
            if (HwAbsStateMachine.this.mIsAntennaStateInCall) {
                HwAbsStateMachine.this.mIsAntennaStateInCall = false;
                HwAbsStateMachine.this.mIsInCallPunish = true;
                HwAbsStateMachine.this.handoverToMimo();
            }
        }

        public void exit() {
            HwAbsUtils.logD(false, "exit SisoState", new Object[0]);
            long sisoScreenOnTime = 0;
            long sisoTime = System.currentTimeMillis() - HwAbsStateMachine.this.mAbsSisoStartTime;
            if (HwAbsStateMachine.this.mAbsSisoScreenOnStartTime != 0) {
                sisoScreenOnTime = System.currentTimeMillis() - HwAbsStateMachine.this.mAbsSisoScreenOnStartTime;
            }
            HwAbsStateMachine.this.mHwAbsChrManager.updateAbsTime(this.mCurrentSsid, 0, sisoTime, 0, sisoScreenOnTime);
            HwAbsStateMachine.this.mAbsSisoScreenOnStartTime = 0;
            HwAbsStateMachine.this.mAbsSisoStartTime = 0;
        }
    }

    public void onStart() {
        this.mHwAbsWifiScenario.startMonitor();
    }

    public boolean isAbsSwitching() {
        HwAbsUtils.logD(false, "isAbsSwitching mIsSwitching = %{public}s", String.valueOf(this.mIsSwitching));
        return this.mIsSwitching;
    }

    private NetworkDetail getNetworkDetail(String bssid) {
        ScanDetail scanDetail;
        NetworkDetail detail = null;
        for (ScanResult result : this.mWifiManager.getScanResults()) {
            if (!(result.BSSID == null || !result.BSSID.equals(bssid) || (scanDetail = ScanResultUtil.toScanDetail(result)) == null)) {
                detail = scanDetail.getNetworkDetail();
            }
        }
        return detail;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isUsingMimoCapability() {
        if (this.mHwAbsWifiHandler.getCurrentCapability() == 2) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int isApSupportMimoCapability(String bssid) {
        NetworkDetail mNetworkDetail = getNetworkDetail(bssid);
        if (mNetworkDetail == null) {
            return -1;
        }
        HwAbsUtils.logD(false, "isApSupportMimoCapability mNetworkDetail.getStream1() = %{public}d mNetworkDetail.getStream2() = %{public}d mNetworkDetail.getStream3() = %{public}d mNetworkDetail.getStream4() = %{public}d", Integer.valueOf(mNetworkDetail.getStream1()), Integer.valueOf(mNetworkDetail.getStream2()), Integer.valueOf(mNetworkDetail.getStream3()), Integer.valueOf(mNetworkDetail.getStream4()));
        return ((mNetworkDetail.getStream1() + mNetworkDetail.getStream2()) + mNetworkDetail.getStream3()) + mNetworkDetail.getStream4() >= 2 ? 1 : 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void hwAbsWifiHandover(int capability) {
        HwAbsUtils.logD(false, "hwAbsWifiHandover capability = %{public}d", Integer.valueOf(capability));
        if (capability == 1) {
            setPunishTime();
            updateAbsHandoverTime();
        }
        this.mHwAbsWifiHandler.hwAbsHandover(capability);
    }

    private void setPunishTime() {
        if (this.mAbsLastHandoverTime == 0 || System.currentTimeMillis() - this.mAbsLastHandoverTime > ABS_INTERVAL_TIME) {
            this.mAbsHandoverTime = 1;
            HwAbsUtils.logD(false, "setPunishTime reset times mAbsHandoverTime = %{public}d", Integer.valueOf(this.mAbsHandoverTime));
        } else {
            this.mAbsHandoverTime++;
            int i = this.mAbsHandoverTime;
            if (i == 10) {
                this.mHwAbsChrManager.increaseEventStatistics(8);
            } else if (i >= 10) {
                updatePingpongChr();
            }
        }
        this.mAbsLastHandoverTime = System.currentTimeMillis();
    }

    private void updatePingpongChr() {
        int i;
        HwAbsChrStatistics record = this.mHwAbsChrManager.getStatisticsInfo();
        if (record != null && record.maxPingPongTimes < (i = this.mAbsHandoverTime)) {
            record.maxPingPongTimes = i;
            this.mHwAbsChrManager.updateChrInfo(record);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean hasSimCard(int subId) {
        int cardState = this.mTelephonyManager.getSimState(subId);
        if (cardState == 5) {
            HwAbsUtils.logD(false, "hasSimCard subID = %{public}d  cardState = SIM_STATE_READY", Integer.valueOf(subId));
            return true;
        }
        HwAbsUtils.logD(false, "hasSimCard subID = %{public}d  cardState = %{public}d", Integer.valueOf(subId), Integer.valueOf(cardState));
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isSimCardStatusIdle() {
        int phoneNum = this.mTelephonyManager.getPhoneCount();
        HwAbsUtils.logD(false, "isSIMCardStatusIdle phoneNum = %{public}d", Integer.valueOf(phoneNum));
        if (phoneNum == 0) {
            return true;
        }
        boolean isCardReady = false;
        int i = 0;
        while (true) {
            if (i >= phoneNum) {
                break;
            } else if (this.mTelephonyManager.getSimState(i) == 5) {
                isCardReady = true;
                break;
            } else {
                i++;
            }
        }
        if (isCardReady) {
            return compareSimStatusWithCardReady(phoneNum);
        }
        HwAbsUtils.logD(false, "isSIMCardStatusIdle return true", new Object[0]);
        return true;
    }

    private boolean compareSimStatusWithCardReady(int cardNum) {
        if (cardNum == 0) {
            return true;
        }
        List<Integer> statusList = new ArrayList<>();
        for (int subId = 0; subId < cardNum; subId++) {
            int cardState = this.mTelephonyManager.getSimState(subId);
            HwAbsUtils.logD(false, "compareSIMStatusWithCardReady subId = %{public}d cardState = %{public}d", Integer.valueOf(subId), Integer.valueOf(cardState));
            if (cardState != 5) {
                statusList.add(2);
            } else {
                ServiceState serviceState = this.mTelephonyManager.getServiceStateForSubscriber(subId);
                if (serviceState == null) {
                    statusList.add(1);
                } else {
                    int voiceState = serviceState.getState();
                    HwAbsUtils.logD(false, "compareSIMStatusWithCardReady subId = %{public}d voiceState = %{public}d", Integer.valueOf(subId), Integer.valueOf(voiceState));
                    if (voiceState == 0 || voiceState == 3) {
                        statusList.add(2);
                    } else {
                        statusList.add(1);
                    }
                }
            }
        }
        for (int i = 0; i < statusList.size(); i++) {
            if (statusList.get(i).intValue() != 2) {
                HwAbsUtils.logD(false, "compareSIMStatusWithCardReady return false", new Object[0]);
                return false;
            }
        }
        HwAbsUtils.logD(false, "compareSIMStatusWithCardReady return true", new Object[0]);
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setWifiAntennaMonitor(boolean enable) {
        if (enable) {
            HwAbsUtils.logD(false, "setWifiAntennaMonitor enable", new Object[0]);
        } else {
            HwAbsUtils.logD(false, "setWifiAntennaMonitor disable", new Object[0]);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isScreenOn() {
        if (((PowerManager) this.mContext.getSystemService("power")).isScreenOn()) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetCapablity(int capablity) {
        HwAbsUtils.logD(false, "resetCapablity capablity = %{public}d", Integer.valueOf(capablity));
        if (capablity != 2) {
            this.mHwAbsWifiHandler.setApCapability(capablity);
            this.mHwAbsWifiHandler.setAbsCurrentState(capablity);
        } else if (isModemStateInIdle() && !isInPunishTime()) {
            this.mHwAbsWifiHandler.setApCapability(capablity);
            this.mHwAbsWifiHandler.setAbsCurrentState(capablity);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isModemStateInIdle() {
        if (this.mIsAntennaStateInCall || this.mIsAntennaStateInSearch || this.mIsAntennaStateInConnect || !isScreenOn() || this.mIsPauseHandover) {
            HwAbsUtils.logD(false, "isModemStateInIdle return false mIsAntennaStateInCall = %{public}s  mIsAntennaStateInSearch = %{public}s  mIsAntennaStateInConnect = %{public}s isScreenOn() = %{public}s mIsPauseHandover = %{public}s", String.valueOf(this.mIsAntennaStateInCall), String.valueOf(this.mIsAntennaStateInSearch), String.valueOf(this.mIsAntennaStateInConnect), String.valueOf(isScreenOn()), String.valueOf(this.mIsPauseHandover));
            return false;
        }
        HwAbsUtils.logD(false, "isModemStateInIdle return true", new Object[0]);
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void addModemState(int subId) {
        HwAbsUtils.logD(false, "addModemState subId = %{public}d", Integer.valueOf(subId));
        if (this.mModemStateList.isEmpty()) {
            this.mModemStateList.add(Integer.valueOf(subId));
        } else {
            for (int i = 0; i < this.mModemStateList.size(); i++) {
                if (this.mModemStateList.get(i).intValue() == subId) {
                    return;
                }
            }
            this.mModemStateList.add(Integer.valueOf(subId));
        }
        HwAbsUtils.logD(false, "addModemState size = %{public}d", Integer.valueOf(this.mModemStateList.size()));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int removeModemState(int subId) {
        HwAbsUtils.logD(false, "removeModemState size = %{public}d subId = %{public}d", Integer.valueOf(this.mModemStateList.size()), Integer.valueOf(subId));
        if (this.mModemStateList.isEmpty()) {
            return 0;
        }
        int flag = -1;
        int i = 0;
        while (true) {
            if (i >= this.mModemStateList.size()) {
                break;
            }
            HwAbsUtils.logD(false, "removeModemState mModemStateList.get(i) = %{public}d subId = %{public}d", this.mModemStateList.get(i), Integer.valueOf(subId));
            if (this.mModemStateList.get(i).intValue() == subId) {
                flag = i;
                break;
            }
            i++;
        }
        if (flag != -1) {
            this.mModemStateList.remove(flag);
        }
        HwAbsUtils.logD(false, "removeModemState size = %{public}d", Integer.valueOf(this.mModemStateList.size()));
        return this.mModemStateList.size();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isInPunishTime() {
        long punishTime = getPunishTime();
        if (System.currentTimeMillis() < this.mAbsLastHandoverTime) {
            this.mAbsLastHandoverTime = System.currentTimeMillis();
        }
        long currentTimeMillis = System.currentTimeMillis();
        long j = this.mAbsLastHandoverTime;
        if (punishTime < currentTimeMillis - j) {
            HwAbsUtils.logD(false, "isInPunishTime is not in punish", new Object[0]);
            return false;
        }
        HwAbsUtils.logD(false, "isInPunishTime is in punish punishTim =%{public}s", String.valueOf((j + punishTime) - System.currentTimeMillis()));
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private long getPunishTime() {
        int i = this.mAbsHandoverTime;
        long punishTime = ((long) (i * i)) * ABS_PUNISH_TIME;
        if (punishTime > ABS_INTERVAL_TIME) {
            return ABS_INTERVAL_TIME;
        }
        return punishTime;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handoverToMimo() {
        HwAbsUtils.logD(false, "handoverToMimo", new Object[0]);
        if (!isModemStateInIdle()) {
            HwAbsUtils.logD(false, "handoverToMimo is not in idle ignore it", new Object[0]);
            return;
        }
        if (hasMessages(101)) {
            removeMessages(101);
            HwAbsUtils.logD(false, "handoverToMimo is already have message remove it", new Object[0]);
        }
        if (isInPunishTime()) {
            long overPunishTime = getPunishTime() - (System.currentTimeMillis() - this.mAbsLastHandoverTime);
            HwAbsUtils.logD(false, "handoverToMimo overPunishTime = %{public}s mIsInCallPunish = %{public}s", String.valueOf(overPunishTime), String.valueOf(this.mIsInCallPunish));
            if (!this.mIsInCallPunish || overPunishTime >= ABS_PUNISH_TIME) {
                sendMessageDelayed(101, overPunishTime);
            } else {
                HwAbsUtils.logD(false, "handoverToMimo reset punish time = %{public}s", String.valueOf((long) ABS_PUNISH_TIME));
                sendMessageDelayed(101, ABS_PUNISH_TIME);
            }
        } else if (this.mIsInCallPunish) {
            HwAbsUtils.logD(false, "handoverToMimo mIsInCallPunish punish time = %{public}s", String.valueOf((long) ABS_PUNISH_TIME));
            sendMessageDelayed(101, ABS_PUNISH_TIME);
        } else {
            sendMessageDelayed(101, 2000);
        }
        this.mIsInCallPunish = false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendHandoverToMimoMsg(int msg, long time) {
        if (hasMessages(msg)) {
            removeMessages(msg);
        }
        sendMessageDelayed(msg, time);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isAirModeOn() {
        Context context = this.mContext;
        if (context != null && Settings.System.getInt(context.getContentResolver(), "airplane_mode_on", 0) == 1) {
            return true;
        }
        return false;
    }

    private List<Integer> getPowerOffSimSubId() {
        List<Integer> subId = new ArrayList<>();
        int phoneNum = this.mTelephonyManager.getPhoneCount();
        HwAbsUtils.logD(false, "getPowerOffSimSubId phoneNum = %{public}d", Integer.valueOf(phoneNum));
        if (phoneNum == 0) {
            return subId;
        }
        for (int i = 0; i < phoneNum; i++) {
            ServiceState serviceState = this.mTelephonyManager.getServiceStateForSubscriber(i);
            if (serviceState != null) {
                int voiceState = serviceState.getState();
                HwAbsUtils.logD(false, "getPowerOffSimSubId subID = %{public}d voiceState = %{public}d", Integer.valueOf(i), Integer.valueOf(voiceState));
                if (voiceState == 3) {
                    subId.add(Integer.valueOf(i));
                }
            }
        }
        return subId;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlePowerOffMessage() {
        if (this.mIsAntennaStateInSearch) {
            List<Integer> list = getPowerOffSimSubId();
            if (list.size() != 0) {
                for (Integer num : list) {
                    removeModemState(num.intValue());
                }
                if (this.mModemStateList.isEmpty()) {
                    this.mIsAntennaStateInSearch = false;
                }
            }
        }
        if (this.mIsAntennaStateInConnect) {
            this.mIsAntennaStateInConnect = false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isMobileAp() {
        if (this.mContext != null) {
            return HwFrameworkFactory.getHwInnerWifiManager().getHwMeteredHint(this.mContext);
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateAbsAssociateTimes(int associateTimes, int associateFailedTimes) {
        String ssid;
        String bssid;
        if (associateTimes == 1) {
            WifiInfo mWifiInfo = this.mWifiManager.getConnectionInfo();
            if (mWifiInfo == null || mWifiInfo.getBSSID() == null || mWifiInfo.getSSID() == null) {
                HwAbsUtils.logE(false, "updateAbsAssociateTimes mWifiInfo error", new Object[0]);
                return;
            }
            bssid = mWifiInfo.getBSSID();
            ssid = HwAbsChrManager.getApSsid(mWifiInfo);
            this.mAssociateSsid = ssid;
            this.mAssociateBssid = bssid;
        } else {
            ssid = this.mAssociateSsid;
            bssid = this.mAssociateBssid;
        }
        HwAbsApInfoData hwAbsApInfoData = this.mHwAbsDataBaseManager.getApInfoByBssid(bssid);
        if (hwAbsApInfoData != null) {
            int blacklistStatus = hwAbsApInfoData.mInBlackList;
            hwAbsApInfoData.mReassociateTimes += associateTimes;
            hwAbsApInfoData.mFailedTimes += associateFailedTimes;
            if (associateFailedTimes != 0) {
                updateAbsAssociateFailedEvent(hwAbsApInfoData);
            }
            this.mHwAbsDataBaseManager.addOrUpdateApInfos(hwAbsApInfoData);
            if (blacklistStatus == 0 && hwAbsApInfoData.mInBlackList == 1) {
                setBlacklistBssid();
                uploadBlacklistException(hwAbsApInfoData);
            }
        } else {
            HwAbsUtils.logE(false, "updateAbsAssociateTimes error!!", new Object[0]);
        }
        this.mHwAbsChrManager.updateChrAssociateTimes(ssid, associateTimes, associateFailedTimes);
    }

    private void updateAbsAssociateFailedEvent(HwAbsApInfoData data) {
        int lowFailedRate;
        int highFailedRate;
        int continuousTimes;
        HwAbsUtils.logE(false, "updateAbsAssociateFailedEvent mIsSupportVoWifi = %{public}s", String.valueOf(this.mIsSupportVoWifi));
        if (!this.mIsSupportVoWifi) {
            continuousTimes = 3;
            highFailedRate = 10;
            lowFailedRate = 30;
        } else {
            continuousTimes = 2;
            highFailedRate = 5;
            lowFailedRate = 15;
        }
        data.mContinuousFailureTimes++;
        if (data.mContinuousFailureTimes >= continuousTimes) {
            HwAbsUtils.logE(false, "updateAbsAssociateFailedEvent mContinuousFailureTimes = %{public}d", Integer.valueOf(data.mContinuousFailureTimes));
            data.mInBlackList = 1;
            this.mAddBlacklistReason = 1;
            return;
        }
        int failedRate = 0;
        if (data.mReassociateTimes > 50) {
            failedRate = highFailedRate;
        } else if (data.mReassociateTimes > 10) {
            failedRate = lowFailedRate;
        }
        int temp = (data.mFailedTimes * 100) / data.mReassociateTimes;
        HwAbsUtils.logD(false, "updateAbsAssociateFailedEvent temp = %{public}d failedRate = %{public}d", Integer.valueOf(temp), Integer.valueOf(failedRate));
        if (failedRate > 0 && temp > failedRate) {
            data.mInBlackList = 1;
            this.mAddBlacklistReason = 2;
        } else if (isHandoverTooMuch(data.mBssid)) {
            HwAbsUtils.logE(false, "updateAbsAssociateFailedEvent isHandoverTooMuch", new Object[0]);
            data.mInBlackList = 1;
            data.mSwitchSisoType = 15;
            this.mAddBlacklistReason = 3;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isApInBlacklist() {
        WifiInfo mWifiInfo = this.mWifiManager.getConnectionInfo();
        if (mWifiInfo == null || mWifiInfo.getBSSID() == null) {
            HwAbsUtils.logE(false, "isApInBlacklist mWifiInfo error", new Object[0]);
            return false;
        }
        HwAbsApInfoData hwAbsApInfoData = this.mHwAbsDataBaseManager.getApInfoByBssid(mWifiInfo.getBSSID());
        if (hwAbsApInfoData == null || hwAbsApInfoData.mInBlackList != 1) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateAbsAssociateSuccess() {
        WifiInfo mWifiInfo = this.mWifiManager.getConnectionInfo();
        if (mWifiInfo == null || mWifiInfo.getBSSID() == null) {
            HwAbsUtils.logE(false, "updateAbsAssociateSuccess mWifiInfo error", new Object[0]);
            return;
        }
        HwAbsApInfoData hwAbsApInfoData = this.mHwAbsDataBaseManager.getApInfoByBssid(mWifiInfo.getBSSID());
        if (hwAbsApInfoData != null) {
            hwAbsApInfoData.mContinuousFailureTimes = 0;
            this.mHwAbsDataBaseManager.addOrUpdateApInfos(hwAbsApInfoData);
        }
    }

    public void setBlacklistBssid() {
        StringBuilder blacklist = new StringBuilder();
        List<HwAbsApInfoData> lists = initBlacklistDate();
        if (lists.size() != 0) {
            for (HwAbsApInfoData data : lists) {
                blacklist.append(data.mBssid);
                blacklist.append(";");
            }
            HwAbsUtils.logD(false, "blackList size = %{public}d", Integer.valueOf(lists.size()));
            this.mHwAbsWifiHandler.setAbsBlackList(blacklist.toString());
        }
    }

    private List<HwAbsApInfoData> initBlacklistDate() {
        List<HwAbsApInfoData> lists = this.mHwAbsDataBaseManager.getApInfoInBlackList();
        if (lists.size() <= 10) {
            return lists;
        }
        return seleteBlackApInfo(lists);
    }

    private List<HwAbsApInfoData> seleteBlackApInfo(List<HwAbsApInfoData> lists) {
        int size;
        Collections.sort(lists);
        Collections.reverse(lists);
        if (lists.size() <= 10) {
            size = lists.size();
        } else {
            size = 10;
        }
        List<HwAbsApInfoData> result = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            result.add(lists.get(i));
        }
        return result;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isApInDatabase(String bssid) {
        if (this.mHwAbsDataBaseManager.getApInfoByBssid(bssid) != null) {
            return true;
        }
        return false;
    }

    private void uploadBlacklistException(HwAbsApInfoData data) {
        HwAbsChrBlackListEvent event = new HwAbsChrBlackListEvent();
        event.mAbsApSsid = data.mSsid;
        event.mAbsApBssid = data.mBssid;
        event.mAbsAddReason = this.mAddBlacklistReason;
        event.mAbsSuportVowifi = this.mIsSupportVoWifi ? 1 : 0;
        event.mAbsSwitchTimes = data.mReassociateTimes;
        event.mAbsFailedTimes = data.mFailedTimes;
        List<HwAbsApInfoData> lists = this.mHwAbsDataBaseManager.getAllApInfo();
        if (lists != null) {
            event.mAbsTotalNum = lists.size();
        }
        List<HwAbsApInfoData> blacklists = this.mHwAbsDataBaseManager.getApInfoInBlackList();
        if (blacklists != null) {
            event.mAbsBlackListNum = blacklists.size();
        }
        this.mHwAbsChrManager.uploadBlacklistException(event);
    }

    public void notifySelEngineEnableWifi() {
        HwAbsUtils.logD(false, "notifySelEngineEnableWifi", new Object[0]);
        HwAbsWiFiHandler hwAbsWiFiHandler = this.mHwAbsWifiHandler;
        hwAbsWiFiHandler.setApCapability(hwAbsWiFiHandler.getCurrentCapability());
    }

    public void notifySelEngineResetCompelete() {
        HwAbsUtils.logD(false, "notifySelEngineResetCompelete", new Object[0]);
        sendMessage(38);
    }

    public void pauseAbsHandover() {
        HwAbsUtils.logD(false, "pauseAbsHandover, mIsPauseHandover =%{public}s", String.valueOf(this.mIsPauseHandover));
        if (!this.mIsPauseHandover) {
            this.mIsPauseHandover = true;
        } else if (hasMessages(103)) {
            removeMessages(103);
            HwAbsUtils.logD(false, "pauseAbsHandover is already have message remove it", new Object[0]);
        }
    }

    public void restartAbsHandover() {
        if (this.mIsPauseHandover && !hasMessages(103)) {
            HwAbsUtils.logD(false, "restartAbsHandover send delay message ", new Object[0]);
            sendMessageDelayed(103, 300000);
        }
    }

    /* access modifiers changed from: private */
    public static class ApHandoverInfo {
        protected long lastTime;
        protected int mHandoverTimes;

        private ApHandoverInfo() {
            this.mHandoverTimes = 0;
            this.lastTime = 0;
        }
    }

    private long getTimesMorning() {
        Calendar cal = Calendar.getInstance();
        cal.set(11, 0);
        cal.set(13, 0);
        cal.set(12, 0);
        cal.set(14, 0);
        return cal.getTimeInMillis();
    }

    private boolean isInOneDay(long now) {
        long startTime = getTimesMorning();
        long endTime = ONE_DAYA_TIME + startTime;
        if (startTime > now || now > endTime) {
            return false;
        }
        return true;
    }

    private void updateAbsHandoverTime() {
        WifiInfo info = this.mWifiManager.getConnectionInfo();
        if (info == null || info.getBSSID() == null) {
            HwAbsUtils.logE(false, "updateAbsHandoverTime error ", new Object[0]);
        } else if (this.mIsAntennaStateInPreempted || this.mIsAntennaStateInSearch || this.mIsAntennaStateInConnect) {
            long curTime = System.currentTimeMillis();
            if (this.mApHandoverInfoList.containsKey(info.getBSSID())) {
                ApHandoverInfo curApInfo = this.mApHandoverInfoList.get(info.getBSSID());
                if (curApInfo != null) {
                    if (isInOneDay(curApInfo.lastTime)) {
                        curApInfo.mHandoverTimes++;
                        curApInfo.lastTime = curTime;
                    } else {
                        HwAbsUtils.logE(false, "updateAbsHandoverTime not in one day", new Object[0]);
                        curApInfo.mHandoverTimes = 1;
                        curApInfo.lastTime = curTime;
                        removeAbsHandoverTimes();
                    }
                    HwAbsUtils.logE(false, "updateAbsHandoverTime curApInfo.mHandoverTimes = %{public}d", Integer.valueOf(curApInfo.mHandoverTimes));
                    this.mApHandoverInfoList.put(info.getBSSID(), curApInfo);
                    return;
                }
                HwAbsUtils.logE(false, "updateAbsHandoverTime curApInfo == null", new Object[0]);
                this.mApHandoverInfoList.remove(info.getBSSID());
            }
            ApHandoverInfo apInfo = new ApHandoverInfo();
            apInfo.mHandoverTimes = 1;
            apInfo.lastTime = curTime;
            this.mApHandoverInfoList.put(info.getBSSID(), apInfo);
        } else {
            HwAbsUtils.logE(false, "updateAbsHandoverTime do not mach type ", new Object[0]);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetAbsHandoverTimes() {
        ApHandoverInfo curApInfo;
        WifiInfo info = this.mWifiManager.getConnectionInfo();
        if (info == null || info.getBSSID() == null) {
            HwAbsUtils.logE(false, "resetAbsHandoverTimes error ", new Object[0]);
        } else if (this.mApHandoverInfoList.containsKey(info.getBSSID()) && (curApInfo = this.mApHandoverInfoList.get(info.getBSSID())) != null && curApInfo.mHandoverTimes >= 1) {
            HwAbsUtils.logE(false, "resetAbsHandoverTimes reset ", new Object[0]);
            curApInfo.mHandoverTimes--;
            curApInfo.lastTime = System.currentTimeMillis();
            this.mApHandoverInfoList.put(info.getBSSID(), curApInfo);
        }
    }

    private boolean isHandoverTooMuch(String bssid) {
        ApHandoverInfo curApInfo;
        if (bssid == null || !this.mApHandoverInfoList.containsKey(bssid) || (curApInfo = this.mApHandoverInfoList.get(bssid)) == null) {
            return false;
        }
        HwAbsUtils.logE(false, "isHandoverTooMach mHandoverTimes = %{public}d", Integer.valueOf(curApInfo.mHandoverTimes));
        if (curApInfo.mHandoverTimes >= 15) {
            return true;
        }
        return false;
    }

    private void removeAbsHandoverTimes() {
        HwAbsUtils.logD(false, "removeAbsHandoverTimes", new Object[0]);
        List<String> strArray = new ArrayList<>();
        for (Map.Entry<String, ApHandoverInfo> entry : this.mApHandoverInfoList.entrySet()) {
            String bssidKey = entry.getKey();
            if ((entry.getValue() instanceof ApHandoverInfo) && !isInOneDay(entry.getValue().lastTime)) {
                strArray.add(bssidKey);
            }
        }
        for (String keyWord : strArray) {
            this.mApHandoverInfoList.remove(keyWord);
        }
    }
}
