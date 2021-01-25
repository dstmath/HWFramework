package com.huawei.dmsdp.devicevirtualization;

public enum Capability {
    CAMERA("camera"),
    MIC("mic"),
    SPEAKER("smartspeaker"),
    DISPLAY("display"),
    VIBRATE("vibrate"),
    SENSOR("sensor"),
    NOTIFICATION("notification");
    
    private String profileType;

    private Capability(String profileType2) {
        this.profileType = profileType2;
    }

    public String getProfileDesc() {
        return this.profileType;
    }
}
