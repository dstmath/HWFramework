package com.android.server.hidata.arbitration;

import android.content.Context;
import com.android.server.hidata.appqoe.HwAPPQoEManager;
import com.android.server.hidata.appqoe.HwAPPStateInfo;
import com.android.server.hidata.appqoe.IHwAPPQoECallback;
import com.android.server.hidata.hiradio.HwHiRadioBoost;
import com.android.server.hidata.hiradio.HwWifiBoost;
import com.android.server.hidata.hiradio.IHiRadioBoostCallback;
import com.android.server.hidata.histream.HwHistreamCHRQoeInfo;
import com.android.server.hidata.histream.IHwHistreamQoeCallback;
import com.android.server.hidata.wavemapping.IWaveMappingCallback;
import java.util.HashMap;

public class HwArbitrationCallbackImpl implements IHwAPPQoECallback, IWaveMappingCallback, IHwHistreamQoeCallback, IHiRadioBoostCallback {
    private static final String TAG = "HiData_HwArbitrationCallbackImpl";
    private static HwArbitrationCallbackImpl mHwArbitrationCallbackImpl;
    private int UID;
    private int checkFailReason = -1;
    private int hiStreamAudioStatus = 0;
    private int isFullInfo = 0;
    private boolean isTargetNetworkGood = true;
    private Context mContext;
    private HashMap<Integer, HwAPPStateInfo> mDelayMsgMap;
    private IGameCHRCallback mGameCHRCallback;
    private HwAPPStateInfo mGameInfo = null;
    private IGameReportCallback mGameReportCallback;
    private int mGameState;
    private HwArbitrationStateMachine mHwArbitrationStateMachine;
    private HwWifiBoost mHwWifiBoost = null;
    private HwAPPStateInfo mPreviousAppInfo;
    private boolean mWifiPlusFromBrain = false;
    private int network;

    public static HwArbitrationCallbackImpl getInstance(Context context) {
        if (mHwArbitrationCallbackImpl == null) {
            mHwArbitrationCallbackImpl = new HwArbitrationCallbackImpl(context);
        }
        return mHwArbitrationCallbackImpl;
    }

    public static HwArbitrationCallbackImpl getInstanceForChr() {
        return mHwArbitrationCallbackImpl;
    }

    public void registGameReport(IGameReportCallback callback) {
        this.mGameReportCallback = callback;
    }

    public void regisGameCHR(IGameCHRCallback callback) {
        this.mGameCHRCallback = callback;
    }

    private HwArbitrationCallbackImpl(Context context) {
        this.mContext = context;
        this.mHwArbitrationStateMachine = HwArbitrationStateMachine.getInstance(this.mContext);
        this.mHwWifiBoost = HwWifiBoost.getInstance(this.mContext);
        HwArbitrationCommonUtils.logD(TAG, false, "init HwArbitrationCallbackImpl completed!", new Object[0]);
    }

    @Override // com.android.server.hidata.appqoe.IHwAPPQoECallback
    public void onWifiLinkQuality(int uid, int scene, boolean isSatisfy) {
        HwArbitrationCommonUtils.logD(TAG, false, "uid: %{public}d scene: %{public}d isSatisfy: %{public}s", Integer.valueOf(uid), Integer.valueOf(scene), String.valueOf(isSatisfy));
        if (isSatisfy) {
            HwArbitrationCommonUtils.logD(TAG, false, "APPQoE detect that wifi is good", new Object[0]);
            this.mHwArbitrationStateMachine.sendMessage(HwArbitrationDEFS.MSG_APPQoE_WIFI_GOOD, uid);
        }
    }

    public HwAPPStateInfo getGameAppStateInfo() {
        return this.mGameInfo;
    }

    @Override // com.android.server.hidata.appqoe.IHwAPPQoECallback
    public void onAPPStateCallBack(HwAPPStateInfo appInfo, int state) {
        this.mGameInfo = appInfo;
        if (appInfo == null) {
            HwArbitrationCommonUtils.logD(TAG, false, "appInfo is null", new Object[0]);
            return;
        }
        this.mHwArbitrationStateMachine.updateStateMachineHashMap(appInfo);
        HwArbitrationCommonUtils.logD(TAG, false, "onAppStateCallBack: appInfo:%{public}s state:%{public}d", appInfo.toString(), Integer.valueOf(state));
        int i = appInfo.mAppType;
        if (i != 1000) {
            if (i != 2000 && i != 3000) {
                if (i == 4000) {
                    this.mHwArbitrationStateMachine.updateCurrentStreamAppState(appInfo, state);
                    switch (state) {
                        case 100:
                            if (HwArbitrationFunction.isStreamingScene(appInfo)) {
                                this.mHwArbitrationStateMachine.sendMessage(106, appInfo);
                                HwArbitrationCommonUtils.logD(TAG, false, "MSG_STREAMING_APP_START", new Object[0]);
                                this.hiStreamAudioStatus = 0;
                            }
                            if (HwAPPQoEManager.isAppStartMonitor(appInfo, this.mContext)) {
                                this.mWifiPlusFromBrain = true;
                            }
                            HwAppTimeDetail.getInstance().streamMediaStartTime(appInfo);
                            break;
                        case 101:
                            if (HwArbitrationFunction.isStreamingScene(appInfo)) {
                                this.mHwArbitrationStateMachine.sendMessage(107, appInfo);
                                HwArbitrationCommonUtils.logD(TAG, false, "MSG_STREAM_APP_END", new Object[0]);
                            }
                            this.mWifiPlusFromBrain = false;
                            HwAppTimeDetail.getInstance().streamMediaStopTime(appInfo);
                            break;
                        case 103:
                            if (HwArbitrationFunction.isStreamingScene(appInfo)) {
                                this.mHwArbitrationStateMachine.sendMessage(120, appInfo);
                                HwArbitrationCommonUtils.logD(TAG, false, "Callback:MSG_STREAMING_APP_FOREGROUND", new Object[0]);
                                this.hiStreamAudioStatus = 0;
                            }
                            if (HwAPPQoEManager.isAppStartMonitor(appInfo, this.mContext)) {
                                this.mWifiPlusFromBrain = true;
                                break;
                            }
                            break;
                        case 104:
                            if (HwArbitrationFunction.isStreamingScene(appInfo)) {
                                HwArbitrationCommonUtils.logD(TAG, false, "Callback:MSG_STREAMING_APP_BACKGROUND", new Object[0]);
                                this.mHwArbitrationStateMachine.sendMessage(121, appInfo);
                                this.hiStreamAudioStatus = 1;
                            }
                            this.mWifiPlusFromBrain = false;
                            break;
                    }
                }
            } else {
                switch (state) {
                    case 100:
                        if (appInfo.mScenceId == 200002) {
                            this.mHwArbitrationStateMachine.sendMessage(104, appInfo);
                            HwArbitrationCommonUtils.logD(TAG, false, "MSG_GAME_ENTER_PVP_BATTLE", new Object[0]);
                            HwArbitrationFunction.setPvpScene(true);
                            this.mGameState = 4;
                            updateGameState(appInfo, this.mGameState);
                        } else if (appInfo.mScenceId == 200001) {
                            this.mHwArbitrationStateMachine.sendMessage(100, appInfo);
                            HwArbitrationCommonUtils.logD(TAG, false, "MSG_GAME_STATE_START", new Object[0]);
                            HwArbitrationDisplay.getInstance(this.mContext).startSmpMonitor();
                        }
                        if (this.mGameReportCallback != null && appInfo.mAppType == 2000) {
                            this.mGameReportCallback.onReportGameState(true, isWiTASWhiteListApp(appInfo), appInfo.mAppId);
                        }
                        this.mHwWifiBoost.setHiLinkAccGameMode(true, appInfo.mAppUID);
                        break;
                    case 101:
                        this.mHwArbitrationStateMachine.sendMessage(101, appInfo);
                        HwArbitrationCommonUtils.logD(TAG, false, "MSG_GAME_STATE_END", new Object[0]);
                        HwArbitrationFunction.setPvpScene(false);
                        this.mGameState = 5;
                        updateGameState(appInfo, this.mGameState);
                        IGameReportCallback iGameReportCallback = this.mGameReportCallback;
                        if (iGameReportCallback != null) {
                            iGameReportCallback.onReportGameState(false, isWiTASWhiteListApp(appInfo), appInfo.mAppId);
                        }
                        this.mHwWifiBoost.setHiLinkAccGameMode(false, appInfo.mAppUID);
                        break;
                    case 102:
                        if (appInfo.mScenceId != 200002) {
                            if (appInfo.mScenceId == 200001) {
                                this.mHwArbitrationStateMachine.sendMessage(105, appInfo);
                                HwArbitrationCommonUtils.logD(TAG, false, "MSG_GAME_EXIT_PVP_BATTLE", new Object[0]);
                                HwArbitrationFunction.setPvpScene(false);
                                this.mGameState = 2;
                                updateGameState(appInfo, this.mGameState);
                                break;
                            }
                        } else {
                            this.mHwArbitrationStateMachine.sendMessage(104, appInfo);
                            HwArbitrationCommonUtils.logD(TAG, false, "MSG_GAME_ENTER_PVP_BATTLE", new Object[0]);
                            HwArbitrationFunction.setPvpScene(true);
                            this.mGameState = 1;
                            updateGameState(appInfo, this.mGameState);
                            IGameReportCallback iGameReportCallback2 = this.mGameReportCallback;
                            if (iGameReportCallback2 != null) {
                                iGameReportCallback2.onReportGameState(true, isWiTASWhiteListApp(appInfo), appInfo.mAppId);
                            }
                            this.mHwWifiBoost.setHiLinkAccGameMode(true, appInfo.mAppUID);
                            break;
                        }
                        break;
                    case 104:
                        this.mHwArbitrationStateMachine.sendMessage(103, appInfo);
                        HwArbitrationCommonUtils.logD(TAG, false, "MSG_GAME_STATE_BACKGROUND", new Object[0]);
                        if (appInfo.mScenceId == 200002) {
                            this.mGameState = 3;
                            updateGameState(appInfo, this.mGameState);
                        }
                        HwArbitrationFunction.setPvpScene(false);
                        IGameReportCallback iGameReportCallback3 = this.mGameReportCallback;
                        if (iGameReportCallback3 != null) {
                            iGameReportCallback3.onReportGameState(false, isWiTASWhiteListApp(appInfo), appInfo.mAppId);
                        }
                        this.mHwWifiBoost.setHiLinkAccGameMode(false, appInfo.mAppUID);
                        break;
                }
            }
        } else {
            switch (state) {
                case 100:
                case 102:
                    if (isPayScene(appInfo)) {
                        this.mHwArbitrationStateMachine.sendMessage(111, appInfo);
                        HwArbitrationCommonUtils.logD(TAG, false, "MSG_INSTANT_PAY_APP_START", new Object[0]);
                    } else if (isTravelScene(appInfo)) {
                        this.mHwArbitrationStateMachine.sendMessage(113, appInfo);
                        HwArbitrationCommonUtils.logD(TAG, false, "MSG_INSTANT_TRAVEL_APP_START", new Object[0]);
                    } else if (isPayEnd(appInfo, this.mPreviousAppInfo)) {
                        this.mHwArbitrationStateMachine.sendMessage(112, appInfo);
                        HwArbitrationCommonUtils.logD(TAG, false, "MSG_INSTANT_PAY_APP_END", new Object[0]);
                    } else if (isTravelEnd(appInfo, this.mPreviousAppInfo)) {
                        this.mHwArbitrationStateMachine.sendMessage(HwArbitrationDEFS.MSG_INSTANT_TRAVEL_APP_END, appInfo);
                        HwArbitrationCommonUtils.logD(TAG, false, "MSG_INSTANT_TRAVEL_APP_END", new Object[0]);
                    } else {
                        this.mHwArbitrationStateMachine.sendMessage(109, appInfo);
                        if (100 == state) {
                            HwArbitrationCommonUtils.logD(TAG, false, "MSG_INSTANT_APP_START", new Object[0]);
                        } else {
                            HwArbitrationCommonUtils.logD(TAG, false, "MSG_INSTANT_APP_UPDATE", new Object[0]);
                        }
                    }
                    this.mPreviousAppInfo = appInfo;
                    if (HwAPPQoEManager.isAppStartMonitor(appInfo, this.mContext)) {
                        this.mWifiPlusFromBrain = true;
                        break;
                    }
                    break;
                case 101:
                    this.mHwArbitrationStateMachine.sendMessage(110, appInfo);
                    HwArbitrationCommonUtils.logD(TAG, false, "MSG_INSTANT_APP_END", new Object[0]);
                    this.mPreviousAppInfo = null;
                    this.mWifiPlusFromBrain = false;
                    break;
            }
        }
        if (802 == HwArbitrationCommonUtils.getActiveConnectType(this.mContext)) {
            if (state == 100) {
                this.mHwArbitrationStateMachine.sendMessage(HwArbitrationDEFS.MSG_APP_STATE_FOREGROUND, appInfo);
            } else if (state == 101) {
                this.mHwArbitrationStateMachine.sendMessage(HwArbitrationDEFS.MSG_APP_STATE_BACKGROUND, appInfo);
            }
        }
        if (800 != HwArbitrationCommonUtils.getActiveConnectType(this.mContext)) {
            HwArbitrationCommonUtils.logD(TAG, false, "------NotifyDSBooster-------", new Object[0]);
            switch (state) {
                case 100:
                case 102:
                case 103:
                    HwHiRadioBoost.createInstance(this.mContext).BrainAppStateNotifyDSBooster(appInfo, 0, 0);
                    return;
                case 101:
                    HwHiRadioBoost.createInstance(this.mContext).BrainAppStateNotifyDSBooster(appInfo, 0, 2);
                    return;
                case 104:
                    HwHiRadioBoost.createInstance(this.mContext).BrainAppStateNotifyDSBooster(appInfo, 0, 1);
                    return;
                default:
                    HwArbitrationCommonUtils.logD(TAG, false, "invalid app state", new Object[0]);
                    return;
            }
        }
    }

    @Override // com.android.server.hidata.appqoe.IHwAPPQoECallback
    public void onAPPQualityCallBack(HwAPPStateInfo appInfo, int experience) {
        this.mGameInfo = appInfo;
        if (appInfo == null) {
            HwArbitrationCommonUtils.logD(TAG, false, "appInfo is null", new Object[0]);
            return;
        }
        HwArbitrationCommonUtils.logD(TAG, false, "%{public}s,experience:%{public}d", appInfo.toString(), Integer.valueOf(experience));
        int i = appInfo.mAppType;
        if (i != 1000) {
            if (i != 2000) {
                if (i != 4000) {
                    return;
                }
            } else if (appInfo.mScenceId == 200002 && experience == 107) {
                if (HwArbitrationCommonUtils.IS_HIDATA2_ENABLED) {
                    this.mHwArbitrationStateMachine.sendMessage(HwArbitrationDEFS.MSG_GAME_WAR_STATE_BAD, appInfo);
                    HwArbitrationCommonUtils.logD(TAG, false, "MSG_GAME_WAR_STATE_BAD", new Object[0]);
                }
                HwHiRadioBoost.createInstance(this.mContext).BrainAppStateNotifyDSBooster(appInfo, 1, 0);
                return;
            } else {
                return;
            }
        }
        if (appInfo.mScenceType == 4 && experience == 111) {
            HwArbitrationCommonUtils.logD(TAG, false, "app stop mplink", new Object[0]);
            this.mHwArbitrationStateMachine.sendMessage(HwArbitrationDEFS.MSG_APP_STOP_MPLINK, appInfo);
        } else if (experience == 107) {
            if (appInfo.mScenceId == 100106) {
                this.mHwArbitrationStateMachine.sendMessage(HwArbitrationDEFS.MSG_STREAMING_VIDEO_BAD, appInfo);
                HwArbitrationCommonUtils.logD(TAG, false, "MSG_STREAMING_VIDEO_BAD", new Object[0]);
            } else if (appInfo.mScenceId == 100105) {
                this.mHwArbitrationStateMachine.sendMessage(HwArbitrationDEFS.MSG_STREAMING_AUDIO_BAD, appInfo);
                HwArbitrationCommonUtils.logD(TAG, false, "MSG_STREAMING_AUDIO_BAD", new Object[0]);
            } else {
                this.mHwArbitrationStateMachine.sendMessage(115, appInfo);
                HwArbitrationCommonUtils.logD(TAG, false, "MSG_INSTANT_BAD", new Object[0]);
            }
            if (801 == HwArbitrationCommonUtils.getActiveConnectType(this.mContext)) {
                HwArbitrationCommonUtils.logD(TAG, false, "------NotifyDSBooster-------", new Object[0]);
                int status = 0;
                if (appInfo.mScenceId == 100105 && 1 == this.hiStreamAudioStatus) {
                    status = 1;
                }
                HwHiRadioBoost.createInstance(this.mContext).BrainAppStateNotifyDSBooster(appInfo, 1, status);
            }
        }
    }

    @Override // com.android.server.hidata.histream.IHwHistreamQoeCallback
    public void onHistreamBadQoedetect(HwHistreamCHRQoeInfo qoeInfo) {
        IGameCHRCallback iGameCHRCallback = this.mGameCHRCallback;
        if (iGameCHRCallback != null && qoeInfo != null) {
            iGameCHRCallback.updateHistreamExperience(qoeInfo);
        }
    }

    @Override // com.android.server.hidata.appqoe.IHwAPPQoECallback
    public void onNetworkQualityCallBack(int UID2, int sense, int network2, boolean isSatisfy) {
        HwArbitrationCommonUtils.logD(TAG, false, "onChannelQoENetworkQualityCallBack", new Object[0]);
        judgeBothCQEAndWM(UID2, network2, isSatisfy, true);
    }

    @Override // com.android.server.hidata.appqoe.IHwAPPQoECallback
    public void onAPPRttInfoCallBack(HwAPPStateInfo info) {
        if (info != null) {
            updataGameExperience(info);
            this.mHwArbitrationStateMachine.updateStateMachineHashMap(info);
        }
    }

    @Override // com.android.server.hidata.wavemapping.IWaveMappingCallback
    public void onWaveMappingRespondCallback(int UID2, int prefer, int network2, boolean isGood, boolean found) {
        HwArbitrationCommonUtils.logD(TAG, false, "onWaveMappingRespondCallback, UID: %{public}d, prefer: %{public}d, network: %{public}d, isGood: %{public}s, found: %{public}s", Integer.valueOf(UID2), Integer.valueOf(prefer), Integer.valueOf(network2), String.valueOf(isGood), String.valueOf(found));
        if (!found) {
            HwArbitrationCommonUtils.logD(TAG, false, "WaveMapping cannot find the history record", new Object[0]);
            isGood = true;
        }
        if (!(network2 == 800 || network2 == 801)) {
            HwArbitrationCommonUtils.logD(TAG, false, "WaveMapping network error", new Object[0]);
            isGood = true;
        }
        judgeBothCQEAndWM(UID2, network2, isGood, false);
    }

    @Override // com.android.server.hidata.wavemapping.IWaveMappingCallback
    public void onWaveMappingReportCallback(int reportType, String networkName, int networkType) {
        HwArbitrationCommonUtils.logD(TAG, false, "onWaveMappingReportCallback", new Object[0]);
    }

    @Override // com.android.server.hidata.wavemapping.IWaveMappingCallback
    public void onWaveMappingRespond4BackCallback(int UID2, int prefer, int network2, boolean isGood, boolean found) {
        HwArbitrationCommonUtils.logD(TAG, false, "onWaveMappingRespond4BackCallback, UID: %{public}d, prefer: %{public}d, network: %{public}d, isGood: %{public}s, found: %{public}s", Integer.valueOf(UID2), Integer.valueOf(prefer), Integer.valueOf(network2), String.valueOf(isGood), String.valueOf(found));
        int wmOut = 0;
        if (!found) {
            HwArbitrationCommonUtils.logD(TAG, false, "WaveMapping cannot find the history record", new Object[0]);
            isGood = true;
        }
        if (!(network2 == 800 || network2 == 801)) {
            HwArbitrationCommonUtils.logD(TAG, false, "WaveMapping network error", new Object[0]);
            isGood = true;
        }
        if (isGood) {
            wmOut = 1;
        }
        this.mHwArbitrationStateMachine.sendMessage(HwArbitrationDEFS.MSG_WM_OUT_FOR_STOP_MPLINK, UID2, wmOut);
    }

    @Override // com.android.server.hidata.hiradio.IHiRadioBoostCallback
    public void LTEto3GResult(int appUid, int result, int reason) {
        String str = "appUid:" + appUid + ", result:" + result + ", reason:" + reason;
        HwArbitrationCommonUtils.logD(TAG, false, "LTEto3GResult:appUid:%{public}d, result:%{public}d, reason:%{public}d", Integer.valueOf(appUid), Integer.valueOf(result), Integer.valueOf(reason));
        this.mHwArbitrationStateMachine.sendMessage(3000, result, reason);
        HwArbitrationCommonUtils.logD(TAG, false, "MSG_HIRADIO_NOTIFY_TO_3G_RESULT", new Object[0]);
    }

    private synchronized void judgeBothCQEAndWM(int UID2, int network2, boolean isGood, boolean isChannelQoE) {
        int i = 4;
        boolean z = false;
        HwArbitrationCommonUtils.logD(TAG, false, " judgeBothCQEAndWM, UID: %{public}d, network: %{public}d, isGood: %{public}s, isChannelQoE: %{public}s", Integer.valueOf(UID2), Integer.valueOf(network2), String.valueOf(isGood), String.valueOf(isChannelQoE));
        if (!isGood) {
            if (isChannelQoE) {
                i = 5;
            }
            this.checkFailReason = i;
        }
        if (1 == this.isFullInfo) {
            if (this.isTargetNetworkGood && isGood) {
                z = true;
            }
            this.isTargetNetworkGood = z;
            if (this.isTargetNetworkGood) {
                this.mHwArbitrationStateMachine.sendMessage(HwArbitrationDEFS.MSG_MPLINK_BIND_CHECK_OK_NOTIFY, UID2);
            } else {
                this.mHwArbitrationStateMachine.sendMessage(HwArbitrationDEFS.MSG_MPLINK_BIND_CHECK_FAIL_NOTIFY, UID2, this.checkFailReason);
            }
            initJudgeBothChqoeAndWm();
        } else {
            this.isFullInfo++;
            this.isTargetNetworkGood = isGood;
            this.UID = UID2;
            this.network = network2;
        }
    }

    public synchronized void initJudgeBothChqoeAndWm() {
        HwArbitrationCommonUtils.logD(TAG, false, "initJudgeBothChqoeAndWm", new Object[0]);
        this.isFullInfo = 0;
        this.UID = -1;
        this.network = -1;
        this.checkFailReason = -1;
    }

    private void updateGameState(HwAPPStateInfo appInfo, int state) {
        IGameCHRCallback iGameCHRCallback = this.mGameCHRCallback;
        if (iGameCHRCallback != null && appInfo != null) {
            iGameCHRCallback.updateGameState(appInfo, state);
        }
    }

    public void updataGameExperience(HwAPPStateInfo appInfo) {
        IGameCHRCallback iGameCHRCallback = this.mGameCHRCallback;
        if (!(iGameCHRCallback == null || appInfo == null)) {
            iGameCHRCallback.updataGameExperience(appInfo);
        }
        IGameReportCallback iGameReportCallback = this.mGameReportCallback;
        if (iGameReportCallback != null && appInfo != null) {
            iGameReportCallback.onReportGameDelay(appInfo.mAppRTT);
        }
    }

    private boolean isPayScene(HwAPPStateInfo appInfo) {
        return false;
    }

    private boolean isTravelScene(HwAPPStateInfo appInfo) {
        return false;
    }

    private boolean isPayEnd(HwAPPStateInfo currentAppInfo, HwAPPStateInfo previousAppInfo) {
        return false;
    }

    private boolean isTravelEnd(HwAPPStateInfo currentAppInfo, HwAPPStateInfo previousAppInfo) {
        return false;
    }

    private boolean isWiTASWhiteListApp(HwAPPStateInfo appInfo) {
        if (2001 == appInfo.mAppId) {
            return true;
        }
        return false;
    }

    public boolean getWifiPlusFlagFromHiData() {
        HwArbitrationStateMachine hwArbitrationStateMachine = this.mHwArbitrationStateMachine;
        if (hwArbitrationStateMachine == null) {
            HwArbitrationCommonUtils.logD(TAG, false, "mHwArbitrationStateMachine is null error", new Object[0]);
            return false;
        } else if (hwArbitrationStateMachine.isDenyByNotification()) {
            HwArbitrationCommonUtils.logD(TAG, false, "hidata due to deny", new Object[0]);
            return false;
        } else if (HwArbitrationCommonUtils.getActiveConnectType(this.mContext) == 802) {
            HwArbitrationCommonUtils.logD(TAG, false, "active connect is none", new Object[0]);
            return false;
        } else if (this.mHwArbitrationStateMachine.isPunishCellDetectImprecise()) {
            HwArbitrationCommonUtils.logD(TAG, false, "punish in pingpang", new Object[0]);
            return false;
        } else if (HwArbitrationManager.isExceedMaxTemperature()) {
            HwArbitrationCommonUtils.logD(TAG, false, "is exceed max temperature", new Object[0]);
            return false;
        } else {
            HwAPPStateInfo appInfo = getGameAppStateInfo();
            if (appInfo == null || appInfo.mScenceType != 4) {
                if (appInfo == null || !HwArbitrationFunction.isAppLinkTurboEnabled(this.mContext, appInfo.mAppUID)) {
                    HwArbitrationCommonUtils.logD(TAG, false, "mWifiPlusFromBrain is %{public}s", String.valueOf(this.mWifiPlusFromBrain));
                    return this.mWifiPlusFromBrain;
                }
                HwArbitrationCommonUtils.logD(TAG, false, "LinkTurbo enabled", new Object[0]);
                return false;
            } else if (!this.mHwArbitrationStateMachine.isInMPLink(appInfo.mAppUID)) {
                return false;
            } else {
                HwArbitrationCommonUtils.logD(TAG, false, "Arbitration is in mplink.", new Object[0]);
                return true;
            }
        }
    }
}
