package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManager.DisplayListener;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.LocalLog;
import android.view.Display;
import com.android.internal.util.IndentingPrintWriter;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

public class DeviceStateMonitor extends Handler {
    protected static final boolean DBG = false;
    private static final int EVENT_CHARGING_STATE_CHANGED = 3;
    private static final int EVENT_POWER_SAVE_MODE_CHANGED = 2;
    private static final int EVENT_RIL_CONNECTED = 0;
    private static final int EVENT_SCREEN_STATE_CHANGED = 1;
    private static final int EVENT_TETHERING_STATE_CHANGED = 4;
    protected static final String TAG = DeviceStateMonitor.class.getSimpleName();
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Message msg;
            int i = 0;
            int i2 = 1;
            DeviceStateMonitor.this.log("received: " + intent, true);
            String action = intent.getAction();
            if (action.equals("android.os.action.POWER_SAVE_MODE_CHANGED")) {
                String str;
                msg = DeviceStateMonitor.this.obtainMessage(2);
                if (DeviceStateMonitor.this.isPowerSaveModeOn()) {
                    i = 1;
                }
                msg.arg1 = i;
                DeviceStateMonitor deviceStateMonitor = DeviceStateMonitor.this;
                StringBuilder append = new StringBuilder().append("Power Save mode ");
                if (msg.arg1 == 1) {
                    str = "on";
                } else {
                    str = "off";
                }
                deviceStateMonitor.log(append.append(str).toString(), true);
            } else if (action.equals("android.os.action.CHARGING")) {
                msg = DeviceStateMonitor.this.obtainMessage(3);
                msg.arg1 = 1;
            } else if (action.equals("android.os.action.DISCHARGING")) {
                msg = DeviceStateMonitor.this.obtainMessage(3);
                msg.arg1 = 0;
            } else if (action.equals("android.net.conn.TETHER_STATE_CHANGED")) {
                ArrayList<String> activeTetherIfaces = intent.getStringArrayListExtra("tetherArray");
                boolean isTetheringOn = activeTetherIfaces != null ? activeTetherIfaces.size() > 0 : false;
                DeviceStateMonitor.this.log("Tethering " + (isTetheringOn ? "on" : "off"), true);
                msg = DeviceStateMonitor.this.obtainMessage(4);
                if (!isTetheringOn) {
                    i2 = 0;
                }
                msg.arg1 = i2;
            } else {
                DeviceStateMonitor.this.log("Unexpected broadcast intent: " + intent, false);
                return;
            }
            DeviceStateMonitor.this.sendMessage(msg);
        }
    };
    private final DisplayListener mDisplayListener = new DisplayListener() {
        public void onDisplayAdded(int displayId) {
        }

        public void onDisplayRemoved(int displayId) {
        }

        public void onDisplayChanged(int displayId) {
            int i = 1;
            boolean screenOn = DeviceStateMonitor.this.isScreenOn();
            Message msg = DeviceStateMonitor.this.obtainMessage(1);
            if (!screenOn) {
                i = 0;
            }
            msg.arg1 = i;
            DeviceStateMonitor.this.sendMessage(msg);
        }
    };
    private boolean mIsCharging;
    private boolean mIsLowDataExpected;
    private boolean mIsPowerSaveOn;
    private boolean mIsScreenOn;
    private boolean mIsTetheringOn;
    private final LocalLog mLocalLog = new LocalLog(100);
    private final Phone mPhone;
    private int mUnsolicitedResponseFilter = 7;

    public DeviceStateMonitor(Phone phone) {
        this.mPhone = phone;
        ((DisplayManager) phone.getContext().getSystemService("display")).registerDisplayListener(this.mDisplayListener, null);
        this.mIsPowerSaveOn = isPowerSaveModeOn();
        this.mIsCharging = isDeviceCharging();
        this.mIsScreenOn = isScreenOn();
        this.mIsTetheringOn = false;
        this.mIsLowDataExpected = false;
        log("DeviceStateMonitor mIsPowerSaveOn=" + this.mIsPowerSaveOn + ",mIsScreenOn=" + this.mIsScreenOn + ",mIsCharging=" + this.mIsCharging, false);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.os.action.POWER_SAVE_MODE_CHANGED");
        filter.addAction("android.os.action.CHARGING");
        filter.addAction("android.os.action.DISCHARGING");
        filter.addAction("android.net.conn.TETHER_STATE_CHANGED");
        this.mPhone.getContext().registerReceiver(this.mBroadcastReceiver, filter, null, this.mPhone);
        this.mPhone.mCi.registerForRilConnected(this, 0, null);
    }

    private boolean isLowDataExpected() {
        if (this.mIsPowerSaveOn) {
            return true;
        }
        return (this.mIsCharging || (this.mIsTetheringOn ^ 1) == 0) ? false : this.mIsScreenOn ^ 1;
    }

    private boolean shouldTurnOffSignalStrength() {
        if (this.mIsPowerSaveOn) {
            return true;
        }
        return !this.mIsCharging ? this.mIsScreenOn ^ 1 : false;
    }

    private boolean shouldTurnOffFullNetworkUpdate() {
        if (this.mIsPowerSaveOn) {
            return true;
        }
        return (this.mIsCharging || (this.mIsScreenOn ^ 1) == 0) ? false : this.mIsTetheringOn ^ 1;
    }

    private boolean shouldTurnOffDormancyUpdate() {
        if (this.mIsPowerSaveOn) {
            return true;
        }
        return (this.mIsCharging || (this.mIsTetheringOn ^ 1) == 0) ? false : this.mIsScreenOn ^ 1;
    }

    public void handleMessage(Message msg) {
        boolean z = false;
        log("handleMessage msg=" + msg, false);
        switch (msg.what) {
            case 0:
                onRilConnected();
                return;
            default:
                int i = msg.what;
                if (msg.arg1 != 0) {
                    z = true;
                }
                updateDeviceState(i, z);
                return;
        }
    }

    private void updateDeviceState(int eventType, boolean state) {
        switch (eventType) {
            case 1:
                if (this.mIsScreenOn != state) {
                    this.mIsScreenOn = state;
                    break;
                }
                return;
            case 2:
                if (this.mIsPowerSaveOn != state) {
                    this.mIsPowerSaveOn = state;
                    sendDeviceState(0, this.mIsPowerSaveOn);
                    break;
                }
                return;
            case 3:
                if (this.mIsCharging != state) {
                    this.mIsCharging = state;
                    sendDeviceState(1, this.mIsCharging);
                    break;
                }
                return;
            case 4:
                if (this.mIsTetheringOn != state) {
                    this.mIsTetheringOn = state;
                    break;
                }
                return;
            default:
                return;
        }
        if (this.mIsLowDataExpected != isLowDataExpected()) {
            this.mIsLowDataExpected ^= 1;
            sendDeviceState(2, this.mIsLowDataExpected);
        }
        int newFilter = 0;
        if (!shouldTurnOffSignalStrength()) {
            newFilter = 1;
        }
        if (!shouldTurnOffFullNetworkUpdate()) {
            newFilter |= 2;
        }
        if (!shouldTurnOffDormancyUpdate()) {
            newFilter |= 4;
        }
        setUnsolResponseFilter(newFilter, false);
    }

    private void onRilConnected() {
        log("RIL connected.", true);
        sendDeviceState(1, this.mIsCharging);
        sendDeviceState(2, this.mIsLowDataExpected);
        sendDeviceState(0, this.mIsPowerSaveOn);
        setUnsolResponseFilter(this.mUnsolicitedResponseFilter, true);
    }

    private String deviceTypeToString(int type) {
        switch (type) {
            case 0:
                return "POWER_SAVE_MODE";
            case 1:
                return "CHARGING_STATE";
            case 2:
                return "LOW_DATA_EXPECTED";
            default:
                return "UNKNOWN";
        }
    }

    private void sendDeviceState(int type, boolean state) {
        log("send type: " + deviceTypeToString(type) + ", state=" + state, true);
        this.mPhone.mCi.sendDeviceState(type, state, null);
    }

    private void setUnsolResponseFilter(int newFilter, boolean force) {
        if (force || newFilter != this.mUnsolicitedResponseFilter) {
            log("old filter: " + this.mUnsolicitedResponseFilter + ", new filter: " + newFilter, true);
            this.mPhone.mCi.setUnsolResponseFilter(newFilter, null);
            this.mUnsolicitedResponseFilter = newFilter;
        }
    }

    private boolean isPowerSaveModeOn() {
        return ((PowerManager) this.mPhone.getContext().getSystemService("power")).isPowerSaveMode();
    }

    private boolean isDeviceCharging() {
        return ((BatteryManager) this.mPhone.getContext().getSystemService("batterymanager")).isCharging();
    }

    private boolean isScreenOn() {
        Display[] displays = ((DisplayManager) this.mPhone.getContext().getSystemService("display")).getDisplays();
        if (displays != null) {
            for (Display display : displays) {
                if (display.getState() == 2) {
                    log("Screen " + Display.typeToString(display.getType()) + " on", true);
                    return true;
                }
            }
            log("Screens all off", true);
            return false;
        }
        log("No displays found", true);
        return false;
    }

    private void log(String msg, boolean logIntoLocalLog) {
        if (logIntoLocalLog) {
            this.mLocalLog.log(msg);
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        IndentingPrintWriter ipw = new IndentingPrintWriter(pw, "  ");
        ipw.increaseIndent();
        ipw.println("mIsTetheringOn=" + this.mIsTetheringOn);
        ipw.println("mIsScreenOn=" + this.mIsScreenOn);
        ipw.println("mIsCharging=" + this.mIsCharging);
        ipw.println("mIsPowerSaveOn=" + this.mIsPowerSaveOn);
        ipw.println("mIsLowDataExpected=" + this.mIsLowDataExpected);
        ipw.println("mUnsolicitedResponseFilter=" + this.mUnsolicitedResponseFilter);
        ipw.println("Local logs:");
        ipw.increaseIndent();
        this.mLocalLog.dump(fd, ipw, args);
        ipw.decreaseIndent();
        ipw.decreaseIndent();
        ipw.flush();
    }
}
