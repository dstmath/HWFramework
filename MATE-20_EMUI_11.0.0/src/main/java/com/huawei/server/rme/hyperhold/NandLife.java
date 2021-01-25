package com.huawei.server.rme.hyperhold;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.internal.os.BackgroundThread;
import com.huawei.server.rme.collector.ResourceCollector;
import java.util.ArrayList;

public class NandLife {
    private static final int DAILY_CYCLE_MSG = 2;
    private static final double EPSILON = 9.999999974752427E-7d;
    private static final int EXCEP_SWAP_OUT_DAILY_RETRY = 20;
    private static final int EXCEP_TIME_ON_RETRY = 20;
    private static final int HOURS_CYCLE_MSG = 3;
    private static final int INIT_MSG = 1;
    private static final int SWAP_DAILY_PASSED_TIME_INDEX = 5;
    private static final int SWAP_OUT_DAILYDQUOTA_INDEX = 1;
    private static final int SWAP_OUT_DAILY_INDEX = 7;
    private static final int SWAP_OUT_DAYSOFPERIOD_INDEX = 4;
    private static final int SWAP_OUT_PASSED_DAYS_INDEX = 3;
    private static final int SWAP_OUT_PASSED_PERIODS_INDEX = 2;
    private static final int SWAP_OUT_PERIODQUOTA_INDEX = 0;
    private static final int SWAP_OUT_PERIOD_INDEX = 6;
    private static final int SWAP_STORE_COUNT = 8;
    private static final String TAG = "SWAPNandLife";
    private static final int UPDATE_IO_COUNT = 900000;
    private static final double UPDATE_IO_LIMIT = 24.0d;
    private static final double UPDATE_IO_STEP = 0.25d;
    private static double dailyQuota = 99.0d;
    private static double daysCount = 0.0d;
    private static double daysInPeriod = 0.0d;
    private static double daysOfPeriod = 7.0d;
    private static double lastSwapOutCountFromBoot = 0.0d;
    private static boolean lifeInDanger = false;
    private static double minDaysControlBegin = 109.0d;
    private static double minDaysFactBegin = 0.0d;
    private static volatile NandLife nandLifeHandler;
    private static double periodQuota = 139.0d;
    private static double periodSwapOut = 0.0d;
    private static int retryDailySwapCount = 0;
    private static int retryTimeOnCount = 0;
    private static double swapOutCount = 0.0d;
    private static double timeCostHours = 0.001d;
    private static int timeFlag = 0;
    private AppScore appScore;
    private Handler handler;

    private NandLife() {
        this.handler = null;
        this.appScore = null;
        this.appScore = AppScore.getInstance();
        Slog.i(TAG, "nandlife module created");
    }

    private void initHandler() {
        Looper looper = HyperHoldServiceThread.getInstance().getLooper();
        if (looper == null) {
            looper = BackgroundThread.get().getLooper();
            Slog.e(TAG, "HyperHold Service Thread get failed, use background looper instead");
        }
        this.handler = new Handler(looper) {
            /* class com.huawei.server.rme.hyperhold.NandLife.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                int i = msg.what;
                if (i == 1) {
                    NandLife.this.handleInitMsg();
                } else if (i == 2) {
                    NandLife.this.handleDailyCycleMsg();
                } else if (i != 3) {
                    Slog.e(NandLife.TAG, "invalid message!");
                } else {
                    NandLife.this.handleHoursCycleMsg();
                }
            }
        };
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleInitMsg() {
        AppModel.getInstance().initAllAppRatioAdjustParam();
        KernelInterface.getInstance().changeSwapOutSwitch(true);
        ArrayList<Double> storedMsg = ParaConfig.getInstance().getNandLifeCycleInfo();
        if (storedMsg.size() == 8) {
            periodQuota = storedMsg.get(0).doubleValue();
            dailyQuota = storedMsg.get(1).doubleValue();
            daysCount = storedMsg.get(2).doubleValue();
            daysInPeriod = storedMsg.get(3).doubleValue();
            daysOfPeriod = storedMsg.get(4).doubleValue();
            timeCostHours = storedMsg.get(5).doubleValue();
            periodSwapOut = storedMsg.get(6).doubleValue();
            swapOutCount = storedMsg.get(7).doubleValue();
            Slog.i(TAG, "nandlife local storage file read success");
            Slog.i(TAG, "nandlife init:curNandLCInfoIs " + periodQuota + " " + dailyQuota + " " + daysCount + " " + daysInPeriod + " " + daysOfPeriod + " " + timeCostHours + " " + periodSwapOut + " " + swapOutCount);
        } else if (!storedMsg.isEmpty()) {
            updateNandLifeInfoInCycle("init begin");
            Slog.e(TAG, "nandlife an terrible error happened: stored data damaged");
        } else {
            updateNandLifeInfoInCycle("init begin");
            Slog.i(TAG, "nandlife no local storage file");
        }
        double toNowSwapOutCount = KernelInterface.getInstance().getSwapOutCountFromBoot();
        if (toNowSwapOutCount > 0.0d) {
            lastSwapOutCountFromBoot = toNowSwapOutCount;
            Slog.i(TAG, "nandlife system server reboot, toNowCount = " + toNowSwapOutCount);
        } else {
            Slog.i(TAG, "nandlife phone reboot, toNowCount = " + toNowSwapOutCount);
        }
        Message msg = this.handler.obtainMessage();
        if (storedMsg.isEmpty() || (doubleLessEqualCompare(dailyQuota, 0.0d) && doubleLessEqualCompare(swapOutCount, 0.0d) && doubleLessEqualCompare(UPDATE_IO_LIMIT, timeCostHours))) {
            msg.what = 2;
            this.handler.sendMessage(msg);
            return;
        }
        msg.what = 3;
        if (checkSwapOutCondition()) {
            KernelInterface.getInstance().feedDog(true);
        }
        this.handler.sendMessageDelayed(msg, 900000);
    }

    private void updateNandLifeInfoInCycle(String timeCycle) {
        ParaConfig.getInstance().updateNandLifeCycleInfo(getStoreData());
        Slog.i(TAG, "nandlife " + timeCycle + ":curNandLCInfoIs " + periodQuota + " " + dailyQuota + " " + daysCount + " " + daysInPeriod + " " + daysOfPeriod + " " + timeCostHours + " " + periodSwapOut + " " + swapOutCount);
    }

    private void openSwapOut() {
        if (!KernelInterface.getInstance().changeSwapOutSwitch(true)) {
            Slog.e(TAG, "nandlife open swapOut failed");
        }
    }

    private void closeSwapOut() {
        if (!KernelInterface.getInstance().changeSwapOutSwitch(false)) {
            Slog.e(TAG, "nandlife close swap out switch failed");
        }
    }

    private void disableSwapOutForever() {
        SystemProperties.set("persist.sys.hyperhold.swapout.enable", "0");
    }

    private void getDailySwapQuota() {
        double dailyQuotaMaxCfg = Double.valueOf((double) ResourceCollector.getDailySwapGB()).doubleValue();
        if (equalCompare(dailyQuotaMaxCfg, 0.0d)) {
            lifeInDanger = true;
            Slog.i(TAG, "lifetime warning");
        } else {
            lifeInDanger = false;
            Slog.i(TAG, "lifetime not in danger");
        }
        dailyQuota = Double.valueOf((double) ParaConfig.getInstance().getStorageLifeParam().getDailySwapOutQuotaMax()).doubleValue();
        Slog.i(TAG, "nandlife daily quota is: " + dailyQuota + " cfgMin= " + dailyQuotaMaxCfg);
        if (doubleLessEqualCompare(minDaysFactBegin, daysCount)) {
            dailyQuota = Math.min(dailyQuota, dailyQuotaMaxCfg);
        }
    }

    private void checkIfNeedRetry() {
        if (retryDailySwapCount > 0) {
            getDailySwapQuota();
            if (dailyQuota > 0.0d) {
                retryDailySwapCount = 0;
            } else {
                dailyQuota = 0.0d;
                retryDailySwapCount--;
            }
        }
        if (retryTimeOnCount > 0) {
            double timeOn = Double.valueOf((double) PersistingData.getInstance().getTimeOn()).doubleValue();
            Slog.i(TAG, "nandlife timeon is: " + timeOn);
            if (doubleLessEqualCompare(timeOn, 0.0d)) {
                retryTimeOnCount--;
                return;
            }
            retryTimeOnCount = 0;
            double daysFact = timeOn / 86400.0d;
            Slog.i(TAG, "nandlife daysFact is: " + daysFact);
            if (daysCount < daysFact) {
                daysCount = daysFact;
            }
        }
    }

    private void checkOpenSwapOutProp() {
        if (SystemProperties.getBoolean("persist.sys.hyperhold.swapout.enable.cfg", false)) {
            SystemProperties.set("persist.sys.hyperhold.swapout.enable", "1");
        }
    }

    private boolean checkSwapOutCondition() {
        boolean dailyAllow = true;
        boolean periodAllow = true;
        boolean storageAllow = true;
        if (!ParaConfig.getInstance().getSwapOutSwitch()) {
            Slog.e(TAG, "nandlife close swap out switch from xml");
            closeSwapOut();
            disableSwapOutForever();
            return false;
        }
        checkOpenSwapOutProp();
        if (ParaConfig.getInstance().getStorageEndStatus()) {
            Slog.i(TAG, "nandlife storage end statisfy, close swapOut");
            storageAllow = false;
        }
        if (doubleLessEqualCompare(dailyQuota, 0.01d) || doubleLessEqualCompare(dailyQuota, swapOutCount)) {
            Slog.i(TAG, "nandlife swap Out daily quota is used out, swapOut will be closed");
            dailyAllow = false;
        }
        if (doubleLessEqualCompare(periodQuota, 0.01d) || doubleLessEqualCompare(periodQuota, periodSwapOut)) {
            Slog.i(TAG, "nandlife swap Out periodly quota is used out, swapOut will be closed");
            periodAllow = false;
        }
        if (dailyAllow && periodAllow && storageAllow) {
            return true;
        }
        closeSwapOut();
        return false;
    }

    private void checkWriteBoosterCondition() {
        if (!ParaConfig.getInstance().getWriteBoostSwitch(lifeInDanger)) {
            KernelInterface.getInstance().changeWriteBoostSwitch(true);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleHoursCycleMsg() {
        timeFlag++;
        if (timeFlag % 4 == 0) {
            this.appScore.handleHourCycleMsg();
        }
        double toNowSwapOutCount = KernelInterface.getInstance().getSwapOutCountFromBoot();
        swapOutCount += Math.max(toNowSwapOutCount - lastSwapOutCountFromBoot, 0.0d);
        periodSwapOut += Math.max(toNowSwapOutCount - lastSwapOutCountFromBoot, 0.0d);
        lastSwapOutCountFromBoot = toNowSwapOutCount;
        timeCostHours += UPDATE_IO_STEP;
        checkIfNeedRetry();
        updateNandLifeInfoInCycle("hour cycle");
        Message msg = this.handler.obtainMessage();
        if (timeCostHours < UPDATE_IO_LIMIT) {
            msg.what = 3;
            if (checkSwapOutCondition()) {
                KernelInterface.getInstance().feedDog(true);
            }
            this.handler.sendMessageDelayed(msg, 900000);
            checkWriteBoosterCondition();
            return;
        }
        msg.what = 2;
        swapOutCount = -1.0d;
        dailyQuota = -1.0d;
        daysCount += 1.0d;
        ArrayList<Double> resetLocalNandInfo = getStoreData();
        Slog.i(TAG, "nandlife today is finish:curNandLCInfoIs " + periodQuota + " " + dailyQuota + " " + daysCount + " " + daysInPeriod + " " + daysOfPeriod + " " + timeCostHours + " " + periodSwapOut + " " + swapOutCount);
        ParaConfig.getInstance().updateNandLifeCycleInfo(resetLocalNandInfo);
        this.handler.sendMessage(msg);
    }

    private boolean firstTimeAfterLowLevelReset() {
        if (equalCompare(timeCostHours, 0.001d)) {
            return true;
        }
        return false;
    }

    private void initPeriodInfo() {
        double daysOfPeriodCfg = Double.valueOf((double) ParaConfig.getInstance().getStorageLifeParam().getSwapControlDays()).doubleValue();
        if (doubleLessEqualCompare(UPDATE_IO_LIMIT, timeCostHours)) {
            daysInPeriod += 1.0d;
        }
        if (daysOfPeriodCfg < 0.0d) {
            minDaysFactBegin = 0.0d - daysOfPeriodCfg;
            daysOfPeriodCfg = 7.0d;
        }
        if (!equalCompare(daysOfPeriod, daysOfPeriodCfg)) {
            daysOfPeriod = daysOfPeriodCfg;
        }
        double periodQuotaMaxCfg = Double.valueOf((double) ParaConfig.getInstance().getStorageLifeParam().getPeriodSwapOutQuotaMax()).doubleValue();
        if (doubleLessEqualCompare(daysOfPeriod, daysInPeriod) || firstTimeAfterLowLevelReset()) {
            daysInPeriod = 0.0d;
            periodQuota = periodQuotaMaxCfg;
            periodSwapOut = 0.0d;
        }
    }

    private void initDailyInfo() {
        getDailySwapQuota();
        if (dailyQuota < 0.0d && doubleLessEqualCompare(minDaysFactBegin, daysCount)) {
            retryDailySwapCount = 20;
            dailyQuota = 0.0d;
        }
        timeCostHours = 0.0d;
        double toNowSwapOutCount = KernelInterface.getInstance().getSwapOutCountFromBoot();
        swapOutCount += toNowSwapOutCount - lastSwapOutCountFromBoot;
        if (swapOutCount < 0.0d) {
            swapOutCount = 0.0d;
        }
        lastSwapOutCountFromBoot = toNowSwapOutCount;
    }

    private void updateLifeContrlByUserInfo() {
        double timeOn = Double.valueOf((double) PersistingData.getInstance().getTimeOn()).doubleValue();
        if (doubleLessEqualCompare(timeOn, 0.0d)) {
            retryTimeOnCount = 20;
        }
        double daysFact = timeOn / 86400.0d;
        Slog.i(TAG, "nandlife daysFact is: " + daysFact);
        Slog.i(TAG, "nandlife timeon is: " + timeOn);
        double d = daysCount;
        if (d < daysFact) {
            double delta = daysFact - d;
            if (delta < daysOfPeriod) {
                daysInPeriod += delta;
            }
            double d2 = daysInPeriod;
            double d3 = daysOfPeriod;
            if (d2 >= d3) {
                daysInPeriod = d2 - d3;
            }
            daysCount = daysFact;
        }
        initPeriodInfo();
        initDailyInfo();
        double nDays = Double.valueOf((double) ParaConfig.getInstance().getStorageLifeParam().getSwapRedDeclineRatio()).doubleValue();
        Slog.i(TAG, "nandlife ndays is: " + nDays);
        double minControlBegin = minDaysControlBegin * nDays;
        if (doubleLessEqualCompare(daysFact, minControlBegin)) {
            daysFact = minControlBegin;
        }
        double daysNormal = Double.valueOf((double) ParaConfig.getInstance().getStorageLifeParam().getLeftOfLife()).doubleValue() * 109.0d;
        Slog.i(TAG, "nandlife daysNormal is: " + daysNormal);
        if (daysFact < daysNormal) {
            periodQuota -= daysOfPeriod * (((daysNormal - daysFact) * 100.0d) / daysFact);
        }
        if (periodQuota < 0.0d) {
            periodQuota = 0.0d;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleDailyCycleMsg() {
        this.appScore.handleDailyCycleMsg();
        checkWriteBoosterCondition();
        updateLifeContrlByUserInfo();
        updateNandLifeInfoInCycle("daily cycle");
        if (checkSwapOutCondition()) {
            openSwapOut();
        }
        Message msg = this.handler.obtainMessage();
        msg.what = 3;
        this.handler.sendMessageDelayed(msg, 900000);
    }

    private boolean doubleLessEqualCompare(double left, double right) {
        if (left < right) {
            return true;
        }
        return equalCompare(left, right);
    }

    private boolean equalCompare(double left, double right) {
        if (Math.abs(left - right) < EPSILON) {
            return true;
        }
        return false;
    }

    private ArrayList<Double> getStoreData() {
        ArrayList<Double> ret = new ArrayList<>();
        ret.add(Double.valueOf(periodQuota));
        ret.add(Double.valueOf(dailyQuota));
        ret.add(Double.valueOf(daysCount));
        ret.add(Double.valueOf(daysInPeriod));
        ret.add(Double.valueOf(daysOfPeriod));
        ret.add(Double.valueOf(timeCostHours));
        ret.add(Double.valueOf(periodSwapOut));
        ret.add(Double.valueOf(swapOutCount));
        return ret;
    }

    public void init() {
        Slog.i(TAG, "nandlife init");
        initHandler();
        Message msg = this.handler.obtainMessage();
        msg.what = 1;
        this.handler.sendMessage(msg);
    }

    public double getSwapOutQuota() {
        Slog.i(TAG, "nandlife daily swap limit:  " + dailyQuota);
        return dailyQuota;
    }

    public double getSwapOutCount() {
        return swapOutCount;
    }

    public static NandLife getInstance() {
        if (nandLifeHandler == null) {
            synchronized (NandLife.class) {
                if (nandLifeHandler == null) {
                    nandLifeHandler = new NandLife();
                }
            }
        }
        Slog.i(TAG, "nandlife getinstance called");
        return nandLifeHandler;
    }
}
