package com.android.server.rms.iaware.cpu;

import android.rms.iaware.AwareConstant.FeatureType;
import android.rms.iaware.AwareLog;
import android.rms.iaware.DumpData;
import android.rms.iaware.StatisticsData;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import java.util.ArrayList;
import java.util.List;

public class CpuDumpRadar {
    public static final int CPU_FEATURE_ID = FeatureType.getFeatureId(FeatureType.FEATURE_CPU);
    private static final int EXT_TYPE = 1;
    private static final int MAX_DUMP_DATA_NUM = 5;
    private static final int MAX_DUMP_DATA_TIME = 60000;
    public static final String STATISTICS_BG_TO_KBG_POLICY = "BgToKBgPolicy";
    public static final String STATISTICS_CHG_FREQ_POLICY = "SetFrequencyPolicy";
    public static final String STATISTICS_FORK_APP_POLICY = "ForkAppPolicy";
    public static final String STATISTICS_FORK_INIT_POLICY = "ForkInitPolicy";
    public static final String STATISTICS_INSERT_CGROUP_PROCS_POLICY = "InsertCgroupProcsPidPolicy";
    public static final String STATISTICS_KBG_TO_BG_POLICY = "KBgToBgPolicy";
    public static final String STATISTICS_RESET_FREQ_POLICY = "ResetFrequencyPolicy";
    public static final String STATISTICS_SCREEN_OFF_POLICY = "ScreenOffPolicy";
    public static final String STATISTICS_SCREEN_ON_POLICY = "ScreenOnPolicy";
    private static final String TAG = "CpuDumpRadar";
    private static CpuDumpRadar sInstance;
    private List<DumpData> mCpuDumpData;
    private List<StatisticsData> mCpuStatisticsData;

    public static synchronized CpuDumpRadar getInstance() {
        CpuDumpRadar cpuDumpRadar;
        synchronized (CpuDumpRadar.class) {
            if (sInstance == null) {
                sInstance = new CpuDumpRadar();
            }
            cpuDumpRadar = sInstance;
        }
        return cpuDumpRadar;
    }

    private CpuDumpRadar() {
        this.mCpuDumpData = null;
        this.mCpuStatisticsData = null;
        this.mCpuDumpData = new ArrayList();
        this.mCpuStatisticsData = new ArrayList();
        initCpuStatisticsData();
    }

    private void initCpuStatisticsData() {
        insertStatisticData(1, STATISTICS_SCREEN_ON_POLICY);
        insertStatisticData(1, STATISTICS_SCREEN_OFF_POLICY);
        insertStatisticData(1, STATISTICS_BG_TO_KBG_POLICY);
        insertStatisticData(1, STATISTICS_KBG_TO_BG_POLICY);
        insertStatisticData(1, STATISTICS_FORK_INIT_POLICY);
        insertStatisticData(1, STATISTICS_FORK_APP_POLICY);
        insertStatisticData(1, STATISTICS_INSERT_CGROUP_PROCS_POLICY);
        insertStatisticData(1, STATISTICS_CHG_FREQ_POLICY);
        insertStatisticData(1, STATISTICS_RESET_FREQ_POLICY);
    }

    private void insertDumpData(long time, String operation, int exetime, String reason) {
        if (operation != null && reason != null) {
            DumpData data = new DumpData(time, CPU_FEATURE_ID, operation, exetime, reason);
            synchronized (this.mCpuDumpData) {
                if (this.mCpuDumpData.isEmpty()) {
                    this.mCpuDumpData.add(data);
                } else {
                    while (!this.mCpuDumpData.isEmpty()) {
                        if (time - ((DumpData) this.mCpuDumpData.get(0)).getTime() > AppHibernateCst.DELAY_ONE_MINS) {
                            this.mCpuDumpData.remove(0);
                        }
                    }
                    this.mCpuDumpData.add(data);
                    while (this.mCpuDumpData.size() > 5) {
                        this.mCpuDumpData.remove(0);
                    }
                }
            }
        }
    }

    public ArrayList<DumpData> getDumpData(int time) {
        long currenttime = System.currentTimeMillis();
        synchronized (this.mCpuDumpData) {
            if (this.mCpuDumpData.isEmpty()) {
                return null;
            }
            ArrayList<DumpData> tempdumplist = new ArrayList();
            int dumpDataSize = this.mCpuDumpData.size();
            for (int i = 0; i < dumpDataSize; i++) {
                DumpData tempDd = (DumpData) this.mCpuDumpData.get(i);
                if (currenttime - tempDd.getTime() < ((long) time) * 1000) {
                    tempdumplist.add(tempDd);
                }
            }
            if (tempdumplist.isEmpty()) {
                return null;
            }
            return tempdumplist;
        }
    }

    public ArrayList<StatisticsData> getStatisticsData() {
        synchronized (this.mCpuStatisticsData) {
            if (this.mCpuStatisticsData.isEmpty()) {
                return null;
            }
            ArrayList<StatisticsData> tempList = new ArrayList();
            int statisticsDataSize = this.mCpuStatisticsData.size();
            for (int i = 0; i < statisticsDataSize; i++) {
                StatisticsData tempSd = (StatisticsData) this.mCpuStatisticsData.get(i);
                tempList.add(new StatisticsData(tempSd.getFeatureId(), tempSd.getType(), tempSd.getSubType(), tempSd.getOccurCount(), tempSd.getTotalTime(), tempSd.getEffect(), tempSd.getStartTime(), System.currentTimeMillis()));
            }
            clearArrayList();
            return tempList;
        }
    }

    private void clearArrayList() {
        synchronized (this.mCpuStatisticsData) {
            if (this.mCpuStatisticsData.isEmpty()) {
                return;
            }
            for (StatisticsData tempSd : this.mCpuStatisticsData) {
                tempSd.setOccurCount(0);
                tempSd.setTotalTime(0);
            }
        }
    }

    /* JADX WARNING: Missing block: B:16:0x002e, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private StatisticsData searchStatisticsDataFromSubtype(String subtype) {
        if (subtype == null) {
            return null;
        }
        synchronized (this.mCpuStatisticsData) {
            if (!this.mCpuStatisticsData.isEmpty()) {
                for (StatisticsData tempSd : this.mCpuStatisticsData) {
                    if (subtype.equals(tempSd.getSubType())) {
                        return tempSd;
                    }
                }
            }
        }
    }

    private void insertStatisticData(int type, String subtype) {
        if (subtype != null) {
            synchronized (this.mCpuStatisticsData) {
                this.mCpuStatisticsData.add(new StatisticsData(CPU_FEATURE_ID, type, subtype, 0, 0, 0, System.currentTimeMillis(), 0));
            }
        }
    }

    private void updateStatisticDataForSubtype(String subtype, int time) {
        if (subtype != null) {
            synchronized (this.mCpuStatisticsData) {
                StatisticsData tempSd = searchStatisticsDataFromSubtype(subtype);
                if (tempSd == null) {
                    AwareLog.e(TAG, "updateStatisticDataForSubtype search failed ,subtype = " + subtype);
                    return;
                }
                tempSd.setOccurCount(tempSd.getOccurCount() + 1);
                tempSd.setTotalTime(tempSd.getTotalTime() + time);
            }
        }
    }

    public void insertDumpInfo(long startTime, String operation, String reason, String subType) {
        int duration = (int) (System.currentTimeMillis() - startTime);
        getInstance().insertDumpData(startTime, operation, duration, reason);
        getInstance().updateStatisticDataForSubtype(subType, duration);
    }
}
