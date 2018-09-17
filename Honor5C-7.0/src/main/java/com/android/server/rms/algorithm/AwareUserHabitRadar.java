package com.android.server.rms.algorithm;

import android.rms.iaware.StatisticsData;
import java.util.ArrayList;
import java.util.Iterator;

class AwareUserHabitRadar {
    static final int APPMNG_FEATURE_ID = 0;
    static final int STATISTIC_DATA_TYPE = 1;
    static final String SUBTYPE_HABIT_FIVE_DAYS_KILL = "habit-five_days-kill";
    static final String SUBTYPE_HABIT_KILL = "habit-seven_days_kill";
    static final String SUBTYPE_HABIT_ONE_DAYS_KILL = "habit-one_days-kill";
    static final String SUBTYPE_HABIT_PREDICT = "habit-predict";
    static final String SUBTYPE_HABIT_THREE_DAYS_KILL = "habit-three_days-kill";
    private long mStartTime;
    private ArrayList<StatisticsData> mStatisticsData;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.rms.algorithm.AwareUserHabitRadar.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.rms.algorithm.AwareUserHabitRadar.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.rms.algorithm.AwareUserHabitRadar.<clinit>():void");
    }

    protected AwareUserHabitRadar() {
        this.mStatisticsData = new ArrayList();
        this.mStartTime = 0;
    }

    protected AwareUserHabitRadar(long startTime) {
        this.mStatisticsData = new ArrayList();
        this.mStartTime = 0;
        this.mStartTime = startTime;
    }

    protected ArrayList<StatisticsData> getStatisticsData() {
        synchronized (this.mStatisticsData) {
            if (this.mStatisticsData.isEmpty()) {
                return null;
            }
            ArrayList<StatisticsData> tempList = new ArrayList();
            int i = APPMNG_FEATURE_ID;
            while (true) {
                if (i < this.mStatisticsData.size()) {
                    StatisticsData tempSd = (StatisticsData) this.mStatisticsData.get(i);
                    tempList.add(new StatisticsData(tempSd.getFeatureId(), tempSd.getType(), tempSd.getSubType(), tempSd.getOccurCount(), tempSd.getTotalTime(), tempSd.getEffect(), tempSd.getStartTime(), tempSd.getEndTime()));
                    i += STATISTIC_DATA_TYPE;
                } else {
                    clearArrayList();
                    return tempList;
                }
            }
        }
    }

    protected void insertStatisticData(String subtype, int exetime, int effect) {
        synchronized (this.mStatisticsData) {
            int size = this.mStatisticsData.size();
            long now = System.currentTimeMillis();
            for (int i = APPMNG_FEATURE_ID; i < size; i += STATISTIC_DATA_TYPE) {
                StatisticsData data = (StatisticsData) this.mStatisticsData.get(i);
                if (data.getSubType().equals(subtype)) {
                    data.setTotalTime(data.getTotalTime() + exetime);
                    data.setOccurCount(data.getOccurCount() + STATISTIC_DATA_TYPE);
                    data.setEffect(data.getEffect() + effect);
                    data.setEndTime(now);
                    return;
                }
            }
            this.mStatisticsData.add(new StatisticsData(APPMNG_FEATURE_ID, STATISTIC_DATA_TYPE, subtype, STATISTIC_DATA_TYPE, exetime, effect, this.mStartTime, now));
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
                tempSd.setOccurCount(APPMNG_FEATURE_ID);
                tempSd.setTotalTime(APPMNG_FEATURE_ID);
                tempSd.setEffect(APPMNG_FEATURE_ID);
                tempSd.setStartTime(now);
                tempSd.setEndTime(now);
            }
        }
    }
}
