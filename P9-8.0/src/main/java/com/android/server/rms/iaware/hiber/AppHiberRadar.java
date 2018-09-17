package com.android.server.rms.iaware.hiber;

import android.os.Parcel;
import android.rms.iaware.AwareConstant.FeatureType;
import android.rms.iaware.AwareLog;
import android.rms.iaware.DumpData;
import android.rms.iaware.StatisticsData;
import java.util.ArrayList;

class AppHiberRadar {
    private static int APPHIBER_FEATURE_ID = FeatureType.getFeatureId(FeatureType.FEATURE_APPHIBER);
    private static final int MAX_RECORD_LEN = 5;
    private static String STATISTIC_SUBTYPE = "apphibernation";
    private static int STATISTIC_TYPE_EXEC = 2;
    private static final String TAG = "AppHiber_Radar";
    private static AppHiberRadar mInstance;
    private ArrayList<DumpData> mDumpData = new ArrayList();
    private StatisticsData mStatisticsData = new StatisticsData(APPHIBER_FEATURE_ID, STATISTIC_TYPE_EXEC, STATISTIC_SUBTYPE, 0, 0, 0, 0, 0);

    AppHiberRadar() {
    }

    protected static synchronized AppHiberRadar getInstance() {
        AppHiberRadar appHiberRadar;
        synchronized (AppHiberRadar.class) {
            if (mInstance == null) {
                mInstance = new AppHiberRadar();
            }
            appHiberRadar = mInstance;
        }
        return appHiberRadar;
    }

    protected void parseData(Parcel recvMsgParcel, int dumpId) {
        int len = recvMsgParcel.readInt();
        if (len <= 0) {
            AwareLog.w(TAG, "recvMsgParcel len <= 0");
            return;
        }
        if (len > 5) {
            AwareLog.d(TAG, "len = " + len + " out of range, set " + 5 + " as default.");
            len = 5;
        }
        switch (dumpId) {
            case 1:
                refreshDumpList(len, recvMsgParcel);
                break;
            case 2:
                refreshStatistics(recvMsgParcel);
                break;
            default:
                AwareLog.w(TAG, "dump Id invalid....");
                break;
        }
    }

    private void refreshDumpList(int dataLen, Parcel dataContent) {
        ArrayList<DumpData> dumpDataList = new ArrayList();
        for (int i = 0; i < dataLen; i++) {
            dumpDataList.add((DumpData) DumpData.CREATOR.createFromParcel(dataContent));
        }
        if (!dumpDataList.isEmpty()) {
            synchronized (this.mDumpData) {
                this.mDumpData.clear();
                this.mDumpData.addAll(dumpDataList);
            }
        }
    }

    private void refreshStatistics(Parcel dataContent) {
        StatisticsData statisticsData = (StatisticsData) StatisticsData.CREATOR.createFromParcel(dataContent);
        if (statisticsData != null) {
            synchronized (this.mStatisticsData) {
                this.mStatisticsData.setOccurCount(this.mStatisticsData.getOccurCount() + statisticsData.getOccurCount());
                this.mStatisticsData.setTotalTime(this.mStatisticsData.getTotalTime() + statisticsData.getTotalTime());
                this.mStatisticsData.setEffect(this.mStatisticsData.getEffect() + statisticsData.getEffect());
                this.mStatisticsData.setStartTime(this.mStatisticsData.getStartTime() + statisticsData.getStartTime());
                this.mStatisticsData.setEndTime(this.mStatisticsData.getEndTime() + statisticsData.getEndTime());
            }
        }
    }

    protected ArrayList<DumpData> getDumpData(int time) {
        synchronized (this.mDumpData) {
            if (this.mDumpData.isEmpty()) {
                return null;
            }
            ArrayList<DumpData> data = new ArrayList(this.mDumpData);
            this.mDumpData.clear();
            return data;
        }
    }

    protected ArrayList<StatisticsData> getStatisticsData() {
        ArrayList<StatisticsData> retList = new ArrayList();
        synchronized (this.mStatisticsData) {
            retList.add(new StatisticsData(this.mStatisticsData.getFeatureId(), this.mStatisticsData.getType(), this.mStatisticsData.getSubType(), this.mStatisticsData.getOccurCount(), this.mStatisticsData.getTotalTime(), this.mStatisticsData.getEffect(), this.mStatisticsData.getStartTime(), this.mStatisticsData.getEndTime()));
            this.mStatisticsData.setOccurCount(0);
            this.mStatisticsData.setTotalTime(0);
            this.mStatisticsData.setEffect(0);
            this.mStatisticsData.setStartTime(0);
            this.mStatisticsData.setEndTime(0);
        }
        return retList.isEmpty() ? null : retList;
    }
}
