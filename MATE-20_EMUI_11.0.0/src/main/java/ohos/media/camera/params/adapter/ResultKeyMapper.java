package ohos.media.camera.params.adapter;

import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.impl.CameraMetadataNative;
import android.hardware.camera2.params.Face;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import ohos.agp.utils.Rect;
import ohos.media.camera.device.adapter.utils.CameraCoordinateUtil;
import ohos.media.camera.device.adapter.utils.Converter;
import ohos.media.camera.params.ResultKey;
import ohos.media.camera.params.adapter.camera2ex.CaptureResultEx;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class ResultKeyMapper {
    private static final int BOTTOM_COORDINATE_INDEX = 3;
    private static final Map<ResultKey.Key<?>, CaptureResult.Key<?>> KEY_MAPPER;
    private static final int LEFT_COORDINATE_INDEX = 0;
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(ResultKeyMapper.class);
    private static final Map<ResultKey.Key<?>, SpecialMappedKeyMethod> MAPPED_METHOD_FOR_CAPTURE_RESULT;
    private static final int RECTANGULAR_COORDINATES_NUMBER = 4;
    private static final int RIGHT_COORDINATE_INDEX = 2;
    private static final int TOP_COORDINATE_INDEX = 1;

    static {
        HashMap hashMap = new HashMap(1);
        hashMap.put(InnerResultKey.SMART_SUGGEST_HINT, CaptureResultEx.HAUWEI_SMART_SUGGEST_HINT);
        KEY_MAPPER = Collections.unmodifiableMap(hashMap);
        HashMap hashMap2 = new HashMap(5);
        hashMap2.put(InnerResultKey.AF_STATE, new SpecialMappedKeyMethod() {
            /* class ohos.media.camera.params.adapter.ResultKeyMapper.AnonymousClass1 */

            /* access modifiers changed from: package-private */
            @Override // ohos.media.camera.params.adapter.SpecialMappedKeyMethod
            public <T> T getMappedResultKeyValue(CameraMetadataNative cameraMetadataNative, StaticCameraCharacteristics staticCameraCharacteristics) {
                Integer num = (Integer) cameraMetadataNative.get(CaptureResult.CONTROL_AF_STATE);
                if (num == null) {
                    ResultKeyMapper.LOGGER.debug("There is no CONTROL_AF_STATE in CaptureResult", new Object[0]);
                    return null;
                }
                switch (num.intValue()) {
                    case 0:
                        return (T) 0;
                    case 1:
                        return (T) 1;
                    case 2:
                        return (T) 2;
                    case 3:
                        return (T) 4;
                    case 4:
                        return (T) 5;
                    case 5:
                        return (T) 6;
                    case 6:
                        return (T) 3;
                    default:
                        ResultKeyMapper.LOGGER.warn("Unsupported CONTROL_AF_STATE: %{public}d", num);
                        return null;
                }
            }
        });
        hashMap2.put(InnerResultKey.AE_STATE, new SpecialMappedKeyMethod() {
            /* class ohos.media.camera.params.adapter.ResultKeyMapper.AnonymousClass2 */

            /* access modifiers changed from: package-private */
            @Override // ohos.media.camera.params.adapter.SpecialMappedKeyMethod
            public <T> T getMappedResultKeyValue(CameraMetadataNative cameraMetadataNative, StaticCameraCharacteristics staticCameraCharacteristics) {
                Integer num = (Integer) cameraMetadataNative.get(CaptureResult.CONTROL_AE_STATE);
                if (num == null) {
                    ResultKeyMapper.LOGGER.debug("There is no CONTROL_AE_STATE in CaptureResult", new Object[0]);
                    return null;
                }
                int intValue = num.intValue();
                if (intValue == 0) {
                    return (T) 0;
                }
                if (intValue == 1) {
                    return (T) 1;
                }
                if (intValue == 2 || intValue == 3) {
                    return (T) 2;
                }
                if (intValue == 4) {
                    return (T) 3;
                }
                if (intValue == 5) {
                    return (T) 4;
                }
                ResultKeyMapper.LOGGER.error("Unsupported CONTROL_AE_STATE: %{public}d", num);
                return null;
            }
        });
        hashMap2.put(InnerResultKey.FACE_DETECT, new SpecialMappedKeyMethod() {
            /* class ohos.media.camera.params.adapter.ResultKeyMapper.AnonymousClass3 */

            /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: java.util.ArrayList */
            /* JADX WARN: Multi-variable type inference failed */
            /* access modifiers changed from: package-private */
            @Override // ohos.media.camera.params.adapter.SpecialMappedKeyMethod
            public <T> T getMappedResultKeyValue(CameraMetadataNative cameraMetadataNative, StaticCameraCharacteristics staticCameraCharacteristics) {
                Face[] faceArr = (Face[]) cameraMetadataNative.get(CaptureResult.STATISTICS_FACES);
                int[] iArr = null;
                if (faceArr == null) {
                    ResultKeyMapper.LOGGER.debug("There is no STATISTICS_FACES in CaptureResult", new Object[0]);
                    return null;
                }
                ArrayList arrayList = new ArrayList(faceArr.length);
                for (Face face : faceArr) {
                    if (face == null) {
                        ResultKeyMapper.LOGGER.debug("There is no face result in CaptureResult", new Object[0]);
                    } else {
                        arrayList.add(Converter.convertFaceData(face));
                    }
                }
                if (arrayList.isEmpty()) {
                    ResultKeyMapper.LOGGER.debug("faceResultList is empty, return null", new Object[0]);
                    return null;
                }
                ohos.media.camera.params.Face[] faceArr2 = (ohos.media.camera.params.Face[]) arrayList.toArray(new ohos.media.camera.params.Face[0]);
                try {
                    iArr = (int[]) cameraMetadataNative.get(CaptureResultEx.HUAWEI_FACE_RECTS);
                } catch (IllegalArgumentException unused) {
                    ResultKeyMapper.LOGGER.error("Get HUAWEI_FACE_RECTS result failed", new Object[0]);
                }
                if (iArr == null || iArr.length == 0) {
                    ResultKeyMapper.LOGGER.debug("Get HUAWEI_FACE_RECTS result is null or empty, no need to transform coordinate", new Object[0]);
                    return (T) faceDataCoordinateConvert(cameraMetadataNative, staticCameraCharacteristics, faceArr2);
                }
                if (faceArr2.length == iArr.length / 4) {
                    for (int i = 0; i < faceArr2.length; i++) {
                        int i2 = i * 4;
                        Rect rect = new Rect(iArr[i2 + 0], iArr[i2 + 1], iArr[i2 + 2], iArr[i2 + 3]);
                        faceArr2[i].getFaceRect().set(rect.left, rect.top, rect.right, rect.bottom);
                    }
                }
                return (T) faceDataCoordinateConvert(cameraMetadataNative, staticCameraCharacteristics, faceArr2);
            }

            private ohos.media.camera.params.Face[] faceDataCoordinateConvert(CameraMetadataNative cameraMetadataNative, StaticCameraCharacteristics staticCameraCharacteristics, ohos.media.camera.params.Face[] faceArr) {
                Rect convert2ZRect = Converter.convert2ZRect((android.graphics.Rect) cameraMetadataNative.get(CaptureResult.SCALER_CROP_REGION));
                for (ohos.media.camera.params.Face face : faceArr) {
                    Optional<Rect> driverToScreen = CameraCoordinateUtil.driverToScreen(face.getFaceRect(), Converter.convert2ZRect((android.graphics.Rect) staticCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)), convert2ZRect);
                    if (!driverToScreen.isPresent()) {
                        ResultKeyMapper.LOGGER.debug("Coordinate convert face rect is null", new Object[0]);
                        return null;
                    }
                    Rect rect = driverToScreen.get();
                    face.getFaceRect().set(rect.left, rect.top, rect.right, rect.bottom);
                }
                ohos.media.camera.params.Face[] faceArr2 = new ohos.media.camera.params.Face[faceArr.length];
                System.arraycopy(faceArr, 0, faceArr2, 0, faceArr.length);
                return faceArr2;
            }
        });
        hashMap2.put(InnerResultKey.FACE_SMILE_SCORE, new SpecialMappedKeyMethod() {
            /* class ohos.media.camera.params.adapter.ResultKeyMapper.AnonymousClass4 */

            /* access modifiers changed from: package-private */
            @Override // ohos.media.camera.params.adapter.SpecialMappedKeyMethod
            public <T> T getMappedResultKeyValue(CameraMetadataNative cameraMetadataNative, StaticCameraCharacteristics staticCameraCharacteristics) {
                T t = (T) ((int[]) cameraMetadataNative.get(CaptureResultEx.HUAWEI_FACE_INFOS));
                if (t != null) {
                    return t;
                }
                ResultKeyMapper.LOGGER.debug("There is no HUAWEI_FACE_INFOS in CaptureResult", new Object[0]);
                return null;
            }
        });
        hashMap2.put(ResultKey.VIDEO_STABILIZATION_STATE, new SpecialMappedKeyMethod() {
            /* class ohos.media.camera.params.adapter.ResultKeyMapper.AnonymousClass5 */

            /* access modifiers changed from: package-private */
            @Override // ohos.media.camera.params.adapter.SpecialMappedKeyMethod
            public <T> T getMappedResultKeyValue(CameraMetadataNative cameraMetadataNative, StaticCameraCharacteristics staticCameraCharacteristics) {
                Integer num = (Integer) cameraMetadataNative.get(CaptureResult.CONTROL_VIDEO_STABILIZATION_MODE);
                if (num == null) {
                    ResultKeyMapper.LOGGER.debug("There is no CONTROL_VIDEO_STABILIZATION_MODE in CaptureResult", new Object[0]);
                    return null;
                }
                boolean z = num.intValue() == 1;
                ResultKeyMapper.LOGGER.debug("Video Stabilization state %{public}b", Boolean.valueOf(z));
                return (T) Boolean.valueOf(z);
            }
        });
        MAPPED_METHOD_FOR_CAPTURE_RESULT = Collections.unmodifiableMap(hashMap2);
    }

    public static Map<ResultKey.Key<?>, Object> getResultKeyValues(CameraMetadataNative cameraMetadataNative, List<ResultKey.Key<?>> list, StaticCameraCharacteristics staticCameraCharacteristics) {
        HashMap hashMap = new HashMap(list.size());
        for (ResultKey.Key<?> key : list) {
            try {
                CaptureResult.Key<?> key2 = KEY_MAPPER.get(key);
                if (key2 != null) {
                    Object obj = cameraMetadataNative.get(key2);
                    if (obj != null) {
                        hashMap.put(key, obj);
                    }
                } else {
                    SpecialMappedKeyMethod specialMappedKeyMethod = MAPPED_METHOD_FOR_CAPTURE_RESULT.get(key);
                    if (specialMappedKeyMethod == null) {
                        LOGGER.warn("Get mapped key %{public}s value fails", key.toString());
                    } else {
                        Object mappedResultKeyValue = specialMappedKeyMethod.getMappedResultKeyValue(cameraMetadataNative, staticCameraCharacteristics);
                        if (mappedResultKeyValue != null) {
                            hashMap.put(key, mappedResultKeyValue);
                        }
                    }
                }
            } catch (IllegalArgumentException e) {
                LOGGER.error("Failed to get %{public}s, exception:%{public}s", key.toString(), e.toString());
            }
        }
        return hashMap;
    }
}
