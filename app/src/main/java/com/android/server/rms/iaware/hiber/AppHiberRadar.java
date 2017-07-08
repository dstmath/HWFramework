package com.android.server.rms.iaware.hiber;

import android.os.Parcel;
import android.rms.iaware.AwareLog;
import android.rms.iaware.DumpData;
import android.rms.iaware.StatisticsData;
import huawei.com.android.server.policy.HwGlobalActionsData;
import java.util.ArrayList;

class AppHiberRadar {
    private static int APPHIBER_FEATURE_ID = 0;
    private static final int MAX_RECORD_LEN = 5;
    private static String STATISTIC_SUBTYPE = null;
    private static int STATISTIC_TYPE_EXEC = 0;
    private static final String TAG = "AppHiber_Radar";
    private static AppHiberRadar mInstance;
    private ArrayList<DumpData> mDumpData;
    private StatisticsData mStatisticsData;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.rms.iaware.hiber.AppHiberRadar.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.rms.iaware.hiber.AppHiberRadar.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.rms.iaware.hiber.AppHiberRadar.<clinit>():void");
    }

    AppHiberRadar() {
        this.mDumpData = new ArrayList();
        this.mStatisticsData = new StatisticsData(APPHIBER_FEATURE_ID, STATISTIC_TYPE_EXEC, STATISTIC_SUBTYPE, 0, 0, 0, 0, 0);
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
        if (len > MAX_RECORD_LEN) {
            AwareLog.d(TAG, "len = " + len + " out of range, set " + MAX_RECORD_LEN + " as default.");
            len = MAX_RECORD_LEN;
        }
        switch (dumpId) {
            case HwGlobalActionsData.FLAG_AIRPLANEMODE_ON /*1*/:
                refreshDumpList(len, recvMsgParcel);
                break;
            case HwGlobalActionsData.FLAG_AIRPLANEMODE_OFF /*2*/:
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
