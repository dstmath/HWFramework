package com.huawei.systemmanager.power;

import com.android.internal.os.BatterySipper;

public class HwBatterySipper {
    private BatterySipper mLocalsipper;

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

    public HwBatterySipper(BatterySipper sipper) {
        this.mLocalsipper = sipper;
    }

    public BatterySipper getBatterySipper() {
        return this.mLocalsipper;
    }

    public String[] getPackages() {
        if (this.mLocalsipper == null) {
            return null;
        }
        return this.mLocalsipper.getPackages();
    }

    public int getUid() {
        if (this.mLocalsipper == null) {
            return 0;
        }
        return this.mLocalsipper.getUid();
    }

    public void add(HwBatterySipper other) {
        if (this.mLocalsipper != null) {
            this.mLocalsipper.add(other.mLocalsipper);
        }
    }

    public double getTotalPowerMah() {
        if (this.mLocalsipper == null) {
            return 0.0d;
        }
        return this.mLocalsipper.totalPowerMah;
    }

    public boolean isDrainTypeApp() {
        boolean z = false;
        if (this.mLocalsipper == null) {
            return false;
        }
        if (this.mLocalsipper.drainType == BatterySipper.DrainType.APP) {
            z = true;
        }
        return z;
    }

    public boolean isSameDrainType(HwDrainType drainType) {
        boolean z = false;
        if (this.mLocalsipper == null || drainType == null) {
            return false;
        }
        if (this.mLocalsipper.drainType == drainType.getInnerLocalDranType()) {
            z = true;
        }
        return z;
    }

    public long getCpuTimeMs() {
        if (this.mLocalsipper == null) {
            return 0;
        }
        return (this.mLocalsipper.uidObj.getUserCpuTimeUs(2) + this.mLocalsipper.uidObj.getSystemCpuTimeUs(2)) / 1000;
    }
}
