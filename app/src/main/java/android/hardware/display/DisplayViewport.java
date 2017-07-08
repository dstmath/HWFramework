package android.hardware.display;

import android.graphics.Rect;

public final class DisplayViewport {
    public int deviceHeight;
    public int deviceWidth;
    public int displayId;
    public final Rect logicalFrame;
    public int orientation;
    public final Rect physicalFrame;
    public boolean valid;

    public DisplayViewport() {
        this.logicalFrame = new Rect();
        this.physicalFrame = new Rect();
    }

    public void copyFrom(DisplayViewport viewport) {
        this.valid = viewport.valid;
        this.displayId = viewport.displayId;
        this.orientation = viewport.orientation;
        this.logicalFrame.set(viewport.logicalFrame);
        this.physicalFrame.set(viewport.physicalFrame);
        this.deviceWidth = viewport.deviceWidth;
        this.deviceHeight = viewport.deviceHeight;
    }

    public String toString() {
        return "DisplayViewport{valid=" + this.valid + ", displayId=" + this.displayId + ", orientation=" + this.orientation + ", logicalFrame=" + this.logicalFrame + ", physicalFrame=" + this.physicalFrame + ", deviceWidth=" + this.deviceWidth + ", deviceHeight=" + this.deviceHeight + "}";
    }
}
