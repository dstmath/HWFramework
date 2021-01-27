package android.hardware.camera2;

import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.params.StreamConfiguration;
import android.hardware.camera2.params.StreamConfigurationDuration;

public final class UsbCameraCharacteristics {
    public static final CameraCharacteristics.Key<StreamConfigurationDuration[]> H264_AVAILABLE_H264_MIN_FRAME_DURATIONS = new CameraCharacteristics.Key<>("com.huawei.device.capabilities.h264.availableMinFrameDurations", StreamConfigurationDuration[].class);
    public static final CameraCharacteristics.Key<StreamConfigurationDuration[]> H264_AVAILABLE_H264_STALL_DURATIONS = new CameraCharacteristics.Key<>("com.huawei.device.capabilities.h264.availableStallDurations", StreamConfigurationDuration[].class);
    public static final CameraCharacteristics.Key<StreamConfiguration[]> H264_AVAILABLE_H264_STREAM_CONFIGURATIONS = new CameraCharacteristics.Key<>("com.huawei.device.capabilities.h264.availableStreamConfigurations", StreamConfiguration[].class);
    public static final CameraCharacteristics.Key<StreamConfigurationDuration[]> H265_AVAILABLE_H265_MIN_FRAME_DURATIONS = new CameraCharacteristics.Key<>("com.huawei.device.capabilities.h265.availableMinFrameDurations", StreamConfigurationDuration[].class);
    public static final CameraCharacteristics.Key<StreamConfigurationDuration[]> H265_AVAILABLE_H265_STALL_DURATIONS = new CameraCharacteristics.Key<>("com.huawei.device.capabilities.h265.availableStallDurations", StreamConfigurationDuration[].class);
    public static final CameraCharacteristics.Key<StreamConfiguration[]> H265_AVAILABLE_H265_STREAM_CONFIGURATIONS = new CameraCharacteristics.Key<>("com.huawei.device.capabilities.h265.availableStreamConfigurations", StreamConfiguration[].class);
}
