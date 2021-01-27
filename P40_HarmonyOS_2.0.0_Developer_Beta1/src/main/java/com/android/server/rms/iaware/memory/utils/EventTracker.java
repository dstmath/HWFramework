package com.android.server.rms.iaware.memory.utils;

import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.rms.iaware.DumpData;
import android.rms.iaware.StatisticsData;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import java.util.ArrayList;
import java.util.Iterator;

public final class EventTracker {
    private static final int DEFAULT_ARRAYLIST_SIZE = 10;
    private static final int DUMP_DATA_SIZE = 5;
    private static final Object EVENT_TRACKER_LOCK = new Object();
    private static final int MAX_DUMP_RECORD_TIME = 60000;
    public static final int MEMORY_FEATURE_ID = AwareConstant.FeatureType.getFeatureId(AwareConstant.FeatureType.FEATURE_MEMORY);
    private static final int STATISTICS_TYPE_ACTION = 2;
    private static final String TAG = "AwareMem_EventTracker";
    private static final long TIME_MS_FACTOR = 1000;
    public static final int TRACK_TYPE_END = 1004;
    public static final int TRACK_TYPE_EXIT = 1001;
    public static final int TRACK_TYPE_INIT = 1000;
    public static final int TRACK_TYPE_KILL = 1002;
    public static final int TRACK_TYPE_STOP = 1005;
    public static final int TRACK_TYPE_TRIG = 1003;
    private static EventTracker sEventTracker = null;
    private int mEvent = 0;
    private boolean mIsValid = false;
    private final ArrayList<DumpData> mRecordDatas = new ArrayList<>(10);
    private final ArrayList<StatisticsData> mStatisticsDatas = new ArrayList<>(10);
    private long mTimeStamp = 0;

    private EventTracker() {
    }

    public static EventTracker getInstance() {
        EventTracker eventTracker;
        synchronized (EVENT_TRACKER_LOCK) {
            if (sEventTracker == null) {
                sEventTracker = new EventTracker();
            }
            eventTracker = sEventTracker;
        }
        return eventTracker;
    }

    public static String toString(int event) {
        switch (event) {
            case 10001:
                return "TOUCH_DOWN";
            case 15001:
                return "APP_PROCESS_LAUNCHER_BEGIN";
            case 15003:
                return "APP_PROCESS_EXIT_BEGIN";
            case 15005:
                return "APP_ACTIVITY_BEGIN";
            case 20011:
                return "SCREEN_ON";
            case 30002:
                return "POLLING_TIMEOUT";
            case 80001:
                return "TOUCH_UP";
            case 85001:
                return "APP_PROCESS_LAUNCHER_FINISH";
            case 85003:
                return "APP_PROCESS_EXIT_FINIFH";
            case 85005:
                return "APP_ACTIVITY_FINISH";
            case 90011:
                return "SCREEN_OFF";
            default:
                return "Unknown:" + event;
        }
    }

    private static String format(int event, long timeStamp) {
        return "[" + toString(event) + "_" + timeStamp + "]";
    }

    public void trackEvent(int type, int newEvent, long newTimeStamp, String info) {
        switch (type) {
            case 1000:
                this.mIsValid = true;
                this.mEvent = newEvent;
                this.mTimeStamp = newTimeStamp;
                return;
            case 1001:
                if (newEvent > 0) {
                    AwareLog.i(TAG, format(newEvent, newTimeStamp) + " is abandoned for event " + format(this.mEvent, this.mTimeStamp) + " is " + info);
                    return;
                }
                this.mIsValid = false;
                AwareLog.i(TAG, "" + format(this.mEvent, this.mTimeStamp) + " is abandoned for " + info);
                return;
            case 1002:
                AwareLog.i(TAG, "" + format(this.mEvent, this.mTimeStamp) + " kill " + info);
                return;
            case TRACK_TYPE_TRIG /* 1003 */:
                AwareLog.i(TAG, "" + format(this.mEvent, this.mTimeStamp) + " trigger " + info);
                return;
            case TRACK_TYPE_END /* 1004 */:
                this.mIsValid = false;
                return;
            case TRACK_TYPE_STOP /* 1005 */:
                if (this.mIsValid) {
                    this.mIsValid = false;
                    AwareLog.i(TAG, format(this.mEvent, this.mTimeStamp) + " is removed for received event " + format(newEvent, newTimeStamp));
                    return;
                }
                return;
            default:
                return;
        }
    }

    public ArrayList<DumpData> getDumpData(int time) {
        long currentTime = System.currentTimeMillis();
        synchronized (this.mRecordDatas) {
            try {
                if (this.mRecordDatas.isEmpty()) {
                    return null;
                }
                ArrayList<DumpData> tempdumplists = new ArrayList<>(10);
                int recordDatasSize = this.mRecordDatas.size();
                for (int idx = recordDatasSize > 5 ? recordDatasSize - 5 : 0; idx < recordDatasSize; idx++) {
                    DumpData tempDd = this.mRecordDatas.get(idx);
                    if (currentTime - tempDd.getTime() < ((long) time) * 1000) {
                        tempdumplists.add(tempDd);
                    }
                }
                if (tempdumplists.isEmpty()) {
                    return null;
                }
                return tempdumplists;
            } catch (Throwable th) {
                th = th;
                throw th;
            }
        }
    }

    public void insertDumpData(long time, String operation, int exeTime, String reason) {
        if (operation != null && reason != null) {
            DumpData dumpData = new DumpData(time, MEMORY_FEATURE_ID, operation, exeTime, reason);
            synchronized (this.mRecordDatas) {
                if (this.mRecordDatas.isEmpty()) {
                    this.mRecordDatas.add(dumpData);
                    return;
                }
                while (!this.mRecordDatas.isEmpty()) {
                    if (!(time - this.mRecordDatas.get(0).getTime() > AppHibernateCst.DELAY_ONE_MINS)) {
                        break;
                    }
                    this.mRecordDatas.remove(0);
                }
                this.mRecordDatas.add(dumpData);
            }
        }
    }

    public ArrayList<StatisticsData> getStatisticsData() {
        synchronized (this.mStatisticsDatas) {
            if (this.mStatisticsDatas.isEmpty()) {
                return null;
            }
            ArrayList<StatisticsData> tempList = new ArrayList<>(10);
            int statisticsDatasSize = this.mStatisticsDatas.size();
            for (int i = 0; i < statisticsDatasSize; i++) {
                StatisticsData tempSd = this.mStatisticsDatas.get(i);
                tempList.add(new StatisticsData(tempSd.getFeatureId(), tempSd.getType(), tempSd.getSubType(), tempSd.getOccurCount(), tempSd.getTotalTime(), tempSd.getEffect(), tempSd.getStartTime(), tempSd.getEndTime()));
            }
            clearArrayList();
            return tempList;
        }
    }

    public void insertStatisticData(String subType, int exeTime, int effect) {
        if (subType != null) {
            synchronized (this.mStatisticsDatas) {
                int size = this.mStatisticsDatas.size();
                long now = System.currentTimeMillis();
                for (int i = 0; i < size; i++) {
                    StatisticsData data = this.mStatisticsDatas.get(i);
                    if (data.getSubType().equals(subType)) {
                        data.setTotalTime(data.getTotalTime() + exeTime);
                        data.setOccurCount(data.getOccurCount() + 1);
                        data.setEffect(data.getEffect() + effect);
                        data.setEndTime(now);
                        return;
                    }
                }
                this.mStatisticsDatas.add(new StatisticsData(MEMORY_FEATURE_ID, 2, subType, 1, exeTime, effect, now, now));
            }
        }
    }

    private void clearArrayList() {
        synchronized (this.mStatisticsDatas) {
            if (!this.mStatisticsDatas.isEmpty()) {
                Iterator<StatisticsData> it = this.mStatisticsDatas.iterator();
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
