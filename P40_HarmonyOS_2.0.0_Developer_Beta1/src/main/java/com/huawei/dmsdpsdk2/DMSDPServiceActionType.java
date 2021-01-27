package com.huawei.dmsdpsdk2;

public enum DMSDPServiceActionType {
    AUDIO_FOCUS_CHANGE(1),
    CAMERA_ADD_SERVICE(2),
    CAMERA_ADD_SERVICE_STRING(3),
    AUDIO_ADD_SERVICE(4),
    AUDIO_ADD_SERVICE_STRING(5);
    
    private int type;

    private DMSDPServiceActionType(int type2) {
        this.type = type2;
    }

    public int getType() {
        return this.type;
    }

    public static DMSDPServiceActionType fromType(int type2) {
        DMSDPServiceActionType[] values = values();
        for (DMSDPServiceActionType actionType : values) {
            if (actionType.type == type2) {
                return actionType;
            }
        }
        return null;
    }
}
