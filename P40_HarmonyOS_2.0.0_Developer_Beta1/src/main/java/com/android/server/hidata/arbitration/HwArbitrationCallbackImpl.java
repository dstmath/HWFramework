package com.android.server.hidata.arbitration;

import android.content.Context;
import com.android.server.hidata.appqoe.HwAppStateInfo;
import com.android.server.hidata.appqoe.IHwAppQoeCallback;
import com.android.server.hidata.hiradio.HwHiRadioBoost;
import com.android.server.hidata.hiradio.HwWifiBoost;
import com.android.server.hidata.hiradio.IHiRadioBoostCallback;
import com.android.server.hidata.wavemapping.IWaveMappingCallback;

public class HwArbitrationCallbackImpl implements IHwAppQoeCallback, IWaveMappingCallback, IHiRadioBoostCallback {
    private static final String TAG = (HwArbitrationDefs.BASE_TAG + HwArbitrationCallbackImpl.class.getSimpleName());
    private static HwArbitrationCallbackImpl sHwArbitrationCallbackImpl;
    private Context mContext;
    private IGameChrCallback mGameChrCallback;
    private IGameReportCallback mGameReportCallback;
    private int mGameState;
    private int mHiStreamAudioStatus = 0;
    private HwArbitrationStateMachine mHwArbitrationStateMachine;
    private HwWifiBoost mHwWifiBoost = null;
    private HwAppStateInfo mPreviousAppInfo;

    private HwArbitrationCallbackImpl(Context context) {
        this.mContext = context;
        this.mHwArbitrationStateMachine = HwArbitrationStateMachine.getInstance(this.mContext);
        this.mHwWifiBoost = HwWifiBoost.getInstance(this.mContext);
        HwArbitrationCommonUtils.logD(TAG, false, "init HwArbitrationCallbackImpl completed!", new Object[0]);
    }

    public static HwArbitrationCallbackImpl getInstance(Context context) {
        if (sHwArbitrationCallbackImpl == null) {
            sHwArbitrationCallbackImpl = new HwArbitrationCallbackImpl(context);
        }
        return sHwArbitrationCallbackImpl;
    }

    public static HwArbitrationCallbackImpl getInstanceForChr() {
        return sHwArbitrationCallbackImpl;
    }

    public void registerGameReport(IGameReportCallback callback) {
        this.mGameReportCallback = callback;
    }

    public void registerGameChr(IGameChrCallback callback) {
        this.mGameChrCallback = callback;
    }

    @Override // com.android.server.hidata.appqoe.IHwAppQoeCallback
    public void onAppStateCallBack(HwAppStateInfo appInfo, int state) {
        if (appInfo == null) {
            HwArbitrationCommonUtils.logD(TAG, false, "appInfo is null", new Object[0]);
            return;
        }
        this.mHwArbitrationStateMachine.updateStateMachineHashMap(appInfo);
        HwArbitrationCommonUtils.logD(TAG, false, "onAppStateCallBack, appInfo:%{public}s state:%{public}d", appInfo.toString(), Integer.valueOf(state));
        int i = appInfo.mAppType;
        if (i == 1000) {
            processAppType(appInfo, state);
        } else if (i == 2000 || i == 3000) {
            processGameType(appInfo, state);
        } else if (i == 4000) {
            this.mHwArbitrationStateMachine.updateCurrentStreamAppState(appInfo, state);
            getStateType(appInfo, state);
        }
        processForegroundState(appInfo, state);
        processRadioBoost(appInfo, state);
    }

    private void processForegroundState(HwAppStateInfo appInfo, int state) {
        if (HwArbitrationCommonUtils.getActiveConnectType(this.mContext) != 802) {
            return;
        }
        if (state == 100) {
            this.mHwArbitrationStateMachine.sendMessage(HwArbitrationDefs.MSG_APP_STATE_FOREGROUND, appInfo);
        } else if (state == 101) {
            this.mHwArbitrationStateMachine.sendMessage(HwArbitrationDefs.MSG_APP_STATE_BACKGROUND, appInfo);
        } else {
            HwArbitrationCommonUtils.logD(TAG, false, "unknown app state", new Object[0]);
        }
    }

    private void processRadioBoost(HwAppStateInfo appInfo, int state) {
        int networkType = HwArbitrationCommonUtils.getActiveConnectType(this.mContext);
        if (networkType != 800 && networkType != 802) {
            HwArbitrationCommonUtils.logD(TAG, false, "notify radio boost", new Object[0]);
            switch (state) {
                case 100:
                case 102:
                case 103:
                    HwHiRadioBoost.createInstance(this.mContext).brainAppStateNotifyDSBooster(appInfo, 0, 0);
                    return;
                case 101:
                    HwHiRadioBoost.createInstance(this.mContext).brainAppStateNotifyDSBooster(appInfo, 0, 2);
                    return;
                case 104:
                    HwHiRadioBoost.createInstance(this.mContext).brainAppStateNotifyDSBooster(appInfo, 0, 1);
                    return;
                default:
                    HwArbitrationCommonUtils.logD(TAG, false, "invalid app state", new Object[0]);
                    return;
            }
        }
    }

    private void processAppType(HwAppStateInfo appInfo, int state) {
        switch (state) {
            case 100:
            case 102:
                sendMessageByTravelScenes(appInfo, state);
                return;
            case 101:
                this.mHwArbitrationStateMachine.sendMessage(110, appInfo);
                HwArbitrationCommonUtils.logD(TAG, false, "MSG_INSTANT_APP_END", new Object[0]);
                this.mPreviousAppInfo = null;
                return;
            default:
                return;
        }
    }

    private void processGameType(HwAppStateInfo appInfo, int state) {
        switch (state) {
            case 100:
                processGameStateStart(appInfo);
                return;
            case 101:
                processGameStateEnd(appInfo);
                return;
            case 102:
                processGameStateUpdate(appInfo);
                return;
            case 103:
            default:
                return;
            case 104:
                processGameStateBackground(appInfo);
                return;
        }
    }

    private void processGameStateStart(HwAppStateInfo appInfo) {
        if (appInfo.mScenesId == 200002) {
            this.mHwArbitrationStateMachine.sendMessage(104, appInfo);
            HwArbitrationCommonUtils.logD(TAG, false, "MSG_GAME_ENTER_PVP_BATTLE", new Object[0]);
            HwArbitrationFunction.setPvpScene(true);
            this.mGameState = 4;
            updateGameState(appInfo, this.mGameState);
        } else if (appInfo.mScenesId == 200001) {
            this.mHwArbitrationStateMachine.sendMessage(100, appInfo);
            HwArbitrationCommonUtils.logD(TAG, false, "MSG_GAME_STATE_START", new Object[0]);
        } else {
            HwArbitrationCommonUtils.logD(TAG, false, "unknown app scenes", new Object[0]);
        }
        if (this.mGameReportCallback != null && appInfo.mAppType == 2000) {
            this.mGameReportCallback.onReportGameState(true, isWiTASWhiteListApp(appInfo), appInfo.mAppId);
        }
        this.mHwWifiBoost.setHiLinkAccGameMode(true, appInfo.mAppUid);
    }

    private void processGameStateUpdate(HwAppStateInfo appInfo) {
        if (appInfo.mScenesId == 200002) {
            this.mHwArbitrationStateMachine.sendMessage(104, appInfo);
            HwArbitrationCommonUtils.logD(TAG, false, "MSG_GAME_UPDATE_PVP_BATTLE", new Object[0]);
            HwArbitrationFunction.setPvpScene(true);
            this.mGameState = 1;
            updateGameState(appInfo, this.mGameState);
            IGameReportCallback iGameReportCallback = this.mGameReportCallback;
            if (iGameReportCallback != null) {
                iGameReportCallback.onReportGameState(true, isWiTASWhiteListApp(appInfo), appInfo.mAppId);
            }
            this.mHwWifiBoost.setHiLinkAccGameMode(true, appInfo.mAppUid);
        } else if (appInfo.mScenesId == 200001) {
            this.mHwArbitrationStateMachine.sendMessage(105, appInfo);
            HwArbitrationCommonUtils.logD(TAG, false, "MSG_GAME_EXIT_PVP_BATTLE", new Object[0]);
            HwArbitrationFunction.setPvpScene(false);
            this.mGameState = 2;
            updateGameState(appInfo, this.mGameState);
        } else {
            HwArbitrationCommonUtils.logD(TAG, false, "unknown app scenes", new Object[0]);
        }
    }

    private void processGameStateBackground(HwAppStateInfo appInfo) {
        this.mHwArbitrationStateMachine.sendMessage(103, appInfo);
        HwArbitrationCommonUtils.logD(TAG, false, "MSG_GAME_STATE_BACKGROUND", new Object[0]);
        if (appInfo.mScenesId == 200002) {
            this.mGameState = 3;
            updateGameState(appInfo, this.mGameState);
        }
        HwArbitrationFunction.setPvpScene(false);
        IGameReportCallback iGameReportCallback = this.mGameReportCallback;
        if (iGameReportCallback != null) {
            iGameReportCallback.onReportGameState(false, isWiTASWhiteListApp(appInfo), appInfo.mAppId);
        }
        this.mHwWifiBoost.setHiLinkAccGameMode(false, appInfo.mAppUid);
    }

    private void processGameStateEnd(HwAppStateInfo appInfo) {
        this.mHwArbitrationStateMachine.sendMessage(101, appInfo);
        HwArbitrationCommonUtils.logD(TAG, false, "MSG_GAME_STATE_END", new Object[0]);
        HwArbitrationFunction.setPvpScene(false);
        this.mGameState = 5;
        updateGameState(appInfo, this.mGameState);
        IGameReportCallback iGameReportCallback = this.mGameReportCallback;
        if (iGameReportCallback != null) {
            iGameReportCallback.onReportGameState(false, isWiTASWhiteListApp(appInfo), appInfo.mAppId);
        }
        this.mHwWifiBoost.setHiLinkAccGameMode(false, appInfo.mAppUid);
    }

    private void getStateType(HwAppStateInfo appInfo, int state) {
        switch (state) {
            case 100:
                if (HwArbitrationFunction.isStreamingScene(appInfo)) {
                    this.mHwArbitrationStateMachine.sendMessage(106, appInfo);
                    HwArbitrationCommonUtils.logD(TAG, false, "MSG_STREAMING_APP_START", new Object[0]);
                    this.mHiStreamAudioStatus = 0;
                }
                HwAppTimeDetail.getInstance().streamMediaStartTime(appInfo);
                return;
            case 101:
                if (HwArbitrationFunction.isStreamingScene(appInfo)) {
                    this.mHwArbitrationStateMachine.sendMessage(107, appInfo);
                    HwArbitrationCommonUtils.logD(TAG, false, "MSG_STREAM_APP_END", new Object[0]);
                }
                HwAppTimeDetail.getInstance().streamMediaStopTime(appInfo);
                return;
            case 102:
            default:
                return;
            case 103:
                if (HwArbitrationFunction.isStreamingScene(appInfo)) {
                    this.mHwArbitrationStateMachine.sendMessage(120, appInfo);
                    HwArbitrationCommonUtils.logD(TAG, false, "MSG_STREAMING_APP_FOREGROUND", new Object[0]);
                    this.mHiStreamAudioStatus = 0;
                    return;
                }
                return;
            case 104:
                if (HwArbitrationFunction.isStreamingScene(appInfo)) {
                    HwArbitrationCommonUtils.logD(TAG, false, "MSG_STREAMING_APP_BACKGROUND", new Object[0]);
                    this.mHwArbitrationStateMachine.sendMessage(121, appInfo);
                    this.mHiStreamAudioStatus = 1;
                    return;
                }
                return;
        }
    }

    private void sendMessageByTravelScenes(HwAppStateInfo appInfo, int state) {
        if (isPayScenes(appInfo)) {
            this.mHwArbitrationStateMachine.sendMessage(111, appInfo);
            HwArbitrationCommonUtils.logD(TAG, false, "MSG_INSTANT_PAY_APP_START", new Object[0]);
        } else if (isTravelScenes(appInfo)) {
            this.mHwArbitrationStateMachine.sendMessage(113, appInfo);
            HwArbitrationCommonUtils.logD(TAG, false, "MSG_INSTANT_TRAVEL_APP_START", new Object[0]);
        } else if (isPayEnd(appInfo, this.mPreviousAppInfo)) {
            this.mHwArbitrationStateMachine.sendMessage(112, appInfo);
            HwArbitrationCommonUtils.logD(TAG, false, "MSG_INSTANT_PAY_APP_END", new Object[0]);
        } else if (isTravelEnd(appInfo, this.mPreviousAppInfo)) {
            this.mHwArbitrationStateMachine.sendMessage(HwArbitrationDefs.MSG_INSTANT_TRAVEL_APP_END, appInfo);
            HwArbitrationCommonUtils.logD(TAG, false, "MSG_INSTANT_TRAVEL_APP_END", new Object[0]);
        } else {
            this.mHwArbitrationStateMachine.sendMessage(109, appInfo);
            if (state == 100) {
                HwArbitrationCommonUtils.logD(TAG, false, "MSG_INSTANT_APP_START", new Object[0]);
            } else {
                HwArbitrationCommonUtils.logD(TAG, false, "MSG_INSTANT_APP_UPDATE", new Object[0]);
            }
        }
        this.mPreviousAppInfo = appInfo;
    }

    @Override // com.android.server.hidata.appqoe.IHwAppQoeCallback
    public void onAppQualityCallBack(HwAppStateInfo appInfo, int experience) {
        if (appInfo == null) {
            HwArbitrationCommonUtils.logD(TAG, false, "appInfo is null", new Object[0]);
            return;
        }
        HwArbitrationCommonUtils.logD(TAG, false, "%{public}s, experience:%{public}d", appInfo.toString(), Integer.valueOf(experience));
        int i = appInfo.mAppType;
        if (i != 1000) {
            if (i == 2000) {
                processGameQuality(appInfo, experience);
                return;
            } else if (i != 4000) {
                return;
            }
        }
        processAppQuality(appInfo, experience);
    }

    private void processGameQuality(HwAppStateInfo appInfo, int experience) {
        if (appInfo.mScenesId == 200002 && experience == 107) {
            HwArbitrationCommonUtils.logD(TAG, false, "MSG_GAME_WAR_STATE_BAD", new Object[0]);
            HwHiRadioBoost.createInstance(this.mContext).brainAppStateNotifyDSBooster(appInfo, 1, 0);
        }
    }

    private void processAppQuality(HwAppStateInfo appInfo, int experience) {
        if (experience == 107) {
            if (appInfo.mScenesId == 100106) {
                HwArbitrationCommonUtils.logD(TAG, false, "MSG_STREAMING_VIDEO_BAD", new Object[0]);
            } else if (appInfo.mScenesId == 100105) {
                HwArbitrationCommonUtils.logD(TAG, false, "MSG_STREAMING_AUDIO_BAD", new Object[0]);
            } else {
                HwArbitrationCommonUtils.logD(TAG, false, "MSG_INSTANT_BAD", new Object[0]);
            }
            if (HwArbitrationCommonUtils.getActiveConnectType(this.mContext) == 801) {
                HwArbitrationCommonUtils.logD(TAG, false, "notify radio boost", new Object[0]);
                int status = 0;
                if (appInfo.mScenesId == 100105 && this.mHiStreamAudioStatus == 1) {
                    status = 1;
                }
                HwHiRadioBoost.createInstance(this.mContext).brainAppStateNotifyDSBooster(appInfo, 1, status);
            }
        }
    }

    @Override // com.android.server.hidata.appqoe.IHwAppQoeCallback
    public void onAppRttInfoCallBack(HwAppStateInfo info) {
        if (info != null) {
            updateGameExperience(info);
            this.mHwArbitrationStateMachine.updateStateMachineHashMap(info);
        }
    }

    @Override // com.android.server.hidata.wavemapping.IWaveMappingCallback
    public void onWaveMappingRespondCallback(int UID, int prefer, int network, boolean isGood, boolean found) {
        HwArbitrationCommonUtils.logD(TAG, false, "onWaveMappingRespondCallback, UID: %{public}d, prefer: %{public}d, network: %{public}d, isGood: %{public}s, found: %{public}s", Integer.valueOf(UID), Integer.valueOf(prefer), Integer.valueOf(network), String.valueOf(isGood), String.valueOf(found));
    }

    @Override // com.android.server.hidata.wavemapping.IWaveMappingCallback
    public void onWaveMappingReportCallback(int reportType, String networkName, int networkType) {
        HwArbitrationCommonUtils.logD(TAG, false, "onWaveMappingReportCallback", new Object[0]);
    }

    @Override // com.android.server.hidata.wavemapping.IWaveMappingCallback
    public void onWaveMappingRespond4BackCallback(int UID, int prefer, int network, boolean isGood, boolean found) {
        HwArbitrationCommonUtils.logD(TAG, false, "onWaveMappingRespond4BackCallback, UID: %{public}d, prefer: %{public}d, network: %{public}d, isGood: %{public}s, found: %{public}s", Integer.valueOf(UID), Integer.valueOf(prefer), Integer.valueOf(network), String.valueOf(isGood), String.valueOf(found));
    }

    @Override // com.android.server.hidata.hiradio.IHiRadioBoostCallback
    public void lteTo3gResult(int appUid, int result, int reason) {
        HwArbitrationCommonUtils.logD(TAG, false, "lteTo3gResult, appUid:%{public}d, result:%{public}d, reason:%{public}d", Integer.valueOf(appUid), Integer.valueOf(result), Integer.valueOf(reason));
        this.mHwArbitrationStateMachine.sendMessage(3000, result, reason);
    }

    private void updateGameState(HwAppStateInfo appInfo, int state) {
        IGameChrCallback iGameChrCallback = this.mGameChrCallback;
        if (iGameChrCallback != null && appInfo != null) {
            iGameChrCallback.updateGameState(appInfo, state);
        }
    }

    private void updateGameExperience(HwAppStateInfo appInfo) {
        IGameChrCallback iGameChrCallback = this.mGameChrCallback;
        if (!(iGameChrCallback == null || appInfo == null)) {
            iGameChrCallback.updateGameExperience(appInfo);
        }
        IGameReportCallback iGameReportCallback = this.mGameReportCallback;
        if (iGameReportCallback != null && appInfo != null) {
            iGameReportCallback.onReportGameDelay(appInfo.mAppRtt);
        }
    }

    private boolean isPayScenes(HwAppStateInfo appInfo) {
        return false;
    }

    private boolean isTravelScenes(HwAppStateInfo appInfo) {
        return false;
    }

    private boolean isPayEnd(HwAppStateInfo currentAppInfo, HwAppStateInfo previousAppInfo) {
        return false;
    }

    private boolean isTravelEnd(HwAppStateInfo currentAppInfo, HwAppStateInfo previousAppInfo) {
        return false;
    }

    private boolean isWiTASWhiteListApp(HwAppStateInfo appInfo) {
        if (appInfo.mAppId == 2001) {
            return true;
        }
        return false;
    }
}
