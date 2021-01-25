package ohos.media.camera.device.adapter;

import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.impl.CameraMetadataNative;
import android.hardware.camera2.params.MeteringRectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import ohos.agp.graphics.Surface;
import ohos.agp.utils.Rect;
import ohos.location.Location;
import ohos.media.camera.device.adapter.utils.CameraCoordinateUtil;
import ohos.media.camera.device.adapter.utils.Converter;
import ohos.media.camera.device.adapter.utils.SurfaceUtils;
import ohos.media.camera.params.adapter.InnerParameterKey;
import ohos.media.camera.params.adapter.ParameterKeyMapper;
import ohos.media.camera.params.adapter.StaticCameraCharacteristics;
import ohos.media.camera.params.adapter.camera2ex.CaptureRequestEx;
import ohos.media.camera.zidl.FrameConfigNative;
import ohos.media.image.common.Size;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class FrameConfigMapper {
    private static final float DEFAULT_ZOOM_RATIO = 1.0f;
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(FrameConfigMapper.class);
    private static final int REPROCESSABLE_SESSION_ID = -1;
    private final Map<Integer, CameraMetadataNative> defaultSettings = new HashMap();
    private final String logicalCameraId;
    private final Float maxZoomValue;
    private final Rect sensorArray;

    public FrameConfigMapper(String str, StaticCameraCharacteristics staticCameraCharacteristics) {
        this.logicalCameraId = str;
        this.sensorArray = Converter.convert2ZRect((android.graphics.Rect) staticCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE));
        this.maxZoomValue = (Float) staticCameraCharacteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM);
    }

    public void addDefaultMetadataSetting(int i, CameraMetadataNative cameraMetadataNative) {
        this.defaultSettings.put(Integer.valueOf(i), cameraMetadataNative);
    }

    public CaptureRequest[] convert2CaptureRequests(List<FrameConfigNative> list) {
        ArrayList arrayList = new ArrayList();
        for (FrameConfigNative frameConfigNative : list) {
            int frameConfigType = frameConfigNative.getFrameConfigType();
            CameraMetadataNative cameraMetadataNative = this.defaultSettings.get(Integer.valueOf(frameConfigType));
            if (cameraMetadataNative == null) {
                LOGGER.warn("Failed to get default settings for template %{public}d", Integer.valueOf(frameConfigType));
            } else {
                CameraMetadataNative cameraRequestMetadatas = ParameterKeyMapper.getCameraRequestMetadatas(frameConfigNative.getConfigParameters(), cameraMetadataNative);
                setExtraParameters(cameraRequestMetadatas, frameConfigNative);
                CaptureRequest.Builder builder = new CaptureRequest.Builder(cameraRequestMetadatas, false, -1, this.logicalCameraId, null);
                for (Surface surface : frameConfigNative.getSurfaces()) {
                    builder.addTarget(Converter.convert2ASurface(surface));
                }
                arrayList.add(builder.build());
            }
        }
        return (CaptureRequest[]) arrayList.toArray(new CaptureRequest[0]);
    }

    private void setExtraParameters(CameraMetadataNative cameraMetadataNative, FrameConfigNative frameConfigNative) {
        setFlashMode(cameraMetadataNative, frameConfigNative);
        setAfParameters(cameraMetadataNative, frameConfigNative);
        setAeParameters(cameraMetadataNative, frameConfigNative);
        setZoomParameters(cameraMetadataNative, frameConfigNative);
        setFaceDetectionParameters(cameraMetadataNative, frameConfigNative);
        setImageRotation(cameraMetadataNative, frameConfigNative);
        setLocation(cameraMetadataNative, frameConfigNative);
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x0038  */
    /* JADX WARNING: Removed duplicated region for block: B:18:? A[RETURN, SYNTHETIC] */
    private void setFlashMode(CameraMetadataNative cameraMetadataNative, FrameConfigNative frameConfigNative) {
        int i;
        Integer num = (Integer) frameConfigNative.get(InnerParameterKey.FLASH_MODE);
        if (num != null) {
            int intValue = num.intValue();
            if (intValue != 0) {
                if (intValue == 1) {
                    i = 0;
                } else if (intValue != 2) {
                    if (intValue != 3) {
                        LOGGER.warn("Unsupported FLASH_MODE: %{public}d", num);
                    } else {
                        i = 2;
                    }
                }
                if (i != null) {
                    cameraMetadataNative.set(CaptureRequest.FLASH_MODE, i);
                    return;
                }
                return;
            }
            i = 1;
            if (i != null) {
            }
        }
        i = null;
        if (i != null) {
        }
    }

    private void setAfParameters(CameraMetadataNative cameraMetadataNative, FrameConfigNative frameConfigNative) {
        setAfMode(cameraMetadataNative, frameConfigNative);
        setAfRegion(cameraMetadataNative, frameConfigNative);
        setAfTrigger(cameraMetadataNative, frameConfigNative);
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x003b  */
    /* JADX WARNING: Removed duplicated region for block: B:16:? A[RETURN, SYNTHETIC] */
    private void setAfMode(CameraMetadataNative cameraMetadataNative, FrameConfigNative frameConfigNative) {
        int i;
        Integer num = (Integer) frameConfigNative.get(InnerParameterKey.AF_MODE);
        if (num != null) {
            int intValue = num.intValue();
            if (intValue != 1) {
                if (intValue != 2) {
                    LOGGER.warn("Unsupported AF_MODE: %{public}d", num);
                } else {
                    i = 1;
                }
            } else if (frameConfigNative.getFrameConfigType() == 3) {
                i = 3;
            } else {
                i = 4;
            }
            if (i == null) {
                cameraMetadataNative.set(CaptureRequest.CONTROL_AF_MODE, i);
                return;
            }
            return;
        }
        i = null;
        if (i == null) {
        }
    }

    private void setAfRegion(CameraMetadataNative cameraMetadataNative, FrameConfigNative frameConfigNative) {
        Size surfaceSize = SurfaceUtils.getSurfaceSize(frameConfigNative.getCoordinateSurface());
        LOGGER.debug("setAfRegion coordinate surface size: %{public}s", surfaceSize);
        cameraMetadataNative.set(CaptureRequest.CONTROL_AF_REGIONS, new MeteringRectangle[]{getTransformRect((Rect) frameConfigNative.get(InnerParameterKey.AF_REGION), surfaceSize, get3ACropRegion(frameConfigNative).orElse(null))});
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x0035  */
    /* JADX WARNING: Removed duplicated region for block: B:16:? A[RETURN, SYNTHETIC] */
    private void setAfTrigger(CameraMetadataNative cameraMetadataNative, FrameConfigNative frameConfigNative) {
        int i;
        Integer num = (Integer) frameConfigNative.get(InnerParameterKey.AF_TRIGGER);
        if (num != null) {
            int intValue = num.intValue();
            if (intValue == 0) {
                i = 0;
            } else if (intValue == 1) {
                i = 1;
            } else if (intValue != 2) {
                LOGGER.warn("Unsupported AF_TRIGGER: %{public}d", num);
            } else {
                i = 2;
            }
            if (i == null) {
                cameraMetadataNative.set(CaptureRequest.CONTROL_AF_TRIGGER, i);
                return;
            }
            return;
        }
        i = null;
        if (i == null) {
        }
    }

    private void setAeParameters(CameraMetadataNative cameraMetadataNative, FrameConfigNative frameConfigNative) {
        setAeMode(cameraMetadataNative, frameConfigNative);
        setAeRegion(cameraMetadataNative, frameConfigNative);
        setAeTrigger(cameraMetadataNative, frameConfigNative);
    }

    /* JADX WARNING: Removed duplicated region for block: B:18:0x0050  */
    /* JADX WARNING: Removed duplicated region for block: B:20:? A[RETURN, SYNTHETIC] */
    private void setAeMode(CameraMetadataNative cameraMetadataNative, FrameConfigNative frameConfigNative) {
        Integer num = (Integer) frameConfigNative.get(InnerParameterKey.FLASH_MODE);
        int i = 1;
        if (num == null) {
            num = 1;
        }
        Integer num2 = (Integer) frameConfigNative.get(InnerParameterKey.AE_MODE);
        if (num2 != null) {
            int intValue = num2.intValue();
            if (intValue == 0) {
                i = 0;
            } else if (intValue != 1) {
                LOGGER.warn("Unsupported AE_MODE: %{public}d", num2);
            } else if (num.intValue() == 0) {
                i = 2;
            } else if (num.intValue() == 2) {
                i = 3;
            }
            if (i == null) {
                cameraMetadataNative.set(CaptureRequest.CONTROL_AE_MODE, i);
                return;
            }
            return;
        }
        i = null;
        if (i == null) {
        }
    }

    private void setAeRegion(CameraMetadataNative cameraMetadataNative, FrameConfigNative frameConfigNative) {
        Size surfaceSize = SurfaceUtils.getSurfaceSize(frameConfigNative.getCoordinateSurface());
        LOGGER.debug("setAeRegion coordinate surface size: %{public}s", surfaceSize);
        cameraMetadataNative.set(CaptureRequest.CONTROL_AE_REGIONS, new MeteringRectangle[]{getTransformRect((Rect) frameConfigNative.get(InnerParameterKey.AE_REGION), surfaceSize, get3ACropRegion(frameConfigNative).orElse(null))});
    }

    private void setAeTrigger(CameraMetadataNative cameraMetadataNative, FrameConfigNative frameConfigNative) {
        Integer convertAeTrigger;
        Integer num = (Integer) frameConfigNative.get(InnerParameterKey.AE_TRIGGER);
        if (num != null && (convertAeTrigger = getConvertAeTrigger(num.intValue())) != null) {
            cameraMetadataNative.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, convertAeTrigger);
        }
    }

    private Integer getConvertAeTrigger(int i) {
        if (i == 0) {
            return 0;
        }
        if (i == 1) {
            return 1;
        }
        if (i == 2) {
            return 2;
        }
        LOGGER.warn("Unsupported AE_TRIGGER: %{public}d", Integer.valueOf(i));
        return null;
    }

    private Optional<Rect> get3ACropRegion(FrameConfigNative frameConfigNative) {
        Optional<Rect> empty = Optional.empty();
        if (this.maxZoomValue == null) {
            return empty;
        }
        Float f = (Float) frameConfigNative.get(InnerParameterKey.ZOOM_RATIO);
        if (f == null) {
            f = Float.valueOf(1.0f);
        }
        return CameraCoordinateUtil.getCropRegion(this.sensorArray, this.maxZoomValue.floatValue(), f.floatValue());
    }

    private void setFaceDetectionParameters(CameraMetadataNative cameraMetadataNative, FrameConfigNative frameConfigNative) {
        byte b;
        Integer num = (Integer) frameConfigNative.get(InnerParameterKey.FACE_DETECTION_TYPE);
        if (num == null) {
            LOGGER.debug("setFaceDetectionParameters failed for null FACE_DETECTION_TYPE key value", new Object[0]);
            return;
        }
        int i = 2;
        if ((num.intValue() & 2) != 0) {
            b = 1;
        } else if ((num.intValue() & 1) != 0) {
            b = 0;
        } else {
            b = 0;
            i = 0;
        }
        cameraMetadataNative.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, Integer.valueOf(i));
        try {
            cameraMetadataNative.set(CaptureRequestEx.HUAWEI_SMILE_DETECTION, Byte.valueOf(b));
        } catch (IllegalArgumentException e) {
            LOGGER.debug("Failed to set HUAWEI_SMILE_DETECTION to metadata, exception: %{public}s", e.getMessage());
        }
    }

    private void setZoomParameters(CameraMetadataNative cameraMetadataNative, FrameConfigNative frameConfigNative) {
        Optional<Rect> empty = Optional.empty();
        if (this.maxZoomValue != null) {
            Float f = (Float) frameConfigNative.get(InnerParameterKey.ZOOM_RATIO);
            if (f == null) {
                LOGGER.debug("setZoomParameters failed for null ZOOM_RATIO key value", new Object[0]);
                return;
            }
            empty = CameraCoordinateUtil.getCropRegion(this.sensorArray, this.maxZoomValue.floatValue(), f.floatValue());
        }
        if (!empty.isPresent()) {
            LOGGER.warn("setZoomParameters failed, get cropRegion is null", new Object[0]);
            return;
        }
        Rect rect = empty.get();
        LOGGER.debug("setZoomParameters cropRegion is %{public}s.", rect.toString());
        cameraMetadataNative.set(CaptureRequest.SCALER_CROP_REGION, Converter.convert2ARect(rect));
    }

    private void setImageRotation(CameraMetadataNative cameraMetadataNative, FrameConfigNative frameConfigNative) {
        Integer num = (Integer) frameConfigNative.get(InnerParameterKey.IMAGE_ROTATION);
        if (num != null) {
            cameraMetadataNative.set(CaptureRequest.JPEG_ORIENTATION, num);
        }
    }

    private void setLocation(CameraMetadataNative cameraMetadataNative, FrameConfigNative frameConfigNative) {
        Location location = (Location) frameConfigNative.get(InnerParameterKey.LOCATION);
        if (location != null) {
            cameraMetadataNative.set(CaptureRequest.JPEG_GPS_LOCATION, Converter.convert2ALocation(location));
        }
    }

    private MeteringRectangle getTransformRect(Rect rect, Size size, Rect rect2) {
        if (rect != null) {
            return Converter.convert2MeteringRectangles(CameraCoordinateUtil.screenToDriver(rect, size, rect2, this.sensorArray));
        }
        LOGGER.debug("getTransformRects rect is null, return default region", new Object[0]);
        return Converter.convert2MeteringRectangles(null);
    }
}
