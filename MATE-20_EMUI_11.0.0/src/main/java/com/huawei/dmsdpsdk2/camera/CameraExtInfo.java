package com.huawei.dmsdpsdk2.camera;

public class CameraExtInfo {
    private String desc;
    private CameraLocation location;

    public CameraLocation getLocation() {
        return this.location;
    }

    public void setLocation(CameraLocation location2) {
        this.location = location2;
    }

    public String getDesc() {
        return this.desc;
    }

    public void setDesc(String desc2) {
        this.desc = desc2;
    }

    public String toString() {
        return "CameraExtInfo{location=" + this.location + ", desc='" + this.desc + "'}";
    }
}
