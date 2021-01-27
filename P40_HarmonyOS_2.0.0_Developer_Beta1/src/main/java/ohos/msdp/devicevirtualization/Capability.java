package ohos.msdp.devicevirtualization;

public enum Capability {
    CAMERA("camera"),
    MIC("mic"),
    SPEAKER("smartspeaker"),
    DISPLAY("display"),
    HDMI("hdmi");
    
    private String profileType;

    private Capability(String str) {
        this.profileType = str;
    }

    public String getProfileDesc() {
        return this.profileType;
    }
}
