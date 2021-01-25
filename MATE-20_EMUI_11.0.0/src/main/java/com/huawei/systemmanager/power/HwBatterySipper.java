package com.huawei.systemmanager.power;

import com.android.internal.os.BatterySipper;

public class HwBatterySipper {
    private BatterySipper mLocalsipper;

    public HwBatterySipper(BatterySipper sipper) {
        this.mLocalsipper = sipper;
    }

    public BatterySipper getBatterySipper() {
        return this.mLocalsipper;
    }

    public String[] getPackages() {
        BatterySipper batterySipper = this.mLocalsipper;
        if (batterySipper == null) {
            return null;
        }
        return batterySipper.getPackages();
    }

    public int getUid() {
        BatterySipper batterySipper = this.mLocalsipper;
        if (batterySipper == null) {
            return 0;
        }
        return batterySipper.getUid();
    }

    public void add(HwBatterySipper other) {
        BatterySipper batterySipper = this.mLocalsipper;
        if (batterySipper != null) {
            batterySipper.add(other.mLocalsipper);
        }
    }

    public double getTotalPowerMah() {
        BatterySipper batterySipper = this.mLocalsipper;
        if (batterySipper == null) {
            return 0.0d;
        }
        return batterySipper.totalPowerMah;
    }

    public enum HwDrainType {
        IDLE(BatterySipper.DrainType.IDLE),
        CELL(BatterySipper.DrainType.CELL),
        PHONE(BatterySipper.DrainType.PHONE),
        WIFI(BatterySipper.DrainType.WIFI),
        BLUETOOTH(BatterySipper.DrainType.BLUETOOTH),
        FLASHLIGHT(BatterySipper.DrainType.FLASHLIGHT),
        SCREEN(BatterySipper.DrainType.SCREEN),
        APP(BatterySipper.DrainType.APP),
        USER(BatterySipper.DrainType.USER),
        UNACCOUNTED(BatterySipper.DrainType.UNACCOUNTED),
        OVERCOUNTED(BatterySipper.DrainType.OVERCOUNTED),
        CAMERA(BatterySipper.DrainType.CAMERA),
        MEMORY(BatterySipper.DrainType.MEMORY);
        
        private BatterySipper.DrainType mLocaldrantype;

        private HwDrainType(BatterySipper.DrainType draintype) {
            this.mLocaldrantype = draintype;
        }

        public BatterySipper.DrainType getInnerLocalDranType() {
            return this.mLocaldrantype;
        }
    }

    public boolean isDrainTypeApp() {
        BatterySipper batterySipper = this.mLocalsipper;
        if (batterySipper != null && batterySipper.drainType == BatterySipper.DrainType.APP) {
            return true;
        }
        return false;
    }

    public boolean isSameDrainType(HwDrainType drainType) {
        BatterySipper batterySipper = this.mLocalsipper;
        if (batterySipper == null || drainType == null || batterySipper.drainType != drainType.getInnerLocalDranType()) {
            return false;
        }
        return true;
    }

    public long getCpuTimeMs() {
        BatterySipper batterySipper = this.mLocalsipper;
        if (batterySipper == null) {
            return 0;
        }
        return (batterySipper.uidObj.getUserCpuTimeUs(2) + this.mLocalsipper.uidObj.getSystemCpuTimeUs(2)) / 1000;
    }
}
