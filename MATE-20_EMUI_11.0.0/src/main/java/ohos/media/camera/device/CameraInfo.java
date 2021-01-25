package ohos.media.camera.device;

import java.util.List;

public interface CameraInfo {

    public @interface DeviceLinkType {
        public static final int DEVICE_LINK_EXTERNAL_MSDP = 2;
        public static final int DEVICE_LINK_EXTERNAL_USB = 1;
        public static final int DEVICE_LINK_NATIVE = 0;
        public static final int DEVICE_LINK_OTHERS = -1;
    }

    public @interface FacingType {
        public static final int CAMERA_FACING_BACK = 1;
        public static final int CAMERA_FACING_FRONT = 0;
        public static final int CAMERA_FACING_OTHERS = -1;
    }

    @DeviceLinkType
    int getDeviceLinkType(String str);

    @FacingType
    int getFacingType();

    String getLogicalId();

    List<String> getPhysicalIdList();
}
