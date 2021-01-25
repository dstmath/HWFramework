package com.huawei.server.rme.hyperhold;

import android.content.Context;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.appactcontrol.AppActConstant;
import java.util.Set;

public final class Swap {
    private static final String TAG = "SWAP_SWAP";
    private static volatile Swap swap = null;
    private AppModel appModel = null;
    private volatile boolean isInit = false;
    private volatile boolean isInitCalled = false;
    private KernelEventReceiver kernelEventReceiver = null;
    private KernelInterface kernelInterface = null;
    private KillDecision killDecision = null;
    private ParaConfig paraConfig = null;
    private PersistingData persistingData = null;
    private int rootAppScore;
    private int rootReclaimRatio;
    private int rootReclaimRefault;
    private int rootSoftLimit;
    private int rootSwapRatio;
    private SceneProcessing sceneProcessing = null;
    private Statistics statistics = null;

    private Swap() {
        Slog.i(TAG, "swap controller created");
    }

    private boolean initJni() {
        try {
            System.loadLibrary("syshyperhold_jni");
            return true;
        } catch (UnsatisfiedLinkError e) {
            Slog.e(TAG, "libsyshyperhold_jni library not found!");
            return false;
        }
    }

    private void setProp(boolean isEnable) {
        String prop = "0";
        if (isEnable) {
            prop = "1";
        }
        SystemProperties.set("persist.sys.hyperhold.swapout.enable", prop);
    }

    public void setSwapEnable(boolean swapEnable) {
        if (!swapEnable || !ParaConfig.getInstance().getSwapOutSwitch() || !SystemProperties.get("persist.sys.hyperhold.swapout.enable.cfg").equals(AppActConstant.VALUE_TRUE)) {
            setProp(false);
            Slog.i(TAG, "setSwapEnable switch off because of the REDLINE, swapEnable or config");
            return;
        }
        setProp(swapEnable);
        Slog.i(TAG, "setSwapEnable switch on by judging the REDLINE, swapEnable and config");
    }

    private void getInstances() {
        this.paraConfig = ParaConfig.getInstance();
        this.appModel = AppModel.getInstance();
        this.sceneProcessing = SceneProcessing.getInstance();
        this.killDecision = KillDecision.getInstance();
        this.kernelInterface = KernelInterface.getInstance();
        this.kernelEventReceiver = KernelEventReceiver.getInstance();
        this.statistics = Statistics.getInstance();
        this.persistingData = PersistingData.getInstance();
    }

    public void init(Context context) {
        Slog.i(TAG, "swap controller init start.");
        if (initJni()) {
            if (isSwapModuleEnable()) {
                BufferProc.getInstance().init();
                if (ParaConfig.getInstance().getSwapOutSwitch()) {
                    setProp(true);
                    Slog.i(TAG, "nandlife swapOut now is opened");
                } else {
                    setProp(false);
                    Slog.e(TAG, "nandlife swapOut will not open");
                }
                NandLife.getInstance().init();
                getInstances();
                AppModel appModel2 = this.appModel;
                if (appModel2 != null) {
                    appModel2.init();
                }
                ParaConfig paraConfig2 = this.paraConfig;
                if (paraConfig2 != null) {
                    paraConfig2.writeZramCriticalThres();
                }
                setRootMemcgPara();
                Statistics statistics2 = this.statistics;
                if (statistics2 != null) {
                    statistics2.init();
                }
                KillDecision killDecision2 = this.killDecision;
                if (killDecision2 != null) {
                    killDecision2.init(context);
                }
                SceneProcessing sceneProcessing2 = this.sceneProcessing;
                if (sceneProcessing2 != null) {
                    sceneProcessing2.init(context);
                }
                KernelEventReceiver kernelEventReceiver2 = this.kernelEventReceiver;
                if (kernelEventReceiver2 != null) {
                    kernelEventReceiver2.init();
                }
                PersistingData persistingData2 = this.persistingData;
                if (persistingData2 != null) {
                    persistingData2.init(context);
                }
                setAppScoreAndRatioBeforeServiceStart();
                Slog.i(TAG, "swap controller initalized.");
                this.isInit = true;
            } else {
                handleSwapDisable(context);
                Slog.i(TAG, "swap controller not initalized.");
            }
            HyperHoldCfgUpdateReceiver.checkSwapFromHWOUC();
            this.isInitCalled = true;
        }
    }

    private void handleSwapDisable(Context context) {
        if (SystemProperties.get("persist.sys.hyperhold.swapout.enable.cfg").equals(AppActConstant.VALUE_TRUE)) {
            NandLife.getInstance().init();
        }
        AliveReport aliveReport = AliveReport.getInstance();
        if (aliveReport != null) {
            aliveReport.init(context);
        }
    }

    private void setRootMemcgPara() {
        ParaConfig paraConfig2;
        if (this.kernelInterface != null && (paraConfig2 = this.paraConfig) != null) {
            this.rootSoftLimit = paraConfig2.getRootMemcgParam().getRootSoftLimit();
            this.rootAppScore = this.paraConfig.getRootMemcgParam().getRootAppScore();
            this.rootReclaimRatio = this.paraConfig.getRootMemcgParam().getRootReclaimRatio();
            this.rootSwapRatio = this.paraConfig.getRootMemcgParam().getRootSwapRatio();
            this.rootReclaimRefault = this.paraConfig.getRootMemcgParam().getRootReclaimRefault();
            this.kernelInterface.setSoftLimit(this.rootSoftLimit);
            this.kernelInterface.setRootAppScore(this.rootAppScore);
            this.kernelInterface.setRootRatio(this.rootReclaimRatio, this.rootSwapRatio, this.rootReclaimRefault);
        }
    }

    private boolean isSwapModuleEnable() {
        boolean isEnable = SystemProperties.get("persist.sys.hyperhold.permanently.closed").equals("0");
        if (!isEnable) {
            BufferProc.getInstance().disableZswapd();
            Slog.i(TAG, "hyperhold will close");
        }
        return isEnable;
    }

    public boolean isSwapEnabledBeforeInit() {
        return !this.isInitCalled;
    }

    public boolean isSwapEnabled() {
        return this.isInit;
    }

    public boolean isAkDebug() {
        return ParaConfig.getInstance().getAdvancedKillParam().getAkEnableDebug();
    }

    public static Swap getInstance() {
        if (swap == null) {
            synchronized (Swap.class) {
                if (swap == null) {
                    swap = new Swap();
                }
            }
        }
        return swap;
    }

    public void setAppScoreAndRatioBeforeServiceStart() {
        Set<String> pkgSet = this.sceneProcessing.getPkgNameSet();
        if (pkgSet != null && pkgSet.size() > 0) {
            for (String curPkg : pkgSet) {
                this.kernelInterface.setScore(curPkg, this.rootAppScore);
                this.kernelInterface.writeRatioNew(curPkg, this.rootReclaimRatio, this.rootSwapRatio, this.rootReclaimRefault);
                Slog.i(TAG, "AMS setScore! pkgName:" + curPkg);
            }
        }
    }
}
