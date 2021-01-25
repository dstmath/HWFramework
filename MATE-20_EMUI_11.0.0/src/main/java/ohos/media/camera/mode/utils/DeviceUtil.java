package ohos.media.camera.mode.utils;

import ohos.app.Context;
import ohos.media.camera.mode.adapter.utils.ScreenUtils;
import ohos.media.image.common.Size;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class DeviceUtil {
    private static final double BYTE_TO_GB_UNIT = 1.073741824E9d;
    private static final double DEFAULT_MAX_SCREENRATIO = 2.0d;
    private static final double DOUBLE_DEVIATION = 0.05d;
    public static final int HISI_PLATFORM = 0;
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(DeviceUtil.class);
    public static final int MTK_PLATFORM = 2;
    public static final int QCOM_PLATFORM = 1;
    private static final double RAM_2G = 2.5d;
    private static final double TOTAL_MEM_STUB = 100.0d;
    private static Context ctx;
    private static double maxScreenRatio;
    private static Size screenSize;
    private static double totalMem = TOTAL_MEM_STUB;

    public static int getPlatformType() {
        return 0;
    }

    private DeviceUtil() {
    }

    public static void initialize(Context context) {
        if (context != null) {
            ctx = context;
            setScreenSize(context);
            setMaxScreenRatio(context);
        }
    }

    public static Context getContext() {
        return ctx;
    }

    public static double getMaxScreenRatio() {
        LOGGER.debug("getMaxScreenRatio: = %{public}f", Double.valueOf(maxScreenRatio));
        return maxScreenRatio;
    }

    public static void setMaxScreenRatio(Context context) {
        if (context != null) {
            Size size = screenSize;
            if (size == null || size.width == 0) {
                maxScreenRatio = DEFAULT_MAX_SCREENRATIO;
            } else {
                maxScreenRatio = ((double) screenSize.height) / ((double) screenSize.width);
            }
        }
    }

    public static Size getScreenSize() {
        LOGGER.debug("getScreenSize:= %{public}s", screenSize);
        return screenSize;
    }

    public static void setScreenSize(Context context) {
        screenSize = ScreenUtils.getRealScreenSize(context);
        LOGGER.debug("initialize screen size = %{public}s", screenSize);
    }
}
