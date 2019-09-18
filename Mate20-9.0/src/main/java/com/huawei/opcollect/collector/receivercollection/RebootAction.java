package com.huawei.opcollect.collector.receivercollection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.huawei.opcollect.odmf.OdmfCollectScheduler;
import com.huawei.opcollect.strategy.Action;
import com.huawei.opcollect.utils.OPCollectConstant;
import java.io.PrintWriter;

public class RebootAction extends Action {
    private static RebootAction sInstance = null;
    private BootBroadcastReceiver mReceiver = null;

    static class BootBroadcastReceiver extends BroadcastReceiver {
        BootBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null && "android.intent.action.REBOOT".equalsIgnoreCase(intent.getAction())) {
                SysEventUtil.collectSysEventData(SysEventUtil.EVENT_REBOOT);
            }
        }
    }

    public static synchronized RebootAction getInstance(Context context) {
        RebootAction rebootAction;
        synchronized (RebootAction.class) {
            if (sInstance == null) {
                sInstance = new RebootAction(context, OPCollectConstant.REBOOT_ACTION_NAME);
            }
            rebootAction = sInstance;
        }
        return rebootAction;
    }

    private RebootAction(Context context, String name) {
        super(context, name);
        setDailyRecordNum(SysEventUtil.querySysEventDailyCount(SysEventUtil.EVENT_REBOOT));
    }

    public void enable() {
        super.enable();
        if (this.mReceiver == null) {
            this.mReceiver = new BootBroadcastReceiver();
            this.mContext.registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.REBOOT"), "com.huawei.permission.OP_COLLECT", OdmfCollectScheduler.getInstance().getCtrlHandler());
        }
    }

    public void disable() {
        super.disable();
        if (this.mReceiver != null) {
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
        synchronized (RebootAction.class) {
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
