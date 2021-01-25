package com.android.server.hidata.appqoe;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SystemClock;
import com.huawei.displayengine.IDisplayEngineService;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

public class HwAPPQoEGameCallback extends Binder implements IInterface {
    private static final long CACHE_WAR_TIME = 60000;
    public static final String GAME_SDK_INDEX_PKG = "pkg";
    public static final String GAME_SDK_INDEX_RTT = "7";
    public static final String GAME_SDK_INDEX_SCENCE = "1";
    public static final String GAME_SDK_INDEX_STATE = "10001";
    public static final String GAME_SDK_INDEX_UID = "uid";
    public static final String GAME_SDK_SCENCE_DEFAULT = "0";
    public static final String GAME_SDK_SCENCE_IN_WAR = "7";
    public static final String GAME_SDK_SCENCE_LOGINING = "3";
    public static final String GAME_SDK_SCENCE_MAIN_VIEW = "4";
    public static final String GAME_SDK_SCENCE_OTHER_LOADING = "6";
    public static final String GAME_SDK_SCENCE_SELF_LOADING = "5";
    public static final String GAME_SDK_SCENCE_STARTING = "1";
    public static final String GAME_SDK_SCENCE_UPDATEING = "2";
    public static final String GAME_SDK_STATE_BACKGROUND = "3";
    public static final String GAME_SDK_STATE_DEFAULT = "0";
    public static final String GAME_SDK_STATE_DIE = "5";
    public static final String GAME_SDK_STATE_FORGROUND = "4";
    public static final String HYXD_SDK_SCENCE_ON_ABOARD = "100";
    public static final String HYXD_SDK_SCENCE_ON_JUMP = "101";
    public static final String HYXD_SDK_SCENCE_ON_LANDED = "102";
    public static final String JDQS_SDK_SCENCE_ON_LANDED = "103";
    private static final String SDK_CALLBACK_DESCRIPTOR = "com.huawei.iaware.sdk.ISDKCallbak";
    public static final String SGAME_SDK_INDEX_RTT = "12";
    public static final String SGAME_SDK_INDEX_SCENCE = "4";
    private static final String TAG = "HiData_HwAPPQoEGameCallback";
    private static final int TRANSACTION_updatePhoneInfo = 1;
    private static long sCacheWarTime = 0;
    private static int sCacheWarUid = 0;
    private HwAPPStateInfo lastAPPStateInfo = new HwAPPStateInfo();
    private List<GameStateInfo> mGamStateInfoList = new ArrayList();
    private final Object mGameListLock = new Object();
    private HwAPPQoEResourceManger mHwAPPQoEResourceManger = null;

    public HwAPPQoEGameCallback() {
        attachInterface(this, SDK_CALLBACK_DESCRIPTOR);
        this.mHwAPPQoEResourceManger = HwAPPQoEResourceManger.getInstance();
    }

    @Override // android.os.Binder
    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (code < 1 || code > 16777215) {
            return super.onTransact(code, data, reply, flags);
        }
        if (code != 1 || data == null) {
            return false;
        }
        try {
            data.enforceInterface(SDK_CALLBACK_DESCRIPTOR);
            resolveCallBackData(data.readString());
            return true;
        } catch (SecurityException e) {
            HwAPPQoEUtils.logD(TAG, false, "onTransact, SecurityException", new Object[0]);
            return false;
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x010b  */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x0111  */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x0123  */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x012b  */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x0133  */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x013e  */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x0150  */
    public void resolveCallBackData(String info) {
        String tempScence;
        char c;
        HwAPPQoEUtils.logD(TAG, false, "resolveCallBackData, info: %{public}s", info);
        if (info == null) {
            HwAPPQoEUtils.logD(TAG, false, "resolveCallBackData, invalid info", new Object[0]);
            return;
        }
        try {
            JSONObject jsonStr = new JSONObject(info);
            String mPackageName = jsonStr.optString("pkg", HwAPPQoEUtils.INVALID_STRING_VALUE);
            GameStateInfo mGameStateInfo = getGameStateByName(mPackageName);
            if (mGameStateInfo == null) {
                HwAPPQoEUtils.logD(TAG, false, "resolveCallBackData, game not configed", new Object[0]);
            } else if (mGameStateInfo.getGameSpecialInfoSources() != 1) {
                HwAPPQoEUtils.logE(TAG, false, "not SDK game", new Object[0]);
            } else {
                String tempState = jsonStr.optString(GAME_SDK_INDEX_STATE, "0");
                mGameStateInfo.curUID = jsonStr.optInt("uid", mGameStateInfo.curUID);
                char c2 = 65535;
                if (mPackageName.equals("com.tencent.tmgp.sgame")) {
                    tempScence = jsonStr.optString("4", "0");
                    mGameStateInfo.curRTT = jsonStr.optInt("12", -1);
                } else {
                    tempScence = jsonStr.optString("1", "0");
                    mGameStateInfo.curRTT = jsonStr.optInt("7", -1);
                }
                HwAPPQoEUtils.logD(TAG, false, "resolveCallBackData, scence: %{public}s, state:%{public}s, tempRTT: %{public}d", tempScence, tempState, Integer.valueOf(mGameStateInfo.curRTT));
                mGameStateInfo.prevScence = mGameStateInfo.curScence;
                int hashCode = tempScence.hashCode();
                if (hashCode != 48) {
                    switch (hashCode) {
                        case 53:
                            if (tempScence.equals("5")) {
                                c = 0;
                                break;
                            }
                            break;
                        case IDisplayEngineService.DE_ACTION_MOTION_SWAP /* 54 */:
                            if (tempScence.equals("6")) {
                                c = 1;
                                break;
                            }
                            break;
                        case 55:
                            if (tempScence.equals("7")) {
                                c = 2;
                                break;
                            }
                            break;
                        default:
                            switch (hashCode) {
                                case 48625:
                                    if (tempScence.equals(HYXD_SDK_SCENCE_ON_ABOARD)) {
                                        c = 3;
                                        break;
                                    }
                                    break;
                                case 48626:
                                    if (tempScence.equals(HYXD_SDK_SCENCE_ON_JUMP)) {
                                        c = 4;
                                        break;
                                    }
                                    break;
                                case 48627:
                                    if (tempScence.equals(HYXD_SDK_SCENCE_ON_LANDED)) {
                                        c = 5;
                                        break;
                                    }
                                    break;
                                case 48628:
                                    if (tempScence.equals(JDQS_SDK_SCENCE_ON_LANDED)) {
                                        c = 6;
                                        break;
                                    }
                                    break;
                            }
                    }
                    switch (c) {
                        case 0:
                        case 1:
                        case 2:
                        case 3:
                        case 4:
                        case 5:
                        case 6:
                            mGameStateInfo.curScence = 200002;
                            break;
                        case 7:
                            break;
                        default:
                            mGameStateInfo.curScence = 200001;
                            mGameStateInfo.curRTT = -1;
                            break;
                    }
                    mGameStateInfo.prevState = mGameStateInfo.curState;
                    switch (tempState.hashCode()) {
                        case 51:
                            if (tempState.equals("3")) {
                                c2 = 1;
                                break;
                            }
                            break;
                        case 52:
                            if (tempState.equals("4")) {
                                c2 = 0;
                                break;
                            }
                            break;
                        case 53:
                            if (tempState.equals("5")) {
                                c2 = 2;
                                break;
                            }
                            break;
                    }
                    if (c2 == 0) {
                        mGameStateInfo.curState = 100;
                    } else if (c2 == 1) {
                        mGameStateInfo.curState = 104;
                    } else if (c2 == 2) {
                        mGameStateInfo.curState = 101;
                        mGameStateInfo.curScence = 200001;
                    }
                    handleGameStateChange(mGameStateInfo);
                } else if (tempScence.equals("0")) {
                    c = 7;
                    switch (c) {
                    }
                    mGameStateInfo.prevState = mGameStateInfo.curState;
                    switch (tempState.hashCode()) {
                    }
                    if (c2 == 0) {
                    }
                    handleGameStateChange(mGameStateInfo);
                }
                c = 65535;
                switch (c) {
                }
                mGameStateInfo.prevState = mGameStateInfo.curState;
                switch (tempState.hashCode()) {
                }
                if (c2 == 0) {
                }
                handleGameStateChange(mGameStateInfo);
            }
        } catch (Exception e) {
            HwAPPQoEUtils.logD(TAG, false, "JSONObject error", new Object[0]);
        }
    }

    public void handleGameStateChange(GameStateInfo gameStateInfo) {
        HwAPPStateInfo curAPPStateInfo = new HwAPPStateInfo();
        curAPPStateInfo.mAppId = gameStateInfo.mAppId;
        curAPPStateInfo.mAppType = 2000;
        curAPPStateInfo.mScenceId = gameStateInfo.curScence;
        curAPPStateInfo.mAppUID = gameStateInfo.curUID;
        curAPPStateInfo.mAppRTT = gameStateInfo.curRTT;
        updateGameCacheWarInfo(gameStateInfo.curUID, gameStateInfo.curScence);
        if (-1 != this.lastAPPStateInfo.mAppUID && this.lastAPPStateInfo.mAppUID != gameStateInfo.curUID && 104 == gameStateInfo.curState && gameStateInfo.curState == gameStateInfo.prevState) {
            return;
        }
        if (!this.lastAPPStateInfo.isObjectValueEqual(curAPPStateInfo) || gameStateInfo.prevState != gameStateInfo.curState) {
            int tempState = updateGameStateBeforeSent(gameStateInfo);
            if (tempState != -1 && isCurrentForegroundAppByUid(gameStateInfo.curUID)) {
                HwAPPQoEContentAware.sentNotificationToSTM(curAPPStateInfo, tempState);
            }
            this.lastAPPStateInfo.copyObjectValue(curAPPStateInfo);
            if (101 == gameStateInfo.curState) {
                gameStateInfo.initGameStateInfoPara();
                synchronized (this.mGameListLock) {
                    this.mGamStateInfoList.remove(gameStateInfo);
                }
                this.lastAPPStateInfo = new HwAPPStateInfo();
                return;
            }
            return;
        }
        HwAPPQoEUtils.logD(TAG, false, "handleSgameChange, game state not changed", new Object[0]);
    }

    private void updateGameCacheWarInfo(int uid, int scence) {
        if (scence == 200002) {
            sCacheWarUid = uid;
            sCacheWarTime = SystemClock.elapsedRealtime();
        } else if (uid == sCacheWarUid) {
            sCacheWarUid = 0;
            sCacheWarTime = 0;
        }
    }

    public static boolean isHasGameCacheWarInfo(int uid) {
        if (uid != sCacheWarUid || sCacheWarTime == 0 || SystemClock.elapsedRealtime() - sCacheWarTime > CACHE_WAR_TIME) {
            return false;
        }
        return true;
    }

    private boolean isCurrentForegroundAppByUid(int uid) {
        HwAPPStateInfo appStateInfo;
        HwAPPQoEManager appQoeManager = HwAPPQoEManager.getInstance();
        if (appQoeManager == null || (appStateInfo = appQoeManager.getCurAPPStateInfo()) == null || uid != appStateInfo.mAppUID) {
            return false;
        }
        return true;
    }

    private int updateGameStateBeforeSent(GameStateInfo gameStateInfo) {
        int tempState = gameStateInfo.curState;
        if (100 == gameStateInfo.curState && gameStateInfo.prevState == gameStateInfo.curState) {
            if (gameStateInfo.prevScence != gameStateInfo.curScence) {
                return 102;
            }
            if (200002 == gameStateInfo.curScence) {
                return 105;
            }
            return tempState;
        } else if (104 == gameStateInfo.curState && gameStateInfo.prevState == gameStateInfo.curState) {
            return -1;
        } else {
            return tempState;
        }
    }

    public GameStateInfo getGameStateByName(String packageName) {
        if (packageName == null) {
            return null;
        }
        synchronized (this.mGameListLock) {
            for (GameStateInfo tempGameStateInfo : this.mGamStateInfoList) {
                if (tempGameStateInfo.mPackageName.equals(packageName)) {
                    HwAPPQoEUtils.logD(TAG, false, "getGameStateByName, game found in gamestateinfo list", new Object[0]);
                    return tempGameStateInfo;
                }
            }
        }
        HwAPPQoEGameConfig gameConfig = this.mHwAPPQoEResourceManger.checkIsMonitorGameScence(packageName);
        if (gameConfig == null) {
            return null;
        }
        GameStateInfo tempGameStateInfo2 = new GameStateInfo(gameConfig.mGameId, packageName);
        HwAPPQoEUtils.logD(TAG, false, "getGameStateByName, generate new gamestateinfo instance", new Object[0]);
        tempGameStateInfo2.setGameSpecialInfoSources(gameConfig.getGameSpecialInfoSources());
        synchronized (this.mGameListLock) {
            this.mGamStateInfoList.add(tempGameStateInfo2);
        }
        return tempGameStateInfo2;
    }

    public static class GameStateInfo {
        public int curRTT = -1;
        public int curScence = 200001;
        public int curState = 100;
        public int curUID = -1;
        private int gameSpecialInfoSources = 0;
        public int mAppId;
        public String mPackageName;
        public int prevScence = 200001;
        public int prevState = 100;

        public GameStateInfo(int gameId, String packageName) {
            this.mPackageName = packageName;
            this.mAppId = gameId;
        }

        public void initGameStateInfoPara() {
            this.curScence = 200001;
            this.curRTT = -1;
            this.curUID = -1;
            this.curState = 100;
            this.prevState = 100;
            this.prevScence = 200001;
            this.gameSpecialInfoSources = 0;
        }

        public int getGameSpecialInfoSources() {
            return this.gameSpecialInfoSources;
        }

        public void setGameSpecialInfoSources(int gameSpecialInfoSources2) {
            this.gameSpecialInfoSources = gameSpecialInfoSources2;
        }
    }

    @Override // android.os.IInterface
    public IBinder asBinder() {
        return this;
    }
}
