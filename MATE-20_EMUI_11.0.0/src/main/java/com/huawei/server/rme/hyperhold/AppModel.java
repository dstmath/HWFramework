package com.huawei.server.rme.hyperhold;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Slog;
import com.android.internal.os.BackgroundThread;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.huawei.server.rme.hyperhold.AppInfo;
import com.huawei.server.rme.hyperhold.ParaConfig;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class AppModel {
    public static final int ACT_SCORE = 1;
    private static final int APP_DIED = 3;
    private static final int BACK_START = 2;
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
    private ConcurrentHashMap<String, AppInfo> activeAppMap = new ConcurrentHashMap<>();
    private int defaultReclaimRatio = 0;
    private int defaultSwapRatio = 0;
    private ConcurrentHashMap<String, AppInfo> diedAppMap = new ConcurrentHashMap<>();
    private Set<String> forbidIawareKillAppList = Collections.synchronizedSet(new HashSet());
    private ConcurrentHashMap<String, AppInfo> freezedAppMap = new ConcurrentHashMap<>();
    private Handler handler = null;
    private ConcurrentHashMap<String, AppInfo> historyAppMap = new ConcurrentHashMap<>();
    private String lastApp = "";
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
                AppModel.this.setThirdParty(str);
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
        Slog.i(TAG, "AppModel getInstance called!");
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

    public boolean isForbidIawareKillApp(String appName) {
        return this.forbidIawareKillAppList.contains(appName);
    }

    public String getPkgByPid(int pid) {
        if (this.pidPkg.containsKey(Integer.valueOf(pid))) {
            return this.pidPkg.get(Integer.valueOf(pid));
        }
        return "";
    }

    public void notifyProcessCreate(String appName, int uid, int pid) {
        this.pidPkg.put(Integer.valueOf(pid), appName);
        if (this.freezedAppMap.containsKey(appName)) {
            AppInfo curApp = this.freezedAppMap.get(appName);
            if (pid > 0) {
                curApp.addPids(Integer.valueOf(pid));
            }
            this.activeAppMap.put(appName, curApp);
            this.freezedAppMap.remove(appName);
        } else if (this.activeAppMap.containsKey(appName)) {
            AppInfo curApp2 = this.activeAppMap.get(appName);
            if (pid > 0) {
                curApp2.addPids(Integer.valueOf(pid));
            }
        } else if (this.diedAppMap.containsKey(appName)) {
            AppInfo curApp3 = this.diedAppMap.get(appName);
            if (pid > 0) {
                curApp3.addPids(Integer.valueOf(pid));
            }
            this.activeAppMap.put(appName, curApp3);
            this.diedAppMap.remove(appName);
            if (curApp3.getThirdParty() && !curApp3.getIsAlive()) {
                writeLiveCountToFile(curApp3, 2);
            }
        } else {
            AppInfo curApp4 = new AppInfo(new AppInfo.AppInfoBase(uid, pid, appName), this.runningTime, this.defaultReclaimRatio, this.defaultSwapRatio, new AppInfo.MemcgInfo.Builder().swapOutTotal(0).swapOutSize(0).swapInSize(0).swapInTotal(0).pageInTotal(0).swapSizeCur(0).swapSizeMax(0).freezeTimes(0).unFreezeTimes(0).create());
            curApp4.setLastBackgroundTime(getNowTime());
            this.activeAppMap.put(appName, curApp4);
            int liveAppCount = this.freezedAppMap.size() + this.activeAppMap.size();
            Slog.i(TAG, "SWAP-APP_START:pkg:" + appName + " uid:" + uid + " liveApp:" + liveAppCount);
        }
    }

    public int getLiveAppNum() {
        int liveAppNum = 0;
        for (AppInfo app : this.activeAppMap.values()) {
            if (app.getThirdParty()) {
                liveAppNum++;
            }
        }
        for (AppInfo app2 : this.freezedAppMap.values()) {
            if (app2.getThirdParty()) {
                liveAppNum++;
            }
        }
        return liveAppNum;
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
        if (this.freezedAppMap.containsKey(appName)) {
            AppInfo curApp = this.freezedAppMap.get(appName);
            curApp.delPid(Integer.valueOf(pid));
            if (curApp.getPids().size() == 0) {
                this.diedAppMap.put(appName, curApp);
                this.freezedAppMap.remove(appName);
                int liveAppCount = this.freezedAppMap.size() + this.activeAppMap.size();
                Slog.i(TAG, "SWAP-APP_DIED:pkg:" + appName + " uid:" + uid + " liveApp:" + liveAppCount);
                writeLiveCountToFile(curApp, 3);
                summaryHistoryApp(curApp);
            }
        } else if (this.activeAppMap.containsKey(appName)) {
            AppInfo curApp2 = this.activeAppMap.get(appName);
            curApp2.delPid(Integer.valueOf(pid));
            if (curApp2.getPids().size() == 0) {
                this.diedAppMap.put(appName, curApp2);
                this.activeAppMap.remove(appName);
                int liveAppCount2 = this.freezedAppMap.size() + this.activeAppMap.size();
                Slog.i(TAG, "SWAP-APP_DIED:pkg:" + appName + " uid:" + uid + " liveApp:" + liveAppCount2);
                writeLiveCountToFile(curApp2, 3);
                summaryHistoryApp(curApp2);
            }
        } else {
            Slog.e(TAG, "notify Process Died , process not exist app: " + appName + " pid = " + pid);
        }
    }

    public void notifyAppStart(String appName, int uid, boolean visible) {
        this.forbidIawareKillAppList.remove(appName);
        notifyProcessCreate(appName, uid, -1);
        if (visible) {
            Message msg = this.handler.obtainMessage();
            msg.what = 4;
            msg.obj = appName;
            this.handler.sendMessage(msg);
        }
        if (this.activeAppMap.containsKey(appName) && visible) {
            if (!"com.huawei.android.launcher".equals(appName)) {
                this.lastSecondApp = this.lastApp;
                this.lastApp = appName;
            }
            AppInfo curApp = this.activeAppMap.get(appName);
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
                Slog.i(TAG, "app already in foreground," + curApp.isVisible());
                return;
            }
            curApp.setVisible(visible);
            Slog.i(TAG, "app switch to foreground," + curApp.isVisible());
        }
    }

    public void notifyAppGoBackground(String appName) {
        this.forbidIawareKillAppList.add(appName);
        if (this.activeAppMap.containsKey(appName)) {
            AppInfo curApp = this.activeAppMap.get(appName);
            KernelInterface.getInstance().getMemcgAnonTotal(appName);
            curApp.setLastBackgroundTime(getNowTime());
            AppInfo.MemcgInfo memcgInfo = KernelInterface.getInstance().readAppMemcgInfo(appName, curApp.getFreezeTimes(), curApp.getUnFreezeTimes());
            if (memcgInfo != null) {
                curApp.setMemcgInfo(memcgInfo);
            }
            curApp.setVisible(false);
        }
    }

    public void notifyAppFreeze(String appName) {
        if (this.activeAppMap.containsKey(appName)) {
            AppInfo curApp = this.activeAppMap.get(appName);
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
            this.activeAppMap.remove(appName);
            this.freezedAppMap.put(appName, curApp);
        }
    }

    public void notifyAppUnFreeze(String appName) {
        if (this.freezedAppMap.containsKey(appName)) {
            AppInfo curApp = this.freezedAppMap.get(appName);
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
            this.freezedAppMap.remove(appName);
            this.activeAppMap.put(appName, curApp);
        } else if (this.activeAppMap.containsKey(appName)) {
            AppInfo curApp2 = this.activeAppMap.get(appName);
            curApp2.incUnFreezeTimes();
            AppInfo.MemcgInfo memcgInfo2 = KernelInterface.getInstance().readAppMemcgInfo(appName, curApp2.getFreezeTimes(), curApp2.getUnFreezeTimes());
            if (memcgInfo2 != null) {
                curApp2.setMemcgInfo(memcgInfo2);
            }
            try {
                if (sdfParse(curApp2.getLastFreezeTime()).getTime() > 0) {
                    curApp2.setHistoryFreezeTime(getTimeTotal(curApp2.getHistoryFreezeTime(), getTimeInterval(getNowTime(), curApp2.getLastFreezeTime())));
                } else {
                    curApp2.setHistoryFreezeTime(getTimeTotal(curApp2.getHistoryFreezeTime(), 0));
                }
            } catch (ParseException e2) {
                Slog.e(TAG, "notifyAppUnFreeze error" + e2);
            }
        } else {
            Slog.e(TAG, "notifyAppUnFreeze can not find " + appName);
        }
    }

    public ArrayList<String> getActiveAppList() {
        return new ArrayList<>(this.activeAppMap.keySet());
    }

    public ArrayList<String> getFreezedAppList() {
        return new ArrayList<>(this.freezedAppMap.keySet());
    }

    public ArrayList<String> getLastTwoAppList() {
        ArrayList<String> ret = new ArrayList<>();
        ret.add(this.lastApp);
        ret.add(this.lastSecondApp);
        return ret;
    }

    public ArrayList<String> getVisibleAppList() {
        ArrayList<String> ret = new ArrayList<>();
        for (Map.Entry<String, AppInfo> entry : this.activeAppMap.entrySet()) {
            if (entry.getValue().isVisible()) {
                ret.add(entry.getKey());
            }
        }
        return ret;
    }

    public boolean isAppFirstStart(String appName) {
        AppInfo curApp;
        if (!this.activeAppMap.containsKey(appName) || (curApp = this.activeAppMap.get(appName)) == null || curApp.getPids().size() > 1) {
            return false;
        }
        return true;
    }

    public boolean isAppDied(String appName) {
        return !this.activeAppMap.containsKey(appName) && !this.freezedAppMap.containsKey(appName);
    }

    public double getAppBackgroundFreezeFreq(String appName) {
        if (isAppDied(appName)) {
            return -1.0d;
        }
        AppInfo curApp = this.activeAppMap.get(appName);
        if (curApp == null) {
            curApp = this.freezedAppMap.get(appName);
        }
        if (curApp == null) {
            Slog.e(TAG, "getAppBackgroundFreezeFreq error!" + appName);
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
        AppInfo curApp = this.activeAppMap.get(appName);
        if (curApp == null) {
            curApp = this.freezedAppMap.get(appName);
        }
        if (curApp != null) {
            if (scoreType == 1) {
                return curApp.getActiveScore();
            }
            if (scoreType == 2) {
                return curApp.getInactScore();
            }
        }
        Slog.e(TAG, "getAppScore error! pkgName:" + appName + " scoreType:" + scoreType);
        return -1;
    }

    public int getUidByPkg(String appName) {
        AppInfo curApp = this.activeAppMap.get(appName);
        if (curApp == null) {
            curApp = this.freezedAppMap.get(appName);
        }
        if (curApp != null) {
            return curApp.getUid();
        }
        Slog.e(TAG, "getUidByPkg error! pkgName:" + appName);
        return -1;
    }

    public void setAppScore(String appName, int score, int scoreType) {
        AppInfo curApp = this.activeAppMap.get(appName);
        if (curApp == null) {
            curApp = this.freezedAppMap.get(appName);
        }
        if (curApp == null) {
            Slog.e(TAG, "setAppScore error! pkgName:" + appName);
        } else if (scoreType == 1) {
            curApp.setActiveScore(score);
        } else if (scoreType == 2) {
            curApp.setInactScore(score);
        } else {
            Slog.e(TAG, "set App Score Error!");
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
            updateAppInfo(appInfo.getAppName(), appInfo.getReclaimRatio(), appInfo.getSwapRatio(), appInfo.getRefault(), appInfo.getAnonTotal());
            updateAppInfoWithMemcgInfo(appInfo.getAppName(), appInfo.getMemcgInfo());
        }
    }

    private void updateAppInfo(String appName, int reclaimRatio, int swapRatio, int refault, int anonTotal) {
        AppInfo curApp = this.activeAppMap.get(appName);
        if (curApp == null) {
            curApp = this.freezedAppMap.get(appName);
        }
        if (curApp == null) {
            Slog.e(TAG, "updateAppInfo can not find " + appName);
            return;
        }
        curApp.setReclaimRatio(reclaimRatio);
        curApp.setSwapRatio(swapRatio);
        curApp.setRefault(refault);
        curApp.setAnonTotal(anonTotal);
    }

    private void updateAppInfoWithMemcgInfo(String appName, AppInfo.MemcgInfo memcgInfo) {
        AppInfo curApp = this.activeAppMap.get(appName);
        if (curApp == null) {
            curApp = this.freezedAppMap.get(appName);
        }
        if (curApp == null) {
            Slog.e(TAG, "updateAppInfoWithMemcgInfo can not find " + appName);
            return;
        }
        curApp.setMemcgInfo(memcgInfo);
    }

    public AppInfo getAppInfoByAppName(String appName) {
        AppInfo curApp = this.activeAppMap.get(appName);
        if (curApp == null) {
            return this.freezedAppMap.get(appName);
        }
        return curApp;
    }

    public void setNowScore(String appName, int score) {
        AppInfo curApp = this.activeAppMap.get(appName);
        if (curApp == null) {
            curApp = this.freezedAppMap.get(appName);
        }
        if (curApp == null) {
            Slog.e(TAG, "setNowScore nullptr: " + appName);
            return;
        }
        curApp.setNowScore(score);
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
            AppInfo tmpApp = new AppInfo(new AppInfo.AppInfoBase(historyApp.getUid(), -1, historyApp.getAppName()), this.runningTime, app.getReclaimRatio(), app.getSwapRatio(), new AppInfo.MemcgInfo.Builder().swapOutTotal(historyApp.getSwapOutTotal() + app.getSwapOutTotal()).swapOutSize(historyApp.getSwapOutSize() + app.getSwapOutSize()).swapInSize(historyApp.getSwapInSize() + app.getSwapInSize()).swapInTotal(historyApp.getSwapInTotal() + app.getSwapInTotal()).pageInTotal(historyApp.getPageInTotal() + app.getPageInTotal()).swapSizeCur(historyApp.getSwapSizeCur() + app.getSwapSizeCur()).swapSizeMax(Math.max(historyApp.getSwapSizeMax(), app.getSwapSizeMax())).freezeTimes(historyApp.getFreezeTimes() + app.getFreezeTimes()).unFreezeTimes(historyApp.getUnFreezeTimes() + app.getUnFreezeTimes()).create());
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
        for (AppInfo app : this.activeAppMap.values()) {
            allMemcg = allMemcg + statisticLiveMemcgWithHistory(app);
            statisticAllMemcgClean(app);
        }
        for (AppInfo app2 : this.freezedAppMap.values()) {
            allMemcg = allMemcg + statisticLiveMemcgWithHistory(app2);
            statisticAllMemcgClean(app2);
        }
        for (AppInfo app3 : this.historyAppMap.values()) {
            if (app3 != null && !this.activeAppMap.containsKey(app3.getAppName()) && !this.freezedAppMap.containsKey(app3.getAppName())) {
                allMemcg = allMemcg + app3.swapToString() + System.lineSeparator();
            }
        }
        Slog.i(TAG, "reportFreezeActive statisticAllMemcg");
        return allMemcg;
    }

    private String getNowTime() {
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

    private String statisitcThirdParty(AppInfo app, int startType) {
        String appStatus = "app start";
        if (startType == 0) {
            appStatus = "Cold Start";
        } else if (startType == 1) {
            appStatus = "Hot Start";
        } else if (startType == 2) {
            appStatus = "Background Start";
        } else if (startType == 3) {
            appStatus = "App Died";
        } else {
            Slog.e(TAG, "statisitcThirdParty get wrong start type " + startType);
        }
        return getNowTime() + ", " + app.getAppName() + ", " + appStatus + ", live count: " + getLiveAppNum() + System.lineSeparator();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void writeBigDataToFile() {
        KernelInterface.getInstance().writeSwapFile("abnormal_freeze_unfreeze", statisticAllMemcg(), true);
        Statistics.getInstance().reportFreezeActive();
        Slog.i(TAG, "reportFreezeActive writeBigDataToFile");
    }

    public void setThirdParty(String appName) {
        AppInfo curApp = this.activeAppMap.get(appName);
        if (curApp == null) {
            curApp = this.freezedAppMap.get(appName);
        }
        if (curApp == null) {
            Slog.e(TAG, "setThirdParty app nullptr: " + appName);
            return;
        }
        curApp.incForegroundTime();
        if (!curApp.getThirdParty()) {
            curApp.setThirdParty();
        }
        if (!curApp.getIsAlive()) {
            curApp.setIsAlive(true);
            writeLiveCountToFile(curApp, 0);
            return;
        }
        writeLiveCountToFile(curApp, 1);
    }

    private void writeLiveCountToFile(AppInfo app, int startType) {
        if (app.getThirdParty()) {
            if (startType == 3) {
                app.setIsAlive(false);
            }
            long curTime = System.currentTimeMillis();
            KernelInterface.getInstance().writeSwapFile("hyperhold_running_state", statisitcThirdParty(app, startType), false);
            File liveCountFile = new File("/data/log/iaware/hyperhold/hyperhold_running_state.log");
            if (curTime - this.lastReportLiveTime > 28800000 || liveCountFile.length() > 81920) {
                Statistics.getInstance().reportRunState();
                this.lastReportLiveTime = curTime;
            }
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
            int dailyFreezeTimes = curApp.getDailyFreezeTimes();
            Slog.i(TAG, "getDailyFreezeTime. pkgName:" + appName + " dailyFreezeTimes:" + dailyFreezeTimes);
            return dailyFreezeTimes;
        }
        Slog.e(TAG, "getDailyFreezeTime Error!pkgName:" + appName + " dailyFreezeTimes:-1");
        return -1;
    }

    public void increaseDailyFreezeTime(String appName) {
        AppInfo curApp = getAppInfoFromMap(appName);
        if (curApp != null) {
            int dailyFreezeTimes = curApp.getDailyFreezeTimes() + 1;
            curApp.setDailyFreezeTimes(dailyFreezeTimes);
            Slog.i(TAG, "setDailyFreezeTime pkgName:" + appName + " DailyFreezeTime:" + dailyFreezeTimes);
            return;
        }
        Slog.e(TAG, "setDailyFreezeTime error! pkgName:" + appName);
    }

    private AppInfo getAppInfoFromMap(String appName) {
        AppInfo curApp = this.activeAppMap.get(appName);
        if (curApp == null) {
            curApp = this.freezedAppMap.get(appName);
        }
        if (curApp == null) {
            curApp = this.diedAppMap.get(appName);
        }
        if (curApp == null) {
            Slog.e(TAG, "getAppInfoFromMap error!pkgName:" + appName);
        }
        return curApp;
    }

    public void initAllAppRatioAdjustParam() {
        for (Map.Entry<String, AppInfo> entry : this.activeAppMap.entrySet()) {
            AppInfo curApp = entry.getValue();
            curApp.setDailyFreezeTimes(0);
            curApp.setDailyUseTime(0);
        }
        for (Map.Entry<String, AppInfo> entry2 : this.freezedAppMap.entrySet()) {
            AppInfo curApp2 = entry2.getValue();
            curApp2.setDailyFreezeTimes(0);
            curApp2.setDailyUseTime(0);
        }
        for (Map.Entry<String, AppInfo> entry3 : this.diedAppMap.entrySet()) {
            AppInfo curApp3 = entry3.getValue();
            curApp3.setDailyFreezeTimes(0);
            curApp3.setDailyUseTime(0);
        }
        Slog.i(TAG, "initAllAppRatioAdjustParam success!");
    }

    public void initAllAppFreezeTime() {
        for (Map.Entry<String, AppInfo> entry : this.activeAppMap.entrySet()) {
            entry.getValue().setDailyFreezeTimes(0);
        }
        for (Map.Entry<String, AppInfo> entry2 : this.freezedAppMap.entrySet()) {
            entry2.getValue().setDailyFreezeTimes(0);
        }
        for (Map.Entry<String, AppInfo> entry3 : this.diedAppMap.entrySet()) {
            entry3.getValue().setDailyFreezeTimes(0);
        }
        Slog.i(TAG, "initAllAppRatioAdjustParam success!");
    }

    public int getAppNowScore(String appName) {
        if (appName == null || appName.length() == 0) {
            return -1;
        }
        AppInfo curApp = this.activeAppMap.get(appName);
        if (curApp == null) {
            curApp = this.freezedAppMap.get(appName);
        }
        if (curApp == null) {
            return -1;
        }
        return curApp.getNowScore();
    }
}
