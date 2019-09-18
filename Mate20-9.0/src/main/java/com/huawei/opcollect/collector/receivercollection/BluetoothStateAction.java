package com.huawei.opcollect.collector.receivercollection;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import com.huawei.opcollect.odmf.OdmfCollectScheduler;
import com.huawei.opcollect.strategy.Action;
import com.huawei.opcollect.utils.OPCollectLog;
import java.io.PrintWriter;

public class BluetoothStateAction extends Action {
    private static final String BLE_ACTION = "android.bluetooth.adapter.action.STATE_CHANGED";
    private static final Object LOCK = new Object();
    private static final String TAG = "BluetoothStateAction";
    private static BluetoothStateAction sInstance = null;
    /* access modifiers changed from: private */
    public String mEventType = null;
    private BluetoothStateBroadcastReceiver mReceiver = null;

    class BluetoothStateBroadcastReceiver extends BroadcastReceiver {
        BluetoothStateBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                OPCollectLog.r("BluetoothStateAction", "onReceive action: " + action);
                if (BluetoothStateAction.BLE_ACTION.equals(action)) {
                    int state = intent.getIntExtra("android.bluetooth.adapter.extra.STATE", -1);
                    if (state == 12) {
                        String unused = BluetoothStateAction.this.mEventType = SysEventUtil.EVENT_BLUETOOTH_ON;
                        BluetoothStateAction.this.perform();
                    } else if (state == 10) {
                        String unused2 = BluetoothStateAction.this.mEventType = SysEventUtil.EVENT_BLUETOOTH_OFF;
                        BluetoothStateAction.this.perform();
                    }
                }
            }
        }
    }

    public static BluetoothStateAction getInstance(Context context) {
        BluetoothStateAction bluetoothStateAction;
        synchronized (LOCK) {
            if (sInstance == null) {
                sInstance = new BluetoothStateAction(context, "BluetoothStateAction");
            }
            bluetoothStateAction = sInstance;
        }
        return bluetoothStateAction;
    }

    private BluetoothStateAction(Context context, String name) {
        super(context, name);
        setDailyRecordNum(SysEventUtil.querySysEventDailyCount(SysEventUtil.EVENT_BLUETOOTH_ON) + SysEventUtil.querySysEventDailyCount(SysEventUtil.EVENT_BLUETOOTH_OFF));
    }

    public void enable() {
        super.enable();
        if (this.mReceiver == null && this.mContext != null) {
            this.mReceiver = new BluetoothStateBroadcastReceiver();
            this.mContext.registerReceiver(this.mReceiver, new IntentFilter(BLE_ACTION), "android.permission.BLUETOOTH", OdmfCollectScheduler.getInstance().getCtrlHandler());
            BluetoothAdapter blueadapter = BluetoothAdapter.getDefaultAdapter();
            String value = SysEventUtil.OFF;
            if (blueadapter != null && blueadapter.getState() == 12) {
                value = SysEventUtil.ON;
            }
            SysEventUtil.collectKVSysEventData("device_connection/bluetooth_status", SysEventUtil.BLUETOOTH_STATUS, value);
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

    private static void destroyInstance() {
        synchronized (LOCK) {
            sInstance = null;
        }
    }

    /* access modifiers changed from: protected */
    public boolean execute() {
        super.execute();
        if (TextUtils.isEmpty(this.mEventType)) {
            return false;
        }
        SysEventUtil.collectSysEventData(this.mEventType);
        SysEventUtil.collectKVSysEventData("device_connection/bluetooth_status", SysEventUtil.BLUETOOTH_STATUS, SysEventUtil.EVENT_BLUETOOTH_ON.equals(this.mEventType) ? SysEventUtil.ON : SysEventUtil.OFF);
        this.mEventType = null;
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
