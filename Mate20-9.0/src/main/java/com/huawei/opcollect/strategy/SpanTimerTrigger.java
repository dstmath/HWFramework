package com.huawei.opcollect.strategy;

import com.huawei.opcollect.strategy.OdmfActionManager;
import com.huawei.opcollect.utils.OPCollectLog;
import com.huawei.opcollect.utils.OPCollectUtils;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SpanTimerTrigger implements ITimerTrigger {
    private static final String TAG = "SpanTimerTrigger";
    private long mEndTime;
    private int mInterval;
    private long mNextRealTime;
    private long mStartTime;

    public SpanTimerTrigger(int begin_h, int begin_m, int begin_s, int end_h, int end_m, int end_s, int interval) {
        long start_second = (((((long) begin_h) * 60) + ((long) begin_m)) * 60) + ((long) begin_s);
        long end_second = (((((long) end_h) * 60) + ((long) end_m)) * 60) + ((long) end_s);
        end_second = end_second < start_second ? end_second + OPCollectUtils.ONEDAYINSECOND : end_second;
        this.mStartTime = start_second;
        this.mEndTime = end_second - start_second;
        this.mInterval = interval;
        this.mNextRealTime = 0;
    }

    public static List<ITimerTrigger> fromJson(JSONArray json_obj) throws JSONException {
        if (json_obj == null || json_obj.length() <= 0) {
            return null;
        }
        List<ITimerTrigger> list_trigger = new ArrayList<>();
        int length = json_obj.length();
        for (int i = 0; i < length; i++) {
            JSONObject item = json_obj.optJSONObject(i);
            if (item != null) {
                String s_time = item.getString("starttime");
                if (s_time != null) {
                    String[] s_hms = s_time.split(":");
                    if (s_hms.length == 3) {
                        String e_time = item.getString("endtime");
                        if (e_time != null) {
                            String[] e_hms = e_time.split(":");
                            if (e_hms.length == 3) {
                                list_trigger.add(new SpanTimerTrigger(Integer.parseInt(s_hms[0]), Integer.parseInt(s_hms[1]), Integer.parseInt(s_hms[2]), Integer.parseInt(e_hms[0]), Integer.parseInt(e_hms[1]), Integer.parseInt(e_hms[2]), item.getInt("interval")));
                            }
                        }
                    }
                }
            }
        }
        return list_trigger;
    }

    public boolean checkTrigger(Calendar calNow, long secondOfDay, long rtNow, OdmfActionManager.NextTimer nxttimer) {
        long timeinsecond = ((OPCollectUtils.ONEDAYINSECOND + secondOfDay) - this.mStartTime) % OPCollectUtils.ONEDAYINSECOND;
        if (timeinsecond > this.mEndTime) {
            this.mNextRealTime = 0;
            nxttimer.update((OPCollectUtils.ONEDAYINSECOND - timeinsecond) * 1000);
            return false;
        }
        if (this.mNextRealTime == 0) {
            this.mNextRealTime = rtNow;
        }
        if (this.mNextRealTime > rtNow) {
            nxttimer.update((this.mNextRealTime - rtNow) * 1000);
            return false;
        }
        this.mNextRealTime = ((long) this.mInterval) + rtNow;
        nxttimer.update(((long) this.mInterval) * 1000);
        OPCollectLog.r(TAG, "mInterval:" + this.mInterval + " mNextRealTime:" + this.mNextRealTime);
        return true;
    }

    public long getStartTime() {
        return this.mStartTime;
    }

    public String toString(String prefix) {
        String prefix2 = OPCollectUtils.DUMP_PRINT_PREFIX + prefix;
        StringBuilder sb = new StringBuilder(prefix).append("<-SpanTimerTrigger->");
        sb.append("\n").append(prefix2).append("mStartTime: ").append(OPCollectUtils.formatTimeInSecond(this.mStartTime));
        sb.append("\n").append(prefix2).append("mEndTime: ").append(OPCollectUtils.formatTimeInSecond(this.mEndTime + this.mStartTime));
        sb.append("\n").append(prefix2).append("mInterval: ").append(this.mInterval);
        sb.append("\n").append(prefix2).append("mNextRealTime: ").append(this.mNextRealTime);
        return sb.toString();
    }
}
