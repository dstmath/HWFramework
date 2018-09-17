package android.hardware.display;

import android.graphics.Rect;
import android.text.TextUtils;

public final class DisplayViewport {
    public int deviceHeight;
    public int deviceWidth;
    public int displayId;
    public final Rect logicalFrame = new Rect();
    public int orientation;
    public final Rect physicalFrame = new Rect();
    public String uniqueId;
    public boolean valid;

    public void copyFrom(DisplayViewport viewport) {
        this.valid = viewport.valid;
        this.displayId = viewport.displayId;
        this.orientation = viewport.orientation;
        this.logicalFrame.set(viewport.logicalFrame);
        this.physicalFrame.set(viewport.physicalFrame);
        this.deviceWidth = viewport.deviceWidth;
        this.deviceHeight = viewport.deviceHeight;
        this.uniqueId = viewport.uniqueId;
    }

    public DisplayViewport makeCopy() {
        DisplayViewport dv = new DisplayViewport();
        dv.copyFrom(this);
        return dv;
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (o == this) {
            return true;
        }
        if (!(o instanceof DisplayViewport)) {
            return false;
        }
        DisplayViewport other = (DisplayViewport) o;
        if (this.valid == other.valid && this.displayId == other.displayId && this.orientation == other.orientation && this.logicalFrame.equals(other.logicalFrame) && this.physicalFrame.equals(other.physicalFrame) && this.deviceWidth == other.deviceWidth && this.deviceHeight == other.deviceHeight) {
            z = TextUtils.equals(this.uniqueId, other.uniqueId);
        }
        return z;
    }

    public int hashCode() {
        int i = 1;
        if (!this.valid) {
            i = 0;
        }
        int result = (i + 31) + 1;
        result += (result * 31) + this.displayId;
        result += (result * 31) + this.orientation;
        result += (result * 31) + this.logicalFrame.hashCode();
        result += (result * 31) + this.physicalFrame.hashCode();
        result += (result * 31) + this.deviceWidth;
        result += (result * 31) + this.deviceHeight;
        return result + ((result * 31) + this.uniqueId.hashCode());
    }

    public String toString() {
        return "DisplayViewport{valid=" + this.valid + ", displayId=" + this.displayId + ", uniqueId='" + this.uniqueId + "'" + ", orientation=" + this.orientation + ", logicalFrame=" + this.logicalFrame + ", physicalFrame=" + this.physicalFrame + ", deviceWidth=" + this.deviceWidth + ", deviceHeight=" + this.deviceHeight + "}";
    }
}
