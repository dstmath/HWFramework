package com.android.server.rms.iaware.srms;

import android.app.mtm.iaware.appmng.AppMngConstant;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.rms.iaware.StatisticsData;
import android.text.TextUtils;
import android.util.ArrayMap;
import com.android.server.mtm.iaware.appmng.DecisionMaker;
import com.android.server.mtm.iaware.appmng.appstart.AwareAppStartupPolicy;
import com.android.server.rms.dualfwk.AwareMiddleware;
import com.android.server.rms.iaware.cpu.CpuCustBaseConfig;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SrmsDumpRadar {
    private static final int INTERVAL_ELAPSED_TIME = 4;
    private static final Object LOCK = new Object();
    private static final int RESOURCE_FEATURE_ID = AwareConstant.FeatureType.getFeatureId(AwareConstant.FeatureType.FEATURE_RESOURCE);
    private static final String[] SUB_TYPE_LIST = {"EnterFgKeyAppBQ", "EnterBgKeyAppBQ"};
    private static final String TAG = "SrmsDumpRadar";
    private static volatile SrmsDumpRadar sSrmsDumpRadar;
    private final ArrayList<Integer> mBigDataList = new ArrayList<>(4);
    private final ArrayMap<String, TrackFakeData> mFakeDataList = new ArrayMap<>();
    private long mFakeStartTime = this.mStartTime;
    private long mStartTime = System.currentTimeMillis();
    private final ArrayMap<String, StartupData> mStartupDataList = new ArrayMap<>();
    private final ArrayList<StatisticsData> mStatisticsData = new ArrayList<>();

    /* access modifiers changed from: private */
    public static class StartupData {
        private boolean mIsTargetApp;
        private ArrayMap<String, int[]> mReasonList;

        private StartupData(String pkg) {
            this.mIsTargetApp = false;
            this.mReasonList = new ArrayMap<>();
            this.mIsTargetApp = AwareMiddleware.getInstance().isZApp(pkg);
        }

        /* JADX INFO: Multiple debug info for r3v2 int[]: [D('index' int), D('val' int[])] */
        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void increase(String[] keys, int[]... values) {
            int length = keys.length;
            int index = 0;
            int index2 = 0;
            while (index2 < length) {
                String k = keys[index2];
                int index3 = index + 1;
                int[] val = values[index];
                if (val != null && !TextUtils.isEmpty(k)) {
                    int size = val.length;
                    int[] value = this.mReasonList.get(k);
                    if (value == null) {
                        int[] value2 = new int[size];
                        System.arraycopy(val, 0, value2, 0, size);
                        this.mReasonList.put(k, value2);
                    } else if (size == value.length) {
                        for (int i = 0; i < value.length; i++) {
                            value[i] = value[i] + val[i];
                        }
                    } else {
                        AwareLog.w(SrmsDumpRadar.TAG, "increase dis-match array size");
                    }
                }
                index2++;
                index = index3;
            }
        }

        private boolean isAllowConsistent(boolean onlyDiff, int totalAlw) {
            int[] values;
            int[] values2;
            if (!onlyDiff) {
                return false;
            }
            int smtAlw = 0;
            int index = this.mReasonList.indexOfKey("I");
            if (index >= 0 && (values2 = this.mReasonList.valueAt(index)) != null && values2.length > 0) {
                smtAlw = values2[0];
            }
            int usrAlw = 0;
            int index2 = this.mReasonList.indexOfKey("U");
            if (index2 >= 0 && (values = this.mReasonList.valueAt(index2)) != null && values.length > 1) {
                usrAlw = values[0] + values[1];
            }
            if (totalAlw == smtAlw + usrAlw) {
                return true;
            }
            return false;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean isNeedReport(int threshold, boolean onlyDiff) {
            int[] values;
            int[] values2;
            int[] values3;
            int totalAlw = 0;
            int nonIawareAlw = 0;
            int index = this.mReasonList.indexOfKey("T");
            if (index >= 0 && (values3 = this.mReasonList.valueAt(index)) != null && values3.length > 1) {
                totalAlw = values3[0];
                nonIawareAlw = values3[1];
            }
            if (isAllowConsistent(onlyDiff, totalAlw - nonIawareAlw)) {
                return false;
            }
            if (totalAlw >= threshold) {
                return true;
            }
            if (totalAlw == 0) {
                int index2 = this.mReasonList.indexOfKey("I");
                if (index2 >= 0 && (values2 = this.mReasonList.valueAt(index2)) != null && values2.length > 0 && values2[values2.length - 1] > 0) {
                    return true;
                }
                int index3 = this.mReasonList.indexOfKey("U");
                if (index3 >= 0 && (values = this.mReasonList.valueAt(index3)) != null && values.length > 0 && values[values.length - 1] > 0) {
                    return true;
                }
            }
            return false;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private String encodeString() {
            String[] tagOrderList = {"T", "U", "I", "H", "O", AppMngConstant.AppStartSource.THIRD_ACTIVITY.getDesc(), AppMngConstant.AppStartSource.THIRD_BROADCAST.getDesc(), AppMngConstant.AppStartSource.SYSTEM_BROADCAST.getDesc(), AppMngConstant.AppStartSource.START_SERVICE.getDesc(), AppMngConstant.AppStartSource.BIND_SERVICE.getDesc(), AppMngConstant.AppStartSource.PROVIDER.getDesc(), AppMngConstant.AppStartSource.SCHEDULE_RESTART.getDesc(), AppMngConstant.AppStartSource.JOB_SCHEDULE.getDesc(), AppMngConstant.AppStartSource.ALARM.getDesc(), AppMngConstant.AppStartSource.ACCOUNT_SYNC.getDesc(), AppMngConstant.AppStartReason.DEFAULT.getDesc(), AppMngConstant.AppStartReason.SYSTEM_APP.getDesc(), AppMngConstant.AppStartReason.LIST.getDesc()};
            StringBuilder result = new StringBuilder();
            boolean firstInsert = true;
            for (String tag : tagOrderList) {
                int size = this.mReasonList.size();
                for (int i = 0; i < size; i++) {
                    String key = this.mReasonList.keyAt(i);
                    if (key.startsWith(tag)) {
                        String stat = getStatString(this.mReasonList.valueAt(i));
                        if (!TextUtils.isEmpty(stat)) {
                            if (!firstInsert) {
                                result.append(',');
                            }
                            result.append(key);
                            result.append('=');
                            result.append(stat);
                            firstInsert = false;
                        }
                    }
                }
            }
            if (this.mIsTargetApp) {
                result.append(",");
                result.append("Z");
                result.append("=");
                result.append("1");
            }
            return result.toString();
        }

        private String getStatString(int[] values) {
            StringBuilder result = new StringBuilder();
            boolean hasNum = false;
            int size = values.length;
            for (int i = 0; i < size; i++) {
                if (values[i] > 0) {
                    result.append(values[i]);
                    hasNum = true;
                }
                if (i < size - 1) {
                    result.append('#');
                }
            }
            if (!hasNum) {
                result.delete(0, result.length());
            }
            return result.toString();
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
        JSONException e;
        String str;
        String pkgPrefix;
        String pkgPrefix2 = "com.";
        String str2 = "com.";
        int prefixStart = str2.length() - 1;
        int threshold = getBigdataThreshold(forBeta);
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("feature", "appstart");
            jsonObj.put("start", this.mStartTime);
            long currentTime = System.currentTimeMillis();
            if (clear) {
                try {
                    this.mStartTime = currentTime;
                } catch (JSONException e2) {
                    e = e2;
                }
            }
            jsonObj.put("end", currentTime);
            int index = 0;
            JSONObject dataJson = new JSONObject();
            for (Map.Entry<String, StartupData> item : this.mStartupDataList.entrySet()) {
                String pkg = item.getKey();
                StartupData startupData = item.getValue();
                try {
                    if (startupData.isNeedReport(threshold, onlyDiff)) {
                        if (pkg.startsWith(str2)) {
                            pkgPrefix = pkgPrefix2;
                            try {
                                str = str2;
                                dataJson.put(pkg.substring(prefixStart), startupData.encodeString());
                            } catch (JSONException e3) {
                                e = e3;
                                AwareLog.e(TAG, "makeStartupJson catch JSONException e: " + e);
                                return jsonObj;
                            }
                        } else {
                            pkgPrefix = pkgPrefix2;
                            str = str2;
                            dataJson.put(pkg, startupData.encodeString());
                        }
                        index++;
                    } else {
                        pkgPrefix = pkgPrefix2;
                        str = str2;
                    }
                    pkgPrefix2 = pkgPrefix;
                    str2 = str;
                } catch (JSONException e4) {
                    e = e4;
                    AwareLog.e(TAG, "makeStartupJson catch JSONException e: " + e);
                    return jsonObj;
                }
            }
            dataJson.put("inf", "V" + DecisionMaker.getInstance().getVersion() + CpuCustBaseConfig.CPUCONFIG_INVALID_STR + index + CpuCustBaseConfig.CPUCONFIG_INVALID_STR + this.mStartupDataList.size());
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(dataJson);
            jsonObj.put("data", jsonArray);
        } catch (JSONException e5) {
            e = e5;
            AwareLog.e(TAG, "makeStartupJson catch JSONException e: " + e);
            return jsonObj;
        }
        return jsonObj;
    }

    public void updateStartupData(String pkg, String[] keys, int[]... values) {
        if (!TextUtils.isEmpty(pkg) && keys != null && values != null && keys.length == values.length) {
            synchronized (this.mStartupDataList) {
                StartupData startupData = this.mStartupDataList.get(pkg);
                if (startupData != null) {
                    startupData.increase(keys, values);
                } else {
                    StartupData startupData2 = new StartupData(pkg);
                    startupData2.increase(keys, values);
                    this.mStartupDataList.put(pkg, startupData2);
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

    public static SrmsDumpRadar getInstance() {
        if (sSrmsDumpRadar == null) {
            synchronized (LOCK) {
                if (sSrmsDumpRadar == null) {
                    sSrmsDumpRadar = new SrmsDumpRadar();
                }
            }
        }
        return sSrmsDumpRadar;
    }

    private SrmsDumpRadar() {
        for (int i = 0; i < 4; i++) {
            this.mBigDataList.add(0);
        }
        for (int i2 = 0; i2 <= 1; i2++) {
            this.mStatisticsData.add(new StatisticsData(RESOURCE_FEATURE_ID, 2, SUB_TYPE_LIST[i2], 0, 0, 0, System.currentTimeMillis(), 0));
        }
    }

    public ArrayList<StatisticsData> getStatisticsData() {
        ArrayList<StatisticsData> dataList = new ArrayList<>();
        synchronized (this.mStatisticsData) {
            for (int i = 0; i <= 1; i++) {
                dataList.add(new StatisticsData(RESOURCE_FEATURE_ID, 2, SUB_TYPE_LIST[i], this.mStatisticsData.get(i).getOccurCount(), 0, 0, this.mStatisticsData.get(i).getStartTime(), System.currentTimeMillis()));
            }
            resetStatisticsData();
        }
        AwareLog.d(TAG, "SRMS getStatisticsData success");
        return dataList;
    }

    public void updateStatisticsData(int subTypeCode) {
        if (subTypeCode >= 0 && subTypeCode <= 1) {
            synchronized (this.mStatisticsData) {
                StatisticsData data = this.mStatisticsData.get(subTypeCode);
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
                this.mStatisticsData.get(i).setSubType(SUB_TYPE_LIST[i]);
                this.mStatisticsData.get(i).setOccurCount(0);
                this.mStatisticsData.get(i).setStartTime(System.currentTimeMillis());
                this.mStatisticsData.get(i).setEndTime(0);
            }
        }
    }

    public String saveSrmsBigData(boolean clear) {
        StringBuilder data;
        synchronized (this.mBigDataList) {
            JSONObject jsonObj = makeSrmsJson();
            data = new StringBuilder("[iAwareSRMSStatis_Start]\n");
            data.append(jsonObj.toString());
            data.append("\n[iAwareSRMSStatis_End]");
            if (clear) {
                resetBigData();
            }
        }
        AwareLog.d(TAG, "SRMS saveSrmsBigData success:" + ((Object) data));
        return data.toString();
    }

    private void updateBigData(int interval) {
        synchronized (this.mBigDataList) {
            if (interval < this.mBigDataList.size()) {
                this.mBigDataList.set(interval, Integer.valueOf(this.mBigDataList.get(interval).intValue() + 1));
            }
        }
    }

    private void resetBigData() {
        for (int i = 0; i < 4; i++) {
            this.mBigDataList.set(i, 0);
        }
    }

    private JSONObject makeSrmsJson() {
        int countElapsedTimeLess20 = this.mBigDataList.get(0).intValue();
        int countElapsedTimeLess60 = this.mBigDataList.get(1).intValue();
        int countElapsedTimeLess100 = this.mBigDataList.get(2).intValue();
        int countElapsedTimeMore100 = this.mBigDataList.get(3).intValue();
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
                sb.append("\n[iAwareFake_Start]\nstartTime: ");
                sb.append(String.valueOf(this.mFakeStartTime));
                for (Map.Entry<String, TrackFakeData> dataEntry : this.mFakeDataList.entrySet()) {
                    TrackFakeData data = dataEntry.getValue();
                    if (data != null) {
                        String dataStr = data.toJsonStr();
                        if (dataStr != null && dataStr.length() > 0) {
                            sb.append("\n");
                            sb.append(dataStr.replace("\\", ""));
                        }
                    }
                }
                sb.append("\nendTime: ");
                sb.append(String.valueOf(System.currentTimeMillis()));
                sb.append("\n[iAwareFake_End]");
                if (clear) {
                    this.mFakeDataList.clear();
                }
            }
        }
    }

    public void updateFakeData(String cmp, String status) {
        if (isBetaUser() && cmp != null && status != null) {
            synchronized (this.mFakeDataList) {
                TrackFakeData fakeData = this.mFakeDataList.get(cmp);
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

    /* access modifiers changed from: private */
    public static class TrackFakeData {
        String cmp;
        private ArrayMap<String, Integer> mStatusList = new ArrayMap<>();

        public TrackFakeData(String cmp2) {
            this.cmp = cmp2;
        }

        public void updateStatus(String status) {
            Integer count = this.mStatusList.get(status);
            if (count == null) {
                this.mStatusList.put(status, 1);
            } else {
                this.mStatusList.put(status, Integer.valueOf(count.intValue() + 1));
            }
        }

        public String toJsonStr() {
            JSONObject jsonObj = new JSONObject();
            try {
                jsonObj.put("cmp", this.cmp);
                Iterator<Map.Entry<String, Integer>> it = this.mStatusList.entrySet().iterator();
                while (it.hasNext()) {
                    procEntry(jsonObj, it);
                }
            } catch (JSONException e) {
                AwareLog.e(SrmsDumpRadar.TAG, "TrackFakeData.toJsonStr catch JSONException.");
            }
            return jsonObj.toString();
        }

        private void procEntry(JSONObject jsonObj, Iterator<Map.Entry<String, Integer>> it) throws JSONException {
            Map.Entry entryObj = it.next();
            if (entryObj instanceof Map.Entry) {
                Map.Entry entry = entryObj;
                Object statusObj = entry.getKey();
                if (statusObj instanceof String) {
                    String status = (String) statusObj;
                    Object countObj = entry.getValue();
                    if (countObj instanceof Integer) {
                        jsonObj.put(status, ((Integer) countObj).intValue());
                    }
                }
            }
        }
    }
}
