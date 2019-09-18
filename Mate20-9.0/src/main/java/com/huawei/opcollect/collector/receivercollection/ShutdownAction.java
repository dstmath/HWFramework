package com.huawei.opcollect.collector.receivercollection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.opcollect.odmf.OdmfCollectScheduler;
import com.huawei.opcollect.strategy.Action;
import com.huawei.opcollect.utils.OPCollectLog;
import java.io.PrintWriter;

public class ShutdownAction extends Action {
    private static final String SHUTDOWN_ACTION_PROPERTY = "sys.shutdown.requested";
    private static final String TAG = "ShutdownAction";
    private static ShutdownAction sInstance = null;
    private ShutdownBroadcastReceiver mReceiver = null;

    class ShutdownBroadcastReceiver extends BroadcastReceiver {
        ShutdownBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                OPCollectLog.r("ShutdownAction", "onReceiver action: " + action);
                if ("android.intent.action.ACTION_SHUTDOWN".equalsIgnoreCase(action)) {
                    ShutdownAction.this.perform();
                }
            }
        }
    }

    public static synchronized ShutdownAction getInstance(Context context) {
        ShutdownAction shutdownAction;
        synchronized (ShutdownAction.class) {
            if (sInstance == null) {
                sInstance = new ShutdownAction(context, "ShutdownAction");
            }
            shutdownAction = sInstance;
        }
        return shutdownAction;
    }

    private ShutdownAction(Context context, String name) {
        super(context, name);
        setDailyRecordNum(SysEventUtil.querySysEventDailyCount(SysEventUtil.EVENT_SHUTDOWN_PHONE));
        OPCollectLog.r("ShutdownAction", "ShutdownAction");
    }

    public void enable() {
        super.enable();
        if (this.mReceiver == null && this.mContext != null) {
            this.mReceiver = new ShutdownBroadcastReceiver();
            this.mContext.registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.ACTION_SHUTDOWN"), "com.huawei.permission.OP_COLLECT", OdmfCollectScheduler.getInstance().getCtrlHandler());
            OPCollectLog.r("ShutdownAction", "enabled");
        }
    }

    /* access modifiers changed from: protected */
    public boolean execute() {
        SysEventUtil.collectSysEventData(SysEventUtil.EVENT_SHUTDOWN_PHONE, SystemPropertiesEx.get(SHUTDOWN_ACTION_PROPERTY, ""));
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
        synchronized (ShutdownAction.class) {
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
