package com.huawei.android.hwgamesdk;

import android.rms.iaware.AwareLog;
import android.rms.iaware.HwGameManager;
import android.rms.iaware.HwGameManager.GameSDKCallBack;
import com.huawei.android.hwgamesdk.util.VersionInfo;
import huawei.android.widget.DialogContentHelper.Dex;

public class HwGameSDK {
    private static final /* synthetic */ int[] -com-huawei-android-hwgamesdk-HwGameSDK$GameSceneSwitchesValues = null;
    private static final int GAME_IN_SCENE = 4;
    private static final int GAME_SCENE_CHANGE_BEGIN = 2;
    private static final int GAME_SCENE_CHANGE_END = 3;
    private static final int GAME_SCENE_LAUNCH_BEGIN = 0;
    private static final int GAME_SCENE_LAUNCH_END = 1;
    private static final String TAG = "HwGameSDK";
    private GameEngineCallBack gameEngineCbk;
    private GameSDKCallBack gameSDKCbk;
    private boolean isRegistedSuccess;
    private HwGameManager mGameManger;

    public interface GameEngineCallBack {
        void changeContinuousFpsMissedRate(int i, int i2);

        void changeDxFpsRate(int i, float f);

        void changeFpsRate(int i);

        void changeMuteEnabled(boolean z);

        void changeSpecialEffects(int i);

        void queryExpectedFps(int[] iArr, int[] iArr2);
    }

    public enum GameScene {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.hwgamesdk.HwGameSDK.GameScene.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.android.hwgamesdk.HwGameSDK.GameScene.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.hwgamesdk.HwGameSDK.GameScene.<clinit>():void");
        }
    }

    private static /* synthetic */ int[] -getcom-huawei-android-hwgamesdk-HwGameSDK$GameSceneSwitchesValues() {
        if (-com-huawei-android-hwgamesdk-HwGameSDK$GameSceneSwitchesValues != null) {
            return -com-huawei-android-hwgamesdk-HwGameSDK$GameSceneSwitchesValues;
        }
        int[] iArr = new int[GameScene.values().length];
        try {
            iArr[GameScene.GAME_INSCENE.ordinal()] = GAME_SCENE_LAUNCH_END;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[GameScene.GAME_LAUNCH_BEGIN.ordinal()] = GAME_SCENE_CHANGE_BEGIN;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[GameScene.GAME_LAUNCH_END.ordinal()] = GAME_SCENE_CHANGE_END;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[GameScene.GAME_SCENECHANGE_BEGIN.ordinal()] = GAME_IN_SCENE;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[GameScene.GAME_SCENECHANGE_END.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        -com-huawei-android-hwgamesdk-HwGameSDK$GameSceneSwitchesValues = iArr;
        return iArr;
    }

    public HwGameSDK() {
        this.gameSDKCbk = new GameSDKCallBack() {
            public void changeFpsRate(int fps) {
                if (HwGameSDK.this.isRegistedSuccess) {
                    HwGameSDK.this.gameEngineCbk.changeFpsRate(fps);
                }
            }

            public void changeSpecialEffects(int level) {
                if (HwGameSDK.this.isRegistedSuccess) {
                    HwGameSDK.this.gameEngineCbk.changeSpecialEffects(level);
                }
            }

            public void changeMuteEnabled(boolean enabled) {
                if (HwGameSDK.this.isRegistedSuccess) {
                    HwGameSDK.this.gameEngineCbk.changeMuteEnabled(enabled);
                }
            }

            public void changeContinuousFpsMissedRate(int cycle, int maxFrameMissed) {
                if (HwGameSDK.this.isRegistedSuccess) {
                    HwGameSDK.this.gameEngineCbk.changeContinuousFpsMissedRate(cycle, maxFrameMissed);
                }
            }

            public void changeDxFpsRate(int cycle, float maxFrameDx) {
                if (HwGameSDK.this.isRegistedSuccess) {
                    HwGameSDK.this.gameEngineCbk.changeDxFpsRate(cycle, maxFrameDx);
                }
            }

            public void queryExpectedFps(int[] outExpectedFps, int[] outRealFps) {
                if (HwGameSDK.this.isRegistedSuccess) {
                    HwGameSDK.this.gameEngineCbk.queryExpectedFps(outExpectedFps, outRealFps);
                }
            }
        };
        this.isRegistedSuccess = false;
        this.gameEngineCbk = null;
        this.mGameManger = HwGameManager.getInstance();
    }

    public boolean registerGame(String apiVersion, GameEngineCallBack callback) {
        if (callback == null || apiVersion == null || !apiVersion.equals(VersionInfo.getApiVersion())) {
            return false;
        }
        AwareLog.d(TAG, "CocosGame registered");
        this.gameEngineCbk = callback;
        this.isRegistedSuccess = true;
        registerGameSDKCallback();
        return true;
    }

    private void registerGameSDKCallback() {
        if (this.gameSDKCbk != null && this.mGameManger != null) {
            this.mGameManger.setGameSDKCallBack(this.gameSDKCbk);
        }
    }

    public void notifyGameScene(GameScene gameScene, int cpuLevel, int gpuLevel) {
        AwareLog.d(TAG, "notifyGameScene gameScene:" + gameScene + " cpuLevel:" + cpuLevel + " gpuLevel:" + gpuLevel);
        if (this.isRegistedSuccess && this.mGameManger != null) {
            switch (-getcom-huawei-android-hwgamesdk-HwGameSDK$GameSceneSwitchesValues()[gameScene.ordinal()]) {
                case GAME_SCENE_LAUNCH_END /*1*/:
                    this.mGameManger.notifyGameScene(GAME_IN_SCENE, cpuLevel, gpuLevel);
                    break;
                case GAME_SCENE_CHANGE_BEGIN /*2*/:
                    this.mGameManger.notifyGameScene(GAME_SCENE_LAUNCH_BEGIN, cpuLevel, gpuLevel);
                    break;
                case GAME_SCENE_CHANGE_END /*3*/:
                    this.mGameManger.notifyGameScene(GAME_SCENE_LAUNCH_END, cpuLevel, gpuLevel);
                    break;
                case GAME_IN_SCENE /*4*/:
                    this.mGameManger.notifyGameScene(GAME_SCENE_CHANGE_BEGIN, cpuLevel, gpuLevel);
                    break;
                case Dex.DIALOG_BODY_TWO_IMAGES /*5*/:
                    this.mGameManger.notifyGameScene(GAME_SCENE_CHANGE_END, cpuLevel, gpuLevel);
                    break;
            }
        }
    }

    public void notifyContinuousFpsMissed(int cycle, int maxFrameMissed, int times) {
        AwareLog.d(TAG, "notifyContinuousFpsMissed");
        if (this.isRegistedSuccess && this.mGameManger != null) {
        }
    }

    public void notifyFpsDx(int cycle, float maxFrameDx, int frame) {
        AwareLog.d(TAG, "notifyFpsDx");
        if (this.isRegistedSuccess && this.mGameManger != null) {
        }
    }
}
