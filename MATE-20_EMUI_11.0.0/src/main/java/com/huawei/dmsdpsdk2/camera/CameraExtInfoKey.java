package com.huawei.dmsdpsdk2.camera;

public enum CameraExtInfoKey {
    CAMERA_LOCATION("CAMERA_LOCATION"),
    CAMERA_DESC("CAMERA_DESC");
    
    private String key;

    private CameraExtInfoKey(String key2) {
        this.key = key2;
    }

    public String getKey() {
        return this.key;
    }

    public static boolean isValid(String s) {
        for (CameraExtInfoKey c : values()) {
            if (c.getKey().equals(s)) {
                return true;
            }
        }
        return false;
    }

    public static CameraExtInfoKey valueFrom(String s) {
        CameraExtInfoKey[] values = values();
        for (CameraExtInfoKey cameraExtInfoKey : values) {
            if (cameraExtInfoKey.getKey().equals(s)) {
                return cameraExtInfoKey;
            }
        }
        return null;
    }
}
