package com.android.server.rms.iaware.appmng.game;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareLog;
import android.rms.iaware.IAwareCMSManager;
import android.rms.iaware.IAwareSdkCore;
import com.android.server.rms.algorithm.AwareUserHabit;
import com.android.server.rms.iaware.memory.utils.MemoryUtils;
import com.huawei.internal.os.BackgroundThreadEx;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class AwareGameStatus {
    private static final String APP_MNG_FEATURE = "appmng_feature";
    private static final String DEFAULT_GAME_NAME = "all_game";
    private static final String GAME_NAME_PROP = "gameName";
    private static final String GAME_PROTECT = "GameProtect";
    private static final String GAME_PROTECT_SWITCH = "gameProtectSwitch";
    private static final int GAME_REG_MAX_RETRY_CNT = 20;
    private static final int GAME_REG_RETRY_STEP = 5000;
    private static final String GAME_SCENE = "GameScene";
    private static final String GAME_SCENE_PROP = "scene";
    private static final Object LOCK = new Object();
    private static final int MSG_APP_UPDATE = 0;
    private static final int MSG_RETRY_REGIST = 1;
    private static final String REG_ARROW = "->";
    private static final String REG_SEMI = ";";
    private static final String REG_STR = "RES:";
    private static final String SWITCH_OPEN = "1";
    private static final String TAG = "AwareGameStatus";
    private static AwareGameStatus sAwareGameStatus = null;
    private static boolean sEnabled = false;
    private String defaultScene;
    private Map<String, AwareGameInfo> gameInfoMap;
    private int gameRegRetryCnt;
    private Map<String, String> gameSceneMap;
    private boolean gameStautsEnable;
    private boolean isGameRegSucc;
    private AwareGameStatusCallback mGameCallback;
    private MyHandler mHandler;
    private AtomicBoolean mIsInitialized;

    private AwareGameStatus() {
        this.isGameRegSucc = false;
        this.gameStautsEnable = false;
        this.gameRegRetryCnt = 0;
        this.defaultScene = SWITCH_OPEN;
        this.gameInfoMap = null;
        this.gameSceneMap = null;
        this.mGameCallback = null;
        this.mIsInitialized = new AtomicBoolean(false);
        this.isGameRegSucc = false;
        this.gameStautsEnable = false;
        this.gameRegRetryCnt = 0;
        this.defaultScene = SWITCH_OPEN;
        this.gameInfoMap = new HashMap();
        this.gameSceneMap = new HashMap();
        this.mGameCallback = new AwareGameStatusCallback();
        this.mIsInitialized = new AtomicBoolean(false);
    }

    public static AwareGameStatus getInstance() {
        AwareGameStatus awareGameStatus;
        synchronized (LOCK) {
            if (sAwareGameStatus == null) {
                sAwareGameStatus = new AwareGameStatus();
            }
            awareGameStatus = sAwareGameStatus;
        }
        return awareGameStatus;
    }

    public static void enable() {
        getInstance().initialize();
        sEnabled = true;
    }

    public static void disable() {
        sEnabled = false;
        getInstance().deInitialize();
    }

    private void initialize() {
        if (!this.mIsInitialized.get()) {
            loadConfig();
            if (this.gameStautsEnable) {
                Looper looper = BackgroundThreadEx.getLooper();
                if (looper != null) {
                    this.mHandler = new MyHandler(looper);
                    getInstance().registerAllGameCallbacks();
                } else {
                    return;
                }
            }
            this.mIsInitialized.set(true);
        }
    }

    private synchronized void deInitialize() {
        if (this.mIsInitialized.get()) {
            synchronized (this.gameInfoMap) {
                this.gameInfoMap.clear();
            }
            this.gameSceneMap.clear();
            this.mIsInitialized.set(false);
        }
    }

    private void loadConfig() {
        loadSwitchCfg();
        loadSceneCfg();
    }

    private void loadSceneCfg() {
        AwareConfig configList = getConfig(APP_MNG_FEATURE, GAME_SCENE);
        if (configList == null) {
            this.gameStautsEnable = false;
            return;
        }
        List<AwareConfig.Item> itemList = configList.getConfigList();
        if (itemList == null || itemList.size() == 0) {
            this.gameStautsEnable = false;
            return;
        }
        for (AwareConfig.Item item : itemList) {
            if (item == null || item.getProperties() == null) {
                AwareLog.d(TAG, "load scene config continue cause null item");
            } else {
                Map<String, String> configPropertries = item.getProperties();
                String gamePkg = configPropertries.get(GAME_NAME_PROP);
                String gameScene = configPropertries.get("scene");
                if (gamePkg.equals(DEFAULT_GAME_NAME)) {
                    this.defaultScene = gameScene;
                }
                this.gameSceneMap.put(gamePkg, gameScene);
            }
        }
    }

    private String getGameRegStr() {
        String regGameStr = REG_STR;
        Map<String, String> map = this.gameSceneMap;
        if (map == null || map.isEmpty()) {
            return regGameStr;
        }
        for (Map.Entry<String, String> entry : this.gameSceneMap.entrySet()) {
            regGameStr = regGameStr + entry.getKey() + REG_ARROW + entry.getValue() + ";";
        }
        return regGameStr;
    }

    private void loadSwitchCfg() {
        AwareConfig.Item curMemItem;
        AwareConfig configList = getConfig(APP_MNG_FEATURE, GAME_PROTECT);
        if (configList != null && (curMemItem = MemoryUtils.getCurrentMemItem(configList, true)) != null) {
            loadDataFromCurMemItem(curMemItem);
        }
    }

    private AwareConfig getConfig(String featureName, String configName) {
        try {
            IBinder awareService = IAwareCMSManager.getICMSManager();
            if (awareService != null) {
                return IAwareCMSManager.getConfig(awareService, featureName, configName);
            }
            AwareLog.i(TAG, "can not find service awareService.");
            return null;
        } catch (RemoteException e) {
            AwareLog.e(TAG, "appmng feature getConfig RemoteException");
            return null;
        }
    }

    private void loadDataFromCurMemItem(AwareConfig.Item item) {
        for (AwareConfig.SubItem subItem : item.getSubItemList()) {
            if (subItem != null) {
                String itemName = subItem.getName();
                String itemValue = subItem.getValue();
                if (!(itemName == null || itemValue == null)) {
                    char c = 65535;
                    if (itemName.hashCode() == -1177534447 && itemName.equals(GAME_PROTECT_SWITCH)) {
                        c = 0;
                    }
                    if (c == 0) {
                        this.gameStautsEnable = itemValue.trim().equals(SWITCH_OPEN);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void registerAllGameCallbacks() {
        int i;
        String regStr = getGameRegStr();
        this.isGameRegSucc = registerGameCallback(regStr, this.mGameCallback);
        AwareLog.d(TAG, "regist game sdk:" + regStr + ", " + this.isGameRegSucc);
        if (!this.isGameRegSucc && (i = this.gameRegRetryCnt) <= 20) {
            this.gameRegRetryCnt = i + 1;
            this.mHandler.sendEmptyMessageDelayed(1, 5000);
        }
    }

    private boolean registerGameCallback(String packageName, AwareGameStatusCallback mGameCallback2) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeString(packageName);
        data.writeStrongBinder(mGameCallback2);
        IAwareSdkCore.handleEvent(4, data, reply);
        int ret = reply.readInt();
        reply.recycle();
        data.recycle();
        return ret > 0;
    }

    public boolean isGaming(String pkgName) {
        AwareGameInfo gameInfo;
        if (pkgName == null || !this.gameStautsEnable || (gameInfo = this.gameInfoMap.get(pkgName)) == null) {
            return false;
        }
        return gameInfo.isGameing();
    }

    public void processGameMsg(String info) {
        if (sEnabled && info != null) {
            AwareGameInfo newGame = AwareGameInfo.initGameInfo(info);
            AwareLog.d(TAG, "game info:" + newGame);
            synchronized (this.gameInfoMap) {
                for (String gamePkg : this.gameInfoMap.keySet()) {
                    if (gamePkg.equals(newGame.packageName)) {
                        this.gameInfoMap.get(gamePkg).scence = newGame.scence;
                        return;
                    }
                }
                this.gameInfoMap.put(newGame.packageName, newGame);
            }
        }
    }

    public void reportAppUpdate(int eventId, Bundle args) {
        MyHandler myHandler;
        if (this.mIsInitialized.get() && args != null && (myHandler = this.mHandler) != null) {
            Message msg = myHandler.obtainMessage();
            msg.what = 0;
            msg.arg1 = eventId;
            msg.setData(args);
            this.mHandler.sendMessage(msg);
        }
    }

    public String getSceneIndex(String pkgName) {
        for (String gamePkg : this.gameSceneMap.keySet()) {
            if (gamePkg.equals(pkgName)) {
                return this.gameSceneMap.get(gamePkg);
            }
        }
        return this.defaultScene;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlerForAppUpdate(Message msg) {
        String pkgName;
        Bundle args = msg.getData();
        if (args != null && msg.arg1 == 2 && (pkgName = args.getString(AwareUserHabit.USER_HABIT_PACKAGE_NAME)) != null) {
            clearDataForPkg(pkgName);
        }
    }

    private void clearDataForPkg(String pkgName) {
        synchronized (this.gameInfoMap) {
            this.gameInfoMap.remove(pkgName);
        }
    }

    /* access modifiers changed from: private */
    public class MyHandler extends Handler {
        public MyHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg == null) {
                AwareLog.w(AwareGameStatus.TAG, "msg is null");
                return;
            }
            int i = msg.what;
            if (i == 0) {
                AwareGameStatus.this.handlerForAppUpdate(msg);
            } else if (i == 1) {
                AwareGameStatus.this.registerAllGameCallbacks();
            }
        }
    }
}
