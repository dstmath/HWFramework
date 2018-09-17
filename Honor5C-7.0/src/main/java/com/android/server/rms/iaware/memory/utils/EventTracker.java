package com.android.server.rms.iaware.memory.utils;

import android.rms.iaware.AwareLog;
import android.rms.iaware.DumpData;
import android.rms.iaware.StatisticsData;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import java.util.ArrayList;
import java.util.Iterator;

public final class EventTracker {
    private static final int MAX_DUMP_RECORD_TIME = 60000;
    public static final int MEMORY_FEATURE_ID = 0;
    private static final int STATISTICS_TYPE_ACTION = 2;
    private static final String TAG = "AwareMem_EventTracker";
    public static final int TRACK_TYPE_END = 1004;
    public static final int TRACK_TYPE_EXIT = 1001;
    public static final int TRACK_TYPE_INIT = 1000;
    public static final int TRACK_TYPE_KILL = 1002;
    public static final int TRACK_TYPE_STOP = 1005;
    public static final int TRACK_TYPE_TRIG = 1003;
    private static EventTracker mEventTracker;
    private boolean mDebug;
    private int mEvent;
    private ArrayList<DumpData> mRecordData;
    private ArrayList<StatisticsData> mStatisticsData;
    private long mTimeStamp;
    private boolean mValid;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.rms.iaware.memory.utils.EventTracker.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.rms.iaware.memory.utils.EventTracker.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.rms.iaware.memory.utils.EventTracker.<clinit>():void");
    }

    public static EventTracker getInstance() {
        EventTracker eventTracker;
        synchronized (EventTracker.class) {
            if (mEventTracker == null) {
                mEventTracker = new EventTracker();
            }
            eventTracker = mEventTracker;
        }
        return eventTracker;
    }

    private EventTracker() {
        this.mEvent = MEMORY_FEATURE_ID;
        this.mDebug = true;
        this.mValid = false;
        this.mTimeStamp = 0;
        this.mRecordData = new ArrayList();
        this.mStatisticsData = new ArrayList();
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

    public void enableTracker() {
        this.mDebug = true;
    }

    public void disableTracker() {
        this.mDebug = false;
    }

    public void trackEvent(int type, int newEvent, long newTimeStamp, String info) {
        if (this.mDebug) {
            switch (type) {
                case TRACK_TYPE_INIT /*1000*/:
                    this.mValid = true;
                    this.mEvent = newEvent;
                    this.mTimeStamp = newTimeStamp;
                    break;
                case TRACK_TYPE_EXIT /*1001*/:
                    if (newEvent <= 0) {
                        this.mValid = false;
                        AwareLog.i(TAG, AppHibernateCst.INVALID_PKG + format(this.mEvent, this.mTimeStamp) + " is abandoned for " + info);
                        break;
                    }
                    AwareLog.i(TAG, format(newEvent, newTimeStamp) + " is abandoned for event " + format(this.mEvent, this.mTimeStamp) + " is " + info);
                    break;
                case TRACK_TYPE_KILL /*1002*/:
                    AwareLog.i(TAG, AppHibernateCst.INVALID_PKG + format(this.mEvent, this.mTimeStamp) + " kill " + info);
                    break;
                case TRACK_TYPE_TRIG /*1003*/:
                    AwareLog.i(TAG, AppHibernateCst.INVALID_PKG + format(this.mEvent, this.mTimeStamp) + " trigger " + info);
                    break;
                case TRACK_TYPE_END /*1004*/:
                    this.mValid = false;
                    break;
                case TRACK_TYPE_STOP /*1005*/:
                    if (this.mValid) {
                        this.mValid = false;
                        AwareLog.i(TAG, format(this.mEvent, this.mTimeStamp) + " is removed for received event " + format(newEvent, newTimeStamp));
                        break;
                    }
                    break;
                default:
                    return;
            }
            return;
        }
        AwareLog.d(TAG, "trackEvent debug disabled");
    }

    public ArrayList<DumpData> getDumpData(int time) {
        long currenttime = System.currentTimeMillis();
        synchronized (this.mRecordData) {
            if (this.mRecordData.isEmpty()) {
                return null;
            }
            ArrayList<DumpData> tempdumplist = new ArrayList();
            i = this.mRecordData.size() > 5 ? this.mRecordData.size() - 5 : MEMORY_FEATURE_ID;
            while (i < this.mRecordData.size()) {
                DumpData tempDd = (DumpData) this.mRecordData.get(i);
                if (currenttime - tempDd.getTime() < ((long) time) * 1000) {
                    tempdumplist.add(tempDd);
                }
                i++;
            }
            if (tempdumplist.isEmpty()) {
                return null;
            }
            return tempdumplist;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void insertDumpData(long time, String operation, int exetime, String reason) {
        if (operation != null && reason != null) {
            DumpData Dd = new DumpData(time, MEMORY_FEATURE_ID, operation, exetime, reason);
            synchronized (this.mRecordData) {
                if (this.mRecordData.isEmpty()) {
                    this.mRecordData.add(Dd);
                } else {
                    while (true) {
                        if (!this.mRecordData.isEmpty()) {
                            if (time - ((DumpData) this.mRecordData.get(MEMORY_FEATURE_ID)).getTime() > AppHibernateCst.DELAY_ONE_MINS) {
                                this.mRecordData.remove(MEMORY_FEATURE_ID);
                            }
                        }
                        this.mRecordData.add(Dd);
                    }
                }
            }
        }
    }

    public ArrayList<StatisticsData> getStatisticsData() {
        synchronized (this.mStatisticsData) {
            if (this.mStatisticsData.isEmpty()) {
                return null;
            }
            ArrayList<StatisticsData> tempList = new ArrayList();
            int i = MEMORY_FEATURE_ID;
            while (true) {
                if (i < this.mStatisticsData.size()) {
                    StatisticsData tempSd = (StatisticsData) this.mStatisticsData.get(i);
                    tempList.add(new StatisticsData(tempSd.getFeatureId(), tempSd.getType(), tempSd.getSubType(), tempSd.getOccurCount(), tempSd.getTotalTime(), tempSd.getEffect(), tempSd.getStartTime(), tempSd.getEndTime()));
                    i++;
                } else {
                    clearArrayList();
                    return tempList;
                }
            }
        }
    }

    public void insertStatisticData(String subtype, int exetime, int effect) {
        if (subtype != null) {
            synchronized (this.mStatisticsData) {
                int size = this.mStatisticsData.size();
                long now = System.currentTimeMillis();
                for (int i = MEMORY_FEATURE_ID; i < size; i++) {
                    StatisticsData data = (StatisticsData) this.mStatisticsData.get(i);
                    if (data.getSubType().equals(subtype)) {
                        data.setTotalTime(data.getTotalTime() + exetime);
                        data.setOccurCount(data.getOccurCount() + 1);
                        data.setEffect(data.getEffect() + effect);
                        data.setEndTime(now);
                        return;
                    }
                }
                this.mStatisticsData.add(new StatisticsData(MEMORY_FEATURE_ID, STATISTICS_TYPE_ACTION, subtype, 1, exetime, effect, now, now));
            }
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
                tempSd.setOccurCount(MEMORY_FEATURE_ID);
                tempSd.setTotalTime(MEMORY_FEATURE_ID);
                tempSd.setEffect(MEMORY_FEATURE_ID);
                tempSd.setStartTime(now);
                tempSd.setEndTime(now);
            }
        }
    }
}
