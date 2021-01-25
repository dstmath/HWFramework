package ohos.media.camera.mode.utils;

import java.util.Objects;
import ohos.app.Context;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class KitUtil {
    private static final String HUAWEI_CAMERAKIT_APK_PACKAGE_NAME = "ohos.media.camera.mode";
    private static final String HUAWEI_CAMERA_APK_PACKAGE_NAME = "com.huawei.camera";
    public static final int HW_CAMERA = 0;
    public static final int INVALID = -1;
    public static final long INVALID_VERSIONCODE = -1;
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(KitUtil.class);
    public static final int THIRD_APP = 1;
    private static int appType = -1;
    private static Context context;
    private static long kitVersionCode;
    private static String kitVersionName;
    private static String packageName;
    private static int[] superSlowMotionRamStatus;

    private KitUtil() {
    }

    public static void initialize(Context context2) {
        if (context2 == null) {
            LOGGER.warn("initialize: context=null!", new Object[0]);
            return;
        }
        context = context2;
        LOGGER.info("appType = %{public}d", Integer.valueOf(appType));
    }

    public static Context getContext() {
        Context context2 = context;
        if (context2 != null) {
            return context2;
        }
        LOGGER.error("getContext: mContext=null!", new Object[0]);
        return null;
    }

    public static boolean isHwApp() {
        return appType == 0;
    }

    public static boolean hasPermission(String str) {
        Objects.requireNonNull(str, "permission should not be null");
        return context.verifySelfPermission(str) == 0;
    }
}
