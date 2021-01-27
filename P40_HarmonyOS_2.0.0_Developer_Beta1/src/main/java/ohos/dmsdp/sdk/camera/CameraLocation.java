package ohos.dmsdp.sdk.camera;

import ohos.global.icu.impl.PatternTokenizer;

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
    PAD_CENTER("0"),
    PAD_LEFT("1"),
    PAD_RIGHT("2");
    
    private String location;

    private CameraLocation(String str) {
        this.location = str;
    }

    public String getLocation() {
        return this.location;
    }

    public static boolean isValid(String str) {
        for (CameraLocation cameraLocation : values()) {
            if (cameraLocation.getLocation().equals(str)) {
                return true;
            }
        }
        return false;
    }

    public static CameraLocation valueFrom(String str) {
        CameraLocation[] values = values();
        for (CameraLocation cameraLocation : values) {
            if (cameraLocation.getLocation().equals(str)) {
                return cameraLocation;
            }
        }
        return null;
    }

    @Override // java.lang.Enum, java.lang.Object
    public String toString() {
        return "CameraLocation{location='" + this.location + PatternTokenizer.SINGLE_QUOTE + '}';
    }
}
