package com.huawei.systemmanager.power;

import android.os.BatteryStats;

public class HwHistoryItem {
    public static final byte CMD_CURRENT_TIME = 5;
    public static final byte CMD_OVERFLOW = 6;
    public static final byte CMD_RESET = 7;
    public static final int STATE2_CAMERA_FLAG = 2097152;
    public static final int STATE2_FLASHLIGHT_FLAG = 134217728;
    public static final int STATE2_WIFI_RUNNING_FLAG = 536870912;
    public static final int STATE_BATTERY_PLUGGED_FLAG = 524288;
    public static final int STATE_CPU_RUNNING_FLAG = Integer.MIN_VALUE;
    public static final int STATE_GPS_ON_FLAG = 536870912;
    public static final int STATE_PHONE_SCANNING_FLAG = 2097152;
    public static final int STATE_PHONE_SIGNAL_STRENGTH_MASK = 56;
    public static final int STATE_PHONE_SIGNAL_STRENGTH_SHIFT = 3;
    public static final int STATE_PHONE_STATE_MASK = 448;
    public static final int STATE_PHONE_STATE_SHIFT = 6;
    public static final int STATE_SCREEN_ON_FLAG = 1048576;
    public static final int STATE_WIFI_FULL_LOCK_FLAG = 268435456;
    public static final int STATE_WIFI_MULTICAST_ON_FLAG = 65536;
    public static final int STATE_WIFI_SCAN_FLAG = 134217728;
    private BatteryStats.HistoryItem mlocalHistoryItem = new BatteryStats.HistoryItem();

    public BatteryStats.HistoryItem getInnerHistoryItem() {
        if (this.mlocalHistoryItem == null) {
            return null;
        }
        return this.mlocalHistoryItem;
    }

    public boolean isDeltaData() {
        if (this.mlocalHistoryItem == null) {
            return false;
        }
        return this.mlocalHistoryItem.isDeltaData();
    }

    public long getTime() {
        if (this.mlocalHistoryItem == null) {
            return 0;
        }
        return this.mlocalHistoryItem.time;
    }

    public int getState() {
        if (this.mlocalHistoryItem == null) {
            return 0;
        }
        return this.mlocalHistoryItem.states;
    }

    public int getState2() {
        if (this.mlocalHistoryItem == null) {
            return 0;
        }
        return this.mlocalHistoryItem.states2;
    }

    public byte getBatteryLevel() {
        if (this.mlocalHistoryItem == null) {
            return 0;
        }
        return this.mlocalHistoryItem.batteryLevel;
    }

    public byte getCmd() {
        if (this.mlocalHistoryItem == null) {
            return 0;
        }
        return this.mlocalHistoryItem.cmd;
    }

    public long getCurrentTime() {
        if (this.mlocalHistoryItem == null) {
            return 0;
        }
        return this.mlocalHistoryItem.currentTime;
    }
}
