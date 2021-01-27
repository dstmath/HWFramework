package com.android.server.hidata.appqoe;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SystemClock;
import android.text.TextUtils;
import com.android.server.hidata.arbitration.HwArbitrationDefs;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public class HwAppQoeGameCallback extends Binder implements IInterface {
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
    private static final String TAG = (HwArbitrationDefs.BASE_TAG + HwAppQoeGameCallback.class.getSimpleName());
    private static final int TRANSACTION_UPDATE_PHONE_INFO = 1;
    private static long sCacheWarTime = 0;
    private static int sCacheWarUid = 0;
    private List<GameStateInfo> mGamStateInfoList = new ArrayList();
    private final Object mGameListLock = new Object();
    private HwAppQoeResourceManager mHwAppQoeResourceManger = null;
    private HwAppStateInfo mLastAppStateInfo = new HwAppStateInfo();

    public HwAppQoeGameCallback() {
        attachInterface(this, SDK_CALLBACK_DESCRIPTOR);
        this.mHwAppQoeResourceManger = HwAppQoeResourceManager.getInstance();
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
            HwAppQoeUtils.logD(TAG, false, "onTransact, SecurityException", new Object[0]);
            return false;
        }
    }

    public void resolveCallBackData(String info) {
        String tempScenes;
        if (TextUtils.isEmpty(info)) {
            HwAppQoeUtils.logD(TAG, false, "resolveCallBackData, invalid info", new Object[0]);
            return;
        }
        HwAppQoeUtils.logD(TAG, false, "resolveCallBackData, info: %{public}s", info);
        try {
            JSONObject jsonStr = new JSONObject(info);
            String mPackageName = jsonStr.optString("pkg", HwAppQoeUtils.INVALID_STRING_VALUE);
            GameStateInfo mGameStateInfo = getGameStateByName(mPackageName);
            if (mGameStateInfo == null) {
                HwAppQoeUtils.logD(TAG, false, "resolveCallBackData, game not configed", new Object[0]);
            } else if (mGameStateInfo.getGameSpecialInfoSources() != 1) {
                HwAppQoeUtils.logE(TAG, false, "not SDK game", new Object[0]);
            } else {
                String tempState = jsonStr.optString(GAME_SDK_INDEX_STATE, "0");
                mGameStateInfo.curUid = jsonStr.optInt("uid", mGameStateInfo.curUid);
                if ("com.tencent.tmgp.sgame".equals(mPackageName)) {
                    tempScenes = jsonStr.optString("4", "0");
                    mGameStateInfo.curRtt = jsonStr.optInt("12", -1);
                } else {
                    tempScenes = jsonStr.optString("1", "0");
                    mGameStateInfo.curRtt = jsonStr.optInt("7", -1);
                }
                HwAppQoeUtils.logD(TAG, false, "resolveCallBackData, scenes: %{public}s, state:%{public}s, tempRTT: %{public}d", tempScenes, tempState, Integer.valueOf(mGameStateInfo.curRtt));
                mGameStateInfo.prevScenes = mGameStateInfo.curScenes;
                updateGameScenes(tempScenes, mGameStateInfo);
                mGameStateInfo.prevState = mGameStateInfo.curState;
                updateGameState(tempState, mGameStateInfo);
                handleGameStateChange(mGameStateInfo);
            }
        } catch (JSONException e) {
            HwAppQoeUtils.logD(TAG, false, "JSONObject error", new Object[0]);
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0064  */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x006d  */
    /* JADX WARNING: Removed duplicated region for block: B:34:? A[RETURN, SYNTHETIC] */
    private void updateGameScenes(String tempScenes, GameStateInfo gameStateInfo) {
        char c;
        int hashCode = tempScenes.hashCode();
        if (hashCode != 48) {
            switch (hashCode) {
                case 53:
                    if (tempScenes.equals("5")) {
                        c = 0;
                        break;
                    }
                    break;
                case 54:
                    if (tempScenes.equals("6")) {
                        c = 1;
                        break;
                    }
                    break;
                case 55:
                    if (tempScenes.equals("7")) {
                        c = 2;
                        break;
                    }
                    break;
                default:
                    switch (hashCode) {
                        case 48625:
                            if (tempScenes.equals(HYXD_SDK_SCENCE_ON_ABOARD)) {
                                c = 3;
                                break;
                            }
                            break;
                        case 48626:
                            if (tempScenes.equals(HYXD_SDK_SCENCE_ON_JUMP)) {
                                c = 4;
                                break;
                            }
                            break;
                        case 48627:
                            if (tempScenes.equals(HYXD_SDK_SCENCE_ON_LANDED)) {
                                c = 5;
                                break;
                            }
                            break;
                        case 48628:
                            if (tempScenes.equals(JDQS_SDK_SCENCE_ON_LANDED)) {
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
                    gameStateInfo.curScenes = 200002;
                    return;
                case 7:
                    return;
                default:
                    gameStateInfo.curScenes = 200001;
                    gameStateInfo.curRtt = -1;
                    return;
            }
        } else if (tempScenes.equals("0")) {
            c = 7;
            switch (c) {
            }
        }
        c = 65535;
        switch (c) {
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private void updateGameState(String tempState, GameStateInfo gameStateInfo) {
        char c;
        switch (tempState.hashCode()) {
            case 51:
                if (tempState.equals("3")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 52:
                if (tempState.equals("4")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 53:
                if (tempState.equals("5")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        if (c == 0) {
            gameStateInfo.curState = 100;
        } else if (c == 1) {
            gameStateInfo.curState = 104;
        } else if (c == 2) {
            gameStateInfo.curState = 101;
            gameStateInfo.curScenes = 200001;
        }
    }

    public void handleGameStateChange(GameStateInfo gameStateInfo) {
        HwAppStateInfo curAppStateInfo = new HwAppStateInfo();
        curAppStateInfo.mAppId = gameStateInfo.mAppId;
        curAppStateInfo.mAppType = 2000;
        curAppStateInfo.mScenesId = gameStateInfo.curScenes;
        curAppStateInfo.mAppUid = gameStateInfo.curUid;
        curAppStateInfo.mAppRtt = gameStateInfo.curRtt;
        updateGameCacheWarInfo(gameStateInfo.curUid, gameStateInfo.curScenes);
        if (this.mLastAppStateInfo.mAppUid != -1 && this.mLastAppStateInfo.mAppUid != gameStateInfo.curUid && gameStateInfo.curState == 104 && gameStateInfo.curState == gameStateInfo.prevState) {
            return;
        }
        if (!this.mLastAppStateInfo.isObjectValueEqual(curAppStateInfo) || gameStateInfo.prevState != gameStateInfo.curState) {
            int tempState = updateGameStateBeforeSent(gameStateInfo);
            if (tempState != -1 && isCurrentForegroundAppByUid(gameStateInfo.curUid)) {
                HwAppQoeContentAware.sentNotificationToStm(curAppStateInfo, tempState);
            }
            this.mLastAppStateInfo.copyObjectValue(curAppStateInfo);
            if (gameStateInfo.curState == 101) {
                gameStateInfo.initGameStateInfoPara();
                synchronized (this.mGameListLock) {
                    this.mGamStateInfoList.remove(gameStateInfo);
                }
                this.mLastAppStateInfo = new HwAppStateInfo();
                return;
            }
            return;
        }
        HwAppQoeUtils.logD(TAG, false, "handleGameStateChange, game state not changed", new Object[0]);
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
        HwAppStateInfo appStateInfo;
        HwAppQoeManager appQoeManager = HwAppQoeManager.getInstance();
        if (appQoeManager == null || (appStateInfo = appQoeManager.getCurAppStateInfo()) == null || uid != appStateInfo.mAppUid) {
            return false;
        }
        return true;
    }

    private int updateGameStateBeforeSent(GameStateInfo gameStateInfo) {
        int tempState = gameStateInfo.curState;
        if (gameStateInfo.curState == 100 && gameStateInfo.prevState == gameStateInfo.curState) {
            if (gameStateInfo.prevScenes != gameStateInfo.curScenes) {
                return 102;
            }
            if (gameStateInfo.curScenes == 200002) {
                return 105;
            }
            HwAppQoeUtils.logE(TAG, false, "other GAME_SCENCE", new Object[0]);
            return tempState;
        } else if (gameStateInfo.curState == 104 && gameStateInfo.prevState == gameStateInfo.curState) {
            return -1;
        } else {
            HwAppQoeUtils.logE(TAG, false, "game state changed between start and background/end, do not change.", new Object[0]);
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
                    HwAppQoeUtils.logD(TAG, false, "getGameStateByName, game found in gamestateinfo list", new Object[0]);
                    return tempGameStateInfo;
                }
            }
        }
        HwAppQoeGameConfig gameConfig = this.mHwAppQoeResourceManger.checkIsMonitorGameScenes(packageName);
        if (gameConfig == null) {
            return null;
        }
        GameStateInfo tempGameStateInfo2 = new GameStateInfo(gameConfig.mGameId, packageName);
        HwAppQoeUtils.logD(TAG, false, "getGameStateByName, generate new gamestateinfo instance", new Object[0]);
        tempGameStateInfo2.setGameSpecialInfoSources(gameConfig.getGameSpecialInfoSources());
        synchronized (this.mGameListLock) {
            this.mGamStateInfoList.add(tempGameStateInfo2);
        }
        return tempGameStateInfo2;
    }

    public static class GameStateInfo {
        public int curRtt = -1;
        public int curScenes = 200001;
        public int curState = 100;
        public int curUid = -1;
        private int gameSpecialInfoSources = 0;
        public int mAppId;
        public String mPackageName;
        public int prevScenes = 200001;
        public int prevState = 100;

        public GameStateInfo(int gameId, String packageName) {
            this.mPackageName = packageName;
            this.mAppId = gameId;
        }

        public void initGameStateInfoPara() {
            this.curScenes = 200001;
            this.curRtt = -1;
            this.curUid = -1;
            this.curState = 100;
            this.prevState = 100;
            this.prevScenes = 200001;
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
