package com.huawei.opcollect.collector.receivercollection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.huawei.opcollect.odmf.OdmfCollectScheduler;
import com.huawei.opcollect.strategy.AbsActionParam;
import com.huawei.opcollect.strategy.Action;
import com.huawei.opcollect.utils.OPCollectLog;
import java.io.PrintWriter;

public class BatteryAction extends Action {
    private static final Object LOCK = new Object();
    private static final String TAG = "BatteryAction";
    private static BatteryAction sInstance = null;
    private BatteryChangeReceiver mReceiver = null;

    private class BatteryActionParam extends AbsActionParam {
        private String battery;

        BatteryActionParam(String battery2) {
            this.battery = battery2;
        }

        /* access modifiers changed from: package-private */
        public String getBattery() {
            return this.battery;
        }
    }

    class BatteryChangeReceiver extends BroadcastReceiver {
        BatteryChangeReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                OPCollectLog.r(BatteryAction.TAG, "onReceive action: " + action);
                if ("android.intent.action.BATTERY_CHANGED".equalsIgnoreCase(action)) {
                    int level = intent.getIntExtra("level", -1);
                    int scale = intent.getIntExtra("scale", -1);
                    if (scale != 0) {
                        boolean unused = BatteryAction.this.performWithArgs(new BatteryActionParam(String.valueOf((int) ((((float) level) / ((float) scale)) * 100.0f))));
                    }
                }
            }
        }
    }

    public static BatteryAction getInstance(Context context) {
        BatteryAction batteryAction;
        synchronized (LOCK) {
            if (sInstance == null) {
                sInstance = new BatteryAction(SysEventUtil.BATTERY_LEFT, context);
            }
            batteryAction = sInstance;
        }
        return batteryAction;
    }

    private BatteryAction(String name, Context context) {
        super(context, name);
        setDailyRecordNum(SysEventUtil.querySysEventDailyCount(SysEventUtil.BATTERY_LEFT));
    }

    public void enable() {
        super.enable();
        if (this.mReceiver == null && this.mContext != null) {
            this.mReceiver = new BatteryChangeReceiver();
            this.mContext.registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.BATTERY_CHANGED"), null, OdmfCollectScheduler.getInstance().getCtrlHandler());
            OPCollectLog.r(TAG, "enabled");
        }
    }

    /* access modifiers changed from: protected */
    public boolean executeWithArgs(AbsActionParam absActionParam) {
        if (absActionParam != null) {
            SysEventUtil.collectSysEventData(SysEventUtil.BATTERY_LEFT, ((BatteryActionParam) absActionParam).getBattery());
            SysEventUtil.collectKVSysEventData("battery/battery_left", SysEventUtil.BATTERY_LEFT, ((BatteryActionParam) absActionParam).getBattery());
        }
        return true;
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

    private static void destroyInstance() {
        synchronized (LOCK) {
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
