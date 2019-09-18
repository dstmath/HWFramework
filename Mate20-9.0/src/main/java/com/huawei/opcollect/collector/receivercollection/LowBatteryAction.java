package com.huawei.opcollect.collector.receivercollection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.huawei.opcollect.odmf.OdmfCollectScheduler;
import com.huawei.opcollect.strategy.Action;
import com.huawei.opcollect.utils.OPCollectLog;
import java.io.PrintWriter;

public class LowBatteryAction extends Action {
    private static final String TAG = "LowBatteryAction";
    private static LowBatteryAction sInstance = null;
    private LowBatteryBroadcastReceiver mReceiver = null;

    class LowBatteryBroadcastReceiver extends BroadcastReceiver {
        LowBatteryBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                OPCollectLog.r("LowBatteryAction", "onReceive action: " + intent.getAction());
                if ("android.intent.action.BATTERY_LOW".equalsIgnoreCase(intent.getAction())) {
                    OPCollectLog.r("LowBatteryAction", "onReceive");
                    LowBatteryAction.this.perform();
                }
            }
        }
    }

    public static synchronized LowBatteryAction getInstance(Context context) {
        LowBatteryAction lowBatteryAction;
        synchronized (LowBatteryAction.class) {
            if (sInstance == null) {
                sInstance = new LowBatteryAction(context, "LowBatteryAction");
            }
            lowBatteryAction = sInstance;
        }
        return lowBatteryAction;
    }

    private LowBatteryAction(Context context, String name) {
        super(context, name);
        OPCollectLog.r("LowBatteryAction", "LowBatteryAction");
        setDailyRecordNum(SysEventUtil.querySysEventDailyCount(SysEventUtil.EVENT_LOW_POWER));
    }

    public void enable() {
        super.enable();
        if (this.mReceiver == null && this.mContext != null) {
            this.mReceiver = new LowBatteryBroadcastReceiver();
            this.mContext.registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.BATTERY_LOW"), "com.huawei.permission.OP_COLLECT", OdmfCollectScheduler.getInstance().getCtrlHandler());
            OPCollectLog.r("LowBatteryAction", "enabled");
        }
    }

    /* access modifiers changed from: protected */
    public boolean execute() {
        SysEventUtil.collectSysEventData(SysEventUtil.EVENT_LOW_POWER);
        return true;
    }

    public boolean destroy() {
        super.destroy();
        destroyInstance();
        return true;
    }

    private static synchronized void destroyInstance() {
        synchronized (LowBatteryAction.class) {
            sInstance = null;
        }
    }

    public void disable() {
        super.disable();
        if (this.mReceiver != null && this.mContext != null) {
            this.mContext.unregisterReceiver(this.mReceiver);
            this.mReceiver = null;
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
