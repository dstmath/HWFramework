package com.android.server.rms.algorithm;

import android.rms.iaware.AwareConstant;
import android.rms.iaware.StatisticsData;
import java.util.ArrayList;
import java.util.Iterator;

/* access modifiers changed from: package-private */
public class AwareUserHabitRadar {
    static final int APPMNG_FEATURE_ID = AwareConstant.FeatureType.getFeatureId(AwareConstant.FeatureType.FEATURE_APPMNG);
    static final int STATISTIC_DATA_TYPE = 1;
    static final String SUBTYPE_HABIT_KILL = "habit_kill";
    static final String SUBTYPE_HABIT_PREDICT = "habit-predict";
    private long mStartTime = 0;
    private final ArrayList<StatisticsData> mStatisticsData = new ArrayList<>();

    protected AwareUserHabitRadar(long startTime) {
        this.mStartTime = startTime;
    }

    /* access modifiers changed from: protected */
    public ArrayList<StatisticsData> getStatisticsData() {
        synchronized (this.mStatisticsData) {
            if (this.mStatisticsData.isEmpty()) {
                return new ArrayList<>(0);
            }
            ArrayList<StatisticsData> tempList = new ArrayList<>();
            int size = this.mStatisticsData.size();
            for (int i = 0; i < size; i++) {
                StatisticsData tempSd = this.mStatisticsData.get(i);
                tempList.add(new StatisticsData(tempSd.getFeatureId(), tempSd.getType(), tempSd.getSubType(), tempSd.getOccurCount(), tempSd.getTotalTime(), tempSd.getEffect(), tempSd.getStartTime(), tempSd.getEndTime()));
            }
            clearArrayList();
            return tempList;
        }
    }

    /* access modifiers changed from: protected */
    public void insertStatisticData(String subtype, int exetime, int effect) {
        Throwable th;
        synchronized (this.mStatisticsData) {
            try {
                int size = this.mStatisticsData.size();
                long now = System.currentTimeMillis();
                for (int i = 0; i < size; i++) {
                    try {
                        StatisticsData data = this.mStatisticsData.get(i);
                        if (data.getSubType().equals(subtype)) {
                            data.setTotalTime(data.getTotalTime() + exetime);
                            data.setOccurCount(data.getOccurCount() + 1);
                            data.setEffect(data.getEffect() + effect);
                            data.setEndTime(now);
                            return;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        throw th;
                    }
                }
                this.mStatisticsData.add(new StatisticsData(APPMNG_FEATURE_ID, 1, subtype, 1, exetime, effect, this.mStartTime, now));
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    private void clearArrayList() {
        synchronized (this.mStatisticsData) {
            if (!this.mStatisticsData.isEmpty()) {
                Iterator<StatisticsData> it = this.mStatisticsData.iterator();
                long now = System.currentTimeMillis();
                while (it.hasNext()) {
                    StatisticsData tempSd = it.next();
                    tempSd.setOccurCount(0);
                    tempSd.setTotalTime(0);
                    tempSd.setEffect(0);
                    tempSd.setStartTime(now);
                    tempSd.setEndTime(now);
                }
            }
        }
    }
}
