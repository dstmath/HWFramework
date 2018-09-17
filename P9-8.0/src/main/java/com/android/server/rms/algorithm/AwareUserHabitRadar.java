package com.android.server.rms.algorithm;

import android.rms.iaware.AwareConstant.FeatureType;
import android.rms.iaware.StatisticsData;
import java.util.ArrayList;
import java.util.Iterator;

class AwareUserHabitRadar {
    static final int APPMNG_FEATURE_ID = FeatureType.getFeatureId(FeatureType.FEATURE_APPMNG);
    static final int STATISTIC_DATA_TYPE = 1;
    static final String SUBTYPE_HABIT_KILL = "habit_kill";
    static final String SUBTYPE_HABIT_PREDICT = "habit-predict";
    private long mStartTime = 0;
    private ArrayList<StatisticsData> mStatisticsData = new ArrayList();

    protected AwareUserHabitRadar() {
    }

    protected AwareUserHabitRadar(long startTime) {
        this.mStartTime = startTime;
    }

    protected ArrayList<StatisticsData> getStatisticsData() {
        synchronized (this.mStatisticsData) {
            if (this.mStatisticsData.isEmpty()) {
                return null;
            }
            ArrayList<StatisticsData> tempList = new ArrayList();
            int size = this.mStatisticsData.size();
            for (int i = 0; i < size; i++) {
                StatisticsData tempSd = (StatisticsData) this.mStatisticsData.get(i);
                tempList.add(new StatisticsData(tempSd.getFeatureId(), tempSd.getType(), tempSd.getSubType(), tempSd.getOccurCount(), tempSd.getTotalTime(), tempSd.getEffect(), tempSd.getStartTime(), tempSd.getEndTime()));
            }
            clearArrayList();
            return tempList;
        }
    }

    protected void insertStatisticData(String subtype, int exetime, int effect) {
        synchronized (this.mStatisticsData) {
            int size = this.mStatisticsData.size();
            long now = System.currentTimeMillis();
            for (int i = 0; i < size; i++) {
                StatisticsData data = (StatisticsData) this.mStatisticsData.get(i);
                if (data.getSubType().equals(subtype)) {
                    data.setTotalTime(data.getTotalTime() + exetime);
                    data.setOccurCount(data.getOccurCount() + 1);
                    data.setEffect(data.getEffect() + effect);
                    data.setEndTime(now);
                    return;
                }
            }
            this.mStatisticsData.add(new StatisticsData(APPMNG_FEATURE_ID, 1, subtype, 1, exetime, effect, this.mStartTime, now));
        }
    }

    private void clearArrayList() {
        synchronized (this.mStatisticsData) {
            if (this.mStatisticsData.isEmpty()) {
                return;
            }
            Iterator<StatisticsData> it = this.mStatisticsData.iterator();
            long now = System.currentTimeMillis();
            while (it.hasNext()) {
                StatisticsData tempSd = (StatisticsData) it.next();
                tempSd.setOccurCount(0);
                tempSd.setTotalTime(0);
                tempSd.setEffect(0);
                tempSd.setStartTime(now);
                tempSd.setEndTime(now);
            }
        }
    }
}
