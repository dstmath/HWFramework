package ohos.media.camera.mode.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import ohos.app.Context;
import ohos.media.camera.device.CameraManager;
import ohos.media.camera.device.impl.CameraAbilityImpl;
import ohos.media.camera.device.impl.CameraInfoImpl;
import ohos.media.camera.params.adapter.InnerPropertyKey;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class CameraManagerHelper {
    private static final String DEFAULT_BACK_PHYSICAL_CAMERA_ID = "0";
    private static final String DEFAULT_FRONT_PHYSICAL_CAMERA_ID = "1";
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(CameraManagerHelper.class);
    private static List<String> backLogicalCameraIds = new ArrayList();
    private static List<String> backMonoCameraIds = new ArrayList();
    private static List<String> backPhysicalCameraIds = new ArrayList();
    private static List<String> backTeleCameraIds = new ArrayList();
    private static List<String> backWideCameraIds = new ArrayList();
    private static CameraManager cameraManager;
    private static List<String> frontLogicalCameraIds = new ArrayList();
    private static List<String> frontMonoCameraIds = new ArrayList();
    private static List<String> frontPhysicalCameraIds = new ArrayList();

    private CameraManagerHelper() {
    }

    public static void initialize(Context context) {
        if (context == null) {
            LOGGER.error("initialize: null context!", new Object[0]);
            return;
        }
        cameraManager = CameraManager.getInstance(context);
        initHelper();
    }

    private static void initHelper() {
        String[] cameraIdList = cameraManager.getCameraIdList();
        if (cameraIdList != null && cameraIdList.length > 0) {
            for (String str : cameraIdList) {
                addPhysicalCamera(str, cameraManager.getCameraInfo(str), cameraManager.getCameraAbility(str));
            }
        }
        LOGGER.end("GlobalCameraManager static init");
    }

    private static void addPhysicalCamera(String str, CameraInfoImpl cameraInfoImpl, CameraAbilityImpl cameraAbilityImpl) {
        List<String> physicalIdList = cameraInfoImpl.getPhysicalIdList();
        if (cameraInfoImpl.getFacingType() == 1) {
            if (!physicalIdList.isEmpty()) {
                LOGGER.info("get camera id. back logical camera : %{public}s, physical camera ids : %{public}s", str, physicalIdList.toString());
                backLogicalCameraIds.add(str);
                return;
            }
            backPhysicalCameraIds.add(str);
            if (isMonochrome(cameraAbilityImpl)) {
                LOGGER.info("get camera id. back physical mono camera : %{public}s", str);
                backMonoCameraIds.add(str);
            } else if (isWideAngle(cameraAbilityImpl)) {
                LOGGER.info("get camera id. back physical wide camera : %{public}s", str);
                backWideCameraIds.add(str);
            } else if (isTeleLens(cameraAbilityImpl)) {
                LOGGER.info("get camera id. back physical tele camera : %{public}s", str);
                backTeleCameraIds.add(str);
            } else {
                LOGGER.info("get camera id. no back special camera", new Object[0]);
            }
        } else if (!physicalIdList.isEmpty()) {
            LOGGER.info("get camera id. front logical camera : %{public}s", str);
            frontLogicalCameraIds.add(str);
        } else {
            LOGGER.info("get camera id. front physical camera : %{public}s", str);
            frontPhysicalCameraIds.add(str);
            if (isMonochrome(cameraAbilityImpl)) {
                LOGGER.info("get camera id. front physical mono camera : %{public}s", str);
                frontMonoCameraIds.add(str);
            } else if (isWideAngle(cameraAbilityImpl)) {
                LOGGER.info("get camera id. front physical wide camera : %{public}s", str);
                backWideCameraIds.add(str);
            } else if (isTeleLens(cameraAbilityImpl)) {
                LOGGER.info("get camera id. front physical tele camera : %{public}s", str);
                backTeleCameraIds.add(str);
            } else {
                LOGGER.info("get camera id. no front special camera", new Object[0]);
            }
        }
    }

    private static boolean isMonochrome(CameraAbilityImpl cameraAbilityImpl) {
        int[] iArr;
        if (!(cameraAbilityImpl == null || (iArr = (int[]) cameraAbilityImpl.getPropertyValue(InnerPropertyKey.AVAILABLE_CAPABILITIES)) == null)) {
            for (int i : iArr) {
                if (i == 12) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isWideAngle(CameraAbilityImpl cameraAbilityImpl) {
        Byte b;
        if (cameraAbilityImpl == null || (b = (Byte) cameraAbilityImpl.getPropertyValue(InnerPropertyKey.WIDE_ANGLE_SUPPORT)) == null || b.byteValue() != 1) {
            return false;
        }
        return true;
    }

    private static boolean isTeleLens(CameraAbilityImpl cameraAbilityImpl) {
        Byte b;
        if (cameraAbilityImpl == null || (b = (Byte) cameraAbilityImpl.getPropertyValue(InnerPropertyKey.TELE_MODE_SUPPORT)) == null || b.byteValue() != 1) {
            return false;
        }
        return true;
    }

    public static boolean isBackCamera(String str) {
        return backLogicalCameraIds.contains(str) || backPhysicalCameraIds.contains(str);
    }

    public static String getBackCameraName() {
        if (!backLogicalCameraIds.isEmpty()) {
            return backLogicalCameraIds.get(0);
        }
        return !backPhysicalCameraIds.isEmpty() ? backPhysicalCameraIds.get(0) : "0";
    }

    public static String getFrontCameraName() {
        if (!frontLogicalCameraIds.isEmpty()) {
            return frontLogicalCameraIds.get(0);
        }
        return !frontPhysicalCameraIds.isEmpty() ? frontPhysicalCameraIds.get(0) : "1";
    }

    public static Optional<String> getWideAngelId() {
        if (!CollectionUtil.isEmptyCollection(backWideCameraIds)) {
            return Optional.ofNullable(backWideCameraIds.get(0));
        }
        return Optional.empty();
    }

    public static Optional<String> getTeleId() {
        if (!CollectionUtil.isEmptyCollection(backTeleCameraIds)) {
            return Optional.ofNullable(backTeleCameraIds.get(0));
        }
        return Optional.empty();
    }

    public static CameraManager getCameraManager() {
        return cameraManager;
    }
}
