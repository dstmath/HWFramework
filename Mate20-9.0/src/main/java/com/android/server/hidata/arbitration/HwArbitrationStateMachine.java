package com.android.server.hidata.arbitration;

import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.os.SystemClock;
import android.os.UserHandle;
import android.rms.iaware.AppTypeRecoManager;
import android.telephony.SubscriptionManager;
import android.widget.Toast;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.hidata.appqoe.HwAPPChrManager;
import com.android.server.hidata.appqoe.HwAPPQoEManager;
import com.android.server.hidata.appqoe.HwAPPQoEResourceManger;
import com.android.server.hidata.appqoe.HwAPPStateInfo;
import com.android.server.hidata.hiradio.HwHiRadioBoost;
import com.android.server.hidata.hiradio.HwWifiBoost;
import com.android.server.hidata.histream.HwHiStreamManager;
import com.android.server.hidata.mplink.HwMplinkManager;
import com.android.server.hidata.mplink.MpLinkQuickSwitchConfiguration;
import com.android.server.hidata.mplink.MplinkBindResultInfo;
import com.android.server.hidata.wavemapping.HwWaveMappingManager;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HwArbitrationStateMachine extends StateMachine {
    private static final long QueryWaitTime = 20000;
    private static final String TAG = "HiData_HwArbitrationStateMachine";
    private static HwArbitrationStateMachine mArbitrationStateMachine;
    /* access modifiers changed from: private */
    public static ConcurrentHashMap<Integer, HwArbitrationAppBoostInfo> mHwArbitrationAppBoostMap;
    /* access modifiers changed from: private */
    public int hiStreamAppState = -1;
    private AppTypeRecoManager mAppTypeRecoManager = null;
    /* access modifiers changed from: private */
    public State mCellMonitorState = new CellMonitorState();
    /* access modifiers changed from: private */
    public int mCoexCount = 0;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public int mCurrentActiveNetwork = 802;
    /* access modifiers changed from: private */
    public int mCurrentServiceState = 1;
    /* access modifiers changed from: private */
    public HwAPPStateInfo mCurrentStreamAppInfo;
    private State mDefaultState = new DefaultState();
    /* access modifiers changed from: private */
    public boolean mDenyByNotification = false;
    /* access modifiers changed from: private */
    public boolean mDeviceBootCommpleted = false;
    /* access modifiers changed from: private */
    public boolean mHiStreamTriggerOrStopMplink = true;
    HwAPPQoEManager mHwAPPQoEManager = null;
    /* access modifiers changed from: private */
    public HwAPPQoEResourceManger mHwAPPQoEResourceManger;
    HwArbitrationChrImpl mHwArbitrationChrImpl;
    /* access modifiers changed from: private */
    public HwHiRadioBoost mHwHiRadioBoost;
    HwHiStreamManager mHwHiStreamManager = null;
    /* access modifiers changed from: private */
    public HwWifiBoost mHwWifiBoost = null;
    /* access modifiers changed from: private */
    public State mInitialState = new InitialState();
    /* access modifiers changed from: private */
    public boolean mIsMpLinkBinding = false;
    /* access modifiers changed from: private */
    public boolean mIsMpLinkError = false;
    /* access modifiers changed from: private */
    public State mMPLinkStartedState = new MPLinkStartedState();
    /* access modifiers changed from: private */
    public State mMPLinkStartingState = new MPLinkStartingState();
    /* access modifiers changed from: private */
    public State mMPLinkStoppingState = new MPLinkStoppingState();
    /* access modifiers changed from: private */
    public int mMpLinkCount = 0;
    private HashMap<Integer, Long> mQueryTime;
    /* access modifiers changed from: private */
    public State mWifiMonitorState = new WifiMonitorState();
    /* access modifiers changed from: private */
    public long pingPongTMCell_Bad = 0;
    /* access modifiers changed from: private */
    public boolean trgPingPongCell_Bad = false;
    /* access modifiers changed from: private */
    public long triggerMPlinkInternal = 20000;

    class CellMonitorState extends State {
        private int actions = 0;
        private int appUID = -1;
        private HwAPPStateInfo mAppInfo = null;

        CellMonitorState() {
        }

        public void enter() {
            String str;
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "Enter CellMonitorState");
            HwArbitrationStateMachine.this.mHwAPPQoEManager = HwAPPQoEManager.getInstance();
            this.mAppInfo = HwArbitrationStateMachine.this.mHwAPPQoEManager == null ? null : HwArbitrationStateMachine.this.mHwAPPQoEManager.getCurAPPStateInfo();
            if (this.mAppInfo == null) {
                str = "mAppInfo is null ";
            } else {
                str = "mAppInfo:" + this.mAppInfo.toString();
            }
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, str);
            if (HwArbitrationFunction.isDataTechSuitable()) {
                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "begin HiRadio  Cellular boost");
                handleCurrentScene(this.mAppInfo);
                handleHiStreamScene();
            }
        }

        private void handleCurrentScene(HwAPPStateInfo appInfo) {
            if (appInfo != null && -1 != appInfo.mAppUID) {
                int i = appInfo.mAppType;
                if (i == 1000) {
                    HwArbitrationStateMachine.this.sendMessage(109, appInfo);
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "CurrentScene:MSG_INSTANT_APP_START");
                } else if (i != 2000) {
                    if (i != 4000) {
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "CurrentScene:no AppQoE monitored scenes at foreground");
                    } else {
                        HwArbitrationStateMachine.this.sendMessage(106, appInfo);
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "CurrentScene:MSG_STREAM_APP_START");
                    }
                } else if (appInfo.mScenceId == 200002) {
                    HwArbitrationStateMachine.this.sendMessage(104, appInfo);
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "CurrentScene:MSG_GAME_ENTER_PVP_BATTLE");
                } else if (appInfo.mScenceId == 200001) {
                    HwArbitrationStateMachine.this.sendMessage(100, appInfo);
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "CurrentScene:MSG_GAME_STATE_START");
                }
                HwArbitrationStateMachine.this.mHwHiRadioBoost.BrainAppStateNotifyDSBooster(appInfo, 0, 0);
            }
        }

        private void handleHiStreamScene() {
            if (HwArbitrationStateMachine.this.mCurrentStreamAppInfo != null) {
                int actions2 = getActionsConfig(HwArbitrationStateMachine.this.mCurrentStreamAppInfo);
                if (actions2 < 0) {
                    actions2 = 1;
                }
                int actions3 = findActionsNeedStart(HwArbitrationStateMachine.this.mCurrentStreamAppInfo, actions2);
                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "Action to start:" + actions3);
                if (actions3 > 0) {
                    HwArbitrationStateMachine.this.mHwHiRadioBoost.startOptimizeActionsForApp(HwArbitrationStateMachine.this.mCurrentStreamAppInfo, actions3);
                    HwArbitrationStateMachine.this.setStateMachineHashMap(HwArbitrationStateMachine.this.mCurrentStreamAppInfo, 801, false, false, actions3);
                }
            }
            HwArbitrationStateMachine.this.mHwHiStreamManager = HwHiStreamManager.getInstance();
            HwAPPStateInfo appInfo = HwArbitrationStateMachine.this.mHwHiStreamManager == null ? null : HwArbitrationStateMachine.this.mHwHiStreamManager.getCurStreamAppInfo();
            if (appInfo != null && -1 != appInfo.mAppUID) {
                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "AppStreamInfo:" + appInfo.toString());
                HwArbitrationStateMachine.this.mHwHiRadioBoost.BrainAppStateNotifyDSBooster(appInfo, 0, HwArbitrationStateMachine.this.hiStreamAppState);
            }
        }

        private void stopAllCellOptimize() {
            if (HwArbitrationStateMachine.mHwArbitrationAppBoostMap == null || HwArbitrationStateMachine.mHwArbitrationAppBoostMap.isEmpty()) {
                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "stopAllCellOptimize: no Optimize to recover ");
                return;
            }
            for (Integer appUid : HwArbitrationStateMachine.mHwArbitrationAppBoostMap.keySet()) {
                HwAPPStateInfo tempData = new HwAPPStateInfo();
                tempData.mAppUID = appUid.intValue();
                stopUidOptimiztion(tempData);
            }
            HwArbitrationStateMachine.mHwArbitrationAppBoostMap.clear();
        }

        private void stopUidOptimiztion(HwAPPStateInfo appInfo) {
            if (appInfo != null && HwArbitrationStateMachine.mHwArbitrationAppBoostMap != null && !HwArbitrationStateMachine.mHwArbitrationAppBoostMap.isEmpty()) {
                HwArbitrationAppBoostInfo boostInfo = (HwArbitrationAppBoostInfo) HwArbitrationStateMachine.mHwArbitrationAppBoostMap.get(Integer.valueOf(appInfo.mAppUID));
                if (boostInfo != null) {
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "stopUidOptimiztion enter: boostInfo is " + boostInfo.toString());
                    HwAPPStateInfo tempData = new HwAPPStateInfo();
                    tempData.copyObjectValue(appInfo);
                    tempData.mAppId = boostInfo.getAppID();
                    tempData.mAppUID = boostInfo.getBoostUID();
                    tempData.mScenceId = boostInfo.getSceneId();
                    int solution = boostInfo.getSolution();
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "stopUidOptimiztion: for " + tempData.mAppId + ", solution to stop is " + solution);
                    HwArbitrationStateMachine.this.mHwHiRadioBoost.stopOptimizedActionsForApp(tempData, false, solution);
                }
            }
        }

        private int findActionsNeedStart(HwAPPStateInfo appInfo, int actions2) {
            if (appInfo == null) {
                return -1;
            }
            if (actions2 <= 0) {
                return actions2;
            }
            if (HwArbitrationStateMachine.mHwArbitrationAppBoostMap != null && !HwArbitrationStateMachine.mHwArbitrationAppBoostMap.isEmpty()) {
                HwArbitrationAppBoostInfo boostInfo = (HwArbitrationAppBoostInfo) HwArbitrationStateMachine.mHwArbitrationAppBoostMap.get(Integer.valueOf(appInfo.mAppUID));
                if (boostInfo != null) {
                    int lastActions = (~boostInfo.getSolution()) & actions2;
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "findActions: actions started is " + boostInfo.getSolution() + ", actions to judge is " + actions2 + ", actions going to start is " + lastActions);
                    return lastActions;
                }
            }
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "findActions: actions to start is " + actions2);
            return actions2;
        }

        private int findActionsNeedStop(HwAPPStateInfo appInfo, int configActions) {
            if (configActions <= 0 || appInfo == null) {
                return configActions;
            }
            int actionsStop = -1;
            if (HwArbitrationStateMachine.mHwArbitrationAppBoostMap == null || HwArbitrationStateMachine.mHwArbitrationAppBoostMap.isEmpty()) {
                HwArbitrationCommonUtils.logE(HwArbitrationStateMachine.TAG, "findActionsNeedStop: no started actions for uid: " + appInfo.mAppUID);
            } else {
                HwArbitrationAppBoostInfo boostInfo = (HwArbitrationAppBoostInfo) HwArbitrationStateMachine.mHwArbitrationAppBoostMap.get(Integer.valueOf(appInfo.mAppUID));
                if (boostInfo != null) {
                    int actionsStop2 = configActions & boostInfo.getSolution();
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "findActionsNeedStop: actions started is " + boostInfo.getSolution() + ", actions to judge is " + configActions + ", actions going to remain is " + actionsStop2);
                    actionsStop = (~actionsStop2) & boostInfo.getSolution();
                }
            }
            return actionsStop;
        }

        private int getActionsToStart(HwAPPStateInfo appInfo) {
            return findActionsNeedStart(appInfo, getActionsConfig(appInfo));
        }

        private int getActionsConfig(HwAPPStateInfo appInfo) {
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "Enter getActionsConfig");
            HwAPPQoEResourceManger unused = HwArbitrationStateMachine.this.mHwAPPQoEResourceManger = HwAPPQoEResourceManger.getInstance();
            int actions2 = 0;
            if (!(HwArbitrationStateMachine.this.mHwAPPQoEResourceManger == null || appInfo == null)) {
                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, appInfo.toString());
                actions2 = HwArbitrationStateMachine.this.mHwAPPQoEResourceManger.getScenceAction(appInfo.mAppType, appInfo.mAppId, appInfo.mScenceId);
                if (actions2 < 0) {
                    HwArbitrationCommonUtils.logE(HwArbitrationStateMachine.TAG, "get SceneAction from AppQoE xmlConfg failed");
                }
            }
            return actions2;
        }

        public void exit() {
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "Exit CellMonitorState");
            stopAllCellOptimize();
            if (HwArbitrationFunction.isPvpScene()) {
                HwArbitrationStateMachine.this.mHwWifiBoost.limitedSpeed(1, 0, 0);
            }
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case 100:
                    this.mAppInfo = (HwAPPStateInfo) message.obj;
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "In CellMonitorState MSG_GAME_STATE_START, mCurrentRAT is " + HwArbitrationFunction.getDataTech());
                    if (this.mAppInfo != null && HwArbitrationFunction.isDataTechSuitable()) {
                        this.actions = getActionsConfig(this.mAppInfo);
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "ENTER GAME: getActionConfig,actions = " + this.actions);
                        this.actions = findActionsNeedStart(this.mAppInfo, this.actions);
                        HwArbitrationStateMachine.this.mHwHiRadioBoost.startOptimizeActionsForApp(this.mAppInfo, this.actions);
                        HwArbitrationStateMachine.this.setStateMachineHashMap(this.mAppInfo, 801, false, false, this.actions);
                        break;
                    }
                case 101:
                case 103:
                    this.mAppInfo = (HwAPPStateInfo) message.obj;
                    if (!(this.mAppInfo == null || HwArbitrationStateMachine.mHwArbitrationAppBoostMap == null)) {
                        stopUidOptimiztion(this.mAppInfo);
                        HwArbitrationStateMachine.mHwArbitrationAppBoostMap.remove(Integer.valueOf(this.mAppInfo.mAppUID));
                    }
                    HwArbitrationStateMachine.this.mHwWifiBoost.limitedSpeed(1, 0, 0);
                    break;
                case 104:
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "In CellMonitorState MSG_GAME_ENTER_PVP_BATTLE");
                    this.mAppInfo = (HwAPPStateInfo) message.obj;
                    if (this.mAppInfo != null && HwArbitrationFunction.isDataTechSuitable()) {
                        this.actions = getActionsConfig(this.mAppInfo) | 1;
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "ENTER PVP: getActionConfig:" + getActionsConfig(this.mAppInfo) + ", actions = " + this.actions);
                        this.actions = findActionsNeedStart(this.mAppInfo, this.actions);
                        HwArbitrationStateMachine.this.mHwHiRadioBoost.startOptimizeActionsForApp(this.mAppInfo, this.actions);
                        HwArbitrationStateMachine.this.updateStateMachineHashMap(this.mAppInfo, 801, false, false, this.actions);
                        HwArbitrationStateMachine.this.mHwWifiBoost.limitedSpeed(1, 1, 1);
                        break;
                    }
                case 105:
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "In CellMonitorState MSG_GAME_EXIT_PVP_BATTLE");
                    this.mAppInfo = (HwAPPStateInfo) message.obj;
                    if (!(this.mAppInfo == null || HwArbitrationStateMachine.mHwArbitrationAppBoostMap == null || HwArbitrationStateMachine.mHwArbitrationAppBoostMap.get(Integer.valueOf(this.mAppInfo.mAppUID)) == null)) {
                        this.actions = getActionsConfig(this.mAppInfo);
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "EXIT PVP:getActionConfig:" + this.actions);
                        int newActions = findActionsNeedStop(this.mAppInfo, this.actions);
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "findActionsNeedStop:StopAction:" + newActions);
                        HwArbitrationStateMachine.this.mHwHiRadioBoost.stopOptimizedActionsForApp(this.mAppInfo, true, newActions);
                        HwArbitrationStateMachine.this.setStateMachineHashMap(this.mAppInfo, 801, false, false, this.actions);
                        HwArbitrationStateMachine.this.mHwWifiBoost.limitedSpeed(1, 0, 0);
                        break;
                    }
                case 106:
                    this.mAppInfo = (HwAPPStateInfo) message.obj;
                    if (this.mAppInfo != null && HwArbitrationFunction.isDataTechSuitable()) {
                        int actions2 = getActionsToStart(this.mAppInfo);
                        if (actions2 < 0) {
                            actions2 = 1;
                        }
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "histream actions:" + actions2);
                        HwArbitrationStateMachine.this.mHwHiRadioBoost.startOptimizeActionsForApp(this.mAppInfo, actions2);
                        HwArbitrationStateMachine.this.setStateMachineHashMap(this.mAppInfo, 801, false, false, actions2);
                        HwArbitrationStateMachine.this.printMap(this.mAppInfo.mAppUID);
                        break;
                    }
                case 107:
                    this.mAppInfo = (HwAPPStateInfo) message.obj;
                    if (!(this.mAppInfo == null || HwArbitrationStateMachine.mHwArbitrationAppBoostMap == null)) {
                        stopUidOptimiztion(this.mAppInfo);
                        HwArbitrationStateMachine.mHwArbitrationAppBoostMap.remove(Integer.valueOf(this.mAppInfo.mAppUID));
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "Stop HiStream actions");
                        break;
                    }
                case 109:
                case 111:
                case 113:
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "In CellMonitorState need startOptimizeActionsForApp");
                    this.mAppInfo = (HwAPPStateInfo) message.obj;
                    if (this.mAppInfo != null) {
                        this.actions = getActionsToStart(this.mAppInfo);
                        if (this.actions > 0 && HwArbitrationFunction.isDataTechSuitable()) {
                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "begin HiRadioBoost, UID = " + this.mAppInfo.mAppUID + ", actions =" + this.actions);
                            HwArbitrationStateMachine.this.mHwHiRadioBoost.startOptimizeActionsForApp(this.mAppInfo, this.actions);
                            HwArbitrationStateMachine.this.setStateMachineHashMap(this.mAppInfo, 801, false, false, this.actions);
                            HwArbitrationStateMachine.this.printMap(this.mAppInfo.mAppUID);
                            break;
                        }
                    }
                    break;
                case 110:
                case 112:
                case 114:
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "In CellMonitorState need stopOptimizedActionsForApp , message is " + message.what);
                    this.mAppInfo = (HwAPPStateInfo) message.obj;
                    if (!(this.mAppInfo == null || HwArbitrationStateMachine.mHwArbitrationAppBoostMap == null)) {
                        HwArbitrationAppBoostInfo boostInfo = (HwArbitrationAppBoostInfo) HwArbitrationStateMachine.mHwArbitrationAppBoostMap.get(Integer.valueOf(this.mAppInfo.mAppUID));
                        if (boostInfo != null && boostInfo.getSceneId() == 100105) {
                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "HiStream Audio is still at background");
                            break;
                        } else {
                            stopUidOptimiztion(this.mAppInfo);
                            HwArbitrationStateMachine.mHwArbitrationAppBoostMap.remove(Integer.valueOf(this.mAppInfo.mAppUID));
                            break;
                        }
                    }
                    break;
                case 115:
                case HwArbitrationDEFS.MSG_STREAMING_VIDEO_BAD:
                case HwArbitrationDEFS.MSG_STREAMING_AUDIO_BAD:
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "In CellMonitorState process MSG_GAME/APP_STATE_BAD");
                    break;
                case 120:
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "in CellMonitor: MSG_STREAMING_APP_FOREGROUND");
                    this.mAppInfo = (HwAPPStateInfo) message.obj;
                    if (this.mAppInfo != null && this.mAppInfo.mScenceId == 100106 && HwArbitrationFunction.isDataTechSuitable()) {
                        int actions3 = getActionsToStart(this.mAppInfo);
                        if (actions3 < 0) {
                            actions3 = 1;
                        }
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "Start HiStream actions:" + actions3);
                        HwArbitrationStateMachine.this.mHwHiRadioBoost.startOptimizeActionsForApp(this.mAppInfo, actions3);
                        HwArbitrationStateMachine.this.setStateMachineHashMap(this.mAppInfo, 801, false, false, actions3);
                        break;
                    }
                case 121:
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "in CellMonitor: MSG_STREAMING_APP_BACKGROUND");
                    this.mAppInfo = (HwAPPStateInfo) message.obj;
                    if (!(this.mAppInfo == null || this.mAppInfo.mScenceId != 100106 || HwArbitrationStateMachine.mHwArbitrationAppBoostMap == null)) {
                        stopUidOptimiztion(this.mAppInfo);
                        HwArbitrationStateMachine.mHwArbitrationAppBoostMap.remove(this.mAppInfo);
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "Stop HiStream actions");
                        break;
                    }
                case 1005:
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "CellMonitorState transitionTo WifiMonitorState");
                    HwArbitrationStateMachine.this.transitionTo(HwArbitrationStateMachine.this.mWifiMonitorState);
                    break;
                case HwArbitrationDEFS.MSG_CELL_STATE_CONNECTED:
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "In CellMonitorState don't process MSG_CELL_STATE_CONNECTED");
                    break;
                case HwArbitrationDEFS.MSG_CELL_STATE_DISCONNECT:
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "CellMonitorState:MSG_CELL_STATE_DISCONNECT");
                    HwArbitrationStateMachine.this.transitionTo(HwArbitrationStateMachine.this.mInitialState);
                    break;
                case HwArbitrationDEFS.MSG_SCREEN_IS_TURNOFF:
                case 1023:
                    stopAllCellOptimize();
                    if (1012 == message.what && HwArbitrationFunction.isPvpScene()) {
                        HwArbitrationStateMachine.this.mHwWifiBoost.limitedSpeed(1, 0, 0);
                        break;
                    }
                case HwArbitrationDEFS.MSG_SCREEN_IS_ON:
                    HwArbitrationStateMachine hwArbitrationStateMachine = HwArbitrationStateMachine.this;
                    HwAPPQoEManager instance = HwAPPQoEManager.getInstance();
                    hwArbitrationStateMachine.mHwAPPQoEManager = instance;
                    if (instance != null) {
                        HwAPPStateInfo curAPPStateInfo = HwArbitrationStateMachine.this.mHwAPPQoEManager.getCurAPPStateInfo();
                        this.mAppInfo = curAPPStateInfo;
                        if (curAPPStateInfo != null && HwArbitrationFunction.isDataTechSuitable()) {
                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, this.mAppInfo.toString());
                            handleCurrentScene(this.mAppInfo);
                        }
                    }
                    handleHiStreamScene();
                    if (HwArbitrationFunction.isPvpScene()) {
                        HwArbitrationStateMachine.this.mHwWifiBoost.limitedSpeed(1, 1, 1);
                        break;
                    }
                    break;
                case 1022:
                    if (HwArbitrationStateMachine.this.mHwAPPQoEManager != null) {
                        this.mAppInfo = HwArbitrationStateMachine.this.mHwAPPQoEManager.getCurAPPStateInfo();
                        if (this.mAppInfo != null) {
                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "AppInfo:" + this.mAppInfo.toString());
                            this.actions = getActionsToStart(this.mAppInfo);
                            HwArbitrationStateMachine.this.mHwHiRadioBoost.startOptimizeActionsForApp(this.mAppInfo, this.actions);
                            HwArbitrationStateMachine.this.setStateMachineHashMap(this.mAppInfo, 801, false, false, this.actions);
                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "Back to RAT suitable, start actions = " + this.actions);
                            handleHiStreamScene();
                            break;
                        }
                    }
                    break;
                case HwArbitrationDEFS.MSG_AIRPLANE_MODE_ON:
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "In CellMonitorState MSG_AIRPLANE_MODE_ON");
                    HwArbitrationStateMachine.this.transitionTo(HwArbitrationStateMachine.this.mInitialState);
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    class DefaultState extends State {
        DefaultState() {
        }

        public void enter() {
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "Enter DefaultState");
            HwArbitrationStateMachine.this.mHwWifiBoost.initialBGLimitModeRecords();
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "HwWifiBoost:initialBGLimitModeRecords complete");
        }

        public void exit() {
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "exit DefaultState");
        }

        public boolean processMessage(Message message) {
            if (HwArbitrationStateMachine.this.getCurrentState() != null) {
                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "DefaultState Msg: " + message.what + ", received in " + HwArbitrationStateMachine.this.getCurrentState().getName());
            }
            switch (message.what) {
                case 100:
                case 104:
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "DefaultState unhandled MSG_GAME_ENTER_PVP_BATTLE or MSG_GAME_STATE_START");
                    break;
                case 105:
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "DefaultState unhandled MSG_GAME_EXIT_PVP_BATTLE");
                    break;
                case 115:
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "DefaultState unhandled MSG_GAME_WAR_STATE_BAD");
                    break;
                case 1005:
                    HwArbitrationStateMachine.this.deferMessage(message);
                    break;
                case HwArbitrationDEFS.MSG_WIFI_STATE_DISCONNECT:
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "DefaultState MSG_WIFI_STATE_DISCONNECT");
                    if (!HwArbitrationStateMachine.this.deliverErrorMPLinkCase()) {
                        if (801 != HwArbitrationCommonUtils.getActiveConnectType(HwArbitrationStateMachine.this.mContext) || HwArbitrationStateMachine.this.mCellMonitorState == HwArbitrationStateMachine.this.getCurrentState()) {
                            if (HwArbitrationStateMachine.this.getCurrentState() != HwArbitrationStateMachine.this.mInitialState && 802 == HwArbitrationCommonUtils.getActiveConnectType(HwArbitrationStateMachine.this.mContext)) {
                                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "Not handle:MSG_WIFI_STATE_DISCONNECT in default state");
                                HwArbitrationStateMachine.this.transitionTo(HwArbitrationStateMachine.this.mInitialState);
                                break;
                            }
                        } else {
                            HwArbitrationStateMachine.this.transitionTo(HwArbitrationStateMachine.this.mCellMonitorState);
                            break;
                        }
                    }
                    break;
                case HwArbitrationDEFS.MSG_CELL_STATE_DISCONNECT:
                case HwArbitrationDEFS.MSG_CLOSE_4G_OR_WCDMA:
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "DefaultState: MSG_CELL_STATE_DISCONNECTED");
                    if (!HwArbitrationStateMachine.this.deliverErrorMPLinkCase()) {
                        if (800 != HwArbitrationCommonUtils.getActiveConnectType(HwArbitrationStateMachine.this.mContext)) {
                            if (802 == HwArbitrationCommonUtils.getActiveConnectType(HwArbitrationStateMachine.this.mContext) && HwArbitrationStateMachine.this.getCurrentState() != HwArbitrationStateMachine.this.mInitialState) {
                                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "Not handle:ACTIVE_CONNECT_IS_NONE in default state");
                                HwArbitrationStateMachine.this.transitionTo(HwArbitrationStateMachine.this.mInitialState);
                                break;
                            }
                        } else {
                            HwArbitrationStateMachine.this.transitionTo(HwArbitrationStateMachine.this.mWifiMonitorState);
                            break;
                        }
                    }
                    break;
                case HwArbitrationDEFS.MSG_SCREEN_IS_TURNOFF:
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "MSG_SCREEN_IS_TURNOFF");
                    HwArbitrationStateMachine.this.deliverErrorMPLinkCase();
                    break;
                case HwArbitrationDEFS.MSG_STATE_IS_ROAMING:
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "update  mDataRoamingState:true");
                    break;
                case HwArbitrationDEFS.MSG_SCREEN_IS_ON:
                    break;
                case HwArbitrationDEFS.MSG_STATE_IN_SERVICE:
                    int unused = HwArbitrationStateMachine.this.mCurrentServiceState = 0;
                    break;
                case HwArbitrationDEFS.MSG_STATE_OUT_OF_SERVICE:
                    int unused2 = HwArbitrationStateMachine.this.mCurrentServiceState = 1;
                    break;
                case 1021:
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "update  mDataRoamingState:false");
                    break;
                case HwArbitrationDEFS.MSG_NOTIFY_CURRENT_NETWORK:
                    int unused3 = HwArbitrationStateMachine.this.mCurrentActiveNetwork = message.arg1;
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "update active network: " + HwArbitrationStateMachine.this.mCurrentActiveNetwork);
                    break;
                case HwArbitrationDEFS.MSG_DEVICE_BOOT_COMPLETED:
                    boolean unused4 = HwArbitrationStateMachine.this.mDeviceBootCommpleted = true;
                    HwArbitrationStateMachine.this.mHwHiRadioBoost.initCommBoosterManager();
                    break;
                case 2001:
                    HwArbitrationCommonUtils.logE(HwArbitrationStateMachine.TAG, "mIsMpLinkError is false");
                    boolean unused5 = HwArbitrationStateMachine.this.mIsMpLinkError = false;
                    break;
                case HwArbitrationDEFS.MSG_MPLINK_NONCOEX_MODE:
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "DefaultState start COEX error");
                    HwArbitrationStateMachine.this.deliverErrorMPLinkCase();
                    break;
                case HwArbitrationDEFS.MSG_WIFI_PLUS_ENABLE:
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "DefaultState MSG_WIFI_PLUS_ENABLE");
                    break;
                case HwArbitrationDEFS.MSG_WIFI_PLUS_DISABLE:
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "DefaultState MSG_WIFI_PLUS_DISABLE");
                    HwArbitrationStateMachine.this.deliverErrorMPLinkCase();
                    break;
                case HwArbitrationDEFS.MSG_AIRPLANE_MODE_ON:
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "DefaultState MSG_AIRPLANE_MODE_ON ");
                    if (!HwArbitrationStateMachine.this.deliverErrorMPLinkCase()) {
                        switch (HwArbitrationCommonUtils.getActiveConnectType(HwArbitrationStateMachine.this.mContext)) {
                            case 800:
                                if (HwArbitrationStateMachine.this.mWifiMonitorState != HwArbitrationStateMachine.this.getCurrentState()) {
                                    HwArbitrationStateMachine.this.transitionTo(HwArbitrationStateMachine.this.mWifiMonitorState);
                                    break;
                                }
                                break;
                            case 801:
                                if (HwArbitrationStateMachine.this.mCellMonitorState != HwArbitrationStateMachine.this.getCurrentState()) {
                                    HwArbitrationStateMachine.this.transitionTo(HwArbitrationStateMachine.this.mCellMonitorState);
                                    break;
                                }
                                break;
                            default:
                                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "cannot distinguish state, Enter InitialState");
                                if (HwArbitrationStateMachine.this.mInitialState != HwArbitrationStateMachine.this.getCurrentState()) {
                                    HwArbitrationStateMachine.this.transitionTo(HwArbitrationStateMachine.this.mInitialState);
                                    break;
                                }
                                break;
                        }
                    }
                    break;
                case HwArbitrationDEFS.MSG_MPLINK_AI_DEVICE_COEX_MODE:
                    HwArbitrationStateMachine.this.transitionTo(HwArbitrationStateMachine.this.mMPLinkStartedState);
                    break;
                case HwArbitrationDEFS.MSG_VPN_STATE_OPEN:
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "MSG_VPN_STATE_OPEN");
                    HwArbitrationStateMachine.this.deliverErrorMPLinkCase();
                    break;
                case HwArbitrationDEFS.MSG_HISTREAM_TRIGGER_MPPLINK_INTERNAL:
                    boolean unused6 = HwArbitrationStateMachine.this.mHiStreamTriggerOrStopMplink = true;
                    break;
                case HwArbitrationDEFS.MSG_Stop_MPLink_By_Notification:
                    HwArbitrationCommonUtils.logE(HwArbitrationStateMachine.TAG, "MSG_Stop_MPLink_By_Notification");
                    break;
                case HwArbitrationDEFS.MSG_Recovery_Flag_By_Notification:
                    HwArbitrationCommonUtils.logE(HwArbitrationStateMachine.TAG, "MSG_Recovery_Flag_By_Notification");
                    boolean unused7 = HwArbitrationStateMachine.this.mDenyByNotification = false;
                    break;
                case 3000:
                    if (message.arg1 != 0) {
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "HIRADIO_4G_SWITCH_3G_FAILED");
                        break;
                    } else {
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "HIRADIO_4G_SWITCH_3G_SUCCESS");
                        HwArbitrationStateMachine.this.showToast(HwArbitrationStateMachine.this.mContext.getString(33686111));
                        break;
                    }
                default:
                    if (HwArbitrationStateMachine.this.getCurrentState() != null) {
                        HwArbitrationCommonUtils.logE(HwArbitrationStateMachine.TAG, "Unhandled Message: " + message.what + " in State: " + HwArbitrationStateMachine.this.getCurrentState().getName());
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
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "Enter InitialState");
            if (HwArbitrationStateMachine.mHwArbitrationAppBoostMap != null) {
                HwArbitrationStateMachine.mHwArbitrationAppBoostMap.clear();
            }
        }

        public void exit() {
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "Exit InitialState");
        }

        public boolean processMessage(Message message) {
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "InitialState Msg: " + message.what);
            int i = message.what;
            if (i == 1005) {
                HwArbitrationStateMachine.this.transitionTo(HwArbitrationStateMachine.this.mWifiMonitorState);
            } else if (i == 1009) {
                HwArbitrationStateMachine.this.transitionTo(HwArbitrationStateMachine.this.mCellMonitorState);
            } else if (i == 2015 || i == 2027) {
                if (2027 == message.what) {
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "MSG_MPLINK_ERROR_HANDLER");
                } else {
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "MSG_MPLINK_STOP_COEX_SUCC");
                }
                switch (HwArbitrationCommonUtils.getActiveConnectType(HwArbitrationStateMachine.this.mContext)) {
                    case 800:
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "InitialState WIFI_NETWORK");
                        HwArbitrationStateMachine.this.transitionTo(HwArbitrationStateMachine.this.mWifiMonitorState);
                        break;
                    case 801:
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "InitialState CELL_NETWORK");
                        HwArbitrationStateMachine.this.transitionTo(HwArbitrationStateMachine.this.mCellMonitorState);
                        break;
                    default:
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "cannot distinguish state");
                        break;
                }
            } else {
                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "Message:" + message.what + " did't process in InitialState, go to parent state");
                return false;
            }
            return true;
        }
    }

    class MPLinkStartedState extends State {
        private static final int InstantAPP = 2;
        private static final int StreamingAPP = 3;
        private HwAPPStateInfo appInfo;
        private MplinkBindResultInfo mplinkBindResultInfo;
        private int mplinkErrorCode = -1;
        private long pingPongTMWiFi_Good = 0;
        private int punishWiFiGoodCount = 0;
        private int stopMplinkReason = -1;
        private boolean trgPingPongWiFi_Good;
        private int uid;
        private boolean wifiGoodFlag = true;

        MPLinkStartedState() {
        }

        public void enter() {
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "Enter MPLinkStartedState");
            this.trgPingPongWiFi_Good = false;
        }

        public void exit() {
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "Exit MPLinkStartedState");
            boolean unused = HwArbitrationStateMachine.this.mIsMpLinkBinding = false;
            int unused2 = HwArbitrationStateMachine.this.mCoexCount = 0;
            int unused3 = HwArbitrationStateMachine.this.mMpLinkCount = 0;
            if (HwArbitrationStateMachine.mHwArbitrationAppBoostMap != null) {
                HwArbitrationStateMachine.mHwArbitrationAppBoostMap.clear();
            }
            boolean unused4 = HwArbitrationStateMachine.this.mIsMpLinkError = false;
            this.wifiGoodFlag = true;
            this.stopMplinkReason = -1;
            this.mplinkErrorCode = -1;
            HwArbitrationStateMachine.this.removeMessages(HwArbitrationDEFS.MSG_SET_PingPong_WiFi_Good_FALSE);
            HwArbitrationDisplay.getInstance(HwArbitrationStateMachine.this.mContext).requestDataMonitor(false, 1);
            if (HwAPPQoEManager.getInstance() != null) {
                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "stopWifiLinkMonitor all");
                HwAPPQoEManager.getInstance().stopWifiLinkMonitor(-1, true);
            }
        }

        public boolean processMessage(Message message) {
            Message message2 = message;
            int i = message2.what;
            switch (i) {
                case 100:
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "MSG_GAME_STATE_START");
                    HwArbitrationStateMachine.this.printMap(this.uid);
                    this.appInfo = (HwAPPStateInfo) message2.obj;
                    HwArbitrationStateMachine.this.handleGeneralGameStart(this.appInfo);
                    break;
                case 101:
                    this.appInfo = (HwAPPStateInfo) message2.obj;
                    if (this.appInfo != null) {
                        HwArbitrationStateMachine.this.printMap(this.appInfo.mAppUID);
                        HwArbitrationStateMachine.this.handleGeneralGameEnd(this.appInfo);
                        HwArbitrationStateMachine.this.handleGamePvpEnd(this.appInfo);
                        break;
                    }
                    break;
                default:
                    switch (i) {
                        case 103:
                            HwArbitrationStateMachine.this.printMap(this.uid);
                            this.appInfo = (HwAPPStateInfo) message2.obj;
                            if (this.appInfo != null) {
                                HwArbitrationStateMachine.this.handleGeneralGameEnd(this.appInfo);
                                break;
                            }
                            break;
                        case 104:
                            this.appInfo = (HwAPPStateInfo) message2.obj;
                            if (this.appInfo != null) {
                                HwArbitrationStateMachine.this.handleGeneralGameStart(this.appInfo);
                                HwArbitrationStateMachine.this.handleGamePvpStart(this.appInfo);
                                updateMapAppInfo(this.appInfo, false);
                                HwArbitrationStateMachine.this.printMap(this.appInfo.mAppUID);
                                break;
                            }
                            break;
                        case 105:
                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "MSG_GAME_EXIT_PVP_BATTLE");
                            this.appInfo = (HwAPPStateInfo) message2.obj;
                            if (this.appInfo != null) {
                                HwArbitrationStateMachine.this.handleGamePvpEnd(this.appInfo);
                                break;
                            }
                            break;
                        case 106:
                            this.appInfo = (HwAPPStateInfo) message2.obj;
                            HwArbitrationStateMachine.this.handleStreamingStart(this.appInfo);
                            break;
                        case 107:
                            this.appInfo = (HwAPPStateInfo) message2.obj;
                            HwArbitrationStateMachine.this.handleStreamingEnd(this.appInfo);
                            if (HwArbitrationStateMachine.this.isInMPLink(this.appInfo.mAppUID)) {
                                this.stopMplinkReason = 6;
                                stopMPLinkAppBind(this.appInfo.mAppUID);
                                break;
                            }
                            break;
                        default:
                            switch (i) {
                                case 109:
                                case 111:
                                case 113:
                                    this.appInfo = (HwAPPStateInfo) message2.obj;
                                    if (this.appInfo != null && (!HwArbitrationStateMachine.this.isInMPLink(this.appInfo.mAppUID) || !HwArbitrationStateMachine.this.isStreamScene(this.appInfo.mAppUID))) {
                                        updateMapAppInfo(this.appInfo, true);
                                        break;
                                    }
                                case 110:
                                    this.appInfo = (HwAPPStateInfo) message2.obj;
                                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "MSG_INSTANT_APP_END");
                                    if (this.appInfo != null) {
                                        if (!HwArbitrationStateMachine.this.isInMPLink(this.appInfo.mAppUID) || HwArbitrationStateMachine.this.isStreamScene(this.appInfo.mAppUID)) {
                                            if (!HwArbitrationStateMachine.this.isInMPLink(this.appInfo.mAppUID)) {
                                                stopMPLinkCoex(this.appInfo.mAppUID);
                                                break;
                                            }
                                        } else {
                                            this.stopMplinkReason = 6;
                                            stopMPLinkAppBind(this.appInfo.mAppUID);
                                            break;
                                        }
                                    }
                                    break;
                                case 112:
                                case 114:
                                    this.appInfo = (HwAPPStateInfo) message2.obj;
                                    if (this.appInfo != null && !HwArbitrationStateMachine.this.isInMPLink(this.appInfo.mAppUID)) {
                                        stopMPLinkCoex(this.appInfo.mAppUID);
                                        break;
                                    }
                                case 115:
                                    this.appInfo = (HwAPPStateInfo) message2.obj;
                                    handleAppExpBad(this.appInfo, 2);
                                    break;
                                default:
                                    switch (i) {
                                        case HwArbitrationDEFS.MSG_MPLINK_BIND_CHECK_OK_NOTIFY:
                                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "MSG_MPLINK_BIND_CHECK_OK_NOTIFY");
                                            HwArbitrationStateMachine.this.removeMessages(HwArbitrationDEFS.MSG_QUERY_QOE_WM_TIMEOUT);
                                            this.uid = message2.arg1;
                                            if (!HwArbitrationStateMachine.this.isInMPLink(this.uid)) {
                                                startMPLinkAppBind(this.uid);
                                                break;
                                            } else {
                                                this.stopMplinkReason = 3;
                                                stopMPLinkAppBind(this.uid);
                                                break;
                                            }
                                        case HwArbitrationDEFS.MSG_MPLINK_BIND_CHECK_FAIL_NOTIFY:
                                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "MSG_MPLINK_BIND_CHECK_FAIL_NOTIFY");
                                            HwArbitrationStateMachine.this.removeMessages(HwArbitrationDEFS.MSG_QUERY_QOE_WM_TIMEOUT);
                                            this.uid = message2.arg1;
                                            HwArbitrationStateMachine.this.setQueryTime(this.uid);
                                            if (HwArbitrationStateMachine.this.noAPPInMPLink()) {
                                                stopMPLinkCoex(this.uid);
                                            }
                                            boolean unused = HwArbitrationStateMachine.this.mIsMpLinkBinding = false;
                                            HwArbitrationStateMachine.this.updateMplinkCHRExceptionEvent(message2.arg1, 8, message2.arg2);
                                            HwAPPChrManager.getInstance().updateStatisInfo(this.appInfo, 11);
                                            break;
                                        case HwArbitrationDEFS.MSG_MPLINK_BIND_SUCCESS:
                                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "MSG_MPLINK_BIND_SUCCESS");
                                            this.mplinkBindResultInfo = (MplinkBindResultInfo) message2.obj;
                                            boolean unused2 = HwArbitrationStateMachine.this.mIsMpLinkBinding = false;
                                            if (this.mplinkBindResultInfo != null) {
                                                this.uid = this.mplinkBindResultInfo.getUid();
                                                if (!(HwArbitrationStateMachine.mHwArbitrationAppBoostMap == null || HwArbitrationStateMachine.mHwArbitrationAppBoostMap.get(Integer.valueOf(this.uid)) == null)) {
                                                    int appID = ((HwArbitrationAppBoostInfo) HwArbitrationStateMachine.mHwArbitrationAppBoostMap.get(Integer.valueOf(this.uid))).getAppID();
                                                    if (!(1004 == appID || 1002 == appID)) {
                                                        HwArbitrationDisplay.setToast(HwArbitrationStateMachine.this.mContext, HwArbitrationStateMachine.this.mContext.getString(33686118));
                                                    }
                                                }
                                                HwArbitrationStateMachine.this.printMap(this.uid);
                                                if (HwArbitrationStateMachine.this.isStreamScene(this.uid)) {
                                                    HwArbitrationStateMachine.this.removeMessages(HwArbitrationDEFS.MSG_HISTREAM_TRIGGER_MPPLINK_INTERNAL);
                                                    HwArbitrationStateMachine.this.sendMessageDelayed(HwArbitrationDEFS.MSG_HISTREAM_TRIGGER_MPPLINK_INTERNAL, HwArbitrationStateMachine.this.triggerMPlinkInternal);
                                                    boolean unused3 = HwArbitrationStateMachine.this.mHiStreamTriggerOrStopMplink = false;
                                                }
                                                if (!(HwArbitrationStateMachine.mHwArbitrationAppBoostMap == null || HwArbitrationStateMachine.mHwArbitrationAppBoostMap.get(Integer.valueOf(this.uid)) == null)) {
                                                    if (!((HwArbitrationAppBoostInfo) HwArbitrationStateMachine.mHwArbitrationAppBoostMap.get(Integer.valueOf(this.uid))).getIsMPlink()) {
                                                        int unused4 = HwArbitrationStateMachine.this.mMpLinkCount = HwArbitrationStateMachine.this.mMpLinkCount + 1;
                                                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "MSG_MPLINK_BIND_SUCCESS, mMpLinkCount is " + HwArbitrationStateMachine.this.mMpLinkCount);
                                                    } else {
                                                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "MSG_MPLINK_BIND_SUCCESS, " + this.uid + "is already in mMpLink!");
                                                    }
                                                }
                                                if (System.currentTimeMillis() - this.pingPongTMWiFi_Good < HwArbitrationDEFS.DelayTimeMillisA) {
                                                    this.punishWiFiGoodCount++;
                                                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "MSG_MPLINK_BIND_SUCCESS, punishWiFiGoodCount: " + this.punishWiFiGoodCount);
                                                    if (1 <= this.punishWiFiGoodCount) {
                                                        this.trgPingPongWiFi_Good = true;
                                                        HwArbitrationStateMachine.this.sendMessageDelayed(HwArbitrationDEFS.MSG_SET_PingPong_WiFi_Good_FALSE, this.uid, HwArbitrationDEFS.DelayTimeMillisB);
                                                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "MSG_MPLINK_BIND_SUCCESS, trigger the Ping-Pong ================================");
                                                        this.punishWiFiGoodCount = 0;
                                                    }
                                                } else {
                                                    this.punishWiFiGoodCount = 0;
                                                }
                                                long unused5 = HwArbitrationStateMachine.this.pingPongTMCell_Bad = System.currentTimeMillis();
                                                if (!(HwAPPQoEManager.getInstance() == null || HwArbitrationStateMachine.mHwArbitrationAppBoostMap == null || HwArbitrationStateMachine.mHwArbitrationAppBoostMap.get(Integer.valueOf(this.uid)) == null || this.trgPingPongWiFi_Good)) {
                                                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "MSG_MPLINK_BIND_SUCCESS, startWifiLinkMonitor: " + this.uid);
                                                    HwAPPQoEManager.getInstance().startWifiLinkMonitor(this.uid, ((HwArbitrationAppBoostInfo) HwArbitrationStateMachine.mHwArbitrationAppBoostMap.get(Integer.valueOf(this.uid))).getSceneId());
                                                }
                                                if (HwArbitrationStateMachine.this.updateStateInfoMap(this.mplinkBindResultInfo, HwArbitrationFunction.getNetwork(HwArbitrationStateMachine.this.mContext, this.mplinkBindResultInfo.getNetwork()), true)) {
                                                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "start MPLink success and update table success");
                                                    HwArbitrationStateMachine.this.sendMPLinkBroadcast(this.uid);
                                                    HwArbitrationStateMachine.this.printMap(this.uid);
                                                }
                                                if (801 == HwArbitrationStateMachine.this.getCurrentNetwork(HwArbitrationStateMachine.this.mContext, this.uid) && HwArbitrationStateMachine.this.isInMPLink(this.uid)) {
                                                    HwArbitrationStateMachine.this.mHwWifiBoost.stopGameBoost(this.uid);
                                                    HwArbitrationStateMachine.this.mHwWifiBoost.stopStreamingBoost(this.uid);
                                                }
                                                HwArbitrationDisplay.getInstance(HwArbitrationStateMachine.this.mContext).requestDataMonitor(true, 1);
                                                HwArbitrationStateMachine.this.updateMplinkCHRExceptionEvent(this.uid, 5, 0);
                                                break;
                                            }
                                            break;
                                        case HwArbitrationDEFS.MSG_MPLINK_BIND_FAIL:
                                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "MSG_MPLINK_BIND_FAIL");
                                            this.mplinkBindResultInfo = (MplinkBindResultInfo) message2.obj;
                                            boolean unused6 = HwArbitrationStateMachine.this.mIsMpLinkBinding = false;
                                            if (this.mplinkBindResultInfo != null) {
                                                this.uid = this.mplinkBindResultInfo.getUid();
                                                HwArbitrationStateMachine.this.printMap(this.uid);
                                                HwArbitrationStateMachine.this.setQueryTime(this.uid);
                                                HwArbitrationStateMachine.this.updateMplinkCHRExceptionEvent(this.uid, 5, 8);
                                            }
                                            HwAPPChrManager.getInstance().updateStatisInfo(this.appInfo, 12);
                                            break;
                                        case HwArbitrationDEFS.MSG_MPLINK_UNBIND_SUCCESS:
                                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "MSG_MPLINK_UNBIND_SUCCESS");
                                            this.mplinkBindResultInfo = (MplinkBindResultInfo) message2.obj;
                                            this.wifiGoodFlag = true;
                                            if (this.mplinkBindResultInfo != null) {
                                                this.uid = this.mplinkBindResultInfo.getUid();
                                                if (HwArbitrationStateMachine.this.isStreamScene(this.uid)) {
                                                    HwArbitrationStateMachine.this.removeMessages(HwArbitrationDEFS.MSG_HISTREAM_TRIGGER_MPPLINK_INTERNAL);
                                                    HwArbitrationStateMachine.this.sendMessageDelayed(HwArbitrationDEFS.MSG_HISTREAM_TRIGGER_MPPLINK_INTERNAL, HwArbitrationStateMachine.this.triggerMPlinkInternal);
                                                    boolean unused7 = HwArbitrationStateMachine.this.mHiStreamTriggerOrStopMplink = false;
                                                }
                                                HwArbitrationStateMachine.this.printMap(this.uid);
                                                int unused8 = HwArbitrationStateMachine.this.mMpLinkCount = HwArbitrationStateMachine.this.mMpLinkCount - 1;
                                                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "MSG_MPLINK_UNBIND_SUCCESS, mMpLinkCount is " + HwArbitrationStateMachine.this.mMpLinkCount);
                                                int network = getTargetNetwork(this.mplinkBindResultInfo.getNetwork(), false);
                                                if (HwArbitrationStateMachine.this.mIsMpLinkError) {
                                                    network = HwArbitrationCommonUtils.getActiveConnectType(HwArbitrationStateMachine.this.mContext);
                                                }
                                                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "MSG_MPLINK_UNBIND_SUCCESS, network is " + network);
                                                if (HwArbitrationStateMachine.this.updateStateInfoMap(this.mplinkBindResultInfo, network, false)) {
                                                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "stop MPLink success and update table success");
                                                }
                                                HwArbitrationStateMachine.this.sendMPLinkBroadcast(this.uid);
                                                HwArbitrationStateMachine.this.updateMplinkCHRExceptionEvent(this.uid, this.stopMplinkReason, 0);
                                                if (!(HwAPPQoEManager.getInstance() == null || HwArbitrationStateMachine.mHwArbitrationAppBoostMap == null || HwArbitrationStateMachine.mHwArbitrationAppBoostMap.get(Integer.valueOf(this.uid)) == null)) {
                                                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "MSG_MPLINK_UNBIND_SUCCESS, stopWifiLinkMonitor " + this.uid);
                                                    HwAPPQoEManager.getInstance().stopWifiLinkMonitor(this.uid, false);
                                                }
                                                stopMPLinkCoex(this.uid);
                                                break;
                                            }
                                            break;
                                        case HwArbitrationDEFS.MSG_MPLINK_UNBIND_FAIL:
                                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "MSG_MPLINK_UNBIND_FAIL");
                                            this.wifiGoodFlag = true;
                                            this.mplinkBindResultInfo = (MplinkBindResultInfo) message2.obj;
                                            if (this.mplinkBindResultInfo != null) {
                                                HwArbitrationStateMachine.this.setQueryTime(this.mplinkBindResultInfo.getUid());
                                                HwArbitrationStateMachine.this.updateMplinkCHRExceptionEvent(this.mplinkBindResultInfo.getUid(), this.stopMplinkReason, 9);
                                                break;
                                            }
                                            break;
                                        default:
                                            switch (i) {
                                                case HwArbitrationDEFS.MSG_QUERY_QOE_WM_TIMEOUT:
                                                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "check ChannelQoE and Wavemapping timeout");
                                                    HwArbitrationCallbackImpl.getInstance(HwArbitrationStateMachine.this.mContext).initJudgeBothChQoEAndWM();
                                                    if (HwArbitrationStateMachine.this.noAPPInMPLink()) {
                                                        stopMPLinkCoex(message2.arg1);
                                                    }
                                                    HwArbitrationStateMachine.this.updateMplinkCHRExceptionEvent(message2.arg1, 8, 7);
                                                    break;
                                                case HwArbitrationDEFS.MSG_APPQoE_WIFI_GOOD:
                                                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "MSG_APPQoE_WIFI_GOOD");
                                                    if (!HwArbitrationStateMachine.this.noAPPInMPLink()) {
                                                        if (!this.wifiGoodFlag) {
                                                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "processing MSG_APPQoE_WIFI_GOOD");
                                                            break;
                                                        } else {
                                                            this.uid = message2.arg1;
                                                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "trgPingPongWiFi_Good: " + this.trgPingPongWiFi_Good);
                                                            if (HwArbitrationStateMachine.mHwArbitrationAppBoostMap != null && HwArbitrationStateMachine.mHwArbitrationAppBoostMap.get(Integer.valueOf(this.uid)) != null && HwArbitrationStateMachine.this.isInMPLink(this.uid) && !this.trgPingPongWiFi_Good) {
                                                                this.wifiGoodFlag = false;
                                                                HwArbitrationStateMachine.this.printMap(this.uid);
                                                                this.stopMplinkReason = 4;
                                                                HwArbitrationAppBoostInfo myAABInfo = (HwArbitrationAppBoostInfo) HwArbitrationStateMachine.mHwArbitrationAppBoostMap.get(Integer.valueOf(this.uid));
                                                                if (!(myAABInfo == null || HwWaveMappingManager.getInstance(HwArbitrationStateMachine.this.mContext) == null || HwAPPQoEManager.getInstance() == null)) {
                                                                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "queryWaveMappingInfo4Back: " + this.uid);
                                                                    HwAPPQoEManager.getInstance().stopWifiLinkMonitor(this.uid, false);
                                                                    HwWaveMappingManager.getInstance(HwArbitrationStateMachine.this.mContext).queryWaveMappingInfo4Back(this.uid, myAABInfo.mAppID, myAABInfo.mSceneId, getTargetNetwork(myAABInfo.mNetwork, true));
                                                                    break;
                                                                }
                                                            } else {
                                                                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "MSG_APPQoE_WIFI_GOOD not allow: " + this.trgPingPongWiFi_Good);
                                                                break;
                                                            }
                                                        }
                                                    } else {
                                                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "there's no app in MPLink");
                                                        break;
                                                    }
                                                    break;
                                                case HwArbitrationDEFS.MSG_SET_PingPong_WiFi_Good_FALSE:
                                                    if (this.trgPingPongWiFi_Good) {
                                                        HwArbitrationCommonUtils.logE(HwArbitrationStateMachine.TAG, "MSG_SET_PingPong_WiFi_Good_FALSE");
                                                        this.trgPingPongWiFi_Good = false;
                                                        this.uid = message2.arg1;
                                                        if (!(HwAPPQoEManager.getInstance() == null || HwArbitrationStateMachine.mHwArbitrationAppBoostMap == null || HwArbitrationStateMachine.mHwArbitrationAppBoostMap.get(Integer.valueOf(this.uid)) == null || !HwArbitrationStateMachine.this.isInMPLink(this.uid) || ((HwArbitrationAppBoostInfo) HwArbitrationStateMachine.mHwArbitrationAppBoostMap.get(Integer.valueOf(this.uid))).getNetwork() != 801)) {
                                                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "MSG_SET_PingPong_WiFi_Good_FALSE, startWifiLinkMonitor: " + this.uid);
                                                            HwAPPQoEManager.getInstance().startWifiLinkMonitor(this.uid, ((HwArbitrationAppBoostInfo) HwArbitrationStateMachine.mHwArbitrationAppBoostMap.get(Integer.valueOf(this.uid))).getSceneId());
                                                            break;
                                                        }
                                                    }
                                                    break;
                                                case HwArbitrationDEFS.MSG_Stop_MPLink_By_Notification:
                                                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "MSG_Stop_MPLink_By_Notification");
                                                    boolean unused9 = HwArbitrationStateMachine.this.mDenyByNotification = true;
                                                    HwArbitrationStateMachine.this.sendMessage(HwArbitrationDEFS.MSG_MPLINK_ERROR);
                                                    this.mplinkErrorCode = 0;
                                                    HwArbitrationStateMachine.this.sendMessageDelayed(HwArbitrationDEFS.MSG_Recovery_Flag_By_Notification, 86400000);
                                                    break;
                                                default:
                                                    switch (i) {
                                                        case 8:
                                                            int callId = message2.arg1;
                                                            int mdefaultSubId = SubscriptionManager.getDefaultSubId();
                                                            if (HwArbitrationCommonUtils.isSlotIdValid(callId) && HwArbitrationCommonUtils.isSlotIdValid(mdefaultSubId) && callId != mdefaultSubId && (!HwArbitrationFunction.isDsDs3() || HwArbitrationCommonUtils.DEL_DEFAULT_LINK)) {
                                                                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "Vice SIM  is starting Calling and not DsDs3.X");
                                                                HwArbitrationStateMachine.this.sendMessage(HwArbitrationDEFS.MSG_MPLINK_ERROR);
                                                                break;
                                                            }
                                                        case 1005:
                                                            break;
                                                        case HwArbitrationDEFS.MSG_CELL_STATE_CONNECTED:
                                                        case HwArbitrationDEFS.MSG_MPLINK_ERROR:
                                                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "MSG_MPLINK_ERROR");
                                                            boolean unused10 = HwArbitrationStateMachine.this.mIsMpLinkBinding = true;
                                                            int unused11 = HwArbitrationStateMachine.this.mCoexCount = 0;
                                                            List<Integer> uidList = HwArbitrationStateMachine.this.getAppUIDInMPLink();
                                                            if (uidList != null && uidList.size() != 0) {
                                                                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "MSG_MPLINK_ERROR: app in MPLink");
                                                                if (this.mplinkErrorCode == 0) {
                                                                    this.stopMplinkReason = 9;
                                                                    this.mplinkErrorCode = -1;
                                                                } else {
                                                                    this.stopMplinkReason = 7;
                                                                }
                                                                int NList = uidList.size();
                                                                for (int i2 = 0; i2 < NList; i2++) {
                                                                    stopMPLinkAppBind(uidList.get(i2).intValue());
                                                                }
                                                                break;
                                                            } else {
                                                                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "MSG_MPLINK_ERROR: no app in MPLink");
                                                                HwArbitrationStateMachine.this.transitionTo(HwArbitrationStateMachine.this.mMPLinkStoppingState);
                                                                break;
                                                            }
                                                        case HwArbitrationDEFS.MSG_SCREEN_IS_TURNOFF:
                                                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "MSG_SCREEN_IS_TURNOFF");
                                                            if (!(HwArbitrationStateMachine.mHwArbitrationAppBoostMap == null || HwArbitrationStateMachine.mHwArbitrationAppBoostMap.size() == 0)) {
                                                                for (HwArbitrationAppBoostInfo boostInfo : HwArbitrationStateMachine.mHwArbitrationAppBoostMap.values()) {
                                                                    if (boostInfo != null && boostInfo.getIsMPlink()) {
                                                                        if (100105 != boostInfo.getSceneId()) {
                                                                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "stop MpLinkBind, uid:" + boostInfo.getBoostUID());
                                                                            stopMPLinkAppBind(boostInfo.getBoostUID());
                                                                        } else {
                                                                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "HiStream Audio : not stop MpLinkBind, uid:" + boostInfo.getBoostUID());
                                                                        }
                                                                    }
                                                                }
                                                                break;
                                                            }
                                                        case 1024:
                                                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "MSG_DATA_SUBID_CHANGE");
                                                            if (message2.arg1 != SubscriptionManager.getDefaultSubId()) {
                                                                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "DataSubId not equal to DefaultId");
                                                                HwArbitrationStateMachine.this.sendMessage(HwArbitrationDEFS.MSG_MPLINK_ERROR);
                                                                break;
                                                            }
                                                            break;
                                                        case HwArbitrationDEFS.MSG_STREAMING_VIDEO_BAD:
                                                        case HwArbitrationDEFS.MSG_STREAMING_AUDIO_BAD:
                                                            this.appInfo = (HwAPPStateInfo) message2.obj;
                                                            if (this.appInfo != null) {
                                                                if (!HwArbitrationStateMachine.this.isInMPLink(this.appInfo.mAppUID)) {
                                                                    if (1106 == message2.what && !HwArbitrationFunction.isInLTE(HwArbitrationStateMachine.this.mContext)) {
                                                                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "Phone is not in LTE");
                                                                        HwArbitrationStateMachine.this.mHwArbitrationChrImpl.updateRequestMplinkChr((HwAPPStateInfo) message2.obj, 1);
                                                                        break;
                                                                    } else {
                                                                        handleAppExpBad(this.appInfo, 3);
                                                                        break;
                                                                    }
                                                                } else {
                                                                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "isStreamScene:" + HwArbitrationStateMachine.this.isStreamScene(this.appInfo.mAppUID));
                                                                    handleAppExpBad(this.appInfo, 3);
                                                                    break;
                                                                }
                                                            }
                                                            break;
                                                        case HwArbitrationDEFS.MSG_QUERY_QOE_WM_INFO:
                                                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "MSG_QUERY_QOE_WM_INFO");
                                                            this.uid = message2.arg1;
                                                            if (HwArbitrationStateMachine.this.isInMPLink(this.uid) && System.currentTimeMillis() - HwArbitrationStateMachine.this.pingPongTMCell_Bad < HwArbitrationDEFS.DelayTimeMillisA) {
                                                                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "trigger ping pong in wifi monitor");
                                                                boolean unused12 = HwArbitrationStateMachine.this.trgPingPongCell_Bad = true;
                                                                HwArbitrationStateMachine.this.sendMessageDelayed(HwArbitrationDEFS.MSG_SET_PingPong_Cell_Bad_FALSE, HwArbitrationDEFS.DelayTimeMillisB);
                                                            }
                                                            startMPLinkBindCheck(this.uid);
                                                            break;
                                                        case HwArbitrationDEFS.MSG_WM_OUT_FOR_STOP_MPLINK:
                                                            this.uid = message2.arg1;
                                                            if (1 == message2.arg2 && HwArbitrationStateMachine.this.isInMPLink(this.uid)) {
                                                                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "MSG_WM_OUT_FOR_STOP_MPLINK, stopMPLinkAppBind " + this.uid);
                                                                this.pingPongTMWiFi_Good = System.currentTimeMillis();
                                                                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "MSG_WM_OUT_FOR_STOP_MPLINK time: " + this.pingPongTMWiFi_Good);
                                                                stopMPLinkAppBind(this.uid);
                                                                break;
                                                            } else {
                                                                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "MSG_WM_OUT_FOR_STOP_MPLINK, re-start WiFi monitor " + this.uid);
                                                                if (!(HwAPPQoEManager.getInstance() == null || HwArbitrationStateMachine.mHwArbitrationAppBoostMap == null || HwArbitrationStateMachine.mHwArbitrationAppBoostMap.get(Integer.valueOf(this.uid)) == null)) {
                                                                    HwAPPQoEManager.getInstance().startWifiLinkMonitor(this.uid, ((HwArbitrationAppBoostInfo) HwArbitrationStateMachine.mHwArbitrationAppBoostMap.get(Integer.valueOf(this.uid))).getSceneId());
                                                                    this.wifiGoodFlag = true;
                                                                    break;
                                                                }
                                                            }
                                                            break;
                                                        default:
                                                            return false;
                                                    }
                                                    break;
                                            }
                                    }
                            }
                            break;
                    }
            }
            return true;
        }

        private void handleAppExpBad(HwAPPStateInfo appInfo2, int type) {
            if (appInfo2 != null) {
                updateMapAppInfo(appInfo2, true);
                HwArbitrationStateMachine.this.printMap(appInfo2.mAppUID);
                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, appInfo2.toString());
                if (!HwArbitrationStateMachine.this.mIsMpLinkBinding) {
                    switch (type) {
                        case 2:
                            HwArbitrationStateMachine.this.sendMessage(HwArbitrationDEFS.MSG_QUERY_QOE_WM_INFO, appInfo2.mAppUID);
                            break;
                        case 3:
                            if (HwArbitrationFunction.isStreamingScene(appInfo2)) {
                                HwArbitrationStateMachine.this.sendMessage(HwArbitrationDEFS.MSG_QUERY_QOE_WM_INFO, appInfo2.mAppUID);
                                break;
                            }
                            break;
                    }
                } else {
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "MPLinkStarted is querying");
                    HwArbitrationStateMachine.this.mHwArbitrationChrImpl.updateRequestMplinkChr(appInfo2, 6);
                }
            }
        }

        private void updateMapAppInfo(HwAPPStateInfo appInfo2, boolean needCoexAdd) {
            if (appInfo2 != null) {
                if (HwArbitrationStateMachine.mHwArbitrationAppBoostMap != null && HwArbitrationStateMachine.mHwArbitrationAppBoostMap.get(Integer.valueOf(appInfo2.mAppUID)) != null) {
                    HwArbitrationAppBoostInfo mHwAAInfo = (HwArbitrationAppBoostInfo) HwArbitrationStateMachine.mHwArbitrationAppBoostMap.get(Integer.valueOf(appInfo2.mAppUID));
                    if (!mHwAAInfo.getIsCoex() && needCoexAdd) {
                        mHwAAInfo.setIsCoex(true);
                        int unused = HwArbitrationStateMachine.this.mCoexCount = HwArbitrationStateMachine.this.mCoexCount + 1;
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "updateMapAppInfo, mCoexCount is: " + HwArbitrationStateMachine.this.mCoexCount);
                    }
                    HwArbitrationStateMachine.this.setStateMachineHashMap(appInfo2, mHwAAInfo.getNetwork(), mHwAAInfo.getIsCoex(), mHwAAInfo.getIsMPlink(), mHwAAInfo.getSolution());
                } else if (HwArbitrationStateMachine.mHwArbitrationAppBoostMap == null || HwArbitrationStateMachine.mHwArbitrationAppBoostMap.get(Integer.valueOf(appInfo2.mAppUID)) == null) {
                    HwArbitrationStateMachine.this.setStateMachineHashMap(appInfo2, HwArbitrationStateMachine.this.getCurrentNetwork(HwArbitrationStateMachine.this.mContext, appInfo2.mAppUID), true, false, 0);
                    int unused2 = HwArbitrationStateMachine.this.mCoexCount = HwArbitrationStateMachine.this.mCoexCount + 1;
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "updateMapAppInfo, mCoexCount is: " + HwArbitrationStateMachine.this.mCoexCount);
                }
            }
        }

        private void startMPLinkBindCheck(int uid2) {
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "startMPLinkBindCheck,uid = " + uid2);
            HwArbitrationStateMachine.this.printMap(uid2);
            if (HwArbitrationStateMachine.mHwArbitrationAppBoostMap != null && HwArbitrationStateMachine.mHwArbitrationAppBoostMap.get(Integer.valueOf(uid2)) != null) {
                HwArbitrationAppBoostInfo myAABInfo = (HwArbitrationAppBoostInfo) HwArbitrationStateMachine.mHwArbitrationAppBoostMap.get(Integer.valueOf(uid2));
                if (!HwArbitrationStateMachine.this.isNeedChQoEquery(uid2)) {
                    HwArbitrationStateMachine.this.updateMplinkCHRExceptionEvent(uid2, 8, 3);
                } else if (HwAPPQoEManager.createHwAPPQoEManager(HwArbitrationStateMachine.this.mContext) != null && HwWaveMappingManager.getInstance(HwArbitrationStateMachine.this.mContext) != null) {
                    boolean unused = HwArbitrationStateMachine.this.mIsMpLinkBinding = true;
                    HwArbitrationStateMachine.this.sendMessageDelayed(HwArbitrationDEFS.MSG_QUERY_QOE_WM_TIMEOUT, uid2, MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
                    HwAPPQoEManager.createHwAPPQoEManager(HwArbitrationStateMachine.this.mContext).queryNetworkQuality(myAABInfo.mUID, myAABInfo.mSceneId, getTargetNetwork(myAABInfo.mNetwork, true), true);
                    HwWaveMappingManager.getInstance(HwArbitrationStateMachine.this.mContext).queryWaveMappingInfo(myAABInfo.mUID, myAABInfo.mAppID, myAABInfo.mSceneId, getTargetNetwork(myAABInfo.mNetwork, true));
                }
            }
        }

        private void startMPLinkAppBind(int uid2) {
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "startMPLinkAppBind:" + uid2);
            HwArbitrationStateMachine.this.printMap(uid2);
            HwMplinkManager.createInstance(HwArbitrationStateMachine.this.mContext).requestBindProcessToNetwork(HwArbitrationStateMachine.this.getTargetNetID(HwArbitrationStateMachine.this.getCurrentNetwork(HwArbitrationStateMachine.this.mContext, uid2)), uid2, HwArbitrationStateMachine.this.getMpLinkQuickSwitchConfiguration(uid2));
        }

        private void stopMPLinkAppBind(int uid2) {
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "stopMPLinkAppBind:" + uid2);
            HwMplinkManager.createInstance(HwArbitrationStateMachine.this.mContext).requestClearBindProcessToNetwork(HwArbitrationFunction.getNetworkID(HwArbitrationStateMachine.this.mContext, HwArbitrationStateMachine.this.getCurrentNetwork(HwArbitrationStateMachine.this.mContext, uid2)), uid2);
        }

        private void stopMPLinkCoex(int uid2) {
            if (!(HwArbitrationStateMachine.mHwArbitrationAppBoostMap == null || HwArbitrationStateMachine.mHwArbitrationAppBoostMap.get(Integer.valueOf(uid2)) == null || !((HwArbitrationAppBoostInfo) HwArbitrationStateMachine.mHwArbitrationAppBoostMap.get(Integer.valueOf(uid2))).getIsCoex())) {
                ((HwArbitrationAppBoostInfo) HwArbitrationStateMachine.mHwArbitrationAppBoostMap.get(Integer.valueOf(uid2))).setIsCoex(false);
                int unused = HwArbitrationStateMachine.this.mCoexCount = HwArbitrationStateMachine.this.mCoexCount - 1;
                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "stopMPLinkCoex, mCoexCount is: " + HwArbitrationStateMachine.this.mCoexCount);
            }
            if (HwArbitrationStateMachine.this.mMpLinkCount > 0 || HwArbitrationStateMachine.this.mCoexCount > 0) {
                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "other app in MPLink, keep COEX");
                return;
            }
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "no app in MPLink, stop COEX");
            HwArbitrationStateMachine.this.transitionTo(HwArbitrationStateMachine.this.mMPLinkStoppingState);
        }

        private int getTargetNetwork(int network, boolean flag) {
            if (!flag) {
                network = HwArbitrationFunction.getNetwork(HwArbitrationStateMachine.this.mContext, network);
            }
            if (800 == network) {
                return 801;
            }
            if (801 == network) {
                return 800;
            }
            return 802;
        }
    }

    class MPLinkStartingState extends State {
        HwAPPStateInfo appStateInfo;

        MPLinkStartingState() {
        }

        public void enter() {
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "Enter MPLinkStartingState");
            HwMplinkManager.createInstance(HwArbitrationStateMachine.this.mContext).requestWiFiAndCellCoexist(true);
            HwArbitrationStateMachine.this.sendMessageDelayed(HwArbitrationDEFS.MSG_MPLINK_COEXIST_TIMEOUT, MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
        }

        public void exit() {
            boolean unused = HwArbitrationStateMachine.this.mIsMpLinkError = false;
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "exit MPLinkStartingState");
            HwArbitrationStateMachine.this.removeMessages(HwArbitrationDEFS.MSG_MPLINK_COEXIST_TIMEOUT);
            this.appStateInfo = null;
        }

        public boolean processMessage(Message message) {
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "MPLinkStartingState Msg: " + message.what);
            int i = message.what;
            if (i == 6) {
                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "WiFi and Celluar coexist successful");
            } else if (i == 107 || i == 110) {
                HwAPPStateInfo appEndInfo = (HwAPPStateInfo) message.obj;
                if (!(appEndInfo == null || this.appStateInfo == null || appEndInfo.mAppUID != this.appStateInfo.mAppUID)) {
                    if (!HwArbitrationFunction.isStreamingScene(this.appStateInfo) || this.appStateInfo.mScenceId == appEndInfo.mScenceId) {
                        HwArbitrationStateMachine.this.mHwArbitrationChrImpl.updateRequestMplinkChr(this.appStateInfo, 10);
                        HwArbitrationStateMachine.this.transitionTo(HwArbitrationStateMachine.this.mMPLinkStoppingState);
                    } else {
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "appEnd SceneID is not the same as appState");
                    }
                }
            } else {
                if (i != 115) {
                    if (i == 1009) {
                        HwArbitrationStateMachine.this.transitionTo(HwArbitrationStateMachine.this.mMPLinkStoppingState);
                    } else if (!(i == 1106 || i == 1108)) {
                        if (i != 2028) {
                            switch (i) {
                                case 2003:
                                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "start coex successfully, MSG_MPLINK_COEX_MODE");
                                    HwArbitrationStateMachine.this.transitionTo(HwArbitrationStateMachine.this.mMPLinkStartedState);
                                    break;
                                case HwArbitrationDEFS.MSG_MPLINK_NONCOEX_MODE:
                                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "MPLinkStartingState start COEX error");
                                    HwArbitrationStateMachine.this.mHwArbitrationChrImpl.updateRequestMplinkChr(this.appStateInfo, 2);
                                    HwArbitrationStateMachine.this.transitionTo(HwArbitrationStateMachine.this.mMPLinkStoppingState);
                                    break;
                                default:
                                    return false;
                            }
                        } else {
                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "Coexist timeout");
                            HwArbitrationStateMachine.this.mHwArbitrationChrImpl.updateRequestMplinkChr(this.appStateInfo, 2);
                            HwArbitrationStateMachine.this.transitionTo(HwArbitrationStateMachine.this.mMPLinkStoppingState);
                        }
                    }
                }
                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "defer BAD MSG");
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
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "Enter MPLinkStoppingState");
            HwMplinkManager.createInstance(HwArbitrationStateMachine.this.mContext).requestWiFiAndCellCoexist(false);
            HwArbitrationStateMachine.this.sendMessageDelayed(HwArbitrationDEFS.MSG_MPLINK_COEXIST_TIMEOUT, MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
        }

        public void exit() {
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "Exit MPLinkStoppingState");
            boolean unused = HwArbitrationStateMachine.this.mIsMpLinkError = false;
            HwArbitrationStateMachine.this.removeMessages(HwArbitrationDEFS.MSG_MPLINK_COEXIST_TIMEOUT);
            HwArbitrationCallbackImpl.getInstance(HwArbitrationStateMachine.this.mContext).initJudgeBothChQoEAndWM();
        }

        public boolean processMessage(Message message) {
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "MPLinkStoppingState Msg: " + message.what);
            int i = message.what;
            if (i != 2011) {
                if (i != 2028) {
                    switch (i) {
                        case 2003:
                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "MSG_MPLINK_COEX_MODE: stop COEX error ");
                            HwArbitrationStateMachine.this.transitionTo(HwArbitrationStateMachine.this.mInitialState);
                            HwArbitrationStateMachine.this.deferMessage(HwArbitrationStateMachine.this.obtainMessage(HwArbitrationDEFS.MSG_MPLINK_STOP_COEX_SUCC));
                            break;
                        case HwArbitrationDEFS.MSG_MPLINK_NONCOEX_MODE:
                            HwArbitrationStateMachine.this.transitionTo(HwArbitrationStateMachine.this.mInitialState);
                            HwArbitrationStateMachine.this.deferMessage(HwArbitrationStateMachine.this.obtainMessage(HwArbitrationDEFS.MSG_MPLINK_STOP_COEX_SUCC));
                            break;
                        default:
                            return false;
                    }
                } else {
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "clear Coexist timeout");
                    HwArbitrationStateMachine.this.transitionTo(HwArbitrationStateMachine.this.mInitialState);
                    HwArbitrationStateMachine.this.deferMessage(HwArbitrationStateMachine.this.obtainMessage(HwArbitrationDEFS.MSG_MPLINK_ERROR_HANDLER));
                }
            }
            return true;
        }
    }

    class WifiMonitorState extends State {
        private HwAPPStateInfo appInfo;

        WifiMonitorState() {
        }

        public void enter() {
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "Enter WifiMonitorState");
        }

        public void exit() {
            resetPingPong();
            HwArbitrationStateMachine.this.removeMessages(HwArbitrationDEFS.MSG_SET_PingPong_Cell_Bad_FALSE);
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "Exit WifiMonitorState");
        }

        public boolean processMessage(Message message) {
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "WifiMonitorState Msg: " + message.what);
            int i = message.what;
            switch (i) {
                case 100:
                    this.appInfo = (HwAPPStateInfo) message.obj;
                    HwArbitrationStateMachine.this.handleGeneralGameStart(this.appInfo);
                    break;
                case 101:
                    this.appInfo = (HwAPPStateInfo) message.obj;
                    HwArbitrationStateMachine.this.handleGamePvpEnd(this.appInfo);
                    HwArbitrationStateMachine.this.handleGeneralGameEnd(this.appInfo);
                    break;
                default:
                    switch (i) {
                        case 103:
                            break;
                        case 104:
                            this.appInfo = (HwAPPStateInfo) message.obj;
                            if (this.appInfo != null) {
                                HwArbitrationStateMachine.this.handleGeneralGameStart(this.appInfo);
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
                            resetPingPong();
                            break;
                        default:
                            switch (i) {
                                case 109:
                                    break;
                                case 110:
                                    resetPingPong();
                                    break;
                                case 111:
                                case 113:
                                    startMPLinkCoex(message, false);
                                    break;
                                case 112:
                                case 114:
                                    resetPingPong();
                                    break;
                                case 115:
                                    startMPLinkCoex(message, true);
                                    break;
                                default:
                                    switch (i) {
                                        case 1005:
                                            break;
                                        case HwArbitrationDEFS.MSG_WIFI_STATE_DISCONNECT:
                                            HwArbitrationStateMachine.this.mHwWifiBoost.stopAllBoost();
                                            HwArbitrationStateMachine.this.transitionTo(HwArbitrationStateMachine.this.mInitialState);
                                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "WifiMonitor transitionTo InitialState");
                                            break;
                                        default:
                                            switch (i) {
                                                case HwArbitrationDEFS.MSG_CELL_STATE_CONNECTED:
                                                    HwArbitrationStateMachine.this.mHwWifiBoost.stopAllBoost();
                                                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "WifiMonitorState transitionTo CellmonitorState");
                                                    HwArbitrationStateMachine.this.transitionTo(HwArbitrationStateMachine.this.mCellMonitorState);
                                                    break;
                                                case HwArbitrationDEFS.MSG_CELL_STATE_DISCONNECT:
                                                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "in WifiMonitorState don't process MSG_CELL_STATE_DISCONNECTED");
                                                    break;
                                                default:
                                                    switch (i) {
                                                        case HwArbitrationDEFS.MSG_SCREEN_IS_TURNOFF:
                                                            if (HwArbitrationFunction.isPvpScene()) {
                                                                HwArbitrationStateMachine.this.mHwWifiBoost.setPMMode(3);
                                                                HwArbitrationStateMachine.this.mHwWifiBoost.setPMMode(7);
                                                                HwArbitrationStateMachine.this.mHwWifiBoost.limitedSpeed(1, 0, 0);
                                                                break;
                                                            }
                                                            break;
                                                        case HwArbitrationDEFS.MSG_SCREEN_IS_ON:
                                                            if (HwArbitrationFunction.isPvpScene()) {
                                                                HwArbitrationStateMachine.this.mHwWifiBoost.setPMMode(6);
                                                                HwArbitrationStateMachine.this.mHwWifiBoost.setPMMode(4);
                                                                HwArbitrationStateMachine.this.mHwWifiBoost.limitedSpeed(1, 1, 1);
                                                                break;
                                                            }
                                                            break;
                                                        case HwArbitrationDEFS.MSG_STREAMING_VIDEO_BAD:
                                                            if (!HwArbitrationFunction.isInLTE(HwArbitrationStateMachine.this.mContext)) {
                                                                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "Phone is not in LTE");
                                                                HwArbitrationStateMachine.this.mHwArbitrationChrImpl.updateRequestMplinkChr((HwAPPStateInfo) message.obj, 1);
                                                                break;
                                                            }
                                                        case HwArbitrationDEFS.MSG_STREAMING_AUDIO_BAD:
                                                            if (!HwArbitrationStateMachine.this.mHiStreamTriggerOrStopMplink) {
                                                                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "avoid Ping-pong switching,not trigger mplink");
                                                                HwArbitrationStateMachine.this.mHwArbitrationChrImpl.updateRequestMplinkChr((HwAPPStateInfo) message.obj, 3);
                                                                break;
                                                            } else {
                                                                startMPLinkCoex(message, true);
                                                                break;
                                                            }
                                                        case 2003:
                                                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "mplink duplicate callback response");
                                                            break;
                                                        case HwArbitrationDEFS.MSG_SET_PingPong_Cell_Bad_FALSE:
                                                            HwArbitrationCommonUtils.logE(HwArbitrationStateMachine.TAG, "MSG_SET_PingPong_Cell_Bad_FALSE in wifi monitor");
                                                            resetPingPong();
                                                            break;
                                                        default:
                                                            return false;
                                                    }
                                                    break;
                                            }
                                    }
                            }
                    }
                    this.appInfo = (HwAPPStateInfo) message.obj;
                    HwArbitrationStateMachine.this.handleGamePvpEnd(this.appInfo);
                    HwArbitrationStateMachine.this.handleGeneralGameEnd(this.appInfo);
                    break;
            }
            return true;
        }

        private void resetPingPong() {
            HwArbitrationCommonUtils.logE(HwArbitrationStateMachine.TAG, "resetPingPong in wifi monitor");
            boolean unused = HwArbitrationStateMachine.this.trgPingPongCell_Bad = false;
            long unused2 = HwArbitrationStateMachine.this.pingPongTMCell_Bad = 0;
        }

        private void startMPLinkCoex(Message message, boolean needDefer) {
            HwAPPStateInfo appInfo2 = (HwAPPStateInfo) message.obj;
            int uid = Integer.MIN_VALUE;
            if (appInfo2 != null) {
                uid = appInfo2.mAppUID;
                if (!HwArbitrationStateMachine.this.isNeedChQoEquery(uid)) {
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "not allow ChannelQoE and WM query in WiFiMonitor");
                    HwAPPChrManager.getInstance().updateStatisInfo(appInfo2, 7);
                    HwArbitrationStateMachine.this.mHwArbitrationChrImpl.updateRequestMplinkChr((HwAPPStateInfo) message.obj, 3);
                    return;
                }
            }
            if (HwArbitrationStateMachine.this.trgPingPongCell_Bad) {
                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "not start MPLink, due to trgPingPong in Cell Bad =============");
            } else if (HwArbitrationStateMachine.this.mDenyByNotification) {
                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "not start MPLink, due to deny by Notification in next 24h");
                HwArbitrationStateMachine.this.mHwArbitrationChrImpl.updateRequestMplinkChr((HwAPPStateInfo) message.obj, 20);
            } else {
                if (HwArbitrationFunction.isAllowMpLink(HwArbitrationStateMachine.this.mContext, uid)) {
                    if (needDefer && message.obj != null) {
                        HwArbitrationStateMachine.this.deferMessage(message);
                    }
                    HwArbitrationStateMachine.this.transitionTo(HwArbitrationStateMachine.this.mMPLinkStartingState);
                } else {
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, "not allow MpLink");
                    HwAPPChrManager.getInstance().updateStatisInfo(appInfo2, 7);
                    HwArbitrationStateMachine.this.mHwArbitrationChrImpl.updateRequestMplinkChr((HwAPPStateInfo) message.obj, 1);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void setStateMachineHashMap(HwAPPStateInfo appInfo, int network, boolean isCoex, boolean isMPLink, int solution) {
        if (appInfo != null) {
            setStateMachineHashMap(appInfo.mAppId, appInfo.mAppUID, appInfo.mScenceId, network, isCoex, isMPLink, solution, appInfo);
        }
    }

    private void setStateMachineHashMap(int AppID, int UID, int sceneId, int network, boolean isCoex, boolean isMPLink, int solution, HwAPPStateInfo appInfo) {
        if (mHwArbitrationAppBoostMap == null) {
            mHwArbitrationAppBoostMap = new ConcurrentHashMap<>();
        }
        HwArbitrationAppBoostInfo hwArbitrationAppBoostInfo = new HwArbitrationAppBoostInfo(AppID, UID, sceneId, network, isCoex, isMPLink, solution);
        hwArbitrationAppBoostInfo.setHwAPPStateInfo(appInfo);
        StringBuilder sb = new StringBuilder();
        sb.append("set Map, UID is ");
        int i = UID;
        sb.append(i);
        sb.append(" BoostInfo is ");
        sb.append(hwArbitrationAppBoostInfo.toString());
        HwArbitrationCommonUtils.logD(TAG, sb.toString());
        mHwArbitrationAppBoostMap.put(Integer.valueOf(i), hwArbitrationAppBoostInfo);
    }

    /* access modifiers changed from: private */
    public void updateStateMachineHashMap(HwAPPStateInfo appInfo, int network, boolean isCoex, boolean isMPLink, int solution) {
        if (mHwArbitrationAppBoostMap == null || mHwArbitrationAppBoostMap.get(Integer.valueOf(appInfo.mAppUID)) == null) {
            setStateMachineHashMap(appInfo, network, isCoex, isMPLink, solution);
            return;
        }
        HwArbitrationAppBoostInfo boostInfo = mHwArbitrationAppBoostMap.get(Integer.valueOf(appInfo.mAppUID));
        int newSolution = boostInfo.getSolution() | solution;
        HwArbitrationCommonUtils.logD(TAG, "setStateMachineHashMap: old solution is    " + boostInfo.getSolution() + ",New solution is  " + newSolution);
        boostInfo.setSolution(newSolution);
        mHwArbitrationAppBoostMap.put(Integer.valueOf(appInfo.mAppUID), boostInfo);
    }

    /* access modifiers changed from: private */
    public void printMap(int uid) {
        if (mHwArbitrationAppBoostMap == null) {
            HwArbitrationCommonUtils.logD(TAG, "Map is null");
        } else if (mHwArbitrationAppBoostMap.get(Integer.valueOf(uid)) == null) {
            HwArbitrationCommonUtils.logD(TAG, "MapInfo is null, uid is: " + uid);
        } else {
            HwArbitrationCommonUtils.logD(TAG, "uid is " + uid + " BoostInfo is " + mHwArbitrationAppBoostMap.get(Integer.valueOf(uid)).toString());
        }
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
    }

    /* access modifiers changed from: private */
    public void handleGeneralGameStart(HwAPPStateInfo appInfo) {
        if (appInfo != null) {
            String appName = this.mContext.getPackageManager().getNameForUid(appInfo.mAppUID);
            if (800 == getCurrentNetwork(this.mContext, appInfo.mAppUID) && appName != null && 9 == this.mAppTypeRecoManager.getAppType(appName)) {
                this.mHwWifiBoost.setPMMode(6);
                this.mHwWifiBoost.pauseABSHandover();
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleGeneralGameEnd(HwAPPStateInfo appInfo) {
        if (appInfo != null && 800 == getCurrentNetwork(this.mContext, appInfo.mAppUID)) {
            this.mHwWifiBoost.setPMMode(7);
            this.mHwWifiBoost.restartABSHandover();
        }
    }

    /* access modifiers changed from: private */
    public void handleGamePvpStart(HwAPPStateInfo appInfo) {
        if (appInfo != null && 800 == getCurrentNetwork(this.mContext, appInfo.mAppUID)) {
            this.mHwWifiBoost.startGameBoost(appInfo.mAppUID);
        }
    }

    /* access modifiers changed from: private */
    public void handleGamePvpEnd(HwAPPStateInfo appInfo) {
        if (appInfo != null && 800 == getCurrentNetwork(this.mContext, appInfo.mAppUID)) {
            this.mHwWifiBoost.stopGameBoost(appInfo.mAppUID);
        }
    }

    /* access modifiers changed from: private */
    public void handleStreamingStart(HwAPPStateInfo appInfo) {
        if (appInfo != null && 800 == getCurrentNetwork(this.mContext, appInfo.mAppUID)) {
            this.mHwWifiBoost.startStreamingBoost(appInfo.mAppUID);
        }
    }

    /* access modifiers changed from: private */
    public void handleStreamingEnd(HwAPPStateInfo appInfo) {
        if (appInfo != null && 800 == getCurrentNetwork(this.mContext, appInfo.mAppUID)) {
            this.mHwWifiBoost.stopStreamingBoost(appInfo.mAppUID);
        }
    }

    /* access modifiers changed from: private */
    public boolean updateStateInfoMap(MplinkBindResultInfo result, int network, boolean isMPLink) {
        int uid = result.getUid();
        if (mHwArbitrationAppBoostMap == null || mHwArbitrationAppBoostMap.get(Integer.valueOf(uid)) == null) {
            return false;
        }
        HwArbitrationAppBoostInfo gmsInfo = mHwArbitrationAppBoostMap.get(Integer.valueOf(uid));
        gmsInfo.setIsMPLink(isMPLink);
        gmsInfo.setNetwork(network);
        mHwArbitrationAppBoostMap.put(Integer.valueOf(uid), gmsInfo);
        HwArbitrationCommonUtils.logD(TAG, "MplinkBindResultInfo " + result.toString());
        HwArbitrationCommonUtils.logD(TAG, "updateStateInfoMap Map");
        printMap(uid);
        return true;
    }

    public boolean isInMPLink(int uid) {
        if (mHwArbitrationAppBoostMap == null || mHwArbitrationAppBoostMap.get(Integer.valueOf(uid)) == null) {
            return false;
        }
        return mHwArbitrationAppBoostMap.get(Integer.valueOf(uid)).getIsMPlink();
    }

    /* access modifiers changed from: private */
    public boolean isStreamScene(int uid) {
        boolean z = false;
        if (mHwArbitrationAppBoostMap == null || mHwArbitrationAppBoostMap.get(Integer.valueOf(uid)) == null) {
            return false;
        }
        int scene = mHwArbitrationAppBoostMap.get(Integer.valueOf(uid)).getSceneId();
        if (scene == 100501 || scene == 100106 || scene == 100105 || scene == 100701) {
            z = true;
        }
        return z;
    }

    public void sendMPLinkBroadcast(int uid) {
        if (mHwArbitrationAppBoostMap != null && mHwArbitrationAppBoostMap.get(Integer.valueOf(uid)) != null) {
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
        if (mHwArbitrationAppBoostMap != null) {
            HwArbitrationAppBoostInfo hwArbitrationAppBoostInfo = mHwArbitrationAppBoostMap.get(Integer.valueOf(uid));
            HwArbitrationAppBoostInfo boostInfo = hwArbitrationAppBoostInfo;
            if (hwArbitrationAppBoostInfo != null) {
                HwArbitrationCommonUtils.logD(TAG, "uid: " + uid + ", CurrentNetwork: " + boostInfo.getNetwork());
                return boostInfo.getNetwork();
            }
        }
        return HwArbitrationCommonUtils.getActiveConnectType(mContext2);
    }

    /* access modifiers changed from: private */
    public boolean isNeedChQoEquery(int uid) {
        if (this.mQueryTime == null || !this.mQueryTime.containsKey(Integer.valueOf(uid))) {
            return true;
        }
        long queryTime = this.mQueryTime.get(Integer.valueOf(uid)).longValue();
        long nowTime = SystemClock.elapsedRealtime();
        if (nowTime - queryTime > 20000) {
            HwArbitrationCommonUtils.logD(TAG, "isNeedChQoEquery: allow ChannelQoE and WM query");
            return true;
        }
        HwArbitrationCommonUtils.logD(TAG, "isNeedChQoEquery: not allow ChannelQoE and WM query, waiting " + ((20000 + queryTime) - nowTime) + " Milliseconds");
        return false;
    }

    /* access modifiers changed from: private */
    public MpLinkQuickSwitchConfiguration getMpLinkQuickSwitchConfiguration(int uid) {
        if (mHwArbitrationAppBoostMap == null || mHwArbitrationAppBoostMap.get(Integer.valueOf(uid)) == null || mHwArbitrationAppBoostMap.get(Integer.valueOf(uid)).getHwAPPStateInfo() == null) {
            return null;
        }
        return mHwArbitrationAppBoostMap.get(Integer.valueOf(uid)).getHwAPPStateInfo().getQuickSwitchConfiguration();
    }

    public boolean deliverErrorMPLinkCase() {
        if (this.mIsMpLinkError) {
            HwArbitrationCommonUtils.logD(TAG, "denyCoexStatus is processing the MPLink Error");
            return false;
        } else if (getCurrentState() == this.mMPLinkStartingState) {
            setFlagAtTimer(30000);
            HwArbitrationCommonUtils.logD(TAG, "deliverErrorMPLinkCase, goto InitialState");
            transitionTo(this.mMPLinkStoppingState);
            return true;
        } else if (getCurrentState() == this.mMPLinkStartedState) {
            setFlagAtTimer(30000);
            HwArbitrationCommonUtils.logD(TAG, "deliverErrorMPLinkCase in MPLinkStartedState");
            sendMessage(HwArbitrationDEFS.MSG_MPLINK_ERROR);
            return true;
        } else if (getCurrentState() != this.mMPLinkStoppingState) {
            return false;
        } else {
            setFlagAtTimer(30000);
            HwArbitrationCommonUtils.logD(TAG, "deliverErrorMPLinkCase in MPLinkStoppingState");
            return true;
        }
    }

    /* access modifiers changed from: private */
    public boolean noAPPInMPLink() {
        List<Integer> result = getAppUIDInMPLink();
        return result == null || result.isEmpty();
    }

    /* access modifiers changed from: private */
    public List<Integer> getAppUIDInMPLink() {
        if (mHwArbitrationAppBoostMap == null) {
            return null;
        }
        List<Integer> result = new ArrayList<>();
        for (Map.Entry entry : mHwArbitrationAppBoostMap.entrySet()) {
            HwArbitrationAppBoostInfo val = (HwArbitrationAppBoostInfo) entry.getValue();
            if (val != null && val.getIsMPlink()) {
                result.add((Integer) entry.getKey());
            }
        }
        return result;
    }

    private void setFlagAtTimer(int milliseconds) {
        this.mIsMpLinkError = true;
        if (milliseconds < 1) {
            milliseconds = 30000;
        }
        HwArbitrationCommonUtils.logD(TAG, "setFlagAtTimer 30S");
        removeMessages(2001);
        sendMessageDelayed(2001, (long) milliseconds);
    }

    /* access modifiers changed from: private */
    public void showToast(String string) {
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
            HwArbitrationCommonUtils.logD(TAG, "hiStreamAppState:" + this.hiStreamAppState);
        }
    }

    /* access modifiers changed from: private */
    public synchronized void updateMplinkCHRExceptionEvent(int appUid, int event, int reason) {
        if (mHwArbitrationAppBoostMap != null) {
            HwArbitrationAppBoostInfo hwArbitrationAppBoostInfo = mHwArbitrationAppBoostMap.get(Integer.valueOf(appUid));
            HwArbitrationAppBoostInfo boostInfo = hwArbitrationAppBoostInfo;
            if (hwArbitrationAppBoostInfo != null) {
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
    }

    /* access modifiers changed from: private */
    public void setQueryTime(int uid) {
        if (this.mQueryTime == null) {
            this.mQueryTime = new HashMap<>(10);
        }
        this.mQueryTime.put(Integer.valueOf(uid), Long.valueOf(SystemClock.elapsedRealtime()));
    }
}
