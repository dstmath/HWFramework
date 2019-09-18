package com.android.server.hidata.mplink;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.HwInnerNetworkManagerImpl;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.ServiceState;
import com.android.internal.telephony.IPhoneCallback;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.hidata.HwHidataJniAdapter;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.android.server.hidata.arbitration.IHiDataCHRCallBack;
import huawei.android.net.hwmplink.MpLinkCommonUtils;
import java.net.InetAddress;
import libcore.net.event.NetworkEventDispatcher;

public class HwMpLinkServiceImpl extends StateMachine implements IMpLinkStateObserverCallback {
    public static final int DEFAULT_SLOT_ID = 0;
    public static final int ILLEGAL_VALUE = -1;
    private static final boolean INTER_DISTURB_CHECK_FOR_ALL = SystemProperties.getBoolean("ro.config.check_disturb_always", false);
    public static final int MPLINK_MSG_AIDEVICE_MPLINK_CLOSE = 218;
    public static final int MPLINK_MSG_AIDEVICE_MPLINK_OPEN = 217;
    public static final int MPLINK_MSG_BASE = 200;
    public static final int MPLINK_MSG_DATA_ROAMING_OFF = 203;
    public static final int MPLINK_MSG_DATA_ROAMING_ON = 204;
    public static final int MPLINK_MSG_DATA_SUB_CHANGE = 230;
    public static final int MPLINK_MSG_DATA_SUITABLE_OFF = 202;
    public static final int MPLINK_MSG_DATA_SUITABLE_ON = 201;
    public static final int MPLINK_MSG_DEFAULT_NETWORK_CHANGE = 231;
    public static final int MPLINK_MSG_HIBRAIN_MPLINK_CLOSE = 212;
    public static final int MPLINK_MSG_HIBRAIN_MPLINK_OPEN = 211;
    public static final int MPLINK_MSG_MOBILE_DATA_AVAILABLE = 221;
    public static final int MPLINK_MSG_MOBILE_DATA_CONNECTED = 215;
    public static final int MPLINK_MSG_MOBILE_DATA_DISCONNECTED = 216;
    public static final int MPLINK_MSG_MOBILE_DATA_SWITCH_CLOSE = 220;
    public static final int MPLINK_MSG_MOBILE_DATA_SWITCH_OPEN = 219;
    public static final int MPLINK_MSG_MOBILE_SERVICE_IN = 205;
    public static final int MPLINK_MSG_MOBILE_SERVICE_OUT = 206;
    public static final int MPLINK_MSG_SET_UL_FREQ_REPORT_START = 1;
    public static final int MPLINK_MSG_SET_UL_FREQ_REPORT_STOP = 0;
    public static final int MPLINK_MSG_UPDTAE_UL_FREQ_INFO = 232;
    public static final int MPLINK_MSG_WIFIPRO_SWITCH_DISABLE = 210;
    public static final int MPLINK_MSG_WIFIPRO_SWITCH_ENABLE = 209;
    public static final int MPLINK_MSG_WIFI_CONNECTED = 213;
    public static final int MPLINK_MSG_WIFI_DISCONNECTED = 214;
    public static final int MPLINK_MSG_WIFI_VPN_CONNETED = 208;
    public static final int MPLINK_MSG_WIFI_VPN_DISCONNETED = 207;
    private static final int MPLK_SK_STRATEGY_CT = 1;
    private static final int MPLK_SK_STRATEGY_FURE = 4;
    private static final int MPLK_SK_STRATEGY_FUTE = 2;
    public static final int MSG_MPLINK_REQUEST_BIND_NETWORK = 2;
    public static final int MSG_MPLINK_REQUEST_UNBIND_NETWORK = 3;
    private static final int MSG_MPLINK_SETTINGS_STATE_CHANGE = 1;
    public static final int MSG_TEST_FOREGROUD_APP_LTE_HANDOVER_WIGI = 102;
    public static final int MSG_TEST_FOREGROUD_APP_WIFI_HANDOVER_LTE = 101;
    public static final int MSG_TEST_NET_COEXIST_LTE_PRIORITIZED = 104;
    public static final int MSG_TEST_NET_COEXIST_WIFI_PRIORITIZED = 103;
    private static final String TAG = "HiData_MpLinkServiceImpl";
    private static final int delayInMs = 30000;
    private static HwMpLinkServiceImpl mMpLinkServiceImpl;
    private final AlarmManager mAlarmManager;
    private ConnectivityManager mConnectivityManager = null;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public int mCurrentBindUid = -1;
    private int mCurrentNetworkStrategy;
    private int mCurrentQequestBindNetWork = -1;
    private int mCurrentUnbindNetId = -1;
    private int mFreqReportSub = -1;
    private IHiDataCHRCallBack mHiDataCHRCallBack = null;
    private HwInnerNetworkManagerImpl mHwInnerNetworkManagerImpl;
    /* access modifiers changed from: private */
    public HwMpLinkContentAware mHwMpLinkContentAware;
    private HwMpLinkDemoMode mHwMpLinkDemoMode;
    private HwMpLinkNetworkImpl mHwMpLinkNetworkImpl;
    /* access modifiers changed from: private */
    public HwMpLinkTelephonyImpl mHwMpLinkTelephonyImpl;
    /* access modifiers changed from: private */
    public HwMpLinkWifiImpl mHwMpLinkWifiImpl;
    /* access modifiers changed from: private */
    public HwMplinkChrImpl mHwMplinkChrImpl = null;
    private HwMplinkStateObserver mHwMplinkStateObserver;
    /* access modifiers changed from: private */
    public State mInitState = new InitState();
    private boolean mInternalMplinkEnable = false;
    /* access modifiers changed from: private */
    public boolean mIsFrequencyUpdateOn = false;
    /* access modifiers changed from: private */
    public State mMpLinkBaseState = new MpLinkBaseState();
    /* access modifiers changed from: private */
    public IMpLinkCallback mMpLinkCallback;
    private int mMpLinkConditionState = -1;
    private int mMpLinkNotifyControl = -1;
    private boolean mMpLinkSwitchEnable = false;
    /* access modifiers changed from: private */
    public State mMpLinkedState = new MpLinkedState();
    /* access modifiers changed from: private */
    public MplinkBindResultInfo mMplinkBindResultInfo = new MplinkBindResultInfo();
    IPhoneCallback mMplinkIPhoneCallback = null;
    private MplinkNetworkResultInfo mMplinkNri = new MplinkNetworkResultInfo();
    private PendingIntent mRequestCheckAlarmIntent = null;
    /* access modifiers changed from: private */
    public boolean shouldIgnoreDefaultChange = false;

    class InitState extends State {
        InitState() {
        }

        public void enter() {
            MpLinkCommonUtils.logD(HwMpLinkServiceImpl.TAG, "Enter InitState");
        }

        public void exit() {
            MpLinkCommonUtils.logI(HwMpLinkServiceImpl.TAG, "Exit InitState");
        }

        public boolean processMessage(Message message) {
            MpLinkCommonUtils.logD(HwMpLinkServiceImpl.TAG, "InitState,msg=" + message.what);
            int i = message.what;
            if (i != 103) {
                if (i != 215) {
                    switch (i) {
                        case 211:
                            if (HwMpLinkServiceImpl.this.mHwMpLinkWifiImpl.isWifiConnected()) {
                                if (HwMpLinkServiceImpl.this.mHwMpLinkTelephonyImpl.isMobileConnected()) {
                                    HwMpLinkServiceImpl.this.notifyNetCoexistFailed(15);
                                    break;
                                } else {
                                    HwMpLinkServiceImpl.this.notifyNetCoexistFailed(9);
                                    break;
                                }
                            } else {
                                HwMpLinkServiceImpl.this.notifyNetCoexistFailed(1);
                                break;
                            }
                        case HwMpLinkServiceImpl.MPLINK_MSG_HIBRAIN_MPLINK_CLOSE /*212*/:
                            HwMpLinkServiceImpl.this.mHwMpLinkTelephonyImpl.closeMobileDataIfOpened();
                            HwMpLinkServiceImpl.this.notifyNetCoexistFailed(true);
                            break;
                        case HwMpLinkServiceImpl.MPLINK_MSG_WIFI_CONNECTED /*213*/:
                            break;
                        default:
                            return true;
                    }
                }
                if (message.what == 213) {
                    HwMpLinkServiceImpl.this.mHwMpLinkTelephonyImpl.updateWifiLcfInfo(HwMpLinkServiceImpl.this.mHwMpLinkWifiImpl.mCurrentWifiFreq, HwMpLinkServiceImpl.this.mHwMpLinkWifiImpl.mCurrentWifiBandWidth);
                }
                HwMpLinkServiceImpl.this.transitionTo(HwMpLinkServiceImpl.this.mMpLinkBaseState);
            } else if (message.arg1 == 0) {
                HwMpLinkServiceImpl.this.requestWiFiAndCellCoexist(true);
            } else if (message.arg1 == 1) {
                HwMpLinkServiceImpl.this.requestWiFiAndCellCoexist(false);
            }
            return true;
        }
    }

    class MpLinkBaseState extends State {
        MpLinkBaseState() {
        }

        public void enter() {
            MpLinkCommonUtils.logD(HwMpLinkServiceImpl.TAG, "Enter MpLinkBaseState");
        }

        public void exit() {
            MpLinkCommonUtils.logI(HwMpLinkServiceImpl.TAG, "Exit MpLinkBaseState");
        }

        public boolean processMessage(Message message) {
            MpLinkCommonUtils.logD(HwMpLinkServiceImpl.TAG, "MpLinkBaseState,msg=" + message.what);
            int i = message.what;
            if (i != 103) {
                if (i != 232) {
                    switch (i) {
                        case 2:
                            HwMpLinkServiceImpl.this.mMplinkBindResultInfo.reset();
                            HwMpLinkServiceImpl.this.mMplinkBindResultInfo.setNetwork(message.arg1);
                            HwMpLinkServiceImpl.this.mMplinkBindResultInfo.setUid(message.arg2);
                            HwMpLinkServiceImpl.this.mMplinkBindResultInfo.setFailReason(101);
                            HwMpLinkServiceImpl.this.mMplinkBindResultInfo.setResult(4);
                            if (HwMpLinkServiceImpl.this.mMpLinkCallback != null) {
                                HwMpLinkServiceImpl.this.mMpLinkCallback.onBindProcessToNetworkResult(HwMpLinkServiceImpl.this.mMplinkBindResultInfo);
                                break;
                            }
                            break;
                        case 3:
                            HwMpLinkServiceImpl.this.handleClearBindProcessToNetwork(0, message.arg2);
                            break;
                        default:
                            switch (i) {
                                case 211:
                                    if (HwMpLinkServiceImpl.this.mHwMpLinkWifiImpl.isWifiConnected()) {
                                        HwMpLinkServiceImpl.this.openMpLinkNetworkCoexist();
                                        break;
                                    } else {
                                        HwMpLinkServiceImpl.this.notifyNetCoexistFailed(1);
                                        break;
                                    }
                                case HwMpLinkServiceImpl.MPLINK_MSG_HIBRAIN_MPLINK_CLOSE /*212*/:
                                    HwMpLinkServiceImpl.this.mHwMpLinkTelephonyImpl.closeMobileDataIfOpened();
                                    HwMpLinkServiceImpl.this.notifyNetCoexistFailed(true);
                                    break;
                                case HwMpLinkServiceImpl.MPLINK_MSG_WIFI_CONNECTED /*213*/:
                                    HwMpLinkServiceImpl.this.mHwMpLinkTelephonyImpl.updateWifiLcfInfo(HwMpLinkServiceImpl.this.mHwMpLinkWifiImpl.mCurrentWifiFreq, HwMpLinkServiceImpl.this.mHwMpLinkWifiImpl.mCurrentWifiBandWidth);
                                    if (HwMpLinkServiceImpl.this.mHwMpLinkTelephonyImpl.isMobileConnected()) {
                                        boolean unused = HwMpLinkServiceImpl.this.shouldIgnoreDefaultChange = true;
                                        HwMpLinkServiceImpl.this.transitionTo(HwMpLinkServiceImpl.this.mMpLinkedState);
                                        break;
                                    }
                                    break;
                                case HwMpLinkServiceImpl.MPLINK_MSG_WIFI_DISCONNECTED /*214*/:
                                    HwMpLinkServiceImpl.this.mHwMpLinkTelephonyImpl.updateWifiLcfInfo(HwMpLinkServiceImpl.this.mHwMpLinkWifiImpl.mCurrentWifiFreq, HwMpLinkServiceImpl.this.mHwMpLinkWifiImpl.mCurrentWifiBandWidth);
                                    if (!HwMpLinkServiceImpl.this.mHwMpLinkTelephonyImpl.isMobileConnected()) {
                                        HwMpLinkServiceImpl.this.transitionTo(HwMpLinkServiceImpl.this.mInitState);
                                        break;
                                    }
                                    break;
                                case HwMpLinkServiceImpl.MPLINK_MSG_MOBILE_DATA_CONNECTED /*215*/:
                                    if (HwMpLinkServiceImpl.this.mHwMpLinkWifiImpl.isWifiConnected()) {
                                        HwMpLinkServiceImpl.this.mHwMplinkChrImpl.updataDualNetworkCnt();
                                        HwMpLinkServiceImpl.this.transitionTo(HwMpLinkServiceImpl.this.mMpLinkedState);
                                        break;
                                    }
                                    break;
                                case HwMpLinkServiceImpl.MPLINK_MSG_MOBILE_DATA_DISCONNECTED /*216*/:
                                    if (!HwMpLinkServiceImpl.this.mHwMpLinkWifiImpl.isWifiConnected()) {
                                        HwMpLinkServiceImpl.this.transitionTo(HwMpLinkServiceImpl.this.mInitState);
                                        break;
                                    }
                                    break;
                                case HwMpLinkServiceImpl.MPLINK_MSG_AIDEVICE_MPLINK_OPEN /*217*/:
                                    break;
                                default:
                                    return true;
                            }
                    }
                } else if (true == HwMpLinkServiceImpl.this.mHwMpLinkTelephonyImpl.isFreqInterdisturbExist()) {
                    MpLinkCommonUtils.logD(HwMpLinkServiceImpl.TAG, "MpLinkBaseState,Freq Interdisturb between wifi and Cell");
                    HwMpLinkServiceImpl.this.notifyNetCoexistFailed(MplinkNetworkResultInfo.messgaeToFailReason(message.what));
                    HwMpLinkServiceImpl.this.mHwMplinkChrImpl.updateInterDisturbHappendTime();
                } else {
                    MpLinkCommonUtils.logD(HwMpLinkServiceImpl.TAG, "MpLinkBaseState,No Freq Interdisturb happened");
                    HwMpLinkServiceImpl.this.mHwMplinkChrImpl.updateOpenMobileDataStamp();
                    HwMpLinkServiceImpl.this.mHwMpLinkTelephonyImpl.mplinkSetMobileData(true);
                    HwMpLinkServiceImpl.this.mHwMplinkChrImpl.updateNoInterDisturbHappendTime();
                }
            } else if (message.arg1 == 0) {
                HwMpLinkServiceImpl.this.requestWiFiAndCellCoexist(true);
            } else if (message.arg1 == 1) {
                HwMpLinkServiceImpl.this.requestWiFiAndCellCoexist(false);
            }
            return true;
        }
    }

    class MpLinkedState extends State {
        MpLinkedState() {
        }

        public void enter() {
            MpLinkCommonUtils.logD(HwMpLinkServiceImpl.TAG, "Enter MpLinkedState");
            if (HwMpLinkServiceImpl.this.mHwMpLinkTelephonyImpl.getMobileDataAvaiable()) {
                HwMpLinkServiceImpl.this.notifyMpLinkNetCoexistSuccessful();
            }
            HwMpLinkServiceImpl.this.mHwMplinkChrImpl.updateAiDeviceOpenCnt(HwMpLinkServiceImpl.this.mHwMpLinkContentAware.getAiDeviceflag());
            if (HwMpLinkServiceImpl.this.mHwMpLinkContentAware.getAiDeviceflag() == 2) {
                int appUid = HwMpLinkServiceImpl.this.mHwMpLinkContentAware.getDeviceAppUid();
                MpLinkCommonUtils.logD(HwMpLinkServiceImpl.TAG, "bind to wifi device app uid:" + appUid);
                HwMpLinkServiceImpl.this.handleBindProcessToNetwork(MpLinkCommonUtils.getNetworkID(HwMpLinkServiceImpl.this.mContext, 1), appUid, new MpLinkQuickSwitchConfiguration(0, 2));
            }
        }

        public void exit() {
            MpLinkCommonUtils.logD(HwMpLinkServiceImpl.TAG, "Exit MpLinkedState");
            int unused = HwMpLinkServiceImpl.this.mCurrentBindUid = -1;
        }

        public boolean processMessage(Message message) {
            MpLinkCommonUtils.logD(HwMpLinkServiceImpl.TAG, "MpLinkedState,msg=" + message.what);
            switch (message.what) {
                case 2:
                    HwMpLinkServiceImpl.this.handleBindProcessToNetwork(message.arg1, message.arg2, (MpLinkQuickSwitchConfiguration) message.obj);
                    break;
                case 3:
                    HwMpLinkServiceImpl.this.handleClearBindProcessToNetwork(0, message.arg2);
                    break;
                case 101:
                case 102:
                    HwMpLinkServiceImpl.this.handleBindProcessToNetwork(message.arg1, message.arg2, new MpLinkQuickSwitchConfiguration(3, 0));
                    break;
                case 103:
                    if (message.arg1 != 0) {
                        if (message.arg1 == 1) {
                            HwMpLinkServiceImpl.this.requestWiFiAndCellCoexist(false);
                            break;
                        }
                    } else {
                        HwMpLinkServiceImpl.this.requestWiFiAndCellCoexist(true);
                        break;
                    }
                    break;
                case 202:
                case 204:
                case 206:
                case HwMpLinkServiceImpl.MPLINK_MSG_WIFI_VPN_CONNETED /*208*/:
                case 210:
                case HwMpLinkServiceImpl.MPLINK_MSG_HIBRAIN_MPLINK_CLOSE /*212*/:
                case HwMpLinkServiceImpl.MPLINK_MSG_DATA_SUB_CHANGE /*230*/:
                    HwMpLinkServiceImpl.this.closeMpLinkNetworkCoexist();
                    break;
                case 211:
                    HwMpLinkServiceImpl.this.notifyMpLinkNetCoexistSuccessful();
                    break;
                case HwMpLinkServiceImpl.MPLINK_MSG_WIFI_CONNECTED /*213*/:
                    HwMpLinkServiceImpl.this.mHwMpLinkTelephonyImpl.updateWifiLcfInfo(HwMpLinkServiceImpl.this.mHwMpLinkWifiImpl.mCurrentWifiFreq, HwMpLinkServiceImpl.this.mHwMpLinkWifiImpl.mCurrentWifiBandWidth);
                    HwMpLinkServiceImpl.this.mHwMpLinkTelephonyImpl.calculateInterdisturb();
                    if (true == HwMpLinkServiceImpl.this.mIsFrequencyUpdateOn && true == HwMpLinkServiceImpl.this.mHwMpLinkTelephonyImpl.isFreqInterdisturbExist()) {
                        HwMpLinkServiceImpl.this.configInterdisturbDetectReport(0);
                        HwMpLinkServiceImpl.this.closeMpLinkNetworkCoexist();
                        break;
                    }
                case HwMpLinkServiceImpl.MPLINK_MSG_WIFI_DISCONNECTED /*214*/:
                    HwMpLinkServiceImpl.this.notifyNetCoexistFailed(1);
                    HwMpLinkServiceImpl.this.mHwMplinkChrImpl.updateCoexistWifiSwitchClosedCnt();
                    HwMpLinkServiceImpl.this.closeMpLinkNetworkCoexist();
                    HwMpLinkServiceImpl.this.mHwMpLinkTelephonyImpl.updateWifiLcfInfo(HwMpLinkServiceImpl.this.mHwMpLinkWifiImpl.mCurrentWifiFreq, HwMpLinkServiceImpl.this.mHwMpLinkWifiImpl.mCurrentWifiBandWidth);
                    HwMpLinkServiceImpl.this.transitionTo(HwMpLinkServiceImpl.this.mMpLinkBaseState);
                    break;
                case HwMpLinkServiceImpl.MPLINK_MSG_MOBILE_DATA_DISCONNECTED /*216*/:
                    HwMpLinkServiceImpl.this.notifyNetCoexistFailed(9);
                    HwMpLinkServiceImpl.this.closeMpLinkNetworkCoexist();
                    HwMpLinkServiceImpl.this.transitionTo(HwMpLinkServiceImpl.this.mMpLinkBaseState);
                    break;
                case HwMpLinkServiceImpl.MPLINK_MSG_AIDEVICE_MPLINK_OPEN /*217*/:
                    break;
                case 220:
                    HwMpLinkServiceImpl.this.mHwMplinkChrImpl.updateCoexistMobileDataSwitchClosedCnt();
                    break;
                case HwMpLinkServiceImpl.MPLINK_MSG_MOBILE_DATA_AVAILABLE /*221*/:
                    HwMpLinkServiceImpl.this.notifyMpLinkNetCoexistSuccessful();
                    break;
                case HwMpLinkServiceImpl.MPLINK_MSG_DEFAULT_NETWORK_CHANGE /*231*/:
                    if (!HwMpLinkServiceImpl.this.shouldIgnoreDefaultChange) {
                        HwMpLinkServiceImpl.this.mHwMplinkChrImpl.updateDefaultRouteChangeCnt();
                        boolean unused = HwMpLinkServiceImpl.this.shouldIgnoreDefaultChange = false;
                        break;
                    }
                    break;
                case HwMpLinkServiceImpl.MPLINK_MSG_UPDTAE_UL_FREQ_INFO /*232*/:
                    if (true == HwMpLinkServiceImpl.this.mHwMpLinkTelephonyImpl.isFreqInterdisturbExist()) {
                        HwMpLinkServiceImpl.this.configInterdisturbDetectReport(0);
                        HwMpLinkServiceImpl.this.closeMpLinkNetworkCoexist();
                        break;
                    }
                    break;
                default:
                    return true;
            }
            return true;
        }
    }

    public static HwMpLinkServiceImpl getInstance(Context context) {
        if (mMpLinkServiceImpl == null) {
            mMpLinkServiceImpl = new HwMpLinkServiceImpl(context);
        }
        return mMpLinkServiceImpl;
    }

    private HwMpLinkServiceImpl(Context context) {
        super("HwMpLinkServiceImpl");
        this.mContext = context;
        addState(this.mInitState);
        addState(this.mMpLinkBaseState, this.mInitState);
        addState(this.mMpLinkedState, this.mMpLinkBaseState);
        setInitialState(this.mInitState);
        start();
        if (MpLinkCommonUtils.isMpLinkTestMode()) {
            this.mHwMpLinkDemoMode = new HwMpLinkDemoMode(this.mContext, getHandler());
        }
        this.mHwMplinkChrImpl = new HwMplinkChrImpl(this.mContext);
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        this.mHwMpLinkContentAware = HwMpLinkContentAware.getInstance(context);
        this.mHwMpLinkContentAware.regiterMpLinkHander(getHandler());
        this.mHwMpLinkTelephonyImpl = new HwMpLinkTelephonyImpl(this.mContext, getHandler());
        this.mHwMpLinkWifiImpl = new HwMpLinkWifiImpl(this.mContext, getHandler());
        this.mHwMplinkStateObserver = new HwMplinkStateObserver(this.mContext, this);
        this.mHwMpLinkNetworkImpl = new HwMpLinkNetworkImpl(context);
        this.mHwInnerNetworkManagerImpl = HwFrameworkFactory.getHwInnerNetworkManager();
        this.mHwMpLinkWifiImpl.setCurrentWifiVpnState(this.mHwMplinkStateObserver.getVpnConnectState());
        this.mMpLinkSwitchEnable = this.mHwMplinkStateObserver.getMpLinkSwitchState();
        setMpLinkConditionDB(200);
        this.mHwMplinkStateObserver.initSimulateHibrain();
        this.mMplinkIPhoneCallback = new IPhoneCallback.Stub() {
            public void onCallback3(int parm1, int param2, Bundle param3) {
                if (param3 == null || !HwMpLinkServiceImpl.this.mIsFrequencyUpdateOn) {
                    MpLinkCommonUtils.logD(HwMpLinkServiceImpl.TAG, "mMplinkIPhoneCallback onCallback3 param3 is null or mIsFrequencyUpdateOn = " + HwMpLinkServiceImpl.this.mIsFrequencyUpdateOn);
                    return;
                }
                MpLinkCommonUtils.logD(HwMpLinkServiceImpl.TAG, "mMplinkIPhoneCallback call back received");
                HwMpLinkInterDisturbInfo disturbInfo = new HwMpLinkInterDisturbInfo();
                disturbInfo.mRat = param3.getInt("rat");
                disturbInfo.mUlfreq = param3.getInt("ulfreq");
                disturbInfo.mUlbw = param3.getInt("ulbw");
                HwMpLinkServiceImpl.this.mHwMpLinkTelephonyImpl.upDataCellUlFreqInfo(disturbInfo);
                HwMpLinkServiceImpl.this.getHandler().sendMessage(HwMpLinkServiceImpl.this.obtainMessage(HwMpLinkServiceImpl.MPLINK_MSG_UPDTAE_UL_FREQ_INFO));
            }

            public void onCallback1(int parm) throws RemoteException {
            }

            public void onCallback2(int parm1, int param2) throws RemoteException {
            }
        };
        MpLinkCommonUtils.logD(TAG, "HwMpLinkServiceImpl complete");
    }

    private void getConnectiviyManger() {
        if (this.mConnectivityManager == null) {
            this.mConnectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        }
    }

    private void setMpLinkConditionDB(int type) {
        if (type >= 200 && type <= 220) {
            int value = 0;
            if (checkMplinkSuitableBeOpen() == 0) {
                value = 1;
            }
            if (this.mMpLinkConditionState != value) {
                MpLinkCommonUtils.logD(TAG, "setMpLinkConditionDB value:" + value);
                Settings.System.putInt(this.mContext.getContentResolver(), "mplink_db_condition_value", value);
                this.mMpLinkConditionState = value;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void stopInterdisturbDetectReport() {
        MpLinkCommonUtils.logD(TAG, "stopInterdisturbDetectReport Enter: mIsFrequencyUpdateOn = " + this.mIsFrequencyUpdateOn);
        if (true == this.mIsFrequencyUpdateOn) {
            configInterdisturbDetectReportToRil(this.mFreqReportSub, 0);
            this.mIsFrequencyUpdateOn = false;
            this.mFreqReportSub = -1;
        }
    }

    public void configInterdisturbDetectReportToRil(int dataSub, int status) {
        if (1 == status) {
            this.mIsFrequencyUpdateOn = true;
        }
        boolean ret = HwTelephonyManagerInner.getDefault().setUplinkFreqBandwidthReportState(dataSub, status, this.mMplinkIPhoneCallback);
        MpLinkCommonUtils.logD(TAG, "configInterdisturbDetectReportToRil Enter: dataSub = " + dataSub + ", status = " + status + ", setUlFreqBandwidthReport ret = " + ret);
        if (!ret) {
            this.mIsFrequencyUpdateOn = false;
            this.mHwMplinkChrImpl.updateInterDisturbCheckFailedTime();
            notifyNetCoexistFailed(11);
        }
    }

    public void configInterdisturbDetectReport(int status) {
        MpLinkCommonUtils.logD(TAG, "configInterdisturbDetectReport Enter");
        this.mFreqReportSub = this.mHwMpLinkTelephonyImpl.getDefaultDataSubId();
        configInterdisturbDetectReportToRil(this.mFreqReportSub, status);
    }

    private int checkMplinkSuitableBeOpen() {
        int failReason = 0;
        if (!this.mMpLinkSwitchEnable) {
            failReason = 8;
        } else if (this.mHwMpLinkContentAware.isAiDevice()) {
            failReason = 0;
        } else if (this.mHwMpLinkTelephonyImpl.getCurrentServceState() != 0) {
            failReason = 5;
        } else if (!this.mHwMpLinkTelephonyImpl.getCurrentDataTechSuitable()) {
            failReason = 3;
        } else if (this.mHwMpLinkTelephonyImpl.getCurrentDataRoamingState()) {
            failReason = 4;
        } else if (!this.mInternalMplinkEnable) {
            failReason = 7;
        } else if (!this.mHwMpLinkTelephonyImpl.isMobileDataEnable()) {
            failReason = 2;
        } else if (this.mHwMpLinkWifiImpl.getCurrentWifiVpnState()) {
            failReason = 6;
        }
        MpLinkCommonUtils.logI(TAG, "checkMplinkSuitableBeOpen ret:" + failReason);
        return failReason;
    }

    /* access modifiers changed from: private */
    public void notifyMpLinkNetCoexistSuccessful() {
        this.mMplinkNri.reset();
        this.mMplinkNri.setFailReason(0);
        this.mMplinkNri.setResult(100);
        this.mHwMplinkChrImpl.updateMobileDataConnectedStamp();
        if (!(this.mMpLinkCallback == null || this.mMpLinkNotifyControl == 1)) {
            MpLinkCommonUtils.logD(TAG, "network coexist successful");
            this.mMpLinkCallback.onWiFiAndCellCoexistResult(this.mMplinkNri);
            this.mMpLinkNotifyControl = 1;
        }
        recordRequestResult(this.mMplinkNri);
    }

    /* access modifiers changed from: private */
    public void notifyNetCoexistFailed(boolean forceNotify) {
        notifyNetCoexistFailed(0, forceNotify);
    }

    /* access modifiers changed from: private */
    public void notifyNetCoexistFailed(int reason) {
        notifyNetCoexistFailed(reason, false);
    }

    private void notifyNetCoexistFailed(int reason, boolean forceNotify) {
        this.mMplinkNri.reset();
        this.mMplinkNri.setFailReason(reason);
        this.mMplinkNri.setResult(101);
        stopInterdisturbDetectReport();
        if (this.mMpLinkCallback != null) {
            if (this.mMpLinkNotifyControl != 0) {
                MpLinkCommonUtils.logD(TAG, "network coexist failed,reason:" + reason);
                this.mMpLinkCallback.onWiFiAndCellCoexistResult(this.mMplinkNri);
                this.mMpLinkNotifyControl = 0;
            } else if (forceNotify) {
                MpLinkCommonUtils.logD(TAG, "force notify network coexist failed,reason:" + reason);
                this.mMpLinkCallback.onWiFiAndCellCoexistResult(this.mMplinkNri);
            }
        }
        recordRequestResult(this.mMplinkNri);
    }

    /* access modifiers changed from: private */
    public void openMpLinkNetworkCoexist() {
        if (this.mHwMpLinkTelephonyImpl.isMobileConnected()) {
            MpLinkCommonUtils.logD(TAG, "openMpLinkNetworkCoexist: Mobile already connected");
            return;
        }
        int reason = checkMplinkSuitableBeOpen();
        if (reason != 0) {
            notifyNetCoexistFailed(reason);
        } else if (true == INTER_DISTURB_CHECK_FOR_ALL) {
            MpLinkCommonUtils.logD(TAG, "openMpLinkNetworkCoexist: Trigger Interdisturb Detect ");
            configInterdisturbDetectReport(1);
            this.mHwMplinkChrImpl.updateInterDisturbCheckTriggerTime();
        } else {
            MpLinkCommonUtils.logD(TAG, "openMpLinkNetworkCoexist: Not trigger Interdisturb Detect, Mp-Link trigger Open Mobile Data ");
            this.mHwMplinkChrImpl.updateOpenMobileDataStamp();
            this.mHwMpLinkTelephonyImpl.mplinkSetMobileData(true);
        }
    }

    /* access modifiers changed from: private */
    public void closeMpLinkNetworkCoexist() {
        MpLinkCommonUtils.logD(TAG, "request close mplink network coexist");
        stopInterdisturbDetectReport();
        this.mHwMpLinkTelephonyImpl.mplinkSetMobileData(false);
        this.mInternalMplinkEnable = false;
    }

    public boolean isMpLinkConditionSatisfy() {
        boolean can = false;
        if (checkMplinkSuitableBeOpen() == 0) {
            can = true;
        }
        MpLinkCommonUtils.logI(TAG, "isMpLinkConditionSatisfy return:" + can);
        return can;
    }

    public void notifyIpConfigCompleted() {
    }

    public void foregroundAppChanged(int uid) {
        if (this.mHwMpLinkContentAware.isWifiLanApp(uid)) {
            MpLinkCommonUtils.logD(TAG, "isWifiLanApp");
        }
    }

    public void registMpLinkCallback(IMpLinkCallback callback) {
        if (this.mMpLinkCallback == null) {
            this.mMpLinkCallback = callback;
        }
    }

    public void registMpLinkCHRCallback(IHiDataCHRCallBack callback) {
        if (this.mHiDataCHRCallBack == null) {
            this.mHiDataCHRCallBack = callback;
        }
    }

    public void registRFInterferenceCallback(IRFInterferenceCallback callback) {
        MpLinkCommonUtils.logD(TAG, "registRFInterferenceCallback");
    }

    private void startRequestSuccessCheck(int requestType) {
        stopRequestSuccessCheck();
        Intent intent = new Intent("mplink_intent_check_request_success");
        intent.putExtra("mplink_intent_key_check_request", requestType);
        this.mRequestCheckAlarmIntent = PendingIntent.getBroadcast(this.mContext, 0, intent, 134217728);
        this.mAlarmManager.setExact(3, SystemClock.elapsedRealtime() + HwArbitrationDEFS.DelayTimeMillisA, this.mRequestCheckAlarmIntent);
    }

    private void stopRequestSuccessCheck() {
        if (this.mRequestCheckAlarmIntent != null) {
            this.mAlarmManager.cancel(this.mRequestCheckAlarmIntent);
            this.mRequestCheckAlarmIntent = null;
        }
    }

    private void recordRequestResult(MplinkNetworkResultInfo resultInfo) {
        if (this.mRequestCheckAlarmIntent != null) {
            int type = this.mRequestCheckAlarmIntent.getIntent().getIntExtra("mplink_intent_key_check_request", -1);
            int result = resultInfo.getResult();
            int reason = resultInfo.getFailReason();
            MpLinkCommonUtils.logD(TAG, "recordRequestResult:" + type + "," + result + "," + reason);
            if (type == 211) {
                if (result == 101) {
                    this.mHwMplinkChrImpl.updateOpenFailCnt(reason);
                    stopRequestSuccessCheck();
                } else if (result == 100) {
                    this.mHwMplinkChrImpl.updateOpenSuccCnt();
                    stopRequestSuccessCheck();
                }
            } else if (type == 212) {
                if (result == 101) {
                    this.mHwMplinkChrImpl.updateCloseSuccCnt();
                    stopRequestSuccessCheck();
                } else if (result == 100) {
                    this.mHwMplinkChrImpl.updateCloseFailCnt(reason);
                    stopRequestSuccessCheck();
                }
                this.mHwMplinkChrImpl.sendDataToChr(this.mHiDataCHRCallBack);
            }
        }
    }

    public void requestWiFiAndCellCoexist(boolean coexist) {
        MpLinkCommonUtils.logD(TAG, "requestCoexist, coexist:" + coexist + ", internal:" + this.mInternalMplinkEnable + ",Control:" + this.mMpLinkNotifyControl);
        if (this.mMpLinkCallback == null) {
            MpLinkCommonUtils.logD(TAG, "callback is null");
        } else if (this.mInternalMplinkEnable != coexist) {
            this.mInternalMplinkEnable = coexist;
            this.mMpLinkNotifyControl = -1;
            if (coexist) {
                sendMessage(211);
                startRequestSuccessCheck(211);
                return;
            }
            sendMessage(MPLINK_MSG_HIBRAIN_MPLINK_CLOSE);
            startRequestSuccessCheck(MPLINK_MSG_HIBRAIN_MPLINK_CLOSE);
        } else {
            MpLinkCommonUtils.logD(TAG, "dup request");
            if (!coexist && !this.mInternalMplinkEnable) {
                sendMessage(MPLINK_MSG_HIBRAIN_MPLINK_CLOSE);
                startRequestSuccessCheck(MPLINK_MSG_HIBRAIN_MPLINK_CLOSE);
            } else if (this.mRequestCheckAlarmIntent == null) {
                MpLinkCommonUtils.logD(TAG, "response for dup request");
                this.mMpLinkCallback.onWiFiAndCellCoexistResult(this.mMplinkNri);
            }
        }
    }

    public void updateMplinkAiDevicesList(int type, String packageWhiteList) {
    }

    public void onMpLinkRequestTimeout(int requestType) {
        MpLinkCommonUtils.logD(TAG, "onMpLinkRequestTimeout, type:" + requestType);
        notifyNetCoexistFailed(10, true);
    }

    public void onTelephonyServiceStateChanged(ServiceState serviceState, int subId) {
        this.mHwMpLinkTelephonyImpl.handleTelephonyServiceStateChanged(serviceState, subId);
    }

    public void onTelephonyDefaultDataSubChanged(int newDataSub) {
        this.mHwMpLinkTelephonyImpl.handleDataSubChange(newDataSub);
    }

    public void onTelephonyDataConnectionChanged(String state, String iface, int subId) {
        this.mHwMpLinkTelephonyImpl.handleTelephonyDataConnectionChanged(state, iface, subId);
    }

    public void onMobileDataSwitchChange(boolean enabled) {
        this.mHwMpLinkTelephonyImpl.handleMobileDataSwitchChange(enabled);
    }

    public void onWifiNetworkStateChanged(NetworkInfo netInfo) {
        this.mHwMpLinkWifiImpl.handleWifiNetworkStateChanged(netInfo);
    }

    public void onVpnStateChange(boolean vpnconnected) {
        this.mHwMpLinkWifiImpl.handleVpnStateChange(vpnconnected);
    }

    public void onMplinkSwitchChange(boolean mplinkSwitch) {
        if (this.mMpLinkSwitchEnable != mplinkSwitch) {
            this.mMpLinkSwitchEnable = mplinkSwitch;
            if (mplinkSwitch) {
                sendMessage(MPLINK_MSG_WIFIPRO_SWITCH_ENABLE);
            } else {
                sendMessage(210);
            }
        }
    }

    public void onSimulateHiBrainRequestForDemo(boolean enable) {
        MpLinkCommonUtils.logD(TAG, "onSimulateHiBrainRequestForTest:" + enable);
        requestWiFiAndCellCoexist(enable);
    }

    public void requestBindProcessToNetwork(int network, int uid, int type) {
        MpLinkCommonUtils.logD(TAG, "bindProcessToNetwork network:" + network + ", uid:" + uid);
        requestBindProcessToNetwork(network, uid, (MpLinkQuickSwitchConfiguration) null);
    }

    public void requestBindProcessToNetwork(int netid, int uid, MpLinkQuickSwitchConfiguration configuration) {
        MpLinkCommonUtils.logD(TAG, "bindProcessToNetwork network:" + netid + ", uid:" + uid + "configuration: " + configuration);
        sendMessage(2, netid, uid, configuration);
    }

    public void requestClearBindProcessToNetwork(int network, int uid) {
        MpLinkCommonUtils.logD(TAG, "clearBindProcessToNetwork network:" + network + ", uid:" + uid);
        this.mCurrentUnbindNetId = network;
        sendMessage(3, 0, uid);
    }

    /* access modifiers changed from: private */
    public void handleBindProcessToNetwork(int network, int uid, MpLinkQuickSwitchConfiguration quickSwitchConfig) {
        this.mMplinkBindResultInfo.reset();
        this.mMplinkBindResultInfo.setNetwork(network);
        this.mMplinkBindResultInfo.setUid(uid);
        this.mCurrentQequestBindNetWork = MpLinkCommonUtils.getNetworkType(this.mContext, network);
        int ret = -1;
        if (this.mCurrentQequestBindNetWork == 0 && quickSwitchConfig != null && !MpLinkCommonUtils.isAppCertified(quickSwitchConfig.getAppId())) {
            MpLinkCommonUtils.logD(TAG, "app has not been confirmed");
            ret = 102;
        }
        if (!(ret == 102 && this.mHwMpLinkDemoMode == null)) {
            ret = bindProcessToNetwork(network, uid);
        }
        if (ret == 0) {
            this.mCurrentBindUid = uid;
            InetAddress.clearDnsCache();
            NetworkEventDispatcher.getInstance().onNetworkConfigurationChanged();
            this.mMplinkBindResultInfo.setResult(1);
            MpLinkCommonUtils.logD(TAG, "bind network successful !");
            if (quickSwitchConfig == null) {
                MpLinkCommonUtils.logD(TAG, "quickSwitchConfig is null");
                closeProcessSockets(0, uid);
                this.mHwMpLinkNetworkImpl.handleNetworkStrategy(0, uid);
            } else {
                MpLinkCommonUtils.logD(TAG, "quickSwitchConfig:" + quickSwitchConfig.toString());
                this.mCurrentNetworkStrategy = quickSwitchConfig.getNetworkStrategy();
                handleSocketStrategy(quickSwitchConfig.getSocketStrategy(), uid);
                if (this.mHwMpLinkDemoMode != null) {
                    this.mCurrentNetworkStrategy = SystemProperties.getInt("mplink_network_type", this.mCurrentNetworkStrategy);
                }
                MpLinkCommonUtils.logD(TAG, "network strategy:" + this.mCurrentNetworkStrategy + ", uid:" + uid);
                this.mHwMpLinkNetworkImpl.handleNetworkStrategy(this.mCurrentNetworkStrategy, uid);
                if (this.mCurrentQequestBindNetWork == 0) {
                    this.mHwMplinkChrImpl.updateMplinkCellBindState(true, this.mHwMpLinkTelephonyImpl.getMobileIface());
                } else {
                    MpLinkCommonUtils.logD(TAG, "bind to wifi successful");
                }
            }
            if (this.mHwMpLinkDemoMode != null) {
                this.mHwMpLinkDemoMode.showToast("bind network successful !");
            }
        } else {
            MpLinkCommonUtils.logD(TAG, "bind network fail with err " + ret);
            this.mMplinkBindResultInfo.setResult(2);
        }
        if (this.mMpLinkCallback != null) {
            this.mMpLinkCallback.onBindProcessToNetworkResult(this.mMplinkBindResultInfo);
        }
    }

    /* access modifiers changed from: private */
    public void handleClearBindProcessToNetwork(int network, int uid) {
        this.mMplinkBindResultInfo.reset();
        this.mMplinkBindResultInfo.setNetwork(this.mCurrentUnbindNetId);
        this.mMplinkBindResultInfo.setUid(uid);
        int ret = bindProcessToNetwork(network, uid);
        if (ret == 0) {
            closeProcessSockets(0, uid);
            InetAddress.clearDnsCache();
            NetworkEventDispatcher.getInstance().onNetworkConfigurationChanged();
            this.mMplinkBindResultInfo.setResult(3);
            this.mCurrentQequestBindNetWork = -1;
            MpLinkCommonUtils.logD(TAG, "unbind network successful !");
            if (this.mHwMpLinkDemoMode != null) {
                this.mHwMpLinkDemoMode.showToast("unbind network successful !");
            }
            if (this.mCurrentBindUid != -1) {
                this.mHwMplinkChrImpl.updateMplinkCellBindState(false, null);
            }
            this.mCurrentBindUid = -1;
        } else {
            MpLinkCommonUtils.logD(TAG, "unbind network fail with err " + ret);
            this.mMplinkBindResultInfo.setResult(4);
        }
        if (this.mMpLinkCallback != null) {
            this.mMpLinkCallback.onBindProcessToNetworkResult(this.mMplinkBindResultInfo);
        }
    }

    private int bindProcessToNetwork(int network, int uid) {
        int reason = HwHidataJniAdapter.getInstance().bindUidProcessToNetwork(network, uid);
        MpLinkCommonUtils.logD(TAG, "bindProcessToNetwork network:" + network + ", uid:" + uid + ", reason:" + reason);
        if (reason != 0) {
            if (network == 0) {
                this.mHwMplinkChrImpl.updateUnBindFailCnt(reason);
            } else {
                this.mHwMplinkChrImpl.updateBindFailCnt(reason);
            }
        } else if (network == 0) {
            this.mHwMplinkChrImpl.updateUnBindSuccCnt();
        } else {
            this.mHwMplinkChrImpl.updateBindSuccCnt();
        }
        return reason;
    }

    private int resetProcessSockets(int network, int uid) {
        MpLinkCommonUtils.logD(TAG, "resetProcessSockets network:" + network + ", uid:" + uid);
        return HwHidataJniAdapter.getInstance().resetProcessSockets(uid);
    }

    private int handleSocketStrategy(int strategy, int uid) {
        if (this.mHwMpLinkDemoMode != null) {
            strategy = SystemProperties.getInt("mplink_close_type", strategy);
        }
        MpLinkCommonUtils.logD(TAG, "Socket strategy:" + strategy + ", uid:" + uid);
        int ret = 0;
        if ((strategy & 1) != 0) {
            this.mHwInnerNetworkManagerImpl.closeSocketsForUid(uid);
        }
        if (!((strategy & 2) == 0 && (strategy & 4) == 0)) {
            ret = HwHidataJniAdapter.getInstance().handleSocketStrategy(strategy, uid);
        }
        if (ret != 0) {
            this.mHwMplinkChrImpl.updateCloseSocketFailCnt(ret);
        } else {
            this.mHwMplinkChrImpl.updateCloseSocketSuccCnt();
        }
        return ret;
    }

    private int closeProcessSockets(int strategy, int uid) {
        MpLinkCommonUtils.logD(TAG, "closeProcessSockets strategy:" + strategy + ", uid:" + uid);
        int ret = 0;
        if ((strategy & 1) != 0) {
            this.mHwInnerNetworkManagerImpl.closeSocketsForUid(uid);
        }
        if (!((strategy & 2) == 0 && (strategy & 4) == 0)) {
            ret = HwHidataJniAdapter.getInstance().handleSocketStrategy(strategy, uid);
        }
        if (ret != 0) {
            this.mHwMplinkChrImpl.updateCloseSocketFailCnt(ret);
        } else {
            this.mHwMplinkChrImpl.updateCloseSocketSuccCnt();
        }
        return ret;
    }

    public NetworkInfo getMpLinkNetworkInfo(NetworkInfo info, int uid) {
        if (!(this.mCurrentBindUid == -1 && this.mHwMpLinkDemoMode == null)) {
            MpLinkCommonUtils.logI(TAG, "uid = " + uid + ", binduid = " + this.mCurrentBindUid + ", strategy: " + this.mCurrentNetworkStrategy);
        }
        if (this.mCurrentBindUid == -1 || uid != this.mCurrentBindUid) {
            return info;
        }
        if (this.mCurrentNetworkStrategy == 1) {
            return this.mHwMpLinkNetworkImpl.createMobileNetworkInfo();
        }
        if (this.mCurrentNetworkStrategy == 2) {
            return this.mHwMpLinkNetworkImpl.createWifiNetworkInfo();
        }
        return info;
    }

    public boolean isAppBindedNetwork() {
        if (this.mCurrentBindUid != -1) {
            return true;
        }
        return false;
    }
}
