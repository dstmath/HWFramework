package com.android.server.rms.iaware.srms;

import android.app.mtm.iaware.appmng.AppMngConstant.AppStartReason;
import android.app.mtm.iaware.appmng.AppMngConstant.AppStartSource;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareConstant.FeatureType;
import android.rms.iaware.AwareLog;
import android.rms.iaware.StatisticsData;
import android.text.TextUtils;
import android.util.ArrayMap;
import com.android.server.mtm.iaware.appmng.DecisionMaker;
import com.android.server.mtm.iaware.appmng.appstart.AwareAppStartupPolicy;
import java.util.ArrayList;
import java.util.Map.Entry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SRMSDumpRadar {
    private static final int INTERVAL_ELAPSED_TIME = 4;
    private static final int RESOURCE_FEATURE_ID = FeatureType.getFeatureId(FeatureType.FEATURE_RESOURCE);
    private static final String TAG = "SRMSDumpRadar";
    private static volatile SRMSDumpRadar mSRMSDumpRadar;
    private static final String[] mSubTypeList = new String[]{"EnterFgKeyAppBQ", "EnterBgKeyAppBQ"};
    private ArrayList<Integer> mBigDataList = new ArrayList(4);
    private ArrayMap<String, TrackFakeData> mFakeDataList = new ArrayMap();
    private long mFakeStartTime = this.mStartTime;
    private long mStartTime = System.currentTimeMillis();
    private ArrayMap<String, StartupData> mStartupDataList = new ArrayMap();
    private ArrayList<StatisticsData> mStatisticsData = null;

    private static class StartupData {
        private ArrayMap<String, int[]> mReasonList;

        /* synthetic */ StartupData(String pkg, StartupData -this1) {
            this(pkg);
        }

        private StartupData(String pkg) {
            this.mReasonList = new ArrayMap();
        }

        private void increase(String[] keys, int[]... values) {
            int length = keys.length;
            int i = 0;
            int index = 0;
            while (i < length) {
                String k = keys[i];
                int index2 = index + 1;
                int[] val = values[index];
                if (!(val == null || TextUtils.isEmpty(k))) {
                    int size = val.length;
                    int[] v = (int[]) this.mReasonList.get(k);
                    if (v == null) {
                        v = new int[size];
                        System.arraycopy(val, 0, v, 0, size);
                        this.mReasonList.put(k, v);
                    } else if (size == v.length) {
                        for (int i2 = 0; i2 < v.length; i2++) {
                            v[i2] = v[i2] + val[i2];
                        }
                    } else {
                        AwareLog.w(SRMSDumpRadar.TAG, "increase dis-match array size");
                    }
                }
                i++;
                index = index2;
            }
        }

        private boolean isAllowConsistent(boolean onlyDiff, int totalAlw) {
            boolean z = true;
            if (!onlyDiff) {
                return false;
            }
            int[] values;
            int smtAlw = 0;
            int index = this.mReasonList.indexOfKey("I");
            if (index >= 0) {
                values = (int[]) this.mReasonList.valueAt(index);
                if (values != null && values.length > 0) {
                    smtAlw = values[0];
                }
            }
            int usrAlw = 0;
            index = this.mReasonList.indexOfKey("U");
            if (index >= 0) {
                values = (int[]) this.mReasonList.valueAt(index);
                if (values != null && values.length > 1) {
                    usrAlw = values[0] + values[1];
                }
            }
            if (totalAlw != smtAlw + usrAlw) {
                z = false;
            }
            return z;
        }

        private boolean isNeedReport(int threshold, boolean onlyDiff) {
            int[] values;
            int totalAlw = 0;
            int nonIawareAlw = 0;
            int index = this.mReasonList.indexOfKey("T");
            if (index >= 0) {
                values = (int[]) this.mReasonList.valueAt(index);
                if (values != null && values.length > 1) {
                    totalAlw = values[0];
                    nonIawareAlw = values[1];
                }
            }
            if (isAllowConsistent(onlyDiff, totalAlw - nonIawareAlw)) {
                return false;
            }
            if (totalAlw >= threshold) {
                return true;
            }
            if (totalAlw == 0) {
                index = this.mReasonList.indexOfKey("I");
                if (index >= 0) {
                    values = (int[]) this.mReasonList.valueAt(index);
                    if (values != null && values.length > 0 && values[values.length - 1] > 0) {
                        return true;
                    }
                }
                index = this.mReasonList.indexOfKey("U");
                if (index >= 0) {
                    values = (int[]) this.mReasonList.valueAt(index);
                    return values != null && values.length > 0 && values[values.length - 1] > 0;
                }
            }
        }

        private String encodeString() {
            int i = 0;
            String[] tagOrderList = new String[]{"T", "U", "I", "H", "O", AppStartSource.THIRD_ACTIVITY.getDesc(), AppStartSource.THIRD_BROADCAST.getDesc(), AppStartSource.SYSTEM_BROADCAST.getDesc(), AppStartSource.START_SERVICE.getDesc(), AppStartSource.BIND_SERVICE.getDesc(), AppStartSource.PROVIDER.getDesc(), AppStartSource.SCHEDULE_RESTART.getDesc(), AppStartSource.JOB_SCHEDULE.getDesc(), AppStartSource.ALARM.getDesc(), AppStartSource.ACCOUNT_SYNC.getDesc(), AppStartReason.DEFAULT.getDesc(), AppStartReason.SYSTEM_APP.getDesc(), AppStartReason.LIST.getDesc()};
            StringBuilder result = new StringBuilder();
            boolean firstInsert = true;
            int length = tagOrderList.length;
            while (i < length) {
                String tag = tagOrderList[i];
                int size = this.mReasonList.size();
                for (int i2 = 0; i2 < size; i2++) {
                    String key = (String) this.mReasonList.keyAt(i2);
                    if (key.startsWith(tag)) {
                        String stat = getStatString((int[]) this.mReasonList.valueAt(i2));
                        if (!TextUtils.isEmpty(stat)) {
                            if (!firstInsert) {
                                result.append(',');
                            }
                            result.append(key).append('=').append(stat);
                            firstInsert = false;
                        }
                    }
                }
                i++;
            }
            return result.toString();
        }

        private String getStatString(int[] values) {
            StringBuilder result = new StringBuilder();
            boolean noZero = false;
            int size = values.length;
            for (int i = 0; i < size; i++) {
                if (values[i] > 0) {
                    result.append(values[i]);
                    noZero = true;
                }
                if (i < size - 1) {
                    result.append('#');
                }
            }
            if (!noZero) {
                result.delete(0, result.length());
            }
            return result.toString();
        }
    }

    private static class TrackFakeData {
        String cmp;
        private ArrayMap<String, Integer> mStatusList = new ArrayMap();

        public TrackFakeData(String cmp) {
            this.cmp = cmp;
        }

        public void updateStatus(String status) {
            Integer count = (Integer) this.mStatusList.get(status);
            if (count == null) {
                this.mStatusList.put(status, Integer.valueOf(1));
            } else {
                this.mStatusList.put(status, Integer.valueOf(count.intValue() + 1));
            }
        }

        public String toJsonStr() {
            JSONObject jsonObj = new JSONObject();
            try {
                jsonObj.put("cmp", this.cmp);
                for (Entry entry : this.mStatusList.entrySet()) {
                    jsonObj.put((String) entry.getKey(), ((Integer) entry.getValue()).intValue());
                }
            } catch (JSONException e) {
                AwareLog.e(SRMSDumpRadar.TAG, "TrackFakeData.toJsonStr catch JSONException.");
            }
            return jsonObj.toString();
        }
    }

    private int getBigdataThreshold(boolean beta) {
        AwareAppStartupPolicy policy = AwareAppStartupPolicy.self();
        if (policy != null) {
            return policy.getBigdataThreshold(beta);
        }
        return 0;
    }

    private JSONObject makeStartupJson(boolean forBeta, boolean clear, boolean onlyDiff) {
        String pkgPrefix = "com.";
        int prefixStart = "com.".length() - 1;
        int threshold = getBigdataThreshold(forBeta);
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("feature", "appstart");
            jsonObj.put("start", this.mStartTime);
            long currentTime = System.currentTimeMillis();
            if (clear) {
                this.mStartTime = currentTime;
            }
            jsonObj.put("end", currentTime);
            int index = 0;
            JSONObject dataJson = new JSONObject();
            for (Entry<String, StartupData> item : this.mStartupDataList.entrySet()) {
                String pkg = (String) item.getKey();
                StartupData startupData = (StartupData) item.getValue();
                if (startupData.isNeedReport(threshold, onlyDiff)) {
                    if (pkg.startsWith("com.")) {
                        dataJson.put(pkg.substring(prefixStart), startupData.encodeString());
                    } else {
                        dataJson.put(pkg, startupData.encodeString());
                    }
                    index++;
                }
            }
            dataJson.put("inf", "V" + DecisionMaker.getInstance().getVersion() + CPUCustBaseConfig.CPUCONFIG_INVALID_STR + index + CPUCustBaseConfig.CPUCONFIG_INVALID_STR + this.mStartupDataList.size());
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(dataJson);
            jsonObj.put("data", jsonArray);
        } catch (JSONException e) {
            AwareLog.e(TAG, "makeStartupJson catch JSONException e: " + e);
        }
        return jsonObj;
    }

    public void updateStartupData(String pkg, String[] keys, int[]... values) {
        if (!TextUtils.isEmpty(pkg) && keys != null && values != null && keys.length == values.length) {
            synchronized (this.mStartupDataList) {
                StartupData startupData = (StartupData) this.mStartupDataList.get(pkg);
                if (startupData != null) {
                    startupData.increase(keys, values);
                } else {
                    startupData = new StartupData(pkg, null);
                    startupData.increase(keys, values);
                    this.mStartupDataList.put(pkg, startupData);
                }
            }
        }
    }

    public String saveStartupBigData(boolean forBeta, boolean clear, boolean onlyDiff) {
        String data;
        synchronized (this.mStartupDataList) {
            data = makeStartupJson(forBeta, clear, onlyDiff).toString();
            if (clear) {
                this.mStartupDataList.clear();
            }
        }
        AwareLog.d(TAG, "saveStartupBigData forBeta=" + forBeta + ", clear=" + clear);
        return data;
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
        for (i = 0; i < 4; i++) {
            this.mBigDataList.add(Integer.valueOf(0));
        }
        this.mStatisticsData = new ArrayList();
        for (i = 0; i <= 1; i++) {
            this.mStatisticsData.add(new StatisticsData(RESOURCE_FEATURE_ID, 2, mSubTypeList[i], 0, 0, 0, System.currentTimeMillis(), 0));
        }
    }

    public ArrayList<StatisticsData> getStatisticsData() {
        ArrayList<StatisticsData> dataList = new ArrayList();
        synchronized (this.mStatisticsData) {
            for (int i = 0; i <= 1; i++) {
                dataList.add(new StatisticsData(RESOURCE_FEATURE_ID, 2, mSubTypeList[i], ((StatisticsData) this.mStatisticsData.get(i)).getOccurCount(), 0, 0, ((StatisticsData) this.mStatisticsData.get(i)).getStartTime(), System.currentTimeMillis()));
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
            for (int i = 0; i <= 1; i++) {
                ((StatisticsData) this.mStatisticsData.get(i)).setSubType(mSubTypeList[i]);
                ((StatisticsData) this.mStatisticsData.get(i)).setOccurCount(0);
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
        for (int i = 0; i < 4; i++) {
            this.mBigDataList.set(i, Integer.valueOf(0));
        }
    }

    private JSONObject makeSRMSJson() {
        int countElapsedTimeLess20 = ((Integer) this.mBigDataList.get(0)).intValue();
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

    public String getFakeBigData(boolean forBeta, boolean clear) {
        if (!forBeta) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        getFakeDetailData(sb, forBeta, clear);
        if (clear) {
            this.mFakeStartTime = System.currentTimeMillis();
        }
        AwareLog.d(TAG, "getFakeBigData success data : " + sb.toString());
        return sb.toString();
    }

    private void getFakeDetailData(StringBuilder sb, boolean forBeta, boolean clear) {
        if (forBeta && sb != null) {
            synchronized (this.mFakeDataList) {
                sb.append("\n[iAwareFake_Start]\nstartTime: ").append(String.valueOf(this.mFakeStartTime));
                for (Entry<String, TrackFakeData> dataEntry : this.mFakeDataList.entrySet()) {
                    TrackFakeData data = (TrackFakeData) dataEntry.getValue();
                    if (data != null) {
                        String dataStr = data.toJsonStr();
                        if (dataStr != null && dataStr.length() > 0) {
                            sb.append("\n").append(dataStr.replace("\\", ""));
                        }
                    }
                }
                sb.append("\nendTime: ").append(String.valueOf(System.currentTimeMillis())).append("\n[iAwareFake_End]");
                if (clear) {
                    this.mFakeDataList.clear();
                }
            }
        }
    }

    public void updateFakeData(String cmp, String status) {
        if (isBetaUser() && cmp != null && status != null) {
            synchronized (this.mFakeDataList) {
                TrackFakeData fakeData = (TrackFakeData) this.mFakeDataList.get(cmp);
                if (fakeData == null) {
                    fakeData = new TrackFakeData(cmp);
                    this.mFakeDataList.put(cmp, fakeData);
                }
                fakeData.updateStatus(status);
            }
        }
    }

    private boolean isBetaUser() {
        return AwareConstant.CURRENT_USER_TYPE == 3;
    }
}
