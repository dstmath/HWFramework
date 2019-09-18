package com.huawei.opcollect.strategy;

import android.os.SystemClock;
import com.huawei.opcollect.strategy.OdmfActionManager;
import com.huawei.opcollect.utils.OPCollectLog;
import com.huawei.opcollect.utils.OPCollectUtils;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class LoopTimerTrigger implements ITimerTrigger {
    private static final String TAG = "LoopTimerTrigger";
    private long mInterval;
    private long mNextRealTime = (SystemClock.elapsedRealtime() / 1000);

    private LoopTimerTrigger(int interval) {
        this.mInterval = (long) interval;
    }

    static List<ITimerTrigger> fromJson(JSONArray json_obj) throws JSONException {
        if (json_obj == null || json_obj.length() <= 0) {
            return null;
        }
        List<ITimerTrigger> list_trigger = new ArrayList<>();
        int length = json_obj.length();
        for (int i = 0; i < length; i++) {
            JSONObject item = json_obj.optJSONObject(i);
            if (item != null) {
                list_trigger.add(new LoopTimerTrigger(item.getInt("interval")));
            }
        }
        if (list_trigger.size() <= 1) {
            return list_trigger;
        }
        OPCollectLog.e(TAG, "Multi loopTimerTrigger are configured.");
        return null;
    }

    public boolean checkTrigger(Calendar calNow, long secondOfDay, long rtNow, OdmfActionManager.NextTimer nxttimer) {
        if (rtNow < this.mNextRealTime) {
            return false;
        }
        this.mNextRealTime = this.mInterval + rtNow;
        nxttimer.update(this.mInterval * 1000);
        return true;
    }

    public String toString(String prefix) {
        String prefix2 = OPCollectUtils.DUMP_PRINT_PREFIX + prefix;
        StringBuilder sb = new StringBuilder(prefix).append("<-LoopTimerTrigger->");
        sb.append("\n").append(prefix2).append("mInterval: ").append(this.mInterval);
        sb.append("\n").append(prefix2).append("mNextRealTime: ").append(this.mNextRealTime);
        return sb.toString();
    }
}
