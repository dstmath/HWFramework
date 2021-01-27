package com.android.server.wifi.cast;

import android.content.Context;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.HwWifiCHRService;
import com.android.server.wifi.HwWifiCHRServiceImpl;
import com.android.server.wifi.HwWifiStateMachine;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.cast.CastOptDeviceCfg;
import com.android.server.wifi.cast.P2pSharing.Utils;
import com.android.server.wifi.hwUtil.WifiCommonUtils;
import java.util.UUID;

public class CastOptChr {
    private static final int CAST_OPT_CHR_ID = 32;
    public static final int CAST_SCENES_PAD_TYPE = 1;
    public static final int CAST_SCENES_PC_EXSCREEN_TYPE = 4;
    public static final int CAST_SCENES_PC_TYPE = 2;
    public static final int CAST_SCENES_TV_TYPE = 3;
    private static final int GC_OPT_NONE_ERROR_TYPE = 0;
    public static final int GC_OPT_NONE_TYPE = 0;
    private static final int GC_OPT_P2P_SHARING_TYPE = 5;
    private static final int GC_OPT_STA_ROAM_24G_ERROR_TYPE = 3;
    public static final int GC_OPT_STA_ROAM_24G_TYPE = 3;
    private static final int GC_OPT_STA_ROAM_5G_ERROR_TYPE = 1;
    public static final int GC_OPT_STA_ROAM_5G_TYPE = 1;
    private static final int GC_OPT_STA_SWITCH_24G_ERROR_TYPE = 4;
    public static final int GC_OPT_STA_SWITCH_24G_TYPE = 4;
    private static final int GC_OPT_STA_SWITCH_5G_ERROR_TYPE = 2;
    public static final int GC_OPT_STA_SWITCH_5G_TYPE = 2;
    private static final int GET_P2P_GROUP_INFO_INTERVAL = 100;
    private static final int GET_P2P_GROUP_INFO_MAX_TIMES = 20;
    private static final int GET_P2P_GROUP_INFO_MSG = 0;
    private static final int GO_OPT_24G_CHANNEL_ERROR_TYPE = 4;
    public static final int GO_OPT_24G_CHANNEL_TYPE = 4;
    private static final int GO_OPT_COMMON_5G_CHANNEL_ERROR_TYPE = 3;
    public static final int GO_OPT_COMMON_5G_CHANNEL_TYPE = 3;
    private static final int GO_OPT_DFS_CHANNEL_ERROR_TYPE = 2;
    public static final int GO_OPT_DFS_CHANNEL_TYPE = 2;
    private static final int GO_OPT_INDOOR_CHANNEL_ERROR_TYPE = 1;
    public static final int GO_OPT_INDOOR_CHANNEL_TYPE = 1;
    private static final int GO_OPT_NONE_ERROR_TYPE = 0;
    public static final int GO_OPT_NONE_TYPE = 0;
    private static final int GO_OPT_P2P_SHARING_TYPE = 5;
    public static final int INVALID_VALUE_OR_TYPE = -1;
    private static final long NETWORK_CHECK_INTERVAL = 2000;
    private static final int NETWORK_CHECK_MSG = 1;
    private static final int NETWORK_RECOVERED_MSG = 2;
    private static final int NETWORK_RECOVER_TIMEOUT_MSG = 3;
    private static final int P2PSHARING_CHANNEL_ERROR = 2;
    private static final int P2PSHARING_NETWORK_ERROR = 1;
    private static final int P2PSHARING_UNKNOWN_ERROR = 3;
    private static final long RECOVER_TIMEOUT_MSG_DELAY = 20000;
    public static final int SESSION_ID_LENGTH = 20;
    private static final int SESSION_ID_SAFE_PRINT_LENGTH = 4;
    private static final String TAG = "CastOptChr";
    private static final long THOUSAND = 1000;
    public static final int TRIGGER_TYPE_REGISTER_CALLBACK = 2;
    public static final int TRIGGER_TYPE_SET_WIFIMODE = 1;
    private static CastOptChr sCastOptChr = null;
    private long mCastBeginTimestamp;
    private long mCastOptBeginTimestamp;
    private CastOptDeviceCfg mCastOptDeviceCfg = null;
    private CastOptManager mCastOptManager = null;
    private int mCastTime;
    private Context mContext = null;
    private String mCyCode;
    private int mGcErrCode;
    private int mGcOptType;
    private int mGcOptUseTime;
    private int mGcStaChAft;
    private int mGcStaChBef;
    private int mGoErrCode;
    private int mGoOptType;
    private int mGoStaChAft;
    private int mGoStaChBef;
    private Handler mHandler;
    private int mHasNetAft;
    private int mHasNetBef;
    private HwWifiCHRService mHwWifiCHRService;
    private int mIsGcConnHwAp;
    private int mIsGcDbac;
    private boolean mIsGo;
    private int mIsGoConnHwAp;
    private int mIsGoDbac;
    private boolean mIsP2pHasTvDevice;
    private int mIsP2pSharingSucc;
    private int mIsSameAcct;
    private int mIsUseSyncCfg;
    private int mIsUsrAgreeShare;
    private int mP2pChAft;
    private int mP2pChOrig;
    private long mP2pSharingBeginTimestamp;
    private int mP2pSharingDuration;
    private long mP2pSharingOptBeginTimestamp;
    private int mP2pSharingOptCost;
    private int mP2pSharingRecoverCost;
    private int mRequesetP2pGroupInfoTimes = 0;
    private int mScenes;
    private int mSharingErrCode;
    private int mTrigCsaCnt;
    private int mTriggerType;
    private long mWifiRecoverBeginTimestamp;

    static /* synthetic */ int access$404(CastOptChr x0) {
        int i = x0.mRequesetP2pGroupInfoTimes + 1;
        x0.mRequesetP2pGroupInfoTimes = i;
        return i;
    }

    private CastOptChr(Context context, Looper looper, CastOptManager castOptManager, CastOptDeviceCfg castOptDeviceCfg) {
        this.mCastOptManager = castOptManager;
        this.mCastOptDeviceCfg = castOptDeviceCfg;
        this.mContext = context;
        this.mHwWifiCHRService = HwWifiCHRServiceImpl.getInstance();
        initHandler(looper);
        resetCastOptInfo();
    }

    protected static CastOptChr createCastOptChr(Context context, Looper looper, CastOptManager castOptManager, CastOptDeviceCfg castOptDeviceCfg) {
        if (sCastOptChr == null) {
            sCastOptChr = new CastOptChr(context, looper, castOptManager, castOptDeviceCfg);
        }
        return sCastOptChr;
    }

    protected static String safePrintSessionId(String sessionId) {
        if (sessionId != null && sessionId.length() > 4) {
            return sessionId.substring(0, 4);
        }
        return "";
    }

    public static CastOptChr getInstance() {
        return sCastOptChr;
    }

    public void setTriggerType(int triggerType) {
        if (triggerType == 1 && this.mTriggerType == 2) {
            HwHiLog.i(TAG, false, "setTriggerType, mTriggerType is already set to be REGISTER_CALLBACK", new Object[0]);
        } else {
            this.mTriggerType = triggerType;
        }
    }

    public void notifyP2pGroupCreatedWithTvDevice() {
        HwHiLog.i(TAG, false, "Notified P2p Group Created With Tv Device", new Object[0]);
        this.mIsP2pHasTvDevice = true;
    }

    private int getCastScenes() {
        if (this.mTriggerType == -1) {
            HwHiLog.i(TAG, false, "getCastScenes, mTriggerType is invalid", new Object[0]);
            return -1;
        }
        CastOptDeviceCfg.DeviceWifiInfo selfDeviceInfo = this.mCastOptDeviceCfg.getSelfDeviceCfgInfo();
        if (selfDeviceInfo.getDeviceType() == 2 || this.mIsP2pHasTvDevice) {
            HwHiLog.i(TAG, false, "getCastScenes, it is tv cast scene", new Object[0]);
            return 3;
        }
        CastOptDeviceCfg.DeviceWifiInfo peerDeviceInfo = this.mCastOptDeviceCfg.getPeerDeviceCfgInfo();
        if ((selfDeviceInfo.getDeviceType() == 0 && peerDeviceInfo.getDeviceType() == 1) || (selfDeviceInfo.getDeviceType() == 1 && peerDeviceInfo.getDeviceType() == 0)) {
            HwHiLog.i(TAG, false, "getCastScenes, it is pad cast scene", new Object[0]);
            return 1;
        } else if (peerDeviceInfo.getDeviceType() == 3) {
            HwHiLog.i(TAG, false, "getCastScenes, it is pc cast scene", new Object[0]);
            return 2;
        } else if (this.mTriggerType == 2 && peerDeviceInfo.getDeviceType() == -1) {
            HwHiLog.i(TAG, false, "getCastScenes,registed callback but no peer device type support to be pc scene", new Object[0]);
            return 2;
        } else {
            HwHiLog.e(TAG, false, "getCastScenes, unknown cast scene", new Object[0]);
            return -1;
        }
    }

    public void setIsSameAcct(boolean isSameAcct) {
        this.mIsSameAcct = isSameAcct ? 1 : 0;
    }

    public void setGoCastOptRecommendChannel(int channel) {
        CastOptGoActioner castOptGoActioner = CastOptGoActioner.getInstance();
        if (castOptGoActioner == null) {
            HwHiLog.e(TAG, false, "setGoCastOptRecommendChannel, castOptGoActioner is null", new Object[0]);
            return;
        }
        if (castOptGoActioner.isRadarChannel(channel)) {
            this.mGoOptType = 2;
        } else if (castOptGoActioner.isIndoorChannel(channel)) {
            this.mGoOptType = 1;
        } else if (castOptGoActioner.is5gChannel(channel)) {
            this.mGoOptType = 3;
        } else if (castOptGoActioner.is24gChannel(channel)) {
            this.mGoOptType = 4;
        } else {
            HwHiLog.i(TAG, false, "setGoCastOptRecommendChannel, unknown channel type", new Object[0]);
            this.mGoOptType = -1;
        }
        this.mIsGo = true;
    }

    public void setGcCastOptStaAction(int gcOptType) {
        this.mCastOptBeginTimestamp = SystemClock.elapsedRealtime();
        this.mGcOptType = gcOptType;
    }

    public void handleP2pConnectionStateChanged(NetworkInfo networkInfo) {
        if (networkInfo != null && networkInfo.getDetailedState() == NetworkInfo.DetailedState.FAILED) {
            handleP2pConnectionFail();
        }
    }

    public void handleHwP2pConnectionStateChanged(int state) {
        if (state == 3) {
            handleP2pConnectionFail();
        }
    }

    public static String generateSessionId() {
        String uuidStr = UUID.randomUUID().toString();
        String sessionId = uuidStr.substring(0, 8) + uuidStr.substring(9, 13) + uuidStr.substring(14, 18) + uuidStr.substring(19, 23);
        HwHiLog.i(TAG, false, "generateSessionId :" + safePrintSessionId(sessionId), new Object[0]);
        return sessionId;
    }

    private String getSessionId() {
        return this.mIsGo ? this.mCastOptDeviceCfg.getSelfDeviceCfgInfo().getSessionId() : this.mCastOptDeviceCfg.getPeerDeviceCfgInfo().getSessionId();
    }

    public void handleP2pSharingOptStarted() {
        this.mP2pSharingOptBeginTimestamp = SystemClock.elapsedRealtime();
        this.mGoOptType = 5;
        this.mGcOptType = 5;
    }

    public void handleP2pSharingStarted() {
        if (this.mP2pSharingOptBeginTimestamp == -1) {
            HwHiLog.w(TAG, false, "P2p sharing started but OptBeginTimestamp is invalid", new Object[0]);
            return;
        }
        this.mIsP2pSharingSucc = 1;
        this.mP2pSharingBeginTimestamp = SystemClock.elapsedRealtime();
        this.mP2pSharingOptCost = (int) ((this.mP2pSharingBeginTimestamp - this.mP2pSharingOptBeginTimestamp) / THOUSAND);
    }

    public void handleP2pSharingEnded() {
        this.mWifiRecoverBeginTimestamp = SystemClock.elapsedRealtime();
        if (this.mP2pSharingOptBeginTimestamp != -1) {
            long j = this.mP2pSharingBeginTimestamp;
            if (j != -1) {
                this.mP2pSharingDuration = (int) ((this.mWifiRecoverBeginTimestamp - j) / THOUSAND);
                return;
            }
        }
        HwHiLog.w(TAG, false, "P2p sharing ended but p2p sharing hasn't begun", new Object[0]);
        this.mP2pSharingDuration = -1;
    }

    public void handleP2pSharingFail(int errorCode) {
        if (this.mWifiRecoverBeginTimestamp == -1) {
            handleP2pSharingEnded();
        }
        int i = 1;
        if (errorCode == 1) {
            this.mSharingErrCode = 1;
        } else if (errorCode != 2) {
            this.mSharingErrCode = 3;
        } else {
            this.mSharingErrCode = 2;
        }
        if (this.mP2pSharingDuration <= 0) {
            i = 0;
        }
        this.mIsP2pSharingSucc = i;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkNetwork() {
        if (!Utils.isWiFiConnected(this.mContext)) {
            Handler handler = this.mHandler;
            handler.sendMessageDelayed(handler.obtainMessage(1), NETWORK_CHECK_INTERVAL);
            return;
        }
        this.mP2pSharingRecoverCost = (int) ((SystemClock.elapsedRealtime() - this.mWifiRecoverBeginTimestamp) / THOUSAND);
        if (this.mHandler.hasMessages(3)) {
            this.mHandler.removeMessages(3);
        }
        Handler handler2 = this.mHandler;
        handler2.sendMessage(handler2.obtainMessage(2));
    }

    private void initHandler(Looper looper) {
        this.mHandler = new Handler(looper) {
            /* class com.android.server.wifi.cast.CastOptChr.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                int i = msg.what;
                if (i == 0) {
                    CastOptChr.this.updateP2pGroupInfo();
                } else if (i == 1) {
                    CastOptChr.this.checkNetwork();
                } else if (i == 2 || i == 3) {
                    if (CastOptChr.this.mHandler.hasMessages(1)) {
                        CastOptChr.this.mHandler.removeMessages(1);
                    }
                    CastOptChr.this.uploadData();
                } else {
                    HwHiLog.e(CastOptChr.TAG, false, "unknown msg", new Object[0]);
                }
            }
        };
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateP2pGroupInfo() {
        WifiP2pManager wifiP2pManager = (WifiP2pManager) this.mContext.getSystemService("wifip2p");
        if (wifiP2pManager == null) {
            HwHiLog.e(TAG, false, "error, wifiP2pManager is null", new Object[0]);
            return;
        }
        Context context = this.mContext;
        wifiP2pManager.requestGroupInfo(wifiP2pManager.initialize(context, context.getMainLooper(), null), new WifiP2pManager.GroupInfoListener() {
            /* class com.android.server.wifi.cast.CastOptChr.AnonymousClass2 */

            @Override // android.net.wifi.p2p.WifiP2pManager.GroupInfoListener
            public void onGroupInfoAvailable(WifiP2pGroup info) {
                if (info == null) {
                    HwHiLog.i(CastOptChr.TAG, false, "updateP2pGroupInfo, WifiP2pGroup is null", new Object[0]);
                    CastOptChr.this.mRequesetP2pGroupInfoTimes = 0;
                } else if (WifiCommonUtils.convertFrequencyToChannelNumber(info.getFrequency()) != CastOptChr.this.mP2pChAft) {
                    HwHiLog.i(CastOptChr.TAG, false, "updateP2pGroupInfo, p2p channel change from %{public}d to %{public}d ", new Object[]{Integer.valueOf(CastOptChr.this.mP2pChAft), Integer.valueOf(info.getFrequency())});
                    CastOptChr.this.mP2pChAft = WifiCommonUtils.convertFrequencyToChannelNumber(info.getFrequency());
                    CastOptChr.this.mRequesetP2pGroupInfoTimes = 0;
                } else if (CastOptChr.access$404(CastOptChr.this) < 20) {
                    CastOptChr.this.mHandler.sendMessageDelayed(CastOptChr.this.mHandler.obtainMessage(0), 100);
                } else {
                    HwHiLog.i(CastOptChr.TAG, false, "updateP2pGroupInfo, mRequesetP2pGroupInfoTimes got max retry times", new Object[0]);
                }
            }
        });
    }

    private void handleP2pConnectionFail() {
        if (!this.mIsGo) {
            HwHiLog.i(TAG, false, "handleP2pConnectionFail, not p2p Go device discard it", new Object[0]);
            resetCastOptInfo();
            return;
        }
        int i = this.mGoOptType;
        if (i == -1 || i == 0) {
            this.mGoErrCode = -1;
        } else if (i == 1) {
            this.mGoErrCode = 1;
        } else if (i == 2) {
            this.mGoErrCode = 2;
        } else if (i == 3) {
            this.mGoErrCode = 3;
        } else if (i != 4) {
            HwHiLog.e(TAG, false, " handleP2pConnectionFail mGoOptType is unknown type", new Object[0]);
        } else {
            this.mGoErrCode = 4;
        }
        if (this.mGoErrCode != -1) {
            uploadData();
        }
    }

    public void handleStaConnectionStateChanged(NetworkInfo info) {
        int i;
        if (info == null || this.mIsGo) {
            HwHiLog.e(TAG, false, " handleStaConnectionStateChanged only process p2p gc device sta disconnect even", new Object[0]);
        } else if (info.isConnected()) {
            if (this.mCastOptBeginTimestamp != -1 && (i = this.mGcOptType) != 0 && i != -1) {
                this.mGcOptUseTime = (int) (SystemClock.elapsedRealtime() - this.mCastOptBeginTimestamp);
                this.mCastOptBeginTimestamp = -1;
                this.mGcErrCode = 0;
            }
        } else if (this.mGcErrCode != 0) {
            int i2 = this.mGcOptType;
            if (i2 == 1) {
                this.mGcErrCode = 1;
            } else if (i2 == 2) {
                this.mGcErrCode = 2;
            } else if (i2 == 3) {
                this.mGcErrCode = 3;
            } else if (i2 != 4) {
                this.mGoErrCode = -1;
            } else {
                this.mGcErrCode = 4;
            }
        }
    }

    public void handleP2pConnected(WifiP2pGroup info) {
        if (!this.mCastOptManager.isCastOptScenes()) {
            HwHiLog.i(TAG, false, "p2p is Connected but isCastOptScenes is false, do not record", new Object[0]);
            resetCastOptInfo();
        } else if (info != null) {
            if (this.mIsGo && !info.isGroupOwner()) {
                HwHiLog.i(TAG, false, "setGoCastOptRecommendChannel set mIsGo to true ,but it turn out to be false", new Object[0]);
            }
            this.mIsGo = info.isGroupOwner();
            this.mP2pChOrig = WifiCommonUtils.convertFrequencyToChannelNumber(info.getFrequency());
            this.mP2pChAft = this.mP2pChOrig;
            this.mCastBeginTimestamp = SystemClock.elapsedRealtime();
            CastOptDeviceCfg.DeviceWifiInfo selfDeviceInfo = this.mCastOptDeviceCfg.getSelfDeviceCfgInfo();
            CastOptDeviceCfg.DeviceWifiInfo peerDeviceInfo = this.mCastOptDeviceCfg.getPeerDeviceCfgInfo();
            this.mGoStaChBef = this.mIsGo ? selfDeviceInfo.getStaChannel() : peerDeviceInfo.getStaChannel();
            this.mGcStaChBef = this.mIsGo ? peerDeviceInfo.getStaChannel() : selfDeviceInfo.getStaChannel();
            this.mIsGoConnHwAp = this.mIsGo ? selfDeviceInfo.getRouterType() : peerDeviceInfo.getRouterType();
            this.mIsGcConnHwAp = this.mIsGo ? peerDeviceInfo.getRouterType() : selfDeviceInfo.getRouterType();
            this.mHasNetBef = selfDeviceInfo.getCurrentApHasInternet() ? 1 : 0;
        }
    }

    public void handleP2pDisconnected() {
        clearUpdateP2pGroupInfo();
        if (this.mCastBeginTimestamp != -1) {
            this.mCastTime = ((int) (SystemClock.elapsedRealtime() - this.mCastBeginTimestamp)) / 1000;
        } else {
            HwHiLog.i(TAG, false, "p2pDisconnected but mCastBeginTimestamp is invalid fail to static cast time", new Object[0]);
        }
        CastOptDeviceCfg.DeviceWifiInfo selfDeviceInfo = this.mCastOptDeviceCfg.getSelfDeviceCfgInfo();
        int selfStaChannel = selfDeviceInfo.getStaChannel();
        CastOptDeviceCfg.DeviceWifiInfo peerDeviceInfo = this.mCastOptDeviceCfg.getPeerDeviceCfgInfo();
        int peerStaChannel = peerDeviceInfo.getStaChannel();
        this.mGoStaChAft = this.mIsGo ? selfStaChannel : peerStaChannel;
        this.mGcStaChAft = this.mIsGo ? peerStaChannel : selfStaChannel;
        this.mHasNetAft = selfDeviceInfo.getCurrentApHasInternet() ? 1 : 0;
        this.mIsGoConnHwAp = this.mIsGo ? selfDeviceInfo.getRouterType() : peerDeviceInfo.getRouterType();
        this.mIsGcConnHwAp = this.mIsGo ? peerDeviceInfo.getRouterType() : selfDeviceInfo.getRouterType();
        this.mIsGoDbac = isDbac(this.mGoStaChAft, this.mP2pChAft) ? 1 : 0;
        this.mIsGcDbac = isDbac(this.mGcStaChAft, this.mP2pChAft) ? 1 : 0;
        if (this.mIsGo || this.mGcOptType != 5 || this.mWifiRecoverBeginTimestamp == -1) {
            uploadData();
            return;
        }
        Handler handler = this.mHandler;
        handler.sendMessageDelayed(handler.obtainMessage(3), RECOVER_TIMEOUT_MSG_DELAY);
        checkNetwork();
    }

    private boolean isDbac(int staChannel, int p2pChannel) {
        if (staChannel == p2pChannel) {
            return false;
        }
        CastOptGoActioner castOptGoActioner = CastOptGoActioner.getInstance();
        if (castOptGoActioner == null) {
            HwHiLog.e(TAG, false, "isDbac, castOptGoActioner is null", new Object[0]);
            return false;
        } else if (castOptGoActioner.is5gChannel(staChannel) && castOptGoActioner.is5gChannel(p2pChannel)) {
            return true;
        } else {
            if (!castOptGoActioner.is24gChannel(staChannel) || !castOptGoActioner.is24gChannel(p2pChannel)) {
                return false;
            }
            return true;
        }
    }

    public void handleCsaAction() {
        clearUpdateP2pGroupInfo();
        this.mTrigCsaCnt++;
        updateP2pGroupInfo();
    }

    public void handleAddGoWifiConfig() {
        this.mIsUseSyncCfg = 1;
    }

    private void clearUpdateP2pGroupInfo() {
        this.mRequesetP2pGroupInfoTimes = 0;
        if (this.mHandler.hasMessages(0)) {
            this.mHandler.removeMessages(0);
        }
    }

    private void resetCastOptInfo() {
        clearUpdateP2pGroupInfo();
        this.mScenes = -1;
        this.mIsGo = false;
        this.mIsSameAcct = 0;
        this.mHasNetBef = -1;
        this.mIsGoConnHwAp = -1;
        this.mIsGcConnHwAp = -1;
        this.mGoStaChBef = -1;
        this.mGcStaChBef = -1;
        this.mGoStaChAft = -1;
        this.mGcStaChAft = -1;
        this.mP2pChOrig = -1;
        this.mP2pChAft = -1;
        this.mIsGoDbac = -1;
        this.mIsGcDbac = -1;
        this.mHasNetAft = -1;
        this.mIsUseSyncCfg = 0;
        this.mGcOptUseTime = -1;
        this.mGoOptType = -1;
        this.mGcOptType = -1;
        this.mGoErrCode = -1;
        this.mGcErrCode = -1;
        this.mTrigCsaCnt = 0;
        this.mCastTime = -1;
        this.mCastBeginTimestamp = -1;
        this.mCastOptBeginTimestamp = -1;
        this.mCastOptDeviceCfg.getSelfDeviceCfgInfo().clearSessionId();
        this.mCastOptDeviceCfg.getPeerDeviceCfgInfo().clearSessionId();
        this.mCastOptDeviceCfg.getPeerDeviceCfgInfo().setDeviceType(-1);
        this.mTriggerType = -1;
        this.mIsP2pHasTvDevice = false;
        this.mIsP2pSharingSucc = -1;
        this.mP2pSharingDuration = -1;
        this.mP2pSharingOptCost = -1;
        this.mP2pSharingRecoverCost = -1;
        this.mIsUsrAgreeShare = -1;
        this.mSharingErrCode = -1;
        this.mP2pSharingOptBeginTimestamp = -1;
        this.mP2pSharingBeginTimestamp = -1;
        this.mWifiRecoverBeginTimestamp = -1;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void uploadData() {
        this.mScenes = getCastScenes();
        if (this.mScenes == -1) {
            HwHiLog.e(TAG, false, "uploadData mScenes is invalid drop it", new Object[0]);
            resetCastOptInfo();
            return;
        }
        if (WifiInjector.getInstance().getClientModeImpl() instanceof HwWifiStateMachine) {
            this.mCyCode = WifiInjector.getInstance().getWifiCountryCode().getCountryCodeSentToDriver();
        }
        Bundle data = new Bundle();
        data.putInt("scenes", this.mScenes);
        data.putString("cyCode", this.mCyCode);
        data.putInt("isGo", this.mIsGo ? 1 : 0);
        data.putInt("isSameAcct", this.mIsSameAcct);
        data.putInt("hasNetBef", this.mHasNetBef);
        data.putInt("isGoConnHwAp", this.mIsGoConnHwAp);
        data.putInt("isGcConnHwAp", this.mIsGcConnHwAp);
        data.putInt("goStaChBef", this.mGoStaChBef);
        data.putInt("gcStaChBef", this.mGcStaChBef);
        data.putInt("goStaChAft", this.mGoStaChAft);
        data.putInt("gcStaChAft", this.mGcStaChAft);
        data.putInt("p2pChOrig", this.mP2pChOrig);
        data.putInt("p2pChAft", this.mP2pChAft);
        data.putInt("isGoDbac", this.mIsGoDbac);
        data.putInt("isGcDbac", this.mIsGcDbac);
        data.putInt("hasNetAft", this.mHasNetAft);
        data.putInt("isUseSyncCfg", this.mIsUseSyncCfg);
        data.putInt("gcOptUseTime", this.mGcOptUseTime);
        data.putInt("goOptType", this.mGoOptType);
        data.putInt("gcOptType", this.mGcOptType);
        data.putInt("goErrCode", this.mGoErrCode);
        data.putInt("gcErrCode", this.mGcErrCode);
        data.putInt("trigCsaCnt", this.mTrigCsaCnt);
        data.putInt("time", this.mCastTime);
        data.putString("sessionId", getSessionId());
        data.putInt("isP2pSharingSucc", this.mIsP2pSharingSucc);
        data.putInt("p2pSharingDuration", this.mP2pSharingDuration);
        data.putInt("p2pSharingOptCost", this.mP2pSharingOptCost);
        data.putInt("p2pSharingRecoverCost", this.mP2pSharingRecoverCost);
        data.putInt("isUsrAgreeShare", this.mIsUsrAgreeShare);
        data.putInt("sharingErrCode", this.mSharingErrCode);
        this.mHwWifiCHRService.uploadDFTEvent(32, data);
        printUploadedData();
        resetCastOptInfo();
    }

    private void printUploadedData() {
        HwHiLog.i(TAG, false, "uploadData : scenes:" + this.mScenes + " isGo:" + this.mIsGo + " isSameAcct:" + this.mIsSameAcct + " hasNetBef:" + this.mHasNetBef + " isGoConnHwAp:" + this.mIsGoConnHwAp + " isGcConnHwAp:" + this.mIsGcConnHwAp + " goStaChBef:" + this.mGoStaChBef + " gcStaChBef:" + this.mGcStaChBef + " goStaChAft:" + this.mGoStaChAft + " gcStaChAft:" + this.mGcStaChAft + " p2pChOrig:" + this.mP2pChOrig + " p2pChAft:" + this.mP2pChAft + " isGoDbac:" + this.mIsGoDbac + " isGcDbac:" + this.mIsGcDbac + " hasNetAft:" + this.mHasNetAft + " isUseSyncCfg:" + this.mIsUseSyncCfg + " gcOptUseTime:" + this.mGcOptUseTime + " goOptType:" + this.mGoOptType + " gcOptType:" + this.mGcOptType + " goErrCode:" + this.mGoErrCode + " gcErrCode:" + this.mGcErrCode + " trigCsaCnt:" + this.mTrigCsaCnt + " time:" + this.mCastTime + " sessionId:" + safePrintSessionId(getSessionId()) + " isP2pSharingSucc:" + this.mIsP2pSharingSucc + " p2pSharingDuration:" + this.mP2pSharingDuration + " p2pSharingOptCost:" + this.mP2pSharingOptCost + " isUsrAgreeShare:" + this.mIsUsrAgreeShare + " p2pSharingRecoverCost:" + this.mP2pSharingRecoverCost + " sharingErrCode:" + this.mSharingErrCode, new Object[0]);
    }
}
