package ohos.ivicommon.drivingsafety.model;

import ohos.agp.window.service.Window;
import ohos.agp.window.service.WindowManager;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class WindowPosition {
    private static final HiLogLabel TAG = new HiLogLabel(3, 0, "WindowPosition");
    private int height = -1;
    private int width = -1;
    private int xPos = -1;
    private int yPos = -1;

    public void getPosition() {
        WindowManager.LayoutConfig layoutConfig = (WindowManager.LayoutConfig) ((Window) WindowManager.getInstance().getTopWindow().get()).getLayoutConfig().get();
        this.xPos = layoutConfig.x;
        this.yPos = layoutConfig.y;
        this.height = layoutConfig.height;
        this.width = layoutConfig.width;
        HiLog.info(TAG, "window layout: x=%{public}d, y=%{public}d, height=%{public}d, width=%{public}d", Integer.valueOf(this.xPos), Integer.valueOf(this.yPos), Integer.valueOf(this.height), Integer.valueOf(this.width));
    }

    public int getXpos() {
        return this.xPos;
    }

    public int getYpos() {
        return this.yPos;
    }

    public int getHeight() {
        return this.height;
    }

    public int getWidth() {
        return this.width;
    }
}
