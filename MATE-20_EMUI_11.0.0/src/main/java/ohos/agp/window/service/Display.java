package ohos.agp.window.service;

import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.utils.Point;
import ohos.agp.window.service.WindowManager;
import ohos.agp.window.wmc.DisplayManagerWrapper;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class Display {
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) LogDomain.END, "Display");
    private DisplayManagerWrapper.DisplayWrapper displayWrapper;
    private DisplayAttributes mAttributes;
    private DisplayAttributes mRealAttributes;

    public Display() {
    }

    public Display(DisplayManagerWrapper.DisplayWrapper displayWrapper2) {
        this.displayWrapper = displayWrapper2;
    }

    public int getDisplayId() {
        checkDisplayWrapper();
        return this.displayWrapper.getDisplayId();
    }

    public int getRotation() {
        checkDisplayWrapper();
        return this.displayWrapper.getRotation();
    }

    public DisplayAttributes getAttributes() {
        checkDisplayWrapper();
        if (this.mAttributes == null) {
            this.mAttributes = new DisplayAttributes();
            transformAttributes(this.mAttributes, this.displayWrapper.getDisplayMetricsWrapper());
        }
        return this.mAttributes;
    }

    public DisplayAttributes getRealAttributes() {
        checkDisplayWrapper();
        if (this.mRealAttributes == null) {
            this.mRealAttributes = new DisplayAttributes();
            transformAttributes(this.mRealAttributes, this.displayWrapper.getDisplayRealMetricsWrapper());
        }
        return this.mRealAttributes;
    }

    public void getCurrentSizeRange(Point point, Point point2) {
        checkDisplayWrapper();
        this.displayWrapper.getCurrentSizeRange(point, point2);
    }

    public void getSize(Point point) {
        checkDisplayWrapper();
        this.displayWrapper.getSize(point);
    }

    public void getRealSize(Point point) {
        checkDisplayWrapper();
        this.displayWrapper.getRealSize(point);
    }

    private void transformAttributes(DisplayAttributes displayAttributes, DisplayManagerWrapper.DisplayMetricsWrapper displayMetricsWrapper) {
        displayAttributes.width = displayMetricsWrapper.widthPixels;
        displayAttributes.height = displayMetricsWrapper.heightPixels;
        displayAttributes.densityPixels = displayMetricsWrapper.density;
        displayAttributes.densityDpi = displayMetricsWrapper.densityDpi;
        displayAttributes.scalDensity = displayMetricsWrapper.scaledDensity;
        displayAttributes.xDpi = displayMetricsWrapper.xdpi;
        displayAttributes.yDpi = displayMetricsWrapper.ydpi;
    }

    private void checkDisplayWrapper() {
        if (this.displayWrapper == null) {
            HiLog.error(LABEL, "displayWrapper is null", new Object[0]);
            throw new WindowManager.PermissionException("reason: displayWrapper is null");
        }
    }
}
