package com.android.server.rms.iaware.appmng;

import android.rms.iaware.DumpData;
import android.rms.iaware.StatisticsData;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort;
import com.android.server.rms.algorithm.AwareUserHabit;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import java.util.ArrayList;
import java.util.Iterator;

public class AppMngDumpRadar {
    public static final int APPMNG_FEATURE_ID = 0;
    private static final int DUMPDATA_MAX_SIZE = 5;
    private static final int ONE_MINUTES = 60000;
    private static final int STATISTIC_DATA_TYPE = 1;
    private static AppMngDumpRadar sInstance;
    private ArrayList<DumpData> mAppMngDumpData;
    private ArrayList<StatisticsData> mStatisticsData;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.rms.iaware.appmng.AppMngDumpRadar.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.rms.iaware.appmng.AppMngDumpRadar.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.rms.iaware.appmng.AppMngDumpRadar.<clinit>():void");
    }

    public static synchronized AppMngDumpRadar getInstance() {
        AppMngDumpRadar appMngDumpRadar;
        synchronized (AppMngDumpRadar.class) {
            if (sInstance == null) {
                sInstance = new AppMngDumpRadar();
            }
            appMngDumpRadar = sInstance;
        }
        return appMngDumpRadar;
    }

    private AppMngDumpRadar() {
        this.mAppMngDumpData = null;
        this.mStatisticsData = null;
        this.mAppMngDumpData = new ArrayList();
        this.mStatisticsData = new ArrayList();
        init();
    }

    private void init() {
    }

    public ArrayList<DumpData> getDumpData(int time) {
        long currenttime = System.currentTimeMillis();
        synchronized (this.mAppMngDumpData) {
            if (this.mAppMngDumpData.isEmpty()) {
                return null;
            }
            ArrayList<DumpData> tempdumplist = new ArrayList();
            i = this.mAppMngDumpData.size() > DUMPDATA_MAX_SIZE ? this.mAppMngDumpData.size() - 5 : APPMNG_FEATURE_ID;
            while (i < this.mAppMngDumpData.size()) {
                DumpData tempDd = (DumpData) this.mAppMngDumpData.get(i);
                if (tempDd != null && currenttime - tempDd.getTime() < ((long) time) * 1000) {
                    tempdumplist.add(tempDd);
                }
                i += STATISTIC_DATA_TYPE;
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
            DumpData Dd = new DumpData(time, APPMNG_FEATURE_ID, operation, exetime, reason);
            synchronized (this.mAppMngDumpData) {
                if (this.mAppMngDumpData.isEmpty()) {
                    this.mAppMngDumpData.add(Dd);
                } else {
                    while (true) {
                        if (!this.mAppMngDumpData.isEmpty()) {
                            DumpData tempDd = (DumpData) this.mAppMngDumpData.get(APPMNG_FEATURE_ID);
                            if (tempDd != null) {
                                if (time - tempDd.getTime() > AppHibernateCst.DELAY_ONE_MINS) {
                                    this.mAppMngDumpData.remove(APPMNG_FEATURE_ID);
                                }
                            }
                        }
                        this.mAppMngDumpData.add(Dd);
                    }
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public ArrayList<StatisticsData> getStatisticsData() {
        if (!AwareAppMngSort.checkAppMngEnable()) {
            return null;
        }
        ArrayList<StatisticsData> tempList = new ArrayList();
        AwareUserHabit habit = AwareUserHabit.getInstance();
        if (habit != null) {
            ArrayList<StatisticsData> habitSDList = habit.getStatisticsData();
            if (!habitSDList.isEmpty()) {
                tempList.addAll(habitSDList);
            }
        }
        synchronized (this.mStatisticsData) {
            if (this.mStatisticsData.isEmpty()) {
                return tempList;
            }
            int i = APPMNG_FEATURE_ID;
            while (true) {
                if (i < this.mStatisticsData.size()) {
                    StatisticsData tempSd = (StatisticsData) this.mStatisticsData.get(i);
                    if (tempSd != null) {
                        tempList.add(new StatisticsData(tempSd.getFeatureId(), tempSd.getType(), tempSd.getSubType(), tempSd.getOccurCount(), tempSd.getTotalTime(), tempSd.getEffect(), tempSd.getStartTime(), tempSd.getEndTime()));
                    }
                    i += STATISTIC_DATA_TYPE;
                } else {
                    clearArrayList();
                    return tempList;
                }
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
                if (tempSd != null) {
                    tempSd.setOccurCount(APPMNG_FEATURE_ID);
                    tempSd.setTotalTime(APPMNG_FEATURE_ID);
                    tempSd.setEffect(APPMNG_FEATURE_ID);
                    tempSd.setStartTime(now);
                    tempSd.setEndTime(now);
                }
            }
        }
    }

    public void insertStatisticData(String subtype, int exetime, int effect) {
        if (subtype != null) {
            synchronized (this.mStatisticsData) {
                int size = this.mStatisticsData.size();
                long now = System.currentTimeMillis();
                for (int i = APPMNG_FEATURE_ID; i < size; i += STATISTIC_DATA_TYPE) {
                    StatisticsData data = (StatisticsData) this.mStatisticsData.get(i);
                    if (data != null && data.getSubType().equals(subtype)) {
                        data.setTotalTime(data.getTotalTime() + exetime);
                        data.setOccurCount(data.getOccurCount() + STATISTIC_DATA_TYPE);
                        data.setEffect(data.getEffect() + effect);
                        data.setEndTime(now);
                        return;
                    }
                }
                this.mStatisticsData.add(new StatisticsData(APPMNG_FEATURE_ID, STATISTIC_DATA_TYPE, subtype, STATISTIC_DATA_TYPE, exetime, effect, now, now));
            }
        }
    }
}
