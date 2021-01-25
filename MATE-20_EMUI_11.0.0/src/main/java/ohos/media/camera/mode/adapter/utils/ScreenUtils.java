package ohos.media.camera.mode.adapter.utils;

import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import ohos.app.Context;
import ohos.media.image.common.Size;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public final class ScreenUtils {
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(ScreenUtils.class);
    private static final int NOTCH_ARRAY_LENGTH = 2;
    private static Method getNotchSizeMethod;

    static {
        getNotchSizeMethod = null;
        try {
            getNotchSizeMethod = Class.forName("com.huawei.android.util.HwNotchSizeUtil").getDeclaredMethod("getNotchSize", new Class[0]);
        } catch (ClassNotFoundException e) {
            LOGGER.debug("getNotchSize: ClassNotFoundException %{public}s", e);
        } catch (NoSuchMethodException e2) {
            LOGGER.debug("getNotchSize: NoSuchMethodException %{public}s", e2);
        }
    }

    private ScreenUtils() {
    }

    public static Size getRealScreenSize(Context context) {
        Size size = new Size(0, 0);
        Object hostContext = context.getHostContext();
        if (!(hostContext instanceof android.content.Context)) {
            LOGGER.debug("getRealScreenSize context instance error, return screen size: %{public}s", size);
            return size;
        }
        android.content.Context context2 = (android.content.Context) hostContext;
        Object systemService = context2.getSystemService("window");
        if (!(systemService instanceof WindowManager)) {
            LOGGER.debug("getRealScreenSize get window manager error, return screen size: %{public}s", size);
            return size;
        }
        Display defaultDisplay = ((WindowManager) systemService).getDefaultDisplay();
        if (defaultDisplay == null) {
            LOGGER.debug("getRealScreenSize get default display error, return screen size: %{public}s", size);
            return size;
        }
        DisplayMetrics displayMetrics = new DisplayMetrics();
        boolean isLandScapeProduct = CustomConfigurationUtil.isLandScapeProduct();
        if (isLandScapeProduct) {
            defaultDisplay.getRealMetrics(displayMetrics);
        } else {
            displayMetrics = context2.getResources().getDisplayMetrics();
        }
        if (displayMetrics == null) {
            LOGGER.debug("getRealScreenSize get default display error, return screen size: %{public}s", size);
            return size;
        }
        Point point = new Point();
        defaultDisplay.getRealSize(point);
        int i = displayMetrics.widthPixels;
        int i2 = displayMetrics.heightPixels;
        LOGGER.debug("isLandScapeProduct = %{public}b, mScreenWidth=%{public}d, mScreenHeight=%{public}d", Boolean.valueOf(isLandScapeProduct), Integer.valueOf(i), Integer.valueOf(i2));
        if ((isLandScapeProduct && i < i2) || (!isLandScapeProduct && i > i2)) {
            int i3 = point.x;
            point.x = point.y;
            point.y = i3;
        }
        point.x -= CustomConfigurationUtil.getCurvedSidePadding(context);
        size.width = point.x;
        size.height = point.y;
        LOGGER.info("getRealScreenSize get screen size: %{public}s", size);
        return size;
    }

    public static int[] getRealNotchSize() {
        Method method = getNotchSizeMethod;
        if (method == null) {
            return new int[]{0, 0};
        }
        try {
            Object invoke = method.invoke(null, new Object[0]);
            if (invoke instanceof int[]) {
                if (((int[]) invoke).length == 2) {
                    int[] iArr = (int[]) invoke;
                    LOGGER.debug("getNotchSize: width = %{public}d, height = %{public}d", Integer.valueOf(iArr[0]), Integer.valueOf(iArr[1]));
                    return iArr;
                }
            }
            LOGGER.error("getRealNotchSize type error", new Object[0]);
            return new int[]{0, 0};
        } catch (InvocationTargetException e) {
            LOGGER.debug("getNotchSize: InvocationTargetException %{public}s", e);
            return new int[]{0, 0};
        } catch (IllegalAccessException e2) {
            LOGGER.debug("getNotchSize: IllegalAccessException %{public}s", e2);
            return new int[]{0, 0};
        }
    }
}
