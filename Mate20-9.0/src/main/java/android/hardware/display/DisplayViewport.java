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
        boolean z = true;
        if (o == this) {
            return true;
        }
        if (!(o instanceof DisplayViewport)) {
            return false;
        }
        DisplayViewport other = (DisplayViewport) o;
        if (!(this.valid == other.valid && this.displayId == other.displayId && this.orientation == other.orientation && this.logicalFrame.equals(other.logicalFrame) && this.physicalFrame.equals(other.physicalFrame) && this.deviceWidth == other.deviceWidth && this.deviceHeight == other.deviceHeight && TextUtils.equals(this.uniqueId, other.uniqueId))) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        int result = 1 + (31 * 1) + (this.valid ? 1 : 0);
        int result2 = result + (31 * result) + this.displayId;
        int result3 = result2 + (31 * result2) + this.orientation;
        int result4 = result3 + (31 * result3) + this.logicalFrame.hashCode();
        int result5 = result4 + (31 * result4) + this.physicalFrame.hashCode();
        int result6 = result5 + (31 * result5) + this.deviceWidth;
        int result7 = result6 + (31 * result6) + this.deviceHeight;
        return result7 + (31 * result7) + this.uniqueId.hashCode();
    }

    public String toString() {
        return "DisplayViewport{valid=" + this.valid + ", displayId=" + this.displayId + ", uniqueId='" + this.uniqueId + "', orientation=" + this.orientation + ", logicalFrame=" + this.logicalFrame + ", physicalFrame=" + this.physicalFrame + ", deviceWidth=" + this.deviceWidth + ", deviceHeight=" + this.deviceHeight + "}";
    }
}
