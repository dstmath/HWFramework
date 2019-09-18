package com.android.server.hidata.arbitration;

import android.content.Context;
import com.android.server.hidata.appqoe.HwAPPStateInfo;
import com.android.server.hidata.appqoe.IHwAPPQoECallback;
import com.android.server.hidata.hiradio.HwHiRadioBoost;
import com.android.server.hidata.hiradio.HwWifiBoost;
import com.android.server.hidata.hiradio.IHiRadioBoostCallback;
import com.android.server.hidata.histream.HwHistreamCHRQoeInfo;
import com.android.server.hidata.histream.IHwHistreamQoeCallback;
import com.android.server.hidata.mplink.IMpLinkCallback;
import com.android.server.hidata.mplink.MplinkBindResultInfo;
import com.android.server.hidata.mplink.MplinkNetworkResultInfo;
import com.android.server.hidata.wavemapping.IWaveMappingCallback;
import java.util.HashMap;

public class HwArbitrationCallbackImpl implements IMpLinkCallback, IHwAPPQoECallback, IWaveMappingCallback, IHwHistreamQoeCallback, IHiRadioBoostCallback {
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
        HwArbitrationCommonUtils.logD(TAG, "init HwArbitrationCallbackImpl completed!");
    }

    public void onBindProcessToNetworkResult(MplinkBindResultInfo result) {
        if (result != null) {
            HwArbitrationCommonUtils.logD(TAG, "onMpLinkBindResult:result = " + result.getResult() + ", failReason = " + result.getFailReason() + ", uid = " + result.getUid() + ", network = " + result.getNetwork() + ", type = " + result.getType());
            switch (result.getResult()) {
                case 1:
                    this.mHwArbitrationStateMachine.sendMessage(HwArbitrationDEFS.MSG_MPLINK_BIND_SUCCESS, result);
                    return;
                case 2:
                    this.mHwArbitrationStateMachine.sendMessage(HwArbitrationDEFS.MSG_MPLINK_BIND_FAIL, result);
                    return;
                case 3:
                    this.mHwArbitrationStateMachine.sendMessage(HwArbitrationDEFS.MSG_MPLINK_UNBIND_SUCCESS, result);
                    return;
                case 4:
                    this.mHwArbitrationStateMachine.sendMessage(HwArbitrationDEFS.MSG_MPLINK_UNBIND_FAIL, result);
                    return;
                default:
                    return;
            }
        }
    }

    public void onWiFiAndCellCoexistResult(MplinkNetworkResultInfo result) {
        HwArbitrationCommonUtils.logD(TAG, "onWiFiAndCellCoexistResult");
        if (result != null) {
            HwArbitrationCommonUtils.logD(TAG, "mResult = " + result.getResult() + ", mFailReason = " + result.getFailReason() + ", mApType = " + result.getAPType() + ", mActivteNetwork =" + result.getActiveNetwork());
            if (100 == result.getResult()) {
                HwArbitrationCommonUtils.logD(TAG, "enter wifi cell coex mode,type:" + result.getAPType());
                if (1 == result.getAPType()) {
                    this.mHwArbitrationStateMachine.sendMessage(HwArbitrationDEFS.MSG_MPLINK_AI_DEVICE_COEX_MODE, result);
                } else {
                    this.mHwArbitrationStateMachine.sendMessage(2003, result);
                }
            } else if (101 == result.getResult()) {
                HwArbitrationCommonUtils.logD(TAG, "leave wifi cell coex mode");
                this.mHwArbitrationStateMachine.sendMessage(HwArbitrationDEFS.MSG_MPLINK_NONCOEX_MODE, result);
            }
        }
    }

    public void onWifiLinkQuality(int uid, int scene, boolean isSatisfy) {
        HwArbitrationCommonUtils.logD(TAG, "uid: " + uid + " scene: " + scene + " isSatisfy: " + isSatisfy);
        if (isSatisfy) {
            HwArbitrationCommonUtils.logD(TAG, "APPQoE detect that wifi is good");
            this.mHwArbitrationStateMachine.sendMessage(HwArbitrationDEFS.MSG_APPQoE_WIFI_GOOD, uid);
        }
    }

    public void onAPPStateCallBack(HwAPPStateInfo appInfo, int state) {
        if (appInfo == null) {
            HwArbitrationCommonUtils.logD(TAG, "appInfo is null");
            return;
        }
        HwArbitrationCommonUtils.logD(TAG, "onAppStateCallBack: appInfo:" + appInfo.toString() + " state:" + state);
        int i = appInfo.mAppType;
        if (i != 1000) {
            if (i != 2000 && i != 3000) {
                if (i == 4000) {
                    this.mHwArbitrationStateMachine.updateCurrentStreamAppState(appInfo, state);
                    switch (state) {
                        case 100:
                            if (HwArbitrationFunction.isStreamingScene(appInfo)) {
                                this.mHwArbitrationStateMachine.sendMessage(106, appInfo);
                                HwArbitrationCommonUtils.logD(TAG, "MSG_STREAMING_APP_START");
                                this.hiStreamAudioStatus = 0;
                            }
                            this.mWifiPlusFromBrain = true;
                            break;
                        case 101:
                            if (HwArbitrationFunction.isStreamingScene(appInfo)) {
                                this.mHwArbitrationStateMachine.sendMessage(107, appInfo);
                                HwArbitrationCommonUtils.logD(TAG, "MSG_STREAM_APP_END");
                            }
                            this.mWifiPlusFromBrain = false;
                            break;
                        case 103:
                            if (HwArbitrationFunction.isStreamingScene(appInfo)) {
                                this.mHwArbitrationStateMachine.sendMessage(120, appInfo);
                                HwArbitrationCommonUtils.logD(TAG, "Callback:MSG_STREAMING_APP_FOREGROUND");
                                this.hiStreamAudioStatus = 0;
                                break;
                            }
                            break;
                        case 104:
                            if (HwArbitrationFunction.isStreamingScene(appInfo)) {
                                HwArbitrationCommonUtils.logD(TAG, "Callback:MSG_STREAMING_APP_BACKGROUND");
                                this.mHwArbitrationStateMachine.sendMessage(121, appInfo);
                                this.hiStreamAudioStatus = 1;
                                break;
                            }
                            break;
                    }
                }
            } else {
                switch (state) {
                    case 100:
                        if (appInfo.mScenceId == 200002) {
                            this.mHwArbitrationStateMachine.sendMessage(104, appInfo);
                            HwArbitrationCommonUtils.logD(TAG, "MSG_GAME_ENTER_PVP_BATTLE");
                            HwArbitrationFunction.setPvpScene(true);
                            this.mGameState = 4;
                            updateGameState(appInfo, this.mGameState);
                        } else if (appInfo.mScenceId == 200001) {
                            this.mHwArbitrationStateMachine.sendMessage(100, appInfo);
                            HwArbitrationCommonUtils.logD(TAG, "MSG_GAME_STATE_START");
                            HwArbitrationDisplay.getInstance(this.mContext).startSmpMonitor();
                        }
                        if (!(this.mGameReportCallback == null || appInfo == null || appInfo.mAppType != 2000)) {
                            this.mGameReportCallback.onReportGameState(true, isWiTASWhiteListApp(appInfo), appInfo.mAppId);
                        }
                        this.mHwWifiBoost.setHiLinkAccGameMode(true, appInfo.mAppUID);
                        break;
                    case 101:
                        this.mHwArbitrationStateMachine.sendMessage(101, appInfo);
                        HwArbitrationCommonUtils.logD(TAG, "MSG_GAME_STATE_END");
                        HwArbitrationFunction.setPvpScene(false);
                        this.mGameState = 5;
                        updateGameState(appInfo, this.mGameState);
                        if (!(this.mGameReportCallback == null || appInfo == null)) {
                            this.mGameReportCallback.onReportGameState(false, isWiTASWhiteListApp(appInfo), appInfo.mAppId);
                        }
                        this.mHwWifiBoost.setHiLinkAccGameMode(false, appInfo.mAppUID);
                        break;
                    case 102:
                        if (appInfo.mScenceId != 200002) {
                            if (appInfo.mScenceId == 200001) {
                                this.mHwArbitrationStateMachine.sendMessage(105, appInfo);
                                HwArbitrationCommonUtils.logD(TAG, "MSG_GAME_EXIT_PVP_BATTLE");
                                HwArbitrationFunction.setPvpScene(false);
                                this.mGameState = 2;
                                updateGameState(appInfo, this.mGameState);
                                break;
                            }
                        } else {
                            this.mHwArbitrationStateMachine.sendMessage(104, appInfo);
                            HwArbitrationCommonUtils.logD(TAG, "MSG_GAME_ENTER_PVP_BATTLE");
                            HwArbitrationFunction.setPvpScene(true);
                            this.mGameState = 1;
                            updateGameState(appInfo, this.mGameState);
                            if (!(this.mGameReportCallback == null || appInfo == null)) {
                                this.mGameReportCallback.onReportGameState(true, isWiTASWhiteListApp(appInfo), appInfo.mAppId);
                            }
                            this.mHwWifiBoost.setHiLinkAccGameMode(true, appInfo.mAppUID);
                            break;
                        }
                        break;
                    case 104:
                        this.mHwArbitrationStateMachine.sendMessage(103, appInfo);
                        HwArbitrationCommonUtils.logD(TAG, "MSG_GAME_STATE_BACKGROUND");
                        if (appInfo.mScenceId == 200002) {
                            this.mGameState = 3;
                            updateGameState(appInfo, this.mGameState);
                        }
                        HwArbitrationFunction.setPvpScene(false);
                        if (!(this.mGameReportCallback == null || appInfo == null)) {
                            this.mGameReportCallback.onReportGameState(false, isWiTASWhiteListApp(appInfo), appInfo.mAppId);
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
                        HwArbitrationCommonUtils.logD(TAG, "MSG_INSTANT_PAY_APP_START");
                    } else if (isTravelScene(appInfo)) {
                        this.mHwArbitrationStateMachine.sendMessage(113, appInfo);
                        HwArbitrationCommonUtils.logD(TAG, "MSG_INSTANT_TRAVEL_APP_START");
                    } else if (isPayEnd(appInfo, this.mPreviousAppInfo)) {
                        this.mHwArbitrationStateMachine.sendMessage(112, appInfo);
                        HwArbitrationCommonUtils.logD(TAG, "MSG_INSTANT_PAY_APP_END");
                    } else if (isTravelEnd(appInfo, this.mPreviousAppInfo)) {
                        this.mHwArbitrationStateMachine.sendMessage(114, appInfo);
                        HwArbitrationCommonUtils.logD(TAG, "MSG_INSTANT_TRAVEL_APP_END");
                    } else {
                        this.mHwArbitrationStateMachine.sendMessage(109, appInfo);
                        if (100 == state) {
                            HwArbitrationCommonUtils.logD(TAG, "MSG_INSTANT_APP_START");
                        } else {
                            HwArbitrationCommonUtils.logD(TAG, "MSG_INSTANT_APP_UPDATE");
                        }
                    }
                    this.mPreviousAppInfo = appInfo;
                    this.mWifiPlusFromBrain = true;
                    break;
                case 101:
                    this.mHwArbitrationStateMachine.sendMessage(110, appInfo);
                    HwArbitrationCommonUtils.logD(TAG, "MSG_INSTANT_APP_END");
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
            HwArbitrationCommonUtils.logD(TAG, "------NotifyDSBooster-------");
            switch (state) {
                case 100:
                case 102:
                case 103:
                    HwHiRadioBoost.createInstance(this.mContext).BrainAppStateNotifyDSBooster(appInfo, 0, 0);
                    break;
                case 101:
                    HwHiRadioBoost.createInstance(this.mContext).BrainAppStateNotifyDSBooster(appInfo, 0, 2);
                    break;
                case 104:
                    if (appInfo.mAppType != 2000) {
                        HwHiRadioBoost.createInstance(this.mContext).BrainAppStateNotifyDSBooster(appInfo, 0, 1);
                        break;
                    } else {
                        HwHiRadioBoost.createInstance(this.mContext).BrainAppStateNotifyDSBooster(appInfo, 0, 2);
                        break;
                    }
                default:
                    HwArbitrationCommonUtils.logD(TAG, "invalid app state");
                    break;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0036, code lost:
        if (r0 != 4000) goto L_0x00b6;
     */
    public void onAPPQualityCallBack(HwAPPStateInfo appInfo, int experience) {
        if (appInfo == null) {
            HwArbitrationCommonUtils.logD(TAG, "appInfo is null");
            return;
        }
        HwArbitrationCommonUtils.logD(TAG, appInfo.toString() + ",experience:" + experience);
        int i = appInfo.mAppType;
        if (i != 1000) {
            if (i == 2000) {
                if (appInfo.mScenceId == 200002 && experience == 107) {
                    HwArbitrationCommonUtils.logD(TAG, "MSG_GAME_WAR_STATE_BAD, ignore it");
                    HwHiRadioBoost.createInstance(this.mContext).BrainAppStateNotifyDSBooster(appInfo, 1, 0);
                }
            }
        }
        if (experience == 107) {
            if (appInfo.mScenceId == 100106) {
                this.mHwArbitrationStateMachine.sendMessage(HwArbitrationDEFS.MSG_STREAMING_VIDEO_BAD, appInfo);
                HwArbitrationCommonUtils.logD(TAG, "MSG_STREAMING_VIDEO_BAD");
            } else if (appInfo.mScenceId == 100105) {
                this.mHwArbitrationStateMachine.sendMessage(HwArbitrationDEFS.MSG_STREAMING_AUDIO_BAD, appInfo);
                HwArbitrationCommonUtils.logD(TAG, "MSG_STREAMING_AUDIO_BAD");
            } else {
                this.mHwArbitrationStateMachine.sendMessage(115, appInfo);
                HwArbitrationCommonUtils.logD(TAG, "MSG_INSTANT_BAD");
            }
            if (801 == HwArbitrationCommonUtils.getActiveConnectType(this.mContext)) {
                HwArbitrationCommonUtils.logD(TAG, "------NotifyDSBooster-------");
                int status = 0;
                if (appInfo.mScenceId == 100105 && 1 == this.hiStreamAudioStatus) {
                    status = 1;
                }
                HwHiRadioBoost.createInstance(this.mContext).BrainAppStateNotifyDSBooster(appInfo, 1, status);
            }
        }
    }

    public void onHistreamBadQoedetect(HwHistreamCHRQoeInfo qoeInfo) {
        if (this.mGameCHRCallback != null && qoeInfo != null) {
            this.mGameCHRCallback.updateHistreamExperience(qoeInfo);
        }
    }

    public void onNetworkQualityCallBack(int UID2, int sense, int network2, boolean isSatisfy) {
        HwArbitrationCommonUtils.logD(TAG, "onChannelQoENetworkQualityCallBack");
        judgeBothCQEAndWM(UID2, network2, isSatisfy, true);
    }

    public void onAPPRttInfoCallBack(HwAPPStateInfo info) {
        updataGameExperience(info);
    }

    public void onWaveMappingRespondCallback(int UID2, int prefer, int network2, boolean isGood, boolean found) {
        HwArbitrationCommonUtils.logD(TAG, "onWaveMappingRespondCallback, UID: " + UID2 + ", prefer: " + prefer + ", network: " + network2 + ", isGood: " + isGood + ", found: " + found);
        if (!found) {
            HwArbitrationCommonUtils.logD(TAG, "WaveMapping cannot find the history record");
            isGood = true;
        }
        if (!(network2 == 800 || network2 == 801)) {
            HwArbitrationCommonUtils.logD(TAG, "WaveMapping network error");
            isGood = true;
        }
        judgeBothCQEAndWM(UID2, network2, isGood, false);
    }

    public void onWaveMappingReportCallback(int reportType, String networkName, int networkType) {
        HwArbitrationCommonUtils.logD(TAG, "onWaveMappingReportCallback");
    }

    public void onWaveMappingRespond4BackCallback(int UID2, int prefer, int network2, boolean isGood, boolean found) {
        HwArbitrationCommonUtils.logD(TAG, "onWaveMappingRespond4BackCallback, UID: " + UID2 + ", prefer: " + prefer + ", network: " + network2 + ", isGood: " + isGood + ", found: " + found);
        int wmOut = 0;
        if (!found) {
            HwArbitrationCommonUtils.logD(TAG, "WaveMapping cannot find the history record");
            isGood = true;
        }
        if (!(network2 == 800 || network2 == 801)) {
            HwArbitrationCommonUtils.logD(TAG, "WaveMapping network error");
            isGood = true;
        }
        if (isGood) {
            wmOut = 1;
        }
        this.mHwArbitrationStateMachine.sendMessage(HwArbitrationDEFS.MSG_WM_OUT_FOR_STOP_MPLINK, UID2, wmOut);
    }

    public void LTEto3GResult(int appUid, int result, int reason) {
        String str = "appUid:" + appUid + ", result:" + result + ", reason:" + reason;
        HwArbitrationCommonUtils.logD(TAG, "LTEto3GResult:appUid:" + appUid + ", result:" + result + ", reason:" + reason);
        this.mHwArbitrationStateMachine.sendMessage(3000, result, reason);
        HwArbitrationCommonUtils.logD(TAG, "MSG_HIRADIO_NOTIFY_TO_3G_RESULT");
    }

    private synchronized void judgeBothCQEAndWM(int UID2, int network2, boolean isGood, boolean isChannelQoE) {
        int i;
        HwArbitrationCommonUtils.logD(TAG, " judgeBothCQEAndWM, UID: " + UID2 + ", network: " + network2 + ", isGood: " + isGood + ", isChannelQoE: " + isChannelQoE);
        if (!isGood) {
            if (isChannelQoE) {
                i = 5;
            } else {
                i = 4;
            }
            this.checkFailReason = i;
        }
        boolean z = true;
        if (1 == this.isFullInfo) {
            if (!this.isTargetNetworkGood || !isGood) {
                z = false;
            }
            this.isTargetNetworkGood = z;
            if (this.isTargetNetworkGood) {
                this.mHwArbitrationStateMachine.sendMessage(HwArbitrationDEFS.MSG_MPLINK_BIND_CHECK_OK_NOTIFY, UID2);
            } else {
                this.mHwArbitrationStateMachine.sendMessage(HwArbitrationDEFS.MSG_MPLINK_BIND_CHECK_FAIL_NOTIFY, UID2, this.checkFailReason);
            }
            initJudgeBothChQoEAndWM();
        } else {
            this.isFullInfo++;
            this.isTargetNetworkGood = isGood;
            this.UID = UID2;
            this.network = network2;
        }
    }

    public synchronized void initJudgeBothChQoEAndWM() {
        HwArbitrationCommonUtils.logD(TAG, "initJudgeBothCQEAndWM");
        this.isFullInfo = 0;
        this.UID = -1;
        this.network = -1;
        this.checkFailReason = -1;
    }

    private void updateGameState(HwAPPStateInfo appInfo, int state) {
        if (this.mGameCHRCallback != null && appInfo != null) {
            this.mGameCHRCallback.updateGameState(appInfo, state);
        }
    }

    public void updataGameExperience(HwAPPStateInfo appInfo) {
        if (!(this.mGameCHRCallback == null || appInfo == null)) {
            this.mGameCHRCallback.updataGameExperience(appInfo);
        }
        if (this.mGameReportCallback != null && appInfo != null) {
            this.mGameReportCallback.onReportGameDelay(appInfo.mAppRTT);
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
        HwArbitrationCommonUtils.logD(TAG, "mWifiPlusFromBrain is " + this.mWifiPlusFromBrain);
        return this.mWifiPlusFromBrain;
    }
}
