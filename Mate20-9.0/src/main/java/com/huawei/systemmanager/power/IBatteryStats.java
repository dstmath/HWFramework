package com.huawei.systemmanager.power;

import com.android.internal.os.BatteryStatsHelper;

public interface IBatteryStats {
    public static final int BATTERY_REAL_TIME = 6;
    public static final int BLUETOOTH_TIME = 7;
    public static final int CHARGE_REMAINING_TIME = 8;
    public static final int IDLE = 1;
    public static final int PHONE_ON = 3;
    public static final int RADIO = 2;
    public static final int RADIO_SCANNING = 5;
    public static final int SCREEN_ON = 4;
    public static final int WIFI_ON = 0;

    long computeTimePerLevel();

    void create();

    void finishIteratingHistoryLocked();

    IHwPowerProfile getIHwPowerProfile();

    BatteryStatsHelper getInnerBatteryStatsHelper();

    boolean getNextHistoryLocked(HwHistoryItem hwHistoryItem);

    long getTimeOfItem(long j, int i);

    void init();

    boolean startIteratingHistoryLocked();
}
