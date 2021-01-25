package ohos.media.camera.params.adapter;

import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.media.MediaCodec;
import android.media.MediaRecorder;
import android.view.SurfaceHolder;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import ohos.agp.graphics.SurfaceOps;
import ohos.media.camera.device.adapter.utils.Converter;
import ohos.media.camera.zidl.StreamConfigAbility;
import ohos.media.image.ImageReceiver;
import ohos.media.image.common.Size;
import ohos.media.recorder.Recorder;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

/* access modifiers changed from: package-private */
public final class StreamConfigAbilityMapper {
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(StreamConfigAbilityMapper.class);
    private static final Map<Integer, Integer> OUTPUT_FORMAT_MAP;
    private static final List<Class<?>> SUPPORTED_A_SURFACE_CLASSES = Arrays.asList(SurfaceHolder.class, SurfaceTexture.class, ImageReader.class, MediaRecorder.class, MediaCodec.class);
    private static final Map<Class<?>, Class<?>> SURFACE_CLASS_MAP;
    private final CameraCharacteristics cameraCharacteristics;

    static {
        HashMap hashMap = new HashMap(3);
        hashMap.put(SurfaceHolder.class, SurfaceOps.class);
        hashMap.put(ImageReader.class, ImageReceiver.class);
        hashMap.put(MediaRecorder.class, Recorder.class);
        SURFACE_CLASS_MAP = Collections.unmodifiableMap(hashMap);
        HashMap hashMap2 = new HashMap(6);
        hashMap2.put(0, 0);
        hashMap2.put(17, 1);
        hashMap2.put(35, 2);
        hashMap2.put(256, 3);
        hashMap2.put(37, 4);
        hashMap2.put(32, 5);
        OUTPUT_FORMAT_MAP = Collections.unmodifiableMap(hashMap2);
    }

    StreamConfigAbilityMapper(CameraCharacteristics cameraCharacteristics2) {
        this.cameraCharacteristics = cameraCharacteristics2;
    }

    /* access modifiers changed from: package-private */
    public Optional<StreamConfigAbility> getStreamConfigAbility() {
        StreamConfigurationMap streamConfigurationMap = (StreamConfigurationMap) this.cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        if (streamConfigurationMap == null) {
            LOGGER.warn("There is no SCALER_STREAM_CONFIGURATION_MAP in ASOP CameraCharacteristics", new Object[0]);
            return Optional.empty();
        }
        Map<Class<?>, List<Size>> supportOutputSizesForAllCLass = getSupportOutputSizesForAllCLass(streamConfigurationMap);
        Map<Integer, List<Size>> supportOutputSizesForAllFormat = getSupportOutputSizesForAllFormat(streamConfigurationMap, streamConfigurationMap.getOutputFormats());
        if (!supportOutputSizesForAllCLass.isEmpty() || !supportOutputSizesForAllFormat.isEmpty()) {
            return Optional.of(new StreamConfigAbility(supportOutputSizesForAllCLass, supportOutputSizesForAllFormat));
        }
        LOGGER.error("There is no output sizes for supported classes and formats", new Object[0]);
        return Optional.empty();
    }

    private Map<Class<?>, List<Size>> getSupportOutputSizesForAllCLass(StreamConfigurationMap streamConfigurationMap) {
        HashMap hashMap = new HashMap(SUPPORTED_A_SURFACE_CLASSES.size());
        for (Class<?> cls : SUPPORTED_A_SURFACE_CLASSES) {
            android.util.Size[] outputSizes = streamConfigurationMap.getOutputSizes(cls);
            if (outputSizes != null && outputSizes.length > 0) {
                hashMap.put(SURFACE_CLASS_MAP.get(cls), Converter.convertSizes(outputSizes));
            }
        }
        return hashMap;
    }

    private Map<Integer, List<Size>> getSupportOutputSizesForAllFormat(StreamConfigurationMap streamConfigurationMap, int[] iArr) {
        HashMap hashMap = new HashMap(iArr.length);
        for (int i : iArr) {
            android.util.Size[] outputSizes = streamConfigurationMap.getOutputSizes(i);
            if (outputSizes != null && outputSizes.length > 0) {
                hashMap.put(OUTPUT_FORMAT_MAP.get(Integer.valueOf(i)), Converter.convertSizes(outputSizes));
            }
        }
        return hashMap;
    }
}
