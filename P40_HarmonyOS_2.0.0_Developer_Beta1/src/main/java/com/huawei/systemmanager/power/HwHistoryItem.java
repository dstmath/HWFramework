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
    private BatteryStats.HistoryItem mLocalHistoryItem = new BatteryStats.HistoryItem();

    public BatteryStats.HistoryItem getInnerHistoryItem() {
        BatteryStats.HistoryItem historyItem = this.mLocalHistoryItem;
        if (historyItem == null) {
            return null;
        }
        return historyItem;
    }

    public boolean isDeltaData() {
        BatteryStats.HistoryItem historyItem = this.mLocalHistoryItem;
        if (historyItem == null) {
            return false;
        }
        return historyItem.isDeltaData();
    }

    public long getTime() {
        BatteryStats.HistoryItem historyItem = this.mLocalHistoryItem;
        if (historyItem == null) {
            return 0;
        }
        return historyItem.time;
    }

    public int getState() {
        BatteryStats.HistoryItem historyItem = this.mLocalHistoryItem;
        if (historyItem == null) {
            return 0;
        }
        return historyItem.states;
    }

    public int getState2() {
        BatteryStats.HistoryItem historyItem = this.mLocalHistoryItem;
        if (historyItem == null) {
            return 0;
        }
        return historyItem.states2;
    }

    public byte getBatteryLevel() {
        BatteryStats.HistoryItem historyItem = this.mLocalHistoryItem;
        if (historyItem == null) {
            return 0;
        }
        return historyItem.batteryLevel;
    }

    public byte getCmd() {
        BatteryStats.HistoryItem historyItem = this.mLocalHistoryItem;
        if (historyItem == null) {
            return 0;
        }
        return historyItem.cmd;
    }

    public long getCurrentTime() {
        BatteryStats.HistoryItem historyItem = this.mLocalHistoryItem;
        if (historyItem == null) {
            return 0;
        }
        return historyItem.currentTime;
    }
}
