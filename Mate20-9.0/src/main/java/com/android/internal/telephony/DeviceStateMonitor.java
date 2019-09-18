package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.display.DisplayManager;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.telephony.Rlog;
import android.util.LocalLog;
import android.util.SparseIntArray;
import android.view.Display;
import com.android.internal.util.IndentingPrintWriter;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

public class DeviceStateMonitor extends Handler {
    protected static final boolean DBG = false;
    private static final int EVENT_CHARGING_STATE_CHANGED = 4;
    private static final int EVENT_HW_BASE = 100;
    private static final int EVENT_POWER_SAVE_MODE_CHANGED = 3;
    private static final int EVENT_RIL_CONNECTED = 0;
    private static final int EVENT_SCREEN_STATE_CHANGED = 2;
    private static final int EVENT_TETHERING_STATE_CHANGED = 5;
    private static final int EVENT_UPDATE_ALL_STATE = 101;
    private static final int EVENT_UPDATE_MODE_CHANGED = 1;
    private static final int HYSTERESIS_KBPS = 50;
    private static final int[] LINK_CAPACITY_DOWNLINK_THRESHOLDS = {500, 1000, AbstractPhoneBase.SET_TO_AOTO_TIME, 10000, 20000};
    private static final int[] LINK_CAPACITY_UPLINK_THRESHOLDS = {100, 500, 1000, AbstractPhoneBase.SET_TO_AOTO_TIME, 10000};
    protected static final String TAG = DeviceStateMonitor.class.getSimpleName();
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* JADX WARNING: Removed duplicated region for block: B:26:0x006e  */
        /* JADX WARNING: Removed duplicated region for block: B:28:0x0085  */
        /* JADX WARNING: Removed duplicated region for block: B:42:0x00c1  */
        /* JADX WARNING: Removed duplicated region for block: B:43:0x00ca  */
        /* JADX WARNING: Removed duplicated region for block: B:44:0x00d3  */
        public void onReceive(Context context, Intent intent) {
            char c;
            Message msg;
            if (intent != null && intent.getAction() != null) {
                int i = 1;
                DeviceStateMonitor.this.log("received: " + intent, true);
                String action = intent.getAction();
                int hashCode = action.hashCode();
                if (hashCode != -1754841973) {
                    if (hashCode != -54942926) {
                        if (hashCode != 948344062) {
                            if (hashCode == 1779291251 && action.equals("android.os.action.POWER_SAVE_MODE_CHANGED")) {
                                c = 0;
                                switch (c) {
                                    case 0:
                                        msg = DeviceStateMonitor.this.obtainMessage(3);
                                        msg.arg1 = DeviceStateMonitor.this.isPowerSaveModeOn() ? 1 : 0;
                                        DeviceStateMonitor deviceStateMonitor = DeviceStateMonitor.this;
                                        StringBuilder sb = new StringBuilder();
                                        sb.append("Power Save mode ");
                                        sb.append(msg.arg1 == 1 ? "on" : "off");
                                        deviceStateMonitor.log(sb.toString(), true);
                                        break;
                                    case 1:
                                        msg = DeviceStateMonitor.this.obtainMessage(4);
                                        msg.arg1 = 1;
                                        break;
                                    case 2:
                                        msg = DeviceStateMonitor.this.obtainMessage(4);
                                        msg.arg1 = 0;
                                        break;
                                    case 3:
                                        ArrayList<String> activeTetherIfaces = intent.getStringArrayListExtra("tetherArray");
                                        boolean isTetheringOn = activeTetherIfaces != null && activeTetherIfaces.size() > 0;
                                        DeviceStateMonitor deviceStateMonitor2 = DeviceStateMonitor.this;
                                        StringBuilder sb2 = new StringBuilder();
                                        sb2.append("Tethering ");
                                        sb2.append(isTetheringOn ? "on" : "off");
                                        deviceStateMonitor2.log(sb2.toString(), true);
                                        msg = DeviceStateMonitor.this.obtainMessage(5);
                                        if (!isTetheringOn) {
                                            i = 0;
                                        }
                                        msg.arg1 = i;
                                        break;
                                    default:
                                        DeviceStateMonitor.this.log("Unexpected broadcast intent: " + intent, false);
                                        return;
                                }
                                DeviceStateMonitor.this.sendMessage(msg);
                            }
                        } else if (action.equals("android.os.action.CHARGING")) {
                            c = 1;
                            switch (c) {
                                case 0:
                                    break;
                                case 1:
                                    break;
                                case 2:
                                    break;
                                case 3:
                                    break;
                            }
                            DeviceStateMonitor.this.sendMessage(msg);
                        }
                    } else if (action.equals("android.os.action.DISCHARGING")) {
                        c = 2;
                        switch (c) {
                            case 0:
                                break;
                            case 1:
                                break;
                            case 2:
                                break;
                            case 3:
                                break;
                        }
                        DeviceStateMonitor.this.sendMessage(msg);
                    }
                } else if (action.equals("android.net.conn.TETHER_STATE_CHANGED")) {
                    c = 3;
                    switch (c) {
                        case 0:
                            break;
                        case 1:
                            break;
                        case 2:
                            break;
                        case 3:
                            break;
                    }
                    DeviceStateMonitor.this.sendMessage(msg);
                }
                c = 65535;
                switch (c) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                }
                DeviceStateMonitor.this.sendMessage(msg);
            }
        }
    };
    private final DisplayManager.DisplayListener mDisplayListener = new DisplayManager.DisplayListener() {
        public void onDisplayAdded(int displayId) {
        }

        public void onDisplayRemoved(int displayId) {
        }

        public void onDisplayChanged(int displayId) {
            boolean screenOn = DeviceStateMonitor.this.isScreenOn();
            Message msg = DeviceStateMonitor.this.obtainMessage(2);
            msg.arg1 = screenOn;
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
    private int mUnsolicitedResponseFilter = -1;
    private SparseIntArray mUpdateModes = new SparseIntArray();

    private static final class AccessNetworkThresholds {
        public static final int[] CDMA2000 = {-105, -90, -75, -65};
        public static final int[] EUTRAN = {-140, -128, -118, -108, -98, -44};
        public static final int[] GERAN = {-109, -103, -97, -89};
        public static final int[] UTRAN = {-114, -104, -94, -84};

        private AccessNetworkThresholds() {
        }
    }

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
        return !this.mIsCharging && !this.mIsTetheringOn && !this.mIsScreenOn;
    }

    private boolean shouldTurnOffSignalStrength() {
        if (this.mIsScreenOn || this.mUpdateModes.get(1) == 2) {
            return false;
        }
        return true;
    }

    private boolean shouldTurnOffFullNetworkUpdate() {
        if (this.mIsScreenOn || this.mIsTetheringOn || this.mUpdateModes.get(2) == 2) {
            return false;
        }
        return true;
    }

    private boolean shouldTurnOffDormancyUpdate() {
        if (this.mIsScreenOn || this.mIsTetheringOn || this.mUpdateModes.get(4) == 2) {
            return false;
        }
        return true;
    }

    private boolean shouldTurnOffLinkCapacityEstimate() {
        if (this.mIsScreenOn || this.mIsTetheringOn || this.mUpdateModes.get(8) == 2) {
            return false;
        }
        return true;
    }

    private boolean shouldTurnOffPhysicalChannelConfig() {
        if (this.mIsScreenOn || this.mIsTetheringOn || this.mUpdateModes.get(16) == 2) {
            return false;
        }
        return true;
    }

    public void setIndicationUpdateMode(int filters, int mode) {
        sendMessage(obtainMessage(1, filters, mode));
    }

    private void onSetIndicationUpdateMode(int filters, int mode) {
        if ((filters & 1) != 0) {
            this.mUpdateModes.put(1, mode);
        }
        if ((filters & 2) != 0) {
            this.mUpdateModes.put(2, mode);
        }
        if ((filters & 4) != 0) {
            this.mUpdateModes.put(4, mode);
        }
        if ((filters & 8) != 0) {
            this.mUpdateModes.put(8, mode);
        }
        if ((filters & 16) != 0) {
            this.mUpdateModes.put(16, mode);
        }
    }

    public void handleMessage(Message msg) {
        String str = TAG;
        Rlog.d(str, "handleMessage msg=" + msg);
        switch (msg.what) {
            case 0:
                onRilConnected();
                return;
            case 1:
                onSetIndicationUpdateMode(msg.arg1, msg.arg2);
                return;
            case 2:
            case 3:
            case 4:
            case 5:
                onUpdateDeviceState(msg.what, msg.arg1 != 0, false);
                return;
            default:
                throw new IllegalStateException("Unexpected message arrives. msg = " + msg.what);
        }
    }

    private void onUpdateDeviceState(int eventType, boolean state, boolean force) {
        if (eventType != 101) {
            switch (eventType) {
                case 2:
                    if (this.mIsScreenOn != state) {
                        this.mIsScreenOn = state;
                        break;
                    } else {
                        return;
                    }
                case 3:
                    if (this.mIsPowerSaveOn != state) {
                        this.mIsPowerSaveOn = state;
                        sendDeviceState(0, this.mIsPowerSaveOn);
                        break;
                    } else {
                        return;
                    }
                case 4:
                    if (this.mIsCharging != state) {
                        this.mIsCharging = state;
                        sendDeviceState(1, this.mIsCharging);
                        break;
                    } else {
                        return;
                    }
                case 5:
                    if (this.mIsTetheringOn != state) {
                        this.mIsTetheringOn = state;
                        break;
                    } else {
                        return;
                    }
                default:
                    return;
            }
        }
        if (this.mIsLowDataExpected != isLowDataExpected()) {
            this.mIsLowDataExpected = !this.mIsLowDataExpected;
            sendDeviceState(2, this.mIsLowDataExpected);
        }
        String str = TAG;
        Rlog.d(str, "mIsScreenOn: " + this.mIsScreenOn + " mIsCharging: " + this.mIsCharging + " mIsTetheringOn: " + this.mIsTetheringOn + " mIsPowerSaveOn:" + this.mIsPowerSaveOn);
        int newFilter = 0;
        if (!shouldTurnOffSignalStrength()) {
            newFilter = 0 | 1;
        }
        if (!shouldTurnOffFullNetworkUpdate()) {
            newFilter |= 2;
        }
        if (!shouldTurnOffDormancyUpdate()) {
            newFilter |= 4;
        }
        if (!shouldTurnOffLinkCapacityEstimate()) {
            newFilter |= 8;
        }
        if (!shouldTurnOffPhysicalChannelConfig()) {
            newFilter |= 16;
        }
        setUnsolResponseFilter(newFilter, force);
    }

    private void onRilConnected() {
        log("RIL connected.", true);
        sendDeviceState(1, this.mIsCharging);
        sendDeviceState(2, this.mIsLowDataExpected);
        sendDeviceState(0, this.mIsPowerSaveOn);
        onUpdateDeviceState(101, false, true);
        setSignalStrengthReportingCriteria();
        setLinkCapacityReportingCriteria();
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

    private void setSignalStrengthReportingCriteria() {
        this.mPhone.setSignalStrengthReportingCriteria(AccessNetworkThresholds.GERAN, 1);
        this.mPhone.setSignalStrengthReportingCriteria(AccessNetworkThresholds.UTRAN, 2);
        this.mPhone.setSignalStrengthReportingCriteria(AccessNetworkThresholds.EUTRAN, 3);
        this.mPhone.setSignalStrengthReportingCriteria(AccessNetworkThresholds.CDMA2000, 4);
    }

    private void setLinkCapacityReportingCriteria() {
        this.mPhone.setLinkCapacityReportingCriteria(LINK_CAPACITY_DOWNLINK_THRESHOLDS, LINK_CAPACITY_UPLINK_THRESHOLDS, 1);
        this.mPhone.setLinkCapacityReportingCriteria(LINK_CAPACITY_DOWNLINK_THRESHOLDS, LINK_CAPACITY_UPLINK_THRESHOLDS, 2);
        this.mPhone.setLinkCapacityReportingCriteria(LINK_CAPACITY_DOWNLINK_THRESHOLDS, LINK_CAPACITY_UPLINK_THRESHOLDS, 3);
        this.mPhone.setLinkCapacityReportingCriteria(LINK_CAPACITY_DOWNLINK_THRESHOLDS, LINK_CAPACITY_UPLINK_THRESHOLDS, 4);
    }

    /* access modifiers changed from: private */
    public boolean isPowerSaveModeOn() {
        return ((PowerManager) this.mPhone.getContext().getSystemService("power")).isPowerSaveMode();
    }

    public boolean isDeviceCharging() {
        return ((BatteryManager) this.mPhone.getContext().getSystemService("batterymanager")).isCharging();
    }

    /* access modifiers changed from: private */
    public boolean isScreenOn() {
        Display[] displays = ((DisplayManager) this.mPhone.getContext().getSystemService("display")).getDisplays();
        if (displays != null) {
            for (Display display : displays) {
                if (display.getState() == 2) {
                    Rlog.d(TAG, "Screen " + Display.typeToString(display.getType()) + " on");
                    return true;
                }
            }
            Rlog.d(TAG, "Screens all off");
            return false;
        }
        Rlog.d(TAG, "No displays found");
        return false;
    }

    /* access modifiers changed from: private */
    public void log(String msg, boolean logIntoLocalLog) {
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
