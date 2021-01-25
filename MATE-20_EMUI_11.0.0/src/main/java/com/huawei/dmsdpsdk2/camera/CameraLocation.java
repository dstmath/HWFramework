package com.huawei.dmsdpsdk2.camera;

import com.huawei.dmsdpsdk2.hiplay.HiPlayHelper;

public enum CameraLocation {
    FRONT("FRONT"),
    BACK("BACK"),
    CAR_FRONT("CAR_FRONT"),
    CAR_TAIL("CAR_TAIL"),
    CAR_LEFT("CAR_LEFT"),
    CAR_RIGHT("CAR_RIGHT"),
    CAR_INSIDE("CAR_INSIDE"),
    CAR_DRIVER("CAR_DRIVER"),
    CAR_CODRIVER("CAR_CODRIVER"),
    CAR_REARSEAT_LEFT("CAR_REARSEAT_LEFT"),
    CAR_REARSEAT_RIGHT("CAR_REARSEAT_RIGHT"),
    PAD_CENTER(HiPlayHelper.STEREO_MODE_SINGLE),
    PAD_LEFT("1"),
    PAD_RIGHT(HiPlayHelper.ACCESS_VERSION_AP);
    
    private String location;

    private CameraLocation(String loc) {
        this.location = loc;
    }

    public String getLocation() {
        return this.location;
    }

    public static boolean isValid(String s) {
        for (CameraLocation cl : values()) {
            if (cl.getLocation().equals(s)) {
                return true;
            }
        }
        return false;
    }

    public static CameraLocation valueFrom(String s) {
        CameraLocation[] values = values();
        for (CameraLocation cl : values) {
            if (cl.getLocation().equals(s)) {
                return cl;
            }
        }
        return null;
    }

    @Override // java.lang.Enum, java.lang.Object
    public String toString() {
        return "CameraLocation{location='" + this.location + "'}";
    }
}
