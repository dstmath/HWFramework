package ohos.media.camera.zidl;

import java.util.Map;

public interface ICameraServiceStatus {
    public static final int STATUS_AVAILABLE = 1;
    public static final int STATUS_LINKING = 2;
    public static final int STATUS_UNKNOWN = -1;
    public static final int STATUS_UNLINK = 3;
    public static final int STATUS_USING = 0;
    public static final int TORCH_AVAILABLE_STATUS_OFF = 1;
    public static final int TORCH_AVAILABLE_STATUS_ON = 2;
    public static final int TORCH_UNAVAILABLE = 0;
    public static final int TORCH_UNKNOWN = -1;

    void onAvailabilityStatusChanged(String str, int i);

    void onCameraServiceDied();

    void onCameraServiceInitialized(Map<String, Integer> map);

    void onFlashlightStatusChanged(String str, int i);
}
