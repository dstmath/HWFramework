package com.huawei.opcollect.strategy;

import com.huawei.opcollect.strategy.OdmfActionManager;
import com.huawei.opcollect.utils.OPCollectUtils;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TimeDisable {
    private long mEndTime;
    private long mStartTime;

    public TimeDisable(int begin_h, int begin_m, int begin_s, int end_h, int end_m, int end_s) {
        long start_second = (((((long) begin_h) * 60) + ((long) begin_m)) * 60) + ((long) begin_s);
        long end_second = (((((long) end_h) * 60) + ((long) end_m)) * 60) + ((long) end_s);
        end_second = end_second < start_second ? end_second + OPCollectUtils.ONEDAYINSECOND : end_second;
        this.mStartTime = start_second;
        this.mEndTime = end_second - start_second;
    }

    public static List<TimeDisable> fromJson(JSONArray json_obj) throws JSONException {
        if (json_obj == null || json_obj.length() <= 0) {
            return null;
        }
        List<TimeDisable> list_trigger = new ArrayList<>();
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
                                list_trigger.add(new TimeDisable(Integer.parseInt(s_hms[0]), Integer.parseInt(s_hms[1]), Integer.parseInt(s_hms[2]), Integer.parseInt(e_hms[0]), Integer.parseInt(e_hms[1]), Integer.parseInt(e_hms[2])));
                            }
                        }
                    }
                }
            }
        }
        return list_trigger;
    }

    public boolean checkDisable(Calendar calNow, long secondOfDay, OdmfActionManager.NextTimer nxttimer) {
        long timeinsecond = ((secondOfDay + OPCollectUtils.ONEDAYINSECOND) - this.mStartTime) % OPCollectUtils.ONEDAYINSECOND;
        if (timeinsecond > this.mEndTime) {
            nxttimer.update((OPCollectUtils.ONEDAYINSECOND - timeinsecond) * 1000);
            return false;
        }
        nxttimer.update((this.mEndTime - timeinsecond) * 1000);
        return true;
    }

    public String toString(String prefix) {
        String prefix2 = OPCollectUtils.DUMP_PRINT_PREFIX + prefix;
        StringBuilder sb = new StringBuilder(prefix).append("<-TimeDisable->");
        sb.append("\n").append(prefix2).append("mStartTime: ").append(OPCollectUtils.formatTimeInSecond(this.mStartTime));
        sb.append("\n").append(prefix2).append("mEndTime: ").append(OPCollectUtils.formatTimeInSecond(this.mEndTime + this.mStartTime));
        return sb.toString();
    }
}
