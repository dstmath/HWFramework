package ohos.dmsdp.sdk.camera;

import ohos.global.icu.impl.PatternTokenizer;

public class CameraExtInfo {
    private String desc;
    private CameraLocation location;

    public CameraLocation getLocation() {
        return this.location;
    }

    public void setLocation(CameraLocation cameraLocation) {
        this.location = cameraLocation;
    }

    public String getDesc() {
        return this.desc;
    }

    public void setDesc(String str) {
        this.desc = str;
    }

    public String toString() {
        return "CameraExtInfo{location=" + this.location + ", desc='" + this.desc + PatternTokenizer.SINGLE_QUOTE + '}';
    }
}
