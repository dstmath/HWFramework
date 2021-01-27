package com.huawei.server.rme.hyperhold;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.internal.os.BackgroundThread;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.server.rme.hyperhold.AppInfo;
import com.huawei.server.rme.hyperhold.ParaConfig;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class AppModel {
    public static final int ACT_SCORE = 1;
    private static final int APP_DIED = 2;
    private static final int APP_STATE_ACTIVE = 1;
    private static final int APP_STATE_FREEZE = 2;
    private static final int COLD_START = 0;
    private static final int GET_MEMCG_BIG_DATA = 3;
    private static final int GET_MEMCG_SNAPSHOT_MSG = 2;
    private static final int HOT_START = 1;
    public static final int IN_ACT_SCORE = 2;
    private static final Object LOCK_OBJ = new Object();
    private static final int RECLAIM_RATIO_STEP = 2;
    private static final int RECLAIM_RATIO_THRESHOLD = 100;
    private static final int REFAULT_SIZE = 400;
    private static final int REPORT_BIG_DATA_DELAY = 28800000;
    private static final int STATISTIC_LIVE_APP = 4;
    private static final String TAG = "SWAP_AppModel";
    private static final int TIME_COUNT_MSG = 1;
    private static final int TIME_TO_STATIC = 600000;
    private static final int ZSWAPD_MSG_DEALY = 4000;
    private static final int ZSWAPD_REFAULT_THRESHOLD = 10;
    private static volatile AppModel appModelHandler;
    private static ConcurrentHashMap<String, ThreadLocal<SimpleDateFormat>> sdfMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, AppInfo> aliveAppMap = new ConcurrentHashMap<>();
    private int defaultReclaimRatio = 0;
    private int defaultSwapRatio = 0;
    private Handler handler = null;
    private ConcurrentHashMap<String, AppInfo> historyAppMap = new ConcurrentHashMap<>();
    private String lastApp = "";
    private long lastReportAppNameTime = 0;
    private long lastReportLiveTime = 0;
    private String lastSecondApp = "";
    private ExecutorService liveAppCountExecutor = Executors.newSingleThreadExecutor();
    private ConcurrentHashMap<Integer, String> pidPkg = new ConcurrentHashMap<>();
    private double runningTime = 0.0d;
    private ConcurrentHashMap<String, AppInfo> snapShotMapFirst = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, AppInfo> snapShotMapSecond = new ConcurrentHashMap<>();

    static /* synthetic */ double access$018(AppModel x0, double x1) {
        double d = x0.runningTime + x1;
        x0.runningTime = d;
        return d;
    }

    private class LiveAppCountMonitor implements Runnable {
        String appName;

        LiveAppCountMonitor(String appName2) {
            this.appName = appName2;
        }

        @Override // java.lang.Runnable
        public void run() {
            String str = this.appName;
            if (str != null) {
                AppModel.this.setIsFront(str);
            }
        }
    }

    private AppModel() {
    }

    public static AppModel getInstance() {
        if (appModelHandler == null) {
            synchronized (AppModel.class) {
                if (appModelHandler == null) {
                    appModelHandler = new AppModel();
                }
            }
        }
        return appModelHandler;
    }

    public void init() {
        Slog.i(TAG, "AppModel init called!");
        initHandler();
        this.lastReportLiveTime = System.currentTimeMillis();
        ParaConfig.ActiveRatioParam activeRatioParam = ParaConfig.getInstance().getActiveRatioParam();
        this.defaultReclaimRatio = activeRatioParam.getReclaimRatio();
        this.defaultSwapRatio = activeRatioParam.getSwapRatio();
        Message msg = this.handler.obtainMessage();
        msg.what = 1;
        setSdfDayTimeZone();
        this.handler.sendMessageDelayed(msg, Constant.MAX_TRAIN_MODEL_TIME);
        if (Statistics.getInstance().getUserType() == 3) {
            Message msg1 = this.handler.obtainMessage();
            msg1.what = 3;
            this.handler.sendMessageDelayed(msg1, 28800000);
        }
    }

    private static SimpleDateFormat getSdf(final String pattern) {
        SimpleDateFormat simpleDateFormat;
        synchronized (LOCK_OBJ) {
            ThreadLocal<SimpleDateFormat> threadLocalSdf = sdfMap.get(pattern);
            if (threadLocalSdf == null) {
                threadLocalSdf = new ThreadLocal<SimpleDateFormat>() {
                    /* class com.huawei.server.rme.hyperhold.AppModel.AnonymousClass1 */

                    /* access modifiers changed from: protected */
                    @Override // java.lang.ThreadLocal
                    public SimpleDateFormat initialValue() {
                        return new SimpleDateFormat(pattern);
                    }
                };
                sdfMap.put(pattern, threadLocalSdf);
            }
            simpleDateFormat = threadLocalSdf.get();
        }
        return simpleDateFormat;
    }

    private static void setSdfDayTimeZone() {
        getSdf("HH:mm:ss").setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
    }

    private static String sdfFormate(Date date) {
        return getSdf("yyyy-MM-dd HH:mm:ss").format(date);
    }

    private static Date sdfParse(String dateStr) throws ParseException {
        return getSdf("yyyy-MM-dd HH:mm:ss").parse(dateStr);
    }

    private static String sdfFormateDay(long date) {
        return getSdf("HH:mm:ss").format(Long.valueOf(date));
    }

    private static Date sdfParseDay(String dateStr) throws ParseException {
        return getSdf("HH:mm:ss").parse(dateStr);
    }

    private void initHandler() {
        Looper looper = HyperHoldServiceThread.getInstance().getLooper();
        if (looper == null) {
            looper = BackgroundThread.get().getLooper();
            Slog.e(TAG, "HyperHold Service Thread get failed, use background looper instead");
        }
        this.handler = new Handler(looper) {
            /* class com.huawei.server.rme.hyperhold.AppModel.AnonymousClass2 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                int i = msg.what;
                if (i == 1) {
                    AppModel.access$018(AppModel.this, 0.16666666666666666d);
                    Message sendMsg = AppModel.this.handler.obtainMessage();
                    sendMsg.what = 1;
                    AppModel.this.handler.sendMessageDelayed(sendMsg, Constant.MAX_TRAIN_MODEL_TIME);
                    Slog.i(AppModel.TAG, "till now, running " + AppModel.this.runningTime + " hours.");
                } else if (i == 2) {
                    AppModel.this.snapShotMapSecond = KernelInterface.getInstance().getActiveRatioRefault(1);
                    AppModel.this.updateActiveMemcg();
                } else if (i == 3) {
                    Message sendMsg2 = AppModel.this.handler.obtainMessage();
                    sendMsg2.what = 3;
                    AppModel.this.writeBigDataToFile();
                    AppModel.this.handler.sendMessageDelayed(sendMsg2, 28800000);
                    Slog.i(AppModel.TAG, "GET_MEMCG_BIG_DATA");
                } else if (i != 4) {
                    Slog.i(AppModel.TAG, "receive no message!");
                } else if (Statistics.getInstance().getUserType() == 3 && (msg.obj instanceof String)) {
                    AppModel.this.liveAppCountExecutor.execute(new LiveAppCountMonitor((String) msg.obj));
                }
                Slog.i(AppModel.TAG, "finish msg processing!");
            }
        };
    }

    public String getPkgByPid(int pid) {
        if (this.pidPkg.containsKey(Integer.valueOf(pid))) {
            return this.pidPkg.get(Integer.valueOf(pid));
        }
        return "";
    }

    public void notifyProcessCreate(String appName, int uid, int pid) {
        if (appName != null) {
            AppInfo curApp = this.aliveAppMap.get(appName);
            if (curApp == null) {
                curApp = new AppInfo(new AppInfo.AppInfoBase(uid, appName), this.runningTime, this.defaultReclaimRatio, this.defaultSwapRatio, new AppInfo.MemcgInfo.Builder().swapOutTotal(0).swapOutSize(0).swapInSize(0).swapInTotal(0).pageInTotal(0).swapSizeCur(0).swapSizeMax(0).freezeTimes(0).unFreezeTimes(0).create());
                AppInfo historyInfo = this.historyAppMap.get(appName);
                if (historyInfo != null) {
                    curApp.setDailyUseTime(historyInfo.getDailyFreezeTimes());
                    curApp.setDailyFreezeTimes(historyInfo.getDailyFreezeTimes());
                }
                this.aliveAppMap.put(appName, curApp);
                Slog.i(TAG, "SWAP-APP_START:pkg:" + appName + " uid:" + uid + " liveApp:" + this.aliveAppMap.size());
            }
            if (pid > 0) {
                curApp.addPids(Integer.valueOf(pid));
                this.pidPkg.put(Integer.valueOf(pid), appName);
            }
            curApp.setState(1);
        }
    }

    public int getLiveAppNum() {
        int liveAppNum = 0;
        for (AppInfo app : this.aliveAppMap.values()) {
            if (app.isFronted()) {
                liveAppNum++;
            }
        }
        return liveAppNum;
    }

    public void writeRunningStateLog(String logStr) {
        boolean isExist = new File(Statistics.RUNSTATE_LOG_PATH).exists();
        KernelInterface.getInstance().writeSwapFile(Statistics.RUNSTATE_LOG_FILE, getNowTime() + ", " + logStr + System.lineSeparator(), false);
        long curTime = System.currentTimeMillis();
        File liveCountFile = new File(Statistics.RUNSTATE_LOG_PATH);
        if (curTime - this.lastReportLiveTime > 28800000 || liveCountFile.length() > 81920) {
            Statistics.getInstance().reportRunState();
            this.lastReportLiveTime = curTime;
        }
        checkAdditionalLog(isExist);
    }

    private void checkAdditionalLog(boolean isExist) {
        File logFile = new File(Statistics.RUNSTATE_LOG_PATH);
        long curTime = SystemClock.uptimeMillis();
        if (logFile.exists() && isExist) {
            long j = this.lastReportAppNameTime;
            if (j != 0 && curTime - j < WifiProCommonUtils.RECHECK_DELAYED_MS) {
                return;
            }
        }
        this.lastReportAppNameTime = curTime;
        KernelInterface.getInstance().writeSwapFile(Statistics.RUNSTATE_LOG_FILE, getNowTime() + ", CurrentVersion, " + SystemProperties.get("persist.sys.hiview.cust_version") + System.lineSeparator(), false);
        StringBuilder appListLog = new StringBuilder(getNowTime());
        appListLog.append(", CurrentLivedApps, ");
        int liveAppNum = 0;
        for (AppInfo app : this.aliveAppMap.values()) {
            if (app.isFronted()) {
                liveAppNum++;
                if (liveAppNum > 1) {
                    appListLog.append("; ");
                }
                appListLog.append(app.getAppName());
            }
        }
        appListLog.append(", ");
        appListLog.append(liveAppNum);
        appListLog.append(System.lineSeparator());
        KernelInterface.getInstance().writeSwapFile(Statistics.RUNSTATE_LOG_FILE, appListLog.toString(), false);
    }

    private void summaryHistoryApp(AppInfo app) {
        if (this.historyAppMap.containsKey(app.getAppName())) {
            AppInfo historyApp = this.historyAppMap.get(app.getAppName());
            String lastBackgroundTime = app.getLastBackgroundTime();
            String lastFreezeTime = app.getLastFreezeTime();
            String historyBackgroundTime = historyApp.getHistoryBackgroundTime();
            String historyFreezeTime = historyApp.getHistoryFreezeTime();
            int appScore = app.getNowScore();
            try {
                historyBackgroundTime = getTimeTotal(historyApp.getHistoryBackgroundTime(), sdfParseDay(app.getHistoryBackgroundTime()).getTime());
                historyFreezeTime = getTimeTotal(historyApp.getHistoryFreezeTime(), sdfParseDay(app.getHistoryFreezeTime()).getTime());
                if (sdfParse(app.getLastBackgroundTime()).getTime() > 0) {
                    historyBackgroundTime = getTimeTotal(historyBackgroundTime, getTimeInterval(getNowTime(), app.getLastBackgroundTime()));
                }
                if (appScore >= 401 && sdfParse(app.getLastFreezeTime()).getTime() > 0) {
                    historyFreezeTime = getTimeTotal(historyFreezeTime, getTimeInterval(getNowTime(), app.getLastFreezeTime()));
                }
            } catch (ParseException e) {
                Slog.e(TAG, "summaryHistoryApp error" + e);
            }
            historyApp.setLastBackgroundTime(lastBackgroundTime);
            historyApp.setLastFreezeTime(lastFreezeTime);
            historyApp.setHistoryBackgroundTime(historyBackgroundTime);
            historyApp.setHistoryFreezeTime(historyFreezeTime);
            historyApp.setSwapOutTotal(historyApp.getSwapOutTotal() + app.getSwapOutTotal());
            historyApp.setSwapOutSize(historyApp.getSwapOutSize() + app.getSwapOutSize());
            historyApp.setSwapInSize(historyApp.getSwapInSize() + app.getSwapInSize());
            historyApp.setSwapInTotal(historyApp.getSwapInTotal() + app.getSwapInTotal());
            historyApp.setPageInTotal(historyApp.getPageInTotal() + app.getPageInTotal());
            historyApp.setSwapSizeCur(historyApp.getSwapSizeCur() + app.getSwapSizeCur());
            historyApp.setSwapSizeMax(Math.max(historyApp.getSwapSizeMax(), app.getSwapSizeMax()));
            historyApp.setFreezeTimes(historyApp.getFreezeTimes() + app.getFreezeTimes());
            historyApp.setUnFreezeTimes(historyApp.getUnFreezeTimes() + app.getUnFreezeTimes());
            historyApp.setLastAnon(0);
            historyApp.setHistoryAnon(historyApp.getHistoryAnon() + app.getHistoryAnon());
            historyApp.setForgroundTime(historyApp.getForgroundTime() + app.getForgroundTime());
            return;
        }
        this.historyAppMap.put(app.getAppName(), app);
    }

    public void notifyProcessDied(String appName, int uid, int pid) {
        this.pidPkg.remove(Integer.valueOf(pid));
        if (appName != null) {
            AppInfo curApp = this.aliveAppMap.get(appName);
            if (curApp != null) {
                curApp.delPid(Integer.valueOf(pid));
                if (curApp.getPids().size() == 0) {
                    this.aliveAppMap.remove(appName);
                    Slog.i(TAG, "SWAP-APP_DIED:pkg:" + appName + " uid:" + uid + " liveApp:" + this.aliveAppMap.size());
                    writeLiveCountToFile(curApp, 2);
                    summaryHistoryApp(curApp);
                    return;
                }
                return;
            }
            Slog.e(TAG, "notify Process Died , process not exist app: " + appName + " pid = " + pid);
        }
    }

    public void notifyAppStart(String appName, int uid, boolean visible) {
        notifyProcessCreate(appName, uid, -1);
        if (visible) {
            Message msg = this.handler.obtainMessage();
            msg.what = 4;
            msg.obj = appName;
            this.handler.sendMessage(msg);
        }
        AppInfo curApp = this.aliveAppMap.get(appName);
        if (curApp != null && visible) {
            if (!"com.huawei.android.launcher".equals(appName)) {
                this.lastSecondApp = this.lastApp;
                this.lastApp = appName;
            }
            AppInfo.MemcgInfo memcgInfo = KernelInterface.getInstance().readAppMemcgInfo(appName, curApp.getFreezeTimes(), curApp.getUnFreezeTimes());
            if (memcgInfo != null) {
                curApp.setMemcgInfo(memcgInfo);
            }
            try {
                if (sdfParse(curApp.getLastBackgroundTime()).getTime() > 0) {
                    curApp.setHistoryBackgroundTime(getTimeTotal(curApp.getHistoryBackgroundTime(), getTimeInterval(getNowTime(), curApp.getLastBackgroundTime())));
                } else {
                    curApp.setHistoryBackgroundTime(getTimeTotal(curApp.getHistoryBackgroundTime(), 0));
                }
            } catch (ParseException e) {
                Slog.e(TAG, "notifyAppStart error" + e);
            }
            if (curApp.isVisible()) {
                Slog.i(TAG, "app already in foreground, " + curApp.isVisible());
                return;
            }
            curApp.setVisible(visible);
            Slog.i(TAG, "app switch to foreground, " + curApp.isVisible());
        }
    }

    public void notifyAppGoBackground(String appName) {
        if (appName != null) {
            AppInfo curApp = this.aliveAppMap.get(appName);
            if (curApp != null) {
                KernelInterface.getInstance().getMemcgAnonTotal(appName);
                curApp.setLastBackgroundTime(getNowTime());
                AppInfo.MemcgInfo memcgInfo = KernelInterface.getInstance().readAppMemcgInfo(appName, curApp.getFreezeTimes(), curApp.getUnFreezeTimes());
                if (memcgInfo != null) {
                    curApp.setMemcgInfo(memcgInfo);
                }
                curApp.setVisible(false);
                writeRunningStateLog("GoBackground, " + appName);
                return;
            }
            Slog.e(TAG, "notifyAppGoBackground, app not exist: " + appName);
        }
    }

    public void notifyAppFreeze(String appName) {
        if (appName != null) {
            AppInfo curApp = this.aliveAppMap.get(appName);
            if (curApp != null) {
                int anon = KernelInterface.getInstance().getMemcgAnonTotal(appName);
                if (anon > 0) {
                    int lastAnon = curApp.getLastAnon();
                    int historyAnon = curApp.getHistoryAnon();
                    if (lastAnon > 0) {
                        curApp.setHistoryAnon((anon - lastAnon) + historyAnon);
                    }
                    curApp.setLastAnon(anon);
                }
                curApp.setLastFreezeTime(getNowTime());
                curApp.incFreezeTimes();
                curApp.setVisible(false);
                AppInfo.MemcgInfo memcgInfo = KernelInterface.getInstance().readAppMemcgInfo(appName, curApp.getFreezeTimes(), curApp.getUnFreezeTimes());
                if (memcgInfo != null) {
                    curApp.setMemcgInfo(memcgInfo);
                }
                curApp.setState(2);
                return;
            }
            Slog.e(TAG, "notifyAppFreeze, app not exist: " + appName);
        }
    }

    public void notifyAppUnFreeze(String appName) {
        if (appName != null) {
            AppInfo curApp = this.aliveAppMap.get(appName);
            if (curApp != null) {
                curApp.incUnFreezeTimes();
                AppInfo.MemcgInfo memcgInfo = KernelInterface.getInstance().readAppMemcgInfo(appName, curApp.getFreezeTimes(), curApp.getUnFreezeTimes());
                if (memcgInfo != null) {
                    curApp.setMemcgInfo(memcgInfo);
                }
                try {
                    if (sdfParse(curApp.getLastFreezeTime()).getTime() > 0) {
                        curApp.setHistoryFreezeTime(getTimeTotal(curApp.getHistoryFreezeTime(), getTimeInterval(getNowTime(), curApp.getLastFreezeTime())));
                    } else {
                        curApp.setHistoryFreezeTime(getTimeTotal(curApp.getHistoryFreezeTime(), 0));
                    }
                } catch (ParseException e) {
                    Slog.e(TAG, "notifyAppUnFreeze error" + e);
                }
                curApp.setState(1);
                return;
            }
            Slog.e(TAG, "notifyAppUnFreeze, app not exist: " + appName);
        }
    }

    public int getLivedAppNum() {
        return this.aliveAppMap.size();
    }

    public ArrayList<String> getVisibleAppList() {
        ArrayList<String> ret = new ArrayList<>();
        for (Map.Entry<String, AppInfo> entry : this.aliveAppMap.entrySet()) {
            if (entry.getValue().isVisible()) {
                ret.add(entry.getKey());
            }
        }
        return ret;
    }

    public boolean isAppDied(String appName) {
        if (appName == null) {
            return true;
        }
        return true ^ this.aliveAppMap.containsKey(appName);
    }

    public double getAppBackgroundFreezeFreq(String appName) {
        if (appName == null) {
            return -1.0d;
        }
        AppInfo curApp = this.aliveAppMap.get(appName);
        if (curApp == null) {
            Slog.e(TAG, "getAppBackgroundFreezeFreq, app not exist: " + appName);
            return -1.0d;
        }
        double startTime = curApp.getStartHour();
        double freezeTime = (double) curApp.getFreezeTimes();
        if (Math.abs(this.runningTime - startTime) < 0.001d) {
            return 0.0d;
        }
        return freezeTime / (this.runningTime - startTime);
    }

    public int getAppScore(String appName, int scoreType) {
        if (appName == null) {
            return -1;
        }
        AppInfo curApp = this.aliveAppMap.get(appName);
        if (curApp != null) {
            if (scoreType == 1) {
                return curApp.getActiveScore();
            }
            if (scoreType == 2) {
                return curApp.getInactScore();
            }
        }
        Slog.e(TAG, "getAppScore, app not exist: " + appName);
        return -1;
    }

    public int getUidByPkg(String appName) {
        if (appName == null) {
            return -1;
        }
        AppInfo curApp = this.aliveAppMap.get(appName);
        if (curApp != null) {
            return curApp.getUid();
        }
        Slog.e(TAG, "getUidByPkg, app not exist: " + appName);
        return -1;
    }

    public void setAppScore(String appName, int score, int scoreType) {
        if (appName != null) {
            AppInfo curApp = this.aliveAppMap.get(appName);
            if (curApp == null) {
                Slog.e(TAG, "setAppScore, app not exist: " + appName);
            } else if (scoreType == 1) {
                curApp.setActiveScore(score);
            } else if (scoreType == 2) {
                curApp.setInactScore(score);
            } else {
                Slog.e(TAG, "set App Score Error, scoreType not correct!");
            }
        }
    }

    public void zswapdFirstRunning() {
        this.snapShotMapFirst = KernelInterface.getInstance().getActiveRatioRefault(0);
        for (AppInfo appInfo : this.snapShotMapFirst.values()) {
            updateAppInfoWithAppInfo(appInfo);
        }
        Message msg = this.handler.obtainMessage();
        msg.what = 2;
        this.handler.sendMessageDelayed(msg, 4000);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateActiveMemcg() {
        AppInfo appInfo;
        int tmpReclaimRatio;
        ConcurrentHashMap<String, AppInfo> bothSnapShotWithRefault = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, AppInfo> concurrentHashMap = this.snapShotMapFirst;
        if (!(concurrentHashMap == null || this.snapShotMapSecond == null)) {
            for (String appNameFirst : concurrentHashMap.keySet()) {
                for (String appNameSecond : this.snapShotMapSecond.keySet()) {
                    if (appNameFirst.equals(appNameSecond) && (appInfo = this.snapShotMapSecond.get(appNameSecond)) != null) {
                        int tmpRefault = (appInfo.getRefault() * 400) / (appInfo.getAnonTotal() + 1);
                        int tmpRealRatio = appInfo.getRealRatio();
                        int tmpReclaimRatio2 = appInfo.getReclaimRatio();
                        int tmpSwapRatio = appInfo.getSwapRatio();
                        if (tmpRealRatio > tmpReclaimRatio2 && tmpRefault < 10) {
                            tmpReclaimRatio = (tmpReclaimRatio2 + 100) / 2;
                        } else if (tmpRealRatio < tmpReclaimRatio2 && tmpRefault > 10) {
                            tmpReclaimRatio = tmpRealRatio;
                            tmpSwapRatio = Math.min(tmpSwapRatio, tmpReclaimRatio);
                        }
                        Slog.i(TAG, "updateActiveMemcg: " + appNameSecond + ", real ratio: " + tmpRealRatio + ", last reclaim ratio: " + appInfo.getReclaimRatio() + ", new reclaim ratio:" + tmpReclaimRatio + ", last swap ratio: " + appInfo.getSwapRatio() + ", new swap ratio: " + tmpSwapRatio + ", refault: " + tmpRefault);
                        appInfo.setReclaimRatio(tmpReclaimRatio);
                        appInfo.setSwapRatio(tmpSwapRatio);
                        updateAppInfoWithAppInfo(appInfo);
                        bothSnapShotWithRefault.put(appNameSecond, appInfo);
                    }
                }
            }
            AppScore.getInstance().setNewActiveMemcg(bothSnapShotWithRefault);
        }
    }

    private void updateAppInfoWithAppInfo(AppInfo appInfo) {
        if (appInfo != null) {
            AppInfo curApp = this.aliveAppMap.get(appInfo.getAppName());
            if (curApp == null) {
                Slog.e(TAG, "updateAppInfo, app not exist: " + appInfo.getAppName());
                return;
            }
            curApp.setReclaimRatio(appInfo.getReclaimRatio());
            curApp.setSwapRatio(appInfo.getSwapRatio());
            curApp.setRefault(appInfo.getRefault());
            curApp.setAnonTotal(appInfo.getAnonTotal());
            curApp.setMemcgInfo(appInfo.getMemcgInfo());
        }
    }

    public AppInfo getAppInfoByAppName(String appName) {
        if (appName == null) {
            return null;
        }
        return this.aliveAppMap.get(appName);
    }

    public void setNowScore(String appName, int score) {
        if (appName != null) {
            AppInfo curApp = this.aliveAppMap.get(appName);
            if (curApp == null) {
                Slog.e(TAG, "setNowScore, app not exist: " + appName);
                return;
            }
            curApp.setNowScore(score);
        }
    }

    private void statisticAllMemcgBeforeClean(AppInfo app) {
        int score;
        if (app != null && (score = app.getNowScore()) != 0) {
            try {
                if (sdfParse(app.getLastBackgroundTime()).getTime() > 0) {
                    app.setHistoryBackgroundTime(getTimeTotal(app.getHistoryBackgroundTime(), getTimeInterval(getNowTime(), app.getLastBackgroundTime())));
                }
                app.setLastBackgroundTime(getNowTime());
                if (score >= 401) {
                    if (sdfParse(app.getLastFreezeTime()).getTime() > 0) {
                        app.setHistoryFreezeTime(getTimeTotal(app.getHistoryFreezeTime(), getTimeInterval(getNowTime(), app.getLastFreezeTime())));
                    }
                    app.setLastFreezeTime(getNowTime());
                }
            } catch (ParseException e) {
                Slog.e(TAG, "statisticAllMemcgBeforeClean " + e);
            }
        }
    }

    private void statisticAllMemcgClean(AppInfo app) {
        if (app != null) {
            int score = app.getNowScore();
            app.setHistoryBackgroundTime("00:00:00");
            app.setHistoryFreezeTime("00:00:00");
            if (score == 0) {
                app.setLastBackgroundTime("1970-01-01 08:00:00");
                app.setLastFreezeTime("1970-01-01 08:00:00");
            } else if (score <= 400) {
                app.setLastBackgroundTime(getNowTime());
                app.setLastFreezeTime("1970-01-01 08:00:00");
            } else {
                app.setLastBackgroundTime(getNowTime());
                app.setLastFreezeTime(getNowTime());
            }
        }
    }

    private String statisticLiveMemcgWithHistory(AppInfo app) {
        if (app == null) {
            return "";
        }
        statisticAllMemcgBeforeClean(app);
        if (this.historyAppMap.containsKey(app.getAppName())) {
            AppInfo historyApp = this.historyAppMap.get(app.getAppName());
            AppInfo tmpApp = new AppInfo(new AppInfo.AppInfoBase(historyApp.getUid(), historyApp.getAppName()), this.runningTime, app.getReclaimRatio(), app.getSwapRatio(), new AppInfo.MemcgInfo.Builder().swapOutTotal(historyApp.getSwapOutTotal() + app.getSwapOutTotal()).swapOutSize(historyApp.getSwapOutSize() + app.getSwapOutSize()).swapInSize(historyApp.getSwapInSize() + app.getSwapInSize()).swapInTotal(historyApp.getSwapInTotal() + app.getSwapInTotal()).pageInTotal(historyApp.getPageInTotal() + app.getPageInTotal()).swapSizeCur(historyApp.getSwapSizeCur() + app.getSwapSizeCur()).swapSizeMax(Math.max(historyApp.getSwapSizeMax(), app.getSwapSizeMax())).freezeTimes(historyApp.getFreezeTimes() + app.getFreezeTimes()).unFreezeTimes(historyApp.getUnFreezeTimes() + app.getUnFreezeTimes()).create());
            tmpApp.setLastBackgroundTime(historyApp.getLastBackgroundTime());
            tmpApp.setLastFreezeTime(historyApp.getLastFreezeTime());
            try {
                tmpApp.setHistoryBackgroundTime(getTimeTotal(historyApp.getHistoryBackgroundTime(), sdfParseDay(app.getHistoryBackgroundTime()).getTime()));
                tmpApp.setHistoryFreezeTime(getTimeTotal(historyApp.getHistoryFreezeTime(), sdfParseDay(app.getHistoryFreezeTime()).getTime()));
            } catch (ParseException e) {
                Slog.e(TAG, "statisticLiveMemcgWithHistory error" + e);
            }
            tmpApp.setHistoryAnon(historyApp.getHistoryAnon() + app.getHistoryAnon());
            tmpApp.setForgroundTime(historyApp.getForgroundTime() + app.getForgroundTime());
            historyApp.setNowScore(0);
            statisticAllMemcgClean(historyApp);
            return tmpApp.swapToString() + System.lineSeparator();
        }
        return app.swapToString() + System.lineSeparator();
    }

    private String statisticAllMemcg() {
        AppInfo.MemcgInfo memcgInfo = KernelInterface.getInstance().readMemcgInfo();
        if (memcgInfo == null) {
            return "memcgInfo is null!";
        }
        String allMemcg = "all memcg: " + memcgInfo.swapToString() + System.lineSeparator();
        for (AppInfo app : this.aliveAppMap.values()) {
            allMemcg = allMemcg + statisticLiveMemcgWithHistory(app);
            statisticAllMemcgClean(app);
        }
        for (AppInfo app2 : this.historyAppMap.values()) {
            if (app2 != null && !this.aliveAppMap.containsKey(app2.getAppName())) {
                allMemcg = allMemcg + app2.swapToString() + System.lineSeparator();
            }
        }
        Slog.i(TAG, "reportFreezeActive statisticAllMemcg");
        return allMemcg;
    }

    public static String getNowTime() {
        return sdfFormate(new Date());
    }

    private long getTimeInterval(String time1, String time2) {
        try {
            return Math.abs(sdfParse(time1).getTime() - sdfParse(time2).getTime());
        } catch (ParseException e) {
            Slog.e(TAG, "getTimeInterval error" + e);
            return 0;
        }
    }

    private String getTimeTotal(String time, long between) {
        try {
            return sdfFormateDay(sdfParseDay(time).getTime() + between);
        } catch (ParseException e) {
            Slog.e(TAG, "getTimeTotal error" + e);
            return "";
        }
    }

    private String statisticFrontedApp(AppInfo app, int startType) {
        String appStatus = "app start";
        if (startType == 0) {
            appStatus = "ColdStart";
        } else if (startType == 1) {
            appStatus = "HotStart";
        } else if (startType == 2) {
            appStatus = "Died";
        } else {
            Slog.e(TAG, "statisticFrontedApp get wrong start type " + startType);
        }
        return appStatus + ", " + app.getAppName() + ", " + getLiveAppNum();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void writeBigDataToFile() {
        KernelInterface.getInstance().writeSwapFile("abnormal_freeze_unfreeze", statisticAllMemcg(), true);
        Statistics.getInstance().reportFreezeActive();
        Slog.i(TAG, "reportFreezeActive writeBigDataToFile");
    }

    public void setIsFront(String appName) {
        if (appName != null) {
            AppInfo curApp = this.aliveAppMap.get(appName);
            if (curApp == null) {
                Slog.e(TAG, "setIsFront, app not exist: " + appName);
                return;
            }
            curApp.incForegroundTime();
            if (!curApp.isFronted()) {
                curApp.setIsFronted(true);
            }
            if (!curApp.getIsAlive()) {
                curApp.setIsAlive(true);
                writeLiveCountToFile(curApp, 0);
                return;
            }
            writeLiveCountToFile(curApp, 1);
        }
    }

    private void writeLiveCountToFile(AppInfo app, int startType) {
        if (app.isFronted()) {
            if (startType == 2) {
                app.setIsAlive(false);
                app.setIsFronted(false);
            }
            writeRunningStateLog(statisticFrontedApp(app, startType));
        }
    }

    public void increaseDailyUseCount(String appName) {
        AppInfo curApp = getAppInfoFromMap(appName);
        if (curApp != null) {
            int dailyUseCount = curApp.getDailyUseTime() + 1;
            curApp.setDailyUseTime(dailyUseCount);
            Slog.i(TAG, "setDailyUseCount pkgName:" + appName + " dailyUseCount:" + dailyUseCount);
            return;
        }
        Slog.e(TAG, "setDailyUseCount error! pkgName:" + appName);
    }

    public int getDailyUseCount(String appName) {
        AppInfo curApp = getAppInfoFromMap(appName);
        if (curApp != null) {
            int dailyUseCount = curApp.getDailyUseTime();
            Slog.i(TAG, "getDailyUseCount. pkgName:" + appName + " dailyUseCount:" + dailyUseCount);
            return dailyUseCount;
        }
        Slog.e(TAG, "getDailyUseCount Error!pkgName:" + appName + " dailyUseCount:-1");
        return -1;
    }

    public int getDailyFreezeTime(String appName) {
        AppInfo curApp = getAppInfoFromMap(appName);
        if (curApp != null) {
            return curApp.getDailyFreezeTimes();
        }
        Slog.e(TAG, "getDailyFreezeTime Error!pkgName:" + appName + " dailyFreezeTimes:-1");
        return -1;
    }

    public void increaseDailyFreezeTime(String appName) {
        AppInfo curApp = getAppInfoFromMap(appName);
        if (curApp != null) {
            curApp.setDailyFreezeTimes(curApp.getDailyFreezeTimes() + 1);
            return;
        }
        Slog.e(TAG, "setDailyFreezeTime error! pkgName:" + appName);
    }

    private AppInfo getAppInfoFromMap(String appName) {
        if (appName == null) {
            return null;
        }
        AppInfo curApp = this.aliveAppMap.get(appName);
        if (curApp == null) {
            Slog.e(TAG, "getAppInfoFromMap, app not exist: " + appName);
        }
        return curApp;
    }

    public void initAllAppRatioAdjustParam() {
        for (Map.Entry<String, AppInfo> entry : this.aliveAppMap.entrySet()) {
            AppInfo curApp = entry.getValue();
            curApp.setDailyFreezeTimes(0);
            curApp.setDailyUseTime(0);
        }
        for (Map.Entry<String, AppInfo> entry2 : this.historyAppMap.entrySet()) {
            AppInfo curApp2 = entry2.getValue();
            curApp2.setDailyFreezeTimes(0);
            curApp2.setDailyUseTime(0);
        }
        Slog.i(TAG, "initAllAppRatioAdjustParam success!");
    }

    public void initAllAppFreezeTime() {
        for (Map.Entry<String, AppInfo> entry : this.aliveAppMap.entrySet()) {
            entry.getValue().setDailyFreezeTimes(0);
        }
        for (Map.Entry<String, AppInfo> entry2 : this.historyAppMap.entrySet()) {
            entry2.getValue().setDailyFreezeTimes(0);
        }
        Slog.i(TAG, "initAllAppRatioAdjustParam success!");
    }

    public int getAppNowScore(String appName) {
        if (appName == null || appName.length() == 0) {
            return -1;
        }
        AppInfo curApp = this.aliveAppMap.get(appName);
        if (curApp != null) {
            return curApp.getNowScore();
        }
        Slog.e(TAG, "getAppNowScore, app not exist: " + appName);
        return -1;
    }
}
