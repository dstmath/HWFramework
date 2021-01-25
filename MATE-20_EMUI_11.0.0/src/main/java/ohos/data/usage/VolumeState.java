package ohos.data.usage;

public enum VolumeState {
    VOLUME_UNMOUNTED("unmounted"),
    VOLUME_CHECKING("checking"),
    VOLUME_MOUNTED("mounted"),
    VOLUME_UNKNOWN("unknown");
    
    private String description;

    private VolumeState(String str) {
        this.description = str;
    }

    public String getDescription() {
        return this.description;
    }

    public static VolumeState getStatus(int i) {
        if (i == 0) {
            return VOLUME_UNMOUNTED;
        }
        if (i > 0) {
            return VOLUME_MOUNTED;
        }
        return VOLUME_UNKNOWN;
    }
}
