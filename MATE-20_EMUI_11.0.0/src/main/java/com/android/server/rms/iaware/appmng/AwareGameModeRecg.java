package com.android.server.rms.iaware.appmng;

import android.app.KeyguardManager;
import android.content.Context;
import android.rms.iaware.AwareLog;
import com.android.server.input.InputManagerServiceEx;
import com.android.server.mtm.MultiTaskManagerService;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;
import java.util.concurrent.ConcurrentHashMap;

public final class AwareGameModeRecg {
    private static final int GAME_MODE_FOREGROUND = 9;
    private static final int GAME_MODE_INVALID = 9;
    private static final int GAME_MODE_OFF = 0;
    private static final int GAME_MODE_ON = 1;
    private static final Object LOCK = new Object();
    private static final String TAG = "RMS.AwareGameModeRecg";
    private static AwareGameModeRecg awareGameModeRecg = null;
    private int gameModeCache = 9;
    private Context mContext = null;
    private InputManagerServiceEx mInputManagerService = null;
    private KeyguardManager mKeyguardManager = null;
    private MultiTaskManagerService mMtmService = null;

    private AwareGameModeRecg() {
    }

    public static AwareGameModeRecg getInstance() {
        AwareGameModeRecg awareGameModeRecg2;
        synchronized (LOCK) {
            if (awareGameModeRecg == null) {
                awareGameModeRecg = new AwareGameModeRecg();
            }
            awareGameModeRecg2 = awareGameModeRecg;
        }
        return awareGameModeRecg2;
    }

    public void setInputManagerService(InputManagerServiceEx inputManagerService) {
        this.mInputManagerService = inputManagerService;
    }

    public void doGameModeRecg(ConcurrentHashMap<Integer, AwareIntelligentRecg.PidInfo> pidInfos) {
        synchronized (LOCK) {
            if (pidInfos != null) {
                for (AwareIntelligentRecg.PidInfo pidInfo : pidInfos.values()) {
                    if (pidInfo.getPkgAndType() != null && pidInfo.getPkgAndType().containsValue(9)) {
                        AwareLog.d(TAG, "GameModeAccurate doGameModeRecg : 1");
                        setGameModeAfterMask(1);
                        return;
                    }
                }
                AwareLog.d(TAG, "GameModeAccurate doGameModeRecg : 0");
                setGameModeAfterMask(0);
            }
        }
    }

    private boolean getKeyLockStatus() {
        Context context;
        MultiTaskManagerService multiTaskManagerService;
        if (this.mMtmService == null) {
            this.mMtmService = MultiTaskManagerService.self();
        }
        if (this.mContext == null && (multiTaskManagerService = this.mMtmService) != null) {
            this.mContext = multiTaskManagerService.context();
        }
        if (this.mKeyguardManager == null && (context = this.mContext) != null) {
            Object manger = context.getSystemService("keyguard");
            if (manger instanceof KeyguardManager) {
                this.mKeyguardManager = (KeyguardManager) manger;
            }
        }
        KeyguardManager keyguardManager = this.mKeyguardManager;
        if (keyguardManager == null) {
            return false;
        }
        return keyguardManager.isKeyguardLocked();
    }

    private void setGameModeAfterMask(int gameMode) {
        boolean setFlag = false;
        if (getKeyLockStatus()) {
            AwareLog.d(TAG, "GameModeAccurate setGameModeAfterMask (Masked): 0");
            this.mInputManagerService.setIawareGameModeAccurate(0);
            this.gameModeCache = 0;
            return;
        }
        if (gameMode != this.gameModeCache) {
            setFlag = true;
        }
        if (setFlag) {
            AwareLog.d(TAG, "GameModeAccurate setGameModeAfterMask : " + gameMode);
            this.mInputManagerService.setIawareGameModeAccurate(gameMode);
            this.gameModeCache = gameMode;
        }
    }
}
