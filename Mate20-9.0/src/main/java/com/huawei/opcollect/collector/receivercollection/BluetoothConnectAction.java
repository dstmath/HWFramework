package com.huawei.opcollect.collector.receivercollection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.huawei.hiai.awareness.AwarenessConstants;
import com.huawei.opcollect.odmf.OdmfCollectScheduler;
import com.huawei.opcollect.strategy.Action;
import com.huawei.opcollect.utils.OPCollectConstant;
import com.huawei.opcollect.utils.OPCollectLog;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class BluetoothConnectAction extends Action {
    private static final int DEVICE_CONNECTED = 1;
    private static final int DEVICE_DISCONNECTED = 0;
    private static final Object LOCK = new Object();
    private static final String TAG = "BluetoothConnectAction";
    private static BluetoothConnectAction sInstance = null;
    private String mBluetoothInfo = "";
    /* access modifiers changed from: private */
    public int mDeviceState = -1;
    private Map<Integer, String> mDeviceTypeList = null;
    private BluetoothBroadcastReceiver mReceiver = null;

    class BluetoothBroadcastReceiver extends BroadcastReceiver {
        BluetoothBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (action != null) {
                    OPCollectLog.r("BluetoothConnectAction", "onReceive");
                    if ("android.bluetooth.device.action.ACL_CONNECTED".equals(action)) {
                        int unused = BluetoothConnectAction.this.mDeviceState = 1;
                    } else if ("android.bluetooth.device.action.ACL_DISCONNECTED".equals(action)) {
                        int unused2 = BluetoothConnectAction.this.mDeviceState = 0;
                    } else {
                        OPCollectLog.r("BluetoothConnectAction", "Unexpected Action");
                        return;
                    }
                    BluetoothConnectAction.this.saveBluetoothDeviceInfo((BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE"));
                    BluetoothConnectAction.this.perform();
                }
            }
        }
    }

    public static BluetoothConnectAction getInstance(Context context) {
        BluetoothConnectAction bluetoothConnectAction;
        synchronized (LOCK) {
            if (sInstance == null) {
                sInstance = new BluetoothConnectAction(context, "BluetoothConnectAction");
            }
            bluetoothConnectAction = sInstance;
        }
        return bluetoothConnectAction;
    }

    private BluetoothConnectAction(Context context, String name) {
        super(context, name);
        setDailyRecordNum(SysEventUtil.querySysEventDailyCount(SysEventUtil.EVENT_BLUETOOTH_CONNECTED) + SysEventUtil.querySysEventDailyCount(SysEventUtil.EVENT_BLUETOOTH_DISCONNECTED));
    }

    public void enable() {
        super.enable();
        if (this.mReceiver == null && this.mContext != null) {
            this.mReceiver = new BluetoothBroadcastReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.bluetooth.device.action.ACL_CONNECTED");
            intentFilter.addAction("android.bluetooth.device.action.ACL_DISCONNECTED");
            this.mContext.registerReceiver(this.mReceiver, intentFilter, "android.permission.BLUETOOTH", OdmfCollectScheduler.getInstance().getCtrlHandler());
            BluetoothAdapter blueadapter = BluetoothAdapter.getDefaultAdapter();
            String value = SysEventUtil.OFF;
            if (blueadapter != null) {
                int state = blueadapter.getState();
                if (state == 1 || state == 2) {
                    value = SysEventUtil.ON;
                }
            }
            SysEventUtil.collectKVSysEventData("device_connection/bluetooth_connect_status", SysEventUtil.BLUETOOTH_CONNECT_STATUS, value);
        }
        initDeviceTypeMap();
    }

    /* access modifiers changed from: protected */
    public boolean execute() {
        if (1 == this.mDeviceState) {
            SysEventUtil.collectSysEventData(SysEventUtil.EVENT_BLUETOOTH_CONNECTED, this.mBluetoothInfo);
            SysEventUtil.collectKVSysEventData("device_connection/bluetooth_connect_status", SysEventUtil.BLUETOOTH_CONNECT_STATUS, SysEventUtil.ON);
        } else if (this.mDeviceState == 0) {
            SysEventUtil.collectSysEventData(SysEventUtil.EVENT_BLUETOOTH_DISCONNECTED, this.mBluetoothInfo);
            SysEventUtil.collectKVSysEventData("device_connection/bluetooth_connect_status", SysEventUtil.BLUETOOTH_CONNECT_STATUS, SysEventUtil.OFF);
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
        this.mDeviceTypeList = null;
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

    /* access modifiers changed from: private */
    public void saveBluetoothDeviceInfo(BluetoothDevice device) {
        JSONObject object = new JSONObject();
        if (device == null) {
            this.mBluetoothInfo = "";
            return;
        }
        BluetoothClass bluetoothClass = device.getBluetoothClass();
        if (bluetoothClass == null) {
            this.mBluetoothInfo = "";
            return;
        }
        String deviceType = getDeviceTypeString(bluetoothClass.getMajorDeviceClass());
        try {
            object.put("address", device.getAddress());
            object.put(OPCollectConstant.WIFI_NAME, device.getName());
            object.put("type", deviceType);
            this.mBluetoothInfo = object.toString();
        } catch (JSONException e) {
            this.mBluetoothInfo = "";
        }
    }

    private String getDeviceTypeString(int type) {
        if (this.mDeviceTypeList == null || type >= this.mDeviceTypeList.size() || type < 0) {
            return "";
        }
        return this.mDeviceTypeList.get(Integer.valueOf(type));
    }

    private void initDeviceTypeMap() {
        if (this.mDeviceTypeList == null) {
            this.mDeviceTypeList = new HashMap();
            this.mDeviceTypeList.put(0, "misc");
            this.mDeviceTypeList.put(256, "computer");
            this.mDeviceTypeList.put(512, "phone");
            this.mDeviceTypeList.put(768, "networking");
            this.mDeviceTypeList.put(Integer.valueOf(AwarenessConstants.HIACTION_CARDUPDATE_ACTION), "audio_video");
            this.mDeviceTypeList.put(1280, "peripheral");
            this.mDeviceTypeList.put(1536, "imaging");
            this.mDeviceTypeList.put(1792, "wearable");
            this.mDeviceTypeList.put(Integer.valueOf(AwarenessConstants.HIACTION_EXPRESS_ACTION), "toy");
            this.mDeviceTypeList.put(2304, "health");
            this.mDeviceTypeList.put(7936, "uncategorized");
        }
    }
}
