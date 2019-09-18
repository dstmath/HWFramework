package com.huawei.opcollect.collector.receivercollection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.huawei.opcollect.odmf.OdmfCollectScheduler;
import com.huawei.opcollect.strategy.Action;
import com.huawei.opcollect.utils.OPCollectLog;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class TimeZoneAction extends Action {
    private static final String TAG = "TimeZoneAction";
    private static TimeZoneAction sInstance = null;
    private TimeZoneBroadcastReceiver mReceiver = null;

    class TimeZoneBroadcastReceiver extends BroadcastReceiver {
        TimeZoneBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                OPCollectLog.r("TimeZoneAction", "onReceive: " + action);
                if ("android.intent.action.TIMEZONE_CHANGED".equalsIgnoreCase(action)) {
                    TimeZoneAction.this.perform();
                }
            }
        }
    }

    public static synchronized TimeZoneAction getInstance(Context context) {
        TimeZoneAction timeZoneAction;
        synchronized (TimeZoneAction.class) {
            if (sInstance == null) {
                sInstance = new TimeZoneAction("TimeZoneAction", context);
            }
            timeZoneAction = sInstance;
        }
        return timeZoneAction;
    }

    private TimeZoneAction(String name, Context context) {
        super(context, name);
        setDailyRecordNum(SysEventUtil.querySysEventDailyCount(SysEventUtil.EVENT_TIMEZONE_CHANGE));
        OPCollectLog.r("TimeZoneAction", "TimeZoneAction");
    }

    public void enable() {
        super.enable();
        if (this.mReceiver == null && this.mContext != null) {
            this.mReceiver = new TimeZoneBroadcastReceiver();
            this.mContext.registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.TIMEZONE_CHANGED"), null, OdmfCollectScheduler.getInstance().getCtrlHandler());
            OPCollectLog.r("TimeZoneAction", "enabled");
        }
    }

    public void disable() {
        super.disable();
        if (this.mReceiver != null && this.mContext != null) {
            this.mContext.unregisterReceiver(this.mReceiver);
            this.mReceiver = null;
        }
    }

    public boolean destroy() {
        super.destroy();
        destroyInstance();
        return true;
    }

    private static synchronized void destroyInstance() {
        synchronized (TimeZoneAction.class) {
            sInstance = null;
        }
    }

    /* access modifiers changed from: protected */
    public boolean execute() {
        super.execute();
        SysEventUtil.collectSysEventData(SysEventUtil.EVENT_TIMEZONE_CHANGE, new SimpleDateFormat("z", Locale.getDefault()).format(Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.getDefault()).getTime()));
        return true;
    }

    public void dump(int indentNum, PrintWriter pw) {
        super.dump(indentNum, pw);
        if (pw != null) {
            String indent = String.format("%" + indentNum + "s\\-", new Object[]{" "});
            if (this.mReceiver == null) {
                pw.println(indent + "receiver is null");
            } else {
                pw.println(indent + "receiver not null");
            }
        }
    }
}
