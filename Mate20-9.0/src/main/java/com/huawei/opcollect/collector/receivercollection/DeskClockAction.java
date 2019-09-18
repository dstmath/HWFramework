package com.huawei.opcollect.collector.receivercollection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.huawei.opcollect.odmf.OdmfCollectScheduler;
import com.huawei.opcollect.strategy.Action;
import com.huawei.opcollect.utils.OPCollectLog;
import java.io.PrintWriter;

public class DeskClockAction extends Action {
    private static final String BROADCAST_PERMISSION = "com.huawei.deskclock.broadcast.permission";
    private static final String CLOCK_ACTION_STRING = "huawei.deskclock.ALARM_ALERT_CONFLICT";
    private static final String TAG = "DeskClockAction";
    private static DeskClockAction sInstance = null;
    private DeskClockReceiver mReceiver = null;

    class DeskClockReceiver extends BroadcastReceiver {
        DeskClockReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                OPCollectLog.r("DeskClockAction", "onReceive action: " + action);
                if (DeskClockAction.CLOCK_ACTION_STRING.equalsIgnoreCase(action)) {
                    DeskClockAction.this.perform();
                }
            }
        }
    }

    public static synchronized DeskClockAction getInstance(Context context) {
        DeskClockAction deskClockAction;
        synchronized (DeskClockAction.class) {
            if (sInstance == null) {
                sInstance = new DeskClockAction("DeskClockAction", context);
            }
            deskClockAction = sInstance;
        }
        return deskClockAction;
    }

    private DeskClockAction(String name, Context context) {
        super(context, name);
        setDailyRecordNum(SysEventUtil.querySysEventDailyCount(SysEventUtil.EVENT_DESKCLOCK_ALARM));
    }

    public void enable() {
        super.enable();
        if (this.mReceiver == null && this.mContext != null) {
            this.mReceiver = new DeskClockReceiver();
            this.mContext.registerReceiver(this.mReceiver, new IntentFilter(CLOCK_ACTION_STRING), BROADCAST_PERMISSION, OdmfCollectScheduler.getInstance().getCtrlHandler());
            OPCollectLog.r("DeskClockAction", "enabled");
        }
    }

    /* access modifiers changed from: protected */
    public boolean execute() {
        SysEventUtil.collectSysEventData(SysEventUtil.EVENT_DESKCLOCK_ALARM);
        return true;
    }

    public boolean perform() {
        return super.perform();
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
        synchronized (DeskClockAction.class) {
            sInstance = null;
        }
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
