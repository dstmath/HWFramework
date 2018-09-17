package com.android.server.rms.iaware.cpu;

import android.rms.iaware.AwareLog;
import android.rms.iaware.DumpData;
import android.rms.iaware.StatisticsData;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import java.util.ArrayList;
import java.util.List;

public class CpuDumpRadar {
    public static final int CPU_FEATURE_ID = 0;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.rms.iaware.cpu.CpuDumpRadar.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.rms.iaware.cpu.CpuDumpRadar.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.rms.iaware.cpu.CpuDumpRadar.<clinit>():void");
    }

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
        insertStatisticData(EXT_TYPE, STATISTICS_SCREEN_ON_POLICY);
        insertStatisticData(EXT_TYPE, STATISTICS_SCREEN_OFF_POLICY);
        insertStatisticData(EXT_TYPE, STATISTICS_BG_TO_KBG_POLICY);
        insertStatisticData(EXT_TYPE, STATISTICS_KBG_TO_BG_POLICY);
        insertStatisticData(EXT_TYPE, STATISTICS_FORK_INIT_POLICY);
        insertStatisticData(EXT_TYPE, STATISTICS_FORK_APP_POLICY);
        insertStatisticData(EXT_TYPE, STATISTICS_INSERT_CGROUP_PROCS_POLICY);
        insertStatisticData(EXT_TYPE, STATISTICS_CHG_FREQ_POLICY);
        insertStatisticData(EXT_TYPE, STATISTICS_RESET_FREQ_POLICY);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void insertDumpData(long time, String operation, int exetime, String reason) {
        if (operation != null && reason != null) {
            DumpData data = new DumpData(time, CPU_FEATURE_ID, operation, exetime, reason);
            synchronized (this.mCpuDumpData) {
                if (this.mCpuDumpData.isEmpty()) {
                    this.mCpuDumpData.add(data);
                } else {
                    while (true) {
                        if (!this.mCpuDumpData.isEmpty()) {
                            if (time - ((DumpData) this.mCpuDumpData.get(CPU_FEATURE_ID)).getTime() > AppHibernateCst.DELAY_ONE_MINS) {
                                this.mCpuDumpData.remove(CPU_FEATURE_ID);
                            }
                        }
                        this.mCpuDumpData.add(data);
                        while (this.mCpuDumpData.size() > MAX_DUMP_DATA_NUM) {
                            this.mCpuDumpData.remove(CPU_FEATURE_ID);
                        }
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
            for (int i = CPU_FEATURE_ID; i < this.mCpuDumpData.size(); i += EXT_TYPE) {
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
            int i = CPU_FEATURE_ID;
            while (true) {
                if (i < this.mCpuStatisticsData.size()) {
                    StatisticsData tempSd = (StatisticsData) this.mCpuStatisticsData.get(i);
                    tempList.add(new StatisticsData(tempSd.getFeatureId(), tempSd.getType(), tempSd.getSubType(), tempSd.getOccurCount(), tempSd.getTotalTime(), tempSd.getEffect(), tempSd.getStartTime(), System.currentTimeMillis()));
                    i += EXT_TYPE;
                } else {
                    clearArrayList();
                    return tempList;
                }
            }
        }
    }

    private void clearArrayList() {
        synchronized (this.mCpuStatisticsData) {
            if (this.mCpuStatisticsData.isEmpty()) {
                return;
            }
            for (StatisticsData tempSd : this.mCpuStatisticsData) {
                tempSd.setOccurCount(CPU_FEATURE_ID);
                tempSd.setTotalTime(CPU_FEATURE_ID);
            }
        }
    }

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
            return null;
        }
    }

    private void insertStatisticData(int type, String subtype) {
        if (subtype != null) {
            synchronized (this.mCpuStatisticsData) {
                this.mCpuStatisticsData.add(new StatisticsData(CPU_FEATURE_ID, type, subtype, CPU_FEATURE_ID, CPU_FEATURE_ID, CPU_FEATURE_ID, System.currentTimeMillis(), 0));
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
                tempSd.setOccurCount(tempSd.getOccurCount() + EXT_TYPE);
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
