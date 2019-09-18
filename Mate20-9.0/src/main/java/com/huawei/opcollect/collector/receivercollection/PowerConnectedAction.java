package com.huawei.opcollect.collector.receivercollection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.huawei.opcollect.odmf.OdmfCollectScheduler;
import com.huawei.opcollect.strategy.Action;
import com.huawei.opcollect.utils.OPCollectConstant;
import com.huawei.opcollect.utils.OPCollectLog;
import java.io.PrintWriter;

public class PowerConnectedAction extends Action {
    private static final Object LOCK = new Object();
    private static final String TAG = "PowerConnectedAction";
    private static PowerConnectedAction sInstance = null;
    private PowerConnectedBroadcastReceiver mReceiver = null;

    class PowerConnectedBroadcastReceiver extends BroadcastReceiver {
        PowerConnectedBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                OPCollectLog.r("PowerConnectedAction", "onReceive action: " + action);
                if ("android.intent.action.ACTION_POWER_CONNECTED".equalsIgnoreCase(action)) {
                    PowerConnectedAction.this.perform();
                }
            }
        }
    }

    public static PowerConnectedAction getInstance(Context context) {
        PowerConnectedAction powerConnectedAction;
        synchronized (LOCK) {
            if (sInstance == null) {
                sInstance = new PowerConnectedAction(context, "PowerConnectedAction");
            }
            powerConnectedAction = sInstance;
        }
        return powerConnectedAction;
    }

    private PowerConnectedAction(Context context, String name) {
        super(context, name);
        setDailyRecordNum(SysEventUtil.querySysEventDailyCount(SysEventUtil.EVENT_POWER_CONNECTED));
        OPCollectLog.r("PowerConnectedAction", OPCollectConstant.PACKAGE_UPDATE_ACTION_NAME);
    }

    public void enable() {
        super.enable();
        if (this.mReceiver == null && this.mContext != null) {
            this.mReceiver = new PowerConnectedBroadcastReceiver();
            this.mContext.registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.ACTION_POWER_CONNECTED"), null, OdmfCollectScheduler.getInstance().getCtrlHandler());
            OPCollectLog.r("PowerConnectedAction", "enabled");
        }
    }

    /* access modifiers changed from: protected */
    public boolean execute() {
        Intent batteryIntent = null;
        if (this.mContext != null) {
            batteryIntent = this.mContext.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
        }
        SysEventUtil.collectKVSysEventData("battery/charging_status", SysEventUtil.CHARGING_STATUS, SysEventUtil.ON);
        if (batteryIntent == null) {
            SysEventUtil.collectSysEventData(SysEventUtil.EVENT_POWER_CONNECTED);
        } else {
            int chargePlug = batteryIntent.getIntExtra("plugged", -1);
            String chargeType = "";
            if (2 == chargePlug) {
                chargeType = "USB";
            } else if (1 == chargePlug) {
                chargeType = "AC";
            } else if (4 == chargePlug) {
                chargeType = "Wireless";
            }
            int level = batteryIntent.getIntExtra("level", -1);
            int scale = batteryIntent.getIntExtra("scale", -1);
            int batteryPct = -1;
            if (scale != 0) {
                batteryPct = (int) ((((float) level) / ((float) scale)) * 100.0f);
            }
            SysEventUtil.collectSysEventData(SysEventUtil.EVENT_POWER_CONNECTED, String.format("{level:%d, chargeType:%s}", new Object[]{Integer.valueOf(batteryPct), chargeType}));
        }
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
