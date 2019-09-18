package com.android.internal.os;

import android.os.BatteryStats;

public class BatterySipper implements Comparable<BatterySipper> {
    public double audioPowerMah;
    public long audioTimeMs;
    public double bluetoothPowerMah;
    public long bluetoothRunningTimeMs;
    public long btRxBytes;
    public long btTxBytes;
    public double cameraPowerMah;
    public long cameraTimeMs;
    public long cpuFgTimeMs;
    public double cpuPowerMah;
    public long cpuTimeMs;
    public DrainType drainType;
    public double flashlightPowerMah;
    public long flashlightTimeMs;
    public double gpsPowerMah;
    public long gpsTimeMs;
    public String[] mPackages;
    public long mobileActive;
    public int mobileActiveCount;
    public double mobileRadioPowerMah;
    public long mobileRxBytes;
    public long mobileRxPackets;
    public long mobileTxBytes;
    public long mobileTxPackets;
    public double mobilemspp;
    public double noCoveragePercent;
    public String packageWithHighestDrain;
    public double percent;
    public double proportionalSmearMah;
    public double screenPowerMah;
    public double sensorPowerMah;
    public boolean shouldHide;
    public double totalPowerMah;
    public double totalSmearedPowerMah;
    public BatteryStats.Uid uidObj;
    public double usagePowerMah;
    public long usageTimeMs;
    public int userId;
    public double videoPowerMah;
    public long videoTimeMs;
    public double wakeLockPowerMah;
    public long wakeLockTimeMs;
    public double wifiPowerMah;
    public long wifiRunningTimeMs;
    public long wifiRxBytes;
    public long wifiRxPackets;
    public long wifiTxBytes;
    public long wifiTxPackets;

    public enum DrainType {
        AMBIENT_DISPLAY,
        APP,
        BLUETOOTH,
        CAMERA,
        CELL,
        FLASHLIGHT,
        IDLE,
        MEMORY,
        OVERCOUNTED,
        PHONE,
        SCREEN,
        UNACCOUNTED,
        USER,
        WIFI
    }

    public BatterySipper(DrainType drainType2, BatteryStats.Uid uid, double value) {
        this.totalPowerMah = value;
        this.drainType = drainType2;
        this.uidObj = uid;
    }

    public void computeMobilemspp() {
        long packets = this.mobileRxPackets + this.mobileTxPackets;
        this.mobilemspp = packets > 0 ? ((double) this.mobileActive) / ((double) packets) : 0.0d;
    }

    public int compareTo(BatterySipper other) {
        if (this.drainType != other.drainType) {
            if (this.drainType == DrainType.OVERCOUNTED) {
                return 1;
            }
            if (other.drainType == DrainType.OVERCOUNTED) {
                return -1;
            }
        }
        return Double.compare(other.totalPowerMah, this.totalPowerMah);
    }

    public String[] getPackages() {
        return this.mPackages;
    }

    public int getUid() {
        if (this.uidObj == null) {
            return 0;
        }
        return this.uidObj.getUid();
    }

    public void add(BatterySipper other) {
        this.totalPowerMah += other.totalPowerMah;
        this.usageTimeMs += other.usageTimeMs;
        this.usagePowerMah += other.usagePowerMah;
        this.audioTimeMs += other.audioTimeMs;
        this.cpuTimeMs += other.cpuTimeMs;
        this.gpsTimeMs += other.gpsTimeMs;
        this.wifiRunningTimeMs += other.wifiRunningTimeMs;
        this.cpuFgTimeMs += other.cpuFgTimeMs;
        this.videoTimeMs += other.videoTimeMs;
        this.wakeLockTimeMs += other.wakeLockTimeMs;
        this.cameraTimeMs += other.cameraTimeMs;
        this.flashlightTimeMs += other.flashlightTimeMs;
        this.bluetoothRunningTimeMs += other.bluetoothRunningTimeMs;
        this.mobileRxPackets += other.mobileRxPackets;
        this.mobileTxPackets += other.mobileTxPackets;
        this.mobileActive += other.mobileActive;
        this.mobileActiveCount += other.mobileActiveCount;
        this.wifiRxPackets += other.wifiRxPackets;
        this.wifiTxPackets += other.wifiTxPackets;
        this.mobileRxBytes += other.mobileRxBytes;
        this.mobileTxBytes += other.mobileTxBytes;
        this.wifiRxBytes += other.wifiRxBytes;
        this.wifiTxBytes += other.wifiTxBytes;
        this.btRxBytes += other.btRxBytes;
        this.btTxBytes += other.btTxBytes;
        this.audioPowerMah += other.audioPowerMah;
        this.wifiPowerMah += other.wifiPowerMah;
        this.gpsPowerMah += other.gpsPowerMah;
        this.cpuPowerMah += other.cpuPowerMah;
        this.sensorPowerMah += other.sensorPowerMah;
        this.mobileRadioPowerMah += other.mobileRadioPowerMah;
        this.wakeLockPowerMah += other.wakeLockPowerMah;
        this.cameraPowerMah += other.cameraPowerMah;
        this.flashlightPowerMah += other.flashlightPowerMah;
        this.bluetoothPowerMah += other.bluetoothPowerMah;
        this.screenPowerMah += other.screenPowerMah;
        this.videoPowerMah += other.videoPowerMah;
        this.proportionalSmearMah += other.proportionalSmearMah;
        this.totalSmearedPowerMah += other.totalSmearedPowerMah;
    }

    public double sumPower() {
        this.totalPowerMah = this.usagePowerMah + this.wifiPowerMah + this.gpsPowerMah + this.cpuPowerMah + this.sensorPowerMah + this.mobileRadioPowerMah + this.wakeLockPowerMah + this.cameraPowerMah + this.flashlightPowerMah + this.bluetoothPowerMah + this.audioPowerMah + this.videoPowerMah;
        this.totalSmearedPowerMah = this.totalPowerMah + this.screenPowerMah + this.proportionalSmearMah;
        return this.totalPowerMah;
    }
}
