package com.android.server.rms.iaware.srms;

import android.rms.iaware.AwareLog;
import android.rms.iaware.StatisticsData;
import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;

public class SRMSDumpRadar {
    private static final int INTERVAL_ELAPSED_TIME = 4;
    private static final int RESOURCE_FEATURE_ID = 0;
    private static final String TAG = "SRMSDumpRadar";
    private static volatile SRMSDumpRadar mSRMSDumpRadar;
    private static final String[] mSubTypeList = null;
    private ArrayList<Integer> mBigDataList;
    private ArrayList<StatisticsData> mStatisticsData;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.rms.iaware.srms.SRMSDumpRadar.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.rms.iaware.srms.SRMSDumpRadar.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.rms.iaware.srms.SRMSDumpRadar.<clinit>():void");
    }

    public static SRMSDumpRadar getInstance() {
        if (mSRMSDumpRadar == null) {
            synchronized (SRMSDumpRadar.class) {
                if (mSRMSDumpRadar == null) {
                    mSRMSDumpRadar = new SRMSDumpRadar();
                }
            }
        }
        return mSRMSDumpRadar;
    }

    private SRMSDumpRadar() {
        int i;
        this.mStatisticsData = null;
        this.mBigDataList = new ArrayList(INTERVAL_ELAPSED_TIME);
        for (i = RESOURCE_FEATURE_ID; i < INTERVAL_ELAPSED_TIME; i++) {
            this.mBigDataList.add(Integer.valueOf(RESOURCE_FEATURE_ID));
        }
        this.mStatisticsData = new ArrayList();
        for (i = RESOURCE_FEATURE_ID; i <= 1; i++) {
            this.mStatisticsData.add(new StatisticsData(RESOURCE_FEATURE_ID, 2, mSubTypeList[i], RESOURCE_FEATURE_ID, RESOURCE_FEATURE_ID, RESOURCE_FEATURE_ID, System.currentTimeMillis(), 0));
        }
    }

    public ArrayList<StatisticsData> getStatisticsData() {
        ArrayList<StatisticsData> dataList = new ArrayList();
        synchronized (this.mStatisticsData) {
            for (int i = RESOURCE_FEATURE_ID; i <= 1; i++) {
                dataList.add(new StatisticsData(RESOURCE_FEATURE_ID, 2, mSubTypeList[i], ((StatisticsData) this.mStatisticsData.get(i)).getOccurCount(), RESOURCE_FEATURE_ID, RESOURCE_FEATURE_ID, ((StatisticsData) this.mStatisticsData.get(i)).getStartTime(), System.currentTimeMillis()));
            }
            resetStatisticsData();
        }
        AwareLog.d(TAG, "SRMS getStatisticsData success");
        return dataList;
    }

    public void updateStatisticsData(int subTypeCode) {
        if (subTypeCode >= 0 && subTypeCode <= 1) {
            synchronized (this.mStatisticsData) {
                StatisticsData data = (StatisticsData) this.mStatisticsData.get(subTypeCode);
                data.setOccurCount(data.getOccurCount() + 1);
            }
        } else if (subTypeCode < 10 || subTypeCode > 13) {
            AwareLog.e(TAG, "error subTypeCode");
        } else {
            updateBigData(subTypeCode - 10);
        }
    }

    private void resetStatisticsData() {
        synchronized (this.mStatisticsData) {
            for (int i = RESOURCE_FEATURE_ID; i <= 1; i++) {
                ((StatisticsData) this.mStatisticsData.get(i)).setSubType(mSubTypeList[i]);
                ((StatisticsData) this.mStatisticsData.get(i)).setOccurCount(RESOURCE_FEATURE_ID);
                ((StatisticsData) this.mStatisticsData.get(i)).setStartTime(System.currentTimeMillis());
                ((StatisticsData) this.mStatisticsData.get(i)).setEndTime(0);
            }
        }
    }

    public String saveSRMSBigData(boolean clear) {
        StringBuilder data;
        synchronized (this.mBigDataList) {
            data = new StringBuilder("[iAwareSRMSStatis_Start]\n").append(makeSRMSJson().toString()).append("\n[iAwareSRMSStatis_End]");
            if (clear) {
                resetBigData();
            }
        }
        AwareLog.d(TAG, "SRMS saveSRMSBigData success:" + data);
        return data.toString();
    }

    private void updateBigData(int interval) {
        synchronized (this.mBigDataList) {
            this.mBigDataList.set(interval, Integer.valueOf(((Integer) this.mBigDataList.get(interval)).intValue() + 1));
        }
    }

    private void resetBigData() {
        for (int i = RESOURCE_FEATURE_ID; i < INTERVAL_ELAPSED_TIME; i++) {
            this.mBigDataList.set(i, Integer.valueOf(RESOURCE_FEATURE_ID));
        }
    }

    private JSONObject makeSRMSJson() {
        int countElapsedTimeLess20 = ((Integer) this.mBigDataList.get(RESOURCE_FEATURE_ID)).intValue();
        int countElapsedTimeLess60 = ((Integer) this.mBigDataList.get(1)).intValue();
        int countElapsedTimeLess100 = ((Integer) this.mBigDataList.get(2)).intValue();
        int countElapsedTimeMore100 = ((Integer) this.mBigDataList.get(3)).intValue();
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("elapsedTime_less20", countElapsedTimeLess20);
            jsonObj.put("elapsedTime_less60", countElapsedTimeLess60);
            jsonObj.put("elapsedTime_less100", countElapsedTimeLess100);
            jsonObj.put("elapsedTime_more100", countElapsedTimeMore100);
        } catch (JSONException e) {
            AwareLog.e(TAG, "make json error");
        }
        return jsonObj;
    }
}
