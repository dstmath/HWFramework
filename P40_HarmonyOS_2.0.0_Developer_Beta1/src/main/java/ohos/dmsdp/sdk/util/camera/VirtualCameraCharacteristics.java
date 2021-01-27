package ohos.dmsdp.sdk.util.camera;

import android.hardware.camera2.CameraCharacteristics;

public final class VirtualCameraCharacteristics {
    public static final CameraCharacteristics.Key<byte[]> HUAWEI_VIRTUAL_CAMERA_DEVICE_ID = KeyGenerator.generateCharacteristicsKey("com.huawei.virtualcamera.metadata.virtrualcamera-deviceId", byte[].class);
    public static final CameraCharacteristics.Key<byte[]> HUAWEI_VIRTUAL_CAMERA_DEVICE_ID_EXT = KeyGenerator.generateCharacteristicsKey("com.huawei.virtualcamera.metadata.virtrualcamera-deviceId-ext", byte[].class);
    public static final CameraCharacteristics.Key<byte[]> HUAWEI_VIRTUAL_CAMERA_LOCATION = KeyGenerator.generateCharacteristicsKey("com.huawei.virtualcamera.metadata.virtrualcamera-location", byte[].class);
    public static final CameraCharacteristics.Key<byte[]> HUAWEI_VIRTUAL_CAMERA_SERVICE_ID = KeyGenerator.generateCharacteristicsKey("com.huawei.virtualcamera.metadata.virtrualcamera-serviceId", byte[].class);
    public static final CameraCharacteristics.Key<Integer> HUAWEI_VIRTUAL_CAMERA_TYPE = KeyGenerator.generateCharacteristicsKey("com.huawei.virtualcamera.metadata.virtrualcamera-type", Integer.TYPE);
}
