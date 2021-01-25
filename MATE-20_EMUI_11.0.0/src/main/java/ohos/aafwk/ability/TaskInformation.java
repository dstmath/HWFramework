package ohos.aafwk.ability;

import ohos.media.image.PixelMap;

public class TaskInformation {
    private static final int COLOR_MAX = 255;
    private static final int COLOR_MIN = 0;
    private int colorPrimary;
    private PixelMap icon;
    private String label;

    private int colorDeal(int i) {
        return i >>> 24;
    }

    public TaskInformation() {
        this(null, null, 0);
    }

    public TaskInformation(String str) {
        this(str, null, 0);
    }

    public TaskInformation(String str, PixelMap pixelMap) {
        this(str, pixelMap, 0);
    }

    public TaskInformation(String str, PixelMap pixelMap, int i) {
        if (i == 0 || colorDeal(i) == 255) {
            this.label = str;
            this.icon = pixelMap;
            this.colorPrimary = i;
            return;
        }
        throw new IllegalArgumentException("color primary should be opaque");
    }

    public String getLabel() {
        return this.label;
    }

    public PixelMap getIcon() {
        return this.icon;
    }

    public int getColorPrimary() {
        return this.colorPrimary;
    }

    public void setLabel(String str) {
        this.label = str;
    }

    public void setIcon(PixelMap pixelMap) {
        this.icon = pixelMap;
    }

    public void setColorPrimary(int i) {
        this.colorPrimary = i;
    }
}
