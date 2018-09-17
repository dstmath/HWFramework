package android.media;

import android.hardware.camera2.params.TonemapCurve;
import android.net.ProxyInfo;

/* compiled from: WebVttRenderer */
class TextTrackRegion {
    static final int SCROLL_VALUE_NONE = 300;
    static final int SCROLL_VALUE_SCROLL_UP = 301;
    float mAnchorPointX = TonemapCurve.LEVEL_BLACK;
    float mAnchorPointY = 100.0f;
    String mId = ProxyInfo.LOCAL_EXCL_LIST;
    int mLines = 3;
    int mScrollValue = 300;
    float mViewportAnchorPointX = TonemapCurve.LEVEL_BLACK;
    float mViewportAnchorPointY = 100.0f;
    float mWidth = 100.0f;

    TextTrackRegion() {
    }

    public String toString() {
        String str;
        StringBuilder append = new StringBuilder(" {id:\"").append(this.mId).append("\", width:").append(this.mWidth).append(", lines:").append(this.mLines).append(", anchorPoint:(").append(this.mAnchorPointX).append(", ").append(this.mAnchorPointY).append("), viewportAnchorPoints:").append(this.mViewportAnchorPointX).append(", ").append(this.mViewportAnchorPointY).append("), scrollValue:");
        if (this.mScrollValue == 300) {
            str = "none";
        } else if (this.mScrollValue == 301) {
            str = "scroll_up";
        } else {
            str = "INVALID";
        }
        return append.append(str).append("}").toString();
    }
}
