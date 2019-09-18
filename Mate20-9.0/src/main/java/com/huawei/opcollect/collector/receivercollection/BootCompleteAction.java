package com.huawei.opcollect.collector.receivercollection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.huawei.opcollect.odmf.OdmfCollectScheduler;
import com.huawei.opcollect.strategy.Action;
import com.huawei.opcollect.utils.OPCollectLog;
import java.io.PrintWriter;

public class BootCompleteAction extends Action {
    private static final String TAG = "BootCompleteAction";
    private static BootCompleteAction sInstance = null;
    private BootBroadcastReceiver mReceiver = null;

    class BootBroadcastReceiver extends BroadcastReceiver {
        BootBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                OPCollectLog.r("BootCompleteAction", "action: " + action);
                if ("android.intent.action.BOOT_COMPLETED".equalsIgnoreCase(action)) {
                    BootCompleteAction.this.perform();
                }
            }
        }
    }

    public static synchronized BootCompleteAction getInstance(Context context) {
        BootCompleteAction bootCompleteAction;
        synchronized (BootCompleteAction.class) {
            if (sInstance == null) {
                sInstance = new BootCompleteAction(context, "BootCompleteAction");
            }
            bootCompleteAction = sInstance;
        }
        return bootCompleteAction;
    }

    private BootCompleteAction(Context context, String name) {
        super(context, name);
        OPCollectLog.r("BootCompleteAction", "BootCompleteAction");
        setDailyRecordNum(SysEventUtil.querySysEventDailyCount(SysEventUtil.EVENT_BOOT_COMPLETED));
    }

    public void enable() {
        super.enable();
        if (this.mReceiver == null && this.mContext != null) {
            this.mReceiver = new BootBroadcastReceiver();
            this.mContext.registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.BOOT_COMPLETED"), null, OdmfCollectScheduler.getInstance().getCtrlHandler());
            OPCollectLog.r("BootCompleteAction", "enabled");
        }
    }

    /* access modifiers changed from: protected */
    public boolean execute() {
        SysEventUtil.collectSysEventData(SysEventUtil.EVENT_BOOT_COMPLETED);
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
        synchronized (BootCompleteAction.class) {
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
