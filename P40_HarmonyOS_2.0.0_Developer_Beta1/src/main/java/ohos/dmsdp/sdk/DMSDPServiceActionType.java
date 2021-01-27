package ohos.dmsdp.sdk;

public enum DMSDPServiceActionType {
    AUDIO_FOCUS_CHANGE(1),
    CAMERA_ADD_SERVICE(2),
    CAMERA_ADD_SERVICE_STRING(3),
    AUDIO_ADD_SERVICE(4),
    AUDIO_ADD_SERVICE_STRING(5);
    
    private int type;

    private DMSDPServiceActionType(int i) {
        this.type = i;
    }

    public int getType() {
        return this.type;
    }

    public static DMSDPServiceActionType fromType(int i) {
        DMSDPServiceActionType[] values = values();
        for (DMSDPServiceActionType dMSDPServiceActionType : values) {
            if (dMSDPServiceActionType.type == i) {
                return dMSDPServiceActionType;
            }
        }
        return null;
    }
}
