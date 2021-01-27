package com.huawei.server.rme.hyperhold;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Trace;
import android.util.Slog;
import com.android.internal.os.BackgroundThread;
import com.huawei.server.HwPartIawareUtil;
import com.huawei.server.rme.hyperhold.ParaConfig;
import com.huawei.server.rme.hyperhold.SceneConst;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class AppScore {
    private static final int ACTIVE_SWAP_RATIO = 1;
    private static final int FOREGOUND_SWAP_RATIO = 0;
    private static final int FOREGROUND_SCORE = 0;
    private static final int FREEZE_SWAP_RATIO = 2;
    private static final int FROZE_ACT_SCORE = 999;
    private static final int FROZE_INACT_SCORE = 1000;
    private static final int LIVE_SCORE = 300;
    private static final int MIN_FROZE_ACT_SCORE = 401;
    private static final int MIN_LIVE_SCORE = 1;
    private static final int PERCENTAGE_BASE = 100;
    private static final int PRELOAD_SCORE = 400;
    private static final int SCORE_STEP = 2;
    private static final int SET_RATIO = 1;
    private static final int SET_RATIO_DELAY = 6000;
    private static final int SET_TO_BACKGROUND_RATIO = 2;
    private static final String TAG = "SWAP_AppScore";
    private static volatile AppScore scoreHandler;
    private AppModel appModel;
    private ConcurrentHashMap<String, Integer> bgActiveAppMap;
    private Map<String, String> fastSetAppMap;
    private int freezeTimeThres;
    private int freqUseThres;
    private ConcurrentHashMap<String, Integer> frequentUseAppMap;
    private Handler handler;
    private boolean isScorePolicyEnable;
    private KernelInterface kernelIntf;
    private ParaConfig para;

    private AppScore() {
        this.appModel = null;
        this.kernelIntf = null;
        this.para = null;
        this.isScorePolicyEnable = false;
        this.handler = null;
        this.freqUseThres = 50;
        this.freezeTimeThres = 100;
        this.fastSetAppMap = new ConcurrentHashMap();
        this.frequentUseAppMap = new ConcurrentHashMap<>();
        this.bgActiveAppMap = new ConcurrentHashMap<>();
        this.para = ParaConfig.getInstance();
        this.isScorePolicyEnable = this.para.getOtherParam().getScorePolicyEnable();
        this.appModel = AppModel.getInstance();
        this.kernelIntf = KernelInterface.getInstance();
        this.fastSetAppMap = this.para.getSpecialAppGroupMap();
        this.frequentUseAppMap = this.para.getFrequentUseAppList();
        this.freqUseThres = this.para.getFrequentUseRatio().getFreqUseThres();
        this.bgActiveAppMap = this.para.getBgActiveAppList();
        this.freezeTimeThres = this.para.getBackgroundActiveRatio().getBgActiveThres();
        initHandler();
        Slog.i(TAG, "AppScore init called.");
    }

    public static AppScore getInstance() {
        if (scoreHandler == null) {
            synchronized (AppScore.class) {
                if (scoreHandler == null) {
                    scoreHandler = new AppScore();
                }
            }
        }
        return scoreHandler;
    }

    public void handleDailyCycleMsg() {
        int useDayCount;
        Slog.i(TAG, "handleDailyCycleMsg!");
        Iterator<Map.Entry<String, Integer>> it = this.frequentUseAppMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Integer> entry = it.next();
            String pkgName = entry.getKey();
            int useDayCount2 = entry.getValue().intValue();
            if (useDayCount2 <= 0) {
                it.remove();
            } else {
                if (this.appModel.getDailyUseCount(pkgName) > this.freqUseThres) {
                    useDayCount = useDayCount2 + 1;
                } else {
                    useDayCount = useDayCount2 - 1;
                }
                this.frequentUseAppMap.put(pkgName, Integer.valueOf(useDayCount));
            }
        }
        updateBgActiveListFile();
        this.para.updateFrequentUseAppList(this.frequentUseAppMap);
        this.para.updateBgActiveAppList(this.bgActiveAppMap);
        this.appModel.initAllAppRatioAdjustParam();
    }

    public void handleHourCycleMsg() {
        Slog.i(TAG, "handleHourCycleMsg!");
        updateBgActiveListFile();
        this.para.updateFrequentUseAppList(this.frequentUseAppMap);
        this.para.updateBgActiveAppList(this.bgActiveAppMap);
        this.appModel.initAllAppFreezeTime();
    }

    private void updateBgActiveListFile() {
        int stayInListTime;
        Iterator<Map.Entry<String, Integer>> it = this.bgActiveAppMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Integer> entry = it.next();
            String appName = entry.getKey();
            int stayInListTime2 = entry.getValue().intValue();
            if (stayInListTime2 <= 0) {
                it.remove();
            } else {
                if (this.appModel.getDailyFreezeTime(appName) > this.freezeTimeThres) {
                    stayInListTime = stayInListTime2 + 1;
                } else {
                    stayInListTime = stayInListTime2 - 1;
                }
                this.bgActiveAppMap.put(appName, Integer.valueOf(stayInListTime));
            }
        }
        Slog.i(TAG, "updateBgActiveListFile");
    }

    private void initHandler() {
        Looper looper = HyperHoldServiceThread.getInstance().getLooper();
        if (looper == null) {
            looper = BackgroundThread.get().getLooper();
            Slog.e(TAG, "HyperHold Service Thread get failed, use background looper instead");
        }
        this.handler = new Handler(looper) {
            /* class com.huawei.server.rme.hyperhold.AppScore.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                int i = msg.what;
                if (i == 1) {
                    AppScore.this.handlerSetRatiosMsg(msg.getData());
                } else if (i != 2) {
                    Slog.i(AppScore.TAG, "receive no message!");
                } else {
                    Bundle bundle = msg.getData();
                    if (AppModel.getInstance().getAppNowScore(bundle.getString("appName")) == 300) {
                        AppScore.this.handlerSetRatiosMsg(bundle);
                    }
                }
            }
        };
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlerSetRatiosMsg(Bundle bundle) {
        this.kernelIntf.writeRatioNew(bundle.getString("appName"), bundle.getInt("reclaimRatio"), bundle.getInt("swapRatio"), bundle.getInt("reclaimRefault"));
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.huawei.server.rme.hyperhold.AppScore$2  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$com$huawei$server$rme$hyperhold$SceneConst$SceneEvent = new int[SceneConst.SceneEvent.values().length];

        static {
            try {
                $SwitchMap$com$huawei$server$rme$hyperhold$SceneConst$SceneEvent[SceneConst.SceneEvent.SWITCH_TO_FOREGROUND.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$huawei$server$rme$hyperhold$SceneConst$SceneEvent[SceneConst.SceneEvent.SWITCH_TO_BACKGROUND.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$huawei$server$rme$hyperhold$SceneConst$SceneEvent[SceneConst.SceneEvent.APP_START.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$huawei$server$rme$hyperhold$SceneConst$SceneEvent[SceneConst.SceneEvent.UNFREEZE.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$huawei$server$rme$hyperhold$SceneConst$SceneEvent[SceneConst.SceneEvent.FREEZE.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
        }
    }

    public void setScoreAndRatioByScene(String pkgName, SceneConst.SceneEvent eventType) {
        int i = AnonymousClass2.$SwitchMap$com$huawei$server$rme$hyperhold$SceneConst$SceneEvent[eventType.ordinal()];
        if (i == 1) {
            setScoreAndRatioForeground(pkgName);
        } else if (i == 2) {
            setScoreAndRatioBackground(pkgName);
        } else if (i == 3) {
            setScoreAndRatioLive(pkgName);
        } else if (i == 4) {
            setScoreAndRatioUnfreeze(pkgName);
        } else if (i != 5) {
            Slog.i(TAG, "has other Scene to set score, need check.");
        } else {
            setScoreAndRatioFreeze(pkgName);
        }
    }

    private void setScoreAndRatioForeground(String pkgName) {
        Trace.traceBegin(8, "AppScore::SetForegroundScore:pkg:" + pkgName + " score: 0");
        setScore(pkgName, 0);
        this.appModel.increaseDailyUseCount(pkgName);
        updateNewFrequentUseAppMap(pkgName);
        int reclaimRatio = this.para.getFrontRatioParam().getReclaimRatio();
        int swapRatio = this.para.getFrontRatioParam().getSwapRatio();
        int reclaimRefault = this.para.getFrontRatioParam().getReclaimRefault();
        setAppScoreAndRatios(pkgName, reclaimRatio, swapRatio, reclaimRefault, 0);
        Slog.i(TAG, "setScoreAndRatioForeground::pkgName:" + pkgName + " score:0  write r-Ratio:" + reclaimRatio + " s-Ratio:" + swapRatio + " refault:" + reclaimRefault);
        Trace.traceEnd(8);
    }

    private void setScoreAndRatioBackground(String pkgName) {
        int activeScore = this.appModel.getAppScore(pkgName, 1);
        int preSetScore = 300;
        if (activeScore != -1) {
            preSetScore = activeScore;
        }
        Trace.traceBegin(8, "AppScore::SetBgScore:pkg:" + pkgName + " score: " + preSetScore);
        setReclaimRatioAndSwapRatioWithAppName(pkgName, preSetScore, SET_RATIO_DELAY);
        Trace.traceEnd(8);
    }

    private void setScoreAndRatioLive(String pkgName) {
        if (!this.appModel.getVisibleAppList().contains(pkgName)) {
            int preSetScore = 300;
            if (this.isScorePolicyEnable && (preSetScore = 300 - (new Double(this.appModel.getAppBackgroundFreezeFreq(pkgName)).intValue() * 2)) < 1) {
                preSetScore = 1;
            }
            Trace.traceBegin(8, "AppScore::SetLiveScore:pkg:" + pkgName + " score: " + preSetScore);
            setReclaimRatioAndSwapRatioWithAppName(pkgName, preSetScore, 0);
            Trace.traceEnd(8);
        }
    }

    private void setScoreAndRatioUnfreeze(String pkgName) {
        if (this.appModel.getVisibleAppList().contains(pkgName)) {
            Slog.i(TAG, "setScoreAndRatioUnfreeze::app is in foreground, skip! pkgName:" + pkgName);
            return;
        }
        int preSetScore = 300;
        if (this.isScorePolicyEnable && (preSetScore = 300 - (new Double(this.appModel.getAppBackgroundFreezeFreq(pkgName)).intValue() * 2)) < 1) {
            preSetScore = 1;
        }
        Trace.traceBegin(8, "AppScore::SetUnfreezeScore:pkg:" + pkgName + " score: " + preSetScore);
        setReclaimRatioAndSwapRatioWithAppName(pkgName, preSetScore, 0);
        Trace.traceEnd(8);
    }

    private void setScoreAndRatioFreeze(String pkgName) {
        int preSetScore;
        int unfreezeTimes = new Double(this.appModel.getAppBackgroundFreezeFreq(pkgName)).intValue();
        if (unfreezeTimes == -1) {
            unfreezeTimes = 0;
        }
        if (unfreezeTimes < 2) {
            preSetScore = 1000;
        } else if (this.isScorePolicyEnable) {
            preSetScore = 999 - (unfreezeTimes * 2);
            if (preSetScore < 401) {
                preSetScore = 401;
            }
        } else {
            preSetScore = 999;
            Slog.i(TAG, "freeze and will always active, 999 " + pkgName);
        }
        this.appModel.increaseDailyFreezeTime(pkgName);
        updateNewBgActiveAppMap(pkgName);
        Trace.traceBegin(8, "AppScore::SetFreezeScore:pkg:" + pkgName + " score: " + preSetScore);
        setScore(pkgName, preSetScore, 2);
        RatioParam ratioParam = getFreezeRatioAndRefault(pkgName);
        setAppScoreAndRatios(pkgName, ratioParam.getReclaimRatio(), ratioParam.getSwapRatio(), ratioParam.getReclaimRefault(), 0);
        this.kernelIntf.reclaimCurrentApp(pkgName);
        Slog.i(TAG, "setScoreAndRatioFreeze::pkgName:" + pkgName + " score:" + preSetScore + ratioParam.toString() + " unfreezeTimes:" + unfreezeTimes);
        Trace.traceEnd(8);
    }

    private void setScore(String pkgName, int score) {
        this.kernelIntf.setScore(pkgName, score);
        this.appModel.setNowScore(pkgName, score);
    }

    private void setScore(String pkgName, int score, int scoreType) {
        this.appModel.setAppScore(pkgName, score, scoreType);
        this.kernelIntf.setScore(pkgName, score);
        this.appModel.setNowScore(pkgName, score);
    }

    public void setNewActiveMemcg(ConcurrentHashMap<String, AppInfo> appMaps) {
        for (Map.Entry<String, AppInfo> entry : appMaps.entrySet()) {
            String pkg = entry.getKey();
            AppInfo app = entry.getValue();
            Trace.traceBegin(8, "AppScore::setNewActiveMemcg::pkgName:" + pkg);
            int tmpScore = app.getNowScore();
            if (tmpScore != 0 && tmpScore < 401) {
                int reclaimRefault = this.para.getActiveRatioParam().getReclaimRefault();
                int reclaimRatio = app.getReclaimRatio();
                int swapRatio = app.getSwapRatio();
                int controlSwapRatio = adjustSwapRatio(pkg, 1);
                if (controlSwapRatio != -1) {
                    swapRatio = controlSwapRatio;
                }
                String pkgName = app.getAppName();
                setAppScoreAndRatios(pkgName, reclaimRatio, swapRatio, reclaimRefault, 0);
                Slog.i(TAG, "setNewActiveMemcg::pkgName:" + pkgName + " write r-Ratio:" + reclaimRatio + " s-Ratio: " + swapRatio + " refault:" + reclaimRefault);
                Trace.traceEnd(8);
            }
        }
    }

    private void setReclaimRatioAndSwapRatioWithAppName(String pkgName, int preSetScore, int delay) {
        AppInfo appInfo = this.appModel.getAppInfoByAppName(pkgName);
        if (appInfo != null) {
            setScore(pkgName, preSetScore, 2);
            this.appModel.setNowScore(pkgName, preSetScore);
            int swapRatio = appInfo.getSwapRatio();
            int controlSwapRatio = adjustSwapRatio(pkgName, 1);
            if (controlSwapRatio != -1) {
                swapRatio = controlSwapRatio;
            }
            int reclaimRefault = this.para.getActiveRatioParam().getReclaimRefault();
            setAppScoreAndRatios(pkgName, appInfo.getReclaimRatio(), swapRatio, this.para.getActiveRatioParam().getReclaimRefault(), delay);
            Slog.i(TAG, "setScoreAndRatioLive::pkgName:" + pkgName + " score:" + preSetScore + " write r-Ratio:" + appInfo.getReclaimRatio() + " s-Ratio: " + swapRatio + " refault:" + reclaimRefault);
        }
    }

    private void setAppScoreAndRatios(String pkgName, int reclaimRatio, int swapRatio, int reclaimRefault, int delay) {
        RatioParam finalRatio = adjustRatioBySwapIndex(pkgName, new RatioParam(swapRatio, reclaimRatio, reclaimRefault));
        Bundle bundle = new Bundle();
        bundle.putString("appName", pkgName);
        bundle.putInt("reclaimRatio", finalRatio.getReclaimRatio());
        bundle.putInt("swapRatio", finalRatio.getSwapRatio());
        bundle.putInt("reclaimRefault", reclaimRefault);
        Message msg = this.handler.obtainMessage();
        msg.setData(bundle);
        if (delay == SET_RATIO_DELAY) {
            msg.what = 2;
        } else {
            msg.what = 1;
        }
        this.handler.sendMessageDelayed(msg, (long) delay);
        Slog.i(TAG, "final ratio info: pkgName:" + pkgName + ", reclaime ratio:" + finalRatio.getReclaimRatio() + ", swap ratio:" + finalRatio.getSwapRatio());
    }

    private int getSwapRatioForFastSetApp(String pkgName, int swapRatioType) {
        int swapRatio = -1;
        String groupName = this.fastSetAppMap.get(pkgName);
        if (groupName != null) {
            ParaConfig.SpecialParam param = this.para.getSpecialParamMap().get(groupName);
            if (swapRatioType == 0) {
                swapRatio = param.getFrontSwapRatio();
            } else if (swapRatioType == 1) {
                swapRatio = param.getActiveSwapRatio();
            } else if (swapRatioType != 2) {
                Slog.e(TAG, "getSwapRatioForFastSetApp error!");
            } else {
                swapRatio = param.getFreezeSwapRatio();
            }
            Slog.i(TAG, "getSwapRatioForFastSetApp pkg:" + pkgName + " Ratio:" + swapRatio);
        } else {
            Slog.e(TAG, "getSwapRatioForFastSetAppList error! + pkgName:" + pkgName);
        }
        return swapRatio;
    }

    private void updateNewFrequentUseAppMap(String pkgName) {
        int useTimes = this.appModel.getDailyUseCount(pkgName);
        if (!this.frequentUseAppMap.containsKey(pkgName) && useTimes > this.freqUseThres) {
            this.frequentUseAppMap.put(pkgName, 3);
            Slog.i(TAG, "setNewFrequentUseAppMap success!" + pkgName + " useTimes:" + useTimes);
        }
    }

    private int getSwapRatioForFrequentUseApp(String pkgName, int swapRatioType) {
        int swapRatio = -1;
        if (swapRatioType == 0) {
            swapRatio = this.para.getFrequentUseRatio().getFrontSwapRatio();
        } else if (swapRatioType == 1) {
            swapRatio = this.para.getFrequentUseRatio().getActiveSwapRatio();
        } else if (swapRatioType != 2) {
            Slog.e(TAG, "getSwapRatioForFrequentUseApp error!");
        } else {
            swapRatio = this.para.getFrequentUseRatio().getFreezeSwapRatio();
        }
        Slog.i(TAG, "getSwapRatioForFrequentUseApp + pkgName:" + pkgName + " swapRatio:" + swapRatio);
        return swapRatio;
    }

    private void updateNewBgActiveAppMap(String pkgName) {
        int ret = this.appModel.getDailyFreezeTime(pkgName);
        Slog.i(TAG, "updateNewBgActiveAppMap:freezeTimes:" + ret + " appName:" + pkgName);
        if (ret > this.freezeTimeThres && !this.bgActiveAppMap.containsKey(pkgName)) {
            this.bgActiveAppMap.put(pkgName, 12);
            Slog.i(TAG, "updateNewBgActiveAppMap::add app success!" + pkgName + " freezeTimes:" + ret);
        }
    }

    private int getSwapRatioForBackgroundActiveApp(String pkgName, int swapRatioType) {
        int swapRatio = -1;
        if (swapRatioType == 0) {
            swapRatio = this.para.getBackgroundActiveRatio().getFrontSwapRatio();
        } else if (swapRatioType == 1) {
            swapRatio = this.para.getBackgroundActiveRatio().getActiveSwapRatio();
        } else if (swapRatioType != 2) {
            Slog.e(TAG, "getSwapRatioForBackgroundActiveApp error!");
        } else {
            swapRatio = this.para.getBackgroundActiveRatio().getFreezeSwapRatio();
        }
        Slog.i(TAG, "getSwapRatioForBackgroundActiveApp + pkgName:" + pkgName + " swapRatio:" + swapRatio);
        return swapRatio;
    }

    private int adjustSwapRatio(String pkgName, int swapRatioType) {
        Slog.i(TAG, "adjustSwapRatio:pkg:" + pkgName);
        if (this.fastSetAppMap.containsKey(pkgName)) {
            int swapRatio = getSwapRatioForFastSetApp(pkgName, swapRatioType);
            Slog.i(TAG, "adjustSwapRatio:fastSetAppMap:swapratio" + swapRatio + " pkgName:" + pkgName);
            return swapRatio;
        } else if (this.frequentUseAppMap.containsKey(pkgName)) {
            int swapRatio2 = getSwapRatioForFrequentUseApp(pkgName, swapRatioType);
            Slog.i(TAG, "adjustSwapRatio:frequentUseAppMap:swapratio" + swapRatio2 + " pkgName:" + pkgName);
            return swapRatio2;
        } else if (!this.bgActiveAppMap.containsKey(pkgName)) {
            return -1;
        } else {
            int swapRatio3 = getSwapRatioForBackgroundActiveApp(pkgName, swapRatioType);
            Slog.i(TAG, "adjustSwapRatio:BackgroundActiveApp:swapratio" + swapRatio3 + " pkgName:" + pkgName);
            return swapRatio3;
        }
    }

    private RatioParam getFreezeRatioAndRefault(String appName) {
        Slog.i(TAG, "getFreezeRatioAndRefault, pkg:" + appName);
        int swapRatio = this.para.getFreezeRatioParam().getSwapRatio();
        int reclaimRatio = this.para.getFreezeRatioParam().getReclaimRatio();
        int reclaimRefault = this.para.getFreezeRatioParam().getReclaimRefault();
        int annoRam = this.kernelIntf.getAnnoRamSumByName(appName) / 1000;
        Slog.i(TAG, "getFreezeRatioAndRefault, anno ram:" + annoRam);
        if (annoRam != -1) {
            Iterator<ParaConfig.RatioGroup> it = this.para.getFreezeRatioGroupList().iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                ParaConfig.RatioGroup ratioGroup = it.next();
                if (annoRam >= ratioGroup.getMinRam()) {
                    if (annoRam >= ratioGroup.getMinRam() && annoRam < ratioGroup.getMaxRam()) {
                        swapRatio = ratioGroup.getSwapRatio();
                        reclaimRatio = ratioGroup.getReclaimRatio();
                        reclaimRefault = ratioGroup.getReclaimRefault();
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        int controlSwapRatio = adjustSwapRatio(appName, 2);
        if (controlSwapRatio != -1) {
            swapRatio = controlSwapRatio;
        }
        return new RatioParam(swapRatio, reclaimRatio, reclaimRefault);
    }

    private int getSwapIndexFromIaware(String appName) {
        Slog.i(TAG, "getSwapRatioFromIaware, pkg:" + appName);
        return HwPartIawareUtil.getAppSwapIndex(appName);
    }

    private RatioParam adjustRatioBySwapIndex(String appName, RatioParam origin) {
        Map<Integer, ParaConfig.AdjustRatioGroup> swapIndexGroupMap = this.para.getSwapIndexGroupMap();
        if (swapIndexGroupMap.size() == 0) {
            Slog.i(TAG, "ParaConfig have no swapIndex parameters");
            return origin;
        }
        int swapIndex = getSwapIndexFromIaware(appName);
        int reclaimRatio = origin.getReclaimRatio();
        int swapRatio = origin.getSwapRatio();
        ParaConfig.AdjustRatioGroup adjustRatioGroup = swapIndexGroupMap.get(Integer.valueOf(swapIndex));
        if (adjustRatioGroup != null) {
            swapRatio = (adjustRatioGroup.getSwapAdjustRatio() * swapRatio) / 100;
            reclaimRatio = (adjustRatioGroup.getReclaimAdjustRatio() * reclaimRatio) / 100;
            Slog.i(TAG, "adjust app:" + appName + " with swapIndex:" + swapIndex + ", swapAdjustRatio:" + adjustRatioGroup.getSwapAdjustRatio() + ", reclaimAdjustRatio:" + adjustRatioGroup.getReclaimAdjustRatio());
        } else {
            Slog.i(TAG, "have not fount swapIndex:" + swapIndex + " in swapIndexGroupMap");
        }
        return new RatioParam(swapRatio, reclaimRatio, origin.getReclaimRefault());
    }

    /* access modifiers changed from: private */
    public static class RatioParam {
        private int reclaimRatio;
        private int reclaimRefault;
        private int swapRatio;

        private RatioParam(int swapRatio2, int reclaimRatio2, int reclaimRefault2) {
            this.swapRatio = swapRatio2;
            this.reclaimRatio = reclaimRatio2;
            this.reclaimRefault = reclaimRefault2;
        }

        public int getSwapRatio() {
            return this.swapRatio;
        }

        public int getReclaimRatio() {
            return this.reclaimRatio;
        }

        public int getReclaimRefault() {
            return this.reclaimRefault;
        }

        public String toString() {
            return "[RatioParm] swapRatio:" + this.swapRatio + ", reclaimRatio:" + this.reclaimRatio + ", reclaimRefault:" + this.reclaimRefault;
        }
    }
}
