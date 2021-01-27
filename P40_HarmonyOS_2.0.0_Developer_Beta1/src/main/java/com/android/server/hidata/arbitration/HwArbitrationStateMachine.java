package com.android.server.hidata.arbitration;

import android.content.Context;
import android.os.Message;
import android.rms.iaware.AppTypeRecoManager;
import android.widget.Toast;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.hidata.appqoe.HwAppQoeManager;
import com.android.server.hidata.appqoe.HwAppQoeResourceManager;
import com.android.server.hidata.appqoe.HwAppStateInfo;
import com.android.server.hidata.hiradio.HwHiRadioBoost;
import com.android.server.hidata.hiradio.HwWifiBoost;
import com.android.server.hidata.histream.HwHiStreamManager;
import java.util.concurrent.ConcurrentHashMap;

public class HwArbitrationStateMachine extends StateMachine {
    private static final String TAG = "HiData_HwArbitrationStateMachine";
    private static HwArbitrationStateMachine sArbitrationStateMachine;
    private static ConcurrentHashMap<Integer, HwArbitrationAppBoostInfo> sHwArbitrationAppBoostMap;
    private AppTypeRecoManager mAppTypeRecoManager = null;
    private State mCellMonitorState = new CellMonitorState();
    private Context mContext;
    private HwAppStateInfo mCurrentStreamAppInfo;
    private State mDefaultState = new DefaultState();
    private boolean mDeviceBootCompleted = false;
    private int mHiStreamAppState = -1;
    private HwAppQoeManager mHwAppQoeManager = null;
    private HwAppQoeResourceManager mHwAppQoeResourceManager;
    private HwHiRadioBoost mHwHiRadioBoost;
    private HwHiStreamManager mHwHiStreamManager = null;
    private HwWifiBoost mHwWifiBoost = null;
    private State mInitialState = new InitialState();
    private State mWifiMonitorState = new WifiMonitorState();

    private HwArbitrationStateMachine(Context context) {
        super("HwArbitrationStateMachine");
        this.mContext = context;
        this.mHwWifiBoost = HwWifiBoost.getInstance(this.mContext);
        this.mHwHiRadioBoost = HwHiRadioBoost.createInstance(context);
        this.mAppTypeRecoManager = AppTypeRecoManager.getInstance();
        addState(this.mDefaultState);
        addState(this.mInitialState, this.mDefaultState);
        addState(this.mWifiMonitorState, this.mDefaultState);
        addState(this.mCellMonitorState, this.mDefaultState);
        setInitialState(this.mInitialState);
        start();
    }

    public static HwArbitrationStateMachine getInstance(Context context) {
        if (sArbitrationStateMachine == null) {
            sArbitrationStateMachine = new HwArbitrationStateMachine(context);
        }
        return sArbitrationStateMachine;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setStateMachineHashMap(HwAppStateInfo appInfo, int network, int solution) {
        if (appInfo != null) {
            if (sHwArbitrationAppBoostMap == null) {
                sHwArbitrationAppBoostMap = new ConcurrentHashMap<>();
            }
            HwArbitrationAppBoostInfo appBoostInfo = new HwArbitrationAppBoostInfo();
            appBoostInfo.setHwAppStateInfo(appInfo);
            appBoostInfo.setAppId(appInfo.mAppId);
            appBoostInfo.setBoostUid(appInfo.mAppUid);
            appBoostInfo.setSceneId(appInfo.mScenesId);
            appBoostInfo.setNetwork(network);
            appBoostInfo.setSolution(solution);
            HwArbitrationCommonUtils.logD(TAG, false, "setStateMachineHashMap, uid is %{public}d BoostInfo is %{public}s", Integer.valueOf(appInfo.mAppUid), appBoostInfo.toString());
            sHwArbitrationAppBoostMap.put(Integer.valueOf(appInfo.mAppUid), appBoostInfo);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateStateMachineHashMap(HwAppStateInfo appInfo, int network, int solution) {
        ConcurrentHashMap<Integer, HwArbitrationAppBoostInfo> concurrentHashMap = sHwArbitrationAppBoostMap;
        if (concurrentHashMap != null) {
            HwArbitrationCommonUtils.logE(TAG, false, "updateStateMachineHashMap, sHwArbitrationAppBoostMap is null", new Object[0]);
            return;
        }
        HwArbitrationAppBoostInfo boostInfo = concurrentHashMap.get(Integer.valueOf(appInfo.mAppUid));
        if (boostInfo == null) {
            setStateMachineHashMap(appInfo, network, solution);
        }
        int newSolution = boostInfo.getSolution() | solution;
        HwArbitrationCommonUtils.logD(TAG, false, "updateStateMachineHashMap, old solution is %{public}d,New solution is %{public}d", Integer.valueOf(boostInfo.getSolution()), Integer.valueOf(newSolution));
        boostInfo.setSolution(newSolution);
        boostInfo.setNetwork(network);
        boostInfo.setHwAppStateInfo(appInfo);
        sHwArbitrationAppBoostMap.put(Integer.valueOf(appInfo.mAppUid), boostInfo);
    }

    public void updateStateMachineHashMap(HwAppStateInfo appInfo) {
        ConcurrentHashMap<Integer, HwArbitrationAppBoostInfo> concurrentHashMap = sHwArbitrationAppBoostMap;
        if (concurrentHashMap == null) {
            HwArbitrationCommonUtils.logE(TAG, false, "sHwArbitrationAppBoostMap is null", new Object[0]);
            return;
        }
        HwArbitrationAppBoostInfo boostInfo = concurrentHashMap.get(Integer.valueOf(appInfo.mAppUid));
        if (boostInfo == null) {
            HwArbitrationCommonUtils.logE(TAG, false, "boostInfo is null", new Object[0]);
            return;
        }
        HwAppStateInfo appStateInfo = boostInfo.getHwAppStateInfo();
        if (appStateInfo == null) {
            HwArbitrationCommonUtils.logE(TAG, false, "appStateInfo is null", new Object[0]);
            return;
        }
        appInfo.setCheckedCellChannelQuality(appStateInfo.getCheckedCellChannelQuality());
        appInfo.setIsMpLinkEnteredFromCell(appStateInfo.getIsMpLinkEnteredFromCell());
        boostInfo.setHwAppStateInfo(appInfo);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void printMap(int uid) {
        ConcurrentHashMap<Integer, HwArbitrationAppBoostInfo> concurrentHashMap = sHwArbitrationAppBoostMap;
        if (concurrentHashMap == null) {
            HwArbitrationCommonUtils.logD(TAG, false, "printMap, sHwArbitrationAppBoostMap is null", new Object[0]);
            return;
        }
        HwArbitrationAppBoostInfo boostInfo = concurrentHashMap.get(Integer.valueOf(uid));
        if (boostInfo == null) {
            HwArbitrationCommonUtils.logD(TAG, false, "printMap, boostInfo is null, uid is: %{public}d", Integer.valueOf(uid));
        } else {
            HwArbitrationCommonUtils.logD(TAG, false, "uid is %{public}d boostInfo is %{public}s", Integer.valueOf(uid), boostInfo.toString());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isBoostInfoAvailable(HwAppStateInfo appInfo) {
        ConcurrentHashMap<Integer, HwArbitrationAppBoostInfo> concurrentHashMap;
        HwArbitrationAppBoostInfo boostInfo;
        if (appInfo == null || (concurrentHashMap = sHwArbitrationAppBoostMap) == null || concurrentHashMap.isEmpty() || (boostInfo = sHwArbitrationAppBoostMap.get(Integer.valueOf(appInfo.mAppUid))) == null || boostInfo.getHwAppStateInfo() == null) {
            return false;
        }
        return true;
    }

    public void notifyIpConfigCompleted() {
        sendMessage(HwArbitrationDefs.MSG_NOTIFY_IP_CONFIG_COMPLETED);
    }

    class DefaultState extends State {
        DefaultState() {
        }

        public void enter() {
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "Enter DefaultState", new Object[0]);
        }

        public void exit() {
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "Exit DefaultState", new Object[0]);
        }

        public boolean processMessage(Message message) {
            if (HwArbitrationStateMachine.this.getCurrentState() != null) {
                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "DefaultState Msg: %{public}d, received in %{public}s", Integer.valueOf(message.what), HwArbitrationStateMachine.this.getCurrentState().getName());
            }
            switch (message.what) {
                case 100:
                case 104:
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "DefaultState handle MSG_GAME_ENTER_PVP_BATTLE or MSG_GAME_STATE_START", new Object[0]);
                    break;
                case 105:
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "DefaultState handle MSG_GAME_EXIT_PVP_BATTLE", new Object[0]);
                    break;
                case 1005:
                    HwArbitrationStateMachine.this.deferMessage(message);
                    break;
                case 1006:
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "DefaultState handle MSG_WIFI_STATE_DISCONNECT", new Object[0]);
                    if (HwArbitrationCommonUtils.getActiveConnectType(HwArbitrationStateMachine.this.mContext) != 801 || HwArbitrationStateMachine.this.mCellMonitorState == HwArbitrationStateMachine.this.getCurrentState()) {
                        if (HwArbitrationStateMachine.this.getCurrentState() != HwArbitrationStateMachine.this.mInitialState && HwArbitrationCommonUtils.getActiveConnectType(HwArbitrationStateMachine.this.mContext) == 802) {
                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "DefaultState unhandled ACTIVE_CONNECT_IS_NONE", new Object[0]);
                            HwArbitrationStateMachine hwArbitrationStateMachine = HwArbitrationStateMachine.this;
                            hwArbitrationStateMachine.transitionTo(hwArbitrationStateMachine.mInitialState);
                            break;
                        }
                    } else {
                        HwArbitrationStateMachine hwArbitrationStateMachine2 = HwArbitrationStateMachine.this;
                        hwArbitrationStateMachine2.transitionTo(hwArbitrationStateMachine2.mCellMonitorState);
                        break;
                    }
                    break;
                case HwArbitrationDefs.MSG_CELL_STATE_DISCONNECT /* 1010 */:
                case HwArbitrationDefs.MSG_CLOSE_4G_OR_WCDMA /* 2022 */:
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "DefaultState handle MSG_CELL_STATE_DISCONNECTED", new Object[0]);
                    if (HwArbitrationCommonUtils.getActiveConnectType(HwArbitrationStateMachine.this.mContext) != 800) {
                        if (HwArbitrationCommonUtils.getActiveConnectType(HwArbitrationStateMachine.this.mContext) == 802 && HwArbitrationStateMachine.this.getCurrentState() != HwArbitrationStateMachine.this.mInitialState) {
                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "DefaultState unhandled ACTIVE_CONNECT_IS_NONE", new Object[0]);
                            HwArbitrationStateMachine hwArbitrationStateMachine3 = HwArbitrationStateMachine.this;
                            hwArbitrationStateMachine3.transitionTo(hwArbitrationStateMachine3.mInitialState);
                            break;
                        }
                    } else {
                        HwArbitrationStateMachine hwArbitrationStateMachine4 = HwArbitrationStateMachine.this;
                        hwArbitrationStateMachine4.transitionTo(hwArbitrationStateMachine4.mWifiMonitorState);
                        break;
                    }
                case HwArbitrationDefs.MSG_SCREEN_IS_TURN_OFF /* 1012 */:
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "screen is turn off", new Object[0]);
                    break;
                case HwArbitrationDefs.MSG_SCREEN_IS_ON /* 1017 */:
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "screen is turn on", new Object[0]);
                    break;
                case HwArbitrationDefs.MSG_DEVICE_BOOT_COMPLETED /* 1127 */:
                    HwArbitrationStateMachine.this.mDeviceBootCompleted = true;
                    HwArbitrationStateMachine.this.mHwHiRadioBoost.initCommBoosterManager();
                    break;
                case HwArbitrationDefs.MSG_WIFI_PLUS_ENABLE /* 2016 */:
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "DefaultState handle MSG_WIFI_PLUS_ENABLE", new Object[0]);
                    break;
                case HwArbitrationDefs.MSG_WIFI_PLUS_DISABLE /* 2018 */:
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "DefaultState handle MSG_WIFI_PLUS_DISABLE", new Object[0]);
                    break;
                case HwArbitrationDefs.MSG_AIRPLANE_MODE_ON /* 2020 */:
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "DefaultState handle MSG_AIRPLANE_MODE_ON ", new Object[0]);
                    getConnectType();
                    break;
                case HwArbitrationDefs.MSG_VPN_STATE_OPEN /* 2024 */:
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "vpn state is open", new Object[0]);
                    break;
                case 3000:
                    if (message.arg1 != 0) {
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "HIRADIO_4G_SWITCH_3G_FAILED", new Object[0]);
                        break;
                    } else {
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "HIRADIO_4G_SWITCH_3G_SUCCESS", new Object[0]);
                        HwArbitrationStateMachine hwArbitrationStateMachine5 = HwArbitrationStateMachine.this;
                        hwArbitrationStateMachine5.showToast(hwArbitrationStateMachine5.mContext.getString(33685811));
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

        private void getConnectType() {
            int activeConnectType = HwArbitrationCommonUtils.getActiveConnectType(HwArbitrationStateMachine.this.mContext);
            if (activeConnectType != 800) {
                if (activeConnectType != 801) {
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "cannot distinguish state, Enter InitialState", new Object[0]);
                    if (HwArbitrationStateMachine.this.mInitialState != HwArbitrationStateMachine.this.getCurrentState()) {
                        HwArbitrationStateMachine hwArbitrationStateMachine = HwArbitrationStateMachine.this;
                        hwArbitrationStateMachine.transitionTo(hwArbitrationStateMachine.mInitialState);
                    }
                } else if (HwArbitrationStateMachine.this.mCellMonitorState != HwArbitrationStateMachine.this.getCurrentState()) {
                    HwArbitrationStateMachine hwArbitrationStateMachine2 = HwArbitrationStateMachine.this;
                    hwArbitrationStateMachine2.transitionTo(hwArbitrationStateMachine2.mCellMonitorState);
                }
            } else if (HwArbitrationStateMachine.this.mWifiMonitorState != HwArbitrationStateMachine.this.getCurrentState()) {
                HwArbitrationStateMachine hwArbitrationStateMachine3 = HwArbitrationStateMachine.this;
                hwArbitrationStateMachine3.transitionTo(hwArbitrationStateMachine3.mWifiMonitorState);
            }
        }
    }

    class InitialState extends State {
        InitialState() {
        }

        public void enter() {
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "Enter InitialState", new Object[0]);
            if (HwArbitrationStateMachine.sHwArbitrationAppBoostMap != null) {
                HwArbitrationStateMachine.sHwArbitrationAppBoostMap.clear();
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
            } else if (HwArbitrationStateMachine.this.getCurrentState() != null) {
                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "Unhandled Message: %{public}d in State: : %{public}s", Integer.valueOf(message.what), HwArbitrationStateMachine.this.getCurrentState().getName());
            }
            return true;
        }
    }

    class WifiMonitorState extends State {
        private HwAppStateInfo appInfo;

        WifiMonitorState() {
        }

        public void enter() {
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "Enter WifiMonitorState", new Object[0]);
            HwArbitrationStateMachine.this.updateNetworkTypeForArbitrationMap(800);
        }

        public void exit() {
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
                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "WifiMonitor transition to InitialState", new Object[0]);
                        } else if (i == 1009) {
                            HwArbitrationStateMachine.this.mHwWifiBoost.stopAllBoost();
                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "WifiMonitorState transition to CellMonitorState", new Object[0]);
                            HwArbitrationStateMachine hwArbitrationStateMachine2 = HwArbitrationStateMachine.this;
                            hwArbitrationStateMachine2.transitionTo(hwArbitrationStateMachine2.mCellMonitorState);
                        } else if (i == 1010) {
                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "WifiMonitorState unhandled MSG_CELL_STATE_DISCONNECTED", new Object[0]);
                        } else if (i != 1012) {
                            if (i != 1017) {
                                switch (i) {
                                    case 103:
                                        break;
                                    case 104:
                                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "WifiMonitorState handle MSG_GAME_ENTER_PVP_BATTLE", new Object[0]);
                                        this.appInfo = (HwAppStateInfo) message.obj;
                                        HwAppStateInfo hwAppStateInfo = this.appInfo;
                                        if (hwAppStateInfo != null) {
                                            HwArbitrationStateMachine.this.handleGeneralGameStart(hwAppStateInfo);
                                            HwArbitrationStateMachine.this.handleGamePvpStart(this.appInfo);
                                            break;
                                        }
                                        break;
                                    case 105:
                                        this.appInfo = (HwAppStateInfo) message.obj;
                                        HwArbitrationStateMachine.this.handleGamePvpEnd(this.appInfo);
                                        break;
                                    case 106:
                                        this.appInfo = (HwAppStateInfo) message.obj;
                                        HwArbitrationStateMachine.this.handleStreamingStart(this.appInfo);
                                        break;
                                    case 107:
                                        this.appInfo = (HwAppStateInfo) message.obj;
                                        HwArbitrationStateMachine.this.handleStreamingEnd(this.appInfo);
                                        break;
                                    default:
                                        return false;
                                }
                            } else if (HwArbitrationFunction.isPvpScene()) {
                                HwArbitrationStateMachine.this.mHwWifiBoost.setPmMode(6);
                                HwArbitrationStateMachine.this.mHwWifiBoost.setPmMode(4);
                                HwArbitrationStateMachine.this.mHwWifiBoost.setGameBoosting(true);
                                HwArbitrationStateMachine.this.mHwWifiBoost.limitedSpeed(1, 1, 1);
                            }
                        } else if (HwArbitrationFunction.isPvpScene()) {
                            HwArbitrationStateMachine.this.mHwWifiBoost.setPmMode(3);
                            HwArbitrationStateMachine.this.mHwWifiBoost.setGameBoosting(false);
                            HwArbitrationStateMachine.this.mHwWifiBoost.setPmMode(7);
                            HwArbitrationStateMachine.this.mHwWifiBoost.limitedSpeed(1, 0, 0);
                        }
                    }
                }
                this.appInfo = (HwAppStateInfo) message.obj;
                HwArbitrationStateMachine.this.handleGamePvpEnd(this.appInfo);
                HwArbitrationStateMachine.this.handleGeneralGameEnd(this.appInfo);
            } else {
                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "WifiMonitorState handle MSG_GAME_STATE_START", new Object[0]);
                this.appInfo = (HwAppStateInfo) message.obj;
                HwAppStateInfo hwAppStateInfo2 = this.appInfo;
                if (hwAppStateInfo2 != null) {
                    HwArbitrationStateMachine.this.handleGeneralGameStart(hwAppStateInfo2);
                    if (HwArbitrationCommonUtils.IS_HIDATA2_ENABLED) {
                        HwArbitrationStateMachine.this.setStateMachineHashMap(this.appInfo, 800, 0);
                    }
                }
            }
            return true;
        }
    }

    class CellMonitorState extends State {
        private int actions = 0;
        private HwAppStateInfo mAppInfo = null;

        CellMonitorState() {
        }

        public void enter() {
            String str;
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "Enter CellMonitorState", new Object[0]);
            HwArbitrationStateMachine.this.updateNetworkTypeForArbitrationMap(801);
            HwArbitrationStateMachine.this.mHwAppQoeManager = HwAppQoeManager.getInstance();
            this.mAppInfo = HwArbitrationStateMachine.this.mHwAppQoeManager == null ? null : HwArbitrationStateMachine.this.mHwAppQoeManager.getCurAppStateInfo();
            if (this.mAppInfo == null) {
                str = "mAppInfo is null ";
            } else {
                str = "mAppInfo:" + this.mAppInfo.toString();
            }
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, str, new Object[0]);
            if (HwArbitrationFunction.isDataTechSuitable() && enterMpLinkFromCell(this.mAppInfo)) {
                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "begin HiRadio Cellular boost", new Object[0]);
                handleCurrentScene(this.mAppInfo);
                handleHiStreamScene();
            }
        }

        private boolean enterMpLinkFromCell(HwAppStateInfo appInfo) {
            HwArbitrationAppBoostInfo boostInfo;
            HwAppStateInfo appStateInfo;
            if (HwArbitrationStateMachine.this.isBoostInfoAvailable(appInfo) && (boostInfo = (HwArbitrationAppBoostInfo) HwArbitrationStateMachine.sHwArbitrationAppBoostMap.get(Integer.valueOf(appInfo.mAppUid))) != null && (appStateInfo = boostInfo.getHwAppStateInfo()) != null && appStateInfo.getIsMpLinkEnteredFromCell() == 1) {
                return false;
            }
            return true;
        }

        private void handleCurrentScene(HwAppStateInfo appInfo) {
            if (appInfo != null && appInfo.mAppUid != -1) {
                int i = appInfo.mAppType;
                if (i == 1000) {
                    HwArbitrationStateMachine.this.sendMessage(109, appInfo);
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "CurrentScene: MSG_INSTANT_APP_START", new Object[0]);
                } else if (i != 2000) {
                    if (i != 4000) {
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "CurrentScene: no monitored scenes at foreground", new Object[0]);
                    } else {
                        HwArbitrationStateMachine.this.sendMessage(106, appInfo);
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "CurrentScene: MSG_STREAM_APP_START", new Object[0]);
                    }
                } else if (appInfo.mScenesId == 200002) {
                    HwArbitrationStateMachine.this.sendMessage(104, appInfo);
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "CurrentScene: MSG_GAME_ENTER_PVP_BATTLE", new Object[0]);
                } else if (appInfo.mScenesId == 200001) {
                    HwArbitrationStateMachine.this.sendMessage(100, appInfo);
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "CurrentScene: MSG_GAME_STATE_START", new Object[0]);
                } else {
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "CurrentScene: others", new Object[0]);
                }
                HwArbitrationStateMachine.this.mHwHiRadioBoost.brainAppStateNotifyDSBooster(appInfo, 0, 0);
            }
        }

        private void handleHiStreamScene() {
            if (HwArbitrationStateMachine.this.mCurrentStreamAppInfo != null) {
                int streamActions = findActionsNeedStart(HwArbitrationStateMachine.this.mCurrentStreamAppInfo, getActionsConfig(HwArbitrationStateMachine.this.mCurrentStreamAppInfo));
                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "Action to start: %{public}d", Integer.valueOf(streamActions));
                if (streamActions > 0) {
                    HwArbitrationStateMachine hwArbitrationStateMachine = HwArbitrationStateMachine.this;
                    hwArbitrationStateMachine.setStateMachineHashMap(hwArbitrationStateMachine.mCurrentStreamAppInfo, 801, streamActions);
                }
                HwArbitrationStateMachine.this.mHwHiStreamManager = HwHiStreamManager.getInstance();
                HwAppStateInfo appInfo = HwArbitrationStateMachine.this.mHwHiStreamManager == null ? null : HwArbitrationStateMachine.this.mHwHiStreamManager.getCurStreamAppInfo();
                if (appInfo != null && appInfo.mAppUid != -1) {
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "AppStreamInfo: %{public}s", appInfo.toString());
                    HwArbitrationStateMachine.this.mHwHiRadioBoost.brainAppStateNotifyDSBooster(appInfo, 0, HwArbitrationStateMachine.this.mHiStreamAppState);
                }
            }
        }

        private void stopAllCellOptimize() {
            if (HwArbitrationStateMachine.sHwArbitrationAppBoostMap == null || HwArbitrationStateMachine.sHwArbitrationAppBoostMap.isEmpty()) {
                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "stopAllCellOptimize: no Optimize to recover", new Object[0]);
                return;
            }
            for (Integer appUid : HwArbitrationStateMachine.sHwArbitrationAppBoostMap.keySet()) {
                HwAppStateInfo tempData = new HwAppStateInfo();
                tempData.mAppUid = appUid.intValue();
                stopUidOptimization(tempData);
            }
        }

        private void stopUidOptimization(HwAppStateInfo appInfo) {
            HwArbitrationAppBoostInfo boostInfo;
            if (appInfo != null && HwArbitrationStateMachine.sHwArbitrationAppBoostMap != null && !HwArbitrationStateMachine.sHwArbitrationAppBoostMap.isEmpty() && (boostInfo = (HwArbitrationAppBoostInfo) HwArbitrationStateMachine.sHwArbitrationAppBoostMap.get(Integer.valueOf(appInfo.mAppUid))) != null) {
                HwAppStateInfo tempData = new HwAppStateInfo();
                tempData.copyObjectValue(appInfo);
                tempData.mAppId = boostInfo.getAppId();
                tempData.mAppUid = boostInfo.getBoostUid();
                tempData.mScenesId = boostInfo.getSceneId();
                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "stopUidOptimization: for %{public}d, solution to stop is %{public}d", Integer.valueOf(tempData.mAppId), Integer.valueOf(boostInfo.getSolution()));
            }
        }

        private int findActionsNeedStart(HwAppStateInfo appInfo, int actions2) {
            HwArbitrationAppBoostInfo boostInfo;
            if (appInfo == null) {
                return -1;
            }
            if (actions2 <= 0) {
                return actions2;
            }
            if (HwArbitrationStateMachine.sHwArbitrationAppBoostMap == null || HwArbitrationStateMachine.sHwArbitrationAppBoostMap.isEmpty() || (boostInfo = (HwArbitrationAppBoostInfo) HwArbitrationStateMachine.sHwArbitrationAppBoostMap.get(Integer.valueOf(appInfo.mAppUid))) == null) {
                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "findActions: actions to start is %{public}d", Integer.valueOf(actions2));
                return actions2;
            }
            int lastActions = (~boostInfo.getSolution()) & actions2;
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "findActions: actions started is %{public}d, actions to judge is %{public}d, actions going to start is %{public}d", Integer.valueOf(boostInfo.getSolution()), Integer.valueOf(actions2), Integer.valueOf(lastActions));
            return lastActions;
        }

        private int findActionsNeedStop(HwAppStateInfo appInfo, int configActions) {
            if (configActions <= 0 || appInfo == null) {
                return configActions;
            }
            if (HwArbitrationStateMachine.sHwArbitrationAppBoostMap == null || HwArbitrationStateMachine.sHwArbitrationAppBoostMap.isEmpty()) {
                HwArbitrationCommonUtils.logE(HwArbitrationStateMachine.TAG, false, "findActionsNeedStop: no started actions for uid: %{public}d", Integer.valueOf(appInfo.mAppUid));
                return -1;
            }
            HwArbitrationAppBoostInfo boostInfo = (HwArbitrationAppBoostInfo) HwArbitrationStateMachine.sHwArbitrationAppBoostMap.get(Integer.valueOf(appInfo.mAppUid));
            if (boostInfo == null) {
                return -1;
            }
            int actionsStop = configActions & boostInfo.getSolution();
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "findActionsNeedStop: actions started is %{public}d, actions to judge is %{public}d, actions going to remain is %{public}d", Integer.valueOf(boostInfo.getSolution()), Integer.valueOf(configActions), Integer.valueOf(actionsStop));
            return (~actionsStop) & boostInfo.getSolution();
        }

        private int getActionsToStart(HwAppStateInfo appInfo) {
            return findActionsNeedStart(appInfo, getActionsConfig(appInfo));
        }

        private int getActionsConfig(HwAppStateInfo appInfo) {
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "Enter getActionsConfig", new Object[0]);
            HwArbitrationStateMachine.this.mHwAppQoeResourceManager = HwAppQoeResourceManager.getInstance();
            int scenesActions = 0;
            if (!(HwArbitrationStateMachine.this.mHwAppQoeResourceManager == null || appInfo == null)) {
                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, appInfo.toString(), new Object[0]);
                scenesActions = HwArbitrationStateMachine.this.mHwAppQoeResourceManager.getScenesAction(appInfo.mAppType, appInfo.mAppId, appInfo.mScenesId);
                if (scenesActions < 0) {
                    HwArbitrationCommonUtils.logE(HwArbitrationStateMachine.TAG, false, "get ScenesAction from AppQoe xml config failed", new Object[0]);
                }
            }
            return scenesActions;
        }

        public void exit() {
            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "Exit CellMonitorState", new Object[0]);
            stopAllCellOptimize();
        }

        public boolean processMessage(Message message) {
            int i = message.what;
            if (i != 100) {
                if (i != 101) {
                    if (i == 120) {
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "CellMonitor handle MSG_STREAMING_APP_FOREGROUND", new Object[0]);
                        this.mAppInfo = (HwAppStateInfo) message.obj;
                        HwAppStateInfo hwAppStateInfo = this.mAppInfo;
                        if (hwAppStateInfo != null && hwAppStateInfo.mScenesId == 100106 && HwArbitrationFunction.isDataTechSuitable()) {
                            int startActions = getActionsToStart(this.mAppInfo);
                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "Start HiStream actions:%{public}d", Integer.valueOf(startActions));
                            HwArbitrationStateMachine.this.setStateMachineHashMap(this.mAppInfo, 801, startActions);
                        }
                    } else if (i == 121) {
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "in CellMonitor: MSG_STREAMING_APP_BACKGROUND", new Object[0]);
                        this.mAppInfo = (HwAppStateInfo) message.obj;
                        HwAppStateInfo hwAppStateInfo2 = this.mAppInfo;
                        if (!(hwAppStateInfo2 == null || hwAppStateInfo2.mScenesId != 100106 || HwArbitrationStateMachine.sHwArbitrationAppBoostMap == null)) {
                            stopUidOptimization(this.mAppInfo);
                            HwArbitrationStateMachine.sHwArbitrationAppBoostMap.remove(this.mAppInfo);
                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "Stop HiStream actions", new Object[0]);
                        }
                    } else if (i != 1005) {
                        if (i != 1012) {
                            if (i == 1017) {
                                if (HwArbitrationStateMachine.this.mHwAppQoeManager = HwAppQoeManager.getInstance() != null) {
                                    HwAppStateInfo curAppStateInfo = HwArbitrationStateMachine.this.mHwAppQoeManager.getCurAppStateInfo();
                                    this.mAppInfo = curAppStateInfo;
                                    if (curAppStateInfo != null && HwArbitrationFunction.isDataTechSuitable()) {
                                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "%{public}s", this.mAppInfo.toString());
                                        handleCurrentScene(this.mAppInfo);
                                    }
                                }
                                handleHiStreamScene();
                            } else if (i == 2020) {
                                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "CellMonitorState handle MSG_AIRPLANE_MODE_ON", new Object[0]);
                                HwArbitrationStateMachine hwArbitrationStateMachine = HwArbitrationStateMachine.this;
                                hwArbitrationStateMachine.transitionTo(hwArbitrationStateMachine.mInitialState);
                            } else if (i == 1009) {
                                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "CellMonitorState unhandled MSG_CELL_STATE_CONNECTED", new Object[0]);
                            } else if (i == 1010) {
                                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "CellMonitorState handle MSG_CELL_STATE_DISCONNECT", new Object[0]);
                                HwArbitrationStateMachine hwArbitrationStateMachine2 = HwArbitrationStateMachine.this;
                                hwArbitrationStateMachine2.transitionTo(hwArbitrationStateMachine2.mInitialState);
                            } else if (i != 1022) {
                                if (i != 1023) {
                                    switch (i) {
                                        case 103:
                                            break;
                                        case 104:
                                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "CellMonitorState handle MSG_GAME_ENTER_PVP_BATTLE", new Object[0]);
                                            this.mAppInfo = (HwAppStateInfo) message.obj;
                                            HwAppStateInfo hwAppStateInfo3 = this.mAppInfo;
                                            if (hwAppStateInfo3 != null) {
                                                this.actions = getActionsConfig(hwAppStateInfo3);
                                                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "CellMonitorState enter PVP: getActionConfig: %{public}d, actions = %{public}d", Integer.valueOf(getActionsConfig(this.mAppInfo)), Integer.valueOf(this.actions));
                                                this.actions = findActionsNeedStart(this.mAppInfo, this.actions);
                                                HwArbitrationStateMachine.this.updateStateMachineHashMap(this.mAppInfo, 801, this.actions);
                                                break;
                                            }
                                            break;
                                        case 105:
                                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "CellMonitorState handle MSG_GAME_EXIT_PVP_BATTLE", new Object[0]);
                                            this.mAppInfo = (HwAppStateInfo) message.obj;
                                            if (!(this.mAppInfo == null || HwArbitrationStateMachine.sHwArbitrationAppBoostMap == null || HwArbitrationStateMachine.sHwArbitrationAppBoostMap.get(Integer.valueOf(this.mAppInfo.mAppUid)) == null)) {
                                                this.actions = getActionsConfig(this.mAppInfo);
                                                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "CellMonitorState exit PVP: getActionConfig:%{public}d", Integer.valueOf(this.actions));
                                                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "CellMonitorState stop action: %{public}d", Integer.valueOf(findActionsNeedStop(this.mAppInfo, this.actions)));
                                                HwArbitrationStateMachine.this.setStateMachineHashMap(this.mAppInfo, 801, this.actions);
                                                break;
                                            }
                                        case 106:
                                            this.mAppInfo = (HwAppStateInfo) message.obj;
                                            if (this.mAppInfo != null && HwArbitrationFunction.isDataTechSuitable()) {
                                                int startActions2 = getActionsToStart(this.mAppInfo);
                                                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "Start HiStream actions:%{public}d", Integer.valueOf(startActions2));
                                                HwArbitrationStateMachine.this.setStateMachineHashMap(this.mAppInfo, 801, startActions2);
                                                HwArbitrationStateMachine.this.printMap(this.mAppInfo.mAppUid);
                                                break;
                                            }
                                        case 107:
                                            this.mAppInfo = (HwAppStateInfo) message.obj;
                                            if (!(this.mAppInfo == null || HwArbitrationStateMachine.sHwArbitrationAppBoostMap == null)) {
                                                stopUidOptimization(this.mAppInfo);
                                                HwArbitrationStateMachine.sHwArbitrationAppBoostMap.remove(Integer.valueOf(this.mAppInfo.mAppUid));
                                                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "Stop HiStream actions", new Object[0]);
                                                break;
                                            }
                                        default:
                                            switch (i) {
                                                case 109:
                                                case 111:
                                                case 113:
                                                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "CellMonitorState need startOptimizeActionsForApp", new Object[0]);
                                                    this.mAppInfo = (HwAppStateInfo) message.obj;
                                                    HwAppStateInfo hwAppStateInfo4 = this.mAppInfo;
                                                    if (hwAppStateInfo4 != null) {
                                                        this.actions = getActionsToStart(hwAppStateInfo4);
                                                        if (this.actions > 0 && HwArbitrationFunction.isDataTechSuitable()) {
                                                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "begin HiRadioBoost, UID = %{public}d, actions =%{public}d", Integer.valueOf(this.mAppInfo.mAppUid), Integer.valueOf(this.actions));
                                                            HwArbitrationStateMachine.this.setStateMachineHashMap(this.mAppInfo, 801, this.actions);
                                                            HwArbitrationStateMachine.this.printMap(this.mAppInfo.mAppUid);
                                                            break;
                                                        }
                                                    }
                                                    break;
                                                case 110:
                                                case 112:
                                                case HwArbitrationDefs.MSG_INSTANT_TRAVEL_APP_END /* 114 */:
                                                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "CellMonitorState need stopOptimizedActionsForApp", new Object[0]);
                                                    this.mAppInfo = (HwAppStateInfo) message.obj;
                                                    if (!(this.mAppInfo == null || HwArbitrationStateMachine.sHwArbitrationAppBoostMap == null)) {
                                                        HwArbitrationAppBoostInfo boostInfo = (HwArbitrationAppBoostInfo) HwArbitrationStateMachine.sHwArbitrationAppBoostMap.get(Integer.valueOf(this.mAppInfo.mAppUid));
                                                        if (boostInfo != null && boostInfo.getSceneId() == 100105) {
                                                            HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "HiStream Audio is still at background", new Object[0]);
                                                            break;
                                                        } else {
                                                            stopUidOptimization(this.mAppInfo);
                                                            HwArbitrationStateMachine.sHwArbitrationAppBoostMap.remove(Integer.valueOf(this.mAppInfo.mAppUid));
                                                            break;
                                                        }
                                                    }
                                                    break;
                                                default:
                                                    return false;
                                            }
                                    }
                                }
                            } else if (HwArbitrationStateMachine.this.mHwAppQoeManager != null) {
                                this.mAppInfo = HwArbitrationStateMachine.this.mHwAppQoeManager.getCurAppStateInfo();
                                HwAppStateInfo hwAppStateInfo5 = this.mAppInfo;
                                if (hwAppStateInfo5 != null) {
                                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "AppInfo: %{public}s", hwAppStateInfo5.toString());
                                    this.actions = getActionsToStart(this.mAppInfo);
                                    HwArbitrationStateMachine.this.setStateMachineHashMap(this.mAppInfo, 801, this.actions);
                                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "Back to RAT suitable, start actions = %{public}d", Integer.valueOf(this.actions));
                                    handleHiStreamScene();
                                }
                            }
                        }
                        stopAllCellOptimize();
                    } else {
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "CellMonitorState transition to WifiMonitorState", new Object[0]);
                        HwArbitrationStateMachine hwArbitrationStateMachine3 = HwArbitrationStateMachine.this;
                        hwArbitrationStateMachine3.transitionTo(hwArbitrationStateMachine3.mWifiMonitorState);
                    }
                }
                this.mAppInfo = (HwAppStateInfo) message.obj;
                if (!(this.mAppInfo == null || HwArbitrationStateMachine.sHwArbitrationAppBoostMap == null)) {
                    stopUidOptimization(this.mAppInfo);
                    HwArbitrationStateMachine.sHwArbitrationAppBoostMap.remove(Integer.valueOf(this.mAppInfo.mAppUid));
                }
            } else {
                this.mAppInfo = (HwAppStateInfo) message.obj;
                HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "CellMonitorState handle MSG_GAME_STATE_START, mCurrentRAT is %{public}d", Integer.valueOf(HwArbitrationFunction.getDataTech()));
                HwAppStateInfo hwAppStateInfo6 = this.mAppInfo;
                if (hwAppStateInfo6 != null) {
                    this.actions = getActionsConfig(hwAppStateInfo6);
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMachine.TAG, false, "CellMonitorState enter GAME: actions = %{public}d", Integer.valueOf(this.actions));
                    this.actions = findActionsNeedStart(this.mAppInfo, this.actions);
                    HwArbitrationStateMachine.this.setStateMachineHashMap(this.mAppInfo, 801, this.actions);
                }
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleGeneralGameStart(HwAppStateInfo appInfo) {
        if (appInfo != null && this.mContext.getPackageManager() != null) {
            String appName = this.mContext.getPackageManager().getNameForUid(appInfo.mAppUid);
            if (getCurrentNetwork(this.mContext, appInfo.mAppUid) == 800 && appName != null && this.mAppTypeRecoManager.getAppType(appName) == 9) {
                this.mHwWifiBoost.setPmMode(6);
                this.mHwWifiBoost.pauseAbsHandover();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleGeneralGameEnd(HwAppStateInfo appInfo) {
        if (appInfo != null && getCurrentNetwork(this.mContext, appInfo.mAppUid) == 800) {
            this.mHwWifiBoost.setPmMode(7);
            this.mHwWifiBoost.restartAbsHandover();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleGamePvpStart(HwAppStateInfo appInfo) {
        if (appInfo != null && getCurrentNetwork(this.mContext, appInfo.mAppUid) == 800) {
            this.mHwWifiBoost.startGameBoost(appInfo.mAppUid);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleGamePvpEnd(HwAppStateInfo appInfo) {
        if (appInfo != null && getCurrentNetwork(this.mContext, appInfo.mAppUid) == 800) {
            this.mHwWifiBoost.stopGameBoost(appInfo.mAppUid);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleStreamingStart(HwAppStateInfo appInfo) {
        if (appInfo != null && getCurrentNetwork(this.mContext, appInfo.mAppUid) == 800 && isVoipStreamApp(appInfo.mScenesId)) {
            this.mHwWifiBoost.startStreamingBoost(appInfo.mAppUid);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleStreamingEnd(HwAppStateInfo appInfo) {
        if (appInfo != null && getCurrentNetwork(this.mContext, appInfo.mAppUid) == 800 && isVoipStreamApp(appInfo.mScenesId)) {
            this.mHwWifiBoost.stopStreamingBoost(appInfo.mAppUid);
        }
    }

    private boolean isVoipStreamApp(int scenesId) {
        if (scenesId == 100106 || scenesId == 100105 || scenesId == 101101) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateNetworkTypeForArbitrationMap(int networkType) {
        ConcurrentHashMap<Integer, HwArbitrationAppBoostInfo> concurrentHashMap = sHwArbitrationAppBoostMap;
        if (!(concurrentHashMap == null || concurrentHashMap.isEmpty())) {
            for (HwArbitrationAppBoostInfo boostInfo : sHwArbitrationAppBoostMap.values()) {
                if (boostInfo != null) {
                    boostInfo.setNetwork(networkType);
                }
            }
        }
    }

    public boolean isInMPLink(int uid) {
        HwArbitrationAppBoostInfo appBoostInfo;
        ConcurrentHashMap<Integer, HwArbitrationAppBoostInfo> concurrentHashMap = sHwArbitrationAppBoostMap;
        if (concurrentHashMap == null || (appBoostInfo = concurrentHashMap.get(Integer.valueOf(uid))) == null) {
            return false;
        }
        return appBoostInfo.isInMpLink();
    }

    public int getCurrentNetwork(Context mContext2, int uid) {
        HwArbitrationAppBoostInfo boostInfo;
        ConcurrentHashMap<Integer, HwArbitrationAppBoostInfo> concurrentHashMap = sHwArbitrationAppBoostMap;
        if (concurrentHashMap == null || (boostInfo = concurrentHashMap.get(Integer.valueOf(uid))) == null) {
            return HwArbitrationCommonUtils.getActiveConnectType(mContext2);
        }
        HwArbitrationCommonUtils.logD(TAG, false, "uid: %{public}d, CurrentNetwork: %{public}d", Integer.valueOf(uid), Integer.valueOf(boostInfo.getNetwork()));
        return boostInfo.getNetwork();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showToast(String string) {
        if (this.mDeviceBootCompleted) {
            Toast.makeText(this.mContext, string, 0).show();
        }
    }

    public void updateCurrentStreamAppState(HwAppStateInfo appInfo, int state) {
        if (appInfo != null) {
            if (state == 100 || state == 103) {
                this.mCurrentStreamAppInfo = appInfo;
                this.mHiStreamAppState = 0;
            } else if (state == 101) {
                this.mCurrentStreamAppInfo = null;
                this.mHiStreamAppState = 2;
            } else if (state == 104) {
                this.mHiStreamAppState = 1;
                if (appInfo.mScenesId == 100106) {
                    this.mCurrentStreamAppInfo = null;
                } else if (appInfo.mScenesId == 100105) {
                    this.mCurrentStreamAppInfo = appInfo;
                }
            } else {
                HwArbitrationCommonUtils.logD(TAG, false, "HiStreamAppState:%{public}d", Integer.valueOf(this.mHiStreamAppState));
            }
            HwArbitrationCommonUtils.logD(TAG, false, "HiStreamAppState:%{public}d", Integer.valueOf(this.mHiStreamAppState));
        }
    }
}
