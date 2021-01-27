package ohos.agp.window.wmc;

import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.window.wmc.AGPWindowManager;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class DisplayManagerWrapper {
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) LogDomain.END, "DisplayManagerWrapper");
    public static final int ROTATION_0 = 0;
    public static final int ROTATION_180 = 2;
    public static final int ROTATION_270 = 3;
    public static final int ROTATION_90 = 1;
    private static volatile DisplayManagerWrapper sInstance;

    public static class DisplayMetricsWrapper {
        public float density;
        public int densityDpi;
        public int heightPixels;
        public float scaledDensity;
        public int widthPixels;
        public float xdpi;
        public float ydpi;
    }

    public static class DisplayWrapper {
        private Display display;
        private DisplayMetricsWrapper mMetrics;
        private DisplayMetricsWrapper mRealMetrics;

        public DisplayWrapper(Display display2) {
            Point point = new Point();
            if (display2 != null) {
                display2.getSize(point);
                this.display = display2;
            }
        }

        public int getDisplayId() {
            checkDisplay();
            return this.display.getDisplayId();
        }

        public String getName() {
            checkDisplay();
            return this.display.getName();
        }

        public int getRotation() {
            checkDisplay();
            int rotation = this.display.getRotation();
            if (rotation == 0) {
                return 0;
            }
            if (rotation == 1) {
                return 1;
            }
            if (rotation != 2) {
                return rotation != 3 ? 0 : 3;
            }
            return 2;
        }

        public void getCurrentSizeRange(ohos.agp.utils.Point point, ohos.agp.utils.Point point2) {
            if (point == null || point2 == null) {
                HiLog.error(DisplayManagerWrapper.LABEL, "getCurrentSizeRange() outSmallestSize or outLargestSize is null", new Object[0]);
                return;
            }
            Point point3 = new Point();
            Point point4 = new Point();
            checkDisplay();
            this.display.getCurrentSizeRange(point3, point4);
            point.position[0] = (float) point3.x;
            point.position[1] = (float) point3.y;
            point2.position[0] = (float) point4.x;
            point2.position[1] = (float) point4.y;
        }

        public void getSize(ohos.agp.utils.Point point) {
            if (point == null) {
                HiLog.error(DisplayManagerWrapper.LABEL, "getSize() getSize is null", new Object[0]);
                return;
            }
            Point point2 = new Point();
            checkDisplay();
            this.display.getSize(point2);
            point.position[0] = (float) point2.x;
            point.position[1] = (float) point2.y;
        }

        public void getRealSize(ohos.agp.utils.Point point) {
            if (point == null) {
                HiLog.error(DisplayManagerWrapper.LABEL, "getRealSize() outSize is null", new Object[0]);
                return;
            }
            Point point2 = new Point();
            checkDisplay();
            this.display.getRealSize(point2);
            point.position[0] = (float) point2.x;
            point.position[1] = (float) point2.y;
        }

        public DisplayMetricsWrapper getDisplayMetricsWrapper() {
            if (this.mMetrics == null) {
                DisplayMetrics displayMetrics = new DisplayMetrics();
                checkDisplay();
                this.display.getMetrics(displayMetrics);
                this.mMetrics = new DisplayMetricsWrapper();
                transformMetrics(this.mMetrics, displayMetrics);
            }
            return this.mMetrics;
        }

        public DisplayMetricsWrapper getDisplayRealMetricsWrapper() {
            if (this.mRealMetrics == null) {
                DisplayMetrics displayMetrics = new DisplayMetrics();
                checkDisplay();
                this.display.getRealMetrics(displayMetrics);
                this.mRealMetrics = new DisplayMetricsWrapper();
                transformMetrics(this.mRealMetrics, displayMetrics);
            }
            return this.mRealMetrics;
        }

        public float getRefreshRate() {
            checkDisplay();
            return this.display.getRefreshRate();
        }

        private void transformMetrics(DisplayMetricsWrapper displayMetricsWrapper, DisplayMetrics displayMetrics) {
            displayMetricsWrapper.widthPixels = displayMetrics.widthPixels;
            displayMetricsWrapper.heightPixels = displayMetrics.heightPixels;
            displayMetricsWrapper.density = displayMetrics.density;
            displayMetricsWrapper.densityDpi = displayMetrics.densityDpi;
            displayMetricsWrapper.scaledDensity = displayMetrics.scaledDensity;
            displayMetricsWrapper.xdpi = displayMetrics.xdpi;
            displayMetricsWrapper.ydpi = displayMetrics.ydpi;
        }

        private void checkDisplay() {
            if (this.display == null) {
                HiLog.error(DisplayManagerWrapper.LABEL, "display is null", new Object[0]);
                throw new AGPWindowManager.BadWindowException("reason: display is null");
            }
        }
    }

    private DisplayManagerWrapper() {
    }

    public static DisplayManagerWrapper getInstance() {
        if (sInstance != null) {
            return sInstance;
        }
        synchronized (DisplayManagerWrapper.class) {
            if (sInstance == null) {
                sInstance = new DisplayManagerWrapper();
            }
        }
        return sInstance;
    }

    public List<DisplayWrapper> getDisplays(Context context) {
        Object hostContext = context.getHostContext();
        if (!(hostContext instanceof android.content.Context)) {
            HiLog.error(LABEL, "DisplayWrapper getDisplays androidContext is null", new Object[0]);
            return Collections.emptyList();
        }
        DisplayManager displayManager = (DisplayManager) ((android.content.Context) hostContext).getSystemService(DisplayManager.class);
        if (displayManager == null) {
            return Collections.emptyList();
        }
        return (List) Arrays.stream(displayManager.getDisplays()).map($$Lambda$VjlH3IT22fic4VkJUe9QWJLOryc.INSTANCE).collect(Collectors.collectingAndThen(Collectors.toList(), $$Lambda$DisplayManagerWrapper$y2fzDLbTCwV7HiJGBJd5ZxaUDJo.INSTANCE));
    }

    public Optional<DisplayWrapper> getDefaultDisplay(Context context) {
        if (context == null) {
            HiLog.error(LABEL, "getDefaultDisplay context is null", new Object[0]);
            return Optional.empty();
        }
        android.content.Context context2 = null;
        Object hostContext = context.getHostContext();
        if (hostContext instanceof android.content.Context) {
            context2 = (android.content.Context) hostContext;
        }
        if (context2 == null) {
            HiLog.error(LABEL, "DisplayWrapper getDefaultDisplay androidContext is null", new Object[0]);
            return Optional.empty();
        }
        Object systemService = context2.getSystemService("window");
        if (systemService == null) {
            return Optional.empty();
        }
        if (systemService instanceof WindowManager) {
            return Optional.of(new DisplayWrapper(((WindowManager) systemService).getDefaultDisplay()));
        }
        HiLog.error(LABEL, "Obj getSystemService is not instance of WindowManager", new Object[0]);
        return Optional.empty();
    }
}
