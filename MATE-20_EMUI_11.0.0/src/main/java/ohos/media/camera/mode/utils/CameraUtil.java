package ohos.media.camera.mode.utils;

import java.util.ArrayList;
import java.util.Optional;
import ohos.media.camera.device.impl.CameraAbilityImpl;
import ohos.media.camera.device.impl.CameraInfoImpl;
import ohos.media.camera.params.adapter.InnerPropertyKey;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class CameraUtil {
    private static final int BIG_APERTURE_RESOLUTION_VALID_LEN = 2;
    private static final int INIT_CAPACITY = 16;
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(CameraUtil.class);
    private static final int ROTATION_DEGREE_0 = 0;
    private static final int ROTATION_DEGREE_180 = 180;
    private static final int ROTATION_DEGREE_270 = 270;
    private static final int ROTATION_DEGREE_30 = 30;
    private static final int ROTATION_DEGREE_360 = 360;
    private static final int ROTATION_DEGREE_90 = 90;
    private static final int SENSOR_HDR_RESOLUTION_INTERVAL = 3;
    private static final String STRING_X = "x";

    private CameraUtil() {
    }

    public static boolean isFrontCamera(CameraAbilityImpl cameraAbilityImpl) {
        if (cameraAbilityImpl == null) {
            LOGGER.warn("isFrontCamera cameraAbility == null", new Object[0]);
            return false;
        }
        CameraInfoImpl cameraInfo = CameraManagerHelper.getCameraManager().getCameraInfo(cameraAbilityImpl.getCameraId());
        if (cameraInfo == null) {
            LOGGER.warn("Camera info is null for id: %{public}s", cameraAbilityImpl.getCameraId());
            return false;
        } else if (cameraInfo.getFacingType() == 0) {
            return true;
        } else {
            return false;
        }
    }

    public static Optional<String> getCameraBigApertureSpecificResolution(CameraAbilityImpl cameraAbilityImpl) {
        if (cameraAbilityImpl == null) {
            LOGGER.warn("getCameraBigApertureSpecificResolution cameraAbility == null", new Object[0]);
            return Optional.empty();
        }
        int[] iArr = (int[]) cameraAbilityImpl.getPropertyValue(InnerPropertyKey.BIG_APERTURE_RESOLUTION_SUPPORTED);
        if (iArr == null || iArr.length != 2) {
            return Optional.empty();
        }
        return Optional.of(iArr[0] + STRING_X + iArr[1]);
    }

    public static boolean isCameraPortraitModeSupported(CameraAbilityImpl cameraAbilityImpl) {
        if (cameraAbilityImpl == null) {
            LOGGER.warn("isCameraPortraitModeSupported cameraAbility == null", new Object[0]);
            return false;
        }
        Byte b = (Byte) cameraAbilityImpl.getPropertyValue(InnerPropertyKey.PORTRAIT_MODE_SUPPORTED);
        if (b == null || b.byteValue() != 1) {
            return false;
        }
        return true;
    }

    public static boolean isPortraitMovieSupported(CameraAbilityImpl cameraAbilityImpl) {
        if (cameraAbilityImpl == null) {
            LOGGER.warn("isPortraitMovieSupported,cameraAbility == null", new Object[0]);
            return false;
        }
        Byte b = (Byte) cameraAbilityImpl.getPropertyValue(InnerPropertyKey.PORTRAIT_MOVIE_MODE_SUPPORTED);
        boolean z = b != null && b.byteValue() == 1;
        LOGGER.debug("isPortraitMovieSupported: %{public}b", Boolean.valueOf(z));
        return z;
    }

    public static String[] getSensorHdrSupportConfigurations(CameraAbilityImpl cameraAbilityImpl) {
        ArrayList arrayList;
        if (cameraAbilityImpl == null) {
            LOGGER.warn("SensorHDR getResolutionList cameraAbility == null", new Object[0]);
            return new String[0];
        }
        int[] iArr = (int[]) cameraAbilityImpl.getPropertyValue(InnerPropertyKey.AVAILABLE_VIDEO_SENSOR_HDR_CONFIGURATIONS);
        if (iArr == null || iArr.length <= 0 || iArr.length % 3 != 0) {
            arrayList = null;
        } else {
            arrayList = new ArrayList(16);
            int length = iArr.length / 3;
            for (int i = 0; i < length; i++) {
                int i2 = i * 3;
                int i3 = iArr[i2];
                int i4 = iArr[i2 + 1];
                int i5 = i2 + 2;
                if (i5 < iArr.length) {
                    arrayList.add(convertSizeToString(i3, i4, iArr[i5]));
                }
            }
            LOGGER.debug("SensorHDR ResolutionList=%{public}s", arrayList.toString());
        }
        return arrayList != null ? (String[]) arrayList.toArray(new String[0]) : new String[0];
    }

    public static String convertSizeToString(int i, int i2, int i3) {
        StringBuilder sb = new StringBuilder(16);
        sb.append(i);
        sb.append(STRING_X);
        sb.append(i2);
        if (i3 != 30) {
            sb.append("_");
            sb.append(i3);
        }
        return sb.toString();
    }

    public static String convertSizeToString(int i, int i2) {
        StringBuilder sb = new StringBuilder(16);
        sb.append(i);
        sb.append(STRING_X);
        sb.append(i2);
        return sb.toString();
    }

    public static boolean isBackCamera(CameraAbilityImpl cameraAbilityImpl) {
        if (cameraAbilityImpl == null) {
            LOGGER.warn("SensorHDR cameraAbility == null", new Object[0]);
            return false;
        }
        Integer num = (Integer) cameraAbilityImpl.getPropertyValue(InnerPropertyKey.LENS_FACING);
        if (num == null) {
            LOGGER.warn("InnerPropertyKey.LENS_FACING return null", new Object[0]);
            return false;
        } else if (num.intValue() == 1) {
            return true;
        } else {
            return false;
        }
    }

    public static int getImageRotation(int i, int i2) {
        int i3;
        LOGGER.debug("getImageRotation: mSensorOrientation = %{public}d", Integer.valueOf(i));
        if (i2 <= 330 && i2 >= 30) {
            if (i2 > 60 && i2 < 120) {
                i3 = 90;
                int i4 = (i3 + i) % ROTATION_DEGREE_360;
                LOGGER.debug("getImageRotation: imageRotation = %{public}d", Integer.valueOf(i4));
                return i4;
            } else if (i2 > 150 && i2 < 210) {
                i3 = 180;
                int i42 = (i3 + i) % ROTATION_DEGREE_360;
                LOGGER.debug("getImageRotation: imageRotation = %{public}d", Integer.valueOf(i42));
                return i42;
            } else if (i2 <= 240 || i2 >= 300) {
                LOGGER.debug("getImageRotation go to the last else branch", new Object[0]);
            } else {
                i3 = 270;
                int i422 = (i3 + i) % ROTATION_DEGREE_360;
                LOGGER.debug("getImageRotation: imageRotation = %{public}d", Integer.valueOf(i422));
                return i422;
            }
        }
        i3 = 0;
        int i4222 = (i3 + i) % ROTATION_DEGREE_360;
        LOGGER.debug("getImageRotation: imageRotation = %{public}d", Integer.valueOf(i4222));
        return i4222;
    }

    public static boolean isCameraAutoFocusSupported(CameraAbilityImpl cameraAbilityImpl) {
        int[] supportedAfMode;
        if (cameraAbilityImpl == null || (supportedAfMode = cameraAbilityImpl.getSupportedAfMode()) == null) {
            return false;
        }
        ArrayList arrayList = new ArrayList(supportedAfMode.length);
        for (int i : supportedAfMode) {
            arrayList.add(Integer.valueOf(i));
        }
        if (!arrayList.contains(1) || !arrayList.contains(2)) {
            return false;
        }
        return true;
    }

    public static boolean isTriggerLockSupported(CameraAbilityImpl cameraAbilityImpl) {
        Byte b;
        return (cameraAbilityImpl == null || (b = (Byte) cameraAbilityImpl.getPropertyValue(InnerPropertyKey.AF_TRIGGER_LOCK_SUPPORTED)) == null || b.byteValue() != 1) ? false : true;
    }

    public static boolean isDualBothSupported(CameraAbilityImpl cameraAbilityImpl) {
        return isDualCameraSupported(cameraAbilityImpl) && isDualCameraSupportDualStream(cameraAbilityImpl);
    }

    private static boolean isDualCameraSupported(CameraAbilityImpl cameraAbilityImpl) {
        if (cameraAbilityImpl == null) {
            LOGGER.warn("isDualCameraSupported cameraAbility == null", new Object[0]);
            return false;
        }
        byte[] bArr = (byte[]) cameraAbilityImpl.getPropertyValue(InnerPropertyKey.AVAILABLE_DUAL_PRIMARY);
        if (bArr == null) {
            return false;
        }
        for (byte b : bArr) {
            if (b == 3 || b == 0) {
                return true;
            }
        }
        return false;
    }

    private static boolean isDualCameraSupportDualStream(CameraAbilityImpl cameraAbilityImpl) {
        if (cameraAbilityImpl == null) {
            LOGGER.warn("isDualCameraSupportDualStream cameraAbility == null", new Object[0]);
            return false;
        }
        Byte b = (Byte) cameraAbilityImpl.getPropertyValue(InnerPropertyKey.DUAL_PRIMARY_SINGLE_REPROCESS);
        if (b == null || b.byteValue() != 1) {
            return true;
        }
        return false;
    }
}
