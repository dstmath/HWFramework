package ohos.dmsdp.sdk.camera;

public enum CameraExtInfoKey {
    CAMERA_LOCATION("CAMERA_LOCATION"),
    CAMERA_DESC("CAMERA_DESC");
    
    private String key;

    private CameraExtInfoKey(String str) {
        this.key = str;
    }

    public String getKey() {
        return this.key;
    }

    public static boolean isValid(String str) {
        for (CameraExtInfoKey cameraExtInfoKey : values()) {
            if (cameraExtInfoKey.getKey().equals(str)) {
                return true;
            }
        }
        return false;
    }

    public static CameraExtInfoKey valueFrom(String str) {
        CameraExtInfoKey[] values = values();
        for (CameraExtInfoKey cameraExtInfoKey : values) {
            if (cameraExtInfoKey.getKey().equals(str)) {
                return cameraExtInfoKey;
            }
        }
        return null;
    }
}
