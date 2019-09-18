package com.huawei.opcollect.strategy;

import android.os.SystemClock;
import com.huawei.opcollect.strategy.OdmfActionManager;
import com.huawei.opcollect.utils.OPCollectUtils;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class EventTimerTrigger implements ITimerTrigger {
    private static final String TAG = "EventTimerTrigger";
    private long mDurationTime;
    private long mEndRealTime = -1;
    private String mEvent;
    private long mInterval;
    private long mNextRealTime = -1;

    public EventTimerTrigger(String event, int durationtime, int interval) {
        this.mEvent = event;
        this.mDurationTime = (long) durationtime;
        this.mInterval = (long) interval;
    }

    public static List<EventTimerTrigger> fromJson(JSONArray json_obj) throws JSONException {
        if (json_obj == null || json_obj.length() <= 0) {
            return null;
        }
        List<EventTimerTrigger> list_trigger = new ArrayList<>();
        int length = json_obj.length();
        for (int i = 0; i < length; i++) {
            JSONObject item = json_obj.optJSONObject(i);
            if (item != null) {
                list_trigger.add(new EventTimerTrigger(item.getString("eventname"), item.getInt("durationtime"), item.getInt("interval")));
            }
        }
        return list_trigger;
    }

    public boolean startTimer(String eventname) {
        if (!this.mEvent.equals(eventname)) {
            return false;
        }
        long nowRealTime = SystemClock.elapsedRealtime() / 1000;
        if (this.mNextRealTime < 0) {
            this.mNextRealTime = nowRealTime;
        }
        this.mEndRealTime = this.mDurationTime + nowRealTime;
        return true;
    }

    public boolean checkTrigger(Calendar calNow, long secondOfDay, long rtNow, OdmfActionManager.NextTimer nxttimer) {
        if (this.mNextRealTime < 0) {
            return false;
        }
        if (rtNow < this.mNextRealTime) {
            nxttimer.update((this.mNextRealTime - rtNow) * 1000);
            return false;
        }
        this.mNextRealTime = this.mInterval + rtNow;
        if (this.mNextRealTime > this.mEndRealTime) {
            this.mNextRealTime = -1;
            this.mEndRealTime = -1;
        } else {
            nxttimer.update((this.mNextRealTime - rtNow) * 1000);
        }
        return true;
    }

    public String toString(String prefix) {
        String prefix2 = OPCollectUtils.DUMP_PRINT_PREFIX + prefix;
        StringBuilder sb = new StringBuilder(prefix).append("<-EventTimerTrigger->");
        sb.append("\n").append(prefix2).append("mEvent: ").append(this.mEvent);
        sb.append("\n").append(prefix2).append("mDurationTime: ").append(this.mDurationTime);
        sb.append("\n").append(prefix2).append("mInterval: ").append(this.mInterval);
        sb.append("\n").append(prefix2).append("mNextRealTime: ").append(this.mNextRealTime);
        sb.append("\n").append(prefix2).append("mEndRealTime: ").append(this.mEndRealTime);
        return sb.toString();
    }
}
