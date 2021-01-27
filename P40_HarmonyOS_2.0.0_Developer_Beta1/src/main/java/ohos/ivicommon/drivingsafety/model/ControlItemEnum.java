package ohos.ivicommon.drivingsafety.model;

public enum ControlItemEnum {
    SETTINGS("driving_mode_settings"),
    PHONE("driving_mode_phone"),
    SYSTEMUI("driving_mode_systemUI"),
    CONTACT("driving_mode_contact"),
    AUTO_RUN("driving_mode_autoRun"),
    REMOTE_CONTROL("driving_mode_remoteControl"),
    UPGRADE("driving_mode_upgrade"),
    VIDEO("driving_mode_video"),
    IME("driving_mode_IME");
    
    private String name;

    private ControlItemEnum(String str) {
        this.name = str;
    }

    public String getName() {
        return this.name;
    }
}
