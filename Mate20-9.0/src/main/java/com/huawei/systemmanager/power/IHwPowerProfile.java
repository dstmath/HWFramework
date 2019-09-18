package com.huawei.systemmanager.power;

public interface IHwPowerProfile {
    public static final String POWER_BLUETOOTH_ON = "bluetooth.on";
    public static final String POWER_CPU_ACTIVE = "cpu.active";
    public static final String POWER_CPU_IDLE = "cpu.idle";
    public static final String POWER_RADIO_ON = "radio.on";
    public static final String POWER_SCREEN_FULL = "screen.full";
    public static final String POWER_SCREEN_ON = "screen.on";
    public static final String POWER_WIFI_ON = "wifi.on";

    double getAveragePower(String str);

    double getAveragePower(String str, int i);

    double getBatteryCapacity();

    long getCpuFgTimeMs(HwBatterySipper hwBatterySipper);

    double getCpuPowerMaMs(HwBatterySipper hwBatterySipper, long j);

    double getMinAveragePowerForCpu();

    long getTotalClusterTime(HwBatterySipper hwBatterySipper);
}
