package com.android.server.hidata.arbitration;

import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings;
import android.rms.iaware.AppTypeRecoManager;
import android.telephony.SubscriptionManager;
import android.widget.Toast;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.hidata.appqoe.HwAPPQoEManager;
import com.android.server.hidata.appqoe.HwAPPQoEResourceManger;
import com.android.server.hidata.appqoe.HwAPPQoEUtils;
import com.android.server.hidata.appqoe.HwAPPStateInfo;
import com.android.server.hidata.hiradio.HwHiRadioBoost;
import com.android.server.hidata.hiradio.HwWifiBoost;
import com.android.server.hidata.histream.HwHiStreamManager;
import com.android.server.hidata.wavemapping.HwWaveMappingManager;
import com.android.server.wifipro.WifiProCommonDefs;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HwArbitrationStateMachine extends StateMachine {
    private static final int MOBILE_DATA_ALWAYS_OFF = 0;
    private static final int MOBILE_DATA_ALWAYS_ON = 1;
    private static final String MOBILE_DATA_ALWAYS_ON_FOR_MPLINK = "mobile_data_always_on_for_mplink";
    private static final long QUERY_WAIT_TIME = 20000;
    private static final String TAG = "HiData_HwArbitrationStateMachine";
    private static HwArbitrationStateMachine mArbitrationStateMachine;
    private static ConcurrentHashMap<Integer, HwArbitrationAppBoostInfo> mHwArbitrationAppBoostMap;
    private int hiStreamAppState = -1;
    private boolean isErrorMsgReceived = false;
    private boolean isMpLinkBinding = false;
    private AppTypeRecoManager mAppTypeRecoManager = null;
    private State mCellMonitorState = new CellMonitorState();
    private int mCoexCount = 0;
    private Context mContext;
    private int mCurrentActiveNetwork = 802;
    private int mCurrentServiceState = 1;
    private HwAPPStateInfo mCurrentStreamAppInfo;
    private State mDefaultState = new DefaultState();
    private boolean mDenyByNotification = false;
    private boolean mDenyByRxBytesInWifi = false;
    private boolean mDeviceBootCommpleted = false;
    private boolean mHiStreamTriggerOrStopMplink = true;
    HwAPPQoEManager mHwAPPQoEManager = null;
    private HwAPPQoEResourceManger mHwAPPQoEResourceManger;
    HwArbitrationChrImpl mHwArbitrationChrImpl;
    private HwHiRadioBoost mHwHiRadioBoost;
    HwHiStreamManager mHwHiStreamManager = null;
    private HwWifiBoost mHwWifiBoost = null;
    private State mInitialState = new InitialState();
    private boolean mIsMpLinkError = false;
    private State mMPLinkStartedState = new MPLinkStartedState();
    private State mMPLinkStartingState = new MPLinkStartingState();
    private State mMPLinkStoppingState = new MPLinkStoppingState();
    private int mMpLinkCount = 0;
    private HashMap<Integer, Long> mQueryTime;
    private State mWifiMonitorState = new WifiMonitorState();
    private int mobileDataAlwaysOn = 0;
    private boolean needStopCoex = false;
    private boolean needTputTest = false;
    private boolean punishCellDetectImprecise = false;
    private long triggerMPlinkInternal = QUERY_WAIT_TIME;
    private long wifiToCellTimestamp = 0;

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setStateMachineHashMap(HwAPPStateInfo appInfo, int network, boolean isCoex, boolean isMPLink, int solution) {
        if (appInfo != null) {
            setStateMachineHashMap(appInfo.mAppId, appInfo.mAppUID, appInfo.mScenceId, network, isCoex, isMPLink, solution, appInfo);
        }
    }

    private void setStateMachineHashMap(int AppID, int UID, int sceneId, int network, boolean isCoex, boolean isMPLink, int solution, HwAPPStateInfo appInfo) {
        if (mHwArbitrationAppBoostMap == null) {
            mHwArbitrationAppBoostMap = new ConcurrentHashMap<>();
        }
        HwArbitrationAppBoostInfo mHwAAInfo = new HwArbitrationAppBoostInfo(AppID, UID, sceneId, network, isCoex, isMPLink, solution);
        mHwAAInfo.setHwAPPStateInfo(appInfo);
        HwArbitrationCommonUtils.logD(TAG, false, "set Map, UID is %{public}d BoostInfo is %{public}s", Integer.valueOf(UID), mHwAAInfo.toString());
        mHwArbitrationAppBoostMap.put(Integer.valueOf(UID), mHwAAInfo);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateStateMachineHashMap(HwAPPStateInfo appInfo, int network, boolean isCoex, boolean isMpLink, int solution) {
        ConcurrentHashMap<Integer, HwArbitrationAppBoostInfo> concurrentHashMap = mHwArbitrationAppBoostMap;
        if (concurrentHashMap == null || concurrentHashMap.get(Integer.valueOf(appInfo.mAppUID)) == null) {
            setStateMachineHashMap(appInfo, network, isCoex, isMpLink, solution);
            return;
        }
        HwArbitrationAppBoostInfo boostInfo = mHwArbitrationAppBoostMap.get(Integer.valueOf(appInfo.mAppUID));
        int newSolution = boostInfo.getSolution() | solution;
        HwArbitrationCommonUtils.logD(TAG, false, "setStateMachineHashMap: old solution is %{public}d,New solution is %{public}d", Integer.valueOf(boostInfo.getSolution()), Integer.valueOf(newSolution));
        boostInfo.setSolution(newSolution);
        boostInfo.setNetwork(network);
        boostInfo.setIsCoex(isCoex);
        boostInfo.setIsMPLink(isMpLink);
        boostInfo.setHwAPPStateInfo(appInfo);
        mHwArbitrationAppBoostMap.put(Integer.valueOf(appInfo.mAppUID), boostInfo);
    }

    public void updateStateMachineHashMap(HwAPPStateInfo appInfo) {
        HwArbitrationAppBoostInfo boostInfo;
        ConcurrentHashMap<Integer, HwArbitrationAppBoostInfo> concurrentHashMap = mHwArbitrationAppBoostMap;
        if (concurrentHashMap != null && concurrentHashMap.get(Integer.valueOf(appInfo.mAppUID)) != null && (boostInfo = mHwArbitrationAppBoostMap.get(Integer.valueOf(appInfo.mAppUID))) != null && boostInfo.getHwAPPStateInfo() != null) {
            appInfo.setCheckedCellChannelQuality(boostInfo.getHwAPPStateInfo().getCheckedCellChannelQuality());
            appInfo.setIsMplinkEnteredFromCell(boostInfo.getHwAPPStateInfo().getIsMplinkEnteredFromCell());
            boostInfo.setHwAPPStateInfo(appInfo);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void printMap(int uid) {
        ConcurrentHashMap<Integer, HwArbitrationAppBoostInfo> concurrentHashMap = mHwArbitrationAppBoostMap;
        if (concurrentHashMap == null) {
            HwArbitrationCommonUtils.logD(TAG, false, "Map is null", new Object[0]);
        } else if (concurrentHashMap.get(Integer.valueOf(uid)) == null) {
            HwArbitrationCommonUtils.logD(TAG, false, "MapInfo is null, uid is: %{public}d", Integer.valueOf(uid));
        } else {
            HwArbitrationCommonUtils.logD(TAG, false, "uid is %{public}d BoostInfo is %{public}s", Integer.valueOf(uid), mHwArbitrationAppBoostMap.get(Integer.valueOf(uid)).toString());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setMobileDataAlwaysOn(int value) {
        Settings.Global.putInt(this.mContext.getContentResolver(), MOBILE_DATA_ALWAYS_ON_FOR_MPLINK, value);
        this.mobileDataAlwaysOn = value;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isBoostInfoAvailable(HwAPPStateInfo appInfo) {
        ConcurrentHashMap<Integer, HwArbitrationAppBoostInfo> concurrentHashMap;
        HwArbitrationAppBoostInfo boostInfo;
        if (appInfo == null || (concurrentHashMap = mHwArbitrationAppBoostMap) == null || concurrentHashMap.isEmpty() || (boostInfo = mHwArbitrationAppBoostMap.get(Integer.valueOf(appInfo.mAppUID))) == null || boostInfo.getHwAPPStateInfo() == null) {
            return false;
        }
        return true;
    }

    public void detectQualityOfCellular(HwAPPStateInfo appInfo) {
        Message message = Message.obtain();
        message.what = HwArbitrationDEFS.MSG_DETECT_CELL_QUALITY;
        message.obj = appInfo;
        deferMessage(message);
    }

    public void notifyIpConfigCompleted() {
        sendMessage(HwArbitrationDEFS.MSG_NOTIFY_IP_CONFIG_COMPLETED);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startMpLinkAppBind(int uid) {
        printMap(uid);
        HwArbitrationCommonUtils.logD(TAG, false, "enter startMpLinkAppBind", new Object[0]);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopMpLinkAppBind(int uid, int type) {
        HwArbitrationCommonUtils.logD(TAG, false, "enter stopMpLinkAppBindForType", new Object[0]);
    }

    private void startMpLinkAppBindForCell(int uid) {
        printMap(uid);
        HwArbitrationCommonUtils.logD(TAG, false, "enter startMpLinkAppBindForCell", new Object[0]);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopMpLinkAppBind(int uid) {
        HwArbitrationCommonUtils.logD(TAG, false, "enter stopMpLinkAppBind", new Object[0]);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startMpLinkCoex(Message message, boolean needDefer) {
        if (message == null) {
            HwArbitrationCommonUtils.logE(TAG, false, "message is null, just return", new Object[0]);
            return;
        }
        HwAPPStateInfo appInfo = (HwAPPStateInfo) message.obj;
        int uid = Integer.MIN_VALUE;
        if (appInfo != null) {
            uid = appInfo.mAppUID;
            if (!isNeedChQoEquery(uid)) {
                HwArbitrationCommonUtils.logD(TAG, false, "not allow ChannelQoE and WM query in WiFiMonitor", new Object[0]);
                this.mHwArbitrationChrImpl.updateRequestMplinkChr((HwAPPStateInfo) message.obj, 3);
                return;
            }
        }
        if (this.punishCellDetectImprecise) {
            HwArbitrationCommonUtils.logD(TAG, false, "not start MPLink, due to trgPingPong in Cell Bad", new Object[0]);
        } else if (this.mDenyByNotification) {
            HwArbitrationCommonUtils.logD(TAG, false, "not start MPLink, due to deny by Notification in next 24h", new Object[0]);
            this.mHwArbitrationChrImpl.updateRequestMplinkChr((HwAPPStateInfo) message.obj, 20);
        } else if (HwArbitrationFunction.isAllowMpLink(this.mContext, uid, appInfo)) {
            if (needDefer && message.obj != null) {
                deferMessage(message);
            }
            transitionTo(this.mMPLinkStartingState);
        } else {
            HwArbitrationCommonUtils.logD(TAG, false, "not allow MpLink", new Object[0]);
            this.mHwArbitrationChrImpl.updateRequestMplinkChr((HwAPPStateInfo) message.obj, 1);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopMpLinkCoex(int uid) {
        ConcurrentHashMap<Integer, HwArbitrationAppBoostInfo> concurrentHashMap = mHwArbitrationAppBoostMap;
        if (!(concurrentHashMap == null || concurrentHashMap.get(Integer.valueOf(uid)) == null || !mHwArbitrationAppBoostMap.get(Integer.valueOf(uid)).getIsCoex())) {
            mHwArbitrationAppBoostMap.get(Integer.valueOf(uid)).setIsCoex(false);
            this.mCoexCount--;
            HwArbitrationCommonUtils.logD(TAG, false, "stopMpLinkCoex, mCoexCount is: %{public}d", Integer.valueOf(this.mCoexCount));
        }
        if (this.mMpLinkCount > 0 || this.mCoexCount > 0) {
            HwArbitrationCommonUtils.logD(TAG, false, "other app in MPLink, keep COEX", new Object[0]);
            return;
        }
        HwArbitrationCommonUtils.logD(TAG, false, "no app in MPLink, stop COEX", new Object[0]);
        transitionTo(this.mMPLinkStoppingState);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startMpLinkBindCheck(int uid) {
        HwArbitrationCommonUtils.logD(TAG, false, "startMpLinkBindCheck,uid = %{public}d", Integer.valueOf(uid));
        printMap(uid);
        ConcurrentHashMap<Integer, HwArbitrationAppBoostInfo> concurrentHashMap = mHwArbitrationAppBoostMap;
        if (concurrentHashMap != null) {
            HwArbitrationAppBoostInfo myAABInfo = concurrentHashMap.get(Integer.valueOf(uid));
            if (!isNeedChQoEquery(uid) || myAABInfo == null) {
                updateMplinkCHRExceptionEvent(uid, 8, 3);
                return;
            }
            HwArbitrationCommonUtils.logD(TAG, false, "start queryNetworkQuality", new Object[0]);
            this.isMpLinkBinding = true;
            sendMessageDelayed(HwArbitrationDEFS.MSG_QUERY_QOE_WM_TIMEOUT, uid, HwArbitrationDEFS.TIMEOUT_FOR_QUERY_QOE_WM);
            HwArbitrationCallbackImpl.getInstance(this.mContext).initJudgeBothChqoeAndWm();
            HwWaveMappingManager.getInstance(this.mContext).queryWaveMappingInfo(myAABInfo.mUID, myAABInfo.mAppID, myAABInfo.mSceneId, getTargetNetwork(myAABInfo.mNetwork));
            this.needTputTest = false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getTargetNetwork(int network) {
        if (800 == network) {
            return 801;
        }
        if (801 == network) {
            return 800;
        }
        return 802;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetPingPongFlag() {
        this.punishCellDetectImprecise = false;
        this.wifiToCellTimestamp = 0;
    }

    public static HwArbitrationStateMachine getInstance(Context context) {
        if (mArbitrationStateMachine == null) {
            mArbitrationStateMachine = new HwArbitrationStateMachine(context);
        }
        return mArbitrationStateMachine;
    }

    private HwArbitrationStateMachine(Context context) {
        super("HwArbitrationStateMachine");
        this.mContext = context;
        this.mHwWifiBoost = HwWifiBoost.getInstance(this.mContext);
        this.mAppTypeRecoManager = AppTypeRecoManager.getInstance();
        this.mHwHiRadioBoost = HwHiRadioBoost.createInstance(context);
        addState(this.mDefaultState);
        addState(this.mInitialState, this.mDefaultState);
        addState(this.mWifiMonitorState, this.mDefaultState);
        addState(this.mCellMonitorState, this.mDefaultState);
        addState(this.mMPLinkStartingState, this.mDefaultState);
        addState(this.mMPLinkStoppingState, this.mDefaultState);
        addState(this.mMPLinkStartedState, this.mDefaultState);
        setInitialState(this.mInitialState);
        start();
        this.mHwArbitrationChrImpl = HwArbitrationChrImpl.createInstance();
        setMobileDataAlwaysOn(0);
    }

    class DefaultState extends State {
        DefaultState() {
        }

        public void enter() {
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "Enter DefaultState", new Object[0]);
        }

        public void exit() {
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "exit DefaultState", new Object[0]);
        }

        public boolean processMessage(Message message) {
            if (HwArbitrationStateMachine.this.getCurrentState() != null) {
                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "DefaultState Msg: %{public}d, received in %{public}s", Integer.valueOf(message.what), HwArbitrationStateMachine.this.getCurrentState().getName());
            }
            switch (message.what) {
                case 100:
                case 104:
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "DefaultState unhandled MSG_GAME_ENTER_PVP_BATTLE or MSG_GAME_STATE_START", new Object[0]);
                    break;
                case 105:
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "DefaultState unhandled MSG_GAME_EXIT_PVP_BATTLE", new Object[0]);
                    break;
                case 115:
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "DefaultState unhandled MSG_GAME_WAR_STATE_BAD", new Object[0]);
                    break;
                case 130:
                    HwArbitrationStateMachine.this.setMobileDataAlwaysOn(0);
                    HwAPPStateInfo gameInfo = HwArbitrationCallbackImpl.getInstance(HwArbitrationStateMachine.this.mContext).getGameAppStateInfo();
                    if (gameInfo != null) {
                        gameInfo.setIsMplinkEnteredFromCell(-1);
                    }
                    if (HwArbitrationCommonUtils.getActiveConnectType(HwArbitrationStateMachine.this.mContext) == 800 && HwArbitrationStateMachine.this.getCurrentState() == HwArbitrationStateMachine.this.mCellMonitorState) {
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "CELL_MPLINK_TIMEOUT, transition to mWifiMonitorState", new Object[0]);
                        HwArbitrationStateMachine hwArbitrationStateMachine = HwArbitrationStateMachine.this;
                        hwArbitrationStateMachine.transitionTo(hwArbitrationStateMachine.mWifiMonitorState);
                        break;
                    }
                case 1005:
                    HwArbitrationStateMachine.this.deferMessage(message);
                    break;
                case 1006:
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "DefaultState MSG_WIFI_STATE_DISCONNECT", new Object[0]);
                    if (!HwArbitrationStateMachine.this.deliverErrorMPLinkCase()) {
                        if (801 != HwArbitrationCommonUtils.getActiveConnectType(HwArbitrationStateMachine.this.mContext) || HwArbitrationStateMachine.this.mCellMonitorState == HwArbitrationStateMachine.this.getCurrentState()) {
                            if (HwArbitrationStateMachine.this.getCurrentState() != HwArbitrationStateMachine.this.mInitialState && 802 == HwArbitrationCommonUtils.getActiveConnectType(HwArbitrationStateMachine.this.mContext)) {
                                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "Not handle:MSG_WIFI_STATE_DISCONNECT in default state", new Object[0]);
                                HwArbitrationStateMachine hwArbitrationStateMachine2 = HwArbitrationStateMachine.this;
                                hwArbitrationStateMachine2.transitionTo(hwArbitrationStateMachine2.mInitialState);
                                break;
                            }
                        } else {
                            HwArbitrationStateMachine hwArbitrationStateMachine3 = HwArbitrationStateMachine.this;
                            hwArbitrationStateMachine3.transitionTo(hwArbitrationStateMachine3.mCellMonitorState);
                            break;
                        }
                    }
                    break;
                case HwArbitrationDEFS.MSG_CELL_STATE_DISCONNECT /* 1010 */:
                case HwArbitrationDEFS.MSG_CLOSE_4G_OR_WCDMA /* 2022 */:
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "DefaultState: MSG_CELL_STATE_DISCONNECTED", new Object[0]);
                    if (!HwArbitrationStateMachine.this.deliverErrorMPLinkCase()) {
                        if (800 != HwArbitrationCommonUtils.getActiveConnectType(HwArbitrationStateMachine.this.mContext)) {
                            if (802 == HwArbitrationCommonUtils.getActiveConnectType(HwArbitrationStateMachine.this.mContext) && HwArbitrationStateMachine.this.getCurrentState() != HwArbitrationStateMachine.this.mInitialState) {
                                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "Not handle:ACTIVE_CONNECT_IS_NONE in default state", new Object[0]);
                                HwArbitrationStateMachine hwArbitrationStateMachine4 = HwArbitrationStateMachine.this;
                                hwArbitrationStateMachine4.transitionTo(hwArbitrationStateMachine4.mInitialState);
                                break;
                            }
                        } else {
                            HwArbitrationStateMachine hwArbitrationStateMachine5 = HwArbitrationStateMachine.this;
                            hwArbitrationStateMachine5.transitionTo(hwArbitrationStateMachine5.mWifiMonitorState);
                            break;
                        }
                    }
                    break;
                case HwArbitrationDEFS.MSG_SCREEN_IS_TURNOFF /* 1012 */:
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "MSG_SCREEN_IS_TURNOFF", new Object[0]);
                    HwArbitrationStateMachine.this.deliverErrorMPLinkCase();
                    break;
                case HwArbitrationDEFS.MSG_STATE_IS_ROAMING /* 1015 */:
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "update  mDataRoamingState:true", new Object[0]);
                    break;
                case HwArbitrationDEFS.MSG_SCREEN_IS_ON /* 1017 */:
                    break;
                case HwArbitrationDEFS.MSG_STATE_IN_SERVICE /* 1019 */:
                    HwArbitrationStateMachine.this.mCurrentServiceState = 0;
                    break;
                case HwArbitrationDEFS.MSG_STATE_OUT_OF_SERVICE /* 1020 */:
                    HwArbitrationStateMachine.this.mCurrentServiceState = 1;
                    break;
                case 1021:
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "update  mDataRoamingState:false", new Object[0]);
                    break;
                case HwArbitrationDEFS.MSG_NOTIFY_CURRENT_NETWORK /* 1030 */:
                    HwArbitrationStateMachine.this.mCurrentActiveNetwork = message.arg1;
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "update active network: %{public}d", Integer.valueOf(HwArbitrationStateMachine.this.mCurrentActiveNetwork));
                    break;
                case HwArbitrationDEFS.MSG_DEVICE_BOOT_COMPLETED /* 1127 */:
                    HwArbitrationStateMachine.this.mDeviceBootCommpleted = true;
                    HwArbitrationStateMachine.this.mHwHiRadioBoost.initCommBoosterManager();
                    break;
                case 2001:
                    HwArbitrationCommonUtils.logE(HwArbitrationStateMachine.TAG, false, "mIsMpLinkError is false", new Object[0]);
                    HwArbitrationStateMachine.this.mIsMpLinkError = false;
                    break;
                case HwArbitrationDEFS.MSG_MPLINK_NONCOEX_MODE /* 2004 */:
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "DefaultState start COEX error", new Object[0]);
                    HwArbitrationStateMachine.this.deliverErrorMPLinkCase();
                    break;
                case HwArbitrationDEFS.MSG_MPLINK_BIND_SUCCESS /* 2007 */:
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "In DefaultState, MSG_MPLINK_BIND_SUCCESS", new Object[0]);
                    HwArbitrationStateMachine.this.isMpLinkBinding = false;
                    break;
                case HwArbitrationDEFS.MSG_WIFI_PLUS_ENABLE /* 2016 */:
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "DefaultState MSG_WIFI_PLUS_ENABLE", new Object[0]);
                    break;
                case HwArbitrationDEFS.MSG_WIFI_PLUS_DISABLE /* 2018 */:
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "DefaultState MSG_WIFI_PLUS_DISABLE", new Object[0]);
                    HwArbitrationStateMachine.this.deliverErrorMPLinkCase();
                    break;
                case HwArbitrationDEFS.MSG_AIRPLANE_MODE_ON /* 2020 */:
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "DefaultState MSG_AIRPLANE_MODE_ON ", new Object[0]);
                    if (!HwArbitrationStateMachine.this.deliverErrorMPLinkCase()) {
                        int activeConnectType = HwArbitrationCommonUtils.getActiveConnectType(HwArbitrationStateMachine.this.mContext);
                        if (activeConnectType == 800) {
                            if (HwArbitrationStateMachine.this.mWifiMonitorState != HwArbitrationStateMachine.this.getCurrentState()) {
                                HwArbitrationStateMachine hwArbitrationStateMachine6 = HwArbitrationStateMachine.this;
                                hwArbitrationStateMachine6.transitionTo(hwArbitrationStateMachine6.mWifiMonitorState);
                                break;
                            }
                        } else if (activeConnectType == 801) {
                            if (HwArbitrationStateMachine.this.mCellMonitorState != HwArbitrationStateMachine.this.getCurrentState()) {
                                HwArbitrationStateMachine hwArbitrationStateMachine7 = HwArbitrationStateMachine.this;
                                hwArbitrationStateMachine7.transitionTo(hwArbitrationStateMachine7.mCellMonitorState);
                                break;
                            }
                        } else {
                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "cannot distinguish state, Enter InitialState", new Object[0]);
                            if (HwArbitrationStateMachine.this.mInitialState != HwArbitrationStateMachine.this.getCurrentState()) {
                                HwArbitrationStateMachine hwArbitrationStateMachine8 = HwArbitrationStateMachine.this;
                                hwArbitrationStateMachine8.transitionTo(hwArbitrationStateMachine8.mInitialState);
                                break;
                            }
                        }
                    }
                    break;
                case HwArbitrationDEFS.MSG_MPLINK_AI_DEVICE_COEX_MODE /* 2023 */:
                    HwArbitrationStateMachine hwArbitrationStateMachine9 = HwArbitrationStateMachine.this;
                    hwArbitrationStateMachine9.transitionTo(hwArbitrationStateMachine9.mMPLinkStartedState);
                    break;
                case HwArbitrationDEFS.MSG_VPN_STATE_OPEN /* 2024 */:
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "MSG_VPN_STATE_OPEN", new Object[0]);
                    HwArbitrationStateMachine.this.deliverErrorMPLinkCase();
                    break;
                case HwArbitrationDEFS.MSG_HISTREAM_TRIGGER_MPPLINK_INTERNAL /* 2026 */:
                    HwArbitrationStateMachine.this.mHiStreamTriggerOrStopMplink = true;
                    break;
                case HwArbitrationDEFS.MSG_Stop_MPLink_By_Notification /* 2032 */:
                    HwArbitrationCommonUtils.logE(HwArbitrationStateMachine.TAG, false, "MSG_Stop_MPLink_By_Notification", new Object[0]);
                    break;
                case HwArbitrationDEFS.MSG_Recovery_Flag_By_Notification /* 2034 */:
                    HwArbitrationCommonUtils.logE(HwArbitrationStateMachine.TAG, false, "MSG_Recovery_Flag_By_Notification", new Object[0]);
                    HwArbitrationStateMachine.this.mDenyByNotification = false;
                    break;
                case HwArbitrationDEFS.MSG_RECOVERY_FLAG_BY_WIFI_RX_BYTES /* 2038 */:
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "MSG_RECOVERY_FLAG_BY_WIFI_RX_BYTES", new Object[0]);
                    HwArbitrationStateMachine.this.mDenyByRxBytesInWifi = false;
                    break;
                case HwArbitrationDEFS.MSG_APP_STOP_MPLINK /* 2042 */:
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "DefaultState MSG_APP_STOP_MPLINK", new Object[0]);
                    HwArbitrationStateMachine.this.deliverErrorMPLinkCase();
                    break;
                case HwArbitrationDEFS.MSG_WM_HIGH_TEMPERATURE_STOP_MPLINK /* 2043 */:
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "DefaultState MSG_WM_HIGH_TEMPERATURE_STOP_MPLINK", new Object[0]);
                    HwArbitrationStateMachine.this.deliverErrorMPLinkCase();
                    break;
                case 3000:
                    if (message.arg1 != 0) {
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "HIRADIO_4G_SWITCH_3G_FAILED", new Object[0]);
                        break;
                    } else {
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "HIRADIO_4G_SWITCH_3G_SUCCESS", new Object[0]);
                        HwArbitrationStateMachine hwArbitrationStateMachine10 = HwArbitrationStateMachine.this;
                        hwArbitrationStateMachine10.showToast(hwArbitrationStateMachine10.mContext.getString(33685747));
                        break;
                    }
                default:
                    if (HwArbitrationStateMachine.this.getCurrentState() != null) {
                        HwArbitrationCommonUtils.logE(HwArbitrationStateMachine.TAG, false, "Unhandled Message: %{public}d in State: %{public}s", Integer.valueOf(message.what), HwArbitrationStateMachine.this.getCurrentState().getName());
                        break;
                    }
                    break;
            }
            return true;
        }
    }

    class InitialState extends State {
        InitialState() {
        }

        public void enter() {
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "Enter InitialState", new Object[0]);
            HwArbitrationStateMachine.this.isErrorMsgReceived = false;
            if (HwArbitrationStateMachine.mHwArbitrationAppBoostMap != null) {
                HwArbitrationStateMachine.mHwArbitrationAppBoostMap.clear();
            }
        }

        public void exit() {
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "Exit InitialState", new Object[0]);
        }

        public boolean processMessage(Message message) {
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "InitialState Msg: %{public}d", Integer.valueOf(message.what));
            int i = message.what;
            if (i == 1005) {
                HwArbitrationStateMachine hwArbitrationStateMachine = HwArbitrationStateMachine.this;
                hwArbitrationStateMachine.transitionTo(hwArbitrationStateMachine.mWifiMonitorState);
            } else if (i == 1009) {
                HwArbitrationStateMachine hwArbitrationStateMachine2 = HwArbitrationStateMachine.this;
                hwArbitrationStateMachine2.transitionTo(hwArbitrationStateMachine2.mCellMonitorState);
            } else if (i == 2015 || i == 2027) {
                if (2027 == message.what) {
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "MSG_MPLINK_ERROR_HANDLER", new Object[0]);
                } else {
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "MSG_MPLINK_STOP_COEX_SUCC", new Object[0]);
                }
                int activeConnectType = HwArbitrationCommonUtils.getActiveConnectType(HwArbitrationStateMachine.this.mContext);
                if (activeConnectType == 800) {
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "InitialState WIFI_NETWORK", new Object[0]);
                    HwArbitrationStateMachine hwArbitrationStateMachine3 = HwArbitrationStateMachine.this;
                    hwArbitrationStateMachine3.transitionTo(hwArbitrationStateMachine3.mWifiMonitorState);
                } else if (activeConnectType != 801) {
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "cannot distinguish state", new Object[0]);
                } else {
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "InitialState CELL_NETWORK", new Object[0]);
                    HwArbitrationStateMachine hwArbitrationStateMachine4 = HwArbitrationStateMachine.this;
                    hwArbitrationStateMachine4.transitionTo(hwArbitrationStateMachine4.mCellMonitorState);
                }
            } else {
                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "Message:%{public}d did't process in InitialState, go to parent state", Integer.valueOf(message.what));
                return false;
            }
            return true;
        }
    }

    class WifiMonitorState extends State {
        private HwAPPStateInfo appInfo;

        WifiMonitorState() {
        }

        public void enter() {
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "Enter WifiMonitorState", new Object[0]);
            HwArbitrationStateMachine.this.updateNetworkTypeForArbitrationMap(800);
        }

        public void exit() {
            HwArbitrationStateMachine.this.resetPingPongFlag();
            HwArbitrationStateMachine.this.removeMessages(HwArbitrationDEFS.MSG_SET_PINGPONG_CELL_BAD_FALSE);
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "Exit WifiMonitorState", new Object[0]);
        }

        public boolean processMessage(Message message) {
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "WifiMonitorState Msg: %{public}d", Integer.valueOf(message.what));
            int i = message.what;
            if (i != 100) {
                if (i != 101) {
                    if (i != 1005) {
                        if (i == 1006) {
                            HwArbitrationStateMachine.this.mHwWifiBoost.stopAllBoost();
                            HwArbitrationStateMachine hwArbitrationStateMachine = HwArbitrationStateMachine.this;
                            hwArbitrationStateMachine.transitionTo(hwArbitrationStateMachine.mInitialState);
                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "WifiMonitor transitionTo InitialState", new Object[0]);
                        } else if (i == 1009) {
                            HwArbitrationStateMachine.this.mHwWifiBoost.stopAllBoost();
                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "WifiMonitorState transitionTo CellmonitorState", new Object[0]);
                            HwArbitrationStateMachine hwArbitrationStateMachine2 = HwArbitrationStateMachine.this;
                            hwArbitrationStateMachine2.transitionTo(hwArbitrationStateMachine2.mCellMonitorState);
                        } else if (i == 1010) {
                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "in WifiMonitorState don't process MSG_CELL_STATE_DISCONNECTED", new Object[0]);
                        } else if (i != 1012) {
                            if (i != 1017) {
                                if (i == 2003) {
                                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "mplink duplicate callback response", new Object[0]);
                                } else if (i != 2035) {
                                    switch (i) {
                                        case 103:
                                            break;
                                        case 104:
                                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "In WifiMonitorState state, message MSG_GAME_ENTER_PVP_BATTLE received", new Object[0]);
                                            this.appInfo = (HwAPPStateInfo) message.obj;
                                            HwAPPStateInfo hwAPPStateInfo = this.appInfo;
                                            if (hwAPPStateInfo != null) {
                                                HwArbitrationStateMachine.this.handleGeneralGameStart(hwAPPStateInfo);
                                                HwArbitrationStateMachine.this.handleGamePvpStart(this.appInfo);
                                                break;
                                            }
                                            break;
                                        case 105:
                                            this.appInfo = (HwAPPStateInfo) message.obj;
                                            HwArbitrationStateMachine.this.handleGamePvpEnd(this.appInfo);
                                            break;
                                        case 106:
                                            this.appInfo = (HwAPPStateInfo) message.obj;
                                            HwArbitrationStateMachine.this.handleStreamingStart(this.appInfo);
                                            break;
                                        case 107:
                                            this.appInfo = (HwAPPStateInfo) message.obj;
                                            HwArbitrationStateMachine.this.handleStreamingEnd(this.appInfo);
                                            HwArbitrationStateMachine.this.resetPingPongFlag();
                                            break;
                                        default:
                                            switch (i) {
                                                case 109:
                                                    break;
                                                case 110:
                                                    HwArbitrationStateMachine.this.resetPingPongFlag();
                                                    break;
                                                case 111:
                                                case 113:
                                                    HwArbitrationStateMachine.this.startMpLinkCoex(message, false);
                                                    break;
                                                case 112:
                                                case HwArbitrationDEFS.MSG_INSTANT_TRAVEL_APP_END /* 114 */:
                                                    HwArbitrationStateMachine.this.resetPingPongFlag();
                                                    break;
                                                default:
                                                    return false;
                                            }
                                    }
                                } else {
                                    HwArbitrationCommonUtils.logE(HwArbitrationStateMachine.TAG, false, "MSG_SET_PINGPONG_CELL_BAD_FALSE in wifi monitor", new Object[0]);
                                    HwArbitrationStateMachine.this.resetPingPongFlag();
                                }
                            } else if (HwArbitrationFunction.isPvpScene()) {
                                HwArbitrationStateMachine.this.mHwWifiBoost.setPMMode(6);
                                HwArbitrationStateMachine.this.mHwWifiBoost.setPMMode(4);
                                HwArbitrationStateMachine.this.mHwWifiBoost.setGameBoosting(true);
                                HwArbitrationStateMachine.this.mHwWifiBoost.limitedSpeed(1, 1, 1);
                            }
                        } else if (HwArbitrationFunction.isPvpScene()) {
                            HwArbitrationStateMachine.this.mHwWifiBoost.setPMMode(3);
                            HwArbitrationStateMachine.this.mHwWifiBoost.setGameBoosting(false);
                            HwArbitrationStateMachine.this.mHwWifiBoost.setPMMode(7);
                            HwArbitrationStateMachine.this.mHwWifiBoost.limitedSpeed(1, 0, 0);
                        }
                    }
                }
                this.appInfo = (HwAPPStateInfo) message.obj;
                HwArbitrationStateMachine.this.handleGamePvpEnd(this.appInfo);
                HwArbitrationStateMachine.this.handleGeneralGameEnd(this.appInfo);
            } else {
                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "In WifiMonitorState state, MSG_GAME_STATE_START received", new Object[0]);
                this.appInfo = (HwAPPStateInfo) message.obj;
                HwAPPStateInfo hwAPPStateInfo2 = this.appInfo;
                if (hwAPPStateInfo2 != null) {
                    HwArbitrationStateMachine.this.handleGeneralGameStart(hwAPPStateInfo2);
                    if (HwArbitrationCommonUtils.IS_HIDATA2_ENABLED) {
                        HwArbitrationStateMachine.this.setStateMachineHashMap(this.appInfo, 800, false, false, 0);
                    }
                }
            }
            return true;
        }
    }

    class CellMonitorState extends State {
        private int actions = 0;
        private int appUID = -1;
        private HwAPPStateInfo mAppInfo = null;
        private int uid;

        CellMonitorState() {
        }

        public void enter() {
            String str;
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "Enter CellMonitorState", new Object[0]);
            HwArbitrationStateMachine.this.updateNetworkTypeForArbitrationMap(801);
            HwArbitrationStateMachine.this.mHwAPPQoEManager = HwAPPQoEManager.getInstance();
            this.mAppInfo = HwArbitrationStateMachine.this.mHwAPPQoEManager == null ? null : HwArbitrationStateMachine.this.mHwAPPQoEManager.getCurAPPStateInfo();
            if (this.mAppInfo == null) {
                str = "mAppInfo is null ";
            } else {
                str = "mAppInfo:" + this.mAppInfo.toString();
            }
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, str, new Object[0]);
            if (HwArbitrationFunction.isDataTechSuitable() && enterMplinkFromCell(this.mAppInfo)) {
                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "begin HiRadio Cellular boost", new Object[0]);
                handleCurrentScene(this.mAppInfo);
                handleHiStreamScene();
            }
        }

        private boolean enterMplinkFromCell(HwAPPStateInfo appInfo) {
            HwArbitrationAppBoostInfo boostInfo;
            if (HwArbitrationStateMachine.this.isBoostInfoAvailable(appInfo) && (boostInfo = (HwArbitrationAppBoostInfo) HwArbitrationStateMachine.mHwArbitrationAppBoostMap.get(Integer.valueOf(appInfo.mAppUID))) != null && boostInfo.getHwAPPStateInfo() != null && boostInfo.getHwAPPStateInfo().getIsMplinkEnteredFromCell() == 1) {
                return false;
            }
            return true;
        }

        private boolean isGameEnteredFromCellMonitorState(int uid2) {
            HwArbitrationAppBoostInfo appBoostInfo;
            HwAPPStateInfo appStateInfo;
            if (HwArbitrationStateMachine.mHwArbitrationAppBoostMap == null || (appBoostInfo = (HwArbitrationAppBoostInfo) HwArbitrationStateMachine.mHwArbitrationAppBoostMap.get(Integer.valueOf(uid2))) == null || (appStateInfo = appBoostInfo.getHwAPPStateInfo()) == null || appStateInfo.getIsMplinkEnteredFromCell() != 1) {
                return false;
            }
            return true;
        }

        private void handleCurrentScene(HwAPPStateInfo appInfo) {
            if (appInfo != null && -1 != appInfo.mAppUID) {
                int i = appInfo.mAppType;
                if (i == 1000) {
                    HwArbitrationStateMachine.this.sendMessage(109, appInfo);
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "CurrentScene:MSG_INSTANT_APP_START", new Object[0]);
                } else if (i != 2000) {
                    if (i != 4000) {
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "CurrentScene:no AppQoE monitored scenes at foreground", new Object[0]);
                    } else {
                        HwArbitrationStateMachine.this.sendMessage(106, appInfo);
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "CurrentScene:MSG_STREAM_APP_START", new Object[0]);
                    }
                } else if (appInfo.mScenceId == 200002) {
                    HwArbitrationStateMachine.this.sendMessage(104, appInfo);
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "CurrentScene:MSG_GAME_ENTER_PVP_BATTLE", new Object[0]);
                } else if (appInfo.mScenceId == 200001) {
                    HwArbitrationStateMachine.this.sendMessage(100, appInfo);
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "CurrentScene:MSG_GAME_STATE_START", new Object[0]);
                }
                HwArbitrationStateMachine.this.mHwHiRadioBoost.BrainAppStateNotifyDSBooster(appInfo, 0, 0);
            }
        }

        private void handleHiStreamScene() {
            if (HwArbitrationStateMachine.this.mCurrentStreamAppInfo != null) {
                int actions2 = findActionsNeedStart(HwArbitrationStateMachine.this.mCurrentStreamAppInfo, getActionsConfig(HwArbitrationStateMachine.this.mCurrentStreamAppInfo));
                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "Action to start:%{public}d", Integer.valueOf(actions2));
                if (actions2 > 0) {
                    HwArbitrationStateMachine hwArbitrationStateMachine = HwArbitrationStateMachine.this;
                    hwArbitrationStateMachine.setStateMachineHashMap(hwArbitrationStateMachine.mCurrentStreamAppInfo, 801, false, false, actions2);
                }
            }
            HwArbitrationStateMachine.this.mHwHiStreamManager = HwHiStreamManager.getInstance();
            HwAPPStateInfo appInfo = HwArbitrationStateMachine.this.mHwHiStreamManager == null ? null : HwArbitrationStateMachine.this.mHwHiStreamManager.getCurStreamAppInfo();
            if (appInfo != null && -1 != appInfo.mAppUID) {
                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "AppStreamInfo:%{public}s", appInfo.toString());
                HwArbitrationStateMachine.this.mHwHiRadioBoost.BrainAppStateNotifyDSBooster(appInfo, 0, HwArbitrationStateMachine.this.hiStreamAppState);
            }
        }

        private void stopAllCellOptimize() {
            if (HwArbitrationStateMachine.mHwArbitrationAppBoostMap == null || HwArbitrationStateMachine.mHwArbitrationAppBoostMap.isEmpty()) {
                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "stopAllCellOptimize: no Optimize to recover ", new Object[0]);
                return;
            }
            for (Integer appUid : HwArbitrationStateMachine.mHwArbitrationAppBoostMap.keySet()) {
                HwAPPStateInfo tempData = new HwAPPStateInfo();
                tempData.mAppUID = appUid.intValue();
                stopUidOptimiztion(tempData);
            }
        }

        private void stopUidOptimiztion(HwAPPStateInfo appInfo) {
            HwArbitrationAppBoostInfo boostInfo;
            if (appInfo != null && HwArbitrationStateMachine.mHwArbitrationAppBoostMap != null && !HwArbitrationStateMachine.mHwArbitrationAppBoostMap.isEmpty() && (boostInfo = (HwArbitrationAppBoostInfo) HwArbitrationStateMachine.mHwArbitrationAppBoostMap.get(Integer.valueOf(appInfo.mAppUID))) != null) {
                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "stopUidOptimiztion enter: boostInfo is %{public}s", boostInfo.toString());
                HwAPPStateInfo tempData = new HwAPPStateInfo();
                tempData.copyObjectValue(appInfo);
                tempData.mAppId = boostInfo.getAppID();
                tempData.mAppUID = boostInfo.getBoostUID();
                tempData.mScenceId = boostInfo.getSceneId();
                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "stopUidOptimiztion: for %{public}d, solution to stop is %{public}d", Integer.valueOf(tempData.mAppId), Integer.valueOf(boostInfo.getSolution()));
            }
        }

        private int findActionsNeedStart(HwAPPStateInfo appInfo, int actions2) {
            HwArbitrationAppBoostInfo boostInfo;
            if (appInfo == null) {
                return -1;
            }
            if (actions2 <= 0) {
                return actions2;
            }
            if (HwArbitrationStateMachine.mHwArbitrationAppBoostMap == null || HwArbitrationStateMachine.mHwArbitrationAppBoostMap.isEmpty() || (boostInfo = (HwArbitrationAppBoostInfo) HwArbitrationStateMachine.mHwArbitrationAppBoostMap.get(Integer.valueOf(appInfo.mAppUID))) == null) {
                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "findActions: actions to start is %{public}d", Integer.valueOf(actions2));
                return actions2;
            }
            int lastActions = (~boostInfo.getSolution()) & actions2;
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "findActions: actions started is %{public}d, actions to judge is %{public}d, actions going to start is %{public}d", Integer.valueOf(boostInfo.getSolution()), Integer.valueOf(actions2), Integer.valueOf(lastActions));
            return lastActions;
        }

        private int findActionsNeedStop(HwAPPStateInfo appInfo, int configActions) {
            if (configActions <= 0 || appInfo == null) {
                return configActions;
            }
            if (HwArbitrationStateMachine.mHwArbitrationAppBoostMap == null || HwArbitrationStateMachine.mHwArbitrationAppBoostMap.isEmpty()) {
                HwArbitrationCommonUtils.logE(HwArbitrationStateMachine.TAG, false, "findActionsNeedStop: no started actions for uid: %{public}d", Integer.valueOf(appInfo.mAppUID));
                return -1;
            }
            HwArbitrationAppBoostInfo boostInfo = (HwArbitrationAppBoostInfo) HwArbitrationStateMachine.mHwArbitrationAppBoostMap.get(Integer.valueOf(appInfo.mAppUID));
            if (boostInfo == null) {
                return -1;
            }
            int actionsStop = configActions & boostInfo.getSolution();
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "findActionsNeedStop: actions started is %{public}d, actions to judge is %{public}d, actions going to remain is %{public}d", Integer.valueOf(boostInfo.getSolution()), Integer.valueOf(configActions), Integer.valueOf(actionsStop));
            return (~actionsStop) & boostInfo.getSolution();
        }

        private int getActionsToStart(HwAPPStateInfo appInfo) {
            return findActionsNeedStart(appInfo, getActionsConfig(appInfo));
        }

        private int getActionsConfig(HwAPPStateInfo appInfo) {
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "Enter getActionsConfig", new Object[0]);
            HwArbitrationStateMachine.this.mHwAPPQoEResourceManger = HwAPPQoEResourceManger.getInstance();
            int actions2 = 0;
            if (!(HwArbitrationStateMachine.this.mHwAPPQoEResourceManger == null || appInfo == null)) {
                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, appInfo.toString(), new Object[0]);
                actions2 = HwArbitrationStateMachine.this.mHwAPPQoEResourceManger.getScenceAction(appInfo.mAppType, appInfo.mAppId, appInfo.mScenceId);
                if (actions2 < 0) {
                    HwArbitrationCommonUtils.logE(HwArbitrationStateMachine.TAG, false, "get SceneAction from AppQoE xmlConfg failed", new Object[0]);
                }
            }
            return actions2;
        }

        public void exit() {
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "Exit CellMonitorState", new Object[0]);
            stopAllCellOptimize();
            HwArbitrationStateMachine.this.resetPingPongFlag();
        }

        public boolean processMessage(Message message) {
            int i = message.what;
            if (i != 100) {
                if (i != 101) {
                    if (i == 120) {
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "in CellMonitor: MSG_STREAMING_APP_FOREGROUND", new Object[0]);
                        this.mAppInfo = (HwAPPStateInfo) message.obj;
                        HwAPPStateInfo hwAPPStateInfo = this.mAppInfo;
                        if (hwAPPStateInfo != null && hwAPPStateInfo.mScenceId == 100106 && HwArbitrationFunction.isDataTechSuitable()) {
                            int actions2 = getActionsToStart(this.mAppInfo);
                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "Start HiStream actions:%{public}d", Integer.valueOf(actions2));
                            HwArbitrationStateMachine.this.setStateMachineHashMap(this.mAppInfo, 801, false, false, actions2);
                        }
                    } else if (i == 121) {
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "in CellMonitor: MSG_STREAMING_APP_BACKGROUND", new Object[0]);
                        this.mAppInfo = (HwAPPStateInfo) message.obj;
                        HwAPPStateInfo hwAPPStateInfo2 = this.mAppInfo;
                        if (!(hwAPPStateInfo2 == null || hwAPPStateInfo2.mScenceId != 100106 || HwArbitrationStateMachine.mHwArbitrationAppBoostMap == null)) {
                            stopUidOptimiztion(this.mAppInfo);
                            HwArbitrationStateMachine.mHwArbitrationAppBoostMap.remove(this.mAppInfo);
                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "Stop HiStream actions", new Object[0]);
                        }
                    } else if (i != 1005) {
                        if (i != 1012) {
                            if (i != 1017) {
                                if (!(i == 1106 || i == 1108)) {
                                    if (i == 2020) {
                                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "In CellMonitorState MSG_AIRPLANE_MODE_ON", new Object[0]);
                                        HwArbitrationStateMachine hwArbitrationStateMachine = HwArbitrationStateMachine.this;
                                        hwArbitrationStateMachine.transitionTo(hwArbitrationStateMachine.mInitialState);
                                    } else if (i == 1009) {
                                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "In CellMonitorState don't process MSG_CELL_STATE_CONNECTED", new Object[0]);
                                    } else if (i == 1010) {
                                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "CellMonitorState:MSG_CELL_STATE_DISCONNECT", new Object[0]);
                                        HwArbitrationStateMachine hwArbitrationStateMachine2 = HwArbitrationStateMachine.this;
                                        hwArbitrationStateMachine2.transitionTo(hwArbitrationStateMachine2.mInitialState);
                                    } else if (i != 1022) {
                                        if (i != 1023) {
                                            switch (i) {
                                                case 103:
                                                    break;
                                                case 104:
                                                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "In CellMonitorState MSG_GAME_ENTER_PVP_BATTLE", new Object[0]);
                                                    this.mAppInfo = (HwAPPStateInfo) message.obj;
                                                    HwAPPStateInfo hwAPPStateInfo3 = this.mAppInfo;
                                                    if (hwAPPStateInfo3 != null) {
                                                        this.actions = getActionsConfig(hwAPPStateInfo3);
                                                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "ENTER PVP: getActionConfig:%{public}d, actions = %{public}d", Integer.valueOf(getActionsConfig(this.mAppInfo)), Integer.valueOf(this.actions));
                                                        this.actions = findActionsNeedStart(this.mAppInfo, this.actions);
                                                        HwArbitrationStateMachine.this.updateStateMachineHashMap(this.mAppInfo, 801, false, false, this.actions);
                                                        break;
                                                    }
                                                    break;
                                                case 105:
                                                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "In CellMonitorState MSG_GAME_EXIT_PVP_BATTLE", new Object[0]);
                                                    this.mAppInfo = (HwAPPStateInfo) message.obj;
                                                    if (!(this.mAppInfo == null || HwArbitrationStateMachine.mHwArbitrationAppBoostMap == null || HwArbitrationStateMachine.mHwArbitrationAppBoostMap.get(Integer.valueOf(this.mAppInfo.mAppUID)) == null)) {
                                                        this.actions = getActionsConfig(this.mAppInfo);
                                                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "EXIT PVP:getActionConfig:%{public}d", Integer.valueOf(this.actions));
                                                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "findActionsNeedStop:StopAction:%{public}d", Integer.valueOf(findActionsNeedStop(this.mAppInfo, this.actions)));
                                                        HwArbitrationStateMachine.this.setStateMachineHashMap(this.mAppInfo, 801, false, false, this.actions);
                                                        break;
                                                    }
                                                case 106:
                                                    this.mAppInfo = (HwAPPStateInfo) message.obj;
                                                    if (this.mAppInfo != null && HwArbitrationFunction.isDataTechSuitable()) {
                                                        int actions3 = getActionsToStart(this.mAppInfo);
                                                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "histream actions:%{public}d", Integer.valueOf(actions3));
                                                        HwArbitrationStateMachine.this.setStateMachineHashMap(this.mAppInfo, 801, false, false, actions3);
                                                        HwArbitrationStateMachine.this.printMap(this.mAppInfo.mAppUID);
                                                        break;
                                                    }
                                                case 107:
                                                    this.mAppInfo = (HwAPPStateInfo) message.obj;
                                                    if (!(this.mAppInfo == null || HwArbitrationStateMachine.mHwArbitrationAppBoostMap == null)) {
                                                        stopUidOptimiztion(this.mAppInfo);
                                                        HwArbitrationStateMachine.mHwArbitrationAppBoostMap.remove(Integer.valueOf(this.mAppInfo.mAppUID));
                                                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "Stop HiStream actions", new Object[0]);
                                                        break;
                                                    }
                                                default:
                                                    switch (i) {
                                                        case 109:
                                                        case 111:
                                                        case 113:
                                                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "In CellMonitorState need startOptimizeActionsForApp", new Object[0]);
                                                            this.mAppInfo = (HwAPPStateInfo) message.obj;
                                                            HwAPPStateInfo hwAPPStateInfo4 = this.mAppInfo;
                                                            if (hwAPPStateInfo4 != null) {
                                                                this.actions = getActionsToStart(hwAPPStateInfo4);
                                                                if (this.actions > 0 && HwArbitrationFunction.isDataTechSuitable()) {
                                                                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "begin HiRadioBoost, UID = %{public}d, actions =%{public}d", Integer.valueOf(this.mAppInfo.mAppUID), Integer.valueOf(this.actions));
                                                                    HwArbitrationStateMachine.this.setStateMachineHashMap(this.mAppInfo, 801, false, false, this.actions);
                                                                    HwArbitrationStateMachine.this.printMap(this.mAppInfo.mAppUID);
                                                                    break;
                                                                }
                                                            }
                                                            break;
                                                        case 110:
                                                        case 112:
                                                        case HwArbitrationDEFS.MSG_INSTANT_TRAVEL_APP_END /* 114 */:
                                                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "In CellMonitorState need stopOptimizedActionsForApp, message is %{public}d", Integer.valueOf(message.what));
                                                            this.mAppInfo = (HwAPPStateInfo) message.obj;
                                                            if (!(this.mAppInfo == null || HwArbitrationStateMachine.mHwArbitrationAppBoostMap == null)) {
                                                                HwArbitrationAppBoostInfo boostInfo = (HwArbitrationAppBoostInfo) HwArbitrationStateMachine.mHwArbitrationAppBoostMap.get(Integer.valueOf(this.mAppInfo.mAppUID));
                                                                if (boostInfo != null && boostInfo.getSceneId() == 100105) {
                                                                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "HiStream Audio is still at background", new Object[0]);
                                                                    break;
                                                                } else {
                                                                    stopUidOptimiztion(this.mAppInfo);
                                                                    HwArbitrationStateMachine.mHwArbitrationAppBoostMap.remove(Integer.valueOf(this.mAppInfo.mAppUID));
                                                                    break;
                                                                }
                                                            }
                                                            break;
                                                        case 115:
                                                            break;
                                                        default:
                                                            return false;
                                                    }
                                            }
                                        }
                                    } else if (HwArbitrationStateMachine.this.mHwAPPQoEManager != null) {
                                        this.mAppInfo = HwArbitrationStateMachine.this.mHwAPPQoEManager.getCurAPPStateInfo();
                                        HwAPPStateInfo hwAPPStateInfo5 = this.mAppInfo;
                                        if (hwAPPStateInfo5 != null) {
                                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "AppInfo:%{public}s", hwAPPStateInfo5.toString());
                                            this.actions = getActionsToStart(this.mAppInfo);
                                            HwArbitrationStateMachine.this.setStateMachineHashMap(this.mAppInfo, 801, false, false, this.actions);
                                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "Back to RAT suitable, start actions = %{public}d", Integer.valueOf(this.actions));
                                            handleHiStreamScene();
                                        }
                                    }
                                }
                                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "In CellMonitorState process MSG_GAME/APP_STATE_BAD", new Object[0]);
                            } else {
                                HwArbitrationStateMachine hwArbitrationStateMachine3 = HwArbitrationStateMachine.this;
                                HwAPPQoEManager instance = HwAPPQoEManager.getInstance();
                                hwArbitrationStateMachine3.mHwAPPQoEManager = instance;
                                if (instance != null) {
                                    HwAPPStateInfo curAPPStateInfo = HwArbitrationStateMachine.this.mHwAPPQoEManager.getCurAPPStateInfo();
                                    this.mAppInfo = curAPPStateInfo;
                                    if (curAPPStateInfo != null && HwArbitrationFunction.isDataTechSuitable()) {
                                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "%{public}s", this.mAppInfo.toString());
                                        handleCurrentScene(this.mAppInfo);
                                    }
                                }
                                handleHiStreamScene();
                            }
                        }
                        stopAllCellOptimize();
                    } else {
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "In CellMonitorState, transition to WifiMonitorState", new Object[0]);
                        HwArbitrationStateMachine hwArbitrationStateMachine4 = HwArbitrationStateMachine.this;
                        hwArbitrationStateMachine4.transitionTo(hwArbitrationStateMachine4.mWifiMonitorState);
                    }
                }
                this.mAppInfo = (HwAPPStateInfo) message.obj;
                if (!(this.mAppInfo == null || HwArbitrationStateMachine.mHwArbitrationAppBoostMap == null)) {
                    stopUidOptimiztion(this.mAppInfo);
                    HwArbitrationStateMachine.mHwArbitrationAppBoostMap.remove(Integer.valueOf(this.mAppInfo.mAppUID));
                }
            } else {
                this.mAppInfo = (HwAPPStateInfo) message.obj;
                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "In CellMonitorState MSG_GAME_STATE_START, mCurrentRAT is %{public}d", Integer.valueOf(HwArbitrationFunction.getDataTech()));
                HwAPPStateInfo hwAPPStateInfo6 = this.mAppInfo;
                if (hwAPPStateInfo6 != null) {
                    this.actions = getActionsConfig(hwAPPStateInfo6);
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "ENTER GAME: getActionConfig,actions = %{public}d", Integer.valueOf(this.actions));
                    this.actions = findActionsNeedStart(this.mAppInfo, this.actions);
                    HwArbitrationStateMachine.this.setStateMachineHashMap(this.mAppInfo, 801, false, false, this.actions);
                }
            }
            return true;
        }
    }

    class MPLinkStartingState extends State {
        HwAPPStateInfo appStateInfo;

        MPLinkStartingState() {
        }

        public void enter() {
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "Enter MPLinkStartingState", new Object[0]);
            HwArbitrationStateMachine.this.sendMessageDelayed(HwArbitrationDEFS.MSG_MPLINK_COEXIST_TIMEOUT, HwArbitrationDEFS.TIMEOUT_FOR_QUERY_QOE_WM);
        }

        public void exit() {
            HwArbitrationStateMachine.this.mIsMpLinkError = false;
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "exit MPLinkStartingState", new Object[0]);
            HwArbitrationStateMachine.this.removeMessages(HwArbitrationDEFS.MSG_MPLINK_COEXIST_TIMEOUT);
            this.appStateInfo = null;
        }

        public boolean processMessage(Message message) {
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "MPLinkStartingState Msg: %{public}d", Integer.valueOf(message.what));
            int i = message.what;
            if (i == 6) {
                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "WiFi and Celluar coexist successful", new Object[0]);
            } else if (i == 101 || i == 107 || i == 110) {
                HwAPPStateInfo appEndInfo = (HwAPPStateInfo) message.obj;
                if (!(appEndInfo == null || this.appStateInfo == null || appEndInfo.mAppUID != this.appStateInfo.mAppUID)) {
                    if (!HwArbitrationFunction.isStreamingScene(this.appStateInfo) || this.appStateInfo.mScenceId == appEndInfo.mScenceId) {
                        HwArbitrationStateMachine.this.mHwArbitrationChrImpl.updateRequestMplinkChr(this.appStateInfo, 10);
                        HwArbitrationStateMachine hwArbitrationStateMachine = HwArbitrationStateMachine.this;
                        hwArbitrationStateMachine.transitionTo(hwArbitrationStateMachine.mMPLinkStoppingState);
                    } else {
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "appEnd SceneID is not the same as appState", new Object[0]);
                    }
                }
            } else {
                if (i != 115) {
                    if (i == 1009) {
                        HwArbitrationStateMachine hwArbitrationStateMachine2 = HwArbitrationStateMachine.this;
                        hwArbitrationStateMachine2.transitionTo(hwArbitrationStateMachine2.mMPLinkStoppingState);
                    } else if (i == 2028) {
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "Coexist timeout", new Object[0]);
                        HwArbitrationStateMachine.this.mHwArbitrationChrImpl.updateRequestMplinkChr(this.appStateInfo, 2);
                        HwArbitrationStateMachine hwArbitrationStateMachine3 = HwArbitrationStateMachine.this;
                        hwArbitrationStateMachine3.transitionTo(hwArbitrationStateMachine3.mMPLinkStoppingState);
                    } else if (i == 2003) {
                        HwArbitrationStateMachine hwArbitrationStateMachine4 = HwArbitrationStateMachine.this;
                        hwArbitrationStateMachine4.transitionTo(hwArbitrationStateMachine4.mMPLinkStartedState);
                    } else if (i != 2004) {
                        switch (i) {
                            case HwArbitrationDEFS.MSG_STREAMING_VIDEO_BAD /* 1106 */:
                            case HwArbitrationDEFS.MSG_GAME_WAR_STATE_BAD /* 1107 */:
                            case HwArbitrationDEFS.MSG_STREAMING_AUDIO_BAD /* 1108 */:
                                break;
                            default:
                                return false;
                        }
                    } else {
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "MPLinkStartingState start COEX error", new Object[0]);
                        HwArbitrationStateMachine.this.mHwArbitrationChrImpl.updateRequestMplinkChr(this.appStateInfo, 2);
                        HwArbitrationStateMachine hwArbitrationStateMachine5 = HwArbitrationStateMachine.this;
                        hwArbitrationStateMachine5.transitionTo(hwArbitrationStateMachine5.mMPLinkStoppingState);
                    }
                }
                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "defer BAD MSG", new Object[0]);
                this.appStateInfo = (HwAPPStateInfo) message.obj;
                if (!HwArbitrationStateMachine.this.hasDeferredMessages(message.what)) {
                    HwArbitrationStateMachine.this.deferMessage(message);
                }
            }
            return true;
        }
    }

    class MPLinkStoppingState extends State {
        MPLinkStoppingState() {
        }

        public void enter() {
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "Enter MPLinkStoppingState", new Object[0]);
            HwArbitrationStateMachine.this.sendMessageDelayed(HwArbitrationDEFS.MSG_MPLINK_COEXIST_TIMEOUT, HwArbitrationDEFS.TIMEOUT_FOR_QUERY_QOE_WM);
        }

        public void exit() {
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "Exit MPLinkStoppingState", new Object[0]);
            HwArbitrationStateMachine.this.mIsMpLinkError = false;
            HwArbitrationStateMachine.this.removeMessages(HwArbitrationDEFS.MSG_MPLINK_COEXIST_TIMEOUT);
        }

        public boolean processMessage(Message message) {
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "MPLinkStoppingState Msg: %{public}d", Integer.valueOf(message.what));
            int i = message.what;
            if (i == 2003) {
                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "MSG_MPLINK_COEX_MODE: stop COEX error ", new Object[0]);
                HwArbitrationStateMachine hwArbitrationStateMachine = HwArbitrationStateMachine.this;
                hwArbitrationStateMachine.transitionTo(hwArbitrationStateMachine.mInitialState);
                HwArbitrationStateMachine hwArbitrationStateMachine2 = HwArbitrationStateMachine.this;
                hwArbitrationStateMachine2.deferMessage(hwArbitrationStateMachine2.obtainMessage(HwArbitrationDEFS.MSG_MPLINK_STOP_COEX_SUCC));
            } else if (i == 2004) {
                HwArbitrationStateMachine hwArbitrationStateMachine3 = HwArbitrationStateMachine.this;
                hwArbitrationStateMachine3.transitionTo(hwArbitrationStateMachine3.mInitialState);
                HwArbitrationStateMachine hwArbitrationStateMachine4 = HwArbitrationStateMachine.this;
                hwArbitrationStateMachine4.deferMessage(hwArbitrationStateMachine4.obtainMessage(HwArbitrationDEFS.MSG_MPLINK_STOP_COEX_SUCC));
            } else if (i != 2011) {
                if (i != 2028) {
                    return false;
                }
                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "clear Coexist timeout", new Object[0]);
                HwArbitrationStateMachine hwArbitrationStateMachine5 = HwArbitrationStateMachine.this;
                hwArbitrationStateMachine5.transitionTo(hwArbitrationStateMachine5.mInitialState);
                HwArbitrationStateMachine hwArbitrationStateMachine6 = HwArbitrationStateMachine.this;
                hwArbitrationStateMachine6.deferMessage(hwArbitrationStateMachine6.obtainMessage(HwArbitrationDEFS.MSG_MPLINK_ERROR_HANDLER));
            }
            return true;
        }
    }

    class MPLinkStartedState extends State {
        private static final int FIRST_TIME_THRESHOLD = 10;
        private static final int GAME_STATE_BAD_COUNT_FOUR = 4;
        private static final int GAME_STATE_BAD_COUNT_THREE = 3;
        private static final int GAME_STATE_BAD_COUNT_TWO = 2;
        private static final int INSTANT_APP = 2;
        private static final int SECOND_TIME_THRESHOLD = 20;
        private static final int STREAMING_APP = 3;
        private static final int THIRD_TIME_THRESHOLD = 30;
        private HwAPPStateInfo appInfo;
        private HwArbitrationAppBoostInfo boostInfo = null;
        private int cellDetectedCount = 0;
        private long cellToWifiTimestamp = 0;
        private long channelDetectionTimeStamp = 0;
        private long mStartWifiBytes = 0;
        private int mplinkErrorCode = -1;
        private long pingPongTMWiFi_Good = 0;
        private int punishWiFiGoodCount = 0;
        private boolean punishWifiDetectImprecise;
        private int stopMplinkReason = -1;
        private boolean trgPingPongWiFi_Good;
        private int uid;
        private int wifiDetectedCount = 0;
        private boolean wifiGoodFlag = true;

        MPLinkStartedState() {
        }

        public void enter() {
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "Enter MPLinkStartedState", new Object[0]);
            this.trgPingPongWiFi_Good = false;
            this.punishWifiDetectImprecise = false;
            HwArbitrationStateMachine.this.needStopCoex = false;
        }

        public void exit() {
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "Exit MPLinkStartedState", new Object[0]);
            HwArbitrationStateMachine.this.isMpLinkBinding = false;
            HwArbitrationStateMachine.this.mCoexCount = 0;
            HwArbitrationStateMachine.this.mMpLinkCount = 0;
            this.cellDetectedCount = 0;
            this.wifiDetectedCount = 0;
            this.channelDetectionTimeStamp = 0;
            if (HwArbitrationStateMachine.mHwArbitrationAppBoostMap != null) {
                HwArbitrationStateMachine.this.stopMpLinkBindForAllApps();
                HwArbitrationStateMachine.mHwArbitrationAppBoostMap.clear();
            }
            HwArbitrationStateMachine.this.mIsMpLinkError = false;
            this.wifiGoodFlag = true;
            this.stopMplinkReason = -1;
            this.mplinkErrorCode = -1;
            HwArbitrationStateMachine.this.setMobileDataAlwaysOn(0);
            HwArbitrationStateMachine.this.removeMessages(HwArbitrationDEFS.MSG_SET_PINGPONG_WIFI_GOOD_FALSE);
            HwArbitrationDisplay.getInstance(HwArbitrationStateMachine.this.mContext).requestDataMonitor(false, 1);
            HwAPPStateInfo gameInfo = HwArbitrationCallbackImpl.getInstance(HwArbitrationStateMachine.this.mContext).getGameAppStateInfo();
            if (gameInfo != null) {
                gameInfo.setIsMplinkEnteredFromCell(-1);
            }
        }

        public boolean processMessage(Message message) {
            int i = message.what;
            if (i == 8) {
                int callId = message.arg1;
                int mdefaultSubId = SubscriptionManager.getDefaultSubId();
                if (HwArbitrationCommonUtils.isSubIdValid(callId) && HwArbitrationCommonUtils.isSubIdValid(mdefaultSubId) && callId != mdefaultSubId && (!HwArbitrationFunction.isDsDs3() || HwArbitrationCommonUtils.DEL_DEFAULT_LINK)) {
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "Vice SIM  is starting Calling and not DsDs3.X", new Object[0]);
                    HwArbitrationStateMachine.this.sendMessage(HwArbitrationDEFS.MSG_MPLINK_ERROR);
                }
            } else if (i != 1005) {
                if (i != 1009) {
                    if (i == 1012) {
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "MSG_SCREEN_IS_TURNOFF received in MplinkStarted state", new Object[0]);
                        if (HwArbitrationFunction.isPvpScene()) {
                            HwArbitrationStateMachine.this.mHwWifiBoost.setPMMode(3);
                            HwArbitrationStateMachine.this.mHwWifiBoost.setGameBoosting(false);
                            HwArbitrationStateMachine.this.mHwWifiBoost.setPMMode(7);
                            HwArbitrationStateMachine.this.mHwWifiBoost.limitedSpeed(1, 0, 0);
                        }
                        if (!(HwArbitrationStateMachine.mHwArbitrationAppBoostMap == null || HwArbitrationStateMachine.mHwArbitrationAppBoostMap.size() == 0)) {
                            for (HwArbitrationAppBoostInfo boostInfo2 : HwArbitrationStateMachine.mHwArbitrationAppBoostMap.values()) {
                                if (boostInfo2 != null && boostInfo2.getIsMPlink()) {
                                    if (100105 != boostInfo2.getSceneId()) {
                                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "stop MpLinkBind, uid:" + boostInfo2.getBoostUID(), new Object[0]);
                                        HwArbitrationStateMachine.this.stopMpLinkAppBind(boostInfo2.getBoostUID());
                                    } else {
                                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "HiStream Audio : not stop MpLinkBind, uid:%{public}d", Integer.valueOf(boostInfo2.getBoostUID()));
                                    }
                                }
                            }
                        }
                    } else if (i == 1017) {
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "MSG_SCREEN_IS_ON received in MplinkStarted state", new Object[0]);
                        if (HwArbitrationFunction.isPvpScene()) {
                            HwArbitrationStateMachine.this.mHwWifiBoost.setPMMode(6);
                            HwArbitrationStateMachine.this.mHwWifiBoost.setPMMode(4);
                            HwArbitrationStateMachine.this.mHwWifiBoost.setGameBoosting(true);
                            HwArbitrationStateMachine.this.mHwWifiBoost.limitedSpeed(1, 1, 1);
                        }
                    } else if (i == 1024) {
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "MSG_DATA_SUBID_CHANGE", new Object[0]);
                        if (message.arg1 != SubscriptionManager.getDefaultSubId()) {
                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "DataSubId not equal to DefaultId", new Object[0]);
                            HwArbitrationStateMachine.this.sendMessage(HwArbitrationDEFS.MSG_MPLINK_ERROR);
                        }
                    } else if (i == 1106 || i == 1108) {
                        this.appInfo = (HwAPPStateInfo) message.obj;
                        HwAPPStateInfo hwAPPStateInfo = this.appInfo;
                        if (hwAPPStateInfo != null) {
                            if (HwArbitrationStateMachine.this.isInMPLink(hwAPPStateInfo.mAppUID)) {
                                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "isStreamScene:%{public}s", String.valueOf(HwArbitrationStateMachine.this.isStreamScene(this.appInfo.mAppUID)));
                                handleAppExpBad(this.appInfo, 3);
                            } else if (1106 != message.what || HwArbitrationFunction.isInLTE(HwArbitrationStateMachine.this.mContext)) {
                                handleAppExpBad(this.appInfo, 3);
                            } else {
                                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "Phone is not in LTE", new Object[0]);
                                HwArbitrationStateMachine.this.mHwArbitrationChrImpl.updateRequestMplinkChr((HwAPPStateInfo) message.obj, 1);
                            }
                        }
                    } else if (i == 2013) {
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "MSG_QUERY_QOE_WM_INFO", new Object[0]);
                        this.uid = message.arg1;
                        if (SystemClock.elapsedRealtime() - HwArbitrationStateMachine.this.wifiToCellTimestamp < HwArbitrationDEFS.DelayTimeMillisA && HwArbitrationStateMachine.this.isInMPLink(this.uid)) {
                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "trigger cell detect imprecise ping pong", new Object[0]);
                            HwArbitrationStateMachine.this.punishCellDetectImprecise = true;
                            HwArbitrationStateMachine.this.sendMessageDelayed(HwArbitrationDEFS.MSG_SET_PINGPONG_CELL_BAD_FALSE, 300000);
                        }
                        if (!HwArbitrationStateMachine.this.isInMPLink(this.uid) || !this.punishWifiDetectImprecise) {
                            HwArbitrationStateMachine.this.startMpLinkBindCheck(this.uid);
                        } else {
                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "MSG_QUERY_QOE_WM_INFO, but the pingpong flag of wifi active detected", new Object[0]);
                        }
                    } else if (i != 2019) {
                        if (i == 100) {
                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "MSG_GAME_STATE_START", new Object[0]);
                            HwArbitrationStateMachine.this.printMap(this.uid);
                            this.appInfo = (HwAPPStateInfo) message.obj;
                            HwArbitrationStateMachine.this.handleGeneralGameStart(this.appInfo);
                        } else if (i != 101) {
                            switch (i) {
                                case 103:
                                    HwArbitrationStateMachine.this.printMap(this.uid);
                                    this.appInfo = (HwAPPStateInfo) message.obj;
                                    HwAPPStateInfo hwAPPStateInfo2 = this.appInfo;
                                    if (hwAPPStateInfo2 != null) {
                                        HwArbitrationStateMachine.this.handleGamePvpEnd(hwAPPStateInfo2);
                                        HwArbitrationStateMachine.this.handleGeneralGameEnd(this.appInfo);
                                        break;
                                    }
                                    break;
                                case 104:
                                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "In MPLinkStartedState, MSG_GAME_ENTER_PVP_BATTLE received", new Object[0]);
                                    this.appInfo = (HwAPPStateInfo) message.obj;
                                    HwAPPStateInfo hwAPPStateInfo3 = this.appInfo;
                                    if (hwAPPStateInfo3 != null) {
                                        HwArbitrationStateMachine.this.handleGeneralGameStart(hwAPPStateInfo3);
                                        HwArbitrationStateMachine.this.handleGamePvpStart(this.appInfo);
                                        updateMapAppInfo(this.appInfo, false);
                                        HwArbitrationStateMachine.this.printMap(this.appInfo.mAppUID);
                                        break;
                                    }
                                    break;
                                case 105:
                                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "MSG_GAME_EXIT_PVP_BATTLE", new Object[0]);
                                    this.appInfo = (HwAPPStateInfo) message.obj;
                                    HwAPPStateInfo hwAPPStateInfo4 = this.appInfo;
                                    if (hwAPPStateInfo4 != null) {
                                        HwArbitrationStateMachine.this.handleGamePvpEnd(hwAPPStateInfo4);
                                        break;
                                    }
                                    break;
                                case 106:
                                    this.appInfo = (HwAPPStateInfo) message.obj;
                                    HwArbitrationStateMachine.this.handleStreamingStart(this.appInfo);
                                    break;
                                case 107:
                                    this.appInfo = (HwAPPStateInfo) message.obj;
                                    HwArbitrationStateMachine.this.handleStreamingEnd(this.appInfo);
                                    if (!HwArbitrationStateMachine.this.isInMPLink(this.appInfo.mAppUID) || !HwArbitrationStateMachine.this.isStreamScene(this.appInfo.mAppUID)) {
                                        HwArbitrationStateMachine.this.stopMpLinkCoex(this.appInfo.mAppUID);
                                        break;
                                    } else {
                                        this.stopMplinkReason = 6;
                                        HwArbitrationStateMachine.this.stopMpLinkAppBind(this.appInfo.mAppUID);
                                        break;
                                    }
                                default:
                                    switch (i) {
                                        case 109:
                                        case 111:
                                        case 113:
                                            this.appInfo = (HwAPPStateInfo) message.obj;
                                            HwAPPStateInfo hwAPPStateInfo5 = this.appInfo;
                                            if (hwAPPStateInfo5 != null && (!HwArbitrationStateMachine.this.isInMPLink(hwAPPStateInfo5.mAppUID) || !HwArbitrationStateMachine.this.isStreamScene(this.appInfo.mAppUID))) {
                                                updateMapAppInfo(this.appInfo, true);
                                                break;
                                            }
                                        case 110:
                                            this.appInfo = (HwAPPStateInfo) message.obj;
                                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "MSG_INSTANT_APP_END", new Object[0]);
                                            HwAPPStateInfo hwAPPStateInfo6 = this.appInfo;
                                            if (hwAPPStateInfo6 != null) {
                                                if (!HwArbitrationStateMachine.this.isInMPLink(hwAPPStateInfo6.mAppUID) || HwArbitrationStateMachine.this.isStreamScene(this.appInfo.mAppUID)) {
                                                    if (!HwArbitrationStateMachine.this.isInMPLink(this.appInfo.mAppUID)) {
                                                        HwArbitrationStateMachine.this.stopMpLinkCoex(this.appInfo.mAppUID);
                                                        break;
                                                    }
                                                } else {
                                                    this.stopMplinkReason = 6;
                                                    HwArbitrationStateMachine.this.stopMpLinkAppBind(this.appInfo.mAppUID);
                                                    break;
                                                }
                                            }
                                            break;
                                        case 112:
                                        case HwArbitrationDEFS.MSG_INSTANT_TRAVEL_APP_END /* 114 */:
                                            this.appInfo = (HwAPPStateInfo) message.obj;
                                            HwAPPStateInfo hwAPPStateInfo7 = this.appInfo;
                                            if (hwAPPStateInfo7 != null && !HwArbitrationStateMachine.this.isInMPLink(hwAPPStateInfo7.mAppUID)) {
                                                HwArbitrationStateMachine.this.stopMpLinkCoex(this.appInfo.mAppUID);
                                                break;
                                            }
                                        case 115:
                                            this.appInfo = (HwAPPStateInfo) message.obj;
                                            handleAppExpBad(this.appInfo, 2);
                                            break;
                                        default:
                                            switch (i) {
                                                case HwArbitrationDEFS.MSG_MPLINK_BIND_CHECK_OK_NOTIFY /* 2005 */:
                                                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "MSG_MPLINK_BIND_CHECK_OK_NOTIFY", new Object[0]);
                                                    HwArbitrationStateMachine.this.removeMessages(HwArbitrationDEFS.MSG_QUERY_QOE_WM_TIMEOUT);
                                                    this.appInfo = HwArbitrationCallbackImpl.getInstance(HwArbitrationStateMachine.this.mContext).getGameAppStateInfo();
                                                    this.uid = message.arg1;
                                                    if (!HwArbitrationStateMachine.this.isInMPLink(this.uid) || this.punishWifiDetectImprecise) {
                                                        if (!HwArbitrationStateMachine.this.isInMPLink(this.uid) && !HwArbitrationStateMachine.this.punishCellDetectImprecise) {
                                                            HwArbitrationStateMachine.this.startMpLinkAppBind(this.uid);
                                                            break;
                                                        } else {
                                                            HwArbitrationStateMachine.this.isMpLinkBinding = false;
                                                            break;
                                                        }
                                                    } else {
                                                        this.stopMplinkReason = 3;
                                                        HwArbitrationStateMachine.this.stopMpLinkAppBind(this.uid);
                                                        break;
                                                    }
                                                    break;
                                                case HwArbitrationDEFS.MSG_MPLINK_BIND_CHECK_FAIL_NOTIFY /* 2006 */:
                                                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "MSG_MPLINK_BIND_CHECK_FAIL_NOTIFY", new Object[0]);
                                                    HwArbitrationStateMachine.this.removeMessages(HwArbitrationDEFS.MSG_QUERY_QOE_WM_TIMEOUT);
                                                    this.appInfo = HwArbitrationCallbackImpl.getInstance(HwArbitrationStateMachine.this.mContext).getGameAppStateInfo();
                                                    this.uid = message.arg1;
                                                    HwArbitrationStateMachine.this.setQueryTime(this.uid);
                                                    if (HwArbitrationStateMachine.this.noAPPInMPLink() && !isInGameScene(this.uid)) {
                                                        HwArbitrationStateMachine.this.stopMpLinkCoex(this.uid);
                                                    }
                                                    HwArbitrationStateMachine.this.isMpLinkBinding = false;
                                                    HwArbitrationStateMachine.this.updateMplinkCHRExceptionEvent(message.arg1, 8, message.arg2);
                                                    break;
                                                case HwArbitrationDEFS.MSG_MPLINK_BIND_SUCCESS /* 2007 */:
                                                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "MSG_MPLINK_BIND_SUCCESS", new Object[0]);
                                                    HwArbitrationStateMachine.this.isMpLinkBinding = false;
                                                    break;
                                                case HwArbitrationDEFS.MSG_MPLINK_BIND_FAIL /* 2008 */:
                                                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "MSG_MPLINK_BIND_FAIL", new Object[0]);
                                                    HwArbitrationStateMachine.this.isMpLinkBinding = false;
                                                    break;
                                                case HwArbitrationDEFS.MSG_MPLINK_UNBIND_SUCCESS /* 2009 */:
                                                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "MSG_MPLINK_UNBIND_SUCCESS", new Object[0]);
                                                    this.wifiGoodFlag = true;
                                                    HwArbitrationStateMachine.this.isMpLinkBinding = false;
                                                    break;
                                                case HwArbitrationDEFS.MSG_MPLINK_UNBIND_FAIL /* 2010 */:
                                                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "MSG_MPLINK_UNBIND_FAIL", new Object[0]);
                                                    this.wifiGoodFlag = true;
                                                    break;
                                                default:
                                                    switch (i) {
                                                        case HwArbitrationDEFS.MSG_QUERY_QOE_WM_TIMEOUT /* 2029 */:
                                                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "check ChannelQoE and Wavemapping timeout", new Object[0]);
                                                            if (HwArbitrationStateMachine.this.noAPPInMPLink()) {
                                                                HwArbitrationStateMachine.this.stopMpLinkCoex(message.arg1);
                                                            }
                                                            HwArbitrationStateMachine.this.updateMplinkCHRExceptionEvent(message.arg1, 8, 7);
                                                            break;
                                                        case HwArbitrationDEFS.MSG_APPQoE_WIFI_GOOD /* 2030 */:
                                                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "MSG_APPQoE_WIFI_GOOD", new Object[0]);
                                                            if (!HwArbitrationStateMachine.this.noAPPInMPLink()) {
                                                                if (this.wifiGoodFlag) {
                                                                    this.uid = message.arg1;
                                                                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "trgPingPongWiFi_Good: %{public}s", String.valueOf(this.trgPingPongWiFi_Good));
                                                                    if (HwArbitrationStateMachine.mHwArbitrationAppBoostMap != null && HwArbitrationStateMachine.mHwArbitrationAppBoostMap.get(Integer.valueOf(this.uid)) != null && HwArbitrationStateMachine.this.isInMPLink(this.uid) && !this.trgPingPongWiFi_Good) {
                                                                        this.wifiGoodFlag = false;
                                                                        HwArbitrationStateMachine.this.printMap(this.uid);
                                                                        this.stopMplinkReason = 4;
                                                                        HwArbitrationAppBoostInfo myAABInfo = (HwArbitrationAppBoostInfo) HwArbitrationStateMachine.mHwArbitrationAppBoostMap.get(Integer.valueOf(this.uid));
                                                                        if (!(myAABInfo == null || HwWaveMappingManager.getInstance(HwArbitrationStateMachine.this.mContext) == null || HwAPPQoEManager.getInstance() == null)) {
                                                                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "queryWaveMappingInfo4Back: %{public}d", Integer.valueOf(this.uid));
                                                                            HwWaveMappingManager.getInstance(HwArbitrationStateMachine.this.mContext).queryWaveMappingInfo4Back(this.uid, myAABInfo.mAppID, myAABInfo.mSceneId, HwArbitrationStateMachine.this.getTargetNetwork(myAABInfo.mNetwork));
                                                                            break;
                                                                        }
                                                                    } else {
                                                                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "MSG_APPQoE_WIFI_GOOD not allow: %{public}s", String.valueOf(this.trgPingPongWiFi_Good));
                                                                        break;
                                                                    }
                                                                } else {
                                                                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "processing MSG_APPQoE_WIFI_GOOD", new Object[0]);
                                                                    break;
                                                                }
                                                            } else {
                                                                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "there's no app in MPLink", new Object[0]);
                                                                break;
                                                            }
                                                            break;
                                                        case HwArbitrationDEFS.MSG_SET_PINGPONG_WIFI_GOOD_FALSE /* 2031 */:
                                                            if (this.trgPingPongWiFi_Good) {
                                                                HwArbitrationCommonUtils.logE(HwArbitrationStateMachine.TAG, false, "MSG_SET_PINGPONG_WIFI_GOOD_FALSE", new Object[0]);
                                                                this.trgPingPongWiFi_Good = false;
                                                                this.uid = message.arg1;
                                                            }
                                                            if (this.punishWifiDetectImprecise) {
                                                                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "MSG_SET_PINGPONG_WIFI_GOOD_FALSE, set punishWifiDetectImprecise to false", new Object[0]);
                                                                this.punishWifiDetectImprecise = false;
                                                                if (HwArbitrationCommonUtils.IS_HIDATA2_ENABLED) {
                                                                    HwArbitrationStateMachine.this.needTputTest = false;
                                                                    break;
                                                                }
                                                            }
                                                            break;
                                                        case HwArbitrationDEFS.MSG_Stop_MPLink_By_Notification /* 2032 */:
                                                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "MSG_Stop_MPLink_By_Notification", new Object[0]);
                                                            HwArbitrationStateMachine.this.mDenyByNotification = true;
                                                            HwArbitrationStateMachine.this.sendMessage(HwArbitrationDEFS.MSG_MPLINK_ERROR);
                                                            this.mplinkErrorCode = 0;
                                                            HwArbitrationStateMachine.this.sendMessageDelayed(HwArbitrationDEFS.MSG_Recovery_Flag_By_Notification, 86400000);
                                                            break;
                                                        default:
                                                            switch (i) {
                                                                case HwArbitrationDEFS.MSG_SET_PINGPONG_CELL_BAD_FALSE /* 2035 */:
                                                                    if (HwArbitrationStateMachine.this.punishCellDetectImprecise) {
                                                                        HwArbitrationCommonUtils.logE(HwArbitrationStateMachine.TAG, false, "MSG_SET_PINGPONG_CELL_BAD_FALSE in MPLinkStartedState", new Object[0]);
                                                                        HwArbitrationStateMachine.this.resetPingPongFlag();
                                                                        break;
                                                                    }
                                                                    break;
                                                                case HwArbitrationDEFS.MSG_WM_OUT_FOR_STOP_MPLINK /* 2036 */:
                                                                    this.uid = message.arg1;
                                                                    if (1 != message.arg2 || !HwArbitrationStateMachine.this.isInMPLink(this.uid)) {
                                                                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "MSG_WM_OUT_FOR_STOP_MPLINK, re-start WiFi monitor %{public}d", Integer.valueOf(this.uid));
                                                                    } else {
                                                                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "MSG_WM_OUT_FOR_STOP_MPLINK, stopMpLinkAppBind %{public}d", Integer.valueOf(this.uid));
                                                                        this.pingPongTMWiFi_Good = SystemClock.elapsedRealtime();
                                                                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "MSG_WM_OUT_FOR_STOP_MPLINK time: %{public}s", String.valueOf(this.pingPongTMWiFi_Good));
                                                                        if (!isInGameScene(this.uid)) {
                                                                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "Not in game scene, switch back to Wifi", new Object[0]);
                                                                            HwArbitrationStateMachine.this.stopMpLinkAppBind(this.uid);
                                                                        }
                                                                    }
                                                                    this.appInfo = HwArbitrationCallbackImpl.getInstance(HwArbitrationStateMachine.this.mContext).getGameAppStateInfo();
                                                                    if (!HwArbitrationStateMachine.this.isBoostInfoAvailable(this.appInfo)) {
                                                                        HwArbitrationCommonUtils.logE(HwArbitrationStateMachine.TAG, false, "boostInfo is not available", new Object[0]);
                                                                        break;
                                                                    } else {
                                                                        this.boostInfo = (HwArbitrationAppBoostInfo) HwArbitrationStateMachine.mHwArbitrationAppBoostMap.get(Integer.valueOf(this.uid));
                                                                        HwArbitrationAppBoostInfo hwArbitrationAppBoostInfo = this.boostInfo;
                                                                        if (hwArbitrationAppBoostInfo != null && hwArbitrationAppBoostInfo.getHwAPPStateInfo() != null) {
                                                                            if (this.boostInfo.getHwAPPStateInfo().getIsMplinkEnteredFromCell() == 1) {
                                                                                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "In game scene, and entered from cell monitor state, switch back to Wifi", new Object[0]);
                                                                                this.boostInfo.getHwAPPStateInfo().setIsMplinkEnteredFromCell(-1);
                                                                                HwArbitrationStateMachine.this.stopMpLinkAppBind(this.uid, 5);
                                                                                break;
                                                                            }
                                                                        } else {
                                                                            HwArbitrationCommonUtils.logE(HwArbitrationStateMachine.TAG, false, "boostInfo is null or no app state info, just break", new Object[0]);
                                                                            break;
                                                                        }
                                                                    }
                                                                    break;
                                                                case HwArbitrationDEFS.MSG_WECHAT_CALL_CHECK_WIFI_RX_BYTES /* 2037 */:
                                                                    int streamUid = message.arg1;
                                                                    long diffWifiBytes = HwArbitrationCommonUtils.getUidWiFiBytes(HwArbitrationStateMachine.this.mContext, streamUid) - this.mStartWifiBytes;
                                                                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "diffWifiRxBytes %{public}s", String.valueOf(diffWifiBytes));
                                                                    if (diffWifiBytes <= HwArbitrationDEFS.WIFI_RX_BYTES_THRESHOLD || !HwArbitrationStateMachine.this.isInMPLink(streamUid)) {
                                                                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "diffWifiRxBytes is less than threshold", new Object[0]);
                                                                        break;
                                                                    } else {
                                                                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "Wechat Stream Receive Bytes in Wifi, Stop Mplink", new Object[0]);
                                                                        HwArbitrationStateMachine.this.mDenyByRxBytesInWifi = true;
                                                                        HwArbitrationStateMachine.this.sendMessageDelayed(HwArbitrationDEFS.MSG_RECOVERY_FLAG_BY_WIFI_RX_BYTES, 86400000);
                                                                        this.stopMplinkReason = 7;
                                                                        HwArbitrationStateMachine.this.stopMpLinkAppBind(streamUid);
                                                                        break;
                                                                    }
                                                                    break;
                                                                default:
                                                                    return false;
                                                            }
                                                    }
                                            }
                                    }
                                    break;
                            }
                        } else {
                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "MSG_GAME_STATE_END", new Object[0]);
                            this.appInfo = (HwAPPStateInfo) message.obj;
                            HwAPPStateInfo hwAPPStateInfo8 = this.appInfo;
                            if (hwAPPStateInfo8 != null) {
                                HwArbitrationStateMachine.this.printMap(hwAPPStateInfo8.mAppUID);
                                HwArbitrationStateMachine.this.handleGeneralGameEnd(this.appInfo);
                                HwArbitrationStateMachine.this.handleGamePvpEnd(this.appInfo);
                            }
                        }
                    }
                }
                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "MSG_MPLINK_ERROR", new Object[0]);
                HwArbitrationStateMachine.this.isMpLinkBinding = true;
                HwArbitrationStateMachine.this.isErrorMsgReceived = true;
                HwArbitrationStateMachine.this.mCoexCount = 0;
                List<Integer> uidList = HwArbitrationStateMachine.this.getAppUidInMpLink();
                if (uidList == null || uidList.size() == 0) {
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "MSG_MPLINK_ERROR: no app in MPLink", new Object[0]);
                    HwArbitrationStateMachine hwArbitrationStateMachine = HwArbitrationStateMachine.this;
                    hwArbitrationStateMachine.transitionTo(hwArbitrationStateMachine.mMPLinkStoppingState);
                } else {
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "MSG_MPLINK_ERROR: app in MPLink", new Object[0]);
                    if (this.mplinkErrorCode == 0) {
                        this.stopMplinkReason = 9;
                        this.mplinkErrorCode = -1;
                    } else {
                        this.stopMplinkReason = 7;
                    }
                    int NList = uidList.size();
                    for (int i2 = 0; i2 < NList; i2++) {
                        HwArbitrationStateMachine.this.stopMpLinkAppBind(uidList.get(i2).intValue());
                    }
                }
            }
            return true;
        }

        private void handleAppExpBad(HwAPPStateInfo appInfo2, int type) {
            if (appInfo2 != null) {
                updateMapAppInfo(appInfo2, true);
                HwArbitrationStateMachine.this.printMap(appInfo2.mAppUID);
                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "%{public}s", appInfo2.toString());
                if (HwArbitrationStateMachine.this.isMpLinkBinding) {
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "MPLinkStarted is querying", new Object[0]);
                    HwArbitrationStateMachine.this.mHwArbitrationChrImpl.updateRequestMplinkChr(appInfo2, 6);
                } else if (type == 2) {
                    HwArbitrationStateMachine.this.sendMessage(HwArbitrationDEFS.MSG_QUERY_QOE_WM_INFO, appInfo2.mAppUID);
                } else if (type == 3 && HwArbitrationFunction.isStreamingScene(appInfo2)) {
                    HwArbitrationStateMachine.this.sendMessage(HwArbitrationDEFS.MSG_QUERY_QOE_WM_INFO, appInfo2.mAppUID);
                }
            }
        }

        private void updateMapAppInfo(HwAPPStateInfo appInfo2, boolean needCoexAdd) {
            if (appInfo2 != null) {
                if (HwArbitrationStateMachine.mHwArbitrationAppBoostMap != null && HwArbitrationStateMachine.mHwArbitrationAppBoostMap.get(Integer.valueOf(appInfo2.mAppUID)) != null) {
                    HwArbitrationAppBoostInfo mHwAAInfo = (HwArbitrationAppBoostInfo) HwArbitrationStateMachine.mHwArbitrationAppBoostMap.get(Integer.valueOf(appInfo2.mAppUID));
                    if (!mHwAAInfo.getIsCoex() && needCoexAdd) {
                        mHwAAInfo.setIsCoex(true);
                        HwArbitrationStateMachine.this.mCoexCount++;
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "updateMapAppInfo, mCoexCount is: %{public}d", Integer.valueOf(HwArbitrationStateMachine.this.mCoexCount));
                    }
                    HwArbitrationStateMachine.this.setStateMachineHashMap(appInfo2, mHwAAInfo.getNetwork(), mHwAAInfo.getIsCoex(), mHwAAInfo.getIsMPlink(), mHwAAInfo.getSolution());
                } else if (HwArbitrationStateMachine.mHwArbitrationAppBoostMap == null || HwArbitrationStateMachine.mHwArbitrationAppBoostMap.get(Integer.valueOf(appInfo2.mAppUID)) == null) {
                    HwArbitrationStateMachine hwArbitrationStateMachine = HwArbitrationStateMachine.this;
                    hwArbitrationStateMachine.setStateMachineHashMap(appInfo2, hwArbitrationStateMachine.getCurrentNetwork(hwArbitrationStateMachine.mContext, appInfo2.mAppUID), true, false, 0);
                    HwArbitrationStateMachine.this.mCoexCount++;
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "updateMapAppInfo, mCoexCount is: %{public}d", Integer.valueOf(HwArbitrationStateMachine.this.mCoexCount));
                }
            }
        }

        private boolean isInGameScene(int uid2) {
            boolean isGameScene = false;
            if (!(HwArbitrationStateMachine.mHwArbitrationAppBoostMap == null || HwArbitrationStateMachine.mHwArbitrationAppBoostMap.get(Integer.valueOf(uid2)) == null || ((HwArbitrationAppBoostInfo) HwArbitrationStateMachine.mHwArbitrationAppBoostMap.get(Integer.valueOf(uid2))).getHwAPPStateInfo() == null || ((HwArbitrationAppBoostInfo) HwArbitrationStateMachine.mHwArbitrationAppBoostMap.get(Integer.valueOf(uid2))).getHwAPPStateInfo().mAppType != 2000)) {
                isGameScene = true;
            }
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "isInGameScene :%{public}s", String.valueOf(isGameScene));
            return isGameScene;
        }

        private void judgeWhetherDetectChannelQuality(int uid2, int counter, boolean isInWifi) {
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "handle game experience bad, counter is: %{public}d", Integer.valueOf(counter));
            if (counter <= 2 || ((counter == 3 && SystemClock.elapsedRealtime() - this.channelDetectionTimeStamp >= 10) || ((counter == 4 && SystemClock.elapsedRealtime() - this.channelDetectionTimeStamp >= 20) || (counter > 4 && SystemClock.elapsedRealtime() - this.channelDetectionTimeStamp >= 30)))) {
                HwArbitrationStateMachine.this.sendMessage(HwArbitrationDEFS.MSG_QUERY_QOE_WM_INFO, uid2);
                updateChannelCheckedCount(isInWifi);
                this.channelDetectionTimeStamp = SystemClock.elapsedRealtime();
                return;
            }
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "Condition not satisfied, don't detect wifi's quality.", new Object[0]);
        }

        private void updateChannelCheckedCount(boolean isInWifi) {
            if (isInWifi) {
                this.cellDetectedCount++;
            } else {
                this.wifiDetectedCount++;
            }
        }

        private void resetGameState(int uid2) {
            if (HwArbitrationStateMachine.mHwArbitrationAppBoostMap != null && HwArbitrationStateMachine.mHwArbitrationAppBoostMap.get(Integer.valueOf(uid2)) != null && ((HwArbitrationAppBoostInfo) HwArbitrationStateMachine.mHwArbitrationAppBoostMap.get(Integer.valueOf(uid2))).getHwAPPStateInfo() != null) {
                ((HwArbitrationAppBoostInfo) HwArbitrationStateMachine.mHwArbitrationAppBoostMap.get(Integer.valueOf(uid2))).getHwAPPStateInfo().setExperience(HwAPPQoEUtils.MSG_APP_STATE_UNKNOW);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleGeneralGameStart(HwAPPStateInfo appInfo) {
        if (appInfo != null && this.mContext.getPackageManager() != null) {
            String appName = this.mContext.getPackageManager().getNameForUid(appInfo.mAppUID);
            if (getCurrentNetwork(this.mContext, appInfo.mAppUID) == 800 && appName != null && this.mAppTypeRecoManager.getAppType(appName) == 9) {
                this.mHwWifiBoost.setPMMode(6);
                this.mHwWifiBoost.pauseABSHandover();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleGeneralGameEnd(HwAPPStateInfo appInfo) {
        if (appInfo != null && 800 == getCurrentNetwork(this.mContext, appInfo.mAppUID)) {
            this.mHwWifiBoost.setPMMode(7);
            this.mHwWifiBoost.restartABSHandover();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleGamePvpStart(HwAPPStateInfo appInfo) {
        if (appInfo != null && 800 == getCurrentNetwork(this.mContext, appInfo.mAppUID)) {
            this.mHwWifiBoost.startGameBoost(appInfo.mAppUID);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleGamePvpEnd(HwAPPStateInfo appInfo) {
        if (appInfo != null && 800 == getCurrentNetwork(this.mContext, appInfo.mAppUID)) {
            this.mHwWifiBoost.stopGameBoost(appInfo.mAppUID);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleStreamingStart(HwAPPStateInfo appInfo) {
        if (appInfo != null && getCurrentNetwork(this.mContext, appInfo.mAppUID) == 800 && isVoipStreamApp(appInfo.mScenceId)) {
            this.mHwWifiBoost.startStreamingBoost(appInfo.mAppUID);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleStreamingEnd(HwAPPStateInfo appInfo) {
        if (appInfo != null && getCurrentNetwork(this.mContext, appInfo.mAppUID) == 800 && isVoipStreamApp(appInfo.mScenceId)) {
            this.mHwWifiBoost.stopStreamingBoost(appInfo.mAppUID);
        }
    }

    private boolean isVoipStreamApp(int sceneceId) {
        if (sceneceId == 100106 || sceneceId == 100105 || sceneceId == 101101) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateNetworkTypeForArbitrationMap(int networkType) {
        ConcurrentHashMap<Integer, HwArbitrationAppBoostInfo> concurrentHashMap = mHwArbitrationAppBoostMap;
        if (!(concurrentHashMap == null || concurrentHashMap.isEmpty())) {
            for (HwArbitrationAppBoostInfo boostInfo : mHwArbitrationAppBoostMap.values()) {
                if (boostInfo != null) {
                    boostInfo.setNetwork(networkType);
                }
            }
        }
    }

    public boolean isInMPLink(int uid) {
        HwArbitrationAppBoostInfo appBoostInfo;
        ConcurrentHashMap<Integer, HwArbitrationAppBoostInfo> concurrentHashMap = mHwArbitrationAppBoostMap;
        if (concurrentHashMap == null || (appBoostInfo = concurrentHashMap.get(Integer.valueOf(uid))) == null) {
            return false;
        }
        return appBoostInfo.getIsMPlink();
    }

    private boolean isAppExit(int uid) {
        HwAPPStateInfo appInfo;
        boolean isAppEnd = false;
        ConcurrentHashMap<Integer, HwArbitrationAppBoostInfo> concurrentHashMap = mHwArbitrationAppBoostMap;
        if (!(concurrentHashMap == null || concurrentHashMap.get(Integer.valueOf(uid)) == null || (appInfo = mHwArbitrationAppBoostMap.get(Integer.valueOf(uid)).getHwAPPStateInfo()) == null || (appInfo.mAppState != 101 && appInfo.mAppState != 104))) {
            isAppEnd = true;
        }
        HwArbitrationCommonUtils.logD(TAG, false, "app is exit: " + isAppEnd, new Object[0]);
        return isAppEnd;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isStreamScene(int uid) {
        ConcurrentHashMap<Integer, HwArbitrationAppBoostInfo> concurrentHashMap = mHwArbitrationAppBoostMap;
        if (concurrentHashMap == null || concurrentHashMap.get(Integer.valueOf(uid)) == null) {
            return false;
        }
        int scene = mHwArbitrationAppBoostMap.get(Integer.valueOf(uid)).getSceneId();
        if (scene == 100501 || scene == 100901 || scene == 100106 || scene == 100105 || scene == 100701) {
            return true;
        }
        return false;
    }

    private boolean isWechatStreamScene(int uid) {
        ConcurrentHashMap<Integer, HwArbitrationAppBoostInfo> concurrentHashMap = mHwArbitrationAppBoostMap;
        if (concurrentHashMap == null || concurrentHashMap.get(Integer.valueOf(uid)) == null) {
            return false;
        }
        int scene = mHwArbitrationAppBoostMap.get(Integer.valueOf(uid)).getSceneId();
        if (scene == 100106 || scene == 100105) {
            return true;
        }
        return false;
    }

    public void sendMPLinkBroadcast(int uid) {
        ConcurrentHashMap<Integer, HwArbitrationAppBoostInfo> concurrentHashMap = mHwArbitrationAppBoostMap;
        if (concurrentHashMap != null && concurrentHashMap.get(Integer.valueOf(uid)) != null) {
            Intent MPLinkIntent = new Intent("com.android.server.hidata.arbitration.HwArbitrationStateMachine");
            MPLinkIntent.putExtra("MPLinkSuccessUIDKey", uid);
            MPLinkIntent.putExtra("MPLinkSuccessNetworkKey", mHwArbitrationAppBoostMap.get(Integer.valueOf(uid)).getNetwork());
            this.mContext.sendBroadcastAsUser(MPLinkIntent, UserHandle.ALL, "com.huawei.hidata.permission.MPLINK_START_CHECK");
        }
    }

    public int getTargetNetID(int network) {
        if (network == 800) {
            return HwArbitrationFunction.getNetworkID(this.mContext, 801);
        }
        if (network == 801) {
            return HwArbitrationFunction.getNetworkID(this.mContext, 800);
        }
        return -1;
    }

    public int getCurrentNetwork(Context mContext2, int uid) {
        HwArbitrationAppBoostInfo boostInfo;
        ConcurrentHashMap<Integer, HwArbitrationAppBoostInfo> concurrentHashMap = mHwArbitrationAppBoostMap;
        if (concurrentHashMap == null || (boostInfo = concurrentHashMap.get(Integer.valueOf(uid))) == null) {
            return HwArbitrationCommonUtils.getActiveConnectType(mContext2);
        }
        HwArbitrationCommonUtils.logD(TAG, false, "uid: %{public}d, CurrentNetwork: %{public}d", Integer.valueOf(uid), Integer.valueOf(boostInfo.getNetwork()));
        return boostInfo.getNetwork();
    }

    private boolean isNeedChQoEquery(int uid) {
        HashMap<Integer, Long> hashMap = this.mQueryTime;
        if (hashMap == null || !hashMap.containsKey(Integer.valueOf(uid))) {
            return true;
        }
        long queryTime = this.mQueryTime.get(Integer.valueOf(uid)).longValue();
        long nowTime = SystemClock.elapsedRealtime();
        if (nowTime - queryTime > QUERY_WAIT_TIME) {
            HwArbitrationCommonUtils.logD(TAG, false, "isNeedChQoEquery: allow ChannelQoE and WM query", new Object[0]);
            return true;
        }
        HwArbitrationCommonUtils.logD(TAG, false, "isNeedChQoEquery: not allow ChannelQoE and WM query, waiting %{public}s Milliseconds", String.valueOf((QUERY_WAIT_TIME + queryTime) - nowTime));
        return false;
    }

    public boolean deliverErrorMPLinkCase() {
        if (this.mIsMpLinkError) {
            HwArbitrationCommonUtils.logD(TAG, false, "denyCoexStatus is processing the MPLink Error", new Object[0]);
            return false;
        } else if (getCurrentState() == this.mMPLinkStartingState) {
            setFlagAtTimer(WifiProCommonDefs.QUERY_TIMEOUT_MS);
            HwArbitrationCommonUtils.logD(TAG, false, "deliverErrorMPLinkCase, goto InitialState", new Object[0]);
            transitionTo(this.mMPLinkStoppingState);
            return true;
        } else if (getCurrentState() == this.mMPLinkStartedState) {
            setFlagAtTimer(WifiProCommonDefs.QUERY_TIMEOUT_MS);
            HwArbitrationCommonUtils.logD(TAG, false, "deliverErrorMPLinkCase in MPLinkStartedState", new Object[0]);
            sendMessage(HwArbitrationDEFS.MSG_MPLINK_ERROR);
            return true;
        } else if (getCurrentState() != this.mMPLinkStoppingState) {
            return false;
        } else {
            setFlagAtTimer(WifiProCommonDefs.QUERY_TIMEOUT_MS);
            HwArbitrationCommonUtils.logD(TAG, false, "deliverErrorMPLinkCase in MPLinkStoppingState", new Object[0]);
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean noAPPInMPLink() {
        List<Integer> result = getAppUidInMpLink();
        return result == null || result.isEmpty();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private List<Integer> getAppUidInMpLink() {
        if (mHwArbitrationAppBoostMap == null) {
            return null;
        }
        List<Integer> result = new ArrayList<>();
        for (Map.Entry<Integer, HwArbitrationAppBoostInfo> entry : mHwArbitrationAppBoostMap.entrySet()) {
            HwArbitrationAppBoostInfo val = entry.getValue();
            if (val != null && val.getIsMPlink()) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopMpLinkBindForAllApps() {
        List<Integer> uidList = getAppUidInMpLink();
        if (!(uidList == null || uidList.isEmpty())) {
            HwArbitrationCommonUtils.logD(TAG, false, "stop mplink bind for all apps", new Object[0]);
            for (Integer uid : uidList) {
                stopMpLinkAppBind(uid.intValue());
            }
        }
    }

    private void setFlagAtTimer(int milliseconds) {
        this.mIsMpLinkError = true;
        if (milliseconds < 1) {
            milliseconds = WifiProCommonDefs.QUERY_TIMEOUT_MS;
        }
        HwArbitrationCommonUtils.logD(TAG, false, "setFlagAtTimer 30S", new Object[0]);
        removeMessages(2001);
        sendMessageDelayed(2001, (long) milliseconds);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showToast(String string) {
        if (this.mDeviceBootCommpleted) {
            Toast.makeText(this.mContext, string, 0).show();
        }
    }

    public void updateCurrentStreamAppState(HwAPPStateInfo appInfo, int state) {
        if (appInfo != null) {
            if (state == 100 || state == 103) {
                this.mCurrentStreamAppInfo = appInfo;
                this.hiStreamAppState = 0;
            } else if (state == 101) {
                this.mCurrentStreamAppInfo = null;
                this.hiStreamAppState = 2;
            } else if (state == 104) {
                this.hiStreamAppState = 1;
                if (appInfo.mScenceId == 100106) {
                    this.mCurrentStreamAppInfo = null;
                } else if (appInfo.mScenceId == 100105) {
                    this.mCurrentStreamAppInfo = appInfo;
                }
            }
            HwArbitrationCommonUtils.logD(TAG, false, "hiStreamAppState:%{public}d", Integer.valueOf(this.hiStreamAppState));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void updateMplinkCHRExceptionEvent(int appUid, int event, int reason) {
        HwArbitrationAppBoostInfo boostInfo;
        if (!(mHwArbitrationAppBoostMap == null || (boostInfo = mHwArbitrationAppBoostMap.get(Integer.valueOf(appUid))) == null)) {
            if (8 == event) {
                if (800 == boostInfo.getNetwork()) {
                    event = 5;
                } else if (801 == boostInfo.getNetwork()) {
                    event = 3;
                }
            }
            if (this.mHwArbitrationChrImpl != null) {
                this.mHwArbitrationChrImpl.updateMplinkActionChr(boostInfo.getHwAPPStateInfo(), event, reason);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setQueryTime(int uid) {
        if (this.mQueryTime == null) {
            this.mQueryTime = new HashMap<>(10);
        }
        this.mQueryTime.put(Integer.valueOf(uid), Long.valueOf(SystemClock.elapsedRealtime()));
    }

    public boolean isDenyByNotification() {
        return this.mDenyByNotification;
    }

    public boolean isPunishCellDetectImprecise() {
        return this.punishCellDetectImprecise;
    }
}
